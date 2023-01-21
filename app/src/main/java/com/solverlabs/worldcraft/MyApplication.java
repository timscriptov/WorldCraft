package com.solverlabs.worldcraft;

import android.app.Activity;

import com.solverlabs.droid.rugl.GameApp;

public class MyApplication extends GameApp {
    private Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Activity getCurrentActivity() {
        return this.currentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.currentActivity = mCurrentActivity;
    }

    public boolean isCurrentActivity(Activity activity) {
        return this.currentActivity != null && this.currentActivity.equals(activity);
    }
}
