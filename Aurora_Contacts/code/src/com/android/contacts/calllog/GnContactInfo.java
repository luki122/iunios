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

import com.android.contacts.util.UriUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract;
import gionee.provider.GnContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.mediatek.contacts.HyphonManager;
import android.util.Log;

/**
 * Information for a contact as needed by the Call Log.
 */
public final class GnContactInfo {

	private static final String TAG = "GnContactInfo";
    public Uri lookupUri;
    public String name;
    public int type;
    public String label;
    public String number;
    public String formattedNumber;
    public String stripedNumber;
    /** The photo for the contact, if available. */
    public long photoId;
    /** The high-res photo for the contact, if available. */
    public Uri photoUri;
    
    //Gionee:huangzy 20120823 add for CR00614805 start
    public boolean hideNumber;
    //Gionee:huangzy 20120823 add for CR00614805 end

    // The following lines are provided and maintained by Mediatek Inc.
    //-1 indicates phone contacts, >0 indicates sim id for sim contacts.
    public int simId;
    public long duration;
    public String countryIso;
    public int vtCall;
    public String geocode;
    public int contactSimId;
    public long date;
    public int nNumberTypeId;
    public int isRead;
    public int gnCallsCount;
    
    //Gionee:huangzy 20130314 add for CR00784577 start
    public Uri voicemailUri;
    public int callId;
    //Gionee:huangzy 20130314 add for CR00784577 end

    public String area;
    public int reject;
    public String mark;
    public int userMark;
    //aurora change zhouxiaobing 20130925 start
    public int ids[];
    //aurora change zhouxiaobing 20130925 end
    public long  private_id;
    public String idsStr;

    public boolean isInitDisplay = false;
    public int simIcon = -1;
    public String displayArea;
    public String displayDuration;
    public int callTypeRes;
    
    public static GnContactInfo fromCursor(Cursor c,Context context) {
        if (null == c) {
            new Exception("ContactInfo.fromCursor(c) - c is null").printStackTrace();
            return null;
        }
        GnContactInfo newContactInfo = new GnContactInfo();
        if (null != newContactInfo) {
            try {
                newContactInfo.private_id = c.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_PRIVATE_ID);
                newContactInfo.number = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER);
                newContactInfo.date = c.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_DATE);
                newContactInfo.duration = c.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_DURATION);
                newContactInfo.type = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_CALL_TYPE);
                newContactInfo.countryIso = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_COUNTRY_ISO);
                newContactInfo.simId = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_SIM_ID);
                newContactInfo.vtCall = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_VTCALL);
                newContactInfo.name = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_DISPLAY_NAME);//getContactNameByPhoneNumber(newContactInfo.number, context, newContactInfo.private_id);
                newContactInfo.nNumberTypeId = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_CALL_NUMBER_TYPE_ID);
                newContactInfo.label = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_CALL_NUMBER_TYPE);
                newContactInfo.photoId = c.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_PHOTO_ID);
                newContactInfo.formattedNumber = HyphonManager.getInstance().formatNumber(newContactInfo.number);
                newContactInfo.stripedNumber = PhoneNumberUtils.stripSeparators(newContactInfo.number);
                newContactInfo.geocode = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_GEOCODED_LOCATION);
                newContactInfo.contactSimId = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_INDICATE_PHONE_SIM);
                long contactId = c.getLong(CallLogQuery.CALLS_JOIN_DATA_VIEW_CONTACT_ID);
                String lookUp = c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_LOOKUP_KEY);
                newContactInfo.lookupUri = (contactId == 0) ? null : Contacts.getLookupUri(contactId, lookUp);
                newContactInfo.isRead = c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_IS_READ);
                newContactInfo.gnCallsCount = c.getInt(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT);
                newContactInfo.area= c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_AREA);
                newContactInfo.reject= c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_REJECT);
                newContactInfo.mark= c.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_MARK);
                newContactInfo.userMark= c.getInt(CallLogQuery.CALLS_JOIN_DATA_VIEW_USER_MARK);
                
                
                newContactInfo.idsStr=c.getString(CallLogQuery.GN_CALLS_JOIN_DATA_VIEW_CALLS_COUNT_IDS);
				if(newContactInfo.idsStr.contains(",")){
					String[] idss=newContactInfo.idsStr.split(",");
					newContactInfo.ids=new int[idss.length];
					for(int j=0;j<idss.length;j++){
						newContactInfo.ids[j]=Integer.parseInt(idss[j]);
					}
				}else{
					newContactInfo.ids=new int[1];
					newContactInfo.ids[0]=Integer.parseInt(newContactInfo.idsStr);
				}
                //Gionee:huangzy 20130314 add for CR00784577 start
                String voiceMailUriStr = c.getString(CallLogQuery.VOICEMAIL_URI);
                if (null != voiceMailUriStr) {
                	newContactInfo.voicemailUri = Uri.parse(voiceMailUriStr); 	
                }
                newContactInfo.callId = c.getInt(CallLogQuery.ID);
                //Gionee:huangzy 20130314 add for CR00784577 end
                
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        
        return newContactInfo;
    }
    // The previous lines are provided and maintained by Mediatek Inc.
    
    
	public static String getContactNameByPhoneNumber(String address,
			Context context, long privateId) {
		Cursor cursor = null;
		String name = null;
		try {
			String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
					ContactsContract.CommonDataKinds.Phone.NUMBER };
			cursor = context.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					projection,
					ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"
							+ address + "' and is_privacy = " + privateId,
					null, null);
			if (cursor == null) {
				Log.i(TAG, "getContactNameByPhoneNumber null");
				return null;
			}
			while (cursor.moveToNext()) {
				name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			}
			Log.i(TAG, "getContactNameByPhoneNumber name = " + name);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("qiaohu", e.toString());
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return name;
	}

    public static GnContactInfo EMPTY = new GnContactInfo();

    @Override
    public int hashCode() {
        // Uses only name and contactUri to determine hashcode.
        // This should be sufficient to have a reasonable distribution of hash codes.
        // Moreover, there should be no two people with the same lookupUri.
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lookupUri == null) ? 0 : lookupUri.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GnContactInfo other = (GnContactInfo) obj;
        if (date != other.date) return false;
        if (!UriUtils.areEqual(lookupUri, other.lookupUri)) return false;
        if (!TextUtils.equals(name, other.name)) return false;
        if (type != other.type) return false;
        if (!TextUtils.equals(label, other.label)) return false;
        if (!TextUtils.equals(number, other.number)) return false;
        if (!TextUtils.equals(formattedNumber, other.formattedNumber)) return false;
        if (!TextUtils.equals(stripedNumber, other.stripedNumber)) return false;
        if (photoId != other.photoId) return false;
        if (!UriUtils.areEqual(photoUri, other.photoUri)) return false;
        if (contactSimId != other.contactSimId) return false;
        if (isRead != other.isRead) return false;
        if (gnCallsCount != other.gnCallsCount) return false;
        return true;
    }
}
