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

/*
 * Copyright (C) 2008 Esmertec AG.
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

package com.android.mms.model;

import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.drm.moblie1.DrmException;
import com.android.mms.drm.DrmWrapper;
import android.media.MediaMetadataRetriever;        // TODO: remove dependency for SDK build
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import com.android.mms.drm.DrmUtils;
import com.android.mms.ui.MessageUtils;

import org.w3c.dom.events.EventListener;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
// Aurora xuyong 2014-12-15 added for bug #10577 start
import android.database.sqlite.SQLiteDiskIOException;
// Aurora xuyong 2014-12-15 added for bug #10577 end
import android.drm.DrmManagerClient;
import com.aurora.featureoption.FeatureOption;

public abstract class MediaModel extends Model implements EventListener {
    protected static final String TAG = "Mms/media";

    private final static String MUSIC_SERVICE_ACTION = "com.android.music.musicservicecommand";

    protected Context mContext;
    protected int mBegin;
    protected int mDuration;
    protected String mTag;
    protected String mSrc;
    protected String mContentType;
    private Uri mUri;
    private byte[] mData;
    protected short mFill;
    protected int mSize;
    protected int mPackagedSize;
    protected int mSeekTo;
    protected DrmWrapper mDrmObjectWrapper;
    protected boolean mMediaResizeable;

    private static final int SMILE_TAG_SIZE_TEXT              = 120;
    private static final int SMILE_TAG_SIZE_PAGE              = 26;
    private static final int SMILE_TAG_SIZE_ATTACH            = 26;
    private static final int SMILE_TAG_SIZE_IMAGE             = 50;
    private static final int SMILE_TAG_SIZE_AUDIO             = 50;
    private static final int SMILE_TAG_SIZE_VIDEO             = 50;
    private final ArrayList<MediaAction> mMediaActions;
    
    private boolean mHasDrmContent;
    private boolean mHasDrmRight;
    
    public boolean hasDrmContent() {
        return mHasDrmContent;
    }
    
    public boolean hasDrmRight() {
        return mHasDrmRight;
    }
   
    public static enum MediaAction {
        NO_ACTIVE_ACTION,
        START,
        STOP,
        PAUSE,
        SEEK,
    }

    public MediaModel(Context context, String tag, String contentType,
            String src, Uri uri) throws MmsException {
        mContext = context;
        mTag = tag;
        mContentType = contentType;
        mSrc = src;
        mUri = uri;
        initMediaSize();
        mMediaActions = new ArrayList<MediaAction>();
        if (FeatureOption.MTK_DRM_APP) {
            Log.i(TAG, "MediaModel got media uri: " + mUri);
            if (MessageUtils.checkUriContainsDrm(mContext, uri)) {
                mHasDrmContent = true;
                DrmManagerClient drmManager= new DrmManagerClient(mContext);
                int right = drmManager.checkRightsStatus(uri, android.drm.DrmStore.Action.DISPLAY);
                if (android.drm.DrmStore.RightsStatus.RIGHTS_VALID == right) {
                    mHasDrmRight = true;
                } else {
                    mHasDrmRight = false;
                }
            }
        }
    }

    public MediaModel(Context context, String tag, String contentType,
            String src, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null.");
        }

        mContext = context;
        mTag = tag;
        mContentType = contentType;
        mSrc = src;
        mData = data;
        mSize = data.length;
        if (true == isText())
        {
            mPackagedSize = mSize == 0 ? 0 : (mSize + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_TEXT);
        } else {
            mPackagedSize = mSize + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_IMAGE;
        }
        mMediaActions = new ArrayList<MediaAction>();
    }

    public MediaModel(Context context, String tag, String contentType,
            String src, DrmWrapper wrapper) throws IOException {
        mContext = context;
        mTag = tag;
        mContentType = contentType;
        mSrc = src;
        mDrmObjectWrapper = wrapper;
        mUri = DrmUtils.insert(context, wrapper);
        mSize = wrapper.getOriginalData().length;
        if (true == isText())
        {
            mPackagedSize = mSize == 0 ? 0 : (mSize + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_TEXT);
        } else {
            mPackagedSize = mSize + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_IMAGE;
        }
        mMediaActions = new ArrayList<MediaAction>();
    }
    public void changeSizeOnlyForText(String newText) {
        int length = newText.getBytes().length;
        mPackagedSize = length + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_TEXT;
    }
    public int getBegin() {
        return mBegin;
    }

    public void setBegin(int begin) {
        mBegin = begin;
        notifyModelChanged(true);
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        if (isPlayable() && (duration < 0)) {
            // 'indefinite' duration, we should try to find its exact value;
            try {
                initMediaDuration();
            } catch (MmsException e) {
                // On error, keep default duration.
                Log.e(TAG, e.getMessage(), e);
                return;
            }
        } else {
            mDuration = duration;
        }
        notifyModelChanged(true);
    }

    public String getTag() {
        return mTag;
    }

    public String getContentType() {
        return mContentType;
    }

    /**
     * Get the URI of the media without checking DRM rights. Use this method
     * only if the media is NOT DRM protected.
     *
     * @return The URI of the media.
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * Get the URI of the media with checking DRM rights. Use this method
     * if the media is probably DRM protected.
     *
     * @return The URI of the media.
     * @throws DrmException Insufficient DRM rights detected.
     */
    public Uri getUriWithDrmCheck() throws DrmException {
        if (mUri != null) {
            if (isDrmProtected() && !mDrmObjectWrapper.consumeRights()) {
                throw new DrmException("Insufficient DRM rights.");
            }
        }
        return mUri;
    }

    public byte[] getData() throws DrmException {
        if (mData != null) {
            if (isDrmProtected() && !mDrmObjectWrapper.consumeRights()) {
                throw new DrmException(
                        mContext.getString(R.string.insufficient_drm_rights));
            }

            byte[] data = new byte[mData.length];
            System.arraycopy(mData, 0, data, 0, mData.length);
            return data;
        }
        return null;
    }

    /**
     * @param uri the mUri to set
     */
    void setUri(Uri uri) {
        mUri = uri;
    }

    /**
     * @return the mSrc
     */
    public String getSrc() {
        return mSrc;
    }

    /**
     * @return the mFill
     */
    public short getFill() {
        return mFill;
    }

    /**
     * @param fill the mFill to set
     */
    public void setFill(short fill) {
        mFill = fill;
        notifyModelChanged(true);
    }

    /**
     * @return whether the media is resizable or not. For instance, a picture can be resized
     * to smaller dimensions or lower resolution. Other media, such as video and sounds, aren't
     * currently able to be resized.
     */
    public boolean getMediaResizable() {
        return mMediaResizeable;
    }

    public int getMediaPackagedSize() {
        return mPackagedSize;
    }
    /**
     * @return the size of the attached media
     */
    public int getMediaSize() {
        return mSize;
    }

    public boolean isText() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_TEXT);
    }

    public boolean isImage() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_IMAGE);
    }

    public boolean isVideo() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_VIDEO);
    }

    public boolean isAudio() {
        return mTag.equals(SmilHelper.ELEMENT_TAG_AUDIO);
    }

    public boolean isDrmProtected() {
        return mDrmObjectWrapper != null;
    }

    public boolean isAllowedToForward() {
        return mDrmObjectWrapper.isAllowedToForward();
    }

    protected void initMediaDuration() throws MmsException {
        if (mUri == null) {
            throw new IllegalArgumentException("Uri may not be null.");
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int duration = 0;
        try {
            retriever.setDataSource(mContext, mUri);
            String dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null) {
                duration = Integer.parseInt(dur);
            }
            mDuration = duration;
        } catch (Exception ex) {
            Log.e(TAG, "MediaMetadataRetriever failed to get duration for " + mUri.getPath(), ex);
            throw new MmsException(ex);
        } finally {
            retriever.release();
        }
    }

    private void initMediaSize() throws MmsException {
        ContentResolver cr = mContext.getContentResolver();
        InputStream input = null;
        try {
            input = cr.openInputStream(mUri);
            if (input instanceof FileInputStream) {
                // avoid reading the whole stream to get its length
                FileInputStream f = (FileInputStream) input;
                mSize = (int) f.getChannel().size();
                if (true == isText())
                {
                    mPackagedSize = mSize == 0 ? 0 : (mSize + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_TEXT);
                } else {
                    mPackagedSize = mSize + SMILE_TAG_SIZE_ATTACH + SMILE_TAG_SIZE_PAGE + SMILE_TAG_SIZE_IMAGE;
                }
            } else {
                // Aurora xuyong 2016-01-09 modified for bug #18349 start
                while (input != null && -1 != input.read()) {
                // Aurora xuyong 2016-01-09 modified for bug #18349 end
                    mSize++;
                    mPackagedSize++;
                }
            }

        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            if (e instanceof FileNotFoundException) {
                throw new MmsException(e.getMessage());
            }
        // Aurora xuyong 2014-12-15 added for bug #10577 start
        } catch (SQLiteDiskIOException e) {
            if (e instanceof SQLiteDiskIOException) {
                throw new MmsException(e.getMessage());
            }
        // Aurora xuyong 2014-12-15 added for bug #10577 end
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
    }

    public static boolean isMmsUri(Uri uri) {
        return uri.getAuthority().startsWith("mms");
    }

    public int getSeekTo() {
        return mSeekTo;
    }

    public void appendAction(MediaAction action) {
        mMediaActions.add(action);
    }

    public MediaAction getCurrentAction() {
        if (0 == mMediaActions.size()) {
            return MediaAction.NO_ACTIVE_ACTION;
        }
        return mMediaActions.remove(0);
    }

    protected boolean isPlayable() {
        return false;
    }

    public DrmWrapper getDrmObject() {
        return mDrmObjectWrapper;
    }

    protected void pauseMusicPlayer() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "pauseMusicPlayer");
        }

        Intent i = new Intent(MUSIC_SERVICE_ACTION);
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
    }

    /**
     * If the attached media is resizeable, resize it to fit within the byteLimit. Save the
     * new part in the pdu.
     * @param byteLimit the max size of the media attachment
     * @throws MmsException
     */
    protected void resizeMedia(int byteLimit, long messageId) throws MmsException {
    }
    //ALPS00289861
    public void setSrc(String src) {
        this.mSrc = src;
    }

}
