/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.telecom;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.Manifest.permission;
import android.net.Uri;
import android.os.AsyncTask;

//import android.provider.CallLog.Calls;
import com.android.server.telecom.AuroraCallLog.Calls;

import android.provider.CallLog.ConferenceCalls;
import android.telecom.CallState;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.VideoProfile;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;
import android.text.TextUtils;
import android.telecom.PhoneAccount;


// TODO: Needed for move to system service: import com.android.internal.R;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.telecom.volte.TelecomVolteUtils;

import java.util.ArrayList;

/**
 * Helper class that provides functionality to write information about calls and their associated
 * caller details to the call log. All logging activity will be performed asynchronously in a
 * background thread to avoid blocking on the main thread.
 */
final class CallLogManager extends CallsManagerListenerBase {
    /**
     * Parameter object to hold the arguments to add a call in the call log DB.
     */
    private static class AddCallArgs {
        /**
         * @param callerInfo Caller details.
         * @param number The phone number to be logged.
         * @param presentation Number presentation of the phone number to be logged.
         * @param callType The type of call (e.g INCOMING_TYPE). @see
         *     {@link android.provider.CallLog} for the list of values.
         * @param features The features of the call (e.g. FEATURES_VIDEO). @see
         *     {@link android.provider.CallLog} for the list of values.
         * @param creationDate Time when the call was created (milliseconds since epoch).
         * @param durationInMillis Duration of the call (milliseconds).
         * @param dataUsage Data usage in bytes, or null if not applicable.
         */
        public AddCallArgs(Context context, AuroraCallerInfo AuroraCallerInfo, String number,
                int presentation, int callType, int features, PhoneAccountHandle accountHandle,
                long creationDate, long durationInMillis, Long dataUsage, long simId) {
            this.context = context;
            this.ci = AuroraCallerInfo;
            this.number = number;
            this.presentation = presentation;
            this.callType = callType;
            this.features = features;
            this.accountHandle = accountHandle;
            this.timestamp = creationDate;
            this.durationInSec = (int)(durationInMillis / 1000);
            this.dataUsage = dataUsage;
			this.mSimId = simId;
        }
        // Since the members are accessed directly, we don't use the
        // mXxxx notation.
        public final Context context;
        public final AuroraCallerInfo ci;
        public final String number;
        public final int presentation;
        public final int callType;
        public final int features;
        public final PhoneAccountHandle accountHandle;
        public final long timestamp;
        public final int durationInSec;
        public final Long dataUsage;
        /// M: For Volte conference call calllog
        public long conferenceCallLogId = -1;
        public final long mSimId;
    }

    private static final String TAG = CallLogManager.class.getSimpleName();

    private final Context mContext;
    private static final String ACTION_CALLS_TABLE_ADD_ENTRY =
                "com.android.server.telecom.intent.action.CALLS_ADD_ENTRY";
    private static final String PERMISSION_PROCESS_CALLLOG_INFO =
                "android.permission.PROCESS_CALLLOG_INFO";
    private static final String CALL_TYPE = "callType";
    private static final String CALL_DURATION = "duration";

    public CallLogManager(Context context) {
        mContext = context;
    }

    @Override
    public void onCallStateChanged(Call call, int oldState, int newState) {
        int disconnectCause = call.getDisconnectCause().getCode();
        boolean isNewlyDisconnected =
                newState == CallState.DISCONNECTED || newState == CallState.ABORTED;
        boolean isCallCanceled = isNewlyDisconnected && disconnectCause == DisconnectCause.CANCELED;

        // Log newly disconnected calls only if:
        // 1) It was not in the "choose account" phase when disconnected
        // 2) It is a conference call
        // 3) Call was not explicitly canceled
        if (isNewlyDisconnected &&
                (oldState != CallState.PRE_DIAL_WAIT &&
                 !call.isConference() &&
                 !isCallCanceled)) {
            int type;
            if (!call.isIncoming()) {
                type = Calls.OUTGOING_TYPE;
            } else if (disconnectCause == DisconnectCause.MISSED) {
                type = Calls.MISSED_TYPE;
            } else {
                type = Calls.INCOMING_TYPE;
            }
            logCall(call, type);

            /// M: Show call duration @{
            if (oldState != CallState.DIALING && oldState != CallState.RINGING) {
                showCallDuration(call);
            }
            /// @}
        }
    }

