package com.solverlabs.droid.rugl;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class GameApp extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
