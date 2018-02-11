package com.aurora.thememanager.utils.themehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.model.ThemeInfo;
import com.aurora.change.receiver.ChangeReceiver;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;
import com.aurora.change.view.CropImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.utils.download.DatabaseController;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadService;

public class LockScreenApplyHelper {

	private static final String TAG = "LockScreenApplyHelper";
	
	private Context mContext;
	
	private String mGroupName;
	
	private DownloadData mDownloadData;
	
	public LockScreenApplyHelper(Context context,DownloadData data){
		this.mContext = context;
		mGroupName = getGroupName();
		mDownloadData = data;
	}
	
	
	/**
	 * 获取时光锁屏的壁纸组名
	 * @return
	 */
	  private String getGroupName() {
	        DbControl control = new DbControl(mContext);
	        List<PictureGroupInfo> groupInfos = control.queryAllGroupInfos();
	        StringBuffer group_name = new StringBuffer(Consts.DEFAULT_LOCKPAPER_FILE_NAME)/*getString(R.string.wallpaper_crop_custom_name)*/;
	        int id = 1;
	        if (groupInfos != null && groupInfos.size() != 0) {
	            String groupName = groupInfos.get(groupInfos.size() - 1).getDisplay_name();
	            if (groupName.contains(Consts.DEFAULT_LOCKPAPER_FILE_NAME)) {
					int currentNumber = Integer.valueOf(groupName.replace(Consts.DEFAULT_LOCKPAPER_FILE_NAME, ""));
					id = currentNumber + 1;
					
				} else {
					id = groupInfos.get(groupInfos.size() - 1).getId() + 1;
				}
	        }
	        if (id < 10) {
	            group_name.append("0").append(id);
	        } else {
	        	group_name.append(id);
	        }
	        control.close();
	        return group_name.toString();
	    }
	
	  /**
	   * 应用时光锁屏
	   * @param imageList
	   * 					存放时光锁屏的List
	   * @return
	   */
	public boolean applyTimeWallpaper(List<InputStream> imageList) {
		  String groupName = mGroupName;
		  boolean bool = false;
		  int index = 0;
		  for(InputStream input:imageList){
			  index+=1;
			  String fileName = "";
			  if(index < 9){
				  fileName = "data0"+index;
			  }else{
				  fileName = "data"+index;
			  }
			 
				Bitmap bitmap = cropLockScreenWallpaper(input);
				if (bitmap == null) {
					return bool;
				}
				DbControl control = new DbControl(mContext);
				StringBuffer path = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
				path.append(groupName).append("/").append(fileName).append(".png");
				bool = FileHelper.writeImage(bitmap, path.toString(), 100);
				StringBuffer filePath = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
				filePath.append(groupName).append("/").append(Consts.LOCKPAPER_SET_FILE);
				
				ThemeInfo mThemeInfo = new ThemeInfo();
				mThemeInfo.name = groupName;
				String defaultGroup = Consts.DEFAULT_LOCKPAPER_GROUP;
				Log.d(TAG, "saveImageFile==============defaultGroup = "+defaultGroup);
	            mThemeInfo.timeBlack = "false";
				String fileString = WallpaperConfigUtil.creatWallpaperConfigurationXmlFile(filePath.toString(), mThemeInfo);
				if (imageList != null) {
					updatePictrueGroupDatabase(control, mThemeInfo, imageList.size());
					updatePictrueDatabase(control, groupName, fileName, path.toString());
				}
				control.close();
				if (bitmap != null) {
					if (!bitmap.isRecycled()) {
						bitmap.recycle();
					}
					bitmap = null;
				}
				  
		  }
		  applyLockScreenWallpaper(imageList);
		return bool;
	}
	
	
	/**
	 * 从sdcard中解析壁纸
	 * @param file
	 * @return
	 */
	private Bitmap cropLockScreenWallpaper(InputStream file){
		Bitmap bitmap = BitmapFactory.decodeStream(file);
		return bitmap;
	}
	
	/**
	 * 应用当前壁纸为时光锁屏
	 * @param imageList
	 */
	 private void applyLockScreenWallpaper(List<InputStream> imageList){
		   if (imageList != null && (imageList.size() > 0)) {
               DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, mGroupName);
               DbControl mDbControl = new DbControl(mContext);
               PictureGroupInfo groupInfo = mDbControl.queryGroupByName(mGroupName);
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
               String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, mGroupName);
               boolean res = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
             
           } 
	}
	
	
	
    private void updatePictrueGroupDatabase(DbControl control, ThemeInfo mThemeInfo, int group_count) {
        PictureGroupInfo groupInfo = new PictureGroupInfo();
        groupInfo.setDisplay_name(mThemeInfo.name);
        groupInfo.setThemeColor(mThemeInfo.nameColor);
        groupInfo.setIsDefaultTheme("false".equals(mThemeInfo.isDefault)? 0 : 1);
        groupInfo.setIsTimeBlack("false".equals(mThemeInfo.timeBlack)? 0 : 1);
        groupInfo.setIsStatusBarBlack("false".equals(mThemeInfo.statusBarBlack)? 0 : 1);
        groupInfo.setCount(group_count);
        groupInfo.downloadId = mDownloadData.downloadId;
        groupInfo.downloadPkgPath = mDownloadData.fileDir+File.separatorChar+mDownloadData.fileName;
        control.insertPictureGroup(groupInfo, false);
    }

    private void updatePictrueDatabase(DbControl control, String group_name, String pictureTitle, String path) {
        PictureGroupInfo belong_group = control.queryGroupByName(group_name);
        int belong_id = belong_group.getId();
        PictureInfo pictureInfo = new PictureInfo();
        pictureInfo.setBelongGroup(belong_id);
        pictureInfo.setIdentify(pictureTitle);
        pictureInfo.setBigIcon(path);
        control.insertPicture(pictureInfo);
    }
	
	
	
	public void setUnApplied(DownloadData data){
		DatabaseController dbController = DownloadService.getDownloadController();
	    if(dbController == null){
	    	dbController = DatabaseController.getController(mContext, DatabaseController.TYPE_DOWNLOAD);
	    	dbController.openDatabase();
	    }
	    dbController.setUnAppApplied(data.downloadId);
	}
	
	
	
	
	
	
	
	
	
	
	
}
