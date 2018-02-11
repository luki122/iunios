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

import com.android.mms.ContentRestrictionException;
import com.android.mms.LogTag;
import com.android.mms.dom.events.EventImpl;
import com.android.mms.dom.smil.SmilMediaElementImpl;
import com.android.mms.drm.DrmWrapper;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SqliteWrapper;

import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementTime;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.drm.DrmManagerClient;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.provider.Telephony.Mms.Part;
import java.io.IOException;
import android.media.MediaMetadataRetriever;        // TODO: remove dependency for SDK build

public class VideoModel extends RegionMediaModel {
    private static final String TAG = MediaModel.TAG;
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = false;

    public VideoModel(Context context, Uri uri, RegionModel region)
            throws MmsException {
        this(context, null, null, uri, region);
        initModelFromUri(uri);
        checkContentRestriction();
    }
    private void initModelFromUri(Uri uri) throws MmsException {
        String scheme = uri.getScheme();
        if (scheme.equals("content")) {
            initFromContentUri(uri);
        } else if (uri.getScheme().equals("file")) {
            initFromFile(uri);
        }
        initMediaDuration();
    }

    public VideoModel(Context context, String contentType, String src,
            Uri uri, RegionModel region) throws MmsException {
        super(context, SmilHelper.ELEMENT_TAG_VIDEO, contentType, src, uri, region);
    }

    public VideoModel(Context context, String contentType, String src,
            DrmWrapper wrapper, RegionModel regionModel) throws IOException {
        super(context, SmilHelper.ELEMENT_TAG_VIDEO, contentType, src, wrapper, regionModel);
    }
    
