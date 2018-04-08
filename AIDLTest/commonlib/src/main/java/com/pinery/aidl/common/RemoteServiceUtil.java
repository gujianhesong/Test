package com.pinery.aidl.common;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/8
 * @desc
 * @version: 3.1.2
 */

public class RemoteServiceUtil {

  private static final String SERVICE_NAME = "com.pinery.aidl.remote.AIDLService";

  public static boolean isServiceRunning(Context context){
    Uri uri = IPCContentProvider.SERVICE_STATUS_URI;

    ContentResolver contentResolver = context.getContentResolver();
    Cursor cursor = contentResolver.query(uri, null, "name = ?", new String[]{SERVICE_NAME}, null);
    if(cursor != null && cursor.moveToFirst()){
      String name = cursor.getString(cursor.getColumnIndex("name"));
      int status = cursor.getInt(cursor.getColumnIndex("status"));
      if(status == 1){
        return true;
      }
    }
    return false;
  }

  public static void setServiceRunning(Context context, boolean isRunning){
    Uri uri = IPCContentProvider.SERVICE_STATUS_URI;

    ContentResolver contentResolver = context.getContentResolver();
    Cursor cursor = contentResolver.query(uri, null, "name = ?", new String[]{SERVICE_NAME}, null);
    if(cursor != null && cursor.getCount() > 0){
      ContentValues values = new ContentValues();
      values.put("status", isRunning ? 1 : 0);
      String where = "name = ?";
      String[] args = new String[]{SERVICE_NAME};
      contentResolver.update(uri, values, where, args);
    }else{
      ContentValues values = new ContentValues();
      values.put("name", SERVICE_NAME);
      values.put("status", isRunning ? 1 : 0);
      contentResolver.insert(uri, values);
    }

  }

}
