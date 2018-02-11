// Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin

package com.gionee.almanac;

import com.android.calendar.R;
import com.gionee.almanac.GNAlmanacUtils.AlmanacInfo;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.day.MessageDispose;
import com.gionee.calendar.view.GNAnimationutils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;
import android.text.format.Time;
import android.util.Log;

import aurora.app.AuroraActivity;

public class GNAlmanacActivity extends AuroraActivity implements ViewFactory,MessageDispose {
	private static final String LOG_TAG = "almanac";
	
	static final int EVENT_QUERY_ALMANAC_INFO = 334;
	static final int EVENT_EXTRA_NO_MOVE = 0;
	static final int EVENT_EXTRA_GOTO_NEXT = 1;
	static final int EVENT_EXTRA_GOTO_PREV = 2;
	
	static final int EVENT_CHANGE_QUERY_TIME = 335;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.d(LOG_TAG, "GNAlmanacActivity---handleMessage---");
			if(msg.what == EVENT_QUERY_ALMANAC_INFO) {
				// set UI color & content here
//				if(msg.arg1 == GNAlmanacUtils.ERROR_CODE_ALMANAC_QUERY_OK) {
					GNAlmanacUtils.AlmanacInfo result = (GNAlmanacUtils.AlmanacInfo) msg.obj;
					int fiveElement = GNAlmanacUtils.getInstance().getFiveElementFromAlmanacInfo(result);

					if(msg.arg2 == EVENT_EXTRA_GOTO_NEXT) {
						setInOutAnimationDefault(true);
						// mViewSwitcher.showNext();
					} else if(msg.arg2 == EVENT_EXTRA_GOTO_PREV) {
						setInOutAnimationDefault(false);
						// mViewSwitcher.showNext();
					}
					mViewSwitcher.showNext();
					// change action bar color as soon as the animation start
					Log.d(LOG_TAG, "GNAlmanacActivity---handleMessage---fiveElement == " + fiveElement);
					setActionBarColor(fiveElement);
					Log.d(LOG_TAG, "GNAlmanacActivity---handleMessage---result == " + result);
					Log.d(LOG_TAG, "GNAlmanacActivity---handleMessage---mQueryDate == " + mQueryDate);
					setAlmanacView(result, fiveElement, mQueryDate);
//				} else if(msg.arg1 == GNAlmanacUtils.ERROR_CODE_ALMANAC_QUERY_INVALID_DATE) {
//					Log.d(LOG_TAG, "ERROR_CODE_ALMANAC_QUERY_INVALID_DATE");
//				}
			} // end of EVENT_QUERY_ALMANAC_INFO
			else if(msg.what == EVENT_CHANGE_QUERY_TIME) {
				mQueryDate.set((Time) msg.obj);
				Log.d(LOG_TAG, "change query date to " + GNCalendarUtils.printDate(mQueryDate));
				showBackTodayButton(true);
			} // end of EVENT_CHANGE_QUERY_TIME
		}
	};
	
	private ImageView mBackIcon = null;
	private RelativeLayout mBackIconBg = null;
	private TextView mTitle = null;
	private Button mBackToday = null;
	private ImageView mTimePicker = null;
	
	private ViewSwitcher mViewSwitcher = null;
	
	private int mColorMetal = 0;
	private int mColorWood = 0;
	private int mColorWater = 0;
	private int mColorFire = 0;
	private int mColorSoil = 0;
	
	private void loadRes() {
		Resources res = this.getResources();

		mColorMetal = res.getColor(R.color.gn_almanac_metal);
		mColorWood = res.getColor(R.color.gn_almanac_wood);
		mColorWater = res.getColor(R.color.gn_almanac_water);
		mColorFire = res.getColor(R.color.gn_almanac_fire);
		mColorSoil = res.getColor(R.color.gn_almanac_soil);
	}
	
	private void loadUIElements() {
		mBackIcon = (ImageView) this.findViewById(R.id.gn_almanac_back_icon);
		mBackIconBg = (RelativeLayout) this.findViewById(R.id.gn_almanac_back_icon_bg);
		mBackIconBg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		mTitle = (TextView) this.findViewById(R.id.gn_almanac_activity_title);
		mBackToday = (Button) this.findViewById(R.id.gn_almanac_back_today_button);
		mBackToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Time now = new Time();
				now.setToNow();
				mHandler.obtainMessage(EVENT_CHANGE_QUERY_TIME, now).sendToTarget();
				int action = ((now.toMillis(true) - mQueryDate.toMillis(true)) > 0) ? 
						EVENT_EXTRA_GOTO_NEXT : EVENT_EXTRA_GOTO_PREV;
				GNAlmanacUtils db = GNAlmanacUtils.getInstance();
//				db.getAlmanacInfo(now.year, now.month + 1, now.monthDay,
//						mHandler.obtainMessage(EVENT_QUERY_ALMANAC_INFO, 0, action));
				Log.d(LOG_TAG, "GNAlmanacActivity---onClick---action == " + action);
				db.getAlmanacInfo(now.year, now.month + 1, now.monthDay,
						GNAlmanacActivity.this,EVENT_QUERY_ALMANAC_INFO,action);
			}
		});
		// mTimePicker = (ImageView) this.findViewById(R.id.gn_almanac_time_picker);
		
		mViewSwitcher = (ViewSwitcher) this.findViewById(R.id.gn_almanac_view_switcher);
	}
	
	private void setActionBarColor(int fiveElements) {
		Log.d(LOG_TAG, "setActionBarColor() " + fiveElements);
		switch(fiveElements) {
			case GNAlmanacConstants.FIVE_ELEMENTS_METAL:
				mBackIcon.setBackgroundResource(R.drawable.gn_almanac_back_jin);
				mTitle.setTextColor(mColorMetal);
				mBackToday.setTextColor(mColorMetal);
				// mTimePicker.setBackgroundResource(R.drawable.gn_almanac_time_picker_jin);
				break;
			case GNAlmanacConstants.FIVE_ELEMENTS_WOOD:
				mBackIcon.setBackgroundResource(R.drawable.gn_almanac_back_mu);
				mTitle.setTextColor(mColorWood);
				mBackToday.setTextColor(mColorWood);
				// mTimePicker.setBackgroundResource(R.drawable.gn_almanac_time_picker_mu);
				break;
			case GNAlmanacConstants.FIVE_ELEMENTS_WATER:
				mBackIcon.setBackgroundResource(R.drawable.gn_almanac_back_shui);
				mTitle.setTextColor(mColorWater);
				mBackToday.setTextColor(mColorWater);
				// mTimePicker.setBackgroundResource(R.drawable.gn_almanac_time_picker_shui);
				break;
			case GNAlmanacConstants.FIVE_ELEMENTS_FIRE:
				mBackIcon.setBackgroundResource(R.drawable.gn_almanac_back_huo);
				mTitle.setTextColor(mColorFire);
				mBackToday.setTextColor(mColorFire);
				// mTimePicker.setBackgroundResource(R.drawable.gn_almanac_time_picker_huo);
				break;
			case GNAlmanacConstants.FIVE_ELEMENTS_SOIL:
				mBackIcon.setBackgroundResource(R.drawable.gn_almanac_back_tu);
				mTitle.setTextColor(mColorSoil);
				mBackToday.setTextColor(mColorSoil);
				// mTimePicker.setBackgroundResource(R.drawable.gn_almanac_time_picker_tu);
				break;
			default:
				mBackIcon.setBackgroundResource(R.drawable.gn_almanac_back_shui);
				mTitle.setTextColor(mColorWater);
				mBackToday.setTextColor(mColorWater);
				break;
		}
		
		// check visibility
		if(mBackIcon.getVisibility() != View.VISIBLE) {
			mBackIcon.setVisibility(View.VISIBLE);
		}
		if(mTitle.getVisibility() != View.VISIBLE) {
			mTitle.setVisibility(View.VISIBLE);
		}
	}
	

	private Time mQueryDate = new Time();
	
	@Override
	protected void onCreate(Bundle arg) {
		super.onCreate(arg);
		
		this.setContentView(R.layout.gn_almanac_activity);
		this.loadUIElements();
		this.loadRes();
	    
	    mViewSwitcher.setFactory(this);
		
		// mQueryDate.setToNow();
	    initQueryDateByIntent();
	    GNAlmanacUtils db = GNAlmanacUtils.getInstance();
//	    db.getAlmanacInfo(mQueryDate.year, mQueryDate.month + 1, mQueryDate.monthDay, 
//	    		mHandler.obtainMessage(EVENT_QUERY_ALMANAC_INFO));
	    db.getAlmanacInfo(mQueryDate.year, mQueryDate.month + 1, mQueryDate.monthDay, 
	    		GNAlmanacActivity.this,EVENT_QUERY_ALMANAC_INFO,-1);
		Log.v("Calendar","GNAlmancActivity---onCreate");

	}
	
	public static final String ALMANAC_EXTRA = "almanac_extra";
	private void initQueryDateByIntent() {
		Intent intent = this.getIntent();
		long millis = intent.getLongExtra(ALMANAC_EXTRA, System.currentTimeMillis());
		// can not set mQueryDate by send message, because query action will be
		// executed earlier than time modify
		// Time t = new Time();
		// t.set(millis);
		// t.normalize(true);
		// Log.d(LOG_TAG, "set mQueryDate to " + GNCalendarUtils.printDate(t) +
		// ", " + millis);
		// mHandler.obtainMessage(EVENT_CHANGE_QUERY_TIME, t).sendToTarget();
		mQueryDate.set(millis);
		mQueryDate.normalize(true);
		this.showBackTodayButton(false);
	}
	
	@Override
	public View makeView() {
		GNAlmanacView view = new GNAlmanacView(this);
		view.setLayoutParams(new ViewSwitcher.LayoutParams(
				ViewSwitcher.LayoutParams.MATCH_PARENT,
				ViewSwitcher.LayoutParams.MATCH_PARENT));
		view.setQueryHandler(mHandler);
		view.setQueryInterface(this);
		return view;
	}
	
	private void setAlmanacView(GNAlmanacUtils.AlmanacInfo info, int fiveElement, Time queryDate) {
		if(mViewSwitcher != null) {
			GNAlmanacView currentView =  (GNAlmanacView) mViewSwitcher.getCurrentView();
			currentView.setAlmanacGregTime(queryDate);
			currentView.setAlmanacView(info, fiveElement);
			Log.d(LOG_TAG, "set current view query date as " + GNCalendarUtils.printDate(queryDate));
		}
	}
	

	static final long DURATION_VIEW_SWITCHING = 400;
	static final Interpolator DEFAULT_INTERPOLATER = new AccelerateInterpolator();
	
	private void setInOutAnimationDefault(boolean gotoFuture) {
		this.setInOutAnimation(gotoFuture, DEFAULT_INTERPOLATER, DURATION_VIEW_SWITCHING);
	}
	
	private void setInOutAnimation(final boolean gotoFuture, Interpolator interpolator, long duration) {
		// init params for switching animation
		float inFromXValue, inToXValue;
		float outFromXValue, outToXValue;
		float progress = 0;
		
		if(gotoFuture) {
			inFromXValue = 1.0f - progress;
            inToXValue = 0.0f;
            outFromXValue = -progress;
            outToXValue = -1.0f;
		} else {
			inFromXValue = progress - 1.0f;
            inToXValue = 0.0f;
            outFromXValue = progress;
            outToXValue = 1.0f;
		}
		
		TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);

		if(duration < 0) {
			duration = 0;
		}
        inAnimation.setDuration(duration);
        outAnimation.setDuration(duration);
        
        if(interpolator != null) {
            inAnimation.setInterpolator(interpolator);
            outAnimation.setInterpolator(interpolator);
        }
        
        inAnimation.setAnimationListener(new AnimationListener() {
        	@Override
        	public void onAnimationEnd(Animation animation) {
        		if(mShowButtonAnimation) {
        			mBackToday.setVisibility(View.VISIBLE);
        			mBackToday.startAnimation(GNAnimationutils.backTodayAnumation(GNAlmanacActivity.this));
        			mShowButtonAnimation = false;
        		}
        	}
        	
        	@Override
        	public void onAnimationRepeat(Animation animation) {
        		// do nothing
        	}
        	
        	@Override
        	public void onAnimationStart(Animation animation) {
        		// for lunar date field animation
        		// GNAlmanacView currentView = (GNAlmanacView) mViewSwitcher.getCurrentView();
        		// currentView.startReturnAnimation(gotoFuture);
        	}
        });
        
        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);
	} // end of setInOutAnimation()
	
	private boolean mShowButtonAnimation = false;
	
	private void showBackTodayButton(boolean showAnim) {
		Time now = new Time();
		now.setToNow();
		
		if(mBackToday.getVisibility() == View.INVISIBLE && !GNCalendarUtils.isIdenticalDate(now, mQueryDate)) {
			// visible
			if(showAnim) {
				mShowButtonAnimation = true;
			} else {
				mBackToday.setVisibility(View.VISIBLE);
			}
			// mBackToday.setAnimation(GNAnimationutils.backTodayAnumation(this));
		} else if(mBackToday.getVisibility() == View.VISIBLE && GNCalendarUtils.isIdenticalDate(now, mQueryDate)) {
			mBackToday.setVisibility(View.INVISIBLE);
		}
	}


	@Override
	public void sendMessage(int msg, AlmanacInfo almanacInfo, Time time,int action,int type) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "GNAlmanacActivity---sendMessage---almanacInfo == " + almanacInfo);
			int fiveElement = GNAlmanacUtils.getInstance().getFiveElementFromAlmanacInfo(almanacInfo);
			Log.d(LOG_TAG, "GNAlmanacActivity---sendMessage---action == " + action);

			if(action == EVENT_EXTRA_GOTO_NEXT) {
				setInOutAnimationDefault(true);
				// mViewSwitcher.showNext();
			} else if(action == EVENT_EXTRA_GOTO_PREV) {
				setInOutAnimationDefault(false);
				// mViewSwitcher.showNext();
			}
			mViewSwitcher.showNext();
			// change action bar color as soon as the animation start
			Log.d(LOG_TAG, "GNAlmanacActivity---handleMessage---fiveElement == " + fiveElement);
			setActionBarColor(fiveElement);
			Log.d(LOG_TAG, "GNAlmanacActivity---handleMessage---mQueryDate == " + mQueryDate);
			setAlmanacView(almanacInfo, fiveElement, mQueryDate);
	}	
	
}

//Gionee <jiangxiao> <2013-07-16> add for CR00837096 end