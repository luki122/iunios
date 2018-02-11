package com.aurora.datauiapi.data;


import java.util.List;

import android.app.ISearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SearchRecListObject;
import com.aurora.datauiapi.data.bean.SearchTimelyObject;
import com.aurora.datauiapi.data.bean.UpgradeListObject;
import com.aurora.datauiapi.data.bean.upcountinfo;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.ISearchAppManager;
import com.aurora.datauiapi.data.interf.IUpMarketManager;
import com.aurora.market.http.data.HttpRequestGetMarketData;
import com.aurora.market.http.data.HttpRequestSearchData;


public class SearchManager extends baseManager implements ISearchAppManager{

	/**
	 * @uml.property name="tAG"
	 */
	private final String TAG = "SearchManager";





	/** (非 Javadoc) 
	* Title: getSearchRecItems
	* Description:
	* @param response
	* @param context
	* @see com.aurora.datauiapi.data.interf.ISearchAppManager#getSearchRecItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context)
	*/ 
	@Override
	public void getSearchRecItems(final DataResponse<SearchRecListObject> response,
			Context context) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<SearchRecListObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestSearchData
						.getSearcjRecObject();
		
				setResponse(response, result, SearchRecListObject.class);
			}
		});
	}




	/** (非 Javadoc) 
	* Title: getSearchListItems
	* Description:
	* @param response
	* @param context
	* @param query
	* @param pageNum
	* @param rowCount
	* @see com.aurora.datauiapi.data.interf.ISearchAppManager#getSearchListItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context, java.lang.String, int, int)
	*/ 
	@Override
	public void getSearchListItems(final DataResponse<MarketListObject> response,
			Context context, final String query, final int pageNum, final int rowCount) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<MarketListObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestSearchData
						.getSearchAppListObject(query, pageNum, rowCount);
		
				setResponse(response, result, MarketListObject.class);
			}
		});
	}

	/** (非 Javadoc) 
	* Title: getSearchTimelyItems
	* Description:
	* @param response
	* @param context
	* @param query
	* @see com.aurora.datauiapi.data.interf.ISearchAppManager#getSearchTimelyItems(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context, java.lang.String)
	*/ 
	@Override
	public void getSearchTimelyItems(final DataResponse<SearchTimelyObject> response,
			Context context, final String query) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<SearchTimelyObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestSearchData
						.getSearchTimeLyObject(query);
		
				setResponse(response, result, SearchTimelyObject.class);
			}
		});
	}


	/** (非 Javadoc) 
	* Title: postActivity
	* Description:
	* @see com.aurora.datauiapi.data.interf.ISearchAppManager#postActivity()
	*/ 
	@Override
	public void postActivity() {
		// TODO Auto-generated method stub
		
	}





	
}
