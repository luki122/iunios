package com.base.compare;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file SequentialSearch.java
 * 摘要:顺序搜索
 *
 * @author yewei
 * @data 2011-6-8
 * @version 
 *
 */
public class SequentialSearch implements ISearchable
{

	@Override
	public int search(Comparable[] aData , Comparable aKey, int aStart)
	{
		// TODO Auto-generated method stub
		for(int i = aStart; i < aData.length; i++)
		{
			if(aData[i].compareTo(aKey) == 0)
				return i;
		}
		return -1;
	}

}
