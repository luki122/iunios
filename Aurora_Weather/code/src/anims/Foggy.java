package anims;

import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
public class Foggy extends HazeOrFoggyOtherAnim {

	public Foggy(SurfaceView view) {
		super(view);
		init();
	}

	@Override
	protected void init() {
		resIds=new int[]{R.drawable.foggy_back,R.drawable.foggy_front};
		mTypeNumbers = new int[] { 1, 1 };
		mMaxNumbers = 2;
		timeRate=0.0004f;
		alpha=1;
		super.init();
	}
	
	@Override
	protected void resetParamsBeforeDraw(AnimParams params, int index) {
		if(AuroraWeatherMain.mIsDayTime)
		{
			params.setAlpha(1);
		}else{
			params.setAlpha(0.6f);
		}
		super.resetParamsBeforeDraw(params, index);
	}
}
