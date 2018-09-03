package com.test.memory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.test.memory.activity.HomeActivity;
import com.test.memory.common.ControInfos;
import com.test.memory.fragment.HomeFragment;

public class MainActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    findViewById(R.id.btn_test_leak).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //测试存在内存泄露1问题，当前存在内存泄露
        ControInfos.reset();
        ControInfos.testMemory = true;
        ControInfos.testMemoryLeak = true;
        HomeActivity.navigate(getBaseContext(), HomeFragment.class);
      }
    });

    findViewById(R.id.btn_test_no_leak).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //测试内存泄露1问题解决，当前不存在内存泄露
        ControInfos.reset();
        ControInfos.testMemory = true;
        ControInfos.testMemoryLeak = false;
        HomeActivity.navigate(getBaseContext(), HomeFragment.class);
      }
    });

    findViewById(R.id.btn_optimize_pre).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //测试未优化内存占用
        ControInfos.reset();
        ControInfos.testMemory = true;
        ControInfos.testMemoryOptimize = false;
        HomeActivity.navigate(getBaseContext(), HomeFragment.class);
      }
    });

    findViewById(R.id.btn_optimize_post).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //测试优化过内存占用
        ControInfos.reset();
        ControInfos.testMemory = true;
        ControInfos.testMemoryOptimize = true;
        HomeActivity.navigate(getBaseContext(), HomeFragment.class);
      }
    });

  }
}
