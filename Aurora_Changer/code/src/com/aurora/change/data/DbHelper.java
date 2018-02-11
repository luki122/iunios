package com.aurora.change.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.aurora.change.R;
import android.os.SystemProperties;
import com.aurora.change.imagecache.DiskLruCache;
import com.aurora.change.imagecache.ImageCache;
import com.aurora.change.model.DbInfoModel;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.model.ThemeInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Files;
import android.util.Log;

public class DbHelper {
    private static final String TAG = "DbHelper";

    public DatabaseHelper mDBHelper;
    public SQLiteDatabase mSqlDb;
    private static final CommonLog log = LogFactory.createLog(TAG);
    
    public DbHelper(Context context) {
    	openDb(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private Context mContext;
		private boolean isUpdate = false;
		private String mType;
		
        public DatabaseHelper(Context context) {
            super(context, DbInfoModel.DATABASE_NAME, null, DbInfoModel.DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("liugj", "oncreate database " + db.getPath());
            db.execSQL("CREATE TABLE " + DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME + " ("
                    + DbInfoModel.GroupColumns.ID + " INTEGER PRIMARY KEY,"
                    + DbInfoModel.GroupColumns.DISPLAY_NAME + " TEXT UNIQUE, "
                    + DbInfoModel.GroupColumns.COUNT + " INTEGER NOT NULL, "
                    + DbInfoModel.GroupColumns.SYSTEM_FLAG + " INTEGER DEFAULT 0, "
                    + DbInfoModel.GroupColumns.REMARK + " TEXT, "
                    
                    //shigq add start
                    + DbInfoModel.GroupColumns.DISPLAY_NAME_COLOR + " TEXT,"
                    + DbInfoModel.GroupColumns.IS_DEFAULT_THEME + " INTEGER DEFAULT 0,"
                    + DbInfoModel.GroupColumns.IS_TIME_BLACK + " INTEGER DEFAULT 0,"
                    + DbInfoModel.GroupColumns.IS_STATUSBAR_BLACK + " INTEGER DEFAULT 0"
                    //shigq add end
                    
                    + ");");
            
            db.execSQL("CREATE TABLE " + DbInfoModel.LOCK_WALLPAPER_TABLE_NAME + " ("
                    + DbInfoModel.ImageColumns.ID + " INTEGER PRIMARY KEY,"
                    + DbInfoModel.ImageColumns.IDENTIFY + " TEXT, " + DbInfoModel.ImageColumns.PATH
                    + " TEXT UNIQUE NOT NULL, " + DbInfoModel.ImageColumns.BELONG_GROUP + " INTEGER NOT NULL, "
                    + DbInfoModel.ImageColumns.REMARK + " TEXT, " + "FOREIGN KEY" + "("
                    + DbInfoModel.ImageColumns.BELONG_GROUP + ") REFERENCES "
                    + DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME + "(" + DbInfoModel.GroupColumns.ID + ")"
                    + ");");
            
			  // Aurora liugj 2014-09-11 modified for bug-8203 start
            if (!isUpdate) {
            	db.execSQL("CREATE TABLE " + DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME + " ("
                        + WallpaperValue.WALLPAPER_ID + " INTEGER PRIMARY KEY,"
                        + WallpaperValue.WALLPAPER_MODIFIED + " INTEGER, "
                        + WallpaperValue.WALLPAPER_OLDPATH + " TEXT UNIQUE NOT NULL, " + WallpaperValue.WALLPAPER_FILENAME
                        + " TEXT UNIQUE NOT NULL" + ");");
			}
			// Aurora liugj 2014-09-11 modified for bug-8203 end
            
            // 创建插入触发器
            db.execSQL("CREATE TRIGGER " + DbInfoModel.FK_INSERT_GROUP + " BEFORE INSERT " + " ON "
                    + DbInfoModel.LOCK_WALLPAPER_TABLE_NAME + " FOR EACH ROW BEGIN"
                    + " SELECT CASE WHEN ((SELECT " + DbInfoModel.GroupColumns.ID + " FROM "
                    + DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME + " WHERE " + DbInfoModel.GroupColumns.ID
                    + "=" + "NEW." + DbInfoModel.ImageColumns.BELONG_GROUP + " ) IS NULL)"
                    + " THEN RAISE (ABORT,'Foreign Key Violation') END;" + "  END;");

            // 创建删除组触发器
            db.execSQL("CREATE TRIGGER " + DbInfoModel.FK_DELETE_GROUP + " BEFORE DELETE " + "ON "
                    + DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME + " FOR EACH ROW BEGIN" + " DELETE FROM "
                    + DbInfoModel.LOCK_WALLPAPER_TABLE_NAME + " WHERE "
                    + DbInfoModel.ImageColumns.BELONG_GROUP + "=OLD." + DbInfoModel.GroupColumns.ID + ";"
                    + " END;");
            
            boolean isFirst = DataOperation.getBooleanPreference(mContext, Consts.IS_FIRST_START, true);
            Log.d("liugj", isFirst + " = oncreate database = " + isUpdate);
            
            //shigq add start
            Log.d("Wallpaper_DEBUG", "DBHelper--------onCreat--------isFirst = "+isFirst + " isUpdate = " + isUpdate);
            String version = WallpaperConfigUtil.getConfigVersion();
            if (version != null) {
            	DataOperation.setStringPreference(mContext, Consts.WALLPAPER_VERSION, version);
			}
            //shigq add end
            
            if (isFirst) {
            	insertSystemLockWallpapers(db);
            	
            	//shigq add start
            	if ("false".equals(SystemProperties.get("phone.type.oversea"))) {
            		findNextDayWallpaper(db);
				}
//            	findNextDayWallpaper(db);
            	//shigq add end
            	
            } else if (isUpdate) {
            	/*insertSystemLockWallpapers(db);
            	findSdCardLockWallpapers(db);*/
            	
            	//shigq add start
            	if (WallpaperConfigUtil.getSwithFlag() && WallpaperConfigUtil.getDestinationPath() != null) {
            		refreshDbFromPath(WallpaperConfigUtil.getDestinationPath(), db);
            		WallpaperConfigUtil.setSwithFlag(false);
            		WallpaperConfigUtil.setDestinationPath(null);
            	} else {
	            	insertSystemLockWallpapers(db);
	            	if ("false".equals(SystemProperties.get("phone.type.oversea"))) {
	            		findNextDayWallpaper(db);
					}
//	            	findNextDayWallpaper(db);
	            	findSdCardLockWallpapers(db);
            	}
            	//shigq add end
            	
            	isUpdate = false;
            	try {
            		File cacheFile = ImageCache.getDiskCacheDir(mContext, Consts.IMAGE_LOCKSCREEN_CACHE_DIR);
            		if (cacheFile != null && cacheFile.exists()) {
            			DiskLruCache.deleteContents(cacheFile);
    					Log.d("liugj", "DbHelper clearImageCache ");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        	isUpdate = true;
            Log.d("liugj", "onUpgrade change database from version " + oldVersion + " to " + currentVersion);
            db.execSQL("DROP TABLE IF EXISTS " + DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DbInfoModel.LOCK_WALLPAPER_TABLE_NAME);
			  // Aurora liugj 2014-09-11 modified for bug-8203 start
            //db.execSQL("DROP TABLE IF EXISTS " + DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME);
			  // Aurora liugj 2014-09-11 modified for bug-8203 end
            db.execSQL("DROP TRIGGER IF EXISTS " + DbInfoModel.FK_INSERT_GROUP);
            db.execSQL("DROP TRIGGER IF EXISTS " + DbInfoModel.FK_DELETE_GROUP);
            onCreate(db);
        }
        
        private void insertSystemLockWallpapers(SQLiteDatabase db) {
        	try {
                mType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
                File file = new File(Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_PATH);
                getFile(db, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
    	}
        
        private void findSdCardLockWallpapers(SQLiteDatabase db){
            try {
                //mType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
                File file = new File(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
                getFile(db, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void getFile(SQLiteDatabase db, File file) {
            if (!file.exists()) {
                return;
            }
            if (file.isFile()) {
                String fileName = file.getName();
                //Log.d("liugj", "name=" + fileName + ",parent=" + file.getParentFile().getName());
                if (isPictureFile(file)) {
                    List<PictureGroupInfo> groupInfos = queryAllGroupInfos(db);
                    for (int i = 0; i < groupInfos.size(); i++) {
                        if (groupInfos.get(i).getDisplay_name().equals(file.getParentFile().getName())) {
                            PictureInfo info = new PictureInfo();
                            String[] nameStrings = file.getName().split("\\.");
                            info.setIdentify(nameStrings[0]);
                            info.setBigIcon(file.getPath());
                            info.setBelongGroup(groupInfos.get(i).getId());
                            insertPicture(db, info);
                        }
                    }
                }

            } else {
                File[] files = file.listFiles();
                
                //shigq add start
                //sort the file name for bug #12314
                if (file.isDirectory()) {
					File[] mFiles = file.listFiles();
					for (File mFile : mFiles) {
						if (mFile.getName().contains(Consts.DEFAULT_LOCKPAPER_FILE_NAME)) {
							Arrays.sort(files, new Comparator<File>() {
								@Override
								public int compare(File file1, File file2) {
									// TODO Auto-generated method stub
									return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
								}
							});
							break;
						}
					}
				}
                //shigq add end
                
                //shigq add start
                String tempName = file.getParentFile().getName();
//                if (null != files && mType.equals(file.getParentFile().getName()) && files.length > 0) {
                //add mType.equals(file.getParentFile().getName().replace(".", "")) for NextDay
                if (null != files && (mType.equals(file.getParentFile().getName()) || mType.equals(tempName.replace(".", ""))) && files.length > 0) {
                //shigq add end
                    boolean isSystem = false;
                    PictureGroupInfo groupInfo = new PictureGroupInfo();
                    groupInfo.setDisplay_name(file.getName());
                    groupInfo.setCount(files.length);
                    //shigq add start
                    for (File mFile : files) {
						if (mFile.getName().contains(".xml")) {
							groupInfo.setCount(files.length - 1);
							break;
						}
					}

                    Log.d("Wallpaper_DEBUG", "DBHelper--------getFile--------file.toString() = "+file.toString());
                    ThemeInfo mThemeInfo = WallpaperConfigUtil.parseSystemThemeByName(mContext, file.toString());
                    if (mThemeInfo == null) {
                    	mThemeInfo = new ThemeInfo();
					}
                    groupInfo.setThemeColor(mThemeInfo.nameColor);
                    groupInfo.setIsDefaultTheme("false".equals(mThemeInfo.isDefault)? 0 : 1);
                    groupInfo.setIsTimeBlack("false".equals(mThemeInfo.timeBlack)? 0 : 1);
                    groupInfo.setIsStatusBarBlack("false".equals(mThemeInfo.statusBarBlack)? 0 : 1);
                    
                    if (groupInfo.getIsDefaultTheme() == 1) {
                    	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, mThemeInfo.name);
                    	if (groupInfo.getIsTimeBlack() == 0) {
    						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "false");
    					} else {
    						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "true");
    					}
    					if (groupInfo.getIsStatusBarBlack() == 0) {
    						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "false");
    					} else {
    						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "true");
    					}
					}
                    //shigq add end
                    
                    //Log.d("liugj", "path=" + file.getPath() + ",parentPath=" + file.getParent());
                    //shigq add start
                    //add for wallpaper NextDay
//                    if (file.getPath().contains(Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_FLAG)) {
                    if (file.getPath().contains(Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_FLAG) || 
                    		file.getPath().contains("." + Consts.WALLPAPER_LOCKSCREEN_TYPE)) {
                    //shigq add end
                    	
                        //Log.d("liugj", "path=" + file.getPath() + ",parentPath=" + file.getParent());
                        isSystem = true;
                    }
                    insertPictureGroup(db, groupInfo, isSystem);
                }
                
                if (files != null) {
                    for (File sFile : files) {
                    	//shigq add start
                    	if ("false".equals(SystemProperties.get("phone.type.oversea"))) {
                    		if (Consts.WALLPAPER_LOCKSCREEN_TYPE.equals(sFile.getParentFile().getName())) {
                    			if (mContext.getResources().getString(R.string.lockscreen_city).equals(sFile.getName()) /*|| 
                            		mContext.getResources().getString(R.string.lockscreen_dream).equals(sFile.getName())*/) {
                					continue;
                				}
                    		}
                    		
                    	} else {
                    		if (Consts.WALLPAPER_LOCKSCREEN_TYPE.equals(sFile.getParentFile().getName())) {
                    			Log.d("Wallpaper_DEBUG", "file name  = "+sFile.getName());
                    			if (mContext.getResources().getString(R.string.lockscreen_cute).equals(sFile.getName()) ||
                    				mContext.getResources().getString(R.string.lockscreen_elegant).equals(sFile.getName()) || 
                    				mContext.getResources().getString(R.string.lockscreen_fascinating).equals(sFile.getName()) || 
                    				mContext.getResources().getString(R.string.lockscreen_misspuff).equals(sFile.getName())) {
        							continue;
        						}
                    		}
                    	}
                    	
                    	getFile(db, sFile);
                    	//shigq add end
                    	
                    }
                }
            }
        }
        
        private boolean isPictureFile(File file) {
            String filecode = "";
            boolean isPicture = false;
            try {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[2];
                if (inputStream.read(buffer) != -1) {
                    for (int i = 0; i < buffer.length; i++) {
                        filecode += Integer.toString((buffer[i] & 0xFF));
                    }
                    switch (Integer.parseInt(filecode)) {
                        case 255216: // fileType = "jpg";
                            isPicture = true;
                            break;
                        case 7173: // fileType = "gif";
                            isPicture = true;
                            break;
                        case 6677: // fileType = "bmp";
                            isPicture = true;
                            break;
                        case 13780: // fileType = "png";
                            isPicture = true;
                            break;
                        default:
                            isPicture = false;
                    }

                }
                inputStream.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return isPicture;
        }
        
        private List<PictureGroupInfo> queryAllGroupInfos(SQLiteDatabase db) {
            List<PictureGroupInfo> list = new ArrayList<PictureGroupInfo>();
            Cursor c = db.query(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null, null, null, null, null,
                    null);
            while (c.moveToNext()) {
                PictureGroupInfo groupInfo = new PictureGroupInfo();
                groupInfo.setId(c.getInt(DbInfoModel.GroupColumns.ID_INDEX));
                groupInfo.setDisplay_name(c.getString(DbInfoModel.GroupColumns.DISPLAY_NAME_INDEX));
                groupInfo.setCount(c.getInt(DbInfoModel.GroupColumns.COUNT_INDEX));
                groupInfo.setSystem_flag(c.getInt(DbInfoModel.GroupColumns.SYSTEM_FLAG_INDEX));
                
                //shigq add start
                groupInfo.setThemeColor(c.getString(DbInfoModel.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
                groupInfo.setIsDefaultTheme(c.getInt(DbInfoModel.GroupColumns.IS_DEFAULT_THEME_INDEX));
                groupInfo.setIsTimeBlack(c.getInt(DbInfoModel.GroupColumns.IS_TIME_BLACK_INDEX));
                groupInfo.setIsStatusBarBlack(c.getInt(DbInfoModel.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
                //shigq add end
                
                list.add(groupInfo);
            }
            c.close();
            return list;
        }
        
        private boolean insertPicture(SQLiteDatabase db, PictureInfo info) {
            long id = 0;
            ContentValues values = new ContentValues();
            values.put(DbInfoModel.ImageColumns.IDENTIFY, info.getIdentify());
            values.put(DbInfoModel.ImageColumns.PATH, String.valueOf(info.getBigIcon()));
            values.put(DbInfoModel.ImageColumns.BELONG_GROUP, info.getBelongGroup());
            if (db.isOpen()) {
                id = db.insert(DbInfoModel.LOCK_WALLPAPER_TABLE_NAME, null, values);
            }
            return id > 0 ? true : false;
        }
        
        private boolean insertPictureGroup(SQLiteDatabase db, PictureGroupInfo groupInfo, boolean isSystem) {
            long id = 0;
            ContentValues values = new ContentValues();
            values.put(DbInfoModel.GroupColumns.DISPLAY_NAME, groupInfo.getDisplay_name());
            values.put(DbInfoModel.GroupColumns.COUNT, groupInfo.getCount());
            
            //shigq add start
            values.put(DbInfoModel.GroupColumns.DISPLAY_NAME_COLOR, groupInfo.getThemeColor());
            if (WallpaperConfigUtil.getSwithFlag()) {
				groupInfo.setIsDefaultTheme(1);
			}
            values.put(DbInfoModel.GroupColumns.IS_DEFAULT_THEME, groupInfo.getIsDefaultTheme());
            values.put(DbInfoModel.GroupColumns.IS_TIME_BLACK, groupInfo.getIsTimeBlack());
            values.put(DbInfoModel.GroupColumns.IS_STATUSBAR_BLACK, groupInfo.getIsStatusBarBlack());
            //shigq add end
            
            if (isSystem) {
                values.put(DbInfoModel.GroupColumns.SYSTEM_FLAG, 1);
            }
            if (db.isOpen()) {
                id = db.insert(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null, values);
            }
            return id > 0 ? true : false;
        }
        
        //shigq add start
        public void refreshDb(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DbInfoModel.LOCK_WALLPAPER_TABLE_NAME);
    		  // Aurora liugj 2014-09-11 modified for bug-8203 start
//            db.execSQL("DROP TABLE IF EXISTS " + DbInfoModel.DESKTOP_WALLPAPER_TABLE_NAME);
    		  // Aurora liugj 2014-09-11 modified for bug-8203 end
            db.execSQL("DROP TRIGGER IF EXISTS " + DbInfoModel.FK_INSERT_GROUP);
            db.execSQL("DROP TRIGGER IF EXISTS " + DbInfoModel.FK_DELETE_GROUP);
            onCreate(db);
        }
        
        public void refreshDbFromPath(String path, SQLiteDatabase db) {
        	try {
                mType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
                File file = new File(path);
                getFile(db, file);
            } catch (Exception e) {
            	Log.d("Wallpaper_DEBUG", "DBHelper--------onCreat--------refreshDbFromPath e = "+e);
            }
        }
        
        public void findNextDayWallpaper(SQLiteDatabase db) {
        	try {
                mType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
                File file = new File(Consts.NEXTDAY_WALLPAPER_PATH);
                Log.d("Wallpaper_DEBUG", "DBHelper--------getFile--------findNextDayWallpaper = "+file.exists());
                getFile(db, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //shigq add end
        
    }

    public void openDb(Context context) {
        mDBHelper = new DatabaseHelper(context);
        try {
            mSqlDb = mDBHelper.getWritableDatabase();
        } catch (Exception e) {
            mSqlDb = mDBHelper.getReadableDatabase();
            e.printStackTrace();
        }
        Log.d("liugj", "DATA=openDb()");
    }

    public void closeDb() throws SQLiteException {
        if (mDBHelper != null) {
            mDBHelper.close();
            mDBHelper = null;
        }
        if (mSqlDb != null) {
            mSqlDb.close();
            mSqlDb = null;
        }
    }
    
    //shigq add start
    public void refreshDb() {
    	Log.d("Wallpaper_DEBUG", "DBHelper--------onCreat--------refreshDb");
    	if (mDBHelper != null && mSqlDb != null) {
    		mDBHelper.isUpdate = true;
			mDBHelper.refreshDb(mSqlDb);
		}
    }
    //shigq add end
    
}
