package com.pinery.audioedit.util;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import com.pinery.audioedit.App;
import com.pinery.audioedit.common.Constant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

  public static final String SAVE_FOLDER = "/" + Constant.NAME;
  public static final String SAVE_AUDIO_FOLDER = "/audio";
  public static final String SAVE_LOG_FOLDER = "/log";

  /**
   * sd卡的根目录
   */
  public static String sSdRootPath = Environment.getExternalStorageDirectory().getPath();
  /**
   * 手机的缓存根目录
   */
  public static String sDataRootPath = App.getInstance().getCacheDir().getPath();

  /**
   * 获取存储的根目录
   */
  public static String getRootDirectory() {

    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? sSdRootPath
        + SAVE_FOLDER : sDataRootPath + SAVE_FOLDER;
  }

  /**
   * 获取缓存存储的根目录
   */
  public static String getCacheRootDirectory() {

    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return App.getInstance().getExternalCacheDir().getAbsolutePath();
    } else {
      return App.getInstance().getCacheDir().getAbsolutePath();
    }
  }

  /**
   * 获取储存日志的目录
   */
  public static String getLogStorageDirectory() {

    return getRootDirectory() + SAVE_LOG_FOLDER;
  }

  /**
   * 获取储存录音的目录
   */
  public static String getAudioStorageDirectory() {

    return getRootDirectory() + SAVE_AUDIO_FOLDER;
  }

  /**
   * 获取音频编辑文件夹的目录
   */
  public static String getAudioEditStorageDirectory() {

    return getRootDirectory() + SAVE_AUDIO_FOLDER;
  }

  /**
   * 外置SD是否可用
   */
  public static boolean isExternalStrorageExsist() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  /**
   * 获取生成时间
   */
  public static String getFileBuildTime(File file) {
    Date date = new Date(file.lastModified());//最后更新的时间
    String t;
    SimpleDateFormat sy2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置年月日时分秒
    t = sy2.format(date);
    return t;
  }

  /**
   * 获取时间长度
   */
  public static String getFilePlayTimeString(Context context, File file) {
    Date date;
    SimpleDateFormat sy1;
    String dateFormat = "error";

    int duration = getFilePlayTime(context, file);

    sy1 = new SimpleDateFormat("HH:mm:ss");//设置为时分秒的格式
    try {

      //使用Date格式化播放时间mediaPlayer.getDuration()
      if(duration < 60 * 60 * 1000){
        sy1 = new SimpleDateFormat("mm:ss");//设置为时分秒的格式
        date = sy1.parse("00:00");
      }else{
        date = sy1.parse("00:00:00");
      }

      date.setTime(duration + date.getTime());//用消除date.getTime()时区差
      dateFormat = sy1.format(date);

    }catch (Exception ex){
      ex.printStackTrace();
    }

    return dateFormat;
  }

  /**
   * 获取时间长度
   */
  public static int getFilePlayTime(Context context, File file) {
    try {
      MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(file.toString()));
      //使用Date格式化播放时间mediaPlayer.getDuration()
      int duration = mediaPlayer.getDuration();

      mediaPlayer.release();

      return duration;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * 删除文件
   */
  public static void deleteFile(String filePath) {
    if (TextUtils.isEmpty(filePath)) return;
    deleteFile(new File(filePath));
  }

  /**
   * 确保目录存在,没有则创建
   */
  public static boolean confirmFolderExist(String folderPath) {

    File file = new File(folderPath);
    if (!file.exists()) {
      return file.mkdirs();
    }

    return false;
  }

  /**
   * 复制单个文件
   *
   * @param srcPath String 原文件路径
   * @param destPath String 复制后路径
   * @return boolean
   */
  public static void copyFile(String srcPath, String destPath) {
    FileInputStream fis = null;
    FileOutputStream fos = null;

    try {

      if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(destPath) || TextUtils.equals(srcPath,
          destPath)) {
        return;
      }

      LogUtil.i("-----------" + new File(destPath).exists());

      File destFile = new File(destPath);
      if (!destFile.getParentFile().exists()) {
        LogUtil.i("-----------" + new File(destPath).exists());
        destFile.getParentFile().mkdirs();
      }

      new File(destPath).delete();
      String tempPath = srcPath + ".temp";

      File oldfile = new File(srcPath);
      if (oldfile.exists()) { //文件存在时
        fis = new FileInputStream(srcPath); //读入原文件
        fos = new FileOutputStream(tempPath);
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = fis.read(buffer)) != -1) {
          fos.write(buffer, 0, length);
        }
      }

      fos.close();
      fis.close();

      LogUtil.i("-----------" + new File(tempPath).renameTo(new File(destPath)));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 复制单个文件
   *
   * @param srcPath String 原文件路径
   * @param destPath String 复制后路径
   * @return boolean
   */
  public static void copyFile2(String srcPath, String destPath) {
    FileInputStream fis = null;
    FileOutputStream fos = null;

    try {

      if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(destPath) || TextUtils.equals(srcPath,
          destPath)) {
        return;
      }

      LogUtil.i("-----------" + new File(destPath).exists());

      File destFile = new File(destPath);
      if (!destFile.getParentFile().exists()) {
        destFile.getParentFile().mkdirs();
      }

      if (destFile.exists()) {
        destFile.delete();
      }

      fis = new FileInputStream(srcPath); //读入原文件
      fos = new FileOutputStream(destPath);
      byte[] buffer = new byte[1024];
      int length = 0;
      while ((length = fis.read(buffer)) != -1) {
        fos.write(buffer, 0, length);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        fos.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      try {
        fis.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * 删除文件夹所有内容
   */
  public static void deleteFile(File file) {
    if (file != null && file.exists()) { // 判断文件是否存在
      if (file.isDirectory()) { // 否则如果它是一个目录
        File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
        if (files != null) {
          for (File childFile : files) { // 遍历目录下所有的文件
            deleteFile(childFile); // 把每个文件 用这个方法进行迭代
          }
        }
      }

      //安全删除文件
      deleteFileSafely(file);
    }
  }

  /**
   * 重命名
   */
  public static File renameFile(File srcFile, String newName) {

    File destFile = new File(newName);
    srcFile.renameTo(destFile);

    return destFile;
  }

  /**
   * 删除所以编辑文件和文件夹
   */
  public static void deleteEditFiles() {
    try {
      File file = new File(getAudioEditStorageDirectory());
      File[] childs = file.listFiles();
      if (childs != null) {
        for (File child : childs) {
          if (child.isDirectory()) {
            deleteFile(child);
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static String createNewFileName(File srcFile) {

    final String srcName = srcFile.getName();
    String newName = srcName;
    int index = 1;
    while (true) {
      newName = srcName.substring(0, srcName.lastIndexOf(".")) + "_" + index;
      File file = new File(getAudioStorageDirectory(), newName + ".wav");
      if (!file.exists()) {

        break;
      }

      index++;
    }

    return newName;
  }

  public static String createNewFileName(String fileName) {

    String newName = fileName;
    int index = 1;
    while (true) {
      newName = fileName + "_" + index;
      File file = new File(getAudioStorageDirectory(), newName + ".wav");
      if (!file.exists()) {
        break;
      }

      index++;
    }

    return newName;
  }

  public static boolean checkFileExist(String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      return false;
    }
    return new File(filePath).exists();
  }

  public static File checkFileExistFile(long time) {

    File audioFolder = new File(FileUtils.getAudioStorageDirectory());
    File[] files = audioFolder.listFiles();
    if (files != null) {
      for (File file : files) {
        LogUtil.i("-------------" + file.lastModified() + "," + time);
        if (file.lastModified() == time) {
          return file;
        }
      }
    }

    return null;
  }

  /**
   * 安全删除文件.防止删除后重新创建文件，报错 open failed: EBUSY (Device or resource busy)
   */
  public static boolean deleteFileSafely(File file) {
    if (file != null) {
      String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
      File tmp = new File(tmpPath);
      file.renameTo(tmp);
      return tmp.delete();
    }
    return false;
  }

  public static String queryFilePath(Context context, Uri uri) {

    if ("content".equalsIgnoreCase(uri.getScheme())) {
      String[] projection = { "_data" };
      Cursor cursor = null;

      try {
        cursor = context.getContentResolver().query(uri, projection,null, null, null);
        int column_index = cursor.getColumnIndexOrThrow("_data");
        if (cursor.moveToFirst()) {
          return cursor.getString(column_index);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    else if ("file".equalsIgnoreCase(uri.getScheme())) {
      return uri.getPath();
    }

    return null;
  }

}
