/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.recent;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.tablet.TabletStatusBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Aurora <zhanggp> <2013-10-17> added for systemui begin
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.view.Surface;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.android.systemui.recent.utils.AuroraRecentsClearUtils;
// Aurora <zhanggp> <2013-10-17> added for systemui end


public class RecentTasksLoader implements View.OnTouchListener {
    static final String TAG = "RecentTasksLoader";
    static final boolean DEBUG = TabletStatusBar.DEBUG || PhoneStatusBar.DEBUG || false;

    private static final int DISPLAY_TASKS = 20;
    private static final int MAX_TASKS = DISPLAY_TASKS + 1; // allow extra for non-apps

    private Context mContext;
    private RecentsPanelView mRecentsPanel;

    private Object mFirstTaskLock = new Object();
    private TaskDescription mFirstTask;
    private boolean mFirstTaskLoaded;

    private AsyncTask<Void, ArrayList<TaskDescription>, Void> mTaskLoader;
    private AsyncTask<Void, TaskDescription, Void> mThumbnailLoader;
    private Handler mHandler;

    private int mIconDpi;
    
    private Bitmap mDefaultIconBackground;
    private int mNumTasksInFirstScreenful = Integer.MAX_VALUE;

    private boolean mFirstScreenful;
    private ArrayList<TaskDescription> mLoadedTasks;

    private enum State { LOADING, LOADED, CANCELLED };
    private State mState = State.CANCELLED;
// Aurora <zhanggp> <2013-10-17> modified  for systemui begin
	private WindowManager mWindowManager;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
	private BitmapDrawable mRecentBg;
	//private Bitmap mDefaultThumbnailBackground;
// Aurora <zhanggp> <2013-10-17> modified  for systemui end
    private static RecentTasksLoader sInstance;
    public static RecentTasksLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecentTasksLoader(context);
        }
        return sInstance;
    }

    private RecentTasksLoader(Context context) {
        mContext = context;
        mHandler = new Handler();

        final Resources res = context.getResources();

        // get the icon size we want -- on tablets, we use bigger icons
        boolean isTablet = res.getBoolean(R.bool.config_recents_interface_for_tablets);
        if (isTablet) {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            mIconDpi = activityManager.getLauncherLargeIconDensity();
        } else {
            mIconDpi = res.getDisplayMetrics().densityDpi;
        }

        // Render default icon (just a blank image)
        int defaultIconSize = res.getDimensionPixelSize(com.aurora.R.dimen.app_icon_size);
        int iconSize = (int) (defaultIconSize * mIconDpi / res.getDisplayMetrics().densityDpi);
        mDefaultIconBackground = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);

        // Aurora <Felix.Duan> <2014-8-26> <BEGIN> Fix SystemUI OOM error.
        ICON_REAL_SIZE = defaultIconSize;
        // Aurora <Felix.Duan> <2014-8-26> <END> Fix SystemUI OOM error.

        // Render the default thumbnail background
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        /*
	        int thumbnailWidth =
	                (int) res.getDimensionPixelSize(com.aurora.R.dimen.thumbnail_width);
	        int thumbnailHeight =
	                (int) res.getDimensionPixelSize(com.aurora.R.dimen.thumbnail_height);
	        int color = res.getColor(R.drawable.status_bar_recents_app_thumbnail_background);

	        mDefaultThumbnailBackground =
	                Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
	        Canvas c = new Canvas(mDefaultThumbnailBackground);
	        c.drawColor(color);
		*/
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();
		mDisplayMetrics = new DisplayMetrics();
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
		
    }

    public void setRecentsPanel(RecentsPanelView newRecentsPanel, RecentsPanelView caller) {
        // Only allow clearing mRecentsPanel if the caller is the current recentsPanel
        if (newRecentsPanel != null || mRecentsPanel == caller) {
            mRecentsPanel = newRecentsPanel;
            if (mRecentsPanel != null) {
                mNumTasksInFirstScreenful = mRecentsPanel.numItemsInOneScreenful();
            }
        }
    }
