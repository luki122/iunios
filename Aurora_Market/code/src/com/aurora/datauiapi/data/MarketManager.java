package com.aurora.datauiapi.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.datauiapi.data.bean.CategoryListObject;
import com.aurora.datauiapi.data.bean.MainListObject;
import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SpecialAllObject;
import com.aurora.datauiapi.data.bean.SpecialListObject;
import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.datauiapi.data.bean.cacheitem;
import com.aurora.datauiapi.data.bean.detailsObject;
import com.aurora.datauiapi.data.bean.specials;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.IMarketManager;
import com.aurora.market.marketApp;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.db.CacheDataAdapter;
import com.aurora.market.http.data.HttpRequestGetMarketData;
import com.aurora.market.util.Globals;
import com.aurora.market.util.SystemUtils;
import com.aurora.market.util.TimeUtils;


public class MarketManager extends baseManager implements IMarketManager{

	/**
	 * @uml.property name="tAG"
	 */
	private final String TAG = "VideoManager";



	private void updatePreference(Context context,int type)
	{
		SharedPreferences sp1 = context.getSharedPreferences(
				Globals.SHARED_DATA_UPDATE, context.MODE_APPEND);
		Editor ed = sp1.edit();
		if(type == 0)
			ed.putString(Globals.SHARED_DATA_CACHE_KEY_UPDATETIME, TimeUtils.getStringDateShort());
		else
			ed.putString(Globals.SHARED_DATA_CACHE_KEY_UPDATETIME, "0");
		ed.commit();
	}

