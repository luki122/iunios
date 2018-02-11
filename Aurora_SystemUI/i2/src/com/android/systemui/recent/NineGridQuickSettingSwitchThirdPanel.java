package com.android.systemui.recent;

import com.android.systemui.recent.utils.Configurable;
import com.android.systemui.recent.utils.StateTracker;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
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
import android.os.Build;

import android.media.AudioManager;
import com.android.systemui.recent.NineGridToolBarView.SilentCallBcak;
import com.android.systemui.recent.NineGridToolBarView.ActiviyCallback;
// Steve.Tang 2014-07-18 start
import gionee.telephony.GnTelephonyManager;
import android.os.SystemProperties;
import android.content.ComponentName;

import com.android.systemui.totalCount.CountUtil;

// Steve.Tang 2014-07-18 end

public class NineGridQuickSettingSwitchThirdPanel extends LinearLayout implements Configurable{
    private static final String TAG = "NineGridQuickSettingSwitchThirdPanel";
    private static final boolean DBG = true;

	//Aurora <SteveTang> 2014-08-30, flag for new torch. start
	private static final boolean IUNI_SUPPORT_NEW_TORCH = true;
	//control torch on/off
	AuroraTorchController torchController;
	//Aurora <SteveTang> 2014-08-30, flag for new torch. end    

    /** Minimum brightness at which the indicator is shown at half-full and ON */
    private static final float HALF_BRIGHTNESS_THRESHOLD = 0.3f;
    /** Minimum brightness at which the indicator is shown at full */
    private static final float FULL_BRIGHTNESS_THRESHOLD = 0.8f;
    
    private static final String ACTION_OPEN_FLASH = "com.aurora.open.flash";
    
    private boolean mAutomaticAvailable;
    
    private boolean mUpdating = false;
    
    private static final int COUNT = 3;

    private static Context mContext;
    private NineGridToolBarView mToolBarView;
    
    private ConfigurationIconView mWifiIcon;
    private ConfigurationIconView mMobileIcon;
    private ConfigurationIconView mElectricTorchIcon;
    
    private Drawable mIndicatorView;

	private ActiviyCallback mActiviyCallback;
	
    private WifiStateTracker mWifiStateTracker;
    private MobileStateTracker mMobileStateTracker;
    private ElectricTorchStateTracker mElectricTorchStateTracker;

    Context mTorchContext;
    Intent  mTorchIntent;
    
    private TelephonyManager mPhone;

    private ContentObserver mMobileStateForSingleCardChangeObserver = new ContentObserver(new Handler()) {
        
        @Override
        public void onChange(boolean selfChange) {
            if (!isWifiOnlyDevice()) {
                mMobileStateTracker.onActualStateChange(mContext, null);
                mMobileStateTracker.setImageViewResources(mContext);
            }
        };
        
    };
    
    private Runnable mInitRunnable = new Runnable() {
        @Override
        public void run() {
            mElectricTorchStateTracker.onActualStateChange(mTorchContext, mTorchIntent);
        }
    };

    protected void registerPhoneStateListener(Context context) {
        // telephony
        mPhone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        mPhone.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }

   PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
			if (!isWifiOnlyDevice()) {
				boolean mSimCardReady = mPhone.getSimState() == TelephonyManager.SIM_STATE_READY;

		        if (mSimCardReady) {
		            mMobileStateTracker.setHasSim(true);
					// Steve.Tang 2014-07-18 if only insert sim card two, disable mobile icon, start
					mMobileIcon.setEnabled(mMobileStateTracker.isClickable());
		            // Steve.Tang 2014-07-18 if only insert sim card two, disable mobile icon, end
					mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
		            mMobileIcon.getConfigNameView().setEnabled(mMobileStateTracker.isClickable());
		            mMobileStateTracker.setImageViewResources(mContext);
		        }
			}
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
        }

        @Override
        public void onDataActivity(int direction) {
        }
    };




    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver(){
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DBG) {
                Log.i(TAG, "onReceive called, action is " + action);
            }
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                mWifiStateTracker.onActualStateChange(context, intent);
                mWifiStateTracker.setImageViewResources(context);
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                boolean enable = intent.getBooleanExtra("state", false);
                if (!isWifiOnlyDevice()) {
                    mMobileStateTracker.setAirlineMode(enable);
                    mMobileStateTracker.setImageViewResources(context);
                    mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
                }
            } else if (action.equals(ACTION_OPEN_FLASH)) {
//                mElectricTorchStateTracker.onActualStateChange(context, intent);
                mElectricTorchStateTracker.setImageViewResources(context);
                try {
                    mTorchContext = context;
                    mTorchIntent = intent;
                    Thread mInitThread = new Thread(mInitRunnable);
                    mInitThread.start();
                } catch (IllegalThreadStateException e) {
 
                }
            }
        }
    };
    
	public void setActiviyCallback(ActiviyCallback cb){
		mActiviyCallback = cb;
	}
	
    public NineGridQuickSettingSwitchThirdPanel(Context context) {
        this(context, null);
    }

    public NineGridQuickSettingSwitchThirdPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
		registerPhoneStateListener(context);
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
        
        mWifiStateTracker = new WifiStateTracker();
        mMobileStateTracker = new MobileStateTracker();
        mElectricTorchStateTracker = new ElectricTorchStateTracker();
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.aurora_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        mWifiIcon = ( ConfigurationIconView ) this.getChildAt(0);
        mMobileIcon = ( ConfigurationIconView ) this.getChildAt(1);
        mElectricTorchIcon = (ConfigurationIconView)this.getChildAt(2);
        
        mWifiIcon.setConfigName(R.string.toolbar_wlan);
        mMobileIcon.setConfigName(R.string.toolbar_mobile);
        mElectricTorchIcon.setConfigName(R.string.toolbar_electric_torch);
        
        mWifiIcon.setClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count Wifi button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_017, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count Wifi button click counts. end
            	mContext.sendBroadcast(new Intent("android.net.wifi.WIFI_STATEBAR_CHANGED"));
                WifiManager mWifiManager = ( WifiManager ) mContext.getSystemService(Context.WIFI_SERVICE);
                if (mWifiManager != null) {
                    int wifiApState = mWifiManager.getWifiApState();
                    if ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
                            && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                        mWifiManager.setWifiApEnabled(null, false);
                    }
                }
                //Aurora <tongyh> <2014-06-28> add wifi enabling and disabling does not respond to click begin
                if(!(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING || mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING)){
                	mWifiStateTracker.toggleState(mContext);
                }
                //Aurora <tongyh> <2014-06-28> add wifi enabling and disabling does not respond to click end

            }
        });
        mWifiIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_WIFI_SETTINGS);
                setStatusBarBG(false);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mMobileIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count mobiledata button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_018, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count mobiledata button click counts. end				
                mMobileStateTracker.toggleState(mContext);
                
            }
        });
        mMobileIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
            	// Aurora <tongyh> <2013-11-07> Whether the SIM card decided to respond to long press event begin
            	if (!hasSimAbsent()) {
					// Aurora <SteveTang> 2011-11-14, fix action not found. start
					startNetworkSettings();
            	    //startSettingsActivity(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
					// Aurora <SteveTang> 2011-11-14, fix action not found. end
                    setStatusBarBG(false);
					mActiviyCallback.finishActivity();
            	}
            	// Aurora <tongyh> <2013-11-07> Whether the SIM card decided to respond to long press event end
                return true;
            }
        });
        
        mElectricTorchIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count Torch button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_019, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count Torch button click counts. end
                if (mElectricTorchStateTracker.isFlashOn()) {
                    mElectricTorchStateTracker.setFlashOn(false);
                } else {
                    mElectricTorchStateTracker.setFlashOn(true);
                }

                mElectricTorchStateTracker.toggleState(mContext);
            }
        });
        mElectricTorchIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
		//Aurora <SteveTang> 2014-08-30, object to control data. start	
		torchController = new AuroraTorchController(mContext);
		//Aurora <SteveTang> 2014-08-30, object to control data. end
        
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
                        Settings.Secure.getUriFor(Settings.Global.MOBILE_DATA), true,
                        mMobileStateForSingleCardChangeObserver);
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
				filter.addAction(ACTION_OPEN_FLASH);
                mContext.registerReceiver(mIntentReceiver, filter);
            } else {
                mContext.getContentResolver().unregisterContentObserver(mMobileStateForSingleCardChangeObserver);
                mContext.unregisterReceiver(mIntentReceiver);
            }
        }
    }

    /**
     * Subclass of StateTracker to get/set Wifi state.
     */
    private final class WifiStateTracker extends StateTracker {
        
        private boolean mIsAirlineMode = false;
        
        public void setAirlineMode(boolean enable) {
            if (DBG) {
                Log.i(TAG, "Mobile setAirlineMode called, enabled is: " + enable);
            }
            mIsAirlineMode = enable;
        }
        
        public boolean isClickable() {
            Log.i(TAG, "wifi mIsAirlineMode is " + mIsAirlineMode + ", mIsUserSwitching is " + mIsUserSwitching);
            return !mIsAirlineMode && super.isClickable();
        }
        
        @Override
        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return STATE_DISABLED;
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
        	if(desiredState){
        		mWifiIcon.getConfigView().setImageResource(getEnabledResource());
        	}else{
        		mWifiIcon.getConfigView().setImageResource(getDisabledResource());
        	}
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.d(TAG, "No wifiManager.");
                setCurrentState(context, STATE_DISABLED);
                return;
            }

            // Actually request the wifi change and persistent
            // settings write off the UI thread, as it can take a
            // user-noticeable amount of time, especially if there's
            // disk contention.
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    /**
                     * Disable tethering if enabling Wifi
                     */
                    // delete these statement, from zte73 we support tether and wifi both eanbled
                    /*int wifiApState = wifiManager.getWifiApState();
                    if (desiredState && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                        wifiManager.setWifiApEnabled(null, false);
                    }*/
                    wifiManager.setWifiEnabled(desiredState);
                    return null;
                }
            }.execute();
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            setCurrentState(context, wifiStateToFiveState(wifiState));
        }

        @Override
        public int getDisabledResource() {
            return R.drawable.toolbar_wifi_off;
        }

        @Override
        public int getEnabledResource() {
            return R.drawable.toolbar_wifi_enable;
        }

        @Override
        public int getInterMedateResource() {
            return R.drawable.wifi_switch_anim;
        }
		

        @Override
        public ImageView getImageButtonView() {
            return mWifiIcon.getConfigView();
        }

        /**
         * Converts WifiManager's state values into our Wifi/Bluetooth-common
         * state values.
         */
        private int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    return STATE_DISABLED;
                case WifiManager.WIFI_STATE_ENABLED:
                    return STATE_ENABLED;
                case WifiManager.WIFI_STATE_DISABLING:
