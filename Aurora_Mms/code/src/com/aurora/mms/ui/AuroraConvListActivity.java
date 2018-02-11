/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.aurora.mms.ui;

import java.util.List;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
// Aurora xuyong 2014-09-29 modified for bug #8949 start
import com.aurora.mms.ui.AuroraSettingPreferencesActivity;
// Aurora xuyong 2014-09-29 modified for bug #8949 end
//import com.android.mms.ui.SearchActivity;
//import com.gionee.aora.numarea.export.INumAreaManager;
//import com.gionee.aora.numarea.export.INumAreaObserver;
//import com.gionee.aora.numarea.export.IUpdataResult;
//import com.gionee.aora.numarea.export.NumAreaInfo;
import com.gionee.mms.ui.DraftFragment.DeleteDraftListener;

import android.R.integer;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Activity;
// Aurora liugj 2013-09-20 added for aurora's new feature start
import android.app.Application;
// Aurora liugj 2013-09-20 added for aurora's new feature end
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import aurora.app.AuroraProgressDialog;
// Aurora liugj 2013-09-16 deleted for aurora's new feature start
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuItem;
// Aurora liugj 2013-10-09 modified for aurora's new feature start
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
// Aurora liugj 2013-10-09 modified for aurora's new feature end
// Aurora liugj 2013-09-16 deleted for aurora's new feature end
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.widget.SearchView;
import aurora.widget.AuroraSearchView;
import android.widget.Toast;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import aurora.preference.AuroraPreferenceManager;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.provider.Telephony.Sms;
import android.os.SystemProperties;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
// gionee zhouyj 2012-06-19 add for CR00613899 start 
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import java.io.File;
import android.os.Environment;
//gionee zhouyj 2012-06-19 add for CR00613899 end 
import android.view.inputmethod.InputMethodManager;
// gionee zhouyj 2012-07-31 add for CR00662942 start 
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
// gionee zhouyj 2012-07-31 add for CR00662942 end 
//gionee kangjiajia 2012-10-11 add for CR00710910
import com.gionee.mms.online.activity.RecommendFragment;
import com.gionee.mms.popup.PopUpMsgActivity;
//gionee kangjiajia 2012-10-11 add for CR00710910

//Gionee liuxiangrong 2012-10-16 add for CR00714584 start
// Aurora xuyong 2013-11-15 modified for S4 adapt start
import gionee.provider.GnTelephony.SIMInfo;
// Aurora xuyong 2013-11-15 modified for S4 adapt end
import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2014-09-01 added for bug #7751 start
import com.aurora.mms.util.Utils;
// Aurora xuyong 2014-09-01 added for bug #7751 end
import com.android.mms.ui.SelectCardPreferenceActivity;
import com.android.mms.ui.ManageSimMessages;
//Gionee liuxiangrong 2012-10-16 add for CR00714584 end

import gionee.content.GnIntent;
import gionee.app.GnStatusBarManager;
//gionee lwzh modify for CR00774362 20130227
import gionee.provider.GnSettings;

//Gionee <gaoj> <2013-05-21> added for CR00817770 begin
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import com.android.mms.ui.ScaleDetector;
import com.android.mms.ui.ScaleDetector.OnScaleListener;
//Gionee <gaoj> <2013-05-21> added for CR00817770
// Aurora xuyong 2015-12-11 added for N1 CTS test start
import aurora.widget.AuroraCheckBox;
// Aurora xuyong 2015-12-11 added for N1 CTS test end

// Aurora liugj 2013-09-16 deleted for aurora's new feature start
public class AuroraConvListActivity extends AuroraActivity {
    // Aurora xuyong 2015-12-22 modified for aurora 2.0 new feature start
    // Aurora xuyong 2016-01-22 modified for bug #18550 start
    private static final int AURORA_SETTING_BUTTON_ID = 2;
    // Aurora xuyong 2016-01-22 modified for bug #18550 end
    private static final int AURORA_SEARCH_BUTTON_ID = 1;
    // Aurora xuyong 2015-12-22 modified for aurora 2.0 new feature end
// Aurora liugj 2013-09-16 deleted for aurora's new feature end
    
    private ConvFragment mConvFragment;
    
    private SharedPreferences mPrefs;
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private PostDrawListener mPostDrawListener = new PostDrawListener();
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    private boolean mLaunched = false;

