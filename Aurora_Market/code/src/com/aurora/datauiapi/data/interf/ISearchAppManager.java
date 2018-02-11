package com.aurora.datauiapi.data.interf;

import android.content.Context;

import com.aurora.datauiapi.data.bean.AppListObject;
import com.aurora.datauiapi.data.bean.BannerItem;
import com.aurora.datauiapi.data.bean.DownLoadObject;
import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SearchRecListObject;
import com.aurora.datauiapi.data.bean.SearchTimelyObject;
import com.aurora.datauiapi.data.bean.UpgradeListObject;
import com.aurora.datauiapi.data.bean.WeatherObject;
import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.datauiapi.data.bean.upcountinfo;
import com.aurora.datauiapi.data.implement.DataResponse;



public interface ISearchAppManager {
	/** 
	* @Title: getSearchRecItems
	* @Description: 搜索推荐列表数据
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getSearchRecItems(final DataResponse<SearchRecListObject> response,final Context context);
	
	/** 
	* @Title: getSearchListItems
	* @Description: 搜索应用接口
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getSearchListItems(final DataResponse<MarketListObject> response,final Context context,final String  query,final int pageNum,final int rowCount);
	
	
	/** 
	* @Title: getSearchTimelyItems
	* @Description: 及时搜索接口
	* @param @param response
	* @param @param context
	* @param @param query
	* @return void
	* @throws 
	*/ 
	public void getSearchTimelyItems(final DataResponse<SearchTimelyObject> response,final Context context,final String  query);
	
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	

}
