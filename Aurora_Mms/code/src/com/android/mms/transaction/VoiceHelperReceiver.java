package com.android.mms.transaction;

import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.util.Log;

import gionee.provider.GnTelephony.Sms;

public class VoiceHelperReceiver extends BroadcastReceiver {

    public static final String TAG = "MmsVoiceHelper";
    public static final String ACTION_VOICEHELPER_STOP = "gn.voice.service.TTSService.stop";
    public static final int CMD_READ              = 2001;
    public static final int CMD_NOT_READ          = 2002;
    public static final int CMD_VOICEHEAPER_ERROR = 2003;
    public static final int CMD_READ_MSG_DONE     = 3001;
    public static final int CMD_READ_MSG_ERROR    = 3002;
    private static boolean mVoiceHelperError = false;
    private static final String KEY = "cmdcode";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (MmsApp.mGnVoiceHelperSupport && ACTION_VOICEHELPER_STOP.equals(intent.getAction())) {
            int cmd = intent.getIntExtra(KEY, -1);
            Log.i(TAG, "onReceive   cmd = " + cmd + "   id = " + intent.getStringExtra("id"));
            if (CMD_READ == cmd) {
                try {
                    int id = Integer.parseInt(intent.getStringExtra("id"));
                    Uri uri = ContentUris.withAppendedId(Sms.CONTENT_URI, id);
                    ContentValues values = new ContentValues(2);
                    values.put(Sms.SEEN, 1);
                    values.put(Sms.READ, 1);
                    SqliteWrapper.update(context, context.getContentResolver(),
                            uri, values, null, null);
                    MessagingNotification.blockingUpdateNewMessageIndicator(
                            context, false, false);
                } catch (NumberFormatException e) {
                    // TODO: handle exception
                    Log.e(TAG, "onReceive   e = " + e.toString());
                }
            } else if (CMD_VOICEHEAPER_ERROR == cmd) {
                Log.i(TAG, "onReceive  (CMD_VOICEHEAPER_ERROR): VoiceHelper   ERROR!!!");
                mVoiceHelperError = true;
                MessagingNotification.blockingUpdateNewMessageIndicator(
                        context, true, true);
            } else if (CMD_READ_MSG_DONE == cmd || CMD_READ_MSG_ERROR == cmd) {
                context.sendBroadcast(new Intent(MessageUtils.VOICEHELPER_SERVICE_STOP));
            }
        }
    }

    public static boolean isVoiceHelperError() {
        return mVoiceHelperError;
    }
    
    public static void resetVoiceHelperFlag() {
        mVoiceHelperError = false;
    }
}
