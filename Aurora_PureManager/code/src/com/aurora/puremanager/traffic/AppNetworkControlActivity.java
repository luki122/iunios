/***
 * 联网权限控制
 */
package com.aurora.puremanager.traffic;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.ActivityUtils;
import com.aurora.puremanager.utils.ActivityUtils.LoadCallback;
import com.aurora.puremanager.utils.mConfig;

public class AppNetworkControlActivity extends AuroraActivity{

	private final int ID_LOADER_DEFAULT = 10;
	private final int REQUEST_CODE_OF_ADD_APP = 1;
	private NetworkControlAdapter adapter;
	private AuroraListView mListView;
	private LinearLayout mHeaderView;
	private AppListLoader mAppListLoader = null;
	
	private ArrayList<NetworkAppInfo>appListInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mConfig.isNative) {
			setContentView(R.layout.app_network_control_activity);
		} else {
			setAuroraContentView(R.layout.app_network_control_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.traffic_manage);
			getAuroraActionBar().setBackgroundResource(
					R.color.main_title_color);
		}
		initView();
		ActivityUtils.sleepForloadScreen(100, new LoadCallback() {
			@Override
			public void loaded() {
//				AutoStartModel.getInstance(AppNetworkControlActivity.this).attach(
//						updateViewHandler);
				updateViewHandler.sendEmptyMessage(0);
			}
		});
		
		try {
			getLoaderManager().restartLoader(ID_LOADER_DEFAULT, null,new AppInitCallbacks());
		} catch (Exception e) {
		}
	}

	private void initView() {
		mListView = (AuroraListView) findViewById(R.id.ListView);
		mHeaderView = (LinearLayout) getLayoutInflater().inflate(
				R.layout.list_header_ly, null);
		mListView.addHeaderView(mHeaderView);
	}

	private final Handler updateViewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			initOrUpdatetListData();
		}
	};

	 @Override
	    protected void onResume() {
	        // TODO Auto-generated method stub
	        super.onResume();
//	        try {
//	            getLoaderManager().restartLoader(ID_LOADER_DEFAULT, null, new AppInitCallbacks());
//	        } catch (Exception e) {
//	        }
	    }
	 
	public void initOrUpdatetListData2() {
		if (mListView == null) {
			return;
		}
		
		  appListInfo = getAppList();
		 
		if (appListInfo.size() == 0) {
			findViewById(R.id.NoAppLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.HaveAppLayout).setVisibility(View.GONE);
		} else {
			if (adapter == null) {
				adapter = new NetworkControlAdapter(AppNetworkControlActivity.this, mListView);
				mListView.setAdapter(adapter);
			} else {
				adapter.notifyDataSetChanged();
			}
			findViewById(R.id.NoAppLayout).setVisibility(View.GONE);
			findViewById(R.id.HaveAppLayout).setVisibility(View.VISIBLE);
			((TextView) mHeaderView.findViewById(R.id.list_head_text)).setText(getResources().getString(R.string.traffic_tips));
			adapter.notifyDataSetChanged(appListInfo, 0);
		}
	}

	private class AppInitCallbacks implements LoaderCallbacks<Object> {

        @Override
        public Loader<Object> onCreateLoader(int id, Bundle args) {
            // TODO Auto-generated method stub
            return mAppListLoader = AppListLoader.getInstance(AppNetworkControlActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<Object> arg0, Object arg1) {
            // TODO Auto-generated method stub
            initOrUpdatetListData2();
        }

        @Override
        public void onLoaderReset(Loader<Object> arg0) {
            // TODO Auto-generated method stub

        }
    }
	
	private ArrayList<NetworkAppInfo> getAppList() {
    	ArrayList<NetworkAppInfo> appListInfo = new ArrayList<NetworkAppInfo>();
    	appListInfo.addAll(mAppListLoader.getAppList());
    	
    	Collections.sort(appListInfo, new Comparator<NetworkAppInfo>() {
			@Override
			public int compare(NetworkAppInfo arg0, NetworkAppInfo arg1) {
				// TODO Auto-generated method stub
				 return chineseCompare(arg0.getAppName(), arg1.getAppName());
			}			
		});
    	
    	return appListInfo;
    }
	
	private int chineseCompare(Object _oChinese1, Object _oChinese2) {
        return Collator.getInstance(Locale.CHINESE).compare(_oChinese1,
                _oChinese2);
    }

	/**
	 * 更新数据
	 */
	public void initOrUpdatetListData() {
		initOrUpdatetListData2();
	}

	@Override
	protected void onDestroy() {
//		AutoStartModel.getInstance(this).detach(updateViewHandler);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CODE_OF_ADD_APP:
			initOrUpdatetListData();
			break;
		}
	}
}
