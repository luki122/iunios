package com.gionee.calendar.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.CalendarController.EventType;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;

//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
public class GNSlidingView  extends LinearLayout{
	
	//Gionee <jiating><2013-06-17> modify for CR00822801 begin
	private static LinearLayout mSearch;
	private static LinearLayout mAllActivities;
	private static TextView mSearchText;
	private static ImageView mSearchView;
	private static TextView mAllActivityText;
	private static ImageView mAllActivityView;
	private static TextView mClearText;
	private static ImageView mClearImage;
	private static LinearLayout mClearActivities;
	//Gionee <jiating><2013-06-17> modify for CR00822801 end
	private LinearLayout mSetting;
	private CalendarController mController;
	private LinearLayout horoscope;
	private AllInOneActivity  allInOneActivity;
	//Gionee <jiating><2013-06-20> modify for CR00828017 begin
//	public static int clickSlidingItem=-1;
//	public static final int CLCIK_SETTING=4;
//	public static final int CLICK_CLEAR=3;
//	public static final int CLICK_ALLEVENT=2;
//	public static final int CLICK_SEARCH=1;
//	public static final int CLICK_HOCOSPOSE=0;
	//Gionee <jiating><2013-06-20> modify for CR00828017 end
	private View test;
	private Context mContext;
	
	// Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin
	private View mAlmanacBotton = null;
	// Gionee <jiangxiao> <2013-07-16> add for CR00837096 end
	public GNSlidingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public GNSlidingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		allInOneActivity=(AllInOneActivity)context;
		mController = CalendarController.getInstance(context);
		LayoutInflater.from(context).inflate(
				R.layout.gn_all_in_one_sliding_view, this, true);
		mSearch=(LinearLayout)findViewById(R.id.sliding_serach);
		//Gionee <jiating><2013-06-17> modify for CR00822801 begin
		mSearchText=(TextView)findViewById(R.id.gn_sliding_search_content);
		mSearchView=(ImageView)findViewById(R.id.gn_sliding_search_image);
		mAllActivityText=(TextView)findViewById(R.id.gn_silding_all_activities_content);
		mAllActivityView=(ImageView)findViewById(R.id.gn_silding_all_activities_image);
		mClearText=(TextView)findViewById(R.id.gn_all_in_one_clear_all_activities);
		mClearImage=(ImageView)findViewById(R.id.gn_sliding_clear_image);
		//Gionee <jiating><2013-06-17> modify for CR00822801 end
		test=findViewById(R.id.test);
		mAllActivities=(LinearLayout)findViewById(R.id.sliding_all_activities);
		mClearActivities=(LinearLayout)findViewById(R.id.sliding_clear_all_activities);
		mSetting=(LinearLayout)findViewById(R.id.sliding_setting);
	
		//Gionee <jiating><2013-06-20> modify for CR00828017 begin
		mSetting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_SETTING);
//				clickSlidingItem=CLCIK_SETTING;
//				allInOneActivity.setMenuIn();
				mController.sendEvent(this, EventType.LAUNCH_SETTINGS, null, null, 0, 0);
			}
		});
		mAllActivities.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_ALL_ACTIVITY_CLICK);
//				clickSlidingItem=CLICK_ALLEVENT;
//				allInOneActivity.setMenuIn();
				mController.sendEvent(this, EventType.LAUNCH_AGENDA, null, null, 0, 0);
				
			}
		});
		
		mSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_SERCH_CLICK);
//				clickSlidingItem=CLICK_SEARCH;
//				allInOneActivity.setMenuIn();
				mController.sendEvent(this, EventType.LAUNCH_SEARCH, null, null, 0, 0);
			}
		});
		
		mClearActivities.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(mContext, Statistics.SLIDING_VIEW_CLEAR_ALL_ACTIVITY);
//				clickSlidingItem=CLICK_CLEAR;
//				allInOneActivity.setMenuIn();
				mController.sendEvent(this, EventType.LAUNCH_SELECT_CLEARABLE_CALENDARS, null, null, 0, 0);
			}
		});
		
		
		horoscope=(LinearLayout)findViewById(R.id.sliding_xingzuo);
		
		horoscope.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				clickSlidingItem=CLICK_HOCOSPOSE;
//				allInOneActivity.setMenuIn();
				mController.sendEvent(this, EventType.LAUNCH_HOROSCOPE, null, null, 0, 0);
			}
		});
		//Gionee <jiating><2013-06-20> modify for CR00828017 end
		test.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		// Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin
		mAlmanacBotton = this.findViewById(R.id.sliding_almanac);
		mAlmanacBotton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mController.sendEvent(this, EventType.LAUNCH_ALMANAC, null, null, 0, 0);
			}
		});
		// Gionee <jiangxiao> <2013-07-16> add for CR00837096 end
	}

	public GNSlidingView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
//Gionee <jiating><2013-06-17> modify for CR00822801 begin
	
	public static void setViewState(Long eventCount,Long allEventCount){
		
		if(eventCount>0){
			mAllActivities.setEnabled(true);
			mAllActivities.setClickable(true);
			mSearch.setEnabled(true);
			mSearch.setClickable(true);
			mSearchText.setEnabled(true);
			mSearchText.setFocusable(true);
			mSearchView.setEnabled(true);
			mAllActivityText.setEnabled(true);
			mAllActivityView.setEnabled(true);
		
		}else{
			
			mAllActivities.setEnabled(false);
			mAllActivities.setClickable(false);
			mSearch.setEnabled(false);
			mSearch.setClickable(false);
			mSearchText.setEnabled(false);	
			mSearchText.setFocusable(false);
			mSearchView.setEnabled(false);
			mAllActivityText.setEnabled(false);
			mAllActivityView.setEnabled(false);
			
		}
		if(allEventCount>0){
			 mClearText.setEnabled(true);
			 mClearText.setFocusable(true);
			 mClearImage.setEnabled(true);
			 mClearImage.setFocusable(true);
			 mClearActivities.setClickable(true);
			 mClearActivities.setEnabled(true);
		}else{
			 mClearText.setEnabled(false);
			 mClearText.setFocusable(false);
			 mClearImage.setEnabled(false);
			 mClearImage.setFocusable(false);
			 mClearActivities.setClickable(false);
			 mClearActivities.setEnabled(false);
		}
		
	}
	//Gionee <jiating><2013-06-17> modify for CR00822801 end

}
//Gionee <jiating> <2013-04-24> modify for CR00000000  end