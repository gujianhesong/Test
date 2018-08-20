package com.test.memory.api;

import android.os.Build;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
  private RetrofitClient() {
  }

  private static class Holder {
    private static RetrofitClient instance = new RetrofitClient();
  }

  public static RetrofitClient getInstance() {
    return Holder.instance;
  }

  private static Retrofit retrofit;

  public static synchronized Retrofit getRetrofit(String url) {
    if (retrofit != null) {
      return retrofit;
    }

    LoggingInterceptor httpLoggingInterceptor = new LoggingInterceptor();
    OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)
        .connectTimeout(500, TimeUnit.SECONDS)
        .readTimeout(500, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build();
    retrofit = new Retrofit.Builder().baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();

    return retrofit;
  }

  public <T> T getApiService(String url, Class<T> cl) {
    return getRetrofit(url).create(cl);
  }

  private static class LoggingInterceptor implements Interceptor {
    private static final String UA = "User-Agent";

    @Override public Response intercept(Chain chain) throws IOException {
      Request request = chain.request().newBuilder().addHeader(UA, makeUA()).build();
      return chain.proceed(request);
    }

    private String makeUA() {
      String s = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
      return Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
    }
  }
}

