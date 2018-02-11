/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.contacts.calllog;

import com.android.contacts.AuroraCallDetailActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.util.Constants;
import com.android.contacts.util.IntentFactory;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.widget.CursorAdapter;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

/**
 * Used to create an intent to attach to an action in the call log.
 * <p>
 * The intent is constructed lazily with the given information.
 */
public abstract class IntentProvider {
    public abstract Intent getIntent(Context context);

    public static IntentProvider getReturnCallIntentProvider(final String number, final long simId) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                // Here, "number" can either be a PSTN phone number or a
                // SIP address.  So turn it into either a tel: URI or a
                // sip: URI, as appropriate.
                Uri uri;
                /**
                * Change Feature by Mediatek Begin.
                * Original Android's Code:
                  if (PhoneNumberUtils.isUriNumber(number)) {
                    uri = Uri.fromParts("sip", number, null);
                   } else {
                    uri = Uri.fromParts("tel", number, null);
                   }
                * Descriptions:
                */
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					uri = Uri.fromParts("tel", number, null);
				} else {
					if (PhoneNumberUtils.isUriNumber(number)) {
						uri = Uri.fromParts("sip", number, null);
					} else {
						uri = Uri.fromParts("tel", number, null);
					}
				}
                /**
                * Change Feature by Mediatek End.
                */
				Intent intent;
		
					intent = IntentFactory.newDialNumberIntent(uri);
			
				
				intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, simId)
				.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
				
				return intent;
            }
        };
    }

    public static IntentProvider getPlayVoicemailIntentProvider(final long rowId,
            final String voicemailUri, final long simId) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                Intent intent = new Intent(context, AuroraCallDetailActivity.class);
                intent.setData(ContentUris.withAppendedId(
                        Calls.CONTENT_URI_WITH_VOICEMAIL, rowId));
                if (voicemailUri != null) {
                    intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_URI,
                            Uri.parse(voicemailUri));
                }
                intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_START_PLAYBACK, true);
                intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, simId);
                return intent;
            }
        };
    }

    public static IntentProvider getCallDetailIntentProvider(
            final CallLogAdapter adapter, final int position, final long id, final int groupSize) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                
               //The following lines are deleted by Mediatek Inc .
                // if (CallLogQuery.isSectionHeader(cursor)) {
                // Do nothing when a header is clicked.
                // return null;
                // }
                //The previous lines are deleted by Mediatek Inc.
                
                Intent intent = new Intent(context, AuroraCallDetailActivity.class);
                
                // Check if the first item is a voicemail.
                String voicemailUri = cursor.getString(CallLogQuery.VOICEMAIL_URI);
                if (voicemailUri != null) {
                    intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_URI,
                            Uri.parse(voicemailUri));
                }
                intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_START_PLAYBACK, false);
                
                if (groupSize > 1) {
                    // We want to restore the position in the cursor at the end.
                    long[] ids = new long[groupSize];
                    // Copy the ids of the rows in the group.
                    for (int index = 0; index < groupSize; ++index) {
                        ids[index] = cursor.getLong(CallLogQuery.ID);
                        cursor.moveToNext();
                    }
                    intent.putExtra(AuroraCallDetailActivity.EXTRA_CALL_LOG_IDS, ids);
                } else {
                    // If there is a single item, use the direct URI for it.
					intent.setData(ContentUris.withAppendedId(
							Calls.CONTENT_URI_WITH_VOICEMAIL, id));
                }
                return intent;
            }
        };
    }
    
    // *******************follow lines are Gionee Ink.********************
    
    public static IntentProvider gnGetCallDetailIntentProvider(final CursorAdapter adapter, 
    		final int position, final String number, final int callsCount) {
        return new IntentProvider() {
            @Override
            public Intent getIntent(Context context) {
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                
                Intent intent = new Intent(context, AuroraCallDetailActivity.class);
                
                // Check if the first item is a voicemail.
                String voicemailUri = cursor.getString(CallLogQuery.VOICEMAIL_URI);
                if (voicemailUri != null) {
                    intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_URI,
                            Uri.parse(voicemailUri));
                }
                intent.putExtra(AuroraCallDetailActivity.EXTRA_VOICEMAIL_START_PLAYBACK, false);
                
                // If there is a single item, use the direct URI for it.
                intent.putExtra(Calls.NUMBER, number);
                intent.putExtra(Calls._COUNT, callsCount);
                if (1 == callsCount) {
                	intent.putExtra(Calls._ID, cursor.getInt(CallLogQuery.ID));
                }
                return intent;
            }
        };
    }
}