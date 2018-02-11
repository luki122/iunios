package com.aurora.datauiapi.data.interf;

import android.content.Context;

import com.aurora.datauiapi.data.bean.UpgradeListObject;
import com.aurora.datauiapi.data.bean.upcountinfo;
import com.aurora.datauiapi.data.implement.DataResponse;



public interface IUpMarketManager {
	/** 
	* @Title: getUpAppListItems
	* @Description: 更新应用市场列表数据
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getUpAppListItems(final DataResponse<UpgradeListObject> response,final Context context);
	
	/** 
	* @Title: getUpdateCount
	* @Description: 得到需要更新的应用数量
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getUpdateCount(final DataResponse<upcountinfo> response,final Context context);
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	

}
