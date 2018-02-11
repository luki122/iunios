/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.incallui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import com.android.incallui.Call;
import com.android.incallui.InCallActivity;
import com.android.incallui.InCallApp;
import com.android.incallui.Log;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.ContactInfoCache.ContactInfoCacheCallback;
import com.android.incallui.ContactInfoCache.ContactInfoUpdatedListener;
import com.android.incallui.InCallPresenter.InCallDetailsListener;
import com.android.incallui.InCallPresenter.InCallEventListener;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.InCallPresenter.IncomingCallListener;
import com.android.incalluibind.ObjectFactory;

import java.lang.ref.WeakReference;

import com.google.common.base.Preconditions;

import android.telephony.SubscriptionManager;

/**
 * Presenter for the Call Card Fragment.
 * <p>
 * This class listens for changes to InCallState and passes it along to the fragment.
 */
public class AuroraCallCardPresenter extends Presenter<AuroraCallCardPresenter.CallCardUi>
        implements InCallStateListener, IncomingCallListener, InCallDetailsListener{

    private static final String TAG = CallCardPresenter.class.getSimpleName();
    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 1000;

    private Call mPrimary;
    private Call mSecondary;
    private ContactCacheEntry mPrimaryContactInfo;
    private ContactCacheEntry mSecondaryContactInfo;
    private CallTimer mCallTimer;
    private Context mContext;
    private TelecomManager mTelecomManager;

    public static class ContactLookupCallback implements ContactInfoCacheCallback {
        private final WeakReference<AuroraCallCardPresenter> mCallCardPresenter;
        private final boolean mIsPrimary;

        public ContactLookupCallback(AuroraCallCardPresenter callCardPresenter, boolean isPrimary) {
            mCallCardPresenter = new WeakReference<AuroraCallCardPresenter>(callCardPresenter);
            mIsPrimary = isPrimary;
        }

        @Override
        public void onContactInfoComplete(String callId, ContactCacheEntry entry) {
        	AuroraCallCardPresenter presenter = mCallCardPresenter.get();
            if (presenter != null) {
                presenter.onContactInfoComplete(callId, entry, mIsPrimary);
            }
        }

        @Override
        public void onImageLoadComplete(String callId, ContactCacheEntry entry) {
        	AuroraCallCardPresenter presenter = mCallCardPresenter.get();
            if (presenter != null) {
                presenter.onImageLoadComplete(callId, entry);
            }
        }

    }

    public AuroraCallCardPresenter() {
        // create the call timer
        mCallTimer = new CallTimer(new Runnable() {
            @Override
            public void run() {
                updateCallTime();
            }
        });
    }

    public void init(Context context, Call call) {
        mContext = Preconditions.checkNotNull(context);

        /// M: For volte @{
        // Here we will use "mContext", so need add here, instead of "onUiReady()"
        ContactInfoCache.getInstance(mContext).addContactInfoUpdatedListener(mContactInfoUpdatedListener);
        /// @}

        // Call may be null if disconnect happened already.
        if (call != null) {
            mPrimary = call;

            // start processing lookups right away.
            if (!call.isConferenceCall()) {
                startContactInfoSearch(call, true, call.getState() == Call.State.INCOMING);
            } else {
                updateContactEntry(null, true, true);
            }
        }
    }

    @Override
    public void onUiReady(CallCardUi ui) {
        super.onUiReady(ui);

        // Contact search may have completed before ui is ready.
        if (mPrimaryContactInfo != null) {
            updatePrimaryDisplayInfo(mPrimaryContactInfo, isConference(mPrimary));
        }

        // Register for call state changes last
        InCallPresenter.getInstance().addListener(this);
        InCallPresenter.getInstance().addIncomingCallListener(this);
        InCallPresenter.getInstance().addDetailsListener(this);
    }

    @Override
    public void onUiUnready(CallCardUi ui) {
        super.onUiUnready(ui);

        // stop getting call state changes
        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        InCallPresenter.getInstance().removeDetailsListener(this);
        /// M: For volte @{
        ContactInfoCache.getInstance(mContext).removeContactInfoUpdatedListener(mContactInfoUpdatedListener);
        /// @}

        mPrimary = null;
        mPrimaryContactInfo = null;
        mSecondaryContactInfo = null;
    }

    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState, Call call) {
        // same logic should happen as with onStateChange()
        onStateChange(oldState, newState, CallList.getInstance());
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        Log.d(this, "onStateChange() " + newState);
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }

        Call primary = null;
        Call secondary = null;

    	AuroraCallCardAnimationController ac = InCallApp.getInCallActivity().mCallCardAnimationController;
        if (newState == InCallState.INCOMING) { 
    		if (ac.isRejectToEndStart()) {
				return;
			}

			if (ac.isAnimationingInRinging()) {
				return;
			}

            primary = callList.getIncomingCall();
			ac.updateRingingCall(primary);
        } else if (newState == InCallState.PENDING_OUTGOING || newState == InCallState.OUTGOING) {
            primary = callList.getOutgoingCall();
            if (primary == null) {
                primary = callList.getPendingOutgoingCall();
            }

            // getCallToDisplay doesn't go through outgoing or incoming calls. It will return the
            // highest priority call to display as the secondary call.
            secondary = getCallToDisplay(callList, null, true);
            ac.updateForegroundCall(primary, secondary);
        } else if (newState == InCallState.INCALL) {
            primary = getCallToDisplay(callList, null, false);
            secondary = getCallToDisplay(callList, primary, true);
            ac.updateForegroundCall(primary, secondary);
        }

        Log.d(this, "Primary call: " + primary);
        Log.d(this, "Secondary call: " + secondary);

        final boolean primaryChanged = !Call.areSame(mPrimary, primary);
        final boolean secondaryChanged = !Call.areSame(mSecondary, secondary);

        mSecondary = secondary;
        mPrimary = primary;

        if (primaryChanged && mPrimary != null) {
            // primary call has changed
            mPrimaryContactInfo = ContactInfoCache.buildCacheEntryFromCall(mContext, mPrimary,
                    mPrimary.getState() == Call.State.INCOMING);
            updatePrimaryDisplayInfo(mPrimaryContactInfo, isConference(mPrimary));
            maybeStartSearch(mPrimary, true);
            mPrimary.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
        }

        if (mSecondary == null) {
            // Secondary call may have ended.  Update the ui.
            mSecondaryContactInfo = null;
            updateSecondaryDisplayInfo(false);
        } else if (secondaryChanged) {
            // secondary call has changed
            mSecondaryContactInfo = ContactInfoCache.buildCacheEntryFromCall(mContext, mSecondary,
                    mSecondary.getState() == Call.State.INCOMING);
            updateSecondaryDisplayInfo(mSecondary.isConferenceCall());
            maybeStartSearch(mSecondary, false);
            mSecondary.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
        }

        // Start/stop timers.
        if (mPrimary != null && mPrimary.getState() == Call.State.ACTIVE) {
            Log.d(this, "Starting the calltime timer");
            mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
        } else {
            Log.d(this, "Canceling the calltime timer");
            mCallTimer.cancel();
            ui.setPrimaryCallElapsedTime(false, null);
        }

        // Set the call state
        int callState = Call.State.IDLE;
        if (mPrimary != null) {
            callState = mPrimary.getState();
            updatePrimaryCallState();
        } else {
            getUi().setCallState(
                    callState,
                    VideoProfile.VideoState.AUDIO_ONLY,
                    Call.SessionModificationState.NO_REQUEST,
                    new DisconnectCause(DisconnectCause.UNKNOWN),
                    null,
                    null,
                    null);
        }

        // Hide/show the contact photo based on the video state.
        // If the primary call is a video call on hold, still show the contact photo.
        // If the primary call is an active video call, hide the contact photo.
        if (mPrimary != null) {
//            getUi().setPhotoVisible(!(mPrimary.isVideoCall(mContext) &&
//                    callState != Call.State.HOLD));
          getUi().setPhotoVisible(!mPrimary.isVideoCall(mContext) && callState != Call.State.ACTIVE);
        }

        maybeShowManageConferenceCallButton();

        final boolean enableEndCallButton = Call.State.isConnectingOrConnected(callState) &&
                callState != Call.State.INCOMING && mPrimary != null;
    }

    @Override
    public void onDetailsChanged(Call call, android.telecom.Call.Details details) {
        updatePrimaryCallState();
    }

    private String getSubscriptionNumber() {
        // If it's an emergency call, and they're not populating the callback number,
        // then try to fall back to the phone sub info (to hopefully get the SIM's
        // number directly from the telephony layer).
        PhoneAccountHandle accountHandle = mPrimary.getAccountHandle();
        if (accountHandle != null) {
            TelecomManager mgr =
                    (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
            PhoneAccount account = mgr.getPhoneAccount(accountHandle);
            if (account != null) {
                return getNumberFromHandle(account.getSubscriptionAddress());
            }
        }
        return null;
    }

    private void updatePrimaryCallState() {
        if (getUi() != null && mPrimary != null) {
            getUi().setCallState(
                    mPrimary.getState(),
                    mPrimary.getVideoState(),
                    mPrimary.getSessionModificationState(),
                    mPrimary.getDisconnectCause(),
                    getConnectionLabel(),
                    getConnectionIcon(),
                    getGatewayNumber());
            setCallbackNumber();
        }
    }

    /**
     * Only show the conference call button if we can manage the conference.
     */
    private void maybeShowManageConferenceCallButton() {
        if (mPrimary == null) {
            getUi().showManageConferenceCallButton(false);
            return;
        }

        final boolean canManageConference = mPrimary.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE);
        getUi().showManageConferenceCallButton(mPrimary.isConferenceCall() && canManageConference);
    }

    private void setCallbackNumber() {
        String callbackNumber = null;

        boolean isEmergencyCall = PhoneNumberUtils.isEmergencyNumber(
                getNumberFromHandle(mPrimary.getHandle()));
        if (isEmergencyCall) {
            callbackNumber = getSubscriptionNumber();
        } else {
            StatusHints statusHints = mPrimary.getTelecommCall().getDetails().getStatusHints();
            if (statusHints != null) {
                Bundle extras = statusHints.getExtras();
                if (extras != null) {
                    callbackNumber = extras.getString(TelecomManager.EXTRA_CALL_BACK_NUMBER);
                }
            }
        }

        TelephonyManager telephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String simNumber = telephonyManager.getLine1Number();
        if (PhoneNumberUtils.compare(callbackNumber, simNumber)) {
            Log.d(this, "Numbers are the same; not showing the callback number");
            callbackNumber = null;
        }

        getUi().setCallbackNumber(callbackNumber, isEmergencyCall);
    }

    public void updateCallTime() {
        final CallCardUi ui = getUi();

        if (ui == null || mPrimary == null || mPrimary.getState() != Call.State.ACTIVE) {
            if (ui != null) {
                ui.setPrimaryCallElapsedTime(false, null);
            }
            mCallTimer.cancel();
        } else {
            final long callStart = mPrimary.getConnectTimeMillis();
            final long duration = System.currentTimeMillis() - callStart;
            ui.setPrimaryCallElapsedTime(true, DateUtils.formatElapsedTime(duration / 1000));
        }
    }

    public void onCallStateButtonTouched() {
        Intent broadcastIntent = ObjectFactory.getCallStateButtonBroadcastIntent(mContext);
        if (broadcastIntent != null) {
            Log.d(this, "Sending call state button broadcast: ", broadcastIntent);
            mContext.sendBroadcast(broadcastIntent, Manifest.permission.READ_PHONE_STATE);
        }
    }

    private void maybeStartSearch(Call call, boolean isPrimary) {
        // no need to start search for conference calls which show generic info.
        if (call != null && !call.isConferenceCall()) {
            startContactInfoSearch(call, isPrimary, call.getState() == Call.State.INCOMING);
        }
    }

    /**
     * Starts a query for more contact data for the save primary and secondary calls.
     */
    private void startContactInfoSearch(final Call call, final boolean isPrimary,
            boolean isIncoming) {
        final ContactInfoCache cache = ContactInfoCache.getInstance(mContext);

        cache.findInfo(call, isIncoming, new ContactLookupCallback(this, isPrimary));
    }

    private void onContactInfoComplete(String callId, ContactCacheEntry entry, boolean isPrimary) {
        updateContactEntry(entry, isPrimary, false);
        if (entry.name != null) {
            Log.d(TAG, "Contact found: " + entry);
        }
        if (entry.contactUri != null) {
            CallerInfoUtils.sendViewNotification(mContext, entry.contactUri);
        }
    }

    private void onImageLoadComplete(String callId, ContactCacheEntry entry) {
        if (getUi() == null) {
            return;
        }

        if (entry.photo != null) {
            if (mPrimary != null && callId.equals(mPrimary.getId())) {
                getUi().setPrimaryImage(entry.photo);
            }
        }
    }

    private static boolean isConference(Call call) {
        return call != null && call.isConferenceCall();
    }

    private static boolean canManageConference(Call call) {
        return call != null && call.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE);
    }

    private void updateContactEntry(ContactCacheEntry entry, boolean isPrimary,
            boolean isConference) {
        if (isPrimary) {
            mPrimaryContactInfo = entry;
            updatePrimaryDisplayInfo(entry, isConference);
        } else {
            mSecondaryContactInfo = entry;
            updateSecondaryDisplayInfo(isConference);
        }
    }

    /**
     * Get the highest priority call to display.
     * Goes through the calls and chooses which to return based on priority of which type of call
     * to display to the user. Callers can use the "ignore" feature to get the second best call
     * by passing a previously found primary call as ignore.
     *
     * @param ignore A call to ignore if found.
     */
    private Call getCallToDisplay(CallList callList, Call ignore, boolean skipDisconnected) {

        // Active calls come second.  An active call always gets precedent.
        Call retval = callList.getActiveCall();
        if (retval != null && retval != ignore) {
            return retval;
        }

        // Disconnected calls get primary position if there are no active calls
        // to let user know quickly what call has disconnected. Disconnected
        // calls are very short lived.
        if (!skipDisconnected) {
            retval = callList.getDisconnectingCall();
            if (retval != null && retval != ignore) {
                return retval;
            }
            retval = callList.getDisconnectedCall();
            if (retval != null && retval != ignore) {
                return retval;
            }
        }

        // Then we go to background call (calls on hold)
        retval = callList.getBackgroundCall();
        if (retval != null && retval != ignore) {
            return retval;
        }

        // Lastly, we go to a second background call.
        retval = callList.getSecondBackgroundCall();

        return retval;
    }

    private void updatePrimaryDisplayInfo(ContactCacheEntry entry, boolean isConference) {
        Log.d(TAG, "Update primary display " + entry);
        final CallCardUi ui = getUi();
        if (ui == null) {
            // TODO: May also occur if search result comes back after ui is destroyed. Look into
            // removing that case completely.
            Log.d(TAG, "updatePrimaryDisplayInfo called but ui is null!");
            return;
        }

        final boolean canManageConference = canManageConference(mPrimary);
        
        if (entry != null && mPrimary != null) {
            final String name = getNameForCall(entry);
            final String number = getNumberForCall(entry);
            final String area = getAreaForCall(entry);
            final String note = getNoteForCall(entry);
            final String mark = getMarkForCall(entry);
            final String markNumber = getMarkNumberForCall(entry);    
            final String slogan = getSlogan();
            final boolean nameIsNumber = name != null && name.equals(entry.number);
            ui.setPrimary(number, name, nameIsNumber, entry.label,
                    entry.photo, isConference, canManageConference, entry.isSipCall);
            ui.setAuroraPrimary(area, note, mark, markNumber, slogan);
            if(entry.mPrivateId > 0 && entry.mPrivateId != AuroraPrivacyUtils.getCurrentAccountId()) {
            	ui.hideViewsForPrivate();
            }
            PhoneAccountHandle accountHandle = mPrimary.getAccountHandle();
            if(accountHandle != null) {
            	 boolean isEmergencyCall = PhoneNumberUtils.isEmergencyNumber(
                         getNumberFromHandle(mPrimary.getHandle()));
            	 if(!isEmergencyCall) {
		            int slot = SubscriptionManager.from(mContext).getSlotId(Integer.valueOf(accountHandle.getId()));
		            ui.updataSimIcon(slot);
            	 }
            }
        } else {
            ui.setPrimary(null, null, false, null, null, isConference, canManageConference, false);
            ui.setAuroraPrimary(null, null, null, null, null);
        }

    }

    private void updateSecondaryDisplayInfo(boolean isConference) {
        final CallCardUi ui = getUi();
        if (ui == null) {
            return;
        }

        final boolean canManageConference = canManageConference(mSecondary);
//        if (mSecondaryContactInfo != null && mSecondary != null) {
//            Log.d(TAG, "updateSecondaryDisplayInfo() " + mSecondaryContactInfo);
//            final String nameForCall = getNameForCall(mSecondaryContactInfo);
//
//            final boolean nameIsNumber = nameForCall != null && nameForCall.equals(
//                    mSecondaryContactInfo.number);
//            ui.setSecondary(true /* show */, nameForCall, nameIsNumber, mSecondaryContactInfo.label,
//                    getCallProviderLabel(mSecondary), getCallProviderIcon(mSecondary),
//                    isConference, canManageConference);
//        } else {
//            // reset to nothing so that it starts off blank next time we use it.
//            ui.setSecondary(false, null, false, null, null, null, isConference, canManageConference);
//        }
        
	      if (mSecondaryContactInfo != null && mSecondary != null) {
	      Log.d(TAG, "updateSecondaryDisplayInfo() " + mSecondaryContactInfo);
	      final String nameForCall = getNameForCall(mSecondaryContactInfo);
	
	      final boolean nameIsNumber = nameForCall != null && nameForCall.equals(
	              mSecondaryContactInfo.number);
		      PhoneAccountHandle accountHandle = mSecondary.getAccountHandle();
	          int slot = SubscriptionManager.from(mContext).getSlotId(Integer.valueOf(accountHandle.getId()));
		      ui.setAuroraSecondary(true /* show */, nameForCall, nameIsNumber, mSecondaryContactInfo.label,
		    		  TelephonyManager.getDefault().isMultiSimEnabled() ? SimIconUtils.getSmallSimIcon(slot): 0, mSecondaryContactInfo.photo,
		              isConference, canManageConference);
		  } else {
		      // reset to nothing so that it starts off blank next time we use it.
		      ui.setAuroraSecondary(false, null, false, null, 0, null, isConference, canManageConference);
		  }
    }


    /**
     * Gets the phone account to display for a call.
     */
    private PhoneAccount getAccountForCall(Call call) {
        PhoneAccountHandle accountHandle = call.getAccountHandle();
        if (accountHandle == null) {
            return null;
        }
        return getTelecomManager().getPhoneAccount(accountHandle);
    }

    /**
     * Returns the gateway number for any existing outgoing call.
     */
    private String getGatewayNumber() {
        if (hasOutgoingGatewayCall()) {
            return getNumberFromHandle(mPrimary.getGatewayInfo().getGatewayAddress());
        }
        return null;
    }

    /**
     * Return the Drawable object of the icon to display to the left of the connection label.
     */
    private Drawable getCallProviderIcon(Call call) {
//        PhoneAccount account = getAccountForCall(call);
//        if (account != null && getTelecomManager().hasMultipleCallCapableAccounts()) {
//            return account.getIcon(mContext);
//        }
        return null;
    }

    /**
     * Return the string label to represent the call provider
     */
    private String getCallProviderLabel(Call call) {
        PhoneAccount account = getAccountForCall(call);
        if (account != null && getTelecomManager().hasMultipleCallCapableAccounts()) {
        	Log.d(this, "getCallProviderLabel = " + account.getLabel().toString());
            return account.getLabel().toString();
        }
        return null;
    }

    /**
     * Returns the label (line of text above the number/name) for any given call.
     * For example, "calling via [Account/Google Voice]" for outgoing calls.
     */
    private String getConnectionLabel() {
        StatusHints statusHints = mPrimary.getTelecommCall().getDetails().getStatusHints();
        if (statusHints != null && !TextUtils.isEmpty(statusHints.getLabel())) {
            return statusHints.getLabel().toString();
        }

        if (hasOutgoingGatewayCall() && getUi() != null) {
            // Return the label for the gateway app on outgoing calls.
            final PackageManager pm = mContext.getPackageManager();
            try {
                ApplicationInfo info = pm.getApplicationInfo(
                        mPrimary.getGatewayInfo().getGatewayProviderPackageName(), 0);
                return pm.getApplicationLabel(info).toString();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(this, "Gateway Application Not Found.", e);
                return null;
            }
        }
        return getCallProviderLabel(mPrimary);
    }

    private Drawable getConnectionIcon() {
        StatusHints statusHints = mPrimary.getTelecommCall().getDetails().getStatusHints();
        if (statusHints != null && statusHints.getIconResId() != 0) {
            Drawable icon = statusHints.getIcon(mContext);
            if (icon != null) {
                return icon;
            }
        }
        return getCallProviderIcon(mPrimary);
    }

    private boolean hasOutgoingGatewayCall() {
        // We only display the gateway information while STATE_DIALING so return false for any othe
        // call state.
        // TODO: mPrimary can be null because this is called from updatePrimaryDisplayInfo which
        // is also called after a contact search completes (call is not present yet).  Split the
        // UI update so it can receive independent updates.
        if (mPrimary == null) {
            return false;
        }
        return Call.State.isDialing(mPrimary.getState()) && mPrimary.getGatewayInfo() != null &&
                !mPrimary.getGatewayInfo().isEmpty();
    }

    /**
     * Gets the name to display for the call.
     */
    private static String getNameForCall(ContactCacheEntry contactInfo) {
        if (TextUtils.isEmpty(contactInfo.name)) {
            return contactInfo.number;
        }
        return contactInfo.name;
    }

    /**
     * Gets the number to display for a call.
     */
    private static String getNumberForCall(ContactCacheEntry contactInfo) {
        // If the name is empty, we use the number for the name...so dont show a second
        // number in the number field
        if (TextUtils.isEmpty(contactInfo.name)) {
//            return contactInfo.location;
        	return null;
        }
        return contactInfo.number;
    }
    
    private static String getAreaForCall(ContactCacheEntry contactInfo) {
        return contactInfo.mArea;
    }
    
    private static String getNoteForCall(ContactCacheEntry contactInfo) {
        return contactInfo.mNote;
    }
    
    private static String getMarkForCall(ContactCacheEntry contactInfo) {
        return contactInfo.mMark;
    }
    
    private static String getMarkNumberForCall(ContactCacheEntry contactInfo) {
        return contactInfo.mMarkNumber;
    }
    
    private static String getSlogan() {
    	if(YuLoreUtils.isInit()) {
	    	return YuLoreUtils.getSlogan();
	    } 
    	return null;
    }
    

    public void secondaryInfoClicked() {
        if (mSecondary == null) {
            Log.wtf(this, "Secondary info clicked but no secondary call.");
            return;
        }

        Log.i(this, "Swapping call to foreground: " + mSecondary);
        TelecomAdapter.getInstance().unholdCall(mSecondary.getId());
    }

    public void endCallClicked() {
        if (mPrimary == null) {
            return;
        }

        Log.i(this, "Disconnecting call: " + mPrimary);
        mPrimary.setState(Call.State.DISCONNECTING);
        CallList.getInstance().onUpdate(mPrimary);
        TelecomAdapter.getInstance().disconnectCall(mPrimary.getId());
    }

    private String getNumberFromHandle(Uri handle) {
        return handle == null ? "" : handle.getSchemeSpecificPart();
    }



    public interface CallCardUi extends Ui {
        void setVisible(boolean on);
        void setPrimary(String number, String name, boolean nameIsNumber, String label,
                Drawable photo, boolean isConference, boolean canManageConference,
                boolean isSipCall);
        void setSecondary(boolean show, String name, boolean nameIsNumber, String label,
                String providerLabel, Drawable providerIcon, boolean isConference,
                boolean canManageConference);
        void setCallState(int state, int videoState, int sessionModificationState,
                DisconnectCause disconnectCause, String connectionLabel,
                Drawable connectionIcon, String gatewayNumber);
        void setPrimaryCallElapsedTime(boolean show, String duration);
        void setPrimaryName(String name, boolean nameIsNumber);
        void setPrimaryImage(Drawable image);
        void setPrimaryPhoneNumber(String phoneNumber);
        void setPrimaryLabel(String label);
        void setCallbackNumber(String number, boolean isEmergencyCalls);
        void setPhotoVisible(boolean isVisible);
        void setProgressSpinnerVisible(boolean visible);
        void showManageConferenceCallButton(boolean visible);
        void setAuroraPrimary(String area, String note, String mark, String markcount, String slogan);
        void setAuroraSecondary(boolean show, String name, boolean nameIsNumber, String label,
                int simicon, Drawable photo, boolean isConference,
                boolean canManageConference);
        void hideViewsForPrivate();
        void updataSimIcon(int slot);
    }

    private TelecomManager getTelecomManager() {
        if (mTelecomManager == null) {
            mTelecomManager =
                    (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        }
        return mTelecomManager;
    }
    
    public boolean isRinging() {
    	return mPrimary != null && (mPrimary.getState() == Call.State.INCOMING || mPrimary.getState() == Call.State.CALL_WAITING);
    }
    
    public boolean isDialing() {
    	return mPrimary != null && (mPrimary.getState() == Call.State.DIALING);
    }
    
    public boolean isConference() {
    	return isConference(mPrimary);
    }
    
    /// M: For volte @{
    /**
     * listner onContactInfoUpdated(),
     * will be notified when ContactInfoCache finish re-query, triggered by some call's number change.
     */
    private final ContactInfoUpdatedListener mContactInfoUpdatedListener = new ContactInfoUpdatedListener() {
        public void onContactInfoUpdated(String callId) {
            handleContactInfoUpdated(callId);
        }
    };
    
    /**
     * ask for new ContactInfo to update UI when re-query complete by ContactInfoCache.
     */
    private void handleContactInfoUpdated(String callId) {
        Log.d(this, "handleContactInfoUpdated()... callId = " + callId);
        Call call = null;
        boolean isPrimary = false;
        if(mPrimary != null && mPrimary.getId() == callId) {
        	isPrimary = true;
            call = mPrimary;
        } else if(mSecondary != null && mSecondary.getId() == callId) {
        	isPrimary = false;
            call = mSecondary;
        } 
        if(call != null) {
            startContactInfoSearch(call, true, call.getState() == Call.State.INCOMING);
        }
    }
    /// @}
}
