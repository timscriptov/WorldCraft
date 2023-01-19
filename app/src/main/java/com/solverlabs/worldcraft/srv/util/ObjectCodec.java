package com.solverlabs.worldcraft.srv.util;

import com.solverlabs.worldcraft.srv.common.Globals;
import com.solverlabs.worldcraft.srv.domain.Camera;
import com.solverlabs.worldcraft.srv.domain.Player;
import com.solverlabs.worldcraft.srv.domain.PlayerDefault;
import com.solverlabs.worldcraft.srv.domain.Room;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public class ObjectCodec {
    private static final String OWNER = "owner";

    private static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        byte[] bArr = new byte[byteBuffer.remaining()];
        byteBuffer.get(bArr);
        return bArr;
    }

    private static void decode(ByteBuffer byteBuffer, Camera camera) {
        camera.playerId = byteBuffer.getInt();
        camera.position.x = byteBuffer.getFloat();
        camera.position.y = byteBuffer.getFloat();
        camera.position.z = byteBuffer.getFloat();
        camera.at.x = byteBuffer.getFloat();
        camera.at.y = byteBuffer.getFloat();
        camera.at.z = byteBuffer.getFloat();
        camera.up.x = byteBuffer.getFloat();
        camera.up.y = byteBuffer.getFloat();
        camera.up.z = byteBuffer.getFloat();
    }

    private static void decode(ByteBuffer byteBuffer, Player player) {
        int i = byteBuffer.getInt();
        String readStr = BufferUtils.readStr(byteBuffer);
        short s = byteBuffer.getShort();
        Camera camera = new Camera();
        decode(byteBuffer, camera);
        player.setId(i);
        player.setPlayerName(readStr);
        player.setSkin(s);
        player.setCamera(camera);
    }

    public static BlockInfo decodeBlockInfo(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        BlockInfo blockInfo = new BlockInfo();
        blockInfo.x = byteBuffer.getShort();
        blockInfo.y = byteBuffer.getShort();
        blockInfo.z = byteBuffer.getShort();
        blockInfo.chunkX = byteBuffer.getShort();
        blockInfo.chunkZ = byteBuffer.getShort();
        blockInfo.blockType = byteBuffer.get();
        if (byteBuffer.remaining() == 1) {
            blockInfo.prevBlockType = byteBuffer.get();
            blockInfo.blockData = blockInfo.prevBlockType;
            return blockInfo;
        } else if (byteBuffer.remaining() != 3) {
            return blockInfo;
        } else {
            blockInfo.blockData = byteBuffer.get();
            blockInfo.prevBlockType = byteBuffer.get();
            blockInfo.prevBlockData = byteBuffer.get();
            return blockInfo;
        }
    }

    public static BlockInfo decodeBlockInfo(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeBlockInfo(prepareUnpackBuffer(bArr));
    }

    public static ModifiedBlockPack decodeBlocks(ByteBuffer byteBuffer) {
        int i = byteBuffer.getInt();
        int i2 = byteBuffer.getInt();
        HashMap hashMap = new HashMap();
        while (byteBuffer.hasRemaining()) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(Short.valueOf(byteBuffer.getShort()));
            arrayList.add(Short.valueOf(byteBuffer.getShort()));
            arrayList.add(Short.valueOf(byteBuffer.getShort()));
            arrayList.add(Short.valueOf(byteBuffer.getShort()));
            arrayList.add(Short.valueOf(byteBuffer.getShort()));
            hashMap.put(arrayList, new Room.BlockData(byteBuffer.get(), byteBuffer.get()));
        }
        return new ModifiedBlockPack(hashMap, i, i2);
    }

    public static ModifiedBlockPack decodeBlocks(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeBlocks(prepareUnpackBuffer(bArr));
    }

    public static Camera decodeCamera(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        Camera camera = new Camera();
        decode(byteBuffer, camera);
        return camera;
    }

    public static Camera decodeCamera(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeCamera(prepareUnpackBuffer(bArr));
    }

    public static CheckVersion decodeCheckVersionRequest(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        CheckVersion checkVersion = new CheckVersion();
        checkVersion.marketName = BufferUtils.readStr(byteBuffer);
        checkVersion.clientBuildNumber = byteBuffer.getInt();
        return checkVersion;
    }

    public static Integer decodeInt(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return Integer.valueOf(prepareUnpackBuffer(bArr).getInt());
    }

    public static Login decodeLoginRequest(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        Login login = new Login();
        login.playerName = BufferUtils.readStr(byteBuffer);
        if (login.playerName == null) {
            return null;
        }
        login.skin = byteBuffer.getShort();
        login.clientVersion = BufferUtils.readStr(byteBuffer);
        login.deviceId = BufferUtils.readStr(byteBuffer);
        login.deviceName = BufferUtils.readStr(byteBuffer);
        login.osVersion = BufferUtils.readStr(byteBuffer);
        login.clientBuildNumber = BufferUtils.readStr(byteBuffer);
        return login;
    }

    public static Login decodeLoginRequest(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        ByteBuffer prepareUnpackBuffer = prepareUnpackBuffer(bArr);
        Login login = new Login();
        login.playerName = BufferUtils.readStr(prepareUnpackBuffer);
        if (login.playerName == null) {
            return null;
        }
        login.skin = prepareUnpackBuffer.getShort();
        login.clientVersion = BufferUtils.readStr(prepareUnpackBuffer);
        login.deviceId = BufferUtils.readStr(prepareUnpackBuffer);
        login.deviceName = BufferUtils.readStr(prepareUnpackBuffer);
        login.osVersion = BufferUtils.readStr(prepareUnpackBuffer);
        login.clientBuildNumber = BufferUtils.readStr(prepareUnpackBuffer);
        return login;
    }

    public static LoginResponse decodeLoginResponse(ByteBuffer byteBuffer) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.playerId = byteBuffer.getInt();
        if (byteBuffer.hasRemaining()) {
            loginResponse.playerName = BufferUtils.readStr(byteBuffer);
        }
        return loginResponse;
    }

    public static Player decodePlayer(ByteBuffer byteBuffer) {
        PlayerDefault playerDefault = new PlayerDefault();
        decode(byteBuffer, playerDefault);
        return playerDefault;
    }

    public static Player decodePlayer(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodePlayer(prepareUnpackBuffer(bArr));
    }

    public static PlayerAction decodePlayerAction(ByteBuffer byteBuffer) {
        PlayerAction playerAction = new PlayerAction();
        playerAction.playerId = byteBuffer.getInt();
        playerAction.action = byteBuffer.get();
        return playerAction;
    }

    public static PlayerAction decodePlayerAction(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodePlayerAction(prepareUnpackBuffer(bArr));
    }

    public static Collection<Player> decodePlayers(ByteBuffer byteBuffer) {
        ArrayList arrayList = new ArrayList();
        while (byteBuffer.hasRemaining()) {
            PlayerDefault playerDefault = new PlayerDefault();
            decode(byteBuffer, playerDefault);
            arrayList.add(playerDefault);
        }
        return arrayList;
    }

    public static Collection<Player> decodePlayers(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodePlayers(prepareUnpackBuffer(bArr));
    }

    public static ReportAbuse decodeReportAbuse(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        ReportAbuse reportAbuse = new ReportAbuse();
        reportAbuse.playerId = byteBuffer.getInt();
        reportAbuse.abuseText = BufferUtils.readStr(byteBuffer);
        return reportAbuse;
    }

    public static ReportAbuse decodeReportAbuse(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeReportAbuse(prepareUnpackBuffer(bArr));
    }

    public static RoomPack decodeRoom(ByteBuffer byteBuffer) {
        boolean z = true;
        if (byteBuffer == null) {
            return null;
        }
        RoomPack roomPack = new RoomPack();
        roomPack.name = BufferUtils.readStr(byteBuffer);
        roomPack.password = BufferUtils.readStr(byteBuffer);
        if (byteBuffer.hasRemaining()) {
            if (byteBuffer.get() != 1) {
                z = false;
            }
            roomPack.isReadOnly = z;
        }
        return roomPack;
    }

    public static RoomPack decodeRoom(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeRoom(prepareUnpackBuffer(bArr));
    }

    public static RoomResponse decodeRoomResponse(ByteBuffer byteBuffer) {
        boolean z = true;
        if (byteBuffer == null) {
            return null;
        }
        RoomResponse roomResponse = new RoomResponse();
        roomResponse.isOwner = byteBuffer.get() == 1;
        if (byteBuffer.get() != 1) {
            z = false;
        }
        roomResponse.isReadOnly = z;
        return roomResponse;
    }

    public static RoomResponse decodeRoomResponse(byte[] bArr) {
        return bArr == null ? new RoomResponse(false, false) : decodeRoomResponse(prepareUnpackBuffer(bArr));
    }

    public static RoomSearchRequest decodeRoomSearchRequest(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        RoomSearchRequest roomSearchRequest = new RoomSearchRequest();
        roomSearchRequest.searchString = BufferUtils.readStr(byteBuffer);
        roomSearchRequest.fromIndex = byteBuffer.getInt();
        return roomSearchRequest;
    }

    public static RoomSearchRequest decodeRoomSearchRequest(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeRoomSearchRequest(prepareUnpackBuffer(bArr));
    }

    public static RoomlistRequest decodeRoomlistRequest(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        RoomlistRequest roomlistRequest = new RoomlistRequest();
        roomlistRequest.roomlistType = byteBuffer.get();
        roomlistRequest.fromIndex = byteBuffer.getInt();
        return roomlistRequest;
    }

    public static RoomlistRequest decodeRoomlistRequest(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeRoomlistRequest(prepareUnpackBuffer(bArr));
    }

    public static Collection<RoomPack> decodeRooms(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        ByteBuffer prepareUnpackBuffer = prepareUnpackBuffer(bArr);
        TreeSet treeSet = new TreeSet();
        while (prepareUnpackBuffer.hasRemaining()) {
            RoomPack roomPack = new RoomPack();
            roomPack.id = prepareUnpackBuffer.getInt();
            roomPack.name = BufferUtils.readStr(prepareUnpackBuffer);
            roomPack.hasPassword = prepareUnpackBuffer.get() == 1;
            treeSet.add(roomPack);
        }
        return treeSet;
    }

    public static RoomsPacket decodeRoomsPacket(ByteBuffer byteBuffer) {
        int i = byteBuffer.getInt();
        int i2 = byteBuffer.getInt();
        short s = byteBuffer.getShort();
        byte b = byteBuffer.get();
        ArrayList arrayList = new ArrayList();
        while (byteBuffer.hasRemaining()) {
            RoomPack roomPack = new RoomPack();
            roomPack.id = byteBuffer.getInt();
            roomPack.name = BufferUtils.readStr(byteBuffer);
            roomPack.hasPassword = byteBuffer.get() == 1;
            roomPack.userCount = byteBuffer.getShort();
            roomPack.maxUserCount = byteBuffer.getShort();
            roomPack.entrancesNumber = byteBuffer.getInt();
            roomPack.rating = byteBuffer.getInt();
            arrayList.add(roomPack);
        }
        return new RoomsPacket(arrayList, i, i2, b, s);
    }

    public static RoomsPacket decodeRoomsPacket(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeRoomsPacket(prepareUnpackBuffer(bArr));
    }

    public static String decodeString(ByteBuffer byteBuffer) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (byteBuffer.hasRemaining()) {
            byteArrayOutputStream.write(byteBuffer.get());
        }
        return new String(byteArrayOutputStream.toByteArray());
    }

    public static String decodeString(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        return new String(bArr);
    }

    public static Warning decodeWarning(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        Warning warning = new Warning();
        warning.warningCount = byteBuffer.getInt();
        warning.abuseText = BufferUtils.readStr(byteBuffer);
        return warning;
    }

    public static Warning decodeWarning(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        return decodeWarning(prepareUnpackBuffer(bArr));
    }

    public static void encode(Player player, ByteBuffer byteBuffer, BlockInfo blockInfo) {
        byteBuffer.putShort(blockInfo.x);
        byteBuffer.putShort(blockInfo.y);
        byteBuffer.putShort(blockInfo.z);
        byteBuffer.putShort(blockInfo.chunkX);
        byteBuffer.putShort(blockInfo.chunkZ);
        byteBuffer.put(blockInfo.blockType);
        if (ClientVersionUtil.clientSupportsBlockData(player)) {
            byteBuffer.put(blockInfo.blockData);
        }
    }

    private static void encode(ByteBuffer byteBuffer, Camera camera) {
        float f = 0.0f;
        byteBuffer.putInt(camera != null ? camera.playerId : 0);
        byteBuffer.putFloat(camera != null ? camera.position.x : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.position.y : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.position.z : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.at.x : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.at.y : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.at.z : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.up.x : 0.0f);
        byteBuffer.putFloat(camera != null ? camera.up.y : 0.0f);
        if (camera != null) {
            f = camera.up.z;
        }
        byteBuffer.putFloat(f);
    }

    public static void encode(ByteBuffer byteBuffer, Player player, String str) {
        if (player == null) {
            return;
        }
        byteBuffer.putInt(player.getId());
        BufferUtils.writeStr(byteBuffer, player.getPlayerName(), true);
        if (ClientVersionUtil.isVersionGreaterThan(str, ClientVersionUtil.CLIENT_VERSION_2_4)) {
            byteBuffer.putShort(player.getSkin());
        } else {
            byteBuffer.putShort((short) (player.getSkin() % 10));
        }
        encode(byteBuffer, player.getCamera());
    }

    public static void encode(ByteBuffer byteBuffer, String str) {
        if (str != null) {
            byteBuffer.put(str.getBytes());
        }
    }

    public static byte[] encode(int i) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putInt(i);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encode(Camera camera) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encode(preparePackBuffer, camera);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encode(Player player, BlockInfo blockInfo) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encode(player, preparePackBuffer, blockInfo);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encode(Player player, String str) {
        if (player == null) {
            return null;
        }
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encode(preparePackBuffer, player, str);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeBlockInfo(Player player, short s, short s2, short s3, short s4, short s5, byte b, byte b2) {
        return encodeBlockInfo(player, s, s2, s3, s4, s5, b, b2, (byte) 0, (byte) 0, false);
    }

    public static byte[] encodeBlockInfo(Player player, short s, short s2, short s3, short s4, short s5, byte b, byte b2, byte b3, byte b4) {
        return encodeBlockInfo(player, s, s2, s3, s4, s5, b, b2, b3, b4, true);
    }

    public static byte[] encodeBlockInfo(Player player, short s, short s2, short s3, short s4, short s5, byte b, byte b2, byte b3, byte b4, boolean z) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putShort(s);
        preparePackBuffer.putShort(s2);
        preparePackBuffer.putShort(s3);
        preparePackBuffer.putShort(s4);
        preparePackBuffer.putShort(s5);
        preparePackBuffer.put(b);
        if (ClientVersionUtil.clientSupportsBlockData(player)) {
            preparePackBuffer.put(b2);
            if (z) {
                preparePackBuffer.put(b3);
                preparePackBuffer.put(b4);
            }
        }
        return byteBufferToArray(preparePackBuffer);
    }

    public static void encodeBlocks(Player player, ByteBuffer byteBuffer, Map<List<Short>, Room.BlockData> map, int i, int i2) {
        if (byteBuffer == null) {
            return;
        }
        byteBuffer.putInt(i);
        byteBuffer.putInt(i2);
        for (Map.Entry<List<Short>, Room.BlockData> entry : map.entrySet()) {
            for (Short sh : entry.getKey()) {
                byteBuffer.putShort(sh.shortValue());
            }
            Room.BlockData value = entry.getValue();
            byteBuffer.put(value.blockType);
            if (ClientVersionUtil.clientSupportsBlockData(player)) {
                byteBuffer.put(value.blockData);
            }
        }
    }

    public static byte[] encodeBlocks(Player player, Map<List<Short>, Room.BlockData> map, int i, int i2) {
        if (map == null) {
            return null;
        }
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putInt(i);
        preparePackBuffer.putInt(i2);
        for (Map.Entry<List<Short>, Room.BlockData> entry : map.entrySet()) {
            for (Short sh : entry.getKey()) {
                preparePackBuffer.putShort(sh.shortValue());
            }
            Room.BlockData value = entry.getValue();
            preparePackBuffer.put(value.blockType);
            if (ClientVersionUtil.clientSupportsBlockData(player)) {
                preparePackBuffer.put(value.blockData);
            }
        }
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeCheckVersion(String str, int i) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        BufferUtils.writeStr(preparePackBuffer, str, true);
        preparePackBuffer.putInt(i);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeLoginRequest(String str, short s, String str2, String str3, String str4, String str5, String str6) {
        if (str == null) {
            return null;
        }
        ByteBuffer preparePackBuffer = preparePackBuffer();
        BufferUtils.writeStr(preparePackBuffer, str, true);
        preparePackBuffer.putShort(s);
        BufferUtils.writeStr(preparePackBuffer, str2, true);
        BufferUtils.writeStr(preparePackBuffer, str3, true);
        BufferUtils.writeStr(preparePackBuffer, str4, true);
        BufferUtils.writeStr(preparePackBuffer, str5, true);
        BufferUtils.writeStr(preparePackBuffer, str6, true);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeLoginResponse(Player player, int i, String str) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putInt(i);
        if (ClientVersionUtil.clientSupportsUsernameInLoginResponse(player)) {
            BufferUtils.writeStr(preparePackBuffer, str, true);
        }
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeMove(int i, byte[] bArr) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putInt(i);
        preparePackBuffer.put(bArr);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodePlayerAction(int i, byte b) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putInt(i);
        preparePackBuffer.put(b);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodePlayers(Collection<Player> collection, String str) {
        if (collection == null) {
            return null;
        }
        ByteBuffer preparePackBuffer = preparePackBuffer();
        for (Player player : collection) {
            encode(preparePackBuffer, player, str);
        }
        return byteBufferToArray(preparePackBuffer);
    }

    public static void encodePlayersExceptOne(ByteBuffer byteBuffer, Collection<Player> collection, int i, String str) {
        for (Player player : collection) {
            if (player.getId() != i && player.isGraphicsInited()) {
                encode(byteBuffer, player, str);
            }
        }
    }

    public static byte[] encodePlayersExceptOne(Collection<Player> collection, int i, String str) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encodePlayersExceptOne(preparePackBuffer, collection, i, str);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeReportAbuse(int i, String str) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.putInt(i);
        BufferUtils.writeStr(preparePackBuffer, str, true);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeRoom(String str, String str2, boolean z) {
        int i = 1;
        ByteBuffer preparePackBuffer = preparePackBuffer();
        BufferUtils.writeStr(preparePackBuffer, str, true);
        BufferUtils.writeStr(preparePackBuffer, str2, true);
        if (!z) {
            i = 0;
        }
        preparePackBuffer.put((byte) i);
        return byteBufferToArray(preparePackBuffer);
    }

    public static void encodeRoomResponse(ByteBuffer byteBuffer, Player player, boolean z, boolean z2) {
        int i = 1;
        if ((ClientVersionUtil.isIPhoneOs(player.getOsVersion()) || !ClientVersionUtil.isVersionGreaterThan(player.getClientVersion(), ClientVersionUtil.CLIENT_VERSION_2_8_3)) && (!ClientVersionUtil.isIPhoneOs(player.getOsVersion()) || !ClientVersionUtil.isVersionGreaterThan(player.getClientVersion(), "4.0"))) {
            if (!z) {
                return;
            }
            BufferUtils.writeStr(byteBuffer, OWNER, false);
            return;
        }
        byteBuffer.put((byte) (z ? 1 : 0));
        if (!z2 || z) {
            i = 0;
        }
        byteBuffer.put((byte) i);
    }

    public static byte[] encodeRoomResponse(Player player, boolean z, boolean z2) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encodeRoomResponse(preparePackBuffer, player, z, z2);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeRoomSearchRequest(String str, int i) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        BufferUtils.writeStr(preparePackBuffer, str, true);
        preparePackBuffer.putInt(i);
        return byteBufferToArray(preparePackBuffer);
    }

    public static byte[] encodeRoomlistRequest(byte b, int i) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        preparePackBuffer.put(b);
        preparePackBuffer.putInt(i);
        return byteBufferToArray(preparePackBuffer);
    }

    public static void encodeRooms(ByteBuffer byteBuffer, Collection<Room> collection, int i, int i2, byte b, short s, String str) {
        if (byteBuffer == null) {
            return;
        }
        byteBuffer.putInt(i);
        byteBuffer.putInt(i2);
        byteBuffer.putShort(s);
        byteBuffer.put(b);
        for (Room room : collection) {
            byteBuffer.putInt((int) room.getId());
            BufferUtils.writeStr(byteBuffer, room.getName(), true);
            byteBuffer.put((byte) (room.hasPassword() ? 1 : 0));
            byteBuffer.putShort((short) (room.getPlayers() == null ? 0 : room.getPlayers().size()));
            byteBuffer.putShort((short) 40);
            byteBuffer.putInt(room.getEntrancesNumber());
            byteBuffer.putInt(room.getRating());
        }
    }

    public static byte[] encodeRooms(Collection<Room> collection, int i, int i2, byte b, short s, String str) {
        if (collection == null) {
            return null;
        }
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encodeRooms(preparePackBuffer, collection, i, i2, b, s, str);
        return byteBufferToArray(preparePackBuffer);
    }

    public static void encodeWarning(ByteBuffer byteBuffer, int i, String str) {
        byteBuffer.putInt(i);
        BufferUtils.writeStr(byteBuffer, str, true);
    }

    public static byte[] encodeWarning(int i, String str) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        encodeWarning(preparePackBuffer, i, str);
        return byteBufferToArray(preparePackBuffer);
    }

    @Deprecated
    public static byte[] packRoomsOldClients(Map<Long, Room> map) {
        ByteBuffer preparePackBuffer = preparePackBuffer();
        for (Room room : map.values()) {
            preparePackBuffer.putInt((int) room.getId());
            BufferUtils.writeStr(preparePackBuffer, room.getName(), true);
            preparePackBuffer.put((byte) (room.hasPassword() ? 1 : 0));
        }
        return byteBufferToArray(preparePackBuffer);
    }

    private static ByteBuffer preparePackBuffer() {
        ByteBuffer allocate = ByteBuffer.allocate(Globals.MAX_EVENT_SIZE);
        allocate.clear();
        return allocate;
    }

    private static ByteBuffer prepareUnpackBuffer(byte[] bArr) {
        ByteBuffer allocate = ByteBuffer.allocate(bArr.length);
        allocate.clear();
        allocate.put(bArr);
        allocate.flip();
        return allocate;
    }


    public static class BlockInfo {
        public static final int BLOCKS_PER_CHUNK = 16;
        public static final int LAST_BLOCK_ID = 118;
        public static final int MAX_CHUNK_X_POS = 31;
        public static final int MAX_CHUNK_Z_POS = 31;
        private static final int EMPTY_BLOCK_ID = 0;
        private static final int MIN_CHUNK_X_POS = 0;
        private static final int MIN_CHUNK_Z_POS = 0;
        public byte blockData;
        public byte blockType;
        public short chunkX;
        public short chunkZ;
        public byte prevBlockData;
        public byte prevBlockType = -1;
        public short x;
        public short y;
        public short z;

        private boolean inRange(int i, int i2, int i3) {
            return i >= i2 && i <= i3;
        }

        public boolean isValid() {
            return inRange((short) (this.chunkX + (this.x / 16)), 0, 31) && inRange((short) (this.chunkZ + (this.z / 16)), 0, 31) && inRange(this.blockType, 0, LAST_BLOCK_ID);
        }
    }


    public static class CheckVersion {
        public int clientBuildNumber;
        public String marketName;

        public CheckVersion() {
        }

        public CheckVersion(String str, int i) {
            this.marketName = str;
            this.clientBuildNumber = i;
        }
    }


    public static class Login {
        public String clientBuildNumber;
        public String clientVersion;
        public String deviceId;
        public String deviceName;
        public String osVersion;
        public String playerName;
        public short skin;
    }


    public static class LoginResponse {
        public int playerId;
        public String playerName;
    }


    public static class ModifiedBlockPack {
        public Map<List<Short>, Room.BlockData> blocks;
        public int curPacket;
        public int packetCount;

        public ModifiedBlockPack(Map<List<Short>, Room.BlockData> map, int i, int i2) {
            this.blocks = map;
            this.curPacket = i;
            this.packetCount = i2;
        }
    }


    public static class PlayerAction {
        public byte action;
        public int playerId;
    }


    public static class ReportAbuse {
        public String abuseText;
        public int playerId;
    }


    public static class RoomPack implements Comparable<RoomPack> {
        public int entrancesNumber;
        public boolean hasPassword;
        public int id;
        public boolean isReadOnly;
        public short maxUserCount;
        public String name;
        public String password;
        public int rating;
        public short userCount;

        @Override
        public int compareTo(RoomPack roomPack) {
            if (roomPack == null) {
                return 1;
            }
            if (this != null) {
                return this.name.compareTo(roomPack.name);
            }
            return -1;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof RoomPack)) {
                return false;
            }
            RoomPack roomPack = (RoomPack) obj;
            return (this.name == null && roomPack.name == null) || (this.name != null && this.name.equals(roomPack.name));
        }

        public int hashCode() {
            return this.name.hashCode();
        }


        public static class RoomComparatorByEneranceCount implements Comparator<RoomPack> {
            @Override
            public int compare(RoomPack roomPack, RoomPack roomPack2) {
                if (roomPack.entrancesNumber > roomPack2.entrancesNumber) {
                    return -1;
                }
                if (roomPack.entrancesNumber >= roomPack2.entrancesNumber) {
                    return roomPack.name.compareToIgnoreCase(roomPack2.name);
                }
                return 1;
            }
        }


        public static class RoomComparatorByRaiting implements Comparator<RoomPack> {
            @Override
            public int compare(RoomPack roomPack, RoomPack roomPack2) {
                if (roomPack.rating > roomPack2.rating) {
                    return -1;
                }
                if (roomPack.rating >= roomPack2.rating) {
                    return roomPack.name.compareToIgnoreCase(roomPack2.name);
                }
                return 1;
            }
        }


        public static class RoomComparatorByUsers implements Comparator<RoomPack> {
            @Override
            public int compare(RoomPack roomPack, RoomPack roomPack2) {
                if (roomPack.userCount > roomPack2.userCount) {
                    return -1;
                }
                if (roomPack.userCount >= roomPack2.userCount) {
                    return roomPack.name.compareToIgnoreCase(roomPack2.name);
                }
                return 1;
            }
        }
    }


    public static class RoomResponse {
        public boolean isOwner;
        public boolean isReadOnly;

        public RoomResponse() {
        }

        public RoomResponse(boolean z, boolean z2) {
            this.isOwner = z;
            this.isReadOnly = z2;
        }
    }


    public static class RoomSearchRequest {
        public int fromIndex;
        public String searchString;
    }


    public static class RoomlistRequest {
        public static final byte ROOMLIST_SEARCH_RESULT = 0;
        public static final byte ROOMLIST_TYPE_ACTIVE_PLAYERS = 1;
        public static final byte ROOMLIST_TYPE_ENTRANCE_NUMBER = 2;
        public static final byte ROOMLIST_TYPE_RATING = 3;
        public static final byte ROOMLIST_TYPE_READONLY = 4;
        public int fromIndex;
        public byte roomlistType;
    }


    public static class RoomsPacket {
        public int curPacket;
        public short initRoomlistSize;
        public int packetCount;
        public Collection<RoomPack> rooms;
        public byte sortType;

        public RoomsPacket(Collection<RoomPack> collection, int i, int i2, byte b, short s) {
            this.rooms = collection;
            this.curPacket = i;
            this.packetCount = i2;
            this.sortType = b;
            this.initRoomlistSize = s;
        }

        public String toString() {
            return new StringBuffer("RoomsPacket(").append(" rooms: ").append(this.rooms.size()).append(" curpack: ").append(this.curPacket).append(" packcount: ").append(this.packetCount).append(" sorttype: ").append((int) this.sortType).append(" initsize: ").append((int) this.initRoomlistSize).append(" )").toString();
        }
    }


    public static class Warning {
        public String abuseText;
        public int warningCount;
    }
}
