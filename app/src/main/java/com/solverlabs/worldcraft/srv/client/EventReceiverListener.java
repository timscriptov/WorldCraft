package com.solverlabs.worldcraft.srv.client;

import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.srv.domain.Room;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;

import java.util.List;
import java.util.Map;

public interface EventReceiverListener {
    void onCheckVersionResponse(byte b, String str);

    void onCreateRoomResponse(byte b, String str);

    void onEnemyAction(int i, byte b);

    void onEnemyDisconnected(int i);

    void onEnemyInfo(Player player);

    void onEnemyMove(Camera camera);

    void onJoinRoomResponse(byte b, String str, boolean z, boolean z2);

    void onLoginResponse(byte b, int i, String str, String str2);

    void onMessage(String str);

    void onModifiedBlocks(Map<List<Short>, Room.BlockData> map);

    void onMoveResponse(byte b);

    void onPingResponse(byte b);

    void onPopupMessage(String str);

    void onReadOnlyRoomModification();

    void onRoomListResponse(byte b, List<ObjectCodec.RoomPack> list, List<ObjectCodec.RoomPack> list2, List<ObjectCodec.RoomPack> list3, List<ObjectCodec.RoomPack> list4, short s);

    void onSetBlockResonse(byte b, Map<List<Short>, Room.BlockData> map);

    void onUnknownEvent(byte b, byte b2);
}
