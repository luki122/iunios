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


public class QuickSettingSwitchPanel extends LinearLayout implements Configurable{
    private static final String TAG = "QuickSettingSwitchPanel";
    private static final boolean DBG = true;
    
    /** Minimum brightness at which the indicator is shown at half-full and ON */
    private static final float HALF_BRIGHTNESS_THRESHOLD = 0.3f;
    /** Minimum brightness at which the indicator is shown at full */
    private static final float FULL_BRIGHTNESS_THRESHOLD = 0.8f;
    
    private boolean mAutomaticAvailable;
    
    private boolean mUpdating = false;
    
    private static final int COUNT = 5;

    private Context mContext;
    private ToolBarView mToolBarView;
    
    private ConfigurationIconView mWifiIcon;
    private ConfigurationIconView mMobileIcon;
    private ConfigurationIconView mSilentIcon;
    private ConfigurationIconView mBrightnessIcon;
    private ConfigurationIconView mAutoRatationIcon;
    
    private Drawable mIndicatorView;

	private ActiviyCallback mActiviyCallback;
	
    private WifiStateTracker mWifiStateTracker;
    private MobileStateTracker mMobileStateTracker;
    private SilentStateTracker mSilentStateTracker;
    private BrightnessStateTracker mBrightnessStateTracker;
    private AutoRotationStateTracker mAutoRotationStateTracker;
    
    private ContentObserver mMobileStateForSingleCardChangeObserver = new ContentObserver(new Handler()) {
        
        @Override
        public void onChange(boolean selfChange) {
            if (!isWifiOnlyDevice()) {
                mMobileStateTracker.onActualStateChange(mContext, null);
                mMobileStateTracker.setImageViewResources(mContext);
            }
        };
        
    };
    
