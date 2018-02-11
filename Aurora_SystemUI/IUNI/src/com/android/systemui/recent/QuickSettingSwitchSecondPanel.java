package com.android.systemui.recent;

import com.android.systemui.recent.utils.Configurable;
import com.android.systemui.recent.utils.StateTracker;

import android.app.ActivityManagerNative;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.systemui.R;

import android.media.AudioManager;
import com.android.systemui.recent.ToolBarView.SilentCallBcak;
import com.android.systemui.recent.ToolBarView.ActiviyCallback;


public class QuickSettingSwitchSecondPanel extends LinearLayout implements Configurable{
    private static final String TAG = "QuickSettingSwitchSecondPanel";
    private static final boolean DBG = true;
    
    private boolean mUpdating = false;
    
    private static final int COUNT = 5;

    private Context mContext;
    private ToolBarView mToolBarView;
    
    private ConfigurationIconView mVibrateIcon;
    private ConfigurationIconView mAirplaneModeIcon;
    private ConfigurationIconView mGpsIcon;
    private ConfigurationIconView mBluetoothIcon;
    private ConfigurationIconView mElectricTorchIcon;
    
    private Drawable mIndicatorView;

	private ActiviyCallback mActiviyCallback;
	
    private VibrateStateTracker mVibrateStateTracker;
    private AirplaneModeStateTracker mAirplaneModeStateTracker;
    private GpsStateTracker mGpsStateTracker;
    private BluetoothStateTracker mBluetoothStateTracker;
    private ElectricTorchStateTracker mElectricTorchStateTracker;
    
    private int mServiceState;
    
