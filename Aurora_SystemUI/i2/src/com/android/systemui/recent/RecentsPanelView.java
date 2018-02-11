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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.LayoutTransition;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.recent.HandlerBar.HandlerBarCallback;
import com.android.systemui.recent.utils.AuroraRecentsClearUtils;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy;
import com.android.systemui.statusbar.phone.BarTransitions;
import com.android.systemui.statusbar.tablet.StatusBarPanel;
import com.android.systemui.statusbar.tablet.TabletStatusBar;
import com.gionee.featureoption.FeatureOption;
import com.android.systemui.Xlog;

import android.view.KeyEvent;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.HashMap;

// Aurora <zhanggp> <2013-10-17> added for systemui begin
import android.widget.Button;
// Aurora <zhanggp> <2013-10-17> added for systemui end
//Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view begin
import android.view.animation.Animation;
import android.os.Handler;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.widget.RelativeLayout;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.view.Gravity;

//Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view end
public class RecentsPanelView extends FrameLayout implements OnItemClickListener, RecentsCallback,
        StatusBarPanel, Animator.AnimatorListener{
    static final String TAG = "RecentsPanelView";
    static final boolean DEBUG = TabletStatusBar.DEBUG || PhoneStatusBar.DEBUG || false;
    private PopupMenu mPopup;
    private View mRecentsScrim;
    private View mRecentsNoApps;
    private ViewGroup mRecentsContainer;
	public static int phoneHeight;
	public static int phoneWidth;
	// Aurora <zhanggp> <2013-10-17> modified for systemui begin
	private View mQuickSetting;
	//private int mNumItemsWaitingForThumbnailsAndIcons;
	// Aurora <zhanggp> <2013-10-17> modified for systemui end
    private StatusBarTouchProxy mStatusBarTouchProxy;

    // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
    private static final int QUIT_ANIMATION_DURATION = 300;
    // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration

    // Aurora <Felix.Duan> <2014-4-19> <BEGIN> Optimize RecentsPanel quit alpha change
    private static final int MASK_VIEW_FULL_ALPHA = 210;
    // Aurora <Felix.Duan> <2014-4-19> <END>  Optimize RecentsPanel quit alpha change

    private boolean mShowing;
    private boolean mWaitingToShow;
    
    private ViewHolder mItemToAnimateInWhenWindowAnimationIsFinished;
    private boolean mWaitingForWindowAnimation;

    private RecentTasksLoader mRecentTasksLoader;
    private ArrayList<TaskDescription> mRecentTaskDescriptions;
    private TaskDescriptionAdapter mListAdapter;
    private int mThumbnailWidth;
    private boolean mFitThumbnailToXY;
    private int mRecentItemLayoutId;
    private boolean mHighEndGfx;
    private HandlerBarCallback mHandlerBarCallback;
    private IntentFilter mIntentFilter;
    public static final String CLOSE_RECENTSPAELVIEW = "com.android.systemui.recent.close_recentspanelview";
    public static final String OPEN_RECENTSPAELVIEW = "com.android.systemui.recent.open_recentspanelview";
    
    //iht 2015-03-23 更新图标广播
    public static final String NOTIFY_APPS_TO_UPDATEIICON = "com.aurora.action.pulbicres.update";
    
 // Aurora <tongyh> <2013-12-13> in response to the home button to exit begin
    public static boolean isResponseHomeKey = false;
    public boolean isScreenOff = false;
    private int density;
 // Aurora <tongyh> <2013-12-13> in response to the home button to exit end
    boolean isrung = true;

    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
    // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
    public boolean mAnimating = false;
    // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
    // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804

    // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
    // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
    // public boolean mScrolling = false; // View is scrolling
    // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
    // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
    
    // Aurora <Felix.Duan> <2014-4-25> <BEGIN> Fix BUG #4462 on SystemUI side, cooperate with Settings
    private static final String RECENTS_PANEL_HIDDEN = "com.android.systemui.recent.aurora.RECENTS_PANEL_HIDDEN";
    // Aurora <Felix.Duan> <2014-4-25> <END> Fix BUG #4462 on SystemUI side, cooperate with Settings

    // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
    private ObjectAnimator animQuickSettingTranslation, animQuickSettingAlpha, bgAlpha;
    // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation

    // Aurora <Felix.Duan> <2014-8-4> <BEGIN> Fix BUG #7009. Quit recents on voice command "Hello IUNI"
    private static final String VOICEWAKEUP_UNLOCK = "com.qualcomm.listen.voicewakeup.unlock";
    // Aurora <Felix.Duan> <2014-8-4> <END> Fix BUG #7009. Quit recents on voice command "Hello IUNI"

    private View mRecentsMaskView;
    private static float quickSettingAlpha = 0;

    // Aurora <Felix.Duan> <2014-10-14> <BEGIN> Fix BUG #8655.
    private AnimatorSet mEnterAnimSet = new AnimatorSet();
    // Aurora <Felix.Duan> <2014-10-14> <END> Fix BUG #8655.

    // Aurora <Felix.Duan> <2014-9-3> <BEGIN> Reference app kill policy from auto-start
    //private CharSequence[] items;
    RemoveTaskPolicy mRemoveTaskPolicy;
    // Aurora <Felix.Duan> <2014-9-3> <END> Reference app kill policy from auto-start
    private boolean isShowInputMethod = false;
    private AuroraRubbishView mAuroraRubbishView;
    public void init(HandlerBarCallback hbc, int mPhoneWidth, int mPhoenHeight){
    	mHandlerBarCallback = hbc;
    	phoneWidth = mPhoneWidth;
    	phoneHeight = mPhoenHeight;
    	//Aurora <tongyh> <2013-12-04> Monitor power button to shut down operations view  begin
    	mIntentFilter = new IntentFilter();
    	mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    	// Aurora <tongyh> <2013-12-13> in response to the home button to exit begin
    	mIntentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    	mIntentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
    	// Aurora <tongyh> <2013-12-13> in response to the home button to exit end
//    	mIntentFilter.addAction(CLOSE_RECENTSPAELVIEW);
//    	mIntentFilter.addAction(OPEN_RECENTSPAELVIEW);
    	//Aurora <tongyh> <2014-02-20> after opening the alarm handlerbar hidden begin
    	mIntentFilter.addAction("com.aurora.deskclock.startalarm");
    	mIntentFilter.addAction("com.aurora.deskclock.stopalarm");

        // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Refactor：update IME state &  sechdule task loading
    	//mIntentFilter.addAction("android.intent.action.ACTION_INPUT_METHOD_SHOW");
    	//mIntentFilter.addAction("android.intent.action.ACTION_INPUT_METHOD_HIDE");
        // Aurora <Felix.Duan> <2014-8-8> <END> Refactor：update IME state &  sechdule task loading
    	//Aurora <tongyh> <2014-02-20> after opening the alarm handlerbar hidden end

        // Aurora <Felix.Duan> <2014-8-4> <BEGIN> Fix BUG #7009. Quit recents on voice command "Hello IUNI"
        mIntentFilter.addAction(VOICEWAKEUP_UNLOCK);

        mIntentFilter.addAction("hhh.hhh.hhh.hhh");
        // Aurora <Felix.Duan> <2014-8-4> <END> Fix BUG #7009. Quit recents on voice command "Hello IUNI"
        
        //iht 2015-03-23 更新图标广播
        mIntentFilter.addAction(NOTIFY_APPS_TO_UPDATEIICON);

    	mContext.registerReceiver(mIntentReceiver, mIntentFilter);

        // Aurora <Felix.Duan> <2014-8-4> <BEGIN> Reload tasks on package removed
        mIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        mIntentFilter.addDataScheme("package");
    	mContext.registerReceiver(mPackageMgrReceiver, mIntentFilter);
        // Aurora <Felix.Duan> <2014-8-4> <END> Reload tasks on package removed

    	density = (int)(mContext.getResources().getDisplayMetrics().density);
    	//Aurora <tongyh> <2013-12-04> Monitor power button to shut down operations view  end

        // Aurora <Felix.Duan> <2014-9-3> <BEGIN> Reference app kill policy from auto-start
    	//items = mContext.getResources().getStringArray(R.array.aurora_white_table);
        mRemoveTaskPolicy = new RemoveTaskPolicy(mContext);
        // Aurora <Felix.Duan> <2014-9-3> <END> Reference app kill policy from auto-start
    }

    // Aurora <Felix.Duan> <2014-8-14> <BEGIN> Fix BUG #7324.  Pull up overlapped
    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged() [" + w + "," + h + "] [" + oldw + "," + oldh + "]");
        // When initializing, width & height can be wrong, update now
        phoneWidth = w;
        phoneHeight = h;
    }
    // Aurora <Felix.Duan> <2014-8-14> <END> Fix BUG #7324.  Pull up overlapped

    public static interface RecentsScrollView {
        public int numItemsInOneScreenful();
        public void setAdapter(TaskDescriptionAdapter adapter);
        public void setCallback(RecentsCallback callback);
        public void setMinSwipeAlpha(float minAlpha);
        public View findViewForTask(int persistentTaskId);
    }

    private final class OnLongClickDelegate implements View.OnLongClickListener {
        View mOtherView;
        OnLongClickDelegate(View other) {
            mOtherView = other;
        }
        public boolean onLongClick(View v) {
            return mOtherView.performLongClick();
        }
    }

    /* package */ final static class ViewHolder {
		
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
		ImageView lockView;
        //View thumbnailView;
        //ImageView thumbnailViewImage;
        //Bitmap thumbnailViewImageBitmap;
		//View calloutLine;
		//boolean loadedThumbnailAndIcon;
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
        ImageView iconView;
        TextView labelView;
        TextView descriptionView;
        
        TaskDescription taskDescription;
       
    }

    /* package */ final class TaskDescriptionAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public TaskDescriptionAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
            /*
             * Load only last page of icons when scrolling even though there`re more in the list
             */
            if (mRecentTaskDescriptions != null) {
                // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                // always load all tasks
                //if (mRecentTaskDescriptions.size() > AuroraPagedView.PAGE_VIEW_NUM
                //        && !((AuroraRecentPage) mRecentsContainer).isScrollable())
                //    return AuroraPagedView.PAGE_VIEW_NUM;
                //else
                //    return mRecentTaskDescriptions.size();
                return mRecentTaskDescriptions.size();
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
            } else {
                return 0;
            }
            // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
        }

        public Object getItem(int position) {
            return position; // we only need the index
        }

        public long getItemId(int position) {
            return position; // we just need something unique for this position
        }
// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        public View createView(ViewGroup parent) {
            View convertView = mInflater.inflate(mRecentItemLayoutId, parent, false);
         // Aurora <tongyh> <2014-04-04> note3 icon size change begin        
            if(SystemProperties.get("ro.product.device") != null && ("hlte".equals(SystemProperties.get("ro.product.device")) || "ha3g".equals(SystemProperties.get("ro.product.device")))){
            	convertView.setScaleX(0.93f);
                convertView.setScaleY(0.93f);
            }
         // Aurora <tongyh> <2014-04-04> note3 icon size change end
            ViewHolder holder = new ViewHolder();

			holder.lockView = (ImageView) convertView.findViewById(R.id.app_lock);

            holder.iconView = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.iconView.setImageBitmap(mRecentTasksLoader.getDefaultIcon());
            holder.labelView = (TextView) convertView.findViewById(R.id.app_label);
            
            holder.descriptionView = (TextView) convertView.findViewById(R.id.app_description);

            convertView.setTag(holder);
            return convertView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createView(parent);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            // index is reverse since most recent appears at the bottom...
            // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
            /*
             * Load only last page of icons when scrolling even though there`s more in the list
             */

            // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
            // always load all tasks
            //final int index = (mRecentTaskDescriptions.size() > AuroraPagedView.PAGE_VIEW_NUM
            //                    && !((AuroraRecentPage) mRecentsContainer).isScrollable())
            //        ? (AuroraPagedView.PAGE_VIEW_NUM - position - 1)
            //        : (mRecentTaskDescriptions.size() - position - 1);
            final int index = (mRecentTaskDescriptions.size() - position - 1);
            // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
            // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy

            final TaskDescription td = mRecentTaskDescriptions.get(index);


            if (td.isLoaded()) {
                updateIcon(holder, td.getIcon(), true, false);

            }
			
			/*((AuroraRecentlItemView)convertView).updateLocked(td.getPackageName() ,
					td.getResolveInfo().activityInfo.taskAffinity,td.getLabel(), td.getResolveInfo().activityInfo.name);*/
			((AuroraRecentlItemView)convertView).updateLocked( td.getResolveInfo(),  td.getLabel(), td.getResolveInfo().activityInfo.taskAffinity);
            holder.labelView.setText(td.getLabel());

			
            if (index == 0) {
                if (mWaitingForWindowAnimation) {
                    if (mItemToAnimateInWhenWindowAnimationIsFinished != null) {
                        holder.iconView.setAlpha(1f);
                        holder.iconView.setTranslationX(0f);
                        holder.iconView.setTranslationY(0f);
                        holder.labelView.setAlpha(1f);
                        holder.labelView.setTranslationX(0f);
                        holder.labelView.setTranslationY(0f);

                    }
                    mItemToAnimateInWhenWindowAnimationIsFinished = holder;
                    final int translation = -getResources().getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_icon_translate_distance);
                    final Configuration config = getResources().getConfiguration();
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        holder.iconView.setAlpha(0f);
                        holder.iconView.setTranslationX(translation);
                        holder.labelView.setAlpha(0f);
                        holder.labelView.setTranslationX(translation);

                    } else {
                        holder.iconView.setAlpha(0f);
                        holder.iconView.setTranslationY(translation);
                    }
                }
            }

            holder.taskDescription = td;
            return convertView;
        }


        public void recycleView(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();

            holder.iconView.setImageBitmap(mRecentTasksLoader.getDefaultIcon());
            holder.iconView.setVisibility(INVISIBLE);
            holder.labelView.setText(null);

            holder.iconView.setAlpha(1f);
            holder.iconView.setTranslationX(0f);
            holder.iconView.setTranslationY(0f);
            holder.labelView.setAlpha(1f);
            holder.labelView.setTranslationX(0f);
            holder.labelView.setTranslationY(0f);

			holder.lockView.setVisibility(INVISIBLE);

            holder.taskDescription = null;

        }
    }
