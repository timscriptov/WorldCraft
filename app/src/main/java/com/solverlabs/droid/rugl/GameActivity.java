package com.solverlabs.droid.rugl;

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
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

/**
 * Handy activity that can be simply subclassed. Just remember to call
 * {@link #start(Game, String)} in your
 * {@link #onCreate(android.os.Bundle)} or nothing will happen.
 * Handles starting the {@link ResourceLoader} and key input
 */
public abstract class GameActivity extends CommonActivity implements Runnable {
    private static final String CHAT_COMMAND_HOME = "/home";
    /**
     * The {@link Game}
     */
    protected Game game;
    protected ProgressDialog loadingDialog;
    protected AlertDialog readOnlyMapNotificationDialog;
    private GameView gameView;

    /**
     * Call this in your {@link #onCreate(android.os.Bundle)}
     * implementation
     *
     * @param game
     */
    public void start(Game game) {
        this.game = game;
        ResourceLoader.start(getResources());
        setContentView(R.layout.activity_game);
        this.gameView = findViewById(R.id.gameViewWithoutBanner);
        this.gameView.init(game);
        Thread reportAbuseCatcher = new Thread(this);
        reportAbuseCatcher.start();
    }

    /**
     * Displays a short message to the user
     *
     * @param message
     * @param longShow <code>true</code> for {@link Toast#LENGTH_LONG},
     *                 <code>false</code> for {@link Toast#LENGTH_SHORT}
     */
    public void showToast(final String message, final boolean longShow) {
        runOnUiThread(() -> {
            Toast t = Toast.makeText(getApplicationContext(), message, longShow ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            t.show();
        });
    }

    public void showGameMenuDialog() {
        final View view = View.inflate(this, R.layout.menulayout, null);
        Button quitButton = view.findViewById(R.id.quitButton);
        Button backButton = view.findViewById(R.id.backButton);
        Button playersList = view.findViewById(R.id.playerListButton);

        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(view);
        builder.setTitle("Game menu");
        final AlertDialog dialog = builder.create();
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
            dialog.dismiss();
        });
        backButton.setOnClickListener(v -> dialog.dismiss());
        playersList.setOnClickListener(v -> {
            showPlayerList();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showPlayerList() {
        final View view = View.inflate(this, R.layout.playerlist, null);

        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(view);
        ArrayList<String> list = new ArrayList<>();
        builder.setTitle("Player list       Room name:  " + Multiplayer.instance.roomName);
        list.add(Multiplayer.instance.playerName + "   (you)");
        Set<Enemy> sortedEnemies = new TreeSet<>(Multiplayer.getEnemiesCopy());
        for (Enemy enemy : sortedEnemies) {
            list.add(enemy.name);
        }
        final AlertDialog dialog = builder.create();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.custom_list_content, R.id.list_content, list);
        ListView playerList = view.findViewById(R.id.playerListView);
        playerList.setAdapter(dataAdapter);
        Button cancelButton = view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
                final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.save_world);
                builder.setMessage(stringId);
                builder.setView(input);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.save_world, (dialog, id) -> {
                    String worldName = String.valueOf(input.getText());
                    if (!DescriptionFactory.emptyText.equals(worldName) && !"null".equals(worldName)) {
                        saveWorld(worldName);
                        hideKeyBoard(input);
                        return;
                    }
                    Toast.makeText(GameActivity.this, R.string.wrong_world_name, Toast.LENGTH_LONG).show();
                });
                builder.setNeutralButton(noButtonResourseId, (dialog, id) -> {
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
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.like_it);
        builder.setMessage(R.string.did_you_like_this_world);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.like, (dialog, id) -> {
            Multiplayer.instance.likeWorld();
            dialog.dismiss();
            showSaveWorldDialog();
        });
        builder.setNegativeButton(R.string.dislike, (dialog, id) -> {
            Multiplayer.instance.dislikeWorld();
            dialog.dismiss();
            showSaveWorldDialog();
        });
        builder.show();
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
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
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