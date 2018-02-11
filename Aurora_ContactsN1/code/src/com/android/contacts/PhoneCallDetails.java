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

package com.android.contacts;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * The details of a phone call to be shown in the UI.
 */
public class PhoneCallDetails {
    /** The number of the other party involved in the call. */
    public final CharSequence number;
    /** The formatted version of {@link #number}. */
    public final CharSequence formattedNumber;
    /** The country corresponding with the phone number. */
    public final String countryIso;
    /** The geocoded location for the phone number. */
    public final String geocode;
    /**
     * The type of calls, as defined in the call log table, e.g.,
     * {@link Calls#INCOMING_TYPE}.
     * <p>
     * There might be multiple types if this represents a set of entries grouped
     * together.
     */
    
    /**
    * Change Feature by Mediatek Begin.
    * Original Android's Code:
    * public final int[] callTypes;
    * Descriptions:
    */

    public final int callType;
    
    public final int callCount;
    /**
    * Change Feature by Mediatek End.
    */

    /** The date of the call, in milliseconds since the epoch. */
    public final long date;
    /** The duration of the call in milliseconds, or 0 for missed calls. */
    public final long duration;
    /** The name of the contact, or the empty string. */
    public final CharSequence name;
    /** The type of phone, e.g., {@link Phone#TYPE_HOME}, 0 if not available. */
    public final int numberType;
    /** The custom label associated with the phone number in the contact, or the empty string. */
    public final CharSequence numberLabel;
    /** The URI of the contact associated with this phone call. */
    public final Uri contactUri;
    /**
     * The photo URI of the picture of the contact that is associated with this phone call or
     * null if there is none.
     * <p>
     * This is meant to store the high-res photo only.
     */
    public final Uri photoUri;
    
    /**
    * Change Feature by Mediatek Begin.
        * Original Android's Code:
       /** Create the details for a call with a number not associated with a contact. 
        public PhoneCallDetails(CharSequence number, CharSequence formattedNumber,
                String countryIso, String geocode, int[] callTypes, long date, long duration) {
            this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, "", 0, "",
                    null, null);
        }
        */

        /** Create the details for a call with a number associated with a contact. 
        public PhoneCallDetails(CharSequence number, CharSequence formattedNumber,
                String countryIso, String geocode, int[] callTypes, long date, long duration,
                CharSequence name, int numberType, CharSequence numberLabel, Uri contactUri,
                Uri photoUri) {
            this.number = number;
            this.formattedNumber = formattedNumber;
            this.countryIso = countryIso;
            this.geocode = geocode;
            this.callTypes = callTypes;
            this.date = date;
            this.duration = duration;
            this.name = name;
            this.numberType = numberType;
            this.numberLabel = numberLabel;
            this.contactUri = contactUri;
            this.photoUri = photoUri;
        }
    }
    * Descriptions:Add simId,vtCall,call count in PhoneCallDetails.
    */
    public final int simId;

    
    // gionee xuhz 20121126 add for GIUI2.0 start
    /** The photo for the contact, if available. */
    public long photoId;
    // gionee xuhz 20121126 add for GIUI2.0 end
    
    public String numberArea;
    public String userMark; // add for reject by wangth
    public int markCount;
    
    public long pirvateId;
    
