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

package com.android.contacts;

import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDeltaList;
import com.android.contacts.model.EntityModifier;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.AggregationExceptions;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.Profile;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.USIMGroupException;
import com.privacymanage.service.AuroraPrivacyUtils;
//Gionee:wangth 20120711 add for CR00637244 begin
import com.android.contacts.ContactsUtils;
//Gionee:wangth 20120711 add for CR00637244 end

/**
 * A service responsible for saving changes to the content provider.
 */
public class ContactSaveService extends IntentService {
    private static final String TAG = "ContactSaveService";

    /** Set to true in order to view logs on content provider operations */
    private static final boolean DEBUG = false;

    public static final String ACTION_NEW_RAW_CONTACT = "newRawContact";

    public static final String EXTRA_ACCOUNT_NAME = "accountName";
    public static final String EXTRA_ACCOUNT_TYPE = "accountType";
    public static final String EXTRA_DATA_SET = "dataSet";
    public static final String EXTRA_CONTENT_VALUES = "contentValues";
    public static final String EXTRA_CALLBACK_INTENT = "callbackIntent";

    public static final String ACTION_SAVE_CONTACT = "saveContact";
    public static final String EXTRA_CONTACT_STATE = "state";
    public static final String EXTRA_SAVE_MODE = "saveMode";
    public static final String EXTRA_SAVE_IS_PROFILE = "saveIsProfile";
    public static final String EXTRA_SAVE_SUCCEEDED = "saveSucceeded";

    //aurora <wangth> <2013-9-16> add for auroro ui begin
    public static final String ACTION_AURORA_CREATE_GROUP = "auroraCreateGroup";
    public static final String ACTION_AURORA_UPDATE_GROUP = "auroraUpdateGroup";
    public static final String ACTION_AURORA_CREATE_GROUP2 = "auroraCreateGroup2";//aurora add zhouxiaobing 20131216
    //aurora <wangth> <2013-9-16> add for auroro ui end
    
    public static final String ACTION_CREATE_GROUP = "createGroup";
    public static final String ACTION_RENAME_GROUP = "renameGroup";
    public static final String ACTION_DELETE_GROUP = "deleteGroup";
    public static final String ACTION_UPDATE_GROUP = "updateGroup";
    public static final String EXTRA_GROUP_ID = "groupId";
    public static final String EXTRA_GROUP_LABEL = "groupLabel";
    public static final String EXTRA_RAW_CONTACTS_TO_ADD = "rawContactsToAdd";
    public static final String EXTRA_RAW_CONTACTS_TO_REMOVE = "rawContactsToRemove";
    public static final String EXTRA_RAW_CONTACTS_TO_UPDATE = "rawContactsToUpdate";

    public static final String ACTION_SET_STARRED = "setStarred";
    public static final String ACTION_DELETE_CONTACT = "delete";
    public static final String EXTRA_CONTACT_URI = "contactUri";
    public static final String EXTRA_STARRED_FLAG = "starred";

    public static final String ACTION_SET_SUPER_PRIMARY = "setSuperPrimary";
    public static final String ACTION_CLEAR_PRIMARY = "clearPrimary";
    public static final String EXTRA_DATA_ID = "dataId";

    public static final String ACTION_JOIN_CONTACTS = "joinContacts";
    public static final String EXTRA_CONTACT_ID1 = "contactId1";
    public static final String EXTRA_CONTACT_ID2 = "contactId2";
    public static final String EXTRA_CONTACT_WRITABLE = "contactWritable";

    public static final String ACTION_SET_SEND_TO_VOICEMAIL = "sendToVoicemail";
    public static final String EXTRA_SEND_TO_VOICEMAIL_FLAG = "sendToVoicemailFlag";

    /*
     * New Feature by Mediatek Begin.            
     * using by block video call     
     */
    public static final String ACTION_SET_BLOCK_VIDEO_CALL = "blockVideoCall";
    public static final String EXTRA_BLOCK_VIDEO_CALL_FLAG = "blockVideoCallFlag";
    /*
     * New Feature  by Mediatek End.
    */
    
    public static final String ACTION_SET_RINGTONE = "setRingtone";
    public static final String EXTRA_CUSTOM_RINGTONE = "customRingtone";
    
    public static final String EXTRA_SELECTION = "selection";
    
    private boolean mIsPrivacyMode = false;

    private static final HashSet<String> ALLOWED_DATA_COLUMNS = Sets.newHashSet(
        Data.MIMETYPE,
        Data.IS_PRIMARY,
        Data.DATA1,
        Data.DATA2,
        Data.DATA3,
        Data.DATA4,
        Data.DATA5,
        Data.DATA6,
        Data.DATA7,
        Data.DATA8,
        Data.DATA9,
        Data.DATA10,
        Data.DATA11,
        Data.DATA12,
        Data.DATA13,
        Data.DATA14,
        Data.DATA15
    );

    private static final int PERSIST_TRIES = 3;

    public interface Listener {
        public void onServiceCompleted(Intent callbackIntent);
    }

    private static final CopyOnWriteArrayList<Listener> sListeners =
            new CopyOnWriteArrayList<Listener>();

    private Handler mMainHandler;

    public ContactSaveService() {
        super(TAG);
        setIntentRedelivery(true);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static void registerListener(Listener listener) {
        if (!(listener instanceof Activity)) {
            throw new ClassCastException("Only activities can be registered to"
                    + " receive callback from " + ContactSaveService.class.getName());
        }
        sListeners.add(0, listener);
    }

    public static void unregisterListener(Listener listener) {
        sListeners.remove(listener);
    }

    @Override
    public Object getSystemService(String name) {
        Object service = super.getSystemService(name);
        if (service != null) {
            return service;
        }

        return getApplicationContext().getSystemService(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	mIsPrivacyMode = intent.getBooleanExtra("is_privacy_contact", false);
    	
        String action = intent.getAction();
        if (ACTION_NEW_RAW_CONTACT.equals(action)) {
            createRawContact(intent);
        } else if (ACTION_SAVE_CONTACT.equals(action)) {
            saveContact(intent);
        } else if (ACTION_CREATE_GROUP.equals(action)) {
            createGroup(intent);
        } else if (ACTION_RENAME_GROUP.equals(action)) {
            renameGroup(intent);
        } else if (ACTION_DELETE_GROUP.equals(action)) {
            deleteGroup(intent);
        } else if (ACTION_UPDATE_GROUP.equals(action)) {
            updateGroup(intent);
        } else if (ACTION_SET_STARRED.equals(action)) {
            setStarred(intent);
        } else if (ACTION_SET_SUPER_PRIMARY.equals(action)) {
            setSuperPrimary(intent);
        } else if (ACTION_CLEAR_PRIMARY.equals(action)) {
            clearPrimary(intent);
        } else if (ACTION_DELETE_CONTACT.equals(action)) {
            deleteContact(intent);
        } else if (ACTION_JOIN_CONTACTS.equals(action)) {
            joinContacts(intent);
        } else if (ACTION_SET_SEND_TO_VOICEMAIL.equals(action)) {
            setSendToVoicemail(intent);
        } else if (ACTION_SET_RINGTONE.equals(action)) {
            setRingtone(intent);
        /*
         * New Feature by Mediatek Begin.            
         * using by block video call, defaults to false        
         */    
        } else if (ACTION_SET_BLOCK_VIDEO_CALL.equals(action)) {
            setBlockVideoCall(intent);
        }
        /*
         * New Feature  by Mediatek End.
        */
        
        //aurora <wangth> <2013-9-16> add for auroro ui begin
        else if (ACTION_AURORA_CREATE_GROUP.equals(action)) {
            auroraCreateGroup(intent);
        } else if (ACTION_AURORA_UPDATE_GROUP.equals(action)) {
            auroraUpdateGroup(intent);
        }
        else if (ACTION_AURORA_CREATE_GROUP2.equals(action)) {
            auroraCreateGroup2(intent);//aurora add zhouxiaobing 20131216
        }
        //aurora <wangth> <2013-9-16> add for auroro ui end
    }

    /**
     * Creates an intent that can be sent to this service to create a new raw contact
     * using data presented as a set of ContentValues.
     */
    public static Intent createNewRawContactIntent(Context context,
            ArrayList<ContentValues> values, AccountWithDataSet account,
            Class<?> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(
                context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_NEW_RAW_CONTACT);
        if (account != null) {
            serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_NAME, account.name);
            serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE, account.type);
            serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_SET, account.dataSet);
        }
        serviceIntent.putParcelableArrayListExtra(
                ContactSaveService.EXTRA_CONTENT_VALUES, values);

