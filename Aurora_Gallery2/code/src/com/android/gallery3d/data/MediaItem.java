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
import android.graphics.BitmapRegionDecoder;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.util.ThreadPool.Job;

// MediaItem represents an image or a video item.
public abstract class MediaItem extends MediaObject {
    // NOTE: These type numbers are stored in the image cache, so it should not
    // not be changed without resetting the cache.
    public static final int TYPE_THUMBNAIL = 1;
    public static final int TYPE_MICROTHUMBNAIL = 2;
	//Aurora <paul> <2014-02-27> for NEW_UI begin
	public static final int TYPE_CUST01 = 3;
	public static final int TYPE_CUST02 = 4;
	public static final int TYPE_CUST03 = 5;
	//Aurora <SQF> <2014-04-22>  for vague .bmp file begin
	public static final int TYPE_CUST04 = 6;  
	//Aurora <SQF> <2014-04-22>  for vague .bmp image end

	//Aurora <SQF> <2014-05-13>  for crop center image begin
	public static final int TYPE_CUST05 = 7;
	//Aurora <SQF> <2014-05-13>  for crop center image end

	public static final int TYPE_CUST01_SIZE = 250;//lroy del "final"
	public static final int TYPE_CUST02_SIZE = 150;
	public static final int TYPE_CUST03_SIZE = 100;
	//Aurora <paul> <2014-02-27> for NEW_UI end
	//Aurora <SQF> <2014-04-22>  for vague .bmp file begin
	public static final int TYPE_CUST04_SIZE = 1920;  
	public static final int TYPE_CUST05_SIZE = 300;  
	//Aurora <SQF> <2014-04-22>  for NEW_UI end

    public static final int CACHED_IMAGE_QUALITY = 95;

    public static final int IMAGE_READY = 0;
    public static final int IMAGE_WAIT = 1;
    public static final int IMAGE_ERROR = -1;

    public static final String MIME_TYPE_JPEG = "image/jpeg";

    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 200 * 1024;

    private static int sMicrothumbnailTargetSize = 200;
    private static BitmapPool sMicroThumbPool;
    private static final BytesBufferPool sMicroThumbBufferPool =
            new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);

    private static int sThumbnailTargetSize = 640;
    private static final BitmapPool sThumbPool =
            ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY
            ? new BitmapPool(4)
            : null;

    // TODO: fix default value for latlng and change this.
    public static final double INVALID_LATLNG = 0f;
    
    
    public abstract Job<Bitmap> requestImage(int type);
    public abstract Job<BitmapRegionDecoder> requestLargeImage();
    
    //Aurora <SQF> <2014-05-13>  for NEW_UI begin
    public Job<Bitmap> requestClearThumbnail(int type) {return null;}
    //Aurora <SQF> <2014-05-13>  for NEW_UI end
    
    public MediaItem(Path path, long version) {
        super(path, version);
    }

    public long getDateInMs() {
        return 0;
    }

	////Iuni <lory><2014-02-22> add begin
	public long getDateModifyMs() {
        return 0;
    }

    public String getName() {
        return null;
    }

    public void getLatLong(double[] latLong) {
        latLong[0] = INVALID_LATLNG;
        latLong[1] = INVALID_LATLNG;
    }

    public String[] getTags() {
        return null;
    }

    public Face[] getFaces() {
        return null;
    }

    // The rotation of the full-resolution image. By default, it returns the value of
    // getRotation().
    public int getFullImageRotation() {
        return getRotation();
    }

    public int getRotation() {
        return 0;
    }

    public long getSize() {
        return 0;
    }

    public abstract String getMimeType();

    public String getFilePath() {
        return "";
    }

    // Returns width and height of the media item.
    // Returns 0, 0 if the information is not available.
    public abstract int getWidth();
    public abstract int getHeight();

    // This is an alternative for requestImage() in PhotoPage. If this
    // is implemented, you don't need to implement requestImage().
    public ScreenNail getScreenNail() {
        return null;
    }

    public static int getTargetSize(int type) {
        switch (type) {
            case TYPE_THUMBNAIL:
                return sThumbnailTargetSize;
            case TYPE_MICROTHUMBNAIL:
                return sMicrothumbnailTargetSize;
			//Aurora <paul> <2014-02-27> for NEW_UI begin	
			case TYPE_CUST01:
				return TYPE_CUST01_SIZE;
			case TYPE_CUST02:
				return TYPE_CUST02_SIZE;
			case TYPE_CUST03:
				return TYPE_CUST03_SIZE;	
			//Aurora <SQF> <2014-04-22>  for NEW_UI begin
			case TYPE_CUST04:
				return TYPE_CUST04_SIZE;
			case TYPE_CUST05:
				return TYPE_CUST05_SIZE;
			//Aurora <SQF> <2014-04-22>  for NEW_UI end
			//Aurora <paul> <2014-02-27> for NEW_UI end
            default:
                throw new RuntimeException(
                    "should only request thumb/microthumb from cache");
        }
    }

    public static BitmapPool getMicroThumbPool() {
        if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY && sMicroThumbPool == null) {
            initializeMicroThumbPool();
        }
        return sMicroThumbPool;
    }

    public static BitmapPool getThumbPool() {
        return sThumbPool;
    }

    public static BytesBufferPool getBytesBufferPool() {
        return sMicroThumbBufferPool;
    }

    private static void initializeMicroThumbPool() {
        sMicroThumbPool =
                ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY
                ? new BitmapPool(sMicrothumbnailTargetSize, sMicrothumbnailTargetSize, 16)
                : null;
    }

    public static void setThumbnailSizes(int size, int microSize) {
        sThumbnailTargetSize = size;
        if (sMicrothumbnailTargetSize != microSize) {
            sMicrothumbnailTargetSize = microSize;
            initializeMicroThumbPool();
        }
    }
}
