package com.solverlabs.worldcraft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private Activity currentActivity = null;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.currentActivity = mCurrentActivity;
    }

    public boolean isCurrentActivity(Activity activity) {
        return this.currentActivity != null && this.currentActivity.equals(activity);
    }
}