    /**
     * Logs a call to the call log based on the {@link Call} object passed in.
     *
     * @param call The call object being logged
     * @param callLogType The type of call log entry to log this call as. See:
     *     {@link android.provider.CallLog.Calls#INCOMING_TYPE}
     *     {@link android.provider.CallLog.Calls#OUTGOING_TYPE}
     *     {@link android.provider.CallLog.Calls#MISSED_TYPE}
     */
    void logCall(Call call, int callLogType) {
    	//aurora change
//        final long creationTime = call.getCreationTimeMillis();
    	long creationTime = call.getConnectTimeMillis(); 
    	if(creationTime == 0) {
    		creationTime = call.getCreationTimeMillis();
    	}
        final long age = call.getAgeMillis();

        final String logNumber = getLogNumber(call);
        /// M: ALPS01899538, when dial a empty voice mail number fail, should not log this @{
        if (PhoneAccount.SCHEME_VOICEMAIL.equals(getLogScheme(call))
                && TextUtils.isEmpty(logNumber)) {
            Log.d(TAG, "Empty voice mail logNumber");
            return;
        }
        /// @}

        Log.d(TAG, "logNumber set to: %s", Log.pii(logNumber));

        final PhoneAccountHandle accountHandle = call.getTargetPhoneAccount();

        // TODO(vt): Once data usage is available, wire it up here.
        int callFeatures = getCallFeatures(call.getVideoStateHistory());
        logCall(call.getCallerInfo(), logNumber, call.getHandlePresentation(),
                callLogType, callFeatures, accountHandle, creationTime, age, null,
                call /* M: For Volte Conference call */);
    }

    /**
     * Inserts a call into the call log, based on the parameters passed in.
     *
     * @param callerInfo Caller details.
     * @param number The number the call was made to or from.
     * @param presentation
     * @param callType The type of call.
     * @param features The features of the call.
     * @param start The start time of the call, in milliseconds.
     * @param duration The duration of the call, in milliseconds.
     * @param dataUsage The data usage for the call, null if not applicable.
     */
    private void logCall(
            AuroraCallerInfo AuroraCallerInfo,
            String number,
            int presentation,
            int callType,
            int features,
            PhoneAccountHandle accountHandle,
            long start,
            long duration,
            Long dataUsage,
            Call call /* For Volte Conference call */) {
        boolean isEmergencyNumber = PhoneNumberUtils.isLocalEmergencyNumber(mContext, number);

        // On some devices, to avoid accidental redialing of emergency numbers, we *never* log
        // emergency calls to the Call Log.  (This behavior is set on a per-product basis, based
        // on carrier requirements.)
        final boolean okToLogEmergencyNumber =
                mContext.getResources().getBoolean(R.bool.allow_emergency_numbers_in_call_log);

        // Don't log emergency numbers if the device doesn't allow it.
        final boolean isOkToLogThisCall = !isEmergencyNumber || okToLogEmergencyNumber;

        sendAddCallBroadcast(callType, duration);

        if (isOkToLogThisCall) {
            Log.d(TAG, "Logging Calllog entry: " + AuroraCallerInfo + ", "
                    + Log.pii(number) + "," + presentation + ", " + callType
                    + ", " + start + ", " + duration);
            AddCallArgs args = new AddCallArgs(mContext, AuroraCallerInfo, number, presentation,
                    callType, features, accountHandle, start, duration, dataUsage, Long.valueOf(accountHandle.getId()));
            /// M: For Volte conference call calllog @{
            logCallAsync(args, call);
            /// @}
        } else {
          Log.d(TAG, "Not adding emergency call to call log.");
        }
        
        if(callType == Calls.INCOMING_TYPE && RejectUtils.isToAddBlack(number)) {         
            String name = "";
            if(AuroraCallerInfo != null) {
            	name = AuroraCallerInfo.name;
            }
            AuroraGlobals.getInstance().mManageReject.notificationMgr.notifyAddBlackCall(number , name);
        }
    }

