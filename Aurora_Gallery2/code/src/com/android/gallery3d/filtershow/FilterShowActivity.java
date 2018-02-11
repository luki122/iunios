/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.gallery3d.filtershow;

import android.app.ActionBar;
//import android.app.AlertDialog;
import aurora.app.AuroraAlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
//import android.support.v4.print.PrintHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.CustomActionBarManager.ActionBarType;
//import com.android.gallery3d.R;
//import com.android.gallery3d.app.PhotoPage;
//import com.android.gallery3d.data.LocalAlbum;
import com.android.gallery3d.filtershow.cache.ImageLoader;
import com.android.gallery3d.filtershow.category.Action;
import com.android.gallery3d.filtershow.category.AuroraAction;
import com.android.gallery3d.filtershow.category.AuroraCategoryAdapter;
import com.android.gallery3d.filtershow.category.CategoryAdapter;
import com.android.gallery3d.filtershow.category.CategorySelected;
import com.android.gallery3d.filtershow.category.CategoryView;
import com.android.gallery3d.filtershow.category.MainPanel;
import com.android.gallery3d.filtershow.category.SubCategoryBeautyShotPanel;
import com.android.gallery3d.filtershow.category.SubCategoryFiltersPanel;
import com.android.gallery3d.filtershow.category.SwipableView;
import com.android.gallery3d.filtershow.category.AuroraAction.AuroraActionType;
import com.android.gallery3d.filtershow.data.UserPresetsManager;
import com.android.gallery3d.filtershow.editors.BasicEditor;
import com.android.gallery3d.filtershow.editors.Editor;
import com.android.gallery3d.filtershow.editors.EditorChanSat;
import com.android.gallery3d.filtershow.editors.EditorColorBorder;
import com.android.gallery3d.filtershow.editors.EditorCrop;
import com.android.gallery3d.filtershow.editors.EditorDraw;
import com.android.gallery3d.filtershow.editors.EditorGrad;
import com.android.gallery3d.filtershow.editors.EditorManager;
import com.android.gallery3d.filtershow.editors.EditorMirror;
import com.android.gallery3d.filtershow.editors.EditorPanel;
import com.android.gallery3d.filtershow.editors.EditorRedEye;
import com.android.gallery3d.filtershow.editors.EditorRotate;
import com.android.gallery3d.filtershow.editors.EditorStraighten;
import com.android.gallery3d.filtershow.editors.EditorTinyPlanet;
import com.android.gallery3d.filtershow.editors.ImageOnlyEditor;
import com.android.gallery3d.filtershow.filters.FilterCropRepresentation;
import com.android.gallery3d.filtershow.filters.FilterDrawRepresentation;
import com.android.gallery3d.filtershow.filters.FilterMirrorRepresentation;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.filters.FilterRotateRepresentation;
import com.android.gallery3d.filtershow.filters.FilterStraightenRepresentation;
import com.android.gallery3d.filtershow.filters.FilterUserPresetRepresentation;
import com.android.gallery3d.filtershow.filters.FiltersManager;
import com.android.gallery3d.filtershow.filters.ImageFilter;
import com.android.gallery3d.filtershow.history.HistoryItem;
import com.android.gallery3d.filtershow.history.HistoryManager;
import com.android.gallery3d.filtershow.imageshow.ImageShow;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.imageshow.Spline;
import com.android.gallery3d.filtershow.info.InfoPanel;
import com.android.gallery3d.filtershow.pipeline.CachingPipeline;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;
import com.android.gallery3d.filtershow.pipeline.ProcessingService;
import com.android.gallery3d.filtershow.presets.PresetManagementDialog;
import com.android.gallery3d.filtershow.presets.UserPresetsAdapter;
import com.android.gallery3d.filtershow.provider.SharedImageProvider;
import com.android.gallery3d.filtershow.state.StateAdapter;
import com.android.gallery3d.filtershow.tools.SaveImage;
import com.android.gallery3d.filtershow.tools.XmpPresets;
import com.android.gallery3d.filtershow.tools.SaveImage.ContentResolverQueryCallback;
import com.android.gallery3d.filtershow.tools.XmpPresets.XMresults;
import com.android.gallery3d.filtershow.ui.ExportDialog;
import com.android.gallery3d.filtershow.ui.FramedTextButton;
import com.android.gallery3d.util.GalleryUtils;
import com.android.photos.data.GalleryBitmapPool;

import android.view.animation.Animation.AnimationListener;

import com.android.gallery3d.data.AuroraLocalAlbum;
import com.android.gallery3d.data.DecodeUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import android.util.Log;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;

import com.android.gallery3d.util.InstallUtils;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;

import com.android.gallery3d.filtershow.ttpic.AuroraTTPicUtils;

