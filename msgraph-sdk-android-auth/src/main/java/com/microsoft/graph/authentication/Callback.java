package com.microsoft.graph.authentication;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class Callback implements Application.ActivityLifecycleCallbacks {

    private Activity currentActivity;

    private void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public Callback(Activity activity) {
        setCurrentActivity(activity);
    }

    public Activity getActivity() {
        return currentActivity;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        setCurrentActivity(activity);
    }
}

