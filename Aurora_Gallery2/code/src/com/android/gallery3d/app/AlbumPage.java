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
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.MtpDevice;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.ActionModeHandler;
import com.android.gallery3d.ui.ActionModeHandler.ActionModeListener;
import com.android.gallery3d.ui.AlbumSlotRenderer;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.AuroraActionModeHandler;
import com.android.gallery3d.ui.FadeTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.PhotoFallbackEffect;
import com.android.gallery3d.ui.RelativePosition;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.Globals;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;
//import com.android.gallery3d.fragmentapp.GridViewFragment;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraSystemMenu;





//Aurora <paul> <2014-02-27> for NEW_UI begin
import com.android.gallery3d.ui.RawTexture;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.TiledScreenNail;

import android.graphics.Bitmap;

import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.viewpager.ViewPagerChileOperattionListener;
import com.android.gallery3d.viewpager.ViewpagerActivity;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.DownloadTaskListManager;
import com.android.gallery3d.xcloudalbum.tools.IuniAccountUtils;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListActivity;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudTaskListenerManager;
import com.android.gallery3d.xcloudalbum.widget.LocalSelectPopupWindow;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import android.os.Message;
import android.os.Environment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileDescriptor;

import com.android.gallery3d.data.LocalVideo;

import android.graphics.BitmapFactory;

import com.android.gallery3d.common.BitmapUtils;

import android.os.SystemClock;

import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.crop.CropExtras;
import com.android.gallery3d.filtershow.crop.CropActivity;
import com.android.gallery3d.xcloudalbum.account.AccountHelper.IAccountChange;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.inter.IBaiduTaskListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.aurora.utils.SystemUtils;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.ui.TiledTexture;
import com.android.gallery3d.ui.FeatureConfig;

import android.view.Gravity;

import com.android.gallery3d.local.widget.PhotoPopupWindowUtil;

import android.content.ActivityNotFoundException;

import com.baidu.xcloud.pluginAlbum.AccountProxy;

