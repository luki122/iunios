package com.android.auroramusic.model;



import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;




public final class AuroraAnimationModel extends Animation{

	private static final String TAG = "AuroraAnimationModel";
	private OnAnimationListener mListener;
	private final WeakReference<View> mView;
	private static final WeakHashMap<View, AuroraAnimationModel> mPoex = new WeakHashMap<View, AuroraAnimationModel>();
	
	private Handler mHandler;
	private float mInterpolatedTime = 0f;
	private Transformation mtTransformation;
	
	
	public static interface OnAnimationListener{
		public void onAnimationCallBack(View view, float interpolatedTime, Transformation t);
	}
	
	public static AuroraAnimationModel createAnimation(View view) {
		AuroraAnimationModel proxy = mPoex.get(view);
        if (proxy == null || proxy != view.getAnimation()) {
            proxy = new AuroraAnimationModel(view);
            mPoex.put(view, proxy);
        }
        return proxy;
    }

	public AuroraAnimationModel(View view) {
		mView = new WeakReference<View>(view);
		this.mHandler = new Handler();
	}
	
	public void setMyAnimationListener(OnAnimationListener listener) {
		this.mListener = listener;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		//this.mInterpolatedTime = interpolatedTime;
		//this.mtTransformation = t;
		View view = mView.get();
		if (mListener != null) {
			mListener.onAnimationCallBack(view, interpolatedTime, t);
		} 
	}

	public static void clear() {
		if (mPoex != null) {
			mPoex.clear();
		}
		return;
	}
}
