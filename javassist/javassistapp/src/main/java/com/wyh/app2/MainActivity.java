package com.wyh.app2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wyh.mylibrary.WYHTest;
import com.yhao.floatwindow.FloatWindow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mTextview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextview = findViewById(R.id.textview);
//        mTextview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("MainActivity", "onClick222");
//            }
//        });

//        FloatWindow.with(getApplicationContext()).build();

        WYHTest.setOnClickListener(mTextview);
    }

    @Override
    public void onClick(View v) {
        Log.d("MainActivity", "onClick111");

    }
}
