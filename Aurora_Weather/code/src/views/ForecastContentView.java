package views;

import datas.CityListShowData;
import datas.WeatherData;
import android.content.Context;
import android.util.AttributeSet;

public class ForecastContentView extends AbstractWeatherView {

	private ForcastContentContainer mForcastContentContainer;
	public ForecastContentView(Context context) {
		super(context);
		initView(context);
	}

	public ForecastContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	//time consuming = 5
	public ForecastContentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}
	
	
	private void initView(Context context){
		mForcastContentContainer=new ForcastContentContainer(context);
		addView(mForcastContentContainer, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		if(mForcastContentContainer!=null)mForcastContentContainer.startInAnimator();
	}
	
	
	@Override
	public void onUpdating() {
		super.onUpdating();
		if(mForcastContentContainer!=null)mForcastContentContainer.playUpdateAnimator();
		if(mForcastContentContainer!=null)mForcastContentContainer.updating(getIndex());
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		if(mForcastContentContainer!=null)mForcastContentContainer.setData(getIndex());
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		if(mForcastContentContainer!=null)mForcastContentContainer.startOutAnimator();
	}
}
