package com.pinery.aidl.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.pinery.aidl.IToastService;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/4
 * @desc
 * @version: 3.1.2
 */

public class Client {
  private static final String TAG = Client.class.getSimpleName();

  private Context mContext;
  private IToastService toastService;

  public Client(Context context){
    mContext = context;
  }

  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      Log.d(TAG, "onServiceConnected");
      toastService = IToastService.Stub.asInterface(service);
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      Log.d(TAG, "onServiceDisconnected");
      toastService = null;
    }
  };

  public void open(){
    Intent intent = new Intent();

    //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
    //intent.setPackage("com.pinery.aidl.remote");
    //intent.setAction("com.pinery.aidl.action.AIDL_ACTION");

    intent.setClassName("com.pinery.aidl.remote", "com.pinery.aidl.remote.AIDLService");

    mContext.startService(intent);
  }

  public void close(){
    Intent intent = new Intent();

    //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
    //intent.setPackage("com.pinery.aidl.remote");
    //intent.setAction("com.pinery.aidl.action.AIDL_ACTION");

    intent.setClassName("com.pinery.aidl.remote", "com.pinery.aidl.remote.AIDLService");

    mContext.stopService(intent);
  }

  public void connect(){
    Intent intent = new Intent();

    //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
    //intent.setPackage("com.pinery.aidl.remote");
    //intent.setAction("com.pinery.aidl.action.AIDL_ACTION");

    intent.setClassName("com.pinery.aidl.remote", "com.pinery.aidl.remote.AIDLService");

    mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
  }

  public void disconnect(){
    mContext.unbindService(mServiceConnection);
  }

  public void showToast(String text) throws RemoteException {
    if(toastService != null){
      toastService.showToast(text);
    }
  }

}
