package com.solverlabs.worldcraft.multiplayer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.MyApplication;
import com.solverlabs.worldcraft.Persistence;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.multiplayer.compress.CompressedWorldDownloader;
import com.solverlabs.worldcraft.multiplayer.compress.CompressedWorldUploader;
import com.solverlabs.worldcraft.multiplayer.dialogs.CreateRoomDialog;
import com.solverlabs.worldcraft.multiplayer.dialogs.RoomlistDialog;
import com.solverlabs.worldcraft.multiplayer.util.DeviceUtils;
import com.solverlabs.worldcraft.multiplayer.util.NetworkUtils;
import com.solverlabs.worldcraft.multiplayer.util.Vector3fUtils;
import com.solverlabs.worldcraft.multiplayer.util.WorldCopier;
import com.solverlabs.worldcraft.srv.client.AndroidClient;
import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.srv.domain.Room;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.util.GameStarter;
import com.solverlabs.worldcraft.util.Properties;
import com.solverlabs.worldcraft.util.WorldGenerator;

import org.jboss.netty.util.internal.jzlib.JZlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class MultiplayerActivityHelper implements AndroidClient.MultiplayerListener, MovementHandler.MovementHandlerListener {
    private Activity activity;
    private Map<List<Short>, Room.BlockData> blocks = new HashMap();
    private String createRoomName;
    private String createWorldName;
    private ObjectCodec.RoomPack currentRoomPack;
    private ProgressDialog loadingDialog;
    private RoomlistDialog roomlistDialog;
    private CreateRoomDialog selectWorldDialog;

    public MultiplayerActivityHelper(Activity activity) {
        this.activity = activity;
    }

    public void startMultiplayer() {
        Multiplayer.instance.isInMultiplayerMode = true;
        if (!WorldUtils.isStorageAvailable(this.activity)) {
            WorldUtils.showStorageNotFoundDialog(this.activity);
        } else if (!NetworkUtils.isNetworkAvailable(this.activity)) {
            showErrorDialog(R.string.please_turn_on_network);
            Multiplayer.instance.isInMultiplayerMode = false;
        } else {
            startMultiplayerGameClient();
        }
    }

    public void onResume(Activity activity) {
        dismissOpenedDialogs();
        this.activity = activity;
    }

    public void onPause() {
        dismissOpenedDialogs();
        this.activity = null;
        this.selectWorldDialog = null;
        this.blocks = null;
        this.loadingDialog = null;
    }

    @Override
    public void myLocationChanged(Vector3f eye, Vector3f at, Vector3f up) {
        if (Multiplayer.instance.gameClient != null) {
            Multiplayer.instance.gameClient.move(Vector3fUtils.convert(eye), Vector3fUtils.convert(at), Vector3fUtils.convert(up));
        }
    }

    @Override
    public void myGraphicsInited(Vector3f eye, Vector3f at, Vector3f up) {
        if (Multiplayer.instance.gameClient != null) {
            Multiplayer.instance.invalidateEnemies();
            Multiplayer.instance.gameClient.graphicsInited(Vector3fUtils.convert(eye), Vector3fUtils.convert(at), Vector3fUtils.convert(up));
        }
    }

    @Override
    public void myAction(byte action) {
        if (Multiplayer.instance.gameClient != null) {
            Multiplayer.instance.gameClient.action(action);
        }
    }

    @Override
    public void onConnectionEstablished() {
        if (Multiplayer.instance.gameClient != null) {
            Multiplayer.checkVersion();
        }
    }

    @Override
    public void onConnectionFailed(String message, Throwable e) {
        if (this.activity != null) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissOpenedDialogs();
                    if (Multiplayer.instance.isWorldShowing && Multiplayer.instance.gameActivity != null) {
                        Multiplayer.instance.gameActivity.showSaveWorldDialogOnConnectionLost();
                        return;
                    }
                    Multiplayer.instance.shutdown();
                    showErrorDialog(R.string.connection_lost);
                }
            });
        }
    }

    @Override
    public void onLoginOk(int playerId, String playerName) {
        Multiplayer.instance.playerId = playerId;
        Multiplayer.setPlayerName(playerName);
        roomlistRequest();
    }

    @Override
    public void onLoginFail(final byte errorCode, final String message) {
        try {
            shutdownMultiplayer();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String dialogMessage = message != null ? message : getErrorLoginMessage(errorCode);
                showErrorDialog(dialogMessage);
            }
        });
    }

    @Override
    public void onCheckVersionOk() {
        Multiplayer.instance.gameClient.login();
    }

    public String getErrorLoginMessage(byte errorCode) {
        switch (errorCode) {
            case -7:
                return this.activity.getString(R.string.error_login_name_forbidden);
            case JZlib.Z_VERSION_ERROR /* -6 */:
                return this.activity.getString(R.string.error_login_too_long);
            case JZlib.Z_BUF_ERROR /* -5 */:
                return this.activity.getString(R.string.error_login_bad_word);
            case JZlib.Z_MEM_ERROR /* -4 */:
                return this.activity.getString(R.string.error_login_device_id_blacklisted);
            case JZlib.Z_DATA_ERROR /* -3 */:
                return this.activity.getString(R.string.error_login_ip_blacklisted);
            case -2:
                return this.activity.getString(R.string.error_login_null_player);
            case -1:
                return this.activity.getString(R.string.error_login_already_logged_in);
            default:
                return this.activity.getString(R.string.error_login_unknown_error);
        }
    }

    @Override
    public void onRoomListLoaded(final Collection<ObjectCodec.RoomPack> roomsReadOnly, final Collection<ObjectCodec.RoomPack> roomsByPlayerNumber, final Collection<ObjectCodec.RoomPack> roomsByRating, final Collection<ObjectCodec.RoomPack> roomsSearch, final short initRoomlistSize) {
        this.activity.runOnUiThread(() -> roomlistDialog.onRoomlistLoaded(roomsReadOnly, roomsByPlayerNumber, roomsByRating, roomsSearch, initRoomlistSize));
    }

    @Override
    public void onCreateRoomOk(final String uploadToken) {
        if (this.selectWorldDialog != null) {
            this.selectWorldDialog.dismiss();
        }
        showLoadingDialog();
        new Thread() {
            @Override
            public void run() {
                WorldCopier wc = new WorldCopier(createWorldName);
                wc.copyWorld();
                CompressedWorldUploader uploader = new CompressedWorldUploader(Properties.MULTIPLAYER_WORLD_NAME, uploadToken);
                final boolean ok = uploader.upload();
                activity.runOnUiThread(() -> {
                    dismissLoadingDialog();
                    if (ok) {
                        startGameActivity();
                        return;
                    }
                    showErrorDialog(R.string.upload_world_failed);
                    Multiplayer.instance.shutdown();
                });
            }
        }.start();
    }

    @Override
    public void onCreateRoomFailed(final byte error, final String message) {
        this.activity.runOnUiThread(() -> {
            Toast.makeText(activity, getCreateRoomErrorMessage(error, message), Toast.LENGTH_LONG).show();
            if (selectWorldDialog != null) {
                selectWorldDialog.show();
            }
        });
    }

    public String getCreateRoomErrorMessage(byte errorCode, String errorMessage) {
        switch (errorCode) {
            case -9:
                return this.activity.getString(R.string.error_create_room_password_too_long);
            case -8:
                return this.activity.getString(R.string.error_create_room_name_forbidden);
            case -7:
                return this.activity.getString(R.string.error_create_room_name_too_long);
            case JZlib.Z_VERSION_ERROR /* -6 */:
                return this.activity.getString(R.string.error_create_room_bad_word);
            case JZlib.Z_BUF_ERROR /* -5 */:
                return this.activity.getString(R.string.error_create_room_failed, new Object[]{this.createRoomName});
            case JZlib.Z_MEM_ERROR /* -4 */:
            default:
                return errorMessage != null ? errorMessage : this.activity.getString(R.string.unable_to_create_room);
            case JZlib.Z_DATA_ERROR /* -3 */:
                return this.activity.getString(R.string.error_create_room_name_exists, new Object[]{this.createRoomName});
            case -2:
                return this.activity.getString(R.string.error_create_room_non_logged_in_user);
            case -1:
                return this.activity.getString(R.string.error_create_room_null_user);
        }
    }

    @Override
    public void onJoinRoomOk(boolean isOwner, boolean isReadOnly) {
        if (this.currentRoomPack != null && this.activity != null) {
            this.roomlistDialog.dismiss();
            String downloadUrl = Properties.WORLD_DOWNLOAD_URL + this.currentRoomPack.id + "/" + Properties.COMPRESSED_WORLD_NAME;
            Multiplayer.setRoomOwner(isOwner);
            Multiplayer.setReadOnly(isReadOnly);
            final CompressedWorldDownloader downloader = new CompressedWorldDownloader(downloadUrl);
            new Thread() {
                @Override
                public void run() {
                    if (downloader.download()) {
                        activity.runOnUiThread(() -> startGameActivity());
                    } else {
                        activity.runOnUiThread(() -> {
                            dismissLoadingDialog();
                            showErrorDialog(R.string.download_world_failed);
                            Multiplayer.instance.shutdown();
                        });
                    }
                }
            }.start();
        }
    }

    @Override
    public void onJoinRoomFailed(final byte errorCode, final String message) {
        this.activity.runOnUiThread(() -> {
            dismissLoadingDialog();
            Toast.makeText(activity, message != null ? message : getJoinRoomErrorMessage(errorCode), Toast.LENGTH_LONG).show();
        });
    }

    public String getJoinRoomErrorMessage(byte errorCode) {
        switch (errorCode) {
            case JZlib.Z_MEM_ERROR /* -4 */:
                return this.activity.getString(R.string.enter_room_failed_player_limit_exceeded);
            case JZlib.Z_DATA_ERROR /* -3 */:
                return this.activity.getString(R.string.enter_room_failed_wrong_password);
            case -2:
                return this.activity.getString(R.string.error_join_room_failed);
            case -1:
                return this.activity.getString(R.string.error_join_room_doesnt_exist);
            default:
                return this.activity.getString(R.string.error_join_room_failed);
        }
    }

    @Override
    public void onEnemyInfo(Player player) {
        Multiplayer.instance.playerInfo(player);
    }

    @Override
    public void onEnemyMove(Camera camera) {
        Multiplayer.instance.moveEnemy(camera);
    }

    @Override
    public void onEnemyAction(Integer playerId, byte action) {
        if (playerId != null) {
            Multiplayer.instance.actionEnemy(playerId, action);
        }
    }

    @Override
    public void onEnemyDisconnected(Integer enemyId) {
        Multiplayer.removeMessages(enemyId);
        Multiplayer.instance.removeEnemy(enemyId);
    }

    @Override
    public void onSetBlockType(int x, int y, int z, int chunkX, int chunkZ, byte blockType, byte blockData) {
        List<Short> arrayList = new ArrayList<>();
        arrayList.add((short) x);
        arrayList.add((short) y);
        arrayList.add((short) z);
        arrayList.add((short) chunkX);
        arrayList.add((short) chunkZ);
        Map<List<Short>, Room.BlockData> blockMap = new HashMap<>();
        blockMap.put(arrayList, new Room.BlockData(blockType, blockData));
        onModifiedBlocksRecieved(blockMap);
    }

    @Override
    public void onCheckVersionWarning(String str) {

    }

    @Override
    public void onCheckVersionCritical(String str) {

    }

    @Override
    public void onChatMesssageReceived(String msg) {
        Multiplayer.addMessage(msg);
        if (Multiplayer.instance.blockView != null && Multiplayer.instance.blockView.gui != null && Multiplayer.instance.blockView.gui.chatBox != null) {
            Multiplayer.instance.blockView.gui.chatBox.addMessage(msg);
        }
    }

    @Override
    public void onMoveResponse() {
        Multiplayer.instance.movementHandler.responseReceived = true;
    }

    @Override
    public void onModifiedBlocksRecieved(Map<List<Short>, Room.BlockData> blockMap) {
        if (blockMap == null) {
            blockMap = new HashMap<>();
        }
        if (Multiplayer.instance.interaction != null) {
            if (Multiplayer.instance.isClientGraphicInited) {
                if (this.blocks != null) {
                    blockMap.putAll(this.blocks);
                    this.blocks.clear();
                }
                Multiplayer.instance.interaction.setBlocks(blockMap);
                Multiplayer.instance.isWorldReady = true;
            } else if (this.blocks != null) {
                this.blocks.putAll(blockMap);
            }
        }
    }

    @Override
    public void onReconnectFinished() {
        Multiplayer.instance.clearEnemies();
    }

    @Override
    public void onPopupMessage(String message) {
        Multiplayer.addPopupMessage(message);
    }

    @Override
    public void onReadOnlyRoomModification() {
        Multiplayer.showReadOnlyRoomModificationDialog();
    }

    private void startMultiplayerGameClient() {
        showLoadingDialog();
        Multiplayer.instance.init(Persistence.getInstance().getPlayerName(), Persistence.getInstance().getPlayerSkin(), DeviceUtils.getAppVersion(this.activity), DeviceUtils.getAppBuildNumber(this.activity), DeviceUtils.getDeviceId(this.activity), this, this);
        Multiplayer.instance.startGameClient();
    }

    public void shutdownMultiplayer() {
        dismissLoadingDialog();
        Multiplayer.instance.shutdown();
    }

    private void showRoomlistDialog(final Collection<ObjectCodec.RoomPack> roomsByEntranceNumber, final Collection<ObjectCodec.RoomPack> roomsByPlayerNumber, final Collection<ObjectCodec.RoomPack> roomsByRating) {
        this.activity.runOnUiThread(() -> {
            if (roomlistDialog == null) {
                roomlistDialog = new RoomlistDialog(activity);
            }
            roomlistDialog.setOnCreateRoomClickListener(new RoomlistDialog.OnCreateRoomClickListener() {
                @Override
                public void onCreateRoomClick() {
                    onCreateRoom();
                }

                @Override
                public void noCreativeModeWorlds() {
                    showPleaseCreateWorldDialog(activity);
                }
            });
            roomlistDialog.setOnRefreshClickListener(this::roomlistRequest);
            roomlistDialog.setOnCancelClickListener(this::shutdownMultiplayer);
            roomlistDialog.setOnRoomClickListener(this::joinRoomSelected);
            roomlistDialog.show();
            roomlistDialog.initData(new ArrayList<>(roomsByEntranceNumber), new ArrayList<>(roomsByPlayerNumber), new ArrayList<>(roomsByRating));
            dismissLoadingDialog();
        });
    }

    public void showPleaseCreateWorldDialog(@NonNull final Activity activity) {
        activity.runOnUiThread(() -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(activity);
            materialAlertDialogBuilder.setTitle(R.string.please_create_world);
            materialAlertDialogBuilder.setNeutralButton(android.R.string.ok, null);
            materialAlertDialogBuilder.show();
        });
        Multiplayer.instance.isInMultiplayerMode = false;
    }

    public void onCreateRoom() {
        this.activity.runOnUiThread(() -> {
            if (selectWorldDialog == null) {
                selectWorldDialog = new CreateRoomDialog(activity, WorldUtils.getmCreativeModeWorlds());
            }
            selectWorldDialog.setOnCancelClickListener(this::shutdownMultiplayer);
            selectWorldDialog.setOnCreateRoomClickListener(this::createRoom);
            selectWorldDialog.show();
        });
    }

    public void createRoom(String worldId, String roomName, String password, boolean isReadonly) {
        this.createWorldName = worldId;
        this.createRoomName = roomName;
        if (Multiplayer.instance.gameClient != null) {
            Multiplayer.createRoom(roomName, password, isReadonly);
        }
    }

    public void roomlistRequest() {
        showLoadingDialog();
        if (Multiplayer.instance.gameClient != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Multiplayer.instance.gameClient.roomList((byte) 1, 0);
                        Multiplayer.instance.gameClient.roomList((byte) 4, 0);
                        Multiplayer.instance.gameClient.roomList((byte) 3, 0);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }.start();
            showRoomlistDialog(new ArrayList(), new ArrayList(), new ArrayList());
        }
    }

    public void joinRoomSelected(ObjectCodec.RoomPack roomPack) {
        if (roomPack != null) {
            this.currentRoomPack = roomPack;
            if (Multiplayer.instance.gameClient != null) {
                Multiplayer.joinRoom(this.currentRoomPack.name, this.currentRoomPack.password);
            }
            showLoadingDialog();
        }
    }

    private void showLoadingDialog() {
        if ((this.loadingDialog == null || !this.loadingDialog.isShowing()) && this.activity != null) {
            this.activity.runOnUiThread(() -> {
                loadingDialog = ProgressDialog.show(activity, DescriptionFactory.emptyText, activity.getString(R.string.loading_please_wait), true);
                loadingDialog.setCancelable(false);
                loadingDialog.show();
            });
        }
    }

    public void dismissLoadingDialog() {
        if (this.loadingDialog != null && this.loadingDialog.isShowing()) {
            try {
                this.loadingDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void showErrorDialog(int stringId) {
        showErrorDialog(this.activity.getString(stringId));
    }

    public void showErrorDialog(final String message) {
        this.activity.runOnUiThread(() -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
            TextView textView = new TextView(activity);
            textView.setText(message);
            builder.setTitle(R.string.error).setView(textView);
            builder.setNeutralButton(android.R.string.ok, (DialogInterface.OnClickListener) null);
            builder.show();
        });
    }

    public void dismissOpenedDialogs() {
        if (this.selectWorldDialog != null && this.selectWorldDialog.isShowing()) {
            try {
                this.selectWorldDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (this.roomlistDialog != null && this.roomlistDialog.isShowing()) {
            try {
                this.roomlistDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        dismissLoadingDialog();
    }

    private void dismissOpenedDialogsAndWait() {
        dismissDialogAndWait(this.selectWorldDialog);
        dismissDialogAndWait(this.roomlistDialog);
        dismissDialogAndWait(this.loadingDialog);
        Multiplayer.dismissLoadingWorldDialogAndWait();
    }

    private void dismissDialogAndWait(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);
                dialog.setOnDismissListener(dialog2 -> latch.countDown());
                dialog.dismiss();
                try {
                    latch.await(1L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public void startGameActivity() {
        this.activity.runOnUiThread(() -> GameStarter.startGame((MyApplication) activity.getApplication(), activity, null, false, 0, WorldGenerator.Mode.CREATIVE));
    }
}
