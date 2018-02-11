// Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin
package com.gionee.almanac;

import com.android.calendar.R;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.day.DayUtils;
import com.gionee.calendar.day.MessageDispose;
import com.gionee.calendar.month.GNMonthView;
import com.gionee.calendar.view.GNCustomTimeDialog;
import static com.gionee.almanac.GNAlmanacActivity.EVENT_QUERY_ALMANAC_INFO;
import static com.gionee.almanac.GNAlmanacActivity.EVENT_EXTRA_NO_MOVE;
import static com.gionee.almanac.GNAlmanacActivity.EVENT_EXTRA_GOTO_PREV;
import static com.gionee.almanac.GNAlmanacActivity.EVENT_EXTRA_GOTO_NEXT;
import static com.gionee.almanac.GNAlmanacActivity.EVENT_CHANGE_QUERY_TIME;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDatePickerDialog;
import aurora.widget.AuroraDatePicker;

public class GNAlmanacView extends ViewGroup {
	private static final String LOG_TAG = "GNAlmanacView";
	
	private static final int FIVE_ELEMENTS_METAL = 0;
	private static final int FIVE_ELEMENTS_WOOD = 1;
	private static final int FIVE_ELEMENTS_WATER = 2;
	private static final int FIVE_ELEMENTS_FIRE = 3;
	private static final int FIVE_ELEMENTS_SOIL = 4;

	private static final int CHINESE_HOROSCOPE_MOUSE = 0;
	private static final int CHINESE_HOROSCOPE_CATTLE = 1;
	private static final int CHINESE_HOROSCOPE_TIGER = 2;
	private static final int CHINESE_HOROSCOPE_RABBIT = 3;
	private static final int CHINESE_HOROSCOPE_DRAGON = 4;
	private static final int CHINESE_HOROSCOPE_SNAKE = 5;
	private static final int CHINESE_HOROSCOPE_HORSE = 6;
	private static final int CHINESE_HOROSCOPE_GOAT = 7;
	private static final int CHINESE_HOROSCOPE_MONKEY = 8;
	private static final int CHINESE_HOROSCOPE_CHOOK = 9;
	private static final int CHINESE_HOROSCOPE_DOG = 10;
	private static final int CHINESE_HOROSCOPE_PIG = 11;

	private int mColorMetal = 0;
	private int mColorWood = 0;
	private int mColorWater = 0;
	private int mColorFire = 0;
	private int mColorSoil = 0;
	
	private String mStrYear = null;
	private String mStrMonth = null;
	private String mStrDay = null;
	
	private String mStrTodayBadDefault = null;
	private String mStrQueryDateInvalid = null;

	// root view
	private ViewGroup mRootView = null;
	private ViewGroup mLunarDateField = null;
	
	// date field
	private ImageView mGotoPrev = null;
	private ImageView mGotoNext = null;
	private TextView mLunarDate = null;
	private TextView mGregDate = null;
	private ImageView mTimePickIndicateLeft;
	private ImageView mTimePickIndicateRight;
	private TextView mWeekDay = null;
	private TextView mChineseEra = null;

	// Chinese horoscope & Good 'n' Bad
	private ViewGroup mAlmanacInfoField = null;
	private ImageView mIconChineseHoroscope = null;
	private ImageView mIconGood = null;
	private ImageView mIconBad = null;
	private TextView mContentGood = null;
	private TextView mContentBad = null;
	private View mSeparatorGood = null;
	private View mSeparatorBad = null;

	// more info field
	private TextView mTitleChong = null;
	private TextView mTitleBirthGod = null;
	private TextView mTitleFiveElements = null;
	private TextView mTitlePengzu = null;
	private TextView mTitleLuckyFairy = null;
	private TextView mTitleEvilSpirit = null;

	private TextView mContentChong = null;
	private TextView mContentBirthGod = null;
	private TextView mContentFiveElements = null;
	private TextView mContentPengzu = null;
	private TextView mContentLuckyFairy = null;
	private TextView mContentEvilSpirit = null;

	// the first index is five elements and the second index
	// is chinese horoscope
	private int mChineseHoroscopeIconId[][] = null;

	private int mFiveElement = -1;
	private int mChineseHoroscope = -1;
	
	private Context mContext = null;
	public GNAlmanacView(Context context) {
		super(context);
		
		initView(context);
	}
	
