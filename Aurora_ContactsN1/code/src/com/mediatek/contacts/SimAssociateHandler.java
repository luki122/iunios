package com.mediatek.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.android.contacts.ContactsApplication;

public class SimAssociateHandler extends HandlerThread {

    private final static String TAG = "SimAssociateHandler";

    private final static int MSG_LOAD = 0;
    private final static int MSG_QUIT = 1;
    
    private final static int MSG_REFRESH = 2;

     
    private static final String ASSOCIATE_CHANGED = "com.android.contacts.associate_changed";

    private static SimAssociateHandler sMe;

    private boolean mLoading = false;
    private boolean mStarted = false;

     
    private HashMap<String, ArrayList> mSimAssociationMaps = new HashMap<String, ArrayList>();
    
     
    private Context mContext;

    private MyHandler mMyHandler;

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ASSOCIATE_CHANGED.equals(action)) {
                log("RCV associated sim changed!");
                mMyHandler.sendEmptyMessage(MSG_REFRESH);
            }
        }
    };
    
    private SimAssociateHandler(Context context) {
        super(TAG);
        mContext = context;
        IntentFilter filter = new IntentFilter(ASSOCIATE_CHANGED);
        mContext.registerReceiver(myReceiver, filter);
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    public static SimAssociateHandler getInstance() {
        if(sMe == null) {
            sMe = new SimAssociateHandler(ContactsApplication.getInstance());
        }
        return sMe;
    }

    public void prepair() {
        start();
        mMyHandler = new MyHandler(getLooper());
    }

    public void relase() {
        if(mMyHandler != null)
            mMyHandler.sendEmptyMessage(MSG_QUIT);
        
        mContext.unregisterReceiver(myReceiver);
    }

    public ArrayList query(String number) {
        return mSimAssociationMaps.get(PhoneNumberUtils.stripSeparators(number));
    }

    public void load() {
        synchronized(this) {
            log("load");
            if(mLoading)
                return;
            else {
                mLoading = true;
                if(mMyHandler.hasMessages(MSG_LOAD))
                    log("duplicate MSG_LOAD");
                else
                    mMyHandler.sendEmptyMessage(MSG_LOAD);
            }
        }
    }

    class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {

            String number = null;
            int id = -1;
            ArrayList associateSims = null;
            boolean exist = false;
            switch(msg.what) {
                case MSG_LOAD:
                    log("+MSG_LOAD");
                    mSimAssociationMaps.clear();
                    Cursor cursor = mContext.getContentResolver().query(
                            Data.CONTENT_URI,
                            new String[] {
                                    Data.DATA1, Data.SIM_ASSOCIATION_ID
                            },
                            Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                                    + "' AND (" + Data.SIM_ASSOCIATION_ID + ">0)", null, null);
                    if(cursor != null && cursor.moveToFirst()) {
                        try {
                            do {
                                number = cursor.getString(0);
                                id = cursor.getInt(1);
                                log("number = " + number +" id = "+id);
                                associateSims = mSimAssociationMaps.get(PhoneNumberUtils.stripSeparators(number));
                                if(associateSims == null) {
                                    associateSims = new ArrayList();
                                    mSimAssociationMaps.put(PhoneNumberUtils.stripSeparators(number), associateSims);
                                }
                                exist = false;
                                for(int i=0; i<associateSims.size(); i++) {
                                    if(((Integer)associateSims.get(i)).intValue() == id) {
                                        exist = true;
                                        break;
                                    }
                                }
                                if(!exist)
                                    associateSims.add(Integer.valueOf(id));
                                log("associateSims = "+associateSims);
                            } while(cursor.moveToNext());
                        } catch(Exception e) {
                            log("exception");
                        } 
                    }
                    if(cursor != null)
                        cursor.close();
                    synchronized(this) {
                        mLoading = false;
                    }
                    log("-MSG_LOADs");
                    break;
                case MSG_QUIT:
                    log("MSG_EXIT");
                    getLooper().quit();
                    break;
                    
                case MSG_REFRESH:
                    load();
                    break;
            }
        }
    }
}
