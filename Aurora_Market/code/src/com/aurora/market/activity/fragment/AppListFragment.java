package com.aurora.market.activity.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraListView;

import com.aurora.datauiapi.data.ManagerThread;
import com.aurora.datauiapi.data.MarketManager;
import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SpecialAllObject;
import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.AppRankingActivity;
import com.aurora.market.activity.module.MarketDetailActivity;
import com.aurora.market.adapter.AppAdapter;
import com.aurora.market.db.CacheDataAdapter;
import com.aurora.market.download.DownloadUpdateListener;
import com.aurora.market.download.FileDownloader;
import com.aurora.market.install.InstallAppManager;
import com.aurora.market.model.DownloadData;
import com.aurora.market.model.InstalledAppInfo;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.service.AppInstallService;
import com.aurora.market.util.DataFromUtils;
import com.aurora.market.util.Globals;
import com.aurora.market.util.LoadingPageUtil;
import com.aurora.market.util.LoadingPageUtil.OnHideListener;
import com.aurora.market.util.LoadingPageUtil.OnRetryListener;
import com.aurora.market.util.LoadingPageUtil.OnShowListener;
import com.aurora.market.util.Log;

public class AppListFragment extends Fragment implements INotifiableController {

	public static final String TAG = "AppListFragment";

	private LoadingPageUtil loadingPageUtil;

	private AuroraListView mListView;

	private AppAdapter adapter;

	// 加载更多面板
	private LinearLayout loadMoreView;
	// 底面板加载更多字段控件
	private TextView forum_foot_more;
	private ProgressBar foot_progress;
	// 数据是否加载完毕
	private boolean isLoadDataFinish = false;

	private int pageNum = 1;
	private int rowCount = 30;

	private MarketListObject obj;
	private SpecialAllObject speObj;

	private List<DownloadData> down_data = new ArrayList<DownloadData>();

	private boolean stopFlag = false;

	private MarketManager mmarketManager;
	private ManagerThread marketThread;
	private View animal_view;
	// 0主界面 1 新品 2排行 3分类 4.专题  5 必备  6 设计
	private int type = 1;
	private String rank_type = "APP";
	private int cat_id = -1;
	private int spe_id = -1;
	private MyBroadcastReciver broadcastReceiver;
	private SharedPreferences app_update;
	private int update_status = 0;
	private Activity m_activity;
	private boolean isCacheData = false;
	public static AppListFragment newInstance(int type, String rank_type,
			int id) {
		AppListFragment f = new AppListFragment();
		Bundle b = new Bundle();
		b.putInt("type", type);
		b.putString("rank_type", rank_type);
		b.putInt("id", id);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			type = args.getInt("type");
			rank_type = args.getString("rank_type");
			int id = args.getInt("id");
			if (type == 3) {
				cat_id = id;
			} else if (type == 4) {
				spe_id = id;
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.e(TAG, "onAttach");

		m_activity = activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		app_update = m_activity.getSharedPreferences(Globals.SHARED_APP_UPDATE,
				Context.MODE_APPEND);
		update_status = app_update.getInt(
				Globals.SHARED_DOWNORUPDATE_KEY_ISEXITS, 0);
		initViews();
		setListener();
		setAdapter();
		initLoadingPage();

		getifCache();
		
		mmarketManager = new MarketManager();
		marketThread = new ManagerThread(mmarketManager);
		marketThread.market(this);
		initData();
		initBroadCast();
	}
	
	private void getifCache()
	{
		CacheDataAdapter ldb = new CacheDataAdapter(m_activity);
		ldb.open();
		String result = ldb.queryCacheByType(type,rank_type,cat_id,spe_id);
		if(null != result)
			isCacheData = true;
		ldb.close();
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

	@Override
	public void onStart() {
		super.onStart();

		if (stopFlag) {
			updateListener.downloadProgressUpdate();
			stopFlag = false;
			mListView.postInvalidate();
		}
		AppDownloadService.registerUpdateListener(updateListener);
	}

	@Override
	public void onStop() {
		super.onStop();

		stopFlag = true;
		AppDownloadService.unRegisterUpdateListener(updateListener);
		
		if (adapter != null) {
			adapter.clearProgressBtnTag(mListView);
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
		super.onDestroy();

		marketThread.quit();
		mmarketManager.setController(null);
		m_activity.unregisterReceiver(broadcastReceiver);

		m_activity = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_applist, container,
				false);
		return view;
	}

	private void initViews() {
		View view = getView();
		mListView = (AuroraListView) view.findViewById(R.id.lv_app);
		mListView.setSelector(R.drawable.list_item_selector);
		loadMoreView = (LinearLayout) m_activity.getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		loadMoreView.setClickable(false);
		loadMoreView.setLongClickable(false);
		forum_foot_more = (TextView) loadMoreView
				.findViewById(R.id.listview_foot_more);
		foot_progress = (ProgressBar) loadMoreView
				.findViewById(R.id.listview_foot_progress);

		mListView.addFooterView(loadMoreView);
		if (update_status == 0) {
			setUpdateSign(0);
		} else {
			setUpdateSign(1);
		}
		setAnimal();
	}

	public void setAnimal() {
		animal_view = new View(m_activity);
		animal_view.layout(0, 0, 200, 200);
		LayoutParams params = new LayoutParams(200, 200);
		ViewGroup tt = (ViewGroup) ((AuroraActivity) m_activity)
				.getWindowLayout();
		tt.addView(animal_view, params);
		animal_view.setVisibility(View.GONE);
		animal_view.setBackgroundColor(getResources().getColor(R.color.red));

	}

	public void setAnimal1(final ImageView view) {
		if(null == m_activity)
			return;
		if (type == 2) {
			((AppRankingActivity) m_activity).main_update.setVisibility(View.VISIBLE);
		} else {
			((AppListActivity) m_activity).main_update.setVisibility(View.VISIBLE);
		}
		int[] loc = new int[2];
		// mListView.getLocationOnScreen(loc);
		DisplayMetrics dm = new DisplayMetrics();
		m_activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;// 宽度height = dm.heightPixels ;//高度
		view.getLocationInWindow(loc);
		ViewGroup flayout = (ViewGroup) ((AuroraActivity) m_activity)
				.getWindowLayout();
		Rect rect = new Rect();
		view.getHitRect(rect);

		flayout.offsetDescendantRectToMyCoords(view, rect);
		TranslateAnimation animation = new TranslateAnimation(rect.left, width,
				rect.top, 1);
		AnimationSet set = new AnimationSet(true);

		// animation.setInterpolator(new DecelerateInterpolator());

		ScaleAnimation animation1 = new ScaleAnimation(0.9f, 0.2f, 0.9f, 0.2f,
				Animation.RELATIVE_TO_PARENT, 0.9f,
				Animation.RELATIVE_TO_PARENT, 0.02f);

		AlphaAnimation animation2 = new AlphaAnimation(1, 0.5f);

		set.addAnimation(animation);
		set.addAnimation(animation1);
		set.addAnimation(animation2);
		set.setDuration(800);
		set.setInterpolator(new AccelerateDecelerateInterpolator());
		set.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				animal_view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				animal_view.setVisibility(view.GONE);
			}
		});
		if (null == view.getDrawingCache())
			return;
		Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		/*
		 * View view3 = new View(inflater.getContext()); view3.layout(0, 1000,
		 * 500, 200);
		 */
		Drawable drawable = new BitmapDrawable(bmp);

