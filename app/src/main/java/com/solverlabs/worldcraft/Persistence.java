package com.solverlabs.worldcraft;

import android.content.Context;
import android.content.SharedPreferences;

import com.solverlabs.worldcraft.factories.DescriptionFactory;


public class Persistence {
    private static final boolean BANNER_REMOVED_DEFAULT_VALUE = false;
    private static final String BANNER_REMOVED_KEY = "IS_BANNER_REMOVED";
    private static final int ENTER_COUNT_DEFAULT_VALUE = 0;
    private static final String ENTER_COUNT_KEY = "enter_count";
    private static final boolean FIRST_TIME_STARTED_DEFAULT_VALUE = true;
    private static final String FIRST_TIME_STARTED_KEY = "isFirstTimeStarted";
    private static final float FOG_DISTANCE_DEFAULT_VALUE = 30.0f;
    private static final String FOG_DISTANCE_KEY = "fog_distance";
    private static final boolean INVERT_Y_DEFAULT_VALUE = false;
    private static final String INVERT_Y_KEY = "invert_y";
    private static final int LOAD_RADIUS_DEFAULT_VALUE = 2;
    private static final String LOAD_RADIUS_KEY = "load_radius";
    private static final String PREFERENCE_STORAGE_NAME = "WRLD_PREF";
    private static final boolean SOUND_ENABLED_DEFAULT_VALUE = true;
    private static final String SOUND_ENABLED_KEY = "sound_enabled";
    private static final String USER_NAME_DEFAULT_VALUE = "Johny";
    private static final String USER_NAME_KEY = "user_name";
    private static final int USER_SKIN_DEFAULT_VALUE = 0;
    private static final String USER_SKIN_KEY = "user_skin";
    private static SharedPreferences.Editor editor;
    private static Persistence instance;
    private static SharedPreferences settings;

    public static void initPersistence(Context context) {
        try {
            getInstance();
            settings = context.getSharedPreferences(PREFERENCE_STORAGE_NAME, 0);
            editor = settings.edit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Persistence getInstance() {
        if (instance == null) {
            instance = new Persistence();
        }
        return instance;
    }

    protected void commit() {
        editor.commit();
    }

    protected String getString(String key, String defVal) {
        if (settings != null) {
            return settings.getString(key, defVal);
        }
        return defVal;
    }

    protected void putString(String key, String value) {
        if (editor != null) {
            editor.putString(key, value);
            commit();
        }
    }

    protected int getInt(String key, int defVal) {
        if (settings != null) {
            return settings.getInt(key, defVal);
        }
        return defVal;
    }

    protected void putInt(String key, int value) {
        if (editor != null) {
            editor.putInt(key, value);
            commit();
        }
    }

    protected float getFloat(String key, float defVal) {
        if (settings != null) {
            return settings.getFloat(key, defVal);
        }
        return defVal;
    }

    protected void putFloat(String key, float value) {
        if (editor != null) {
            editor.putFloat(key, value);
            commit();
        }
    }

    protected boolean getBoolean(String key, boolean defVal) {
        if (settings != null) {
            return settings.getBoolean(key, defVal);
        }
        return defVal;
    }

    protected void putBoolean(String key, boolean value) {
        if (editor != null) {
            editor.putBoolean(key, value);
            commit();
        }
    }

    protected short getShort(String key, int defVal) {
        return (short) getInt(key, defVal);
    }

    protected void putShort(String key, short value) {
        putInt(key, value);
    }

    public String getPlayerName() {
        return getString(USER_NAME_KEY, USER_NAME_DEFAULT_VALUE);
    }

    public void setPlayerName(String playerName) {
        if (playerName == null || playerName.trim().equals(DescriptionFactory.emptyText)) {
            playerName = USER_NAME_DEFAULT_VALUE;
        }
        putString(USER_NAME_KEY, playerName);
    }

    public short getPlayerSkin() {
        return getShort(USER_SKIN_KEY, 0);
    }

    public void setPlayerSkin(short playerSkin) {
        putShort(USER_SKIN_KEY, playerSkin);
    }

    public boolean isSoundEnabled() {
        return getBoolean(SOUND_ENABLED_KEY, true);
    }

    public void setSoundEnabled(boolean isSoundEnabled) {
        putBoolean(SOUND_ENABLED_KEY, isSoundEnabled);
    }

    public int getLoadRadius() {
        return getInt(LOAD_RADIUS_KEY, 2);
    }

    public void setLoadRadius(int loadRadius) {
        putInt(LOAD_RADIUS_KEY, loadRadius);
    }

    public float getFogDistance() {
        return getFloat(FOG_DISTANCE_KEY, FOG_DISTANCE_DEFAULT_VALUE);
    }

    public void setFogDistance(float fogDistance) {
        putFloat(FOG_DISTANCE_KEY, fogDistance);
    }

    public boolean isInvertY() {
        return getBoolean(INVERT_Y_KEY, false);
    }

    public void setInvertY(boolean isInvertY) {
        putBoolean(INVERT_Y_KEY, isInvertY);
    }

    public int getEnterCount() {
        return getInt(ENTER_COUNT_KEY, 0);
    }

    public void setEnterCount(int enterCount) {
        putInt(ENTER_COUNT_KEY, enterCount);
    }

    public boolean isFirstTimeStarted() {
        return getBoolean(FIRST_TIME_STARTED_KEY, true);
    }

    public void setFirstTimeStarted(boolean isFirstTimeStarted) {
        putBoolean(FIRST_TIME_STARTED_KEY, isFirstTimeStarted);
    }
}
