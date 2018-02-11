package com.gionee.aora.numarea.data;

import java.io.DataInputStream;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file Common_Info.java
 * 摘要:常用号码信息
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
public class Common_Info implements Comparable< String >
{
	/**
	 * 号码
	 */
	private String iTelNum;
	/**
	 * 名称
	 */
	private String iName;
	/**
	 * 组名称
	 */
	private String iGroupName;
	
	public Common_Info(String aGroupName,String aTelNum,String aName)
	{
		iGroupName = aGroupName;
		iTelNum = aTelNum;
		iName = aName;
//		NumAreaManager.LOG.print(this);
	}
	
	

	/**
	 * @return the iTelNum
	 */
	public String getiTelNum()
	{
		return iTelNum;
	}



	/**
	 * @return the iName
	 */
	public String getiName()
	{
		return iName;
	}



	/**
	 * @return the iGroupName
	 */
	public String getiGroupName()
	{
		return iGroupName;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Common_Info## groupname=" + iGroupName + ",telnum=" + iTelNum + ",name=" + iName;
	}

	@Override
	public int compareTo(String another)
	{
		// TODO Auto-generated method stub
		if(iTelNum.equals(another))
			return 0;
		else 
			return 1;
		
	}
	
	
}
