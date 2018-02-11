/*
 * Copyright (C) 2007 Google Inc.
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

package com.android.systemui.usb;

//gionee wangyy 20120710 modify for CR00643075 begin
// import com.aurora.R;
import com.android.systemui.R;
//gionee wangyy 20120710 modify for CR00643075 end
import aurora.app.AuroraActivity;
import android.app.ActivityManager;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;

import java.util.List;
import com.android.systemui.SXlog;
import com.gionee.featureoption.FeatureOption;
//gionee wangyy 20120710 modify for CR00643075 begin
import android.provider.Settings;
import android.app.ProgressDialog;
import android.widget.CheckBox;
import android.os.SystemProperties;
//gionee wangyy 20120710 modify for CR00643075 end

// Gionee xiaolin 20121216 add for CR00746871 start
import aurora.widget.AuroraSwitch;
import android.widget.CompoundButton;
// Gionee xiaolin 20121216 add for CR00746871 end
/**
 * This activity is shown to the user for him/her to enable USB mass storage
 * on-demand (that is, when the USB cable is connected). It uses the alert
 * dialog style. It will be launched from a notification.
 */
public class UsbStorageActivity extends AuroraActivity
		//gionee guozj 20130204 modified for CR00765349 start
        // Gionee xiaolin 20121216 modify for CR00746871 start