// Aurora <zhanggp> <2013-10-17> modified for systemui begin
/*
    public Bitmap getDefaultThumbnail() {
        return mDefaultThumbnailBackground;
    }
*/

	public BitmapDrawable getRecentBgImage(){
		if(mFirstTaskLoaded){
			return  mRecentBg;
		}
		return null;
	}
// Aurora <zhanggp> <2013-10-17> modified for systemui end
    public Bitmap getDefaultIcon() {
        return mDefaultIconBackground;
    }

    public ArrayList<TaskDescription> getLoadedTasks() {
        return mLoadedTasks;
    }

    public boolean isFirstScreenful() {
        return mFirstScreenful;
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            final PackageManager pm = mContext.getPackageManager();
            homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .resolveActivityInfo(pm, 0);
        }
		// Aurora <zhanggp> <2013-11-02> modified for systemui begin
        if (component.getClassName().equals("com.aurora.launcher.Launcher")) {
            return true;
        }
        // Aurora <zhanggp> <2013-11-02> modified for systemui end
		
        return homeInfo != null
            && homeInfo.packageName.equals(component.getPackageName())
            && homeInfo.name.equals(component.getClassName());
    }

    // Create an TaskDescription, returning null if the title or icon is null
    TaskDescription createTaskDescription(int taskId, int persistentTaskId, Intent baseIntent,
            ComponentName origActivity, CharSequence description) {
        Intent intent = new Intent(baseIntent);
        if (origActivity != null) {
            intent.setComponent(origActivity);
        }
        final PackageManager pm = mContext.getPackageManager();
        intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            final ActivityInfo info = resolveInfo.activityInfo;
            final String title = info.loadLabel(pm).toString();

            if (title != null && title.length() > 0) {
                if (DEBUG) Log.v(TAG, "creating activity desc for id="
                        + persistentTaskId + ", label=" + title);

                TaskDescription item = new TaskDescription(taskId,
                        persistentTaskId, resolveInfo, baseIntent, info.packageName,
                        description);
                item.setLabel(title);

                return item;
            } else {
                if (DEBUG) Log.v(TAG, "SKIPPING item " + persistentTaskId);
            }
        }
        return null;
    }
// Aurora <zhanggp> <2013-10-17> modified for systemui begin
    void loadThumbnailAndIcon(TaskDescription td) {
        final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final PackageManager pm = mContext.getPackageManager();
        Drawable icon = getFullResIcon(td.resolveInfo, pm);

        synchronized (td) {

            if (icon != null) {
                td.setIcon(icon);
            }
            td.setLoaded(true);
        }
    }