    private MenuItem mSearchItem;
//    private AuroraSearchView mSearchView;
    // gionee zhouyj 2012-04-28 added for CR00585947 start
    public static Context sContext;
    // Aurora liugj 2013-09-20 added for aurora's new feature start
    public static Application sApp;
    // Aurora liugj 2013-09-20 added for aurora's new feature end
    // gionee zhouyj 2012-04-28 added for CR00585947 end
    //gionee gaoj 2012-5-25 added for CR00421454 start
    // Aurora xuyong 2014-11-12 modified for bug #9759 start
    public static AuroraActivity sAuroraConvListActivity;
    // Aurora xuyong 2014-11-12 modified for bug #9759 end
    //gionee gaoj 2012-5-25 added for CR00421454 end
    // gionee zhouyj 2012-06-19 add for CR00613899 start 
    // Aurora liugj 2013-09-16 deleted for aurora's new feature start
    private AuroraActionBar mActionBar;
    // Aurora liugj 2013-09-16 deleted for aurora's new feature end
    private boolean mSearchMode = false;
    // gionee zhouyj 2012-06-19 add for CR00613899 end
    private ImageButton mAddMmsButton;
    
    //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
    private static final Boolean gnQMflag = SystemProperties.get("ro.gn.oversea.custom").equals("PAKISTAN_QMOBILE");
    //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
    protected void onCreate(Bundle arg0) {
        //gionee gaoj 2012-5-30 added for CR00601661 start
//        if (MmsApp.mTransparent) {
//            setTheme(R.style.TabActivityTheme);
//        } else if (MmsApp.mLightTheme) {
//            setTheme(R.style.GnMmsLightTheme);
//        } else if (MmsApp.mDarkTheme) {
//            setTheme(R.style.GnMmsDarkTheme);
//        }
        //gionee gaoj 2012-5-30 added for CR00601661 end

        // gionee zhouyj 2012-04-28 added for CR00585947 start
        sContext = this.getApplicationContext();
        // Aurora xuyong 2014-10-24 added for privacy feature start
        Utils.addInstance(this);
        // Aurora xuyong 2014-10-24 added for privacy feature end
        // Aurora liugj 2013-09-20 added for aurora's new feature start
        sApp = this.getApplication();
        // Aurora liugj 2013-09-20 added for aurora's new feature end
        // gionee zhouyj 2012-04-28 added for CR00585947 end
        super.onCreate(arg0);
        // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature start
        getSharedPreferences(ConvFragment.CONV_NOTIFICATION, AuroraActivity.MODE_PRIVATE).edit().putInt(ConvFragment.CONV_NOTIFICATION_INDEX,
                0).commit();
        // Aurora xuyong 2016-01-27 added for aurora 2.0 new feature end
        final Intent intent = getIntent();
        //Gionee <guoyx> <2013-05-03> modified for CR00797658 begin
         // Aurora liugj 2013-09-16 deleted for aurora's new feature start
         // Aurora liugj 2013-09-20 modified for aurora's new feature start
          // Aurora liugj 2013-12-03 modified for search animation start
          // Aurora liugj 2013-12-26 modified for start optimize start
        setAuroraContentView(R.layout.aurora_conversation_list_activity, AuroraActionBar.Type.Empty/*, true*/);
          // Aurora liugj 2013-12-26 modified for start optimize end
          // Aurora liugj 2013-12-03 modified for search animation end
        // Aurora liugj 2013-09-20 modified for aurora's new feature end
        
        //Gionee <guoyx> <2013-05-03> modified for CR00797658 end
        mActionBar = getAuroraActionBar();
        // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature start
        setAuroraActionbarSplitLineVisibility(View.GONE);
        mActionBar.setElevation(0f);
        // Aurora xuyong 2015-12-29 added for aurora 2.0 new feature end
       // Aurora liugj 2013-09-16 deleted for aurora's new feature end
        
        //gionee gaoj added for CR00725602 20121201 start
        /*if (MmsApp.mLightTheme) {
            mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_light_bg));
            mActionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_light_bg));
        } else {
            mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_dark_bg));
            mActionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_dark_bg));
        }*/
        //gionee gaoj added for CR00725602 20121201 end
        // Aurora xuyong 2015-12-11 added for N1 CTS test start
        if (needShowConfirmDialog()) {
            showConfirmDialog(this);
        }
        // Aurora xuyong 2015-12-11 added for N1 CTS test end
        // Aurora xuyong 2016-03-10 modified for bug #20942 start
        int tabIndex = intent.getIntExtra("notification_index", -1);
        mConvFragment = new ConvFragment(tabIndex);
        // Aurora xuyong 2016-03-10 modified for bug #20942 end
        getFragmentManager().beginTransaction().add(R.id.fragment_container, mConvFragment).commit();
        
          // Aurora liugj 2013-09-16 deleted for aurora's new feature start
        initAuroraActionBar();
        // Aurora liugj 2013-09-16 deleted for aurora's new feature end
        
        mLaunched = true;
        //getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
        //        ActionBar.DISPLAY_SHOW_CUSTOM);
        //gionee gaoj 2012-5-25 added for CR00421454 start
        sAuroraConvListActivity = AuroraConvListActivity.this;
        //gionee gaoj 2012-5-25 added for CR00421454 end
        // gionee zhouyj 2012-06-19 add for CR00613899 start 

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
        filter.addAction("android.intent.action.THEME_CHANGED"/*Intent.ACTION_THEME_CHANGED*/);
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
        registerReceiver(mReceiver, filter);
        // gionee zhouyj 2012-07-31 add for CR00662942 end 

        //gionee gaoj 2012-9-21 added for CR00687379 start
        if (MmsApp.mGnMtkGeminiSupport) {
            mSinIndicatorReceiver = new SimIndicatorBroadcastReceiver();
            IntentFilter intentFilter =
                new IntentFilter(GnIntent.ACTION_SMS_DEFAULT_SIM_CHANGED);
            registerReceiver(mSinIndicatorReceiver, intentFilter);
        }
        //gionee gaoj 2012-9-21 added for CR00687379 end
        
        // Aurora liugj 2013-09-16 deleted for aurora's new feature start
        // Aurora liugj 2013-12-26 modified for start optimize start
        new Handler().post(new Runnable() {
            
            @Override
            public void run() {
                addSearchviewInwindowLayout();
                // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature start
                setAuroraSystemMenuCallBack(auroraMenuCallBack);
                // Aurora xuyong 2016-01-14 modified for aurora 2.0 new feature end
                setAuroraMenuItems(R.menu.aurora_conversation_list_menu);
            }
        });
        // Aurora liugj 2013-12-26 modified for start optimize end
      // Aurora liugj 2013-09-16 deleted for aurora's new feature end
        // Aurora xuyong 2014-09-01 added for bug #7751 start
        if (Utils.hasKitKat()) {
            Utils.kitKatDefaultMsgCheck(this);
        }
        // Aurora xuyong 2014-09-01 added for bug #7751 end
    }
    // Aurora xuyong 2015-12-11 added for N1 CTS test start

