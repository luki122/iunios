package anims;

import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

public class SnowMiddle extends SnowAnim{

	public SnowMiddle(SurfaceView view) {
		super(view);

		init();
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.mTypeNumbers = new int[]{
				3,
				7,
				10,
				10,
				30,
				40
		};

		super.init();
	}

	@Override
	protected void initParams(AnimParams params , int index) {
		// TODO Auto-generated method stub
		super.initParams(params, index);
		params.setDefaultTimeRate(0.0011f);
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
