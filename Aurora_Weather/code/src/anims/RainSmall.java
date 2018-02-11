package anims;

import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

public class RainSmall extends RainAnim {
	
	
	public RainSmall(SurfaceView view) {
		super(view);
		init();
	}
	
	@Override
	protected void init() {
		super.mTypeNumbers = new int[]{
				10,
				10,
				45
			};
		
		super.mMaxNumbers = 65;
		
		super.init();
	}
	
	
	@Override
	protected void initParams(AnimParams params , int index) {
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
