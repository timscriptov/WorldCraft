package com.solverlabs.worldcraft;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
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
        return (this.currentActivity == null || activity == null || !this.currentActivity.equals(activity)) ? false : true;
    }
}
