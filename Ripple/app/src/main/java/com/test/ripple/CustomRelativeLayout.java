package com.test.ripple;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 打印绘制相关信息
 */
public class CustomRelativeLayout extends RelativeLayout {
  private final String TAG = getClass().getSimpleName();


  public CustomRelativeLayout(Context context) {
    super(context);

    init();
  }

  public CustomRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  private void init() {


  }

  @Override public void draw(Canvas canvas) {
    Log.e(TAG, "----------------draw");

    super.draw(canvas);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Log.e(TAG, "----------------onDraw");
  }

  @Override protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    Log.e(TAG, "----------------drawChild");

    return super.drawChild(canvas, child, drawingTime);
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    Log.e(TAG, "----------------dispatchDraw");

    super.dispatchDraw(canvas);
  }
}
