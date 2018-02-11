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
import com.android.mms.model.AudioModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.Model;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.VideoModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
//import android.drm.DrmManagerClient;
import gionee.drm.GnDrmManagerClient;
import android.media.MediaMetadataRetriever;
import com.aurora.featureoption.FeatureOption;

//gionee gaoj 2012-4-10 added for CR00555790 start
import com.android.mms.MmsApp;
// Aurora xuyong 2014-05-04 added for aurora's new feature start
import com.gionee.mms.ui.SlidesBrowserItemView;
// Aurora xuyong 2014-05-04 added for aurora's new feature end
//gionee gaoj 2012-4-10 added for CR00555790 end
public class MmsThumbnailPresenter extends Presenter {
    private static final String TAG = "MmsThumbnailPresenter";
    private Context mContext;
    private int mSlideCount = 0;
    public MmsThumbnailPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
        mContext = context;
    }

    @Override
    public void present() {
        SlideModel slide = ((SlideshowModel) mModel).get(0);
        mSlideCount = ((SlideshowModel) mModel).size();
        // Aurora xuyong 2014-03-04 modified for aurora's new feature start
        if (slide != null && mSlideCount > 1) {
            presentSlideThumbnail((SlideViewInterface) mView, null);
        } else if (slide != null) {
        // Aurora xuyong 2014-03-04 modified for aurora's new feature end
            Log.i(TAG, "The first slide is not null.");
            presentFirstSlide((SlideViewInterface) mView, slide);
        }
    }

    private void presentFirstSlide(SlideViewInterface view, SlideModel slide) {
        view.reset();
        //gionee gaoj 2012-4-10 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            if (true == ismGnFlag()) {
                boolean hasNoAttachment = !slide.hasImage() && !slide.hasAudio()
                        && !slide.hasVideo();
                if (hasNoAttachment) {
                    presentSlideThumbnail(view, null);
                    setmGnFlag(false);
                    return;
                }
            }
        }
        //gionee gaoj 2012-4-10 added for CR00555790 end
        boolean imageVisibility = true;
        if (slide.hasImage()) {
            Log.i(TAG, "The first slide has image.");
            presentImageThumbnail(view, slide.getImage());
        } else if (slide.hasVideo()) {
            Log.i(TAG, "The first slide has video.");
            presentVideoThumbnail(view, slide.getVideo());
        } else if (slide.hasAudio()) {
            Log.i(TAG, "The first slide has audio.");
            presentAudioThumbnail(view, slide.getAudio());
            // Aurora xuyong 2014-01-03 deleted for aurora;s new feature start
            //imageVisibility = false;
            // Aurora xuyong 2014-01-03 deleted for aurora;s new feature end
        } else {
            Log.i(TAG, "The first slide has only text.");
            imageVisibility = false;
        }
        view.setImageVisibility(imageVisibility);
    }

    private void presentVideoThumbnail(SlideViewInterface view, VideoModel video) {
        if (video.isDrmProtected()) {
            showDrmIcon(view, video.getSrc());
        } else {
            view.setVideo(video.getSrc(), video.getUri());
            // Aurora xuyong 2014-01-03 added for aurora;s new feature start
            view.setImageVisibility(true);
            // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        }
    }

    private void presentImageThumbnail(SlideViewInterface view, ImageModel image) {
        if (image != null) {
            Log.d(TAG, "MmsThumbnailPresent. presentImageThumbnail. image src:" + image.getSrc());
        }
        if (image.isDrmProtected()) {
            showDrmIcon(view, image.getSrc());
        } else {
            if (FeatureOption.MTK_DRM_APP) {
                String extName = image.getSrc().substring(image.getSrc().lastIndexOf('.') + 1);
                if (extName.equals("dcf") && mSlideCount == 1) {
                    Bitmap bitmap = null;/*BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.ic_missing_thumbnail_picture);*/
                    Drawable front = mContext.getResources().getDrawable(-1/*R.drawable.drm_red_lock*/);
                    GnDrmManagerClient drmManager= new GnDrmManagerClient(mContext);
                    Bitmap drmBitmap = drmManager.overlayBitmap(bitmap, front);
                    view.setImage(image.getSrc(), drmBitmap);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                } else {
                // Aurora xuyong 2014-04-25 modified for bug #4301 start
                 // Aurora xuyong 2014-05-04 added for aurora's new feature start
                // Aurora xuyong 2014-05-04 modified for aurora's new feature start
                    if (view instanceof MessageListItem || view instanceof ImageAttachmentView || view instanceof SlidesBrowserItemView) {
                // Aurora xuyong 2014-05-04 modified for aurora's new feature end
                 // Aurora xuyong 2014-05-04 added for aurora's new feature end
                        view.setImage(image.getSrc(), image.getUri());
                    } else {
                        view.setImage(image.getSrc(), image.getBitmap());
                    }
                // Aurora xuyong 2014-04-25 modified for bug #4301 end
                }
            } else {
             // Aurora xuyong 2014-04-25 modified for bug #4301 start
             // Aurora xuyong 2014-05-04 added for aurora's new feature start
             // Aurora xuyong 2014-05-04 added for aurora's new feature start
                if (view instanceof MessageListItem || view instanceof ImageAttachmentView || view instanceof SlidesBrowserItemView) {
             // Aurora xuyong 2014-05-04 added for aurora's new feature end
             // Aurora xuyong 2014-05-04 added for aurora's new feature end
                    view.setImage(image.getSrc(), image.getUri());
                } else {
                    view.setImage(image.getSrc(), image.getBitmap());
                }
             // Aurora xuyong 2014-04-25 modified for bug #4301 end
            }
        }
    }

    protected void presentAudioThumbnail(SlideViewInterface view, AudioModel audio) {
        if (audio.isDrmProtected()) {
            showDrmIcon(view, audio.getSrc());
        } else {
            view.setAudio(audio.getUri(), audio.getSrc(), audio.getExtras()); 
            // Aurora xuyong 2014-01-03 added for aurora;s new feature start
            view.setImageVisibility(true);
            // Aurora xuyong 2014-01-03 added for aurora;s new feature end
        }
    }

    // Show an icon instead of real content in the thumbnail.
    private void showDrmIcon(SlideViewInterface view, String name) {
        try {
            Bitmap bitmap = null;/*BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.ic_mms_drm_protected);*/
            view.setImage(name, bitmap);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "showDrmIcon: out of memory: ", e);
        }
    }

    public void onModelChanged(Model model, boolean dataChanged) {
        // TODO Auto-generated method stub
    }
    //gionee gaoj 2012-4-10 added for CR00555790 start
    protected void presentSlideThumbnail(SlideViewInterface view, String name) {
        // Aurora xuyong 2014-03-04 modified for aurora's new feature start
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.aurora_slideshow_thumbnail);
        // Aurora xuyong 2014-03-04 modified for aurora's new feature end
        view.setImage(name, bitmap);
    }
    //gionee gaoj 2012-4-10 added for CR00555790 end
}
