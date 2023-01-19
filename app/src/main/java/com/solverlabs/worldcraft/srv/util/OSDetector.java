package com.solverlabs.worldcraft.srv.util;


public class OSDetector {
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    public static boolean isServer() {
        if (isUnix()) {
            try {
                return Class.forName("android.os.Build") == null;
            } catch (ClassNotFoundException e) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSolaris() {
        return System.getProperty("os.name").toLowerCase().indexOf("sunos") >= 0;
    }

    public static boolean isUnix() {
        String lowerCase = System.getProperty("os.name").toLowerCase();
        return lowerCase.indexOf("nix") >= 0 || lowerCase.indexOf("nux") >= 0;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }
}
