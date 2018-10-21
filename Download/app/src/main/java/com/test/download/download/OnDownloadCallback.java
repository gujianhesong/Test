package com.test.download.download;

import java.io.File;

/**
 * Created by gujian on 2018-10-21.
 */

public interface OnDownloadCallback {

    /**
     * 下载开始事件
     */
    void onEventStart();

    /**
     * 获取到下载文件路径事件
     *
     * @param filePath
     */
    void onEventGetFilePath(String filePath);

    /**
     * 下载进度事件
     *
     * @param progress
     * @param total
     */
    void onEventProgress(long progress, long total);

    /**
     * 下载完成事件
     *
     * @param file
     */
    void onEventSuccess(File file);

    /**
     * 下载完成事件
     *
     * @param throwable
     */
    void onEventFailure(Throwable throwable);

}
