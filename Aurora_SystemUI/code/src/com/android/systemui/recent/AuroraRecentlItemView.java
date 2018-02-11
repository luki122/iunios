package com.android.systemui.recent;

import android.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.recent.utils.AuroraRecentsClearUtils;
import com.android.systemui.recent.utils.AuroraIconHelper;
import com.android.systemui.recent.utils.Utils;
import com.aurora.utils.Utils2Icon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Rect;

import android.animation.ObjectAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.animation.DecelerateInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;
import android.graphics.Canvas;
import java.lang.ref.WeakReference;

public class AuroraRecentlItemView extends LinearLayout{
	private boolean mLocked = false;
	private ImageView mLockView;

	private Utils2Icon mUtils2Icon;

	//Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
	public ImageView getmLockView() {
		return mLockView;
	}
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation end
	private boolean mNeedReadLockFlag = true;
	private Context mContext;
	public String mName;

    final static Rect mDigitDayPos = new Rect();
  	final static int mDayResIdList[] = new int[31];//everyday in month
    private static int ICON_REAL_SIZE = 178;
    private static int APP_ICON_RADIUS = 12;

	public AuroraRecentlItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;

		mUtils2Icon = Utils2Icon.getInstance(context);
		
	}

	public void updateLocked(String pkg,String taskAffinity,CharSequence lab, String classname){
		if(mNeedReadLockFlag){
			//if(null != lab){
			//	mName = pkg + lab.toString();
			//}else{
			//	mName = pkg;
			//}

            // Aurora <Felix.Duan> <2014-9-9> <BEGIN> Fix BUG #8091. Icon lock status using class name.
            mName = pkg + classname;
            // Aurora <Felix.Duan> <2014-9-9> <END> Fix BUG #8091. Icon lock status using class name.
			mNeedReadLockFlag = false;
			if(null != mName){
				mLocked = AuroraRecentsClearUtils.getLockFlag(mName);
			}
			mLockView.setVisibility(mLocked ? VISIBLE : INVISIBLE);
			int resId = AuroraIconHelper.getInstance().getDrawableId(pkg,taskAffinity);
			ImageView bgView = (ImageView)findViewById(R.id.app_icon);
//			if(0 != resId){
//				bgView.setImageResource(resId);
//			}else{
				Bitmap icon = mUtils2Icon.getIcon(pkg, classname, Utils2Icon.INTER_SHADOW);
				if(icon != null){
					icon = zoomDrawable(icon, mContext.getResources());
					bgView.setImageBitmap(icon);
				}/*else{
				    Drawable base = getResources().getDrawable(R.drawable.drawable_base);
				    Drawable src = bgView.getDrawable();
				    Drawable toUse =mUtils2Icon.isRectangle(src, mContext);
				    if(toUse==null){
				    	toUse = getCustomizedIcon(src,base);
				    }
				    bgView.setImageDrawable(toUse);
				}*/
//			}
		}

	}
	
	public void updateLocked(ResolveInfo rif, CharSequence lab,String taskAffinity){
		if(mNeedReadLockFlag){
			//if(null != lab){
			//	mName = rif.activityInfo.packageName + lab.toString();
			//}else{
			//	mName = rif.activityInfo.packageName;
			//}

            // Aurora <Felix.Duan> <2014-9-9> <BEGIN> Fix BUG #8091. Icon lock status using class name.
            mName = rif.activityInfo.packageName + rif.activityInfo.name;
            // Aurora <Felix.Duan> <2014-9-9> <END> Fix BUG #8091. Icon lock status using class name.
			mNeedReadLockFlag = false;
			if(null != mName){
				mLocked = AuroraRecentsClearUtils.getLockFlag(mName);
			}
			mLockView.setVisibility(mLocked ? VISIBLE : INVISIBLE);
			int resId = AuroraIconHelper.getInstance().getDrawableId(rif.activityInfo.packageName,taskAffinity);
			ImageView bgView = (ImageView)findViewById(R.id.app_icon);
//			if(0 != resId){
//				bgView.setImageResource(resId);
//			}else{
				Bitmap icon = mUtils2Icon.getIcon(rif, Utils2Icon.INTER_SHADOW);
				if(icon != null){
					icon = zoomDrawable(icon, mContext.getResources());
					bgView.setImageBitmap(icon);
				}/*else{
				    Drawable base = getResources().getDrawable(R.drawable.drawable_base);
				    Drawable src = bgView.getDrawable();
				    Drawable toUse =mUtils2Icon.isRectangle(src, mContext);
				    if(toUse==null){
				    	toUse = getCustomizedIcon(src,base);
				    }
				    bgView.setImageDrawable(toUse);
				}*/
//			}

            if ("com.android.calendar".equals(rif.activityInfo.packageName)) {
				//Log.v("xiaoyong", "is com.android.calendar");
				final long time = System.currentTimeMillis();
				final Calendar mCalendar = Calendar.getInstance();
				mCalendar.setTimeInMillis(time);
				int dayNow = mCalendar.get(Calendar.DAY_OF_MONTH);
				//Log.v("xiaoyong", "dayNow = " + dayNow);

                Bitmap bmpDayBg = loadRes(mContext, dayNow);
				if(bmpDayBg != null){
					bmpDayBg = zoomDrawable(bmpDayBg, mContext.getResources());
					bgView.setImageBitmap(bmpDayBg);
				}
				
            }
			
            //Log.v("xiaoyong", "packageName = " + rif.activityInfo.packageName);
		}

	}

	private Bitmap zoomDrawable(Bitmap oldbmp, Resources res) {
		Matrix matrix = new Matrix();
		int width = oldbmp.getWidth();
		int height = oldbmp.getHeight();
		float percent = res.getInteger(R.integer.aurora_recent_icon_zoom_percent);
		float scaleWidth = percent/100f;
		float scaleHeight = scaleWidth;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		WeakReference<Bitmap> reference = new WeakReference<Bitmap>(newbmp);
		newbmp = null;
/*		if (!oldbmp.isRecycled()){
			oldbmp.recycle();
		}*/
//		android.util.Log.e("xiuxiuxiu","width: " + newbmp.getWidth() + "    height: " + newbmp.getHeight());
//		return newbmp;
		if(reference.get() == null){
			return oldbmp;
		}else{
			return reference.get();
		}
	}
	

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mLockView = (ImageView)findViewById(R.id.app_lock);
	}
	public boolean setLocked(){
		
		mLocked = !mLocked;
		mLockView.setVisibility(mLocked ? VISIBLE : INVISIBLE);
		if(null != mName){
			AuroraRecentsClearUtils.setLockFlag(mName,mLocked);

            // Aurora <Felix.Duan> <2014-4-22> <BEGIN> Fix BUG #4470. Move IO task off UI thread.
            // Save lock status every time we change it.
            // TODO we can do better to just save one item than all of them.
            AuroraRecentsClearUtils.saveLockFlag(mContext);
            // Aurora <Felix.Duan> <2014-4-22> <END>  Fix BUG #4470. Move IO task off UI thread.
		}
		return mLocked;
	}

	
	public boolean isLocked(){
		return mLocked;
	}
	// Never used 
	/*public Drawable getCustomizedIcon(Drawable sourceicon,Drawable drawablebase) {
		Bitmap baseBitmap = ((BitmapDrawable) drawablebase).getBitmap();
		//steve.tang 2014-07-14 get base bitmap's round style. start
		baseBitmap = mUtils2Icon.getRoundedBitmap(baseBitmap, mContext);
		//steve.tang 2014-07-14 get base bitmap's round style. end
		Bitmap iconBitmap = null;
		
		if (null == sourceicon) {
			return null;
		}
		
		if(null == drawablebase){
			return sourceicon;
		}

		int basewidth = drawablebase.getIntrinsicWidth();
		int baseheight = drawablebase.getIntrinsicHeight();

        //Bug #8299, can't get app icon, user default. xiaoyong 2014-09-22 begin
        if (sourceicon.getIntrinsicWidth() == 0 || sourceicon.getIntrinsicHeight() == 0) {
            sourceicon = getResources().getDrawable(R.drawable.default_app_icon);
        }
        //Bug #8299, can't get app icon, user default. xiaoyong 2014-09-22 end
		int width = sourceicon.getIntrinsicWidth();
		int height = sourceicon.getIntrinsicHeight();

        iconBitmap = ((BitmapDrawable) sourceicon).getBitmap();
        if (iconBitmap == null) {
            return null;
        }

		Bitmap newBitmap = null;
		
		if (width > 178) {
			iconBitmap = getRoundedCornerBitmap(iconBitmap);
			final float ratiowidth = (float) iconBitmap.getWidth() / 180;
			final float ratioheight = (float) iconBitmap.getHeight() / 180;
			
			if (basewidth > 0 && baseheight > 0) {
				Matrix matrix = new Matrix();   // set up the matrix for scale operation for Bitmap
	            matrix.postScale(1 / ratiowidth, 1 / ratioheight);         // set the ratio for scale down or scale up
	            newBitmap = Bitmap.createBitmap(iconBitmap, 0, 0, iconBitmap.getWidth(), iconBitmap.getHeight(), matrix, true);       // get the new scaled Bitmap
				width = newBitmap.getWidth();
				height = newBitmap.getHeight();
			}
		} else {
			newBitmap = iconBitmap;
			width = newBitmap.getWidth();
			height = newBitmap.getHeight();
		}
		
//		final float ratiowidth = (float) width / 178;
//		final float ratioheight = (float) height / 178;
//		
//		if (basewidth > 0 && baseheight > 0) {
//			Matrix matrix = new Matrix();   // set up the matrix for scale operation for Bitmap
//            matrix.postScale(1 / ratiowidth, 1 / ratioheight);         // set the ratio for scale down or scale up
//            newBitmap = Bitmap.createBitmap(iconBitmap, 0, 0, iconBitmap.getWidth(), iconBitmap.getHeight(), matrix, true);       // get the new scaled Bitmap
//			width = newBitmap.getWidth();
//			height = newBitmap.getHeight();
//		}
//		newBitmap = toRoundCorner(baseBitmap, newBitmap, 10);
		
		
		int left = Math.abs(width - basewidth) / 2;
		int top = Math.abs(height - baseheight) / 2;
		
		final Bitmap sourcebitmap = Bitmap.createBitmap(basewidth, baseheight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(sourcebitmap);
		Paint mPaint = new Paint();
		canvas.drawBitmap(baseBitmap, 0, 0, mPaint);
		canvas.drawBitmap(newBitmap, left, top, mPaint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		BitmapDrawable iconDrawable = new BitmapDrawable(getResources(), sourcebitmap);
		return iconDrawable;
	}*/
