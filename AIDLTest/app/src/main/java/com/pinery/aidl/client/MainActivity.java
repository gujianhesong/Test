package com.pinery.aidl.client;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
  private Client mClient;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mClient = new Client(getBaseContext());
    mClient.connect();

    findViewById(R.id.tv_text).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        try {
          mClient.showToast("hahahahhha");
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    });

  }

  @Override protected void onDestroy() {
    super.onDestroy();

    mClient.disconnect();
  }
}
