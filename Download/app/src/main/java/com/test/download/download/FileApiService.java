package com.test.download.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileApiService {

    /**
     * 下载
     *
     * @return
     */
    @Streaming
    @GET
    Call<ResponseBody> loadFile(@Url String fileUrl);

    /**
     * 断点续传下载
     *
     * @return
     */
    @Streaming
    @GET
    Call<ResponseBody> loadFile(@Url String fileUrl, @Header("Range") String range);

}