//Aurora <paul> <2014-02-27> for NEW_UI end
public class AlbumPage extends ActivityState implements GalleryActionBar.ClusterRunner,
        SelectionManager.SelectionListener, MediaSet.SyncListener, GalleryActionBar.OnAlbumModeSelectedListener, 
        IAccountChange, IBaiduinterface, IBaiduTaskListener{
    @SuppressWarnings("unused")

	public static boolean NEW_UI = false;//Aurora <paul> <2014-02-27> for NEW_UI
	public static final int REQUEST_TUYA = 1001;
    private static final String TAG = "AlbumPage";
    
    //SQF ADDED ON 2015.4.24 begin
    public static final String PREF_KEY_IUNI_ACCOUNT_TOKEN = "IUNI_ACCOUNT_TOKEN";
    public static final int REQUEST_ACCOUNT_LOGIN = 123;
    //SQF ADDED ON 2015.4.24 end
    
    public static final String KEY_MEDIA_PATH = "media-path";
    public static final String KEY_PARENT_MEDIA_PATH = "parent-media-path";
    public static final String KEY_SET_CENTER = "set-center";
    public static final String KEY_AUTO_SELECT_ALL = "auto-select-all";
    public static final String KEY_SHOW_CLUSTER_MENU = "cluster-menu";
    public static final String KEY_EMPTY_ALBUM = "empty-album";
    public static final String KEY_RESUME_ANIMATION = "resume_animation";
    
    //lory add start
    public static final String KEY_WHETHER_IN_ROOT_PAGE = "defult-manner";
    public static final String KEY_ALLITEM_NUM = "aurora-imgnum";
    private boolean mIsRootState;
    //lory add end

    private static final int REQUEST_SLIDESHOW = 1;
    public static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_DO_ANIMATION = 3;
    //Aurora <SQF> <2014-6-17>  for NEW_UI begin
    private static final int REQUEST_EDIT = PhotoPage.REQUEST_EDIT;
    //Aurora <SQF> <2014-6-17>  for NEW_UI end
    
    



    //Aurora <SQF> <2014-5-15>  for NEW_UI begin

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;

    private static final float USER_DISTANCE_METER = 0.3f;

    private boolean mIsActive = false;
    private AlbumSlotRenderer mAlbumView;
    private Path mMediaSetPath;
    private String mParentMediaSetString;
    private SlotView mSlotView;

    private AlbumDataLoader mAlbumDataAdapter;

    protected SelectionManager mSelectionManager;
    //private Vibrator mVibrator;

    //mGetContent indicates jump from SMS/Contacts.
    private boolean mGetContent;
    private boolean mShowClusterMenu;
    
    private int mFocusIndex = 0;
    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private MediaSet mMediaSet;
    private boolean mShowDetails;
    private float mUserDistance; // in pixel
    private Future<Integer> mSyncTask = null;
    private boolean mLaunchedFromPhotoPage;
    private boolean mInCameraApp;
    private boolean mInCameraAndWantQuitOnPause;

    private int mLoadingBits = 0;
    private boolean mInitialSynced = false;
    private int mSyncResult;
    private boolean mLoadingFailed;
    private RelativePosition mOpenCenter = new RelativePosition();
    private ActionModeHandler mActionModeHandler;

    private Handler mHandler;
    private static final int MSG_PICK_PHOTO = 0;
    private static final int MSG_UPDATE_DATELAYOUT = 1;
    private static final int MSG_BITMAP = 2;
    private static final int MSG_GOING_PHOTOPAGE = 3;
    private static final int MSG_UPDATE_UPLOAD_PROGRESS = 4;
    
    
    //private GridViewFragment mFragment;//lory add
	// Aurora <paul> <2013-12-24> added for gallery begin
	private ViewpagerActivity mViewpagerActivity;
	private int mMenuPaddingRight;
	private int mMenuPaddingBottom;
	private boolean mNeedAnim = false; // Aurora <paul> <2014-05-06> 
	private static final int MENU_COUNT = 5;
	private static final char MENU_SHARE = 0X01; 	//index 0 
	private static final char MENU_DELETE = 0X02;	//index 1 
	private static final char MENU_FAVORITE = 0X04;	//index 2
	private static final char MENU_CANCEL_FAVORITE = 0X08;	//index 3
	private static final char MENU_MORE = 0X10;		//index 4
	
	//private static final char MENU_UPLOAD = 0X20;	//index5
	//private static final char MENU_MOVE = 0X40;		//index 6
	
	
	private static final char MENU_ALL = MENU_SHARE | MENU_DELETE | MENU_FAVORITE | MENU_CANCEL_FAVORITE | MENU_MORE;
	
	// Aurora <paul> <2013-12-24> added for gallery end
	//Iuni <lory><2013-12-29> add begin
	private AuroraActionModeHandler mAuroraActionModeHandler;
	private boolean mEnterPhotoPage = false;
	private AuroraActionBar mAuroraActionBar;
	private ContentResolver m_AuroraResolver;
    private Animation mInAnimation;
    private Animation mOutAnimation;
    private Animation mInAlphaAnimation;
    private Animation mOutAlphaAnimation;
    private int mActionBarHight = 0;
    private static final int ACTIONBAR_DURATION = 220;
    
    //SQF_ANNOTATED_BEGIN
//    private LinearLayout date_GroupLayout;
//    private TextView date_TextView;
//    private TextView year_TextView;
//    private TextView wenzi_TextView;
    //SQF_ANNOTATED_END
    private int mheader_paddingtop;
    private int lastFirstVisibleItem = -1;
    //private static final String AURORA_DEFAULT_NUMBER_FONT_PATH = "system/fonts/Roboto-Light.ttf";
	//private Typeface m_auroraNumberTf;
	//SQF_ANNOTATED_BEGIN
//	private View mheader_background;
	//SQF_ANNOTATED_END
	private String m_dayStr;
	private String m_monthStr;
	
	public static String AURORA_PIC_MANAGER = "com.aurora.filemanager";
	public static String AURORA_PIC_MANAGER_ACTION = "com.aurora.pic.file.manager.action";
	private int mDleted = 0;
	
	private boolean m_bOnlyVideo = false;
	private String mImgWhereClause;
    private String mVideoWhereClause;
    //private String []m_ImgSelectionArgs;
    //private String []m_VideoSelectionArgs;
    private final Uri mImgBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
    private final Uri mVideoBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
    private int m_totalnum = 0;
    private int m_firstVisible = 0;
    private boolean mbDeletedMode = false; 
    private static boolean m_bfirstEnter = false;
    public static final int MSG_ANIMATION_INVIEW = 101;
    private int m_AllItemNum = 0;
	//Iuni <lory><2013-12-29> add end
    
    //SQF ADD ON 2015.4.27 begin
    private List<CommonFileInfo> mCommonFilelist;
    private ArrayList<Path> mSelectedPaths;
    //private NetworkStateReceiver mNetworkStateReceiver = new NetworkStateReceiver();
    //SQF ADD ON 2015.4.27 end
    
	//paul del
	/*
    private PhotoFallbackEffect mResumeEffect;
    */
    //Aurora <SQF> <2014-09-18>  for NEW_UI begin
    public boolean isInSelectionMode() {
    	if(mSelectionManager == null) return false;
    	return mSelectionManager.inSelectionMode();
    }
    //Aurora <SQF> <2014-09-18>  for NEW_UI end

    private PhotoFallbackEffect.PositionProvider mPositionProvider =
            new PhotoFallbackEffect.PositionProvider() {
        @Override
        public Rect getPosition(int index) {
        	index = getIndexInSlotView(index);//Aurora <paul> <2014-02-27> for NEW_UI
            Rect rect = mSlotView.getSlotRect(index);
            Rect bounds = mSlotView.bounds();
            rect.offset(bounds.left - mSlotView.getScrollX(),
                    bounds.top - mSlotView.getScrollY());
            return rect;
        }

        @Override
        public int getItemIndex(Path path) {
            int start = mSlotView.getVisibleStart();
            int end = mSlotView.getVisibleEnd();
            for (int i = start; i < end; ++i) {
                MediaItem item = mAlbumDataAdapter.get(i);
                if (item != null && item.getPath() == path) return i;
            }
            return -1;
        }
    };

    @Override
    protected int getBackgroundColorId() {
    	if (mIsFullScreen) {
    		return R.color.album_pohtopage_background;
		} else {
			return R.color.album_background;
		}
    }

    private final GLView mRootPane = new GLView() {
        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
			if(!mIsRootState) {//Iuni <lory><2013-12-21> add begin for crush from photopage when ORIENTATION SENSOR changes
				if(NEW_UI){
					//wenyongzhe 2015.9.24
//					mViewH = bottom - top+mAuroraActionBar.getHeight();
					mViewH = bottom - top;
					mViewW = right - left;
				 }
				//wenyongzhe 2015.9.24 start
//				int slotViewTop = mActivity.getGalleryActionBar().getHeight();
//				int slotViewTop = (int)(mAuroraActionBar.getHeight()*0.25);//wenyongzhe 2016.1.6 new_ui
				int slotViewTop = 0;//wenyongzhe 2016.1.6 new_ui  paul modify 100 -> 0
				//wenyongzhe 2015.9.24 end
				
				int slotViewBottom = bottom - top;
	            int slotViewRight = right - left;

	            if (mShowDetails) {
	                mDetailsHelper.layout(left, slotViewTop, right, bottom);
	            } else {
	                mAlbumView.setHighlightItemPath(null);
	            }
	            // Set the mSlotView as a reference point to the open animation
	            mOpenCenter.setReferencePosition(0, slotViewTop);
	            mSlotView.layout(0, slotViewTop, slotViewRight, slotViewBottom);
	            GalleryUtils.setViewPointMatrix(mMatrix,
	                    (right - left) / 2, (bottom - top) / 2, -mUserDistance);
				if(NEW_UI){
					mPhotoPage.toLayout( changed, left, top, right, bottom);
				}
			}
            
        }

        @Override
        protected void render(GLCanvas canvas) {
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            canvas.multiplyMatrix(mMatrix, 0);
            super.render(canvas);
			//paul del
			/*
            if (mResumeEffect != null) {
                boolean more = mResumeEffect.draw(canvas);
                if (!more) {
                    mResumeEffect = null;
                    mAlbumView.setSlotFilter(null);
                }
                // We want to render one more time even when no more effect
                // required. So that the animated thumbnails could be draw
                // with declarations in super.render().
                invalidate();
            }
            */
            canvas.restore();
        }
		//Aurora <paul> <2014-02-27> for NEW_UI begin
		@Override
		protected boolean renderAnimation(GLCanvas canvas) {
			if(state.LOAD == mOpenAnimFlag){
				return true;
			}
			if(state.BUILDOPEN== mOpenAnimFlag){//open		
				if(mSlotView.isAnimPlaying()){
					resetState();
					return false;
				}
				mHandler.sendMessage(mHandler.obtainMessage(
						MSG_GOING_PHOTOPAGE));
				mAlbumView.setPressedIndex(-1);
				mEnteringEffect = buildFallbackEffectEx(mRootPane, canvas);
				mEnteringEffect.setToOpenningAnimation();
				mEnteringEffect.setPositionProvider(mPositionProvider,mCurrentIndex);
				mEnteringEffect.start();
				//Aurora <SQF> <2014-05-17>  for NEW_UI begin
				//ORIGINALLY:
				//mOpenAnimFlag = state.DRAWING;
				//SQF MODIFIED TO:
				setOpenAnimState(state.DRAWING);
				//Aurora <SQF> <2014-05-17>  for NEW_UI end
				mSlotView.setUnrenderIndex(getIndexInSlotView(mCurrentIndex));
				//mSlotView.setUnrenderIndex(mCurrentIndex);
				return true;
			}
			
			if(state.BUILDCLOSE == mOpenAnimFlag){//close
				mLeavingEffect = mPhotoPage.getFallbackEffect(canvas);//buildFallbackEffectEx(mRootPane, canvas);
				mLeavingEffect.setPositionProvider(mPositionProvider,mFocusIndex);
				mLeavingEffect.start();
				//Aurora <SQF> <2014-05-17>  for NEW_UI begin
				//ORIGINALLY:
				//mOpenAnimFlag = state.DRAWING;
				//SQF MODIFIED TO:
				setOpenAnimState(state.DRAWING);
				//Aurora <SQF> <2014-05-17>  for NEW_UI end
				mSlotView.setUnrenderIndex(mViewFocusIndex);
				//return true;
			}			
			
			if(state.DRAWING == mOpenAnimFlag){
				canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
					if (mLeavingEffect != null) {
						boolean more = mLeavingEffect.draw(canvas);
						if (!more) {
							mLeavingEffect = null;
							//Aurora <SQF> <2014-05-17>  for NEW_UI begin
							//ORIGINALLY:
							//mOpenAnimFlag = state.IDLE;
							//SQF MODIFIED TO:
							setOpenAnimState(state.IDLE);
							//Aurora <SQF> <2014-05-17>  for NEW_UI end
							mSlotView.setUnrenderIndex(-1);
						}
						// We want to render one more time even when no more effect
						// required. So that the animated thumbnails could be draw
						// with declarations in super.render().
						invalidate();
					}else if(mEnteringEffect != null){
						boolean more = mEnteringEffect.draw(canvas);
						if (!more) {
							mEnteringEffect = null;
							//Aurora <SQF> <2014-05-17>  for NEW_UI begin
							//ORIGINALLY:
							//mOpenAnimFlag = state.IDLE;
							//SQF MODIFIED TO:
							setOpenAnimState(state.IDLE);
							//Aurora <SQF> <2014-05-17>  for NEW_UI end
							mSlotView.setVisibility(GLView.INVISIBLE);
							mPhotoPage.getPhotoView().setVisibility(GLView.VISIBLE);
							mSlotView.setUnrenderIndex(-1);
							setContentPaneBackGroud(false);// Iuni <lory><2014-02-28> add begin
						}
						// We want to render one more time even when no more effect
						// required. So that the animated thumbnails could be draw
						// with declarations in super.render().
						invalidate();
					}else{
						//Aurora <SQF> <2014-05-17>  for NEW_UI begin
						//ORIGINALLY:
						//mOpenAnimFlag = state.IDLE;
						//SQF MODIFIED TO:
						setOpenAnimState(state.IDLE);
						//Aurora <SQF> <2014-05-17>  for NEW_UI end
					}
				canvas.restore();
				
				return true;
			} 

			return false;
		}
		//Aurora <paul> <2014-02-27> for NEW_UI end
    };

    // This are the transitions we want:
    //
    // +--------+           +------------+    +-------+    +----------+
    // | Camera |---------->| Fullscreen |--->| Album |--->| AlbumSet |
    // |  View  | thumbnail |   Photo    | up | Page  | up |   Page   |
    // +--------+           +------------+    +-------+    +----------+
    //     ^                      |               |            ^  |
    //     |                      |               |            |  |         close
    //     +----------back--------+               +----back----+  +--back->  app
    //
    @Override
    protected void onBackPressed() {
		//Aurora <paul> <2014-02-27> for NEW_UI begin
		if(NEW_UI){
			if(mActivity != null && mActivity.isFromXCloudAlbumMultiSelection()) {
				mActivity.finish();
				return;
			}

	    	if(mIsFullScreen || mPhotoPage.toFinishPhotoPage()){
				if(mOpenAnimFlag != state.IDLE) return;

				int backIndex = mPhotoPage.onBackToAlbum();
				if(-1 == backIndex) {
					return;
				}

	    		if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
	        		if (mAlbumDataAdapter != null) {
	    				mAlbumDataAdapter.setFlagToNotUpdateViews(true);
	    			}
	        		if (mIsFullScreen || mWillShowActionBar) {//Aurora <SQF> <2014-6-18>  add "|| mWillShowActionBar"
	        			ShowAnimationActionBar((View)mAuroraActionBar);
					}
	        		//Iuni <lory><2014-02-28> add begin
	    	    	setContentPaneBackGroud(true);
	    	    	//Iuni <lory><2014-02-28> add end
				}

				mAlbumView.setPressedIndex(-1);
				mFocusIndex = backIndex;
				mViewFocusIndex = getIndexInSlotView(backIndex);
	            mSlotView.makeSlotVisible(mViewFocusIndex);
				mOpenAnimFlag = state.BUILDCLOSE; 
				mIsFullScreen = false;
				mSlotView.setVisibility(GLView.VISIBLE);
				mPhotoPage.getPhotoView().setVisibility(GLView.INVISIBLE);
				
				//wenyongzhe 2015.10.8
				full(mIsFullScreen);
				
				return;	
			}
		}
		//Aurora <paul> <2014-02-27> for NEW_UI end
        if (mShowDetails) {
            hideDetails();
        } else if (mSelectionManager.inSelectionMode()) {
            mSelectionManager.leaveSelectionMode();
        } else {
            if(mLaunchedFromPhotoPage) {
                mActivity.getTransitionStore().putIfNotPresent(
                        PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                        PhotoPage.MSG_ALBUMPAGE_RESUMED);
            }
            // TODO: fix this regression
            // mAlbumView.savePositions(PositionRepository.getInstance(mActivity));
            if (mInCameraApp) {
                super.onBackPressed();
            } else {
                onUpPressed();
            }
        }
    }

    //lory add
    @Override
	protected void onSetSelectIndex(int index) {
		// TODO Auto-generated method stub
		super.onSetSelectIndex(index);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, index, 0),100);
		return;
	}

    //wenyongzhe 2015.10.29
    public void leaveSelectionMode(){
    	if (mSelectionManager != null && mSelectionManager.inSelectionMode()) {
			mSelectionManager.leaveSelectionMode();
		}
    }
    
	@Override
	protected boolean onMyKeyDownEvent(int keyCode, KeyEvent event) {
		
		if(mActivity != null && mActivity.isFromXCloudAlbumMultiSelection()) {
			mActivity.finish();
			return true;
		}
		
		//super.onMyKeyDownEvent(keyCode, event);
		if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
			if (mAuroraActionBar != null && (mAuroraActionBar.auroraIsExitEditModeAnimRunning() || mAuroraActionBar.auroraIsEntryEditModeAnimRunning())) {
				return true;
			}
			
			if (mSelectionManager.inSelectionMode()) {
				mSelectionManager.leaveSelectionMode();
				return true;
			}
			
			return false;
		}
		/*
		if ((mFragment != null) && 
			(mFragment.isVisible()) && 
			(mFragment.bDelteMenuShow)) {
    		mFragment.onKeyBackPressed();
    		return true;
		}
		*/
		
		//Iuni <lory><2013-12-21> add begin for exit from phone info mode
		if(mGetContent)
		{
            mActivity.finish();
			return true;
		}
		return false;
	}

	private void onUpPressed() {
    	//lory add start
    	if (mIsRootState) {
            super.onBackPressed();
        }
    	//lory add end
    	
        if (mInCameraApp) {
            GalleryUtils.startGalleryActivity(mActivity);
        } else if (mActivity.getStateManager().getStateCount() > 1) {
            super.onBackPressed();
        } else if (mParentMediaSetString != null) {
            //Aurora <SQF> <2014-10-23>  for NEW_UI begin
            //SQF ANNOTATED ON 2014-10-23
        	/*
            Bundle data = new Bundle(getData());
            data.putString(AlbumSetPage.KEY_MEDIA_PATH, mParentMediaSetString);
            mActivity.getStateManager().switchState(
                    this, AlbumSetPage.class, data);
                    */
          //Aurora <SQF> <2014-10-23>  for NEW_UI end
        } 
   	 	mActivity.finish();

        return;
    }

    private void onDown(int index) {
	        mAlbumView.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
			if(NEW_UI){
				if(state.LOAD == mOpenAnimFlag) {
					resetState();
					if(null != mLoadBitmapTask) {
						mLoadBitmapTask.cancel();
						mLoadBitmapTask = null;
					}
				}
			}
            // Avoid showing press-up animations for long-press.
            mAlbumView.setPressedIndex(-1);
        }else{
				mAlbumView.setPressedUp();
        }
    }
    
    private void onSingleTapUp(int slotIndex) {
        if (!mIsActive) return;

        if (mSelectionManager.inSelectionMode()) {
            MediaItem item = mAlbumDataAdapter.get(slotIndex);
            if (item == null) return; // Item not ready yet, ignore the click
            mSelectionManager.toggle(item.getPath());
            mSlotView.invalidate();
        } else {
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		HideAnimationActionBar((View)mAuroraActionBar);
			}
        	
            // Render transition in pressed state
            if(NEW_UI){
            	//pickPhoto(slotIndex);
			}else{
	            mAlbumView.setPressedIndex(slotIndex);
	            mAlbumView.setPressedUp();
	            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, slotIndex, 0),
	                    FadeTexture.DURATION);
			}
        }
    }

    private void pickPhoto(int slotIndex, int noheadindex) {
		if(NEW_UI){
			//Iuni <lory><2014-03-03> add begin
			if (!mGetContent) {
				AuroraSystemMenu auroraMenu = ((AuroraActivity )mActivity).getAuroraMenu();
				if (auroraMenu != null && auroraMenu.isShowing()) {
					return;
				}
			}
			//Iuni <lory><2014-02-03> add end
			
			//Aurora <SQF> <2014-04-30>  for bug#4617 begin
			if(mSlotView != null && mSlotView.isAnimPlaying()) {
				return;
			}
			//Aurora <SQF> <2014-04-30>  for bug#4617 end

			MediaItem item = mAlbumDataAdapter.get(noheadindex);
			if (item == null) {
				return; // Item not ready yet, ignore the click
			}
			//Iuni <lory><2014-03-03> add begin
			if (mGetContent) {
				if (!mIsActive) { 
					return;
				}
				// Launch photos in lights out mode
	            //mActivity.getGLRoot().setLightsOutMode(true);//SQF ANNOTATED ON 2014-07-16
	            onGetContent(item);
	            return;
	        }
			//Iuni <lory><2014-02-03> add end
			
			//SQF ADDED ON 2015.4.29 BEGIN 
			mActivity.dismissUploadProgress();
			//SQF ADDED ON 2015.4.29 end
			
			if(state.IDLE == mOpenAnimFlag){					
				mAlbumView.setPressedIndex(noheadindex);
				mCurrentIndex = noheadindex;
				mSlotIndex = noheadindex;
				mPhotoPage.setPhotoIndex(mCurrentIndex);
				//Aurora <SQF> <2014-05-17>  for NEW_UI begin
				//ORIGINALLY:
				//mOpenAnimFlag = state.LOAD;
				//SQF MODIFIED TO:
				setOpenAnimState(state.LOAD);
				//Aurora <SQF> <2014-05-17>  for NEW_UI end
				mIsFullScreen = true;
				
				//wenyongzhe 2015.10.8 start
				full(mIsFullScreen);
				//wenyongzhe 2015.10.8 end
				
				int indexData = noheadindex;
				Bitmap bmp = mSlotView.loadBitmap(indexData);
				mDrawingBmpRotation = item.getFullImageRotation();
				if(null != bmp){
					mDrawingBmp = BitmapUtils.fitBitmapFullScreen(bmp,mViewW,mViewH,false);
					//Aurora <SQF> <2014-04-04>  for NEW_UI begin
					mPhotoPage.setEnteringBitmap(mCurrentIndex, mDrawingBmp, mDrawingBmpRotation);
					//Aurora <SQF> <2014-04-04>  for NEW_UI end
					//Aurora <SQF> <2014-05-17>  for NEW_UI begin
					//ORIGINALLY:
					//mOpenAnimFlag = state.BUILDOPEN;
					//SQF MODIFIED TO:
					setOpenAnimState(state.BUILDOPEN);
					//Aurora <SQF> <2014-05-17>  for NEW_UI end
				}
				else{
					loadbmp(item);
				}
				mSlotView.invalidate();
			}
		}else{
			pickPhoto(noheadindex, false);
		}
    }

    private void pickPhoto(int slotIndex, boolean startInFilmstrip) {
        if (!mIsActive) return;

        if (!startInFilmstrip) {
            // Launch photos in lights out mode
            //mActivity.getGLRoot().setLightsOutMode(true);//SQF ANNOTATED ON 2014-07-16
        }

        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null) 
        {
        	return; // Item not ready yet, ignore the click
        }
        
        if (mGetContent) {
            onGetContent(item);
        } else if (mLaunchedFromPhotoPage) {
            TransitionStore transitions = mActivity.getTransitionStore();
            transitions.put(
                    PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                    PhotoPage.MSG_ALBUMPAGE_PICKED);
            transitions.put(PhotoPage.KEY_INDEX_HINT, slotIndex);
            onBackPressed();
        } else {
            // Get into the PhotoPage.
            // mAlbumView.savePositions(PositionRepository.getInstance(mActivity));
            Bundle data = new Bundle();
            data.putInt(PhotoPage.KEY_INDEX_HINT, slotIndex);
            data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT,
                    mSlotView.getSlotRect(slotIndex, mRootPane));//
            data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
                    mMediaSetPath.toString());
            data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH,
                    item.getPath().toString());
            data.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                    PhotoPage.MSG_ALBUMPAGE_STARTED);//
            data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP,
                    startInFilmstrip);
            data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, mMediaSet.isCameraRoll());
            //startInFilmstrip:true
            mEnterPhotoPage = true;
            if (startInFilmstrip) {
                mActivity.getStateManager().switchState(this, PhotoPage.class, data);
            } else {
                mActivity.getStateManager().startStateForResult(
                            PhotoPage.class, REQUEST_PHOTO, data);
            }
        }
    }

    private void onGetContent(final MediaItem item) {
        DataManager dm = mActivity.getDataManager();
        Activity activity = mActivity;

        if (mData.getString(Gallery.EXTRA_CROP) != null) {
            // TODO: Handle MtpImagew
            Uri uri = dm.getContentUri(item.getPath());
            Intent intent = new Intent(CropActivity.CROP_ACTION, uri)
                    .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                    .putExtras(getData());
            if (mData.getParcelable(MediaStore.EXTRA_OUTPUT) == null) {
                intent.putExtra(CropExtras.KEY_RETURN_DATA, true);
            }
            activity.startActivity(intent);
            activity.finish();
        } else {
            Intent intent = new Intent(null, item.getContentUri())
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
    }

    public void onLongTap(int slotIndex) {
		if(mIsFullScreen) return; // Aurora <paul> <2014-03-08> for NEW_UI
		
        if (mGetContent) return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null) return;
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	mSelectionManager.setAutoLeaveSelectionMode(false);
		} else {
			mSelectionManager.setAutoLeaveSelectionMode(true);
		}
        mSelectionManager.toggle(item.getPath());
        mSlotView.invalidate();
    }
    
    @Override
    public void doCluster(int clusterType) {
        String basePath = mMediaSet.getPath().toString();
        String newPath = FilterUtils.newClusterPath(basePath, clusterType);
        Bundle data = new Bundle(getData());
        data.putString(AlbumSetPage.KEY_MEDIA_PATH, newPath);
        if (mShowClusterMenu) {
            Context context = mActivity.getAndroidContext();
            data.putString(AlbumSetPage.KEY_SET_TITLE, mMediaSet.getName());
            data.putString(AlbumSetPage.KEY_SET_SUBTITLE,
                    GalleryActionBar.getClusterByTypeString(context, clusterType));
        }

        // mAlbumView.savePositions(PositionRepository.getInstance(mActivity));
        mActivity.getStateManager().startStateForResult(
                AlbumSetPage.class, REQUEST_DO_ANIMATION, data);
    }
    


    @Override
    protected void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        //Log.i("SQF_LOG", "AlbumPage::onCreate");
        //Aurora <SQF> <2015-04-24>  for NEW_UI begin
        if(!Globals.OVERSEA_VERSION){
        	//registerNetworkStateReceiver(); paul del for BUG #18622
        	mActivity.getAccountHelper().setIAccountChangeListener(this);
        }
        //Aurora <SQF> <2015-04-24>  for NEW_UI end

		mNeedAnim = true;// Aurora <paul> <2014-05-06> 

        //lory add
        mIsRootState = false;//data.getBoolean(KEY_WHETHER_IN_ROOT_PAGE, false);
		mGetContent = data.getBoolean(Gallery.KEY_GET_CONTENT, false);
		
        mUserDistance = GalleryUtils.meterToPixel(USER_DISTANCE_METER);
        initializeViews();
        initializeData(data);
        
        mShowClusterMenu = data.getBoolean(KEY_SHOW_CLUSTER_MENU, false);
        mDetailsSource = new MyDetailsSource();
        Context context = mActivity.getAndroidContext();
        //mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	m_bfirstEnter = false;
        	mbDeletedMode = false;
        	
        	mImgWhereClause = MediaSetUtils.getImageQueryStr(context);
        	//m_ImgSelectionArgs = MediaSetUtils.geQueryParamStr(context);
        	
        	mVideoWhereClause = MediaSetUtils.getVideoQueryStr(context);
        	//m_VideoSelectionArgs = MediaSetUtils.geQueryParamStr(context);
        	
        	if (m_AuroraResolver == null) {
    			m_AuroraResolver = mActivity.getContentResolver();
    		}
    		
    		mAlbumDataAdapter.setContentResolver(m_AuroraResolver, mImgWhereClause, mVideoWhereClause);
        	
            
            initCustomActionBar();
        	
        	mInAnimation = new TranslateAnimation(0, 0, -mActionBarHight, 0);
        	mOutAnimation = new TranslateAnimation(0, 0, 0, -mActionBarHight);
        	//Log.i("zll", "zll ---- AlbumPage onCreate mActionBarHight:"+mActionBarHight);
        	
        	mInAnimation.setDuration(ACTIONBAR_DURATION);
        	mOutAnimation.setDuration(ACTIONBAR_DURATION);
        	
        	mInAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        	mInAlphaAnimation.setDuration(100);
        	
        	mOutAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        	mOutAlphaAnimation.setDuration(100);
		}

        // Enable auto-select-all for mtp album
        if (data.getBoolean(KEY_AUTO_SELECT_ALL)) {
            mSelectionManager.selectAll();
        }

        mLaunchedFromPhotoPage =
                mActivity.getStateManager().hasStateClass(PhotoPage.class);
        mInCameraApp = data.getBoolean(PhotoPage.KEY_APP_BRIDGE, false);
        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_PICK_PHOTO: {
                    	pickPhoto(message.arg1, message.arg2);
                        break;
                    }
					//Aurora <paul> <2014-02-27> for NEW_UI begin
                    case MSG_BITMAP: {
						if(state.LOAD == mOpenAnimFlag){
							mDrawingBmp = (Bitmap) message.obj;
							//Aurora <SQF> <2014-05-17>  for NEW_UI begin
							//ORIGINALLY:
							//mOpenAnimFlag = state.BUILDOPEN;
							//SQF MODIFIED TO:
							setOpenAnimState(state.BUILDOPEN);
							//Aurora <SQF> <2014-05-17>  for NEW_UI end
							//Aurora <SQF> <2014-04-04>  for NEW_UI begin
							mPhotoPage.setEnteringBitmap(mCurrentIndex, mDrawingBmp, mDrawingBmpRotation);
							//Aurora <SQF> <2014-04-04>  for NEW_UI end
							mSlotView.invalidate();
						}
                        break;
                    }
					
                    case MSG_GOING_PHOTOPAGE:{
						if (mSelectionManager.inSelectionMode()) {
							mSelectionManager.leaveSelectionMode();
						}
						HideAnimationActionBar((View)mAuroraActionBar);
                    	break;
                    }
                    
                    case MSG_UPDATE_UPLOAD_PROGRESS:
                    	mActivity.showUploadProgress();
                    	break;

					//Aurora <paul> <2014-02-27> for NEW_UI end
                    
                    //Aurora <SQF> <2014-05-22>  for NEW_UI begin
                    //SQF ANNOTATED ON 2014.5.22
                    /*
                    case MSG_UPDATE_DATELAYOUT:{
                    	int firstVisibleItem = message.arg1;
        				int bottom = message.arg2;
        				boolean bheader = Boolean.parseBoolean(message.obj.toString());
        				updateAuroraDateLayout(firstVisibleItem, bottom, bheader);
        				break;
                    }
                    */
                    //Aurora <SQF> <2014-05-22>  for NEW_UI end                
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        
		if(NEW_UI){
			Bundle pData = new Bundle();
			pData.putString(PhotoPage.KEY_MEDIA_SET_PATH,
					mMediaSetPath.toString());
			mPhotoPage = mActivity.getStateManager().createPhotoPage(pData);
			mPhotoPage.createDirect(pData,null,mRootPane);
		}
		
		//mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//Iuni <lory><2014-04-01> add begin
        //lory add sart
        /*
        if (mIsRootState) {
        	if (mActivity.getFragmentManager().findFragmentByTag(TAG) == null) {
        		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//Iuni <lory><2013-12-31> add begin
        		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
        		mFragment = new GridViewFragment();
        		if (mGetContent || mIsGnCrop) {
        			mFragment.SetAlbumPageDate(mAlbumDataAdapter, mMediaSetPath, mData, mGetContent, mIsGnCrop);
				} else {
					mFragment.SetAlbumPageMediaSet(mAlbumDataAdapter, mMediaSet);
				}
        		
        		View fragmentview = mActivity.findViewById(R.id.fragment_container);
        		if (fragmentview != null) {
        			ft.add(R.id.fragment_container, mFragment, TAG);
                    //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    //ft.addToBackStack(null);
            		ft.commit();
				}
        		
            }
        }
        */
        //lory add end

    }



	//Aurora <paul> <2014-02-27> for NEW_UI begin
	private PhotoPage mPhotoPage;
	private Rect mAnimRect;
	private Bitmap mDrawingBmp;
	//Aurora <SQF> <2014-04-17>  for NEW_UI begin
	private int mDrawingBmpRotation;
	//Aurora <SQF> <2014-04-17>  for NEW_UI end
	private PhotoFallbackEffect mLeavingEffect;
	private PhotoFallbackEffect mEnteringEffect;
	private int mOpenAnimFlag = state.IDLE;
	private int mCurrentIndex;
	private int mViewH;
	private int mViewW;
	private boolean mIsFullScreen = false;
	
    //Aurora <SQF> <2014-6-18>  for NEW_UI begin
	private boolean mWillShowActionBar = true;
	//Aurora <SQF> <2014-6-18>  for NEW_UI end
	private int mViewFocusIndex = 0;
	
	private int mSlotIndex;//Iuni <lory><2014-03-12> add begin
	
    //Aurora <SQF> <2014-6-18>  for NEW_UI begin
	public void setWillShowActionBar(boolean willShow) {
		mWillShowActionBar = willShow;
	}
	//Aurora <SQF> <2014-6-18>  for NEW_UI end
	

    private class LoadBitmapDataTask implements Job<Bitmap> {
        MediaItem mItem;

        public LoadBitmapDataTask(MediaItem item) {
            mItem = item;
        }
        @Override
        public Bitmap run(JobContext jc) {
        
            return mItem == null
                    ? null
                    : mItem.requestImage(MediaItem.TYPE_CUST01).run(jc);
              
        }
    }

	private Future<Bitmap> mLoadBitmapTask;
	private void loadbmp(MediaItem item){
		mLoadBitmapTask = mActivity.getThreadPool().submit(new LoadBitmapDataTask(item),
				new FutureListener<Bitmap>() {
			@Override
			public void onFutureDone(Future<Bitmap> future) {
				mLoadBitmapTask = null;
				Bitmap bitmap = future.get();
				if (future.isCancelled()) {
					if (bitmap != null) bitmap.recycle();
					return;
				}

				bitmap = BitmapUtils.fitBitmapFullScreen(bitmap,mViewW,mViewH,true);
				
				mHandler.sendMessage(mHandler.obtainMessage(
						MSG_BITMAP, bitmap));
			}
		});

	}
	

    public PhotoFallbackEffect buildFallbackEffectEx(GLView root, GLCanvas canvas) {
        PhotoFallbackEffect effect = new PhotoFallbackEffect(mViewW,mViewH);
		int i = 0;
        MediaItem item = mAlbumDataAdapter.get(mSlotIndex);
        
        if (item == null || mDrawingBmp == null) return effect;
        if(mDrawingBmp.isRecycled()) return effect;
		
        ScreenNail sc = new BitmapScreenNail(mDrawingBmp);

        int width = sc.getWidth();
        int height = sc.getHeight();

		int rotation = item.getFullImageRotation();
		RawTexture texture;
		int displayWidth;
		int displayHeight;
		if ((rotation % 180) == 0) {
			texture = new RawTexture(width, height, true);
			canvas.beginRenderTarget(texture);
			canvas.translate(width / 2f, height / 2f);
			displayWidth = width;
			displayHeight = height;
		} else {
			texture = new RawTexture(height, width, true);
			canvas.beginRenderTarget(texture);
			canvas.translate(height / 2f, width / 2f);
			float scale = Math.min((float)mViewH/width,(float)mViewW/height);
			displayWidth = (int)(height * scale);
			displayHeight = (int)(width * scale);
		}

		int l = ((mViewW + 1) >> 1) - ((displayWidth + 1) >> 1);
		int t = ((mViewH + 1) >> 1) - ((displayHeight + 1) >> 1);

		Rect rect = new Rect(l,t,displayWidth + l,displayHeight + t);
		

		canvas.rotate(rotation, 0, 0, 1);
		canvas.translate(-width / 2f, -height / 2f);

        sc.draw(canvas, 0, 0, width, height);
        canvas.endRenderTarget();
        effect.addEntry(item.getPath(), rect, texture);

		mDrawingBmp = null;
		sc.recycle();

        return effect;
    }


	public static class state{
		public static final int IDLE = 0;
		public static final int LOAD = IDLE + 1;
		public static final int BUILDOPEN = LOAD + 1;
		public static final int BUILDCLOSE = BUILDOPEN + 1;
		public static final int DRAWING = BUILDCLOSE + 1;
	}
	/*
	public void onTouchDown(int index) {

	}
	
	public void onTouchUp(int index) {

	}
	public void onTouchCancel() {


	}
	*/
	private void resetState(){
		//Aurora <SQF> <2014-05-17>  for NEW_UI begin
		//ORIGINALLY:
		//mOpenAnimFlag = state.IDLE;
		//SQF MODIFIED TO:
		setOpenAnimState(state.IDLE);
		//Aurora <SQF> <2014-05-17>  for NEW_UI end
		mIsFullScreen = false;
		mSlotView.setUnrenderIndex(-1);
	}
	
	//wenyongzhe 2015.10.8 全屏切换
	private void full(boolean enable) {
		   if (enable) {
		      WindowManager.LayoutParams lp =  mActivity.getWindow().getAttributes();
		      lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		      mActivity.getWindow().setAttributes(lp);
		      SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_WHITE, mActivity);
		   } else {
		      WindowManager.LayoutParams attr = mActivity.getWindow().getAttributes();
		      attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		      mActivity.getWindow().setAttributes(attr);
		      SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, mActivity);
		   }
		}
	
	
	//Aurora <SQF> <2014-05-17>  for NEW_UI begin
	private void setOpenAnimState(int state) {
		mOpenAnimFlag = state;
		mAlbumView.notifyEnteringLeavingEffectState(state);
	}
	//Aurora <SQF> <2014-05-17>  for NEW_UI end
	
	
    //Aurora <SQF> <2014-08-08>  for NEW_UI begin
	public boolean isShowingPhotoView() {
		boolean isShowingPhotoView = mPhotoPage != null && mPhotoPage.getPhotoView() != null && 
				mPhotoPage.getPhotoView().getVisibility() == GLView.VISIBLE;
		return isShowingPhotoView;
	}
    //Aurora <SQF> <2014-08-08>  for NEW_UI end
	public void doMulSelect(int selStart, int selEnd, boolean toSel){
		if(null != mSelectionManager){
			for(int i = selStart; i <= selEnd; ++i){
				MediaItem item = mAlbumDataAdapter.get(i);
				if (item == null){
					continue;
				}
				mSelectionManager.select(item.getPath(), toSel, item.isFavorite());
			}
		}
	}
	public boolean isItemSelected(int index){
		if(null != mSelectionManager){
			MediaItem item = mAlbumDataAdapter.get(index);
			if (item != null) {
				return mSelectionManager.isItemSelected(item.getPath());
			}
		}
		return false;
	}

	
	
	//Aurora <paul> <2014-02-27> for NEW_UI end
    @Override
    protected void onResume() {
		// Aurora <paul> <2014-05-06> start
    	//wenyongzhe 2015.11.2 setBaiduTaskListener start
    	 BaiduAlbumUtils util = BaiduAlbumUtils.getInstance(mActivity);
 		util.setBaiduTaskListener(AlbumPage.this);
 		//wenyongzhe 2015.11.2 setBaiduTaskListener end
 		
    	//wenyongzhe 2015.10.8
		full(mIsFullScreen);
		
		if(mNeedAnim){ 
			mNeedAnim = false;
			mSlotView.startRisingAnimation();
		}
		// Aurora <paul> <2014-05-06> end
	    if(NEW_UI){
	    	mPhotoPage.onResume();
	    } 
	    
        super.onResume();
        
        mIsActive = true;
		/*
	        mResumeEffect = mActivity.getTransitionStore().get(KEY_RESUME_ANIMATION);
	        if (mResumeEffect != null) {
	            mAlbumView.setSlotFilter(mResumeEffect);
	            mResumeEffect.setPositionProvider(mPositionProvider);
	            mResumeEffect.start();
	        }
		*/
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	if (mAlbumDataAdapter != null) {
				mAlbumDataAdapter.setFlagToNotUpdateViews(true);
			}
		}
        
		//if(!mGetContent)
		/*
        if(mIsRootState)
		{
        	if (mAuroraActionBar == null) {
        		//wenyongzhe 2016.1.6 new_ui
        		 mAuroraActionBar = 	((GalleryAppImpl)mActivity.getApplicationContext()).getmAuroraActionBar();
			}
			if(mAuroraActionBar != null)
			{
				if (mAuroraActionBar.getVisibility() == View.GONE) {
					mAuroraActionBar.setVisibility(View.VISIBLE);
				}
			}
		}
		*/
        setContentPane(mRootPane);

        boolean enableHomeButton = (mActivity.getStateManager().getStateCount() > 1) |
                mParentMediaSetString != null;
        
        if (!MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	GalleryActionBar actionBar = mActivity.getGalleryActionBar();
            actionBar.setDisplayOptions(enableHomeButton, false);
            if (!mGetContent) {
                //lory del and modify actionBar.enableAlbumModeMenu(GalleryActionBar.ALBUM_GRID_MODE_SELECTED, this);
            	actionBar.disableAlbumModeMenu(true);
            }
		}
        // Set the reload bit here to prevent it exit this page in clearLoadingBit().
        setLoadingBit(BIT_LOADING_RELOAD);
        mLoadingFailed = false;
        mAlbumDataAdapter.resume();
        mAlbumView.resume();
        mAlbumView.setPressedIndex(-1);
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	//mAuroraActionModeHandler.resume();
		} else {
			mActionModeHandler.resume();
		}
        if (!mInitialSynced) {
            setLoadingBit(BIT_LOADING_SYNC);
            mSyncTask = mMediaSet.requestSync(this);
        }
        
        mInCameraAndWantQuitOnPause = mInCameraApp;

		if(mSelectionManager.resume()) {//paul add
			int now = mMode;
			mMode = SelectionManager.FAVORITE_MODE_INIT;
			onFavoriteModeChange(now);
		}
    }

	
    @Override
    protected void onPause() {
		if(NEW_UI){
			
			//wenyongzhe 2015.10.8
			full(false);
			
			mPhotoPage.onPause();
			mSlotView.setUnrenderIndex(-1);
			if (mLoadBitmapTask != null && !mLoadBitmapTask.isDone()) {
				// load in progress, try to cancel it
				resetState();
				mLoadBitmapTask.cancel();
				mLoadBitmapTask.waitDone();
			}
		}
		/*
    	if (mFragment != null) {
    		if (mEnterPhotoPage) {
    			if (mFragment.isVisible()) {
    				mFragment.onPause();
    				mFragment.setBottomActionBarStatus(false);
    				mFragment.setFragmentHide(true);
    				FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
    	    		ft.hide(mFragment);
    	    		ft.commit();
    			}
    			mEnterPhotoPage = false;
			} else {
				mFragment.setFragmentHide(false);
			}
			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);//Iuni <lory><2013-12-17> add begin
		}
		*/
        super.onPause();
        mIsActive = false;
        mAlbumView.setSlotFilter(null);
		if(!mActivity.isFromXCloudAlbumMultiSelection()){//paul add
			mSelectionManager.pause();
		}
        //Aurora <SQF> <2014-6-23>  for NEW_UI begin
        mSlotView.stopScroll();
        //Aurora <SQF> <2014-6-23>  for NEW_UI end

        mAlbumDataAdapter.pause();
        mAlbumView.pause();
        DetailsHelper.pause();
        

    	//if (!mGetContent) {
            //mAuroraActionBar.disableAlbumModeMenu(true);
        //}

        
        if (mSyncTask != null) {
            mSyncTask.cancel();
            mSyncTask = null;

            clearLoadingBit(BIT_LOADING_SYNC);
        }
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	//mAuroraActionModeHandler.pause();
		} else {
			mActionModeHandler.pause();
		}
        
        mActivity.dismissUploadProgress();
    }

    @Override
    protected void onDestroy() {
    	if(FeatureConfig.UI_REFRESH_ADVANCED){
    		mAlbumView.stop();
    	}

    	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
    		if (mSelectionManager.inSelectionMode()) {
                mSelectionManager.leaveSelectionMode();
            }
        	mAuroraActionModeHandler.pause();
		}
    	
		if(NEW_UI){
			mPhotoPage.onDestroy();
		}
	
        super.onDestroy();
        if (mAlbumDataAdapter != null) {
            mAlbumDataAdapter.setLoadingListener(null);
        }

		if(mActivity != null) {
			mActivity.setIsFromXCloudAlbumMultiSelection(false);//mFromXCloudAlbumMultiSelection = false;//SQF ADDED ON 2015.5.18
		}

    }
    
    public boolean isRootView() {
        return mIsRootState;
    }

    private void initializeViews() {
        mSelectionManager = new SelectionManager(mActivity, true);//paul modify <2015-09-15> for BUG #16513 false
        mSelectionManager.setSelectionListener(this);
        Config.AlbumPage config = Config.AlbumPage.get(mActivity);
        mSlotView = new SlotView(mActivity, config.slotViewSpec);
        mSlotView.setAlbumPage(this);

        //wenyongzhe 2015.12.3
//        mScrollBarView = new ScrollBarPanelView(mActivity,100,200);
		//wenyongzhe 2015.12.3
		
        mActionBarHight = config.slotViewSpec.actionbarHeight;
		if(mIsRootState)
		{
			mSlotView.setVisibility(View.GONE);
		}
		
        mAlbumView = new AlbumSlotRenderer(mActivity, mSlotView,
                mSelectionManager, config.placeholderColor);
        
        //mRootPane.addComponent(mSlotView);
        if (!mIsRootState) {
        	mSlotView.setSlotRenderer(mAlbumView);
        	mSlotView.setListener(new SlotView.SimpleListener() {
        		//Iuni <lory><2014-03-12> add begin
        		@Override
				public void onAuroraDown(int index, int noheadindex) {
					AlbumPage.this.onAuroraDown(index, noheadindex);
				}

				@Override
				public void onAuroraSingleTapUp(int index, int noheadindex) {
					AlbumPage.this.onAuroraSingleTapUp(index, noheadindex);
				}

				@Override
				public void onAuroraLongTap(int index, int noheadindex) {
					AlbumPage.this.onAuroraLongTap(index, noheadindex);
				}
        		//Iuni <lory><2014-03-12> add end
        		
                @Override
                public void onDown(int index) {
                    //AlbumPage.this.onDown(index);//Iuni <lory><2014-03-12> add begin
                }

				@Override
                public void onUp(boolean followedByLongPress) {
                    AlbumPage.this.onUp(followedByLongPress);
                }

                @Override
                public void onSingleTapUp(int slotIndex) {
                    //AlbumPage.this.onSingleTapUp(slotIndex);//Iuni <lory><2014-03-12> add begin
                }

                @Override
                public void onLongTap(int slotIndex) {
                    //AlbumPage.this.onLongTap(slotIndex);//Iuni <lory><2014-03-12> add begin
                }
				
				@Override
				public void onScrollPosition(int firstVisibleItem, int bottom, boolean bheader) {
				    //Aurora <SQF> <2014-6-19>  for NEW_UI begin
					//SQF ANNOTATED ON 2014.6.19
					//AlbumPage.this.onScrollPosition(firstVisibleItem, bottom, bheader);
				    //Aurora <SQF> <2014-6-19>  for NEW_UI end
					 
				}

				//wenyongzhe 2015.12.4 scrollbar 
//				@Override
//				public void onScrollPositionChanged(int position, int total) {
//					//TODO Auto-generated method stub
//					super.onScrollPositionChanged(position, total);
//					mScrollBarView.setContentPosition(position, total);
//				}
                
            });
		}
        
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	mAuroraActionModeHandler = new AuroraActionModeHandler(mActivity, mSelectionManager);
		} else {
			mActionModeHandler = new ActionModeHandler(mActivity, mSelectionManager);
	        if (!mIsRootState) {
	        	 mActionModeHandler.setActionModeListener(new ActionModeListener() {
	                 @Override
	                 public boolean onActionItemClicked(MenuItem item) {
	                	 if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
	                		 return true;
						} else {
							return onItemSelected(item);
						}
	                 }
	             });
			}
		}
        //mRootPane.addComponent(mSlotView); paul del, added in initializeData
    }

    private void initializeData(Bundle data) {

    	//Log.i("SQF_LOG", "initializeData : mFromXCloudAlbumMultiSelection: " + mFromXCloudAlbumMultiSelection);
        mMediaSetPath = Path.fromString(data.getString(KEY_MEDIA_PATH));
        mParentMediaSetString = data.getString(KEY_PARENT_MEDIA_PATH);
        mMediaSet = mActivity.getDataManager().getMediaSet(mMediaSetPath);
        if (mMediaSet == null) {
            Utils.fail("MediaSet is null. Path = %s", mMediaSetPath);
        }
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mAlbumDataAdapter = new AlbumDataLoader(mActivity, mMediaSet);
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	mAlbumDataAdapter.setActionModeHandler(mAuroraActionModeHandler);
        	if (mGetContent) {
        		mAlbumDataAdapter.setBundleData(mData, mGetContent);
			}
		}
        
		//mAlbumDataAdapter.setStartMode(mIsRootState);//Iuni <lory><2014-01-06> del begin
        mAlbumDataAdapter.setLoadingListener(new MyLoadingListener());
        mAlbumView.setModel(mAlbumDataAdapter);
		
		mRootPane.addComponent(mSlotView);//Aurora <paul> <2014-05-08> add
