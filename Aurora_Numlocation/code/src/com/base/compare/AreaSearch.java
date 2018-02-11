package com.base.compare;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;

import com.gionee.aora.numarea.data.City_Info;
import com.gionee.aora.numarea.data.Common_Info;
import com.gionee.aora.numarea.data.Province_Info;
import com.gionee.aora.numarea.data.Country_Info;
import com.gionee.aora.numarea.export.NumAreaInfo;
import com.gionee.aora.numarea.util.ALLSTATE;

/**
 * Copyright (c) 2011, ÉîÛÚÊÐ°ÂÈíÍøÂç¿Æ¼¼¹«Ë¾ÑÐ·¢²¿ All rights reserved.
 * 
 * @file AreaSearch.java ÕªÒª:×Ö·û´®Ä£ºý²éÕÒ·½·¨
 * 
 * @author jiangfan
 * @data 2011-7-6
 * @version v1.0.0
 * 
 */
public class AreaSearch
{
	/**
	 * ´æµØÇøºÍ³£ÓÃºÅÂëµÄÊý×é
	 */
	private static NumAreaInfo areaNum_Infos[];
	/**
	 * ´òÓ¡LOGµÄ¹¤¾ß
	 */
//	final static public JLog LOG = new JLog("NUM_AREA", ALLSTATE.LOG_STATE);
	/**
	 * ³£ÓÃÐÅÏ¢±ê¼Ç
	 */
	final static private int TAG_COM_INFO = 1;
	/**
	 * ¹úÄÚ³ÇÊÐÐÅÏ¢±ê¼Ç
	 */
	final static private int TAG_CITY_INFO = TAG_COM_INFO + 1;
	/**
	 * ¹ú¼ÊÐÅÏ¢±ê¼Ç
	 */
	final static private int TAG_INTERNATIONAL = TAG_CITY_INFO + 1;

	/**
	 * ¹¹Ôì·½·¨
	 */
	public AreaSearch()
	{
//		LOG.print("��ʼ��");
//		LOG.print("AreaSearch40��ʼ��");
	}

