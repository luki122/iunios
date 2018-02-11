package com.aurora.thememanager.view;

import com.aurora.thememanager.R;
import com.aurora.utils.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 该类用于处理listview的滑动事件，例如滑动状态的监听，
 * 分页加载的处理。
 * @author alexluo
 *
 */
public class ListViewDelegate implements OnScrollListener{
	
	private static final String TAG = "ListViewDelegate";
	
	private static final int TEXT_LOAD_FINISH = R.string.load_finish;
	
	private static final int TEXT_LOADING = R.string.loading;
	
	private static final int LOAD_MORE_VIEW_HEIGHT = R.dimen.load_more_view_height;
	
	private int mLoadMoreViewHeight;
	
	/**
	 * 需要处理的ListView
	 */
	private ListView mList;
	
	/**
	 * 分页加载时显示在ListView底部的footerView
	 */
	private View mLoadMoreView;
	
	/**
	 * 加载进度条
	 */
	private View mProgress;
	
	/**
	 * 加载提示语
	 */
	private TextView mLoadText;
	
	/**
	 * 用于表示该ListView是否需要分页加载功能
	 */
	private boolean mLoadMore;
	
	private boolean mLoadFinished = false;
	
	private boolean mLoadMoreViewClickable;
	
	private OnListScrollChange mCallback;
	
	private BaseAdapter mAdapter;
	/**
	 * ListViewDelegate滑动时处理各种事件的回调
	 * 
	 * @author alexluo
	 *
	 */
	public interface OnListScrollChange{
		/**
		 * 显示加载更多View时需要处理的逻辑在这里处理
		 */
		public void onShowLoadMoreView();
		/**
		 * 隐藏加载更多View时需要处理的逻辑在这里处理
		 */
		public void onHideLoadMoreView();
		
		public void loadMore();
	}
	
	public ListViewDelegate(ListView listview, boolean loadMore,boolean clickable ){
		mList = listview;
		mList.setOnScrollListener(this);
		mLoadMore = loadMore;
		mLoadMoreViewClickable = clickable;
		mLoadMoreViewHeight = listview.getResources().getDimensionPixelSize(LOAD_MORE_VIEW_HEIGHT);
		if(loadMore){
			mLoadMoreView = LayoutInflater.from(listview.getContext()).inflate(R.layout.list_load_more_view, null);
			mProgress = mLoadMoreView.findViewById(R.id.listview_foot_progress);
			mLoadText = (TextView)mLoadMoreView.findViewById(R.id.listview_foot_more);
			showLoadMoreView();
		}
	}
	
	public void setCallBack(OnListScrollChange callback){
		mCallback = callback;
	}
	
	/**
	 * 将ListView的adapter传进来做数据处理
	 * @param adapter
	 */
	public void setAdapter(BaseAdapter adapter){
		this.mAdapter = adapter;
	}
	
	/**
	 * 自定义添加加载更多的footerview
	 */
	public void addLoadMoreView(View footer,boolean clickable){
		   mLoadMore = true;
		   mLoadMoreView = footer;
		   mLoadMoreViewClickable = clickable;
	}
	

	
	/**
	 * 隐藏加载更多的footerview
	 */
	public void hideLoadMoreView(){
		if(mLoadMore){
			mLoadMoreView.setVisibility(View.GONE);
		}
	}
	
	public void removeLoadView(){
		if(mLoadMore){
			mList.removeFooterView(mLoadMoreView);
		}
	}
	
	/**
	 * 显示加载更多的footerview
	 */
	public void showLoadMoreView(){
		if(mLoadMore){
			ViewGroup.LayoutParams params = mLoadMoreView.getLayoutParams();
			if(params == null){
				params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLoadMoreViewHeight);
			}
			mLoadMoreView.setLayoutParams(params);
			mList.addFooterView(mLoadMoreView,null,mLoadMoreViewClickable);
		}
	}

	/**
	 * 设置是否加载完成的标志
	 * @param finish
	 */
	public void loadFinished(boolean finish){
		mLoadFinished = finish;
		if(mProgress != null){
			mProgress.setVisibility(finish?View.GONE:View.VISIBLE);
		}
		if(mLoadText != null){
			mLoadText.setText(finish?TEXT_LOAD_FINISH:TEXT_LOADING);
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onScrollStateChanged:"+scrollState);
		if(mLoadMoreView.getVisibility() == View.GONE){
			mLoadMoreView.setVisibility(View.VISIBLE);
		}
		switch (scrollState) {
		case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: {
			if(mAdapter != null){
				mAdapter.notifyDataSetChanged();
			}
			break;
		}
		case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL: {
			
			break;
		}
		case AbsListView.OnScrollListener.SCROLL_STATE_FLING: {

			break;
		}
		}
		/*
		 * 如果已经加载完成就退出
		 */
		if(mLoadFinished){
			return;
		}
		/*
		 * 如果滑动到加载更多的view时就加载剩余数据
		 */
		if(mLoadMoreView.getParent() == null){
			return;
		}
		if (view.getPositionForView(mLoadMoreView) == view.getLastVisiblePosition()) {
			if (mCallback != null) {
				mCallback.loadMore();
			}
		}
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
