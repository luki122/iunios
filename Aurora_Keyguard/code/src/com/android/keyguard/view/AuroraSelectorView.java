package com.android.keyguard.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.R;
import com.android.keyguard.utils.AnimUtils;
import com.android.keyguard.utils.AuroraLog;
import com.android.keyguard.utils.LockScreenBgUtils;
import com.android.keyguard.utils.LockScreenUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

// Aurora liugj 2014-06-27 modified for MusicLockScreen start
public class AuroraSelectorView extends FrameLayout implements KeyguardSecurityView {

	//private static final float MAX_FLOAT_VALUE = 1.0f;
    private StatusView mStatusView;
	 // Aurora liugj 2014-07-01 modified for bug-6149 start
    private MyViewPager mPager;
	 // Aurora liugj 2014-07-01 modified for bug-6149 end

    private List<View> mViews;

    private static final String TAG = "AuroraSelectorView";
    private KeyguardSecurityCallback mCallback;
    
    //private ImageView mWallPaper;
    //private LinearLayout mWallPaperBg;
    private AlphaLayout mAlphaLayout;
    //private AlphaBackground mAlphaBackground;

	 // Aurora liugj 2014-09-22 deleted for 防误触 start
	 // Aurora liugj 2014-09-18 modified for 防误触 start
    /*private SensorManager mSensorMgr;
    private Sensor mProximitySensor;
    private Sensor mLightSensor;
    private boolean mRegisterSensorFlg = false;
    private boolean mProSensorFlag = false;
    private boolean mLightSensorFlag = false;*/
	 // Aurora liugj 2014-09-18 modified for 防误触 end
	 // Aurora liugj 2014-09-22 deleted for 防误触 end
    
    public AuroraSelectorView(Context context) {
        this(context, null);
        //mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public AuroraSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        //mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        
    }

    public AuroraSelectorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //initView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
		  // Aurora liugj 2014-07-01 modified for bug-6149 start
        mPager = ( MyViewPager ) findViewById(R.id.selector_viewpager);
		  // Aurora liugj 2014-07-01 modified for bug-6149 end
        mStatusView = ( StatusView ) View.inflate(getContext(), R.layout.status_view, null);

        //mWallPaperBg = ( LinearLayout ) mStatusView.findViewById(R.id.lock_screen_bg_view);
        //mWallPaper = ( ImageView ) mStatusView.findViewById(R.id.lock_screen_bg_wallpaper);
        
//        mAlphaBackground = ( AlphaBackground ) findViewById(R.id.lock_screen_alpha_bg);

        /*if (!LockScreenUtils.getInstance(getContext()).isSecure()) {
            mWallPaperBg.setVisibility(View.VISIBLE);
//            mWallPaper.setBackground(WallpaperManager.getInstance(getContext()).getDrawable());
            //LockScreenBgUtils.getInstance().setViewBg(mWallPaper);
        }*/
//        LockScreenBgUtils.getInstance().setViewBg(mWallPaper);

        mViews = new ArrayList<View>();
        mViews.add(mStatusView);
//        if (LockScreenUtils.getInstance(getContext()).isSecure()) {
//            View view = new View(getContext());
//            mViews.add(view);
//        } else {
            ImageView img = new ImageView(getContext());
            img.setBackgroundResource(R.drawable.xiangji);
            mViews.add(img);
//        }

        changeViewPagerFling();
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(mViews);
        mPager.setAdapter(pagerAdapter);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mPager.setOverScrollMode(OVER_SCROLL_NEVER);

