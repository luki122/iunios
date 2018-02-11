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
import com.android.systemui.recent.NineGridToolBarView.SilentCallBcak;
import com.android.systemui.recent.NineGridToolBarView.ActiviyCallback;

import com.android.systemui.totalCount.CountUtil;

public class NineGridQuickSettingSwitchFirstPanel extends LinearLayout implements Configurable{
    private static final String TAG = "NineGridQuickSettingSwitchFirstPanel";
    private static final boolean DBG = true;
    
    private boolean mUpdating = false;
    
    private static final int COUNT = 3;

    private Context mContext;
    private NineGridToolBarView mToolBarView;
    private ConfigurationIconView mAutoRatationIcon;
    
    private ConfigurationIconView mAirplaneModeIcon;
    private ConfigurationIconView mGpsIcon;
    
    private Drawable mIndicatorView;

	private ActiviyCallback mActiviyCallback;
	
    private AirplaneModeStateTracker mAirplaneModeStateTracker;
    private GpsStateTracker mGpsStateTracker;
    private AutoRotationStateTracker mAutoRotationStateTracker;
    
    private int mServiceState;
    
    private static final String ACTION_OPEN_FLASH = "com.aurora.open.flash";
    
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        
        public void onServiceStateChanged(android.telephony.ServiceState serviceState) {
            Log.i(TAG, "PhoneStateListener1.onServiceStateChanged: serviceState="+serviceState);
            mServiceState = serviceState.getState();
            onAirplaneModeChanged();
        };
    };
    
    
	
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver(){
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DBG) {
                Log.i(TAG, "onReceive called, action is " + action);
            }
            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                mGpsStateTracker.onActualStateChange(context, intent);
                mGpsStateTracker.setImageViewResources(context);
            }
        }
    };
    
    
    private ContentObserver mAutoRotationChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mAutoRotationStateTracker.onActualStateChange(mContext, null);
            mAutoRotationStateTracker.setImageViewResources(mContext);
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
	
    public NineGridQuickSettingSwitchFirstPanel(Context context) {
        this(context, null);
    }

    public NineGridQuickSettingSwitchFirstPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public void setToolBar(NineGridToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }

    public void setStatusBarBG(boolean isTransparent) {
        if (!isTransparent) {
            Intent StatusBarBGIntent = new Intent();
            StatusBarBGIntent.setAction("aurora.action.CHANGE_STATUSBAR_BG");
            StatusBarBGIntent.putExtra("transparent", false);
            mContext.sendBroadcast(StatusBarBGIntent);
        }
    }

    public void buildIconViews() {
        
        mAirplaneModeStateTracker = new AirplaneModeStateTracker();
        mGpsStateTracker = new GpsStateTracker();
        mAutoRotationStateTracker = new AutoRotationStateTracker();
        
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.aurora_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        mAirplaneModeIcon = ( ConfigurationIconView ) this.getChildAt(0);
        mGpsIcon = ( ConfigurationIconView ) this.getChildAt(1);
        mAutoRatationIcon = (ConfigurationIconView)this.getChildAt(2);
        
        mAirplaneModeIcon.setConfigName(R.string.toolbar_airplanemode);
        mGpsIcon.setConfigName(R.string.toolbar_gps);
        mAutoRatationIcon.setConfigName(R.string.toolbar_autorotate);
        
        mAirplaneModeIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.i("ClickEvent", "AirPlane button click");
                mAirplaneModeStateTracker.setAirPlaneModeClickable(false);
                mAirplaneModeStateTracker.toggleState(mContext);

				// Aurora <Steve.Tang> 2015-03-02, Count Airplane button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_011, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count Airplane button click counts. end

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

//                Intent StatusBarBGIntent = new Intent();
//                StatusBarBGIntent.setAction("aurora.action.CHANGE_STATUSBAR_BG");
//                StatusBarBGIntent.putExtra("transparent", false);
//                mContext.sendBroadcast(StatusBarBGIntent);
                setStatusBarBG(false);

				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mGpsIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count GPS button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_012, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count GPS button click counts. end
                mGpsStateTracker.toggleState(mContext);
                
            }
        });
        mGpsIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                setStatusBarBG(false);
				mActiviyCallback.finishActivity();
                return true;
            }
        });

        mAutoRatationIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count Rotation button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_013, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count Rotation button click counts. end
                mAutoRotationStateTracker.toggleState(mContext);
            }
        });
        mAutoRatationIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
                setStatusBarBG(false);
				mActiviyCallback.finishActivity();
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

                final TelephonyManager tm = ( TelephonyManager ) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
                
                IntentFilter filter = new IntentFilter();
                filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
                mContext.registerReceiver(mIntentReceiver, filter);
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                        true, mAutoRotationChangeObserver);
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
                final TelephonyManager tm = ( TelephonyManager ) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                mContext.getContentResolver().unregisterContentObserver(mAutoRotationChangeObserver);
            }
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
            setImageViewResources(mContext);
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
     * Subclass of StateTracker for AutoRotation state.
     */
    private final class AutoRotationStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
            int state = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1);
            if (state == 1) {
                return STATE_ENABLED;
            } else if (state == 0) {
                return STATE_DISABLED;
            } else {
                return STATE_UNKNOWN;
            }
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
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.ACCELEROMETER_ROTATION, desiredState ? 1 : 0);
                    return desiredState;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    setCurrentState(context, result ? STATE_ENABLED : STATE_DISABLED);
                }
            }.execute();
        }

        public int getDisabledResource() {
            return R.drawable.toolbar_auto_rotation_off;
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_auto_rotation_enable;
        }

        public ImageView getImageButtonView() {
            return mAutoRatationIcon.getConfigView();
        }

        @Override
        public TextView getNameTextView() {
            return mAutoRatationIcon.getConfigNameView();
        }
    }


    
    
    public void initConfigurationState() {
        mAirplaneModeStateTracker.setImageViewResources(mContext);
        mGpsStateTracker.setImageViewResources(mContext);
        mAutoRotationStateTracker.setImageViewResources(mContext);
        
    }
    
    public void enlargeTouchRegion() {
        mAirplaneModeIcon.enlargeTouchRegion();
        mGpsIcon.enlargeTouchRegion();
        mAutoRatationIcon.enlargeTouchRegion();
        
    }
    
    public void updateResources(){
        mAirplaneModeIcon.setConfigName(R.string.toolbar_airplanemode);
        mGpsIcon.setConfigName(R.string.toolbar_gps);
        mAutoRatationIcon.setConfigName(R.string.toolbar_autorotate);
    }
    
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
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
