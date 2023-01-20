package com.solverlabs.worldcraft.srv.client;

import com.solverlabs.worldcraft.srv.client.base.GameClient;
import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.srv.domain.Room;
import com.solverlabs.worldcraft.srv.log.WcLog;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.srv.util.Vector3f;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AndroidClient extends GameClient implements EventReceiverListener {
    protected static final WcLog log = WcLog.getLogger("AndroidClient");
    private Reconnector reconnector;
    private EventTransmitter transmitter;

    public interface MultiplayerListener extends GameClient.ConnectionListener {
        void onChatMesssageReceived(String str);

        void onCheckVersionCritical(String str);

        void onCheckVersionOk();

        void onCheckVersionWarning(String str);

        void onCreateRoomFailed(byte b, String str);

        void onCreateRoomOk(String str);

        void onEnemyAction(Integer num, byte b);

        void onEnemyDisconnected(Integer num);

        void onEnemyInfo(Player player);

        void onEnemyMove(Camera camera);

        void onJoinRoomFailed(byte b, String str);

        void onJoinRoomOk(boolean z, boolean z2);

        void onLoginFail(byte b, String str);

        void onLoginOk(int i, String str);

        void onModifiedBlocksRecieved(Map<List<Short>, Room.BlockData> map);

        void onMoveResponse();

        void onPopupMessage(String str);

        void onReadOnlyRoomModification();

        void onReconnectFinished();

        void onRoomListLoaded(Collection<ObjectCodec.RoomPack> collection, Collection<ObjectCodec.RoomPack> collection2, Collection<ObjectCodec.RoomPack> collection3, Collection<ObjectCodec.RoomPack> collection4, short s);

        void onSetBlockType(int i, int i2, int i3, int i4, int i5, byte b, byte b2);
    }

    private boolean isMultiplayerListenerValid() {
        return this.gameListener != null && (this.gameListener instanceof MultiplayerListener);
    }

    private void notifyListenerConnectionError(String str, Throwable th) {
        shutdown();
        if (isMultiplayerListenerValid()) {
            this.gameListener.onConnectionFailed(str, th);
        }
    }

    private void userJoinedRoom() {
        this.reconnector.setUserInRoom();
    }

    public void action(byte b) {
        this.transmitter.action(b);
    }

    public void blockType(int i, int i2, int i3, int i4, int i5, byte b, byte b2, byte b3, byte b4) {
        this.transmitter.blockType(i, i2, i3, i4, i5, b, b2, b3, b4);
    }

    public void chat(String str) {
        this.transmitter.chat(str);
    }

    public void checkVersion() {
        this.transmitter.checkVersion();
    }

    public void createRoom(String str, String str2, boolean z) {
        this.transmitter.createGame(str, str2, z);
    }

    public void dislike() {
        this.transmitter.dislike();
    }

    public EventTransmitter getTransmitter() {
        return this.transmitter;
    }

    public void graphicsInited(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        this.transmitter.graphicsInited(vector3f, vector3f2, vector3f3);
    }

    public void init(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, int i) {
        super.init(str, this, this);
        this.transmitter = new EventTransmitter(this.transmitter, this.outQueue);
        this.transmitter.setPlayerName(str2);
        this.transmitter.setSkinType(Short.parseShort(str3));
        this.transmitter.setClientVersion(str4);
        this.transmitter.setClientBuildNumber(i);
        this.transmitter.setDeviceId(str5);
        this.transmitter.setDeviceName(str6);
        this.transmitter.setOsVersion(str7);
        this.transmitter.setAndroidApiLevel(str8);
        this.reconnector = new Reconnector(this, this.transmitter, this.receiver);
    }

    public void intTransmitterQueue() {
        this.transmitter.setOutQueue(this.outQueue);
    }

    public void joinRoom(String str, String str2) {
        this.transmitter.joinRoom(str, str2);
    }

    public void like() {
        this.transmitter.like();
    }

    public void login() {
        this.transmitter.login();
    }

    public void move(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        this.transmitter.move(vector3f, vector3f2, vector3f3);
    }

    @Override 
    public void onCheckVersionResponse(byte b, String str) {
        if (isMultiplayerListenerValid()) {
            if (b == 0) {
                ((MultiplayerListener) this.gameListener).onCheckVersionOk();
            } else if (b == -1) {
                ((MultiplayerListener) this.gameListener).onCheckVersionWarning(str);
            } else if (b == -2) {
                ((MultiplayerListener) this.gameListener).onCheckVersionCritical(str);
            }
        }
    }

    @Override 
    protected void onConnectionLost(String str, Throwable th) {
        if (this.reconnector.isReconnectAllowed() && hasUsefullGameEventsDueIdleTime()) {
            System.out.println("try reconnect");
            this.reconnector.tryReconnect();
            return;
        }
        System.out.println("notifyListenerConnectionError: ");
        if (th != null) {
            th.printStackTrace();
        } else {
            try {
                throw new RuntimeException("connection lost");
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        notifyListenerConnectionError(str, th);
    }

    @Override 
    public void onCreateRoomResponse(byte b, String str) {
        if (isMultiplayerListenerValid()) {
            if (b == 0) {
                ((MultiplayerListener) this.gameListener).onCreateRoomOk(str);
            } else {
                ((MultiplayerListener) this.gameListener).onCreateRoomFailed(b, str);
            }
        }
    }

    @Override 
    public void onEnemyAction(int i, byte b) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onEnemyAction(i, b);
        }
    }

    @Override 
    public void onEnemyDisconnected(int i) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onEnemyDisconnected(i);
        }
    }

    @Override 
    public void onEnemyInfo(Player player) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onEnemyInfo(player);
        }
    }

    @Override 
    public void onEnemyMove(Camera camera) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onEnemyMove(camera);
        }
    }

    @Override 
    public void onJoinRoomResponse(byte b, String str, boolean z, boolean z2) {
        if (isMultiplayerListenerValid()) {
            if (b != 0) {
                ((MultiplayerListener) this.gameListener).onJoinRoomFailed(b, str);
                return;
            }
            userJoinedRoom();
            ((MultiplayerListener) this.gameListener).onJoinRoomOk(z, z2);
        }
    }

    @Override 
    public void onLoginResponse(byte b, int i, String str, String str2) {
        if (b != 0) {
            if (isMultiplayerListenerValid()) {
                ((MultiplayerListener) this.gameListener).onLoginFail(b, str2);
                return;
            }
            return;
        }
        this.transmitter.setPlayerId(i);
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onLoginOk(i, str);
        }
    }

    @Override 
    public void onMessage(String str) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onChatMesssageReceived(str);
        }
    }

    @Override 
    public void onModifiedBlocks(Map<List<Short>, Room.BlockData> map) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onModifiedBlocksRecieved(map);
        }
    }

    @Override 
    public void onMoveResponse(byte b) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onMoveResponse();
        }
    }

    @Override 
    public void onPingResponse(byte b) {
    }

    @Override 
    public void onPopupMessage(String str) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onPopupMessage(str);
        }
    }

    @Override 
    public void onReadOnlyRoomModification() {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onReadOnlyRoomModification();
        }
    }

    public void onReconnectFinished() {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onReconnectFinished();
        }
    }

    @Override 
    public void onRoomListResponse(byte b, List<ObjectCodec.RoomPack> list, List<ObjectCodec.RoomPack> list2, List<ObjectCodec.RoomPack> list3, List<ObjectCodec.RoomPack> list4, short s) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onRoomListLoaded(list2, list, list3, list4, s);
        }
    }

    @Override 
    public void onSetBlockResonse(byte b, Map<List<Short>, Room.BlockData> map) {
        if (isMultiplayerListenerValid()) {
            ((MultiplayerListener) this.gameListener).onModifiedBlocksRecieved(map);
        }
    }

    @Override 
    public void onUnknownEvent(byte b, byte b2) {
    }

    @Override 
    protected void ping() {
        this.transmitter.ping();
    }

    public void reconnectError(String str, Throwable th) {
        notifyListenerConnectionError(str, th);
    }

    public void reportAbuse(int i, String str) {
        this.transmitter.reportAbuse(i, str);
    }

    public void roomList(byte b, int i) {
        this.transmitter.roomList(b, i);
    }

    public void roomSearch(String str, int i) {
        this.transmitter.roomSearch(str, i);
    }

    @Override 
    public void shutdown() {
        super.shutdown();
    }

    public void start() {
        new Thread(this).start();
    }
}
