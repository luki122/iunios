package com.aurora.weatherforecast;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aurora.weatherdata.util.Blur;

import views.WeatherDetailsView;

import datas.CityListShowData;
import datas.DynamicDeskIconService;
import datas.WarnInfoHelp;
import datas.WeatherWarningInfo;
import adapters.WarnDetailAdapter;
import adapters.WarnDetailAdapter.Holder;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build.VERSION;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GnSurface;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class AuroraWeatherWarnDeatilInfo extends AuroraActivity {

	private LinearLayout ll_warn_detail;
	private CityListShowData cityListShowData;
	private int showCityIndex=-1;
	public static final String SHOW_WARN_INDEX="show_warn_index";
	public static final String SHOW_WARN_IMAGE="show_warn_image";
	private Bitmap showBg;
	private ListView lv_warndetail;
	private WarnDetailAdapter adapter;
	private TextView btn_back;
	private ImageView btn_share;
	private HashMap<Integer, WarnDetailAdapter.Holder> viewsMap = new HashMap<Integer, WarnDetailAdapter.Holder>();
	private AnimatorSet enterSet;
	private boolean isComeFromNotification = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.warn_deatilinfo);
		
		cityListShowData=CityListShowData.getInstance(this);
		if(getIntent() != null && getIntent().getIntExtra(DynamicDeskIconService.COME_FROM_KEY, 0)==DynamicDeskIconService.COME_FROM_NOTIFICATION)
		{
			if(cityListShowData.isExistLocalCity())
			{
				NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(cityListShowData.getCityInfo(0).getID());
			}
			isComeFromNotification = true;
			showCityIndex = 0;
			datas = getIntent().getParcelableArrayListExtra(DynamicDeskIconService.WEATHER_WARN_INFO_KEY);
			weatherDate = getIntent().getStringExtra(DynamicDeskIconService.WEATHER_DATE_KEY);
			String weatherType = getIntent().getStringExtra(DynamicDeskIconService.WEATHER_TYPE);
			showBg =BitmapFactory.decodeResource(getResources(), cityListShowData.getBgResIdByWeatherType(weatherType));
		}else{
			isComeFromNotification = false;
			showCityIndex=getIntent().getIntExtra(SHOW_WARN_INDEX, -1);
			if(cityListShowData.getCityWeatherInfo(showCityIndex) == null)
			{
				finish();
				return;
			}
			if(cityListShowData.getCityWeatherInfo(showCityIndex).getHourInfo() == null)
			{
				finish();
				return;
			}
			datas = cityListShowData.getCityWeatherInfo(showCityIndex).getWeatherWarningList();
			weatherDate = cityListShowData.getCityWeatherInfo(showCityIndex).getHourInfo().getmWeatherDate();
			showBg = WeatherDetailsView.bitmap;
			if (showBg != null) {
				if (VERSION.SDK_INT > 20) {
					showBg = Blur.fastblur(this, showBg, 25);
				} else {
					showBg = Blur.fastblur(this, showBg, 80);
				}
			} else {
				showBg = BitmapFactory.decodeResource(getResources(),
						cityListShowData.getBgResIdByWeatherType(weatherDate));
			}
		}
		WarnInfoHelp.getInstance(this).addWarnInfo(cityListShowData.getCityInfo(showCityIndex).getID(),datas.get(0));
		init();
	}
	
	
	private Runnable runAnimatorRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			startEnterAnimator();
		}
	};
	
	private float delY;
	
	/**
	 * 创建入场动画
	 * @param target
	 * @param sourceY
	 * @return
	 */
	@SuppressLint("NewApi")
	private ObjectAnimator createEnterAnimator(View target,float targetAlpha){
		PropertyValuesHolder pi1 = PropertyValuesHolder.ofFloat("alpha", 0, targetAlpha);
		PropertyValuesHolder pi2 = PropertyValuesHolder.ofFloat("y",delY, 0);
		return ObjectAnimator.ofPropertyValuesHolder(target, pi1,pi2);
	}
	
	/**
	 * 创建出场动画
	 * @param target
	 * @param sourceY
	 * @return
	 */
	@SuppressLint("NewApi")
	private ObjectAnimator createExitAnimator(View target,float targetAlpha){
		PropertyValuesHolder pi1 = PropertyValuesHolder.ofFloat("alpha", targetAlpha, 0);
		PropertyValuesHolder pi2 = PropertyValuesHolder.ofFloat("y",0,delY);
		return ObjectAnimator.ofPropertyValuesHolder(target, pi1,pi2);
	}
	
	private static final int BASE_DURATION = 200;
	private AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
	private AnimatorSet titleAnimator,titleExitAnimator;
	@SuppressLint("NewApi")
	private void startEnterAnimator(){
		titleAnimator=new AnimatorSet();
		ObjectAnimator leftAnimator = ObjectAnimator.ofFloat(btn_back, "alpha", 0,1).setDuration(130);
		ObjectAnimator rightAnimator = ObjectAnimator.ofFloat(btn_share, "alpha", 0,1).setDuration(130);
		titleAnimator.playTogether(leftAnimator,rightAnimator);
		enterSet = new AnimatorSet();
		int count = adapter.getCount();
		List<Animator> sets = new ArrayList<Animator>();
		for(int i = 0;i<count;i++)
		{
			WarnDetailAdapter.Holder holder = viewsMap.get(i);
			ObjectAnimator o1 = createEnterAnimator(holder.tv_warninfo_title,0.8f).setDuration(BASE_DURATION);
			ObjectAnimator o2 = createEnterAnimator(holder.tv_warninfo_content,0.7f).setDuration(BASE_DURATION + 70);
			ObjectAnimator o3 = createEnterAnimator(holder.tv_warninfo_time,0.5f).setDuration(BASE_DURATION + 70);
			o1.setInterpolator(accelerateDecelerateInterpolator);
			o2.setInterpolator(accelerateDecelerateInterpolator);
			o3.setInterpolator(accelerateDecelerateInterpolator);
			AnimatorSet set = new AnimatorSet();
			set.playTogether(o1,o2,o3);
			sets.add(set);
		}
		enterSet.playSequentially(sets);
		enterSet.addListener(animatorListener);
		titleAnimator.addListener(animatorListener);
		titleAnimator.start();
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(isComeFromNotification)
			{
				finish();
				overridePendingTransition(0, 0);
			}else{
			    startExitAnimator();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private boolean isAnimatorEnd = false;
	
	@SuppressLint("NewApi")
	private AnimatorListener animatorListener = new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			if(animation == titleAnimator)
			{
				enterSet.start();
			}else if(animation == enterSet)
			{
				adapter.disableRunAnimator();
				isAnimatorEnd = true;
			}
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
		}
	};
	
	@SuppressLint("NewApi")
	private void startExitAnimator(){
		titleExitAnimator=new AnimatorSet();
		ObjectAnimator leftAnimator = ObjectAnimator.ofFloat(btn_back, "alpha", 1,0).setDuration(100);
		ObjectAnimator rightAnimator = ObjectAnimator.ofFloat(btn_share, "alpha", 1,0).setDuration(100);
		titleExitAnimator.playTogether(leftAnimator,rightAnimator);
		titleExitAnimator.start();
		finish();
		overridePendingTransition(R.anim.main_in, R.anim.warn_detail_out);
	}
	
	private List<WeatherWarningInfo> datas;
	private String weatherDate;
	
	private void init(){
		delY=getResources().getDisplayMetrics().density*10;
		ll_warn_detail=(LinearLayout)findViewById(R.id.ll_warn_detail);
		
		if(WeatherDetailsView.bitmap!=null&&!WeatherDetailsView.bitmap.isRecycled())
		{
			WeatherDetailsView.bitmap.recycle();
			WeatherDetailsView.bitmap=null;
		}
		ll_warn_detail.setBackgroundDrawable(new BitmapDrawable(showBg));
		lv_warndetail=(ListView)findViewById(R.id.lv_warndetail);
		adapter=new WarnDetailAdapter(this, showCityIndex,viewsMap,datas,weatherDate);
		if(adapter.getCount()==1)
		{
			lv_warndetail.setDivider(null);
		}else{
			lv_warndetail.setDivider(getResources().getDrawable(R.drawable.warn_divide));
		}
		lv_warndetail.setAdapter(adapter);
		btn_back = (TextView)findViewById(R.id.btn_back);
		btn_share = (ImageView)findViewById(R.id.btn_share);
		btn_back.setOnClickListener(listener);
		btn_share.setOnClickListener(listener);
		new Handler().postDelayed(runAnimatorRunnable,250);
	}
	
	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_back:
				if(isComeFromNotification)
				{
					finish();
					overridePendingTransition(0, 0);
				}else{
				   startExitAnimator();
				}
				break;
			case R.id.btn_share:
				share();
				break;
			}
		}
	};
	
	private boolean isShare = false;
	
	protected void onResume() {
		super.onResume();
		isShare = false;
	}
	
	
	@SuppressLint("NewApi")
	private void share(){
		if(!isAnimatorEnd || isShare)
			return;
		isShare = true;
		String sharePath = MediaStore.Images.Media.insertImage(getContentResolver(),
				GnSurface.screenshot(getResources().getDisplayMetrics().widthPixels,
				getResources().getDisplayMetrics().heightPixels), "weather_share_image", null);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(sharePath));
		startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(showBg!=null&&!showBg.isRecycled())
		{
			showBg.recycle();
			showBg=null;
		}
	}
}
