/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.contacts.activities;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.model.ExchangeAccountType;
import com.android.contacts.model.GoogleAccountType;
import com.mediatek.contacts.model.LocalPhoneAccountType;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore.Images;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.DisplayPhoto;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Provides an external interface for other applications to attach images
 * to contacts. It will first present a contact picker and then run the
 * image that is handed to it through the cropper to make the image the proper
 * size and give the user a chance to use the face detector.
 */
public class AttachPhotoActivity extends ContactsActivity {
    private static final int REQUEST_PICK_CONTACT = 1;
    private static final int REQUEST_CROP_PHOTO = 2;

    private static final String RAW_CONTACT_URIS_KEY = "raw_contact_uris";

    private Long[] mRawContactIds;

    private ContentResolver mContentResolver;

    // Height/width (in pixels) to request for the photo - queried from the provider.
    private static int mPhotoDim;
    
    // The following lines are provided and maintained by Mediatek inc.
    private static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    // The following lines are provided and maintained by Mediatek inc.

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (icicle != null) {
            mRawContactIds = toClassArray(icicle.getLongArray(RAW_CONTACT_URIS_KEY));
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            /*
             * Change Feature by Mediatek Begin.
             *   Original Android¡¯s code:
             *     intent.setType(Contacts.CONTENT_ITEM_TYPE);
             *   CR ID: ALPS00111821
             *   Descriptions: ¡­
             */
            intent.setType(Contacts.CONTENT_ITEM_TYPE);
            intent.putExtra("contactPhoto", true);
            /*
             * Change Feature by Mediatek End.
             */
            
            startActivityForResult(intent, REQUEST_PICK_CONTACT);
        }

        mContentResolver = getContentResolver();

