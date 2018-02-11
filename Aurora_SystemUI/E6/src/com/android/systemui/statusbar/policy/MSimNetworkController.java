/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (c) 2012-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

package com.android.systemui.statusbar.policy;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wimax.WimaxManagerConstants;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cdma.EriInfo;
import com.android.internal.util.AsyncChannel;

import com.android.systemui.R;

import android.util.Log;
import android.os.SystemProperties;

import android.os.SystemClock;

import com.gionee.internal.telephony.GnPhone;
import gionee.telephony.GnTelephonyManager;

public class MSimNetworkController extends NetworkController {
    // debug
    static final String TAG = "StatusBar.MSimNetworkController";
    static final boolean DEBUG = true;
    static final boolean CHATTY = true; // additional diagnostics, but not logspew

    // telephony
    boolean[] mMSimDataConnected;
    IccCardConstants.State[] mMSimState;
    int[] mMSimDataActivity;
    ServiceState[] mMSimServiceState;
    SignalStrength[] mMSimSignalStrength;
    private PhoneStateListener[] mMSimPhoneStateListener;
    private CharSequence[] mCarrierTextSub;

    String[] mMSimNetworkName;
    int[] mMSimPhoneSignalIconId;
    int[] mMSimLastPhoneSignalIconId;
    private int[] mMSimIconId;
    int[] mMSimDataDirectionIconId; // data + data direction on phones
    int[] mMSimDataSignalIconId;
    int[] mMSimDataTypeIconId;
    int[] mNoMSimIconId;
    int[] mMSimMobileActivityIconId; // overlay arrows for data direction

    String[] mMSimContentDescriptionPhoneSignal;
    String[] mMSimContentDescriptionCombinedSignal;
    String[] mMSimContentDescriptionDataType;

    int[] mMSimLastDataDirectionIconId;
    int[] mMSimLastCombinedSignalIconId;
    int[] mMSimLastDataTypeIconId;
    int[] mMSimcombinedSignalIconId;
    int[] mMSimcombinedActivityIconId;
    int[] mMSimLastSimIconId;
    private int mDefaultSubscription;
    boolean[] mShowSpn;
    boolean[] mShowPlmn;
    String[] mSpn;
    String[] mPlmn;
    private String mEmergencyCallsOnly = null;
    ArrayList<MSimSignalCluster> mSimSignalClusters = new ArrayList<MSimSignalCluster>();
    ArrayList<TextView> mSubsLabelViews = new ArrayList<TextView>();

