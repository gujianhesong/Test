package com.test.download.download;

import android.content.Context;
import android.content.Intent;

import com.test.download.service.DownloadService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hesong-os on 2018/10/19.
 */

public class DownloadManager {
    private static ConcurrentHashMap<String, Boolean> sStateMap = new ConcurrentHashMap();

    public static void excueteDownload(Context context, String url){
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("state", 1);
        intent.putExtra("url", url);
        context.startService(intent);
    }

    public static void cancelDownload(Context context, String url){
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("state", 2);
        intent.putExtra("url", url);
        context.startService(intent);
    }

    public static boolean isDownloading(String url){
        Boolean bool = sStateMap.get(url);
        return bool != null ? bool.booleanValue() : false;
    }

    public static void updateDownloadingState(String url, boolean downloading){
        sStateMap.put(url, downloading);
    }

}
