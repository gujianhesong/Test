package com.pinery.audioedit;

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
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.pinery.audioedit.bean.AudioMsg;
import com.pinery.audioedit.service.AudioTaskCreator;
import com.pinery.audioedit.util.FileUtils;
import com.pinery.audioedit.util.ToastUtil;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author hesong
 * @time 2018/1/9
 * @desc
 */

public class MixFragment extends Fragment {

  private static final int REQUEST_AUDIO_CODE = 1;

  private TextView tvAudioPath1, tvAudioPath2;
  private Button btnPickAudioPath1, btnPickAudioPath2;
  private Button btnMixAudio;
  private Button btnPlayAudio;
  private SeekBar sbAudio1;
  private SeekBar sbAudio2;
  private TextView tvMsgInfo;

  private float progressAudio1, progressAudio2;

  private int mCurPickBtnId;
  private String mCurPath;

  public static MixFragment newInstance() {
    MixFragment fragment = new MixFragment();
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

    EventBus.getDefault().register(this);

    View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_mix, null);

    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();

    EventBus.getDefault().unregister(this);
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
    tvAudioPath2 = view.findViewById(R.id.tv_audio_path_2);
    btnPickAudioPath1 = view.findViewById(R.id.btn_pick_audio_1);
    btnPickAudioPath2 = view.findViewById(R.id.btn_pick_audio_2);
    btnMixAudio = view.findViewById(R.id.btn_mix_audio);
    btnPlayAudio = view.findViewById(R.id.btn_play_audio);
    sbAudio1 = view.findViewById(R.id.sb_audio_1);
    sbAudio2 = view.findViewById(R.id.sb_audio_2);
    tvMsgInfo = view.findViewById(R.id.tv_msg_info);

    btnPickAudioPath1.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        pickAudioFile(view.getId());
      }
    });

    btnPickAudioPath2.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        pickAudioFile(view.getId());
      }
    });

    btnMixAudio.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        mixAudio();
      }
    });

    btnPlayAudio.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if(TextUtils.isEmpty(mCurPath)){
          ToastUtil.showToast("没找到文件");
          return;
        }
        playAudio(mCurPath);
      }
    });
  }

  /**
   * 选取音频文件
   */
  private void pickAudioFile(int viewId) {
    mCurPickBtnId = viewId;

    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("audio/*");
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    startActivityForResult(intent, REQUEST_AUDIO_CODE);
  }

  /**
   *
   */
  private void mixAudio() {

    String path1 = tvAudioPath1.getText().toString();
    String path2 = tvAudioPath2.getText().toString();

    if(TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2)){
      ToastUtil.showToast("音频路径为空");
      return;
    }

    progressAudio1 = sbAudio1.getProgress() * 1f / sbAudio1.getMax();
    progressAudio2 = sbAudio2.getProgress() * 1f / sbAudio2.getMax();

    AudioTaskCreator.createMixAudioTask(getContext(), path1, path2, progressAudio1, progressAudio2);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_AUDIO_CODE) {
        String path = FileUtils.queryFilePath(getContext(), data.getData());

        switch (mCurPickBtnId) {
          case R.id.btn_pick_audio_1:
            tvAudioPath1.setText(path);
            break;
          case R.id.btn_pick_audio_2:
            tvAudioPath2.setText(path);
            break;
          default:
            break;
        }
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onReceiveAudioMsg(AudioMsg msg) {
    if(msg != null && !TextUtils.isEmpty(msg.msg)){
      tvMsgInfo.setText(msg.msg);
      mCurPath = msg.path;
    }
  }

  private void playAudio(String path){
    try {
      Intent it = new Intent(Intent.ACTION_VIEW);
      it.setDataAndType(Uri.parse("file://" + path), "audio/*");
      startActivity(it);
    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

}
