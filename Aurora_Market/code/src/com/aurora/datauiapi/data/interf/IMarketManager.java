package com.aurora.datauiapi.data.interf;

import android.content.Context;

import com.aurora.datauiapi.data.bean.CategoryListObject;
import com.aurora.datauiapi.data.bean.MainListObject;
import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SpecialAllObject;
import com.aurora.datauiapi.data.bean.SpecialListObject;
import com.aurora.datauiapi.data.bean.detailsObject;
import com.aurora.datauiapi.data.implement.DataResponse;



public interface IMarketManager {

	
	/** 
	* @Title: getMarketItems
	* @Description: 得到应用市场列表数据(包括banner头)
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getMarketItems(final DataResponse<MarketListObject> response,final Context context,final int type,final String app_type,final int catid,final int pageNum,final int rowCount,final boolean isCacheData);
	
	/** 
	* @Title: getMainListItems
	* @Description: 得到应用市场列表数据(包括banner头)
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getMainListItems(final DataResponse<MainListObject> response,final Context context,final int type,final String app_type,final int catid,final int pageNum,final int rowCount,final boolean isCacheData);
	
	/** 
	* @Title: getDetailsItems
	* @Description: 得到应用详细数据
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getDetailsItems(final DataResponse<detailsObject> response,final Context context,final String packagename);
	
	/** 
	* @Title: getAppListItems
	* @Description: 应用市场列表数据
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getCategoryListItems(final DataResponse<CategoryListObject> response,final Context context,final String type,final String style,final boolean isCacheData);
	/** 
	* @Title: getSpecialListItems
	* @Description: 专题列表数据
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getSpecialListItems(final DataResponse<SpecialListObject> response,final Context context,final String type,final int pageNum,final int rowCount,final boolean isCacheData);
	/** 
	* @Title: getSpecialListItems
	* @Description: 单个专题全部数据
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getSpecialAllItems(final DataResponse<SpecialAllObject> response,final Context context,final int specialId,final int pageNum,final int rowCount,final boolean isCacheData);
	
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	

}
