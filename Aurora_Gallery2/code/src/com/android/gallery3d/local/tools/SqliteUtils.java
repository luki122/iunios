package com.android.gallery3d.local.tools;

import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

import android.R.string;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;

public class SqliteUtils {
	
	private static int AURORA_PIC_SIZE = 20 * 1024;
	public static int ImageType = 0;
	public static int VideoType = 1;
	
	public  static CursorLoader buildCursorLoader(int id,Context mContext){
		return buildCursorLoader(id, mContext, null);
	}
	
	public  static CursorLoader buildCursorLoader(int id,Context mContext,String path){
		if (id == ImageType) {// Image
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String where = " (" + MediaColumns.SIZE + " > " + AURORA_PIC_SIZE + " or " + FileColumns.DATA + " LIKE '" + GalleryLocalActivity.sdPath + "/QQ_Screenshot%' " +" or "+FileColumns.DATA + " LIKE '" + GalleryLocalActivity.dcimPath + "%' "+ " or favorite = 1" + " ) and "
					+ FileColumns.DATA + " NOT LIKE '%/aurora/change/lockscreen%' and " + FileColumns.DATA + " NOT LIKE '" + GalleryLocalActivity.sdPath + "/iuni/wallpaper/save%' and " + FileColumns.DATA
					+ " NOT LIKE '%.pcx' and " + FileColumns.DATA + " NOT LIKE '%.tif'  "+((!TextUtils.isEmpty(path))?(" and "+FileColumns.DATA+" LIKE '"+path+"/%' and "+FileColumns.DATA+" NOT LIKE '"+path+"/%/%'"):"");
			String[] cons = new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.SIZE, MediaStore.Images.ImageColumns.TITLE,
					MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, "favorite" };
			String sortOrder = MediaStore.Images.ImageColumns.DATE_TAKEN + " desc , " + MediaStore.Images.ImageColumns._ID + " desc ";
			return new CursorLoader(mContext, uri, cons, where, null, sortOrder);
		} else if (id == VideoType) {
			Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			String where =" ("+ MediaStore.Video.VideoColumns.DATA + " LIKE '" + GalleryLocalActivity.dcimPath + "%' or favorite = 1 )"+((!TextUtils.isEmpty(path))?(" and "+FileColumns.DATA+" LIKE '"+path+"/%' and "+FileColumns.DATA+" NOT LIKE '"+path+"/%/%'"):"");
			String[] cons = new String[] { MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.SIZE, MediaStore.Video.VideoColumns.TITLE,
					MediaStore.Video.VideoColumns.DATE_TAKEN ,"favorite"};
			String sortOrder = MediaStore.Video.VideoColumns.DATE_TAKEN + " desc , " + MediaStore.Video.VideoColumns._ID + " desc ";
			return new CursorLoader(mContext, uri, cons, where, null, sortOrder);
		}
		return null;
	}
	
	/**
	 * 收藏CursorLoader
	 * @param mContext
	 * @return
	 */
	public static CursorLoader buildFavoriteCursorLoader(Context mContext){
		Uri uri =MediaStore.Files.getContentUri(MediaFileOperationUtil.external);
		String where = " favorite=1";
		String[] cons = new String[] { MediaStore.Files.FileColumns._ID,  MediaStore.Files.FileColumns.DATA,  MediaStore.Files.FileColumns.SIZE,  MediaStore.Files.FileColumns.TITLE,
				MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.ORIENTATION, "favorite",FileColumns.MEDIA_TYPE };
		String sortOrder = MediaStore.Images.ImageColumns.DATE_TAKEN + " desc , " + MediaStore.Images.ImageColumns._ID + " desc ";
		return new CursorLoader(mContext, uri, cons, where, null, sortOrder);
	}
	
	
	
}