public class FilterShowActivity extends FragmentActivity implements OnItemClickListener,
        OnShareTargetSelectedListener, DialogInterface.OnShowListener,
        DialogInterface.OnDismissListener, PopupMenu.OnDismissListener {

    private String mAction = "";
    MasterImage mMasterImage = null;

    private static final long LIMIT_SUPPORTS_HIGHRES = 134217728; // 128Mb
    
    //Aurora <SQF> <2014-07-21>  for NEW_UI begin
    public static final String INTENT_DATA_USER_SELECTED_FILE_PATH = "INTENT_DATA_USER_SELECTED_FILE_PATH";
    public static final String INTENT_DATA_FROM_FILTERSHOW_ACT = "INTENT_DATA_FROM_FILTERSHOW_ACT";
    public static final String INTENT_DATA_BACK_TO_PHOTOLIST = "INTENT_DATA_BACK_TO_PHOTOLIST";
    //Aurora <SQF> <2014-07-21>  for NEW_UI end
    
	public static final int DEFAULT_PANEL = MainPanel.GEOMETRY;//paul modify
	
    public static final String TINY_PLANET_ACTION = "com.android.camera.action.TINY_PLANET";
    public static final String LAUNCH_FULLSCREEN = "launch-fullscreen";
    public static final boolean RESET_TO_LOADED = false;
    private ImageShow mImageShow = null;

    private View mSaveButton = null;

    private EditorPlaceHolder mEditorPlaceHolder = new EditorPlaceHolder(this);
    private Editor mCurrentEditor = null;

    private static final int SELECT_PICTURE = 1;
    private static final String LOGTAG = "FilterShowActivity";

    private boolean mShowingTinyPlanet = false;
    private boolean mShowingImageStatePanel = false;
    private boolean mShowingVersionsPanel = false;
    
    //Aurora <SQF> <2014-09-15>  for NEW_UI begin
    private boolean mShowingAuroraEffectsPanel = false;
    //Aurora <SQF> <2014-09-15>  for NEW_UI end
    
    //Aurora <SQF> <2014-12-25>  for NEW_UI begin
    private boolean mShowingAuroraBeautyShotPanel = false;
    private static final String TTPIC_STORE_PLACE = "system/sdcard/com.tencent.ttpic-1.apk";
    //Aurora <SQF> <2014-12-25>  for NEW_UI end

    private final Vector<ImageShow> mImageViews = new Vector<ImageShow>();

    private ShareActionProvider mShareActionProvider;
    private File mSharedOutputFile = null;

    private boolean mSharingImage = false;

    private WeakReference<ProgressDialog> mSavingProgressDialog;

    private LoadBitmapTask mLoadBitmapTask;

    private Uri mOriginalImageUri = null;
    private ImagePreset mOriginalPreset = null;

    private Uri mSelectedImageUri = null;
    
    private String mSelectedFilePath = null;

    private ArrayList<Action> mActions = new ArrayList<Action>();
    private UserPresetsManager mUserPresetsManager = null;
    private UserPresetsAdapter mUserPresetsAdapter = null;
    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //ORIGINALLY:
    /*
    private CategoryAdapter mCategoryLooksAdapter = null;
    private CategoryAdapter mCategoryBordersAdapter = null;
    private CategoryAdapter mCategoryGeometryAdapter = null;
    private CategoryAdapter mCategoryFiltersAdapter = null;
    private CategoryAdapter mCategoryVersionsAdapter = null;
    */
    //SQF MODIFIED TO:
    private AuroraCategoryAdapter mCategoryBeautyShotAdapter = null;
    private AuroraCategoryAdapter mCategoryFiltersAdapter = null;
    private AuroraCategoryAdapter mCategoryAdapter = null;
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    private int mCurrentPanel = DEFAULT_PANEL;//GEOMETRY;//LOOKS
    private Vector<FilterUserPresetRepresentation> mVersions =
            new Vector<FilterUserPresetRepresentation>();
    private int mVersionsCounter = 0;

    private boolean mHandlingSwipeButton = false;
    private View mHandledSwipeView = null;
    private float mHandledSwipeViewLastDelta = 0;
    private float mSwipeStartX = 0;
    private float mSwipeStartY = 0;

    private ProcessingService mBoundService;
    private boolean mIsBound = false;
    private Menu mMenu;
    private DialogInterface mCurrentDialog = null;
    private PopupMenu mCurrentMenu = null;
    private boolean mLoadingVisible = true;
    private boolean mLoadingComplete = false;

	private boolean mSavingImg = false;

	//paul add start
	private Handler mHandler = null;
	private int mTryTime = 0;
	private int mTryTime2 = 0;
	private FilterRepresentation mShowingRep = null;
	private static final int MSG_WAIT_FOR_UPDATE = 0;
	private static final int MSG_WAIT_FOR_READY = 1;

    //Aurora <SQF> <2015-01-06>  for NEW_UI begin
	public Uri getOriginalImageUri() {
		return mOriginalImageUri;
	}
	//Aurora <SQF> <2015-01-06>  for NEW_UI end

	public void toInstallTTPT(IPackageInstallObserver.Stub observer){
		File apkFile = new File(TTPIC_STORE_PLACE);
		if(apkFile.exists() && apkFile.isFile()){
			InstallUtils.installApp(this, "com.tencent.ttpic", apkFile, observer);
		}
	}
	//paul add end
	
	private interface MainPanelAnimationListener {
		public void hideEnd();
	};
	
    //Aurora <SQF> <2014-06-03>  for NEW_UI begin
    
    private CustomActionBarManager.Listener mActionBarListener = new CustomActionBarManager.Listener() {

		@Override
		public void userActionCancel(ActionBarType currentActionBarType) {
			// TODO Auto-generated method stub
			onBackPressed();
			//Log.i("SQF_LOG", "FilterShowActivity:: CustomActionBarManager.Listener --> userActionCancel onBackPressed...");
		}
		
		@Override
		public void userActionCancelCrop(ActionBarType currentActionBarType) {
			//below equals to : EditorPanel::cancelCurrentFilter()
			
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(FilterShowActivity.this);
            builder.setMessage(R.string.photo_edit_cancel_crop).setTitle(R.string.photo_edit_cancel_tip_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	MasterImage masterImage = MasterImage.getImage();
        			if(masterImage.updating()) return;//paul add
        			if (mCurrentEditor != null) {
        				if(mCurrentEditor.skipClick()) return;
        			}
        	        HistoryManager adapter = masterImage.getHistory();
        	        int position = adapter.undo();
        	        masterImage.onHistoryItemClick(position);
        	        invalidateViews();
        			
        			//paul add
        			if (mCurrentEditor != null) {
        				mCurrentEditor.backToMain();
        			}

                    backToMain();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	dialog.dismiss();
                }
            });
            builder.show(); 
			
			
			
		}

		@Override
		public void userActionConfirmCrop(ActionBarType currentActionBarType) {
			// TODO Auto-generated method stub
			if(MasterImage.getImage().updating()) return;//paul add
			
			Editor editor = getEditor(EditorCrop.ID);
			if(editor.skipClick()) return;
			editor.finalApplyCalled();
			
			//paul modify
			mTryTime = 0;
			mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_WAIT_FOR_UPDATE),50);
            //backToMain();
		}

		@Override
		public void userActionSave(ActionBarType currentActionBarType) {
			// TODO Auto-generated method stub
			saveImage();
		}
		
		@Override
		public void userActionCancelFilter(ActionBarType currentActionBarType) {
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(FilterShowActivity.this);
            builder.setMessage(R.string.photo_edit_cancel_filter).setTitle(R.string.photo_edit_cancel_tip_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	if(mShowingAuroraEffectsPanel) {
        	        	removeAuroraEffectRepresentation();
        	    		loadAuroraEffectsPanel(false);
        	    		mActionBarManager.setType(ActionBarType.ABT_SAVE);
        	    	}
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	dialog.dismiss();
                }
            });
            builder.show(); 
			
		}
		
		@Override
		public void userActionConfirmFilter(ActionBarType currentActionBarType) {
			/*
			if(mShowingAuroraEffectsPanel) {
	    		loadAuroraEffectsPanel(false);
	    		mActionBarManager.setType(ActionBarType.ABT_SAVE);
	    	}
	    	*/
			saveImage();
		}

    };
    
    private CustomActionBarManager mActionBarManager = new CustomActionBarManager(mActionBarListener);
    public CustomActionBarManager getActionBarManager() {
    	return mActionBarManager;
    }
    
    private void findOutActionBar() {
    	View actionBar = this.findViewById(R.id.custom_action_bar);
    	mActionBarManager.setView(actionBar);
    	//mActionBarManager.setType(ActionBarType.ABT_INITIAL);
    	mActionBarManager.setType(ActionBarType.ABT_SAVE);
    }
    
    //Aurora <SQF> <2014-06-03>  for NEW_UI end

    public ProcessingService getProcessingService() {
        return mBoundService;
    }

    public boolean isSimpleEditAction() {
        return true;//!PhotoPage.ACTION_NEXTGEN_EDIT.equalsIgnoreCase(mAction);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            /*
             * This is called when the connection with the service has been
             * established, giving us the service object we can use to
             * interact with the service.  Because we have bound to a explicit
             * service that we know is running in our own process, we can
             * cast its IBinder to a concrete class and directly access it.
             */
            mBoundService = ((ProcessingService.LocalBinder)service).getService();
            mBoundService.setFiltershowActivity(FilterShowActivity.this);
            mBoundService.onStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            /*
             * This is called when the connection with the service has been
             * unexpectedly disconnected -- that is, its process crashed.
             * Because it is running in our same process, we should never
             * see this happen.
             */
            mBoundService = null;
        }
    };

    void doBindService() {
        /*
         * Establish a connection with the service.  We use an explicit
         * class name because we want a specific service implementation that
         * we know will be running in our own process (and thus won't be
         * supporting component replacement by other applications).
         */
        bindService(new Intent(FilterShowActivity.this, ProcessingService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void updateUIAfterServiceStarted() {
        MasterImage.setMaster(mMasterImage);
        ImageFilter.setActivityForMemoryToasts(this);
        mUserPresetsManager = new UserPresetsManager(this);
        mUserPresetsAdapter = new UserPresetsAdapter(this);

        setupMasterImage();
        setupMenu();
        setDefaultValues();
        fillEditors();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        loadXML();

        fillCategories();
        loadMainPanel();
        extractXMPData();
        processIntent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Aurora <SQF> <2014-07-21>  for NEW_UI begin
        setLightsOutMode();
        //Aurora <SQF> <2014-07-21>  for NEW_UI end
        
        boolean onlyUsePortrait = getResources().getBoolean(R.bool.only_use_portrait);
        if (onlyUsePortrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		
		//paul add		
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_WAIT_FOR_UPDATE: {
 						if(MasterImage.getImage().updating()){
							if(++mTryTime > 40){
								Log.w(LOGTAG, "MasterImage: too long time to update!");
								MasterImage.getImage().setUpdating(false);
								backToMain();
								return;
							}
							mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_WAIT_FOR_UPDATE),100);
						}else{
							backToMain();
						}
                        break;
                    }
                    case MSG_WAIT_FOR_READY: {
 						if(MasterImage.getImage().showing()){
							if(++mTryTime2 > 40){
								Log.w(LOGTAG, "MasterImage: too long time to load!");
								mImageShow.stopDrawing(false);
								MasterImage.getImage().setShowing(false);
								mShowingRep = null;
								backToMain();
								return;
							}

							mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_WAIT_FOR_READY),100);
						}else if(null != mShowingRep){
							mEditorPlaceHolder.displayEditor(mShowingRep.getEditorId());
							loadEditorPanel(mShowingRep, mCurrentEditor);
							mShowingRep = null;
							mImageShow.stopDrawing(false);
							mActionBarManager.setType(ActionBarType.ABT_CONFIRM_CROP);
						}
                        break;
                    }

                }
            }
        };

        clearGalleryBitmapPool();
        doBindService();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));//Color.GRAY

        setContentView(R.layout.filtershow_splashscreen);
        mLoadingComplete = false;
    }
    
    //Aurora <SQF> <2014-07-21>  for NEW_UI begin
    private void setLightsOutMode() {
    	View v = this.getWindow().getDecorView();
    	int flags = v.getSystemUiVisibility();
    	flags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
		v.setSystemUiVisibility(flags);
    }
    //Aurora <SQF> <2014-07-21>  for NEW_UI end
    
    //Aurora <SQF> <2014-07-16>  for NEW_UI begin
    public void changeMainPanelBackgroundColor() {
    	View v = findViewById(R.id.mainPanel);
    	v.setBackgroundColor(Color.argb(136, 0, 0, 0));
    }
    
    public void resetMainPanelBackgroundColor() {
    	View v = findViewById(R.id.mainPanel);
    	v.setBackgroundColor(Color.TRANSPARENT);
    }
    //Aurora <SQF> <2014-07-16>  for NEW_UI end

    public boolean isShowingImageStatePanel() {
        return mShowingImageStatePanel;
    }

    public void loadMainPanel() {
        if (findViewById(R.id.main_panel_container) == null) {
            return;
        }
		try{
	        MainPanel panel = new MainPanel();
	        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	        transaction.replace(R.id.main_panel_container, panel, MainPanel.FRAGMENT_TAG);
	        transaction.commitAllowingStateLoss();
		} catch(Exception e){//paul add for BUG #16903
			e.printStackTrace();
		}
    }
    
    
    //Aurora <SQF> <2014-09-15>  for NEW_UI begin
    //Aurora <SQF> <2014-09-01>  for NEW_UI begin
    
    public void showAuroraEffectsPanelAnimation(boolean show, AnimationListener listener) {
    	final View v = findViewById(R.id.filters_sub_panel_container);
    	if(v == null) return;
    	if(show) {
    		v.setVisibility(View.VISIBLE);
    		Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_up_in);
    		v.startAnimation(animation);
    	} else {
    		Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_down_out);
    		animation.setAnimationListener(listener);
    		v.startAnimation(animation);
    	}
    }
    //Aurora <SQF> <2014-09-12>  for NEW_UI begin

    public void loadAuroraEffectsPanel(boolean show) {
    	final View container = findViewById(R.id.filters_sub_panel_container);
    	final View mainPanelContainerView = findViewById(R.id.main_panel_container);
    	final MainPanel mainPanel = (MainPanel)getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
    	if (container == null) {
            return;
        }
    	final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	if(show) {
    		transaction.setCustomAnimations(R.anim.float_up_in, R.anim.float_down_out);
    		
			mainPanel.showMainMenuCategoryPanelAnimation(false, new AnimationListener() {
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					
					mainPanelContainerView.setVisibility(View.INVISIBLE);
					
					SubCategoryFiltersPanel panel = new SubCategoryFiltersPanel();
			        transaction.replace(R.id.filters_sub_panel_container, panel, SubCategoryFiltersPanel.FRAGMENT_TAG);
			        transaction.commitAllowingStateLoss();
					
					/*
					SubCategoryFiltersPanel panel = (SubCategoryFiltersPanel) getSupportFragmentManager().findFragmentByTag(SubCategoryFiltersPanel.FRAGMENT_TAG);
					if(panel != null) {
						transaction.show(panel);
					} else {
						panel = new SubCategoryFiltersPanel();
						transaction.replace(R.id.filters_sub_panel_container, panel, SubCategoryFiltersPanel.FRAGMENT_TAG);
					}
					transaction.commitAllowingStateLoss();
					*/
					
			        mShowingAuroraEffectsPanel = true;
			        showAuroraEffectsPanelAnimation(true, null);
			        
			        mActionBarManager.setType(ActionBarType.ABT_CONFIRM_FILTER);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
	    	
    	} else {
    		showAuroraEffectsPanelAnimation(false, new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub

					SubCategoryFiltersPanel auroraEffectsCategoryPanel = (SubCategoryFiltersPanel)getSupportFragmentManager().findFragmentByTag(SubCategoryFiltersPanel.FRAGMENT_TAG);
					transaction.remove(auroraEffectsCategoryPanel);
		    		transaction.commit();
					
		    		/*
					SubCategoryFiltersPanel panel = (SubCategoryFiltersPanel) getSupportFragmentManager().findFragmentByTag(SubCategoryFiltersPanel.FRAGMENT_TAG);
					if(panel != null) {
						transaction.hide(panel);
					} 
					transaction.commit();
					*/
					
		    		mShowingAuroraEffectsPanel = false;
		    		mainPanelContainerView.setVisibility(View.VISIBLE);
		    		mainPanel.showMainMenuCategoryPanelAnimation(true, null);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
    			
    		});
    	}
    }
    
    
    /////
    
    public void showAuroraBeautyShotPanelAnimation(boolean show, AnimationListener listener) {
    	final View v = findViewById(R.id.beauty_shot_sub_panel_container);
    	if(v == null) return;
    	if(show) {
    		v.setVisibility(View.VISIBLE);
    		Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_up_in);
    		v.startAnimation(animation);
    	} else {
    		Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_down_out);
    		animation.setAnimationListener(listener);
    		v.startAnimation(animation);
    	}
    }
    
    /*
     * load ttpic panel
     */
    public void loadAuroraBeautyShotPanel(boolean show) {
    	//Log.i("SQF_LOG", "loadAuroraBeautyShotPanel-------------------------------------------------------------------------");
    	final View container = findViewById(R.id.beauty_shot_sub_panel_container);
    	final View mainPanelContainerView = findViewById(R.id.main_panel_container);
    	final MainPanel mainPanel = (MainPanel)getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
    	if (container == null) {
            return;
        }
    	final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	if(show) {
    		transaction.setCustomAnimations(R.anim.float_up_in, R.anim.float_down_out);
    		
			mainPanel.showMainMenuCategoryPanelAnimation(false, new AnimationListener() {
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					
					mainPanelContainerView.setVisibility(View.INVISIBLE);
					
					SubCategoryBeautyShotPanel panel = new SubCategoryBeautyShotPanel();
			        transaction.replace(R.id.beauty_shot_sub_panel_container, panel, SubCategoryBeautyShotPanel.FRAGMENT_TAG);
			        transaction.commitAllowingStateLoss();
					
					/*
					SubCategoryFiltersPanel panel = (SubCategoryFiltersPanel) getSupportFragmentManager().findFragmentByTag(SubCategoryFiltersPanel.FRAGMENT_TAG);
					if(panel != null) {
						transaction.show(panel);
					} else {
						panel = new SubCategoryFiltersPanel();
						transaction.replace(R.id.filters_sub_panel_container, panel, SubCategoryFiltersPanel.FRAGMENT_TAG);
					}
					transaction.commitAllowingStateLoss();
					*/
					
			        mShowingAuroraBeautyShotPanel = true;
			        showAuroraBeautyShotPanelAnimation(true, null);
			        
			        //mActionBarManager.setType(ActionBarType.ABT_CONFIRM_FILTER);
			        mActionBarManager.setType(ActionBarType.ABT_PITU_RETURN);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
	    	
    	} else {
    		showAuroraBeautyShotPanelAnimation(false, new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub

					SubCategoryBeautyShotPanel auroraBeautyShotCategoryPanel = (SubCategoryBeautyShotPanel)getSupportFragmentManager().findFragmentByTag(SubCategoryBeautyShotPanel.FRAGMENT_TAG);
					transaction.remove(auroraBeautyShotCategoryPanel);
		    		transaction.commit();
					
		    		/*
					SubCategoryFiltersPanel panel = (SubCategoryFiltersPanel) getSupportFragmentManager().findFragmentByTag(SubCategoryFiltersPanel.FRAGMENT_TAG);
					if(panel != null) {
						transaction.hide(panel);
					} 
					transaction.commit();
					*/
					
		    		mShowingAuroraBeautyShotPanel = false;
		    		mainPanelContainerView.setVisibility(View.VISIBLE);
		    		mainPanel.showMainMenuCategoryPanelAnimation(true, null);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
    			
    		});
    	}
    }
    ////
    
    
    
    

	public static boolean NO_BOTTOM = false;
	
    public void loadEditorPanel(FilterRepresentation representation,
                                final Editor currentEditor) {
        if (representation.getEditorId() == ImageOnlyEditor.ID) {
            currentEditor.reflectCurrentFilter();
            return;
        }
        final int currentId = currentEditor.getID();
        Runnable showEditor = new Runnable() {
            @Override
            public void run() {
            	try{
	                EditorPanel panel = new EditorPanel();
	                panel.setEditor(currentId);
	                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	                transaction.remove(getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG));
	                transaction.replace(R.id.main_panel_container, panel, MainPanel.FRAGMENT_TAG);
	                transaction.commit();
            	}catch(Exception e){//paul add for BUG #15389<2015-08-05>
					Log.e(LOGTAG, "showEditor err");
				}
            }
        };
        Fragment main = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        boolean doAnimation = false;
        if (mShowingImageStatePanel
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            doAnimation = true;
        }
        if (doAnimation && main != null && main instanceof MainPanel) {
            MainPanel mainPanel = (MainPanel) main;
            View container = mainPanel.getView().findViewById(R.id.category_panel_container);
            View bottom = mainPanel.getView().findViewById(R.id.bottom_panel);
            int panelHeight = container.getHeight() + bottom.getHeight();
            ViewPropertyAnimator anim = mainPanel.getView().animate();
            anim.translationY(panelHeight).start();
            final Handler handler = new Handler();
            handler.postDelayed(showEditor, anim.getDuration());
        } else {
            showEditor.run();
        }
    }

    public void toggleInformationPanel() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);

        InfoPanel panel = new InfoPanel();
        panel.show(transaction, InfoPanel.FRAGMENT_TAG);
    }

    private void loadXML() {
        setContentView(R.layout.filtershow_activity);
        //Aurora <SQF> <2014-06-03>  for NEW_UI begin
        findOutActionBar();
        //Aurora <SQF> <2014-06-03>  for NEW_UI end
        //Aurora <SQF> <2014-05-29>  for NEW_UI begin
        //SQF ANNOTATED ...
        /*
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.filtershow_actionbar);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.background_screen)));

        mSaveButton = actionBar.getCustomView();
        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });
        */
        //Aurora <SQF> <2014-05-29>  for NEW_UI end
        mImageShow = (ImageShow) findViewById(R.id.imageShow);
        mImageViews.add(mImageShow);

        setupEditors();

        mEditorPlaceHolder.hide();
        mImageShow.attach();

        setupStatePanel();
    }

    public void fillCategories() {
        //Aurora <SQF> <2014-08-29>  for NEW_UI begin
        //ORIGINALLY:
    	/*
    	fillLooks();
        loadUserPresets();
        fillBorders();
        fillTools();
        fillEffects();
        fillVersions();
        */
        //SQF MODIFIED TO:
    	//fillBeautyShots();
    	fillEffects();
    	fillAuroraAdapter();
        //Aurora <SQF> <2014-08-29>  for NEW_UI end
        
    }

    public void setupStatePanel() {
        MasterImage.getImage().setHistoryManager(mMasterImage.getHistory());
    }
    
    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-29
    /*
    private void fillVersions() {
        if (mCategoryVersionsAdapter != null) {
            mCategoryVersionsAdapter.clear();
        }
        mCategoryVersionsAdapter = new CategoryAdapter(this);
        mCategoryVersionsAdapter.setShowAddButton(true);
    }
    */
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    public void registerAction(Action action) {
        if (mActions.contains(action)) {
            return;
        }
        mActions.add(action);
    }

    private void loadActions() {
        for (int i = 0; i < mActions.size(); i++) {
            Action action = mActions.get(i);
            action.setImageFrame(new Rect(0, 0, 96, 96), 0);
        }
    }
    
    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-29
    /*
    public void updateVersions() {
        mCategoryVersionsAdapter.clear();
        FilterUserPresetRepresentation originalRep = new FilterUserPresetRepresentation(
                getString(R.string.filtershow_version_original), new ImagePreset(), -1);
        mCategoryVersionsAdapter.add(
                new Action(this, originalRep, Action.FULL_VIEW));
        ImagePreset current = new ImagePreset(MasterImage.getImage().getPreset());
        FilterUserPresetRepresentation currentRep = new FilterUserPresetRepresentation(
                getString(R.string.filtershow_version_current), current, -1);
        mCategoryVersionsAdapter.add(
                new Action(this, currentRep, Action.FULL_VIEW));
        if (mVersions.size() > 0) {
            mCategoryVersionsAdapter.add(new Action(this, Action.SPACER));
        }
        for (FilterUserPresetRepresentation rep : mVersions) {
            mCategoryVersionsAdapter.add(
                    new Action(this, rep, Action.FULL_VIEW, true));
        }
        mCategoryVersionsAdapter.notifyDataSetInvalidated();
    }
    */
    //Aurora <SQF> <2014-08-29>  for NEW_UI end

    public void addCurrentVersion() {
        ImagePreset current = new ImagePreset(MasterImage.getImage().getPreset());
        mVersionsCounter++;
        FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation(
                "" + mVersionsCounter, current, -1);
        mVersions.add(rep);
        //updateVersions();//SQF ANNOTATED ON 2014-08-30
    }

    public void removeVersion(Action action) {
        mVersions.remove(action.getRepresentation());
        //updateVersions();//SQF ANNOTATED ON 2014-08-30
    }

    public void removeLook(Action action) {
        FilterUserPresetRepresentation rep =
                (FilterUserPresetRepresentation) action.getRepresentation();
        if (rep == null) {
            return;
        }
        mUserPresetsManager.delete(rep.getId());
        //updateUserPresetsFromManager();//SQF ANNOTATED ON 2014-08-29
    }

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-29

