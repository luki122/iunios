package com.android.settings.fingerprint.beam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.aurora.utils.SettingFileUtile;

public class AuroraFingerprintSqlite extends CustomSQLiteOpenHelper {
    private static final String DATABASE_PATH = "data/aurora/com.android.settings/databases/";
    private static final String DATABASE_NAME = "AuroraFingerprint.db";//数据库名
    private static final int DATABASE_VERSION = 3;//数据库版本号

    /**
     * 表名：id action
     */
    public static final String TABLE_NAME_OF_action = "fingerAction";

    //各表公共字段
    public static final String FIELD_ID = "_id";

    //"action"表 字段
    public static final String FINGER_ID = "finger_id";
    public static final String ACTION = "intent_action";
    public static final String ACTION_FLAG = "intent_action_flag";

    public AuroraFingerprintSqlite(Context context) {
        super(context, DATABASE_NAME,
                DATABASE_PATH,
                null,
                DATABASE_VERSION);
        if (!SettingFileUtile.fileIsExists(DATABASE_PATH + DATABASE_NAME)) {
            /**
             * 由于最开始数据库是存放在"data/data/"+Utils.getOwnPackageName(context)+"/databases"中的，
             * 后来为了防止清除用户数据时清掉数据库，于是改变了数据库存放的位置。
             * 这样就需要把原地址中的数据库复制到现在地址中来。
             */
            String fromPath = "data/data/com.android.settings/databases/";
            SettingFileUtile.copyFolder(fromPath, DATABASE_PATH);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_OF_action +
                " (" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FINGER_ID + " INT4," +
                ACTION + " TEXT," +
                ACTION_FLAG + " INT4);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OF_action);
        onCreate(db);
    }
}
