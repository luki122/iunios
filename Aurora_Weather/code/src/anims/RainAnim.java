package anims;


import org.codehaus.jackson.mrbean.MrBeanModule;

import com.aurora.weatherforecast.R;

import android.util.Log;
import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;
import views.AbstractWeatherAnim;

public class RainAnim extends AbstractWeatherAnim {
	
	private int resIds[] = new int[]{
		R.drawable.rain_large,
		R.drawable.rain_middle,
		R.drawable.rain_small
	};
	
	protected int mTypeNumbers[];
	
	protected int mMaxNumbers;
	
	private int mHeight = 0;
	
	private final int INIT_DISTANCE = -43;
	private final int RATE = 42;
	
	public RainAnim(SurfaceView view) {
		super(view);
		// TODO Auto-generated constructor stub
		
		
	}
	
	protected void init()
	{
		setContendIds(resIds);
		
		setTypeNumbers(mTypeNumbers);
		
		setMaxDrawableNum(mMaxNumbers);
		
		initDrawablesThrowIds();
		
		initAnimParams();
		isRainAnim = true;
	}
	
	@Override
	protected void initParams(AnimParams params, int index) {
		// TODO Auto-generated method stub
		params.setAlpha(1.0f);
		params.setRotate(0);
		params.setDefaultTimeRate(0.025f);
		
	}

	@Override
	protected void resetParamsBeforeDraw(AnimParams params, int index) {
		// TODO Auto-generated method stub
		int drawableHeight = params.getShowDrawableHeight();
		
		int height = super.getParentHeight() + drawableHeight;
		
		float rate = getRateByIndex(index, params.getRateYValue());
		
		params.getLocation().y = (int) (rate * height) - drawableHeight;
		
		if ( 1.0f - rate >= 0 ) {
			params.setAlpha((1.0f - rate) * 0.8f);
		} else {
			params.setAlpha(0);
		}
		
//		float rate = getRateByIndex(index);
//		
//		if ( params.getLocation().y == 0 ) {
//			params.getLocation().y = INIT_DISTANCE;
//		}
//		
//		params.getLocation().y += rate;
		
//		if ( index == 0 ) {
//			Log.e("333333", "---RainAnim params.getLocation().y---" + params.getLocation().y);
//		}
		
		//params.setAlpha(((float)(super.getParentHeight() - params.getLocation().y))/super.getParentHeight());
	}
	
	private int getRateByIndex(int index) {
		int rate = 0;
		if (index < getSumOfTypeUtil(0)) {
			rate = RATE;
		} else if (getSumOfTypeUtil(0) <= index && index < getSumOfTypeUtil(1)) {
			rate = RATE;
		} else if (getSumOfTypeUtil(1) <= index && index < getSumOfTypeUtil(2)) {
			rate = RATE;
		}
		return rate;
	}

	/**
	 * 计算雨的下降速率
	 * 
	 * @param index
	 * @param rate
	 * @return
	 */
	private float getRateByIndex(int index, float rate) {
		if (index < getSumOfTypeUtil(0)) {
			rate = rate + 0.005f;
		} else if (getSumOfTypeUtil(0) <= index && index < getSumOfTypeUtil(1)) {
			rate = rate + 0.015f;
		} else if (getSumOfTypeUtil(1) <= index && index < getSumOfTypeUtil(2)) {
			rate = rate + 0.025f;
		}
		return rate;
	}
	
	/**
	 * 获取kind种类之前的雨的总数（包括kind）
	 * 
	 * @param kind
	 * @return
	 */
	private int getSumOfTypeUtil(int kind) {
		int sum = 0;
		if (mTypeNumbers != null) {
			for (int i = 0; i <= kind; i++) {
				sum += mTypeNumbers[i];
			}
		}
		return sum;
	}

}
