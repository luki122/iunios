/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.android.contacts;

import com.android.contacts.util.Constants;

import android.app.Activity;
import android.os.Bundle;
import android.content.ContentUris;
import android.content.Intent;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import android.database.Cursor;
import android.content.ContentResolver;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import java.util.ArrayList;
import java.util.List;
//import com.android.mms.ui./*ComposeMessageActivity*/*;

import aurora.app.AuroraActivity;


public class ShareContactViaSMS extends AuroraActivity {
	
	private static final String TAG = "ShareContactViaSMS";
	private String mAction;
	private Uri dataUri;
	private int singleContactId = -1;
	
	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
		Contacts.DISPLAY_NAME_PRIMARY, // 1
		Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
		Contacts.SORT_KEY_PRIMARY, // 3
		Contacts.DISPLAY_NAME, // 4
    };
	
	static final int PHONE_ID_COLUMN_INDEX = 0;
//    final String[] sLookupProjection = new String[] {
//            Contacts.LOOKUP_KEY
//    };
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        mAction = intent.getAction();
        String contactId = intent.getStringExtra("contactId");
        String userProfile = intent.getStringExtra("userProfile");
        if (userProfile != null && "true".equals(userProfile)) {
        	Toast.makeText(this.getApplicationContext(), getString(R.string.user_profile_cannot_sd_card), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
        
        if (contactId != null && !"".equals(contactId)) {
            singleContactId = Integer.parseInt(contactId);
        }

//        dataUri = intent.getData();
        Log.i(TAG,"mAction is " + mAction);
//        Log.i(TAG,"dataUri is " + dataUri);
        if (Intent.ACTION_SEND.equals(mAction) && intent.hasExtra(Intent.EXTRA_STREAM)) {
        	String type = intent.getType();
        	dataUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
//        	dataUri.buildUpon().appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, "1").build();
        	String lookUpUris = intent.getStringExtra("LOOKUPURIS");
        	Log.i(TAG,"dataUri is " + dataUri);
        	Log.i(TAG,"type is " + type);
            if (dataUri != null && type != null) {
            	shareViaSMS(lookUpUris);
            }
        }
    }
    
    public void shareViaSMS(String lookUpUris) {
    	if ((lookUpUris == null || "".equals(lookUpUris)) && singleContactId == -1) {
        	Toast.makeText(this.getApplicationContext(), getString(R.string.send_file_sms_error), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
    	
		StringBuilder contactsID = new StringBuilder();
		int curIndex = 0;
		Cursor cursor = null;
		String id = null;
        String textVCard = "";
		if (singleContactId == -1) {
		    String[] tempUris = lookUpUris.split(":");
		    StringBuilder selection = new StringBuilder(Contacts.LOOKUP_KEY + " in (");
		    int index = 0;
		    for (int i = 0; i < tempUris.length; i++) {
		        selection.append("'" + tempUris[i] + "'");
		        if (index != tempUris.length-1) {
		            selection.append(",");
                }
		        index++;
            }
		    
		    selection.append(" AND is_privacy>-1"); // add by wangth 20141029
		    
		    selection.append(")");
			cursor = getContentResolver().query(/*dataUri*/Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection.toString(), null, Contacts.SORT_KEY_PRIMARY);
			Log.i(TAG,"cursor is " + cursor);
			if (null != cursor) {
				while (cursor.moveToNext()) {				
					if (cursor != null) id = cursor.getString(PHONE_ID_COLUMN_INDEX);
					Log.i(TAG,"id is " + id);
					if (curIndex++ != 0) {
						contactsID.append("," + id);
					} else {
						contactsID.append(id);
					}
				}
				cursor.close();
			}
		} else {			
			id = Integer.toString(singleContactId);
			contactsID.append(id);
		}

        long[] contactsIds = null;
        if (contactsID.toString() != null && !contactsID.toString().equals("")) {
            String[] vCardConIds = contactsID.toString().split(",");
            Log.e(TAG, "ComposeMessage.initActivityState(): vCardConIds.length" + vCardConIds.length);
            contactsIds = new long[vCardConIds.length];
            try {
                for (int i = 0; i < vCardConIds.length; i++) {
                    contactsIds[i] = Long.parseLong(vCardConIds[i]);
                }
            } catch (NumberFormatException e) {
                contactsIds = null;
            }
        }
        if (contactsIds != null && contactsIds.length > 0) {
            Log.i(TAG, "compose.addTextVCard(): contactsIds.length() = " + contactsIds.length);
//          String textVCard = TextUtils.isEmpty(mTextEditor.getText())? "": "\n";
		
          StringBuilder sb = new StringBuilder("");
          for (long contactId : contactsIds) {
              if (contactId == contactsIds[contactsIds.length-1]) {
                  sb.append(contactId);
              } else {
                  sb.append(contactId + ",");
              }
          }
          String selection = Data.CONTACT_ID + " in (" + sb.toString() + ")";
          
          selection += (" AND is_privacy>-1"); // add by wangth 20141029
		
          Log.i(TAG, "compose.addTextVCard(): selection = " + selection);
          Uri dataUri = Uri.parse("content://com.android.contacts/data");
          Log.i(TAG,"Before query to build contact name and number string ");
          Cursor c = getContentResolver().query(
              dataUri, // URI
              new String[]{Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1}, // projection
              selection, // selection
              null, // selection args
              Contacts.SORT_KEY_PRIMARY); // sortOrder
          Log.i(TAG,"After query to build contact name and number string ");
          if (c != null) {
        	  Log.i(TAG,"Before getVCardString ");
              textVCard = getVCardString(c, textVCard);
              Log.i(TAG,"After getVCardString ");
              c.close();
          }	
        }
//		Intent it = new Intent(Intent.ACTION_SENDTO);           
//		it.setData(Uri.fromParts(Constants.SCHEME_SMSTO,"" , null));			
//		Log.i(TAG,"contactsID.toString() = " + contactsID.toString());
//		it.putExtra("multi_export_contacts", contactsID.toString());
        Log.i(TAG,"textVCard is " + " \n" + textVCard);
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", "", null));
		i.putExtra("sms_body", textVCard);
		ShareContactViaSMS.this.startActivity(i);		
		finish();
    }
    
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.i(TAG,"In onBackPressed");
		finish();
	}
    
    // create the String of vCard via Contacts message
    private String getVCardString(Cursor cursor, String textVCard) {
        final int dataContactId     = 0;
        final int dataMimeType      = 1;
        final int dataString        = 2;
        long contactId = 0l;
        long contactCurrentId = 0l;
        int i = 1;
        String mimeType;
        TextVCardContact tvc = new TextVCardContact();
        int j = 0;
        while (cursor.moveToNext()) {
            contactId = cursor.getLong(dataContactId);
            mimeType = cursor.getString(dataMimeType);
            if (contactCurrentId == 0l) {
                contactCurrentId = contactId;
            }

            // put one contact information into textVCard string
            if (contactId != contactCurrentId) {
                contactCurrentId = contactId;
                textVCard += tvc.toString();
                tvc.reset();
            }

            // get cursor data
            if (CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.name = cursor.getString(dataString);
            }
            if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.numbers.add(cursor.getString(dataString));
            }
            if (CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.emails.add(cursor.getString(dataString));
            }
            if (CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                tvc.organizations.add(cursor.getString(dataString));
            }
            // put the last one contact information into textVCard string
            if (cursor.isLast()) {
                // aurora <wangth> <2013-11-11> modify for aurora begin
                //textVCard += tvc.toString() + "\n";
                textVCard += tvc.toString();
                // aurora <wangth> <2013-11-11> modify for aurora end
            }
            j++;
            if (j % 10 == 0) {
            	if (textVCard.length() > 2000) {
            		break;
            	}
            }
        }
//        Log.i(TAG, "compose.getVCardString():return string = " + textVCard);
        return textVCard;
    }
    

    private class TextVCardContact {
        protected String name = "";
        protected List<String> numbers = new ArrayList<String>();
        protected List<String> emails = new ArrayList<String>();
        protected List<String> organizations = new ArrayList<String>();

        protected void reset() {
            name = "";
            numbers.clear();
            emails.clear();
            organizations.clear();
        }
        @Override
        public String toString() {
            String textVCardString = "";
            int i = 1;
            if (name != null && !name.equals("")) {
                textVCardString += getString(R.string.nameLabelsGroup) + ": " + name + "\n";
            }
            if (!numbers.isEmpty()) {
                if (numbers.size() > 1) {
                    i = 1;
                    for (String number : numbers) {
                        // gionee zhouyj 2012-05-30 modify for CR00611506 start
                        textVCardString += getString(R.string.contact_tel) + i + ": " + number + "\n";
                        // gionee zhouyj 2012-05-30 modify for CR00611506 end
                        i++;
                    }
                } else {
                    // gionee zhouyj 2012-05-30 modify for CR00611506 start
                    textVCardString += getString(R.string.contact_tel) + ": " + numbers.get(0) + "\n";
                    // gionee zhouyj 2012-05-30 modify for CR00611506 end
                }
            }
            if (!emails.isEmpty()) {
                if (emails.size() > 1) {
                    i = 1;
                    for (String email : emails) {
                        textVCardString += getString(R.string.email_other) + i + ": " + email + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.email_other) + ": " + emails.get(0) + "\n";
                }
            }
            if (!organizations.isEmpty()) {
                if (organizations.size() > 1) {
                    i = 1;
                    for (String organization : organizations) {
                        textVCardString += getString(R.string.organizationLabelsGroup) + i + ": " + organization + "\n";
                        i++;
                    }
                } else {
                    textVCardString += getString(R.string.organizationLabelsGroup) + ": " + organizations.get(0) + "\n";
                }
            }
            return textVCardString;
        }
    }
}