/*

        public View createView(ViewGroup parent) {
            View convertView = mInflater.inflate(mRecentItemLayoutId, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.thumbnailView = convertView.findViewById(R.id.app_thumbnail);
            holder.thumbnailViewImage =
                    (ImageView) convertView.findViewById(R.id.app_thumbnail_image);
            // If we set the default thumbnail now, we avoid an onLayout when we update
            // the thumbnail later (if they both have the same dimensions)
            updateThumbnail(holder, mRecentTasksLoader.getDefaultThumbnail(), false, false);
            holder.iconView = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.iconView.setImageBitmap(mRecentTasksLoader.getDefaultIcon());
            holder.labelView = (TextView) convertView.findViewById(R.id.app_label);
            holder.calloutLine = convertView.findViewById(R.id.recents_callout_line);
            holder.descriptionView = (TextView) convertView.findViewById(R.id.app_description);

            convertView.setTag(holder);
            return convertView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createView(parent);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            // index is reverse since most recent appears at the bottom...
            final int index = mRecentTaskDescriptions.size() - position - 1;

            final TaskDescription td = mRecentTaskDescriptions.get(index);

            holder.labelView.setText(td.getLabel());
            holder.thumbnailView.setContentDescription(td.getLabel());
            holder.loadedThumbnailAndIcon = td.isLoaded();
            if (td.isLoaded()) {
                updateThumbnail(holder, td.getThumbnail(), true, false);
                updateIcon(holder, td.getIcon(), true, false);
                mNumItemsWaitingForThumbnailsAndIcons--;
            }
            if (index == 0) {
                if (mWaitingForWindowAnimation) {
                    if (mItemToAnimateInWhenWindowAnimationIsFinished != null) {
                        holder.iconView.setAlpha(1f);
                        holder.iconView.setTranslationX(0f);
                        holder.iconView.setTranslationY(0f);
                        holder.labelView.setAlpha(1f);
                        holder.labelView.setTranslationX(0f);
                        holder.labelView.setTranslationY(0f);
                        if (holder.calloutLine != null) {
                            holder.calloutLine.setAlpha(1f);
                            holder.calloutLine.setTranslationX(0f);
                            holder.calloutLine.setTranslationY(0f);
                        }
                    }
                    mItemToAnimateInWhenWindowAnimationIsFinished = holder;
                    final int translation = -getResources().getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_icon_translate_distance);
                    final Configuration config = getResources().getConfiguration();
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        holder.iconView.setAlpha(0f);
                        holder.iconView.setTranslationX(translation);
                        holder.labelView.setAlpha(0f);
                        holder.labelView.setTranslationX(translation);
                        holder.calloutLine.setAlpha(0f);
                        holder.calloutLine.setTranslationX(translation);
                    } else {
                        holder.iconView.setAlpha(0f);
                        holder.iconView.setTranslationY(translation);
                    }
                }
            }

            holder.thumbnailView.setTag(td);
            holder.thumbnailView.setOnLongClickListener(new OnLongClickDelegate(convertView));
            holder.taskDescription = td;
            return convertView;
        }

        public void recycleView(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            updateThumbnail(holder, mRecentTasksLoader.getDefaultThumbnail(), false, false);
            holder.iconView.setImageBitmap(mRecentTasksLoader.getDefaultIcon());
            holder.iconView.setVisibility(INVISIBLE);
            holder.labelView.setText(null);
            holder.thumbnailView.setContentDescription(null);
            holder.thumbnailView.setTag(null);
            holder.thumbnailView.setOnLongClickListener(null);
            holder.thumbnailView.setVisibility(INVISIBLE);
            holder.iconView.setAlpha(1f);
            holder.iconView.setTranslationX(0f);
            holder.iconView.setTranslationY(0f);
            holder.labelView.setAlpha(1f);
            holder.labelView.setTranslationX(0f);
            holder.labelView.setTranslationY(0f);
            if (holder.calloutLine != null) {
                holder.calloutLine.setAlpha(1f);
                holder.calloutLine.setTranslationX(0f);
                holder.calloutLine.setTranslationY(0f);
            }
            holder.taskDescription = null;
            holder.loadedThumbnailAndIcon = false;
        }
    }
*/
// Aurora <zhanggp> <2013-10-17> modified for systemui end
    public RecentsPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentsPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateValuesFromResources();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecentsPanelView,
                defStyle, 0);

        mRecentItemLayoutId = a.getResourceId(R.styleable.RecentsPanelView_recentItemLayout, 0);
        mRecentTasksLoader = RecentTasksLoader.getInstance(context);
		initQuickSettingsSize();
        a.recycle();
    }

	// Aurora <Steve.Tang> 2015-01-19. supprt orientation land recent panel. start
	private View quickSettingsView = null;
	private View recentPanelView = null;
	private View rootView = null;

	int last_orientation = getScreenState();
	
	private int getScreenState(){
		return getResources().getConfiguration().orientation;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation == last_orientation) return;
		last_orientation = newConfig.orientation;
		resetRecentsPanelViewOffset(false);
		initView();
		if(getScreenState() == Configuration.ORIENTATION_PORTRAIT){
			orientationPortraitLayout();
		} else {
			orientationLandLayout();
		}
		rotationChangeAnimation();
	}
	// this method needs modify
	private void rotationChangeAnimation(){
		ObjectAnimator animReset01 = null;
		ObjectAnimator animReset02 = null;

		if(isOrientationPortrait()){
			//((RecentsLayout)mRecentsMaskView).startScroll(HandlerBar.mRecentsScrimHeight, 0, 0, 0, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
			((RecentsLayout)mRecentsMaskView).startScroll(0, 0, 0, HandlerBar.mRecentsScrimHeight, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
	    	//animReset01 = ObjectAnimator.ofFloat(mQuickSetting, "translationX", quickSettingAnimEnd,0);
			//animReset02 = ObjectAnimator.ofFloat(mQuickSetting, "translationY", quickSettingAnimStart,quickSettingAnimEnd);
		} else {
			//((RecentsLayout)mRecentsMaskView).startScroll(0, HandlerBar.mRecentsScrimHeight, 0, 0, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
			((RecentsLayout)mRecentsMaskView).startScroll(0, 0, HandlerBar.mRecentsScrimHeight, 0, HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
	    	//animReset01 = ObjectAnimator.ofFloat(mQuickSetting, "translationY", quickSettingAnimEnd,0);
			//animReset02 = ObjectAnimator.ofFloat(mQuickSetting, "translationX", quickSettingAnimStart,quickSettingAnimEnd);
		}
		//animReset02.setDuration(QUIT_ANIMATION_DURATION);
		//AnimatorSet animSet = new AnimatorSet();
    	//animSet.play(animReset02);
		//animSet.start();
		autoQuickSettingEnterAnimation();
	}

	private void resetRecentsPanelViewOffset(boolean removePanel){
		if (animQuickSettingTranslation != null && animQuickSettingTranslation.isRunning()){
            animQuickSettingTranslation.cancel();
        }
        if (mEnterAnimSet != null && mEnterAnimSet.isRunning())
            mEnterAnimSet.cancel();		
		mQuickSetting.setTranslationY(0);		
		mQuickSetting.setTranslationX(0);

		mRecentsMaskView.scrollBy(-mRecentsMaskView.getScrollX(), -mRecentsMaskView.getScrollY());
	}

	private boolean isOrientationPortrait(){
		int orientation = getScreenState();
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}


	private int quickSettingWidth;
	private int quickSettingHeight;
	private int quickSettingWidthLand;
	private int quickSettingHeightLand;
	private int quickSettingMarginTop;
	private int quickSettingMarginLeft;
	private int quickSettingMarginTopLand;
	private int quickSettingMarginLeftLand;

	private int recentHeight;
	private int recentWidthLand;


	private void initQuickSettingsSize(){
		quickSettingWidth = getResources().getDimensionPixelSize(R.dimen.quick_settings_width);
		quickSettingHeight = getResources().getDimensionPixelSize(R.dimen.quick_settings_height);
		quickSettingWidthLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_width_land);
		quickSettingHeightLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_height_land);
		quickSettingMarginTop = getResources().getDimensionPixelSize(R.dimen.quick_settings_margintop);
		quickSettingMarginLeft = getResources().getDimensionPixelSize(R.dimen.quick_settings_marginleft);
		quickSettingMarginTopLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_margintop_land);
		quickSettingMarginLeftLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_marginleft_land);
	}

	private void initView(){
		if(quickSettingsView == null || recentPanelView == null){
			quickSettingsView = findViewById(R.id.quick_setting);
			recentPanelView = findViewById(R.id.upbgview);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		/*if(changed){
			initView();
			initQuickSettingsSize();
			if(getScreenState() == Configuration.ORIENTATION_PORTRAIT){
				orientationPortraitLayout();
			} else {
				orientationLandLayout();
			}
		}*/
	}

	private void orientationPortraitLayout(){

		FrameLayout.LayoutParams quickParam = (FrameLayout.LayoutParams)quickSettingsView.getLayoutParams();
		quickParam.width = quickSettingWidth;
		quickParam.height = quickSettingHeight;
		quickParam.setMargins(quickSettingMarginLeft, quickSettingMarginTop, 0, 0);
		quickParam.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

		FrameLayout.LayoutParams recentParam = (FrameLayout.LayoutParams)recentPanelView.getLayoutParams();
		recentParam.width = FrameLayout.LayoutParams.MATCH_PARENT;
		recentParam.height = FrameLayout.LayoutParams.MATCH_PARENT;
		recentParam.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

		quickSettingsView.setLayoutParams(quickParam);
		recentPanelView.setLayoutParams(recentParam);

	}

	private void orientationLandLayout(){
		FrameLayout.LayoutParams quickParam = (FrameLayout.LayoutParams)quickSettingsView.getLayoutParams();
		quickParam.width = quickSettingWidthLand;
		quickParam.height = quickSettingHeightLand;
		quickParam.setMargins(quickSettingMarginLeftLand, quickSettingMarginTopLand, 0, 0);
		quickParam.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

		FrameLayout.LayoutParams recentParam = (FrameLayout.LayoutParams)recentPanelView.getLayoutParams();
		recentParam.width = FrameLayout.LayoutParams.MATCH_PARENT;
		recentParam.height = FrameLayout.LayoutParams.MATCH_PARENT;
		recentParam.gravity = Gravity.TOP | Gravity.RIGHT;

		quickSettingsView.setLayoutParams(quickParam);
		recentPanelView.setLayoutParams(recentParam);
	}
	// Aurora <Steve.Tang> 2015-01-19. supprt orientation land recent panel. end
	
    public int numItemsInOneScreenful() {
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                    = (RecentsScrollView) mRecentsContainer;
            return scrollView.numItemsInOneScreenful();
        }  else {
            throw new IllegalArgumentException("missing Recents[Horizontal]ScrollView");
        }
    }

    private boolean pointInside(int x, int y, View v) {
        final int l = v.getLeft();
        final int r = v.getRight();
        final int t = v.getTop();
        final int b = v.getBottom();
        return x >= l && x < r && y >= t && y < b;
    }

    public boolean isInContentArea(int x, int y) {
        if (pointInside(x, y, mRecentsContainer)
			// Aurora <zhanggp> <2013-10-17> added for systemui begin
			|| pointInside(x, y, mRecentsNoApps)
			|| pointInside(x, y, mQuickSetting) 
			// Aurora <zhanggp> <2013-10-17> added for systemui end
												){
            return true;
        } else if (mStatusBarTouchProxy != null &&
                pointInside(x, y, mStatusBarTouchProxy)) {
            return true;
        } else {
            return false;
        }
    }

    public void show(boolean show) {
    	
        show(show, null, false, false);
    }

    public void show(boolean show, ArrayList<TaskDescription> recentTaskDescriptions,
            boolean firstScreenful, boolean waitingForWindowAnimation) {
    	
        mWaitingForWindowAnimation = waitingForWindowAnimation;
        if (show) {
            // Aurora <Felix.Duan> <2014-8-22> <BEGIN> Fix BUG #7822. Update string resource
            updateResources();
            // Aurora <Felix.Duan> <2014-8-22> <END> Fix BUG #7822. Update string resource
            // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
            // Immediatly set NaviBar bg to black
            setNaviBarColorBlack(true);
            // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent

            mWaitingToShow = true;
            // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
            //refreshRecentTasksList(recentTaskDescriptions, firstScreenful);
            // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
            showIfReady();
            // Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view begin
//            enterAnimation();
            // Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view end
        } else {
        	
        	// Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view begin
//        	exitAnimation();
        	// Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view end
            showImpl(false);
        }
    }

    private void showIfReady() {
        // mWaitingToShow => there was a touch up on the recents button
        // mRecentTaskDescriptions != null => we've created views for the first screenful of items
        if (mWaitingToShow && mRecentTaskDescriptions != null) {
            showImpl(true);
        }
    }

    static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    private void showImpl(boolean show) {
//        sendCloseSystemWindows(mContext, BaseStatusBar.SYSTEM_DIALOG_REASON_RECENT_APPS);
    	
        mShowing = show;

        if (show) {
            // if there are no apps, bring up a "No recent apps" message
            boolean noApps = mRecentTaskDescriptions != null
                    && (mRecentTaskDescriptions.size() == 0);

            int visibility = getSystemUiVisibility();
            // Aurora <Felix.Duan> <2014-8-7> <BEGIN> Low_Profile on recents
            // Not good enough
            //// Aurora <Felix.Duan> <2014-8-6> <BEGIN> Low_Profile on recents
            //setSystemUiVisibility(visibility|View.SYSTEM_UI_FLAG_LOW_PROFILE);
            //// Aurora <Felix.Duan> <2014-8-6> <END> Low_Profile on recents
            // Aurora <Felix.Duan> <2014-8-7> <END> Low_Profile on recents

			// Aurora <zhanggp> <2013-10-17> modified for systemui begin
            /// M: Support theme for ThemeManager @{
            /*
            final TextView noRecentTextView = (TextView) mRecentsNoApps
                    .findViewById(R.id.recents_no_apps_text);
            Xlog.d(TAG, "show: mRecentsNoApps = " + mRecentsNoApps
                    + ",noRecentTextView = " + noRecentTextView);
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                final int themeMainColor = mContext.getResources().getThemeMainColor();
                if (themeMainColor != 0) {
                    noRecentTextView.setTextColor(themeMainColor);
                }
            }
            */
            /// M: Support theme for ThemeManager @}
			// Aurora <zhanggp> <2013-10-17> modified for systemui end
            mRecentsNoApps.setAlpha(1f);
            // Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view begin
//            if(noApps){
//            	Animation animationRecentsNoApps = AnimationUtils.loadAnimation(mContext,
//        				R.anim.aurora_recents_enter_buttom);
//                mRecentsNoApps.startAnimation(animationRecentsNoApps);
//            	mRecentsNoApps.setVisibility(View.VISIBLE);
//            }

            // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel 
            // Update no app text
            final TextView noRecentTextView = (TextView) mRecentsNoApps
                    .findViewById(R.id.no_app_label);
            noRecentTextView.setVisibility(noApps ? View.VISIBLE : View.INVISIBLE);
            // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel 
            // Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view end

//            onAnimationEnd(null);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        } else {
            mWaitingToShow = false;
            // call onAnimationEnd() and clearRecentTasksList() in onUiHidden()
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    }

    public void onUiHidden() {
        if (!mShowing && mRecentTaskDescriptions != null) {
        	
//            onAnimationEnd(null);
            // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
            //clearRecentTasksList();
            // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
        }

        // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
        Intent intent= new Intent(HandlerBar.ENABLE_HANDLER);
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        // Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.

      /// M: [ALPS00557795] Dismiss popup window on ui hidden condition.
        //Gionee: <guozj><2013-4-18> add for CR00793539 begin
        if (mPopup != null) {
            mPopup.dismiss();
        }
        //Gionee: <guozj><2013-4-18> add for CR00793539 end

        // Aurora <Felix.Duan> <2014-4-25> <BEGIN> Fix BUG #4462 on SystemUI side, cooperate with Settings
        // Broadcast we are hidden, good time for Settings to update it`s data.
        mContext.sendBroadcastAsUser(new Intent(RECENTS_PANEL_HIDDEN),
            new UserHandle(UserHandle.USER_CURRENT));
        // Aurora <Felix.Duan> <2014-4-25> <END> Fix BUG #4462 on SystemUI side, cooperate with Settings
    }

    public void dismiss() {
//        ((RecentsActivity) mContext).dismissAndGoHome();
    	
    	removeRecentsPanelView();
    }

    public void dismissAndGoBack() {
//        ((RecentsActivity) mContext).dismissAndGoBack();
    	
    	removeRecentsPanelView();
    }

    public void onAnimationCancel(Animator animation) {
        Log.d("felix","RecentsPanelView.DEBUG onAnimationCancel()");
    }

    public void onAnimationEnd(Animator animation) {
        Log.d("felix","RecentsPanelView.DEBUG onAnimationEnd()");
        if (mShowing) {
            final LayoutTransition transitioner = new LayoutTransition();
            ((ViewGroup)mRecentsContainer).setLayoutTransition(transitioner);
            createCustomAnimations(transitioner);
        } else {
            ((ViewGroup)mRecentsContainer).setLayoutTransition(null);
        }
    }

    public void onAnimationRepeat(Animator animation) {
        Log.d("felix","RecentsPanelView.DEBUG onAnimationRepeat()");
    }

    public void onAnimationStart(Animator animation) {
        Log.d("felix","RecentsPanelView.DEBUG onAnimationStart()");
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        // Ignore hover events outside of this panel bounds since such events
        // generate spurious accessibility events with the panel content when
        // tapping outside of it, thus confusing the user.
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }

    /**
     * Whether the panel is showing, or, if it's animating, whether it will be
     * when the animation is done.
     */
    public boolean isShowing() {
        return mShowing;
    }

    public void setStatusBarView(View statusBarView) {
        if (mStatusBarTouchProxy != null) {
            mStatusBarTouchProxy.setStatusBar(statusBarView);
        }
    }

    public void setRecentTasksLoader(RecentTasksLoader loader) {
        mRecentTasksLoader = loader;
    }

    public void updateValuesFromResources() {
        final Resources res = mContext.getResources();
        mThumbnailWidth = Math.round(res.getDimension(R.dimen.status_bar_recents_thumbnail_width));
        mFitThumbnailToXY = res.getBoolean(R.bool.config_recents_thumbnail_image_fits_to_xy);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecentsContainer = (ViewGroup) findViewById(R.id.recents_container);
        mStatusBarTouchProxy = (StatusBarTouchProxy) findViewById(R.id.status_bar_touch_proxy);
        mListAdapter = new TaskDescriptionAdapter(mContext);
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                    = (RecentsScrollView) mRecentsContainer;
            scrollView.setAdapter(mListAdapter);
            scrollView.setCallback(this);
        } else {
            throw new IllegalArgumentException("missing Recents[Horizontal]ScrollView");
        }

        mRecentsScrim = findViewById(R.id.recents_bg_protect);
        mRecentsNoApps = findViewById(R.id.recents_no_apps);
        // Aurora <Felix.Duan> <2014-8-22> <BEGIN> Fix BUG #7822. Update string resource
        mNoAppLabel = (TextView) findViewById(R.id.no_app_label);
        // Aurora <Felix.Duan> <2014-8-22> <END> Fix BUG #7822. Update string resource
        mRecentsMaskView = findViewById(R.id.upbgview);
        if (mRecentsScrim != null) {
            mHighEndGfx = ActivityManager.isHighEndGfx();
            if (!mHighEndGfx) {
                mRecentsScrim.setBackground(null);
            }
			// Aurora <zhanggp> <2013-10-17> modified for systemui begin
			/*
			else if (mRecentsScrim.getBackground() instanceof BitmapDrawable) {
                // In order to save space, we make the background texture repeat in the Y direction
                ((BitmapDrawable) mRecentsScrim.getBackground()).setTileModeY(TileMode.REPEAT);
            }
			*/
			// Aurora <zhanggp> <2013-10-17> modified for systemui end
        }
		
		// Aurora <zhanggp> <2013-10-17> added for systemui begin
		mQuickSetting = findViewById(R.id.quick_setting);
		AuroraPageClear clearView = (AuroraPageClear) LayoutInflater.from(mContext).inflate(R.layout.aurora_recent_clearview, null);

        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel 
		((AuroraRecentPage) mRecentsContainer)
            .addView(clearView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel 
		// Aurora <zhanggp> <2013-10-17> added for systemui end
    }

    public void setMinSwipeAlpha(float minAlpha) {
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                = (RecentsScrollView) mRecentsContainer;
            scrollView.setMinSwipeAlpha(minAlpha);
        }
    }

    private void createCustomAnimations(LayoutTransition transitioner) {
        transitioner.setDuration(200);
        transitioner.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, null);
    }

    private void updateIcon(ViewHolder h, Drawable icon, boolean show, boolean anim) {
        if (icon != null) {
            h.iconView.setImageDrawable(icon);
            if (show && h.iconView.getVisibility() != View.VISIBLE) {
                if (anim) {
                    h.iconView.setAnimation(
                            AnimationUtils.loadAnimation(mContext, R.anim.recent_appear));
                }
                h.iconView.setVisibility(View.VISIBLE);
            }
        }
    }
	// Aurora <zhanggp> <2013-10-17> modified for systemui begin
	/*
    private void updateThumbnail(ViewHolder h, Bitmap thumbnail, boolean show, boolean anim) {
        if (thumbnail != null) {
            // Should remove the default image in the frame
            // that this now covers, to improve scrolling speed.
            // That can't be done until the anim is complete though.
            h.thumbnailViewImage.setImageBitmap(thumbnail);

            // scale the image to fill the full width of the ImageView. do this only if
            // we haven't set a bitmap before, or if the bitmap size has changed
            if (h.thumbnailViewImageBitmap == null ||
                h.thumbnailViewImageBitmap.getWidth() != thumbnail.getWidth() ||
                h.thumbnailViewImageBitmap.getHeight() != thumbnail.getHeight()) {
                if (mFitThumbnailToXY) {
                    h.thumbnailViewImage.setScaleType(ScaleType.FIT_XY);
                } else {
                    Matrix scaleMatrix = new Matrix();
                    float scale = mThumbnailWidth / (float) thumbnail.getWidth();
                    scaleMatrix.setScale(scale, scale);
                    h.thumbnailViewImage.setScaleType(ScaleType.MATRIX);
                    h.thumbnailViewImage.setImageMatrix(scaleMatrix);
                }
            }
            if (show && h.thumbnailView.getVisibility() != View.VISIBLE) {
                if (anim) {
                    h.thumbnailView.setAnimation(
                            AnimationUtils.loadAnimation(mContext, R.anim.recent_appear));
                }
                h.thumbnailView.setVisibility(View.VISIBLE);
            }
            h.thumbnailViewImageBitmap = thumbnail;
        }
    }

    void onTaskThumbnailLoaded(TaskDescription td) {
  
        synchronized (td) {
            if (mRecentsContainer != null) {
                ViewGroup container = mRecentsContainer;
                if (container instanceof RecentsScrollView) {
                    container = (ViewGroup) container.findViewById(
                            R.id.recents_linear_layout);
                }
                // Look for a view showing this thumbnail, to update.
                for (int i=0; i < container.getChildCount(); i++) {
                    View v = container.getChildAt(i);
                    if (v.getTag() instanceof ViewHolder) {
                        ViewHolder h = (ViewHolder)v.getTag();
                        if (!h.loadedThumbnailAndIcon && h.taskDescription == td) {
                            // only fade in the thumbnail if recents is already visible-- we
                            // show it immediately otherwise
                            //boolean animateShow = mShowing &&
                            //    mRecentsContainer.getAlpha() > ViewConfiguration.ALPHA_THRESHOLD;
                            boolean animateShow = false;
                            updateIcon(h, td.getIcon(), true, animateShow);
                            //updateThumbnail(h, td.getThumbnail(), true, animateShow);
                            h.loadedThumbnailAndIcon = true;
                            //mNumItemsWaitingForThumbnailsAndIcons--;
                        }
                    }
                }
            }
        }
        
        showIfReady();
    }
	*/
	// Aurora <zhanggp> <2013-10-17> modified for systemui end
    public void onWindowAnimationStart() {
        if (mItemToAnimateInWhenWindowAnimationIsFinished != null) {
            final int startDelay = 150;
            final int duration = 250;
            final ViewHolder holder = mItemToAnimateInWhenWindowAnimationIsFinished;
            final TimeInterpolator cubic = new DecelerateInterpolator(1.5f);
            for (View v :
				// Aurora <zhanggp> <2013-10-17> modified for systemui begin
                new View[] { holder.iconView, holder.labelView }) {
				//new View[] { holder.iconView, holder.labelView, holder.calloutLine }) {
				// Aurora <zhanggp> <2013-10-17> modified for systemui end
                if (v != null) {
                    v.animate().translationX(0).translationY(0).alpha(1f).setStartDelay(startDelay)
                            .setDuration(duration).setInterpolator(cubic);
                }
            }
            mItemToAnimateInWhenWindowAnimationIsFinished = null;
            mWaitingForWindowAnimation = false;
        }
    }

    public void clearRecentTasksList() {
        // Clear memory used by screenshots
        if (mRecentTaskDescriptions != null) {
            mRecentTasksLoader.cancelLoadingThumbnailsAndIcons(this);
            onTaskLoadingCancelled();
        }
    }

    public void onTaskLoadingCancelled() {
        // Gets called by RecentTasksLoader when it's cancelled

        if (mRecentTaskDescriptions != null) {
            mRecentTaskDescriptions = null;
            mListAdapter.notifyDataSetInvalidated();
        }

    }
    
    public void refreshViewIcons(){
    	Log.v("iht-ssui","+++++++++++++++++++++++++++++++++++++++++icons_update");
    	mListAdapter.notifyDataSetInvalidated();
    }
    

    public void refreshViews() {
        Log.d("felix","RecentsPanelView.DEBUG refreshViews()");
        mListAdapter.notifyDataSetInvalidated();
        updateUiElements();
        showIfReady();
    }

    /// M: [SystemUI][ALPS00444338]When a call come in, will clear recent tasks, but when resume, doesn't reload tasks. @{
    public void refreshRecentTasks() {
        if (mRecentTaskDescriptions == null) {
            refreshRecentTasksList();
        }
    }
    /// M: [SystemUI][ALPS00444338]When a call come in, will clear recent tasks, but when resume, doesn't reload tasks. @}

    public void refreshRecentTasksList() {
        refreshRecentTasksList(null, false);
    }

    private void refreshRecentTasksList(
            ArrayList<TaskDescription> recentTasksList, boolean firstScreenful) {
        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
        //if (mRecentTaskDescriptions == null && recentTasksList != null) {
        if (mRecentTaskDescriptions != null) {
        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
            onTasksLoaded(recentTasksList, firstScreenful);
        } else {
            mRecentTasksLoader.loadTasksInBackground();
        }
    }

    public void onTasksLoaded(ArrayList<TaskDescription> tasks, boolean firstScreenful) {
        Log.d("felix", "RecentsPanelView.DEBUG onTasksLoaded()  tasks.size() = " + tasks.size()
                + "  firstScreenful = " + firstScreenful);
        // Aurora <Felix.Duan> <2014-5-15> <BEGIN> Fix BUG #4694. Duplicate task icon.
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
        //mNumItemsWaitingForThumbnailsAndIcons = firstScreenful
        //        ? tasks.size() : mRecentTaskDescriptions == null
        //                ? 0 : mRecentTaskDescriptions.size();
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
        if (mRecentTaskDescriptions == null) {
            mRecentTaskDescriptions = new ArrayList<TaskDescription>(tasks);
        } else {
            Log.d("felix", "RecentsPanelView.DEBUG onTasksLoaded() empty mRecentTaskDescriptions first !");
            mRecentTaskDescriptions.clear();
            mRecentTaskDescriptions.addAll(tasks);
        }
        if (mRecentTaskDescriptions.size()!=0)
            for(TaskDescription task: mRecentTaskDescriptions) {
                Log.d("felix", "RecentsPanelView.DEBUG  mRecentTaskDescriptions.packageName = " + task.packageName);
            }
        // Aurora <Felix.Duan> <2014-5-15> <END> Fix BUG #4694. Duplicate task icon.
//        if (((RecentsActivity) mContext).isActivityShowing()) {
//        if (isShowing()){
            refreshViews();
//        }
        
    }

    private void updateUiElements() {
        final int items = mRecentTaskDescriptions != null
                ? mRecentTaskDescriptions.size() : 0;

        mRecentsContainer.setVisibility(items > 0 ? View.VISIBLE : View.GONE);

        // Set description for accessibility
        int numRecentApps = mRecentTaskDescriptions != null
                ? mRecentTaskDescriptions.size() : 0;
        String recentAppsAccessibilityDescription;
        if (numRecentApps == 0) {
            recentAppsAccessibilityDescription =
                getResources().getString(R.string.status_bar_no_recent_apps);
        } else {
            recentAppsAccessibilityDescription = getResources().getQuantityString(
                R.plurals.status_bar_accessibility_recent_apps, numRecentApps, numRecentApps);
        }
        setContentDescription(recentAppsAccessibilityDescription);
    }

    public boolean simulateClick(int persistentTaskId) {
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                = (RecentsScrollView) mRecentsContainer;
            View v = scrollView.findViewForTask(persistentTaskId);
            if (v != null) {
                handleOnClick(v);
                return true;
            }
        }
        return false;
    }

    public void handleOnClick(View view) {
    	
        ViewHolder holder = (ViewHolder)view.getTag();
        TaskDescription ad = holder.taskDescription;
        final Context context = view.getContext();
        final ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
		Bundle opts = null;
		/*
        Bitmap bm = holder.thumbnailViewImageBitmap;
        boolean usingDrawingCache;
        if (bm.getWidth() == holder.thumbnailViewImage.getWidth() &&
                bm.getHeight() == holder.thumbnailViewImage.getHeight()) {
            usingDrawingCache = false;
        } else {
            holder.thumbnailViewImage.setDrawingCacheEnabled(true);
            bm = holder.thumbnailViewImage.getDrawingCache();
            usingDrawingCache = true;
        }
        Bundle opts = (bm == null) ?
                null :
                ActivityOptions.makeThumbnailScaleUpAnimation(
                        holder.thumbnailViewImage, bm, 0, 0, null).toBundle();
	*/
	 
	 // Aurora <zhanggp> <2013-10-17> modified for systemui end
		
        show(false);
        if (ad.taskId >= 0) {
            // This is an active task; it should just go to the foreground.
            am.moveTaskToFront(ad.taskId, ActivityManager.MOVE_TASK_WITH_HOME,
                    opts);
            removeRecentsPanelView();
        } else {
            Intent intent = ad.intent;
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                    | Intent.FLAG_ACTIVITY_TASK_ON_HOME
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (DEBUG) Log.v(TAG, "Starting activity " + intent);
            context.startActivityAsUser(intent, opts,
                    new UserHandle(UserHandle.USER_CURRENT));
            removeRecentsPanelView();
        }
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
		/*
        if (usingDrawingCache) {
            holder.thumbnailViewImage.setDrawingCacheEnabled(false);
        }
        */
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        handleOnClick(view);
    }

    public void handleSwipe(View view) {
        final TaskDescription ad = ((ViewHolder) view.getTag()).taskDescription;
        if (ad == null) {
            Log.v(TAG, "Not able to find activity description for swiped task; view=" + view +
                    " tag=" + view.getTag());
            return;
        }

        // Aurora <Felix.Duan> <2014-9-18> <BEGIN> Add animation of swipe up icon on recents panel 
        // Clone a item view to perform deletion animation
        if (view instanceof AuroraRecentlItemView
            && ((AuroraRecentlItemView)view).isSingleSwipe()) {
            AuroraRecentlItemView clone = ((AuroraRecentlItemView)view).clone();
            addView(clone, new LayoutParams(view.getWidth(), view.getHeight()));
            clone.buildAnim().start();
        }
        // Aurora <Felix.Duan> <2014-9-18> <END> Add animation of swipe up icon on recents panel 

        if (DEBUG) Log.v(TAG, "Jettison " + ad.getLabel());
        if(mRecentTaskDescriptions == null){
        	return;
        }
        mRecentTaskDescriptions.remove(ad);

        // Handled by widget containers to enable LayoutTransitions properly
        // mListAdapter.notifyDataSetChanged();

        if (mRecentTaskDescriptions.size() == 0) {
            dismissAndGoBack();
        }

        new Thread( new Runnable() { //tymy_20150514_bug13118    
            public void run() {
        // Currently, either direction means the same thing, so ignore direction and remove
        // the task.
        final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.removeTask(ad.persistentTaskId, ActivityManager.REMOVE_TASK_KILL_PROCESS);

            // Aurora <Felix.Duan> <2014-9-3> <BEGIN> Reference app kill policy from auto-start
            ////Aurora <tongyh> <2014-01-13>  delete the process and service begin
            //  Log.d("0403", "ad.packageName = " + ad.packageName);
            //  try {
            //  	if(("com.android.music".equals(ad.packageName)) || 
            //  	("com.android.contacts".equals(ad.packageName)) || 
            //  	("com.android.mms".equals(ad.packageName)) || 
            //  	("com.sec.android.app.videoplayer".equals(ad.packageName)) || 
            //  	("com.samsung.everglades.video".equals(ad.packageName)) || 
            //  	("com.sec.android.app.music".equals(ad.packageName))){
            //  		am.forceStopPackage(ad.packageName);
            //  	}
            //  	if(!isThisASystemPackage(ad.packageName)){
            //  		String label = ad.getLabel().toString().toLowerCase();
            //  		for(int i = 0; i < items.length; i++){
            //  			if(label != null){
            //  				if(label.contains(items[i].toString().toLowerCase())){
            //  					am.forceStopPackage(ad.packageName);
            //  					break;
            //  				};
            //  			}
            //  		}
            //  		label = null;
            //  	}
		    //  } catch (Exception e) {
		    //  	e.printStackTrace();
		    //  }
            ////Aurora <tongyh> <2014-01-13> delete the process and service  end

            if (mRemoveTaskPolicy.shouldForceStopPackage(ad)) {
                Log.d(TAG, "force stop package " + ad.packageName);
                am.forceStopPackage(ad.packageName);
            }
            // Aurora <Felix.Duan> <2014-9-3> <END> Reference app kill policy from auto-start

            // Accessibility feedback
            setContentDescription(
                    mContext.getString(R.string.accessibility_recents_item_dismissed, ad.getLabel()));
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            setContentDescription(null);
            
            Thread.currentThread().interrupt();
        }
        }            
        }).start();
    }

    private void startApplicationDetailsActivity(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setComponent(intent.resolveActivity(mContext.getPackageManager()));
        TaskStackBuilder.create(getContext())
                .addNextIntentWithParentStack(intent).startActivities();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if (mPopup != null){
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public void handleLongPress(
            final View selectedView, final View anchorView, final View thumbnailView) {
        thumbnailView.setSelected(true);
        final PopupMenu popup =
            new PopupMenu(mContext, anchorView == null ? selectedView : anchorView);
        mPopup = popup;
        popup.getMenuInflater().inflate(R.menu.recent_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.recent_remove_item) {
                    mRecentsContainer.removeViewInLayout(selectedView);
                } else if (item.getItemId() == R.id.recent_inspect_item) {
                    ViewHolder viewHolder = (ViewHolder) selectedView.getTag();
                    if (viewHolder != null) {
                        final TaskDescription ad = viewHolder.taskDescription;
                        startApplicationDetailsActivity(ad.packageName);
                        
                        show(false);
                    } else {
                        throw new IllegalStateException("Oops, no tag on view " + selectedView);
                    }
                } else {
                    return false;
                }
                return true;
            }
        });
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            public void onDismiss(PopupMenu menu) {
                thumbnailView.setSelected(false);
                mPopup = null;
            }
        });
        popup.show();
    }
	
	// Aurora <zhanggp> <2013-11-05> added for systemui begin
	public boolean isRecentActivityShowing(){
		if(null == mContext){
			return false;
		}
//		return ((RecentsActivity) mContext).isActivityShowing();
		return isShowing();
	}
	// Aurora <zhanggp> <2013-11-05> added for systemui end
	
    // Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view begin
    /*public void enterAnimation(){
    	Animation animationQuickSetting = AnimationUtils.loadAnimation(mContext,
				R.anim.aurora_recents_enter_top);
    	mQuickSetting.startAnimation(animationQuickSetting);
    	mQuickSetting.setVisibility(View.VISIBLE);
    	
    	Animation animationRecentsContainer = AnimationUtils.loadAnimation(mContext,
				R.anim.aurora_recents_enter_buttom);
    	if((mRecentTaskDescriptions != null
                && mRecentTaskDescriptions.size() == 0)){
    		mRecentsNoApps.startAnimation(animationRecentsContainer);
    		mRecentsNoApps.setVisibility(View.VISIBLE);
    	}else{
    		mRecentsContainer.startAnimation(animationRecentsContainer);
        	mRecentsContainer.setVisibility(View.VISIBLE);
    	}
    	
    }*/
    
