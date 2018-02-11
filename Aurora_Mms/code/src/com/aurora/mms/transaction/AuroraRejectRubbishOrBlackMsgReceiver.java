package com.aurora.mms.transaction;

import java.util.HashSet;
import java.util.Set;
// Aurora xuyong 2014-07-21 added for reject feature start
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
// Aurora xuyong 2014-07-21 added for reject feature end
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import android.net.Uri;
import android.provider.Telephony.Threads;

public class AuroraRejectRubbishOrBlackMsgReceiver extends BroadcastReceiver{
    
    public static final String TAG = "AuroraRejectRubbishOrBlackMsgReceiver";
    public static final String SHARED_TAG = "aurora_reject_share";
    public static final String RUBBISH_REJECT_KEY = "aurora_reject_rubbish_msg";
    public static final String BLACK_REJECT_KEY = "aurora_reject_black_msg";
    // Aurora xuyong 2014-08-08 modified for bug #7389 start
    public static final String RUBBISH_MSG_REJECT_ACTION = "com.android.reject.RUBBISH_MSG_REJECT";
    public static final String BLACK_MSG_REJECT_ACTION = "com.android.reject.BLACK_MSG_REJECT";
   // Aurora xuyong 2014-08-08 modified for bug #7389 end
    public static final String BLACK_TABLE_CHANGED_ACTION = "com.aurora.black.changed";
    
    private static final int BLACK_OPE_INSERT = 1;
    private static final int BLACK_OPE_UPDATE = 2;
    private static final int BLACK_OPE_DELETE = 3;
    // Aurora xuyong 2014-07-21 added for reject feature start
    // Aurora xuyong 2014-10-30 modified for bug #9450 start
    ExecutorService mFixedThreadExecutor = Executors.newSingleThreadExecutor();
    // Aurora xuyong 2014-10-30 modified for bug #9450 end
    // Aurora xuyong 2014-07-21 added for reject feature end
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
      // Aurora xuyong 2014-07-22 added for bug #6742 start
        final Context fContext = context;
      // Aurora xuyong 2014-07-22 added for bug #6742 end
        if (intent.getAction().equals(RUBBISH_MSG_REJECT_ACTION)) {
            persistRejectRubbishMsgFlag(initEdtor(context), intent.getBooleanExtra("isRejectRubbishMsg", false));
        } else if (intent.getAction().equals(BLACK_MSG_REJECT_ACTION)) {
            persistRejectBlackMsgFlag(initEdtor(context), intent.getBooleanExtra("isRejectBlack", false));
        }
    }
    
    private void persistRejectRubbishMsgFlag(Editor editor, boolean rFlag) {
         editor.putBoolean(RUBBISH_REJECT_KEY, rFlag);
         editor.commit();
    }
    
    private void persistRejectBlackMsgFlag(Editor editor, boolean bFlag) {
        editor.putBoolean(BLACK_REJECT_KEY, bFlag);
        editor.commit();
    }
    
    private Editor initEdtor(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_TAG, Context.MODE_PRIVATE);
        return sp.edit();
    }

}
