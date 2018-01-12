package com.pinery.audioedit.util;

import android.util.Log;
import com.pinery.audioedit.common.Constant;

/**
 * Log打印日志类
 */
public class LogUtil {
  public static boolean hideLog = !Constant.Debug;
  private static final String TAG = "AudioEdit";

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
   * @param message
   * @param throwable
   */
  public static void e(String message, Throwable throwable) {
    e(TAG, message, throwable);
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
}
