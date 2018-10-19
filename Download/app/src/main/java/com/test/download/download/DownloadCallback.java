package com.test.download.download;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.test.download.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class DownloadCallback implements Callback<ResponseBody> {

    private CompositeDisposable rxSubscriptions = new CompositeDisposable();
    private String destFileDir;
    private String destFileName;
    private String url;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Call call;
    //进度信息，存在文件头部
    private byte[] progressBuffer = new byte[DownloadApi.BUFFER_SIZE];
    private int progress;

    public DownloadCallback(String url, String destFileDir) {
        this.url = url;
        this.destFileDir = destFileDir;
        subscribeLoadProgress();
    }

    public abstract void onStarted();

    public abstract void onSuccess(File file);

    public abstract void progress(String filePath, long progress, long total);

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        this.call = call;
        Flowable.just(response).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Response<ResponseBody>>() {
                    @Override
                    public void accept(Response<ResponseBody> responseBodyResponse) throws Exception {

                        saveFile(responseBodyResponse);
                    }
                });
    }

    /**
     * 保存
     *
     * @param response
     */
    public void saveFile(Response<ResponseBody> response) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onStarted();
            }
        });

        String content = response.headers().get("Content-Disposition");
        if(!TextUtils.isEmpty(content)){
            String startStr = "filename=\"";
            int startIndex = content.indexOf(startStr);
            if(startIndex >= 0){
                int endIndex = content.indexOf("\"", startIndex + startStr.length());
                if(endIndex > startIndex){
                    this.destFileName  = content.substring(startIndex + startStr.length(), endIndex);
                }
            }
        }

        if(TextUtils.isEmpty(this.destFileName)){
            String tempStr = url.substring(url.lastIndexOf("/") + 1);
            this.destFileName = tempStr;
        }

        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File file = new File(dir, this.destFileName);
        LogUtil.i("file size： " + file.length() + ", " + file.getAbsolutePath());

        long downloadedSize = 0;
        long totalSize = response.body().contentLength();

        //获取下载信息
        DownloadInfo downloadInfo = DownloadInfoUtil.readDownloadInfo(file);
        if (downloadInfo == null) {
            downloadInfo = new DownloadInfo();
            downloadInfo.setUrl(url);
            downloadInfo.setName(destFileName);
            downloadInfo.setTotalSize(totalSize);
        } else {
            totalSize = downloadInfo.getTotalSize();
            downloadedSize = downloadInfo.getDownloadSize();
        }

        LogUtil.i("file size： " + file.length() + ", " + downloadedSize + ", " + file.getAbsolutePath());

        InputStream is = null;
        byte[] buf = new byte[4096];
        int len;
        RandomAccessFile fos = null;
        try {
            is = response.body().byteStream();
            fos = new RandomAccessFile(file, "rw");
            //最近下载大小
            long lastDownloadSize = downloadedSize;
            while ((len = is.read(buf)) != -1) {

                //移动到文件尾部，写入下载的数据
                fos.seek(lastDownloadSize + progressBuffer.length);
                fos.write(buf, 0, len);

                //保存当前的下载进度信息
                downloadedSize += len;
                downloadInfo.setDownloadSize(downloadedSize);
                DownloadInfoUtil.saveDownloadInfo(fos, downloadInfo, progressBuffer);

                //LogUtil.i("bytes size : " + fos.length());

                //通知进度变化
                int progress = (int) (downloadedSize * 1.0 / totalSize * 100);
                if (this.progress < progress) {
                    RxBus.getDefault().post(new FileLoadEvent(downloadInfo.getUrl(), file.getAbsolutePath(), totalSize, downloadedSize));
                    this.progress = progress;
                }

                lastDownloadSize = downloadedSize;
            }

            DownloadInfoUtil.convertFileToRealFile(file, progressBuffer);

            unsubscribe();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(file);
                }
            });

        } catch (final Exception ex) {
            unsubscribe();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onFailure(call, ex);
                }
            });
        } finally {
            try {
                if (is != null) is.close();
            } catch (final IOException ex) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(call, ex);
                    }
                });
            }
            try {
                if (fos != null) fos.close();
            } catch (final IOException ex) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onFailure(call, ex);
                    }
                });
            }
        }
    }

    /**
     * 订阅文件下载进度
     */
    private void subscribeLoadProgress() {
        rxSubscriptions.add(RxBus.getDefault()
                .toObservable(FileLoadEvent.class)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FileLoadEvent>() {
                    @Override
                    public void accept(FileLoadEvent fileLoadEvent) throws Exception {
                        if(TextUtils.equals(fileLoadEvent.getUrl(), url)){
                            progress(fileLoadEvent.getFilePath(), fileLoadEvent.getProgress(), fileLoadEvent.getTotal());
                        }
                    }
                }));
    }

    /**
     * 取消订阅，防止内存泄漏
     */
    private void unsubscribe() {
        if (!rxSubscriptions.isDisposed()) {
            rxSubscriptions.dispose();
        }
    }

}