package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析Json文档帮助类
 * Created by SJ on 2017/7/17.
 */

public class Utility {


    /**
     * 解析服务器返回的省份数据并转存到本地数据库表
     * @param response 服务器返回的数据
     * @return
     */
    public static boolean handleProvinceResponse(String response)
    {
        if(!TextUtils.isEmpty(response))
        {
            try {
                //将服务器返回的数据放入数据
                JSONArray allProvinces = new JSONArray(response);
                //循环遍历数组，取出每个单实体信息,并转存到本地数据库表
                for(int i = 0;i<allProvinces.length();i++)
                {
                    JSONObject jsonObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return  false;
    }


    /**
     * 解析服务器返回的城市数据并转存到本地数据库表
     * @param response  服务器返回的数据
     * @param provinceId  省份Id
     * @return
     */
    public  static boolean handCityResponse(String response,int provinceId)
    {
        if(!TextUtils.isEmpty(response))
        {
            try
            {
                //将服务器返回的数据放入数据
                JSONArray allCitys = new JSONArray(response);
                //循环遍历数组，取出每个单实体信息,并转存到本地数据库表
                for(int i = 0;i<allCitys.length();i++)
                {
                    JSONObject jsonObject = allCitys.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return  true;
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return  false;
    }

    /**
     * 解析服务器返回的县级数据并转存到本地数据库表
     * @param response 服务器返回的数据
     * @param cityId   城市Id
     * @return
     */
    public static  boolean handCountyRequest(String response,int cityId)
    {
        if(!TextUtils.isEmpty(response))
        {
            try
            {
                JSONArray allCountys = new JSONArray(response);

                for(int i = 0;i<allCountys.length();i++)
                {
                    JSONObject jsonObject = allCountys.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;

            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 将服务器返回的json数据解析成Weather实体类
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Gson gson = new Gson();
            return gson.fromJson(weatherContent,Weather.class);

        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
