package com.aurora.market.activity.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.widget.AuroraListView;

import com.aurora.datauiapi.data.ManagerThread;
import com.aurora.datauiapi.data.MarketManager;
import com.aurora.datauiapi.data.bean.CategoryListObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.CategoryActivity;
import com.aurora.market.adapter.CategoryAdapter;
import com.aurora.market.adapter.CategoryAdapter.CustomOnItemClickListener;
import com.aurora.market.db.CacheDataAdapter;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.util.DataFromUtils;
import com.aurora.market.util.Globals;
import com.aurora.market.util.LoadingPageUtil;
import com.aurora.market.util.LoadingPageUtil.OnHideListener;
import com.aurora.market.util.LoadingPageUtil.OnRetryListener;
import com.aurora.market.util.LoadingPageUtil.OnShowListener;
import com.aurora.market.util.Log;

public class CategoryFragment extends Fragment implements INotifiableController {

	public static final String TAG = "CategoryFragment";
	private static final String AGRS_TYPE = "type";

	private LoadingPageUtil loadingPageUtil;

	private String type;

 	private AuroraListView mListView;
 	private CategoryAdapter categoryAdapter;
	
	// 加载更多面板
	private LinearLayout loadMoreView;
	// 底面板加载更多字段控件
	private TextView forum_foot_more;
	private ProgressBar foot_progress;
	// 数据是否加载完毕
	private boolean isLoadDataFinish = false;
	private ManagerThread thread;

