package com.solverlabs.worldcraft.srv.client;

import com.solverlabs.worldcraft.client.common.EventQueue;
import com.solverlabs.worldcraft.mob.MobFactory;
import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;
import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.srv.domain.Room;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventReceiver {
    private Map<List<Short>, Room.BlockData> blocks;
    private EventReceiverListener listener;
    private NetworkChecker networkChecker;
    private int receivedBlockPackets;
    private int receivedRoomByPlayerNumberPackets;
    private int receivedRoomByRatingPackets;
    private int receivedRoomReadOnly;
    private int receivedRoomSearchPackets;
    private List<ObjectCodec.RoomPack> roomsByPlayerNumber;
    private List<ObjectCodec.RoomPack> roomsByRating;
    private List<ObjectCodec.RoomPack> roomsReadOnly;
    private List<ObjectCodec.RoomPack> roomsSearch;

    public EventReceiver(EventQueue eventQueue, EventReceiverListener eventReceiverListener, NetworkChecker networkChecker) {
        if (eventQueue == null) {
            throw new IllegalArgumentException("InQueue instance is null");
        }
        if (eventReceiverListener == null) {
            throw new IllegalArgumentException("Listener instance is null");
        }
        this.listener = eventReceiverListener;
        this.networkChecker = networkChecker;
        this.roomsByPlayerNumber = new ArrayList();
        this.roomsReadOnly = new ArrayList();
        this.roomsByRating = new ArrayList();
        this.roomsSearch = new ArrayList();
        this.receivedBlockPackets = 0;
        this.blocks = new HashMap();
    }

    private void blockType(WorldCraftGameEvent worldCraftGameEvent) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(worldCraftGameEvent);
        setBlockType(arrayList);
    }

    private void checkVersion(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onCheckVersionResponse(worldCraftGameEvent.getError(), ObjectCodec.decodeString(worldCraftGameEvent.getInputBuffer()));
    }

    private void createRoomResponse(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onCreateRoomResponse(worldCraftGameEvent.getError(), ObjectCodec.decodeString(worldCraftGameEvent.getInputBuffer()));
    }

    private void enemyAction(WorldCraftGameEvent worldCraftGameEvent) {
        ObjectCodec.PlayerAction decodePlayerAction = ObjectCodec.decodePlayerAction(worldCraftGameEvent.getInputBuffer());
        this.listener.onEnemyAction(decodePlayerAction.playerId, decodePlayerAction.action);
    }

    private void enemyDisconnected(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onEnemyDisconnected(worldCraftGameEvent.getInputBuffer().getInt());
    }

    private void enemyInfo(WorldCraftGameEvent worldCraftGameEvent) {
        if (worldCraftGameEvent == null) {
            return;
        }
        for (Player player : ObjectCodec.decodePlayers(worldCraftGameEvent.getInputBuffer())) {
            this.listener.onEnemyInfo(player);
        }
    }

    private void enemyJoinedRoom(WorldCraftGameEvent worldCraftGameEvent) {
        if (worldCraftGameEvent == null) {
            return;
        }
        Player decodePlayer = ObjectCodec.decodePlayer(worldCraftGameEvent.getInputBuffer());
        if (decodePlayer.getId() == worldCraftGameEvent.getPlayerId()) {
            return;
        }
        this.listener.onEnemyInfo(decodePlayer);
    }

    private void enemyMove(WorldCraftGameEvent worldCraftGameEvent) {
        Camera decodeCamera = ObjectCodec.decodeCamera(worldCraftGameEvent.getInputBuffer());
        if (decodeCamera != null) {
            this.listener.onEnemyMove(decodeCamera);
        }
    }

    private void informModifiedBlocksReceived() {
        this.listener.onModifiedBlocks(this.blocks);
        this.blocks.clear();
    }

    private void joinRoomResponse(WorldCraftGameEvent worldCraftGameEvent) {
        boolean z;
        boolean z2 = false;
        String str = null;
        if (worldCraftGameEvent.getError() == 0) {
            ObjectCodec.RoomResponse decodeRoomResponse = ObjectCodec.decodeRoomResponse(worldCraftGameEvent.getInputBuffer());
            z = decodeRoomResponse.isOwner;
            z2 = decodeRoomResponse.isReadOnly;
        } else {
            str = ObjectCodec.decodeString(worldCraftGameEvent.getInputBuffer());
            z = false;
        }
        this.listener.onJoinRoomResponse(worldCraftGameEvent.getError(), str, z, z2);
    }

    private void loginResponse(WorldCraftGameEvent worldCraftGameEvent) {
        int i = -1;
        String str = null;
        if (worldCraftGameEvent.getError() == 0) {
            ObjectCodec.LoginResponse decodeLoginResponse = ObjectCodec.decodeLoginResponse(worldCraftGameEvent.getInputBuffer());
            i = decodeLoginResponse.playerId;
            str = decodeLoginResponse.playerName;
        }
        this.listener.onLoginResponse(worldCraftGameEvent.getError(), i, str, ObjectCodec.decodeString(worldCraftGameEvent.getInputBuffer()));
    }

    private void message(WorldCraftGameEvent worldCraftGameEvent) {
        String decodeString = ObjectCodec.decodeString(worldCraftGameEvent.getInputBuffer());
        if (decodeString != null) {
            this.listener.onMessage(decodeString);
        }
    }

    private void modifiedBlocks(WorldCraftGameEvent worldCraftGameEvent) {
        if (worldCraftGameEvent == null) {
            informModifiedBlocksReceived();
            return;
        }
        ObjectCodec.ModifiedBlockPack modifiedBlockPack = null;
        try {
            modifiedBlockPack = ObjectCodec.decodeBlocks(worldCraftGameEvent.getInputBuffer());
            if (modifiedBlockPack != null) {
                this.blocks.putAll(modifiedBlockPack.blocks);
            }
            this.receivedBlockPackets++;
        } catch (OutOfMemoryError e) {
            this.receivedBlockPackets++;
            this.blocks.clear();
            System.runFinalization();
            System.gc();
        }
        if (modifiedBlockPack == null || this.receivedBlockPackets != modifiedBlockPack.packetCount) {
            return;
        }
        if (this.blocks == null) {
            this.blocks = new HashMap();
        }
        informModifiedBlocksReceived();
    }

    private void moveResponse(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onMoveResponse(worldCraftGameEvent.getError());
    }

    private void pingResponse(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onPingResponse(worldCraftGameEvent.getError());
    }

    private void playerGraphicsInited(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onModifiedBlocks(new HashMap());
    }

    private void popupMessage(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onPopupMessage(ObjectCodec.decodeString(worldCraftGameEvent.getInputBuffer()));
    }

    private void reportAbuseResponse(WorldCraftGameEvent worldCraftGameEvent) {
    }

    private void roomListResponse(WorldCraftGameEvent worldCraftGameEvent) {
        if (worldCraftGameEvent == null) {
            return;
        }
        ObjectCodec.RoomsPacket decodeRoomsPacket = ObjectCodec.decodeRoomsPacket(worldCraftGameEvent.getInputBuffer());
        switch (decodeRoomsPacket.sortType) {
            case 0:
                this.roomsSearch.addAll(decodeRoomsPacket.rooms);
                this.receivedRoomSearchPackets++;
                if (this.receivedRoomSearchPackets == decodeRoomsPacket.packetCount || decodeRoomsPacket.packetCount == 0) {
                    this.receivedRoomSearchPackets = 0;
                    break;
                }
                break;
            case 1:
                this.roomsByPlayerNumber.addAll(decodeRoomsPacket.rooms);
                Collections.sort(this.roomsByPlayerNumber, new ObjectCodec.RoomPack.RoomComparatorByUsers());
                this.receivedRoomByPlayerNumberPackets++;
                if (this.receivedRoomByPlayerNumberPackets == decodeRoomsPacket.packetCount || decodeRoomsPacket.packetCount == 0) {
                    this.receivedRoomByPlayerNumberPackets = 0;
                    break;
                }
                break;
            case 3:
                this.roomsByRating.addAll(decodeRoomsPacket.rooms);
                Collections.sort(this.roomsByRating, new ObjectCodec.RoomPack.RoomComparatorByRaiting());
                this.receivedRoomByRatingPackets++;
                if (this.receivedRoomByRatingPackets == decodeRoomsPacket.packetCount || decodeRoomsPacket.packetCount == 0) {
                    this.receivedRoomByRatingPackets = 0;
                    break;
                }
                break;
            case 4:
                this.roomsReadOnly.addAll(decodeRoomsPacket.rooms);
                Collections.sort(this.roomsReadOnly, new ObjectCodec.RoomPack.RoomComparatorByEneranceCount());
                this.receivedRoomReadOnly++;
                if (this.receivedRoomReadOnly == decodeRoomsPacket.packetCount || decodeRoomsPacket.packetCount == 0) {
                    this.receivedRoomReadOnly = 0;
                    break;
                }
                break;
        }
        if (this.receivedRoomSearchPackets != 0 || this.receivedRoomReadOnly != 0 || this.receivedRoomByPlayerNumberPackets != 0 || this.receivedRoomByRatingPackets != 0) {
            return;
        }
        this.listener.onRoomListResponse(worldCraftGameEvent.getError(), this.roomsByPlayerNumber, this.roomsReadOnly, this.roomsByRating, this.roomsSearch, decodeRoomsPacket.initRoomlistSize);
        this.roomsReadOnly = new ArrayList();
        this.roomsByPlayerNumber = new ArrayList();
        this.roomsByRating = new ArrayList();
        this.roomsSearch = new ArrayList();
    }

    private void setBlockType(List<WorldCraftGameEvent> list) {
        HashMap hashMap = new HashMap();
        for (WorldCraftGameEvent worldCraftGameEvent : list) {
            ObjectCodec.BlockInfo decodeBlockInfo = ObjectCodec.decodeBlockInfo(worldCraftGameEvent.getInputBuffer());
            ArrayList arrayList = new ArrayList();
            arrayList.add(Short.valueOf(decodeBlockInfo.x));
            arrayList.add(Short.valueOf(decodeBlockInfo.y));
            arrayList.add(Short.valueOf(decodeBlockInfo.z));
            arrayList.add(Short.valueOf(decodeBlockInfo.chunkX));
            arrayList.add(Short.valueOf(decodeBlockInfo.chunkZ));
            hashMap.put(arrayList, new Room.BlockData(decodeBlockInfo.blockType, decodeBlockInfo.blockData));
        }
        this.listener.onSetBlockResonse((byte) 0, hashMap);
    }

    private void setBlockTypeResponse(WorldCraftGameEvent worldCraftGameEvent) {
        if (worldCraftGameEvent != null && worldCraftGameEvent.getError() == -2) {
            this.listener.onReadOnlyRoomModification();
        }
    }

    private void unknownEvent(WorldCraftGameEvent worldCraftGameEvent) {
        this.listener.onUnknownEvent(worldCraftGameEvent.getError(), worldCraftGameEvent.getType());
    }

    public void processIncomingEvents(WorldCraftGameEvent worldCraftGameEvent) {
        if (worldCraftGameEvent != null) {
            this.networkChecker.updatePacketTime();
        }
        switch (worldCraftGameEvent.getType()) {
            case 2:
                loginResponse(worldCraftGameEvent);
                return;
            case 11:
                message(worldCraftGameEvent);
                return;
            case 15:
                createRoomResponse(worldCraftGameEvent);
                return;
            case 19:
                joinRoomResponse(worldCraftGameEvent);
                return;
            case 22:
                checkVersion(worldCraftGameEvent);
                return;
            case 25:
                roomListResponse(worldCraftGameEvent);
                return;
            case 28:
                setBlockTypeResponse(worldCraftGameEvent);
                return;
            case 29:
                blockType(worldCraftGameEvent);
                return;
            case 31:
                moveResponse(worldCraftGameEvent);
                return;
            case 32:
                enemyMove(worldCraftGameEvent);
                return;
            case 33:
                enemyDisconnected(worldCraftGameEvent);
                return;
            case 35:
                return;
            case 36:
                enemyAction(worldCraftGameEvent);
                return;
            case 38:
                playerGraphicsInited(worldCraftGameEvent);
                return;
            case Room.MAX_USER_COUNT /* 40 */:
                modifiedBlocks(worldCraftGameEvent);
                return;
            case MobFactory.DISTANCE_TO_DESPAWN_MOB /* 45 */:
                enemyInfo(worldCraftGameEvent);
                return;
            case 46:
                enemyJoinedRoom(worldCraftGameEvent);
                return;
            case 48:
                pingResponse(worldCraftGameEvent);
                return;
            case 53:
                reportAbuseResponse(worldCraftGameEvent);
                return;
            case 54:
                popupMessage(worldCraftGameEvent);
                return;
            default:
                unknownEvent(worldCraftGameEvent);
                return;
        }
    }

    public void setEventReceiverListener(EventReceiverListener eventReceiverListener) {
        this.listener = eventReceiverListener;
    }
}
