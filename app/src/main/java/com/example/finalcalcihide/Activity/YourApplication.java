package com.example.finalcalcihide.Activity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class YourApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private int numStarted = 0;
    boolean FirsttimeStarted = false;



    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (numStarted == 0 && FirsttimeStarted) {
            onAppForeground();
        }
        FirsttimeStarted = true;
        numStarted++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        numStarted--;
        if (numStarted == 0) {
            onAppBackground();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    private void onAppForeground() {
        Log.d("YourApplication", "App came to the foreground -- YourApplicationClass");

        Intent intent = new Intent(this, ForegroundReceiver.class);
        sendBroadcast(intent);

    }

    private void onAppBackground() {
        Log.d("YourApplication", "App went to the background -- YourApplicationClass");

    }

    // Public method to get app state
    public boolean isAppInForeground() {
        return numStarted > 0;
    }

    // Example public method
    public void logAppStatus(String message) {
        Log.d("YourApplication", message);
    }


}
