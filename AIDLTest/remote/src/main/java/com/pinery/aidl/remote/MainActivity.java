package com.pinery.aidl.remote;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
  private Client mClient;

  private Button btnServiceOpen;
  private Button btnServiceConnnect;
  private TextView tvServiceMsg;
  private Handler mHandler = new Handler();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initViews();

    initData();

  }

  @Override protected void onResume() {
    super.onResume();

    updateServiceState();
  }

  private void initViews(){
    tvServiceMsg = findViewById(R.id.tv_msg);
    btnServiceOpen = findViewById(R.id.btn_toogle_service);
    btnServiceConnnect = findViewById(R.id.btn_toogle_connect);

    btnServiceOpen.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if(mClient.isServiceRunning()){
          mClient.close();
        }else{
          mClient.open();
        }

        updateServiceState();

      }
    });
    btnServiceConnnect.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if(mClient.isServiceConnected()){
          mClient.disconnect();
        }else{
          mClient.connect();
        }

        updateServiceState();

      }
    });
    findViewById(R.id.btn_show_toast).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        try {
          mClient.showToast("hahahahhaha");
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void initData(){
    mClient = new Client(getBaseContext());

    updateServiceState();
  }

  private void updateServiceState(){

    mHandler.postDelayed(new Runnable() {
      @Override public void run() {

        String msg = "";
        if(mClient.isServiceRunning()){
          btnServiceOpen.setText("停止服务");
          msg = "服务已开启";
        }else{
          btnServiceOpen.setText("开启服务");
          msg = "服务已停止";
        }

        if(mClient.isServiceConnected()){
          btnServiceConnnect.setText("断连服务");
          msg += "，已连接服务";
        }else{
          btnServiceConnnect.setText("连接服务");
          msg += "，未连接服务";
        }

        tvServiceMsg.setText(msg);

      }
    }, 500);

  }

  @Override protected void onDestroy() {
    super.onDestroy();

    mClient.disconnect();
  }
}
