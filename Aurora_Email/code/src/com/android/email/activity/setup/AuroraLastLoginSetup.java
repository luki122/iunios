package com.android.email.activity.setup;

import java.io.IOException;
import com.android.email.R;
import com.android.email.provider.AccountBackupRestore;
import com.android.email.service.EmailServiceUtils;
import com.android.email.service.EmailServiceUtils.EmailServiceInfo;
import com.android.email2.ui.MailActivityEmail;
import com.android.emailcommon.Logging;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent.AccountColumns;
import com.android.emailcommon.service.EmailServiceProxy;
import com.android.emailcommon.utility.Utility;
import com.android.mail.utils.LogUtils;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

public class AuroraLastLoginSetup {
	private Activity activity;
	private SetupData mSetupData;
	private onErrorMsmListener mOnErrorMsmListener = null;
	private String commitErrorMessage;
	
	public AuroraLastLoginSetup(Activity activity){
		this.activity = activity;
	}
	
	public void setErrorMsmListener(onErrorMsmListener listener){
		this.mOnErrorMsmListener = listener;
	}
	
	public interface onErrorMsmListener{
		public void onErrorMessageReciver(String error);
	}
	
	
    /** 
     * Aurora <shihao> <2014-10-25> for go into mailbox directly after confirmation server begin
     * 
     * See: AccountSetupOptions.java && AccountSetupNames.java
     */
	public void toConfigureAndCommitAccount(SetupData setupData){
		mSetupData = setupData;
    	final Account account = mSetupData.getAccount();
        if (account.isSaved()) {
            // Disrupting the normal flow could get us here, but if the account is already
            // saved, we've done this work
            return;
        } else if (account == null) {
            throw new IllegalStateException("AuroraLastLoginSetup unexpected null account");
        } 
        else if (account.mHostAuthRecv == null) {
            throw new IllegalStateException("in AuroraLastLoginSetup with null mHostAuthRecv");
        }
    	EmailServiceInfo mServiceInfo = EmailServiceUtils.getServiceInfo(activity.getApplicationContext(),
                 account.mHostAuthRecv.mProtocol); 
    	
    	account.setDisplayName(account.getEmailAddress());
        int newFlags = account.getFlags() & ~(Account.FLAGS_BACKGROUND_ATTACHMENTS);
        if (mServiceInfo.offerAttachmentPreload) {
            newFlags |= Account.FLAGS_BACKGROUND_ATTACHMENTS;
        }
        account.setFlags(newFlags);
        
        // Finish setting up the account, and commit it to the database
        // Set the incomplete flag here to avoid reconciliation issues in ExchangeService
        account.mFlags |= Account.FLAGS_INCOMPLETE;
        if (mSetupData.getPolicy() != null) {
            account.mFlags |= Account.FLAGS_SECURITY_HOLD;
            account.mPolicy = mSetupData.getPolicy();
        }
        
        // Finally, write the completed account (for the first time) and then
        // install it into the Account manager as well.  These are done off-thread.
        // The account manager will report back via the callback, which will take us to
        // the next operations.
        final boolean email = true;
        final boolean calendar = mServiceInfo.syncCalendar;
        final boolean contacts = mServiceInfo.syncContacts;

        Utility.runAsync(new Runnable() {
            @Override
            public void run() {
                final Context context = activity;
                AccountSettingsUtils.commitSettings(context, account);
                EmailServiceUtils.setupAccountManagerAccount(context, account,
                        email, calendar, contacts, mAccountManagerCallback);

            }
        });
    }
    
