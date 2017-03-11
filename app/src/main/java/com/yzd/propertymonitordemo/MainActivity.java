package com.yzd.propertymonitordemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.yzd.propertymonitor.annotation.Monitor;

public class MainActivity extends Activity {

    @Monitor(willSet = "nameChangeBefore", didSet = "nameChangeAfter")
    String name = "李四";

    boolean nameChangeBefore() {
        Log.d("MainActivity", "nameChangeBefore");
        return true;
    }

    void nameChangeAfter(String oldValue, final String newValue) {
        Log.d("MainActivity", "nameChangeAfter");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                helloTv.setText(newValue);
            }
        });
    }

    TextView helloTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helloTv = (TextView) findViewById(R.id.tv_hello);

        final MainActivityHelper helper = (MainActivityHelper) MainActivityHelper.init(this);

        new Thread() {
            @Override
            public void run() {
                int a = 10;
                while (true) {
                    a++;
                    helper.setName("张三" + a);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }
}
