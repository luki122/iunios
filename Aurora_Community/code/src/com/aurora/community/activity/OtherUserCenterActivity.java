package com.aurora.community.activity;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.activity.PersonalCenter.RefreshMode;
import com.aurora.community.adapter.CollectionOfUserCenterAdapter;
import com.aurora.community.adapter.PublishOfUserCenterAdapter;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.Log;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.CollectionDataInfo;
import com.aurora.datauiapi.data.bean.CollectionOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.PublishDataInfo;
import com.aurora.datauiapi.data.bean.PublishOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.PublishPageInfo;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableManager;

public class OtherUserCenterActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_fragment_layout);
		setupViews();
		if(DefaultUtil.checkNetWork(this))
		{
		   requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
		   lv_usercenter.setVisibility(View.GONE);
		   loading_layout.setVisibility(View.VISIBLE);
		}else{
			showNetworkError();
		}
		setTabState();
		showPublish();
	}

	private int switchBgRes[] = { R.drawable.usercenter_tab_bg_select,
			R.drawable.usercenter_tab_bg_unselect };
	private int switchTextColor[] = { R.color.usercenter_tab_text_select,
			R.color.usercenter_tab_text_unselect };
	private int tabIconRes[] = { R.drawable.tab_publish_select,
			R.drawable.tab_publish_unselect, R.drawable.tab_collection_select,
			R.drawable.tab_collection_unselect };

	private TextView[] tabText = new TextView[2];
	private ImageView[] tabIcon = new ImageView[2];
	private LinearLayout[] tabLL = new LinearLayout[2];
	private TextView[] tabCount = new TextView[2];
	private TabType currentType = TabType.PUBLISH;
	private ImageView user_icon;
	private TextView user_screen_name;


	private Drawable tabDrawables[] = new Drawable[4];

	private PullToRefreshListView lv_usercenter;

	private ArrayList<PublishDataInfo> publishDatas = new ArrayList<PublishDataInfo>();
	private ArrayList<CollectionDataInfo> collectionDatas = new ArrayList<CollectionDataInfo>();

	private PublishOfUserCenterAdapter publishAdapter;
	private CollectionOfUserCenterAdapter collectionAdapter;
	private int publishLoadPage = 1, collectionLoadPage = 1;
	private RefreshMode refreshMode = RefreshMode.REFRESH_MODE;

	private ImageView iv_empty_show;
	private RelativeLayout network_layout,loading_layout;
	
	private static enum TabType {
		PUBLISH, COLLECTION;
	}

	private String userId,userNickName;

	private int emptyIcons[] = {R.drawable.other_no_publish,R.drawable.other_no_collection};
	
	private Drawable getTextLeftDrawable(int res) {
		Drawable drawable = getResources().getDrawable(res);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		return drawable;
	}

	@Override
	public void setupViews() {
		mComanager = new CommunityManager(this);
		setupUserInfo();
		setupListView();
		setupTabViews();
		network_layout = (RelativeLayout) findViewById(R.id.network_layout);
		loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
		findViewById(R.id.bt_retry_network).setOnClickListener(onClickListener);
		user_icon = (ImageView) findViewById(R.id.user_icon);
		user_screen_name = (TextView) findViewById(R.id.user_screen_name);
		mComanager = new CommunityManager(this);
	}
	
	private void showNetworkError(){
		network_layout.setVisibility(View.VISIBLE);
		lv_usercenter.setVisibility(View.GONE);
		loading_layout.setVisibility(View.GONE);
	}
	
	private void hideNetWorkNrror(){
		network_layout.setVisibility(View.GONE);
		lv_usercenter.setVisibility(View.VISIBLE);
	}
	
	
	private void showEmptyTip(PublishPageInfo info){
		if(info == null)
		{
			return;
		}
		iv_empty_show.setVisibility(View.GONE);
		if(currentType == TabType.PUBLISH && Integer.parseInt(info.getPosts()) == 0)
		{
			iv_empty_show.setVisibility(View.VISIBLE);
			iv_empty_show.setImageResource(emptyIcons[0]);
		}
		
		if(currentType == TabType.COLLECTION && Integer.parseInt(info.getFavorites()) == 0)
		{
			iv_empty_show.setVisibility(View.VISIBLE);
			iv_empty_show.setImageResource(emptyIcons[1]);
		}
	}
	
	public static final String USER_ID_KEY = "user_id",
			                      USER_NICK_KEY = "user_nick";
	private PublishPageInfo personInfo;
	private void setupUserInfo(){
		userId = getIntent().getStringExtra(USER_ID_KEY);
		userNickName = getIntent().getStringExtra(USER_NICK_KEY);
		setTitleText(userNickName);
	}

	private View headView;

	private void setupListView() {
		headView = LayoutInflater.from(this).inflate(
				R.layout.headview_of_usercenter, null);
		headView.findViewById(R.id.arrow).setVisibility(View.GONE);
		iv_empty_show = (ImageView) headView.findViewById(R.id.iv_empty_show);
		lv_usercenter = (PullToRefreshListView) findViewById(R.id.lv_usercenter);
		lv_usercenter.addHeaderView(headView);
		lv_usercenter.setPullLoadEnable(false);
		lv_usercenter.setXListViewListener(listViewListener);
		publishAdapter = new PublishOfUserCenterAdapter(publishDatas, this);
		collectionAdapter = new CollectionOfUserCenterAdapter(collectionDatas,
				this);
		lv_usercenter.setOnItemClickListener(itemClickListener);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg3 == -1) {
				return;
			}
			Intent intent = new Intent(OtherUserCenterActivity.this,PostDetailActivity.class);
			intent.putExtra(PostDetailActivity.USER_ID_KEY, currentType == TabType.PUBLISH ?
					publishDatas.get((int) arg3).getUid() : collectionDatas.get((int) arg3).getUid());
			intent.putExtra(PostDetailActivity.PAGE_ID_KEY,currentType == TabType.PUBLISH ? 
					publishDatas.get((int) arg3).getPid() : collectionDatas.get((int) arg3).getPid());
			startActivityForResult(intent, POST_DETAIL_REQUEST_CODE);
		}

	};

	private static final int REQ_PAGE_COUNT = 20;

	private boolean hasPublishLoadFinish = true,
			hasCollectionLoadFinish = true;

	private IXListViewListener listViewListener = new IXListViewListener() {

		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			if (refreshMode != RefreshMode.NONE) {
				return;
			}
			refreshMode = RefreshMode.REFRESH_MODE;
			if (currentType == TabType.PUBLISH) {
				publishLoadPage = 1;
				requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
			} else {
				collectionLoadPage = 1;
				requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
			}
		}

		@Override
		public void onLoadMore() {
			// TODO Auto-generated method stub
			if (refreshMode != RefreshMode.NONE) {
				return;
			}

			refreshMode = RefreshMode.LOAD_MORE_MODE;
			if (currentType == TabType.PUBLISH) {
				publishLoadPage++;
				requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
			} else {
				collectionLoadPage++;
				requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
			}

		}
	};

	private void setupTabViews() {
		tabLL[0] = (LinearLayout) findViewById(R.id.ll_publish);
		tabLL[1] = (LinearLayout) findViewById(R.id.ll_collection);
		tabIcon[0] = (ImageView) findViewById(R.id.tab_icon_publish);
		tabIcon[1] = (ImageView) findViewById(R.id.tab_icon_collection);
		tabText[0] = (TextView) findViewById(R.id.tab_text_publish);
		tabText[1] = (TextView) findViewById(R.id.tab_text_collection);
		tabCount[0] = (TextView) findViewById(R.id.tab_publish_count);
		tabCount[1] = (TextView) findViewById(R.id.tab_collection_count);
		tabLL[0].setOnClickListener(onClickListener);
		tabLL[1].setOnClickListener(onClickListener);
	}

	private void setTabState() {
		if (currentType == TabType.PUBLISH) {
			tabLL[0].setBackgroundResource(switchBgRes[0]);
			tabLL[1].setBackgroundResource(switchBgRes[1]);
			tabIcon[0].setImageResource(tabIconRes[0]);
			tabIcon[1].setImageResource(tabIconRes[3]);
			tabText[0].setTextColor(getColorByResId(switchTextColor[0]));
			tabText[1].setTextColor(getColorByResId(switchTextColor[1]));
			tabCount[0].setTextColor(getColorByResId(switchTextColor[0]));
			tabCount[1].setTextColor(getColorByResId(switchTextColor[1]));
		} else {
			tabLL[0].setBackgroundResource(switchBgRes[1]);
			tabLL[1].setBackgroundResource(switchBgRes[0]);
			tabIcon[0].setImageResource(tabIconRes[1]);
			tabIcon[1].setImageResource(tabIconRes[2]);
			tabText[0].setTextColor(getColorByResId(switchTextColor[1]));
			tabText[1].setTextColor(getColorByResId(switchTextColor[0]));
			tabCount[0].setTextColor(getColorByResId(switchTextColor[1]));
			tabCount[1].setTextColor(getColorByResId(switchTextColor[0]));
		}
	}

	@SuppressLint("NewApi")
	private int getColorByResId(int resId) {
		return this.getResources().getColor(resId);
	}

	private void showPublish() {
		lv_usercenter.setAdapter(publishAdapter);
		setLoadMoreState();
		showEmptyTip(personInfo);
	}

	private void showCollection() {
		lv_usercenter.setAdapter(collectionAdapter);
		if (collectionDatas.size() == 0) {
			refreshMode = RefreshMode.REFRESH_MODE;
			requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
		}
		setLoadMoreState();
		showEmptyTip(personInfo);
	}

	private void setUserInfo(PublishPageInfo info) {
		user_screen_name.setText(info.getNickname());
		ImageLoaderHelper.disPlay(info.getAvatar(), user_icon,
				DefaultUtil.getDefaultUserDrawable(this));
		tabCount[0].setText(info.getPosts());
		tabCount[1].setText(info.getFavorites());
		showEmptyTip(personInfo);
	}

	private void requestPublishData(int currentReqPage, int reqPageCount) {
		if(!DefaultUtil.checkNetWork(this))
		{
			showNetworkError();
			return;
		}else{
			hideNetWorkNrror();
		}
		mComanager.getPublishOfUserCenter(
				new DataResponse<PublishOfUserCenterHolder>() {
					@Override
					public void run() {
						super.run();
						if (value != null) {
							if (value.getReturnCode() == Globals.CODE_SUCCESS) {
								lv_usercenter.setVisibility(View.VISIBLE);
								loading_layout.setVisibility(View.GONE);
								if (refreshMode == RefreshMode.REFRESH_MODE) {
									publishDatas.clear();
									publishDatas.addAll(value.getData()
											.getDataContext());
								} else if (refreshMode == RefreshMode.LOAD_MORE_MODE) {
									publishDatas.addAll(value.getData()
											.getDataContext());
								}
								publishAdapter.notifyDataSetChanged();
								personInfo = value.getData().getPageuser();
								if (personInfo != null) {
									setUserInfo(personInfo);
								}

								if (publishDatas.size() >= value.getData()
										.getCountRow()) {
									hasPublishLoadFinish = true;
								} else {
									hasPublishLoadFinish = false;
								}
								setLoadMoreState();
							}
						}
						lv_usercenter.stopLoadMore();
						lv_usercenter.stopRefresh();
						refreshMode = RefreshMode.NONE;
					}
				}, currentReqPage, reqPageCount, userId);
	}

	private void requestCollectionData(int currentReqPage, int reqPageCount) {
		if(!DefaultUtil.checkNetWork(this))
		{
			showNetworkError();
			return;
		}else{
			hideNetWorkNrror();
		}
		mComanager.getCollectionOfUserCenter(
				new DataResponse<CollectionOfUserCenterHolder>() {
					@Override
					public void run() {
						super.run();
						if (value != null) {
							if (value.getReturnCode() == Globals.CODE_SUCCESS) {
								if (refreshMode == RefreshMode.REFRESH_MODE) {
									collectionDatas.clear();
									collectionDatas.addAll(value.getData()
											.getDataContext());
								} else if (refreshMode == RefreshMode.LOAD_MORE_MODE) {
									collectionDatas.addAll(value.getData()
											.getDataContext());
								}
								collectionAdapter.notifyDataSetChanged();
								personInfo = value.getData().getPageuser();
								if (personInfo != null) {
									setUserInfo(personInfo);
								}
							}
							if (collectionDatas.size() >= value.getData()
									.getCountRow()) {
								hasCollectionLoadFinish = true;
							} else {
								hasCollectionLoadFinish = false;
							}
							setLoadMoreState();
						}
						lv_usercenter.stopLoadMore();
						lv_usercenter.stopRefresh();
						refreshMode = RefreshMode.NONE;
					}
				}, currentReqPage, reqPageCount, userId);

	}

	private void setLoadMoreState() {
		if (currentType == TabType.PUBLISH) {
			if (hasPublishLoadFinish) {
				lv_usercenter.setPullLoadEnable(false);
			} else {
				lv_usercenter.setPullLoadEnable(true);
			}

		} else {
			if (hasCollectionLoadFinish) {
				lv_usercenter.setPullLoadEnable(false);
			} else {
				lv_usercenter.setPullLoadEnable(true);
			}

		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	private static final int POST_DETAIL_REQUEST_CODE = 0X4587;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == POST_DETAIL_REQUEST_CODE)
		{
			if(resultCode == Activity.RESULT_OK)
			{
				refreshPage();
			}
		}
		
	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		if(itemId == BACK_ITEM_ID)
		{
			finish();
		}
	}
	
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(refreshMode != RefreshMode.NONE)
			{
				return;
			}
			switch (v.getId()) {
			case R.id.ll_publish:
				if(currentType == TabType.PUBLISH)
				{
					return;
				}
				currentType = TabType.PUBLISH;
				showPublish();
				break;
			case R.id.ll_collection:
				if(currentType == TabType.COLLECTION)
				{
					return;
				}
				currentType = TabType.COLLECTION;
				showCollection();
				break;
			case R.id.bt_retry_network:
				if(!DefaultUtil.checkNetWork(OtherUserCenterActivity.this))
				{
					return;
				}
				refreshPage();
				break;
			}
			setTabState();
		}
	};

	private void refreshPage() {
		if (currentType == TabType.PUBLISH) {
			refreshMode = RefreshMode.REFRESH_MODE;
			if (publishDatas.size() == 0) {
				requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
			} else {
				requestPublishData(1, publishDatas.size());
			}
		} else {
			refreshMode = RefreshMode.REFRESH_MODE;
			if (collectionDatas.size() == 0) {
				requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
			} else {
				requestCollectionData(1, collectionDatas.size());
			}
		}
	}
     private Handler errorHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			lv_usercenter.stopLoadMore();
			lv_usercenter.stopRefresh();
			showNetworkError();
		};
	};
	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		super.onError(code, message, manager, e);
		errorHandler.sendEmptyMessage(0);
	}

}