/*
    void loadThumbnailAndIcon(TaskDescription td) {
        final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final PackageManager pm = mContext.getPackageManager();
        Bitmap thumbnail = am.getTaskTopThumbnail(td.persistentTaskId);
        Drawable icon = getFullResIcon(td.resolveInfo, pm);

        if (DEBUG) Log.v(TAG, "Loaded bitmap for task "
                + td + ": " + thumbnail);
        synchronized (td) {
            if (thumbnail != null) {
                td.setThumbnail(thumbnail);
            } else {
                td.setThumbnail(mDefaultThumbnailBackground);
            }
            if (icon != null) {
                td.setIcon(icon);
            }
            td.setLoaded(true);
        }
    }
*/
// Aurora <zhanggp> <2013-10-17> modified for systemui end
    Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
    }

    Drawable getFullResIcon(Resources resources, int iconId) {
        try {
            // Aurora <Felix.Duan> <2014-8-26> <BEGIN> Fix SystemUI OOM error.
            // Load sampled resource to avoid OOM error
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, iconId, option);
            option.inSampleSize = calculateInSampleSize(option, ICON_REAL_SIZE, ICON_REAL_SIZE);
            option.inJustDecodeBounds = false;
            Bitmap bmpIcon = BitmapFactory.decodeResource(resources, iconId, option);
            return new BitmapDrawable(resources, bmpIcon);
            //return resources.getDrawableForDensity(iconId, mIconDpi);
            // Aurora <Felix.Duan> <2014-8-26> <END> Fix SystemUI OOM error.
        } catch (Resources.NotFoundException e) {
            return getFullResDefaultActivityIcon();
        }
    }

    // Aurora <Felix.Duan> <2014-8-26> <BEGIN> Fix SystemUI OOM error.
    private int ICON_REAL_SIZE;
    // Calculate resource sample rate
    private int calculateInSampleSize(BitmapFactory.Options options,  
            int reqWidth, int reqHeight) {  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
        if (height > reqHeight || width > reqWidth) {  
            final int heightRatio = Math.round((float) height / (float) reqHeight);  
            final int widthRatio = Math.round((float) width / (float) reqWidth);  
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;  
        }  
        return inSampleSize;  
    }  
    // Aurora <Felix.Duan> <2014-8-26> <END> Fix SystemUI OOM error.

    private Drawable getFullResIcon(ResolveInfo info, PackageManager packageManager) {
        Resources resources;
        try {
            resources = packageManager.getResourcesForApplication(
                    info.activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.activityInfo.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    Runnable mPreloadTasksRunnable = new Runnable() {
            public void run() {
                loadTasksInBackground();
            }
        };

    // additional optimization when we have software system buttons - start loading the recent
    // tasks on touch down
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            preloadRecentTasksList();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            cancelPreloadingRecentTasksList();
        } else if (action == MotionEvent.ACTION_UP) {
            // Remove the preloader if we haven't called it yet
            mHandler.removeCallbacks(mPreloadTasksRunnable);
            if (!v.isPressed()) {
                cancelLoadingThumbnailsAndIcons();
            }

        }
        return false;
    }

    public void preloadRecentTasksList() {
        mHandler.post(mPreloadTasksRunnable);
    }

    public void cancelPreloadingRecentTasksList() {
		if(mRecentsPanel == null){
        	cancelLoadingThumbnailsAndIcons();
        	mHandler.removeCallbacks(mPreloadTasksRunnable);
		}
    }

    public void cancelLoadingThumbnailsAndIcons(RecentsPanelView caller) {
        // Only oblige this request if it comes from the current RecentsPanel
        // (eg when you rotate, the old RecentsPanel request should be ignored)
        if (mRecentsPanel == caller) {
            cancelLoadingThumbnailsAndIcons();
        }
    }


    private void cancelLoadingThumbnailsAndIcons() {
        Log.d(TAG, "cancelLoadingThumbnailsAndIcons()");
        if (mTaskLoader != null) {
            mTaskLoader.cancel(false);
            mTaskLoader = null;
        }
        if (mThumbnailLoader != null) {
            mThumbnailLoader.cancel(false);
            mThumbnailLoader = null;
        }
        mLoadedTasks = null;
        if (mRecentsPanel != null) {
            mRecentsPanel.onTaskLoadingCancelled();
        }
        mFirstScreenful = false;
        mState = State.CANCELLED;
    }

    private void clearFirstTask() {
        synchronized (mFirstTaskLock) {
            mFirstTask = null;
            mFirstTaskLoaded = false;
        }
    }

    public void preloadFirstTask() {
        Thread bgLoad = new Thread() {
            public void run() {
                TaskDescription first = loadFirstTask();
                synchronized(mFirstTaskLock) {
                    if (mCancelPreloadingFirstTask) {
                        clearFirstTask();
                    } else {
                        mFirstTask = first;
                        mFirstTaskLoaded = true;
                    }
                    mPreloadingFirstTask = false;
                }
            }
        };
        synchronized(mFirstTaskLock) {
            if (!mPreloadingFirstTask) {
                clearFirstTask();
                mPreloadingFirstTask = true;
                bgLoad.start();
            }
        }
    }

    public void cancelPreloadingFirstTask() {
		if(mRecentsPanel == null){
	        synchronized(mFirstTaskLock) {
	            if (mPreloadingFirstTask) {
	                mCancelPreloadingFirstTask = true;
	            } else {
	                clearFirstTask();
	            }
	        }
		}
    }

    boolean mPreloadingFirstTask;
    boolean mCancelPreloadingFirstTask;
    public TaskDescription getFirstTask() {
        while(true) {
            synchronized(mFirstTaskLock) {
                if (mFirstTaskLoaded) {
                    return mFirstTask;
                } else if (!mFirstTaskLoaded && !mPreloadingFirstTask) {
                    mFirstTask = loadFirstTask();
                    mFirstTaskLoaded = true;
                    return mFirstTask;
                }
            }
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
            }
        }
    }
	
