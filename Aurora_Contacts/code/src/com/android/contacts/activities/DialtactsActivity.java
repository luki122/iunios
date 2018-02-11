/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.contacts.activities;

import java.util.List;
import java.util.Locale;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.interactions.PhoneNumberInteraction;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.list.ContactListFilterController.ContactListFilterListener;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.list.PhoneFavoriteFragment;
import com.android.contacts.list.PhoneNumberPickerFragment;
import com.android.contacts.activities.TransactionSafeActivity;
import com.android.contacts.util.AccountFilterUtil;
import com.android.internal.telephony.ITelephony;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.Profiler;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager; // import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Intents.UI;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnCloseListener;
import aurora.widget.AuroraSearchView.OnQueryTextListener;

import com.mediatek.contacts.util.OperatorUtils; 
import com.mediatek.contacts.util.TelephonyUtils;
// The following lines are provided and maintained by Mediatek Inc.
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.CallLogUnavailableFragment;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import gionee.provider.GnContactsContract.ProviderStatus;
import gionee.provider.GnContactsContract;
import com.android.contacts.list.ProviderStatusLoader.ProviderStatusListener;
import com.android.contacts.list.ProviderStatusLoader;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsUnavailableFragment;
import com.android.contacts.list.OnContactsUnavailableActionListener;
import gionee.app.GnStatusBarManager;
import com.android.contacts.GNContactsUtils;

// The previous lines are provided and maintained by Mediatek Inc.
/**
 * The dialer activity that has one tab with the virtual 12key
 * dialer, a tab with recent calls in it, a tab with the contacts and
 * a tab with the favorite. This is the container and the tabs are
 * embedded using intents.
 * The dialer tab's title is 'phone', a more common name (see strings.xml).
 */
/*
 * Bug Fix by Mediatek Begin. Original Android's code: public class
 * DialtactsActivity extends TransactionSafeActivity { CR ID: ALPS00115673
 * Descriptions: add wait cursor
 */
public class DialtactsActivity extends TransactionSafeActivity implements ProviderStatusListener {
    /*
     * Bug Fix by Mediatek End.
     */
    private static final String TAG = "DialtactsActivity";

    /** Used to open Call Setting */
    private static final String PHONE_PACKAGE = "com.android.phone";
    //aurora change liguangyu 201311125 start
//    private static final String CALL_SETTINGS_CLASS_NAME = "com.android.phone.CallSettings";
    private static final String CALL_SETTINGS_CLASS_NAME = "com.android.phone.CallFeaturesSetting";
    //aurora change liguangyu 201311125 end
    
    /**
     * Copied from PhoneApp. See comments in Phone app for more detail.
     */
    public static final String EXTRA_CALL_ORIGIN = "com.android.phone.CALL_ORIGIN";

    public static final String CALL_ORIGIN_DIALTACTS = "com.android.contacts.activities.DialtactsActivity";

    /**
     * Just for backward compatibility. Should behave as same as
     * {@link Intent#ACTION_DIAL}.
     */
    private static final String ACTION_TOUCH_DIALER = "com.android.phone.action.TOUCH_DIALER";

    /** Used both by {@link ActionBar} and {@link ViewPagerAdapter} */
    private static final int TAB_INDEX_DIALER = 0;
    private static final int TAB_INDEX_CALL_LOG = 1;
    private static final int TAB_INDEX_FAVORITES = 2;

    private static final int TAB_INDEX_COUNT = 3;

    private SharedPreferences mPrefs;

    /** Last manually selected tab index */
    private static final String PREF_LAST_MANUALLY_SELECTED_TAB =
            "DialtactsActivity_last_manually_selected_tab";
    private static final int PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT = TAB_INDEX_DIALER;

    private static final int SUBACTIVITY_ACCOUNT_FILTER = 1;

    public static boolean isUr = false;	

