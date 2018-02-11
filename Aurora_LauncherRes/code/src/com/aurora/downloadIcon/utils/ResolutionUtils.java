package com.aurora.downloadIcon.utils;

import android.util.DisplayMetrics;

public class ResolutionUtils {
	private static final String RESOLUTION_LOW = "ldpi";
	private static final String RESOLUTION_MEDIUM = "mdpi";
	private static final String RESOLUTION_HIGH = "hdpi";
	private static final String RESOLUTION_XHIGH = "xhdpi";
	private static final String RESOLUTION_XXHIGH = "xxhdpi";
	private static final String RESOLUTION_XXXHIGH = "xxxhdpi";
	public static String DEVICE_RESOLUTION = "xxhdpi";
	
	public static String getResolution(int density){
		String ret = null;
		switch (density) {
		case DisplayMetrics.DENSITY_LOW:
			ret = RESOLUTION_LOW;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			ret = RESOLUTION_MEDIUM;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			ret = RESOLUTION_HIGH;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			ret = RESOLUTION_XHIGH;
			break;
		case DisplayMetrics.DENSITY_XXHIGH:
			ret = RESOLUTION_XXHIGH;
			break;
		case DisplayMetrics.DENSITY_XXXHIGH:
			ret = RESOLUTION_XXXHIGH;
			break;
		default:
			ret = RESOLUTION_HIGH;
			break;
		}
		return ret;
	}
	
	public static float iconScale(String device_resolution,String icon_resolution){
		Log.i("test1", "device_resolution = "+device_resolution+"   ,icon_resolution = "+icon_resolution);
		int density_device = changeResolution2density(device_resolution);
		int density_icon = changeResolution2density(icon_resolution);
		return (float)((float)density_device/(float)density_icon);
	}
	
	public static int changeResolution2density(String resolution){ 
		int ret = 320;
		if(RESOLUTION_LOW.equals(resolution)){
			ret = DisplayMetrics.DENSITY_LOW;
		}else if(RESOLUTION_MEDIUM.equals(resolution)){
			ret = DisplayMetrics.DENSITY_MEDIUM;
		}else if(RESOLUTION_HIGH.equals(resolution)){
			ret = DisplayMetrics.DENSITY_HIGH;
		}else if(RESOLUTION_XHIGH.equals(resolution)){
			ret = DisplayMetrics.DENSITY_XHIGH;
		}else if(RESOLUTION_XXHIGH.equals(resolution)){
			ret = DisplayMetrics.DENSITY_XXHIGH;
		}else if(RESOLUTION_XXXHIGH.equals(resolution)){
			ret = DisplayMetrics.DENSITY_XXXHIGH;
		}
		return ret;
	}

	public static String getDEVICE_RESOLUTION() {
		return DEVICE_RESOLUTION;
	}

	public static void setDEVICE_RESOLUTION(String resolution) {
		DEVICE_RESOLUTION = resolution;
	}
}
