/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.phone;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;


//import android.provider.CallLog.Calls;
import com.android.phone.AuroraCallLog.Calls;

import android.text.TextUtils;
import android.util.Log;

import com.android.phone.NumberAreaUtil;
import com.android.internal.telephony.Connection;

import android.os.Build;

/**
 * Class to access the call logs database asynchronously since
 * database ops can take a long time depending on the system's load.
 * It uses AsyncTask which has its own thread pool.
 *
 * <pre class="prettyprint">
 * Typical usage:
 * ==============
 *
 *  // From an activity...
 *  String mLastNumber = "";
 *
 *  CallLogAsync log = new CallLogAsync();
 *
 *  CallLogAsync.AddCallArgs addCallArgs = new CallLogAsync.AddCallArgs(
 *      this, ci, number, presentation, type, timestamp, duration);
 *
 *  log.addCall(addCallArgs);
 *
 *  CallLogAsync.GetLastOutgoingCallArgs lastCallArgs = new CallLogAsync.GetLastOutgoingCallArgs(
 *      this, new CallLogAsync.OnLastOutgoingCallComplete() {
 *               public void lastOutgoingCall(String number) { mLastNumber = number; }
 *            });
 *  log.getLastOutgoingCall(lastCallArgs);
 * </pre>
 *
 */

public class CallLogAsync {
    private static final String TAG = "CallLogAsync";

    /**
     * Parameter object to hold the args to add a call in the call log DB.
     */
    public static class AddCallArgs {
        /**
         * @param ci               CallerInfo.
         * @param number           To be logged.
         * @param presentation     Of the number.
         * @param callType         The type of call (e.g INCOMING_TYPE). @see
         *                         android.provider.CallLog for the list of values.
         * @param timestamp        Of the call (millisecond since epoch).
         * @param durationInMillis Of the call (millisecond).
         */
        public AddCallArgs(Context context,
                           AuroraCallerInfo ci,
                           String number,
                           int presentation,
                           int callType,
                           long timestamp,
                           long durationInMillis,
                           long simId) {
            // Note that the context is passed each time. We could
            // have stored it in a member but we've run into a bunch
            // of memory leaks in the past that resulted from storing
            // references to contexts in places that were long lived
            // when the contexts were expected to be short lived. For
            // example, if you initialize this class with an Activity
            // instead of an Application the Activity can't be GCed
            // until this class can, and Activities tend to hold
            // references to large amounts of RAM for things like the
            // bitmaps in their views.
            //
            // Having hit more than a few of those bugs in the past
            // we've grown cautious of storing references to Contexts
            // when it's not very clear that the thing holding the
            // references is tightly tied to the Context, for example
            // Views the Activity is displaying.

            this.context = context;
            this.ci = ci;
            this.number = number;
            this.presentation = presentation;
            this.callType = callType;
            this.timestamp = timestamp;
            this.durationInSec = (int)(durationInMillis / 1000);
			this.mSimId = simId;
        }
        // Since the members are accessed directly, we don't use the
        // mXxxx notation.
        public final Context context;
        public final AuroraCallerInfo ci;
        public final String number;
        public final int presentation;
        public final int callType;
        public final long timestamp;
        public final int durationInSec;
        public final long mSimId;
    }

    /**
     * Parameter object to hold the args to get the last outgoing call
     * from the call log DB.
     */
    public static class GetLastOutgoingCallArgs {
        public GetLastOutgoingCallArgs(Context context,
                                       OnLastOutgoingCallComplete callback) {
            this.context = context;
            this.callback = callback;
        }
        public final Context context;
        public final OnLastOutgoingCallComplete callback;
    }

    /**
     * Non blocking version of CallLog.addCall(...)
     */
    public AsyncTask addCall(AddCallArgs args) {
        assertUiThread();
        return new AddCallTask().execute(args);
    }

    /** Interface to retrieve the last dialed number asynchronously. */
    public interface OnLastOutgoingCallComplete {
        /** @param number The last dialed number or an empty string if
         *                none exists yet. */
        void lastOutgoingCall(String number);
    }

    /**
     * CallLog.getLastOutgoingCall(...)
     */
    public AsyncTask getLastOutgoingCall(GetLastOutgoingCallArgs args) {
        assertUiThread();
        return new GetLastOutgoingCallTask(args.callback).execute(args);
    }

