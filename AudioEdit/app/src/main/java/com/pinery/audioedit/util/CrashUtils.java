package com.pinery.audioedit.util;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import com.pinery.audioedit.common.Constant;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Crash Log相关工具类
 */
public final class CrashUtils {

  private static final String INIT_DIR_NAME = Constant.NAME;
  private static final String TAG = "CrashUtils";
  private static final String FILE_SEP = System.getProperty("file.separator");
  private static final String LINE_SEP = System.getProperty("line.separator");
  private static final Format FORMAT =
      new SimpleDateFormat("MM-dd HH:mm:ss.SSS ", Locale.getDefault());

  private static CrashUtils sCrashUtils;

  private Application mContext;
  private ExecutorService mExecutor;
  // log存储目录
  private String mCrashDir;
  // log文件前缀
  private String mFilePrefix = "crash-";
  // log写入文件开关，默认开
  private boolean mLog2FileSwitch = true;

  private CrashUtils(Application context) {
    mContext = context;

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerWrapped());

    setDefaultDir();
  }

  public void destroy() {
    if (mExecutor != null) {
      mExecutor.shutdown();
      mExecutor = null;
    }
    sCrashUtils = null;
  }

  public static CrashUtils getInstance(Application context) {
    if (sCrashUtils == null) {
      synchronized (CrashUtils.class) {
        if (sCrashUtils == null) {
          sCrashUtils = new CrashUtils(context);
        }
      }
    }
    return sCrashUtils;
  }

  private void printCrash2File(final String threadName, final String msg) {
    Date now = new Date(System.currentTimeMillis());
    String format = FORMAT.format(now);
    String date = format.substring(0, 5);
    String time = format.substring(6);
    final String fullPath = mCrashDir + mFilePrefix + date + ".txt";
    if (!createOrExistsFile(fullPath)) {
      Log.e(TAG, "log to " + fullPath + " failed!");
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(time)
        .append("FATAL EXCEPTION:")
        .append(threadName)
        .append("\n")
        .append(msg)
        .append("\n════════════════════════\n")
        .append(LINE_SEP);

    final String content = sb.toString();
    if (write2File(content, fullPath)) {
      Log.d(TAG, "log to " + fullPath + " success!");
    } else {
      Log.e(TAG, "log to " + fullPath + " failed!");
    }
  }

  private boolean createOrExistsFile(final String filePath) {
    File file = new File(filePath);
    if (file.exists()) return file.isFile();
    if (!createOrExistsDir(file.getParentFile())) return false;
    try {
      boolean isCreate = file.createNewFile();
      if (isCreate) printDeviceInfo(filePath);
      return isCreate;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private void printDeviceInfo(final String filePath) {
    String versionName = "";
    int versionCode = 0;
    try {
      PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
      if (pi != null) {
        versionName = pi.versionName;
        versionCode = pi.versionCode;
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    final String head = new StringBuilder().append("************* Log Head ****************")
        .append("\nDevice Manufacturer: ")
        .append(Build.MANUFACTURER)
        .append("\nDevice Model       : ")
        .append(Build.MODEL)
        .append("\nAndroid Version    : ")
        .append(Build.VERSION.RELEASE)
        .append("\nAndroid SDK        : ")
        .append(Build.VERSION.SDK_INT)
        .append("\nApp VersionName    : ")
        .append(versionName)
        .append("\nApp VersionCode    : ")
        .append(versionCode)
        .append("\n************* Log Head ****************\n\n")
        .toString();
    write2File(head, filePath);
  }

  private static boolean createOrExistsDir(final File file) {
    return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
  }

  private static boolean isSpace(final String s) {
    if (s == null) return true;
    for (int i = 0, len = s.length(); i < len; ++i) {
      if (!Character.isWhitespace(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean write2File(final String input, final String filePath) {
    if (mExecutor == null) {
      mExecutor = Executors.newSingleThreadExecutor();
    }
    Future<Boolean> submit = mExecutor.submit(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        BufferedWriter bw = null;
        try {
          bw = new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8"));

          bw.write(input);
          bw.flush();
          return true;
        } catch (IOException e) {
          e.printStackTrace();
          return false;
        } finally {
          try {
            if (bw != null) {
              bw.close();
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });
    try {
      return submit.get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public static void init(Application app) {
    getInstance(app);
  }

  private class UncaughtExceptionHandlerWrapped implements Thread.UncaughtExceptionHandler {
    Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    UncaughtExceptionHandlerWrapped() {
      uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override public void uncaughtException(Thread thread, Throwable throwable) {
      if (mLog2FileSwitch) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        try {
          throwable.printStackTrace(printWriter);
          printWriter.flush();
          printCrash2File(Thread.currentThread().getName(), outputStream.toString());
        } finally {
          printWriter.close();
        }
      }
      if (uncaughtExceptionHandler != null) {
        uncaughtExceptionHandler.uncaughtException(thread, throwable);
      }
    }
  }

  public CrashUtils setLog2FileSwitch(final boolean log2FileSwitch) {
    mLog2FileSwitch = log2FileSwitch;
    return this;
  }

  public CrashUtils setDefaultDir() {
    String defaultDir;

    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      defaultDir = new StringBuilder().append(Environment.getExternalStorageDirectory())
          .append(FILE_SEP)
          .append(INIT_DIR_NAME)
          .append(FILE_SEP)
          .append("crash")
          .append(FILE_SEP)
          .toString();
    } else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
        && mContext.getExternalCacheDir() != null) {
      defaultDir = new StringBuilder().append(mContext.getExternalCacheDir())
          .append(FILE_SEP)
          .append("crash")
          .append(FILE_SEP)
          .toString();
    } else {
      defaultDir = new StringBuilder().append(mContext.getCacheDir())
          .append(FILE_SEP)
          .append("crash")
          .append(FILE_SEP)
          .toString();
    }

    return setDir(defaultDir);
  }

  public CrashUtils setDir(final String dir) {
    if (isSpace(dir)) {
      mCrashDir = null;
    } else {
      mCrashDir = dir.endsWith(FILE_SEP) ? dir : dir + FILE_SEP;
    }
    return this;
  }

  public CrashUtils setDir(final File dir) {
    mCrashDir = dir == null ? null : dir.getAbsolutePath() + FILE_SEP;
    return this;
  }

  public CrashUtils setFilePrefix(final String filePrefix) {
    if (isSpace(filePrefix)) {
      mFilePrefix = "crash-";
    } else {
      mFilePrefix = filePrefix;
    }
    return this;
  }

  @Override public String toString() {
    return new StringBuilder().append(LINE_SEP)
        .append("file: ")
        .append(mLog2FileSwitch)
        .append(LINE_SEP)
        .append("dir: ")
        .append(mCrashDir)
        .append(LINE_SEP)
        .append("filePrefix")
        .append(mFilePrefix)
        .toString();
  }
}