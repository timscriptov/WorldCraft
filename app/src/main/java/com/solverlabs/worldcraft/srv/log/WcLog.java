package com.solverlabs.worldcraft.srv.log;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WcLog {
    private static final String TAG = "MULTIPLAYER";
    private static Method debugMethod;
    private static Method debugThrowableMethod;
    private static Method errorMethod;
    private static Method errorThrowableMethod;
    private static Method evqueInfoMethod;
    private static Object evqueLogger;
    private static Method infoMethod;
    private static Method infoThrowableMethod;
    private static Object log4jLogger;
    private static Method warnMethod;
    private static Method warnThrowableMethod;

    static {
        log4jLogger = null;
        evqueLogger = null;
        debugMethod = null;
        debugThrowableMethod = null;
        infoMethod = null;
        infoThrowableMethod = null;
        warnMethod = null;
        warnThrowableMethod = null;
        errorMethod = null;
        errorThrowableMethod = null;
        evqueInfoMethod = null;
        try {
            Class<?> cls = Class.forName("com.solverlabs.worldcraft.srv.log.JLogger");
            log4jLogger = cls.getDeclaredMethod("getOutLogger", new Class[0]).invoke(null, new Object[0]);
            debugMethod = log4jLogger.getClass().getMethod("debug", Object.class);
            debugThrowableMethod = log4jLogger.getClass().getMethod("debug", Object.class, Throwable.class);
            infoMethod = log4jLogger.getClass().getMethod("info", Object.class);
            infoThrowableMethod = log4jLogger.getClass().getMethod("info", Object.class, Throwable.class);
            warnMethod = log4jLogger.getClass().getMethod("warn", Object.class);
            warnThrowableMethod = log4jLogger.getClass().getMethod("warn", Object.class, Throwable.class);
            errorMethod = log4jLogger.getClass().getMethod("error", Object.class);
            errorThrowableMethod = log4jLogger.getClass().getMethod("error", Object.class, Throwable.class);
            evqueLogger = cls.getDeclaredMethod("getEvqueLogger", new Class[0]).invoke(null, new Object[0]);
            evqueInfoMethod = evqueLogger.getClass().getMethod("info", Object.class);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private final String name;
    private boolean useEvque;

    protected WcLog(String str) {
        this.useEvque = false;
        this.name = str;
        if ("EventQueue".equals(str)) {
            this.useEvque = true;
        }
    }

    @NonNull
    @Contract("_ -> new")
    public static WcLog getLogger(@NonNull Class<?> cls) {
        return new WcLog(cls.getName());
    }

    @NonNull
    @Contract(value = "_ -> new", pure = true)
    public static WcLog getLogger(String str) {
        return new WcLog(str);
    }

    @SuppressLint("SimpleDateFormat")
    private void print(String str, Object obj) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");
        System.out.println("[" + str + "] - " + simpleDateFormat.format(date) + " - " + obj);
    }

    public void debug(Object obj) {
        try {
            debugMethod.invoke(log4jLogger, obj);
        } catch (Throwable th) {
            debug(obj, null);
        }
    }

    public void debug(Object obj, Throwable th) {
        try {
            debugThrowableMethod.invoke(log4jLogger, obj, th);
        } catch (Throwable th2) {
            print("debug", obj);
            if (th != null) {
                th.printStackTrace();
            }
        }
    }

    public void error(Object obj) {
        try {
            errorMethod.invoke(log4jLogger, obj);
        } catch (Throwable th) {
            error(obj, null);
        }
    }

    public void error(Object obj, Throwable th) {
        try {
            errorThrowableMethod.invoke(log4jLogger, obj, th);
        } catch (Throwable th2) {
            print("error", obj);
            if (th != null) {
                th.printStackTrace();
            }
        }
    }

    public void fatal(Object obj) {
        fatal(obj, null);
    }

    public void fatal(Object obj, Throwable th) {
        print("fatal", obj);
        if (th != null) {
            th.printStackTrace();
        }
    }

    public void info(Object obj) {
        try {
            if (this.useEvque) {
                evqueInfoMethod.invoke(evqueLogger, obj);
            } else {
                infoMethod.invoke(log4jLogger, obj);
            }
        } catch (Throwable th) {
            info(obj, null);
        }
    }

    public void info(Object obj, Throwable th) {
        try {
            infoThrowableMethod.invoke(log4jLogger, obj, th);
        } catch (Throwable th2) {
            print("info", obj);
            if (th != null) {
                th.printStackTrace();
            }
        }
    }

    public void init() {
    }

    public void warn(Object obj) {
        try {
            warnMethod.invoke(log4jLogger, obj);
        } catch (Throwable th) {
            warn(obj, null);
        }
    }

    public void warn(Object obj, Throwable th) {
        try {
            warnThrowableMethod.invoke(log4jLogger, obj, th);
        } catch (Throwable th2) {
            print("warn", obj);
            if (th != null) {
                th.printStackTrace();
            }
        }
    }
}
