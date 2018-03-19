package com.pinery.test;

import android.app.Application;
import android.os.Handler;
import com.pinery.test.util.LogUtil;

/**
 * App
 */
public class App extends Application {

  public static App sInstance;

  public Handler mHandler;

  public static App getInstance() {
    return sInstance;
  }

  @Override public void onCreate() {
    super.onCreate();

    try {
      LogUtil.i("onCreate...");

      mHandler = new Handler();
      sInstance = this;

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override public void onTerminate() {
    super.onTerminate();
    LogUtil.i("onTerminate...");

    mHandler = null;
    sInstance = null;
  }
}
