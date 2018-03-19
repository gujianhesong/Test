package com.pinery.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.pinery.test.util.FileUtils;
import com.pinery.test.util.TaskManager;
import com.pinery.test.util.ToastUtil;
import java.io.File;

/**
 * @author hesong
 * @time 2018/1/9
 * @desc
 */

public class MainFragment extends Fragment {

  private static final int REQUEST_AUDIO_CODE = 1;

  private TextView tvAudioPath1;

  private int mCurPickBtnId;
  private String mCurPath;

  public static MainFragment newInstance() {
    MainFragment fragment = new MainFragment();
    fragment.setArguments(new Bundle());
    return fragment;
  }

  public void navigateTo(FragmentActivity activity, int parentViewId){
    activity.getSupportFragmentManager().beginTransaction()
        .add(parentViewId, this, getClass().getSimpleName())
        .addToBackStack(getClass().getSimpleName())
        .commit();
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_main, null);

    return view;
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initViews(view);
  }

  private void initViews(View view) {
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return true;
      }
    });
    tvAudioPath1 = view.findViewById(R.id.tv_audio_path_1);

    view.findViewById(R.id.btn_pick_audio_1).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        pickAudioFile(view.getId());
      }
    });

    view.findViewById(R.id.btn_decode_audio).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        decodeAudio();
      }
    });
    view.findViewById(R.id.btn_decode_video).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        decodeVideo();
      }
    });
    view.findViewById(R.id.btn_encode_audio).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        encodeAudio();
      }
    });
    view.findViewById(R.id.btn_encode_video).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        encodeVideo();
      }
    });

  }

  private void decodeAudio(){
    if(TextUtils.isEmpty(mCurPath)){
      ToastUtil.showToast("没找到文件");
      return;
    }

    final String newPath = FileUtils.getAudioStorageDirectory() + "/output.pcm";

    FileUtils.confirmFolderExist(new File(newPath).getParent());

    TaskManager.getInstance().post(new Runnable() {
      @Override public void run() {
        VideoUtil.decodeAudio(mCurPath, newPath);
      }
    });

  }

  private void decodeVideo(){
    if(TextUtils.isEmpty(mCurPath)){
      ToastUtil.showToast("没找到文件");
      return;
    }

    final String newPath = FileUtils.getAudioStorageDirectory() + "/output.yuv";

    FileUtils.confirmFolderExist(new File(newPath).getParent());

    TaskManager.getInstance().post(new Runnable() {
      @Override public void run() {
        VideoUtil.decodeVideo(mCurPath, newPath);
      }
    });

  }

  private void encodeAudio(){
    final String srcPath = FileUtils.getAudioStorageDirectory() + "/output.pcm";

    final String newPath = FileUtils.getAudioStorageDirectory() + "/output.aac";

    FileUtils.confirmFolderExist(new File(newPath).getParent());

    TaskManager.getInstance().post(new Runnable() {
      @Override public void run() {
        VideoUtil.encodeAudio(srcPath, newPath);
      }
    });

  }

  private void encodeVideo(){
    final String srcPath = FileUtils.getAudioStorageDirectory() + "/output.yuv";

    final String newPath = FileUtils.getAudioStorageDirectory() + "/output.h264";

    FileUtils.confirmFolderExist(new File(newPath).getParent());

    TaskManager.getInstance().post(new Runnable() {
      @Override public void run() {
        VideoUtil.encodeVideo(srcPath, newPath);
      }
    });

  }

  /**
   * 选取音频文件
   */
  private void pickAudioFile(int viewId) {
    mCurPickBtnId = viewId;

    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("video/*");
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    startActivityForResult(intent, REQUEST_AUDIO_CODE);
  }

  private void playAudio(String path){
    try {
      Intent it = new Intent(Intent.ACTION_VIEW);
      it.setDataAndType(Uri.parse("file://" + path), "video/*");
      startActivity(it);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_AUDIO_CODE) {
        String path = FileUtils.queryFilePath(getContext(), data.getData());

        switch (mCurPickBtnId) {
          case R.id.btn_pick_audio_1:
            tvAudioPath1.setText(path);
            mCurPath = path;

            break;
          default:
            break;
        }
      }
    }
  }

}
