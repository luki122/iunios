package com.aurora.thememanager.utils.download;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
	
	/**
	* @Title: getTimeString
	* @Description: 获取时间戳，格式为20140702102040
	* @param @param time
	* @param @return
	* @return String
	* @throws
	 */
	public static String getTimeString(long time) {
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		return dateFormat.format(date);
	}

}
