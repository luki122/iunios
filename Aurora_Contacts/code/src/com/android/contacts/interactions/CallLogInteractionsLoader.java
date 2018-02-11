/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.contacts.interactions;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.PhoneCallDetails;
import com.android.internal.annotations.VisibleForTesting;
import com.privacymanage.service.AuroraPrivacyUtils;

import gionee.os.storage.GnStorageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.R;
import com.android.contacts.util.AuroraDatabaseUtils;

public class CallLogInteractionsLoader extends AsyncTaskLoader<List<ContactInteraction>> {

	   private static final String TAG = "CallLogInteractionsLoader";
    private final String[] mPhoneNumbers;
    private final int mMaxToRetrieve;
    private List<ContactInteraction> mData;
    private boolean mIsPrivate;

    public CallLogInteractionsLoader(Context context, String[] phoneNumbers,
            int maxToRetrieve) {
        super(context);
        mPhoneNumbers = phoneNumbers;
        mMaxToRetrieve = maxToRetrieve;
    }
    
    public CallLogInteractionsLoader(Context context, String[] phoneNumbers,
            int maxToRetrieve, boolean isPrivate) {
        this(context, phoneNumbers, maxToRetrieve);
        mIsPrivate = isPrivate;
    }

    @Override
    public List<ContactInteraction> loadInBackground() {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
                || mPhoneNumbers == null || mPhoneNumbers.length <= 0 || mMaxToRetrieve <= 0) {
            return Collections.emptyList();
        }
        
        foundAllPhoneRecords();

