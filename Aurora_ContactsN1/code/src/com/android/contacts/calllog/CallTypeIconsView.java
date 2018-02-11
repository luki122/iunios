/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.contacts.calllog;

import com.android.contacts.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.provider.CallLog.Calls;
import android.util.AttributeSet;
import android.view.View;

/**
 * View that draws one or more symbols for different types of calls (missed calls, outgoing etc).
 * The symbols are set up horizontally. As this view doesn't create subviews, it is better suited
 * for ListView-recycling that a regular LinearLayout using ImageViews.
 */
public class CallTypeIconsView extends View {
    /**
    * Change Feature by Mediatek Begin.
    * Original Android's Code:
     private List<Integer> mCallTypes = Lists.newArrayListWithCapacity(3);
    * Descriptions:Goole default display 3 type icons in call log list,
    *              MTK only display only 1.   
    */
    private int mCallType;
    /**
    * Change Feature by Mediatek End.
    */
    private Resources mResources;
    private int mWidth;
    private int mHeight;

    public CallTypeIconsView(Context context) {
        this(context, null);
    }

    public CallTypeIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResources = new Resources(context);
    }

    public void clear() {
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
          mCallTypes.clear();
        * Descriptions:
        */
        mCallType = -1;
        /**
        * Change Feature by Mediatek End.
        */
        mWidth = 0;
        mHeight = 0;
        invalidate();
    }


    public int getCallType() {
        return mCallType;
    }

    private Drawable getCallTypeDrawable(int callType) {
        switch (callType) {
            case Calls.INCOMING_TYPE:
                return mResources.incoming;
            case Calls.OUTGOING_TYPE:
                return mResources.outgoing;
            case Calls.MISSED_TYPE:
                return mResources.missed;
            case Calls.VOICEMAIL_TYPE:
                return mResources.voicemail;
            default:
                throw new IllegalArgumentException("invalid call type: " + callType);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
            /**
            * Change Feature by Mediatek Begin.
            * Original Android's Code:
              int left = 0;
             for (Integer callType : mCallTypes) {
               final Drawable drawable = getCallTypeDrawable(callType);
               final int right = left + drawable.getIntrinsicWidth();
               drawable.setBounds(left, 0, right, drawable.getIntrinsicHeight());
               drawable.draw(canvas);
               left = right + mResources.iconMargin;
              }
            * Descriptions:
            */
            final Drawable drawable = getCallTypeDrawable(mCallType);
            if (null == drawable) {
                return;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            /**
             * Change Feature by Mediatek End.
             */
    }

    private static class Resources {
        public final Drawable incoming;
        public final Drawable outgoing;
        public final Drawable missed;
        //The following lines are provided and maintained by Mediatek Inc.
        public final Drawable vtincoming;
        public final Drawable vtoutgoing;
        public final Drawable vtmissed;
        //The previous lines are provided and maintained by Mediatek Inc.
        public final Drawable voicemail;
        public final int iconMargin;

        public Resources(Context context) {
            final android.content.res.Resources r = context.getResources();
            incoming = r.getDrawable(R.drawable.ic_call_incoming_holo_dark);
            outgoing = r.getDrawable(R.drawable.ic_call_outgoing_holo_dark);
            missed = r.getDrawable(R.drawable.ic_call_missed_holo_dark);
            voicemail = r.getDrawable(R.drawable.ic_call_voicemail_holo_dark);
            iconMargin = r.getDimensionPixelSize(R.dimen.call_log_icon_margin);
            //The following lines are provided and maintained by Mediatek Inc.
            vtincoming = r.getDrawable(R.drawable.ic_video_call_incoming_holo_dark);
            vtoutgoing = r.getDrawable(R.drawable.ic_video_call_outgoing_holo_dark);
            vtmissed = r.getDrawable(R.drawable.ic_video_call_missed_holo_dark);
            //The previous lines are provided and maintained by Mediatek Inc.
        }
    }
    
  //The following lines are provided and maintained by Mediatek Inc.
    public void set(int callType) {
        mCallType = callType;
        final Drawable drawable = getCallTypeDrawable(callType);
        if (null == drawable) {
            return;
        }
        mWidth = drawable.getIntrinsicWidth();
        mHeight = Math.max(mHeight, drawable.getIntrinsicHeight());
        invalidate();
    }
    
    
  //The previous lines are provided and maintained by Mediatek Inc.
}
