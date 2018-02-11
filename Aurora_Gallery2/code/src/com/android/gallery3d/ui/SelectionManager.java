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

import android.content.Context;
import android.util.Log;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.viewpager.ViewpagerActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import com.android.gallery3d.util.MediaSetUtils;

public class SelectionManager {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectionManager";

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;
    public static final int UNSELECT_ALL_MODE = 4;

	public static final int FAVORITE_MODE_INIT = 0;
    public static final int SET_FAVORITE_MODE = 1;
    public static final int UNSET_FAVORITE_MODE = 2;
	

    private Set<Path> mClickedSet;
    private MediaSet mSourceMediaSet;
    private SelectionListener mListener;
    private DataManager mDataManager;
    private boolean mInverseSelection;
    private boolean mIsAlbumPage;//paul modify <2015-09-15> for BUG #16513
    private boolean mInSelectionMode;
    private boolean mAutoLeave = false;//paul modify for BUG #14648
    private int mTotal;
    private Context mContext;//wenyongzhe 2016.1.7 new_ui
    

    public interface SelectionListener {
        public void onSelectionModeChange(int mode);
		public void onFavoriteModeChange(int mode);
        public void onSelectionChange(Path path, boolean selected);
    }

    public SelectionManager(AbstractGalleryActivity activity, boolean isAlbumPage) {
    	mContext = activity;
        mDataManager = activity.getDataManager();
        mClickedSet = new HashSet<Path>();
        mIsAlbumPage = isAlbumPage;//paul modify <2015-09-15> for BUG #16513 isAlbumSet;
        mTotal = -1;
    }

    // Whether we will leave selection mode automatically once the number of
    // selected items is down to zero.
    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

	private int queryFavCount(){
		int count = 0;
		Cursor cursor = null;
		try {
	        cursor = mContext.getContentResolver().query(
	                Images.Media.EXTERNAL_CONTENT_URI, 
	                new String[]{ImageColumns._ID},
	                MediaSetUtils.getLocalFavQueryStr(true),
	                null,
	                null);
	        if (cursor == null) {
	            Log.w(TAG, "query1 fail");
	            return 0;
	        }
			count = cursor.getCount();
        }  catch (Exception e) {//catch exception
				Log.e(TAG, "query1 exception!");
        } finally {
        	if (null != cursor){
            	cursor.close();
				cursor = null;
        	}
        }

		try {
			cursor = mContext.getContentResolver().query(
					Video.Media.EXTERNAL_CONTENT_URI, 
					new String[]{VideoColumns._ID},
					MediaSetUtils.getLocalFavQueryStr(false),
					null,
					null);
			if (cursor == null) {
				Log.w(TAG, "query2 fail");
				return count;
			}
			count += cursor.getCount();
		}  catch (Exception e) {//catch exception
				Log.e(TAG, "query2 exception!");
		} finally {
			if (null != cursor){
				cursor.close();
				cursor = null;
			}
		}
		return count;
	}
    public void selectAll() {
        mInverseSelection = true;
		mFavoriteCount = 0;
        mClickedSet.clear();
        enterSelectionMode();
		
		int total = getTotalCount();
		mFavoriteCount = queryFavCount();

		if(mFavoriteCount == total){
			if (mListener != null) mListener.onFavoriteModeChange(UNSET_FAVORITE_MODE);
		} else {
			if (mListener != null) mListener.onFavoriteModeChange(SET_FAVORITE_MODE);
		}
		
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    public void deSelectAll() {
        leaveSelectionMode();
        mInverseSelection = false;
        mClickedSet.clear();
    }
    
    //Iuni <lory><2014-02-22> add begin
    public void unSelectAll() {
        unSelectionAllMode();
        mInverseSelection = false;
        mClickedSet.clear();
    }
    
    private void unSelectionAllMode() {
        if (!mInSelectionMode) return;

        //mInSelectionMode = false;
        mInverseSelection = false;
		mFavoriteCount = 0;
        mClickedSet.clear();
        if (mListener != null) mListener.onSelectionModeChange(UNSELECT_ALL_MODE);
		if (mListener != null) mListener.onFavoriteModeChange(SET_FAVORITE_MODE);
    }
    
    public boolean inSelectedAllItem() {
    	int count = getSelectedCount();
        if (count == getTotalCount()) {
        	return true;
        }
    	
        return false;
    }
    //Iuni <lory><2014-02-22> add end

    public boolean inSelectAllMode() {
        return mInverseSelection;
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode) return;

        mInSelectionMode = true;
		mFavoriteCount = 0;
        if (mListener != null) {
			mListener.onFavoriteModeChange(FAVORITE_MODE_INIT);//paul add for UI_20
        	mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
        }
    }

	//paul add <2016.02.26> fix popupwindow bug start
	boolean mPaused = false;
	public void pause(){
		if (!mInSelectionMode) return;
		mPaused = true;
        if (mListener != null) {
        	mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
        }
	}

	public boolean resume(){
		if(mPaused){
			mPaused = false;
	        if (mListener != null) {
	        	mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
				return true;
	        }
		}
		return false;
	}
	//paul add <2016.02.26> fix popupwindow bug end
	
