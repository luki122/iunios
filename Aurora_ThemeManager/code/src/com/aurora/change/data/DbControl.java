package com.aurora.change.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.util.Log;

import com.aurora.change.model.DbInfoModel;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;

import com.aurora.thememanager.R;

public class DbControl extends DbHelper {

    private static CommonLog log = LogFactory.createLog("DbControl");
    private Context mContext;

    public DbControl(Context context) {
    	super(context);
        //super.openDb(context);
        mContext = context;
    }

    public void close() {
        super.closeDb();
    }

    public boolean insertPicture(PictureInfo info) {
        long id = 0;
        ContentValues values = new ContentValues();
        values.put(DbInfoModel.ImageColumns.IDENTIFY, info.getIdentify());
        values.put(DbInfoModel.ImageColumns.PATH, String.valueOf(info.getBigIcon()));
        values.put(DbInfoModel.ImageColumns.BELONG_GROUP, info.getBelongGroup());
        if (mSqlDb.isOpen()) {
            id = mSqlDb.insert(DbInfoModel.LOCK_WALLPAPER_TABLE_NAME, null, values);
        }
        return id > 0 ? true : false;
    }

    public boolean insertPictureGroup(PictureGroupInfo groupInfo, boolean isSystem) {
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
        values.put(DbInfoModel.GroupColumns.DOWNLOAD_ID,groupInfo.downloadId);
        values.put(DbInfoModel.GroupColumns.DOWNLOADED_PKG_PATH,groupInfo.downloadPkgPath);
        //shigq add end
        
        if (isSystem) {
            values.put(DbInfoModel.GroupColumns.SYSTEM_FLAG, 1);
        }
        if (mSqlDb.isOpen()) {
            id = mSqlDb.insert(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null, values);
        }
        return id > 0 ? true : false;
    }

    public boolean delPictureByPath(String path) {
        long bool = 0;
        if (mSqlDb.isOpen()) {
            bool = mSqlDb.delete(DbInfoModel.LOCK_WALLPAPER_TABLE_NAME, DbInfoModel.ImageColumns.PATH + "=?",
                    new String[] {path});
        }
        return bool > 0 ? true : false;
    }

    public boolean delPictureGroupByID(int id) {
        long bool = 0;
        if (mSqlDb.isOpen()) {
            bool = mSqlDb.delete(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, DbInfoModel.GroupColumns.ID
                    + "=?", new String[] {id + ""});
        }
        return bool > 0 ? true : false;
    }

    public boolean delPictureGroupByName(String name) {
        long bool = 0;
        if (mSqlDb.isOpen()) {
            bool = mSqlDb.delete(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME,
                    DbInfoModel.GroupColumns.DISPLAY_NAME + "=?", new String[] {name});
        }
        return bool > 0 ? true : false;
    }

    public List<PictureGroupInfo> queryAllGroupInfos() {
        List<PictureGroupInfo> list = new ArrayList<PictureGroupInfo>();
        Cursor c = mSqlDb.query(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null, null, null, null, null,
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
            groupInfo.downloadId = c.getInt(DbInfoModel.GroupColumns.DOWNLOAD_ID_INDEX);
            groupInfo.downloadPkgPath = c.getString(DbInfoModel.GroupColumns.DOWNLOADED_PKG_PATH_INDEX);
            //shigq add end
            
            list.add(groupInfo);
        }
        c.close();
        return list;
    }

    public List<PictureGroupInfo> queryAllGroupInfosOrderBy() {
        List<PictureGroupInfo> list = new ArrayList<PictureGroupInfo>();
		 // Aurora liugj 2014-09-23 modified for bug-8003 start
        Cursor c = mSqlDb.query(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null, null, null, null, null,
                DbInfoModel.GroupColumns.SYSTEM_FLAG + " desc, " + DbInfoModel.GroupColumns.ID + " desc");
		 // Aurora liugj 2014-09-23 modified for bug-8003 end
        //shigq add start
        PictureGroupInfo tempInfo = null;
        //shigq add end
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
            groupInfo.downloadId = c.getInt(DbInfoModel.GroupColumns.DOWNLOAD_ID_INDEX);
            groupInfo.downloadPkgPath = c.getString(DbInfoModel.GroupColumns.DOWNLOADED_PKG_PATH_INDEX);
            //shigq add end
            
            //shigq add start
//            if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(groupInfo.getDisplay_name())) {
            if ("NextDay".equals(groupInfo.getDisplay_name())) {
				tempInfo = groupInfo;
				tempInfo.setDisplay_name(mContext.getResources().getString(R.string.nextday_wallpaper_name));
			} else {
				list.add(groupInfo);
			}
//            list.add(groupInfo);
            //shigq add end
        }
        c.close();
        
