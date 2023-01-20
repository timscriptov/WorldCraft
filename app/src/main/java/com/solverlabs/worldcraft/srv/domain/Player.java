package com.solverlabs.worldcraft.srv.domain;

public interface Player {
    public static final short BEE = 7;
    public static final short CAROLINE = 18;
    public static final short COP = 4;
    public static final short DIANA = 11;
    public static final short EAGLE_EYE = 5;
    public static final short EMILY = 10;
    public static final short FLASH = 6;
    public static final short JENNIFER = 15;
    public static final short JESSICA = 12;
    public static final short LILY = 16;
    public static final short MARY = 17;
    public static final short MILITARY_MAN = 2;
    public static final short PIRATE = 3;
    public static final short RACHEL = 14;
    public static final short RED_MAN = 0;
    public static final short ROBO = 8;
    public static final short SENSEI_MAN = 1;
    public static final short TUX = 9;
    public static final short VANESSA = 19;
    public static final short VICTORIA = 13;

    String getAndroidApiLevel();

    Camera getCamera();

    String getClientVersion();

    long getDevId();

    String getDeviceId();

    String getDeviceName();

    int getId();

    String getIp();

    long getLastRequestTime();

    String getOsVersion();

    String getPlayerName();

    Room getRoom();

    short getSkin();

    boolean inRoom();

    boolean isGraphicsInited();

    boolean loggedIn();

    boolean requiresRemove();

    void setAndroidApiLevel(String str);

    void setCamera(Camera camera);

    void setClientVersion(String str);

    void setDeviceId(String str);

    void setDeviceName(String str);

    void setGraphicsInited(boolean z);

    void setId(int i);

    void setLastRequestTime(long j);

    void setLoggedIn(boolean z);

    void setOsVersion(String str);

    void setPlayerName(String str);

    void setRoom(Room room);

    void setSkin(short s);

    void updateCamera(Camera camera);
}
