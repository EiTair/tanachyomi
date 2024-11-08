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

    int theWrapContentMoreDataLayoutHeight = 0;
    private static final int NOTIFICATION_REQUEST_CODE = 1919;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //see if needed premssion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    SettingActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST_CODE);
            }

        }

        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);


        com.google.android.material.switchmaterial.SwitchMaterial switchSettingToNotify = findViewById(R.id.switchSettingToNotifi);
        Button buttonSettingSetAlarmTime = findViewById(R.id.buttonSettingSetAlarmTime);
        com.google.android.material.switchmaterial.SwitchMaterial switchIsStudent = findViewById(R.id.switchIsStudent);
        TextView textViewImportantMassage = findViewById(R.id.textViewImportantMassage);
        ConstraintLayout notificationLayout = findViewById(R.id.notifactionLayout);

        switchIsStudent.setChecked(MainActivity.getIfStudent());
        //מקבל את הגובה של הגדרות של התראות כשהוא עותף את הטקסט
        //כדי לחזור אליו
        notificationLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                notificationLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                theWrapContentMoreDataLayoutHeight = notificationLayout.getHeight(); // here get the height of the view

                if (sharedpreferences.getBoolean("toNotify", false)) {
                    slideView(notificationLayout, 0, theWrapContentMoreDataLayoutHeight);
                    switchSettingToNotify.setChecked(true);
                    int selectedHour = sharedpreferences.getInt("notifyHour", 0);
                    int selectedMinute = sharedpreferences.getInt("notifyMinute", 1);

                    String selectedHourStr = "" + selectedHour;
                    String selectedMinuteStr = "" + selectedMinute;
                    if (selectedHour < 10) {
                        selectedHourStr = "0" + selectedHour;
                    }
                    if (selectedMinute < 10) {
                        selectedMinuteStr = "0" + selectedMinute;
                    }

                    buttonSettingSetAlarmTime.setText(selectedHourStr + ":" + selectedMinuteStr);

                } else {
                    slideView(notificationLayout, theWrapContentMoreDataLayoutHeight, 0);
                }
            }
        });

        //אם לוחצים על "חשוב לדעת"
        textViewImportantMassage.setOnClickListener(v -> {
            ImportantDialog Dialog = new ImportantDialog(SettingActivity.this);
            Dialog.show();
        });
        //כאשר לוחצים על הזמן כדי לקבוע שעת התראה
        buttonSettingSetAlarmTime.setOnClickListener(v -> {
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(SettingActivity.this, (timePicker, selectedHour, selectedMinute) -> {
                String selectedHourStr = "" + selectedHour;
                String selectedMinuteStr = "" + selectedMinute;
                if (selectedHour < 10) {
                    selectedHourStr = "0" + selectedHour;
                }
                if (selectedMinute < 10) {
                    selectedMinuteStr = "0" + selectedMinute;
                }
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
                //אם זמן ההתראה היה כבר היום אז יתריע רק מחר
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                Log.e("AAA","asdasdasdasdasdsa");


            }, sharedpreferences.getInt("notifyHour", 0), sharedpreferences.getInt("notifyMinute", 1), true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();


        });

        //הסוויץ בין להפעיל ללכבות התראות
        switchSettingToNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {

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
        switch (requestCode) {
            case NOTIFICATION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(SettingActivity.this, R.string.not_allowed, Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
    public static void slideView(View view, int currentHeight, int newHeight) {

        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(700);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        slideAnimator.addUpdateListener(animation1 -> {
            view.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            view.requestLayout();
        });

        /*  We use an animationSet to play the animation  */

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

            Button buttonGoToAppInfo = findViewById(R.id.buttonGoToAppInfo);
            Button dismiss = findViewById(R.id.buttonDialogExit);

            buttonGoToAppInfo.setOnClickListener(v -> {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            });
            dismiss.setOnClickListener(v -> dismiss());

        }

    }
}