package com.pinery.audioedit.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * 执行后台任务的服务
 */
public class AudioTaskService extends IntentService {

  private AudioTaskHandler mTaskHandler;

  public AudioTaskService() {
    super("AudioTaskService");
  }

  @Override public void onCreate() {
    super.onCreate();

    mTaskHandler = new AudioTaskHandler();
  }

  /**
   * 实现异步任务的方法
   *
   * @param intent Activity传递过来的Intent,数据封装在intent中
   */
  @Override protected void onHandleIntent(Intent intent) {

    if (mTaskHandler != null) {
      mTaskHandler.handleIntent(intent);
    }
  }
}