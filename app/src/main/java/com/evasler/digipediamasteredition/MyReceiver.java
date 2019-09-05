package com.evasler.digipediamasteredition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {

    public MyReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MyNewIntentService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent1);
        } else {
            context.startService(intent1);
        }
    }
}
