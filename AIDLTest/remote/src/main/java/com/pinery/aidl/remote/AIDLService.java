package com.pinery.aidl.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.pinery.aidl.IToastService;
import com.pinery.aidl.common.RemoteServiceUtil;
import java.lang.ref.WeakReference;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/4
 * @desc
 * @version: 3.1.2
 */

public class AIDLService extends Service {
  private static final String TAG = AIDLService.class.getSimpleName();

  //这种方式创建的binder对象是不行的
  //private IToastService.Stub mStub = new MyBinder(getApplication());

  @Override public void onCreate() {
    super.onCreate();

    Log.d(TAG, "onCreate");
    RemoteServiceUtil.setServiceRunning(this, true);
  }

  @Override public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "onDestroy");
    RemoteServiceUtil.setServiceRunning(this, false);
  }

  @Override public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind");

    return super.onUnbind(intent);
  }

  @Override public void onRebind(Intent intent) {
    Log.d(TAG, "onRebind");

    super.onRebind(intent);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    //不能返回已经创建好的对象，要实时返回新创建的
    //return mStub;
    return new MyBinder(this);
  }

  private static class MyBinder extends IToastService.Stub{
    //弱引用，防止MyBinder对象持有Service引用，导致Service对象内存泄漏
    private WeakReference<Context> context;
    //appContext是Appliction级的
    private Context appContext;

    public MyBinder(Context context){
      this.context = new WeakReference<Context>(context);
      appContext = context.getApplicationContext();
    }

    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble,
        String aString) throws RemoteException {

    }

    @Override public void showToast(final String text) throws RemoteException {
      Log.d(TAG, "showToast " + text);

      if(context != null && context.get() != null){
        Log.d(TAG, "showToast context  " + context.get());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override public void run() {
            if(appContext != null){
              Toast.makeText(appContext, "显示来自remote : " + text, Toast.LENGTH_SHORT).show();
            }
          }
        });

      }else{
        Log.d(TAG, "showToast context  null");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override public void run() {
            if(appContext != null){
              Toast.makeText(appContext, "Service已关闭，显示来自remote : " + text, Toast.LENGTH_SHORT).show();
            }
          }
        });

      }

    }
  }
}
