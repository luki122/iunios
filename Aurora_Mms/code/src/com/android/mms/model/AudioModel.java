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
import com.android.mms.dom.events.EventImpl;
import com.android.mms.dom.smil.SmilMediaElementImpl;
import com.android.mms.drm.DrmWrapper;
import com.android.mms.ui.MessageUtils;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.MmsException;
//Aurora xuyong 2013-11-15 modified for google adapt end
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;

import org.w3c.dom.events.Event;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.Telephony.Mms.Part;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.drm.DrmManagerClient;
import com.aurora.featureoption.FeatureOption;
import android.media.MediaMetadataRetriever;        // TODO: remove dependency for SDK build

public class AudioModel extends MediaModel {
    private static final String TAG = MediaModel.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private final HashMap<String, String> mExtras;

    public AudioModel(Context context, Uri uri) throws MmsException {
        this(context, null, null, uri);
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            initModelFromUri(uri);
        } else if (scheme != null && scheme.equals("file")) {
            initModelFromFileUri(uri);
        }
        checkContentRestriction();
    }

    public AudioModel(Context context, String contentType, String src, Uri uri) throws MmsException {
        super(context, SmilHelper.ELEMENT_TAG_AUDIO, contentType, src, uri);
        mExtras = new HashMap<String, String>();
    }

    public AudioModel(Context context, String contentType, String src,
            DrmWrapper wrapper) throws IOException {
        super(context, SmilHelper.ELEMENT_TAG_AUDIO, contentType, src, wrapper);
        mExtras = new HashMap<String, String>();
    }

    private void initModelFromUri(Uri uri) throws MmsException {
        ContentResolver cr = mContext.getContentResolver();
        // Aurora xuyong 2014-11-05 modified for privacy feature start
        Cursor c = SqliteWrapper.query(mContext, cr, uri, null, null, null, null);
        // Aurora xuyong 2014-11-05 modified for privacy feature end
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String path;
                    boolean isFromMms = isMmsUri(uri);

                    // FIXME We suppose that there should be only two sources
                    // of the audio, one is the media store, the other is
                    // our MMS database.
                    if (isFromMms) {
                        path = c.getString(c.getColumnIndexOrThrow(Part._DATA));
                        mContentType = c.getString(c.getColumnIndexOrThrow(Part.CONTENT_TYPE));
                    } else {
                        path = c.getString(c.getColumnIndexOrThrow(Audio.Media.DATA));
                        mContentType = c.getString(c.getColumnIndexOrThrow(
                                Audio.Media.MIME_TYPE));
                        getExtraInfo(c);
                    }
                    if (FeatureOption.MTK_DRM_APP) {
                        if (MessageUtils.checkUriContainsDrm(mContext, uri)) {
                            path += ".dcf";
                        }
                    }
                    
                    mSrc = path.substring(path.lastIndexOf('/') + 1).replace(' ', '_');
                    Log.i(TAG, "path: " + path);
                    Log.i(TAG, "mSrc: " + mSrc);

                    if (TextUtils.isEmpty(mContentType)) {
                        throw new MmsException("Type of media is unknown.");
                    }

                    if (LOCAL_LOGV) {
                        Log.v(TAG, "New AudioModel created:"
                                + " mSrc=" + mSrc
                                + " mContentType=" + mContentType
                                + " mUri=" + uri
                                + " mExtras=" + mExtras);
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

    // Get more extras information which would be useful
    // to the user.
    private void getExtraInfo(Cursor c) {
        String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM));
        if (!TextUtils.isEmpty(album)) {
            mExtras.put(MediaStore.Audio.AlbumColumns.ALBUM, album);
        }

        String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST));
        if (!TextUtils.isEmpty(artist)) {
            mExtras.put(MediaStore.Audio.AlbumColumns.ARTIST, artist);
        }
    }

    public void initModelFromFileUri(Uri uri) throws MmsException {
        String path = uri.getPath();
        mSrc = path.substring(path.lastIndexOf('/') + 1);
        Log.i(TAG, "AudioModel mSrc: " + mSrc);
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
        Log.i(TAG, "AudioModel extension: " + extension);
        mContentType = mimeTypeMap.getMimeTypeFromExtension(extension);
        if (mContentType == null) {
          // Aurora xuyong 2014-06-19 modified for bug #5922 start
            if (extension.equals("amr"))  {
                mContentType = "audio/amr";
            }  if (extension.equals("acc"))  {
                mContentType = "audio/acc";
            }  if (extension.equals("imy"))  {
                mContentType = "audio/imelody";
            } else {
                // set default content type to "application/octet-stream"
                mContentType = "application/octet-stream";
                if (extension != null && extension.equals("dcf")) {
                    DrmManagerClient drmManager= new DrmManagerClient(mContext);
                    mContentType = drmManager.getOriginalMimeType(path);
               }
            }
            // Aurora xuyong 2014-06-19 modified for bug #5922 end
        }
        Log.i(TAG, "AudioModel got mContentType: " + mContentType);
        if (mContentType != null && mContentType.startsWith("video/")) {
            String temp = mContentType.substring(mContentType.lastIndexOf('/') + 1);
            mContentType = "audio/";
            mContentType += temp;
        }
        Log.i(TAG, "AudioModel mContentType: " + mContentType);
        //initMediaDuration

        if (path != null) {
            Log.i(TAG, "Audio Path: " + path);
            ContentResolver cr = mContext.getContentResolver();
            String[] projection = new String[] {
                    MediaStore.MediaColumns._ID,
                    MediaStore.Audio.AlbumColumns.ALBUM,
                    MediaStore.Audio.AlbumColumns.ARTIST
            };
            Cursor c = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.MediaColumns.DATA + "=?", new String[] { path }, null);

            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        Uri audioUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getString(0));
                        Log.i(TAG, "Get audio id in MediaStore:" + c.getString(0));
                        initMediaDuration(audioUri);
                        getExtraInfo(c);
                    } else {
                        Log.i(TAG, "MediaStore has not this audio");
                    }
                } finally {
                c.close();
                }
            } else {
                throw new MmsException("Bad URI: " + uri);
            }
        }
    }

    public void stop() {
        appendAction(MediaAction.STOP);
        notifyModelChanged(false);
    }

    public void handleEvent(Event evt) {
        String evtType = evt.getType();
        if (LOCAL_LOGV) {
            Log.v(TAG, "Handling event: " + evtType + " on " + this);
        }

        MediaAction action = MediaAction.NO_ACTIVE_ACTION;
        if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            action = MediaAction.START;
            // if the Music player app is playing audio, we should pause that so it won't
            // interfere with us playing audio here.
            pauseMusicPlayer();
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_END_EVENT)) {
            action = MediaAction.STOP;
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_PAUSE_EVENT)) {
            action = MediaAction.PAUSE;
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_SEEK_EVENT)) {
            action = MediaAction.SEEK;
            mSeekTo = ((EventImpl) evt).getSeekTo();
        }

        appendAction(action);
        notifyModelChanged(false);
    }

    public Map<String, ?> getExtras() {
        return mExtras;
    }

    protected void checkContentRestriction() throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkAudioContentType(mContentType);
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
            Log.i(TAG, "Got audio duration:" + duration);
        } catch (Exception ex) {
            Log.e(TAG, "MediaMetadataRetriever failed to get duration for " + uri.getPath(), ex);
            throw new MmsException(ex);
        } finally {
            retriever.release();
        }
    }
}
