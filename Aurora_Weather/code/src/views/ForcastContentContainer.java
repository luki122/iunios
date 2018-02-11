package views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherdata.db.CityConditionAdapter;
import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.WeatherForcastInfo;
@SuppressLint("NewApi")
public class ForcastContentContainer extends LinearLayout {
	private final int SHOW_COUNT = 6;// 显示的数据天数
	private LinearLayout dayContainer, temperatureContainer;
	private RelativeLayout weatherContainer;
	private final int SHORT_DURATION=320,LONG_DURATION=400;
	private float density;
	private LinearLayout.LayoutParams lp;
	private int[] showImgaeId = { R.drawable.smail_char_n, R.drawable.smail_char_n,
			R.drawable.smail_char_n, R.drawable.smail_char_n, R.drawable.smail_char_n, R.drawable.smail_char_n };
	
	private final int[] mSmallWeatherIcon = new int[] {
		//晴
		R.drawable.s_sunny,
		//晴转多云
		R.drawable.s_sunny_to_cloudy,
		//晴转雾
		R.drawable.s_sunny_to_fog,
		//晴转雨
		R.drawable.s_sunny_to_rainy,
		//晴转雷阵雨
		R.drawable.s_sunny_to_thundershower,
		//晴转雪
		R.drawable.s_sunny_to_snow,
		//晴转沙尘
		R.drawable.s_sunny_to_dust,
		//多云
		R.drawable.s_cloudy,
		//多云转雾
		R.drawable.s_cloudy_to_fog,
		//多云转雨
		R.drawable.s_cloudy_to_rainy,
		//多云转雷阵雨
		R.drawable.s_cloudy_to_thundershower,
		//多云转雪
		R.drawable.s_cloudy_to_snow,
		//多云转沙尘
		R.drawable.s_cloudy_to_dust,
		//雾
		R.drawable.s_fog,
		//雾转雨
		R.drawable.s_fog_to_rainy,
		//雾转雷阵雨
		R.drawable.s_fog_to_thundershower,
		//雾转雪
		R.drawable.s_fog_to_snow,
		//雾转沙尘
		R.drawable.s_fog_to_dust,
		//雨
		R.drawable.s_rainy,
		//雨转雷阵雨
		R.drawable.s_rainy_to_thundershower,
		//雨转雪
		R.drawable.s_rainy_to_snow,
		//雨转沙尘
		R.drawable.s_rainy_to_dust,
		//雷阵雨
		R.drawable.s_thundershower,  
		//雷阵雨转雪
		R.drawable.s_thundershower_snow,
		//雷阵雨转沙尘
		R.drawable.s_thundershower_dust,
		//雪
		R.drawable.s_snow,
		//雪转沙尘
		R.drawable.s_snow_to_dust,
		//沙尘
		R.drawable.s_dust,
		//阵雨
		R.drawable.s_shower,
		//小雨
		R.drawable.s_small_rain,
		//中雨
		R.drawable.s_middle_rain,
		//大雨
		R.drawable.s_large_rain,
		//暴雨
		R.drawable.s_heavy_rain,
		//雷阵雨伴有冰雹
		R.drawable.s_thundershower_with_hail,
		//冻雨
		R.drawable.s_ice_rain,
		//霾
		R.drawable.s_haze,
		//扬尘
		R.drawable.s_raise_dust,
		//阴
		R.drawable.s_shade,
		//小雪
		R.drawable.s_small_snow,
		//中雪
		R.drawable.s_middle_snow,
		//大雪
		R.drawable.s_large_snow,
		//暴雪
		R.drawable.s_heavy_snow,
		//雨夹雪
		R.drawable.s_rain_with_snow,
		//阵雪
		R.drawable.s_snow_shower
	};
	
	private String[] temperatures={"","","","","",""};
    private int dayContainerHeight,weatherContainerHeight,temperatureContainerHeight;
	private String[] weeks;
	private float delY=0;
	public ForcastContentContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public ForcastContentContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ForcastContentContainer(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		mCityListShowData=CityListShowData.getInstance(context);
		setDefaultValue(context);
		weeks=getResources().getStringArray(R.array.week_name_chinese);
		delY=DensityUtil.dip2px(context, 20);
		lp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		setOrientation(LinearLayout.VERTICAL);
		initDayView(context);
		initWeatherView(context);
		initTemperatureView(context);
		setWeatherShowIcon();
		setTemperature();
	}
	