    private ContentObserver mBrightnessChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mBrightnessStateTracker.onActualStateChange(mContext, null);
            mBrightnessStateTracker.setImageViewResources(mContext);
        }
    };
    
    private ContentObserver mBrightnessModeChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mBrightnessStateTracker.onActualStateChange(mContext, null);
            mBrightnessStateTracker.setImageViewResources(mContext);
        }
    };
    
    private ContentObserver mAutoRotationChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mAutoRotationStateTracker.onActualStateChange(mContext, null);
            mAutoRotationStateTracker.setImageViewResources(mContext);
        }
    };

    private ContentObserver mVibrateChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mSilentStateTracker.onActualStateChange(mContext, null);
            mSilentStateTracker.setImageViewResources(mContext);
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
            }
            
        }
    };
	
	public void setActiviyCallback(ActiviyCallback cb){
		mActiviyCallback = cb;
	}
	
    public QuickSettingSwitchPanel(Context context) {
        this(context, null);
    }

    public QuickSettingSwitchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public void setToolBar(ToolBarView toolBarView) {
        mToolBarView = toolBarView;
    }

    public void buildIconViews() {
        
        mWifiStateTracker = new WifiStateTracker();
        mMobileStateTracker = new MobileStateTracker();
        mSilentStateTracker = new SilentStateTracker(mContext);
        mBrightnessStateTracker = new BrightnessStateTracker();
        mAutoRotationStateTracker = new AutoRotationStateTracker();
        
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.aurora_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        mWifiIcon = ( ConfigurationIconView ) this.getChildAt(0);
        mMobileIcon = ( ConfigurationIconView ) this.getChildAt(1);
        mSilentIcon = ( ConfigurationIconView ) this.getChildAt(2);
        mBrightnessIcon = (ConfigurationIconView)this.getChildAt(3);
        mAutoRatationIcon = (ConfigurationIconView)this.getChildAt(4);
        
        mWifiIcon.setConfigName(R.string.toolbar_wlan);
        mMobileIcon.setConfigName(R.string.toolbar_mobile);
        mSilentIcon.setConfigName(R.string.toolbar_silent);
        mBrightnessIcon.setConfigName(R.string.toolbar_brightness);
        mAutoRatationIcon.setConfigName(R.string.toolbar_autorotate);
        
        mWifiIcon.setClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                WifiManager mWifiManager = ( WifiManager ) mContext.getSystemService(Context.WIFI_SERVICE);
                if (mWifiManager != null) {
                    int wifiApState = mWifiManager.getWifiApState();
                    if ((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
                            && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                        mWifiManager.setWifiApEnabled(null, false);
                    }
                }
                mWifiStateTracker.toggleState(mContext);

            }
        });
        mWifiIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_WIFI_SETTINGS);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mMobileIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mMobileStateTracker.toggleState(mContext);
                
            }
        });
        mMobileIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
            	// Aurora <tongyh> <2013-11-07> Whether the SIM card decided to respond to long press event begin
            	if (!hasSimAbsent()) {
            	    startSettingsActivity(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
					mActiviyCallback.finishActivity();
            	}
            	// Aurora <tongyh> <2013-11-07> Whether the SIM card decided to respond to long press event end
                return true;
            }
        });
        
        mSilentIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSilentStateTracker.toggleState(mContext);
                
            }
        });
        mSilentIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
            	startSettingsActivity("gn.com.android.audioprofile.action.AUDIO");
				mActiviyCallback.finishActivity();
                return true;
            }
        });

        mBrightnessIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBrightnessStateTracker.toggleState(mContext);
            }
        });
        mBrightnessIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        mAutoRatationIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoRotationStateTracker.toggleState(mContext);
            }
        });
        mAutoRatationIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_DISPLAY_SETTINGS);
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
                mContext.getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Global.MOBILE_DATA), true,
                        mMobileStateForSingleCardChangeObserver);
                mContext.getContentResolver().registerContentObserver(
                            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                            true, mBrightnessChangeObserver);
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                        true, mBrightnessModeChangeObserver);
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                        true, mAutoRotationChangeObserver);

                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING),
                        true, mVibrateChangeObserver);
				

                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
				
                mContext.registerReceiver(mIntentReceiver, filter);
            } else {
           		mContext.getContentResolver().unregisterContentObserver(mVibrateChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mMobileStateForSingleCardChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mBrightnessChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mBrightnessModeChangeObserver);
                mContext.getContentResolver().unregisterContentObserver(mAutoRotationChangeObserver);
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
                    return STATE_TURNING_OFF;
                case WifiManager.WIFI_STATE_ENABLING:
                    return STATE_TURNING_ON;
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
            if (mHasSim && !mIsAirlineMode && !mIsMmsOngoing && super.isClickable()) {
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
            cm.setMobileDataEnabled(!enabled);
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
                    cm.setMobileDataEnabled(!enabled);
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
    public void setSilentCallback(SilentCallBcak cb){
    	mSilentStateTracker.setCallBcak(cb);
    }
	
    private final class SilentStateTracker extends StateTracker {
    	private AuroraMuteAndVibrateLinkage muteAndVibrateLinkage;
    	AudioManager audioManager;
		private boolean mSilentState = false;
		private SilentCallBcak mSilentCallBcak = null;
		SilentStateTracker(Context context){
			audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			muteAndVibrateLinkage = new AuroraMuteAndVibrateLinkage(context,audioManager);
			mSilentState = muteAndVibrateLinkage.isSilent();
		}
		public void setCallBcak(SilentCallBcak cb){
			mSilentCallBcak = cb;
		}
        @Override
        public int getActualState(Context context) {
        	boolean mActualSilentState = muteAndVibrateLinkage.isSilent();
		    if(mActualSilentState){
				 return STATE_ENABLED;
		    }
			return STATE_DISABLED;
        }
        
        @Override
        public void onActualStateChange(Context context, Intent intent) {
        	setCurrentState(context, getActualState(context));
        }
        
        @Override
        public void requestStateChange(Context context, boolean desiredState) {
        	int state = STATE_DISABLED;
			mSilentState = desiredState;
        	if(desiredState){
				state = STATE_ENABLED;
        	}
			mSilentCallBcak.setVibrateState(context,muteAndVibrateLinkage.silentChecked(desiredState));
			setCurrentState(context, state);
			setImageViewResources(context);
        }
        
        @Override
        public int getDisabledResource() {
            return R.drawable.toolbar_silent_off;
        }
        
        @Override
        public int getEnabledResource() {
            return R.drawable.toolbar_silent_enable;
        }
        
        @Override
        public ImageView getImageButtonView() {
            // TODO Auto-generated method stub
            return mSilentIcon.getConfigView();
        }

        @Override
        public TextView getNameTextView() {
            return mSilentIcon.getConfigNameView();
        }
    }
    
    /**
     * Subclass of StateTracker to get/set Brightness state.
     */
    private final class BrightnessStateTracker extends StateTracker {
        
        @Override
        public int getActualState(Context context) {
            return STATE_ENABLED;
        }

        @Override
        protected void requestStateChange(Context context, final boolean desiredState) {
            setCurrentState(context, STATE_ENABLED);
        }

        @Override
        public void onActualStateChange(Context context, Intent intent) {
            setCurrentState(context, STATE_ENABLED);
        }

        @Override
        public int getDisabledResource() {
            return R.drawable.toolbar_brightness_off;
        }

        @Override
        public int getEnabledResource() {
            boolean brightnessMode = getBrightnessMode(mContext);
            int brightness = getBrightness(mContext);
            int res = R.drawable.toolbar_brightness_auto;
            if (brightnessMode) {
                res = R.drawable.toolbar_brightness_auto;
            } else {
                PowerManager power = ( PowerManager ) mContext.getSystemService(Context.POWER_SERVICE);
                int nFullBrightness = ( int ) (FULL_BRIGHTNESS_THRESHOLD * power.getMaximumScreenBrightnessSetting());
                int nMiddleBrightness = ( int ) (HALF_BRIGHTNESS_THRESHOLD * power.getMaximumScreenBrightnessSetting());
                if (brightness > nFullBrightness) {
                    res = R.drawable.toolbar_brightness_fully;
                }else if (brightness > nMiddleBrightness) {
                    res = R.drawable.toolbar_brightness_middle;
                } else {
                    res = R.drawable.toolbar_brightness_low;
                }
            }
            return res;
        }

        @Override
        public ImageView getImageButtonView() {
            return mBrightnessIcon.getConfigView();
        }

        @Override
        public void toggleState(Context context) {
            toggleBrightness(context);
        }

        @Override
        public TextView getNameTextView() {
            return mBrightnessIcon.getConfigNameView();
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
    
    /**
     * Increases or decreases the brightness.
     * 
     * @param context
     */
    private void toggleBrightness(Context context) {
        try {

            //Aurora <zhang_xin> <2013-10-10> modify for brightness begin
            /*IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                PowerManager pm = ( PowerManager ) context.getSystemService(Context.POWER_SERVICE);
                
                ContentResolver cr = context.getContentResolver();
                int brightness = Settings.System.getInt(cr,
                        Settings.System.SCREEN_BRIGHTNESS);
                int brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                //Only get brightness setting if available
                if (context.getResources().getBoolean(
                        com.aurora.R.bool.config_automatic_brightness_available)) {
                    brightnessMode = Settings.System.getInt(cr,
                            Settings.System.SCREEN_BRIGHTNESS_MODE);
                }

                // Rotate AUTO -> MINIMUM -> DEFAULT -> MAXIMUM
                // Technically, not a toggle...
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    brightness = pm.getMinimumScreenBrightnessSetting();
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                } else if (brightness < pm.getDefaultScreenBrightnessSetting()) {
                    brightness = pm.getDefaultScreenBrightnessSetting();
                } else if (brightness < pm.getMaximumScreenBrightnessSetting()) {
                    brightness = pm.getMaximumScreenBrightnessSetting();
                } else {
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
                    brightness = pm.getMinimumScreenBrightnessSetting();
                }

                if (context.getResources().getBoolean(
                        com.aurora.R.bool.config_automatic_brightness_available)) {
                    // Set screen brightness mode (automatic or manual)
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            brightnessMode);
                } else {
                    // Make sure we set the brightness if automatic mode isn't available
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                }
            }*/
            PowerManager power = ( PowerManager ) context.getSystemService(Context.POWER_SERVICE);
            int nMinimumBrightness = power.getMinimumScreenBrightnessSetting();
            int nMaximumBrightness = power.getMaximumScreenBrightnessSetting();
            int nDefaultBrightness = power.getDefaultScreenBrightnessSetting();
            
            if (power != null) {
                SensorManager mgr = ( SensorManager ) getContext().getSystemService(Context.SENSOR_SERVICE);
                mAutomaticAvailable = mgr.getDefaultSensor(Sensor.TYPE_LIGHT) != null;
                ContentResolver cr = context.getContentResolver();
                int brightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
                int brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                // Only get brightness setting if available
                if (mAutomaticAvailable) {
                    brightnessMode = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE);
                }
                // Rotate AUTO -> MINIMUM -> DEFAULT -> MAXIMUM
                // Technically, not a toggle...
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    brightness = nMinimumBrightness;
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                } else if (brightness < nDefaultBrightness) {
                    brightness = nDefaultBrightness;
                } else if (brightness < nMaximumBrightness) {
                    brightness = nMaximumBrightness;
                } else {
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
                    brightness = nMinimumBrightness;
                }

                if (mAutomaticAvailable) {
                    // Set screen brightness mode (automatic or manual)
                    Settings.System.putInt(context.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE, brightnessMode);
                } else {
                    // Make sure we set the brightness if automatic mode isn't available
                    brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
                }
                if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                    power.setBacklightBrightness(brightness);
                    Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, brightness);
                }
            }
            //Aurora <zhang_xin> <2013-10-10> modify for brightness end
        } catch (Exception e) {
            Log.d(TAG, "toggleBrightness: " + e);
        }
    }
    
    /**
     * Gets state of brightness.
     * 
     * @param context
     * @return true if more than moderately bright.
     */
    public static int getBrightness(Context context) {
        if (DBG) {
            Log.i(TAG, "getBrightness called.");
        }
        try {
            int brightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            return brightness;
        } catch (Exception e) {
            Log.d(TAG, "getBrightness: " + e);
        }
        return 0;
    }
    
    /**
     * Gets state of brightness mode.
     *
     * @param context
     * @return true if auto brightness is on.
     */
    private static boolean getBrightnessMode(Context context) {
        try {
            int brightnessMode = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            return brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Exception e) {
            Log.d(TAG, "getBrightnessMode: " + e);
        }
        return false;
    }
    
    public void initConfigurationState() {
        mWifiStateTracker.setImageViewResources(mContext);
        mSilentStateTracker.setImageViewResources(mContext);
        mBrightnessStateTracker.setImageViewResources(mContext);
        mAutoRotationStateTracker.setImageViewResources(mContext);
        
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
                mMobileIcon.getConfigView().setEnabled(mMobileStateTracker.isClickable());
                mMobileIcon.getConfigNameView().setEnabled(mMobileStateTracker.isClickable());
                mMobileStateTracker.setImageViewResources(mContext);
            }
        }
    }
    
    public void enlargeTouchRegion() {
        mWifiIcon.enlargeTouchRegion();
        mMobileIcon.enlargeTouchRegion();
        mSilentIcon.enlargeTouchRegion();
        mBrightnessIcon.enlargeTouchRegion();
        mAutoRatationIcon.enlargeTouchRegion();
    }
    
    public void updateResources(){
        mWifiIcon.setConfigName(R.string.toolbar_wlan);
        mMobileIcon.setConfigName(R.string.toolbar_mobile);
        mSilentIcon.setConfigName(R.string.toolbar_silent);
        mBrightnessIcon.setConfigName(R.string.toolbar_brightness);
        mAutoRatationIcon.setConfigName(R.string.toolbar_autorotate);
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
	
}
