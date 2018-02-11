package anims;

import views.AbstractWeatherAnim;
import android.view.SurfaceView;
import aurora.view.AuroraGLSurfaceView;

import com.aurora.weatherdata.util.Log;
import com.aurora.weatherforecast.R;

public class SnowAnim extends AbstractWeatherAnim {

	private int resIds[] = new int[] { R.drawable.snow_middle0,
			R.drawable.snow_middle1, R.drawable.snow_middle2,
			R.drawable.snow_middle3,R.drawable.snow_small0, R.drawable.snow_small1 };

	protected int mTypeNumbers[];

	protected int mMaxNumbers;

	private int[] mCount;

	public SnowAnim(SurfaceView view) {
		super(view);

	}

	protected void init() {
		setContendIds(resIds);

		setTypeNumbers(mTypeNumbers);

		mMaxNumbers = getSumOfTypeUtil(mTypeNumbers.length - 1);
		//mMaxNumbers = 100;
		setMaxDrawableNum(mMaxNumbers);

		initDrawablesThrowIds();

		mCount = new int[mMaxNumbers];

		initAnimParams();
	}

	@Override
	protected void initParams(AnimParams params, int index) {
		params.setAlpha(1.0f);
		params.setRotate(0);

	}

	@Override
	protected void resetParamsBeforeDraw(AnimParams params, int index) {
		int drawableHeight = params.getShowDrawableHeight() * 2;

		int height = super.getParentHeight() + drawableHeight;

		float rate = getRateByIndex(index, params.getRateYValue());

		params.getLocation().y = (int) (rate * height) - drawableHeight;

		if (0 <= params.getLocation().y && params.getLocation().y <= 5) {
			params.setChangeDir(true);
		}

		if (params.isChangeDir()) {
			params.setxOffset(getXOffset(index, params));
		}
		params.getLocation().x = params.getLocation().x + params.getxOffset();
		params.setAlpha((1.0f - rate) * 1);
		
		//Log.e("333333", "--444Alpha = ---" + Float.valueOf(1.0f - rate));

		mCount[index]++;

		if (mCount[index] >= mMaxNumbers) {
			mCount[index] = 0;
		}
	}

	/**
	 * 获得雪花在x轴的偏移量
	 * 
	 * @param index
	 * @param y
	 * @return
	 */
	private int getXOffset(int index, AnimParams params) {
		// x轴的偏移量
		int xOffset = 0;
		// 每下降yDistance的距离，将偏移方向设为与上次相反
		int yDistance = 0;
		if (index % 10 == 0 && mCount[index] % 2 == 0) {
			yDistance = 400;
		} else if (index % 8 == 0 && mCount[index] % 3 == 0) {
			yDistance = 500;
		} else if (index % 5 == 0 && mCount[index] % 4 == 0) {
			yDistance = 600;
		} else if (index % 3 == 0 && mCount[index] % 5 == 0) {
			yDistance = 700;
		}
		if (yDistance != 0) {
			// 让雪花偏移
			int result = params.getLocation().y / yDistance;
			if(result % 2 == 0) {
				if(index < getSumOfTypeUtil(1)) {
					xOffset = 3;
				}else {
					xOffset = 1;
				}
			}else {
				if(index < getSumOfTypeUtil(1)) {
					xOffset = -3;
				}else {
					xOffset = -1;
				}
			}
			if (mRandom.nextInt(2) == 0 && index < getSumOfTypeUtil(1)) {
				if(0 <= params.getLocation().y && params.getLocation().y <= 5) {
					// 在本轮下落中不改变雪花方向
					params.setChangeDir(false);
					xOffset = mRandom.nextInt(2) == 0 ? 1 : -1;
				}
			}
		}
		return xOffset;
	}

	/**
	 * 获取kind种类之前的雪花总数（包括kind）
	 * 
	 * @param kind
	 * @return
	 */
	private int getSumOfTypeUtil(int kind) {
		int sum = 0;
		if (mTypeNumbers != null) {
			for (int i = 0; i <= kind; i++) {
				sum += mTypeNumbers[i];
			}
		}
		return sum;
	}

	/**
	 * 计算雪花的下降速率
	 * 
	 * @param index
	 * @param rate
	 * @return
	 */
	private float getRateByIndex(int index, float rate) {
		if (index < getSumOfTypeUtil(0)) {
			rate = rate * 4.0f;
		} else if (getSumOfTypeUtil(0) <= index && index < getSumOfTypeUtil(1)) {
			rate = rate * 2.5f;
		} else if (getSumOfTypeUtil(1) <= index && index < getSumOfTypeUtil(2)) {
			rate = rate * 2.0f;
		} else if (getSumOfTypeUtil(2) <= index && index < getSumOfTypeUtil(3)) {
			rate = rate * 1.5f;
		} else if (getSumOfTypeUtil(3) <= index && index < getSumOfTypeUtil(4)) {
			rate = rate * 1.3f;
		} else if (getSumOfTypeUtil(4) <= index && index < getSumOfTypeUtil(5)) {
			rate = rate * 1.1f;
		} 
		return rate;
	}

}
