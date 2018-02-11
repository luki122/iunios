package com.android.keyguard.view;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.DataFormatException;

import com.android.keyguard.AuroraKeyguardHostView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R;
import com.android.keyguard.charge.ChartAnimationView;
import com.android.keyguard.utils.AuroraLog;
import com.android.keyguard.utils.LockScreenBgUtils;
import com.android.keyguard.utils.LunarUtils;
import com.android.keyguard.utils.LockScreenUtils;
import com.android.keyguard.KeyguardSecurityCallback;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;


public class StatusView extends FrameLayout {

    private static final String TAG = "StatusView";

    private static final String DATE_FORMAT = "MM-dd";
    private static final String DATE_MONTH_FORMAT = "MM";
    private static final String DATE_DAY_FORMAT = "dd";
    private static final boolean USE_UPPER_CASE = true;

    public static final int LOCK_ICON = 0; // R.drawable.ic_lock_idle_lock;
    public static final int ALARM_ICON = R.drawable.ic_lock_idle_alarm;
    public static final int CHARGING_ICON = 0; // R.drawable.ic_lock_idle_charging;
    public static final int BATTERY_LOW_ICON = 0; // R.drawable.ic_lock_idle_low_battery;

    private CharSequence mDateFormatString;

	private ImageView mWallPaper;
    private LinearLayout mWallPaperBg;
	// Aurora liugj 2014-06-27 add for MusicLockScreen start
    private RelativeLayout mMusicLockLayout;
    private LinearLayout mNormalLockLayout;
    public static boolean isPlayingMusic = false;
    public static boolean isMusicShow = false;
	// Aurora liugj 2014-06-27 add for MusicLockScreen end
    private boolean isResumed = false;
	
//    private TextView mDateView;
    private TextView mDateMonthView;
    private TextView mDateDivView;
    private TextView mDateDayView;
    private TextView mAlarmStatusView;
    //private ClockView mClockView;
    private TextView mChartFinishView;
    //private TextView mPalmRejectionView;

    private ClockImageView mClockImageView1;
    private ClockImageView mClockImageView2;
    private ClockImageView mClockImageView3;
    private ClockImageView mClockImageView4;
    private ClockImageView mClockImageView5;
    
    private LayoutTransition mTransition;

    private TextView mLunarDateView;
    private TextView mWeekView;

    private Typeface mClockFace;
    private Typeface mDateFace;
    private Typeface mWeekFace;

    private Context mContext;

//    private static final String AURORA_CLOCK_FONT_FILE = "/system/fonts/LockScreen_Clock.ttf";
    private static final String AURORA_CLOCK_FONT_FILE = "/system/fonts/Roboto-Light.ttf";
    private static final String AURORA_DATE_FONT_FILE = "/system/fonts/Roboto-Light.ttf";
    private static final String AURORA_WEEK_FONT_FILE = "/system/fonts/DroidSans.ttf";

    // Aurora <zhang_xin> <2014-3-5> modify for battery charge begin
    private ChartAnimationView mChartAnimationView;
    private ImageView mChartLayoutBg;
    private int mBatteryLevel = 100;
    private boolean mShowingBatteryInfo = false;
    private boolean mIsDone = false;
    private boolean mDelayed = false;

	// Aurora liugj 2014-06-16 added for MusicLockScreen start    
    static final int DEFAULT_DETAL = 50;
    static final int SNAP_VELOCITY = 4000;
    private static final int STATE_UNLOCK = 1;
    private static final int STATE_CAMERA = 2;
    private static final int STATE_REST = 0;
    private int mState = STATE_REST;
    private static final int UNVAIABLE_Y = -1;
    
    private int mDetalY = 0;
    private MyInterpolator mInterpolater;
    private float mLastY;
    private Scroller mScroller;
    private VelocityTracker mTraker;
    private float mLastDownX;
    private float mLastDownY;
    
    private float mTotalDx;
    private float mTotalDy;
    private boolean mOnMove = false;
    private boolean mOnYMove = false;
    
    private KeyguardSecurityCallback mCallback;
    private AlphaBackground mAlphaBackground;
	// Aurora liugj 2014-06-16 added for MusicLockScreen end
    
