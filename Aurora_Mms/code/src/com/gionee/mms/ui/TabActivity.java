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
package com.gionee.mms.ui;

import java.util.List;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessagingPreferenceActivity;
//import com.android.mms.ui.SearchActivity;
//import com.gionee.aora.numarea.export.INumAreaManager;
//import com.gionee.aora.numarea.export.INumAreaObserver;
//import com.gionee.aora.numarea.export.IUpdataResult;
//import com.gionee.aora.numarea.export.NumAreaInfo;
import com.gionee.mms.ui.DraftFragment.DeleteDraftListener;

import android.R.integer;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import aurora.app.AuroraProgressDialog;
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
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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
// Aurora liugj 2013-09-13 modified for aurora's new feature start
// Aurora xuyong 2013-11-15 modified for S4 adapt start
import gionee.provider.GnTelephony.SIMInfo;
// Aurora xuyong 2013-11-15 modified for S4 adapt end
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import com.aurora.featureoption.FeatureOption;
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
//Gionee <gaoj> <2013-05-21> added for CR00817770 end

public class TabActivity extends AuroraActivity implements AuroraSearchView.OnCloseListener{

    //gionee gaoj 2013-4-1 modified for CR00788343 start
    private ConvFragment mConvFragment;
    private FavoritesFragment mFavoritesFragment;
    private DraftFragment mDraftFragment;
    private RecommendFragment mRecommendFragment;
    private static int TAB_INDEX_COUNT = 4;
    //gionee gaoj 2013-4-1 modified for CR00788343 end
    
    private int mLastManuallySelectedFragment;
    private SharedPreferences mPrefs;
    private static final int PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT = 0;

    /** Last manually selected tab index */
    private static final String PREF_LAST_MANUALLY_SELECTED_TAB =
            "TabActivity_last_manually_selected_tab";
   // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
   // private PostDrawListener mPostDrawListener = new PostDrawListener();
   // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    private boolean mLaunched = false;
    private ViewPagerAdapter mViewPagerAdapter;
    /** Enables horizontal swipe between Fragments. */
    private ViewPager mViewPager;
    private final PageChangeListener mPageChangeListener = new PageChangeListener();

    private MenuItem mSearchItem;
    private AuroraSearchView mSearchView;
    // gionee zhouyj 2012-04-28 added for CR00585947 start
    public static Context sContext;
    // gionee zhouyj 2012-04-28 added for CR00585947 end
    //gionee gaoj 2012-5-25 added for CR00421454 start
    public static AuroraActivity sTabActivity;
    //gionee gaoj 2012-5-25 added for CR00421454 end
    // gionee zhouyj 2012-06-19 add for CR00613899 start
    // Aurora liugj 2013-09-13 modified for aurora's new feature start
    private ActionBar mActionBar;
    // Aurora liugj 2013-09-13 modified for aurora's new feature end
    private boolean mSearchMode = false;
    // gionee zhouyj 2012-06-19 add for CR00613899 end

    //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
    private static final Boolean gnQMflag = SystemProperties.get("ro.gn.oversea.custom").equals("PAKISTAN_QMOBILE");
    //Gionee liuxiangrong 2012-10-16 add for CR00714584 end
    