    /**
     * Create the details for a call with a number not associated with a
     * contact.
     */
    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso,
            String geocode, int callType, long date, long duration, int simId, int callCount) {
        this(number, formattedNumber, countryIso, geocode, callType, date, duration, "", 0, "",
                null, null, simId, callCount);
    }

    /** Create the details for a call with a number associated with a contact. */
    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso,
            String geocode, int callType, long date, long duration, CharSequence name,
            int numberType, CharSequence numberLabel, Uri contactUri, Uri photoUri, int simId,
            int callCount) {
        this.number = number;
        this.formattedNumber = formattedNumber;
        this.countryIso = countryIso;
        this.geocode = geocode;
        this.callType = callType;
        this.date = date;
        this.duration = duration;
        this.name = name;
        this.numberType = numberType;
        this.numberLabel = numberLabel;
        this.contactUri = contactUri;
        this.photoUri = photoUri;
        this.simId = simId;
        this.callCount = callCount;
    }
    /**
    * Change Feature by Mediatek End.
    */
    
    // gionee xuhz 20121126 add for GIUI2.0 start
    /** Create the details for a call with a number associated with a contact. */
    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso,
            String geocode, int callType, long date, long duration, CharSequence name,
            int numberType, CharSequence numberLabel, Uri contactUri, long photoId, Uri photoUri, int simId,
            int callCount) {
        this.number = number;
        this.formattedNumber = formattedNumber;
        this.countryIso = countryIso;
        this.geocode = geocode;
        this.callType = callType;
        this.date = date;
        this.duration = duration;
        this.name = name;
        this.numberType = numberType;
        this.numberLabel = numberLabel;
        this.contactUri = contactUri;
        this.photoId = photoId;
        this.photoUri = photoUri;
        this.simId = simId;
        this.callCount = callCount;
    }
    // gionee xuhz 20121126 add for GIUI2.0 end
    
    // add for reject by wangth
    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso,
            String geocode, int callType, long date, long duration, CharSequence name,
            int numberType, CharSequence numberLabel, Uri contactUri, long photoId, Uri photoUri, int simId,
            int callCount, String numberArea, String userMark, int markCount) {
        this.number = number;
        this.formattedNumber = formattedNumber;
        this.countryIso = countryIso;
        this.geocode = geocode;
        this.callType = callType;
        this.date = date;
        this.duration = duration;
        this.name = name;
        this.numberType = numberType;
        this.numberLabel = numberLabel;
        this.contactUri = contactUri;
        this.photoId = photoId;
        this.photoUri = photoUri;
        this.simId = simId;
        this.callCount = callCount;
        this.numberArea = numberArea;
        this.userMark = userMark;
        this.markCount = markCount;
    }
    
    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso,
            String geocode, int callType, long date, long duration, CharSequence name,
            int numberType, CharSequence numberLabel, Uri contactUri, long photoId, Uri photoUri, int simId,
            int callCount, String numberArea, String userMark, int markCount,long pirvateId) {
        this.number = number;
        this.formattedNumber = formattedNumber;
        this.countryIso = countryIso;
        this.geocode = geocode;
        this.callType = callType;
        this.date = date;
        this.duration = duration;
        this.name = name;
        this.numberType = numberType;
        this.numberLabel = numberLabel;
        this.contactUri = contactUri;
        this.photoId = photoId;
        this.photoUri = photoUri;
        this.simId = simId;
        this.callCount = callCount;
        this.numberArea = numberArea;
        this.userMark = userMark;
        this.markCount = markCount;
        this.pirvateId = pirvateId;
    }
    
    private List<PhoneCallRecord> mPhoneRecords;
    
    public void addPhoneRecords(PhoneCallRecord phoneRecord) {
    	if (null == mPhoneRecords) {
    		mPhoneRecords = new ArrayList<PhoneCallDetails.PhoneCallRecord>();
    	}
		mPhoneRecords.add(phoneRecord);
	}

	public List<PhoneCallRecord> getPhoneRecords() {
		return mPhoneRecords;
	}
	
	public boolean betweenCall(long time) {
		long gap = time - date;
		//Gionee:huangzy 20121017 modify for CR00710756, CR00710636 start
		/*return gap >= 0 && gap <= duration*1000;*/
		return gap >= 0 && gap <= duration*1000 + 999;
		//Gionee:huangzy 20121017 modify for CR00710756, CR00710636 end
	}
	
	public static class PhoneCallRecord {
		private String mPath;
		private long mDruation;
		private long mEndTime;
		private String mType;
		
		public void setPath(String path) {
			mPath = path;
		}
		public String getPath() {
			return mPath;
		}
		public void setDruation(long druation) {
			mDruation = druation;
		}
		public long getDruation() {
			return mDruation;
		}
		public void setEndTime(long endTime) {
			mEndTime = endTime;
		}
		public long getEndTime() {
			return mEndTime;
		}
		
		@Override
		public String toString() {
			return "Path = " + mPath + "\n Druation = " + mDruation + "   EndTime = " + mEndTime;
		}
		public void setMimeType(String type) {
			mType = type;
		}
		public String getMimeType() {
			return mType;
		}
	}

}