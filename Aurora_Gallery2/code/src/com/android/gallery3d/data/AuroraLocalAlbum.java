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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
//import com.android.gallery3d.util.MyLog;

import java.util.ArrayList;
import android.provider.MediaStore.MediaColumns;

// LocalAlbumSet lists all media items in one bucket on local storage.
// The media items need to be all images or all videos, but not both.
public class AuroraLocalAlbum extends MediaSet {
    private static final String TAG = "LocalAlbum";
    private static final String[] COUNT_PROJECTION = { "count(*)" };
    static final String[] AURORA_COUNT_PROJECTION =  {// Aurora <paul> <2014-03-07> add
            MediaColumns._ID };

    private static final int INVALID_COUNT = -1;
    private String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private final String[] mProjection;

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;
    private final int mBucketId;
    private final String mName;
    private final boolean mIsImage;
    private final ChangeNotifier mNotifier;
    private final Path mItemPath;
    private int mCachedCount = INVALID_COUNT;
	

    public AuroraLocalAlbum(Path path, GalleryApp application, int bucketId,
            boolean isImage, String name) {
        super(path, nextVersionNumber());
		/*
        MyLog.i("SQF_LOG", "AuroraLocalAlbum::AuroraLocalAlbum 000");
    	MyLog.i("SQF_LOG", "AuroraLocalAlbum::AuroraLocalAlbum isImage:" + isImage + 
				" name:" + name + 
				" bucketId:" + bucketId +
				" path:" + path);
    	*/
        mApplication = application;
        mResolver = application.getContentResolver();
        mBucketId = bucketId;
        mName = getLocalizedName(application.getResources(), bucketId, name);
        mIsImage = isImage;

        if (isImage) {
            mWhereClause = MediaSetUtils.getImageQueryStr(mApplication.getAndroidContext());
            mOrderClause = ImageColumns.DATE_TAKEN + " DESC, "
                    + ImageColumns._ID + " DESC";
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalImage.PROJECTION;
            mItemPath = LocalImage.ITEM_PATH;
            /*
            MyLog.i("SQF_LOG", "AuroraLocalAlbum::AuroraLocalAlbum isImage->true mWhereClause:" + mWhereClause + " mOrderClause:" + mOrderClause + 
            		" mBaseUri:" + mBaseUri + " mItemPath:" + mItemPath
            		);
			*/
        } else {
            mWhereClause = MediaSetUtils.getVideoQueryStr(mApplication.getAndroidContext());
            mOrderClause = VideoColumns.DATE_TAKEN + " DESC, "
                    + VideoColumns._ID + " DESC";
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalVideo.PROJECTION;
            mItemPath = LocalVideo.ITEM_PATH;
            /*
            MyLog.i("SQF_LOG", "AuroraLocalAlbum::AuroraLocalAlbum isImage->false mWhereClause:" + mWhereClause + " mOrderClause:" + mOrderClause + 
            		" mBaseUri:" + mBaseUri + " mItemPath:" + mItemPath
            		);
			*/
        }

        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }

    public AuroraLocalAlbum(Path path, GalleryApp application, int bucketId,
            boolean isImage) {
        this(path, application, bucketId, isImage,
                BucketHelper.getBucketName(
                application.getContentResolver(), bucketId));
		/*
        MyLog.i("SQF_LOG", "AuroraLocalAlbum::AuroraLocalAlbum isImage:" + isImage + 
				" bucketId:" + bucketId +
				" path:" + path);
		*/
    }

    @Override
    public boolean isCameraRoll() {
        return mBucketId == MediaSetUtils.CAMERA_BUCKET_ID;
    }

