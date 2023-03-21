package com.eitankri.tanachyomi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import hotchemi.android.rate.AppRate;


public class MainActivity extends AppCompatActivity {
    String mPreference = "mPreference";

    SharedPreferences sharedpreferences;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);


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
                ((TextView) findViewById(R.id.textView8)).setText(R.string.loading);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        //כאשר סוגרים את האפליקציה דרך המסך הראשי הוא עובר לכאן ומכאן שוב יוצא
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finishAndRemoveTask();

        }

        String mURL = "https://www.tanachyomi.co.il/";


        findViewById(R.id.buttonToDonate).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tanachyomi.co.il/%D7%AA%D7%A8%D7%95%D7%9E%D7%94_%D7%9C%D7%90%D7%AA%D7%A8"));
            startActivity(browserIntent);
        });
        findViewById(R.id.floatingActionButton).setOnClickListener(v -> {


            Intent startActivity = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(startActivity);
        });

        WebView mWebView = findViewById(R.id.webViewMikveSearch);
        mWebView.loadUrl(mURL);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);




        mWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if(url.startsWith("tel:") || url.startsWith("whatsapp:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {

                mWebView.loadUrl("javascript:(function() { " +
                        "document.getElementsByClassName(' hideOnLG')[0].style.display='none'; })()");

                //make all loading invisible
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                findViewById(R.id.view).setVisibility(View.INVISIBLE);
                findViewById(R.id.textView8).setVisibility(View.INVISIBLE);
                findViewById(R.id.imageViewLogo).setAlpha(0f);

                findViewById(R.id.floatingActionButton).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonToDonate).setVisibility(View.VISIBLE);
                if (sharedpreferences.getBoolean("firstTime", true)) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(getApplicationContext(), reminderBroadcast.class);
                    PendingIntent pendingIntent;
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 1, 0);
                    //כדי שלא תקפוץ התראה שפותח פעם ראשונה
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);


                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("notifyHour", 0);
                    editor.putInt("notifyMinute", 1);
                    editor.putBoolean("firstTime", false);
                    editor.putBoolean("toNotify", true);
                    editor.apply();

                    FirstTimeDialog Dialog = new FirstTimeDialog(MainActivity.this);
                    Dialog.show();
                } else {
                    if (url.equals("https://www.tanachyomi.co.il/")) {
                        mWebView.loadUrl("javascript:document.getElementById('showDateHeader').scrollIntoView()");
                        mWebView.loadUrl("javascript:window.scrollBy(0, -10)");

                    }

                }

            }
        });


    }

    //כדי ליחזור אחורה בדפדפן ולא באפליקציה
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView mWebView = findViewById(R.id.webViewMikveSearch);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("לסגור אפליקציה")
                            .setMessage("האם אתה בטוח  שאתה רוצה לצאת")
                            .setPositiveButton("כן", (dialog, which) -> {

                                finishAndRemoveTask();

                            })
                            .setNegativeButton("לא", null)
                            .show();
                    //נמצא כדי להיות לפני הדיאלוג ששלעיל
                    //מדרג את האפליקציה
                    AppRate.with(this)
                            .setInstallDays(2)
                            .setLaunchTimes(10)
                            .setRemindInterval(3)
                            .monitor();
                    AppRate.showRateDialogIfMeetsConditions(this);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * -------------------------------for alarms----------------------
     */
    private void checkIfRecreateNotify() {
        Context context = getApplicationContext();
        String mPreference = "mPreference";
        SharedPreferences sharedpreferences = context.getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);

        if (!isAlarmUp(context, 1)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent NotIntent = new Intent(context, reminderBroadcast.class);

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                    , sharedpreferences.getInt("notifyHour", 0), sharedpreferences.getInt("notifyMinute", 1));


            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, NotIntent,PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
    public static boolean isAlarmUp(Context context, int request) {
        Intent myIntent = new Intent(context, reminderBroadcast.class);
        return PendingIntent.getBroadcast(context, request, myIntent, PendingIntent.FLAG_IMMUTABLE) != null;
    }

    /************************************end of main code**********************************************/
    public static class FirstTimeDialog extends Dialog {

        public FirstTimeDialog(Activity a) {
            super(a);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_first_time);

            Button buttonGoToSetting = findViewById(R.id.buttonGoToSetting);
            Button dismiss = findViewById(R.id.buttonDialogExit);

            buttonGoToSetting.setOnClickListener(v -> dismiss());
            dismiss.setOnClickListener(v -> dismiss());

        }

    }
    public static Boolean getIfStudent(){
        String CookieValue = "";
        String siteName = "https://www.tanachyomi.co.il/";
        String cookieName = "students";

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        String[] temp=cookies.split(";");
        for (String ar1 : temp ){
            if(ar1.contains(cookieName)){
                String[] temp1=ar1.split("=");
                CookieValue = temp1[1];
                break;
            }
        }
        return CookieValue.equals("y");
    }
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("students", getIfStudent());
        editor.apply();
        CookieManager.getInstance().flush();//if user destroy the app still save cookies
    }
}
