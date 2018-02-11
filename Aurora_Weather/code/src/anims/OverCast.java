package anims;

import android.graphics.Point;
import android.location.Location;
import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;
import views.AbstractWeatherAnim;

import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
/**
 * 阴天气动画
 * @author j
 *
 */
public class OverCast extends HazeOrFoggyOtherAnim {
	protected void init() {
		resIds = new int[] { R.drawable.overcast_back,
				R.drawable.overcast_front };
		mTypeNumbers = new int[] { 1, 1 };
		mMaxNumbers = 2;
		timeRate=0.001f;
		alpha=1;
		super.init();
	}


	@Override
	protected void initParams(AnimParams params, int index) {
		int drawWidth = params.getShowDrawable().getIntrinsicWidth();
		int parentWidth = super.getParentWidth();
		int delWidth=parentWidth-drawWidth;
		Point location=params.getLocation();
		params.setDefaultTimeRate(timeRate);
		params.setAlpha(alpha);
		switch (index) {
		case 0:
			location.set(0, 0);
			params.setTime(0.4f);
			break;
		case 1:
			location.set(delWidth, 0);
			params.setTime(0.4f);
			break;
		}
		params.setRateInterface(new AnimParams.AnimRateInterface() {

			@Override
			public float getRateY(float time) {
				return 0;
			}
			@Override
			public float getRateX(float time) {
				// TODO Auto-generated method stub
				return time;
			}
		});
	}
	
	@Override
	protected void resetParamsBeforeDraw(AnimParams params, int index) {
		if(AuroraWeatherMain.mIsDayTime)
		{
			params.setAlpha(0.7f);
		}else{
			params.setAlpha(0.3f);
		}
		// TODO Auto-generated method stub
		super.resetParamsBeforeDraw(params, index);
	}
	
	public OverCast(SurfaceView view) {
		super(view);
		init();
	}
}
