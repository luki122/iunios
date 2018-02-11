/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.gallery3d.common.ApiHelper;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aurora.utils.SystemUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MyLog;




// Aurora <zhanggp> <2013-12-06> added for gallery begin
import android.provider.MediaStore.Images.Media;

import java.io.File;

import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadBroadcastReceiver;

// Aurora <zhanggp> <2013-12-06> added for gallery end
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.fragmentapp.GridViewFragment;
import com.android.gallery3d.fragmentapp.GridViewFragment.OnGridItemSelectListener;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.aurora.utils.SystemUtils;

import android.text.TextUtils;



public final class Gallery extends AbstractGalleryActivity implements OnCancelListener ,OnGridItemSelectListener{
    public static final String EXTRA_SLIDESHOW = "slideshow";
    public static final String EXTRA_DREAM = "dream";
    public static final String EXTRA_CROP = "crop";
    
    //Aurora <SQF> <2014-08-04>  for NEW_UI begin
    public static final String BLACK_ALPHA = "#BF000000";//75% Alpha, Black //8C000000 55% Alpha, Black
    public static final int SET_NAVI_BAR_COLOR_DELAY = 500;
    //private static final String RECENTS_PANEL_HIDDEN = "com.android.systemui.recent.aurora.RECENTS_PANEL_HIDDEN";
    //Aurora <SQF> <2014-08-04>  for NEW_UI end

    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
    public static final String KEY_GET_CONTENT = "get-content";
    public static final String KEY_GET_ALBUM = "get-album";
    public static final String KEY_TYPE_BITS = "type-bits";
    public static final String KEY_MEDIA_TYPES = "mediaTypes";
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";
    public static final String FRAGMENT_ACTION_STRING = "android.intent.action.fragment23d";//"fragment_to_gallery3d";
    public static final String GALLERY3D_TO_FRAGMENT_ACTION_STRING = "android.intent.action.3d2fragment";//"fragment_to_gallery3d";

    private static final String TAG = "Gallery";
    private Dialog mVersionCheckDialog;
    private int mTotalNum = 0;
	
	// Aurora <paul> <2015-4-23> add start	
	private boolean mIsCloudView = false;
	private int mCloudViewIndex = -1;
	// Aurora <paul> <2015-4-23> add end
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		// Aurora <zhanggp> <2013-12-09> modified for gallery begin
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		// Aurora <zhanggp> <2013-12-09> modified for gallery end
        //Aurora <SQF> <2014-10-14>  for NEW_UI begin
        MediaSetUtils.setContext(this.getAndroidContext()); 
        //Aurora <SQF> <2014-10-14>  for NEW_UI end
        
        //Aurora <SQF> <2014-07-28>  for NEW_UI begin
        if(GalleryUtils.needNavigationBarControl()) {
        	setTranslucentSystemBars(true);
        }
        //Aurora <SQF> <2014-07-28>  for NEW_UI end

        if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        //lory modify setContentView(R.layout.main);
        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	setAuroraPicContentView(R.layout.main);
        	RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.id.gallery_root);
        	//wenyongzhe
