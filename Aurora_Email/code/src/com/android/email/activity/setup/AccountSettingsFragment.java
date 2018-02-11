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

package com.android.email.activity.setup;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import aurora.preference.AuroraEditTextPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceFragment;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;

import com.android.email.R;
import com.android.email.SecurityPolicy;
import com.android.email.provider.EmailProvider;
import com.android.email.provider.FolderPickerActivity;
import com.android.email.service.EmailServiceUtils;
import com.android.email.service.EmailServiceUtils.EmailServiceInfo;
import com.android.email2.ui.MailActivityEmail;
import com.android.emailcommon.Logging;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.HostAuth;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.provider.Policy;
import com.android.emailcommon.utility.Utility;
import com.android.mail.preferences.AccountPreferences;
import com.android.mail.preferences.FolderPreferences;
import com.android.mail.providers.Folder;
import com.android.mail.providers.UIProvider;
import com.android.mail.ui.settings.SettingsUtils;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import com.android.email.activity.setup.AuroraDelAccCategory.OnViewClickListener;
import android.app.FragmentManager;
import android.util.Log;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.InputFilter;

/**
 * Fragment containing the main logic for account settings.  This also calls out to other
 * fragments for server settings.
 *
 * TODO: Remove or make async the mAccountDirty reload logic.  Probably no longer needed.
 * TODO: Can we defer calling addPreferencesFromResource() until after we load the account?  This
 *       could reduce flicker.
 */
