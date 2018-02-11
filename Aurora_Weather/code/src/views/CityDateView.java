package views;

import java.util.Calendar;
import java.util.TimeZone;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.WeatherData;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CityDateView extends AbstractWeatherView {
	
	private Context mContext;
	private TextView mDateTimeView;
	private TextView mCityTextView;
	private Calendar mCalendar;
	private String mFormat;
	private LinearLayout mCityDateViewLinear;
	
	private final static String M24 = "kk:mm";
	
	private int mAnimator_mode = 0;
	
	private boolean updateFlag = false;
	
	private boolean isAnimRun;

	public CityDateView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public CityDateView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public CityDateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.citydateview, this, true);
		mCityDateViewLinear = (LinearLayout)this.findViewById(R.id.citydateviewlinear);
		mCityTextView = (TextView)this.findViewById(R.id.citytextview);
		String firstPageCityName = ((AuroraWeatherMain)context).getFirstPageCity();
		if(firstPageCityName!=null)
		{
			mCityTextView.setText(firstPageCityName);
		}
		mDateTimeView = (TextView)this.findViewById(R.id.dateandtimetextview);
		//mCityTextView.setText("深圳");
	}
	
	public TextView getCityTextView( ) {
		return mCityTextView;
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		
		if (isAnimRun) {
			return;
		}
		
		float distance = DensityUtil.dip2px(mContext, 40);
		
		ObjectAnimator translateIn = getAnimator(mCityTextView, "TranslationY", -distance, 0);
		ObjectAnimator alphaIn = getAnimator(mCityTextView, "Alpha", 0, 1);
		translateIn.setDuration(400);
		
		ObjectAnimator translateIn2 = getAnimator(mDateTimeView, "TranslationY", -distance, 0);
		ObjectAnimator alphaIn2 = getAnimator(mDateTimeView, "Alpha", 0, 1);
		translateIn2.setDuration(400);
		
		AnimatorSet animator = new AnimatorSet();
		animator.setInterpolator(new DecelerateInterpolator());
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
		animator.play(translateIn).with(translateIn2).with(alphaIn).with(alphaIn2);
		animator.start();
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		mCityTextView.setText(mCityListShowData.getWeatherDateItem(getIndex()).getCityName());
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		
		if (isAnimRun) {
			return;
		}
		
		float distance = DensityUtil.dip2px(mContext, 40);
		
		ObjectAnimator translateIn = getAnimator(mCityTextView, "TranslationY", 0, distance);
		ObjectAnimator alphaIn = getAnimator(mCityTextView, "Alpha", 1, 0);
		
		ObjectAnimator translateIn2 = getAnimator(mDateTimeView, "TranslationY", 0, distance);
		ObjectAnimator alphaIn2 = getAnimator(mDateTimeView, "Alpha", 1, 0);
		
		AnimatorSet animator = new AnimatorSet();
		animator.setInterpolator(new DecelerateInterpolator());
		animator.setDuration(400);
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
		animator.play(translateIn).with(translateIn2).with(alphaIn).with(alphaIn2);
		animator.start();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUpdating() {
		// TODO Auto-generated method stub
		updatingViews( );
	}
	
	private void updatingViews( ) {
		updateFlag = true;
		startExitAnim();
	}
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
        
        updateTime();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mCalendar = Calendar.getInstance();
		setDateFormat();
	}
	
    private void setDateFormat() {
    	mFormat = CityDateView.M24;
    }
    
    public void updateTime() {
    	setDateFormat();
    	mCalendar.setTimeZone(TimeZone.getDefault());
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
		
		String currentTime = String.valueOf(mCalendar.get(Calendar.MONTH) + 1) + "月" + 
				mCalendar.get(Calendar.DAY_OF_MONTH) + "日   " + newTime;
		
		mDateTimeView.setText(currentTime);
    }
	
}
