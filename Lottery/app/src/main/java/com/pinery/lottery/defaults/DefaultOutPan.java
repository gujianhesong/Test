package com.pinery.lottery.defaults;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

import com.pinery.lottery.CustomOutPanView;
import com.pinery.lottery.IOutPanView;
import com.pinery.lottery.R;

/**
 * 默认转盘外部区域
 * Created by hesong-os on 2018/9/28.
 */

public class DefaultOutPan extends AppCompatImageView implements IOutPanView {
    public DefaultOutPan(Context context) {
        super(context);

        setBackgroundResource(R.drawable.lottery_pan_outer);
    }

    @Override
    public void setOnOutPanListener(CustomOutPanView.OnOutPanListener listener) {
    }

    @Override
    public void startLuckLight() {
    }
}
