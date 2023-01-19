package com.solverlabs.worldcraft.util;


public class GameTime {
    private static long lastPlayedTime;
    private static long systemStartSessionTime;
    private static long timeOffset = 0;

    public static long getTime() {
        return (System.currentTimeMillis() - systemStartSessionTime) + lastPlayedTime + timeOffset;
    }

    public static void initTime(long lastPlayedTime2) {
        lastPlayedTime = lastPlayedTime2;
        systemStartSessionTime = System.currentTimeMillis();
    }

    public static void incTime(long time) {
        timeOffset += time;
    }
}