//    private void fillEffects() {
//   
//        FiltersManager filtersManager = FiltersManager.getManager();
//        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getEffects();
//        if (mCategoryFiltersAdapter != null) {
//            mCategoryFiltersAdapter.clear();
//        }
//        mCategoryFiltersAdapter = new CategoryAdapter(this);
//        for (FilterRepresentation representation : filtersRepresentations) {
//            if (representation.getTextId() != 0) {
//                representation.setName(getString(representation.getTextId()));
//            }
//            mCategoryFiltersAdapter.add(new Action(this, representation));
//        }
//        
//    }
//    
//    private void fillTools() {
//    	
//        FiltersManager filtersManager = FiltersManager.getManager();
//        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getTools();
//        if (mCategoryGeometryAdapter != null) {
//            mCategoryGeometryAdapter.clear();
//        }
//        mCategoryGeometryAdapter = new CategoryAdapter(this);
//        boolean found = false;
//        for (FilterRepresentation representation : filtersRepresentations) {
//        	//Log.i("SQF_LOG", "FilterShowActivity::fillTools --> add .......");
//            mCategoryGeometryAdapter.add(new Action(this, representation));
//			/*
//            if (representation instanceof FilterDrawRepresentation) {
//                found = true;
//            }
//            */
//            
//        }
//		
//        //Aurora <SQF> <2014-6-4>  for annotation begin
//        //SQF ANNOTATED ON 2014.6.4
//        /*
//        if (!found) {
//            FilterRepresentation representation = new FilterDrawRepresentation();
//            Action action = new Action(this, representation);
//            action.setIsDoubleAction(true);
//            //Log.i("SQF_LOG", "FilterShowActivity::fillTools --> add .......333");
//            mCategoryGeometryAdapter.add(action);
//        }
//        */
//        //Aurora <SQF> <2014-6-4>  for annotation begin
//    }
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    
    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    
