package com.privacymanage.sqlite;

import com.privacymanage.utils.FileUtils;
import com.privacymanage.utils.Utils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class AuroraPrivacySqlite extends CustomSQLiteOpenHelper {
	private static final String DATABASE_PATH = "data/aurora/com.aurora.privacymanage/databases/";
    private static final String DATABASE_NAME = "AuroraPrivacy.db";//数据库名
    private static final int  DATABASE_VERSION= 3;//数据库版本号
    
    /**
     * 表名：账户相关
     */
    public static final String TABLE_NAME_OF_account = "account";
    
    /**
     * 表名：模块信息相关
     */
    public static final String TABLE_NAME_OF_moduleInfo = "moduleInfo";
    
    /**
     * 表名： 账户对应的配置信息
     */
    public static final String TABLE_NAME_OF_accountConfig = "accountConfig";   
    
    //各表公共字段
    public static final String FIELD_ID="_id";    
    public static final String ACCOUNT_ID = "account_id";  
    public static final String EXTRA_1 = "extra_1"; 
    public static final String EXTRA_2 = "extra_2"; 
    public static final String EXTRA_3 = "extra_3"; 
     
    
    //"account"表 字段
    public static final String PASSWORD = "password";    
    public static final String E_MAIL = "e_mail";    
    public static final String HOME_PATH = "homePath";   
    public static final String CREATE_TIME = "createTime";
    public static final String ACCOUNT_STATE = "accountState";
    
    //"moduleInfo"表 字段
    public static final String PACKAGE_NAME = "packageName";
    public static final String CLASS_NAME = "className";
    public static final String ITEM_NUM = "itemNum";
    
    //"accountConfig"表 字段
    public static final String MSG_NOTIFY_SWITCH = "msg_notify_switch";
    public static final String MSG_NOTIFY_HINT = "msg_notify_hint";
      
    public AuroraPrivacySqlite(Context context) {
        super(context, DATABASE_NAME,
        		DATABASE_PATH,
        		null,
        		DATABASE_VERSION);
        if(!FileUtils.fileIsExists(DATABASE_PATH+DATABASE_NAME)){
        	/**
        	 * 由于最开始数据库是存放在"data/data/"+Utils.getOwnPackageName(context)+"/databases"中的，
        	 * 后来为了防止清除用户数据时清掉数据库，于是改变了数据库存放的位置。
        	 * 这样就需要把原地址中的数据库复制到现在地址中来。
        	 */
        	String fromPath = "data/data/"+Utils.getOwnPackageName(context)+"/databases/";
        	FileUtils.copyFolder(fromPath, DATABASE_PATH);
        }
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {    
    	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_account + 
          		" ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
          		ACCOUNT_ID +" INT4," +
          		PASSWORD +" TEXT," +
          		E_MAIL +" TEXT," +
          		HOME_PATH +" TEXT," +
          		CREATE_TIME +" INT4," +
          		ACCOUNT_STATE+" INT4,"+
          		EXTRA_1+" TEXT,"+
          		EXTRA_2+" TEXT,"+
          		EXTRA_3+" TEXT);"
          		); 
      	 
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_moduleInfo + 
   			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
   			 ACCOUNT_ID +" INT4," +
   			 PACKAGE_NAME +" TEXT," +
   			 CLASS_NAME+" TEXT,"+
   			 ITEM_NUM+" INT4,"+
       		 EXTRA_1+" TEXT,"+
       		 EXTRA_2+" TEXT,"+
       		 EXTRA_3+" TEXT);"
   			); 
      	
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_accountConfig + 
  			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
  			ACCOUNT_ID +" INT4," +
  			MSG_NOTIFY_SWITCH+" INT4,"+
  			MSG_NOTIFY_HINT+" TEXT,"+
      		EXTRA_1+" TEXT,"+
      		EXTRA_2+" TEXT,"+
      		EXTRA_3+" TEXT);"
  		); 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_account);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_moduleInfo);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_accountConfig);
		onCreate(db);
    }
}
