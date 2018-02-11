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

package com.android.settings.deviceinfo;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.settings.R;

import aurora.preference.AuroraPreference;

public class AuroraStorageItemPreference extends AuroraPreference {

    /**
     * index for total size in size array
     */
    private static final int TOTAL_SIZE = 0;

    /**
     * index for available size in size array
     */
    private static final int AVALIABLE_SIZE = 1;

    /**
     * widget for show total size
     */
    private TextView mTotalSizeTextView;
    /**
     * widget for show available size
     */
    private TextView mAvailableSizeTextView;
    /**
     * widget for show progress of storage size
     */
    private ProgressBar mProgress;

    /**
     * save total size here
     */
    private int mTotalSize;

    /**
     * save available size here
     */
    private int mAvalibaleSize;

    public int color;

    public int userHandle;

    private Context mContext;

    private boolean mNeedProgress;

    private final String totalPre;

    private final String availablePre;

    private final String mZero = "0.0GB";

    public AuroraStorageItemPreference(Context context, int titleRes, int colorRes, int layoutRes) {
        this(context, titleRes != 0 ? context.getText(titleRes) : "", colorRes, layoutRes, UserHandle.USER_NULL);
    }

    public AuroraStorageItemPreference(Context context, CharSequence title, int colorRes, int layoutRes, int userHandle) {
        super(context);
        mContext = context;
        if (layoutRes != 0) {
            setLayoutResource(layoutRes);
        }
        this.userHandle = userHandle;
        totalPre = mContext.getResources().getString(R.string.total_size);
        availablePre = mContext.getResources().getString(R.string.avail_size);

    }


    /**
     * update current storage information
     *
     * @param totalSize
     * @param avaliableSize
     */
    public void updateSize(Integer totalSize, Integer avaliableSize) {
        this.mTotalSize = totalSize.intValue();
        this.mAvalibaleSize = avaliableSize.intValue();
        mNeedProgress = true;
    }

    /**
     * get current storage size
     *
     * @return
     */
    public int[] getSize() {
        int[] size = new int[2];
        size[TOTAL_SIZE] = mTotalSize;
        size[AVALIABLE_SIZE] = mAvalibaleSize;
        return size;
    }


    private boolean needProgress() {
        return mNeedProgress;
    }


    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        if (getLayoutResource() != 0) {

		 
		   /*
		    * set total size to widget to show
		    */
            mTotalSizeTextView = (TextView) view.findViewById(R.id.total);
            CharSequence title = getTitle();
            if (TextUtils.isEmpty(title)) {
                //if total size was wrong,set it to 0
                title = mZero;
            }
            mTotalSizeTextView.setText(totalPre + title);
		/*
		 * set available size to widget to show
		 */
            mAvailableSizeTextView = (TextView) view.findViewById(R.id.available);
            CharSequence summary = getSummary();
            if (TextUtils.isEmpty(summary)) {
                //if total available was wrong,set it to 0
                summary = mZero;
            }
            mAvailableSizeTextView.setText(availablePre + summary);
            /**
             * set progress to widget to show
             */
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            final int[] progress = getSize();
            mProgress.setMax(progress[TOTAL_SIZE]);
            mProgress.setProgress(progress[TOTAL_SIZE] - progress[AVALIABLE_SIZE]);
        }
    }


    private static ShapeDrawable createRectShape(int width, int height, int color) {
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.setIntrinsicHeight(height);
        shape.setIntrinsicWidth(width);
        shape.getPaint().setColor(color);
        return shape;
    }


}
