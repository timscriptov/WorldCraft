package com.solverlabs.worldcraft.util;

public class GameTime {
    private static long currentGameTime;
    private static long lastPlayedTime;
    private static long systemStartSessionTime;
    private static long timeOffset = 0;

    public static long getTime() {
        currentGameTime = (System.currentTimeMillis() - systemStartSessionTime) + lastPlayedTime + timeOffset;
        return currentGameTime;
    }

    public static void initTime(long lastPlayedTime2) {
        lastPlayedTime = lastPlayedTime2;
        systemStartSessionTime = System.currentTimeMillis();
    }

    public static void incTime(long time) {
        timeOffset += time;
    }
}
