package anims;

import java.util.Calendar;

import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherdata.util.SystemUtils;
import com.aurora.weatherforecast.AuroraWeatherMain;
import com.aurora.weatherforecast.R;
import com.aurora.weatherforecast.WeatherMainFragment;

public class Sunny extends HazeOrFoggyOtherAnim {

	// 光晕移动的最小坐标
	private static final int minPos = 0;
	
	// 光晕移动的最大坐标
	private static final int maxPos = 14;
	
	//出现两次流星之间的间隔时间
	private static final int METEOR_INTERVAL = 8000;
	
	// 大流星移动的速度（步长）
	private static final int LARGE_METEOR_STEP_LENGTH = 12;
	
	// 大流星出现一次最多被绘制的次数
	private static final int LARGE_METEOR_MAX_DRAW_COUNT = 55;
	
	// 大光晕当前的坐标
	private int locationHaloBg;
	
	// 小光晕当前的坐标
	private int locationHaloLittle;
	
	// 大小光晕移动的增量
	private int bgIncrement;
	private int littleIncrement;
	
	// 距离最近一次移动光晕后界面刷新的次数
	private int drawCountAfterLastMoving;

	// 增加或者减小光晕的alpha值
	private boolean upAlpha;
	
	// 是否需要画白天动画
	private boolean drawDaytimeAnims;
	
	// 蓝光的alpha
	private float blueLightAlpha;

	// 大小流星的起始位置
	private int smallMeteorOrigX;
	private int smallMeteorOrigY = 100;
	private int largeMeteorOrigX;
	private int largeMeteorOrigY;

	// 大流星被画的次数
	private int smallMeteorDrawCount;
	
	// 小流星被画的次数
	private int largeMeteorDrawCount;
	
	// 最近一次大流星划过的时间
	private long lastLargeMeteorFinishedTime;
	
	// 最近一次小流星划过的时间
	private long lastSmallMeteorFinishedTime;

	public Sunny(SurfaceView view) 
	{
		super(view);
		init();
	}

	@Override
	protected void init() 
	{
		resIds = new int[] { R.drawable.sunny_halo_bg,
				R.drawable.sunny_halo_little, R.drawable.sunny_blue_light,
				R.drawable.meteor_small, R.drawable.meteor_large };
		mTypeNumbers = new int[] { 1, 1, 1, 1, 1 };
		mMaxNumbers = resIds.length;

		super.init();
	}

	@Override
	protected void initParams(AnimParams params, int index) 
	{
		if (index <= 2) 
		{
			// 白天
			params.setLocation(new Point(0, 0));
			doDrawAnims();
			if (AuroraWeatherMain.mIsDayTime) 
			{
				if (index == 2) 
				{
					params.setAlpha(0);
				}
			} 
			else 
			{
				params.setAlpha(0);
			}
		} 
		else 
		{
			// 晚上
			if (index == 3) 
			{
				smallMeteorOrigX = getParentWidth()
						/ 2
						- (int) (params.getShowDrawable().getIntrinsicHeight() / Math
								.sqrt(2));
				params.setTranslationY(-DensityUtil.dip2px(mContext, 80));
				params.setLocation(new Point(smallMeteorOrigX, smallMeteorOrigY));
			} 
			else 
			{
				largeMeteorOrigX = getParentWidth() * 3 / 4;
				params.setTranslationY(-DensityUtil.dip2px(mContext, 140));
				params.setTranslationX(DensityUtil.dip2px(mContext, 30));
				params.setLocation(new Point(largeMeteorOrigX, largeMeteorOrigY));
			}
			params.setAlpha(0);
			params.setRotate(-45);
			//params.setTranslationY(-DensityUtil.dip2px(mContext, 80));
			smallMeteorDrawCount = 0;
			largeMeteorDrawCount = 0;
		}
	}

	/**
	 * 设置开始绘画动画
	 */
	private void doDrawAnims() 
	{
		bgIncrement = 1;
		littleIncrement = -bgIncrement;
		locationHaloBg = minPos;
		locationHaloLittle = maxPos;
		drawDaytimeAnims = true;
		upAlpha = true;
		drawCountAfterLastMoving = 0;
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		Log.e("222222", "------Sunny onResume--");
		if(!drawDaytimeAnims) {
			//刷新当前页会调用onResume()方法两次，会导致光晕卡一下的情况出现。为了避免这种情况，规定如果正在播放白天动画，则不让进行参数重置
			doDrawAnims();
		}
	}

