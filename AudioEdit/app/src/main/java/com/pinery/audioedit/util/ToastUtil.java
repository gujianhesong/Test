package com.pinery.audioedit.util;

import android.widget.Toast;
import com.pinery.audioedit.App;

public class ToastUtil {

  /**
   * 系统Toast
   */
  public static void showToast(final String text) {
    App.getInstance().mHandler.post(new Runnable() {
      @Override public void run() {
        Toast.makeText(App.getInstance(), text, Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * 系统Toast
   */
  public static void showToast(final int textId) {
    App.getInstance().mHandler.post(new Runnable() {
      @Override public void run() {
        Toast.makeText(App.getInstance(), App.getInstance().getString(textId), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }
}