	private CategoryListObject obj = new CategoryListObject();
	private MarketManager mmarketManager;
	private MyBroadcastReciver broadcastReceiver;
	private SharedPreferences app_update;
	private int update_status = 0;
	private Activity m_activity;
	private String style = "iunios";
	private boolean isCacheData = false;
	public static CategoryFragment newInstance(String type) {
		CategoryFragment f = new CategoryFragment();
		Bundle b = new Bundle();
		b.putCharSequence(AGRS_TYPE, type);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			type = args.getString(AGRS_TYPE);
		}
	}
	
	@Override
	public void onPause() {
		mListView.auroraOnPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		mListView.auroraOnResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		thread.quit();
		mmarketManager.setController(null);
		m_activity.unregisterReceiver(broadcastReceiver);

		m_activity = null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app_update = m_activity.getSharedPreferences(Globals.SHARED_APP_UPDATE,
				m_activity.MODE_APPEND);
		update_status = app_update.getInt(
				Globals.SHARED_DOWNORUPDATE_KEY_ISEXITS, 0);
		initViews();
		setAdapter();
		initLoadingPage();
		getifCache();
		mmarketManager = new MarketManager();
		thread = new ManagerThread(mmarketManager);
		thread.market(this);
		initData();
		initBroadCast();
	}
	private void getifCache()
	{
		CacheDataAdapter ldb = new CacheDataAdapter(m_activity);
		ldb.open();
		String result = ldb.queryCacheByType(AppListActivity.TYPE_CATEGORY_MAIN,type,0,0);
		if(null != result)
			isCacheData = true;
		ldb.close();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_category, container,
				false);
		 return view;
	}

	private void initViews() {
		View view = getView();
		mListView = (AuroraListView) view.findViewById(R.id.mListView);
		loadMoreView = (LinearLayout) m_activity.getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		loadMoreView.setClickable(false);
		loadMoreView.setLongClickable(false);
		forum_foot_more = (TextView) loadMoreView
				.findViewById(R.id.listview_foot_more);
		foot_progress = (ProgressBar) loadMoreView
				.findViewById(R.id.listview_foot_progress);
		if (update_status == 0) {
			setUpdateSign(0);
		} else {
			setUpdateSign(1);
		}
		
		mListView.addFooterView(loadMoreView);
	}

	private void setUpdateSign(int sign_type) {
		if (m_activity == null) {
			return;
		}

		if (sign_type == 0) {
			((CategoryActivity) m_activity).main_update.setVisibility(View.GONE);
					
		} else {
			((CategoryActivity) m_activity).main_update.setVisibility(View.VISIBLE);
			
		}

	}

	private void setAdapter() {
		if(null != m_activity)
		{
			categoryAdapter = new CategoryAdapter(m_activity, obj.getCategories());
			categoryAdapter
					.setCustomOnItemClickListener(new CustomOnItemClickListener() {
						@Override
						public void onItemClick(View view, int position) {
							Intent i = new Intent(m_activity, AppListActivity.class);
							i.putExtra(AppListActivity.OPEN_TYPE,
									AppListActivity.TYPE_CATEGORY);
							i.putExtra(AppListActivity.CATEGORY_NAME, obj
									.getCategories().get(position).getName());
							i.putExtra(AppListActivity.CATEGORY_ID, obj
									.getCategories().get(position).getId());
							m_activity.startActivity(i);
						}
					});
			mListView.setAdapter(categoryAdapter);
		}
	}

	private void initData() {
		isLoadDataFinish = false;
		getNetData();
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(m_activity, getView());
		loadingPageUtil.setOnRetryListener(new OnRetryListener() {
			@Override
			public void retry() {
				initData();
			}
		});
		loadingPageUtil.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow() {
				mListView.setVisibility(View.GONE);
			}
		});
		loadingPageUtil.setOnHideListener(new OnHideListener() {
			@Override
			public void onHide() {
				mListView.setVisibility(View.VISIBLE);
			}
		});
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();
	}

	/**
	 * @Title: initBroadCast
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param
	 * @return void
	 * @throws
	 */
	private void initBroadCast() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Globals.BROADCAST_ACTION_DOWNLOAD);
		intentFilter.addAction(Globals.MARKET_UPDATE_ACTION);
		broadcastReceiver = new MyBroadcastReciver();
		m_activity.registerReceiver(broadcastReceiver, intentFilter);
	}

	private class MyBroadcastReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Globals.BROADCAST_ACTION_DOWNLOAD)
					|| action.equals(Globals.MARKET_UPDATE_ACTION)) {

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						getUpAppSign();
					}
				}, 1000);

			}
		}

	}

	private void getNetData() {
		mmarketManager.getCategoryListItems(
				new DataResponse<CategoryListObject>() {
					public void run() {
						if (value != null) {
							isCacheData = false;
							isLoadDataFinish = true;
							obj = value;
							disView();
						}
					}
				}, m_activity, type,style,isCacheData);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.e(TAG, "onAttach");

		m_activity = activity;
	}

	private void getUpAppSign() {

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (m_activity == null) {
					return;
				}

				int count = AppDownloadService.getDownloaders().size();
				int sum = 0;
				if (count == 0) {
					DataFromUtils up_data = new DataFromUtils();
					sum = up_data.getUpdateSum(m_activity);
				}
				if ((sum > 0) || (count > 0)) {
					if (null != m_activity) {
						m_activity.runOnUiThread(new Runnable() {
							public void run() {

								setUpdateSign(1);

							}

						});
					}
				} else {
					if (null != m_activity) {
						m_activity.runOnUiThread(new Runnable() {
							public void run() {

								setUpdateSign(0);

							}
						});
					}
				}
			}

		}.start();
	}

	private void disView() {
		setAdapter();
		loadingPageUtil.hideLoadPage();

		if (null == obj) {
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
			return;
		}

		if (isLoadDataFinish) {
			mListView.removeFooterView(loadMoreView);
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
		}
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		Log.i(TAG, "onWrongConnectionState");
		mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
		Log.i(TAG, "onError");
		switch (code) {
		case INotifiableController.CODE_UNKNONW_HOST:
		case INotifiableController.CODE_WRONG_DATA_FORMAT:
		case INotifiableController.CODE_REQUEST_TIME_OUT:
		case INotifiableController.CODE_CONNECT_ERROR:
		case INotifiableController.CODE_GENNERAL_IO_ERROR:
		case INotifiableController.CODE_NOT_FOUND_ERROR:
		case INotifiableController.CODE_JSON_PARSER_ERROR:
		case INotifiableController.CODE_JSON_MAPPING_ERROR:
		case INotifiableController.CODE_UNCAUGHT_ERROR:
			mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
			break;
		case INotifiableController.CODE_NOT_NETWORK:
			mHandler.sendEmptyMessage(Globals.NO_NETWORK);
			break;
		default:
			break;
		}
	}

	@Override
	public void onMessage(String message) {
		Log.i(TAG, "onMessage");
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		Log.i(TAG, "runOnUI");
		if (m_activity != null) {
			mHandler.post(response);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Globals.NETWORK_ERROR:
				if (loadingPageUtil.isShowing()) {
					loadingPageUtil.showNetworkError();
				}
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.no_connection_prompt);
				break;
			case Globals.NO_NETWORK:
				if (loadingPageUtil.isShowing()) {
					loadingPageUtil.showNoNetWork();
				}
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.no_connection_prompt);
				break;
			default:
				break;
			}
		}

	};

	public AuroraListView getmListView() {
		return mListView;
	}

}
