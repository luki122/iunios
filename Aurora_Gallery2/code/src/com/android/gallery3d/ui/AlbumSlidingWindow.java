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

package com.android.gallery3d.ui;

import java.util.List;

import android.R.integer;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.AlbumDataLoader.Position;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.MediaObject.PanoramaSupportCallback;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.JobLimiter;
import com.android.gallery3d.common.BitmapUtils;//Aurora <paul> <2014-02-27> for NEW_UI
import com.android.gallery3d.app.AlbumPage;//Aurora <paul> <2014-02-27> for NEW_UI

public class AlbumSlidingWindow implements AlbumDataLoader.DataListener {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumSlidingWindow";
    private static final int MSG_UPDATE_ENTRY = 0;

    //Aurora <SQF> <2014-05-13>  for NEW_UI begin
    private static final int MSG_UPDATE_ENTRY_2 = 1;
    private static final int MSG_START_LOAD_THUMBNAIL_DELAYED = 2;
    private static final int MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED = 3;
    
    private static final int THUMBNAIL_DELAY_TIME = 100;
    private static final int CLEAR_THUMBNAIL_DELAY_TIME = 300;
    //Aurora <SQF> <2014-05-13>  for NEW_UI end

    private static final int JOB_LIMIT = 2;//Iuni <lory><2014-02-19> modify 2->3

    public static interface Listener {
        public void onSizeChanged(int size);
        public void onContentChanged();
    }

    public static class AlbumEntry {
        public MediaItem item;
        public Path path;
        public boolean isPanorama;
        public int rotation;
        public int mediaType;
        public boolean isWaitDisplayed;
        public TiledTexture bitmapTexture;
		public Bitmap bitmap;//Aurora <paul> <2014-02-27> for NEW_UI
        public Texture content;
        
        //Aurora <SQF> <2014-05-13>  for NEW_UI begin
        //public Bitmap cropCenterBitmap;
        public TiledTexture cropCenterBitmapTexture;
        //public Texture cropCenterContent;
      	//Aurora <SQF> <2014-05-13>  for NEW_UI end

        private BitmapLoader contentLoader;
		//Aurora <SQF> <2014-05-13>  for NEW_UI begin
        private ClearThumbnailLoader contentLoader2;
        //Aurora <SQF> <2014-05-13>  for NEW_UI end
        private PanoSupportListener mPanoSupportListener;
    }

    private final AlbumDataLoader mSource;
    private final AlbumEntry mData[];
    private final SynchronizedHandler mHandler;
    //private final SynchronizedHandler mClearThumbnailHandler;
    private final JobLimiter mThreadPool;
    private final TiledTexture.Uploader mTileUploader;

    private int mSize;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private Listener mListener;

    private int mActiveRequestCount = 0;
    
    //Aurora <SQF> <2014-05-30>  for NEW_UI begin
    private int mActiveClearThumbnailRequestCount = 0;
    //Aurora <SQF> <2014-05-30>  for NEW_UI end
    private boolean mIsActive = false;
    
    //Aurora <SQF> <2014-05-13>  for NEW_UI begin
    private boolean mHasSpeedLimit = false;
    private AlbumSlotRenderer mSlotRenderer;
    //Aurora <SQF> <2014-05-13>  for NEW_UI end
    

