package com.eitankri.tanachyomi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import hotchemi.android.rate.AppRate;


public class MainActivity extends AppCompatActivity {
    String mPreference = "mPreference";
    private static final int NOTIFICATION_REQUEST_CODE = 1919;


    SharedPreferences sharedpreferences;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(mPreference,
                Context.MODE_PRIVATE);

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
            public void onPageFinished(WebView view, String url) {

                mWebView.loadUrl("javascript:(function() { " +
                        "document.getElementsByClassName(' hideOnLG')[0].style.display='none'; })()");

                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                findViewById(R.id.view).setVisibility(View.INVISIBLE);
                findViewById(R.id.textView8).setVisibility(View.INVISIBLE);
                findViewById(R.id.floatingActionButton).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonToDonate).setVisibility(View.VISIBLE);
                if (sharedpreferences.getBoolean("firstTime", true)) {


                    FirstTimeDialog Dialog = new FirstTimeDialog(MainActivity.this);
                    Dialog.show();
                    Dialog.setOnDismissListener(dialog -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                            if (ContextCompat.checkSelfPermission(
                                    MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) ==
                                    PackageManager.PERMISSION_DENIED) {
                                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST_CODE);
                            }

                        }

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
                    });


                } else {
                    if (url.equals("https://www.tanachyomi.co.il/")) {
                        mWebView.scrollBy(0, 2400);
                    }

                }
            }
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
                    Toast.makeText(MainActivity.this, R.string.not_allowed, Toast.LENGTH_SHORT).show();
                }
                return;
        }
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


                                Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("EXIT", true);
                                startActivity(intent);
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

    public static Boolean getIfStudent() {
        String CookieValue = "";
        String siteName = "https://www.tanachyomi.co.il/";
        String cookieName = "students";

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        String[] temp = cookies.split(";");
        for (String ar1 : temp) {
            if (ar1.contains(cookieName)) {
                String[] temp1 = ar1.split("=");
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
