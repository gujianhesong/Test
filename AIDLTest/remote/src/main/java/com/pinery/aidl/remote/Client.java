package com.pinery.aidl.remote;

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

  private final static String PKG_NAME = "com.pinery.aidl.remote";
  private final static String SERVICE_NAME = AIDLService.class.getName();
  private final static String ACTION = "com.pinery.aidl.action.AIDL_ACTION";

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
      //该方法会在Service意外停止的时候执行，正常关闭Service不会执行。

      Log.d(TAG, "onServiceDisconnected");
      toastService = null;
      isConnect = false;
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
      isConnect = false;
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

    //然后这里有点奇怪，在调用了断连和关闭Service之后，Service服务已经关闭了，但是该方法调用还是有效，不清楚为什么？
    //猜想应该是，虽然Service对象销毁了，但是Binder对象并没有销毁，Binder对象不是，也不能作为Service的属性，它是返回给系统底层使用的，系统底层仍然可用。

    if(toastService != null){
      toastService.showToast(text);
    }
  }

}
