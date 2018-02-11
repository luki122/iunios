package views;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherforecast.R;

import interfaces.IWeatherView;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class OptionView extends AbstractWeatherView {
	
	/**
	 *  we should get parentView to call animations
	 */
	private WeatherMainView mParentView;
	
	private ImageView mOptionViewImage;
	private Context mContext;
	
	public OptionView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public OptionView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public OptionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.optionviewimage, this, true);
		mOptionViewImage = (ImageView)this.findViewById(R.id.optionviewimage);
		
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		float distance = DensityUtil.dip2px(mContext, 30);
		ObjectAnimator translateIn = getAnimator(mOptionViewImage, "TranslationX", distance, 0);
		translateIn.setDuration(200);
		translateIn.setInterpolator(new AccelerateInterpolator());
		translateIn.start();
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		
	}
	
	public ImageView getOptionViewImage(){
		return mOptionViewImage;
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		float distance = DensityUtil.dip2px(mContext, 30);
		ObjectAnimator translateIn = getAnimator(mOptionViewImage, "TranslationX", 0, distance);
		translateIn.setDuration(200);
		translateIn.setInterpolator(new AccelerateInterpolator());
		translateIn.start();
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
	 * call this method at mParentView.java constructor
	 * @param parent
	 */
	public void setParent(WeatherMainView parent)
	{
		this.mParentView = parent;
	}
	
	@Override
	public void setOnClickListener(OnClickListener l) {
		// TODO Auto-generated method stub
		//super.setOnClickListener(l);
		mOptionViewImage.setOnClickListener(l);
		if (mParentView != null) {
			mParentView.stateChanged(IWeatherView.COMING_OUT);
		}
	}
}