public class AccountSettingsFragment extends AuroraPreferenceFragment
        implements AuroraPreference.OnPreferenceChangeListener {

    // Keys used for arguments bundle
    private static final String BUNDLE_KEY_ACCOUNT_ID = "AccountSettingsFragment.AccountId";
    private static final String BUNDLE_KEY_ACCOUNT_EMAIL = "AccountSettingsFragment.Email";



    private static final String PREFERENCE_NAME = "account_name";

	


	
	private AuroraEditTextPreference mAccountName;


    private AccountSettings mContext;

    /**
     * mAccount is email-specific, transition to using mUiAccount instead
     */
    @Deprecated
    private Account mAccount;
    private boolean mAccountDirty;
    private com.android.mail.providers.Account mUiAccount;
    private Callback mCallback = EmptyCallback.INSTANCE;
    private boolean mStarted;
    private boolean mLoaded;
    private boolean mSaveOnExit;


    /** The e-mail of the account being edited. */
    private String mAccountEmail;

    // Async Tasks
    private AsyncTask<?,?,?> mLoadAccountTask;

    /**
     * Callback interface that owning activities must provide
     */
    public interface Callback {
        public void onSettingsChanged(Account account, String preference, Object value);
        public void onEditQuickResponses(com.android.mail.providers.Account account);
        public void onIncomingSettings(Account account);
        public void onOutgoingSettings(Account account);
        public void abandonEdit();
    }

    private static class EmptyCallback implements Callback {
        public static final Callback INSTANCE = new EmptyCallback();
        @Override public void onSettingsChanged(Account account, String preference, Object value) {}
        @Override public void onEditQuickResponses(com.android.mail.providers.Account account) {}
        @Override public void onIncomingSettings(Account account) {}
        @Override public void onOutgoingSettings(Account account) {}
        @Override public void abandonEdit() {}
    }

    /**
     * If launching with an arguments bundle, use this method to build the arguments.
     */
    public static Bundle buildArguments(long accountId, String email) {
        Bundle b = new Bundle();
        b.putLong(BUNDLE_KEY_ACCOUNT_ID, accountId);
        b.putString(BUNDLE_KEY_ACCOUNT_EMAIL, email);
        return b;
    }

    public static String getTitleFromArgs(Bundle args) {
        return (args == null) ? null : args.getString(BUNDLE_KEY_ACCOUNT_EMAIL);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (AccountSettings)activity;
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before {@link #onActivityCreated(Bundle)}.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onCreate");
        }
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.account_settings_preferences);

        // Start loading the account data, if provided in the arguments
        // If not, activity must call startLoadingAccount() directly
        Bundle b = getArguments();
        if (b != null) {
            long accountId = b.getLong(BUNDLE_KEY_ACCOUNT_ID, -1);
            mAccountEmail = b.getString(BUNDLE_KEY_ACCOUNT_EMAIL);
            if (accountId >= 0 && !mLoaded) {
                startLoadingAccount(accountId);
            }
        }

        mAccountDirty = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onActivityCreated");
        }
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Called when the Fragment is visible to the user.
     */
    @Override
    public void onStart() {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onStart");
        }
        super.onStart();
        mStarted = true;

        // If the loaded account is ready now, load the UI
        if (mAccount != null && !mLoaded) {
            loadSettings();
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * TODO: Don't read account data on UI thread.  This should be fixed by removing the need
     * to do this, not by spinning up yet another thread.
     */
    @Override
    public void onResume() {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onResume");
        }
        super.onResume();

        if (mAccountDirty) {
            // if we are coming back from editing incoming or outgoing settings,
            // we need to refresh them here so we don't accidentally overwrite the
            // old values we're still holding here
            mAccount.mHostAuthRecv =
                HostAuth.restoreHostAuthWithId(mContext, mAccount.mHostAuthKeyRecv);
            mAccount.mHostAuthSend =
                HostAuth.restoreHostAuthWithId(mContext, mAccount.mHostAuthKeySend);
            // Because "delete policy" UI is on edit incoming settings, we have
            // to refresh that as well.
            Account refreshedAccount = Account.restoreAccountWithId(mContext, mAccount.mId);
            if (refreshedAccount == null || mAccount.mHostAuthRecv == null) {
                mSaveOnExit = false;
                mCallback.abandonEdit();
                return;
            }
            mAccount.setDeletePolicy(refreshedAccount.getDeletePolicy());
            mAccountDirty = false;
        }
    }

    @Override
    public void onPause() {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onPause");
        }
        super.onPause();
        if (mSaveOnExit) {
            saveSettings();
        }
    }

    /**
     * Called when the Fragment is no longer started.
     */
    @Override
    public void onStop() {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onStop");
        }
        super.onStop();
        mStarted = false;
    }

    /**
     * Listen to all preference changes in this class.
     * @param preference The changed Preference
     * @param newValue The new value of the Preference
     * @return True to update the state of the Preference with the new value
     */
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue){
        // Can't use a switch here. Falling back to a giant conditional.
        final String key = preference.getKey();
 		if (key.equals(PREFERENCE_NAME)) {
            final String summary = newValue.toString().trim();
            //if (!TextUtils.isEmpty(summary)) {
                mAccountName.setSummary(summary);
                mAccountName.setText(summary);
                preferenceChanged(PREFERENCE_NAME, summary);
            //}
            return false;
        } else {
            // Default behavior, just indicate that the preferences were written
            preferenceChanged(key, newValue);
            return true;
        }
    }

    /**
     * Called when the fragment is no longer in use.
     */
    @Override
    public void onDestroy() {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onDestroy");
        }
        super.onDestroy();

        Utility.cancelTaskInterrupt(mLoadAccountTask);
        mLoadAccountTask = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (Logging.DEBUG_LIFECYCLE && MailActivityEmail.DEBUG) {
            LogUtils.d(Logging.LOG_TAG, "AccountSettingsFragment onSaveInstanceState");
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.settings_fragment_menu, menu);
    }

    /**
     * Activity provides callbacks here
     */
    public void setCallback(Callback callback) {
        mCallback = (callback == null) ? EmptyCallback.INSTANCE : callback;
    }

    /**
     * Start loading a single account in preparation for editing it
     */
    public void startLoadingAccount(long accountId) {
        Utility.cancelTaskInterrupt(mLoadAccountTask);
        mLoadAccountTask = new LoadAccountTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, accountId);
    }

    /**
     * Async task to load account in order to view/edit it
     */
    private class LoadAccountTask extends AsyncTask<Long, Void, Map<String, Object>> {
        static final String ACCOUNT_KEY = "account";
        static final String UI_ACCOUNT_KEY = "uiAccount";

        @Override
        protected Map<String, Object> doInBackground(Long... params) {
            final long accountId = params[0];
            Account account = Account.restoreAccountWithId(mContext, accountId);
            if (account != null) {
                account.mHostAuthRecv =
                    HostAuth.restoreHostAuthWithId(mContext, account.mHostAuthKeyRecv);
                account.mHostAuthSend =
                    HostAuth.restoreHostAuthWithId(mContext, account.mHostAuthKeySend);
                if (account.mHostAuthRecv == null) {
                    account = null;
                }
            }

            final Cursor accountCursor = mContext.getContentResolver().query(EmailProvider
                    .uiUri("uiaccount", accountId), UIProvider.ACCOUNTS_PROJECTION, null,
                    null, null);

            final com.android.mail.providers.Account uiAccount;
            try {
                if (accountCursor != null && accountCursor.moveToFirst()) {
                    uiAccount = new com.android.mail.providers.Account(accountCursor);
                } else {
                    uiAccount = null;
                }
            } finally {
                if (accountCursor != null) {
                    accountCursor.close();
                }
            }

            final Map<String, Object> map = new HashMap<String, Object>(2);
            map.put(ACCOUNT_KEY, account);
            map.put(UI_ACCOUNT_KEY, uiAccount);
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Object> map) {
            if (!isCancelled()) {
                final Account account = (Account) map.get(ACCOUNT_KEY);
                mUiAccount = (com.android.mail.providers.Account) map.get(UI_ACCOUNT_KEY);
                if (account == null) {
                    mSaveOnExit = false;
                    mCallback.abandonEdit();
                } else {
                    mAccount = account;
                    if (mStarted && !mLoaded) {
                        loadSettings();
                    }
                }
            }
        }
    }

    /**
     * Loads settings that are dependent on a {@link com.android.mail.providers.Account}, which
     * must be obtained off the main thread.
     */
    private void loadSettingsOffMainThread() {
    
    }

    /**
     * Load account data into preference UI. This must be called on the main thread.
     */
    private void loadSettings() {
        // We can only do this once, so prevent repeat
        mLoaded = true;
        // Once loaded the data is ready to be saved, as well
        mSaveOnExit = false;

        loadSettingsOffMainThread();

        mAccountName = (AuroraEditTextPreference) findPreference(PREFERENCE_NAME);
		mAccountName.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
		
		AuroraDelAccCategory category = (AuroraDelAccCategory) findPreference("account_delete_category");
		if(null != category){
			category.setClickListener(new OnViewClickListener(){
				@Override
				public void onViewClick(){
					mContext.deleteAccount(mAccount);
				}
			});
		}

		
        String senderName = mAccount.getSenderName();
        // In rare cases, sendername will be null;  Change this to empty string to avoid NPE's
        if (senderName == null) senderName = "";
        mAccountName.setSummary(senderName);
        mAccountName.setText(senderName);
        mAccountName.setOnPreferenceChangeListener(this);
    }

    /**
     * Called any time a preference is changed.
     */
    private void preferenceChanged(String preference, Object value) {
        mCallback.onSettingsChanged(mAccount, preference, value);
        mSaveOnExit = true;
    }

    /*
     * Note: This writes the settings on the UI thread.  This has to be done so the settings are
     * committed before we might be killed.
     */
    private void saveSettings() {
        // Turn off all controlled flags - will turn them back on while checking UI elements
        //int newFlags = mAccount.getFlags() & ~(Account.FLAGS_BACKGROUND_ATTACHMENTS);

        // The sender name must never be empty (this is enforced by the preference editor)
        mAccount.setSenderName(mAccountName.getText().trim());
        //mAccount.setFlags(newFlags);

        // Commit the changes
        // Note, this is done in the UI thread because at this point, we must commit
        // all changes - any time after onPause completes, we could be killed.  This is analogous
        // to the way that SharedPreferences tries to work off-thread in apply(), but will pause
        // until completion in onPause().
        ContentValues cv = AccountSettingsUtils.getSenderNameValues(mAccount.getSenderName());
        mAccount.update(mContext, cv);

        // Run the remaining changes off-thread
        MailActivityEmail.setServicesEnabledAsync(mContext);
    }

    public String getAccountEmail() {
        // Get the e-mail address of the account being editted, if this is for an existing account.
        return mAccountEmail;
    }
}
