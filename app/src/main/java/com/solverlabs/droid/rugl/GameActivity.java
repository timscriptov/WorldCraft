package com.solverlabs.droid.rugl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.worldcraft.BlockView;
import com.solverlabs.worldcraft.Enemy;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.activity.CommonActivity;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.multiplayer.dialogs.PopupDialog;
import com.solverlabs.worldcraft.multiplayer.util.TextUtils;
import com.solverlabs.worldcraft.ui.ChatBox;
import com.solverlabs.worldcraft.util.KeyboardUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public abstract class GameActivity extends CommonActivity implements Runnable {
    private static final String CHAT_COMMAND_HOME = "/home";
    protected Game game;
    private GameView gameView;
    protected ProgressDialog loadingDialog;
    protected AlertDialog readOnlyMapNotificationDialog;

    public void start(Game game) {
        this.game = game;
        ResourceLoader.start(getResources());
        setContentView(R.layout.main_without_banner);
        this.gameView = findViewById(R.id.gameViewWithoutBanner);
        this.gameView.init(game);
        Thread reportAbuseCatcher = new Thread(this);
        reportAbuseCatcher.start();
    }

    public void showToast(final String message, final boolean longShow) {
        runOnUiThread(() -> {
            Toast t = Toast.makeText(getApplicationContext(), message, longShow ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            t.show();
        });
    }

    public void showGameMenuDialog() {
        final Dialog menuDialog = new Dialog(this);
        menuDialog.setContentView(R.layout.menulayout);
        menuDialog.setTitle("Game menu");
        Button quitButton = menuDialog.findViewById(R.id.quitButton);
        Button backButton = menuDialog.findViewById(R.id.backButton);
        Button playersList = menuDialog.findViewById(R.id.playerListButton);
        if (GameMode.isMultiplayerMode()) {
            playersList.setVisibility(View.VISIBLE);
        } else {
            playersList.setVisibility(View.GONE);
        }
        quitButton.setOnClickListener(v -> {
            if (GameMode.isMultiplayerMode()) {
                showLikeDialog();
            } else {
                completeCurrentPhase(true);
            }
            SoundManager.stopAllSounds();
            menuDialog.dismiss();
        });
        backButton.setOnClickListener(v -> menuDialog.dismiss());
        playersList.setOnClickListener(v -> {
            showPlayerList();
            menuDialog.dismiss();
        });
        menuDialog.show();
    }

    public void showPlayerList() {
        final Dialog playerListDialog = new Dialog(this);
        playerListDialog.setContentView(R.layout.playerlist);
        ArrayList<String> list = new ArrayList<>();
        if (Multiplayer.instance != null) {
            playerListDialog.setTitle("Player list       Room name:  " + Multiplayer.instance.roomName);
            list.add(Multiplayer.instance.playerName + "   (you)");
            Set<Enemy> sortedEnemies = new TreeSet<>(Multiplayer.getEnemiesCopy());
            for (Enemy enemy : sortedEnemies) {
                list.add(enemy.name);
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.custom_list_content, R.id.list_content, list);
        ListView playerList = playerListDialog.findViewById(R.id.playerListView);
        playerList.setAdapter(dataAdapter);
        Button cancelButton = playerListDialog.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(v -> playerListDialog.dismiss());
        playerListDialog.show();
    }

    private void showLoadingDialog(final String message) {
        try {
            runOnUiThread(() -> {
                if (!isFinishing()) {
                    loadingDialog = ProgressDialog.show(GameActivity.this, DescriptionFactory.emptyText, message, true);
                    loadingDialog.setCancelable(false);
                    loadingDialog.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissLoadingDialog() {
        try {
            runOnUiThread(() -> {
                if (loadingDialog != null && !loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveWorld(final String worldName) {
        new Thread() {
            @Override
            public void run() {
                try {
                    gameView.game.getBlockView().saveWorld(worldName);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                try {
                    completeCurrentPhase(true);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }.start();
    }

    public void completeCurrentPhase(final boolean needSaveWorld) {
        new Thread() {
            @Override
            public void run() {
                try {
                    gameView.game.getBlockView().complete(needSaveWorld);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                try {
                    Multiplayer.instance.shutdown();
                    dismissLoadingDialog();
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }.start();
    }

    public void showSaveWorldDialogOnConnectionLost() {
        showSaveWorldDialog(R.string.connection_lost_would_you_like_to_save_world);
    }

    public void showSaveWorldDialog() {
        showSaveWorldDialog(R.string.would_you_like_to_save_world);
    }

    public void showReadOnlyRoomModificationDialog() {
        showSaveWorldDialog(R.string.modification_read_only_world_warning, 17039360, false);
    }

    public void showSaveWorldDialog(int stringId) {
        showSaveWorldDialog(stringId, R.string.dont_save, true);
    }

    public void showSaveWorldDialog(final int stringId, final int noButtonResourseId, final boolean completePhaseOnNoClick) {
        runOnUiThread(() -> {
            if (readOnlyMapNotificationDialog == null || !readOnlyMapNotificationDialog.isShowing()) {
                final EditText input = new EditText(GameActivity.this);
                input.setHint(R.string.world_name);
                input.setText(Multiplayer.instance.roomName);
                KeyboardUtils.hideKeyboardOnEnter(GameActivity.this, input);
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                builder.setTitle(R.string.save_world).setMessage(stringId).setView(input).setCancelable(false).setPositiveButton(R.string.save_world, (dialog, id) -> {
                    String worldName = String.valueOf(input.getText());
                    if (worldName != null && !DescriptionFactory.emptyText.equals(worldName) && !"null".equals(worldName)) {
                        saveWorld(worldName);
                        hideKeyBoard(input);
                        return;
                    }
                    Toast.makeText(GameActivity.this, R.string.wrong_world_name, Toast.LENGTH_LONG).show();
                }).setNeutralButton(noButtonResourseId, (dialog, id) -> {
                    if (completePhaseOnNoClick) {
                        completeCurrentPhase(false);
                    }
                });
                readOnlyMapNotificationDialog = builder.create();
                readOnlyMapNotificationDialog.getWindow().setSoftInputMode(2);
                readOnlyMapNotificationDialog.show();
            }
        });
    }

    public void showLikeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.like_it).setMessage(R.string.did_you_like_this_world).setCancelable(false).setPositiveButton(R.string.like, (dialog, id) -> {
            Multiplayer.instance.likeWorld();
            dialog.dismiss();
            showSaveWorldDialog();
        }).setNegativeButton(R.string.dislike, (dialog, id) -> {
            Multiplayer.instance.dislikeWorld();
            dialog.dismiss();
            showSaveWorldDialog();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void sendChatMessage(@NonNull final EditText msg) {
        BlockView phase;
        final String messageText = String.valueOf(msg.getText());
        if (CHAT_COMMAND_HOME.equals(messageText)) {
            if (this.game != null && this.game.getBlockView() != null && (phase = this.game.getBlockView()) != null && (phase instanceof BlockView)) {
                phase.resetPlayerLocation();
                return;
            }
            return;
        }
        new Thread() {
            @Override
            public void run() {
                if (Multiplayer.instance.gameClient != null && TextUtils.isEditTextMessageEmpty(msg)) {
                    Multiplayer.instance.gameClient.chat(messageText);
                }
            }
        }.start();
    }

    public void showChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText msg = new EditText(this);
        msg.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ChatBox.getMaxChatMessageLength())});
        builder.setTitle(R.string.your_message).setView(msg).setPositiveButton(android.R.string.ok, (dialog, id) -> sendChatMessage(msg)).setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
        final AlertDialog alert = builder.create();
        msg.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null || event.getKeyCode() != 66) {
                return false;
            }
            sendChatMessage(msg);
            alert.dismiss();
            return true;
        });
        alert.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissLoadingDialog();
        if (this.gameView != null) {
            this.gameView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.gameView != null) {
            this.gameView.onResume();
        }
        if (this.game != null) {
            this.game.resetTouches();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        BlockView p;
        if (event.getRepeatCount() == 0 && this.gameView != null && this.gameView.game != null && (p = this.gameView.game.getBlockView()) != null) {
            if (keyCode == 4 && (p instanceof BlockView)) {
                showGameMenuDialog();
            } else {
                p.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        BlockView p;
        if (this.gameView != null && this.gameView.game != null && (p = this.gameView.game.getBlockView()) != null) {
            p.onKeyUp(keyCode, event);
            return true;
        }
        return true;
    }

    public void hideKeyBoard(@NonNull View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void run() {
        while (GameMode.isMultiplayerMode()) {
            String message = Multiplayer.pollPopupMessage();
            if (message != null) {
                PopupDialog.showInUiThread(R.string.warning, message, this);
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}