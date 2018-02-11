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

package com.android.email.activity.setup;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ActionBar;
import android.view.MenuItem;

import com.android.email.EmailAddressValidator;
import com.android.email.EmailConnectivityManager;
import com.android.email.Preferences;
import com.android.email.R;
import com.android.email.activity.ActivityHelper;
import com.android.email.activity.UiUtilities;
import com.android.email.service.EmailServiceUtils;
import com.android.email.service.EmailServiceUtils.EmailServiceInfo;
import com.android.emailcommon.Logging;
import com.android.emailcommon.VendorPolicyLoader.Provider;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.HostAuth;
import com.android.emailcommon.service.ServiceProxy;
import com.android.emailcommon.utility.EmailAsyncTask;
import com.android.emailcommon.utility.Utility;
import com.android.mail.utils.LogUtils;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
//Aurora <shihao> <2014-10-27> add for login model
import aurora.widget.AuroraLoginView.EventListener;
import aurora.widget.AuroraLoginView;
import android.view.inputmethod.InputMethodManager;
import com.android.emailcommon.mail.MessagingException;
import java.net.URI;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;

/**
 * Prompts the user for the email address and password. Also prompts for "Use this account as
 * default" if this is the 2nd+ account being set up.
 *
 * If the domain is well-known, the account is configured fully and checked immediately
 * using AccountCheckSettingsFragment.  If this succeeds we proceed directly to AccountSetupOptions.
 *
 * If the domain is not known, or the user selects Manual setup, we invoke the
 * AccountSetupAccountType activity where the user can begin to manually configure the account.
 *
 * === Support for automated testing ==
 * This activity can also be launched directly via ACTION_CREATE_ACCOUNT.  This is intended
 * only for use by continuous test systems, and is currently only available when
 * {@link ActivityManager#isRunningInTestHarness()} is set.  To use this mode, you must construct
 * an intent which contains all necessary information to create the account.  No connection
 * checking is done, so the account may or may not actually work.  Here is a sample command, for a
 * gmail account "test_account" with a password of "test_password".
 *
 *      $ adb shell am start -a com.android.email.CREATE_ACCOUNT \
 *          -e EMAIL test_account@gmail.com \
 *          -e USER "Test Account Name" \
 *          -e INCOMING imap+ssl+://test_account:test_password@imap.gmail.com \
 *          -e OUTGOING smtp+ssl+://test_account:test_password@smtp.gmail.com
 *
 * Note: For accounts that require the full email address in the login, encode the @ as %40.
 * Note: Exchange accounts that require device security policies cannot be created automatically.
 */
