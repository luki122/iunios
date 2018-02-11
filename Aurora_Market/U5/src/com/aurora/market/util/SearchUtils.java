package com.aurora.market.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraAutoCompleteTextView.OnDismissListener;
import aurora.widget.AuroraCustomActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraCustomActionBar.onSearchViewClickedListener;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import aurora.widget.AuroraSearchView;

import com.aurora.datauiapi.data.ManagerThread;
import com.aurora.datauiapi.data.SearchManager;
import com.aurora.datauiapi.data.bean.MarketListObject;
import com.aurora.datauiapi.data.bean.SearchRecListObject;
import com.aurora.datauiapi.data.bean.SearchTimelyObject;
import com.aurora.datauiapi.data.bean.TimelyInfo;
import com.aurora.datauiapi.data.bean.appListtem;
import com.aurora.datauiapi.data.bean.recommendInfo;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.MarketDetailActivity;
import com.aurora.market.activity.module.MarketSearchActivity;
import com.aurora.market.adapter.PopQueryResultAdapter;
import com.aurora.market.model.DownloadData;
import com.aurora.market.MarketMainActivity;
import com.aurora.utils.DensityUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class SearchUtils {
	private final static String TAG = "SearchUtils";
	private SearchManager mSearchManager;
	private ManagerThread mSearchThread;

	private AuroraSearchView mSearchView;
	private Button mCancelBtn;
	private String mQuery;
	private List<Map<String, Object>> mHistoryRecords = new ArrayList<Map<String, Object>>();
	private SharedPreferences mSharedPreference;
	private SearchRecListObject recObj = new SearchRecListObject();
	private MarketListObject searchObj = new MarketListObject();
	private SearchTimelyObject m_searchobj = new SearchTimelyObject();
	public final static int SEARCH_REQUEST = 1;

	private FrameLayout mBackgroundSearchLayout;
	private FrameLayout mSearchViewBackground;
	private View mGotoSearchLayout;
	private GridView mRecGrid;
	private LinearLayout mEssentialView;
	private LinearLayout mDesignAwardView;
	private LinearLayout mLLQuitDoor;
	private AuroraListView mHistoryListView;
	private TextView mClearHistoryTxt;
	private View mLayoutDivider0;
	private View mLayoutDivider;
	private SimpleAdapter mHistoryAdapter;

	private AuroraActivity mActivity;
	private View mMaskLayer;
	private View mBottomPanel;

	private boolean isLoadDataFinish = false;

	private boolean isSearchMode = false;

	private final static int SHOW_SIZE_LIMIT = 6;
	private ListView mPopListView;
	private PopQueryResultAdapter mPopResultAdatper;
	private ArrayAdapter<String> mArrAdapter;
	private int mPopResultSize;
	private PopupWindow mPopWin;
	private boolean mIsShowPopWin = false;
	private int pageNum = 1;
	private int rowCount = 15;
	private SearchRecInfoAdapter lGvAdatper;
	private int mStatusHeight;

	public void initSearchMode(final AuroraActivity pActivity) {
		mActivity = pActivity;

		mSearchManager = new SearchManager();
		mSearchThread = new ManagerThread(mSearchManager);
		mSearchThread.market((INotifiableController) pActivity);
		mSharedPreference = mActivity.getSharedPreferences(
				Globals.HISTORY_RECORDS_FILENAME, Activity.MODE_PRIVATE);
		initViews();
	}

	public void startSearchMode(final AuroraCustomActionBar mCustomActionBar) {

		//setCustomActionBarSearchListener(mCustomActionBar);
	}

	public void startSearchMode() {

		showSearchViewLayout();
		initPopWin();
	}

	private void updateHistory()
	{
		int lInsertPos = mSharedPreference.getInt(
				Globals.HISTORY_NEXT_INSERT_POSITION, 0);
		int lIndex;
		if (lInsertPos == Globals.HISTORY_MAX_LIMIT) {
			lIndex = lInsertPos - 1;
		} else {
			lIndex = lInsertPos;
		}
		String lHistoryRecord = null;

		Map<String, Object> lMap;
		mHistoryRecords.clear();
		for (int i = lIndex; i >= 0; i--) {

			lHistoryRecord = mSharedPreference.getString(
					Globals.HISTORY_RECORDS + i, null);

			if (lHistoryRecord != null) {
				lMap = new HashMap<String, Object>();
				lMap.put("HISTORY_RECORD", lHistoryRecord);
				mHistoryRecords.add(lMap);
			}
		}
		mHistoryAdapter.notifyDataSetChanged();
		if (mHistoryRecords.size() == 0) {
			mClearHistoryTxt.setEnabled(false);
			mClearHistoryTxt.setVisibility(View.GONE);
			mLayoutDivider.setVisibility(View.GONE);
			mLayoutDivider0.setVisibility(View.GONE);
		} else {
			mClearHistoryTxt.setEnabled(true);
			mClearHistoryTxt.setVisibility(View.VISIBLE);
			mLayoutDivider.setVisibility(View.VISIBLE);
			mLayoutDivider0.setVisibility(View.VISIBLE);
		}
	}
	
	private void showSearchViewLayout() {
		

		// initLabelAdapter();
		// setOnQueryTextListener(new svQueryTextListener());

	    if(mActivity instanceof MarketMainActivity)
	       mActivity.showSearchviewLayout();
	    else{
	    	mBackgroundSearchLayout.setPadding(0,mStatusHeight+(int) mActivity.getResources().getDimension(com.aurora.R.dimen.aurora_action_bar_search_view_height), 0, 0);
	    	
	       mActivity.showSearchviewLayoutWithOnlyAlphaAnim();
	       
	    }
		setSearchMode(true);
		updateHistory();
		if(recObj.getRecommendations().size() == 0)
			getSearchRecData();
	}

	private void initViews() {
		// addSearchviewInwindowLayout();
		// mGotoSearchLayout = (LinearLayout) inflater.inflate(
		// R.layout.aurora_goto_search_mode, mListView, false)
		mActivity.setSearchviewBarBackgroundResource(
				R.drawable.aurora_action_bar_search_bg_green,
				R.drawable.aurora_action_bar_header_bg);
		
		mSearchViewBackground = (FrameLayout) mActivity
				.getSearchViewGreyBackground();

		mBackgroundSearchLayout = (FrameLayout) LayoutInflater.from(mActivity)
				.inflate(R.layout.activity_search_page, null, false);
		if (mSearchViewBackground != null) {
			mSearchViewBackground.addView(mBackgroundSearchLayout);
			// mSearchViewBackground.setBackgroundColor(mActivity.getResources().getColor(
			// R.color.progressRoundGreenColor));
            
			mRecGrid = (GridView) mBackgroundSearchLayout
					.findViewById(R.id.recommend_grid);
			
			mDesignAwardView = (LinearLayout)mBackgroundSearchLayout.findViewById(R.id.design_award_view);
			
			mEssentialView = (LinearLayout)mBackgroundSearchLayout.findViewById(R.id.essential_view);
			
		//	mLLQuitDoor = (LinearLayout) mBackgroundSearchLayout.findViewById(R.id.ll_quitdoor);
			
			mHistoryListView = (AuroraListView) mBackgroundSearchLayout
					.findViewById(R.id.search_history_list);
			mHistoryListView.setSelector(R.drawable.list_item_selector);
			
			mClearHistoryTxt = (TextView) mBackgroundSearchLayout
					.findViewById(R.id.clear_history_btn);
			
			mLayoutDivider0 = mBackgroundSearchLayout.findViewById(R.id.layout_divider0);
			mLayoutDivider = mBackgroundSearchLayout.findViewById(R.id.layout_divider);
			
			mBottomPanel = (View) mBackgroundSearchLayout.findViewById(R.id.bottom_panel);
			
			mMaskLayer = (View) mBackgroundSearchLayout.findViewById(R.id.mark_layer);
			// mLabelListContainer =
			// mBackgroundSearchLayout.findViewById(R.id.label_list_container);
			// mLabelList = (LabelList) mBackgroundSearchLayout
			// .findViewById(R.id.label_list);
		}
		mSearchView = mActivity.getSearchView();
		//mActivity.setSearchViewAnimDuration(250);
		mSearchView.setTextColor(Color.WHITE);
		mSearchView.setSearchViewBorder(R.drawable.header_search_bg_black);
		mSearchView
				.setSearchIconDrawable(R.drawable.aurora_seachview_hint_left_icon);
		mSearchView.setDeleteButtonDrawable(R.drawable.search_cancel);
		
		mCancelBtn = mActivity.getSearchViewRightButton();
		mCancelBtn.setBackground(mActivity.getResources().getDrawable(
				R.drawable.button_white_normal));

		if (mSearchView.getQuery().toString().trim().equals("")) {
			mCancelBtn.setText(mActivity.getResources().getString(
					R.string.dialog_cancel));
		} else {
			mCancelBtn.setText(mActivity.getResources().getString(
					R.string.search_page_submit));
		}

		mCancelBtn.setTextColor(mActivity.getResources().getColor(
				R.color.progressRoundGreenColor));
		mCancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		mCancelBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View pView, MotionEvent pEvent) {
				// TODO Auto-generated method stub
				switch (pEvent.getAction()) {

				case MotionEvent.ACTION_DOWN:
					mCancelBtn.getBackground().setAlpha(100);
					break;
				case MotionEvent.ACTION_UP:
					mCancelBtn.getBackground().setAlpha(255);
					break;

				}
				return false;
			}
		});
		if (null == mSearchView) {
			return;
		}
		if (mCancelBtn != null) {
			mCancelBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					if (mCancelBtn.getText().equals(
							mActivity.getResources().getString(
									R.string.dialog_cancel))) {
					    
					    if(mActivity instanceof MarketMainActivity){
					       mActivity.hideSearchViewLayout(true);		
					    }
					    else{
						   mActivity.hideSearchViewLayoutWithOnlyAlphaAnim();
					    }
					    setSearchMode(false);
						return;
					}

					mQuery = mSearchView.getQuery().toString();
					saveSearchRecords();

					if (mQuery.trim().equals("") || mQuery == null) {

						Toast.makeText(mActivity, R.string.search_check_query,
								Toast.LENGTH_SHORT).show();

					} else {

						startSearchActivity(mQuery);

					}

				}
			});
		}
		setSearchListener();
		disSearchView(mActivity);
		
		mStatusHeight = SystemUtils.getStatusHeight(mActivity);
	}
 
	private void getSearchRecData() {
		mSearchManager.getSearchRecItems(
				new DataResponse<SearchRecListObject>() {
					public void run() {
						if (value != null) {
							// Log.v(TAG, "aurora.jiangmx result: " +
							// value.getRecommendations().get(0).getTitle());
							//recObj = value;
							
							
							
							//disSearchView(mActivity);
							
							
							recObj.getRecommendations().clear();
							recObj.getRecommendations().addAll(
									value.getRecommendations());
							
							
							lGvAdatper.notifyDataSetChanged();
						}
					}
				}, mActivity);
		//disSearchView(mActivity);
	}

	private void disSearchView(Context pContext) {
/*		ArrayList<recommendInfo> lRecInfoList = (ArrayList<recommendInfo>) recObj
				.getRecommendations();

		if (recObj != null)
			Log.v(TAG, "aurora.jiangmx recommendInfo: " + lRecInfoList.size());*/

		lGvAdatper = new SearchRecInfoAdapter(pContext,
				(ArrayList<recommendInfo>) recObj
				.getRecommendations());
		mRecGrid.setAdapter(lGvAdatper);

		
		
		
		mHistoryAdapter = new SimpleAdapter(mActivity, mHistoryRecords,
				R.layout.search_page_history_item,
				new String[] { "HISTORY_RECORD" },
				new int[] { R.id.history_title });

		mHistoryListView.setAdapter(mHistoryAdapter);
	

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

							buildSearchPopWin();
						}
					}

				}, mActivity, mQuery);
	}



	private void initPopWin() {
		mMaskLayer.setVisibility(View.GONE);
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		mSearchView.getQueryTextView().setThreshold(1);
		mSearchView.getQueryTextView().setDropDownHorizontalOffset(0);
		mSearchView.getQueryTextView().setDropDownVerticalOffset(
				DensityUtil.dip2px(mActivity, 6));
		mSearchView.getQueryTextView().setDropDownWidth(width);

		mSearchView.getQueryTextView().setDropDownBackgroundDrawable(
				mActivity.getResources().getDrawable(R.drawable.search_autotext_bg));
		mPopResultAdatper = new PopQueryResultAdapter(mActivity,
				R.layout.search_pop_list_item, R.id.pop_text,
				m_searchobj.getSuggestions(), mSearchView);
		mSearchView.getQueryTextView().setAdapter(mPopResultAdatper);
		
		mSearchView.getQueryTextView().setOnDismissListener( new OnDismissListener() {
            
            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                mMaskLayer.setVisibility(View.GONE);
            }
        });

	}

	private void buildSearchPopWin() {
	    if(m_searchobj.getSuggestions().size() > 0){
	        mMaskLayer.setVisibility(View.VISIBLE);
	    }
	    if(null != mPopResultAdatper)
	    	
	    	mPopResultAdatper.notifyDataSetChanged();
	}

	private void setSearchListener() {

		mRecGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> pParent, View pView,
					int pPos, long arg3) {
				// TODO Auto-generated method stub
				/*
				 * TextView lRecApp = (TextView) pView
				 * .findViewById(R.id.rec_app_name); mQuery =
				 * lRecApp.getText().toString(); saveSearchRecords();
				 * startSearchActivity(mQuery);
				 */

			   // mActivity.hideSearchviewLayout();
			    
				recommendInfo info = (recommendInfo) mRecGrid.getAdapter()
						.getItem(pPos);

				DownloadData tmp = new DownloadData();
				tmp.setPackageName(info.getPackageName());
                 
				Intent intent = new Intent(mActivity,
						MarketDetailActivity.class);
				intent.putExtra("downloaddata", tmp);
				mActivity.startActivity(intent);
			}
		});

		mDesignAwardView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	Intent newIntent = new Intent(mActivity,
    					AppListActivity.class);
    			newIntent.putExtra(AppListActivity.OPEN_TYPE,
    					AppListActivity.TYPE_DESIGN);
    			mActivity.startActivity(newIntent);
                
            }
        });
		
		mEssentialView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	Intent newIntent = new Intent(mActivity,
    					AppListActivity.class);
    			newIntent.putExtra(AppListActivity.OPEN_TYPE,
    					AppListActivity.TYPE_STARTER);
    			mActivity.startActivity(newIntent);
                
            }
        });
		
		mHistoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> pParent, View pView,
					int pPos, long arg3) {
				// TODO Auto-generated method stub
				TextView lHistorySearch = (TextView) pView
						.findViewById(R.id.history_title);
				startSearchActivity(lHistorySearch.getText().toString());
			}
		});

		mClearHistoryTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pView) {
				// TODO Auto-generated method stub
				if (mSharedPreference == null) {
					mSharedPreference = mActivity.getSharedPreferences(
							Globals.HISTORY_RECORDS_FILENAME,
							Activity.MODE_PRIVATE);
				}

				mSharedPreference.edit().clear().commit();

				mHistoryRecords.clear();
				mHistoryAdapter.notifyDataSetChanged();

				mClearHistoryTxt.setEnabled(false);
				mClearHistoryTxt.setVisibility(View.GONE);
				mLayoutDivider0.setVisibility(View.GONE);
				mLayoutDivider.setVisibility(View.GONE);
				 
			}
		});
		mSearchView.getQueryTextView().setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (KeyEvent.KEYCODE_ENTER == keyCode
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					mQuery = mSearchView.getQueryTextView().getText()
							.toString();
					saveSearchRecords();

					startSearchActivity(mQuery);
					return true;
				}
				return false;
			}
		});
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String pQuery) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onQueryTextChange(String pQuery) {
				// TODO Auto-generated method stub
				mQuery = pQuery;
				if (mQuery.trim().equals("")) {
					mCancelBtn.setText(mActivity.getResources().getString(
							R.string.dialog_cancel));
				} else {
					mCancelBtn.setText(mActivity.getResources().getString(
							R.string.search_page_submit));

					getTimeLyNetData();
				}

				return false;
			}
		});
		mSearchView.getQueryTextView().setOnItemClickListener(
				new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						TextView lPopQuery = (TextView) arg1
								.findViewById(R.id.pop_text);
						mQuery = lPopQuery.getText().toString();
						saveSearchRecords();

						mSearchView.getQueryTextView().setText(mQuery);
						mSearchView.getQueryTextView().setSelection(
								mQuery.length());
						startSearchActivity(mQuery);

					}
				});
		
		mBottomPanel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mActivity instanceof MarketMainActivity){
				       mActivity.hideSearchViewLayout(true);					      
				    }
				    else{
					   mActivity.hideSearchViewLayoutWithOnlyAlphaAnim();
				 }
				setSearchMode(false);
			}
		});

	}

	public class SearchRecInfoAdapter extends BaseAdapter {

		private final static String TAG = "SearchRecInfoAdapter";
		private LayoutInflater inflater;
		private ImageView mRecAppAvatar;
		private TextView mRecAppText;

		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

		private ImageLoader imageLoader = ImageLoader.getInstance();
		private DisplayImageOptions optionsImage;

		private ArrayList<recommendInfo> mRecList;

		public SearchRecInfoAdapter(Context pContext,
				ArrayList<recommendInfo> pRecList) {
			mRecList = pRecList;
			inflater = LayoutInflater.from(pContext);
			optionsImage = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.page_appicon_mostsmall)
					.showImageForEmptyUri(R.drawable.page_appicon_mostsmall)
					.showImageOnFail(R.drawable.page_appicon_mostsmall)
					.cacheInMemory(true).cacheOnDisc(true).build();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mRecList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mRecList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return mRecList.get(arg0).getId();
		}

		@Override
		public View getView(int pPos, View pView, ViewGroup pParent) {
			// TODO Auto-generated method stub
			Log.v(TAG, "aurora.jiangmx getView() pPos: " + pPos);
			if (pView == null)
				pView = inflater.inflate(
						R.layout.activity_search_recommend_item, null);

			mRecAppAvatar = (ImageView) pView.findViewById(R.id.rec_app_avatar);
			if(SystemUtils.isLoadingImage(inflater.getContext()))
			{
				imageLoader.displayImage(mRecList.get(pPos).getIcons().getPx100(),
						mRecAppAvatar, optionsImage, animateFirstListener);
			}
			mRecAppText = (TextView) pView.findViewById(R.id.rec_app_name);
			mRecAppText.setText(mRecList.get(pPos).getTitle());
			return pView;
		}

	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	private void startSearchActivity(String pQuery) {

		Intent lInt = new Intent(mActivity, MarketSearchActivity.class);
		lInt.putExtra("query", pQuery);
		mActivity.startActivity(lInt);
		
		// ((MarketMainActivity)mActivity).startActivityForResult(lInt,
		// SEARCH_REQUEST);
	}

	private void saveSearchRecords() {

		if (!mQuery.trim().equals("") && mQuery != null) {
			if (mSharedPreference == null) {
				mSharedPreference = mActivity
						.getSharedPreferences(Globals.HISTORY_RECORDS_FILENAME,
								Activity.MODE_PRIVATE);
			}

			for (int i = 0; i < Globals.HISTORY_MAX_LIMIT; i++) {

				String lHistory = mSharedPreference.getString(
						Globals.HISTORY_RECORDS + i, null);

				if (lHistory != null) {
					if (lHistory.equals(mQuery))
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
								mQuery).commit();
			} else {
				mSharedPreference
						.edit()
						.putString(Globals.HISTORY_RECORDS + lInsertPos, mQuery)
						.commit();
			}
			if (lInsertPos < Globals.HISTORY_MAX_LIMIT)
				lInsertPos++;

			mSharedPreference.edit()
					.putInt(Globals.HISTORY_NEXT_INSERT_POSITION, lInsertPos)
					.commit();
		}
	}

	// 设置当前主界面的搜索模式
	public void setSearchMode(boolean pIsSearchMode) {
		isSearchMode = pIsSearchMode;
		/*
		 * if (mListView != null) { mListView.setCanMoveHeadView(!isSearchMode);
		 * mListView.showHeadView(); }
		 */
	}
	
	public boolean isInSearchMode(){
	    return isSearchMode;
	}

	public void removeSearchMode() {
	    
		mSearchThread.quit();
		mSearchManager.setController(null);
	}

}
