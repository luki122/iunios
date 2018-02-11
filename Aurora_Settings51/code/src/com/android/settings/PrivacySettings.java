 /* 文件:PrivacySettings.java
 * 描述：恢复出厂设置交互
 * 
 * 
 * 修改时间：20150714
 * 描述：按要求，移植U3恢复出厂设置交互
 * 修改人：hujianwei
 */

package com.android.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.util.Log;
import android.content.Context;
import android.os.Build;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.backup.IBackupManager;
import android.content.BroadcastReceiver;
import com.android.settings.search.Indexable;
import com.android.internal.os.storage.ExternalStorageFormatter;
import android.os.SystemProperties;
import android.content.res.Resources;

import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import com.mediatek.xlog.Xlog;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.android.settings.AuroraSettingsPreferenceFragment;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreferenceGroup;
import aurora.widget.AuroraCheckBox;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraActivity;

//<Aurora><hujianwei> 20150718 add for import
import android.os.SystemProperties;
//<Aurora><hujianwei> 20150718 add for import
/**
 * Gesture lock pattern settings.
 */
public class PrivacySettings extends AuroraSettingsPreferenceFragment implements
        DialogInterface.OnClickListener, Indexable {

    private static final String TAG = "PrivacySettings";
    public static final String ACTION_CONFIRM_KEY = "com.android.settings.ACTION_CONFIRM_KEY";
    public static final String KEY_AURORA_FACTORY_DEFAULT_PREF = "aurora_factory_default";
    private static final int KEYGUARD_REQUEST = 55;

    private Button mStartFactory;
    private AuroraCheckBox mCbClearMultiData;
    private View mCbItem; 
    
  //<Aurora><hujianwei> 20150718 add for ro.build.cts support
    public static final boolean gIsCtsSupportFlag = SystemProperties.get("ro.build.cts").equals("yes");
 
    // Vendor specific
    private static final String GSETTINGS_PROVIDER = "com.google.settings";
    private static final String BACKUP_CATEGORY = "backup_category";
    private static final String BACKUP_DATA = "backup_data";
    private static final String AUTO_RESTORE = "auto_restore";
    ///M: Add for DRM settings
    private static final String DRM_RESET = "drm_settings";
    ///@}
    private static final String CONFIGURE_ACCOUNT = "configure_account";
    private static final String BACKUP_INACTIVE = "backup_inactive";
    private static final String PERSONAL_DATA_CATEGORY = "personal_data_category";

    private IBackupManager mBackupManager;
    private AuroraSwitchPreference mBackup;
    private AuroraSwitchPreference mAutoRestore;
    private Dialog mConfirmDialog;
    private AuroraPreferenceScreen mConfigure;
    private boolean mEnabled;
    private static final int DIALOG_ERASE_BACKUP = 2;
    private int mDialogType;

    ///M: change backup reset title
    private ISettingsMiscExt mExt;
    //<Aurora><hujianwei> 20150718 add for ro.build.cts support
    
    private BroadcastReceiver confirmKeyReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ACTION_CONFIRM_KEY)){
				Log.i(TAG, "onActivityResult()****");
				showFinalConfirmation();
			}
		}
	};

	//<Aurora><hujianwei> 20150718 add start adater for cts 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(gIsCtsSupportFlag){

            addPreferencesFromResource(R.xml.privacy_settings);
            final AuroraPreferenceScreen screen = getPreferenceScreen();
            mBackupManager = IBackupManager.Stub.asInterface(
                    ServiceManager.getService(Context.BACKUP_SERVICE));

            mBackup = (AuroraSwitchPreference) screen.findPreference(BACKUP_DATA);
            mBackup.setOnPreferenceChangeListener(preferenceChangeListener);

            mAutoRestore = (AuroraSwitchPreference) screen.findPreference(AUTO_RESTORE);
            mAutoRestore.setOnPreferenceChangeListener(preferenceChangeListener);

            mConfigure = (AuroraPreferenceScreen) screen.findPreference(CONFIGURE_ACCOUNT);
            ///M: change backup reset title @{
            mExt = UtilsExt.getMiscPlugin(getActivity());
            mExt.setFactoryResetTitle(getActivity());
            /// @}

            ArrayList<String> keysToRemove = getNonVisibleKeys(getActivity());
            final int screenPreferenceCount = screen.getPreferenceCount();
            for (int i = screenPreferenceCount - 1; i >= 0; --i) {
                AuroraPreference preference = screen.getPreference(i);
                if (keysToRemove.contains(preference.getKey())) {
                    screen.removePreference(preference);
                }
            }
            AuroraPreferenceCategory backupCategory = (AuroraPreferenceCategory) findPreference(BACKUP_CATEGORY);
            if (backupCategory != null) {
                final int backupCategoryPreferenceCount = backupCategory.getPreferenceCount();
                for (int i = backupCategoryPreferenceCount - 1; i >= 0; --i) {
                    AuroraPreference preference = backupCategory.getPreference(i);
                    if (keysToRemove.contains(preference.getKey())) {
                        backupCategory.removePreference(preference);
                    }
                }
            }
            updateToggles();
            ///M: Check Drm compile option for DRM settings @{
            if (!FeatureOption.MTK_DRM_APP) {
                AuroraPreferenceCategory perDataCategory =
                           (AuroraPreferenceCategory) findPreference(PERSONAL_DATA_CATEGORY);
                if (perDataCategory != null) {
                    perDataCategory.removePreference(findPreference(DRM_RESET));
                }
            }
            ///@}
            
        }else
        {
            IntentFilter intentFilter = new IntentFilter(ACTION_CONFIRM_KEY);
            getActivity().registerReceiver(confirmKeyReceiver,intentFilter);
           
        }
       
    }
  //<Aurora><hujianwei> 20150718 add start adater for cts end
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		if (!gIsCtsSupportFlag) {

			View mView = null;

			mView = inflater.inflate(R.layout.aurora_factory_reset_main,
					container, false);
			mCbClearMultiData = (AuroraCheckBox) mView
					.findViewById(R.id.cb_clear_multi_user_data);
			mCbItem = mView.findViewById(R.id.ll_clear_multi_user_data);
			mCbItem.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCbClearMultiData.setChecked(!mCbClearMultiData.isChecked());
				}

			});

			mStartFactory = (Button) mView
					.findViewById(R.id.start_factory_button);
			mStartFactory.setOnClickListener(mStartFactoryListener);
			return mView;
		}
		else
		{
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		
    }
    
    protected void showFinalConfirmation() {
		// TODO Auto-generated method stub
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity());
        builder.setTitle(R.string.master_clear_title)
        .setMessage(R.string.master_clear_final_desc)
        .setPositiveButton(R.string.okay_continue,new DialogInterface.OnClickListener(){
        	
            public void onClick(DialogInterface dialog, int which){
            		new AuroraAlertDialog.Builder(getActivity())
            		.setTitle(R.string.master_clear_title)
            		.setMessage(R.string.master_clear_confirm)
            		.setPositiveButton(R.string.okay_restore,new DialogInterface.OnClickListener(){
            			
            			public void onClick(DialogInterface dialog, int which){
            				startFactoryDefault();
            				}
            		 })
            		.setNegativeButton(R.string.cancel_action,null)
            	    .show()
            	    .setCanceledOnTouchOutside(false);
            }
			})
        .setNegativeButton(R.string.cancel_action,null)
        .show()
        .setCanceledOnTouchOutside(false);
	}
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
    
	private void startFactoryDefault() {
        if (Utils.isMonkeyRunning()) {
            return;
        }

		Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
		intent.putExtra("wipe_internal_data", getCheckBoxState() ? true
				: false);
		getActivity().sendBroadcast(intent);
      
	}
	
    private final Button.OnClickListener mStartFactoryListener = new Button.OnClickListener() {
        public void onClick(View v) {
           Log.d(TAG,"start mStartFactoryListener");
           boolean isUnlockPattern = runKeyguardConfirmation(KEYGUARD_REQUEST);
           if(!isUnlockPattern){
                showFinalConfirmation();
           }
        }
    }; 
    
    public boolean getCheckBoxState(){
    	if(mCbClearMultiData !=null){
    		return mCbClearMultiData.isChecked();
    	}
    	return false;
    }
    
    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper((AuroraActivity)getActivity())
                .launchConfirmationActivity(request,
                        res.getText(R.string.master_clear_gesture_prompt),
                        res.getText(R.string.master_clear_gesture_explanation));
    }

    @Override
    public void onResume() {
        super.onResume();
        if(gIsCtsSupportFlag){
            // Refresh UI
            if (mEnabled) {
                updateToggles();
            }
        }
    }

	@Override
	public void onStop() {
		if (gIsCtsSupportFlag) {
			if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
				mConfirmDialog.dismiss();
			}
			mConfirmDialog = null;
			mDialogType = 0;
		}
		super.onStop();
	}
    
    public void onDestroy() {
    	super.onDestroy();
    	if(! gIsCtsSupportFlag){
    		getActivity().unregisterReceiver(confirmKeyReceiver);
    	}
    	
    }
    

    //<Aurora><hujianwei>20150718 add api start
    private AuroraPreference.OnPreferenceChangeListener preferenceChangeListener = new AuroraPreference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
            if (!(preference instanceof AuroraSwitchPreference)) {
                return true;
            }
            boolean nextValue = (Boolean) newValue;
            boolean result = false;
            if (preference == mBackup) {
                if (nextValue == false) {
                    // Don't change Switch status until user makes choice in dialog
                    // so return false here.
                    showEraseBackupDialog();
                } else {
                    setBackupEnabled(true);
                    result = true;
                }
            } else if (preference == mAutoRestore) {
                try {
                    mBackupManager.setAutoRestore(nextValue);
                    result = true;
                } catch (RemoteException e) {
                    mAutoRestore.setChecked(!nextValue);
                }
            }
            return result;
        }
    };

    private void showEraseBackupDialog() {
        mDialogType = DIALOG_ERASE_BACKUP;
        CharSequence msg = getResources().getText(R.string.backup_erase_dialog_message);
        // TODO: DialogFragment?
        mConfirmDialog = new AlertDialog.Builder(getActivity()).setMessage(msg)
                .setTitle(R.string.backup_erase_dialog_title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .show();
    }

    /*
     * Creates toggles for each available location provider
     */
    private void updateToggles() {
        ContentResolver res = getContentResolver();

        boolean backupEnabled = false;
        Intent configIntent = null;
        String configSummary = null;
        try {
            backupEnabled = mBackupManager.isBackupEnabled();
            String transport = mBackupManager.getCurrentTransport();
            configIntent = mBackupManager.getConfigurationIntent(transport);
            configSummary = mBackupManager.getDestinationString(transport);
        } catch (RemoteException e) {
            // leave it 'false' and disable the UI; there's no backup manager
            mBackup.setEnabled(false);
        }
        mBackup.setChecked(backupEnabled);

        if (backupEnabled) {
            // provision the backup manager.
            IBackupManager bm = IBackupManager.Stub.asInterface(ServiceManager
                    .getService(Context.BACKUP_SERVICE));
            if (bm != null) {
                try {
                    bm.setBackupProvisioned(true);
                } catch (RemoteException e) {
                    Xlog.e(TAG, "set backup provisioned false!");
                }
            }
        }

        mAutoRestore.setChecked(Settings.Secure.getInt(res,
                Settings.Secure.BACKUP_AUTO_RESTORE, 1) == 1);
        mAutoRestore.setEnabled(backupEnabled);

        final boolean configureEnabled = (configIntent != null) && backupEnabled;
        mConfigure.setEnabled(configureEnabled);
        mConfigure.setIntent(configIntent);
        setConfigureSummary(configSummary);
    }

    private void setConfigureSummary(String summary) {
        if (summary != null) {
            mConfigure.setSummary(summary);
        } else {
            mConfigure.setSummary(R.string.backup_configure_account_default_summary);
        }
    }

    private void updateConfigureSummary() {
        try {
            String transport = mBackupManager.getCurrentTransport();
            String summary = mBackupManager.getDestinationString(transport);
            setConfigureSummary(summary);
        } catch (RemoteException e) {
            // Not much we can do here
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Dialog is triggered before Switch status change, that means marking the Switch to
        // true in showEraseBackupDialog() method will be override by following status change.
        // So we do manual switching here due to users' response.
        if (mDialogType == DIALOG_ERASE_BACKUP) {
            // Accept turning off backup
            if (which == DialogInterface.BUTTON_POSITIVE) {
                setBackupEnabled(false);
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                // Reject turning off backup
                setBackupEnabled(true);
            }
            updateConfigureSummary();
        }
        mDialogType = 0;
    }

    /**
     * Informs the BackupManager of a change in backup state - if backup is disabled,
     * the data on the server will be erased.
     * @param enable whether to enable backup
     */
    private void setBackupEnabled(boolean enable) {
        if (mBackupManager != null) {
            try {
                mBackupManager.setBackupEnabled(enable);
                mBackupManager.setBackupProvisioned(enable);
            } catch (RemoteException e) {
                mBackup.setChecked(!enable);
                mAutoRestore.setEnabled(!enable);
                return;
            }
        }
        mBackup.setChecked(enable);
        mAutoRestore.setEnabled(enable);
        mConfigure.setEnabled(enable);
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_backup_reset;
    }

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new PrivacySearchIndexProvider();

    private static class PrivacySearchIndexProvider extends BaseSearchIndexProvider {

        boolean mIsPrimary;

        public PrivacySearchIndexProvider() {
            super();

            mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;
        }

        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {

            List<SearchIndexableResource> result = new ArrayList<SearchIndexableResource>();

            // For non-primary user, no backup or reset is available
            if (!mIsPrimary) {
                return result;
            }

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.privacy_settings;
            result.add(sir);

            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            return getNonVisibleKeys(context);
        }
    }

    private static ArrayList<String> getNonVisibleKeys(Context context) {
        final ArrayList<String> nonVisibleKeys = new ArrayList<String>();
        final IBackupManager backupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        boolean isServiceActive = false;
        try {
            isServiceActive = backupManager.isBackupServiceActive(UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.w(TAG, "Failed querying backup manager service activity status. " +
                    "Assuming it is inactive.");
        }
        if (isServiceActive) {
            nonVisibleKeys.add(BACKUP_INACTIVE);
        } else {
            nonVisibleKeys.add(AUTO_RESTORE);
            nonVisibleKeys.add(CONFIGURE_ACCOUNT);
            nonVisibleKeys.add(BACKUP_DATA);
        }
        if (UserManager.get(context).hasUserRestriction(
                UserManager.DISALLOW_FACTORY_RESET)) {
            nonVisibleKeys.add(PERSONAL_DATA_CATEGORY);
        }
        // Vendor specific
        if (context.getPackageManager().
                resolveContentProvider(GSETTINGS_PROVIDER, 0) == null) {
            nonVisibleKeys.add(BACKUP_CATEGORY);
        }
        return nonVisibleKeys;
    }
    //<Aurora><hujianwei>20150718 add api end
    
}

