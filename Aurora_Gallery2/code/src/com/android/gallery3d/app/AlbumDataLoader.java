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

package com.android.gallery3d.app;

import android.R.integer;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
//import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AuroraActionModeHandler;
//Aurora <SQF> <2014-04-28>  for NEW_UI begin
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.MyLog;

import android.content.Context;
//Aurora <SQF> <2014-04-28>  for NEW_UI end
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.fragmentdata.GalleryItem;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class AlbumDataLoader {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumDataAdapter";
    private static final int DATA_CACHE_SIZE = 1000;//1000;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;
	private static final int MSG_NOTIFY_DIRTY = 4;//Aurora <paul> <2014-05-19>
	
	//Aurora <SQF> <2014-05-21>  for NEW_UI begin
	private static final int MSG_HIDE_NO_FILE_VIEW = 5;
	//Aurora <SQF> <2014-05-21>  for NEW_UI end
	
	//Aurora <SQF> <2014-6-16>  for NEW_UI begin
    private Object dateContainerLock = new Object();
	//Aurora <SQF> <2014-6-16>  for NEW_UI end 
	
    private static final int MIN_LOAD_COUNT = 58;
    //MAX_LOAD_COUNT ORIGINALLY:106, SQF MODIFIED TO 90 ON 2014.6.17
    public static final int MAX_LOAD_COUNT = 90;//96;//64;//Iuni <lory><2014-01-21> add begin, 

    private final MediaItem[] mData;
    private final long[] mItemVersion;
    private final long[] mSetVersion;
    
    private List<Integer> mHeaderList = null;

    public static interface DataListener {
    	//public void onAuroraContentChanged(int index, List<Integer> header);
//    	public void onAuroraContentChanged(int index, int type);
        public void onContentChanged(int index);
        public void onSizeChanged(int size);
		public void update(boolean toUpdate);//paul add
		public void reload();//paul add
    }

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private final MediaSet mSource;
    private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;

    private final Handler mMainHandler;
    private int mSize = 0;
    
    private int mAllSize = 0;//Iuni <lory><2014-02-20> add begin
    private int mItemSize = 0;//Iuni <lory><2014-02-20> add begin

    private DataListener mDataListener;
    private MySourceListener mSourceListener = new MySourceListener();
    private LoadingListener mLoadingListener;
	private boolean mStartMode = false;

    private ReloadTask mReloadTask;
    // the data version on which last loading failed
    private long mFailedVersion = MediaObject.INVALID_DATA_VERSION;
    
    private boolean bSourceListener = true;//Iuni <lory><2014-02-22> add begin
    private boolean bStartScanner = false;
    private AuroraActionModeHandler modeHandler = null;
    
    //Aurora <SQF> <2014-04-28>  for NEW_UI begin
    private Context mContext;
    //Aurora <SQF> <2014-04-28>  for NEW_UI end

    public AlbumDataLoader(AbstractGalleryActivity context, MediaSet mediaSet) {
    	//Aurora <SQF> <2014-04-28>  for NEW_UI begin
    	mContext = context.getAndroidContext();
    	//Aurora <SQF> <2014-04-28>  for NEW_UI end
        mSource = mediaSet;

        mData = new MediaItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);
        
        bSourceListener = true;
        bStartScanner = false;

        mMainHandler = new SynchronizedHandler(context.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_RUN_OBJECT:
                        ((Runnable) message.obj).run();
                        return;
					//Aurora <paul> <2014-05-19> start
					case MSG_NOTIFY_DIRTY:
						if(null != mReloadTask){
							mReloadTask.notifyDirty();
						}
						return;
					//Aurora <paul> <2014-05-19> end
                    case MSG_LOAD_START:
                        if (mLoadingListener != null) mLoadingListener.onLoadingStarted();
                        return;
                    case MSG_LOAD_FINISH:
                        if (mLoadingListener != null) {
                            boolean loadingFailed =
                                    (mFailedVersion != MediaObject.INVALID_DATA_VERSION);
                            mLoadingListener.onLoadingFinished(loadingFailed);
                        }
                        return;
                    case MSG_HIDE_NO_FILE_VIEW:
                    	if(null != mLoadingListener && (mLoadingListener instanceof AlbumPage.MyLoadingListener)) {
                    		((AlbumPage.MyLoadingListener)mLoadingListener).hideNoFileView();
                    	}
                    	return;
                }
            }
        };
    }
    
    //Iuni <lory><2014-01-21> add begin
    public void setActionModeHandler(AuroraActionModeHandler mActionModeHandler) {
    	if (mActionModeHandler != null) {
    		modeHandler = mActionModeHandler;
		}
		return;
	}
    
    //Iuni <lory><2014-01-21> add begin
    public ArrayList<MediaItem> getMediaSetListAlbumDataLoader() {
		if (mSource != null) {
			return mSource.getMediaItem(0, mItemSize);
		}
    	return null;
	}
    
    public void setFlagToNotUpdateViews(boolean flag) {
    	if (bSourceListener != flag) {
    		bSourceListener = flag;
		}
    	
		return;
	}

    public boolean getStartScanner() {
		return bStartScanner;
	}
    
    public void resume() {
        mSource.addContentListener(mSourceListener);
        bStartScanner = false;
        //bSourceListener = true;
        if (modeHandler != null) {
        	bSourceListener = modeHandler.getMenuDialogActive()?false:true;
		} else {
			bSourceListener = true;
		}
        

        mReloadTask = new ReloadTask();
        mReloadTask.start();
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	//Aurora <SQF> <2014-6-17>  for NEW_UI begin

            //ORIGINALLY:
        	//mDateTask = new DateThreadRun();
        	//mDateTask.start();

            //SQF MODIFIED TO:
        	//if(null == mDateTask) {
        		mDateTask = new DateThreadRun();
        		mDateTask.start();
        	//}
        	mDateTask.onNotify();
            //Aurora <SQF> <2014-6-17>  for NEW_UI end
		}
    }

    public void pause() {
        mReloadTask.terminate();
        mReloadTask = null;
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	if(mDateTask != null) {
        		mDateTask.onTerminate();
        		//mDateTask = null;//SQF ANNOTATED ON 2014-6-17
        		mDateTask.interrupt();//SQF ADDED ON 2014-6-17
        	}
		}
        mSource.removeContentListener(mSourceListener);
    }

    public MediaItem get(int index) {
    	if (!isActive(index)) {
			//Aurora <paul> <2014-04-24> start
            //throw new IllegalArgumentException(String.format(
            //        "%s not in (%s, %s)", index, mActiveStart, mActiveEnd));
			//Log.e("SQF_LOG",String.format("%s not in (%s, %s)", index, mActiveStart, mActiveEnd));
			return null;
			//Aurora <paul> <2014-04-24> end

        }
    	
        return mData[index % mData.length];
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public boolean isActive(int index) {
    	//lory modify
    	if (!mStartMode) {
    		return index >= mActiveStart && index < mActiveEnd;
		}
    	else {
    		return index >= mActiveStart && index < mSize;
		}
    }

    public int size() {
    	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
    		return mItemSize;
		} else {
			return mSize;
		}
    }
    
    public int getAllSize() {
		return mSource.getMediaItemCount();
	}

    // Returns the index of the MediaItem with the given path or
    // -1 if the path is not cached
    public int findItem(Path id) {
        for (int i = mContentStart; i < mContentEnd; i++) {
            MediaItem item = mData[i % DATA_CACHE_SIZE];
            if (item != null && id == item.getPath()) {
                return i;
            }
        }
        return -1;
    }

    private void clearSlot(int slotIndex) {
    	if (false) {//MySelfBuildConfig.USEGALLERY3D_FLAG
    		Position adapterPosition = translatePosition(slotIndex);
    		if (adapterPosition.mPosition == POSITION_HEADER) {
    			//do nothing
			} else if (adapterPosition.mPosition == POSITION_FILLER) {
				//do nothing
			} else {
				mData[adapterPosition.mPosition] = null;
		        mItemVersion[adapterPosition.mPosition] = MediaObject.INVALID_DATA_VERSION;
		        mSetVersion[adapterPosition.mPosition] = MediaObject.INVALID_DATA_VERSION;
			}
    		
		} else {
			mData[slotIndex] = null;
	        mItemVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
	        mSetVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
		}
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd) return;
        int end = mContentEnd;
        int start = mContentStart;

        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
        }
        long[] itemVersion = mItemVersion;
        long[] setVersion = mSetVersion;
        
        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        }
        //Log.i("SQF_LOG", "AlbumDataLoader::setContentWindow  mContentStart:" + mContentStart + " mContentEnd:" + mContentEnd);
        if (mReloadTask != null) mReloadTask.notifyDirty();
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd) return;
        
        Utils.assertTrue(start <= end
                && end - start <= mData.length && end <= mSize);

        int length = mData.length;
        mActiveStart = start;
        mActiveEnd = end;

        if (start == end) return;

        int contentStart = Utils.clamp((start + end) / 2 - length / 2,
                0, Math.max(0, mSize - length));
        int contentEnd = Math.min(contentStart + length, mSize);

        if (mContentStart > start || mContentEnd < end
                || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private class MySourceListener implements ContentListener {
        @Override
        public void onContentDirty() {
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		if (mReloadTask != null && bSourceListener){
					//Aurora <paul> <2014-05-19> start
					if(!mMainHandler.hasMessages(MSG_NOTIFY_DIRTY)){
						mMainHandler.sendMessageDelayed(mMainHandler.obtainMessage(MSG_NOTIFY_DIRTY),100);
					}
					//	mReloadTask.notifyDirty();
					//Aurora <paul> <2014-05-19> end
        		} 
			} else {
				if (mReloadTask != null) mReloadTask.notifyDirty();
			}
        }
    }
    
    public void auroraUpdateDatabase() {
    	if (mReloadTask != null && bSourceListener) mReloadTask.notifyDirty();
	}

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

	//Iuni <lory><2013-12-19> add begin
    //Aurora <SQF> <2014-6-11>  for NEW_UI begin
    //SQF ANNOTATED ON 2014.6.11
    /*
	public void setStartMode(boolean bMode) {
        mStartMode = bMode;
    }
    */
	//Aurora <SQF> <2014-6-11>  for NEW_UI end

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(
                mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<Integer> getHeaderList() {
    	synchronized (this) {
    		return mHeaderList;
		}
	}

    private static class UpdateInfo {
        public long version;
        public int reloadStart;
        public int reloadCount;

        public int size;
        public ArrayList<MediaItem> items;
        
        public int headCount;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        @Override
        public UpdateInfo call() throws Exception {
            if (mFailedVersion == mVersion) {
                // previous loading failed, return null to pause loading
                Log.w(TAG,"previous loading failed : " + mFailedVersion);
                //return null; paul del for BUG #16129
            }
            
            UpdateInfo info = new UpdateInfo();
            long version = mVersion;
            info.version = mSourceVersion;
            info.size = mSize;
            long setVersion[] = mSetVersion;
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                int index = i % DATA_CACHE_SIZE;
                if (setVersion[index] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
                    return info;
                }
            }
			
            return mSourceVersion == mVersion ? null : info;
        }
    }

    private class UpdateContent implements Callable<Void> {

        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }
        
        @Override
        public Void call() throws Exception {
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;
        	if (mSize != info.size) {
                mSize = info.size;
                initDataBaseList();
                if (mDataListener != null) mDataListener.onSizeChanged(mSize);
                if (mContentEnd > mSize) mContentEnd = mSize;
                if (mActiveEnd > mSize) mActiveEnd = mSize;
            }

            ArrayList<MediaItem> items = info.items;
            mFailedVersion = MediaObject.INVALID_DATA_VERSION;
            if ((items == null) || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    mFailedVersion = info.version;
                    Log.d(TAG, "loading failed: " + mFailedVersion);
                }
                return null;
            }
            mDataListener.reload();//paul add
            boolean toUpdate = false;//paul add
			
            int start = Math.max(info.reloadStart, mContentStart);
            int end = Math.min(info.reloadStart + items.size(), mContentEnd);
            for (int i = start; i < end; ++i) {
                int index = i % DATA_CACHE_SIZE;
                mSetVersion[index] = info.version;
                MediaItem updateItem = items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (mItemVersion[index] != itemVersion) {
                    mItemVersion[index] = itemVersion;
                    mData[index] = updateItem;
                    if (mDataListener != null && i >= mActiveStart && i < mActiveEnd) {
                        mDataListener.onContentChanged(i);
						toUpdate = true;//paul add
                    }
                }
            }
            
			mDataListener.update(toUpdate);//paul add
            return null;
        }
    }

    /*
     * The thread model of ReloadTask
     *      *
     * [Reload Task]       [Main Thread]
     *       |                   |
     * getUpdateInfo() -->       |           (synchronous call)
     *     (wait) <----    getUpdateInfo()
     *       |                   |
     *   Load Data               |
     *       |                   |
     * updateContent() -->       |           (synchronous call)
     *     (wait)          updateContent()
     *       |                   |
     *       |                   |
     */
    private class ReloadTask extends Thread {

        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading) return;
            mIsLoading = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && updateComplete) {
                        updateLoading(false);
                        Utils.waitWithoutInterrupt(this);
                        continue;
                    }
                    mDirty = false;
                }
                
                if (!bSourceListener) {
					continue;
				}
                updateLoading(true);
                long version = mSource.reload();
                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
                if (updateComplete) continue;
                if (info.version != version) {
                    info.size = mSource.getMediaItemCount();
                    info.version = version;
                    mItemSize = info.size;
                    //Aurora <SQF> <2014-05-21>  for NEW_UI begin
                    if(mItemSize > 0) {
                        mMainHandler.sendEmptyMessage(MSG_HIDE_NO_FILE_VIEW);
                    }
                    //Aurora <SQF> <2014-05-21>  for NEW_UI end
                    bStartScanner = true;
                }
                if (info.reloadCount > 0) {
                    info.items = mSource.getMediaItem(info.reloadStart, info.reloadCount);
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }
    }
    
    private int addNumHeader(int start, int count) {
		int  header = 0;
		int  last = start;
		int  total = start+count;
		
		int lastindex = 0;
		
		if (last == 0) {
			lastindex = -1;
		} else {
			lastindex = getHeaderBySlotIndex(last);
		}
		header = getHeaderBySlotIndex(total) - lastindex;
		return header;
	}
    
    //Iuni <lory><2014-01-21> add begin
	private ArrayList<Container> mContainers;
    private ArrayList<MediaItem> mList;
    private int m_Clomus = 1;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy'.'MM'.'dd");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy'.'MM");
    
    private final Uri mImgBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
    private final Uri mVideoBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
    private String mImgWhereClause;
    private String mVideoWhereClause;
    private ContentResolver mResolver = null;
    private ArrayList<AuroraItemInfos> mAuroraList = null;
    
    private ArrayList<Container> mDateContainers;
    private ArrayList<Container> mDayContainers;
    private DateThreadRun mDateTask;
    private Bundle tBundle = null;
    private boolean tGetContent = false;
    
    
    
    static final String[] AURORA_PROJECTION_IMG =  {
        ImageColumns._ID,           // 0
        ImageColumns.DATE_TAKEN
	};
    
    static final String[] AURORA_PROJECTION_VIDEO = {
        VideoColumns._ID,
        VideoColumns.DATE_TAKEN,
	};
    
    private class Container {
        String title;
        //ArrayList<MediaItem> items;
        ArrayList<AuroraItemInfos> items;

        public Container(String title) {
            this.title = title;
            //this.items = new ArrayList<MediaItem>();
            this.items = new ArrayList<AuroraItemInfos>();
        }
    }
    
    private class AuroraItemInfos {
    	int 	id;
    	long	time = 0;
    	
    	
    	public AuroraItemInfos(int id, long time) {
			this.id = id;
			this.time = time;
		}
    	
    	
    	//Aurora <SQF> <2014-6-16>  for NEW_UI begin
		//SQF ANNOTATED ON 2014.6.16
    	/*
    	int     day_index = 0;
    	int		month_index = 0;
    	

    	
    	public void setValue(int day, int month) {
    		if (day >= 0) {
    			this.day_index = day;
			}
    		
    		if (month >= 0) {
    			this.month_index = month;
			}
		}

    	//Aurora <SQF> <2014-05-21>  for ArrayList.contains in DateThreadRun.run begin
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			if (obj instanceof AuroraItemInfos) {   
				AuroraItemInfos info = (AuroraItemInfos) obj;   
				return this.id == info.id  && this.time == info.time;  
			}   
			return super.equals(obj);
		}
    	
		//Aurora <SQF> <2014-05-21>  for ArrayList.contains in DateTask.run end

    	//Aurora <SQF> <2014-6-16>  for NEW_UI end
		 */
    	
    } 
    
    public void setContentResolver(ContentResolver resolver, String imgWhereClause, String videoWhereClause) {
    	mResolver = resolver;
    	mImgWhereClause = imgWhereClause;
    	mVideoWhereClause = videoWhereClause;
		return;
	}
    
    public void setBundleData(Bundle bundle, boolean getContent) {
    	tBundle = bundle;
    	tGetContent = getContent;
		return;
	}
    
    private void initDataBaseList() {
    	//mAuroraList = null;//SQF ANNOTAED ON 2014.6.20
    	
    	synchronized (this) {
    		mAuroraList = null;//SQF ADDED ON 2014.6.20
    		if (tGetContent && tBundle != null) {
    			int typeBits = tBundle.getInt(Gallery.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
    			if ((typeBits & DataManager.INCLUDE_IMAGE) != 0) {
    				getImgThumbs();
    			}
    			
    			if ((typeBits & DataManager.INCLUDE_VIDEO) != 0) {
    				getVideoThumbs();
    			}
			} else {
				getImgThumbs();
	        	getVideoThumbs();
			}
        	initDataBaseGroup();
        	//mDateContainers = null;//SQF ANNOTATED ON 2014.6.17
        	mContainers = null;
        	mDayContainers = null;
        	mDayContainers = new ArrayList<Container>();
    		//mDateContainers = new ArrayList<Container>();//SQF ANNOTATED ON 2014.6.17
    		
    		IniAuroraContainers(mAuroraList);
        	if (getCloums() == SlotView.CLOUMNS_NUM6 && GalleryUtils.isPortrait(mContext)) {

        		//Aurora <SQF> <2014-6-16>  for NEW_UI begin
        	    //ORIGINALLY:
				//IniAuroraMonthContainers(mAuroraList);
        		
        	    //SQF MODIFIED TO:
				initAuroraMonthContainers();
        	    //Aurora <SQF> <2014-6-16>  for NEW_UI end
        		mContainers = mDateContainers;
				return;
			} else {
				//MyLog.i("SQF_LOG", "initDataBaseList---2222222222222222222222222222222222222222222");
				mContainers = mDayContainers;
				if (mDateTask != null) {
	        		mDateTask.onNotify();
				}
			}
        	
		}
    	
		return;
	}
    
    public void setSortMode(int num) {
    	synchronized (this) {
    		if (num == SlotView.CLOUMNS_NUM6 && GalleryUtils.isPortrait(mContext) && mDateContainers != null) {
        		mContainers = mDateContainers;
    		} else {
    			if (mDayContainers != null) {
    				mContainers = mDayContainers;
    			}
    		}
		}
		return;
	}
    
    private class DateThreadRun extends Thread{
    	private volatile boolean mDateActive = true;
    	private volatile boolean mDateDirty = true;

		@Override
		public void run() {
			while (mDateActive && ! isInterrupted() ) {//SQF add " ! isInterrupted() " ON 2014.6.17  
				synchronized (this) {
					if (!mDateDirty) {
						//Log.i("SQF_LOG", "DateThreadRun::wait---> ................................");
						Utils.waitWithoutInterrupt(this);
						//Log.i("SQF_LOG", "DateThreadRun::continue---> ................................");
						continue;
					}
				}					
				//Aurora <SQF> <2014-6-16>  for NEW_UI begin
				//ORIGINALLY:
				/*
				synchronized (dateContainerLock) {
					if (mAuroraList != null && mDateContainers != null) {
						mDateContainers.clear();
						//int pos = -1;
						for (AuroraItemInfos item : mAuroraList) {
				            if (mDateContainers.size() == 0) {
				            	String time1 = sdf2.format(new Date(item.time));
				            	mDateContainers.add(new Container(time1));
				            	//pos++;
				            }
				            Container container = mDateContainers.get(mDateContainers.size() - 1);
				            String time2 = sdf2.format(new Date(item.time));
				            if (container.title.equals(time2)) {
				            } else {
				            	mDateContainers.add(new Container(time2));
				                container = mDateContainers.get(mDateContainers.size() - 1);
				                //pos++;
				            }
				            //pos++;
				            //item.setValue(-1, pos);
				            //Aurora <SQF> <2014-05-21>  for NEW_UI begin
				            if( ! container.items.contains(item)) {
				            //Aurora <SQF> <2014-05-21>  for NEW_UI end
				            	Log.i("SQF_LOG", "DateThreadRun::run---> ................add item:" + item.id);
				            	container.items.add(item);
				            }
				        }
						mDateDirty = false;
					} else {
						mDateDirty = false;
					}
					
				}
				*/
				//SQF MODIFIED TO:
				initAuroraMonthContainers();
				mDateDirty = false;
				//Aurora <SQF> <2014-6-16>  for NEW_UI end
			}
		}
		
		public synchronized void onNotify(){
			mDateDirty = true;
			notifyAll();
		}
		
		public synchronized void onTerminate(){
			//Aurora <SQF> <2014-6-17>  for NEW_UI begin
		    //ORIGINALLY:
			//mDateActive = false;
		    //SQF MODIFIED TO:
			mDateDirty = false;
			mDateActive = false;
		    //Aurora <SQF> <2014-6-17>  for NEW_UI end
			notifyAll();
		}
		
    } 
    
    public int getImgThumbs(){
    	int img_num = 0;
    	Cursor imgCursor = null;
    	String mOrderClause = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";
    	
    	if (mResolver == null) {
			return 0;
		}
    	
    	if (mAuroraList == null) {
    		mAuroraList = new ArrayList<AuroraItemInfos>();
		}
    	
		try {
			imgCursor = mResolver.query(mImgBaseUri, AURORA_PROJECTION_IMG, MediaSetUtils.getImageQueryStr(mContext), null, mOrderClause);
			if (imgCursor != null) {
				img_num += imgCursor.getCount();
				if (imgCursor.moveToFirst()) {
					do {
						int idIndex = imgCursor.getInt(0); //imgCursor.getColumnIndexOrThrow("_id");
						long time = imgCursor.getLong(1);  //getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
						AuroraItemInfos infos = new AuroraItemInfos(idIndex, time);
						mAuroraList.add(infos);
					} while (imgCursor.moveToNext());
				}
			}

		} catch (Exception e) {
			Log.e("SQF_LOG", "zll --- getImgThumbs error");
		} finally {
			if (imgCursor != null) {
				imgCursor.close();
			}
		}
    	
    	return img_num;
    }
    
    public int getVideoThumbs(){
    	int vedio_num = 0;
    	Cursor videoCursor = null;
    	String mOrderClause = VideoColumns.DATE_TAKEN + " DESC, " + VideoColumns._ID + " DESC";
    	
    	if (mResolver == null) {
			return 0;
		}
    	
    	if (mAuroraList == null) {
    		mAuroraList = new ArrayList<AuroraItemInfos>();
		}
    	
		try {
			videoCursor = mResolver.query(mVideoBaseUri, AURORA_PROJECTION_VIDEO, MediaSetUtils.getVideoQueryStr(mContext), null, mOrderClause);
			if (videoCursor != null) {
				vedio_num += videoCursor.getCount();
				if (videoCursor.moveToFirst()) {
					do {
						int idIndex = videoCursor.getInt(0);//videoCursor.getColumnIndexOrThrow("_id");
						long time = videoCursor.getLong(1); //videoCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
						
						AuroraItemInfos infos = new AuroraItemInfos(idIndex, time);
						mAuroraList.add(infos);
					} while (videoCursor.moveToNext());
				}
			}
		} catch (Exception e) {
			Log.e("SQF_LOG", "zll --- getVideoThumbs error");
		} finally {
			if (videoCursor != null) {
				videoCursor.close();
			}
		}
    	
    	return vedio_num;
    }
    
    public void initDataBaseGroup() {
    	if (mAuroraList == null) {
			return;
		}
    	
    	Collections.sort(mAuroraList, new Comparator<AuroraItemInfos>() {
			@Override
			public int compare(AuroraItemInfos lhs, AuroraItemInfos rhs) {
				 int tmp = (lhs.time < rhs.time ? -1 : lhs.time == rhs.time ? 0 : 1);
				 return -tmp;
			}
		});

		return;
	}
    
    private void initAuroraMonthContainers() {
    	if (mAuroraList == null) {
			return;
		}
    	synchronized (dateContainerLock) {
    		
    		if(null == mDateContainers) {
    			mDateContainers = new ArrayList<Container>();
    		} else {
    			mDateContainers.clear();
    		}
    		
			for (AuroraItemInfos item : mAuroraList) {
	            if (mDateContainers.size() == 0) {
	            	String time1 = sdf2.format(new Date(item.time));
	            	mDateContainers.add(new Container(time1));
	            }
	            Container container = mDateContainers.get(mDateContainers.size() - 1);
	            String time2 = sdf2.format(new Date(item.time));
	
	            if (container.title.equals(time2)) {
	            } else {
	            	mDateContainers.add(new Container(time2));
	                container = mDateContainers.get(mDateContainers.size() - 1);
	            }
	            container.items.add(item);
	        }
    	}
    }
    
    private void IniAuroraContainers(ArrayList<AuroraItemInfos> items) {
    	//Aurora <SQF> <2014-05-28>  for NEW_UI begin
    	//mSize = 0;
    	//Aurora <SQF> <2014-05-28>  for NEW_UI end
    	
    	if (items == null) {
			return;
		}
    	
    	int pos = -1;
        for (AuroraItemInfos item : items) {
            if (mDayContainers.size() == 0) {
            	String time1 = sdf.format(new Date(item.time));
            	mDayContainers.add(new Container(time1));
            	pos++;
            }
            
            Container container = mDayContainers.get(mDayContainers.size() - 1);
            String time2 = sdf.format(new Date(item.time));
            if (container.title.equals(time2)) {
            } else {
            	mDayContainers.add(new Container(time2));
                container = mDayContainers.get(mDayContainers.size() - 1);
                pos++;
            }
            pos++;
            //item.setValue(pos, -1); //SQF ANNOTATED ON 2014.6.16
            container.items.add(item);
            
            //Aurora <SQF> <2014-05-28>  for NEW_UI begin
            //++ mSize;
            //Aurora <SQF> <2014-05-28>  for NEW_UI end
        }
    }

    public int getSixSlotCount(int num) {
		int total = 0;
		if (num == SlotView.CLOUMNS_NUM6 && GalleryUtils.isPortrait(mContext)) {
			if (mDateContainers == null) {
				return 0;
			}
			total = mItemSize + mDateContainers.size();
		} else {
			if (mDayContainers == null) {
				return 0;
			}
			total = mItemSize + mDayContainers.size();
		}
		return total;
	}
    
    
    //Aurora <SQF> <2014-6-16>  for NEW_UI begin
    //SQF ANNOTATED ON 2014.6.16
    /*
    public int getDayIndexByMonthIndex(int index) {
		int month = 0;
		
		if (index <= 0) {
			return 0;
		}
		
		int total = 0;
		int header = 0;
		
		if (mAuroraList != null && mAuroraList.size() > 0) {
			AuroraItemInfos infos;
			for (int i = 0; i < mAuroraList.size(); i++) {
				infos = mAuroraList.get(i);
				if (infos != null && infos.month_index == index) {
					return infos.day_index;
				} 
				
				if (infos.month_index > index+mDayContainers.size()) {
					break;
				}
			}
		}
		return -1;
	}
	*/
    //Aurora <SQF> <2014-6-16>  for NEW_UI end
    
    public String getMyDateString(int position) {
    	synchronized (this) {
    		if (mContainers != null) {
        		int index = getDaterIndex(position);
        		if(index == -1) return "";
        		int total = mContainers.size();
        		if (total >0) {
        			if (index >= total) {
        				index = mContainers.size()-1;
    				}
            		return mContainers.get(index).title;
    			}
    		}
		}
    	
		return "";
	}
    
    public int getHeaderNumBySlotIndex(int index) {
		int header = 0;
		int total = 0;
		
		if (mContainers == null) {
			return 0;
		}
		
		if (index == 0) {
			return 0;
		}
		
		synchronized (this) {
			for (Container c : mContainers) {
				total += getAlbumDLCountForHeader(header);
				total++;
				//Aurora <SQF> <2014-05-23>  for NEW_UI begin
				//SQF ANNOTATED ON 2014.5.23
//				if (index == total) {
//					return header+1;
//				}
				//Aurora <SQF> <2014-05-23>  for NEW_UI end
				if (index < total) {
					return header;
				}
				header++;
			}
			//Aurora <SQF> <2014-05-23>  for NEW_UI begin
			if(index >= total) {
				return header - 1;
			}
			//Aurora <SQF> <2014-05-23>  for NEW_UI end
		}
		return header;
	}
    
    private int getHeaderBySlotIndex(int index) {
		int header = 0;
		int total = 0;
		int last = 0;
		
		if (mContainers == null) {
			return 0;
		}
		
		if (index == 0) {
			return 0;
		}
		
		synchronized (this) {
			for (Container c : mContainers) {
				total += getAlbumDLCountForHeader(header);
				total++;
				if (index <= total && index > last) {
					return header;
				}
				
				last = total;
				header++;
			}
		}
		
		return header;
	}
    
    public int getDaterIndex(int position) {
		int index = 0;
		int header = 0;
		int total = 0;
		
		if (position <= 0 || mContainers == null) {
			return 0;
		}
		
		synchronized (this) {
			for (Container c : mContainers) {
				total += mContainers.get(header).items.size();
				total += 1;
				if (position == total) {
					
					//Aurora <SQF> <2014-05-23>  for NEW_UI begin
					//ORIGINALLY:
					//return (header+1);
					//SQF MODIFIED TO:
					if(mContainers.size() >= header + 2) {
						return (header+1);
					} else {
						return -1;//NOT FOUND
					}
					//Aurora <SQF> <2014-05-23>  for NEW_UI end
				}
				
				if (position < total) {
					break;
				}
				header++;
			}
		}
		return 0;
	}
    
    public int getCloums() {
		return m_Clomus;
	}
    
    public void setCloums(int colums) {
    	m_Clomus = colums;
		return;
	}
    
    public Position translatePosition(int position) {
    	synchronized (this) {
    		int numHeaders = getNumHeaders();
            if (numHeaders == 0) {
                if (position >= mSize) {
                    return new Position(POSITION_FILLER, 0);
                }
                return new Position(position, 0);
            }

            int adapterPosition = position;
            int place = position;
            int i;

            for (i = 0; i < numHeaders; i++) {
                int sectionCount = getCountForHeader(i);
                if (place == 0) {
                    return new Position(POSITION_HEADER, i);
                }
                place -= 1;
                adapterPosition -= 1;

                if (place < sectionCount) {
                    return new Position(adapterPosition, i);
                }

                place -= sectionCount;
            }

            return new Position(POSITION_FILLER, i);
		}
    }
    
    protected static final int POSITION_FILLER = -0x01;
    protected static final int POSITION_HEADER = -0x02;
    protected static final int POSITION_VIEW = 0x01;
    
    public class Position {
    	public int mHeader;
    	public int mPosition;
    	
    	public Position(int position, int header) {
            mPosition = position;
            mHeader = header;
        }
    }
    
    private int getNumHeaders() {
		return getAlbumDLNumHeaders();
	}
    
    private int getCountForHeader(int header) {
		return getAlbumDLCountForHeader(header);
	}
    
    private int unFilledSpacesInHeaderGroup(int header) {
        int remainder = getCountForHeader(header) % m_Clomus;
        return remainder == 0 ? 0 : m_Clomus - remainder;
    }
    
    public boolean isAlbumDLSectionHeader(int position) {

		int totalRowSize = 0;
		if (position == 0)
			return true;
		
		synchronized (this) {
			for (Container c : mContainers) {
				if (totalRowSize == position) {
					return true;
				}
				
				int rowSize = getAlbumDLRowSize(c);
				totalRowSize += rowSize;
			}
		}
		
		return false;
	}
    
    public int getAlbumDLNumHeaders() {
		int num = 0;
		
		if (mContainers != null) {
			num = mContainers.size();
			return num;
		}
		
		return num;
	}
    
    public int getAlbumDLCountForHeader(int header){
    	int num = 0;
    	
    	synchronized (this) {
    		if (mContainers != null && header < mContainers.size()) {
        		num = mContainers.get(header).items.size();
    			return num;
    		}
		}
    	
    	return num;
    }
    
    /*
     * getAlbumDLRowSize
     * @param c 存放图片信息的容器
     * @return 当前Container在m_Clomus列的情况下有多少行, 包含日期空白行,此空白行的高度为mHeaderHeight.
     */
    public int getAlbumDLRowSize(Container c) {
        return ((c.items.size() + m_Clomus - 1) / m_Clomus) + 1;
    }
    
    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
    public int getAlbumDLRowSize(Container c, int columnNum) {
    	return ((c.items.size() + columnNum - 1) / columnNum) + 1;
    }
    //Aurora <SQF> <2014-08-13>  for NEW_UI end
    
    public int getHeaderNumByIndex(int index) {
		int headernum = 0;
		int total = 0;
		int last = 0;
		
		if (mContainers == null) {
			return 0;
		}
		
		synchronized (this) {
			for (Container c : mContainers) {
				total += getAlbumDLCountForHeader(headernum);
				if (index <= total && index > last) {
					return (headernum+1);
				}
				last = total;
				headernum++;
			}
		}
		
		return 0;
	}

    /* 获取当前header在当前列数的情况下,共有多少行
     * @param header
     * @return 本header的图片行数，包括一个日期的空白行，所以如果要知道真实的行数，需要减去1
     */
    public int getAlbumDLCurRowsByHeader(int header) {
    	int rows = 0;
    	int cur_header = 0;
    	if (mContainers == null) return 0;
    	synchronized (this) {
    		for (Container c : mContainers) {
    			if (cur_header == header) {
    				return getAlbumDLRowSize(c);
    			}
    			
    			cur_header++;
    		}
		}
    	return rows;
	}
    
    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
    /* 获取当前header在当前列数的情况下,共有多少行
     * @param header
     * @return 本header的图片行数，包括一个日期的空白行，所以如果要知道真实的行数，需要减去1
     */
    public int getAlbumDLCurRowsByHeader(int header, int columnNum) {
    	int rows = 0;
    	int cur_header = 0;
    	if (mContainers == null) return 0;
    	synchronized (this) {
    		for (Container c : mContainers) {
    			if (cur_header == header) {
    				return getAlbumDLRowSize(c, columnNum);
    			}
    			
    			cur_header++;
    		}
		}
    	return rows;
	}
    //Aurora <SQF> <2014-08-13>  for NEW_UI end
    
    public int remainderAlbumDLInLastRowGroup(int header) {
		int remainder = 0;
    	if (mContainers != null && mContainers.size() > 0) {
    		remainder = (mContainers.get(header).items.size())%m_Clomus;
		}
    	return remainder == 0 ? 0 : m_Clomus - remainder; 
    }
    
    public int getRowsByCurHeader(int header){
    	int rows = 0;
    	
    	if (header == 0 && mContainers == null) {
			return 1;
		}
    	
    	synchronized (this) {
    		for (Container c : mContainers) {
        		rows += getAlbumDLRowSize(c);
    			if (header < rows) {
    				return rows;
    			}
    		}
		}
    	
    	return rows;
    }
    
    public String getMyHeaderTitle(int position){
    	int totalRowSize = 0;
    	
    	if (mContainers == null) {
			return "";
		}
    	
    	synchronized (this) {
    		for (Container c : mContainers){
        		totalRowSize += getAlbumDLRowSize(c);
        		if (totalRowSize >= position) {
                    return c.title;
                }
        	}
		}
    	
    	return "";
    }
    
    public int getAlbumDLCurToatalRowsByHeader(int header) {
		int rows = 0;
		
		if (header == 0) {
			return 0;
		}
		
		synchronized (this) {
			int cur_header = 0;
			int cur_rows = 0;
			
			for (Container c : mContainers) {
				if (cur_header == header) {
					return cur_rows;
				}
				
				cur_rows += getAlbumDLRowSize(c);
				cur_header++;
			}
		}
		
		return rows;
	}
    
    public int getAlbumDLAllItemsByRows(int rows) {
		int num = 0;
		int index = 0;
		
		if (rows < 0) {
			return num;
		}
		
		synchronized (this) {
			for (Container c : mContainers) {
				num += getAlbumDLCountForHeader(index);
				if (rows == index) {
					return num;
				}
				
				index++;
			}
		}
		
		return num;
	}
    
    //AlbumSR  == AlbumSlotRenderer
    public int getAlbumDLTotalRowSize(int colums) {
    	int count = 0;
    	
    	if (mContainers == null) {
			return 0;
		}
    	
    	synchronized (this) {
    		for (Container c : mContainers) {
                count += getAlbumDLRowSize(c);
            }
		}
        return count;
	}
    
    public int getIndexByRows(int rows, int header) {
		int index = 0;
		int total = 0;
		int position = 0;
		
		synchronized (this) {
			for (Container c : mContainers) {
				if (header == index) {
					position = total + (rows-1)*m_Clomus+1;
					return position;
				}
				total += getAlbumDLCountForHeader(index);
				index++;
			}
		}
		
		return 0;
	}
    
    public int getItemIndexInViews(int header, int headrows, int x, int y, int width) {
		int index = 0;
		int total = 0;
		int last = 0;
		int totalrows = 0;
		
		int remainder = 0;//(mContainers.get(header).items.size())%m_Clomus;
		int nextheader = 0;
		
		synchronized (this) {
			for (Container c : mContainers) {
				if (index == header) {
					int rows = totalrows + headrows + 1;
					int clos = x / width;
				}
				
				total += mContainers.get(header).items.size();
				totalrows += getAlbumDLRowSize(c);
				index++;
			}
		}
		
		return index;
	}

    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
    //ORIGINALLY:

    
    /*
    public ItemPositionInfo getRowsAndCloumns(int position, int num) {
    	int rows = 0;
    	int lasTotalRowSize = 0;
    	int curTotalNum = 0;
    	int header = 0;
    	int lasTtotalNum = 0;
    	int t_num = 0;
    	int totalRowSize = 0;
    	
    	synchronized (this) {
    		try {
    			if (mContainers == null) {
        			return null;
        		}
            	
            	if (num < 0) {
            		t_num = m_Clomus;
        		} else {
        			t_num = num;
        		}
            	
            	ItemPositionInfo infos = new ItemPositionInfo();
            	if (position == 0) {
            		infos.type = 0;
            		infos.rows = 0;
            		infos.cloumns = 0;
            		infos.header_index = 0;
        			return infos;
        		}
            	
            	//curTotalNum++;
            	lasTtotalNum = curTotalNum;
            	for (Container c : mContainers) {
            		
            		curTotalNum += mContainers.get(header).items.size();
            		totalRowSize += getAlbumDLRowSize(c);
            		//view is header
            		if (position == curTotalNum+header+1) {
            			infos.type = 0;
                		infos.rows = totalRowSize;
                		infos.cloumns = 0;
                		infos.header_index = header+1;
        				return infos;
        			}
            		
            		if (position < curTotalNum+header+1) {
            			int t_rows = 0;
            			int tmp = position-lasTtotalNum -(header+1);
            			int tmp_col = (tmp+1) % t_num;
            			if (tmp_col == 0) {
            				t_rows = (tmp+1)/t_num;
            				tmp_col = tmp%t_num+1;
        				} else {
        					t_rows = tmp/t_num+1;
        				}
            			
            			rows = lasTotalRowSize+t_rows;

            			infos.type = 1;
            			infos.rows = rows;
                		infos.cloumns = tmp_col-1;
                		infos.header_index = header;
                		return infos;
        			}
            		
            		header++;
            		lasTtotalNum = curTotalNum;
            		lasTotalRowSize = totalRowSize;
        		}
			} catch (Exception e) {
				Log.e("SQF_LOG", "zll ---- getRowsAndCloumns fail -----");
			}
		}
    	
		return null;
	}
	*/
    //SQF MODIFIED TO:
    public ItemPositionInfo getRowsAndCloumns(int position, int num) {
    	int rows = 0;
    	int lasTotalRowSize = 0;
    	int curTotalNum = 0;
    	int header = 0;
    	int lasTtotalNum = 0;
    	int t_num = 0;
    	int totalRowSize = 0;
    	
    	synchronized (this) {
    		try {
    			if (mContainers == null) {
        			return null;
        		}
            	
            	if (num < 0) {
            		t_num = m_Clomus;
        		} else {
        			t_num = num;
        		}
            	
            	ItemPositionInfo infos = new ItemPositionInfo();
            	if (position == 0) {
            		infos.type = 0;
            		infos.rows = 0;
            		infos.cloumns = 0;
            		infos.header_index = 0;
        			return infos;
        		}
            	
            	//curTotalNum++;
            	lasTtotalNum = curTotalNum;
            	for (Container c : mContainers) {
            		
            		curTotalNum += mContainers.get(header).items.size();
            	    //Aurora <SQF> <2014-08-13>  for NEW_UI begin
            	    //ORIGINALLY:
            		//totalRowSize += getAlbumDLRowSize(c);
            	    //SQF MODIFIED TO:
            		totalRowSize += getAlbumDLRowSize(c, t_num);
            	    //Aurora <SQF> <2014-08-13>  for NEW_UI end
            		
            		//view is header
            		if (position == curTotalNum+header+1) {
            			infos.type = 0;
                		infos.rows = totalRowSize;
                		infos.cloumns = 0;
                		infos.header_index = header+1;
        				return infos;
        			}
            		
            		if (position < curTotalNum+header+1) {
            			int t_rows = 0;
            			int tmp = position-lasTtotalNum -(header+1);
            			int tmp_col = (tmp+1) % t_num;
            			if (tmp_col == 0) {
            				t_rows = (tmp+1)/t_num;
            				tmp_col = tmp%t_num+1;
        				} else {
        					t_rows = tmp/t_num+1;
        				}
            			
            			rows = lasTotalRowSize+t_rows;

            			infos.type = 1;
            			infos.rows = rows;
                		infos.cloumns = tmp_col-1;
                		infos.header_index = header;
                		return infos;
        			}
            		
            		header++;
            		lasTtotalNum = curTotalNum;
            		lasTotalRowSize = totalRowSize;
        		}
			} catch (Exception e) {
				Log.e("SQF_LOG", "zll ---- getRowsAndCloumns fail -----");
			}
		}
    	
		return null;
	}
    //Aurora <SQF> <2014-08-13>  for NEW_UI end
    
    public static class ItemPositionInfo {
    	public int 	type;//0:header,1:imageviews
    	public int  rows;
    	public int 	cloumns;
    	public int 	header_index;
	}
    //Iuni <lory><2014-01-21> add end
}
