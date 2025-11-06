package com.eitankri.tanachyomi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;


import java.util.Calendar;

import hotchemi.android.rate.AppRate;


public class MainActivity extends AppCompatActivity {
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private static final int FLEXIBLE_APP_UPDATE_REQ_CODE = 123;
    private static final int NOTIFICATION_REQUEST_CODE = 1919;

    String mPreference = "mPreference";

    SharedPreferences sharedpreferences;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

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
                tryUpdate();

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
                if (url.startsWith("tel:") || url.startsWith("whatsapp:")) {
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
                    //see if needed premssion
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        if (ContextCompat.checkSelfPermission(
                                MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) ==
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


        // מאזין לאירוע חזרה חדש
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("לסגור אפליקציה")
                            .setMessage("האם אתה בטוח שאתה רוצה לצאת?")
                            .setPositiveButton("כן", (dialog, which) -> finishAndRemoveTask())
                            .setNegativeButton("לא", null)
                            .show();

                    // מדרג את האפליקציה (כמו קודם)
                    AppRate.with(MainActivity.this)
                            .setInstallDays(2)
                            .setLaunchTimes(10)
                            .setRemindInterval(3)
                            .monitor();
                    AppRate.showRateDialogIfMeetsConditions(MainActivity.this);
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


    /**
     * -------------------------------for update----------------------
     */
    private void tryUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        installStateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                removeInstallStateUpdateListener();
            } else {
//                Toast.makeText(getApplicationContext(), "InstallStateUpdatedListener: state: " + state.installStatus(), Toast.LENGTH_LONG).show();
            }
        };
        appUpdateManager.registerListener(installStateUpdatedListener);
        checkUpdate();
    }

    private void checkUpdate() {

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() ==
                    UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate();
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void popupSnackBarForCompleteUpdate() {
        Snackbar.make(findViewById(android.R.id.content).getRootView(), R.string.install_avaliable, Snackbar.LENGTH_INDEFINITE)

                .setAction(R.string.install, view -> {
                    if (appUpdateManager != null) {
                        appUpdateManager.completeUpdate();

                    }
                })
                .setActionTextColor(getResources().getColor(R.color.white))
                .show();
    }

    private void removeInstallStateUpdateListener() {
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FLEXIBLE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Update canceled by user!", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_OK) {
//                Toast.makeText(getApplicationContext(), "Update success!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Update Failed!", Toast.LENGTH_LONG).show();
//                tryUpdate();
            }

        }
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


            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, NotIntent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
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

    public static Boolean getIfStudent() {
        String CookieValue = "";
        String siteName = "https://www.tanachyomi.co.il/";
        String cookieName = "students";

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if (cookies == null)
            return false;
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

    @Override
    protected void onStop() {
        super.onStop();
        removeInstallStateUpdateListener();
    }
}