package com.aurora.mms.util;
// Aurora xuyong 2015-02-12 created for repoter feature

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class RepoterUtils {
    private static final String LOG_TAG = "ReporterUtils";

    private static final Uri    REPORT_URI = Uri.parse("content://com.iuni.reporter/module/");
    private static final String MODULE = "module_key";
    private static final String ITEM = "item_tag";
    private static final String VALUE = "value";
    
    private static final String module_key = "100";
    
    public static final String MSG_SEN_RP = "011";
    public static final String MSG_DEL_RP = "012";
    public static final String MSG_REP_RP = "013";
    
    public static void addInternal(final Context context, final String item){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                ContentValues values = new ContentValues(3);
                values.put(MODULE, module_key);
                values.put(ITEM, item);
                values.put(VALUE, Integer.valueOf(1));
                int id = context.getContentResolver().update(REPORT_URI, values, null, null);
                Log.v(LOG_TAG, " id =" + id);
            }
            
        }).start();
    }

}
