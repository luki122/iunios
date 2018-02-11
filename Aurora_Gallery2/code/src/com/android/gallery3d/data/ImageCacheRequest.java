/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

abstract class ImageCacheRequest implements Job<Bitmap> {
    private static final String TAG = "ImageCacheRequest";

    protected GalleryApp mApplication;
    private Path mPath;
    private int mType;
    private int mTargetSize;

    public boolean isPNG = false; //wenyongzhe2016.3.30
    
    public ImageCacheRequest(GalleryApp application,
            Path path, int type, int targetSize) {
        mApplication = application;
        mPath = path;
        mType = type;
        mTargetSize = targetSize;
    }

    private String debugTag() {
        return mPath + "," +
                ((mType == MediaItem.TYPE_THUMBNAIL) ? "THUMB" :
                (mType == MediaItem.TYPE_MICROTHUMBNAIL) ? "MICROTHUMB" : "?");
    }

    @Override
    public Bitmap run(JobContext jc) {
        ImageCacheService cacheService = mApplication.getImageCacheService();

        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
        try {
            boolean found = cacheService.getImageData(mPath, mType, buffer);
            if (jc.isCancelled()) return null;
            if (found) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap;
                if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                    bitmap = DecodeUtils.decode(jc,
                            buffer.data, buffer.offset, buffer.length, options,
                            MediaItem.getMicroThumbPool());
                } else {
                    bitmap = DecodeUtils.decode(jc,
                            buffer.data, buffer.offset, buffer.length, options,
                            MediaItem.getThumbPool());
                }
                if (bitmap == null && !jc.isCancelled()) {
                    Log.w(TAG, "decode cached failed " + debugTag());
                }
                return bitmap;
            }
        } finally {
            MediaItem.getBytesBufferPool().recycle(buffer);
        }
        Bitmap bitmap = onDecodeOriginal(jc, mType);
        if (jc.isCancelled()) return null;

        if (bitmap == null) {
            Log.w(TAG, "decode orig failed " + debugTag());
            return null;
        }

        if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
            bitmap = BitmapUtils.resizeAndCropCenter(bitmap, mTargetSize, true);
        }
        //Aurora <SQF> <2014-05-12>  for NEW_UI begin
        else if(mType == MediaItem.TYPE_CUST05) {
        	//don't call BitmapUtils.resizeDownBySideLength, otherwise the bitmap will blur.
        }
        //Aurora <SQF> <2014-05-12>  for NEW_UI end
        else {
        	//Log.i("SQF_LOG", "---- BEFORE resizeDownBySideLength bitmap width, height:" + bitmap.getWidth() + " " + bitmap.getHeight());
            bitmap = BitmapUtils.resizeDownBySideLength(bitmap, mTargetSize, true);
            //Log.i("SQF_LOG", "---- AFTER  resizeDownBySideLength bitmap width, height:" + bitmap.getWidth() + " " + bitmap.getHeight());
        }
        if (jc.isCancelled()) return null;

        byte[] array = isPNG ?  BitmapUtils.compressToBytesPNG(bitmap) : BitmapUtils.compressToBytes(bitmap); //wenyongzhe2016.3.30
        if (jc.isCancelled()) return null;

        cacheService.putImageData(mPath, mType, array);
        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}
