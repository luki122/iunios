package com.aurora.community.activity.fragment;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.aurora.community.R;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.activity.PostDetailActivity;
import com.aurora.community.adapter.NewsStickyListAdapter;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.Log;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.ArticleDataInfo;
import com.aurora.datauiapi.data.bean.ArticleHolder;
import com.aurora.datauiapi.data.implement.DataResponse;

public class NewsFragment extends BaseFragment implements IXListViewListener,
		AdapterView.OnItemClickListener {

	private static final String TAG = "NewsFragment";

	private StickyListHeadersListView mNewsListView;

	private NewsStickyListAdapter adapter;

	private RefreshMode mRefreshMode = RefreshMode.NONE;

	private String tid = "";
	private int page = 1;
	private final int count = 5;

	private ArrayList<ArticleDataInfo> orderNewsList = new ArrayList<ArticleDataInfo>();

	public enum RefreshMode {
		REFRESH_MODE, LOAD_MORE_MODE, NONE
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("linp", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		mComanager = new CommunityManager(this);
		setRefreshMode(RefreshMode.REFRESH_MODE);
		getArticleInfo(1);

	}

	private String postCount = "0";
	
	public void setTid(String tid,String postCount) {
		this.tid = tid;
		this.postCount = postCount;
	}

	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		Log.e("linp", "onHiddenChanged");
		if (!hidden) {
			// 清空上次数据
			orderNewsList.clear();
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
			hideFinish();
			mNewsListView.setPullLoadEnable(true);
			mNewsListView.showLoadMore();
			getArticleInfo(1);
			MainActivity actvity = (MainActivity) getActivity();
			if (actvity != null) {
				actvity.enableBackItem(true);
			}
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("linp", "onCreateView");
		return inflater
				.inflate(R.layout.news_fragment_layout, container, false);
	}

	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		mNewsListView = (StickyListHeadersListView) getView().findViewById(
				R.id.lv_news);
		mNewsListView.setOnItemClickListener(this);

		mNewsListView.setXListViewListener(this);
		mNewsListView.setPullLoadEnable(true);
		setupStickyListView();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.e("linp", "onResume");
		super.onResume();
	}

	/*
	 * private void getNewsInfo(String url) { mComanager.getNewsInfo(new
	 * DataResponse<NewsInfoHolder>() {
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub
	 * if(value.getInfo()!=null){ Log.e("linp", "~~~~~~~~~~getNewsInfo"); list =
	 * orderNewsListByRefreshMode(value .getInfo()); if (adapter != null) {
	 * adapter.notifyDataSetChanged(); } else { setupStickyListView(); }
	 * 
	 * } mNewsListView.stopLoadMore(); mNewsListView.stopRefresh();
	 * setRefreshMode(RefreshMode.NONE); } }); }
	 */
	public void getArticleInfo(int page) {
		if (TextUtils.isEmpty(tid))
			return;
		mComanager.getArticleInfo(new DataResponse<ArticleHolder>() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mNewsListView.stopLoadMore();
				mNewsListView.stopRefresh();
				
				if (value != null) {
					Log.e("linp", "~~~~~~~~~~getNewsInfo");
					if(!value.getData().getCountRow().equals(postCount))
					{
						sendRefreshBroadcast();
					}
					orderNewsListByRefreshMode(value.getData()
							.getDataContext());
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					} else {
						setupStickyListView();
					}
					if(getActivity()!=null){
						((MainActivity)getActivity()).hideNoNetWorkLayer();
					}
				}
				
				setRefreshMode(RefreshMode.NONE);
			}
		}, tid, page, count);
	}

	private void sendRefreshBroadcast(){
		Intent intent = new Intent(MainActivity.REFRESH_MAIN_CATEGORY_ACTION);
		getActivity().sendBroadcast(intent);
	}
	
	private void setupStickyListView() {
		adapter = new NewsStickyListAdapter(getActivity(),
				orderNewsList);
		mNewsListView.setAdapter(adapter);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		hideFinish();
		setRefreshMode(RefreshMode.REFRESH_MODE);
		page = 1;
		getArticleInfo(page);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		page++;
		setRefreshMode(RefreshMode.LOAD_MORE_MODE);
		getArticleInfo(page);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		try {
			Intent intent = new Intent(getActivity(), PostDetailActivity.class);
			intent.putExtra(PostDetailActivity.PAGE_ID_KEY,
					orderNewsList.get((int) arg3).getPid());
			intent.putExtra(PostDetailActivity.USER_ID_KEY,
					orderNewsList.get((int) arg3).getUid());
			startActivityForResult(intent, Globals.REQUEST_POSTDETAIL_CODE);
		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO: handle exception
			Log.e("linp", "###########NewsFragment onItemClick ArrayIndexOutOfBoundsException="+e);
		}
	
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.i("zhangwei", "zhangwei the requestCode=" + requestCode
				+ " the resultCode=" + resultCode);
		switch (requestCode) {
		case Globals.REQUEST_POSTDETAIL_CODE:
			if (resultCode == Activity.RESULT_OK) {
				onRefresh();
			}
			break;
		default:
			break;
		}
	}

	public void setRefreshMode(RefreshMode mode) {
		this.mRefreshMode = mode;
	}

	public RefreshMode getRefreshMode() {
		return this.mRefreshMode;
	}

	private ArrayList<ArticleDataInfo> orderNewsListByRefreshMode(
			ArrayList<ArticleDataInfo> source) {
		
		if (source == null) {
			loadFinish();
		} else {
			switch (getRefreshMode()) {
			case REFRESH_MODE:
				Log.e("linp", "~~~~~~~~~~~~~~~~~~~~~~~~REFRESH_MODE");
				orderNewsList.clear();
				for (int i = (source.size()); i > 0;) {
					i--;
					ArticleDataInfo info = source.get(i);
					orderNewsList.add(0, info);
					// setNewsInfoType(info);
				}
				break;
			case LOAD_MORE_MODE:
				Log.e("linp", "~~~~~~~~~~~~~~~~~~~~~~~~LOAD_MORE_MODE");
				for (ArticleDataInfo info : source) {
					orderNewsList.add(info);
					// setNewsInfoType(info);
				}
				// 是否已经是最后一页
				if (source.size() < count) {
					loadFinish();
				}
				break;
			}
		}
		Log.e("linp", "~~~~~~~~~~~~~~~~~~~~~~~~3");
		return orderNewsList;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		if(getActivity()!=null){
			((MainActivity) getActivity()).handleMessage(getTag());
		}
		
	}
	
	/**
	* @Title: loadFinish
	* @Description: TODO 显示已加载完
	* @param 
	* @return void
	* @throws
	 */
	private void loadFinish() {
		mNewsListView.showFinish();
	}
	
	/**
	* @Title: hideFinish
	* @Description: TODO 隐藏已加载完
	* @param 
	* @return void
	* @throws
	 */
	private void hideFinish() {
		page = 1;
		mNewsListView.hideFinish();
	}

}
