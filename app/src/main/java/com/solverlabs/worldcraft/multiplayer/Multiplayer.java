package com.solverlabs.worldcraft.multiplayer;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.GameActivity;
import com.solverlabs.worldcraft.BlockView;
import com.solverlabs.worldcraft.Enemy;
import com.solverlabs.worldcraft.srv.client.AndroidClient;
import com.solverlabs.worldcraft.srv.client.base.GameClient;
import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.ui.Interaction;
import com.solverlabs.worldcraft.util.Properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Multiplayer {
    instance;

    private static final int MAX_COUNT_OF_CHAT_MESSAGE_PER_PLAYER = 10;
    private static final LinkedList<String> popUpMessages = new LinkedList<>();
    private static Map<String, List<Message>> messageMap = new HashMap();
    public BlockView blockView;
    public int clientBuildNumber;
    public String clientVersion;
    public String deviceId;
    public Map<Integer, Enemy> enemies;
    public GameActivity gameActivity;
    public AndroidClient gameClient;
    public Interaction interaction;
    public boolean isClientGraphicInited;
    public boolean isInMultiplayerMode = false;
    public boolean isInited;
    public boolean isWorldReady;
    public boolean isWorldShowing;
    public MovementHandler movementHandler;
    public int playerId;
    public String playerName;
    public String roomName;
    public short skinType;
    private boolean isReadOnly;
    private boolean roomOwner;

    Multiplayer() {
    }

    private static String getDeviceName() {
        return Build.MODEL;
    }

    private static String getOsVersion() {
        return Build.VERSION.RELEASE;
    }

    @NonNull
    private static String getAndroidApiLevel() {
        return Integer.toString(Build.VERSION.SDK_INT);
    }

    @NonNull
    public static Collection<Enemy> getEnemiesCopy() {
        Collection<Enemy> result = new ArrayList<>();
        if (instance.enemies != null) {
            synchronized (instance.enemies) {
                if (instance.enemies != null) {
                    result.addAll(instance.enemies.values());
                }
            }
        }
        return result;
    }

    public static void checkVersion() {
        instance.gameClient.checkVersion();
    }

    public static void addMessage(@NonNull String msg) {
        int separatorIndex = msg.indexOf(62);
        if (separatorIndex != -1) {
            String playerName = msg.substring(0, separatorIndex);
            String message = msg.substring(separatorIndex + 2);
            List<Message> messageList = messageMap.get(playerName);
            if (messageList == null) {
                messageList = new ArrayList<>();
            }
            if (messageList.size() > 10) {
                messageList.remove(0);
            }
            messageList.add(new Message(message));
            messageMap.put(playerName, messageList);
        }
    }

    public static void removeMessages(Integer enemyId) {
        Enemy enemy;
        if (enemyId != null && (enemy = getEnemy(enemyId)) != null) {
            messageMap.remove(enemy.name);
        }
    }

    @NonNull
    public static Collection<Message> getPlayerMessageList(String playerName) {
        Collection<Message> result = messageMap.get(playerName);
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    @Nullable
    public static Enemy getEnemy(String name) {
        if (instance.enemies == null) {
            return null;
        }
        if (name != null && instance != null) {
            synchronized (instance.enemies) {
                if (instance.enemies == null) {
                    return null;
                }
                for (Enemy enemy : instance.enemies.values()) {
                    if (enemy != null && name.equals(enemy.name)) {
                        return enemy;
                    }
                }
            }
        }
        return null;
    }

    public static Enemy getEnemy(int id) {
        Enemy enemy = null;
        if (instance.enemies != null) {
            synchronized (instance.enemies) {
                if (instance.enemies != null) {
                    enemy = instance.enemies.get(id);
                }
            }
        }
        return enemy;
    }

    public static void reportAbuse(int playerId, String abuseText) {
        if (instance != null && instance.gameClient != null) {
            instance.gameClient.reportAbuse(playerId, abuseText);
        }
    }

    public static void addPopupMessage(String message) {
        popUpMessages.offer(message);
    }

    public static String pollPopupMessage() {
        return popUpMessages.poll();
    }

    public static void createRoom(String roomName2, String password, boolean isReadonly) {
        instance.roomOwner = true;
        instance.gameClient.createRoom(roomName2, password, isReadonly);
    }

    public static void joinRoom(String name, String password) {
        instance.roomOwner = false;
        instance.roomName = name;
        instance.gameClient.joinRoom(name, password);
    }

    public static boolean isRoomOwner() {
        return instance.roomOwner;
    }

    public static void setRoomOwner(boolean value) {
        instance.roomOwner = value;
    }

    public static boolean isReadOnly() {
        return instance.isReadOnly;
    }

    public static void setReadOnly(boolean isReadOnly) {
        instance.isReadOnly = isReadOnly;
    }

    public static void showReadOnlyRoomModificationDialog() {
        if (instance.gameActivity != null) {
            instance.gameActivity.showReadOnlyRoomModificationDialog();
        }
    }

    public static void setBlockType(int x, int y, int z, int chunkX, int chunkZ, byte blockType, byte blockData, byte prevBlockType, byte prevBlockData) {
        if (instance.gameClient != null) {
            instance.gameClient.blockType(x, y, z, chunkX, chunkZ, blockType, blockData, prevBlockType, prevBlockData);
        }
    }

    public static void setPlayerName(String playerName2) {
        instance.playerName = playerName2;
    }

    public static void dismissLoadingWorldDialog() {
        if (instance.blockView != null && instance.blockView.world != null) {
            instance.blockView.world.dismissLoadingDialog();
        }
    }

    public static void dismissLoadingWorldDialogAndWait() {
        if (instance.blockView != null && instance.blockView.world != null) {
            instance.blockView.world.dismissLoadingDialogAndWait();
        }
    }

    public void init(String playerName, short skinType, String clientVersion, int clientBuildNumber, String deviceId, MovementHandler.MovementHandlerListener movementListener, GameClient.ConnectionListener gameListener) {
        shutdownWithoutActivityFinish();
        this.playerName = playerName;
        this.skinType = skinType;
        this.clientVersion = clientVersion;
        this.clientBuildNumber = clientBuildNumber;
        this.deviceId = deviceId;
        this.isClientGraphicInited = false;
        this.isInited = false;
        this.isWorldReady = false;
        this.isWorldShowing = false;
        this.isInMultiplayerMode = true;
        this.blockView = null;
        this.enemies = new ConcurrentHashMap<>();
        messageMap = new HashMap<>();
        this.movementHandler = new MovementHandler();
        this.movementHandler.setListener(movementListener);
        this.gameClient = new AndroidClient();
        this.gameClient.setGameListener(gameListener);
    }

    public void startGameClient() {
        this.gameClient.init(Properties.MULTIPLAYER_SERVER_IP, this.playerName, Short.toString(this.skinType), this.clientVersion, this.deviceId, getDeviceName(), getOsVersion(), getAndroidApiLevel(), getClientBuildNumber());
        this.gameClient.start();
    }

    private int getClientBuildNumber() {
        return this.clientBuildNumber;
    }

    public void addEnemy(Enemy enemy) {
        if (enemy != null && enemy.id != this.playerId && this.enemies != null) {
            synchronized (this.enemies) {
                this.enemies.put(enemy.id, enemy);
            }
        }
    }

    public void moveEnemy(Camera camera) {
        if (camera != null) {
            try {
                Enemy enemy = getEnemy(camera.playerId);
                if (enemy != null) {
                    enemy.onMovement(camera);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void playerInfo(Player player) {
        if (player != null) {
            try {
                if (player.getCamera() != null) {
                    Enemy enemy = getEnemy(player.getId());
                    if (enemy == null) {
                        enemy = new Enemy();
                        enemy.id = player.getId();
                        addEnemy(enemy);
                    }
                    if (enemy.skin == 0) {
                        enemy.skin = player.getSkin();
                    }
                    enemy.id = player.getId();
                    enemy.name = player.getPlayerName();
                    enemy.onMovement(player.getCamera());
                    enemy.invalidate();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void actionEnemy(Integer pId, byte action) {
        if (pId != null) {
            Enemy enemy = getEnemy(pId);
            if (enemy == null) {
                enemy = new Enemy();
                enemy.id = pId;
                addEnemy(enemy);
            }
            enemy.onAction(action);
        }
    }

    public void removeEnemy(Integer enemyId) {
        if (enemyId != null && this.enemies != null) {
            synchronized (this.enemies) {
                if (this.enemies != null) {
                    this.enemies.remove(enemyId);
                }
            }
        }
    }

    public void clientGraphicInited() {
        if (!this.isClientGraphicInited) {
            this.isClientGraphicInited = true;
            if (this.movementHandler != null) {
                this.movementHandler.clientGraphicsInited();
            }
            this.isInited = true;
        }
    }

    public void likeWorld() {
        this.gameClient.like();
    }

    public void dislikeWorld() {
        this.gameClient.dislike();
    }

    public void shutdown() {
        shutdownWithoutActivityFinish();
        if (this.gameActivity != null) {
            this.gameActivity.finish();
            this.gameActivity = null;
        }
    }

    public void shutdownWithoutActivityFinish() {
        this.isInMultiplayerMode = false;
        this.isClientGraphicInited = false;
        this.isInited = false;
        this.isWorldReady = false;
        this.isWorldShowing = false;
        if (this.gameClient != null) {
            this.gameClient.shutdown();
            this.gameClient = null;
        }
        if (this.enemies != null) {
            synchronized (this.enemies) {
                if (this.enemies != null) {
                    this.enemies.clear();
                    this.enemies = null;
                }
            }
        }
        this.movementHandler = null;
        this.interaction = null;
        this.blockView = null;
    }

    public void invalidateEnemies() {
        for (Enemy enemy : getEnemiesCopy()) {
            if (enemy != null) {
                enemy.invalidate();
            }
        }
    }

    public void clearEnemies() {
        if (this.enemies != null) {
            synchronized (this.enemies) {
                if (this.enemies != null) {
                    this.enemies.clear();
                } else {
                    this.enemies = new ConcurrentHashMap<>();
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public static class Message {
        public long createdAt = System.currentTimeMillis();
        public String message;

        public Message(String message) {
            this.message = message;
        }
    }
}
