package com.pinery.lottery.defaults;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

import com.pinery.lottery.IInnerPanView;
import com.pinery.lottery.R;

/**
 * 默认转盘内部区域
 * Created by hesong-os on 2018/9/28.
 */

public class DefaultInnerPan extends AppCompatImageView implements IInnerPanView {
    public DefaultInnerPan(Context context) {
        super(context);

        setBackgroundResource(R.drawable.lottery_pan);
    }

    @Override
    public int getRadius() {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        return radius;
    }
}