//		mRootPane.addComponent(mScrollBarView);
    }

    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mActivity, mRootPane, mDetailsSource);
            mDetailsHelper.setCloseListener(new CloseListener() {
                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
        mAlbumView.setHighlightItemPath(null);
        mSlotView.invalidate();
    }

    @Override
    protected boolean onCreateActionBar(Menu menu) {
    	if (!mIsRootState) 
    	{
    		if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
    			initCustomActionBar();
			} else {
				GalleryActionBar actionBar = mActivity.getGalleryActionBar();
	            MenuInflater inflator = getSupportMenuInflater();
	            if (mGetContent) {
	                inflator.inflate(R.menu.pickup, menu);
	                int typeBits = mData.getInt(Gallery.KEY_TYPE_BITS,
	                        DataManager.INCLUDE_IMAGE);
	                actionBar.setTitle(GalleryUtils.getSelectionModePrompt(typeBits));
	            } else {
	                inflator.inflate(R.menu.album, menu);
	                actionBar.setTitle(mMediaSet.getName());
	                menu.findItem(R.id.action_slideshow)
	                        .setVisible(!(mMediaSet instanceof MtpDevice));

	                FilterUtils.setupMenuItems(actionBar, mMediaSetPath, true);

	                menu.findItem(R.id.action_group_by).setVisible(mShowClusterMenu);
	                menu.findItem(R.id.action_camera).setVisible(
	                        MediaSetUtils.isCameraSource(mMediaSetPath)
	                        && GalleryUtils.isCameraAvailable(mActivity));

	            }
	            actionBar.setSubtitle(null);
			}
		}
        
        return true;
    }

    private void prepareAnimationBackToFilmstrip(int slotIndex) {
        if (mAlbumDataAdapter == null || !mAlbumDataAdapter.isActive(slotIndex)) return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null) return;
        TransitionStore transitions = mActivity.getTransitionStore();
        transitions.put(PhotoPage.KEY_INDEX_HINT, slotIndex);
        transitions.put(PhotoPage.KEY_OPEN_ANIMATION_RECT,
                mSlotView.getSlotRect(slotIndex, mRootPane));
    }

    private void switchToFilmstrip() {
        if (mAlbumDataAdapter.size() < 1) return;
        int targetPhoto = mSlotView.getVisibleStart();
        prepareAnimationBackToFilmstrip(targetPhoto);
        if(mLaunchedFromPhotoPage) {
            onBackPressed();
        } else {
            pickPhoto(targetPhoto, true);
        }
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onUpPressed();
                return true;
            }
            case R.id.action_cancel:
                mActivity.getStateManager().finishState(this);
                return true;
            case R.id.action_select:
                mSelectionManager.setAutoLeaveSelectionMode(false);
                mSelectionManager.enterSelectionMode();
                return true;
            case R.id.action_group_by: {
            	if (!MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		mActivity.getGalleryActionBar().showClusterDialog(this);
				}
                
                return true;
            }
            case R.id.action_slideshow: {
                mInCameraAndWantQuitOnPause = false;
                Bundle data = new Bundle();
                data.putString(SlideshowPage.KEY_SET_PATH,
                        mMediaSetPath.toString());
                data.putBoolean(SlideshowPage.KEY_REPEAT, true);
                mActivity.getStateManager().startStateForResult(
                        SlideshowPage.class, REQUEST_SLIDESHOW, data);
                return true;
            }
            case R.id.action_details: {
                if (mShowDetails) {
                    hideDetails();
                } else {
                    showDetails();
                }
                return true;
            }
            case R.id.action_camera: {
                GalleryUtils.startCameraActivity(mActivity);
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    protected void onStateResult(int request, int result, Intent data) {
    	//Log.i("SQF_LOG", "on State Result : request:" + request);
        switch (request) {
        //SQF ADDED 2015-4-24 BEGIN
        case REQUEST_ACCOUNT_LOGIN:
        	String token = mActivity.getAccountHelper().user_id;
        	//Log.i("SQF_LOG", "AlbumPage::onStateResult ----> REQUEST_ACCOUNT_LOGIN token:" + token);
        	if(TextUtils.isEmpty(token)) {
        		return;
        	}
        	PrefUtil.setString(mActivity, PREF_KEY_IUNI_ACCOUNT_TOKEN, token);
        	BaiduAlbumUtils.getInstance(mActivity).setBaiduinterface(this);
        	if(AlbumConfig.IUNI_TEST) {
        		token = AlbumConfig.IUNI_TEST_TOKEN;
        	}
        	mActivity.showLoginBaiduAuroraProgressDialog();
        	BaiduAlbumUtils.getInstance(mActivity).loginBaidu(token, false);
        	break;
        //SQF ADDED 2015-4-24 END
        
        case MSG_ANIMATION_INVIEW:
        	mSlotView.startRisingAnimation();
        	//mSlotView.startScatteringAnimation(mOpenCenter);
        	break;
        	
            case REQUEST_SLIDESHOW: {
                // data could be null, if there is no images in the album
                if (data == null) return;
                mFocusIndex = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
                mSlotView.setCenterIndex(mFocusIndex);
                break;
            }
            case REQUEST_PHOTO: {
            	if (mIsRootState) {
            	    //Aurora <SQF> <2014-6-19>  for NEW_UI begin
            	    //SQF ANNOTATED ON 2014.6.19
            		/*
            		if(mFragment != null)
					{
						//mFragment.OnInitResumeGridview();//Iuni <lory><2014-01-04> del begin
					}
					*/
            		//Aurora <SQF> <2014-6-19>  for NEW_UI end
				}
            	
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		if (mAlbumDataAdapter != null) {
        				mAlbumDataAdapter.setFlagToNotUpdateViews(true);
        			}
            		ShowAnimationActionBar((View)mAuroraActionBar);
				}
            	
                if (data == null) return;
                mFocusIndex = data.getIntExtra(PhotoPage.KEY_RETURN_INDEX_HINT, 0);
                
                int tindex = mFocusIndex;
                mFocusIndex = getIndexInSlotView(tindex);
				if(!mIsRootState)
				{
					mSlotView.makeSlotVisible(mFocusIndex);
				}
				/*
				if (mIsRootState) {
					if(mFragment != null)
					{
						mFragment.setFoucsIndex(mFocusIndex);//Iuni <lory><2014-01-07> add begin
					}
				}
				*/
                break;
            }
            case REQUEST_DO_ANIMATION: {
                mSlotView.startRisingAnimation();
            	//mFocusIndex = data.getIntExtra(PhotoPage.KEY_RETURN_INDEX_HINT, 0);
            	//mSlotView.startScatteringAnimation(mOpenCenter);
                break;
            }
            //Aurora <SQF> <2014-6-17>  for NEW_UI begin
            case REQUEST_EDIT:
            	if(data != null) {
            		if(data.getBooleanExtra(FilterShowActivity.INTENT_DATA_BACK_TO_PHOTOLIST, false)) {
            			resetState();
            			setWillShowActionBar(false);
            			mPhotoPage.hideBarsDirectly();
            			mPhotoPage.setToFinishPhotoPage(true);
                		onBackPressed();
                		mPhotoPage.setToFinishPhotoPage(false);
                		setWillShowActionBar(true);
                		mSlotView.setScrollPosition(0);
                		return;
                	}
            		
	            	resetState();
	            	setWillShowActionBar(false);
	            	mPhotoPage.setCurrentPhotoByIntentEx(data);
	            	setWillShowActionBar(true);
            	} else {
            		setWillShowActionBar(false);
            	}
            	break;
            	//mIsFullScreen = true;
            	//setFilterShowActivitySavedPhotoInfo(data);
            	//mPhotoPage.setPhotoIndex(0);
            //Aurora <SQF> <2014-6-17>  for NEW_UI end
        }
    }

    private int getIndexInSlotView(int position){
    	int index = position;
    	int header = 0;
    	
    	if (mAlbumDataAdapter == null || position <= 0) {
			return 1;
		}
    	header = mAlbumDataAdapter.getHeaderNumByIndex(position+1);
    	index += header;
    	return index;
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
				if(!mActivity.isFromXCloudAlbumMultiSelection()){
					mViewpagerActivity.subActivitySelectionModeChange(true);
				}
				ShowOrHideAuroraActionBar(true);
//				mLastSelectedCount = -1;//wenyongzhe 2016.3.15 disable
				/*
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		mAuroraActionModeHandler.startActionMode();
				} else {
					mActionModeHandler.startActionMode();
				}
				*/
                //if (mHapticsEnabled) mVibrator.vibrate(100);
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
				if(!mActivity.isFromXCloudAlbumMultiSelection()){
					mViewpagerActivity.subActivitySelectionModeChange(false);
				}
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		if (mAlbumDataAdapter != null) {
        				mAlbumDataAdapter.setFlagToNotUpdateViews(true);
        			}
            		mAuroraActionModeHandler.finishActionMode();
            		mbDeletedMode = true;
                	ShowOrHideAuroraActionBar(false);
				} else {
					mActionModeHandler.finishActionMode();
				}
				mRootPane.invalidate();
                break;
            }
            case SelectionManager.SELECT_ALL_MODE: {
            	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		mAuroraActionModeHandler.updateSupportedOperation();
				} else {
					mActionModeHandler.updateSupportedOperation();
				}
            	UpdateAllMenuStatus(true);
                mRootPane.invalidate();
                break;
            }
            
            case SelectionManager.UNSELECT_ALL_MODE:{
            	UpdateAllMenuStatus(false);
            	mRootPane.invalidate();
            	break;
            }
        }
    }

