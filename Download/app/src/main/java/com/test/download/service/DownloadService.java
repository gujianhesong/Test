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
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by hesong-os on 2018/10/19.
 */

public class DownloadService extends Service {
    private HashMap<String, Boolean> cancelMap = new HashMap<>();

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
                            excuetDownloadFile(url);
                        } else if (state == 2) {
                            //取消下载
                            cancelDownloadFile(url);
                        }
                    }
                });

        return super.onStartCommand(intent, flags, startId);
    }

    private void excuetDownloadFile(final String url) {
        String downloadDir = DownloadApi.DOWNLOAD_DIR;
        boolean isCancel;

        DownloadApi.getInstance(url).loadFileByName(new DownloadCallback(url, downloadDir) {

            @Override
            public void onStarted() {
                LogUtil.e("下载开始，当前时间：" + new Date() + ", " + url);
                DownloadEvent event = new DownloadEvent(DownloadEvent.State.START, url);
                EventBus.getDefault().post(event);

                DownloadManager.updateDownloadingState(url, true);
            }

            @Override
            public void progress(String filePath, long downloaded, long total) {
                int progress = (int) (100f * downloaded / total);
                LogUtil.e("下载进度：" + downloaded + ", " + total + ", " + progress + ", " + url);
                DownloadEvent event = new DownloadEvent(DownloadEvent.State.PROGRESS, url);
                event.setProgress(progress);
                event.setLocalPath(filePath);
                EventBus.getDefault().post(event);
            }

            @Override
            public void onSuccess(File file) {
                LogUtil.e("下载完成，当前时间：" + new Date() + ", " + url);
                DownloadEvent event = new DownloadEvent(DownloadEvent.State.SUCCESS, url);
                EventBus.getDefault().post(event);

                DownloadManager.updateDownloadingState(url, false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LogUtil.e("下载错误，当前时间：" + new Date() + ", " + url + ", " + t.getMessage());

                Boolean obj = cancelMap.remove(url);
                if(obj != null && obj.booleanValue()){
                    //取消
                    DownloadEvent event = new DownloadEvent(DownloadEvent.State.CANCEL, url);
                    EventBus.getDefault().post(event);
                }else{
                    //失败
                    DownloadEvent event = new DownloadEvent(DownloadEvent.State.FAIL, url);
                    event.setErrorMsg(t.getMessage());
                    EventBus.getDefault().post(event);
                }

                DownloadManager.updateDownloadingState(url, false);
            }
        });
    }

    private void cancelDownloadFile(final String url) {
        cancelMap.put(url, true);
        DownloadApi.getInstance(url).cancel();
    }
}
