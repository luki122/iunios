package com.gionee.aora.numarea.data;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import com.base.compare.AreaSearch;
import com.base.compare.BinarySearch;
import com.base.compare.ISearchable;
import com.base.compare.SequentialSearch;
//import com.base.datacollect.BaseDataCollectFormat;
//import com.base.datacollect.BaseDataCollect_Get;
//import com.base.log.JLog;
//import com.base.net.HttpNet;
//import com.base.net.INet;
//import com.base.net.INetListenser;
import com.gionee.aora.numarea.export.INumAreaObserver;
import com.gionee.aora.numarea.export.IUpdataResult;
import com.gionee.aora.numarea.export.NumAreaInfo;
import com.gionee.aora.numarea.export.STATE;
import com.gionee.aora.numarea.util.ALLSTATE;
import com.gionee.aora.numarea.util.Util;
// Gionee <xuhz> <2013-07-26> delete for CR00843511 begin
//import com.gionee.utils.ProductConfiguration; 
//Gionee <xuhz> <2013-07-26> delete for CR00843511 end
import android.telephony.TelephonyManager;
/**
 * Copyright (c) 2011, �����а�������Ƽ���˾�з���
 * All rights reserved.
 *
 * @file NumAreaManager.java
 * ժҪ:��������Ϣ������
 *
 * @author yewei
 * @data 2011-5-26
 * @version 
 *
 */
class NumAreaManager 
{
	/**
	 * ���DB���ļ���
	 */
	final static private String NUM_AREA_DB = "NumAreaDB";

	/**
	 * �汾��
	 */
	private int iVersion;
	private Context iContext;
	/**
	 * ��ӡLOG�Ĺ���
	 */
//	final static public JLog LOG = new JLog("NUM_AREA",  ALLSTATE.LOG_STATE);
	
	/**
	 * ������Ϣ
	 */
//	static private List<City_Info> iCity_Info = new ArrayList< City_Info >();
	static private City_Info[] iCity_Info;
	/**
	 * ������Ϣ
	 */
	static private Common_Info_List iCommon_Info_List ;
	/**
	 * �޼���Ϣ
	 */
//	static private List<International_Info> iInternational_Info = new ArrayList< International_Info >();
	static private International_Info[] iInternational_Info;
	/**
	 * �޼ʹ����Ϣ
	 */
	static private Country_Info[] country_Infos;
	/**
	 * �ֻ����ǰ��λ�Ĺ��������
	 */
//	static private List<Mobile_MNO> iMobile_MNO = new ArrayList< Mobile_MNO >();
	static private Mobile_MNO[] iMobile_MNO;
	/**
	 * ʡ����Ϣ
	 */
//	static private List< Province_Info > iProvince_Info = new ArrayList< Province_Info >();
	static private Province_Info[] iProvince_Info;
	/**
	 * ���������
	 */
	private NumAreaParser iNumParser;
	/**
	 * �������������ݵ������
	 */
	private OutputStream iOs;
	/**
	 * ��������ݿ������
	 */
//	private NumAreaDBUpdataer iDBUpdataer;
	/**
	 * DB������
	 */
	private Vector< INumAreaObserver > iListener = new Vector< INumAreaObserver >();
	/**
	 * �鳣����Ϣ�ı�־
	 */
	final static private  int TAG_COM_INFO=1;
	/**
	 * �������Ϣ�ı�־
	 */
	final static private  int TAG_CITY_INFO=TAG_COM_INFO+1;
	/**
	 * ���ҹ����Ϣ�ı�־
	 * @param aContext
	 */
	final static private int TAG_INTERNATIONAL=TAG_CITY_INFO+1;
	
	private boolean updateTag=true;
	
	public NumAreaManager(Context aContext)
	{
		iContext = aContext;
		iNumParser = new NumAreaParser();
//		iDBUpdataer = new NumAreaDBUpdataer();
//		initData();
	}
	
	/**
	 * ע��һ���۲���
	 * @param aObserver
	 */
	public boolean registObserver(INumAreaObserver aObserver)
	{
		if(!iListener.contains(aObserver))
		{
			iListener.add(aObserver);
			return true;
		}
		else
			return false;
	}
	
	/**
	 * ע��һ���۲���
	 * @param aObserver
	 * @return
	 */
	public boolean unRegistObserver(INumAreaObserver aObserver)
	{
		return iListener.remove(aObserver);
	}
	
	/**
	 * ֪ͨ���й۲��߸������
	 * @param aResultCode
	 */
	public void notifyObserver(int aResultCode)
	{
		notifyObserver(aResultCode,null);
	}
	public void notifyObserver(int aResultCode,Bundle bundle){
		for(int i = 0; i < iListener.size(); i++)
		{
			try
			{
				if(null==bundle){
					bundle=new Bundle();
				}
				//aurora modify liguangyu 20140327 start
				if(iListener.get(i) != null) {
					iListener.get(i).updata(aResultCode,bundle);
				}
				//aurora modify liguangyu 20140327 end
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
//				LOG.print("NumAreaManger.java167���쳣"+e.toString());
				e.printStackTrace();
			}
		}
	
	}
	/**
	 * ����DB�е����� 
	 * @return
	 * �Ƿ�ɹ�����
	 */
	public boolean initData()
	{
		try
		{
			readFile();
			System.out.println("�������111");
			notifyObserver(IUpdataResult.RESULT_INIT_FINISH);
			return true;
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
//			LOG.print(e.fillInStackTrace());
			notifyObserver(IUpdataResult.RESULT_ERROR_PARSE_DB_FAILD);
			iContext.deleteFile(NUM_AREA_DB);
		}
		return false;
	}
	
