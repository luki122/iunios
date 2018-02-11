package com.privacymanage.utils;

import java.io.File;

public class UrlUtils {
	private final static String DirName = "privacytest1234567890";	
	private final static String urlHeaderOfTest = "http://adstest.virtual.iunios.com/sendmail?";//测试服务器
	private final static String urlHeaderOfFormal = "http://i.iunios.com/sendmail?";//正式服务器
	
	public static String getSendEmailUrl() {
		return getUrlHeader();  
	}
	
	/**
	 * 返回请求的域名或ip地址
	 * @param context
	 * @return
	 */
	private static String getUrlHeader(){
		String sdPath = FileUtils.getSDCardPath()+"/";
		if(FileUtils.dirIsExists(sdPath+DirName)){
			String firstFileName = getFirstFileName(sdPath+DirName);
			if(firstFileName == null){
				return urlHeaderOfTest;
			}else{
				return firstFileName;
			}			
		}else{
			return urlHeaderOfFormal;
		}
	}
	
	private static String getFirstFileName(String dirName) {
		try {
			File f = new File(dirName);
			File[] fileList = f.listFiles();
			if(fileList != null && fileList.length > 0){
				return "http://"+fileList[0].getName()+"/";
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}		
}
