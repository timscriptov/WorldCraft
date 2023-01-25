package com.mcal.worldcraft.srv.util;

public class OSDetector {
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static boolean isServer() {
        if (isUnix()) {
            try {
                Class.forName("android.os.Build");
            } catch (ClassNotFoundException e) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSolaris() {
        return System.getProperty("os.name").toLowerCase().contains("sunos");
    }

    public static boolean isUnix() {
        String lowerCase = System.getProperty("os.name").toLowerCase();
        return lowerCase.contains("nix") || lowerCase.contains("nux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
