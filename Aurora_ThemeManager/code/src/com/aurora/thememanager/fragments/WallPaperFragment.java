package com.aurora.thememanager.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import aurora.widget.AuroraListView;

import com.aurora.internet.HttpUtils;
import com.aurora.internet.InternetError;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.activity.MainActivity;
import com.aurora.thememanager.activity.MainActivity.OnNetworkChangeListener;
import com.aurora.thememanager.adapter.TimeWallpaperAdapter;
import com.aurora.thememanager.adapter.WallpaperAdapter;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.parser.JsonParser;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.ThemeInternetHelper;
import com.aurora.thememanager.view.ListViewDelegate;

public class WallPaperFragment extends SuperAwesomeCardFragment implements OnNetworkChangeListener,
JsonParser.CallBack,OnClickListener,ListViewDelegate.OnListScrollChange{
	
	private static final String TAG = "WallPaperFragment";
	private ThemeInternetHelper mThemeLoadHelper;

	private ListView mWallPaperList;
	
	private View mContent;
	private View mProgress;
	private View mNoNetWorkView;
	private ListViewDelegate mListDelegate;

	private List<Object> mThemes = new ArrayList<Object>();
	
	private WallpaperAdapter mThemeAdapter;
	
	private int mCurrentPage = 1;
	private int mTotalPage;
	
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
		// TODO Auto-generated method stub
		mContent = inflater.inflate(R.layout.wallpaper_page_theme, null);
		mWallPaperList = (ListView)mContent.findViewById(android.R.id.list);
		mWallPaperList.setVerticalScrollBarEnabled(false);
		mNoNetWorkView = mContent.findViewById(R.id.no_network_error);
		mProgress =  mContent.findViewById(R.id.progress);
		mThemeAdapter = new WallpaperAdapter(getActivity());
		mWallPaperList.setAdapter(mThemeAdapter);
		
		mListDelegate = new ListViewDelegate(mWallPaperList, true,false);
		mListDelegate.setCallBack(this);
		mListDelegate.setAdapter(mThemeAdapter);
		
		mThemeLoadHelper = new ThemeInternetHelper(getActivity());
		requestTheme(mCurrentPage);
		
//		mWallPaperList.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				// TODO Auto-generated method stub
//				Theme theme = mThemeAdapter.getTheme(position);
//				Intent intent = new Intent(Action.ACTION_PREVIEW_WALLPAPER);
//				intent.putExtra(Action.KEY_SHOW_WALL_PAPER_PREVIEW, theme);
//				getActivity().startActivity(intent);
//			}
//		});
		
		((MainActivity)getActivity()).addNetworkListener(this);

		return mContent;
	}
	
	/**
	 * 请求网络数据
	 * @param page
	 */
	private void requestTheme(int page){
		mThemeLoadHelper.clearRequest();
		mThemeLoadHelper.request(ThemeConfig.HttpConfig.THEME_WALLPAPER_REQUEST_URL,getHttpListener(),HttpUtils.createPostParams(getActivity(), page));
		mThemeLoadHelper.startRequest();
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
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
		}
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
			Parser parser = getThemeParser(Parser.TYPE_WALLPAPER);
			parser.setCallBack(this);
			mThemes =parser.startParser(response.toString());
			if (mThemes != null) {
				mHandler.sendEmptyMessage(0);
			}
		}
	}

	@Override
	public void onError(InternetError error) {
		// TODO Auto-generated method stub
		Log.e("101010", "------WallPaperFragment error = ---------" + error);
		mProgress.setVisibility(View.GONE);
		Context context = getActivity();
		if(context != null){
			showNoNetworkView(!SystemUtils.isNetworkConnected(getActivity()));
		}
	}
	
	private void showNoNetworkView(boolean show){
		mNoNetWorkView.setVisibility(show?View.VISIBLE:View.GONE);
		mWallPaperList.setVisibility(show?View.GONE:View.VISIBLE);
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
		showNoNetworkView(false);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onParserSuccess(boolean success, int statusCode, String desc,
			int totalPage) {
		// TODO Auto-generated method stub
		if(success && statusCode == 0){
			mWallPaperList.setVisibility(View.VISIBLE);
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
}