    private void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private final String MMS_AUTHORITY_CONFIRM = "mms_authority_check";
    private final String MMS_AUTHORITY_CONFIRM_TAG = "mms_authority_confirmed";

    private void putConfirmStatus(AuroraCheckBox checkBox) {
        if (checkBox.isChecked()) {
            getSharedPreferences(MMS_AUTHORITY_CONFIRM, AuroraActivity.MODE_PRIVATE).edit().putBoolean(MMS_AUTHORITY_CONFIRM_TAG, true).commit();
        }
    }

    private boolean needShowConfirmDialog() {
        return !getSharedPreferences(MMS_AUTHORITY_CONFIRM, AuroraActivity.MODE_PRIVATE).getBoolean(MMS_AUTHORITY_CONFIRM_TAG, false);
    }

    private void showConfirmDialog(Context context) {
        View showContent = LayoutInflater.from(context).inflate(R.layout.aurora_alert_authority_confirm_dialog, null);
        final AuroraCheckBox checkBox = (AuroraCheckBox)showContent.findViewById(R.id.checkbox);
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature start
        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature end
                .setTitle(R.string.sys_auth_dlg_title)
                .setView(showContent)
                .setPositiveButton(R.string.aurora_continue, new DialogInterface.OnClickListener() {

                    @Override
                    public final void onClick(DialogInterface dialog, int which) {
                        putConfirmStatus(checkBox);
                    }

                })
                .setNegativeButton(R.string.aurora_exit,  new DialogInterface.OnClickListener() {

                    @Override
                    public final void onClick(DialogInterface dialog, int which) {
                        killProcess();
                    }
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature start
                }).create();
        // Aurora xuyong 2016-04-11 added for bug #22165 start
        dialog.setCancelable(false);
        // Aurora xuyong 2016-04-11 added for bug #22165 end
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature end
    }
    // Aurora xuyong 2015-12-11 added for N1 CTS test end
    
    // Aurora liugj 2013-09-16 deleted for aurora's new feature start
    private void initAuroraActionBar() {
        // Aurora xuyong 2015-12-22 deleted for aurora 2.0 new feature start
        //addAuroraActionBarItem(AuroraActionBarItem.Type.Add, ACTION_BTN_NEW);
        // Aurora xuyong 2015-12-22 deleted for aurora 2.0 new feature end
        mActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
        mActionBar.setTitle(R.string.app_label);
        // Aurora xuyong 2015-12-22 added for aurora 2.0 new feature start
        mActionBar.addItem(AuroraActionBarItem.Type.Search, AURORA_SEARCH_BUTTON_ID);
        // Aurora xuyong 2016-01-22 modified for bug #18550 start
        mActionBar.addItem(AuroraActionBarItem.Type.Set, AURORA_SETTING_BUTTON_ID);
        // Aurora xuyong 2016-01-22 modified for bug #18550 end
        // Aurora xuyong 2015-12-22 added for aurora 2.0 new feature end
        // Aurora liugj 2013-09-20 modified for aurora's new feature start
        //mActionBar.setDisplayHomeAsUpEnabled(false);
        // Aurora liugj 2013-09-20 modified for aurora's new feature end
    }
    
