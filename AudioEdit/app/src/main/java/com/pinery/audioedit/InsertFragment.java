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

public class InsertFragment extends Fragment {

  private static final int REQUEST_AUDIO_CODE = 1;

  private TextView tvAudioPath1, tvAudioPath2;
  private Button btnPickAudioPath1, btnPickAudioPath2;
  private Button btnInsertAudio;
  private Button btnPlayAudio;
  private TextView tvMsgInfo;
  private EditText etStartTime;
  private TextView tvAudioLength1, tvAudioLength2;

  private int mCurPickBtnId;
  private String mCurPath;

  public static InsertFragment newInstance() {
    InsertFragment fragment = new InsertFragment();
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

    View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_insert, null);

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
    btnInsertAudio = view.findViewById(R.id.btn_insert_audio);
    btnPlayAudio = view.findViewById(R.id.btn_play_audio);
    tvMsgInfo = view.findViewById(R.id.tv_msg_info);
    etStartTime = view.findViewById(R.id.et_start_time);
    tvAudioLength1 = view.findViewById(R.id.tv_audio_length_1);
    tvAudioLength2 = view.findViewById(R.id.tv_audio_length_2);

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

    btnInsertAudio.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        insertAudio();
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

    updateAudioTime(tvAudioLength1, tvAudioPath1.getText().toString());
    updateAudioTime(tvAudioLength2, tvAudioPath2.getText().toString());

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
  private void insertAudio() {
    String path1 = tvAudioPath1.getText().toString();
    String path2 = tvAudioPath2.getText().toString();

    if(TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2)){
      ToastUtil.showToast("音频路径为空");
      return;
    }
    if(TextUtils.isEmpty(etStartTime.getText().toString())){
      ToastUtil.showToast("开始时间不能为空");
      return;
    }

    float startTime = Float.valueOf(etStartTime.getText().toString());

    float path1Time = FileUtils.getFilePlayTime(getContext(), new File(path1)) / 1000f;
    if(startTime < 0 || startTime > path1Time){
      ToastUtil.showToast("开始时间不正确");
      return;
    }

    AudioTaskCreator.createInsertAudioTask(getContext(), path1, path2, startTime);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_AUDIO_CODE) {
        String path = FileUtils.queryFilePath(getContext(), data.getData());

        switch (mCurPickBtnId) {
          case R.id.btn_pick_audio_1:
            tvAudioPath1.setText(path);
            updateAudioTime(tvAudioLength1, tvAudioPath1.getText().toString());
            break;
          case R.id.btn_pick_audio_2:
            tvAudioPath2.setText(path);
            updateAudioTime(tvAudioLength2, tvAudioPath2.getText().toString());
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

  private void updateAudioTime(TextView textView, String path){
    if(!TextUtils.isEmpty(path)){
      String time = FileUtils.getFilePlayTimeString(getContext(), new File(path));
      textView.setText("音频时长：" + time);
    }
  }

}