    // Aurora <zhang_xin> <2014-3-5> modify for battery charge end

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        initView();
    }
    
    private Runnable mBackRunnable = new Runnable() {

        public void run() {
            int i = Math.min(mDetalY, 1);
            mDetalY = -1 + mDetalY;
            scrollBy(0, i);
            if (mDetalY >= 0) {
//                post(mBackRunnable);
                postDelayed(mBackRunnable, 4);
            } else {
                tapScrollY();
            }
        }
    };

    Runnable mUnLockRunnable = new Runnable() {

        public void run() {
            mCallback.userActivity(0);
            if (mAlphaBackground != null) {
            	mAlphaBackground.setAlpha(0);
			}
            mCallback.dismiss(false);
        }

    };

    private void initView() {
        mInterpolater = new MyInterpolator();
        mScroller = new Scroller(getContext(), mInterpolater);
    }

    private void tapScrollY() {
        int i = Math.max(getScrollY(), 1500);
        mInterpolater.setInterpolatorType(3);
        mScroller.startScroll(0, DEFAULT_DETAL, 0, -DEFAULT_DETAL, i);
        postInvalidate();
    }

	 // Aurora liugj 2014-09-12 added for 防误触 start
    public void handleActionUnlockUp() {
    	if (mTraker != null) {
    		mTraker.computeCurrentVelocity(1000);
    		handleActionUnlockUp(mTraker);
    		mTraker.recycle();
            mTraker = null;
		}
    	mState = STATE_REST;
        mLastY = UNVAIABLE_Y;
        mOnMove = false;
        mOnYMove = false;
    }
	 // Aurora liugj 2014-09-12 added for 防误触 end
    
    private void handleActionUnlockUp(VelocityTracker tracker) {
        int j = getHeight();
        int k = getScrollY();
        int l = 0;
        int d = 400;
        Runnable runnable = null;
        if ((tracker!= null && tracker.getYVelocity() < -SNAP_VELOCITY) || k > (j / 3)) {
            l = j - k;
            d = Math.min(l, 300);
            runnable = mUnLockRunnable;
            mInterpolater.setInterpolatorType(0);
            mScroller.startScroll(0, k, 0, l, d);
            invalidate();
            if (mAlphaBackground != null) {
            	mAlphaBackground.setAlpha(0);
			}
        } else if (k > DEFAULT_DETAL) {
            l = Math.max(k, 1800);
            mInterpolater.setInterpolatorType(1);
            mScroller.startScroll(0, k, 0, -k, l);
            invalidate();
        } else {
            d = 0;
            mDetalY = DEFAULT_DETAL - k;
            runnable = mBackRunnable;
            if (mAlphaBackground != null) {
            	mAlphaBackground.setAlpha(180);
			}
        }
//        logd("getYVelocity=" + tracker.getYVelocity() + ",getScrollY=" + k + ",mDetalY=" + mDetalY + ",mScroller.getY=" + mScroller.getCurrY());

        if (runnable != null) {
            long l1 = d;
            postDelayed(runnable, l1);
        }
    }

    private void handleActionUnlockMove(float y) {
        int i = ( int ) (mLastY - y);
        mLastY = y;
        if (i < 0) {
            int k = getScrollY();
            if (k >= 0) {
                int i1 = Math.max(i, -k);
                if (i1 < 0) {
                    scrollBy(0, i1);
                }
            }
        } else {
            int j1 = getHeight() - getScrollY();
            if (j1 >= 0) {
                scrollBy(0, Math.min(j1, i));
            }
        }
        backgroundAlphaChanged(mAlphaBackground, getScrollY());
//        logd(",getScrollY=" + getScrollY() + ",mDetalY=" + mDetalY + ",mScroller.getY=" + mScroller.getCurrY() + ",mlastY=" + mLastY);
//        logd(",getScrollY=" + getScrollY() + ",mlastY=" + mLastY + ",i=" + i);
    }

    public void setAlphaBackgroundView(AlphaBackground background) {
        mAlphaBackground = background;
    }
    
    public void backgroundAlphaChanged(AlphaBackground background, float frac) {
    	if (background == null) {
			return;
		}
      final int fullAlpha = 255;
      final int H = getHeight() / 2;
      frac = Math.min(frac, H);
      float alpha = 1f;
      alpha = fullAlpha - (fullAlpha * frac / H);
      background.setAlpha((int)alpha);
    }
    
    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
    }
    
    class MyInterpolator extends AccelerateDecelerateInterpolator {
        private int mInterpolatorType;

        private float bounce(float f) {
            return 8.0F * f * f;
        }

        public float getInterpolation(float input) {
            float f2;
            if (mInterpolatorType == 0) {
                f2 = super.getInterpolation(input);
            } else {
                float f1 = input * 1.1226F;
                if (f1 < 0.3535F)
                    f2 = bounce(f1);
                else if (f1 < 0.7408F)
                    f2 = 0.7F + bounce(f1 - 0.54719F);
                else if (f1 < 0.9644F)
                    f2 = 0.9F + bounce(f1 - 0.8526F);
                else
                    f2 = 0.95F + bounce(f1 - 1.0435F);
            }
            return f2;
        }

        public void setInterpolatorType(int i) {
            mInterpolatorType = i;
        }

    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources res = getContext().getResources();
        mDateFormatString = DATE_FORMAT;
//                res.getText(R.string.abbrev_wday_month_day_no_year);
//        mDateView = ( TextView ) findViewById(R.id.date);
        mDateMonthView = ( TextView ) findViewById(R.id.date_month);
        mDateDivView = ( TextView ) findViewById(R.id.date_div);
        mDateDayView = ( TextView ) findViewById(R.id.date_day);
        mAlarmStatusView = ( TextView ) findViewById(R.id.alarm_status);
        //mClockView = ( ClockView ) findViewById(R.id.clock_text);

        mClockImageView1 = (ClockImageView)findViewById(R.id.clock_image_1);
        mClockImageView2 = (ClockImageView)findViewById(R.id.clock_image_2);
        mClockImageView3 = (ClockImageView)findViewById(R.id.clock_image_3);
        mClockImageView4 = (ClockImageView)findViewById(R.id.clock_image_4);
        mClockImageView5 = (ClockImageView)findViewById(R.id.clock_image_5);

//        mLunarDateView = ( TextView ) findViewById(R.id.lunar);
//        mLunarDateView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);

        mWeekView = ( TextView ) findViewById(R.id.week);
        mWallPaperBg = ( LinearLayout ) findViewById(R.id.lock_screen_bg_view);
        mWallPaper = ( ImageView ) findViewById(R.id.lock_screen_bg_wallpaper);
        mWallPaperBg.setVisibility(View.VISIBLE);
        LockScreenBgUtils.getInstance().setViewBg(mWallPaper);
        
		// Aurora liugj 2014-06-16 add for MusicLockScreen start
        mMusicLockLayout = (RelativeLayout) findViewById(R.id.music_lockscreen_view);
            
        mNormalLockLayout = (LinearLayout) findViewById(R.id.normal_lockscreen_layout);
    	// Aurora liugj 2014-06-16 add for MusicLockScreen end
		
        //Aurora <zhang_xin> <2014-3-5> modify for battery charge begin
        mChartAnimationView = ( ChartAnimationView ) findViewById(R.id.chartAnimationView);
        mChartLayoutBg = ( ImageView ) findViewById(R.id.chart_layout_bg);
        //Aurora <zhang_xin> <2014-3-5> modify for battery charge end
        mChartFinishView = (TextView) findViewById(R.id.chart_finish_text);
		 // Aurora liugj 2014-09-12 added for 防误触 start
		  // Aurora liugj 2014-09-22 deleted for 防误触 start
        //mPalmRejectionView = (TextView) findViewById(R.id.palm_rejection_text);
		  // Aurora liugj 2014-09-22 deleted for 防误触 end
		 // Aurora liugj 2014-09-12 added for 防误触 end
		 
		 try {
        	Context changeContext = mContext.createPackageContext("com.aurora.change", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = changeContext.getSharedPreferences("aurora_change", Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
            String defaultGroup = Build.MODEL.equals("IUNI i1") ? AuroraKeyguardHostView.BLACKSTAYLE_LOCKPAPER_GROUP_2 : AuroraKeyguardHostView.DEFAULT_LOCKPAPER_GROUP;
            String currentGroup = sp.getString("current_lockpaper_group", defaultGroup);
            Log.d("liugj3", TAG+"-----onFinishInflate---currentGroup="+currentGroup);
            
            //shigq add start
            String currentGroupTimeBlack = sp.getString("current_lockpaper_group_time_black", null);
            Log.d("Wallpaper_DEBUG", "currentGroup----------= "+currentGroup);
            Log.d("Wallpaper_DEBUG", "current_lockpaper_group_time_black----------= "+currentGroupTimeBlack);
            if (currentGroupTimeBlack != null) {
            	if ("false".equals(currentGroupTimeBlack)) {
                	mDateMonthView.setTextColor(Color.WHITE);
                	mDateDivView.setTextColor(Color.WHITE);
                	mDateDayView.setTextColor(Color.WHITE);
    				mWeekView.setTextColor(Color.WHITE);
    				mAlarmStatusView.setTextColor(Color.WHITE);
    				mChartFinishView.setTextColor(Color.WHITE);
    				mClockImageView1.setBlackStyle(false);
    				mClockImageView2.setBlackStyle(false);
    				mClockImageView3.setBlackStyle(false);
    				mClockImageView4.setBlackStyle(false);
    				mClockImageView5.setBlackStyle(false);
    				
    			} else if ("true".equals(currentGroupTimeBlack)) {
    				mDateMonthView.setTextColor(Color.BLACK);
                	mDateDivView.setTextColor(Color.BLACK);
                	mDateDayView.setTextColor(Color.BLACK);
    				mWeekView.setTextColor(Color.BLACK);
    				mAlarmStatusView.setTextColor(Color.BLACK);
    				mChartFinishView.setTextColor(mContext.getResources().getColor(R.color.chart_finish_black_color));
    				mClockImageView1.setBlackStyle(true);
    				mClockImageView2.setBlackStyle(true);
    				mClockImageView3.setBlackStyle(true);
    				mClockImageView4.setBlackStyle(true);
    				mClockImageView5.setBlackStyle(true);
    			}
            	
			} else 
            //shigq add end
            
            if (currentGroup.equals(AuroraKeyguardHostView.BLACKSTAYLE_LOCKPAPER_GROUP_1) || currentGroup.equals(AuroraKeyguardHostView.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
            	mDateMonthView.setTextColor(Color.BLACK);
            	mDateDivView.setTextColor(Color.BLACK);
            	mDateDayView.setTextColor(Color.BLACK);
				mWeekView.setTextColor(Color.BLACK);
				mAlarmStatusView.setTextColor(Color.BLACK);
				mChartFinishView.setTextColor(mContext.getResources().getColor(R.color.chart_finish_black_color));
				mClockImageView1.setBlackStyle(true);
				mClockImageView2.setBlackStyle(true);
				mClockImageView3.setBlackStyle(true);
				mClockImageView4.setBlackStyle(true);
				mClockImageView5.setBlackStyle(true);
			}else {
				mDateMonthView.setTextColor(Color.WHITE);
            	mDateDivView.setTextColor(Color.WHITE);
            	mDateDayView.setTextColor(Color.WHITE);
				mWeekView.setTextColor(Color.WHITE);
				mAlarmStatusView.setTextColor(Color.WHITE);
				mChartFinishView.setTextColor(Color.WHITE);
				mClockImageView1.setBlackStyle(false);
				mClockImageView2.setBlackStyle(false);
				mClockImageView3.setBlackStyle(false);
				mClockImageView4.setBlackStyle(false);
				mClockImageView5.setBlackStyle(false);
			}
		} catch (NameNotFoundException e) {
			// TODO: handle exception
		}

        // Use custom font in mClockView/mDateView/mWeekView
        try {
            mClockFace = Typeface.createFromFile(AURORA_CLOCK_FONT_FILE);
            mDateFace = Typeface.createFromFile(AURORA_DATE_FONT_FILE);
            mWeekFace = Typeface.createFromFile(AURORA_WEEK_FONT_FILE);
            //mClockView.setTypeface(mClockFace);
            mDateMonthView.setTypeface(mDateFace);
            mDateDivView.setTypeface(mDateFace);
            mDateDayView.setTypeface(mDateFace);
            mWeekView.setTypeface(mWeekFace);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDateDivView.setText("-");

        // Required to get Marquee to work.
        final View marqueeViews[] = {mDateMonthView, mDateDayView, mWeekView, mAlarmStatusView};
        for (int i = 0; i < marqueeViews.length; i++) {
            View v = marqueeViews[i];
            if (v == null) {
                throw new RuntimeException("Can't find widget at index " + i);
            }
            v.setSelected(true);
        }
        refresh();
        
        //refreshBattery();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mInfoCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(mContext).removeCallback(mInfoCallback);
    }
	
	public void refreshView() {
    	Log.d("liugj2", "========refreshView=========");
        
        	if (isPlayingMusic || isMusicShow) {
	        	if (mMusicLockLayout.getChildCount() == 0) {
	        		View musicLockView = getMusicLockView();
	        		Log.d("liugj", (musicLockView != null) + "====refreshView===ChildCount==0");
	                if (musicLockView != null) {
	                	mMusicLockLayout.addView(musicLockView);
	                	mMusicLockLayout.setVisibility(View.VISIBLE);
	        			mNormalLockLayout.setVisibility(View.GONE);
	        			isMusicShow = true;
	        		}
				}
			}
	}

    protected void refresh() {
        //mClockView.updateTime();
        mClockImageView1.updateTime();
        mClockImageView2.updateTime();
        mClockImageView3.updateTime();
        mClockImageView4.updateTime();
        mClockImageView5.updateTime();
		
        refreshDate();
//        refreshLunarDate();
//        refreshAlarmStatus(); // might as well
        refreshWeek();
    }

    void refreshAlarmStatus() {
        // Update Alarm status
        String nextAlarm = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (!TextUtils.isEmpty(nextAlarm)) {
            maybeSetUpperCaseText(mAlarmStatusView, nextAlarm);
            mAlarmStatusView.setCompoundDrawablesWithIntrinsicBounds(ALARM_ICON, 0, 0, 0);
            mAlarmStatusView.setVisibility(View.GONE);
        } else {
            mAlarmStatusView.setVisibility(View.GONE);
        }
    }

    void refreshDate() {
//        maybeSetUpperCaseText(mDateView, DateFormat.format(mDateFormatString, new Date()));
        maybeSetUpperCaseText(mDateMonthView, mDateDayView, DateFormat.format(DATE_MONTH_FORMAT, new Date()),
                DateFormat.format(DATE_DAY_FORMAT, new Date()));
    }

    void refreshLunarDate() {
        setLunarDate(mLunarDateView);
    }

    void refreshWeek() {
        setWeek(mWeekView);
    }

    private void maybeSetUpperCaseText(TextView textView, CharSequence text) {
        if (USE_UPPER_CASE) {
            textView.setText(text != null ? text.toString().toUpperCase() : null);
        } else {
            textView.setText(text);
        }
    }

    private void maybeSetUpperCaseText(TextView monthView, TextView dayView, CharSequence monthText,
            CharSequence dayText) {
        if (USE_UPPER_CASE) {
            monthView.setText(monthText != null ? monthText.toString().toUpperCase() : null);
            dayView.setText(dayText != null ? dayText.toString().toUpperCase() : null);
        } else {
            monthView.setText(monthText);
            dayView.setText(dayText);
        }
    }

    private void setLunarDate(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Time tCalendar = null;
        if (tCalendar == null) {
            tCalendar = new Time(calendar.getTimeZone().getID());
        }
        long now = System.currentTimeMillis();
        tCalendar.set(now);
        LunarUtils lunar = new LunarUtils(mContext);
        lunar.SetSolarDate(tCalendar);

        String chinaDate = lunar.GetLunarNYRString();
        String shortDate = lunar.GetLunarDateString();
        shortDate = shortDate.trim();
        String nlString = "";

        if (chinaDate != null) {
            if (chinaDate.substring(10, 11).equalsIgnoreCase(" ")) {
                nlString = chinaDate.substring(3, 10);
            } else {
                nlString = chinaDate.substring(3);
                if (shortDate != null) {
                    if (chinaDate.substring(7).contains(shortDate)) {
                        nlString = shortDate;
                    }
                }
            }
        }
        textView.setText(nlString);
    }

    private void setWeek(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String[] dayOfWeek = mContext.getResources().getStringArray(R.array.day_of_week);
        if (week > 0 && dayOfWeek != null) {
            textView.setText(dayOfWeek[week - 1]);
        }
    }

    private KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {

        @Override
        public void onTimeChanged() {
        	if (!isMusicShow) {
        		refresh();
			}
        }

        @Override
        public void onKeyguardVisibilityChanged(boolean showing) {
            if (showing) {
                AuroraLog.d(TAG, "refresh statusview showing:" + showing);
                if (!isMusicShow) {
                	refresh();
                }
            }
        };

        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
            mShowingBatteryInfo = status.isPluggedIn();
            mBatteryLevel = status.level;
            mIsDone = status.getDone();
        	if (isResumed) {
                refreshBattery();
			}
        };
    };

    // Aurora <zhang_xin> <2014-3-5> modify for battery charge begin
    @SuppressLint("NewApi")
	public void refreshBattery(){
    	if (mChartAnimationView == null) {
            return;
        }
        Log.d("liugj", mIsDone+"=refreshBattery==mShowingBatteryInfo=" + mShowingBatteryInfo + ",mBatteryLevel=" + mBatteryLevel);
        if (mShowingBatteryInfo && !isMusicShow) {
        	resetTransition();
    		if (mTransition == null) {
            	mTransition = new LayoutTransition();
			}
        	if (mBatteryLevel == 100) {
        		if (mIsDone) {
					chartAnimationView();
					mDelayed = true;
					mIsDone = false;
				}
        		if (mChartAnimationView.getVisibility() == View.VISIBLE && mDelayed) {
        			mDelayed = false;
        			postDelayed(new Runnable() {
						
						@Override
						public void run() {
							mChartAnimationView.onStop();
			                mChartAnimationView.setVisibility(View.GONE);
			                ObjectAnimator oa1 = ObjectAnimator.ofFloat(mChartAnimationView, "alpha", 1f, 0f);
			            	oa1.setDuration(600);
			            	mTransition.setAnimator(LayoutTransition.DISAPPEARING, oa1);
			            	mChartLayoutBg.setVisibility(View.GONE);
						}
					}, 1000);
				}
                mChartFinishView.setVisibility(View.VISIBLE);
                ObjectAnimator oa2 = ObjectAnimator.ofFloat(mChartFinishView, "alpha", 0f, 1f);
            	oa2.setDuration(300);
            	mTransition.setAnimator(LayoutTransition.APPEARING, oa2);
                return;
			}
            chartAnimationView();
        } else {
        	Log.d("liugj2", mChartFinishView.isShown()+"=refreshBattery---->"+mChartAnimationView.getVisibility());
        	if (mChartAnimationView.getVisibility() ==  View.VISIBLE) {
        		mChartAnimationView.onStop();
        		mChartAnimationView.setVisibility(View.GONE);
			}
    		
            mChartLayoutBg.setVisibility(View.GONE);
			
    		if (mChartFinishView.isShown()) {
    			mChartFinishView.setVisibility(View.GONE);
			}
        }
    }
    // Aurora <zhang_xin> <2014-3-5> modify for battery charge end

    @SuppressLint("NewApi")
	private void chartAnimationView() {
    	// Aurora <liugj> <2014-7-9> modify for bug-6331 begin 
        if (mChartAnimationView.getVisibility() !=  View.VISIBLE) {
        	mChartLayoutBg.setVisibility(View.VISIBLE);
        	ObjectAnimator oa = ObjectAnimator.ofFloat(mChartLayoutBg, "alpha", 0f, 1f);
        	oa.setDuration(600);
        	mTransition.setAnimator(LayoutTransition.APPEARING, oa);
        	mChartAnimationView.setVisibility(View.VISIBLE);
            mChartFinishView.setVisibility(View.GONE);
        }
		// Aurora <liugj> <2014-7-9> modify for bug-6331 end
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                mChartAnimationView.powerChanges(mBatteryLevel);
            }
        };
        new Thread() {
            public void run() {
                handler.sendEmptyMessage(0);
            };
        }.start();
	}
    
    @SuppressLint("NewApi")
	private void resetTransition() {
    	mTransition = new LayoutTransition();
		this.setLayoutTransition(mTransition);
	}
    
    public void onResume(){
//        mChartAnimationView.onRestart();
		isResumed = true;
        Log.d(TAG, "statusView=onResume");
		refreshView();
        refreshBattery();
    }

    public void onPause() {
//        mChartAnimationView.onStop();
        Log.d(TAG, "statusView=onPause");
        if (mScroller != null) {
            mScroller.abortAnimation();
            scrollTo(0, 0);
            //mPager.setCurrentItem(0);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            /*if (!isCameraScrolling()) {*/
                scrollTo(0, mScroller.getCurrY());
                postInvalidate();
            //}
        }
    }
    
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.d(TAG, "StatusView=====dispatchTouchEvent====="+ev.getAction());
		return super.dispatchTouchEvent(ev);
	}

	// Aurora liugj 2014-07-01 modified for bug-5994 start
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.d(TAG, "StatusView=====onInterceptTouchEvent====="+event.getAction());
		int action = event.getAction();
		float x = event.getX();
        float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
            if (mTraker == null) {
                mTraker = VelocityTracker.obtain();
                mTraker.addMovement(event);
            }

            mLastY = y;
            mLastDownX = event.getX();
            mLastDownY = event.getY();
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
                return true;
            }
            break;
            
        case MotionEvent.ACTION_MOVE:

            //mTotalDx = x - mLastDownX;
            mTotalDy = y - mLastDownY;

            //final int diffX = ( int ) Math.abs(mTotalDx);
            final int diffY = ( int ) Math.abs(mTotalDy);

            if (diffY > 20) {
            	//handleActionUnlockMove(y);
                return true;
			}
            break;
         default:
        	 break;
			
		}
		return false;
	}
	// Aurora liugj 2014-07-01 modified for bug-5994 end

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "StatusView=====onTouchEvent====="+event.getAction());
		int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