    private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
                // Aurora xuyong 2015-12-22 modified for aurora 2.0 new feature start
                // Aurora xuyong 2016-01-22 modified for bug #18550 start
                case AURORA_SETTING_BUTTON_ID:
                // Aurora xuyong 2016-01-22 modified for bug #18550 end
                    // Aurora xuyong 2015-12-30 modified for aurora 2.0 new feature start
                    //showAuroraMenu();
                    Intent intent = new Intent(AuroraConvListActivity.this, AuroraSettingPreferencesActivity.class);
                    startActivityIfNeeded(intent, -1);
                    // Aurora xuyong 2015-12-30 modified for aurora 2.0 new feature end
                    break;
                case AURORA_SEARCH_BUTTON_ID:
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature start
                    //startActivity(ComposeMessageActivity.createIntent(AuroraConvListActivity.this, 0));
                    mConvFragment.gotoSearchMode(mActionBar);
                    // Aurora xuyong 2016-01-06 added for bug #18286 start
                    showInputMethod();
                    // Aurora xuyong 2016-01-06 added for bug #18286 end
                    // Aurora xuyong 2015-12-15 modified for aurora 2.0 new feature end
                    break;
                default:
                    break;
                // Aurora xuyong 2015-12-22 modified for aurora 2.0 new feature end
            }
        }
    };
    // Aurora xuyong 2016-01-06 added for bug #18286 start
    private void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
    // Aurora xuyong 2016-01-06 added for bug #18286 end
    // Aurora xuyong 2014-03-21 modified for aurora's new feature start
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // Aurora xuyong 2016-01-11 added for aurora 2.0 new feature start
            case KeyEvent.KEYCODE_MENU:
                 // do nothing
                 return true;
            // Aurora xuyong 2016-01-11 added for aurora 2.0 new feature end
            case KeyEvent.KEYCODE_BACK:
                 // Aurora xuyong 2014-03-22 modified for aurora's new feature start
                 if (mActionBar != null && (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
                 // Aurora xuyong 2014-03-22 modified for aurora's new feature end
                     return true;
                 }
                 if (mConvFragment != null && mConvFragment.isBottomMenuShowing()) {
                     mConvFragment.leaveBatchMode();
                     return true;
                 }
                 break;
        }
        return super.onKeyDown(keyCode, event);
    }
    // Aurora xuyong 2014-03-21 modified for aurora's new feature end
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.aurora_menu_settings:
             // Aurora xuyong 2014-09-29 modified for bug #8949 start
                Intent intent = new Intent(AuroraConvListActivity.this, AuroraSettingPreferencesActivity.class);
             // Aurora xuyong 2014-09-29 modified for bug #8949 end
                startActivityIfNeeded(intent, -1);
                break;
            default:
                break;
            }
        }
    };

    /*private void initActionBar(){
        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
                .inflate(R.layout.aurora_titlebar, null);
        
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        TextView titleView = (TextView) v.findViewById(R.id.mms_title);
        mAddMmsButton = (ImageButton) v.findViewById(R.id.add_mms_button);
        mAddMmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ComposeMessageActivity.createIntent(AuroraConvListActivity.this, 0));
            }
        });
        mActionBar.setCustomView(v);
//        mActionBar.setDisplayShowTitleEnabled(true);
//        mActionBar.setDisplayShowHomeEnabled(false);
//        mActionBar.setDisplayHomeAsUpEnabled(true); 
    }*/
    // Aurora liugj 2013-09-16 deleted for aurora's new feature end    

    @Override
    protected void onNewIntent(Intent newIntent) {
        // TODO Auto-generated method stub
        super.onNewIntent(newIntent);
        setIntent(newIntent);
    }
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    /*private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
        public boolean onPostDraw() {
            //add below line for Log
            return true;
        }
    }*/
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        // gionee zhouyj 2013-04-02 add for CR00792181 start
        if (PopUpMsgActivity.sPopUpMsgActivity != null) {
            PopUpMsgActivity.sPopUpMsgActivity.finish();
        }
        // gionee zhouyj 2013-04-02 add for CR00792181 end
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //gionee gaoj 2012-9-21 added for CR00687379 start
        if (MmsApp.mGnMtkGeminiSupport) {
            //Log.d("Test", "onResume, setSimIndicatorVisibility ");
            setSimIndicatorVisibility(true);
            mShowSimIndicator = true;
        }
        //gionee gaoj 2012-9-21 added for CR00687379 end
        // gionee zhouyj 2012-11-14 add for CR00728779 start 
        /*if (mSearchMode) {
            String text = "" + mSearchView.getQuery();
//            exitSearchMode();
//            enterSearchMode();
            mSearchView.setQuery(text, false);
        }*/
        // gionee zhouyj 2012-11-14 add for CR00728779 end 
        
        //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
          // Aurora liugj 2013-10-25 modified for fix bug-238 start 
        /*if (MmsApp.mGnHideEncryption) {
            mIsCmcc = true;
            if (mScaleDetector == null) {
                mScaleDetector = new ScaleDetector(this, new ScaleListener());
            }
        }*/
          // Aurora liugj 2013-10-25 modified for fix bug-238 end 
        //Gionee <gaoj> <2013-05-21> added for CR00817770 end
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // gionee zhouyj 2012-07-27 add for CR00657773 start 
        // Aurora xuyong 2016-01-06 deleted for bug #18286 start
        /*if (mSearchMode) {
            hideInputMethod();
        }*/
        // Aurora xuyong 2016-01-06 deleted for bug #18286 end
        // gionee zhouyj 2012-07-27 add for CR00657773 end 

        //gionee gaoj 2012-9-21 added for CR00687379 start
        if (MmsApp.mGnMtkGeminiSupport) {
            Log.d("Test", "onPause, setSimIndicatorVisibility ");
            setSimIndicatorVisibility(false);
            mShowSimIndicator = false;
        }
        //gionee gaoj 2012-9-21 added for CR00687379 end
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Aurora xuyong 2014-10-24 added for privacy feature start
        Utils.removeInstance(this);
        // Aurora xuyong 2014-10-24 added for privacy feature end
        // gionee zhouyj 2012-07-31 add for CR00662942 start 
        unregisterReceiver(mReceiver);
        // gionee zhouyj 2012-07-31 add for CR00662942 end

        //gionee gaoj 2012-9-21 added for CR00687379 start
        if (MmsApp.mGnMtkGeminiSupport) {
            if (mSinIndicatorReceiver != null) {
                unregisterReceiver(mSinIndicatorReceiver);
                mSinIndicatorReceiver = null;
            }
        }
        //gionee gaoj 2012-9-21 added for CR00687379 end
        //Gionee <zhouyj> <2013-05-04> add for CR00801649 begin
        if (mActionBar != null && mConvFragment != null) {
//            exitSearchMode();
        }
        //Gionee <zhouyj> <2013-05-04> add for CR00801649 end
    }

    //gionee gaoj 2012-9-21 added for CR00687379 start
 // New feature for SimIndicator begin
    private StatusBarManager mStatusBarMgr = null;
    private boolean mShowSimIndicator = false;
    private SimIndicatorBroadcastReceiver mSinIndicatorReceiver = null;

    private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MmsApp.mGnMtkGeminiSupport) {
                Log.d("Test", "SimIndicatorBroadcastReceiver, onReceive ");
                if (action.equals(GnIntent.ACTION_SMS_DEFAULT_SIM_CHANGED)) {
                    Log.d("Test", "SimIndicatorBroadcastReceiver, onReceive, mShowSimIndicator= "
                            + mShowSimIndicator);
                    if (true == mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    }
                }
            }
        }
    }

    void setSimIndicatorVisibility(boolean visible) {
        if(mStatusBarMgr == null)
            mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        // lwzh OMIGO_TODO
        if (visible)
            GnStatusBarManager.showSIMIndicator(mStatusBarMgr, getComponentName(), GnSettings.System.SMS_SIM_SETTING);
        else
            GnStatusBarManager.hideSIMIndicator(mStatusBarMgr, getComponentName());
    }
    // New feature for SimIndicator end
    //gionee gaoj 2012-9-21 added for CR00687379 end

    //gionee gaoj added for CR00725602 20121201 start
    private MenuItem mCreateNewItem;
     
     // Aurora liugj 2013-09-16 deleted for aurora's new feature start    
    /*public boolean onCreateOptionsMenu(Menu menu) {
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
        if(gnQMflag){
            getMenuInflater().inflate(R.menu.gn_qm_conversation_list_menu, menu);
        }else{
            getMenuInflater().inflate(R.menu.gn_conversation_list_menu, menu); //E6执行该步
        }
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
        
        getMenuInflater().inflate(R.menu.aurora_conversation_list_menu, menu);
        
        return super.onCreateOptionsMenu(menu);
    }*/
    // Aurora liugj 2013-09-16 deleted for aurora's new feature end    

    private MenuItem mActionInOutItem;
    private MenuItem mActionSynItem;
    
     // Aurora liugj 2013-09-16 deleted for aurora's new feature start
    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
            if (mConvFragment != null) {
                mConvFragment.onPrepareOptionsMenu(menu);
            }
            //Gionee <zhouyj> <2013-04-25> modify for CR00802357 start
            mSearchItem = menu.findItem(R.id.gn_search);
            mActionInOutItem = menu.findItem(R.id.gn_action_in_out);
            mActionSynItem = menu.findItem(R.id.gn_action_synchronizer);
            if (mSearchMode) {
                mSearchItem.setVisible(false);
            } else {
                mSearchItem.setVisible(true);
            }
            if (MmsApp.mIsSafeModeSupport) {
                mActionInOutItem.setEnabled(false);
            } else {
                mActionInOutItem.setEnabled(true);
            }
            if (!MmsApp.mGnCloudBackupSupport || !MmsApp.isGnSynchronizerSupport()) {
                mActionSynItem.setVisible(false);
            } else {
                mActionSynItem.setVisible(true);
                if (MmsApp.mIsSafeModeSupport){
                    mActionSynItem.setEnabled(false);
                } else {
                    mActionSynItem.setEnabled(true);
                }
            }
            //Gionee <zhouyj> <2013-04-25> modify for CR00802357 end 
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        return super.onPrepareOptionsMenu(menu);
    }*/
     // Aurora liugj 2013-09-16 deleted for aurora's new feature end
    
    //Gionee <zhouyj> <2013-08-07> add for CR00850690 begin
    /*@Override
    public boolean onOptionsItemLongClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.gn_action_compose_new:
            Log.i("AuroraConvListActivity", "Aurora AuroraConvListActivity onOptionsItemLongClick create new message with VoiceHelper");
            Intent i = new Intent(AuroraConvListActivity.this, ComposeMessageActivity.class);
            i.putExtra("voice_helper", true);
            startActivity(i);
            break;
        default:
            break;
        }
        return true;
    }*/
    //Gionee <zhouyj> <2013-08-07> add for CR00850690 end

     // Aurora liugj 2013-11-20 deleted for bug-927 start
    /*public boolean onSearchRequested() {
        startSearch(null, false, null, false);
        return true;
    };*/
     // Aurora liugj 2013-11-20 deleted for bug-927 end
    
     // Aurora liugj 2013-09-16 deleted for aurora's new feature start
    /*@SuppressWarnings("deprecation")
    @Override
    // gionee zhouyj 2012-05-16 modify for CR00601094 start
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case R.id.gn_action_compose_new:
            startActivity(ComposeMessageActivity.createIntent(this, 0));
            break;
        case R.id.gn_action_settings:
            Intent intent = new Intent(this, MessagingPreferenceActivity.class);
            startActivityIfNeeded(intent, -1);
            break;
        case R.id.gn_search:
            // gionee zhouyj 2012-04-27 annoted for CR00585541 start
            //onSearchRequested();
            // gionee zhouyj 2012-04-27 annoted for CR00585541 end
            // gionee zhouyj 2012-06-19 add for CR00613899 start 
            enterSearchMode();
            // gionee zhouyj 2012-06-19 add for CR00613899 end 
            break;
        case R.id.gn_action_exchange:
            Intent pimIntent = getPackageManager().getLaunchIntentForPackage("com.gionee.aora.pim");
            pimIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            pimIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            pimIntent.putExtra("pim_sms", 1);
            startActivity(pimIntent);
            break;
        case R.id.gn_action_in_out:
            Intent impExpIntent = new Intent("android.intent.action.ImportExportSmsActivity");
            startActivity(impExpIntent);
            break;
        case R.id.gn_action_cancel_all_favorite:
            //gionee gaoj 2012-8-7 added for CR00667606 start
//            showGnDialog();
            //gionee gaoj 2012-8-7 added for CR00667606 end
            break;
        case 5:
        case R.id.gn_action_delete_all_draft:
//            mDraftFragment.onOptionsItemSelected(item);
            break;
        case R.id.gn_action_delete_all:
            mConvFragment.onOptionsItemSelected(item);
            break;
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
        case R.id.gn_action_sim_messages:
            if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                List<SIMInfo> listSimInfo = SIMInfo.getInsertedSIMList(this);
            if (listSimInfo.size() > 1) { 
                Intent simSmsIntent = new Intent();
                simSmsIntent.setClass(this, SelectCardPreferenceActivity.class);
                simSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                simSmsIntent.putExtra("preference", MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES);
                startActivity(simSmsIntent);
            } else {  
                Intent simSmsIntent = new Intent();
                simSmsIntent.setClass(this, ManageSimMessages.class);
                simSmsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                simSmsIntent.putExtra("SlotId", listSimInfo.get(0).mSlot); 
                startActivity(simSmsIntent);
            }
            } else { 
                startActivity(new Intent(this, ManageSimMessages.class));
            }
            break;
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
        //gionee gaoj 2012-12-10 added for CR00741704 start
        case R.id.gn_action_doctoran:
            Intent qqintent = new Intent();
            qqintent.setAction("com.tencent.gionee.interceptcenter");
            qqintent.putExtra("tab_name", "tab_msg");
            qqintent.putExtra("from", "gionee");
            startActivity(qqintent);
            break;
        //gionee gaoj 2012-12-10 added for CR00741704 end
            
            //gionee gaoj added for CR00725602 20121201 start
        case R.id.gn_action_batch_operation:
            mConvFragment.onOptionsItemSelected(item);
            break;
        case R.id.gn_action_encryption:
            mConvFragment.onOptionsItemSelected(item);
            break;
            //gionee gaoj added for CR00725602 20121201 end
            //Gionee <zhouyj> <2013-04-25> add for CR00802357 start
        case R.id.gn_action_synchronizer:
            try {
                Intent i = new Intent();
                i.setClassName("gn.com.android.synchronizer",
                        "gn.com.android.synchronizer.WelcomeActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                Log.e("TabActiviy", "gn.com.android.synchronizer not found! e = " + e.toString());
            } 
            break;
            //Gionee <zhouyj> <2013-04-25> add for CR00802357 end
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }*/
    // gionee zhouyj 2012-05-16 modify for CR00601094 end
     // Aurora liugj 2013-09-16 deleted for aurora's new feature end

    //gionee gaoj 2012-8-7 added for CR00667606 start
    /*private void showGnDialog() {
        new AuroraAlertDialog.Builder(this)//, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
//        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.gn_cancel_all_favorite_title)
        .setMessage(R.string.gn_cancel_all_favorite_content)
        .setPositiveButton(R.string.gn_cancel_all_favorite_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        cancelAllFavorite();
                    }
                })
        .setNegativeButton(R.string.gn_cancel_all_favorite_cancel_btn, null).show();
    }*/
    //gionee gaoj 2012-8-7 added for CR00667606 end

