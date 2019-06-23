package com.wyh.asmapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wyh.asmlibrary.HunterDebug;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int sum = add(2, 3);
        print("" + add(10L, 10L));
    }

    @HunterDebug
    private int add(int i, int j) {
        return i + j;
    }

    @HunterDebug
    private long add(long i, long j) {
        return i + j;
    }

    @HunterDebug
    private void print(String msg) {
        Log.d("MainActivity", msg);
    }
}
