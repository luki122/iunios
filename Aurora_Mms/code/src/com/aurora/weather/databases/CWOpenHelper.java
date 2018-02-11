package com.aurora.weather.databases;

import java.io.File;
// Aurora xuyong 2015-04-23 added for aurora's new feature start
import com.aurora.weather.data.WeatherInfo;
// Aurora xuyong 2015-04-23 added for aurora's new feature end
import com.aurora.weather.util.AuroraMsgWeatherUtils;
// Aurora xuyong 2015-04-23 added for aurora's new feature start
import com.aurora.weather.data.WeatherResult;
// Aurora xuyong 2015-04-23 added for aurora's new feature end
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CWOpenHelper extends SQLiteOpenHelper{
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    private static final String AURORA_WEATHER_INFO_TABLE = "aurora_weather_info";
    private static final String WEATHER_ID = "_id";
    private static final String WEATHER_SOURCE_INFO = "source_info";
    private static final String WEATHER_FL_INFO = "fl_info";
    private static final String WEATHER_SL_INFO = "sl_info";
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    private static final String WEATHER_DATA_TABLE = "weather_data";
    
    private static final String CITY_DATA_TABLE = "city_data";
    private static final String ID              = "_id";
    private static final String CITY_NAME       = "cityName";
    
    private String[] selectColumn = new String[] {
            ID,
            CITY_NAME
    };
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    private String[] mResultSelectColumn = new String[] {
            WEATHER_ID,
            WEATHER_SL_INFO
    };
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
    public CWOpenHelper(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }
    // Aurora xuyong 2015-04-23 modified for aurora's new feature start
    private static final int DB_VERSION = 5;
    // Aurora xuyong 2015-04-23 modified for aurora's new feature start
    private Context mContext;

    public CWOpenHelper(Context context) {
        super(context, AuroraMsgWeatherUtils.WEATHER_DB_NAME, null, DB_VERSION);
        // TODO Auto-generated constructor stub
        mContext = context;
        initDb();
    }
    
    private void initDb() {
        File file = new File(AuroraMsgWeatherUtils.MMS_WEATHER_DB_PATH + File.separator + AuroraMsgWeatherUtils.WEATHER_DB_NAME);
        if (!file.exists()) {
            file = new File(AuroraMsgWeatherUtils.MMS_WEATHER_DB_PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            AuroraMsgWeatherUtils.copyAssetsToFilesystem(mContext,
                    AuroraMsgWeatherUtils.NATIVE_WEATHER_DB_NAME, AuroraMsgWeatherUtils.MMS_WEATHER_DB_PATH);
        }
        
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // Aurora xuyong 2015-04-23 added for aurora's new feature start
        AuroraMsgWeatherUtils.copyAssetsToFilesystem(mContext,
                AuroraMsgWeatherUtils.NATIVE_WEATHER_DB_NAME, AuroraMsgWeatherUtils.MMS_WEATHER_DB_PATH);
        // Aurora xuyong 2015-04-23 added for aurora's new feature end
    }
    
    private void closeDb(SQLiteDatabase db) {
        if (db != null) {
            db.close();
            db = null;
        }
    }
    
    private void close(SQLiteDatabase db, Cursor cusror) {
        closeDb(db);
        closeCursor(cusror);
    }
    
    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }
    
    public String getCityIdByName(String cityName) {
        // Aurora xuyong 2015-04-24 added for bug  #13349 start
    	if (cityName == null) {
    		return null;
    	}
        // Aurora xuyong 2015-04-24 added for bug  #13349 end
        Cursor result = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        String selection = CITY_NAME + " = ?";
        String[] selectionArgs = new String[] {
                cityName
        };
        try {
            result = db.query(CITY_DATA_TABLE, selectColumn, selection, selectionArgs, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (result != null && result.moveToFirst()) {
                String id = result.getString(0);
                close(db, result);
                return id;
            }
        }
        close(db, result);
        return null;
    }
    
    public String getCityNameFromLocation (String location) {
        // Aurora xuyong 2015-04-24 added for bug  #13349 start
    	if (location == null) {
    		return null;
    	}
        // Aurora xuyong 2015-04-24 added for bug  #13349 end
        Cursor result = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        try {
            result = db.query(CITY_DATA_TABLE, selectColumn, null, null, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (result != null && result.moveToFirst()) {
                String cityName = null;
                do
                {
                    cityName = result.getString(1);
                    Log.e("Mms/Weather", "cityName = " + cityName);
                    if (location.contains(cityName)) {
                        return cityName;
                    }
                } while (!result.isAfterLast() && result.moveToNext());
            }
        }
        close(db, result);
        return null;
        
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature start
    public WeatherResult getWeatherResult(WeatherInfo info) {
        // Aurora xuyong 2015-04-24 added for bug  #13349 start
        if (info == null) {
            return null;  
        }
        // Aurora xuyong 2015-04-24 added for bug  #13349 end
        WeatherResult weatherResult = new WeatherResult();
        Cursor result = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        String selection = WEATHER_SOURCE_INFO + " = ?";
        String[] selectionArgs = new String[] {
                info.getWeatherType()
        };
        try {
            result = db.query(AURORA_WEATHER_INFO_TABLE, mResultSelectColumn, selection, selectionArgs, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (result != null && result.moveToFirst()) {
                weatherResult.setIndex(result.getInt(0));
                weatherResult.setName(result.getString(1));
            }
        }
        close(db, result);
        return weatherResult;
    }
    // Aurora xuyong 2015-04-23 added for aurora's new feature end
}
