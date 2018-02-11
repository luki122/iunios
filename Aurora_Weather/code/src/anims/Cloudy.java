package anims;

import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceView;
import anims.AnimParams.AnimRateInterface;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
/**
 * 多云动画
 * @author j
 *
 */
public class Cloudy extends HazeOrFoggyOtherAnim {
	private float timeRates[]={0.00002f,0.0001f,0.0002f,0.0002f};
	public Cloudy(SurfaceView view) {
		super(view);
		init();
	}

	@Override
	protected void init() {
		resIds = new int[] { R.drawable.cloudy_back,R.drawable.cloudy_middle,
				R.drawable.cloudy_front,R.drawable.cloudy_front};
		mTypeNumbers = new int[] { 1,1,1,1};
		mMaxNumbers = 4;
		super.init();
	}
	
	private int beforeDrawableWidth;
	
    private AnimRateInterface mAnimRateInterface=new AnimRateInterface() {
		
		@Override
		public float getRateY(float time) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public float getRateX(float time) {
			// TODO Auto-generated method stub
			return time;
		}
	};
	@Override
	protected void initParams(AnimParams params, int index) {
		params.setRateInterface(mAnimRateInterface);
		Point location=params.getLocation();
		if(index==3)
		{
			 location.set(getParentWidth(), 0);
			 params.setTime(0f);
		}else if(index==2)
		{
			location.set(getParentWidth(), 0);
			params.setTime(params.getShowDrawable().getIntrinsicWidth()/(float)(getParentWidth()+params.getShowDrawable().getIntrinsicWidth()));
		}else{
		      location.set(0, 0);
		      params.setTime(0.5f);
		}
		params.setDefaultTimeRate(timeRates[index]);
	}
	@Override
	protected void resetParamsBeforeDraw(AnimParams params, int index) {
		// TODO Auto-generated method stub
		if(AuroraWeatherMain.mIsDayTime)
		{
			if(index==2||index==3)
			{
			 params.setAlpha(0.8f);
			}else{
				params.setAlpha(1f);
			}
		}else{
			if(index==2||index==3)
			{
			 params.setAlpha(0.4f);
			}else{
				params.setAlpha(0.6f);
			}
		}
		Point location=params.getLocation();
		float rate=params.getRateXValue();
		int parentWidth=getParentWidth();
		int drawableWidth=params.getShowDrawable().getIntrinsicWidth();
		location.set((int) (parentWidth-rate*(parentWidth+drawableWidth)),location.y);
		
	}
}
