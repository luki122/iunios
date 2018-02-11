/*
 * Copyright (C) 2011-2012, The Linux Foundation. All rights reserved.
 * Copyright (c) 2013, The Linux Foundation. All Rights Reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following
       disclaimer in the documentation and/or other materials provided
       with the distribution.
     * Neither the name of The Linux Foundation nor the names of its
       contributors may be used to endorse or promote products derived
       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gionee.android.contacts;

import android.app.Service;
import android.accounts.Account;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import aurora.preference.AuroraPreferenceManager; // import android.preference.PreferenceManager;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.Settings;
import android.text.TextUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.util.WeakAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import com.gionee.internal.telephony.GnTelephonyManagerEx;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.SimInfo;

import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;

import android.database.StaleDataException;
import com.mediatek.contacts.util.TelephonyUtils;


public class SimContactsService extends Service {
    private static final String TAG = "SimContactsService";
    private static boolean DBG = true;
    
    private static final String ACTION_PHB_LOAD_FINISHED = "com.android.contacts.ACTION_PHB_LOAD_FINISHED";
    private static final int FINISH_IMPORTING = 500;

    private static final String[] COLUMN_NAMES = new String[] { "name",
            "number", "emails", "anrs", "_id" };

    protected static final int NAME_COLUMN = 0;
    protected static final int NUMBER_COLUMN = 1;
    protected static final int EMAILS_COLUMN = 2;
    protected static final int ANRS_COLUMN = 3;
    protected static final int INDEX_COLUMN = 4;

    static final String[] CONTACTS_ID_PROJECTION = new String[] {
            RawContacts._ID, RawContacts.CONTACT_ID,
            RawContacts.INDICATE_PHONE_SIM, };

    private static final int RAW_CONTACT_ID_COLUMN = 0;
    private static final int CONTACT_ID_COLUMN_COLUMN = 1;

    static final String SIM_DATABASE_SELECTION = RawContacts.INDICATE_PHONE_SIM
            + "=?" + " AND " + RawContacts.DELETED + "=?";
    static final String SIM_DATABASE_DELTETE_SELECTION = RawContacts._ID + " IN ";

    static final String[] SIM_DATABASE_SELECTARGS = { "1", "0" };

    static final String[] SIM_DATABASE_SELECTARGS_SUB1 = { "1", "0" };

    static final String[] SIM_DATABASE_SELECTARGS_SUB2 = { "2", "0" };

    public static final String SUBSCRIPTION = "sub_id";
    public static final String OPERATION = "operation";
    public static final String SIM_STATE = "sim_state";

    protected static final int SUB1 = 0;
    protected static final int SUB2 = 1;
    private static final int DEFAULT_SUB = 0;

    protected static final int QUERY_TOKEN = 0;
    protected static final int INSERT_TOKEN = 1;
    protected static final int UPDATE_TOKEN = 2;
    protected static final int DELETE_TOKEN = 3;

    protected static final int QUERY_TOKEN_DATABASE = 10;
    protected static final int QUERY_TOKEN_DATABASE_SUB1 = 11;
    protected static final int QUERY_TOKEN_DATABASE_SUB2 = 12;
    protected static final int INSERT_TOKEN_DATABASE = 20;
    protected static final int UPDATE_TOKEN_DATABASE = 30;
    protected static final int DELETE_TOKEN_DATABASE = 40;

    private static final String IMSI[] = { "imsi_sub1", "imsi_sub2" };

    static final ContentValues sEmptyContentValues = new ContentValues();
    private static Context mContext;

    private static int mPhoneNumber = 0;
    protected Cursor[] mSimCursor;
    protected Cursor[] mDatabaseCursor;
    protected QuerySimHandler[] mQuerySimHandler;
    protected QueryDatabaseHandler[] mQueryDatabaseHandler;
    private boolean[] isNewCard;
    private boolean[] isSimOperationInprocess;
    private TelephonyManager mTelephonyManager;
    private SharedPreferences mPrefs;
    private volatile Handler mServiceHandler;
    private HashMap<Integer, Integer> refreshQueue;

    private int[] mSimState;

    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 20;
    public static boolean mIsImporting = false;
    
    private static ArrayList<String> mNamesArray = new ArrayList<String>();
    private static ArrayList<String> mPhoneNumberArray = new ArrayList<String>();

    @Override
    public void onCreate() {
        Log.d(TAG, "service onCreate!");
        mPhoneNumber = GNContactsUtils.getPhoneCount();
        mContext = getApplicationContext();
        final boolean isAirMode = android.provider.Settings.System.getInt(
                mContext.getContentResolver(),
                android.provider.Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        mTelephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(mContext);
        mQuerySimHandler = new QuerySimHandler[mPhoneNumber];
        mQueryDatabaseHandler = new QueryDatabaseHandler[mPhoneNumber];
        mSimCursor = new Cursor[mPhoneNumber];
        mDatabaseCursor = new Cursor[mPhoneNumber];
        isNewCard = new boolean[mPhoneNumber];
        isSimOperationInprocess = new boolean[mPhoneNumber];
        mSimState = new int[mPhoneNumber];
        refreshQueue = new HashMap<Integer, Integer>();

        mServiceHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle args = (Bundle) msg.obj;
                switch (msg.what) {

                case GNContactsUtils.OP_PHONE:
                    for (int i = 0; i < GNContactsUtils.getPhoneCount(); i++) {
//                        if (!GNContactsUtils.hasIccCard(i)) {
//                            if (!isAirMode) {
                            	log("GNContactsUtils.OP_PHONE mSimState[" + i + "] = " + mSimState[i]);
//                            	if (mSimState[i] != GNContactsUtils.SIM_STATE_READY) {
                                    handleNoSim(i);
//                            	}
//                            }
//                        }
                    }
                    break;

                case GNContactsUtils.OP_SIM:
                    final int state = args.getInt(SimContactsService.SIM_STATE);
                    if (ContactsApplication.isMultiSimEnabled) {
                        int subscription = args.getInt(GNContactsUtils.SUB, -1);
                        if (subscription == -1) {
                            break;
                        }

                        if (state != mSimState[subscription]) {
                            log(" new sim state is "
                                    + (state == 1 ? "Ready" : "Not Ready")
                                    + " at sub: "
                                    + subscription
                                    + ", original sim state is "
                                    + (mSimState[subscription] == 1 ? "Ready"
                                            : "Not Ready"));

                            mSimState[subscription] = state;

                            if (state == GNContactsUtils.SIM_STATE_READY
                                    && !isSimOperationInprocess[subscription]) {
                                handleSimOp(subscription);
                            } else if (state == GNContactsUtils.SIM_STATE_ERROR) {
                                handleNoSim(subscription);
                            }
                        }
                    } else {
                        if (state != mSimState[DEFAULT_SUB]) {
                            log(" new sim state is "
                                    + (state == 1 ? "Ready" : "Not Ready")
                                    + ", original sim state is "
                                    + (mSimState[DEFAULT_SUB] == 1 ? "Ready"
                                            : "Not Ready"));
                            mSimState[DEFAULT_SUB] = state;
                            if (state == GNContactsUtils.SIM_STATE_READY) {
                                handleSimOp();
                            } else if (state == GNContactsUtils.SIM_STATE_ERROR) {
                                handleNoSim(DEFAULT_SUB);
                            }
                        }
                    }
                    break;

                case GNContactsUtils.OP_SIM_REFRESH:
                    if (ContactsApplication.isMultiSimEnabled) {
                        int subscription = args.getInt(GNContactsUtils.SUB, -1);
                        if (mSimState[subscription] == GNContactsUtils.SIM_STATE_READY
                                && !isSimOperationInprocess[subscription]) {
                            log("refresh sim op");
                            handleSimOp(subscription);
                        } else {
                            log("queue refresh sim op");
                            refreshQueue.put(subscription,
                                    GNContactsUtils.OP_SIM_REFRESH);
                        }
                    }
                    break;

                case GNContactsUtils.OP_SIM_DELETE:
                    if (ContactsApplication.isMultiSimEnabled) {
                        deleteDatabaseSimContacts(SUB1);
                        sendPhbLoadFinished(SUB1);
                        deleteDatabaseSimContacts(SUB2);
                        sendPhbLoadFinished(SUB2);
                    } else {
                        deleteDatabaseSimContacts();
                    }
                    break;
                    
                case SUB1 + FINISH_IMPORTING:
                    sendPhbLoadFinished(SUB1);
                    break;
                
                case SUB2 + FINISH_IMPORTING:
                    sendPhbLoadFinished(SUB2);
                    break;
                }
            }
        };

        for (int i = 0; i < mPhoneNumber; i++) {
            mQuerySimHandler[i] = new QuerySimHandler(
                    mContext.getContentResolver(), i);
            mQueryDatabaseHandler[i] = new QueryDatabaseHandler(
                    mContext.getContentResolver(), i);
            isNewCard[i] = true;
            isSimOperationInprocess[i] = false;
            mSimState[i] = GNContactsUtils.SIM_STATE_NOT_READY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bind. Action: " + intent.getAction());
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "service onStartCommand!");
        if (intent == null) {
            Log.d(TAG, "service onStart! intent is null");
            return Service.START_NOT_STICKY;
        }

        Bundle args = intent.getExtras();
        if (args == null) {
            Log.d(TAG, "service onStart! args is null");
            return Service.START_NOT_STICKY;
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.what = args.getInt(SimContactsService.OPERATION, -1);
        msg.obj = args;
        mServiceHandler.sendMessage(msg);
        return Service.START_REDELIVER_INTENT;
	}
    
    private void handleSimOp() {
        log("handleSimOp()");
        compareAndSaveIccid(DEFAULT_SUB);
        if (isNewCardInserted(DEFAULT_SUB)) {
            deleteDatabaseSimContacts();
            querySimContacts();
        } else {
            querySimContacts();
        }
    }

    private void handleSimOp(int subscription) {
        log("handleSimOp() at sub " + subscription);
        compareAndSaveIccid(subscription);
        isSimOperationInprocess[subscription] = true;

        if (isNewCardInserted(subscription)) {
            log("This is a new card at sub: " + subscription);
            deleteDatabaseSimContacts(subscription);
            querySimContacts(subscription);
        } else {
            querySimContacts(subscription);
        }
    }

    private void handleNoSim(int subscription) {
        log("handle no sim on sub : " + subscription);
        if (ContactsApplication.isMultiSimEnabled) {
            deleteDatabaseSimContacts(subscription);
        } else {
            deleteDatabaseSimContacts();
        }
    }

    private void compareAndSaveIccid(int subscription) {
        isNewCard[subscription] = true;
//        String oldImsi = mPrefs.getString(IMSI[subscription], "");
//        String newImsi = GnTelephonyManagerEx.getDefault().getSubscriberId(
//                subscription);
//        if (GNContactsUtils.isMultiSimEnabled()) {
//            newImsi = GnTelephonyManagerEx.getDefault().getSubscriberId(
//                  subscription);
//        } else {
//            newImsi = mTelephonyManager.getSubscriberId();
//        }
//
//        log("newImsi = " + newImsi + "  oldImsi = " + oldImsi + "   oldImsi.equals(newImsi) = " + oldImsi.equals(newImsi));
//        if (!oldImsi.equals(newImsi)) {
//            Editor editor = mPrefs.edit();
//            editor.putString(IMSI[subscription], newImsi);
//            editor.apply();
//            isNewCard[subscription] = true;
//        } else {
//            isNewCard[subscription] = false;
//        }
    }

    private boolean isNewCardInserted(int subscription) {
        return isNewCard[subscription];
    }

    private void querySimContacts() {
        Intent intent = new Intent();
        intent.setData(Uri.parse("content://icc/adn"));
        Uri uri = intent.getData();
        log("querySimContacts: starting an async query");
        mIsImporting = true;
        
//        boolean deviceProIuni = ContactsUtils.mIsIUNIDeviceOnly;
//        log("deviceProIuni = " + deviceProIuni);
//        if (!deviceProIuni) {
//            try {
//                Thread.sleep(8000);
//              } catch (Exception e) {
//                e.printStackTrace();
//              }
//        }
        
        mQuerySimHandler[DEFAULT_SUB].cancelOperation(QUERY_TOKEN);
        mQuerySimHandler[DEFAULT_SUB].startQuery(QUERY_TOKEN, null, uri,
                COLUMN_NAMES, null, null, null);
    }

    private void querySimContacts(int subscription) {
        Intent intent = new Intent();
        if (subscription != SUB1 && subscription != SUB2) {
            isSimOperationInprocess[subscription] = false;
            sendPendingSimRefreshUpdateMsg(subscription);
            return;
        }

        if (subscription == SUB1) {
            intent.setData(Uri.parse("content://iccmsim/adn"));
        } else if (subscription == SUB2) {
            intent.setData(Uri.parse("content://iccmsim/adn_sub2"));
        }

        Uri uri = intent.getData();
        if (DBG)
            log("querySimContacts: starting an async query");
        mQuerySimHandler[subscription].cancelOperation(QUERY_TOKEN);
        mQuerySimHandler[subscription].startQuery(QUERY_TOKEN, null, uri,
                COLUMN_NAMES, null, null, null);
    }

    private void queryDatabaseSimContactsID() {
        Uri uri = RawContacts.CONTENT_URI;
        String orderBy = "_ID asc";
        log("queryDatabaseSimContacts: starting an async query");
        mQueryDatabaseHandler[0].startQuery(QUERY_TOKEN_DATABASE, null, uri,
                CONTACTS_ID_PROJECTION, SIM_DATABASE_SELECTION,
                SIM_DATABASE_SELECTARGS, orderBy);
    }

    private void queryDatabaseSimContactsID(int subscription) {
        Uri uri = RawContacts.CONTENT_URI;
        String orderBy = "_ID asc";
        log("queryDatabaseSimContacts: starting an async query");
        if (subscription == 0) {
            mQueryDatabaseHandler[subscription].startQuery(
                    QUERY_TOKEN_DATABASE_SUB1, null, uri,
                    CONTACTS_ID_PROJECTION, SIM_DATABASE_SELECTION,
                    SIM_DATABASE_SELECTARGS_SUB1, orderBy);
        } else if (subscription == 1) {
            mQueryDatabaseHandler[subscription].startQuery(
                    QUERY_TOKEN_DATABASE_SUB2, null, uri,
                    CONTACTS_ID_PROJECTION, SIM_DATABASE_SELECTION,
                    SIM_DATABASE_SELECTARGS_SUB2, orderBy);
        }
    }

    private void deleteDatabaseSimContacts() {
        Uri uri = GnContactsContract.RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter("sim", "true").build();
        int simCount = 0;
        StringBuilder simContactsRawContactIds = new StringBuilder("(");
        Cursor simCursor = getApplicationContext().getContentResolver().query(
                uri, new String[]{"_id"}, SIM_DATABASE_SELECTION, SIM_DATABASE_SELECTARGS, null);
        if (simCursor != null) {
            simCount = simCursor.getCount();
            Log.d(TAG, "local db simCount = " + simCount);
            
            if (simCursor.moveToFirst()) {
                int index = 0;
                do {
                    index++;
                    if (index > 300) {
                        index = 0;
                        simContactsRawContactIds.deleteCharAt(simContactsRawContactIds.length() - 1);
                        simContactsRawContactIds.append(")");
                        
                        mQueryDatabaseHandler[0].startDelete(DELETE_TOKEN, null, uri,
                                SIM_DATABASE_DELTETE_SELECTION + simContactsRawContactIds.toString(), null);
                        simContactsRawContactIds.delete(1, simContactsRawContactIds.length());
                        Log.d(TAG, "startDelete = ");
                    }
                    
                    String id = simCursor.getString(0);
                    simContactsRawContactIds.append(id);
                    simContactsRawContactIds.append(",");
                } while (simCursor.moveToNext());
                
                Log.d(TAG, "index = " + index);
                if (index > 0 && simContactsRawContactIds.length() > 1) {
                    simContactsRawContactIds.deleteCharAt(simContactsRawContactIds.length() - 1);
                    simContactsRawContactIds.append(")");
                    mQueryDatabaseHandler[0].startDelete(DELETE_TOKEN, null, uri,
                            SIM_DATABASE_DELTETE_SELECTION + simContactsRawContactIds.toString(), null);
                    simContactsRawContactIds.delete(1, simContactsRawContactIds.length());
                }
            }
            
            simCursor.close();
        }
        
//        mQueryDatabaseHandler[0].startDelete(DELETE_TOKEN, null, uri,
//                SIM_DATABASE_SELECTION, SIM_DATABASE_SELECTARGS);

        log("deleteDatabaseSimContacts");
    }

    private void deleteDatabaseSimContacts(int subscription) {
        log("deleteDatabaseSimContacts  subscription = " + subscription);
        Uri uri = GnContactsContract.RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter("sim", "true").build();
        
        int simId = -1;
        String selec = null;
        try {
        	if (mSimState[subscription] != 1) {
        		Thread.sleep(500);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(mContext, subscription);
        if (simInfo != null) {
            simId = (int) simInfo.mSimId;
        }
        
		Cursor cursor = mContext.getContentResolver().query(
				SimInfo.CONTENT_URI,
				new String[] {"_id"}, "slot='-1'", null, null);
		StringBuilder simIds = new StringBuilder("("); 
		try {
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						simIds.append(cursor.getString(0));
						simIds.append(",");
					} while (cursor.moveToNext());
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			
			simIds.append(simId);
			simIds.append(")");
		}
		
		log("simIds.toString() = " + simIds.toString() + "   simId = " + simId);
		
        if (simId > 0) {
            selec = RawContacts.INDICATE_PHONE_SIM + " in " + simIds.toString() + " AND " + RawContacts.DELETED + "=0 AND account_type != 'Local Phone Account'";
        } else if (simId == -1) {
            if (!SimCardUtils.isSimInserted(subscription)) {
            	if (0 == subscription) {
            	    if (SimCardUtils.isSimInserted(1)) {
            	        int simId2 = SIMInfoWrapper.getDefault().getSimIdBySlotId(1);
            	        selec = RawContacts.INDICATE_PHONE_SIM + ">0 AND " + RawContacts.INDICATE_PHONE_SIM + "!=" + simId2 + " AND " + RawContacts.DELETED + "=0";
            	    } else {
            	    	selec = RawContacts.INDICATE_PHONE_SIM + ">0 AND " + RawContacts.DELETED + "=0";
            		}
            	} else if (1 == subscription) {
            		if (SimCardUtils.isSimInserted(0)) {
            		    int simId1 = SIMInfoWrapper.getDefault().getSimIdBySlotId(0);
            		    selec = RawContacts.INDICATE_PHONE_SIM + ">0 AND " + RawContacts.INDICATE_PHONE_SIM + "!=" + simId1 + " AND " + RawContacts.DELETED + "=0";
            	    } else {
            	    	selec = RawContacts.INDICATE_PHONE_SIM + ">0 AND " + RawContacts.DELETED + "=0";
            		}
            	}
            } else {
                return;
            }
        }
        
//        mQueryDatabaseHandler[subscription].startDelete(DELETE_TOKEN, null,
//                uri, selec, null);
        
        int simCount = 0;
        StringBuilder simContactsRawContactIds = new StringBuilder("(");
        Cursor simCursor = getApplicationContext().getContentResolver().query(
                uri, new String[]{"_id"}, selec, null, null);
        if (simCursor != null) {
            simCount = simCursor.getCount();
            Log.d(TAG, "local db simCount = " + simCount);
            
            if (simCursor.moveToFirst()) {
                int index = 0;
                do {
                    index++;
                    if (index > 300) {
                        index = 0;
                        simContactsRawContactIds.deleteCharAt(simContactsRawContactIds.length() - 1);
                        simContactsRawContactIds.append(")");
                        
                        mQueryDatabaseHandler[0].startDelete(DELETE_TOKEN, null, uri,
                                SIM_DATABASE_DELTETE_SELECTION + simContactsRawContactIds.toString(), null);
                        simContactsRawContactIds.delete(1, simContactsRawContactIds.length());
                        Log.d(TAG, "startDelete = ");
                    }
                    
                    String id = simCursor.getString(0);
                    simContactsRawContactIds.append(id);
                    simContactsRawContactIds.append(",");
                } while (simCursor.moveToNext());
                
                Log.d(TAG, "index = " + index);
                if (index > 0 && simContactsRawContactIds.length() > 1) {
                    simContactsRawContactIds.deleteCharAt(simContactsRawContactIds.length() - 1);
                    simContactsRawContactIds.append(")");
                    mQueryDatabaseHandler[0].startDelete(DELETE_TOKEN, null, uri,
                            SIM_DATABASE_DELTETE_SELECTION + simContactsRawContactIds.toString(), null);
                    simContactsRawContactIds.delete(1, simContactsRawContactIds.length());
                }
            }
            
            simCursor.close();
        }
    }

    private void addAllSimContactsIntoDatabase(int subscription) {
        ImportAllSimContactsThread thread = new ImportAllSimContactsThread(
                subscription);
        thread.start();
    }

    private void addAllSimContactsIntoDatabase() {
        ImportAllSimContactsThread thread = new ImportAllSimContactsThread();
        thread.start();
    }

    protected class QuerySimHandler extends AsyncQueryHandler {
        private int mSubscription = 0;

        public QuerySimHandler(ContentResolver cr, int subscription) {
            super(cr);
            mSubscription = subscription;
        }

        public QuerySimHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            //aurora add liguangyu 20140918 for phb start
    		sendPhbLoadEnd(mSubscription);
    	    //aurora add liguangyu 20140918 for phb end
            if (c == null) {
                log(" QuerySimHandler onQueryComplete: cursor is null");
                isSimOperationInprocess[mSubscription] = false;
                sendPendingSimRefreshUpdateMsg(mSubscription);
                return;
            }
            log(" QuerySimHandler onQueryComplete: cursor.count="
                    + c.getCount());

            mSimCursor[mSubscription] = c;
            log("isNewCardInserted(" + mSubscription + ") "
                    + isNewCardInserted(mSubscription));
            if (isNewCardInserted(mSubscription)) {
                if (ContactsApplication.isMultiSimEnabled) {
                    addAllSimContactsIntoDatabase(mSubscription);
                } else {
                    addAllSimContactsIntoDatabase();
                }
            } else {
                if (ContactsApplication.isMultiSimEnabled) {
                    queryDatabaseSimContactsID(mSubscription);
                } else {
                    queryDatabaseSimContactsID();
                }
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
        }
        
        public void startQuery(int token, Object cookie, Uri uri,
                String[] projection, String selection, String[] selectionArgs,
                String orderBy) {
            //aurora add liguangyu 20140918 for phb start
    		sendPhbLoadStart(mSubscription);
    	    //aurora add liguangyu 20140918 for phb end
    		super.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
        }
    }

    private void UpdateSimDatabaseInPhone(int subscription) {
        UpdateContactsThread thread = new UpdateContactsThread(subscription);
        thread.start();
    }

    private class QueryDatabaseHandler extends AsyncQueryHandler {
        private int mSubscription = 0;

        public QueryDatabaseHandler(ContentResolver cr, int subscription) {
            super(cr);
            mSubscription = subscription;
        }

        public QueryDatabaseHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            if (c == null) {
                log(" QueryDatabaseHandler onQueryComplete: cursor is null");
                isSimOperationInprocess[mSubscription] = false;
                sendPendingSimRefreshUpdateMsg(mSubscription);
                return;
            }
            log(" QueryDatabaseHandler onQueryComplete: cursor.count="
                    + c.getCount() + "  sub is " + mSubscription);
            mDatabaseCursor[mSubscription] = c;
            UpdateSimDatabaseInPhone(mSubscription);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            log("Complete delete database sim contacts");
        }
    }

    protected static void log(String msg) {
        if (DBG)
            Log.d(TAG, msg);
    }

    private class ImportAllSimContactsThread extends Thread {
        private int mSubscription = 0;

        public ImportAllSimContactsThread(int subscription) {
            super("ImportAllSimContactsThread");
            mSubscription = subscription;
        }

        public ImportAllSimContactsThread() {
            super("ImportAllSimContactsThread");
        }

        @Override
        public void run() {
            synchronized (this) {
            	try {
                    final ContentValues emptyContentValues = new ContentValues();
                    final ContentResolver resolver = mContext.getContentResolver();

                    log("import sim contact to sub_id: " + mSubscription);
                    log("mSimCursor[" + mSubscription + "].getCount() "
                            + mSimCursor[mSubscription].getCount());
                    mSimCursor[mSubscription].moveToPosition(-1);
                    /*while (mSimCursor[mSubscription].moveToNext()) {
                        actuallyImportOneSimContact(mSimCursor[mSubscription],
                                resolver, mSubscription);
                    }*/
                    
                    if (!ContactsUtils.mIsIUNIDeviceOnly) {
                    	mNamesArray.clear();
                    	mPhoneNumberArray.clear();
                    }
                    actuallyImportBatchSimContact(mSimCursor[mSubscription],
                            resolver, mSubscription);
                    mSimCursor[mSubscription].close();

                    isSimOperationInprocess[mSubscription] = false;
                    sendPendingSimRefreshUpdateMsg(mSubscription);
                    
                    Message msg = mServiceHandler.obtainMessage(mSubscription + FINISH_IMPORTING);
                    msg.sendToTarget();
            	} catch (StaleDataException e) {
            		Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            	}
            }
        }
    }

    private static void actuallyImportOneSimContact(final Cursor cursor,
            final ContentResolver resolver, int sub_id) {

        log("actuallyImportOneSimContact");
        String accountName = GNContactsUtils.getSimAccountNameBySlot(sub_id);
        String accountType = GNContactsUtils.getSimAccountTypeBySlot(sub_id);

        
        final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
                cursor.getString(NAME_COLUMN));
        String name = namePhoneTypePair.name;
        String phoneNumber = cursor.getString(NUMBER_COLUMN);
        final String emailAddresses = null;//cursor.getString(EMAILS_COLUMN);
        final String anrs = null;//cursor.getString(ANRS_COLUMN);
        String index = null;//cursor.getString(INDEX_COLUMN);
        if (index != null) {
            index = String.valueOf(Integer.valueOf(501) + 1);
        } else {
            index = String.valueOf(502);
        }
        
        if (name == null && phoneNumber == null) {
            return;
        }
        
        if ((name != null && name.isEmpty()) && (phoneNumber != null && phoneNumber.isEmpty())) {
            return;
        }
        
        if (name == null) {
            name = "";
        }
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        
        log("name: " + name + "  number: " + phoneNumber + "  anrs: " + anrs
                + "  email: " + emailAddresses + "  index: " + index);
