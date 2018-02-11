/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.util.Log;
import android.service.notification.StatusBarNotification;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.ActivityManagerNative;

public class AuroraGreenStatusBarView extends FrameLayout
{   private Context mContext;
    private PhoneStatusBarView mStatusBarView = null;
    private GestureDetector mGD;
    private StatusBarNotification mStatusBarNotification;
    private SimpleOnGestureListener mSimpleOnGestureListener 
    = new GestureDetector.SimpleOnGestureListener() {
    	@Override
    	public boolean onSingleTapUp(MotionEvent e) {
    		send();
            return false;
        }
    };
    
    public AuroraGreenStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mGD = new GestureDetector(context, mSimpleOnGestureListener);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	mGD.onTouchEvent(event);
    	return mStatusBarView.dispatchTouchEvent(event);
    }

 /*   @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	return super.onTouchEvent(event);
    }*/
    
    public void setPhoneStatusBarView(PhoneStatusBarView statusBarView){
    	mStatusBarView = statusBarView;
    }
    
    public void setStatusBarNotification(StatusBarNotification statusBarNotification){
    	mStatusBarNotification = statusBarNotification;
    }
    
    private void send(){
    	PendingIntent mIntent = mStatusBarNotification.getNotification().contentIntent;
		try {
		    ActivityManagerNative.getDefault().resumeAppSwitches();
//		    ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
		    ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn ();
            mIntent.send(mContext, 0, null);
        } catch (Exception ee) {
            // the stack trace isn't very helpful here.  Just log the exception message.
        }
        KeyguardManager kgm =
            (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        if (kgm != null) kgm.exitKeyguardSecurely(null);
    }
}