    public interface MSimSignalCluster {
        void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
                String contentDescription);
        void setMobileDataIndicators(boolean visible, int strengthIcon, int activityIcon,
                int typeIcon, String contentDescription, String typeContentDescription,
                int noSimIcon, int subscription);
        void setIsAirplaneMode(boolean is, int airplaneIcon);
    }

    /**
     * Construct this controller object and register for updates.
     */
    public MSimNetworkController(Context context) {
        super(context);

        int numPhones = GnTelephonyManager.getPhoneCount();

		// Aurora <Steve.Tang> 2014-10-27 10s delay update for signal strenth. start
		mSSUHandler = new SignalStrengthUpdateHandler[numPhones];
		newSignalStrength = new SignalStrength[numPhones];
		// Aurora <Steve.Tang> 2014-10-27 10s delay update for signal strenth. end

        mMSimSignalStrength = new SignalStrength[numPhones];
        mMSimServiceState = new ServiceState[numPhones];
        mMSimState = new IccCardConstants.State[numPhones];
        mMSimIconId = new int[numPhones];
        mMSimPhoneSignalIconId = new int[numPhones];
        mMSimDataTypeIconId = new int[numPhones];
        mNoMSimIconId = new int[numPhones];
        mMSimMobileActivityIconId = new int[numPhones];
        mMSimContentDescriptionPhoneSignal = new String[numPhones];
        mMSimLastPhoneSignalIconId = new int[numPhones];
        mMSimNetworkName = new String[numPhones];
        mMSimLastDataTypeIconId = new int[numPhones];
        mMSimDataConnected = new boolean[numPhones];
        mMSimDataSignalIconId = new int[numPhones];
        mMSimDataDirectionIconId = new int[numPhones];
        mMSimLastDataDirectionIconId = new int[numPhones];
        mMSimLastCombinedSignalIconId = new int[numPhones];
        mMSimcombinedSignalIconId = new int[numPhones];
        mMSimcombinedActivityIconId = new int[numPhones];
        mMSimDataActivity = new int[numPhones];
        mMSimContentDescriptionCombinedSignal = new String[numPhones];
        mMSimContentDescriptionDataType = new String[numPhones];
        mMSimLastSimIconId = new int[numPhones];
        mCarrierTextSub = new CharSequence[numPhones];
        mShowSpn = new boolean[numPhones];
        mShowPlmn = new boolean[numPhones];
        mSpn = new String[numPhones];
        mPlmn = new String[numPhones];

        for (int i=0; i < numPhones; i++) {
			
			// Aurora <Steve.Tang> 2014-10-27 10s delay update for signal strenth. start
			mSSUHandler[i] = new SignalStrengthUpdateHandler(i);
			newSignalStrength[i] = new SignalStrength();
			// Aurora <Steve.Tang> 2014-10-27 10s delay update for signal strenth. end

            mMSimSignalStrength[i] = new SignalStrength();
            mMSimServiceState[i] = new ServiceState();
            mMSimState[i] = IccCardConstants.State.READY;
            // phone_signal
            mMSimPhoneSignalIconId[i] = R.drawable.stat_sys_signal_null;
            mMSimLastPhoneSignalIconId[i] = -1;
            mMSimLastDataTypeIconId[i] = -1;
            mMSimDataConnected[i] = false;
            mMSimLastDataDirectionIconId[i] = -1;
            mMSimLastCombinedSignalIconId[i] = -1;
            mMSimcombinedSignalIconId[i] = 0;
            mMSimcombinedActivityIconId[i] = 0;
            mMSimDataActivity[i] = TelephonyManager.DATA_ACTIVITY_NONE;
            mMSimLastSimIconId[i] = 0;
            mMSimNetworkName[i] = mNetworkNameDefault;
        }

		registerPhoneStateListener(context);

        mDefaultSubscription = GnTelephonyManager.getDefaultSubscription(context);
        mDataConnected = mMSimDataConnected[mDefaultSubscription];
        mSimState = mMSimState[mDefaultSubscription];
        mDataActivity = mMSimDataActivity[mDefaultSubscription];
        mServiceState = mMSimServiceState[mDefaultSubscription];
        mSignalStrength = mMSimSignalStrength[mDefaultSubscription];
        mPhoneStateListener = mMSimPhoneStateListener[mDefaultSubscription];

        mNetworkName = mMSimNetworkName[mDefaultSubscription];

		mPhoneSignalIconId.setResources(null);
	    mPhoneSignalIconId.setIconId(mMSimPhoneSignalIconId[mDefaultSubscription]);

        mLastPhoneSignalIconId = mMSimLastPhoneSignalIconId[mDefaultSubscription];
        // data + data direction on phones
        mDataDirectionIconId = mMSimDataDirectionIconId[mDefaultSubscription];

		mDataSignalIconId.setResources(null);
	    mDataSignalIconId.setIconId(mMSimDataSignalIconId[mDefaultSubscription]);

        mDataTypeIconId = mMSimDataTypeIconId[mDefaultSubscription];
        mNoSimIconId = mNoMSimIconId[mDefaultSubscription];
        // overlay arrows for data direction

		mMobileActivityIconId.setResources(null);
	    mMobileActivityIconId.setIconId(mMSimMobileActivityIconId[mDefaultSubscription]);

        mContentDescriptionPhoneSignal = mMSimContentDescriptionPhoneSignal[mDefaultSubscription];
        mContentDescriptionCombinedSignal = mMSimContentDescriptionCombinedSignal[
                mDefaultSubscription];
        mContentDescriptionDataType = mMSimContentDescriptionDataType[mDefaultSubscription];

        mLastDataDirectionIconId = mMSimLastDataDirectionIconId[mDefaultSubscription];
        mLastCombinedSignalIconId = mMSimLastCombinedSignalIconId[mDefaultSubscription];
        mLastDataTypeIconId = mMSimLastDataTypeIconId[mDefaultSubscription];
        mLastSimIconId = mMSimLastSimIconId[mDefaultSubscription];
        mEmergencyCallsOnly = context.getString(com.android.internal.R.string.emergency_calls_only);
    }

    protected void registerPhoneStateListener(Context context) {
        // telephony
        int numPhones = GnTelephonyManager.getPhoneCount();
        mMSimPhoneStateListener = new PhoneStateListener[numPhones];

        for (int i=0; i < numPhones; i++) {
            mMSimPhoneStateListener[i] = getPhoneStateListener(i);
            GnTelephonyManager.listenGemini(mMSimPhoneStateListener[i],
                              PhoneStateListener.LISTEN_SERVICE_STATE
                            | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                            | PhoneStateListener.LISTEN_CALL_STATE
                            | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                            | PhoneStateListener.LISTEN_DATA_ACTIVITY, i);
        }
    }

    public void addSignalCluster(MSimSignalCluster cluster, int subscription) {
        mSimSignalClusters.add(cluster);
        refreshSignalCluster(cluster, subscription);
    }

    public void refreshSignalCluster(MSimSignalCluster cluster, int subscription) {
        cluster.setWifiIndicators(
                // only show wifi in the cluster if connected or if wifi-only
                mWifiEnabled && (mWifiConnected || !mHasMobileDataFeature),
                mWifiIconId,
                mWifiActivityIconId,
                mContentDescriptionWifi);
        cluster.setMobileDataIndicators(
                mHasMobileDataFeature,
                mMSimPhoneSignalIconId[subscription],
                mMSimMobileActivityIconId[subscription],
                mMSimDataTypeIconId[subscription],
                mMSimContentDescriptionPhoneSignal[subscription],
                mMSimContentDescriptionDataType[subscription],
                mNoMSimIconId[subscription], subscription);
        if (mIsWimaxEnabled && mWimaxConnected) {
            // wimax is special
            cluster.setMobileDataIndicators(
                    true,
                    mAlwaysShowCdmaRssi ? mPhoneSignalIconId.getIconId() : mWimaxIconId,
                    mMSimMobileActivityIconId[subscription],
                    mMSimDataTypeIconId[subscription],
                    mContentDescriptionWimax,
                    mMSimContentDescriptionDataType[subscription],
                    mNoMSimIconId[subscription], subscription);
        } else {
            // normal mobile data
            cluster.setMobileDataIndicators(
                    mHasMobileDataFeature,
                    mShowPhoneRSSIForData ? mMSimPhoneSignalIconId[subscription]
                        : mMSimDataSignalIconId[subscription],
                    mMSimMobileActivityIconId[subscription],
                    mMSimDataTypeIconId[subscription],
                    mMSimContentDescriptionPhoneSignal[subscription],
                    mMSimContentDescriptionDataType[subscription],
                    mNoMSimIconId[subscription], subscription);
        }
        cluster.setIsAirplaneMode(mAirplaneMode, mAirplaneIconId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d("0603", "action = " + action.toString());
        if (action.equals(WifiManager.RSSI_CHANGED_ACTION)
                || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
                || action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            updateWifiState(intent);
            refreshViews(mDefaultSubscription);
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            updateSimState(intent);
            for (int sub = 0; sub < GnTelephonyManager.getPhoneCount(); sub++) {
				// Aurora <Steve.Tang> 2014-09-25 update sim state. start
				updateTelephonySignalStrength(sub);
				// Aurora <Steve.Tang> 2014-09-25 update sim state. end
                updateDataIcon(sub);
                refreshViews(sub);
            }
        } else if (action.equals(TelephonyIntents.SPN_STRINGS_UPDATED_ACTION)) {
            final int subscription = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, 0);
            Slog.d(TAG, "Received SPN update on sub :" + subscription);
            mShowSpn[subscription] = intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_SPN, false);
            mSpn[subscription] = intent.getStringExtra(TelephonyIntents.EXTRA_SPN);
            mShowPlmn[subscription] = intent.getBooleanExtra(
                    TelephonyIntents.EXTRA_SHOW_PLMN, false);
            mPlmn[subscription] = intent.getStringExtra(TelephonyIntents.EXTRA_PLMN);
            //Aurora <rock.tong> 2014-10-08 have sim card shows no service begin
            if(mSpn[subscription] == null && mPlmn[subscription] == null){
            	mPlmn[subscription] = getAuroraPlmn(subscription,context);
            	Log.d(TAG,"mMSimServiceState[" + subscription + "]---Plmm    " + mMSimServiceState[subscription].getOperatorAlphaLong());
            }
            //Aurora <rock.tong> 2014-10-08 have sim card shows no service end
            updateNetworkName(mShowSpn[subscription], mSpn[subscription], mShowPlmn[subscription],
                    mPlmn[subscription], subscription);
            updateCarrierText(subscription);
            refreshViews(subscription);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                 action.equals(ConnectivityManager.INET_CONDITION_ACTION)) {
            updateConnectivity(intent);
            refreshViews(mDefaultSubscription);
        } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
            refreshViews(mDefaultSubscription);
		// Aurora <Steve.Tang> 2014-08-26 fix 7827,show one airplane mode string in English, start
		} else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED) || (Intent.ACTION_LOCALE_CHANGED).equals(action)) {
        //} else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
		// Aurora <Steve.Tang> 2014-08-26 fix 7827,show one airplane mode string in English, end
            updateAirplaneMode();
            for (int i = 0; i < GnTelephonyManager.getPhoneCount(); i++) {
//                updateSimIcon(i);
                updateCarrierText(i);
				// Steve.tang 2014-07-17 resolve signal icon change too slow. start
            	refreshViews(i);
            }
            //refreshViews(mDefaultSubscription);
			// Steve.tang 2014-07-17 resolve signal icon change too slow. end
        } else if (action.equals(WimaxManagerConstants.NET_4G_STATE_CHANGED_ACTION) ||
                action.equals(WimaxManagerConstants.SIGNAL_LEVEL_CHANGED_ACTION) ||
                action.equals(WimaxManagerConstants.WIMAX_NETWORK_STATE_CHANGED_ACTION)) {
            updateWimaxState(intent);
            refreshViews(mDefaultSubscription);
		// Aurora <Steve.Tang> 2014-09-25 add a log for test(no service). start
        } else if (("com.aurora.test.noservice").equals(action)){
			for (int i = 0; i < GnTelephonyManager.getPhoneCount(); i++) {
                updateCarrierText(i);
				Slog.d(TAG, "updateCarrierText for sub:" + i + " simState =" + mMSimState[i] + " networkName: " + mMSimNetworkName[i]);
            }
		// Aurora <Steve.Tang> 2014-09-25 add a log for test(no service). end
		}
    }

    public void addSubsLabelView(TextView v) {
        mSubsLabelViews.add(v);
    }

    private void updateCarrierText(int sub) {
        int textResId = 0;
        if (mAirplaneMode) {
            textResId = R.string.status_bar_settings_airplane;
        } else {
            if (DEBUG) {
                Slog.d(TAG, "updateCarrierText for sub:" + sub + " simState =" + mMSimState[sub]);
            }

            switch (mMSimState[sub]) {
                case ABSENT:
                case UNKNOWN:
                case NOT_READY:
                    textResId = com.android.internal.R.string.lockscreen_missing_sim_message_short;
                    break;
                case PIN_REQUIRED:
                    textResId = com.android.internal.R.string.lockscreen_sim_locked_message;
                    break;
                case PUK_REQUIRED:
                    textResId = com.android.internal.R.string.lockscreen_sim_puk_locked_message;
                    break;
                case READY:
                    // If the state is ready, set the text as network name.
                    mCarrierTextSub[sub] = mMSimNetworkName[sub];
                    break;
                case PERM_DISABLED:
                    textResId = com.android.internal.
                            R.string.lockscreen_permanent_disabled_sim_message_short;
                    break;
                default:
                    textResId = com.android.internal.R.string.lockscreen_missing_sim_message_short;
                    break;
            }
        }

        if (textResId != 0) {
            mCarrierTextSub[sub] = mContext.getString(textResId);
        }
    }

    private void setCarrierText() {
        /*String carrierName = mCarrierTextSub[GnPhone.GEMINI_SIM_1]
                  + "    " + mCarrierTextSub[GnPhone.GEMINI_SIM_2];*/
    	String carrierNameSUB1 = mCarrierTextSub[GnPhone.GEMINI_SIM_1]+"";
        String carrierNameSUB2 = mCarrierTextSub[GnPhone.GEMINI_SIM_2]+"";
        //Aurora <rock.tong> 2014-10-08 no sim card shows no service begin
        if(mPhone != null){
        	if(GnTelephonyManager.getSimStateGemini(0) == TelephonyManager.SIM_STATE_ABSENT){
            	carrierNameSUB1 = mContext.getString(com.android.internal.R.string.lockscreen_missing_sim_message_short);
    	    }
        	
            if(GnTelephonyManager.getSimStateGemini(1) == TelephonyManager.SIM_STATE_ABSENT){
            	carrierNameSUB2 = mContext.getString(com.android.internal.R.string.lockscreen_missing_sim_message_short);
    	    }
        }
        //Aurora <rock.tong> 2014-10-08 no sim card shows no service end
		// Aurora <Steve.Tang> add for test, start
		if (DEBUG) {
        	Slog.d(TAG, "setCarrierText  carrierNameSUB1: " + carrierNameSUB1 + " carrierNameSUB2: " + carrierNameSUB2);
        }
		// Aurora <Steve.Tang> add for test, end		

		//steve.tang 2014-07-15 do not show 'sim card not insert' as only one sim card insert. start
		boolean hasIccCard01 = true;
		boolean hasIccCard02 = true;
		//steve.tang 2014-07-15 do not show 'sim card not insert' as only one sim card insert. end

        if(carrierNameSUB1 != null && !carrierNameSUB1.equals("")){
        	carrierNameSUB1 = getLocaleString(carrierNameSUB1);
        }
        if(carrierNameSUB2 != null && !carrierNameSUB2.equals("")){
        	carrierNameSUB2 = getLocaleString(carrierNameSUB2);
        }
        if(carrierNameSUB1 != null && carrierNameSUB1.equals(mContext.getString(com.android.internal.R.string.lockscreen_missing_sim_message_short))){        	
        	carrierNameSUB1 = mContext.getString(R.string.aurora_missing_sim_message_short_one);
			//steve.tang 2014-07-15 slot one not insert. start
			hasIccCard01 = false;
			//steve.tang 2014-07-15 slot one not insert. end
        }
        if(carrierNameSUB2 != null && carrierNameSUB2.equals(mContext.getString(com.android.internal.R.string.lockscreen_missing_sim_message_short))){
        	carrierNameSUB2 = mContext.getString(R.string.aurora_missing_sim_message_short_two);
			//steve.tang 2014-07-15 slot two not insert. start
        	hasIccCard02 = false;
			//steve.tang 2014-07-15 slot two not insert. end
        }
		// Aurora <Steve.Tang> 2014-09-23, add state for sim card service state. start
		boolean hasService01 = true;
		boolean hasService02 = true; 
		// Aurora <Steve.Tang> 2014-09-23, add state for sim card service state. end
        if(carrierNameSUB1 != null && carrierNameSUB1.equals(mContext.getString(com.android.internal.R.string.lockscreen_carrier_default))){
        	carrierNameSUB1 = mContext.getString(R.string.carrier_default);
			hasService01 = false;
        }
        if(carrierNameSUB2 != null && carrierNameSUB2.equals(mContext.getString(com.android.internal.R.string.lockscreen_carrier_default))){
        	carrierNameSUB2 = mContext.getString(R.string.carrier_default);
			hasService02 = false;
        }
        String carrierName = carrierNameSUB1 + "    " + carrierNameSUB2;
		//steve.tang 2014-07-15 do not show 'sim card not insert' as only one sim card insert. start
		if((!hasIccCard01 && hasIccCard02) || (hasIccCard01 && !hasIccCard02)){
				carrierName = hasIccCard01 ? carrierNameSUB1 : carrierNameSUB2;
		}
		//steve.tang 2014-07-15 do not show 'sim card not insert' as only one sim card insert. end
		boolean isAirmodeOn = false;
        if((carrierNameSUB1 != null && carrierNameSUB1.equals(mContext.getString(R.string.status_bar_settings_airplane)))
        		&& (carrierNameSUB1 != null && carrierNameSUB2.equals(mContext.getString(R.string.status_bar_settings_airplane)))){
        	carrierName = mContext.getString(R.string.status_bar_settings_airplane);
			isAirmodeOn = true;
        }

		// Aurora <Steve.Tang> 2014-09-23, refresh views about state start
		Log.e("steve","state: " + mMSimServiceState[0].isEmergencyOnly() + "    " + mMSimServiceState[1].isEmergencyOnly() + "   " + isEmergencyOnly());
		boolean isEmergencyMode = false;
		if((!hasIccCard01 && !hasIccCard02) && ((mMSimServiceState[0].isEmergencyOnly())||(mMSimServiceState[1].isEmergencyOnly()))){
			isEmergencyMode = true;
		}
		// Aurora <Steve.Tang> 2014-09-23, refresh views about state end
        for (int i = 0; i < mSubsLabelViews.size(); i++) {
            TextView v = mSubsLabelViews.get(i);
            v.setText(carrierName);
        }
    }


	//Aurora <Steve.Tang> 2014-10-27 10s delay update for signal strenth. start
	private SignalStrength[] newSignalStrength;
	private SignalStrengthUpdateHandler[] mSSUHandler;
	private final boolean IS_SIGNAL_UPDATE_DELAY= ("IUNI").equals(SystemProperties.get("ro.product.device"));

	private Object lock01 = new Object();
	private Object lock02 = new Object();
	private Object[] lock = { lock01, lock02 };

	private boolean isSignalStrengthNeedDelayUpdate(){
		android.util.Log.e("stevetang","is support 10s delay " + IS_SIGNAL_UPDATE_DELAY);
		return IS_SIGNAL_UPDATE_DELAY;
	}

	public class SignalStrengthUpdateHandler extends Handler {

		private int subId = 0;
		private int oldLevel = 0;
		private long firstComeTime = 0;
		private boolean isFirstCome = false;


		public SignalStrengthUpdateHandler(int subId) {
			super();
			this.subId = subId;
		}

		private Runnable mDelaynotifySignalStrength = new Runnable() {
			public void run() {
				updateSignalStrength();
			}
		};

		public void updateSignalStrength(){
			synchronized(lock[subId]) {

				android.util.Log.e("stevetang","the updateSignalStrength method start");

				long now = SystemClock.uptimeMillis();

				android.util.Log.e("stevetang","the oldLevel: " + oldLevel + " isFirstCome: " + isFirstCome);
				android.util.Log.e("stevetang","the newSignalStrength.level: " + newSignalStrength[subId].getLevel());

				if(newSignalStrength[subId].getLevel() < oldLevel){

					if(!isFirstCome){
						isFirstCome = true;
						firstComeTime = now;
						this.removeCallbacks(mDelaynotifySignalStrength);
			            this.postDelayed(mDelaynotifySignalStrength, 10000);
						return;

					}else if(now - firstComeTime < 10000) {
						return;
					}
				}

				this.removeCallbacks(mDelaynotifySignalStrength);
				android.util.Log.e("stevetang","time to update signal strength \n");
				firstComeTime = now;
				isFirstCome = false;
				oldLevel = newSignalStrength[subId].getLevel();
				mMSimSignalStrength[subId] = newSignalStrength[subId];
				updateTelephonySignalStrength(subId);
				refreshViews(subId);
			}
		}
	}
	// Aurora <Steve.Tang> 2014-10-27 10s delay update for signal strenth. end

    // ===== Telephony ==============================================================

    private PhoneStateListener getPhoneStateListener(int subscription) {
		final int id = subscription;
        PhoneStateListener mMSimPhoneStateListener = new PhoneStateListener() {
			int mSubscription = id;	
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				android.util.Log.e("stevetang","\n onSignalStrengthsChanged the level is " + signalStrength.getLevel());
                if (DEBUG) {
                    Slog.d(TAG, "onSignalStrengthsChanged received on subscription :"
                        + mSubscription + "signalStrength=" + signalStrength +
                        ((signalStrength == null) ? "" : (" level=" + signalStrength.getLevel())));
                }
				// Aurora <Steve.Tang> 2014-10-27 10s signal update delay. start
				if(isSignalStrengthNeedDelayUpdate()){
					newSignalStrength[mSubscription] = signalStrength;
					mSSUHandler[mSubscription].updateSignalStrength();
				} else {
		            mMSimSignalStrength[mSubscription] = signalStrength;
		            updateTelephonySignalStrength(mSubscription);
		            refreshViews(mSubscription);
				}
				// Aurora <Steve.Tang> 2014-10-27 10s signal update delay. end
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                if (DEBUG) {
                    Slog.d(TAG, "onServiceStateChanged received on subscription :"
                        + mSubscription + "state=" + state.getState());
                }
                mMSimServiceState[mSubscription] = state;
				// Aurora <Steve.tang> 2014-10-21 use operator code to get plmn, start
				if(mSpn[mSubscription] == null &&  mPlmn[mSubscription] ==null){
					mPlmn[mSubscription] = getAuroraPlmn(mSubscription,mContext);
					mShowPlmn[mSubscription] = true;
				}
				// Aurora <Steve.tang> 2014-10-21 use operator code to get plmn, end
                updateTelephonySignalStrength(mSubscription);
                updateDataNetType(mSubscription);
                updateDataIcon(mSubscription);
                updateNetworkName(mShowSpn[mSubscription], mSpn[mSubscription],
                                mShowPlmn[mSubscription], mPlmn[mSubscription], mSubscription);
                updateCarrierText(mSubscription);

                refreshViews(mSubscription);
            }

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DEBUG) {
                    Slog.d(TAG, "onCallStateChanged received on subscription :"
                    + mSubscription + "state=" + state);
                }
                // In cdma, if a voice call is made, RSSI should switch to 1x.
                if (isCdma(mSubscription)) {
                    updateTelephonySignalStrength(mSubscription);
                    refreshViews(mSubscription);
                }
            }

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                if (DEBUG) {
                    Slog.d(TAG, "onDataConnectionStateChanged received on subscription :"
                    + mSubscription + "state=" + state + " type=" + networkType);
                }

                // DSDS case: Data is active only on DDS. Ignore the Data Connection
                // State changed notifications of the other NON-DDS.
                if (mSubscription ==
                        GnTelephonyManager.getPreferredDataSubscription(mContext)) {
                    mDataState = state;
                    mDataNetType = networkType;
                }
                updateDataNetType(mSubscription);
                updateDataIcon(mSubscription);
                refreshViews(mSubscription);
            }

            @Override
            public void onDataActivity(int direction) {
                if (DEBUG) {
                    Slog.d(TAG, "onDataActivity received on subscription :"
                        + mSubscription + "direction=" + direction);
                }
                mMSimDataActivity[mSubscription] = direction;
                updateDataIcon(mSubscription);
                refreshViews(mSubscription);
            }
        };
        return mMSimPhoneStateListener;
    }

    // ===== Wifi ===================================================================

    class MSimWifiHandler extends WifiHandler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiManager.DATA_ACTIVITY_NOTIFICATION:
                    if (msg.arg1 != mWifiActivity) {
                        mWifiActivity = msg.arg1;
                        refreshViews(GnTelephonyManager.getPreferredDataSubscription(mContext));
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    protected void updateSimState(Intent intent) {
        IccCardConstants.State simState;
        String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
        // Obtain the subscription info from intent.
        int sub = intent.getIntExtra(GnPhone.GEMINI_SIM_ID_KEY, 0);
        Slog.d(TAG, "updateSimState for subscription :" + sub);
        if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
            simState = IccCardConstants.State.ABSENT;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
            simState = IccCardConstants.State.READY;
        }
        else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
            final String lockedReason = intent.getStringExtra(IccCardConstants.
                                                            INTENT_KEY_LOCKED_REASON);
            if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                simState = IccCardConstants.State.PIN_REQUIRED;
            }
            else if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                simState = IccCardConstants.State.PUK_REQUIRED;
            } else {
        	    simState = IccCardConstants.State.UNKNOWN;
			}
        } else {
            simState = IccCardConstants.State.UNKNOWN;
        }
        // Update the sim state and carrier text.
        if (simState != IccCardConstants.State.UNKNOWN && simState != mMSimState[sub]) {
            mMSimState[sub] = simState;
            updateCarrierText(sub);
            Slog.d(TAG, "updateSimState simState =" + mMSimState[sub]);
        }