        final List<ContactInteraction> interactions = new ArrayList<>();
        for (String number : mPhoneNumbers) {
            interactions.addAll(getCallLogInteractions(number));
        }
        // Sort the call log interactions by date for duplicate removal
        Collections.sort(interactions, new Comparator<ContactInteraction>() {
            @Override
            public int compare(ContactInteraction i1, ContactInteraction i2) {
                if (i2.getInteractionDate() - i1.getInteractionDate() > 0) {
                    return 1;
                } else if (i2.getInteractionDate() == i1.getInteractionDate()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        // Duplicates only occur because of fuzzy matching. No need to dedupe a single number.
        if (mPhoneNumbers.length == 1) {
            return interactions;
        }
        return pruneDuplicateCallLogInteractions(interactions, mMaxToRetrieve);
    }

    /**
     * Two different phone numbers can match the same call log entry (since phone number
     * matching is inexact). Therefore, we need to remove duplicates. In a reasonable call log,
     * every entry should have a distinct date. Therefore, we can assume duplicate entries are
     * adjacent entries.
     * @param interactions The interaction list potentially containing duplicates
     * @return The list with duplicates removed
     */
    @VisibleForTesting
    static List<ContactInteraction> pruneDuplicateCallLogInteractions(
            List<ContactInteraction> interactions, int maxToRetrieve) {
        final List<ContactInteraction> subsetInteractions = new ArrayList<>();
        for (int i = 0; i < interactions.size(); i++) {
            if (i >= 1 && interactions.get(i).getInteractionDate() ==
                    interactions.get(i-1).getInteractionDate()) {
                continue;
            }
            subsetInteractions.add(interactions.get(i));
            if (subsetInteractions.size() >= maxToRetrieve) {
                break;
            }
        }
        return subsetInteractions;
    }

    private List<ContactInteraction> getCallLogInteractions(String phoneNumber) {
        final String normalizedNumber = PhoneNumberUtils.normalizeNumber(phoneNumber);
        // If the number contains only symbols, we can skip it
        if (TextUtils.isEmpty(normalizedNumber)) {
            return Collections.emptyList();
        }
        final Uri uri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI,
                Uri.encode(normalizedNumber));
        // Append the LIMIT clause onto the ORDER BY clause. This won't cause crashes as long
        // as we don't also set the {@link android.provider.CallLog.Calls.LIMIT_PARAM_KEY} that
        // becomes available in KK.
        final String orderByAndLimit = Calls.DATE + " DESC LIMIT " + mMaxToRetrieve;
        String selection = mIsPrivate ? "privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId()  : null;
        final Cursor cursor = getContext().getContentResolver().query(uri, null, selection, null,
                orderByAndLimit);
        try {
            if (cursor == null || cursor.getCount() < 1) {
                return Collections.emptyList();
            }
            cursor.moveToPosition(-1);
            List<ContactInteraction> interactions = new ArrayList<>();
            while (cursor.moveToNext()) {
                final ContentValues values = new ContentValues();
                AuroraDatabaseUtils.cursorRowToContentValues(cursor, values);
                interactions.add(new CallLogInteraction(values, mAllRecords));
            }
            return interactions;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
      

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void deliverResult(List<ContactInteraction> data) {
        mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
        if (mData != null) {
            mData.clear();
        }
    }
    
   private  ArrayList<PhoneCallRecord> mAllRecords = new ArrayList<PhoneCallRecord>();
    
   private void foundAllPhoneRecords() {    	
    	String path = GnStorageManager.getInstance(ContactsApplication.getInstance()).getInternalStoragePath();
    	if (path == null) {
    		return;
    	}
    	
    	String historyPath = path + "/" + ContactsApplication.getInstance().getString(R.string.aurora_call_record_history_path);    	
    	int found = 0;    	
    	parseRecording(mAllRecords, historyPath, false);
    	
    	if (ContactsApplication.sIsAuroraPrivacySupport && AuroraPrivacyUtils.mCurrentAccountId > 0) {
    		historyPath = AuroraPrivacyUtils.mCurrentAccountHomePath
                    + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
    		historyPath = ContactsUtils.replaceBlank(historyPath);
    		parseRecording(mAllRecords, historyPath, true);
    	}
    	
    }
   
   private void parseRecording(ArrayList<PhoneCallRecord> records,
			String path, boolean isPrivacyPath) {
		try {
			synchronized (this) {
				File file = new File(path);
				if (file.isDirectory()) {
					String[] filesArr = file.list();
					File[] files = file.listFiles();
					String origName = null;

					if (filesArr != null) {
						int fileLen = filesArr.length;

						if (fileLen > 0) {
							for (int i = 0; i < fileLen; i++) {
								String name = filesArr[i];
								origName = name;
								String startTime = "";
								String duration = "";
								Log.d(TAG, "name = " + name);
								String postfix = ".3gpp";
								if (!TextUtils.isEmpty(name)
										&& name.endsWith(".amr")) {
									postfix = ".amr";
								}

								if (isPrivacyPath && !name.contains(postfix)) {
									boolean change = ContactsUtils
											.auroraChangeFile(files[i]
													.getPath());
									Log.i(TAG,
											"files[i].getPath():"
													+ files[i].getPath()
													+ "  change:" + change);
									if (!change) {
										continue;
									} else {
										name = new String(Base64.decode(name,
												Base64.URL_SAFE), "UTF-8");
										try {
											boolean rename = files[i]
													.renameTo(new File(path,
															name));
											Log.i(TAG, "rename:" + rename
													+ "  path:" + path
													+ "  name:" + name);
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									}
								}

								if (name != null) {
									if (name.length() > 20) {
										startTime = name.substring(0, 13);
										if (!TextUtils.isEmpty(startTime)) {
											long endTime = 0;
											long durationTime = 0;
											try {
												int durEnd = (name.substring(
														15, name.length()))
														.indexOf("_");
												durEnd += 15;
												duration = name.substring(14,
														durEnd);
												if (!TextUtils
														.isEmpty(duration)) {
													durationTime = Long
															.valueOf(duration);
													endTime = Long
															.valueOf(startTime)
															+ durationTime;
													PhoneCallRecord record = new PhoneCallRecord();
													record.setPath(path + "/"
															+ name);
													record.setEndTime(endTime);
													record.setDruation(durationTime);
													record.setMimeType("audio/amr");
													records.add(record);
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								}
								Log.d(TAG, "name = " + name + "  startTime = "
										+ startTime + " duration = " + duration
										+ "  records.size = " + records.size());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}