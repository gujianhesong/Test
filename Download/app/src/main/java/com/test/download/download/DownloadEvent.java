package com.test.download.download;

public class DownloadEvent {
    public enum State{
        IDEL,
        START,
        PROGRESS,
        SUCCESS,
        FAIL,
        CANCEL
    }

    private State state;
    private int progress;
    private String localPath;
    private String errorMsg;
    private String url;
    private long downloadedSize;
    private long totalSize;

    public DownloadEvent(State state, String url) {
        setState(state);
        setUrl(url);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