//        implements View.OnClickListener, OnCancelListener , CompoundButton.OnCheckedChangeListener{
	implements View.OnClickListener, OnCancelListener{
	// / Gionee xiaolin 20121216 modify for CR00746871 end
		//gionee guozj 20130204 modified for CR00765349 end
    private static final String TAG = "UsbStorageActivity";

    private Button mMountButton;
    private Button mUnmountButton;
    private ProgressBar mProgressBar;
    private TextView mBanner;
    private TextView mMessage;
    private ImageView mIcon;
    //gionee wangyy 20120710 modify for CR00643075 begin
	// Gionee xiaolin 20121216 modify for CR00746871 start
    private AuroraSwitch mUsbCheck;
    private AuroraSwitch mDebugCheck;
	// Gionee xiaolin 20121216 modify for CR00746871 end
    private View mUsbView;
    private View mDebugView;
    private boolean mDebugState;
    private boolean mUsbStorageOpened = false;
    private static final String ADB_STATE_ON = "adb_state_on";
    private static final String ADB_STATE_OFF = "adb_state_off";
    //gionee wangyy 20120710 modify for CR00643075 end
    private StorageManager mStorageManager = null;
    private static final int DLG_CONFIRM_KILL_STORAGE_USERS = 1;
    private static final int DLG_ERROR_SHARING = 2;
    //gionee wangyy 20120710 modify for CR00643075 begin
    private static final int DLG_USB_STORAGE_OPENING = 3;
    private static final int DLG_ADB_WARN = 4;
    private static final int DLG_NO_SDCARD = 5;
    private ImageView mDiverView;
    private static final boolean mGNUsbUISupport = true;//SystemProperties.get("ro.gn.usb.ui.support").equals("yes");
    //gionee wangyy 20120710 modify for CR00643075 end
    static final boolean localLOGV = true;
    private boolean mDestroyed;
    private boolean mHasCheck = false;
    private boolean mSettingUMS = false;
    private int mAllowedShareNum = 0;
    private int mSharedCount = 0;

    // UI thread
    private Handler mUIHandler;

    // thread for working with the storage services, which can be slow
    private Handler mAsyncStorageHandler;

    /** Used to detect when the USB cable is unplugged, so we can call finish() */
    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_STATE)) {
                handleUsbStateChanged(intent);
            }
        }
    };

    /*
    * Check how many storages can be shared on the device.
    * It seems that the device supported SHARED SD need to check.
    */
    private boolean isSharable() {
        if (!FeatureOption.MTK_SHARED_SDCARD) {
			return true;
        }

        StorageVolume[] volumes = mStorageManager.getVolumeList();
        int allowedShareNum = 0;
        if (volumes != null) {
            SXlog.d(TAG, "isSharable - length:" + volumes.length);
            for (int i=0; i<volumes.length; i++) {
                if (volumes[i].allowMassStorage() && !volumes[i].isEmulated()) {
                    String pth = volumes[i].getPath();
                    String st = mStorageManager.getVolumeState(pth);
                    if (st != null) {
                        SXlog.d(TAG, "isSharable - allowMassStorage:" + volumes[i].allowMassStorage() + ", isEmulated:" + volumes[i].isEmulated());
                        /* Only count the number of the storage can be shared */
                        if ( !st.equals(Environment.MEDIA_UNMOUNTABLE) && !st.equals(Environment.MEDIA_NOFS) &&
                            !st.equals(Environment.MEDIA_REMOVED) && !st.equals(Environment.MEDIA_BAD_REMOVAL) ) {
                            allowedShareNum++;
                        }
                    }
                }
            }
        }
        SXlog.d(TAG, "isSharable - allowedShareNum:" + allowedShareNum);
        if (allowedShareNum == 0)
            return false;
        else
            return true;
    }

    private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            final boolean on = newState.equals(Environment.MEDIA_SHARED);
            SXlog.d(TAG, "onStorageStateChanged - on: " + on + ", mSettingUMS: " + mSettingUMS + ", path: " + path + ", oldState: " + oldState + ", newState: " + newState);

            if (mSettingUMS) {
                StorageVolume[] volumes = mStorageManager.getVolumeList();
                SXlog.d(TAG, "onStorageStateChanged - [UMS Enabled] volumes.length: " + volumes.length + ", path: " + path + ", volumes state: " + newState + ", mAllowedShareNum: " + mAllowedShareNum + ", mSharedCount: " + mSharedCount);

                if (FeatureOption.MTK_SHARED_SDCARD) {
                    if (on) {
                        mSharedCount++;
                        SXlog.d(TAG, "onStorageStateChanged - [SSD] mSharedCount: " + mSharedCount);

                        if (mAllowedShareNum == mSharedCount) {
                            SXlog.d(TAG, "onStorageStateChanged - [All Shared] mSharedCount: " + mSharedCount);
                            switchDisplay(on);
                        }
                    } else {
                        if (!isSharable()) {
                            finish();
                        }
                    }
                } else {
                    boolean haveShared = false;
                    for (int i = 0; i < volumes.length; i++) {
                        if (Environment.MEDIA_SHARED.equals(mStorageManager.getVolumeState(volumes[i].getPath()))) {
                            haveShared = true;
                            break;
                        }
                    }
                    SXlog.d(TAG, "onStorageStateChanged - haveShared: " + haveShared);
                    switchDisplay(haveShared);
                }
            } else {
                SXlog.d(TAG, "onStorageStateChanged - [UMS Disable] mSettingUMS: " + mSettingUMS + ", on: " + on);
                switchDisplay(on);

                /*
                * When no storage can be shared, just finish the usb storage activity.
                * It happened the device with shared SD.
                * 1. Insert SD card.
                * 2. Launch Usb storage activiy.
                * 3. Remove SD card.
                * 4. Usb storage activity should be gone auto.
                */
                if (!isSharable()) {
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            if (mStorageManager == null) {
                Log.w(TAG, "Failed to get StorageManager");
            }
        }

        mSettingUMS = mStorageManager.isUsbMassStorageEnabled();
        Log.w(TAG, "mSettingUMS=" + mSettingUMS);

        if (FeatureOption.MTK_SHARED_SDCARD) {
            StorageVolume[] volumes = mStorageManager.getVolumeList();
            if (volumes != null) {
                for (int i=0; i<volumes.length; i++) {
                    if (volumes[i].allowMassStorage() && !volumes[i].isEmulated()) {
                        String path = volumes[i].getPath();
                        String st = mStorageManager.getVolumeState(path);
                        if (st != null) {
                            /* Only count the number of the storage can be shared */
                            if ( !st.equals(Environment.MEDIA_UNMOUNTABLE) && !st.equals(Environment.MEDIA_NOFS) &&
                                !st.equals(Environment.MEDIA_REMOVED) && !st.equals(Environment.MEDIA_BAD_REMOVAL) ) {
                                mAllowedShareNum++;
                            }
                        }
                    }
                }
            }
        }

        mUIHandler = new Handler();

        HandlerThread thr = new HandlerThread("SystemUI UsbStorageActivity");
        thr.start();
        mAsyncStorageHandler = new Handler(thr.getLooper());

        //gionee wangyy 20120710 modify for CR00643075 begin
        if (!mGNUsbUISupport) {
        //gionee wangyy 20120710 modify for CR00643075 end
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        if (Environment.isExternalStorageRemovable()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        setContentView(com.aurora.R.layout.usb_storage_activity);

        mIcon = (ImageView) findViewById(android.R.id.icon);
        mBanner = (TextView) findViewById(com.aurora.R.id.banner);
        mMessage = (TextView) findViewById(android.R.id.message);

        mMountButton = (Button) findViewById(com.aurora.R.id.mount_button);
        mMountButton.setOnClickListener(this);
        mUnmountButton = (Button) findViewById(com.aurora.R.id.unmount_button);
        mUnmountButton.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        //gionee wangyy 20120710 modify for CR00643075 begin
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Aurora <zhanggp> <2013-10-18> modified for systemui begin
            setContentView(R.layout.aurora_usb_storage_activity);
			//setContentView(R.layout.zzzzz_gn_usb_storage_activity);
			// Aurora <zhanggp> <2013-10-18> modified for systemui end
			// Gionee xiaolin 20121216 modify for CR00746871 start
            mUsbCheck = (AuroraSwitch) findViewById(R.id.check_usb);
            mUsbCheck.setOnClickListener(this);
            mDebugCheck = (AuroraSwitch) findViewById(R.id.check_debug);
			// Gionee xiaolin 20121216 modify for CR00746871 end
            mDebugCheck.setOnClickListener(this);
            mUsbView = (View) findViewById(R.id.layout_usb);
            mUsbView.setOnClickListener(this);
            mDebugView = (View) findViewById(R.id.layout_debug);
            mDebugView.setOnClickListener(this);
            mDiverView = (ImageView)findViewById(R.id.diver_debug);
            //gionee guozj 20130204 modified for CR00765749 start
            mUsbCheck.setClickable(false);
            mDebugCheck.setClickable(false);
            //mUsbCheck.setOnCheckedChangeListener(this);
            //mDebugCheck.setOnCheckedChangeListener(this);
            //gionee guozj 20130204 modified for CR00765749 end
        } 
        //gionee wangyy 20120710 modify for CR00643075 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAsyncStorageHandler.getLooper().quit();
        mDestroyed = true;
    }

    private void switchDisplay(final boolean usbStorageInUse) {
        // Gionee xiaolin 20120820 modify for CR00674062 start
        /*
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                switchDisplayAsync(usbStorageInUse);
            }
        });
        */
        Runnable action = new Runnable() {
            @Override
            public void run() {
                switchDisplayAsync(usbStorageInUse);
            }
        };
        if (usbStorageInUse) {
            mUIHandler.postDelayed(action, 600);
        } else {
            mUIHandler.post(action);
        }
        // Gionee xiaolin 20120820 modify for CR00674062 end
    }

    private void switchDisplayAsync(boolean usbStorageInUse) {
        if (usbStorageInUse) {
            SXlog.d(TAG, "switchDisplayAsync - [Mount] usbStorageInUse:  " + usbStorageInUse);
            //gionee wangyy 20120710 modify for CR00643075 begin
            if (!mGNUsbUISupport) {
            //gionee wangyy 20120710 modify for CR00643075 end
            mProgressBar.setVisibility(View.GONE);
            mUnmountButton.setVisibility(View.VISIBLE);
            mMountButton.setVisibility(View.GONE);
            mIcon.setImageResource(com.aurora.R.drawable.usb_android_connected);
            mBanner.setText(com.aurora.R.string.usb_storage_stop_title);
            mMessage.setText(com.aurora.R.string.usb_storage_stop_message);
            //gionee wangyy 20120710 modify for CR00643075 begin
            } else {
                mUsbCheck.setChecked(true);
                removeDialog(DLG_USB_STORAGE_OPENING);
                mUsbStorageOpened = true;
            }
            //gionee wangyy 20120710 modify for CR00643075 end
        } else {
            SXlog.d(TAG, "switchDisplayAsync - [Unmount] usbStorageInUse:  " + usbStorageInUse);
            //gionee wangyy 20120710 modify for CR00643075 begin
            if (!mGNUsbUISupport) {
            //gionee wangyy 20120710 modify for CR00643075 end
            mProgressBar.setVisibility(View.GONE);
            mUnmountButton.setVisibility(View.GONE);
            mMountButton.setVisibility(View.VISIBLE);
            mIcon.setImageResource(com.aurora.R.drawable.usb_android);
            mBanner.setText(com.aurora.R.string.usb_storage_title);
            mMessage.setText(com.aurora.R.string.usb_storage_message);
            //gionee wangyy 20120710 modify for CR00643075 begin
            } else {
                mUsbCheck.setChecked(false);
                removeDialog(DLG_USB_STORAGE_OPENING);
                mUsbStorageOpened = false;
            }
            //gionee wangyy 20120710 modify for CR00643075 end
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //gionee wangyy 20120710 modify for CR00643075 begin
        if (mGNUsbUISupport) {
            mDebugState = Settings.Secure.getInt(getContentResolver(),Settings.Secure.ADB_ENABLED, 0)!=0;
            boolean flagOfShownDebugView = Settings.Secure.getInt(getContentResolver(),"real_debug_state", 0) == 0;
            if (mDebugState) {
                if (!flagOfShownDebugView) {
                mDebugView.setVisibility(View.GONE);
                mDiverView.setVisibility(View.INVISIBLE);
            }
            mDebugCheck.setChecked(true);
            } else {
                if (flagOfShownDebugView) {
                    mDebugView.setVisibility(View.VISIBLE);
                    mDiverView.setVisibility(View.VISIBLE);
                }
                mDebugCheck.setChecked(false);
            }
        }
        //gionee wangyy 20120710 modify for CR00643075 end

        mHasCheck = false;
        mStorageManager.registerListener(mStorageListener);
        registerReceiver(mUsbStateReceiver, new IntentFilter(UsbManager.ACTION_USB_STATE));
        try {
            mAsyncStorageHandler.post(new Runnable() {
                @Override
                public void run() {
                    switchDisplay(mStorageManager.isUsbMassStorageEnabled());
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Failed to read UMS enable state", ex);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mUsbStateReceiver);
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }

    private void handleUsbStateChanged(Intent intent) {
        boolean connected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
        boolean isUMSmode = intent.getExtras().getBoolean(UsbManager.USB_FUNCTION_MASS_STORAGE);

        SXlog.d(TAG, "handleUsbStateChanged - connected:  " + connected + ", isUMSmode: " + isUMSmode);
        if (!connected || !isUMSmode) {
            /** If the USB cable was unplugged when UMS was enabled, set the UMS enable flag to false */
            if (mSettingUMS) {
                mSettingUMS = false;
                SXlog.d(TAG, "handleUsbStateChanged - [Unplug when UMS enabled] connected:  " + connected);
            }
            // It was disconnected from the plug, so finish
			if (FeatureOption.MTK_SHARED_SDCARD) {
				mSharedCount = 0;
			}
            finish();
        }
    }

    private IMountService getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
    	Log.i(TAG, "onCreateDialoge");
        switch (id) {

        case DLG_CONFIRM_KILL_STORAGE_USERS:
            //gionee wangyy 20120710 modify for CR00643075 begin
        	// Gionee xiaolin 20121216 modify for CR00746871 start
//            return new AlertDialog.Builder(this, AlertDialog.THEME_GIONEEVIEW_FULLSCREEN)
            // Gionee xiaolin 20121216 modify for CR00746871 end
        	//gionee guozj 20130130 modified for CR00769470 start
            AuroraAlertDialog comfirmKillStorageUserAlertDialog=new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.zzzzz_dlg_confirm_kill_storage_users_title)
                    .setPositiveButton(R.string.zzzzz_dlg_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	mHasCheck = false;
                            switchUsbMassStorage(true);
                        }})
                    .setNegativeButton(R.string.zzzzz_cancel, new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int which) {
                    		mHasCheck = false;
                          if (mGNUsbUISupport) {
                              mUsbCheck.setChecked(false);
                          }
                    	}})
                    .setMessage(R.string.zzzzz_dlg_confirm_kill_storage_users_text)
//                    .setOnCancelListener(this)
                    .create();
//        	comfirmKillStorageUserAlertDialog.setCancelable(true);
        	comfirmKillStorageUserAlertDialog.setCanceledOnTouchOutside(false);
        	return comfirmKillStorageUserAlertDialog;
        case DLG_ERROR_SHARING:
//            return new AlertDialog.Builder(this)
            AuroraAlertDialog errorSharingAlertDialog=new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.zzzzz_dlg_error_title)
                    .setNeutralButton(R.string.zzzzz_dlg_ok, null)
                    .setMessage(R.string.zzzzz_usb_storage_error_message)
//                    .setOnCancelListener(this)
                    .create();
            errorSharingAlertDialog.setCancelable(true);
            errorSharingAlertDialog.setCanceledOnTouchOutside(false);
            return errorSharingAlertDialog;
        //gionee wangyy 20120710 modify for CR00643075 end
        //gionee wangyy 20120710 modify for CR00643075 begin           
        case DLG_USB_STORAGE_OPENING:
		    // Gionee xiaolin 20121225 modify for CR00746871 start
            //return ProgressDialog.show(this, "", 
            //               getString(R.string.zzzzz_mount_sdcard_dialog_title));
			AuroraProgressDialog pd =  new AuroraProgressDialog(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
			pd.setMessage(getString(R.string.zzzzz_mount_sdcard_dialog_title));
			pd.setCancelable(true);
			pd.setCanceledOnTouchOutside(false);
			return pd;
			// Gionee xiaolin 20121225 modify for CR00746871  end
        case DLG_ADB_WARN:
        	// Gionee xiaolin 20121216 modify for CR00746871 start
            AuroraAlertDialog adbWarnAlertDialog=new AuroraAlertDialog.Builder(this,AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
//            return new AlertDialog.Builder(this, AlertDialog.THEME_GIONEEVIEW_FULLSCREEN).setMessage(
            // Gionee xiaolin 20121216 modify for CR00746871 end
                    .setMessage(getResources().getString(R.string.zzzzz_adb_warning_message))
                    .setTitle(R.string.zzzzz_adb_warning_title)
					// Gionee xiaolin 20121225 delete for CR00746871 start
                    //.setIcon(android.R.drawable.ic_dialog_alert)
					// Gionee xiaolin 20121225 delete for CR00746871 end
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mDebugCheck.setChecked(false);
                        }})
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
                            mDebugState = true;
                            mDebugCheck.setChecked(true);
                        }})