//                    return STATE_TURNING_OFF;
                	return STATE_DISABLED;
                case WifiManager.WIFI_STATE_ENABLING:
//                    return STATE_TURNING_ON;
                	return STATE_ENABLED;
                default:
                    return STATE_DISABLED;
            }
        }

        @Override
        public ImageView getSwitchingGifView() {
            return mWifiIcon.getSwitchingGifView();
        }

        @Override
        public TextView getNameTextView() {
            return mWifiIcon.getConfigNameView();
        }
    }

	// Aurora <Steve.tang> support dual card dual data connection. start
	private boolean isMultiSimEnable(){
		return GnTelephonyManager.isMultiSimEnabled();
	}

	private boolean isSupportDualDataConnection(){
		String proid = SystemProperties.get("ro.gn.gnprojectid", "8910");
		android.util.Log.e("xiuxiuxiu","project id: " + proid);
		return !proid.contains("8910");
		
	}

	private int getDefaultDataConnectionSub(){
		if(!isSupportDualDataConnection()) return 0;
		try{
			int subscription = Settings.Global.getInt(mContext.getContentResolver(),
		                Settings.Global.MULTI_SIM_DATA_CALL_SUBSCRIPTION);
			android.util.Log.e("xiuxiuxiu","Default data subscription: " + subscription);
			return subscription;
		} catch (android.provider.Settings.SettingNotFoundException e){
			return 0;
		}
	}

	private boolean isCanUseDataConnection(){
		if(isMultiSimEnable()){
			int defSubid = getDefaultDataConnectionSub();
			return GnTelephonyManager.hasIccCardGemini(defSubid) && 
						(GnTelephonyManager.getSimStateGemini(defSubid) == TelephonyManager.SIM_STATE_READY);
		}
		return true;
	}
	// Aurora <Steve.tang> support dual card dual data connection. end

    /**
     * M: Subclass of StateTracker for Mobile state.
     */
    private final class MobileStateTracker extends StateTracker {
        private boolean mGprsTargSim = false;
        private boolean mIsAirlineMode = false;
        private boolean mHasSim = false;
        private boolean mIsMmsOngoing = false;

        public void setHasSim(boolean enable) {
            mHasSim = enable;
        }

        public void setAirlineMode(boolean enable) {
            if (DBG) {
                Log.d(TAG, "Mobile setAirlineMode called, enabled is: " + enable);
            }
            mIsAirlineMode = enable;
        }

        public void setIsMmsOngoing(boolean enable) {
            mIsMmsOngoing = enable;
        }

        public void setIsUserSwitching(boolean enable) {
            mIsUserSwitching = enable;
        }

        public boolean getIsUserSwitching() {
            return mIsUserSwitching;
        }

        public boolean isClickable() {
            Log.d(TAG, "mobile mHasSim is " + mHasSim + ", mIsAirlineMode is " + mIsAirlineMode
                    + ", mIsMmsOngoing is " + mIsMmsOngoing + ", mIsUserSwitching is " + mIsUserSwitching);
            // Steve.Tang 2014-07-18 U3 only supprt slot one data connection, if only insert slot 2, disable data connection. start
			if (isCanUseDataConnection() && mHasSim && !mIsAirlineMode && !mIsMmsOngoing && super.isClickable()) {
			//if (mHasSim && !mIsAirlineMode && !mIsMmsOngoing && super.isClickable()) {
            // Steve.Tang 2014-07-18 U3 only supprt slot one data connection, if only insert slot 2, disable data connection. end
			    return true;
            } else {
                return false;
            }
        }

        @Override
        public int getActualState(Context context) {
            if (!mHasSim || mIsAirlineMode) {
                return STATE_DISABLED;
            }
            ConnectivityManager cm = ( ConnectivityManager ) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                return cm.getMobileDataEnabled() ? STATE_ENABLED : STATE_DISABLED;
            } else {
                return STATE_DISABLED;
            }
//            Log.d(TAG, "mobile:getActualState=" + cm.getMobileDataEnabled());
        }

        @Override
        public void toggleState(Context context) {
            switchDataConnectionMode();
        }

        private void switchDataConnectionMode() {
            ConnectivityManager cm = ( ConnectivityManager ) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean enabled = cm.getMobileDataEnabled();
            if (!isWifiOnlyDevice()) {
                mMobileStateTracker.setIsUserSwitching(true);
            }
            //update to 5.0 begin
//            cm.setMobileDataEnabled(!enabled);
            mPhone.setDataEnabled(!enabled);
            //update to 5.0 end
			// Aurora <Steve.Tang> 2014-08-14 fix 7575,  enable.disable data connection(U3), start
			Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.MOBILE_DATA + getDefaultDataConnectionSub(), !enabled ? 1 : 0);
			// Aurora <Steve.Tang> 2014-08-14 fix 7575,  enable.disable data connection(U3), end
            if (!enabled) {
                mGprsTargSim = true;
            } else {
                mGprsTargSim = false;
            }
            if (!isWifiOnlyDevice()) {
                mMobileStateTracker.getImageButtonView().setVisibility(View.GONE);
                int resId = mMobileStateTracker.getInterMedateResource();
                if (resId != -1) {
                    mMobileStateTracker.getSwitchingGifView().setImageResource(resId);
                    mMobileStateTracker.getSwitchingGifView().setVisibility(View.VISIBLE);
                }
                mMobileStateTracker.getImageButtonView().setEnabled(false);
            }
            AnimationDrawable mFrameDrawable = ( AnimationDrawable ) getSwitchingGifView().getDrawable();
            if (mFrameDrawable != null && !mFrameDrawable.isRunning()) {
                mFrameDrawable.start();
            }
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            int currentState = getActualState(context);
            if (DBG) {
                Log.d(TAG, "single card onActualStateChange called, currentState is " + currentState);
            }
            setCurrentState(context, currentState);

        }

        private int mobileStateToFiveState(Intent intent) {
            return STATE_UNKNOWN;
        }

        @Override
        public void requestStateChange(final Context context, final boolean desiredState) {
            final ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... args) {
                    ConnectivityManager cm = ( ConnectivityManager ) mContext
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    boolean enabled = cm.getMobileDataEnabled();
                    //update to 5.0 begin
//                    cm.setMobileDataEnabled(!enabled);
                    mPhone.setDataEnabled(!enabled);
                    //update to 5.0 end
					// Aurora <Steve.Tang> 2014-08-14 fix 7575,  enable.disable data connection(U3), start
					Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.MOBILE_DATA + getDefaultDataConnectionSub(), !enabled ? 1 : 0);
					// Aurora <Steve.Tang> 2014-08-14 fix 7575,  enable.disable data connection(U3), end
                    return null;
                }
            }.execute();
        }

        public int getDisabledResource() {
            if (isAirplaneModeOn(mContext) || !mHasSim) {
                return R.drawable.toolbar_mobile_disable;
            } else {
                return R.drawable.toolbar_mobile_off;
            }
        }

        public int getEnabledResource() {
            return R.drawable.toolbar_mobile_enable;
        }

        public ImageView getImageButtonView() {
            return mMobileIcon.getConfigView();
        }

        @Override
        public int getInterMedateResource() {
//            return R.drawable.mobile_switch_anim;
            return 0;
        }

        @Override
        public ImageView getSwitchingGifView() {
            return mMobileIcon.getSwitchingGifView();
        }

        @Override
        public TextView getNameTextView() {
            return mMobileIcon.getConfigNameView();
        }
    }


    /**
     * Subclass of StateTracker to get/set Silent state.
     */

    public void initConfigurationState() {
        mWifiStateTracker.setImageViewResources(mContext);
        
        if (!isWifiOnlyDevice()) {
            mMobileStateTracker.setAirlineMode(isAirplaneModeOn(mContext));
            mMobileStateTracker.setHasSim(false);
            mMobileStateTracker.setCurrentState(mContext, StateTracker.STATE_DISABLED);
            mMobileStateTracker.setImageViewResources(mContext);

            final TelephonyManager tm = ( TelephonyManager ) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            boolean mSimCardReady = tm.getSimState() == TelephonyManager.SIM_STATE_READY;
            if (mSimCardReady) {
                Log.d(TAG, "Oops, sim ready, maybe phone is drop down and restarted");
                mMobileStateTracker.setHasSim(true);
				// Steve.Tang 2014-07-18 if only insert sim card two, disable mobile icon, start
				mMobileIcon.setEnabled(mMobileStateTracker.isClickable());
                // Steve.Tang 2014-07-18 if only insert sim card two, disable mobile icon, end
				mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
                mMobileIcon.getConfigNameView().setEnabled(mMobileStateTracker.isClickable());
                mMobileStateTracker.setImageViewResources(mContext);
            }
        }
        mElectricTorchStateTracker.setImageViewResources(mContext);
    }
    
    public void enlargeTouchRegion() {
        mWifiIcon.enlargeTouchRegion();
        mMobileIcon.enlargeTouchRegion();
        mElectricTorchIcon.enlargeTouchRegion();
    }
    
    public void updateResources(){
        mWifiIcon.setConfigName(R.string.toolbar_wlan);
        mMobileIcon.setConfigName(R.string.toolbar_mobile);
        mElectricTorchIcon.setConfigName(R.string.toolbar_electric_torch);
    }
    
    /**
     * M: Used to check weather this device is wifi only.
     */
    private boolean isWifiOnlyDevice() {
        ConnectivityManager cm = ( ConnectivityManager ) getContext().getSystemService(
                mContext.CONNECTIVITY_SERVICE);
        return !(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE));
    }
    
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
	// Aurora <SteveTang> 2011-11-14, fix action not found. start
	private void startNetworkSettings(){
		Intent intent = new Intent();
		if(GnTelephonyManager.isMultiSimEnabled()){
			intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MSimMobileNetworkSettings"));
		} else {
			intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
		}
    	startSettingsActivity(intent);
	}
    // Aurora <SteveTang> 2011-11-14, fix action not found. end
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
//        	update to 5.0 begin
//            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
            ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn ();
        	//update to 5.0 end
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
//        getService().animateCollapsePanels();
    }
 // Aurora <tongyh> <2013-11-07> Whether the SIM card decided to respond to long press event begin
    private boolean hasSimAbsent(){
    	TelephonyManager manager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
    	int absent = manager.getSimState();
    	if(1 == absent){
    		return true;
    	}else{
    		return false;
    	}
    }
 // Aurora <tongyh> <2013-11-07> Whether the SIM card decided to respond to long press event  end
    /**
     * Subclass of StateTracker for AutoRotation state.
     */
    private final class ElectricTorchStateTracker extends StateTracker {

        @Override
        public int getActualState(Context context) {
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. start
	        //return Flash.isFlashOn() ? STATE_ENABLED : STATE_DISABLED;
			return (IUNI_SUPPORT_NEW_TORCH ? torchController.isTorchOn() : Flash.isFlashOn()) ? STATE_ENABLED : STATE_DISABLED;
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. end

        }

        @Override
        public void onActualStateChange(Context context, Intent unused) {
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. start
			//Flash.opFlash();
			if(IUNI_SUPPORT_NEW_TORCH){
				torchController.controlTorch();
			} else {
		        Flash.opFlash();
			}
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. end
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
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. start
            //Flash.opFlash();
			if(IUNI_SUPPORT_NEW_TORCH){
				torchController.controlTorch();
			} else {
		        Flash.opFlash();
			}
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. end
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

        public boolean isFlashOn() {
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. start
			//return Flash.isFlashOn();
            return IUNI_SUPPORT_NEW_TORCH ? torchController.isTorchOn() : Flash.isFlashOn();
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. end
        }

        public void setFlashOn(boolean isOn) {
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. start
			if(IUNI_SUPPORT_NEW_TORCH){
				torchController.changeTorchState(isOn);
			}else{
		        if (Flash.mIsOpenOrCloseing) {
		            return;
		        }
		        Flash.setFlashOn(isOn);
			}
			//Aurora <SteveTang> 2014-08-30, new method to control torch with flag. end
        }
    }
    
    static class Flash {
        private static final String TAG = "LockFlash";
        private static Camera mCamera;
        private static Camera.Parameters mParameters;
        private static boolean mIsFlahsOn = false;
        private static boolean mIsOpenOrCloseing = false;

        public static boolean isFlashOn() {
            Log.d(TAG, "isFlashOn");
            return mIsFlahsOn;
        }

        public static void setFlashOn(boolean isOn) {
            mIsFlahsOn = isOn;
        }

        private static void openFlash() {
//            String buildModel = Build.MODEL;
//            if (buildModel.equals("GT-I9500") || buildModel.contains("SM-N900")) {
//                Settings.System.putInt(mContext.getContentResolver(), "torch_light", 1); 
//                mIsFlahsOn = true;
//                return;
//            }

            if (mIsOpenOrCloseing) {
                return;
            }
            mIsOpenOrCloseing = true;

            Log.d(TAG, "openFlash thread" + Thread.currentThread());
            try {
                if (mCamera == null && mParameters == null) {
                    mCamera = Camera.open();
                    mCamera.startPreview();
                    mParameters = mCamera.getParameters();
                }
 //               Log.v("xiaoyong", "mCamera = " + mCamera + " mParameters = " + mParameters);
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
                mIsOpenOrCloseing = false;
                e.printStackTrace();
            }
             
            mIsOpenOrCloseing = false;
        }

        private static void closeFlash() {

//            String buildModel = Build.MODEL;
//            if (buildModel.equals("GT-I9500") || buildModel.contains("SM-N900")) {
//                Settings.System.putInt(mContext.getContentResolver(), "torch_light", 0);
//                mIsFlahsOn = false;
//                return;
//            }
            if (mIsOpenOrCloseing) {
                return;
            }
            mIsOpenOrCloseing = true;           

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
       
            mIsOpenOrCloseing = false;
        }

        public static void opFlash() {
            if (mIsFlahsOn) {
                Log.d(TAG, "opFlash1");
                openFlash();
            } else {
                Log.d(TAG, "opFlash2");
                closeFlash();
            }
        }
    }
}
