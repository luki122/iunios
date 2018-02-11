package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file Mobile_Info.java
 * 摘要:归属地信息,手机号码第四到第七位
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Mobile_Info implements Comparable< Integer >
{
	/**
	 * 手机号码前7位的后四位
	 */
	private int iMobile_Info_Num;
	/**
	 * 城市ID
	 */
	private int iCityId;
	/**
	 * 连续号码个数
	 */
	private int iContinuNumCount;
	
	public Mobile_Info(DataInputStream aDis) throws Exception
	{
		iMobile_Info_Num = aDis.readUnsignedShort();
		iCityId = aDis.readUnsignedShort();
		iContinuNumCount = aDis.readUnsignedByte();
//		NumAreaManager.LOG.print(this);
	}
	
	

	/**
	 * @return the iMobile_Info_Num
	 */
	public int getiMobile_Info_Num()
	{
		return iMobile_Info_Num;
	}



	/**
	 * @return the iCityId
	 */
	public int getiCityId()
	{
		return iCityId;
	}



	/**
	 * @return the iContinuNumCount
	 */
	public int getiContinuNumCount()
	{
		return iContinuNumCount;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Mobile_Info## num=" + iMobile_Info_Num + ",city_id=" + iCityId + ",continuNum=" + iContinuNumCount;
	}

	@Override
	public int compareTo(Integer another)
	{
		// TODO Auto-generated method stub
		int compareValue;
		int result = -1;
		for(int i = 0; i < iContinuNumCount; i++)
		{
			compareValue = iMobile_Info_Num + i;
			if(compareValue < another)
				result = -1;
			else if(compareValue > another)
				result = 1;
			else
			{
				return 0;
			}
		}
		return result;
	}
	
	
}
