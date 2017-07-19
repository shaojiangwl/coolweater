package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;//主活动页面的滚动控件

    private TextView titleCityName;//title布局文件中显示城市名称的控件

    private TextView titleUpdateTime;//title布局中显示更新日期的控件

    private TextView degreesText;//now布局中显示当前天气气温的控件

    private TextView weatherInfoText;//now布局中显示当前天气概况的控件

    private LinearLayout forecastLayout;//forecast显示未来天气的布局控件

    private TextView aqiText;//aqi布局中显示当前aqi指数的控件

    private TextView pm25Text;//now布局中显示当前pm2.5指数的控件

    private TextView comfortText;//suggestion布局中显示当前天气舒适度的控件

    private TextView carWashText;//suggestion布局中显示当前天气洗车指数的控件

    private TextView sportText;//suggestion布局中显示当前天气运动建议的控件


    private ImageView bing_pic_img;//背景图片


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //如果系统的版本高于5.0系统
        if(Build.VERSION.SDK_INT>21)
        {
            //
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            //设置状态背景为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        setContentView(R.layout.activity_weather);

        //初始化各个控件
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);//主活动页面的滚动控件
        titleCityName = (TextView)findViewById(R.id.title_cityName);//title布局文件中显示城市名称的控件
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);//title布局中显示更新日期的控件
        degreesText = (TextView)findViewById(R.id.degree_text);//now布局中显示当前天气气温的控件
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);//now布局中显示当前天气概况的控件
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);//forecast显示未来天气的布局控件
        aqiText = (TextView)findViewById(R.id.aqi_text);//aqi布局中显示当前aqi指数的控件
        pm25Text = (TextView)findViewById(R.id.pm25_text);//now布局中显示当前pm2.5指数的控件
        comfortText = (TextView)findViewById(R.id.comfort_text);//suggestion布局中显示当前天气舒适度的控件
        carWashText = (TextView)findViewById(R.id.car_wash_text);//suggestion布局中显示当前天气洗车指数的控件
        sportText = (TextView)findViewById(R.id.sport_text);//suggestion布局中显示当前天气运动建议的控件
        bing_pic_img = (ImageView)findViewById(R.id.bing_pic_img);//背景图片

        //获取本地缓存中的天气信息
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);
        //如果有缓存，直接解析天气信息
        if(weatherString!=null)
        {
            //将本地缓存中的数据解析成Weather类
            Weather weather = Utility.handleWeatherResponse(weatherString);
            //显示天气信息
            showWeatherInfo(weather);
        }else
        {
            String weatherId = getIntent().getStringExtra("weather_id");
            //隐藏主活动中的滚动控件
            weatherLayout.setVisibility(View.INVISIBLE);
            //根据天气Id查询天气信息
            requestWeather(weatherId);
        }

        //查找缓存中的图片，如果存在使用Glide加载至图片控件
        String bingImg = preferences.getString("bing_pic",null);
        if(bingImg!=null)
        {
            Glide.with(this).load(bingImg).into(bing_pic_img);
        }else
        {
            //从必应服务器获取
            loadBingPic();
        }

    }

    private void loadBingPic() {

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
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();//应用保存

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //更新活动背景图片
                        Glide.with(WeatherActivity.this).load(bingPic).into(bing_pic_img);
                    }
                });
            }
        });
    }

    /**
     * 根据天气Id请求城市天气信息
     * @param weatherId
     */
    private void requestWeather(final String weatherId) {

        //请求天气的地址，ac54a1d6e1ab4a128d39e658a1c86221和风天气注册的Key
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+
                weatherId+"&key=ac54a1d6e1ab4a128d39e658a1c86221";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取从服务器返回数据信息
                final  String responseText = response.body().string();
                //将服务器返回的信息解析成Weather实体类
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果解析到的数据实体类不为空，或者获取到数据状态为OK
                        if(weather!=null&&"ok".equals(weather.status))
                        {
                            //将数据保存在本地数据文件
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();//应用保存

                            //显示天气信息
                            showWeatherInfo(weather);
                        }else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    /**
     * 显示天气信息
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;//城市名称
        String updateTime = weather.basic.update.updateTime.split(" ")[1];//更新日期时间
        String degree = weather.now.tmperature+"℃";//气温
        String weatherInfo = weather.now.more.info;//天气概况
        titleCityName.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreesText.setText(degree);
        weatherInfoText.setText(weatherInfo);


        //循环遍历未来天气数组
        for(Forecast forecast :weather.forecastsList)
        {
            //动态加载未来天气项布局文件
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);//将未来天气项布局文件添加父布局（至未来天气布局）控件中
        }


        if(weather.aqi!=null)
        {
            aqiText.setText(weather.aqi.city.aqi);//aqi指数
            pm25Text.setText(weather.aqi.city.pm25);//pm2.5指数
        }

        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动指数："+weather.suggestion.sport.info;
        comfortText.setText(comfort);//舒适度
        carWashText.setText(carWash);//洗车指数
        sportText.setText(sport);//运动指数
        //显示滚动控件
        weatherLayout.setVisibility(View.VISIBLE);

    }
}
