package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 天气类，用于集合管理Basic、AQI、Now、Suggestion、Forecast五个类
 * Created by SJ on 2017/7/18.
 */

public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastsList;


}