        // Callback intent will be invoked by the service once the new contact is
        // created.  The service will put the URI of the new contact as "data" on
        // the callback intent.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);
        return serviceIntent;
    }

    private void createRawContact(Intent intent) {
        String accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME);
        String accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE);
        String dataSet = intent.getStringExtra(EXTRA_DATA_SET);
        List<ContentValues> valueList = intent.getParcelableArrayListExtra(EXTRA_CONTENT_VALUES);
        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, accountName)
                .withValue(RawContacts.ACCOUNT_TYPE, accountType)
                .withValue(RawContacts.DATA_SET, dataSet)
                .build());

        int size = valueList.size();
        for (int i = 0; i < size; i++) {
            ContentValues values = valueList.get(i);
            values.keySet().retainAll(ALLOWED_DATA_COLUMNS);
            operations.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValues(values)
                    .build());
        }

        ContentResolver resolver = getContentResolver();
        ContentProviderResult[] results;
        try {
            results = resolver.applyBatch(GnContactsContract.AUTHORITY, operations);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store new contact", e);
        }

        Uri rawContactUri = results[0].uri;
        callbackIntent.setData(RawContacts.getContactLookupUri(resolver, rawContactUri));

        deliverCallback(callbackIntent);
    }

    /**
     * Creates an intent that can be sent to this service to create a new raw contact
     * using data presented as a set of ContentValues.
     */
    public static Intent createSaveContactIntent(Context context, EntityDeltaList state,
            String saveModeExtraKey, int saveMode, boolean isProfile, Class<?> callbackActivity,
            String callbackAction) {
        Intent serviceIntent = new Intent(
                context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SAVE_CONTACT);
        serviceIntent.putExtra(EXTRA_CONTACT_STATE, (Parcelable) state);
        serviceIntent.putExtra(EXTRA_SAVE_IS_PROFILE, isProfile);

        // Callback intent will be invoked by the service once the contact is
        // saved.  The service will put the URI of the new contact as "data" on
        // the callback intent.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.putExtra(saveModeExtraKey, saveMode);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);
        return serviceIntent;
    }

    private void saveContact(Intent intent) {
    	ContactsApplication.sendSimContactBroad();
        EntityDeltaList state = intent.getParcelableExtra(EXTRA_CONTACT_STATE);
        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        boolean isProfile = intent.getBooleanExtra(EXTRA_SAVE_IS_PROFILE, false);

        // Trim any empty fields, and RawContacts, before persisting
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(this);
        EntityModifier.trimEmpty(state, accountTypes);

        Uri lookupUri = null;

        final ContentResolver resolver = getContentResolver();

        // Attempt to persist changes
        int tries = 0;
        
        while (tries++ < PERSIST_TRIES) {
            try {
                // Build operations and try applying
                final ArrayList<ContentProviderOperation> diff = state.buildDiff();
                
                if (DEBUG) {
                    Log.v(TAG, "Content Provider Operations:");
                    for (ContentProviderOperation operation : diff) {
                        Log.v(TAG, operation.toString());
                    }
                }
                
//                //add by liyang begin
//                boolean hasPhoto=false;
//                for (ContentProviderOperation operation : diff) {
//                    Log.d("liyang", "operation:"+operation.toString());
//                    if(operation.toString().indexOf("mimetype=vnd.android.cursor.item/photo")>0){
//                    	hasPhoto=true;                    	
//                    }
//                }
//                Log.d(TAG,"hasPhoto:"+hasPhoto);
//                //add by liyang end

                ContentProviderResult[] results = null;
                if (!diff.isEmpty()) {
                    results = resolver.applyBatch(GnContactsContract.AUTHORITY, diff);
                }

                final long rawContactId = getRawContactId(state, diff, results);
                
                
                if (rawContactId == -1) {
                	
                    throw new IllegalStateException("Could not determine RawContact ID after save");
                }
                if (isProfile) {
                	
                	//Gionee <huangzy> <2013-04-26> add for CR00795330 begin
               		ContactsUtils.broadcastUserProfileUpdated();
               	    //Gionee <huangzy> <2013-04-26> add for CR00795330 end
                	
                    // Since the profile supports local raw contacts, which may have been completely
                    // removed if all information was removed, we need to do a special query to
                    // get the lookup URI for the profile contact (if it still exists).
                    Cursor c = resolver.query(Profile.CONTENT_URI,
                            new String[] {Contacts._ID, Contacts.LOOKUP_KEY},
                            null, null, null);
                    try {
                        if (c.moveToFirst()) {
                            final long contactId = c.getLong(0);
                            final String lookupKey = c.getString(1);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }
                    } finally {
                        c.close();
                    }
                } else {
                	Log.d(TAG,"isProfile false");
                    final Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                                    rawContactId);
                    lookupUri = RawContacts.getContactLookupUri(resolver, rawContactUri);
                    
                    if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyMode) {
                    	ContentValues values = new ContentValues();
                    	long privacyId = ContactsApplication.sIsAuroraPrivacySupport ? 
                        				AuroraPrivacyUtils.mCurrentAccountId
                        				: 0;
                    	values.put("is_privacy", privacyId);
                    	resolver.update(RawContacts.CONTENT_URI, values, 
                    			RawContacts._ID + "=" + rawContactId, null);
                    	resolver.update(Data.CONTENT_URI, values, 
                                Data.RAW_CONTACT_ID + "=" + rawContactId + " AND is_privacy>-10", null);
                    	
                    	AuroraPrivacyUtils.mPrivacyContactsNum++;
                    	AuroraPrivacyUtils.setPrivacyNum(getApplicationContext(),
    							"com.android.contacts.activities.AuroraPrivacyContactListActivity", 
    							AuroraPrivacyUtils.mPrivacyContactsNum, 
    							privacyId);
                    }
                    
//                    //add by liyang begin
//                    if(!hasPhoto){
//                    	
//                    	ContentValues values = new ContentValues();
//                    	int photo_id=-(new Random()).nextInt(14)-100;
//                    	values.put("photo_id", photo_id);
//                    	
//                    	values.put("flag", 1);
//                    	resolver.update(Contacts.CONTENT_URI, values, 
//                    			Contacts.NAME_RAW_CONTACT_ID + "=" + rawContactId, null);
//                    	Log.d(TAG,"update photoid,uri:"+Contacts.CONTENT_URI+" photo_id:"+photo_id+" rawcontactid:"+rawContactId);
//                    }
//                    //add by liyang end
                }
                Log.v(TAG, "Saved contact. New URI: " + lookupUri);
                // Mark the intent to indicate that the save was successful (even if the lookup URI
                // is now null).  For local contacts or the local profile, it's possible that the
                // save triggered removal of the contact, so no lookup URI would exist..
                callbackIntent.putExtra(EXTRA_SAVE_SUCCEEDED, true);
                break;

            } catch (RemoteException e) {
                // Something went wrong, bail without success
                Log.e(TAG, "Problem persisting user edits", e);
                break;

            } catch (OperationApplicationException e) {
                // Version consistency failed, re-parent change and try again
                Log.w(TAG, "Version consistency failed, re-parenting: " + e.toString());
                final StringBuilder sb = new StringBuilder(RawContacts._ID + " IN(");
                boolean first = true;
                final int count = state.size();
                for (int i = 0; i < count; i++) {
                    Long rawContactId = state.getRawContactId(i);
                    if (rawContactId != null && rawContactId != -1) {
                        if (!first) {
                            sb.append(',');
                        }
                        sb.append(rawContactId);
                        first = false;
                    }
                }
                sb.append(")");

                if (first) {
                    throw new IllegalStateException("Version consistency failed for a new contact");
                }

                final EntityDeltaList newState = EntityDeltaList.fromQuery(
                        isProfile
                                ? RawContactsEntity.PROFILE_CONTENT_URI
                                : RawContactsEntity.CONTENT_URI,
                        resolver, sb.toString(), null, null);
                state = EntityDeltaList.mergeAfter(newState, state);

                // Update the new state to use profile URIs if appropriate.
                if (isProfile) {
                    for (EntityDelta delta : state) {
                        delta.setProfileQueryUri();
                    }
                }
            }
            // Gionee:wangth 20121201 add for CR00737997 begin
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
            // Gionee:wangth 20121201 add for CR00737997 end
        }

        callbackIntent.setData(lookupUri);

        deliverCallback(callbackIntent);
    }

    private long getRawContactId(EntityDeltaList state,
            final ArrayList<ContentProviderOperation> diff,
            final ContentProviderResult[] results) {
        long rawContactId = state.findRawContactId();
        if (rawContactId != -1) {
            return rawContactId;
        }

        final int diffSize = diff.size();
        for (int i = 0; i < diffSize; i++) {
            ContentProviderOperation operation = diff.get(i);
            if (operation.getType() == ContentProviderOperation.TYPE_INSERT
                    && operation.getUri().getEncodedPath().contains(
                            RawContacts.CONTENT_URI.getEncodedPath())) {
                // Gionee:wangth 20120711 modify for CR00637244 begin
                /*
                return ContentUris.parseId(results[i].uri);
                */
                long result = -1;
                if (ContactsUtils.mIsGnContactsSupport) {
                    try {
                        result = ContentUris.parseId(results[i].uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    return result;
                } else {
                    return ContentUris.parseId(results[i].uri);
                }
                // Gionee:wangth 20120711 modify for CR00637244 end
            }
        }
        return -1;
    }

    /**
     * Creates an intent that can be sent to this service to create a new group as
     * well as add new members at the same time.
     *
     * @param context of the application
     * @param account in which the group should be created
     * @param label is the name of the group (cannot be null)
     * @param rawContactsToAdd is an array of raw contact IDs for contacts that
     *            should be added to the group
     * @param callbackActivity is the activity to send the callback intent to
     * @param callbackAction is the intent action for the callback intent
     */
    public static Intent createNewGroupIntent(Context context, AccountWithDataSet account,
            String label, long[] rawContactsToAdd, Class<?> callbackActivity,
            String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_CREATE_GROUP);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE, account.type);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_NAME, account.name);
        serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_SET, account.dataSet);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, label);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD, rawContactsToAdd);

        // Callback intent will be invoked by the service once the new group is
        // created.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }
    
    //aurora <wangth> <2013-9-16> add for auroro ui begin
    public static Intent auroraCreateNewGroupIntent(Context context, AccountWithDataSet account,
            String label, long[] rawContactsToAdd, Class<?> callbackActivity,
            String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_AURORA_CREATE_GROUP);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE, account.type);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_NAME, account.name);
        serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_SET, account.dataSet);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, label);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD, rawContactsToAdd);

        // Callback intent will be invoked by the service once the new group is
        // created.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }
    
    public static Intent auroraUpdateGroupIntent(Context context, long groupId, String newLabel,
            long[] rawContactsToAdd, long[] rawContactsToRemove, long[] rawContactsToUpdate,
            Class<?> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_AURORA_UPDATE_GROUP);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_ID, groupId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, newLabel);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD, rawContactsToAdd);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_REMOVE,
                rawContactsToRemove);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_UPDATE,
                rawContactsToUpdate);

        // Callback intent will be invoked by the service once the group is updated
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }
    //aurora <wangth> <2013-9-16> add for auroro ui end

    private void createGroup(Intent intent) {
        String accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE);
        String accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME);
        String dataSet = intent.getStringExtra(EXTRA_DATA_SET);
        String label = intent.getStringExtra(EXTRA_GROUP_LABEL);
        final long[] rawContactsToAdd = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_ADD);

		// the following lines are provided and maintained by Mediatek Inc.
		int[] simIndexArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_ARRAY);
		int slotId = intent.getIntExtra(ContactSaveService.EXTRA_SLOT_ID, -1);
		Log.i(TAG, slotId+"-------slotId-createGroup()");
		int ugrpId = -1;
		if (slotId >=0) {
            try {
                ugrpId = ContactsGroupUtils.USIMGroup
                        .syncUSIMGroupNewIfMissing(slotId, label);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (USIMGroupException e) {
                Log.d(TAG, "[SyncUSIMGroup] catched USIMGroupException." 
                        + " ErrorType: " + e.getErrorType()+"----");
                Log.d(TAG, "[SyncUSIMGroup] catched USIMGroupException." 
                        + " getErrorSlotId: " + e.getErrorSlotId()+"-------");
                mErrorType[e.getErrorSlotId()] = e.getErrorType();
                // USIM Group begin
                if (mErrorType[0] > 0) {
                    // slot 0 for Gemini or slot for single card.
                    Log.d(TAG, "[showToast] mErrorType[0]b: " + mErrorType[0]);
                    showMoveUSIMGroupErrorToast(mErrorType[0], 0);
                    Log.d(TAG, "[showToast] mErrorType[0]e: " + mErrorType[0]);

                }
                if (mErrorType[1] > 0) {
                    showMoveUSIMGroupErrorToast(mErrorType[1], 1);
                }
                Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
                Log.i(TAG, EXTRA_CALLBACK_INTENT);
                callbackIntent.setAction("dismissDailog");
                callbackIntent.putExtra("dismissDialog", true);
                deliverCallback(callbackIntent);
             // USIM Group end
                return;
            }
		}

		// the previous lines are provided and maintained by Mediatek Inc.
		
        ContentValues values = new ContentValues();
        values.put(Groups.ACCOUNT_TYPE, accountType);
        values.put(Groups.ACCOUNT_NAME, accountName);
        values.put(Groups.DATA_SET, dataSet);
        values.put(Groups.TITLE, label);
        values.put(Groups.GROUP_VISIBLE, "1");

        final ContentResolver resolver = getContentResolver();

        // Create the new group
        final Uri groupUri = resolver.insert(Groups.CONTENT_URI, values);

        // If there's no URI, then the insertion failed. Abort early because group members can't be
        // added if the group doesn't exist
        if (groupUri == null) {
            Log.e(TAG, "Couldn't create group with label " + label);
            return;
        }

        // Add new group members
        
		/*
		 * New feature by Mediatek Begin Original Android code:
		 * addMembersToGroup(resolver, rawContactsToAdd,
		 * ContentUris.parseId(groupUri));
		 */
		addMembersToGroup(resolver, rawContactsToAdd, ContentUris.parseId(groupUri), simIndexArray, slotId, ugrpId);
		/*
		 * New feature by Mediatek End
		 */

        // TODO: Move this into the contact editor where it belongs. This needs to be integrated
        // with the way other intent extras that are passed to the {@link ContactEditorActivity}.
        values.clear();
        values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
        values.put(GroupMembership.GROUP_ROW_ID, ContentUris.parseId(groupUri));

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        callbackIntent.setData(groupUri);
        // TODO: This can be taken out when the above TODO is addressed
        
        // The following lines are provided and maintained by Mediatek Inc.
        callbackIntent.putExtra("mSlotId", slotId);
        callbackIntent.putExtra("dismissDialog", true);
        // The previous  lines are provided and maintained by Mediatek Inc.

        callbackIntent.putExtra(GnContactsContract.Intents.Insert.DATA, Lists.newArrayList(values));
        deliverCallback(callbackIntent);
    }
 //aurora add zhouxiaobing 20131216 start
    private void auroraCreateGroup2(Intent intent) {
        String accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE);
        String accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME);
        String dataSet = intent.getStringExtra(EXTRA_DATA_SET);
        String label = intent.getStringExtra(EXTRA_GROUP_LABEL);
        final long[] rawContactsToAdd = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_ADD);

		// the following lines are provided and maintained by Mediatek Inc.
		int[] simIndexArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_ARRAY);
		int slotId = intent.getIntExtra(ContactSaveService.EXTRA_SLOT_ID, -1);
		Log.i(TAG, slotId+"-------slotId-createGroup()");
		int ugrpId = -1;
		if (slotId >=0) {
            try {
                ugrpId = ContactsGroupUtils.USIMGroup
                        .syncUSIMGroupNewIfMissing(slotId, label);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (USIMGroupException e) {
                Log.d(TAG, "[SyncUSIMGroup] catched USIMGroupException." 
                        + " ErrorType: " + e.getErrorType()+"----");
                Log.d(TAG, "[SyncUSIMGroup] catched USIMGroupException." 
                        + " getErrorSlotId: " + e.getErrorSlotId()+"-------");
                mErrorType[e.getErrorSlotId()] = e.getErrorType();
                // USIM Group begin
                if (mErrorType[0] > 0) {
                    // slot 0 for Gemini or slot for single card.
                    Log.d(TAG, "[showToast] mErrorType[0]b: " + mErrorType[0]);
                    showMoveUSIMGroupErrorToast(mErrorType[0], 0);
                    Log.d(TAG, "[showToast] mErrorType[0]e: " + mErrorType[0]);

                }
                if (mErrorType[1] > 0) {
                    showMoveUSIMGroupErrorToast(mErrorType[1], 1);
                }
                Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
                Log.i(TAG, EXTRA_CALLBACK_INTENT);
                callbackIntent.setAction("dismissDailog");
                callbackIntent.putExtra("dismissDialog", true);
                deliverCallback(callbackIntent);
             // USIM Group end
                return;
            }
		}

		// the previous lines are provided and maintained by Mediatek Inc.
		
        ContentValues values = new ContentValues();
        values.put(Groups.ACCOUNT_TYPE, accountType);
        values.put(Groups.ACCOUNT_NAME, accountName);
        values.put(Groups.DATA_SET, dataSet);
        values.put(Groups.TITLE, label);
        values.put(Groups.GROUP_VISIBLE, "1");

        final ContentResolver resolver = getContentResolver();

        // Create the new group
        final Uri groupUri = resolver.insert(Groups.CONTENT_URI, values);

        // If there's no URI, then the insertion failed. Abort early because group members can't be
        // added if the group doesn't exist
        if (groupUri == null) {
            Log.e(TAG, "Couldn't create group with label " + label);
            return;
        }

        // Add new group members
        
		/*
		 * New feature by Mediatek Begin Original Android code:
		 * addMembersToGroup(resolver, rawContactsToAdd,
		 * ContentUris.parseId(groupUri));
		 */
		addMembersToGroup(resolver, rawContactsToAdd, ContentUris.parseId(groupUri), simIndexArray, slotId, ugrpId);
		/*
		 * New feature by Mediatek End
		 */

        // TODO: Move this into the contact editor where it belongs. This needs to be integrated
        // with the way other intent extras that are passed to the {@link ContactEditorActivity}.
        values.clear();
        values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
        values.put(GroupMembership.GROUP_ROW_ID, ContentUris.parseId(groupUri));

    }  
    public static Intent auroraCreateNewGroupIntent2(Context context, AccountWithDataSet account,
            String label, long[] rawContactsToAdd, Class<?> callbackActivity,
            String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_AURORA_CREATE_GROUP2);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_TYPE, account.type);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ACCOUNT_NAME, account.name);
        serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_SET, account.dataSet);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, label);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD, rawContactsToAdd);

        // Callback intent will be invoked by the service once the new group is
        // created.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }    
  //aurora add zhouxiaobing 20131216 end   
    private void auroraCreateGroup(Intent intent) {
        String accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE);
        String accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME);
        String dataSet = intent.getStringExtra(EXTRA_DATA_SET);
        String label = intent.getStringExtra(EXTRA_GROUP_LABEL);
        String groupRingtone = intent.getStringExtra(EXTRA_GROUP_RINGTONE);
        final long[] rawContactsToAdd = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_ADD);

        // the following lines are provided and maintained by Mediatek Inc.
        int[] simIndexArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_ARRAY);
        int slotId = intent.getIntExtra(ContactSaveService.EXTRA_SLOT_ID, -1);
        Log.i(TAG, slotId+"-------slotId-createGroup()");
        int ugrpId = -1;
        if (slotId >=0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Groups.ACCOUNT_TYPE, accountType);
        values.put(Groups.ACCOUNT_NAME, accountName);
        values.put(Groups.DATA_SET, dataSet);
        values.put(Groups.TITLE, label);
        values.put(Groups.GROUP_VISIBLE, "1");
        values.put("group_ringtone", groupRingtone);
        

        final ContentResolver resolver = getContentResolver();

        // Create the new group
        final Uri groupUri = resolver.insert(Groups.CONTENT_URI, values);

        // If there's no URI, then the insertion failed. Abort early because group members can't be
        // added if the group doesn't exist
        if (groupUri == null) {
            Log.e(TAG, "Couldn't create group with label " + label);
            return;
        }

        // Add new group members
        
        /*
         * New feature by Mediatek Begin Original Android code:
         * addMembersToGroup(resolver, rawContactsToAdd,
         * ContentUris.parseId(groupUri));
         */
        addMembersToGroup(resolver, rawContactsToAdd, ContentUris.parseId(groupUri), simIndexArray, slotId, ugrpId);
        /*
         * New feature by Mediatek End
         */

        // TODO: Move this into the contact editor where it belongs. This needs to be integrated
        // with the way other intent extras that are passed to the {@link ContactEditorActivity}.
        values.clear();
        values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
        values.put(GroupMembership.GROUP_ROW_ID, ContentUris.parseId(groupUri));

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        callbackIntent.setData(groupUri);
        // TODO: This can be taken out when the above TODO is addressed
        
        // The following lines are provided and maintained by Mediatek Inc.
        callbackIntent.putExtra("mSlotId", slotId);
        callbackIntent.putExtra("dismissDialog", true);
        // The previous  lines are provided and maintained by Mediatek Inc.

        callbackIntent.putExtra(GnContactsContract.Intents.Insert.DATA, Lists.newArrayList(values));
        deliverCallback(callbackIntent);
    }
    
    private void auroraUpdateGroup(Intent intent) {
        long groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1);
        String label = intent.getStringExtra(EXTRA_GROUP_LABEL);
        String groupRingtone = intent.getStringExtra(EXTRA_GROUP_RINGTONE);
        long[] rawContactsToAdd = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_ADD);
        long[] rawContactsToRemove = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_REMOVE);
        long[] rawContactsToUpdate = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_UPDATE);

        int[] simIndexToAddArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_ADD);
        int[] simIndexToRemoveArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_REMOVE);
        int slotId = intent.getIntExtra(ContactSaveService.EXTRA_SLOT_ID, -1);
        
        int ugrpId = -1;
        if (slotId >=0) {
            return;
        }
        
        if (groupId == -1) {
            Log.e(TAG, "Invalid arguments for updateGroup request");
            return;
        }

        final ContentResolver resolver = getContentResolver();
        final Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);

        // Update group name if necessary
        if (label != null) {
            ContentValues values = new ContentValues();
            values.put(Groups.TITLE, label);
            values.put("group_ringtone", groupRingtone);
            resolver.update(groupUri, values, null, null);
        }

         removeMembersFromGroup(resolver, rawContactsToRemove, groupId, simIndexToRemoveArray, slotId, ugrpId);
         addMembersToGroup(resolver, rawContactsToAdd, groupId, simIndexToAddArray, slotId, ugrpId);
         
         updateMembersFromGroup(resolver, rawContactsToUpdate, groupRingtone);

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        callbackIntent.setData(groupUri);
        
        callbackIntent.putExtra("mSlotId", slotId);
        callbackIntent.putExtra("dismissDialog", true);
        Log.i(TAG, slotId+"----mSlotId[ContctSaveService]"); 
        deliverCallback(callbackIntent);
    }
    
    private void updateMembersFromGroup(ContentResolver resolver,
            long[] rawContactsToUpdate, String groupRingtone) {
        if (rawContactsToUpdate == null) {
            return;
        }

        long rawContactId = 0;
        int mCount = 0;
        final int MAX_OP_COUNT_IN_ONE_BATCH = 20;
        ArrayList<ContentProviderOperation> rawContactOperations = new ArrayList<ContentProviderOperation>();
        for (int i = 0, count = rawContactsToUpdate.length; i < count; i++) {
            rawContactId = rawContactsToUpdate[i];

            // Build an update operation
            final ContentProviderOperation.Builder updateBuilder = ContentProviderOperation
                    .newUpdate(Contacts.CONTENT_URI);
            updateBuilder.withValue(Contacts.CUSTOM_RINGTONE, groupRingtone);
            updateBuilder.withSelection("name_raw_contact_id=" + rawContactId, null);
            rawContactOperations.add(updateBuilder.build());

            mCount++;
            if (mCount > MAX_OP_COUNT_IN_ONE_BATCH) {
                Log.i(TAG, "mCount > MAX_OP_COUNT_IN_ONE_BATCH");
                try {
                    // Apply batch
                    ContentProviderResult[] results = null;
                    if (!rawContactOperations.isEmpty()) {
                        results = resolver.applyBatch(
                                GnContactsContract.AUTHORITY,
                                rawContactOperations);
                    }
                    
                    try {
                        Thread.sleep(300);                     
                    } catch (Exception e) {
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    // The assert could have failed because the contact is
                    // already in the group,
                    // just continue to the next contact
                    e.printStackTrace();
                }
                rawContactOperations.clear();
                mCount = 0;
            }
        }
        try {
            Log.i(TAG, "mCount < MAX_OP_COUNT_IN_ONE_BATCH");
            // Apply batch
            ContentProviderResult[] results = null;
            if (!rawContactOperations.isEmpty()) {
                results = resolver.applyBatch(GnContactsContract.AUTHORITY,
                        rawContactOperations);
            }
        } catch (RemoteException e) {
            // Something went wrong, bail without success
            Log.e(TAG, "Problem persisting user edits for raw contact ID ", e);
        } catch (OperationApplicationException e) {
            // The assert could have failed because the contact is already in
            // the group,
            // just continue to the next contact
            Log.w(TAG, "Assert failed in adding raw contact ID "
                    + ". Already exists in group ", e);
        }
    }

    /**
     * Creates an intent that can be sent to this service to rename a group.
     */
    public static Intent createGroupRenameIntent(Context context, long groupId, String newLabel,
            Class<?> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_RENAME_GROUP);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_ID, groupId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, newLabel);

        // Callback intent will be invoked by the service once the group is renamed.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }

    private void renameGroup(Intent intent) {
        long groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1);
        String label = intent.getStringExtra(EXTRA_GROUP_LABEL);

        if (groupId == -1) {
            Log.e(TAG, "Invalid arguments for renameGroup request");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Groups.TITLE, label);
        final Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        getContentResolver().update(groupUri, values, null, null);

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        callbackIntent.setData(groupUri);
        deliverCallback(callbackIntent);
    }

    /**
     * Creates an intent that can be sent to this service to delete a group.
     */
    public static Intent createGroupDeletionIntent(Context context, long groupId) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_DELETE_GROUP);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_ID, groupId);
        return serviceIntent;
    }

    private void deleteGroup(Intent intent) {
        long groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1);
        
        if (groupId == -1) {
            Log.e(TAG, "Invalid arguments for deleteGroup request");
            return;
        }
        // The following lines are provided and maintained by Mediatek Inc.
        String groupLabel = intent.getStringExtra(EXTRA_GROUP_LABEL);
        Log.i(TAG, groupLabel+"----groupLabel--------[deleteGroup]");
        
        int slotId = intent.getIntExtra(ContactSaveService.EXTRA_SLOT_ID, -1);
        if(slotId >=0){
            int simId = intent.getIntExtra(ContactSaveService.EXTRA_SIM_ID, -1);
            Log.i(TAG, simId+"----simId--------[deleteGroup]");
            int ugrpId = -1;
            Uri groupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, groupLabel);
            Cursor c = getContentResolver().query(groupUri, 
                    new String[]{Contacts._ID, Contacts.INDEX_IN_SIM}, 
                    Contacts.INDICATE_PHONE_SIM+" = "+simId , null, null);
            int[] simIndexArray = null;
            Log.i(TAG, c.getCount()+"----c.getCount()--------[deleteGroup]");
            if (c != null && c.getCount() > 0) {
                simIndexArray = new int[c.getCount()];
                int i = 0;
                if (c.moveToFirst()) {
                    do {
                        simIndexArray[i] = c.getInt(1);
                    } while (c.moveToNext());
                }
            }
            if(c != null) {
                c.close();
            }
            Log.i(TAG, simIndexArray+"----simIndexArray--------[deleteGroup]");
            if (slotId >=0 && !TextUtils.isEmpty(groupLabel) ) {   
                if (simIndexArray != null) {
                    try {
                        ugrpId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, groupLabel);
                        Log.i(TAG, ugrpId+"----ugrpId--------[deleteGroup]");
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //Delete USIM group members
                    int i =0;
                    for (int simIndex : simIndexArray) {
                        ContactsGroupUtils.USIMGroup.deleteUSIMGroupMember(slotId, simIndexArray[i], ugrpId);
                        Log.i(TAG, ugrpId+"----ugrpId--------[deleteGroup]");
                        i++;
                    }
                }
                //Delete USIM group
                int error = ContactsGroupUtils.USIMGroup.deleteUSIMGroup(slotId, groupLabel);   
                Log.i(TAG, error+"----error--------[deleteGroup]");
            }   
        }
       
   	    // The previous  lines are provided and maintained by Mediatek Inc.
        
        getContentResolver().delete(
                ContentUris.withAppendedId(Groups.CONTENT_URI, groupId), null, null);
    }

    /**
     * Creates an intent that can be sent to this service to rename a group as
     * well as add and remove members from the group.
     *
     * @param context of the application
     * @param groupId of the group that should be modified
     * @param newLabel is the updated name of the group (can be null if the name
     *            should not be updated)
     * @param rawContactsToAdd is an array of raw contact IDs for contacts that
     *            should be added to the group
     * @param rawContactsToRemove is an array of raw contact IDs for contacts
     *            that should be removed from the group
     * @param callbackActivity is the activity to send the callback intent to
     * @param callbackAction is the intent action for the callback intent
     */
    public static Intent createGroupUpdateIntent(Context context, long groupId, String newLabel,
            long[] rawContactsToAdd, long[] rawContactsToRemove,
            Class<?> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_UPDATE_GROUP);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_ID, groupId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, newLabel);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD, rawContactsToAdd);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_REMOVE,
                rawContactsToRemove);

        // Callback intent will be invoked by the service once the group is updated
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }

    private void updateGroup(Intent intent) {
        long groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1);
        String label = intent.getStringExtra(EXTRA_GROUP_LABEL);
        long[] rawContactsToAdd = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_ADD);
        long[] rawContactsToRemove = intent.getLongArrayExtra(EXTRA_RAW_CONTACTS_TO_REMOVE);

        // the following lines are provided and maintained by Mediatek Inc.
        
        int[] simIndexToAddArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_ADD);
        int[] simIndexToRemoveArray = intent.getIntArrayExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_REMOVE);
        int slotId = intent.getIntExtra(ContactSaveService.EXTRA_SLOT_ID, -1);
        String originalName = intent.getStringExtra(ContactSaveService.EXTRA_ORIGINAL_GROUP_NAME);
        
        int ugrpId = -1;
        if (slotId >=0) {
        	try {
                ugrpId = ContactsGroupUtils.USIMGroup.syncUSIMGroupUpdate(slotId, originalName, label);
                Log.i(TAG, ugrpId+"---------ugrpId[updateGroup]");
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (USIMGroupException e) {
                Log.d(TAG, "[SyncUSIMGroup] catched USIMGroupException." 
                        + " ErrorType: " + e.getErrorType());
                mErrorType[e.getErrorSlotId()] = e.getErrorType();
                // USIM Group begin
                if (mErrorType[0] > 0) {
                    // slot 0 for Gemini or slot for single card.
                    Log.d(TAG, "[showToast] mErrorType[0]b: " + mErrorType[0]);
                    showMoveUSIMGroupErrorToast(mErrorType[0], 0);
                    Log.d(TAG, "[showToast] mErrorType[0]e: " + mErrorType[0]);

                }
                if (mErrorType[1] > 0) {
                    showMoveUSIMGroupErrorToast(mErrorType[1], 1);
                }
                Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
                Log.i(TAG, EXTRA_CALLBACK_INTENT);
                callbackIntent.putExtra("dismissDialog", true);
                callbackIntent.setAction("dismissDailog");
                deliverCallback(callbackIntent);
             // USIM Group end
                return;
            }
        	 if ( ugrpId < 1){
             	return;
             }
        }
   	    // the previous lines are provided and maintained by Mediatek Inc.
        
        if (groupId == -1) {
            Log.e(TAG, "Invalid arguments for updateGroup request");
            return;
        }

        final ContentResolver resolver = getContentResolver();
        final Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);

        // Update group name if necessary
        if (label != null) {
            ContentValues values = new ContentValues();
            values.put(Groups.TITLE, label);
            resolver.update(groupUri, values, null, null);
        }

        // Add and remove members if necessary
        
        
    	/*
		 * New feature by Mediatek Begin Original Android code:
		 * addMembersToGroup(resolver, rawContactsToAdd,
		 * ContentUris.parseId(groupUri));
		 * removeMembersFromGroup(resolver, rawContactsToRemove, groupId);
		 */
		 
		 removeMembersFromGroup(resolver, rawContactsToRemove, groupId, simIndexToRemoveArray, slotId, ugrpId);
		 addMembersToGroup(resolver, rawContactsToAdd, groupId, simIndexToAddArray, slotId, ugrpId);
		/*
		 * New feature by Mediatek End
		 */

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        callbackIntent.setData(groupUri);
        
        // The following lines are provided and maintained by Mediatek Inc.
        callbackIntent.putExtra("mSlotId", slotId);
        callbackIntent.putExtra("dismissDialog", true);
        Log.i(TAG, slotId+"----mSlotId[ContctSaveService]"); 
        // The previous  lines are provided and maintained by Mediatek Inc.
        deliverCallback(callbackIntent);
    }

    private void addMembersToGroup(ContentResolver resolver, long[] rawContactsToAdd, long groupId) {
        if (rawContactsToAdd == null) {
            return;
        }
        for (long rawContactId : rawContactsToAdd) {
            try {
                final ArrayList<ContentProviderOperation> rawContactOperations =
                        new ArrayList<ContentProviderOperation>();

                // Build an assert operation to ensure the contact is not already in the group
                final ContentProviderOperation.Builder assertBuilder = ContentProviderOperation
                        .newAssertQuery(Data.CONTENT_URI);
                assertBuilder.withSelection(Data.RAW_CONTACT_ID + "=? AND " +
                        Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                        new String[] { String.valueOf(rawContactId),
                        GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)});
                assertBuilder.withExpectedCount(0);
                rawContactOperations.add(assertBuilder.build());

                // Build an insert operation to add the contact to the group
                final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
                        .newInsert(Data.CONTENT_URI);
                insertBuilder.withValue(Data.RAW_CONTACT_ID, rawContactId);
                insertBuilder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                insertBuilder.withValue(GroupMembership.GROUP_ROW_ID, groupId);
                rawContactOperations.add(insertBuilder.build());

                if (DEBUG) {
                    for (ContentProviderOperation operation : rawContactOperations) {
                        Log.v(TAG, operation.toString());
                    }
                }

                // Apply batch
                ContentProviderResult[] results = null;
                if (!rawContactOperations.isEmpty()) {
                    results = resolver.applyBatch(GnContactsContract.AUTHORITY, rawContactOperations);
                }
            } catch (RemoteException e) {
                // Something went wrong, bail without success
                Log.e(TAG, "Problem persisting user edits for raw contact ID " +
                        String.valueOf(rawContactId), e);
            } catch (OperationApplicationException e) {
                // The assert could have failed because the contact is already in the group,
                // just continue to the next contact
                Log.w(TAG, "Assert failed in adding raw contact ID " +
                        String.valueOf(rawContactId) + ". Already exists in group " +
                        String.valueOf(groupId), e);
            }
        }
    }

    private void removeMembersFromGroup(ContentResolver resolver, long[] rawContactsToRemove,
            long groupId) {
        if (rawContactsToRemove == null) {
            return;
        }
        for (long rawContactId : rawContactsToRemove) {
            // Apply the delete operation on the data row for the given raw contact's
            // membership in the given group. If no contact matches the provided selection, then
            // nothing will be done. Just continue to the next contact.
            getContentResolver().delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=? AND " +
                    Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                    new String[] { String.valueOf(rawContactId),
                    GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)});
        }
    }

    /**
     * Creates an intent that can be sent to this service to star or un-star a contact.
     */
    public static Intent createSetStarredIntent(Context context, Uri contactUri, boolean value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_STARRED);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_URI, contactUri);
        serviceIntent.putExtra(ContactSaveService.EXTRA_STARRED_FLAG, value);

        return serviceIntent;
    }
    
    public static Intent createSetStarredIntent(Context context, long[] rawContactIds, boolean value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_STARRED);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_URI, Contacts.CONTENT_URI);
        serviceIntent.putExtra(ContactSaveService.EXTRA_STARRED_FLAG, value);
        serviceIntent.putExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD, rawContactIds);

        return serviceIntent;
    }

    private void setStarred(Intent intent) {
        Uri contactUri = intent.getParcelableExtra(EXTRA_CONTACT_URI);
        boolean value = intent.getBooleanExtra(EXTRA_STARRED_FLAG, false);
        if (contactUri == null) {
            Log.e(TAG, "Invalid arguments for setStarred request");
            return;
        }
        
        String selection = null;
        long[] rawContactIds = intent.getLongArrayExtra(ContactSaveService.EXTRA_RAW_CONTACTS_TO_ADD);
        if (null != rawContactIds) {
        	StringBuilder sb = new StringBuilder(" name_raw_contact_id IN (");
        	
        	for (int i = 0, s = rawContactIds.length - 1; i < s; i++) {
        		sb.append(rawContactIds[i]).append(",");
        	}
        	sb.append(rawContactIds[rawContactIds.length-1]).append(")");
        	
        	selection = sb.toString();
        }

        final ContentValues values = new ContentValues(1);
        values.put(Contacts.STARRED, value);
        getContentResolver().update(contactUri, values, selection, null);
    }

    /**
     * Creates an intent that can be sent to this service to set the redirect to voicemail.
     */
    public static Intent createSetSendToVoicemail(Context context, Uri contactUri,
            boolean value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_SEND_TO_VOICEMAIL);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_URI, contactUri);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SEND_TO_VOICEMAIL_FLAG, value);

        return serviceIntent;
    }

    /**
     * Creates an intent that can be sent to this service to set the redirect to voicemail.
     */
    
    /*
     * New Feature by Mediatek Begin.            
     * Creates an intent that can be sent to this service to set the value of block video call        
     */
    public static Intent createSetBlockVideoCall(Context context, Uri contactUri, boolean value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_BLOCK_VIDEO_CALL);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_URI, contactUri);
        serviceIntent.putExtra(ContactSaveService.EXTRA_BLOCK_VIDEO_CALL_FLAG, value);

        return serviceIntent;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    private void setSendToVoicemail(Intent intent) {
        Uri contactUri = intent.getParcelableExtra(EXTRA_CONTACT_URI);
        boolean value = intent.getBooleanExtra(EXTRA_SEND_TO_VOICEMAIL_FLAG, false);
        if (contactUri == null) {
            Log.e(TAG, "Invalid arguments for setRedirectToVoicemail");
            return;
        }

        final ContentValues values = new ContentValues(1);
        values.put(Contacts.SEND_TO_VOICEMAIL, value);
        getContentResolver().update(contactUri, values, null, null);
    }

    /*
     * New Feature by Mediatek Begin.            
     * save the value of block video call to db        
     */
    private void setBlockVideoCall(Intent intent) {
        Uri contactUri = intent.getParcelableExtra(EXTRA_CONTACT_URI);
        boolean value = intent.getBooleanExtra(EXTRA_BLOCK_VIDEO_CALL_FLAG, false);
        if (contactUri == null) {
            Log.e(TAG, "Invalid arguments for setBlockVideoCall");
            return;
        }

        final ContentValues values = new ContentValues(1);
        values.put(Contacts.SEND_TO_VOICEMAIL_VT, value);
        getContentResolver().update(contactUri, values, null, null);
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /**
     * Creates an intent that can be sent to this service to save the contact's ringtone.
     */
    public static Intent createSetRingtone(Context context, Uri contactUri,
            String value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_RINGTONE);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_URI, contactUri);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CUSTOM_RINGTONE, value);

        return serviceIntent;
    }
    
    public static Intent createBatchSetRingtone(Context context, String selection, 
    		String value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_RINGTONE);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SELECTION, selection);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CUSTOM_RINGTONE, value);

        return serviceIntent;
    }

    private void setRingtone(Intent intent) {
    	Uri contactUri = intent.getParcelableExtra(EXTRA_CONTACT_URI);
    	String value = intent.getStringExtra(EXTRA_CUSTOM_RINGTONE);
    	String selection = null;
    	
    	if (ContactsApplication.sIsGnContactsSupport) {
    		selection = intent.getStringExtra(EXTRA_SELECTION);
    		if (null != selection) {
    			contactUri = GnContactsContract.Contacts.CONTENT_URI;
    		}
    	}    	
                
        if (contactUri == null) {
            Log.e(TAG, "Invalid arguments for setRingtone");
            return;
        }
        
        ContentValues values = new ContentValues(1);
        values.put(Contacts.CUSTOM_RINGTONE, value);
        getContentResolver().update(contactUri, values, selection, null);
    }

    /**
     * Creates an intent that sets the selected data item as super primary (default)
     */
    public static Intent createSetSuperPrimaryIntent(Context context, long dataId) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_SET_SUPER_PRIMARY);
        serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_ID, dataId);
        return serviceIntent;
    }

    private void setSuperPrimary(Intent intent) {
        long dataId = intent.getLongExtra(EXTRA_DATA_ID, -1);
        if (dataId == -1) {
            Log.e(TAG, "Invalid arguments for setSuperPrimary request");
            return;
        }

        // Update the primary values in the data record.
        ContentValues values = new ContentValues(1);
        values.put(Data.IS_SUPER_PRIMARY, 1);
        values.put(Data.IS_PRIMARY, 1);

        getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, dataId),
                values, null, null);
    }

    /**
     * Creates an intent that clears the primary flag of all data items that belong to the same
     * raw_contact as the given data item. Will only clear, if the data item was primary before
     * this call
     */
    public static Intent createClearPrimaryIntent(Context context, long dataId) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_CLEAR_PRIMARY);
        serviceIntent.putExtra(ContactSaveService.EXTRA_DATA_ID, dataId);
        return serviceIntent;
    }

    private void clearPrimary(Intent intent) {
        long dataId = intent.getLongExtra(EXTRA_DATA_ID, -1);
        if (dataId == -1) {
            Log.e(TAG, "Invalid arguments for clearPrimary request");
            return;
        }

        // Update the primary values in the data record.
        ContentValues values = new ContentValues(1);
        values.put(Data.IS_SUPER_PRIMARY, 0);
        values.put(Data.IS_PRIMARY, 0);

        getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, dataId),
                values, null, null);
    }

    /**
     * Creates an intent that can be sent to this service to delete a contact.
     */
    public static Intent createDeleteContactIntent(Context context, Uri contactUri) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_DELETE_CONTACT);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_URI, contactUri);
        return serviceIntent;
    }

    private void deleteContact(Intent intent) {
        Uri contactUri = intent.getParcelableExtra(EXTRA_CONTACT_URI);
        if (contactUri == null) {
            Log.e(TAG, "Invalid arguments for deleteContact request");
            return;
        }

        int ret = getContentResolver().delete(contactUri, null, null);
        
        //Gionee <huangzy> <2013-04-26> add for CR00795330 begin
        if (ret > 0 && null != contactUri &&
        		contactUri.toString().contains("contacts/lookup/profile")) {
    		ContactsUtils.broadcastUserProfileUpdated();
    	}
    	//Gionee <huangzy> <2013-04-26> add for CR00795330 end
    }

    /**
     * Creates an intent that can be sent to this service to join two contacts.
     */
    public static Intent createJoinContactsIntent(Context context, long contactId1,
            long contactId2, boolean contactWritable,
            Class<?> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction(ContactSaveService.ACTION_JOIN_CONTACTS);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_ID1, contactId1);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_ID2, contactId2);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CONTACT_WRITABLE, contactWritable);

        // Callback intent will be invoked by the service once the contacts are joined.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);

        return serviceIntent;
    }


    private interface JoinContactQuery {
        String[] PROJECTION = {
                RawContacts._ID,
                RawContacts.CONTACT_ID,
                RawContacts.NAME_VERIFIED,
                RawContacts.DISPLAY_NAME_SOURCE,
        };

        String SELECTION = RawContacts.CONTACT_ID + "=? OR " + RawContacts.CONTACT_ID + "=?";

        int _ID = 0;
        int CONTACT_ID = 1;
        int NAME_VERIFIED = 2;
        int DISPLAY_NAME_SOURCE = 3;
    }

    private void joinContacts(Intent intent) {
        long contactId1 = intent.getLongExtra(EXTRA_CONTACT_ID1, -1);
        long contactId2 = intent.getLongExtra(EXTRA_CONTACT_ID2, -1);
        boolean writable = intent.getBooleanExtra(EXTRA_CONTACT_WRITABLE, false);
        if (contactId1 == -1 || contactId2 == -1) {
            Log.e(TAG, "Invalid arguments for joinContacts request");
            return;
        }

        final ContentResolver resolver = getContentResolver();

        // Load raw contact IDs for all raw contacts involved - currently edited and selected
        // in the join UIs
        Cursor c = resolver.query(RawContacts.CONTENT_URI,
                JoinContactQuery.PROJECTION,
                JoinContactQuery.SELECTION,
                new String[]{String.valueOf(contactId1), String.valueOf(contactId2)}, null);

        long rawContactIds[];
        long verifiedNameRawContactId = -1;
        try {
            int maxDisplayNameSource = -1;
            rawContactIds = new long[c.getCount()];
            for (int i = 0; i < rawContactIds.length; i++) {
                c.moveToPosition(i);
                long rawContactId = c.getLong(JoinContactQuery._ID);
                rawContactIds[i] = rawContactId;
                int nameSource = c.getInt(JoinContactQuery.DISPLAY_NAME_SOURCE);
                if (nameSource > maxDisplayNameSource) {
                    maxDisplayNameSource = nameSource;
                }
            }

            // Find an appropriate display name for the joined contact:
            // if should have a higher DisplayNameSource or be the name
            // of the original contact that we are joining with another.
            if (writable) {
                for (int i = 0; i < rawContactIds.length; i++) {
                    c.moveToPosition(i);
                    if (c.getLong(JoinContactQuery.CONTACT_ID) == contactId1) {
                        int nameSource = c.getInt(JoinContactQuery.DISPLAY_NAME_SOURCE);
                        if (nameSource == maxDisplayNameSource
                                && (verifiedNameRawContactId == -1
                                        || c.getInt(JoinContactQuery.NAME_VERIFIED) != 0)) {
                            verifiedNameRawContactId = c.getLong(JoinContactQuery._ID);
                        }
                    }
                }
            }
        } finally {
            c.close();
        }

        // For each pair of raw contacts, insert an aggregation exception
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < rawContactIds.length; i++) {
            for (int j = 0; j < rawContactIds.length; j++) {
                if (i != j) {
                    buildJoinContactDiff(operations, rawContactIds[i], rawContactIds[j]);
                }
            }
        }

        // Mark the original contact as "name verified" to make sure that the contact
        // display name does not change as a result of the join
        if (verifiedNameRawContactId != -1) {
            Builder builder = ContentProviderOperation.newUpdate(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, verifiedNameRawContactId));
            builder.withValue(RawContacts.NAME_VERIFIED, 1);
            operations.add(builder.build());
        }

        boolean success = false;
        // Apply all aggregation exceptions as one batch
        try {
            resolver.applyBatch(GnContactsContract.AUTHORITY, operations);
            showToast(R.string.contactsJoinedMessage);
            success = true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to apply aggregation exception batch", e);
            showToast(R.string.contactSavedErrorToast);
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Failed to apply aggregation exception batch", e);
            showToast(R.string.contactSavedErrorToast);
        }

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        if (success) {
            Uri uri = RawContacts.getContactLookupUri(resolver,
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactIds[0]));
            callbackIntent.setData(uri);
        }
        deliverCallback(callbackIntent);
    }

    /**
     * Construct a {@link AggregationExceptions#TYPE_KEEP_TOGETHER} ContentProviderOperation.
     */
    private void buildJoinContactDiff(ArrayList<ContentProviderOperation> operations,
            long rawContactId1, long rawContactId2) {
        Builder builder =
                ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
        builder.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
        operations.add(builder.build());
    }

    /**
     * Shows a toast on the UI thread.
     */
    private void showToast(final int message) {
        mMainHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ContactSaveService.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deliverCallback(final Intent callbackIntent) {
        mMainHandler.post(new Runnable() {

            @Override
            public void run() {
                deliverCallbackOnUiThread(callbackIntent);
            }
        });
    }

    void deliverCallbackOnUiThread(final Intent callbackIntent) {
        // TODO: this assumes that if there are multiple instances of the same
        // activity registered, the last one registered is the one waiting for
        // the callback. Validity of this assumption needs to be verified.
        for (Listener listener : sListeners) {
            if (callbackIntent.getComponent().equals(
                    ((Activity) listener).getIntent().getComponent())) {
                listener.onServiceCompleted(callbackIntent);
                return;
            }
        }
    }
    
    // the following lines are provided and maintained by Mediatek Inc.
    public static final String EXTRA_SIM_INDEX_TO_ADD = "simIndexToAdd";
    public static final String EXTRA_SIM_INDEX_TO_REMOVE = "simIndexToRemove";
    public static final String EXTRA_ORIGINAL_GROUP_NAME = "originalGroupName";
    
    public static final String EXTRA_SIM_INDEX_ARRAY = "simIndexArray";
    public static final String EXTRA_SLOT_ID = "slotId";
    public static final String EXTRA_SIM_ID = "simId";
    
    //aurora <wangth> <2013-9-16> add for auroro ui begin
    public static final String EXTRA_GROUP_RINGTONE = "groupRingtone";
    //aurora <wangth> <2013-9-16> add for auroro ui end
    
    private int []mErrorType = {-1,-1};
//    private static Context mContext;
    /** 
     * add two parm base on the function createNewGroupIntent
     * @param simIndex  
     * @param simSlotId
     * @return
     */
    public static Intent createNewGroupIntent(Context context, AccountWithDataSet account,
            String label, final long[] rawContactsToAdd, Class<?> callbackActivity,
            String callbackAction, final int[] simIndexArray,int slotId) { 
//        mContext = context;
        Intent serviceIntent = createNewGroupIntent(context, account,label, rawContactsToAdd, callbackActivity, callbackAction);

        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_INDEX_ARRAY, simIndexArray);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SLOT_ID, slotId);
        return serviceIntent;
    }
    
    //aurora <wangth> <2013-9-16> add for auroro ui begin
    public static Intent auroraCreateNewGroupIntent(Context context, AccountWithDataSet account,
            String label, final long[] rawContactsToAdd, Class<?> callbackActivity,
            String callbackAction, final int[] simIndexArray,int slotId) { 
//        mContext = context;
        Intent serviceIntent = auroraCreateNewGroupIntent(context, account,label, rawContactsToAdd, callbackActivity, callbackAction);

        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_INDEX_ARRAY, simIndexArray);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SLOT_ID, slotId);
        return serviceIntent;
    }
    
    public static Intent auroraUpdateGroupIntent(Context context, long groupId, String newLabel,
            long[] rawContactsToAdd, long[] rawContactsToRemove, long[] rawContactsToUpdate,
            Class<?> callbackActivity, String callbackAction, String OriginalGroupName, int slotId, 
            int[] simIndexToAddArray,int[] simIndexToRemoveArray) {
//        mContext = context;
        Intent serviceIntent = auroraUpdateGroupIntent(context, groupId, newLabel,
                rawContactsToAdd, rawContactsToRemove, rawContactsToUpdate,
                 callbackActivity, callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SLOT_ID, slotId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_ADD, simIndexToAddArray);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_REMOVE, simIndexToRemoveArray);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ORIGINAL_GROUP_NAME, OriginalGroupName);
        return serviceIntent;
    }
    //aurora <wangth> <2013-9-16> add for auroro ui end
    
    /** 
     * update the group intent add four params then to ReName the group add 
     * addMembersToGroup or removeMembersFromGroup
     * @param OriginalGroupName 
     * @param slotId 
     * @param simIndexToAddArray  
     * @param simIndexToRemoveArray
     * @return
     */
    public static Intent createGroupUpdateIntent(Context context, long groupId, String newLabel,
            long[] rawContactsToAdd, long[] rawContactsToRemove,
            Class<?> callbackActivity, String callbackAction, String OriginalGroupName, int slotId, 
            int[] simIndexToAddArray,int[] simIndexToRemoveArray) {
//        mContext = context;
        Intent serviceIntent = createGroupUpdateIntent( context, groupId, newLabel,
                rawContactsToAdd, rawContactsToRemove,
                 callbackActivity, callbackAction);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SLOT_ID, slotId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_ADD, simIndexToAddArray);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_INDEX_TO_REMOVE, simIndexToRemoveArray);
        serviceIntent.putExtra(ContactSaveService.EXTRA_ORIGINAL_GROUP_NAME, OriginalGroupName);
        return serviceIntent;
    }
    
    private void addMembersToGroup(ContentResolver resolver, long[] rawContactsToAdd,
            long groupId, int[] simIndexArry, int slotId, int ugrpId) {
    	 if (rawContactsToAdd == null) {
             return;
         }
    	 long rawContactId =-1 ;
    	 int simIndex = -1; 
         int mCount = 0;
         final int MAX_OP_COUNT_IN_ONE_BATCH = 20;
         ArrayList<ContentProviderOperation> rawContactOperations =
                 new ArrayList<ContentProviderOperation>();    
    	 for(int i = 0,count = rawContactsToAdd.length;i < count;i++){
    	     rawContactId = rawContactsToAdd[i];
    	     simIndex = simIndexArry[i];
    	     Log.i(TAG, "slotId"+slotId+"--");
    	     Log.i(TAG, "simIndex"+simIndex+"==");
    	     Log.i(TAG, "ugrpId"+ugrpId+"---");
             boolean ret = false;
             if (slotId >=0 && simIndex >=0 && ugrpId >=0) {
                ret = ContactsGroupUtils.USIMGroup.addUSIMGroupMember(slotId, simIndex, ugrpId);
                Log.i(TAG, "ret"+ret);
                if (!ret) 
                    continue;
             }
                 // Build an assert operation to ensure the contact is not already in the group
                 final ContentProviderOperation.Builder assertBuilder = ContentProviderOperation
                         .newAssertQuery(Data.CONTENT_URI);
                 assertBuilder.withSelection(Data.RAW_CONTACT_ID + "=? AND " +
                         Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                         new String[] { String.valueOf(rawContactId),
                         GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)});
                 assertBuilder.withExpectedCount(0);
                 rawContactOperations.add(assertBuilder.build());

                 // Build an insert operation to add the contact to the group
                 final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
                         .newInsert(Data.CONTENT_URI);
                 insertBuilder.withValue(Data.RAW_CONTACT_ID, rawContactId);
                 insertBuilder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                 insertBuilder.withValue(GroupMembership.GROUP_ROW_ID, groupId);
                 rawContactOperations.add(insertBuilder.build());

                 if (DEBUG) {
                     for (ContentProviderOperation operation : rawContactOperations) {
                         Log.v(TAG, operation.toString());
                     }
                 }
                 mCount++;
                 if (mCount >MAX_OP_COUNT_IN_ONE_BATCH ) {
                	 Log.i(TAG, "mCount >MAX_OP_COUNT_IN_ONE_BATCH");
                     try {
                         // Apply batch
                         ContentProviderResult[] results = null;
                         if (!rawContactOperations.isEmpty()) {
                             results = resolver.applyBatch(GnContactsContract.AUTHORITY, rawContactOperations);
                         }
                         
                         try {
                             Thread.sleep(300);                     
                         } catch (Exception e) {
                         }
                     } catch (RemoteException e) {
                         // Something went wrong, bail without success
                         Log.e(TAG, "Problem persisting user edits for raw contact ID " +
                                 String.valueOf(rawContactId), e);
                     } catch (OperationApplicationException e) {
                         // The assert could have failed because the contact is already in the group,
                         // just continue to the next contact
                         Log.w(TAG, "Assert failed in adding raw contact ID " +
                                 String.valueOf(rawContactId) + ". Already exists in group " +
                                 String.valueOf(groupId), e);
                     }  
                     rawContactOperations.clear();
                     mCount = 0;
                 }                     
         } 	
         try {
        	 Log.i(TAG, "mCount<MAX_OP_COUNT_IN_ONE_BATCH");
             // Apply batch
             ContentProviderResult[] results = null;
             if (!rawContactOperations.isEmpty()) {
                 results = resolver.applyBatch(GnContactsContract.AUTHORITY, rawContactOperations);
             }
         } catch (RemoteException e) {
             // Something went wrong, bail without success
             Log.e(TAG, "Problem persisting user edits for raw contact ID " , e);
         } catch (OperationApplicationException e) {
             // The assert could have failed because the contact is already in the group,
             // just continue to the next contact
             Log.w(TAG, "Assert failed in adding raw contact ID " + ". Already exists in group ", e);
         }  
    }
    
    
    private void removeMembersFromGroup(ContentResolver resolver, long[] rawContactsToRemove,
            long groupId, int[] simIndexArray, int slotId,int ugrpId) {
    	removeMembersFromGroup(resolver, rawContactsToRemove,
                groupId);
    	 if (rawContactsToRemove == null) {
             return;
         }
    	 long rawContactId;
    	 int simIndex;
    	 for(int i = 0,count = rawContactsToRemove.length;i < count;i++){
             rawContactId = rawContactsToRemove[i];
        	 simIndex = simIndexArray[i];
             boolean ret = false;
             if (slotId >=0 && simIndex >= 0 && ugrpId >=0) {
           	  ContactsGroupUtils.USIMGroup.deleteUSIMGroupMember(slotId, simIndex, ugrpId);
                if (!ret) 
                    continue;
             }
        	 
             // Apply the delete operation on the data row for the given raw contact's
             // membership in the given group. If no contact matches the provided selection, then
             // nothing will be done. Just continue to the next contact.
             getContentResolver().delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=? AND " +
                     Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                     new String[] { String.valueOf(rawContactId),
                     GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)});
         }
    }
    
    
    /**
     * Creates an intent that can be sent to this service to delete a group.
     */
    public static Intent createGroupDeletionIntent(Context context, long groupId, int simId, int slotId, String groupLabel) {
        Intent serviceIntent = createGroupDeletionIntent(context, groupId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SIM_ID, simId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_SLOT_ID, slotId);
        serviceIntent.putExtra(ContactSaveService.EXTRA_GROUP_LABEL, groupLabel);
        return serviceIntent;
    }

    private void showMoveUSIMGroupErrorToast(int errCode, int slot) {
        String toastMsg = null;
        switch(errCode) {
        case USIMGroupException.GROUP_NAME_OUT_OF_BOUND:{
            toastMsg = getString(R.string.usim_group_name_exceed_limit);
            break;
        }
        case USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND:{
            toastMsg = getString(R.string.usim_group_count_exceed_limit);
            break;
        }
        }
        final String msg = toastMsg;
        if (toastMsg != null) {
            Log.i(TAG, toastMsg+"toastMsg()");
//            ((Activity) mContext).runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                	 Toast.makeText(ContactsApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
//                }
//            });
            Log.i(TAG, toastMsg+"toastMsg() end");
        }
    }
  // The previous lines are provided and maintained by Mediatek Inc.

}
