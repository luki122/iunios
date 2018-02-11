package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file Country_Info.java
 * 摘要:国际区号信息
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Country_Info implements Comparable< String >
{
	/**
	 * 国家名字
	 */
	private String iCountryName;
	/**
	 * 国际区号
	 */
	private String iTelCode;
	
	public Country_Info(DataInputStream aDis) throws Exception
	{
		iCountryName = aDis.readUTF();
		iTelCode = aDis.readUTF();
	}

	
	
	/**
	 * @return the iCountryName
	 */
	public String getiCountryName()
	{
		return iCountryName;
	}



	/**
	 * @return the iTelCode
	 */
	public String getiTelCode()
	{
		return iTelCode;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Country_Info## countryName=" + iCountryName + ",tel=" + iTelCode;
	}

	@Override
	public int compareTo(String another)
	{
		// TODO Auto-generated method stub
		if(iTelCode.equals(another))
			return 0;
		else 
			return 1;
	}
	
	
}