//        	setAuroraContentView(R.layout.main);
//            setAuroraContentView(R.layout.main, AuroraActionBar.Type.Empty, true);
//            startContentView(R.layout.main);//lory add
		} else {
			setContentView(R.layout.main);
		}
        
		//Aurora <SQF> <2014-08-08>  for NEW_UI begin    
        //GLRootView view = (GLRootView)this.getGLRoot();
        //view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        setLowProfileMode(); //wenyongzhe 2015.9.28
		//Aurora <SQF> <2014-08-08>  for NEW_UI end

        if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	try {
        		AuroraActionBar mActionBar = getAuroraActionBar();
            	View tView = mActionBar.getHomeButton();
            	if (tView != null) {
    				tView.setVisibility(View.GONE);
    			}
            	mActionBar.setmOnActionBarBackItemListener(mOnActionBarBackItemListener);
			} catch (Exception e) {
				Log.i(TAG, "zll --- Gallery onCreate ActionBar fail");
			}
		}
        
        if (savedInstanceState != null) {
            getStateManager().restoreFromState(savedInstanceState);
        } else {
            initializeByIntent();
        }
        
        //TEST....
        /*
        new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(XCloudAutoUploadBroadcastReceiver.XCLOUD_AUTO_UPLOAD_ACTION);
				Gallery.this.sendBroadcast(intent);
			}
        	
        }, 3000);
        */

    }
    
    
    
    //Aurora <SQF> <2014-07-28>  for NEW_UI begin
    private static final String NAVI_KEY_HIDE = "navigation_key_hide";
    
    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    public void setNavigationBarShowStatus(boolean show) {
    	ContentResolver contentResolver = this.getAndroidContext().getContentResolver(); 
        String projection[] = new String[]{ android.provider.Settings.System.VALUE };
        ContentValues values = new ContentValues();
        values.put(android.provider.Settings.System.NAME, NAVI_KEY_HIDE);
        if(show) {
        	values.put(android.provider.Settings.System.VALUE, "0");
        } else {
        	values.put(android.provider.Settings.System.VALUE, "1");
        }
        contentResolver.insert(android.provider.Settings.System.CONTENT_URI, values);
    }
    
	/*
    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    public boolean isShowingNavigationBar() {  
    	//test begin
    	int navigationBarHeight = getNavigationBarHeight();
    	Log.i("SQF_LOG", "navigationBarHeight:" + navigationBarHeight);
    	//test end
    	boolean isShowing = true;
        ContentResolver contentResolver = this.getAndroidContext().getContentResolver(); 
        String projection[] = new String[]{ android.provider.Settings.System.VALUE };
        Cursor cursor = null;
        cursor = contentResolver.query(android.provider.Settings.System.CONTENT_URI,  
        		projection, android.provider.Settings.System.NAME + "='" + NAVI_KEY_HIDE + "'", null, null);  
        boolean b = cursor.moveToFirst();
    	int columnIndex = cursor.getColumnIndex(android.provider.Settings.System.VALUE);
    	String value = cursor.getString(columnIndex);
    	if(value.equalsIgnoreCase("0")) {
    		isShowing = true;
    	} else if(value.equalsIgnoreCase("1")) {
    		isShowing = false;
    	}
        cursor.close();  
        return isShowing;
    }  
	*/
    
    public int getNavigationBarHeight() {
    	int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
    	int navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
    	return navigationBarHeight;
    }

    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
	public void setTranslucentSystemBars(boolean tf) {
	        if (Build.VERSION.SDK_INT >= ApiHelper.VERSION_CODES.KITKAT) {
	        	Window window = getWindow();
	        	if(tf) {
		        	window.setFlags(ApiHelper.FLAG_TRANSLUCENT_STATUS, ApiHelper.FLAG_TRANSLUCENT_STATUS);
		        	window.setFlags(ApiHelper.FLAG_TRANSLUCENT_NAVIGATION, ApiHelper.FLAG_TRANSLUCENT_NAVIGATION);
	        	} else {
	        		window.clearFlags(ApiHelper.FLAG_TRANSLUCENT_STATUS);
	        		window.clearFlags(ApiHelper.FLAG_TRANSLUCENT_NAVIGATION);
	            }
	        }
    }

	public static final int MODE_UNSET = -1;
    public static final int MODE_CUSTOM_COLOR = 6;
    /**
     * color string可以是: #RRGGBB #AARRGGBB 标准格式, 必须以'#'开头
     * 或者：
     *     'red', 'blue', 'green', 'black', 'white', 'darkgray', 'grey', 'lightgrey',
     *     'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'olive', 'purple', 'silver', 'teal'
     *     'darkgrey', 'aqua', 'fuschia', 'lime', 'maroon', 'navy' 
     *
     * 如果传入的 color string不符合标准, 会抛出 IllegalArgumentException exception。请自行处理。
     */
    
    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    private void setNavigationBarColor(String color) {
        Intent intent = new Intent("aurora.action.SET_NAVIBAR_COLOR");
        intent.putExtra("mode", MODE_CUSTOM_COLOR );
        intent.putExtra("color", color);
        this.sendBroadcast(intent);
    }

    /**
     * 设置了颜色之后，不再需要时一定要重设 mode 为-1。否则无法恢复android标准的状态!!!
     */
    @TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    public void unsetNavigationBarColor() {
        Intent intent = new Intent("aurora.action.SET_NAVIBAR_COLOR");
        intent.putExtra("mode", MODE_UNSET);
        //intent.putExtra("color", color);
        this.sendBroadcast(intent);
    }
    //Aurora <SQF> <2014-07-28>  for NEW_UI end
    
    //lory add start ----------------------//
    private OnAuroraActionBarBackItemClickListener mOnActionBarBackItemListener = new OnAuroraActionBarBackItemClickListener(){
    	@Override
    	public void onAuroraActionBarBackItemClicked(int itemid){
        	return;
        }
    };
    //lory add end ----------------------//
    
    private void initializeByIntent() {
    	mIsCloudView = false;// Aurora <paul> <2015-4-23> add
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
            startGetContent(intent);
		// Aurora <paul> <2013-12-24> added for gallery begin				
        }else if ("android.intent.action.GN_GET_CONTENT".equalsIgnoreCase(action)) {
            startGetContent(intent);
		// Aurora <paul> <2013-12-24> added for gallery end	
        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            // We do NOT really support the PICK intent. Handle it as
            // the GET_CONTENT. However, we need to translate the type
            // in the intent here.
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image")) intent.setType("image/*");
                if (type.endsWith("/video")) intent.setType("video/*");
            }
            startGetContent(intent);
        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(action)
                || ACTION_REVIEW.equalsIgnoreCase(action)){
            startViewAction(intent);
		} else if ("aurora.cloud.action.VIEW".equalsIgnoreCase(action)){// Aurora <paul> <2015-4-22> add
			mIsCloudView = true;
			mCloudViewIndex = intent.getIntExtra("position", -1);
			startViewAction(intent);
        } else if (FRAGMENT_ACTION_STRING.equals(action)) {
        	startPhotoPage(intent);
		} else {
            //startDefaultPage();
        	startTargetPage(intent);//lory add
        }
    }
    
    public void startPhotoPage(Intent intent) {
        PicasaSource.showSignInReminder(this);
        
        Bundle tBundle = intent.getExtras();
        if (tBundle == null) {
			return;
		}
        
        int position = tBundle.getInt(PhotoPage.KEY_INDEX_HINT);
        Bundle data = new Bundle();
        String defultPath = Path.fromString("/local/allsets/" + MediaSetUtils.CAMERA_BUCKET_ID).toString();
        
        data.putInt(PhotoPage.KEY_INDEX_HINT, position);
        data.putString(PhotoPage.KEY_MEDIA_SET_PATH, defultPath);
        getStateManager().startState(PhotoPage.class, data);
        mVersionCheckDialog = PicasaSource.getVersionCheckDialog(this);
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.setOnCancelListener(this);
        }
    }
    
    public void startTargetPage(Intent intent) {
		
    	if (MySelfBuildConfig.USEALBUMPAGE_FLAG) {
			{
				PicasaSource.showSignInReminder(this);
		        Bundle data = new Bundle();
		        
				//Path PATH_ALL = Path.fromString("/local/all");
				//String defultPath = PATH_ALL.getChild(MediaSetUtils.CAMERA_BUCKET_ID).toString();
		        //String defultPath = Path.fromString("/local/all").getChild(MediaSetUtils.CAMERA_BUCKET_ID).toString();
		        String defultPath = Path.fromString("/local/allsets/" + MediaSetUtils.CAMERA_BUCKET_ID).toString();
				data.putString(AlbumPage.KEY_MEDIA_PATH, defultPath);
			    data.putBoolean(AlbumPage.KEY_SHOW_CLUSTER_MENU, true);
			    data.putInt(AlbumPage.KEY_ALLITEM_NUM, mTotalNum);
			    //MyLog.i("SQF_LOG", "startTargetPage:: defultPath ----> " + defultPath);
			    //SQF ADD ON 2015.5.4 begin
			    if(intent.getBooleanExtra(CloudItemFragment.FROM_XCLOUD_MULTLI_SELECTION, false)) {
			    	data.putBoolean(CloudItemFragment.FROM_XCLOUD_MULTLI_SELECTION, true);
			    }
			    //SQF ADD ON 2015.5.4 end
			    //data.putBoolean(AlbumPage.KEY_WHETHER_IN_ROOT_PAGE, true);
			    getStateManager().startState(AlbumPage.class, data);
			   
			    
			    mVersionCheckDialog = PicasaSource.getVersionCheckDialog(this);
			    if (mVersionCheckDialog != null) {
			        mVersionCheckDialog.setOnCancelListener(this);
			    }
			}
		}
		else {
			startDefaultPage();
		}
	}

    public void startDefaultPage() {
        PicasaSource.showSignInReminder(this);
        Bundle data = new Bundle();
        data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                getDataManager().getTopSetPath(DataManager.INCLUDE_ALL));
        getStateManager().startState(AlbumSetPage.class, data);
        mVersionCheckDialog = PicasaSource.getVersionCheckDialog(this);
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.setOnCancelListener(this);
        }
    }

    private void startGetContent(Intent intent) {
        Bundle data = intent.getExtras() != null
                ? new Bundle(intent.getExtras())
                : new Bundle();
        data.putBoolean(KEY_GET_CONTENT, true);
        int typeBits = GalleryUtils.determineTypeBits(this, intent);

		// Aurora <zhanggp> <2013-12-21> modified for gallery begin	
		String defultPath = Path.fromString(GalleryUtils.getMediaSetPath(typeBits) + MediaSetUtils.CAMERA_BUCKET_ID).toString();
		
        data.putString(AlbumPage.KEY_MEDIA_PATH,defultPath);
        if (!MySelfBuildConfig.USEGALLERY3D_FLAG) {
        	data.putBoolean(AlbumPage.KEY_WHETHER_IN_ROOT_PAGE, true);//Iuni <lory><2013-12-29> add begin
		}
        
        data.putInt(AlbumPage.KEY_ALLITEM_NUM, mTotalNum);//Iuni <lory><2014-04-10> add 
        data.putInt(KEY_TYPE_BITS, typeBits); //Iuni <lory><2013-12-29> add begin
        //data.putString(AlbumSetPage.KEY_MEDIA_PATH,
        //        getDataManager().getTopSetPath(typeBits));
        getStateManager().startState(AlbumPage.class, data);//AlbumSetPage
		// Aurora <zhanggp> <2013-12-21> modified for gallery end
    }

    private String getContentType(Intent intent) {
        String type = intent.getType();
        if (type != null) {
            return GalleryUtils.MIME_TYPE_PANORAMA360.equals(type)
                ? MediaItem.MIME_TYPE_JPEG : type;
        }

        Uri uri = intent.getData();
        try {
            return getContentResolver().getType(uri);
        } catch (Throwable t) {
            Log.w(TAG, "get type fail", t);
            return null;
        }
    }

    private void startViewAction(Intent intent) {
        Boolean slideshow = intent.getBooleanExtra(EXTRA_SLIDESHOW, false);
        if (slideshow) {
            getActionBar().hide();
            DataManager manager = getDataManager();
            Path path = manager.findPathByUri(intent.getData(), intent.getType());
            if (path == null || manager.getMediaObject(path)
                    instanceof MediaItem) {
                path = Path.fromString(
                        manager.getTopSetPath(DataManager.INCLUDE_IMAGE));
            }
            Bundle data = new Bundle();
            data.putString(SlideshowPage.KEY_SET_PATH, path.toString());
            data.putBoolean(SlideshowPage.KEY_RANDOM_ORDER, true);
            data.putBoolean(SlideshowPage.KEY_REPEAT, true);
            if (intent.getBooleanExtra(EXTRA_DREAM, false)) {
                data.putBoolean(SlideshowPage.KEY_DREAM, true);
            }
            
            //Iuni <lory><2014-03-03> add begin
            if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
            	AuroraActionBar mActionBar = getAuroraActionBar();
            	if (mActionBar != null) {
            		mActionBar.setVisibility(View.GONE);
    			}
    		}
            //Iuni <lory><2014-02-03> add end
            
            getStateManager().startState(SlideshowPage.class, data);
        } else {
            Bundle data = new Bundle();
            DataManager dm = getDataManager();
            Uri uri = intent.getData();
            String contentType = getContentType(intent);
            if (contentType == null) {
                Toast.makeText(this,
                        R.string.no_such_item, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            if (uri == null) {
				// Aurora <paul> <2014-05-08> start
				startTargetPage(intent);//SQF ADD intent on 2015.5.3
				/*
                int typeBits = GalleryUtils.determineTypeBits(this, intent);
                data.putInt(KEY_TYPE_BITS, typeBits);
                data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                        getDataManager().getTopSetPath(typeBits));
                getStateManager().startState(AlbumSetPage.class, data);
                */
				// Aurora <paul> <2014-05-08> end
            } else if (contentType.startsWith(
                    ContentResolver.CURSOR_DIR_BASE_TYPE)) {
                int mediaType = intent.getIntExtra(KEY_MEDIA_TYPES, 0);
                if (mediaType != 0) {
                    uri = uri.buildUpon().appendQueryParameter(
                            KEY_MEDIA_TYPES, String.valueOf(mediaType))
                            .build();
                }
                Path setPath = dm.findPathByUri(uri, null);
                MediaSet mediaSet = null;
                if (setPath != null) {
                    mediaSet = (MediaSet) dm.getMediaObject(setPath);
                }
                if (mediaSet != null) {
                    if (mediaSet.isLeafAlbum()) {
                        data.putString(AlbumPage.KEY_MEDIA_PATH, setPath.toString());
                        data.putString(AlbumPage.KEY_PARENT_MEDIA_PATH,
                                dm.getTopSetPath(DataManager.INCLUDE_ALL));
                        getStateManager().startState(AlbumPage.class, data);
                    } else {
						// Aurora <paul> <2014-05-08> start
                        data.putString(AlbumPage.KEY_MEDIA_PATH, setPath.toString());//AlbumSetPage
						getStateManager().startState(AlbumPage.class, data);//AlbumSetPage
						// Aurora <paul> <2014-05-08> end
                    }
                } else {
                    startTargetPage(intent);// Aurora <paul> <2014-05-08> startDefaultPage();
                }
            } else {
				// Aurora <zhanggp> <2013-12-06> modified for gallery begin
                //change file:///mnt/sdcard... type uri to context://media/external...
                //if possible.
                Uri tmpUri = tryContentMediaUri(uri);
				if(null != tmpUri) uri = tmpUri;
                Path itemPath = dm.findPathByUri(uri, null);
                if (itemPath == null) {
                    itemPath = dm.findPathByUri(uri, contentType);
					if(null == itemPath){
						Toast.makeText(this,
								R.string.fail_to_load, Toast.LENGTH_LONG).show();
						finish();
						return;
					}
                }
                Path albumPath = dm.getDefaultSetOf(itemPath);
                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, itemPath.toString());

                // TODO: Make the parameter "SingleItemOnly" public so other
                //       activities can reference it.
                boolean singleItemOnly = (albumPath == null)
                        || intent.getBooleanExtra("SingleItemOnly", false);
                if (!singleItemOnly) {
                    data.putString(PhotoPage.KEY_MEDIA_SET_PATH, albumPath.toString());
                    // when FLAG_ACTIVITY_NEW_TASK is set, (e.g. when intent is fired
                    // from notification), back button should behave the same as up button
                    // rather than taking users back to the home screen
                    //paul del
                    /*
                if (intent.getBooleanExtra(PhotoPage.KEY_TREAT_BACK_AS_UP, false)) {
                        data.putBoolean(PhotoPage.KEY_TREAT_BACK_AS_UP, true);
                    }
                    */
                }
                
                //Iuni <lory><2014-03-03> add begin
                if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
                	AuroraActionBar mActionBar = getAuroraActionBar();
                	if (mActionBar != null) {
                		mActionBar.setVisibility(View.GONE);
        			}
        		}
                //Iuni <lory><2014-02-03> add end
                getStateManager().startState(PhotoPage.class, data);
				/*
                Path itemPath = dm.findPathByUri(uri, contentType);
                Path albumPath = dm.getDefaultSetOf(itemPath);

                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, itemPath.toString());

                // TODO: Make the parameter "SingleItemOnly" public so other
                //       activities can reference it.
                boolean singleItemOnly = (albumPath == null)
                        || intent.getBooleanExtra("SingleItemOnly", false);
                if (!singleItemOnly) {
                    data.putString(PhotoPage.KEY_MEDIA_SET_PATH, albumPath.toString());
                    // when FLAG_ACTIVITY_NEW_TASK is set, (e.g. when intent is fired
                    // from notification), back button should behave the same as up button
                    // rather than taking users back to the home screen
                    if (intent.getBooleanExtra(PhotoPage.KEY_TREAT_BACK_AS_UP, false)
                            || ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0)) {
                        data.putBoolean(PhotoPage.KEY_TREAT_BACK_AS_UP, true);
                    }
                }

                getStateManager().startState(PhotoPage.class, data);
				*/
				// Aurora <zhanggp> <2013-12-06> modified for gallery end
            }
        }
    }

    //Aurora <SQF> <2014-07-31>  for NEW_UI begin
	//cannot set navigation bar in onCreate and onResume, use handler to sendMessage
    private Handler mChangeNavigationBarColorHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(GalleryUtils.needNavigationBarControl()) {
				if(msg.what == MODE_CUSTOM_COLOR) {
					setNavigationBarColor(BLACK_ALPHA);
				} else if(msg.what == MODE_UNSET) {
					unsetNavigationBarColor();
				}
			}
		}
    	
    };
    
	@TargetApi(ApiHelper.VERSION_CODES.KITKAT)
    public void setNavigationBarColorByMessage(/*String color*/) {
    	if(mChangeNavigationBarColorHandler.hasMessages(MODE_CUSTOM_COLOR)) {
    		mChangeNavigationBarColorHandler.removeMessages(MODE_CUSTOM_COLOR);
    	}
    	Message msg = mChangeNavigationBarColorHandler.obtainMessage(MODE_CUSTOM_COLOR);
    	mChangeNavigationBarColorHandler.sendMessageDelayed(msg, SET_NAVI_BAR_COLOR_DELAY);
    }
    //Aurora <SQF> <2014-07-31>  for NEW_UI end
    
    
    
    @Override
    public void onResume() {
        //lory del 
    	Utils.assertTrue(getStateManager().getStateCount() > 0);
        super.onResume();
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.show();
        }
    }
    
    
    //
    /*
    public class NavigationBarReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getAction().equals(RECENTS_PANEL_HIDDEN)) {
    			setNavigationBarColorByMessage();
    			Log.i("SQF_LOG","NavigationBarReceiver onReceive...");
    			//setNavigationBarColor(BLACK_ALPHA);
    		}
    	}
    }
    
    private NavigationBarReceiver mNavigationBarReceiver = new NavigationBarReceiver();
    */
    //

    @Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		//Aurora <SQF> <2014-07-31>  for NEW_UI begin
        if(GalleryUtils.needNavigationBarControl()) {
        	setNavigationBarColorByMessage();

        	//IntentFilter filter = new IntentFilter(RECENTS_PANEL_HIDDEN);
        	//registerReceiver(mNavigationBarReceiver, filter); 
        }
        //Aurora <SQF> <2014-07-31>  for NEW_UI end
	}

	@Override
    protected void onPause() {
        super.onPause();
        //Aurora <SQF> <2014-07-31>  for NEW_UI begin
        if(GalleryUtils.needNavigationBarControl()) {
        	mChangeNavigationBarColorHandler.sendEmptyMessageDelayed(MODE_UNSET, 500);
        	
        	//unregisterReceiver(mNavigationBarReceiver);
        	
        }
        //Aurora <SQF> <2014-07-31>  for NEW_UI end
        if (mVersionCheckDialog != null) {
            mVersionCheckDialog.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialog == mVersionCheckDialog) {
            mVersionCheckDialog = null;
        }
    }
