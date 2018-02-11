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

package com.aurora.auroraaccountsettings;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.collect.Maps;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;

public class AccountPreferenceBase extends SettingsPreferenceFragment
        implements OnAccountsUpdateListener {

    protected static final String TAG = "AccountSettings";
    public static final String AUTHORITIES_FILTER_KEY = "authorities";
    public static final String ACCOUNT_TYPES_FILTER_KEY = "account_types";
    private final Handler mHandler = new Handler();
    private Object mStatusChangeListenerHandle;
    private HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = null;
    private AuthenticatorHelper mAuthenticatorHelper = new AuthenticatorHelper();
    private java.text.DateFormat mDateFormat;
    private java.text.DateFormat mTimeFormat;
    public static List<AuroraPreference> prefList = new ArrayList<AuroraPreference>();

    public static boolean prefListFlag = true;
    public static boolean isClickFlag = true;


    /**
     * Overload to handle account updates.
     */
    public void onAccountsUpdated(Account[] accounts) {

    }

    /**
     * Overload to handle authenticator description updates
     */
    protected void onAuthDescriptionsUpdated() {

    }

    /**
     * Overload to handle sync state updates.
     */
    protected void onSyncStateUpdated() {

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();

        mDateFormat = DateFormat.getDateFormat(activity);
        mTimeFormat = DateFormat.getTimeFormat(activity);

    }

    @Override
    public void onResume() {
        super.onResume();

        mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
                        | ContentResolver.SYNC_OBSERVER_TYPE_STATUS
                        | ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS,
                mSyncStatusObserver);

        onSyncStateUpdated();
    }

    @Override
    public void onPause() {
        super.onPause();
        ContentResolver.removeStatusChangeListener(mStatusChangeListenerHandle);
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            mHandler.post(new Runnable() {
                public void run() {
                    onSyncStateUpdated();
                }
            });
        }
    };

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        if (mAccountTypeToAuthorities == null) {
            mAccountTypeToAuthorities = Maps.newHashMap();
            SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();
            for (int i = 0, n = syncAdapters.length; i < n; i++) {
                final SyncAdapterType sa = syncAdapters[i];
                ArrayList<String> authorities = mAccountTypeToAuthorities.get(sa.accountType);
                if (authorities == null) {
                    authorities = new ArrayList<String>();
                    mAccountTypeToAuthorities.put(sa.accountType, authorities);
                }
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.d(TAG, "added authority " + sa.authority + " to accountType "
                            + sa.accountType);
                }
                authorities.add(sa.authority);
            }
        }
        return mAccountTypeToAuthorities.get(type);
    }

    /**
     * Gets the preferences.xml file associated with a particular account type.
     *
     * @param accountType the type of account
     * @return a AuroraPreferenceScreen inflated from accountPreferenceId.
     */
    public AuroraPreferenceScreen addPreferencesForType(final String accountType,
                                                        AuroraPreferenceScreen parent) {
        AuroraPreferenceScreen prefs = null;

        if (mAuthenticatorHelper.containsAccountType(accountType)) {
            AuthenticatorDescription desc = null;
            try {
                desc = mAuthenticatorHelper.getAccountTypeDescription(accountType);
                if (desc != null && desc.accountPreferencesId != 0) {
                    Context authContext = getActivity().createPackageContext(desc.packageName, 0);
                    prefs = getPreferenceManager().inflateFromResource(authContext,
                            desc.accountPreferencesId, parent);
                    //Log.e("JOY", prefs.getSummary().toString() + " prefs");
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Couldn't load preferences.xml file from " + desc.packageName);
            } catch (Resources.NotFoundException e) {
                Log.w(TAG, "Couldn't load preferences.xml file from " + desc.packageName);
            }
        }

        // replace the preferenceCategory title  qy
        prefs.setOrderingAsAdded(true);
        if (prefListFlag) {
            prefList.clear();
            String title = null;
            AuroraPreference preference;
            for (int i = 0; i < prefs.getPreferenceCount(); i++) {
                if (prefs.getPreference(i) instanceof AuroraPreferenceCategory) {
                    AuroraPreferenceCategory auroraPrefCategory = new AuroraPreferenceCategory(getActivity());
                    String account = getActivity().getResources().getString(R.string.aurora_account_category_title);
                    Log.e("hello", "i =? " + i);
                    String secureSetting = getActivity().getResources().getString(R.string.aurora_account_category_security_title);
                    if (i == 0) {
                        Log.e("hello", "i = 0");
                        auroraPrefCategory.setTitle(account);
                    } else if (i == 2) {
                        Log.e("hello", "i = 2");
                        auroraPrefCategory.setTitle(secureSetting);
                    } else {
                        Log.e("hello", "i = else");
                        preference = ((AuroraPreferenceCategory) prefs.getPreference(i));
                        if (preference != null && preference.getTitle() != null) {
                            title = preference.getTitle().toString();
                        }
                        if (title != null) {
                            auroraPrefCategory.setTitle(title);
                        }
                    }
                    prefs.removePreference(prefs.getPreference(i));
                    auroraPrefCategory.setOrder(i);
                    prefs.addPreference(auroraPrefCategory);
                    prefList.add(auroraPrefCategory);
                } else {
                    AuroraPreference auroraPref = prefs.getPreference(i);
                    prefList.add(auroraPref);
                }
            }
        } else {
            prefs.removeAll();
            for (int i = 0; i < prefList.size(); i++) {
                prefs.addPreference(prefList.get(i));
            }
        }
        return prefs;
    }

    public void updateAuthDescriptions() {
        mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        onAuthDescriptionsUpdated();
    }

    protected Drawable getDrawableForType(final String accountType) {
        return mAuthenticatorHelper.getDrawableForType(getActivity(), accountType);
    }

    protected CharSequence getLabelForType(final String accountType) {
        return mAuthenticatorHelper.getLabelForType(getActivity(), accountType);
    }

    protected String formatSyncDate(Date date) {
        // TODO: Switch to using DateUtils.formatDateTime
        return mDateFormat.format(date) + " " + mTimeFormat.format(date);
    }
}
