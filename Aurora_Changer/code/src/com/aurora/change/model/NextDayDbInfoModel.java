package com.aurora.change.model;

import android.provider.BaseColumns;

public class NextDayDbInfoModel {

//    public static final String LOCK_WALLPAPER_GROUP_TABLE_NAME = "lockpaper_group";
//    public static final String LOCK_WALLPAPER_TABLE_NAME = "lockpaper";
//    public static final String DESKTOP_WALLPAPER_TABLE_NAME = "desktop_wallpaper";
//    public static final String FK_INSERT_GROUP = "fk_insert_group";
//    public static final String FK_DELETE_GROUP = "fk_delete_group";
	public static final String NEXTDAY_TABLE_NAME = "nextday_picture_info";
//	public static final String FK_UPDATE_TRIGGER = "update_trigger";
//	public static final String DELETE_TRIGGER = "delete_trigger";
	public static final String DATABASE_NAME = "nextday.db";
	public static final int DATABASE_VERSION = 1;

    public static class PictureColumns implements BaseColumns {
    	
    	public static final String NAME 			=	"picture_name";
    	public static final String TIME 			=	"picture_time";
    	public static final String DIMENSION 		= 	"picture_dimension";
    	public static final String THUMNAIL_URL 	= 	"picture_thumnail_url";
    	public static final String ORIGINAL_URL 	= 	"picture_original_url";
    	public static final String TIME_BLACK 		= 	"picture_time_black";
    	public static final String STATUSBAR_BLACK 	= 	"picture_statusbar_black";
    	public static final String COMMENT_CITY 	= 	"picture_comment_city";
    	public static final String COMMENT 			= 	"picture_comment";
    	
    	/**
         * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY
         * COLUMNS
         */
    	public static final int NAME_INDEX 				= 	0;
    	public static final int TIME_INDEX 				= 	1;
    	public static final int DIMENSION_INDEX 		= 	2;
    	public static final int THUMNAIL_URL_INDEX 		= 	3;
    	public static final int ORIGINAL_URL_INDEX		=	4;
    	public static final int TIME_BLACK_INDEX		= 	5;
    	public static final int STATUSBAR_BLACK_INDEX 	= 	6;
    	public static final int COMMENT_CITY_INDEX 		= 	7;
    	public static final int COMMENT_INDEX 			= 	8;
    }
}