package com.android.contacts.activities;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.list.AccountFilterActivity;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.vcard.ExportVCardActivity;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.preference.AuroraPreferenceManager;
import aurora.preference.AuroraPreferenceGroup;

import com.android.contacts.activities.AuroraContactsAccountsActivity;

public class AuroraContactsSetting extends AuroraPreferenceActivity 
        implements AuroraPreference.OnPreferenceClickListener{
    
    private AuroraPreference mContactAccounts;
    private AuroraPreference mImportExport;
    private AuroraPreference mMergeContacts;
    private boolean flag=true;
    private AuroraPreference mSimContacts;
    private AuroraPreference mSimContacts02;
    
    private static final int CONTACT_ACCOUNTS = 0;
    private boolean mGotoAccountSetting = false;
    private final int NEED_FINISH = 1;
    
    public static boolean mContactsAccountsChange = false;
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        getAuroraActionBar().setTitle(R.string.aurora_contacts_setting);
        addPreferencesFromResource(R.xml.aurora_preferrence_setting);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mGotoAccountSetting = extras.getBoolean("goto_account_setting", false);
            if (mGotoAccountSetting) {
                final Intent intent = new Intent(this, AuroraContactsAccountsActivity.class);
                startActivityForResult(intent, CONTACT_ACCOUNTS);
            }
        }
        
        mContactAccounts = (AuroraPreference) findPreference("pref_key_accounts");
        mImportExport = (AuroraPreference) findPreference("pref_key_import_export");
        mMergeContacts = (AuroraPreference) findPreference("pref_key_merge_contact");
        mContactAccounts.setOnPreferenceClickListener(this);
        mImportExport.setOnPreferenceClickListener(this);
        mMergeContacts.setOnPreferenceClickListener(this);
        
        mSimContacts = (AuroraPreference) findPreference("pref_key_sim_contact");
        mSimContacts.setOnPreferenceClickListener(this);
        mSimContacts02 = (AuroraPreference) findPreference("pref_key_sim_contact_02");
        mSimContacts02.setOnPreferenceClickListener(this);
        
        if (ContactsUtils.mIsIUNIDeviceOnly) {
            ((AuroraPreferenceGroup)(getPreferenceScreen().getPreference(0))).removePreference(mSimContacts);
            ((AuroraPreferenceGroup)(getPreferenceScreen().getPreference(0))).removePreference(mSimContacts02);
        } else {
        	if (FeatureOption.MTK_GEMINI_SUPPORT) {
        		mSimContacts.setTitle(R.string.aurora_menu_sim1_contacts);
        		if (!SimCardUtils.isSimInserted(0)) {
            		mSimContacts.setEnabled(false);
            		mSimContacts.setSelectable(false);
            	}
        		
        		if (!SimCardUtils.isSimInserted(1)) {
            		mSimContacts.setEnabled(false);
            		mSimContacts.setSelectable(false);
            	}
        	} else {
        		if (!SimCardUtils.isSimInserted(0)) {
            		mSimContacts.setEnabled(false);
            		mSimContacts.setSelectable(false);
            	}
        		
        		((AuroraPreferenceGroup)(getPreferenceScreen().getPreference(0))).removePreference(mSimContacts02);
        	}
        }
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

	    switch(requestCode) {
	    case CONTACT_ACCOUNTS: {
	        final Intent intent = new Intent();
            ContactListFilter filter = ContactListFilter.createFilterWithType(
                    ContactListFilter.FILTER_TYPE_CUSTOM);
            
            SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
            int filterInt = mPrefs.getInt("filter.type", -1);
            if (!mContactsAccountsChange && filterInt != -3) {
                filter = new ContactListFilter(ContactListFilter.FILTER_TYPE_ACCOUNT,
                        AccountType.ACCOUNT_TYPE_LOCAL_PHONE, AccountType.ACCOUNT_NAME_LOCAL_PHONE,
                        null, null);
            }
            intent.putExtra(AccountFilterActivity.KEY_EXTRA_CONTACT_LIST_FILTER, filter);
            setResult(Activity.RESULT_OK, intent);
            if (mGotoAccountSetting) {
                finish();
            }
            break;
	    }
	    
	    case NEED_FINISH: {
	      if(resultCode==Activity.RESULT_OK)
	           finish();
	        break;
	    }
	    }
	    
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		flag=true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public boolean onPreferenceClick(AuroraPreference mPreference) {
		String key = mPreference.getKey();
		if (key != null) {
			if (key.equals("pref_key_merge_contact")) {
				if(flag){
					flag=false;
					Intent intent = new Intent(this, MergableQueryActivity.class);
			    	startActivity(intent);
					return true;
				}
			} else if (key.equals("pref_key_sim_contact")) {
				Intent intent = new Intent(this, AuroraSimContactListActivity_3rd.class);
				intent.putExtra("slot", 0);
		    	startActivity(intent);
				return true;
			} else if (key.equals("pref_key_sim_contact_02")) {
				Intent intent = new Intent(this, AuroraSimContactListActivity_3rd.class);
				intent.putExtra("slot", 1);
		    	startActivity(intent);
				return true;
			}
		}
		return false;
	}

}
