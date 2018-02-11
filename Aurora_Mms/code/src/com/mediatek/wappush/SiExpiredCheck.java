/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.wappush;

import gionee.provider.GnTelephony.WapPush;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SiExpiredCheck {
    
    private static String TAG = "Mms/WapPush";
    private static int SLEEP_INTERVAL = 1000;
    private boolean isStarted = false; // thread is started?
    private boolean isPaused = true;  // check is paused?
    
    private Context mContext;
    
    public SiExpiredCheck(Context context){
        mContext = context;
    }
    
    /*
     * Check selected si message whether it expired
     * hasError: 0 check unexpired message
     *           1 check expired message when time and date are changed
     */
    
    private static synchronized void siExpiredCheck(Context context, int isExpired){
        
        if(context == null){
            return;
        }

        int currentTime = (int)(System.currentTimeMillis()/1000);
        
        
        String selection = WapPush.ERROR + " = " + isExpired ;

        ContentResolver resolver = context.getContentResolver();
        String []projection = {WapPush._ID,WapPush.EXPIRATION};
        Cursor cursor = resolver.query(WapPush.CONTENT_URI_SI, projection, selection, null, null);    //android.database.CursorWindowAllocationException: Cursor window could not be created from binder. 2013-10-19
        ContentValues expiredValues = new ContentValues();
        ContentValues noExpiredValues = new ContentValues();
        expiredValues.put(WapPush.ERROR, 1);
        noExpiredValues.put(WapPush.ERROR, 0);
        
        if(cursor != null){
            try {
                if (cursor.moveToFirst()) {
                    do{
                        long messageId = cursor.getLong(0);
                        int expiredTime = cursor.getInt(1);
                        //the message is expired!
                        if(expiredTime > 0 && expiredTime < currentTime && isExpired == 0 ){
                            Log.i(TAG,"SiExpiredCheck: message " + messageId + " is expired!");
                            resolver.update(WapPush.CONTENT_URI_SI,expiredValues,WapPush._ID + " = " + messageId,null);
                        }
                        //the message is not expired 
                        if(expiredTime > currentTime && isExpired == 1 ){
                            Log.i(TAG,"SiExpiredCheck: message " + messageId + " is set noexpired!");
                            resolver.update(WapPush.CONTENT_URI_SI,noExpiredValues,WapPush._ID + " = " + messageId,null);
                        }
                        
                    }while(cursor.moveToNext());

                }
            } finally {
                cursor.close();
            }
        } 
    }
    
    /*
     * Check unexpired message whether it expired
     */
    
    public static void siExpiredCheck(final Context context){
        siExpiredCheck(context,0);
    }
    
    /*
     * When the time is changed , the expired message should be rechecked.
     */
    public static void onTimeChanged(final Context context){
        Log.i(TAG,"onTimeChanged");
        siExpiredCheck(context,1);
    }
    
    public void startExpiredCheck(){
        if(isPaused){
            Log.w(TAG,"startExpiredCheck!");
            isPaused = false;
        }
        
    }
    
    public void stopExpiredCheck(){
        if(!isPaused){
            Log.w(TAG,"stopExpiredCheck!");
            isPaused = true;
        }
    }
    
    private Thread mThread = new Thread(){
        public void run() {
            while (isStarted) {
                if (!isPaused) {
                    siExpiredCheck(mContext);
                }
                try {
                    sleep(SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    };
    
    //start looper thread;
    public void startSiExpiredCheckThread(){
        try{
            isStarted = true;
            mThread.start();
        }catch(Exception e){
            Log.w(TAG,"SiExpiredCheck: thread start error!");
        }
    }
    
    //stop looper thread;
    public void stopSiExpiredCheckThread(){
        try{
            mThread.interrupt();
            isStarted = false;    
        }catch(Exception e){
            Log.w(TAG,"SiExpiredCheck: thread stop error!");
        }
    }
}
