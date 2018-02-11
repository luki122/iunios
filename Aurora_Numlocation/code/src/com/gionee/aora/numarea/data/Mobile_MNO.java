package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.base.compare.BinarySearch;
import com.base.compare.ISearchable;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file Mobile_MNO.java
 * 摘要:运营商信息,手机号码前三位
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
class Mobile_MNO implements Comparable< Integer >
{
	/**
	 * 手机号码前3位
	 */
	private int iMobile_Head_Num;
	/**
	 * 运营商
	 */
	private String iMNO_Name;
	/**
	 * 归属地信息
	 */
//	private List< Mobile_Info > iMobileInfo = new ArrayList< Mobile_Info >();
	private Mobile_Info[]  iMobileInfo;
	/**
	 * 构造方法
	 * @param aDis
	 * @throws Exception
	 */
	public Mobile_MNO(DataInputStream aDis) throws Exception
	{
		iMobile_Head_Num = aDis.readUnsignedByte();
		iMNO_Name = aDis.readUTF();
//		int len = aDis.readInt();
//		for(int i = 0; i < len; i++)
//		{
//			iMobileInfo.add(new Mobile_Info(aDis));
//		}
		int len = aDis.readInt();
		iMobileInfo = new Mobile_Info[len];
		for(int i = 0; i < len; i++)
		{
			iMobileInfo[i] = new Mobile_Info(aDis);
		}
	}

	/**
	 * @return the iMobile_Head_Num
	 */
	public int getMobile_Head_Num()
	{
		return iMobile_Head_Num;
	}

	/**
	 * @return the iMNO_Name
	 */
	public String getMNO_Name()
	{
		return iMNO_Name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Mobile_MNO## mno=" + iMobile_Head_Num + ",name=" + iMNO_Name;
	}

	@Override
	public int compareTo(Integer another)
	{
		// TODO Auto-generated method stub
		if(iMobile_Head_Num < another)
			return -1;
		else if(iMobile_Head_Num > another)
			return 1;
		else
			return 0;
	}
	
	/**
	 * 根据后4位的号码去查找对应的城市信息
	 * @param aSearch
	 * @param aInfo
	 * @return
	 */
	public int getAreaInfo(ISearchable aSearch , int aInfo)
	{
		int result = -1;
		int index = aSearch.search(iMobileInfo, aInfo,0);
		if(index != -1)
			result = iMobileInfo[index].getiCityId();
		return result;
	}
	
}