    public void initFromFile(Uri uri) throws MmsException {
        String path = uri.getPath();
        mSrc = path.substring(path.lastIndexOf('/') + 1);

        if(mSrc.startsWith(".") && mSrc.length() > 1) {
            mSrc = mSrc.substring(1);
        }

        // Some MMSCs appear to have problems with filenames
        // containing a space.  So just replace them with
        // underscores in the name, which is typically not
        // visible to the user anyway.
        mSrc = mSrc.replace(' ', '_');
        //mUri = uri;

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(path).toLowerCase();
        if (TextUtils.isEmpty(extension)) {
            // getMimeTypeFromExtension() doesn't handle spaces in filenames nor can it handle
            // urlEncoded strings. Let's try one last time at finding the extension.
            int dotPos = path.lastIndexOf('.');
            if (0 <= dotPos) {
                extension = path.substring(dotPos + 1);
                extension = extension.toLowerCase();
            }
        }
        
        mContentType = mimeTypeMap.getMimeTypeFromExtension(extension);
        if (mContentType == null) {
            // set default content type to "application/octet-stream"
            mContentType = "application/octet-stream";
            if (extension != null && extension.equals("dcf")) {
                DrmManagerClient drmManager= new DrmManagerClient(mContext);
                mContentType = drmManager.getOriginalMimeType(path);
            }
        }
        Log.i(TAG, "VideoModel got mContentType: " + mContentType);
        if (mContentType != null && mContentType.startsWith("audio/")) {
            String temp = mContentType.substring(mContentType.lastIndexOf('/') + 1);
            mContentType = "video/";
            mContentType += temp;
        }
        Log.i(TAG, "VideoModel got mContentType: " + mContentType);

        if (path != null) {
            Log.i(TAG, "Video Path: " + path);
            ContentResolver cr = mContext.getContentResolver();
            Cursor c = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                    new String[] {MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?",
                    new String[] {path}, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        Uri videoUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getString(0));
                        Log.i(TAG, "Get video id in MediaStore:" + c.getString(0));
                        initMediaDuration(videoUri);
                    } else {
                        Log.i(TAG, "MediaStore has not this video");
                    }
                } finally {
                c.close();
                }
            } else {
                throw new MmsException("Bad URI: " + uri);
            }
        }
        
    }
    private void initFromContentUri(Uri uri) throws MmsException {
        ContentResolver cr = mContext.getContentResolver();
        // Aurora xuyong 2014-11-05 modified for privacy feature start
        Cursor c = SqliteWrapper.query(mContext, cr, uri, null, null, null, null);
        // Aurora xuyong 2014-11-05 modified for privacy feature end
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String path;
                    try {
                        // Local videos will have a data column
                        path = c.getString(c.getColumnIndexOrThrow(Images.Media.DATA));
                    } catch (IllegalArgumentException e) {
                        // For non-local videos, the path is the uri
                        path = uri.toString();
                    }
                    mSrc = path.substring(path.lastIndexOf('/') + 1).replace(' ', '_');

                    int columnIndex = c.getColumnIndex(Part.CONTENT_TYPE);
                    if (columnIndex != -1) {
                        mContentType = c.getString(columnIndex);
                    } else {
                        mContentType = c.getString(c
                                .getColumnIndexOrThrow(Images.Media.MIME_TYPE));
                    }
                    
                    if (TextUtils.isEmpty(mContentType)) {
                        throw new MmsException("Type of media is unknown.");
                    }

                    if (mContentType.equals(ContentType.VIDEO_MP4) && !(TextUtils.isEmpty(mSrc))) {
                        int index = mSrc.lastIndexOf(".");
                        if (index != -1) {
                            try {
                                String extension = mSrc.substring(index + 1);
                                if (!(TextUtils.isEmpty(extension)) &&
                                        (extension.equalsIgnoreCase("3gp") ||
                                        extension.equalsIgnoreCase("3gpp") ||
                                        extension.equalsIgnoreCase("3g2"))) {
                                    mContentType = ContentType.VIDEO_3GPP;
                                }
                            } catch(IndexOutOfBoundsException ex) {
                                if (LOCAL_LOGV) {
                                    Log.v(TAG, "Media extension is unknown.");
                                }
                            }
                        }
                    }

                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        Log.v(TAG, "New VideoModel initFromContentUri created:"
                                + " mSrc=" + mSrc
                                + " mContentType=" + mContentType
                                + " mUri=" + uri);
                    }
                } else {
                    throw new MmsException("Nothing found: " + uri);
                }
            } finally {
                c.close();
            }
        } else {
            throw new MmsException("Bad URI: " + uri);
        }

        initMediaDuration();
    }

    // EventListener Interface
    public void handleEvent(Event evt) {
        String evtType = evt.getType();
        if (LOCAL_LOGV || Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "[VideoModel] handleEvent " + evt.getType() + " on " + this);
        }

        MediaAction action = MediaAction.NO_ACTIVE_ACTION;
        if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            action = MediaAction.START;

            // if the Music player app is playing audio, we should pause that so it won't
            // interfere with us playing video here.
            pauseMusicPlayer();

            mVisible = true;
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_END_EVENT)) {
            action = MediaAction.STOP;
            if (mFill != ElementTime.FILL_FREEZE) {
                mVisible = false;
            }
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_PAUSE_EVENT)) {
            action = MediaAction.PAUSE;
            mVisible = true;
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_SEEK_EVENT)) {
            action = MediaAction.SEEK;
            mSeekTo = ((EventImpl) evt).getSeekTo();
            mVisible = true;
        }

        appendAction(action);
        notifyModelChanged(false);
    }

    protected void checkContentRestriction() throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkVideoContentType(mContentType);
    }

    @Override
    protected boolean isPlayable() {
        return true;
    }
    
    private void initMediaDuration(Uri uri) throws MmsException {
        if (uri == null) {
            throw new IllegalArgumentException("Uri may not be null.");
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int duration = 0;
        try {
            retriever.setDataSource(mContext, uri);
            String dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null) {
                duration = Integer.parseInt(dur);
            }
            mDuration = duration;
            Log.i(TAG, "Got video duration:" + duration);
        } catch (Exception ex) {
            Log.e(TAG, "MediaMetadataRetriever failed to get duration for " + uri.getPath(), ex);
            throw new MmsException(ex);
        } finally {
            retriever.release();
        }
    }
}