    /**
     * Based on the video state of the call, determines the call features applicable for the call.
     *
     * @param videoState The video state.
     * @return The call features.
     */
    private static int getCallFeatures(int videoState) {
        if ((videoState & VideoProfile.VideoState.TX_ENABLED)
                == VideoProfile.VideoState.TX_ENABLED) {
            return Calls.FEATURES_VIDEO;
        }
        return 0;
    }

    /**
     * Retrieve the phone number from the call, and then process it before returning the
     * actual number that is to be logged.
     *
     * @param call The phone connection.
     * @return the phone number to be logged.
     */
    private String getLogNumber(Call call) {
        Uri handle = call.getOriginalHandle();

        if (handle == null) {
            return null;
        }

        String handleString = handle.getSchemeSpecificPart();
        if (!PhoneNumberUtils.isUriNumber(handleString)) {
            handleString = PhoneNumberUtils.stripSeparators(handleString);
        }
        return handleString;
    }

    /**
     * Adds the call defined by the parameters in the provided AddCallArgs to the CallLogProvider
     * using an AsyncTask to avoid blocking the main thread.
     *
     * @param args Prepopulated call details.
     * @return A handle to the AsyncTask that will add the call to the call log asynchronously.
     */
    public AsyncTask<AddCallArgs, Void, Uri[]> logCallAsync(AddCallArgs args) {
        return new LogCallAsyncTask().execute(args);
    }

    /**
     * Helper AsyncTask to access the call logs database asynchronously since database operations
     * can take a long time depending on the system's load. Since it extends AsyncTask, it uses
     * its own thread pool.
     */
    private class LogCallAsyncTask extends AsyncTask<AddCallArgs, Void, Uri[]> {
        @Override
        protected Uri[] doInBackground(AddCallArgs... callList) {
            int count = callList.length;
            Uri[] result = new Uri[count];
            for (int i = 0; i < count; i++) {
                AddCallArgs c = callList[i];

                try {
                    /// M: For Volte conference call calllog @{
//                    if (c.conferenceCallLogId > 0) {
//                        result[i] = Calls.addCall(c.ci, c.context, c.number, c.presentation,
//                                c.callType, c.features, c.accountHandle, c.timestamp, c.durationInSec,
//                                c.dataUsage, true /* addForAllUsers */, c.conferenceCallLogId);
//                        continue;
//                    }
                    /// @}

                    // May block.
//                    result[i] = Calls.addCall(c.AuroraCallerInfo, c.context, c.number, c.presentation,
//                            c.callType, c.features, c.accountHandle, c.timestamp, c.durationInSec,
//                            c.dataUsage, true /* addForAllUsers */);
                	
                	

					Log.d(TAG, "call log add start   ");
					if (c.ci != null) {
						c.ci.name = c.ci.mRealName;
					}

					ContentValues values = new ContentValues();

					//handleMark
					boolean isMark = false;									
					if ((c.ci == null || TextUtils.isEmpty(c.ci.name))
							&& (c.callType == Calls.INCOMING_TYPE || c.callType == Calls.MISSED_TYPE)) {
						String Mark, MarkNumber;	
						if (c.ci != null && c.ci.isCompelete) {
							Mark = c.ci.mMark;
							MarkNumber = c.ci.mMarkNumber;
						} else {
							AuroraMarkUtils.getNumberInfoInternal(c.number);
							String[] sgNote = AuroraMarkUtils.getNote(c.number);
							Mark = sgNote[0];
							MarkNumber = sgNote[1];
						}
						if (!TextUtils.isEmpty(Mark)) {
							isMark = true;
							if (!TextUtils.isEmpty(MarkNumber)) {
								values.put("user_mark", MarkNumber);
							} else {
								values.put("user_mark", -1);
							}
							values.put("mark", Mark);
						}						
					}
					Log.d(TAG, "call log add handleMark end   ");

					//handlePrivacy
					boolean isPrivate = true;
					long privateId = 0, rawContactId = 0, privateNoti = 0, dataId = 0;
					if (c.ci != null && c.ci.isCompelete) {
						privateId = c.ci.mPrivateId;
						rawContactId = c.ci.mRawContactId;
						privateNoti = c.ci.mPrivateNotiType;
					} else {
						int privateData[] = AuroraPrivacyUtils.getPrivateData(c.number);
						rawContactId = privateData != null ? privateData[0] : 0;
						privateId = privateData != null ? privateData[1] : 0;
						privateNoti = privateData != null ? privateData[2] : 0;
												
					}

					if (rawContactId > 0) {
						dataId = ProviderUtils.getDataIdByRawContactId(rawContactId);
					}

					Log.d(TAG, "call log add handlePrivacy end   ");
					
					//handleReject
					boolean isReject = false;
					long currentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
					if (c.durationInSec == 0
							&& c.callType == Calls.INCOMING_TYPE) {
						if (privateNoti == 1 && privateId != currentPrivateId) {

						} else if (RejectUtils.isBlackNumber(c.number)
								&& privateId == 0) {
							isReject = true;
							values.put("black_name",
									RejectUtils.getLastBlackName());
						}
					}
					values.put("reject", isReject ? 1 : 0);
					
					String area = "";
					if (c.ci != null && c.ci.isCompelete) {
						area = c.ci.mArea;
					} else {
						area = AuroraAreaUtils.getNumArea(c.number);; 
					}
					Log.d(TAG, "call log add area =  " + area);
					values.put("area", area);
					
					Log.d(TAG, "call log add handleReject end   ");

					result[i] = Calls.addCall(c.ci, c.context, c.number,
							c.presentation, c.callType, c.timestamp,
							c.durationInSec, (int) c.mSimId, -1, rawContactId,
							dataId, privateId);
					Log.d(TAG, "call log add:   " + result[i]);

//					if (isReject || isMark || isPrivate) {
						c.context.getContentResolver().update(result[i],
								values, "privacy_id > -1", null);
//					}    
                
                } catch (Exception e) {
                    // This is very rare but may happen in legitimate cases.
                    // E.g. If the phone is encrypted and thus write request fails, it may cause
                    // some kind of Exception (right now it is IllegalArgumentException, but this
                    // might change).
                    //
                    // We don't want to crash the whole process just because of that, so just log
                    // it instead.
                    Log.e(TAG, e, "Exception raised during adding CallLog entry.");
                    result[i] = null;
                }
            }
            return result;
        }

