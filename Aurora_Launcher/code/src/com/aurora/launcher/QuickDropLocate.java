package com.aurora.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

public class QuickDropLocate extends QuickDropTarget {

	private static final String TAG = "QuickDropLocate";
	public static final int  CATEGORIES_WORKSPACE = 1;
	public static final int  CATEGORIES_FOLDER = 2;
	
	public QuickDropLocate(Context context) {
		super(context);
		//initialize(context);
	}

	public QuickDropLocate(Context context, AttributeSet attrs) {
		super(context, attrs);
		//initialize(context);
	}

	public QuickDropLocate(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//initialize(context);
	}

	@Override
	public void handleDrop(View v) {
		processQuickLocateAction(v);
	}

	@Override
	public void getWidgetRect(Rect r) {
		getGlobalVisibleRect(r);
	}

	@Override
	public void initialize(Context c) {
		super.initialize(c);
	    //mLauncher = (Launcher) c;
	    Resources r = getResources();
	    mDrawbleAnim = r.getDrawable(R.drawable.quick_index_locate_zone_animation);
	    mDrawbleNormal = r.getDrawable(R.drawable.quick_locate_normal);
		resetDropTargetBackground();
	}
	private void processQuickLocateAction(View v) {
		ItemInfo info = (ItemInfo) v.getTag();
		Log.i("process","mLauncher = "+mLauncher+" this = "+this);
		if ((int) info.container != LauncherSettings.Favorites.CONTAINER_DESKTOP
				&& (int) info.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			mLauncher.getDragController().exitTransModeWithTime(350);
			mLauncher.sendOpenFolderMessage(v,CATEGORIES_FOLDER);
		} else {
			if (info.container != LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
				mLauncher.getDragController().exitTransModeWithTime(350);
				mLauncher.sendOpenFolderMessage(v,CATEGORIES_WORKSPACE);
			}else{
				mLauncher.getDragController().exitTransModeWithTime(350);
				mLauncher.startQuickSearchItemAnim(v);
			}
		}
	}

}