    /**
     * This is called at the completion of MailService.setupAccountManagerAccount()
     */
    AccountManagerCallback<Bundle> mAccountManagerCallback = new AccountManagerCallback<Bundle>() {
        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                // Block until the operation completes
//               	LogUtils.i("test","AccoutnSetupActivtiuy mAccountManagerCallback");
                future.getResult();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {                   	
                        optionsComplete();
                    }
                });
                return;
            } catch (OperationCanceledException e) {
            	LogUtils.d(Logging.LOG_TAG, "addAccount was canceled");
            } catch (IOException e) {
            	LogUtils.d(Logging.LOG_TAG, "addAccount failed: " + e);
            } catch (AuthenticatorException e) {
            	LogUtils.d(Logging.LOG_TAG, "addAccount failed: " + e);
            }
            commitErrorMessage = activity.getString(R.string.account_setup_failed_dlg_auth_message,
                    R.string.system_account_create_failed);
            if(mOnErrorMsmListener != null){
            	mOnErrorMsmListener.onErrorMessageReciver(commitErrorMessage);
            }
        }
    };
    
    /**
     * This is called after the account manager creates the new account.
     */
    private void optionsComplete() {
        // If the account manager initiated the creation, report success at this point
    	LogUtils.i("test","AuroraLastLoginSetup optionsComplete");
        final AccountAuthenticatorResponse authenticatorResponse =
            mSetupData.getAccountAuthenticatorResponse();
        if (authenticatorResponse != null) {
            authenticatorResponse.onResult(null);
            mSetupData.setAccountAuthenticatorResponse(null);
        }
        // Now that AccountManager account creation is complete, clear the INCOMPLETE flag
        final Account account = mSetupData.getAccount();
        account.mFlags &= ~Account.FLAGS_INCOMPLETE;

        // If we've got policies for this account, ask the user to accept.
        if ((account.mFlags & Account.FLAGS_SECURITY_HOLD) != 0) {
            account.mFlags &= ~Account.FLAGS_SECURITY_HOLD;
        }
        AccountSettingsUtils.commitSettings(activity, account);
        
        saveAccountAndFinish();
    }
    
    /**
     * These are the final cleanup steps when creating an account:
     *  Clear incomplete & security hold flags
     *  Update account in DB
     *  Enable email services
     *  Enable exchange services
     *  Move to final setup screen
     */
    private void saveAccountAndFinish() {   	
    	LogUtils.i("test","AuroraLastLoginSetup saveAccountAndFinish");
        // Clear the security hold flag now
        final Account account = mSetupData.getAccount();
        
        // Start up services based on new account(s)
        MailActivityEmail.setServicesEnabledSync(activity);
        EmailServiceUtils.startService(activity, account.mHostAuthRecv.mProtocol);
        
        // Update the folder list (to get our starting folders, e.g. Inbox)
        final EmailServiceProxy proxy = EmailServiceUtils.getServiceForAccount(activity, account.mId);
        try {
            proxy.updateFolderList(account.mId);
        } catch (RemoteException e) {
            // It's all good
        }
        
        setupAccountNames();
    }
    
    //See as AccountSetupNames.java
    /**
     * Final account setup work is handled in this AsyncTask:
     *   Commit final values to provider
     *   Trigger account backup
     *   Check for security hold
     *
     * When this completes, we return to UI thread for the following steps:
     *   If security hold, dispatch to AccountSecurity activity
     *   Otherwise, return to AccountSetupBasics for conclusion.
     *
     * TODO: If there was *any* indication that security might be required, we could at least
     * force the DeviceAdmin activation step, without waiting for the initial sync/handshake
     * to fail.
     * TODO: If the user doesn't update the security, don't go to the MessageList.
     */
    public void setupAccountNames(){
    	LogUtils.i("test","AuroraLastLoginSetup setupAccountNames");
        final Account mAccount = mSetupData.getAccount();
      
        final ContentValues cv = new ContentValues();
        cv.put(AccountColumns.DISPLAY_NAME, mAccount.getDisplayName());
        cv.put(AccountColumns.SENDER_NAME, mAccount.getSenderName());
        mAccount.update(activity, cv);

        // Update the backup (side copy) of the accounts
        AccountBackupRestore.backup(activity);
        
        finishActivity();
    }
    
    private void finishActivity() {
    	 LogUtils.w("test","AuroraLastLoginSetup:: finishActivity");
         
        final AccountAuthenticatorResponse authenticatorResponse =
                mSetupData.getAccountAuthenticatorResponse();
        if (authenticatorResponse != null) {
            authenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            mSetupData.setAccountAuthenticatorResponse(null);
        }

        if (mSetupData.getFlowMode() == SetupData.FLOW_MODE_NO_ACCOUNTS) {
            AccountSetupBasics.actionAccountCreateFinishedWithResult(activity);
        } else if (mSetupData.getFlowMode() != SetupData.FLOW_MODE_NORMAL) {
            AccountSetupBasics.actionAccountCreateFinishedAccountFlow(activity);
        } else {
            final Account account = mSetupData.getAccount();
            if (account != null) {
                AccountSetupBasics.actionAccountCreateFinished(activity, account);
            }
        }
    }
}
