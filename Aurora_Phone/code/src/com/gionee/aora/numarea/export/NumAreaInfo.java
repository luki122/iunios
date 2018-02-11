package com.gionee.aora.numarea.export;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file NumAreaInfo.java
 * ժҪ:�����������Ϣ
 *
 * @author yewei
 * @data 2011-5-21
 * @version v1.0.0
 *
 */
public class NumAreaInfo implements Parcelable, Comparable< String >
{
	/**
	 * ����Ϣ,
	 * �ֻ����&���ں���:������Ϣ,��ʺ���:�����Ϣ,ͨ�ú���:ͨ����Ϣ
	 * ��1350297:����,0755-22331234:����,001-23423242:����,110:����
	 * Number
	 */
	private String iBaseInfo;
	/**
	 * ͷ��Ϣ,
	 * �ֻ����&���ں���:ʡ����Ϣ,��ʺ���:����Ϣ,ͨ�ú���:ͨ����Ϣ����
	 * ��1350297:�㶫,0755-22331234:�㶫,001-23423242:������,110:���ַ���
	 * Name
	 */
	private String iHeadInfo;
	/**
	 * ������Ϣ,
	 * �ֻ����&���ں���:��Ӫ����Ϣ,��ʺ���:��,ͨ�ú���:��
	 * ��1350297:�ƶ�,0755-22331234:��,001-23423242:��,110:��
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
	 * ����
	 */
	private Context iContext;
	/**
	 * ���췽��
	 */
	public NumAreaInfo(){
		
	}
	/**
	 * ���췽��
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
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer result = new StringBuffer();
		if (iHeadInfo != null) {
			result.append(iHeadInfo);
		}
		if (iBaseInfo != null) {
			result.append(iBaseInfo);
		//Gionee:wangtaihu 20110902 add for CR00344497 begin
		} else {
			return null;
		//Gionee:wangtaihu 20110902 add end
		}

		if (iExtraInfo != null) {
			System.out.println("3");
			result.append(iExtraInfo);
		}
		if (result.length() > 0) {
			return result.toString();
		} else {
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