	/**
	 * ²éÕÒ·½·¨
	 * 
	 * @param Db_Date
	 * @param inputArea
	 * @param tag
	 * @return
	 */
	public static NumAreaInfo[] selectAreaNum(Comparable Db_Date[] , String inputArea , int tag)
	{
		int k = 0;
		ArrayList< Comparable > list = new ArrayList< Comparable >();
		try
		{

			for (int i = 0 ; i < Db_Date.length ; i++)
			{// ±È½Ï×Ö·û´®,½«ÊäÈëµÄ×Ö·û´®ÓëÊý¾Ý¿â½øÐÐÄ£ºý¶Ô±È
				Pattern p = Pattern.compile(inputArea);
				Matcher m = p.matcher(Db_Date[i].toString());
				if (m.find() == true)
				{
					list.add(Db_Date[i]);// ½«´æÔÚ·ûºÏÊý¾ÝµÄ´æÔÚÒ»¸ölistÖÐ
					k++;
				}
			}
		}
		catch(Exception e)
		{
			// TODO: handle exception
//			LOG.print("AreaSearch69�쳣");
			k = 0;
		}
		if (k > 0)
		{// ���ԱȲ������ݲ��ǿյ�
			Comparable Area_Num_Strings[] = new Comparable[ k ];
			for (int i = 0 ; i < Area_Num_Strings.length ; i++)
			{
				Area_Num_Strings[i] = list.get(i);
			}
			Comparable temp = null;
			for (int i = 0 ; i < Area_Num_Strings.length ; i++)
			{
				for (int j = i ; j < Area_Num_Strings.length ; j++)
				{// ¶ÔÊý¾Ý½øÐÐÅÅÐò£¬Ô½ÊÇºÍÊäÈë×Ö·ûÆ¥ÅäµÄ£¬ÔÚÇ°ÃæµÄÎ»ÖÃ

					if (Area_Num_Strings[i].toString().indexOf(inputArea) > Area_Num_Strings[j].toString().indexOf(inputArea))
					{

						// ·½·¨Ò»£º
						temp = Area_Num_Strings[i];
						Area_Num_Strings[i] = Area_Num_Strings[j];
						Area_Num_Strings[j] = temp;
						/*
						 * //·½·¨¶þ: targetArr[i] = targetArr[i] + targetArr[j];
						 * targetArr[j] = targetArr[i] - targetArr[j];
						 * targetArr[i] = targetArr[i] - targetArr[j];
						 */
					}

				}
			}
			if (tag == TAG_COM_INFO)
			{// Èç¹û²éÑ¯µÄÊÇ³£ÓÃºÅÂë
				areaNum_Infos = new NumAreaInfo[ Area_Num_Strings.length ];
//				LOG.print("Area_Num_Strings" + Area_Num_Strings.length);
				for (int i = 0 ; i < Area_Num_Strings.length ; i++)
				{
					Common_Info common_Info = (Common_Info) Area_Num_Strings[i];
					NumAreaInfo areaNum_Info = new NumAreaInfo();
					areaNum_Info.setiHeadInfo(common_Info.getiName());
					areaNum_Info.setiBaseInfo(common_Info.getiTelNum());
					areaNum_Info.setiTag("1");
					areaNum_Infos[i] = areaNum_Info;
//					LOG.print(common_Info.getiName() + "CommSearch94" + "Area_Num_Strings.length" + Area_Num_Strings.length);
					// LOG.print(
					// "---------------------222"+"**"+i+"**"+common_Info.getiName()+common_Info.getiTelNum());
				}
//				LOG.print("here******************************************" + inputArea);
				selectAreaNum(areaNum_Infos, inputArea, 4);// ������һ�Σ����ǲ�����1-3������
				return areaNum_Infos;
			}// Èç¹û²éÑ¯µÄÊÇ³ÇÊÐ
			else if (tag == TAG_CITY_INFO)
			{
				areaNum_Infos = new NumAreaInfo[ Area_Num_Strings.length ];
				for (int j = 0 ; j < Area_Num_Strings.length ; j++)
				{
					City_Info city_Info = (City_Info) Area_Num_Strings[j];
					NumAreaInfo areaNum_Info = new NumAreaInfo();
					areaNum_Info.setiHeadInfo(city_Info.getiCityName());
					areaNum_Info.setiBaseInfo(city_Info.getiAreaCode());
					areaNum_Info.setiTag("2");
					areaNum_Infos[j] = areaNum_Info;
//					LOG.print(city_Info.getiCityName() + "CitySearch108" + "Area_Num_Strings.length" + Area_Num_Strings.length);
					// LOG.print(
					// "---------------------city111info****"+city_Info.toString());
				}
				return areaNum_Infos;
			}
			else if (tag == TAG_INTERNATIONAL)
			{
//				LOG.print("��ã������ǲ�ѯ��ʳ�����Ϣ" + Area_Num_Strings.length + Area_Num_Strings[0]);
             areaNum_Infos = new NumAreaInfo[ Area_Num_Strings.length ];
				for (int i = 0 ; i < Area_Num_Strings.length ; i++)
				{
					Country_Info country_Info = (Country_Info) Area_Num_Strings[i];
					NumAreaInfo areaNum_Info = new NumAreaInfo();
					areaNum_Info.setiHeadInfo(country_Info.getiCountryName());
					areaNum_Info.setiTag("3");
					areaNum_Info.setiBaseInfo(country_Info.getiTelCode());
					areaNum_Infos[i] = areaNum_Info;
//					LOG.print("876544412321321-------------" + areaNum_Infos[i]);
//					LOG.print(country_Info.getiCountryName() + "country_Info" + "Area_Num_Strings.length" + Area_Num_Strings.length);
				}
				return areaNum_Infos;
			}
			else
			{

				return areaNum_Infos;// ÕâÀïÊÇ4ºÅÇé¿ö·µ»ØµÄµØ·½°É¡£
			}
		}
		return null;// ·µ»ØÒ»¸öÊý×é
	}
}
