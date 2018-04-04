package com.pinery.aidl.remote;

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

  private final static String PKG_NAME = "com.pinery.aidl.remote";
  private final static String SERVICE_NAME = AIDLService.class.getName();
  private final static String ACTION = "com.pinery.aidl.action.AIDL_ACTION";

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
    try {
      Intent intent = new Intent();

      //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
      //intent.setPackage(PKG_NAME);
      //intent.setAction(ACTION);

      intent.setClassName(PKG_NAME, SERVICE_NAME);

      mContext.startService(intent);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void close(){
    try {
      Intent intent = new Intent();

      //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
      //intent.setPackage(PKG_NAME);
      //intent.setAction(ACTION);

      intent.setClassName(PKG_NAME, SERVICE_NAME);

      mContext.stopService(intent);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void connect(){
    try {
      Intent intent = new Intent();

      //应用内启动绑定服务，可以显示和隐式启动

      // 1.隐式启动
      intent.setPackage(PKG_NAME);
      intent.setAction(ACTION);

      // 2.显式启动
      //intent.setClassName(PKG_NAME, SERVICE_NAME);

      mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void disconnect(){
    try {
      mContext.unbindService(mServiceConnection);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public boolean isServiceRunning(){
    return ServiceUtil.isProcessRunning(mContext, "com.pinery.aidl.remote_service");
  }

  public boolean isServiceConnected(){
    return toastService != null;
  }

  public void toogleOpen(){
    if(isServiceRunning()){
      disconnect();
      close();
    }else{
      open();
    }
  }

  public void showToast(String text) throws RemoteException {
    if(toastService != null){
      toastService.showToast(text);
    }
  }

}