//    private void fillBeautyShots() {
//        FiltersManager filtersManager = FiltersManager.getManager();
//        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getBeautyShotRepresentations();
//        if (mCategoryBeautyShotAdapter != null) {
//        	mCategoryBeautyShotAdapter.clear();
//        }
//        mCategoryBeautyShotAdapter = new AuroraCategoryAdapter(this);
//        for (FilterRepresentation representation : filtersRepresentations) {
//            if (representation.getTextId() != 0) {
//                representation.setName(getString(representation.getTextId()));
//            }
//            mCategoryBeautyShotAdapter.add(new AuroraAction(this, representation));
//        }
//        Log.i("SQF_LOG", "fillBeautyShots mCategoryBeautyShotAdapter size:" + mCategoryBeautyShotAdapter.getCount());
//    }

    private void fillEffects() {
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getEffects();
        if (mCategoryFiltersAdapter != null) {
            mCategoryFiltersAdapter.clear();
        }
        mCategoryFiltersAdapter = new AuroraCategoryAdapter(this);
        for (FilterRepresentation representation : filtersRepresentations) {
            if (representation.getTextId() != 0) {
                representation.setName(getString(representation.getTextId()));
            }
            mCategoryFiltersAdapter.add(new AuroraAction(this, representation));
        }
        Log.i("SQF_LOG", "fillEffects mCategoryFiltersAdapter size:" + mCategoryFiltersAdapter.getCount());
    }
    
    private void fillAuroraAdapter() {
    	FiltersManager filtersManager = FiltersManager.getManager();
    	ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getAuroraRepresentations();

    	mCategoryAdapter = new AuroraCategoryAdapter(this);
    	//mCategoryAdapter.setOnClickListener(this);
    	for (FilterRepresentation representation : filtersRepresentations) {
    		Log.i("SQF_LOG", "fillAuroraAdapter:" + representation.getSerializationName());
    		AuroraAction action = new AuroraAction(this, representation);
    		mCategoryAdapter.add(action);
    	}
    	//for aurora filter effects 
    	AuroraAction actionFilterEffects = new AuroraAction(this, null);
    	actionFilterEffects.setType(AuroraActionType.MAIN_MENU_FILTERS);
    	mCategoryAdapter.add(actionFilterEffects);
    	
    	//for aurora beauty shot
    	AuroraAction actionBeautyShots = new AuroraAction(this, null);
    	actionBeautyShots.setType(AuroraActionType.MAIN_MENU_BEAUTY_SHOTS);
    	mCategoryAdapter.add(actionBeautyShots);
    }
    //Aurora <SQF> <2014-08-29>  for NEW_UI end

    private void processIntent() {
    	//Log.i("SQF_LOG", "FilterShowActivity::processIntent");
        Intent intent = getIntent();
        if (intent.getBooleanExtra(LAUNCH_FULLSCREEN, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mAction = intent.getAction();
        mSelectedImageUri = intent.getData();
        mSelectedFilePath = intent.getStringExtra(INTENT_DATA_USER_SELECTED_FILE_PATH);
        Log.i("SQF_LOG", "FilterShowActivity::processIntent mSelectedImageUri:" + mSelectedImageUri + " path:" + mSelectedImageUri.getPath());
        Uri loadUri = mSelectedImageUri;
        if (mOriginalImageUri != null) {
            loadUri = mOriginalImageUri;
            //Log.i("SQF_LOG", "FilterShowActivity::processIntent mOriginalImageUri:" + mOriginalImageUri);
        }
        if (loadUri != null) {
            startLoadBitmap(loadUri);
        } else {
            pickImage();
        }
    }

    private void setupEditors() {
        mEditorPlaceHolder.setContainer((FrameLayout) findViewById(R.id.editorContainer));
        EditorManager.addEditors(mEditorPlaceHolder);
        mEditorPlaceHolder.setOldViews(mImageViews);
       
    }

    private void fillEditors() {
        mEditorPlaceHolder.addEditor(new EditorChanSat());
        mEditorPlaceHolder.addEditor(new EditorGrad());
        mEditorPlaceHolder.addEditor(new EditorDraw());
        mEditorPlaceHolder.addEditor(new EditorColorBorder());
        mEditorPlaceHolder.addEditor(new BasicEditor());
        mEditorPlaceHolder.addEditor(new ImageOnlyEditor());
        mEditorPlaceHolder.addEditor(new EditorTinyPlanet());
        mEditorPlaceHolder.addEditor(new EditorRedEye());
        mEditorPlaceHolder.addEditor(new EditorCrop());
        mEditorPlaceHolder.addEditor(new EditorMirror());
        //mEditorPlaceHolder.addEditor(new EditorRotate());//SQF ANNOTATED ON 2014.10.11
        mEditorPlaceHolder.addEditor(new EditorStraighten());
    }

    private void setDefaultValues() {
        Resources res = getResources();

        // TODO: get those values from XML.
        FramedTextButton.setTextSize((int) getPixelsFromDip(14));
        FramedTextButton.setTrianglePadding((int) getPixelsFromDip(4));
        FramedTextButton.setTriangleSize((int) getPixelsFromDip(10));

        Drawable curveHandle = res.getDrawable(R.drawable.camera_crop);
        int curveHandleSize = (int) res.getDimension(R.dimen.crop_indicator_size);
        Spline.setCurveHandle(curveHandle, curveHandleSize);
        Spline.setCurveWidth((int) getPixelsFromDip(3));

        mOriginalImageUri = null;
    }

    private void startLoadBitmap(Uri uri) {
        final View imageShow = findViewById(R.id.imageShow);
        imageShow.setVisibility(View.INVISIBLE);
        startLoadingIndicator();
        mShowingTinyPlanet = false;
        mLoadBitmapTask = new LoadBitmapTask();
        mLoadBitmapTask.execute(uri);
    }

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-29

    /*
    private void fillBorders() {
    	
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> borders = filtersManager.getBorders();

        for (int i = 0; i < borders.size(); i++) {
            FilterRepresentation filter = borders.get(i);
            filter.setName(getString(R.string.borders));
            if (i == 0) {
                filter.setName(getString(R.string.none));
            }
        }

        if (mCategoryBordersAdapter != null) {
            mCategoryBordersAdapter.clear();
        }
        mCategoryBordersAdapter = new CategoryAdapter(this);
        for (FilterRepresentation representation : borders) {
            if (representation.getTextId() != 0) {
                representation.setName(getString(representation.getTextId()));
            }
            mCategoryBordersAdapter.add(new Action(this, representation, Action.FULL_VIEW));
        }
        
    }
    */
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    
    public UserPresetsAdapter getUserPresetsAdapter() {
        return mUserPresetsAdapter;
    }
    
    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //ORIGINALLY:
    /*
    public CategoryAdapter getCategoryLooksAdapter() {
        return mCategoryLooksAdapter;
    }

    public CategoryAdapter getCategoryBordersAdapter() {
        return mCategoryBordersAdapter;
    }

    public CategoryAdapter getCategoryGeometryAdapter() {
        return mCategoryGeometryAdapter;
    }

    public CategoryAdapter getCategoryFiltersAdapter() {
        return mCategoryFiltersAdapter;
    }

    public CategoryAdapter getCategoryVersionsAdapter() {
        return mCategoryVersionsAdapter;
    }
    */
    //SQF MODIFIED TO:
    
    public AuroraCategoryAdapter getCategoryBeautyShotAdapter() {
        return mCategoryBeautyShotAdapter;
    }
    
    public AuroraCategoryAdapter getCategoryFiltersAdapter() {
        return mCategoryFiltersAdapter;
    }
    
    public AuroraCategoryAdapter getCategoryAdapter() {
    	return mCategoryAdapter;
    }
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    

    public void removeFilterRepresentation(FilterRepresentation filterRepresentation) {
        if (filterRepresentation == null) {
            return;
        }
        ImagePreset oldPreset = MasterImage.getImage().getPreset();
        ImagePreset copy = new ImagePreset(oldPreset);
        copy.removeFilter(filterRepresentation);
        MasterImage.getImage().setPreset(copy, copy.getLastRepresentation(), true);
        if (MasterImage.getImage().getCurrentFilterRepresentation() == filterRepresentation) {
            FilterRepresentation lastRepresentation = copy.getLastRepresentation();
            MasterImage.getImage().setCurrentFilterRepresentation(lastRepresentation);
        }
    }

    public void useFilterRepresentation(FilterRepresentation filterRepresentation) {
        if (filterRepresentation == null) {
            return;
        }
        if (!(filterRepresentation instanceof FilterRotateRepresentation)
            && !(filterRepresentation instanceof FilterMirrorRepresentation)
            && MasterImage.getImage().getCurrentFilterRepresentation() == filterRepresentation) {
            return;
        }
        if (filterRepresentation instanceof FilterUserPresetRepresentation
                || filterRepresentation instanceof FilterRotateRepresentation
                || filterRepresentation instanceof FilterMirrorRepresentation) {
            MasterImage.getImage().onNewLook(filterRepresentation);
        }
        ImagePreset oldPreset = MasterImage.getImage().getPreset();
        ImagePreset copy = new ImagePreset(oldPreset);
        FilterRepresentation representation = copy.getRepresentation(filterRepresentation);

        if (representation == null) {
            filterRepresentation = filterRepresentation.copy();
            copy.addFilter(filterRepresentation);
        } else {
            if (filterRepresentation.allowsSingleInstanceOnly()) {
                // Don't just update the filter representation. Centralize the
                // logic in the addFilter(), such that we can keep "None" as
                // null.
                if (!representation.equals(filterRepresentation)) {
                    // Only do this if the filter isn't the same
                    // (state panel clicks can lead us here)
                    copy.removeFilter(representation);
                    copy.addFilter(filterRepresentation);
                    //Log.i("SQF_LOG", "filterRepresentation copy.addFilter -----------------------" );
                }
            }
        }
        MasterImage.getImage().setPreset(copy, filterRepresentation, true);
        MasterImage.getImage().setCurrentFilterRepresentation(filterRepresentation);
    }
    
    //Aurora <SQF> <2014-07-15>  for NEW_UI begin
    public void showMainPanelContainerAnimation(boolean show, final MainPanelAnimationListener listener) {
    	final View v = findViewById(R.id.main_panel_container);
    	//final View v = findViewById(R.id.mainPanel);
    	if(v == null) return;
    	if(show) {
    		v.setVisibility(View.VISIBLE);
    		resetMainPanelBackgroundColor();
    		Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_up_in);
    		v.startAnimation(animation);
    	} else {
    		Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_down_out);
    		animation.setAnimationListener(new AnimationListener() {
    			public void onAnimationEnd(Animation animation) {
    	            v.setVisibility(View.INVISIBLE);
    	            if(listener != null) listener.hideEnd();
    	        }

    	        public void onAnimationRepeat(Animation animation) {
    	        }

    	        public void onAnimationStart(Animation animation) {
    	        }
    		});
    		v.startAnimation(animation);
    	}
    }
    //Aurora <SQF> <2014-07-15>  for NEW_UI end
    
    
    
    
    //Aurora <SQF> <2014-09-16>  for NEW_UI begin
    
    public void removeAuroraEffectRepresentation() {
    	ImagePreset preset = MasterImage.getImage().getPreset();
    	if(preset.getAuroraEffectRepresentation() != null) {
			ImagePreset copy = new ImagePreset(preset);
			copy.removeAuroraEffectFilter();
			MasterImage.getImage().setPreset(copy, null, true);
            MasterImage.getImage().setCurrentFilterRepresentation(null);
            return;
		}
    }
    
    public void removeAuroraEffectRepresentation(final FilterRepresentation newRepresentation) {
    	if(newRepresentation == null) return;
    	if(newRepresentation.getFilterType() == FilterRepresentation.TYPE_AURORA_EFFECTS) {
    		ImagePreset preset = MasterImage.getImage().getPreset();
    		if(preset.getAuroraEffectRepresentation(newRepresentation) != null) {
    			ImagePreset copy = new ImagePreset(preset);
    			copy.removeAuroraEffectFilter(newRepresentation);
    			FilterRepresentation filterRepresentation = newRepresentation.copy();
    			MasterImage.getImage().setPreset(copy, filterRepresentation, true);
                MasterImage.getImage().setCurrentFilterRepresentation(null);
                return;
    		}
    	}
    }
    //Aurora <SQF> <2014-09-16>  for NEW_UI end

    public void showRepresentation(final FilterRepresentation representation) {
        if (representation == null) {
            return;
        }
        
        //Aurora <SQF> <2014-09-16>  for NEW_UI begin
        MasterImage master = MasterImage.getImage();
        if(representation.getFilterType() == representation.TYPE_AURORA_EFFECTS) {
        	removeAuroraEffectRepresentation(representation);
        	master.setIsAuroraEffectUpdating(true);
        	startLoadingIndicator();				//SQF ADDED ON 2014.10.09
        }
        //Aurora <SQF> <2014-09-16>  for NEW_UI end
  
        if (representation instanceof FilterRotateRepresentation) {
        	//Aurora <SQF> <2014-07-25>  for NEW_UI begin
        	if(! master.isPreviewUpdateFinish() || master.onGoingNewLookAnimation()) return;
        	master.setPreviewUpdateFinish(false);
        	//Aurora <SQF> <2014-07-25>  for NEW_UI end
            FilterRotateRepresentation r = (FilterRotateRepresentation) representation;
            r.rotateCW();
			MasterImage.getImage().setRotated();//paul add
        }
        if (representation instanceof FilterMirrorRepresentation) {
            FilterMirrorRepresentation r = (FilterMirrorRepresentation) representation;
            r.cycle();
        }
        if (representation.isBooleanFilter()) {
            ImagePreset preset = MasterImage.getImage().getPreset();
            if (preset.getRepresentation(representation) != null) {
                // remove
                ImagePreset copy = new ImagePreset(preset);
                copy.removeFilter(representation);
                FilterRepresentation filterRepresentation = representation.copy();
                MasterImage.getImage().setPreset(copy, filterRepresentation, true);
                MasterImage.getImage().setCurrentFilterRepresentation(null);
                return;
            }
        }
		
		//paul modify start
        // show representation
        if (mCurrentEditor != null) {
            mCurrentEditor.detach();
        }
		boolean reload = false;
		boolean isCrop = false;
		//Aurora <SQF> <2014-7-15>  for NEW_UI begin
        if(representation.getEditorId() == EditorCrop.ID) {
			mImageShow.stopDrawing(true);//paul add
			isCrop = true;
			reload = MasterImage.getImage().load();
        	//mActionBarManager.setType(ActionBarType.ABT_CONFIRM_CROP);
        	showMainPanelContainerAnimation(false, null);
			MasterImage.getImage().setShowing(true);
        }else{
			mImageShow.stopDrawing(false);//paul add
		}
		//Aurora <SQF> <2014-7-15>  for NEW_UI begin

        useFilterRepresentation(representation);
        if(isCrop){
        	mCurrentEditor = mEditorPlaceHolder.getCurrentEditor(representation.getEditorId());
			mCurrentEditor.enter(reload);
			mTryTime2 = 0;
			mShowingRep = representation;
			if(!mHandler.hasMessages(MSG_WAIT_FOR_READY)) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_WAIT_FOR_READY));
			}
        } else {
			mCurrentEditor = mEditorPlaceHolder.showEditor(representation.getEditorId());
        	loadEditorPanel(representation, mCurrentEditor);
        }
		/*
        useFilterRepresentation(representation);
        
		//Aurora <SQF> <2014-7-15>  for NEW_UI begin
        if(representation.getEditorId() == EditorCrop.ID) {
        	mActionBarManager.setType(ActionBarType.ABT_CONFIRM_CROP);
        	showMainPanelContainerAnimation(false, new MainPanelAnimationListener() {

				@Override
				public void hideEnd() {
					// TODO Auto-generated method stub
					loadCurrentEditorPanel(representation);
				}
        	});
        }
		//Aurora <SQF> <2014-7-15>  for NEW_UI begin
        // show representation
        else {
//	        if (mCurrentEditor != null) {
//	            mCurrentEditor.detach();
//	        }
//	        mCurrentEditor = mEditorPlaceHolder.showEditor(representation.getEditorId());
//	        loadEditorPanel(representation, mCurrentEditor);
        	loadCurrentEditorPanel(representation);
        }
    }
    
    //Aurora <SQF> <2014-07-25>  for NEW_UI begin
    private void loadCurrentEditorPanel(final FilterRepresentation representation) {
    	if (mCurrentEditor != null) {
            mCurrentEditor.detach();
        }
        mCurrentEditor = mEditorPlaceHolder.showEditor(representation.getEditorId());
        loadEditorPanel(representation, mCurrentEditor);
		*/
		//paul modify end
    }
    //Aurora <SQF> <2014-07-25>  for NEW_UI end

    public Editor getEditor(int editorID) {
        return mEditorPlaceHolder.getEditor(editorID);
    }

    public void setCurrentPanel(int currentPanel) {
        mCurrentPanel = currentPanel;
    }

    public int getCurrentPanel() {
        return mCurrentPanel;
    }

    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-30
    /*
    public void updateCategories() {
        if (mMasterImage == null) {
            return;
        }
        ImagePreset preset = mMasterImage.getPreset();
        mCategoryLooksAdapter.reflectImagePreset(preset);
        mCategoryBordersAdapter.reflectImagePreset(preset);
    }
    */
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
    
    public View getMainStatePanelContainer(int id) {
        return findViewById(id);
    }

    public void onShowMenu(PopupMenu menu) {
        mCurrentMenu = menu;
        menu.setOnDismissListener(this);
    }

    @Override
    public void onDismiss(PopupMenu popupMenu){
        if (mCurrentMenu == null) {
            return;
        }
        mCurrentMenu.setOnDismissListener(null);
        mCurrentMenu = null;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mCurrentDialog = dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        mCurrentDialog = null;
    }

    private class LoadHighresBitmapTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            MasterImage master = MasterImage.getImage();
            Rect originalBounds = master.getOriginalBounds();
            if (master.supportsHighRes()) {
                int highresPreviewSize = master.getOriginalBitmapLarge().getWidth() * 2;
                if (highresPreviewSize > originalBounds.width()) {
                    highresPreviewSize = originalBounds.width();
                }
                Rect bounds = new Rect();
                Bitmap originalHires = ImageLoader.loadOrientedConstrainedBitmap(master.getUri(),
                        master.getActivity(), highresPreviewSize,
                        master.getOrientation(), bounds);
                master.setOriginalBounds(bounds);
                master.setOriginalBitmapHighres(originalHires);
                mBoundService.setOriginalBitmapHighres(originalHires);
                master.warnListeners();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Bitmap highresBitmap = MasterImage.getImage().getOriginalBitmapHighres();
            if (highresBitmap != null) {
                float highResPreviewScale = (float) highresBitmap.getWidth()
                        / (float) MasterImage.getImage().getOriginalBounds().width();
                mBoundService.setHighresPreviewScaleFactor(highResPreviewScale);
            }
            MasterImage.getImage().warnListeners();
        }
    }

    public boolean isLoadingVisible() {
        return mLoadingVisible;
    }

    public void startLoadingIndicator() {
        final View loading = findViewById(R.id.loading);
        mLoadingVisible = true;
        loading.setVisibility(View.VISIBLE);
    }

    public void stopLoadingIndicator() {
        final View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mLoadingVisible = false;
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Boolean, Boolean> {
        int mBitmapSize;

        public LoadBitmapTask() {
            mBitmapSize = getScreenImageSize();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            if (!MasterImage.getImage().loadBitmap(params[0], mBitmapSize)) {
                return false;
            }
            //publishProgress(ImageLoader.queryLightCycle360(MasterImage.getImage().getActivity()));
            return true;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            if (isCancelled()) {
                return;
            }
            if (values[0]) {
                mShowingTinyPlanet = true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            MasterImage.setMaster(mMasterImage);
            if (isCancelled()) {
                return;
            }

            if (!result) {
                if (mOriginalImageUri != null
                        && !mOriginalImageUri.equals(mSelectedImageUri)) {
                    mOriginalImageUri = mSelectedImageUri;
                    mOriginalPreset = null;
                    Toast.makeText(FilterShowActivity.this,
                            R.string.cannot_edit_original, Toast.LENGTH_SHORT).show();
                    startLoadBitmap(mOriginalImageUri);
                } else {
                    cannotLoadImage();
                }
                return;
            }
            
            /*
            if (null == CachingPipeline.getRenderScriptContext()){
                Log.v(LOGTAG,"RenderScript context destroyed during load");
                return;
            }
            */
            
            final View imageShow = findViewById(R.id.imageShow);
            imageShow.setVisibility(View.VISIBLE);


            Bitmap largeBitmap = MasterImage.getImage().getOriginalBitmapLarge();
            mBoundService.setOriginalBitmap(largeBitmap);

            float previewScale = (float) largeBitmap.getWidth()
                    / (float) MasterImage.getImage().getOriginalBounds().width();
            mBoundService.setPreviewScaleFactor(previewScale);
            if (!mShowingTinyPlanet) {
                //mCategoryFiltersAdapter.removeTinyPlanet();//SQF ANNOTATED ON 2014-08-30
            }
            
            //Aurora <SQF> <2014-08-30>  for NEW_UI begin
            //SQF ANNOTATED ON 2014-08-30
            /*
            mCategoryLooksAdapter.imageLoaded();
            mCategoryBordersAdapter.imageLoaded();
            mCategoryGeometryAdapter.imageLoaded();
            mCategoryFiltersAdapter.imageLoaded();
            */
            //Aurora <SQF> <2014-08-30>  for NEW_UI end
            
            mLoadBitmapTask = null;

            MasterImage.getImage().warnListeners();
            loadActions();
            mLoadingComplete = false;

            if (mOriginalPreset != null) {
                MasterImage.getImage().setLoadedPreset(mOriginalPreset);
                MasterImage.getImage().setPreset(mOriginalPreset,
                        mOriginalPreset.getLastRepresentation(), true);
                mOriginalPreset = null;
            } else {
                setDefaultPreset();
            }

            MasterImage.getImage().resetGeometryImages(true);
            
            //Aurora <SQF> <2014-08-30>  for NEW_UI begin
            //SQF ANNOTATED ON 2014-08-30
            /*
            if (mAction == TINY_PLANET_ACTION) {
                showRepresentation(mCategoryFiltersAdapter.getTinyPlanet());
            }
            */
            //Aurora <SQF> <2014-08-30>  for NEW_UI end
            LoadHighresBitmapTask highresLoad = new LoadHighresBitmapTask();
            highresLoad.execute();
            MasterImage.getImage().warnListeners();
            super.onPostExecute(result);
        }

    }

    private void clearGalleryBitmapPool() {
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Free memory held in Gallery's Bitmap pool.  May be O(n) for n bitmaps.
                GalleryBitmapPool.getInstance().clear();
                return null;
            }
        }).execute();
    }

    @Override
    protected void onDestroy() {
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        mUserPresetsManager.close();
        doUnbindService();
        super.onDestroy();
    }

    // TODO: find a more robust way of handling image size selection
    // for high screen densities.
    private int getScreenImageSize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return Math.max(outMetrics.heightPixels, outMetrics.widthPixels);
    }

    private void showSavingProgress(String albumName) {
        ProgressDialog progress;
        if (mSavingProgressDialog != null) {
            progress = mSavingProgressDialog.get();
            if (progress != null) {
                progress.show();
                return;
            }
        }
        // TODO: Allow cancellation of the saving process
        String progressText;
        if (albumName == null) {
            progressText = getString(R.string.saving_image);
        } else {
            progressText = getString(R.string.filtershow_saving_image, albumName);
        }
        progress = ProgressDialog.show(this, "", progressText, true, false);
        mSavingProgressDialog = new WeakReference<ProgressDialog>(progress);
    }

    private void hideSavingProgress() {
        if (mSavingProgressDialog != null) {
            ProgressDialog progress = mSavingProgressDialog.get();
            if (progress != null)
                progress.dismiss();
        }
    }

    public void completeSaveImage(Uri saveUri) {
        if (mSharingImage && mSharedOutputFile != null) {
            // Image saved, we unblock the content provider
            Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                    Uri.encode(mSharedOutputFile.getAbsolutePath()));
            ContentValues values = new ContentValues();
            values.put(SharedImageProvider.PREPARE, false);
            getContentResolver().insert(uri, values);
        }
        Intent intent = new Intent().setData(saveUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(INTENT_DATA_FROM_FILTERSHOW_ACT, true);
        setResult(RESULT_OK, intent);
        //hideSavingProgress(); SQF_ANNOTAED on 2014.7.4
        //Log.i("SQF_LOG", "FilterShowActivity::completeSaveImage saveUri:" + saveUri);
        finish();
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider arg0, Intent arg1) {
        // First, let's tell the SharedImageProvider that it will need to wait
        // for the image
        Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                Uri.encode(mSharedOutputFile.getAbsolutePath()));
        ContentValues values = new ContentValues();
        values.put(SharedImageProvider.PREPARE, true);
        getContentResolver().insert(uri, values);
        mSharingImage = true;

        // Process and save the image in the background.
        showSavingProgress(null);
        mImageShow.saveImage(this, mSharedOutputFile);
        return true;
    }

    private Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(SharedImageProvider.MIME_TYPE);
        mSharedOutputFile = SaveImage.getNewFile(this, MasterImage.getImage().getUri());
        Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                Uri.encode(mSharedOutputFile.getAbsolutePath()));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        return intent;
    }
    
    //Aurora <SQF> <2014-6-10>  for NEW_UI begin
    //SQF ANNOTATED ON 2014.6.10
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filtershow_activity_menu, menu);
        MenuItem showState = menu.findItem(R.id.showImageStateButton);
        if (mShowingImageStatePanel) {
            showState.setTitle(R.string.hide_imagestate_panel);
        } else {
            showState.setTitle(R.string.show_imagestate_panel);
        }
        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share)
                .getActionProvider();
        mShareActionProvider.setShareIntent(getDefaultShareIntent());
        mShareActionProvider.setOnShareTargetSelectedListener(this);
        mMenu = menu;
        setupMenu();
        return true;
    }
    */
    public boolean onCreateOptionsMenu(Menu menu) {
    	return false;
    }
    //Aurora <SQF> <2014-6-10>  for NEW_UI end

    private void setupMenu(){
        if (mMenu == null || mMasterImage == null) {
            return;
        }
        MenuItem undoItem = mMenu.findItem(R.id.undoButton);
        MenuItem redoItem = mMenu.findItem(R.id.redoButton);
        MenuItem resetItem = mMenu.findItem(R.id.resetHistoryButton);
        MenuItem printItem = mMenu.findItem(R.id.printButton);
        //if (!PrintHelper.systemSupportsPrint()) {
            printItem.setVisible(false);
        //}
        mMasterImage.getHistory().setMenuItems(undoItem, redoItem, resetItem);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mShareActionProvider != null) {
            mShareActionProvider.setOnShareTargetSelectedListener(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Aurora <SQF> <2014-07-03>  for multiple opening begin
        MasterImage.getImage().invalidatePreview();
        //Aurora <SQF> <2014-07-03>  for multiple opening end
        if (mShareActionProvider != null) {
            mShareActionProvider.setOnShareTargetSelectedListener(this);
        }
    }

    //Aurora <SQF> <2014-6-10>  for NEW_UI begin
    //SQF ANNOTATED ON 2014.6.10
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undoButton: {
                HistoryManager adapter = mMasterImage.getHistory();
                int position = adapter.undo();
                mMasterImage.onHistoryItemClick(position);
                backToMain();
                invalidateViews();
                return true;
            }
            case R.id.redoButton: {
                HistoryManager adapter = mMasterImage.getHistory();
                int position = adapter.redo();
                mMasterImage.onHistoryItemClick(position);
                invalidateViews();
                return true;
            }
            case R.id.resetHistoryButton: {
                resetHistory();
                return true;
            }
            case R.id.showImageStateButton: {
                toggleImageStatePanel();
                return true;
            }
            case R.id.exportFlattenButton: {
                showExportOptionsDialog();
                return true;
            }
            case android.R.id.home: {
                saveImage();
                return true;
            }
            case R.id.manageUserPresets: {
                manageUserPresets();
                return true;
            }
            case R.id.showInfoPanel: {
                toggleInformationPanel();
                return true;
            }
            case R.id.printButton: {
                print();
                return true;
            }
        }
        return false;
    }
    */
    //Aurora <SQF> <2014-6-10>  for NEW_UI end

    public void print() {
    	/*
        Bitmap bitmap = MasterImage.getImage().getHighresImage();
        PrintHelper printer = new PrintHelper(this);
        printer.printBitmap("ImagePrint", bitmap);
        */
    }

    public void addNewPreset() {
        DialogFragment dialog = new PresetManagementDialog();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    private void manageUserPresets() {
        DialogFragment dialog = new PresetManagementDialog();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    private void showExportOptionsDialog() {
        DialogFragment dialog = new ExportDialog();
        dialog.show(getSupportFragmentManager(), "ExportDialogFragment");
    }

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-29
    /*
    public void updateUserPresetsFromAdapter(UserPresetsAdapter adapter) {
        ArrayList<FilterUserPresetRepresentation> representations =
                adapter.getDeletedRepresentations();
        for (FilterUserPresetRepresentation representation : representations) {
            deletePreset(representation.getId());
        }
        ArrayList<FilterUserPresetRepresentation> changedRepresentations =
                adapter.getChangedRepresentations();
        for (FilterUserPresetRepresentation representation : changedRepresentations) {
            updatePreset(representation);
        }
        adapter.clearDeletedRepresentations();
        adapter.clearChangedRepresentations();
        loadUserPresets();
    }

    public void loadUserPresets() {
        mUserPresetsManager.load();
        updateUserPresetsFromManager();
    }
    
    
    public void updateUserPresetsFromManager() {
        ArrayList<FilterUserPresetRepresentation> presets = mUserPresetsManager.getRepresentations();
        if (presets == null) {
            return;
        }
        if (mCategoryLooksAdapter != null) {
            fillLooks();
        }
        if (presets.size() > 0) {
            mCategoryLooksAdapter.add(new Action(this, Action.SPACER));
        }
        mUserPresetsAdapter.clear();
        for (int i = 0; i < presets.size(); i++) {
            FilterUserPresetRepresentation representation = presets.get(i);
            mCategoryLooksAdapter.add(
                    new Action(this, representation, Action.FULL_VIEW, true));
            mUserPresetsAdapter.add(new Action(this, representation, Action.FULL_VIEW));
        }
        if (presets.size() > 0) {
            mCategoryLooksAdapter.add(new Action(this, Action.ADD_ACTION));
        }
        mCategoryLooksAdapter.notifyDataSetChanged();
        mCategoryLooksAdapter.notifyDataSetInvalidated();
    }
	*/
    //Aurora <SQF> <2014-08-29>  for NEW_UI end
    
    public void saveCurrentImagePreset(String name) {
        mUserPresetsManager.save(MasterImage.getImage().getPreset(), name);
    }

    private void deletePreset(int id) {
        mUserPresetsManager.delete(id);
    }

    private void updatePreset(FilterUserPresetRepresentation representation) {
        mUserPresetsManager.update(representation);
    }

    public void enableSave(boolean enable) {
        mLoadingComplete = true;
        if (mSaveButton != null) {
            mSaveButton.setEnabled(enable);
        }
    }

    //Aurora <SQF> <2014-08-29>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-29
//    private void fillLooks() {
//        FiltersManager filtersManager = FiltersManager.getManager();
//        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getLooks();
//
//        if (mCategoryLooksAdapter != null) {
//            mCategoryLooksAdapter.clear();
//        }
//        mCategoryLooksAdapter = new CategoryAdapter(this);
//        int verticalItemHeight = (int) getResources().getDimension(R.dimen.action_item_height);
//        mCategoryLooksAdapter.setItemHeight(verticalItemHeight);
//        for (FilterRepresentation representation : filtersRepresentations) {
//            mCategoryLooksAdapter.add(new Action(this, representation, Action.FULL_VIEW));
//        }
//        if (mUserPresetsManager.getRepresentations() == null
//            || mUserPresetsManager.getRepresentations().size() == 0) {
//            mCategoryLooksAdapter.add(new Action(this, Action.ADD_ACTION));
//        }
//		/*
//        Fragment panel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
//        if (panel != null) {
//            if (panel instanceof MainPanel) {
//                MainPanel mainPanel = (MainPanel) panel;
//                mainPanel.loadCategoryLookPanel(true);
//            }
//        }
//        */
//        
//    }
 	//Aurora <SQF> <2014-08-29>  for NEW_UI end

    public void setDefaultPreset() {
        // Default preset (original)
        ImagePreset preset = new ImagePreset(); // empty
        mMasterImage.setPreset(preset, preset.getLastRepresentation(), true);
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Some utility functions
    // TODO: finish the cleanup.

    public void invalidateViews() {
        for (ImageShow views : mImageViews) {
            views.updateImage();
        }
    }

    public void hideImageViews() {
        for (View view : mImageViews) {
            view.setVisibility(View.GONE);
        }
        mEditorPlaceHolder.hide();
    }

    // //////////////////////////////////////////////////////////////////////////////
    // imageState panel...

    public void toggleImageStatePanel() {
    	//Log.i("SQF_LOG", "FilterShowActivity::toggleImageStatePanel");
        invalidateOptionsMenu();
        mShowingImageStatePanel = !mShowingImageStatePanel;
        Fragment panel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (panel != null) {
            if (panel instanceof EditorPanel) {
                EditorPanel editorPanel = (EditorPanel) panel;
                editorPanel.showImageStatePanel(mShowingImageStatePanel);
            } else if (panel instanceof MainPanel) {
                MainPanel mainPanel = (MainPanel) panel;
                mainPanel.showImageStatePanel(mShowingImageStatePanel);
            }
        }
    }

    
    //Aurora <SQF> <2014-08-30>  for NEW_UI begin
    //SQF ANNOTATED ON 2014-08-30
    /*
    public void toggleVersionsPanel() {
    	//Log.i("SQF_LOG", "FilterShowActivity::toggleVersionsPanel");
        mShowingVersionsPanel = !mShowingVersionsPanel;
        Fragment panel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (panel != null && panel instanceof MainPanel) {
            MainPanel mainPanel = (MainPanel) panel;
            mainPanel.loadCategoryVersionsPanel();
        }
    }
    */
    //Aurora <SQF> <2014-08-30>  for NEW_UI end
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        //Log.i("SQF_LOG", "FilterShowActivity::onConfigurationChanged");
        setDefaultValues();
        if (mMasterImage == null) {
            return;
        }
        loadXML();
        fillCategories();
        loadMainPanel();

        if (mCurrentMenu != null) {
            mCurrentMenu.dismiss();
            mCurrentMenu = null;
        }
        if (mCurrentDialog != null) {
            mCurrentDialog.dismiss();
            mCurrentDialog = null;
        }
        // mLoadBitmapTask==null implies you have looked at the intent
        if (!mShowingTinyPlanet && (mLoadBitmapTask == null)) {
            //mCategoryFiltersAdapter.removeTinyPlanet();//SQF ANNOTATED ON 2014-08-30
        }
        stopLoadingIndicator();
    }

    public void setupMasterImage() {

        HistoryManager historyManager = new HistoryManager();
        StateAdapter imageStateAdapter = new StateAdapter(this, 0);
        MasterImage.reset();
        mMasterImage = MasterImage.getImage();
        mMasterImage.setHistoryManager(historyManager);
        mMasterImage.setStateAdapter(imageStateAdapter);
        mMasterImage.setActivity(this);

        if (Runtime.getRuntime().maxMemory() > LIMIT_SUPPORTS_HIGHRES) {
            mMasterImage.setSupportsHighRes(true);
        } else {
            mMasterImage.setSupportsHighRes(false);
        }
    }

    void resetHistory() {
        HistoryManager adapter = mMasterImage.getHistory();
        adapter.reset();
        HistoryItem historyItem = adapter.getItem(0);
        ImagePreset original = null;
        if (RESET_TO_LOADED) {
            original = new ImagePreset(historyItem.getImagePreset());
        } else {
            original = new ImagePreset();
        }
        FilterRepresentation rep = null;
        if (historyItem != null) {
            rep = historyItem.getFilterRepresentation();
        }
        mMasterImage.setPreset(original, rep, true);
        invalidateViews();
        backToMain();
    }

    public void showDefaultImageView() {
        mEditorPlaceHolder.hide();
        mImageShow.setVisibility(View.VISIBLE);
		//paul add start
		MasterImage.getImage().resetValue();
		//paul add end
        MasterImage.getImage().setCurrentFilter(null);
        MasterImage.getImage().setCurrentFilterRepresentation(null);
    }

    public void backToMain() {
        Fragment currentPanel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (currentPanel instanceof MainPanel) {
            return;
        }
				
        loadMainPanel();
        showDefaultImageView();
        
        mActionBarManager.setType(ActionBarType.ABT_SAVE);
        showMainPanelContainerAnimation(true, null);
    }

    @Override
    public void onBackPressed() {
        Fragment currentPanel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
    	//Aurora <SQF> <2014-09-23>  for NEW_UI begin
        
        
        if(mShowingAuroraBeautyShotPanel) {
        	mActionBarManager.setType(ActionBarType.ABT_SAVE);
        	loadAuroraBeautyShotPanel(false);
    		return;
        }
        
        if(mShowingAuroraEffectsPanel) {
        	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(FilterShowActivity.this);
            builder.setMessage(R.string.photo_edit_cancel_filter).setTitle(R.string.photo_edit_cancel_tip_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	//paul add
                	mActionBarManager.setType(ActionBarType.ABT_SAVE);
                	removeAuroraEffectRepresentation();
            		loadAuroraEffectsPanel(false);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	dialog.dismiss();
                }
            });
            builder.show(); 
    		return;
    	}
        //Aurora <SQF> <2014-09-23>  for NEW_UI end

        if (currentPanel instanceof MainPanel) {
            if (!mImageShow.hasModifications()) {
                if (!mLoadingComplete) {
                    Log.v(LOGTAG,"Background processing is ON, rejecting back key event");
                    return;
                }
                done();
            } else {
                //Aurora <SQF> <2014-6-23>  for NEW_UI begin
                //ORIGINALLY:
            	/*
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.unsaved).setTitle(R.string.save_before_exit);
                builder.setPositiveButton(R.string.save_and_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveImage();
                    }
                });
                builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        done();
                    }
                });
                builder.show(); 
            	 */
                //SQF MODIFIED TO:

            	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
                builder.setMessage(R.string.unsaved).setTitle(R.string.save_before_exit);
                builder.setPositiveButton(R.string.save_and_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveImage();
                    }
                });
                builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        done();
                    }
                });
                builder.show(); 
            	
                //Aurora <SQF> <2014-6-23>  for NEW_UI end
                
            }
        } else {
        	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(FilterShowActivity.this);
            builder.setMessage(R.string.photo_edit_cancel_current_edit).setTitle(R.string.photo_edit_cancel_tip_title);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	//paul add
        			if (mCurrentEditor != null) {
        				mCurrentEditor.backToMain();
        			}
        			backToMain();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                	dialog.dismiss();
                }
            });
            builder.show(); 
        }
    }

    public void cannotLoadImage() {
        Toast.makeText(this, R.string.cannot_load_image, Toast.LENGTH_SHORT).show();
        finish();
    }

    // //////////////////////////////////////////////////////////////////////////////

    public float getPixelsFromDip(float value) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                r.getDisplayMetrics());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        mMasterImage.onHistoryItemClick(position);
        invalidateViews();
    }

    public void pickImage() {
    	//Log.i("SQF_LOG", "FilterShowActivity::pickImage");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                startLoadBitmap(selectedImageUri);
            } 
            /*
            else if(requestCode == AuroraTTPicUtils.REQ_TO_PITU) {
            	ArrayList<Uri> result = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            	if(result != null && result.size() >= 1) {
	            	Uri uri = result.get(0);
	            	File file = new File(uri.toString());
	            	if(file != null) {
	            		try {
	            			String uriStr = Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), "");
	            			if(uriStr != null) {
	            				Intent intent = new Intent().setData(Uri.parse(uriStr));
	            		        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	            				setResult(RESULT_OK, intent);
	            				finish();
	            			}
	            		} catch(Exception e) {
	            			if(e != null) e.printStackTrace();
	            		}
	            	}
            	}
            }
            */
            else if(requestCode == AuroraTTPicUtils.REQ_TO_PITU) {
            	ArrayList<Uri> result = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            	if(result != null && result.size() >= 1) {
	            	Uri uri = result.get(0);
	            	File file = new File(uri.toString());
	            	if(file != null) {
	            		try {
	            			String uriStr = Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), "");
	            			if(uriStr != null) {
	            				Intent intent = new Intent().setData(Uri.parse(uriStr));
	            				intent.putExtra(FilterShowActivity.INTENT_DATA_BACK_TO_PHOTOLIST, true);
	            		        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	            				setResult(RESULT_OK, intent);
	            				finish();
	            			}
	            		} catch(Exception e) {
	            			if(e != null) e.printStackTrace();
	            		}
	            	}
            	}
            }
            
        } 
    }

    public void saveImage() {
    	//Log.i("SQF_LOG", "FilterShowActivity::saveImage");
        if (mImageShow.hasModifications()) {
        	//Log.i("SQF_LOG", "FilterShowActivity::saveImage -->   mImageShow.hasModifications() mSelectedImageUri:" + mSelectedImageUri);
            // Get the name of the album, to which the image will be saved
            //File saveDir = SaveImage.getFinalSaveDirectory(this, mSelectedImageUri);
            //Aurora <SQF> <2014-6-5>  for NEW_UI begin

            //ORIGINALLY:
            //int bucketId = GalleryUtils.getBucketId(saveDir.getPath());
            //String albumName = "Image";//String albumName = LocalAlbum.getLocalizedName(getResources(), bucketId, null);
            //SQF MODIFIED TO:
            //int bucketId = GalleryUtils.getBucketId(saveDir.getPath());
            //String albumName = AuroraLocalAlbum.getLocalizedName(getResources(), bucketId, null);
            //Log.i("SQF_LOG", "FilterShowActivity::saveImage --> mImageShow.hasModifications() bucketId:" + bucketId + " albumName:" + albumName);
            //Aurora <SQF> <2014-6-5>  for NEW_UI end
            
            //showSavingProgress(albumName);
            //paul add
			if(mSavingImg) return;
            mSavingImg = true;
			
            mImageShow.saveImage(this, null);
        } else {
            done();
        }
    }


    public void done() {
    	//Log.i("SQF_LOG", "FilterShowActivity::done");
        hideSavingProgress();
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        finish();
    }

    private void extractXMPData() {
        XMresults res = XmpPresets.extractXMPData(
                getBaseContext(), mMasterImage, getIntent().getData());
        if (res == null)
            return;

        mOriginalImageUri = res.originalimage;
        mOriginalPreset = res.preset;
    }

    public Uri getSelectedImageUri() {
        return mSelectedImageUri;
    }
    
    public String getSelectedFilePath() {
        return mSelectedFilePath;
    }

    public void setHandlesSwipeForView(View view, float startX, float startY) {
        if (view != null) {
            mHandlingSwipeButton = true;
        } else {
            mHandlingSwipeButton = false;
        }
        mHandledSwipeView = view;
        int[] location = new int[2];
        view.getLocationInWindow(location);
        mSwipeStartX = location[0] + startX;
        mSwipeStartY = location[1] + startY;
    }

    public boolean dispatchTouchEvent (MotionEvent ev) {
        if (mHandlingSwipeButton) {
            int direction = CategoryView.HORIZONTAL;
            if (mHandledSwipeView instanceof CategoryView) {
                direction = ((CategoryView) mHandledSwipeView).getOrientation();
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                float delta = ev.getY() - mSwipeStartY;
                float distance = mHandledSwipeView.getHeight();
                if (direction == CategoryView.VERTICAL) {
                    delta = ev.getX() - mSwipeStartX;
                    mHandledSwipeView.setTranslationX(delta);
                    distance = mHandledSwipeView.getWidth();
                } else {
                    mHandledSwipeView.setTranslationY(delta);
                }
                delta = Math.abs(delta);
                float transparency = Math.min(1, delta / distance);
                mHandledSwipeView.setAlpha(1.f - transparency);
                mHandledSwipeViewLastDelta = delta;
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL
                    || ev.getActionMasked() == MotionEvent.ACTION_UP) {
                mHandledSwipeView.setTranslationX(0);
                mHandledSwipeView.setTranslationY(0);
                mHandledSwipeView.setAlpha(1.f);
                mHandlingSwipeButton = false;
                float distance = mHandledSwipeView.getHeight();
                if (direction == CategoryView.VERTICAL) {
                    distance = mHandledSwipeView.getWidth();
                }
                if (mHandledSwipeViewLastDelta > distance) {
                    ((SwipableView) mHandledSwipeView).delete();
                }
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public Point mHintTouchPoint = new Point();

    public Point hintTouchPoint(View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int x = mHintTouchPoint.x - location[0];
        int y = mHintTouchPoint.y - location[1];
        return new Point(x, y);
    }

    public void startTouchAnimation(View target, float x, float y) {
		/*
        final CategorySelected hint =
                (CategorySelected) findViewById(R.id.categorySelectedIndicator);
        int location[] = new int[2];
        target.getLocationOnScreen(location);
        mHintTouchPoint.x = (int) (location[0] + x);
        mHintTouchPoint.y = (int) (location[1] + y);
        int locationHint[] = new int[2];
        ((View)hint.getParent()).getLocationOnScreen(locationHint);
        int dx = (int) (x - (hint.getWidth())/2);
        int dy = (int) (y - (hint.getHeight())/2);
        hint.setTranslationX(location[0] - locationHint[0] + dx);
        hint.setTranslationY(location[1] - locationHint[1] + dy);
        hint.setVisibility(View.VISIBLE);
        hint.animate().scaleX(2).scaleY(2).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                hint.setVisibility(View.INVISIBLE);
                hint.setScaleX(1);
                hint.setScaleY(1);
                hint.setAlpha(1);
            }
        });
        */
    }
}
