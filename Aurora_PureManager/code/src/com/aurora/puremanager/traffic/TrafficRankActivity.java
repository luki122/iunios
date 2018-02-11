package com.aurora.puremanager.traffic;

import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.TrafficStats.UID_REMOVED;
import static android.net.TrafficStats.UID_TETHERING;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.view.PagerAdapter;
import aurora.view.ViewPager.OnPageChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;

import com.aurora.puremanager.R;
import com.aurora.puremanager.activity.AppDetailActivity;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.Constants;
import com.aurora.puremanager.utils.ActivityBarUtils;
import com.aurora.puremanager.utils.StringUtils;
import com.aurora.puremanager.utils.mConfig;
import com.google.android.collect.Lists;

public class TrafficRankActivity extends AuroraActivity {

	private ArrayList<TextView> mTextViewEmpty = new ArrayList<TextView>();
	private ArrayList<AuroraListView> mListView = new ArrayList<AuroraListView>();
	private ArrayList<MobileDataUsageAdapter> mListAdapter = new ArrayList<MobileDataUsageAdapter>();
	private int[] mTextViewArray;
	private INetworkStatsService mStatsService;
	private INetworkStatsSession mStatsSession;
	private NetworkPolicyManager mPolicyManager;
	private NetworkTemplate mTemplate;
	private UidDetailProvider mUidDetailProvider;

	private int mInsetSide = 0;
	private int mPosition = 0;
	private int mSimIndex;
	private boolean mEntranceFromNoti = false;
	private ArrayList<AppItem> mAppItems = new ArrayList<AppItem>();
	private Context mContext;
	private AuroraTabWidget mTabWidget;
	public AuroraViewPager mViewPager;
	public MyPagerAdapter mMypagerAdapter;
	private List<View> mViewList;
	private LayoutInflater mInflater;
	private int curActionBarStyle = ActivityBarUtils.TYPE_OF_Normal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getEntranceFlag(this);
		super.onCreate(savedInstanceState);

