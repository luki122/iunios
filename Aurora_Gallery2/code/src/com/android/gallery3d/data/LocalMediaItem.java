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

import android.database.Cursor;
import android.graphics.Rect;

import com.android.gallery3d.util.GalleryUtils;

import java.text.DateFormat;
import java.util.Date;

import com.android.gallery3d.util.MediaSetUtils;

//
// LocalMediaItem is an abstract class captures those common fields
// in LocalImage and LocalVideo.
//
public abstract class LocalMediaItem extends MediaItem {

    @SuppressWarnings("unused")
    private static final String TAG = "LocalMediaItem";

    // database fields
    public int id;
    public String caption;
    public String mimeType;
    public long fileSize;
    public double latitude = INVALID_LATLNG;
    public double longitude = INVALID_LATLNG;
    public long dateTakenInMs;
    public long dateAddedInSec;
    public long dateModifiedInSec;
    public String filePath;
    public int bucketId;
    public int width;
    public int height;

    public LocalMediaItem(Path path, long version) {
        super(path, version);
    }

    @Override
    public long getDateInMs() {
        return dateTakenInMs;
    }

	//Iuni <lory><2014-02-22> add begin
	@Override
    public long getDateModifyMs() {
        return dateModifiedInSec;
    }

    @Override
    public String getName() {
        return caption;
    }

    @Override
    public void getLatLong(double[] latLong) {
        latLong[0] = latitude;
        latLong[1] = longitude;
    }

    abstract protected boolean updateFromCursor(Cursor cursor);

    public int getBucketId() {
        return bucketId;
    }

    protected void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            mDataVersion = nextVersionNumber();
        }
    }
    
    //Aurora <SQF> <2014-07-18>  for NEW_UI begin
    private void loadWidthAndHeightFromFile(String filePath) {
    	Rect bounds = new Rect();
    	DecodeUtils.decodeBounds(filePath, bounds);
    	width = bounds.width();
    	height = bounds.height();
    }
    //Aurora <SQF> <2014-07-18>  for NEW_UI end

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_PATH, MediaSetUtils.getDisplayPath(filePath));//paul<2014-02-28> details.addDetail(MediaDetails.INDEX_PATH, filePath);
        details.addDetail(MediaDetails.INDEX_TITLE, caption);
        DateFormat formater = DateFormat.getDateTimeInstance();
        details.addDetail(MediaDetails.INDEX_DATETIME,
                formater.format(new Date(dateTakenInMs)));//paul<2014-02-28> formater.format(new Date(dateModifiedInSec * 1000)));
        //Aurora <SQF> <2014-07-18>  for NEW_UI begin
        if(width == 0 || height == 0) {
        	loadWidthAndHeightFromFile(filePath);
        }
        //Aurora <SQF> <2014-07-18>  for NEW_UI end
        //Aurora <SQF> <2014-04-09>  for NEW_UI begin
        details.addDetail(MediaDetails.INDEX_WIDTH, width);
        details.addDetail(MediaDetails.INDEX_HEIGHT, height);
        //Aurora <SQF> <2014-04-09>  for NEW_UI end
        if (GalleryUtils.isValidLocation(latitude, longitude)) {
            details.addDetail(MediaDetails.INDEX_LOCATION, new double[] {latitude, longitude});
        }
        if (fileSize > 0) details.addDetail(MediaDetails.INDEX_SIZE, fileSize);
        return details;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long getSize() {
        return fileSize;
    }
}
