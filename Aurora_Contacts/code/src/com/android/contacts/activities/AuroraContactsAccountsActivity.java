package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.AuroraAccountTypeManager;
import com.android.contacts.util.EmptyService;
import com.android.contacts.util.WeakAsyncTask;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.model.AccountWithDataSetEx;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.widget.Toast;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreferenceFragment;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceManager;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.Settings;

public class AuroraContactsAccountsActivity extends AuroraPreferenceActivity
        implements OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "AuroraContactsAccountsActivity";
    private AuroraPreferenceScreen accountListPreference;
    private String mPhoneAccountStr;
    private String mSimAccountStr;
    private String mSim1AccountStr;
    private String mSim2AccountStr;
    private Context mContext;
    private Set<String> mSettingsAccount = new HashSet<String>();
    private Set<String> mSettingsAccountUngroup = new HashSet<String>();
    
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mContext = this;
        mPhoneAccountStr = getString(R.string.aurora_contacts_account_phone);
        mSimAccountStr = getString(R.string.aurora_menu_sim_contacts);
        mSim1AccountStr = getString(R.string.aurora_menu_sim1_contacts);
        mSim2AccountStr = getString(R.string.aurora_menu_sim2_contacts);
        
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.aurora_contacts_account_act);
        accountListPreference = (AuroraPreferenceScreen) findPreference("account_pre");
        
        new Thread( new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                querySetting();
                showAccounts(mContext);
            }
        }).start();
    }
    
    private void querySetting() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    Settings.CONTENT_URI,
                    new String[] {Settings.ACCOUNT_NAME, Settings.ACCOUNT_TYPE, Settings.DATA_SET, Settings.UNGROUPED_VISIBLE}, 
                    null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        mSettingsAccount.add(cursor.getString(0) + "þ" + cursor.getString(1) + "þ" + cursor.getString(2));
                        if (cursor.getInt(3) == 1) {
                            mSettingsAccountUngroup.add(cursor.getString(0) + "þ" + cursor.getString(1) + "þ" + cursor.getString(2));
                        }
                    } while (cursor.moveToNext());
                }
                Log.d(TAG, "mSettingsAccount = " + mSettingsAccount);
                Log.d(TAG, "mSettingsAccountUngroup = " + mSettingsAccountUngroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
    }

    private void showAccounts(Context context) {
        final AuroraAccountTypeManager am = new AuroraAccountTypeManager();
        List<AccountWithDataSet> accounts = am.getAccounts(context);
        Log.d(TAG, "accounts.size() = " + accounts.size());
        
        for (AccountWithDataSet account : accounts) {
            Log.d(TAG, "account = " + account);

            AuroraSwitchPreference item = null;
            item = new AuroraSwitchPreference(context);
            String key = account.name + "þ" + account.type + "þ" + account.dataSet;
            item.setKey(key);
            if (account.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                item.setTitle(mPhoneAccountStr);
                item.setChecked(true);
            } else if (account.type.equals(AccountType.ACCOUNT_TYPE_SIM)
                    || account.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    if(account instanceof AccountWithDataSetEx){
                        int slotId = ((AccountWithDataSetEx) account).getSlotId();
                        if (slotId == 0) {
                            item.setTitle(mSim1AccountStr);
                        } else if (slotId == 1) {
                            item.setTitle(mSim2AccountStr);
                        } else {
                            item.setTitle(mSimAccountStr);
                        }
                    }
                } else {
                    item.setTitle(mSimAccountStr);
                }
            } else {
                item.setTitle(account.name);
            }
            
            if (!mSettingsAccount.contains(key)) {
                item.setPersistent(false);
                item.setChecked(false);
            } else {
                item.setPersistent(true);
            }
            
            if (accountListPreference != null) {
                accountListPreference.addPreference(item);
            }

            item.setOnPreferenceClickListener(this);
            item.setOnPreferenceChangeListener(this);
            if (AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(account.type)) {
                mPrefs.edit().putBoolean("local_phone_filter_trun_on", item.isChecked()).apply();
            }
            
            item.setPersistent(true);
            Log.d(TAG, "isChecked() = " + item.isChecked());
        }
    }

    @Override
    public boolean onPreferenceChange(AuroraPreference preference,
            Object newValue) {
        Log.d(TAG, "newValue = " + newValue);
        AuroraContactsSetting.mContactsAccountsChange = true;
        int vis = 1;
        if (!Boolean.valueOf(newValue.toString())) {
            vis = 0;
        }
        final int fVis = vis;

        try {
            String key = preference.getKey();
            final String[] keys = key.split("þ");
            if (keys[0].equals(AccountType.ACCOUNT_NAME_LOCAL_PHONE) 
                    && keys[1].equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                mPrefs.edit().putBoolean("local_phone_filter_trun_on", Boolean.valueOf(newValue.toString())).commit();
            }
            
            Log.d(TAG, "onPreferenceChange  mSettingsAccount.contains(key) = " + mSettingsAccount.contains(key));
            if (mSettingsAccount.contains(key)) {
                ContentValues values = new ContentValues();
                values.put(Settings.UNGROUPED_VISIBLE, fVis);
                ContentProviderOperation co = ContentProviderOperation
                        .newUpdate(Settings.CONTENT_URI)
                        .withSelection(Settings.ACCOUNT_NAME + "='" + keys[0] + "'",
                                null).withValues(values).build();
                final ArrayList<ContentProviderOperation> diff = new ArrayList<ContentProviderOperation>();
                diff.add(co);
                new UpdateTask(this).execute(diff);
            } else  if (!keys[0].equals(AccountType.ACCOUNT_NAME_LOCAL_PHONE) 
                    && !keys[1].equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                new Thread(){
                    public void run() {
                        try {
                            ContentValues v = new ContentValues();
                            v.put(Settings.ACCOUNT_NAME, keys[0]);
                            v.put(Settings.ACCOUNT_TYPE, keys[1]);
                            v.put(Settings.UNGROUPED_VISIBLE, fVis);
                            v.put(Settings.SHOULD_SYNC, 1);
                            
                            mContext.getContentResolver().insert(Settings.CONTENT_URI, v);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            new Thread(){
                public void run() {
                    ContentValues groupValues = new ContentValues();
                    groupValues.put("group_visible", fVis);
                    mContext.getContentResolver().update(
                            addCallerIsSyncAdapterParameter(Groups.CONTENT_URI),
                            groupValues, 
                            Settings.ACCOUNT_NAME + "='" + keys[0] 
                                    + "' and " + Settings.ACCOUNT_TYPE + "='" + keys[1] + "'", null);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
        
        Log.d(TAG, "changed before filter.type = " + mPrefs.getInt("filter.type", -1));
        ContactListFilter filter = ContactListFilter.createFilterWithType(
                ContactListFilter.FILTER_TYPE_CUSTOM);
        mPrefs.edit().putInt("filter.type", filter == null ? ContactListFilter.FILTER_TYPE_DEFAULT : filter.filterType).apply();
        
        Log.d(TAG, "changed after filter.type = " + mPrefs.getInt("filter.type", -1));
        mContext.sendBroadcast(new Intent("com.aurora.change.contacts.account"));
        
        return true;
    }

    @Override
    public boolean onPreferenceClick(AuroraPreference preference) {
        return true;
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(GnContactsContract.CALLER_IS_SYNCADAPTER,
                        "true").build();
    }

    public static class UpdateTask
            extends WeakAsyncTask<ArrayList<ContentProviderOperation>, Void, Void, Activity> {

        public UpdateTask(Activity target) {
            super(target);
        }

        /** {@inheritDoc} */
        @Override
        protected void onPreExecute(Activity target) {
            final Context context = target;
            
            context.startService(new Intent(context, EmptyService.class));
        }

        /** {@inheritDoc} */
        @Override
        protected Void doInBackground(Activity target,
                ArrayList<ContentProviderOperation>... params) {
            final Context context = target;
            final ContentValues values = new ContentValues();
            final ContentResolver resolver = context.getContentResolver();

            try {
                final ArrayList<ContentProviderOperation> diff = params[0];
                Log.d(TAG, "doInBackground(), before applyBatch() ****");
                resolver.applyBatch(GnContactsContract.AUTHORITY, diff);
            } catch (RemoteException e) {
                Log.e(TAG, "Problem saving display groups", e);
            } catch (OperationApplicationException e) {
                Log.e(TAG, "Problem saving display groups", e);
            }

            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(Activity target, Void result) {
            final Context context = target;

            // Stop the service that was protecting us
            context.stopService(new Intent(context, EmptyService.class));
        }
    }
}