    @Override
    public Uri getContentUri() {
        if (mIsImage) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID,
                            String.valueOf(mBucketId)).build();
        } else {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID,
                            String.valueOf(mBucketId)).build();
        }
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();
		Cursor cursor = null;
		try {
	        cursor = mResolver.query(
	                uri, mProjection, mWhereClause,
	                MediaSetUtils.geQueryParamStr(mApplication.getAndroidContext()),
	                mOrderClause);
	        if (cursor == null) {
	            Log.w(TAG, "query fail: " + uri);
	            return list;
	        }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column
                Path childPath = mItemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor,
                        dataManager, mApplication, mIsImage);
                list.add(item);
            }
        }  catch (Exception e) {//catch exception
				Log.e(TAG, "getMediaItem query exception!");
        } finally {
        	if (null != cursor){
            	cursor.close();
				cursor = null;
        	}
        }
        return list;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
            DataManager dataManager, GalleryApp app, boolean isImage) {
        synchronized (DataManager.LOCK) {
            LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                if (isImage) {
                    item = new LocalImage(path, app, cursor);
                } else {
                    item = new LocalVideo(path, app, cursor);
                }
            } else {
                item.updateContent(cursor);
            }
            return item;
        }
    }

    // The pids array are sorted by the (path) id.
    public static MediaItem[] getMediaItemById(
            GalleryApp application, boolean isImage, ArrayList<Integer> ids) {
        // get the lower and upper bound of (path) id
        MediaItem[] result = new MediaItem[ids.size()];
        if (ids.isEmpty()) return result;
        int idLow = ids.get(0);
        int idHigh = ids.get(ids.size() - 1);

        // prepare the query parameters
        Uri baseUri;
        String[] projection;
        Path itemPath;
        if (isImage) {
            baseUri = Images.Media.EXTERNAL_CONTENT_URI;
            projection = LocalImage.PROJECTION;
            itemPath = LocalImage.ITEM_PATH;
        } else {
            baseUri = Video.Media.EXTERNAL_CONTENT_URI;
            projection = LocalVideo.PROJECTION;
            itemPath = LocalVideo.ITEM_PATH;
        }

        ContentResolver resolver = application.getContentResolver();
        DataManager dataManager = application.getDataManager();
        Cursor cursor = null;
		try {
			cursor = resolver.query(baseUri, projection, "_id BETWEEN ? AND ?",
	                new String[]{String.valueOf(idLow), String.valueOf(idHigh)},
	                "_id");
	        if (cursor == null) {
	            Log.w(TAG, "query fail" + baseUri);
	            return result;
	        }
       
            int n = ids.size();
            int i = 0;

            while (i < n && cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column

                // Match id with the one on the ids list.
                if (ids.get(i) > id) {
                    continue;
                }

                while (ids.get(i) < id) {
                    if (++i >= n) {
                        return result;
                    }
                }

                Path childPath = itemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor, dataManager,
                        application, isImage);
                result[i] = item;
                ++i;
            }
            return result;
		}  catch (Exception e) {
				Log.e(TAG, "query exception!");
				return result;
		} finally {
			if(null != cursor){
            	cursor.close();
				cursor = null;
			}
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
            String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?",
                new String[]{String.valueOf(id)}, null);
    }

    @Override
    public int getMediaItemCount() {
        if (mCachedCount == INVALID_COUNT) {
			Cursor cursor = null;
            try {
				cursor = mResolver.query(
						mBaseUri, AURORA_COUNT_PROJECTION, mWhereClause,// Aurora <paul> <2014-03-07> COUNT_PROJECTION
						MediaSetUtils.geQueryParamStr(mApplication.getAndroidContext()), null);
				
				if (cursor == null) {
					Log.w(TAG, "query fail");
					return 0;
				}

               // Aurora <paul> <2014-03-07> modified start
                mCachedCount = cursor.getCount();
				// Aurora <paul> <2014-03-07> modified end
            } catch (Exception e) {
				Log.e(TAG, "query exception!");
            	return 0;
        	} finally {
				if(null != cursor) {
                	cursor.close();
					cursor = null;
				}
            }
        }
        //MyLog.i("SQF_LOG", "AuroraLocalAlbum::getMediaItemCount --> count:" + mCachedCount);
        return mCachedCount;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
            if (mIsImage) {
				mWhereClause = MediaSetUtils.getImageQueryStr(mApplication.getAndroidContext());
			} else {
				mWhereClause = MediaSetUtils.getVideoQueryStr(mApplication.getAndroidContext());
			}
        }
        return mDataVersion;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        mResolver.delete(mBaseUri, mWhereClause,
                new String[]{String.valueOf(mBucketId)});
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    public static String getLocalizedName(Resources res, int bucketId,
            String name) {
        if (bucketId == MediaSetUtils.CAMERA_BUCKET_ID) {
            return res.getString(R.string.folder_camera);
        } else if (bucketId == MediaSetUtils.DOWNLOAD_BUCKET_ID) {
            return res.getString(R.string.folder_download);
        } else if (bucketId == MediaSetUtils.IMPORTED_BUCKET_ID) {
            return res.getString(R.string.folder_imported);
        } else if (bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID) {
            return res.getString(R.string.folder_screenshot);
        } else if (bucketId == MediaSetUtils.EDITED_ONLINE_PHOTOS_BUCKET_ID) {
            return res.getString(R.string.folder_edited_online_photos);
        } else {
            return name;
        }
    }
}
