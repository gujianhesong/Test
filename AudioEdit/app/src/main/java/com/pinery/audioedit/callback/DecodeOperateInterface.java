package com.pinery.audioedit.callback;

/**
 * 音频解码监听器
 */
public interface DecodeOperateInterface {
    public void updateDecodeProgress(int decodeProgress);

    public void decodeSuccess();

    public void decodeFail();
}
