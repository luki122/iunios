// Gionee <Author: lihongyu> <2013-07-13> add for CR000000 begin
package com.gionee.almanac;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.json.JSONObject;

import com.gionee.astro.DBOpenHelper;
import com.gionee.astro.DBOperations;
import com.gionee.astro.GNAstroUtils.AstroRequest;
import com.gionee.astro.GNAstroUtils.DayAstroInfo;
import com.gionee.calendar.day.MessageDispose;

import com.android.calendar.R;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;

public class GNAlmanacUtils implements MessageDispose{
	private static final String LOG_TAG = "almanac";
	
	private static final String DB_PATH = "/data/data/com.android.calendar/databases/";
	private static final String DB_NAME = "almanac.db";
	private static final String DB_FILE_PATH = DB_PATH + DB_NAME;
	private static final String TABLE_NAME = "almanac";
	private static final String COLUMN_NAME_GREGRIAN_DATE = "greg_date";
	private static final String COLUMN_NAME_LUNAR_DATE = "lunar_date";
	private static final String COLUMN_NAME_CHINESE_ERA = "chinese_era"; // tian gan di zhi
	private static final String COLUMN_NAME_TODAY_FITTED = "today_fitted";
	private static final String COLUMN_NAME_TODAY_UNFITTED = "today_unfitted";
	private static final String COLUMN_NAME_FIVE_ELEMENTS = "five_elements"; // wu xing
	private static final String COLUMN_NAME_CHONG = "chong";
	private static final String COLUMN_NAME_PENGZU = "pengzu"; // peng zu bai ji
	private static final String COLUMN_NAME_LUCKY_FAIRY = "lucky_fairy"; // ji shen yi qu
	private static final String COLUMN_NAME_EVIL_SPIRIT = "evil_spirit"; // xiong shen yi ji
	private static final String COLUMN_NAME_BIRTH_GOD = "birth_god"; // tai shen
	
	private static final String[] COLUMNS = {
		COLUMN_NAME_GREGRIAN_DATE,
		COLUMN_NAME_LUNAR_DATE,
		COLUMN_NAME_CHINESE_ERA,
		COLUMN_NAME_TODAY_FITTED,
		COLUMN_NAME_TODAY_UNFITTED,
		COLUMN_NAME_FIVE_ELEMENTS,
		COLUMN_NAME_CHONG,
		COLUMN_NAME_PENGZU,
		COLUMN_NAME_LUCKY_FAIRY,
		COLUMN_NAME_EVIL_SPIRIT,
		COLUMN_NAME_BIRTH_GOD
	};
	
	private static final String SELECTION = COLUMN_NAME_GREGRIAN_DATE + "=?";
	
	private static final int OK = 1;
	private static final int ERROR = 0;
	private static int mInitState;
	private static final String TAG = "GNAlmanacUtils";
	
	public static final int ERROR_CODE_ALMANAC_QUERY_OK = 0;
	public static final int ERROR_CODE_ALMANAC_QUERY_NO_DATA = 1;
	public static final int ERROR_CODE_ALMANAC_QUERY_FAIL = 2;
	public static final int ERROR_CODE_ALMANAC_QUERY_INVALID_DATE = 3;
	
	public int initAlmanac(Context context) {
		File dir = new File(DB_PATH);
		if(!dir.exists()) {
			boolean result = dir.mkdir();
			if(false==result) {
				return ERROR;
			}
			Log.d(TAG, "create db folder");
		}
		
		File dbFile = new File(DB_FILE_PATH);
		if(!dbFile.exists()) {
//			InputStream is = null;
			InputStream is = context.getResources().openRawResource(R.raw.almanac);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dbFile);
				Log.d(TAG, "open db file");
			} catch(FileNotFoundException e) {
				Log.d(TAG, "db file not found!");
				return ERROR;
			}
			
			try {
				Log.d(TAG, "copy db file start");
				byte[] buffer = new byte[1024 * 8];
				int count = 0;
				while((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				Log.d(TAG, "copy db file end");
			} catch(IOException e) {
				Log.d(TAG, "IO Error!");
				//return ERROR;
			} finally {
				try {
					if(fos != null) fos.close();
					if(is != null) is.close();
					Log.d(TAG, "close file res");
				} catch(IOException e) {
					Log.d(TAG, "IO Error when close stream");
				}
			}
		} // end of db file copy
		
		this.loadRes(context);

		return OK;
	}
	
	// Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin
	private String mStrFiveElementsMetal = null;
	private String mStrFiveElementsWood = null;
	private String mStrFiveElementsWater = null;
	private String mStrFiveElementsFire = null;
	private String mStrFiveElementsSoil = null;
	
	private String mStrChineseHoroscopeMouse = null;
	private String mStrChineseHoroscopeCattle = null;
	private String mStrChineseHoroscopeTiger = null;
	private String mStrChineseHoroscopeRabbit = null;
	private String mStrChineseHoroscopeDragon = null;
	private String mStrChineseHoroscopeSnake = null;
	private String mStrChineseHoroscopeHorse = null;
	private String mStrChineseHoroscopeGoat = null;
	private String mStrChineseHoroscopeMonkey = null;
	private String mStrChineseHoroscopeChook = null;
	private String mStrChineseHoroscopeDog = null;
	private String mStrChineseHoroscopePig = null;
	
	private char[] mChineseHoroscopeCharCode = null;
	
	private void loadRes(Context context) {
		Resources res = context.getResources();
		
		mStrFiveElementsMetal = res.getString(R.string.almanac_five_elements_metal);
		mStrFiveElementsWood = res.getString(R.string.almanac_five_elements_wood);
		mStrFiveElementsWater = res.getString(R.string.almanac_five_elements_water);
		mStrFiveElementsFire = res.getString(R.string.almanac_five_elements_fire);
		mStrFiveElementsSoil = res.getString(R.string.almanac_five_elements_soil);
		Log.d(LOG_TAG, "load five elements string: " + mStrFiveElementsMetal + ", "
				+ mStrFiveElementsWood + ", " + mStrFiveElementsWater + ", "
				+ mStrFiveElementsFire + ", " + mStrFiveElementsSoil);
		
		mStrChineseHoroscopeMouse = res.getString(R.string.almanac_chinese_horoscope_mouse);
		mStrChineseHoroscopeCattle = res.getString(R.string.almanac_chinese_horoscope_cattle);
		mStrChineseHoroscopeTiger = res.getString(R.string.almanac_chinese_horoscope_tiger);
		mStrChineseHoroscopeRabbit = res.getString(R.string.almanac_chinese_horoscope_rabbit);
		mStrChineseHoroscopeDragon = res.getString(R.string.almanac_chinese_horoscope_dragon);
		mStrChineseHoroscopeSnake = res.getString(R.string.almanac_chinese_horoscope_snake);
		mStrChineseHoroscopeHorse = res.getString(R.string.almanac_chinese_horoscope_horse);
		mStrChineseHoroscopeGoat = res.getString(R.string.almanac_chinese_horoscope_goat);
		mStrChineseHoroscopeMonkey = res.getString(R.string.almanac_chinese_horoscope_monkey);
		mStrChineseHoroscopeChook = res.getString(R.string.almanac_chinese_horoscope_chook);
		mStrChineseHoroscopeDog = res.getString(R.string.almanac_chinese_horoscope_dog);
		mStrChineseHoroscopePig = res.getString(R.string.almanac_chinese_horoscope_pig);
		
		mChineseHoroscopeCharCode = new char[12];
		mChineseHoroscopeCharCode[0] = mStrChineseHoroscopeMouse.charAt(0);
		mChineseHoroscopeCharCode[1] = mStrChineseHoroscopeCattle.charAt(0);
		mChineseHoroscopeCharCode[2] = mStrChineseHoroscopeTiger.charAt(0);
		mChineseHoroscopeCharCode[3] = mStrChineseHoroscopeRabbit.charAt(0);
		mChineseHoroscopeCharCode[4] = mStrChineseHoroscopeDragon.charAt(0);
		mChineseHoroscopeCharCode[5] = mStrChineseHoroscopeSnake.charAt(0);
		mChineseHoroscopeCharCode[6] = mStrChineseHoroscopeHorse.charAt(0);
		mChineseHoroscopeCharCode[7] = mStrChineseHoroscopeGoat.charAt(0);
		mChineseHoroscopeCharCode[8] = mStrChineseHoroscopeMonkey.charAt(0);
		mChineseHoroscopeCharCode[9] = mStrChineseHoroscopeChook.charAt(0);
		mChineseHoroscopeCharCode[10] = mStrChineseHoroscopeDog.charAt(0);
		mChineseHoroscopeCharCode[11] = mStrChineseHoroscopePig.charAt(0);
		
		Log.d(LOG_TAG, "load chinese horoscope CHAR CODE: "
				+ mStrChineseHoroscopeMouse.charAt(0) + ", " +
				+ mStrChineseHoroscopeCattle.charAt(0) + ", " +
				+ mStrChineseHoroscopeTiger.charAt(0) + ", " +
				+ mStrChineseHoroscopeRabbit.charAt(0) + ", " +
				+ mStrChineseHoroscopeDragon.charAt(0) + ", " +
				+ mStrChineseHoroscopeSnake.charAt(0) + ", " +
				+ mStrChineseHoroscopeHorse.charAt(0) + ", " +
				+ mStrChineseHoroscopeGoat.charAt(0) + ", " +
				+ mStrChineseHoroscopeMonkey.charAt(0) + ", " +
				+ mStrChineseHoroscopeChook.charAt(0) + ", " +
				+ mStrChineseHoroscopeDog.charAt(0) + ", " +
				+ mStrChineseHoroscopePig.charAt(0));
	}
	// Gionee <jiangxiao> <2013-07-16> add for CR00837096 end
	private class AlmanacRequest {
		public String queryDate;
		public Message message;		
		public MessageDispose messageDispose;
		public int type;
		public int almanacAction;
		public AlmanacRequest(String queryDate,Message message) {
			this.queryDate = queryDate;
			this.message = message;			
		}
		
