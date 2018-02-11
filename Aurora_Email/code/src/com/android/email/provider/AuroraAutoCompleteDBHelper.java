package com.android.email.provider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import com.android.email.provider.DBHelper.DatabaseHelper;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.android.emailcommon.mail.Address;
import com.android.email.provider.HanziToPinyin;
import com.android.mail.providers.Conversation;
import com.android.mail.providers.FolderList;

public class AuroraAutoCompleteDBHelper extends SQLiteOpenHelper {

	private final String TAG = "AuroraAutoCompleteDBHelper"; 
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String SIMPLE_NAME ="sname";
	public static final String SIMPLE_EMAIL ="semail";
	public static final String UPDATETIME = "updateTime";
	public static final String PINYIN = "pinyin";
	public static final String PINYINS = "pinyins";
	public static final String TABLE_NAME = "AutoComplete_";
	public static final String DATEBASE_NAME = "AuroraEmailAddress.db";
	public static final int DATABASE_VERSION = 2;
	private String mCurrentAccount;
	private SQLiteDatabase mSqLiteDatabase;
	private static AuroraAutoCompleteDBHelper mInstance;
	private static String mPinyinS;
	
	public static AuroraAutoCompleteDBHelper getInstance(Context context) {
		if(mInstance == null){
			syncInit(context);
		}
		return mInstance;	
	}
	private static synchronized void syncInit(Context context) {  
        if (mInstance == null) {  
        	mInstance = new AuroraAutoCompleteDBHelper(context); 
        }  
    }      

	public static void createAutoCompleteTable(SQLiteDatabase db,String name) {
		String autoCompleteColumns = NAME + " text, " +SIMPLE_NAME + " text, "+ EMAIL + " text, "+ SIMPLE_EMAIL + " text, "
				+ PINYIN + " text, "+PINYINS+" text, " + UPDATETIME + " integer " + ");";
		String createString = " (" + ID
				+ " integer primary key autoincrement, " + autoCompleteColumns;
		// The three tables have the same schema
		db.execSQL("create table  if not exists " + TABLE_NAME + name + createString);
	}

