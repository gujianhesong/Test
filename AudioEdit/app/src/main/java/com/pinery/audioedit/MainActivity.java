package com.pinery.audioedit;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    MainFragment.newInstance().navigateTo(this, R.id.fl_content);
  }

  @Override public void onBackPressed() {
    getSupportFragmentManager().popBackStack();
    if(getSupportFragmentManager().getBackStackEntryCount() > 1){
      return;
    }
    super.onBackPressed();
  }
}