    /**
     * Listener interface for Fragments accommodated in {@link ViewPager} enabling them to know
     * when it becomes visible or invisible inside the ViewPager.
     */
    public interface ViewPagerVisibilityListener {
        public void onVisibilityChanged(boolean visible);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
    	public DialpadFragment mDialpadFragment;
    	
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_INDEX_DIALER:
                    return (mDialpadFragment = new DialpadFragment());
                case TAB_INDEX_CALL_LOG:
                	return new CallLogFragment();                   
                case TAB_INDEX_FAVORITES:
                    return new PhoneFavoriteFragment();
            }
            throw new IllegalStateException("No fragment at position " + position);
        }

        @Override
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

        @Override
        public void onPageScrolled(
                int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            final ActionBar actionBar = getActionBar();
            if (mCurrentPosition == position) {
                Log.w(TAG, "Previous position and next position became same (" + position + ")");
            }

            actionBar.selectTab(actionBar.getTabAt(position));
            mNextPosition = position;
        }

        public void setCurrentPosition(int position) {
            mCurrentPosition = position;
        }

        @Override
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
                    // Gionee zhangxx 2012-05-24 modify for CR00608225 begin
                    if (ContactsApplication.sIsGnContactsSupport && 0 == mCurrentPosition && mDialpadFragment != null) {
                        mDialpadFragment.queryLastOutgoingCall();
                    }
                    // Gionee zhangxx 2012-05-24 modify for CR00608225 end

                    break;
                }
                case ViewPager.SCROLL_STATE_DRAGGING:
                case ViewPager.SCROLL_STATE_SETTLING:
                default:
                    break;
            }
        }
    }

    private String mFilterText;

    /** Enables horizontal swipe between Fragments. */
    private ViewPager mViewPager;
    private final PageChangeListener mPageChangeListener = new PageChangeListener();
    private DialpadFragment mDialpadFragment;
    private CallLogFragment mCallLogFragment;
    private PhoneFavoriteFragment mPhoneFavoriteFragment;

    private final ContactListFilterListener mContactListFilterListener =
            new ContactListFilterListener() {
        @Override
        public void onContactListFilterChanged() {
            boolean doInvalidateOptionsMenu = false;

            if (mPhoneFavoriteFragment != null && mPhoneFavoriteFragment.isAdded()) {
                mPhoneFavoriteFragment.setFilter(mContactListFilterController.getFilter());
                doInvalidateOptionsMenu = true;
            }

            if (mSearchFragment != null && mSearchFragment.isAdded()) {
                mSearchFragment.setFilter(mContactListFilterController.getFilter());
                doInvalidateOptionsMenu = true;
            } else {
                Log.w(TAG, "Search Fragment isn't available when ContactListFilter is changed");
            }

            if (doInvalidateOptionsMenu) {
                invalidateOptionsMenu();
            }
        }
    };

    private final TabListener mTabListener = new TabListener() {
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            Log.i(TAG, "listener tab mTabListener");
            showCallLogUnavailableFragmentIfNeed();
            /*
             * Bug Fix by Mediatek End.
             */
            if (mViewPager.getCurrentItem() != tab.getPosition()) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }

            // During the call, we don't remember the tab position.
            if (!DialpadFragment.phoneIsInUse()) {
                // Remember this tab index. This function is also called, if the tab is set
                // automatically in which case the setter (setCurrentTab) has to set this to its old
                // value afterwards
                mLastManuallySelectedFragment = tab.getPosition();
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    };

    /**
     * Fragment for searching phone numbers. Unlike the other Fragments, this doesn't correspond
     * to tab but is shown by a search action.
     */
    private PhoneNumberPickerFragment mSearchFragment;
    /**
     * True when this Activity is in its search UI (with a {@link SearchView} and
     * {@link PhoneNumberPickerFragment}).
     */
    private boolean mInSearchUi;
    private AuroraSearchView mSearchView;
    // gionee xuhz 20120529 add for remove home icon from ActionBar start
    private ImageView mGnAciontUpView;
    // gionee xuhz 20120529 add for remove home icon from ActionBar end

    private final OnClickListener mFilterOptionClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            final PopupMenu popupMenu = new PopupMenu(DialtactsActivity.this, view);
            final Menu menu = popupMenu.getMenu();
            popupMenu.inflate(R.menu.dialtacts_search_options);
            final MenuItem filterOptionMenuItem = menu.findItem(R.id.filter_option);
            filterOptionMenuItem.setOnMenuItemClickListener(mFilterOptionsMenuItemClickListener);
            final MenuItem addContactOptionMenuItem = menu.findItem(R.id.add_contact);
            addContactOptionMenuItem.setIntent(
                    new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI));
            popupMenu.show();
        }
    };

    /**
     * The index of the Fragment (or, the tab) that has last been manually selected.
     * This value does not keep track of programmatically set Tabs (e.g. Call Log after a Call)
     */
    private int mLastManuallySelectedFragment;

    private ContactListFilterController mContactListFilterController;
    private OnMenuItemClickListener mFilterOptionsMenuItemClickListener =
            new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AccountFilterUtil.startAccountFilterActivityForResult(
                    DialtactsActivity.this, SUBACTIVITY_ACCOUNT_FILTER);
            return true;
        }
    };

    private OnMenuItemClickListener mSearchMenuItemClickListener =
            new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            enterSearchUi();
            return true;
        }
    };

    /**
     * Listener used when one of phone numbers in search UI is selected. This will initiate a
     * phone call using the phone number.
     */
    private final OnPhoneNumberPickerActionListener mPhoneNumberPickerActionListener =
            new OnPhoneNumberPickerActionListener() {
                @Override
                public void onPickPhoneNumberAction(Uri dataUri) {
                    // Specify call-origin so that users will see the previous tab instead of
                    // CallLog screen (search UI will be automatically exited).
                    PhoneNumberInteraction.startInteractionForPhoneCall(
                            DialtactsActivity.this, dataUri,
                            CALL_ORIGIN_DIALTACTS);
                }

                @Override
                public void onShortcutIntentCreated(Intent intent) {
                    Log.w(TAG, "Unsupported intent has come (" + intent + "). Ignoring.");
                }

                @Override
                public void onHomeInActionBarSelected() {
                    exitSearchUi();
                }
    };

    /**
     * Listener used to send search queries to the phone search fragment.
     */
    private final OnQueryTextListener mPhoneSearchQueryTextListener =
            new OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        hideInputMethod(view);
                        view.clearFocus();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Show search result with non-empty text. Show a bare list otherwise.
                    if (mSearchFragment != null) {
                        mSearchFragment.setQueryString(newText, true);
                    }
                    return true;
                }
    };

    /**
     * Listener used to handle the "close" button on the right side of {@link SearchView}.
     * If some text is in the search view, this will clean it up. Otherwise this will exit
     * the search UI and let users go back to usual Phone UI.
     *
     * This does _not_ handle back button.
     */
    private final OnCloseListener mPhoneSearchCloseListener =
            new OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                        mSearchView.setQuery(null, true);
                    }
                    return true;
                }
    };

    private final View.OnLayoutChangeListener mFirstLayoutListener
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            v.removeOnLayoutChangeListener(this); // Unregister self.
            addSearchFragment();
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        // gionee xuhz add for support gn theme start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_DialtactsTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_DialtactsTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_DialtactsTheme_light);
        }
        // gionee xuhz add for support gn theme start
        super.onCreate(icicle);
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	gnOnCreate(icicle);
        	return;
        }
        
        log("onCreate");
        Profiler.trace(Profiler.DialtactsActivityEnterOnCreate);

        final Intent intent = getIntent();
        fixIntent(intent);

        HyphonManager.getInstance().setCountryIso(ContactsUtils.getCurrentCountryIso());

        setContentView(R.layout.dialtacts_activity);

        mContactListFilterController = ContactListFilterController.getInstance(this);
        mContactListFilterController.addListener(mContactListFilterListener);

        findViewById(R.id.dialtacts_frame).addOnLayoutChangeListener(mFirstLayoutListener);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
        mViewPager.setOnPageChangeListener(mPageChangeListener);

        // Setup the ActionBar tabs (the order matches the tab-index contants TAB_INDEX_*)
        setupDialer();
        setupCallLog();
        setupFavorites();
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);

        // Load the last manually loaded tab
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        mLastManuallySelectedFragment = mPrefs.getInt(PREF_LAST_MANUALLY_SELECTED_TAB,
                PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT);
        if (mLastManuallySelectedFragment >= TAB_INDEX_COUNT) {
            // Stored value may have exceeded the number of current tabs. Reset it.
            mLastManuallySelectedFragment = PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT;
        }

        setCurrentTab(intent);

        if (UI.FILTER_CONTACTS_ACTION.equals(intent.getAction())
                && icicle == null) {
            setupFilterText(intent);
        }

        /**
         * add by mediatek .inc
         * description register the sim indicator changed broadcast receiver
         */
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            this.registerReceiver(mReceiver, intentFilter);
        }

        IntentFilter phbLoadIntentFilter =
            new IntentFilter((AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED));
        this.registerReceiver(mReceiver, phbLoadIntentFilter);

        mLaunched = true;
        /**
         * add by mediatek .inc end
         */
        Profiler.trace(Profiler.DialtactsActivityLeaveOnCreate);
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * add by mediatek .inc
         * description : show the sim indicator when 
         * Activity onResume
         */
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            setSimIndicatorVisibility(true);
            mShowSimIndicator = true;
        }

        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mProviderStatusLoader.setProviderStatusListener(this);

        showCallLogUnavailableFragmentIfNeed();
        /*
         * Bug Fix by Mediatek End.
         */
        if (Locale.getDefault().getLanguage().toLowerCase().equals("ur")){
            isUr=true;
        }else {
            isUr=false;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPhoneFavoriteFragment != null) {
            mPhoneFavoriteFragment.setFilter(mContactListFilterController.getFilter());
        }
        if (mSearchFragment != null) {
            mSearchFragment.setFilter(mContactListFilterController.getFilter());
        }

