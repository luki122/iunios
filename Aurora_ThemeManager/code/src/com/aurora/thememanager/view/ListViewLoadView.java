package com.aurora.thememanager.view;

import com.aurora.thememanager.R;
import com.aurora.utils.Log;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
public class ListViewLoadView extends LinearLayout implements OnClickListener{

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
	 * 加载进度条
	 */
	private View mProgress;
	
	/**
	 * 加载提示语
	 */
	private TextView mLoadText;
	
	private Context mContext;
	
	/**
	 * 用于表示该ListView是否需要分页加载功能
	 */
	private boolean mLoadMore;
	
	private boolean mLoadFinished = false;
	
	private boolean mLoadMoreViewClickable;
	
	private OnLoadViewClick mCallback;
	
	private BaseAdapter mAdapter;
	/**
	 * ListViewDelegate滑动时处理各种事件的回调
	 * 
	 * @author alexluo
	 *
	 */
	public interface OnLoadViewClick{
		/**
		 * 显示加载更多View时需要处理的逻辑在这里处理
		 */
		public void onListViewLoadViewShowLoadMoreView();
		/**
		 * 隐藏加载更多View时需要处理的逻辑在这里处理
		 */
		public void onListViewLoadViewHideLoadMoreView();
		
		public void onListViewLoadVieWLoadMore();
	}
	
	public static final int STATUS_LOADMORE = 1;
	public static final int STATUS_LOADING = 2;
	public static final int STATUS_LOADFINISHED = 3;
	private int mRingtonePhoneStatus = 1;
	
	public ListViewLoadView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initViews(context);
	}
	
	public ListViewLoadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initViews(context);
	}
	
	public ListViewLoadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initViews(context);
	}
	
	private void initViews(Context context){
		
		mLoadMoreViewHeight = context.getResources().getDimensionPixelSize(LOAD_MORE_VIEW_HEIGHT);
		mContext = context;
		
		LayoutInflater.from(mContext).inflate(R.layout.list_load_more_view_in_1, this, true);
		
		mProgress = findViewById(R.id.listview_foot_progress);
		mLoadText = (TextView)findViewById(R.id.listview_foot_more);
		setStatus(STATUS_LOADMORE);
		setOnClickListener(this);
		showLoadMoreView();
	}
	
	public void setCallBack(OnLoadViewClick callback){
		mCallback = callback;
	}
	
    public void setStatus(int status) {
    	mRingtonePhoneStatus = status;
    	switch (status) {
		case STATUS_LOADMORE:
			mProgress.setVisibility(View.GONE);
			mLoadText.setText(R.string.load_more);
			mLoadText.setTextColor(mContext.getResources().getColor(R.color.local_theme_item_name_title_color));
			break;
		case STATUS_LOADING:
			mProgress.setVisibility(View.VISIBLE);
			mLoadText.setText(R.string.loading);
			mLoadText.setTextColor(mContext.getResources().getColor(R.color.local_theme_item_name_title_color));
			break;
		case STATUS_LOADFINISHED:
			mProgress.setVisibility(View.GONE);
			mLoadText.setText(R.string.load_finish);
			mLoadText.setTextColor(mContext.getResources().getColor(R.color.theme_item_count_color));
			break;

		default:
			break;
		}
    }
	
	/**
	 * 将ListView的adapter传进来做数据处理
	 * @param adapter
	 */
	public void setAdapter(BaseAdapter adapter){
		this.mAdapter = adapter;
	}

	/**
	 * 隐藏加载更多的footerview
	 */
	public void hideLoadMoreView(){
		setVisibility(View.GONE);
	}
	
	/**
	 * 显示加载更多的footerview
	 */
	public void showLoadMoreView(){

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
	public void onClick(View paramView) {
		// TODO Auto-generated method stub
		/*
		 * 如果已经加载完成就退出
		 */
		if(mLoadFinished){
			return;
		}
		
		if(mRingtonePhoneStatus == STATUS_LOADMORE) {
			setStatus(STATUS_LOADING);
			if (mCallback != null) {
				mCallback.onListViewLoadVieWLoadMore();
			}
		}
	}
}
