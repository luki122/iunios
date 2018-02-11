package com.aurora.datauiapi.data;


import android.content.Context;
import android.util.Log;

import com.aurora.datauiapi.data.bean.UpgradeListObject;
import com.aurora.datauiapi.data.bean.upcountinfo;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.IUpMarketManager;
import com.aurora.market.http.data.HttpRequestGetMarketData;


public class UpMarketManager extends baseManager implements IUpMarketManager{

	/**
	 * @uml.property name="tAG"
	 */
	private final String TAG = "VideoManager";




	@Override
	public void getUpAppListItems(final DataResponse<UpgradeListObject> response,
			final Context context) {
		mHandler.post(new Command<UpgradeListObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetMarketData
						.getUpAppListObject(context);
				
				Log.v(TAG, "aurora.jiangmx return data:" + result);
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				setResponse(response, result, UpgradeListObject.class);
			}
		});
	}
	/** (非 Javadoc) 
	* Title: getUpdateCount
	* Description:
	* @param response
	* @param context
	* @see com.aurora.datauiapi.data.interf.IUpMarketManager#getUpdateCount(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context)
	*/ 
	@Override
	public void getUpdateCount(final DataResponse<upcountinfo> response,
			final Context context) {
		// TODO Auto-generated method stub
		mHandler.post(new Command<upcountinfo>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetMarketData
						.getUpdateCountObject(context);
				// String result =
				// HttpRequstData.doRequest("http://m.weather.com.cn/data/101110101.html");
				setResponse(response, result, upcountinfo.class);
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
