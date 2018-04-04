package com.pinery.aidl.remote;

import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
  private Client mClient;

  private Button btnServiceOpen, btnServiceConnect;
  private TextView tvServiceOpen;
  private Handler mHandler = new Handler();

  private boolean isConnect;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initViews();

    initData();

  }

  private void initViews(){
    btnServiceOpen = findViewById(R.id.btn_toogle_service);
    btnServiceOpen.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        if(mClient.isServiceRunning()){
          mClient.disconnect();
          mClient.close();
        }else{
          mClient.toogleOpen();
          mClient.connect();
        }

        mHandler.postDelayed(new Runnable() {
          @Override public void run() {
            updateServiceState();
          }
        }, 500);

      }
    });

    //btnServiceConnect = findViewById(R.id.btn_toogle_connect);
    //btnServiceConnect.setOnClickListener(new View.OnClickListener() {
    //  @Override public void onClick(View v) {
    //
    //    if(!mClient.isServiceRunning()){
    //      Toast.makeText(getBaseContext(), "服务没有开启", Toast.LENGTH_SHORT).show();
    //      return;
    //    }
    //
    //    if(isConnect){
    //      mClient.disconnect();
    //    }else{
    //      mClient.connect();
    //    }
    //    isConnect = !isConnect;
    //
    //    updateServiceState();
    //
    //  }
    //});

    tvServiceOpen = findViewById(R.id.tv_toogle_service);

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
          msg = "服务已开启, 已连接服务";
        }else{
          btnServiceOpen.setText("开启服务");
          msg = "服务已停止, 未连接服务";
        }

        tvServiceOpen.setText(msg);


      }
    }, 500);

  }

  @Override protected void onDestroy() {
    super.onDestroy();

    mClient.disconnect();
  }
}
