package com.byteflipper.crashcatchermanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

public class CrashCatcherManager {

    private static final String TAG = "CrashCatcherManager";
    private static CrashCatcherManager instance;
    private Thread.UncaughtExceptionHandler defaultUEH;

    private final Context context;

    private final NetworkUtils networkUtils;
    private final ShakeDetector shakeDetector;

    public CrashCatcherManager(Context context) {
        this.context = context.getApplicationContext();
        networkUtils = new NetworkUtils(context);
        shakeDetector = new ShakeDetector(context);
        shakeDetector.setOnShakeListener(this::showShakeDialog);
    }

    public static synchronized CrashCatcherManager getInstance(Context context) {
        if (instance == null) {
            instance = new CrashCatcherManager(context);
        }
        return instance;
    }

    public void init() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            logException(exception);

            if (defaultUEH != null) {
                defaultUEH.uncaughtException(thread, exception);
            }
        });
    }

    public void startShakeDetection() {
        shakeDetector.start();
    }

    public void stopShakeDetection() {
        shakeDetector.stop();
    }

    private void logException(Throwable exception) {
        try {
            String fileName = "crash_" + System.currentTimeMillis() + ".log";
            File file = new File(context.getExternalFilesDir(null), fileName);

            PrintWriter pw = new PrintWriter(new FileWriter(file, true));
            pw.println("Timestamp: " + new Date());
            pw.println("Device: " + Build.BRAND + " " + Build.MODEL);
            pw.println("Android Version: " + Build.VERSION.RELEASE + " API: " + Build.VERSION.SDK_INT);
            pw.println("Network Type: " + networkUtils.getConnectionType());
            pw.println("---- Crash Log ----");
            exception.printStackTrace(pw);
            pw.println("-------------------");
            pw.flush();
            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "An error occurred while logging exception", e);
        }
    }

    private void showShakeDialog() {
        /* BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.shake_detect_dialog);
        bottomSheetDialog.show(); */

        if (context instanceof Activity) {
            Intent intent = new Intent(context, CrashDetectActivity.class);
            context.startActivity(intent);
        } else {
            Log.e(TAG, "Context is not an instance of Activity, cannot start CrashDetectActivity");
            // Optionally, you can use the application context with a new task flag, but be cautious with this approach
            // Intent intent = new Intent(context, CrashDetectActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // context.startActivity(intent);
        }
    }

}