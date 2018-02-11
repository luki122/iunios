package datas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherdata.db.CNCityAdapter;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by joy on 11/3/14.
 * 类方法只能在主线程下调用
 */
public class LocationProvider {
    private LocationClient locationClient;
    private Context context;
    private ILocateResult iLocateResult;
    public LocationProvider(Context context,ILocateResult iLocateResult){
    	locationClient=new LocationClient(context);
    	initLocation();
    	this.context=context;
    	locationClient.registerLocationListener(mBDLocationListener);
    	this.iLocateResult=iLocateResult;
    	locationClient.start();
    }
    
	private CityItem getCityIdFromLocation(String name,String upCity,CNCityAdapter weatherDB){
        int id = -1;
		List<CityItem> listCityItem = new ArrayList<CityItem>();
        weatherDB.getCityListFromHZ(listCityItem, name);
        CityItem item=null;
		for(int i=0;i<listCityItem.size();i++)
		{
			item=listCityItem.get(i);
			item=weatherDB.getCityFromId(item.getId());
			if(item!=null)
			{
                if(TextUtils.isEmpty(upCity)||(!TextUtils.isEmpty(item.getProvince()))&&upCity.contains(item.getProvince()))
                {
                	return item;
                }
			}
		}
		return null;
	}
	
	
	private CityItem queryLocateCityId(BDLocation location){
		CNCityAdapter adapter=new CNCityAdapter(context);
		adapter.open();
		String district=location.getDistrict();
		String city=location.getCity();
		String province=location.getProvince();
//		district = "";
//		city="博山";
//		province="山东省";		
		CityItem cityItem=null;
		if(!TextUtils.isEmpty(district))
		{
		   cityItem=getCityIdFromLocation(district,province, adapter);
		}
		if(cityItem==null)
		{
			if(!TextUtils.isEmpty(district))
			{
			   cityItem=getCityIdFromLocation(district.substring(0, district.length()-1), province, adapter);
			}
			if(cityItem==null)
			{
				cityItem=getCityIdFromLocation(city, province, adapter);
				if(cityItem==null)
				{
					cityItem=getCityIdFromLocation(city.substring(0, city.length()-1), province, adapter);
				}
			}
		}
		adapter.close();
		return cityItem;
	}
	
	private boolean getIsLocalFail(){
		SharedPreferences sp=context.getSharedPreferences("AuroraWeatherMain", Context.MODE_PRIVATE);
		return sp.getBoolean("islocalfail", false);
	}
	
    private BDLocationListener mBDLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location

            if (location != null) {
                if (location.hasAddr()) {
                    CityItem searchItem=queryLocateCityId(location);
                    //以下是测试代码
                    if(AuroraWeatherMain.isShowTest&&getIsLocalFail())
                    {
                    	searchItem=null;//模拟定位失败
                    }
                    //发送取相应城市天气的消息
                    if(searchItem!=null)
                    {
	            		iLocateResult.locateSuccess(searchItem.getCityName(), searchItem.getId());
	            		locationClient.stop();
	                    locationClient.unRegisterLocationListener(mBDLocationListener);
	            		Log.e("jadon", "定位成功 cityName="+searchItem.getCityName()+"          upCityname="+searchItem.getUpCity());
	            		return;
                    }else{
                    	Log.e("jadon", "定位失败111");
                    	iLocateResult.locateError();
                    	locationClient.stop();
                        locationClient.unRegisterLocationListener(mBDLocationListener);
                        return;
                    }
                   
                }else{
	                locationClient.stop();
	                locationClient.unRegisterLocationListener(mBDLocationListener);
	                Log.e("jadon", "定位失败222");
	            	iLocateResult.locateError();
                }
            }else{
	            	 locationClient.stop();
	                 locationClient.unRegisterLocationListener(mBDLocationListener);
	                 Log.e("jadon", "定位失败333");
	             	 iLocateResult.locateError();
            }

        }
    };
    
    public void dispose(){
    	if(locationClient.isStarted())
    	{
    		locationClient.stop();
    		locationClient.unRegisterLocationListener(mBDLocationListener);
    	}
    }

    
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//设置定位模式
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
        option.setScanSpan(1500);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }

    public static interface ILocateResult{
    	void locateSuccess(String cityName,int cityId);
    	void locateError();
    }
    
    
}