// Aurora <zhanggp> <2013-10-17> modified for systemui begin
    public TaskDescription loadFirstTask() {
		AuroraRecentsClearUtils.loadLockFlag(mContext);
		/*
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasksForUser(
                1, ActivityManager.RECENT_IGNORE_UNAVAILABLE, UserHandle.CURRENT.getIdentifier());
        TaskDescription item = null;
        if (recentTasks.size() > 0) {
            ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(0);

            Intent intent = new Intent(recentInfo.baseIntent);
            if (recentInfo.origActivity != null) {
                intent.setComponent(recentInfo.origActivity);
            }

            // Don't load the current home activity.
            if (isCurrentHomeActivity(intent.getComponent(), null)) {
                return null;
            }

            // Don't load ourselves
            if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                return null;
            }

            item = createTaskDescription(recentInfo.id,
                    recentInfo.persistentId, recentInfo.baseIntent,
                    recentInfo.origActivity, recentInfo.description);
            if (item != null) {
                loadThumbnailAndIcon(item);
            }
            return item;
        }*/
        return null;
    }
// Aurora <zhanggp> <2013-10-17> modified for systemui end
	public boolean isRecentActivityShowing(){
		if(null != mRecentsPanel){
			return mRecentsPanel.isRecentActivityShowing();
		}
		return false;
	}
    public void loadTasksInBackground() {
        loadTasksInBackground(false);
    }

    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
    public void loadTasksInBackground(final boolean sample) {
        Log.d(TAG, "loadTasksInBackground()  sample = " + sample + "  mState = " + mState);
    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
        if (mState != State.CANCELLED) {
            return;
        }
        mState = State.LOADING;
        mFirstScreenful = true;
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        //final LinkedBlockingQueue<TaskDescription> tasksWaitingForThumbnails =
        //        new LinkedBlockingQueue<TaskDescription>();
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
        mTaskLoader = new AsyncTask<Void, ArrayList<TaskDescription>, Void>() {
            @Override
            protected void onProgressUpdate(ArrayList<TaskDescription>... values) {
                if (!isCancelled()) {
                    ArrayList<TaskDescription> newTasks = values[0];

                    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                    Log.d(TAG, "loadTasksInBackground()  onProgressUpdate() sample = " + sample + " newTasks = " + newTasks.size());
                    if (sample) {
                        if (mSampleTasks == null) {
                            mSampleTasks = new ArrayList<TaskDescription>();
                        }
                        mSampleTasks.clear();
                        mSampleTasks.addAll(newTasks);
                        
                        mState = State.CANCELLED;
                        if (hasTasksChanged()) {
                            loadTasksInBackground();
                        }
                    } else {
                        // do a callback to RecentsPanelView to let it know we have more values
                        // how do we let it know we're all done? just always call back twice
                        if (mRecentsPanel != null) {
                            mRecentsPanel.onTasksLoaded(newTasks, mFirstScreenful);
                        }
                        if (mLoadedTasks == null) {
                            mLoadedTasks = new ArrayList<TaskDescription>();
                        }
                        mLoadedTasks.clear();
                        mLoadedTasks.addAll(newTasks);
                        Log.d(TAG, "loadTasksInBackground()  onProgressUpdate() sample = " + sample + " mLoadedTasks.size() = " + mLoadedTasks.size());
                        mFirstScreenful = false;
                        // Aurora <Felix.Duan> <2014-4-29> <BEGIN> Fix BUG #4467.  Reset task loader status when loading finished.
                        mState = State.CANCELLED;
                        // Aurora <Felix.Duan> <2014-4-29> <END> Fix BUG #4467.  Reset task loader status when loading finished.
                    }
                    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
                }
            }
            @Override
            protected Void doInBackground(Void... params) {
                Log.d(TAG, "loadTasksInBackground()  doInBackground()");
                // We load in two stages: first, we update progress with just the first screenful
                // of items. Then, we update with the rest of the items
                final int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                final PackageManager pm = mContext.getPackageManager();
                final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);

                // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                final List<ActivityManager.RecentTaskInfo> recentTasks =
                        am.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
                int numTasks = recentTasks.size();
                ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);
				// Aurora <zhanggp> <2013-10-17> modified for systemui begin
                boolean firstScreenful = false;
				//boolean firstScreenful = true;
				// Aurora <zhanggp> <2013-10-17> modified for systemui end
                ArrayList<TaskDescription> tasks = new ArrayList<TaskDescription>();

                // skip the first task - assume it's either the home screen or the current activity.
				// Aurora <zhanggp> <2013-10-17> modified for systemui begin
                final int first = 1;
				//final int first = 0;
				// Aurora <zhanggp> <2013-10-17> modified for systemui end
                for (int i = first, index = 0; i < numTasks && (index < MAX_TASKS); ++i) {
                    if (isCancelled()) {
                        break;
                    }
                    final ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(i);

                    Intent intent = new Intent(recentInfo.baseIntent);
                    if (recentInfo.origActivity != null) {
                        intent.setComponent(recentInfo.origActivity);
                    }

                    // Don't load the current home activity.
                    if (isCurrentHomeActivity(intent.getComponent(), homeInfo)) {
                        continue;
                    }

                    // Aurora <Felix.Duan> <2014-8-6> <BEGIN> Hide Hello-IUNI in recent task
                    // Don`t load voicewakeup pkg
                    if (intent.getComponent().getPackageName().equals("com.qualcomm.listen.voicewakeup")) {
                        continue;
                    }
                    // Aurora <Felix.Duan> <2014-8-6> <END> Hide Hello-IUNI in recent task

                    // Don't load ourselves
                    if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                        continue;
                    }
                    TaskDescription item = createTaskDescription(recentInfo.id,
                            recentInfo.persistentId, recentInfo.baseIntent,
                            recentInfo.origActivity, recentInfo.description);

                    if (item != null) {
						// Aurora <zhanggp> <2013-10-17> modified for systemui begin
						 loadThumbnailAndIcon(item);
						/*
                        while (true) {
                            try {
                                tasksWaitingForThumbnails.put(item);
                                break;
                            } catch (InterruptedException e) {
                            }
                        }*/
                       
						// Aurora <zhanggp> <2013-10-17> modified for systemui end
                        tasks.add(item);
                        if (firstScreenful && tasks.size() == mNumTasksInFirstScreenful) {
                            publishProgress(tasks);
                            tasks = new ArrayList<TaskDescription>();
                            firstScreenful = false;
                            //break;
                        }
                        ++index;
                    }
                }

                if (!isCancelled()) {
                    publishProgress(tasks);
                    if (firstScreenful) {
                        // always should publish two updates
                        publishProgress(new ArrayList<TaskDescription>());
                    }
                }
				// Aurora <zhanggp> <2013-10-17> modified for systemui begin
				/*
                while (true) {
                    try {
                        tasksWaitingForThumbnails.put(new TaskDescription());
                        break;
                    } catch (InterruptedException e) {
                    }
                }
			    */
				// Aurora <zhanggp> <2013-10-17> modified for systemui end
                Process.setThreadPriority(origPri);
                return null;
            }
        };
        mTaskLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
		/*
        loadThumbnailsAndIconsInBackground(tasksWaitingForThumbnails);
		*/
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
    }

    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
    private static final int SAMPLES = 4;
    private ArrayList<TaskDescription> mSampleTasks;
    
    /**
     * If sample matches, then no need to reload tasks
     * If not match, reload recent tasks
     *
     * @author Felix.Duan
     * @date 2014-7-28
     */
    private boolean hasTasksChanged() {
        //loadTasksInBackground(true);
        if (mSampleTasks == null || mLoadedTasks == null) {
            Log.d(TAG, "hasTasksChanged() mSampleTasks is null");
            return true;
        }
        int count = Math.min(SAMPLES, Math.min(mSampleTasks.size(), mLoadedTasks.size()));
        //Log.d(TAG, "hasTasksChanged() mSampleTasks.size() = " + mSampleTasks.size());
        //Log.d(TAG, "hasTasksChanged() mLoadedTasks.size() = " + mLoadedTasks.size());
        //Log.d(TAG, "hasTasksChanged() count = " + count);

        if (count == 0) return true;

        // Aurora <Felix.Duan> <2014-9-9> <BEGIN> Fix recents not updated bug
        if (mSampleTasks.size() != mLoadedTasks.size()) return true;
        // Aurora <Felix.Duan> <2014-9-9> <END> Fix recents not updated bug

        //for (TaskDescription td: mSampleTasks) {
        TaskDescription sampleTd, loadedTd;
        for (int i = 0; i < count; i++) {
            sampleTd = mSampleTasks.get(i);
            //Log.d(TAG, "hasTasksChanged() mSampleTasks getPackageName = " + sampleTd.getPackageName() + " getLabel = " + sampleTd.getLabel() + " getResolveInfo = " + sampleTd.getResolveInfo());
            loadedTd = mLoadedTasks.get(i);
            //Log.d(TAG, "hasTasksChanged() mLoadedTasks getPackageName = " + loadedTd.getPackageName() + " getLabel = " + loadedTd.getLabel() + " getResolveInfo = " + loadedTd.getResolveInfo());
            if (!sampleTd.getPackageName().equals(loadedTd.getPackageName())
                || !sampleTd.getLabel().equals(loadedTd.getLabel())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check task sample comparison result.
     * According to result, to update full recent tasks list or not.
     *
     * @author Felix.Duan
     * @date 2014-7-28
     */
    public void checkTaskSample() {
        Log.d(TAG, "checkTaskSample()");
        loadTasksInBackground(true);
    }
    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views

	// Aurora <zhanggp> <2013-10-17> modified for systemui begin
	/*
    private void loadThumbnailsAndIconsInBackground(
            final BlockingQueue<TaskDescription> tasksWaitingForThumbnails) {
        // continually read items from tasksWaitingForThumbnails and load
        // thumbnails and icons for them. finish thread when cancelled or there
        // is a null item in tasksWaitingForThumbnails
		
        
        mThumbnailLoader = new AsyncTask<Void, TaskDescription, Void>() {
            @Override
            protected void onProgressUpdate(TaskDescription... values) {
                if (!isCancelled()) {
                    TaskDescription td = values[0];
                    if (td.isNull()) { // end sentinel
                        mState = State.LOADED;
                    } else {
                        if (mRecentsPanel != null) {
                            mRecentsPanel.onTaskThumbnailLoaded(td);
                        }
                    }
                }
            }
            @Override
            protected Void doInBackground(Void... params) {
                final int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                while (true) {
                    if (isCancelled()) {
                        break;
                    }
                    TaskDescription td = null;
                    while (td == null) {
                        try {
                            td = tasksWaitingForThumbnails.take();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (td.isNull()) { // end sentinel
                        publishProgress(td);
                        break;
                    }
                    loadThumbnailAndIcon(td);

                    publishProgress(td);
                }

                Process.setThreadPriority(origPri);
                return null;
            }
        };
        mThumbnailLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
    }
	*/
	// Aurora <zhanggp> <2013-10-17> modified for systemui end
}