//                    .setOnCancelListener(this)
                    .create();
        	adbWarnAlertDialog.setCancelable(true);
        	adbWarnAlertDialog.setCanceledOnTouchOutside(false);
        	return adbWarnAlertDialog;
        case DLG_NO_SDCARD:
//            return  new AlertDialog.Builder(this).setMessage(
            AuroraAlertDialog noSDCardAlertDialog=new AuroraAlertDialog.Builder(this,AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setMessage(getResources().getString(R.string.zzzzz_no_sdcard_message))
                    .setTitle(R.string.zzzzz_no_sdcard_title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mUsbCheck.setChecked(false);
                        }})
                    .setOnCancelListener(this)
                    .create();
        	noSDCardAlertDialog.setCancelable(true);
        	noSDCardAlertDialog.setCanceledOnTouchOutside(false);
        	return noSDCardAlertDialog;
        //gionee wangyy 20120710 modify for CR00643075 end
        //gionee guozj 20130130 modified for CR00769470 end
        }
        return null;
    }

    private void scheduleShowDialog(final int id) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mDestroyed) {
                    removeDialog(id);
                    showDialog(id);
                }
            }
        });
    }

    private void switchUsbMassStorage(final boolean on) {
        // things to do on the UI thread
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                //gionee wangyy 20120710 modify for CR00643075 begin
                if (!mGNUsbUISupport) {
                //gionee wangyy 20120710 modify for CR00643075 end
                mUnmountButton.setVisibility(View.GONE);
                mMountButton.setVisibility(View.GONE);

                mProgressBar.setVisibility(View.VISIBLE);
                //gionee wangyy 20120710 modify for CR00643075 begin
                } else {
                    if (!mUsbStorageOpened) {
                        if (!isFinishing()) {
                            scheduleShowDialog(DLG_USB_STORAGE_OPENING);
                        }
                    }
                }
                //gionee wangyy 20120710 modify for CR00643075 end
                // will be hidden once USB mass storage kicks in (or fails)
            }
        });

        // things to do elsewhere
        mAsyncStorageHandler.post(new Runnable() {
            @Override
            public void run() {
				if (FeatureOption.MTK_SHARED_SDCARD) {
					mSharedCount = 0;
				}
                if (on) {
                    mSettingUMS = true;
                    mStorageManager.enableUsbMassStorage();
                } else {
                    mSettingUMS = false;
                    mStorageManager.disableUsbMassStorage();
                }
            }
        });
    }

    private void checkStorageUsers() {
        mAsyncStorageHandler.post(new Runnable() {
            @Override
            public void run() {
                checkStorageUsersAsync();
            }
        });
    }

    private void checkStorageUsersAsync() {
        IMountService ims = getMountService();
        if (ims == null) {
            // Display error dialog
            scheduleShowDialog(DLG_ERROR_SHARING);
        }
        String extStoragePath = Environment.getExternalStorageDirectory().toString();
        boolean showDialog = false;
        try {
            int[] stUsers = ims.getStorageUsers(extStoragePath);
            if (stUsers != null) {
                SXlog.d(TAG, "checkStorageUsersAsync - stUsers.length: " + stUsers.length);
            } else {
                SXlog.d(TAG, "checkStorageUsersAsync - [NO Storage Users]");
            }
            if (stUsers != null && stUsers.length > 0) {
                showDialog = true;
            } else {
                // List of applications on sdcard.
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<ApplicationInfo> infoList = am.getRunningExternalApplications();
                if (infoList != null) {
                    SXlog.d(TAG, "checkStorageUsersAsync - infoList.size(): " + infoList.size());
                } else {
                    SXlog.d(TAG, "checkStorageUsersAsync - [NO EXT RUNNING APPS]");
                }
                if (infoList != null && infoList.size() > 0) {
                    showDialog = true;
                }
            }
        } catch (RemoteException e) {
            // Display error dialog
            scheduleShowDialog(DLG_ERROR_SHARING);
        }
        if (showDialog) {
            SXlog.d(TAG, "checkStorageUsersAsync - [SHOW DIALOG] showDialog: " + showDialog);
            // Display dialog to user
            scheduleShowDialog(DLG_CONFIRM_KILL_STORAGE_USERS);
        } else {
            SXlog.d(TAG, "checkStorageUsersAsync - [NO DIALOG] showDialog: " + showDialog);
            if (localLOGV) Log.i(TAG, "Enabling UMS");
            switchUsbMassStorage(true);
        }
    }

    public void onClick(View v) {
    	Log.i(TAG, "onClickaa"+ mHasCheck);
        //gionee wangyy 20120710 modify for CR00643075 begin
        if (!mGNUsbUISupport) {
        //gionee wangyy 20120710 modify for CR00643075 end
        if (v == mMountButton) {
           // Check for list of storage users and display dialog if needed.

            if(false == mHasCheck) {
                Log.i(TAG, "onClick"+ mHasCheck);
                mHasCheck = true;
                if (FeatureOption.MTK_SHARED_SDCARD) {
                    /*No storage can be shared. Change UI directedly.*/
                    if (mAllowedShareNum == 0)
                        switchDisplay(true);
                    else
                        checkStorageUsers();
                } else
                    checkStorageUsers();
            }
        } else if (v == mUnmountButton) {
            if (localLOGV) Log.i(TAG, "Disabling UMS");
            mHasCheck = false;
            switchUsbMassStorage(false);
        }
		  //gionee wangyy 20120710 modify for CR00643075 begin
        ////gionee guozj 20130130 modified for CR00769470 start
        } else {
            if (v == mDebugView){			// || v == mDebugCheck
                if (mDebugState) {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
                    mDebugCheck.setChecked(false);
                    mDebugState = false;
                } else {
//                    mDebugCheck.setChecked(true);
                    scheduleShowDialog(DLG_ADB_WARN);
                }
            } else if (v == mUsbView) {			// || v == mUsbCheck
                if (!mUsbStorageOpened) {
                    String externalStorageState = Environment.getExternalStorageState();
                    if (externalStorageState.equals(Environment.MEDIA_REMOVED) || externalStorageState.equals(Environment.MEDIA_BAD_REMOVAL) || externalStorageState.equals(Environment.MEDIA_CHECKING)) {
                        scheduleShowDialog(DLG_NO_SDCARD);
                        return;
                    }
//                    mUsbCheck.setChecked(true);
                    if(false == mHasCheck) {
                        Log.i(TAG, "onClick"+ mHasCheck);
//                        mHasCheck = true;
                        checkStorageUsers();
                   }
            } else {
                 if (localLOGV) Log.i(TAG, "Disabling UMS");
//                 mHasCheck = false;
                 switchUsbMassStorage(false);
            }
       }
       }
	    //gionee guozj 20130117 modified for CR00769470 end
		// Gionee xiaolin 20121216 delete for CR00746871 end
    }

    public void onCancel(DialogInterface dialog) {
        //gionee wangyy 20120710 modify for CR00643075 begin
        if (mGNUsbUISupport) {
            mUsbCheck.setChecked(false);
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) == 0) {
                mDebugCheck.setChecked(false);
            }
        }
        //gionee wangyy 20120710 modify for CR00643075 end
        finish();
    }
    //gionee guozj 20130204 deleted for CR00765349 start
    // Gionee xiaolin 20121216 add for CR00746871 start
    /*
    @Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.check_usb:
			if (isChecked) {
				if (!mUsbStorageOpened) {
					mHasCheck = true;
					checkStorageUsers();
				}
			} else {
				if (mUsbStorageOpened) {
					mHasCheck = false;
					switchUsbMassStorage(false);
				}
			}
			break;
		case R.id.check_debug:
			if (isChecked) {
				if (!mDebugState) {
					scheduleShowDialog(DLG_ADB_WARN);
				}
			} else {
				if (mDebugState) {
					Settings.Secure.putInt(getContentResolver(),
							Settings.Secure.ADB_ENABLED, 0);
					mDebugState = false;
				}
			}
			break;
		}
	}*/
	// Gionee xiaolin 20121216 add for CR00746871 end
    //gionee guozj 20130204 deleted for CR00765349 end
}
