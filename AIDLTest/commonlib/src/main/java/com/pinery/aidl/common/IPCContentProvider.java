package com.pinery.aidl.common;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/4/8
 * @desc
 * @version: 3.1.2
 */

public class IPCContentProvider extends ContentProvider {
  private final String TAG = this.getClass().getSimpleName();
  private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  public static final String AUTHORITY = "com.pinery.aidl_remote.provider.IPCContentProvider";
  public static final String CONTENT_NAME = "service_state";
  public static final Uri SERVICE_STATUS_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_NAME);


  private SQLiteDatabase mDatabase;
  private Context mContext;
  private String mTable;

  private static final int TABLE_CODE_PERSON = 2;

  static {
    //关联不同的 URI 和 code，便于后续 getType
    mUriMatcher.addURI(AUTHORITY, CONTENT_NAME, TABLE_CODE_PERSON);
  }

  @Override public boolean onCreate() {
    initProvider();
    return false;
  }

  /**
   * 初始化时清楚旧数据，插入一条数据
   */
  private void initProvider() {
    mTable = DbOpenHelper.TABLE_NAME;
    mContext = getContext();
    mDatabase = new DbOpenHelper(mContext).getWritableDatabase();

    new Thread(new Runnable() {
      @Override public void run() {
        mDatabase.execSQL("delete from " + mTable);
      }
    }).start();
  }

  @Nullable @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection,
      final String[] selectionArgs, final String sortOrder) {
    String tableName = getTableName(uri);
    showLog(tableName + " 查询数据");
    return mDatabase.query(tableName, projection, selection, selectionArgs, null, sortOrder, null);
  }

  @Nullable @Override public Uri insert(final Uri uri, final ContentValues values) {
    String tableName = getTableName(uri);
    showLog(tableName + " 插入数据");
    mDatabase.insert(tableName, null, values);
    mContext.getContentResolver().notifyChange(uri, null);
    return null;
  }

  @Override public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
    String tableName = getTableName(uri);
    showLog(tableName + " 删除数据");
    int deleteCount = mDatabase.delete(tableName, selection, selectionArgs);
    if (deleteCount > 0) {
      mContext.getContentResolver().notifyChange(uri, null);
    }
    return deleteCount;
  }

  @Override public int update(final Uri uri, final ContentValues values, final String selection,
      final String[] selectionArgs) {
    String tableName = getTableName(uri);
    showLog(tableName + " 更新数据");
    int updateCount = mDatabase.update(tableName, values, selection, selectionArgs);
    if (updateCount > 0) {
      mContext.getContentResolver().notifyChange(uri, null);
    }
    return updateCount;
  }

  /**
   * CRUD 的参数是 Uri，根据 Uri 获取对应的表名
   */
  private String getTableName(final Uri uri) {
    String tableName = "";
    int match = mUriMatcher.match(uri);
    switch (match) {
      case TABLE_CODE_PERSON:
        tableName = DbOpenHelper.TABLE_NAME;
    }
    showLog("UriMatcher " + uri.toString() + ", result: " + match);
    return tableName;
  }

  @Nullable @Override public String getType(final Uri uri) {
    return null;
  }

  private void showLog(final String s) {
    Log.d(TAG, s + "***** @ " + Thread.currentThread().getName());
  }
}
