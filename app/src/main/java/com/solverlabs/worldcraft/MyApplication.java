package com.solverlabs.worldcraft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
