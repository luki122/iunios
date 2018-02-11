package views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.TimerTask;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.AuroraWeatherWarnDeatilInfo;
import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.CityListShowItem;
import datas.WarnInfoHelp;
import datas.WeatherData;
import datas.WeatherHourInfo;
import datas.WeatherWarningInfo;

import adapters.WarnDetailAdapter;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GnSurface;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

@SuppressLint("NewApi")
public class WeatherDetailsView extends AbstractWeatherView {

	private ImageView mTemperatureImage_1;
	private ImageView mTemperatureImage_2;
	private ImageView mTemperatureImage_point;
	private RelativeLayout mTemperatureImage_detail;
	private ImageView mTemperatureImage_minus;
	private ImageView mTemperatureImage_point_small;
	private TextView mWeatherDetailText,tv_warn_info;
	private RelativeLayout mWweatherdetail_rela;
	private Context mContext;

	private int mAnimator_mode = 0;
	private int mCurTemp;

	private boolean updateFlag = false;
	
	private boolean isAnimRun;

	private static final int[] temprature_resid = new int[]{
		R.drawable.temperature0, R.drawable.temperature1, R.drawable.temperature2,
		R.drawable.temperature3, R.drawable.temperature4, R.drawable.temperature5,
		R.drawable.temperature6, R.drawable.temperature7, R.drawable.temperature8,
		R.drawable.temperature9
	};

	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//Log.e("111111", "---111mTemperatureImage_2----mIndex_bit = ---" + mIndex_bit);
			//Log.e("111111", "---111----mCurTemp = ---" + mCurTemp);
			mTemperatureImage_2.setImageResource(temprature_resid[mIndex_bit]);
			mTemperatureImage_1.setImageResource(temprature_resid[mIndex_tenBit]);
			mIndex_bit++;
			if ( mIndex_bit > 9 ) {
				mIndex_tenBit ++;
				mIndex_bit = 0;
			}
			if ( mIndex_bit + mIndex_tenBit * 10 <= mCurTemp ) {
				//Log.e("111111", "---222mTemperatureImage_2----mIndex_bit = ---" + mIndex_bit);
				mHandler.postDelayed(mRunnable, 10);
			}
		}
	};
	private int mIndex_bit = 0;
	private int mIndex_tenBit = 0;

	public WeatherDetailsView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public WeatherDetailsView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public WeatherDetailsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		init(context);
	}
	private int screenWidth,screenHeight;
	private WarnInfoHelp warnInfoHelp;
	private void init(Context context) {
		mContext = context;
		warnInfoHelp=WarnInfoHelp.getInstance(mContext);
		screenWidth=mContext.getResources().getDisplayMetrics().widthPixels;
		screenHeight=mContext.getResources().getDisplayMetrics().heightPixels;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.weatherdetailsview, this, true);
		mTemperatureImage_minus = (ImageView)this.findViewById(R.id.temperature_minus);
		mTemperatureImage_1 = (ImageView)this.findViewById(R.id.temperature_1);
		mTemperatureImage_2 = (ImageView)this.findViewById(R.id.temperature_2);
		mTemperatureImage_point = (ImageView)this.findViewById(R.id.temperature_point);
		mWeatherDetailText = (TextView)this.findViewById(R.id.weatherdetailtext);
		tv_warn_info=(TextView)findViewById(R.id.tv_warn_info);
		tv_warn_info.setOnClickListener(onClickListener);
		//mWeatherDetailText.setText("无数据 0~0");

		mWweatherdetail_rela = (RelativeLayout)this.findViewById(R.id.weatherdetail_rela);
		mTemperatureImage_detail = (RelativeLayout)this.findViewById(R.id.temperature_detail);
		
		mTemperatureImage_point_small = (ImageView)this.findViewById(R.id.temperature_point_small);
	}

	
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.tv_warn_info:
				toWarnDetailInfoPage();
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCurrentpageChange() {
		handleWarnInfo();
	};
	
	private boolean isExistWarnInfo(){
		if(mCityListShowData.getCityWeatherInfo(getIndex()) == null)
		{
			return false;
		}
		warnInfos = mCityListShowData.getCityWeatherInfo(getIndex()).getWeatherWarningList();
		if(warnInfos!=null&&warnInfos.size()>0)
		{
			return true;
		}
		return false;
	}
	
	private void handleWarnInfo(){
		if(isExistWarnInfo())
		{
			tv_warn_info.setVisibility(View.VISIBLE);
			tv_warn_info.setText(mContext.getString(R.string.tv_have_warn, warnInfos.get(0).getTitle()));
			setShowWarnAnim(warnInfos.get(0));
		}else{
			tv_warn_info.setVisibility(View.GONE);
			tv_warn_info.setText("");
		}
	}
	
	
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		boolean isVisible = visibility==View.VISIBLE;
		if(isVisible)
		{
			handleWarnInfo();
		}else{
			if(warnAnimThread!=null)
			{
				warnAnimThread.setIsWait(true);
			}
		}
	}
	
	public static Bitmap bitmap;
	
	private void toWarnDetailInfoPage()
	{
		float rate=3.8f;
		bitmap =GnSurface.screenshot((int)(screenWidth/rate), (int)(screenHeight/rate));
		Intent intent=new Intent(mContext, AuroraWeatherWarnDeatilInfo.class);
		intent.putExtra(AuroraWeatherWarnDeatilInfo.SHOW_WARN_INDEX, getIndex());
		mContext.startActivity(intent);
		((AuroraWeatherMain)mContext).overridePendingTransition(R.anim.warn_detail_in, R.anim.main_out);
	}
	
	private List<WeatherWarningInfo> warnInfos;
	
	private void showCurrentTemp(String temp,CityListShowItem dateItem){
		if(temp == null)
		{
			return;
		}
		
		try {
			mCurTemp = Integer.parseInt(temp);
		} catch (NumberFormatException e) {
			if(dateItem != null)
			{
			   mWeatherDetailText.setText(dateItem.getWeahterType() + " " + dateItem.getLowAndHighTempStr());
			}
			mTemperatureImage_minus.setVisibility(View.GONE);
			mTemperatureImage_1.setVisibility(View.GONE);
			mTemperatureImage_2.setImageResource(R.drawable.default_temperature);
			mTemperatureImage_point.setVisibility(View.GONE);
			e.printStackTrace();
			return;
		}
		if ( mCurTemp < 0 ) {
			mTemperatureImage_minus.setVisibility(View.VISIBLE);
		} else {
			mTemperatureImage_minus.setVisibility(View.GONE);
			mTemperatureImage_1.setPadding(0, 0, 0, 0);
		}
		mCurTemp = Math.abs(mCurTemp);
		if ( mCurTemp / 10 > 0 ) {
			mTemperatureImage_1.setVisibility(View.VISIBLE);
		} else {
			mTemperatureImage_1.setVisibility(View.GONE);
			mTemperatureImage_2.setPadding(0, 0, 0, 0);
		}
		mTemperatureImage_1.setImageResource(temprature_resid[mCurTemp / 10]);
		mTemperatureImage_2.setImageResource(temprature_resid[mCurTemp % 10]);
		mTemperatureImage_point.setVisibility(View.VISIBLE);
	}
	
	private void setWeatherTypeText(String weatherType,String lowAndHighTemStr){
		mWeatherDetailText.setText(weatherType + " " + lowAndHighTemStr);
	}
	
	
	private void showViews( ) {
		CityListShowItem dateItem=mCityListShowData.getWeatherDateItem(getIndex());
		if(dateItem==null)
			return;
		showCurrentTemp(dateItem.getCurTemp(),dateItem);
		setWeatherTypeText(dateItem.getWeahterType(), dateItem.getLowAndHighTempStr());  
		handleWarnInfo();
	}
	
	private boolean isPageShow()
	{
		return mCityListShowData.getCurrentPageIndex()==getIndex();
	}
	
	
	private void setShowWarnAnim(WeatherWarningInfo info){
		if(isPageShow()&&!warnInfoHelp.isExistWarnInfo(mCityListShowData.getCityInfo(getIndex()).getID(), info))
		{
			if(warnAnimThread==null)
			{
				warnAnimThread = new WarnAnimThread();
				warnAnimThread.start();
				warnAnimThread.setIsWait(false);
			}else{
				warnAnimThread.setIsWait(false);
			}
		}else{
			if(warnAnimThread!=null)
			{
				warnAnimThread.setIsWait(true);
				tv_warn_info.setAlpha(0.4f);
			}
		}
	}
	
	
	
	private WarnAnimThread warnAnimThread;

	@SuppressLint("NewApi")
	private class WarnAnimThread extends Thread {
		private float MAX_ALPHA = 0.4F, MIN_ALPHA = 0.1F;
		private final float DEL_ALPHA = 0.02F;
		private float alpha = MAX_ALPHA;
		private Object synObj = new Object();
		private boolean isWait = true;

		public void setIsWait(boolean isWait) {
			if (this.isWait == isWait) {
				return;
			}
			this.isWait = isWait;
			if (!isWait) {
				synchronized (synObj) {
					synObj.notifyAll();
				}
			}
			this.isWait = isWait;
		}

		public void run() {
			boolean isReverse = false;
			while (isRunning) {
				synchronized (synObj) {
					if (isWait) {
						try {
							synObj.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if (alpha >= MAX_ALPHA) {
					alpha = MAX_ALPHA;
					isReverse = false;
				} else if (alpha <= MIN_ALPHA) {
					alpha = MIN_ALPHA;
					isReverse = true;
				}
				if (isReverse) {
					alpha += DEL_ALPHA;
				} else {
					alpha -= DEL_ALPHA;
				}
				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						tv_warn_info.setAlpha(alpha);
					}
				});

				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
	}
	
	private boolean isRunning=true;
	private void updateViews( ) {
		updateFlag = true;
		startExitAnim();
	}

	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		startTranslationInAnim( );
		if ( mAnimator_mode == 0 && mCityListShowData.getCitySize() > 0) {
			startChangeTemperatureAnim( );
		}
	}

	private void startTranslationInAnim( ) {
		
		if (isAnimRun) {
			return;
		}
		
		float distance = DensityUtil.dip2px(mContext, 40);

		ObjectAnimator translateIn = getAnimator(mTemperatureImage_detail, "TranslationY", -distance, 0);
		ObjectAnimator alphaIn = getAnimator(mTemperatureImage_detail, "Alpha", 0, 1);

		AnimatorSet animator = new AnimatorSet();
		animator.setInterpolator(new DecelerateInterpolator());
		animator.setDuration(300);
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				isAnimRun = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				isAnimRun = false;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				isAnimRun = false;
			}
		});
		animator.play(translateIn).with(alphaIn);
		animator.start();
	}

	private void startChangeTemperatureAnim( ) {
		if(mCityListShowData.getWeatherDateItem(getIndex()).getCurTemp().equals(mContext.getString(R.string.default_temperature)))
			return;
		//Log.e("111111", "----startChangeTemperatureAnim-----");
		mIndex_bit = 0;
		mIndex_tenBit = 0;
		mHandler.removeCallbacks(mRunnable);
		mHandler.post(mRunnable);

	}

	private void startTranslationOutAnim( ) {
		
		if (isAnimRun) {
			return;
		}
		
		float distance = DensityUtil.dip2px(mContext, 40);

		ObjectAnimator translateIn = getAnimator(mTemperatureImage_detail, "TranslationY", 0, distance);
		ObjectAnimator alphaIn = getAnimator(mTemperatureImage_detail, "Alpha", 1, 0);

		AnimatorSet animator = new AnimatorSet();
		animator.setInterpolator(new DecelerateInterpolator());
		if ( updateFlag ) {
			animator.setDuration(400);
		} else {
			animator.setDuration(500);
		}
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				isAnimRun = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				isAnimRun = false;
				mTemperatureImage_detail.setTranslationY(0);
				mTemperatureImage_detail.setAlpha(1f);
				if ( updateFlag ) {
					updateFlag = false;
					normalShow();
					startEntryAnim();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				isAnimRun = false;
				if ( updateFlag ) {
					updateFlag = false;
					normalShow();
					startEntryAnim();
				}
			}
		});
		animator.play(translateIn).with(alphaIn);
		animator.start();
	}

	private boolean isInit=true;
	
	@Override
	public void normalShow() {
		String curTemp = ((AuroraWeatherMain)mContext).getFirstPageCityTemp();
		if(getIndex()==0&&isInit&&curTemp!=null)
		{
			if(curTemp!=null)
			{
				showCurrentTemp(curTemp,null);
			}
			
			String weatherType = ((AuroraWeatherMain)mContext).getFirstPageCityWeatherType();
			String lowHighTempStr = ((AuroraWeatherMain)mContext).getFirstPageCityLowHighTemp();
			if(weatherType!=null && lowHighTempStr!=null)
			{
				setWeatherTypeText(weatherType, lowHighTempStr);
			}
		}else{
		// TODO Auto-generated method stub
		showViews( );
		}
		isInit=false;
	}

	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		startTranslationOutAnim( );
	}

	@Override
	public void onDestroy() {
		if(warnAnimThread!=null)
		{
			isRunning=false;
			warnAnimThread.setIsWait(false);
		}
       warnInfoHelp.release();
	}

	@Override
	public void onUpdating() {
		// TODO Auto-generated method stub
		updateViews( );
	}
	
	/*
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		registerNavigationBarObserver();
		//Log.e("111111", "---onAttachedToWindow = -----");
		judgeToChangeBottom( );
	}
	
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		//Log.e("111111", "---onDetachedFromWindow = -----");
		unregisterNavigationBarObserver();
	}
	
	private void judgeToChangeBottom( ) {
		int isNaviBarHide = Settings.System.getInt(mContext.getContentResolver(), NAVI_KEY_HIDE, 1);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mWweatherdetail_rela.getLayoutParams();
		//Log.e("111111", "---isNaviBarHide = -----" + isNaviBarHide);
		//0表示显示，1表示隐藏
		if ( isNaviBarHide == 0 ) {
			params.bottomMargin = (int)mContext.getResources().getDimension(R.dimen.detailsviewmarginbottom);
		} else {
			params.bottomMargin = (int)mContext.getResources().getDimension(R.dimen.detailsviewmarginbottom_no_navigationbar);
		}
		mWweatherdetail_rela.setLayoutParams(params);
	}
	
	//专门为U3虚拟键监听而增加的内容 aurora add by tangjun 2014.8.1 start
	private static final String NAVI_KEY_HIDE  = "navigation_key_hide"; // Settings.System 对应的键值
    private NavigationBarObserver mNavigationBarObserver = new NavigationBarObserver(new Handler());
    
    private class NavigationBarObserver extends ContentObserver {

		public NavigationBarObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			judgeToChangeBottom( );
		}
    };
    
    private void registerNavigationBarObserver() {
    	//Log.e("111111", "-AuroraMenuBase--registerNavigationBarObserver = ---------");
    	Uri uri = Settings.System.getUriFor(NAVI_KEY_HIDE);
    	mContext.getContentResolver().registerContentObserver(uri, true, mNavigationBarObserver);
    }
    
    private void unregisterNavigationBarObserver() {
    	//Log.e("111111", "-AuroraMenuBase--unregisterNavigationBarObserver = ---------");
    	mContext.getContentResolver().unregisterContentObserver(mNavigationBarObserver);
    }

	//专门为U3虚拟键监听而增加的内容 aurora add by tangjun 2014.8.1 end
	 * 
	 */
}
