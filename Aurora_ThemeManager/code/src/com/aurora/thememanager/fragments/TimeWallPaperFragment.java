package com.aurora.thememanager.fragments;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.R;
import com.aurora.thememanager.adapter.TimeWallpaperAdapter;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.parser.JsonParser;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadUpdateListener;
import com.aurora.thememanager.utils.download.TimeWallpaperDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;
import com.aurora.thememanager.view.ListViewDelegate;

public class TimeWallPaperFragment extends SuperAwesomeCardFragment implements ListViewDelegate.OnListScrollChange,JsonParser.CallBack{
	private static final String TAG = "TimeWallPaperFragment";
	
	
	private ThemeInternetHelper mThemeLoadHelper;
	private View mContentView;
	
	private ListViewDelegate mListDelegate;

	private ListView mWallPaperList;
	
	private int mTotalPage;

	private int mCurrentPage = 1;
	
	private View mProgress;

	private List<Object> mThemes;
	
	private TimeWallpaperAdapter mThemeAdapter;
	
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
	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (mThemeAdapter != null) {
				// adapter.notifyDataSetChanged();
				mThemeAdapter.updateData();
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContentView = inflater.inflate(R.layout.theme_page, null);
		mWallPaperList = (ListView)mContentView.findViewById(android.R.id.list);
		mWallPaperList.setVerticalScrollBarEnabled(false);
		mThemeLoadHelper = new ThemeInternetHelper(getActivity());
		mThemeAdapter = new TimeWallpaperAdapter(getActivity());
		mWallPaperList.setAdapter(mThemeAdapter);
		mListDelegate = new ListViewDelegate(mWallPaperList, true,false);
		mListDelegate.setCallBack(this);
		mListDelegate.setAdapter(mThemeAdapter);
		requestTheme(mCurrentPage);
		mProgress =  mContentView.findViewById(R.id.progress);
		return mContentView;
	}
	
	private void requestTheme(int page){
		mThemeLoadHelper.clearRequest();
		mThemeLoadHelper.request(ThemeConfig.HttpConfig.THEME_TIME_WALLPAPER_REQUEST_URL,getHttpListener(),HttpUtils.createPostParams(getActivity(),page));
		mThemeLoadHelper.startRequest();
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		TimeWallpaperDownloadService.unRegisterUpdateListener(updateListener);
		mThemeLoadHelper.stopRequest();
		if(mThemeAdapter != null){
			mThemeAdapter.stopQueue();
		}
	}
	


	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
		mProgress.setVisibility(View.VISIBLE);
	}



	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onSuccess(Object response) {
		// TODO Auto-generated method stub
		mProgress.setVisibility(View.GONE);
		if(response != null){
			Log.d(TAG, ""+response.toString());
			Parser parser = getThemeParser(Parser.TYPE_TIME_WALLPAPER);
			parser.setCallBack(this);
			mThemes = parser.startParser(response.toString());
			if (mThemes != null) {
				mHandler.sendEmptyMessage(0);
			}
		}
	}



	@Override
	public void onError(InternetError error) {
		// TODO Auto-generated method stub
		Log.d("tm", "onError:"+error);
	}



	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onNetworking() {
		// TODO Auto-generated method stub
	}



	@Override
	public void onUsedCache() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onRetry() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onProgressChange(long fileSize, long downloadedSize) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		TimeWallpaperDownloadService.registerUpdateListener(updateListener);
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
			mCurrentPage++;
			mListDelegate.loadFinished(false);
			requestTheme(mCurrentPage);
		}else{
			mListDelegate.loadFinished(true);
		}
	}

	@Override
	public void onParserSuccess(boolean success, int statusCode, String desc,
			int totalPage) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onParserSuccess:"+"  success:"+success+" totalPage:"+totalPage);
		if(success){
			mWallPaperList.setVisibility(View.VISIBLE);
			if(mTotalPage == 0){
				mTotalPage = totalPage;
			}
			
		}
	}
	
	
	
	
	
}
