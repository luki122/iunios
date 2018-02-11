package com.aurora.market.activity.module;

import android.app.ActionBar.LayoutParams;
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
import android.view.KeyEvent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCustomActionBar;
import aurora.widget.AuroraCustomActionBar.onOptionItemClickListener;
import aurora.widget.AuroraListView;

import com.aurora.datauiapi.data.ManagerThread;
import com.aurora.datauiapi.data.MarketManager;
import com.aurora.datauiapi.data.bean.SpecialListObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.market.R;
import com.aurora.market.activity.BaseActivity;
import com.aurora.market.activity.setting.MarketManagerPreferenceActivity;
import com.aurora.market.adapter.SpecialAdapter;
import com.aurora.market.db.CacheDataAdapter;
import com.aurora.market.download.DownloadUpdateListener;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.util.DataFromUtils;
import com.aurora.market.util.Globals;
import com.aurora.market.util.LoadingPageUtil;
import com.aurora.market.util.LoadingPageUtil.OnHideListener;
import com.aurora.market.util.LoadingPageUtil.OnRetryListener;
import com.aurora.market.util.LoadingPageUtil.OnShowListener;
import com.aurora.market.util.SearchUtils;

public class SpecialActivity extends BaseActivity implements OnClickListener,
		INotifiableController {

	private AuroraListView mListView;
	public AuroraCustomActionBar mActionBar;
	public ImageView main_update;
	private View animal_view;

	private SpecialAdapter adapter;

	private int pageNum = 1;
	private int rowCount = 5;
	// 加载更多面板
	private LinearLayout loadMoreView;
	// 底面板加载更多字段控件
	private TextView forum_foot_more;
	private ProgressBar foot_progress;
	// 数据是否加载完毕
	private boolean isLoadDataFinish = false;

	private SpecialListObject obj = new SpecialListObject();
	// private List<DownloadData> down_data = new ArrayList<DownloadData>();

	private MarketManager mmarketManager;
	private ManagerThread marketThread;

	private LoadingPageUtil loadingPageUtil;

	private boolean stopFlag = false;

	private MyBroadcastReciver broadcastReceiver;

	private SearchUtils mSearchUtils;
	private ImageView mSearchImgV;
	private boolean isCacheData = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_special,
				AuroraActionBar.Type.NEW_COSTOM, true);
		getifCache();
		mmarketManager = new MarketManager();
		marketThread = new ManagerThread(mmarketManager);
		marketThread.market(this);

		initActionBar();
		initViews();
		initLoadingPage();
		setAdapter();
		setListener();
		initBroadCast();
		initdata();

		mSearchUtils = new SearchUtils();
		mSearchUtils.initSearchMode(this);
	}
	private void getifCache()
	{
		CacheDataAdapter ldb = new CacheDataAdapter(this);
		ldb.open();
		String result = ldb.queryCacheByType(AppListActivity.TYPE_SPECIAL_MAIN,Globals.TYPE_APP,0,0);
		if(null != result)
			isCacheData = true;
		ldb.close();
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
	}

	@Override
	protected void onPause() {
		mListView.auroraOnPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mListView.auroraOnResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mSearchUtils.removeSearchMode();

		marketThread.quit();
		mmarketManager.setController(null);

		unregisterReceiver(broadcastReceiver);
	}

	private void initViews() {
		mListView = (AuroraListView) findViewById(R.id.lv_special);

		loadMoreView = (LinearLayout) getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		// loadMoreView.setClickable(false);
		loadMoreView.setLongClickable(false);
		forum_foot_more = (TextView) loadMoreView
				.findViewById(R.id.listview_foot_more);
		foot_progress = (ProgressBar) loadMoreView
				.findViewById(R.id.listview_foot_progress);

		mListView.addFooterView(loadMoreView);

		SharedPreferences app_update = getSharedPreferences(
				Globals.SHARED_APP_UPDATE, Context.MODE_APPEND);
		int update_status = app_update.getInt(
				Globals.SHARED_DOWNORUPDATE_KEY_ISEXITS, 0);
		if (update_status == 0) {
			setUpdateSign(0);
		} else {
			setUpdateSign(1);
		}

		setAnimal();
	}

	private void initActionBar() {
		mActionBar = getCustomActionBar();
		mActionBar.setTitle(getString(R.string.app_special));
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));
		mActionBar.setDefaultOptionItemDrawable(getResources().getDrawable(
				R.drawable.btn_main_right_selector));

		
		
		mActionBar.showDefualtItem(false);
		mActionBar.addItemView(R.layout.actionbar_main_right);
		mActionBar.addItemView(R.layout.actionbar_search_item);
		mSearchImgV = (ImageView) mActionBar.findViewById(R.id.bar_search_item);
		main_update = (ImageView) mActionBar.findViewById(R.id.actionbar_main_update);
		View view = mActionBar.findViewById(R.id.download_layout);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SpecialActivity.this,
						MarketManagerPreferenceActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(this, getContentView());
		loadingPageUtil.setOnRetryListener(new OnRetryListener() {
			@Override
			public void retry() {
				initdata();
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

	private void initdata() {
		pageNum = 1;
		isLoadDataFinish = false;
		getNetData();
	}

	private void setListener() {

		mActionBar
				.setOnOptionItemClickListener(new onOptionItemClickListener() {
					@Override
					public void click(View view) {
						Intent intent = new Intent(SpecialActivity.this,
								MarketManagerPreferenceActivity.class);
						startActivity(intent);
					}
				});

		mSearchImgV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mSearchUtils.startSearchMode();
			}
		});

		loadMoreView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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

				if (obj.getSpecials().size() < pageNum * rowCount)
					return;

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
	}

	private void setAdapter() {
		adapter = new SpecialAdapter(this, obj.getSpecials(), mListView);
		mListView.setAdapter(adapter);
	}

	private void getNetData() {
		mmarketManager.getSpecialListItems(
				new DataResponse<SpecialListObject>() {
					public void run() {
						
						if (value != null) {
							// Log.i(TAG, "the value=" + value.getCode());
							isCacheData = false;
							if (pageNum == 1) {
								obj = value;

								int size = value.getSpecials().size();
								if (size < rowCount)
									isLoadDataFinish = true;
							} else {
								int size = value.getSpecials().size();
								if (size < rowCount)
									isLoadDataFinish = true;

								for (int i = 0; i < size; i++) {
									obj.getSpecials().add(
											value.getSpecials().get(i));
								}
								// upDownLoadData(value);
							}
							disView();
						}
					}
				}, SpecialActivity.this, "APP", pageNum, rowCount,isCacheData);

	}

	private void disView() {
		if (pageNum == 1) {
			setAdapter();
			loadingPageUtil.hideLoadPage();
		} else {
			adapter.notifyDataSetChanged();
		}

		if (null == obj) {
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
			return;

		}
		if (obj.getSpecials().size() < pageNum * rowCount) {
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
			return;

		}
		if (isLoadDataFinish) {
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
		}
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
		registerReceiver(broadcastReceiver, intentFilter);
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

	private void getUpAppSign() {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (SpecialActivity.this.isFinishing()
						|| SpecialActivity.this.isDestroyed()) {
					return;
				}

				int count = AppDownloadService.getDownloaders().size();
				int sum = 0;
				if (count == 0) {
					DataFromUtils up_data = new DataFromUtils();
					sum = up_data.getUpdateSum(SpecialActivity.this);
				}
				if ((sum > 0) || (count > 0)) {
					if (!SpecialActivity.this.isFinishing()
							&& !SpecialActivity.this.isDestroyed()) {
						SpecialActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								setUpdateSign(1);
							}

						});
					}
				} else {
					if (!SpecialActivity.this.isFinishing()
							&& !SpecialActivity.this.isDestroyed()) {
						SpecialActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								setUpdateSign(0);
							}

						});
					}
				}
			}

		}.start();
	}

	private void setUpdateSign(int sign_type) {
		if (isFinishing() || isDestroyed()) {
			return;
		}

		if (sign_type == 0) {
			main_update.setVisibility(View.GONE);
		} else {
			main_update.setVisibility(View.VISIBLE);
		}
	}

	public void setAnimal() {
		animal_view = new View(this);
		animal_view.layout(0, 0, 200, 200);
		LayoutParams params = new LayoutParams(200, 200);
		ViewGroup tt = (ViewGroup) getWindowLayout();
		tt.addView(animal_view, params);
		animal_view.setVisibility(View.GONE);
		animal_view.setBackgroundColor(getResources().getColor(R.color.red));
	}

	public void setAnimal1(final ImageView view) {
		main_update.setVisibility(View.VISIBLE);
		int[] loc = new int[2];
		// mListView.getLocationOnScreen(loc);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;// 宽度height = dm.heightPixels ;//高度
		view.getLocationInWindow(loc);
		ViewGroup flayout = (ViewGroup) getWindowLayout();
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (mSearchUtils.isInSearchMode()) {
				SpecialActivity.this.hideSearchViewLayoutWithOnlyAlphaAnim();
				mSearchUtils.setSearchMode(false);
			}

		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {

	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
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

	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		mHandler.post(response);
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

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (adapter != null) {
				adapter.updateView(mListView);
			}
		}
	};

	@Override
	public void onClick(View v) {

	}

}