//        logd("action=" + action + ",x=" + x + ",y=" + y);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mTraker == null) {
                    mTraker = VelocityTracker.obtain();
                    mTraker.addMovement(event);
                }

                mLastY = y;
                mLastDownX = event.getX();
                mLastDownY = event.getY();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mCallback.userActivity(0);
                break;
            case MotionEvent.ACTION_UP:
            	if (mTraker != null) {
            		mTraker.computeCurrentVelocity(1000);
				}
                final int diffUpX = (int) Math.abs(x - mLastDownX);
                final int diffUpY = (int) Math.abs(y - mLastDownY);
                if (LockScreenUtils.getInstance(getContext()).isSecure()) {
                    post(mUnLockRunnable);
                } else {
                    if (((diffUpY >= diffUpX && (y- mLastDownY) <= 0) || getScrollY() > 0) /*&& !isCameraScrolling()*/) {
                    	Log.d(TAG, "StatusView=====onTouchEvent=====handleActionUnlockUp");
                    	handleActionUnlockUp(mTraker);
                    }
                }
                mState = STATE_REST;
                mLastY = UNVAIABLE_Y;
                mOnMove = false;
                mOnYMove = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mLastY == UNVAIABLE_Y || LockScreenUtils.getInstance(getContext()).isSecure()) {
//                if (mLastY == UNVAIABLE_Y)
                    return true;
                }
                if (mTraker != null) {
                    mTraker.addMovement(event);
                }

                mTotalDx = x - mLastDownX;
                mTotalDy = y - mLastDownY;

                final int diffX = ( int ) Math.abs(mTotalDx);
                final int diffY = ( int ) Math.abs(mTotalDy);
