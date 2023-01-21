package com.solverlabs.worldcraft.srv.domain;

@Deprecated
public class Action {
    public static final String ACTION_CHAT_MESSAGE_MODERATED = "chat-message-moderated";
    public static final String ACTION_DEVICE_ID_BLACKLIST_ADD = "deviceid-blacklist-add";
    public static final String ACTION_DEVICE_ID_BLACKLIST_REMOVE = "deviceid-blacklist-remove";
    public static final String ACTION_IP_BLACKLIST_ADD = "ip-blacklist-add";
    public static final String ACTION_IP_BLACKLIST_REMOVE = "ip-blacklist-remove";
    public static final String ACTION_ROOM_REMOVE = "room-remove";
    public static final String ACTION_ROOM_UPDATE = "room-update";
    public static final String ACTION_USER_REMOVE = "user-remove";
    public static final String ACTION_USER_UPDATE = "user-update";
    public static final int CHAT_MESSAGE_MODERATED = 9;
    public static final int DEVICE_ID_BLACKLIST_ADD = 7;
    @Deprecated
    public static final int DEVICE_ID_BLACKLIST_REMOVE = 8;
    public static final int IP_BLACKLIST_ADD = 5;
    public static final int IP_BLACKLIST_REMOVE = 6;
    public static final int ROOM_REMOVE = 2;
    public static final int ROOM_UPDATE = 1;
    public static final int USER_REMOVE = 4;
    public static final int USER_UPDATE = 3;
    private long createdAt;
    private long id;
    private long recordId;
    private int type;

    public Action(long j, String str, long j2) {
        this.id = j;
        setType(str);
        this.recordId = j2;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long j) {
        this.createdAt = j;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long j) {
        this.id = j;
    }

    public long getRecordId() {
        return this.recordId;
    }

    public void setRecordId(long j) {
        this.recordId = j;
    }

    public int getType() {
        return this.type;
    }

    public void setType(String str) {
        if (ACTION_ROOM_UPDATE.equals(str)) {
            this.type = 1;
        } else if (ACTION_ROOM_REMOVE.equals(str)) {
            this.type = 2;
        } else if (ACTION_USER_UPDATE.equals(str)) {
            this.type = 3;
        } else if (ACTION_USER_REMOVE.equals(str)) {
            this.type = 4;
        } else if (ACTION_IP_BLACKLIST_ADD.equals(str)) {
            this.type = 5;
        } else if (ACTION_IP_BLACKLIST_REMOVE.equals(str)) {
            this.type = 6;
        } else if (ACTION_DEVICE_ID_BLACKLIST_ADD.equals(str)) {
            this.type = 7;
        } else if (ACTION_DEVICE_ID_BLACKLIST_REMOVE.equals(str)) {
            this.type = 8;
        } else if (ACTION_CHAT_MESSAGE_MODERATED.equals(str)) {
            this.type = 9;
        }
    }
}
