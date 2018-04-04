package com.pinery.aidl.remote;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.pinery.aidl.IToastService;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/4
 * @desc
 * @version: 3.1.2
 */

public class AIDLService extends Service {
  private static final String TAG = AIDLService.class.getSimpleName();

  @Override public void onCreate() {
    super.onCreate();

    Log.d(TAG, "onCreate");
  }

  @Override public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "onDestroy");
  }

  private IToastService.Stub mStub = new IToastService.Stub() {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble,
        String aString) throws RemoteException {

    }

    @Override public void showToast(final String text) throws RemoteException {
      Log.d(TAG, "showToast " + text);

      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override public void run() {
          Toast.makeText(getBaseContext(), "显示来自remote : " + text, Toast.LENGTH_SHORT).show();
        }
      });

    }
  };

  @Nullable @Override public IBinder onBind(Intent intent) {
    return mStub;
  }
}
