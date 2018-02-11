package com.aurora.change.model;

import android.provider.BaseColumns;

public class DbInfoModel {

    public static final String LOCK_WALLPAPER_GROUP_TABLE_NAME = "lockpaper_group";
    public static final String LOCK_WALLPAPER_TABLE_NAME = "lockpaper";
    public static final String DESKTOP_WALLPAPER_TABLE_NAME = "desktop_wallpaper";
    public static final String FK_INSERT_GROUP = "fk_insert_group";
    public static final String FK_DELETE_GROUP = "fk_delete_group";
    public static final String DATABASE_NAME = "wallpaper.db";
    public static final int DATABASE_VERSION = 11;

    public static class ImageColumns implements BaseColumns {

        public static final String ID = "image_id";
        public static final String IDENTIFY = "identify";
        public static final String PATH = "path";
        public static final String BELONG_GROUP = "belong_group";
        public static final String REMARK = "remark";

        /**
         * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY
         * COLUMNS
         */
        public static final int ID_INDEX = 0;
        public static final int IDENTIFY_INDEX = 1;
        public static final int PATH_INDEX = 2;
        public static final int BELONG_GROUP_INDEX = 3;
        public static final int REMARK_INDEX = 4;
    }

    public static class GroupColumns implements BaseColumns {

        public static final String ID = "group_id";
        public static final String DISPLAY_NAME = "display_name";
        public static final String COUNT = "count";
        public static final String SYSTEM_FLAG = "is_system";
        public static final String REMARK = "remark";
        
        //shigq add start
        public static final String DISPLAY_NAME_COLOR = "display_name_color";
        public static final String IS_DEFAULT_THEME = "is_default_theme";
        public static final String IS_TIME_BLACK = "is_time_black";
        public static final String IS_STATUSBAR_BLACK = "is_statusbar_black";
        //shigq add end

        /**
         * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY
         * COLUMNS
         */
        public static final int ID_INDEX = 0;
        public static final int DISPLAY_NAME_INDEX = 1;
        public static final int COUNT_INDEX = 2;
        public static final int SYSTEM_FLAG_INDEX = 3;
        public static final int REMARK_INDEX = 4;
        
        //shigq add start
        public static final int DISPLAY_NAME_COLOR_INDEX = 5;
        public static final int IS_DEFAULT_THEME_INDEX = 6;
        public static final int IS_TIME_BLACK_INDEX = 7;
        public static final int IS_STATUSBAR_BLACK_INDEX = 8;
        //shigq add end
    }
}