	public GNAlmanacView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView(context);
	}
	
	public GNAlmanacView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initView(context);
	}
	
	private void initView(Context context) {
		mContext = context;
		
		loadLayoutElements();
		loadRes();
		
		mGestureDetector = new GestureDetector(mContext, new AlmanacGestureDetector());
		
		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(mRootView, params);
	}

	@Override
	protected void onLayout(boolean arg0, int l, int t, int r, int b) {
		if(mRootView != null) {
			mRootView.layout(l, t, r, b);
		}
	}
	
	@Override
	protected void onMeasure(int measuredWidth, int measuredHeight) {
		if(mRootView != null) {
			mRootView.measure(measuredWidth, measuredHeight);
		}
		
		this.setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private void loadLayoutElements() {
		Log.v("Calendar","GNAlmancActivity---GNAlmanacView---loadLayoutElements");
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int fontScale = 0/*Settings.System.getInt(mContext.getContentResolver(), Settings.System.GN_FONT_SIZE, 0)*/;
		if(fontScale == 0){
			mRootView = (ViewGroup) inflater.inflate(R.layout.gn_almanac_view, null);
		}else{
			mRootView = (ViewGroup) inflater.inflate(R.layout.gn_almanac_view_for_big_font, null);
		}
		Log.v("Calendar","GNAlmancActivity---GNAlmanacView---fontScale == " + fontScale);
		mLunarDateField = (ViewGroup) mRootView.findViewById(R.id.lunar_date_field);
		// mLunarDateField.setOnClickListener(mLunarFieldOnClickListener);
		mLunarDateField.setOnTouchListener(mLunarFieldOnTouchListener);
		
		mGotoPrev = (ImageView) mRootView.findViewById(R.id.lunar_date_previous);
		mGotoNext = (ImageView) mRootView.findViewById(R.id.lunar_date_next);
		mLunarDate = (TextView) mRootView.findViewById(R.id.lunar_date);
		// mLunarDate.setOnClickListener(mLunarFieldOnClickListener);
		mLunarDate.setOnTouchListener(mLunarFieldOnTouchListener);
		mGregDate = (TextView) mRootView.findViewById(R.id.greg_date);
		mTimePickIndicateLeft = (ImageView) mRootView.findViewById(R.id.timepick_indicate_left);
		mTimePickIndicateRight = (ImageView) mRootView.findViewById(R.id.timepick_indicate_Right);
		mWeekDay = (TextView) mRootView.findViewById(R.id.week_day);
		mChineseEra = (TextView) mRootView.findViewById(R.id.chinese_era);

		mAlmanacInfoField = (ViewGroup) mRootView.findViewById(R.id.almanac_info_field);
		mIconChineseHoroscope = (ImageView) mRootView.findViewById(R.id.chinese_horoscope);
		mIconGood = (ImageView) mRootView.findViewById(R.id.icon_good);
		mIconBad = (ImageView) mRootView.findViewById(R.id.icon_bad);
		mContentGood = (TextView) mRootView.findViewById(R.id.affair_good);
		mContentBad = (TextView) mRootView.findViewById(R.id.affair_bad);
		mSeparatorGood = mRootView.findViewById(R.id.separator_good);
		mSeparatorBad = mRootView.findViewById(R.id.separator_bad);

		mTitleChong = (TextView) mRootView.findViewById(R.id.more_info_title_chong);
		mTitleBirthGod = (TextView) mRootView.findViewById(R.id.more_info_title_birth_god);
		mTitleFiveElements = (TextView) mRootView.findViewById(R.id.more_info_title_five_elements);
		mTitlePengzu = (TextView) mRootView.findViewById(R.id.more_info_title_pengzu);
		mTitleLuckyFairy = (TextView) mRootView.findViewById(R.id.more_info_title_lucky_fairy);
		mTitleEvilSpirit = (TextView) mRootView.findViewById(R.id.more_info_title_evil_spirit);

		mContentChong = (TextView) mRootView.findViewById(R.id.more_info_content_chong);
		mContentBirthGod = (TextView) mRootView.findViewById(R.id.more_info_content_birth_god);
		mContentFiveElements = (TextView) mRootView.findViewById(R.id.more_info_content_five_elements);
		mContentPengzu = (TextView) mRootView.findViewById(R.id.more_info_content_pengzu);
		mContentLuckyFairy = (TextView) mRootView.findViewById(R.id.more_info_content_lucky_fairy);
		mContentEvilSpirit = (TextView) mRootView.findViewById(R.id.more_info_content_evil_spirit);
	}

	private void loadRes() {
		Resources res = mContext.getResources();

		mColorMetal = res.getColor(R.color.gn_almanac_metal);
		mColorWood = res.getColor(R.color.gn_almanac_wood);
		mColorWater = res.getColor(R.color.gn_almanac_water);
		mColorFire = res.getColor(R.color.gn_almanac_fire);
		mColorSoil = res.getColor(R.color.gn_almanac_soil);
		
		mStrYear = res.getString(R.string.almanac_greg_date_year);
		mStrMonth = res.getString(R.string.almanac_greg_date_month);
		mStrDay = res.getString(R.string.almanac_greg_date_day);
		
		mStrTodayBadDefault = res.getString(R.string.almanac_today_bad_default);
		mStrQueryDateInvalid = res.getString(R.string.almanac_query_date_invalid);
		
		this.loadResMessageCode(res);

		// 5: Five Elements 12: Chinese Era
		mChineseHoroscopeIconId = new int[5][12];
		// metal
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_MOUSE] = R.drawable.gn_almanac_shu_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_CATTLE] = R.drawable.gn_almanac_niu_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_TIGER] = R.drawable.gn_almanac_hu_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_RABBIT] = R.drawable.gn_almanac_tu_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_DRAGON] = R.drawable.gn_almanac_long_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_SNAKE] = R.drawable.gn_almanac_she_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_HORSE] = R.drawable.gn_almanac_ma_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_GOAT] = R.drawable.gn_almanac_yang_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_MONKEY] = R.drawable.gn_almanac_hou_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_CHOOK] = R.drawable.gn_almanac_ji_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_DOG] = R.drawable.gn_almanac_gou_jin;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_METAL][CHINESE_HOROSCOPE_PIG] = R.drawable.gn_almanac_zhu_jin;
		// wood
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_MOUSE] = R.drawable.gn_almanac_shu_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_CATTLE] = R.drawable.gn_almanac_niu_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_TIGER] = R.drawable.gn_almanac_hu_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_RABBIT] = R.drawable.gn_almanac_tu_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_DRAGON] = R.drawable.gn_almanac_long_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_SNAKE] = R.drawable.gn_almanac_she_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_HORSE] = R.drawable.gn_almanac_ma_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_GOAT] = R.drawable.gn_almanac_yang_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_MONKEY] = R.drawable.gn_almanac_hou_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_CHOOK] = R.drawable.gn_almanac_ji_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_DOG] = R.drawable.gn_almanac_gou_mu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WOOD][CHINESE_HOROSCOPE_PIG] = R.drawable.gn_almanac_zhu_mu;
		// water
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_MOUSE] = R.drawable.gn_almanac_shu_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_CATTLE] = R.drawable.gn_almanac_niu_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_TIGER] = R.drawable.gn_almanac_hu_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_RABBIT] = R.drawable.gn_almanac_tu_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_DRAGON] = R.drawable.gn_almanac_long_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_SNAKE] = R.drawable.gn_almanac_she_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_HORSE] = R.drawable.gn_almanac_ma_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_GOAT] = R.drawable.gn_almanac_yang_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_MONKEY] = R.drawable.gn_almanac_hou_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_CHOOK] = R.drawable.gn_almanac_ji_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_DOG] = R.drawable.gn_almanac_gou_shui;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_WATER][CHINESE_HOROSCOPE_PIG] = R.drawable.gn_almanac_zhu_shui;
		// fire
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_MOUSE] = R.drawable.gn_almanac_shu_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_CATTLE] = R.drawable.gn_almanac_niu_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_TIGER] = R.drawable.gn_almanac_hu_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_RABBIT] = R.drawable.gn_almanac_tu_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_DRAGON] = R.drawable.gn_almanac_long_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_SNAKE] = R.drawable.gn_almanac_she_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_HORSE] = R.drawable.gn_almanac_ma_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_GOAT] = R.drawable.gn_almanac_yang_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_MONKEY] = R.drawable.gn_almanac_hou_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_CHOOK] = R.drawable.gn_almanac_ji_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_DOG] = R.drawable.gn_almanac_gou_huo;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_FIRE][CHINESE_HOROSCOPE_PIG] = R.drawable.gn_almanac_zhu_huo;
		// soil
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_MOUSE] = R.drawable.gn_almanac_shu_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_CATTLE] = R.drawable.gn_almanac_niu_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_TIGER] = R.drawable.gn_almanac_hu_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_RABBIT] = R.drawable.gn_almanac_tu_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_DRAGON] = R.drawable.gn_almanac_long_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_SNAKE] = R.drawable.gn_almanac_she_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_HORSE] = R.drawable.gn_almanac_ma_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_GOAT] = R.drawable.gn_almanac_yang_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_MONKEY] = R.drawable.gn_almanac_hou_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_CHOOK] = R.drawable.gn_almanac_ji_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_DOG] = R.drawable.gn_almanac_gou_tu;
		mChineseHoroscopeIconId[FIVE_ELEMENTS_SOIL][CHINESE_HOROSCOPE_PIG] = R.drawable.gn_almanac_zhu_tu;
	}
	


	private void changeThemeByFiveElements(int fiveElement) {
		changeTextColorByFiveElements(fiveElement);
		changeIconColorByFiveElements(fiveElement);
	}

	private void changeTextColorByFiveElements(int fiveElement) {
		switch (fiveElement) {
			case FIVE_ELEMENTS_METAL:
				this.setTextColor(mColorMetal);
				break;
			case FIVE_ELEMENTS_WOOD:
				this.setTextColor(mColorWood);
				break;
			case FIVE_ELEMENTS_WATER:
				this.setTextColor(mColorWater);
				break;
			case FIVE_ELEMENTS_FIRE:
				this.setTextColor(mColorFire);
				break;
			case FIVE_ELEMENTS_SOIL:
				this.setTextColor(mColorSoil);
				break;
			default:
				this.setTextColor(mColorWater);
				break;
		}
	}

	private void changeIconColorByFiveElements(int fiveElements) {
		switch (fiveElements) {
			case FIVE_ELEMENTS_METAL:
				mGotoPrev.setImageResource(R.drawable.gn_almanac_last_jin);
				mGotoNext.setImageResource(R.drawable.gn_almanac_next_jin);
				mIconGood.setImageResource(R.drawable.gn_almanac_fitting_jin);
				mIconBad.setImageResource(R.drawable.gn_almanac_avoid_jin);
				mTimePickIndicateLeft.setImageResource(R.drawable.last_day_jin);
				mTimePickIndicateRight.setImageResource(R.drawable.next_day_jin);
				mAlmanacInfoField.setBackgroundResource(R.drawable.gn_almanac_bg_kuang_jin);
				changeChineseHoroscopeIcon(FIVE_ELEMENTS_METAL, mChineseHoroscope);
				break;
			case FIVE_ELEMENTS_WOOD:
				mGotoPrev.setImageResource(R.drawable.gn_almanac_last_mu);
				mGotoNext.setImageResource(R.drawable.gn_almanac_next_mu);
				mIconGood.setImageResource(R.drawable.gn_almanac_fitting_mu);
				mIconBad.setImageResource(R.drawable.gn_almanac_avoid_mu);
				mTimePickIndicateLeft.setImageResource(R.drawable.last_day_mu);
				mTimePickIndicateRight.setImageResource(R.drawable.next_day_mu);
				mAlmanacInfoField.setBackgroundResource(R.drawable.gn_almanac_bg_kuang_mu);
				changeChineseHoroscopeIcon(FIVE_ELEMENTS_WOOD, mChineseHoroscope);
				break;
			case FIVE_ELEMENTS_WATER:
				mGotoPrev.setImageResource(R.drawable.gn_almanac_last_shui);
				mGotoNext.setImageResource(R.drawable.gn_almanac_next_shui);
				mIconGood.setImageResource(R.drawable.gn_almanac_fitting_shui);
				mIconBad.setImageResource(R.drawable.gn_almanac_avoid_shui);
				mTimePickIndicateLeft.setImageResource(R.drawable.last_day_shui);
				mTimePickIndicateRight.setImageResource(R.drawable.next_day_shui);
				mAlmanacInfoField.setBackgroundResource(R.drawable.gn_almanac_bg_kuang_shui);
				changeChineseHoroscopeIcon(FIVE_ELEMENTS_WATER, mChineseHoroscope);
				break;
			case FIVE_ELEMENTS_FIRE:
				mGotoPrev.setImageResource(R.drawable.gn_almanac_last_huo);
				mGotoNext.setImageResource(R.drawable.gn_almanac_next_huo);
				mIconGood.setImageResource(R.drawable.gn_almanac_fitting_huo);
				mIconBad.setImageResource(R.drawable.gn_almanac_avoid_huo);
				mTimePickIndicateLeft.setImageResource(R.drawable.last_day_huo);
				mTimePickIndicateRight.setImageResource(R.drawable.next_day_huo);
				mAlmanacInfoField.setBackgroundResource(R.drawable.gn_almanac_bg_kuang_huo);
				changeChineseHoroscopeIcon(FIVE_ELEMENTS_FIRE, mChineseHoroscope);
				break;
			case FIVE_ELEMENTS_SOIL:
				mGotoPrev.setImageResource(R.drawable.gn_almanac_last_tu);
				mGotoNext.setImageResource(R.drawable.gn_almanac_next_tu);
				mIconGood.setImageResource(R.drawable.gn_almanac_fitting_tu);
				mIconBad.setImageResource(R.drawable.gn_almanac_avoid_tu);
				mTimePickIndicateLeft.setImageResource(R.drawable.last_day_tu);
				mTimePickIndicateRight.setImageResource(R.drawable.next_day_tu);
				mAlmanacInfoField.setBackgroundResource(R.drawable.gn_almanac_bg_kuang_tu);
				changeChineseHoroscopeIcon(FIVE_ELEMENTS_SOIL, mChineseHoroscope);
				break;
			default:
				mGotoPrev.setImageResource(R.drawable.gn_almanac_last_shui);
				mGotoNext.setImageResource(R.drawable.gn_almanac_next_shui);
				mIconGood.setImageResource(R.drawable.gn_almanac_fitting_shui);
				mIconBad.setImageResource(R.drawable.gn_almanac_avoid_shui);
				mTimePickIndicateLeft.setImageResource(R.drawable.last_day_shui);
				mTimePickIndicateRight.setImageResource(R.drawable.next_day_shui);
				mAlmanacInfoField.setBackgroundResource(R.drawable.gn_almanac_bg_kuang_shui);
				changeChineseHoroscopeIcon(FIVE_ELEMENTS_WATER, mChineseHoroscope);
				break;
		}
	}

	private void changeChineseHoroscopeIcon(int color, int name) {
		if (name < CHINESE_HOROSCOPE_MOUSE || name > CHINESE_HOROSCOPE_PIG
				|| color < FIVE_ELEMENTS_METAL || color > FIVE_ELEMENTS_SOIL) {
			Log.d(LOG_TAG, "changeChineseHoroscopeIcon() invalid index: "
					+ color + ", " + name);
			return;
		}
		Log.v("Calendar","GNAlmancActivity---GNAlmanacView---changeChineseHoroscopeIcon---name ==" + name);
		this.mIconChineseHoroscope
				.setImageResource(mChineseHoroscopeIconId[color][name]);
	}

	private void setTextColor(int color) {
		mLunarDate.setTextColor(color);
		mGregDate.setTextColor(color);
		mWeekDay.setTextColor(color);
		mChineseEra.setTextColor(color);

		mContentGood.setTextColor(color);
		mContentBad.setTextColor(color);
		mSeparatorGood.setBackgroundColor(color);
		mSeparatorBad.setBackgroundColor(color);
		
		// check visibility of title field
		if(mTitleChong.getVisibility() != View.VISIBLE) {
			mTitleChong.setVisibility(View.VISIBLE);
		}
		if(mTitleBirthGod.getVisibility() != View.VISIBLE) {
			mTitleBirthGod.setVisibility(View.VISIBLE);
		}
		if(mTitleFiveElements.getVisibility() != View.VISIBLE) {
			mTitleFiveElements.setVisibility(View.VISIBLE);
		}
		if(mTitlePengzu.getVisibility() != View.VISIBLE) {
			mTitlePengzu.setVisibility(View.VISIBLE);
		}
		if(mTitleLuckyFairy.getVisibility() != View.VISIBLE) {
			mTitleLuckyFairy.setVisibility(View.VISIBLE);
		}
		if(mTitleEvilSpirit.getVisibility() != View.VISIBLE) {
			mTitleEvilSpirit.setVisibility(View.VISIBLE);
		}

		mTitleChong.setTextColor(color);
		mTitleBirthGod.setTextColor(color);
		mTitleFiveElements.setTextColor(color);
		mTitlePengzu.setTextColor(color);
		mTitleLuckyFairy.setTextColor(color);
		mTitleEvilSpirit.setTextColor(color);

		mContentChong.setTextColor(color);
		mContentBirthGod.setTextColor(color);
		mContentFiveElements.setTextColor(color);
		mContentPengzu.setTextColor(color);
		mContentLuckyFairy.setTextColor(color);
		mContentEvilSpirit.setTextColor(color);
	}
	
	private void setTextContent(GNAlmanacUtils.AlmanacInfo info) {
		Log.d(LOG_TAG, "GNAlmanacActivity---setTextContent---info == " + info);
		if(info == null){ 
			initNoContentView();
			return;
		}

		// info.lunarDate will be split to 3 parts
		// 0 is lunar date, 1 is week day, 2 is horoscope
		String[] parsedLunarInfo = info.lunarDate.split(" ");
		
		// replace / with Chinese character
		String gregDate = info.gregDate;
		String separator = "/";
		Log.d(LOG_TAG, "gregDate separator char code: " + gregDate.charAt(4));
		Log.d(LOG_TAG, "separator char code: " + separator.charAt(0));
		gregDate = gregDate.replaceFirst("/", mStrYear);
		gregDate = gregDate.replaceFirst("/", mStrMonth);
		gregDate += mStrDay;
		
		mLunarDate.setText(parsedLunarInfo[0]);
		mGregDate.setText(gregDate);
		mWeekDay.setText(parsedLunarInfo[1]);
		mChineseEra.setText(info.chineseEra); // TODO: pares this string later
		
		//Gionee <jiangxiao> <2013-07-31> modify for CR00837096 begin
	    // delete messy code
		mContentGood.setText(checkMessyCode_AnDui(info.todayFitted));
		if(info.todayUnfitted == null || info.todayUnfitted.length() == 0) {
			mContentBad.setText(mStrTodayBadDefault);
		} else {
			mContentBad.setText(checkMessyCode_AnDui(info.todayUnfitted));
		}
		//Gionee <jiangxiao> <2013-07-31> modify for CR00837096 end
		
		mContentChong.setText(info.chong);
		mContentBirthGod.setText(info.birthGod);
		mContentFiveElements.setText(info.fiveElements);
		mContentPengzu.setText(formatPengzuText(info.pengzu));
		mContentLuckyFairy.setText(info.luckyFairy);
		mContentEvilSpirit.setText(info.evilSpirit);
	}
	
	private void initNoContentView(){
		// info.lunarDate will be split to 3 parts
		// 0 is lunar date, 1 is week day, 2 is horoscope
		Log.d(LOG_TAG, "GNAlmanacActivity---initNoContentView---mQueryDate == " + mQueryDate);
		String lunarDate = DayUtils.getLaunarDateForDayAndAlmanac(mContext,mQueryDate);
		mLunarDate.setText(lunarDate);
		String date = mQueryDate.year + mStrYear + (mQueryDate.month + 1) + mStrMonth + mQueryDate.MONTH_DAY + mStrDay;
		mGregDate.setText(date);
		mWeekDay.setText(DayUtils.getLunarWeek(mContext,mQueryDate.weekDay));
		String noContent = mContext.getResources().getString(R.string.gn_day_no_content);
//		mChineseEra.setText(noContent); // TODO: pares this string later
		
		//Gionee <jiangxiao> <2013-07-31> modify for CR00837096 begin
	    // delete messy code
		mContentGood.setText(noContent);
		mContentBad.setText(noContent); 		
		mContentChong.setText(noContent);
		mContentBirthGod.setText(noContent);
		mContentFiveElements.setText(noContent);
		mContentPengzu.setText(noContent);
		mContentLuckyFairy.setText(noContent);
		mContentEvilSpirit.setText(noContent);
	}
	
	private String formatPengzuText(String s) {
		int first = s.indexOf(' ');
		int last = s.lastIndexOf(' ');
		
		String result = s;
		for(int i = 0, n = (last - first); i < n; ++i) {
			result = result.replaceFirst(" ", "");
		}
		
		return result;
	}
	
	public void setAlmanacView(GNAlmanacUtils.AlmanacInfo info, int fiveElement) {
		this.mFiveElement = fiveElement;
		this.mChineseHoroscope = GNAlmanacUtils.getInstance().getChineseHoroscopeFromAlmanacInfo(info);
				
		this.changeThemeByFiveElements(fiveElement);
		this.setTextContent(info);
	}
	

	private static final int TOUCH_MODE_UNKNOWN = 0;
	private static final int TOUCH_MODE_DOWN = 0x01;
	private static final int TOUCH_MODE_HSCROLL = 0x02;
	private static final int TOUCH_MODE_VSCROLL = 0x04;

	private static final float SCROLL_SENSITIVITY = 1.7f;
	// the distance on Y-axis should bigger than this value
	private static final float MIN_VSCROLL_DISTANCE = 60.0f;
	
	private int mTouchMode = TOUCH_MODE_UNKNOWN;
	private GestureDetector mGestureDetector = null;
	
	private boolean mIsNewScrolling = true;
	private float mSumScrollX = 0;
	private float mSumScrollY = 0;
	private float mFirstTouchedX = 0;
	private float mFirstTouchedY = 0;
	
	private ViewSwitcher mViewSwitcher = null;
	private static final long DURATION_MONTH_SWITCHIGN = 700;
	private static final Interpolator DEFAULT_INTERPOLATER = new AccelerateInterpolator();
	
	private class AlmanacGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent event) {
			Log.d(LOG_TAG, "AlmanacGestureDetector: Detect DOWN event");
			mTouchMode = TOUCH_MODE_DOWN;

			return true;
		}
		