        // Load the photo dimension to request.
        Cursor c = mContentResolver.query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
                new String[]{DisplayPhoto.DISPLAY_MAX_DIM}, null, null, null);
        try {
            c.moveToFirst();
            mPhotoDim = c.getInt(0);
        } finally {
            c.close();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRawContactIds != null && mRawContactIds.length != 0) {
            outState.putLongArray(RAW_CONTACT_URIS_KEY, toPrimativeArray(mRawContactIds));
        }
    }

    private static long[] toPrimativeArray(Long[] in) {
        if (in == null) {
            return null;
        }
        long[] out = new long[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static Long[] toClassArray(long[] in) {
        if (in == null) {
            return null;
        }
        Long[] out = new Long[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        if (requestCode == REQUEST_PICK_CONTACT) {
            //Gionee:huangzy 20120614 add for CR00624875 start
            if (ContactsApplication.sIsGnZoomClipSupport) {
                gnPick(result);
                return;
            }
            //Gionee:huangzy 20120614 add for CR00624875 end
            
            // A contact was picked. Launch the cropper to get face detection, the right size, etc.
            // TODO: get these values from constants somewhere
            Intent myIntent = getIntent();
            Intent intent = new Intent("com.android.camera.action.CROP", myIntent.getData());
            if (myIntent.getStringExtra("mimeType") != null) {
                intent.setDataAndType(myIntent.getData(), myIntent.getStringExtra("mimeType"));
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", mPhotoDim);
            intent.putExtra("outputY", mPhotoDim);
            intent.putExtra("return-data", true);
            // The following lines are provided and maintained by Mediatek inc.
            intent.putExtra(KEY_SCALE_UP_IF_NEEDED, true);
            // The following lines are provided and maintained by Mediatek inc.
            startActivityForResult(intent, REQUEST_CROP_PHOTO);

            // while they're cropping, convert the contact into a raw_contact
            final long contactId = ContentUris.parseId(result.getData());
            final ArrayList<Long> rawContactIdsList = queryForAllRawContactIds(
                    mContentResolver, contactId);
            mRawContactIds = new Long[rawContactIdsList.size()];
            mRawContactIds = rawContactIdsList.toArray(mRawContactIds);

            if (mRawContactIds == null || rawContactIdsList.isEmpty()) {
                Toast.makeText(this, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CROP_PHOTO) {
            //Gionee:huangzy 20120614 add for CR00624875 start
            if (ContactsApplication.sIsGnZoomClipSupport) {
                gnCrop(result);
                finish();
                return;
            }
            //Gionee:huangzy 20120614 add for CR00624875 end
            
            final Bundle extras = result.getExtras();
            if (extras != null && mRawContactIds != null) {
                Bitmap photo = extras.getParcelable("data");
                if (photo != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);

                    final ContentValues imageValues = new ContentValues();
                    imageValues.put(Photo.PHOTO, stream.toByteArray());
                    imageValues.put(RawContacts.Data.IS_SUPER_PRIMARY, 1);

                    // attach the photo to every raw contact
                    for (Long rawContactId : mRawContactIds) {

                        // exchange and google only allow one image, so do an update rather than insert
                        boolean shouldUpdate = false;

                        final Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                                rawContactId);
                        final Uri rawContactDataUri = Uri.withAppendedPath(rawContactUri,
                                RawContacts.Data.CONTENT_DIRECTORY);
                        insertPhoto(imageValues, rawContactDataUri, true);
                    }
                }
            }
            finish();
        }
    }

    // TODO: move to background
    public static ArrayList<Long> queryForAllRawContactIds(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        ArrayList<Long> rawContactIds = new ArrayList<Long>();
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null) {
                while (rawContactIdCursor.moveToNext()) {
                    rawContactIds.add(rawContactIdCursor.getLong(0));
                }
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactIds;
    }

    /**
     * Inserts a photo on the raw contact.
     * @param values the photo values
     * @param assertAccount if true, will check to verify that no photos exist for Google,
     *     Exchange and unsynced phone account types. These account types only take one picture,
     *     so if one exists, the account will be updated with the new photo.
     */
    private void insertPhoto(ContentValues values, Uri rawContactDataUri,
            boolean assertAccount) {

        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();

        if (assertAccount) {
            // Make sure no pictures exist for Google, Exchange and unsynced phone accounts.
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.DATA_SET + " IS NULL AND ("
                            + RawContacts.ACCOUNT_TYPE + " IN (?,?) OR "
                            + RawContacts.ACCOUNT_TYPE + " IS NULL)",
                            new String[] {Photo.CONTENT_ITEM_TYPE, GoogleAccountType.ACCOUNT_TYPE,
                            ExchangeAccountType.ACCOUNT_TYPE})
                            .withExpectedCount(0).build());
             *   CR ID: ALPS00249819
             *   Descriptions: add phone account type in query 
             */
            operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.DATA_SET + " IS NULL AND ("
                            + RawContacts.ACCOUNT_TYPE + " IN (?,?,?) OR "
                            + RawContacts.ACCOUNT_TYPE + " IS NULL)",
                            new String[] {Photo.CONTENT_ITEM_TYPE, GoogleAccountType.ACCOUNT_TYPE,
                            ExchangeAccountType.ACCOUNT_TYPE, LocalPhoneAccountType.ACCOUNT_TYPE})
                            .withExpectedCount(0).build());
            /*
             * Bug Fix by Mediatek End.
             */
        }

        // insert the photo
        values.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        operations.add(ContentProviderOperation.newInsert(rawContactDataUri)
                .withValues(values).build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            // the account doesn't allow multiple photos, so update
            if (assertAccount) {
                updatePhoto(values, rawContactDataUri, false);
            } else {
                throw new IllegalStateException("Problem inserting photo into raw_contacts/data", e);
            }
        }
    }

    /**
     * Tries to update the photo on the raw_contact.  If no photo exists, and allowInsert == true,
     * then will try to {@link #updatePhoto(ContentValues, boolean)}
     */
    private void updatePhoto(ContentValues values, Uri rawContactDataUri,
            boolean allowInsert) {
        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();

        values.remove(Photo.MIMETYPE);

        // check that a photo exists
        operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {
                    Photo.CONTENT_ITEM_TYPE
                }).withExpectedCount(1).build());

        // update that photo
        operations.add(ContentProviderOperation.newUpdate(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {Photo.CONTENT_ITEM_TYPE})
                .withValues(values).build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            if (allowInsert) {
                // they deleted the photo between insert and update, so insert one
                insertPhoto(values, rawContactDataUri, false);
            } else {
                // Gionee <wangth><2013-06-14> remove for CR00792030 begin
                /*
                throw new IllegalStateException("Problem inserting photo raw_contacts/data", e);
                */
                // Gionee <wangth><2013-06-14> remove for CR00792030 end
            }
        }
    }
    
    //Gionee:huangzy 20120614 add for CR00624875 start
    private void gnPick(Intent result) {
        Intent myIntent = getIntent();
        Log.d("AttachPhotoActivity", "myIntent.getData() = " + myIntent.getData().toString());
        
        // aurora wangth 20140325 add begin
        Uri uri = myIntent.getData();
        if (myIntent != null && myIntent.getData() != null && myIntent.getData().getScheme() != null
                && myIntent.getData().getScheme().equals("file")) {
            String path = uri.getEncodedPath();
            Log.d("AttachPhotoActivity", "path = " + path);
            
            if (path == null) {
                return;
            }
            
            ContentResolver cr = this.getContentResolver();  
            StringBuffer buff = new StringBuffer();  
            buff.append("(")  
                    .append(Images.ImageColumns.DATA)  
                    .append("=")  
                    .append("'" + path + "'")  
                    .append(")");
            
            Cursor cur = cr.query(  
                    Images.Media.EXTERNAL_CONTENT_URI,  
                    new String[] { Images.ImageColumns._ID },  
                    buff.toString(), null, null);
            
            int index = 0;  
            for (cur.moveToFirst(); !cur.isAfterLast(); cur  
                    .moveToNext()) {  
                index = cur.getColumnIndex(Images.ImageColumns._ID);  
                // set _id value  
                index = cur.getInt(index);  
            }
            
            if (index == 0) {  
                //do nothing  
                return;
            } else {  
                Uri uri_temp = Uri  
                        .parse("content://media/external/images/media/"  
                                + index);  
                Log.d("AttachPhotoActivity", "uri_temp is " + uri_temp);  
                if (uri_temp != null) {  
                    uri = uri_temp;  
                }  
            }
            
            if (cur != null) {
                cur.close();
            }
        }
        
        Log.e("AttachPhotoActivity", "uri = " + uri);
        //  aurora wangth 20140325 add end
        
        Intent intent = new Intent("com.android.camera.action.GNCROP", uri);
        intent.putExtra("gn_crop", true);
        if (myIntent.getStringExtra("mimeType") != null) {
            intent.setDataAndType(myIntent.getData(), myIntent.getStringExtra("mimeType"));
        }        
        startActivityForResult(intent, REQUEST_CROP_PHOTO);

        // while they're cropping, convert the contact into a raw_contact
        final long contactId = ContentUris.parseId(result.getData());
        final ArrayList<Long> rawContactIdsList = queryForAllRawContactIds(
                mContentResolver, contactId);
        mRawContactIds = new Long[rawContactIdsList.size()];
        mRawContactIds = rawContactIdsList.toArray(mRawContactIds);

        if (mRawContactIds == null || rawContactIdsList.isEmpty()) {
            Toast.makeText(this, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
        }
    }
    
    private void gnCrop(Intent result) {
        if (result != null && mRawContactIds != null) {
            String photoPath = result.getStringExtra("gn_data");
            Bitmap photo = null;
            if (null != photoPath) {
                File photoFile = new File(photoPath);
                try {
                    FileInputStream ffs = new FileInputStream(photoFile);
                    photo = BitmapFactory.decodeStream(ffs);  
                    ffs.close();
                } catch (Exception e) {
                }
                
                if (null != photo) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    
                    final ContentValues imageValues = new ContentValues();
                    imageValues.put(Photo.PHOTO, stream.toByteArray());
                    imageValues.put(RawContacts.Data.IS_SUPER_PRIMARY, 1);

                    // attach the photo to every raw contact
                    for (Long rawContactId : mRawContactIds) {
                        // exchange and google only allow one image, so do an update rather than insert
                        boolean shouldUpdate = false;
                        final Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                                rawContactId);
                        final Uri rawContactDataUri = Uri.withAppendedPath(rawContactUri,
                                RawContacts.Data.CONTENT_DIRECTORY);
                        insertPhoto(imageValues, rawContactDataUri, true);
                    }
                }
                photoFile.delete();
            }
        }
    }
    //Gionee:huangzy 20120614 add for CR00624875 end
}
