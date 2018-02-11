package com.base.compare;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file ISearchable.java
 * 摘要:查询接口
 *
 * @author yewei
 * @data 2011-6-3
 * @version 
 *
 * @param <T>
 */
public interface ISearchable<T extends Comparable< T >>
{
	/**
	 * 开始查找
	 * @param aData
	 * 要查找的数据源
	 * @param aKey
	 * 要找到的数据
	 * @param start
	 * 从数据源的哪里开始找
	 * @return
	 * 要找数据在数据源中的位置,没有则为-1
	 */
	public int search(T[] aData,T aKey,int start);
}