		mContext = this;
		if (mConfig.isNative) {
			setContentView(R.layout.traffic_rank_activity);
		} else {
			setAuroraContentView(R.layout.traffic_rank_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.traffic_rank);
			getAuroraActionBar()
					.setBackgroundResource(R.color.main_title_color);
		}
		initView();
		initUI();
		try{
			mSimIndex = getIntent().getIntExtra(TrafficPreference.getSimValue(),
					Constant.HAS_NO_SIMCARD);
			initNetworkInfo(mContext, mSimIndex);
		}catch(Exception e){
		}
	}

	private void initView() {
		mViewList = new ArrayList<View>();
		mInflater = getLayoutInflater();
		mViewList.add(mInflater.inflate(R.layout.trafficassistant_rank_layout,null));
		mViewList.add(mInflater.inflate(R.layout.trafficassistant_rank_layout,null));

		mTabWidget = (AuroraTabWidget) findViewById(R.id.rank_tabwidget);
		mTabWidget.seTextColorFocus(R.color.main_title_color);
		mTabWidget.seTextColorNormal(R.color.tab_text_normal_color);
		mTabWidget.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
//				initUI(arg0);
				UpdateView(arg0);
			}
		});
		mMypagerAdapter = new MyPagerAdapter();
		mViewPager = mTabWidget.getViewPager();
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mMypagerAdapter);
		mViewPager.setCurrentItem(0);
	}

	private void initNetworkInfo(Context context, int simIndex) {

		mTemplate = MobileTemplate.getTemplate(context, simIndex);

		mUidDetailProvider = new UidDetailProvider(context);

		mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager
				.getService(Context.NETWORK_STATS_SERVICE));

		mPolicyManager = NetworkPolicyManager.from(context);

		try {
			mStatsSession = mStatsService.openSession();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void initUI() {
		mTextViewArray = new int[] { R.string.traffic_week_no,
				R.string.traffic_month_no };
		for(int i = 0; i < 2; i++){
			View view = mViewList.get(i);
			initNetworkInfo(mContext, mSimIndex);
			mTextViewEmpty.add(i,
					(TextView) view.findViewById(R.id.text_empty));
			
			mTextViewEmpty.get(i).setText(mTextViewArray[i]);
			
			mListView
			.add(i, (AuroraListView) view.findViewById(R.id.traffic_list));
			mListAdapter.add(i, new MobileDataUsageAdapter(
					mUidDetailProvider, mInsetSide, mAppItems));
			mListView.get(i).setOnItemClickListener(mListListener);
			mListView.get(i).setAdapter(mListAdapter.get(i));
		}
	}

	public void UpdateView(int postion) {
		mPosition = postion;
		mTextViewEmpty.get(mPosition).setText(mTextViewArray[mPosition]);
		mTextViewEmpty.get(mPosition).setVisibility(View.GONE);
		updateDetailData();
	}

	private String getIntentCycleTime() {
		return getIntent().getStringExtra(TrafficPreference.getTrafficCycle());
	}

	private String[] analyticalData() {
		String cycle = getIntentCycleTime();
		String[] array = cycle.split("-");
		return array;
	}

	private void getEntranceFlag(Context context) {

		if (getIntentCycleTime() != null) {
			mEntranceFromNoti = true;
		} else {
			mEntranceFromNoti = false;
		}
	}

	private void setVisibilityAndText(boolean flag) {
		if (flag) {
			mTextViewEmpty.get(mPosition).setVisibility(View.GONE);

		} else {
			mTextViewEmpty.get(mPosition).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		try {
			mStatsService.forceUpdate();
		} catch (RemoteException e) {

		}

		getLoaderManager()
				.restartLoader(Constant.LOADER_CHART_DATA,
						ChartDataLoader.buildArgs(mTemplate, null),
						mChartDataCallbacks);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mUidDetailProvider.clearCache();
		mUidDetailProvider = null;
		TrafficStats.closeQuietly(mStatsSession);
		super.onDestroy();
	}

	private OnItemClickListener mListListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			final AppItem app = (AppItem) arg0.getItemAtPosition(arg2);
			if (app != null) {
				if (app.key < 10000) {
					return;
				}
				
				Intent intent = new Intent(TrafficRankActivity.this,AppDetailActivity.class);
				final PackageManager pm = mContext.getPackageManager();
				intent.putExtra(mConfig.ACTION_BAR_STYLE, curActionBarStyle);
				intent.setData(Uri.fromParts(Constants.SCHEME, pm.getNameForUid(app.key), null));
				TrafficRankActivity.this.startActivity(intent);
			}
		}
	};

	private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<ChartData>() {

		@Override
		public Loader<ChartData> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			return new ChartDataLoader(mContext, mStatsSession, args);
		}

		@Override
		public void onLoadFinished(Loader<ChartData> arg0, ChartData arg1) {
			// TODO Auto-generated method stub
			updateDetailData();
		}

		@Override
		public void onLoaderReset(Loader<ChartData> arg0) {
			// TODO Auto-generated method stub
		}

	};

	private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {

		@Override
		public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			return new SummaryForAllUidLoader(mContext, mStatsSession, args);
		}

		@Override
		public void onLoadFinished(Loader<NetworkStats> arg0, NetworkStats arg1) {
			// TODO Auto-generated method stub
			final int[] restrictedUids = mPolicyManager
					.getUidsWithPolicy(POLICY_REJECT_METERED_BACKGROUND);
			refreshListView(arg1, restrictedUids);
		}

		@Override
		public void onLoaderReset(Loader<NetworkStats> arg0) {
			// TODO Auto-generated method stub
			refreshListView(null, new int[0]);
		}

	};

	private void refreshListView(NetworkStats stats, int[] restrictedUids) {
		ArrayList<AppItem> appItems = getAppsUsingMobileData(stats);
		long mLargest = (appItems.size() > 0) ? appItems.get(0).total : 0;
		setVisibilityAndText((mLargest == 0) ? false : true);
		mListAdapter.get(mPosition).notifyDataSetChanged(appItems, mLargest);
	}

	private ArrayList<AppItem> getAppsUsingMobileData(NetworkStats stats) {

		ArrayList<AppItem> items = Lists.newArrayList();
		final int currentUserId = ActivityManager.getCurrentUser();
		final SparseArray<AppItem> knownItems = new SparseArray<AppItem>();
		final int size = (stats != null) ? stats.size() : 0;

		NetworkStats.Entry entry = null;
		for (int i = 0; i < size; i++) {
			entry = stats.getValues(i, entry);
			final int uid = entry.uid;
			final int collapseKey;
			if (UserHandle.isApp(uid)) {
				if (UserHandle.getUserId(uid) == currentUserId) {
					collapseKey = uid;
				} else {
					collapseKey = UidDetailProvider.buildKeyForUser(UserHandle
							.getUserId(uid));
				}
			} else if (uid == UID_REMOVED || uid == UID_TETHERING) {
				collapseKey = uid;
			} else {
				collapseKey = android.os.Process.SYSTEM_UID;
			}

			AppItem item = knownItems.get(collapseKey);
			if (item == null) {
				item = new AppItem(collapseKey);
				items.add(item);
				knownItems.put(item.key, item);
			}

			item.addUid(uid);
			item.total += entry.rxBytes + entry.txBytes;
		}

		Collections.sort(items, new Comparator<AppItem>() {
			@Override
			public int compare(AppItem lhs, AppItem rhs) {
				return Long.compare(rhs.total, lhs.total);
			}
		});
		return items;
	}

	private void updateDetailData() {

		long[] timeZone = getTimeZone(mPosition);

		getLoaderManager().restartLoader(
				Constant.LOADER_SUMMARY,
				SummaryForAllUidLoader.buildArgs(mTemplate, timeZone[0],
						timeZone[1]), mSummaryCallbacks);

	}

	public class MyPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mViewList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == (object);
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			((AuroraViewPager) collection).addView(mViewList.get(position), 0);
			return mViewList.get(position);
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((AuroraViewPager) collection).removeView(mViewList.get(position));
		}
	}

	private long[] getTimeZone(int position) {

		long[] timeZone = new long[3];
		int[] timeArray = null;

		switch (position) {
		case 0:
			if (getIntentCycleTime() != null) {
				String[] array = analyticalData();
				timeZone[0] = Long.valueOf(array[1]);
				timeZone[1] = Long.valueOf(array[2]);

			} else {
				String[] weekArray = TimeFormat.getWeekArray();
				String[] split = weekArray[0].split("-");
				timeArray = new int[] { Integer.parseInt(split[0]),
						Integer.parseInt(split[1]), Integer.parseInt(split[2]),
						0, 0, 0 };

			}
			break;

		case 1:
			 int cycleDay =getCycleDay(mContext,mSimIndex);
			 timeArray =initDateInterval(cycleDay);
			break;
		default:
			break;
		}

		if (timeArray != null) {
			timeZone[0] = TimeFormat.getStartTime(timeArray[0], timeArray[1],
					timeArray[2], 0, 0, 0);
			timeZone[1] = System.currentTimeMillis();
		}

		timeZone[2] = System.currentTimeMillis();

		return timeZone;
	}
	
	private int getCycleDay(Context context, int sim) {
        int cycleDay = 0;
        try {

            String[] data = TrafficPreference.getPreference(context, sim);
            cycleDay = Integer.valueOf(data[2]);

        } catch (Exception ex) {
            cycleDay = 1;
        }
        return cycleDay;
    }
	
	 private int[] initDateInterval(int cycleDay) {

	        Calendar cal = Calendar.getInstance();
	        int currentYear = cal.get(Calendar.YEAR);
	        int currentMonth = cal.get(Calendar.MONTH);
	        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
	        int[] dayOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	        if ((currentYear % 4 == 0 && currentYear % 100 != 0) || currentYear % 400 == 0) {
	            dayOfMonth[1] = 29;
	        }

	        int minMonth = 1, maxMonth = 12;
	        int[] date = new int[6];
	        if (currentDay >= cycleDay) {
	            int day = (cycleDay < dayOfMonth[currentMonth]) ? cycleDay : dayOfMonth[currentMonth];
	            int month = currentMonth;
	            int year = currentYear;
	            date[0] = year;
	            date[1] = month + 1;
	            date[2] = day;

	            day = (cycleDay - 1 > 0) ? (cycleDay - 1) : (dayOfMonth[currentMonth]);
	            if (day == cycleDay - 1) {
	                month = (currentMonth + 1) % maxMonth;
	                if (month == 0) {
	                    year++;
	                }
	            }
	            date[3] = year;
	            date[4] = month + 1;
	            date[5] = day;

	        } else {
	            int day = (cycleDay - 1 < dayOfMonth[currentMonth]) ? (cycleDay - 1) : (dayOfMonth[currentMonth]);
	            int month = currentMonth;
	            int year = currentYear;
	            date[3] = year;
	            date[4] = month + 1;
	            date[5] = day;

	            month = (currentMonth - 1) < (minMonth - 1) ? (maxMonth - 1) : (currentMonth - 1);

	            if (currentMonth - 1 < minMonth - 1) {
	                year--;
	            }
	            day = cycleDay < dayOfMonth[month] ? cycleDay : dayOfMonth[month];
	            date[0] = year;
	            date[1] = month + 1;
	            date[2] = day;
	        }

	        return date;
	    }
}
