package com.aurora.iunivoice.utils;

import android.annotation.SuppressLint;
import java.util.HashMap;

/**    
 * 保存缩略图绝对路径
 * @author GuiLin
 */
public class ThumbnailsUtil {
	
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer,String> hash = new HashMap<Integer, String>();
	
	/**
	 * 返回value
	 * @param key
	 * @return
	 */
	public static String MapgetHashValue(int key,String defalt){
		if(hash==null||!hash.containsKey(key))return defalt;
		return hash.get(key);
	}
	
	/**
	 */
	public static void put(Integer key,String value){
		hash.put(key, value);
	}
	
	public static void clear(){
		hash.clear();
	}
}