//        final String[] emailAddressArray;
//        final String[] anrArray;
//        if (!TextUtils.isEmpty(emailAddresses)) {
//            emailAddressArray = emailAddresses.split(",");
//        } else {
//            emailAddressArray = null;
//        }
//        if (!TextUtils.isEmpty(anrs)) {
//            anrArray = anrs.split(",");
//        } else {
//            anrArray = null;
//        }

        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        builder.withValue(RawContacts.AGGREGATION_MODE,
                RawContacts.AGGREGATION_MODE_DISABLED);
        builder.withValue(RawContacts.ACCOUNT_NAME, accountName);
        builder.withValue(RawContacts.ACCOUNT_TYPE, accountType);
        builder.withValue(RawContacts.INDICATE_PHONE_SIM, sub_id + 1);
        if (index != null) {
            builder.withValue(RawContacts.INDEX_IN_SIM, index);
        }
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(StructuredName.DISPLAY_NAME, name);
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builder.withValue(Phone.NUMBER, phoneNumber);
        builder.withValue(Data.DATA2, 2);
        // builder.withValue(Data.IS_PRIMARY, 1);
        operationList.add(builder.build());

//        if (anrArray != null) {
//            for (String anr : anrArray) {
//                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
//                builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
//                builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//                builder.withValue(Data.DATA2, 7);
//                builder.withValue(Phone.NUMBER, anr);
//                builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
//                operationList.add(builder.build());
//            }
//        }
//
//        if (emailAddresses != null) {
//            for (String emailAddress : emailAddressArray) {
//                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
//                builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
//                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
//                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
//                builder.withValue(Email.ADDRESS, emailAddress);
//                operationList.add(builder.build());
//            }
//        }

        try {
            resolver.applyBatch(GnContactsContract.AUTHORITY, operationList);
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        }
    }

    private class UpdateContactsThread extends Thread {
        private int mSubscription = 0;
        private SimContactsOperation mSimContactsOperation;

        public UpdateContactsThread(int subscription) {
            super("LoadSimContactsInPhone");
            mSubscription = subscription;
            mSimContactsOperation = new SimContactsOperation(mContext);
        }

        @Override
        public void run() {
            synchronized (this) {
            	try {
                    final ContentResolver resolver = mContext.getContentResolver();
                    ContentValues mAfter = new ContentValues();
                    mDatabaseCursor[mSubscription].moveToPosition(-1);
                    mSimCursor[mSubscription].moveToPosition(-1);

                    while ((!mSimCursor[mSubscription].isLast())
                            && (!mDatabaseCursor[mSubscription].isLast())
                            && mSimCursor[mSubscription].moveToNext()
                            && mDatabaseCursor[mSubscription].moveToNext()) {

                        final long contactId = mDatabaseCursor[mSubscription]
                                .getLong(CONTACT_ID_COLUMN_COLUMN);
                        final long rawContactId = mDatabaseCursor[mSubscription]
                                .getLong(RAW_CONTACT_ID_COLUMN);
                        ContentValues mBefore = mSimContactsOperation
                                .getSimAccountValues(contactId);
                        
                        final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
                                mSimCursor[mSubscription].getString(NAME_COLUMN));
                        final String simName = namePhoneTypePair.name;
                        final String simNumber = mSimCursor[mSubscription]
                                .getString(NUMBER_COLUMN);
//                        final String simEmails = mSimCursor[mSubscription]
//                                .getString(EMAILS_COLUMN);
//                        final String simAnrs = mSimCursor[mSubscription]
//                                .getString(ANRS_COLUMN);
//                        final String simIndex = mSimCursor[mSubscription]
//                                .getString(INDEX_COLUMN);
//                        final String[] simEmailArray = simEmails == null ? null
//                                : simEmails.split(",");
//                        final String[] simAnrArray = simAnrs == null ? null
//                                : simAnrs.split(",");

                        mAfter.clear();
                        mAfter.put(GNContactsUtils.STR_TAG, simName);
                        mAfter.put(GNContactsUtils.STR_NUMBER, simNumber);
//                        if (GNContactsUtils.cardIsUsim(mSubscription)) {
//                            if (simAnrArray != null && simAnrArray.length > 0) {
//                                mAfter.put(GNContactsUtils.STR_ANRS, simAnrArray[0]);
//                            } else {
//                                mAfter.putNull(GNContactsUtils.STR_ANRS);
//                            }
//                            if (simEmailArray != null && simEmailArray.length > 0) {
//                                mAfter.put(GNContactsUtils.STR_EMAILS,
//                                        simEmailArray[0]);
//                            } else {
//                                mAfter.putNull(GNContactsUtils.STR_EMAILS);
//                            }
//                        }
                        mAfter.put(GNContactsUtils.STR_INDEX,
                                String.valueOf(502));

                        log(" UpdateContactsThread mAfter is : " + mAfter
                                + " mBefore is: " + mBefore + " rawContactId is: "
                                + rawContactId + "    mBefore.equals(mAfter) = " + mBefore.equals(mAfter));

                        if (!mBefore.equals(mAfter)) {
                            actuallyUpdateOneSimContact(resolver, mBefore, mAfter,
                                    rawContactId);
                        }
                    }

                    while (mDatabaseCursor[mSubscription].moveToNext()) {
                        deleteOneSimContactFromDatabase(
                                mDatabaseCursor[mSubscription], resolver,
                                mSubscription);
                    }

                    if (mDatabaseCursor[mSubscription].getCount() == 0) {
                        mSimCursor[mSubscription].moveToPosition(-1);
                    }

                    /*while (mSimCursor[mSubscription].moveToNext()) {
                        actuallyImportOneSimContact(mSimCursor[mSubscription],
                                resolver, mSubscription);
                    }*/
                    actuallyImportBatchSimContact(mSimCursor[mSubscription],
                            resolver, mSubscription);

                    mSimCursor[mSubscription].close();
                    mDatabaseCursor[mSubscription].close();

                    isSimOperationInprocess[mSubscription] = false;
                    sendPendingSimRefreshUpdateMsg(mSubscription);
                    
                    Message msg = mServiceHandler.obtainMessage(mSubscription + FINISH_IMPORTING);
                    msg.sendToTarget();
            	} catch (StaleDataException e) {
            		Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            	}  catch (IllegalStateException e) {
            	    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            	}
            }
        }
    }

    private void sendPendingSimRefreshUpdateMsg(int subscription) {
        if (refreshQueue.get(subscription) != null) {
            log("send refresh op msg since it is in refreshQueue at sub "
                    + subscription);
            Bundle args = new Bundle();
            args.putInt(GNContactsUtils.SUB, subscription);
            Message msg = mServiceHandler.obtainMessage();
            msg.what = refreshQueue.get(subscription);
            msg.obj = args;
            mServiceHandler.sendMessage(msg);
            refreshQueue.put(subscription, null);
        }
    }

    private void actuallyUpdateOneSimContact(final ContentResolver resolver,
            final ContentValues before, final ContentValues after,
            long rawContactId) {

        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder;
        builder = buildDiff(before, after, GNContactsUtils.STR_TAG,
                rawContactId);
        if (builder != null) {
            operationList.add(builder.build());
        }
        builder = buildDiff(before, after, GNContactsUtils.STR_NUMBER,
                rawContactId);
        if (builder != null) {
            operationList.add(builder.build());
        }
        builder = buildDiff(before, after, GNContactsUtils.STR_ANRS,
                rawContactId);
        if (builder != null) {
            operationList.add(builder.build());
        }
        builder = buildDiff(before, after, GNContactsUtils.STR_EMAILS,
                rawContactId);
        if (builder != null) {
            operationList.add(builder.build());
        }
        
        //Gionee <wangth><2013-04-17> add for CR00797545 begin
        builder = buildDiff(before, after, GNContactsUtils.STR_INDEX,
                rawContactId);
        if (builder != null) {
            operationList.add(builder.build());
        }
        
        if (operationList.isEmpty()) {
            return;
        }
        //Gionee <wangth><2013-04-17> add for CR00797545 end

        log(" actuallyUpdateOneSimContact : update new values "
                + after.toString());
        
        try {
            resolver.applyBatch(GnContactsContract.AUTHORITY, operationList);
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        }
    }

    private boolean isDeleted(final ContentValues before,
            final ContentValues after, String key) {
        return !TextUtils.isEmpty(before.getAsString(key))
                && TextUtils.isEmpty(after.getAsString(key));
    }

    private boolean isInserted(final ContentValues before,
            final ContentValues after, String key) {
        return TextUtils.isEmpty(before.getAsString(key))
                && !TextUtils.isEmpty(after.getAsString(key));
    }

    private boolean isUpdated(final ContentValues before,
            final ContentValues after, String key) {
        return before.getAsString(key) != null
                && after.getAsString(key) != null
                && !before.getAsString(key).equals(after.getAsString(key));
    }

    private Builder buildUpdatedName(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
        String nameSelection = StructuredName.RAW_CONTACT_ID + "=? AND "
                + Data.MIMETYPE + "=?";
        String[] nameSelectionArg = new String[] {
                String.valueOf(rawContactId), StructuredName.CONTENT_ITEM_TYPE };
        builder.withSelection(nameSelection, nameSelectionArg);
        builder.withValue(StructuredName.GIVEN_NAME, null);
        builder.withValue(StructuredName.FAMILY_NAME, null);
        builder.withValue(StructuredName.PREFIX, null);
        builder.withValue(StructuredName.MIDDLE_NAME, null);
        builder.withValue(StructuredName.SUFFIX, null);
        builder.withValue(StructuredName.DISPLAY_NAME,
                after.getAsString(GNContactsUtils.STR_TAG));
        return builder;
    }

    private Builder buildDeletedName(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
        String nameSelection = StructuredName.RAW_CONTACT_ID + "=? AND "
                + Data.MIMETYPE + "=?";
        String[] nameSelectionArg = new String[] {
                String.valueOf(rawContactId), StructuredName.CONTENT_ITEM_TYPE };
        builder.withSelection(nameSelection, nameSelectionArg);
        return builder;
    }

    private Builder buildInsertedName(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        String nameSelection = StructuredName.RAW_CONTACT_ID + "=? AND "
                + Data.MIMETYPE + "=?";
        builder.withValue(StructuredName.RAW_CONTACT_ID, rawContactId);
        builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(StructuredName.DISPLAY_NAME,
                after.getAsString(GNContactsUtils.STR_TAG));
        return builder;
    }

    private Builder buildUpdatedNumber(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
        String selection = Phone.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
                + "=? AND " + Phone.TYPE + "=?";
        String[] selectionArg = new String[] { String.valueOf(rawContactId),
                Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_MOBILE) };
        builder.withSelection(selection, selectionArg);
        builder.withValue(Phone.TYPE, Phone.TYPE_MOBILE);
        builder.withValue(Data.IS_PRIMARY, 1);
        builder.withValue(Phone.NUMBER,
                after.getAsString(GNContactsUtils.STR_NUMBER));
        return builder;
    }

    private Builder buildDeletedNumber(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
        String selection = Phone.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
                + "=? AND " + Phone.TYPE + "=?";
        String[] selectionArg = new String[] { String.valueOf(rawContactId),
                Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_MOBILE) };
        builder.withSelection(selection, selectionArg);
        return builder;
    }

    private Builder buildInsertedNumber(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValue(Phone.RAW_CONTACT_ID, rawContactId);
        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builder.withValue(Phone.TYPE, Phone.TYPE_MOBILE);
        builder.withValue(Data.IS_PRIMARY, 1);
        builder.withValue(Phone.NUMBER,
                after.getAsString(GNContactsUtils.STR_NUMBER));
        return builder;
    }

    private Builder buildUpdatedAnr(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
        String selection = Phone.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
                + "=? AND " + Phone.TYPE + "=?";
        String[] selectionArg = new String[] { String.valueOf(rawContactId),
                Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_HOME) };
        builder.withSelection(selection, selectionArg);
        builder.withValue(Phone.TYPE, Phone.TYPE_HOME);
        builder.withValue(Phone.NUMBER,
                after.getAsString(GNContactsUtils.STR_ANRS));
        return builder;
    }

    private Builder buildDeletedAnr(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
        String selection = Phone.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
                + "=? AND " + Phone.TYPE + "=?";
        String[] selectionArg = new String[] { String.valueOf(rawContactId),
                Phone.CONTENT_ITEM_TYPE, String.valueOf(Phone.TYPE_HOME) };
        builder.withSelection(selection, selectionArg);
        return builder;
    }

    private Builder buildInsertedAnr(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValue(Phone.RAW_CONTACT_ID, rawContactId);
        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builder.withValue(Phone.TYPE, Phone.TYPE_HOME);
        builder.withValue(Phone.NUMBER,
                after.getAsString(GNContactsUtils.STR_ANRS));
        return builder;
    }

    private Builder buildUpdatedEmail(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
        String selection = Email.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
                + "=?";
        String[] selectionArg = new String[] { String.valueOf(rawContactId),
                Email.CONTENT_ITEM_TYPE };
        builder.withSelection(selection, selectionArg);
        builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
        builder.withValue(Email.ADDRESS,
                after.getAsString(GNContactsUtils.STR_EMAILS));
        return builder;
    }

    private Builder buildDeletedEmail(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
        String selection = Email.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
                + "=?";
        String[] selectionArg = new String[] { String.valueOf(rawContactId),
                Email.CONTENT_ITEM_TYPE };
        builder.withSelection(selection, selectionArg);
        return builder;
    }

    private Builder buildInsertedEmail(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValue(Email.RAW_CONTACT_ID, rawContactId);
        builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
        builder.withValue(Email.ADDRESS,
                after.getAsString(GNContactsUtils.STR_EMAILS));
        return builder;
    }
    
    //Gionee <wangth><2013-04-17> add for CR00797545 begin
    private Builder buildUpdatedIndex(final ContentValues after,
            final long rawContactId) {
        Builder builder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
        String selection = RawContacts._ID + "=?";
        String[] selectionArg = new String[] {String.valueOf(rawContactId)};
        builder.withSelection(selection, selectionArg);
        builder.withValue(RawContacts.INDEX_IN_SIM,
                after.getAsString(GNContactsUtils.STR_INDEX));
        return builder;
    }
    //Gionee <wangth><2013-04-17> add for CR00797545 end

    public ContentProviderOperation.Builder buildDiff(ContentValues before,
            ContentValues after, String key, long rawContactId) {
        Builder builder = null;
        if (isInserted(before, after, key)) {
            if (GNContactsUtils.STR_TAG.equals(key)) {
                builder = buildInsertedName(after, rawContactId);
            } else if (GNContactsUtils.STR_NUMBER.equals(key)) {
                builder = buildInsertedNumber(after, rawContactId);
            } else if (GNContactsUtils.STR_ANRS.equals(key)) {
                builder = buildInsertedAnr(after, rawContactId);
            } else if (GNContactsUtils.STR_EMAILS.equals(key)) {
                builder = buildInsertedEmail(after, rawContactId);
            }
        } else if (isDeleted(before, after, key)) {
            if (GNContactsUtils.STR_TAG.equals(key)) {
                builder = buildDeletedName(after, rawContactId);
            } else if (GNContactsUtils.STR_NUMBER.equals(key)) {
                builder = buildDeletedNumber(after, rawContactId);
            } else if (GNContactsUtils.STR_ANRS.equals(key)) {
                builder = buildDeletedAnr(after, rawContactId);
            } else if (GNContactsUtils.STR_EMAILS.equals(key)) {
                builder = buildDeletedEmail(after, rawContactId);
            }
        } else if (isUpdated(before, after, key)) {
            if (GNContactsUtils.STR_TAG.equals(key)) {
                builder = buildUpdatedName(after, rawContactId);
            } else if (GNContactsUtils.STR_NUMBER.equals(key)) {
                builder = buildUpdatedNumber(after, rawContactId);
            } else if (GNContactsUtils.STR_ANRS.equals(key)) {
                builder = buildUpdatedAnr(after, rawContactId);
            } else if (GNContactsUtils.STR_EMAILS.equals(key)) {
                builder = buildUpdatedEmail(after, rawContactId);
            }
            //Gionee <wangth><2013-04-17> add for CR00797545 begin
            else if (GNContactsUtils.STR_INDEX.equals(key)) {
                builder = buildUpdatedIndex(after, rawContactId);
            }
            //Gionee <wangth><2013-04-17> add for CR00797545 end
        }
        return builder;
    }

    private void deleteOneSimContactFromDatabase(final Cursor cursor,
            final ContentResolver resolver, int subscription) {

        String id;
        id = String.valueOf(mDatabaseCursor[subscription]
                .getLong(CONTACT_ID_COLUMN_COLUMN));
        Uri uri = Uri.withAppendedPath(Contacts.CONTENT_URI, id);
        log("delete uri is " + uri);
        resolver.delete(uri, null, null);
    }
    
    private void sendPhbLoadFinished(int subId) {
        Intent intent = new Intent(ACTION_PHB_LOAD_FINISHED);
        intent.putExtra("simId", subId);
        intent.putExtra("slotId", subId);
        mContext.sendBroadcast(intent);
    }
    
    private static class NamePhoneTypePair {
        public String name;
        public int phoneType;
        public String phoneTypeSuffix;
        public NamePhoneTypePair(String nameWithPhoneType) {
            if (nameWithPhoneType == null) {
                return;
            }
            // Look for /W /H /M or /O at the end of the name signifying the type
            int nameLen = nameWithPhoneType.length();
            if (nameLen - 2 >= 0 && nameWithPhoneType.charAt(nameLen - 2) == '/') {
                char c = Character.toUpperCase(nameWithPhoneType.charAt(nameLen - 1));
                phoneTypeSuffix = String.valueOf(nameWithPhoneType.charAt(nameLen - 1));
                if (c == 'W') {
                    phoneType = Phone.TYPE_WORK;
                } else if (c == 'M' || c == 'O') {
                    phoneType = Phone.TYPE_MOBILE;
                } else if (c == 'H') {
                    phoneType = Phone.TYPE_HOME;
                } else {
                    phoneType = Phone.TYPE_OTHER;
                }
                name = nameWithPhoneType.substring(0, nameLen - 2);
            } else {
                phoneTypeSuffix = "";
                phoneType = Phone.TYPE_OTHER;
                name = nameWithPhoneType;
            }
        }
    }
    
    private static void actuallyImportBatchSimContact(final Cursor cursor,
            final ContentResolver resolver, int sub_id) {

        log("actuallyImportBatchSimContact");
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        int i = 0;
        
        if (cursor != null && cursor.getCount() > 0) {
        	while (!cursor.isLast() && cursor.moveToNext()) {
        		int j = 0;
            	String accountName = GNContactsUtils.getSimAccountNameBySlot(sub_id);
                String accountType = GNContactsUtils.getSimAccountTypeBySlot(sub_id);

                final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
                        cursor.getString(NAME_COLUMN));
                String name = namePhoneTypePair.name;
                String phoneNumber = cursor.getString(NUMBER_COLUMN);
                final String emailAddresses = null;//cursor.getString(EMAILS_COLUMN);
                final String anrs = null;//cursor.getString(ANRS_COLUMN);
                String index = null;//cursor.getString(INDEX_COLUMN);
                if (index != null) {
                    index = String.valueOf(Integer.valueOf(index) + 1);
                } else {
                    index = String.valueOf(502);
                }
                
                if (name == null && phoneNumber == null) {
                    continue;
                }
                
                if ((name != null && name.isEmpty()) && (phoneNumber != null && phoneNumber.isEmpty())) {
                    continue;
                }
                
                if (name == null) {
                    name = "";
                }
                if (phoneNumber == null) {
                    phoneNumber = "";
                }
                
                log("name: " + name + "  number: " + phoneNumber + "  anrs: " + anrs
                        + "  email: " + emailAddresses + "  index: " + index+"  accountName->"+accountName+"  accountType->"+accountType);
//                final String[] emailAddressArray;
//                final String[] anrArray;
//                if (!TextUtils.isEmpty(emailAddresses)) {
//                    emailAddressArray = emailAddresses.split(",");
//                } else {
//                    emailAddressArray = null;
//                }
//                if (!TextUtils.isEmpty(anrs)) {
//                    anrArray = anrs.split(",");
//                } else {
//                    anrArray = null;
//                }
                
                if (!ContactsUtils.mIsIUNIDeviceOnly) {
                	mNamesArray.add(name);
                	mPhoneNumberArray.add(phoneNumber);
                	
                	i++;
                	if (i >= cursor.getCount()) {
                		sendImportCompletedBroast();
                		break;
                	}
                	
                	continue;
                }

                int simId = -1;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
//                    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(mContext, sub_id);
//                    if (simInfo != null) {
//                        simId = (int) simInfo.mSimId;
//                    }
                	simId =  SIMInfoWrapper.getDefault().getSimIdBySlotId(sub_id);
                } else {
                    simId = 1;
                }
                
                if (simId < 1) {
                    return;
                }

                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(RawContacts.CONTENT_URI);
                builder.withValue(RawContacts.AGGREGATION_MODE,
                        RawContacts.AGGREGATION_MODE_DISABLED);
                builder.withValue(RawContacts.ACCOUNT_NAME, accountName);
                builder.withValue(RawContacts.ACCOUNT_TYPE, accountType);
                builder.withValue(RawContacts.INDICATE_PHONE_SIM, simId);
                builder.withValue(RawContacts.INDEX_IN_SIM, index);
                operationList.add(builder.build());
                j++;

                if (!TextUtils.isEmpty(name)) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, i);
                    builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                    builder.withValue(StructuredName.DISPLAY_NAME, name);
                    operationList.add(builder.build());
                    j++;
                }

                if (!TextUtils.isEmpty(phoneNumber)) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
                    builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    builder.withValue(Phone.NUMBER, phoneNumber);
                    builder.withValue(Data.DATA2, 2);
                    // builder.withValue(Data.IS_PRIMARY, 1);
                    operationList.add(builder.build());
                    j++;
                }

