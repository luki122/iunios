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
 * limitations under the License
 */

package com.android.contacts.editor;

import com.gionee.CellConnService.GnCellConnMgr;
import gionee.provider.GnTelephony.SIMInfo;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.AccountsListAdapter;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.model.AccountWithDataSetEx;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Shows a dialog asking the user which account to chose.
 *
 * The result is passed to {@code targetFragment} passed to {@link #show}.
 */
public final class SelectAccountDialogFragment extends DialogFragment {
    public static final String TAG = "SelectAccountDialogFragment";

    private static final String KEY_TITLE_RES_ID = "title_res_id";
    private static final String KEY_LIST_FILTER = "list_filter";
    private static final String KEY_EXTRA_ARGS = "extra_args";

    public SelectAccountDialogFragment() { // All fragments must have a public default constructor.
    }

    /**
     * Show the dialog.
     *
     * @param fragmentManager {@link FragmentManager}.
     * @param targetFragment {@link Fragment} that implements {@link Listener}.
     * @param titleResourceId resource ID to use as the title.
     * @param accountListFilter account filter.
     * @param extraArgs Extra arguments, which will later be passed to
     *     {@link Listener#onAccountChosen}.  {@code null} will be converted to
     *     {@link Bundle#EMPTY}.
     */
    public static <F extends Fragment & Listener> void show(FragmentManager fragmentManager,
            F targetFragment, int titleResourceId,
            AccountListFilter accountListFilter, Bundle extraArgs) {
        final Bundle args = new Bundle();
        args.putInt(KEY_TITLE_RES_ID, titleResourceId);
        args.putSerializable(KEY_LIST_FILTER, accountListFilter);
        args.putBundle(KEY_EXTRA_ARGS, (extraArgs == null) ? Bundle.EMPTY : extraArgs);

        final SelectAccountDialogFragment instance = new SelectAccountDialogFragment();
        instance.setArguments(args);
        instance.setTargetFragment(targetFragment, 0);
        instance.show(fragmentManager, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity());
        final Bundle args = getArguments();

        final AccountListFilter filter = (AccountListFilter) args.getSerializable(KEY_LIST_FILTER);
        final AccountsListAdapter accountAdapter = new AccountsListAdapter(builder.getContext(),
                filter);

        final DialogInterface.OnClickListener clickListener =
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                /*
                 * New feature by Mediatek Begin
                 * Original Android code:
                 * onAccountSelected(accountAdapter.getItem(which));
                 */
                 account = accountAdapter.getItem(which);
                 if(account instanceof AccountWithDataSetEx){
                     account = (AccountWithDataSetEx)accountAdapter.getItem(which);
                     mSlotId = ((AccountWithDataSetEx)account).getSlotId();
                    
                 }
                 int nRet = mCellMgr.handleCellConn(mSlotId, REQUEST_TYPE);
                /*
                 * New feature by Mediatek End
                 */
                 
                 if (GNContactsUtils.isOnlyQcContactsSupport() && account != null) {
                     Log.d(TAG, "selected account name is" + account.name);
                     onAccountSelected(account);
                 }
            }
        };

        builder.setTitle(args.getInt(KEY_TITLE_RES_ID));
        builder.setSingleChoiceItems(accountAdapter, 0, clickListener);
        final AuroraAlertDialog result = builder.create();
        return result;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment != null && targetFragment instanceof Listener) {
            final Listener target = (Listener) targetFragment;
            target.onAccountSelectorCancelled();
        }
    }

    /**
     * Calls {@link Listener#onAccountChosen} of {@code targetFragment}.
     */
    private void onAccountSelected(AccountWithDataSet account) {
        final Fragment targetFragment = getTargetFragment();
        //Gionee <xuhz> <2013-08-16> modify for CR00859105 begin
        //old:if (targetFragment != null && targetFragment instanceof Listener) {
        if (targetFragment != null && targetFragment instanceof Listener && account != null) {
        //Gionee <xuhz> <2013-08-16> modify for CR00859105 end
            final Listener target = (Listener) targetFragment;
            target.onAccountChosen(account, getArguments().getBundle(KEY_EXTRA_ARGS));
        }
    }

    public interface Listener {
        void onAccountChosen(AccountWithDataSet account, Bundle extraArgs);
        void onAccountSelectorCancelled();
    }

    // The following lines are provided and maintained by Mediatek Inc.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCellMgr.register(this.getActivity());
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDetach() {
    	//Gionee <huangzy> <2013-05-14> modify for CR00811789 begin
    	/*mCellMgr.unregister();*/
    	try {
    		mCellMgr.unregister();	
		} catch (Exception e) {
		}
    	//Gionee <huangzy> <2013-05-14> modify for CR00811789 end
        
        super.onDetach();
    }

    private Runnable serviceComplete = new Runnable() {
        public void run() {
            Log.d(TAG, "serviceComplete run");          
            int nRet = mCellMgr.getResult();
            Log.d(TAG, "serviceComplete result = " + GnCellConnMgr.resultToString(nRet));
            Log.d(TAG, "mCellMgr.RESULT_ABORT = " + mCellMgr.RESULT_ABORT);
            Log.d(TAG, "nRet = " + nRet);
            if (mCellMgr.RESULT_ABORT == nRet) { 
                getTargetFragment().getActivity().finish();
                SelectAccountDialogFragment.this.dismiss();
                return;
            } else {
                onAccountSelected(account);
                return;
            }
        }
    };
    private int mSlotId =-1;
    private GnCellConnMgr mCellMgr = new GnCellConnMgr(serviceComplete);
    private static final int REQUEST_TYPE = 304;
    private AccountWithDataSet account;
    // The previous  lines are provided and maintained by Mediatek Inc.
}
