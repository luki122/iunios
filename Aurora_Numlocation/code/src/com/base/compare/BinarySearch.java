package com.base.compare;


/**
 * Copyright (c) 2001, ÉîÛÚÊÐ°ÂÈíÍøÂç¿Æ¼¼¹«Ë¾ÑÐ·¢²¿
 * All rights reserved.
 *
 * @file BinarySearch.java
 * ÕªÒª:¶þ·Ö²éÕÒËã·¨
 *
 * @author yewei
 * @data 2011-6-3
 * @version 
 *
 */
public class BinarySearch implements ISearchable
{

	public int search(Comparable[] aData , Comparable aKey , int start)
	{
		int low;
		int high;
		int mid;

		if (aData == null) return -1;

		low = start;
		high = aData.length - 1;

		while (low <= high)
		{
			mid = ( low + high ) / 2;

			if (aData[mid].compareTo(aKey) > 0)
			{
				high = mid - 1;
			}
			else if (aData[mid].compareTo(aKey) < 0)
			{
				low = mid + 1;
			}
			else if (aData[mid].compareTo(aKey) == 0)
			{
				return mid;
			}
		}

		return -1;
	}



}
