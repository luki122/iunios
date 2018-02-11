package views;

import java.util.Arrays;
import java.util.Random;

import com.aurora.weatherdata.util.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.SurfaceView;
import anims.AnimParams;
import aurora.opengl.AuroraGLDrawable;
import aurora.view.AuroraGLSurfaceView;
import interfaces.IWeatherAnim;
import com.aurora.weatherforecast.R;

public abstract class AbstractWeatherAnim implements IWeatherAnim {
	
	protected Context mContext;
	
	private SurfaceView mView;
	/**
	 * drawable ids
	 */
	private int mResIds[];
	
	private int mMaxNumbers;
	
	private Drawable mDrawables[];
	
	private AnimParams mAnimParams[];
	
	private AnimParams mInitParams;
	private AnimParams mParams;
	
	private Bitmap bitmap;
	private Paint paint = new Paint();
	
	/**
	 * each drawable type has how many drawables 
	 */
	private int mDrawableTypeNumbers[];
	
	private int mHeight;
	
	private int mWidth;
	
	protected Random mRandom = new Random();
	
	private static boolean onPause = false;
	
	protected boolean isRainAnim = false;
	
	private float[] animAlpha;
	
	public AbstractWeatherAnim(SurfaceView view)
	{
		mContext = view.getContext();
		
		mView = view;
		
		mWidth = view.getMeasuredWidth();
		
		mHeight = view.getMeasuredHeight();
	}
	
	public void emptyAnimAlpha( ) {
		if ( this.animAlpha == null ) {
			this.animAlpha = new float[mMaxNumbers];
		}
		Arrays.fill(this.animAlpha, 0f);
	}
	
	private long a;
	private long b;
	
	private float alphaFactor=1;
	
	public void setAlphaFactory(float alphaFactory){
		this.alphaFactor=alphaFactory;
	}
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		canvas.save();
		
		if ( mMaxNumbers > 0 ) {
			mInitParams = mAnimParams[0];
			canvas.rotate(-mInitParams.getRotate());
			canvas.translate(mInitParams.getTranslationX(), mInitParams.getTranslationY());
		}
		
