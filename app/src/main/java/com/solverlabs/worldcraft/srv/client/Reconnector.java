package com.solverlabs.worldcraft.srv.client;

import com.solverlabs.worldcraft.srv.client.NIOEventReader;
import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.srv.domain.Room;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import java.util.List;
import java.util.Map;

public class Reconnector implements NIOEventReader.OnEventReaderStartedListener, EventReceiverListener {
    private static final int RECONNECT_TIMEOUT = 2000;
    private static final int RECONNECT_TRY_COUNT = 10;
    private final AndroidClient gameClient;
    private final EventReceiver receiver;
    private final EventTransmitter transmitter;
    private int reconnectCounter = 0;
    private boolean isUserInRoom = false;

    public Reconnector(AndroidClient androidClient, EventTransmitter eventTransmitter, EventReceiver eventReceiver) {
        this.gameClient = androidClient;
        this.transmitter = eventTransmitter;
        this.receiver = eventReceiver;
    }

    private void errorOccurs(String str, Exception exc) {
        this.gameClient.reconnectError(str, exc);
    }

    private void onReconnectFinished() {
        this.reconnectCounter = 0;
        this.gameClient.setOnEventReaderStartedListener(this.gameClient, this.gameClient);
        this.gameClient.onReconnectFinished();
    }

    private void reconnectSleep() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isReconnectAllowed() {
        return this.isUserInRoom && this.reconnectCounter <= 10;
    }

    @Override
    public void onCheckVersionResponse(byte b, String str) {
    }

    @Override
    public void onCreateRoomResponse(byte b, String str) {
    }

    @Override
    public void onEnemyAction(int i, byte b) {
    }

    @Override
    public void onEnemyDisconnected(int i) {
    }

    @Override
    public void onEnemyInfo(Player player) {
    }

    @Override
    public void onEnemyMove(Camera camera) {
    }

    @Override
    public void onEventReaderErrorOccurs(String str, Exception exc) {
        errorOccurs(str, exc);
    }

    @Override
    public void onEventReaderListenerStarted() {
        this.gameClient.intTransmitterQueue();
        this.receiver.setEventReceiverListener(this);
        this.transmitter.login();
    }

    @Override
    public void onJoinRoomResponse(byte b, String str, boolean z, boolean z2) {
        if (b != 0) {
            errorOccurs("Unable to reconnect. Join Room failed: " + ((int) b), null);
            return;
        }
        onReconnectFinished();
        this.transmitter.graphicsInited();
    }

    @Override
    public void onLoginResponse(byte b, int i, String str, String str2) {
        if (b != 0) {
            errorOccurs("Unable to reconnect. Login failed: " + ((int) b), null);
            return;
        }
        this.transmitter.setPlayerId(i);
        this.transmitter.joinRoom();
    }

    @Override 
    public void onMessage(String str) {
    }

    @Override 
    public void onModifiedBlocks(Map<List<Short>, Room.BlockData> map) {
    }

    @Override 
    public void onMoveResponse(byte b) {
    }

    @Override 
    public void onPingResponse(byte b) {
    }

    @Override 
    public void onPopupMessage(String str) {
    }

    @Override 
    public void onReadOnlyRoomModification() {
    }

    @Override 
    public void onRoomListResponse(byte b, List<ObjectCodec.RoomPack> list, List<ObjectCodec.RoomPack> list2, List<ObjectCodec.RoomPack> list3, List<ObjectCodec.RoomPack> list4, short s) {
    }

    @Override 
    public void onSetBlockResonse(byte b, Map<List<Short>, Room.BlockData> map) {
    }

    @Override 
    public void onUnknownEvent(byte b, byte b2) {
    }

    public void setUserInRoom() {
        this.isUserInRoom = true;
    }

    public void tryReconnect() {
        this.gameClient.shutdown();
        reconnectSleep();
        this.reconnectCounter++;
        this.gameClient.init(this, this);
        this.gameClient.start();
    }
}