	/**
	 * ��ݺ����ȡ��������Ϣ
	 * @param aPhoneNum
	 * ����ĺ���
	 * @return
	 * ��������Ϣ
	 */
	public NumAreaInfo getNumAreaInfo(String aPhoneNum)
	{
		// TODO Auto-generated method stub
		return iNumParser.parsePhoneNum(aPhoneNum);//��ݴ�������ֵ���н���
	}
	/**
	 * ���������ַ��ȡ��������Ϣ
	 * @param aPhoneNum
	 * ������ַ�
	 * @param aTag
	 * ����"1"Ϊ���ú����ѯ"2"Ϊ���к����ѯ
	 * @return
	 * ��������Ϣ
	 */
	public NumAreaInfo[] getAreaNumInfo(String aArea,String aTag){
		return iNumParser.parseArea(aArea,aTag);
		
	}
	/**
	 * ������еĲ��ú�����Ϣ��Ϣ
	 * @return
	 */
	public NumAreaInfo[] getComAreaNumInfo(){
		
		NumAreaInfo areaNum_Infos[]=new NumAreaInfo[iCommon_Info_List.getiCommon_Info().length];
     	for (int j = 0; j < areaNum_Infos.length;j++) {
     		Common_Info common_Info=(Common_Info)iCommon_Info_List.getiCommon_Info()[j];
     		NumAreaInfo areaNum_Info=new NumAreaInfo();
     		areaNum_Info.setiHeadInfo(common_Info.getiName());
     		areaNum_Info.setiBaseInfo(common_Info.getiTelNum());
     		areaNum_Info.setiTag("1");
     		areaNum_Infos[j]=areaNum_Info;
     		//LOG.print(common_Info.getiName()+"getComAreaNumInfo237"+"Area_Num_Strings.length");
     	}
     	return areaNum_Infos;
		
	}
	/**
	 * ���¹��������
	 * @param aObserver
	 */
	public void updataDB(INumAreaObserver aObserver)
	{
		// TODO Auto-generated method stub
//		try
//		{
//			iDBUpdataer.updataDB(aObserver);
//		}
//		catch(Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	
	/**
	 * ȡ�����
	 */
	public void cancelUpdata()
	{
		// TODO Auto-generated method stub
//		iDBUpdataer.cancelUpdata();
	}
	
	

	/**
	 * ��ȡdb�е����
	 * @throws Exception
	 */
	private void readFile() throws Exception
	{
		InputStream is;
		if(iContext.getFileStreamPath(NUM_AREA_DB).exists())
		{
			is = iContext.openFileInput(NUM_AREA_DB);
//			LOG.print("create stream by download db");
		}
		else
		{
			is = iContext.getAssets().open(NUM_AREA_DB);
//			LOG.print("create stream by asset db");
		}
		DataInputStream dis = new DataInputStream(is);
		iVersion = dis.readInt();
//		NumAreaManager.LOG.print("version=" + iVersion);
		
		long time = System.currentTimeMillis();
		parseMobile(dis);
//		LOG.print("parseMobile use time = " + (System.currentTimeMillis() - time));
		
		time = System.currentTimeMillis();
		parseProvince(dis);
//		LOG.print("parseProvince use time = " + (System.currentTimeMillis() - time));
		
		time = System.currentTimeMillis();
		parseCity(dis);
//		LOG.print("parseCity use time = " + (System.currentTimeMillis() - time));
		
		time = System.currentTimeMillis();
		parseInternational(dis);
//		LOG.print("parseCity use time = " + (System.currentTimeMillis() - time));
		
		time = System.currentTimeMillis();
		parseCommon(dis);
//		LOG.print("parseCommon use time = " + (System.currentTimeMillis() - time));
		is.close();
	}
	/**
	 * �����ֻ�����������Ϣ
	 * @param aDis
	 * @throws Exception 
	 */
	private void parseMobile(DataInputStream aDis) throws Exception
	{
//		int len = aDis.readByte();
//		for(int i = 0; i < len; i++)
//		{
//			Mobile_MNO mno_info = new Mobile_MNO(aDis);
//			LOG.print(mno_info);
//			iMobile_MNO.add(mno_info);
//		}
		int len = aDis.readByte();
//		LOG.print("parseMobile^^^**$$%##@#@"+len);
		iMobile_MNO = new Mobile_MNO[len];
		for(int i = 0; i < len; i++)
		{
			Mobile_MNO mno_info = new Mobile_MNO(aDis);
//			LOG.print(mno_info);
//			iMobile_MNO.add(mno_info);
			iMobile_MNO[i] = mno_info;
		}		
	}
	
	/**
	 * ����ʡ����Ϣ
	 * @param aDis
	 * @throws Exception 
	 */
	private void parseProvince(DataInputStream aDis) throws Exception
	{
//		LOG.print("parseProvince");
//		int len = aDis.readByte();
//		for(int i = 0; i < len; i++)
//		{
//			Province_Info province_info = new Province_Info(aDis);
//			LOG.print(province_info);
//			iProvince_Info.add(province_info);
//		}
		int len = aDis.readByte();
		iProvince_Info = new Province_Info[len];
		for(int i = 0; i < len; i++)
		{
			Province_Info province_info = new Province_Info(aDis);
//			LOG.print(province_info);
			iProvince_Info[i] = province_info;
		}
	}
	
	/**
	 * ����������Ϣ
	 * @param aDis
	 * @throws Exception 
	 */
	private void parseCity(DataInputStream aDis) throws Exception
	{
//		LOG.print("parseCity");
//		int len = aDis.readUnsignedShort();
//		LOG.print("len=" + len);
//		for(int i = 0; i < len; i++)
//		{
//			City_Info city_info = new City_Info(aDis);
//			LOG.print(city_info);
//			iCity_Info.add(city_info);
//		}
//		LOG.print("parseCity");
		int len = aDis.readUnsignedShort();
//		LOG.print("len=" + len);
		iCity_Info = new City_Info[len];
		for(int i = 0; i < len; i++)
		{
			City_Info city_info = new City_Info(aDis);
//			LOG.print(city_info);
			iCity_Info[i] = city_info;
			//LOG.print(city_info+"����+349");
		}
	}
	
	/**
	 * ������������Ϣ
	 * @param aDis
	 * @throws Exception 
	 */
	private void parseInternational(DataInputStream aDis) throws Exception
	{
//		LOG.print("parseInternational");
//		int len = aDis.readUnsignedByte();
//		LOG.print("len=" + len);
//		for(int i = 0; i < len; i++)
//		{
//			International_Info info = new International_Info(aDis);
//			LOG.print(info);
//			iInternational_Info.add(info);
//		}
//		LOG.print("parseInternational");
		int len = aDis.readUnsignedByte();
//		LOG.print("len=" + len);
		iInternational_Info = new International_Info[len];
		List<Country_Info>country_List=new ArrayList<Country_Info>();
		for(int i = 0; i < len; i++)
		{
			International_Info info = new International_Info(aDis);
//			LOG.print(info);
			iInternational_Info[i] = info;
			for(int j=0;j<iInternational_Info[i].getiCountryInfo().length;j++){
				country_List.add(iInternational_Info[i].getiCountryInfo()[j]);
			}
		}
		country_Infos=new Country_Info[country_List.size()];
		for(int i=0;i<country_List.size();i++){
			country_Infos[i]=country_List.get(i);
		}
	}
	
	/**
	 * ����������Ϣ
	 * @param aDis
	 * @throws Exception 
	 */
	private void parseCommon(DataInputStream aDis) throws Exception
	{
//		LOG.print("parseCommon");
		iCommon_Info_List = new Common_Info_List(aDis);
	}
	
	
	/**
	 * ȫ��������,�����Դ������ƥ��aKey��λ����������
	 * @param aSearch
	 * �����㷨
	 * @param aSource
	 * ���Դ
	 * @param aKey
	 * Ҫ���������
	 * @param aIndex
	 * �����￪ʼ����
	 * @return
	 * ����ƥ���indexλ��
	 */
	final static public List<Integer> searchInfo(ISearchable aSearch, Comparable[] aSource, Comparable aKey, int aIndex)
	{
		List<Integer> result = new ArrayList<Integer>();
		int index = aIndex - 1;
		do
		{
			index = aSearch.search(aSource, aKey, ++index);
			if(index != -1)
			{
				result.add(index);
			}
		}
		while(index != -1);
		if(result.size() > 0)
			return result;
		else
			return null;
	}
	
	/**
	 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
	 * All rights reserved.
	 * @file NumAreaManager.java
	 * ժҪ:���������
	 * @author yewei
	 * @data 2011-5-31
	 * @version 
	 *
	 */
	class NumAreaParser
	{
		/**
		 * �ֻ����������ʽ
		 * 11λ�ֻ����|�ֻ����ǰ7λ|ǰ׺+11λ�ֻ����|+86+11λ�ֻ����
		 */
		private Pattern iMobilePattern = 
			Pattern.compile("^(1\\d{6})\\d{4}$|^(1\\d{6})$|^1\\d{4}(1\\d{6})\\d{4}$|^\\+86(1\\d{6})\\d{4}$");
		/**
		 * 2-3λ�ı������������ʽ
		 * +86+0+2λ���+6-8λ����|0+2λ���+6-8λ����|ǰ׺+0+2λ���+6-8λ����
		 * +86+0+3λ���+6-8λ����|0+3λ���+6-8λ����|ǰ׺+0+3λ���+6-8λ����
		 * 0+2-3λ���
		 */
		//Gionee <xuhz> <2013-08-19> modify for CR00846782 begin
		private Pattern[] iLocalPhoneNumPattern = new Pattern[]{
				Pattern.compile("^\\+86(0\\d{2})\\d{1,8}$|^(0\\d{2})\\d{1,8}$|^1\\d{4}(0\\d{2})\\d{1,8}$"),
				Pattern.compile("^\\+86(0\\d{3})\\d{1,8}$|^(0\\d{3})\\d{1,8}$|^1\\d{4}(0\\d{3})\\d{1,8}$"),
				Pattern.compile("^(0\\d{2,3})$"),
		};
		//Gionee <xuhz> <2013-08-19> modify for CR00846782 end
		/**
		 * 1-4λ�Ĺ�����������ʽ
		 * 00+4λ���+6-16λ����|ǰ׺+00+4λ���+6-16λ����
		 * 00+3λ���+6-16λ����|ǰ׺+00+3λ���+6-16λ����
		 * 00+2λ���+6-16λ����|ǰ׺+00+2λ���+6-16λ����
		 * 00+1λ���+6-16λ����|ǰ׺+00+1λ���+6-16λ����
		 * 00+1-4λ���
		 */
		private Pattern[] iInternationalPattern = new Pattern[]{
				Pattern.compile("^(00\\d{4})\\d{6,16}$|^1\\d{4}(00\\d{4})\\d{6,16}$"),
				Pattern.compile("^(00\\d{3})\\d{6,16}$|^1\\d{4}(00\\d{3})\\d{6,16}$"),
				Pattern.compile("^(00\\d{2})\\d{6,16}$|^1\\d{4}(00\\d{2})\\d{6,16}$"),
				Pattern.compile("^(00\\d{1})\\d{6,16}$|^1\\d{4}(00\\d{1})\\d{6,16}$"),
				Pattern.compile("^(00\\d{1,4})$"),
		};
		/**
		 * ���ú����������ʽ
		 * 3-����λ�����
		 */
		private Pattern iCommonInfoPattern = Pattern.compile("^(\\d{3,})$");
		
		/**
		 * ���ֲ����㷨
		 */
		private ISearchable iSearch_Binary = new BinarySearch();
		/**
		 * ˳������㷨
		 */
		private ISearchable iSearch_Sequential = new SequentialSearch();
		AreaSearch areaSearch =new AreaSearch();
//		/**
//		 * ������Ϣ������ʽ
//		 */
//		private Pattern iCommonPattern = Pattern.compile("(^\\d{3,5})$");
	/*	NumAreaInfo parsePhoneNum(String aPhoneNum)
		{
			return parsePhoneNum(aPhoneNum,"2");
		}*/
		/**
		 * ��ݴ����������ݽ������
		 * @param aPhoneNum
		 * @return
		 */
		NumAreaInfo parsePhoneNum(String aPhoneNum)
		{
            NumAreaInfo result = new NumAreaInfo(iContext);
            Boolean flag0 = false;
            Boolean flag = false;
            aPhoneNum=Util.delete_String(aPhoneNum);
            if (aPhoneNum.length() > 4) {
//                LOG.print("length > 4" + aPhoneNum);
             flag = aPhoneNum.substring(0, 3).equals("+86");
             flag0 = aPhoneNum.substring(0, 4).equals("0086");
            }
//            LOG.print("flag = "+ flag + "    ^-------^ flag0 = " + flag0);
            if (flag0) {
                aPhoneNum = aPhoneNum.substring(4);
//                LOG.print("kill 0086 :" + aPhoneNum);
            } else  {
                aPhoneNum = Util.Cut_86_Num(aPhoneNum);
            }

            if (flag0 || flag) {
                if (!(aPhoneNum.startsWith("0") || aPhoneNum.startsWith("1"))) {
                    aPhoneNum = "0" + aPhoneNum;
//                    LOG.print("add 0 = " + aPhoneNum);
                } else if (aPhoneNum.substring(0, 2).equals("10")) {
                    aPhoneNum = "0" + aPhoneNum;
//                    LOG.print(" 010 add 0 = " + aPhoneNum);
                }
            }
//			LOG.print("parsePhoneNum=" + aPhoneNum);
				if(disposeCommonNum(aPhoneNum, result))
				{
					//�ж��Ƿ��ǳ�����Ϣ��������д���
				}
				else if(disposeMobileNum(aPhoneNum, result))
				{
				
					//�ж��Ƿ����ֻ�ţ�������д���
				}
				else if(disposeInternationalPhoneNum(aPhoneNum, result))
				{
					//�ж��Ƿ��ǹ�������Ϣ��������д���
				}
				else if(disposeLocalNum(aPhoneNum, result))
				{
					//�ж��Ƿ��Ǳ�����룬������д���	
				}
				
				else {
					result=null;
//					LOG.print("NumAreaManagerδ֪����584****"+result);
				}
				return result;
			
		}
		/**
		 * ��ݴ����������ݽ������
		 * @param aPhoneNum
		 * @return
		 */
		NumAreaInfo[] parseArea(String aAreaString,String aTag){
				if(aTag.equals("1")){//widget��ѯ
					NumAreaInfo common_Info[]=areaSearch.selectAreaNum(iCommon_Info_List.getiCommon_Info(), aAreaString,TAG_COM_INFO);
//					LOG.print("������Ϣ��ѯ588");//������Ϣ��ѯ
					
					if(null!=common_Info){
//						LOG.print("596----------------��");
						return common_Info;
					}
					else {
						NumAreaInfo null_Infos[]=null;
//						LOG.print("68211+δ֪����---------------------��");
						return null_Infos;
						//return null;
					}
					
				}else {
//					LOG.print("NumAreaManger613************************��");
					//��ѯ������Ϣ
					NumAreaInfo common_Info[]=areaSearch.selectAreaNum(iCommon_Info_List.getiCommon_Info(), aAreaString,TAG_COM_INFO);
					//��ѯ���ڳ�����Ϣ
					NumAreaInfo international_Info[]=areaSearch.selectAreaNum(country_Infos, aAreaString, TAG_INTERNATIONAL);
					//��ѯ��ʳ�����Ϣ
					NumAreaInfo city_Info[]=areaSearch.selectAreaNum(iCity_Info, aAreaString,TAG_CITY_INFO);
					
					//��������ַ� ��ĵ���Ϣ�� ��� ���� ����ʱ��������кϲ�
					if(null!=city_Info&&null!=common_Info&&null!=international_Info){
//						LOG.print("�ϲ�1----*************"+(common_Info.length+city_Info.length+international_Info.length));
						NumAreaInfo areaNum_Info[]=new NumAreaInfo[common_Info.length+city_Info.length+international_Info.length];
						System.arraycopy(common_Info,0,areaNum_Info,0,common_Info.length); 
						System.arraycopy(city_Info,0,areaNum_Info,common_Info.length,city_Info.length);
						System.arraycopy(international_Info, 0, areaNum_Info, city_Info.length+common_Info.length, international_Info.length);
						return areaNum_Info;
					}
					//��������ַ� ��ĵ���Ϣ��  ���� ����ʱ��������кϲ�
					if(null!=city_Info&&null!=common_Info&&null==international_Info){
//						LOG.print("�ϲ�2----*************"+(common_Info.length+city_Info.length));
						NumAreaInfo areaNum_Info[]=new NumAreaInfo[common_Info.length+city_Info.length];
						System.arraycopy(common_Info,0,areaNum_Info,0,common_Info.length); 
						System.arraycopy(city_Info,0,areaNum_Info,common_Info.length,city_Info.length);
						return areaNum_Info;
					}
					//��������ַ� ��ĵ���Ϣ�� ��� ���� ����ʱ��������кϲ�
					else if(null!=city_Info&&null==common_Info&&null!=international_Info){
//						LOG.print("�ϲ�3----*************"+(city_Info.length+international_Info.length));
						NumAreaInfo areaNum_Info[]=new NumAreaInfo[city_Info.length+international_Info.length];
						System.arraycopy(city_Info, 0, areaNum_Info, 0, city_Info.length);
						System.arraycopy(international_Info, 0, areaNum_Info, city_Info.length, international_Info.length);
						return areaNum_Info;
					}
					//��������ַ� ��ĵ���Ϣ�� ���  ����ʱ��������кϲ�
					else if(null==city_Info&&null!=common_Info&&null!=international_Info){
//						LOG.print("�ϲ�4----*************"+(common_Info.length+international_Info.length));
						NumAreaInfo areaNum_Info[]=new NumAreaInfo[common_Info.length+international_Info.length];
						System.arraycopy(common_Info, 0, areaNum_Info, 0, common_Info.length);
						System.arraycopy(international_Info, 0, areaNum_Info, common_Info.length, international_Info.length);
						return areaNum_Info;
					}
					//��������ַ� ��ĵ���Ϣ��  ����ʱ
					else if(null!=common_Info&&null==city_Info&&null==international_Info){
//						LOG.print("common_Info--length------->5555555555555555"+common_Info.length);
//						LOG.print("city-------592"+common_Info.length);
						return common_Info;
					}
					//��������ַ� ��ĵ���Ϣ�� ���� 
					else if(null!=city_Info&&null==common_Info&&null==international_Info){
//						LOG.print("city_Info--length------->6666666666666666"+city_Info.length);
//						LOG.print("city_Info--length------->2222597"+city_Info.length);
						return city_Info;
					}
					//��������ַ� ��ĵ���Ϣ�� ���
					else if(null==city_Info&&null==common_Info&&null!=international_Info){
						return international_Info;
					}
					//��������ַ� ��ĵ���ϢΪ��
					else {
//						LOG.print("NumAreaManager673+����Ϊ��");
						return null;
					}
				}
				//areaSearch.selectAreaNum(iInternational_Info, aPhoneNum,3);
				
		
		}
		/**
		 * �жϺ����Ƿ����ֻ����,���ֻ��������д���
		 * @param aPhoneNum
		 * @param aMatcher
		 * @return
		 */
		private boolean disposeMobileNum(String aPhoneNum,NumAreaInfo aNumAreaInfo)
		{
			String matcher_result = null;
			if(aPhoneNum.length()>6&&aPhoneNum.length()<12){
				String numberString=aPhoneNum.substring(0, 7);
				matcher_result = getMatcherValue(iMobilePattern,numberString);
			}
			else {
				matcher_result = getMatcherValue(iMobilePattern,aPhoneNum);
			}
//			LOG.print("�����ж��Ƿ����ֻ��");
			
			if(matcher_result != null)
			{
//				LOG.print("������********************");
				/*�����ֳ�ͷ3λ��β4λ*/
				int head = Integer.parseInt(matcher_result.substring(0,3));
				int info = Integer.parseInt(matcher_result.substring(3, matcher_result.length()));
				
//				LOG.print("*********************head=" + head + ",info=" + info);
				int index = iSearch_Binary.search(iMobile_MNO, head,0);//2�ֲ��ҷ�ȥ�Ҷ�Ӧ��MNO���
				if(index != -1)
				{
					aNumAreaInfo.setiExtraInfo(iMobile_MNO[index].getMNO_Name());//������Ӫ����Ϣ
					int city_id = iMobile_MNO[index].getAreaInfo(iSearch_Binary, info);//ͨ��β4λ�����ȥ���ҳ���ID
					if(city_id != -1)
					{
						int city_index = iSearch_Binary.search(iCity_Info, city_id,0);//ͨ�����ID����City_info
						if(city_index != -1)
						{
							aNumAreaInfo.setiBaseInfo(iCity_Info[city_index].getiCityName());//���ó��е�����
							int province_id = iCity_Info[city_index].getiProvinceId();
							int province_index = iSearch_Binary.search(iProvince_Info, province_id,0);//���ʡ��IDȥ����Province_info
							if(province_index != -1)
							{
								aNumAreaInfo.setiHeadInfo(iProvince_Info[province_index].getProvinceName());//����ʡ����Ϣ
							}
						}
					}
				}
//				LOG.print("disposeMobileNum=" + aNumAreaInfo);
				aNumAreaInfo.setiTag(ALLSTATE.NUM_AREA_TAG);
				return true;
			}
			return false;
		}
		
		/**
		 * �жϺ����Ƿ��Ǳ������.�ǵĻ�����д���
		 * @param aPhoneNum
		 * @return
		 */
		private boolean disposeLocalNum(String aPhoneNum,NumAreaInfo aNumAreaInfo)
		{
			String matcher_result = null;
			boolean result = false;
			for(int i = 0; i < iLocalPhoneNumPattern.length; i++)
			{
				matcher_result = getMatcherValue(iLocalPhoneNumPattern[i],aPhoneNum);
				if(matcher_result != null)
				{
					//���ҳ��б��е����
//					int areacode = Integer.parseInt(matcher_result);
					/*ͬһ��ſ��ܶ�Ӧ�������,������Ҫ�����н�����һ��*/
					for(City_Info city_info : iCity_Info)
					{
						if(city_info.getiAreaCode().equals(matcher_result))
						{
							if(aNumAreaInfo.getiBaseInfo() != null)
							{//�����ж�����.����,�ŷָ�
								aNumAreaInfo.setiBaseInfo(aNumAreaInfo.getiBaseInfo() + "," + city_info.getiCityName());
							}
							else
							{
								try
								{
									aNumAreaInfo.setiBaseInfo(city_info.getiCityName());
									int province_index = iSearch_Binary.search(iProvince_Info, city_info.getiProvinceId(), 0);//���ʡ��ID����ʡ����Ϣ
									aNumAreaInfo.setiHeadInfo(iProvince_Info[province_index].getProvinceName());
									if(city_info.getiCityName().trim().toString().equals(iProvince_Info[province_index].getProvinceName().trim().toString())){
										aNumAreaInfo.setiHeadInfo("");
									}
									
								}
								catch(Exception e)
								{
									// TODO: handle exception
//									LOG.print("NumAreaManger766��ѯ�����쳣");
									aNumAreaInfo.setiBaseInfo(city_info.getiCityName());
									int province_index = iSearch_Binary.search(iProvince_Info, city_info.getiProvinceId(), 0);//���ʡ��ID����ʡ����Ϣ
									aNumAreaInfo.setiHeadInfo(iProvince_Info[province_index].getProvinceName());
								}
							}
							
						}
					}
					if(aNumAreaInfo.getiBaseInfo() != null)
					{//�����ҵ����.���˳�����
						result = true;
						aNumAreaInfo.setiTag(ALLSTATE.CITY_TAG);
//						LOG.print("disposeLocalNum=" + aNumAreaInfo);
						break;
					}
				}
			}
			return result;
		}
		
		/**
		 * �ж��Ƿ��ǹ�������Ϣ.������д���
		 * @param aPhoneNum
		 * @param aNumAreaInfo
		 * @return
		 */
		private boolean disposeInternationalPhoneNum(String aPhoneNum,NumAreaInfo aNumAreaInfo)
		{
			String matcher_result = null;
			boolean result = false;
			for(int i = 0; i < iInternationalPattern.length; i++)
			{
				matcher_result = getMatcherValue(iInternationalPattern[i],aPhoneNum);
				if(matcher_result != null)
				{
					//���ҹ�ʱ��е����
//					int area_code = Integer.parseInt(matcher_result);//ȡ�ù�����
					for(int j = 0; j < iInternational_Info.length; j++)
					{//�����й����Ϣ�в��ҷ����ŵ����
						List<Country_Info> info = iInternational_Info[j].searchCountryInfo(iSearch_Sequential, matcher_result);//��ݹ�����ȥÿ������Ϣ���Ҷ�Ӧ�Ĺ����Ϣ
						if(info != null)
						{
							for(Country_Info country_info : info)
							{
								if(aNumAreaInfo.getiBaseInfo() != null)
								{//����ͬһ�����Ҳ���Ӧ���������,������Ҫ�������
									aNumAreaInfo.setiBaseInfo(aNumAreaInfo.getiBaseInfo() + "," + country_info.getiCountryName());
								}
								else
								{//��һ�β��ҵ����
									aNumAreaInfo.setiBaseInfo(country_info.getiCountryName());
									aNumAreaInfo.setiHeadInfo(iInternational_Info[j].getiName());
								}						
							}
//							LOG.print("disposeInternationalPhoneNum=" + aNumAreaInfo);
							aNumAreaInfo.setiTag(ALLSTATE.INTERNTION_TAG);
							return true;
						}
					}
				}
			}
			return result;			
		}
		
		/**
		 * �ж��Ƿ����Ϣ,������д���
		 * @param aPhoneNum
		 * @param aNumAreaInfo
		 * @return
		 */
		private boolean disposeCommonNum(String aPhoneNum,NumAreaInfo aNumAreaInfo)
		{
			String matcher_result = null;
			matcher_result = getMatcherValue(iCommonInfoPattern,aPhoneNum);
			if(matcher_result != null && matcher_result.length() < 19)
			{
//				long number = Long.parseLong(matcher_result);
				Common_Info info = iCommon_Info_List.searchInfo(iSearch_Sequential, matcher_result);
				if(info != null)
				{
					//aNumAreaInfo.setiHeadInfo(info.getiGroupName());
					aNumAreaInfo.setiHeadInfo("");
					aNumAreaInfo.setiBaseInfo(info.getiName());
					aNumAreaInfo.setiTag(ALLSTATE.COMMON_TAG);
//					LOG.print("disposeCommonNum=" + aNumAreaInfo);
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		
		/**
		 * ��ȡ������ʽ�еĹؼ�����
		 * @param aMatcher
		 * @return
		 */
		private String getMatcherValue(Pattern aPattern,String aPhoneNum)
		{
			String result = null;
			Matcher matcher = aPattern.matcher(aPhoneNum);
			if(matcher.find())
			{
				int len = matcher.groupCount();
				for(int i = 1; i <= len; i++)
				{
					result = matcher.group(i);
					if(result != null)
						break;
				}
			}
			return result;
		}
	}
	
	/**
	 * Copyright (c) 2001, �����а�������Ƽ���˾�з���
	 * All rights reserved.
	 *
	 * @file NumAreaManager.java
	 * ժҪ:��������ݿ��������
	 *
	 * @author Administrator
	 * @data 2011-6-2
	 * @version 
	 *
	 */
//	class NumAreaDBUpdataer implements INetListenser
//	{
//		private INet iNet;
//
//		/**
//		 * db����URL
//		 */
////		final private static String URL = "http://192.168.180.216:8080/aoraappservlet/gettelinfoJ?vs=";
//		// gionee xuhz 20130122 modify for CR00766787 start
//		//final private static String URL ="http://weather.gionee.com/numlocation/gettelinfoJ2?vs=";
//		final private static String URL ="http://numlocation.gionee.com/numlocation/gettelinfoJ2?vs=";
//		// gionee xuhz 20130122 modify for CR00766787 end
//		final private static String URLTEST = "http://test1.gionee.com/numlocation/gettelinfoJ2?vs=";
//		//final private static String URL ="http://hiapk.91rb.com/data/upload//2012/02_03/com.ijinshan.kbatterydoctor_192713.apk";
//	//	final private static String URL = "http://test.myaora.net:9999/aoraappservlet/gettelinfoJ2?vs=";
//		/**
//		 * �������DB����ʱ�ļ�
//		 */
//		final static private String NUM_AREA_DB_TEMP = "NumAreaDB_TEMP";
////		final private BaseDataCollect_Get iDataCollect = new BaseDataCollect_Get(iContext);
//		final private Bundle bundle=new Bundle();
//		int i=0;
//		int iTemp=0;
//		int jTemp=0;
//		boolean iflag=true,jflag=false;
//		public NumAreaDBUpdataer()
//		{
////			iDataCollect.setCollectData(new BaseDataCollectFormat(iContext, "14"));
//		}
//		/**
//		 * ���DB��ʱ�ļ�
//		 */
//		private void cleanTempDB()
//		{
//			/*�ж����޻���������ļ�.*/
//			if(iContext.getFileStreamPath(NUM_AREA_DB_TEMP).exists())
//			{
//				iContext.deleteFile(NUM_AREA_DB_TEMP);
//			}
//		}
//		
//		/**
//		 * ȡ����
//		 */
//		public void cancelUpdata()
//		{
//			if(iNet != null && iNet.getNetState() == INet.STATE_CONNECTING)
//			{
//				iNet.close();
//			}
//			notifyObserver(IUpdataResult.RESULT_USER_CANCEL_UPDATA);
//			updateTag=false;
//		}
//		
//		/**
//		 * ����������
//		 * @param aObserver
//		 * @throws Exception
//		 */
//		public void updataDB(INumAreaObserver aObserver) throws Exception
//		{
////			LOG.print("updataDB");
//				//LOG.print(iNet.getNetState()+"******************����״̬");
//			if(iNet != null)
//			{
//				if(iNet.getNetState() == INet.STATE_CONNECTING)
//				{//���統ǰ���ڸ�����,�򲻽��и��²���
////					LOG.print("���統ǰ���ڸ�����,�򲻽��и��²���");
//				//	aObserver.updata(IUpdataResult.RESULT_ERROR_UPDATA_PROCESSING);
//					return;
//				}
//			}
//	
////			LOG.print("�������NumAreaManger952");
//			
//			cleanTempDB();
//			iOs = iContext.openFileOutput(NUM_AREA_DB_TEMP, Context.MODE_PRIVATE);
//			iNet = new HttpNet(iContext);
//			iNet.setOutputBuffer(iOs);
//			iNet.setKeyWord(null);
//			iNet.setWriteBuffSize(25*1024);
//			iNet.setDataCollect(iDataCollect);
//            String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
//            File file = new File(SDCardRoot + "numbertest1234567890" + File.separator);
//            TelephonyManager tm = (TelephonyManager) iContext.getSystemService(iContext.TELEPHONY_SERVICE);
//            String uaString = null;
//            // Gionee <xuhz> <2013-07-26> delete for CR00843511 begin
//            /*if (tm != null) {
//                String imei = tm.getDeviceId();
//                uaString = ProductConfiguration.getUAString(imei);
//                LOG.print("-_*------>uaString"+uaString);
//                iNet.setUA(uaString);
//            }else*/
//            // Gionee <xuhz> <2013-07-26> delete for CR00843511 end
//            {
//                iNet.setUA("Nokia7610;openwave;opera mini;MAUI WAP Browser");
////                LOG.print("Nokia7610;openwave;opera mini;MAUI WAP Browser");
//            }
//            
////            LOG.print("path = "+file.getPath());
//            if (file.exists()) {
////                LOG.print("URLTEST ->" + URLTEST);
//                if (iContext.getFileStreamPath(NUM_AREA_DB).exists())// �ж�������ݿ��Ƿ�ɾ����
//                {
//                    iNet.setURL(URLTEST + iVersion);
////                    LOG.print("@_@------->URLTEST+ iVersion="+URLTEST+ iVersion);
//
//                }
//                else{
//                    iNet.setURL(URLTEST+ iVersion);
////                    LOG.print("@_@------->URLTEST+ version="+URLTEST+ iVersion);
//                }
//            } else {
////                LOG.print("URL----" + URL);
//                if (iContext.getFileStreamPath(NUM_AREA_DB).exists())// �ж�������ݿ��Ƿ�ɾ����
//                {
//                    iNet.setURL(URL + iVersion);
////                    LOG.print("-_*------->URL+ iVersion="+URL+ iVersion);
//                }
//                else{
//                    iNet.setURL(URL+ iVersion);
////                    LOG.print("-_*------->URL+ version="+URL+ iVersion);
//                }
//            }
//			iNet.addListenser(this);
//			iNet.start();
//		}
//		
//		@Override
//        public void ConnectFaild(long aRange, int aErrCode) {
//            cleanTempDB();
//            notifyObserver(aErrCode);
//            
//        }
//
//
//		@Override
//		public void ConnectFinish(INet arg0)
//		{
//			// TODO Auto-generated method stub
//			
//			/*����ʱ�ļ�*/
//			File file = iContext.getFileStreamPath(NUM_AREA_DB_TEMP);
//			if(file.length() > 10)
//			{//���ص��ֽ������10��������Ч���
//                if (file.length() > 100 * 1024) {
//                    if (file.renameTo(iContext.getFileStreamPath(NUM_AREA_DB))) {
////                        LOG.print("rename success");
//                    }
//                }
//				notifyObserver(IUpdataResult.RESULT_SUCCESS);
//				//notifyObserver(IUpdataResult.RESULT_ERROR_UPDATA_PROCESSING);
//				initData();
//			}
//			else
//			{
//				if(true){
//					notifyObserver(IUpdataResult.RESULT_DB_IS_LAST_VERSION);
//				}
//			}
////			LOG.print("ConnectFinish&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"); 
//		}
//		
//        @Override
//        public void ConnectStart(long arg0) {
//            // TODO Auto-generated method stub
////            LOG.print("ConnectStart����������������������������������������������������������������1"); 
//            iTemp=0;
//            jTemp=0;
//        }
//
//		@Override
//        public void downloading(long arg0, long arg1, byte[] arg2, int arg3) {
//		    if(iflag){
//                iTemp=(int)arg0;
//                iflag=false;
//            }
//		  //��2�μ���
//            jTemp=(int)arg0;
//            if (jTemp - iTemp > (5 * 1024) || arg0 == arg1) {
//                bundle.putInt(STATE.HAVEDOWN, (int) arg0);
//                bundle.putInt(STATE.NEEDDOWN, (int) arg1);
//                notifyObserver(IUpdataResult.RESULT_DOWNLOADING, bundle);
//                iflag = true;
//                System.out.println("Im  downloading:" + (i++));
//            }
//            
//        }
//
//		
//
//        @Override
//        public void uploading(long arg0, long arg1) {
//            // TODO Auto-generated method stub
//            
//        }
//
//
//		
//	}

}
