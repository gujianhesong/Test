package com.pinery.lottery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by hesong-os on 2018/9/14.
 */

public class CustomOutPanView extends View implements IOutPanView{

    public interface OnOutPanListener {
        int getDistanceWithInnerPan();

        boolean isRotating();
    }

    private boolean isYellow = false;
    private Paint smallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int radius;
    int CircleX;
    int CircleY;
    private int delayTime = 500;
    private int distanceWithInnerPan;
    private OnOutPanListener onOutPanListener;

    public CustomOutPanView(Context context) {
        super(context);

        backgroundPaint.setColor(Color.rgb(255, 92, 93));
        distanceWithInnerPan = dp2px(30);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOuterCircle(canvas);
    }

    @Override
    public void setOnOutPanListener(OnOutPanListener listener) {
        onOutPanListener = listener;
    }

    @Override
    public void startLuckLight() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isYellow = !isYellow;
                invalidate();

                if (onOutPanListener != null && onOutPanListener.isRotating()) {
                    postDelayed(this, delayTime);
                }
            }
        }, delayTime);
    }

    private void drawOuterCircle(Canvas canvas) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        int width = getWidth() - paddingLeft - paddingRight;
        int height = getHeight() - paddingTop - paddingBottom;

        int MinValue = Math.min(width, height);

        radius = MinValue / 2;
        CircleX = getWidth() / 2;
        CircleY = getHeight() / 2;

        canvas.drawCircle(CircleX, CircleY, radius, backgroundPaint);

        drawSmallCircle(canvas, isYellow);
    }

    private void drawSmallCircle(Canvas canvas, boolean FirstYellow) {
        if (onOutPanListener != null) {
            distanceWithInnerPan = onOutPanListener.getDistanceWithInnerPan();
        }

        int pointDistance = (radius + radius - distanceWithInnerPan) / 2;
        for (int i = 0; i <= 360; i += 20) {
            int x = (int) (pointDistance * Math.sin(change(i))) + CircleX;
            int y = (int) (pointDistance * Math.cos(change(i))) + CircleY;

            if (FirstYellow) {
                smallCirclePaint.setColor(Color.YELLOW);
                canvas.drawCircle(x, y, dp2px(4), smallCirclePaint);
            } else {
                smallCirclePaint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, dp2px(4), smallCirclePaint);
            }
            FirstYellow = !FirstYellow;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param dpValue 虚拟像素
     * @return 像素
     */
    private int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * getContext().getResources().getDisplayMetrics().density);
    }

    private double change(double a) {
        return a * Math.PI / 180;
    }
}