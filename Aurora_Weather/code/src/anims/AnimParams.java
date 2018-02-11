package anims;

import android.graphics.Point;  
import android.graphics.drawable.Drawable;
import android.util.Log;
import aurora.opengl.AuroraGLDrawable;

public class AnimParams {
	
	
	/**
	 * the position to show !!!
	 */
	private Point mLocation;
	
	/**
	 * drawable's rotation
	 */
	private float mRotate;	
	
	/**
	 * [0,1]
	 */
	private float mAlpha;
	
	/**
	 * [0,1]
	 */
	private float mScaleX;
	
	/**
	 * [0,1]
	 */
	private float mScaleY;
	
	private float mTranslationX;
	
	private float mTranslationY;
	
	private float mTime = 0;
	
	private Drawable mShowDrawable;
	
	private int mDrawableHeight;
	
	private int mDrawableWidth;
	
	private float mRateX;
	
	private float mRateY;
	
	private AnimRateInterface mAri;
	
	private float default_timeRate = 0.03f;
	
	private boolean changeDir;
	
	private int xOffset;
	
	public AnimParams(int pointX, int pointY)
	{
		setLocation(new Point(pointX , pointY));
		
		this.setRotate(0);
		
		this.setAlpha(1.0f);
		
		this.setScaleX(1.0f);
		
		this.setScaleY(1.0f);
		
		this.setTranslationX(0);
		
		this.setTranslationY(0);
	}
	
	public void setAlpha(float alpha)
	{
		this.mAlpha = alpha;
	}

	public void setRotate(float rotate)
	{
		this.mRotate = rotate;
	}
	
	public void setDrawable(Drawable drawable)
	{
		 setShowDrawable(drawable);
	}
	
	public void calculateRate()
	{
	
		mTime += default_timeRate;
		
		if(mTime > 1)mTime = 0;
		
		float time =  mTime;
		
		if(mAri != null)
		{
			mRateX = mAri.getRateX(time);
			
			mRateY = mAri.getRateY(time);
		}
		//Log.e("liuwei", "mTime = " + mTime + ", mRateX = " + mRateX + ", mRateY = "+ mRateY);
		
	}
	
	public interface AnimRateInterface{
		//time : [0,1]
		public float getRateX(float time);
		
		public float getRateY(float time);
	}
	
	public void setRateInterface(AnimRateInterface animRateInterface)
	{
		mAri = animRateInterface;
	}
	
	public float getRateXValue()
	{
		return mRateX;
	}
	
	public float getRateYValue()
	{
		return mRateY;
	}

	public Point getLocation() {
		return mLocation;
	}

	public void setLocation(Point mLocation) {
		this.mLocation = mLocation;
	}

	public Drawable getShowDrawable() {
		return mShowDrawable;
	}

	public void setShowDrawable(Drawable mShowDrawable) {
		this.mShowDrawable = mShowDrawable;
		mDrawableHeight = mShowDrawable.getIntrinsicHeight();
		mDrawableWidth = mShowDrawable.getIntrinsicWidth();
	}
	
	public int getShowDrawableHeight( ) {
		return mDrawableHeight;
	}
	
	public int getShowDrawableWidth( ) {
		return mDrawableWidth;
	}

	public float getRotate() {
		return mRotate;
	}

	public float getAlpha() {
		return mAlpha;
	}

	public float getScaleX() {
		return mScaleX;
	}

	public void setScaleX(float mScaleX) {
		this.mScaleX = mScaleX;
	}

	public float getScaleY() {
		return mScaleY;
	}

	public void setScaleY(float mScaleY) {
		this.mScaleY = mScaleY;
	}
	
	public float getTime() {
		return mTime;
	}

	public void setTime(float time) {
		this.mTime = time;
	}

	public void setDefaultTimeRate( float timeRate ) {
		this.default_timeRate = timeRate;
	}

	public boolean isChangeDir() {
		return changeDir;
	}

	public void setChangeDir(boolean changeDir) {
		this.changeDir = changeDir;
	}

	public int getxOffset() {
		return xOffset;
	}

	public void setxOffset(int xOffset) {
		this.xOffset = xOffset;
	}
	
	public float getTranslationX() {
		return mTranslationX;
	}

	public void setTranslationX(float mTranslationX) {
		this.mTranslationX = mTranslationX;
	}

	public float getTranslationY() {
		return mTranslationY;
	}

	public void setTranslationY(float mTranslationY) {
		this.mTranslationY = mTranslationY;
	}
	
}
