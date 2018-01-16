package com.pinery.audioedit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.TextView;
import com.pinery.audioedit.bean.AudioMsg;
import com.pinery.audioedit.service.AudioTaskCreator;
import com.pinery.audioedit.util.FileUtils;
import com.pinery.audioedit.util.ToastUtil;
import java.io.File;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author hesong
 * @time 2018/1/9
 * @desc
 */

public class CutFragment extends Fragment {

  private static final int REQUEST_AUDIO_CODE = 1;

  private TextView tvAudioPath1;
  private Button btnPickAudioPath1;
  private Button btnCutAudio;
  private Button btnPlayAudio;
  private TextView tvAudioLength;
  private TextView tvMsgInfo;

  private EditText etStartTime, etEndTime;

  private int mCurPickBtnId;
  private String mCurPath;

  public static CutFragment newInstance() {
    CutFragment fragment = new CutFragment();
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

    View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_cut, null);

    return view;
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initViews(view);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();

    EventBus.getDefault().unregister(this);
  }

  private void initViews(View view) {
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return true;
      }
    });
    tvAudioPath1 = view.findViewById(R.id.tv_audio_path_1);
    btnPickAudioPath1 = view.findViewById(R.id.btn_pick_audio_1);
    btnCutAudio = view.findViewById(R.id.btn_cut_audio);
    btnPlayAudio = view.findViewById(R.id.btn_play_audio);
    tvAudioLength = view.findViewById(R.id.tv_audio_length_1);
    tvMsgInfo = view.findViewById(R.id.tv_msg_info);
    etStartTime = view.findViewById(R.id.et_start_time);
    etEndTime = view.findViewById(R.id.et_end_time);

    btnPickAudioPath1.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        pickAudioFile(view.getId());
      }
    });

    btnCutAudio.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        cutAudio();
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

    updateAudioTime(tvAudioPath1.getText().toString());
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
   * 裁剪音频
   */
  private void cutAudio() {

    String path1 = tvAudioPath1.getText().toString();

    if(TextUtils.isEmpty(path1)){
      ToastUtil.showToast("音频路径为空");
      return;
    }
    if(TextUtils.isEmpty(etStartTime.getText().toString()) || TextUtils.isEmpty(etEndTime.getText().toString())){
      ToastUtil.showToast("开始时间和结束时间不能为空");
      return;
    }

    float startTime = Float.valueOf(etStartTime.getText().toString());
    float endTime = Float.valueOf(etEndTime.getText().toString());

    if(startTime <= 0){
      ToastUtil.showToast("时间不对");
      return;
    }
    if(endTime <= 0){
      ToastUtil.showToast("时间不对");
      return;
    }
    if(startTime >= endTime){
      ToastUtil.showToast("时间不对");
      return;
    }

    AudioTaskCreator.createCutAudioTask(getContext(), path1, startTime, endTime);
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

  private void updateAudioTime(String path){
    String time = FileUtils.getFilePlayTimeString(getContext(), new File(path));
    tvAudioLength.setText("音频时长：" + time);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_AUDIO_CODE) {
        String path = FileUtils.queryFilePath(getContext(), data.getData());

        switch (mCurPickBtnId) {
          case R.id.btn_pick_audio_1:
            tvAudioPath1.setText(path);

            updateAudioTime(path);

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


}
