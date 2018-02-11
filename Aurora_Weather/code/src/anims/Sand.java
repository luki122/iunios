package anims;

import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
public class Sand extends HazeOrFoggyOtherAnim {
	
	public Sand(SurfaceView view) {
		super(view);
		init();
	}

	protected void init()
	{
		resIds=new int[]{R.drawable.sand_back,R.drawable.sand_front};
		mTypeNumbers = new int[] { 1, 1 };
		mMaxNumbers = 2;
		timeRate=0.0006f;
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
		// TODO Auto-generated method stub
		super.resetParamsBeforeDraw(params, index);
	}
}
