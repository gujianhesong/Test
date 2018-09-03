package com.test.memory.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.test.memory.common.ControInfos;

/**
 * 存在内存泄露的动画View，由于动画Cancel之后，还是会回调onAnimationEnd，所以需要额外判断是否取消状态，否则动画会一直执行下去，导致内存泄露问题
 *
 * Created by hesong-os on 2018/9/3.
 */

public class LeakAnimView extends View{
    private static final String TAG = "AnimView";

    private AnimatorSet animatorSet, animatorSet2;
    private ObjectAnimator scaleX, scaleY;
    private ObjectAnimator scaleX2, scaleY2;

    private boolean isAnimating;

    public LeakAnimView(Context context) {
        super(context);

        init();
    }

    public LeakAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        setBackgroundColor(Color.RED);

        animatorSet = new AnimatorSet();
        scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.5f, 1f);
        scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.5f, 1f);
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.setDuration(500);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "animatorSet onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "animatorSet onAnimationEnd");

                //取消动画时，该方法依然会被回调，所以下个动画会执行，存在内存泄露问题，所以要做状态的判断

                if(ControInfos.testMemoryLeak){
                    //这里存在内存泄露问题
                    animatorSet2.start();
                }else{
                    //这里解决了内存泄露问题
                    if(isAnimating){
                        animatorSet2.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "animatorSet onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet2 = new AnimatorSet();
        scaleX2 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.5f);
        scaleY2 = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.5f);
        animatorSet2.play(scaleX2).with(scaleY2);
        animatorSet2.setDuration(500);
        animatorSet2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "animatorSet2 onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "animatorSet2 onAnimationEnd");

                //取消动画时，该方法依然会被回调，所以下个动画会执行，存在内存泄露问题，所以要做状态的判断

                if(ControInfos.testMemoryLeak){
                    //这里存在内存泄露问题
                    animatorSet.start();
                }else{
                    //这里解决了内存泄露问题
                    if(isAnimating){
                        animatorSet.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "animatorSet2 onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    public void start(){
        if(isAnimating){
            return;
        }
        isAnimating = true;
        animatorSet.start();
    }

    public void cancel(){
        if(isAnimating){
            isAnimating = false;
            if(animatorSet.isRunning() || animatorSet.isStarted()){
                animatorSet.cancel();
            }
            if(animatorSet2.isRunning() || animatorSet2.isStarted()){
                animatorSet2.cancel();
            }
        }
    }
}
