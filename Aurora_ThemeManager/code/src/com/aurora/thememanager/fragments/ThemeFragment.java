package com.aurora.thememanager.fragments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import aurora.widget.AuroraListView;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.MainActivity;
import com.aurora.thememanager.activity.MainActivity.OnNetworkChangeListener;
import com.aurora.thememanager.adapter.ThemeListAdapter;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeBeanFromJsonList;
import com.aurora.thememanager.parser.JsonParser;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.utils.JsonMapUtils;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadUpdateListener;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;
import com.aurora.thememanager.view.ListViewDelegate;

public class ThemeFragment extends SuperAwesomeCardFragment implements OnNetworkChangeListener,
JsonParser.CallBack,OnClickListener,ListViewDelegate.OnListScrollChange{
	
	private static final String TAG = "ThemeFragment";
	
	private ThemeInternetHelper mThemeLoadHelper;
	
	private ListView mThemeList;
	private ThemeListAdapter mThemeAdapter;
	
	private View mContent;
	
	private View mProgress;
	
	private View mNoNetWorkView;
	
	private ListViewDelegate mListDelegate;
	
	private List<Object> mThemes = new ArrayList<Object>();
	
	private int mCurrentPage = 1;
	
	private int mTotalPage;
	
	private boolean mIsNetworking = false;
	
	private boolean mIsRequesting;
	
	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (mThemeAdapter != null) {
				// adapter.notifyDataSetChanged();
				mThemeAdapter.updateView(mThemeList);
			}
		}
	};
	
	
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
			if(mThemes != null && mThemes.size() > 0){
				
				int count = mThemes.size();
				for(Object theme:mThemes){
					
					Theme th  = (Theme)theme;
					mThemeAdapter.addData(th);
				}
			}
		};
		
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mThemeAdapter = new ThemeListAdapter(getActivity());
		mContent = inflater.inflate(R.layout.theme_page, null);
		mThemeList = (ListView)mContent.findViewById(android.R.id.list);
		mNoNetWorkView = mContent.findViewById(R.id.no_network_error);
		mProgress =  mContent.findViewById(R.id.progress);
		// TODO Auto-generated method stub
		mThemeLoadHelper = new ThemeInternetHelper(getActivity());
		mThemeList.setAdapter(mThemeAdapter);
		mListDelegate = new ListViewDelegate(mThemeList, true,false);
		mListDelegate.setCallBack(this);
		mListDelegate.setAdapter(mThemeAdapter);
		mProgress.setVisibility(View.VISIBLE);
		requestTheme(mCurrentPage);
		
		return mContent;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		((MainActivity)getActivity()).addNetworkListener(this);
	}
	
	
	
	
	/**
	 * 请求网络数据
	 * @param page
	 */
	private void requestTheme(int page){
		if(!mIsRequesting) {
			mThemeLoadHelper.clearRequest();
			mThemeLoadHelper.request(ThemeConfig.HttpConfig.THEME_PACKAGE_REQUEST_URL,getHttpListener(),HttpUtils.createPostParams(getActivity(), page));
			mThemeLoadHelper.startRequest();
			
			mIsRequesting = true;
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DownloadService.registerUpdateListener(updateListener);
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mThemeAdapter.updateData();
	}
	
	
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DownloadService.unRegisterUpdateListener(updateListener);
		mThemeLoadHelper.stopRequest();
		if(mThemeAdapter != null){
			mThemeAdapter.stopQueue();
		}
	}
	
	


	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
		if(mCurrentPage == 1){
			mListDelegate.hideLoadMoreView();
			mProgress.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		mIsRequesting = false;
	}

	/**
	 * 网络请求成功
	 * @param response
	 */
	@Override
	public void onSuccess(Object response) {
		// TODO Auto-generated method stub
		mProgress.setVisibility(View.GONE);
		
		if(response != null){
			Parser parser = getThemeParser();
			parser.setCallBack(this);
			
			mThemes =parser.startParser(response.toString());
			if (mThemes != null) {
				mHandler.sendEmptyMessage(0);
			}
		}
	}

	/**
	 * 网络请求错误
	 * @param error
	 */
	@Override
	public void onError(InternetError error) {
		// TODO Auto-generated method stub
		mProgress.setVisibility(View.GONE);
		Context context = getActivity();
		if(context != null){
			showNoNetworkView(!SystemUtils.isNetworkConnected(getActivity()));
		}
		
	}
	
	private void showNoNetworkView(boolean show){
		mNoNetWorkView.setVisibility(show?View.VISIBLE:View.GONE);
		mThemeList.setVisibility(show?View.GONE:View.VISIBLE);
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 正在执行网络请求
	 */
	@Override
	public void onNetworking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUsedCache() {
		// TODO Auto-generated method stub
		showNoNetworkView(false);
	}

	/**
	 * 重试，重新请求网络
	 */
	@Override
	public void onRetry() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgressChange(long fileSize, long downloadedSize) {
		// TODO Auto-generated method stub
		
	}





	/**
	 * 服务器返回的数据解析成功之后的数据状态
	 */
	@Override
	public void onParserSuccess(boolean success, int statusCode, String desc,
			int totalPage) {
		// TODO Auto-generated method stub
		if(success && statusCode == 0){
			mThemeList.setVisibility(View.VISIBLE);
			if(mTotalPage == 0){
				mTotalPage = totalPage;
			}
			
		}
	}


	
	@Override
	public void onNetConnnectedChange(boolean hasNetwork) {
		// TODO Auto-generated method stub
		    boolean showNoNetworkView = mThemes.size() < 1 && !hasNetwork;
			mNoNetWorkView.setVisibility(showNoNetworkView?View.VISIBLE:View.GONE);
			if(hasNetwork && mThemes.size() < 1 || mCurrentPage < mTotalPage){
				requestTheme(mCurrentPage);
			}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onShowLoadMoreView() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onHideLoadMoreView() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void loadMore() {
		// TODO Auto-generated method stub
		if(mCurrentPage < mTotalPage){
			if(!mIsRequesting) {
				mCurrentPage++;
				mListDelegate.loadFinished(false);
				Log.d(TAG, "currentPage:"+mCurrentPage);
				requestTheme(mCurrentPage);
			}
		}else{
			mListDelegate.loadFinished(true);
		}
	}
	
	
	
	
	
	
	
	

}