//	private int mLastSelectedCount = -1;//paul add   //wenyongzhe 2016.3.15 disable
    @Override
    public void onSelectionChange(Path path, boolean selected) {
    	int count = mSelectionManager.getSelectedCount();
        /*if (!MySelfBuildConfig.USEGALLERY3D_FLAG) {
            String format = mActivity.getResources().getQuantityString(
                    R.plurals.number_of_items_selected, count);
            mActionModeHandler.setTitle(String.format(format, count));
            mActionModeHandler.updateSupportedOperation(path, selected);
		} else*/ {
			
			//wenyongzhe 2016.3.15 disable
//			if (count == 0) {
//				UpdateAllMenuStatus(false);
//			} else if(mLastSelectedCount <= 0){
//				UpdateAllMenuStatus(true);
//			}
//			mLastSelectedCount = count;
			
			//wenyongzhe 2016.3.15 add
			if (count > 0) {
				UpdateAllMenuStatus(true);
			} else {
				UpdateAllMenuStatus(false);
			}
				
			updateRightButton(count);
		}
        
        return;
    }

    @Override
    public void onSyncDone(final MediaSet mediaSet, final int resultCode) {
        ((Activity) mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GLRoot root = mActivity.getGLRoot();
                root.lockRenderThread();
                mSyncResult = resultCode;
                try {
                    if (resultCode == MediaSet.SYNC_RESULT_SUCCESS) {
                        mInitialSynced = true;
                    }
                    clearLoadingBit(BIT_LOADING_SYNC);
                    showSyncErrorIfNecessary(mLoadingFailed);
                } finally {
                    root.unlockRenderThread();
                }
            }
        });
    }

    // Show sync error toast when all the following conditions are met:
    // (1) both loading and sync are done,
    // (2) sync result is error,
    // (3) the page is still active, and
    // (4) no photo is shown or loading fails.
    private void showSyncErrorIfNecessary(boolean loadingFailed) {
        if ((mLoadingBits == 0) && (mSyncResult == MediaSet.SYNC_RESULT_ERROR) && mIsActive
                && (loadingFailed || (mAlbumDataAdapter.size() == 0))) {
            Toast.makeText(mActivity, R.string.sync_album_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

	//paul add <2015-09-15> for BUG #16513 start
	private void setTotal(int total){
		Log.i(TAG, "setTotal:" + total);
		m_totalnum = total;
		if(null == mSelectionManager){
			Log.e(TAG, "setTotal null");
			return;
		}
		mSelectionManager.setTotalCount(total);
	}
	//paul add <2015-09-15> for BUG #16513 end
	
    //private static long g_time1 = 0;
    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;
        
        int total = mAlbumDataAdapter.size();
        //Iuni <lory><2014-01-04> add begin
        /*
        if (mFragment != null && mAlbumDataAdapter != null) {
			mFragment.setDirtyContentObserver(total);
		}
		*/
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	if (mAlbumDataAdapter.getStartScanner()) {
        		boolean rflag = total>0?false:true;
            	showEmptyViews(rflag);
			}
        	
        	if (mbDeletedMode) {
        		mbDeletedMode = false;
        		mRootPane.invalidate();
			}
        	
        	if (m_totalnum != total && m_bfirstEnter && mAuroraActionBar != null) {
				//paul modify <2015-09-15> for BUG #16513
        		//m_totalnum = total;
				setTotal(total);
				/* paul del for BUG #19597
				if(!mGetContent){//paul add
	            	String num = String.format(mActivity.getString(R.string.action_title), total);
	    	    	if (total > 0) {
	    	    		mAuroraActionBar.setTitle(num);
	    			} else {
	    	    		mAuroraActionBar.setTitle(R.string.appwidget_title);
	    			}
				}
				*/
			}
        	
        	m_bfirstEnter = true;
		}
        //Iuni <lory><2014-01-04> add end
        
        if (mLoadingBits == 0 && mIsActive) {
            if (total == 0) {
            	if (mIsRootState) {
					return;
				}
            	if (!MySelfBuildConfig.USEGALLERY3D_FLAG) {
            		Intent result = new Intent();
                    result.putExtra(KEY_EMPTY_ALBUM, true);
                    setStateResult(Activity.RESULT_OK, result);
                    mActivity.getStateManager().finishState(this);
				}
            }
        }
    }
    //Aurora <SQF> <2014-05-21>  for NEW_UI begin
    //ORIGINALLY:
    //private class MyLoadingListener implements LoadingListener {
    //SQF MODIFIED TO:
    public interface DataListener extends LoadingListener {
    	public void hideNoFileView();
    }
    public class MyLoadingListener implements DataListener {
    //Aurora <SQF> <2014-05-21>  for NEW_UI end
        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = false;
        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = loadingFailed;
            showSyncErrorIfNecessary(loadingFailed);
        }
        
        //Aurora <SQF> <2014-05-21>  for NEW_UI begin
        @Override
        public void hideNoFileView() {
            showEmptyViews(false);
        }
        //Aurora <SQF> <2014-05-21>  for NEW_UI end
    }

    private class MyDetailsSource implements DetailsHelper.DetailsSource {
        private int mIndex;

        @Override
        public int size() {
            return mAlbumDataAdapter.size();
        }

        @Override
        public int setIndex() {
            Path id = mSelectionManager.getSelected(false).get(0);
            mIndex = mAlbumDataAdapter.findItem(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            // this relies on setIndex() being called beforehand
            MediaObject item = mAlbumDataAdapter.get(mIndex);
            if (item != null) {
                mAlbumView.setHighlightItemPath(item.getPath());
                return item.getDetails();
            } else {
                return null;
            }
        }
    }

    @Override
    public void onAlbumModeSelected(int mode) {
        if (mode == GalleryActionBar.ALBUM_FILMSTRIP_MODE_SELECTED) {
            switchToFilmstrip();
        }
    }
   
    //Iuni <lory><2014-02-20> add begin
    public void onScrollPosition(int firstVisibleItem, int bottom, boolean bheader) {

    }
    
    private void ShowAnimationActionBar(View view) {
    	if (view == null) {
			return;
		}
    	//Aurora <SQF> <2014-6-19>  for NEW_UI begin
    	if(view.getVisibility() == View.VISIBLE) return;
    	//Aurora <SQF> <2014-6-19>  for NEW_UI end
    	view.clearAnimation();
    	mInAnimation.reset();
    	
    	view.startAnimation(mInAnimation);
    	view.setVisibility(View.VISIBLE);
		return;
	}
    
    private void HideAnimationActionBar(View view) {
    	if (view == null) return;
		//Aurora <SQF> <2014-6-19>  for NEW_UI begin
    	if(view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE) return;
    	//Aurora <SQF> <2014-6-19>  for NEW_UI end
    	view.clearAnimation();
    	mOutAnimation.reset();
    	
    	view.startAnimation(mOutAnimation);
    	view.setVisibility(View.GONE);
		return;
	}
    
    private void ShowAlphaAnimationHeaderView(View view) {
    	if (view == null) return;
    	//Aurora <SQF> <2014-6-19>  for NEW_UI begin
    	if(view.getVisibility() == View.VISIBLE) return;
    	//Aurora <SQF> <2014-6-19>  for NEW_UI end
    	view.clearAnimation();
    	mInAlphaAnimation.reset();
    	
    	view.startAnimation(mInAlphaAnimation);
    	view.setVisibility(View.VISIBLE);
		return;
	}
    
    private void HideAlphaAnimationHeaderView(View view) {
    	if (view == null) {
			return;
		}
        //Aurora <SQF> <2014-6-19>  for NEW_UI begin
    	if(view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE) return;
    	//Aurora <SQF> <2014-6-19>  for NEW_UI end
    	view.clearAnimation();
    	mOutAlphaAnimation.reset();
    	
    	view.startAnimation(mOutAlphaAnimation);
    	view.setVisibility(View.GONE);
		return;
	}
    
    private void ShowOrHideAuroraActionBar(boolean bShow){
    	//if(mFromXCloudAlbumMultiSelection) return;//SQF ADDED ON 2015.5.18
    	
    	if(mActivity != null && mActivity.isFromXCloudAlbumMultiSelection()) {
    		return;
    	}
    	
    	
    	if (mAuroraActionBar == null) {
          //wenyongzhe 2016.1.6 new_ui
    		 mAuroraActionBar = 	((GalleryAppImpl)mActivity.getApplicationContext()).getmAuroraActionBar();
        	if (mAuroraActionBar == null) {
				return;
			}
		}
        try {//wenyongzhe 2015.11.19 bug17047
        	if (bShow) {
        		mAuroraActionBar.setShowBottomBarMenu(true);
        		mAuroraActionBar.showActionBarDashBoard();
        	} else {
        		mAuroraActionBar.setShowBottomBarMenu(false);
        		mAuroraActionBar.showActionBarDashBoard();
        	}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
        
    	return;
    }
    
    private void UpdateActionBarRightButton(boolean flag) {
		if ((mAuroraActionBar != null) && 
			(mAuroraActionBar.getSelectRightButton() != null)) {
			if (flag) {
				((TextView) (mAuroraActionBar.getSelectRightButton())).setText(mActivity.getString(R.string.myselect_all));
			}else {
				((TextView) (mAuroraActionBar.getSelectRightButton())).setText(mActivity.getString(R.string.unmyselect_all));
			}
			mAuroraActionBar.getMiddleTextView()
				.setText(String.format(mActivity.getResources().getString(R.string.adlum_select_num, mSelectionManager.getSelectedCount())));
		
		}
		
		return;
	}
    
	//paul add start
    private void UpdateOkButton(boolean flag) {
		if ((mAuroraActionBar != null) && 
			(mAuroraActionBar.getOkButton() != null)) {
			if (flag) {
				((TextView) (mAuroraActionBar.getOkButton())).setText(mActivity.getString(R.string.myselect_all));
			}else {
				((TextView) (mAuroraActionBar.getOkButton())).setText(mActivity.getString(R.string.unmyselect_all));
			}
		}
		mAuroraActionBar.getMiddleTextView()
			.setText(String.format(mActivity.getResources().getString(R.string.adlum_select_num, mSelectionManager.getSelectedCount())));
		return;
	}

    private void UpdateAllMenuStatus(boolean show) {
		if (mAuroraActionBar == null) {
			return;
		}
		int count = MENU_COUNT;
		if(mActivity.isFromXCloudAlbumMultiSelection()){
			count = 1;
		}
		AuroraMenu auroraMenu = mAuroraActionBar.getAuroraActionBottomBarMenu();
		for(int i = 0; i < count; ++i){
			auroraMenu.setBottomMenuItemEnable(i, show);
		}
		if(!show && count == MENU_COUNT){
			setFavOrNot(true);
		}
	}

	
    public void windowFocusChanged(boolean hasFocus) {
    	if(hasFocus) {
			if(mActivity.isFromXCloudAlbumMultiSelection()){
				mAuroraActionBar = mActivity.getAuroraActionBar();
				UpdateOkButton(true);
		    	mActivity.setAuroraBottomBarMenuCallBack(mActionBarMenuItemCallBack);
				mAuroraActionBar.initActionBottomBarMenu(R.menu.gallery_menu_cloud_request_multi_selection, 1);		
				SetSelectionActionBarBtnListener();
				mAuroraActionBar.showActionBottomeBarMenu();
				mSelectionManager.enterSelectionMode();	
				UpdateAllMenuStatus(false);
			} else {
				mSlotView.resume();
			}
		}
	}
	
	private static final String ACTION_TUYA = "action.picture.tool.tuya";
	/*
	private void launchTuYa(Path path) {
		MediaItem current = (MediaItem) mActivity.getDataManager().getMediaObject(path);
		if (current == null ) {// || (current.getSupportedOperations()
				//& MediaObject.SUPPORT_TUYA) == 0  TYM
			Toast.makeText(mActivity, R.string.tuya_st_msg_nonsupport, Toast.LENGTH_SHORT).show();
			return;
		}

		Intent intent = new Intent(ACTION_TUYA);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("start_single_photo", false);
		intent.setDataAndType(current.getContentUri(), current.getMimeType())
				.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			mActivity.startActivityForResult(intent, REQUEST_TUYA);
		} catch (ActivityNotFoundException e) {
		}
	}
	*/
	//paul add  end
	public void pageSelected(){
		initCustomActionBar();
		mSlotView.invalidate();
	}
    public void initCustomActionBar()
    {
    	if (mGetContent) {
	    	int typeBits = mData.getInt(Gallery.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
	    	AuroraActionBar ab = mActivity.getAuroraActionBar();
			if(null != ab) ab.setTitle(GalleryUtils.getSelectionModePrompt(typeBits));
			return;
		}

    	if(mActivity.isFromXCloudAlbumMultiSelection()) {
			return;
		}
    	if (mAuroraActionBar == null) {
    		mAuroraActionBar = 	((GalleryAppImpl)mActivity.getApplicationContext()).getmAuroraActionBar();
		}
    	
    	if (mAuroraActionBar == null) {
			return;
		}
    	
		if (mAuroraActionBar.getVisibility() != View.VISIBLE) {
			((View)mAuroraActionBar).setVisibility(View.VISIBLE);
		}

		//paul modify <2015-09-15> for BUG #16513
		//m_totalnum = getAllThumbNums();
		setTotal(getAllThumbNums());
		
		int total = m_totalnum;
		
		//wenyongzhe 2016.1.5 viewpager new ui end
		
	    	boolean rflag = total>0?false:true;
	    	showEmptyViews(rflag);
	    	
	    	
	    	if(Globals.OVERSEA_VERSION) {
	    		mAuroraActionBar.initActionBottomBarMenu(R.menu.gallery_menu_oversea, 1);
	    	} else {
	    		mAuroraActionBar.initActionBottomBarMenu(R.menu.gallery_menu, MENU_COUNT);
	    	}
	    	SetActionBarBtnListener();
			
		setMenuCallback();	
    }
    private void setMenuCallback(){
		if(null == mViewpagerActivity){
			mViewpagerActivity = ((ViewpagerActivity)((GalleryAppImpl)mActivity.getApplicationContext()).getmActivityContext());
			mMenuPaddingRight = mActivity.getResources().getDimensionPixelSize(R.dimen.sys_menu_padding_right);
			mMenuPaddingBottom = mActivity.getResources().getDimensionPixelSize(R.dimen.sys_menu_padding_bottom);
		}
	    mViewpagerActivity.setAuroraBottomBarMenuCallBack(mActionBarMenuItemCallBack);
		mViewpagerActivity.setAuroraSystemMenuCallBack(mOptionMenuCallBack);
		mViewpagerActivity.setAuroraMenuItems(R.menu.gallery_albumpage_menu);
		mViewpagerActivity.getAuroraMenu().setAnimationStyle(com.aurora.R.style.AuroraMenuRightBottomAnimation);
	}
    private void showEmptyViews(boolean bshow) {
		setEmptyBackGroud(bshow);//paul add
		mSlotView.setRenderEmptyMessage(bshow);
	}
	
	private AuroraMenuBase.OnAuroraMenuItemClickListener mOptionMenuCallBack = 
			new AuroraMenuBase.OnAuroraMenuItemClickListener() {
			
    	@Override
    	public void auroraMenuItemClick(int itemId) {
    		ArrayList<MediaItem> path = mSelectionManager.getSelectedInfo();//wenyongzhe 2016.3.7
    		switch (itemId) {
				case R.id.menu_upload_to_xcloud: {
					if(!NetworkUtil.checkNetwork(mActivity)){
						Toast.makeText(mActivity, R.string.aurora_album_network_fail, Toast.LENGTH_SHORT).show();
					}else if (!NetworkUtil.checkWifiNetwork(mActivity)) {
						showDialog();
					}else {
						onUploadToXcloud();
						mSelectionManager.leaveSelectionMode();
					}
				}
				break;
				case R.id.menu_move: {
					if(path.size() <= 0) {
						break;
					}
					PhotoPopupWindowUtil popupWindowUtil = PhotoPopupWindowUtil.getInstance(mViewpagerActivity);//wenyongzhe 2016.3.7
					popupWindowUtil.showSelectPopupWindow(mViewpagerActivity.getResources().getString(R.string.aurora_actionbar_move_to), mViewpagerActivity.getCurrentFocus());
					popupWindowUtil.setSelectPath(path);
					mSelectionManager.leaveSelectionMode();	//wenyongzhe 2016.2.17 
				}
				break;
				default:
					break;
			}
    	}
	};
	
    private AuroraMenuBase.OnAuroraMenuItemClickListener mActionBarMenuItemCallBack =
			new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		
    	@Override
    	public void auroraMenuItemClick(int itemId) {
    		switch (itemId) {
			//paul add for UI_20 start
    		case R.id.menu_set_favorite:
			case R.id.menu_cancel_favorite:{
				mAuroraActionModeHandler.onActionFavorite();
				mSelectionManager.leaveSelectionMode();
				break;
			}
    		case R.id.menu_share:{
				String[] mimeType = new String[1];
				mimeType[0] = null;
				ArrayList<Uri> uris = mSelectionManager.getSelected(mimeType);
				String title = mActivity.getResources().getString(R.string.share);
				GalleryUtils.ShareFiles(uris, mActivity.getAndroidContext(), title, mimeType[0]);
				mSelectionManager.leaveSelectionMode();
				break;
			}
    		case R.id.menu_more:{
				mViewpagerActivity.showAuroraMenu(mViewpagerActivity.getWindow().getDecorView(),
						Gravity.RIGHT|Gravity.BOTTOM,
						mMenuPaddingRight,
						mMenuPaddingBottom);
				break;
			}
			//paul add for UI_20 end
			case R.id.menu_delete:{
				//onMenuDelete(itemId);
				if (mAlbumDataAdapter != null) {
					mAlbumDataAdapter.setFlagToNotUpdateViews(false);
				}
				
				mbDeletedMode = false;
				
				//wenyongzhe 2016.3.9
				String confirmMsg;
				mSelectedPaths = mSelectionManager.getSelected(false);
				getSelectedFilePath();
				//wenyongzhe start
				if(mSelectionManager.getSelectedCount() == 1){
					if( !mSelectedFilesAreAllVideo ){
						confirmMsg =  mActivity.getResources().getString(R.string.number_of_files_selected_del_one);
					}else{
						confirmMsg =  mActivity.getResources().getString(R.string.number_of_files_selected_del_movie_one);
					}
				}else{
					if( mSelectedFilesAreAllVideo ){
						confirmMsg =  mActivity.getResources().getString(R.string.number_of_files_selected_del_movie_other,mSelectionManager.getSelectedCount());
					}else if( !mSelectedFilesContainsVideo ){
						confirmMsg =  mActivity.getResources().getString(R.string.number_of_files_selected_del_other,mSelectionManager.getSelectedCount());
					}else{
						confirmMsg =  mActivity.getResources().getString(R.string.number_of_files_selected_del_movie_and_photo_other,mSelectionManager.getSelectedCount());
					}
				}
				//wenyongzhe end
				
				mAuroraActionModeHandler.onActionDelete(R.id.action_delete, confirmMsg, mConfirmDialogListener, true);
				break;
			}
						
			case R.id.menu_confirm_xcloud_multi_select:
				mSelectedPaths = mSelectionManager.getSelected(false);
				((GalleryAppImpl)mActivity.getApplicationContext()).setSelectedFilesForXCloud(getSelectedFilePath());
//				if(!mActivity.ismFromLocalAlbumMultiSelection()){//wenyongzhe2016.3.2
					if(mSelectedFilesAreAllVideo || mSelectedFilesContainsVideo) {
						Toast.makeText(mActivity, R.string.aurora_not_support_move_video, Toast.LENGTH_SHORT).show();
					}
					if(mSelectedFilesAreAllVideo) {
						return;
					}
//				}
				mActivity.setResult(Activity.RESULT_OK, new Intent());
				if(Globals.OVERSEA_VERSION) {
		    		mAuroraActionBar.initActionBottomBarMenu(R.menu.gallery_menu_oversea, 1);
				} else {
					mAuroraActionBar.initActionBottomBarMenu(R.menu.gallery_menu, MENU_COUNT);
				}
				mActivity.finish();
				break;
			//SQF ADDED ON 2014.4.24 end
			default:
				break;
			}
    	}
    };
    
  //wenyongzhe 2015.9.17 checkwifi start
    private void onUploadToXcloud(){
    	mActivity.getAccountHelper().update();
    	
    	//wenyongzhe 2016.2.18 start
    	if(BaiduAlbumUtils.getInstance(mActivity).getAccountInfo() == null ){
    		String token = mActivity.getAccountHelper().user_id;
    		if(TextUtils.isEmpty(token)) {
    			Toast.makeText(mActivity, R.string.aurora_cloud_login, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		PrefUtil.setString(mActivity, PREF_KEY_IUNI_ACCOUNT_TOKEN, token);
    		BaiduAlbumUtils.getInstance(mActivity).setBaiduinterface(this);
    		if(AlbumConfig.IUNI_TEST) {
    			token = AlbumConfig.IUNI_TEST_TOKEN;
    		}
    		BaiduAlbumUtils.getInstance(mActivity).getLoginBaiduInfo(token, false);
    	}
    	//wenyongzhe 2016.2.18 end
		if(!mActivity.getAccountHelper().mLoginStatus 
			|| BaiduAlbumUtils.getInstance(mActivity).getAccountInfo() == null 
			|| AccountProxy.getInstance().hasLogout()){
			Toast.makeText(mActivity, R.string.aurora_cloud_login, Toast.LENGTH_SHORT).show();
			return;
		}

		String confirmMsg =  null;//mActivity.getResources().getQuantityString(R.plurals.upload_to_xcloud_confirm_string, 1);
		mAuroraActionModeHandler.onActionUploadToXCloud(R.id.action_upload_to_xcloud, confirmMsg, null, true);
  }
  	private Dialog mDialog;
  	private void showDialog(){
  		  mDialog = new AuroraAlertDialog.Builder(mActivity)
            .setTitle(R.string.photo_upload_no_wifi_title)//wenyongzhe
            .setMessage(R.string.aurora_cloud_download_dialog_message)
            .setPositiveButton(android.R.string.ok, mOnDownloadConfirmListener)
            .setNegativeButton(R.string.cancel, mOnDownloadCancelListener).create();
  	       mDialog.show();
  	}
  	 private DialogInterface.OnClickListener mOnDownloadConfirmListener = new DialogInterface.OnClickListener() {
  	        @Override
  	        public void onClick(DialogInterface dialog, int whichButton) {
  	            mDialog.dismiss();
  	          onUploadToXcloud();
			  mSelectionManager.leaveSelectionMode();
  	        }
  	    };
      private DialogInterface.OnClickListener mOnDownloadCancelListener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
              mDialog.dismiss();
			  mSelectionManager.leaveSelectionMode();
          }
      };
    //wenyongzhe 2015.9.17 checkwifi end
    
	private boolean mSelectedFilesAreAllVideo = false;
	private boolean mSelectedFilesContainsVideo = false;
	/*
	public ArrayList<String> getSelectedFilePath() {

		ArrayList<String> filePaths = new ArrayList<String>();
		if(mSelectedPaths == null || mSelectedPaths.size() == 0) return filePaths;

		for(int i=0; i<mSelectedPaths.size(); i++) {
			//Log.i("SQF_LOG", "AlbumPage paths:" + mSelectedPaths.get(i));
			MediaObject obj = mActivity.getDataManager().getMediaObject(mSelectedPaths.get(i));
			if(obj instanceof LocalImage) {
				LocalImage image = (LocalImage)obj;
				//Log.i("SQF_LOG", "AlbumPage paths: add-------" + image.filePath);
				filePaths.add(image.filePath);
			}
		}
		return filePaths;
	}
	*/
	public ArrayList<String> getSelectedFilePath() {
		ArrayList<String> filePaths = new ArrayList<String>();
		if(mSelectedPaths == null || mSelectedPaths.size() == 0) return filePaths;
		mSelectedFilesAreAllVideo = true;
		mSelectedFilesContainsVideo = false;
		//for(int i=0; i<mSelectedPaths.size(); i++) {
		for(int i=mSelectedPaths.size() - 1; i >= 0; i--) {
			MediaObject obj = mActivity.getDataManager().getMediaObject(mSelectedPaths.get(i));
			if(obj instanceof LocalImage) {
				LocalImage image = (LocalImage)obj;
				filePaths.add(image.filePath);
				mSelectedFilesAreAllVideo = false;
				//Log.i("SQF_LOG", "AlbumPage::getSelectedFilePath  LocalImage");
			} else if(obj instanceof LocalVideo) {
				//wenyongzhe start
				//LocalVideo image = (LocalVideo)obj;
				//filePaths.add(image.filePath);
				//wenyongzhe end
				mSelectedFilesContainsVideo = true;
				//Log.i("SQF_LOG", "AlbumPage::getSelectedFilePath  LocalVideo");
			}
		}
		return filePaths;
	}
    
    private int getImgThumbs(){
    	int img_num = 0;
    	Cursor imgCursor = null;
		try {
			imgCursor = m_AuroraResolver.query(mImgBaseUri, null, mImgWhereClause, null, null);
			if (imgCursor != null) {
				img_num += imgCursor.getCount();
			}
		} catch (Exception e) {
			Log.e("SQF_LOG", "zll --- getImgThumbs 1 error",e);
		} finally {
			if (imgCursor != null) {
				imgCursor.close();
			}
		}
    	return img_num;
    }
    
    private int getVideoThumbs(){
    	int vedio_num = 0;
    	Cursor videoCursor = null;
		try {
			videoCursor = m_AuroraResolver.query(mVideoBaseUri, null, mVideoWhereClause, null, null);
			if (videoCursor != null) {
				vedio_num += videoCursor.getCount();
			}
		} catch (Exception e) {
			Log.e("SQF_LOG", "zll --- getImgThumbs 2 error", e);
		} finally {
			if (videoCursor != null) {
				videoCursor.close();
			}
		}
    	
    	return vedio_num;
    }
    
    private int getAllThumbNums() {
		int  total = 0;
		
		if (mAlbumDataAdapter == null) {
			return 0;
		}
		
		if (!mGetContent) {
			total += getImgThumbs();
			total += getVideoThumbs();
		} else {
			int typeBits = mData.getInt(Gallery.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
			boolean tflag = false;
			
			if ((typeBits & DataManager.INCLUDE_IMAGE) != 0) {
				total += getImgThumbs();
				tflag = true;
			}
			
			if ((typeBits & DataManager.INCLUDE_VIDEO) != 0) {
				total += getVideoThumbs();
				if (!tflag) {
					m_bOnlyVideo = true;
				}
			}
		}
		return total;
	}
    
    private void SetActionBarBtnListener() {
    	if (mAuroraActionBar == null) {
			return;
		}
    	
    	if (mAuroraActionBar.getSelectLeftButton() != null ) {
    		mAuroraActionBar.getSelectLeftButton().setOnClickListener(new View.OnClickListener() {
        		@Override
        		public void onClick(View v) {
        			if (mSelectionManager.inSelectionMode()) {
        				mSelectionManager.leaveSelectionMode();
					}
        		}
        	});
		}
    	
    	if (mAuroraActionBar.getSelectRightButton() != null) {
    		mAuroraActionBar.getSelectRightButton().setOnClickListener(new View.OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			if (mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {//auroraIsEntryEditModeAnimRunning
						return;
					}
        			if (mSelectionManager.inSelectedAllItem()) {
        				mSelectionManager.unSelectAll();
						UpdateActionBarRightButton(true);
					} else {
						mSelectionManager.selectAll();
						UpdateActionBarRightButton(false);
					}
        		}
        	});
		}

	}
	
    //paul add
    private void SetSelectionActionBarBtnListener() {
    	if (mAuroraActionBar == null) {
			return;
		}
    	
    	if (mAuroraActionBar.getCancelButton() != null ) {

    		mAuroraActionBar.getCancelButton().setOnClickListener(new View.OnClickListener() {
        		@Override
        		public void onClick(View v) {
        			mActivity.finish();
        		}
        	});
		}
    	
    	if (mAuroraActionBar.getOkButton() != null) {
    		mAuroraActionBar.getOkButton().setOnClickListener(new View.OnClickListener() {
        		@Override
        		public void onClick(View v) {
        			if (mAuroraActionBar.auroraIsExitEditModeAnimRunning()) {//auroraIsEntryEditModeAnimRunning
						return;
					}
        			
        			if (mSelectionManager.inSelectedAllItem()) {
        				mSelectionManager.unSelectAll();
        				UpdateOkButton(true);
					} else {
						mSelectionManager.selectAll();
						UpdateOkButton(false);
					}
        		}
        	});
		}

	}

    
    private MenuExecutor.ProgressListener mConfirmDialogListener = new MenuExecutor.ProgressListener() {
		
		@Override
		public void onProgressUpdate(int index) {
			mDleted = index;
		}
		
		@Override
		public void onProgressStart() {
		}

		@Override
		public void onProgressComplete(int result) {
			if (mAlbumDataAdapter != null) {
				mAlbumDataAdapter.setFlagToNotUpdateViews(true);
				mAlbumDataAdapter.auroraUpdateDatabase();
			}
			
			if (result == 1) { //success
				String num = String.format(mActivity.getString(R.string.delete_finish), mDleted);
				Toast.makeText(mActivity, num, Toast.LENGTH_SHORT).show();
			} else if (result == 2) {//fail
				//Toast.makeText(getActivity(), num,Toast.LENGTH_LONG).show();
			} else if (result == 3) {//cancel
				//Toast.makeText(getActivity(), num,Toast.LENGTH_LONG).show();
			}
			
		}
		
		@Override
		public void onConfirmDialogShown() {
		}
		
		@Override
		public void onConfirmDialogDismissed(boolean confirmed) {
			//Aurora <SQF> <2014-05-22>  for BUG #4667 begin
			/* paul del for BUG #19597
			if(confirmed) {
				int count = mSelectionManager.getSelectedCount();
				int fakeNum = mAlbumDataAdapter.size() - count;
        		String fakeNumString = String.format(mActivity.getString(R.string.action_title), fakeNum);
        		if (fakeNum > 0) {
    	    		mAuroraActionBar.setTitle(fakeNumString);
    			} else {
    	    		mAuroraActionBar.setTitle(R.string.appwidget_title);
    			}
			}
			*/
			//Aurora <SQF> <2014-05-22>  for BUG #4667 end
		}
	};
	
	private void onAuroraDown(int index, int noheadindex) {
		mAlbumView.setPressedIndex(noheadindex);
    }
    
    public void onAuroraLongTap(int index, int noheadindex) {
        if (mGetContent || mIsFullScreen) return;
        MediaItem item = mAlbumDataAdapter.get(noheadindex);
        if (item == null) return;
        //SQF ADDED 2015.04.29 begin
        mActivity.dismissUploadProgress();
        //SQF ADDED 2015.04.29 end
        //mSelectionManager.setAutoLeaveSelectionMode(false);
        mSelectionManager.toggle(item.getPath(), item.isFavorite());
        mSlotView.invalidate();
    }
    
    private void updateRightButton(int num){
    	if ((mAuroraActionBar != null) && 
    		(mAuroraActionBar.getSelectRightButton() != null)) {
			if (num == m_totalnum) {
				((TextView) (mAuroraActionBar.getSelectRightButton())).setText(mActivity.getString(R.string.unmyselect_all));
			}else {
				((TextView) (mAuroraActionBar.getSelectRightButton())).setText(mActivity.getString(R.string.myselect_all));
			}
    	}
    	
		mAuroraActionBar.getMiddleTextView()
			.setText(String.format(mActivity.getResources().getString(R.string.adlum_select_num, num)));
    	return;
    }
    
    private void onAuroraSingleTapUp(int index, int noheadindex) {
        if (!mIsActive) return;
        if (mSelectionManager.inSelectionMode()) {
        	MediaItem item = mAlbumDataAdapter.get(noheadindex);
            if (item == null) return; // Item not ready yet, ignore the click
			//paul modify start
			/*
			 mSelectionManager.toggle(item.getPath());
			*/
            boolean checked = mSelectionManager.toggle(item.getPath(), item.isFavorite());
			/*
			if(checked){
            	mAlbumView.startCheckAnimation(index);
			}else{
				mAlbumView.startUnCheckAnimation(index);
			}
			*/
			//paul modify end
            mSlotView.invalidate();
        } else {
            // Render transition in pressed state
            if(NEW_UI){
	            pickPhoto(index, noheadindex);
			} else {
				if(mGetContent){
					pickPhoto(index, noheadindex);
					return;
				}
				MediaItem item = mAlbumDataAdapter.get(noheadindex);
				if (item == null) return;
				/*
				if(false){
					launchTuYa(item.getPath());
				} else*/ {
					Intent intent = new Intent();
					intent.setAction("aurora.aciton.photopage.view");
					intent.putExtra("AuroraPhotoPage", true);
					intent.setDataAndType(mActivity.getDataManager().getContentUri(item.getPath()), item.getMimeType());
					mActivity.startActivity(intent);
				}
			}
        }
    }
    //Iuni <lory><2014-02-20> add end
    
    //Aurora <SQF> <2014-07-30>  for NEW_UI begin
	@Override
	protected void onConfigurationChanged(Configuration config) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(config);
		if(mPhotoPage != null) {
			mPhotoPage.onConfigurationChanged(config);
		}
	}
	//Aurora <SQF> <2014-07-30>  for NEW_UI end
    
	
	//Aurora <SQF> <2015-04-24>  for NEW_UI begin
	@Override
	public void changeAccount() {
		Log.i("SQF_LOG", "IUNI ACCOUNT: AlbumPage::changeAccount");
	}
	
	@Override
	public void unLogin() {
		Log.i("SQF_LOG", "IUNI ACCOUNT: AlbumPage::unLogin");
	}
	
	@Override
	public void loginComplete(boolean success) {
		//baidu login complete
		//Log.i("SQF_LOG", "baidu login complete success:" + success);
		mActivity.dismissAuroraProgressDialog();
		if(! success) {
			Toast.makeText(mActivity, R.string.baidu_cloud_login_failed, Toast.LENGTH_SHORT).show();
			return;
		}
		XCloudTaskListenerManager.getInstance(mActivity).sendGetPhotoTaskListDelayed();
		
		String confirmMsg =  null;//mActivity.getResources().getQuantityString(R.plurals.upload_to_xcloud_confirm_string, 1);
		//mAuroraActionModeHandler.onActionUploadToXCloud(R.id.action_upload_to_xcloud, confirmMsg, mConfirmUploadListener, true);//wenyongzhe disable
	}

	@Override
	public void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath, CommonFileInfo info) {
		mCommonFilelist = list;
		return;
	}
	//Aurora <SQF> <2015-04-24>  for NEW_UI end

	//wenyongzhe add 2015.9.24 add baiduTaskListener start
	//wenyongzhe 2015.11.2  Toash&&Scan  start
	@Override
	public void baiduTaskStatus(FileTaskStatusBean bean) {
//		if(mActivity == null ){
//			return;
//		}
//		DownloadTaskListManager manager = ((GalleryAppImpl) mActivity.getApplication()).getDownloadTaskListManager();
//		manager.updateDownloadStatus(bean);
//		if (manager.getDownloadTaskSize() == 0) {
//			sendBroadcastScan();
//		}
//		
//		manager.updateDownloadStatus(bean);
////		FileDownloadInfo info = manager.getCurrentTaskInfo();
//		int complete = manager.getCompleteIndex();
//		int total = manager.getDownloadSize();
//		
//    	if(manager.getDownloadinfos().size()>0){
////    		uploadNotificationNotify(total,complete,manager.getDownloadinfos().get(0).get(0).hashCode(),false);
//    		uploadNotificationNotify(total,complete,111,false);
//    	}
//    	else{
//    		uploadNotificationNotify(total,complete,-1,false);
//    	}
	}

	private NotificationManager mNotifManager;
	private Notification.Builder builder;
	private Notification notification;
	private int mLastCmpIndex = -1;
	private int mHashcode = -1;//wenyongzhe 2016.2.19
	private void uploadNotificationNotify(int total, int complete,int hashCode, boolean isUpload) {   //wenyongzhe 2016.1.30 new_ui
		if(mLastCmpIndex == complete) return;
		if(hashCode==-1){
			hashCode = mHashcode;
		}else{
			mHashcode = hashCode;
		}
		if(builder == null){
			builder = new Notification.Builder(mActivity);
		}
		if(mNotifManager == null){
			mNotifManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mLastCmpIndex = complete;
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
	
		Intent resultIntent = new Intent(mActivity,UploadDownloadListActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(mActivity, 0, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		
		if(total == complete ){ 
			mLastCmpIndex = -1;
			builder.setContentTitle(mActivity.getResources().getString(R.string.aurora_downloaded));
		}else if(total > complete){
			builder.setContentTitle(mActivity.getResources().getString(R.string.aurora_downloading));
		}
		builder.setContentText(complete+"/"+total);
		if(complete==0){
			if(isUpload  ){
				builder.setTicker(mActivity.getResources().getString(R.string.aurora_cloud_notify_uploading));
			}else{
				builder.setTicker(mActivity.getResources().getString(R.string.aurora_cloud_notify_dowloading));
			}
		}else{
		}
		notification = builder.build();
		mNotifManager.notify(hashCode, notification);
	}
	
  
	public void sendBroadcastScan() {
		Intent intent = new Intent();
		intent.setAction(CloudItemFragment.ACTION_DIR_SCAN);
		intent.setData(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory() + "/DCIM/cloud")));
		mActivity.sendBroadcast(intent);
	}
	
	@Override
	public void baiduDownloadTaskStatus(FileTaskStatusBean bean) {
	}

	@Override
	public void baiduUploadTaskStatus(FileTaskStatusBean bean) {
//		UploadTaskListManager manager = ((GalleryAppImpl) mActivity.getApplication()).getUploadTaskListManager();
//		if (manager.getParcelSize() == 0) {
////			sendBroadcastScan();
//		}
//		manager.updateFileUploadTaskInfo(bean);
//		int total = manager.howManyPhotosUnderCurrentParcel();
//    	int complete = manager.completeNumberUnderCurrentParcel();
//    	if(manager.getmUploadTaskList().size()>0){
//    		int index = manager.getCurrentTaskParcelIndex();//wenyongzhe 2016.2.19
////    		uploadNotificationNotify(total,complete,manager.getmUploadTaskList().get(index).get(0).hashCode(), true);
//    		uploadNotificationNotify(total,complete,5, true);
//    	}
//    	else{
//    		uploadNotificationNotify(total,complete,-1,true);
//    	}
	}
	//wenyongzhe add 2015.9.24 baiduTaskListener end
	
	//paul add for UI_20 start
	private int mMode = SelectionManager.FAVORITE_MODE_INIT;
	private void setFavOrNot(boolean fav){
		View v2 = mAuroraActionBar.getAuroraActionBottomBarMenu().getLayoutByPosition(1);
		if(null == v2){
			Log.w(TAG, "setFavOrNot v2 null!");
			return;
		}
		View v3 = mAuroraActionBar.getAuroraActionBottomBarMenu().getLayoutByPosition(2);
		if(null == v3){
			Log.w(TAG, "setFavOrNot v3 null!");
			return;
		}

		if(fav){
			v2.setVisibility(View.VISIBLE);
			v3.setVisibility(View.GONE);
			mMode = SelectionManager.SET_FAVORITE_MODE;
		} else {
			v3.setVisibility(View.VISIBLE);
			v2.setVisibility(View.GONE);
			mMode = SelectionManager.UNSET_FAVORITE_MODE;
		}
	}
	@Override
    public void onFavoriteModeChange(int mode) {
    	//Log.d(TAG,"onFavoriteModeChange:" + mode + " mMode:" + mMode);
		if(mActivity.isFromXCloudAlbumMultiSelection() || mMode == mode){
			return;
		}
        switch (mode) {
            case SelectionManager.FAVORITE_MODE_INIT: {
					mMode = SelectionManager.FAVORITE_MODE_INIT;
					break;
				}
            case SelectionManager.SET_FAVORITE_MODE: {
					setFavOrNot(true);
					mAuroraActionModeHandler.enterFavoriteMode(true);
					break;
				}
            case SelectionManager.UNSET_FAVORITE_MODE: {
					setFavOrNot(false);
					mAuroraActionModeHandler.enterFavoriteMode(false);
					break;
				}
		}

	}
	//paul add for UI_20 end
}
