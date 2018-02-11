package com.aurora.puremanager.sqlite;

//import com.aurora.puremanager.activity.CustomApplication;
//import com.aurora.puremanager.model.AuroraPrivacyManageModel;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 该数据库主要作用是公开内部数据
 * @author chengrq
 */
public class OpenDataSqlite extends SQLiteOpenHelper {	
	
    private static final String  DATABASE_NAME = "SecureOpenData.db";//数据库名
    private static final int  DATABASE_VERSION= 2;
    
    /**
     * 表名：允许自启动的应用
     */
    public static final String TABLE_NAME_OF_AllowAutoStartApp = "AllowAutoStartApp";
    
    /**
     * 表名：冻结应用
     */
    public static final String TABLE_NAME_OF_FreezedApp = "FreezedApp";
    

    public static final String FIELD_ID="_id"; 
    public static final String PACKAGE_NAME = "packageName";
    public static final String ACCOUNT_ID = "account_id"; 
    
    
    public OpenDataSqlite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {     	   	    	   	
    	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_AllowAutoStartApp + 
          		" ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
          		PACKAGE_NAME +" TEXT);"
          		); 
    	
    	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_FreezedApp + 
          		" ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
          		ACCOUNT_ID +" INT4," +
          		PACKAGE_NAME +" TEXT);"
          		); 
//    	resetPrivacyNumOfAllAccount();  
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_AllowAutoStartApp);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_FreezedApp);
		onCreate(db);
    }
    
    /*private void resetPrivacyNumOfAllAccount(){    	
    	AuroraPrivacyManageModel.getInstance(
    			CustomApplication.getApplication()).resetPrivacyNumOfAllAccount();
    }*/
}
