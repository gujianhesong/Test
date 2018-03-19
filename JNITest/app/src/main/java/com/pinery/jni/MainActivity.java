package com.pinery.jni;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

  private int page = 1;

  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Example of a call to a native method
    TextView tv = (TextView) findViewById(R.id.sample_text);

    //tv.setText(int2String(100));

    tv.setText("page 的值是 " + getJavaFieldValue());

    editJavaFieldValue();
    tv.setText("page 的值是 " + page);

    callJavaMethod();


  }

  private void showMessage(String msg){
    Toast.makeText(this, "来自底层的消息：" + msg, Toast.LENGTH_SHORT).show();
  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  public native String stringFromJNI();

  public native String int2String(int value);

  public native int getJavaFieldValue();

  public native void editJavaFieldValue();

  public native void callJavaMethod();


}
