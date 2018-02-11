package com.gionee.astro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;



public class DBOpenHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "Astro";
	private static final String TABLE_NAME = "TABLE_NAME";
	public static final String WEEK_ASTRO_TABLE_NAME = "WeekAstroInfo";	
	public static final String DAY_ASTRO_TABLE_NAME = "DayAstroInfo";



	public static final String ID = "_id";
	public static final String CONTENT = "content";
	public static final String UPDATE_DATE = "cdate";
	public static final String UPDATE_TIME = "ctime";
	public static final String ALARM_TIME = "atime";
	public static final String BG_COLOR = "bgcolor";
	public static final String IS_FOLDER = "isfolder";
	public static final String PARENT_FOLDER = "parentfile";
	public static final String NOTE_FONT_SIZE = "noteFontSize";
	public static final String NOTE_LIST_MODE = "noteListMode";
	public static final String FOLDER_HAVE_NOTE_COUNTS= "haveNoteCount" ;
	public static final String WIDGET_ID = "widgetId";
	public static final String WIDGET_TYPE = "widgetType";
	public static final String NOTE_TITLE = "nodeTitle";
	public static final String ADDRESS_NAME = "addressName";
	public static final String ADDRESS_DETAIL = "addressDetail";
	
	
	
	
	
	
	
	
	public static final String ASTRO = "astro";
	public static final String WEEKBEGIN = "weekBegin";
	public static final String WEEKEND = "weekEnd";
	public static final String FORTUNETYPE = "fortuneType";
	public static final String FORTUNECONTENT = "fortuneContent";
	
	public static final String YEAR       = "year";
	public static final String MONTH      = "month";
	public static final String MONTH_DAY  = "monthDay";
	public static final String ASTRO_PARA = "astroPara";
	
	
	

	
	
	
	public static final String SELF_ASTRO = "selfAstro";
//	public static final String MATCH_ASTRO = "matchAstro";
	public static final String INDEX_COMPREHENSION = "indexComprehension";
	public static final String INDEX_LOVE = "indexLove";
	public static final String INDEX_CAREER = "indexCareer";
	public static final String INDEX_FINANCE = "indexFinance";
	public static final String INDEX_HEALTH = "indexHealth";
	public static final String LUCKY_COLOR = "luckyColor";
	public static final String LUCKY_NUMBER = "luckyNumber";
	
	public static final String Q_FRIEND = "QFriend";
	public static final String DESCRIPTION = "description";
	
	public static final String DAY = "day";


	public static final String NOTE_ALL = " * ";




	public DBOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(DB_NAME,"DBOpenHelper------onCreate start!");

		db.execSQL(" CREATE TABLE IF NOT EXISTS " + WEEK_ASTRO_TABLE_NAME + " ( " + ID
				+ " integer primary key autoincrement , " 
				+ ASTRO + " text , " 
				+ WEEKBEGIN + " text , " 
				+ WEEKEND + " text , " 
				+ FORTUNETYPE + " text , " 
				+ FORTUNECONTENT + " text ,"
				+ YEAR + " integer , "
				+ MONTH + " integer , "
				+ MONTH_DAY + " integer , "
				+ ASTRO_PARA + " text "
				+");");

		db.execSQL(" CREATE TABLE IF NOT EXISTS " + DAY_ASTRO_TABLE_NAME + " ( " + ID
				+ " integer primary key autoincrement , "
				+ SELF_ASTRO + " text , "
				+ INDEX_COMPREHENSION + " integer , "
				+ INDEX_LOVE + " integer , "
				+ INDEX_CAREER + " integer , "
				+ INDEX_FINANCE + " integer , "
				+ INDEX_HEALTH + " integer , "
				+ LUCKY_COLOR + " text , "
				+ LUCKY_NUMBER + " text , "
				+ Q_FRIEND + " text , "
				+ DESCRIPTION + " text , "
				+ DAY + " text , "
				+ YEAR + " integer , "
				+ MONTH + " integer , "
				+ MONTH_DAY + " integer , "
				+ ASTRO_PARA + " text "
				+");");

		Log.i(DB_NAME,"DBOpenHelper------onCreate end!");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		Log.i("DBOpenHelper------onUpgrade start!");
