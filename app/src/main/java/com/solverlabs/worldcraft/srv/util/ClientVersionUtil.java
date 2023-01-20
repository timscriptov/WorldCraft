package com.solverlabs.worldcraft.srv.util;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.srv.domain.Player;

public class ClientVersionUtil {
    public static final String CLIENT_VERSION_2_4 = "2.4";
    public static final String CLIENT_VERSION_2_6 = "2.6";
    public static final String CLIENT_VERSION_2_8_3 = "2.8.3";
    public static final String CLIENT_VERSION_2_8_5 = "2.8.5";
    public static final String CLIENT_VERSION_4_0 = "4.0";
    public static final String LATEST = "4.0";

    public static boolean clientSupportsBlockData(Player player) {
        return player == null || (isAndroidOs(player.getOsVersion()) && isVersionGreaterThan(player.getClientVersion(), CLIENT_VERSION_2_8_5)) || (isIPhoneOs(player.getOsVersion()) && isVersionGreaterThan(player.getClientVersion(), "4.0"));
    }

    public static boolean clientSupportsUsernameInLoginResponse(Player player) {
        return player == null || (isAndroidOs(player.getOsVersion()) && isVersionGreaterThan(player.getClientVersion(), CLIENT_VERSION_2_8_5)) || (isIPhoneOs(player.getOsVersion()) && isVersionGreaterThan(player.getClientVersion(), "4.0"));
    }

    public static boolean isAndroidOs(String str) {
        return !isIPhoneOs(str);
    }

    public static boolean isIPhoneOs(String str) {
        return str != null && str.toLowerCase().contains("iphone");
    }

    public static boolean isVersionGreaterThan(String str, String str2) {
        try {
            return Float.parseFloat(normalizeVersion(str)) > Float.parseFloat(normalizeVersion(str2));
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }

    @NonNull
    private static String normalizeVersion(@NonNull String str) {
        int indexOf = str.indexOf(46);
        if (indexOf == -1) {
            return str;
        }
        String replaceAll = str.replaceAll("\\.", DescriptionFactory.emptyText);
        return replaceAll.substring(0, indexOf) + "." + replaceAll.substring(indexOf);
    }
}