//    public void exitAnimation(){
//    	if(((RecentsActivity) mContext).getFlag()){
//    		return;
//    	}
//    	Animation animationQuickSetting = AnimationUtils.loadAnimation(mContext,
//				R.anim.aurora_recents_exit_top);
//    	mQuickSetting.startAnimation(animationQuickSetting);
//    	mQuickSetting.setVisibility(View.GONE);
//    	
//    	Animation animationRecentsContainer = AnimationUtils.loadAnimation(mContext,
//				R.anim.aurora_recents_exit_buttom);
//    	
//    	if(mRecentTaskDescriptions != null
//                && (mRecentTaskDescriptions.size() == 0)){
//    		mRecentsNoApps.startAnimation(animationRecentsContainer);
//    		mRecentsNoApps.setVisibility(View.GONE);
//    	}else{
//    		mRecentsContainer.startAnimation(animationRecentsContainer);
//        	mRecentsContainer.setVisibility(View.GONE);
//    	}
//    	((RecentsActivity) mContext).setFlag(true);
//    	handler.postDelayed(finishRecentsActivity, 250);
//    	
//    }
    
    private Runnable finishRecentsActivity = new Runnable(){

	    @Override
		public void run() {
	    	//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview begin
//			((RecentsActivity) mContext).finishRecentsActivity();

	    	removeRecentsPanelView();
	    	//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview end
		}
  	
    };
    private Handler handler = new Handler();
    
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	if(inRangeOfView(mQuickSetting,event)){
    		return true;
    	}
    	
    	if (event.getAction() ==  MotionEvent.ACTION_UP){
    		
    	   removeRecentsPanelView();
    	}
		return true;
	}
    
    

    private boolean inRangeOfView(View view, MotionEvent ev){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if(ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())){
            return false;
        }
        return true;
    }

    
    // Aurora <tongyh> <2013-11-04> add animation to QuickSetting's and RecentsNoApps's and RecentsContainer's view end
    
       @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
        	    if (event.getAction() == KeyEvent.ACTION_UP) {
                    // Aurora <Felix.Duan> <2014-6-20> <BEGIN> Don`t perform canceled BACK event
                    // Cause pull up from BACK key fail
                    if (!event.isCanceled()) {
                        removeRecentsPanelView();
                    }
                    // Aurora <Felix.Duan> <2014-6-20> <END> Don`t perform canceled BACK event
        	    }
        	    break;
            
        }
        return super.dispatchKeyEvent(event);
    }

    public void removeRecentsPanelView(){
        // Aurora <Felix.Duan> <2014-10-14> <BEGIN> Fix BUG #8655.
        if (mEnterAnimSet.isRunning())
            mEnterAnimSet.cancel();
        // Aurora <Felix.Duan> <2014-10-14> <END> Fix BUG #8655.
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
        Log.d("felixx", "removeRecentsPanelView() 1 mAnimating = " + mAnimating);
        if (mAnimating) return;
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
        // Aurora <Felix.Duan> <2014-6-18> <BEGIN> Support NaviBar full mode change from intent
        // Unset NaviBar bg to android standard

        // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
        /*
        Intent intent = new Intent (PhoneStatusBarPolicy.ACTION_SET_NAVIBAR_COLOR);
        intent.putExtra("mode", -1);
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        */
        // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
        // Aurora <Felix.Duan> <2014-6-18> <END> Support NaviBar full mode change from intent
         Log.d("felix","RecentsPanelView.DEBUG removeRecentsPanelView() isScreenOff = " + isScreenOff);
         // Aurora <Felix.Duan> <2014-4-22> <BEGIN> Fix BUG #4470. Move IO task off UI thread.
         // Here we need more cpu resource for animation, move this save task to other place. 
 		 //AuroraRecentsClearUtils.saveLockFlag(mContext);
         // Aurora <Felix.Duan> <2014-4-22> <END> Fix BUG #4470. Move IO task off UI thread.
//    	 mHandlerBarCallback.removeBar();
//    	 mHandlerBarCallback.addBar();
    	 if(isScreenOff){
 			scrollToOriginalLocationNoAnimation();
 			setRecentsMaskViewAlpha();
			((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
			mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(0);
			if(mAuroraRubbishView == null){
				mAuroraRubbishView = (AuroraRubbishView)findViewById(R.id.clear_all);
			}
			mAuroraRubbishView.rest();
			mHandlerBarCallback.removeRecentPanelView();
			isScreenOff = false;
    	 }else{
    		 scrollToOriginalLocation();
        	 autoQuickSettingQuitAnimation();
    	 }
         // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
         //// Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
    	 // ((AuroraRecentPage) mRecentsContainer).removeChildPages();
         //// Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
         // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration
//    	 snap(false);
    	//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  begin
	     show(false);
         onUiHidden();
//         isResponseHomeKey = false;
//         mHandlerBarCallback.removeRecentPanelView();
         BaseStatusBar.isCanSetStatusBarViewBg = false;
    	//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  end
     }
     
    public void removeRecentsPanelView(boolean isShowHandlerBar){
        // Aurora <Felix.Duan> <2014-10-14> <BEGIN> Fix BUG #8655.
        if (mEnterAnimSet.isRunning())
            mEnterAnimSet.cancel();
        // Aurora <Felix.Duan> <2014-10-14> <END> Fix BUG #8655.
         // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
        Log.d("felixx", "removeRecentsPanelView() 2 mAnimating = " + mAnimating);
         if (mAnimating) return;
         // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
         Log.d("felix","RecentsPanelView.DEBUG removeRecentsPanelView() isShowHandlerBar = " + isShowHandlerBar);
         // Aurora <Felix.Duan> <2014-4-22> <BEGIN> Fix BUG #4470. Move IO task off UI thread.
         // Here we need more cpu resource for animation, move this save task to other place. 
    	 //AuroraRecentsClearUtils.saveLockFlag(mContext);
         // Aurora <Felix.Duan> <2014-4-22> <END> Fix BUG #4470. Move IO task off UI thread.
//    	 mHandlerBarCallback.removeBar();
//    	 mHandlerBarCallback.addBar();
    	 if(isScreenOff){
 			scrollToOriginalLocationNoAnimation();
 			setRecentsMaskViewAlpha();
			((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
			mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(0);
			if(mAuroraRubbishView == null){
				mAuroraRubbishView = (AuroraRubbishView)findViewById(R.id.clear_all);
			}
			mAuroraRubbishView.rest();
			mHandlerBarCallback.removeRecentPanelView(false);
			isScreenOff = false;
    	 }else{
    		 scrollToOriginalLocation();
        	 autoQuickSettingQuitAnimation(isShowHandlerBar);
    	 }
	     show(false);
         onUiHidden();
         BaseStatusBar.isCanSetStatusBarViewBg = false;
     }
     
/*     @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	 super.onLayout(changed, left, top, right, bottom);
    	 final int count = getChildCount();
    	 for (int i = 0; i < count; i++) {
             final View child = getChildAt(i);
             if (child.getVisibility() != View.GONE) {
                 if(child.getId() == R.id.quick_setting){
//            	     child.layout(0, -HandlerBar.mQuickSettingHeight-(HandlerBar.mRecentsScrimHeight-HandlerBar.mQuickSettingHeight)/density,phoneWidth, 0-(HandlerBar.mRecentsScrimHeight-HandlerBar.mQuickSettingHeight)/density);
//                	 child.layout(0, 0,phoneWidth, HandlerBar.mQuickSettingHeight);
                 }else if(child.getId() == R.id.recents_bg_protect){
            	     child.layout(0, phoneHeight, phoneWidth, phoneHeight+HandlerBar.mRecentsScrimHeight);
                 }
             }
    	 }
     }*/

    // Not used any more
    //public void quickRecentPanelYMoveUp(float y, float dy) {
 	//	int yy = (int) y;
 	//	int ddyy = (int) dy;
 	//	mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(blurredBackgroundAlphaChanged(Math.min(ddyy, 255)));
    //    // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 	//	setMaskAlpha(backgroundAlphaChanged(dy));
    //    // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 	//	mQuickSetting.setTranslationY(-Math.max(yy, -(259)));
 	//	mQuickSetting.setAlpha(distanChangeToAlpahZeroToOne(dy));
 	//	mRecentsScrim.setTranslationY(Math.max(yy, -(HandlerBar.mRecentsScrimHeight)));
 	//}

    // Not used any more
 	//public void quickRecentPanelYMoveDown(float y, float dy) {
 	//	int yy = (int) y;
 	//	int ddyy = (int) dy;
 	//	mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(blurredBackgroundAlphaChanged(Math.min(ddyy, 255)));
    //    // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 	//	setMaskAlpha(backgroundAlphaChanged(dy));
    //    // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 	//	mRecentsScrim.setTranslationY(Math.min(yy, (HandlerBar.mRecentsScrimHeight)));
 	//	mQuickSetting.setTranslationY(-Math.min(yy, 259));
 	//	mQuickSetting.setAlpha(distanChangeToAlpahZeroToOne(dy));
 	//}
 	
	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
 	public void quickRecentPanelZMove(int scrollZ) {
        Log.d("felix","RecentsPanelView.DEBUG quickRecentPanelZMove() scrollZ = " + scrollZ);

        // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
        // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
        if (((AuroraRecentPage) mRecentsContainer).isScrollable()) {
            if (BaseStatusBar.FELIXDBG)
                Log.d("felix", "RecentsPanelView quickRecentPanelZMove() setScrollable(false) 111");
            ((AuroraRecentPage) mRecentsContainer).setScrollable(false);
            handler.removeCallbacks(mDisableHandlerRunnable);
        }
        // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
        // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
 		mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(blurredBackgroundAlphaChanged(Math.min(scrollZ, 255)));
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 		setMaskAlpha(backgroundAlphaChanged(scrollZ));
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
        if (isOrientationPortrait()) 		
            mQuickSetting.setTranslationY(-Math.max(-scrollZ, -(quickSettingAnimEnd)));
        else
            mQuickSetting.setTranslationX(-Math.max(-scrollZ, -(quickSettingAnimEnd)));
	    mQuickSetting.setAlpha(distanChangeToAlpahZeroToOne(scrollZ));
 	}
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
 	
 	/*public void snap(final boolean openFlag) {
 		Log.d("wocao", "------------------openFlag = " +openFlag);
		float bottomY = 0f;
		float openTopY = 0f;
		float openDownY = 0f;
		//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  begin
		int animaTime = 350;
		if(!openFlag){
			animaTime = 300;
		}
		//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  end
		if (openFlag) {// open
			openTopY = HandlerBar.mQuickSettingHeight;
			openDownY = -(HandlerBar.mRecentsScrimHeight);
		} else {// close
			bottomY = 0;
		}
		ObjectAnimator animBottom = createTranslationAnimation(mRecentsScrim,
				openFlag ? openDownY : bottomY);
		//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  begin
//		int durationBottom = 200;
		animBottom.setDuration(animaTime);
		//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  end

//		animBottom.start();

//		ObjectAnimator animTop = createTranslationAnimation(mQuickSetting,
//				openFlag ? openTopY : bottomY);
//		int durationTop = 200;
//		animTop.setDuration(animaTime);
//		animTop.start();
		AnimatorSet animSet = new AnimatorSet();
//		animSet.play(animBottom).with(animTop);
		animSet.play(animBottom);
		animSet.start();
		if(!openFlag){
			autoQuickSettingQuitAnimation();
		}
		//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview  begin
		
		animBottom.addListener(new AnimatorListenerAdapter() {
	            public void onAnimationEnd(Animator animation) {
	            	//Aurora <tongyh> <2013-12-15> solve touch force close begin
	            	((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
	            	//Aurora <tongyh> <2013-12-15> solve touch force close end
	            	// Aurora <tongyh> <2013-12-13> in response to the home button to exit begin
	            	if(openFlag){
	            		Log.d("wocao", "------------------");
		            	isResponseHomeKey = true;
		            	((RecentsLayout)mRecentsMaskView).startScroll(0, mRecentsMaskView.getScrollY(), 0, HandlerBar.mRecentsScrimHeight, 1000);
	            	}else{
	            		Log.d("wocao", "+++++++++++++++++");
//	            		setRecentsQuickSettingViewAlpha();
//	            		setRecentsMaskViewAlpha();
	            	    isResponseHomeKey = false;
	            	 // Aurora <tongyh> <2013-12-13> in response to the home button to exit end
	            	 // Aurora <tongyh> <2013-12-13> set PhoneStatusBarView background transparent begin
	            	    BaseStatusBar.isCanSetStatusBarViewBg = false;
	            	 // Aurora <tongyh> <2013-12-13> set PhoneStatusBarView background transparent end
	            	    show(false);
	                    onUiHidden();
//	                    mHandlerBarCallback.removeRecentPanelView();
	            	}
//	            }
//	        });
		//Aurora <tongyh> <2013-12-09> add out animation for recentspanelview end
	}*/
 	
 	
 	private ObjectAnimator createTranslationAnimation(View v, float y) {
		ObjectAnimator anim = ObjectAnimator.ofFloat(v, "translationY", y);
		return anim;
	}
 	
 	//Aurora <tongyh> <2013-12-04> Monitor power button to shut down operations view  begin
 	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Log.d("felix","RecentsPanelView.DEBUG onReceive() " + action);
            
            //iht 更新图标
            if(action.equals(NOTIFY_APPS_TO_UPDATEIICON)){
            	mHandlerBarCallback.refreshViewIcons();
            }
            
        	if (Intent.ACTION_SCREEN_OFF.equals(action)) {
        		if(isShown()){
        			isScreenOff = true;
        			removeRecentsPanelView();
        		}
            }
        	// Aurora <tongyh> <2013-12-13> in response to the home button to exit begin
        	else if(Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)){
//            	if(isResponseHomeKey){
                // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                //if(mRecentsMaskView.getScrollY() == HandlerBar.mRecentsScrimHeight){
                boolean shown = isShown();
                Log.d("felix","RecentsPanelView.DEBUG onReceive() ACTION_CLOSE_SYSTEM_DIALOGS shown = " + shown);
                if(shown && !mAnimating) {
            		removeRecentsPanelView();
            	}
                // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            	isResponseHomeKey = false;
            }else if(Intent.ACTION_CONFIGURATION_CHANGED.equals(action)){
            	int orientation = getScreenState();
				if(last_orientation != orientation){
					dispatchConfigurationChanged(mContext.getResources().getConfiguration());
				}
                // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
                mHandlerBarCallback.updateOrientation(orientation);
                if (InvokerService.getInstance() != null) // Avoid NullPointer Exception when not using InvokerService
                    InvokerService.getInstance().mLandscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);
                // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out

/*
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                	mHandlerBarCallback.showHandlerBarView(true);
                }else{
                	if(isShown()){
						// Aurora <Steve.Tang> support land recent panel. start
                		//removeRecentsPanelView(false);
						// Aurora <Steve.Tang> support land recent panel. end
                	}else{
                		mHandlerBarCallback.showHandlerBarView(false);
                	}
//                	mHandlerBarCallback.showHandlerBarView(false);
                }*/
            }
        	//Aurora <tongyh> <2014-02-20> after opening the alarm handlerbar hidden begin
            else if("com.aurora.deskclock.startalarm".equals(action)){
//            	mHandlerBarCallback.showHandlerBarView(false);
            	if(isShown()){
            		removeRecentsPanelView(false);
            	}else{
            		mHandlerBarCallback.showHandlerBarView(false);
            	}
            }else if("com.aurora.deskclock.stopalarm".equals(action)){
            	mHandlerBarCallback.showHandlerBarView(true);
            // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Refactor：update IME state &  sechdule task loading
            //}else if("android.intent.action.ACTION_INPUT_METHOD_SHOW".equals(action)){
            //	isShowInputMethod = true;
            //    // mHandlerBarCallback.showHandlerBarView(false);
            //	if(isShown()){
            //		removeRecentsPanelView(false);
            //	}else{
            //		mHandlerBarCallback.showHandlerBarView(false);
            //	}
            //    if (InvokerService.getInstance() != null) // Avoid NullPointer Exception when not using InvokerService
            //        InvokerService.getInstance().updateInputMethodStatus(isShowInputMethod);
            //}else if("android.intent.action.ACTION_INPUT_METHOD_HIDE".equals(action)){
            //	if(isShowInputMethod){
            //		mHandlerBarCallback.showHandlerBarView(true);
            //		isShowInputMethod = false;
            //        if (InvokerService.getInstance() != null) // Avoid NullPointer Exception when not using InvokerService
            //            InvokerService.getInstance().updateInputMethodStatus(isShowInputMethod);
            //	} else {
            //    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
            //        if (!isShowing()) {
            //            handler.removeCallbacks(mLoadTaskRunnable);
            //            handler.postDelayed(mLoadTaskRunnable, 100);
            //        }
            //    }
            //    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
            // Aurora <Felix.Duan> <2014-8-8> <END> Refactor：update IME state &  sechdule task loading
            // Aurora <Felix.Duan> <2014-8-4> <BEGIN> Fix BUG #7009. Quit recents on voice command "Hello IUNI"
            } else if (VOICEWAKEUP_UNLOCK.equals(action)) {
                removeRecentsPanelView();
            } else if ("hhh.hhh.hhh.hhh".equals(action)){
				mQuickSetting.setTranslationY(0);		
				mQuickSetting.setTranslationX(0);
			}
            // Aurora <Felix.Duan> <2014-8-4> <END> Fix BUG #7009. Quit recents on voice command "Hello IUNI"
        	//Aurora <tongyh> <2014-02-20> after opening the alarm handlerbar hidden end
        	// Aurora <tongyh> <2013-12-13> in response to the home button to exit end
/*        	else if(CLOSE_RECENTSPAELVIEW.equals(intent.getAction())){
            	removeRecentsPanelView();
            }*/
//            else if(OPEN_RECENTSPAELVIEW.equals(intent.getAction())){
//            	if(!isShown()){
//            		mHandlerBarCallback.showRecentwsPanelView();
//            		snapForHomekey(true);
//            	}
//            }
        }
 	};
 	//Aurora <tongyh> <2013-12-04> Monitor power button to shut down operations view  end
 	
    // Aurora <Felix.Duan> <2014-8-4> <BEGIN> Reload tasks on package removed
 	private BroadcastReceiver mPackageMgrReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("felix","RecentsPanelView.DEBUG onReceive() " + action);
            //if (!isShowing()) {
                handler.removeCallbacks(mLoadTaskRunnable);
                handler.postDelayed(mLoadTaskRunnable, 100);
            //}
        }
    };
    // Aurora <Felix.Duan> <2014-8-4> <END> Reload tasks on package removed

    public void setRecentsMaskViewAlpha(){
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
	    setMaskAlpha(0);
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    }
    public void setRecentsMaskViewAlphaFull(){
//        mRecentsMaskView.getBackground().setAlpha(219);
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    	setMaskAlpha(MASK_VIEW_FULL_ALPHA);
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    }
    public void setRecentsQuickSettingViewAlpha(){
    	mQuickSetting.setAlpha(0);
    }
    
    public float distanChangeToAlpahZeroToOne(float frac){
        final int fullAlpha = 1;
        final int H = HandlerBar.mRecentsScrimHeight;
        frac = Math.min(frac, H);
        float alpha = 1f;
        alpha = fullAlpha * frac / H;
        if(alpha < 0){
        	alpha = 0;
        }
        return alpha;
    }
    
    public float distanChangeToAlpahOneToZero(float frac) {
        final int fullAlpha = 1;
        final int H = HandlerBar.mRecentsScrimHeight;
        frac = Math.min(frac, H);
        float alpha = 1f;
        alpha = fullAlpha - (fullAlpha * frac / H);
        if(alpha < 0){
        	alpha = 0;
        }
        return alpha;
    }
    
    public int backgroundAlphaChanged(float frac) {
        final int H = HandlerBar.mRecentsScrimHeight-60;
        frac = Math.min(frac, H);
        float alpha = 1f;
        alpha = MASK_VIEW_FULL_ALPHA * frac / H;
        if(alpha < 0){
        	alpha = 0;
        }
        return (int)alpha;
    }
    
    public int blurredBackgroundAlphaChanged(float frac) {
        final int fullAlpha = 255;
        final int H = HandlerBar.mRecentsScrimHeight-60;
        frac = Math.min(frac, H);
        float alpha = 1f;
        alpha = fullAlpha * frac / H;
        if(alpha < 0){
        	alpha = 0;
        }
        return (int)alpha;
    }

	private int quickSettingAnimStart = getResources().getDimensionPixelSize(R.dimen.quick_settings_scroll_start);
	private int quickSettingAnimEnd = getResources().getDimensionPixelSize(R.dimen.quick_settings_scroll_end); 

    public void autoQuickSettingEnterAnimation(){
        Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingEnterAnimation()");
//    	mRecentsMaskView.getBackground().setAlpha(backgroundAlphaChanged(dy));
// 		mQuickSetting.setTranslationY(-Math.max(yy, -(HandlerBar.mQuickSettingHeight)));
// 		mQuickSetting.setAlpha(distanChangeToAlpahZeroToOne(dy));
    	
//    	ObjectAnimator animRecentsMaskViewAlpha = ObjectAnimator.ofFloat(mRecentsMaskView, "alpha", 0.0f,1.0f);
//    	animRecentsMaskViewAlpha.setDuration(1000);
//    	mRecentsMaskView.getBackground().setAlpha(219);
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    	setMaskAlpha(MASK_VIEW_FULL_ALPHA);
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingTranslation != null && animQuickSettingTranslation.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingTranslation.cancel()");
            animQuickSettingTranslation.cancel();
        }

		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. start
		if(isOrientationPortrait()){
			animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationY", quickSettingAnimStart,quickSettingAnimEnd);
		} else {
	    	animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationX", quickSettingAnimStart,quickSettingAnimEnd);
		}
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. end
    	animQuickSettingTranslation.setDuration(HandlerBar.AUTO_ENTER_ANIMATION_DURATION);

        if (animQuickSettingAlpha != null && animQuickSettingAlpha.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingAlpha.cancel()");
            animQuickSettingAlpha.cancel();
        }
    	animQuickSettingAlpha = ObjectAnimator.ofFloat(mQuickSetting, "alpha", 0f,1f);
    	animQuickSettingAlpha.setDuration(HandlerBar.AUTO_ENTER_ANIMATION_DURATION);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
        // Aurora <Felix.Duan> <2014-10-14> <BEGIN> Fix BUG #8655.
    	mEnterAnimSet = new AnimatorSet();

        // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
        mEnterAnimSet.addListener(new AnimatorListenerAdapter() {
            boolean canceled;
            // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimating = true;
                canceled = false;
            }
            // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                canceled = true;
                mAnimating = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingEnterAnimation() onAnimationEnd()");
                if (BaseStatusBar.FELIXDBG)
                    Log.d("felix", "RecentsPanelView autoQuickSettingEnterAnimation() setScrollable(true) 222");
                ((AuroraRecentPage) mRecentsContainer).setScrollable(true);
				// Aurora <Steve.Tang> 2015-01-19 enable recentpage scrollable. start
                if (canceled) return;
				// Aurora <Steve.Tang> 2015-01-19 enable recentpage scrollable. end
				// Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
                handler.postDelayed(mDisableHandlerRunnable, 0);
				// Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.
                // Load other icons after scroll
                // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                //refreshViews();
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
                // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                mAnimating = false;
                // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            }
        });
        // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw

    	mEnterAnimSet.play(animQuickSettingTranslation);
