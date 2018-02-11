package anims;

import views.AbstractWeatherAnim;

import com.aurora.weatherdata.util.Log;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.SurfaceView;
import android.widget.Toast;
import anims.AnimParams.AnimRateInterface;
import aurora.opengl.AuroraGLDrawable;
import aurora.view.AuroraGLSurfaceView;

import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
/**
 * 霾动画
 * @author j
 *
 */
public class Haze extends HazeOrFoggyOtherAnim {
	public Haze(SurfaceView view) {
		super(view);
		init();
	}
	protected void init()
	{
		resIds=new int[]{R.drawable.haze_back,R.drawable.haze_front};
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
		super.resetParamsBeforeDraw(params, index);
	}
}
