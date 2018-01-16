package com.pinery.audioedit.util;

import com.pinery.audioedit.common.Constant;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 音频格式转换
 */
public class AudioEncodeUtil {

  /**
   * wav转pcm
   */
  public static void convertWav2Pcm(String inWaveFilePath, String outPcmFilePath) {

    FileInputStream in = null;
    FileOutputStream out = null;
    byte[] data = new byte[1024];

    try {

      in = new FileInputStream(inWaveFilePath);
      out = new FileOutputStream(outPcmFilePath);

      byte[] header = new byte[44];
      in.read(header);

      int length = 0;
      while ((length = in.read(data)) > 0) {
        out.write(data, 0, length);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * pcm转wav
   */
  public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath) {

    convertPcm2Wav(inPcmFilePath, outWavFilePath, Constant.ExportSampleRate,
        Constant.ExportChannelNumber, 16);
  }


  /**
   * PCM文件转WAV文件
   * @param inPcmFilePath 输入PCM文件路径
   * @param outWavFilePath 输出WAV文件路径
   * @param sampleRate 采样率，例如44100
   * @param channels 声道数 单声道：1或双声道：2
   * @param bitNum 采样位数，8或16
   */
  public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate,
      int channels, int bitNum) {

    FileInputStream in = null;
    FileOutputStream out = null;
    byte[] data = new byte[1024];

    try {

      in = new FileInputStream(inPcmFilePath);
      out = new FileOutputStream(outWavFilePath);

      //PCM文件大小
      long totalAudioLen = in.getChannel().size();

      writeWaveFileHeader(out, totalAudioLen, sampleRate, channels, bitNum);

      int length = 0;
      while ((length = in.read(data)) > 0) {
        out.write(data, 0, length);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 输出WAV文件
   * @param out WAV输出文件流
   * @param totalAudioLen 整个音频PCM数据大小
   * @param sampleRate 采样率
   * @param channels 声道数
   * @param bitNum 采样位数
   * @throws IOException
   */
  private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
      int sampleRate, int channels, int bitNum) throws IOException {
    byte[] header = getWaveHeader(totalAudioLen, sampleRate, channels, bitNum);
    out.write(header, 0, 44);
  }

  /**
   * 获取Wav header 字节数据
   * @param totalAudioLen 整个音频PCM数据大小
   * @param sampleRate 采样率
   * @param channels 声道数
   * @param bitNum 采样位数
   * @throws IOException
   */
  public static byte[] getWaveHeader(long totalAudioLen, int sampleRate, int channels, int bitNum) throws IOException {

    //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
    long totalDataLen = totalAudioLen + 36;
    //采样字节byte率
    long byteRate = sampleRate * channels * bitNum / 8;

    byte[] header = new byte[44];
    header[0] = 'R'; // RIFF
    header[1] = 'I';
    header[2] = 'F';
    header[3] = 'F';
    header[4] = (byte) (totalDataLen & 0xff);//数据大小
    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
    header[8] = 'W';//WAVE
    header[9] = 'A';
    header[10] = 'V';
    header[11] = 'E';
    //FMT Chunk
    header[12] = 'f'; // 'fmt '
    header[13] = 'm';
    header[14] = 't';
    header[15] = ' ';//过渡字节
    //数据大小
    header[16] = 16; // 4 bytes: size of 'fmt ' chunk
    header[17] = 0;
    header[18] = 0;
    header[19] = 0;
    //编码方式 10H为PCM编码格式
    header[20] = 1; // format = 1
    header[21] = 0;
    //通道数
    header[22] = (byte) channels;
    header[23] = 0;
    //采样率，每个通道的播放速度
    header[24] = (byte) (sampleRate & 0xff);
    header[25] = (byte) ((sampleRate >> 8) & 0xff);
    header[26] = (byte) ((sampleRate >> 16) & 0xff);
    header[27] = (byte) ((sampleRate >> 24) & 0xff);
    //音频数据传送速率,采样率*通道数*采样深度/8
    header[28] = (byte) (byteRate & 0xff);
    header[29] = (byte) ((byteRate >> 8) & 0xff);
    header[30] = (byte) ((byteRate >> 16) & 0xff);
    header[31] = (byte) ((byteRate >> 24) & 0xff);
    // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
    header[32] = (byte) (channels * 16 / 8);
    header[33] = 0;
    //每个样本的数据位数
    header[34] = 16;
    header[35] = 0;
    //Data chunk
    header[36] = 'd';//data
    header[37] = 'a';
    header[38] = 't';
    header[39] = 'a';
    header[40] = (byte) (totalAudioLen & 0xff);
    header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
    header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
    header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

    return header;
  }

}
