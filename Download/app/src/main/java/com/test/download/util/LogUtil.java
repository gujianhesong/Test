package com.test.download.util;

import android.util.Log;

public final class LogUtil {

    public static final String TAG = "PointsMall_M";

    public static boolean hideLog = false;

    /**
     * 设置打印日志是否可用
     */
    public static void setEnable(boolean logEnable) {
        hideLog = !logEnable;
    }

    /**
     * @param message
     */
    public static void v(String message) {
        v("", message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void v(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.v(tag, message);
    }

    /**
     * @param message
     */
    public static void d(String message) {
        d("", message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.d(tag, message);
    }

    /**
     * @param message
     */
    public static void w(String message) {
        w("", message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void w(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.w(tag, message);
    }

    /**
     */
    public static void i() {
        i("", "");
    }

    /**
     * @param message
     */
    public static void i(String message) {
        i("", message);
    }

    /**
     * @param showLog
     * @param message
     */
    public static void i(boolean showLog, String message) {
        if (!showLog) {
            return;
        }

        i(message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void i(String tag, String message) {
        if (hideLog) {
            return;
        }

        tag = TAG + "-" + tag;
        message = getFunctionName() + message;

        Log.i(tag, message);
    }

    /**
     * @param message
     */
    public static void e(String message) {
        e(TAG, message);
    }

    /**
     * @param tag
     * @param message
     */
    public static void e(String tag, String message) {
        e(tag, message, null);
    }

    /**
     * @param throwable
     */
    public static void e(final Throwable throwable) {
        e(TAG, "", throwable);
    }

    /**
     * @param tag
     * @param message
     */
    public static void e(String tag, final String message, final Throwable throwable) {
        if (hideLog) {
            return;
        }

        final String tagName = getFunctionName() + message;

        Log.e(tag, tagName, throwable);

    }

    /**
     * @param message
     */
    public static void printStack(String message) {
        try {
            throw new Exception("打印堆栈:" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return "";
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(LogUtil.class.getName())) {
                continue;
            }

            return "["
                    + Thread.currentThread().getId()
                    + ": "
                    + st.getFileName()
                    + " : "
                    + st.getLineNumber()
                    + " : "
                    + st.getMethodName()
                    + "]---";
        }

        return "";
    }

    public static void printError(Exception e) {
        e.printStackTrace();
    }

    public static void now(String msg) {
    }

    public static void cost(String msg) {
    }

    public static void at(String msg) {
        long now = System.currentTimeMillis();
        d("time", msg + ":" + now);
    }

    public static void printPerformance(int code, long startTime) {
    }

}