		public AlmanacRequest(String queryDate,MessageDispose messageDispose) {
			this.queryDate = queryDate;
			this.messageDispose = messageDispose;			
		}
		
		public AlmanacRequest(String queryDate,MessageDispose messageDispose,int type,int action) {
			this.queryDate = queryDate;
			this.messageDispose = messageDispose;		
			this.type = type;
			this.almanacAction = action;
		}
		
	}

	public class AlmanacInfo {
		public String gregDate;
		public String lunarDate;
		public String chineseEra;
		public String todayFitted;
		public String todayUnfitted;
		public String fiveElements;
		public String chong;
		public String pengzu;
		public String luckyFairy;
		public String evilSpirit;
		public String birthGod;
	}
	
	public class AlmanacDataAsyncTask extends AsyncTask<AlmanacRequest, AlmanacInfo, AlmanacInfo> {
		
		public MessageDispose messageDispose = null;
		public int action;
		public int error;
		@Override
		protected AlmanacInfo doInBackground(AlmanacRequest... requests) {
			// TODO Auto-generated method stub
			if(requests == null || requests.length == 0) {
				Log.d(TAG, "para is error ");
				return null;
			}
			
			AlmanacRequest request = requests[0];
			messageDispose = request.messageDispose;
			this.action = request.almanacAction;
			Log.d(TAG, "doInBackground---doInBackground---action == " + action);
			if((request.messageDispose == null)||(request.queryDate == null)) {
				Log.d(TAG, "para member is error ");
				return null;
			}
			Log.d(TAG, "Almanac query date: " + request.queryDate);
			AlmanacInfo almanacInfo = null;
			
			{
				almanacInfo = new AlmanacInfo(); 
				SQLiteDatabase db = null;
				
				try {
					db = SQLiteDatabase.openDatabase(
							DB_FILE_PATH, null, SQLiteDatabase.OPEN_READONLY);
				} catch (SQLiteException e) {
					// TODO: handle exception
					error = ERROR_CODE_ALMANAC_QUERY_INVALID_DATE;
					return null;
				}
				Log.d(TAG, "Almanac query date: " + request.queryDate);
				String[] selectionArgs = new String[] {request.queryDate};
				Cursor result = db.query(TABLE_NAME, COLUMNS, SELECTION, selectionArgs, null, null, null, null);
				
				if (result != null && result.moveToFirst() != false) {
					almanacInfo.gregDate      = result.getString(result.getColumnIndex(COLUMN_NAME_GREGRIAN_DATE ));
					almanacInfo.lunarDate     = result.getString(result.getColumnIndex(COLUMN_NAME_LUNAR_DATE    ));
					almanacInfo.chineseEra    = result.getString(result.getColumnIndex(COLUMN_NAME_CHINESE_ERA   ));
					almanacInfo.todayFitted   = result.getString(result.getColumnIndex(COLUMN_NAME_TODAY_FITTED  ));
					almanacInfo.todayUnfitted = result.getString(result.getColumnIndex(COLUMN_NAME_TODAY_UNFITTED));
					almanacInfo.fiveElements  = result.getString(result.getColumnIndex(COLUMN_NAME_FIVE_ELEMENTS ));
					almanacInfo.chong         = result.getString(result.getColumnIndex(COLUMN_NAME_CHONG         ));
					almanacInfo.pengzu        = result.getString(result.getColumnIndex(COLUMN_NAME_PENGZU        ));
					almanacInfo.luckyFairy    = result.getString(result.getColumnIndex(COLUMN_NAME_LUCKY_FAIRY   ));
					almanacInfo.evilSpirit    = result.getString(result.getColumnIndex(COLUMN_NAME_EVIL_SPIRIT   ));
					almanacInfo.birthGod      = result.getString(result.getColumnIndex(COLUMN_NAME_BIRTH_GOD     ));
				}
				else
				{
					if (result!=null) {
						result.close();
					}
					if (db != null) {
						db.close();
					}
					error = ERROR_CODE_ALMANAC_QUERY_NO_DATA;
					return null; 
				}
				result.close();
				db.close();
				
				Log.d(LOG_TAG, "AlmanacInfo: " + 
						almanacInfo.gregDate      +" "+
						almanacInfo.lunarDate     +" "+
						almanacInfo.chineseEra    +" "+
						almanacInfo.todayFitted   +" "+
						almanacInfo.todayUnfitted +" "+
						almanacInfo.fiveElements  +" "+
						almanacInfo.chong         +" "+
						almanacInfo.pengzu        +" "+
						almanacInfo.luckyFairy    +" "+
						almanacInfo.evilSpirit    +" "+
						almanacInfo.birthGod);
				Log.v("GNAlm","almanacInfo.chineseEra == " + almanacInfo.chineseEra);
			}
			return almanacInfo;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(AlmanacInfo almanacInfo) {
			// TODO Auto-generated method stub
//			super.onPostExecute(result);
			try {
				Log.v("Almanac", "onPostExecute---action0 == " + action);
				if (almanacInfo != null && messageDispose != null) {
					Log.v("Almanac", "onPostExecute---action == " + action);
					messageDispose.sendMessage(ERROR_CODE_ALMANAC_QUERY_OK, almanacInfo,null,action,-1);
				}else{
					messageDispose.sendMessage(error,null,null,-1,-1);
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.v("Almanac", "onPostExecute---e == " + e);
			}
		}

	}
	
	public class DataAsyncTask extends AsyncTask<AlmanacRequest, Void, Void> {
		
		public Message message;

		@Override
		protected Void doInBackground(AlmanacRequest... requests) {
			// TODO Auto-generated method stub
			if(requests == null || requests.length == 0) {
				Log.d(TAG, "para is error ");
				return null;
			}
			
			AlmanacRequest request = requests[0];
			if((request.message == null)||(request.queryDate == null)) {
				Log.d(TAG, "para member is error ");
				return null;
			}
			Log.d(TAG, "Almanac query date: " + request.queryDate);
			
			message = request.message;
			{
				AlmanacInfo almanacInfo = new AlmanacInfo(); 
				SQLiteDatabase db = null;
				
				try {
					db = SQLiteDatabase.openDatabase(
							DB_FILE_PATH, null, SQLiteDatabase.OPEN_READONLY);
				} catch (SQLiteException e) {
					// TODO: handle exception
					request.message.arg1 = ERROR_CODE_ALMANAC_QUERY_FAIL;
					request.message.obj = null;
					return null;
				}
				Log.d(TAG, "Almanac query date: " + request.queryDate);
				String[] selectionArgs = new String[] {request.queryDate};
				Cursor result = db.query(TABLE_NAME, COLUMNS, SELECTION, selectionArgs, null, null, null, null);
				
				if (result != null && result.moveToFirst() != false) {
					almanacInfo.gregDate      = result.getString(result.getColumnIndex(COLUMN_NAME_GREGRIAN_DATE ));
					almanacInfo.lunarDate     = result.getString(result.getColumnIndex(COLUMN_NAME_LUNAR_DATE    ));
					almanacInfo.chineseEra    = result.getString(result.getColumnIndex(COLUMN_NAME_CHINESE_ERA   ));
					almanacInfo.todayFitted   = result.getString(result.getColumnIndex(COLUMN_NAME_TODAY_FITTED  ));
					almanacInfo.todayUnfitted = result.getString(result.getColumnIndex(COLUMN_NAME_TODAY_UNFITTED));
					almanacInfo.fiveElements  = result.getString(result.getColumnIndex(COLUMN_NAME_FIVE_ELEMENTS ));
					almanacInfo.chong         = result.getString(result.getColumnIndex(COLUMN_NAME_CHONG         ));
					almanacInfo.pengzu        = result.getString(result.getColumnIndex(COLUMN_NAME_PENGZU        ));
					almanacInfo.luckyFairy    = result.getString(result.getColumnIndex(COLUMN_NAME_LUCKY_FAIRY   ));
					almanacInfo.evilSpirit    = result.getString(result.getColumnIndex(COLUMN_NAME_EVIL_SPIRIT   ));
					almanacInfo.birthGod      = result.getString(result.getColumnIndex(COLUMN_NAME_BIRTH_GOD     ));
				}
				else
				{
					if (result!=null) {
						result.close();
					}
					if (db != null) {
						db.close();
					}
					request.message.arg1 = ERROR_CODE_ALMANAC_QUERY_NO_DATA;
					request.message.obj = null;
					return null; 
				}
				result.close();
				db.close();
				
				
				request.message.arg1 = ERROR_CODE_ALMANAC_QUERY_OK;
				request.message.obj = almanacInfo;
				
				Log.d(LOG_TAG, "AlmanacInfo: " + 
						almanacInfo.gregDate      +" "+
						almanacInfo.lunarDate     +" "+
						almanacInfo.chineseEra    +" "+
						almanacInfo.todayFitted   +" "+
						almanacInfo.todayUnfitted +" "+
						almanacInfo.fiveElements  +" "+
						almanacInfo.chong         +" "+
						almanacInfo.pengzu        +" "+
						almanacInfo.luckyFairy    +" "+
						almanacInfo.evilSpirit    +" "+
						almanacInfo.birthGod);
				Log.v("GNAlm","almanacInfo.chineseEra == " + almanacInfo.chineseEra);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
//			super.onPostExecute(result);
			try {
				Log.v("Almanac", "onPostExecute---message == " + message);
				if (message != null) {
					message.sendToTarget();
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.v("Almanac", "onPostExecute---e == " + e);
			}
		}

	}
	
	public static void getAlmanacInfo(String queryDate,Message message) {
		GNAlmanacUtils almanacUtils = new GNAlmanacUtils();
		
		AlmanacRequest request = almanacUtils.new AlmanacRequest(queryDate,message);
		DataAsyncTask task = almanacUtils.new DataAsyncTask();
    	task.execute(request);
	}
	
	// Gionee <jiangxiao> <2013-07-16> add for CR00837096 begin
	private GNAlmanacUtils() {
		
	}
	
	private static GNAlmanacUtils mInstance = null;
	
	public static GNAlmanacUtils getInstance() {
		if(mInstance == null) {
			Log.d(LOG_TAG, "new instance");
			mInstance = new GNAlmanacUtils();
		}
		
		Log.d(LOG_TAG, "existed instance");
		return mInstance;
	}
	
	public static final int MAX_QUERY_YEAR = 2018;
	public static final int MIN_QUERY_YEAR = 2012;
	
	private static final int FIVE_ELEMENT_WORD_OFFSET = 3;
	private static final int CHINESE_HOROSCOPE_WORD_OFFSET = 8;
	
	/*messageDispose:the class of receive the almanac datas which is implement MessageDispose
	type:message type,such as EVENT_QUERY_ALMANAC_INFO
	action:page to next or pre*/
	public void getAlmanacInfo(int year, int month, int monthDay,MessageDispose messageDispose,int type,int action) {
		if(year < MIN_QUERY_YEAR || year > MAX_QUERY_YEAR 
				|| month < 1 || month > 12 
				|| monthDay < 1 || monthDay > 31) {
			// date check fail
			messageDispose.sendMessage(ERROR_CODE_ALMANAC_QUERY_INVALID_DATE,null,null,-1,-1);
		}
		Log.v("Calendar","GNAlmancActivity---getAlmanacInfo---year == " + year);
		Log.v("Calendar","GNAlmancActivity---getAlmanacInfo---month == " + month);
		Log.v("Calendar","GNAlmancActivity---getAlmanacInfo---monthDay == " + monthDay);

		String date = year + "/" + month + "/" + monthDay;
		Log.d(LOG_TAG, "get almanac info of date " + date);
		
		AlmanacRequest request = new AlmanacRequest(date,messageDispose,type,action);
		AlmanacDataAsyncTask task = new AlmanacDataAsyncTask();
    	task.execute(request);
	}
	
	public int getFiveElementFromAlmanacInfo(AlmanacInfo info) {
		if(info == null || info.fiveElements == null 
				|| info.fiveElements.length() < FIVE_ELEMENT_WORD_OFFSET) {
			return -1;
		}

//		Log.d(LOG_TAG, "load five elements CHAR CODE: " + mStrFiveElementsMetal.charAt(0) + ", "
//				+ mStrFiveElementsWood.charAt(0) + ", " + mStrFiveElementsWater.charAt(0) + ", "
//				+ mStrFiveElementsFire.charAt(0) + ", " + mStrFiveElementsSoil.charAt(0));
		
		char code = info.fiveElements.charAt(FIVE_ELEMENT_WORD_OFFSET - 1);
		Log.d(LOG_TAG, "CHAR CODE of AlmanacInfo is " + code);
		if(code == mStrFiveElementsMetal.charAt(0)) {
			return GNAlmanacConstants.FIVE_ELEMENTS_METAL;
		} else if(code == mStrFiveElementsWood.charAt(0)) {
			return GNAlmanacConstants.FIVE_ELEMENTS_WOOD;
		} else if(code == mStrFiveElementsWater.charAt(0)) {
			return GNAlmanacConstants.FIVE_ELEMENTS_WATER;
		} else if(code == mStrFiveElementsFire.charAt(0)) {
			return GNAlmanacConstants.FIVE_ELEMENTS_FIRE;
		} else if(code == mStrFiveElementsSoil.charAt(0)) {
			return GNAlmanacConstants.FIVE_ELEMENTS_SOIL;
		} else {
			return -1;
		}
	}
	
	private int findChineseHoroscope(int charCode) {
		for(int i = 0; i < mChineseHoroscopeCharCode.length; ++i) {
			if(mChineseHoroscopeCharCode[i] == charCode) {
				return i;
			}
		}
		
		return -1;
	}
	
	public int getChineseHoroscopeFromAlmanacInfo(AlmanacInfo info) {
		if(info == null || info.chineseEra == null 
				|| info.chineseEra.length() < CHINESE_HOROSCOPE_WORD_OFFSET) {
			return -1;
		}
		char code = info.chineseEra.charAt(CHINESE_HOROSCOPE_WORD_OFFSET - 1);
		Log.v("code","code == " + code);
		return findChineseHoroscope(code);
	}
	// Gionee <jiangxiao> <2013-07-16> add for CR00837096 end

	@Override
	public void sendMessage(int msg, AlmanacInfo almanacInfo, Time time,int action,int type) {
		// TODO Auto-generated method stub
		
	}
}

//Gionee <Author: lihongyu> <2013-07-13> add for CR000000 end