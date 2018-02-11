/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mediatek.contacts.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import android.content.AsyncTaskLoader;

import com.mediatek.contacts.list.ContactsIntentResolverEx;
import com.android.contacts.list.ContactListFilterView;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.CustomContactListFilterActivity;
import android.content.Loader;
import android.accounts.Account;
import android.app.LoaderManager.LoaderCallbacks;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.AccountSelectionUtil;
import com.mediatek.contacts.interactions.ImportExportDialogFragmentEx;
import com.mediatek.contacts.util.ContactsIntent;
import com.google.android.collect.Lists;

import android.app.ActionBar;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import aurora.widget.AuroraButton;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.CheckedTextView;

//The following lines are provided and maintained by Mediatek Inc.
//import com.mediatek.CellConnService.CellConnMgr;
import com.gionee.CellConnService.GnCellConnMgr;
import gionee.provider.GnTelephony.SIMInfo;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
// Description: for SIM name display
import com.android.contacts.util.AccountFilterUtil;

import android.widget.LinearLayout;
// The previous lines are provided and maintained by Mediatek Inc.

//Gionee:wangth 20120413 add for CR00572577 && CR00624473 begin
import com.android.contacts.ContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.os.storage.StorageVolume;
import android.os.IBinder;
import android.os.RemoteException;
import gionee.os.storage.GnStorageManager;
//Gionee:wangth 20120413 add for CR00572577 && CR00624473 end

import aurora.app.AuroraActivity;


public class ContactImportExportActivity extends AuroraActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = ContactImportExportActivity.class.getSimpleName();
    /*
     * Bug Fix by Mediatek Begin.    
     *   CR ID: ALPS00110214
     */
    public static final int REQUEST_CODE = 111111;
    public static final int RESULT_CODE = 111112;
    /*
     * Bug Fix by Mediatek End.
     */
    
    private final int ACCOUNT_LOADER_ID = 0;
    
    private AuroraListView mListView;
    private String mSDCardString = "";
    private List<AccountWithDataSetEx> mAccounts = null;
    
    private int mShowingStep = 0;
    private int mCheckedPosition = 0;
    private boolean mIsFirstEntry = true;
    private AccountWithDataSetEx mCheckedAccount1 = null;
    private AccountWithDataSetEx mCheckedAccount2 = null;    
    private List<ListViewItemObject> mListItemObjectList = new ArrayList<ListViewItemObject>();
    private AccountListAdapter mAdapter = null;
    //Gionee:wangth 20120413 add for CR00572577 begin
    private String mPhoneString = null;
    //Gionee:wangth 20120413 add end
    //Gionee:wangth 20120616 add for CR00576392 && CR00624473 begin
    public static String mUSBStorageString = null;
    // Gionee:wangth 20121113 modify for CR00729290 begin
    /*
    public static final String mSDCard = "/mnt/sdcard";
    public static final String mSDCard2 = "/mnt/sdcard2";
    public static String mSdCard = "/mnt/sdcard";
    public static String mSdCard2 = "/mnt/sdcard2";
    */
    public static final String mSDCard = ContactsUtils.getRealSdPath(1);
    public static final String mSDCard2 = ContactsUtils.getRealSdPath(2);
    public static String mSdCard = ContactsUtils.getRealSdPath(1);
    public static String mSdCard2 = ContactsUtils.getRealSdPath(2);
    // Gionee:wangth 20121113 modify for CR00729290 end
    public static int mUSBStoragePostion = 0;
    public static int mSelectedStep1Postion = 0;
    public static int mSelectedStep2Postion = 0;
    static StorageManager mStorageManager;
    public static int mStorageCount = 1;
    public static String mSaveTime;
    //Gionee:wangth 20120616 add for CR00576392 && CR00624473 end
    
     private class ListViewItemObject {
        public AccountWithDataSetEx mAccount; 
        /*public CheckedTextView view;*/
        
        public ListViewItemObject(AccountWithDataSetEx account) {
            mAccount = account;
        }
        
        public String getName() {
            if (mAccount == null) {
                return mSDCardString;
            } else {
                String displayName = null;
                displayName = AccountFilterUtil.getAccountDisplayNameByAccount(mAccount.type, mAccount.name);
                if (null == displayName) {
                    return mAccount.name;
                } else {
                    return displayName;
                }
            }                
        }
        //Gionee:wangth 20120420 add for CR00576392 begin
        private String getName(int position) {
            if (mAccount == null) {
                if (position == (mListItemObjectList.size() - 1)) {
                    return mUSBStorageString;
                }
                return mSDCardString;
            } else {
                String displayName = null;
                displayName = AccountFilterUtil.getAccountDisplayNameByAccount(mAccount.type, mAccount.name);
                if (null == displayName) {
                    if (mAccount.type.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)) {
                        return mPhoneString;
                    } else {
                        return mAccount.name;
                    }
                } else {
                    return displayName;
                }
            }                
        }
        //Gionee:wangth 20120420 add for CR00576392 end
    }

     
    @Override
    protected void onCreate(Bundle icicle) {
        // gionee xuhz add for support gn theme start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_PeopleTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_PeopleTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_PeopleTheme_light);
        } 
        // gionee xuhz add for support gn theme start
        
        // Gionee:wangth 20120616 add for CR00624473 begin
        if (ContactsUtils.mIsGnContactsSupport) {
            setSDCardPatch(this);
        }
        // Gionee:wangth 20120616 add for CR00624473 begin
        super.onCreate(icicle);
        setContentView(R.layout.import_export_bridge_layout);
        mSDCardString = getResources().getString(R.string.imexport_bridge_sd_card);
        //Gionee:wangth 20120413 add for CR00572577 begin
        mPhoneString = getResources().getString(R.string.gn_phone_text);
        mUSBStorageString = getResources().getString(R.string.gn_usb_storage_text);
        //Gionee:wangth 20120413 add for CR00572577 end
        ((AuroraButton) findViewById(R.id.btn_action)).setOnClickListener(this);       
