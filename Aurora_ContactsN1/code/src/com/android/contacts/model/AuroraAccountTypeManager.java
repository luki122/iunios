package com.android.contacts.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.google.android.collect.Lists;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.Objects;

/**
 * Singleton holder for all parsed {@link AccountType} available on the
 * system, typically filled through {@link PackageManager} queries.
 */
public class AuroraAccountTypeManager {
    private static final String TAG = "AuroraAccountTypeManager";
    
    private AccountManager mAccountManager;
    
    public List<AccountWithDataSet> getAccounts(Context context) {
        mAccountManager = AccountManager.get(context);
        final List<AccountWithDataSet> allAccounts = Lists.newArrayList();
        
        try {
            allAccounts.add(new AccountWithDataSet("Phone", AccountType.ACCOUNT_TYPE_LOCAL_PHONE, null));
            
            if (ContactsUtils.mIsIUNIDeviceOnly) {
            	int slotid = SimCardUtils.SimSlot.SLOT_ID1;
                String simAccountType = AccountType.ACCOUNT_TYPE_SIM;
                String simName = null;
                if (SimCardUtils.isSimStateReady(slotid)) {
                    simAccountType = getAccountTypeBySlot(slotid);
                    simName = getSimAccountNameBySlot(slotid);
                    Log.i(TAG, "loadAccountsInBackground slotid:" + slotid + " AccountType:"
                            + simAccountType + " simName:" + simName);
                    
                    if (!TextUtils.isEmpty(simName) && !TextUtils.isEmpty(simAccountType)) {
                        allAccounts.add(new AccountWithDataSetEx(simName, simAccountType, slotid));
                    }
                }

                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    slotid = SimCardUtils.SimSlot.SLOT_ID2;
                    simAccountType = AccountType.ACCOUNT_TYPE_SIM;
                    simName = null;
                    if (SimCardUtils.isSimStateReady(slotid)) {
                        simAccountType = getAccountTypeBySlot(slotid);
                        simName = getSimAccountNameBySlot(slotid);
                        Log.i(TAG, "loadAccountsInBackground slotid2:" + slotid + " AccountType:"
                                + simAccountType + " simName:" + simName);
                        
                        if (!TextUtils.isEmpty(simName) && !TextUtils.isEmpty(simAccountType)) {
                            allAccounts.add(new AccountWithDataSetEx(simName, simAccountType, slotid));
                        }
                    }
                }
            }
            
            Account[] accounts = mAccountManager.getAccounts();
            for (Account account : accounts) {
                AccountWithDataSet accountWithDataSet = new AccountWithDataSet(
                        account.name, account.type, null);
                allAccounts.add(accountWithDataSet);
            }

//            Collections.sort(allAccounts, ACCOUNT_COMPARATOR);
            return allAccounts;
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return allAccounts;
    }
    
    public String getAccountTypeBySlot(int slotId) {
        Log.i(TAG, "getAccountTypeBySlot()+ - slotId:" + slotId);
        if (slotId < SimCardUtils.SimSlot.SLOT_ID1 || slotId > SimCardUtils.SimSlot.SLOT_ID2) {
            Log.e(TAG, "Error! - slot id error. slotid:" + slotId);
            return null;
        }
        int simtype = SimCardUtils.SimType.SIM_TYPE_SIM;
        String simAccountType = AccountType.ACCOUNT_TYPE_SIM;

        if (SimCardUtils.isSimInserted(slotId)) {
            simtype = SimCardUtils.getSimTypeBySlot(slotId);
            if (SimCardUtils.SimType.SIM_TYPE_USIM == simtype) {
                simAccountType = AccountType.ACCOUNT_TYPE_USIM;
            }
        } else {
            Log.e(TAG, "Error! getAccountTypeBySlot - slotId:" + slotId + " no sim inserted!");
            simAccountType = null;
        }
        Log.i(TAG, "getAccountTypeBySlot()- - slotId:" + slotId + " AccountType:" + simAccountType);
        return simAccountType;
    }

