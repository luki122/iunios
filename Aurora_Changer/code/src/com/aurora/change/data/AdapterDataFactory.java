package com.aurora.change.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.aurora.change.activities.WallpaperLocalActivity;
import com.aurora.change.adapters.WallpaperLocalAdapter;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;

public class AdapterDataFactory {

    private static final String TAG = "AdapterDataFactory";
//    private static AdapterDataFactory sDataFactory;
    private Context mContext;
    private DbControl mDbControl;
    private String mType;
    private static CommonLog log = LogFactory.createLog(TAG);
    private WallpaperLocalAdapter mLocalAdapter;
    private List<PictureGroupInfo> mGroupInfos;
    private List<PictureInfo> mPictureInfos;
    private List<List<PictureInfo>> mAllPictureInfos;

    public AdapterDataFactory(Context context, String wallpaperType) {
        Log.d("liugj", "DATA=AdapterDataFactory()");
        mContext = context;
        mDbControl = new DbControl(context);
        mType = wallpaperType;
    }

//    public static synchronized AdapterDataFactory getInstance(Context context, String wallpaperType) {
//        if (sDataFactory == null) {
//            sDataFactory = new AdapterDataFactory(context, wallpaperType);
//        }
//        return sDataFactory;
//    }

    public void initWallpaperItems() {
        boolean isFirst = DataOperation.getBooleanPreference(mContext, Consts.IS_FIRST_START, true);
        Log.d("liugj", "initWallpaperItems : "+isFirst);
        //shigq add start
        String version = WallpaperConfigUtil.getConfigVersion();
        //shigq add end
        
        if (isFirst) {
            findSystemDesktopWallpapers();
            //findSystemLockWallpapers();
            //DataOperation.getInstance(mContext).putBoolean(Consts.IS_FIRST_START, false);
            DataOperation.setBooleanPreference(mContext, Consts.IS_FIRST_START, false);
//            findSdCardLockWallpapers();
            delSdCardLockWallpapers();
            
            //if recover to factory, need to clear the version
            version = "first";
        }
        
        //shigq add start
//        String version = WallpaperConfigUtil.getConfigVersion();
        String currentVersion = DataOperation.getStringPreference(mContext, Consts.WALLPAPER_VERSION, null);
        Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------initWallpaperItems-------version = "+version+" currentVersion = "+currentVersion);
        if (currentVersion == null && version != null) {
        	Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------initWallpaperItems-------add configuration files firstly and refresh DB!!!!!!!!!");
			DataOperation.setStringPreference(mContext, Consts.WALLPAPER_VERSION, version);
			mDbControl.refreshDb();
			
		} else if (currentVersion != null) {
			if(!currentVersion.equals(version)) {
				Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------initWallpaperItems-------update configuration files and need to refresh DB!!!!!!!!!");
				DataOperation.setStringPreference(mContext, Consts.WALLPAPER_VERSION, version);
				mDbControl.refreshDb();
			}
		}
        //shigq add end
        
        checkSdCardLockWallpapers();
        checkSystemDefaultWallpaper();
    }

    public WallpaperLocalAdapter getLocalAdapter() {
        Log.d("liugj", "DATA=getLocalAdapter");
//        mGroupInfos = mDbControl.queryAllGroupInfos();
        mGroupInfos = mDbControl.queryAllGroupInfosOrderBy();
        mAllPictureInfos = mDbControl.queryAllPictureInfosOrderBy();
        mLocalAdapter = new WallpaperLocalAdapter(mContext, mGroupInfos, mAllPictureInfos);
        return mLocalAdapter;
    }

    public List<PictureGroupInfo> getAllGroupInfos() {
        if (mGroupInfos == null) {
            mGroupInfos = mDbControl.queryAllGroupInfos();
        }
        return mGroupInfos;
    }

    public List<PictureInfo> getItemsByGroup(int group_id) {
        mPictureInfos = mDbControl.queryAllItemsByGroupId(group_id);
        return mPictureInfos;
    }