//AURORA-END::Fix bug #073::Shi guiqiang::20131031
	
//AURORA-START::Fix bug #073::Shi guiqiang::20131106
	/**
	 * 
	 * @return get the round corner
	 */
    public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
            //setup new bitmap
            Bitmap bgBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            //set this bitmap as canvas
            Canvas mCanvas = new Canvas(bgBitmap);
            
            Paint mPaint = new Paint();
            int offsetX = bitmap.getWidth() / 20;
            int offsetY = bitmap.getHeight() / 20;
            Rect mRect = new Rect(offsetX, offsetY, bitmap.getWidth() - offsetX, bitmap.getHeight() - offsetY);
            RectF mRectF = new RectF(mRect);
            //set the radius as 10
            float roundPx = 10;
            mPaint.setAntiAlias(true);
            //draw the rect with round corner firstly
            mCanvas.drawRoundRect(mRectF, roundPx, roundPx, mPaint);
            
            //set the mode for superpostion
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            //draw the final bitmap
            mCanvas.drawBitmap(bitmap, mRect, mRect, mPaint);
            
            return bgBitmap;
    }

    // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel
    /**
     * Clone an item view to perform deletion animation, 
     * so other icons can line up without disturbing.
     */
    public AuroraRecentlItemView clone() {
        AuroraRecentlItemView clone = (AuroraRecentlItemView) LayoutInflater.from(mContext).inflate(R.layout.aurora_normal_item, null);
        // clone res
        clone.setItemDrawable(getItemDrawable());
        clone.setItemLabel(getItemLabel());
        clone.setScaleX(getScaleX());
        clone.setScaleY(getScaleY());
        clone.setAlpha(getAlpha());
        // clone lock state
        if (isLocked()) clone.setLocked();
        // clone pos
        int[] inWin = new int[2];
        getLocationInWindow(inWin);
        clone.setTranslationX(inWin[0]);
        clone.setTranslationY(inWin[1]);
        return clone;
    }

    private void setItemDrawable(Drawable drawable) {
        ImageView bgView = (ImageView)findViewById(R.id.app_icon);
        bgView.setImageDrawable(drawable);
    }

    private Drawable getItemDrawable() {
        ImageView bgView = (ImageView)findViewById(R.id.app_icon);
        return bgView.getDrawable();
    }

    private void setItemLabel(CharSequence text) {
        TextView label = (TextView)findViewById(R.id.app_label);
        label.setText(text);
    }

    private CharSequence getItemLabel() {
        TextView label = (TextView)findViewById(R.id.app_label);
        return label.getText();
    }

    // Added for distinguish between single swipe and clear all.
    // Determine to play single deletion animation or not.
    private boolean mSingleSwipe = false;

    public void setSingleSwipe() {
        mSingleSwipe = true;
    }

    public boolean isSingleSwipe() {
        return mSingleSwipe;
    }

    /**
     * Build single deletion animation, containing:
     * 1. shrink
     * 2. disappear
     *
     *    LiuHeng confirmed all animation params
     */
    public AnimatorSet buildAnim() {
        ObjectAnimator animX = ObjectAnimator.ofFloat(this, "scaleX", getScaleX(), 0.5f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(this, "scaleY", getScaleY(), 0.5f);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), 0f);
        animAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                // Self terminated
                ((ViewGroup)getParent()).removeView((View) ((ObjectAnimator)anim).getTarget());
            }

        });
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());
        set.playTogether(animX, animY, animAlpha);
        set.setDuration(400);
        return set;
    }
    // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel

    private Bitmap createDrawableDay(Context context, int day) {
		Resources res = context.getResources();
		
		//get background of day with shadow and rounded corner
		BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(R.drawable.recent_calendar_bg);
		Bitmap bmpDayBg = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		
		//get bitmap of the digit(from 1~31)
		Bitmap bmpDay = createBitmapDigit(context,day);
		
		//get bitmap of the foreground
//		Bitmap bmpFore = createForeBitmap(context);
		
		//measure
		DyMeasure(bmpDayBg.getWidth(), bmpDayBg.getHeight());

		Canvas canvas = new Canvas(bmpDayBg);
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		canvas.drawBitmap(bmpDay, mDigitDayPos.left , mDigitDayPos.top, paint);

//		canvas.drawBitmap(bmpFore, 0,0, paint);

		//add shadow
		//Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		//bmpDayBg = Utils.getRoundedBitmap(bmpDayBg, context);
		bmpDayBg = mUtils2Icon.getRoundedBitmap(bmpDayBg);
		//bmpDayBg = Utils.getShadowBitmap1(bmpDayBg, shadowDrawable);
		
		//resize for device
		//vulcan added it in 2014-7-1
		//if(DeviceProperties.isNeedScale()) {
		//	bmpDayBg = Utilities.zoomBitmap(bmpDayBg, context);
		//}
		
		canvas.setBitmap(null);
		
		//return new FastBitmapDrawable(bmpDayBg);
        return bmpDayBg;
	}

    public static void DyMeasure(int bgWidth, int bgHeight) {
		//measure the water
		final float digitDayOffsetXRate = 33.0f/184.0f;
		final float digitDayOffsetYRate = 33.0f/184.0f;
		final float digitDayWidthRate = 118.0f/184.0f;
		final float digitDayHeightRate = 118.0f/184.0f;
		
		mDigitDayPos.left = Math.round(digitDayOffsetXRate * bgWidth);
		mDigitDayPos.top = Math.round(digitDayOffsetYRate * bgHeight);
		mDigitDayPos.right = mDigitDayPos.left + Math.round(digitDayWidthRate * bgWidth);
		mDigitDayPos.bottom = mDigitDayPos.top + Math.round(digitDayHeightRate * bgHeight);
		return;
	}

	private Bitmap createBitmapDigit(Context context, int digit) {
		Resources res = context.getResources();
		BitmapDrawable bmpdDigit = (BitmapDrawable)res.getDrawable(getResIDByDig(digit));
		Bitmap bmpDigit = bmpdDigit.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
		return bmpDigit;
	}

	private static int getResIDByDig(int dig) {
		if (dig >= 1 && dig < mDayResIdList.length + 1) {
			return mDayResIdList[dig - 1];
		}
		return R.drawable.recent_cal_day_1;
	}

	public Bitmap loadRes(Context context, int dayNow) {
		
		mDayResIdList[0] = R.drawable.recent_cal_day_1;
		mDayResIdList[1] = R.drawable.recent_cal_day_2;
		mDayResIdList[2] = R.drawable.recent_cal_day_3;
		mDayResIdList[3] = R.drawable.recent_cal_day_4;
		mDayResIdList[4] = R.drawable.recent_cal_day_5;
		mDayResIdList[5] = R.drawable.recent_cal_day_6;
		mDayResIdList[6] = R.drawable.recent_cal_day_7;
		mDayResIdList[7] = R.drawable.recent_cal_day_8;
		mDayResIdList[8] = R.drawable.recent_cal_day_9;
		mDayResIdList[9] = R.drawable.recent_cal_day_10;
		mDayResIdList[10] = R.drawable.recent_cal_day_11;
		mDayResIdList[11] = R.drawable.recent_cal_day_12;
		mDayResIdList[12] = R.drawable.recent_cal_day_13;
		mDayResIdList[13] = R.drawable.recent_cal_day_14;
		mDayResIdList[14] = R.drawable.recent_cal_day_15;
		mDayResIdList[15] = R.drawable.recent_cal_day_16;
		mDayResIdList[16] = R.drawable.recent_cal_day_17;
		mDayResIdList[17] = R.drawable.recent_cal_day_18;
		mDayResIdList[18] = R.drawable.recent_cal_day_19;
		mDayResIdList[19] = R.drawable.recent_cal_day_20;
		mDayResIdList[20] = R.drawable.recent_cal_day_21;
		mDayResIdList[21] = R.drawable.recent_cal_day_22;
		mDayResIdList[22] = R.drawable.recent_cal_day_23;
		mDayResIdList[23] = R.drawable.recent_cal_day_24;
		mDayResIdList[24] = R.drawable.recent_cal_day_25;
		mDayResIdList[25] = R.drawable.recent_cal_day_26;
		mDayResIdList[26] = R.drawable.recent_cal_day_27;
		mDayResIdList[27] = R.drawable.recent_cal_day_28;
		mDayResIdList[28] = R.drawable.recent_cal_day_29;
		mDayResIdList[29] = R.drawable.recent_cal_day_30;
		mDayResIdList[30] = R.drawable.recent_cal_day_31;
		
		
		//create drawable for every day of month
		Bitmap bitmap = null;
        bitmap = createDrawableDay(context, dayNow);        

        if (bitmap == null) {
            Resources res = context.getResources();
            BitmapDrawable bmpdDayBg = (BitmapDrawable) res.getDrawable(R.drawable.recent_calendar_bg);
            bitmap = bmpdDayBg.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
        }
		
		return bitmap;
	}
}
