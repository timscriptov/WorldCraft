package com.mcal.worldcraft.srv.client;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.srv.common.WorldCraftGameEvent;
import com.mcal.worldcraft.srv.domain.Camera;
import com.mcal.worldcraft.srv.util.ObjectCodec;
import com.mcal.worldcraft.srv.util.Vector3f;

public class EventFactory {
    @NonNull
    public static WorldCraftGameEvent action(int i, byte b) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 34);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodePlayerAction(i, b));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent blockType(int i, int i2, int i3, int i4, int i5, int i6, byte b, byte b2, byte b3, byte b4) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 27);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeBlockInfo(null, (short) i2, (short) i3, (short) i4, (short) i5, (short) i6, b, b2, b3, b4));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent chat(int i, String str) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 10);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(str.getBytes());
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent checkVersion(int i, String str, int i2) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 21);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeCheckVersion(str, i2));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent createRoom(int i, String str, String str2, boolean z) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 14);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeRoom(str, str2, z));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent dislike(int i) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 50);
        worldCraftGameEvent.setPlayerId(i);
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent graphicsInited(int i, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        return pmove((byte) 37, i, vector3f, vector3f2, vector3f3);
    }

    @NonNull
    public static WorldCraftGameEvent joinRoom(int i, String str, String str2) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 18);
        worldCraftGameEvent.putMessage(str);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeRoom(str, str2, false));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent like(int i) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 49);
        worldCraftGameEvent.setPlayerId(i);
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent login(String str, short s, String str2, String str3, String str4, String str5, String str6) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 1);
        worldCraftGameEvent.putData(ObjectCodec.encodeLoginRequest(str, s, str2, str3, str4, str5, str6));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent move(int i, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        return pmove((byte) 30, i, vector3f, vector3f2, vector3f3);
    }

    @NonNull
    public static WorldCraftGameEvent ping(int i) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 47);
        worldCraftGameEvent.setPlayerId(i);
        return worldCraftGameEvent;
    }

    @NonNull
    private static WorldCraftGameEvent pmove(byte b, int i, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent(b);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encode(new Camera(i, vector3f, vector3f2, vector3f3)));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent reportAbuse(int i, int i2, String str) {
        if (str != null && str.length() > 300) {
            str.substring(0, 300);
        }
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 52);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeReportAbuse(i2, str));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent roomList(int i, byte b, int i2) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 24);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeRoomlistRequest(b, i2));
        return worldCraftGameEvent;
    }

    @NonNull
    public static WorldCraftGameEvent roomSearch(int i, String str, int i2) {
        WorldCraftGameEvent worldCraftGameEvent = new WorldCraftGameEvent((byte) 51);
        worldCraftGameEvent.setPlayerId(i);
        worldCraftGameEvent.putData(ObjectCodec.encodeRoomSearchRequest(str, i2));
        return worldCraftGameEvent;
    }
}
