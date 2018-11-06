package com.pinery.lottery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.pinery.lottery.defaults.DefaultInnerPan;
import com.pinery.lottery.defaults.DefaultLotteryButton;
import com.pinery.lottery.defaults.DefaultOutPan;

/**
 * 抽奖转盘
 */
public class LotteryView extends RelativeLayout implements WheelListener {
    public interface LotteryListener {
        /**
         * 检查是否开始准备成功
         *
         * @return 返回 true 才会启动转动
         */
        boolean checkStartPrepareSuccess();

        /**
         * 旋转结束
         *
         * @param position
         */
        void onRotateEnd(int position);
    }

    private static final String TAG = LotteryView.class.getSimpleName();

    private RotateActionDirector mRotateActionDirector;

    private IOutPanView mOutPanView;
    private IInnerPanView mInnerPanView;
    private ILotterButton mLotterButton;

    private float mTimeSecond;

    public LotteryView(Context context) {
        super(context);

        init(context);
    }

    public LotteryView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init(context);
    }

    public LotteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        //转盘外部
        DefaultOutPan outPan = new DefaultOutPan(context);
        setOutPanView(outPan);

        //转盘
        DefaultInnerPan innerPan = new DefaultInnerPan(context);
        setInnerPanView(innerPan);

        //旋转处理器
        mRotateActionDirector = new RotateActionDirector(innerPan);

        //抽奖按钮
        DefaultLotteryButton lotteryButton = new DefaultLotteryButton(context);
        setLotterButton(lotteryButton);
    }

    @Override
    public void startRotate() {
        if (!mRotateActionDirector.isRotating()) {
            mRotateActionDirector.startRotate();
        }
    }

    @Override
    public void stopRotate(final int position) {
        if (mTimeSecond < mRotateActionDirector.minTime) {
            int delay = (int) ((mRotateActionDirector.minTime - mTimeSecond) * 1000);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRotateActionDirector.stopRotate(position);
                }
            }, delay);
        }
    }

    @Override
    public boolean isRotating() {
        return mRotateActionDirector.isRotating();
    }

    @Override
    public void setRotateInMiddle(boolean inMiddle) {
        mRotateActionDirector.setRotateInMiddle(inMiddle);
    }

    @Override
    public void setTimeOutRotatePosition(int position) {
        mRotateActionDirector.setTimeOutRotatePosition(position);
    }

    @Override
    public void setLotteryListener(LotteryListener listener) {
        mRotateActionDirector.setLotteryListener(listener);
    }

    @Override
    public void setItemCount(int count) {
        mRotateActionDirector.setItemCount(count);
    }

    @Override
    public void setMinTime(float minTime) {
        mRotateActionDirector.setMinTime(minTime);
    }

    @Override
    public void setMaxTime(float maxTime) {
        mRotateActionDirector.setMaxTime(maxTime);
    }

    /**
     *
     * @param outPanView
     */
    public void setOutPanView(IOutPanView outPanView){
        if(mOutPanView != null){
            removeView((View) mOutPanView);
        }

        mOutPanView = outPanView;
        if(mOutPanView != null){
            mOutPanView.setOnOutPanListener(new CustomOutPanView.OnOutPanListener() {
                @Override
                public int getDistanceWithInnerPan() {
                    return mInnerPanView != null ? mInnerPanView.getRadius() : 0;
                }

                @Override
                public boolean isRotating() {
                    return LotteryView.this.isRotating();
                }
            });

            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView((View) mOutPanView, layoutParams);
        }

    }

    /**
     *
     * @param innerPanView
     */
    public void setInnerPanView(IInnerPanView innerPanView){
        if(mInnerPanView != null){
            removeView((View) mInnerPanView);
        }

        mInnerPanView = innerPanView;
        if(mInnerPanView != null){
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView((View) mInnerPanView, layoutParams);
        }
    }

    /**
     *
     * @param lotterButton
     */
    public void setLotterButton(ILotterButton lotterButton){
        if(mLotterButton != null){
            removeView((View) mLotterButton);
        }

        mLotterButton = lotterButton;
        if(mLotterButton != null){
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView((View) mLotterButton, layoutParams);

            ((View) mLotterButton).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startRotate();
                }
            });
        }
    }

    /**
     * 转动动作处理
     */
    private class RotateActionDirector implements WheelListener {
        private ValueAnimator mAnimtor;
        private static final float MAX_TIME = 9;
        private static final float MIN_TIME = 3;
        private static final float DEFAULT_TIME = 15;
        private static final float DEFAULT_START_TIME = 1f;
        private static final float DEFAULT_END_TIME = 3f;
        private static final float DEFAULT_NUM_TYPE = 6;
        private float time = DEFAULT_TIME;
        private float speed = 360 * 2;
        private float itemCount = DEFAULT_NUM_TYPE;
        private boolean isInMiddle = false;
        private int timeOutPosition = 0;
        private LotteryListener lotteryListener;

        private float minTime = MIN_TIME;
        private float maxTime = MAX_TIME;

        /**
         * 当前旋转角度
         */
        private float curRotateDegree;
        /**
         * 开始的旋转角度
         */
        private float startRotateDegree;

        private View view;

        public RotateActionDirector(View view) {
            this.view = view;
            if (this.view == null) {
                throw new NullPointerException("没有可用的View");
            }
        }

        @Override
        public void startRotate() {
            if (lotteryListener == null || lotteryListener != null && !lotteryListener.checkStartPrepareSuccess()) {
                Log.e(TAG, "没有批准通过，不能开始");
                return;
            }

            startRotateDegree = curRotateDegree;

            mAnimtor = ValueAnimator.ofFloat(0, time);
            mAnimtor.setInterpolator(new LinearInterpolator());
            mAnimtor.setDuration((long) (time * 1000));
            mAnimtor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float updateValue = (float) animation.getAnimatedValue();
                    mTimeSecond = updateValue;

                    if (updateValue < DEFAULT_START_TIME) {
                        //起始加速
                        handleRotateStart(updateValue);

                    } else if (updateValue > maxTime) {
                        //超时减速停止
                        handleTimeOutEnd();

                    } else {

                        float delta = speed / DEFAULT_START_TIME;
                        float startDegrees = (float) (delta * Math.pow(DEFAULT_START_TIME, 2) / 2);

                        float degrees = startDegrees + (updateValue - DEFAULT_START_TIME) * speed;

                        rotateDegrees(degrees);
                    }

                }
            });
            mAnimtor.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTimeSecond = 0;
                }
            });
            mAnimtor.start();
        }

        @Override
        public void stopRotate(final int position) {
            mAnimtor.cancel();

            final float startDegree = curRotateDegree;
            final float endDegree = caculateEndDegree(curRotateDegree, position);
            final float time = DEFAULT_END_TIME;

            Log.i(TAG, "stopRotate, 停止position:" + position + ", degree : " + (endDegree - startDegree) + ", time : " + time);

            final ValueAnimator stopAnimator = ValueAnimator.ofFloat(0, 1);
            stopAnimator.setDuration((long) (time * 1000));
            stopAnimator.setInterpolator(new DecelerateInterpolator());
            stopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float updateValue = (float) animation.getAnimatedValue();

                    float degree = startDegree + (endDegree - startDegree) * updateValue;
                    rotateDegrees(degree);
                }
            });
            stopAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (lotteryListener != null) {
                        lotteryListener.onRotateEnd(position);
                    }
                }
            });
            stopAnimator.start();
        }

        @Override
        public boolean isRotating() {
            return mAnimtor != null && mAnimtor.isRunning();
        }

        @Override
        public void setRotateInMiddle(boolean inMiddle) {
            this.isInMiddle = inMiddle;
        }

        @Override
        public void setTimeOutRotatePosition(int position) {
            this.timeOutPosition = position;
        }

        @Override
        public void setLotteryListener(LotteryListener listener) {
            this.lotteryListener = listener;
        }

        @Override
        public void setItemCount(int count) {
            this.itemCount = count;
        }

        @Override
        public void setMinTime(float minTime) {
            if(minTime > 0){
                this.minTime = minTime;
            }
        }

        @Override
        public void setMaxTime(float maxTime) {
            if(maxTime > this.minTime){
                this.maxTime = maxTime;
            }
        }

        /**
         * 计算结束的旋转角度
         *
         * @param startDegree 开始角度
         * @param position    结束的位置
         * @return
         */
        private float caculateEndDegree(float startDegree, int position) {
            float perTypeDegree = 360f / itemCount;

            //float positionDegree = perTypeDegree * position + perTypeDegree / 2;
            float positionDegree = perTypeDegree * position;
            if (!isInMiddle) {
                //如果不是指定正中，则为一定范围的随机角度
                positionDegree += (Math.random() - 0.5) * 0.8 * perTypeDegree;
            }

            float endDegree = startDegree + (positionDegree - startDegree % 360 + 360);
            Log.i(TAG, "caculateEndDegree, positionDegree:" + positionDegree + ", startDegreeaaaa : " + (startDegree % 360));
            Log.i(TAG, "caculateEndDegree, startDegree:" + startDegree + ", endDegree : " + endDegree);

            float downDegree = caculateSpeedDownDegrees();
            Log.i(TAG, "caculateEndDegree, downDegree:" + downDegree);
            float delta = downDegree - endDegree;
            delta = (delta / 360 + 1) * 360;

            return endDegree;
        }

        private void handleRotateStart(float value) {
            //起始加速度
            float acceleration = speed / DEFAULT_START_TIME;

            //从当前位置开始转动
            float degrees = startRotateDegree % 360 + (float) (acceleration * Math.pow(value, 2) / 2);

            rotateDegrees(degrees);
        }

        private float caculateSpeedDownDegrees() {
            //起始加速度
            float downTime = DEFAULT_END_TIME;
            float acceleration = speed / downTime;

            float degrees = (float) (acceleration * Math.pow(downTime, 2) / 2);

            return degrees;
        }

        private void handleTimeOutEnd() {
            Log.i(TAG, "超时，开始减速停止");
            //超时处理，停止位置为超时位置
            stopRotate(timeOutPosition);
        }

        /**
         * 旋转角度
         *
         * @param degrees
         */
        private void rotateDegrees(float degrees) {
            curRotateDegree = degrees;
            view.setRotation(degrees);
        }
    }

}

interface WheelListener {
    /**
     * 开始转动
     */
    void startRotate();

    /**
     * 停止转动到指定位置
     *
     * @param position
     */
    void stopRotate(int position);

    /**
     * 是否正在转动
     *
     * @return
     */
    boolean isRotating();

    /**
     * 设置是否转到某个项的正中间，如果不是，则是一定范围的随机位置
     *
     * @param inMiddle
     */
    void setRotateInMiddle(boolean inMiddle);

    /**
     * 设置超时的默认旋转位置
     *
     * @param position
     */
    void setTimeOutRotatePosition(int position);

    /**
     * 设置监听
     *
     * @param listener
     */
    void setLotteryListener(LotteryView.LotteryListener listener);

    /**
     * 设置项目数量
     *
     * @param count
     */
    void setItemCount(int count);

    /**
     * 设置最小转动时间
     *
     * @param minTime
     */
    void setMinTime(float minTime);

    /**
     * 设置最大转动时间
     *
     * @param maxTime
     */
    void setMaxTime(float maxTime);
}