    public void leaveSelectionMode() {
        if (!mInSelectionMode) return;

        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        if (mListener != null) {
        	mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
        }
    }

    public boolean isItemSelected(Path itemId) {
        return mInverseSelection ^ mClickedSet.contains(itemId);
    }

    private int getTotalCount() {
        if (mSourceMediaSet == null) return -1;
		//Log.i(TAG, "getTotalCount:" + mTotal);
		//paul del <2015-09-15> for BUG #16513
        //lory del 2014.4.3 if (mTotal < 0) 
        /*
        {
            mTotal = mIsAlbumSet
                    ? mSourceMediaSet.getSubMediaSetCount()
                    : mSourceMediaSet.getMediaItemCount();
        }
        */
        //Log.i(TAG, "zll ---- getTotalCount 2 mTotal:"+mTotal);
        return mTotal;
    }

    public int getSelectedCount() {
        int count = mClickedSet.size();
        if (mInverseSelection) {
            count = getTotalCount() - count;
        }
        //Log.i(TAG, "zll ---- getSelectedCount mInverseSelectionl:"+mInverseSelection);
        return count;
    }
	
	//paul modify start
	/*
    public void toggle(Path path) {
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
        }

        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        } 

        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
    }	
	*/
	
	//paul add <2015-09-15> for BUG #16513 start
	public void setTotalCount(int total){
		mTotal = total;
	}
	//paul add <2015-09-15> for BUG #16513 end
	
    public boolean toggle(Path path) {
	
	    //paul add <2015-09-15> for BUG #16513 start
    	if(!mIsAlbumPage){
            enterSelectionMode();
            mClickedSet.add(path);
			return true;
		}
		//paul add <2015-09-15> for BUG #16513 end
		
		boolean checked = false;
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
			checked = true;
        }

