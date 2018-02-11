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

package com.android.settings.notification;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;
import aurora.preference.AuroraPreferenceActivity;

import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.content.ContentProviderOperation;
import com.aurora.utils.SystemUtils;

import android.content.Context;
import com.android.settings.R;


import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;

import com.google.android.collect.Lists;

import aurora.app.AuroraActivity;

public class AuroraNotificationAppListActivity extends AuroraActivity {

    private static final String TAG = "AuroraNotificationAppListActivity";


    /**
     * Showing a list of Contacts. Also used for showing search results in search mode.
     */
    private AuroraNotificationAppList mAllFragment;

    private ImageView mGnAudioSearchView;

    /**
     * If {@link #configureFragments(boolean)} is already called.  Used to avoid calling it twice
     * in {@link #onStart}.
     * (This initialization only needs to be done once in onStart() when the Activity was just
     * created from scratch -- i.e. onCreate() was just called)
     */
    private boolean mFragmentInitialized;
    private AuroraActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        
        setAuroraContentView(R.layout.aurora_notification_app_list_act,
                AuroraActionBar.Type.Normal);

        mActionBar = getAuroraActionBar();
        mActionBar.setTitle(R.string.notify_mgr);
        mActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
        mAllFragment = (AuroraNotificationAppList)getFragmentManager().
                findFragmentById(R.id.aurora_notification_app_list_frg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO move to the fragment
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            if (mActionBar != null && 
                    (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
                return true;
            }
            
            if (mAllFragment.getEditMode()) {
                try {
                    Thread.sleep(300);

                    mActionBar.setShowBottomBarMenu(false);
                    mActionBar.showActionBarDashBoard();
                    mAllFragment.changeToNormalMode();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                } 
            }
            finish();
            overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
            return true;
        }
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    }

    private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
    	       public void onAuroraActionBarBackItemClicked(int itemId) {
    	               switch (itemId) {
    	               case -1:
     	                     finish();
     	                     overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);		
     	                     break;
     	               default:
     	                     break;
                       }
    	       }
    };
}
