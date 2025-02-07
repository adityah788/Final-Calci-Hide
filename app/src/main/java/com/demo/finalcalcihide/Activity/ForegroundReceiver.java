package com.demo.finalcalcihide.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ForegroundReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // Check if MainActivity3 is already active
        if (!Calculator.isActivityActive()) {
            Intent activityIntent = new Intent(context, Calculator.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Needed if you are starting the activity from a non-activity context
            context.startActivity(activityIntent);
        }
    }

}

