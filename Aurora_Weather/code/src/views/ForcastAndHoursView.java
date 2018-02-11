package views;

import java.util.ArrayList;

import interfaces.IWeatherView;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;

public class ForcastAndHoursView extends AbstractWeatherView {

	private LinearLayout fr_content;
	private ForecastContentView mForecastContentView;
	private HoursContents mHoursContents;
	private Forecast24hAirView mForecast24hAirView;
	private ImageView iv_line_left,iv_line_right;
	private static int frContentHeight = 0;
	public ForcastAndHoursView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public ForcastAndHoursView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ForcastAndHoursView(Context context) {
		super(context);
		initView(context);
	}

	public void changeContent(int select){
		switch (select) {
		case Forecast24hAirView.FORCAST_SELECT:
			if(mForecastContentView.getVisibility()==View.VISIBLE)
			{
				return;
			}
			mForecastContentView.setVisibility(View.VISIBLE);
			mForecastContentView.startEntryAnim();
			mHoursContents.setVisibility(View.INVISIBLE);
			break;
		case Forecast24hAirView.HOUR_24_SELECT:
			if(!isSetbg)
			{
				int[] location=new int[2];
				fr_content.getLocationOnScreen(location);
				mHoursContents.setHideViewBg(bgDrawable,location[1]);
				isSetbg=true;
			}
			if(mHoursContents.getVisibility()==View.VISIBLE)
				return;
			mHoursContents.startEntryAnim();
			mHoursContents.setVisibility(View.VISIBLE);
			mForecastContentView.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void setIndex(int index) {
		// TODO Auto-generated method stub
		super.setIndex(index);
		
		if(mCityListShowData.getCitySize() == 0)
		{
			iv_line_left.setVisibility(View.INVISIBLE);
			iv_line_right.setVisibility(View.VISIBLE);
		}else{
			if(index == 0)
			{
				iv_line_left.setVisibility(View.INVISIBLE);
			}else{
				iv_line_left.setVisibility(View.VISIBLE);
			}
			
			if(index == mCityListShowData.getCitySize()-1)
			{
				iv_line_right.setVisibility(View.INVISIBLE);
			}else{
				iv_line_right.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void exit(int select){
		switch (select) {
		case Forecast24hAirView.FORCAST_SELECT:
			mForecastContentView.startExitAnim();
			break;
		case Forecast24hAirView.HOUR_24_SELECT:
			mHoursContents.startExitAnim();
			break;
		default:
			break;
		}
	}
	
	private Drawable bgDrawable;
	private boolean isSetbg=false;
	public void setHideViewBg(Drawable d){
		bgDrawable=d;
	}
	
	public int getViewHeight(View view){
		return View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
	}
	
	private void initView(final Context context){
		LayoutInflater.from(context).inflate(R.layout.forcast_hours_layout, this);
		iv_line_left=(ImageView)findViewById(R.id.iv_line_left);
		iv_line_right=(ImageView)findViewById(R.id.iv_line_right);
		fr_content=(LinearLayout) findViewById(R.id.fr_content);
		ViewTreeObserver obser = fr_content.getViewTreeObserver();
		obser.addOnPreDrawListener(new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
				if(frContentHeight == 0)
				{
					// TODO Auto-generated method stub
					frContentHeight = fr_content.getMeasuredHeight();
					((AuroraWeatherMain)context).setBottomViewHeight(frContentHeight);
				}
				return true;
			}
		});
//		mForecastContentView=new ForecastContentView(context);
//		mForecast24hAirView=(Forecast24hAirView)findViewById(R.id.mForecast24hAirView);
//		mHoursContents=new HoursContents(context);
//		childs.add(mHoursContents);
//		mHoursContents.setVisibility(View.INVISIBLE);
//		fr_content.addView(mForecastContentView);
	}
	
	@Override
	public void startEntryAnim() {
		super.startEntryAnim();
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		super.startExitAnim();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		frContentHeight = 0;
	}
	
}
