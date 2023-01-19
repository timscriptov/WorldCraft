package com.solverlabs.worldcraft.srv.domain;

import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.srv.util.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Room {
    public static final int DENIED_RADIUS = 5;
    public static final int MAX_USER_COUNT = 40;
    private static final int MAX_IDLE_IN_MEMORY_TIME = 900000;
    private static final int MAX_IDLE_TIME = 604800000;
    private static int nextId = 0;
    private Map<List<Short>, BlockData> blocks;
    private int dislikes;
    private int entrancesNumber;
    private long id;
    private long idleFrom;
    private boolean isInitInProgress;
    private boolean isInitNeeded;
    private boolean isInited;
    private boolean isLoaded;
    private boolean isReadOnly;
    private long lastActivity;
    private int likes;
    private String name;
    private Player owner;
    private String ownerDeviceId;
    private String password;
    private int startPositionX;
    private int startPositionY;
    private int startPositionZ;
    private byte[] unmutableBlocks;
    private String uploadToken;
    private Map<Integer, Player> users;

    private Room() {
        this.users = new ConcurrentHashMap();
        this.blocks = new ConcurrentHashMap();
        this.idleFrom = System.currentTimeMillis();
    }

    public Room(long j, String str, String str2) {
        this();
        this.id = j;
        this.name = str;
        this.password = str2;
    }

    public Room(Player player) {
        this();
        this.id = getNextId();
        resetGame();
    }

    private static synchronized int getNextId() {
        int i;
        synchronized (Room.class) {
            i = nextId;
            nextId = i + 1;
        }
        return i;
    }

    public static int getUnmutableBlockIndex(int i, int i2, int i3, short s, short s2, short s3, short s4, short s5) {
        return ((((s4 * 16) + s) - (i - 5)) * 5 * 5 * 4) + ((s2 - (i2 - 5)) * 5 * 2) + (((s5 * 16) + s3) - (i3 - 5));
    }

    private boolean inDeniedArea(ObjectCodec.BlockInfo blockInfo) {
        int i = blockInfo.x + (blockInfo.chunkX * 16);
        int i2 = blockInfo.z + (blockInfo.chunkZ * 16);
        return i >= this.startPositionX + (-5) && i < this.startPositionX + 5 && blockInfo.y >= this.startPositionY + (-5) && blockInfo.y < this.startPositionY + 5 && i2 >= this.startPositionZ + (-5) && i2 < this.startPositionZ + 5;
    }

    public synchronized boolean acquireInitLock() {
        boolean z = true;
        synchronized (this) {
            if (this.isInited || this.isInitInProgress) {
                z = false;
            } else {
                this.isInitInProgress = true;
            }
        }
        return z;
    }

    public void addPlayer(Player player) {
        if (player == null || this.users == null) {
            return;
        }
        this.users.put(Integer.valueOf(player.getId()), player);
    }

    public void dislike() {
        this.dislikes++;
    }

    public boolean equals(Object obj) {
        return obj != null && (obj instanceof Room) && this.id == ((Room) obj).id;
    }

    public Map<List<Short>, BlockData> getBlocks() {
        return this.blocks;
    }

    public Map<List<Short>, BlockData> getBlocksCopy() {
        HashMap hashMap;
        synchronized (this.blocks) {
            hashMap = new HashMap(this.blocks);
        }
        return hashMap;
    }

    public Player getCreator() {
        return this.owner;
    }

    public void setCreator(Player player) {
        this.owner = player;
    }

    public int getDislikes() {
        return this.dislikes;
    }

    public void setDislikes(int i) {
        this.dislikes = i;
    }

    public int getEntrancesNumber() {
        return this.entrancesNumber;
    }

    public void setEntrancesNumber(int i) {
        this.entrancesNumber = i;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long j) {
        this.id = j;
    }

    public long getLastActivity() {
        return this.lastActivity;
    }

    public void setLastActivity(long j) {
        this.lastActivity = j;
    }

    public int getLikes() {
        return this.likes;
    }

    public void setLikes(int i) {
        this.likes = i;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getOwnerDeviceId() {
        return this.ownerDeviceId;
    }

    public void setOwnerDeviceId(String str) {
        this.ownerDeviceId = str;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String str) {
        this.password = str;
    }

    public Map<Integer, Player> getPlayers() {
        return this.users;
    }

    public void setPlayers(Map<Integer, Player> map) {
        this.users = map;
    }

    public int getRating() {
        return this.likes;
    }

    public byte getUnmutableBlockType(short s, short s2, short s3, short s4, short s5) {
        return this.unmutableBlocks[getUnmutableBlockIndex(this.startPositionX, this.startPositionY, this.startPositionZ, s, s2, s3, s4, s5)];
    }

    public String getUploadToken() {
        return this.uploadToken;
    }

    public void setUploadToken(String str) {
        this.uploadToken = str;
    }

    public boolean hasPassword() {
        return this.password != null && !DescriptionFactory.emptyText.equals(this.password);
    }

    public int hashCode() {
        return (int) (17 + this.id);
    }

    public void init(Vector3f vector3f, byte[] bArr) {
        this.startPositionX = (int) vector3f.x;
        this.startPositionY = (int) vector3f.y;
        this.startPositionZ = (int) vector3f.z;
        this.unmutableBlocks = bArr;
        this.isInited = true;
    }

    public boolean isCorrectPassword(String str) {
        return ((this.password == null || DescriptionFactory.emptyText.equals(this.password)) && (str == null || DescriptionFactory.emptyText.equals(str))) || (this.password != null && this.password.equals(str));
    }

    public boolean isInited() {
        return this.isInited;
    }

    public void setInited(boolean z) {
        this.isInited = z;
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    public void setLoaded(boolean z) {
        this.isLoaded = z;
    }

    public boolean isOwner(Player player) {
        return (this.ownerDeviceId == null || player == null || !this.ownerDeviceId.equals(player.getDeviceId())) ? false : true;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public void setReadOnly(boolean z) {
        this.isReadOnly = z;
    }

    public void like() {
        this.likes++;
    }

    public synchronized void releaseInitLock() {
        this.isInitInProgress = false;
    }

    public void remove(Map<List<Short>, BlockData> map) {
        for (List<Short> list : map.keySet()) {
            synchronized (this.blocks) {
                this.blocks.remove(list);
            }
        }
    }

    public void removePlayer(int i) {
        if (this.users != null) {
            this.users.remove(Integer.valueOf(i));
            if (this.users.size() != 0) {
                return;
            }
            this.idleFrom = System.currentTimeMillis();
        }
    }

    public void removePlayer(Player player) {
        if (player != null) {
            removePlayer(player.getId());
        }
    }

    public boolean requiresDelete() {
        return this.users.size() == 0 && System.currentTimeMillis() - this.idleFrom > 604800000;
    }

    public boolean requiresRemoveFromMemory() {
        return this.users.size() == 0 && System.currentTimeMillis() - this.idleFrom > 900000;
    }

    public void resetGame() {
    }

    public void setBlock(List<Short> list, byte b, byte b2) {
        synchronized (this.blocks) {
            this.blocks.put(list, new BlockData(b, b2));
        }
    }

    public boolean setBlock(ObjectCodec.BlockInfo blockInfo) {
        if (inDeniedArea(blockInfo)) {
            return false;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(Short.valueOf(blockInfo.x));
        arrayList.add(Short.valueOf(blockInfo.y));
        arrayList.add(Short.valueOf(blockInfo.z));
        arrayList.add(Short.valueOf(blockInfo.chunkX));
        arrayList.add(Short.valueOf(blockInfo.chunkZ));
        setBlock(arrayList, blockInfo.blockType, blockInfo.blockData);
        return true;
    }

    public void startGame() {
    }

    public String toString() {
        return new StringBuffer().append("Room [").append(this.id).append(":").append(this.name).append("]").toString();
    }


    public static class BlockData {
        public byte blockData;
        public byte blockType;

        public BlockData(byte b, byte b2) {
            this.blockType = b;
            this.blockData = b2;
        }
    }


    public static class RoomComparatorByEneranceCount implements Comparator<Room> {
        @Override
        public int compare(Room room, Room room2) {
            if (room.entrancesNumber > room2.entrancesNumber) {
                return -1;
            }
            if (room.entrancesNumber >= room2.entrancesNumber) {
                return room.name.compareToIgnoreCase(room2.name);
            }
            return 1;
        }
    }


    public static class RoomComparatorByRaiting implements Comparator<Room> {
        @Override
        public int compare(Room room, Room room2) {
            if (room.getRating() > room2.getRating()) {
                return -1;
            }
            if (room.getRating() >= room2.getRating()) {
                return room.name.compareToIgnoreCase(room2.name);
            }
            return 1;
        }
    }


    public static class RoomComparatorByUsers implements Comparator<Room> {
        @Override
        public int compare(Room room, Room room2) {
            if (room == null && room2 == null) {
                return 0;
            }
            if (room == null) {
                return 1;
            }
            if (room2 != null && room.users.size() <= room2.users.size()) {
                if (room.users.size() < room2.users.size()) {
                    return 1;
                }
                return room.name.compareToIgnoreCase(room2.name);
            }
            return -1;
        }
    }
}
