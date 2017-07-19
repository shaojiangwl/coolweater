package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 *  和风天气json数据中的Basic字段属性
 * 由于json中的一些字段可能不太适合直接作为java字段来命名，
 * 因此需要使用@SerializedName("city")注解的方式来让json字段和java字段相关联
 * 括号内的字段就是json中的字段属性
 * Created by SJ on 2017/7/18.
 */

public class Now {

    @SerializedName("tmp")
    public String tmperature;

    @SerializedName("cond")
    public More more;

    public class  More
    {
        @SerializedName("txt")
        public String info;
    }

  }
