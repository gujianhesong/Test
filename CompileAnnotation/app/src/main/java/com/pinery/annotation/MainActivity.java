package com.pinery.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.pinery.annotations.BindView;
import com.pinery.bind_lib.BindHelper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_text)
    TextView tvText;
    @BindView(R.id.btn_text)
    Button btnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BindHelper.bind(this);

        tvText.setText("Hello, BindView");
        btnText.setText("Hello, BindView");
    }
}
