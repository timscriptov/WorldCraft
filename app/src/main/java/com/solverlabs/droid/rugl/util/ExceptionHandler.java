package com.solverlabs.droid.rugl.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;


public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String exceptionsDir = "exceptions";
    private static final Map<String, String> extraInfo = new TreeMap();
    private static ExceptionHandler instance = null;
    private final Context context;
    private Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

    private ExceptionHandler(Context context) {
        this.context = context;
    }

    public static void addLogInfo(String key, String logInfo) {
        extraInfo.put(key, logInfo);
    }

    public static void register(Context context, String... address) {
        if (instance == null) {
            instance = new ExceptionHandler(context);
        }
        Thread.setDefaultUncaughtExceptionHandler(instance);
        File ed = context.getDir(exceptionsDir, 0);
        if (ed.listFiles().length > 0) {
            StringBuilder subject = new StringBuilder();
            StringBuilder body = new StringBuilder();
            File[] arr$ = ed.listFiles();
            for (File f : arr$) {
                subject.append(f.getName()).append(", ");
                try {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    while (true) {
                        String line = br.readLine();
                        if (line != null) {
                            body.append(line).append("\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                body.append("\n\t----\n");
                f.delete();
            }
            Intent i = new Intent("android.intent.action.SEND");
            i.setType("message/rfc822");
            i.putExtra("android.intent.extra.EMAIL", address);
            i.putExtra("android.intent.extra.SUBJECT", subject.toString());
            i.putExtra("android.intent.extra.TEXT", body.toString());
            context.startActivity(Intent.createChooser(i, "Send error report with:"));
        }
    }

    public static void handle(Throwable e) {
        if (instance != null) {
            instance.uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        File ed = this.context.getDir(exceptionsDir, 0);
        File out = new File(ed, ex.getClass().getSimpleName() + "@" + new Date().toString().replace(' ', '_'));
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(out));
            try {
                PackageInfo pi = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
                bos.write("Version number = " + pi.versionCode + "\n");
                bos.write("Version name = " + pi.versionName + "\n");
            } catch (PackageManager.NameNotFoundException e) {
                bos.write("could not find package info");
            }
            bos.write(new Date().toString() + "\n\n");
            bos.write("SDK version = " + Build.VERSION.SDK + "\n");
            bos.write("Manufacturer = " + Build.MANUFACTURER + "\n");
            bos.write("Product = " + Build.PRODUCT + "\n");
            bos.write("Model = " + Build.MODEL + "\n");
            bos.write("Device = " + Build.DEVICE + "\n");
            bos.write("Brand = " + Build.BRAND + "\n");
            bos.write("Fingerprint = " + Build.FINGERPRINT + "\n\n");
            bos.write(result.toString());
            bos.write("\nOn thread " + thread.toString());
            bos.write("\n\nExtra log information:\n");
            for (Map.Entry<String, String> e2 : extraInfo.entrySet()) {
                bos.write(e2.getKey());
                bos.write(":\t");
                bos.write(e2.getValue());
            }
            bos.close();
        } catch (Exception e3) {
            Log.e("ExceptionHandler", "Oh dear. encountered a " + e3.getMessage() + " while trying to save a log for:", ex);
        }
        if (this.defaultHandler != null) {
            this.defaultHandler.uncaughtException(thread, ex);
        }
    }
}
