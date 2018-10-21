package com.test.download.download;

import com.test.download.util.LogUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class DownloadApi {

    public static String DOWNLOAD_DIR = "/sdcard/DownloadFile/";
    public static int BUFFER_SIZE = 1024;
    private static final int DEFAULT_TIMEOUT = 20;

    private String url;
    private Retrofit retrofit;
    private FileApiService fileService;
    private Call<ResponseBody> call;

    public DownloadApi(String baseUrl) {
        this.url = baseUrl;
        int index = baseUrl.lastIndexOf("/");
        if (index >= 0) {
            baseUrl = baseUrl.substring(0, index) + "/";
        }
        retrofit = new Retrofit.Builder()
                .client(initOkHttpClient())
                .baseUrl(baseUrl)
                .build();
        fileService = retrofit.create(FileApiService.class);
    }

    /**
     * 下载文件
     *
     * @param callback
     */
    public void loadFileByName(DownloadCallback callback) {
        HashMap<String, DownloadInfo> downloadInfoMap = DownloadInfoUtil.getAllDownloadInfo();
        DownloadInfo downloadInfo = null;
        if (downloadInfoMap != null) {
            downloadInfo = downloadInfoMap.get(url);
        }
        if (downloadInfo != null) {
            LogUtil.i("开启续传下载： " + downloadInfo.getDownloadSize() + ", " + downloadInfo.getTotalSize() + ", "
                    + downloadInfo.getUrl() + ", " + downloadInfo.getName());
            String header = "bytes=" + downloadInfo.getDownloadSize() + "-";
            call = fileService.loadFile(url, header);
            call.enqueue(callback);
        } else {
            LogUtil.i("开启下载： " + url + ", ");
            call = fileService.loadFile(url);
            call.enqueue(callback);
        }

        callback.onEventStart();
    }

    /**
     * 取消下载
     */
    public void cancel() {
        if (call != null && call.isCanceled() == false) {
            call.cancel();
        }
    }

    /**
     * 初始化OkHttpClient
     *
     * @return
     */
    private OkHttpClient initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        builder.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse
                        .newBuilder()
                        .build();
            }
        });
        return builder.build();
    }

}
