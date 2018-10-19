package com.test.download.download;

public class FileLoadEvent {
    /**
     * 下载地址
     */
    String url;
    /**
     * 下载路径
     */
    String path;
    /**
     * 文件大小
     */
    long total;
    /**
     * 已下载大小
     */
    long progress;

    public long getProgress() {
        return progress;
    }

    public long getTotal() {
        return total;
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return path;
    }

    public FileLoadEvent(String url, String path, long total, long progress) {
        this.url = url;
        this.path = path;
        this.total = total;
        this.progress = progress;
    }
}
