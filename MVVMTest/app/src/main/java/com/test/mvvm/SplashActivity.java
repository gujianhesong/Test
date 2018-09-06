package com.test.mvvm;

import android.app.Activity;
import android.os.Bundle;

import com.test.mvvm.activity.HomeActivity;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HomeActivity.navigate(getBaseContext());

        finish();
    }
}
