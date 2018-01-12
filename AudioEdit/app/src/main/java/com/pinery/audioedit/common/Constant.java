package com.pinery.audioedit.common;

import com.pinery.audioedit.BuildConfig;

public class Constant {
    public static final boolean Debug = BuildConfig.DEBUG;
    public static final String NAME = "AudioEdit";

    public static final int ExportChannelNumber = 2;  // 输出声道为双声道
    public static final int ExportByteNumber = 2; //输出采样精度字节数
    public static final int ExportSampleRate = 44100; //输出采样率

    public static final int OneSecond = 1000;

    public static final int NormalMaxProgress = 100;

    public static boolean isBigEnding = false;

    public static String SUFFIX_WAV = ".wav";
    public static String SUFFIX_PCM = ".pcm";



}