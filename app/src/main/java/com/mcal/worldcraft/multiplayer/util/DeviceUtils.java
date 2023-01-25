package com.mcal.worldcraft.multiplayer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.mcal.worldcraft.factories.DescriptionFactory;

import java.util.UUID;

public class DeviceUtils {
    private static final String DEFAULT_DEVICE_ID = "wrlddevid";

    public static String getDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice = DescriptionFactory.emptyText + tm.getDeviceId();
            String tmSerial = DescriptionFactory.emptyText + tm.getSimSerialNumber();
            @SuppressLint("HardwareIds") String androidId = DescriptionFactory.emptyText + Settings.Secure.getString(context.getContentResolver(), "android_id");
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            return deviceUuid.toString();
        } catch (Throwable th) {
            return DEFAULT_DEVICE_ID;
        }
    }

    public static String getAppVersion(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return DescriptionFactory.emptyText;
    }

    public static int getAppBuildNumber(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                return 0;
            }
        }
        return 0;
    }
}
