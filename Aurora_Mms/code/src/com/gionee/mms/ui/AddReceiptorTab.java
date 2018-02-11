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

import java.util.ArrayList;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;

import android.R.integer;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import aurora.app.AuroraProgressDialog;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import aurora.widget.AuroraButton;
import android.widget.LinearLayout;
import android.widget.TabHost;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
//gionee gaoj 2012-5-14 added for CR00596441 start
import android.app.Dialog;
//gionee gaoj 2012-5-14 added for CR00596441 end
//gionee zhouyj 2012-07-17 add for CR00650522 start 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
//gionee zhouyj 2012-07-17 add for CR00650522 end 
public class AddReceiptorTab extends AuroraActivity {

    /** Used both by {@link ActionBar} and {@link ViewPagerAdapter} */
    private static final int TAB_INDEX_MSGRECENT = 0;
    private static final int TAB_INDEX_CONTACT = 1;
    private static final int TAB_INDEX_GROUP = 2;
    private static final int TAB_INDEX_STAR = 3;
    private static final int TAB_INDEX_COUNT = 4;
    private static final int LANCHE_TAG = 1;
    
    private SharedPreferences mPrefs;

    public static LinearLayout mBottomBttnBar;
    ContactsPicker contactsPicker;

    private static final int MODE_ADD_CONTACT = 1;
    private static final int MODE_INSERT_CONTACT = 2;
    int mMode = MODE_ADD_CONTACT;
    /** Last manually selected tab index */
    private static final String PREF_LAST_MANUALLY_SELECTED_TAB =
            "AddReceiptorTab_last_manually_selected_tab";
    private static final int PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT = TAB_INDEX_CONTACT;
    // gionee zhouyj 2012-07-03 modify for CR00633165 start 
    public static AuroraActivity mCurrent = null;
    // gionee zhouyj 2012-07-03 modify for CR00633165 end 
    final static String TAG = "AddReceiptorTab";
    private boolean mUseOldCache = false;
    private int mLastManuallySelectedFragment;

    /** Enables horizontal swipe between Fragments. */
    private ViewPager mViewPager;
    private final PageChangeListener mPageChangeListener = new PageChangeListener();
    private ContactFragment   mContactFragment;
    private MsgRecentFragment mMsgRecentFragment;
//    private CallLogListFragment   mCalllogFragment;
    private GroupFragment     mGroupFragment;
    private ViewPagerAdapter mViewPagerAdapter;
    private StarFragment mStarFragment;

    private boolean mLaunched = false;
    //gionee gaoj 2012-5-14 added for CR00596441 start
    private static final int DIALOG_ADD = 1;
    AuroraProgressDialog dialog;
    //gionee gaoj 2012-5-14 added for CR00596441 end

    //gionee gaoj 2012-8-3 added for CR00663537 start
    private boolean mIsAttachContacts;
    //gionee gaoj 2012-8-3 added for CR00663537 end
    // Aurora liugj 2013-09-13 modified for aurora's new feature start
    ActionBar mActionBar;
    // Aurora liugj 2013-09-13 modified for aurora's new feature end
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    //private PostDrawListener mPostDrawListener = new PostDrawListener();
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
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
            
            if (mCurrentPosition == position) {
                Log.w("mms", "Previous position and next position became same (" + position + ")");
            }

            //gionee gaoj 2012-6-27 added for CR00628404 start
            if (mActionBar.getTabCount() != TAB_INDEX_COUNT || position < 0 || position > (TAB_INDEX_COUNT - 1)) {
                return;
            }
            //gionee gaoj 2012-6-27 added for CR00628404 end
            mActionBar.selectTab(mActionBar.getTabAt(position));
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
    
    //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
    @Override
    public void onAttachFragment(Fragment fragment) {
        // TODO Auto-generated method stub
        super.onAttachFragment(fragment);
        final int currentPosition = mViewPager != null ? mViewPager.getCurrentItem() : -1;
        if (fragment instanceof MsgRecentFragment) {
            mMsgRecentFragment = (MsgRecentFragment) fragment;
        } else if (fragment instanceof ContactFragment) {
            mContactFragment = (ContactFragment) fragment;
        } else if (fragment instanceof GroupFragment) { // add by jiajia for inline
            mGroupFragment = (GroupFragment) fragment;
        } else {
            mStarFragment = (StarFragment) fragment;
        }
    }
    //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end

    private void sendFragmentVisibilityChange(int position, boolean visibility) {
        final Fragment fragment = getFragmentAt(position);
        if (fragment instanceof ViewPagerVisibilityListener) {
            ((ViewPagerVisibilityListener) fragment).onVisibilityChanged(visibility);
        }
    }

