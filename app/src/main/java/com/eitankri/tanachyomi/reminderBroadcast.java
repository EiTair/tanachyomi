package com.eitankri.tanachyomi;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

import static com.eitankri.tanachyomi.App.CHANNEL_2_ID;


public class reminderBroadcast extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {


        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = context.getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);
        boolean isStudent = sharedpreferences.getBoolean("students", false);
        if (sharedpreferences.getBoolean("toNotify", false)) {
            if ( isStudent|| Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY ) {
                String text = new ParekYomiCalculator().getParekYomi(isStudent);
                if (!text.equals("")) {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    Notification notification;

                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,
                            10, notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE);

                    notification = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                            .setAutoCancel(true)
                            .setContentTitle(text)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentIntent(contentIntent)
                            .build();
                    notificationManager.notify(1, notification);
                }
            }
        }
    }

}
