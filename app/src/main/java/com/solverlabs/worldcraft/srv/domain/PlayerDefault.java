package com.solverlabs.worldcraft.srv.domain;

import androidx.annotation.NonNull;

public class PlayerDefault implements Player {
    public static final long MAX_IDLE_TIME = 120000;
    private static int nextId = 0;
    private String androidApiLevel;
    private Camera camera = new Camera();
    private String clientVersion;
    private long created;
    private String deviceId;
    private String deviceName;
    private int id;
    private boolean isGraphicsInited;
    private long lastContact;
    private long lastRequestTime;
    private boolean loggedIn;
    private String osVersion;
    private String playerName;
    private Room room;
    private short skin;

    @NonNull
    public static Player create(@NonNull Camera camera) {
        PlayerDefault playerDefault = new PlayerDefault();
        playerDefault.setId(camera.playerId);
        playerDefault.updateCamera(camera);
        return playerDefault;
    }

    public static synchronized int getNextId() {
        int i;
        synchronized (PlayerDefault.class) {
            i = nextId;
            nextId = i + 1;
        }
        return i;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PlayerDefault)) {
            return false;
        }
        PlayerDefault playerDefault = (PlayerDefault) obj;
        if (this.id == playerDefault.getId() && this.playerName == null && playerDefault.getPlayerName() == null) {
            return true;
        }
        if (this.playerName != null) {
            return this.playerName.equals(playerDefault.getPlayerName());
        }
        return false;
    }

    @Override
    public String getAndroidApiLevel() {
        return this.androidApiLevel;
    }

    @Override
    public void setAndroidApiLevel(String str) {
        this.androidApiLevel = str;
    }

    @Override
    public Camera getCamera() {
        return this.camera;
    }

    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public String getClientVersion() {
        return this.clientVersion;
    }

    @Override
    public void setClientVersion(String str) {
        this.clientVersion = str;
    }

    @Override
    public long getDevId() {
        return 0L;
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    @Override
    public String getDeviceName() {
        return this.deviceName;
    }

    @Override
    public void setDeviceName(String str) {
        this.deviceName = str;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int i) {
        this.id = i;
    }

    @Override
    public String getIp() {
        return this.playerName;
    }

    @Override
    public long getLastRequestTime() {
        return this.lastRequestTime;
    }

    @Override
    public void setLastRequestTime(long j) {
        this.lastRequestTime = j;
    }

    @Override
    public String getOsVersion() {
        return this.osVersion;
    }

    @Override
    public void setOsVersion(String str) {
        this.osVersion = str;
    }

    @Override
    public String getPlayerName() {
        return this.playerName;
    }

    @Override
    public void setPlayerName(String str) {
        this.playerName = str;
    }

    @Override
    public Room getRoom() {
        return this.room;
    }

    @Override
    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public short getSkin() {
        return this.skin;
    }

    @Override
    public void setSkin(short s) {
        this.skin = s;
    }

    public int hashCode() {
        return this.playerName.hashCode();
    }

    @Override
    public boolean inRoom() {
        return this.room != null;
    }

    @Override
    public boolean isGraphicsInited() {
        return this.isGraphicsInited;
    }

    @Override
    public void setGraphicsInited(boolean z) {
        this.isGraphicsInited = z;
    }

    @Override
    public boolean loggedIn() {
        return this.loggedIn;
    }

    @Override
    public boolean requiresRemove() {
        return System.currentTimeMillis() - this.lastRequestTime > MAX_IDLE_TIME;
    }

    @Override
    public void setLoggedIn(boolean z) {
        this.loggedIn = z;
    }

    @NonNull
    public String toString() {
        return "Player @, id:" + this.id + " name: " + this.playerName + " skin: " + (int) this.skin + " camera: " + this.camera;
    }

    @Override
    public void updateCamera(@NonNull Camera camera) {
        this.camera.playerId = camera.playerId;
        this.camera.position.x = camera.position.x;
        this.camera.position.y = camera.position.y;
        this.camera.position.z = camera.position.z;
        this.camera.at.x = camera.at.x;
        this.camera.at.y = camera.at.y;
        this.camera.at.z = camera.at.z;
        this.camera.up.x = camera.up.x;
        this.camera.up.y = camera.up.y;
        this.camera.up.z = camera.up.z;
    }
}
