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

package com.android.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import aurora.app.AuroraActivity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
// Gionee:wangyaohui 20120522 modify for CR00607863 begin
import android.os.SystemProperties;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.content.DialogInterface;
import com.android.internal.os.storage.ExternalStorageFormatter;
// Gionee:wangyaohui 20120522 modify for CR00607863 end 

//Gionee <chenml> <2013-08-24> modify for CR00871601 begin
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.util.Locale;
//Gionee <chenml> <2013-08-24> modify for CR00871601 end
/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the initial screen.
 */
public class MasterClear extends Fragment {
    private static final String TAG = "MasterClear";

    private static final int KEYGUARD_REQUEST = 55;

    static final String ERASE_EXTERNAL_EXTRA = "erase_sd";

    private View mContentView;
    private Button mInitiateButton;
    private View mExternalStorageContainer;
    private CheckBox mExternalStorage;
  //Gionee <chenml> <2013-08-24> modify for CR00871601 begin
    private String mVolumeDescription;
  //Gionee <chenml> <2013-08-24> modify for CR00871601 end
    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper(getActivity(), this)
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.master_clear_gesture_explanation));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == AuroraActivity.RESULT_OK) {
            showFinalConfirmation();
        } else {
            establishInitialState();
        }
    }

    private void showFinalConfirmation() {
     /* AuroraPreference preference = new AuroraPreference(getActivity());
        preference.setFragment(MasterClearConfirm.class.getName());
        preference.setTitle(R.string.master_clear_confirm_title);
        preference.getExtras().putBoolean(ERASE_EXTERNAL_EXTRA, mExternalStorage.isChecked());
        ((AuroraPreferenceActivity) getActivity()).onPreferenceStartFragment(null, preference); */
        
         Context context = getActivity();
			//Gionee:zhang_xin 2012-12-18 modify for CR00746738 start
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context/*, AuroraAlertDialog.THEME_GIONEEVIEW_FULLSCREEN*/);
			builder.setTitle(R.string.reset_warning)
			// .setIconAttribute(android.R.attr.AuroraAlertDialogIcon)
			.setMessage(R.string.master_clear_final_desc)
			.setPositiveButton(R.string.okay_action,new DialogInterface.OnClickListener() {
				                    	public void onClick(DialogInterface dialog, int which) {
	            							if (Utils.isMonkeyRunning()) {
	            							    return;
	            							}
	
	            							if (mExternalStorage.isChecked()) {
	            							    Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
	            							    intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
	            							    getActivity().startService(intent);
	            							} else {
	            							    getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
	            							    // Intent handling is asynchronous -- assume it will happen soon.
	            							}
	
										}
									}
							  )
			.setNegativeButton(R.string.cancel_action,new DialogInterface.OnClickListener() {
				                        	public void onClick(DialogInterface dialog, int which) {
												// dismiss();
											}
										}
							  ).show().setCanceledOnTouchOutside(false);
			//Gionee:zhang_xin 2012-12-18 modify for CR00746738 end 
   }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private final Button.OnClickListener mInitiateListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                showFinalConfirmation();
            }
        }
    };

    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        mInitiateButton = (Button) mContentView.findViewById(R.id.initiate_master_clear);
        mInitiateButton.setOnClickListener(mInitiateListener);
        mExternalStorageContainer = mContentView.findViewById(R.id.erase_external_container);
        mExternalStorage = (CheckBox) mContentView.findViewById(R.id.erase_external);

        /*
         * If the external storage is emulated, it will be erased with a factory
         * reset at any rate. There is no need to have a separate option until
         * we have a factory reset that only erases some directories and not
         * others. Likewise, if it's non-removable storage, it could potentially have been
         * encrypted, and will also need to be wiped.
         */
        boolean isExtStorageEmulated = Environment.isExternalStorageEmulated();
        if (isExtStorageEmulated
                || (!Environment.isExternalStorageRemovable() && isExtStorageEncrypted())) {
            mExternalStorageContainer.setVisibility(View.GONE);

            final View externalOption = mContentView.findViewById(R.id.erase_external_option_text);
            externalOption.setVisibility(View.GONE);

            final View externalAlsoErased = mContentView.findViewById(R.id.also_erases_external);
            externalAlsoErased.setVisibility(View.VISIBLE);

            // If it's not emulated, it is on a separate partition but it means we're doing
            // a force wipe due to encryption.
            mExternalStorage.setChecked(!isExtStorageEmulated);
        } else {
            mExternalStorageContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mExternalStorage.toggle();
                }
            });
        }

        loadAccountList();
    }

    private boolean isExtStorageEncrypted() {
        String state = SystemProperties.get("vold.decrypt");
        return !"".equals(state);
    }

    private void loadAccountList() {
        View accountsLabel = mContentView.findViewById(R.id.accounts_label);
        LinearLayout contents = (LinearLayout)mContentView.findViewById(R.id.accounts);
        contents.removeAllViews();

        Context context = getActivity();

        AccountManager mgr = AccountManager.get(context);
        Account[] accounts = mgr.getAccounts();
        final int N = accounts.length;
        if (N == 0) {
            accountsLabel.setVisibility(View.GONE);
            contents.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        AuthenticatorDescription[] descs = AccountManager.get(context).getAuthenticatorTypes();
        final int M = descs.length;

        for (int i=0; i<N; i++) {
            Account account = accounts[i];
            AuthenticatorDescription desc = null;
            for (int j=0; j<M; j++) {
                if (account.type.equals(descs[j].type)) {
                    desc = descs[j];
                    break;
                }
            }
            if (desc == null) {
                Log.w(TAG, "No descriptor for account name=" + account.name
                        + " type=" + account.type);
                continue;
            }
            // Gionee <wangyaohui><2013-06-11> modify for CR00820932 begin
            // Drawable icon = null;
            // try {
            //     if (desc.iconId != 0) {
            //         Context authContext = context.createPackageContext(desc.packageName, 0);
            //         icon = authContext.getResources().getDrawable(desc.iconId);
            //     }
            // } catch (PackageManager.NameNotFoundException e) {
            //     Log.w(TAG, "No icon for account type " + desc.type);
            // }
            Drawable icon = null,tmpIcon = null;
            try {
                if (desc.iconId != 0) {
                  Context authContext = context.createPackageContext(desc.packageName, 0);
					tmpIcon = authContext.getResources().getDrawable(desc.iconId);
					int icon_width = (int)(getActivity().getResources().getDimension(R.dimen.icon_width));
                	int icon_height = (int)(getActivity().getResources().getDimension(R.dimen.icon_height));
					icon = Utils.zoomDrawable(tmpIcon, icon_width, icon_height);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "No icon for account type " + desc.type);
            } 
			catch (Exception ee) {
                ee.printStackTrace();
            }
            // Gionee <wangyaohui><2013-06-11> modify for CR00820932 end

            TextView child = (TextView)inflater.inflate(R.layout.master_clear_account,
                    contents, false);
            child.setText(account.name);
            if (icon != null) {
                child.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
            contents.addView(child);
        }

        accountsLabel.setVisibility(View.VISIBLE);
        contents.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //gionee wangyaohui 20120522 modify for CR00588626 begin
         mContentView = inflater.inflate(R.layout.master_clear, null);

//Gionee <chenml> <2013-08-24> modify for CR00871601 begin
        getVolumeDescription();
        updateTextLable();
//Gionee <chenml> <2013-08-24> modify for CR00871601 end
        establishInitialState();
        return mContentView;
    }
//Gionee <chenml> <2013-08-24> add for CR00871601 begin
    private void updateTextLable() {
        TextView externalOption1 = (TextView) mContentView
                .findViewById(R.id.erase_external_option_text);
        externalOption1
                .setText(getVolumeString(R.string.master_clear_desc_erase_external_storage));
        TextView externalOption2 = (TextView) mContentView
                .findViewById(R.id.erase_external_storage);
        externalOption2
                .setText(getVolumeString(R.string.erase_external_storage));
        TextView externalOption3 = (TextView) mContentView
                .findViewById(R.id.erase_external_storage_description);
        externalOption3
                .setText(getVolumeString(R.string.erase_external_storage_description));
    }

    private void getVolumeDescription() {
        StorageManager mStorageManager;
        mStorageManager = (StorageManager) getActivity().getSystemService(
                Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        for (int i = 0; i < volumes.length; i++) {
            if (!volumes[i].isRemovable()) {
                mVolumeDescription = volumes[i].getDescription(this
                        .getActivity());
                mVolumeDescription = mVolumeDescription.toLowerCase();
                break;
            }
        }
        Log.d(TAG, "mVolumeDescription=" + mVolumeDescription);
    }

    private String getVolumeString(int stringId) {
        if (mVolumeDescription == null) { // no volume description
            Log.d(TAG, "+mVolumeDescription is null and use default string");
            return getString(stringId);
        }
        //SD card string
        String sdCardString = getString(R.string.sdcard_setting);
        Log.d(TAG, "sdCardString=" + sdCardString);
        String str = getString(stringId).replace(sdCardString,
                mVolumeDescription);
        // maybe it is in lower case, no replacement try another
        if (str != null && str.equals(getString(stringId))) {
            sdCardString = sdCardString.toLowerCase();
            // restore to SD
            sdCardString = sdCardString.replace("sd", "SD");
            Log.d(TAG, "sdCardString" + sdCardString);
            str = getString(stringId).replace(sdCardString, mVolumeDescription);
            Log.d(TAG, "str" + str);
        }
        if (str != null && str.equals(getString(stringId))) {
            str = getString(stringId).replace("SD", mVolumeDescription);
            Log.d(TAG, "Not any available then replase key word sd str=" + str);
        }
        Locale tr = Locale.getDefault();
        // For chinese there is no space
        if (tr.getCountry().equals(Locale.CHINA.getCountry())
                || tr.getCountry().equals(Locale.TAIWAN.getCountry())) {
            // delete the space
            str = str.replace(" " + mVolumeDescription, mVolumeDescription);
        }
        return str;
    }
//Gionee <chenml> <2013-08-24> add for CR00871601 end
    
}
