package com.coolweather.android.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * 自定义全局Application类
 * Created by SJ on 2017/7/17.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);//初始化LitePal的Application,否则不能操作数据库
    }


    public static Context getContext()
    {
        return context;
    }


}
