package views;

import com.aurora.weatherforecast.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.aurora.utils.DensityUtil;

import datas.CityListShowData;
import datas.CityListShowItem;
import datas.WeatherAirQualities;
import datas.WeatherCityInfo;
import datas.WeatherData;
import datas.WeatherDataEveryDay;
import datas.WeatherHourInfo;

@SuppressLint("NewApi")
public class Forecast24hAirView extends AbstractWeatherView implements
		OnClickListener,AnimatorListener {
	private TextView tv_forcast, tv_24hour;
	private ImageView iv_focus;
    private RelativeLayout rl_forcast,rl_24hour;
    private RelativeLayout ll_air_detail;
	private  int select;
	public static final int FORCAST_SELECT=1,HOUR_24_SELECT=2;
	private final int DURATION=400;
	private TextView tv_air,tv_weather,tv_air_quality;
	private ImageView iv_point;
	
	
	public Forecast24hAirView(Context context) {
		super(context);
		initView(context);
	}

	public Forecast24hAirView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public Forecast24hAirView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	
	private void initView(Context context) {
		LayoutInflater.from(context).inflate(R.layout.forcast_air_layout, this);
//		tv_forcast = (TextView) findViewById(R.id.tv_forcast);
//		tv_24hour = (TextView) findViewById(R.id.tv_24hour);
//		rl_forcast=(RelativeLayout) findViewById(R.id.rl_forcast);
//		rl_24hour=(RelativeLayout) findViewById(R.id.rl_24hour);
//		rl_forcast.setOnClickListener(this);
//		rl_24hour.setOnClickListener(this);
//		iv_focus = (ImageView) findViewById(R.id.iv_focus);
		tv_air=(TextView)findViewById(R.id.tv_air);
		tv_weather=(TextView)findViewById(R.id.tv_weather);
		ll_air_detail=(RelativeLayout)findViewById(R.id.ll_air_detail);
		tv_air_quality=(TextView)findViewById(R.id.tv_air_quality);
		iv_point=(ImageView)findViewById(R.id.iv_point);
		select=FORCAST_SELECT;
		setAnimator();
	}

	private ObjectAnimator focusAnimatorRight, focusAnimatorLeft,alphaFromAnimator,alphaToAnimator;
	
	private ObjectAnimator airDisAppearAnimator,airAppearAnimator,
	                        weatherDisAppearAnimator,weatherAppearAnimator;
	private AnimatorSet updateAnimatorSet,enterAnimatorSet,exitAnimatorSet;
	
	private final int DELT_DISTANCE=20;
	private final float FROM_ALPHA=0.65F,TO_ALPHA=0;
	private void setAnimator() {
		airAppearAnimator=createXTranslaterAndAlphaAnimator(ll_air_detail, DensityUtil.dip2px(getContext(), DELT_DISTANCE),0,TO_ALPHA,FROM_ALPHA).setDuration(DURATION);
		airDisAppearAnimator=createXTranslaterAndAlphaAnimator(ll_air_detail, 0,DensityUtil.dip2px(getContext(), DELT_DISTANCE),FROM_ALPHA,TO_ALPHA).setDuration(DURATION);
		weatherAppearAnimator=createXTranslaterAndAlphaAnimator(tv_weather, DensityUtil.dip2px(getContext(), -DELT_DISTANCE),0,TO_ALPHA,FROM_ALPHA).setDuration(DURATION);
		weatherDisAppearAnimator=createXTranslaterAndAlphaAnimator(tv_weather, 0,DensityUtil.dip2px(getContext(), -DELT_DISTANCE),FROM_ALPHA,TO_ALPHA).setDuration(DURATION);
		AnimatorSet airUpdateSet=new AnimatorSet();
		airUpdateSet.playSequentially(airDisAppearAnimator,airAppearAnimator);
		AnimatorSet weatherUpdateSet=new AnimatorSet();
		weatherUpdateSet.playSequentially(weatherDisAppearAnimator,weatherAppearAnimator);
		enterAnimatorSet=new AnimatorSet();
		enterAnimatorSet.playTogether(airAppearAnimator,weatherAppearAnimator);
		enterAnimatorSet.addListener(animatorListener);
		exitAnimatorSet=new AnimatorSet();
		exitAnimatorSet.playTogether(airDisAppearAnimator,weatherDisAppearAnimator);
		exitAnimatorSet.addListener(animatorListener);
		updateAnimatorSet = new AnimatorSet();
		updateAnimatorSet.playTogether(airUpdateSet,weatherUpdateSet);
		updateAnimatorSet.addListener(animatorListener);
	}
	
	private AnimatorListener animatorListener=new AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationRepeat(Animator arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animator arg0) {
			// TODO Auto-generated method stub
			tv_weather.setAlpha(FROM_ALPHA);
			ll_air_detail.setAlpha(FROM_ALPHA);
			tv_weather.setX(0);
			ll_air_detail.setX(0);
		}
		
		@Override
		public void onAnimationCancel(Animator arg0) {
			
		}
	};
	
	private ObjectAnimator createXTranslaterAndAlphaAnimator(View target,float... values){
		PropertyValuesHolder xHolder=PropertyValuesHolder.ofFloat("x", values[0],values[1]);
		PropertyValuesHolder alphaHolder=PropertyValuesHolder.ofFloat("alpha", values[2],values[3]);
		return ObjectAnimator.ofPropertyValuesHolder(target, xHolder,alphaHolder);
	}
	
	/**
	 * 暂时没有作用
	 */
	private void setFocusAnimator() {
		focusAnimatorRight = ObjectAnimator.ofFloat(iv_focus, "x",
				rl_forcast.getX(), rl_24hour.getX()).setDuration(DURATION);
		focusAnimatorLeft = ObjectAnimator.ofFloat(iv_focus, "x",
				rl_24hour.getX(), rl_forcast.getX()).setDuration(DURATION);
		tv_24hour.setWidth(tv_forcast.getWidth());
		focusAnimatorRight.addListener(this);
		focusAnimatorLeft.addListener(this)	;
	}

	@Override
	public void startEntryAnim() {
		enterAnimatorSet.start();
	}

	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		setData();
	}
    /**
     * 设置数据
     */
	private void setData(){
		CityListShowItem item=mCityListShowData.getWeatherDateItem(getIndex());
		if(item==null)
		{
			return;
		}
		if(!item.isHasAirQuality())
		{
			tv_weather.setText(item.getWindContent());
			tv_air.setText(item.getmHumidityContent());
			iv_point.setVisibility(View.INVISIBLE);
			tv_air_quality.setText("");
		}else{
			tv_weather.setText(item.getWindContent()+"  "+item.getmHumidityContent());
			tv_air_quality.setText(item.getAirQualityDesc());
			tv_air.setText(item.getAirQualityValueDes());
			iv_point.setVisibility(View.VISIBLE);
		}
		
	}
	
	
	@Override
	public void onUpdating() {
		super.onUpdating();
		setData();
		if(updateAnimatorSet!=null&&updateAnimatorSet.isRunning())
			return;
		updateAnimatorSet.start();
	}
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		exitAnimatorSet.start();
	}

	private boolean IS_SHOW_24_HOURS_TEM=false;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_forcast:
			if(!IS_SHOW_24_HOURS_TEM)
				return;
			if(select==FORCAST_SELECT)
				return;
			if(getParent().getParent()!=null)
			{
				((ForcastAndHoursView)getParent().getParent()).exit(select);
			}
			if (focusAnimatorRight == null || focusAnimatorLeft == null)
				setFocusAnimator();
			focusAnimatorLeft.start();
			rl_forcast.findViewById(R.id.focus).setVisibility(View.INVISIBLE);
			rl_24hour.findViewById(R.id.focus).setVisibility(View.INVISIBLE);
			iv_focus.setVisibility(View.VISIBLE);
			select=FORCAST_SELECT;
			break;
		case R.id.rl_24hour:
			if(!IS_SHOW_24_HOURS_TEM)
				return;
			if(select==HOUR_24_SELECT)
				return;
			if(getParent().getParent()!=null)
			{
				((ForcastAndHoursView)getParent().getParent()).exit(select);
			}
			if (focusAnimatorRight == null || focusAnimatorLeft == null)
				setFocusAnimator();
			focusAnimatorRight.start();
			rl_forcast.findViewById(R.id.focus).setVisibility(View.INVISIBLE);
			rl_24hour.findViewById(R.id.focus).setVisibility(View.INVISIBLE);
			iv_focus.setVisibility(View.VISIBLE);
			select=HOUR_24_SELECT;
			break;
		default:
			break;
		}

	}

	/**
	 * Animator listener
	 * @param animation
	 */
	@Override
	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public void onAnimationEnd(Animator animation) {
		// TODO Auto-generated method stub
		if(animation==focusAnimatorRight)
		{
			rl_24hour.findViewById(R.id.focus).setVisibility(View.VISIBLE);
			tv_24hour.setAlpha(FROM_ALPHA);
			tv_forcast.setAlpha(FROM_ALPHA);
		}else if(animation==focusAnimatorLeft)
		{
			rl_forcast.findViewById(R.id.focus).setVisibility(View.VISIBLE);
			tv_24hour.setAlpha(FROM_ALPHA);
			tv_forcast.setAlpha(FROM_ALPHA);
		}
		if(getParent().getParent()!=null)
		{
			((ForcastAndHoursView)getParent().getParent()).changeContent(select);
		}
		iv_focus.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub
		if(animation==focusAnimatorRight)
		{
			rl_24hour.findViewById(R.id.focus).setVisibility(View.VISIBLE);
			tv_24hour.setAlpha(1);
			tv_forcast.setAlpha(0.5f);
		}else if(animation==focusAnimatorLeft)
		{
			rl_forcast.findViewById(R.id.focus).setVisibility(View.VISIBLE);
			tv_24hour.setAlpha(0.5f);
			tv_forcast.setAlpha(1);
		}
		if(getParent().getParent()!=null)
		{
			((ForcastAndHoursView)getParent().getParent()).changeContent(select);
		}
		iv_focus.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub
		
	}
}
