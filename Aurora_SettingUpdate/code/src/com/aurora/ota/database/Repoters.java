package com.aurora.ota.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class Repoters {
    public static final String AUTHORITY = "com.iuni.reporter";
    private Repoters(){}
    
    public static final class Columns implements BaseColumns,ReporterKey {
        private Columns() {}
        
        public static final String KEY_ITEM = "item_uri";
        
        public static final String TABLE_NAME = "reporter";

        private static final String SCHEME = "content://";

        private static final String PATH_REPORTER = "/reporter";

        private static final String PATH_REPORTER_ID = "/reporter/";
        
        private static final String MODULE_PATH_REPORTER = "/module";

        private static final String MODULE_PATH_REPORTER_ID = "/module/";

        public static final Uri CONTENT_URI =
                Uri.parse(SCHEME+AUTHORITY+PATH_REPORTER_ID);
 
        
        public static final Uri CONTENT_URI_ITEMS =
                Uri.parse(SCHEME+AUTHORITY+PATH_REPORTER);
        
        public static final Uri CONTENT_MODULE_URI_ITEMS =
                Uri.parse(SCHEME+AUTHORITY+MODULE_PATH_REPORTER);
        
        public static final int REPORTER_ID_PATH_POSITION = 1;
        
        
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID+" DESC";
    }
    
    
}
