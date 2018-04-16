package com.pinery.aidl.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.pinery.aidl.IToastService;
import com.pinery.aidl.common.RemoteServiceUtil;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/4
 * @desc
 * @version: 3.1.2
 */

public class Client {
  private static final String TAG = Client.class.getSimpleName();

  private static final String SERVICE_PKG_NAME = "com.pinery.aidl.remote";
  private static final String SERVICE_NAME = "com.pinery.aidl.remote.AIDLService";
  private static final String SERVICE_ACTION_NAME = "com.pinery.aidl.action.AIDL_ACTION";

  private Context mContext;
  private IToastService toastService;

  private boolean isConnect;

  public Client(Context context){
    mContext = context;
  }

  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      Log.d(TAG, "onServiceConnected");
      toastService = IToastService.Stub.asInterface(service);
      isConnect = true;
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
      //intent.setPackage(SERVICE_PKG_NAME);
      //intent.setAction(SERVICE_ACTION_NAME);

      intent.setClassName(SERVICE_PKG_NAME, SERVICE_NAME);

      mContext.startService(intent);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void close(){
    try {
      Intent intent = new Intent();

      //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
      //intent.setPackage(SERVICE_PKG_NAME);
      //intent.setAction(SERVICE_ACTION_NAME);

      intent.setClassName(SERVICE_PKG_NAME, SERVICE_NAME);

      mContext.stopService(intent);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void connect(){
    try {
      Intent intent = new Intent();

      //跨应用启动和绑定服务，5.0版本以上不能设置隐式Intent来绑定服务了
      //intent.setPackage(SERVICE_PKG_NAME);
      //intent.setAction(SERVICE_ACTION_NAME);

      intent.setClassName(SERVICE_PKG_NAME, SERVICE_NAME);

      mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

      isConnect = true;
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void disconnect(){
    try {
      mContext.unbindService(mServiceConnection);
      isConnect = false;
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public boolean isServiceRunning(){
    //这是判断进程是否正在运行，判断服务是否正在运行的方法已经不可用了，所以暂时只能用变量判断了
    //return ServiceUtil.isProcessRunning(mContext, "com.pinery.aidl.remote_service");
    //return isConnect;
    boolean isRunning = RemoteServiceUtil.isServiceRunning(mContext);
    return isRunning;
  }

  public boolean isServiceConnected(){
    return isConnect;
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
