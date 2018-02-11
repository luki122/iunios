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

package com.android.contacts.util;

import com.android.contacts.R;
import com.android.contacts.list.AccountFilterActivity;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

// The following lines are provided and maintained by Mediatek Inc.
// Description: for SIM name display
import java.util.List;

import com.android.contacts.ContactsApplication;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
// Gionee lihuafang 20120503 modify for CR00588600 begin
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
// Gionee lihuafang 20120503 modify for CR00588600 end
import com.mediatek.contacts.model.AccountWithDataSetEx;
// The previous lines are provided and maintained by Mediatek inc.

//Gionee:wangth 20120413 add for CR00572577 begin
import com.android.contacts.ContactsUtils;
//Gionee:wangth 20120413 add for CR00572577 end

/**
 * Utility class for account filter manipulation.
 */
public class AccountFilterUtil {
    private static final String TAG = AccountFilterUtil.class.getSimpleName();

    /**
     * Find TextView with the id "account_filter_header" and set correct text for the account
     * filter header.
     *
     * @param filterContainer View containing TextView with id "account_filter_header"
     * @return true when header text is set in the call. You may use this for conditionally
     * showing or hiding this entire view.
     */
    public static boolean updateAccountFilterTitleForPeople(View filterContainer,
            ContactListFilter filter, boolean isLoading, boolean showTitleForAllAccounts) {
        return updateAccountFilterTitle(
                filterContainer, filter, isLoading, showTitleForAllAccounts, false);
    }

    /**
     * Similar to {@link #updateAccountFilterTitleForPeople(View, ContactListFilter, boolean,
     * boolean)}, but for Phone UI.
     */
    public static boolean updateAccountFilterTitleForPhone(View filterContainer,
            ContactListFilter filter, boolean isLoading, boolean showTitleForAllAccounts) {
        return updateAccountFilterTitle(
                filterContainer, filter, isLoading, showTitleForAllAccounts, true);
    }

    //Gionee:huangzy 20120710 modify CR00614794 start
    private static boolean updateAccountFilterTitle(View filterContainer,
            ContactListFilter filter, boolean isLoading, boolean showTitleForAllAccounts,
            boolean forPhone) {
        final Context context = filterContainer.getContext();
        
       
        final ImageView typeImageView = (ImageView)filterContainer.findViewById(R.id.account_type_icon);
        if (null != typeImageView) {
            int iconRes = ContactListFilter.getIconFilterType(filter.filterType);
            if (0 != iconRes) {
                typeImageView.setImageResource(iconRes);
            } else {
                typeImageView.setImageDrawable(AccountType.getDisplayIconByAccount(filterContainer.getContext(),
                        filter.accountName, filter.accountType));
            }
        }
        
        
        final TextView headerTextView = (TextView)
                filterContainer.findViewById(R.id.account_filter_header);

        boolean textWasSet = false;
        if (isLoading) {
            headerTextView.setText(R.string.contact_list_loading);
        } else if (filter != null) {
            // The following lines are provided and maintained by Mediatek Inc.
            // Description: for SIM name display
            String displayName = null;
            displayName = getAccountDisplayNameByAccount(filter.accountType, filter.accountName);
            if (null == displayName) {
                //Gionee:wangth 20120413 add for CR00572577 begin
                //displayName = filter.accountName;
                final String mPhoneString = context.getString(R.string.gn_phone_text);
                
                if ( AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(filter.accountType)) {
                    displayName = mPhoneString;
                } else {
                    displayName = filter.accountName;
                }
                //Gionee:wangth 20120413 add for CR00572577 end
            }
            // The previous lines are provided and maintained by Mediatek inc.
            if (forPhone) {
                if (filter.filterType == ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS) {
                    if (showTitleForAllAccounts) {
                        headerTextView.setText(R.string.list_filter_phones);
                        textWasSet = true;
                    }
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
                    // The following lines are provided and maintained by Mediatek Inc.
                    // Description: for SIM name display
                    // Keep previous code here.
                    // headerTextView.setText(context.getString(
                    // R.string.listAllContactsInAccount, filter.accountName));
                	// gionee xuhz 20121203 modify for GIUI2.0 start
                	
                        headerTextView.setText(displayName);
                
                	// gionee xuhz 20121203 modify for GIUI2.0 end
                    // The previous lines are provided and maintained by Mediatek inc.
                    textWasSet = true;
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                    headerTextView.setText(R.string.listCustomView);
                    textWasSet = true;
                } else {
                    Log.w(TAG, "Filter type \"" + filter.filterType + "\" isn't expected.");
                }
            } else {
                if (filter.filterType == ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS) {
                    if (showTitleForAllAccounts) {
                        headerTextView.setText(R.string.list_filter_all_accounts);
                        textWasSet = true;
                    }
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
                    // The following lines are provided and maintained by Mediatek Inc.
                    // Description: for SIM name display
                    // Keep previous code here.
                    // headerTextView.setText(context.getString(
                    // R.string.listAllContactsInAccount, filter.accountName));
                	// gionee xuhz 20121203 modify for GIUI2.0 start
              
                        headerTextView.setText(displayName);
                
                	// gionee xuhz 20121203 modify for GIUI2.0 end
                    // The previous lines are provided and maintained by Mediatek inc.
                    textWasSet = true;
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                    headerTextView.setText(R.string.listCustomView);
                    textWasSet = true;
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
                    headerTextView.setText(R.string.listSingleContact);
                    textWasSet = true;
                } else {
                    Log.w(TAG, "Filter type \"" + filter.filterType + "\" isn't expected.");
                }
            }
        } else {
            Log.w(TAG, "Filter is null.");
        }
        return textWasSet;
    }
    //Gionee:huangzy 20120710 modify CR00614794 end