//        ((AuroraButton) findViewById(R.id.btn_back)).setOnClickListener(this);        
        /*
         * Bug Fix by Mediatek Begin.         
         *   CR ID: ALPS00235206
         */
        ((LinearLayout) findViewById(R.id.buttonbar_layout)).setVisibility(View.GONE); 
        /*
         * Bug Fix by Mediatek End.
         */
        
        mListView = (AuroraListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE, 
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);                   
            actionBar.setTitle(R.string.imexport_title); 
        }
        
        mAdapter = new AccountListAdapter(ContactImportExportActivity.this);
        getLoaderManager().initLoader(ACCOUNT_LOADER_ID, null, new MyLoaderCallbacks());

        // The following lines are provided and maintained by Mediatek inc.
        mCellMgr.register(this);
        // The following lines are provided and maintained by Mediatek inc.
    }

    private void setButtonState(boolean isTrue) {    
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     findViewById(R.id.btn_back).setEnabled(isTrue && (mShowingStep > 1));
         *   CR ID: ALPS00117312
         */

        /*// gionee xuhz 20120604 modify for CR00614790 start
        if (ContactsApplication.sIsGnContactsSupport) {
            findViewById(R.id.btn_back).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_back).setVisibility((isTrue && (mShowingStep > 1))? View.VISIBLE : View.GONE);
        }
        // gionee xuhz 20120604 modify for CR00614790 end*/
    	
         /*
         * Bug Fix by Mediatek End.
         */
        findViewById(R.id.btn_action).setEnabled(isTrue && (mShowingStep > 0));                             
    }
    
    private void setShowingStep(int showingStep) {
        mShowingStep = showingStep;
        mListItemObjectList.clear();
        /*
         * Bug Fix by Mediatek Begin.         
         *   CR ID: ALPS00235206
         */
        ((LinearLayout) findViewById(R.id.buttonbar_layout)).setVisibility(View.VISIBLE);
        /*
         * Bug Fix by Mediatek End.
         */
      
        if (mShowingStep == 1) {
            ((TextView) findViewById(R.id.tips)).setText(R.string.tips_source);
            for (AccountWithDataSetEx account : mAccounts) {
                mListItemObjectList.add(new ListViewItemObject(account));
            }           
        } else if (mShowingStep == 2) {
            ((TextView) findViewById(R.id.tips)).setText(R.string.tips_target);
            for (AccountWithDataSetEx account : mAccounts) {
                if (mCheckedAccount1 != account) {
                    if (mCheckedAccount1 == null) {  
                        //selected SD card,
                        if (account.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_SIM) || 
                            account.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_USIM)) {
                            continue;
                        }
                    }
                    mListItemObjectList.add(new ListViewItemObject(account));
                }
            }            
        }
        // Gionee baorui 2012-04-26 modify for CR00582548 begin
        if (ContactsUtils.mIsGnContactsSupport) {
            if (mShowingStep == 1 || (mCheckedAccount1 != null)) {
                // is 1 step or selected phone
                mListItemObjectList.add(new ListViewItemObject(null));
                // Gionee:wangth 20120420 add for CR00576392 begin
                //Gionee <wangth><2013-05-03> modify for CR00805658 begin
                if (ContactsUtils.mIsGnContactsSupport && isExSdcardInserted()) {
                    mListItemObjectList.add(new ListViewItemObject(null));
                }
                //Gionee <wangth><2013-05-03> modify for CR00805658 end
                // Gionee:wangth 20120420 add for CR00576392 end
            }
        }
        else
        {
            if (mShowingStep == 1 || (mCheckedAccount1 != null && 
                    !mCheckedAccount1.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_SIM) && 
                    !mCheckedAccount1.type.equalsIgnoreCase(AccountType.ACCOUNT_TYPE_USIM))) {  
                    //is 1 step or selected phone
                    mListItemObjectList.add(new ListViewItemObject(null));
                    // Gionee:wangth 20120420 add for CR00576392 begin
                    //Gionee <wangth><2013-05-03> modify for CR00805658 begin
                    if (ContactsUtils.mIsGnContactsSupport && isExSdcardInserted()) {
                        mListItemObjectList.add(new ListViewItemObject(null));
                    }
                    //Gionee <wangth><2013-05-03> modify for CR00805658 end
                    // Gionee:wangth 20120420 add for CR00576392 end
            }
        }
        // Gionee baorui 2012-04-26 modify for CR00582548 end
    }
    
    private static class AccountsLoader extends AsyncTaskLoader<List<AccountWithDataSetEx>> {
        private Context mContext;

        public AccountsLoader(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public List<AccountWithDataSetEx> loadInBackground() {
            return loadAccountFilters(mContext);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        protected void onReset() {
            onStopLoading();
        }
    }

    private void setCheckedPosition(int checkedPosition) {
        /*if (mCheckedPosition != checkedPosition) {
            setListViewItemChecked(mCheckedPosition, false);
            mCheckedPosition = checkedPosition;
            setListViewItemChecked(mCheckedPosition, true);
        }*/
    	
    	if (mCheckedPosition != checkedPosition) {
    		mCheckedPosition = checkedPosition;
    		mAdapter.notifyDataSetChanged();
    	}    		
    }
    
    private void setCheckedAccount(int position) {
        if (mShowingStep == 1) {
            mCheckedAccount1 = mListItemObjectList.get(position).mAccount;   
            //Gionee:wangth 20120420 add for CR00576392 begin
            if (ContactsUtils.mIsGnContactsSupport) {
                mSelectedStep1Postion = position;
            }
            //Gionee:wangth 20120420 add for CR00576392 end
        } else if (mShowingStep == 2) {
            // Gionee:wangth 20120809 modify for CR00672132 begin
            /*
            mCheckedAccount2 = mListItemObjectList.get(position).mAccount;
            */
            try {
                mCheckedAccount2 = mListItemObjectList.get(position).mAccount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Gionee:wangth 20120809 modify for CR00672132 end
            // The following lines are provided and maintained by Mediatek inc.
            mCheckedAccountEnd = mCheckedAccount2;
            // The following lines are provided and maintained by Mediatek inc.
            //Gionee:wangth 20120420 add for CR00576392 begin
            if (ContactsUtils.mIsGnContactsSupport) {
                mSelectedStep2Postion = position;
            }
            //Gionee:wangth 20120420 add for CR00576392 end
        }
        
    }
    
    /*private void setListViewItemChecked(int checkedPosition, boolean checked) {
        if (checkedPosition > -1) {
            // Gionee:wangth 20120824 modify for CR00680559 begin
            
            ListViewItemObject itemObj = mListItemObjectList.get(checkedPosition);
            if (itemObj.view != null) {
                itemObj.view.setChecked(checked);
            }
            
            ListViewItemObject itemObj = null;
            
            try {
                itemObj = mListItemObjectList.get(checkedPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (itemObj != null && itemObj.view != null) {
                itemObj.view.setChecked(checked);
            }
            // Gionee:wangth 20120824 modify for CR00680559 end
            //CheckedTextView view = (CheckedTextView) mListView.getChildAt(checkedPosition);
            //view.setChecked(checked);
        }
    }*/
    
    private static List<AccountWithDataSetEx> loadAccountFilters(Context context) {

        List<AccountWithDataSetEx> accountsEx = new ArrayList<AccountWithDataSetEx>();

        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(context);
        List<AccountWithDataSet> accounts = accountTypes.getAccounts(false);
        for (AccountWithDataSet account : accounts) {
            AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
            if (accountType.isExtension() && !account.hasData(context)) {
                // Hide extensions with no raw_contacts.
                continue;
            }

            int slot = 0;
            if (account instanceof AccountWithDataSetEx) {
                slot = ((AccountWithDataSetEx) account).getSlotId();
            }
            
            accountsEx.add(new AccountWithDataSetEx(account.name, account.type, slot));

        }

        return accountsEx;
    }

    private class MyLoaderCallbacks implements LoaderCallbacks<List<AccountWithDataSetEx>> {
        @Override
        public Loader<List<AccountWithDataSetEx>> onCreateLoader(int id, Bundle args) {
            return new AccountsLoader(ContactImportExportActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<AccountWithDataSetEx>> loader, List<AccountWithDataSetEx> data) {
            if (data == null) { // Just in case...
                Log.e(TAG, "Failed to load accounts");
                return;
            }
            if (mAccounts == null) {
                mAccounts = data;

                if (mShowingStep == 0) {
                    setShowingStep(1);
                } else {
                    setShowingStep(mShowingStep);
                }
                setCheckedAccount(mCheckedPosition);
                updateUi();  
            }
        }

        @Override
        public void onLoaderReset(Loader<List<AccountWithDataSetEx>> loader) {
        }
    }

    private class AccountListAdapter extends BaseAdapter {
        private final LayoutInflater mLayoutInflater;

        public AccountListAdapter(Context context) {
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mListItemObjectList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public AccountWithDataSetEx getItem(int position) { 
            return null; 
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final CheckedTextView view;
            if (convertView != null) {
                view = (CheckedTextView) convertView;
            } else {
                view = (CheckedTextView) mLayoutInflater.inflate(R.layout.simple_list_item_single_choice, parent, false);
            }

            ListViewItemObject itemObj = mListItemObjectList.get(position);  
            /*itemObj.view = view;*/
            //Gionee:wangth 20120420 modify for CR00576392 begin
            //view.setText(itemObj.getName()); 
            //String name = itemObj.getName();
            String name = null;
            if (ContactsUtils.mIsGnContactsSupport) {
                name = itemObj.getName(position);
                if (name.equals(mUSBStorageString)) {
                    mUSBStoragePostion = position;
                }
                // Gionee:wangth 20120824 add for CR00680559 begin
                if (getCount() <= mCheckedPosition) {
                    mCheckedPosition = 0;
                    setCheckedAccount(mCheckedPosition);
                }
                // Gionee:wangth 20120824 add for CR00680559 end
            } else {
                name = itemObj.getName();
            }
            view.setText(name);
            //Gionee:wangth 20120420 modify for CR00576392 end
            view.setChecked(mCheckedPosition == position);
           
            return view;
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {        
        setCheckedPosition(position);
        setCheckedAccount(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // gionee xuhz 20120604 modify for CR00614790 start
                if (ContactsApplication.sIsGnContactsSupport) {
                    gnOnBackPressed();
                    return true;
                } else {
                    finish();
                    return true;
                }
                // gionee xuhz 20120604 modify for CR00614790 end

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_action:
//            case R.id.btn_back:  
                int pos = 0;
                if (view.getId() == R.id.btn_action) {
                    if (mShowingStep >= 2) {
                        // The following lines are provided and maintained by Mediatek inc.
                        checkRaidoState2();
                        // handleImportExportAction();
                        // The following lines are provided and maintained by Mediatek inc.
                        return;
                    } 
                    setShowingStep(2);
                    if (mIsFirstEntry || (mCheckedAccount1 == null && mCheckedAccount2 == null)) {
                        pos = 0;
                    } else {
                        pos = getCheckedAccountPosition(mCheckedAccount2);
                    }
                    mIsFirstEntry = false;
                } else {
                    setShowingStep(1);    
                    pos = getCheckedAccountPosition(mCheckedAccount1);
                }
                // Gionee:wangth 20120807 modify for CR00667822 begin
                /*
                mCheckedPosition = pos;
                */
                if (ContactsUtils.mIsGnContactsSupport) {
                    mCheckedPosition = mSelectedStep2Postion;
                } else {
                    mCheckedPosition = pos;
                }
                // Gionee:wangth 20120807 modify for CR00667822 end
                setCheckedAccount(mCheckedPosition);
                updateUi();                
                break;
                
            default:
                break;
        }
    }
    
    private void updateUi() {
        setButtonState(true);
        mListView.setAdapter(mAdapter);
    }
    
    private int getCheckedAccountPosition(AccountWithDataSetEx checkedAccount) {
        for (int i=0; i<mListItemObjectList.size(); i++) {
            ListViewItemObject obj = mListItemObjectList.get(i);
            if (obj.mAccount == checkedAccount) {
                return i;
            }
        }
        return 0;
    }
    
    private void handleImportExportAction() {
        
        if (mCheckedAccount1 == null || mCheckedAccount2 == null) {
            if (!checkSDCardAvaliable()) {               
                //Gionee <xuhz> <2013-07-20> add for CR00824492 begin
            	if (ContactsApplication.sIsInternalSdcardOnly) {
            		new AuroraAlertDialog.Builder(this)
                    .setMessage(R.string.gn_no_sdcard_message)
                    .setTitle(R.string.gn_no_sdcard_title)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int which) {
                    		finish();
                        }
                     }).show();
                    return;
            	}
                //Gionee <xuhz> <2013-07-20> add for CR00824492 end
                new AuroraAlertDialog.Builder(this)
                .setMessage(R.string.no_sdcard_message)
                .setTitle(R.string.no_sdcard_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int which) {
                		finish();
                    }
                 }).show();
                return;
            } 
        }        
        
        if (mCheckedAccount1 == null) { // import from SDCard            
            if (mCheckedAccount2 != null) {
                AccountSelectionUtil.doImportFromSdCard(this, mCheckedAccount2);
            }
        } else {
            if (mCheckedAccount2 == null) { // export to SDCard
                if(isSDCardFull()){ //SD card is full
                    Log.i(TAG,"[handleImportExportAction] isSDCardFull");
                    new AuroraAlertDialog.Builder(this)
                    .setMessage(R.string.storage_full)
                    .setTitle(R.string.storage_full)
                    .setIcon(R.drawable.ic_dialog_alert_holo_light)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
                    return;
                }
                /*
                 * Bug Fix by Mediatek Begin.
                 *   Original Android's code:
                 *    startActivity(new Intent(this,
                      com.mediatek.contacts.list.ContactListMultiChoiceActivity.class).setAction(
                      ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS).putExtra("request_type",
                      ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER).putExtra(
                      "toSDCard", true).putExtra("fromaccount", mCheckedAccount1));
                 *   CR ID: ALPS00110214
                 */
                Intent intent = new Intent(this, com.mediatek.contacts.list.ContactListMultiChoiceActivity.class)
                .setAction(ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS)
                .putExtra("request_type", ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER)
                .putExtra("toSDCard", true)
                .putExtra("fromaccount", mCheckedAccount1);
                startActivityForResult(intent, ContactImportExportActivity.REQUEST_CODE);
                /*
                 * Bug Fix by Mediatek End.
                 */
            } else { // account to account
                /*
                 * Bug Fix by Mediatek Begin.
                 *   Original Android's code:
                      startActivity(new Intent(this,
                      com.mediatek.contacts.list.ContactListMultiChoiceActivity.class).setAction(
                      ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS).putExtra("request_type",
                      ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER).putExtra(
                      "toSDCard", false).putExtra("fromaccount", mCheckedAccount1)
                      .putExtra("toaccount", mCheckedAccount2));
                 *   CR ID: ALPS00110214
                 */
                Intent intent = new Intent(this,com.mediatek.contacts.list.ContactListMultiChoiceActivity.class)
                .setAction(ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS)
                .putExtra("request_type", ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER)
                .putExtra("toSDCard", false)
                .putExtra("fromaccount", mCheckedAccount1)
                .putExtra("toaccount", mCheckedAccount2);
                startActivityForResult(intent, ContactImportExportActivity.REQUEST_CODE);
                /*
                 * Bug Fix by Mediatek End.
                 */
            }
        } 
    }
    
    private boolean checkSDCardAvaliable() {
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     return (Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED));
         *   CR ID: ALPS00229364
         *   Descriptions: 
         */
        getExternalStorageDirectory();
        return (getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED));
        /*
         * Bug Fix by Mediatek End.
         */
        
    }

    private boolean isSDCardFull() {
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     String state = Environment.getExternalStorageState(); 
         *     if(Environment.MEDIA_MOUNTED.equals(state)) { 
                   File sdcardDir = Environment.getExternalStorageDirectory(); 
                   StatFs sf = new StatFs(sdcardDir.getPath());
                   long availCount = sf.getAvailableBlocks(); 
                   if(availCount>0){
                       return false;
                   } else {
                       return true;
                   }
               } 
         *   CR ID: ALPS00229364
         *   Descriptions: 
         */
        getExternalStorageDirectory();
        String state = getExternalStorageState(); 
        /*
         * Bug Fix by Mediatek End.
         */
               if(Environment.MEDIA_MOUNTED.equals(state)) { 
                   File sdcardDir = getExternalStorageDirectory(); 
                   StatFs sf = new StatFs(sdcardDir.getPath());
                   long availCount = sf.getAvailableBlocks(); 
                   if(availCount>0){
                       return false;
                   } else {
                       return true;
                   }
               } 

        return true;
    }  
    
    /*
     * Bug Fix by Mediatek Begin.
     *   CR ID: ALPS00110214
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ContactImportExportActivity.REQUEST_CODE) {
            if (resultCode == ContactImportExportActivity.RESULT_CODE) {
                this.finish();
            }
        }
    }
    /*
     * Bug Fix by Mediatek End.
     */
    // The following lines are provided and maintained by Mediatek inc.

    @Override
    protected void onDestroy() {
    	// Gionee:wangth 20120807 add for CR00667822 begin
    	if (ContactsUtils.mIsGnContactsSupport) {
    		mSelectedStep1Postion = 0;
        	mSelectedStep2Postion = 0;
    	}
    	// Gionee:wangth 20120807 add for CR00667822 end

    	//Gionee:huangzy 20130323 modify for CR00787985 start
    	/*mCellMgr.unregister();*/
    	try {
    		mCellMgr.unregister();
    	} catch (Exception e) {
    	}
    	//Gionee:huangzy 20130323 modify for CR00787985 end
    	
        super.onDestroy();
        Log.i(TAG,"[onDestroy]");
    }

    public void checkRaidoState2() {
        mSlotId = SimCardUtils.SimSlot.SLOT_NONE;
        if ((mCheckedAccount1 != null)
                && (mCheckedAccount1.type.equals(mSimAccountType) || mCheckedAccount1.type
                        .equals(mUsimAccountType))) {
            mSlotId = ((AccountWithDataSetEx) mCheckedAccount1).getSlotId();
            // Gionee <xuhz> <2013-07-15> modify for CR00828974 begin
            //old: int nRet = mCellMgr.handleCellConn(mSlotId, REQUEST_TYPE, serviceComplete);
            int nRet;
            if (ContactsApplication.sIsGnContactsSupport) {
            	nRet = mCellMgr.handleCellConn(mSlotId, REQUEST_TYPE, serviceComplete);
            } else {
            	nRet = mCellMgr.handleCellConn(mSlotId, REQUEST_TYPE);
            }
            // Gionee <xuhz> <2013-07-15> modify for CR00828974 end
            
            Log.i(TAG, "[checkRaidoState2] nRet : " + nRet);
            // Gionee:wangth 20130304 add for CR00777917 begin
            if (GNContactsUtils.isOnlyQcContactsSupport()) {
                handleImportExportAction();
            }
            // Gionee:wangth 20130304 add for CR00777917 end
        } else {
            handleImportExportAction();
        }

    }

    private Runnable serviceComplete = new Runnable() {
        public void run() {
            Log.d(TAG, "serviceComplete run");
            int nRet = mCellMgr.getResult();
            Log.d(TAG, "serviceComplete result = " + GnCellConnMgr.resultToString(nRet));
            Log.d("James", "serviceComplete result = " + GnCellConnMgr.resultToString(nRet));
            if (mCellMgr.RESULT_ABORT == nRet) {
                return;
            } else {
                SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(ContactImportExportActivity.this,
                        mSlotId);
                if (siminfo != null) {
                    mSimId = siminfo.mSimId;
                }
                Log.i(TAG, "mSimSelectionDialog mSimId is " + mSimId);
                handleImportExportAction();
                
                return;
            }
        }
    };

    private GnCellConnMgr mCellMgr = new GnCellConnMgr(serviceComplete);

    private static final int REQUEST_TYPE = 304;

    private int mSlotId;

    private long mSimId;

    private String mSimAccountType =  AccountType.ACCOUNT_TYPE_SIM;
    private String mUsimAccountType =  AccountType.ACCOUNT_TYPE_USIM;
    public static  AccountWithDataSetEx mCheckedAccountEnd = null;
    private static File mFile;
    
    
    
    
    
    public static String getExternalStorageState() {
        try {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                    .getService("mount"));
            Log.i(TAG,"[getExternalStorageState] mFile : "+mFile);
            return mountService.getVolumeState(mFile
                    .toString());
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }
    }
    public File getExternalStorageDirectory(){
//        StorageManager mSM = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
        //Gionee:wangth 20120420 modify for CR00576392 begin
        //String path = mSM.getDefaultPath();
        String path = null;
        if (ContactsUtils.mIsGnContactsSupport) {
            if (mSelectedStep1Postion == mUSBStoragePostion || mSelectedStep2Postion == mUSBStoragePostion) {
                path = mSdCard;
            } else {
                path = mSdCard2;
            }
        } else {
            path = GnStorageManager.getDefaultPath();
        }
        //Gionee:wangth 20120420 modify for CR00576392 end
        // Gionee:wangth 20121113 modify for CR00729290 begin
        /*
        final File file = getDirectory(path, "/mnt/sdcard");
        */
        File file;
        
        if (ContactsUtils.mIsSuperSDCardVersionSupport) {
            String sdPath = ContactsUtils.getRealSdPath(1);
            file = getDirectory(path, sdPath);
        } else {
            file = getDirectory(path, "/mnt/sdcard");
        }
        // Gionee:wangth 20121113 modify for CR00729290 end
        Log.i(TAG,"[getExternalStorageDirectory]file.path : "+file.getPath());
        mFile = file;
        return file;
    }
    public  File getDirectory(String path, String defaultPath) {
        Log.i("getDirectory","path : "+path);
        return path == null ? new File(defaultPath) : new File(path);
    }
    // The following lines are provided and maintained by Mediatek inc.
    
    // gionee xuhz 20120604 add for CR00614790 start
    public void onBackPressed() {
        if (ContactsApplication.sIsGnContactsSupport) {
            gnOnBackPressed();
            return;
        }
        super.onBackPressed();
    }
    
    public void gnOnBackPressed() {
        if (!ContactsApplication.sIsGnContactsSupport) {
            return;
        }
        if (mShowingStep <= 0) {
            Log.e(TAG, "Failed on Back/Up pressed, mShowingStep = " + mShowingStep);
            return;
        } else if (mShowingStep == 1) {
            finish();
        } else if (mShowingStep == 2) {
            setShowingStep(1);    
            int pos = getCheckedAccountPosition(mCheckedAccount1);
            // Gionee:wangth 20120618 modify for CR00625772 begin
            /*
            mCheckedPosition = pos;
            */
            mCheckedPosition = mSelectedStep1Postion;
            // Gionee:wangth 20120618 modify for CR00625772 end
            setCheckedAccount(mCheckedPosition);
            updateUi();
        } else {
            Log.e(TAG, "Failed on Back/Up pressed, mShowingStep = " + mShowingStep);
            return;
        }
    }
    // gionee xuhz 20120604 add for CR00614790 end
    
    // Gionee:wangth 20120616 add for CR00624473 begin
    public static void setSDCardPatch(Context context) {
        if (ContactsFeatureConstants.FeatureOption.MTK_2SDCARD_SWAP) {
            mStorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume[] storageVolume = mStorageManager.getVolumeList();
            mStorageCount = storageVolume.length;
            File[] systemSDCardMountPointPathList = new File[mStorageCount];
            
            for (int i = 0; i < mStorageCount; i++) {
                systemSDCardMountPointPathList[i] = new File(storageVolume[i].getPath());
            }
            
            File[] mSDCardMountPointPathList = updateMountedPointList(systemSDCardMountPointPathList);
            mStorageCount = mSDCardMountPointPathList.length;
            Log.d(TAG, "storageCount == " + mStorageCount);
            
            // Gionee:wangth 20121113 modify for CR00729290 begin
            if (mStorageCount >= 2) {
                mSdCard = ContactsUtils.getRealSdPath(2);
                mSdCard2 = ContactsUtils.getRealSdPath(1);
            } else {
                mSdCard = ContactsUtils.getRealSdPath(1);
                mSdCard2 = ContactsUtils.getRealSdPath(2);
                // Gionee:wangth 20120808 add for CR00671679 begin
                if (isExSdcardInserted()) {
                    mSdCard = ContactsUtils.getRealSdPath(2);
                    mSdCard2 = ContactsUtils.getRealSdPath(1);
                }
                // Gionee:wangth 20120808 add for CR00671679 end
            }
        } else {
            mSdCard = ContactsUtils.getRealSdPath(1);
            mSdCard2 = ContactsUtils.getRealSdPath(2);
            // Gionee:wangth 20121113 modify for CR00729290 end
        }
    }
    
    private static File[] updateMountedPointList(File[] systemSDCardMountPointPathList){
        int mountCount = 0;
        for (int i = 0; i < systemSDCardMountPointPathList.length; i++) {
            if (checkSDCardMount(systemSDCardMountPointPathList[i].getAbsolutePath())) {
                mountCount++;
            }
        }
        
        File[] sdCardMountPointPathList = new File[mountCount];
        
        for (int i = 0, j = 0; i < systemSDCardMountPointPathList.length; i++) {
            if (checkSDCardMount(systemSDCardMountPointPathList[i].getAbsolutePath())) {
                sdCardMountPointPathList[j++] = systemSDCardMountPointPathList[i];
            }
        }
        
        return sdCardMountPointPathList;
    }

    protected static boolean checkSDCardMount(String mountPoint) {
        if(mountPoint == null){
            return false;
        }
        
        String state = null;
        state = mStorageManager.getVolumeState(mountPoint);
        
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    // Gionee:wangth 20120616 add for CR00624473 end
    // Gionee:wangth 20120808 add for CR00671679 begin
    public static boolean isExSdcardInserted() {
        IBinder service = ServiceManager.getService("mount");
        Log.d("FileExplorerTabActivity", "Util:service is " + service);
 
        if (service != null) {
            IMountService mountService = IMountService.Stub.asInterface(service);
            Log.d(TAG, "Util:mountService is " + mountService);
            
            if(mountService == null) {
                return false;
            }
            
//            try {
                return GnStorageManager.isSDExist();
//            } catch (RemoteException e) {
//                Log.d("FileExplorerTabActivity", "Util:RemoteException when isSDExist: " + e);
//                return false;
//            }
        } else {
            return false;
        }
    }
    // Gionee:wangth 20120808 add for CR00671679 end

}
