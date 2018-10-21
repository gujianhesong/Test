package com.test.download.download;

import android.text.TextUtils;

import com.test.download.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class DownloadCallback implements Callback<ResponseBody>, OnDownloadCallback {

    private String destFileDir;
    private String destFileName;
    private String url;
    private Call call;

    //进度信息，存在文件头部
    private byte[] progressBuffer = new byte[DownloadApi.BUFFER_SIZE];
    private int progress;

    public DownloadCallback(String url) {
        this.url = url;
        this.destFileDir = DownloadApi.DOWNLOAD_DIR;
    }

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

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        onEventFailure(t);
    }

    /**
     * 保存
     *
     * @param response
     */
    public void saveFile(Response<ResponseBody> response) {
        destFileName = requestFileName(response);

        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File file = new File(dir, this.destFileName);
        LogUtil.i("file size： " + file.length() + ", " + file.getAbsolutePath());

        //通知获取下载文件的路径
        onEventGetFilePath(file.getAbsolutePath());

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
                    //通知进度
                    onEventProgress(downloadedSize, totalSize);
                    this.progress = progress;
                }

                lastDownloadSize = downloadedSize;
            }

            DownloadInfoUtil.convertFileToRealFile(file, progressBuffer);

            //通知成功
            onEventSuccess(file);

        } catch (final Exception ex) {
            onEventFailure(ex);
        } finally {
            try {
                if (is != null) is.close();
            } catch (final IOException ex) {
                onEventFailure(ex);
            }
            try {
                if (fos != null) fos.close();
            } catch (final IOException ex) {
                onEventFailure(ex);
            }
        }
    }

    /**
     * 获取文件名
     *
     * @param response
     * @return
     */
    private String requestFileName(Response<ResponseBody> response) {
        String destFileName = "";
        String content = response.headers().get("Content-Disposition");
        if (!TextUtils.isEmpty(content)) {
            String startStr = "filename=\"";
            int startIndex = content.indexOf(startStr);
            if (startIndex >= 0) {
                int endIndex = content.indexOf("\"", startIndex + startStr.length());
                if (endIndex > startIndex) {
                    destFileName = content.substring(startIndex + startStr.length(), endIndex);
                }
            }
        }

        if (TextUtils.isEmpty(destFileName)) {
            destFileName = url.substring(url.lastIndexOf("/") + 1);
        }

        return destFileName;
    }


}