package com.eitankri.tanachyomi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Calendar;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive triggered with action: " + (intent != null ? intent.getAction() : "null"));

        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = context.getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);
        Log.d(TAG, "SharedPreferences retrieved: " + mPreference);

        int notifyHour = sharedpreferences.getInt("notifyHour", 0);
        int notifyMinute = sharedpreferences.getInt("notifyMinute", 1);
        Log.d(TAG, "Notification time - Hour: " + notifyHour + ", Minute: " + notifyMinute);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent NotIntent = new Intent(context, reminderBroadcast.class);
        Log.d(TAG, "AlarmManager and Intent initialized");

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                , notifyHour, notifyMinute);
        Log.d(TAG, "Alarm set for: " + calendar.getTime().toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, NotIntent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.d(TAG, "Repeating alarm scheduled");
    }
}