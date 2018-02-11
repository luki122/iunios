package com.gionee.aora.numarea.export;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file NumAreaInfo.java
 * 摘要:归属地数据信息
 *
 * @author yewei
 * @data 2011-5-21
 * @version v1.0.0
 *
 */
public class NumAreaInfo implements Parcelable, Comparable< String >
{
	/**
	 * 基本信息,
	 * 手机号码&国内号码:城市信息,国际号码:国家信息,通用号码:通用信息
	 * 如1350297:深圳,0755-22331234:深圳,001-23423242:美国,110:警匪
	 * Number
	 */
	private String iBaseInfo;
	/**
	 * 头信息,
	 * 手机号码&国内号码:省份信息,国际号码:洲信息,通用号码:通用信息种类
	 * 如1350297:广东,0755-22331234:广东,001-23423242:北美洲,110:特种服务
	 * Name
	 */
	private String iHeadInfo;
	/**
	 * 额外信息,
	 * 手机号码&国内号码:运营商信息,国际号码:无,通用号码:无
	 * 如1350297:移动,0755-22331234:无,001-23423242:无,110:无
	 * Flag
	 */
	private String iExtraInfo;
	private String iTag;
	public String getiTag()
	{
		return iTag;
	}
	public void setiTag(String iTag)
	{
		this.iTag = iTag;
	}

	/**
	 * 内容
	 */
	private Context iContext;
	/**
	 * 构造方法
	 */
	public NumAreaInfo(){
		
	}
	/**
	 * 构造方法
	 * param aContext
	 */
	public NumAreaInfo(Context aContext)
	{
		// TODO Auto-generated constructor stub
		iContext = aContext;
	}
	

	
	/**
	 * @return the iBaseInfo
	 */
	public String getiBaseInfo()
	{
		return iBaseInfo;
	}



	/**
	 * @param iBaseInfo the iBaseInfo to set
	 */
	public void setiBaseInfo(String iBaseInfo)
	{
		this.iBaseInfo = iBaseInfo;
	}



	/**
	 * @return the iHeadInfo
	 */
	public String getiHeadInfo()
	{
		return iHeadInfo;
	}



	/**
	 * @param iHeadInfo the iHeadInfo to set
	 */
	public void setiHeadInfo(String iHeadInfo)
	{
		this.iHeadInfo = iHeadInfo;
	}



	/**
	 * @return the iExtraInfo
	 */
	public String getiExtraInfo()
	{
		return iExtraInfo;
	}



	/**
	 * @param iExtraInfo the iExtraInfo to set
	 */
	public void setiExtraInfo(String iExtraInfo)
	{
		this.iExtraInfo = iExtraInfo;
	}



	private NumAreaInfo(Parcel in)
	{
		iHeadInfo = in.readString();
		iBaseInfo = in.readString();
		iExtraInfo = in.readString();
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		StringBuffer result = new StringBuffer();
		if(iHeadInfo != null){
			result.append(iHeadInfo);
	}
		if(iBaseInfo != null){
			result.append(iBaseInfo);
	}
		if (iBaseInfo==null){
			return null;
		}
		if(iExtraInfo != null){
			System.out.println("3");
			result.append(iExtraInfo);
	}
		if(result.length() > 0){
			return result.toString();
}
		else{
			return null;
			}
	}



	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest , int flags)
	{
		// TODO Auto-generated method stub
		dest.writeString(iHeadInfo);
		dest.writeString(iBaseInfo);
		dest.writeString(iExtraInfo);
	}
	
	public static final Parcelable.Creator< NumAreaInfo > CREATOR = new Parcelable.Creator< NumAreaInfo >()
	{
		public NumAreaInfo createFromParcel(Parcel source)
		{
			return new NumAreaInfo(source);
		}

		public NumAreaInfo[] newArray(int size)
		{
			return new NumAreaInfo[ size ];
		}
	};
	@Override
	public int compareTo(String another)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	

}