	/** (非 Javadoc)  
	* Title: getMarketItems
	* Description:
	* @param response
	* @param context
	* @see com.aurora.datauiapi.data.interf.IMarketManager#getMarketItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context)
	*/ 
	@Override
	public void getMarketItems(final DataResponse<MarketListObject> response,final Context context,final int type,final String app_type,final int catid,final int pageNum,final int rowCount,final boolean isCacheData) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<MarketListObject>(response, this,isCacheData) {
			@Override
			public void doRun() throws Exception {
				String result = new String();
				CacheDataAdapter ldb = new CacheDataAdapter(context);
				ldb.open();
				if(isCacheData)
				{
					result = ldb.queryCacheByType(type,app_type,catid,0);
					if(TextUtils.isEmpty(result))
						updatePreference(context,1);
				}
				else
				{
					result = HttpRequestGetMarketData
						.getMarketListObject(type,app_type,catid,pageNum,rowCount);
					
					if (pageNum == 1) {
						cacheitem item = new cacheitem();
						item.setContext(result);
						item.setApp_type(app_type);
						item.setCat_id(catid);
						item.setType(type);
						item.setUpdate_time(String.valueOf(System.currentTimeMillis()));
						ldb.insert(item);
						updatePreference(context,0);
					}
				}
				ldb.close();
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				setResponse(response, result, MarketListObject.class);
			}
		});
	}
	
	/** (非 Javadoc)  
	* Title: getMainListItems
	* Description:
	* @param response
	* @param context
	* @see com.aurora.datauiapi.data.interf.IMarketManager#getMainListItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context)
	*/ 
	@Override
	public void getMainListItems(final DataResponse<MainListObject> response,final Context context,final int type,final String app_type,final int catid,final int pageNum,final int rowCount,final boolean isCacheData) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<MainListObject>(response, this,isCacheData) {
			@Override
			public void doRun() throws Exception {
				String result = new String();
				CacheDataAdapter ldb = new CacheDataAdapter(context);
				ldb.open();
				if(isCacheData)
				{
					result = ldb.queryCacheByType(type,app_type,catid,0);
					if(TextUtils.isEmpty(result))
						updatePreference(context,1);
				}
				else
				{
					result = HttpRequestGetMarketData
						.getMainListObject(type,app_type,catid,pageNum,rowCount);
					
					if (pageNum == 1) {
						cacheitem item = new cacheitem();
						item.setContext(result);
						item.setApp_type(app_type);
						item.setCat_id(catid);
						item.setType(type);
						item.setUpdate_time(String.valueOf(System.currentTimeMillis()));
						ldb.insert(item);
						updatePreference(context,0);
					}
				}
				ldb.close();
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				setResponse(response, result, MainListObject.class);
			}
		});
	}
	
	/** (非 Javadoc) 
	* Title: getDetailsItems
	* Description:
	* @param response
	* @param context
	* @param id
	* @see com.aurora.datauiapi.data.interf.IMarketManager#getDetailsItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context, int)
	*/ 
	@Override
	public void getDetailsItems(final DataResponse<detailsObject> response,
			Context context, final String packagename) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<detailsObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetMarketData
						.getDetailsObject(packagename);
				//result = result.replaceAll("<br />", "\n");
				String[][] object = { new String[] { "\\<br />", "\n" } };
				result = SystemUtils.replace(result, object);
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				setResponse(response, result, detailsObject.class);
			}
		});
		
	}

	
	/** (非 Javadoc) 
	* Title: getCategoryListItems
	* Description:
	* @param response
	* @param context
	* @param type
	* @see com.aurora.datauiapi.data.interf.IMarketManager#getCategoryListItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context, java.lang.String)
	*/ 
	@Override
	public void getCategoryListItems(final DataResponse<CategoryListObject> response,
			final Context context, final String type,final String style,final boolean isCacheData) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<CategoryListObject>(response, this,isCacheData) {
			@Override
			public void doRun() throws Exception {
				String result = new String();
				CacheDataAdapter ldb = new CacheDataAdapter(context);
				ldb.open();
				if(isCacheData)
				{
					result = ldb.queryCacheByType(AppListActivity.TYPE_CATEGORY_MAIN,type,0,0);
				}
				else
				{
					result = HttpRequestGetMarketData
							.getCategoryListObject(type,style);
					
					cacheitem item = new cacheitem();
					item.setContext(result);
					item.setApp_type(type);
					item.setType(AppListActivity.TYPE_CATEGORY_MAIN);
					item.setUpdate_time(String.valueOf(System.currentTimeMillis()));
					ldb.insert(item);
					updatePreference(context,0);
				}
				ldb.close();
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				setResponse(response, result, CategoryListObject.class);
			}
		});
		
	}
	
	@Override
	public void getSpecialListItems(final DataResponse<SpecialListObject> response,
			final Context context, final String type,final int pageNum,final int rowCount,final boolean isCacheData) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<SpecialListObject>(response, this,isCacheData) {
			@Override
			public void doRun() throws Exception {
				String result = new String();
				CacheDataAdapter ldb = new CacheDataAdapter(context);
				ldb.open();
				if(isCacheData)
				{
					result = ldb.queryCacheByType(AppListActivity.TYPE_SPECIAL_MAIN,Globals.TYPE_APP,0,0);
				}
				else
				{
					result = HttpRequestGetMarketData
							.getSpeciaListObject(type,pageNum,rowCount);
					
					if (pageNum == 1) {
						cacheitem item = new cacheitem();
						item.setContext(result);
						item.setType(AppListActivity.TYPE_SPECIAL_MAIN);
						item.setUpdate_time(String.valueOf(System.currentTimeMillis()));
						ldb.insert(item);
						updatePreference(context,0);
					}
				}
				ldb.close();
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				//String result = SystemUtils.getFromAssets(context, "special.json");
				setResponse(response, result, SpecialListObject.class);
				
			}
		});
	}

	@Override
	public void getSpecialAllItems(final DataResponse<SpecialAllObject> response,
			final Context context, final int specialId,final int pageNum,final int rowCount,final boolean isCacheData) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<SpecialAllObject>(response, this,isCacheData) {
			@Override
			public void doRun() throws Exception {
				String result = new String();
				CacheDataAdapter ldb = new CacheDataAdapter(context);
				ldb.open();
				if(isCacheData)
				{
					result = ldb.queryCacheByType(AppListActivity.TYPE_SPECIAL,Globals.TYPE_APP,0,specialId);
				}
				else
				{
					result = HttpRequestGetMarketData
							.getSpeciaAllObject(specialId,pageNum,rowCount);
					
					cacheitem item = new cacheitem();
					item.setContext(result);
					item.setApp_type(Globals.TYPE_APP);
					item.setSpe_id(specialId);
					item.setType(AppListActivity.TYPE_SPECIAL);
					item.setUpdate_time(String.valueOf(System.currentTimeMillis()));
					ldb.insert(item);
					updatePreference(context,0);
				}
				ldb.close();
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				//String result = SystemUtils.getFromAssets(context, "special.json");
				setResponse(response, result, SpecialAllObject.class);
				
			}
		});
	}

	/** (非 Javadoc) 
	* Title: postActivity
	* Description:
	* @see com.aurora.datauiapi.data.interf.IMarketManager#postActivity()
	*/ 
	@Override
	public void postActivity() {
		// TODO Auto-generated method stub
		/*
		 * if(failedRequests!=null){ failedRequests.clear(); }
		 */
		if (failedIORequests != null) {
			failedIORequests.clear();
		}
	}

}
