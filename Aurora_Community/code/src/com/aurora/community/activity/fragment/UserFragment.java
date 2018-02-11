package com.aurora.community.activity.fragment;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.community.R;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.activity.PostDetailActivity;
import com.aurora.community.activity.PersonalCenter.RefreshMode;
import com.aurora.community.activity.account.AccountInfoActivity;
import com.aurora.community.adapter.CollectionOfUserCenterAdapter;
import com.aurora.community.adapter.PublishOfUserCenterAdapter;
import com.aurora.community.totalCount.TotalCount;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.AccountUtil;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.CollectionDataInfo;
import com.aurora.datauiapi.data.bean.CollectionOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.PublishDataInfo;
import com.aurora.datauiapi.data.bean.PublishOfUserCenterHolder;
import com.aurora.datauiapi.data.bean.PublishPageInfo;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.umeng.analytics.MobclickAgent;

/**
 * 个人fragment
 * 
 * @author j
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UserFragment extends BaseFragment {

	private static final String TAG = "UserFragment";

	private View mRootView;

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

	private Activity activity = null;

	private Drawable tabDrawables[] = new Drawable[4];

	private PullToRefreshListView lv_usercenter;

	private ArrayList<PublishDataInfo> publishDatas = new ArrayList<PublishDataInfo>();
	private ArrayList<CollectionDataInfo> collectionDatas = new ArrayList<CollectionDataInfo>();

	private PublishOfUserCenterAdapter publishAdapter;
	private CollectionOfUserCenterAdapter collectionAdapter;
	private CommunityManager netManage;

	private int publishLoadPage = 1, collectionLoadPage = 1;

	private RefreshMode refreshMode = RefreshMode.REFRESH_MODE;

	private ImageView iv_empty_show;
	private ImageView arrow;

	private int emptyIcons[] = { R.drawable.no_publish_tip,
			R.drawable.no_collection_tip };

	private PublishPageInfo personInfo = null;

	public static enum TabType {
		PUBLISH, COLLECTION;
	}

	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			refreshMode = RefreshMode.NONE;
			lv_usercenter.stopLoadMore();
			lv_usercenter.stopRefresh();
		};
	};
	
	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		super.onError(code, message, manager, e);
		handler.sendEmptyMessage(0);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		netManage = new CommunityManager(this);
		setTabState();
		showPublish();
/*		if ((publishDatas.size() == 0) || (collectionDatas.size() == 0))
			refreshPage();*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.user_fragment_layout, container,
				false);
		return mRootView;
	}

	private Drawable getTextLeftDrawable(int res) {
		Drawable drawable = getResources().getDrawable(res);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		return drawable;
	}

	@Override
	public void setupViews() {
		setupListView();
		setupTabViews();
		user_icon = (ImageView) mRootView.findViewById(R.id.user_icon);
		user_screen_name = (TextView) mRootView
				.findViewById(R.id.user_screen_name);
	}

	private View headView;

	private void setupListView() {
		headView = LayoutInflater.from(activity).inflate(
				R.layout.headview_of_usercenter, null);
		iv_empty_show = (ImageView) headView.findViewById(R.id.iv_empty_show);
		arrow = (ImageView) headView.findViewById(R.id.arrow);
		if (AccountUtil.getInstance().isIuniOS()) {
			arrow.setVisibility(View.GONE);
		} else {
			arrow.setVisibility(View.VISIBLE);
		}
		
		lv_usercenter = (PullToRefreshListView) mRootView
				.findViewById(R.id.lv_usercenter);
		
		lv_usercenter.addHeaderView(headView);
		lv_usercenter.setPullLoadEnable(false);
		lv_usercenter.setXListViewListener(listViewListener);
		publishAdapter = new PublishOfUserCenterAdapter(publishDatas,
				getActivity());
		collectionAdapter = new CollectionOfUserCenterAdapter(collectionDatas,
				getActivity());
		lv_usercenter.setOnItemClickListener(itemClickListener);
		
		headView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (!AccountUtil.getInstance().isIuniOS() && AccountUtil.getInstance().isLogin()) {
					Intent i = new Intent(activity, AccountInfoActivity.class);
					getActivity().startActivityForResult(i, Globals.REQUEST_LOGOUT_CODE);
					MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_EDIT);
				}
			}
		});
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg3 == -1) {
				return;
			}
			Intent intent = new Intent(getActivity(), PostDetailActivity.class);
			intent.putExtra(
					PostDetailActivity.USER_ID_KEY,
					currentType == TabType.PUBLISH ? publishDatas.get(
							(int) arg3).getUid() : collectionDatas.get(
							(int) arg3).getUid());
			intent.putExtra(
					PostDetailActivity.PAGE_ID_KEY,
					currentType == TabType.PUBLISH ? publishDatas.get(
							(int) arg3).getPid() : collectionDatas.get(
							(int) arg3).getPid());
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

			if (currentType == TabType.PUBLISH) {
				if (publishDatas.size() < REQ_PAGE_COUNT) {
					refreshMode = RefreshMode.REFRESH_MODE;
					publishLoadPage = 1;
				} else {
					refreshMode = RefreshMode.LOAD_MORE_MODE;
					publishLoadPage++;
				}
				requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
			} else {
				if (collectionDatas.size() < REQ_PAGE_COUNT) {
					refreshMode = RefreshMode.REFRESH_MODE;
					collectionLoadPage = 1;
				} else {
					refreshMode = RefreshMode.LOAD_MORE_MODE;
					collectionLoadPage++;
				}
				requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
			}

		}
	};

	private void setupTabViews() {
		tabLL[0] = (LinearLayout) mRootView.findViewById(R.id.ll_publish);
		tabLL[1] = (LinearLayout) mRootView.findViewById(R.id.ll_collection);
		tabIcon[0] = (ImageView) mRootView.findViewById(R.id.tab_icon_publish);
		tabIcon[1] = (ImageView) mRootView
				.findViewById(R.id.tab_icon_collection);
		tabText[0] = (TextView) mRootView.findViewById(R.id.tab_text_publish);
		tabText[1] = (TextView) mRootView
				.findViewById(R.id.tab_text_collection);
		tabCount[0] = (TextView) mRootView.findViewById(R.id.tab_publish_count);
		tabCount[1] = (TextView) mRootView
				.findViewById(R.id.tab_collection_count);
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
		return getActivity().getResources().getColor(resId);
	}

	private void showPublish() {
		lv_usercenter.setAdapter(publishAdapter);
		if (publishDatas.size() == 0) {
			refreshMode = RefreshMode.REFRESH_MODE;
			requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
		}
		setLoadMoreState();
		showEmptyTip(personInfo);
	}

	private void showCollection() {
		lv_usercenter.setAdapter(collectionAdapter);
		if (collectionDatas.size() == 0) {
			refreshMode = RefreshMode.REFRESH_MODE;
			requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
		}
		showEmptyTip(personInfo);
		setLoadMoreState();
	}

	private void showEmptyTip(PublishPageInfo info) {
		if (info == null) {
			return;
		}
		iv_empty_show.setVisibility(View.GONE);
		if (currentType == TabType.PUBLISH
				&& Integer.parseInt(info.getPosts()) == 0) {
			iv_empty_show.setVisibility(View.VISIBLE);
			iv_empty_show.setImageResource(emptyIcons[0]);
		}

		if (currentType == TabType.COLLECTION
				&& Integer.parseInt(info.getFavorites()) == 0) {
			iv_empty_show.setVisibility(View.VISIBLE);
			iv_empty_show.setImageResource(emptyIcons[1]);
		}
	}

	
	private void setUserInfo(PublishPageInfo info) {
		user_screen_name.setText(info.getNickname());
		ImageLoaderHelper.disPlay(info.getAvatar(), user_icon,
				DefaultUtil.getDefaultUserDrawable(activity));
		tabCount[0].setText(info.getPosts());
		tabCount[1].setText(info.getFavorites());
		showEmptyTip(info);
	}

	public void requestPublishData(int currentReqPage, int reqPageCount) {
		netManage.getPublishOfUserCenter(
				new DataResponse<PublishOfUserCenterHolder>() {
					@Override
					public void run() {
						super.run();
						if (value != null) {
							if (value.getReturnCode() == Globals.CODE_SUCCESS) {
								if (refreshMode == RefreshMode.REFRESH_MODE) {
									publishDatas.clear();
								}

								publishDatas.addAll(value.getData()
										.getDataContext());
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
				}, currentReqPage, reqPageCount, AccountHelper.user_id);
	}

	public void requestCollectionData(int currentReqPage, int reqPageCount) {
		netManage.getCollectionOfUserCenter(
				new DataResponse<CollectionOfUserCenterHolder>() {
					@Override
					public void run() {
						super.run();
						if (value != null) {
							if (value.getReturnCode() == Globals.CODE_SUCCESS) {
								if (refreshMode == RefreshMode.REFRESH_MODE) {
									collectionDatas.clear();
								}
								collectionDatas.addAll(value.getData()
										.getDataContext());
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
				}, currentReqPage, reqPageCount, AccountHelper.user_id);

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

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (refreshMode != RefreshMode.NONE)
				return;
			switch (v.getId()) {
			case R.id.ll_publish:
				if (currentType == TabType.PUBLISH) {
					return;
				}
				currentType = TabType.PUBLISH;
				/*new TotalCount(getActivity(), "300", "014", 1)
				.CountData();*/
				MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_PUBLISH3);
				showPublish();
				break;
			case R.id.ll_collection:
				if (currentType == TabType.COLLECTION) {
					return;
				}
				currentType = TabType.COLLECTION;
				showCollection();
				/*new TotalCount(getActivity(), "300", "015", 1)
				.CountData();*/
				MobclickAgent.onEvent(getActivity(), Globals.PREF_TIMES_COLLECTION);
				break;
			}
			setTabState();
		}
	};

	public void refreshPageForNewPublish() {
		tabLL[0].performClick();
		lv_usercenter.setSelection(0);
		refreshMode = RefreshMode.REFRESH_MODE;
		requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
	}

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

	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		this.activity = activity;
		super.onAttach(activity);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);

		if(!hidden)
		{
			((MainActivity)activity).setTitleText(getString(R.string.user_center_title));
			if(refreshMode != RefreshMode.NONE)
			{
				return;
			}
			if(currentType == TabType.PUBLISH)
			{
				if(publishDatas.size() == 0)
				{
					refreshMode = RefreshMode.REFRESH_MODE;
					requestPublishData(publishLoadPage, REQ_PAGE_COUNT);
				}
				
			}else{
				if(collectionDatas.size() == 0)
				{
					refreshMode = RefreshMode.REFRESH_MODE;
					requestCollectionData(collectionLoadPage, REQ_PAGE_COUNT);
				}
			}
		}
	}

	private static final int POST_DETAIL_REQUEST_CODE = 0X4587;

	public void changeAccount(){
		collectionDatas.clear();
		publishDatas.clear();
		currentType = TabType.PUBLISH;
		publishLoadPage = 1;
		collectionLoadPage = 1;
		if (publishAdapter != null) {
			publishAdapter.notifyDataSetChanged();
		}
		if (collectionAdapter != null) {
			collectionAdapter.notifyDataSetChanged();
		}
		if (lv_usercenter != null) {
			lv_usercenter.setAdapter(publishAdapter);
		}
		refreshPage();
	}
	
	public void logOut(){
		collectionDatas.clear();
		publishDatas.clear();
		currentType = TabType.PUBLISH;
		publishLoadPage = 1;
		collectionLoadPage = 1;
		if (publishAdapter != null) {
			publishAdapter.notifyDataSetChanged();
		}
		if (collectionAdapter != null) {
			collectionAdapter.notifyDataSetChanged();
		}
		if (lv_usercenter != null) {
			lv_usercenter.setAdapter(publishAdapter);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == POST_DETAIL_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				refreshPage();
			}
		}

	}

	@Override
	public void handleMessage(Message msg) {
		if(msg.what == Globals.NO_NETWORK)
		{
			((MainActivity) getActivity()).handleMessage(getTag());
		}
	}

}
