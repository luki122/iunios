package com.aurora.puremanager.traffic;

import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.mConfig;
import com.gionee.framework.SIMInfo;
import com.gionee.framework.SIMInfoFactory;
import com.mediatek.xlog.Xlog;

public class TrafficAssistantMainActivity extends AuroraActivity implements
		OnClickListener {
	
	private static final String TAG = "TrafficAssistantMainActivity";
	
	private static final int DIALOG_WAITING = 1001;
    //time out message event
    private static final int EVENT_DETACH_TIME_OUT = 2000;
    private static final int EVENT_ATTACH_TIME_OUT = 2001;
    //time out length
    private static final int DETACH_TIME_OUT_LENGTH = 10000;
    private static final int ATTACH_TIME_OUT_LENGTH = 30000;
	
	private Context mContext;
	
	private List<TrafficassistantFlowView> views; 
	private TrafficassistantFlowView trafficassistantFlowView;
	private TrafficassistantFlowView currentFlowView;
	private ViewPager viewPager;
	private LinearLayout bannerDot;
	private TextView mTextViewSimCardFlag;
	private LinearLayout ll_set_flow_package;
	private ImageView iv_set_flow_package;
	private TextView tv_set_flow_package;
	private LinearLayout ll_rank;
	private LinearLayout ll_permission;
	
	private List<ImageView> dotList;

	private static boolean[] sTrafficSettings = new boolean[Constant.SIM_COUNT];
	private String mTimeZoneStr;
	private String mRemainderDay;
	private float mActualFlow;
	private float mDefinedFlow;
	private float mUseFlow;
	private int mTotalFlow;
	private int mWarnPercent;
	private int mDay;
	private int mSimCount;
	private String[] mSimName;
	private boolean[] mSimState = { false, false };
	private int mCurrentSimIndex = 0;
	private int mActivatedSimIndex;
	private int mMobileSimIndex;
	
	private boolean isFirst = true;
	private AuroraProgressDialog progressDialog;
	private boolean mIsGprsSwitching = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		
		if (mConfig.isNative) {
			setContentView(R.layout.trafficassistant_main_activity);
		} else {
			setAuroraContentView(R.layout.trafficassistant_main_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.net_flow_manage);
			setAuroraActionbarSplitLineVisibility(View.GONE);
//			getAuroraActionBar().getTitleView().setTextColor(getResources().getColor(R.color.white));
			getAuroraActionBar().setBackgroundResource(R.color.main_title_color);
		}
		
		initView();
        initData();
        checkSimCardEmpty();
        
        IntentFilter filter = new IntentFilter(mConfig.ACTION_SIM_CHANGE);
		registerReceiver(simChangeReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(simChangeReceiver);
		
		mTimeHandler.removeMessages(EVENT_ATTACH_TIME_OUT);
        mTimeHandler.removeMessages(EVENT_DETACH_TIME_OUT);
	}

	private void initView() {
		trafficassistantFlowView = (TrafficassistantFlowView) findViewById(R.id.flowView);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		bannerDot = (LinearLayout) findViewById(R.id.bannerDot);
		mTextViewSimCardFlag = (TextView) findViewById(R.id.sim_card_flag);
		ll_set_flow_package = (LinearLayout) findViewById(R.id.ll_set_flow_package);
		iv_set_flow_package = (ImageView) findViewById(R.id.iv_set_flow_package);
		tv_set_flow_package = (TextView) findViewById(R.id.tv_set_flow_package);
		ll_rank = (LinearLayout) findViewById(R.id.ll_rank);
		ll_permission = (LinearLayout) findViewById(R.id.ll_permission);
		
		ll_set_flow_package.setOnClickListener(this);
		ll_rank.setOnClickListener(this);
		ll_permission.setOnClickListener(this);
	}
	
	private void initData() {
		initSimInfo(this);
		showTrafficSettingsDialog(this);
		
		currentFlowView = trafficassistantFlowView;
		views = new ArrayList<TrafficassistantFlowView>();
		views.add(new TrafficassistantFlowView(this));
		views.add(new TrafficassistantFlowView(this));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (mSimCount == Constant.SIM_COUNT) {
			refreshViewPager();
			
			if (viewPager != null && isFirst) {
    			viewPager.setCurrentItem(mMobileSimIndex);
    			isFirst = false;
    		}
		} else {
			getAuroraActionBar().setTitle(R.string.net_flow_manage);
		}
		
		resetUI(this, mCurrentSimIndex, true);
	}

	private void initSimInfo(Context context) {
        getSimStateAndName(context);
        mActivatedSimIndex = getActivatedSimIndex(context);
        mMobileSimIndex = getMobileSimIndex(context);
        if (mSimCount == Constant.SIM_COUNT) {
        	viewPager.setVisibility(View.VISIBLE);
        	trafficassistantFlowView.setVisibility(View.GONE);
        	bannerDot.setVisibility(View.VISIBLE);
        } else {
        	if (mSimCount < 1) {
        		mTextViewSimCardFlag.setVisibility(View.VISIBLE);
        	} else {
        		mTextViewSimCardFlag.setVisibility(View.GONE);
        	}
        	viewPager.setVisibility(View.GONE);
        	trafficassistantFlowView.setVisibility(View.VISIBLE);
        	bannerDot.setVisibility(View.GONE);
        }
    }
	
	private void getSimStateAndName(Context context) {
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);
        int count = wrapper.getInsertedSimCount();
        mSimCount = count;
        TypedArray name = context.getResources().obtainTypedArray(R.array.tab_name);
        mSimName = new String[name.length()];
        for (int i = 0; i < name.length(); i++) {
            mSimName[i] = name.getString(i);
        }

        if (count > 0) {
            SIMParame simInfo = new SIMParame();
            for (int i = 0; i < count; i++) {
                simInfo = wrapper.getInsertedSimInfo().get(i);
                mSimState[simInfo.mSlot] = true;
                if (count == 1) {
                    mCurrentSimIndex = simInfo.mSlot;
                } else {
                    mCurrentSimIndex = getIntent()
                            .getIntExtra(TrafficPreference.getSimValue(), Constant.SIM1);
                    if (mCurrentSimIndex == Constant.HAS_NO_SIMCARD) {
                        mCurrentSimIndex = Constant.SIM1;
                    }
                }
            }

            for (int i = 0; i < count; i++) {
                simInfo = wrapper.getInsertedSimInfo().get(i);
                mSimName[simInfo.mSlot] = simInfo.mDisplayName;
            }
        }
        SIMInfoWrapper.setEmptyObject(context);
    }
	
	private int getActivatedSimIndex(Context context) {
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int activatedSimIndex = Constant.HAS_NO_SIMCARD;
        if (simInfo.gprsIsOpenMethod("getMobileDataEnabled") && !simInfo.isWiFiActived()) {
            activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        }
        SIMInfoWrapper.setEmptyObject(context);
        return activatedSimIndex;
    }
	
	private int getMobileSimIndex(Context context) {
		SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int mobileSimIndex = Constant.HAS_NO_SIMCARD;
        mobileSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();
        SIMInfoWrapper.setEmptyObject(context);
        return mobileSimIndex;
	}
	
	private void showTrafficSettingsDialog(Context context) {
		getTrafficSettingsFlag(context);
		boolean isFirst = isFirstEntry(context);
		if (!isFirst && (!sTrafficSettings[0] && !sTrafficSettings[1])) {
			showDialog(context);
			resetFirstEntryFlag(context);
		}
	}

	private boolean getTrafficSettingsFlag(Context context) {
		SharedPreferences share = PreferenceManager
				.getDefaultSharedPreferences(context);
		for (int index = 0; index < Constant.SIM_COUNT; index++) {
			sTrafficSettings[index] = share.getBoolean(
					TrafficPreference.getSimSetting(index), false);
		}
		return true;
	}
	
	private boolean isFirstEntry(Context context) {
        SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isFlag = share.getBoolean(TrafficPreference.getFirstEntryFlag(), false);
        return isFlag;
    }
	
	private boolean resetFirstEntryFlag(Context context) {
        SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(context);
        share.edit().putBoolean(TrafficPreference.getFirstEntryFlag(), true).commit();
        return true;
    }
	
	private void showDialog(Context context) {
//        AmigoAlertDialog.Builder mDialog = new AmigoAlertDialog.Builder(context);
//        mDialog.setTitle(R.string.popup_setting_title_dialog);
//        mDialog.setMessage(R.string.popup_setting_content_dialog);
//        mDialog.setPositiveButton(R.string.action_settings, listener);
//        mDialog.setNegativeButton(R.string.action_cancel, listener);
//        mDialog.show();
    }
	
	private void refreshViewPager() {
		initPagerData(mSimCount);
		viewPager.setAdapter(new SlideImageAdapter());
		viewPager.setOnPageChangeListener(new ImagePageChangeListener());
        viewPager.setCurrentItem(mCurrentSimIndex);
    }
	
	private void resetUI(final Context context, final int sim, boolean flag) {
		if (flag) {
			setViewsVibility(mSimState[sim]);
			setTrafficVariables(context, sim);
			setTxt(mSimState[sim], sTrafficSettings[sim]);
			setOtherViewText(sTrafficSettings[sim]);
			drawImage(context, sim);
		} else {
			setViewsVibility(mSimState[sim]);
			
			new Thread() {
				@Override
				public void run() {
					setTrafficVariables(context, sim);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setTxt(mSimState[sim], sTrafficSettings[sim]);
							setOtherViewText(sTrafficSettings[sim]);
							drawImage(context, sim);
						}
					});
				}
			}.start();
		}
	}
	
	private void setViewsVibility(boolean hasSimCard) {
        if (hasSimCard) {
        	if (mSimCount == Constant.SIM_COUNT) {
        		trafficassistantFlowView.setVisibility(View.GONE);
        	} else {
        		trafficassistantFlowView.setVisibility(View.VISIBLE);
        	}
        } else {
        	trafficassistantFlowView.setVisibility(View.GONE);
        }
    }
	
	private void setTrafficVariables(Context context, int sim) {

		System.out.println("sim: " + sim);
		System.out.println("sTrafficSettings[sim]: " + sTrafficSettings[sim]);
		
        if (!sTrafficSettings[sim]) {

            mTotalFlow = 0;
            mWarnPercent = 0;
            mDay = 1;
            mDefinedFlow = 0;
            int[] dateInterval = initDateInterval(mDay);
            mActualFlow = trafficStatistic(context, dateInterval, sim);
            mUseFlow = mDefinedFlow + mActualFlow;
            mTimeZoneStr = "";

        } else {

            String[] flowData = resetFlowData(context, sim);
            mTotalFlow = (int)(Double.parseDouble(flowData[0]));
            mWarnPercent = Integer.parseInt(flowData[1]);
            mDay = Integer.parseInt(flowData[2]);
            mDefinedFlow = Float.parseFloat(flowData[3]);

            int[] dateInterval = initDateInterval(mDay);
            mTimeZoneStr = dateInterval[1] + "." + dateInterval[2] + " - " + dateInterval[4] + "."
                    + dateInterval[5];
            mRemainderDay = String.format(getString(R.string.traffic_remainder_to_end), TimeFormat.getRemainderDaysToMonthEndDate(mDay));
            mActualFlow = trafficStatistic(context, dateInterval, sim);
            mUseFlow = mDefinedFlow + mActualFlow;
        }
        
        printData();
    }
	
	private void printData() {
		System.out.println("mTotalFlow: " + mTotalFlow);
		System.out.println("mWarnPercent: " + mWarnPercent);
		System.out.println("mDay: " + mDay);
		System.out.println("mDefinedFlow: " + mDefinedFlow);
		System.out.println("mTimeZoneStr: " + mTimeZoneStr);
		System.out.println("mActualFlow: " + mActualFlow);
		System.out.println("mUseFlow: " + mUseFlow);
	}
	
	private void setTxt(boolean hasSimCard, boolean setTrafficPackages) {

        if (!hasSimCard) {
            mTextViewSimCardFlag.setVisibility(View.VISIBLE);
            mTextViewSimCardFlag.setText(getString(R.string.no_card_warning));
            return;
        } else {
            if (mSimCount == Constant.SIM_COUNT) {
                mTextViewSimCardFlag.setVisibility(View.VISIBLE);
//                trafficassistantFlowView.setSetButtonVisable(true);
            } else {
                trafficassistantFlowView.setSetButtonVisable(false);
//                mTextViewSimCardFlag.setVisibility(View.GONE);
            }
            mTextViewSimCardFlag.setVisibility(View.GONE);
        }

        if (mMobileSimIndex == mCurrentSimIndex) {
        	currentFlowView.setSetButtonEnable(false);
        } else {
        	currentFlowView.setSetButtonEnable(true);
        }
        currentFlowView.updatePro(mUseFlow, mTotalFlow, setTrafficPackages, mRemainderDay);
        currentFlowView.setSetButtonListener(trafficViewSetButtonListener);
    }
	
	private void setOtherViewText(boolean setPackage) {
		
    }
	
	private void drawImage(Context context, int sim) {

    }
	
	public static int[] initDateInterval(int cycleDay) {

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
	
	public static float trafficStatistic(Context context, int[] date, int simIndex) {
        long startTime = TimeFormat.getStartTime(date[0], date[1], date[2], 0, 0, 0);
        long endTime = System.currentTimeMillis();

        String flowValue = getTrafficString(context, simIndex, startTime, endTime, System.currentTimeMillis());
        Float value = Float.valueOf(flowValue.substring(0, flowValue.length() - 2));

        if (flowValue.contains(Constant.STRING_UNIT_GB)) {
            value = value * Constant.UNIT;
        } else if (flowValue.contains(Constant.STRING_UNIT_MB)) {

        } else if (flowValue.contains(Constant.STRING_UNIT_KB)) {
            value = value / Constant.KB;
        } else if (flowValue.contains(Constant.STRING_UNIT_B)) {
            value = value / Constant.MB;
        }

        return Float.valueOf(StringFormat.getStringFormat(value));
    }
	
	private static String getTrafficString(Context context, int simIndex, long startTime, long endTime,
            long nowTime) {
        long data = getTrafficData(context, simIndex, startTime, endTime, System.currentTimeMillis());
        String str = StringFormat.format(context, data);
        return str;
    }
	
	private static long getTrafficData(Context context, int simIndex, long startTime, long endTime,
            long nowTime) {

        if (0 == endTime) {
            endTime = System.currentTimeMillis();
        }
        if (0 == nowTime) {
            nowTime = System.currentTimeMillis();
        }

        NetworkTemplate template = MobileTemplate.getTemplate(context, simIndex);
        INetworkStatsService statsService = INetworkStatsService.Stub.asInterface(ServiceManager
                .getService(Context.NETWORK_STATS_SERVICE));
        NetworkStatsHistory netWorkStatsHistory;
        INetworkStatsSession statsSession;
        NetworkStatsHistory.Entry entry;
        long totalBytes = 0;
        try {
            statsSession = statsService.openSession();
            statsService.forceUpdate();
            if (statsSession != null && template != null) {
                netWorkStatsHistory = statsSession.getHistoryForNetwork(template, FIELD_RX_BYTES
                        | FIELD_TX_BYTES);
                entry = netWorkStatsHistory.getValues(startTime, endTime, nowTime, null);
                totalBytes = (entry != null ? entry.rxBytes + entry.txBytes : 0);
                TrafficStats.closeQuietly(statsSession);
            }

        } catch (Exception e) {
            Debuger.print(e.toString());
        }

        return totalBytes;
    }
	
	private String[] resetFlowData(Context context, int sim) {

        if (!sTrafficSettings[sim]) {
            return null;
        }

        String[] flowData = getFlowData(context, sim);

        String[] oldTimes = flowData[4].split("-");
        // Gionee: mengdw <2015-08-26> modify for CR01543245 begin
        long oldUtcTime = 0;
        try {
            oldUtcTime = TimeFormat.getStartTime(Integer.valueOf(oldTimes[0]), Integer.valueOf(oldTimes[1]),
                    Integer.valueOf(oldTimes[2]), Integer.valueOf(oldTimes[3]), Integer.valueOf(oldTimes[4]),
                    Integer.valueOf(oldTimes[5]));
        } catch (NumberFormatException e) {
            Log.d("TrafficAssistantMainActivity", " getoldUtcTime Exception ");
            Debuger.print("Exception :" + e.toString());
        }
        // Gionee: mengdw <2015-08-26> modify for CR01543245 end

        int[] currTimes = TimeFormat.getNowTimeArray();
        long currUtcTime = TimeFormat.getStartTime(currTimes[0], currTimes[1], currTimes[2], currTimes[3],
                currTimes[4], currTimes[5]);
        // Gionee: mengdw <2015-10-19> modify for CR01569888 begin
        try {
            Log.d(TAG, "resetFlowData currTimes len=" + currTimes.length + " oldTimes len=" + oldTimes.length
                    + " flowData[4]=" + flowData[4]);
            if ((currTimes[1] != Integer.valueOf(oldTimes[1])) || (currUtcTime < oldUtcTime)) {
                flowData[3] = "0";
                saveFlowData(context, sim, flowData);
            }
        } catch (Exception e) {
            Debuger.print("Exception :" + e.toString());
        }
        // Gionee: mengdw <2015-10-19> modify for CR01569888 end

        return flowData;
    }
	
	private String[] getFlowData(Context context, int sim) {
        String[] data = TrafficPreference.getPreference(context, sim);
        return data;
    }
	
	private void saveFlowData(Context context, int sim, String[] data) {
        TrafficPreference.setPreference(context, sim, data);
    }
	
	private void initPagerData(int size) {
		int dot_size = size;
		dotList = new ArrayList<ImageView>();
		bannerDot.removeAllViews();
		ImageView dotIv = null;
		LinearLayout.LayoutParams paramsMargin = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		int marginRight = getResources().getDimensionPixelSize(
				R.dimen.net_manage_dot_margin_right);
		paramsMargin.setMargins(0, 0, marginRight, 0);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);
		for (int i = 0; i < dot_size; i++) {
			dotIv = new ImageView(this);
			// 默认第一个为开
			if (i == 0) {
				dotIv.setImageResource(R.drawable.banner_dot_on);
			} else {
				dotIv.setImageResource(R.drawable.banner_dot_off);
			}
			if (i == dot_size - 1) {
				dotIv.setLayoutParams(params);
			} else {
				dotIv.setLayoutParams(paramsMargin);
			}
			dotList.add(dotIv);
			bannerDot.addView(dotIv);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_set_flow_package:
			if (mSimCount == 0) {
				// 没有SIM卡，暂时不做任何操作
			} else {
				Intent intent = new Intent(TrafficAssistantMainActivity.this, TrafficLimitActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(TrafficPreference.getUserDefined(),
						String.valueOf(mDefinedFlow + ":" + mActualFlow));
				bundle.putInt(TrafficPreference.getSimValue(), mCurrentSimIndex);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			
			break;
		case R.id.ll_rank:
			Intent rankIntent = new Intent(TrafficAssistantMainActivity.this, TrafficRankActivity.class);
			startActivity(rankIntent);
			break;
		case R.id.ll_permission:
			Intent controlIntent = new Intent(TrafficAssistantMainActivity.this, AppNetworkControlActivity.class);
			startActivity(controlIntent);
			break;
		}
	}
	
	public static void setTrafficSettings(int index, boolean value) {
        sTrafficSettings[index] = value;
    }
	
	private void findTabViews(int position) {
        if (mSimCount >= Constant.SIM_COUNT) {
            currentFlowView = views.get(position);
        } else {
        	currentFlowView = trafficassistantFlowView;
        }
    }
	
	private class SlideImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            // TODO Auto-generated method stub
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View view, int position, Object arg2) {
            if (view instanceof ViewPager) {
                ((ViewPager) view).removeView(views.get(position));
            }
        }

        @Override
        public Object instantiateItem(View view, int position) {
            // ((ViewPager) view).addView(mImagePageViewList.get(position));
            findTabViews(position);
            if (view instanceof ViewPager) {
                ((ViewPager) view).addView(views.get(position));
            }
            
            resetUI(mContext, mCurrentSimIndex, true);
            onFocesChanged(mCurrentSimIndex);
            return views.get(position);
        }
    }
	
	private class ImagePageChangeListener implements OnPageChangeListener {

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
            mCurrentSimIndex = arg0;
            findTabViews(arg0);
            resetUI(mContext, mCurrentSimIndex, false);
            onFocesChanged(arg0);
            
            for (int i = 0; i < dotList.size(); i++) {
				if (mCurrentSimIndex == i) {
					dotList.get(i).setImageResource(R.drawable.banner_dot_on);
				} else {
					dotList.get(i).setImageResource(R.drawable.banner_dot_off);
				}
			}
        }
    }

    private void onFocesChanged(int position) {
        // Gionee: mengdw <2015-08-05>modify for CR01539821 begin
        Log.d(TAG, "onFocesChanged position=" + position);
        int slotId = position;
        // Gionee: mengdw <2015-10-08>modify for CR01563733 begin
        SIMInfo simInfo = null;
        if (SIMInfoFactory.getDefault() != null) {
            simInfo = SIMInfoFactory.getDefault().getSIMInfoBySlot(mContext, slotId);
        }
        // Gionee: mengdw <2015-10-08>modify for CR01563733 end
        // Gionee: mengdw <2015-08-05>modify for CR01539821 end
        if (position == 0) {
            // Gionee: mengdw <2015-09-29>modify for CR01539821 begin
            // mTextViewSimCardFlag.setText(getString(R.string.traffic_simcard_focus1));
            if (simInfo != null && simInfo.mDisplayName !=null && simInfo.mDisplayName.length()>0) {
//                mTextViewSimCardFlag.setText(getString(R.string.traffic_simcard_focus1) + "("
//                        + simInfo.mDisplayName + ")");
                String title = getString(R.string.net_flow_manage) + " (" + "SIM1 " + simInfo.mDisplayName + ")";
                getAuroraActionBar().setTitle(title);
            } else {
//                mTextViewSimCardFlag.setText(getString(R.string.traffic_simcard_focus1));
                String title = getString(R.string.net_flow_manage) + " (SIM1)";
                getAuroraActionBar().setTitle(title);
            }
            // Gionee: mengdw <2015-08-05>modify for CR01539821 end
//            mSimFocus1.setBackgroundResource(R.drawable.traffic_move_focus);
//            mSimFocus2.setBackgroundResource(R.drawable.traffic_move_unfocus);
        } else {
            // Gionee: mengdw <2015-09-29>modify for CR01539821 begin
            // mTextViewSimCardFlag.setText(getString(R.string.traffic_simcard_focus2));
            
            if (simInfo != null && simInfo.mDisplayName !=null && simInfo.mDisplayName.length()>0) {
//                mTextViewSimCardFlag.setText(getString(R.string.traffic_simcard_focus2) + "("
//                        + simInfo.mDisplayName + ")");
            	String title = getString(R.string.net_flow_manage) + " (" + "SIM2 " + simInfo.mDisplayName + ")";
                getAuroraActionBar().setTitle(title);
            } else {
//                mTextViewSimCardFlag.setText(getString(R.string.traffic_simcard_focus2));
            	String title = getString(R.string.net_flow_manage) + " (SIM2)";
                getAuroraActionBar().setTitle(title);
            }
            // Gionee: mengdw <2015-08-05>modify for CR01539821 end
//
//            mSimFocus1.setBackgroundResource(R.drawable.traffic_move_unfocus);
//            mSimFocus2.setBackgroundResource(R.drawable.traffic_move_focus);
        }
    }
    
    private OnClickListener trafficViewSetButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
