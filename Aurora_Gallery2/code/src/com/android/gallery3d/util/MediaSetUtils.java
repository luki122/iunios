/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.util;

import android.os.Environment;

import com.android.gallery3d.data.LocalAlbum;
import com.android.gallery3d.data.LocalMergeAlbum;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.GalleryApp;

import java.util.Comparator;
import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;

import java.lang.StringBuilder;

import android.provider.MediaStore.Images;
//paul modified
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;
import java.io.File;
import com.android.gallery3d.R;

public class MediaSetUtils {
    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();
	
    
    public static  int CAMERA_BUCKET_ID = 0;
	public static  int DCIM_BUCKET_ID = 0;
	
	//Aurora <SQF> <2014-10-14>  for NEW_UI begin
	public static Context mContext;
	public static void setContext(Context c) {
		mContext = c;
	}
	//Aurora <SQF> <2014-10-14>  for NEW_UI end
	
    //lory add
    /*public static final int ALLMEIDA_BUCKET_ID = GalleryUtils.getBucketId(
    		Environment.getExternalStorageDirectory().toString() +CAMERA_BUCKET_NAME);*/
    //lory modify 

    //protected static AbstractGalleryActivity mActivity;
    
    
    /*public static final int CAMERA_BUCKET_ID = GalleryUtils.getBucketId(
    		gionee.os.storage.GnStorageManager.getInstance(this).getInternalStoragePath() + "/DCIM");*/
    public static final int DOWNLOAD_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.DOWNLOAD);
    public static final int EDITED_ONLINE_PHOTOS_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.EDITED_ONLINE_PHOTOS);
    public static final int IMPORTED_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
            + BucketNames.IMPORTED);
    public static final int SNAPSHOT_BUCKET_ID = GalleryUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() +
            "/Pictures/Screenshots");

    private static final Path[] CAMERA_PATHS = {
            Path.fromString("/local/all/" + CAMERA_BUCKET_ID),
            Path.fromString("/local/image/" + CAMERA_BUCKET_ID),
            Path.fromString("/local/video/" + CAMERA_BUCKET_ID)};

    public static boolean isCameraSource(Path path) {
        return CAMERA_PATHS[0] == path || CAMERA_PATHS[1] == path
                || CAMERA_PATHS[2] == path;
    }

	//paul modified
	private static String DISPLAY_PATHS_IMAGE;
	private static String DISPLAY_PATHS_VIDEO;
	private static String mQueryPaths[];

	public static String LAST_SD_STORAGE = null;
	public static String EX_STORAGE = null;
	public static String ROOT_STORAGE = null;

	private static String LAST_SD_STORAGE_REPLACE = null;
	private static String EX_STORAGE_REPLACE = null;
	private static String ROOT_STORAGE_REPLACE = null;

	
	public static final String CAMERA_BUCKET_NAME = "/DCIM/";
	public static final String SNAPSHOT_BUCKET_NAME = "/Screenshots/"; //"/\u622a\u5c4f/";//Iuni <lory><2014-01-13> add begin for 截屏
    //Aurora <SQF> <2014-07-29>  for NEW_UI begin
	public static final String MTXX_BUCKET_NAME = "/MTXX/";
    //Aurora <SQF> <2014-07-29>  for NEW_UI end
    public static final String WEIXIN_BUCKET_NAME = "/tencent/MicroMsg/WeiXin";
    //wenyongzhe 2015.10.20
    public static final String QQ_BUCKET_NAME = "/Tencent/QQ_Images/";
    public static final String WEIBO_BUCKET_NAME = "/sina/weibo/weibo/";

		
	public static boolean refreshPath(Context c){
		String SD_STORAGE = gionee.os.storage.GnStorageManager.getInstance(c).getExternalStoragePath();
		if(LAST_SD_STORAGE == null){
			if(null == SD_STORAGE) return false;
		}else{
			if(LAST_SD_STORAGE.equals(SD_STORAGE)) return false;
		}
		getCameraBucketID(c);
		return true;
	}
	//Iuni <lory><2013-12-17> add begin

	public static String getDisplayPath(String path) {

			if (path == null){
				return "";
			}

			if(null == EX_STORAGE) return path;
			
			File file = new File(path);
			boolean isExistZeroPath = false;
			if (file.isDirectory()) {
				String[] tempPath = path.split("/");
				for (int i = 0; i < tempPath.length; i++) {
					if (tempPath[i].startsWith("0")) {
						isExistZeroPath = true;
						break;
					}
				}
			}
		    //Aurora <SQF> <2014-10-14>  for NEW_UI begin
			LAST_SD_STORAGE_REPLACE = mContext.getString(R.string.aurora_storage_external);
			EX_STORAGE_REPLACE = mContext.getString(R.string.aurora_storage_internal);
			ROOT_STORAGE_REPLACE = mContext.getString(R.string.aurora_storage_root);
			//Aurora <SQF> <2014-10-14>  for NEW_UI end
			
			
			if (LAST_SD_STORAGE != null && path.startsWith(LAST_SD_STORAGE)) {
				return ROOT_STORAGE_REPLACE + "/" + LAST_SD_STORAGE_REPLACE
						+ path.substring(LAST_SD_STORAGE.length());
			} else if (path.startsWith(EX_STORAGE) && !isExistZeroPath) {
				return ROOT_STORAGE_REPLACE + "/"  + EX_STORAGE_REPLACE
						+ path.substring(EX_STORAGE.length());
			} else if (path.startsWith(ROOT_STORAGE)) {
				return ROOT_STORAGE_REPLACE
						+ path.substring(ROOT_STORAGE.length());
			}
			
			return path;
			
	}

    public static void getCameraBucketID(Context c) {
		EX_STORAGE = gionee.os.storage.GnStorageManager.getInstance(c).getInternalStoragePath();
		if(EX_STORAGE != null) {
			CAMERA_BUCKET_ID = GalleryUtils.getBucketId(EX_STORAGE + CAMERA_BUCKET_NAME);
		} else {
			File file = Environment.getExternalStorageDirectory();
			if(null != file){
				EX_STORAGE = file.toString();
				CAMERA_BUCKET_ID = GalleryUtils.getBucketId(EX_STORAGE + CAMERA_BUCKET_NAME);
			}else{
				return;
			}
		}
		ROOT_STORAGE = EX_STORAGE.split("/")[0];
		LAST_SD_STORAGE = gionee.os.storage.GnStorageManager.getInstance(c).getExternalStoragePath();

		if(null == LAST_SD_STORAGE_REPLACE){
			LAST_SD_STORAGE_REPLACE = c.getString(R.string.aurora_storage_external);
			EX_STORAGE_REPLACE = c.getString(R.string.aurora_storage_internal);
			ROOT_STORAGE_REPLACE = c.getString(R.string.aurora_storage_root);
		}
		
		//paul modified
		if(null != LAST_SD_STORAGE){
			mQueryPaths = new String[]{
						EX_STORAGE + CAMERA_BUCKET_NAME + "%",
						EX_STORAGE + MTXX_BUCKET_NAME + "%",
						EX_STORAGE + SNAPSHOT_BUCKET_NAME + "%",
						EX_STORAGE + WEIXIN_BUCKET_NAME + "%",
						EX_STORAGE + WEIBO_BUCKET_NAME + "%",//wenyongzhe 2015.10.20
						EX_STORAGE + QQ_BUCKET_NAME + "%",//wenyongzhe 2015.10.20
						LAST_SD_STORAGE + CAMERA_BUCKET_NAME + "%",
						LAST_SD_STORAGE + MTXX_BUCKET_NAME + "%",
						LAST_SD_STORAGE + WEIXIN_BUCKET_NAME + "%",
						LAST_SD_STORAGE + WEIBO_BUCKET_NAME + "%",//wenyongzhe 2015.10.20
						LAST_SD_STORAGE + QQ_BUCKET_NAME + "%"//wenyongzhe 2015.10.20
					};
		}else{
			mQueryPaths = new String[]{
						EX_STORAGE + CAMERA_BUCKET_NAME + "%",
						EX_STORAGE + MTXX_BUCKET_NAME + "%",
						EX_STORAGE + SNAPSHOT_BUCKET_NAME + "%",
						EX_STORAGE + WEIXIN_BUCKET_NAME + "%",
						EX_STORAGE + WEIBO_BUCKET_NAME + "%",//wenyongzhe 2015.10.20
						EX_STORAGE + QQ_BUCKET_NAME + "%"//wenyongzhe 2015.10.20
					};
		}
		StringBuffer imagePathsStr = new StringBuffer("(");
		StringBuffer videoPathsStr = new StringBuffer("(");
		
		for(int i = 0; i < mQueryPaths.length; ++i){
			imagePathsStr.append(ImageColumns.DATA);
			imagePathsStr.append(" LIKE '" + mQueryPaths[i]);

			videoPathsStr.append(VideoColumns.DATA);
			videoPathsStr.append(" LIKE '" + mQueryPaths[i]);
			
			if(i + 1 < mQueryPaths.length){
				imagePathsStr.append("' OR ");
				videoPathsStr.append("' OR ");
			}else{
				imagePathsStr.append("'");
				videoPathsStr.append("'");

			}
		}
		

		imagePathsStr.append(") AND " + ImageColumns.SIZE + " > 0");
		videoPathsStr.append(") AND " + VideoColumns.SIZE + " > 0");
		
		imagePathsStr.append(") GROUP BY SUBSTR(_data,0,length(_data)");
		videoPathsStr.append(") GROUP BY SUBSTR(_data,0,length(_data)");
		
		DISPLAY_PATHS_IMAGE =  imagePathsStr.toString();
		DISPLAY_PATHS_VIDEO = videoPathsStr.toString();
		return;
	}
	public static String getImageQueryStr(Context c){
		refreshPath(c);
		
		return DISPLAY_PATHS_IMAGE;
	}

	public static String getVideoQueryStr(Context c){
		refreshPath(c);
		return DISPLAY_PATHS_VIDEO;
	}
	
	public static String[] geQueryParamStr(Context c){
		//refreshPath(c);
		return null;
	}

	//Iuni <lory><2013-12-17> add end
	
    // Sort MediaSets by name
    public static class NameComparator implements Comparator<MediaSet> {
        @Override
        public int compare(MediaSet set1, MediaSet set2) {
            int result = set1.getName().compareToIgnoreCase(set2.getName());
            if (result != 0) return result;
            return set1.getPath().toString().compareTo(set2.getPath().toString());
        }
    }
}