// Aurora <zhanggp> <2013-12-06> added for gallery begin
    private Uri tryContentMediaUri(Uri uri) {
        if (null == uri) {
            return null;
        }
        
        String scheme = uri.getScheme();
        if (!ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri;
        } else {
            String path = uri.getPath();
            Log.d(TAG, "tryContentMediaUri:for " + path);
            if (!new File(path).exists()) {
                return null;
            }
        }

        Cursor cursor = null;
        try {
            //for file kinds of uri, query media database
            cursor = Media.query(
                    getContentResolver(), Media.getContentUri("external"), 
                    new String[] {Media._ID, Media.BUCKET_ID},
                    "_data=(?)", new String[] {uri.getPath()},
                    null);// " bucket_id ASC, _id ASC");
            if (null != cursor && cursor.moveToNext()) {
                long id = cursor.getLong(0);
                final String imagesUri = Media.getContentUri("external").toString();
                uri = Uri.parse(imagesUri + "/" + id);
                Log.i(TAG,"tryContentMediaUri:got " + uri);
            } else {
                Log.w(TAG,"tryContentMediaUri:fail to convert " + uri);
            }
		}  catch (Exception e) {//catch exception
				Log.e(TAG, "query exception!");
		} finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }

        return uri;
    }
	
	public boolean inCloudView(){
		return mIsCloudView;
	}
    public void onIndexChanged(int curIndex){
		if(mIsCloudView){
	    	if(curIndex == 0) return;
			mCloudViewIndex += curIndex;
			BaiduAlbumUtils.getInstance(getApplicationContext()).downloadSingleImg(mCloudViewIndex, curIndex > 0);
	    }
	}

	public boolean isImgDownloaded(String name){//int offset
		if(mIsCloudView){
			if(TextUtils.isEmpty(name)) return false;
			return BaiduAlbumUtils.getInstance(getApplicationContext()).isImgDownloaded(name);
		}
		return false;
	}

// Aurora <zhanggp> <2013-12-06> added for gallery end

    //lory add
	@Override
	public void onGridItemSelectListener(int index, Rect mRect) {
		// TODO Auto-generated method stub
		getStateManager().setSelectIndex(index);
		GalleryUtils.setIndexRect(mRect);
		return;
	}
	
    //Aurora <SQF> <2014-08-19>  for NEW_UI begin
	public void setLowProfileMode() {
		GLRootView view = (GLRootView)this.getGLRoot();
		int flags = view.getSystemUiVisibility();
		flags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        view.setSystemUiVisibility(flags);
		
	}
	//Aurora <SQF> <2014-08-19>  for NEW_UI end

	
}