    private static final String ACTION_OPEN_FLASH = "com.aurora.open.flash";
    
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        
        public void onServiceStateChanged(android.telephony.ServiceState serviceState) {
            Log.i(TAG, "PhoneStateListener1.onServiceStateChanged: serviceState="+serviceState);
            mServiceState = serviceState.getState();
            onAirplaneModeChanged();
        };
    };
    
    private ContentObserver mVibrateChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mVibrateStateTracker.onActualStateChange(mContext, null);
            mVibrateStateTracker.setImageViewResources(mContext);
        }
    };
	
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver(){
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DBG) {
                Log.i(TAG, "onReceive called, action is " + action);
            }
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                mBluetoothStateTracker.onActualStateChange(context, intent);
                mBluetoothStateTracker.setImageViewResources(context);
            } else if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                mGpsStateTracker.onActualStateChange(context, intent);
                mGpsStateTracker.setImageViewResources(context);
            } else if (action.equals(ACTION_OPEN_FLASH)) {
                mElectricTorchStateTracker.onActualStateChange(context, intent);
                mElectricTorchStateTracker.setImageViewResources(context);
            }
        }
    };

    /**
     * Called when we've received confirmation that the airplane mode was set.
     */
    private void onAirplaneModeChanged() {
        boolean airplaneModeEnabled = isAirplaneModeOn(mContext);

        if (airplaneModeEnabled) {
            if (mServiceState != ServiceState.STATE_POWER_OFF) {
                Log.i(TAG, "Unfinish! serviceState:" + mServiceState);
                return;
            }
        }
        Log.i(TAG, "onServiceStateChanged called, inAirplaneMode is: " + airplaneModeEnabled);
        Intent intent = new Intent();
        intent.putExtra("state", airplaneModeEnabled);
        mAirplaneModeStateTracker.onActualStateChange(mContext, intent);
        mAirplaneModeStateTracker.setImageViewResources(mContext);
    }
	public void setActiviyCallback(ActiviyCallback cb){
		mActiviyCallback = cb;
	}
	
    public QuickSettingSwitchSecondPanel(Context context) {
        this(context, null);
    }

    public QuickSettingSwitchSecondPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public void setToolBar(ToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }

    public void buildIconViews() {
        
        mVibrateStateTracker = new VibrateStateTracker(mContext);
        mAirplaneModeStateTracker = new AirplaneModeStateTracker();
        mGpsStateTracker = new GpsStateTracker();
        mBluetoothStateTracker = new BluetoothStateTracker();
        mElectricTorchStateTracker = new ElectricTorchStateTracker();
        
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.aurora_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        mVibrateIcon = ( ConfigurationIconView ) this.getChildAt(0);
        mAirplaneModeIcon = ( ConfigurationIconView ) this.getChildAt(1);
        mGpsIcon = ( ConfigurationIconView ) this.getChildAt(2);
        mBluetoothIcon = (ConfigurationIconView)this.getChildAt(3);
        mElectricTorchIcon = (ConfigurationIconView)this.getChildAt(4);
        
        mVibrateIcon.setConfigName(R.string.toolbar_vibrate);
        mAirplaneModeIcon.setConfigName(R.string.toolbar_airplanemode);
        mGpsIcon.setConfigName(R.string.toolbar_gps);
        mBluetoothIcon.setConfigName(R.string.toolbar_bluetooth);
        mElectricTorchIcon.setConfigName(R.string.toolbar_electric_torch);
        
        mVibrateIcon.setClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVibrateStateTracker.toggleState(mContext);
            }
        });
        mVibrateIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
            	startSettingsActivity("gn.com.android.audioprofile.action.AUDIO");
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mAirplaneModeIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.i("ClickEvent", "AirPlane button click");
                mAirplaneModeStateTracker.setAirPlaneModeClickable(false);
                mAirplaneModeStateTracker.toggleState(mContext);
                postDelayed(new Runnable() {
                    public void run() {
                        mAirplaneModeStateTracker.setAirPlaneModeClickable(true);
                        mAirplaneModeStateTracker.getImageButtonView().setEnabled(mAirplaneModeStateTracker.isClickable());
                    }
                }, 600);
            }
        });
        mAirplaneModeIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
                startSettingsActivity(intent);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mGpsIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mGpsStateTracker.toggleState(mContext);
                
            }
        });
        mGpsIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mActiviyCallback.finishActivity();
                return true;
            }
        });

        mBluetoothIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothStateTracker.toggleState(mContext);
            }
        });
        mBluetoothIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mElectricTorchIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mElectricTorchStateTracker.toggleState(mContext);
            }
        });
        mElectricTorchIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }
    
    @Override
    protected int getSuggestedMinimumWidth() {
        // makes the large background bitmap not force us to full width
        return 0;
    }

    void setUpdates(boolean update) {
        if (update != mUpdating) {
            mUpdating = update;
            if (update) {

                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING),
                        true, mVibrateChangeObserver);

                final TelephonyManager tm = ( TelephonyManager ) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
                
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                filter.addAction(ACTION_OPEN_FLASH);
				filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                mContext.registerReceiver(mIntentReceiver, filter);
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
                mContext.getContentResolver().unregisterContentObserver(mVibrateChangeObserver);
                final TelephonyManager tm = ( TelephonyManager ) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }
    }

    /**
     * Subclass of StateTracker to get/set Vibrate state.
     */
    public void setVibrateState(Context context,boolean enable){
    	mVibrateStateTracker.requestStateChange(context,enable);
    }
    private final class VibrateStateTracker extends StateTracker {
		private AuroraMuteAndVibrateLinkage muteAndVibrateLinkage;
		AudioManager audioManager;
        private boolean mVibrateState = false;

		VibrateStateTracker(Context context){
			audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			muteAndVibrateLinkage = new AuroraMuteAndVibrateLinkage(context,audioManager);
			mVibrateState = muteAndVibrateLinkage.isVibrate();
		}

        @Override
        public int getActualState(Context context) {
        	boolean mActualVibrateState = muteAndVibrateLinkage.isVibrate();
		    if(mActualVibrateState){
				 return STATE_ENABLED;
		    }
			return STATE_DISABLED;

        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
        	setCurrentState(context, getActualState(context));
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
        	int state = STATE_DISABLED;
			mVibrateState = desiredState;
        	if(desiredState){
				state = STATE_ENABLED;
        	}
			muteAndVibrateLinkage.vibrateChecked(desiredState);
			setCurrentState(context, state);
			setImageViewResources(context);
        }

        public int getDisabledResource() {
            return R.drawable.toolbar_vibrate_off;
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_vibrate_enable;
        }

        public ImageView getImageButtonView() {
            return mVibrateIcon.getConfigView();
        }

        @Override
        public TextView getNameTextView() {
            return mVibrateIcon.getConfigNameView();
        }
    }
    
    /**
     * Subclass of StateTracker for Airplane Mode state.
     */
    private final class AirplaneModeStateTracker extends StateTracker {
        private boolean mAirPlaneModeClickable = true;
        
        public void setAirPlaneModeClickable(boolean enable) {
            if (DBG) {
                Log.i(TAG, "setAirPlaneModeClickable called, enabled is: " + enable);
            }
            mAirPlaneModeClickable = enable;
        }

        @Override
        public int getActualState(Context context) {
            return isAirplaneModeOn(mContext) ? STATE_ENABLED : STATE_DISABLED;
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            boolean enabled = intent.getBooleanExtra("state", false);
            setCurrentState(context, enabled ? STATE_ENABLED : STATE_DISABLED);
        }
        
        @Override
        public void toggleState(Context context) {
            if (getIsUserSwitching()) {
                Log.i(TAG, "toggleState user is swithing, so just return");
                return;
            }
            boolean airlineMode = isAirplaneModeOn(mContext);
            setIsUserSwitching(true);
            getImageButtonView().setEnabled(isClickable());
            Log.i(TAG, "Airplane toogleState: " + isClickable() + ", current airlineMode is " + airlineMode);
            Settings.Global.putInt(
                    mContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON,
                    airlineMode ? 0 : 1);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra("state", !airlineMode);
            mContext.sendBroadcast(intent);
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            // Do nothing, for we have done all operation in toggleState
        }

        public int getDisabledResource() {
            return R.drawable.toolbar_flight_mode_off;
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_flight_mode_on;
        }

        public ImageView getImageButtonView() {
            return mAirplaneModeIcon.getConfigView();
        }
        
        public boolean isClickable() {
            Log.i(TAG, "mAirPlaneModeClickable is " + mAirPlaneModeClickable + " super.isClickable is " + super.isClickable());
            return mAirPlaneModeClickable && super.isClickable();
        }

        @Override
        public TextView getNameTextView() {
            return mAirplaneModeIcon.getConfigNameView();
        }
    }
    
    /**
     * Subclass of StateTracker for GPS state.
     */
    private final class GpsStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            ContentResolver resolver = context.getContentResolver();
            boolean on = Settings.Secure.isLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER);
            return on ? STATE_ENABLED : STATE_DISABLED;
        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
            // Note: the broadcast location providers changed intent
            // doesn't include an extras bundles saying what the new value is.
            setCurrentState(context, getActualState(context));
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            final ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... args) {
                    Settings.Secure.setLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER, desiredState);
                    return desiredState;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    setCurrentState(context, result ? STATE_ENABLED : STATE_DISABLED);
                    setImageViewResources(context);
                }
            }.execute();
        }

        public int getDisabledResource() {
            return R.drawable.toolbar_gps_off;
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_gps_enable;
        }

        public ImageView getImageButtonView() {
            return mGpsIcon.getConfigView();
        }

        @Override
        public TextView getNameTextView() {
            return mGpsIcon.getConfigNameView();
        }
    }
    
    /**
     * Subclass of StateTracker to get/set Bluetooth state.
     */
    private final class BluetoothStateTracker extends StateTracker {
        
        @Override
        public int getActualState(Context context) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                return STATE_DISABLED;
            }
            return bluetoothStateToFiveState(bluetoothAdapter.getState());
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                setCurrentState(context, STATE_DISABLED);
                return;
            }
            // Actually request the Bluetooth change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    if (desiredState) {
                        bluetoothAdapter.enable();
                    } else {
                        bluetoothAdapter.disable();
                    }
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                return;
            }
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            setCurrentState(context, bluetoothStateToFiveState(bluetoothState));
        }

        /**
         * Converts BluetoothAdapter's state values into our
         * Wifi/Bluetooth-common state values.
         */
        private int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_OFF:
                    return STATE_DISABLED;
                case BluetoothAdapter.STATE_ON:
                    return STATE_ENABLED;
                case BluetoothAdapter.STATE_TURNING_ON:
                    return STATE_TURNING_ON;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return STATE_TURNING_OFF;
                default:
                    return STATE_UNKNOWN;
            }
        }

        public int getDisabledResource() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                return R.drawable.toolbar_bluetooth_disable;
            }
            return R.drawable.toolbar_bluetooth_off;
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_bluetooth_enable;
        }

        public ImageView getImageButtonView() {
            return mBluetoothIcon.getConfigView();
        }
        
        @Override
        public ImageView getSwitchingGifView() {
            return mBluetoothIcon.getSwitchingGifView();
        }

        @Override
        public int getInterMedateResource() {
            return R.drawable.bt_switch_anim;
        }
        
        @Override
        public TextView getNameTextView() {
            return mBluetoothIcon.getConfigNameView();
        }
    }

    /**
     * Subclass of StateTracker for AutoRotation state.
     */
    private final class ElectricTorchStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            return Flash.isFlashOn() ? STATE_ENABLED : STATE_DISABLED;
        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
            Flash.opFlash();
            setCurrentState(context, getActualState(context));
        }

        @Override
        public void toggleState(Context context) {
            int state = getActualState(context);
            Intent intent = new Intent(ACTION_OPEN_FLASH);
            intent.putExtra("state", state);
            context.sendBroadcast(intent);
        }
        
        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            Flash.opFlash();
        }

        public int getDisabledResource() {
            return R.drawable.toolbar_flash_off;
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_flash_on;
        }

        public ImageView getImageButtonView() {
            return mElectricTorchIcon.getConfigView();
        }

        @Override
        public TextView getNameTextView() {
            return mElectricTorchIcon.getConfigNameView();
        }
    }
    
    public void initConfigurationState() {
        mVibrateStateTracker.setImageViewResources(mContext);
        mAirplaneModeStateTracker.setImageViewResources(mContext);
        mGpsStateTracker.setImageViewResources(mContext);
        mBluetoothStateTracker.setImageViewResources(mContext);
        mElectricTorchStateTracker.setImageViewResources(mContext);
        
    }
    
    public void enlargeTouchRegion() {
        mVibrateIcon.enlargeTouchRegion();
        mAirplaneModeIcon.enlargeTouchRegion();
        mGpsIcon.enlargeTouchRegion();
        mBluetoothIcon.enlargeTouchRegion();
        mElectricTorchIcon.enlargeTouchRegion();
    }
    
    public void updateResources(){
        mVibrateIcon.setConfigName(R.string.toolbar_vibrate);
        mAirplaneModeIcon.setConfigName(R.string.toolbar_airplanemode);
        mGpsIcon.setConfigName(R.string.toolbar_gps);
        mBluetoothIcon.setConfigName(R.string.toolbar_bluetooth);
        mElectricTorchIcon.setConfigName(R.string.toolbar_electric_torch);
    }
    
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
    
    static class Flash {
        private static final String TAG = "LockFlash";
        private static Camera mCamera;
        private static Camera.Parameters mParameters;
        private static boolean mIsFlahsOn = false;

        public static boolean isFlashOn() {
            Log.d(TAG, "isFlashOn");
            return mIsFlahsOn;
        }

        private static void openFlash() {
            Log.d(TAG, "openFlash thread" + Thread.currentThread());
            try {
                if (mCamera == null && mParameters == null) {
                    mCamera = Camera.open();
                    mCamera.startPreview();
                    mParameters = mCamera.getParameters();
                }
                if (mCamera != null && mParameters != null) {
                    Log.d(TAG, "openFlash");
                    String currFlashMode = mParameters.getFlashMode();
                    if (currFlashMode == null
                            || (!currFlashMode
                                    .equals(Camera.Parameters.FLASH_MODE_TORCH))) {
                        mParameters
                                .setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(mParameters);
                        mIsFlahsOn = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void closeFlash() {
            Log.d(TAG, "closeFlash thread" + Thread.currentThread());
            if (mCamera != null && mParameters != null) {
                Log.d(TAG, "closeFlash");
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParameters);
                mCamera.release();
                mIsFlahsOn = false;
                mCamera = null;
                mParameters = null;
            }
        }

        public static void opFlash() {
            if (mIsFlahsOn) {
                Log.d(TAG, "opFlash1");
                closeFlash();
            } else {
                Log.d(TAG, "opFlash2");
                openFlash();
            }
        }
    }
    
    private void startSettingsActivity(String action) {
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }
    
    private void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }
    
    private void startSettingsActivity(Intent intent, boolean onlyProvisioned) {
//        if (onlyProvisioned && !getService().isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
//        getService().animateCollapsePanels();
    }
}
