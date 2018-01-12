package com.pinery.audioedit.util;

import android.os.Environment;
import android.text.TextUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileFunction {
  public static boolean isExitsSdcard() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  public static boolean isFileExists(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }

    return new File(path).exists();
  }

  private static void createDirectory(String path) {
    File dir = new File(path);

    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  public static void saveFile(String url, String content) {
    saveFile(url, content, true, false);
  }

  public static void saveFile(String url, String content, boolean cover, boolean append) {
    FileOutputStream out = null;
    File file = new File(url);

    try {
      if (file.exists()) {
        if (cover) {
          file.delete();
          file.createNewFile();
        }
      } else {
        file.createNewFile();
      }

      out = new FileOutputStream(file, append);
      out.write(content.getBytes());
      out.close();
      LogUtil.i("保存文件" + url + "保存文件成功");
    } catch (Exception e) {
      LogUtil.e("保存文件" + url, e);

      if (out != null) {
        try {
          out.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  public static void deleteFile(String path) {
    if (!TextUtils.isEmpty(path)) {
      File file = new File(path);

      if (file.exists()) {
        try {
          file.delete();
        } catch (Exception e) {
          LogUtil.e("删除本地文件失败", e);
        }
      }
    }
  }

  public static void copyFile(String oldPath, String newPath) {
    try {
      int byteRead;

      File oldFile = new File(oldPath);
      File newFile = new File(newPath);

      if (oldFile.exists()) { //文件存在时
        if (newFile.exists()) {
          newFile.delete();
        }

        newFile.createNewFile();

        FileInputStream inputStream = new FileInputStream(oldPath); //读入原文件
        FileOutputStream outputStream = new FileOutputStream(newPath);
        byte[] buffer = new byte[1024];

        while ((byteRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, byteRead);
        }

        inputStream.close();
      }
    } catch (Exception e) {
      LogUtil.e("复制单个文件操作出错", e);
    }
  }

  public static FileInputStream getFileInputStreamFromFile(String fileUrl) {
    FileInputStream fileInputStream = null;

    try {
      File file = new File(fileUrl);

      fileInputStream = new FileInputStream(file);
    } catch (Exception e) {
      LogUtil.e("GetBufferedInputStreamFromFile异常", e);
    }

    return fileInputStream;
  }

  public static FileOutputStream getFileOutputStreamFromFile(String fileUrl) {
    FileOutputStream bufferedOutputStream = null;

    try {
      File file = new File(fileUrl);

      if (file.exists()) {
        file.delete();
      }

      file.createNewFile();

      bufferedOutputStream = new FileOutputStream(file);
    } catch (Exception e) {
      LogUtil.e("GetFileOutputStreamFromFile异常", e);
    }

    return bufferedOutputStream;
  }

  public static BufferedOutputStream getBufferedOutputStreamFromFile(String fileUrl) {
    BufferedOutputStream bufferedOutputStream = null;

    try {
      File file = new File(fileUrl);

      if (file.exists()) {
        file.delete();
      }

      file.createNewFile();

      bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
    } catch (Exception e) {
      LogUtil.e("GetBufferedOutputStreamFromFile异常", e);
    }

    return bufferedOutputStream;
  }

  public static void renameFile(String oldPath, String newPath) {
    if (!TextUtils.isEmpty(oldPath) && !TextUtils.isEmpty(newPath)) {
      File newFile = new File(newPath);

      if (newFile.exists()) {
        newFile.delete();
      }

      File oldFile = new File(oldPath);

      if (oldFile.exists()) {
        try {
          oldFile.renameTo(new File(newPath));
        } catch (Exception e) {
          LogUtil.e("删除本地文件失败", e);
        }
      }
    }
  }
}
