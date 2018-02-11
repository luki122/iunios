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

package com.android.settings;

import com.android.internal.util.ArrayUtils;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.AuthenticatorHelper;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.applications.ManageApplications;
import com.android.settings.bluetooth.BluetoothEnabler;
import com.android.settings.deviceinfo.Memory;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.wifi.WifiEnabler;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
//import android.os.UserId;
import android.os.UserHandle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceActivity.Header;
import aurora.preference.AuroraPreferenceFragment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import aurora.widget.AuroraSwitch;
import android.widget.TextView;
import android.net.ConnectivityManager;
import aurora.app.AuroraActivity;
import com.android.internal.widget.ActionBarView;
import com.android.internal.telephony.TelephonyIntents;
import com.gionee.internal.telephony.GnTelephonyIntents;
import gionee.content.GnIntent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import android.os.SystemProperties;

//Gionee:zhang_xin 2012-12-15 add for CR00746738 start
import android.util.TypedValue;

//Gionee:zhang_xin 2012-12-15 add for CR00746738 end

/**
 * Top-level settings activity to handle single pane and double pane UI layout.
 */
public class GnSettingsCommon extends AuroraPreferenceActivity implements ButtonBarHandler,
        OnAccountsUpdateListener {

    private static final String LOG_TAG = "Settings";

    private static final String META_DATA_KEY_HEADER_ID = "com.android.settings.TOP_LEVEL_HEADER_ID";
    private static final String META_DATA_KEY_FRAGMENT_CLASS = "com.android.settings.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_PARENT_TITLE = "com.android.settings.PARENT_FRAGMENT_TITLE";
    private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS = "com.android.settings.PARENT_FRAGMENT_CLASS";

    private static final String EXTRA_CLEAR_UI_OPTIONS = "settings:remove_ui_options";

    private static final String SAVE_KEY_CURRENT_HEADER = "com.android.settings.CURRENT_HEADER";
    private static final String SAVE_KEY_PARENT_HEADER = "com.android.settings.PARENT_HEADER";

    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    private Header mParentHeader;
    private boolean mInLocalHeaderSwitch;
    private ActionBarView mActionBar;

    // Show only these settings for restricted users
    private int[] SETTINGS_FOR_RESTRICTED = {R.id.wifi_settings, R.id.bluetooth_settings,
            R.id.display_settings, R.id.security_settings};

    // TODO: Update Call Settings based on airplane mode state.

    protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();
    // /M: add for wifi only project tablet
    private boolean mIsWifiOnly = false;
    private AuthenticatorHelper mAuthenticatorHelper;
    private Header mLastHeader;
    private boolean mListeningToAccountUpdates;
    // /M: add for gray the imageview
    private static final int IMAGE_GRAY = 75; // 30% of 0xff in transparent
    private static final int ORIGINAL_IMAGE = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gionee fangbin 20120719 modified for CR00651589 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(
                GnSettingsUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120719 modified for CR00651589 end
        if (getIntent().getBooleanExtra(EXTRA_CLEAR_UI_OPTIONS, false)) {
            getWindow().setUiOptions(0);
        }

        mAuthenticatorHelper = new AuthenticatorHelper();
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, null);
        /** M: to get current mIsWifiOnly value @ { */
        final ConnectivityManager cm = ( ConnectivityManager ) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        mIsWifiOnly = (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
        /** @}*/
        getMetaData();
        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        mInLocalHeaderSwitch = false;

        if (!onIsHidingHeaders() && onIsMultiPane()) {
            highlightHeader(mTopLevelHeaderId);
            // Force the title so that it doesn't get overridden by a direct launch of
            // a specific settings screen.
            setTitle(R.string.settings_label);
        }

        // Retrieve any saved state
        if (savedInstanceState != null) {
            mCurrentHeader = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_HEADER);
            mParentHeader = savedInstanceState.getParcelable(SAVE_KEY_PARENT_HEADER);
        }

        // If the current header was saved, switch to it
        if (savedInstanceState != null && mCurrentHeader != null) {
            // switchToHeaderLocal(mCurrentHeader);
            showBreadCrumbs(mCurrentHeader.title, null);
        }

        if (mParentHeader != null) {
            setParentTitle(mParentHeader.title, null, new OnClickListener() {
                public void onClick(View v) {
                    switchToParent(mParentHeader.fragment);
                }
            });
        }

        // Override up navigation for multi-pane, since we handle it in the fragment breadcrumbs
        if (onIsMultiPane()) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(false);
        }

        // Gionee:zhang_xin 2012-12-26 add for CR00746738 start
        mActionBar = ( ActionBarView ) findViewById(com.android.internal.R.id.action_bar);
        if (mActionBar != null && !mActionBar.isSplitActionBar()) {
            mActionBar.setSplitActionBar(true);
        }
        // Gionee:zhang_xin 2012-12-26 add for CR00746738 start
        getListView().setSelector(android.R.color.transparent);
        getListView().setDivider(null);
        // Gionee:zhang_xin 2012-12-26 add for CR00746738 end

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            (( HeaderAdapter ) listAdapter).resume();
            // /M: add for sim management
            (( HeaderAdapter ) listAdapter).isSimManagementAvailable(this);
        }
        invalidateHeaders();

        IntentFilter intentFilter = new IntentFilter(GnTelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        intentFilter.addAction(GnIntent.SIM_SETTINGS_INFO_CHANGED);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mSimReceiver, intentFilter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current fragment, if it is the same as originally launched
        if (mCurrentHeader != null) {
            outState.putParcelable(SAVE_KEY_CURRENT_HEADER, mCurrentHeader);
        }
        if (mParentHeader != null) {
            outState.putParcelable(SAVE_KEY_PARENT_HEADER, mParentHeader);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            (( HeaderAdapter ) listAdapter).resume();
            // /M: add for sim management
            (( HeaderAdapter ) listAdapter).isSimManagementAvailable(this);
        }
        invalidateHeaders();
    }

    @Override
    public void onPause() {
        super.onPause();

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            (( HeaderAdapter ) listAdapter).pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListeningToAccountUpdates) {
            AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        }

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter instanceof HeaderAdapter) {
            (( HeaderAdapter ) listAdapter).pause();
        }
    }

    private void switchToHeaderLocal(Header header) {
        mInLocalHeaderSwitch = true;
        switchToHeader(header);
        mInLocalHeaderSwitch = false;
    }

    @Override
    public void switchToHeader(Header header) {
        if (!mInLocalHeaderSwitch) {
            mCurrentHeader = null;
            mParentHeader = null;
        }
        super.switchToHeader(header);
    }

    /**
     * AuroraSwitch to parent fragment and store the grand parent's info
     * 
     * @param className
     *            name of the activity wrapper for the parent fragment.
     */
    private void switchToParent(String className) {
        final ComponentName cn = new ComponentName(this, className);
        try {
            final PackageManager pm = getPackageManager();
            final ActivityInfo parentInfo = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);

            if (parentInfo != null && parentInfo.metaData != null) {
                String fragmentClass = parentInfo.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                CharSequence fragmentTitle = parentInfo.loadLabel(pm);
                Header parentHeader = new Header();
                parentHeader.fragment = fragmentClass;
                parentHeader.title = fragmentTitle;
                mCurrentHeader = parentHeader;

                switchToHeaderLocal(parentHeader);
                highlightHeader(mTopLevelHeaderId);

                mParentHeader = new Header();
                mParentHeader.fragment = parentInfo.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
                mParentHeader.title = parentInfo.metaData.getString(META_DATA_KEY_PARENT_TITLE);
            }
        } catch (NameNotFoundException nnfe) {
            Log.w(LOG_TAG, "Could not find parent activity : " + className);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // If it is not launched from history, then reset to top-level
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 && mFirstHeader != null
                && !onIsHidingHeaders() && onIsMultiPane()) {
            switchToHeaderLocal(mFirstHeader);
        }
    }

    private void highlightHeader(int id) {
        if (id != 0) {
            Integer index = mHeaderIndexMap.get(id);
            if (index != null) {
                getListView().setItemChecked(index, true);
                getListView().smoothScrollToPosition(index);
            }
        }
    }

    @Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null && !onIsMultiPane()) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, superIntent.getExtras());
            return modIntent;
        }
        return superIntent;
    }

    /**
     * Checks if the component name in the intent is different from the Settings class and returns the class
     * name to load as a fragment.
     */
    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) {
            return mFragmentClass;
        }

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) {
            return null;
        }

        if ("com.android.settings.ManageApplications".equals(intentClass)
                || "com.android.settings.RunningServices".equals(intentClass)
                || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            // Old names of manage apps.
            intentClass = com.android.settings.applications.ManageApplications.class.getName();
        }

        return intentClass;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be launched for a specific
     * fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName,
            Bundle args, int titleRes, int shortTitleRes) {
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);

        // some fragments want to avoid split actionbar
        if (DataUsageSummary.class.getName().equals(fragmentName)
                || PowerUsageSummary.class.getName().equals(fragmentName)
                || AccountSyncSettings.class.getName().equals(fragmentName)
                || UserDictionarySettings.class.getName().equals(fragmentName)
                || Memory.class.getName().equals(fragmentName)
                || ManageApplications.class.getName().equals(fragmentName)
                || WirelessSettings.class.getName().equals(fragmentName)
                || PrivacySettings.class.getName().equals(fragmentName)
                || ManageAccountsSettings.class.getName().equals(fragmentName)) {
            intent.putExtra(EXTRA_CLEAR_UI_OPTIONS, true);
        }

        intent.setClass(this, SubSettings.class);
        return intent;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> headers) {

        loadHeadersFromResource(R.xml.gn_settings_headers_common, headers);

        updateHeaderList(headers);

        // Gionee:zhang_xin 2012-12-15 add for CR00746738 start
        getFrameListBackground(GnSettingsCommon.this);
        sPreferenceBackgroundIndexs = getPreferenceBackgroundIndexs(headers);
        // Gionee:zhang_xin 2012-12-15 add for CR00746738 end
    }

    private void updateHeaderList(List<Header> target) {
        int i = 0;
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = ( int ) header.id;
            if (id == R.id.wifi_settings) {
                // Remove WiFi Settings if WiFi service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                    target.remove(header);
                }
            } else if (id == R.id.gn_mobile_network_settings) {
                if (mIsWifiOnly) {
                    target.remove(header);
                }
            } else if (id == R.id.bluetooth_settings) {
                // Remove Bluetooth Settings if Bluetooth service is not available.
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                    target.remove(header);
                }
            }
            //Gionee jingjc 2013-02-02 add for CR00770865 start
            else if (id == R.id.gn_power_saving_settings) {
               // if(!SystemProperties.get("ro.gn.powersaving.support","no").equals("yes")){
                    target.remove(header);
                //}
            }
            //Gionee jingjc 2013-02-02 add for CR00770865 end

            // Gionee zengxuanhui 20121120 add for CR00724044 begin
            else if (id == R.id.sound_settings) {
                boolean isGnProflieSupport = SystemProperties.get("ro.gn.audioprofile.support", "no").equals(
                        "yes");
                if (!isGnProflieSupport) {
                    target.remove(header);
                }
            }
            // Gionee zengxuanhui 20121120 add for CR00724044 end

            if (UserHandle.MU_ENABLED && UserHandle.myUserId() != 0
                    && !ArrayUtils.contains(SETTINGS_FOR_RESTRICTED, id)) {
                target.remove(header);
            }

           // if (UserId.MU_ENABLED && UserId.myUserId() != 0
              //   && !ArrayUtils.contains(SETTINGS_FOR_RESTRICTED, id)) {
             // target.remove(header);
           // }

            // Increment if the current one wasn't removed by the Utils code.
            if (target.get(i) == header) {
                // Hold on to the first header, when we need to reset to the top-level
                if (mFirstHeader == null
                        && HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
                    PackageManager.GET_META_DATA);
            if (ai == null || ai.metaData == null) {
                return;
            }
            mTopLevelHeaderId = ai.metaData.getInt(META_DATA_KEY_HEADER_ID);
            mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);

            // Check if it has a parent specified and create a Header object
            final int parentHeaderTitleRes = ai.metaData.getInt(META_DATA_KEY_PARENT_TITLE);
            String parentFragmentClass = ai.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
            if (parentFragmentClass != null) {
                mParentHeader = new Header();
                mParentHeader.fragment = parentFragmentClass;
                if (parentHeaderTitleRes != 0) {
                    mParentHeader.title = getResources().getString(parentHeaderTitleRes);
                }
            }
        } catch (NameNotFoundException nnfe) {
            // No recovery
        }
    }

    @Override
    public boolean hasNextButton() {
        return super.hasNextButton();
    }

    @Override
    public Button getNextButton() {
        return super.getNextButton();
    }

    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;
        static final int HEADER_TYPE_SWITCH = 2;
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_SWITCH + 1;

        private final WifiEnabler mWifiEnabler;
		//Aurora liugj 2013-10-22 modified for aurora's new feature start
        //private final BluetoothEnabler mBluetoothEnabler;
		//Aurora liugj 2013-10-22 modified for aurora's new feature end
        // /M: add for tablet feature check whether sim exist
        private boolean mIsSimEnable = false;
        private AuthenticatorHelper mAuthHelper;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
            AuroraSwitch switch_;
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
            if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else if (header.id == R.id.wifi_settings || header.id == R.id.bluetooth_settings) {
                return HEADER_TYPE_NORMAL;
            } else {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects, AuthenticatorHelper authenticatorHelper) {
            super(context, 0, objects);

            mAuthHelper = authenticatorHelper;
            mInflater = ( LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Temp Switches provided as placeholder until the adapter replaces these with actual
            // Switches inflated from their layouts. Must be done before adapter is set in super
            mWifiEnabler = new WifiEnabler(context, new AuroraSwitch(context));
			//Aurora liugj 2013-10-22 modified for aurora's new feature start
            //mBluetoothEnabler = new BluetoothEnabler(context, new AuroraSwitch(context));
			//Aurora liugj 2013-10-22 modified for aurora's new feature end
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null, android.R.attr.listSeparatorTextViewStyle);
                        holder.title = ( TextView ) view;
                        break;

                    case HEADER_TYPE_SWITCH:
                        view = mInflater.inflate(R.layout.preference_header_switch_item, parent, false);
                        holder.icon = ( ImageView ) view.findViewById(R.id.icon);
                        holder.title = ( TextView ) view.findViewById(android.R.id.title);
                        holder.summary = ( TextView ) view.findViewById(android.R.id.summary);
                        holder.switch_ = ( AuroraSwitch ) view.findViewById(R.id.switchWidget);
//                        holder.switch_.setVisibility(View.GONE);
                        break;

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(R.layout.preference_header_item, parent, false);
                        holder.icon = ( ImageView ) view.findViewById(R.id.icon);
                        holder.title = ( TextView ) view.findViewById(android.R.id.title);
                        holder.summary = ( TextView ) view.findViewById(android.R.id.summary);
                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = ( HeaderViewHolder ) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    break;

                case HEADER_TYPE_SWITCH:
                    // Would need a different treatment if the main menu had more switches
                    if (header.id == R.id.wifi_settings) {
                        mWifiEnabler.setSwitch(holder.switch_);
                    } else {
						//Aurora liugj 2013-10-22 modified for aurora's new feature start
                        //mBluetoothEnabler.setSwitch(holder.switch_);
						//Aurora liugj 2013-10-22 modified for aurora's new feature end
                    }
                    // No break, fall through on purpose to update common fields

                    //$FALL-THROUGH$
                case HEADER_TYPE_NORMAL:
                    if (header.extras != null
                            && header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                        String accType = header.extras.getString(ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                        ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
                        lp.width = getContext().getResources().getDimensionPixelSize(
                                R.dimen.header_icon_width);
                        lp.height = lp.width;
                        holder.icon.setLayoutParams(lp);
                        Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
                        holder.icon.setImageDrawable(icon);
                        // Gionee:zhang_xin 2012-12-25 add for CR00746738 start
                        if (accType.equals("com.android.email")) {
                            holder.icon.setImageResource(R.drawable.ic_settings_account_email);
                        }
                        // Gionee:zhang_xin 2012-12-25 add for CR00746738 end
                    } else {
                        holder.icon.setImageResource(header.iconRes);
                    }
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                    break;
            }
            // /M: add for sim management feature
            if (header.id == R.id.gn_mobile_network_settings) {
                handleDisableHolder(holder, view);
            } else {
                handleEnableHolder(holder, view);
            }

            // Gionee:zhang_xin 2012-12-15 add for CR00746738 start
            if (sPreferenceBackgroundIndexs != null && sPreferenceBackgroundIndexs.length > position) {
                if (sPreferenceBackgroundRes != null
                        && sPreferenceBackgroundIndexs[position] < sPreferenceBackgroundRes.length
                        && sPreferenceBackgroundRes[sPreferenceBackgroundIndexs[position]] > 0) {
                    view.setBackgroundResource(sPreferenceBackgroundRes[sPreferenceBackgroundIndexs[position]]);
                }
            }
            // Gionee:zhang_xin 2012-12-15 add for CR00746738 end

            return view;
        }

        /*
         * M: set the icon title and summary for sim management
         *@ param holder the view holder
         *@ param view the view
         */
        private void handleEnableHolder(HeaderViewHolder holder, View view) {
            if (holder.icon != null) {
                holder.icon.setEnabled(true);
                // /M: set the icon back to original alpha @{
                holder.icon.setAlpha(ORIGINAL_IMAGE);
                // /@}
            }
            if (holder.title != null) {
                holder.title.setEnabled(true);
            }
            if (holder.summary != null) {
                holder.summary.setEnabled(true);
            }
            view.setClickable(false);
        }

        /*
         * M: Disable the holder if there is no sim card 
         */
        private void handleDisableHolder(HeaderViewHolder holder, View view) {
            holder.icon.setEnabled(mIsSimEnable);
            /** M: add to gray the imageview when there is no sim card inserted @{ */
            holder.icon.setAlpha(mIsSimEnable ? ORIGINAL_IMAGE : IMAGE_GRAY);
            /**@}*/
            holder.title.setEnabled(mIsSimEnable);
            holder.summary.setEnabled(mIsSimEnable);
            view.setClickable(!mIsSimEnable);
        }

        public void isSimManagementAvailable(Context context) {
            int isInternetCallEnabled = android.provider.Settings.System.getInt(context.getContentResolver(),
                    gionee.provider.GnSettings.System.ENABLE_INTERNET_CALL, 0);
            Log.i(LOG_TAG, " isInternetCallEnabled = " + isInternetCallEnabled);
            boolean isVoipSupported = (android.net.sip.SipManager.isVoipSupported(context))
                    && (isInternetCallEnabled != 0);
            boolean isHasSimCards = (gionee.provider.GnTelephony.SIMInfo.getInsertedSIMCount(context) != 0);
            Log.i(LOG_TAG, " isVoipSupported = " + isVoipSupported);
            Log.i(LOG_TAG, " isHasSimCards = " + isHasSimCards);
//            mIsSimEnable = isHasSimCards || isVoipSupported;
            boolean isAirplaneMode = android.provider.Settings.System.getInt(context.getContentResolver(),
                    android.provider.Settings.System.AIRPLANE_MODE_ON, 0) != 0;
            boolean isAllRadioOff = (Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, -1) == 1)
                    || (Settings.System.getInt(context.getContentResolver(),
                    		gionee.provider.GnSettings.System.DUAL_SIM_MODE_SETTING, -1) == 0);
            Log.d(LOG_TAG, "isAllRadioOff=" + isAllRadioOff);
            mIsSimEnable = isHasSimCards && !isAllRadioOff;
        }

        public void resume() {
//            mWifiEnabler.resume();
//            mBluetoothEnabler.resume();
        }

        public void pause() {
//            mWifiEnabler.pause();
//            mBluetoothEnabler.pause();
        }

    }

    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            Log.d(LOG_TAG, "mSimReceiver receive action=" + action);
            if (action.equals(GnTelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)
                    || action.equals(GnIntent.SIM_SETTINGS_INFO_CHANGED)
                    || action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {

                ListAdapter listAdapter = getListAdapter();
                if (listAdapter instanceof HeaderAdapter) {
                    (( HeaderAdapter ) listAdapter).resume();
                    // /M: add for sim management
                    (( HeaderAdapter ) listAdapter).isSimManagementAvailable(context);
                }
                invalidateHeaders();
            }

        }
    };

    @Override
    public void onHeaderClick(Header header, int position) {
        boolean revert = false;
        if (header.id == R.id.account_add) {
            revert = true;
        }

        super.onHeaderClick(header, position);

        if (revert && mLastHeader != null) {
            highlightHeader(( int ) mLastHeader.id);
        } else {
            mLastHeader = header;
        }
    }

    @Override
    public boolean onPreferenceStartFragment(AuroraPreferenceFragment caller, AuroraPreference pref) {
        // Override the fragment title for Wallpaper settings
        int titleRes = pref.getTitleRes();
        if (pref.getFragment().equals(WallpaperTypeSettings.class.getName())) {
            titleRes = R.string.wallpaper_settings_fragment_title;
        }
        startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, pref.getTitle(), null, 0);
        return true;
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, Settings.class));
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders(), mAuthenticatorHelper));
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        mAuthenticatorHelper.onAccountsUpdated(this, accounts);
        invalidateHeaders();
    }

    // Gionee:zhang_xin 2012-12-15 add for CR00746738 start
    private static int[] sPreferenceBackgroundIndexs;
    private static int[] sPreferenceBackgroundRes;

    private static final int FRAME_LIST_BACKGROUND_NULL = 0;
    private static final int FRAME_LIST_BACKGROUND_FULL = 1;
    private static final int FRAME_LIST_BACKGROUND_TOP = 2;
    private static final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private static final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private static final int FRAME_LIST_BACKGROUND_TOTAL = 5;

    private int[] getPreferenceBackgroundIndexs(List<Header> header) {
        if (header == null || header.size() <= 0) {
            return null;
        }

        int[] arrays = new int[header.size()];
        for (int i = 0; i < header.size(); i++) {
            if (HeaderAdapter.getHeaderType(header.get(i)) == HeaderAdapter.HEADER_TYPE_CATEGORY) {
                arrays[i] = FRAME_LIST_BACKGROUND_NULL;
                continue;
            }

            if (i > 0) {
                switch (arrays[i - 1]) {
                    case 0:
                        arrays[i] = FRAME_LIST_BACKGROUND_FULL;
                        break;
                    case 1:
                        arrays[i - 1] = FRAME_LIST_BACKGROUND_TOP;
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 2:
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 3:
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 4:
                        arrays[i - 1] = FRAME_LIST_BACKGROUND_MIDDLE;
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    default:
                        break;
                }
            } else {
                arrays[i] = FRAME_LIST_BACKGROUND_FULL;
            }
        }
        return arrays;
    }

    private void getFrameListBackground(Context context) {
        sPreferenceBackgroundRes = new int[FRAME_LIST_BACKGROUND_TOTAL];
        sPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_NULL] = 0;
        TypedValue outValue = new TypedValue();
     
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBackground, outValue, true);
        sPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_FULL] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListTopBackground, outValue,
                 true);
        sPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_TOP] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListMiddleBackground, outValue,
                true);
        sPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBottomBackground, outValue,
                true);
        sPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_BOTTOM] = outValue.resourceId;
    }
    // Gionee:zhang_xin 2012-12-15 add for CR00746738 end

}
