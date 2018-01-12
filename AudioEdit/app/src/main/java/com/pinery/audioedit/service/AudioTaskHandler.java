package com.pinery.audioedit.service;

import android.content.Intent;
import android.text.TextUtils;
import com.pinery.audioedit.bean.Audio;
import com.pinery.audioedit.bean.AudioMsg;
import com.pinery.audioedit.callback.DecodeOperateInterface;
import com.pinery.audioedit.common.Constant;
import com.pinery.audioedit.util.AudioMixUtil;
import com.pinery.audioedit.util.DecodeEngine;
import com.pinery.audioedit.util.FileUtils;
import com.pinery.audioedit.util.ToastUtil;
import java.io.File;
import org.greenrobot.eventbus.EventBus;

import static android.R.attr.path;
import static com.pinery.audioedit.util.AudioMixUtil.mixAudioWithSame;

/**
 */
public class AudioTaskHandler {


  public void handleIntent(Intent intent){

    if(intent == null){
      return;
    }

    String action = intent.getAction();

    switch (action){
      case AudioTaskCreator.ACTION_AUDIO_CUT:

      {
        //裁剪
        String path = intent.getStringExtra(AudioTaskCreator.PATH_1);
        float startTime = intent.getFloatExtra(AudioTaskCreator.START_TIME, 0);
        float endTime = intent.getFloatExtra(AudioTaskCreator.END_TIME, 0);
        cutAudio(path, startTime, endTime);
      }

        break;
      case AudioTaskCreator.ACTION_AUDIO_INSERT:

      {
        //合成
        String path1 = intent.getStringExtra(AudioTaskCreator.PATH_1);
        String path2 = intent.getStringExtra(AudioTaskCreator.PATH_2);
        float insertTime = intent.getFloatExtra(AudioTaskCreator.START_TIME, 0);

        insertAudio(path1, path2, insertTime);
      }

        break;
      case AudioTaskCreator.ACTION_AUDIO_MIX:

      {
        //合成
        String path1 = intent.getStringExtra(AudioTaskCreator.PATH_1);
        String path2 = intent.getStringExtra(AudioTaskCreator.PATH_2);

        mixAudio(path1, path2);
      }

        break;
      default:
      break;
    }

  }

  private void cutAudio(String path1, float startTime, float endTime){
    String fileName1 = new File(path1).getName();
    String nameNoSuffix = fileName1.substring(0, fileName1.lastIndexOf('.'));
    fileName1 = nameNoSuffix + Constant.SUFFIX_WAV;
    String outName = nameNoSuffix + "_cut.wav";

    String destPath1 = FileUtils.getAudioEditStorageDirectory() + File.separator + outName;

    decodeAudio(path1, destPath1);

    if(!FileUtils.checkFileExist(destPath1)){
      ToastUtil.showToast("解码失败" + destPath1);
      return;
    }

    Audio audio1 = getAudioFromPath(destPath1);

    if(audio1 != null){
      AudioMixUtil.cutAudio(audio1, startTime, endTime);
    }

    String msg = "裁剪完成";
    EventBus.getDefault().post(new AudioMsg(AudioTaskCreator.ACTION_AUDIO_CUT, destPath1, msg));
  }

  private void insertAudio(String path1, String path2, float startTime){
    String fileName1 = new File(path1).getName();
    fileName1 = fileName1.substring(0, fileName1.lastIndexOf('.')) + Constant.SUFFIX_WAV;
    String fileName2 = new File(path2).getName();
    fileName2 = fileName2.substring(0, fileName2.lastIndexOf('.')) + Constant.SUFFIX_WAV;

    String destPath1 = FileUtils.getAudioEditStorageDirectory() + File.separator + fileName1;
    String destPath2 = FileUtils.getAudioEditStorageDirectory() + File.separator + fileName2;

    decodeAudio(path1, destPath1);
    decodeAudio(path2, destPath2);

    if(!FileUtils.checkFileExist(destPath1)){
      ToastUtil.showToast("解码失败" + destPath1);
      return;
    }
    if(!FileUtils.checkFileExist(destPath2)){
      ToastUtil.showToast("解码失败" + destPath2);
      return;
    }

    Audio audio1 = getAudioFromPath(destPath1);
    Audio audio2 = getAudioFromPath(destPath2);
    Audio outAudio = new Audio();
    outAudio.setPath(new File(new File(destPath1).getParentFile(), "insert_out.wav").getAbsolutePath());

    if(audio1 != null && audio2 != null){
      AudioMixUtil.insertAudioWithSame(audio1, audio2, outAudio, startTime);
    }

    String msg = "插入完成";
    EventBus.getDefault().post(new AudioMsg(AudioTaskCreator.ACTION_AUDIO_INSERT, outAudio.getPath(), msg));

  }

  private void mixAudio(String path1, String path2){
    String fileName1 = new File(path1).getName();
    fileName1 = fileName1.substring(0, fileName1.lastIndexOf('.')) + Constant.SUFFIX_WAV;
    String fileName2 = new File(path2).getName();
    fileName2 = fileName2.substring(0, fileName2.lastIndexOf('.')) + Constant.SUFFIX_WAV;

    String destPath1 = FileUtils.getAudioEditStorageDirectory() + File.separator + fileName1;
    String destPath2 = FileUtils.getAudioEditStorageDirectory() + File.separator + fileName2;

    decodeAudio(path1, destPath1);
    decodeAudio(path2, destPath2);

    if(!FileUtils.checkFileExist(destPath1)){
      ToastUtil.showToast("解码失败" + destPath1);
      return;
    }
    if(!FileUtils.checkFileExist(destPath2)){
      ToastUtil.showToast("解码失败" + destPath2);
      return;
    }

    Audio audio1 = getAudioFromPath(destPath1);
    Audio audio2 = getAudioFromPath(destPath2);
    Audio outAudio = new Audio();
    outAudio.setPath(new File(new File(destPath1).getParentFile(), "out.wav").getAbsolutePath());

    if(audio1 != null && audio2 != null){
      AudioMixUtil.mixAudioWithSame(audio1, audio2, outAudio, 0);
    }

    String msg = "合成完成";
    EventBus.getDefault().post(new AudioMsg(AudioTaskCreator.ACTION_AUDIO_MIX, outAudio.getPath(), msg));

  }

  private void decodeAudio(String path, String destPath){
    final File file = new File(path);

    if(FileUtils.checkFileExist(destPath)){
      FileUtils.deleteFile(new File(destPath));
    }

    FileUtils.confirmFolderExist(new File(destPath).getParent());

    DecodeEngine.getInstance().convertMusicFileToWaveFile(path, destPath, new DecodeOperateInterface() {
      @Override public void updateDecodeProgress(int decodeProgress) {
        String msg = String.format("解码文件：%s，进度：%d", file.getName(), decodeProgress) + "%";
        EventBus.getDefault().post(new AudioMsg(AudioTaskCreator.ACTION_AUDIO_MIX, msg));
      }

      @Override public void decodeSuccess() {

      }

      @Override public void decodeFail() {

      }
    });
  }

  private Audio getAudioFromPath(String path){
    if(!FileUtils.checkFileExist(path)){
      return null;
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
      try {
        Audio audio = Audio.createAudioFromFile(new File(path));
        return audio;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return null;
  }

}