        /**
         * Performs a simple sanity check to make sure the call was written in the database.
         * Typically there is only one result per call so it is easy to identify which one failed.
         */
        @Override
        protected void onPostExecute(Uri[] result) {
            for (Uri uri : result) {
                if (uri == null) {
                    Log.w(TAG, "Failed to write call to the log.");
                }
            }
        }
    }

    private void sendAddCallBroadcast(int callType, long duration) {
        Intent callAddIntent = new Intent(ACTION_CALLS_TABLE_ADD_ENTRY);
        callAddIntent.putExtra(CALL_TYPE, callType);
        callAddIntent.putExtra(CALL_DURATION, duration);
        mContext.sendBroadcast(callAddIntent, PERMISSION_PROCESS_CALLLOG_INFO);
    }

    /**
     * M: ALPS01899538, add for getting scheme from call.
     * @param call
     * @return the phone number scheme to be logged.
     */
    private String getLogScheme(Call call) {
        Uri handle = call.getOriginalHandle();

        if (handle == null) {
            return null;
        }
        String scheme = handle.getScheme();
        return scheme;
    }

    /// M: Show call duration @{
    private void showCallDuration(Call call) {
        long callDuration = System.currentTimeMillis() - call.getConnectTimeMillis();

        Log.d(TAG, "showCallDuration: " + callDuration);

        if (callDuration / 1000 != 0 && call.getConnectTimeMillis() != 0) {
            Toast.makeText(mContext, getFormateDuration((int) (callDuration / 1000)), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormateDuration(long duration) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        if (duration >= 3600) {
            hours = duration / 3600;
            minutes = (duration - hours * 3600) / 60;
            seconds = duration - hours * 3600 - minutes * 60;
        } else if (duration >= 60) {
            minutes = duration / 60;
            seconds = duration - minutes * 60;
        } else {
            seconds = duration;
        }

        String duration_title = mContext.getResources().getString(R.string.call_duration_title);
        String duration_content = mContext.getResources().getString(R.string.call_duration_format, hours, minutes, seconds);
        return  duration_title + " (" + duration_content + ")";
    }
    /// @}

    /// M: For Volte conference call calllog @{
    private LogConferenceCallAsyncTask mLogConferenceCallAsyncTask = null;

    private void logCallAsync(AddCallArgs args, Call call) {
        Log.d(TAG, "Logging Calllog Call: " + call);
        // If support volte, and the call is volte conference call child,
        // and the call is on the launcher side
        Call confCall = call.getParentCall();
        if (confCall != null && isVoLTECall(call) && !confCall.isIncoming()) {
            // It is Volte conference call child
            long confCallLogId = confCall.getConferenceCallLogId();
            if (confCallLogId > 0) {
                Log.d(TAG, "Logging Conference call. Conference Call Id: " + confCallLogId);
                // The conference call has saved into database
                args.conferenceCallLogId = confCallLogId;
                logCallAsync(args);
            } else if (mLogConferenceCallAsyncTask == null) {
                // The conference call has not saved
                // Launch an AsyncTask to save the conference call first
                mLogConferenceCallAsyncTask = new LogConferenceCallAsyncTask(
                        args.context, confCall);
                mLogConferenceCallAsyncTask.addCallArgs(args);
                mLogConferenceCallAsyncTask.execute();
            } else {
                mLogConferenceCallAsyncTask.addCallArgs(args);
            }
        } else {
            // It is not volte conference call
            logCallAsync(args);
        }
    }

    private boolean isVoLTECall(Call call) {
        return TelecomVolteUtils.isVolteSupport()
                && (call.getConnectionCapabilities() & Connection.CAPABILITY_VOLTE) != 0;
    }
    
    /**
     * Helper AsyncTask to access the call logs database asynchronously since database operations
     * can take a long time depending on the system's load. Since it extends AsyncTask, it uses
     * its own thread pool.
     */
    private class LogConferenceCallAsyncTask extends AsyncTask<Void, Void, Uri> {
        private final ArrayList<AddCallArgs> mAddCallArgsList = new ArrayList<AddCallArgs>();
        private final Call mConferenceCall;
        private final Context mContext;

        LogConferenceCallAsyncTask(Context context, Call conferenceCall) {
            mContext = context;
            mConferenceCall = conferenceCall;
        }

        void addCallArgs(AddCallArgs addCallArgs) {
            mAddCallArgsList.add(addCallArgs);
        }

        @Override
        protected Uri doInBackground(Void... args) {
            ContentValues values = new ContentValues();
            values.put(ConferenceCalls.CONFERENCE_DATE,
            		//aurora change
//                    mConferenceCall.getCreationTimeMillis());
            	  mConferenceCall.getConnectTimeMillis() > 0 ? mConferenceCall.getConnectTimeMillis(): mConferenceCall.getCreationTimeMillis() );
            Uri result = null;
            try {
                result = mContext.getContentResolver().insert(
                        ConferenceCalls.CONTENT_URI, values);
            } catch (Exception e) {
                Log.e(TAG, e, "Exception raised during adding CallLog entry.");
                result = null;
            }
            return result;
        }

        /**
         * Performs a simple sanity check to make sure the call was written in the database.
         * Typically there is only one result per call so it is easy to identify which one failed.
         * And save the child call into database with conference call id.
         */
        @Override
        protected void onPostExecute(Uri result) {
            if (result == null) {
                Log.w(TAG, "Failed to write call to the log.");
            }
            long confCallId = -1;
            try {
                confCallId = ContentUris.parseId(result);
                mConferenceCall.setConferenceCallLogId(confCallId);
            } catch (Exception ex) {
                Log.w(TAG, "Failed to write call to the log. Without id feedback.");
            }
            Log.d(TAG, "Logging Conference call. New Conference Call Id: "
                    + confCallId);
            for (AddCallArgs addCallArgs : mAddCallArgsList) {
                addCallArgs.conferenceCallLogId = confCallId;
                logCallAsync(addCallArgs);
            }
            mLogConferenceCallAsyncTask = null;
        }
    }
    /// @}
}