        //Aurora <zhang_xin> <2013-9-26> modify for alphaEffect begin
        View view = getChildAt(0);
        if (view != null && view instanceof AlphaLayout) {
            mAlphaLayout = ( AlphaLayout ) view;
        }
        //Aurora <zhang_xin> <2013-9-26> modify for alphaEffect end
		  // Aurora liugj 2014-09-22 deleted for 防误触 start
		  // Aurora liugj 2014-09-12 modified for 防误触 start
        /*if (mProximitySensor == null) {
        	mProximitySensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if(mProximitySensor != null) {
            	mProximityThreshold = Math.min(mProximitySensor.getMaximumRange(), MAX_FLOAT_VALUE);
            }
		}
			if (mLightSensor == null) {
			mLightSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
		}*/
		// Aurora liugj 2014-09-12 modified for 防误触 end
		// Aurora liugj 2014-09-22 deleted for 防误触 end
    }
    
	// Aurora liugj 2014-06-16 deleted for MusicLockScreen start
    /*static final int DEFAULT_DETAL = 50;
    static final int SNAP_VELOCITY = 800;
    private static final int STATE_UNLOCK = 1;
    private static final int STATE_CAMERA = 2;
    private static final int STATE_REST = 0;
    private int mState = STATE_REST;
    private static final int UNVAIABLE_Y = -1;
    
    private int mDetalY = 0;
    private MyInterpolator mInterpolater;
    private float mLastY;
    private Scroller mScroller;
    private VelocityTracker mTraker;*/
	// Aurora liugj 2014-06-16 deleted for MusicLockScreen end
    private float mLastDownX;
    private float mLastDownY;
    
    private float mTotalDx;
    private float mTotalDy;
	 private boolean mOnXMove = false;
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen start
    /*private boolean mOnMove = false;
    private boolean mOnYMove = false;*/
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen end

	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen start
    /*private Runnable mBackRunnable = new Runnable() {

        public void run() {
            int i = Math.min(mDetalY, 1);
            mDetalY = -1 + mDetalY;
            scrollBy(0, i);
            if (mDetalY >= 0) {
//                post(mBackRunnable);
                postDelayed(mBackRunnable, 4);
            } else {
                tapScrollY();
            }
        }
    };

    Runnable mUnLockRunnable = new Runnable() {

        public void run() {
            mCallback.userActivity(0);
            mAlphaBackground.setAlpha(0);
            //mCallback.dismiss(false);
        }

    };*/
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen end

	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen start
    /*private void initView() {
        mInterpolater = new MyInterpolator();
        mScroller = new Scroller(getContext(), mInterpolater);
    }

    private void tapScrollY() {
        int i = Math.max(getScrollY(), 1500);
        mInterpolater.setInterpolatorType(3);
        mScroller.startScroll(0, DEFAULT_DETAL, 0, -DEFAULT_DETAL, i);
        postInvalidate();
    }*/
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen end

    @Override
    public void computeScroll() {
        /*if (mScroller.computeScrollOffset()) {
            if (!isCameraScrolling()) {
                scrollTo(0, mScroller.getCurrY());
                postInvalidate();
            }
        }*/
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //Aurora <zhang_xin> <2013-9-26> modify for alphaEffect begin
//        int i = getHeight();
//        int j = getScrollY();
//        if (mAlphaLayout != null && i > 0 && j > 0) {
//            int k = 255 - ( int ) (j * 255 / 1.0D * i);
//            mAlphaLayout.setAlpha(k);
//        }
        //Aurora <zhang_xin> <2013-9-26> modify for alphaEffect end
        super.dispatchDraw(canvas);
    }

	 // Aurora liugj 2014-06-16 modified for MusicLockScreen start
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
		// Aurora liugj 2014-09-12 modified for 防误触 start
    	int action = event.getAction();
		// Aurora liugj 2014-09-22 deleted for 防误触 start
    	/*if (mLightSensorFlag && mProSensorFlag) {
    		if (isCameraScrolling()) {
    			action = MotionEvent.ACTION_UP;
			}else {
				if (mStatusView.isUnlockScrolling()) {
    				mStatusView.handleActionUnlockUp();
    			}
				return true;
			}
		}*/
		// Aurora liugj 2014-09-12 modified for 防误触 end
		// Aurora liugj 2014-09-22 deleted for 防误触 end
        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "Selector=====dispatchTouchEvent====="+action);
    	switch (action) {
    		case MotionEvent.ACTION_DOWN:
    			mLastDownX = event.getX();
                mLastDownY = event.getY();
                mCallback.userActivity(0);
    			break;
    		case MotionEvent.ACTION_MOVE:
    			mTotalDx = x - mLastDownX;
                mTotalDy = y - mLastDownY;

                final int diffX = ( int ) Math.abs(mTotalDx);
                //final int diffY = ( int ) Math.abs(mTotalDy);
				// Aurora liugj 2014-07-01 modified for bug-6149 start
                if (LockScreenUtils.getInstance(getContext()).isSecure() && diffX > 150 && !isUnlockScrolling()) {
                	Log.d(TAG, "Selector=====dispatchTouchEvent=====postUnLockRunnable");
                	mOnXMove = true;
                	mStatusView.postUnLockRunnable();
            		return true;
				}
    			break;
    		case MotionEvent.ACTION_UP:
    			if (mOnXMove) {
    				mOnXMove = false;
					return true;
				}
    			break;
    		case MotionEvent.ACTION_CANCEL:
    			mOnXMove = false;
                break;
			// Aurora liugj 2014-07-01 modified for bug-6149 end
    		default:
                break;
    	}
    	return super.dispatchTouchEvent(event);
    }
	 // Aurora liugj 2014-06-16 modified for MusicLockScreen end
    
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen start
    /*private void handleActionUnlockUp(VelocityTracker tracker) {
        int j = getHeight();
        int k = getScrollY();
        int l = 0;
        int d = 400;
        Runnable runnable = null;
        if (tracker.getYVelocity() < -SNAP_VELOCITY || k > (j * 2) / 5) {
            l = j - k;
            d = Math.min(l, 300);
            runnable = mUnLockRunnable;
            mInterpolater.setInterpolatorType(0);
            mScroller.startScroll(0, k, 0, l, d);
            invalidate();
            mAlphaBackground.setAlpha(0);
        } else if (k > DEFAULT_DETAL) {
            l = Math.max(k, 1800);
            mInterpolater.setInterpolatorType(1);
            mScroller.startScroll(0, k, 0, -k, l);
            invalidate();
        } else {
            d = 0;
            mDetalY = DEFAULT_DETAL - k;
            runnable = mBackRunnable;
            mAlphaBackground.setAlpha(180);
        }
//        logd("getYVelocity=" + tracker.getYVelocity() + ",getScrollY=" + k + ",mDetalY=" + mDetalY + ",mScroller.getY=" + mScroller.getCurrY());

        if (runnable != null) {
            long l1 = d;
            postDelayed(runnable, l1);
        }
    }*/

    /*private void handleActionUnlockMove(float y) {
        int i = ( int ) (mLastY - y);
        mLastY = y;
        if (i < 0) {
            int k = getScrollY();
            if (k >= 0) {
                int i1 = Math.max(i, -k);
                if (i1 < 0) {
                    scrollBy(0, i1);
                }
            }
        } else {
            int j1 = getHeight() - getScrollY();
            if (j1 >= 0) {
                scrollBy(0, Math.min(j1, i));
            }
        }
        backgroundAlphaChanged(mAlphaBackground, getScrollY());
//        logd(",getScrollY=" + getScrollY() + ",mDetalY=" + mDetalY + ",mScroller.getY=" + mScroller.getCurrY() + ",mlastY=" + mLastY);
//        logd(",getScrollY=" + getScrollY() + ",mlastY=" + mLastY + ",i=" + i);
    }*/

    /*class MyInterpolator extends AccelerateDecelerateInterpolator {
        private int mInterpolatorType;

        private float bounce(float f) {
            return 8.0F * f * f;
        }

        public float getInterpolation(float input) {
            float f2;
            if (mInterpolatorType == 0) {
                f2 = super.getInterpolation(input);
            } else {
                float f1 = input * 1.1226F;
                if (f1 < 0.3535F)
                    f2 = bounce(f1);
                else if (f1 < 0.7408F)
                    f2 = 0.7F + bounce(f1 - 0.54719F);
                else if (f1 < 0.9644F)
                    f2 = 0.9F + bounce(f1 - 0.8526F);
                else
                    f2 = 0.95F + bounce(f1 - 1.0435F);
            }
            return f2;
        }

        public void setInterpolatorType(int i) {
            mInterpolatorType = i;
        }

    }*/
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen end

    private static final boolean IS_DEBUG = true;

    private void logd(String msg) {
        if (IS_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
//            Log.d("zhang", "onPageScrollStateChanged=" + state + ",pager.getScrollX=" + mPager.getScrollX() + ",getScrollY=" + getScrollY());
            if (state == ViewPager.SCROLL_STATE_DRAGGING && getScrollY() < 75) {
                scrollTo(0, 0);
				// Aurora liugj 2014-06-16 deleted for MusicLockScreen start
                //mScroller.abortAnimation();
				// Aurora liugj 2014-06-16 deleted for MusicLockScreen end
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d("zhangxin", "onPageScrolled=" + position + ",positionOffset=" + positionOffset + ",positionOffsetPixels=" + positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int positon) {
            if (positon == mViews.size() - 1) {
				// Aurora liugj 2014-06-16 modified for MusicLockScreen start
                //post(mCameraRunnable);
            	mStatusView.postCameraRunnable();
				// Aurora liugj 2014-06-16 modified for MusicLockScreen end
            }
        }

    }

	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen start
    /*Runnable mCameraRunnable = new Runnable() {

        @Override
        public void run() {
            post(mUnLockRunnable);
            try {
                Intent intent = new Intent();
                if (LockScreenUtils.getInstance(getContext()).isSecure()) {
                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                } else {
                    intent.setClassName("com.android.camera", "com.android.camera.CameraActivity");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getContext().startActivity(intent);
            } catch (Exception e) {
                try {
                    Intent intent = new Intent();
                    if (LockScreenUtils.getInstance(getContext()).isSecure()) {
                        intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                    } else {
                        intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };*/
	 // Aurora liugj 2014-06-16 deleted for MusicLockScreen end

    public boolean isCameraScrolling() {
        float scrollX = mPager.getScrollX();
        int displayW = getContext().getResources().getDisplayMetrics().widthPixels;
        boolean b = (( int ) scrollX % displayW) == 0;
        return !b;
    }

    public boolean isUnlockScrolling() {
        float scrollY = getScrollY();
        int displayH = getContext().getResources().getDisplayMetrics().heightPixels;
        boolean b = (( int ) scrollY % displayH) == 0;
        return !b;
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
		// Aurora liugj 2014-06-16 added for MusicLockScreen start
        mStatusView.setKeyguardCallback(callback);
		// Aurora liugj 2014-06-16 added for MusicLockScreen end
    }

    @Override
    public void setLockPatternUtils(LockPatternUtils utils) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause() {
		// Aurora liugj 2014-06-16 modified for MusicLockScreen start
        /*if (mScroller != null) {
            mScroller.abortAnimation();
            scrollTo(0, 0);
            mPager.setCurrentItem(0);
        }*/
    	mStatusView.onPause();
	    // Aurora liugj 2014-06-16 modified for MusicLockScreen end
		// Aurora liugj 2014-09-12 added for 防误触 start
		// Aurora liugj 2014-09-22 deleted for 防误触 start
    	/*if (mRegisterSensorFlg) {
            mSensorMgr.unregisterListener(mProSensorEventListener);
            mSensorMgr.unregisterListener(mLightSensorEventListener);
            mRegisterSensorFlg = false;
        }*/
		 // Aurora liugj 2014-09-22 deleted for 防误触 end
		 // Aurora liugj 2014-09-12 added for 防误触 end
    }

    @Override
    public void playDisAppearAnim() {
        createDisapperingAnim().start();
    }

	// Aurora liugj 2014-06-16 modified for MusicLockScreen start
    @Override
    public void onResume(int reason) {
    	Log.d("liugj2", TAG+"====onResume===="+reason);
    	if (reason == KeyguardSecurityView.SCREEN_ON) {
        	mStatusView.onResume();
            /*if (!LockScreenUtils.getInstance(getContext()).isSecure()) {
                mWallPaperBg.setVisibility(View.VISIBLE);
//                mWallPaper.setBackground(WallpaperManager.getInstance(getContext()).getDrawable());
//                mWallPaper.setBackgroundColor(Color.BLACK);
                LockScreenBgUtils.getInstance().setViewBg(mWallPaper);
            }*/

			// Aurora liugj 2014-09-22 deleted for 防误触 start
            /*if(mProximitySensor != null && mLightSensor != null && !mRegisterSensorFlg) {
            	Log.d("liugj4", "Selector=====onResume=====registerProSensorEventListener");
                mSensorMgr.registerListener(mProSensorEventListener, mProximitySensor,  SensorManager.SENSOR_DELAY_NORMAL); 
                mSensorMgr.registerListener(mLightSensorEventListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mRegisterSensorFlg = true; 
            }*/
			// Aurora liugj 2014-09-22 deleted for 防误触 end
		}else if (reason == KeyguardSecurityView.VIEW_MUSIC) {
			mStatusView.refreshView();
	    	mStatusView.refreshBattery();
		}// add by tangjie 2015-03-25 for BUG #12584 start 
		else if(reason == KeyguardSecurityView.VIEW_REVEALED){
			mStatusView.onResume();
		}
    	// add by tangjie 2015-03-25 for BUG #12584 end
    }
	// Aurora liugj 2014-06-16 modified for MusicLockScreen end
    
	 // Aurora liugj 2014-09-22 deleted for 防误触 start
	 // Aurora liugj 2014-09-12 added for 防误触 start
    //private float mProximityThreshold = MAX_FLOAT_VALUE;
    
    /*private final SensorEventListener  mProSensorEventListener = new SensorEventListener() {

    	@Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
    		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
    			final float distance = event.values[0];
    			mProSensorFlag = (distance >= 0.0f && distance <= mProximityThreshold);
                post(new Runnable() {
    				
    				@Override
    				public void run() {
    					mStatusView.setSensorMessage(mProSensorFlag, mLightSensorFlag);
    				}
    			});
    		}
        }

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

    };*/
	 // Aurora liugj 2014-09-12 added for 防误触 end
    
	 // Aurora liugj 2014-09-18 added for 防误触 start
	 /*private final SensorEventListener  mLightSensorEventListener = new SensorEventListener() {

    	@Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
    		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
    			float value = event.values[0];
    			mLightSensorFlag = (value >= 0.0f && value <= MAX_FLOAT_VALUE);
    			post(new Runnable() {
    				
    				@Override
    				public void run() {
    					mStatusView.setSensorMessage(mProSensorFlag, mLightSensorFlag);
    				}
    			});
			}
        }

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

    };*/
	 // Aurora liugj 2014-09-18 added for 防误触 end
	 // Aurora liugj 2014-09-22 deleted for 防误触 end

    @Override
    public boolean needsInput() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        // TODO Auto-generated method stub
        return mCallback;
    }

    @Override
    public void showUsabilityHint() {
        // TODO Auto-generated method stub

    }

    @Override
    public void showBouncer(int duration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void hideBouncer(int duration) {
        // TODO Auto-generated method stub

    }
    
    public void setAlphaBackgroundView(AlphaBackground background) {
		// Aurora liugj 2014-06-16 modified for MusicLockScreen start
        //mAlphaBackground = background;
    	mStatusView.setAlphaBackgroundView(background);
		// Aurora liugj 2014-06-16 modified for MusicLockScreen end
    }

    public void backgroundAlphaChanged(AlphaBackground background, float frac) {
//        logd("backgroundAlphaChanged: f=" + frac);
        final int fullAlpha = 255;
        final int H = getHeight() / 2;
        frac = Math.min(frac, H);
        float alpha = 1f;
        alpha = fullAlpha - (fullAlpha * frac / H);
        background.setAlpha((int)alpha);
    }

    private Animator createDisapperingAnim(){
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0.0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("translationY", -300f);
        ObjectAnimator anim = AnimUtils.ofPropertyValuesHolder(mStatusView, pvhAlpha, pvhY);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.addListener(new AnimatorListenerAdapter() {
            
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                View view = (View) ((ObjectAnimator)animation).getTarget();
                view.setTranslationY(0);
                view.setAlpha(1.0f);
            }
            
        });
        return anim;
    }

    @Override
    public void playAppearAnim() {
        // TODO Auto-generated method stub
        
    }
    
    private void changeViewPagerFling() {
        final float density = getContext().getResources().getDisplayMetrics().density;
        try {
            Field field = ViewPager.class.getDeclaredField("mFlingDistance");
            field.setAccessible(true);
            field.setInt(mPager, ( int ) (125 * density));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
