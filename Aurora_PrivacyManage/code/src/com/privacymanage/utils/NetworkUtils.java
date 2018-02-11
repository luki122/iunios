package com.privacymanage.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
	public final static int NET_TYPE_NONE = -1;
	public final static int NET_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
	public final static int NET_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
	
    public static boolean isConn(Context context){   
    	if(context == null){
    		return true;
    	}
    	
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network!=null){
        	return network.isAvailable();
        }else{
        	return false;
        }      
    }
    
    /**
     * 判断当前的联网状态
     * @param context
     * @return NET_TYPE_NONE，NET_TYPE_WIFI，NET_TYPE_MOBILE
     */
    public static int getNetState(Context context){   
    	if(context == null){
    		return NET_TYPE_NONE;
    	}
    	
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = conManager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {  
            return NET_TYPE_NONE;  
        }  
        
        if(networkinfo.getType()  == ConnectivityManager.TYPE_WIFI){
        	return NET_TYPE_WIFI;
        }else if(networkinfo.getType() == ConnectivityManager.TYPE_MOBILE){
        	return NET_TYPE_MOBILE;
        }else{
        	return NET_TYPE_NONE;
        }
    }
    
    /**
     * 判断wifi是否打卡
     * @param context
     * @return
     */
    public static boolean checkNetworkOfWifiConnection(Context context){   
    	if(context == null){
    		return false;
    	}
        ConnectivityManager connMgr = (ConnectivityManager)context.
    		   getSystemService(Context.CONNECTIVITY_SERVICE);   
        android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);     
  
        if(wifi.isAvailable())   
           return true;   
        else  
           return false;   
    }  
    
    /**
     * 判断sim卡网络是否打卡
     * @param context
     * @return
     */
    public static boolean checkNetworkOfMobileConnection(Context context){ 
    	if(context == null){
    		return false;
    	}
        ConnectivityManager connMgr = (ConnectivityManager)context.
        		getSystemService(Context.CONNECTIVITY_SERVICE);   

        android.net.NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   
  
        if(mobile.isAvailable())   
           return true;   
        else  
           return false;   
    } 
    
    /**
     * @param context
     */
    public static void setNetwork(Context context){
        Intent intent=null;
        if(android.os.Build.VERSION.SDK_INT>10){
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        }else{
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        context.startActivity(intent);
    }	
}