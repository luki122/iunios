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

import android.net.Uri;
import android.provider.MediaStore;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

// MergeAlbum merges items from two or more MediaSets. It uses a Comparator to
// determine the order of items. The items are assumed to be sorted in the input
// media sets (with the same order that the Comparator uses).
//
// This only handles MediaItems, not SubMediaSets.
public class LocalMergeAlbum extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "LocalMergeAlbum";
    private static final int PAGE_SIZE = 80;//SQF modified to 80, originally: 106 //Iuni <lory><2014-03-10> add begin 64;

    private final Comparator<MediaItem> mComparator;
    private final MediaSet[] mSources;

    private String mName;
    private FetchCache[] mFetcher;
    private int mSupportedOperation;
    private int mBucketId;

    // mIndex maps global position to the position of each underlying media sets.
    private TreeMap<Integer, int[]> mIndex = new TreeMap<Integer, int[]>();

    public LocalMergeAlbum(
            Path path, Comparator<MediaItem> comparator, MediaSet[] sources, int bucketId) {
        super(path, INVALID_DATA_VERSION);
        mComparator = comparator;
        mSources = sources;
        mName = sources.length == 0 ? "" : sources[0].getName();
        mBucketId = bucketId;
        for (MediaSet set : mSources) {
            set.addContentListener(this);
        }
        reload();
    }

    @Override
    public boolean isCameraRoll() {
        if (mSources.length == 0) return false;
        for(MediaSet set : mSources) {
            if (!set.isCameraRoll()) return false;
        }
        return true;
    }

    private void updateData() {
		// Aurora <paul> <2014-05-06> start
		if(null == mFetcher){
        	int supported = mSources.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
	        mFetcher = new FetchCache[mSources.length];
	        for (int i = 0, n = mSources.length; i < n; ++i) {
	            mFetcher[i] = new FetchCache(mSources[i]);
	            supported &= mSources[i].getSupportedOperations();
	        }
		
        	mSupportedOperation = supported;
			mName = mSources.length == 0 ? "" : mSources[0].getName();
		}        
		/*
        ArrayList<MediaSet> matches = new ArrayList<MediaSet>();
        int supported = mSources.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        mFetcher = new FetchCache[mSources.length];
        for (int i = 0, n = mSources.length; i < n; ++i) {
            mFetcher[i] = new FetchCache(mSources[i]);
            supported &= mSources[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        mIndex.clear();
        mIndex.put(0, new int[mSources.length]);
        mName = mSources.length == 0 ? "" : mSources[0].getName();
		*/
		// Aurora <paul> <2014-05-06> end
    }

    private void invalidateCache() {
        for (int i = 0, n = mSources.length; i < n; i++) {
            mFetcher[i].invalidate();
        }
        mIndex.clear();
        mIndex.put(0, new int[mSources.length]);
    }

    @Override
    public Uri getContentUri() {
        String bucketId = String.valueOf(mBucketId);
        if (ApiHelper.HAS_MEDIA_PROVIDER_FILES_TABLE) {
            return MediaStore.Files.getContentUri("external").buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID, bucketId)
                    .build();
        } else {
            // We don't have a single URL for a merged image before ICS
            // So we used the image's URL as a substitute.
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendQueryParameter(LocalSource.KEY_BUCKET_ID, bucketId)
                    .build();
        }
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getMediaItemCount() {
        return getTotalMediaItemCount();
    }

	@Override
	public ArrayList<MediaItem> getMediaItem(int start, int count) {
		synchronized (this) {
			// First find the nearest mark position <= start.
			ArrayList<MediaItem> result = new ArrayList<MediaItem>();

			SortedMap<Integer, int[]> head = mIndex.headMap(start + 1);
			// Iuni <lory><2014-03-12> add begin
			int markPos = 0;
			if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
				try {
					markPos = head.lastKey();
				} catch (Exception e) {
	    			Log.i(TAG, "zll ---- getMediaItem start:"+start+",count:"+count);
				}
			} else {
				markPos = head.lastKey();
			}
			// Iuni <lory><2014-03-12> add begin
			// int markPos = head.lastKey();

			int[] subPos = null;
			try {// lory modify
				subPos = head.get(markPos).clone();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (subPos == null) {
				return result;
			}

			// int[] subPos = head.get(markPos).clone();
			MediaItem[] slot = new MediaItem[mSources.length];

			int size = mSources.length;

			// fill all slots
			for (int i = 0; i < size; i++) {
				slot[i] = mFetcher[i].getItem(subPos[i]);
			}

			for (int i = markPos; i < start + count; i++) {
				int k = -1; // k points to the best slot up to now.
				for (int j = 0; j < size; j++) {
					if (slot[j] != null) {
	                    if (k == -1 || mComparator.compare(slot[j], slot[k]) < 0) {
	                        k = j;
						}
					}
				}

				// If we don't have anything, all streams are exhausted.
	            if (k == -1) break;

				// Pick the best slot and refill it.
				subPos[k]++;
				if (i >= start) {
					result.add(slot[k]);
				}

				slot[k] = mFetcher[k].getItem(subPos[k]);

				// Periodically leave a mark in the index, so we can come back
				// later.
				if ((i + 1) % PAGE_SIZE == 0) {
					mIndex.put(i + 1, subPos.clone());
					// Log.i("SQF_LOG",
					// "LocalMergeAlbum::getMediaItem mIndex.put" + (i + 1) );
				}
			}
			return result;
		}
	}
	
    //Aurora <SQF> <2014-08-06>  for NEW_UI begin
	@Override
	public ArrayList<MediaItem> getMediaItem(int start, int count, Path currentItemPath) {
		synchronized (this) {
			// First find the nearest mark position <= start.
			ArrayList<MediaItem> result = new ArrayList<MediaItem>();

			SortedMap<Integer, int[]> head = mIndex.headMap(start + 1);
			// Iuni <lory><2014-03-12> add begin
			int markPos = 0;
			if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
				try {
					markPos = head.lastKey();
				} catch (Exception e) {
	    			Log.i(TAG, "zll ---- getMediaItem start:"+start+",count:"+count);
				}
			} else {
				markPos = head.lastKey();
			}
			// Iuni <lory><2014-03-12> add begin
			// int markPos = head.lastKey();

			int[] subPos = null;
			try {// lory modify
				subPos = head.get(markPos).clone();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (subPos == null) {
				return result;
			}

			// int[] subPos = head.get(markPos).clone();
			MediaItem[] slot = new MediaItem[mSources.length];

			int size = mSources.length;

			// fill all slots
			for (int i = 0; i < size; i++) {
				slot[i] = mFetcher[i].getItem(subPos[i], currentItemPath);
			}

			for (int i = markPos; i < start + count; i++) {
				int k = -1; // k points to the best slot up to now.
				for (int j = 0; j < size; j++) {
					if (slot[j] != null) {
	                    if (k == -1 || mComparator.compare(slot[j], slot[k]) < 0) {
	                        k = j;
						}
					}
				}

				// If we don't have anything, all streams are exhausted.
	            if (k == -1) break;

				// Pick the best slot and refill it.
				subPos[k]++;
				if (i >= start) {
					result.add(slot[k]);
				}

				slot[k] = mFetcher[k].getItem(subPos[k], currentItemPath);

				// Periodically leave a mark in the index, so we can come back
				// later.
				if ((i + 1) % PAGE_SIZE == 0) {
					mIndex.put(i + 1, subPos.clone());
					// Log.i("SQF_LOG",
					// "LocalMergeAlbum::getMediaItem mIndex.put" + (i + 1) );
				}
			}
			return result;
		}
    }
	//Aurora <SQF> <2014-08-06>  for NEW_UI end
	

 
    @Override
    public int getTotalMediaItemCount() {
    	synchronized(this){// Aurora <paul> <2014-05-06> add
	        int count = 0;
	        for (MediaSet set : mSources) {
				count += set.getTotalMediaItemCount();
	        }
	        return count;
    	}
    }

    @Override
    public long reload() {
    	synchronized(this){// Aurora <paul> <2014-05-06> add
	        boolean changed = false;
	        for (int i = 0, n = mSources.length; i < n; ++i) {
	            if (mSources[i].reload() > mDataVersion) changed = true;
	        }
	        if (changed) {
	            mDataVersion = nextVersionNumber();
	            updateData();
	            invalidateCache();
	        }
	        return mDataVersion;
    	}
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    @Override
    public int getSupportedOperations() {
        return mSupportedOperation;
    }

    @Override
    public void delete() {
        for (MediaSet set : mSources) {
            set.delete();
        }
    }

    @Override
    public void rotate(int degrees) {
        for (MediaSet set : mSources) {
            set.rotate(degrees);
        }
    }

    private class FetchCache {
        private MediaSet mBaseSet;
        private SoftReference<ArrayList<MediaItem>> mCacheRef;
        private int mStartPos;

        public FetchCache(MediaSet baseSet) {
            mBaseSet = baseSet;
        }

        public void invalidate() {
            mCacheRef = null;
        }
        
        public MediaItem getItem(int index) {
            boolean needLoading = false;
            ArrayList<MediaItem> cache = null;
            if (mCacheRef == null
                    || index < mStartPos || index >= mStartPos + PAGE_SIZE) {
                needLoading = true;
            } else {
                cache = mCacheRef.get();
                if (cache == null) {
                    needLoading = true;
                }
            }

            if (needLoading) {
                cache = mBaseSet.getMediaItem(index, PAGE_SIZE);
                mCacheRef = new SoftReference<ArrayList<MediaItem>>(cache);
                mStartPos = index;
            }
            
            for(int i=0; i<mCacheRef.get().size(); i++ ) {
            	MediaItem item = mCacheRef.get().get(i);
            }
            if (index < mStartPos || index >= mStartPos + cache.size()) {
                return null;
            }
            return cache.get(index - mStartPos);
        }
        
        //Aurora <SQF> <2014-07-22>  for NEW_UI begin
        public MediaItem getItem(int index, Path currentPath) {
            boolean needLoading = false;
            ArrayList<MediaItem> cache = null;
            if (mCacheRef == null
                    || index < mStartPos || index >= mStartPos + PAGE_SIZE) {
                needLoading = true;
            } else {
                cache = mCacheRef.get();
                if (cache == null) {
                    needLoading = true;
                }
            }
            if(cache != null && ! needLoading) {
            	for(int i=0; i<cache.size(); i ++) {
            		//Log.i("SQF_LOG", "......cache.get(i).mPath:" + cache.get(i).mPath);
            		if(cache.get(i).mPath.equals(currentPath)) {
            			//Log.i("SQF_LOG", "...............................set needLoading = true; ...........................");
            			needLoading = true;
            			break;
            		}
            	}
            }
            //Log.i("SQF_LOG", "................ needLoading = true : " + (needLoading = true));
            if (needLoading) {
                cache = mBaseSet.getMediaItem(index, PAGE_SIZE);
                mCacheRef = new SoftReference<ArrayList<MediaItem>>(cache);
                mStartPos = index;
            }
            
            for(int i=0; i<mCacheRef.get().size(); i++ ) {
            	MediaItem item = mCacheRef.get().get(i);
            }
            if (index < mStartPos || index >= mStartPos + cache.size()) {
                return null;
            }
            return cache.get(index - mStartPos);
        }
        //Aurora <SQF> <2014-07-15>  for NEW_UI end
        
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }
}
