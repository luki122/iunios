package com.aurora.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class QuickDropUninstall extends QuickDropTarget {

	private static final String TAG = "QuickDropUninstall";
	
	public QuickDropUninstall(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		//initialize(context);
	}

	public QuickDropUninstall(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		//initialize(context);
	}

	public QuickDropUninstall(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//initialize(context);
	}
	
	@Override
	public void initialize(Context context){
		super.initialize(context);
		//mLauncher = (Launcher) context;
	    Resources r = getResources();
	    mDrawbleAnim = r.getDrawable(R.drawable.quick_index_delete_zone_animation);
	    mDrawbleNormal = r.getDrawable(R.drawable.quick_delete_normal);
		//start it reset 
		resetDropTargetBackground();
	}
	
	@Override
	public void handleDrop(View v) {
		// TODO Auto-generated method stub
		ProcessUninstallApps(v);
	}

	@Override
	public void getWidgetRect(Rect r) {
		// TODO Auto-generated method stub
		getGlobalVisibleRect(r);
	}

	private void ProcessUninstallApps(View v) {
		ShortcutInfo info = (ShortcutInfo) v.getTag();
		ComponentName cn = info.intent.getComponent();
		mLauncher.startAuroraApplicationUninstallActivity(cn, info.flags);
	}
		
}