	@Override
	protected void resetParamsBeforeDraw(AnimParams params, int index) 
	{
		if (AuroraWeatherMain.mIsDayTime) 
		{
			// 绘制光晕
			if (drawDaytimeAnims) 
			{
				if (index < 2) 
				{
					// 绘制大小光晕
					if (params.getAlpha() == 0) 
					{
						params.setAlpha(1);
					}
					if (index == 0) 
					{
						if (drawCountAfterLastMoving % 2 == 0) 
						{
							params.setLocation(new Point(locationHaloBg,
									locationHaloBg));
							locationHaloBg += bgIncrement;
							if (locationHaloBg == maxPos
									|| locationHaloBg == minPos) 
							{
								bgIncrement = -bgIncrement;
							}
						}
					} 
					else if (index == 1) 
					{
						if (drawCountAfterLastMoving % 2 == 0) 
						{
							params.setLocation(new Point(locationHaloLittle,
									locationHaloLittle));
							locationHaloLittle += littleIncrement;
							if (locationHaloLittle == maxPos
									|| locationHaloLittle == minPos) 
							{
								littleIncrement = -littleIncrement;
							}
							drawCountAfterLastMoving = 0;
						}
						drawCountAfterLastMoving++;
					}
					if (!upAlpha) 
					{
						params.setAlpha(blueLightAlpha);
						//Log.e("222222", "------Sunny 111blueLightAlpha = --" + blueLightAlpha);
					}
				} 
				else if (index == 2) 
				{
					// 绘制蓝光
					blueLightAlpha = params.getAlpha();
					if (upAlpha) 
					{
						blueLightAlpha += 0.03;
						//Log.e("222222", "------Sunny 222blueLightAlpha = --" + blueLightAlpha);
						if (blueLightAlpha < 1) 
						{
							params.setAlpha(blueLightAlpha);
						} 
						else 
						{
							blueLightAlpha = 1;
							upAlpha = false;
						}
					} 
					else 
					{
						//Log.e("222222", "------Sunny 333blueLightAlpha = --" + blueLightAlpha);
						blueLightAlpha -= 0.03;
						if (blueLightAlpha > 0) 
						{
							params.setAlpha(blueLightAlpha);
						} 
						else 
						{
							blueLightAlpha = 0;
							upAlpha = true;
							drawDaytimeAnims = false;
						}
					}
				}
			} 
			else 
			{
				// 绘制完后，将光晕设为不可见
				params.setAlpha(0);
			}

		} 
		else 
		{
			if (!isDuskNow()) 
			{
				// 非黄昏时间，绘制流星
				long crtTime = System.currentTimeMillis();
				if (index == 4) 
				{
					if (crtTime - lastLargeMeteorFinishedTime >= METEOR_INTERVAL) 
					{
						params.setAlpha(getAlphaByDrawCount(params.getAlpha(),
								largeMeteorDrawCount,
								LARGE_METEOR_MAX_DRAW_COUNT));
						int stepLength = getMeteorStepLength(
								largeMeteorDrawCount, LARGE_METEOR_STEP_LENGTH);
						//int x = params.getLocation().x - stepLength;
						int x = params.getLocation().x;
						int y = params.getLocation().y + stepLength;
						params.setLocation(new Point(x, y));
						if (largeMeteorDrawCount >= LARGE_METEOR_MAX_DRAW_COUNT) 
						{
							params.setAlpha(0);
							params.setLocation(new Point(largeMeteorOrigX,
									largeMeteorOrigY));
							lastLargeMeteorFinishedTime = crtTime;
							largeMeteorDrawCount = 0;
						}
						largeMeteorDrawCount++;
					}
				} 
				else if (index == 3) 
				{
					// 首次进入，小流星在大流星划过之后再运动
					if (lastLargeMeteorFinishedTime != 0) 
					{
						if (crtTime - lastSmallMeteorFinishedTime >= METEOR_INTERVAL * 3 / 5) 
						{
							int maxDrawCount = LARGE_METEOR_MAX_DRAW_COUNT / 5 * 4;
							params.setAlpha(getAlphaByDrawCount(
									params.getAlpha(), smallMeteorDrawCount,
									maxDrawCount));
							int stepLength = getMeteorStepLength(
									smallMeteorDrawCount,
									LARGE_METEOR_STEP_LENGTH - 3);
							//int x = params.getLocation().x - stepLength;
							int x = params.getLocation().x;
							int y = params.getLocation().y + stepLength;
							params.setLocation(new Point(x, y));
							if (smallMeteorDrawCount >= maxDrawCount) 
							{
								params.setAlpha(0);
								params.setLocation(new Point(smallMeteorOrigX,
										smallMeteorOrigY));
								lastSmallMeteorFinishedTime = crtTime;
								smallMeteorDrawCount = 0;
							}
							smallMeteorDrawCount++;
						}
					}
				}
			}
		}
	}

	/**
	 *  计算流星的移动速度
	 * @param crtDrawCount
	 * @param origOffset
	 * @return
	 */
	private int getMeteorStepLength(int crtDrawCount, int origOffset) 
	{
		if (crtDrawCount / 10 <= 1) 
		{
			return origOffset;
		} 
		else if (crtDrawCount / 10 <= 2) 
		{
			return origOffset + 3;
		} 
		else 
		{
			return origOffset + 5;
		}
	}

	/**
	 * 获取流星移动过程中的透明度
	 * 
	 * @param alpha
	 * @param crtDrawCount
	 * @param maxDrawCount
	 * @return
	 */
	private float getAlphaByDrawCount(float alpha, int crtDrawCount,
			int maxDrawCount) 
	{
		float alphaStep = 1.0f / (maxDrawCount / 2);
		
		if (crtDrawCount <= (maxDrawCount / 2)) 
		{
			return alpha + alphaStep;
		} 
		else 
		{
			return alpha - alphaStep;
		}
	}
	
	/**
	 * 判断现在是否是黄昏时间（18：00～20：00）
	 * 
	 * @return
	 */
	private boolean isDuskNow() 
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int hour = c.get(Calendar.HOUR_OF_DAY);
		if (hour >= WeatherMainFragment.MARK_HOUR_2
				&& hour < WeatherMainFragment.MARK_HOUR_4) 
		{
			return true;
		}
		return false;
	}

}