//			Toast.makeText(mContext, "当前点击" + mCurrentSimIndex, Toast.LENGTH_SHORT).show();
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
			String title = String.format(mContext.getString(R.string.dialog_change_gprsdefautlsim_title), (mCurrentSimIndex + 1));
			String msg = mContext.getString(R.string.dialog_change_gprsdefautlsim_msg);
			builder.setTitle(title);
			builder.setMessage(msg);
			builder.setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					switchGprsDefautlSIM(mCurrentSimIndex);
				}
			});
			builder.setNegativeButton(mContext.getString(R.string.cancel), null);
			builder.create().show();
		}
	};
    
    Handler mTimeHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case EVENT_ATTACH_TIME_OUT:
                    Xlog.d(TAG, "attach time out......");
                    if (isResumed()) {
                    	removeSimDialog(DIALOG_WAITING);
                    }
                    mIsGprsSwitching = false;
//                    updateDataEnabler();
                    break;
                case EVENT_DETACH_TIME_OUT:
                    Xlog.d(TAG, "detach time out......");
                    if (isResumed()) {
                    	removeSimDialog(DIALOG_WAITING);
                    }
                    mIsGprsSwitching = false;
//                    updateDataEnabler();
                    break;
                default:
                    break;
            }
        };
    };
    
    private void switchGprsDefautlSIM(int index) {
    	int subId = SubscriptionManager.getSubIdUsingPhoneId(index);

        if (subId < 0) {
            return;
        }
        
        if (SubscriptionManager.isValidSubscriptionId(subId) &&
                subId != SubscriptionManager.getDefaultDataSubId()) {
        	SubscriptionManager.from(mContext).setDefaultDataSubId(subId);
            showSimDialog(DIALOG_WAITING);
            mIsGprsSwitching = true;
            if (subId > 0) {
                mTimeHandler.sendEmptyMessageDelayed(EVENT_ATTACH_TIME_OUT, ATTACH_TIME_OUT_LENGTH);
                Xlog.d(TAG,"set ATTACH_TIME_OUT");
            } else {
                mTimeHandler.sendEmptyMessageDelayed(EVENT_DETACH_TIME_OUT, DETACH_TIME_OUT_LENGTH);
                Xlog.d(TAG,"set DETACH_TIME_OUT");
            }
        }

    }
    
    private void showSimDialog(int id) {
    	if (id == DIALOG_WAITING) {
    		progressDialog = new AuroraProgressDialog(this);
    		progressDialog.setMessage(getResources().getString(R.string.dialog_change_gprsdefaultsim_wait));
    		progressDialog.setIndeterminate(true);
    		progressDialog.setCancelable(false);
    		progressDialog.show();
    	}
    }
    
    private void removeSimDialog(int id) {
    	if (id == DIALOG_WAITING) {
    		if (progressDialog != null && progressDialog.isShowing()) {
    			progressDialog.dismiss();
    			mIsGprsSwitching = false;
    		}
    	}
    }
    
    private BroadcastReceiver simChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(mConfig.ACTION_SIM_CHANGE)) {
				
				removeSimDialog(DIALOG_WAITING);
				resetData();
				initSimInfo(context);
				if (mSimCount == Constant.SIM_COUNT) {
					refreshViewPager();
					viewPager.setCurrentItem(mMobileSimIndex);
				} else {
					getAuroraActionBar().setTitle(R.string.net_flow_manage);
				}

				resetUI(context, mCurrentSimIndex, true);
				checkSimCardEmpty();
			}
		}
    	
    };
    
    private void resetData() {
    	mSimCount = 0;
    	mSimName = null;
    	mSimState[0] = false;
    	mSimState[1] = false;
    	mCurrentSimIndex = 0;
    	mActivatedSimIndex = 0;
    	mMobileSimIndex = 0;
    }
    
    private void checkSimCardEmpty() {
    	if (mSimCount == 0) {
    		ll_set_flow_package.setEnabled(false);
    		tv_set_flow_package.setEnabled(false);
    		iv_set_flow_package.setAlpha(0.5f);
    	} else {
    		ll_set_flow_package.setEnabled(true);
    		tv_set_flow_package.setEnabled(true);
    		iv_set_flow_package.setAlpha(1f);
    	}
    }

}