//                if (anrArray != null) {
//                    for (String anr : anrArray) {
//                        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
//                        builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
//                        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//                        builder.withValue(Data.DATA2, 7);
//                        builder.withValue(Phone.NUMBER, anr);
//                        builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
//                        operationList.add(builder.build());
//                        j++;
//                    }
//                }
//
//                if (emailAddresses != null) {
//                    for (String emailAddress : emailAddressArray) {
//                        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
//                        builder.withValueBackReference(Email.RAW_CONTACT_ID, i);
//                        builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
//                        builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
//                        builder.withValue(Email.ADDRESS, emailAddress);
//                        operationList.add(builder.build());
//                        j++;
//                    }
//                }

                i = i + j;
                if (i > MAX_OP_COUNT_IN_ONE_BATCH) {
                	TelephonyUtils.sleepInCall();
                    try {
                        resolver.applyBatch(GnContactsContract.AUTHORITY, operationList);
                    } catch (RemoteException e) {
                        Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (OperationApplicationException e) {
                        Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
					i = 0;
					operationList.clear();
                }
        	}
            try {
            	if (!operationList.isEmpty()) {
                    resolver.applyBatch(GnContactsContract.AUTHORITY, operationList);
                    operationList.clear();
            	}
            } catch (RemoteException e) {
                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            } catch (OperationApplicationException e) {
                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            }  catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        	sendImportCompletedBroast();
        	return;
        }
        
        mIsImporting = false;
        
        if (ContactsApplication.isMultiSimEnabled) {
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }
    }

    private static void sendImportCompletedBroast() {
    	if (ContactsUtils.mIsIUNIDeviceOnly) {
    		return;
    	}
    	
    	Intent intent = new Intent("aurora.sim.contacts.loaded");
    	intent.putStringArrayListExtra("names", mNamesArray);
    	intent.putStringArrayListExtra("phones", mPhoneNumberArray);
    	mContext.sendBroadcast(intent);
    	
    	mNamesArray.clear();
    	mPhoneNumberArray.clear();
    }
    
    //aurora add liguangyu 20140918 for phb start
    private static final String AURORA_ACTION_PHB_LOAD_START = "com.android.contacts.ACTION_PHB_LOAD_START";
    private static final String AURORA_ACTION_PHB_LOAD_END = "com.android.contacts.ACTION_PHB_LOAD_END";
    private void sendPhbLoadStart(int subId) {
        Intent intent = new Intent(AURORA_ACTION_PHB_LOAD_START);
        intent.putExtra("simId", subId);
        intent.putExtra("slotId", subId);
        mContext.sendBroadcast(intent);
    }
    
    private void sendPhbLoadEnd(int subId) {
        Intent intent = new Intent(AURORA_ACTION_PHB_LOAD_END);
        intent.putExtra("simId", subId);
        intent.putExtra("slotId", subId);
        mContext.sendBroadcast(intent);
    }
    //aurora add liguangyu 20140918 for phb end
}

