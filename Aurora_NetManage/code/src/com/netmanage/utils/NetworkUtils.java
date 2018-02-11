package com.netmanage.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import android.telephony.SubscriptionManager;

public class NetworkUtils {
	public final static int NET_TYPE_NONE = -1;
	public final static int NET_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
	public final static int NET_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
	
	/**
	 * 设置数据连接的状态
	 * @param enabled true：打开 ； false：关闭
	 */
	public static void setMobileDataEnabled(Context context,boolean enabled){
		if(context == null){
			return ;
		}
		try {
            ITelephony iTel = ITelephony.Stub.asInterface(
                    ServiceManager.getService(Context.TELEPHONY_SERVICE));

            if (null == iTel) {
                Log.i("NetworkUtils", "Can not get phone service");
                return;
            }

//            iTel.setDataEnabled(enabled);
            iTel.setDataEnabled(SubscriptionManager.getDefaultDataSubId(), enabled);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    } 
	
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
    
    /**
     * （所有应用）获取通过Mobile连接收到的字节总数
     * Return number of bytes received across mobile networks since device boot.
     * @return
     */
    public static long getMobileRxBytes(){  
        return TrafficStats.getMobileRxBytes()==TrafficStats.UNSUPPORTED?
        		0:(TrafficStats.getMobileRxBytes());    
    } 
    
    /**
     * （所有应用）获取通过Mobile连接发送字节总数
     * Return number of bytes transmitted across mobile networks since device boot.
     * @return
     */
    public static long getMobileTxBytes(){  
        return TrafficStats.getMobileTxBytes()==TrafficStats.UNSUPPORTED?
        		0:(TrafficStats.getMobileTxBytes());    
    } 
    
    /**
     * （所有应用）获取总的接受字节数，包含Mobile和WiFi等    
     * Return number of bytes received since device boot.
     * @return
     */
    public static long getTotalRxBytes(){
        return TrafficStats.getTotalRxBytes()==TrafficStats.UNSUPPORTED?
        		0:(TrafficStats.getTotalRxBytes());    
    }    
    
    /**
     * （所有应用）总的发送字节数，包含Mobile和WiFi等 
     * Return number of bytes transmitted since device boot.
     * @return
     */
    public static long getTotalTxBytes(){   
        return TrafficStats.getTotalTxBytes()==TrafficStats.UNSUPPORTED?
        		0:(TrafficStats.getTotalTxBytes());    
    } 
    
    /**
     * （所有应用）总的发送字节数 与 总的接受字节数 之合 （包含Mobile和WiFi等）
     * Return number of bytes transmitted and received since device boot.
     * @return
     */
    public static long getTotalTxAndRxBytes(){
    	return getTotalRxBytes()+getTotalTxBytes();
    }
    
    /**
     * （所有应用）总的发送字节数 与 总的接受字节数 之合 （包含Mobile）
     * Return number of bytes transmitted and received since device boot.
     * @return
     */
    public static long getMobileTxAndRxBytes(){
    	return getMobileRxBytes()+getMobileTxBytes();
    }
    
    /**
     * 获取某一个进程总的接受字节数，包含Mobile和WiFi等    
     * Return number of bytes received by the given UID since device boot.
     * @param uid
     * @return
     */
    public static long getUidRxBytes(int uid){
    	return TrafficStats.getUidRxBytes(uid)==TrafficStats.UNSUPPORTED?
        		0:(TrafficStats.getUidRxBytes(uid));  
    }
    
    /**
     * 获取某一个进程总的发送字节数，包含Mobile和WiFi等    
     * Return number of bytes transmitted by the given UID since device boot.
     * @param uid
     * @return
     */
    public static long getUidTxBytes(int uid){
    	return TrafficStats.getUidTxBytes(uid)==TrafficStats.UNSUPPORTED?
        		0:(TrafficStats.getUidTxBytes(uid));  
    }
   
	
}
