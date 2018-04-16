package com.test.ripple;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * 圆角的RelativeLayout
 */
public class RippleLayout extends RelativeLayout {
  private final String TAG = getClass().getSimpleName();

  private Path mPath;
  private int mRadius;

  private int mWidth;
  private int mHeight;

  private int mX, mY;

  private float mProgress = 1;
  private float mBackProgress;

  public RippleLayout(Context context) {
    super(context);

    init();
  }

  public RippleLayout(Context context, AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  private void init() {

    if(getBackground() == null){
      //需设置背景,否则无法显示圆角裁剪
      setBackgroundColor(Color.TRANSPARENT);
    }

    mPath = new Path();
    mPath.setFillType(Path.FillType.EVEN_ODD);

    setCornerRadius(dp2px(4));
  }

  public void setCenterXY(int x, int y) {
    mX = x;
    mY = y;
  }

  public void updateRoundShowRadius(int radius) {
    mRadius = radius;

    postInvalidate();
  }

  /**
   * 设置圆角半径
   */
  public void setCornerRadius(int radius) {
    mRadius = radius;
  }

  /**
   * 设置进度
   */
  public void setProgress(float progress) {

    mProgress = progress;

    postInvalidate();
  }

  private void checkPathChanged() {
    if (mProgress == mBackProgress) {
      return;
    }
    mBackProgress = mProgress;

    mWidth = getWidth();
    mHeight = getHeight();
    setCenterXY(mWidth / 2, mHeight / 2);
    int maxRadius = (int) Math.hypot(mWidth, mHeight) / 2;
    mRadius = (int) (maxRadius * mProgress);

    mPath.reset();
    mPath.addCircle(mX, mY, mRadius, Path.Direction.CW);
  }

  @Override public void draw(Canvas canvas) {
    Log.e(TAG, "----------------draw");

    int saveCount = canvas.save();

    checkPathChanged();

    canvas.clipPath(mPath);
    super.draw(canvas);

    canvas.restoreToCount(saveCount);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Log.e(TAG, "----------------onDraw");
  }

  private int dp2px(float dpValue) {
    final float scale = getContext().getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }
}