    private class PanoSupportListener implements PanoramaSupportCallback {
        public final AlbumEntry mEntry;
        public PanoSupportListener (AlbumEntry entry) {
            mEntry = entry;
        }
        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360) {
            if (mEntry != null) mEntry.isPanorama = isPanorama;
        }
    }

    public AlbumSlidingWindow(AbstractGalleryActivity activity, AlbumSlotRenderer slotRenderer,
            AlbumDataLoader source, int cacheSize) {
        source.setDataListener(this);
        mSource = source;
        //Aurora <SQF> <2014-05-15>  for NEW_UI begin
        mSlotRenderer = slotRenderer;
        //Aurora <SQF> <2014-05-15>  for NEW_UI end
        
        mData = new AlbumEntry[cacheSize];
        mSize = source.size();

        mHandler = new SynchronizedHandler(activity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
            	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
            	//ORIGINALLY:
            	//Utils.assertTrue(message.what == MSG_UPDATE_ENTRY);
                //((ThumbnailLoader) message.obj).updateEntry();
            	//SQF MODIFIED TO:
            	if(message.what == MSG_UPDATE_ENTRY) {
            		if(mSlotRenderer.isSlotViewUserActionNotFinished() || mSlotRenderer.isDrawingEnteringOrLeavingEffect()) {
            			//Log.i("SQF_LOG", "AlbumSlidingWindow::AlbumSlidingWindow mHandler --> message.what == MSG_UPDATE_ENTRY");
            			Message msg = Message.obtain(message);
            			this.sendMessageDelayed(msg, THUMBNAIL_DELAY_TIME);
            		} else {
            			((ThumbnailLoader) message.obj).updateEntry();
            		}
            	} else if(message.what == MSG_UPDATE_ENTRY_2) {
            		//Log.i("SQF_LOG", "mHandler message.what == MSG_UPDATE_ENTRY_2 --> isDrawingEnteringOrLeavingEffect: " + (mSlotRenderer.isDrawingEnteringOrLeavingEffect()));
            		if(mSlotRenderer.isSlotViewUserActionNotFinished() || mSlotRenderer.isDrawingEnteringOrLeavingEffect()) {
            			Message msg = Message.obtain(message);
            			this.sendMessageDelayed(msg, CLEAR_THUMBNAIL_DELAY_TIME);
            		} else {
            			//Log.i("SQF_LOG", "mHandler message.what == MSG_UPDATE_ENTRY_2 --> ");
            			((ClearThumbnailLoader) message.obj).updateEntry();
            		}
            		
            		//((ClearThumbnailLoader) message.obj).updateEntry();
            	} else if(message.what == MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED) {
            		int slotIndex = message.arg1;
        			if(! isActiveSlot(slotIndex)) return;
        			//Log.i("SQF_LOG", "mClearThumbnailHandler message.what == MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED --> isDrawingEnteringOrLeavingEffect: " + (mSlotRenderer.isDrawingEnteringOrLeavingEffect()));
        			if(mSlotRenderer.isSlotViewUserActionNotFinished() || mSlotRenderer.isDrawingEnteringOrLeavingEffect()) {
        				//Log.i("SQF_LOG", "delay...................................................");
        				Message msg = Message.obtain(message);
        				this.sendMessageDelayed(msg, 300);
        			} else {
        				
        				AlbumEntry entry = (AlbumEntry)message.obj;
        				if(entry != null && entry.content != null && entry.cropCenterBitmapTexture == null && !mHasSpeedLimit) {
        	        		if(entry.contentLoader2 != null) {
        	        			//Log.i("SQF_LOG", "contentLoader2.startLoad() -------------- slotIndex:" + slotIndex);
        	        			entry.contentLoader2.startLoad();
        	        		}
        	        	}
        			}
            	} else if(message.what == MSG_START_LOAD_THUMBNAIL_DELAYED) {
            		int slotIndex = message.arg1;
        			if(! isActiveSlot(slotIndex)) {
        				//Log.i("SQF_LOG", "slotIndex: ----> return " + slotIndex);
        				return;
        			}
        			//Log.i("SQF_LOG", "mClearThumbnailHandler message.what == MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED --> isDrawingEnteringOrLeavingEffect: " + (mSlotRenderer.isDrawingEnteringOrLeavingEffect()));
        			if(mSlotRenderer.isSlotViewUserActionNotFinished() || mSlotRenderer.isDrawingEnteringOrLeavingEffect()) {
        				//Log.i("SQF_LOG", "delay...................................................");
        				Message msg = Message.obtain(message);
        				this.sendMessageDelayed(msg, THUMBNAIL_DELAY_TIME);
        			} else {
        				AlbumEntry entry = (AlbumEntry)message.obj;
        				if(entry != null /*&& entry.content == null*/) {
        	        		if(entry.contentLoader != null) {
        	        			//Log.i("SQF_LOG", "contentLoader2.startLoad() -------------- slotIndex:" + slotIndex);
        	        			entry.contentLoader.startLoad();
        	        		}
        	        	}
        			}
            	}
            	//Aurora <SQF> <2014-05-13>  for NEW_UI end
            }
        };

      //Aurora <SQF> <2014-05-13>  for NEW_UI begin
        /*
       	mClearThumbnailHandler = new SynchronizedHandler(activity.getGLRoot()) {
    		@Override
    		public void handleMessage(Message msg) {
    			// TODO Auto-generated method stub
    			//super.handleMessage(msg);
    			if(MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED != msg.what) return;
    			int slotIndex = msg.arg1;
    			if(! isActiveSlot(slotIndex)) return;
    			//Log.i("SQF_LOG", "mClearThumbnailHandler message.what == MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED --> isDrawingEnteringOrLeavingEffect: " + (mSlotRenderer.isDrawingEnteringOrLeavingEffect()));
    			if(mSlotRenderer.isSlotViewUserActionNotFinished() || mSlotRenderer.isDrawingEnteringOrLeavingEffect()) {
    				//Log.i("SQF_LOG", "delay...................................................");
    				Message message = Message.obtain(msg);
    				this.sendMessageDelayed(message, 300);
    			} else {
    				
    				AlbumEntry entry = (AlbumEntry)msg.obj;
    				if(entry != null && entry.content != null && entry.cropCenterBitmapTexture == null && !mHasSpeedLimit) {
    	        		if(entry.contentLoader2 != null) {
    	        			//Log.i("SQF_LOG", "contentLoader2.startLoad() -------------- slotIndex:" + slotIndex);
    	        			entry.contentLoader2.startLoad();
    	        		}
    	        	}
    			}
    		}
        	
        };
        */

        mThreadPool = new JobLimiter(activity.getThreadPool(), JOB_LIMIT);
        mTileUploader = new TiledTexture.Uploader(activity.getGLRoot());
    }
    
    
    //Aurora <SQF> <2014-05-13>  for NEW_UI begin
    public void setThumbnailLoadInfo(boolean speedLimit) {
    	//Log.i("SQF_LOG", "AlbumSlidingWindow::setHasSpeedLimit --> " + speedLimit);
    	mHasSpeedLimit = speedLimit;
    }
    //Aurora <SQF> <2014-05-13>  for NEW_UI end

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public AlbumEntry get(int slotIndex) {
    	
    	if (false) {
        	Position adapterPosition = mSource.translatePosition(slotIndex);
        	//Log.i(TAG, "zll --- AlbumEntry get 1 slotIndex:"+slotIndex+",adapterPosition.mPosition:"+adapterPosition.mPosition);
			if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                adapterPosition.mPosition != SLID_POSITION_FILLER ) {
				
				if (!isActiveSlot(slotIndex)) {
					//Log.i(TAG, "zll --- AlbumEntry get 2 slotIndex:"+slotIndex+",adapterPosition.mPosition:"+adapterPosition.mPosition);
		            Utils.fail("zll invalid slot: %s outsides (%s, %s)",
		            		slotIndex, mActiveStart, mActiveEnd);
		        }
				//Log.i(TAG, "zll --- AlbumEntry get 3 slotIndex:"+slotIndex+",adapterPosition.mPosition:"+adapterPosition.mPosition);
				/*if (mData[adapterPosition.mPosition % mData.length] == null) {
					Log.i(TAG, "zll ----- eeee 7 slotIndex:"+slotIndex+",adapterPosition.mPosition:"+adapterPosition.mPosition);
				}*/
		        return mData[adapterPosition.mPosition % mData.length];
			}
			return null;
		} else {
			//Log.i(TAG, "zll --- AlbumEntry get 2 slotIndex:"+slotIndex);
			if (!isActiveSlot(slotIndex)) {
	            /*Utils.fail("zll invalid slot: %s outsides (%s, %s)",
	                    slotIndex, mActiveStart, mActiveEnd);lory modify 2014.4.22
	                    */
				if (slotIndex < mActiveStart) {
					slotIndex = mActiveStart;
				}  else if (slotIndex >= mActiveEnd) {
					slotIndex = mActiveEnd-1;
				}
	        }
			if(slotIndex < 0) return null;
			return mData[slotIndex % mData.length];
		}
    }

    public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= mActiveStart && slotIndex < mActiveEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;

        if (!mIsActive) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
            mSource.setActiveWindow(contentStart, contentEnd);
            return;
        }

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
        	//Log.i(TAG, "zll ----- eeee 2 setContentWindow");
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            //mSource.setActiveWindow(contentStart, contentEnd);
            if (false) {
            	int t_header1 = mSource.getAlbumDLNumHeaders()+1;
            	int t_contentStart1 = contentStart > t_header1 ? contentStart - t_header1 : 0;
            	//Log.i(TAG, "zll ----- eeee 2.1 setContentWindow t_header1:"+t_header1+",t_contentStart1:"+t_contentStart1);
            	mSource.setActiveWindow(t_contentStart1, contentEnd);
            	
            	for (int i = contentStart; i < contentEnd; ++i) {
                    //myprepareSlotContent(i, contentStart, contentEnd);
            		prepareSlotContent(i);
                }
			} else {
				mSource.setActiveWindow(contentStart, contentEnd);
				for (int i = contentStart; i < contentEnd; ++i) {
	                prepareSlotContent(i);
	            }
			}
            
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeSlotContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            if (false) {
            	int t_header = mSource.getAlbumDLNumHeaders()+1;
            	int t_contentStart = contentStart > t_header ? contentStart - t_header : 0;
            	mSource.setActiveWindow(t_contentStart, contentEnd);
            	for (int i = contentStart, n = mContentStart; i < n; ++i) {
            		prepareSlotContent(i);
                }
                for (int i = mContentEnd; i < contentEnd; ++i) {
                	prepareSlotContent(i);
                }
			} else {
				mSource.setActiveWindow(contentStart, contentEnd);
				for (int i = contentStart, n = mContentStart; i < n; ++i) {
	                prepareSlotContent(i);
	            }
	            for (int i = mContentEnd; i < contentEnd; ++i) {
	                prepareSlotContent(i);
	            }
			}
            
        }

        mContentStart = contentStart;
        mContentEnd = contentEnd;
    }

    public void setActiveWindow(int start, int end) {
    	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
    		int t1 = mSource.getHeaderNumBySlotIndex(start);
    		int t_start = start > (t1 +1)?(start - t1 -1):0;
    		int t2 = mSource.getHeaderNumBySlotIndex(end);
        	int t_end = end > t2?(end - t2):0;
        	t_end = Math.min(mSize, t_end);        	
        	if (!(t_start <= t_end && t_end - t_start <= mData.length && t_end <= mSize)) {
				return;										
            }
        	AlbumEntry data[] = mData;
            mActiveStart = t_start;
            mActiveEnd = t_end;
             int contentStart = Utils.clamp((t_start + t_end) / 2 - data.length / 2,
                    0, Math.max(0, mSize - data.length));
            int contentEnd = Math.min(contentStart + data.length, mSize);
            setContentWindow(contentStart, contentEnd);
            updateTextureUploadQueue();
            if (mIsActive) updateAllImageRequests();
            
        	return;
		} 
    	
        if (!(start <= end && end - start <= mData.length && end <= mSize)) {
            Utils.fail("zll ---- %s, %s, %s, %s", start, end, mData.length, mSize);
        }
        AlbumEntry data[] = mData;

        mActiveStart = start;
        mActiveEnd = end;

        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2,
                0, Math.max(0, mSize - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSize);
        setContentWindow(contentStart, contentEnd);
        updateTextureUploadQueue();
        if (mIsActive) updateAllImageRequests();
    }

    private void uploadBgTextureInSlot(int index) {
        if (index < mContentEnd && index >= mContentStart) {
        	AlbumEntry entry = mData[index % mData.length];
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            	if (entry != null && entry.bitmapTexture != null) {
                    mTileUploader.addTexture(entry.bitmapTexture);
                }
			} else {
				if (entry.bitmapTexture != null) {
	                mTileUploader.addTexture(entry.bitmapTexture);
	            }
			}
        	
        }
    }

    private void updateTextureUploadQueue() {
        if (!mIsActive) return;
        mTileUploader.clear();

        // add foreground textures
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
        	if (false) {
            	Position adapterPosition = mSource.translatePosition(i);
    			if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                    adapterPosition.mPosition != SLID_POSITION_FILLER ) {
    				
	    				AlbumEntry entry = mData[adapterPosition.mPosition % mData.length];
			            if (entry != null && entry.bitmapTexture != null) {
			                mTileUploader.addTexture(entry.bitmapTexture);
			            }
    			} 
    		} else {
    			AlbumEntry entry = mData[i % mData.length];
	            if (entry != null && entry.bitmapTexture != null) {
	                mTileUploader.addTexture(entry.bitmapTexture);
	            }
			}
           
        }

        // add background textures
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        //Log.i(TAG, "zll ---- updateTextureUploadQueue 1.5 range:"+range);
        for (int i = 0; i < range; ++i) {
        	if (false) {
            	Position adapterPosition = mSource.translatePosition(i);
    			if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                    adapterPosition.mPosition != SLID_POSITION_FILLER ) {
    				
    				uploadBgTextureInSlot(mActiveEnd + adapterPosition.mPosition);
    	            uploadBgTextureInSlot(mActiveStart - adapterPosition.mPosition - 1);
    			}
    		} else {
    			uploadBgTextureInSlot(mActiveEnd + i);
                uploadBgTextureInSlot(mActiveStart - i - 1);
			}
        	
        }
    }

    // We would like to request non active slots in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        //Log.i(TAG, "zll ---- xxxx 3 range:"+range+",mContentEnd:"+mContentEnd+",mActiveEnd:"+mActiveEnd+",mActiveStart:"+mActiveStart+",mContentStart:"+mContentStart+",length:"+mData.length);
        for (int i = 0 ;i < range; ++i) {
        	
        	if (false) {
            	Position adapterPosition = mSource.translatePosition(i);
            	//Log.i(TAG, "zll ---- xxxx 3 adapterPosition.mPosition:"+adapterPosition.mPosition+",i:"+i+",mActiveEnd:"+mActiveEnd+",mActiveStart:"+mActiveStart);
    			if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                    adapterPosition.mPosition != SLID_POSITION_FILLER ) {
    				
    				requestSlotImage(mActiveEnd + adapterPosition.mPosition);
    	            requestSlotImage(mActiveStart - adapterPosition.mPosition - 1);
    			}
    		} else {
    			requestSlotImage(mActiveEnd + i);
                requestSlotImage(mActiveStart - 1 - i);
			}
        	
        }
    }
	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
    private void startLoadClearThumbnail(int slotIndex) {
    	if(mHasSpeedLimit) return;
    	if (slotIndex < mActiveStart || slotIndex >= mActiveEnd) return;
        AlbumEntry entry = mData[slotIndex % mData.length];
        if(null == entry) return;
    	if(mSlotRenderer.isSlotViewUserActionNotFinished() || mActiveRequestCount != 0  || 
    			mSlotRenderer.isDrawingEnteringOrLeavingEffect() || 
    			mActiveClearThumbnailRequestCount > 0) {
    		//Log.i("SQF_LOG", "startLoadClearThumbnail delayed");
			//Message msg = Message.obtain(mClearThumbnailHandler, MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED, slotIndex, 0, entry);
			Message msg = Message.obtain(mHandler, MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED, slotIndex, 0, entry);
			mHandler.sendMessageDelayed(msg, CLEAR_THUMBNAIL_DELAY_TIME);
		} else {
			if(entry.content != null && entry.cropCenterBitmapTexture == null && !mHasSpeedLimit) {
        		if(entry.contentLoader2 != null /*&& !entry.contentLoader2.isRequestInProgress()*/) {
        			entry.contentLoader2.startLoad();
        			if(entry.contentLoader2.isRequestInProgress()) {
        				mActiveClearThumbnailRequestCount ++;
        			}
        		}
        	}
		}
    }
    
    private void startLoadThumbnail(int slotIndex) {
    	if (slotIndex < mActiveStart || slotIndex >= mActiveEnd) return;
        AlbumEntry entry = mData[slotIndex % mData.length];
        if(null == entry) return;
        
        //Aurora <SQF> <2014-08-19>  for NEW_UI begin
        if(mSlotRenderer.isLongScale()) {
        	if(entry.content == null) {
        		if(entry.contentLoader != null /*&& !entry.contentLoader.isRequestInProgress()*/) {
        			entry.contentLoader.startLoad();
        		}
        	}
        	return;
        }
        //Aurora <SQF> <2014-08-19>  for NEW_UI end
        
    	if(mSlotRenderer.isSlotViewUserActionNotFinished() || mSlotRenderer.isDrawingEnteringOrLeavingEffect()) {
    		//Log.i("SQF_LOG", "startLoadClearThumbnail delayed");
			//Message msg = Message.obtain(mClearThumbnailHandler, MSG_START_LOAD_CLEAR_THUMBNAIL_DELAYED, slotIndex, 0, entry);
			Message msg = Message.obtain(mHandler, MSG_START_LOAD_THUMBNAIL_DELAYED, slotIndex, 0, entry);
			mHandler.sendMessageDelayed(msg, THUMBNAIL_DELAY_TIME);
		} else {
			if(entry.content == null) {
        		if(entry.contentLoader != null /*&& !entry.contentLoader.isRequestInProgress()*/) {
        			entry.contentLoader.startLoad();
        		}
        	}
		}
    }
    
    //Aurora <SQF> <2014-05-13>  for NEW_UI end

    // return whether the request is in progress or not
    private boolean requestSlotImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return false;
        //Log.i("SQF_LOG", "AlbumSlidingWindow::requestSlotImage --> slotIndex:" + slotIndex);
        AlbumEntry entry = mData[slotIndex % mData.length];
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
        	if(isActiveSlot(slotIndex) && entry != null && entry.content != null && entry.cropCenterBitmapTexture == null) {
        		startLoadClearThumbnail(slotIndex);
        	}
        	//Aurora <SQF> <2014-05-13>  for NEW_UI end
        	if (entry == null || entry.content != null || entry.item == null) return false;
		} else {
			if (entry.content != null || entry.item == null) return false;
		}

        //Log.i(TAG, "zll ---- requestSlotImage 1.2 slotIndex:"+slotIndex);
        // Set up the panorama callback
        entry.mPanoSupportListener = new PanoSupportListener(entry);
        entry.item.getPanoramaSupport(entry.mPanoSupportListener);
        
        //Aurora <SQF> <2014-05-26>  for NEW_UI begin
        //ORIGINALLY:
        //entry.contentLoader.startLoad();
        //SQF MODIFIED TO:
        startLoadThumbnail(slotIndex);
        //Aurora <SQF> <2014-05-26>  for NEW_UI end
        
       //Log.i(TAG, "zll ---- xxxx 2 slotIndex:"+slotIndex);
        return entry.contentLoader.isRequestInProgress();
    }

    private void cancelNonactiveImages() {
        int range = Math.max((mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
        	if (false) {
            	Position adapterPosition = mSource.translatePosition(i);
    			if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                    adapterPosition.mPosition != SLID_POSITION_FILLER ) {
    				
    				cancelSlotImage(mActiveEnd + adapterPosition.mPosition);
    	            cancelSlotImage(mActiveStart - 1 - adapterPosition.mPosition);
    			}
    		} else {
    			cancelSlotImage(mActiveEnd + i);
                cancelSlotImage(mActiveStart - 1 - i);
			}
        }
    }

    private void cancelSlotImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return;
        AlbumEntry item = mData[slotIndex % mData.length];
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	//Aurora <SQF> <2014-05-13>  for NEW_UI begin
        	//ORIGINALLY:
        	//if (item != null && item.contentLoader != null) item.contentLoader.cancelLoad();
        	//SQF MODIFIED TO:
        	if (item != null) {
        		//Log.i("SQF_LOG", "cancelSlotImage slotIndex:"+slotIndex);
        		if(item.contentLoader != null) item.contentLoader.cancelLoad();
        		if(item.contentLoader2 != null) item.contentLoader2.cancelLoad();
        	}
			//Aurora <SQF> <2014-05-13>  for NEW_UI end
		} else {
			if (item.contentLoader != null) item.contentLoader.cancelLoad();
		}
        
    } 

    private static final int SLID_POSITION_FILLER = -0x01;
    private static final int SLID_POSITION_HEADER = -0x02;
    
    private void freeSlotContent(int slotIndex) {
        AlbumEntry data[] = mData;
        
        if (false) {//MySelfBuildConfig.USEGALLERY3D_FLAG
        	Position adapterPosition = mSource.translatePosition(slotIndex);
        	//Log.i(TAG, "zll ----- eeee 8.0 freeSlotContent 0.2 slotIndex:"+slotIndex +",adapterPosition.mPosition:"+adapterPosition.mPosition);
        	if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                adapterPosition.mPosition != SLID_POSITION_FILLER ) {
        		int index = adapterPosition.mPosition % data.length;
                AlbumEntry entry = data[index];
                if (entry != null && entry.contentLoader != null) entry.contentLoader.recycle();
                if (entry != null && entry.bitmapTexture != null) entry.bitmapTexture.recycle();
                if(entry != null &&  entry.bitmap != null) entry.bitmap.recycle();//Aurora <paul> <2014-02-27> for NEW_UI
                //Log.i(TAG, "zll ----- eeee 8.1 freeSlotContent 0.2 slotIndex:"+slotIndex +",adapterPosition.mPosition:"+adapterPosition.mPosition);
                //Log.i(TAG, "zll ---- freeSlotContent 1 adapterPosition.mPosition:"+adapterPosition.mPosition+",index:"+index+",slotIndex:"+slotIndex);
                data[index] = null;
			} else {
				//Log.i(TAG, "zll ---- freeSlotContent 0.1 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex);
			}
		} else {
			int index = slotIndex % data.length;
	        AlbumEntry entry = data[index];
	        //Log.i(TAG, "zll ----- eeee 8 freeSlotContent 0.2 slotIndex:"+slotIndex);
	        if (entry != null) {
	        	if (entry.contentLoader != null) entry.contentLoader.recycle();
	        	
	            if (entry.bitmapTexture != null) entry.bitmapTexture.recycle();
	            if (entry.bitmap != null) entry.bitmap.recycle();//Aurora <paul> <2014-02-27> for NEW_UI
	            //Aurora <SQF> <2014-05-27>  for NEW_UI begin
	        	if (entry.contentLoader2 != null) entry.contentLoader2.recycle();
	        	if (entry.cropCenterBitmapTexture != null) entry.cropCenterBitmapTexture.recycle();
	        	//Aurora <SQF> <2014-05-27>  for NEW_UI end
			}
            
	        data[index] = null;
		}
    }
    
    private void myprepareSlotContent(int slotIndex, int start, int end) {
    	AlbumEntry entry = new AlbumEntry();
    	
    	Position adapterPosition = mSource.translatePosition(slotIndex);
    	//Log.i(TAG, "zll ----- eeee  6 prepareSlotContent 0 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex+",start:"+start+",end:"+end);
		if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
            adapterPosition.mPosition != SLID_POSITION_FILLER  /*&&
            adapterPosition.mPosition >= start &&
            adapterPosition.mPosition <= end*/) {
			MediaItem item = mSource.get(adapterPosition.mPosition); // item could be null;
			entry.item = item;
	        entry.mediaType = (item == null)
	                ? MediaItem.MEDIA_TYPE_UNKNOWN
	                : entry.item.getMediaType();
	        entry.path = (item == null) ? null : item.getPath();
	        entry.rotation = (item == null) ? 0 : item.getRotation();
	        entry.contentLoader = new ThumbnailLoader(adapterPosition.mPosition, entry.item);
	        mData[adapterPosition.mPosition % mData.length] = entry;
	        /*if (mData[adapterPosition.mPosition % mData.length] == null) {
				Log.i(TAG, "zll ----- eeee 6 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex+",start:"+start+",end:"+end);
			} else {
				Log.i(TAG, "zll ----- eeee 9 add adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex+",start:"+start+",end:"+end);
			}*/
		} else {
			//Log.i(TAG, "zll ---- prepareSlotContent 0.2 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex+",start:"+start+",end:"+end);
		}
		
		return;
	}

    private void prepareSlotContent(int slotIndex) {
        AlbumEntry entry = new AlbumEntry();
        
        if (false) {
        	Position adapterPosition = mSource.translatePosition(slotIndex);
        	//Log.i(TAG, "zll ---- prepareSlotContent 1.1 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex);
			if (adapterPosition.mPosition != SLID_POSITION_HEADER &&
                adapterPosition.mPosition != SLID_POSITION_FILLER &&
                adapterPosition.mPosition >= mContentStart && 
                adapterPosition.mPosition < mContentEnd) {
				MediaItem item = mSource.get(adapterPosition.mPosition); // item could be null;
				entry.item = item;
		        entry.mediaType = (item == null)
		                ? MediaItem.MEDIA_TYPE_UNKNOWN
		                : entry.item.getMediaType();
		        entry.path = (item == null) ? null : item.getPath();
		        entry.rotation = (item == null) ? 0 : item.getRotation();
		        entry.contentLoader = new ThumbnailLoader(adapterPosition.mPosition, entry.item);
		        mData[adapterPosition.mPosition % mData.length] = entry;
		        /*if (mData[adapterPosition.mPosition % mData.length] == null) {
					Log.i(TAG, "zll ---- prepareSlotContent 2 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex);
				} else {
					Log.i(TAG, "zll ---- prepareSlotContent 2.222 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex);
				}*/
			} else {
				//Log.i(TAG, "zll ---- prepareSlotContent 0.3 adapterPosition.mPosition:"+adapterPosition.mPosition+",slotIndex:"+slotIndex);
			}
		} else {
			MediaItem item = mSource.get(slotIndex); // item could be null;
	        entry.item = item;
	        entry.mediaType = (item == null)
	                ? MediaItem.MEDIA_TYPE_UNKNOWN
	                : entry.item.getMediaType();
	        entry.path = (item == null) ? null : item.getPath();
	        entry.rotation = (item == null) ? 0 : item.getRotation();
	        entry.contentLoader = new ThumbnailLoader(slotIndex, entry.item);
	        //Aurora <SQF> <2014-05-13>  for NEW_UI begin
	        entry.contentLoader2 = new ClearThumbnailLoader(slotIndex, entry.item);
	        //Aurora <SQF> <2014-05-13>  for NEW_UI end
	        mData[slotIndex % mData.length] = entry;
			
	        /*if (mData[slotIndex % mData.length] == null) {
				//Log.i(TAG, "zll ---- prepareSlotContent 3 slotIndex:"+slotIndex);
			}*/
		}
    }

    private void updateAllImageRequests() {
        mActiveRequestCount = 0;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
    		if (requestSlotImage(i)) ++mActiveRequestCount;
        }
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    private class ThumbnailLoader extends BitmapLoader  {
        private final int mSlotIndex;
        private final MediaItem mItem;

        public ThumbnailLoader(int slotIndex, MediaItem item) {
            mSlotIndex = slotIndex;
            mItem = item;
        }

        @Override
        protected void recycleBitmap(Bitmap bitmap) {
            BitmapPool pool = MediaItem.getMicroThumbPool();
            if (pool != null) pool.recycle(bitmap);
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
	        if(AlbumPage.NEW_UI){
	            return mThreadPool.submit(
						mItem.requestImage(MediaItem.TYPE_CUST01), this);
	        }else{
	            return mThreadPool.submit(
	                    mItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL), this);
			}
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY, this).sendToTarget();
        }

        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            if (bitmap == null) return; // error or recycled
            AlbumEntry entry = mData[mSlotIndex % mData.length];
            //Aurora <SQF> <2014-05-27>  for NEW_UI begin
            if(entry == null) return;
			if( ! entry.item.getPath().toString().equals(mItem.getPath().toString())) return;
			//Aurora <SQF> <2014-05-27>  for NEW_UI end
			if(AlbumPage.NEW_UI){
				entry.bitmap = bitmap;
				bitmap = BitmapUtils.resizeAndCropCenter(bitmap, MediaItem.getTargetSize(MediaItem.TYPE_CUST01), false);
			}
            entry.bitmapTexture = new TiledTexture(bitmap);
            entry.content = entry.bitmapTexture;//Iuni <lory><2014-01-20> :entry.content 即需要加载的缩列图片

            if (isActiveSlot(mSlotIndex)) {
                mTileUploader.addTexture(entry.bitmapTexture);
                //Aurora <SQF> <2014-05-13>  for NEW_UI begin
                startLoadClearThumbnail(mSlotIndex);
                //Aurora <SQF> <2014-05-13>  for NEW_UI end
                --mActiveRequestCount;
                if (mActiveRequestCount == 0) requestNonactiveImages();
                if (mListener != null) mListener.onContentChanged();
            } else {
                mTileUploader.addTexture(entry.bitmapTexture);
            }
        }
    }

    private class ClearThumbnailLoader extends BitmapLoader  {
        private final int mSlotIndex;
        private final MediaItem mItem;

        public ClearThumbnailLoader(int slotIndex, MediaItem item) {
            mSlotIndex = slotIndex;
            mItem = item;
        }

        @Override
        protected void recycleBitmap(Bitmap bitmap) {
            BitmapPool pool = MediaItem.getMicroThumbPool();
            if (pool != null) {
            	pool.recycle(bitmap);
            }
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
        	if(! (mItem instanceof LocalImage)) {
        		//Log.e("SQF_LOG", "AlbumSlidingWindow::ClearThumbnailLoader submitBitmapTask mItem is not a LocalImage \n " + mItem.getClass().getName());
        		return null;
        	}
        	//Log.i("SQF_LOG", "ClearThumbnailLoader::submitBitmapTask ==> slotIndex:" + mSlotIndex);
        	return mThreadPool.submit(mItem.requestClearThumbnail(MediaItem.TYPE_CUST05), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY_2, this).sendToTarget();
        }

        public void updateEntry() {
        	-- mActiveClearThumbnailRequestCount;
        	Bitmap bitmap = getBitmap();
            if (bitmap == null || bitmap.isRecycled()) return; // error or recycled
			if (isActiveSlot(mSlotIndex)) {
				AlbumEntry entry = mData[mSlotIndex % mData.length];
				if(entry == null) return;
				//Aurora <SQF> <2014-05-30>  for NEW_UI begin
				if( ! entry.item.getPath().toString().equals(mItem.getPath().toString())) return;
				//Aurora <SQF> <2014-05-30>  for NEW_UI end
				//Log.i("SQF_LOG" , "ClearThumbnailLoader::updateEntry ---> mSlotIndex:" + mSlotIndex);
				entry.cropCenterBitmapTexture = new TiledTexture(bitmap);
				mTileUploader.addTexture(entry.cropCenterBitmapTexture);
				if (mListener != null) mListener.onContentChanged();
			} else {
				// AlbumEntry entry = mData[mSlotIndex % mData.length];
				// mTileUploader.addTexture(entry.cropCenterBitmapTexture);
			}
        }
    }
    //Aurora <SQF> <2014-05-13>  for NEW_UI end

	@Override
    public void onSizeChanged(int size) {
        if (mSize != size) {
            mSize = size;
            if (mListener != null) mListener.onSizeChanged(mSize);
            if (mContentEnd > mSize) mContentEnd = mSize;
            if (mActiveEnd > mSize) mActiveEnd = mSize;
        }
    }


	
	
	@Override
    public void update(boolean toUpdate) {
    	if(mIsActive) {
			if(toUpdate){
				updateAllImageRequests();
				if (mListener != null) mListener.onContentChanged();
			}
    	}
	}

	@Override
    public void reload() {
		cancelNonactiveImages();
	}

    public void onContentChanged(int index) {
			if (index >= mContentStart && index < mContentEnd && mIsActive) {
	            freeSlotContent(index);
	            prepareSlotContent(index);
				//paul del
				/*
	            updateAllImageRequests();
	            if (mListener != null && isActiveSlot(index)) {
	                mListener.onContentChanged();
	            }
				*/
	        }
    }

    public void resume() {
        mIsActive = true;
        TiledTexture.prepareResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            prepareSlotContent(i);
        }
        updateAllImageRequests();
    }

    public void pause() {
        mIsActive = false;
        mTileUploader.clear();
        TiledTexture.freeResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            freeSlotContent(i);
        }
    }
}
