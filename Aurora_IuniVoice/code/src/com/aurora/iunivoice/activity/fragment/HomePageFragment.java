package com.aurora.iunivoice.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.HomepageDataInfo;
import com.aurora.datauiapi.data.bean.HomepageListObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.PageDetailActivity;
import com.aurora.iunivoice.adapter.HomePageListAdapter;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.LoadingPageUtil;
import com.aurora.iunivoice.utils.LoadingPageUtil.OnHideListener;
import com.aurora.iunivoice.utils.LoadingPageUtil.OnRetryListener;
import com.aurora.iunivoice.utils.LoadingPageUtil.OnShowListener;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.ToastUtil;
import com.aurora.iunivoice.widget.FrameBannerView;

public class HomePageFragment extends BaseViewPagerFragment implements
		IXListViewListener, AdapterView.OnItemClickListener, OnClickListener {

	private static final String TAG = "HomePageFragment";

	private IuniVoiceManager mManager;

	private LoadingPageUtil loadingPageUtil;

	private PullToRefreshListView listView;
	private View headerView;
	private FrameBannerView bannerView;
	private TextView tv_sign_daily;
	private TextView tv_newguide;

	private String formhash;
	private String newguide;

	private List<HomepageDataInfo> infoDataList;
	private HomePageListAdapter adapter;

	public static List<String> mRecords = new ArrayList<String>();

	private int page = 1;
	private int tpp = 10;
	// 是否正在加载
	private boolean isLoading = false;

	// 数据是否加载完毕

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, "HomePageFragment onActivityCreated()");
		super.onActivityCreated(savedInstanceState);

		// 由于是首页需要首先加载，别的页不需要手动调用
		checkAndLoadData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		loadingPageUtil.clearRegister();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home_page, container, false);
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case Globals.NETWORK_ERROR:
			isLoading = false;
			if (loadingPageUtil.isShowing()) {
				loadingPageUtil.showNetworkError();
			}
			break;
		case Globals.NO_NETWORK:
			isLoading = false;
			if (loadingPageUtil.isShowing()) {
				loadingPageUtil.showNoNetWork();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void setupViews() {
		listView = (PullToRefreshListView) getView()
				.findViewById(R.id.listView);

		headerView = LayoutInflater.from(getActivity()).inflate(
				R.layout.view_homepage_header, null);
		bannerView = (FrameBannerView) headerView.findViewById(R.id.banner_ad);
		tv_newguide = (TextView) headerView.findViewById(R.id.tv_newguide);
		tv_sign_daily = (TextView) headerView.findViewById(R.id.tv_sign_daily);
		listView.addHeaderView(headerView);

		listView.setPullRefreshEnable(false);
		listView.setPullLoadEnable(true);

		tv_newguide.setOnClickListener(this);
		tv_sign_daily.setOnClickListener(this);
	}

	private void initData() {
		Log.i(TAG, "HomePageFragment initData()");

		mManager = new IuniVoiceManager(this);

		infoDataList = new ArrayList<HomepageDataInfo>();
		adapter = new HomePageListAdapter(getActivity(), infoDataList);

		listView.setAdapter(adapter);
		listView.setXListViewListener(this);
		listView.setOnItemClickListener(this);

		initLoadingPage();
		getNetData();
	}

	@Override
	public void onRefresh() {
		Log.i(TAG, "HomePageFragment onRefresh()");
		page = 1;
		getNetData();
	}

	@Override
	public void onLoadMore() {
		if(!SystemUtils.hasNetwork()){
			//ToastUtil.shortToast("无可用网络，请检查网络链接是否打开");
			ToastUtil.shortToast(R.string.network_not_available);
			listView.stopLoadMore();
			return ;
		}
		Log.i(TAG, "HomePageFragment onLoadMore() page: " + page + 1);
		page++;
		getNetData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_newguide:
			if (!TextUtils.isEmpty(newguide)) {
				Intent intent = new Intent(getActivity(), PageDetailActivity.class);
				intent.putExtra(PageDetailActivity.PAGE_ID_KEY, newguide);
				intent.putExtra(PageDetailActivity.PAGE_TITLE_KEY, tv_newguide.getText().toString());
				intent.putExtra(PageDetailActivity.FORM_HASH_KEY, formhash);
				startActivity(intent);
			}
			break;
		case R.id.tv_sign_daily:
			if (!AccountUtil.getInstance().isLogin()) {
				AccountUtil.getInstance().startLogin(getActivity());
			} else {
				mManager.signDaily(new DataResponse<BaseResponseObject>() {
					@Override
					public void run() {
						if (value != null) {
							if (value.getReturnCode() == 0) { // 成功
								ToastUtil.shortToast(value.getMsg());
							} else {
								ToastUtil.shortToast(value.getMsg());
							}
						}
					}
				}, formhash);
			}

			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		try {
			int index = i - listView.getHeaderViewsCount(); // 由于使用PullToRefreshListView，需要减掉加上的headerView

			if (!mRecords.contains(infoDataList.get(index).getTid()))
				mRecords.add(infoDataList.get(index).getTid());

			Intent intent = new Intent(getActivity(), PageDetailActivity.class);
			intent.putExtra(PageDetailActivity.PAGE_ID_KEY,
					infoDataList.get(index).getTid());
			intent.putExtra(PageDetailActivity.PAGE_TITLE_KEY, infoDataList
					.get(index).getSubject());
			intent.putExtra(PageDetailActivity.PAGE_DESCRIBE_KEY, infoDataList
					.get(index).getPortal_summary());
			intent.putExtra(PageDetailActivity.FORM_HASH_KEY, formhash);
			startActivityForResult(intent, Globals.REQUEST_POSTDETAIL_CODE);
		} catch (ArrayIndexOutOfBoundsException e) {
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Globals.REQUEST_POSTDETAIL_CODE:
			if (resultCode == Activity.RESULT_OK) {
				onRefresh();
			}else{
				if (adapter != null)
					adapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void loadData() {
		initData();
	}

	private void getNetData() {
		if (isLoading) {
			return;
		}
		isLoading = true;

		mManager.getHomePageList(new DataResponse<HomepageListObject>() {
			@Override
			public void run() {
				Log.i(TAG, "getForumInfo run()");
				if (value != null) {
					if (!TextUtils.isEmpty(value.getFormhash())) {
						formhash = value.getFormhash();
					}
					if (value.getData() == null) {
						isLoading = false;
						if (loadingPageUtil.isShowing()) {
							loadingPageUtil.showNetworkError();
						}
						return;
					}
					
					if (!TextUtils.isEmpty(value.getData().getNewguide())) {
						newguide = value.getData().getNewguide();
					}

					List<HomepageDataInfo> dataList = value.getData()
							.getForum_threadlist();
					if (dataList != null) {
						if (page == 1) {
							infoDataList.clear();
							loadingPageUtil.hideLoadPage();
						}

						infoDataList.addAll(dataList);
						adapter.notifyDataSetChanged();

						listView.stopRefresh();
						listView.stopLoadMore();
					}

					// 是否最后一页
					if (dataList.size() < tpp) {
						listView.setPullLoadEnable(false);
					}

					// 加载第一页时设置banner
					if (page == 1 && value.getData().getSlides() != null) {
						bannerView.setImages(value.getData().getSlides());
						bannerView.setFormhash(formhash);
					}
				}

				isLoading = false;
			}
		}, page, tpp);
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(getActivity(), getView());
		loadingPageUtil.setOnRetryListener(new OnRetryListener() {
			@Override
			public void retry() {
				getNetData();// 重新加载数据
			}
		});
		loadingPageUtil.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow() {
				listView.setVisibility(View.GONE);
			}
		});
		loadingPageUtil.setOnHideListener(new OnHideListener() {
			@Override
			public void onHide() {
				listView.setVisibility(View.VISIBLE);
			}
		});
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();
	}

	public String getFormhash() {
		return formhash;
	}

}
