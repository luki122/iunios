package com.aurora.market.activity.module;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraAutoCompleteTextView.OnDismissListener;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnQueryTextListener;

import com.aurora.datauiapi.data.ManagerThread;
import com.aurora.datauiapi.data.SearchManager;
import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SearchTimelyObject;
import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.market.R;
import com.aurora.market.activity.BaseActivity;
import com.aurora.market.adapter.AppAdapter;
import com.aurora.market.adapter.PopQueryResultAdapter;
import com.aurora.market.download.DownloadUpdateListener;
import com.aurora.market.model.DownloadData;
import com.aurora.market.service.AppDownloadService;
import com.aurora.market.util.Globals;
import com.aurora.market.util.LoadingPageUtil;
import com.aurora.market.util.Log;
import com.aurora.market.util.LoadingPageUtil.OnHideListener;
import com.aurora.market.util.LoadingPageUtil.OnRetryListener;
import com.aurora.market.util.LoadingPageUtil.OnShowListener;
import com.aurora.utils.DensityUtil;

public class MarketSearchActivity extends BaseActivity implements
		OnQueryTextListener, OnClickListener, INotifiableController {

	private final static String TAG = "MarketSearchActivity";
	private SearchTimelyObject m_searchobj = new SearchTimelyObject();
	private LayoutInflater inflater;
	private AuroraActionBar mActionBar;
	private static final int AURORA_SERACH_ITEM = 0;

	private View mActionBarView;
	private AuroraSearchView mSearchView;
	private TextView mSearchBtn;

	private SearchManager mSearchManager;
	private boolean isLoadDataFinish = false;
	private boolean ifScroll = true;
	private int pageNum = 1;
	private int rowCount = 15;
	private String query;
	private ManagerThread thread;
	private MarketListObject obj = new MarketListObject();

	private final static int SHOW_SIZE_LIMIT = 6;

	private ArrayAdapter<String> mArrAdapter;
	private int mPopResultSize;
	private PopupWindow mPopWin;
	private AuroraListView mListView;
	private LinearLayout mEmptyView;
	private AppAdapter adapter;
	private boolean stopFlag = false;
	// private boolean mIsShowPopWin = false;
	private int isNetRequest = 0;
	private List<DownloadData> down_data = new ArrayList<DownloadData>();

	// 加载更多面板
	private LinearLayout loadMoreView;
	// 底面板加载更多字段控件
	private TextView forum_foot_more;
	private ProgressBar foot_progress;
	private SharedPreferences mSharedPreference;
	private View lContentView;
	private PopQueryResultAdapter mPopResultAdatper;
	
	private LoadingPageUtil loadingPageUtil;

	private View mask_layer;
	private boolean isCantextSearch = true;
	private boolean isTextEmpty = false;
	//点击了搜索后 不弹出搜索提式候选框
	private boolean isClickSearch = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.search_result_applist,
				AuroraActionBar.Type.Custom);
		
		query = getIntent().getStringExtra("query");

		initActionBar();
		initViews();

		setListener();

		initLoadingPage();
		
		mSearchManager = new SearchManager();
		thread = new ManagerThread(mSearchManager);

		thread.market(this);

		initdata();

		if (stopFlag) {
			updateListener.downloadProgressUpdate();
			stopFlag = false;
		}
		AppDownloadService.registerUpdateListener(updateListener);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		if (adapter != null) {
			adapter.clearProgressBtnTag(mListView);
		}
	}

	private void showSoftInput() {
		if(null != mSearchView)
		{
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mSearchView.getQueryTextView(), 0);
		}
	}

	private void hideSoftInput() {
		if(null != mSearchView)
		{
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchView.getQueryTextView().getWindowToken(), 0);
		}
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.app_name);
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));
		// mActionBar.getHomeTextView().setVisibility(View.GONE);

		mActionBar.getHomeButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		mActionBar.addItem(R.layout.search_actionbar_item_layout,
				AURORA_SERACH_ITEM);

		mActionBar.setCustomView(R.layout.search_home_page_actionbar);
	}

	 private void initLoadingPage() {
	        loadingPageUtil = new LoadingPageUtil();
	        loadingPageUtil.init(this, this.getContentView());
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

	private void initViews() {
		mask_layer = (View)findViewById(R.id.mark_layer);
		mSearchView = (AuroraSearchView) mActionBar.findViewById(R.id.search);
		mSearchView.setSearchViewBorder(R.drawable.header_search_bg_black);
		mSearchView.setPaddingLeft(0);
		mSearchView.setTextColor(Color.WHITE);
		mSearchView
				.setSearchIconDrawable(R.drawable.aurora_seachview_hint_left_icon);
		mSearchView.setDeleteButtonDrawable(R.drawable.search_cancel);
		mSearchView.setFocusable(false);

		if (!query.trim().equals(""))
			mSearchView.setQuery(query, false);

		mSearchBtn = (TextView) mActionBar.findViewById(R.id.actionBarItem);

		mListView = (AuroraListView) findViewById(R.id.search_result_lv_app);
		mEmptyView = (LinearLayout) findViewById(R.id.empty_layout);

		mListView.setSelector(R.drawable.list_item_selector);
		loadMoreView = (LinearLayout) this.getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		//loadMoreView.setClickable(false);
		loadMoreView.setLongClickable(false);
		
		forum_foot_more = (TextView) loadMoreView
				.findViewById(R.id.listview_foot_more);
		foot_progress = (ProgressBar) loadMoreView
				.findViewById(R.id.listview_foot_progress);

		mListView.addFooterView(loadMoreView);
		initPopWin();
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
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View pView, boolean arg1) {
				// TODO Auto-generated method stub
				if (mSearchView.isFocusable()) {
					// mSearchView.;
				}
			}
		});

		mSearchView.getQueryTextView().setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                
                if (KeyEvent.KEYCODE_ENTER == keyCode
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                	mSearchView.getQueryTextView().dismissDropDown();
                    query = mSearchView.getQuery()
                            .toString();
                    saveSearchRecords();
                    
                    down_data.clear();
                    obj.getApps().clear();
                    
                    initdata();
                    return true;
                }
                return false;
            }
        });
		
		mSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// setAdapter();
			    
			    if(query == null || query.trim().length() == 0 ){
			        
			        Toast.makeText(MarketSearchActivity.this, R.string.search_content_empty_tip,
			                Toast.LENGTH_SHORT).show();
			        return;
			    }
				hideSoftInput();
				mEmptyView.setVisibility(View.GONE);
				query = mSearchView.getQuery().toString();
				saveSearchRecords();
				down_data.clear();
				obj.getApps().clear();
				if (null != adapter)
					adapter.notifyDataSetChanged();
				forum_foot_more.setText(R.string.loading);
				forum_foot_more.setVisibility(View.VISIBLE);
				foot_progress.setVisibility(View.VISIBLE);
				isClickSearch = true;
				initdata();

			}
		});

		mSearchBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View pView, MotionEvent pEvent) {
				// TODO Auto-generated method stub

				switch (pEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mSearchBtn.getBackground().setAlpha(100);
					break;
				case MotionEvent.ACTION_UP:
					mSearchBtn.getBackground().setAlpha(255);
					break;
				}

				return false;
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

				if (obj.getApps().size() < pageNum * rowCount)
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

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if(mListView.getAdapter().getCount() <= position)
				{
					return;
				}
				
				if (mListView.getAdapter().getItem(position) != null) {
					Intent intent = new Intent(MarketSearchActivity.this,
							MarketDetailActivity.class);
					intent.putExtra("downloaddata", ((DownloadData) mListView
							.getAdapter().getItem(position)));
					startActivity(intent);
				}

			}

		});
		mSearchView.getQueryTextView().setOnItemClickListener(
				new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						
						//
						mEmptyView.setVisibility(View.GONE);
						
						isCantextSearch = false;
						mask_layer.setVisibility(View.GONE);
						hideSoftInput();
						TextView lPopQuery = (TextView) arg1
								.findViewById(R.id.pop_text);
						query = lPopQuery.getText().toString();
						saveSearchRecords();
						mSearchView.getQueryTextView().setText(query);
						mSearchView.getQueryTextView().setSelection(
								query.length());
						down_data.clear();
						obj.getApps().clear();
						if (null != adapter)
							adapter.notifyDataSetChanged();
				
						forum_foot_more.setText(R.string.loading);
						forum_foot_more.setVisibility(View.VISIBLE);
						foot_progress.setVisibility(View.VISIBLE);
						
						initdata();
						//hideSoftInput();

					}
				});
	}

	private void initdata() {
		pageNum = 1;
		isLoadDataFinish = false;

		getNetData();
	}

	private void getTimeLyNetData() {
		mSearchManager.getSearchTimelyItems(
				new DataResponse<SearchTimelyObject>() {
					public void run() {
						if (value != null) {
							Log.i(TAG, "the value=" + value.getCode());
							m_searchobj.getSuggestions().clear();
							m_searchobj.getSuggestions().addAll(
									value.getSuggestions());
							
							mPopResultAdatper.notifyDataSetChanged();

							buildPopWin();
						}
					}

				}, MarketSearchActivity.this, query);
	}

	private void getNetData() {
		isNetRequest = 1;
		mSearchManager.getSearchListItems(new DataResponse<MarketListObject>() {
			public void run() {
				if (value != null) {
					isNetRequest = 0;
					Log.i(TAG, "the value=" + value.getCode());
					isClickSearch = false;
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

		}, MarketSearchActivity.this, query, pageNum, rowCount);
	}

	private void setAdapter() {
		adapter = new AppAdapter(this, obj.getApps(), down_data);
		mListView.setAdapter(adapter);
	}
	
	
	
	private void disView() {
		mask_layer.setVisibility(View.GONE);
		isCantextSearch = true;
		hideSoftInput();
		if (pageNum == 1) {
			setAdapter();
			loadingPageUtil.hideLoadPage();
		} else {
			adapter.notifyDataSetChanged();
		}

		if (null == obj) {
			foot_progress.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			forum_foot_more.setText(R.string.all_loaded);
			return;
		}

		if (obj.getApps().size() == 0) {
			mEmptyView.setVisibility(View.VISIBLE);
			forum_foot_more.setVisibility(View.GONE);
		} else {
			mEmptyView.setVisibility(View.GONE);
			forum_foot_more.setVisibility(View.VISIBLE);
		}

		if (obj.getApps().size() < pageNum * rowCount) {
			foot_progress.setVisibility(View.GONE);
			forum_foot_more.setText(R.string.all_loaded);
			return;
		}

		if (isLoadDataFinish) {
			foot_progress.setVisibility(View.GONE);
			//forum_foot_more.setText(R.string.all_loaded);
		}
	}

	private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	};

	public void getTotalHeightofListView(ListView listView) {
		ListAdapter mAdapter = listView.getAdapter();
		if (mAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View mView = mAdapter.getView(i, null, listView);
			mView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			// mView.measure(0, 0);
			totalHeight += mView.getMeasuredHeight();
			Log.w("HEIGHT" + i, String.valueOf(totalHeight));
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (mAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		hideSoftInput();
		mSearchView.clearFocus();
		mActionBar.setBackground(getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green));
	}

	private void initPopWin() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		mSearchView.getQueryTextView().setThreshold(1);
		//mSearchView.getQueryTextView().setDropDownHorizontalOffset(0);
		mSearchView.getQueryTextView().setDropDownVerticalOffset(
				DensityUtil.dip2px(MarketSearchActivity.this, 6));
		mSearchView.getQueryTextView().setDropDownWidth(width);
		
		//mSearchView.getQueryTextView().setOverScrollMode(View.OVER_SCROLL_NEVER);
		
		mSearchView.getQueryTextView().setDropDownBackgroundDrawable(
				getResources().getDrawable(R.drawable.search_autotext_bg));
		mPopResultAdatper = new PopQueryResultAdapter(
				MarketSearchActivity.this, R.layout.search_pop_list_item,
				R.id.pop_text, m_searchobj.getSuggestions(), mSearchView);
		mSearchView.getQueryTextView().setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				if(isNetRequest ==0 )
					mask_layer.setVisibility(View.GONE);
			}
			
		});
		
		mSearchView.getQueryTextView().setAdapter(mPopResultAdatper);
		
		mPopResultAdatper.notifyDataSetChanged();
	}

	private void buildPopWin() {
		if(isClickSearch)
			return;
		
		if(isTextEmpty)
			return;
		if(m_searchobj.getSuggestions().size() >0)
		{
			mask_layer.setVisibility(View.VISIBLE);
		}
		mPopResultAdatper.notifyDataSetChanged();

	}

	
	private void saveSearchRecords() {

		if (!query.trim().equals("") && query != null) {
			if (mSharedPreference == null) {
				mSharedPreference = this
						.getSharedPreferences(Globals.HISTORY_RECORDS_FILENAME,
								Activity.MODE_PRIVATE);
			}

			for (int i = 0; i < Globals.HISTORY_MAX_LIMIT; i++) {

				String lHistory = mSharedPreference.getString(
						Globals.HISTORY_RECORDS + i, null);

				if (lHistory != null) {
					if(lHistory.equals(query))
						return;
				}
			}
			int lInsertPos = mSharedPreference.getInt(
					Globals.HISTORY_NEXT_INSERT_POSITION, 0);

			if (lInsertPos == Globals.HISTORY_MAX_LIMIT) {
				for (int i = 0; i < Globals.HISTORY_MAX_LIMIT - 1; i++) {
					mSharedPreference
							.edit()
							.putString(
									Globals.HISTORY_RECORDS + i,
									mSharedPreference.getString(
											Globals.HISTORY_RECORDS + (i + 1),
											null)).commit();
				}
				mSharedPreference
						.edit()
						.putString(Globals.HISTORY_RECORDS + (lInsertPos - 1),
								query).commit();
			} else {
				mSharedPreference.edit()
						.putString(Globals.HISTORY_RECORDS + lInsertPos, query)
						.commit();
			}

			if (lInsertPos < Globals.HISTORY_MAX_LIMIT)
				lInsertPos++;

			mSharedPreference.edit()
					.putInt(Globals.HISTORY_NEXT_INSERT_POSITION, lInsertPos)
					.commit();
		}
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		isNetRequest = 0;
	    mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
		// TODO Auto-generated method stub
		isNetRequest = 0;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
            switch (msg.what) {
            case Globals.NETWORK_ERROR:
                if (loadingPageUtil.isShowing()) {
                    loadingPageUtil.showNetworkError();
                }
                break;
            case Globals.NO_NETWORK:
                if (loadingPageUtil.isShowing()) {
                    loadingPageUtil.showNoNetWork();
                }
                break;
            default:
                break;
            }
      

		}

	};

	@Override
	public boolean onQueryTextChange(String pQuery) {
		// TODO Auto-generated method stub
		query = pQuery;
		
		if(TextUtils.isEmpty(query))
		{
			//mEmptyView.setVisibility(View.GONE);
			isTextEmpty = true;
			mask_layer.setVisibility(View.GONE);
			return false;
		}
		//addMaskLayer();
		//mask_layer.setVisibility(View.VISIBLE);
		isTextEmpty = false;
		if(isCantextSearch)
			getTimeLyNetData();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String pQuery) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		stopFlag = true;
		// ((MarketManager) mmarketManager).setController(null);
		thread.quit();
		AppDownloadService.unRegisterUpdateListener(updateListener);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			finish();
		}

		Log.v(TAG, "aurora.jiangmx onKeyDown()");
		return false;
	}

	/**
	 * (非 Javadoc) Title: onClick Description:
	 * 
	 * @param v
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		// lContentView.setVisibility(View.GONE);

	}

}
