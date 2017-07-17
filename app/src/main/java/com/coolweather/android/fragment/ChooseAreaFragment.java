package com.coolweather.android.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.R;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.MyApplication;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 遍历全国省市县的功能碎片
 * Created by SJ on 2017/7/17.
 */

public class ChooseAreaFragment extends Fragment {

    public static final  int LEVEL_PROVINCE = 0;//省份节点

    public static final  int LEVEL_CITY = 1;//城市节点

    public static final  int LEVEL_COUNTY = 2;//县级节点


    private ProgressDialog progressDialog;

    private TextView titleView;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList();

    /**
     * 省份列表
     */
    private List<Province> provincesList;

    /**
     * 市区列表
     */
    private List<City> cityList;


    /**
     * 县列表
     */
    private List<County> countyList;


    /**
     * 选中的省份
     */
    private Province selectedProvince;


    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前节点
     */
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //加载碎片布局文件
       View view = inflater.inflate(R.layout.choose_area,container,false);
        //查找布局页面上的控件
        titleView = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(MyApplication.getContext(),android.R.layout.simple_list_item_1,dataList);
        //初始化数据到listview控件
        listView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //设置listview的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE)
                {
                    selectedProvince = provincesList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY)
                {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });

        //设置按钮的点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY)
                {
                    queryCities();//加载城市数据
                }else if(currentLevel==LEVEL_CITY)
                {
                    queryProvinces();//加载
                }
            }
        });

        queryProvinces();//加载省份数据
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        provincesList = DataSupport.findAll(Province.class);
        if(provincesList.size()>0)
        {
            dataList.clear();
            for(Province province :provincesList)
            {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else
        {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询省份下的所有城市信息，如果查询不到就从服务器获取
     */
    private void queryCities() {

        titleView.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);

        if(cityList.size()>0)
        {
            dataList.clear();
            for (City city : cityList)
            {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else
        {
            //根据省份代码查询城市列表
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 获取城市下所有县，优先从数据库获取，如果没有查询到再从服务器获取
     */
    private void queryCounties() {
        //设置标题显示为当前选中的城市名称
        titleView.setText(selectedCity.getCityName());
        //显示按钮
        backButton.setVisibility(View.VISIBLE);
        //根据城市代码查询数据
        countyList = DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0)
        {
            dataList.clear();
            for (County county : countyList)
            {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }


    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {

        showProgressDialog();//显示进度对话框

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            /**
             * 加载失败后关闭进度对话框，给出加载失败提示
             * @param call
             * @param e
             */
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MyApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            /**
             * 加载数据
             * @param call
             * @param response
             * @throws IOException
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText = response.body().string();

                boolean result = false;

                if("province".equals(type))
                {
                    //解析从服务器获取到的Json格式的省份信息
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type))
                {
                    //解析从服务器获取到的Json格式的城市信息
                    result = Utility.handCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type))
                {
                    //解析从服务器获取到的Json格式的区县信息
                    result = Utility.handCountyRequest(responseText,selectedCity.getId());
                }

                if(result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();

                            if("province".equals(type))
                            {
                               queryProvinces();//查询省份信息
                            }else if("city".equals(type))
                            {
                               queryCities();//查询城市信息
                            }else if("county".equals(type))
                            {
                               queryCounties();//查询区县信息
                            }
                        }
                    });
                }

            }
        });


    }

    /**
     * 显示进度对话条
     */
    private void showProgressDialog() {

        if(progressDialog==null)
        {
            progressDialog = new ProgressDialog(getActivity());

            progressDialog.setMessage("正在加载...");

            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog()
    {
        if(progressDialog!=null) {
            progressDialog.dismiss();
        }
    }

}
