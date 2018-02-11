package anims;

import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

public class SnowSmall extends SnowAnim {
	
	
	public SnowSmall(SurfaceView view) {
		super(view);
		init();
	}
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.mTypeNumbers = new int[]{
				4,
				4,
				5,
				5,
				10,
				25
			};
		
		super.init();
	}
	
	
	@Override
	protected void initParams(AnimParams params , int index) {
		super.initParams(params, index);
		params.setDefaultTimeRate(0.0006f);
		params.setRateInterface(new AnimParams.AnimRateInterface() {
			
			@Override
			public float getRateY(float time) {
				return time;
			}
			
			@Override
			public float getRateX(float time) {
				return 0;
			}
		});
	}

	@Override
	protected void resetParamsBeforeDraw(AnimParams params , int index) {
		super.resetParamsBeforeDraw(params,index);
		
	}
}