//        getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
    }

    public void onStop() {
        super.onStop();
//        getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();        
        mContactListFilterController.removeListener(mContactListFilterListener);

        /**
         * add by mediatek .inc
         * description : unregister the sim indicator changed broadcast receiver
         */
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            unregisterReceiver(mReceiver);
        }
        /**
         * add by mediatek .inc end
         */
    }

    /**
     * Add search fragment.  Note this is called during onLayout, so there's some restrictions,
     * such as executePendingTransaction can't be used in it.
     */
    private void addSearchFragment() {
        // In order to take full advantage of "fragment deferred start", we need to create the
        // search fragment after all other fragments are created.
        // The other fragments are created by the ViewPager on the first onMeasure().
        // We use the first onLayout call, which is after onMeasure().

        // Just return if the fragment is already created, which happens after configuration
        // changes.
        if (mSearchFragment != null) return;

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment searchFragment = new PhoneNumberPickerFragment();

        searchFragment.setUserVisibleHint(false);
        ft.add(R.id.dialtacts_frame, searchFragment);
        ft.hide(searchFragment);
        ft.commitAllowingStateLoss();
    }

    private void prepareSearchView() {
        final View searchViewLayout =
                getLayoutInflater().inflate(R.layout.dialtacts_custom_action_bar, null);
        mSearchView = (AuroraSearchView) searchViewLayout.findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(mPhoneSearchQueryTextListener);
        mSearchView.setOnCloseListener(mPhoneSearchCloseListener);
        // Since we're using a custom layout for showing SearchView instead of letting the
        // search menu icon do that job, we need to manually configure the View so it looks
        // "shown via search menu".
        // - it should be iconified by default
        // - it should not be iconified at this time
        // See also comments for onActionViewExpanded()/onActionViewCollapsed()
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setQueryHint(getString(R.string.hint_findContacts));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showInputMethod(view.findFocus());
                }
            }
        });

        // gionee xuhz 20120529 add for remove home icon from ActionBar start
        if (ContactsApplication.sIsGnContactsSupport) {
            mGnAciontUpView = (ImageView)searchViewLayout.findViewById(R.id.gn_action_up);
            mGnAciontUpView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitSearchUi();
                }
            });
        }
        // gionee xuhz 20120529 add for remove home icon from ActionBar end
        
        if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
            // Filter option menu should be shown on the right side of SearchView.
            final View filterOptionView = searchViewLayout.findViewById(R.id.search_option);
            
            if (ContactsApplication.sIsGnContactsSupport) {
            	filterOptionView.setVisibility(View.GONE);
            } else {
            	filterOptionView.setVisibility(View.VISIBLE);
            }
            
            filterOptionView.setOnClickListener(mFilterOptionClickListener);
        }

        getActionBar().setCustomView(searchViewLayout,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        // This method can be called before onCreate(), at which point we cannot rely on ViewPager.
        // In that case, we will setup the "current position" soon after the ViewPager is ready.
        final int currentPosition = mViewPager != null ? mViewPager.getCurrentItem() : -1;

        if (fragment instanceof DialpadFragment) {
            mDialpadFragment = (DialpadFragment) fragment;
            mDialpadFragment.setListener(mDialpadListener);
            if (currentPosition == TAB_INDEX_DIALER) {
                mDialpadFragment.onVisibilityChanged(true);
            }
        } else if (fragment instanceof CallLogFragment) {
            mCallLogFragment = (CallLogFragment) fragment;
            if (currentPosition == TAB_INDEX_CALL_LOG) {
                mCallLogFragment.onVisibilityChanged(true);
            }
        } else if (fragment instanceof PhoneFavoriteFragment) {
            mPhoneFavoriteFragment = (PhoneFavoriteFragment) fragment;
            mPhoneFavoriteFragment.setListener(mPhoneFavoriteListener);
            if (mContactListFilterController != null
                    && mContactListFilterController.getFilter() != null) {
                mPhoneFavoriteFragment.setFilter(mContactListFilterController.getFilter());
            }
        } else if (fragment instanceof PhoneNumberPickerFragment) {
            mSearchFragment = (PhoneNumberPickerFragment) fragment;
            mSearchFragment.setOnPhoneNumberPickerActionListener(mPhoneNumberPickerActionListener);
            mSearchFragment.setQuickContactEnabled(true);
            // gionee xuhz 20120514 modify start
            if (ContactsApplication.sIsGnContactsSupport) {
                mSearchFragment.setDarkTheme(false);
            } else {
                mSearchFragment.setDarkTheme(true);
            }
            // gionee xuhz 20120514 modify start
            mSearchFragment.setPhotoPosition(ContactListItemView.PhotoPosition.LEFT);
            if (mContactListFilterController != null
                    && mContactListFilterController.getFilter() != null) {
                mSearchFragment.setFilter(mContactListFilterController.getFilter());
            }
            // Here we assume that we're not on the search mode, so let's hide the fragment.
            //
            // We get here either when the fragment is created (normal case), or after configuration
            // changes.  In the former case, we're not in search mode because we can only
            // enter search mode if the fragment is created.  (see enterSearchUi())
            // In the latter case we're not in search mode either because we don't retain
            // mInSearchUi -- ideally we should but at this point it's not supported.
            mSearchFragment.setUserVisibleHint(false);
            // After configuration changes fragments will forget their "hidden" state, so make
            // sure to hide it.
            if (!mSearchFragment.isHidden()) {
                final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.hide(mSearchFragment);
                transaction.commitAllowingStateLoss();
            }
        }

        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        else if (fragment instanceof CallLogUnavailableFragment) {
            mcallLogUnavailableFragment = (CallLogUnavailableFragment) fragment;
            mcallLogUnavailableFragment.setProviderStatusLoader(mProviderStatusLoader);
            mcallLogUnavailableFragment
                    .setOnCallLogUnavailableActionListener(new CallLogUnavailableFragmentListener());
        }
        /*
         * Bug Fix by Mediatek End.
         */

    }

    @Override
    protected void onPause() {
        super.onPause();
        Profiler.trace(Profiler.DialtactsActivityEnterOnPause);

       // The following lines are provided and maintained by Mediatek Inc.
       //MTK81281 add null pointer protect for Cr:ALPS00119664 start
       if(mCallLogFragment != null){          	   	
            if (mCallLogFragment.mSelectResDialog != null ){
                if(mCallLogFragment.mSelectResDialog.isShowing()) {
                    mCallLogFragment.mSelectResDialog.dismiss();
                    mCallLogFragment.mSelectResDialog = null;
                }
            }
        }
        //MTK81281 add null pointer protect for Cr:ALPS00119664 end
        // The previous lines are provided and maintained by Mediatek Inc.
        
        mPrefs.edit().putInt(PREF_LAST_MANUALLY_SELECTED_TAB, mLastManuallySelectedFragment)
                .apply();

        /**
         * add by mediatek .inc
         * description : hide the sim indicator when
         * activity onPause
         */
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            setSimIndicatorVisibility(false);
            mShowSimIndicator = false;
        }
        Profiler.trace(Profiler.DialtactsActivityLeaveOnPause);

        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mProviderStatusLoader.setProviderStatusListener(null);
        /*
         * Bug Fix by Mediatek End.
         */

    }

    private void fixIntent(Intent intent) {
        // This should be cleaned up: the call key used to send an Intent
        // that just said to go to the recent calls list.  It now sends this
        // abstract action, but this class hasn't been rewritten to deal with it.
        if (Intent.ACTION_CALL_BUTTON.equals(intent.getAction())) {
            intent.setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE);
            intent.putExtra("call_key", true);
            setIntent(intent);
        }
    }

    private void setupDialer() {
        final Tab tab = getActionBar().newTab();
        tab.setContentDescription(R.string.dialerIconLabel);
        tab.setTabListener(mTabListener);
        tab.setIcon(R.drawable.ic_tab_dialer);
        getActionBar().addTab(tab);
    }

    private void setupCallLog() {
        final Tab tab = getActionBar().newTab();
        tab.setContentDescription(R.string.recentCallsIconLabel);
        tab.setIcon(R.drawable.ic_tab_recent);
        tab.setTabListener(mTabListener);
        getActionBar().addTab(tab);
    }

    private void setupFavorites() {
        final Tab tab = getActionBar().newTab();
        tab.setContentDescription(R.string.contactsFavoritesLabel);
        tab.setIcon(R.drawable.ic_tab_all);
        tab.setTabListener(mTabListener);
        getActionBar().addTab(tab);
    }

    /**
     * Returns true if the intent is due to hitting the green send key while in a call.
     *
     * @param intent the intent that launched this activity
     * @param recentCallsRequest true if the intent is requesting to view recent calls
     * @return true if the intent is due to hitting the green send key while in a call
     */
    private boolean isSendKeyWhileInCall(final Intent intent, boolean recentCallsRequest) {
        return TelephonyUtils.isSendKeyWhileInCall(this, intent, recentCallsRequest);
    }

    /**
     * Sets the current tab based on the intent's request type
     *
     * @param intent Intent that contains information about which tab should be selected
     */
    private void setCurrentTab(Intent intent) {
        // If we got here by hitting send and we're in call forward along to the in-call activity
    	final boolean recentCallsRequest = Calls.CONTENT_TYPE.equals(intent.getType());
        if (isSendKeyWhileInCall(intent, recentCallsRequest)) {
            finish();
            return;
        }

        // Remember the old manually selected tab index so that it can be restored if it is
        // overwritten by one of the programmatic tab selections
        final int savedTabIndex = mLastManuallySelectedFragment;

        
        // gionee xuhz 20120618 add for CR00624070 star
        int missedCallCount;
        if (ContactsApplication.sIsGnContactsSupport) {
            missedCallCount = ContactsUtils.gnGetMissedCallCount(getContentResolver());
        } else {
            missedCallCount = 0;
        }
        // gionee xuhz 20120618 add for CR00624070 end
        
        final int tabIndex;
        if (DialpadFragment.phoneIsInUse() || isDialIntent(intent)) {
            tabIndex = TAB_INDEX_DIALER;
        // gionee xuhz 20120618 modify for CR00624070 star
        } else if (recentCallsRequest || missedCallCount > 0) {
        // gionee xuhz 20120618 modify for CR00624070 end
            tabIndex = TAB_INDEX_CALL_LOG;
        } else {
            tabIndex = mLastManuallySelectedFragment;
        }

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
    public void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        fixIntent(newIntent);
        setCurrentTab(newIntent);
        final String action = newIntent.getAction();
        if (UI.FILTER_CONTACTS_ACTION.equals(action)) {
            setupFilterText(newIntent);
        }
        if (mInSearchUi || (mSearchFragment != null && mSearchFragment.isVisible())) {
            exitSearchUi();
        }

        if (mViewPager.getCurrentItem() == TAB_INDEX_DIALER) {
            if (mDialpadFragment != null) {
                mDialpadFragment.configureScreenFromIntent(newIntent);
            } else {
                Log.e(TAG, "DialpadFragment isn't ready yet when the tab is already selected.");
            }
        }
        
        invalidateOptionsMenu();
    }

    /** Returns true if the given intent contains a phone number to populate the dialer with */
    private boolean isDialIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || ACTION_TOUCH_DIALER.equals(action)) {
            return true;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            final Uri data = intent.getData();
            if (data != null && "tel".equals(data.getScheme())) {
                return true;
            }
        }
        if("OP01".equals(OperatorUtils.getOptrProperties())){		
            String componentName = intent.getComponent().getClassName();	
                if("com.android.contacts.VideoCallEntryActivity".equals(componentName)){
                    return true;		
            }
        }		
        return false;
    }

    /**
     * Retrieves the filter text stored in {@link #setupFilterText(Intent)}.
     * This text originally came from a FILTER_CONTACTS_ACTION intent received
     * by this activity. The stored text will then be cleared after after this
     * method returns.
     *
     * @return The stored filter text
     */
    public String getAndClearFilterText() {
        String filterText = mFilterText;
        mFilterText = null;
        return filterText;
    }

    /**
     * Stores the filter text associated with a FILTER_CONTACTS_ACTION intent.
     * This is so child activities can check if they are supposed to display a filter.
     *
     * @param intent The intent received in {@link #onNewIntent(Intent)}
     */
    private void setupFilterText(Intent intent) {
        // If the intent was relaunched from history, don't apply the filter text.
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
        String filter = intent.getStringExtra(UI.FILTER_TEXT_EXTRA_KEY);
        if (filter != null && filter.length() > 0) {
            mFilterText = filter;
        }
    }

    @Override
    public void onBackPressed() {
        Profiler.trace(Profiler.DialtactsActivityOnBackPressed);
        if (mInSearchUi) {
            // We should let the user go back to usual screens with tabs.
            exitSearchUi();
        } else if (isTaskRoot()) {
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            moveTaskToBack(false);
        } else {
            super.onBackPressed();
        }
    }

    private DialpadFragment.Listener mDialpadListener = new DialpadFragment.Listener() {
        @Override
        public void onSearchButtonPressed() {
            enterSearchUi();
        }
    };

    private PhoneFavoriteFragment.Listener mPhoneFavoriteListener =
            new PhoneFavoriteFragment.Listener() {
        @Override
        public void onContactSelected(Uri contactUri) {
            PhoneNumberInteraction.startInteractionForPhoneCall(
                    DialtactsActivity.this, contactUri,
                    CALL_ORIGIN_DIALTACTS);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (ContactsApplication.sIsGnContactsSupport) {
        	inflater.inflate(R.menu.gn_dialtacts_options, menu);
        } else {
        	inflater.inflate(R.menu.dialtacts_options, menu);
        }

        super.onCreateOptionsMenu(menu);
        
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnOnPrepareOptionsMenu(menu);
    		super.onPrepareOptionsMenu(menu);

    		return true;
    	}
        final MenuItem searchMenuItem = menu.findItem(R.id.search_on_action_bar);
        final MenuItem filterOptionMenuItem = menu.findItem(R.id.filter_option);
        final MenuItem addContactOptionMenuItem = menu.findItem(R.id.add_contact);
        final MenuItem callSettingsMenuItem = menu.findItem(R.id.menu_call_settings);
        //The following lines are provided and maintained by Mediatek Inc.
        final MenuItem chooseResoucesMenuItem = menu.findItem(R.id.choose_resources);
        //The previous lines are provided and maintained by Mediatek Inc.
        Tab tab = getActionBar().getSelectedTab();
        if (mInSearchUi) {
            searchMenuItem.setVisible(false);
            //The following lines are provided and maintained by Mediatek Inc.
            chooseResoucesMenuItem.setVisible(false);
            //The previous lines are provided and maintained by Mediatek Inc.
            if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
                filterOptionMenuItem.setVisible(true);
                filterOptionMenuItem.setOnMenuItemClickListener(
                        mFilterOptionsMenuItemClickListener);
                addContactOptionMenuItem.setVisible(true);
                addContactOptionMenuItem.setIntent(
                        new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI));
            } else {
                // Filter option menu should be not be shown as a overflow menu.
                filterOptionMenuItem.setVisible(false);
                addContactOptionMenuItem.setVisible(false);
            }
            callSettingsMenuItem.setVisible(false);
        } else {
            final boolean showCallSettingsMenu;
            if (tab != null && tab.getPosition() == TAB_INDEX_DIALER) {
                searchMenuItem.setVisible(false);
                //The following lines are provided and maintained by Mediatek Inc.
                chooseResoucesMenuItem.setVisible(false);
                //The previous lines are provided and maintained by Mediatek Inc.
                // When permanent menu key is _not_ available, the call settings menu should be
                // available via DialpadFragment.
                showCallSettingsMenu = ViewConfiguration.get(this).hasPermanentMenuKey();
            } else {
                searchMenuItem.setVisible(true);
                searchMenuItem.setOnMenuItemClickListener(mSearchMenuItemClickListener);
                showCallSettingsMenu = true;
                //The following lines are provided and maintained by Mediatek Inc.
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					chooseResoucesMenuItem.setVisible(true);
					chooseResoucesMenuItem
							.setOnMenuItemClickListener(new OnMenuItemClickListener() {
								public boolean onMenuItemClick(MenuItem item) {
									mCallLogFragment.showChoiceResourceDialog();
									return true;
								}
							});
				} else {
					chooseResoucesMenuItem.setVisible(false);
				}
                //The previous lines are provided and maintained by Mediatek Inc.
            }
            if (tab != null && tab.getPosition() == TAB_INDEX_FAVORITES) {
                //The following lines are provided and maintained by Mediatek Inc.
                chooseResoucesMenuItem.setVisible(false);
                //The previous lines are provided and maintained by Mediatek Inc.
                filterOptionMenuItem.setVisible(true);
                filterOptionMenuItem.setOnMenuItemClickListener(
                        mFilterOptionsMenuItemClickListener);
                addContactOptionMenuItem.setVisible(true);
                addContactOptionMenuItem.setIntent(
                        new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI));
            } else {
                filterOptionMenuItem.setVisible(false);
                addContactOptionMenuItem.setVisible(false);
            }

            if (showCallSettingsMenu) {
                callSettingsMenuItem.setVisible(true);
                callSettingsMenuItem.setIntent(DialtactsActivity.getCallSettingsIntent());
            } else {
                callSettingsMenuItem.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {
        if (mSearchFragment != null && mSearchFragment.isAdded() && !globalSearch) {
            if (mInSearchUi) {
                if (mSearchView.hasFocus()) {
                    showInputMethod(mSearchView.findFocus());
                } else {
                    mSearchView.requestFocus();
                }
            } else {
                enterSearchUi();
            }
        } else {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        }
    }

    /**
     * Hides every tab and shows search UI for phone lookup.
     */
    private void enterSearchUi() {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnEnterSearchUi();
    		return;
    	}
        if (mSearchFragment == null) {
            // We add the search fragment dynamically in the first onLayoutChange() and
            // mSearchFragment is set sometime later when the fragment transaction is actually
            // executed, which means there's a window when users are able to hit the (physical)
            // search key but mSearchFragment is still null.
            // It's quite hard to handle this case right, so let's just ignore the search key
            // in this case.  Users can just hit it again and it will work this time.
            return;
        }
        if (mSearchView == null) {
            prepareSearchView();
        }

        final ActionBar actionBar = getActionBar();

        final Tab tab = actionBar.getSelectedTab();

        // User can search during the call, but we don't want to remember the status.
        if (tab != null && !DialpadFragment.phoneIsInUse()) {
            mLastManuallySelectedFragment = tab.getPosition();
        }

        mSearchView.setQuery(null, true);

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        sendFragmentVisibilityChange(mViewPager.getCurrentItem(), false);

        // Show the search fragment and hide everything else.
        mSearchFragment.setUserVisibleHint(true);
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.show(mSearchFragment);
        transaction.commitAllowingStateLoss();
        mViewPager.setVisibility(View.GONE);

        // We need to call this and onActionViewCollapsed() manually, since we are using a custom
        // layout instead of asking the search menu item to take care of SearchView.
        mSearchView.onActionViewExpanded();
        mInSearchUi = true;
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(view, 0)) {
                Log.w(TAG, "Failed to show soft input method.");
            }
        }
    }

    private void hideInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Goes back to usual Phone UI with tags. Previously selected Tag and associated Fragment
     * should be automatically focused again.
     */
    private void exitSearchUi() {
        final ActionBar actionBar = getActionBar();

        // Hide the search fragment, if exists.
        if (mSearchFragment != null) {
            mSearchFragment.setUserVisibleHint(false);

            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.hide(mSearchFragment);
            transaction.commitAllowingStateLoss();
        }

        // We want to hide SearchView and show Tabs. Also focus on previously selected one.
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        sendFragmentVisibilityChange(mViewPager.getCurrentItem(), true);

        mViewPager.setVisibility(View.VISIBLE);

        hideInputMethod(getCurrentFocus());

        // Request to update option menu.
        invalidateOptionsMenu();

        // See comments in onActionViewExpanded()
        mSearchView.onActionViewCollapsed();
        mInSearchUi = false;
    }

    private Fragment getFragmentAt(int position) {
        switch (position) {
            case TAB_INDEX_DIALER:
                return mDialpadFragment;
            case TAB_INDEX_CALL_LOG:
                return mCallLogFragment;
            case TAB_INDEX_FAVORITES:
                return mPhoneFavoriteFragment;
            default:
                throw new IllegalStateException("Unknown fragment index: " + position);
        }
    }

    private void sendFragmentVisibilityChange(int position, boolean visibility) {
        final Fragment fragment = getFragmentAt(position);
        if (fragment instanceof ViewPagerVisibilityListener) {
            ((ViewPagerVisibilityListener) fragment).onVisibilityChanged(visibility);
        }
    }

    /** Returns an Intent to launch Call Settings screen */
    public static Intent getCallSettingsIntent() {
//        final Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.setClassName(PHONE_PACKAGE, CALL_SETTINGS_CLASS_NAME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        return intent;
        return GNContactsUtils.getCallSettingsIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case SUBACTIVITY_ACCOUNT_FILTER: {
                AccountFilterUtil.handleAccountFilterResult(
                        mContactListFilterController, resultCode, data);
            }
            break;
        }
    }

    /* below ared added by mediatek .inc */
    private BroadcastReceiver mReceiver = new DialtactsBroadcastReceiver();
