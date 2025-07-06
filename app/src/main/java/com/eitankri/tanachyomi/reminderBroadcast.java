package com.eitankri.tanachyomi;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager; // Keep if you plan to check permissions
import android.util.Log; // Import Android's Log class

import androidx.core.app.ActivityCompat; // Keep if you plan to check permissions
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

import static com.eitankri.tanachyomi.App.CHANNEL_2_ID;


public class reminderBroadcast extends BroadcastReceiver {

    // Define a TAG for this class
    private static final String TAG = "ReminderBroadcast";

    @SuppressLint("MissingPermission") // Keep if you are sure about notification permissions
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "onReceive triggered. Intent action: " + (intent != null ? intent.getAction() : "null intent"));

        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = context.getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);
        boolean isStudent = sharedpreferences.getBoolean("students", false);

        Log.d(TAG, "Preference 'students': " + isStudent);

        boolean toNotify = sharedpreferences.getBoolean("toNotify", false);
        Log.d(TAG, "Preference 'toNotify': " + toNotify);

        if (toNotify) {
            Log.i(TAG, "'toNotify' is true. Checking conditions for notification.");

            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            boolean isSaturday = (dayOfWeek == Calendar.SATURDAY);
            Log.d(TAG, "Current day of week: " + dayOfWeek + ", Is Saturday: " + isSaturday);

            if (isStudent || !isSaturday) {
                Log.i(TAG, "Conditions met (isStudent or not Saturday). Proceeding to get Parek Yomi.");
                String text = "";
                try {
                    text = new ParekYomiCalculator().getParekYomi(isStudent);
                    Log.d(TAG, "Parek Yomi text calculated: \"" + text + "\"");
                } catch (Exception e) {
                    Log.e(TAG, "Error calculating Parek Yomi", e);
                }


                if (text != null && !text.isEmpty()) { // Check for null as well
                    Log.i(TAG, "Parek Yomi text is not empty. Building notification.");
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    Notification notification;

                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,
                            10, notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE);

                    notification = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                            .setAutoCancel(true)
                            .setContentTitle(text)
                            .setSmallIcon(R.drawable.ic_notification) // Ensure this drawable exists
                            .setContentIntent(contentIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Good practice
                            .build();

                    try {
                        // On Android 13 (API 33) and above, you need to check for POST_NOTIFICATIONS permission
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot send notification on Android 13+.");
                                // Optionally, you might want to inform the user or log this more prominently.
                                return; // Exit if permission is not granted
                            }
                        }
                        notificationManager.notify(1, notification);
                        Log.i(TAG, "Notification sent successfully with ID 1.");
                    } catch (SecurityException se) {
                        Log.e(TAG, "SecurityException while trying to send notification. Missing permission?", se);
                    }
                    catch (RuntimeException e) {
                        Log.e(TAG, "RuntimeException while sending notification.", e);
                    }
                } else {
                    Log.i(TAG, "Parek Yomi text is empty or null. Notification not sent.");
                }
            } else {
                Log.i(TAG, "Conditions NOT met (Not student AND it is Saturday). Notification not sent.");
            }
        } else {
            Log.i(TAG, "'toNotify' is false. No notification will be sent.");
        }
    }
}