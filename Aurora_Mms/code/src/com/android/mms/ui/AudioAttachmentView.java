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

package com.android.mms.ui;

import com.android.mms.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;
import android.media.MediaMetadataRetriever;
import com.aurora.featureoption.FeatureOption;
import gionee.drm.GnDrmManagerClient;
// Aurora xuyong 2014-05-04 added for aurora's new feature start
import com.aurora.mms.ui.AuroraRoundImageView;
// Aurora xuyong 2014-05-04 added for aurora's new feature end
//gionee gaoj 2012-4-10 added for CR00555790 start
import android.os.SystemProperties;
import com.android.mms.MmsApp;
//gionee gaoj 2012-4-10 added for CR00555790 end
/**
 * This class provides an embedded editor/viewer of audio attachment.
 */
public class AudioAttachmentView extends LinearLayout implements
        SlideViewInterface {
    private static final String TAG = "AudioAttachmentView";

    private final Resources mRes;
    //Aurora xuyong 2013-10-11 modified for aurora's new feature start
    private ImageView mNameView;
    //Aurora xuyong 2013-10-11 modified for aurora's new feature end
    private TextView mAlbumView;
    private TextView mArtistView;
    private TextView mErrorMsgView;
    // Aurora xuyong 2014-05-04 modified for aurora's new feature start
    private AuroraRoundImageView mAudioImageView;
    // Aurora xuyong 2014-05-04 modified for aurora's new feature end
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    private TextView mAudioBgView;
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end
    private Uri mAudioUri;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPlaying;

    public AudioAttachmentView(Context context) {
        super(context);
        mRes = context.getResources();
    }

    public AudioAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRes = context.getResources();
    }

    @Override
    protected void onFinishInflate() {
        //Aurora xuyong 2013-10-11 modified for aurora's new feature start
        mNameView = (ImageView) findViewById(R.id.audio_name);
        //Aurora xuyong 2013-10-11 modified for aurora's new feature end
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (!MmsApp.mGnMessageSupport) {
        mAlbumView = (TextView) findViewById(R.id.album_name);
        mArtistView = (TextView) findViewById(R.id.artist_name);
        mErrorMsgView = (TextView) findViewById(R.id.audio_error_msg);
        // Aurora xuyong 2014-05-04 modified for aurora's new feature start
        mAudioImageView = (AuroraRoundImageView) findViewById(R.id.audio_image_content);
        // Aurora xuyong 2014-05-04 modified for aurora's new feature end
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    private void onPlaybackError() {
        Log.e(TAG, "Error occurred while playing audio.");
        showErrorMessage(mRes.getString(R.string.cannot_play_audio));
        stopAudio();
    }

    private void cleanupMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } finally {
                mMediaPlayer = null;
            }
        }
    }

    synchronized public void startAudio() {
        if (!mIsPlaying && (mAudioUri != null)) {
            mMediaPlayer = MediaPlayer.create(mContext, mAudioUri);
            if (mMediaPlayer != null) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        stopAudio();
                    }
                });
                mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        onPlaybackError();
                        return true;
                    }
                });

                mIsPlaying = true;
                mMediaPlayer.start();
            }
        }
    }

    public void startVideo() {
        // TODO Auto-generated method stub

    }

    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        synchronized (this) {
            mAudioUri = audio;
        }
        // Aurora xuyong 2014-01-03 added for aurora;s new feature start
        mAudioBgView = (TextView) findViewById(R.id.aurora_audio_bg_down);
        // Aurora xuyong 2014-05-04 modified for aurora's new feature start
        mAudioImageView = (AuroraRoundImageView) findViewById(R.id.audio_image_content);
        mAudioImageView.bindTextView(mAudioBgView);
        // Aurora xuyong 2014-05-07 modified for bug 4693 start
        ComposeMessageActivity.mThumbnailWorker.loadImage(audio, mAudioImageView, MessageListItem.isAudio);
        // Aurora xuyong 2014-05-07 modified for bug 4693 end
        // Aurora xuyong 2014-05-04 modified for aurora's new feature end
        // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        //Aurora xuyong 2013-10-11 deleted for aurora's new feature start
        //mNameView.setText(name);
        //Aurora xuyong 2013-10-11 deleted for aurora's new feature end
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (!MmsApp.mGnMessageSupport) {
        mAlbumView.setText((String) extras.get("album"));
        mArtistView.setText((String) extras.get("artist"));
        String extName = name.substring(name.lastIndexOf('.') + 1);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
               -1); 
        if (extName.equals("dcf")) {
            Log.i(TAG, "contain drm audio");
            if (FeatureOption.MTK_DRM_APP) {
                Drawable front = getResources().getDrawable(-1/*R.drawable.drm_red_lock*/);
                GnDrmManagerClient drmManager= new GnDrmManagerClient(mContext);
                Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                mAudioImageView.setImageBitmap(drmBitmap);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            } else {
                mAudioImageView.setImageBitmap(bitmap);
            }
        } else {
            mAudioImageView.setImageBitmap(bitmap);
        }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    public void setImage(String name, Bitmap bitmap) {
        // TODO Auto-generated method stub

    }

    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub

    }

    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub

    }

    public void setText(String name, String text) {
        // TODO Auto-generated method stub

    }

    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub

    }

    public void setVideo(String name, Uri video) {
        // TODO Auto-generated method stub

    }

    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub

    }

    synchronized public void stopAudio() {
        try {
            cleanupMediaPlayer();
        } finally {
            mIsPlaying = false;
        }
    }

    public void stopVideo() {
        // TODO Auto-generated method stub

    }

    public void reset() {
        synchronized (this) {
            if (mIsPlaying) {
                stopAudio();
            }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (!MmsApp.mGnMessageSupport) {
        mErrorMsgView.setVisibility(GONE);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub

    }

    private void showErrorMessage(String msg) {
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (!MmsApp.mGnMessageSupport) {
        mErrorMsgView.setText(msg);
        mErrorMsgView.setVisibility(VISIBLE);
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
    }

    public void pauseAudio() {
        // TODO Auto-generated method stub

    }

    public void pauseVideo() {
        // TODO Auto-generated method stub

    }

    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub

    }

    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub

    }
   // Aurora xuyong 2014-04-25 added for bug #4301 start
    @Override
    public void setImage(String name, Uri uri) {
        // TODO Auto-generated method stub
        
    }
   // Aurora xuyong 2014-04-25 added for bug #4301 end
}