//    	mEnterAnimSet.play(animRecentsMaskViewAlpha);
    	mEnterAnimSet.play(animQuickSettingAlpha);
    	mEnterAnimSet.start();
        // Aurora <Felix.Duan> <2014-10-14> <END> Fix BUG #8655.
    }
    
    
    public void continueAutoQuickSettingEnterAnimation(){
        Log.d("felix","RecentsPanelView.DEBUG continueAutoQuickSettingEnterAnimation()");
//    	mRecentsMaskView.getBackground().setAlpha(backgroundAlphaChanged(dy));
// 		mQuickSetting.setTranslationY(-Math.max(yy, -(HandlerBar.mQuickSettingHeight)));
// 		mQuickSetting.setAlpha(distanChangeToAlpahZeroToOne(dy));
    	
//    	ObjectAnimator animRecentsMaskViewAlpha = ObjectAnimator.ofFloat(mRecentsMaskView, "alpha", 0.0f,1.0f);
//    	animRecentsMaskViewAlpha.setDuration(1000);
//    	mRecentsMaskView.getBackground().setAlpha(219);
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    	setMaskAlpha(MASK_VIEW_FULL_ALPHA);
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.

        // Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
    	int translateZ = (int) ((isOrientationPortrait())
            ? mQuickSetting.getTranslationY()
            : mQuickSetting.getTranslationX());
    	if(translateZ <= quickSettingAnimStart){
    		translateZ = quickSettingAnimStart;
    	}else if(translateZ >= quickSettingAnimEnd){
    		translateZ = quickSettingAnimEnd;
    	}
        // Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
        // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingTranslation != null && animQuickSettingTranslation.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingTranslation.cancel()");
            animQuickSettingTranslation.cancel();
        }
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. start
		String orientation = "translationY";
		if(!isOrientationPortrait()){
			orientation = "translationX";
		}
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. end
    	animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, orientation, translateZ,quickSettingAnimEnd);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingTranslation.setDuration(HandlerBar.ANIMATION_CONTINUE_ENTER_DURATION);

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingAlpha != null && animQuickSettingAlpha.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingAlpha.cancel()");
            animQuickSettingAlpha.cancel();
        }
    	animQuickSettingAlpha = ObjectAnimator.ofFloat(mQuickSetting, "alpha", mQuickSetting.getAlpha(),1f);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingAlpha.setDuration(HandlerBar.ANIMATION_CONTINUE_ENTER_DURATION);
        // Aurora <Felix.Duan> <2014-10-14> <BEGIN> Fix BUG #8655.
    	mEnterAnimSet = new AnimatorSet();
    	mEnterAnimSet.play(animQuickSettingTranslation);