//    private void cancelAllFavorite() {
//        ContentValues values = new ContentValues(1);
//        values.put("star", 0);
//        getApplicationContext().getContentResolver().update(Sms.CONTENT_URI,values,"star=1",null);
//        mFavoritesFragment.refreshFavData();
//    }

    private final View.OnLayoutChangeListener mFirstLayoutListener
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            v.removeOnLayoutChangeListener(this); // Unregister self.
        }
    };

    public boolean onContextItemSelected(MenuItem item) {
        if (mConvFragment != null) {
            mConvFragment.onContextItemSelected(item);
        }
        return true;
    };
    
    // gionee zhouyj 2012-04-27 added for CR00585541 start 
    /*private MenuItem.OnActionExpandListener mExpandCollapseListener = new MenuItem.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem arg0) {
            // TODO Auto-generated method stub
            mSearchView.setBackgroundColor(android.R.color.transparent);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            return true;
        }
        
        @Override
        public boolean onMenuItemActionCollapse(MenuItem arg0) {
            // TODO Auto-generated method stub
//            mActionBar.setDisplayShowCustomEnabled(false);
//            mActionBar.setDisplayShowHomeEnabled(false);
//            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            return true;
        }
    };*/
    
    /*AuroraSearchView.OnQueryTextListener mQueryTextListener = new AuroraSearchView.OnQueryTextListener() {
        public boolean onQueryTextSubmit(String query) {
            Intent intent = new Intent();
            intent.setClass(AuroraConvListActivity.this, SearchActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
            return true;
        }

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };*/
    // gionee zhouyj 2012-04-27 added for CR00585541 end
    
    // gionee zhouyj 2012-06-19 add for CR00613899 start 
    /*private void enterSearchMode() {
        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.gn_searchview, null);
        
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ImageButton quit = (ImageButton) v.findViewById(R.id.back_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitSearchMode();
            }
        });
        mSearchView = (AuroraSearchView) v.findViewById(R.id.search_view);
        mSearchView.requestFocus();
        mSearchView.onActionViewExpanded();
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(mQueryTextListener);
        mSearchView.setQueryHint(getString(R.string.gn_search_hint));
        mSearchView.setBackgroundColor(android.R.color.transparent);
        mSearchView.setOnCloseListener(this);
        mSearchMode = true;
        //gionee gaoj 2012-10-12 added for CR00711168 start
        if (!MmsApp.mIsSafeModeSupport) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(this.getComponentName());
            mSearchView.setSearchableInfo(info);
        }
        }
        //gionee gaoj 2012-10-12 added for CR00711168 end
        mActionBar.setCustomView(v);
        invalidateOptionsMenu();
        //gionee gaoj 2012-6-30 added for CR00632246 start
        if (mConvFragment != null) {
            mConvFragment.setListViewWatcher(new ConvFragment.ListViewWatcher() {

                @Override
                public void listViewChanged(boolean isChange) {
                    // TODO Auto-generated method stub
                    if (isChange && mSearchMode) {
                        //Gionee <zhouyj> <2013-06-17> modify for CR00826647 start
                        if (mSearchView != null && mSearchView.getSuggestionsAdapter() != null) {
                            mSearchView.getSuggestionsAdapter().notifyDataSetChanged();
                        }
                        //Gionee <zhouyj> <2013-04-25> modify for CR00826647 end
                        exitSearchMode();
                    }
                }
            });
        }
        //gionee gaoj 2012-6-30 added for CR00632246 end
    }
    
    private void exitSearchMode() {
        mSearchMode = false;
        // gionee zhouyj 2012-08-08 modify for CR00663845 start 
        hideInputMethod();
        // gionee zhouyj 2012-08-08 modify for CR00663845 end 
//        mActionBar.setDisplayShowCustomEnabled(true);
//        mActionBar.setDisplayShowTitleEnabled(true);
//        mActionBar.setDisplayShowHomeEnabled(false);
//        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initActionBar();
        // gionee zhouyj 2012-08-11 modify for CR00664390 start 
        if (mConvFragment != null) {
            mConvFragment.setListViewWatcher(null);
        }
        // gionee zhouyj 2012-08-11 modify for CR00664390 end 
        invalidateOptionsMenu();
    }*/
    
    // gionee zhouyj 2012-07-27 add for CR00657773 start 
    private void hideInputMethod() {
        InputMethodManager inputMethodManager =
            (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(getWindow()!=null && getWindow().getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }
    // gionee zhouyj 2012-07-27 add for CR00657773 end 
    
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if(mConvFragment.mSearchMode) {
              // Aurora liugj 2013-10-10 modified for aurora's new feature start
            // Aurora liugj 2013-12-03 modified for search animation start
            hideSearchviewLayout();
            // Aurora liugj 2013-12-03 modified for search animation end
              // Aurora liugj 2013-10-10 modified for aurora's new feature end
        } else {
            // Aurora liugj 2014-01-07 added for bug:when delete visible onBackPressed activity finished start
            // Aurora xuyong 2015-12-04 deleted for aurora2.0 new feature start
            /*if (mConvFragment.hideDeleteBack()) {
                return;
            }*/
            // Aurora xuyong 2015-12-04 deleted for aurora2.0 new feature end
            // Aurora liugj 2014-01-07 added for bug:when delete visible onBackPressed activity finished end
            //gionee gaoj 2012-7-16 added for CR00628364 start
            // Aurora xuyong 2015-09-09 deleted for bug #15978 start
            //if (isTaskRoot()) {
            //    moveTaskToBack(false);
            //} else {
            // Aurora xuyong 2015-09-09 deleted for bug #15978 end
                super.onBackPressed();
            // Aurora xuyong 2015-09-09 deleted for bug #15978 start
            //}
            // Aurora xuyong 2015-09-09 deleted for bug #15978 end
            //gionee gaoj 2012-7-16 added for CR00628364 end
        }
    }

    // gionee zhouyj 2012-06-19 add for CR00613899 end 
    
    public static boolean checkMsgImportExportSms() {
        return MmsApp.mGnMessageSupport && SystemProperties.get("ro.gn.export.import.support").equals("yes");
    }
    
    public static boolean checkDisturb() {
        return false;
    }

    // gionee zhouyj 2012-07-31 add for CR00662942 start 
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            if (Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())
                    || "android.intent.action.THEME_CHANGED"/*Intent.ACTION_THEME_CHANGED*/.equals(intent.getAction())) {
            // Aurora xuyong 2013-11-15 modified for S4 adapt end
                if (null != mConvFragment) {
                    mConvFragment.leaveForChanged();
                }
            }
        }
    };
    // gionee zhouyj 2012-07-31 add for CR00662942 end
    // Aurora liugj 2013-10-25 modified for fix bug-238 start 
    //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
    /*private boolean mIsCmcc = false;
    private ScaleDetector mScaleDetector = null;*/

    /*public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = false;
        if(mIsCmcc && mScaleDetector != null &&
                mConvFragment != null && !mConvFragment.isEncryptionList &&
                mConvFragment.ReadPopTag(sContext, mConvFragment.FIRSTENCRYPTION)){
                ret = mScaleDetector.onTouchEvent(event);
        }
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    };*/

    /*public class ScaleListener implements OnScaleListener{

        @Override
        public boolean onScaleStart(ScaleDetector detector) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public void onScaleEnd(ScaleDetector detector) {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean onScale(ScaleDetector detector) {
            // TODO Auto-generated method stub
            if (detector.getScaleFactor() < 1 && mConvFragment.mHideEncryp == false) {
                mConvFragment.setEncryptionHide(true);
            } else if (detector.getScaleFactor() > 1 && mConvFragment.mHideEncryp == true) {
                mConvFragment.setEncryptionHide(false);
            }
            return true;
        }
    }*/
    //Gionee <gaoj> <2013-05-21> added for CR00817770 end
    // Aurora liugj 2013-10-25 modified for fix bug-238 end
}