    /**
     * Listener interface for Fragments accommodated in {@link ViewPager} enabling them to know
     * when it becomes visible or invisible inside the ViewPager.
     */
    public interface ViewPagerVisibilityListener {
        public void onVisibilityChanged(boolean visible);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        public ConvFragment mConvFragment;
        
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return (mConvFragment = new ConvFragment());
                case 1:
                    return new FavoritesFragment();
                case 2:
                    //gionee gaoj 2013-4-1 modified for CR00788343 start
                    if (MmsApp.mIsDraftOpen) {
                        return new DraftFragment();
                    }
                    if (MmsApp.mIsNeedOnLine) {
                        return new RecommendFragment();
                    }
                    //gionee gaoj 2013-4-1 modified for CR00788343 end
                    //gionee kangjiajia 2012-10-11 add for inline_Mms start 
                case 3:
                    if (MmsApp.mIsNeedOnLine) {
                        return new RecommendFragment();
                    } else {
                        return new DraftFragment();
                    }
                    // gionee kangjiajia 2012-10-11 add for inline_Mms end
            }
            throw new IllegalStateException("No fragment at position " + position);
        }

        public int getCount() {
            return TAB_INDEX_COUNT;
        }
    }

    private class PageChangeListener implements OnPageChangeListener {
        private int mCurrentPosition = -1;
        /**
         * Used during page migration, to remember the next position {@link #onPageSelected(int)}
         * specified.
         */
        private int mNextPosition = -1;

        public void onPageScrolled(
                int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
             // Aurora liugj 2013-09-13 modified for aurora's new feature start
            final ActionBar actionBar = mActionBar;
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            if (mCurrentPosition == position) {
            }

            actionBar.selectTab(actionBar.getTabAt(position));
            mNextPosition = position;
        }

        public void setCurrentPosition(int position) {
            mCurrentPosition = position;
        }

        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE: {
                    if (mCurrentPosition >= 0) {
                        sendFragmentVisibilityChange(mCurrentPosition, false);
                    }
                    if (mNextPosition >= 0) {
                        sendFragmentVisibilityChange(mNextPosition, true);
                    }
                    invalidateOptionsMenu();

                    mCurrentPosition = mNextPosition;
                    break;
                }
                case ViewPager.SCROLL_STATE_DRAGGING:
                case ViewPager.SCROLL_STATE_SETTLING:
                default:
                    break;
            }
        }
    }

    private final TabListener mTabListener = new TabListener() {
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
            // TODO Auto-generated method stub

            if (mViewPager.getCurrentItem() != arg0.getPosition()) {
                mViewPager.setCurrentItem(arg0.getPosition(), true);
            }

            mLastManuallySelectedFragment = arg0.getPosition();
        }

    };

    protected void onCreate(Bundle arg0) {
        //gionee gaoj 2012-5-30 added for CR00601661 start
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        } else if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkTheme) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-5-30 added for CR00601661 end

        // gionee zhouyj 2012-04-28 added for CR00585947 start
        sContext = this;
        // gionee zhouyj 2012-04-28 added for CR00585947 end
        super.onCreate(arg0);

        final Intent intent = getIntent();
        //Gionee <guoyx> <2013-05-03> modified for CR00797658 begin
        if (MmsApp.mGnPerfOpt1Support) {
            setContentView(R.layout.gn_tab_frame_viewstub);
        } else {
            setContentView(R.layout.gn_tab_frame);
        }
        //Gionee <guoyx> <2013-05-03> modified for CR00797658 end
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mActionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end

        //gionee gaoj added for CR00725602 20121201 start
        /*if (MmsApp.mLightTheme) {
            //mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_light_bg));
            mActionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_light_bg));
        } else {
            mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_dark_bg));
            mActionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.gn_tab_dark_bg));
        }*/
        //gionee gaoj added for CR00725602 20121201 end
        
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        initViewPager();
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        
        //gionee gaoj 2013-4-1 modified for CR00788343 start
        setupConvTab();
        setupFavoritesTab();
        mActionBar.getTabAt(0).setText(R.string.tab_message);
        mActionBar.getTabAt(1).setText(R.string.tab_favorites);
        
        TAB_INDEX_COUNT = 2;
        if (MmsApp.mIsDraftOpen) {
            TAB_INDEX_COUNT++;
            setupDraftTab();
            mActionBar.getTabAt(TAB_INDEX_COUNT - 1).setText(R.string.tab_draft);
        }
        
        if (MmsApp.mIsNeedOnLine) {
            TAB_INDEX_COUNT++;
            setupRecommodTab();
            mActionBar.getTabAt(TAB_INDEX_COUNT - 1).setText(R.string.tab_recommend);
        }
        //gionee gaoj 2013-4-1 modified for CR00788343 end

        setCurrentTab(intent);
        mLaunched = true;
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        //getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
        //        ActionBar.DISPLAY_SHOW_CUSTOM);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        //gionee gaoj 2012-5-25 added for CR00421454 start
        sTabActivity = TabActivity.this;
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
    }
    //Gionee <guoyx> <2013-04-17> added for CR00797658 begin
    public void initViewPager() {
        if (MmsApp.mGnPerfOpt1Support) {
        if (mViewPager == null) {
            mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
            ViewStub viewStub = (ViewStub) findViewById(R.id.viewstub_pager);
            LinearLayout ll = (LinearLayout) viewStub.inflate();
            mViewPager = (ViewPager)ll.findViewById(R.id.pager);
            mViewPager.setAdapter(mViewPagerAdapter);
            mViewPager.setOnPageChangeListener(mPageChangeListener);
            mViewPager.setOffscreenPageLimit(2);
        }
        } else {
        mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setOffscreenPageLimit(2);
        }
    }
    //Gionee <guoyx> <2013-04-17> added for CR00797658 end

    public void setupConvTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_message);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    public void setupFavoritesTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_favorites);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    public void setupRecommodTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_recommend);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    public void setupDraftTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_draft);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    /**
     * Sets the current tab based on the intent's request type
     *
     * @param intent Intent that contains information about which tab should be selected
     */
    private void setCurrentTab(Intent intent) {
        // Remember the old manually selected tab index so that it can be restored if it is
        // overwritten by one of the programmatic tab selections

        final int tabIndex = 0;
        final int previousItemIndex = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(tabIndex, false /* smoothScroll */);
        if (previousItemIndex != tabIndex) {
            sendFragmentVisibilityChange(previousItemIndex, false);
        }
        mPageChangeListener.setCurrentPosition(tabIndex);
        sendFragmentVisibilityChange(tabIndex, true);

    }

    private void sendFragmentVisibilityChange(int position, boolean visibility) {
        final Fragment fragment = getFragmentAt(position);
        if (fragment instanceof ViewPagerVisibilityListener) {
            ((ViewPagerVisibilityListener) fragment).onVisibilityChanged(visibility);
        }
    }

    private Fragment getFragmentAt(int position) {
        switch (position) {
            case 0:
                return mConvFragment;
            case 1:
                return mFavoritesFragment;
            case 2:
                //gionee gaoj 2013-4-1 modified for CR00788343 start
                if (MmsApp.mIsDraftOpen) {
                    return mDraftFragment;
                }
                if (MmsApp.mIsNeedOnLine) {
                    return mRecommendFragment;
                }
                //gionee gaoj 2013-4-1 modified for CR00788343 end
                // gionee kangjiajia 2012-10-11 add for inline_Mms start
            case 3:
                if (MmsApp.mIsNeedOnLine) {
                    return mRecommendFragment;
                } else {
                    return mDraftFragment;
                }
                // gionee kangjiajia 2012-10-11 add for inline_Mms end
            default:
                throw new IllegalStateException("Unknown fragment index: " + position);
        }
      }

    @Override
    protected void onNewIntent(Intent newIntent) {
        // TODO Auto-generated method stub
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        setCurrentTab(newIntent);
    }
   // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
   /* private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
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
            Log.d("Test", "onResume, setSimIndicatorVisibility ");
            setSimIndicatorVisibility(true);
            mShowSimIndicator = true;
        }
        //gionee gaoj 2012-9-21 added for CR00687379 end
        // gionee zhouyj 2012-11-14 add for CR00728779 start 
        if (mSearchMode) {
            String text = "" + mSearchView.getQuery();
            exitSearchMode();
            enterSearchMode();
            mSearchView.setQuery(text, false);
        }
        // gionee zhouyj 2012-11-14 add for CR00728779 end 
        //Gionee <zhouyj> <2013-04-22> add for CR00797045 start
        int pos = mViewPager.getCurrentItem();
        if (pos > 0) {
            sendFragmentVisibilityChange(pos, true);
        }
        //Gionee <zhouyj> <2013-04-22> add for CR00797045 end
        
        //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
        if (MmsApp.mGnHideEncryption) {
            mIsCmcc = true;
            if (mScaleDetector == null) {
                mScaleDetector = new ScaleDetector(this, new ScaleListener());
            }
        }
        //Gionee <gaoj> <2013-05-21> added for CR00817770 end
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // gionee zhouyj 2012-07-27 add for CR00657773 start 
        if (mSearchMode) {
            hideInputMethod();
        }
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
            exitSearchMode();
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        // TODO Auto-generated method stub
        super.onAttachFragment(fragment);
        final int currentPosition = mViewPager != null ? mViewPager.getCurrentItem() : -1;
        if (fragment instanceof ConvFragment) {
            mConvFragment = (ConvFragment) fragment;
        } else if (fragment instanceof FavoritesFragment) {
            mFavoritesFragment = (FavoritesFragment) fragment;
       } else if (fragment instanceof RecommendFragment) { // add by jiajia for inline
                mRecommendFragment = (RecommendFragment) fragment;
        } else {
            mDraftFragment = (DraftFragment) fragment;
        }
    }

    //gionee gaoj added for CR00725602 20121201 start
    private MenuItem mCreateNewItem;

    public boolean onCreateOptionsMenu(Menu menu) {
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 start
        if(gnQMflag){
            getMenuInflater().inflate(R.menu.gn_qm_conversation_list_menu, menu);
        }else{
            getMenuInflater().inflate(R.menu.gn_conversation_list_menu, menu);
        }
        //Gionee liuxiangrong 2012-10-16 add for CR00714584 end

        return super.onCreateOptionsMenu(menu);
    }

    private MenuItem mActionInOutItem;
    private MenuItem mActionSynItem;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 begin
        if (mViewPager != null) {
            final int position = mViewPager.getCurrentItem(); 
            final Fragment fragment = getFragmentAt(position);
            if (fragment != null) {
                fragment.onPrepareOptionsMenu(menu);
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
        }
        //Gionee <guoyx> <2013-04-17> modified for CR00797658 end
        return super.onPrepareOptionsMenu(menu);
    }
    
    //Gionee <zhouyj> <2013-08-07> add for CR00850690 begin
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
    /*@Override
    public boolean onOptionsItemLongClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.gn_action_compose_new:
            Log.i("TabActivity", "Aurora TabActivity onOptionsItemLongClick create new message with VoiceHelper");
            Intent i = new Intent(TabActivity.this, ComposeMessageActivity.class);
            i.putExtra("voice_helper", true);
            startActivity(i);
            break;
        default:
            break;
        }
        return true;
    }*/
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    //Gionee <zhouyj> <2013-08-07> add for CR00850690 end

    public boolean onSearchRequested() {
        startSearch(null, false, null, false);
        return true;
    };

    @SuppressWarnings("deprecation")
    @Override
    // gionee zhouyj 2012-05-16 modify for CR00601094 start
    public boolean onOptionsItemSelected(MenuItem item) {
        final int position = mViewPager.getCurrentItem();
        final Fragment fragment = getFragmentAt(position);
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
            showGnDialog();
            //gionee gaoj 2012-8-7 added for CR00667606 end
            break;
        case 5:
        case R.id.gn_action_delete_all_draft:
            mDraftFragment.onOptionsItemSelected(item);
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
            fragment.onOptionsItemSelected(item);
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
    }
    // gionee zhouyj 2012-05-16 modify for CR00601094 end

    //gionee gaoj 2012-8-7 added for CR00667606 start
    private void showGnDialog() {
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
    }
    //gionee gaoj 2012-8-7 added for CR00667606 end

    private void cancelAllFavorite() {
        ContentValues values = new ContentValues(1);
        values.put("star", 0);
        getApplicationContext().getContentResolver().update(Sms.CONTENT_URI,values,"star=1",null);
        mFavoritesFragment.refreshFavData();
    }

    private final View.OnLayoutChangeListener mFirstLayoutListener
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            v.removeOnLayoutChangeListener(this); // Unregister self.
        }
    };

    public boolean onContextItemSelected(MenuItem item) {
        final int position = mViewPager.getCurrentItem();
        final Fragment fragment = getFragmentAt(position);
        if (fragment != null) {
            fragment.onContextItemSelected(item);
        }
        return true;
    };
    
    // gionee zhouyj 2012-04-27 added for CR00585541 start 
    private MenuItem.OnActionExpandListener mExpandCollapseListener = new MenuItem.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem arg0) {
            // TODO Auto-generated method stub
            mSearchView.setBackgroundColor(android.R.color.transparent);
            mActionBar.setDisplayShowCustomEnabled(true);
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            return true;
        }
        
        @Override
        public boolean onMenuItemActionCollapse(MenuItem arg0) {
            // TODO Auto-generated method stub
            mActionBar.setDisplayShowCustomEnabled(false);
            mActionBar.setDisplayShowHomeEnabled(false);
            // Aurora liugj 2013-09-13 modified for aurora's new feature start
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // Aurora liugj 2013-09-13 modified for aurora's new feature end
            return true;
        }
    };
    
    AuroraSearchView.OnQueryTextListener mQueryTextListener = new AuroraSearchView.OnQueryTextListener() {
          // Aurora liugj 2013-10-25 modified for aurora's new feature start
        public boolean onQueryTextSubmit(String query) {
            /*Intent intent = new Intent();
            intent.setClass(TabActivity.this, SearchActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);*/
            return true;
        }
          // Aurora liugj 2013-10-25 modified for aurora's new feature end

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };
    // gionee zhouyj 2012-04-27 added for CR00585541 end
    
    // gionee zhouyj 2012-06-19 add for CR00613899 start 
    private void enterSearchMode() {
        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.gn_searchview, null);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
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
        mActionBar.setDisplayShowCustomEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        // gionee zhouyj 2012-08-11 modify for CR00664390 start 
        if (mConvFragment != null) {
            mConvFragment.setListViewWatcher(null);
        }
        // gionee zhouyj 2012-08-11 modify for CR00664390 end 
        invalidateOptionsMenu();
    }
    
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
        if(mSearchMode) {
            exitSearchMode();
        } else {
            //gionee gaoj 2012-7-16 added for CR00628364 start
            if (isTaskRoot()) {
                moveTaskToBack(false);
            } else {
                super.onBackPressed();
            }
            //gionee gaoj 2012-7-16 added for CR00628364 end
        }
    }

    @Override
    public boolean onClose() {
        // TODO Auto-generated method stub
        mSearchView.onActionViewExpanded();
        mSearchView.setQuery(null, false);
        return false;
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
    
    //Gionee <gaoj> <2013-05-21> added for CR00817770 begin
    private boolean mIsCmcc = false;
    private ScaleDetector mScaleDetector = null;

    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = false;
        final int position = mViewPager.getCurrentItem();
        if(mIsCmcc && mScaleDetector != null && position == 0 &&
                mConvFragment != null && !mConvFragment.isEncryptionList &&
                mConvFragment.ReadPopTag(sContext, mConvFragment.FIRSTENCRYPTION)){
                ret = mScaleDetector.onTouchEvent(event);
        }
        if(!ret){
            ret = super.dispatchTouchEvent(event); 
        }
        return ret;
    };

    public class ScaleListener implements OnScaleListener{

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
    }
    //Gionee <gaoj> <2013-05-21> added for CR00817770 end
}
