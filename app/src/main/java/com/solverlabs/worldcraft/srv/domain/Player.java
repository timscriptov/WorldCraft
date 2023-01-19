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

    void setAndroidApiLevel(String str);

    Camera getCamera();

    void setCamera(Camera camera);

    String getClientVersion();

    void setClientVersion(String str);

    long getDevId();

    String getDeviceId();

    void setDeviceId(String str);

    String getDeviceName();

    void setDeviceName(String str);

    int getId();

    void setId(int i);

    String getIp();

    long getLastRequestTime();

    void setLastRequestTime(long j);

    String getOsVersion();

    void setOsVersion(String str);

    String getPlayerName();

    void setPlayerName(String str);

    Room getRoom();

    void setRoom(Room room);

    short getSkin();

    void setSkin(short s);

    boolean inRoom();

    boolean isGraphicsInited();

    void setGraphicsInited(boolean z);

    boolean loggedIn();

    boolean requiresRemove();

    void setLoggedIn(boolean z);

    void updateCamera(Camera camera);
}