	private String defaultWeatherTypes[];
	private String defaultDays[];
	private String defaultTemperature;
	private void setDefaultValue(Context context){
		defaultTemperature=context.getString(R.string.default_temperature);
		temperatures=context.getResources().getStringArray(R.array.weather_temperature_forcast);
		defaultDays=context.getResources().getStringArray(R.array.default_day_show);
		defaultWeatherTypes=context.getResources().getStringArray(R.array.weather_type_forcast);
		showImgaeId = new int[]{ R.drawable.smail_char_n, R.drawable.smail_char_n,
				R.drawable.smail_char_n, R.drawable.smail_char_n, R.drawable.smail_char_n, R.drawable.smail_char_n };
	}
	
	private SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
	private Calendar calendar=Calendar.getInstance();
	private Calendar today=Calendar.getInstance();
	
	private Calendar formatStr2Calendar(String dateStr){
		    if(TextUtils.isEmpty(dateStr))
		    	return null;
		    try {
				Date d=format.parse(dateStr);
				calendar.setTime(format.parse(dateStr));
				return calendar;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
			
	}
	
	private String getDayOfWeekStrByDateStr(String dateStr) {
		try {
			calendar.setTime(format.parse(dateStr));
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			dayOfWeek = dayOfWeek > 7 ? dayOfWeek - 7 : dayOfWeek;
			return weeks[dayOfWeek - 1];
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	private String getDayOfWeekStrByDayOfWeek(int dayOfWeek) {
		dayOfWeek = dayOfWeek > 7 ? dayOfWeek - 7 : dayOfWeek;
		return weeks[dayOfWeek - 1];
	}

	
	private int isSameDay(Calendar d1,Calendar d2){
		if(d1==null||d2==null)
			return 2;
		if(d1.get(Calendar.YEAR)==d2.get(Calendar.YEAR)&&d1.get(Calendar.MONTH)==d2.get(Calendar.MONTH)&&d1.get(Calendar.DAY_OF_MONTH)==d2.get(Calendar.DAY_OF_MONTH))
		{
			return 0;
		}else if(d1.after(d2))
		{
			return 1;
		}else
		{
			return -1;
		}
	}
	
	/**
	 * 填充上面星期的view
	 * 
	 * @param context
	 */
	private void initDayView(Context context) {
		dayContainer = new LinearLayout(context);
		dayContainer.setOrientation(LinearLayout.HORIZONTAL);
		dayContainer.setPadding(0, DensityUtil.dip2px(context, 21f), 0,
				DensityUtil.dip2px(context, 21f));
		for (int i = 0; i < SHOW_COUNT; i++) {
			TextView tv_day = new TextView(context);
			tv_day.setTextColor(Color.WHITE);
			tv_day.setGravity(Gravity.CENTER);
			tv_day.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
			if (i == 0) {
				tv_day.setAlpha(0.35f);
			} else if (i == 1) {
				tv_day.setAlpha(0.6f);
			} else {
				tv_day.setAlpha(0.6f);
			}
			tv_day.setText(defaultDays[i]);
			dayContainer.addView(tv_day, lp);
		}
		addView(dayContainer, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		dayContainerHeight=getViewWidthAndHeight(dayContainer)[1];
	}

	private void setDayDate(){
		int todayOfWeek=today.get(Calendar.DAY_OF_WEEK);
		for(int i=0;i<SHOW_COUNT;i++)
		{
			TextView tv_day=(TextView) dayContainer.getChildAt(i);
			if(i==0)
			{
				tv_day.setText(R.string.lastday);
			}else if(i==1)
			{
				tv_day.setText(R.string.today);
			}else{
				tv_day.setText(getDayOfWeekStrByDayOfWeek(++todayOfWeek));
			}
		}
	}
	
	private float FIRST_ALPHA=0.35f,OTHER_ALPHA=0.6f;
	
	/**
	 * 填充中间的天气view
	 * 
	 * @param context
	 */
	private void initWeatherView(Context context) {
		weatherContainer = new RelativeLayout(context);
		fillWeatherIcon(context);
		fillWeatherLine(context);
		RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		weatherContainer.addView(weatherIconContainer,rlp);
		weatherContainer.addView(weatherLineContainer, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		addView(weatherContainer, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		weatherContainerHeight=getViewWidthAndHeight(weatherContainer)[1];
	}

	private LinearLayout weatherIconContainer,weatherLineContainer;
	private void fillWeatherIcon(Context context){
		weatherIconContainer=new LinearLayout(context);
		weatherIconContainer.setGravity(Gravity.CENTER_VERTICAL);
		for(int i=0;i<SHOW_COUNT;i++)
		{
			ImageView weatherIcon=createImageView(context, showImgaeId[i]);
			weatherIconContainer.addView(weatherIcon, lp);
			if(i==0)
			{
				weatherIcon.setAlpha(FIRST_ALPHA);
			}else{
				weatherIcon.setAlpha(OTHER_ALPHA);
			}
		}
	}
	
	private void fillWeatherLine(Context context){
		weatherLineContainer=new LinearLayout(context);
		int averageWidth=context.getResources().getDisplayMetrics().widthPixels/SHOW_COUNT;
		for(int i=1;i<SHOW_COUNT;i++)
		{
			LinearLayout.LayoutParams lp=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			ImageView weatherIcon=createImageView(context, R.drawable.fenge_shu);
			lp.leftMargin=averageWidth;
			weatherLineContainer.addView(weatherIcon, lp);
		}
	}
	
	private ImageView createImageView(Context context,int res){
		ImageView imageView=new ImageView(context);
		imageView.setImageResource(res);
		return imageView;
	}
	
	/**
	 * 填充底部的气温view
	 */
	private void initTemperatureView(Context context) {
		temperatureContainer = new LinearLayout(context);
		temperatureContainer.setOrientation(LinearLayout.HORIZONTAL);
		temperatureContainer.setPadding(0, DensityUtil.dip2px(context, 21f), 0,
				DensityUtil.dip2px(context, 21f));
		for (int i = 0; i < SHOW_COUNT; i++) {
			View view=inflate(getContext(), R.layout.temperature_bottom_textview, null);
			TextView tv_temperature = (TextView)view.findViewById(R.id.tv_temperature);
			if (i == 0) {
				view.setAlpha(FIRST_ALPHA);
			} else {
				view.setAlpha(OTHER_ALPHA);
			}
			temperatureContainer.addView(view, lp);
		}
		addView(temperatureContainer, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		temperatureContainerHeight=getViewWidthAndHeight(temperatureContainer)[1];
	}



	/**
	 * 创建入场动画
	 * @param target
	 * @param sourceY
	 * @return
	 */
	private ObjectAnimator createEnterAnimator(View target,float sourceY){
		PropertyValuesHolder pi1 = PropertyValuesHolder.ofFloat("alpha", 0, 1);
		PropertyValuesHolder pi2 = PropertyValuesHolder.ofFloat("y",delY+sourceY, sourceY);
		return ObjectAnimator.ofPropertyValuesHolder(target, pi1,pi2);
	}
	
	/**
	 * 创建出场动画
	 * @param target
	 * @param sourceY
	 * @return
	 */
	private ObjectAnimator createExitAnimator(View target,float sourceY){
		PropertyValuesHolder pi1 = PropertyValuesHolder.ofFloat("alpha", 1, 0);
		PropertyValuesHolder pi2 = PropertyValuesHolder.ofFloat("y",sourceY,delY+sourceY);
		return ObjectAnimator.ofPropertyValuesHolder(target, pi1,pi2);
	}
	
	
	private boolean isNeedInitAnimator=true;
	private AnimatorSet animatorInSet,animatorOutSet,animatorInOutSet,animatorUpdateInSet,animatorUpdateOutSet;
	private void initAnimator(){
		if(isNeedInitAnimator)
		{
			setDayAnimator();
			setWeatherAnimator();
			setTemperatureAnimator();
			isNeedInitAnimator=false;
			animatorInSet=new AnimatorSet();
			animatorOutSet=new AnimatorSet();
			animatorInOutSet=new AnimatorSet();
			animatorUpdateInSet=new AnimatorSet();
			animatorUpdateOutSet=new AnimatorSet();
			animatorInSet.playTogether(dayAnimatorIn,temAnimatorIn,weatherIconAnimatorIn,weatherLineAnimatorIn);
			animatorOutSet.playTogether(temAnimatorOut,dayAnimatorOut,weatherIconAnimatorOut,weatherLineAnimatorOut);
			animatorUpdateInSet.playTogether(dayAnimatorIn,temAnimatorIn,weatherIconAnimatorIn);
			animatorUpdateOutSet.playTogether(dayAnimatorOut,temAnimatorOut,weatherIconAnimatorOut);
			animatorOutSet.addListener(animatorInitListener);
			animatorInOutSet.playSequentially(animatorUpdateOutSet,animatorUpdateInSet);
			animatorUpdateOutSet.addListener(animatorInitListener);
		}
	}
	
	private AnimatorListener animatorInitListener=new AnimatorListener() {
		
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
			// TODO Auto-generated method stub
			initAnimatorProperty();
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			initAnimatorProperty();
		}
	};
	
	private void initAnimatorProperty(){
		dayContainer.setAlpha(1);
		dayContainer.setY(0);
		weatherIconContainer.setAlpha(1);
		weatherLineContainer.setAlpha(1);
		weatherIconContainer.setY(weatherIconSourceY);
		temperatureContainer.setAlpha(1);
		temperatureContainer.setY(temperatureContainerSourceY);
	}
	
	
	public void startInAnimator(){
		initAnimator();
		animatorInSet.start();
	}
	
	public void startOutAnimator(){
		initAnimator();
		animatorOutSet.start();
	}
	
	public void playUpdateAnimator(){
		if(animatorInOutSet!=null&&animatorInOutSet.isRunning())
		{
			return;
		}
		 initAnimator();
		 animatorInOutSet.start();
	}
	
	private int getSameDayIndex(List<WeatherForcastInfo> infos,Calendar day){
		for(int i=0;i<infos.size();i++)
		{
			if(isSameDay(day, formatStr2Calendar(infos.get(i).getWeatherDate()))==0)
			{
				return i;
			}
		}
		return -1;
	}
	
	private String getCalendarStr(Calendar day){
		return day.get(Calendar.YEAR)+"-"+day.get(Calendar.MONTH)+"-"+day.get(Calendar.DAY_OF_MONTH);
	}
	
	private int getSameDayIndex(WeatherForcastInfo info,Calendar day)
	{
		for(int i=0;i<SHOW_COUNT;i++,day.add(Calendar.DAY_OF_MONTH, 1))
		{
			if(isSameDay(day,formatStr2Calendar(info.getWeatherDate()))==0)
			{
				return i;
			}
		}
		return -1;
	}
	
	private void checkForcastInfo(List<WeatherForcastInfo> infos){
		mWeatherForcastInfos.clear();
		if(infos.size()==0)
			return;
		Calendar firstDay=Calendar.getInstance();
		firstDay.add(Calendar.DAY_OF_MONTH, -1);
		int retSame=isSameDay(firstDay, formatStr2Calendar(infos.get(0).getWeatherDate()));
		int index;
		if (retSame == 0) {
			mWeatherForcastInfos.addAll(infos);
		} else if (retSame == 1) {
			index=getSameDayIndex(infos, firstDay);
			if(index==-1)
				return;
			for(int i=0;i<infos.size();i++)
			{
				if(index<=i)
				{
					mWeatherForcastInfos.add(infos.get(i));
				}
				}
			}else if (retSame == -1) {
				index=getSameDayIndex(infos.get(0), firstDay);
				if(index==-1)
					return;
				for(int i=0;i<infos.size();i++)
				{
					if(i>=index)
					{
						mWeatherForcastInfos.add(infos.get(i-index));
					}else{
					    mWeatherForcastInfos.add(null);
					}
				}
			}
	}
	
	private List<WeatherForcastInfo> mWeatherForcastInfos=new ArrayList<WeatherForcastInfo>();
	
	private CityListShowData mCityListShowData;
	public void setData(int dataIndex){
		if(mCityListShowData.getCityWeatherInfo(dataIndex)==null)
		{
			setDefaultValue(getContext());
			setTemperature();
			setWeatherShowIcon();
			return;
		}
		List<WeatherForcastInfo> infos=mCityListShowData.getWeatherForcecasts(dataIndex);
		checkForcastInfo(infos);
		CityConditionAdapter weatherConditionAdapter = new CityConditionAdapter(getContext());
		weatherConditionAdapter.open();
		WeatherForcastInfo info=null;
		for(int i=0;i<SHOW_COUNT;i++)
		{
			if(i>=mWeatherForcastInfos.size())
			{
				info=null;
			}else{
				info=mWeatherForcastInfos.get(i);
			}
			if(info!=null)
			{
				temperatures[i]=((int)info.getLowTemp())+"/"+((int)info.getHighTemp());
				int index=weatherConditionAdapter.queryPicId(info.getWeatherType());
				if(index!=-1)
				{
				    showImgaeId[i] = mSmallWeatherIcon[index];
				}
			}else{
				temperatures[i]=defaultTemperature;
				showImgaeId[i]=R.drawable.smail_char_n;
			}
		}
		weatherConditionAdapter.close();
		setTemperature();
		setWeatherShowIcon();
		setDayDate();
	}
	
	
	
	public void updating(int index){
		setData(index);
	}
	
	/**
	 * 填充温度
	 */
	private void setTemperature(){
		TextView tv_temp;
		for(int i=0;i<SHOW_COUNT;i++)
		{
			tv_temp=(TextView) temperatureContainer.getChildAt(i).findViewById(R.id.tv_temperature);
			tv_temp.setText(temperatures[i]);
		}
	}
	
	/**
	 * 设置天气图标
	 */
	private void setWeatherShowIcon(){
		int childCount=weatherIconContainer.getChildCount();
		ImageView iv_temp;
		for(int i=0;i<childCount;i++)
		{
			iv_temp=(ImageView) weatherIconContainer.getChildAt(i);
			if(showImgaeId[i]!=-1)
			{
			  iv_temp.setImageResource(showImgaeId[i]);
			}
		}
	}
	private ObjectAnimator dayAnimatorIn, dayAnimatorOut;
	private void setDayAnimator() {
		dayAnimatorIn =createEnterAnimator(dayContainer, 0).setDuration(SHORT_DURATION);
		dayAnimatorOut = createExitAnimator(dayContainer, 0).setDuration(LONG_DURATION);
	}
	private Animator weatherIconAnimatorIn,weatherIconAnimatorOut,weatherLineAnimatorIn,weatherLineAnimatorOut;
	private float weatherIconSourceY;
	private void setWeatherAnimator() {
		weatherIconSourceY=getViewWidthAndHeight(weatherLineContainer)[1]/2-getViewWidthAndHeight(weatherIconContainer)[1]/2;
		weatherIconAnimatorIn=createEnterAnimator(weatherIconContainer, weatherIconSourceY).setDuration(SHORT_DURATION);
		weatherIconAnimatorOut=createExitAnimator(weatherIconContainer, weatherIconSourceY).setDuration(SHORT_DURATION);
		weatherLineAnimatorIn=ObjectAnimator.ofFloat(weatherLineContainer, "alpha", 0,1).setDuration(LONG_DURATION);
		weatherLineAnimatorOut=ObjectAnimator.ofFloat(weatherLineContainer, "alpha", 1,0).setDuration(LONG_DURATION);
	}
	
	private ObjectAnimator temAnimatorIn, temAnimatorOut;
	private float temperatureContainerSourceY;
	private void setTemperatureAnimator() {
		temperatureContainerSourceY=dayContainerHeight+weatherContainerHeight;
		temAnimatorIn =createEnterAnimator(temperatureContainer, temperatureContainerSourceY).setDuration(LONG_DURATION);
		temAnimatorOut =createExitAnimator(temperatureContainer, temperatureContainerSourceY).setDuration(SHORT_DURATION);
	}

	public int[] getViewWidthAndHeight(View view){
		int[] wh=new int[2];
		int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		view.measure(width,height);
		wh[0]=view.getMeasuredWidth();
		wh[1]=view.getMeasuredHeight();
		return wh;
	}

}
