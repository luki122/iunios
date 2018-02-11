package com.privacymanage.model;

import java.io.File;

import com.privacymanage.utils.FileUtils;
import com.privacymanage.utils.StringUtils;
import com.privacymanage.utils.Utils;
import com.privacymanage.utils.mConfig;
import android.content.Context;
/**
 * @author Administrator
 */
public class FileSyncModel {
	private static Object sGlobalLock = new Object();
	private String sdFilePath = "";
	private String dataFilePath = "";
	private static FileSyncModel instance;

	public static FileSyncModel getInstance(Context context) {
		synchronized (sGlobalLock) {
			if (instance == null) {
				instance = new FileSyncModel(context);
			}
			return instance;
		}
	}
	
	private FileSyncModel(Context context) {		
		sdFilePath = Utils.getSDCardPath()+File.separator+mConfig.PATH_SOFT+File.separator;
		dataFilePath = Utils.getApplicationFilesPath(context)+File.separator;
		initSoftDir(); 		
	}
	
	public static void resetInstance(){
		instance = null;
	}

	private void initSoftDir() {
		FileUtils.makeDir(sdFilePath);		
	}
	
	/**
	 * 获取该项目在sd卡中的主文件目录
	 * @return
	 */
	public String getSoftPath(){
		return sdFilePath;
	}
	
	/**
	 * 读取并且同步文件
	 * @param fileName
	 * @return
	 */
	public String readAndSynFile(String fileName){
		String contentStr = null;
		if (StringUtils.isEmpty(fileName)) {
			return contentStr;
		}
		
		initSoftDir(); 
		
		String pathOfSd = sdFilePath+fileName;
		String patnOfSys = dataFilePath+fileName;
        
		if(isExistsInSys(fileName)){
			contentStr = FileUtils.readFile(patnOfSys);			
			if(!isExistsInSD(fileName)){
				FileUtils.CopyFile(patnOfSys,pathOfSd);
			}			
		}else if(isExistsInSD(fileName)){
			contentStr = FileUtils.readFile(pathOfSd);			
			FileUtils.CopyFile(pathOfSd,patnOfSys);
		}		
		return contentStr;
	} 
	
	/**
	 * 写入并同步文件
	 * @param fileName 
	 * @param contentStr 写入的内容
	 */
	public void writeAndSynFile(String fileName ,String contentStr){
		if (StringUtils.isEmpty(fileName)) {
			return ;
		}
		
		initSoftDir(); 
		
		String pathOfSd = sdFilePath+fileName;
		String patnOfSys = dataFilePath+fileName;
		
		FileUtils.writeFile(pathOfSd,contentStr);
		FileUtils.writeFile(patnOfSys,contentStr);
	}
	
	/**
	 * ͬ同步指定文件
	 * @param fileName
	 */
	public void synFile(String fileName){
		if (StringUtils.isEmpty(fileName)) {
			return ;
		}
		
		initSoftDir(); 
		
		String pathOfSd = sdFilePath+fileName;
		String patnOfSys = dataFilePath+fileName;
        
		if(isExistsInSys(fileName)){		
			if(!isExistsInSD(fileName)){
				FileUtils.CopyFile(patnOfSys,pathOfSd);
			}			
		}else if(isExistsInSD(fileName)){		
			FileUtils.CopyFile(pathOfSd,patnOfSys);
		}
	}
	
	//判断sd卡里面有没有指定的文件
	private boolean isExistsInSD(String fileName){
		String filePath = sdFilePath+fileName;
		if (FileUtils.fileIsExists(filePath)) {
			return true;
	    }else{
	    	return false;
	    }
	}
	
	//判断应用的data目录下有没有指定的文件
	private boolean isExistsInSys(String fileName){
		String filePath = dataFilePath+fileName;
		if (FileUtils.fileIsExists(filePath)) {
			return true;
	    }else{
	    	return false;
	    }
	}	
}
