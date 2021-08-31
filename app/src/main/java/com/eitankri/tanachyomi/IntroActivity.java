package com.eitankri.tanachyomi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class IntroActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        checkIfRecreateNotify();
        ImageView imageView = findViewById(R.id.imageViewLogo);
        //מכין את האנימציה
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        //מתחיל את האנימציה בפועל
        imageView.startAnimation(anim);
        //מקשיב מתי האנימציה נגמרת
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Intent startActivity = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(startActivity);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        //כאשר סוגרים את האפליקציה דרך המסך הראשי הוא עובר לכאן ומכאן שוב יוצא
        if (getIntent().getBooleanExtra("EXIT", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            }else{
                finish();
            }

        }
    }
    private void checkIfRecreateNotify() {
        Context context = getApplicationContext();
        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = context.getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);

        if (!IntroActivity.isAlarmUp(context, 1)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent NotIntent = new Intent(context, reminderBroadcast.class);

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                    , sharedpreferences.getInt("notifyHour", 0), sharedpreferences.getInt("notifyMinute", 1));


            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, NotIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
    public static boolean isAlarmUp(Context context, int request) {
        Intent myIntent = new Intent(context, reminderBroadcast.class);
        return PendingIntent.getBroadcast(context, request, myIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }
}






