package com.android.gallery3d.ui;




import java.util.ArrayList;

import com.android.gallery3d.app.AbstractGalleryActivity;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.R;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaObject.PanoramaSupportCallback;
import com.android.gallery3d.data.Path;
import android.view.View;
import android.content.Intent;
import android.os.Handler;



public class AuroraActionModeHandler {

	private static final String TAG = "AuroraActionModeHandler";

    private static final int SUPPORT_MULTIPLE_MASK = MediaObject.SUPPORT_DELETE
            | MediaObject.SUPPORT_ROTATE | MediaObject.SUPPORT_SHARE
            | MediaObject.SUPPORT_CACHE | MediaObject.SUPPORT_IMPORT;
    
	private AuroraActionModeListener mListener;
	private final AbstractGalleryActivity mActivity;
	private final SelectionManager mSelectionManager;
	private Future<?> mMenuTask;
	private final MenuExecutor mMenuExecutor;
	private final Handler mMainHandler;
	
	
	public AuroraActionModeHandler(AbstractGalleryActivity activity, SelectionManager selectionManager) {
		mActivity = Utils.checkNotNull(activity);
        mSelectionManager = Utils.checkNotNull(selectionManager);
        mMenuExecutor = new MenuExecutor(activity, selectionManager);
        mMainHandler = new Handler(activity.getMainLooper());
	}
	
	private static class GetAllPanoramaSupports implements PanoramaSupportCallback {
        private int mNumInfoRequired;
        private JobContext mJobContext;
        public boolean mAllPanoramas = true;
        public boolean mAllPanorama360 = true;
        public boolean mHasPanorama360 = false;
        private Object mLock = new Object();

        public GetAllPanoramaSupports(ArrayList<MediaObject> mediaObjects, JobContext jc) {
            mJobContext = jc;
            mNumInfoRequired = mediaObjects.size();
            for (MediaObject mediaObject : mediaObjects) {
                mediaObject.getPanoramaSupport(this);
            }
        }

        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360) {
            synchronized (mLock) {
                mNumInfoRequired--;
                mAllPanoramas = isPanorama && mAllPanoramas;
                mAllPanorama360 = isPanorama360 && mAllPanorama360;
                mHasPanorama360 = mHasPanorama360 || isPanorama360;
                if (mNumInfoRequired == 0 || mJobContext.isCancelled()) {
                    mLock.notifyAll();
                }
            }
        }