	private AuroraAutoCompleteDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	private AuroraAutoCompleteDBHelper(Context context) {
		super(context, DATEBASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		resetAccountTable(arg0, arg1, arg2);
	}

	public void saveAddress(final com.android.emailcommon.mail.Message message,String tableName) {
		try {
			mSqLiteDatabase = getWritableDatabase();
			Address[] from = message.getFrom();
			Address[] to = message
					.getRecipients(com.android.emailcommon.mail.Message.RecipientType.TO);
			Address[] cc = message
					.getRecipients(com.android.emailcommon.mail.Message.RecipientType.CC);
			Address[] bcc = message
					.getRecipients(com.android.emailcommon.mail.Message.RecipientType.BCC);
			Address[] replyTo = message.getReplyTo();
			String querySql = "SELECT _id , updateTime FROM " + TABLE_NAME + tableName
					+ " WHERE name = ? AND email = ?";
			String sql = "INSERT INTO " + TABLE_NAME + tableName
					+ "(name,sname,email,semail,pinyin,pinyins,updateTime) values (?,?,?,?,?,?,?)";
			String updateSql = "UPDATE "+TABLE_NAME+tableName+" SET updateTime = ? WHERE _id = ?";
			long time = getStringToDate(message.getInternalDate().toString());
			insertAddress(mSqLiteDatabase, from, querySql, sql,updateSql,time);
			insertAddress(mSqLiteDatabase, to, querySql, sql,updateSql,time);
			insertAddress(mSqLiteDatabase, cc, querySql, sql,updateSql,time);
			insertAddress(mSqLiteDatabase, bcc, querySql, sql,updateSql,time);
			insertAddress(mSqLiteDatabase, replyTo, querySql, sql,updateSql,time);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
		}
	}
	public void saveAddress(Rfc822Token[] tokens,String tableName){
		try {
			mSqLiteDatabase = getWritableDatabase();
			String querySql = "SELECT _id , updateTime FROM " + TABLE_NAME + tableName
					+ " WHERE name = ? AND email = ?";
			String sql = "INSERT INTO " + TABLE_NAME + tableName
					+ "(name,sname,email,semail,pinyin,pinyins,updateTime) values (?,?,?,?,?,?,?)";
			String updateSql = "UPDATE "+TABLE_NAME+tableName+" SET updateTime = ? WHERE _id = ?";
			long time =System.currentTimeMillis();
			insertAddress(mSqLiteDatabase, tokens, querySql, sql,updateSql,time);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
		}
	}
	public void insertAddress(SQLiteDatabase db,Rfc822Token[] tokens,String querySql, String sql,String updateSql,long time){
		for (int i = 0; i < tokens.length; i++) {
			String name = tokens[0].getName().trim();
			String address = tokens[0].getAddress().trim();
			if (TextUtils.isEmpty(name)) {
				name = address;
			}
			Cursor cursor = db.rawQuery(querySql,
					new String[] { name, address });
			if (cursor != null && cursor.getCount() == 0) {
				String sname= name;
				String saddress = address;
				int index = sname.indexOf("@");
				if(index>=0){
					sname = sname.substring(0, index);
				}
				index = address.indexOf("@");
				if(index>=0){
					saddress = address.substring(0, index);
				}
				db.execSQL(sql, new String[] { name,sname, address,saddress, getSpell(sname),mPinyinS,
						String.valueOf(time) });
			}else if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				if(time>Long.valueOf(cursor.getString(1))){
					db.execSQL(updateSql, new String[] {String.valueOf(time),String.valueOf(cursor.getInt(0))});
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	public void insertAddress(SQLiteDatabase db, Address[] adrresses,
			String querySql, String sql,String updateSql,long time) {
		for (int i = 0; i < adrresses.length; i++) {
			Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(adrresses[i]
					.toString());
			String name = tokens[0].getName().trim();
			String address = tokens[0].getAddress().trim();
			if (TextUtils.isEmpty(name)) {
				name = address;
			}
			Cursor cursor = db.rawQuery(querySql,
					new String[] { name, address });
			if (cursor != null && cursor.getCount() == 0) {
				String sname= name;
				String saddress = address;
				int index = sname.indexOf("@");
				if(index>=0){
					sname = sname.substring(0, index);
				}
				index = address.indexOf("@");
				if(index>=0){
					saddress = address.substring(0, index);
				}
				db.execSQL(sql, new String[] { name,sname, address,saddress, getSpell(sname),mPinyinS,
						String.valueOf(time) });
			}else if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				if(time>Long.valueOf(cursor.getString(1))){
					db.execSQL(updateSql, new String[] {String.valueOf(time),String.valueOf(cursor.getInt(0))});
				}
			}
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public Cursor getAdrress(String key,int type) {
		mSqLiteDatabase = getReadableDatabase();
		String querySql = "";
		if(type==0){
			querySql = "SELECT _id,email,name FROM " + TABLE_NAME + mCurrentAccount
					+ " WHERE email LIKE ? OR sname LIKE ? OR pinyin LIKE ? OR pinyins LIKE ?";
		}else{
			querySql = "SELECT _id,email,name FROM " + TABLE_NAME + mCurrentAccount
					+ " WHERE semail LIKE ? OR sname LIKE ? OR pinyin LIKE ? OR pinyins LIKE ?";
		}
		Cursor cursor = mSqLiteDatabase.rawQuery(querySql, new String[] {key + "%",key + "%",key + "%","%"+key+"%"});
		return cursor;
	}

	public Cursor getNewlyAdrress(long count) {
		mSqLiteDatabase = getReadableDatabase();
		String querySql = "SELECT _id,email,name FROM " + TABLE_NAME + mCurrentAccount
				+ " order by updateTime desc limit "+count;
		Cursor cursor = mSqLiteDatabase.rawQuery(querySql, null);
		return cursor;
	}
	public void setCurrentTableName(String name){
		mCurrentAccount = name;
		mSqLiteDatabase = getWritableDatabase();
		createAutoCompleteTable(mSqLiteDatabase, name);
	}

	public void close() {
		if(mSqLiteDatabase != null){
			mSqLiteDatabase.close();
		}
	}

	private void resetAccountTable(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		Cursor cursor = db.rawQuery(
				"select * from sqlite_master where type ='table'", null);
		if (cursor == null) {
			return;
		}
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if (cursor.getString(1) != null&&cursor.getString(1).startsWith(TABLE_NAME)) {
				try {
					db.execSQL("drop table " + cursor.getString(1));
				} catch (SQLException e) {
				} finally {
				}
			}
		}
		cursor.close();
	}

	public static String getSpell(String str) {
		mPinyinS = "";
		StringBuffer buffer = new StringBuffer();
		boolean flag = false;
		if (str != null && !str.equals("")) {
			char[] cc = str.toCharArray();
			for (int i = 0; i < cc.length; i++) {
				ArrayList<HanziToPinyin.Token> mArrayList = HanziToPinyin
						.getInstance().get(String.valueOf(cc[i]));
				if (mArrayList.size() > 0) {
					String n = mArrayList.get(0).target;
					buffer.append(n);
					mPinyinS += n.substring(0, 1);
				}
			}
		}
		String spellStr = buffer.toString();
		return spellStr;
	}
	
	public static long getStringToDate(String time){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss 'GMT+08:00' yyyy", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		try {
			date = sdf.parse(time);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return date.getTime();
	}
	//Aurora <shihao> <2015-03-17> for back up to Calendar begin
	public static final String M_TABLE_NAME = "Email_Calendar_backup";
	public static final String TITLE = "Email_Title";
	public static final String ACCOUNT = "account_name";
	public static final String FOLDER_URI = "folder_uri";
	public static final String CONVERSATION = "conversation";
	public static final String FOLDERLIST = "folderlist";
	public static String columns[] = {TITLE,ACCOUNT,FOLDER_URI,FOLDERLIST,CONVERSATION};

	public void createBackupTable(){
		SQLiteDatabase db = getWritableDatabase();
		Log.i("shihao","AuroraAutoCompleteDBHelper come to createBackupTable");
		db.execSQL("create table if not exists "+ M_TABLE_NAME +"("
						+ ID + " integer primary key autoincrement,"
						+ TITLE + " text,"
						+ ACCOUNT + " text,"
						+ FOLDER_URI + " text,"
						+ FOLDERLIST + " BLOB,"
						+ CONVERSATION + " BLOB);"
						);
		db.close();
	}
	
	public void insertData(String account,String folderUri, Conversation conversation){
		if(isExist(conversation.subject))
			return;
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(TITLE, conversation.subject);
		values.put(ACCOUNT, account);
		values.put(FOLDER_URI, folderUri);
		values.put(FOLDERLIST, conversation.rawFolders.toBlob());
		values.put(CONVERSATION, conversation.toBlob());
		db.insert(M_TABLE_NAME, null, values);
		db.close();
	}
	
	public HashMap<String, Object> getAllMessages(String title){
		createBackupTable();
		String accountString = new String();
		Uri folderUri = null;
		Conversation conversation = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(M_TABLE_NAME, columns, TITLE + " =?", new String[]{title}, null, null, null);
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			accountString = cursor.getString(cursor.getColumnIndex(ACCOUNT));
			folderUri = Uri.parse(cursor.getString(cursor.getColumnIndex(FOLDER_URI)));
			conversation = Conversation.fromBlob(cursor.getBlob(cursor.getColumnIndex(CONVERSATION)));
			if(conversation != null)
				conversation.setRawFolders(FolderList.fromBlob(cursor.getBlob(cursor.getColumnIndex(FOLDERLIST))));
			cursor.close();
		}
		db.close();
		map.put(ACCOUNT, accountString);
		map.put(FOLDER_URI, folderUri);
		map.put(CONVERSATION, conversation);
		return map;
	}	
	
	private boolean isExist(String title){
		boolean isExist = false;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(M_TABLE_NAME, columns, TITLE + " =?", new String[]{title}, null, null, null);
		if(cursor != null && cursor.getCount() > 0)
			isExist = true;
		return isExist;
	}
	//Aurora <shihao> <2015-03-17> for back up to Calendar end
}
