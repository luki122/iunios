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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import aurora.preference.AuroraPreferenceActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.email.R;
import com.android.email.activity.ActivityHelper;
import com.android.email.provider.EmailProvider;
import com.android.emailcommon.Logging;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent.AccountColumns;
import com.android.emailcommon.service.ServiceProxy;
import com.android.emailcommon.utility.IntentUtilities;
import com.android.emailcommon.utility.Utility;
import com.android.mail.providers.Folder;
import com.android.mail.providers.UIProvider.EditSettingsExtras;
import com.android.mail.ui.FeedbackEnabledActivity;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.Utils;

import java.util.List;
import android.util.Log;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraEditTextPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;

import com.android.mail.ui.settings.SettingsUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import android.content.ContentResolver;
import android.content.ContentValues;
import com.android.email2.ui.MailActivityEmail;
import android.text.TextUtils;
//import android.widget.TextView;
import aurora.widget.AuroraActionBar;
import android.util.TypedValue;
import android.text.InputFilter;
import android.media.RingtoneManager;

/**
 * Handles account preferences, using multi-pane arrangement when possible.
 *
 * This activity uses the following fragments:
 *   AccountSettingsFragment
 *   Account{Incoming/Outgoing}Fragment
 *   AccountCheckSettingsFragment
 *   GeneralPreferences
 *   DebugFragment
 *
 * TODO: Delete account - on single-pane view (phone UX) the account list doesn't update properly
 * TODO: Handle dynamic changes to the account list (exit if necessary).  It probably makes
 *       sense to use a loader for the accounts list, because it would provide better support for
 *       dealing with accounts being added/deleted and triggering the header reload.
 */