		for(int i=0 ; i < mMaxNumbers ; i++)
		{	
			mParams = mAnimParams[i];
			
			//a = System.currentTimeMillis();
			
			Drawable drawable = mParams.getShowDrawable();
			
	        int w = mParams.getShowDrawableWidth();
	        int h = mParams.getShowDrawableHeight();
	        
			//b = System.currentTimeMillis() - a;
			//Log.e("333333", "--doDraw cost111 = ---" + b);
			
			mParams.calculateRate();
			
			resetParamsBeforeDraw(mParams,i);
			
			//b = System.currentTimeMillis() - a;
			//Log.e("333333", "--doDraw cost222 = ---" + b);
			
//			drawable.setAlpha((int)(mParams.getAlpha()*alphaFactor));
			if ( animAlpha[i] >= mParams.getAlpha() ) {
				drawable.setAlpha((int)(mParams.getAlpha() * 255*alphaFactor));
			} else {
				drawable.setAlpha((int)(animAlpha[i] * 255*alphaFactor));
			}
			
			//Log.e("222222", "--Alpha = ---" + params.getAlpha() * 255);
			
			if( mParams.getRotate() != mInitParams.getRotate() || mParams.getTranslationX() != mInitParams.getTranslationX()
					|| mParams.getTranslationY() != mInitParams.getTranslationY()) {
				canvas.restore();
				canvas.save();
				
				canvas.rotate(-mParams.getRotate());
				canvas.translate(mParams.getTranslationX(), mParams.getTranslationY());
				
				mInitParams = mParams;
			}
			
			//drawable.setMartix(params.getLocation().x, params.getLocation().y, params.getRotate(), params.getScaleX(), params.getScaleY());
			drawable.setBounds(mParams.getLocation().x, mParams.getLocation().y, mParams.getLocation().x + w, mParams.getLocation().y + h);
			
			if(!onPause) {
				drawable.draw(canvas);
				//canvas.drawBitmap(bitmap, mParams.getLocation().x, mParams.getLocation().y, paint);
			}
			
			//b = System.currentTimeMillis() - a;
			//Log.e("333333", "--doDraw cost333 = ---" + b);
			
			int drawableHeight = 0; 
			if(isRainAnim) {
				drawableHeight = w;
			}
			if(mParams.getLocation().y + drawableHeight >= mHeight) {
				mParams.getLocation().x = mRandom.nextInt(mWidth);
				//params.getLocation().y = mRandom.nextInt(mHeight);
				mParams.getLocation().y = 0;
				//params.setTime(0);
				mAnimParams[i] = mParams;
			}
			if ( animAlpha[i] <= 1f ) {
				animAlpha[i] += 0.02f;
			}
			
			//b = System.currentTimeMillis() - a;
			//Log.e("333333", "--doDraw cost444 = ---" + b);
		}
		canvas.restore();
	}
	
	protected void setContendIds(int[] ids)
	{
		// TODO Auto-generated method stub
		mResIds = ids;
	}
	
	/**
	 * assignment mDrawableTypeNumbers !!!
	 */
	protected void setTypeNumbers(int[] numbers)
	{
		mDrawableTypeNumbers = numbers;
	}

	protected void setMaxDrawableNum(int maxNumber) 
	{
		// TODO Auto-generated method stub
		mMaxNumbers = maxNumber;
	}
	
	protected void initDrawablesThrowIds()
	{
		if(mDrawables == null)
		{
			mDrawables = new Drawable[mResIds.length];
			
			for(int i = 0 ; i < mResIds.length ; i++)
			{
				mDrawables[i] = mContext.getResources().getDrawable(mResIds[i]);
				//Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mResIds[i]);
				//Log.e("222222", "--initDrawablesThrowIds bitmap.getWidth() = ----" + bitmap.getWidth());
				//Log.e("222222", "--initDrawablesThrowIds bitmap.getHeight() = ----" + bitmap.getHeight());
				//mDrawables[i] = new BitmapDrawable(mContext.getResources(), bitmap);
				//Log.e("222222", "--initDrawablesThrowIds i = ----" + i);
				//Log.e("222222", "--initDrawablesThrowIds mDrawables[i] = ----" + mDrawables[i].getIntrinsicWidth());
				//Log.e("222222", "--initDrawablesThrowIds mDrawables[i] = ----" + mDrawables[i].getIntrinsicHeight());
			}
		}
		
		//bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.rain_large);
	}
	
	protected void initAnimParams()
	{
		if(mAnimParams == null)
		{
			mAnimParams = new AnimParams[mMaxNumbers];
			
			for(int i = 0 ; i < mMaxNumbers ; i++)
			{
				int x = mRandom.nextInt(mWidth);
				int y = mRandom.nextInt(mHeight);
				
				mAnimParams[i] = new AnimParams(x,y);
				
				Drawable drawable = getDrawables(i);
				
				mAnimParams[i].setDrawable(drawable);
				
				mAnimParams[i].setTime((float)y/(float)mHeight);
				
				initParams(mAnimParams[i],i);
			}
		}
	}
	
	private Drawable getDrawables(int index)
	{
		int max = 0;
		
		for(int i = 0 ; i < mDrawables.length ; i++)
		{
			max += mDrawableTypeNumbers[i];
			
			if(index < max)
			{
				return mDrawables[i];
			}
		}
		
		return mDrawables[0];
	}
	
	protected SurfaceView getParentView()
	{
		return mView;
	}
	/**
	 * init Accelerate and so on !!!
	 * @param params
	 */
	protected abstract void initParams(AnimParams params , int index);
	
	/**
	 * let user has the last chance to change the Location !!!
	 * @param params
	 */
	protected abstract void resetParamsBeforeDraw(AnimParams params,int index);
	
	protected int getParentHeight()
	{
		return mHeight;
	}
	
	protected int getParentWidth()
	{
		return mWidth;
	}
	
	@Override
	public void onPause()
	{
		onPause = true;
		//Log.e("333333", "-onPause onPause = ---" + onPause);
	}
	
	@Override
	public void onResume()
	{
		onPause = false;
		//Log.e("333333", "-onResume onPause = ---" + onPause);
	}
}
