package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateWeatherService extends Service {
    public AutoUpdateWeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    /**
     * 服务启动时
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {

        updateWeather();//更新天气信息
        updateBingPic();//更新必应图片

        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);//获取定时服务
        int anHour = 8*60*60*1000;//8个小时的毫秒数
        long trggerAtTime = SystemClock.currentThreadTimeMillis()+anHour;//
        Intent i = new Intent(this,AutoUpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,trggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();

                //将数据保存在本地数据文件
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateWeatherService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();//应用保存
            }
        });

    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //从缓存获取天气数据
        String weatherString = prefs.getString("weather",null);
        if(weatherString!=null)
        {
            //如果有缓存，就直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=ac54a1d6e1ab4a128d39e658a1c86221";

            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    //如果解析到的数据实体类不为空，或者获取到数据状态为OK
                    if(weather!=null&&"ok".equals(weather.status))
                    {
                        //将数据保存在本地数据文件
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateWeatherService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();//应用保存

                    }
                }
            });
        }
    }
}
