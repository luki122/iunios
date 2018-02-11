package com.android.email.activity.setup;

import java.io.IOException;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.TextView;
import android.widget.Toast;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;

import com.android.email.R;
import com.android.email.preferences.AuroraManualEditPreferences;
import com.android.email.preferences.AuroraTypePreferences;
import com.android.email.provider.AccountBackupRestore;
import com.android.email.service.EmailServiceUtils;
import com.android.email.service.EmailServiceUtils.EmailServiceInfo;
import com.android.email2.ui.MailActivityEmail;
import com.android.emailcommon.Logging;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.HostAuth;
import com.android.emailcommon.provider.EmailContent.AccountColumns;
import com.android.emailcommon.service.EmailServiceProxy;
import com.android.emailcommon.utility.Utility;
import com.android.emailcommon.mail.MessagingException;

import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraActionBar;

public class AuroraManualSettings extends AuroraPreferenceActivity
					implements OnSharedPreferenceChangeListener ,SetupData.SetupDataContainer,AccountCheckSettingsFragment.Callbacks
					             ,AuroraLastLoginSetup.onErrorMsmListener{	
	
	String mBaseScheme = "protocol";
	private static String IMAP ;
	private static String POP3 ;
	private final static String IMAP_PORT_NO = "143";
	private final static String IMAP_PORT_SSL = "993";
	private final static String POP_PORT_NO = "110";
	private final static String POP_PORT_SSL = "995";
	private final static String SMTP_PORT_NO = "25";
	private final static String SMTP_PORT_SSL = "465";
	
	private final static String AURORA_EMAIL_ACCOUNT = "aurora_email_account_a"; //邮箱帐号
	private final static String AURORA_SERVER_TYPE = "aurora_server_type";//服务器类型
	//收件服务器
	private final static String AURORA_RECEI_SERVER = "aurora_email_recei_server"; //server
	private final static String AURORA_RECEI_ACCOUNT = "aurora_email_recei_account"; //account
	private final static String AURORA_RECEI_PASSWORD = "aurora_email_recei_password"; //password
	private final static String AURORA_RECEI_PORT = "aurora_email_recei_port"; //port
	private final static String AURORA_RECEI_SSL = "aurora_email_recei_ssl";//ssl
	
	//发件服务器
	private final static String AURORA_SENT_SERVER = "aurora_email_sent_server"; //server
	private final static String AURORA_SENT_ACCOUNT = "aurora_email_sent_account"; //account
	private final static String AURORA_SENT_PASSWORD = "aurora_email_sent_password"; //password
	private final static String AURORA_SENT_PORT = "aurora_email_sent_port"; //port
	private final static String AURORA_SENT_SSL = "aurora_email_sent_ssl";//ssl
	

	//邮箱服务器
	private AuroraManualEditPreferences mEmailAccountPref;
	private AuroraTypePreferences mServerTypePref;
	
	//收件服务器
	private AuroraManualEditPreferences mReEditServerPref,mReEditAccountPref,mReEditPwPref,mReEditPortPref;
	private AuroraSwitchPreference mReSSLPreference;
	
	//发件服务器
	private AuroraManualEditPreferences mSeEditServerPref,mSeEditAccountPref,mSeEditPwPref,mSeEditPortPref;
	private AuroraSwitchPreference mSeSSLPreference;
	
	private SetupData mSetupData = null;
	private InputMethodManager mInputMethodManager;
	private AuroraLastLoginSetup mAuroraLastLoginSetup;
	
    public static void actionAccountManualSetup(Activity fromActivity, SetupData setupData) {
        final Intent i = new ForwardingIntent(fromActivity, AuroraManualSettings.class);
        i.putExtra(SetupData.EXTRA_SETUP_DATA, setupData);
        fromActivity.startActivity(i);
    }
    
	//Onclick Event
	@Override
	public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen,
			AuroraPreference preference) {
		// TODO Auto-generated method stub
		String key = preference.getKey();
		if(key.equalsIgnoreCase(AURORA_SERVER_TYPE)){
			View focusView = AuroraManualSettings.this.getCurrentFocus();
			if(mInputMethodManager.isActive() && focusView!=null)
				mInputMethodManager.hideSoftInputFromWindow(focusView.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
			showAuroraMenu();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        mSetupData = getIntent().getParcelableExtra(SetupData.EXTRA_SETUP_DATA);
        mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mAuroraLastLoginSetup = new AuroraLastLoginSetup(this);
        mAuroraLastLoginSetup.setErrorMsmListener(this);
        
		addPreferencesFromResource(R.xml.aurora_manual_login_preference);		
		setAuroraSystemMenuCallBack(auroraMenuCallBack);
		//this.setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		this.setAuroraMenuItems(R.xml.aurora_server_type_menu);
		initPreference();
	}

	private void initPreference() {
		// TODO Auto-generated method stub
		getAuroraActionBar().addItem(R.layout.aurora_manual_login_button,R.id.manual_login_bn);
		TextView loginView = (TextView)getAuroraActionBar().findViewById(R.id.manual_login_bn);
		loginView.setOnClickListener(myClickListener);
		

		
		IMAP = getString(R.string.imap_name);
		POP3 = getString(R.string.pop3_name);
		
		mServerTypePref = (AuroraTypePreferences)findPreference(AURORA_SERVER_TYPE);		
		mEmailAccountPref = (AuroraManualEditPreferences)findPreference(AURORA_EMAIL_ACCOUNT);
		
		mReEditServerPref = (AuroraManualEditPreferences)findPreference(AURORA_RECEI_SERVER);
		mReEditAccountPref = (AuroraManualEditPreferences) findPreference(AURORA_RECEI_ACCOUNT);
		mReEditPwPref = (AuroraManualEditPreferences)findPreference(AURORA_RECEI_PASSWORD);
		mReEditPortPref = (AuroraManualEditPreferences)findPreference(AURORA_RECEI_PORT);
		mReSSLPreference = (AuroraSwitchPreference)findPreference(AURORA_RECEI_SSL);
		
		mSeEditServerPref = (AuroraManualEditPreferences)findPreference(AURORA_SENT_SERVER);
		mSeEditAccountPref = (AuroraManualEditPreferences)findPreference(AURORA_SENT_ACCOUNT);
		mSeEditPwPref = (AuroraManualEditPreferences)findPreference(AURORA_SENT_PASSWORD);
		mSeEditPortPref = (AuroraManualEditPreferences)findPreference(AURORA_SENT_PORT);
		mSeSSLPreference = (AuroraSwitchPreference)findPreference(AURORA_SENT_SSL);
				
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		String serverType = mServerTypePref.getServerType();
		final Account account = mSetupData.getAccount();
		
		//see as AccountSetupType.class
		HostAuth recvAuth = account.getOrCreateHostAuthRecv(this);
        recvAuth.setConnection(serverType.toLowerCase(), recvAuth.mAddress, recvAuth.mPort, recvAuth.mFlags);
        Log.e("shihao","recvAuth.mProtocol =="+recvAuth.mProtocol);
        EmailServiceInfo info = EmailServiceUtils.getServiceInfo(this, recvAuth.mProtocol);
        if (info.usesAutodiscover) {
            mSetupData.setCheckSettingsMode(SetupData.CHECK_AUTODISCOVER);
        } else {
            mSetupData.setCheckSettingsMode(
                    SetupData.CHECK_INCOMING | (info.usesSmtp ? SetupData.CHECK_OUTGOING : 0));
        }
        recvAuth.mLogin = recvAuth.mLogin + "@" + recvAuth.mAddress;
        String prefix = info.inferPrefix;
        if (prefix != null && !recvAuth.mAddress.startsWith(prefix + ".")) {
        	recvAuth.mAddress = prefix + "." + recvAuth.mAddress;
        }
        AccountSetupBasics.setDefaultsForProtocol(this, account);
      
        //send
        HostAuth sendAuth = account.getOrCreateHostAuthSend(this);
        String hostName = AccountSettingsUtils.inferServerName(this, recvAuth.mAddress, null, "smtp");
        sendAuth.setLogin(recvAuth.mLogin, recvAuth.mPassword);
        sendAuth.setConnection(sendAuth.mProtocol, hostName, sendAuth.mPort, sendAuth.mFlags);
        
        //set
        setDate(account, recvAuth, sendAuth, serverType);
	}
	
	//if ServerType is changed
	private void changeDataForServerType(){
		String serverType =  mServerTypePref.getServerType();
		final Account account = mSetupData.getAccount();
		HostAuth recvAuth = account.getOrCreateHostAuthRecv(this);
		recvAuth.mProtocol = serverType.toLowerCase();
		
        EmailServiceInfo info = EmailServiceUtils.getServiceInfo(this, recvAuth.mProtocol);
        if (info.usesAutodiscover) {
            mSetupData.setCheckSettingsMode(SetupData.CHECK_AUTODISCOVER);
        } else {
            mSetupData.setCheckSettingsMode(
                    SetupData.CHECK_INCOMING | (info.usesSmtp ? SetupData.CHECK_OUTGOING : 0));
        }
        
        String prefix = info.inferPrefix;
        if (prefix != null && !recvAuth.mAddress.startsWith(prefix + ".")) {
        	int i = recvAuth.mAddress.indexOf(".");
        	recvAuth.mAddress = recvAuth.mAddress.substring(i);
        	Log.i("shihao","recv.address =="+recvAuth.mAddress);
        	recvAuth.mAddress = prefix + recvAuth.mAddress;
        }
        recvAuth.setConnection(serverType.toLowerCase(), recvAuth.mAddress, recvAuth.mPort, recvAuth.mFlags);
        HostAuth sendAuth = account.getOrCreateHostAuthSend(this);
        setDate(account, recvAuth, sendAuth, serverType);
	}
	
	private void setDate(Account account,HostAuth recvAuth,HostAuth sendAuth,String serverType){
		mSeSSLPreference.setChecked(true); 	
		mReSSLPreference.setChecked(true);
		
        //Email_account
        mEmailAccountPref.setEditText(account.mEmailAddress);
        
        //Receive
        mReEditServerPref.setEditText(recvAuth.mAddress);
        mReEditAccountPref.setEditText(recvAuth.mLogin);
        mReEditPwPref.setEditText(recvAuth.mPassword);
        setReServerPort(serverType);
        
        //Sent
        mSeEditServerPref.setEditText(sendAuth.mAddress);
        mSeEditAccountPref.setEditText(sendAuth.mLogin);
        mSeEditPwPref.setEditText(sendAuth.mPassword);
        setSeServerPort();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(
                outState);
        outState.putParcelable(SetupData.EXTRA_SETUP_DATA, mSetupData);
    }	
    
    private void setReServerPort(String serverType){
        if(mReSSLPreference.isChecked())
        	mReEditPortPref.setEditText(serverType.equalsIgnoreCase(IMAP) ? IMAP_PORT_SSL : POP_PORT_SSL);
        else
        	mReEditPortPref.setEditText(serverType.equalsIgnoreCase(IMAP) ? IMAP_PORT_NO : POP_PORT_NO);
    }
    
    private void setSeServerPort(){
        if(mSeSSLPreference.isChecked())
        	mSeEditPortPref.setEditText(SMTP_PORT_SSL);
        else
        	mSeEditPortPref.setEditText(SMTP_PORT_NO);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		// TODO Auto-generated method stub
		if(key.equalsIgnoreCase(AURORA_RECEI_SSL)){
			String serverType = mServerTypePref.getServerType();
			setReServerPort(serverType);
		}else if(key.equalsIgnoreCase(AURORA_SENT_SSL)){
			setSeServerPort();
		}
	}
	
	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.aurora_server_type_imap:
				if(! mServerTypePref.getServerType().equalsIgnoreCase(IMAP)){
					mServerTypePref.setServerType(IMAP);
					changeDataForServerType();
				}
				break;
			case R.id.aurora_server_type_pop:
				if(!mServerTypePref.getServerType().equalsIgnoreCase(POP3)){
					mServerTypePref.setServerType(POP3);
					changeDataForServerType();
				}
				break;
			default:
				break;
			}
		}
	};
	
	private OnClickListener myClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(isInfoComplete()){
				View focusView = AuroraManualSettings.this.getCurrentFocus();
				if(mInputMethodManager.isActive() && focusView!=null)
					mInputMethodManager.hideSoftInputFromWindow(focusView.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
				beReadyToLogin();
				toLogin();
			}else{
				Toast.makeText(AuroraManualSettings.this, R.string.aurora_manual_login_info_error, Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private boolean isInfoComplete(){
		return (!TextUtils.isEmpty(mEmailAccountPref.getEditText()) && !TextUtils.isEmpty(mReEditServerPref.getEditText()) &&
				!TextUtils.isEmpty(mReEditAccountPref.getEditText()) && !TextUtils.isEmpty(mReEditPwPref.getEditText()) &&
				!TextUtils.isEmpty(mReEditPortPref.getEditText()) && !TextUtils.isEmpty(mSeEditServerPref.getEditText()) &&
				!TextUtils.isEmpty(mSeEditAccountPref.getEditText()) && !TextUtils.isEmpty(mSeEditPwPref.getEditText()) &&
				!TextUtils.isEmpty(mSeEditPortPref.getEditText()));
	}
	
	//Ready to login
	private void beReadyToLogin(){
		final Account account = mSetupData.getAccount();
		mBaseScheme = account.mHostAuthRecv.mProtocol;
		
		//Incoming
		final HostAuth recvAuth = account.getOrCreateHostAuthRecv(this);
		final String userName = mReEditAccountPref.getEditText().toString().trim();
        final String userPassword = mReEditPwPref.getEditText().toString().trim();
        recvAuth.setLogin(userName, userPassword);
        
        final String serverAddress = mReEditServerPref.getEditText().toString().trim();
        int serverPort = Integer.parseInt(mReEditPortPref.getEditText().toString().trim());
        int securityType = mReSSLPreference.isChecked() ? HostAuth.FLAG_SSL:HostAuth.FLAG_NONE;

        recvAuth.setConnection(mBaseScheme, serverAddress, serverPort, securityType);

        //Outgoing
        final HostAuth sendAuth = account.getOrCreateHostAuthSend(this);
        final String userNameSend = mSeEditAccountPref.getEditText().toString().trim();
        final String userPasswordSend = mSeEditPwPref.getEditText().toString();
        sendAuth.setLogin(userNameSend, userPasswordSend);
        
        final String serverAddressSend = mSeEditServerPref.getEditText().toString().trim();
        int serverPortSend = Integer.parseInt(mSeEditPortPref.getEditText().toString().trim());
        int securityTypeSend = mSeSSLPreference.isChecked() ? HostAuth.FLAG_SSL:HostAuth.FLAG_NONE;

        sendAuth.setConnection(HostAuth.LEGACY_SCHEME_SMTP, serverAddressSend, serverPortSend, securityTypeSend);
	}
	
    protected void toLogin() {
		// TODO Auto-generated method stub
    	final AccountCheckSettingsFragment checkerFragment =
                AccountCheckSettingsFragment.newInstance(
                    SetupData.CHECK_INCOMING | SetupData.CHECK_OUTGOING, null,true);
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(checkerFragment, AccountCheckSettingsFragment.TAG);
            transaction.addToBackStack("back");
            transaction.commit();
	}

	@Override
    public SetupData getSetupData() {
        return mSetupData;
    }

    @Override
    public void setSetupData(SetupData setupData) {
        mSetupData = setupData;
    }
    
    @Override
    public void onCheckSettingsComplete(int result, SetupData setupData) {
        mSetupData = setupData;
        if (result == AccountCheckSettingsFragment.CHECK_SETTINGS_OK) {
        	mAuroraLastLoginSetup.toConfigureAndCommitAccount(mSetupData);
        }
    }

    @Override
    public void onAutoDiscoverComplete(int result, SetupData setupData) {
        throw new IllegalStateException();
    }
    
    @Override
    public void onSendErrorMessage(String errorMessage,MessagingException ex){
    }
    
    public void onErrorMessageReciver(String error){
    	Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    };
}