public class AccountSettings extends AuroraPreferenceActivity implements FeedbackEnabledActivity,
        SetupData.SetupDataContainer, AuroraPreference.OnPreferenceChangeListener, OnSharedPreferenceChangeListener{
    /*
     * Intent to open account settings for account=1
        adb shell am start -a android.intent.action.EDIT \
            -d '"content://ui.email.android.com/settings?ACCOUNT_ID=1"'
     */

	private static final String PREFERENCE_SIGNATURE = "account_signature";
	private static final String PREFERENCE_FREQUENCY = "account_check_frequency";
	//Aurora <shihao> <20150402> for notification ringtone begin
	private static final String PRFE_NOTIFITCATION_SWITCH = "aurora_notification_switch";
	private static final String PRFE_NOTIFICATION_RINGTONE = "aurora_notification_ringtone";
	private AuroraSwitchPreference mNotificationSwitch;
	private AuroraPreferenceScreen mNotificationRingtone;
	//Aurora <shihao> <20150402> for notification ringtone end

    private AuroraEditTextPreference mAccountSignature;
    private AuroraListPreference mCheckFrequency;



    // Intent extras for our internal activity launch
    private static final String EXTRA_ENABLE_DEBUG = "AccountSettings.enable_debug";
    private static final String EXTRA_LOGIN_WARNING_FOR_ACCOUNT = "AccountSettings.for_account";
    private static final String EXTRA_LOGIN_WARNING_REASON_FOR_ACCOUNT =
            "AccountSettings.for_account_reason";
    private static final String EXTRA_TITLE = "AccountSettings.title";
    public static final String EXTRA_NO_ACCOUNTS = "AccountSettings.no_account";
    //Aurora <shihao> <2014-11-05> for add_New_AccountsWithResult from DrawLayout begin
    public static final String EXTRA_ADD_NEW_ACCOUNTS_FROM_DL = "AccountSettings.add_newAccount_fromDL";
    //Aurora <shihao> <2014-11-05> for add_New_AccountsWithResult from DrawLayout end

    // Intent extras for launch directly from system account manager
    // NOTE: This string must match the one in res/xml/account_preferences.xml
    private static String ACTION_ACCOUNT_MANAGER_ENTRY;
    // NOTE: This constant should eventually be defined in android.accounts.Constants
    private static final String EXTRA_ACCOUNT_MANAGER_ACCOUNT = "account";

    // Key for arguments bundle for QuickResponse editing
    private static final String QUICK_RESPONSE_ACCOUNT_KEY = "account";

    // Key codes used to open a debug settings fragment.
    private static final int[] SECRET_KEY_CODES = {
            KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_U,
            KeyEvent.KEYCODE_G
            };
    private int mSecretKeyCodeIndex = 0;

    // Support for account-by-name lookup
    private static final String SELECTION_ACCOUNT_EMAIL_ADDRESS =
        AccountColumns.EMAIL_ADDRESS + "=?";

    // When the user taps "Email Preferences" 10 times in a row, we'll enable the debug settings.
    private int mNumGeneralHeaderClicked = 0;

    private long mRequestedAccountId;
    private Account[] mAccountList;
    private Header mAppPreferencesHeader;
    /* package */ Fragment mCurrentFragment;
    private long mDeletingAccountId = -1;
    private boolean mShowDebugMenu;
    private List<Header> mGeneratedHeaders;
    private Uri mFeedbackUri;
    private MenuItem mFeedbackMenuItem;

    private SetupData mSetupData;

    // Async Tasks
    private LoadAccountListTask mLoadAccountListTask;
    private GetAccountIdFromAccountTask mGetAccountIdFromAccountTask;
    private ContentObserver mAccountObserver;

    // Specific callbacks used by settings fragments
    private final AccountSettingsFragmentCallback mAccountSettingsFragmentCallback
            = new AccountSettingsFragmentCallback();
    private final AccountServerSettingsFragmentCallback mAccountServerSettingsFragmentCallback
            = new AccountServerSettingsFragmentCallback();

	private boolean mSignatrueChanged = false;
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
        if (key.equals(PREFERENCE_FREQUENCY)) {
            final String summary = newValue.toString();
            final int index = mCheckFrequency.findIndexOfValue(summary);
            mCheckFrequency.setSummary(mCheckFrequency.getEntries()[index]);
            mCheckFrequency.setValue(summary);
            return false;
        } else if (key.equals(PREFERENCE_SIGNATURE)) {
            // Clean up signature if it's only whitespace (which is easy to do on a
            // soft keyboard) but leave whitespace in place otherwise, to give the user
            // maximum flexibility, e.g. the ability to indent
            String signature = newValue.toString();
            mAccountSignature.setText(signature);
            SettingsUtils.updatePreferenceSummary(mAccountSignature, signature, "");
			mSignatrueChanged = true;
            return false;
        } else {
            // Default behavior, just indicate that the preferences were written
            return true;
        } 
    }
    
    //Aurora <shihao> <20150402> for notification ringtone begin
    @Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		// TODO Auto-generated method stub
        if (key.equals(PRFE_NOTIFITCATION_SWITCH)){
        	mNotificationRingtone.setEnabled(mNotificationSwitch.isEnabled() && mNotificationSwitch.isChecked());
        } 
	}
	//Aurora <shihao> <20150402> for notification ringtone end

	private void loadSettings() {

		String signature = Account.mSignature;
		if(null == signature){
			signature = getResources().getString(R.string.aurora_default_signatrue);
		}
		mAccountSignature.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
		mAccountSignature.setText(signature);
		mAccountSignature.setOnPreferenceChangeListener(this);
		SettingsUtils.updatePreferenceSummary(mAccountSignature, signature, "");
		
		if(Account.mSyncInterval != -2 && Account.mSyncInterval < 0) Account.mSyncInterval = 15;
        mCheckFrequency.setValue(String.valueOf(Account.mSyncInterval));
        mCheckFrequency.setSummary(mCheckFrequency.getEntry());
        mCheckFrequency.setOnPreferenceChangeListener(this);
	}
	
	private void saveSettings() {
		if (mAccountList == null || mAccountList.length <= 0) {
			return;
		}
		
		String signature = mAccountSignature.getText();
		if(mSignatrueChanged){
			Account.mSignature = signature;
		}

		int freq = Integer.parseInt(mCheckFrequency.getValue());
		Account.mSyncInterval = freq;

        final int count = mAccountList.length;
        for (int index = 0; index < count; ++index) {
            Account account = mAccountList[index];
            if (account != null) {
				//ContentResolver.setSyncAutomatically(account, EmailContent.AUTHORITY, true);
				ContentValues cv = AccountSettingsUtils.getStaticContentValues(Account.mSyncInterval, Account.mSignature);
				account.update(this, cv);
            }
        }
		MailActivityEmail.setServicesEnabledAsync(this);
		
	}


    /**
     * Display (and edit) settings for a specific account, or -1 for any/all accounts
     */
    public static void actionSettings(Activity fromActivity, long accountId) {
        fromActivity.startActivity(createAccountSettingsIntent(accountId, null, null));
    }

    /**
     * Create and return an intent to display (and edit) settings for a specific account, or -1
     * for any/all accounts.  If an account name string is provided, a warning dialog will be
     * displayed as well.
     */
    public static Intent createAccountSettingsIntent(long accountId,
            String loginWarningAccountName, String loginWarningReason) {
        final Uri.Builder b = IntentUtilities.createActivityIntentUrlBuilder(
                IntentUtilities.PATH_SETTINGS);
        IntentUtilities.setAccountId(b, accountId);
        final Intent i = new Intent(Intent.ACTION_EDIT, b.build());
        if (loginWarningAccountName != null) {
            i.putExtra(EXTRA_LOGIN_WARNING_FOR_ACCOUNT, loginWarningAccountName);
        }
        if (loginWarningReason != null) {
            i.putExtra(EXTRA_LOGIN_WARNING_REASON_FOR_ACCOUNT, loginWarningReason);
        }
        return i;
    }




    /**
     * Launch generic settings and pre-enable the debug preferences
     */
    public static void actionSettingsWithDebug(Context fromContext) {
        final Intent i = new Intent(fromContext, AccountSettings.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(EXTRA_ENABLE_DEBUG, true);
        fromContext.startActivity(i);
    }


	private Intent getIntentByAccountId(long accountId,String accountName){
        if (accountId < 0) {
            return super.getIntent();
        }
        //Aurora <shihao> <20150408> for android5.0(with4.4) because in 5.0 it can't start Activity if do like old ways begin
//        Intent modIntent = new Intent(super.getIntent());
        Intent modIntent = new Intent(getApplicationContext(), AccountSettings.class);
        //Aurora <shihao> <20150408> end
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, AccountSettingsFragment.class.getCanonicalName());
		accountName = accountName.split("@")[0];//paul add
        modIntent.putExtra(
                EXTRA_SHOW_FRAGMENT_ARGUMENTS,
                AccountSettingsFragment.buildArguments(
                        accountId, accountName));
        modIntent.putExtra(EXTRA_TITLE, accountName);
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;

	}
 
    public void addAccounts() {
		AuroraPreferenceCategory accounts =
				(AuroraPreferenceCategory) findPreference("account_list");
		accounts.removeAll();
		
        if (mAccountList != null) {
            final int count = mAccountList.length;
            for (int index = 0; index < count; ++index) {
                Account account = mAccountList[index];
                if (account != null) {
					AuroraPreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
					intentPref.setIntent(getIntentByAccountId(account.mId,account.mEmailAddress));
					intentPref.setTitle(account.mEmailAddress);
					accounts.addPreference(intentPref);
                }
            }				
        }

		AuroraPreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
		if(null == mAccountSignature){
			mAccountSignature = (AuroraEditTextPreference) findPreference(PREFERENCE_SIGNATURE);
			mCheckFrequency = (AuroraListPreference) findPreference(PREFERENCE_FREQUENCY);
			//Aurora <shihao> <20150402> for notification ringtone begin
			mNotificationSwitch = (AuroraSwitchPreference) findPreference(PRFE_NOTIFITCATION_SWITCH);
			mNotificationRingtone = (AuroraPreferenceScreen) findPreference(PRFE_NOTIFICATION_RINGTONE);
			mNotificationRingtone.setIntent(new Intent()
					.setComponent(new ComponentName("com.android.settings", "com.aurora.audioprofile.AuroraRingPickerActivity")) //for android5.0
					.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
					.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.aurora_email_notifi_ringtone_title)));
			//Aurora <shihao> <20150402> for notification ringtone end
		}
		if(accounts.getPreferenceCount() > 0){
			mAccountSignature.setEnabled(true);
			mCheckFrequency.setEnabled(true);
			mNotificationSwitch.setEnabled(true); //Aurora <shihao> <20150402> for notification ringtone 
			mNotificationSwitch.setChecked(true);//Aurora <shihao> <20150402> for notification ringtone 
			intentPref.setIntent(new Intent(this,AccountSetupBasics.class).putExtra(AccountSetupBasics.EXTRA_FLOW_MODE, SetupData.FLOW_MODE_NORMAL));
		}else{
			mAccountSignature.setEnabled(false);
			mCheckFrequency.setEnabled(false);
			mNotificationSwitch.setEnabled(false);//Aurora <shihao> <20150402> for notification ringtone 
			mNotificationSwitch.setChecked(false);//Aurora <shihao> <20150402> for notification ringtone 
			Account.mSignature = null;
			Account.mSyncInterval = -1;
			intentPref.setIntent(new Intent(this,AccountSetupBasics.class).putExtra(AccountSetupBasics.EXTRA_FLOW_MODE, SetupData.FLOW_MODE_NO_ACCOUNTS));
		}
		mNotificationRingtone.setEnabled(mNotificationSwitch.isEnabled()&& mNotificationSwitch.isChecked());//Aurora <shihao> <20150402> for notification ringtone 
		intentPref.setTitle(R.string.aurora_account_settings_add);
		accounts.addPreference(intentPref);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.debugSetWindowFlags(this);

        final Intent i = getIntent();
        if (savedInstanceState == null) {
            // If we are not restarting from a previous instance, we need to
            // figure out the initial prefs to show.  (Otherwise, we want to
            // continue showing whatever the user last selected.)
            if (ACTION_ACCOUNT_MANAGER_ENTRY == null) {
                ACTION_ACCOUNT_MANAGER_ENTRY =
                        ServiceProxy.getIntentStringForEmailPackage(this,
                                getString(R.string.intent_account_manager_entry));
            }
            if (ACTION_ACCOUNT_MANAGER_ENTRY.equals(i.getAction())) {
                // This case occurs if we're changing account settings from Settings -> Accounts
                mGetAccountIdFromAccountTask =
                        (GetAccountIdFromAccountTask) new GetAccountIdFromAccountTask()
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, i);
            } else if (i.hasExtra(EditSettingsExtras.EXTRA_FOLDER)) {
                launchMailboxSettings(i);
                return;
            } else if (i.hasExtra(EXTRA_NO_ACCOUNTS)) { 
            	//Aurora <shihao> <2014-11-05> for add_New_AccountsWithResult from DrawLayout begin
            	boolean isNoAccount = i.getBooleanExtra(EXTRA_ADD_NEW_ACCOUNTS_FROM_DL, true);
            	if(isNoAccount)
            		AccountSetupBasics.actionNewAccountWithResult(this);
            	else
            		onAddNewAccount();
                //Aurora <shihao> <2014-11-05> for add_New_AccountsWithResult from DrawLayout end
                finish();
                return;
            } else {
				addPreferencesFromResource(R.xml.settings_preferences);
            }
        } else {
            mSetupData = savedInstanceState.getParcelable(SetupData.EXTRA_SETUP_DATA);
        }
        mShowDebugMenu = i.getBooleanExtra(EXTRA_ENABLE_DEBUG, false);

        final String title = i.getStringExtra(EXTRA_TITLE);
        if (title != null) {
            AuroraActionBar actionbar = getAuroraActionBar();
			actionbar.setTitle(title);
			TextView tv = (TextView)actionbar.getTitleView();
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
			tv.setTextColor(0xff000000);
        }
		/*
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
	*/
        mAccountObserver = new ContentObserver(Utility.getMainThreadHandler()) {
            @Override
            public void onChange(boolean selfChange) {
                updateAccounts();
            }
        };

        mFeedbackUri = Utils.getValidUri(getString(R.string.email_feedback_uri));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(
                outState);
        outState.putParcelable(SetupData.EXTRA_SETUP_DATA, mSetupData);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Aurora <shihao> <20150402> for notification ringtone begin
        if(getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null)
        	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this); 
        //Aurora <shihao> <20150402> for notification ringtone end
        getContentResolver().registerContentObserver(Account.NOTIFIER_URI, true, mAccountObserver);
        updateAccounts();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mAccountObserver);

		saveSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Aurora <shihao> <20150402> for notification ringtone begin
        if(getPreferenceScreen() != null && getPreferenceScreen().getSharedPreferences() != null)
        	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        //Aurora <shihao> <20150402> for notification ringtone end
        Utility.cancelTaskInterrupt(mLoadAccountListTask);
        mLoadAccountListTask = null;
        Utility.cancelTaskInterrupt(mGetAccountIdFromAccountTask);
        mGetAccountIdFromAccountTask = null;
    }

    /**
     * Listen for secret sequence and, if heard, enable debug menu
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == SECRET_KEY_CODES[mSecretKeyCodeIndex]) {
            mSecretKeyCodeIndex++;
            if (mSecretKeyCodeIndex == SECRET_KEY_CODES.length) {
                mSecretKeyCodeIndex = 0;
                enableDebugMenu();
            }
        } else {
            mSecretKeyCodeIndex = 0;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        mFeedbackMenuItem = menu.findItem(R.id.feedback_menu_item);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mFeedbackMenuItem != null) {
            // We only want to enable the feedback menu item, if there is a valid feedback uri
            mFeedbackMenuItem.setVisible(!Uri.EMPTY.equals(mFeedbackUri));
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // The app icon on the action bar is pressed.  Just emulate a back press.
                // TODO: this should navigate to the main screen, even if a sub-setting is open.
                // But we shouldn't just finish(), as we want to show "discard changes?" dialog
                // when necessary.
                onBackPressed();
                break;
            case R.id.add_new_account:
                onAddNewAccount();
                break;
            case R.id.feedback_menu_item:
                Utils.sendFeedback(this, mFeedbackUri, false /* reportingProblem */);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
            int titleRes, int shortTitleRes) {
        final Intent intent = super.onBuildStartFragmentIntent(
                fragmentName, args, titleRes, shortTitleRes);

        // When opening a sub-settings page (e.g. account specific page), see if we want to modify
        // the activity title.
        String title = AccountSettingsFragment.getTitleFromArgs(args);
        if ((titleRes == 0) && (title != null)) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        return intent;
    }

    /**
     * Any time we exit via this pathway, and we are showing a server settings fragment,
     * we put up the exit-save-changes dialog.  This will work for the following cases:
     *   Cancel button
     *   Back button
     *   Up arrow in application icon
     * It will *not* apply in the following cases:
     *   Click the parent breadcrumb - need to find a hook for this
     *   Click in the header list (e.g. another account) - handled elsewhere
     */
    @Override
    public void onBackPressed() {
        if (mCurrentFragment instanceof AccountServerBaseFragment) {
            if (((AccountServerBaseFragment) mCurrentFragment).haveSettingsChanged()) {
                UnsavedChangesDialogFragment dialogFragment =
                        UnsavedChangesDialogFragment.newInstanceForBack();
                dialogFragment.show(getFragmentManager(), UnsavedChangesDialogFragment.TAG);
                return; // Prevent "back" from being handled
            }
        }
        super.onBackPressed();
    }

    private void launchMailboxSettings(Intent intent) {
        final Folder folder = intent.getParcelableExtra(EditSettingsExtras.EXTRA_FOLDER);

        // TODO: determine from the account if we should navigate to the mailbox settings.
        // See bug 6242668

        // Get the mailbox id from the folder
        final long mailboxId =
                Long.parseLong(folder.folderUri.fullUri.getPathSegments().get(1));

        MailboxSettings.start(this, mailboxId);
        finish();
    }


    private void enableDebugMenu() {
        mShowDebugMenu = true;
        invalidateHeaders();
    }

    private void onAddNewAccount() {
        AccountSetupBasics.actionNewAccount(this);
    }

    /**
     * Start the async reload of the accounts list (if the headers are being displayed)
     */
    private void updateAccounts() {
        Utility.cancelTaskInterrupt(mLoadAccountListTask);
        mLoadAccountListTask = (LoadAccountListTask)
                new LoadAccountListTask().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, mDeletingAccountId);
    }

    /**
     * Generate and return the first header, for app preferences
     */
    private Header getAppPreferencesHeader() {
        // Set up fixed header for general settings
        if (mAppPreferencesHeader == null) {
            mAppPreferencesHeader = new Header();
            mAppPreferencesHeader.title = getText(R.string.header_label_general_preferences);
            mAppPreferencesHeader.summary = null;
            mAppPreferencesHeader.iconRes = 0;
            mAppPreferencesHeader.fragment = GeneralPreferences.class.getCanonicalName();
            mAppPreferencesHeader.fragmentArguments = null;
        }
        return mAppPreferencesHeader;
    }

    /**
     * This AsyncTask reads the accounts list and generates the headers.  When the headers are
     * ready, we'll trigger PreferenceActivity to refresh the account list with them.
     *
     * The array generated and stored in mAccountListHeaders may be sparse so any readers should
     * check for and skip over null entries, and should not assume array length is # of accounts.
     *
     * TODO: Smaller projection
     * TODO: Convert to Loader
     * TODO: Write a test, including operation of deletingAccountId param
     */
    private class LoadAccountListTask extends AsyncTask<Long, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Long... params) {
            Account[] result = null;
            Boolean deletingAccountFound = false;
            final long deletingAccountId = params[0];
            Cursor c = getContentResolver().query(
                    Account.CONTENT_URI,
                    Account.CONTENT_PROJECTION, null, null, null);
            try {
                int index = 0;
                result = new Account[c.getCount()];

                while (c.moveToNext()) {
                    final long accountId = c.getLong(Account.CONTENT_ID_COLUMN);
                    if (accountId == deletingAccountId) {
                        deletingAccountFound = true;
                        continue;
                    }
                    result[index] = new Account();
					result[index++].restore(c);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return new Object[] { result, deletingAccountFound };
        }

        @Override
        protected void onPostExecute(Object[] result) {
            if (isCancelled() || result == null) return;
            // Extract the results
            final Account[] accounts= (Account[]) result[0];
            final boolean deletingAccountFound = (Boolean) result[1];
            // report the settings
            mAccountList = accounts;
            if (!deletingAccountFound) {
                mDeletingAccountId = -1;
            }

			addAccounts();
			loadSettings();
        }
    }
	

    /**
     * Forcefully go backward in the stack. This may potentially discard unsaved settings.
     */
    private void forceBack() {
        // Clear the current fragment; we're navigating away
        mCurrentFragment = null;
        onBackPressed();
    }

    @Override
    public void onAttachFragment(Fragment f) {
        super.onAttachFragment(f);

        if (f instanceof AccountSettingsFragment) {
            final AccountSettingsFragment asf = (AccountSettingsFragment) f;
            asf.setCallback(mAccountSettingsFragmentCallback);
        } else if (f instanceof AccountServerBaseFragment) {
            final AccountServerBaseFragment asbf = (AccountServerBaseFragment) f;
            asbf.setCallback(mAccountServerSettingsFragmentCallback);
        } else {
            // Possibly uninteresting fragment, such as a dialog.
            return;
        }
        mCurrentFragment = f;

        // When we're changing fragments, enable/disable the add account button
        invalidateOptionsMenu();
    }

    /**
     * Callbacks for AccountSettingsFragment
     */
    private class AccountSettingsFragmentCallback implements AccountSettingsFragment.Callback {
        @Override
        public void onSettingsChanged(Account account, String preference, Object value) {
            //AccountSettings.this.onSettingsChanged(account, preference, value);
        }
        @Override
        public void onEditQuickResponses(com.android.mail.providers.Account account) {
            AccountSettings.this.onEditQuickResponses(account);
        }
        @Override
        public void onIncomingSettings(Account account) {
            AccountSettings.this.onIncomingSettings(account);
        }
        @Override
        public void onOutgoingSettings(Account account) {
            AccountSettings.this.onOutgoingSettings(account);
        }
        @Override
        public void abandonEdit() {
            finish();
        }
    }

    /**
     * Callbacks for AccountServerSettingsFragmentCallback
     */
    private class AccountServerSettingsFragmentCallback
            implements AccountServerBaseFragment.Callback {
        @Override
        public void onEnableProceedButtons(boolean enable) {
            // This is not used - it's a callback for the legacy activities
        }

        @Override
        public void onProceedNext(int checkMode, AccountServerBaseFragment target) {
            AccountCheckSettingsFragment checkerFragment =
                AccountCheckSettingsFragment.newInstance(checkMode, target);
            startPreferenceFragment(checkerFragment, true);
        }

        /**
         * After verifying a new server configuration as OK, we return here and continue.  This
         * simply does a "back" to exit the settings screen.
         */
        @Override
        public void onCheckSettingsComplete(int result, SetupData setupData) {
            if (result == AccountCheckSettingsFragment.CHECK_SETTINGS_OK) {
                // Settings checked & saved; clear current fragment
                mCurrentFragment = null;
                onBackPressed();
            }
        }
    }

    /**
     * Dispatch to edit quick responses.
     */
    public void onEditQuickResponses(com.android.mail.providers.Account account) {
        try {
            final Bundle args = new Bundle(1);
            args.putParcelable(QUICK_RESPONSE_ACCOUNT_KEY, account);
            startPreferencePanel(AccountSettingsEditQuickResponsesFragment.class.getName(), args,
                    R.string.account_settings_edit_quick_responses_label, null, null, 0);
        } catch (Exception e) {
            LogUtils.d(Logging.LOG_TAG, "Error while trying to invoke edit quick responses.", e);
        }
    }

    /**
     * Dispatch to edit incoming settings.
     */
    public void onIncomingSettings(Account account) {
        try {
            mSetupData = new SetupData(SetupData.FLOW_MODE_EDIT, account);
            final Fragment f = new AccountSetupIncomingFragment();
            f.setArguments(AccountSetupIncomingFragment.getArgs(true));
            // Use startPreferenceFragment here because we need to keep this activity instance
            startPreferenceFragment(f, true);
        } catch (Exception e) {
            LogUtils.d(Logging.LOG_TAG, "Error while trying to invoke store settings.", e);
        }
    }

    /**
     * Dispatch to edit outgoing settings.
     *
     * TODO: Make things less hardwired
     */
    public void onOutgoingSettings(Account account) {
        try {
            mSetupData = new SetupData(SetupData.FLOW_MODE_EDIT, account);
            final Fragment f = new AccountSetupOutgoingFragment();
            f.setArguments(AccountSetupOutgoingFragment.getArgs(true));
            // Use startPreferenceFragment here because we need to keep this activity instance
            startPreferenceFragment(f, true);
        } catch (Exception e) {
            LogUtils.d(Logging.LOG_TAG, "Error while trying to invoke sender settings.", e);
        }
    }

    /**
     * Delete the selected account
     */
    public void deleteAccount(final Account account) {
        // Kick off the work to actually delete the account
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Uri uri = EmailProvider.uiUri("uiaccount", account.mId);
                getContentResolver().delete(uri, null, null);
            }}).start();

        mDeletingAccountId = account.mId;
        updateAccounts();
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		} else {
			finish();
		}
    }

    /**
     * This AsyncTask looks up an account based on its email address (which is what we get from
     * the Account Manager).  When the account id is determined, we refresh the header list,
     * which will select the preferences for that account.
     */
    private class GetAccountIdFromAccountTask extends AsyncTask<Intent, Void, Long> {

        @Override
        protected Long doInBackground(Intent... params) {
            final Intent intent = params[0];
            android.accounts.Account acct =
                intent.getParcelableExtra(EXTRA_ACCOUNT_MANAGER_ACCOUNT);
            return Utility.getFirstRowLong(AccountSettings.this, Account.CONTENT_URI,
                    Account.ID_PROJECTION, SELECTION_ACCOUNT_EMAIL_ADDRESS,
                    new String[] {acct.name}, null, Account.ID_PROJECTION_COLUMN, -1L);
        }

        @Override
        protected void onPostExecute(Long accountId) {
            if (accountId != -1 && !isCancelled()) {
                mRequestedAccountId = accountId;
                invalidateHeaders();
            }
        }
    }

    /**
     * Dialog fragment to show "exit with unsaved changes?" dialog
     */
    public static class UnsavedChangesDialogFragment extends DialogFragment {
        final static String TAG = "UnsavedChangesDialogFragment";

        // Argument bundle keys
        private final static String BUNDLE_KEY_HEADER = "UnsavedChangesDialogFragment.Header";
        private final static String BUNDLE_KEY_BACK = "UnsavedChangesDialogFragment.Back";

        /**
         * Creates a save changes dialog when the user selects a new header
         * @param position The new header index to make active if the user accepts the dialog. This
         * must be a valid header index although there is no error checking.
         */
        public static UnsavedChangesDialogFragment newInstanceForHeader(int position) {
            final UnsavedChangesDialogFragment f = new UnsavedChangesDialogFragment();
            final Bundle b = new Bundle(1);
            b.putInt(BUNDLE_KEY_HEADER, position);
            f.setArguments(b);
            return f;
        }

        /**
         * Creates a save changes dialog when the user navigates "back".
         * {@link #onBackPressed()} defines in which case this may be triggered.
         */
        public static UnsavedChangesDialogFragment newInstanceForBack() {
            final UnsavedChangesDialogFragment f = new UnsavedChangesDialogFragment();
            final Bundle b = new Bundle(1);
            b.putBoolean(BUNDLE_KEY_BACK, true);
            f.setArguments(b);
            return f;
        }

        // Force usage of newInstance()
        public UnsavedChangesDialogFragment() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AccountSettings activity = (AccountSettings) getActivity();
            final int position = getArguments().getInt(BUNDLE_KEY_HEADER);
            final boolean isBack = getArguments().getBoolean(BUNDLE_KEY_BACK);

            return new AlertDialog.Builder(activity)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(R.string.account_settings_exit_server_settings)
                .setPositiveButton(
                        R.string.okay_action,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isBack) {
                                    activity.forceBack();
                                } else {
                                    //activity.forceSwitchHeader(position);
                                }
                                dismiss();
                            }
                        })
                .setNegativeButton(
                        activity.getString(R.string.cancel_action), null)
                .create();
        }
    }

    /**
     * Dialog briefly shown in some cases, to indicate the user that login failed.  If the user
     * clicks OK, we simply dismiss the dialog, leaving the user in the account settings for
     * that account;  If the user clicks "cancel", we exit account settings.
     */
    public static class LoginWarningDialog extends DialogFragment
            implements DialogInterface.OnClickListener {
        private static final String BUNDLE_KEY_ACCOUNT_NAME = "account_name";
        private String mReason;

        // Public no-args constructor needed for fragment re-instantiation
        public LoginWarningDialog() {}

        /**
         * Create a new dialog.
         */
        public static LoginWarningDialog newInstance(String accountName, String reason) {
            final LoginWarningDialog dialog = new LoginWarningDialog();
            final Bundle b = new Bundle(1);
            b.putString(BUNDLE_KEY_ACCOUNT_NAME, accountName);
            dialog.setArguments(b);
            dialog.mReason = reason;
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String accountName = getArguments().getString(BUNDLE_KEY_ACCOUNT_NAME);

            final Context context = getActivity();
            final Resources res = context.getResources();
            final AlertDialog.Builder b = new AlertDialog.Builder(context);
            b.setTitle(R.string.account_settings_login_dialog_title);
            b.setIconAttribute(android.R.attr.alertDialogIcon);
            if (mReason != null) {
                final TextView message = new TextView(context);
                final String alert = res.getString(
                        R.string.account_settings_login_dialog_reason_fmt, accountName, mReason);
                SpannableString spannableAlertString = new SpannableString(alert);
                Linkify.addLinks(spannableAlertString, Linkify.WEB_URLS);
                message.setText(spannableAlertString);
                // There must be a better way than specifying size/padding this way
                // It does work and look right, though
                final int textSize = res.getDimensionPixelSize(R.dimen.dialog_text_size);
                message.setTextSize(textSize);
                final int paddingLeft = res.getDimensionPixelSize(R.dimen.dialog_padding_left);
                final int paddingOther = res.getDimensionPixelSize(R.dimen.dialog_padding_other);
                message.setPadding(paddingLeft, paddingOther, paddingOther, paddingOther);
                message.setMovementMethod(LinkMovementMethod.getInstance());
                b.setView(message);
            } else {
                b.setMessage(res.getString(R.string.account_settings_login_dialog_content_fmt,
                        accountName));
            }
            b.setPositiveButton(R.string.okay_action, this);
            b.setNegativeButton(R.string.cancel_action, this);
            return b.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                getActivity().finish();
            }
        }
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public SetupData getSetupData() {
        return mSetupData;
    }

    @Override
    public void setSetupData(SetupData setupData) {
        mSetupData = setupData;
    }
}
