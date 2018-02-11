/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.android.gallery3d.R;
import com.android.gallery3d.anim.StateTransitionAnimation;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.PreparePageFadeoutTexture;
import com.android.gallery3d.ui.RawTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.fragmentapp.GridViewFragment;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;

abstract public class ActivityState {
    protected static final int FLAG_HIDE_ACTION_BAR = 1;
    protected static final int FLAG_HIDE_STATUS_BAR = 2;
    protected static final int FLAG_SCREEN_ON_WHEN_PLUGGED = 4;
    protected static final int FLAG_SCREEN_ON_ALWAYS = 8;
    protected static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON = 16;
    protected static final int FLAG_SHOW_WHEN_LOCKED = 32;

    protected AbstractGalleryActivity mActivity;
    protected Bundle mData;
    protected int mFlags;

    protected ResultEntry mReceivedResults;
    protected ResultEntry mResult;

    protected static class ResultEntry {
        public int requestCode;
        public int resultCode = Activity.RESULT_CANCELED;
        public Intent resultData;
    }

    protected boolean mHapticsEnabled;
    private ContentResolver mContentResolver;

    private boolean mDestroyed = false;
    private boolean mPlugged = false;
    boolean mIsFinishing = false;

    private static final String KEY_TRANSITION_IN = "transition-in";

    private StateTransitionAnimation.Transition mNextTransition =
            StateTransitionAnimation.Transition.None;
    private StateTransitionAnimation mIntroAnimation;
    private GLView mContentPane;
    
    private boolean bWhiteBack = true;
    

    protected ActivityState() {
    }

    protected void setContentPane(GLView content) {
        mContentPane = content;
        if (mIntroAnimation != null) {
            mContentPane.setIntroAnimation(mIntroAnimation);
            mIntroAnimation = null;
        }
        //Log.i("zll", "zll ---- setContentPane bWhiteBack:"+bWhiteBack);
        mContentPane.setBackgroundColor(getBackgroundColor());
        mActivity.getGLRoot().setContentPane(mContentPane);
    }
    
    //Iuni <lory><2014-02-28> add begin
    protected void setContentPaneBackGroud(boolean bWhiteCoclor){
    	//Log.i("zll", "zll ---- setContentPaneBackGroud bWhiteBack:"+bWhiteBack+",bWhiteCoclor:"+bWhiteCoclor);
    	if (bWhiteBack != bWhiteCoclor) {
    		bWhiteBack = bWhiteCoclor;
    		if (mContentPane != null) {
    			mContentPane.setBackgroundColor(getBackgroundColor());
			}
		}
    }
    //Iuni <lory><2014-02-28> add end

    void initialize(AbstractGalleryActivity activity, Bundle data) {
        mActivity = activity;
        mData = data;
        mContentResolver = activity.getAndroidContext().getContentResolver();
    }

    public Bundle getData() {
        return mData;
    }

    protected void onBackPressed() {
        mActivity.getStateManager().finishState(this);
    }
    
    //lory add
    protected void onSetSelectIndex(int index) {
        
    }
    
    //lory add 12.6
    protected boolean onMyKeyDownEvent(int keyCode, KeyEvent event) {
        return false;
    }

    protected void setStateResult(int resultCode, Intent data) {
        if (mResult == null) return;
        mResult.resultCode = resultCode;
        mResult.resultData = data;
    }

    protected void onConfigurationChanged(Configuration config) {
    }

    protected void onSaveState(Bundle outState) {
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
    }

    protected float[] mWhitegroundColor;
    protected float[] mBackgroundColor;//Iuni <lory><2014-02-28> add begin

    protected int getBackgroundColorId() {
    	//Iuni <lory><2014-02-28> modify 
    	//Log.i("zll", "zll ---- dddd getBackgroundColorId bWhiteBack:"+bWhiteBack);
    	if (bWhiteBack) {
    		return R.color.default_background;
		} else {
			return R.color.default_two_background;
		}
        //return R.color.default_background;
    }

    protected float[] getBackgroundColor() {
    	//Log.i("zll", "zll ---- dddd getBackgroundColor bWhiteBack:"+bWhiteBack);
    	//Iuni <lory><2014-02-28> modify 
    	if (bWhiteBack) {
    		return mWhitegroundColor;
		} else {
			return mBackgroundColor;
		}
        //return mBackgroundColor;
    }

    protected void onCreate(Bundle data, Bundle storedState) {
    	mWhitegroundColor = GalleryUtils.intColorToFloatARGBArray(Color.parseColor("#ffffff"));//Iuni <lory><2014-01-16> add for test
    	//Iuni <lory><2014-02-28> modify 
        //Aurora <SQF> <2014-07-23>  for NEW_UI begin
        //ORIGINALLY:
    	//mBackgroundColor  = GalleryUtils.intColorToFloatARGBArray(Color.parseColor("#1A1A1A"));//(mActivity.getResources().getColor(getBackgroundColorId()));
        //SQF MODIFIED TO:
    	mBackgroundColor  = GalleryUtils.intColorToFloatARGBArray(Color.parseColor("#0F0F0F"));//(mActivity.getResources().getColor(getBackgroundColorId()));
        //Aurora <SQF> <2014-07-23>  for NEW_UI end
    	
    }

    protected void clearStateResult() {
    }

