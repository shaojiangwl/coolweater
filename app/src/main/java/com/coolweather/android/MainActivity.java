package com.coolweather.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


/**
 * 1、在build.gradle中添加项目所需的各种依赖库
 * 2、创建应用所需的实体类
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
