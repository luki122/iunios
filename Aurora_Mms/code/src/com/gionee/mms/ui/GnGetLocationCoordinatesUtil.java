package com.gionee.mms.ui;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import aurora.app.AuroraActivity;
import android.widget.Toast;
import android.content.Intent;
import android.provider.Settings;
import android.content.SharedPreferences;
import android.app.Dialog;
import aurora.app.AuroraProgressDialog;
import android.provider.Telephony.Threads;
import com.android.mms.util.Recycler;

public class GnGetLocationCoordinatesUtil {
    private Context context;
    LocationManager locManager;
    StringBuilder stb;
    public GnGetLocationCoordinatesUtil (Context context)
    {this.context=context;
     getCoordibates();}
    private void getCoordibates(){
        locManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        // 从GPS获取最近的最近的定位信息
        Location location = locManager.getLastKnownLocation(
            LocationManager.GPS_PROVIDER);
        // 使用location根据EditText的显示
        updateView(location);
        // 设置每3秒获取一次GPS的定位信息
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER
            , 3000, 8, new LocationListener()
        {   public void onLocationChanged(Location location)
            {
                // 当GPS定位信息发生改变时，更新位置
                updateView(location);
            }
            
            public void onProviderDisabled(String provider)
            {
                updateView(null);}

            public void onProviderEnabled(String provider)
            {
                // 当GPS LocationProvider可用时，更新位置
                updateView(locManager.getLastKnownLocation(provider));
            }
            public void onStatusChanged(String provider, int status,
                Bundle extras)
            {}});
    }
    // 更新坐标
    public void updateView(Location newLocation)
    {
//        Log.d("MYLOG","haha,this is updateView func:");    

        stb = new StringBuilder();
        if (newLocation != null)
           {
            /*tianyi keep these code for debug*/
//            String mCurrentLocationString = null;
            double lat = newLocation.getLatitude();   
            double lng = newLocation.getLongitude();   
//            mCurrentLocationString = "{\"longitude\":" + lat + ",\"latitude\":" + lng + "}";
//            writePreviousLocation(mCurrentLocationString);//更新位置的时候，需要更新上一次的位置信息
//            Log.d("MYLOG","updateView....lat:" + lat + "lng:" + lng);    
//            Log.d("MYLOG","mCurrentLocationString:" + mCurrentLocationString);
//            Toast.makeText(context, "纬度与经度！", Toast.LENGTH_SHORT).show();
//            Toast.makeText(context, mCurrentLocationString, Toast.LENGTH_SHORT).show();
            
            stb.append("{\"longitude\":");
            stb.append((float)lng);
            stb.append(",\"latitude\":");
            stb.append((float)lat);
            stb.append("}");
            writePreviousLocation(stb.toString());//只有能获取位置的时候，才更新上一次的位置信息；获取无效时，走else语句
           }
        else
           {stb=null;}
        
    }
    
    public String ReturnCoordinates(){
//        Log.d("MYLOG","haha,this is ReturnCoordinates()");    
        if(stb!=null)
           {return stb.toString();}
        else {return null;}
    }
    
    public void writePreviousLocation(String preLocationString)
    {
        SharedPreferences GnPreviousLocation = context.getSharedPreferences("pre_loc_data", 0); //首先获取一个sharedpreferences对象
        GnPreviousLocation.edit().putString("Previous_Location", preLocationString).commit();//把gps位置信息放入"Previous_Location"中
    }
    public String readPreviousLocation()
    {
        SharedPreferences GnPreviousLocation = context.getSharedPreferences("pre_loc_data", 0); //首先获取一个sharedpreferences对象
        return GnPreviousLocation.getString("Previous_Location",null);//获取保存的gps位置信息
    }
    
    //由于系统可以设置是否开启GPS功能，如果用户关闭了此功能程序是无法获取GPS信息的，我们可以通过下面的代码检测他是否开启，如果没有开启可以提示用户打开此功能。
    public void openGPSSettings() {
        
        Log.d("MYLOG","haha,this is openGPSSettings");        
        
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "GPS模块正常", Toast.LENGTH_SHORT).show();
            
            Log.d("MYLOG","haha,this is openGPSSettings & gps is well");        
        }else{    
            Toast.makeText(context, "请开启GPS！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//            context.startActivityForResult(intent,0); //此为设置完成后返回到获取界面getSharedPreferences
        }
    }
    
}






