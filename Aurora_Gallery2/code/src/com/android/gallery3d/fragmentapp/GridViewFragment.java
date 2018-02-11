package com.android.gallery3d.fragmentapp;




import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.fragmentutil.ImageCache.ImageCacheParams;
import com.android.gallery3d.fragmentutil.ImageResizer;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.fragmentutil.RecyclingImageView;
import com.android.gallery3d.fragmentutil.MyUtils;


import android.R.integer;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
//import android.app.ProgressDialog;
import aurora.app.AuroraProgressDialog;
import android.content.BroadcastReceiver;
//import android.app.ActionBar.LayoutParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
//import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.fragmentdata.GalleryItem;
import com.android.gallery3d.fragmentdata.GridApapter;

import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraActionBarItem;

import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import com.android.gallery3d.fragmentdata.GridApapter.OnGridItemClickListener;
import com.android.gallery3d.fragmentdata.GridApapter.OnGridItemLongClickListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import aurora.app.AuroraAlertDialog;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Future;

import com.android.gallery3d.fragmentutil.AnimationAdapter;
import com.android.gallery3d.fragmentutil.AlphaInAnimationAdapter;



@SuppressLint("NewApi")
public class GridViewFragment extends Fragment implements OnGridItemClickListener, 
OnGridItemLongClickListener, OnTouchListener{

	private static final String TAG = "GridViewFragment";
	private static final String IMAGE_CACHE_DIR = "mythumbs";
	
	public static final String KEY_MEDIA_PATH = "media-path";
	
	private static final int FRAGMENT_BIT_LOADING_RELOAD = 1;
	private static final int FRAGMENT_BIT_LOADING_SYNC = 2;
	private int mLoadingBits = 0;
	private boolean mLoadingFailed;
	private int mSyncResult;
	private boolean mInitialSynced = false;
	
	private ImageResizer mImageResizer;
	private GridApapter<GalleryItem> mAdapter;
	private ListView mListView;
	
	private final int TYPE_PIC = 1;
	private final int TYPE_VIDEO = 2;
	
	private final int m_Padding = 10;
	private int m_Displaywidth = 0;
	private int m_Displayheight = 0;
	private int m_ImageThumbSize = 0;
	private int m_leftlayoutwidth = 0;
	//private int m_leftparentwidth = 3;
	private int m_RowColumns = 3;
	private int m_tmpRows = m_RowColumns;
	private int m_horizontalSpacing = 0;
	private int m_rightSpacing = 10;
	private int m_verticalSpacing = 0;
	private int m_totalnum = 0;
	private int m_tilestatubarhigh = -1;
	
	private int m_lastheight = 0;
	private int totalHeight = 0;
	private boolean m_first = false;
	
	private ScaleGestureDetector mScaleDetector;
	private boolean m_bMounted = false;
	
	//private Cursor mCursor = null;
	//private Cursor imgCursor = null;
	public static String[] imageThumbUrls = null; //not use now
	private List<GalleryItem> tmplist = null;
	private String [] m_datas;//not use now
	
	private ContentResolver m_Resolver;
	
	private int MAXHEAD_ID = 9999;
	
	//动画
	private AnimationInterpolator mAnimationController = null;
	
	private GridViewObservable mObservable = null;
	private Vibrator mVibrator;
	//private ActionBar mActionBar;
	private boolean m_bShowCheckbox = false;

	private TextView mActionText;
	private static final int MENU_SELECT_ALL = 0;
	private HashMap<Long, Boolean> mSelectMap = new HashMap<Long, Boolean>();//Integer = id 
	
	private ArrayList<Long> mSlectedId = null;
	
	private  boolean m_oneflag = false;
	private final static int MENU_TITEL_ID = 11;
	private boolean m_allselect = false;
	private boolean m_deleteselect = false;
	
	//photopage 相关
	private boolean mIsActive = false;
	private AuroraActionBar mActionBar;
	private int mScreenNum = 0;
	
	private static final int MSG_ACTIONBAR_DELETE = 0;
	private static final int MSG_ADD_NUMCOLUMS = 1;
	private static final int MSG_REMOVE_NUMCOLUMS = 2;
	private static final int MSG_UPDATE_IMGDATABASE = 3;
	private static final int MSG_UPDATE_VIDEODATABASE = 4;
	private static final int MSG_UPDATE_THUMBS= 5;
	private static final int MSG_UPDATE_SLOTVIEW_INDEX= 6;
	private static final int MSG_UPDATE_DATELAYOUT= 7;
	
	private static final int MENU_ITEMS_CANCEL = 1;
	
	private static final int FIRST_NUM = 15;
	//private static int MSG_NUM = 0;
	
	public boolean bDelteMenuShow = false;
	private boolean bMultiSelect = false;
	private boolean bAll = false;//select all or not select all
	
	private final static String m_Path1 = "/storage/sdcard0/照相机/Camera";
	private final static String m_Path = "/storage/sdcard0/DCIM";
	private final static int IMAGE_TYPE = 1;
	private final static int VIDEO_TYOE = IMAGE_TYPE+1;
	public static String AURORA_PIC_MANAGER = "com.aurora.filemanager";
	public static String AURORA_PIC_MANAGER_ACTION = "com.aurora.pic.file.manager.action";
	
	private float OldListY = 0;
	private int g_ListIndex = 0;
	private int mScrolledX = 0;
	private int mScrolledY = 0;
	
	private int m_FoucsIndex = 0;
	
	private OnGridItemSelectListener mSelectListener;
	private AlbumDataLoader mDataLoader;
	private MediaSet mMediaSet;
    private Path mPath;
    private boolean mGetContent;
    private boolean m_bOnlyVideo = false;
    private Bundle mDataBundle;
    private boolean mIsGnCrop;
    private boolean mIsPinch = false;
    private AuroraProgressDialog mProgressDialog;
    private boolean m_bRegisterReceiver = false;
    private boolean m_bHide = false;
    
    private final Uri mImgBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
    private final Uri mVideoBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
    
    private String mImgWhereClause;
    private String mVideoWhereClause;
    private String []m_ImgSelectionArgs;
    private String []m_VideoSelectionArgs;
    
    private boolean m_bStopThread = false;
    private boolean m_bBackGroud = false;
    private boolean m_bClearData = false;
    
    /*private LinearLayout titleLayout;
    private TextView title_str;*/
    private RelativeLayout date_GroupLayout;
    private TextView date_TextView;
    private TextView year_TextView;
    private TextView wenzi_TextView;
    private int mheader_paddingtop;
    private int lastFirstVisibleItem = -1;
    //private static final String AURORA_DEFAULT_NUMBER_FONT_PATH = "system/fonts/Roboto-Light.ttf";
	//private Typeface m_auroraNumberTf;
	private View mheader_background;
	
	private View mheader_actionbarBg;
	
	
	private onResumeDataLoader resumeDataLoader = new onResumeDataLoader();
    private static final String[] COUNT_PROJECTION = { "count(*)" };
    
    
	
	static final String[] PROJECTION_IMG =  {
        ImageColumns._ID,           // 0
        ImageColumns.TITLE,         // 1
        ImageColumns.MIME_TYPE,     // 2
        ImageColumns.LATITUDE,      // 3
        ImageColumns.LONGITUDE,     // 4
        ImageColumns.DATE_TAKEN,    // 5
        ImageColumns.DATE_ADDED,    // 6
        ImageColumns.DATE_MODIFIED, // 7
        ImageColumns.DATA,          // 8
        ImageColumns.ORIENTATION,   // 9
        ImageColumns.BUCKET_ID,     // 10
        ImageColumns.SIZE,          // 11
        "0",                        // 12
        "0"                         // 13
	};
	
	static final String[] PROJECTION_VIDEO = {
        VideoColumns._ID,
        VideoColumns.TITLE,
        VideoColumns.MIME_TYPE,
        VideoColumns.LATITUDE,
        VideoColumns.LONGITUDE,
        VideoColumns.DATE_TAKEN,
        VideoColumns.DATE_ADDED,
        VideoColumns.DATE_MODIFIED,
        VideoColumns.DATA,
        VideoColumns.DURATION,
        VideoColumns.BUCKET_ID,
        VideoColumns.SIZE,
        VideoColumns.RESOLUTION,
	};
	
	
	/*
     * Empty constructor as per the Fragment documentation
     */
    public GridViewFragment() {}

    
	public interface OnGridItemSelectListener{
		public void onGridItemSelectListener(int index, Rect mRect);
	}
	
	private void onFragmentGetContent(final MediaItem item) {
		

    }
	
	private void onFragmentToGetContent(Uri tUri) {
	
    }

	private void switchOtherActivity(Long id, int position, View v){
		Log.i(TAG, "zll --- switchOtherActivity 1 position:"+position+",id:"+id);
		
		if (mGetContent || mIsGnCrop) {
			GalleryItem nowitem = mAdapter.getGalleryItem(position);
			if (nowitem == null) {
				return;
			}
			
			Uri tUri;
			if (nowitem.getType() == TYPE_PIC) {
				tUri = Uri.withAppendedPath(mImgBaseUri, "" + nowitem.getId());
			} else {
				tUri = Uri.withAppendedPath(mVideoBaseUri, "" + nowitem.getId());
			}
			onFragmentToGetContent(tUri);
		} else {
			final Intent tIntent = new Intent(getActivity(), Gallery.class);
			
			Bundle data = new Bundle();
			data.putInt(PhotoPage.KEY_INDEX_HINT, position);
			tIntent.setAction(Gallery.FRAGMENT_ACTION_STRING);
			tIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
	        tIntent.putExtras(data);
	        
	        GalleryItem nowitem = mAdapter.getGalleryItem(position);
			if (nowitem == null) {
				return;
			}
	        
			Uri tUri;
			if (nowitem.getType() == TYPE_PIC) {
				tUri = Uri.withAppendedPath(mImgBaseUri, "" + nowitem.getId());
			} else {
				tUri = Uri.withAppendedPath(mVideoBaseUri, "" + nowitem.getId());
			}
			
			if (getPathByUri(tUri)) {
				return;
			}
			
			if (MyUtils.hasJellyBean()) {
	            //ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
	            startActivityForResult(tIntent, MyFragmentActivity.FRAGMENT_REQUEST_PHOTO);
	            //getActivity().startActivity(tIntent, options.toBundle());
	            //getActivity().startActivityForResult(tIntent, MyFragmentActivity.FRAGMENT_REQUEST_PHOTO, options.toBundle());
	        } else {
	            startActivity(tIntent);
	        }
		}
		
		return;
	}
	
	private boolean getPathByUri(Uri uri){
		boolean flag = false;
		String[] proj = {MediaStore.Images.Media.DATA};
		
		Cursor cursor = null;
		try {
			cursor = m_Resolver.query(uri, proj, null, null, null);
			if (cursor != null) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				if (cursor.getString(column_index) == null) {
					flag = true;
				}
			}
		} catch (Exception e) {
			Log.i(TAG, "zll --- getPathByUri query out---");
		} finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return flag;
	}

	private void picktoPhotoPage(int slotIndex, Rect tRect) {
		if (mGetContent || mIsGnCrop) {
			if (mDataLoader == null) {
				return;
			}
			
			MediaItem item = mDataLoader.get(slotIndex);
	        if (item == null) 
	        {
	        	return; // Item not ready yet, ignore the click
	        }
	        
	        GalleryItem nowitem = mAdapter.getGalleryItem(slotIndex);
	        Uri baseUri = Uri.parse("content://media/external/images/media");
	        Uri tUri = Uri.withAppendedPath(baseUri, "" + nowitem.getId());
	        Log.i(TAG, "zll ---- picktoPhotoPage ------tttttttt 1 slotIndex:"+slotIndex+",tUri:"+tUri);
	        
			onFragmentGetContent(item);
		} else {
			Log.i(TAG, "zll ---- picktoPhotoPage ------tttttttt 2 slotIndex:"+slotIndex);
			//ImageView fragment_ani = (ImageView)(getActivity().findViewById(R.id.animation_container));
			//fragment_ani.layout(tRect.left, tRect.top, tRect.right, tRect.bottom);
			//fragment_ani.setLayoutParams(new FrameLayout.LayoutParams(m_ImageThumbSize, m_ImageThumbSize));
			//fragment_ani.setTranslationX(tRect.left);
			//fragment_ani.setTranslationY(tRect.top);
			//fragment_ani.setVisibility(View.VISIBLE);
			
			/*ImageView fragment_ani = (ImageView)(getActivity().findViewById(R.id.animation_container));
			fragment_ani.clearAnimation();
			
			if (m_Displaywidth == 0) {
				DisplayMetrics dm = new DisplayMetrics();
		    	getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				m_Displaywidth = dm.widthPixels;
				m_Displayheight = dm.heightPixels;
			}
			
			float px = tRect.left + (tRect.right-tRect.left)/2;
			float py = tRect.top + (tRect.bottom-tRect.top)/2;
			
			float endx = m_Displaywidth/2;
			float endy = m_Displayheight/2;
			
			Log.i(TAG, "zll --- jjjjjjj m_ImageThumbSize:"+m_ImageThumbSize+",px:"+px+",py:"+py+",endx:"+endx+",endy:"+endy);
			ScaleAnimation animation = new ScaleAnimation(m_ImageThumbSize/m_Displaywidth, 1f, m_ImageThumbSize/m_Displaywidth, 1f, Animation.RELATIVE_TO_SELF, tRect.left/endx, Animation.RELATIVE_TO_SELF,tRect.top/endy);
			animation.setDuration(1000);
			fragment_ani.setVisibility(View.VISIBLE);
			animation.setAnimationListener(new OnMyAnimationListener());
			fragment_ani.startAnimation(animation);
			
			if ((mAnimationController != null) && (fragment_ani != null)) {
				mAnimationController.animation_scaleIn(fragment_ani, 1000, 0);
			}*/
			
			g_slotIndex = slotIndex;
			g_rect = tRect;
			
			mSelectListener.onGridItemSelectListener(slotIndex, tRect);
		}
		
		return;
	}
	
	private int g_slotIndex = 0;
	private Rect g_rect;
	private class OnMyAnimationListener implements AnimationListener {
		

		@Override
		public void onAnimationEnd(Animation animation) {
			//mSelectListener.onGridItemSelectListener(g_slotIndex, g_rect);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mGetContent || mIsGnCrop) {
			return;
		}
		
		if (MySelfBuildConfig.USEFRAGMENTACTIVITY_FLAG) {
			return;
		}
		
		try {
			mSelectListener = (OnGridItemSelectListener)activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString() + "must implement OnGridItemSelectListener");
		}
	}
    
    public void SetAlbumPageDate(AlbumDataLoader dataLoader, Path path, Bundle mBundle, boolean getContent, boolean isGnCrop) {
    	
    	Log.i(TAG, "zll ----- SetAlbumPageDate --- getContent:"+getContent+",isGnCrop:"+isGnCrop);
    	//this.myActivity = activity;
    	this.mDataLoader = dataLoader;
    	this.mPath = path;
    	this.mDataBundle = mBundle;
    	this.mGetContent = getContent;
    	this.mIsGnCrop = isGnCrop;
		return;
	}
    
    public void SetAlbumPageMediaSet(AlbumDataLoader dataLoader, MediaSet tMediaSet) {
    	
    	//this.myActivity = activity;
    	this.mMediaSet = tMediaSet;
    	this.mDataLoader = dataLoader;
    	//Log.i(TAG, "zll --- mMediaSet count:"+mMediaSet.getMediaItemCount());
    	
		return;
	}
    
    private boolean isMediaScannerScanning(ContentResolver cr) {
        boolean result = false;
        
        if (m_Resolver == null) {
			m_Resolver = getActivity().getContentResolver();
		}
        
        Cursor cursor = Media.query(m_Resolver, MediaStore.getMediaScannerUri(),
               new String [] {MediaStore.MEDIA_SCANNER_VOLUME},
                null, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                result = "external".equals(cursor.getString(0));
            }
            cursor.close();
        }    
        
        return result;
    }

	private void initimagelist() {

    	if (tmplist == null) {
    		tmplist = new ArrayList<GalleryItem>();
		}

    	//String mWhereClause = ImageColumns.DATA+ " like ?";
		//String mWhereClause = MediaSetUtils.getImageQueryStr(getActivity());
    	mImgWhereClause = MediaSetUtils.getImageQueryStr(getActivity());
		String mOrderClause = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";
		//Uri mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
		m_ImgSelectionArgs = MediaSetUtils.geQueryParamStr(getActivity());
		//String [] selectionArgs = MediaSetUtils.geQueryParamStr(getActivity());
		//String [] selectionArgs = {m_Path+"%"};
		
		if (m_Resolver == null) {
			m_Resolver = getActivity().getContentResolver();
		}
		
		//Log.i(TAG, "zll ---- mImgWhereClause:"+mImgWhereClause);
		Cursor imgCursor = null;
		try {
			imgCursor = m_Resolver.query(mImgBaseUri, PROJECTION_IMG, mImgWhereClause, m_ImgSelectionArgs, mOrderClause);
			if (imgCursor == null) {
				return;
			}
			
			int numimg = imgCursor.getCount();
			Log.i(TAG, "zll --- img num:"+numimg);
			if (imgCursor.moveToFirst()) {
				do {
					int idIndex = imgCursor.getColumnIndexOrThrow("_id");
		            String tPath = imgCursor.getString(imgCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));//imgCursor.getString(8);
		            if (tPath == null) {
						continue;
					}
		            
		            String taken = imgCursor.getString(imgCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN));
		            if (taken == null) {
		            	taken = imgCursor.getString(imgCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED));
					}
		            
		            GalleryItem item = new GalleryItem(imgCursor.getLong(idIndex), TYPE_PIC, imgCursor.getString(idIndex), taken, tPath, imgCursor.getInt(9));
					tmplist.add(item);
					
				} while (imgCursor.moveToNext());
			}
		} finally{
			if (imgCursor != null) {
				imgCursor.close();
			}
		}
    
	}
    
    private void initvideolist() {

    	if (tmplist == null) {
			tmplist = new ArrayList<GalleryItem>();
		}
		
    	//String mWhereClause = ImageColumns.DATA+ " like ?";
		//String mWhereClause = MediaSetUtils.getVideoQueryStr(getActivity());
    	mVideoWhereClause = MediaSetUtils.getVideoQueryStr(getActivity());
		String mOrderClause = VideoColumns.DATE_TAKEN + " DESC, " + VideoColumns._ID + " DESC";
		//Uri mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
		int mBucketId = 0;
		//String [] selectionArgs = MediaSetUtils.geQueryParamStr(getActivity());
		m_VideoSelectionArgs = MediaSetUtils.geQueryParamStr(getActivity());
		//String [] selectionArgs = {m_Path+"%"};
		if (m_Resolver == null) {
			m_Resolver = getActivity().getContentResolver();
		}
		
		Cursor mCursor = null;
		try {
			mCursor = m_Resolver.query(mVideoBaseUri, PROJECTION_VIDEO, mVideoWhereClause, m_VideoSelectionArgs, mOrderClause);
			if (mCursor == null) {
				return;
			}
			
			int numv = mCursor.getCount();
			Log.i(TAG, "zll --- video num:"+numv);
			if (mCursor.moveToFirst()) {
				do {
					int idIndex = mCursor.getColumnIndexOrThrow("_id");
		            String tPath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
		            if (tPath == null) {
						continue;
					}
		            
		            String taken = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN));
		            if (taken == null) {
		            	taken = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED));
					}

		            GalleryItem item = new GalleryItem(mCursor.getLong(idIndex), TYPE_VIDEO, mCursor.getString(idIndex), taken, tPath, -1);
					tmplist.add(item);
				} while (mCursor.moveToNext());
			}
		} finally {
			if (mCursor != null) {
				mCursor.close();
			}
		}
		
		return;
    
    }

	private void initlist() {

		if (!mGetContent) {
			initimagelist();
			initvideolist();
		} else {
			int typeBits = mDataBundle.getInt(Gallery.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
			boolean tflag = false;
			if ((typeBits & DataManager.INCLUDE_IMAGE) != 0) {
				initimagelist();
				tflag = true;
			}
			
			if ((typeBits & DataManager.INCLUDE_VIDEO) != 0) {
				initvideolist();
				if (!tflag) {
					m_bOnlyVideo = true;
				}
			}
		}

		if(tmplist == null)
		{
			m_totalnum = 0;
			return;
		} else {
			if (tmplist.size() <= 0) {
				m_totalnum = 0;
			}
		}

		m_totalnum = tmplist.size();
		Log.i(TAG, "zll ---- initlist 1 m_totalnum:"+m_totalnum);

		if (m_totalnum > 0) {
			//Iuni <lory><2013-12-17> add begin
			Collections.sort(tmplist, new Comparator<GalleryItem>() {
				@Override
				public int compare(GalleryItem lhs, GalleryItem rhs) {
					 int tmp = (lhs.getTime() < rhs.getTime() ? -1 : lhs.getTime() == rhs.getTime() ? 0 : 1);
					 return -tmp;
				}
			});
		}

		return;
	}
    
    private void init_utildata() {

		//long time1 = System.currentTimeMillis();
		initlist();
		
		//Log.i(TAG, "zll --- InitData need time 1.2 is :"+(System.currentTimeMillis() - time1));
		mAdapter.CreateItemsIndex(tmplist);
		
		return;
	}
    
    private void showMySyncErrorIfNecessary() {
    	Toast.makeText(getActivity(), R.string.sync_album_error, Toast.LENGTH_LONG).show();
    }
    
    private void init_gallery3d_data(Bundle data) {
    	Log.i(TAG, "zll ---- init_gallery3d_data 111");
    	if (data == null) {
    		//showMySyncErrorIfNecessary();
    		return;
		}
    	
		mIsGnCrop = data.getBoolean("gn_crop", false);
    	mGetContent = data.getBoolean(Gallery.KEY_GET_CONTENT, false);
    	Log.i(TAG, "zll ---- init_gallery3d_data mIsGnCrop:"+mIsGnCrop+",mGetContent:"+mGetContent);
		return;
	}
    
    private int GetHighofTitleStatuBar() {
		int contentTop = 0;
		
		contentTop = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop(); 
		
		//Log.i(TAG, "zll ---GetHighofTitleStatuBar contentTop:"+contentTop);
    	return contentTop;
	}
    
    private void initActionBar(View view)
    {
    	mActionBar = ((AuroraActivity )getActivity()).getAuroraActionBar();

    	if (mActionBar == null) {
			return;
		}
    	
		if (mActionBar.getVisibility() != View.VISIBLE) {
			mActionBar.setVisibility(View.VISIBLE);
		}
		
		/*{
			if (mGetContent) {
                inflator.inflate(R.menu.pickup, menu);
                int typeBits = mData.getInt(Gallery.KEY_TYPE_BITS,DataManager.INCLUDE_IMAGE);
                actionBar.setTitle(GalleryUtils.getSelectionModePrompt(typeBits));
            }
		}*/
		
		Log.i(TAG, "zll ---- initActionBar mGetContent:"+mGetContent);
		if (mGetContent) {

	    	int typeBits = mDataBundle.getInt(Gallery.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
    		mActionBar.setTitle(GalleryUtils.getSelectionModePrompt(typeBits));
	    	
    		//((AuroraActivity )getActivity()).addAuroraActionBarItem(AuroraActionBarItem.Type.Info, MENU_ITEMS_CANCEL);
	    	/*mActionBar.addItem(R.drawable.empty_photo_bk, MENU_ITEMS_CANCEL, "fddf");
	    	mActionBar.setOnAuroraActionBarListener(auroraMenuCallBack);*/
			//mActionBar.showActionBarDashBoard();
    		
		} else {
			String num = String.format(getActivity().getString(R.string.action_title), m_totalnum);
	    	if (m_totalnum > 0) {
	    		mActionBar.setTitle(num);
			}
	    	else {
	    		mActionBar.setTitle(R.string.appwidget_title);
			}
	    	
	    	((AuroraActivity )getActivity()).setAuroraMenuCallBack(auroraItemCallBack);
	    	((AuroraActivity )getActivity()).setAuroraMenuItems(R.menu.aurora_action);
	    	
	    	mActionBar.initActionBottomBarMenu(R.menu.gallery_menu,1);

	    	//Log.i(TAG, "zll --- mActionBar:"+mActionBar);
	    	ShowActionBarMenu();
		}
    	
    	return;
    }
    
    private void ShowGetContentActionBar() {
    	//Log.i(TAG, "zll ---- ShowGetContentActionBar -----");
    	if (mActionBar.getSelectLeftButton() != null ) {
    		//Log.i(TAG, "zll ---- ShowGetContentActionBar getSelectLeftButton");
    		int typeBits = mDataBundle.getInt(Gallery.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
    		((TextView) (mActionBar.getSelectLeftButton())).setText(GalleryUtils.getSelectionModePrompt(typeBits));
    		mActionBar.getSelectLeftButton().setOnClickListener(new View.OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			Log.i(TAG, "zll ---- ShowGetContentActionBar getSelectLeftButton");
        		}
        	});
		}
    	
    	if (mActionBar.getSelectRightButton() != null) {
    		//Log.i(TAG, "zll ---- ShowGetContentActionBar getSelectRightButton");
    		((TextView) (mActionBar.getSelectRightButton())).setText(getActivity().getString(R.string.cancel));
    		mActionBar.getSelectRightButton().setOnClickListener(new View.OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			Log.i(TAG, "zll ---- ShowGetContentActionBar getSelectRightButton");
        		}
        	});
		}
    	
		return;
	}
    
    private void ShowActionBarMenu() {
    	if (mActionBar == null) {
			return;
		}
    	
    	if (mActionBar.getSelectLeftButton() != null ) {
    		mActionBar.getSelectLeftButton().setOnClickListener(new View.OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			// TODO Auto-generated method stub
        			mSelectMap.clear();
        			mAdapter.clearSelectMap();
        			mAdapter.OnClickSetCheckImgVisible(false);
        			mAdapter.notifyDataSetChanged();
        			//String num = m_totalnum+"个图片";
					String num = String.format(getActivity().getString(R.string.action_title), m_totalnum);
        	    	mActionBar.setTitle(num);
        	    	
        			mActionBar.setShowBottomBarMenu(false);
        			mActionBar.showActionBarDashBoard();
        			bMultiSelect = false;
        			m_allselect = false;
        			bDelteMenuShow = false;
        		}
        	});
		}
    	
    	if (mActionBar.getSelectRightButton() != null) {
    		mActionBar.getSelectRightButton().setOnClickListener(new View.OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			// TODO Auto-generated method stub
        			UpdateActionBarRightButton(bAll);
        			if (!bAll) {
        				if (tmplist != null) {
							for (int i = 0; i < tmplist.size(); i++) {
								mSelectMap.put(tmplist.get(i).getId(), true);
							}
						}
            			
            			m_allselect = true;
            			Log.i(TAG, "zll ---- mSelectMap size:"+mSelectMap.size());
            			mAdapter.setSelectMap(mSelectMap);
            			mAdapter.notifyDataSetChanged();
            			UpdateDeleteMenuStatus(true);
					}else {
						mSelectMap.clear();
						mAdapter.clearSelectMap();
						m_allselect = false;
	        			mAdapter.notifyDataSetChanged();
	        			UpdateDeleteMenuStatus(false);
					}
        		}
        	});
		}
    	
		return;
	}

    public void setFragmentHide(boolean bshow){
    	m_bHide = bshow;
    }
    
	public void setBottomActionBarStatus(boolean bshow)
	{
		//Log.i(TAG, "zll --- setBottomActionBarStatus mGetContent:"+mGetContent);
		if (mGetContent) {
			return;
		} else {
			if(bshow)
			{
				((AuroraActivity )getActivity()).setAuroraMenuCallBack(auroraItemCallBack);
			}else{
				((AuroraActivity )getActivity()).setAuroraMenuCallBack(null);
			}
		}
		
		return;
	}
    
    private AuroraMenuBase.OnAuroraMenuItemClickListener auroraItemCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener()
    {
    	@Override
    	public void auroraMenuItemClick(int itemId)
    	{
    		switch (itemId) {
    		case R.id.menu_tofilemanager:
    			Log.i(TAG, "zll --- auroraMenuItemClick menu_tofilemanager");
    			PackageManager packageManager = getActivity().getPackageManager();
    			Intent mIntent = new Intent();
    			try {
    				mIntent =packageManager.getLaunchIntentForPackage(AURORA_PIC_MANAGER); 
    				if (mIntent == null) {
						return;
					}
    				
    				Bundle mBundle = new Bundle();
    				mBundle.putBoolean(AURORA_PIC_MANAGER_ACTION, true);
    				mIntent.putExtras(mBundle);
				} catch (Exception e) {
					// TODO: handle exception
					Log.i(TAG, "zll --- to filemanager fail");
				}
    			
    			startActivity(mIntent);
    			break;
    			
			case R.id.menu_delete:
				onMenuDelete(itemId);
				
				/*//<!-- Iuni <lory><2013-12-11> modify start-->
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_ACTIONBAR_DELETE, MSG_NUM, 0),100);
				//<!-- Iuni <lory><2013-12-11> modify end-->
		    	
				mActionBar.setShowBottomBarMenu(false);
    			mActionBar.showActionBarDashBoard();
    			bMultiSelect = false;
    			//m_allselect = false;
    			
    			bDelteMenuShow = false;*/
				break;

			default:
				break;
			}
    	}
    };
   
    private MyAuroraAlertDialog mAuroraAlertDialog;
    private void onMenuDelete(int itemId) {
    	String confirmMsg = null;
		confirmMsg = getActivity().getResources().getQuantityString(R.plurals.delete_selection, 1);
		if (mAuroraAlertDialog != null) {
    		mAuroraAlertDialog = null;
		}
		
		mAuroraAlertDialog = new MyAuroraAlertDialog(getActivity(), mSelectMap, tmplist);
		mAuroraAlertDialog.onMenuClicked(itemId, confirmMsg, new MyAuroraAlertDialog.MyProgressListener() {
			
			@Override
			public void onProgressUpdate(int index) {
				//Log.i(TAG, "zll ---- onProgressUpdate ----");
			}
			
			@Override
			public void onProgressStart() {
			}
			
			@Override
			public void onProgressComplete(int result, int delnum) {
				Log.i(TAG, "zll --- onProgressComplete result:"+result);
				if (result == 1) {
					String num = String.format(getActivity().getString(R.string.delete_finish), delnum);
					Toast.makeText(getActivity(), num,Toast.LENGTH_LONG).show();
				}
				
				InitClearList();
				//Log.i(TAG, "zll --- 2 OnDeleteViews tmplist.size:"+tmplist.size()+",mList.size:"+mList.size()+",mIdMap.size:"+mIdMap.size()+",mIdMap.size:"+mIdMap.size());
				init_utildata();
				
				mActionBar.setShowBottomBarMenu(false);
    			mActionBar.showActionBarDashBoard();
    			bMultiSelect = false;
    			bDelteMenuShow = false;
    			
    			mAdapter.OnClickSetCheckImgVisible(false);
    			mAdapter.notifyDataSetChanged();
				
				if ((tmplist != null) && (tmplist.size() == 0)) {
					/*if (mScrollView !=null) {
						mScrollView.setVisibility(View.GONE);
					}*/
					
					//empty_layout
					TextView view2 = (TextView)getActivity().findViewById(R.id.empty_gridview);
					view2.setVisibility(View.VISIBLE);

					if (mActionBar != null) {
						mActionBar.setTitle(R.string.appwidget_title);
					}
					return;
				}

				String num = String.format(getActivity().getString(R.string.action_title), m_totalnum);
		    	mActionBar.setTitle(num);
		    	
		    	m_allselect = false;
			}
			
			@Override
			public void onConfirmDialogShown() {
			}
			
			@Override
			public void onConfirmDialogDismissed(boolean confirmed) {
				//Log.i(TAG, "zll --- onConfirmDialogDismissed confirmed:"+confirmed);
				if (!confirmed) {
					mActionBar.setShowBottomBarMenu(false);
	    			mActionBar.showActionBarDashBoard();
	    			bMultiSelect = false;
	    			bDelteMenuShow = false;
	    			
	    			if (mSelectMap != null) {
	    				mSelectMap.clear();
	    				mAdapter.clearSelectMap();
	    			}
	    			mAdapter.OnClickSetCheckImgVisible(false);
	    			mAdapter.notifyDataSetChanged();
				}
			}
		});
    	
    	
		return;
	}

    private AuroraActionBar.OnAuroraActionBarItemClickListener auroraMenuCallBack = new AuroraActionBar.OnAuroraActionBarItemClickListener() {
    	
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			Log.i(TAG, "zll ---- onAuroraActionBarItemClicked itemId:"+itemId);
			switch (itemId) {
			case R.id.menu_delete:
				Log.i(TAG, "zll ---- onAuroraActionBarItemClicked");
				Toast.makeText(getActivity(), "zll aurora_backup",Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	private void UpdateActionBarRightButton(boolean flag) {
		if ((mActionBar != null) && 
			(mActionBar.getSelectRightButton() != null)) {
			bAll = !bAll;
			if (bAll) {
				((TextView) (mActionBar.getSelectRightButton())).setText(getActivity().getString(R.string.myselect_all));
			}else {
				((TextView) (mActionBar.getSelectRightButton())).setText(getActivity().getString(R.string.unmyselect_all));
			}
		}
		return;
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//getArguments().getInt(key)
		setHasOptionsMenu(true);
		mDataBundle = getArguments();
		
		mIsActive = false;
		m_bBackGroud = false;
		tmplist = null;
		mScreenNum = 0;
		m_bOnlyVideo = false;
		
		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.2f);
		
		mAdapter = new GridApapter(getActivity(), m_RowColumns);
		
		if (MySelfBuildConfig.USEFRAGMENTACTIVITY_FLAG) {
			init_gallery3d_data(mDataBundle);
		}
		
		init_utildata();
		m_ImageThumbSize = mAdapter.getGridImgSize();
		Log.i(TAG, "zll --- onCreate m_ImageThumbSize:"+m_ImageThumbSize);
		mImageResizer = new ImageResizer(getActivity(), m_ImageThumbSize);
		mImageResizer.setLoadingImage(R.drawable.empty_photo);
		/*if (m_bClearData) {
			m_bClearData = false;
			mImageResizer.clearCache();
		}*/
		mImageResizer.addImageCache(getActivity().getFragmentManager(), cacheParams);
		mAdapter.setImageResizer(mImageResizer);
		
		mAnimationController = new AnimationInterpolator();

		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		
		mScaleDetector = new ScaleGestureDetector(getActivity(), new MyGridViewScaleListener());
		
		//getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		//StartViewAnimation();
	}

	private void initViews(View v) {
		/*titleLayout = (LinearLayout)view.findViewById(R.id.title_layout);
		title_str = (TextView)view.findViewById(R.id.title_str);*/
		mheader_paddingtop = (int)getActivity().getResources().getDimension(R.dimen.header_paddingtop);
		date_GroupLayout = (RelativeLayout)v.findViewById(R.id.header_dategroup);
		date_TextView = (TextView)v.findViewById(R.id.header_date_date);
		year_TextView = (TextView)v.findViewById(R.id.header_date_year);
		wenzi_TextView = (TextView)v.findViewById(R.id.header_date_wenzi);
		mheader_background = (View)v.findViewById(R.id.header_background);
		
		mheader_actionbarBg = (View)getActivity().findViewById(R.id.fragment_actionbar);
		
		/*
		try {
			m_auroraNumberTf = Typeface.createFromFile(AURORA_DEFAULT_NUMBER_FONT_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		return;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.gridview_fragment, container, false);
		
		initActionBar(view);
		initViews(view);
		
		if((tmplist != null) && (tmplist.size() == 0))
		{
			TextView view2 = (TextView)view.findViewById(R.id.empty_gridview);
			if (m_bOnlyVideo) {
				view2.setText(getActivity().getString(R.string.appwidget_empty_video));
			} else {
				view2.setText(getActivity().getString(R.string.appwidget_empty_text));
			}
			
			//int topdim = (int)getActivity().getResources().getDimension(R.dimen.gridview_marginTop);
			//view2.setTranslationX(topdim);
			//view2.setTranslationY(topdim);
			view2.setVisibility(View.VISIBLE);
		}
		
		mListView = (ListView)view.findViewById(R.id.listview);
		//mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		if (true) {
			AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
			animAdapter.setAbsListView(mListView);
			mListView.setAdapter(animAdapter);
		} else {
			mListView.setAdapter(mAdapter);
		}
		
		mListView.setDividerHeight(mAdapter.getSpaceBetweenChildrenInRow());
		//mGridView.setMultiChoiceModeListener(this);
		mListView.setOnTouchListener(this);
		mAdapter.OnClickSetCheckImgVisible(false);
		mAdapter.setOnListener(this);
		mAdapter.setOnLongListener(this);
		
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//Log.i(TAG, "zll --- scrollState:"+scrollState);
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageResizer.setPauseWork(true);
                } else {
                	mImageResizer.setPauseWork(false);
                	/*if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                		mScrolledX = mListView.getScrollX();
                		mScrolledY = mListView.getScrollY();
					}*/
                }
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mScreenNum == 0) {
					mScreenNum = visibleItemCount;
				}
				
				//Log.i(TAG, "zll --- onScroll visibleItemCount:"+visibleItemCount);
				mHandler.removeMessages(MSG_UPDATE_SLOTVIEW_INDEX);
				if (firstVisibleItem == 0) {
					//date_GroupLayout.setVisibility(View.GONE);
					mheader_background.setVisibility(View.GONE);
					lastFirstVisibleItem = firstVisibleItem;
					
					mheader_actionbarBg.setVisibility(View.GONE);
					return;
				}
				//mHandler.obtainMessage(MSG_UPDATE_SLOTVIEW_INDEX, firstVisibleItem, visibleItemCount).sendToTarget();
				mHandler.obtainMessage(MSG_UPDATE_SLOTVIEW_INDEX, firstVisibleItem, visibleItemCount, view).sendToTarget();
				
				/*mHandler.removeMessages(MSG_UPDATE_DATELAYOUT);
				if (firstVisibleItem == 0) {
					date_GroupLayout.setVisibility(View.GONE);
					lastFirstVisibleItem = firstVisibleItem;
					return;
				}
				
				mHandler.obtainMessage(MSG_UPDATE_DATELAYOUT, firstVisibleItem, 0, view).sendToTarget();*/
			}
		});
		
		if (mAnimationController != null) {
			mAnimationController.SetOnViewAnimationListener(new AnimationInterpolator.OnViewAnimationListener() {
				
				@Override
				public void onViewAnimationStart(int type) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onViewAnimationEnd(int type) {
					// TODO Auto-generated method stub
					if (m_tilestatubarhigh < 0) {
						m_tilestatubarhigh = GetHighofTitleStatuBar();
					}
					
					if (!m_oneflag) {
						m_oneflag = true;
					}
				}
			});
		}
		
		//InitObservable();
		m_first = true;	
		
		return view;
	}
	
	private void updateLayout(AbsListView view, int firstVisibleItem) {
		
		//Log.i(TAG, "zll --- onScroll 1 firstVisibleItem:"+firstVisibleItem);
		if (firstVisibleItem == 0) {
			//date_GroupLayout.setVisibility(View.GONE);
			mheader_background.setVisibility(View.GONE);
			lastFirstVisibleItem = firstVisibleItem;
			
			mheader_actionbarBg.setVisibility(View.GONE);
			return;
		}
		
		mheader_actionbarBg.setVisibility(View.VISIBLE);
		if (mAdapter != null) {
			if (mAdapter.isSectionHeader(firstVisibleItem)) {
				//date_GroupLayout.setVisibility(View.GONE);
				mheader_background.setVisibility(View.GONE);
				lastFirstVisibleItem = firstVisibleItem;
				return;
			}
			
			int nextSecPosition = mAdapter.getDateLayoutPosition(firstVisibleItem);
			//Log.i(TAG, "zll --- onScroll 2 nextSecPosition:"+nextSecPosition+",firstVisibleItem:"+firstVisibleItem+",lastFirstVisibleItem:"+lastFirstVisibleItem);
			if (firstVisibleItem != lastFirstVisibleItem) 
			//if (mAdapter.isDateLayout(firstVisibleItem))
			{
				clearTopDateLayoutText();
				MarginLayoutParams params = (MarginLayoutParams)mheader_background.getLayoutParams();
				params.topMargin = 0;
				//date_GroupLayout.setLayoutParams(params);
				//date_GroupLayout.setVisibility(View.VISIBLE);
				mheader_background.setLayoutParams(params);
				mheader_background.setVisibility(View.VISIBLE);
				((View)date_GroupLayout).setAlpha(1f);
				showTopDateLayout(firstVisibleItem);
			}
			
			if (nextSecPosition == firstVisibleItem + 1) 
			{
				View childView = view.getChildAt(0);
				if (childView != null) {
					int titleHeight = mheader_background.getHeight();
					int bottom = childView.getBottom();
					//Log.i(TAG, "zll --- onScroll 3 bottom:"+bottom+",getHeight:"+childView.getHeight()+",titleHeight:"+titleHeight);
					MarginLayoutParams params = (MarginLayoutParams) mheader_background.getLayoutParams();
					if (bottom < titleHeight) {
						float pushedDistance = bottom - titleHeight;
						params.topMargin = (int) pushedDistance;
						//date_GroupLayout.setLayoutParams(params);
						mheader_background.setLayoutParams(params);
						((View)date_GroupLayout).setAlpha(1f*bottom/titleHeight);
						//myValueAnimator(date_GroupLayout, false);
					} else {
						if (params.topMargin != 0) {
							params.topMargin = 0;
							mheader_background.setLayoutParams(params);
							((View)date_GroupLayout).setAlpha(1f);
						}
					}
				}
			}
			
			lastFirstVisibleItem = firstVisibleItem;
		}
		
		return;
	}
	
	private void showTopDateLayout(int position) {

		if (mAdapter == null) {
			return;
		}
		
		synchronized (this) {
			
			String tmpstr = mAdapter.getHeaderTitle(position);
			if (tmpstr.length() == 0 || tmpstr.equals("")) {
				return;
			}
			
			if (m_RowColumns == 6) {
				wenzi_TextView.setText(getActivity().getString(R.string.date_month));
			} else {
				wenzi_TextView.setText(getActivity().getString(R.string.date_day));
			}
			
			date_TextView.setText(tmpstr.substring(tmpstr.length()-2, tmpstr.length()));
			year_TextView.setText(tmpstr.substring(0, tmpstr.length()-3));
			
			/*
			try {
				year_TextView.setTypeface(m_auroraNumberTf);
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
			
		}
		
		return;
	}
	
	private void clearTopDateLayoutText() {
		date_TextView.setText("");
		year_TextView.setText("");
		
		return;
	}

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ACTIONBAR_DELETE:
				{
					OnDeleteViews();
					
					Log.i(TAG, "zll --- 3 OnDeleteViews tmplist.size:"+tmplist.size());
					mAdapter.notifyDataSetChanged();
					
					if ((tmplist != null) && (tmplist.size() == 0)) {
						/*if (mScrollView !=null) {
							mScrollView.setVisibility(View.GONE);
						}*/
						
						//empty_layout
						TextView view2 = (TextView)getActivity().findViewById(R.id.empty_gridview);
						view2.setVisibility(View.VISIBLE);

						if (mActionBar != null) {
							mActionBar.setTitle(R.string.appwidget_title);
						}
						return;
					}

					String num = String.format(getActivity().getString(R.string.action_title), m_totalnum);
			    	mActionBar.setTitle(num);
			    	
			    	m_allselect = false;
				}
				break;
				
			case MSG_UPDATE_IMGDATABASE:
				{
					if (m_totalnum != getThumbsNum()) 
					{
						//doHashMapThread();
						OnInitResumeGridview();
					}
				}
				
				break;
				
			case MSG_UPDATE_VIDEODATABASE:
				{
					if (m_totalnum != getThumbsNum()) 
					{
						OnInitResumeGridview();
					}
				}
				
				break;
				
			case MSG_UPDATE_THUMBS:
				{
					int num = msg.arg1;
					//Log.i(TAG, "zll ---- MSG_UPDATE_THUMBS num:"+num+",m_totalnum:"+m_totalnum);
					if (num > 0 && num != m_totalnum) {
						OnInitResumeGridview();
					}
				}
				break;
				
			case MSG_UPDATE_SLOTVIEW_INDEX:
				{
					//Log.i(TAG, "zll ---- MSG_UPDATE_SLOTVIEW_INDEX 1");
					int firstVisibleItem = msg.arg1;
					int visibleItemCount = msg.arg2;
					AbsListView view = (AbsListView)msg.obj;
					
					if (mDataLoader != null && mDataLoader.size() > 0) 
					{
						//Log.i(TAG, "zll ---- MSG_UPDATE_SLOTVIEW_INDEX 2");
						int startCol = mAdapter.getListViewPosition(firstVisibleItem, 0);
						int start = Math.max(0, (startCol-10));
						int end = Math.min(m_totalnum, m_RowColumns * (firstVisibleItem+visibleItemCount));
						mDataLoader.setActiveWindow(start, end);
					}
					
					if (view != null) {
						updateLayout(view, firstVisibleItem);
					}
				}
				break;
				
			case MSG_UPDATE_DATELAYOUT:
				{
					int firstVisibleItem = msg.arg1;
					AbsListView view = (AbsListView)msg.obj;
					updateLayout(view, firstVisibleItem);
				}
				break;
				
			case MSG_ADD_NUMCOLUMS:
				break;
				
			case MSG_REMOVE_NUMCOLUMS:
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	public void OnInitResumeGridview()
	{
		//Log.i("zll", "zll ---- OnInitResumeGridview 1");
		InitClearList();
		init_utildata();
		
		if(mAdapter != null)
		{
			mAdapter.notifyDataSetChanged();
		}

		InitVisibleView();

		if (mActionBar != null) {
			String num = String.format(getActivity().getString(R.string.action_title), m_totalnum);
			//Log.i("zll", "zll ---- OnInitResumeGridview 2 m_totalnum:"+m_totalnum);
	    	if (m_totalnum > 0) {
	    		mActionBar.setTitle(num);
			}
	    	else {
	    		mActionBar.setTitle(R.string.appwidget_title);
	    		
	    		mActionBar.setShowBottomBarMenu(false);
    			mActionBar.showActionBarDashBoard();
    			bMultiSelect = false;
    			bDelteMenuShow = false;
			}
		}
		
		return;
	}
	
	private boolean CheckBeSelect() {
		boolean flag = false;
		
		Iterator iter = mSelectMap.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry entry = (Map.Entry) iter.next();
			int key = Integer.parseInt(String.valueOf(entry.getKey()));
			boolean value = Boolean.parseBoolean(entry.getValue().toString());
			if (value) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}

	//Iuni <lory><2013-12-17> add begin
	private void UpdateDeleteMenuStatus(boolean flag) {
		
		if (mActionBar == null) {
			return;
		}
		
		AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        auroraMenu.setBottomMenuItemEnable(1, flag);

		return;
	}
	
	private void doHashMapThread() {
		synchronized (this) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (!m_bStopThread) {
						//OnInitResumeGridview();
					}
				}
			}).start();
		}
		
		return;
	}
	
	private void InitObservable() {
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		mObservable = new GridViewObservable();
		mObservable.addObserver(new Observer() {
			
			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				Log.i(TAG, "zll ---- bbbbbbbbbbbbb ------update");
			}
		});
		
		return;
	}

	public void RecyclingImageViewScale(){
		if ((mAdapter == null) || (mListView == null)) {
			return;
		}
		
		final int childCount = mListView.getChildCount();
		//Log.i(TAG, "zll --- RecyclingImageViewScale childCount:"+childCount+",m_totalnum:"+m_totalnum);
		
		for (int i = 0; i < childCount; i++) {
			//View child = mGridView.getChildAt(i);
			View mView = mListView.getChildAt(i);
			if ((mView != null) && (mView instanceof RecyclingImageView)) {
				//((RecyclingImageView)mView).ScaleImage();
				Log.i(TAG, "zll ---- zzzz getLeft:"+mView.getLeft()+",getTop:"+mView.getTop()+",getRight:"+mView.getRight()+",getBottom:"+mView.getBottom());
			}
		}
		
		//ChangeNumColums();
		return;
	}
	
	private void StartViewAnimation(View view) {
		if ((mAnimationController != null) && (view != null)) {
			mAnimationController.animation_scaleIn(view, 1000, 0);
		}

		return;
	}

	@Override
	public void onDestroy() {
		//Log.i(TAG, "zll ---- qqqq onDestroy 1");
		super.onDestroy();
		if (mDataLoader != null) {
			mDataLoader.setLoadingListener(null);
        }
		
		mIsActive = false;
		
		InitClearList();
		
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		if (mHandler != null) {
			mHandler.removeMessages(0);
		}
		
		mImageResizer.clearMemoryCache();
		mImageResizer.closeCache();
	}

	@Override
	public void onPause() {
		//Log.i(TAG, "zll ---- qqqq onPause 1");
		super.onPause();
		//Log.i(TAG, "zll ---- qqqq onPause 1");
		
		mHandler.removeMessages(MSG_UPDATE_SLOTVIEW_INDEX);
		if (m_bHide) {
			return;
		}
		
		if (mAuroraAlertDialog != null) {
			mAuroraAlertDialog.stopTaskAndDismissDialog();
		}
		
		//Log.i(TAG, "zll ---- unregisterReceiver");
		/*if (m_SdBroadCastRec != null && m_bRegisterReceiver) {
			getActivity().unregisterReceiver(m_SdBroadCastRec);
			m_bRegisterReceiver = false;
		}*/
		
		mImageResizer.setPauseWork(false);//Iuni <lory><2014-01-15> add begin
		mImageResizer.setExitTasksEarly(true);
		mImageResizer.flushCache();
		
		if (m_bBackGroud) {
			onSaveData();
		}
	}
	
	private void onSaveData() {
		if ((mSelectMap == null) || (mSelectMap.size() <= 0)) {
			return;
		}
		
		//Log.i(TAG, "zll --- onSaveData");
		
		mSlectedId = null;
		mSlectedId = new ArrayList<Long>();
		
		int count = 0;
		Iterator iter = mSelectMap.entrySet().iterator();
		while (iter.hasNext()){
			
			Map.Entry entry = (Map.Entry) iter.next();
			long key = Long.valueOf(String.valueOf(entry.getKey()));
			boolean value = Boolean.parseBoolean(entry.getValue().toString());
			if (!value) {
				continue;
			}
			
			//Log.i(TAG, "zll --- delte 1 key:"+key+",value:"+value+",pos:"+pos);
			mSlectedId.add(key);
			count++;
		}
		
		return;
	}
	
	private void onResumeData() {
		//Log.i(TAG, "zll --- onResumeData");
		if (mSlectedId != null) {
			for (int i = 0; i < mSlectedId.size(); i++) {
				mSelectMap.put(mSlectedId.get(i), true);
			}
			
			if (mAdapter != null) {
				if (bDelteMenuShow) {
					mAdapter.OnClickSetCheckImgVisible(true);
				}
				mAdapter.setSelectMap(mSelectMap);
    			mAdapter.notifyDataSetChanged();
			}
		}
		return;
	}
	
	public void onKeyBackPressed() {
		//Log.i(TAG, "zll --- onKeyBackPressed");
		m_bHide = false;
		//mIsActive = false;
		
		if (mActionBar != null) {
			Log.i(TAG, "zll --- onKeyBackPressed 2");
			mSelectMap.clear();
			mAdapter.clearSelectMap();
			mAdapter.OnClickSetCheckImgVisible(false);
			mAdapter.notifyDataSetChanged();
			
			//String num = m_totalnum+"个图片";
			String num = String.format(getActivity().getString(R.string.action_title), m_totalnum);
	    	mActionBar.setTitle(num);
	    	
			mActionBar.setShowBottomBarMenu(false);
			mActionBar.showActionBarDashBoard();
			
			//bDelteMenuShow = false;
			m_allselect = false;
			bMultiSelect = false;
		}
		
		bDelteMenuShow = false;
		return;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		
		String tmp = m_totalnum+"个图片";
		MenuItem picstr = menu.add(0, MENU_TITEL_ID, 0, tmp);
		picstr.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	//Iuni <lory><2013-12-17> add begin
	private void InitVisibleView() {
		if ((tmplist != null) && 
			(tmplist.size() == 0)) {
			TextView view2 = (TextView)getActivity().findViewById(R.id.empty_gridview);
			if (m_bOnlyVideo) {
				view2.setText(getActivity().getString(R.string.appwidget_empty_video));
			} else {
				view2.setText(getActivity().getString(R.string.appwidget_empty_text));
			}
			if (view2.getVisibility() != View.VISIBLE) {
				view2.setVisibility(View.VISIBLE);
			}
		}
		else {
			TextView view2 = (TextView)getActivity().findViewById(R.id.empty_gridview);
			if (m_bOnlyVideo) {
				view2.setText(getActivity().getString(R.string.appwidget_empty_video));
			} else {
				view2.setText(getActivity().getString(R.string.appwidget_empty_text));
			}
			view2.setVisibility(View.GONE);
		}

		return;
	}
	
	private final BroadcastReceiver m_SdBroadCastRec = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "zll --- onReceive 0 action:"+action);
			if(action.equals(Intent.ACTION_MEDIA_MOUNTED))
			{
				//Log.i(TAG, "zll --- onReceive 1");
				m_bMounted = true;
				
			} else if(action.equals(Intent.ACTION_MEDIA_EJECT)
					/*||action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
					||action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
					||action.equals(Intent.ACTION_MEDIA_REMOVED)*/)
			{
				//Log.i(TAG, "zll --- onReceive 2");
				m_bMounted = false;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				OnInitResumeGridview();
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				//Log.i(TAG, "zll --- onReceive 3");
				OnInitResumeGridview();
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				
				mProgressDialog = createProgressDialog(getActivity(), R.string.update_pic, 100);
				mProgressDialog.show();
			}
			
		}
	};
	
	private AuroraProgressDialog createProgressDialog(Context context, int titleId, int progressMax) {
		AuroraProgressDialog dialog = new AuroraProgressDialog(context);
        dialog.setTitle(titleId);
        dialog.setMax(progressMax);
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        if (progressMax > 1) {
            //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        
        return dialog;
    }
	
	private void InitSdIntentFilter() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		mFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		mFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		mFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		mFilter.addDataScheme("file");
		//Log.i(TAG, "zll ---- InitSdIntentFilter");
		getActivity().registerReceiver(m_SdBroadCastRec, mFilter);
		m_bRegisterReceiver = true;
		
		return;
	}
	
	private int getDataBaseVideoNum() {
		int mCachedCount = 0;
		
		if (m_Resolver == null) {
			m_Resolver = getActivity().getContentResolver();
		}
		
		Cursor cursor = m_Resolver.query(mImgBaseUri, COUNT_PROJECTION, mImgWhereClause, m_ImgSelectionArgs, null);
        if (cursor == null) {
            return 0;
        }
        
        try {
            Utils.assertTrue(cursor.moveToNext());
            mCachedCount = cursor.getInt(0);
        } finally {
            cursor.close();
        }
        
		return mCachedCount;
	}
	
	private int getDataBaseImgNum() {
		int mCachedCount = 0;
		
		if (m_Resolver == null) {
			m_Resolver = getActivity().getContentResolver();
		}
		
		Cursor cursor = m_Resolver.query(mVideoBaseUri, COUNT_PROJECTION, mVideoWhereClause, m_VideoSelectionArgs, null);
        if (cursor == null) {
            return 0;
        }
        
        try {
            Utils.assertTrue(cursor.moveToNext());
            mCachedCount = cursor.getInt(0);
        } finally {
            cursor.close();
        }
        
		return mCachedCount;
	}
	
	private int getThumbsNum() {
		int total_num =0;
		
		total_num += getDataBaseVideoNum();
		total_num += getDataBaseImgNum();
		
		return total_num;
	}
	
	private class onResumeDataLoader implements Runnable{

		@Override
		public void run() {
			if (mDataLoader != null && mDataLoader.size() > 0) 
			{
				int start = 0;
				int end = Math.min(m_totalnum, m_RowColumns * FIRST_NUM);
				mDataLoader.setActiveWindow(start, end);
			} else {
				mHandler.postDelayed(resumeDataLoader, 100);
			}
			
			return;
		}
		
	}
	
	@Override
	public void onResume() {
		//Log.i(TAG, "zll ---- qqqq onResume 1");
		super.onResume();

		if (m_bHide) {
			return;
		}
		
		if (MySelfBuildConfig.USEFRAGMENTACTIVITY_FLAG) {
			if (mIsActive) {
				//Log.i(TAG, "zll --- onResume 3.1 mMediaSet count");
				OnInitResumeGridview();
			}
			//Log.i(TAG, "zll ---- qqqq onResume 2 mIsActive:"+mIsActive+",m_bBackGroud:"+m_bBackGroud);
			if (m_bBackGroud) {
				onResumeData();
			}
		} else {
			if (mMediaSet != null && mIsActive) {
				Log.i(TAG, "zll --- onResume 3.2 mMediaSet count:"+mMediaSet.getMediaItemCount());
				OnInitResumeGridview();
			} 
		}
	
		mIsActive = true;
		if (!MySelfBuildConfig.USEFRAGMENTACTIVITY_FLAG) {
			mHandler.postDelayed(resumeDataLoader, 50);
		}
		
		//InitSdIntentFilter();
		/*if (m_totalnum != getThumbsNum()) {
			OnInitResumeGridview();
		}*/
		InitVisibleView();
		
		m_allselect = false;
		m_deleteselect = false;
		
		mImageResizer.setExitTasksEarly(false);
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
		
		if (m_FoucsIndex != 0) {//from photopage should update now index
			setFoucusIndexFromPhotoPage(m_FoucsIndex);
			m_FoucsIndex = 0;
		}
	}
	
	public void ShowViewAnimation(final View view, final int id, final float p1, final float p2) {
		if (mAnimationController == null) {
			return;
		}
		
		mAnimationController.animation_fadeOut(view, 1000, 0);
		return;
	}
	
	private void ChangeNumColums() {
		
		synchronized (this) {
			if (m_tmpRows == 2) {
				m_tmpRows = 3;
			}
			else if (m_tmpRows == 3 || m_tmpRows == 0) {
				m_tmpRows = 6;
			}
			else if (m_tmpRows == 6) {
				m_tmpRows = 2;
			}
			//Log.i(TAG, "zll -- GridView_ItemNumChange m_menu:"+m_menu);
			
			m_RowColumns = m_tmpRows;
			
			//lory 2012.12.09 m_ImageThumbSize = (m_Displaywidth-m_leftlayoutwidth-(m_RowColumns-1)*m_horizontalSpacing)/m_RowColumns;
			m_ImageThumbSize = (m_Displaywidth-m_leftlayoutwidth-(m_RowColumns+1)*m_horizontalSpacing - m_rightSpacing)/m_RowColumns;
			mImageResizer.setImageSize(m_ImageThumbSize);//lory 2012.12.10
			//mListView.setNumColumns(m_RowColumns);
			//mGridView.OnLayoutAllGridView();
			mListView.invalidate();
		}
		
		if (mAdapter == null) {
			mListView.setAdapter(mAdapter);
		}
		
		return;
	}

	public class GridViewObservable extends Observable
	{
		public void OnActionBarChanged(boolean visible) {
			if (m_bShowCheckbox != visible) {
				setChanged();
				notifyObservers(visible);
			}
		}
	}
	
	public void delete(Long id, int type) {  
		int num = 0;
		if (type == IMAGE_TYPE) {
			num = getActivity().getContentResolver().delete(Images.Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{String.valueOf(id)});
		}
		else {
			num = getActivity().getContentResolver().delete(Video.Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{String.valueOf(id)});
		}
		
		Log.i(TAG, "zll ---- delete id:"+id+",num:"+num+",type:"+type);
		
		return;
	}
	
	private void OnDeleteViews() {
		//Log.i(TAG, "zll --- OnDeleteViews 1");
		if ((mSelectMap == null) || (mSelectMap.size() <= 0)) {
			return;
		}

		//Log.i(TAG, "zll --- OnDeleteViews 2");
		
		Iterator iter = mSelectMap.entrySet().iterator();
		while (iter.hasNext()){
			Map.Entry entry = (Map.Entry) iter.next();
			//Log.i(TAG, "zll --- delte key 22222");
			long key = Long.valueOf(String.valueOf(entry.getKey()));
			boolean value = Boolean.parseBoolean(entry.getValue().toString());
			if (!value) {
				continue;
			}
			
			mSelectMap.put(key, false);
			//Log.i(TAG, "zll --- delte 1 key:"+key+",value:"+value+",pos:"+pos);
			delete(key, getImgTypeFromID(key));
		}
		
		InitClearList();
		//Log.i(TAG, "zll --- 2 OnDeleteViews tmplist.size:"+tmplist.size()+",mList.size:"+mList.size()+",mIdMap.size:"+mIdMap.size()+",mIdMap.size:"+mIdMap.size());
		init_utildata();
		
		return;
	}
	
	private int getImgTypeFromID(Long id) {

		for (int i = 0; i < tmplist.size(); i++) {
			GalleryItem item = tmplist.get(i);
			if (item.getId() == id) {
				return item.getType();
			}
		}
		
		return -1;
	}
	
	private void InitClearList() {
		
		if (tmplist != null) {
			tmplist.clear();
		}
		
		if (mSelectMap != null) {
			mSelectMap.clear();
			mAdapter.clearSelectMap();
		}
		
		if (mAdapter != null) {
			mAdapter.clearContainerList();
		}
		
		return;
	}

	@Override
	public boolean onGridItemLongClicked(Long id, int position, View v) {
		Log.i(TAG, "zll --- onGridItemLongClicked position:"+position+",id:"+id);
		if (mIsPinch) {
			return false;
		}
		
		if (mGetContent || mIsGnCrop) {
			return false;
		}
		
		if (bMultiSelect) {
			return false;
		}
		
		if (mActionBar != null) {
			bMultiSelect = true;
			mVibrator.vibrate(100);

			mActionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
			mActionBar.setShowBottomBarMenu(true);
			mActionBar.showActionBarDashBoard();
			
			mSelectMap.put(id, true);
			mAdapter.setSelectMap(mSelectMap);
			mAdapter.OnClickSetCheckImgVisible(true);
			mAdapter.notifyDataSetChanged();
			
			bAll = false;
			UpdateActionBarRightButton(bAll);
			
			UpdateDeleteMenuStatus(true);
			bDelteMenuShow = true;
		}
		
		return true;
	}

	private void GetandSaveCurrentImage() {
		long time1 = System.currentTimeMillis();
				
		WindowManager windowManager = getActivity().getWindowManager();
		Display display = windowManager.getDefaultDisplay();   
		int w = display.getWidth();   
		int h = display.getHeight();    
		Bitmap Bmp = Bitmap.createBitmap( w, h, Config.RGB_565);        
		Log.i(TAG, "zll --- tttttt InitData need time 1.2 is :"+(System.currentTimeMillis() - time1));
		long time2 = System.currentTimeMillis();
		
		View decorview = getActivity().getWindow().getDecorView();     
		decorview.setDrawingCacheEnabled(true);     
		Bmp = decorview.getDrawingCache();
		
		Log.i(TAG, "zll --- tttttt InitData need time 1.2 is :"+(System.currentTimeMillis() - time2));
	}
	
	private View gView = null;
	private Rect gRect;
	@Override
	public void onGridItemClicked(Long id, int position, View v) {
		//Log.i(TAG, "zll --- onGridItemClicked position:"+position+",id:"+id);
		//GetandSaveCurrentImage();
		if (mIsPinch) {
			return;
		}

		gView = v;
		if (!bMultiSelect) {
			
			if (MySelfBuildConfig.USEFRAGMENTACTIVITY_FLAG) {
				
				switchOtherActivity(id, position, v);
				return;
			} 
			
			final int[] location = new int[2];
			v.getLocationOnScreen(location); 
			
			//Log.i(TAG, "zll --- onGridItemClicked position x:"+location[0]+",y:"+location[1]);
			
			Rect tRect = new Rect();
			//v.getHitRect(tRect);
			tRect.left = location[0];
			tRect.top = location[1];
			tRect.right = location[0]+v.getWidth();
			tRect.bottom = location[1]+v.getHeight();
			
			//Log.i(TAG, "zll --- left:"+v.getWidth()+",right:"+v.getHeight());
			//Log.i(TAG, "zll ---- rrrr 1 left:"+tRect.left+",top:"+tRect.top+",right:"+tRect.right+",bottom:"+tRect.bottom);
			{
				/*ScaleAnimation animation = new ScaleAnimation(1f, 2f, 1f, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setDuration(2000);
				animation.setStartOffset(0);
				v.setVisibility(View.VISIBLE);
				v.startAnimation(animation);*/
			}
			
			/*Rect dRect = new Rect();
			v.getGlobalVisibleRect(dRect);
			Log.i(TAG, "zll --- rrrr 2 left:"+dRect.left+",top:"+dRect.top+",right:"+dRect.right+",bottom:"+dRect.bottom);*/
			gRect = tRect;
			picktoPhotoPage(position, tRect);
			
			return;
		}
		
		boolean bselect = true;
		if (mSelectMap.get(id) != null) {
			bselect = mSelectMap.get(id);
			bselect = !bselect;
		}
		
		mSelectMap.put(id, bselect);
		mAdapter.setSelectMap(mSelectMap);
		mAdapter.notifyDataSetChanged();
		UpdateDeleteMenuStatus(CheckBeSelect());
		return;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		if (event.getPointerCount() >= 2) {
			//Log.i(TAG, "zll ----- ttttttt 1");
			mIsPinch = true;
		} else {
			//Log.i(TAG, "zll ----- ttttttt 2");
			mIsPinch = false;
		}
		mScaleDetector.onTouchEvent(event);
		return false;
	}
	
	private class MyGridViewScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

		public MyGridViewScaleListener() {
			super();
		}
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			
			mIsPinch = true;
			//Log.i(TAG, "zll --- onScale xxxxx 2");
			float scale = detector.getScaleFactor();
			
			if (scale >= 1.03) {
				onScaleRemoveView();
				return true;
			}else if (scale <= 0.97) {
				onScaleAddView();
				return true;
			}
			
			return false;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			//Log.i(TAG, "zll --- onScaleBegin xxxxx 1");
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			//Log.i(TAG, "zll --- onScaleEnd xxxxx 3");
			mIsPinch = false;
		}
	}

	private void onCancelScale() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mScaleDetector.onTouchEvent(cancelEvent);
        cancelEvent.recycle();
    }
	
	private void onScaleRemoveView() {
		if (m_RowColumns == 2) {
			return;
		}else if (m_RowColumns == 3) {
			m_RowColumns = 2;
		}else if (m_RowColumns == 6) {
			m_RowColumns = 3;
		}
		
		//Log.i(TAG, "zll --- dddd 2 m_RowColumns:"+m_RowColumns);
		/*下面的也ok
		mAdapter.clearContainerList();
		mAdapter.notifyDataSetChanged();
		
		if (mAdapter != null) {
			mAdapter = null;
		}
		mAdapter = new GridApapter(getActivity(), m_RowColumns);
		tmplist.clear();
		init_utildata();*/
		
		initListView();
		onCancelScale();
		return;
	}
	
	private void onScaleAddView() {
		if (m_RowColumns == 2) {
			m_RowColumns = 3;
		}else if (m_RowColumns == 3) {
			m_RowColumns = 6;
		}else if (m_RowColumns == 6) {
			return;
		}
		
		//Log.i(TAG, "zll --- dddd 1 m_RowColumns:"+m_RowColumns);
		initListView();
		onCancelScale();
		return;
	}
	
	private void initListView() {
		
		//int nowid = mAdapter.getListViewPosition(mListView.getFirstVisiblePosition(), 0);
		int nowid = mListView.getFirstVisiblePosition();
		{
			View tView = mListView.getChildAt(nowid);
			if (tView != null) {
				int top = tView.getTop();
				//Log.i(TAG, "zll --- initListView top:"+top);
				if (top < 0) {
					nowid++;
				}
			}
		}
		
		if (mAdapter.isSectionHeader(nowid)) {
			//Log.i(TAG, "zll --- initListView 1.1 nowid:"+nowid);
			nowid++;
		}
		
		int itemid = mAdapter.getListViewPosition(nowid, 0);
		//Log.i(TAG, "zll --- initListView 2 itemid:"+itemid +",nowid:"+nowid);
		GalleryItem nowitem = mAdapter.getGalleryItem(itemid);
		/*if (nowitem == null) {
			Log.i(TAG, "zll --- initListView 2 itemid:"+itemid +",nowitem == null:");
		} else {
			Log.i(TAG, "zll --- initListView 2 itemid:"+itemid +",getUri:"+nowitem.getUri());
		}*/
		
		mAdapter.clearContainerList();
		mAdapter.notifyDataSetChanged();
		mAdapter.setNumColumns(m_RowColumns);
		mAdapter.CreateItemsIndex(tmplist);
		
		//g_ListIndex = mListView.getFirstVisiblePosition();
		mListView.setAdapter(mAdapter);
		
		int newindex = mAdapter.getListViewIndexFromItem(nowitem);
		Log.i(TAG, "zll --- initListView newindex:"+newindex);
		if (newindex == 0) {
			newindex = mListView.getFirstVisiblePosition();
		} 
		mListView.setSelectionFromTop(newindex, 0);
		
		m_ImageThumbSize = mAdapter.getGridImgSize();
		//mListView.scrollTo(mScrolledX, mScrolledY);
		return;
	}
	
	public void setFoucsIndex(int index) {
		//Log.i(TAG, "zll --- setFoucsIndex index:"+index);
		m_FoucsIndex = index;
		
		return;
	}
	
	private void setFoucusIndexFromPhotoPage(int index) {
		//Log.i(TAG, "zll --- setFoucusIndexFromPhotoPage index:"+index);
		GalleryItem nowitem = mAdapter.getGalleryItem(index);
		
		int newindex = mAdapter.getListViewIndexFromItem(nowitem);
		//Log.i(TAG, "zll --- setFoucusIndexFromPhotoPage newindex:"+newindex+",index:"+index);
		if (newindex > 1 && newindex > mScreenNum) {
			newindex -= 1;
		} else {
			newindex = 0;
		}
		mListView.setSelectionFromTop(newindex, 0);
		//String tUri = nowitem.getUri();
		//Log.i(TAG, "zll --- setFoucusIndexFromPhotoPage tUri:"+tUri);
		/*if (mAnimationController != null) {
			mAnimationController.animation_translate(gView, 500, 0);
		}*/
		{/*
			//TranslateAnimation mAnimation = new TranslateAnimation(0, 50, 0, 50);
			if (m_Displaywidth == 0) {
				DisplayMetrics dm = new DisplayMetrics();
		    	getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				m_Displaywidth = dm.widthPixels;
				m_Displayheight = dm.heightPixels;
			}
			
			TranslateAnimation mAnimation;
			int offset =(m_ImageThumbSize*3)/2;
			Log.i(TAG, "zll --- gRect.left:"+gRect.left);
			
			int tw = m_Displaywidth/2;
			int th = m_Displayheight/2;
			if (gRect.left < tw && gRect.top < th) {
				mAnimation = new TranslateAnimation(gRect.left+m_ImageThumbSize, 0, gRect.top+offset, 0);
			} else if (gRect.left > tw && gRect.top < th) {
				mAnimation = new TranslateAnimation(gRect.left-m_ImageThumbSize, 0, gRect.top+offset, 0);
				//mAnimation.setRepeatMode(Animation.REVERSE);
			} else if (gRect.left < tw && gRect.top > th) {
				mAnimation = new TranslateAnimation(gRect.left+m_ImageThumbSize, 0, gRect.top-offset, 0);
			}else if (gRect.left > tw && gRect.top > th) {
				mAnimation = new TranslateAnimation(gRect.left-m_ImageThumbSize, 0, gRect.top-offset, 0);
				//mAnimation.setRepeatMode(Animation.REVERSE);
			} else {
				mAnimation = new TranslateAnimation(gRect.left+m_ImageThumbSize, 0, gRect.top, 0);
			}
			
			mAnimation.setInterpolator(new AnticipateOvershootInterpolator());
			mAnimation.setDuration(1000);
			gView.startAnimation(mAnimation);
		*/}
		
		return;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//Log.i(TAG, "zll ---- onConfigurationChanged xxxxxxx orientation:"+newConfig.orientation);
		return;
	}
	
	public void isChildVisible(View child){
		for (int i = 0; i < mListView.getLastVisiblePosition(); i++) {
			View mView = mListView.getChildAt(i);
			
			if (mView != null) {
				Rect scrollBounds = new Rect();
				mView.getHitRect(scrollBounds);
				Log.i(TAG, "zll ---- scrollBounds i:"+i+",left:"+scrollBounds.left+",right:"+scrollBounds.right+",top:"+scrollBounds.top+",bottom:"+scrollBounds.bottom);
				
				Log.i(TAG, "zll ---- mView.getTop():"+mView.getTop());
			}
		}
	}
	
	/*public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
		velocityX = velocityX > 4000 ? 4000 : velocityX;
		velocityX = velocityX < -4000 ? -4000 : velocityX;
		// 根据手势的速度来计算缩放比，如果velocityX>0，放大图像，否则缩小图像。
		currentScale += currentScale * velocityX / 4000.0f;
		// 保证currentScale不会等于0
		currentScale = currentScale > 0.01 ? currentScale: 0.01f;
		System.out.println(currentScale+"----"+width+"---"+height);
		// 重置Matrix
		matrix.reset();
		// 缩放Matrix
		matrix.setScale(currentScale, currentScale, 160, 200);
		BitmapDrawable tmp = (BitmapDrawable)image.getDrawable();
		// 如果图片还未回收，先强制回收该图片
		if (!tmp.getBitmap().isRecycled()) // ①
		{
			tmp.getBitmap().recycle();
		}
		// 根据原始位图和Matrix创建新图片
		Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0
			, width, height, matrix, true);
		// 显示新的位图
		image.setImageBitmap(bitmap2);
		return true;
	}*/
	
	public void clearCacheData() {
		//m_bClearData = true;
		return;
	}
	
	public void setDirtyContentObserver(int totalnum) {
		mHandler.obtainMessage(MSG_UPDATE_THUMBS, totalnum, 0).sendToTarget();
//		Log.i("SQF_LOG", "setDirtyContentObserver");
		return;
	}
	
	public void setBackGroud(boolean flag) {
		m_bBackGroud = flag;
	}
}
