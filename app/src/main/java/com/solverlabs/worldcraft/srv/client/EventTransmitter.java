package com.solverlabs.worldcraft.srv.client;

import com.solverlabs.worldcraft.client.common.EventQueue;
import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;
import com.solverlabs.worldcraft.srv.util.Vector3f;


public class EventTransmitter {
    private String androidApiLevel;
    private Vector3f at;
    private int clientBuildNumber;
    private String clientVersion;
    private String deviceId;
    private String deviceName;
    private Vector3f eye;
    private String marketName;
    private String osVersion;
    private EventQueue outQueue;
    private int playerId;
    private String playerName;
    private String roomName;
    private String roomPassword;
    private short skinType;
    private Vector3f up;

    public EventTransmitter(EventTransmitter eventTransmitter, EventQueue eventQueue) {
        this.outQueue = eventQueue;
        if (eventTransmitter != null) {
            this.eye = eventTransmitter.eye;
            this.at = eventTransmitter.at;
            this.up = eventTransmitter.up;
        }
    }

    public void action(byte b) {
        enQueue(EventFactory.action(this.playerId, b));
    }

    public void blockType(int i, int i2, int i3, int i4, int i5, byte b, byte b2, byte b3, byte b4) {
        enQueue(EventFactory.blockType(this.playerId, i, i2, i3, i4, i5, b, b2, b3, b4));
    }

    public void chat(String str) {
        enQueue(EventFactory.chat(this.playerId, str));
    }

    public void checkVersion() {
        enQueue(EventFactory.checkVersion(this.playerId, this.marketName, this.clientBuildNumber));
    }

    public void createGame(String str, String str2, boolean z) {
        enQueue(EventFactory.createRoom(this.playerId, str, str2, z));
    }

    public void dislike() {
        enQueue(EventFactory.dislike(this.playerId));
    }

    public void enQueue(WorldCraftGameEvent worldCraftGameEvent) {
        this.outQueue.enQueue(worldCraftGameEvent);
    }

    public String getAndroidApiLevel() {
        return this.androidApiLevel;
    }

    public void setAndroidApiLevel(String str) {
        this.androidApiLevel = str;
    }

    public int getClientBuildNumber() {
        return this.clientBuildNumber;
    }

    public void setClientBuildNumber(int i) {
        this.clientBuildNumber = i;
    }

    public String getClientVersion() {
        return this.clientVersion;
    }

    public void setClientVersion(String str) {
        this.clientVersion = str;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String str) {
        this.deviceId = str;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String str) {
        this.deviceName = str;
    }

    public String getMarketName() {
        return this.marketName;
    }

    public void setMarketName(String str) {
        this.marketName = str;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public void setOsVersion(String str) {
        this.osVersion = str;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(int i) {
        this.playerId = i;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String str) {
        this.playerName = str;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public void setRoomName(String str) {
        this.roomName = str;
    }

    public String getRoomPassword() {
        return this.roomPassword;
    }

    public void setRoomPassword(String str) {
        this.roomPassword = str;
    }

    public short getSkinType() {
        return this.skinType;
    }

    public void setSkinType(short s) {
        this.skinType = s;
    }

    public void graphicsInited() {
        enQueue(EventFactory.graphicsInited(this.playerId, this.eye, this.at, this.up));
    }

    public void graphicsInited(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        this.eye = vector3f;
        this.at = vector3f2;
        this.up = vector3f3;
        enQueue(EventFactory.graphicsInited(this.playerId, vector3f, vector3f2, vector3f3));
    }

    public void joinRoom() {
        enQueue(EventFactory.joinRoom(this.playerId, this.roomName, this.roomPassword));
    }

    public void joinRoom(String str, String str2) {
        setRoom(str, str2);
        joinRoom();
    }

    public void like() {
        enQueue(EventFactory.like(this.playerId));
    }

    public void login() {
        enQueue(EventFactory.login(this.playerName, this.skinType, this.clientVersion, this.deviceId, this.deviceName, this.osVersion, this.androidApiLevel));
    }

    public void move(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        this.eye = vector3f;
        this.at = vector3f2;
        this.up = vector3f3;
        enQueue(EventFactory.move(this.playerId, vector3f, vector3f2, vector3f3));
    }

    public void ping() {
        enQueue(EventFactory.ping(this.playerId));
    }

    public void reportAbuse(int i, String str) {
        enQueue(EventFactory.reportAbuse(this.playerId, i, str));
    }

    public void roomList(byte b, int i) {
        enQueue(EventFactory.roomList(this.playerId, b, i));
    }

    public void roomSearch(String str, int i) {
        enQueue(EventFactory.roomSearch(this.playerId, str, i));
    }

    public void setOutQueue(EventQueue eventQueue) {
        this.outQueue = eventQueue;
    }

    public void setRoom(String str, String str2) {
        this.roomName = str;
        this.roomPassword = str2;
    }
}
