package com.pinery.lottery;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by hesong-os on 2018/9/14.
 */

public class MainActivity extends Activity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LotteryView lotteryView = findViewById(R.id.lottery_view);
        lotteryView.setLotteryListener(new LotteryView.LotteryListener() {
            @Override
            public boolean checkStartPrepareSuccess() {
                return true;
            }

            @Override
            public void onRotateEnd(int position) {

            }
        });

    }
}
