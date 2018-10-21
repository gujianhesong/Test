package com.test.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.test.download.download.DownloadApi;
import com.test.download.download.DownloadCallback;
import com.test.download.download.DownloadEvent;
import com.test.download.download.DownloadManager;
import com.test.download.util.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hesong-os on 2018/10/19.
 */

public class DownloadService extends Service {
    private HashMap<String, Boolean> cancelMap = new HashMap<>();
    private HashMap<String, DownloadApi> downloadApiMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.i();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        String url = intent.getStringExtra("url");
        final int state = intent.getIntExtra("state", 0);
        LogUtil.i("onStartCommand : " + state + ", " + url);
        Observable.just(url).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(final String url) throws Exception {
                        if (state == 1) {
                            //下载
                            download(url);
                        } else if (state == 2) {
                            //取消下载
                            cancel(url);
                        }
                    }
                });

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 下载
     *
     * @param url
     */
    private void download(final String url) {
        DownloadCallback callback = new DownloadCallback(url) {
            String filePath;

            @Override
            public void onEventStart() {
                LogUtil.e("下载开始，当前时间：" + new Date() + ", " + url);
                DownloadEvent event = new DownloadEvent(DownloadEvent.State.START, url);
                EventBus.getDefault().post(event);

                DownloadManager.updateDownloadingState(url, true);
            }

            @Override
            public void onEventGetFilePath(String filePath) {
                LogUtil.e("获取下载路径：" + filePath + ", " + url);
                this.filePath = filePath;
            }

            @Override
            public void onEventProgress(long downloaded, long total) {
                int progress = (int) (100f * downloaded / total);
                LogUtil.e("下载进度：" + downloaded + ", " + total + ", " + progress + ", " + url);
                DownloadEvent event = new DownloadEvent(DownloadEvent.State.PROGRESS, url);
                event.setProgress(progress);
                event.setLocalPath(filePath);
                event.setDownloadedSize(downloaded);
                event.setTotalSize(total);
                EventBus.getDefault().post(event);
            }

            @Override
            public void onEventSuccess(File file) {
                LogUtil.e("下载完成，当前时间：" + new Date() + ", " + url);
                DownloadEvent event = new DownloadEvent(DownloadEvent.State.SUCCESS, url);
                event.setLocalPath(filePath);
                EventBus.getDefault().post(event);

                downloadApiMap.remove(url);

                DownloadManager.updateDownloadingState(url, false);
            }

            @Override
            public void onEventFailure(Throwable t) {
                Boolean obj = cancelMap.remove(url);
                if (obj != null && obj.booleanValue()) {
                    LogUtil.e("下载取消，当前时间：" + new Date() + ", " + url);
                    //取消
                    DownloadEvent event = new DownloadEvent(DownloadEvent.State.CANCEL, url);
                    event.setLocalPath(filePath);
                    EventBus.getDefault().post(event);
                } else {
                    LogUtil.e("下载错误，当前时间：" + new Date() + ", " + url + ", " + t.getMessage());
                    //失败
                    DownloadEvent event = new DownloadEvent(DownloadEvent.State.FAIL, url);
                    event.setLocalPath(filePath);
                    event.setErrorMsg(t.getMessage());
                    EventBus.getDefault().post(event);
                }

                downloadApiMap.remove(url);

                DownloadManager.updateDownloadingState(url, false);
            }
        };

        DownloadApi downloadApi = new DownloadApi(url);
        downloadApiMap.put(url, downloadApi);
        downloadApi.loadFileByName(callback);

    }

    /**
     * 取消下载
     *
     * @param url
     */
    private void cancel(final String url) {
        cancelMap.put(url, true);
        DownloadApi downloadApi = downloadApiMap.remove(url);
        if (downloadApi != null) {
            downloadApi.cancel();
        }

    }
}