//		@Override
//		public boolean onSingleTapUp(MotionEvent event) {
//			Log.d(LOG_TAG, "AlmanacGestureDetector: Detect UP event");
//			// fail
////			if(mTouchMode == TOUCH_MODE_HSCROLL) {
////				handleHScroll();
////				mTouchMode = TOUCH_MODE_UNKNOWN;
////			}
//			
//			return true;
//		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
			// Log.d(LOG_TAG, "AlmanacGestureDetector: Detect SCROLL event");
			if(mIsNewScrolling) {
				mSumScrollX = 0;
				mSumScrollY = 0;
				mIsNewScrolling = false;
			}
			mSumScrollX += deltaX;
			mSumScrollY += deltaY;
			
			if(mTouchMode == TOUCH_MODE_DOWN) {
				float absSumScrollX = Math.abs(mSumScrollX);
				float absSumScrollY = Math.abs(mSumScrollY);
				
				if(absSumScrollX * SCROLL_SENSITIVITY > absSumScrollY) {
					if(absSumScrollX > MIN_VSCROLL_DISTANCE) {
						mTouchMode = TOUCH_MODE_HSCROLL;
					}
				}
			} else if(mTouchMode == TOUCH_MODE_HSCROLL) {
				// should not do H-scroll here, because this method
				// will be invoked many times
				// should call this method when the finger leave the screen
				// handleHScroll();
			}
			
			return true;
		}// end of onScroll()
	} // end of class AlmanacGestureDetector
	
	private void handleHScroll() {
		boolean gotoNext = (mSumScrollX > 0) ? true : false;
		int action = 0;
		Log.d(LOG_TAG, "GNAlmanacActivity---handleHScroll---mQueryDate == " + mQueryDate);
		Time nextTime = new Time();
		nextTime.set(mQueryDate);
		if(gotoNext) {
			nextTime.monthDay = mQueryDate.monthDay + 1;
			nextTime.normalize(true);
			if(nextTime.year > GNAlmanacUtils.MAX_QUERY_YEAR || nextTime.year < GNAlmanacUtils.MIN_QUERY_YEAR) {
				GNCalendarUtils.showToast(mContext, mStrQueryDateInvalid, Toast.LENGTH_SHORT);
				return;
			}
			
			mQueryDate.monthDay += 1;
			action = EVENT_EXTRA_GOTO_NEXT;
		} else {
			nextTime.monthDay = mQueryDate.monthDay - 1;
			nextTime.normalize(true);
			if(nextTime.year < GNAlmanacUtils.MIN_QUERY_YEAR || nextTime.year > GNAlmanacUtils.MAX_QUERY_YEAR) {
				GNCalendarUtils.showToast(mContext, mStrQueryDateInvalid, Toast.LENGTH_SHORT);
				return;
			}
			
			mQueryDate.monthDay -= 1;
			action = EVENT_EXTRA_GOTO_PREV;
		}
		mQueryDate.normalize(true);
		Log.d(LOG_TAG, "query date " + GNCalendarUtils.printDate(mQueryDate));
		// change query time
		mHandler.obtainMessage(EVENT_CHANGE_QUERY_TIME, mQueryDate).sendToTarget();
		
		GNAlmanacUtils db = GNAlmanacUtils.getInstance();
	    db.getAlmanacInfo(mQueryDate.year, mQueryDate.month + 1, mQueryDate.monthDay, 
	    		mMessageDispose,EVENT_QUERY_ALMANAC_INFO,action);
	}
	
	private boolean checkQueryDateRange() {
		if(mQueryDate.year < GNAlmanacUtils.MIN_QUERY_YEAR 
				|| mQueryDate.year > GNAlmanacUtils.MAX_QUERY_YEAR) {
			GNCalendarUtils.showToast(mContext, mStrQueryDateInvalid, Toast.LENGTH_SHORT);
			Log.d(LOG_TAG, "query date " + GNCalendarUtils.printDate(mQueryDate) + " is out of range[2012 - 2018]");
		}
		
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch(action) {
			case MotionEvent.ACTION_CANCEL:
				// Log.d(LOG_TAG, "INTERCEPT ACTION_CANCEL");
				return false;
				
			case MotionEvent.ACTION_DOWN:
				// Log.d(LOG_TAG, "INTERCEPT ACTION_DOWN");
				mIsNewScrolling = true;
				mGestureDetector.onTouchEvent(event);
				return false;
				
			// case MotionEvent.ACTION_MOVE:
			// // Log.d(LOG_TAG, "INTERCEPT ACTION_MOVE");
			// mGestureDetector.onTouchEvent(event);
			// return false;
				
			case MotionEvent.ACTION_UP:
				Log.d(LOG_TAG, "INTERCEPT ACTION_UP");
				mIsNewScrolling = false;
				// mGestureDetector.onTouchEvent(event);
				// fail
//				if(mTouchMode == TOUCH_MODE_HSCROLL) {
//					handleHScroll();
//					mTouchMode = TOUCH_MODE_UNKNOWN;
//				}
				
				return true;
			
			default:
				// Log.d(LOG_TAG, "INTERCEPT default");
				mGestureDetector.onTouchEvent(event);
				return false;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		
		switch(action) {
			case MotionEvent.ACTION_CANCEL:
				// Log.d(LOG_TAG, "DISPATCHED ACTION_CANCEL");
				return true;
				
			case MotionEvent.ACTION_DOWN:
				// Log.d(LOG_TAG, "DISPATCHED ACTION_DOWN");
				return true;
	
			case MotionEvent.ACTION_MOVE:
				// Log.d(LOG_TAG, "DISPATCHED ACTION_MOVE");
				mGestureDetector.onTouchEvent(event);
				return true;
	
			case MotionEvent.ACTION_UP:
				Log.d(LOG_TAG, "DISPATCHED ACTION_UP");
				// mGestureDetector.onTouchEvent(event);
				if (mTouchMode == TOUCH_MODE_HSCROLL) {
					handleHScroll();
					mTouchMode = TOUCH_MODE_UNKNOWN;
				}
				return true;
				
			default:
				//Log.d(LOG_TAG, "DISPATCHED default");
				mGestureDetector.onTouchEvent(event);
				return true;
		}
	}
	
	private Time mQueryDate = new Time();
	
	public void setAlmanacGregTime(Time t) {
		mQueryDate.set(t);
		Log.v("GNAlmanacView","setAlmanacGregTime---t == " + t);
	}
	
	private Handler mHandler = null;
	
	public void setQueryHandler(Handler h) {
		mHandler = h;
	}
	
	private MessageDispose mMessageDispose; 
	public void setQueryInterface(MessageDispose messageDispose) {
		mMessageDispose = messageDispose;
	}
	
	static final float offset = 0.5f;
	
	public void startDelayAnimation(boolean gotoNext) {
		Animation anim = null;
		if(gotoNext) {
			anim = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0,
	                Animation.RELATIVE_TO_SELF, -offset,
	                Animation.ABSOLUTE, 0,
	                Animation.ABSOLUTE, 0);
		} else {
			anim = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0,
	                Animation.RELATIVE_TO_SELF, offset,
	                Animation.ABSOLUTE, 0,
	                Animation.ABSOLUTE, 0);
		}
		anim.setDuration(GNAlmanacActivity.DURATION_VIEW_SWITCHING);
		
		mLunarDateField.startAnimation(anim);
	}
	
	public void startReturnAnimation(boolean gotoNext) {
		Animation anim = null;
		if(gotoNext) {
			anim = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, offset,
	                Animation.RELATIVE_TO_SELF, 0,
	                Animation.ABSOLUTE, 0,
	                Animation.ABSOLUTE, 0);
		} else {
			anim = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, -offset,
	                Animation.RELATIVE_TO_SELF, 0,
	                Animation.ABSOLUTE, 0,
	                Animation.ABSOLUTE, 0);
		}
		anim.setDuration(GNAlmanacActivity.DURATION_VIEW_SWITCHING + 300);
		anim.setInterpolator(GNAlmanacActivity.DEFAULT_INTERPOLATER);
		
		mLunarDateField.startAnimation(anim);
	}
	
	// for almanac date picker begin
	private View.OnClickListener mLunarFieldOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			showDatePickerDialog();
		}
	};
	
	private View.OnTouchListener mLunarFieldOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if(mTouchMode == TOUCH_MODE_DOWN) {
				showDatePickerDialog();
				
				return true;
			}
			
			return false;
		}
	};
	
	private GNCustomTimeDialog mDatePickerDlg = null;
	private boolean mDlgShowing = false;
	
	private void showDatePickerDialog() {
		if(mDatePickerDlg == null) {
			Log.d("AlmanacTimePicker", "create time picker dialog---mQueryDate == " + mQueryDate);
			mDatePickerDlg = new GNCustomTimeDialog(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN,
					mOnDateSetListener, mQueryDate.year, mQueryDate.month, mQueryDate.monthDay);
			mDatePickerDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					mDlgShowing = false;
				}
			});
		}
		
		if(!mDlgShowing) {
			Log.d("AlmanacTimePicker", "show time picker dialog---mQueryDate == " + mQueryDate);
			mDatePickerDlg.updateDate(mQueryDate.year, mQueryDate.month, mQueryDate.monthDay);
			mDatePickerDlg.show();
			
			mDlgShowing = true;
		}
	}
	
	AuroraDatePickerDialog.OnDateSetListener mOnDateSetListener = new AuroraDatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(AuroraDatePicker view, int year, int month, int monthDay) {
			Log.d("AlmanacTimePicker", "select time " + year + "-" + (month + 1) + "-" + monthDay);
			
		    mDlgShowing = false;
			
			if(mQueryDate.year == year && mQueryDate.month == month && mQueryDate.monthDay == monthDay) {
				Log.d("AlmanacTimePicker", "time is unchanged, no need to query");
				return;
			}
			
			if(year < GNAlmanacUtils.MIN_QUERY_YEAR || year > GNAlmanacUtils.MAX_QUERY_YEAR) {
				Log.d("AlmanacTimePicker", "time is out of range, year = " + year);
				GNCalendarUtils.showToast(mContext, mStrQueryDateInvalid, Toast.LENGTH_SHORT);
				return;
			}
			
			Time savedQueryDate = new Time(mQueryDate);
			
			mQueryDate.year = year;
			mQueryDate.month = month;
			mQueryDate.monthDay = monthDay;
			mQueryDate.normalize(true);
			
			mHandler.obtainMessage(EVENT_CHANGE_QUERY_TIME, mQueryDate).sendToTarget();
			
			int compare = GNCalendarUtils.compareDate(mQueryDate, savedQueryDate);
			int action = EVENT_EXTRA_NO_MOVE;
			if(compare > 0) {
				action = EVENT_EXTRA_GOTO_NEXT;
			} else if(compare < 0) {
				action = EVENT_EXTRA_GOTO_PREV;
			}
			
			GNAlmanacUtils db = GNAlmanacUtils.getInstance();
//		    db.getAlmanacInfo(mQueryDate.year, mQueryDate.month + 1, mQueryDate.monthDay, 
//		    		mHandler.obtainMessage(EVENT_QUERY_ALMANAC_INFO, 0, action));
		    db.getAlmanacInfo(mQueryDate.year, mQueryDate.month + 1, mQueryDate.monthDay,mMessageDispose,EVENT_QUERY_ALMANAC_INFO,action
    		);
		    // mDatePickerDlg.dismiss();
		}
	};
	// for almanac date picker end
	
	//Gionee <jiangxiao> <2013-07-31> add for CR00837096 begin
	// delete messy code
	private String mMessyCodeToken = null;
	private String mMessgeCodeChar = null;
	
	private void loadResMessageCode(Resources res) {
		mMessyCodeToken = res.getString(R.string.almanac_messy_code_token);
		mMessgeCodeChar = res.getString(R.string.almanac_messy_code_char);
	}
	
	private String checkMessyCode_AnDui(String s) {
		if(s == null || s.length() == 0) return s;
		
		int loc = s.indexOf(mMessyCodeToken);
		if(loc >= 0) {
			// string s contain token
			Log.d(LOG_TAG, "find token");
			loc += mMessyCodeToken.length();
			if(loc < s.length() && mMessgeCodeChar.charAt(0) == s.charAt(loc)) {
				Log.d(LOG_TAG, "find messy code " + mMessgeCodeChar);
				StringBuilder sb = new StringBuilder();
				sb.append(s);
				sb.deleteCharAt(loc);
				
				return sb.toString();
			}
		}
		
		Log.d(LOG_TAG, "not find messy code " + mMessgeCodeChar);
		return s;
	}
	//Gionee <jiangxiao> <2013-07-31> add for CR00837096 end
	
}
//Gionee <jiangxiao> <2013-07-16> add for CR00837096 end