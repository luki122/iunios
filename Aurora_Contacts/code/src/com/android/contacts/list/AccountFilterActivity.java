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

package com.android.contacts.list;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.list.AccountFilterActivity.FilterListAdapter;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.ExchangeAccountType;
import com.google.android.collect.Lists;

import android.app.ActionBar;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import aurora.widget.AuroraListView;

import java.util.ArrayList;
import java.util.List;
// The following lines are provided and maintained by Mediatek Inc.
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.os.Message;
// The previous lines are provided and maintained by Mediatek Inc.

// Gionee lihuafang 20120503 modify for CR00588600 begin
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.contacts.ContactsUtils;
// Gionee lihuafang 20120503 modify for CR00588600 end

// The following lines are provided and maintained by Mediatek Inc.
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;
// The previous lines are provided and maintained by Mediatek Inc.


/**
 * Shows a list of all available accounts, letting the user select under which account to view
 * contacts.
 */
public class AccountFilterActivity extends ContactsActivity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = AccountFilterActivity.class.getSimpleName();

    private static final int SUBACTIVITY_CUSTOMIZE_FILTER = 0;

    public static final String KEY_EXTRA_CONTACT_LIST_FILTER = "contactListFilter";

    private static final int FILTER_LOADER_ID = 0;

    private AuroraListView mListView;
    
    private AuroraAlertDialog mAccountFilterDialog;
    
    public static final String EXTRA_ONLY_STARRBLE_ACCOUNT = "onlyStarrbleAccount";
    private Bundle mExInfo;
    private boolean mIsOnlyShowStarrbleAccount;

    @Override
    protected void onCreate(Bundle icicle) {
/*        // gionee xuhz add for support gn theme start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_ContactListFilterTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_AccountFilterTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_AccountFilterTheme_light);
        } 
        // gionee xuhz add for support gn theme start
 		*/    	
    	
        // gionee xuhz add for support gn theme start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_PeopleTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_PeopleTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_PeopleTheme_light);
        } 
        // gionee xuhz add for support gn theme start
        
        super.onCreate(icicle);
        setContentView(R.layout.contact_list_filter);

        mListView = (AuroraListView) findViewById(com.android.internal.R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setVisibility(View.GONE);
        
        mExInfo = getIntent().getExtras();
        if (null != mExInfo) {
        	mIsOnlyShowStarrbleAccount = mExInfo.getBoolean(EXTRA_ONLY_STARRBLE_ACCOUNT); 
        }
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mLoadingContainer = this.findViewById(R.id.loading_container);
        mLoadingContact = (TextView) this.findViewById(R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) this.findViewById(R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);

        /*
         * Bug Fix by Mediatek End.
         */
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            
            // gionee xuhz 201220529 add for remove ActionBar home icon start
            if (ContactsApplication.sIsGnContactsSupport) {
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
            }
            // gionee xuhz 201220529 add for remove ActionBar home icon end
        }

        getLoaderManager().initLoader(FILTER_LOADER_ID, null, new MyLoaderCallbacks());
    }

    private static class FilterLoader extends AsyncTaskLoader<List<ContactListFilter>> {
        private Context mContext;
        private boolean mIsOnlyShowStarrbleAccount;

        public FilterLoader(Context context, boolean isOnlyShowStarrbleAccount) {
            super(context);
            mContext = context;
            mIsOnlyShowStarrbleAccount = isOnlyShowStarrbleAccount;
        }

        @Override
        public List<ContactListFilter> loadInBackground() {
            return loadAccountFilters(mContext, mIsOnlyShowStarrbleAccount);
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

    private static List<ContactListFilter> loadAccountFilters(Context context, boolean isOnlyShowStarrbleAccount) {
        final ArrayList<ContactListFilter> result = Lists.newArrayList();
        final ArrayList<ContactListFilter> accountFilters = Lists.newArrayList();
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(context);
        List<AccountWithDataSet> accounts = accountTypes.getAccounts(false);
        for (AccountWithDataSet account : accounts) {
        	if (isOnlyShowStarrbleAccount) {
        		if (account.type.equals(AccountType.ACCOUNT_TYPE_SIM) ||
        				account.type.equals(AccountType.ACCOUNT_TYPE_USIM)) {
        			continue;
        		}
        	}
        	
            AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
            if (accountType.isExtension() && !account.hasData(context)) {
                // Hide extensions with no raw_contacts.
                continue;
            }
            Log.i("geticon","[accountfilteractivity] ");
            
            /*
             * Change feature by Mediatek Begin.
             *   Original Android's code:
             *   Drawable icon = accountType != null ? accountType.getDisplayIcon(context) : null;
             *   CR ID: ALPS00233786
             *   Descriptions: cu feature change photo by slot id 
             */
            Drawable icon;
            int slotId = -1;
            if (isSimUsimAccountType(accountType.accountType)) {
                slotId = ((AccountWithDataSetEx) account).mSlotId;
            }
            // Gionee <xuhz> <2013-08-16> modify for CR00858149 begin
            //old:if (accountType != null && OperatorUtils.getOptrProperties().equals("OP02") && isSimUsimAccountType(accountType.accountType)) {
            if (accountType != null && OperatorUtils.getActualOptrProperties().equals("OP02") && isSimUsimAccountType(accountType.accountType)) {
            // Gionee <xuhz> <2013-08-16> modify for CR00858149 end
                Log.i(TAG,"[AccountFilterActivity] mSlotId : "+slotId);
                icon = accountType.getDisplayIconBySlotId(context, slotId);
            } 
            // Gionee lihuafang 20120503 modify for CR00588600 begin
            else if (ContactsUtils.mIsGnContactsSupport && FeatureOption.MTK_GEMINI_SUPPORT
                    && (ContactsUtils.mIsGnShowSlotSupport || ContactsUtils.mIsGnShowDigitalSlotSupport)) {
                icon = accountType.getDisplayIconBySlotId(context, slotId);
            }
            // Gionee lihuafang 20120503 modify for CR00588600 end
            else {
                icon = accountType != null ? accountType.getDisplayIcon(context) : null;
            }
            /*
             * Change Feature by Mediatek End.
             */
            
            // qc begin
            if (GNContactsUtils.isOnlyQcContactsSupport() && GNContactsUtils.isMultiSimEnabled() && slotId > -1) {
                icon = accountType.getDisplayIconBySlotId(context, slotId);
            }
            // qc end
            
            accountFilters.add(ContactListFilter.createAccountFilter(
                    account.type, account.name, account.dataSet, icon));
        }

        // Always show "All", even when there's no accounts.  (We may have local contacts)
        if (!isOnlyShowStarrbleAccount) {
        	result.add(ContactListFilter.createFilterWithType(
        			ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
        }

        final int count = accountFilters.size();
        if (count >= 1) {
            // If we only have one account, don't show it as "account", instead show it as "all"
            // gionee xuhz modify for CR00582602
            if (ContactsApplication.sIsGnContactsSupport) {
                result.addAll(accountFilters);
            } else {
                if (count > 1) {
                    result.addAll(accountFilters);
                }                
            }
            // gionee xuhz modify for CR00582602
            
            if (!isOnlyShowStarrbleAccount) {
	            result.add(ContactListFilter.createFilterWithType(
	                    ContactListFilter.FILTER_TYPE_CUSTOM));
            }
        }
        return result;
    }
    
    //Gionee:huangzy 20120710 add for CR00614794 start
    public static List<ContactListFilter> loadAccountFilters(Context context) {
        return loadAccountFilters(context, false);
    }
    //Gionee:huangzy 20120710 add for CR00614794 end
    private class MyLoaderCallbacks implements LoaderCallbacks<List<ContactListFilter>> {
        @Override
        public Loader<List<ContactListFilter>> onCreateLoader(int id, Bundle args) {
            
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            Log.i(TAG,"onCreateLoader");
            isFinished = false;
            mLoadingContainer.setVisibility(View.GONE);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                    WAIT_CURSOR_DELAY_TIME);
            /*
             * Bug Fix by Mediatek End.
             */
            
            return new FilterLoader(AccountFilterActivity.this, mIsOnlyShowStarrbleAccount);
        }

        @Override
        public void onLoadFinished(
                Loader<List<ContactListFilter>> loader, List<ContactListFilter> data) {
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            Log.i(TAG,"onLoadFinished");
            isFinished = true;
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(AccountFilterActivity.this,
                    android.R.anim.fade_out));
            mLoadingContainer.setVisibility(View.GONE);
            mLoadingContact.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
            /*
             * Bug Fix by Mediatek End.
             */
            if (data == null) { // Just in case...
                Log.e(TAG, "Failed to load filters");
                return;
            }
            
            FilterListAdapter adapter = new FilterListAdapter(AccountFilterActivity.this, data);
            mListView.setAdapter(adapter);
            
            mAccountFilterDialog = new AuroraAlertDialog.Builder(AccountFilterActivity.this)            
            	.setTitle(R.string.gn_menu_contacts_filter)
            	.setAdapter(adapter, null).setTitleDividerVisible(true)
            	.create();
            
            mAccountFilterDialog.getListView().setOnItemClickListener(AccountFilterActivity.this);
            mAccountFilterDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					onBackPressed();
				}
			});
            mAccountFilterDialog.show();
        }

        @Override
        public void onLoaderReset(Loader<List<ContactListFilter>> loader) {
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
    	if (null != mAccountFilterDialog && mAccountFilterDialog.isShowing()) {
    		mAccountFilterDialog.dismiss();
    	}
    	
        final ContactListFilter filter = (ContactListFilter) view.getTag();
        if (filter == null) return; // Just in case
        if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
            final Intent intent = new Intent(this,
                    CustomContactListFilterActivity.class);
            startActivityForResult(intent, SUBACTIVITY_CUSTOMIZE_FILTER);
        } else {
            final Intent intent = new Intent();
            intent.putExtra(KEY_EXTRA_CONTACT_LIST_FILTER, filter);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case SUBACTIVITY_CUSTOMIZE_FILTER: {
                final Intent intent = new Intent();
                ContactListFilter filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_CUSTOM);
                intent.putExtra(KEY_EXTRA_CONTACT_LIST_FILTER, filter);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            }
        }
    }

    //Gionee:huangzy 20120710 add modify CR00614794 start
    public static class FilterListAdapter extends BaseAdapter {
    //Gionee:huangzy 20120710 add modify CR00614794 end
        private final List<ContactListFilter> mFilters;
        private final LayoutInflater mLayoutInflater;

        public FilterListAdapter(Context context, List<ContactListFilter> filters) {
            mLayoutInflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            mFilters = filters;
        }

        @Override
        public int getCount() {
            return mFilters.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public ContactListFilter getItem(int position) {
            return mFilters.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ContactListFilterView view;
            if (convertView != null) {
                view = (ContactListFilterView) convertView;
            } else {
                view = (ContactListFilterView) mLayoutInflater.inflate(
                        R.layout.contact_list_filter_item, parent, false);
            }
            view.setSingleAccount(mFilters.size() == 1);
            final ContactListFilter filter = mFilters.get(position);
            view.setContactListFilter(filter);
            view.bindView(true);
            view.setTag(filter);
            return view;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // We have two logical "up" Activities: People and Phone.
                // Instead of having one static "up" direction, behave like back as an
                // exceptional case.
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    /*
     * Change feature by Mediatek Begin.
     *   Original Android's code:
     *   
     *   CR ID: ALPS00233786
     *   Descriptions: cu feature change photo by slot id 
     */
    private static boolean isSimUsimAccountType(String accountType) {
        Log.i("checkphoto","accountType : "+accountType);
        boolean bRet = false;
        if (AccountType.ACCOUNT_TYPE_SIM.equals(accountType)  
                || AccountType.ACCOUNT_TYPE_USIM.equals(accountType)) {
            bRet = true;
        }
        return bRet;
    }
    /*
     * Change Feature by Mediatek End.
     */
    
    
    
    
    /*
     * Bug Fix by Mediatek Begin. Original Android's code: CR ID: ALPS00115673
     * Descriptions: add wait cursor
     */

    private TextView mLoadingContact;

    private ProgressBar mProgress;

    private View mLoadingContainer;

    public static boolean isFinished = false;

    private static final int WAIT_CURSOR_START = 1230;

    private static final long WAIT_CURSOR_DELAY_TIME = 500;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage msg==== " + msg.what);

            switch (msg.what) {

                case WAIT_CURSOR_START:
                    Log.i(TAG, "start WAIT_CURSOR_START !isFinished : " + !isFinished);
                    if (!isFinished) {
                        mLoadingContainer.setVisibility(View.VISIBLE);
                        mLoadingContact.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };
    /*
     * Bug Fix by Mediatek End.
     */
    
    
    
}
