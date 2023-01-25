package com.mcal.worldcraft.srv.common;

import com.mcal.worldcraft.srv.domain.Player;

public interface GameEvent {
    public static final byte CLIENT_HAVE_TO_UPDATE_APP = -2;
    public static final byte CLIENT_RATHER_UPDATE_APP = -1;
    public static final int CLIENT_VERSION_2_3 = 1;
    public static final int CLIENT_VERSION_LESS_THAN_2_3 = 0;
    public static final byte C_CHAT_MSG = 10;
    public static final byte C_CHECK_VERSION = 21;
    public static final byte C_CREATE_ROOM_REQ = 14;
    public static final byte C_DISLIKE_WORLD_REQ = 50;
    public static final byte C_JOIN_ROOM_REQ = 18;
    public static final byte C_LIKE_WORLD_REQ = 49;
    public static final byte C_LOGIN_REQ = 1;
    public static final byte C_MOVE_REQ = 12;
    public static final byte C_PING_REQ = 47;
    public static final byte C_PLAYER_ACTION_REQ = 34;
    public static final byte C_PLAYER_GRAPHICS_INITED_REQ = 37;
    public static final byte C_PLAYER_MOVE_REQ = 30;
    public static final byte C_REPORT_ABUSE_REQ = 52;
    public static final byte C_ROOM_LIST_REQ = 24;
    public static final byte C_ROOM_SEARCH_REQ = 51;
    public static final byte C_SET_BLOCK_TYPE_REQ = 27;
    public static final byte ERROR_CREATE_ROOM_BAD_WORD = -6;
    public static final byte ERROR_CREATE_ROOM_FAILED = -5;
    public static final byte ERROR_CREATE_ROOM_IS_NULL = -4;
    public static final byte ERROR_CREATE_ROOM_NAME_EXISTS = -3;
    public static final byte ERROR_CREATE_ROOM_NAME_FORBIDDEN = -8;
    public static final byte ERROR_CREATE_ROOM_NAME_TOO_LONG = -7;
    public static final byte ERROR_CREATE_ROOM_NON_LOGGED_IN_USER = -2;
    public static final byte ERROR_CREATE_ROOM_NULL_USER = -1;
    public static final byte ERROR_CREATE_ROOM_PASSWORD_TOO_LONG = -9;
    public static final byte ERROR_JOIN_ROOM_DOESNT_EXIST = -1;
    public static final byte ERROR_JOIN_ROOM_FAILED = -2;
    public static final byte ERROR_JOIN_ROOM_PLAYER_LIMIT_EXCEEDED = -4;
    public static final byte ERROR_JOIN_ROOM_WRONG_PASSWORD = -3;
    public static final byte ERROR_LOGIN_ALREADY_LOGGED_IN = -1;
    public static final byte ERROR_LOGIN_BAD_WORD = -5;
    public static final byte ERROR_LOGIN_DEVICE_ID_BLACKLISTED = -4;
    public static final byte ERROR_LOGIN_IP_BLACKLISTED = -3;
    public static final byte ERROR_LOGIN_NAME_FORBIDDEN = -7;
    public static final byte ERROR_LOGIN_NULL_PLAYER = -2;
    public static final byte ERROR_LOGIN_TOO_LONG = -6;
    public static final byte ERROR_REPORT_ABUSE_TOO_OFTEN = -1;
    public static final byte ERROR_SET_BLOCK_TYPE_FAILED = -1;
    public static final byte ERROR_SET_BLOCK_TYPE_FAILED_READONLY_ROOM = -2;
    public static final byte ERROR_UPLOAD_FILE_NOT_IN_ROOM = -1;
    public static final byte SB_CHAT_MSG = 11;
    public static final byte SB_PLAYER_JOINED_ROOM = 46;
    public static final byte S_CHECK_VERSION = 22;
    public static final byte S_CREATE_ROOM_RESP = 15;
    public static final byte S_JOIN_ROOM_RESP = 19;
    public static final byte S_LOGIN_RESP = 2;
    public static final byte S_MODIFIED_BLOCKS = 40;
    public static final byte S_MOVE_RESP = 13;
    public static final byte S_PING_RESP = 48;
    public static final byte S_PLAYERS_INFO = 45;
    public static final byte S_PLAYER_ACTION = 36;
    public static final byte S_PLAYER_ACTION_RESP = 35;
    public static final byte S_PLAYER_DISCONNECTED = 33;
    public static final byte S_PLAYER_GRAPHICS_INITED_RESP = 38;
    public static final byte S_PLAYER_MOVE = 32;
    public static final byte S_PLAYER_MOVE_RESP = 31;
    public static final byte S_POPUP_MESSAGE = 54;
    public static final byte S_REPORT_ABUSE_RES = 53;
    public static final byte S_ROOM_LIST_RESP = 25;
    public static final byte S_SET_BLOCK_TYPE = 29;
    public static final byte S_SET_BLOCK_TYPE_RESP = 28;

    byte getError();

    void setError(byte b);

    Player getPlayer();

    void setPlayer(Player player);

    int getPlayerId();

    void setPlayerId(int i);

    byte getType();

    void setType(byte b);
}
