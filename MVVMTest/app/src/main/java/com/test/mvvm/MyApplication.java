package com.test.mvvm;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by gujian on 2018-08-19.
 */

public class MyApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();

    LeakCanary.install(this);

  }
}
