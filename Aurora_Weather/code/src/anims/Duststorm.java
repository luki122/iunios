package anims;

import com.aurora.weatherdata.util.Log;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;

import android.graphics.Point;
import android.view.SurfaceView;
import anims.AnimParams.AnimRateInterface;
import aurora.view.AuroraGLSurfaceView;
public class Duststorm extends HazeOrFoggyOtherAnim {
	private float timeRates[]={0.00002f,0.00004f,0.0006f,0.0006f};
	public Duststorm(SurfaceView view) {
		super(view);
		init();
	}

	@Override
	protected void init() {
		resIds=new int[]{R.drawable.duststorm_back,R.drawable.duststorm_middle,R.drawable.duststorm_first,R.drawable.duststorm_first};
		mTypeNumbers=new int[]{1,1,1,1};
		mMaxNumbers=4;
		super.init();
	}
	
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
		if(AuroraWeatherMain.mIsDayTime)
		{
			params.setAlpha(1);
		}else{
			params.setAlpha(0.6f);
		}
		Point location=params.getLocation();
		float rate=params.getRateXValue();
		int parentWidth=getParentWidth();
		int drawableWidth=params.getShowDrawable().getIntrinsicWidth();
		location.set((int) (parentWidth-rate*(parentWidth+drawableWidth)),location.y);
	}
	
}
