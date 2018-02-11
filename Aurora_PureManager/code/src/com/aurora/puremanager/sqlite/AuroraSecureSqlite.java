package com.aurora.puremanager.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AuroraSecureSqlite extends SQLiteOpenHelper {	
	
    private static final String  DATABASE_NAME = "AuroraSecure.db";//数据库名
    private static final int  DATABASE_VERSION= 13;//数据库版本号 11
    
    /**
     * 表名：权限相关
     */
    public static final String TABLE_NAME_OF_permission = "permission";
    
    /**
     * 表名：应用相关属性
     */
    public static final String TABLE_NAME_OF_appsInfo = "appsInfo";
    
    /**
     * 表名： 广告提供商信息
     */
    public static final String TABLE_NAME_OF_adProviderInfo = "adProviderInfo";
    
    /**
     * 表名： 广告插件中包含的类名
     */
    public static final String TABLE_NAME_OF_adClassInfo = "adClassInfo";
    
    /**
     * 表名： 广告插件中包含的权限
     */
    public static final String TABLE_NAME_OF_adPermission = "adPermission";
    
    /**
     * 表名：应用的广告信息
     */
    public static final String TABLE_NAME_OF_appsAdInfo = "appsAdInfo";
    
    /**
     * 表名：记录用户对应用的操作信息
     */
    public static final String TABLE_NAME_OF_appUseOperate = "appUseOperate";
    
    
    /**
     * 列名1,_id
     */
    public static final String FIELD_ID="_id"; 
    
    /**
     * 列名2，包名 
     */
    public static final String PACKAGE_NAME = "packageName";
    
    /**
     * 列名： 应用名
     */
    public static final String APP_NAME = "appName";
    
    /**
     * 列名： 应用名拼音
     */
    public static final String APP_NAME_PINGYIN = "appNamePinYin";
    
    /**
     * 列名： 软件版本号
     */
    public static final String APP_VERSION = "appVersion";
    
    /**
     * 列名： 是否为第三方应用（0表示FALSE，1表示TRUE）
     */
    public static final String IS_USER_APP ="isUserApp";
    
    /**
     * 列名：是否是系统白名单应用（0表示FALSE，1表示TRUE）
     */
    public static final String IS_SYS_WHITE_APP = "isSysWhiteApp";
    
    /**
     * 列名：是否有联网权限（0表示FALSE，1表示TRUE）
     */
    public static final String IS_HAVE_NET_PERM = "isHaveNetPerm";
    
    /**
     * 列名： 是否拦截了广告（0表示FALSE，1表示TRUE）
     */
    public static final String IS_BLOCKED_AD ="isBlockedAd";
      
    /**
     * 列名：权限名
     */
    public static final String PERMISSION_NAME = "permName";
    
    /**
     * 列名：权限描述
     */
    public static final String PERMISSION_DESC = "permDesc";
    
    /**
     * 列名：权限id
     */
    public static final String PERMISSION_ID = "permId";
    
    /**
     * 列名：权限状态
     */
    public static final String PERMISSION_STATE = "permState";
    
    /**
     * 列名：开关
     */
    public static final String SWITCH = "switch";
    
    /**
     * 广告插件的包名
     */
    public static final String AD_CLASS_COMM_NAME = "adClassCommName";
    
    /**
     * 广告插件的提供商
     */
    public static final String AD_PROVIDER_NAME = "adProviderName";
    
    /**
     * 广告链接地址
     */
    public static final String AD_URL = "adUrl";
    
    /**
     * 是否有通知栏广告
     */
    public static final String IS_HAVE_NOTIFY_AD = "haveNotifyAd";
    
    /**
     * 是否有视图广告
     */
    public static final String IS_HAVE_VIEW_AD = "haveViewAd";
    
    /**
     * 广告类名
     */
    public static final String CLASS_NAME = "className";
    
    /**
     * 广告描诉
     */
    public static final String DESC = "desc";
    
    /**
     * 广告危险级别
     */
    public static final String LEVEL = "level";
    
    
    public AuroraSecureSqlite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {     	
    	/**
    	 * 创建一个名为 permission 的表，
    	 * 第一个列名为 _id，并且是主键，这列的值是会自动增长的整数（例如，当你插入一行时，SQLite 会给这列自动赋值），
    	 * 第二个列名为：packageName( 字符 ) ，存放应用的包名
    	 * 第三个列名为：permId( int ) ，权限id 
    	 * 第四个列名为：permName( 字符 ) ，权限名 
    	 * 第五个列名为：permDesc( 字符 ) ，权限描述
    	 */    	    	   	
    	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_permission + 
          		" ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
          		PACKAGE_NAME +" TEXT," +
                PERMISSION_ID +" INT4," +
      		    PERMISSION_NAME +" TEXT," +
      		    PERMISSION_DESC +" TEXT," +
                PERMISSION_STATE+" INT4);"
          		); 
      	 
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_appsInfo + 
   			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
   			 PACKAGE_NAME +" TEXT," +
   			 APP_NAME+" TEXT,"+
   			 APP_NAME_PINGYIN+" TEXT,"+
   			 APP_VERSION+" INT4,"+
   			 IS_USER_APP+" INT4,"+
   			 IS_SYS_WHITE_APP+" INT4,"+
   			 IS_HAVE_NET_PERM+" INT4);"
   			); 
      	
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_adProviderInfo + 
  			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
  			AD_PROVIDER_NAME+" TEXT,"+
  			AD_CLASS_COMM_NAME +" TEXT," +
  			IS_HAVE_NOTIFY_AD+" INT4,"+
  			IS_HAVE_VIEW_AD+" INT4,"+
  			AD_URL+" TEXT);"
  			); 
      	
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_adClassInfo + 
     			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
     			AD_PROVIDER_NAME+" TEXT,"+
     			CLASS_NAME+" TEXT,"+
     			DESC+" TEXT);"
     			); 
      	
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_adPermission + 
     			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
     			AD_PROVIDER_NAME+" TEXT,"+
     			PERMISSION_NAME+" TEXT);"
     			); 
      	
      	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_appsAdInfo + 
 			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
 			PACKAGE_NAME +" TEXT," +
 			APP_VERSION +" INT4,"+
 			AD_PROVIDER_NAME+" TEXT);"
 			); 
      	
       	db.execSQL("CREATE TABLE " + TABLE_NAME_OF_appUseOperate + 
      			 " ("+FIELD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
      			 PACKAGE_NAME +" TEXT," +
      			 IS_BLOCKED_AD+" INT4);"
      			); 
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_permission);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_appsInfo);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_adProviderInfo);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_adClassInfo);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_adPermission);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_appsAdInfo);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_appUseOperate);
		onCreate(db);
    }
}
