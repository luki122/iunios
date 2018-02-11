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


public class NineGridQuickSettingSwitchSecondPanel extends LinearLayout implements Configurable{
    private static final String TAG = "NineGridQuickSettingSwitchSecondPanel";
    private static final boolean DBG = true;
    
    private boolean mUpdating = false;
    
    private static final int COUNT = 3;

    private Context mContext;
    private NineGridToolBarView mToolBarView;
    
    private ConfigurationIconView mSilentIcon;
    private ConfigurationIconView mVibrateIcon;
    private ConfigurationIconView mBluetoothIcon;
    
    private Drawable mIndicatorView;

	private ActiviyCallback mActiviyCallback;
	
	private SilentStateTracker mSilentStateTracker;
    private VibrateStateTracker mVibrateStateTracker;
    private BluetoothStateTracker mBluetoothStateTracker;
    
    private int mServiceState;
    private int mSaveMusicVolue;
    
    private ContentObserver mVibrateChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	mSilentStateTracker.onActualStateChange(mContext, null);
            mSilentStateTracker.setImageViewResources(mContext);
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
            }
        }
    };

  
	public void setActiviyCallback(ActiviyCallback cb){
		mActiviyCallback = cb;
	}
	
    public NineGridQuickSettingSwitchSecondPanel(Context context) {
        this(context, null);
    }

    public NineGridQuickSettingSwitchSecondPanel(Context context, AttributeSet attrs) {
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
    	
    	mSilentStateTracker = new SilentStateTracker(mContext);
        mVibrateStateTracker = new VibrateStateTracker(mContext);
        mBluetoothStateTracker = new BluetoothStateTracker();
        
        this.removeAllViews();

        LinearLayout.LayoutParams layutparams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        for (int i = 0; i < COUNT; i++) {
            ConfigurationIconView configIconView = (ConfigurationIconView) View.inflate(mContext, R.layout.aurora_toolbar_configuration_icon_view, null);
            configIconView.setOrientation(LinearLayout.VERTICAL);
            this.addView(configIconView, layutparams);
        }
        
        mSilentIcon = ( ConfigurationIconView ) this.getChildAt(0);
        mVibrateIcon = ( ConfigurationIconView ) this.getChildAt(1);
        mBluetoothIcon = (ConfigurationIconView)this.getChildAt(2);
        
        mSilentIcon.setConfigName(R.string.toolbar_silent);
        mVibrateIcon.setConfigName(R.string.toolbar_vibrate);
        mBluetoothIcon.setConfigName(R.string.toolbar_bluetooth);
        
        mVibrateIcon.setClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count Vibration button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_015, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count Vibration button click counts. end
                mVibrateStateTracker.toggleState(mContext);
            }
        });
        mVibrateIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
            	startSettingsActivity("gn.com.android.audioprofile.action.AUDIO");
                setStatusBarBG(false);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
        
        

        mBluetoothIcon.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count bluetooth button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_016, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count bluetooth button click counts. end

            	// Aurora <tongyh> <2014-01-15> slove bluetooth shortcuts keeps flashing begin
            	int state = mBluetoothStateTracker.getActualState(mContext);
            	if(state == 0 ||  state == 1){
            		mBluetoothStateTracker.toggleState(mContext);
            	}
            	//mBluetoothStateTracker.toggleState(mContext);
            	// Aurora <tongyh> <2014-01-15> slove bluetooth shortcuts keeps flashing end
            }
        });
        mBluetoothIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                setStatusBarBG(false);
				mActiviyCallback.finishActivity();
                return true;
            }
        });
        
       mSilentIcon.setClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
				// Aurora <Steve.Tang> 2015-03-02, Count Silent button click counts. start
				CountUtil.getInstance(mContext).update(CountUtil.COUNT_ITEM_014, 1);
				// Aurora <Steve.Tang> 2015-03-02, Count Silent button click counts. end
                mSilentStateTracker.toggleState(mContext);
                
            }
        });
        mSilentIcon.setLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
            	startSettingsActivity("gn.com.android.audioprofile.action.AUDIO");
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

                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.VIBRATE_WHEN_RINGING),
                        true, mVibrateChangeObserver);
                
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
				filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                mContext.registerReceiver(mIntentReceiver, filter);
            } else {
                mContext.unregisterReceiver(mIntentReceiver);
                mContext.getContentResolver().unregisterContentObserver(mVibrateChangeObserver);
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
     * Subclass of StateTracker for GPS state.
     */
    
    
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
                    mInTransition = false;
                    mActualState = false;
                    return STATE_DISABLED;
                case BluetoothAdapter.STATE_ON:
                    mInTransition = false;
                    mActualState = true;
                    return STATE_ENABLED;
                case BluetoothAdapter.STATE_TURNING_ON:
                    mInTransition = true;
                    mActualState = false;
                    return STATE_TURNING_ON;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    mInTransition = true;
                    mActualState = true;
                    return STATE_TURNING_OFF;
                default:
                    mInTransition = false;
                    mActualState = false;
                    return STATE_UNKNOWN;
            }
        }
		// Aurora <Steve.Tang> 2014-08-13 reset intransiton state, start
		public void resetInTransitionState(){
			android.util.Log.e("xiuyong", "come here every time?");
			mInTransition = false;
		}
		// Aurora <Steve.Tang> 2014-08-13 reset intransiton state, end

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
    
    
    public void initConfigurationState() {
    	mSilentStateTracker.setImageViewResources(mContext);
        mVibrateStateTracker.setImageViewResources(mContext);
		// Aurora <Steve.Tang> 2014-08-13 reset intransiton state, start
		mBluetoothStateTracker.resetInTransitionState();
		// Aurora <Steve.Tang> 2014-08-13 reset intransiton state, start
        mBluetoothStateTracker.setImageViewResources(mContext);
        
    }
    
    public void enlargeTouchRegion() {
    	mSilentIcon.enlargeTouchRegion();
        mVibrateIcon.enlargeTouchRegion();
        mBluetoothIcon.enlargeTouchRegion();
    }
    
    public void updateResources(){
    	mSilentIcon.setConfigName(R.string.toolbar_silent);
        mVibrateIcon.setConfigName(R.string.toolbar_vibrate);
        mBluetoothIcon.setConfigName(R.string.toolbar_bluetooth);
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
        	// Aurora <tongyh> <2014-04-08> After the status bar to close on the sound, no sound save the media begin
        	if(desiredState){
        		if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0){
        			mSaveMusicVolue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        		}
            }
        	// Aurora <tongyh> <2014-04-08> After the status bar to close on the sound, no sound save the media end
//			mSilentCallBcak.setVibrateState(context,muteAndVibrateLinkage.muteChecked(desiredState));
            boolean isVirbrate = muteAndVibrateLinkage.silentChecked(desiredState);

			setCurrentState(context, state);
			setImageViewResources(context);

            if (isVirbrate) {
                state = STATE_ENABLED;
            } else {
                state = STATE_DISABLED;
            } 
            
            mVibrateStateTracker.setCurrentState(context, state);
            mVibrateStateTracker.setImageViewResources(context);
        	// Aurora <tongyh> <2014-04-08> After the status bar to close on the sound, no sound save the media begin
            if(!desiredState){
            	if(mSaveMusicVolue > 0){
            		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveMusicVolue, 0);
            	}
            	
            }
        	//Aurora <tongyh> <2014-04-08> After the status bar to close on the sound, no sound save the media end
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
}
