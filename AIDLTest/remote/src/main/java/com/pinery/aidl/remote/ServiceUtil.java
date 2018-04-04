package com.pinery.aidl.remote;

import android.app.ActivityManager;
import android.content.Context;
import java.util.List;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/4
 * @desc
 * @version: 3.1.2
 */

public class ServiceUtil {

  /**
   * 判断某个服务是否正在运行的方法
   *
   * @param mContext
   * @param serviceName
   *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
   * @return true代表正在运行，false代表服务没有正在运行
   */
  @Deprecated
  public static boolean isServiceWork(Context mContext, String serviceName) {
    boolean isWork = false;
    ActivityManager myAM = (ActivityManager) mContext
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
    if (myList.size() <= 0) {
      return false;
    }
    for (int i = 0; i < myList.size(); i++) {
      String mName = myList.get(i).service.getClassName().toString();
      if (mName.equals(serviceName)) {
        isWork = true;
        break;
      }
    }
    return isWork;
  }

  /**
   * 判断某个进程是否正在运行的方法
   *
   * @param mContext
   * @param processName
   * @return true代表正在运行，false代表服务没有正在运行
   */
  public static boolean isProcessRunning(Context mContext, String processName) {
    boolean isWork = false;
    ActivityManager myAM = (ActivityManager) mContext
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> myList = myAM.getRunningAppProcesses();
    if (myList.size() <= 0) {
      return false;
    }
    for (int i = 0; i < myList.size(); i++) {
      String mName = myList.get(i).processName;
      if (mName.equals(processName)) {
        isWork = true;
        break;
      }
    }
    return isWork;
  }

}