//    	mEnterAnimSet.play(animRecentsMaskViewAlpha);
    	mEnterAnimSet.play(animQuickSettingAlpha);
        mEnterAnimSet.addListener(new AnimatorListenerAdapter() {
            boolean canceled;
            // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimating = true;
                canceled = false;
            }
            // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                canceled = true;
                mAnimating = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d("felix","RecentsPanelView.DEBUG continueAutoQuickSettingEnterAnimation() onAnimationEnd()");
                // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
                if (BaseStatusBar.FELIXDBG)
                    Log.d("felix", "RecentsPanelView continueAutoQuickSettingEnterAnimation() setScrollable(true) 333");
                ((AuroraRecentPage) mRecentsContainer).setScrollable(true);
                if (canceled) return;
				// Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
                // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                handler.postDelayed(mDisableHandlerRunnable, 0);
				// Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.
                // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
                // Load other icons after scroll
                //refreshViews();
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
                // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                mAnimating = false;
                // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            }
        });
        // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
    	mEnterAnimSet.start();
        // Aurora <Felix.Duan> <2014-10-14> <END> Fix BUG #8655.
    }

    public void autoQuickSettingQuitAnimation(){
        Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingQuitAnimation()");
//    	mRecentsMaskView.getBackground().setAlpha(219);
        // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingTranslation != null && animQuickSettingTranslation.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingTranslation.cancel()");
            animQuickSettingTranslation.cancel();
        }
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. start
		if(isOrientationPortrait()){
				animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationY", quickSettingAnimEnd,quickSettingAnimStart);
		} else {
				animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationX", quickSettingAnimEnd,quickSettingAnimStart);
		}
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. end
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingTranslation.setDuration(QUIT_ANIMATION_DURATION);

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingAlpha != null && animQuickSettingAlpha.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingAlpha.cancel()");
            animQuickSettingAlpha.cancel();
        }
    	animQuickSettingAlpha = ObjectAnimator.ofFloat(mQuickSetting, "alpha", mQuickSetting.getAlpha(),0f);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingAlpha.setDuration(QUIT_ANIMATION_DURATION);

        // Aurora <Felix.Duan> <2014-4-19> <BEGIN> Optimize RecentsPanel quit alpha change
        mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(0);
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (bgAlpha != null && bgAlpha.isRunning()) {
            Log.d("felix","RecentsPanelView.DEBUG bgAlpha.cancel()");
            bgAlpha.cancel();
        }
    	bgAlpha = ObjectAnimator.ofInt(mRecentsMaskView.getBackground(), "alpha", sMaskAlpha,0);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    	bgAlpha.setDuration(QUIT_ANIMATION_DURATION);
        // Aurora <Felix.Duan> <2014-4-19> <END>  Optimize RecentsPanel quit alpha change

        // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration
    	animQuickSettingTranslation.addListener(new AnimatorListenerAdapter(){

            // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimating = true;
            }
            // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.

			@Override
			public void onAnimationCancel(Animator arg0) {
		        Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingQuitAnimation() onAnimationCancel()");
                // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
                mAnimating = false;
                // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
				setRecentsMaskViewAlpha();
				mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(0);
				((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
				
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
                Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingQuitAnimation() onAnimationEnd()");
                // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
                mAnimating = false;
                // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
				setRecentsMaskViewAlpha();
				((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
				if(mAuroraRubbishView == null){
					mAuroraRubbishView = (AuroraRubbishView)findViewById(R.id.clear_all);
				}
				mAuroraRubbishView.rest();
				mHandlerBarCallback.removeRecentPanelView();
//				closeHardAccelerate();

                // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
                // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
                if (BaseStatusBar.FELIXDBG)
                    Log.d("felix", "RecentsPanelView autoQuickSettingQuitAnimation() setScrollable(true) 444");
                ((AuroraRecentPage) mRecentsContainer).setScrollable(true);
                // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
                // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
                // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
                // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                //((AuroraRecentPage) mRecentsContainer).removeChildPages();
                // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration
                setNaviBarColorBlack(false);
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
			}
			
		}
    	);
    	AnimatorSet animSet = new AnimatorSet();
    	animSet.play(animQuickSettingAlpha);
    	animSet.play(animQuickSettingTranslation);
        // Aurora <Felix.Duan> <2014-4-19> <BEGIN> Optimize RecentsPanel quit alpha change
    	animSet.play(bgAlpha);
        // Aurora <Felix.Duan> <2014-4-19> <END>  Optimize RecentsPanel quit alpha change
    	animSet.start();
    }
    
    public void autoQuickSettingQuitAnimation(final boolean isShowHandlerBar){
        Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingQuitAnimation() 2");
//    	mRecentsMaskView.getBackground().setAlpha(219);
        // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingTranslation != null && animQuickSettingTranslation.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingTranslation.cancel()");
            animQuickSettingTranslation.cancel();
        }
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. start
		if(isOrientationPortrait()){
				animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationY", quickSettingAnimEnd,quickSettingAnimStart);
		} else {
				animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationX", quickSettingAnimEnd,quickSettingAnimStart);
		}
		// Aurora <Steve.Tang> 2015-01-19 support land recent panel. end
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingTranslation.setDuration(QUIT_ANIMATION_DURATION);

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingAlpha != null && animQuickSettingAlpha.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingAlpha.cancel()");
            animQuickSettingAlpha.cancel();
        }
    	animQuickSettingAlpha = ObjectAnimator.ofFloat(mQuickSetting, "alpha", mQuickSetting.getAlpha(),0f);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingAlpha.setDuration(QUIT_ANIMATION_DURATION);
        // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration

        // Aurora <Felix.Duan> <2014-4-19> <BEGIN> Optimize RecentsPanel quit alpha change
        mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(0);
        // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (bgAlpha != null && bgAlpha.isRunning()) {
            Log.d("felix","RecentsPanelView.DEBUG bgAlpha.cancel()");
            bgAlpha.cancel();
        }
        bgAlpha = ObjectAnimator.ofInt(mRecentsMaskView.getBackground(), "alpha",sMaskAlpha,0);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
        // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
        bgAlpha.setDuration(QUIT_ANIMATION_DURATION);
        // Aurora <Felix.Duan> <2014-4-19> <END>  Optimize RecentsPanel quit alpha change

    	animQuickSettingTranslation.addListener(new AnimatorListenerAdapter(){

            // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
			@Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimating = true;
            }
            // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.

            @Override
			public void onAnimationCancel(Animator arg0) {
                Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingQuitAnimation() 2 onAnimationCancel()");
                // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
                mAnimating = false;
                // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
				setRecentsMaskViewAlpha();
				mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(0);
				((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
				
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
                Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingQuitAnimation() 2 onAnimationEnd()");
                // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Don`t apply another animation while animating.
                mAnimating = false;
                // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Don`t apply another animation while animating.
				setRecentsMaskViewAlpha();
				((AuroraRecentPage) mRecentsContainer).setmNeedToClean(false);
				if(mAuroraRubbishView == null){
					mAuroraRubbishView = (AuroraRubbishView)findViewById(R.id.clear_all);
				}
				mAuroraRubbishView.rest();
				mHandlerBarCallback.removeRecentPanelView(isShowHandlerBar);
//				closeHardAccelerate();

                // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
                // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
                if (BaseStatusBar.FELIXDBG)
                    Log.d("felix", "RecentsPanelView autoQuickSettingQuitAnimation() setScrollable(true) 555");
				((AuroraRecentPage) mRecentsContainer).setScrollable(true);
                // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
                // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
                // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
                // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                //((AuroraRecentPage) mRecentsContainer).removeChildPages();
                // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration
                Intent intent = new Intent (PhoneStatusBarPolicy.ACTION_SET_NAVIBAR_COLOR);
                intent.putExtra("mode", -1);
                mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
			}
			
		}
    	);
    	AnimatorSet animSet = new AnimatorSet();
    	animSet.play(animQuickSettingAlpha);
    	animSet.play(animQuickSettingTranslation);
        // Aurora <Felix.Duan> <2014-4-19> <BEGIN> Optimize RecentsPanel quit alpha change
        animSet.play(bgAlpha);
        // Aurora <Felix.Duan> <2014-4-19> <END>  Optimize RecentsPanel quit alpha change
    	animSet.start();
    }
    
    private boolean isThisASystemPackage(String packageName) {
        try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_DISABLED_COMPONENTS
					|PackageManager.GET_UNINSTALLED_PACKAGES
                    |PackageManager.GET_SIGNATURES);	
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0){
				return true;
			}else{
				return false;
			}
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
	}
    
 	public void scrollToOriginalLocation(){
        Log.d("felix","RecentsPanelView.DEBUG scrollToOriginalLocation()");
// 		mRecentsMaskView.scrollTo(0, 0);
        // Aurora <Felix.Duan> <2014-4-11> <BEGIN> Optimize quit animation duration
        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
 		((RecentsLayout)mRecentsMaskView).startScroll(0, mRecentsMaskView.getScrollY(), 0,
            -mRecentsMaskView.getScrollY(), QUIT_ANIMATION_DURATION * 2);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
        // Aurora <Felix.Duan> <2014-4-11> <END> Optimize quit animation duration
 	}
 	
 	public View getMoveView(){
 		return mRecentsMaskView;
 	}
 	
 	public View getQuickSettingView(){
// 		mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(blurredBackgroundAlphaChanged(Math.min(y, 255)));
// 		mRecentsMaskView.getBackground().setAlpha(backgroundAlphaChanged(y));
 		return mQuickSetting;
 	}
 	
	// Aurora <Felix.Duan> <2015-1-29> <BEGIN> Support landscape recents panel pull out
 	public void autoQuickSettingEnterAnimationForZ(int scrollZ){
 		if(scrollZ>HandlerBar.mRecentsScrimHeight){
 			return;
 		}
        Log.d("felix","RecentsPanelView.DEBUG autoQuickSettingEnterAnimationForZ()");
        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingTranslation != null && animQuickSettingTranslation.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingTranslation.cancel()");
            animQuickSettingTranslation.cancel();
        }
        if (isOrientationPortrait()) {
            animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationY",
                    mQuickSetting.getTranslationY() < quickSettingAnimStart ? quickSettingAnimStart : mQuickSetting.getTranslationY(),
                    quickSettingAnimEnd);
        } else {
            animQuickSettingTranslation = ObjectAnimator.ofFloat(mQuickSetting, "translationX",
                    mQuickSetting.getTranslationX() < quickSettingAnimStart ? quickSettingAnimStart : mQuickSetting.getTranslationX(),
                    quickSettingAnimEnd);
        }
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingTranslation.setDuration(500);

        // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
        if (animQuickSettingAlpha != null && animQuickSettingAlpha.isRunning()){
            Log.d("felix","RecentsPanelView.DEBUG animQuickSettingAlpha.cancel()");
            animQuickSettingAlpha.cancel();
        }
    	animQuickSettingAlpha = ObjectAnimator.ofFloat(mQuickSetting, "alpha", mQuickSetting.getAlpha(),1f);
        // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    	animQuickSettingAlpha.setDuration(500);
    	AnimatorSet animSet = new AnimatorSet();
        // Aurora <Felix.Duan> <2014-4-28> <BEGIN> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (BaseStatusBar.FELIXDBG)
                    Log.d("felix", "RecentsPanelView autoQuickSettingEnterAnimationForZ() setScrollable(true)");
                ((AuroraRecentPage) mRecentsContainer).setScrollable(true);
                // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
				//// Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
                //// Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
                //handler.postDelayed(mDisableHandlerRunnable, 500);
				//// Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.
                // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
                // Load other icons after scroll
                //refreshViews();
                // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views
            }
        });
        // Aurora <Felix.Duan> <2014-4-28> <END> Re-Fix BUG #4244. Orphan icon view left seen due to logic sequence flaw
    	animSet.play(animQuickSettingTranslation);
    	animSet.play(animQuickSettingAlpha);
    	animSet.start();
    }
	// Aurora <Felix.Duan> <2015-1-29> <END> Support landscape recents panel pull out
 	
    // Not used any more
 	//public void autoQuickSettingEnterAnimationForY(int scrolly,int duration){
 	//	if(scrolly>HandlerBar.mRecentsScrimHeight){
 	//		return;
 	//	}
    //    // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
    //    if (animQuickSettingTranslationY != null && animQuickSettingTranslationY.isRunning()){
    //        Log.d("felix","RecentsPanelView.DEBUG animQuickSettingTranslationY.cancel()");
    //        animQuickSettingTranslationY.cancel();
    //    }
    //	animQuickSettingTranslationY = ObjectAnimator.ofFloat(mQuickSetting, "translationY", mQuickSetting.getTranslationY() < 200 ? 200 : mQuickSetting.getTranslationY(), 259);
    //    // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
    //    // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    //	animQuickSettingTranslationY.setDuration(duration);

    // Not used any more
    //    // Aurora <Felix.Duan> <2014-5-29> <BEGIN> Unfinished enter animation caused quick no animation
    //    if (animQuickSettingAlpha != null && animQuickSettingAlpha.isRunning()){
    //        Log.d("felix","RecentsPanelView.DEBUG animQuickSettingAlpha.cancel()");
    //        animQuickSettingAlpha.cancel();
    //    }
    //	animQuickSettingAlpha = ObjectAnimator.ofFloat(mQuickSetting, "alpha", mQuickSetting.getAlpha(),1f);
    //    // Aurora <Felix.Duan> <2014-5-29> <END> Unfinished enter animation caused quick no animation
    //	animQuickSettingAlpha.setDuration(duration);
    //	AnimatorSet animSet = new AnimatorSet();
    //	animSet.play(animQuickSettingTranslationY);
    //	animSet.play(animQuickSettingAlpha);
    //	animSet.start();
    //}
 	
 	private static float mScrolly = 0;
 	
 	public void continueAlpha(final int scrolly){
 		isrung = true;
 		
 		mScrolly = scrolly;
 		
 		autoQuickSettingEnterAnimationForZ(scrolly);
 		new Thread(new Runnable(){
 			@Override
 			public void run(){
 				while(isrung){
 				    updateAlpha();
 				}
 				mScrolly = 0;
 			}
 		}).start();
 	}
 	
 	private Handler mHandler = new Handler(){
 		@Override
  	    public void handleMessage(Message msg){
 			
 			super.handleMessage(msg);
 			float alpha = msg.getData().getFloat("scrolly");
 		    // 设置示当前的Alpha值  
// 			imageView.setAlpha(image_alpha);
            // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 			setMaskAlpha(backgroundAlphaChanged(alpha));
            // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
 			mHandlerBarCallback.setRecentsPanelViewBackgroundAlpha(blurredBackgroundAlphaChanged(Math.min(alpha, 255)));
  	    }
 	};
 	
 	public void updateAlpha() {
 		
 		if (mScrolly + 0.1 <= 324) {
 			mScrolly += 0.1;
 		}else {
 			mScrolly = 324;
 			isrung = false;
 		}
 		
 		Message msg = mHandler.obtainMessage();
 		Bundle bundle = new Bundle();
 		bundle.putFloat("scrolly", mScrolly);
 		msg.setData(bundle);
 	    // 发送需要更新imageview视图的消息-->这里是发给主线程
 		mHandler.sendMessageDelayed(msg, 220);
 	}
 	
 	public void closeHardAccelerate(){
//    	view.setLayerType(View.LAYER_TYPE_NONE,null);
 		mHandlerBarCallback.getRecentsPanelView().setLayerType(View.LAYER_TYPE_NONE,null);
    	mRecentsMaskView.setLayerType(View.LAYER_TYPE_NONE,null);
    	mQuickSetting.setLayerType(View.LAYER_TYPE_NONE,null);
    	mRecentsScrim.setLayerType(View.LAYER_TYPE_NONE,null);
    }
 	
    public void openHardAccelerate(){
    	mHandlerBarCallback.getRecentsPanelView().setLayerType(View.LAYER_TYPE_HARDWARE,null);
    	mRecentsMaskView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
    	mQuickSetting.setLayerType(View.LAYER_TYPE_HARDWARE,null);
    	mRecentsScrim.setLayerType(View.LAYER_TYPE_HARDWARE,null);
    }
    
    public void scrollToOriginalLocationNoAnimation(){
    	mRecentsMaskView.scrollTo(0, 0);
    }

    // Aurora <Felix.Duan> <2014-5-8> <BEGIN> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.
    private static volatile int sMaskAlpha = 0; // Hold current alpha of mRecentsMaskView
    /**
     * Set RecentsMaskView`s background alpha and remember it.
     * Because Android doesn`t support Drawable.getAlpha() untill api-level 19
     * @param alpha to set
     * 
     * @author Felix.Duan
     * @date 2014-5-8
     */
    private void setMaskAlpha(int alpha) {
        mRecentsMaskView.getBackground().setAlpha(alpha);
        sMaskAlpha = alpha;
    }
    // Aurora <Felix.Duan> <2014-5-8> <END> Re-fix BUG #4496. Sub bug: Use current mask alpha than full value.

    private Runnable mDisableHandlerRunnable = new Runnable() {
        @Override
        public void run() {
        Log.d("felix","RecentsPanelView mDisableHandlerRunnable disable runnable mShowing = " + mShowing);
            // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
            //if (mShowing) {
            if (isShown()) {
                // Aurora <Felix.Duan> <2014-5-20> <BEGIN> Disable pull up feature in certain cases.
                Intent intent = new Intent(HandlerBar.DISABLE_HANDLER);
                mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
                // Aurora <Felix.Duan> <2014-5-20> <END> Disable pull up feature in certain cases.
            }
            // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
        }
    };

    // Aurora <Felix.Duan> <2014-7-28> <BEGIN> Pull-up refactor: preload tasks & views
    private Runnable mLoadTaskRunnable = new Runnable() {
        @Override
        public void run() {
            mRecentTasksLoader.checkTaskSample();
        }
    };
    // Aurora <Felix.Duan> <2014-7-28> <END> Pull-up refactor: preload tasks & views

    // Aurora <Felix.Duan> <2014-7-30> <BEGIN> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804
    public void setNaviBarColorBlack(boolean black) {
        Intent intent = new Intent (PhoneStatusBarPolicy.ACTION_SET_NAVIBAR_COLOR);
        intent.putExtra("mode", (black?BarTransitions.MODE_INSTANT_OPAQUE:-1));
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
    }
    // Aurora <Felix.Duan> <2014-7-30> <END> Fix BUG #6566 BUG #6492 BUG #6493 BUG #6716 BUG #6804

    // Aurora <Felix.Duan> <2014-8-8> <BEGIN> Refactor：update IME state &  sechdule task loading
    public void scheduleLoadTask() {
        Log.d(TAG, "scheduleLoadTask()");
        handler.removeCallbacks(mLoadTaskRunnable);
        handler.postDelayed(mLoadTaskRunnable, 100);
    }


    private Runnable mUpdateImeStateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mUpdateImeStateRunnable run()"); 
            if (isShowInputMethod) {
            	if(isShown()){
            		removeRecentsPanelView(false);
            	}else{
            		mHandlerBarCallback.showHandlerBarView(false);
            	}
                if (InvokerService.getInstance() != null) // Avoid NullPointer Exception when not using InvokerService
                    InvokerService.getInstance().updateInputMethodStatus(isShowInputMethod);
            } else {
                mHandlerBarCallback.showHandlerBarView(true);
                isShowInputMethod = false;
                if (InvokerService.getInstance() != null) // Avoid NullPointer Exception when not using InvokerService
                    InvokerService.getInstance().updateInputMethodStatus(isShowInputMethod);
            }
        }
    };

    public void updateImeState(boolean showing) {
        Log.d(TAG, "updateImeState() showing = " + showing);
        isShowInputMethod = showing;
        handler.removeCallbacks(mUpdateImeStateRunnable);
        handler.postDelayed(mUpdateImeStateRunnable, 100);
    }
    // Aurora <Felix.Duan> <2014-8-8> <END> Refactor：update IME state &  sechdule task loading

    
    // Aurora <Felix.Duan> <2014-8-22> <BEGIN> Fix BUG #7822. Update string resource
    private TextView mNoAppLabel;
    public void updateResources() {
        mNoAppLabel.setText(R.string.recent_no_task);
    }
    // Aurora <Felix.Duan> <2014-8-22> <END> Fix BUG #7822. Update string resource
}
