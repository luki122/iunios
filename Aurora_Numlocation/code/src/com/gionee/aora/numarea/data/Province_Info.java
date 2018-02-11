package com.gionee.aora.numarea.data;
import java.io.DataInputStream;
/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file Province_Info.java
 * 摘要:省份信息
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Province_Info implements Comparable< Integer >
{
	/**
	 * 省份ID
	 */
	private int iProvinceId;
	/**
	 * 城市个数
	 */
	private int iCityCount;
	/**
	 * 省份名称
	 */
	private String iProvinceName;
	
	public Province_Info(DataInputStream aDis) throws Exception
	{
		iProvinceId = aDis.readUnsignedByte();
		iProvinceName = aDis.readUTF();
		iCityCount = aDis.readUnsignedByte();
	}
	
	

	/**
	 * @return the iProvinceId
	 */
	public int getProvinceId()
	{
		return iProvinceId;
	}



	/**
	 * @return the iCityCount
	 */
	public int getCityCount()
	{
		return iCityCount;
	}



	/**
	 * @return the iProvinceName
	 */
	public String getProvinceName()
	{
		return iProvinceName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Province_Info## provinceid=" + iProvinceId + ",name=" + iProvinceName + ",count=" + iCityCount;
	}

	@Override
	public int compareTo(Integer another)
	{
		// TODO Auto-generated method stub
		if(iProvinceId < another)
			return -1;
		else if(iProvinceId > another)
			return 1;
		else
			return 0;
	}
	
	
}
