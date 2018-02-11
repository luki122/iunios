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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.content.Intent;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.database.Cursor;
import android.content.ContentResolver;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.os.Environment;
import java.io.File;

import com.android.contacts.R;
import com.android.contacts.vcard.ExportVCardActivity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.StatFs;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;

import aurora.app.AuroraActivity;


public class ShareContactViaSDCard extends AuroraActivity {
	
	private static final String TAG = "ShareContactViaSDCard";
	private String mAction;
	private Uri dataUri;
	private int singleContactId = -1;
	
	boolean sdIsVisible = true;
	
	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
		Contacts.DISPLAY_NAME_PRIMARY, // 1
		Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
		Contacts.SORT_KEY_PRIMARY, // 3
		Contacts.DISPLAY_NAME, // 4
    };
	
	static final int PHONE_ID_COLUMN_INDEX = 0;
	
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
        
        Log.i(TAG,"mAction is " + mAction);
        if (!checkSDCardAvaliable()) {
            AuroraAlertDialog alert = new AuroraAlertDialog.Builder(this).create();
            alert.setCanceledOnTouchOutside(true);
            alert.setMessage(this.getResources().getText(R.string.no_sdcard_message));
            alert.setTitle(R.string.no_sdcard_title);
            alert.setIcon(R.drawable.ic_dialog_alert_holo_light);
            alert.setButton(this.getResources().getText(android.R.string.ok), mCancelListener);
            alert.setOnDismissListener(new OnDismissListener() {
                
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            
            sdIsVisible = false;
        }
        
        if (checkSDCardAvaliable() && isSDCardFull()) {
            AuroraAlertDialog alert = new AuroraAlertDialog.Builder(this).create();
            alert.setCanceledOnTouchOutside(true);
            alert.setMessage(this.getResources().getText(R.string.storage_full));
            alert.setTitle(R.string.storage_full);
            alert.setIcon(R.drawable.ic_dialog_alert_holo_light);
            alert.setButton(this.getResources().getText(android.R.string.ok), mCancelListener);
            alert.setOnDismissListener(new OnDismissListener() {
                
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            
//            new AuroraAlertDialog.Builder(this)
//            .setMessage(R.string.storage_full)
//            .setTitle(R.string.storage_full)
//            .setIcon(android.R.drawable.ic_dialog_alert)
//            .setPositiveButton(android.R.string.ok, mCancelListener)
//            .show();
            sdIsVisible = false;
//            finish();
//            return;
        }
        
        if (sdIsVisible) {
            if (Intent.ACTION_SEND.equals(mAction) && intent.hasExtra(Intent.EXTRA_STREAM)) {
                String type = intent.getType();
                dataUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                Log.i(TAG,"dataUri is " + dataUri);
                Log.i(TAG,"type is " + type);
                String lookUpUris = intent.getStringExtra("LOOKUPURIS");
                if (dataUri != null && type != null) {
                    shareViaSDCard(lookUpUris);
                }
            }
        }
        
    }
    
    public void shareViaSDCard(String lookUpUris) {
    	if ((lookUpUris == null || "".equals(lookUpUris)) && singleContactId == -1) {
    		Toast.makeText(this.getApplicationContext(), getString(R.string.file_already_on_sd_card), Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
    	
		StringBuilder contactsID = new StringBuilder();
		int curIndex = 0;
		Cursor cursor = null;
		String id = null;
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
            selection.append(")");
            
			cursor = getContentResolver().query(/*dataUri*/Contacts.CONTENT_URI, CONTACTS_PROJECTION, selection.toString(), null, null);
			Log.i(TAG,"cursor is " + cursor);
			if (null != cursor) {
				while (cursor.moveToNext()) {				
					if (cursor != null) id = cursor.getString(PHONE_ID_COLUMN_INDEX);
//					Log.i(TAG,"id is " + id);
//					if (null == id) {
//						id = "";
//						Log.i(TAG, "OnClick contactId is null");
//						return;
//					}
//					if (TextUtils.isEmpty(id)) {
//						Log.i(TAG, "OnClick contactId is empty");
//						return;
//					}
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
		
		String exportselection = Contacts._ID + " IN (" + contactsID.toString() +")";
		
        Intent it = new Intent(this, ExportVCardActivity.class);
        it.putExtra("multi_export_type", 1);
        it.putExtra("exportselection", exportselection);
        this.startActivity(it);
        finish();
        return;
        }

	private boolean checkSDCardAvaliable() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	private boolean isSDCardFull() {
//        String state = Environment.getExternalStorageState(); 
//               if(Environment.MEDIA_MOUNTED.equals(state)) { 
//                   File sdcardDir = Environment.getExternalStorageDirectory(); 
//                   StatFs sf = new StatFs(sdcardDir.getPath());
//                   long availCount = sf.getAvailableBlocks(); 
//                   if(availCount>0){
//                       return false;
//                   } else {
//                       return true;
//                   }
//               } 
//
//		return true;
	    getExternalStorageDirectory();
        String state = getExternalStorageState(); 
        /*
         * Bug Fix by Mediatek End.
         */
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long availCount = sf.getAvailableBlocks();
            if (availCount > 0) {
                return false;
            } else {
                return true;
            }
        } 

        return true;
	}
	private static File mFile;
	
	public File getExternalStorageDirectory(){
//        StorageManager mSM = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
        String path = gionee.os.storage.GnStorageManager.getDefaultPath();
        // Gionee:wangth 20121113 modify for CR00729290 begin
        /*
        final File file = getDirectory(path, "/mnt/sdcard");
        */
        File file;
        
        if (ContactsUtils.mIsSuperSDCardVersionSupport) {
            String sdPath = ContactsUtils.getRealSdPath(1);
            file = getDirectory(path, sdPath);
        } else {
            file = getDirectory(path, "/mnt/sdcard");
        }
        // Gionee:wangth 20121113 modify for CR00729290 end
        Log.i(TAG,"[getExternalStorageDirectory]file.path : "+file.getPath());
        mFile = file;
        return file;
    }
	
	public  File getDirectory(String path, String defaultPath) {
        Log.i("getDirectory","path : "+path);
        return path == null ? new File(defaultPath) : new File(path);
    }
	
    public static String getExternalStorageState() {
        try {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                        .getService("mount"));
            Log.i(TAG, "[getExternalStorageState] mFile : " + mFile);
            return mountService.getVolumeState(mFile
                        .toString());
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }
    }
	
    private class CancelListener
            implements DialogInterface,DialogInterface.OnClickListener, DialogInterface.OnCancelListener,DialogInterface.OnKeyListener {
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }

        public void onCancel(DialogInterface dialog) {
            finish();
        }

        public void cancel() {
        }

        public void dismiss() {
            finish();
        }

        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            finish();
            return false;
        }
    }

	private CancelListener mCancelListener = new CancelListener();

}