		if(mInverseSelection) checked = !checked;
		
        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        } 

        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
		
		return checked;
    }
	

	private int mFavoriteCount = 0;
    public boolean toggle(Path path, boolean isFavorite) {
	
	    //paul add <2015-09-15> for BUG #16513 start
    	if(!mIsAlbumPage){
            enterSelectionMode();
            mClickedSet.add(path);
			return true;
		}
		//paul add <2015-09-15> for BUG #16513 end
		
		boolean checked = false;
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
			checked = true;
        }

		if(mInverseSelection) checked = !checked;
		
        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalCount()) {
            selectAll();
        } 

		boolean selected = isItemSelected(path);
		
		if(isFavorite){
			if(selected) {
				++mFavoriteCount;
				if(mFavoriteCount == count){
					if (mListener != null) mListener.onFavoriteModeChange(UNSET_FAVORITE_MODE);
				}
			} else {
				--mFavoriteCount;
				if(mFavoriteCount + 1 == count || count == 0){
					if (mListener != null) mListener.onFavoriteModeChange(SET_FAVORITE_MODE);
				}
			}
		} else {
			if(selected) {
				if (mListener != null) mListener.onFavoriteModeChange(SET_FAVORITE_MODE);
			} else {
				if(mFavoriteCount == count) mListener.onFavoriteModeChange(UNSET_FAVORITE_MODE);
			}
		}
        if (mListener != null) mListener.onSelectionChange(path, selected);
        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
		
		return checked;
    }
	
    public void select(Path path, boolean toSelect, boolean isFavorite) {
		boolean selected = isItemSelected(path);
		
		if(toSelect == selected){
			return;
		}
		if(mInverseSelection) toSelect = !toSelect;
        if (mClickedSet.contains(path)) {
			if(!toSelect){
            	mClickedSet.remove(path);
			}
        } else {
			if(toSelect){
            	mClickedSet.add(path);
			}
        }
		
		if(isFavorite){
			if(toSelect) {
				++mFavoriteCount;
				if(mFavoriteCount == getSelectedCount()){
					if (mListener != null) mListener.onFavoriteModeChange(UNSET_FAVORITE_MODE);
				}
			} else {
				--mFavoriteCount;
				int c = getSelectedCount();
				if(mFavoriteCount + 1 == c || c == 0){
					if (mListener != null) mListener.onFavoriteModeChange(SET_FAVORITE_MODE);
				}
			}
		} else {
			if(toSelect) {
				if (mListener != null) mListener.onFavoriteModeChange(SET_FAVORITE_MODE);
			} else {
				if(mFavoriteCount == getSelectedCount()) mListener.onFavoriteModeChange(UNSET_FAVORITE_MODE);
			}
		}


        if (mListener != null) mListener.onSelectionChange(path, selected);
    }

	//paul modify end
    private static void expandMediaSet(ArrayList<Path> items, MediaSet set) {
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            expandMediaSet(items, set.getSubMediaSet(i));
        }
        int total = set.getMediaItemCount();
        int batch = 50;
        int index = 0;

        while (index < total) {
            int count = index + batch < total
                    ? batch
                    : total - index;
            ArrayList<MediaItem> list = set.getMediaItem(index, count);
            for (MediaItem item : list) {
                items.add(item.getPath());
            }
            index += batch;
        }
    }

    public ArrayList<Path> getSelected(boolean expandSet) {
        ArrayList<Path> selected = new ArrayList<Path>();
		//paul del <2015-09-15> for BUG #16513
		/*
        if (mIsAlbumSet) {
            if (mInverseSelection) {
                int total = getTotalCount();
                for (int i = 0; i < total; i++) {
                    MediaSet set = mSourceMediaSet.getSubMediaSet(i);
                    Path id = set.getPath();
                    if (!mClickedSet.contains(id)) {
                        if (expandSet) {
                            expandMediaSet(selected, set);
                        } else {
                            selected.add(id);
                        }
                    }
                }
            } else {
                for (Path id : mClickedSet) {
                    if (expandSet) {
                        expandMediaSet(selected, mDataManager.getMediaSet(id));
                    } else {
                        selected.add(id);
                    }
                }
            }
        } else */{
            if (mInverseSelection) {
                int total = getTotalCount();
                int index = 0;
                while (index < total) {
                    int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                    ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                    for (MediaItem item : list) {
                        Path id = item.getPath();
                        if (!mClickedSet.contains(id)) selected.add(id);
                    }
                    index += count;
                }
            } else {
                for (Path id : mClickedSet) {
                    selected.add(id);
                }
            }
        }
        return selected;
    }

    public ArrayList<Uri> getSelected(String mimetype[]) {
        ArrayList<Uri> uri = new ArrayList<Uri>();
		boolean mixMimetype = false;
		char flag = 0x0;
        if (mInverseSelection) {
            int total = getTotalCount();
            int index = 0;
            while (index < total) {
                int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                for (MediaItem item : list) {
					if(mClickedSet.contains(item.getPath())){
						continue;
					}
					if(!mixMimetype){
						if(item.getMimeType().startsWith("image")){
							flag |= 0x1;
						} else {
							flag |= 0x2;
						}
						if(flag == 0x3){
							mixMimetype = true;
						}
					}
					uri.add(item.getContentUri());
                }
                index += count;
            }
        } else {
            MediaItem obj;
			for (Path id : mClickedSet) {
                 obj = (MediaItem)mDataManager.getMediaObject(id);
				 if(null != obj){
					 if(!mixMimetype){
						 if(obj.getMimeType().startsWith("image")){
							 flag |= 0x1;
						 } else {
							 flag |= 0x2;
						 }
						 if(flag == 0x3){
							 mixMimetype = true;
						 }
					 }
					uri.add(obj.getContentUri());
				 }
            }
        }

		if(flag == 0x1){
			mimetype[0] = "image/*";
		} else if(flag == 0x2){
			mimetype[0] = "video/*";
		} else {
			mimetype[0] = "*/*";
		}
        return uri;
    }

    public String getSelected() {
        StringBuffer selectedIds = null;
		int itemId = -1;
		
        if (mInverseSelection) {
            int total = getTotalCount();
            int index = 0;
            while (index < total) {
                int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                for (MediaItem item : list) {
					if(mClickedSet.contains(item.getPath())){
						continue;
					}

					itemId = item.getId();
					if(itemId >= 0){
					 	if(null == selectedIds){
							selectedIds = new StringBuffer();
							selectedIds.append(itemId);
						} else {
							selectedIds.append(",");
							selectedIds.append(itemId);
						}
                	}
                }
                index += count;
            }
        } else {
            MediaItem obj;
			for (Path id : mClickedSet) {
                 obj = (MediaItem)mDataManager.getMediaObject(id);
				 if(null != obj){
					 itemId = obj.getId();
					 if(itemId >= 0){
					 	if(null == selectedIds){
							selectedIds = new StringBuffer();
							selectedIds.append(itemId);
						} else {
							selectedIds.append(",");
							selectedIds.append(itemId);
						}
			 		 }
				 }
            }
        }
        return null == selectedIds ? null : selectedIds.toString();
    }

    public ArrayList<MediaItem> getSelectedInfo() {//wenyongzhe 2016.3.7
		ArrayList<MediaItem> selectFileInfo = new ArrayList<MediaItem>();
		int itemId = -1;
        if (mInverseSelection) {
            int total = getTotalCount();
            int index = 0;
            while (index < total) {
                int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                for (MediaItem item : list) {
					if(mClickedSet.contains(item.getPath())){
						continue;
					}
					itemId = item.getId();
					if(itemId >= 0){
						selectFileInfo.add(item);//wenyongzhe 2016.3.7
                	}
                }
                index += count;
            }
        } else {
            MediaItem obj;
			for (Path id : mClickedSet) {
                 obj = (MediaItem)mDataManager.getMediaObject(id);
				 if(null != obj){
					 itemId = obj.getId();
					 if(itemId >= 0){
						 selectFileInfo.add(obj);//wenyongzhe 2016.3.7
			 		 }
				 }
            }
        }
        return selectFileInfo;
    }

    public void setSourceMediaSet(MediaSet set) {
        mSourceMediaSet = set;
        //mTotal = -1; //paul del <2015-09-15> for BUG #16513
    }
}