        //shigq add start
        if (tempInfo != null) {
			list.add(0, tempInfo);
		}
        //shigq add end
        
        return list;
    }

    public List<PictureInfo> queryAllItemsByGroupId(int belong_id) {
        List<PictureInfo> list = new ArrayList<PictureInfo>();
        Cursor c = mSqlDb
                .query(DbInfoModel.LOCK_WALLPAPER_TABLE_NAME, null, DbInfoModel.ImageColumns.BELONG_GROUP
                        + "=?", new String[] {belong_id + ""}, null, null, null);
        while (c.moveToNext()) {
            PictureInfo pictureInfo = new PictureInfo();
            pictureInfo.setId(c.getInt(DbInfoModel.ImageColumns.ID_INDEX));
            pictureInfo.setIdentify(c.getString(DbInfoModel.ImageColumns.IDENTIFY_INDEX));
            pictureInfo.setBigIcon(c.getString(DbInfoModel.ImageColumns.PATH_INDEX));
            pictureInfo.setBelongGroup(c.getInt(DbInfoModel.ImageColumns.BELONG_GROUP_INDEX));
//            log.d("pictureInfo=" + pictureInfo.toString());
            list.add(pictureInfo);
        }
        Collections.sort(list, new WallpaperUtil.PictureInfoComparator());
        c.close();
        return list;
    }

    public PictureGroupInfo queryGroupByName(String name) {
        PictureGroupInfo groupInfo = new PictureGroupInfo();
        Cursor c = mSqlDb.query(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null,
                DbInfoModel.GroupColumns.DISPLAY_NAME + "=?", new String[] {name}, null, null, null);
        while (c.moveToNext()) {
            groupInfo.setId(c.getInt(DbInfoModel.GroupColumns.ID_INDEX));
            groupInfo.setDisplay_name(c.getString(DbInfoModel.GroupColumns.DISPLAY_NAME_INDEX));
            groupInfo.setCount(c.getInt(DbInfoModel.GroupColumns.COUNT_INDEX));
            groupInfo.setSystem_flag(c.getInt(DbInfoModel.GroupColumns.SYSTEM_FLAG_INDEX));
            
            //shigq add start
            groupInfo.setThemeColor(c.getString(DbInfoModel.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            groupInfo.setIsDefaultTheme(c.getInt(DbInfoModel.GroupColumns.IS_DEFAULT_THEME_INDEX));
            groupInfo.setIsTimeBlack(c.getInt(DbInfoModel.GroupColumns.IS_TIME_BLACK_INDEX));
            groupInfo.setIsStatusBarBlack(c.getInt(DbInfoModel.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            groupInfo.downloadId = c.getInt(DbInfoModel.GroupColumns.DOWNLOAD_ID_INDEX);
            groupInfo.downloadPkgPath = c.getString(DbInfoModel.GroupColumns.DOWNLOADED_PKG_PATH_INDEX);
            //shigq add end
            
        }
        c.close();
        return groupInfo;
    }

    public List<List<PictureInfo>> queryAllPictureInfos() {
        List<List<PictureInfo>> lists = new ArrayList<List<PictureInfo>>();
        List<PictureGroupInfo> groupInfos = queryAllGroupInfos();
        for (int i = 0; i < groupInfos.size(); i++) {
            List<PictureInfo> pictureInfos = queryAllItemsByGroupId(groupInfos.get(i).getId());
            lists.add(pictureInfos);
        }
        return lists;
    }

    public List<List<PictureInfo>> queryAllPictureInfosOrderBy() {
        List<List<PictureInfo>> lists = new ArrayList<List<PictureInfo>>();
        List<PictureGroupInfo> groupInfos = queryAllGroupInfosOrderBy();
        for (int i = 0; i < groupInfos.size(); i++) {
            List<PictureInfo> pictureInfos = queryAllItemsByGroupId(groupInfos.get(i).getId());
            lists.add(pictureInfos);
        }
        return lists;
    }

    public List<PictureGroupInfo> queryAllSDPictureGroupInos() {
        List<PictureGroupInfo> lists = new ArrayList<PictureGroupInfo>();
        Cursor c = mSqlDb.query(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null,
                DbInfoModel.GroupColumns.SYSTEM_FLAG + "=?", new String[] {0 + ""}, null, null, null, null);
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
            groupInfo.downloadId = c.getInt(DbInfoModel.GroupColumns.DOWNLOAD_ID_INDEX);
            groupInfo.downloadPkgPath = c.getString(DbInfoModel.GroupColumns.DOWNLOADED_PKG_PATH_INDEX);
            //shigq add end
            
            lists.add(groupInfo);
        }
        c.close();
        return lists;
    }

    public boolean delAllSDPictureGroupInos() {
        long bool = mSqlDb.delete(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME,
                DbInfoModel.GroupColumns.SYSTEM_FLAG + "=?", new String[] {0 + ""});
        Log.d("DbControl", "delAllSDPictureGroupInos");
        return bool > 0;
    }
    
    //shigq add start
    public PictureGroupInfo queryDefaultGroup() {
        PictureGroupInfo groupInfo = new PictureGroupInfo();
        Cursor c = mSqlDb.query(DbInfoModel.LOCK_WALLPAPER_GROUP_TABLE_NAME, null,
                DbInfoModel.GroupColumns.IS_DEFAULT_THEME + "=?", new String[] {1 + ""}, null, null, null);
        
        if (c == null || c.getCount() <= 0) return null;
		
        while (c.moveToNext()) {
            groupInfo.setId(c.getInt(DbInfoModel.GroupColumns.ID_INDEX));
            groupInfo.setDisplay_name(c.getString(DbInfoModel.GroupColumns.DISPLAY_NAME_INDEX));
            groupInfo.setCount(c.getInt(DbInfoModel.GroupColumns.COUNT_INDEX));
            groupInfo.setSystem_flag(c.getInt(DbInfoModel.GroupColumns.SYSTEM_FLAG_INDEX));
            groupInfo.setThemeColor(c.getString(DbInfoModel.GroupColumns.DISPLAY_NAME_COLOR_INDEX));
            groupInfo.setIsDefaultTheme(c.getInt(DbInfoModel.GroupColumns.IS_DEFAULT_THEME_INDEX));
            groupInfo.setIsTimeBlack(c.getInt(DbInfoModel.GroupColumns.IS_TIME_BLACK_INDEX));
            groupInfo.setIsStatusBarBlack(c.getInt(DbInfoModel.GroupColumns.IS_STATUSBAR_BLACK_INDEX));
            groupInfo.downloadId = c.getInt(DbInfoModel.GroupColumns.DOWNLOAD_ID_INDEX);
            groupInfo.downloadPkgPath = c.getString(DbInfoModel.GroupColumns.DOWNLOADED_PKG_PATH_INDEX);
        }
        Log.d("Wallpaper_DEBUG", "DbControl----------queryDefaultGroup-------groupInfo.getDisplay_name() = "+groupInfo.getDisplay_name());
        Log.d("Wallpaper_DEBUG", "DbControl----------queryDefaultGroup-------groupInfo.getIsDefaultTheme() = "+groupInfo.getIsDefaultTheme());
        c.close();
        return groupInfo;
    }
    
    public void updateNextDayDB(String insertPath, String deletePath) {
//    	File file = new File(Consts.NEXTDAY_WALLPAPER_PATH);
    	List<PictureGroupInfo> groupInfos = queryAllGroupInfos();
    	for (int i = 0; i < groupInfos.size(); i++) {
            if (groupInfos.get(i).getDisplay_name().equals("NextDay")) {
                boolean b = delPictureByPath(deletePath);
                
                long id = 0;
                ContentValues values = new ContentValues();
                String identify = insertPath.replace(Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator, "").replace(".jpg", "");
                values.put(DbInfoModel.ImageColumns.IDENTIFY, identify);
                values.put(DbInfoModel.ImageColumns.PATH, insertPath);
                values.put(DbInfoModel.ImageColumns.BELONG_GROUP, groupInfos.get(i).getId());
                if (mSqlDb.isOpen()) {
                    id = mSqlDb.insert(DbInfoModel.LOCK_WALLPAPER_TABLE_NAME, null, values);
                }
                
                Log.d("Wallpaper_DEBUG", "updateNextDayDB--------insert "+identify+" = "+id);
                
                break;
            }
        }
    }
    
    public boolean insertPicture(ContentValues values) {
        long id = 0;
        if (mSqlDb.isOpen()) {
            id = mSqlDb.insert(DbInfoModel.LOCK_WALLPAPER_TABLE_NAME, null, values);
        }
        return id > 0 ? true : false;
    }
    //shigq add end
    
}
