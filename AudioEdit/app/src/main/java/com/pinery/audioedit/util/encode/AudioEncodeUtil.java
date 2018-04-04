package com.pinery.audioedit.util.encode;

import com.pinery.audioedit.common.Constant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 音频格式转换
 *
 */
public class AudioEncodeUtil {

    /**
     * wav转pcm
     * @param inWaveFilePath
     * @param outPcmFilePath
     */
    public static void convertWav2Pcm(String inWaveFilePath, String outPcmFilePath){

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
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null){
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
     * @param inPcmFilePath
     * @param outWavFilePath
     */
    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath){

        convertPcm2Wav(inPcmFilePath, outWavFilePath, Constant.ExportSampleRate, Constant.ExportChannelNumber);

    }

    /**
     * pcm转wav
     * @param inPcmFilePath
     * @param outWavFilePath
     * @param sampleRate
     * @param channels
     */
    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate, int channels){

        FileInputStream in = null;
        FileOutputStream out = null;
        long byteRate = 16 * sampleRate * channels / 8;
        byte[] data = new byte[1024];

        try {

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            long totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void convertPcm2Mp3(String inPcmFilePath, String outMp3FilePath){



    }

    public static void convertWav2Mp3(String inWaveFilePath, String outMp3FilePath){

        String pcmPath = inWaveFilePath + ".pcm";

        convertWav2Pcm(inWaveFilePath, pcmPath);

        convertPcm2Mp3(pcmPath, outMp3FilePath);

        new File(pcmPath).delete();

    }

    public static void convertPcm2Acc(String inPcmFilePath, String outAccFilePath){

        AudioEncoder.createAccEncoder(inPcmFilePath).encodeToFile(outAccFilePath);

    }

    public static void convertWav2Acc(String inWaveFilePath, String outAccFilePath){

        String pcmPath = inWaveFilePath + ".pcm";

        convertWav2Pcm(inWaveFilePath, pcmPath);

        convertPcm2Acc(pcmPath, outAccFilePath);

        new File(pcmPath).delete();

    }

    public static void convertWav2Amr(String inWaveFilePath, String outAmrFilePath){

        final byte[] header = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A};

        try {
            FileInputStream fileInputStream = new FileInputStream(inWaveFilePath);
            FileOutputStream fileoutputStream = new FileOutputStream(outAmrFilePath);
            // 获得Class
            Class<?> cls = Class.forName("android.media.AmrInputStream");
            // 通过Class获得所对应对象的方法
            Method[] methods = cls.getMethods();
            // 输出每个方法名
            fileoutputStream.write(header);
            Constructor<?> con = cls.getConstructor(InputStream.class);
            Object obj = con.newInstance(fileInputStream);
            for (Method method : methods) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if ("read".equals(method.getName())
                        && parameterTypes.length == 3) {
                    byte[] buf = new byte[1024];
                    int len = 0;
                    while ((len = (int) method.invoke(obj, buf, 0, buf.length)) > 0) {
                        fileoutputStream.write(buf, 0, len);
                    }
                    break;
                }
            }
            for (Method method : methods) {
                if ("close".equals(method.getName())) {
                    method.invoke(obj);
                    break;
                }
            }
            fileoutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的，
     *
     * @param out
     * @param totalAudioLen
     * @param totalDataLen
     * @param longSampleRate
     * @param channels
     * @param byteRate
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, int longSampleRate,
                                     int channels, long byteRate) throws IOException {
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
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
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
        out.write(header, 0, 44);
    }

}