//		Log.d("DBOpenHelper------oldVersion: " + oldVersion + ", newVersion: " + newVersion);
//
//		//gn lilg 2012-12-04 modify for db upgrade begin
//		if(oldVersion == 2){
//			Log.d("DBOpenHelper------oldVersion == 2!");
//			//gn lilg 2012-12-04 add for db upgrade begin
//
//			boolean isExists = isTableExists(db, MEIDA_TABLENAME);
//
//			//backup the data in the table of use now to the temp table
//			backUpData(db, isExists);
//			//gn lilg 2012-12-04 add for db upgrade end
//
//			db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
//			db.execSQL(" DROP TABLE IF EXISTS " + MEIDA_TABLENAME);
//
//			onCreate(db);
//
//			//gn lilg 2012-12-04 add for db upgrade begin
//			//insert the data in the temp table to the reCreate table
//			restoreData(db, isExists);
//			//gn lilg 2012-12-04 add for db upgrade end
//
//			oldVersion ++;
//		}
//
//		if(oldVersion == 3){
//			//pengwei 20120222 modify for CR00772557 begin
//			Log.d("DBOpenHelper------oldVersion == 3!");
//			//backup the data in the table of use now to the temp table
//			String[] sqlStrs = {CREATE_TEMP_TABLE_NOTE_SQL3,CREATE_TEMP_TABLE_MEDIA_SQL,
//					INSERT_DATA_INTO_TEMP_NOTE_TABLE3,INSERT_DATA_INTO_TEMP_MEDIA_TABLE};
//			backUpDataNew(db, sqlStrs);
//			//gn lilg 2012-12-04 add for db upgrade end
//
//			db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
//			db.execSQL(" DROP TABLE IF EXISTS " + MEIDA_TABLENAME);
//
//			onCreate(db);
//			restoreDataNew(db);
//
//			oldVersion ++;
//			//pengwei 20120222 modify for CR00772557 end
//		}
//		//gn lilg 2012-12-04 modify for db upgrade end
//
//		Log.i("DBOpenHelper------onUpgrade end!");
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		Log.i("DBOpenHelper------onDowngrade start!");
//		Log.d("DBOpenHelper------oldVersion: " + oldVersion + ", newVersion: " + newVersion);
//		Log.i("DBOpenHelper------onDowngrade end!");
	}
	

	
	/**
	 * 
	 * @param sqls:To perform SQLS
	 */
	private void exeSql(SQLiteDatabase db,String[] sqls){
		for(int i = 0;i<sqls.length;i++){
			db.execSQL(sqls[i]);
//			Log.d("DBOpenHelper------exeSqls: " + sqls[i]);

		}
	}
	
	/**
	 * 
	 * @param db
	 * @param isExists
	 */
