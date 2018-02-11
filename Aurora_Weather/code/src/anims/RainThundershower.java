package anims;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.SurfaceView;
import aurora.opengl.AuroraGLDrawable;
import aurora.view.AuroraGLSurfaceView;

import com.aurora.weatherforecast.R;

/**
 * 雷阵雨
 * 
 * @author leo
 * 
 */
public class RainThundershower extends RainAnim {

	private SurfaceView view;

	// 右边闪电间隔时间
	private static final int RIGHTINTERVAL = 4000;
	private static final int THUNDER_TIME_LONG = 40;
	// 右边闪电上次出现的时间戳
	private long lastRightThunderingTime;
	private Drawable drawableRightThundering;
	private int drawCountRight;

	private Drawable drawableLeftThundering;
	private int drawCountLeft;

	// 闪电图片的透明度
	private float alpha;
	// 透明度变化大步长
	private float alphaOffset;

	private boolean onPause;

	public RainThundershower(SurfaceView view) {
		super(view);
		this.view = view;
		init();
	}

	@Override
	protected void init() {
		super.mTypeNumbers = new int[] { 20, 30, 10 };

		super.mMaxNumbers = 60;

		super.init();
		initThunderingDrawable();
		setParams();
	}

	@Override
	public void onResume() {
		super.onResume();
		setParams();
	}

	/**
	 * 初始化一些参数
	 */
	private void setParams() {
		lastRightThunderingTime = 0;
		drawCountRight = 0;

		drawCountLeft = 0;
		this.onPause = false;
	}

	/**
	 * 初始化闪电图片资源
	 */
	private void initThunderingDrawable() {
		Bitmap bitmapRight = decodeBitmapByScreenWidth(
				R.drawable.thunder_right, getParentWidth());
		if (bitmapRight != null) {
			drawableRightThundering = new BitmapDrawable(mContext.getResources(), bitmapRight);
		}
		//bitmapRight.recycle();

		Bitmap bitmapLeft = decodeBitmapByScreenWidth(R.drawable.thunder_left,
				getParentWidth());
		Log.e("222222", "-bitmapLeft Width = --" + bitmapLeft.getWidth());
		Log.e("222222", "-bitmapLeft Height = --" + bitmapLeft.getHeight());
		if (bitmapLeft != null) {
			drawableLeftThundering = new BitmapDrawable(mContext.getResources(), bitmapLeft);
			//drawableLeftThundering = new BitmapDrawable(bitmapLeft);
		}
		//bitmapLeft.recycle();
		Log.e("222222", "-drawableLeftThundering Width = --" + drawableLeftThundering.getIntrinsicWidth());
		Log.e("222222", "-drawableLeftThundering Height = --" + drawableLeftThundering.getIntrinsicHeight());
	}

	/**
	 * 根据父视图的宽度，改变图片的宽高
	 * 
	 * @param bmpId
	 *            图片资源id
	 * @param parentWidth
	 *            父视图宽度
	 * @return
	 */
	private Bitmap decodeBitmapByScreenWidth(int bmpId, int parentWidth) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(mContext.getResources(), bmpId, options);
		int bmpWidth = options.outWidth;
		Log.e("222222", "--bmpWidth = --" + bmpWidth);
		int inSampleSize = 1;
		if (bmpWidth > parentWidth) {
			inSampleSize = bmpWidth / parentWidth;
		}
		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(mContext.getResources(), bmpId,
				options);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (!onPause) {
			canvas.save();
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastRightThunderingTime >= RIGHTINTERVAL) {
				if (lastRightThunderingTime == 0) {
					lastRightThunderingTime = currentTime;
				} else {
					if (drawableRightThundering != null) {
						setAlphaOffset(drawCountRight);
						alpha += alphaOffset;
						drawableRightThundering.setBounds(0, 0, drawableRightThundering.getIntrinsicWidth(), drawableRightThundering.getIntrinsicHeight());
						drawableRightThundering.setAlpha((int)(alpha * 255));
						//Log.e("222222", "--drawableRightThundering alpha = --" + alpha);
						drawableRightThundering.draw(canvas);
						drawCountRight++;
						if (drawCountRight == THUNDER_TIME_LONG) {
							lastRightThunderingTime = currentTime;
							drawCountRight = 0;
							drawCountLeft = -1;
							alpha = 0;
						}
					}
				}
			}
			if (currentTime - lastRightThunderingTime >= (RIGHTINTERVAL / 10)
					&& drawCountLeft != 0) {
				if (drawCountLeft == -1) {
					drawCountLeft = 0;
				}
				if (drawableLeftThundering != null) {
					setAlphaOffset(drawCountLeft);
					alpha += alphaOffset;
					drawableLeftThundering.setAlpha((int)(alpha * 255));
					drawableLeftThundering.setBounds(0, 0, drawableLeftThundering.getIntrinsicWidth(), drawableLeftThundering.getIntrinsicHeight());
					//Log.e("222222", "--drawableLeftThundering alpha = --" + alpha);
					drawableLeftThundering.draw(canvas);
					drawCountLeft++;
					if (drawCountLeft == THUNDER_TIME_LONG) {
						drawCountLeft = 0;
						alpha = 0;
					}
				}
			}
			canvas.restore();
		}
	}

	/**
	 * 设置闪电过程中的透明度
	 * 
	 * @param drawCount
	 */
	private void setAlphaOffset(int drawCount) {
		if (drawCount < THUNDER_TIME_LONG * 2 / 9
				|| (drawCount >= THUNDER_TIME_LONG * 1 / 3 && drawCount < THUNDER_TIME_LONG * 7 / 9)) {
			if (drawCount < THUNDER_TIME_LONG * 2 / 9) {
				alphaOffset = 1.0f / (THUNDER_TIME_LONG * 2 / 9);
			} else {
				alphaOffset = 1.0f / (THUNDER_TIME_LONG * 2 / 9 * 2);
			}
		} else {
			if (drawCount < THUNDER_TIME_LONG * 1 / 3) {
				alphaOffset = -1.0f / (THUNDER_TIME_LONG * 1 / 9);
			} else {
				alphaOffset = -1.0f / (THUNDER_TIME_LONG * 2 / 9);
			}
		}
	}

	@Override
	protected void initParams(AnimParams params, int index) {
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
	protected void resetParamsBeforeDraw(AnimParams params, int index) {
		super.resetParamsBeforeDraw(params, index);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.onPause = true;
	}

}