    public List<List<PictureInfo>> getAllPictureInfos() {
        if (mAllPictureInfos == null) {
            mAllPictureInfos = mDbControl.queryAllPictureInfos();
        }
        return mAllPictureInfos;
    }

    private void findSystemLockWallpapers() {
        try {
            mType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
            File file = new File(Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_PATH);
            getFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findSdCardLockWallpapers(){
        try {
            List<PictureGroupInfo> groupInfos = mDbControl.queryAllSDPictureGroupInos();
//            if (groupInfos.size() > 0) {
//                for (int i = 0; i < groupInfos.size(); i++) {
//                    Log.d("lockpaper", "groupInfo=" + groupInfos.toString());
//                }
//            }
//            if (null != groupInfos && groupInfos.size() > 0) {
                mDbControl.delAllSDPictureGroupInos();
//            }
            mType = Consts.WALLPAPER_LOCKSCREEN_TYPE;
            File file = new File(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
            getFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查数据库中保存的路径是否在SD卡中存在，未检查SD卡中存在的文件是否包含在数据库中
     */
    private void checkSdCardLockWallpapers(){
        try {
            List<PictureGroupInfo> groupInfos = mDbControl.queryAllSDPictureGroupInos();
            for (int i = 0; i < groupInfos.size(); i++) {
                String groupName = groupInfos.get(i).getDisplay_name();
                String groupPath = Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH + groupName;
                File file = new File(groupPath);
                Log.d("CheckSD", "SD File=" + file.toString());
                Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------checkSdCardLockWallpapers-------file.toString() = "+file.toString());
                
                //shigq add start
//                if (!file.exists() || (file.exists() && file.isDirectory() && file.list().length != groupInfos.get(i).getCount())) {
                if (!file.exists() || (file.exists() && file.isDirectory() && file.list().length - 1 != groupInfos.get(i).getCount())) {
                //shigq add end
                	
                    Log.d("CheckSD", "SD File Error=" + groupName);
                    if (!file.exists()) {
                        Log.d("CheckSD", "file not exists");
                    } else {
                        Log.d("CheckSD", "file length=" + file.list().length + ",groupCount=" + groupInfos.get(i).getCount());
                    }
                    mDbControl.delPictureGroupByName(groupName);
                    File fileCache = mContext.getCacheDir();
                    FileHelper.deleteDirectory(fileCache.toString());
                    FileHelper.deleteDirectory(groupPath);
                    
                    //shigq add start
//                  String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
                    String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, null);
                    if (current_group == null) {
                    	PictureGroupInfo groupInfo = mDbControl.queryDefaultGroup();
                    	
                    	if (groupInfo != null) {
                    		current_group = groupInfo.getDisplay_name();
                    	} else {
                    		current_group = Consts.DEFAULT_LOCKPAPER_GROUP;
                    	}
                    }
                    //shigq add end
                    
                    if (current_group.equals(groupName)) {
                        DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
                        
                        //shigq add start
                        DbControl mDbControl = new DbControl(mContext);
                        PictureGroupInfo groupInfo = mDbControl.queryGroupByName(Consts.DEFAULT_LOCKPAPER_GROUP);
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
                        mDbControl.close();
                        //shigq add end
                        
                        String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, Consts.DEFAULT_LOCKPAPER_GROUP);
                        
                        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//                      FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
                        FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
                        //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
                        
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private void checkSystemDefaultWallpaper(){
        File file = new File(Consts.LOCKSCREEN_WALLPAPER_PATH);
        //shigq add start
//        if (!file.exists()) {
//          String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
            String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, null);
            if (current_group == null) {
            	PictureGroupInfo groupInfo = mDbControl.queryDefaultGroup();
            	if (groupInfo != null) {
            		current_group = groupInfo.getDisplay_name();
    			} else {
    				current_group = Consts.DEFAULT_LOCKPAPER_GROUP;
    			}
			}
            
            Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------checkSystemDefaultWallpaper-------file.exists() = "+file.exists());
            Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------checkSystemDefaultWallpaper-------current_group = "+current_group);
            String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, current_group);
            Log.d("Wallpaper_DEBUG", "AdapterDataFactory---------checkSystemDefaultWallpaper-------currentPath = "+currentPath);
            
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//          FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
            FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
//        }
        //shigq add end
            
    }
    
    private void delSdCardLockWallpapers(){
        FileHelper.deleteDirectory(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
    }

    private void findSystemDesktopWallpapers() {
        try {
            mType = Consts.WALLPAPER_DESKTOP_TYPE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            String fileName = file.getName();
            log.d("name=" + fileName + ",parent=" + file.getParentFile().getName());
            if (isPictureFile(file)) {
                List<PictureGroupInfo> groupInfos = mDbControl.queryAllGroupInfos();
                for (int i = 0; i < groupInfos.size(); i++) {
                    if (groupInfos.get(i).getDisplay_name().equals(file.getParentFile().getName())) {
                        PictureInfo info = new PictureInfo();
                        String[] nameStrings = file.getName().split("\\.");
                        info.setIdentify(nameStrings[0]);
                        info.setBigIcon(file.getPath());
                        info.setBelongGroup(groupInfos.get(i).getId());
                        mDbControl.insertPicture(info);
                    }
                }
            }

        } else {
            File[] files = file.listFiles();
            if (null != files && mType.equals(file.getParentFile().getName()) && files.length > 0) {
                boolean isSystem = false;
                PictureGroupInfo groupInfo = new PictureGroupInfo();
                groupInfo.setDisplay_name(file.getName());
                groupInfo.setCount(files.length);
                Log.d("lockpaper", "path=" + file.getPath() + ",parentPath=" + file.getParent());
                if (file.getPath().contains(Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_FLAG)) {
                    Log.d("lockpaper", "path=" + file.getPath() + ",parentPath=" + file.getParent());
                    isSystem = true;
                }
                mDbControl.insertPictureGroup(groupInfo, isSystem);
            }
            if (files != null) {
                for (File sFile : files) {
                    getFile(sFile);
                }
            }
        }
    }
    /*private void getFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            String fileName = file.getName();
            log.d("name=" + fileName + ",parent=" + file.getParentFile().getName());
            if (isPictureFile(file)) {
                List<PictureGroupInfo> groupInfos = mDbControl.queryAllGroupInfos();
                List<PictureInfo> pictureInfos = new ArrayList<PictureInfo>();
                for (int i = 0; i < groupInfos.size(); i++) {
                    if (groupInfos.get(i).getDisplay_name().equals(file.getParentFile().getName())) {
                        PictureInfo info = new PictureInfo();
                        info.setIdentify(file.getName());
                        info.setBigIcon(file.getPath());
                        info.setBelongGroup(groupInfos.get(i).getId());
                        pictureInfos.add(info);
//                        mDbControl.insertPicture(info);
                    }
                }
                Collections.sort(pictureInfos, new ComparatorFileName());
            }
            
        } else {
            File[] files = file.listFiles();
            if (null != files && mType.equals(file.getParentFile().getName()) && files.length > 0) {
                boolean isSystem = false;
                PictureGroupInfo groupInfo = new PictureGroupInfo();
                groupInfo.setDisplay_name(file.getName());
                groupInfo.setCount(files.length);
                if (file.getAbsolutePath().contains(Consts.DEFAULT_SYSTEM_LOCKSCREEN_WALLPAPER_PATH)) {
                    isSystem = true;
                }
                mDbControl.insertPictureGroup(groupInfo, isSystem);
            }
            if (files != null) {
                for (File sFile : files) {
                    getFile(sFile);
                }
            }
        }
    }*/

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

    public void clearData() {
        mDbControl.close();
//        sDataFactory = null;
    }
    
    
    class ComparatorFileName implements Comparator<PictureInfo> {

        @Override
        public int compare(PictureInfo lhs, PictureInfo rhs) {
            return lhs.getIdentify().compareTo(rhs.getIdentify());
        }
    }
}