    /**
     * Listener interface for Fragments accommodated in {@link ViewPager} enabling them to know
     * when it becomes visible or invisible inside the ViewPager.
     */
    public interface ViewPagerVisibilityListener {
        public void onVisibilityChanged(boolean visible);
    }

    private Fragment getFragmentAt(int position) {
        switch (position) {
            case TAB_INDEX_CONTACT:
                return mContactFragment;
            case TAB_INDEX_MSGRECENT:
                return mMsgRecentFragment;
//            case TAB_INDEX_CALLLOG:
//                return mCalllogFragment;
            case TAB_INDEX_GROUP:
                return mGroupFragment;
            case TAB_INDEX_STAR:
                return mStarFragment;
            default:
                //Gionee <zhouyj> <2013-04-19> add for CR00798755 start
                Log.e(TAG, "Unknown fragment index: " + position);
                return null;
                //throw new IllegalStateException("Unknown fragment index: " + position);
                //Gionee <zhouyj> <2013-04-19> add for CR00798755 end
        }
    }

    private final TabListener mTabListener = new TabListener() {
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            Log.d("MMS", "TabListener     onTabUnselected");
        }

        @Override
        public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
            // TODO Auto-generated method stub
            Log.d("MMS", "TabListener     onTabReselected");
        }

        @Override
        public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
            // TODO Auto-generated method stub
            if (mViewPager.getCurrentItem() != arg0.getPosition()) {
                mViewPager.setCurrentItem(arg0.getPosition(), true);
            }

            ContactsPicker contactsPicker = (ContactsPicker)getFragmentAt(arg0.getPosition());
            Log.d("MMS", "TabListener   arg0.getPosition() = "+arg0.getPosition());
            if (contactsPicker != null) {
                contactsPicker.updateButton();
            }
            mLastManuallySelectedFragment = arg0.getPosition();
        }

    };

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            switch (position) {
                case TAB_INDEX_CONTACT:
                    return (mContactFragment = new ContactFragment());
                case TAB_INDEX_MSGRECENT:
                    return (mMsgRecentFragment = new MsgRecentFragment());
//                case TAB_INDEX_CALLLOG:
//                    return (mCalllogFragment = new CallLogListFragment(AddReceiptorTab.this));
                case TAB_INDEX_GROUP:
                    return (mGroupFragment = new GroupFragment());
                case TAB_INDEX_STAR:
                    return mStarFragment = new StarFragment();
            }
            throw new IllegalStateException("No fragment at position " + position);
        }

        public int getCount() {
            return TAB_INDEX_COUNT;
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        } else if (MmsApp.mLightTheme) {
            setTheme(R.style.GnLightTheme);
        } else if (MmsApp.mDarkTheme) {
            setTheme(R.style.GnDarkTheme);
        }
        //Gionee <zhouyj> <2013-04-22> modify for start CR00797045 start
        mCurrent = this;
        //Gionee <zhouyj> <2013-04-22> modify for start CR00797045 end
        super.onCreate(arg0);
        final Intent intent = getIntent();
        setContentView(R.layout.gn_addreceiptortab_frame);
        findViewById(R.id.addreceiptortab_frame).addOnLayoutChangeListener(mFirstLayoutListener);

        mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mPageChangeListener);

        mBottomBttnBar = (LinearLayout) findViewById(R.id.bottom_bar);
        // default mode
        mMode = MODE_ADD_CONTACT;
        Bundle bundle = getIntent().getExtras();

        // mode indicate that activity started for inserting contact info as
        // text message
        //gionee gaoj 2012-8-3 modified for CR00663537 start
        ArrayList<String> contactNumbers = null;
        if (bundle != null) {
            if (bundle.getBoolean("InsertContact")) {
                mMode = MODE_INSERT_CONTACT;
            }
            if (bundle.getBoolean("useoldcache")) {
                mUseOldCache = true;
            }
            if (bundle.getBoolean("attachcontacts")) {
                mIsAttachContacts = true;
            }
            contactNumbers = bundle.getStringArrayList("ContactNumbers");
        }
        //gionee gaoj 2012-8-3 modified for CR00663537 end

        if (mMode == MODE_INSERT_CONTACT) {
            setTitle(getResources().getString(R.string.insert_contacts));
        } else if (mMode == MODE_ADD_CONTACT) {
            setTitle(getResources().getString(R.string.title_add_receipents));
        }

        //gionee gaoj 2012-6-20 modified for CR00626418 start
        if (ContactsCacheSingleton.getInstance().isInited()) {
            if (mUseOldCache) {
                ContactsCacheSingleton.getInstance().setCheckedNumber(contactNumbers);
            } 
            //gionee gaoj 2012-6-20 modified for CR00626418 end
        } else {
            ContactsCacheSingleton.getInstance().init(contactNumbers, getApplicationContext());
            ContactsCacheSingleton.getInstance().starQueryNumbers();
        }

        ContactsCacheSingleton.getInstance().setOnQueryCompleteListener(
                new ContactsCacheSingleton.onQueryCompleteListener() {

                    @Override
                    public void onQueryComplete(
                            ArrayList<ContactListItemCache> result) {

                        //Gionee <Gaoj> <2013-05-07> delete for CR00802481 begin
                        /*addMsgRecentTab();
                        addContactTab();
                        addGroupTab();
                        addStarTab();
                        // Aurora liugj 2013-09-13 modified for aurora's new feature start
                        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                        getActionBar().setDisplayShowTitleEnabled(false);
                        getActionBar().setDisplayShowHomeEnabled(false);

                        getActionBar().getTabAt(0).setText(R.string.tab_recent_contact);
                        getActionBar().getTabAt(1).setText(R.string.tab_contact);
                        getActionBar().getTabAt(2).setText(R.string.tab_group_contact);
                        getActionBar().getTabAt(3).setText(R.string.tab_favorites);
                        // Aurora liugj 2013-09-13 modified for aurora's new feature end
                        setCurrentTab(intent);*/
                        //Gionee <Gaoj> <2013-05-07> delete for CR00802481 end
                        if (mMsgRecentFragment != null) {
                            mMsgRecentFragment.updateAdapter();
                        }
                        if (mContactFragment != null) {
                            mContactFragment.updateAdapter();
                        }
                        if (mStarFragment != null) {
                            mStarFragment.updateAdapter();
                        }
                    }
                });

        //Gionee <Gaoj> <2013-05-07> add for CR00802481 begin
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mActionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        
        addMsgRecentTab();
        addContactTab();
        addGroupTab();
        addStarTab();
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);

        mActionBar.getTabAt(0).setText(R.string.tab_recent_contact);
        mActionBar.getTabAt(1).setText(R.string.tab_contact);
        mActionBar.getTabAt(2).setText(R.string.tab_group_contact);
        mActionBar.getTabAt(3).setText(R.string.tab_favorites);
        setCurrentTab(intent);
        //Gionee <Gaoj> <2013-05-07> add for CR00802481 end
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        mLastManuallySelectedFragment = mPrefs.getInt(PREF_LAST_MANUALLY_SELECTED_TAB,
                PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT);
        if (mLastManuallySelectedFragment >= TAB_INDEX_COUNT) {
            // Stored value may have exceeded the number of current tabs. Reset it.
            mLastManuallySelectedFragment = PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT;
        }

        mLaunched = true;
        // gionee zhouyj 2012-07-17 add for CR00650522 start 
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // Aurora xuyong 2013-11-15 modified for S4 adapt start
        filter.addAction("android.intent.action.THEME_CHANGED"/*Intent.ACTION_THEME_CHANGED*/);
        // Aurora xuyong 2013-11-15 modified for S4 adapt end
        registerReceiver(mReceiver, filter);
        // gionee zhouyj 2012-07-17 add for CR00650522 end 
    }
    
    //gionee gaoj added for CR00725602 20121201 start
    private MenuItem mSelectAll;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
    //gionee gaoj added for CR00725602 20121201 end
    
    // gionee zhouyj 2012-07-17 add for CR00650522 start 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.gn_add_receiptor_menu, menu);
        mSelectAll = menu.findItem(R.id.gn_add_select_all);
        super.onCreateOptionsMenu(menu);
        return true;
    }
    // gionee zhouyj 2012-07-17 add for CR00650522 end 

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        final int position = mViewPager.getCurrentItem();
        final Fragment fragment = getFragmentAt(position);
        if (fragment != null) {
            fragment.onPrepareOptionsMenu(menu);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_add_select_all:
            final int position = mViewPager.getCurrentItem();
            final Fragment fragment = getFragmentAt(position);
            if (fragment != null) {
                fragment.onOptionsItemSelected(item);
            }
            break;
        case R.id.gn_add_select_done:
            showDialog(DIALOG_ADD);
            ArrayList<String> contactInfoList = ContactsCacheSingleton.getInstance()
                    .getCheckedNumbers();

            int size = contactInfoList.size();
            //gionee gaoj 2012-7-9 added for CR00626901 start
            final int recipientLimit = MmsConfig.getSmsRecipientLimit();
            if (!mIsAttachContacts && recipientLimit != Integer.MAX_VALUE && size > recipientLimit) {
                new AuroraAlertDialog.Builder(this)
                        .setTitle(R.string.pick_too_many_recipients)
                        .setMessage(getString(R.string.too_many_recipients, size, recipientLimit))
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
                if (dialog != null) {
                    removeDialog(DIALOG_ADD);
                }
            } else {
            //gionee gaoj 2012-7-9 added for CR00626901 end
                final Intent intent = new Intent();
                if (mMode == MODE_ADD_CONTACT) {
                    intent.putExtra("ContactNumbers", contactInfoList);
                } else if (mMode == MODE_INSERT_CONTACT) {
                    intent.putExtra("ContactInfo", contactInfoList);
                }
                if (getParent() == null) {
                    setResult(RESULT_OK, intent);
                } else {
                    getParent().setResult(RESULT_OK, intent);
                }
                finish();
            }
            break;
        case R.id.gn_add_cancel:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void addContactTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_contact);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    private void addMsgRecentTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_recent_contact);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    private void addCalllogTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.calllog);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    private void addGroupTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_group_contact);
        tab.setTabListener(mTabListener);
        mActionBar.addTab(tab);
    }

    private void addStarTab() {
        final Tab tab = mActionBar.newTab();
        tab.setContentDescription(R.string.tab_favorites);
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
        final int savedTabIndex = mLastManuallySelectedFragment;

        final int tabIndex;
        tabIndex = mLastManuallySelectedFragment;

        final int previousItemIndex = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(tabIndex, false /* smoothScroll */);
        if (previousItemIndex != tabIndex) {
            sendFragmentVisibilityChange(previousItemIndex, false);
        }
        mPageChangeListener.setCurrentPosition(tabIndex);
        sendFragmentVisibilityChange(tabIndex, true);

        // Restore to the previous manual selection
        mLastManuallySelectedFragment = savedTabIndex;
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        // TODO Auto-generated method stub
        super.onNewIntent(newIntent);
        setIntent(newIntent);
        setCurrentTab(newIntent);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    }
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
    /*private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
        public boolean onPostDraw() {
            //add below line for Log
            if(mLaunched) {
                mLaunched = false;
                mViewPager.post(new Runnable() {
                    public void run() {
                        mViewPager.setOffscreenPageLimit(2);
                        mViewPager.requestLayout();
                    }
                });
            }
            return true;
        }
    }*/
    // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        final int position = mViewPager.getCurrentItem();
        final Fragment fragment = getFragmentAt(position);
        if (fragment != null) {
            fragment.onResume();
            //Gionee <zhouyj> <2013-04-24> add for CR00801550 start
            sendFragmentVisibilityChange(position, true);
            //Gionee <zhouyj> <2013-04-24> add for CR00801550 end
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mPrefs.edit().putInt(PREF_LAST_MANUALLY_SELECTED_TAB, mLastManuallySelectedFragment).apply();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  start 
        //getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
        // Aurora xuyong 2013-10-17 deleted for platform adapt to s4  end
        //gionee gaoj 2012-5-14 added for CR00596441 start
        if (dialog != null) {
            removeDialog(DIALOG_ADD);
        }
        //gionee gaoj 2012-5-14 added for CR00596441 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mUseOldCache && mCurrent == this) {
            ContactsCacheSingleton.destoryInstance();
        }
        // gionee zhouyj 2012-07-17 add for CR00650522 start 
        unregisterReceiver(mReceiver);
        // gionee zhouyj 2012-07-17 add for CR00650522 end 
    }

    private final View.OnLayoutChangeListener mFirstLayoutListener
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            v.removeOnLayoutChangeListener(this); // Unregister self.
        }
    };

    public interface ContactsPicker {
        public void markAll();
        public void updateButton();
    }

    //gionee gaoj 2012-5-14 added for CR00596441 start
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id){
        case DIALOG_ADD: {
            if (dialog != null && dialog.getContext()!= this){
                removeDialog(DIALOG_ADD);
            }
            dialog = new AuroraProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.refreshing));
            return dialog;
        }
    }
    return null;
    }
    //gionee gaoj 2012-5-14 added for CR00596441 end

    // gionee zhouyj 2012-07-17 add for CR00650522 start 
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Aurora xuyong 2013-11-15 modified for S4 adapt start
            if(Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())
                    || "android.intent.action.THEME_CHANGED"/*Intent.ACTION_THEME_CHANGED*/.equals(intent.getAction())) {
            // Aurora xuyong 2013-11-15 modified for S4 adapt end
                AddReceiptorTab.this.finish();
            }
        }
    };
    // gionee zhouyj 2012-07-17 add for CR00650522 end 
}