//        updateSimIcon(sub);
        updateDataIcon(sub);
    }

    private boolean isCdma(int subscription) {
        return (mMSimSignalStrength[subscription] != null) &&
                !mMSimSignalStrength[subscription].isGsm();
    }

    private boolean hasService(int subscription) {
        ServiceState ss = mMSimServiceState[subscription];
        if (ss != null) {
            switch (ss.getState()) {
                case ServiceState.STATE_OUT_OF_SERVICE:
                case ServiceState.STATE_POWER_OFF:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

	// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. start
	private final int [] STATE_SIGNAL_NULL_GEMINI = {
		R.drawable.stat_sys_signal_null_1,
		R.drawable.stat_sys_signal_null_2
	};
	// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. end

    private final void updateTelephonySignalStrength(int subscription) {
        Slog.d(TAG, "updateTelephonySignalStrength: subscription =" + subscription);
        if (!hasService(subscription)) {
            if (DEBUG) Slog.d(TAG, " No service");
/*            mMSimPhoneSignalIconId[subscription] = R.drawable.stat_sys_signal_null;
            mMSimDataSignalIconId[subscription] = R.drawable.stat_sys_signal_null;*/
            if(mMSimState[subscription] != IccCardConstants.State.ABSENT){
                // Aurora <tongyh> <2014-05-12>update sim state end
				// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. start
				if(TWO_CARD_INSERT != getSimCountInsert()){
		        	mMSimPhoneSignalIconId[subscription] = R.drawable.stat_sys_signal_null;
		        	mMSimDataSignalIconId[subscription] = R.drawable.stat_sys_signal_null;
				} else {
					mMSimPhoneSignalIconId[subscription] = STATE_SIGNAL_NULL_GEMINI[subscription];
		        	mMSimDataSignalIconId[subscription] = STATE_SIGNAL_NULL_GEMINI[subscription];
				}
				// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. end
       		}else{
       	         mMSimPhoneSignalIconId[subscription] = R.drawable.stat_sys_signal_no_sim;
       	         mMSimDataSignalIconId[subscription] = R.drawable.stat_sys_signal_no_sim;
       		}
        } else {
            if (mMSimSignalStrength[subscription] == null || (mMSimServiceState == null)) {
                if (DEBUG) {
                    Slog.d(TAG, " Null object, mMSimSignalStrength= "
                            + mMSimSignalStrength[subscription]
                            + " mMSimServiceState " + mMSimServiceState[subscription]);
                }
                mMSimPhoneSignalIconId[subscription] = R.drawable.stat_sys_signal_null;
                mMSimDataSignalIconId[subscription] = R.drawable.stat_sys_signal_null;
                mMSimContentDescriptionPhoneSignal[subscription] = mContext.getString(
                        AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0]);
            } else {
                int iconLevel;
                int[] iconList;
                if (isCdma(subscription) && mAlwaysShowCdmaRssi) {
                    mLastSignalLevel = iconLevel = mMSimSignalStrength[subscription].getCdmaLevel();
                    if(DEBUG) Slog.d(TAG, "mAlwaysShowCdmaRssi= " + mAlwaysShowCdmaRssi
                            + " set to cdmaLevel= "
                            + mMSimSignalStrength[subscription].getCdmaLevel()
                            + " instead of level= " + mMSimSignalStrength[subscription].getLevel());
                } else {
                    mLastSignalLevel = iconLevel = mMSimSignalStrength[subscription].getLevel();
                }

                // Though mPhone is a Manager, this call is not an IPC
                if ((isCdma(subscription) && isCdmaEri(subscription)) || GnTelephonyManager.isNetworkRoamingGemini(subscription)) {
                    iconList = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH_ROAMING[mInetCondition];
                } else {
                    iconList = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH[mInetCondition];
                }

                Slog.d(TAG, "updateTelephonySignalStrength iconList = " + iconList + "iconLevel = "
                        + iconLevel + " mInetCondition = " + mInetCondition);
				if(iconLevel>4) iconLevel = 4;
                mMSimPhoneSignalIconId[subscription] = iconList[iconLevel];
                mMSimContentDescriptionPhoneSignal[subscription] = mContext.getString(
                        AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[iconLevel]);
				// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. start
				if(TWO_CARD_INSERT != getSimCountInsert()){
                		mMSimDataSignalIconId[subscription] = TelephonyIcons.DATA_SIGNAL_STRENGTH[mInetCondition][iconLevel];
					} else {
						 mMSimDataSignalIconId[subscription] = TelephonyIcons.AURORA_TELEPHONY_SIGNAL_STRENGTH[subscription][iconLevel];
					}
				// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. end
            }
        }
    }
    // Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. start
	private static final int TWO_CARD_INSERT = 2;	
	private int getSimCountInsert(){
		return gionee.provider.GnTelephony.SIMInfo.getInsertedSIMCount(mContext);
	}
	// Aurora <Steve.Tang> 2014-08-13 add resource for gemini card. end

    private final void updateDataNetType(int subscription) {
        // DSDS case: Data is active only on DDS. Clear the icon for NON-DDS
        int dataSub = GnTelephonyManager.getPreferredDataSubscription(mContext);
        if (subscription != dataSub) {
            Slog.d(TAG,"updateDataNetType: SUB" + subscription
                    + " is not DDS(=SUB" + dataSub + ")!");
            mMSimDataTypeIconId[subscription] = 0;
        } else {
            if (mIsWimaxEnabled && mWimaxConnected) {
                // wimax is a special 4g network not handled by telephony
                mDataIconList = TelephonyIcons.DATA_4G[mInetCondition];
                mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_4g_blue;
                mMSimContentDescriptionDataType[subscription] = mContext.getString(
                        R.string.accessibility_data_connection_4g);
            } else {
                Slog.d(TAG,"updateDataNetType sub = " + subscription
                        + " mDataNetType = " + mDataNetType);
                switch (mDataNetType) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        if (DEBUG) {
                            Slog.e(TAG, "updateDataNetType NETWORK_TYPE_UNKNOWN");
                        }
                        if (!mShowAtLeastThreeGees) {
                            mDataIconList = TelephonyIcons.DATA_G[mInetCondition];
                            mMSimDataTypeIconId[subscription] = 0;
                            mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                    R.string.accessibility_data_connection_gprs);
                            break;
                        } else {
                            // fall through
                        }
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        if (!mShowAtLeastThreeGees) {
                            mDataIconList = TelephonyIcons.DATA_E[mInetCondition];
                            mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_e_blue;
                            mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                    R.string.accessibility_data_connection_edge);
                            break;
                        } else {
                            // fall through
                        }
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        mDataIconList = TelephonyIcons.DATA_3G[mInetCondition];
                        mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_3g_blue;
                        mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                R.string.accessibility_data_connection_3g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        if (mHspaDataDistinguishable) {
                            mDataIconList = TelephonyIcons.DATA_H[mInetCondition];
                            mMSimDataTypeIconId[subscription] = R.drawable.
                            		stat_sys_gemini_data_connected_h_blue;
                            mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                    R.string.accessibility_data_connection_3_5g);
							// Aurora <Steve.Tang> 2014-08-27 show H+ icon for HAPAP. start
							if(mDataNetType == TelephonyManager.NETWORK_TYPE_HSPAP){
								mMSimDataTypeIconId[subscription] = R.drawable.
                            		stat_sys_gemini_data_connected_h_plus_blue;
							}
							// Aurora <Steve.Tang> 2014-08-27 show H+ icon for HAPAP. start
                        } else {
                            mDataIconList = TelephonyIcons.DATA_3G[mInetCondition];
                            mMSimDataTypeIconId[subscription] = R.drawable.
                            		stat_sys_gemini_data_connected_3g_blue;
                            mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                    R.string.accessibility_data_connection_3g);
                        }
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        // display 1xRTT for IS95A/B
                        mDataIconList = TelephonyIcons.DATA_1X[mInetCondition];
                        mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_1x_blue;
                        mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                R.string.accessibility_data_connection_cdma);
                        break;
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        mDataIconList = TelephonyIcons.DATA_1X[mInetCondition];
                        mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_1x_blue;
                        mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                R.string.accessibility_data_connection_cdma);
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0: //fall through
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        mDataIconList = TelephonyIcons.DATA_3G[mInetCondition];
                        mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_3g_blue;
                        mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                R.string.accessibility_data_connection_3g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        mDataIconList = TelephonyIcons.DATA_4G[mInetCondition];
                        mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_4g_blue;
                        mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                R.string.accessibility_data_connection_4g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        if (!mShowAtLeastThreeGees) {
                            mDataIconList = TelephonyIcons.DATA_G[mInetCondition];
                            mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_g_blue;
                            mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                    R.string.accessibility_data_connection_gprs);
                        } else {
                            mDataIconList = TelephonyIcons.DATA_3G[mInetCondition];
                            mMSimDataTypeIconId[subscription] =
                                R.drawable.stat_sys_gemini_data_connected_3g_blue;
                            mMSimContentDescriptionDataType[subscription] = mContext.getString(
                                    R.string.accessibility_data_connection_3g);
                        }
                        break;
                    default:
                        if (DEBUG) {
                            Slog.e(TAG, "updateDataNetType unknown radio:" + mDataNetType);
                        }
                        mDataNetType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                        mMSimDataTypeIconId[subscription] = 0;
                        break;
                }
            }
        }

        if (isCdma(subscription)) {
            if (isCdmaEri(subscription)) {
                mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_roam_blue;
            }
        } else if (GnTelephonyManager.isNetworkRoamingGemini(subscription)) {
            mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_roam_blue;
        }
    }

    boolean isCdmaEri(int subscription) {
        if ((mMSimServiceState[subscription] != null) && (hasService(subscription))) {
            final int iconIndex = mMSimServiceState[subscription].getCdmaEriIconIndex();
            if (iconIndex != EriInfo.ROAMING_INDICATOR_OFF) {
                final int iconMode = mMSimServiceState[subscription].getCdmaEriIconMode();
                if (iconMode == EriInfo.ROAMING_ICON_MODE_NORMAL
                        || iconMode == EriInfo.ROAMING_ICON_MODE_FLASH) {
                    return true;
                }
            }
        }
        return false;
    }

    private final void updateSimIcon(int cardIndex) {
        Slog.d(TAG,"In updateSimIcon card =" + cardIndex + ", simState= " + mMSimState[cardIndex]);
        if (mMSimState[cardIndex] ==  IccCardConstants.State.ABSENT) {
            mNoMSimIconId[cardIndex] = R.drawable.stat_sys_signal_no_sim;
        } else {
            mNoMSimIconId[cardIndex] = 0;
        }
        refreshViews(cardIndex);
    }

    private final void updateDataIcon(int subscription) {
        Slog.d(TAG,"updateDataIcon subscription =" + subscription);
        int iconId = 0;
        boolean visible = true;

        int dataSub = GnTelephonyManager.getPreferredDataSubscription(mContext);
        Slog.d(TAG,"updateDataIcon dataSub =" + dataSub);
        // DSDS case: Data is active only on DDS. Clear the icon for NON-DDS
        if (subscription != dataSub) {
            mMSimDataConnected[subscription] = false;
            Slog.d(TAG,"updateDataIconi: SUB" + subscription
                     + " is not DDS.  Clear the mMSimDataConnected Flag and return");
            return;
        }

        Slog.d(TAG,"updateDataIcon  when SimState =" + mMSimState[subscription]);
        if (mDataNetType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            // If data network type is unknown do not display data icon
            visible = false;
        } else if (!isCdma(subscription)) {
             Slog.d(TAG,"updateDataIcon  when gsm mMSimState =" + mMSimState[subscription]);
            // GSM case, we have to check also the sim state
            if (mMSimState[subscription] == IccCardConstants.State.READY ||
                mMSimState[subscription] == IccCardConstants.State.UNKNOWN) {
                if (mDataState == TelephonyManager.DATA_CONNECTED) {
                    switch (mMSimDataActivity[subscription]) {
                        case TelephonyManager.DATA_ACTIVITY_IN:
                            iconId = mDataIconList[1];
                            break;
                        case TelephonyManager.DATA_ACTIVITY_OUT:
                            iconId = mDataIconList[2];
                            break;
                        case TelephonyManager.DATA_ACTIVITY_INOUT:
                            iconId = mDataIconList[3];
                            break;
                        default:
                            iconId = mDataIconList[0];
                            break;
                    }
                    mMSimDataDirectionIconId[subscription] = iconId;
                } else {
                    iconId = 0;
                    visible = false;
                }
            } else {
                Slog.d(TAG,"updateDataIcon when no sim");
                iconId = R.drawable.stat_sys_no_sim;
                visible = false; // no SIM? no data
            }
        } else {
            // CDMA case, mMSimDataActivity can be also DATA_ACTIVITY_DORMANT
            if (mDataState == TelephonyManager.DATA_CONNECTED) {
                switch (mMSimDataActivity[subscription]) {
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        iconId = mDataIconList[1];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        iconId = mDataIconList[2];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        iconId = mDataIconList[3];
                        break;
                    case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    default:
                        iconId = mDataIconList[0];
                        break;
                }
            } else {
                iconId = 0;
                visible = false;
            }
        }

        // yuck - this should NOT be done by the status bar
        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.notePhoneDataConnectionState(GnTelephonyManager.getNetworkTypeGemini(subscription), visible);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }

        mMSimDataDirectionIconId[subscription] = iconId;
        mMSimDataConnected[subscription] = visible;
        Slog.d(TAG,"updateDataIcon when mMSimDataConnected =" + mMSimDataConnected[subscription]);
    }

    void updateNetworkName(boolean showSpn, String spn, boolean showPlmn, String plmn,
            int subscription) {
        if (DEBUG) {
            Slog.d(TAG, "updateNetworkName showSpn=" + showSpn + " spn=" + spn
                    + " showPlmn=" + showPlmn + " plmn=" + plmn);
        }
        StringBuilder str = new StringBuilder();
        boolean something = false;
        if (showPlmn && plmn != null) {
            str.append(plmn);
            something = true;
        }
		// Aurora <Steve.Tang> 2014-10-11 only show one info,if show plmn, do not show spn. start
		if(showPlmn && plmn != null){
			showSpn = false;
		}
		// Aurora <Steve.Tang> 2014-10-11 only show one info,if show plmn, do not show spn. end
        if (showSpn && spn != null) {
            if (something) {
                str.append(mNetworkNameSeparator);
            }
            str.append(spn);
            something = true;
        }
        if (something) {
        	if(mEmergencyCallsOnly.equals(str.toString())){
        	    mMSimNetworkName[subscription] = mNetworkNameDefault;
        	}else{
                mMSimNetworkName[subscription] = str.toString();
            }
        } else {
        	mMSimNetworkName[subscription] = mNetworkNameDefault;
        }
        Slog.d(TAG, "mMSimNetworkName[subscription] " + mMSimNetworkName[subscription]
                                                      + "subscription " + subscription);
    }

    // ===== Full or limited Internet connectivity ==================================
    @Override
    protected void updateConnectivity(Intent intent) {
        if (CHATTY) {
            Slog.d(TAG, "updateConnectivity: intent=" + intent);
        }

        final ConnectivityManager connManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connManager.getActiveNetworkInfo();

        // Are we connected at all, by any interface?
        mConnected = info != null && info.isConnected();
        if (mConnected) {
            mConnectedNetworkType = info.getType();
            mConnectedNetworkTypeName = info.getTypeName();
        } else {
            mConnectedNetworkType = ConnectivityManager.TYPE_NONE;
            mConnectedNetworkTypeName = null;
        }

        int connectionStatus = intent.getIntExtra(ConnectivityManager.EXTRA_INET_CONDITION, 0);

        if (CHATTY) {
            Slog.d(TAG, "updateConnectivity: networkInfo=" + info);
            Slog.d(TAG, "updateConnectivity: connectionStatus=" + connectionStatus);
        }
		// Aurora <Steve.Tang> 2014-08-19 fiix 7715, only use blue resource. start
        // mInetCondition = (connectionStatus > INET_CONDITION_THRESHOLD ? 1 : 0);
		mInetCondition = 0;
		// Aurora <Steve.Tang> 2014-08-19 fiix 7715, only use blue resource. end
        if (info != null && info.getType() == ConnectivityManager.TYPE_BLUETOOTH) {
            mBluetoothTethered = info.isConnected();
        } else {
            mBluetoothTethered = false;
        }

        // We want to update all the icons, all at once, for any condition change
        updateWimaxIcons();
        for (int sub = 0; sub < GnTelephonyManager.getPhoneCount(); sub++) {
            updateDataNetType(sub);
            updateDataIcon(sub);
            updateTelephonySignalStrength(sub);
        }
        updateWifiIcons();
    }

    // ===== Update the views =======================================================

    protected void refreshViews(int subscription) {
        Context context = mContext;

        String combinedLabel = "";
        String mobileLabel = "";
        String wifiLabel = "";
        int N;
        Slog.d(TAG,"refreshViews subscription =" + subscription + "mMSimDataConnected ="
                + mMSimDataConnected[subscription]);
        Slog.d(TAG,"refreshViews mMSimDataActivity =" + mMSimDataActivity[subscription]);
        if (!mHasMobileDataFeature) {
            mMSimDataSignalIconId[subscription] = mMSimPhoneSignalIconId[subscription] = 0;
            mobileLabel = "";
        } else {
            // We want to show the carrier name if in service and either:
            //   - We are connected to mobile data, or
            //   - We are not connected to mobile data, as long as the *reason* packets are not
            //     being routed over that link is that we have better connectivity via wifi.
            // If data is disconnected for some other reason but wifi (or ethernet/bluetooth)
            // is connected, we show nothing.
            // Otherwise (nothing connected) we show "No internet connection".

            if (mMSimDataConnected[subscription]) {
                mobileLabel = mMSimNetworkName[subscription];
            } else if (mConnected) {
                if (hasService(subscription)) {
                    mobileLabel = mMSimNetworkName[subscription];
                } else {
                    mobileLabel = "";
                }
            } else {
                mobileLabel
                    = context.getString(R.string.status_bar_settings_signal_meter_disconnected);
            }

            // Now for things that should only be shown when actually using mobile data.
            if (mMSimDataConnected[subscription]) {
                mMSimcombinedSignalIconId[subscription] = mMSimDataSignalIconId[subscription];
                switch (mMSimDataActivity[subscription]) {
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        mMSimMobileActivityIconId[subscription] = R.drawable.stat_sys_signal_in;
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        mMSimMobileActivityIconId[subscription] = R.drawable.stat_sys_signal_out;
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        mMSimMobileActivityIconId[subscription] = R.drawable.stat_sys_signal_inout;
                        break;
                    default:
                        mMSimMobileActivityIconId[subscription] = 0;
                        break;
                }

                combinedLabel = mobileLabel;
                mMSimcombinedActivityIconId[subscription] = mMSimMobileActivityIconId[subscription];
                // set by updateDataIcon()
                mMSimcombinedSignalIconId[subscription] = mMSimDataSignalIconId[subscription];
                mMSimContentDescriptionCombinedSignal[subscription] =
                        mMSimContentDescriptionDataType[subscription];
            } else {
                mMSimMobileActivityIconId[subscription] = 0;
            }
        }

        if (mWifiConnected) {
			// Aurora <Steve.Tang> 2014-10-23 Do not show data activity as wifi connected. start
			mMSimMobileActivityIconId[subscription] = 0;
			// Aurora <Steve.Tang> 2014-10-23 Do not show data activity as wifi connected. end
            if (mWifiSsid == null) {
                wifiLabel = context.getString(
                        R.string.status_bar_settings_signal_meter_wifi_nossid);
                mWifiActivityIconId = 0; // no wifis, no bits
            } else {
                wifiLabel = mWifiSsid;
                if (DEBUG) {
                    wifiLabel += "xxxxXXXXxxxxXXXX";
                }
                switch (mWifiActivity) {
                    case WifiManager.DATA_ACTIVITY_IN:
                        mWifiActivityIconId = R.drawable.stat_sys_wifi_in;
                        break;
                    case WifiManager.DATA_ACTIVITY_OUT:
                        mWifiActivityIconId = R.drawable.stat_sys_wifi_out;
                        break;
                    case WifiManager.DATA_ACTIVITY_INOUT:
                        mWifiActivityIconId = R.drawable.stat_sys_wifi_inout;
                        break;
                    case WifiManager.DATA_ACTIVITY_NONE:
                        mWifiActivityIconId = 0;
                        break;
                }
            }

            mMSimcombinedActivityIconId[subscription] = mWifiActivityIconId;
            combinedLabel = wifiLabel;
            mMSimcombinedSignalIconId[subscription] = mWifiIconId; // set by updateWifiIcons()
            mMSimContentDescriptionCombinedSignal[subscription] = mContentDescriptionWifi;
        } else {
            if (mHasMobileDataFeature) {
                wifiLabel = "";
            } else {
                wifiLabel = context.getString(
                        R.string.status_bar_settings_signal_meter_disconnected);
            }
        }

        if (mBluetoothTethered) {
            combinedLabel = mContext.getString(R.string.bluetooth_tethered);
            mMSimcombinedSignalIconId[subscription] = mBluetoothTetherIconId;
            mMSimContentDescriptionCombinedSignal[subscription] = mContext.getString(
                    R.string.accessibility_bluetooth_tether);
        }

        final boolean ethernetConnected = (mConnectedNetworkType ==
                ConnectivityManager.TYPE_ETHERNET);
        if (ethernetConnected) {
            // TODO: icons and strings for Ethernet connectivity
            combinedLabel = mConnectedNetworkTypeName;
        }

        if (mAirplaneMode &&
                (mMSimServiceState[subscription] == null || (!hasService(subscription)
                    && !mMSimServiceState[subscription].isEmergencyOnly()))) {
            // Only display the flight-mode icon if not in "emergency calls only" mode.

            // look again; your radios are now airplanes
            mMSimContentDescriptionPhoneSignal[subscription] = mContext.getString(
                    R.string.accessibility_airplane_mode);
            mAirplaneIconId = R.drawable.stat_sys_signal_flightmode;
            mMSimPhoneSignalIconId[subscription] = mMSimDataSignalIconId[subscription]
                    = mMSimDataTypeIconId[subscription] = 0;
            mNoMSimIconId[subscription] = 0;

            // combined values from connected wifi take precedence over airplane mode
            if (mWifiConnected) {
                // Suppress "No internet connection." from mobile if wifi connected.
                mobileLabel = "";
            } else {
                if (mHasMobileDataFeature) {
                    // let the mobile icon show "No internet connection."
                    wifiLabel = "";
                } else {
                    wifiLabel = context.getString(
                            R.string.status_bar_settings_signal_meter_disconnected);
                    combinedLabel = wifiLabel;
                }
                mMSimContentDescriptionCombinedSignal[subscription] =
                        mContentDescriptionPhoneSignal;
                mMSimcombinedSignalIconId[subscription] = mMSimDataSignalIconId[subscription];
            }
            mMSimDataTypeIconId[subscription] = 0;
            mNoMSimIconId[subscription] = 0;

            mMSimcombinedSignalIconId[subscription] = mMSimDataSignalIconId[subscription];
        }
        else if (!mMSimDataConnected[subscription] && !mWifiConnected && !mBluetoothTethered &&
                !mWimaxConnected && !ethernetConnected) {
            // pretty much totally disconnected

            combinedLabel = context.getString(
                    R.string.status_bar_settings_signal_meter_disconnected);
            // On devices without mobile radios, we want to show the wifi icon
            mMSimcombinedSignalIconId[subscription] =
                    mHasMobileDataFeature ? mMSimDataSignalIconId[subscription] : mWifiIconId;
            mMSimContentDescriptionCombinedSignal[subscription] = mHasMobileDataFeature
                    ? mMSimContentDescriptionDataType[subscription] : mContentDescriptionWifi;

            mMSimDataTypeIconId[subscription] = 0;
            if (isCdma(subscription)) {
                if (isCdmaEri(subscription)) {
                    mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_roam_blue;
                }
            } else if (GnTelephonyManager.isNetworkRoamingGemini(subscription)) {
                mMSimDataTypeIconId[subscription] = R.drawable.stat_sys_gemini_data_connected_roam_blue;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "refreshViews connected={"
                    + (mWifiConnected?" wifi":"")
                    + (mMSimDataConnected[subscription]?" data":"")
                    + " } level="
                    + ((mMSimSignalStrength[subscription] == null)?"??":Integer.toString
                            (mMSimSignalStrength[subscription].getLevel()))
                    + " mMSimcombinedSignalIconId=0x"
                    + Integer.toHexString(mMSimcombinedSignalIconId[subscription])
                    + "/" + getResourceName(mMSimcombinedSignalIconId[subscription])
                    + " mMSimcombinedActivityIconId=0x" + Integer.toHexString
                            (mMSimcombinedActivityIconId[subscription])
                    + " mAirplaneMode=" + mAirplaneMode
                    + " mMSimDataActivity=" + mMSimDataActivity[subscription]
                    + " mMSimPhoneSignalIconId=0x" + Integer.toHexString
                            (mMSimPhoneSignalIconId[subscription])
                    + " mMSimDataDirectionIconId=0x" + Integer.toHexString
                            (mMSimDataDirectionIconId[subscription])
                    + " mMSimDataSignalIconId=0x" + Integer.toHexString
                            (mMSimDataSignalIconId[subscription])
                    + " mMSimDataTypeIconId=0x" + Integer.toHexString
                            (mMSimDataTypeIconId[subscription])
                    + " mNoMSimIconId=0x" + Integer.toHexString(mNoMSimIconId[subscription])
                    + " mWifiIconId=0x" + Integer.toHexString(mWifiIconId)
                    + " mBluetoothTetherIconId=0x" + Integer.toHexString(mBluetoothTetherIconId));
        }

        if (mMSimLastPhoneSignalIconId[subscription] != mMSimPhoneSignalIconId[subscription]
         || mLastDataDirectionOverlayIconId != mMSimcombinedActivityIconId[subscription]
         || mLastWifiIconId                 != mWifiIconId
         || mLastWimaxIconId                != mWimaxIconId
         || mMSimLastDataTypeIconId[subscription] != mMSimDataTypeIconId[subscription]
         || mLastAirplaneMode               != mAirplaneMode
         || mMSimLastSimIconId[subscription] != mNoMSimIconId[subscription])
        {
            // NB: the mLast*s will be updated later
            for (MSimSignalCluster cluster : mSimSignalClusters) {
                refreshSignalCluster(cluster, subscription);
            }
        }

        if (mLastAirplaneMode != mAirplaneMode) {
            mLastAirplaneMode = mAirplaneMode;
        }

        // the phone icon on phones
        if (mMSimLastPhoneSignalIconId[subscription] != mMSimPhoneSignalIconId[subscription]) {
			//steve.tang 2014-07-15 change resource as resouce has changed successed.start
            //mMSimLastPhoneSignalIconId[subscription] = mMSimPhoneSignalIconId[subscription];
            N = mPhoneSignalIconViews.size();
			if(N > 0){
		        mMSimLastPhoneSignalIconId[subscription] = mMSimPhoneSignalIconId[subscription];
			}
			//steve.tang 2014-07-15 change resource as resouce has changed successed.end
            for (int i=0; i<N; i++) {
                final ImageView v = mPhoneSignalIconViews.get(i);
                if (mPhoneSignalIconId.getIconId() == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mPhoneSignalIconId.getIconId());
                    v.setContentDescription(mContentDescriptionPhoneSignal);
                }
            }
        }

        // the data icon on phones
        if (mMSimLastDataDirectionIconId[subscription] != mMSimDataDirectionIconId[subscription]) {
            mMSimLastDataDirectionIconId[subscription] = mMSimDataDirectionIconId[subscription];
            N = mDataDirectionIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mDataDirectionIconViews.get(i);
                v.setImageResource(mMSimDataDirectionIconId[subscription]);
                v.setContentDescription(mMSimContentDescriptionDataType[subscription]);
            }
        }

        if (mMSimLastSimIconId[subscription] != mNoMSimIconId[subscription]) {
            mMSimLastSimIconId[subscription] = mNoMSimIconId[subscription];
        }

        // the wifi icon on phones
        if (mLastWifiIconId != mWifiIconId) {
            mLastWifiIconId = mWifiIconId;
            N = mWifiIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mWifiIconViews.get(i);
                if (mWifiIconId == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mWifiIconId);
                    v.setContentDescription(mContentDescriptionWifi);
                }
            }
        }

        // the wimax icon on phones
        if (mLastWimaxIconId != mWimaxIconId) {
            mLastWimaxIconId = mWimaxIconId;
            N = mWimaxIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mWimaxIconViews.get(i);
                if (mWimaxIconId == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mWimaxIconId);
                    v.setContentDescription(mContentDescriptionWimax);
                }
           }
        }
        // the combined data signal icon
        if (mMSimLastCombinedSignalIconId[subscription] !=
                mMSimcombinedSignalIconId[subscription]) {
            mMSimLastCombinedSignalIconId[subscription] = mMSimcombinedSignalIconId[subscription];
            N = mCombinedSignalIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mCombinedSignalIconViews.get(i);
                v.setImageResource(mMSimcombinedSignalIconId[subscription]);
                v.setContentDescription(mMSimContentDescriptionCombinedSignal[subscription]);
            }
        }

        // the data network type overlay
        if (mMSimLastDataTypeIconId[subscription] != mMSimDataTypeIconId[subscription]) {
            mMSimLastDataTypeIconId[subscription] = mMSimDataTypeIconId[subscription];
            N = mDataTypeIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mDataTypeIconViews.get(i);
                if (mMSimDataTypeIconId[subscription] == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mMSimDataTypeIconId[subscription]);
                    v.setContentDescription(mMSimContentDescriptionDataType[subscription]);
                }
            }
        }

        // the data direction overlay
        if (mLastDataDirectionOverlayIconId != mMSimcombinedActivityIconId[subscription]) {
            if (DEBUG) {
                Slog.d(TAG, "changing data overlay icon id to " +
                        mMSimcombinedActivityIconId[subscription]);
            }
            mLastDataDirectionOverlayIconId = mMSimcombinedActivityIconId[subscription];
            N = mDataDirectionOverlayIconViews.size();
            for (int i=0; i<N; i++) {
                final ImageView v = mDataDirectionOverlayIconViews.get(i);
                if (mMSimcombinedActivityIconId[subscription] == 0) {
                    v.setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(mMSimcombinedActivityIconId[subscription]);
                    v.setContentDescription(mMSimContentDescriptionDataType[subscription]);
                }
            }
        }

      // the combinedLabel in the notification panel
        if (!mLastCombinedLabel.equals(combinedLabel)) {
            mLastCombinedLabel = combinedLabel;
            N = mCombinedLabelViews.size();
            for (int i=0; i<N; i++) {
                TextView v = mCombinedLabelViews.get(i);
                v.setText(combinedLabel);
            }
        }

        // wifi label
        N = mWifiLabelViews.size();
        for (int i=0; i<N; i++) {
            TextView v = mWifiLabelViews.get(i);
            v.setText(wifiLabel);
            if ("".equals(wifiLabel)) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }

        // mobile label
        setCarrierText();
        N = mMobileLabelViews.size();
        for (int i=0; i<N; i++) {
            TextView v = mMobileLabelViews.get(i);
            v.setText(mobileLabel);
            if ("".equals(mobileLabel)) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args, int subscription) {
        pw.println("NetworkController for SUB : " + subscription + " state:");
        pw.println(String.format("  %s network type %d (%s)",
                mConnected?"CONNECTED":"DISCONNECTED",
                mConnectedNetworkType, mConnectedNetworkTypeName));
        pw.println("  - telephony ------");
        pw.print("  hasService()=");
        pw.println(hasService(subscription));
        pw.print("  mHspaDataDistinguishable=");
        pw.println(mHspaDataDistinguishable);
        pw.print("  mMSimDataConnected=");
        pw.println(mMSimDataConnected[subscription]);
        pw.print("  mMSimState=");
        pw.println(mMSimState[subscription]);
        pw.print("  mPhoneState=");
        pw.println(mPhoneState);
        pw.print("  mDataState=");
        pw.println(mDataState);
        pw.print("  mMSimDataActivity=");
        pw.println(mMSimDataActivity[subscription]);
        pw.print("  mDataNetType=");
        pw.print(mDataNetType);
        pw.print("/");
        pw.println(TelephonyManager.getNetworkTypeName(mDataNetType));
        pw.print("  mMSimServiceState=");
        pw.println(mMSimServiceState[subscription]);
        pw.print("  mMSimSignalStrength=");
        pw.println(mMSimSignalStrength[subscription]);
        pw.print("  mLastSignalLevel");
        pw.println(mLastSignalLevel);
        pw.print("  mMSimNetworkName=");
        pw.println(mMSimNetworkName[subscription]);
        pw.print("  mNetworkNameDefault=");
        pw.println(mNetworkNameDefault);
        pw.print("  mNetworkNameSeparator=");
        pw.println(mNetworkNameSeparator.replace("\n","\\n"));
        pw.print("  mMSimPhoneSignalIconId=0x");
        pw.print(Integer.toHexString(mMSimPhoneSignalIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimPhoneSignalIconId[subscription]));
        pw.print("  mMSimDataDirectionIconId=");
        pw.print(Integer.toHexString(mMSimDataDirectionIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimDataDirectionIconId[subscription]));
        pw.print("  mMSimDataSignalIconId=");
        pw.print(Integer.toHexString(mMSimDataSignalIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimDataSignalIconId[subscription]));
        pw.print("  mMSimDataTypeIconId=");
        pw.print(Integer.toHexString(mMSimDataTypeIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimDataTypeIconId[subscription]));

        pw.println("  - wifi ------");
        pw.print("  mWifiEnabled=");
        pw.println(mWifiEnabled);
        pw.print("  mWifiConnected=");
        pw.println(mWifiConnected);
        pw.print("  mWifiRssi=");
        pw.println(mWifiRssi);
        pw.print("  mWifiLevel=");
        pw.println(mWifiLevel);
        pw.print("  mWifiSsid=");
        pw.println(mWifiSsid);
        pw.println(String.format("  mWifiIconId=0x%08x/%s",
                    mWifiIconId, getResourceName(mWifiIconId)));
        pw.print("  mWifiActivity=");
        pw.println(mWifiActivity);

        if (mWimaxSupported) {
            pw.println("  - wimax ------");
            pw.print("  mIsWimaxEnabled="); pw.println(mIsWimaxEnabled);
            pw.print("  mWimaxConnected="); pw.println(mWimaxConnected);
            pw.print("  mWimaxIdle="); pw.println(mWimaxIdle);
            pw.println(String.format("  mWimaxIconId=0x%08x/%s",
                        mWimaxIconId, getResourceName(mWimaxIconId)));
            pw.println(String.format("  mWimaxSignal=%d", mWimaxSignal));
            pw.println(String.format("  mWimaxState=%d", mWimaxState));
            pw.println(String.format("  mWimaxExtraState=%d", mWimaxExtraState));
        }

        pw.println("  - Bluetooth ----");
        pw.print("  mBtReverseTethered=");
        pw.println(mBluetoothTethered);

        pw.println("  - connectivity ------");
        pw.print("  mInetCondition=");
        pw.println(mInetCondition);

        pw.println("  - icons ------");
        pw.print("  mMSimLastPhoneSignalIconId=0x");
        pw.print(Integer.toHexString(mMSimLastPhoneSignalIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimLastPhoneSignalIconId[subscription]));
        pw.print("  mMSimLastDataDirectionIconId=0x");
        pw.print(Integer.toHexString(mMSimLastDataDirectionIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimLastDataDirectionIconId[subscription]));
        pw.print("  mLastDataDirectionOverlayIconId=0x");
        pw.print(Integer.toHexString(mLastDataDirectionOverlayIconId));
        pw.print("/");
        pw.println(getResourceName(mLastDataDirectionOverlayIconId));
        pw.print("  mLastWifiIconId=0x");
        pw.print(Integer.toHexString(mLastWifiIconId));
        pw.print("/");
        pw.println(getResourceName(mLastWifiIconId));
        pw.print("  mMSimLastCombinedSignalIconId=0x");
        pw.print(Integer.toHexString(mMSimLastCombinedSignalIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimLastCombinedSignalIconId[subscription]));
        pw.print("  mMSimLastDataTypeIconId=0x");
        pw.print(Integer.toHexString(mMSimLastDataTypeIconId[subscription]));
        pw.print("/");
        pw.println(getResourceName(mMSimLastDataTypeIconId[subscription]));
        pw.print("  mMSimLastCombinedLabel=");
        pw.print(mLastCombinedLabel);
        pw.println("");
    }
    
    private String getAuroraPlmn(int subscription, Context context){
    	String operator = null;
    	operator = GnTelephonyManager.getNetworkOperatorGemini(subscription);
    	if(operator!=null){ 
    		if(operator.equals("46000") || operator.equals("46002")|| operator.equals("46007")){
                return context.getString(
                    R.string.China_Mobile);
    		}else if(operator.equals("46001")){
    			return context.getString(R.string.China_Unicom);
    		}else if(operator.equals("46003")){
    			return context.getString(R.string.China_Telecom);
    		}else{
    			return GnTelephonyManager.getNetworkOperatorNameGemini(subscription);
    		}
    	}
    	return null;
    }
}
