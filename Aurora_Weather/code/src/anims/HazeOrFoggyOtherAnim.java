package anims;


import android.graphics.Point;
import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;
import views.AbstractWeatherAnim;

import com.aurora.weatherforecast.R;
/**
 * 晴/阴/多云/雾/霾
 * @author j
 *
 */
public class HazeOrFoggyOtherAnim extends AbstractWeatherAnim {

	protected int[] resIds;
	protected int[] mTypeNumbers;
	protected int mMaxNumbers;
	protected float timeRate;
	protected float alpha;
	public HazeOrFoggyOtherAnim(SurfaceView view) {
		super(view);
		DEL_PX=view.getResources().getDisplayMetrics().widthPixels/3;
	}
	protected void init()
	{
		setContendIds(resIds);
		
		setTypeNumbers(mTypeNumbers);
		
		setMaxDrawableNum(mMaxNumbers);
		
		initDrawablesThrowIds();
		
		initAnimParams();
	}
	
	private boolean isRevers[] = new boolean[2];
	private final int DEL_PX;
	
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
			params.setTime(0);
			break;
		case 1:
			location.set(delWidth, 0);
			params.setTime(0);
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
		int drawableWidth = params.getShowDrawable().getIntrinsicWidth();
		int parentWidth = super.getParentWidth();
        int delWidth=parentWidth-drawableWidth;
		float rate = params.getRateXValue();
		Point location=params.getLocation();
        switch (index) {
		case 0:
			if(isRevers[0])
			{
				location.set((int)(DEL_PX*(rate-1)), 0);
				if(params.getTime()>0.99f)
				{
					params.setTime(0);
					isRevers[0]=false;
				}
			}else{
				location.set((int) (DEL_PX*(-rate)), 0);
				if(params.getTime()>0.99f)
				{
					params.setTime(0);
					isRevers[0]=true;
				}
			}
			break;
		case 1:
			if(isRevers[1])
			{
				location.set((int)(delWidth+DEL_PX*(1-rate)), 0);
				if(params.getTime()>0.99f)
				{
					isRevers[1]=false;
					params.setTime(0);
				}
			}else{
				location.set((int) (delWidth+DEL_PX*rate), 0);
				if(params.getTime()>0.99f)
				{
					isRevers[1]=true;
					params.setTime(0);
				}
			}
			break;
		default:
			break;
		}
	}

}
