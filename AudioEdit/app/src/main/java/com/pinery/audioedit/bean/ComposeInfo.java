package com.pinery.audioedit.bean;

/**
 * 音频合成信息
 *
 */
public class ComposeInfo {

    /**
     * 音频文件路径
     */
    public String audioPath;

    /**
     * 音频解码后的pcm文件路径
     */
    public String pcmPath;

    /**
     * 音频开始播放的时间
     */
    public float offsetSeconds;

    /**
     * 参与合成的权重大小
     */
    public float weight;


}
