package com.gionee.aora.numarea.util;

import com.gionee.aora.numarea.export.NumAreaInfo;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Copyright (c) 2011, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file AreaSearch.java
 * 摘要:全局变量
 *
 * @author jiangfan
 * @data 2011-7-6
 * @version v1.0.0
 *
 */
public class ALLSTATE {
	/**
	 * 屏幕X坐标
	 */
	public static int XPOINT=0;
	/**
	 * 屏幕y坐标
	 */
	public static int YPOINT=0;
	/**
	 * 全局listview
	 */
	public  static ListView LISTVIEW=null;
	/**
	 * 常用电话号码信息标签
	 */
	public static String COMMON_TAG="1";
	/**
	 * 国内城市信息号码标签
	 */
	public static String CITY_TAG="2";
	/**
	 * 国际城市区号
	 */
	public static String INTERNTION_TAG="3";
	/**
	 * 如果输入的是电话号码，查出的便是城市--标签
	 */
	public static String NUM_AREA_TAG="5";
	/**
	 * 查询结果为空
	 */
	public static String NULL_TAG="6";
	/**
	 * widget上面显示的字符
	 */
	public static String WIDGET_TAG1="1";
	public static TextView textView=null;
	public static TextView textView1=null;
	public static NumAreaInfo AREANUM_INFO1=null;
	public static NumAreaInfo AREANUM_INFO2=null;
	public static NumAreaInfo AREANUM_INFO3=null;
	public static boolean WIDGET_TAG=false;
	public static boolean LOG_STATE=true;
}
