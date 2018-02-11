package com.android.auroramusic.db;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class AuroraDbHelper extends SQLiteOpenHelper{
	
	public AuroraDbHelper(Context context) {
		super(context, AuroraDbData.DATABASE_NAME, null, AuroraDbData.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建歌曲表 (INTEGER PRIMARY KEY AUTOINCREMENT,)
		/*db.execSQL("CREATE TABLE IF NOT EXISTS " + AuroraDbData.SONG_TABLENAME + "("
				+ AuroraDbData.SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AuroraDbData.SONG_ALBUMID + " INTEGER," + AuroraDbData.SONG_ARTISTID
				+ " INTEGER," + AuroraDbData.SONG_NAME + " NVARCHAR(100),"
				+ AuroraDbData.SONG_DISPLAYNAME + " NVARCHAR(100),"
				+ AuroraDbData.SONG_NETURL + " NVARCHAR(500),"
				+ AuroraDbData.SONG_DURATIONTIME + " INTEGER," + AuroraDbData.SONG_SIZE
				+ " INTEGER," + AuroraDbData.SONG_ISLIKE + " INTEGER,"
				+ AuroraDbData.SONG_LYRICPATH + " NVARCHAR(300),"
				+ AuroraDbData.SONG_FILEPATH + " NVARCHAR(300),"
				+ AuroraDbData.SONG_PLAYERLIST + " NVARCHAR(500)," + AuroraDbData.SONG_ISNET
				+ " INTEGER," + AuroraDbData.SONG_MIMETYPE + " NVARCHAR(50),"
				+ AuroraDbData.SONG_ISDOWNFINISH + " INTEGER)");*/
		db.execSQL("CREATE TABLE IF NOT EXISTS " + AuroraDbData.FAVORITES_TABLENAME + "("
				+ AuroraDbData.FAVORITES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AuroraDbData.FAVORITES_AUDIO_ID + " INTEGER ,"
				+ AuroraDbData.FAVORITES_PLAY_ORDER + " INTEGER,"
				+ AuroraDbData.FAVORITES_TITLE + " TEXT,"
				+ AuroraDbData.FAVORITES_ALBUMNAME + " TEXT,"
				+ AuroraDbData.FAVORITES_ARTISTNAME + " TEXT,"
				+ AuroraDbData.FAVORITES_URI + " TEXT,"
				+ AuroraDbData.FAVORITES_ISNET + " INTEGER "
				+ ")");
		
		//添加共享数据表
		db.execSQL("CREATE TABLE IF NOT EXISTS " + AuroraDbData.SHARE_TABLENAME + "("
				+ AuroraDbData.SHARE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AuroraDbData.SHARE_ISPLAYING + " INTEGER "
				+ ")");
		
		//添加歌词表
		db.execSQL("CREATE TABLE IF NOT EXISTS " + AuroraDbData.AUDIOINFO_TABLENAME + "("
				+ AuroraDbData.AUDIOINFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AuroraDbData.AUDIOINFO_SONG_ID + " INTEGER ,"
				+ AuroraDbData.AUDIOINFO_SONG_TITLE + " TEXT,"
				+ AuroraDbData.AUDIOINFO_SONG_ARTIST + " TEXT,"
				+ AuroraDbData.AUDIOINFO_SONG_LRC + " TEXT,"
				+ AuroraDbData.AUDIOINFO_SONG_ALBUMPIC + " TEXT,"
				+ AuroraDbData.AUDIOINFO_SONG_ISNET + " INTEGER"
				+ ")");
		
		// 添加默认列表
		
		// 创建专辑图片表
		
		// 创建歌手表
		
		// 创建播放列表
		
		//add by chenhl 20140711 start
		//创建收藏歌单列表
		db.execSQL("CREATE TABLE IF NOT EXISTS "+AuroraDbData.COLLECT_TABLENAME +"("
				+ AuroraDbData.COLLECT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AuroraDbData.COLLECT_NAME+" TEXT,"
				+ AuroraDbData.COLLECT_IMG+ " TEXT,"
				+ AuroraDbData.COLLECT_SONG_SIZE+ " INTEGER,"
				+ AuroraDbData.COLLECT_SHOU_INFO+" TEXT ,"
				+ AuroraDbData.COLLECT_PLAYLISTID+" TEXT,"
				+ AuroraDbData.COLLECT_LIST_TYPE+" INTEGER,"
				+ AuroraDbData.COLLECT_TYPE+" TEXT"
				+ ")");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ AuroraDbData.SEARCH_HISTORY_TABLENAME + "("
				+ AuroraDbData.SEARCH_HISTORY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ AuroraDbData.SEARCH_HISTORY_KEY + " TEXT" + ")");
		//add by chenhl 20140711 end
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.ALBUM_TABLENAME);
		db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.ARTIST_TABLENAME);
		db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.PLAYERLIST_TABLENAME);*/
		db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.AUDIOINFO_TABLENAME);
		db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.FAVORITES_TABLENAME);
		db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.SHARE_TABLENAME);
		db.execSQL("DROP TABLE IF EXISTS " + AuroraDbData.COLLECT_TABLENAME);//add by chenhl 20140711
		db.execSQL("DROP TABLE IF EXISTS "+AuroraDbData.SEARCH_HISTORY_TABLENAME);//add by chenhl 20140813
		onCreate(db);
	}

}
