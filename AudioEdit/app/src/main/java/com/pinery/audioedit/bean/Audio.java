package com.pinery.audioedit.bean;

import android.media.AudioFormat;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import java.io.File;
import java.io.FileInputStream;

import static android.media.MediaFormat.KEY_PCM_ENCODING;

/**
 * 音频信息
 */
public class Audio {
    private String path;
    private String name;
    private float volume = 1f;
    private int channel = 2;
    private int sampleRate = 44100;
    private int bitNum = 16;
    private int timeMillis;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getBitNum() {
        return bitNum;
    }

    public void setBitNum(int bitNum) {
        this.bitNum = bitNum;
    }

    public int getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(int timeMillis) {
        this.timeMillis = timeMillis;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN) public static Audio createAudioFromFile(File inputFile) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        MediaFormat format = null;
        int i;

        try {
            extractor.setDataSource(inputFile.getPath());
        }catch (Exception ex){
            ex.printStackTrace();
            extractor.setDataSource(new FileInputStream(inputFile).getFD());
        }

        int numTracks = extractor.getTrackCount();
        for (i = 0; i < numTracks; i++) {
            format = extractor.getTrackFormat(i);
            if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                extractor.selectTrack(i);
                break;
            }
        }
        if (i == numTracks) {
            throw new Exception("No audio track found in " + inputFile);
        }

        Audio audio = new Audio();
        audio.name = inputFile.getName();
        audio.path = inputFile.getAbsolutePath();
        audio.sampleRate = format.containsKey(MediaFormat.KEY_SAMPLE_RATE) ? format.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 44100;
        audio.channel = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT) ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 1;
        audio.timeMillis = (int) ((format.getLong(MediaFormat.KEY_DURATION) / 1000.f));

        //根据pcmEncoding编码格式，得到采样精度，MediaFormat.KEY_PCM_ENCODING这个值不一定有
        int pcmEncoding = format.containsKey(MediaFormat.KEY_PCM_ENCODING) ? format.getInteger(MediaFormat.KEY_PCM_ENCODING) : AudioFormat.ENCODING_PCM_16BIT;
        switch (pcmEncoding){
            case AudioFormat.ENCODING_PCM_FLOAT:
                audio.bitNum = 32;
                break;
            case AudioFormat.ENCODING_PCM_8BIT:
                audio.bitNum = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
            default:
                audio.bitNum = 16;
                break;
        }

        extractor.release();

        return audio;
    }

}