    /**
     * AsyncTask to save calls in the DB.
     */
    private class AddCallTask extends AsyncTask<AddCallArgs, Void, Uri[]> {
        @Override
        protected Uri[] doInBackground(AddCallArgs... callList) {
            int count = callList.length;
            Uri[] result = new Uri[count];
            for (int i = 0; i < count; i++) {
                AddCallArgs c = callList[i];

                try {
					Log.e(TAG, "call log add start   ");
					// May block.
					// result[i] = Calls.addCall(
					// c.ci, c.context, c.number, c.presentation,
					// c.callType, c.timestamp, c.durationInSec);
					if (c.ci != null && !TextUtils.isEmpty(c.ci.mRealName)) {
						c.ci.name = c.ci.mRealName;
					}
					
					if(c.ci.person_id == 0) {
						c.ci.name = null;
					}

					ContentValues values = new ContentValues();

					//handleMark
					boolean isMark = false;									
//					if ((c.ci == null || TextUtils.isEmpty(c.ci.name))
					if ((c.ci == null || c.ci.person_id == 0)					
							&& (c.callType == Calls.INCOMING_TYPE || c.callType == Calls.MISSED_TYPE)) {
						String Mark, MarkNumber;	
						if (c.ci != null && c.ci.isCompelete) {
							Mark = c.ci.mMark;
							MarkNumber = c.ci.mMarkNumber;
						} else {
							AuroraMarkUtils.resetMarks();
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
					Log.e(TAG, "call log add handleMark end   ");

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

					Log.e(TAG, "call log add handlePrivacy end   ");
					
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
//					if (c.ci != null && c.ci.isCompelete) {
//						area = c.ci.mArea;
//					} else {
						area = AuroraAreaUtils.getNumLocation(c.number);
//					}
					values.put("area", area);
					
					if (c.ci != null && c.ci.isUseYoloreImage && c.ci.cachedPhoto != null) {
						values.put("photo",  getCompressedBytes(((BitmapDrawable)c.ci.cachedPhoto).getBitmap()));
					}
					
					Log.e(TAG, "call log add handleReject end   ");

					result[i] = Calls.addCall(c.ci, c.context, c.number,
							c.presentation, c.callType, c.timestamp,
							c.durationInSec, (int) c.mSimId, -1, rawContactId,
							dataId, privateId);
					Log.e(TAG, "call log add:   " + result[i]);
					
					
					boolean isAndoid5 = Build.VERSION.SDK_INT >= 21;
					if(isAndoid5){
//						 values.put("subscription_component_name", accountComponentString);
				         values.put("subscription_id", c.mSimId + "");
					}

					if (isReject || isMark || isPrivate || isAndoid5) {
						c.context.getContentResolver().update(result[i],
								values, "privacy_id > -1", null);
					}    
                } catch (Exception e) {
                    // This must be very rare but may happen in legitimate cases.
                    // e.g. If the phone is encrypted and thus write request fails, it may
                    // cause some kind of Exception (right now it is IllegalArgumentException, but
                    // might change).
                    //
                    // We don't want to crash the whole process just because of that.
                    // Let's just ignore it and leave logs instead.
                    Log.e(TAG, "Exception raised during adding CallLog entry: " + e);
                    result[i] = null;
                }
            }
            return result;
        }

        // Perform a simple sanity check to make sure the call was
        // written in the database. Typically there is only one result
        // per call so it is easy to identify which one failed.
        @Override
        protected void onPostExecute(Uri[] result) {
            for (Uri uri : result) {
                if (uri == null) {
                    Log.e(TAG, "Failed to write call to the log.");
                }
            }
        }
    }

    /**
     * AsyncTask to get the last outgoing call from the DB.
     */
    private class GetLastOutgoingCallTask extends AsyncTask<GetLastOutgoingCallArgs, Void, String> {
        private final OnLastOutgoingCallComplete mCallback;
        private String mNumber;
        public GetLastOutgoingCallTask(OnLastOutgoingCallComplete callback) {
            mCallback = callback;
        }

        // Happens on a background thread. We cannot run the callback
        // here because only the UI thread can modify the view
        // hierarchy (e.g enable/disable the dial button). The
        // callback is ran rom the post execute method.
        @Override
        protected String doInBackground(GetLastOutgoingCallArgs... list) {
            int count = list.length;
            String number = "";
            for (GetLastOutgoingCallArgs args : list) {
                // May block. Select only the last one.
                number = Calls.getLastOutgoingCall(args.context);
            }
            return number;  // passed to the onPostExecute method.
        }

        // Happens on the UI thread, it is safe to run the callback
        // that may do some work on the views.
        @Override
        protected void onPostExecute(String number) {
            assertUiThread();
            mCallback.lastOutgoingCall(number);
        }
    }

    private void assertUiThread() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new RuntimeException("Not on the UI thread!");
        }
    }
    
    private byte[] getCompressedBytes(Bitmap b) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean compressed = b.compress(Bitmap.CompressFormat.JPEG, 95, baos);
        if (!compressed) {
            throw new IOException("Unable to compress image");
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }
    
}
