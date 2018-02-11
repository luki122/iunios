package com.aurora.util;

import java.util.ArrayList;
import android.os.SystemProperties;
import android.util.Log;

public  class DeviceProperties {
	
    public enum DeviceCategories {
    	DEFAULT_DEVICE_PROPERTIES, IUNI_DEVICE_PROPERTIES, SAMSUNG_DEVICE_PROPERTIES,
    	XIAOMI_DEVICE_PROPERTIES
    }
	
	/**start expand it */
//	public static final int DEFAULT_DEVICE_PROPERTIES = 0;
//	public static final DeviceCategories IUNI_DEVICE_PROPERTIES = 1;
//	public static final int SAMSUNG_DEVICE_PROPERTIES = 2;
//	public static final int XIAOMI_DEVICE_PROPERTIES = 3;
	
	public static String strDeviceProperties;
	
	public static boolean needScale = false;
	public static ArrayList<String> ARRAY_LIST = new ArrayList<String>();
	
	 static {
		 strDeviceProperties =  SystemProperties.get("ro.product.device", "");
		 InitDevicesArrayList();
		 String strDensityProperties = SystemProperties.get("ro.sf.lcd_density", "160");
		 if(strDeviceProperties!=null&&strDeviceProperties.toUpperCase().contains("IUNI")){
			
			 if(strDensityProperties.equals("480")){
				 needScale = false;
			 }else{
				 needScale = ARRAY_LIST.contains(strDeviceProperties);
			 }
		 }else if(strDensityProperties.equals("320")||strDensityProperties.equals("480")){
			 needScale = true;
		 }else{
			 needScale = ARRAY_LIST.contains(strDeviceProperties);
		 }
	 }
	
	/**
	 * @return will return default, or iuni or samsung or xiaomi devices number
	 * */
	public static DeviceCategories getSysProductDeviceName() {
		if (strDeviceProperties.toUpperCase().contains("IUNI")) {
			Log.e("linp","~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~getSysProductDeviceName called strDeviceProperties="+strDeviceProperties);
			return DeviceCategories.IUNI_DEVICE_PROPERTIES;
		} else if (isNeedScale()) {
			return DeviceCategories.SAMSUNG_DEVICE_PROPERTIES;
		} else if (strDeviceProperties.equals("cancro")) {
			return DeviceCategories.XIAOMI_DEVICE_PROPERTIES;
		}
		return DeviceCategories.DEFAULT_DEVICE_PROPERTIES;
	}
	
	public static String getSysProductDevicePrefixName(){
		/**M:Hazel start to modify for adjust all name's contains IUNI to default*/
		if(strDeviceProperties.toUpperCase().contains("IUNI")){
			Log.e("linp","-------------------------->getSysProductDevicePrefixName start to convert contains iuni to default");
			strDeviceProperties = "iuni";
		}
		return strDeviceProperties.toLowerCase().trim();
	}
	
	/**Init Devices Array List. support samsung devices only */
	private static void InitDevicesArrayList() {
		if (ARRAY_LIST.size() <= 0) {
			ARRAY_LIST.add("hlte");
			ARRAY_LIST.add("ha3g");
			ARRAY_LIST.add("A0001");
			ARRAY_LIST.add("X9000");
			ARRAY_LIST.add("X9007");
		//	ARRAY_LIST.add("m0"); //ht 2014-10-11 适配S3 I9300
		}
	}
	
	 public static boolean isNeedScale() {
		 return needScale;
	 }


}