    /**
     * Launches account filter setting Activity using
     * {@link Activity#startActivityForResult(Intent, int)}.
     *
     * @param activity
     * @param requestCode requestCode for {@link Activity#startActivityForResult(Intent, int)}
     */
    public static void startAccountFilterActivityForResult(
            Activity activity, int requestCode) {
        final Intent intent = new Intent(activity, AccountFilterActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Very similar to {@link #startAccountFilterActivityForResult(Activity, int)} but uses
     * Fragment instead.
     */
    public static void startAccountFilterActivityForResult(
            Fragment fragment, int requestCode) {
        startAccountFilterActivityForResult(fragment, requestCode, null);
    }
    
    public static void startAccountFilterActivityForResult(
            Fragment fragment, int requestCode, Bundle exInfo) {
        final Activity activity = fragment.getActivity();
        if (activity != null) {
            final Intent intent = new Intent(activity, AccountFilterActivity.class);
            if (null != exInfo) {
            	intent.putExtras(exInfo);
            }
            fragment.startActivityForResult(intent, requestCode);
        } else {
            Log.w(TAG, "getActivity() returned null. Ignored");
        }
    }

    /**
     * Useful method to handle onActivityResult() for
     * {@link #startAccountFilterActivityForResult(Activity, int)} or
     * {@link #startAccountFilterActivityForResult(Fragment, int)}.
     *
     * This will update filter via a given ContactListFilterController.
     */
    public static void handleAccountFilterResult(
            ContactListFilterController filterController, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final ContactListFilter filter = (ContactListFilter)
                    data.getParcelableExtra(AccountFilterActivity.KEY_EXTRA_CONTACT_LIST_FILTER);
            if (filter == null) {
                return;
            }
            if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                filterController.selectCustomFilter();
            } else {
                filterController.setContactListFilter(filter, true);
            }
        }
    }

    // The following lines are provided and maintained by Mediatek Inc.
    // Description: for SIM name display
    public static String getAccountDisplayNameByAccount(String accountType, String accountName) {
        String accountDisplayName = null;
        if (null == accountType || null == accountName) {
            return accountDisplayName;
        }

        ContactsApplication contactsApp = ContactsApplication.getInstance();
        if (null == contactsApp) {
            return accountDisplayName;
        }
        List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(contactsApp)
                .getAccounts(true);

        if (null == accounts) {
            return accountDisplayName;
        }
        for (AccountWithDataSet ads : accounts) {
            if (ads instanceof AccountWithDataSetEx) {
                // Gionee lihuafang 20120503 modify for CR00588600 begin
                /*
                if (accountType.equals(ads.type) && accountName.equals(ads.name))
                accountDisplayName = ((AccountWithDataSetEx) ads).getDisplayName();
                */
            
                    if (accountType.equals(ads.type) && accountName.equals(ads.name))
                        accountDisplayName = ((AccountWithDataSetEx) ads).getDisplayName();
                         
                // Gionee lihuafang 20120503 modify for CR00588600 end
            }
        }

        return accountDisplayName;
    }
    // The previous lines are provided and maintained by Mediatek inc.
}