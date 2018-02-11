package views;

import interfaces.IWeatherView;

import java.util.ArrayList;
import java.util.List;

import com.aurora.weatherforecast.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class WeatherMainView extends AbstractWeatherView {
	private Context mContext;
	private WeatherDetailsView mWeatherDetailsView;
	private ForcastAndHoursView mForcastAndHoursView;
	
	public WeatherMainView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}

	public WeatherMainView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
	}

	public WeatherMainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.weather_main_view, this, true);
		mWeatherDetailsView = (WeatherDetailsView)this.findViewById(R.id.weatherdetailsview);
		mForcastAndHoursView=(ForcastAndHoursView)this.findViewById(R.id.mForcastAndHoursView);
	}
	
	public WeatherDetailsView getWeatherDetailsView( ) {
		return mWeatherDetailsView;
	}
	@Override
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		mForcastAndHoursView.setHideViewBg(background);
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUpdating() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * state changed , Notice children update surface!!!
	 * @param state
	 */
	public void stateChanged(int state)
	{
		super.onStateChanged(state);
		
	}
}