        public void waitForPanoramaSupport() {
            synchronized (mLock) {
                while (mNumInfoRequired != 0 && !mJobContext.isCancelled()) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        // May be a cancelled job context
                    }
                }
            }
        }
    }
	
	public interface AuroraActionModeListener {
        public boolean onActionItemClicked(MenuItem item);
    }

	public void setTitle(String title) {
    }
	
	public void finishActionMode() {
    }
	
	public void setActionModeListener(AuroraActionModeListener listener) {
        mListener = listener;
    }
	
	public void startActionMode() {
    }
	
	private ArrayList<MediaObject> getSelectedMediaObjects(JobContext jc) {
        ArrayList<Path> unexpandedPaths = mSelectionManager.getSelected(false);
        if (unexpandedPaths.isEmpty()) {
            // This happens when starting selection mode from overflow menu
            // (instead of long press a media object)
            return null;
        }
        ArrayList<MediaObject> selected = new ArrayList<MediaObject>();
        DataManager manager = mActivity.getDataManager();
        for (Path path : unexpandedPaths) {
            if (jc.isCancelled()) {
                return null;
            }
            selected.add(manager.getMediaObject(path));
        }

        return selected;
    }
	
	private int computeMenuOptions(ArrayList<MediaObject> selected) {
        int operation = MediaObject.SUPPORT_ALL;
        int type = 0;
        for (MediaObject mediaObject: selected) {
            int support = mediaObject.getSupportedOperations();
            type |= mediaObject.getMediaType();
            operation &= support;
        }

        switch (selected.size()) {
            case 1:
                final String mimeType = MenuExecutor.getMimeType(type);
                if (!GalleryUtils.isEditorAvailable(mActivity, mimeType)) {
                    operation &= ~MediaObject.SUPPORT_EDIT;
                }
                break;
            default:
                operation &= SUPPORT_MULTIPLE_MASK;
        }

        return operation;
    }
	//paul add for UI_20 start
	public void onActionFavorite() {
		mMenuExecutor.setItemsFavorite();
	}
	
	public void enterFavoriteMode(boolean enter){
		mMenuExecutor.setFavoriteState(enter);
	}
	//paul add for UI_20 end
	public void onActionDelete(int itemId, String confirmMsg, final MenuExecutor.ProgressListener listener, boolean bAlbumPage) {
		
		GLRoot root = mActivity.getGLRoot();
        root.lockRenderThread();
        try {
            if (itemId == R.id.action_delete) {
                updateSupportedOperation();
                mMenuExecutor.onAuroraMenuClickedEx(itemId, confirmMsg, listener, bAlbumPage);
                //mMenuExecutor.onMenuClickedEx(itemId, confirmMsg, listener);
            }
            
            return;
        } finally {
            root.unlockRenderThread();
        }
		
	}
	
	//SQF ADDED ON 2015.04.23 begin
	public void onActionUploadToXCloud(int itemId, String confirmMsg, final MenuExecutor.ProgressListener listener, boolean bAlbumPage) {
		GLRoot root = mActivity.getGLRoot();
        root.lockRenderThread();
        try {
            if (itemId == R.id.action_upload_to_xcloud) {
            	Log.i("SQF_LOG", "AuroraActionModeHandler::onActionUploadToXCloud action_upload_to_xcloud");
                updateSupportedOperation();
                mMenuExecutor.onAuroraMenuClickedEx(itemId, confirmMsg, listener, bAlbumPage);
                //mMenuExecutor.onMenuClickedEx(itemId, confirmMsg, listener);
            }
            
            return;
        } finally {
            root.unlockRenderThread();
        }
	}
	//SQF ADDED ON 2015.04.23 end
	
	public void updateSupportedOperation() {/*
        // Interrupt previous unfinished task, mMenuTask is only accessed in main thread
        if (mMenuTask != null) mMenuTask.cancel();

        //updateSelectionMenu();

        // Disable share actions until share intent is in good shape
        //if (mSharePanoramaMenuItem != null) mSharePanoramaMenuItem.setEnabled(false);
        //if (mShareMenuItem != null) mShareMenuItem.setEnabled(false);

        // Generate sharing intent and update supported operations in the background
        // The task can take a long time and be canceled in the mean time.
        mMenuTask = mActivity.getThreadPool().submit(new Job<Void>() {
            @Override
            public Void run(final JobContext jc) {
                // Pass1: Deal with unexpanded media object list for menu operation.
                ArrayList<MediaObject> selected = getSelectedMediaObjects(jc);
                if (selected == null) {
                    return null;
                }
                final int operation = computeMenuOptions(selected);
                if (jc.isCancelled()) {
                    return null;
                }
                final GetAllPanoramaSupports supportCallback = new GetAllPanoramaSupports(selected,
                        jc);

                // Pass2: Deal with expanded media object list for sharing operation.
                //final Intent share_panorama_intent = computePanoramaSharingIntent(jc);
                //final Intent share_intent = computeSharingIntent(jc);

                supportCallback.waitForPanoramaSupport();
                if (jc.isCancelled()) {
                    return null;
                }
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMenuTask = null;
                        if (jc.isCancelled()) return;
                        MenuExecutor.updateMenuOperation(mMenu, operation);
                        MenuExecutor.updateMenuForPanorama(mMenu, supportCallback.mAllPanorama360,supportCallback.mHasPanorama360);
                        if (mSharePanoramaMenuItem != null) {
                            mSharePanoramaMenuItem.setEnabled(true);
                            if (supportCallback.mAllPanorama360) {
                                mShareMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                                mShareMenuItem.setTitle(
                                    mActivity.getResources().getString(R.string.share_as_photo));
                            } else {
                                mSharePanoramaMenuItem.setVisible(false);
                                mShareMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                                mShareMenuItem.setTitle(
                                    mActivity.getResources().getString(R.string.share));
                            }
                            mSharePanoramaActionProvider.setShareIntent(share_panorama_intent);
                        }
                        
                        if (mShareMenuItem != null) {
                            mShareMenuItem.setEnabled(true);
                            mShareActionProvider.setShareIntent(share_intent);
                        }
                    }
                });
                
                return null;
            }
        });
    */}
	
	public void pause() {
        if (mMenuTask != null) {
            mMenuTask.cancel();
            mMenuTask = null;
        }
        
        mMenuExecutor.auroraPause();
    }

    public void resume() {
        if (mSelectionManager.inSelectionMode()) updateSupportedOperation();
    }
    
    public boolean getMenuDialogActive() {
    	if (mMenuExecutor != null) {
    		return mMenuExecutor.getDialogActive();
		}
    	
    	return false;
	}
}
