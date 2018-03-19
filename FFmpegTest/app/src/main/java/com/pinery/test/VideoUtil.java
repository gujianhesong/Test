package com.pinery.test;

/**
 * @author hesong
 * @e-mail hes1335@13322.com
 * @time 2018/2/7
 * @desc
 * @version: 3.1.2
 */

public class VideoUtil {

  static {
    System.loadLibrary("avcodec57");
    System.loadLibrary("avdevice57");
    System.loadLibrary("avfilter6");
    System.loadLibrary("avformat57");
    System.loadLibrary("avutil55");
    System.loadLibrary("postproc54");
    System.loadLibrary("swresample2");
    System.loadLibrary("swscale4");
    System.loadLibrary("zp_decode");
  }

  public native static int decodeAudio(String inputPath, String outputPath);
  public native static int decodeVideo(String inputPath, String outputPath);
  public native static int encodeAudio(String inputPath, String outputPath);
  public native static int encodeVideo(String inputPath, String outputPath);

}