    public String getSimAccountNameBySlot(int slotId) {
        String retSimName = null;
        int simType = SimCardUtils.SimType.SIM_TYPE_SIM;

        Log.i(TAG, "getSimAccountNameBySlot()+ slotId:" + slotId);
        if (!SimCardUtils.isSimInserted(slotId)) {
            Log.e(TAG, "getSimAccountNameBySlot Error! - SIM not inserted!");
            return retSimName;
        }

        simType = SimCardUtils.getSimTypeBySlot(slotId);
        Log.i(TAG, "getSimAccountNameBySlot() slotId:" + slotId + " simType(0-SIM/1-USIM):" + simType);

        if (SimCardUtils.SimType.SIM_TYPE_SIM == simType) {
            retSimName = AccountType.ACCOUNT_NAME_SIM;
            if (SimCardUtils.SimSlot.SLOT_ID2 == slotId) {
                retSimName = AccountType.ACCOUNT_NAME_SIM2;
            }
        } else if (SimCardUtils.SimType.SIM_TYPE_USIM == simType) {
            retSimName = AccountType.ACCOUNT_NAME_USIM;
            if (SimCardUtils.SimSlot.SLOT_ID2 == slotId) {
                retSimName = AccountType.ACCOUNT_NAME_USIM2;
            }
        } else if (GNContactsUtils.isOnlyQcContactsSupport()) {
            if (GNContactsUtils.isMultiSimEnabled() && GNContactsUtils.cardIsUsim(slotId)) {
                retSimName = AccountType.ACCOUNT_NAME_USIM;
                if (SimCardUtils.SimSlot.SLOT_ID2 == slotId) {
                    retSimName = AccountType.ACCOUNT_NAME_USIM2;
                }
            }
            
            if (SimCardUtils.SimType.SIM_TYPE_UIM == simType) {
                retSimName = AccountType.ACCOUNT_NAME_UIM;
                if (SimCardUtils.SimSlot.SLOT_ID2 == slotId) {
                    retSimName = AccountType.ACCOUNT_NAME_UIM2;
                }
            }
        } else {
            Log.e(TAG, "getSimAccountNameBySlot() Error!  get SIM Type error! simType:" + simType);
        }

        Log.i(TAG, "getSimAccountNameBySlot()- slotId:" + slotId + " SimName:" + retSimName);
        return retSimName;
    }
    
    public static final Comparator<Account> ACCOUNT_COMPARATOR = new Comparator<Account>() {
        @Override
        public int compare(Account a, Account b) {
            String aDataSet = null;
            String bDataSet = null;
            if (a instanceof AccountWithDataSet) {
                aDataSet = ((AccountWithDataSet) a).dataSet;
            }
            if (b instanceof AccountWithDataSet) {
                bDataSet = ((AccountWithDataSet) b).dataSet;
            }

            AccountWithDataSetEx aEX = null;
            AccountWithDataSetEx bEX = null;
            int aSlot = -1;
            int bSlot = -1;
            boolean flagA = false;
            boolean flagB = false;
            
            if (a instanceof AccountWithDataSetEx) {
                aEX = (AccountWithDataSetEx) a;
            }

            if (b instanceof AccountWithDataSetEx) {
                bEX = (AccountWithDataSetEx) b;
            }

            if (aEX != null) {
                aSlot = aEX.getSlotId();
                flagA = true;
            }

            if (bEX != null) {
                bSlot = bEX.getSlotId();
                flagB = true;
            }

            if (flagA && flagB) {
                if (aSlot > bSlot) {
                    return 1;
                } else if (aSlot < bSlot) {
                    return -1;
                }
            } else if (!flagA && flagB) {
                return -1;
            } else if (!flagB && flagA) {
                return 1;
            }
            
            if (Objects.equal(a.name, b.name) && Objects.equal(a.type, b.type)
                    && Objects.equal(aDataSet, bDataSet)) {
                return 0;
            } else if (b.name == null || b.type == null) {
                return -1;
            } else if (a.name == null || a.type == null) {
                return 1;
            } else {
                int diff = a.name.compareTo(b.name);
                if (diff != 0) {
                    return diff;
                }
                diff = a.type.compareTo(b.type);
                if (diff != 0) {
                    return diff;
                }

                // Accounts without data sets get sorted before those that have them.
                if (aDataSet != null) {
                    return bDataSet == null ? 1 : aDataSet.compareTo(bDataSet);
                } else {
                    return -1;
                }
            }
        }
    };
}