package com.wyh.mylibrary;

import android.util.Log;
import android.view.View;

/**
 * @author WangYingHao
 * @since 2019-10-20
 */
public class WYHTest {
    public static void setOnClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "onClick333");

            }
        });
    }
}