//    private PostDrawListener mPostDrawListener = new PostDrawListener();
    private StatusBarManager mStatusBarMgr;

    private boolean mShowSimIndicator = false;
    private boolean mLaunched = false;

//    private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
//        public boolean onPostDraw() {
//            //add below line for Xlog
//        	//Aurora liumx 20130828 modify for remove Mediatek Xlog begin
//            Log.i("Xlog:[AppLaunch]", "[AppLaunch] twelvekeydialer onPostDraw");
//            //Aurora liumx 20130828 modify for remove Mediatek Xlog end
//            if(mLaunched) {
//                mLaunched = false;
//                mViewPager.post(new Runnable() {
//                    public void run() {
//                        Profiler.trace(Profiler.DialtactsActivitySetOffscreenPageLimit);
//                        mViewPager.setOffscreenPageLimit(2);
//                        mViewPager.requestLayout();
//                    }
//                });
//            }
//            return true;
//        }
//    }

    private class DialtactsBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("DialtactsBroadcastReceiver, onReceive action = " + action);

            if(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED.equals(action)) {
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    if (mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    }
                }
            }
			
            if (action.equals(AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED)) {
                if(mDialpadFragment != null)
                    mDialpadFragment.updateDialerSearch();
            }
        }
    }

    void setSimIndicatorVisibility(boolean visible) {
        if(mStatusBarMgr == null)
            mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);

        if (visible)
           GnStatusBarManager.showSIMIndicator(mStatusBarMgr, getComponentName(), ContactsFeatureConstants.VOICE_CALL_SIM_SETTING);
        else
           GnStatusBarManager.hideSIMIndicator(mStatusBarMgr, getComponentName());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if(mViewPager.getCurrentItem() == TAB_INDEX_DIALER) {
            handled = mDialpadFragment.onKeyDown(keyCode, event);
        }

        if(handled)
            return handled;

        return super.onKeyDown(keyCode, event);
    }
    
    public int getCurrentFragmentId() {
        return mViewPager.getCurrentItem();
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    // The following lines are provided and maintained by Mediatek Inc.

    private CallLogUnavailableFragment mcallLogUnavailableFragment;

    private ProviderStatusLoader mProviderStatusLoader;

    public DialtactsActivity() {
        mProviderStatusLoader = new ProviderStatusLoader(this);
    }

    private class CallLogUnavailableFragmentListener implements OnContactsUnavailableActionListener {

        public void onAddAccountAction() {
            // TODO Auto-generated method stub

        }

        public void onCreateNewContactAction() {
            // TODO Auto-generated method stub

        }

        public void onFreeInternalStorageAction() {
            // TODO Auto-generated method stub

        }

        public void onImportContactsFromFileAction() {
            // TODO Auto-generated method stub

        }
    }

    private void showCallLogUnavailableFragmentIfNeed() {
        View callLogUnavailableView = findViewById(R.id.call_log_unavailable_view);

        Tab tab = getActionBar().getSelectedTab();
        int providerStatus = mProviderStatusLoader.getProviderStatus();
        Log.i(TAG,"showCallLogUnavailableFragmentIfNeed providerStatus : "+providerStatus);
        if (providerStatus == ProviderStatus.STATUS_NORMAL) {
            callLogUnavailableView.setVisibility(View.GONE);

        } else if (tab != null && tab.getPosition() != TAB_INDEX_DIALER && providerStatus == ProviderStatus.STATUS_CHANGING_LOCALE) {
            if (mcallLogUnavailableFragment == null) {
                Log.i(TAG, "mContactsUnavailableFragment == null");
                mcallLogUnavailableFragment = new CallLogUnavailableFragment();
                mcallLogUnavailableFragment.setProviderStatusLoader(mProviderStatusLoader);
                mcallLogUnavailableFragment
                        .setOnCallLogUnavailableActionListener(new CallLogUnavailableFragmentListener());
                getFragmentManager().beginTransaction().replace(
                        R.id.call_log_unavailable_container, mcallLogUnavailableFragment)
                        .commitAllowingStateLoss();
            } else {
                Log.i(TAG, "mContactsUnavailableFragment != null");
                mcallLogUnavailableFragment.update();
            }
            boolean mDestroyed = mcallLogUnavailableFragment.mDestroyed;
            Log.i(TAG,"  mDestroyed11 : "+mDestroyed);
            Log.i(TAG,"mProviderStatusLoader.getProviderStatus()11 : "+mProviderStatusLoader.getProviderStatus());
            if(mDestroyed ){
                callLogUnavailableView.setVisibility(View.GONE);
                mcallLogUnavailableFragment.mDestroyed = false;
            }else {
                Log.i(TAG,"  mDestroyed11 is false: ");
                callLogUnavailableView.setVisibility(View.VISIBLE);
            }
            Log.i(TAG, "tab.getPosition() : " + tab.getPosition());
            
        } else {
            Log.i(TAG,"callLogUnavailableView.setVisibility(View.GONE);");
            callLogUnavailableView.setVisibility(View.GONE);
        }

    }


    @Override
    public void onProviderStatusChange() {
        Log.i(TAG, "call onProviderStatusChange");
        showCallLogUnavailableFragmentIfNeed();
    }

    // The previous lines are provided and maintained by Mediatek Inc.
    
	private ViewPagerAdapter mViewPagerAdapter;
	private boolean mIsPressing = false;
    
    protected void gnOnCreate(Bundle icicle) {
        log("onCreate");
        Profiler.trace(Profiler.DialtactsActivityEnterOnCreate);

        final Intent intent = getIntent();
        fixIntent(intent);

        HyphonManager.getInstance().setCountryIso(ContactsUtils.getCurrentCountryIso());

        setContentView(R.layout.dialtacts_activity);

        mContactListFilterController = ContactListFilterController.getInstance(this);
        mContactListFilterController.addListener(mContactListFilterListener);

        findViewById(R.id.dialtacts_frame).addOnLayoutChangeListener(mFirstLayoutListener);

        mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mPageChangeListener);

        // Setup the ActionBar tabs (the order matches the tab-index contants TAB_INDEX_*)
        setupDialer();
        setupCallLog();
        setupFavorites();
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        
       	getActionBar().getTabAt(0).setIcon(null).setText(R.string.dialerIconLabel);
       	getActionBar().getTabAt(1).setIcon(null).setText(R.string.recentCallsIconLabel);
       	getActionBar().getTabAt(2).setIcon(null).setText(R.string.gn_contactsFavoritesLabel);

        // Load the last manually loaded tab
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        mLastManuallySelectedFragment = mPrefs.getInt(PREF_LAST_MANUALLY_SELECTED_TAB,
                PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT);
        if (mLastManuallySelectedFragment >= TAB_INDEX_COUNT) {
            // Stored value may have exceeded the number of current tabs. Reset it.
            mLastManuallySelectedFragment = PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT;
        }

        setCurrentTab(intent);

        if (UI.FILTER_CONTACTS_ACTION.equals(intent.getAction())
                && icicle == null) {
            setupFilterText(intent);
        }

        /**
         * add by mediatek .inc
         * description register the sim indicator changed broadcast receiver
         */
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            this.registerReceiver(mReceiver, intentFilter);
        }

        IntentFilter phbLoadIntentFilter =
            new IntentFilter((AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED));
        this.registerReceiver(mReceiver, phbLoadIntentFilter);

        mLaunched = true;
        /**
         * add by mediatek .inc end
         */
        Profiler.trace(Profiler.DialtactsActivityLeaveOnCreate);    
    }
        
    public boolean gnOnPrepareOptionsMenu(Menu menu) {
        final MenuItem searchMenuItem = menu.findItem(R.id.search_on_action_bar);
        final MenuItem filterOptionMenuItem = menu.findItem(R.id.filter_option);
        final MenuItem addContactOptionMenuItem = menu.findItem(R.id.add_contact);
        final MenuItem clearFrequentContactedMenuItem = menu.findItem(R.id.menu_clear_frequent);
        final MenuItem callSettingsMenuItem = menu.findItem(R.id.menu_call_settings);
        
        // gionee xuhz 20120511 add the menus in CallLog start
        final MenuItem gnCalllogMenuItem = menu.findItem(R.id.gn_show_other_calllog);
        final MenuItem gnCalllogDeleteAllMenuItem = menu.findItem(R.id.gn_calllog_delete_all);
        gnCalllogMenuItem.setVisible(false);
        gnCalllogDeleteAllMenuItem.setVisible(false);
        // gionee xuhz 20120511 add the menus in CallLog end
        clearFrequentContactedMenuItem.setVisible(false);
        
        Tab tab = getActionBar().getSelectedTab();
        if (mInSearchUi) {
            searchMenuItem.setVisible(false);
            if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
                filterOptionMenuItem.setVisible(true);
                filterOptionMenuItem.setOnMenuItemClickListener(
                        mFilterOptionsMenuItemClickListener);
                addContactOptionMenuItem.setVisible(true);
                addContactOptionMenuItem.setIntent(
                        new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI));
            } else {
                // Filter option menu should be not be shown as a overflow menu.
                filterOptionMenuItem.setVisible(false);
                addContactOptionMenuItem.setVisible(false);
            }
            callSettingsMenuItem.setVisible(false);
        } else {
            final boolean showCallSettingsMenu;
            if (tab != null && tab.getPosition() == TAB_INDEX_DIALER) {
                searchMenuItem.setVisible(false);
                // When permanent menu key is _not_ available, the call settings menu should be
                // available via DialpadFragment.
                showCallSettingsMenu = ViewConfiguration.get(this).hasPermanentMenuKey();
            } else if (tab != null && tab.getPosition() == TAB_INDEX_CALL_LOG) {
                //Gionee:huangzy 20120611 modify for CR00623367 start
            	/*boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();                
                searchMenuItem.setVisible(hasMenuKey);*/
                searchMenuItem.setVisible(false);
                //Gionee:huangzy 20120611 modify for CR00623367 end
                
                // gionee xuhz 20120511 add the menus in CallLog start
                //old : showCallSettingsMenu = hasMenuKey;
                showCallSettingsMenu = true;
                gnCalllogDeleteAllMenuItem.setVisible(true);
                gnCalllogMenuItem.setVisible(true);
                // gionee xuhz 20120511 add the menus in CallLog end
            } else {
                searchMenuItem.setVisible(true);
                searchMenuItem.setOnMenuItemClickListener(mSearchMenuItemClickListener);
                showCallSettingsMenu = true;
            }
            if (tab != null && tab.getPosition() == TAB_INDEX_FAVORITES) {
                filterOptionMenuItem.setVisible(true);
                filterOptionMenuItem.setOnMenuItemClickListener(
                        mFilterOptionsMenuItemClickListener);
                addContactOptionMenuItem.setVisible(true);
                addContactOptionMenuItem.setIntent(
                        new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI));

                if (mPhoneFavoriteFragment != null) {
                    mPhoneFavoriteFragment.onPrepareOptionsMenu(menu);
                }
            } else {
                filterOptionMenuItem.setVisible(false);
                addContactOptionMenuItem.setVisible(false);
            }

            if (showCallSettingsMenu) {
                callSettingsMenuItem.setVisible(true);
                callSettingsMenuItem.setIntent(DialtactsActivity.getCallSettingsIntent());
            } else {
                callSettingsMenuItem.setVisible(false);
            }
            
            // gionee xuhz 20120606 add start
            if (mDialpadFragment != null && tab != null) {
                if (tab.getPosition() == TAB_INDEX_DIALER) {
                    mDialpadFragment.gnShowAdditionalButtons(true);
                } else {
                    mDialpadFragment.gnShowAdditionalButtons(false);
                }
            }
            // gionee xuhz 20120530 add end
        }

        return true;
    }
    
    private static final int FLING_MIN_VERTICAL_DISTANCE = 50;
    private static final int FLING_MAX_HORIZONTAL_DISTANCE = 90;
    private GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Tab tab = getActionBar().getSelectedTab();
			if (null != tab && tab.getPosition() == TAB_INDEX_DIALER &&
					null != e1 && null != e2 && Math.abs(e1.getX() - e2.getX()) < FLING_MAX_HORIZONTAL_DISTANCE) {
				
				DialpadFragment dialpadFragment = mViewPagerAdapter.mDialpadFragment;
				if (null != dialpadFragment) {
					
					Rect rect = dialpadFragment.gnGetKeyLayoutRect();
					if (null != rect && rect.contains((int)e1.getX(), (int)e1.getY())) {
						float distanceY = e2.getY() - e1.getY();
						if (distanceY > FLING_MIN_VERTICAL_DISTANCE) {
							dialpadFragment.gnShowDialpad(false);
//							return true;
						} else if (-distanceY > FLING_MIN_VERTICAL_DISTANCE) {
							dialpadFragment.gnShowDialpad(true);
//							return true;
						}
						
					}
				}
			}
			
			return false;
		}
    	
    });
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (!ContactsApplication.sIsGnContactsSupport) {
    		return super.dispatchTouchEvent(ev);
    	}
    	
    	if(mGestureDetector.onTouchEvent(ev)) {
    		return true;
    	}
    	
    	switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsPressing = true;
			break;
		case MotionEvent.ACTION_UP:
			mIsPressing = false;
			break;

		default:
			break;
		}
    	
    	return super.dispatchTouchEvent(ev);
    }
    
    private void gnEnterSearchUi() {
        if (mSearchFragment == null) {
            return;
        }
        if (mSearchView == null) {
            prepareSearchView();
        }
        
        if (null != mSearchView) {
	        new SimpleAsynTask() {
				@Override
				protected Integer doInBackground(Integer... params) {
					int contactsCount = ContactsUtils.getContactsCount(getContentResolver());  
					return contactsCount;
				}
				
				protected void onPostExecute(Integer result) {
					mSearchView.setQueryHint(String.format(getString(R.string.gn_hint_findContacts), result));
				};
	        }.execute();
        }

        final ActionBar actionBar = getActionBar();

        final Tab tab = actionBar.getSelectedTab();

        // User can search during the call, but we don't want to remember the status.
        if (tab != null && !DialpadFragment.phoneIsInUse()) {
            mLastManuallySelectedFragment = tab.getPosition();
        }

        mSearchView.setQuery(null, true);

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        // gionee xuhz 20120529 modify for remove home icon from ActionBar start
        if (ContactsApplication.sIsGnContactsSupport) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            mGnAciontUpView.setVisibility(View.VISIBLE);
        } else {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            mGnAciontUpView.setVisibility(View.GONE);
        }
        // gionee xuhz 20120529 modify for remove home icon from ActionBar end

        sendFragmentVisibilityChange(mViewPager.getCurrentItem(), false);

        // Show the search fragment and hide everything else.
        mSearchFragment.setUserVisibleHint(true);
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.show(mSearchFragment);
        transaction.commitAllowingStateLoss();
        mViewPager.setVisibility(View.GONE);

        // We need to call this and onActionViewCollapsed() manually, since we are using a custom
        // layout instead of asking the search menu item to take care of SearchView.
        mSearchView.onActionViewExpanded();
        mInSearchUi = true;
    }
    
    public boolean getIsPressing() {
    	return mIsPressing;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Tab tab = getActionBar().getSelectedTab();
    	if (tab != null && tab.getPosition() == TAB_INDEX_FAVORITES) {
    		mPhoneFavoriteFragment.onOptionsItemSelected(item);
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
}
