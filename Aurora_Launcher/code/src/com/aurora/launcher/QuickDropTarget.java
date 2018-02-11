package com.aurora.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class QuickDropTarget extends Button {

	protected Launcher mLauncher;
	
	protected Drawable mDrawbleAnim;
	protected Drawable mDrawbleNormal;
	
	protected  AnimationDrawable mButtonDropTargetAnimationDrawable;
	
	public QuickDropTarget(Context context) {
		super(context);
		mLauncher = (Launcher) context;
	}

	public QuickDropTarget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
	}

	public QuickDropTarget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mLauncher = (Launcher) context;
	}

	public interface QuickDropTargetAction {
		public void handleDrop(View v);

		public void getWidgetRect(Rect r);

		public void startDropTargetAnim();

		public void stopDropTargetAnim();

		public void resetDropTargetBackground();
	}
	
	public void initialize(Context c){
		if(mClearRunnable!=null){
			this.removeCallbacks(mClearRunnable);
		}
	}
	public void handleDrop(View v){}
	public void getWidgetRect(Rect r){}
	
	
	public void startDropTargetAnim(){
		setBackgroundDrawable(mDrawbleAnim);
		mButtonDropTargetAnimationDrawable = (AnimationDrawable) getBackground();
		mButtonDropTargetAnimationDrawable.start();
	}
	
	public void stopDropTargetAnim(){
		if (mButtonDropTargetAnimationDrawable != null) {
			if (mButtonDropTargetAnimationDrawable.isRunning()) {
				mButtonDropTargetAnimationDrawable.stop();
				setBackgroundDrawable(null);
			}
		}
	}
	
	public void resetDropTargetBackground(){
		setBackgroundDrawable(mDrawbleNormal);	
	}
	
	public void clear(){
		if(mClearRunnable!=null){
			this.removeCallbacks(mClearRunnable);
			this.postDelayed(mClearRunnable, 1000);
		}
	}
	
	Runnable mClearRunnable = new Runnable() {
		@Override
		public void run() {
			mDrawbleAnim = null;
			mDrawbleNormal =  null;
			mButtonDropTargetAnimationDrawable = null;
			resetDropTargetBackground();
		}
	};
	
}
