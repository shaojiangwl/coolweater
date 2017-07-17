package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 发送网络请求帮助类
 * Created by SJ on 2017/7/17.
 */

public class HttpUtil {

    /**
     * 发送网络请求
     * @param address 请求地址
     * @param callback 回调函数
     */
    public static  void sendOkHttpRequest(String address,okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }


}
