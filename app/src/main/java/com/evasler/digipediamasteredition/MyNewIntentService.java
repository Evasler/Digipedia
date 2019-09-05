package com.evasler.digipediamasteredition;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.Objects;

import androidx.core.app.NotificationManagerCompat;

public class MyNewIntentService extends IntentService {

    private static final int NOTIFICATION_ID = 1;
    private static final int FOREGROUND_ID = 1995;
    private static final String NOTIFICATION_CHANNEL_ID = "digipedia_channel";

    public MyNewIntentService() {
        super("MyNewIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        String status = prefs.getString(getString(R.string.notification_status), "empty");

        if(Objects.requireNonNull(status).equals("enabled")) {


            DailyDigimon.generateDailyDigimon(getApplicationContext());

            Uri digiviceRing = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.digivice_single);

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle("Featured Digimon")
                    .setContentText(DailyDigimon.getDailyDigimon(getApplicationContext()).replace("plusSign", "+"))
                    .setSmallIcon(R.drawable.digivice_notification)
                    .setSound(digiviceRing)
                    .setAutoCancel(true);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder.setChannelId(NOTIFICATION_CHANNEL_ID);
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.daily_notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Intent notifyIntent = new Intent(this, MainActivity.class);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //to be able to launch your activity from the notification
            builder.setContentIntent(pendingIntent);

            startForeground(FOREGROUND_ID, builder.build());

            Notification notificationCompat = builder.build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(NOTIFICATION_ID, notificationCompat);
        }
    }
}
