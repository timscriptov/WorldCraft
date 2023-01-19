package com.solverlabs.droid.rugl;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
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
    protected Game mGame;
    protected ProgressDialog mLoadingDialog;
    protected AlertDialog mReadOnlyMapNotificationDialog;
    private GameView mGameView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Call this in your {@link #onCreate(android.os.Bundle)}
     * implementation
     *
     * @param game
     */
    public void start(Game game) {
        mGame = game;
        ResourceLoader.start(getResources());
        setContentView(R.layout.main_without_banner);
        mGameView = (GameView) findViewById(R.id.gameViewWithoutBanner);
        mGameView.init(game);
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
        final Dialog menuDialog = new Dialog(this);
        menuDialog.setContentView(R.layout.menulayout);
        menuDialog.setTitle("Game menu");
        Button quitButton = (Button) menuDialog.findViewById(R.id.quitButton);
        Button backButton = (Button) menuDialog.findViewById(R.id.backButton);
        Button playersList = (Button) menuDialog.findViewById(R.id.playerListButton);
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
        playerListDialog.setTitle("Player list       Room name:  " + Multiplayer.instance.roomName);
        list.add(Multiplayer.instance.playerName + "   (you)");
        Set<Enemy> sortedEnemies = new TreeSet<>(Multiplayer.getEnemiesCopy());
        for (Enemy enemy : sortedEnemies) {
            list.add(enemy.name);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.custom_list_content, R.id.list_content, list);
        ListView playerList = (ListView) playerListDialog.findViewById(R.id.playerListView);
        playerList.setAdapter((ListAdapter) dataAdapter);
        Button cancelButton = (Button) playerListDialog.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(v -> playerListDialog.dismiss());
        playerListDialog.show();
    }

    private void showLoadingDialog(final String message) {
        try {
            runOnUiThread(() -> {
                if (!isFinishing()) {
                    mLoadingDialog = ProgressDialog.show(GameActivity.this, DescriptionFactory.emptyText, message, true);
                    mLoadingDialog.setCancelable(false);
                    mLoadingDialog.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissLoadingDialog() {
        try {
            runOnUiThread(() -> {
                if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
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
                    mGameView.mGame.getBlockView().saveWorld(worldName);
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
                    mGameView.mGame.getBlockView().complete(needSaveWorld);
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
            if (mReadOnlyMapNotificationDialog == null || !mReadOnlyMapNotificationDialog.isShowing()) {
                final EditText input = new EditText(GameActivity.this);
                input.setHint(R.string.world_name);
                input.setText(Multiplayer.instance.roomName);
                KeyboardUtils.hideKeyboardOnEnter(GameActivity.this, input);
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(GameActivity.this);
                materialAlertDialogBuilder.setTitle(R.string.save_world);
                materialAlertDialogBuilder.setMessage(stringId);
                materialAlertDialogBuilder.setView(input);
                materialAlertDialogBuilder.setCancelable(false);
                materialAlertDialogBuilder.setPositiveButton(R.string.save_world, (dialog, id) -> {
                    String worldName = input.getText().toString();
                    if (!DescriptionFactory.emptyText.equals(worldName)) {
                        saveWorld(worldName);
                        hideKeyBoard(input);
                        return;
                    }
                    Toast.makeText(GameActivity.this, R.string.wrong_world_name, Toast.LENGTH_LONG).show();
                });
                materialAlertDialogBuilder.setNeutralButton(noButtonResourseId, (dialog, id) -> {
                    if (completePhaseOnNoClick) {
                        completeCurrentPhase(false);
                    }
                });
                mReadOnlyMapNotificationDialog = materialAlertDialogBuilder.create();
                materialAlertDialogBuilder.show();
            }
        });
    }

    public void showLikeDialog() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setTitle(R.string.like_it);
        materialAlertDialogBuilder.setMessage(R.string.did_you_like_this_world);
        materialAlertDialogBuilder.setCancelable(false).setPositiveButton(R.string.like, (dialog, id) -> {
            Multiplayer.instance.likeWorld();
            dialog.dismiss();
            showSaveWorldDialog();
        });
        materialAlertDialogBuilder.setNegativeButton(R.string.dislike, (dialog, id) -> {
            Multiplayer.instance.dislikeWorld();
            dialog.dismiss();
            showSaveWorldDialog();
        });
        materialAlertDialogBuilder.show();
    }

    public void sendChatMessage(@NonNull final EditText msg) {
        BlockView phase;
        final String messageText = String.valueOf(msg.getText());
        if (CHAT_COMMAND_HOME.equals(messageText)) {
            if (mGame != null && mGame.getBlockView() != null && (phase = mGame.getBlockView()) != null) {
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
        final EditText msg = new EditText(this);
        msg.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ChatBox.getMaxChatMessageLength())});

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setTitle(R.string.your_message);
        materialAlertDialogBuilder.setView(msg);
        materialAlertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, id) -> sendChatMessage(msg));
        materialAlertDialogBuilder.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
        final AlertDialog alert = materialAlertDialogBuilder.create();
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
        if (mGameView != null) {
            mGameView.onPause();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGameView != null) {
            mGameView.onResume();
        }
        if (mGame != null) {
            mGame.resetTouches();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getRepeatCount() == 0 && mGameView != null && mGameView.mGame != null) {
            BlockView p = mGameView.mGame.getBlockView();
            if (p != null) {
                if (keyCode == 4) {
                    showGameMenuDialog();
                } else {
                    p.onKeyDown(keyCode, event);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mGameView != null && mGameView.mGame != null) {
            BlockView p = mGameView.mGame.getBlockView();
            if (p != null) {
                p.onKeyUp(keyCode, event);
                return true;
            }
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
