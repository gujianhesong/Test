package com.pinery.lottery.defaults;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

import com.pinery.lottery.ILotterButton;
import com.pinery.lottery.R;

/**
 * 默认抽奖按钮
 * Created by hesong-os on 2018/9/28.
 */

public class DefaultLotteryButton extends AppCompatImageView implements ILotterButton {
    public DefaultLotteryButton(Context context) {
        super(context);

        setBackgroundResource(R.drawable.lottery_node);
    }
}
