package com.test.ripple;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
  private final static int ANIM_TIME = 1000;

  Button btnToogle;
  Button btnToogle2;
  Button btnToast;
  RippleLayout rippleView;

  private boolean isExpand = true;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    rippleView = (RippleLayout) findViewById(R.id.ripple_view);

    btnToogle = (Button) findViewById(R.id.btn_toogle);
    btnToogle.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        toogle();
      }
    });

    btnToogle2 = (Button) findViewById(R.id.btn_toogle2);
    btnToogle2.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        toogleOther();
      }
    });

    btnToast = (Button) findViewById(R.id.btn_toast);
    btnToast.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Toast.makeText(MainActivity.this, "点击到我了", Toast.LENGTH_SHORT).show();
      }
    });

  }

  private void toogle(){
    isExpand = !isExpand;

    if(isExpand){
      expand();
    }else{
      unexpand();
    }

  }

  private void expand(){
    doRippleAnim(0.2f, 1);
  }

  private void unexpand(){
    doRippleAnim(1, 0.2f);
  }

  private void doRippleAnim(final float fromPercent, final float toPercent){
    ValueAnimator animator = ValueAnimator.ofFloat(fromPercent, toPercent).setDuration(ANIM_TIME);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {

        final float progress = fromPercent + animation.getAnimatedFraction() * (toPercent - fromPercent);

        rippleView.setProgress(progress);

      }
    });
    animator.start();

  }

  private void toogleOther(){
    isExpand = !isExpand;

    if(isExpand){
      expandOther();
    }else{
      unexpandOther();
    }

  }

  /**
   * 展开动画
   */
  private void expandOther(){
    final float fromPercent = 0.1f;
    final float toPercent = 1f;

    final float fromScale = 0.1f;
    final float toScale = 1f;

    final float fromX = dp2px(100);
    final float fromY = dp2px(100);
    final float toX = 0;
    final float toY = 0;

    final float fromProgress =
        (float) (rippleView.getWidth() / Math.hypot(rippleView.getWidth(), rippleView.getHeight()));
    final float toProgress = 1;

    ValueAnimator animator = ValueAnimator.ofFloat(fromPercent, toPercent).setDuration(ANIM_TIME);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {

        final float progress = animation.getAnimatedFraction();

        rippleView.setProgress((toProgress - fromProgress) * progress + fromProgress);

        rippleView.setX((toX - fromX) * progress + fromX);
        rippleView.setY((toY - fromY) * progress + fromY);
        rippleView.setScaleX((toScale - fromScale) * progress + fromScale);
        rippleView.setScaleY((toScale - fromScale) * progress + fromScale);

      }
    });
    animator.start();
  }

  /**
   * 收起动画
   */
  private void unexpandOther(){
    final float fromPercent = 1f;
    final float toPercent = 0.1f;

    final float fromScale = 1f;
    final float toScale = 0.1f;

    final float fromX = 0;
    final float fromY = 0;
    final float toX = dp2px(100);
    final float toY = dp2px(100);

    final float fromProgress = 1;
    final float toProgress =
        (float) (rippleView.getWidth() / Math.hypot(rippleView.getWidth(), rippleView.getHeight()));

    ValueAnimator animator = ValueAnimator.ofFloat(fromPercent, toPercent).setDuration(ANIM_TIME);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {

        final float progress = animation.getAnimatedFraction();

        rippleView.setProgress((toProgress - fromProgress) * progress + fromProgress);

        rippleView.setX((toX - fromX) * progress + fromX);
        rippleView.setY((toY - fromY) * progress + fromY);
        rippleView.setScaleX((toScale - fromScale) * progress + fromScale);
        rippleView.setScaleY((toScale - fromScale) * progress + fromScale);

      }
    });
    animator.start();
  }

  private int dp2px(float dpValue) {
    final float scale = getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

}
