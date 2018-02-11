package com.android.providers.contacts;

import com.android.providers.contacts.ContactsDatabaseHelper.NameLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.NameLookupType;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.mediatek.providers.contacts.ContactsFeatureConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract.CommonDataKinds.SipAddress;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.PhoneLookup;
import com.android.providers.contacts.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.providers.contacts.ContactsFeatureConstants.FeatureOption;

public class DataRowHandlerForSipAddress extends DataRowHandlerForCommonDataKind {
	private static final String TAG = "DataRowHandlerForSipAddress";
	private static final boolean DBG = ContactsFeatureConstants.DBG_DIALER_SEARCH;
	private Context mContext;
	private ContactsDatabaseHelper mDbHelper;
	
	
	private SQLiteStatement mCallsNewInsertDataIdUpdate;
	private SQLiteStatement mCallsGetLatestCallLogIdForOneContactQuery;
	private SQLiteStatement mCallsReplaceDataIdUpdate;

	public DataRowHandlerForSipAddress(Context context,
            ContactsDatabaseHelper dbHelper, ContactAggregator aggregator) {
        super(context, dbHelper, aggregator, SipAddress.CONTENT_ITEM_TYPE, SipAddress.TYPE, SipAddress.LABEL);
        mContext = context;
        mDbHelper = dbHelper;
    }

	@Override
	public int delete(SQLiteDatabase db, TransactionContext txContext,Cursor c) {
        long dataId = c.getLong(DataDeleteQuery._ID);
        
		int count = super.delete(db, txContext, c);
		
		if (FeatureOption.MTK_SEARCH_DB_SUPPORT) {
			log("[delete] dataId: " + dataId );
			//For callLog, remove raw_contact_id and data_id
			if (mCallsReplaceDataIdUpdate == null) {
	        	mCallsReplaceDataIdUpdate = db.compileStatement(
	        			"UPDATE " + Tables.CALLS +
	        			" SET " + Calls.DATA_ID + "=?, " + 
	        			Calls.RAW_CONTACT_ID + "=? " + 
	        			" WHERE " + Calls.DATA_ID + " =? ");
			}
			mCallsReplaceDataIdUpdate.bindNull(1);
			mCallsReplaceDataIdUpdate.bindNull(2);
			mCallsReplaceDataIdUpdate.bindLong(3, dataId);
			mCallsReplaceDataIdUpdate.execute();
			log("[delete] Remove raw_contact_id and data_id data in CallLog. ");
		}
		return count;
	}

	@Override
	public long insert(SQLiteDatabase db, TransactionContext txContext,
			long rawContactId, ContentValues values) {
		long dataId;
		if (values.containsKey(SipAddress.SIP_ADDRESS)){
			dataId = super.insert(db, txContext, rawContactId, values);
			if (FeatureOption.MTK_SEARCH_DB_SUPPORT) {
				String sipNumber = values.getAsString(SipAddress.SIP_ADDRESS);
            	// update call Log record, and get the Latest call_log_id of the inserted number
            	int mLatestCallLogId = updateCallsInfoForNewInsertNumber(db, sipNumber, rawContactId, dataId);
            	log("[insert] latest call log id: " + mLatestCallLogId);
			}
		} else {
			dataId = super.insert(db, txContext, rawContactId, values);
		}
		
		return dataId;
	}

	@Override
	public boolean update(SQLiteDatabase db, TransactionContext txContext,
			ContentValues values, Cursor c, boolean callerIsSyncAdapter)  {
		
		if (!super.update(db, txContext, values, c, callerIsSyncAdapter))
			return false;
		if (values.containsKey(SipAddress.SIP_ADDRESS)) {
			if (FeatureOption.MTK_SEARCH_DB_SUPPORT == true) {
				long dataId = c.getLong(DataUpdateQuery._ID);
                long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
				String sipNumber = values.getAsString(SipAddress.SIP_ADDRESS);
                String mStrDataId = String.valueOf(dataId);
                String mStrRawContactId = String.valueOf(rawContactId);
	            log("[update]update: sipNumber: " + sipNumber + " || mStrRawContactId: " + mStrRawContactId + " || mStrDataId: " + mStrDataId);
	            //update calls table to clear raw_contact_id and data_id, if the changing number or the changed number exists in call log.
	            int mDeletedCallLogId = 0;

            	//update records in calls table to no name callLog
	            if (mCallsReplaceDataIdUpdate == null) {
	            	mCallsReplaceDataIdUpdate = db.compileStatement(
	            			"UPDATE " + Tables.CALLS +
	            			" SET " + Calls.DATA_ID + "=?, " + 
	            			Calls.RAW_CONTACT_ID + "=? " + 
	            			" WHERE " + Calls.DATA_ID + " =? ");
	            }
	            mCallsReplaceDataIdUpdate.bindNull(1);
	            mCallsReplaceDataIdUpdate.bindNull(2);
	            mCallsReplaceDataIdUpdate.bindLong(3, dataId);
	            mCallsReplaceDataIdUpdate.execute();
	            log("[update] Change the old records in calls table to a NO NAME CALLLOG.");
	            
	            //update new number's callLog info(dataId & rawContactId) if exists
				int mLatestCallLogId = updateCallsInfoForNewInsertNumber(db, sipNumber, rawContactId, dataId);
            	log("[update] latest call log id: " + mLatestCallLogId);
			}
		}
		return true;
		
	}
	
	
	int updateCallsInfoForNewInsertNumber(SQLiteDatabase db,
			String number, long rawContactId, long dataId) {
		if (mCallsNewInsertDataIdUpdate == null) {
			mCallsNewInsertDataIdUpdate = db.compileStatement(
        			"UPDATE " + Tables.CALLS + 
        			" SET " + Calls.DATA_ID + "=?, " + 
        			Calls.RAW_CONTACT_ID + "=? " + 
        			" WHERE PHONE_NUMBERS_EQUAL(" + Calls.NUMBER + ", ?) AND " + 
        			Calls.DATA_ID + " IS NULL ");
		}
		if (mCallsGetLatestCallLogIdForOneContactQuery == null) {
			mCallsGetLatestCallLogIdForOneContactQuery = db.compileStatement(
        			"SELECT " + Calls._ID + " FROM " + Tables.CALLS +
        			" WHERE " + Calls.DATE + " = (" +
        			" SELECT MAX( " + Calls.DATE + " ) " +
        			" FROM " + Tables.CALLS +
        			" WHERE " + Calls.DATA_ID + " =? )");
		}
		mCallsNewInsertDataIdUpdate.bindLong(1,dataId);
		mCallsNewInsertDataIdUpdate.bindLong(2,rawContactId);
        bindString(mCallsNewInsertDataIdUpdate,3,number);
        mCallsNewInsertDataIdUpdate.execute();
        int mCallLogId = 0;
        try{
        	mCallsGetLatestCallLogIdForOneContactQuery.bindLong(1,dataId);
        	mCallLogId = (int) mCallsGetLatestCallLogIdForOneContactQuery.simpleQueryForLong();
        } catch (android.database.sqlite.SQLiteDoneException e) {
        	return 0;
        } catch (NullPointerException e){
        	return 0;
        }

//        Commount out call log notification since ICS call still uses the default one.
//        if (mCallLogId > 0) {
//            notifyCallsChanged();
//        }
        
        return mCallLogId;
    }
	
    void bindString(SQLiteStatement stmt, int index, String value) {
        if (value == null) {
            stmt.bindNull(index);
        } else {
            stmt.bindString(index, value);
        }
    }
	
	private void log(String msg) {
		if (DBG) {
			Log.d(TAG, msg);
		}
	}

}
