package com.pinery.audioedit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

  }

}
