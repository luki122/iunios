package com.android.providers.contacts;

import com.android.providers.contacts.ContactsDatabaseHelper;
import com.android.providers.contacts.ContactsDatabaseHelper.NameLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.NameLookupType;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.PhoneLookupColumns;
import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.SearchIndexManager.IndexBuilder;
import com.mediatek.providers.contacts.ContactsFeatureConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.PhoneLookup;
import com.android.providers.contacts.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.providers.contacts.ContactsFeatureConstants.FeatureOption;


public class DataRowHandlerForPhoneNumberEx extends
		DataRowHandlerForPhoneNumber {
	private static final String TAG = "DataRowHandlerForPhoneNumberEx";
	private static final boolean DBG = ContactsFeatureConstants.DBG_DIALER_SEARCH;
	private Context mContext;
	private ContactsDatabaseHelper mDbHelper;
	
	
	private SQLiteStatement mCallsNewInsertDataIdUpdate;
	private SQLiteStatement mCallsGetLatestCallLogIdForOneContactQuery;
	private SQLiteStatement mCallsReplaceDataIdUpdate;

	public DataRowHandlerForPhoneNumberEx(Context context,
			ContactsDatabaseHelper dbHelper, ContactAggregator aggregator) {
		super(context, dbHelper, aggregator);
		mContext = context;
		mDbHelper = dbHelper;
	}

	@Override
	public int delete(SQLiteDatabase db, TransactionContext txContext, Cursor c) {
		int result = super.delete(db, txContext, c);
		
        //SYNC calls table and dialer_search table AFTER delete data table.
        //This code can is called IFF IN Editing Contacts view, 
        //other cases are handled by raw_contacts trigger. 
        if (FeatureOption.MTK_SEARCH_DB_SUPPORT == true) {
        	long dataId = c.getLong(DataDeleteQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            log("[delete] dataId: " + dataId + " || rawContactId: " + rawContactId);
            // For callLog, remove raw_contact_id and data_id
            updateCallsAndDialerSearchByContactNumberChanged(db, rawContactId, dataId);
            // log("[delete] Remove raw_contact_id and data_id data in CallLog. ");
        }
        return result;
	}

	@Override
	public long insert(SQLiteDatabase db, TransactionContext txContext,
			long rawContactId, ContentValues values) {
		long dataId = 0;
        if (values.containsKey(Phone.NUMBER)) {
            String number = values.getAsString(Phone.NUMBER);
            dataId = super.insert(db, txContext, rawContactId, values);

			if (FeatureOption.MTK_SEARCH_DB_SUPPORT) {
	            String numberForDialerSearch =  null;
	            if (number != null) {
	            	numberForDialerSearch = DialerSearchUtils.stripSpecialCharInNumberForDialerSearch(number);
	            }
	            log("[insert] number:"+ number + " || numberForDialerSearch: " + numberForDialerSearch);
            	log("[insert] rawContactId: "+ rawContactId+"dataId: "+dataId);
            	// update call Log record, and get the Latest call_log_id of the inserted number
				int latestCallLogId = updateCallsInfoForNewInsertNumber(db,
								numberForDialerSearch, rawContactId, dataId);
            	log("[insert] latest call log id: " + latestCallLogId);
            	// wangth 20140410 add for aurora begin
            	ContactsDatabaseHelper.updateCalls(db);
            	// wangth 20140410 add for aurora end
			}
        }
		return dataId;
	}

	@Override
	public boolean update(SQLiteDatabase db, TransactionContext txContext,
			ContentValues values, Cursor c, boolean callerIsSyncAdapter) {
		boolean result = super.update(db, txContext, values, c, callerIsSyncAdapter);
		if (!result) {
			return false;
		}
		if (values.containsKey(Phone.NUMBER)) {
			String number = values.getAsString(Phone.NUMBER);
             long dataId = c.getLong(DataUpdateQuery._ID);
             long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
             //update calls table and dialer search table AFTER updating data table.
             if (FeatureOption.MTK_SEARCH_DB_SUPPORT == true) {
                 String mStrDataId = String.valueOf(dataId);
                 String mStrRawContactId = String.valueOf(rawContactId);
                 log("[update]update: number: " + number + " || mStrRawContactId: " + mStrRawContactId + " || mStrDataId: " + mStrDataId);
                 //update calls table to clear raw_contact_id and data_id, if the changing number or the changed number exists in call log.
                 int mDeletedCallLogId = 0;

                 //update records in calls table to no name callLog
                 updateCallsAndDialerSearchByContactNumberChanged(db, rawContactId, dataId);

                  String numberForDialerSearch =  null;
                  if (number != null) {
                      numberForDialerSearch = DialerSearchUtils.stripSpecialCharInNumberForDialerSearch(number);
                  }
                  log("[update] number:"+ number + " || numberForDialerSearch: " + numberForDialerSearch);
                  log("[update]Delete old records in dialer_search FOR its callLogId=0.");
                  //update new number's callLog info(dataId & rawContactId) if exists
                  int latestCallLogId = updateCallsInfoForNewInsertNumber(db, numberForDialerSearch, rawContactId, dataId);
                  log("[update] latest call log id: " + latestCallLogId);
             }
		}
		return result;
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
        			" WHERE " + Calls._ID + " = (" +
        			" SELECT MAX( " + Calls._ID + " ) " +
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
    
	void updateCallsReplaceDataId(SQLiteStatement sqliteState,
			SQLiteDatabase db, long newDataId, long newContactId, long oldDataId) {
		if (sqliteState == null) {
			sqliteState = db.compileStatement("UPDATE " + Tables.CALLS
					+ " SET " + Calls.DATA_ID + "=?, " 
					+ Calls.RAW_CONTACT_ID + "=? " 
					+ " WHERE " + Calls.DATA_ID + " =? ");
		}
		if (newDataId > 0) {
			sqliteState.bindLong(1, newDataId);
		} else {
			sqliteState.bindNull(1);
		}
		if (newContactId > 0) {
			sqliteState.bindLong(2, newContactId);
		} else {
			sqliteState.bindNull(2);
		}
		sqliteState.bindLong(3, oldDataId);
		sqliteState.execute();
	}
	
    void bindString(SQLiteStatement stmt, int index, String value) {
        if (value == null) {
            stmt.bindNull(index);
        } else {
            stmt.bindString(index, value);
        }
    }
    
    /**
     * Update calls and dialer_search when the contact number is changed
     * @param db
     * @param rawContactId
     * @param dataId
     */
    private void updateCallsAndDialerSearchByContactNumberChanged(SQLiteDatabase db, long rawContactId,
            long dataId) {
        String oldNumber = null;
        long  callLogId = -1;

        long newDataId = -1;
        long newContactId = -1;

        log("[updateCallsAndDialerSearch] rawContactId:" + rawContactId + " dataId:" + dataId);

        // Get old number from table calls by raw_contact_id and data_id
        Cursor numberCallCursor = db.query(Tables.CALLS, 
                new String[] {Calls._ID, Calls.NUMBER} ,
                Calls.DATA_ID + "=" + dataId + " AND " + Calls.RAW_CONTACT_ID + "=" + rawContactId,
                null, null, null, " _id DESC ", "1");
        try {
            if ((null != numberCallCursor) && (numberCallCursor.moveToFirst())) {
                callLogId = numberCallCursor.getLong(0);
                oldNumber = numberCallCursor.getString(1);
            }
        } finally {
            if (numberCallCursor != null)
                numberCallCursor.close();
        }

        // Search the raw_contact_id and data_id according the got old number
        if (null != oldNumber) {
        	
        	Cursor newContactIdCursor = mContext.getContentResolver().query(
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri
                            .encode(oldNumber)),
                    new String[] {
                            PhoneLookupColumns.DATA_ID,
                            PhoneLookupColumns.RAW_CONTACT_ID,
                    }, null, null, null);
    
            
            if (newContactIdCursor != null) {
                while (newContactIdCursor.moveToNext()) {
                    Cursor numberDataCursor = db.query(Tables.DATA, new String[] {
                        "data1"
                    }, "_id = " + newContactIdCursor.getLong(0), null, null, null, null, null);
                    if (numberDataCursor != null && (numberDataCursor.moveToFirst())) {
                        String newNum = numberDataCursor.getString(0);
                        if (newNum != null) {
                            String newStripNum = DialerSearchUtils.stripSpecialCharInNumberForDialerSearch(newNum);
                                newDataId = newContactIdCursor.getLong(0);
                                newContactId = newContactIdCursor.getLong(1);
                                numberDataCursor.close();
                                break;
                        }
                    }
                    if (null != numberDataCursor) {
                        numberDataCursor.close();
                    }
                }
                newContactIdCursor.close();
            }
        }

        log("[updateCallsAndDialerSearch] callLogId:" + callLogId + " newDataId:" + newDataId
                + " newContactId:" + newContactId);

        // Update dialer_search table:
        // Delete the old record and insert an new call log in dailer_search table
        if ((newDataId > 0) && (newContactId > 0)) {
            // Update the Calls Table (data_id and raw_contact_id to new value)
            log("[updateCallsAndDialerSearch] Update Calls table(data_id to new data_id).");
            updateCallsReplaceDataId(mCallsReplaceDataIdUpdate, db, newDataId, newContactId, dataId);
        } else {
            // Update the Calls Table (data_id to null and raw_contact_id to null)
            log("[updateCallsAndDialerSearch] Update Calls table(data_id to null).");
            if (callLogId > 0) {
                updateCallsReplaceDataId(mCallsReplaceDataIdUpdate, db, 0, 0, dataId);
            }
        }
        
//      Commount out call log notification since ICS call still uses the default one.        
//        if (callLogId > 0) {
//            notifyCallsChanged();
//        }
    }
    
	private void log(String msg) {
		if (DBG) {
			Log.d(TAG, msg);
		}
	}

}