		animal_view.setBackground(drawable);
		animal_view.setVisibility(view.VISIBLE);
		animal_view.startAnimation(set);

	}

	private void setListener() {
		loadMoreView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isLoadDataFinish)
					return;
				if (foot_progress.getVisibility() == View.VISIBLE)
					return;
				forum_foot_more.setText(R.string.loading);
				foot_progress.setVisibility(View.VISIBLE);
				getNetData();
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					adapter.setLoadImage(false);
				} else if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					adapter.setLoadImage(true);
					adapter.notifyDataSetChanged();

				} else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					adapter.setLoadImage(false);
				}
				
				if (isLoadDataFinish)
					return;
				
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(loadMoreView) == view
							.getLastVisiblePosition()) {
						scrollEnd = true;
					}
				} catch (Exception e) {
					scrollEnd = false;
				}

				if (type == 4) {
					if (speObj.getApps().size() < pageNum * rowCount)
						return;
				} else {
					if (obj.getApps().size() < pageNum * rowCount)
						return;
				}

				if (scrollEnd) {
					forum_foot_more.setText(R.string.loading);
					foot_progress.setVisibility(View.VISIBLE);
					pageNum++;
					getNetData();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (mListView.getAdapter().getItem(position) != null) {

					Intent intent = new Intent(m_activity,
							MarketDetailActivity.class);
					intent.putExtra("downloaddata", ((DownloadData) mListView
							.getAdapter().getItem(position)));

					startActivity(intent);
				}
			}

		});
	}

	private void initData() {
		pageNum = 1;
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

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (adapter != null) {
				// adapter.notifyDataSetChanged();
				adapter.updateView(mListView);
			}
		}
	};

	private void upDownLoadData(MarketListObject m_obj) {
		for (int i = 0; i < m_obj.getApps().size(); i++) {
			DownloadData tmp_data = new DownloadData();
			appListtem list = m_obj.getApps().get(i);
			tmp_data.setApkId(list.getId());
			tmp_data.setApkDownloadPath(list.getDownloadURL());
			tmp_data.setApkLogoPath(list.getIcons().getPx256());
			tmp_data.setApkName(list.getTitle());
			tmp_data.setPackageName(list.getPackageName());
			tmp_data.setVersionCode(list.getVersionCode());
			tmp_data.setVersionName(list.getVersionName());
			down_data.add(tmp_data);
		}
	}
	
	private void upDownLoadData(SpecialAllObject s_obj) {
		for (int i = 0; i < s_obj.getApps().size(); i++) {
			DownloadData tmp_data = new DownloadData();
			appListtem list = s_obj.getApps().get(i);
			tmp_data.setApkId(list.getId());
			tmp_data.setApkDownloadPath(list.getDownloadURL());
			tmp_data.setApkLogoPath(list.getIcons().getPx256());
			tmp_data.setApkName(list.getTitle());
			tmp_data.setPackageName(list.getPackageName());
			tmp_data.setVersionCode(list.getVersionCode());
			tmp_data.setVersionName(list.getVersionName());
			down_data.add(tmp_data);
		}
	}

	private void getNetData() {
		if (type == 4) {
			mmarketManager.getSpecialAllItems(new DataResponse<SpecialAllObject>() {
				public void run() {
					if (value != null) {
						isCacheData = false;
						if (pageNum == 1) {
							speObj = value;
							
							int size = value.getApps().size();
							if (size < rowCount)
								isLoadDataFinish = true;
							
							upDownLoadData(value);
						} else {
							int size = value.getApps().size();
							if (size < rowCount)
								isLoadDataFinish = true;
							
							for (int i = 0; i < size; i++) {
								speObj.getApps().add(value.getApps().get(i));
							}
							upDownLoadData(value);
						}
						disView();
					}
				}
			}, m_activity, spe_id, pageNum, rowCount,isCacheData);
		} else {
			mmarketManager.getMarketItems(new DataResponse<MarketListObject>() {
				public void run() {
					if (value != null) {
						
						isCacheData = false;
						if (pageNum == 1) {
							obj = value;
							
							int size = value.getApps().size();
							if (size < rowCount)
								isLoadDataFinish = true;
							
							upDownLoadData(value);
						} else {
							int size = value.getApps().size();
							if (size < rowCount)
								isLoadDataFinish = true;
							
							for (int i = 0; i < size; i++) {
								obj.getApps().add(value.getApps().get(i));
							}
							upDownLoadData(value);
						}
						disView();
					}
				}
			}, m_activity, type, rank_type, cat_id, pageNum, rowCount,isCacheData);
		}
	}

	private void setUpdateSign(int sign_type) {
		if (m_activity == null) {
			return;
		}

		if (sign_type == 0) {
			if (type == 2) {
				((AppRankingActivity) m_activity).main_update.setVisibility(View.GONE);
			} else {

				((AppListActivity) m_activity).main_update.setVisibility(View.GONE);
			}
		} else {
			if (type == 2) {
				((AppRankingActivity) m_activity).main_update.setVisibility(View.VISIBLE);
			} else {
				((AppListActivity) m_activity).main_update.setVisibility(View.VISIBLE);
			}
		}

	}

	private void getUpAppSign() {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (null == m_activity) {
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
		if (pageNum == 1) {
			setAdapter();
			loadingPageUtil.hideLoadPage();
		} else {
			adapter.notifyDataSetChanged();
		}
		
		if (type == 4) {
			if (null == speObj) {
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
			}

			if (speObj.getApps().size() < pageNum * rowCount) {
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
			}
		} else {
			if (null == obj) {
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
			}

			if (obj.getApps().size() < pageNum * rowCount) {
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
			}
		}

		if (isLoadDataFinish) {
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
		}
	}

	/**
	 * @Title: setAdapter
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param
	 * @return void
	 * @throws
	 */
	private void setAdapter() {
		if (null != m_activity) {
			if (type == 4) {
				if (speObj == null) {
					speObj = new SpecialAllObject();
				}
				adapter = new AppAdapter(m_activity, speObj.getApps(), down_data);
			} else {
				if (obj == null) {
					obj = new MarketListObject();
				}
				adapter = new AppAdapter(m_activity, obj.getApps(), down_data);
			}
			mListView.setAdapter(adapter);
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

	public void installAll() {

		Log.e(TAG, "installAll::down_data length === " + down_data.size());
		Map<Integer, FileDownloader> lDownloaders = AppDownloadService
				.getDownloaders();

		if (lDownloaders != null && down_data != null
				&& AppInstallService.getInstalls() != null) {
			
			for (DownloadData data : down_data) {
				Log.e(TAG, "installAll::for"
						+ AppInstallService.getInstalls().size());
				if (lDownloaders.get(data.getApkId()) == null) { // 是否在下载列表中
					Log.e(TAG, "installAll::不在下载列表中");
					if (AppInstallService.getInstalls().get(data.getApkId()) == null) { // 是否在安装列表中
						Log.e(TAG, "installAll::// " + data.getApkName()
								+ " 不在安装列表中");
						InstalledAppInfo info = InstallAppManager
								.getInstalledAppInfo(m_activity,
										data.getPackageName());
						if (info != null
								&& info.getVersionCode() < data
										.getVersionCode()) { // 由于安装完成到更新之前有个时间差，这里判断是否已经是最新版本了
							AppDownloadService.startDownload(m_activity, data);
							continue;
						}
						if (info == null) { // 不在已安装列表中
							AppDownloadService.startDownload(m_activity, data);
						}
					}
				}
			}

		}

	}
	
}
