package com.eitankri.tanachyomi;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = "SettingActivity";
    int theWrapContentMoreDataLayoutHeight = 0;
    private static final int NOTIFICATION_REQUEST_CODE = 1919;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Log.d(TAG, "onCreate: Activity started");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    SettingActivity.this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "Requesting notification permission");
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }

        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = getSharedPreferences(mPreference, Context.MODE_PRIVATE);

        com.google.android.material.switchmaterial.SwitchMaterial switchSettingToNotify = findViewById(R.id.switchSettingToNotifi);
        Button buttonSettingSetAlarmTime = findViewById(R.id.buttonSettingSetAlarmTime);
        com.google.android.material.switchmaterial.SwitchMaterial switchIsStudent = findViewById(R.id.switchIsStudent);
        TextView textViewImportantMassage = findViewById(R.id.textViewImportantMassage);
        ConstraintLayout notificationLayout = findViewById(R.id.notifactionLayout);

        switchIsStudent.setChecked(MainActivity.getIfStudent());
        Log.d(TAG, "Student mode: " + MainActivity.getIfStudent());

        notificationLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                notificationLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                theWrapContentMoreDataLayoutHeight = notificationLayout.getHeight();
                Log.d(TAG, "Notification layout height calculated: " + theWrapContentMoreDataLayoutHeight);

                boolean toNotify = sharedpreferences.getBoolean("toNotify", false);
                Log.d(TAG, "SharedPreferences toNotify: " + toNotify);

                if (toNotify) {
                    slideView(notificationLayout, 0, theWrapContentMoreDataLayoutHeight);
                    switchSettingToNotify.setChecked(true);

                    int selectedHour = sharedpreferences.getInt("notifyHour", 0);
                    int selectedMinute = sharedpreferences.getInt("notifyMinute", 1);
                    Log.d(TAG, "Saved alarm time: " + selectedHour + ":" + selectedMinute);

                    String selectedHourStr = (selectedHour < 10 ? "0" : "") + selectedHour;
                    String selectedMinuteStr = (selectedMinute < 10 ? "0" : "") + selectedMinute;
                    buttonSettingSetAlarmTime.setText(selectedHourStr + ":" + selectedMinuteStr);
                } else {
                    slideView(notificationLayout, theWrapContentMoreDataLayoutHeight, 0);
                }
            }
        });

        textViewImportantMassage.setOnClickListener(v -> {
            Log.d(TAG, "Important dialog clicked");
            ImportantDialog dialog = new ImportantDialog(SettingActivity.this);
            dialog.show();
        });

        buttonSettingSetAlarmTime.setOnClickListener(v -> {
            Log.d(TAG, "Set alarm time button clicked");
            TimePickerDialog mTimePicker = new TimePickerDialog(SettingActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                String selectedHourStr = (selectedHour < 10 ? "0" : "") + selectedHour;
                String selectedMinuteStr = (selectedMinute < 10 ? "0" : "") + selectedMinute;
                buttonSettingSetAlarmTime.setText(selectedHourStr + ":" + selectedMinuteStr);

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("notifyHour", selectedHour);
                editor.putInt("notifyMinute", selectedMinute);
                editor.apply();

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), reminderBroadcast.class);
                PendingIntent pendingIntent;

                Calendar calendar = Calendar.getInstance();
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), selectedHour, selectedMinute, 0);
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                Log.d(TAG, "Alarm set for: " + calendar.getTime().toString());
            }, sharedpreferences.getInt("notifyHour", 0), sharedpreferences.getInt("notifyMinute", 1), true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        switchSettingToNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Switch toNotify changed to: " + isChecked);
            if (isChecked) {
                slideView(notificationLayout, 0, theWrapContentMoreDataLayoutHeight);
            } else {
                slideView(notificationLayout, theWrapContentMoreDataLayoutHeight, 0);
            }
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean("toNotify", isChecked);
            editor.apply();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode=" + requestCode);
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.d(TAG, "Notification permission denied");
                Toast.makeText(SettingActivity.this, R.string.not_allowed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void slideView(View view, int currentHeight, int newHeight) {
        Log.d(TAG, "Sliding view from " + currentHeight + " to " + newHeight);
        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(700);

        slideAnimator.addUpdateListener(animation1 -> {
            view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public class ImportantDialog extends Dialog {

        public ImportantDialog(Activity a) {
            super(a);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_important_dialog);

            Log.d(TAG, "ImportantDialog created");

            Button buttonGoToAppInfo = findViewById(R.id.buttonGoToAppInfo);
            Button dismiss = findViewById(R.id.buttonDialogExit);

            buttonGoToAppInfo.setOnClickListener(v -> {
                Log.d(TAG, "User opened app info from dialog");
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            });

            dismiss.setOnClickListener(v -> {
                Log.d(TAG, "Dialog dismissed");
                dismiss();
            });
        }
    }
}
