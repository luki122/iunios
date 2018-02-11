package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.base.compare.BinarySearch;
import com.base.compare.ISearchable;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file International_Info.java
 * 摘要:洲际信息
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class International_Info 
{
	/**
	 * 洲名称
	 */
	private String iName;
	/**
	 * 国家信息
	 */
//	private List< Country_Info > iCountryInfo = new ArrayList< Country_Info >();
	private Country_Info[] iCountryInfo;
	
	public Country_Info[] getiCountryInfo() {
		return iCountryInfo;
	}

	public void setiCountryInfo(Country_Info[] iCountryInfo) {
		this.iCountryInfo = iCountryInfo;
	}
	public International_Info(DataInputStream aDis) throws Exception
	{
//		iName = aDis.readUTF();
//		int len = aDis.readUnsignedShort();
//		for(int i = 0; i < len; i++)
//		{
//			iCountryInfo.add(new Country_Info(aDis));
//		}
		iName = aDis.readUTF();
		int len = aDis.readUnsignedShort();
		iCountryInfo = new Country_Info[len];
		for(int i = 0; i < len; i++)
		{
			iCountryInfo[i] = new Country_Info(aDis);
		}
	}
	
	/**
	 * 根据国际区号查找国家信息
	 * @param aSearch
	 * @param aTelNum
	 * @return
	 */
	public List<Country_Info> searchCountryInfo(ISearchable aSearch, String aTelNum)
	{
		List<Country_Info> list = new ArrayList< Country_Info >();
		List<Integer> indexList = NumAreaManager.searchInfo(aSearch, iCountryInfo, aTelNum, 0);
		/*由于同一区号可能对应不同国家.所以需要用List来装载所有的结果*/
		if(indexList != null)
		{
			for(Integer index : indexList)
			{
				list.add(iCountryInfo[index]);
			}
			return list;
		}
		else
			return null;
	}

	/**
	 * @return the iName
	 */
	public String getiName()
	{
		return iName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "International_Info## name=" + iName;
	}


	
	
}
