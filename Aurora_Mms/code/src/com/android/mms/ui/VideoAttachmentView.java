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
import android.media.MediaMetadataRetriever;        // TODO: remove dependency for SDK build
// Aurora xuyong 2014-01-03 added for aurora;s new feature start
import android.media.MediaPlayer;
// Aurora xuyong 2014-01-03 added for aurora;s new feature end

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
// Aurora xuyong 2014-01-03 added for aurora;s new feature start
import android.widget.TextView;
// Aurora xuyong 2014-01-03 added for aurora;s new feature end
import android.widget.LinearLayout;
// Aurora xuyong 2014-01-03 added for aurora;s new feature start
import java.io.IOException;
// Aurora xuyong 2014-01-03 added for aurora;s new feature end
import java.util.Map;
// Aurora xuyong 2014-05-04 added for aurora's new feature start
import com.aurora.mms.ui.AuroraRoundImageView;
// Aurora xuyong 2014-05-04 added for aurora's new feature end

import com.aurora.featureoption.FeatureOption;
import gionee.drm.GnDrmManagerClient;

/**
 * This class provides an embedded editor/viewer of video attachment.
 */
public class VideoAttachmentView extends LinearLayout implements
        SlideViewInterface {
    private static final String TAG = "VideoAttachmentView";
    // Aurora xuyong 2014-05-04 modified for aurora's new feature start
    private AuroraRoundImageView mThumbnailView;
    // Aurora xuyong 2014-05-04 modified for aurora's new feature end
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    private TextView mVideoBgView;
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end

    public VideoAttachmentView(Context context) {
        super(context);
    }

    public VideoAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        // Aurora xuyong 2014-05-04 modified for aurora's new feature start
        mThumbnailView = (AuroraRoundImageView) findViewById(R.id.video_thumbnail);
        // Aurora xuyong 2014-05-04 modfiied for aurora's new feature end
    }

    public void startAudio() {
        // TODO Auto-generated method stub
    }

    public void startVideo() {
        // TODO Auto-generated method stub
    }

    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        // TODO Auto-generated method stub
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
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    public static String initMediaDuration(int duration) {
        String minStamp;
        String secStamp;
        int seTime = duration / 1000;
        int min = seTime / 60;
        int sec = seTime % 60;
        if (min < 10) {
            minStamp = "0" + min;
        } else {
            minStamp = "" + min;
        }
        if (sec < 10) {
            secStamp = "0" + sec;
        } else {
            secStamp = "" + sec;
        }
        return minStamp + ":" + secStamp;
    }
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end

    public void setVideo(String name, Uri video) {
        // Aurora xuyong 2014-01-03 added for aurora;s new feature start
        mVideoBgView = (TextView) findViewById(R.id.aurora_video_bg_down);
        mVideoBgView.setText(VideoAttachmentView.initMediaDuration(VideoAttachmentView.getMediaDuration(getContext(), video)));
        // Aurora xuyong 2014-05-04 modified for aurora's new feature start
        mThumbnailView.bindTextView(mVideoBgView);
        // Aurora xuyong 2014-05-07 modified for bug 4693 start
        ComposeMessageActivity.mThumbnailWorker.loadImage(video, mThumbnailView, MessageListItem.isVideo);
        // Aurora xuyong 2014-05-07 modified for bug 4693 end
        // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        /*try {
            Bitmap bitmap = createVideoThumbnail(mContext, video);
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_video);
            }
            Log.i(TAG, "Video name is " + name + ", bitmap.hight=" + bitmap.getHeight() + ", bitmap.width=" + bitmap.getWidth());
            String extName = name.substring(name.lastIndexOf('.') + 1);
            if (extName.equals("dcf")) {
                if (FeatureOption.MTK_DRM_APP) { 
                    Drawable front = getResources().getDrawable(R.drawable.drm_red_lock);
                    GnDrmManagerClient drmManager= new GnDrmManagerClient(mContext);
                    Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                    mThumbnailView.setImageBitmap(drmBitmap);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                } else {
                    mThumbnailView.setImageBitmap(bitmap);
                }
            } else {
                //Gionee <guoyx> <2013-06-13> add for CR00812038 begin
                bitmap = getResizedVideoThumbnail(bitmap);
                //Gionee <guoyx> <2013-06-13> add for CR00812038 end
                mThumbnailView.setImageBitmap(bitmap);
            }
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setVideo: out of memory: ", e);
        }*/
        // Aurora xuyong 2014-05-04 modified for aurora's new feature end
    }
    // Aurora xuyong 2014-01-03 added for aurora;s new feature start
    public static int getMediaDuration(Context context, Uri uri) {
        int duaration = -1;
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(context, uri);
            mp.prepare();
            duaration = mp.getDuration();
        } catch (IOException ex) {
            return duaration;
            // Assume this is a corrupt video file.
        } finally {
            try {
                mp.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
                return duaration;
            }
            return duaration;
        }
    }
    // Aurora xuyong 2014-01-03 added for aurora;s new feature end

    public static Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return bitmap;
    }

    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void stopAudio() {
        // TODO Auto-generated method stub
    }

    public void stopVideo() {
        // TODO Auto-generated method stub
    }

    public void reset() {
        // TODO Auto-generated method stub
    }

    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
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
    
    //Gionee <guoyx> <2013-06-13> add for CR00812038 begin
    /**
     * getResizedVideoThumbnail
     * @param bitmap
     * @return Resized Video Thumbnail bitmap
     */
    private Bitmap getResizedVideoThumbnail(Bitmap bitmap) {
        final float density = mContext.getResources().getDisplayMetrics().density;
        int outWidth = getDesiredThumbnailWidth(density);
        int outHeight = getDesiredThumbnailWidth(density);
        Log.i(TAG, "getResizedVideoThumnail  outWidth=" + outWidth + ", outHeight=" + outHeight);
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return newBitmap;
    }
    
    /**
     * getDesiredThumbnailWidth
     * @param density
     * @return Desired Thumbnail Width
     */
    private int getDesiredThumbnailWidth(float density) {
        return (int) (100 * density);
    }

    /**
     * getDesiredThumbnailHeight
     * @param density
     * @return Desired Thumbnail Height
     */
    private int getDesiredThumbnailHeight(float density) {
        return (int) (100 * density);
    }
    //Gionee <guoyx> <2013-06-13> add for CR00812038 end
   // Aurora xuyong 2014-04-25 added for bug #4301 start
    @Override
    public void setImage(String name, Uri uri) {
        // TODO Auto-generated method stub
        
    }
   // Aurora xuyong 2014-04-25 added for bug #4301 end
}
