package com.android.providers.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.CallLog.Calls;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.PhoneLookup;
import com.android.providers.contacts.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

public class DataRowHandlerForStructuredNameEx extends
		DataRowHandlerForStructuredName {
	private Context mContext;
	private ContactsDatabaseHelper mDbHelper;
	
    public DataRowHandlerForStructuredNameEx(Context context, ContactsDatabaseHelper dbHelper,
            ContactAggregator aggregator, NameSplitter splitter,
            NameLookupBuilder nameLookupBuilder) {
		super(context, dbHelper, aggregator, splitter, nameLookupBuilder);
		mContext = context;
		mDbHelper = dbHelper;
    }

    @Override
    public long insert(SQLiteDatabase db, TransactionContext txContext, long rawContactId,
            ContentValues values) {
        return super.insert(db, txContext, rawContactId, values);
    }

    @Override
    public boolean update(SQLiteDatabase db, TransactionContext txContext, ContentValues values,
            Cursor c, boolean callerIsSyncAdapter) {
        return super.update(db, txContext, values, c, callerIsSyncAdapter);
    }

    @Override
    public int delete(SQLiteDatabase db, TransactionContext txContext, Cursor c) {
        return super.delete(db, txContext, c);
    }
}