//                logd("move,y=" + y + ",mTotalDx=" + mTotalDx+ ",mTotalDy=" + mTotalDy);

                /*if (diffX > 22 || diffY > 22) {
                    if (diffX <= diffY && !isCameraScrolling()) {
                        mState = STATE_UNLOCK;
                    } else if (diffX > diffY && !isUnlockScrolling()) {
                        if ((diffY / diffX) > Math.tan(30*Math.PI/180)) {
                            return true;
                        }
                        mState = STATE_CAMERA;
                    }
                } else {
                    if (diffX > diffY && !isUnlockScrolling()) {
                        if ((diffY / diffX) > Math.tan(30*Math.PI/180)) {
                            return true;
                        }
                    }
                }*/
                double angle = 0;
                if (diffX > 0) {
                    angle = (double)diffY / diffX;
                }
                if ((mOnYMove || (angle >= Math.tan(30 * Math.PI / 180)) || diffX == 0) /*&& !isCameraScrolling()*/) {
                    mState = STATE_UNLOCK;
                    mOnYMove = true;
                } else if (((angle < Math.tan(30 * Math.PI / 180) || mOnMove)) && !isUnlockScrolling()) {
                    mState = STATE_CAMERA;
                    mOnMove = true;
                    return false;
                } else {
                    return true;
                }
                if (mState == STATE_UNLOCK) {
                    handleActionUnlockMove(y);
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mTraker != null) {
                    mTraker.recycle();
                    mTraker = null;
                }
                if (mScroller != null) {
                    mScroller.abortAnimation();
                    scrollTo(0, 0);
                    //mPager.setCurrentItem(0);
                }
                mState = STATE_REST;
                mLastY = UNVAIABLE_Y;
                mOnMove = false;
                mOnYMove = false;
                break;
            default:
                break;
        }
        return /*super.onTouchEvent(event)*/true;
	}
	
	public boolean isUnlockScrolling() {
        float scrollY = getScrollY();
        int displayH = getContext().getResources().getDisplayMetrics().heightPixels;
        boolean b = (( int ) scrollY % displayH) == 0;
        return !b;
    }
	
	Runnable mCameraRunnable = new Runnable() {

        @Override
        public void run() {
            post(mUnLockRunnable);
            try {
                Intent intent = new Intent();
                if (LockScreenUtils.getInstance(getContext()).isSecure()) {
                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                } else {
                    intent.setClassName("com.android.camera", "com.android.camera.CameraActivity");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getContext().startActivity(intent);
            } catch (Exception e) {
                try {
                    Intent intent = new Intent();
                    if (LockScreenUtils.getInstance(getContext()).isSecure()) {
                        intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                    } else {
                        intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };
    
    public void postCameraRunnable() {
    	post(mCameraRunnable);
    }
    
    public void postUnLockRunnable() {
    	post(mUnLockRunnable);
    }
    
	 // Aurora liugj 2014-09-18 modified for 防误触 start
	 // Aurora liugj 2014-09-22 deleted for 防误触 start
    /*public void setSensorMessage(boolean proFlag, boolean lightFlag) {
		if (proFlag && lightFlag) {
			mPalmRejectionView.setVisibility(View.VISIBLE);
		}else {
			mPalmRejectionView.setVisibility(View.GONE);
		}
	}*/
	// Aurora liugj 2014-09-22 deleted for 防误触 end
	 // Aurora liugj 2014-09-18 modified for 防误触 end
	 
	// Aurora liugj 2014-06-16 add for MusicLockScreen start
    private View getMusicLockView() {
    	View musicView = null;
    	try {
    		String mPackageName = "com.android.music";
            String mClassName = "com.android.auroramusic.ui.lock.AuroraMusicLockView";
            Context context = getContext().createPackageContext(mPackageName, Context.CONTEXT_INCLUDE_CODE
                    | Context.CONTEXT_IGNORE_SECURITY);
            ClassLoader pluginLoader = context.getClassLoader();
            Class<?> pluginClass = pluginLoader.loadClass(mClassName);
            Constructor ct = pluginClass.getConstructor(Context.class);
            Object object = ct.newInstance(context);
            musicView = (View) object;
            //musicView = ( View ) pluginClass.getMethod("getAuroraLockView", Context.class).invoke(object, context);
    	}catch (Exception e) {
    		e.printStackTrace();
    		Log.e("liugj", "===getMusicLockView==="+e);
    	}
    	return musicView;
	}
	// Aurora liugj 2014-06-16 add for MusicLockScreen end
}
