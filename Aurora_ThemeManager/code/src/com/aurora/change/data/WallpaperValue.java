package com.aurora.change.data;

import android.net.Uri;
import android.os.Environment;

public class WallpaperValue {
	public static final String WALLPAPER_PATH = Environment.getExternalStorageDirectory() + "/IUNI/wallpaper/save/";
	public static final String WALLPAPER_ASSETS_DIR = "wallpaper/";
	public static final String WALLPAPER_ID = "_id";
	public static final String WALLPAPER_MODIFIED = "modified";
	public static final String WALLPAPER_OLDPATH = "oldpath";
	public static final String WALLPAPER_FILENAME = "filename";
	public static final String WALLPAPER_SELECTED = "seleted";
	public static final String WALLPAPER_URI_TYPE = "vnd.android.cursor.dir/wallpaper";
	public static final Uri LOCAL_WALLPAPER_URI = Uri.parse("content://com.aurora.change.provider/wallpaper");
	
	public static final String ACTION_WALLPAPER_SET = "com.aurora.action.WALLPAPER_SET";
}