    BroadcastReceiver mPowerIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                boolean plugged = (0 != intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0));

                if (plugged != mPlugged) {
                    mPlugged = plugged;
                    setScreenFlags();
                }
            }
        }
    };

    private void setScreenFlags() {
        final Window win = mActivity.getWindow();
        final WindowManager.LayoutParams params = win.getAttributes();
        if ((0 != (mFlags & FLAG_SCREEN_ON_ALWAYS)) ||
                (mPlugged && 0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED))) {
            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }
        if (0 != (mFlags & FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)) {
            params.flags |= WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        }
        if (0 != (mFlags & FLAG_SHOW_WHEN_LOCKED)) {
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        }
        win.setAttributes(params);
    }

    protected void transitionOnNextPause(Class<? extends ActivityState> outgoing,
            Class<? extends ActivityState> incoming, StateTransitionAnimation.Transition hint) {
		// Aurora <zhanggp> <2013-12-06> modified for gallery begin
        
        /*if (outgoing == PhotoPage.class && incoming == AlbumPage.class) {
            mNextTransition = StateTransitionAnimation.Transition.Outgoing;
        } else if (outgoing == AlbumPage.class && incoming == PhotoPage.class) {
            mNextTransition = StateTransitionAnimation.Transition.PhotoIncoming;
        } else {
            mNextTransition = hint;
        }*/
        
		// Aurora <zhanggp> <2013-12-06> modified for gallery end
    }

    protected void onPause() {
        if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
            ((Activity) mActivity).unregisterReceiver(mPowerIntentReceiver);
        }
		// Aurora <zhanggp> <2013-12-06> modified for gallery begin
		
        /*if (mNextTransition != StateTransitionAnimation.Transition.None) {
            mActivity.getTransitionStore().put(KEY_TRANSITION_IN, mNextTransition);
            PreparePageFadeoutTexture.prepareFadeOutTexture(mActivity, mContentPane);
            mNextTransition = StateTransitionAnimation.Transition.None;
        }*/
        
		// Aurora <zhanggp> <2013-12-06> modified for gallery end
    }

    // should only be called by StateManager
    void resume() {
 
        AbstractGalleryActivity activity = mActivity;
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            if ((mFlags & FLAG_HIDE_ACTION_BAR) != 0) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
            int stateCount = mActivity.getStateManager().getStateCount();
            mActivity.getGalleryActionBar().setDisplayOptions(stateCount > 1, true);
            // Default behavior, this can be overridden in ActivityState's onResume.
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        activity.invalidateOptionsMenu();

        setScreenFlags();
        //Aurora <SQF> <2014-07-15>  for NEW_UI begin
        //ORIGINALLY:
        //boolean lightsOut = ((mFlags & FLAG_HIDE_STATUS_BAR) != 0);
        //mActivity.getGLRoot().setLightsOutMode(lightsOut);
        //SQF MODIFIED TO:
        //mActivity.getGLRoot().setLightsOutMode(false);
        //Aurora <SQF> <2014-07-15>  for NEW_UI end
        
        ResultEntry entry = mReceivedResults;
        if (entry != null) {
            mReceivedResults = null;
            onStateResult(entry.requestCode, entry.resultCode, entry.resultData);
        }
        /*
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	ActivityState top = mActivity.getStateManager().getTopState();
        	if (AlbumPage.class == top.getClass()) {
        		top.onStateResult(AlbumPage.MSG_ANIMATION_INVIEW, -1, null);
			}
		}
		*/
        if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
            // we need to know whether the device is plugged in to do this correctly
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            activity.registerReceiver(mPowerIntentReceiver, filter);
        }

        try {
            mHapticsEnabled = Settings.System.getInt(mContentResolver,
                    Settings.System.HAPTIC_FEEDBACK_ENABLED) != 0;
        } catch (SettingNotFoundException e) {
            mHapticsEnabled = false;
        }
        
        onResume();

        // the transition store should be cleared after resume;
        mActivity.getTransitionStore().clear();
    }

    // a subclass of ActivityState should override the method to resume itself
    protected void onResume() {
		// Aurora <zhanggp> <2013-12-06> modified for gallery begin
    	
        /*RawTexture fade = mActivity.getTransitionStore().get(
                PreparePageFadeoutTexture.KEY_FADE_TEXTURE);
        mNextTransition = mActivity.getTransitionStore().get(
                KEY_TRANSITION_IN, StateTransitionAnimation.Transition.None);
        if (mNextTransition != StateTransitionAnimation.Transition.None) {
            mIntroAnimation = new StateTransitionAnimation(mNextTransition, fade);
            mNextTransition = StateTransitionAnimation.Transition.None;
        }*/
        
		// Aurora <zhanggp> <2013-12-06> modified for gallery end
    }

    protected boolean onCreateActionBar(Menu menu) {
        // TODO: we should return false if there is no menu to show
        //       this is a workaround for a bug in system
        return true;
    }

    protected boolean onItemSelected(MenuItem item) {
        return false;
    }

    protected void onDestroy() {
        mDestroyed = true;
    }

    boolean isDestroyed() {
        return mDestroyed;
    }

    public boolean isFinishing() {
        return mIsFinishing;
    }

    protected MenuInflater getSupportMenuInflater() {
        return mActivity.getMenuInflater();
    }
    
    public static GridViewFragment mFragment;
    public void setFragmentStatus(GridViewFragment fragment) {
		mFragment = fragment;
	}
    
    public GridViewFragment gettFragmentStatus() {
		return mFragment;
	}
}