//	private void backUpData(SQLiteDatabase db, boolean isExists) {
//		Log.i("DBOpenHelper------backUpData begin!");
//
//		try{
//
//			if(!isExists){
//				// the table MediaItems not exists
//				
//				// create a temp table
//				Log.d("DBOpenHelper------create temp table note sql: " + CREATE_TEMP_TABLE_NOTE_SQL);
//				db.execSQL(CREATE_TEMP_TABLE_NOTE_SQL);
//
//				// backup the data to the temp table
//				Log.d("DBOpenHelper------insert data into temp note table: " + INSERT_DATA_INTO_TEMP_NOTE_TABLE2);
//				db.execSQL(INSERT_DATA_INTO_TEMP_NOTE_TABLE2);
//
//			}else{
//				// the table MediaItems exists
//
//				// create the temp tables
//				Log.d("DBOpenHelper------create temp table note sql: " + CREATE_TEMP_TABLE_NOTE_SQL);
//				db.execSQL(CREATE_TEMP_TABLE_NOTE_SQL);
//				Log.d("DBOpenHelper------create temp table midia sql: " + CREATE_TEMP_TABLE_MEDIA_SQL);
//				db.execSQL(CREATE_TEMP_TABLE_MEDIA_SQL);
//
//				// backup the data to the temp tables
//				Log.d("DBOpenHelper------insert data into temp note table: " + INSERT_DATA_INTO_TEMP_NOTE_TABLE);
//				db.execSQL(INSERT_DATA_INTO_TEMP_NOTE_TABLE);
//				Log.d("DBOpenHelper------insert data into temp media table: " + INSERT_DATA_INTO_TEMP_MEDIA_TABLE);
//				db.execSQL(INSERT_DATA_INTO_TEMP_MEDIA_TABLE);
//			}
//
//		}catch(Exception e){
//			Log.e("DBOpenHelper------backUpdata exception!", e);
//			
//			// drop the temp tables
//			db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE_NAME_NOTE);
//			db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE_NAME_MEDIA);
//			Log.d("DROP TABLE IF EXISTS: " + TEMP_TABLE_NAME_NOTE + ", " + TEMP_TABLE_NAME_MEDIA);
//		}
//
//		Log.i("DBOpenHelper------backUpData end!");
//	}

	/**
	 * 
	 * @param isExists
	 */
	private void restoreDataNew(SQLiteDatabase db) {
//		Log.i("DBOpenHelper------restoreData begin!");
//
//		try{
//			// gn pengwei 20121225 modify for CR00753125 begin
//				Log.d("DBOpenHelper------insert data into note table: " + INSERT_DATA_INTO_NOTE_TABLE2);
//				String[] sqlsStr = {
//						UPDATE_DATA_INTO_NOTE_TABLE_FOR_ATIME,
//						INSERT_DATA_INTO_NOTE_TABLE,INSERT_DATA_INTO_MEDIA_TABLE
//				};
//				exeSql(db, sqlsStr);
//				// gn pengwei 20121225 modify for CR00753125 end
//
//		}catch(Exception e){
//			Log.e("DBOpenHelper------restoreData exception!", e);
//			
//			// drop the tables
//			db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
//			db.execSQL(" DROP TABLE IF EXISTS " + MEIDA_TABLENAME);
//			Log.d("DROP TABLE IF EXISTS: " + TABLE_NAME + ", " + MEIDA_TABLENAME);
//			
//			// create the tables again
//			onCreate(db);
//		}finally{
//			// drop the temp tables
//			db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE_NAME_NOTE);
//			db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE_NAME_MEDIA);
//			Log.d("DROP TABLE IF EXISTS: " + TEMP_TABLE_NAME_NOTE + ", " + TEMP_TABLE_NAME_MEDIA);
//		}
//
//		Log.i("DBOpenHelper------restoreData end!");
	}
	
	/**
	 * 
	 * @param isExists
	 */
	private void restoreData(SQLiteDatabase db, boolean isExists) {
//		Log.i("DBOpenHelper------restoreData begin!");
//
//		try{
//
//			if(!isExists){
//				// the table MediaItems not exists
//				
//				// restore the data from the temp table
//				Log.d("DBOpenHelper------insert data into note table: " + INSERT_DATA_INTO_NOTE_TABLE2);
//				// gn pengwei 20121225 modify for CR00753125 begin
//				db.execSQL(UPDATE_DATA_INTO_NOTE_TABLE_FOR_ATIME);
//				// gn pengwei 20121225 modify for CR00753125 end
//				db.execSQL(INSERT_DATA_INTO_NOTE_TABLE2);
//			}else{
//				// the table MediaItems exists
//				
//				// restore the data from the temp tables
//				Log.d("DBOpenHelper------insert data into note table: " + INSERT_DATA_INTO_NOTE_TABLE);
//				db.execSQL(INSERT_DATA_INTO_NOTE_TABLE);
//				Log.d("DBOpenHelper------insert data into media table: " + INSERT_DATA_INTO_MEDIA_TABLE);
//				db.execSQL(INSERT_DATA_INTO_MEDIA_TABLE);
//			}
//
//		}catch(Exception e){
//			Log.e("DBOpenHelper------restoreData exception!", e);
//			
//			// drop the tables
//			db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
//			db.execSQL(" DROP TABLE IF EXISTS " + MEIDA_TABLENAME);
//			Log.d("DROP TABLE IF EXISTS: " + TABLE_NAME + ", " + MEIDA_TABLENAME);
//			
//			// create the tables again
//			onCreate(db);
//		}finally{
//			// drop the temp tables
//			db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE_NAME_NOTE);
//			db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE_NAME_MEDIA);
//			Log.d("DROP TABLE IF EXISTS: " + TEMP_TABLE_NAME_NOTE + ", " + TEMP_TABLE_NAME_MEDIA);
//		}
//
//		Log.i("DBOpenHelper------restoreData end!");
	}

	/**
	 * 
	 * @param db
	 * @param tableName
	 * @return
	 */
//	private boolean isTableExists(SQLiteDatabase db, String tableName)	{
//		Log.i("DBOpenHelper------in isTableExists!");
//
//		boolean isExists = false;
//		if(TextUtils.isEmpty(tableName)){
//			Log.e("DBOpenHelper------tableName: " + tableName);
//			return isExists;
//		}
//
//		Cursor cursor = null;
//		try{
//			String sql = "select count(*) as c from sqlite_master where type = 'table' and name = '" + tableName.trim() + "'; ";
//			cursor = db.rawQuery(sql, null);
//			if(cursor.moveToNext()){
//				int count = cursor.getInt(0);
//				Log.d("DBOpenHelper------count: " + count);
//				if(count > 0){
//					isExists = true;
//				}
//			}
//		}catch(Exception e){
//			Log.e("DBOpenHelper------error in judge the table if exists!", e);
//		}finally{
//			if(cursor != null){
//				cursor.close();
//			}
//		}
//
//		return isExists;
//	}
}