public class AccountSetupBasics extends AccountSetupActivity
        implements OnClickListener, TextWatcher, AccountCheckSettingsFragment.Callbacks ,AuroraLastLoginSetup.onErrorMsmListener{

    // Set to false before shipping, logs PII
    private final static boolean ENTER_DEBUG_SCREEN = false;

    /**
     * Direct access for forcing account creation
     * For use by continuous automated test system (e.g. in conjunction with monkey tests)
     */
    private static final String ACTION_CREATE_ACCOUNT = "com.android.email.CREATE_ACCOUNT";
    public static final String EXTRA_FLOW_MODE = "FLOW_MODE";//paul modify
    private static final String EXTRA_FLOW_ACCOUNT_TYPE = "FLOW_ACCOUNT_TYPE";
    private static final String EXTRA_CREATE_ACCOUNT_EMAIL = "EMAIL";
    private static final String EXTRA_CREATE_ACCOUNT_USER = "USER";
    private static final String EXTRA_CREATE_ACCOUNT_INCOMING = "INCOMING";
    private static final String EXTRA_CREATE_ACCOUNT_OUTGOING = "OUTGOING";
    private static final Boolean DEBUG_ALLOW_NON_TEST_HARNESS_CREATION = false;

    private static final String STATE_KEY_PROVIDER = "AccountSetupBasics.provider";

    // Support for UI
    private EditText mEmailView;
    private EditText mPasswordView;
    private final EmailAddressValidator mEmailValidator = new EmailAddressValidator();
    private Provider mProvider;
    private Button mManualButton;
    private Button mNextButton;
    private boolean mNextButtonInhibit;
    private boolean mPaused;
    private boolean mReportAccountAuthenticatorError;

    // FutureTask to look up the owner
    //FutureTask<String> mOwnerLookupTask; paul del
    
    //Aurora <shihao> <2014-10-27> add for New login model begin
    private AuroraLoginView mAuroraLoginView;
    private String mEmailName = new String();
    private String mPassword = new String();
    private InputMethodManager mInputMethodManager;
    private boolean isPopCheck = false;
    private String[] defaultDomins = {"qq.com","163.com","126.com","foxmail.com","vip.qq.com","sina.com",
    		"sina.cn","sohu.com","yeah.net","tom.com","21cn.com","139.com","189.com","wo.com.cn","iuni.com"};
    private String[] needOpenServiceDomins = {"qq.com"};
    boolean isNoAccount = true;
    EmailConnectivityManager mEmailConnectivityManager = null;
    private AuroraLastLoginSetup mAuroraLastLoginSetup;
    //Aurora <shihao> <2014-10-27> add for New login model end

    public static void actionNewAccount(Activity fromActivity) {
        final Intent i = new Intent(fromActivity, AccountSetupBasics.class);
        i.putExtra(EXTRA_FLOW_MODE, SetupData.FLOW_MODE_NORMAL);
        fromActivity.startActivity(i);
    }

    //Aurora <shihao> <2014-11-05> for add_New_AccountsWithResult from DrawLayout begin
    public static void actionNewAccountWithResult(Activity fromActivity) {
        final Intent i = new ForwardingIntent(fromActivity, AccountSetupBasics.class);
        i.putExtra(EXTRA_FLOW_MODE, SetupData.FLOW_MODE_NO_ACCOUNTS);
        fromActivity.startActivity(i);
    }
    //Aurora <shihao> <2014-11-05> for add_New_AccountsWithResult from DrawLayout end

    /**
     * This generates setup data that can be used to start a self-contained account creation flow
     * for exchange accounts.
     */
    public static Intent actionGetCreateAccountIntent(Context context, String accountManagerType) {
        final Intent i = new Intent(context, AccountSetupBasics.class);
        i.putExtra(EXTRA_FLOW_MODE, SetupData.FLOW_MODE_ACCOUNT_MANAGER);
        i.putExtra(EXTRA_FLOW_ACCOUNT_TYPE, accountManagerType);
        return i;
    }

    public static void actionAccountCreateFinishedAccountFlow(Activity fromActivity) {
        // TODO: handle this case - modifying state on SetupData when instantiating an Intent
        // is not safe, since it's not guaranteed that an Activity will run with the Intent, and
        // information can get lost.

        final Intent i= new ForwardingIntent(fromActivity, AccountSetupBasics.class);
        // If we're in the "account flow" (from AccountManager), we want to return to the caller
        // (in the settings app)
        i.putExtra(SetupData.EXTRA_SETUP_DATA, new SetupData(SetupData.FLOW_MODE_RETURN_TO_CALLER));
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }

    public static void actionAccountCreateFinishedWithResult(Activity fromActivity) {
        // TODO: handle this case - modifying state on SetupData when instantiating an Intent
        // is not safe, since it's not guaranteed that an Activity will run with the Intent, and
        // information can get lost.

        final Intent i= new ForwardingIntent(fromActivity, AccountSetupBasics.class);
        // If we're in the "no accounts" flow, we want to return to the caller with a result
        i.putExtra(SetupData.EXTRA_SETUP_DATA,
                new SetupData(SetupData.FLOW_MODE_RETURN_NO_ACCOUNTS_RESULT));
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }

    public static void actionAccountCreateFinished(final Activity fromActivity, Account account) {
        final Intent i = new Intent(fromActivity, AccountSetupBasics.class);
        // If we're not in the "account flow" (from AccountManager), we want to show the
        // message list for the new inbox
        i.putExtra(SetupData.EXTRA_SETUP_DATA,
                new SetupData(SetupData.FLOW_MODE_RETURN_TO_MESSAGE_LIST, account));
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.debugSetWindowFlags(this);

        // Check for forced account creation first, as it comes from an externally-generated
        // intent and won't have any SetupData prepared.
        final Intent intent = getIntent();
        final String action = intent.getAction();

        //Aurora <shihao> <2014-10-28> for New Login Model Title begin
        final int intentFlowMode =
                intent.getIntExtra(EXTRA_FLOW_MODE, SetupData.FLOW_MODE_UNSPECIFIED);
        //Aurora <shihao> <2014-10-28> for New Login Model Title end
        
        if (ServiceProxy.getIntentStringForEmailPackage(
                this, ACTION_CREATE_ACCOUNT).equals(action)) {
            mSetupData = new SetupData(SetupData.FLOW_MODE_FORCE_CREATE);
        } else {
/*            final int intentFlowMode =
                    intent.getIntExtra(EXTRA_FLOW_MODE, SetupData.FLOW_MODE_UNSPECIFIED);*/
            if (intentFlowMode != SetupData.FLOW_MODE_UNSPECIFIED) {
                mSetupData = new SetupData(intentFlowMode,
                        intent.getStringExtra(EXTRA_FLOW_ACCOUNT_TYPE));
            }
        }

        final int flowMode = mSetupData.getFlowMode();
        if (flowMode == SetupData.FLOW_MODE_RETURN_TO_CALLER) {
            // Return to the caller who initiated account creation
            finish();
            return;
        } else if (flowMode == SetupData.FLOW_MODE_RETURN_NO_ACCOUNTS_RESULT) {
            if (EmailContent.count(this, Account.CONTENT_URI) > 0) {
            	LogUtils.i("shihao","AccountSetupBasic RESULT_OK");
                setResult(RESULT_OK);
            } else {
            	LogUtils.i("shihao","AccountSetupBasic RESULT_CANCELED");
                setResult(RESULT_CANCELED);
            }
            finish();
            return;
        } else if (flowMode == SetupData.FLOW_MODE_RETURN_TO_MESSAGE_LIST) {
            final Account account = mSetupData.getAccount();
            if (account != null && account.mId >= 0) {
                // Show the message list for the new account
                //***
                //Welcome.actionOpenAccountInbox(this, account.mId);
                finish();
                return;
            }
        }
        
        //Aurora <shihao> <2014-10-28> for New Login Model Title begin
        if((intentFlowMode == SetupData.FLOW_MODE_NORMAL)
        		|| (intentFlowMode == SetupData.FLOW_MODE_ACCOUNT_MANAGER)){//add by shihao for add new Account from Settings_Account
        	setAuroraContentView(R.layout.account_setup_basics);
        	getAuroraActionBar().setTitle(getString(R.string.account_setup_basics_title_add_account));
        }else{
        	setAuroraContentView(R.layout.account_setup_basics,AuroraActionBar.Type.Empty);
        	getAuroraActionBar().setTitle(getString(R.string.account_setup_basics_title));
        }
//        setContentView(R.layout.account_setup_basics);
        //Aurora <shihao> <2014-10-28> for New Login Model Title end
        
        //Aurora <shihao> <2014-10-27> add for New login model begin
        initLoginView();
        //Aurora <shihao> <2014-10-27> add for New login model end
        mAuroraLastLoginSetup = new AuroraLastLoginSetup(this);
        mAuroraLastLoginSetup.setErrorMsmListener(this);
        
        mEmailView = UiUtilities.getView(this, R.id.account_email);
        mPasswordView = UiUtilities.getView(this, R.id.account_password);

        mEmailView.addTextChangedListener(this);
        mPasswordView.addTextChangedListener(this);

        // Configure buttons
        mManualButton = UiUtilities.getView(this, R.id.manual_setup);
        mNextButton = UiUtilities.getView(this, R.id.next);
        mManualButton.setVisibility(View.VISIBLE);
        mManualButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        // Force disabled until validator notifies otherwise
        onEnableProceedButtons(false);
        // Lightweight debounce while Async tasks underway
        mNextButtonInhibit = false;

        // Set aside incoming AccountAuthenticatorResponse, if there was any
        final AccountAuthenticatorResponse authenticatorResponse =
            getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        mSetupData.setAccountAuthenticatorResponse(authenticatorResponse);
        if (authenticatorResponse != null) {
            // When this Activity is called as part of account authentification flow,
            // we are responsible for eventually reporting the result (success or failure) to
            // the account manager.  Most exit paths represent an failed or abandoned setup,
            // so the default is to report the error.  Success will be reported by the code in
            // AccountSetupOptions that commits the finally created account.
            mReportAccountAuthenticatorError = true;
        }

        // Load fields, but only once
        final String userName = mSetupData.getUsername();
        if (userName != null) {
            mEmailView.setText(userName);
            mSetupData.setUsername(null);
        }
        final String password = mSetupData.getPassword();
        if (userName != null) {
            mPasswordView.setText(password);
            mSetupData.setPassword(null);
        }

        // Handle force account creation immediately (now that fragment is set up)
        // This is never allowed in a normal user build and will exit immediately.
        if (mSetupData.getFlowMode() == SetupData.FLOW_MODE_FORCE_CREATE) {
            if (!DEBUG_ALLOW_NON_TEST_HARNESS_CREATION &&
                    !ActivityManager.isRunningInTestHarness()) {
                LogUtils.e(Logging.LOG_TAG,
                        "ERROR: Force account create only allowed while in test harness");
                finish();
                return;
            }
            final String email = intent.getStringExtra(EXTRA_CREATE_ACCOUNT_EMAIL);
            final String user = intent.getStringExtra(EXTRA_CREATE_ACCOUNT_USER);
            final String incoming = intent.getStringExtra(EXTRA_CREATE_ACCOUNT_INCOMING);
            final String outgoing = intent.getStringExtra(EXTRA_CREATE_ACCOUNT_OUTGOING);
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(user) ||
                    TextUtils.isEmpty(incoming) || TextUtils.isEmpty(outgoing)) {
                LogUtils.e(Logging.LOG_TAG, "ERROR: Force account create requires extras EMAIL, " +
                        "USER, INCOMING, OUTGOING");
                finish();
                return;
            }
            forceCreateAccount(email, user, incoming, outgoing);
            // calls finish
            onCheckSettingsComplete(AccountCheckSettingsFragment.CHECK_SETTINGS_OK, mSetupData);
            return;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_KEY_PROVIDER)) {
            mProvider = (Provider) savedInstanceState.getSerializable(STATE_KEY_PROVIDER);
        }

        // Launch a worker to look up the owner name.  It should be ready well in advance of
        // the time the user clicks next or manual.
        
        //paul del
        //mOwnerLookupTask = new FutureTask<String>(mOwnerLookupCallable);
        //EmailAsyncTask.runAsyncParallel(mOwnerLookupTask);
        
    }

    //Aurora <shihao> <2014-10-27> add for new Login Model begin
    public void initLoginView(){
    	mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	mEmailConnectivityManager = new EmailConnectivityManager(this, LogUtils.TAG);
    	
    	mAuroraLoginView = UiUtilities.getView(this, R.id.login);
    	mAuroraLoginView.setUserNameHint(getString(R.string.aurora_account_input_hint));
    	mAuroraLoginView.setPasswordHint(getString(R.string.aurora_passward_input_hint));
    	mAuroraLoginView.setLinkText(getString(R.string.aurora_account_manual_setup));
    	mAuroraLoginView.setEventListener(new EventListener() {
			
			@Override
			public void onSubmitClick(CharSequence userName,CharSequence password) {
				// TODO Auto-generated method stub
				//登录事件回调
				isPopCheck = false;
				mAuroraLoginView.setErrorMsg(null);
				mAuroraLoginView.showProgress();
				if(mInputMethodManager.isActive())
//					mInputMethodManager.hideSoftInputFromWindow(AccountSetupBasics.this.getCurrentFocus().getWindowToken(),
					if(mAuroraLoginView.getWindowToken() != null)
						mInputMethodManager.hideSoftInputFromWindow(mAuroraLoginView.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				
				mEmailName = userName.toString().trim();
				mPassword = password.toString();

				checkAndNext(false);
			}
			
			@Override
			public void onLinkClick() {}
			
			@Override
			public void onLinkClick(CharSequence userName,CharSequence password){
				// TODO Auto-generated method stub
				mEmailName = userName.toString().trim();
				mPassword = password.toString();
				mAuroraLoginView.setErrorMsg(null);
				checkAndNext(true);
			}
		});
    }
    //Aurora <shihao> <2015-01-09> for Manual_Setup_Login begin
    private void checkAndNext(boolean isManualSetup){
		//1.The account or password is empty
		if(TextUtils.isEmpty(mEmailName) || TextUtils.isEmpty(mPassword)){
			setErrorMessage(getString(R.string.account_or_password_not_complete));
		}
		//2.The account is not email_style like "@" ...
		else if(!mEmailValidator.isValid(mEmailName.toString().trim())){
			setErrorMessage(getString(R.string.account_not_support_style));
		} 
		//3.If there is not network connection
		else if(mEmailConnectivityManager != null && !mEmailConnectivityManager.hasConnectivity()){
			setErrorMessage(getString(R.string.network_is_not_connection));
		}
		//4.Go to login or Manual_Setup
		else{			
			if(isManualSetup)
				new DuplicateCheckTask(AccountSetupBasics.this, mEmailName, false)
        			.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else
				onNext();
		}
    }
    
    
    public void onErrorMessageReciver(String error){
    	if(!TextUtils.isEmpty(error))
    		setErrorMessage(error);
    };
    //Aurora <shihao> <2014-10-27> add for new Login Model end
    
    //Aurora <shihao> <2014-11-27> for tuan progress bar to login button begin
    public void onBackPressed(){
    	FragmentManager fm = getFragmentManager();
    	AccountCheckSettingsFragment fragment = null;
    	fragment = (AccountCheckSettingsFragment)fm.findFragmentByTag(AccountCheckSettingsFragment.TAG);
    	if(fragment != null)
        	mAuroraLoginView.dismissProgress();
  	  	super.onBackPressed(); 	  
    }
    //Aurora <shihao> <2014-11-27> for tuan progress bar to login button end
    
    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuroraLoginView.dismissProgress();
        mPaused = false;
    }

    @Override
    public void finish() {
        // If the account manager initiated the creation, and success was not reported,
        // then we assume that we're giving up (for any reason) - report failure.
        if (mReportAccountAuthenticatorError) {
            final AccountAuthenticatorResponse authenticatorResponse =
                    mSetupData.getAccountAuthenticatorResponse();
            if (authenticatorResponse != null) {
                authenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
                mSetupData.setAccountAuthenticatorResponse(null);
            }
        }
        super.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mProvider != null) {
            outState.putSerializable(STATE_KEY_PROVIDER, mProvider);
        }
    }

    /**
     * Implements OnClickListener
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                // Simple debounce - just ignore while async checks are underway
                if (mNextButtonInhibit) {
                    return;
                }
                onNext();
                break;
            case R.id.manual_setup:
                onManualSetup(false);
                break;
        }
    }

    /**
     * Implements TextWatcher
     */
    @Override
    public void afterTextChanged(Editable s) {
        validateFields();
    }

    /**
     * Implements TextWatcher
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Implements TextWatcher
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private void validateFields() {
        final boolean valid = !TextUtils.isEmpty(mEmailView.getText())
                && !TextUtils.isEmpty(mPasswordView.getText())
                && mEmailValidator.isValid(mEmailView.getText().toString().trim());
        onEnableProceedButtons(valid);

        // Warn (but don't prevent) if password has leading/trailing spaces
        AccountSettingsUtils.checkPasswordSpaces(this, mPasswordView);
    }

    /**
     * Return an existing username if found, or null.  This is the result of the Callable (below).
     */
     //paul del
     /*
    private String getOwnerName() {
        try {
            return mOwnerLookupTask.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }
	*/
    /**
     * Callable that returns the username (based on other accounts) or null.
     */
     //paul del
     /*
    private final Callable<String> mOwnerLookupCallable = new Callable<String>() {
        @Override
        public String call() {
            final Context context = AccountSetupBasics.this;

            final long lastUsedAccountId =
                    Preferences.getPreferences(context).getLastUsedAccountId();
            final long defaultId = Account.getDefaultAccountId(context, lastUsedAccountId);

            if (defaultId != -1) {
                final Account account = Account.restoreAccountWithId(context, defaultId);
                if (account != null) {
                    return account.getSenderName();
                }
            }

            return null;
        }
    };
	*/
    /**
     * Finish the auto setup process, in some cases after showing a warning dialog.
     */
    private void finishAutoSetup() {
    	//Aurora <shihao> <2014-10-27> add for New login model begin
    	final String email = mEmailName;
    	final String password = mPassword;
    	//Aurora <shihao> <2014-10-27> add for New login model end
        try {
        	if(!isPopCheck)
        		mProvider.expandTemplates(email);

            final Account account = mSetupData.getAccount();
            final HostAuth recvAuth = account.getOrCreateHostAuthRecv(this);
            HostAuth.setHostAuthFromString(recvAuth, mProvider.incomingUri);

            recvAuth.setLogin(mProvider.incomingUsername, password);
            final EmailServiceInfo info = EmailServiceUtils.getServiceInfo(this,
                    recvAuth.mProtocol);
            recvAuth.mPort =
                    ((recvAuth.mFlags & HostAuth.FLAG_SSL) != 0) ? info.portSsl : info.port;
//            LogUtils.i("shihao","AccountSetupBasci:: finishAutoSetup ---> recvAuth.mPort =="+recvAuth.mPort + "   recvAuth.mAddress="+recvAuth.mAddress);
            final HostAuth sendAuth = account.getOrCreateHostAuthSend(this);
            HostAuth.setHostAuthFromString(sendAuth, mProvider.outgoingUri);
            sendAuth.setLogin(mProvider.outgoingUsername, password);

            // Populate the setup data, assuming that the duplicate account check will succeed
            populateSetupData(null, email);//paul modify getOwnerName()
 //           LogUtils.i("shihao","AccountSetupBasci:: finishAutoSetup ---> sendAuth.mPort =="+sendAuth.mPort+ "   sendAuth.mAddress="+sendAuth.mAddress );
            // Stop here if the login credentials duplicate an existing account
            // Launch an Async task to do the work
            new DuplicateCheckTask(this, email, true)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (URISyntaxException e) {
            /*
             * If there is some problem with the URI we give up and go on to manual setup.
             * Technically speaking, AutoDiscover is OK here, since the user clicked "Next"
             * to get here. This will not happen in practice because we don't expect to
             * find any EAS accounts in the providers list.
             */
            onManualSetup(true);
        }
    }

    /**
     * Async task that continues the work of finishAutoSetup().  Checks for a duplicate
     * account and then either alerts the user, or continues.
     */
    private class DuplicateCheckTask extends AsyncTask<Void, Void, String> {
        private final Context mContext;
        private final String mCheckAddress;
        private final boolean mAutoSetup;

        public DuplicateCheckTask(Context context, String checkAddress,
                boolean autoSetup) {
            mContext = context;
            mCheckAddress = checkAddress;
            // Prevent additional clicks on the next button during Async lookup
            mNextButtonInhibit = true;
            mAutoSetup = autoSetup;
        }

        @Override
        protected String doInBackground(Void... params) {
            return Utility.findExistingAccount(mContext, null, mCheckAddress);
        }

        @Override
        protected void onPostExecute(String duplicateAccountName) {
            mNextButtonInhibit = false;
            // Exit immediately if the user left before we finished
            if (mPaused) return;
            // Show duplicate account warning, or proceed
            if (duplicateAccountName != null) {
            	//Aurora <shihao> <2014-10-29> for account duplicate begin
/*                final DuplicateAccountDialogFragment dialogFragment =
                    DuplicateAccountDialogFragment.newInstance(duplicateAccountName);
                dialogFragment.show(getFragmentManager(), DuplicateAccountDialogFragment.TAG);*/
            	setErrorMessage(getString(R.string.account_duplicate_dlg_message_fmt, duplicateAccountName));
            } else {
                if (mAutoSetup) {
                    final AccountCheckSettingsFragment checkerFragment =
                        AccountCheckSettingsFragment.newInstance(
                            SetupData.CHECK_INCOMING | SetupData.CHECK_OUTGOING, null);
                    final FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.add(checkerFragment, AccountCheckSettingsFragment.TAG);
                    transaction.addToBackStack("back");
                    transaction.commit();
                } else {
                    onManualSetup(true);
                }
            }
        }

        @Override
        protected void onCancelled(String s) {
            mNextButtonInhibit = false;
            LogUtils.d(LogUtils.TAG, "DuplicateCheckTask cancelled (AccountSetupBasics)");
        }
    }

    /**
     * When "next" button is clicked
     */
    private void onNext() {
        // Try auto-configuration from XML providers (unless in EAS mode, we can skip it)
    	
    	//Aurora <shihao> <2014-10-27> add for New login model begin
//        final String email = mEmailView.getText().toString().trim();
    	final String email = mEmailName;
    	//Aurora <shihao> <2014-10-27> add for New login model end
    	
        final String[] emailParts = email.split("@");
        final String domain = emailParts[1].trim();
        mProvider = AccountSettingsUtils.findProviderForDomain(this, domain);
        if (mProvider != null) {
            mProvider.expandTemplates(email);
            if (mProvider.note != null) {
                final NoteDialogFragment dialogFragment =
                        NoteDialogFragment.newInstance(mProvider.note);
                dialogFragment.show(getFragmentManager(), NoteDialogFragment.TAG);
            } else {
                finishAutoSetup();
            }
        } else {
        // Can't use auto setup (although EAS accounts may still be able to AutoDiscover)
        	//Aurora <shihao> <2014-10-24> add for New Login Model begin
    		AuroraAlertDialog builder = new AuroraAlertDialog.Builder(this)
	        .setTitle(R.string.app_name)
			.setMessage(R.string.aurora_login_need_manual_setup)
			.setPositiveButton(R.string.aurora_manual_login_goon_button, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					// TODO Auto-generated method stub
					new DuplicateCheckTask(AccountSetupBasics.this, email, false)
                    	.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					dialog.dismiss();
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					// TODO Auto-generated method stub
					mAuroraLoginView.dismissProgress();
					dialog.dismiss();
				}
			}).create();
    		builder.show();
          //Aurora <shihao> <2014-10-24> add for New Login Model end
        }
    }

    /**
     * When "manual setup" button is clicked
     *
     * @param allowAutoDiscover - true if the user clicked 'next' and (if the account is EAS)
     * it's OK to use autodiscover.  false to prevent autodiscover and go straight to manual setup.
     * Ignored for IMAP & POP accounts.
     */
    private void onManualSetup(boolean allowAutoDiscover) {
   /*     final String email = mEmailView.getText().toString().trim();
        final String password = mPasswordView.getText().toString();*/
        final String email = mEmailName;
        final String password = mPassword;
        
        final String[] emailParts = email.split("@");
        final String user = emailParts[0].trim();
        final String domain = emailParts[1].trim();

        // Alternate entry to the debug options screen (for devices without a physical keyboard:
        //  Username: d@d.d
        //  Password: debug
        if (ENTER_DEBUG_SCREEN && "d@d.d".equals(email) && "debug".equals(password)) {
            mEmailView.setText("");
            mPasswordView.setText("");
            AccountSettings.actionSettingsWithDebug(this);
            return;
        }

        final Account account = mSetupData.getAccount();
        final HostAuth recvAuth = account.getOrCreateHostAuthRecv(this);
        recvAuth.setLogin(user, password);
        recvAuth.setConnection(null, domain, HostAuth.PORT_UNKNOWN, HostAuth.FLAG_NONE);

        final HostAuth sendAuth = account.getOrCreateHostAuthSend(this);
        sendAuth.setLogin(user, password);
        sendAuth.setConnection(null, domain, HostAuth.PORT_UNKNOWN, HostAuth.FLAG_NONE);

        populateSetupData(null, email);//paul modify getOwnerName()

        mSetupData.setAllowAutodiscover(allowAutoDiscover);
//        AccountSetupType.actionSelectAccountType(this, mSetupData);
        AuroraManualSettings.actionAccountManualSetup(this, mSetupData);
    }

    /**
     * To support continuous testing, we allow the forced creation of accounts.
     * This works in a manner fairly similar to automatic setup, in which the complete server
     * Uri's are available, except that we will also skip checking (as if both checks were true)
     * and all other UI.
     *
     * @param email The email address for the new account
     * @param user The user name for the new account
     * @param incoming The URI-style string defining the incoming account
     * @param outgoing The URI-style string defining the outgoing account
     */
    private void forceCreateAccount(String email, String user, String incoming, String outgoing) {
        Account account = mSetupData.getAccount();
        try {
            final HostAuth recvAuth = account.getOrCreateHostAuthRecv(this);
            HostAuth.setHostAuthFromString(recvAuth, incoming);

            final HostAuth sendAuth = account.getOrCreateHostAuthSend(this);
            HostAuth.setHostAuthFromString(sendAuth, outgoing);

            populateSetupData(user, email);
        } catch (URISyntaxException e) {
            // If we can't set up the URL, don't continue - account setup pages will fail too
            Toast.makeText(
                    this, R.string.account_setup_username_password_toast, Toast.LENGTH_LONG).show();
        }
    }

    public static void setDefaultsForProtocol(Context context, Account account) {
        final String protocol = account.mHostAuthRecv.mProtocol;
        if (protocol == null) return;
        final EmailServiceInfo info = EmailServiceUtils.getServiceInfo(context, protocol);
		//paul modify start
		//account.mSyncInterval = info.defaultSyncInterval;
		if(Account.INVALID_SYNC_INTERVAL == Account.mSyncInterval){
        	Account.mSyncInterval = info.defaultSyncInterval;
		}
		//paul modify end
        account.mSyncLookback = info.defaultLookback;
        if (info.offerLocalDeletes) {
            account.setDeletePolicy(info.defaultLocalDeletes);
        }
    }

    /**
     * Populate SetupData's account with complete setup info.
     */
    private void populateSetupData(String senderName, String senderEmail) {
        final Account account = mSetupData.getAccount();
        //account.setSenderName(senderName); paul del
        account.setEmailAddress(senderEmail);
        account.setDisplayName(senderEmail);
        setDefaultsForProtocol(this, account);
    }

    /**
     * Implements AccountCheckSettingsFragment.Callbacks
     *
     * This is used in automatic setup mode to jump directly down to the options screen.
     *
     * This is the only case where we finish() this activity but account setup is continuing,
     * so we inhibit reporting any error back to the Account manager.
     */
    @Override
    public void onCheckSettingsComplete(int result, SetupData setupData) {
        mSetupData = setupData;
        if (result == AccountCheckSettingsFragment.CHECK_SETTINGS_OK) {
        	mAuroraLastLoginSetup.toConfigureAndCommitAccount(mSetupData);
//            AccountSetupOptions.actionOptions(this, mSetupData);
            mReportAccountAuthenticatorError = false;
//            finish();
        }
    }

    /**
     * Implements AccountCheckSettingsFragment.Callbacks
     * This is overridden only by AccountSetupIncoming
     */
    @Override
    public void onAutoDiscoverComplete(int result, SetupData setupData) {
        throw new IllegalStateException();
    }
    
    //Aurora <shihao> <2014-10-24> add for show ErrorMessage when Login failed begin
    /**
     * 1.First we use imap protocol to login 
     * 2.if there are some error like "con't connect server.."
     * 3.we try to use pop3 to login again
     * @param errorMessage
     * @param ex
     */
    @Override
    public void onSendErrorMessage(String errorMessage,MessagingException ex){
    	boolean isExist = false;
    	boolean isNeedOpenServer = false;
        final String[] emailParts = mEmailName.split("@");
        final String currentDomain = emailParts[1].trim();
        
        for(String domain : needOpenServiceDomins){
        	if(currentDomain.equalsIgnoreCase(domain)){
        		isNeedOpenServer = true;
        		break;
        	}
        }
        
    	//First time wo login failed
    	if(!isPopCheck){
    		isPopCheck = true;  	
            for(String domain : defaultDomins){
            	if(currentDomain.equalsIgnoreCase(domain)){
            		isExist = true;
            		break;
            	}
            }
            //If the email contains defaultDomins,wo try to check and use pop3 login
            if(isExist){
            	try{
            		checkAndUsePop3ToLogin(errorMessage,ex,isNeedOpenServer);
            	}catch (URISyntaxException e) {
					// TODO: handle exception
            		setErrorMessage(errorMessage,ex.getExceptionType(),isNeedOpenServer);
				}
            }else
            	setErrorMessage(errorMessage,ex.getExceptionType(),isNeedOpenServer);
    	}
    	//the second time wo failed
    	else{
        	setErrorMessage(errorMessage,ex.getExceptionType(),isNeedOpenServer);
    	}
    }
    
    public void checkAndUsePop3ToLogin(String errorMsg,MessagingException ex,boolean isNeedOpenServer)
    		throws URISyntaxException{
    	String incomingUrl = mProvider.incomingUri;
    	if(incomingUrl.contains("imap") && getChangeMode(ex)){
    		if(setInComingUrl(incomingUrl))
    			finishAutoSetup();
    		else
    			setErrorMessage(errorMsg,ex.getExceptionType(),isNeedOpenServer);
    	}else{
    		setErrorMessage(errorMsg,ex.getExceptionType(),isNeedOpenServer);
    	}
    }
    
    public boolean getChangeMode(MessagingException ex) {
    	boolean isNeedtoChange = false;
        switch (ex.getExceptionType()) {
        	//These need to relogin with pop3
            case MessagingException.CERTIFICATE_VALIDATION_ERROR://///////////////                
            case MessagingException.IOERROR:////////////////////////////               
            case MessagingException.PROTOCOL_VERSION_UNSUPPORTED:///////////////////////
            	isNeedtoChange = true;
                break;
                
            //These Exceptions not need to restart login with pop3
            case MessagingException.AUTHENTICATION_FAILED:
            case MessagingException.AUTODISCOVER_AUTHENTICATION_FAILED:
            case MessagingException.AUTHENTICATION_FAILED_OR_SERVER_ERROR:
            case MessagingException.TLS_REQUIRED:
            case MessagingException.AUTH_REQUIRED:
            case MessagingException.SECURITY_POLICIES_UNSUPPORTED:
            case MessagingException.ACCESS_DENIED:
            case MessagingException.GENERAL_SECURITY:
            case MessagingException.CLIENT_CERTIFICATE_REQUIRED:
            case MessagingException.CLIENT_CERTIFICATE_ERROR:
            	isNeedtoChange = false;
                break;
                
            default:     /////////////
            	isNeedtoChange = true;
                break;
        }
        return isNeedtoChange;
    }
    
    //Change incomingUrl to Pop3 protocol
    public boolean setInComingUrl(String incomingUrl){
    	try{
    		URI uri = new URI(incomingUrl);
    		String scheme = uri.getScheme().replaceAll("imap", "pop3");
    		String host = uri.getHost().replaceAll("imap", "pop");
    		incomingUrl = scheme + "://" + host;
    		LogUtils.i("shihao","AccountSetupBasic::setInComingUrl  new incominturl =="+incomingUrl);  
    		mProvider.incomingUri = incomingUrl;
    		return true;
    	}catch(URISyntaxException e){
    		return false;
    	}		
    }
    
    public void setErrorMessage(String errorMsg){
    	setErrorMessage(errorMsg,MessagingException.NO_ERROR,false);
    }
    public void setErrorMessage(String errorMsg, int exceptionType , boolean isNeedOpenServer){
		mAuroraLoginView.dismissProgress();
        //if The email_address need to open Imap/pop3 server on the service
    	if(exceptionType == MessagingException.AUTHENTICATION_FAILED && isNeedOpenServer){
    		AuroraAlertDialog builder = new AuroraAlertDialog.Builder(this)
    		        .setTitle(R.string.aurora_login_error_title)
    				.setMessage(getString(R.string.aurora_login_error_messae,mSetupData.getAccount().mHostAuthRecv.mProtocol.toUpperCase()))
    				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("http://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=166"));//设置一个URI地址
							startActivity(intent);
							dialog.dismiss();
						}
					}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					}).create();
			builder.show();
    	}else{
    		mAuroraLoginView.setErrorMsg(errorMsg);
    	}
    }
  //Aurora <shihao> <2014-10-24> add for show ErrorMessage when Login failed end

    private void onEnableProceedButtons(boolean enabled) {
        mManualButton.setEnabled(enabled);
        mNextButton.setEnabled(enabled);
    }

    /**
     * Dialog fragment to show "setup note" dialog
     */
    public static class NoteDialogFragment extends DialogFragment {
        final static String TAG = "NoteDialogFragment";

        // Argument bundle keys
        private final static String BUNDLE_KEY_NOTE = "NoteDialogFragment.Note";

        // Public no-args constructor needed for fragment re-instantiation
        public NoteDialogFragment() {}

        /**
         * Create the dialog with parameters
         */
        public static NoteDialogFragment newInstance(String note) {
            final NoteDialogFragment f = new NoteDialogFragment();
            final Bundle b = new Bundle(1);
            b.putString(BUNDLE_KEY_NOTE, note);
            f.setArguments(b);
            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final String note = getArguments().getString(BUNDLE_KEY_NOTE);

            return new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(note)
                .setPositiveButton(
                        R.string.okay_action,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Activity a = getActivity();
                                if (a instanceof AccountSetupBasics) {
                                    ((AccountSetupBasics)a).finishAutoSetup();
                                }
                                dismiss();
                            }
                        })
                .setNegativeButton(
                        context.getString(R.string.cancel_action),
                        null)
                .create();
        }
    }
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mEmailConnectivityManager != null)
			mEmailConnectivityManager.unregister();
	}
}
