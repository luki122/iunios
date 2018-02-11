package anims;

import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

public class RainLarge extends RainAnim{
	
	//雨旋转的角度
	private static final int ROTATE_DEGRESS = 15;

	public RainLarge(SurfaceView view) {
		super(view);
		
		init();
	}
	
	@Override
	protected void init() {
		super.mTypeNumbers = new int[]{
				40,
				20,
				10
			};
		
		super.mMaxNumbers = 70;
		
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
		
		//将雨滴旋转一定的度数
		params.setRotate(ROTATE_DEGRESS);
		params.setTranslationX(-250);
	}

	@Override
	protected void resetParamsBeforeDraw(AnimParams params , int index) {
		//雨滴移动之前，保存当前雨滴的Y值
		int yPreMove = params.getLocation().y;
		
		//在Y轴方向上移动雨滴
		super.resetParamsBeforeDraw(params,index);
		
		//雨滴移动之后，保存当前雨滴的Y值
		int yPostMove = params.getLocation().y;
		
		//将雨滴在X轴上偏移相应的距离
		//params.getLocation().x = params.getLocation().x + (int)((yPostMove - yPreMove) * Math.tan(ROTATE_DEGRESS * Math.PI / 180));
	}

}
