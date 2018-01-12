package com.pinery.audioedit.util;

import com.pinery.audioedit.bean.Audio;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 音频合成
 */

public class AudioMixUtil {

  //wave头文件大小
  private static final int WAVE_HEAD_SIZE = 44;


  /**
   * 裁剪音频
   * @param audio
   * @param cutStartTime
   * @param cutEndTime
   */
  public static void cutAudio(Audio audio, float cutStartTime, float cutEndTime){
    if(cutStartTime == 0 && cutEndTime == audio.getTimeMillis() / 1000f){
      return;
    }

    String srcWavePath = audio.getPath();
    int sampleRate = audio.getSampleRate();
    int channels = audio.getChannel();
    int byteNum = audio.getByteNum();
    RandomAccessFile srcFis = null;
    RandomAccessFile newFos = null;
    String tempOutPcmPath = srcWavePath + ".tempPcm";
    try {

      //创建输入流
      srcFis = new RandomAccessFile(srcWavePath, "rw");
      newFos = new RandomAccessFile(tempOutPcmPath, "rw");

      final int cutStartPos = getPositionFromWave(cutStartTime, channels, sampleRate);
      final int cutEndPos = getPositionFromWave(cutEndTime, channels, sampleRate);

      //复制源音频srcStartTime时间之前的数据
      //跳过头文件数据
      srcFis.seek(WAVE_HEAD_SIZE + cutStartPos);

      copyData(srcFis, newFos, cutEndPos - cutStartPos);

    } catch (Exception e) {
      e.printStackTrace();

      return;

    }finally {
      //关闭输入流
      if(srcFis != null){
        try {
          srcFis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(newFos != null){
        try {
          newFos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // 删除源文件,
    new File(srcWavePath).delete();
    // 转换临时文件为源文件
    AudioEncodeUtil.convertPcm2Wav(tempOutPcmPath, srcWavePath, sampleRate, channels, byteNum * 8);
    //删除临时文件
    new File(tempOutPcmPath).delete();

  }

  /**
   * 插入音频,针对声道数,采样率,采样位数都一样的wav音频
   * @param srcAudio 源音频路径
   * @param coverAudio 覆盖音频路径
   * @param outAudio
   * @param srcStartTime 源音频起始时间
   */
  public static void insertAudioWithSame(Audio srcAudio, Audio coverAudio, Audio outAudio, float srcStartTime){

    String srcWavePath = srcAudio.getPath();
    String coverWavePath = coverAudio.getPath();
    int sampleRate = srcAudio.getSampleRate();
    int channels = srcAudio.getChannel();
    int byteNum = srcAudio.getByteNum();
    RandomAccessFile srcFis = null;
    RandomAccessFile coverFis = null;
    RandomAccessFile newFos = null;
    String tempOutPcmPath = srcWavePath + ".tempPcm";
    try {

      //创建输入流
      srcFis = new RandomAccessFile(srcWavePath, "rw");
      coverFis = new RandomAccessFile(coverWavePath, "rw");
      newFos = new RandomAccessFile(tempOutPcmPath, "rw");

      final int srcStartPos = getPositionFromWave(srcStartTime, channels, sampleRate);
      final int coverStartPos = 0;
      final int coverEndPos = (int) coverFis.length() - WAVE_HEAD_SIZE;

      //复制源音频srcStartTime时间之前的数据
      //跳过头文件数据
      srcFis.seek(WAVE_HEAD_SIZE);

      copyData(srcFis, newFos, srcStartPos);

      //复制覆盖音频指定时间段的数据
      //跳过指定位置数据
      coverFis.seek(WAVE_HEAD_SIZE + coverStartPos);

      int copyCoverSize = coverEndPos - coverStartPos;
      float volume = coverAudio.getVolume();
      copyData(coverFis, newFos, copyCoverSize, volume);

      //复制srcStartTime时间后的源文件数据
      final long srcFileSize = new File(srcWavePath).length() - WAVE_HEAD_SIZE;
      int remainSize = (int) (srcFileSize - srcStartPos);
      if(remainSize > 0){

        coverFis.seek(WAVE_HEAD_SIZE + coverStartPos);
        copyData(srcFis, newFos, remainSize);

      }

    } catch (Exception e) {
      e.printStackTrace();

      return;

    }finally {
      //关闭输入流
      if(srcFis != null){
        try {
          srcFis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(coverFis != null){
        try {
          coverFis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(newFos != null){
        try {
          newFos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // 删除源文件,
    //new File(srcWavePath).delete();
    // 转换临时文件为源文件
    AudioEncodeUtil.convertPcm2Wav(tempOutPcmPath, outAudio.getPath(), sampleRate, channels, byteNum * 8);
    //删除临时文件
    new File(tempOutPcmPath).delete();
  }

  /**
   * 混合音频,针对声道数,采样率,采样位数都一样的wav音频
   *
   * @param srcAudio 源音频路径
   * @param coverAudio 覆盖音频路径
   * @param startTime 源音频起始时间
   */
  public static void mixAudioWithSame(Audio srcAudio, Audio coverAudio, Audio outAudio, float startTime) {

    String srcWavePath = srcAudio.getPath();
    String coverWavePath = coverAudio.getPath();
    int sampleRate = srcAudio.getSampleRate();
    int channels = srcAudio.getChannel();
    int byteNum = srcAudio.getByteNum();
    RandomAccessFile srcFis = null;
    RandomAccessFile coverFis = null;
    RandomAccessFile newFos = null;
    String tempOutPcmPath = outAudio.getPath() + ".tempPcm";
    try {

      //创建输入流
      srcFis = new RandomAccessFile(srcWavePath, "rw");
      coverFis = new RandomAccessFile(coverWavePath, "rw");
      newFos = new RandomAccessFile(tempOutPcmPath, "rw");

      final int srcStartPos = getPositionFromWave(startTime, channels, sampleRate);
      final int coverStartPos = 0;
      final int coverEndPos = (int) coverFis.length() - WAVE_HEAD_SIZE;

      //复制源音频srcStartTime时间之前的数据
      //跳过头文件数据
      srcFis.seek(WAVE_HEAD_SIZE);

      copyData(srcFis, newFos, srcStartPos);

      //跳过指定位置数据
      coverFis.seek(WAVE_HEAD_SIZE);
      //混合音频指定时间段的数据
      int copyCoverSize = coverEndPos - coverStartPos;
      float volume = coverAudio.getVolume();
      mixData(srcFis, coverFis, newFos, copyCoverSize, volume);

      //判断源音频后面是否还有音频数据,如果有则复制
      final long srcFileSize = srcFis.getChannel().size();
      int remainSize = (int) (srcFileSize - (srcStartPos + copyCoverSize));
      if (remainSize > 0) {

        //跳过指定位置数据
        srcFis.seek(WAVE_HEAD_SIZE + srcStartPos + copyCoverSize);
        copyData(srcFis, newFos, remainSize);
      }
    } catch (Exception e) {
      e.printStackTrace();

      return;
    } finally {
      //关闭输入流
      if (srcFis != null) {
        try {
          srcFis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (coverFis != null) {
        try {
          coverFis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (newFos != null) {
        try {
          newFos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // 删除源文件,
    //new File(srcWavePath).delete();
    // 转换临时文件为源文件
    AudioEncodeUtil.convertPcm2Wav(tempOutPcmPath, outAudio.getPath(), sampleRate, channels, byteNum * 8);
    //删除临时文件
    new File(tempOutPcmPath).delete();
  }

  /**
   * 获取wave文件某个时间对应的数据位置
   */
  private static int getPositionFromWave(float time, int channels, int sampleRate) {
    int position = (int) (time * 16 / 8 * channels * sampleRate);

    position = position / (16 / 8 * channels) * (16 / 8 * channels);

    return position;
  }

  /**
   * 复制数据
   *
   * @param fis 源输入流
   * @param fos 目标输出流
   * @param cooySize 复制大小
   */
  private static void copyData(RandomAccessFile fis, RandomAccessFile fos, final int cooySize) {

    byte[] buffer = new byte[2048];
    int length;
    int totalReadLength = 0;

    try {

      while ((length = fis.read(buffer)) != -1) {

        //short[] aa = converToShort(buffer);

        fos.write(buffer, 0, length);

        totalReadLength += length;

        int remainSize = cooySize - totalReadLength;
        if (remainSize <= 0) {
          //读取指定位置完成
          break;
        } else if (remainSize < buffer.length) {
          //离指定位置的大小小于buffer的大小,换remainSize的buffer
          buffer = new byte[remainSize];
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * 复制数据
   * @param fis 源输入流
   * @param fos 目标输出流
   * @param cooySize 复制大小
   * @param volumeValue 音量调节大小
   */
  private static void copyData(RandomAccessFile fis, RandomAccessFile fos, final int cooySize, final float volumeValue){

    byte[] buffer = new byte[2048];
    int length;
    int totalReadLength = 0;

    try {

      while((length = fis.read(buffer)) != -1){

        fos.write(changeDataWithVolume(buffer, volumeValue), 0, length);

        totalReadLength += length;

        int remainSize = cooySize - totalReadLength;
        if(remainSize <= 0){
          //读取指定位置完成
          break;
        }else if(remainSize < buffer.length){
          //离指定位置的大小小于buffer的大小,换remainSize的buffer
          buffer = new byte[remainSize];
        }

      }

    }catch (Exception ex){
      ex.printStackTrace();
    }

  }

  /**
   * 合成音频
   */
  private static void mixData(RandomAccessFile srcFis, RandomAccessFile coverFis,
      RandomAccessFile fos, final int copySize, float volumeValue) {

    MultiAudioMixer mix = MultiAudioMixer.createDefaultAudioMixer();

    byte[] srcBuffer = new byte[2048];
    byte[] coverBuffer = new byte[2048];
    int length;
    int totalReadLength = 0;

    try {

      while ((length = coverFis.read(coverBuffer)) != -1) {

        srcFis.read(srcBuffer);
        coverBuffer = changeDataWithVolume(coverBuffer, volumeValue);

        byte[] mixData = mix.mixRawAudioBytes(new byte[][] { srcBuffer, coverBuffer });
        fos.write(mixData);

        totalReadLength += length;

        int remainSize = copySize - totalReadLength;
        if (remainSize <= 0) {
          //读取指定位置完成
          break;
        } else if (remainSize < coverBuffer.length) {
          //离指定位置的大小小于buffer的大小,换remainSize的buffer
          coverBuffer = new byte[remainSize];
          srcBuffer = new byte[remainSize];
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * 改变音量
   */
  private static byte[] changeDataWithVolume(byte[] buffer, float volumeValue) {

    for (int i = 0; i < buffer.length; i = i + 2) {
      int value = ByteUtil.byte2Short(buffer[i + 1], buffer[i]);
      int tempValue = value;
      value *= volumeValue;
      value = value > 0x7fff ? 0x7fff : value;
      value = value < -0x8000 ? -0x8000 : value;

      short newValue = (short) value;

      byte[] array = ByteUtil.short2Byte(newValue);
      buffer[i + 1] = array[0];
      buffer[i] = array[1];
    }

    return buffer;
  }
}
