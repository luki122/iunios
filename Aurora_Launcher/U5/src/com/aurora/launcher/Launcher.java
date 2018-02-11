/*
 * Copyright (C) 2008 The Android Open Source Project
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
 
package com.aurora.launcher;

import android.R.anim;
import android.R.color;
import android.R.string;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.view.animation.TranslateAnimation;
import android.graphics.drawable.AnimationDrawable;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.SearchManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.GnSurface;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Advanceable;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

import com.aurora.calender.CalenderAdapter;
import com.aurora.calender.CalenderPlugin;
import com.aurora.calender.FlipViewController;
import com.aurora.calender.NumberImageView;
//import com.android.common.Search;
import com.aurora.launcher.DropTarget.DragObject;
import com.aurora.launcher.InstallShortcutReceiver.IAddAuroraShortcut;
import com.aurora.launcher.Launcher.EditMode;
import com.aurora.launcher.Launcher.TransModeEnum;
import com.aurora.launcher.Workspace.ItemPos;
import com.aurora.launcher.PagedView.PageCountChangeListener;
import com.aurora.launcher.Workspace.State;
import com.aurora.stickylistheaders.StickyListHeadersListView;
import com.aurora.util.StringUtils;
import com.aurora.util.Utils;
import com.aurora.view.AlphbetSideBar;
import com.aurora.view.ClassifyLoadingAnimation;
import com.aurora.view.DefMarginImageView;
import com.aurora.view.QuickIndexFrameLayout;
import com.aurora.view.QuickIndexLayout;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.Duration;

import aurora.app.AuroraAlertDialog;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.KeyguardManager;
import android.util.TypedValue;

import com.aurora.plugin.CalendarIcon;
import com.aurora.plugin.DynIconDriver;
import com.aurora.plugin.DynIconPlg;
import com.aurora.util.ClassifyApp;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

/**
 * Default launcher application.
 */

//change Activity to extends ActivityGroup for voice
public final class Launcher extends ActivityGroup implements View.OnClickListener,
		OnLongClickListener, LauncherModel.Callbacks, View.OnTouchListener, IFullScreen, ResponsePintchEvent,
		OnEditorActionListener {
	static final String TAG = "Launcher";
	static final boolean LOGD = false;

	static final boolean PROFILE_STARTUP = false;
	static final boolean DEBUG_WIDGETS = false;
	static final boolean DEBUG_STRICT_MODE = false;

	private static final int MENU_GROUP_WALLPAPER = 1;
	private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
	private static final int MENU_MANAGE_APPS = MENU_WALLPAPER_SETTINGS + 1;
	private static final int MENU_SYSTEM_SETTINGS = MENU_MANAGE_APPS + 1;
	private static final int MENU_HELP = MENU_SYSTEM_SETTINGS + 1;
	private static final int MENU_WIDGET = MENU_HELP + 1;
	private static final int REQUEST_CREATE_SHORTCUT = 1;
	private static final int REQUEST_CREATE_APPWIDGET = 5;
	private static final int REQUEST_PICK_APPLICATION = 6;
	private static final int REQUEST_PICK_SHORTCUT = 7;
	private static final int REQUEST_PICK_APPWIDGET = 9;
	private static final int REQUEST_PICK_WALLPAPER = 10;

	private static final int REQUEST_BIND_APPWIDGET = 11;

	private static final int MAX_PAGE_FOR_SMART_CLASSIFY = 2;
	
	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

	static final int SCREEN_COUNT = 5;
	static final int DEFAULT_SCREEN = 2;

	private static final String PREFERENCES = "launcher.preferences";
	// To turn on these properties, type
	// adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
	static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
	static final String DUMP_STATE_PROPERTY = "launcher_dump_state";

	// The Intent extra that defines whether to ignore the launch animation
	static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION = "com.aurora.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

	// Type: int
	private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
	// Type: int
	private static final String RUNTIME_STATE = "launcher.state";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
	// Type: boolean
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
	// Type: long
	private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
	// Type: int
	private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
	// Type: parcelable
	private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";

	private static final String TOOLBAR_ICON_METADATA_NAME = "com.aurora.launcher.toolbar_icon";
	private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME = "com.aurora.launcher.toolbar_search_icon";
	private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME = "com.aurora.launcher.toolbar_voice_search_icon";

	private static final String ADD_NAVIGATION_TRANSLUCENT_FLAG = "com.aurora.voiceassistant.ACTION_ADD_NAVIGATION_TRANSLUCENT_FLAG";
	
	private String ENTER_VOICEMODE_BY_VOICEPRINT = "com.aurora.voiceassistant.ACTION_ENTER_MODE_BY_VOICEPRINT";
	
	private static final String VOICEPRINT_INTENT_EXTRA_KEY_NAME = "status";
	
	private String ACTION_NAVIGATION_HINT_BACK = "com.android.systemui.ACTION_NAVIGATION_HINT_BACK";
	
	private String ACTION_VOICE_CLEAR_SCREEN = "com.iuni.voiceassistant.ACTION_VOICE_CLEAR_SCREEN";
	
	private  final int LAUNCHER_TASK_FLAG_INVALID = -1;
	public  final int LAUNCHER_TASK_FLAG_EXIST = 1;
	public  final int LAUNCHER_TASK_FLAG_NOT_EXIST = 2;
	
	
	private Boolean play=false;
    FlipViewController flipView;
    CalenderPlugin layout;
	LayoutInflater inflater;
	private boolean isInClassifyModel=false;
	
	private static boolean isHomeKeyPress = false;
	
	//indicates if the workspace is binding the views
	//if it is binding, we should not look up contents in the workspace
	//NOTE: ONLY ACCESS IT in set-get method
	public boolean mIsWorkspaceBinding = false;
	
	/** The different states that Launcher can be in. */
	// AUROR-START:for workspace:xiejun
	private enum State {
		NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED, SCALE_WORKSPACE
	};

	/*
	 * private enum State { NONE, WORKSPACE, APPS_CUSTOMIZE,
	 * APPS_CUSTOMIZE_SPRING_LOADED, FOLDER_IMPORT_L, APPWIDGET_ADD_L };
	 */

	public enum EditMode {
		NONE,DRAG, FOLDER_IMPORT, APPWIDGET_ADD, QUICK_INDEX,CLASSIFICATE_ICONS
	}

	private EditMode mEditMode = EditMode.NONE;
	private EditMode mOldEditMode=EditMode.NONE;
	private TextView mFolderImportHint;
	private Button mFolderImportButton;
	private View mFolderImportHintContainer;
	private View mFolderImportButtonContainer;
	// AUROR-START:for workspace:xiejun

	private State mState = State.WORKSPACE;
	private AnimatorSet mStateAnimation;
	private AnimatorSet mDividerAnimator;

	static final int APPWIDGET_HOST_ID = 1024;
	private static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
	private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
	private static final int SHOW_CLING_DURATION = 550;
	private static final int DISMISS_CLING_DURATION = 250;

	private static final Object sLock = new Object();
	private static int sScreen = DEFAULT_SCREEN;

	// How long to wait before the new-shortcut animation automatically pans the
	// workspace
	private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 10;

	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
	private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

	private LayoutInflater mInflater;
	
	private boolean mFolderIconShouldSave = false;
	private final HashMap<Long,FolderIcon> mSavedFolderIconMap = new HashMap<Long,FolderIcon>();

	private Workspace mWorkspace;
	private View mQsbDivider;
	private View mDockDivider;
	private View mLauncherView;
	private DragLayer mDragLayer;
	private DragController mDragController;

	private AppWidgetManager mAppWidgetManager;
	private LauncherAppWidgetHost mAppWidgetHost;

	private ItemInfo mPendingAddInfo = new ItemInfo();
	private AppWidgetProviderInfo mPendingAddWidgetInfo;

	private int[] mTmpAddItemCellCoordinates = new int[2];

	private FolderInfo mFolderInfo;

	//private Hotseat mHotseat;
	public Hotseat mHotseat;
	//public StickyListHeadersListView mQuickIndexListView;//iht快速检索？？？
	public QuickIndexListAdapter mQuickIndexListAdapter; //iht快速检索***********************************************
	public AppsCustomizePagedView mWidgetViewContent; //weight显示内容
	
	
	private View mAllAppsButton;
	private LinearLayout mWidgetViewLayout;
	private LinearLayout mClassficateIonsLayout;
	//private AppsCustomizePagedView mWidgetViewContent; //weight显示内容
	private ImageView mWidgetframeleft;
	private ImageView mWidgetframeright;
	public TextView mPageIndicatotorTextView;
	private PageIndicator mPageIndicator;
	private SearchDropTargetBar mSearchDropTargetBar;
	private AppsCustomizeTabHost mAppsCustomizeTabHost;
	private AppsCustomizePagedView mAppsCustomizeContent;
	private StickyListHeadersListView mQuickIndexListView;//iht快速检索？？？
	private StickyListHeadersListView mSearchQuickIndexListView;
	private AlphbetSideBar letterSideBar;
	//private QuickIndexFrameLayout mQuickIndexOutline;
	public QuickIndexFrameLayout mQuickIndexOutline;
	
	private QuickIndexLayout mQuickIndexLayout;
	private ImageView mQuickIndexSearchImageView;
	private FrameLayout mQuickIndexSearchLayout;
	private TextView mQuickIndexTitle;
	private EditText mQuickIndexSearchEditText;
	//private QuickIndexListAdapter mQuickIndexListAdapter; //iht快速检索
	private QuickIndexListAdapter mSearchQuickIndexListAdapter;
	private List<AppCategoryData> appCategoryDataList;
	private List<AppCategoryData> appSearchCategoryDataList;
	private HashSet<ShortcutInfo> currentInfosSet = new HashSet<ShortcutInfo>();
	private HashSet<ShortcutInfo> searchInfosSet = new HashSet<ShortcutInfo>();
	private boolean isEnterFilterMode = false;
	private boolean mAutoAdvanceRunning = false;

	private Bundle mSavedState;
	// We set the state in both onCreate and then onNewIntent in some cases,
	// which causes both
	// scroll issues (because the workspace may not have been measured yet) and
	// extra work.
	// Instead, just save the state that we need to restore Launcher to, and
	// commit it in onResume.
	private State mOnResumeState = State.NONE;

	private SpannableStringBuilder mDefaultKeySsb = null;

	private boolean mWorkspaceLoading = true;

	private boolean mPaused = true;
	private boolean mRestoring;
	private boolean mWaitingForResult;
	private boolean mOnResumeNeedsLoad;

	// Keep track of whether the user has left launcher
	private static boolean sPausedFromUserAction = false;

	private Bundle mSavedInstanceState;

	private LauncherModel mModel;
	private IconCache mIconCache;
	private boolean mUserPresent = true;
	private boolean mVisible = false;
	private boolean mAttached = false;

	private static LocaleConfiguration sLocaleConfiguration = null;

	private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

	private Intent mAppMarketIntent = null;

	// Related to the auto-advancing of widgets
	private final int ADVANCE_MSG = 1;
	//ht 2014-09-30
	private final int ICON_RESHOW = -50; 
	private final int mAdvanceInterval = 20000;
	private final int mAdvanceStagger = 250;
	private long mAutoAdvanceSentTime;
	private long mAutoAdvanceTimeLeft = -1;
	private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance = new HashMap<View, AppWidgetProviderInfo>();

	// Aurora <haojj> <2013-09-12> add for saving shortcuts when batching to add them onto folder begin
	private boolean isAlignIconDialogOpen = false;
	public ArrayList<ShortcutInfo> mCheckedShortcutInfos = new ArrayList<ShortcutInfo>();
	public ArrayList<BubbleTextView> mCheckedBubbleTextViews = new ArrayList<BubbleTextView>();
	public HashSet<FolderInfo> mCheckedFolderInfos = new HashSet<FolderInfo>();
	FolderInfo mEditFolderInfo;
	FolderIcon mEditFolderIcon;
	// 判断是否打开文件夹
	private boolean mOpenFolderIcon=false;
	// 最多还能导入多少个icon到文件夹
	private int mMaxImportItemsCount = 0;
	private int mRemainImportItemsCount = 0;
	Dialog mAlignIconDialog;
	
    private boolean mIsUninstallShow = false;

    /**Button description*/
    private static final int APP_RETRIEVAL_NONE = 0;
    private static final int APP_RETRIEVAL_HOME = 1;
    private static final int APP_RETRIEVAL_BACK = 2;
    
    /**Flag indicated that voice print start Application whether  is restart or is already started it*/
    private boolean bAppRestart = false;
    
	void removeCheckedFolderInfosItems(FolderInfo info){
		mCheckedFolderInfos.remove(info);
	}
	
	int getRemainImportItemsCount(){
		return mRemainImportItemsCount;
	}
	
	public View getFolderImportButtonContainer(){
		return mFolderImportButtonContainer;
	}
	
	public void setAndEnterEditMode(FolderInfo info, FolderIcon folderIcon, int maxCount){
		mMaxImportItemsCount = maxCount;
		mRemainImportItemsCount = mMaxImportItemsCount;
		// 这个是为了怕出现foldericon消失的情况
		showFolderIcon();
		mEditFolderIcon = folderIcon;
		mEditFolderInfo = info;
		enterEditMode(EditMode.FOLDER_IMPORT,true);
		
	}
	
	public void addCheckedFolderInfo(FolderInfo info){
		if (!mCheckedFolderInfos.contains(info)) {
			mCheckedFolderInfos.add(info);
		}
	}
	// Aurora <haojj> <2013-09-12> add for saving shortcuts when batching to add them onto folder end

	// Determines how long to wait after a rotation before restoring the screen
	// orientation to
	// match the sensor state.
	private final int mRestoreScreenOrientationDelay = 500;

	// External icons saved in case of resource changes, orientation, etc.
	private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
	private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];
	private static Drawable.ConstantState[] sAppMarketIcon = new Drawable.ConstantState[2];

	private Drawable mWorkspaceBackgroundDrawable;
	private Drawable mBlackBackgroundDrawable;

	private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();

	static final ArrayList<String> sDumpLogs = new ArrayList<String>();

	// We only want to get the SharedPreferences once since it does an FS stat
	// each time we get
	// it from the context.
	private SharedPreferences mSharedPrefs;

	// Holds the page that we need to animate to, and the icon views that we
	// need to animate up
	// when we scroll to that page on resume.
	private int mNewShortcutAnimatePage = -1;
	private ArrayList<View> mNewShortcutAnimateViews = new ArrayList<View>();
	private ImageView mFolderIconImageView;
	private Bitmap mFolderIconBitmap;
	private Canvas mFolderIconCanvas;
	private Rect mRectForFolderAnimation = new Rect();

	private BubbleTextView mWaitingForResume;

	// Aurora <haojj> <2013-09-09> add for initing begin
    private ImageView mFolderBgBlurView;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mStatusBarHeight = 0;
    private int mQuickIndexShadowImageHeight = 0;
    private int mQuickIndexShadowImageRightMargin = 0;

	//AURORA-START tangjun 9.23:
    private ThumbnailHorizontalScrollView previewBar = null;
    private ThumbnailImage previewContent = null;
    private ThumbnailImageArrowhead leftarrowhead = null;
    private ThumbnailImageArrowhead rightarrowhead = null;
    private ValueAnimator bgAnim;
    //AURORA-END   tangjun 9.23:
    
    //AURORA-START::App Index::Shi guiqiang::20140111
    enum TransModeEnum {
    	NONE, VOICE, APPS_INDEX
    }
    private TransModeEnum TransMode = TransModeEnum.NONE;

    public ImageView mAppSearchBgImageView;
    public AppSearchImageView mAppSearchImageView;
    public ImageView mAppSearchBgShadowImageView;
    public ImageView mQuickIndexBgBlurView;

    public DefMarginImageView mShadowImageView;
    public ImageView mUpperShadowImageView;
    
    private Animator mAppSearchViewAnimator;
    private Animator mAppSearchEditTextAnimator;
    private Animator appIndexSearchAnimator;
    private Animator mGaussBlurAlphaAnimator;
    private boolean mAnimationIsGoing = false;
    private boolean mAppSearchAnimatorGoing = false;
    private boolean mAppRetrievalAppListAnimator = false;
    //AURORA-END::App Index::Shi guiqiang::20140111
    private CustomMenuProxy mCustomMenuProxy;
    
    //For voice test
    private FrameLayout mVoiceOutLine;
    private VoiceLayout mVoiceLayout;
    private View mVoiceContentView = null;
    
    private VoiceButtonImageView mVoiceButton;
    
    public Intent mMasterToSlaveIntent = null;
    public IntentFilter mSlaveToMasterFilter;
    
    private String inputIsShowing = "android.intent.action.ACTION_INPUT_METHOD_SHOW";
	private String inputIsHiden = "android.intent.action.ACTION_INPUT_METHOD_HIDE";
	private boolean inputState = false;
    //For voice test
    
    private Typeface mTypeface;
    
    private View mResultEmpty;
    
    private Bitmap[] bgArray; 
    
    public AsyncTask HandleQuickIndexBackGroundTask;
    
    private Bitmap[] blurImage  = new Bitmap[3];
    
    public GlsurfaceMask mLayerMask; 
    
    private int  AppStatus;
    
    public static final String EXPAND_STATUSBAR_ENABLE = "com.aurora.launcher.enable_expand_statusbar";
    public static final String EXPAND_STATUSBAR_DISABLE = "com.aurora.launcher.disable_expand_statusbar";
    
    //private MyPhoneStateListener mMyPhoneStateListener=new MyPhoneStateListener();
    
    private Button btnClassifyOk ,btnCalssifyCancel;
    
    /**flag in order to let finish binding execute completed and exit edit mode*/
    private boolean bClassifyCompleted = false;
    private boolean bPullbackAction = false;
    //private static int ClassifyStatus = -1;
    private Dialog  ClassifyAlertDialg;
    
	private ImageView ClassifyLoadingdot1;
	private ImageView ClassifyLoadingdot2;
	private ImageView ClassifyLoadingdot3;
	private RelativeLayout mClassifyAnimationLayout;
	private AnimatorSet allSet;
    private ClassifyLoadingAnimation clsAnimation = new ClassifyLoadingAnimation();
    	
    public int getStatusBarHeight(){
    	if(mStatusBarHeight == 0){
    		Class<?> c = null;
    		Object obj = null;
    		java.lang.reflect.Field field = null;
    		int x = 0;
    		try {
    			c = Class.forName("com.android.internal.R$dimen");
    			obj = c.newInstance();
    			field = c.getField("status_bar_height");
    			x = Integer.parseInt(field.get(obj).toString());
    			mStatusBarHeight = getResources().getDimensionPixelSize(x);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return mStatusBarHeight;
    }
    
    private void initScreen(){
    	mQuickIndexShadowImageHeight = getResources().getDimensionPixelOffset(R.dimen.quick_index_shadow_image_height);
        mQuickIndexShadowImageRightMargin = getResources().getDimensionPixelOffset(R.dimen.quick_index_shadow_image_margin_right);
        
		mStatusBarHeight = getStatusBarHeight();
		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
		//Log.e("HJJ", "==Screen width:" + mScreenWidth + ", height:" + mScreenHeight + ",statusbar:" + mStatusBarHeight);
    }
    // Aurora <haojj> <2013-09-09> add for initing end
	private HideFromAccessibilityHelper mHideFromAccessibilityHelper = new HideFromAccessibilityHelper();

	private Runnable mBuildLayersRunnable = new Runnable() {
		public void run() {
			if (mWorkspace != null) {
				mWorkspace.buildPageHardwareLayers();
			}
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		Log.i("xiejun","newConfig = "+ newConfig.toString());
		super.onConfigurationChanged(newConfig);
	}

	private static ArrayList<PendingAddArguments> sPendingAddList = new ArrayList<PendingAddArguments>();

	private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

	private static class PendingAddArguments {
		int requestCode;
		Intent intent;
		long container;
		int screen;
		int cellX;
		int cellY;
	}

	private static boolean isPropertyEnabled(String propertyName) {
		return Log.isLoggable(propertyName, Log.VERBOSE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("linp","--------------------------------------------Launcher onCreate");
		if (DEBUG_STRICT_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork() // or.detectAll() for all detectable problems
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
					.penaltyLog().penaltyDeath().build());
		}
		
		super.onCreate(savedInstanceState);
		
		//Vulcan removed it in 2014-6-11
		//call open in static code block
		//LauncherApplication.logVulcan.open(this,"logVulcan.txt");
		LauncherApplication.logVulcan.print("=========================>onCreate,first log");
		LauncherApplication.logVulcan.print("Launcher built time:" + LogWriter.timeBuilt());
		Log.d("vulcan","Launcher.onCreate() is being called!!!");
		
		LauncherApplication app = ((LauncherApplication) getApplication());
		mSharedPrefs = getSharedPreferences(
				LauncherApplication.getSharedPreferencesKey(),
				Context.MODE_PRIVATE);
		mModel = app.setLauncher(this);
		mIconCache = app.getIconCache();
		mDragController = new DragController(this);
		mInflater = getLayoutInflater();
		
		DynIconPlg.createPlugin(this);
		

		mAppWidgetManager = AppWidgetManager.getInstance(this);
		mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
		mAppWidgetHost.startListening();

		// If we are getting an onCreate, we can actually preempt onResume and
		// unset mPaused here,
		// this also ensures that any synchronous binding below doesn't
		// re-trigger another
		// LauncherModel load.
		mPaused = false;

		if (PROFILE_STARTUP) {
			android.os.Debug.startMethodTracing(Environment
					.getExternalStorageDirectory() + "/launcher");
		}
		checkForLocaleChange();
		setContentView(R.layout.launcher);
		inflater=LayoutInflater.from(this);
		mCustomMenuProxy=(CustomMenuProxy)findViewById(R.id.custom_menu_proxy_id);
		mCustomMenuProxy.setAnimationStyle(R.style.PopupAnimation);
		mCustomMenuProxy.setCustomMenuItemListener(mCustomMenuItemClickListener);
		setupViews();
        initScreen();
		mModel.setWorkSpace(mWorkspace);
		app.registerAddShortcut(mAddshortcut);	
		//showFirstRunWorkspaceCling();
		registerContentObservers();
		lockAllApps();
		mSavedState = savedInstanceState;
		restoreState(mSavedState);
		// Update customization drawer _after_ restoring the states
		/*if (mAppsCustomizeContent != null) {
			mAppsCustomizeContent.onPackagesUpdated();
		}*/
		if (mWidgetViewContent != null) {
			mWidgetViewContent.onPackagesUpdated();
		}
		if (PROFILE_STARTUP) {
			android.os.Debug.stopMethodTracing();
		}

		if (!mRestoring) {
			if (sPausedFromUserAction) {
				// If the user leaves launcher, then we should just load items
				// asynchronously when
				// they return.
				LauncherApplication.logVulcan.print("to call startLoader when onCreate with sPausedFromUserAction");
				Log.d("vulcan-finish","to call startLoader when onCreate with sPausedFromUserAction!");
				mModel.startLoader(true, -1);
			} else {
				// We only load the page synchronously if the user rotates (or
				// triggers a
				// configuration change) while launcher is in the foreground
				LauncherApplication.logVulcan.print("to call startLoader when onCreate with mWorkspace.getCurrentPage");
				Log.d("vulcan-finish","to call startLoader when onCreate with mWorkspace.getCurrentPage!");
				mModel.startLoader(true, mWorkspace.getCurrentPage());
			}
		}

		if (!mModel.isAllAppsLoaded()) {
			ViewGroup appsCustomizeContentParent = (ViewGroup) mAppsCustomizeContent
					.getParent();
			mInflater.inflate(R.layout.apps_customize_progressbar,
					appsCustomizeContentParent);
		}

		// For handling default keys
		mDefaultKeySsb = new SpannableStringBuilder();
		Selection.setSelection(mDefaultKeySsb, 0);

		IntentFilter filter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		/*
		 * Add action to disable | enable to expand statusbar
		 */
		filter.addAction(EXPAND_STATUSBAR_ENABLE);
		filter.addAction(EXPAND_STATUSBAR_DISABLE);
		registerReceiver(mCloseSystemDialogsReceiver, filter);
		// On large interfaces, we want the screen to auto-rotate based on the
		// current orientation
		unlockScreenOrientation(true);	
		IntentFilter startAnimIntentFilter = new IntentFilter(unlockToLauncherRecieverAction);
		registerReceiver(startAnim,startAnimIntentFilter);
		//For voice test
		mMasterToSlaveIntent = new Intent();
		mSlaveToMasterFilter = new IntentFilter();
		mSlaveToMasterFilter.addAction(voiceInputChanged_Receive);
		mSlaveToMasterFilter.addAction(ADD_NAVIGATION_TRANSLUCENT_FLAG);
		mSlaveToMasterFilter.addAction(ENTER_VOICEMODE_BY_VOICEPRINT);
		mSlaveToMasterFilter.addAction(ACTION_NAVIGATION_HINT_BACK);
		mSlaveToMasterFilter.addAction(ACTION_VOICE_CLEAR_SCREEN);
		registerReceiver(mSlaveToMasterReceiver, mSlaveToMasterFilter);
		startCheckVoicePrintStatus(getIntent());
		//获取电话服务  
		//TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);  
        //manager.listen(mMyPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); 
	
		
	}

	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		sPausedFromUserAction = true;
	}

	private void updateGlobalIcons() {
	}

	private void checkForLocaleChange() {
		if (sLocaleConfiguration == null) {
			new AsyncTask<Void, Void, LocaleConfiguration>() {
				@Override
				protected LocaleConfiguration doInBackground(Void... unused) {
					LocaleConfiguration localeConfiguration = new LocaleConfiguration();
					readConfiguration(Launcher.this, localeConfiguration);
					return localeConfiguration;
				}

				@Override
				protected void onPostExecute(LocaleConfiguration result) {
					sLocaleConfiguration = result;
					checkForLocaleChange(); // recursive, but now with a locale configuration
				}
			}.execute();
			return;
		}

		final Configuration configuration = getResources().getConfiguration();

		final String previousLocale = sLocaleConfiguration.locale;
		final String locale = configuration.locale.toString();

		final int previousMcc = sLocaleConfiguration.mcc;
		final int mcc = configuration.mcc;

		final int previousMnc = sLocaleConfiguration.mnc;
		final int mnc = configuration.mnc;

		boolean localeChanged = !locale.equals(previousLocale)
				|| mcc != previousMcc || mnc != previousMnc;

		if (localeChanged) {
			sLocaleConfiguration.locale = locale;
			sLocaleConfiguration.mcc = mcc;
			sLocaleConfiguration.mnc = mnc;

			mIconCache.flush();

			final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
			new Thread("WriteLocaleConfiguration") {
				@Override
				public void run() {
					writeConfiguration(Launcher.this, localeConfiguration);
				}
			}.start();
		}
	}
	

	private static class LocaleConfiguration {
		public String locale;
		public int mcc = -1;
		public int mnc = -1;
	}

	private static void readConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataInputStream in = null;
		try {
			in = new DataInputStream(context.openFileInput(PREFERENCES));
			configuration.locale = in.readUTF();
			configuration.mcc = in.readInt();
			configuration.mnc = in.readInt();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// Ignore
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	private static void writeConfiguration(Context context,
			LocaleConfiguration configuration) {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(context.openFileOutput(PREFERENCES,
					MODE_PRIVATE));
			out.writeUTF(configuration.locale);
			out.writeInt(configuration.mcc);
			out.writeInt(configuration.mnc);
			out.flush();
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			// noinspection ResultOfMethodCallIgnored
			context.getFileStreamPath(PREFERENCES).delete();
			LauncherApplication.logVulcan.print("IOException when writeConfiguration");
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

	public DragLayer getDragLayer() {
		return mDragLayer;
	}

	boolean isDraggingEnabled() {
		// We prevent dragging when we are loading the workspace as it is
		// possible to pick up a view
		// that is subsequently removed from the workspace in startBinding().
		return !mModel.isLoadingWorkspace();
	}

	static int getScreen() {
		synchronized (sLock) {
			return sScreen;
		}
	}

	static void setScreen(int screen) {
		synchronized (sLock) {
			sScreen = screen;
		}
	}

	/**
	 * Returns whether we should delay spring loaded mode -- for shortcuts and
	 * widgets that have a configuration step, this allows the proper animations
	 * to run after other transitions.
	 */
	private boolean completeAdd(PendingAddArguments args) {
		boolean result = false;
		switch (args.requestCode) {
		case REQUEST_PICK_APPLICATION:
			completeAddApplication(args.intent, args.container, args.screen,
					args.cellX, args.cellY);
			break;
		case REQUEST_PICK_SHORTCUT:
			processShortcut(args.intent);
			break;
		case REQUEST_CREATE_SHORTCUT:
			completeAddShortcut(args.intent, args.container, args.screen,
					args.cellX, args.cellY);
			result = true;
			break;
		case REQUEST_CREATE_APPWIDGET:
			int appWidgetId = args.intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			completeAddAppWidget(appWidgetId, args.container, args.screen,
					null, null);
			result = true;
			break;
		case REQUEST_PICK_WALLPAPER:
			// We just wanted the activity result here so we can clear
			// mWaitingForResult
			break;
		}
		// Before adding this resetAddInfo(), after a shortcut was added to a
		// workspace screen,
		// if you turned the screen off and then back while in All Apps,
		// Launcher would not
		// return to the workspace. Clearing mAddInfo.container here fixes this
		// issue
		resetAddInfo();
		return result;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		//AURORA-START::Fix bug #3429::Shi guiqiang::20140328
		mWidgetViewContent.setWidgetClickable(false);
		mWidgetViewContent.delaytToAddWidgetEnable(300);
		//AURORA-END::Fix bug #3429::Shi guiqiang::20140328
		
		if (requestCode == REQUEST_BIND_APPWIDGET) {
			int appWidgetId = data != null ? data.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
			if (resultCode == RESULT_CANCELED) {
				completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
			} else if (resultCode == RESULT_OK) {
				addAppWidgetImpl(appWidgetId, mPendingAddInfo, null,
						mPendingAddWidgetInfo);
			}
			return;
		}
		boolean delayExitSpringLoadedMode = false;
		boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET);
		mWaitingForResult = false;

		// We have special handling for widgets
		if (isWidgetDrop) {
			int appWidgetId = data != null ? data.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
			if (appWidgetId < 0) {
				Log.e(TAG,
						"Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\"
								+ "widget configuration activity.");
				completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
			} else {
				completeTwoStageWidgetDrop(resultCode, appWidgetId);
			}
			return;
		}

		// The pattern used here is that a user PICKs a specific application,
		// which, depending on the target, might need to CREATE the actual
		// target.

		// For example, the user would PICK_SHORTCUT for "Music playlist", and
		// we
		// launch over to the Music app to actually CREATE_SHORTCUT.
		if (resultCode == RESULT_OK
				&& mPendingAddInfo.container != ItemInfo.NO_ID) {
			final PendingAddArguments args = new PendingAddArguments();
			args.requestCode = requestCode;
			args.intent = data;
			args.container = mPendingAddInfo.container;
			args.screen = mPendingAddInfo.screen;
			args.cellX = mPendingAddInfo.cellX;
			args.cellY = mPendingAddInfo.cellY;
			if (isWorkspaceLocked()) {
				sPendingAddList.add(args);
			} else {
				delayExitSpringLoadedMode = completeAdd(args);
			}
		}
		mDragLayer.clearAnimatedView();
		// Exit spring loaded mode if necessary after cancelling the
		// configuration of a widget
		exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED),
				delayExitSpringLoadedMode, null);
	}

	private void completeTwoStageWidgetDrop(final int resultCode,
			final int appWidgetId) {
		CellLayout cellLayout = (CellLayout) mWorkspace
				.getChildAt(mPendingAddInfo.screen);
		Runnable onCompleteRunnable = null;
		int animationType = 0;

		AppWidgetHostView boundWidget = null;
		if (resultCode == RESULT_OK) {
			animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
			final AppWidgetHostView layout = mAppWidgetHost.createView(this,
					appWidgetId, mPendingAddWidgetInfo);
			boundWidget = layout;
			onCompleteRunnable = new Runnable() {
				@Override
				public void run() {
					completeAddAppWidget(appWidgetId,
							mPendingAddInfo.container, mPendingAddInfo.screen,
							layout, null);
					exitSpringLoadedDragModeDelayed(
							(resultCode != RESULT_CANCELED), false, null);
				}
			};
		} else if (resultCode == RESULT_CANCELED) {
			animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
			onCompleteRunnable = new Runnable() {
				@Override
				public void run() {
					exitSpringLoadedDragModeDelayed(
							(resultCode != RESULT_CANCELED), false, null);
				}
			};
		}
		if (mDragLayer.getAnimatedView() != null) {
			mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
					(DragView) mDragLayer.getAnimatedView(),
					onCompleteRunnable, animationType, boundWidget, true);
		} else {
			// The animated view may be null in the case of a rotation during
			// widget configuration
			onCompleteRunnable.run();
		}
	}
	
	//ht 2014-09-11 文件夹内拖动图标至删除，被隐藏后显示出来；
	private FolderIcon mIcon;
	public void setFolderIcon(FolderIcon icon){
		mIcon = icon;
	}

	@Override
	protected void onResume() {
		super.onResume();
	//	 flipView.onResume();
		if(mEditMode!=EditMode.NONE){
			//exitEditMode();
		}
		// Restore the previous launcher state
		Log.i("xiejun8","mOnResumeState="+mOnResumeState);
		if (mOnResumeState == State.WORKSPACE) {
			showWorkspace(false);
		} else if (mOnResumeState == State.APPS_CUSTOMIZE) {
			showAllApps(false);
		}
		mOnResumeState = State.NONE;

		// Aurora <jialf> <2013-11-12> add for fix bug #434 begin
		if (checkAppUninstall(mAuroraUninstallPkgName)) {
			if (mAuroraDragView != null) {
				//ItemInfo info = (ItemInfo)mAuroraDragView.getTag();
				//if(info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
					mWorkspace.resetAuroraChild(mAuroraDragView, false);
					if (mAuroraDragView instanceof FolderIcon) {
						((FolderIcon) mAuroraDragView).setTextVisible(true);
					} else if(mAuroraDragView instanceof BubbleTextView){
						((BubbleTextView) mAuroraDragView).setTextColor(getResources()
								.getColor(R.color.workspace_icon_text_color));
					}
				//}
				CellLayout parent = mWorkspace.getParentCellLayoutForView(mAuroraDragView);
				if (parent != null) {
					parent.markCellsAsOccupiedForView(mAuroraDragView);
					mAuroraDragView.setVisibility(View.VISIBLE);
				}else{
					//AURORA_START:ht 2014-09-30
					//在跨区域拖拽的过程中（Desk->HotSeat->Desk,此时HotSeat并没有填满）至删除，导致DragInfo.cell与CellLayout分离，在而当最后一页时，造成其CellLayout无子View而被回收；
					//当判断其View无父容器时，则创建一个CellLayout;
					createNewPage();
					ItemInfo info = (ItemInfo)mAuroraDragView.getTag();
					mHandler.sendEmptyMessage(ICON_RESHOW);
					if(mWorkspace != null && mAuroraDragView != null && info != null){
						if(mWorkspace != null){
							int screen = mWorkspace.getChildCount()-1;
							mAuroraDragView.setVisibility(View.VISIBLE);
							mWorkspace.addInScreen(mAuroraDragView, LauncherSettings.Favorites.CONTAINER_DESKTOP
									, screen, info.cellX, info.cellY, info.spanX, info.spanY);
						}
					}
					//AURORA_END:ht 2014-09-30
				}
				resetAuroraUninstallData();
			}
		}else{
			resetAuroraUninstallData();
		}
		
		// Aurora <jialf> <2013-11-12> add for fix bug #434 end

		// Background was set to gradient in onPause(), restore to black if in
		// all apps.
		//setWorkspaceBackground(mState == State.WORKSPACE);

		// Process any items that were added while Launcher was away
		InstallShortcutReceiver.flushInstallQueue(this);

		mPaused = false;
		sPausedFromUserAction = false;
		if (mRestoring || mOnResumeNeedsLoad) {
			mWorkspaceLoading = true;
			LauncherApplication.logVulcan.print("to call startLoader when onResume");
			mModel.startLoader(true, -1);
			mRestoring = false;
			mOnResumeNeedsLoad = false;
		}

		// Reset the pressed state of icons that were locked in the press state
		// while activities
		// were launching
		if (mWaitingForResume != null) {
			// Resets the previous workspace icon press state
			mWaitingForResume.setStayPressed(false);
		}
		if (mAppsCustomizeContent != null) {
			// Resets the previous all apps icon press state
			mAppsCustomizeContent.resetDrawableState();
		}
		// It is possible that widgets can receive updates while launcher is not
		// in the foreground.
		// Consequently, the widgets will be inflated in the orientation of the
		// foreground activity
		// (framework issue). On resuming, we ensure that any widgets are
		// inflated for the current
		// orientation.
		getWorkspace().reinflateWidgetsIfNecessary();
		Log.i("xiejun8", "mEditMode="+mEditMode);
		if(mEditMode==EditMode.NONE){
			mWidgetViewLayout.setTranslationY(mWidgetViewLayout.getHeight()+170);
			mWidgetViewLayout.setVisibility(View.GONE);
			mWidgetViewLayout.setAlpha(0);
			
			mClassficateIonsLayout.setTranslationY(mClassficateIonsLayout.getHeight()+170);
			mClassficateIonsLayout.setVisibility(View.GONE);
			mClassficateIonsLayout.setAlpha(0);
		}
		/*
		if (lockState && !isOurLockMessage) {
			ALog.i("start", "2");
			lockState = false;
			mHandler.removeCallbacks(startAnimationRunnable);
			mHandler.post(startAnimationRunnable);
		}
		*/
		
		//ht 2014-09-11 显示文件夹内的内容
		if(mIcon != null){
			mIcon.addItem();
		}
		mIcon = null;
		
		//vulcan added it in 2014-6-20
		//notify the DynIconDriver that launcher resumes running.
		DynIconDriver.setRunningSwitch(true);
	}
	public  void dismissCustomMenu() {
		mCustomMenuProxy.dismissCustomMenu();
	}

	@Override
	protected void onPause() {
		// NOTE: We want all transitions from launcher to act as if the
		// wallpaper were enabled
		// to be consistent. So re-enable the flag here, and we will re-disable
		// it as necessary
		// when Launcher resumes and we are still in AllApps.
		updateWallpaperVisibility(true);
		super.onPause();
	//	 flipView.onPause();
		mPaused = true;
		// Aurora <jialf> <2013-11-15> add for fix bug #748 begin
		if(mQuickIndexOutline.getDragState()){
		    mQuickIndexOutline.cancelDrag();
		}
		boolean isDragging = mDragController.isDragging();
		Log.i(TAG, "child is draging : " + isDragging);
		if (isDragging) {
			Log.i(TAG, "drag view not null is : " + (mAuroraDragView != null));
			if (mAuroraDragView != null) {
				ItemInfo info = (ItemInfo) mAuroraDragView.getTag();
				CellLayout parent = mWorkspace.getParentCellLayoutForView(mAuroraDragView);
				boolean fromHotseat = mWorkspace.ismAuroraSwapTag();
				Log.i(TAG, "fromHotseat is : " + fromHotseat
						+ ", parent == null is : " + (parent == null));
				if ((parent == null && fromHotseat) || !fromHotseat) {
					mWorkspace.resetAuroraChild(mAuroraDragView, true);
				}
				if (parent != null) {
					parent.markCellsAsOccupiedForView(mAuroraDragView);
					mAuroraDragView.setVisibility(View.VISIBLE);
				}
				mAuroraDragView = null;
			}
		}
		
		//xiejun begin
		if (mWorkspace != null) {
			mWorkspace.snapToDestination();
		}
		Folder openFolder = mWorkspace.getOpenFolder();
		if(openFolder!=null){
			openFolder.snapToDestination();
		}
		//xiejun end
		
		
		// Aurora <jialf> <2013-11-15> add for fix bug #748 end
		cancelDrag();
		//aurora add by tangjun start 2013.10.25
		mPageIndicatotorTextView.setAlpha(0f);
		//aurora add by tangjun end 2013.10.25
		
		//vulcan added it in 2014-6-20
		//notify DynIconDriver that launcher pause running.
		DynIconDriver.setRunningSwitch(false);
		
		LauncherApplication.logVulcan.print("onPause is called!");
	}
	
	public void cancelDrag(){
		if(mDragController!=null){
			mDragController.cancelDrag();
			mDragController.resetLastGestureUpTime();
		}
			
	}
	
	public void exitFolderImportMode(boolean isOpenFolderIcon){
		showFolderIcon();
		if(isOpenFolderIcon)mOpenFolderIcon = isOpenFolderIcon;
		exitEditMode(true);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Flag the loader to stop early before switching
		mModel.stopLoader();
		if (mAppsCustomizeContent != null) {
			mAppsCustomizeContent.surrender();
		}
		return Boolean.TRUE;
	}

	// We can't hide the IME if it was forced open. So don't bother
	/*
	 * @Override public void onWindowFocusChanged(boolean hasFocus) {
	 * super.onWindowFocusChanged(hasFocus);
	 * 
	 * if (hasFocus) { final InputMethodManager inputManager =
	 * (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	 * WindowManager.LayoutParams lp = getWindow().getAttributes();
	 * inputManager.hideSoftInputFromWindow(lp.token, 0, new
	 * android.os.ResultReceiver(new android.os.Handler()) { protected void
	 * onReceiveResult(int resultCode, Bundle resultData) { Log.d(TAG,
	 * "ResultReceiver got resultCode=" + resultCode); } }); Log.d(TAG,
	 * "called hideSoftInputFromWindow from onWindowFocusChanged"); } }
	 */

	private boolean acceptFilter() {
		final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		return !inputManager.isFullscreenMode();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ALog.i("menu","event.getKeyCode() = "+event.getKeyCode());
		final int uniChar = event.getUnicodeChar();
		final boolean handled = super.onKeyDown(keyCode, event);
		final boolean isKeyNotWhitespace = uniChar > 0
				&& !Character.isWhitespace(uniChar);
		if (!handled && acceptFilter() && isKeyNotWhitespace) {
			boolean gotKey = TextKeyListener.getInstance().onKeyDown(
					mWorkspace, mDefaultKeySsb, keyCode, event);
			if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
				// something usable has been typed - start a search
				// the typed text will be retrieved and cleared by
				// showSearchDialog()
				// If there are multiple keystrokes before the search dialog
				// takes focus,
				// onSearchRequested() will be called for every keystroke,
				// but it is idempotent, so it's fine.
				ALog.i(TAG,"onKeyDown  return 1");
				return onSearchRequested();
			}
		}

		// Eat the long press event so the keyboard doesn't come up.
		if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
			ALog.i(TAG,"onKeyDown  return 2");
			return true;
		}
		return handled;
	}

	private String getTypedText() {
		return mDefaultKeySsb.toString();
	}

	private void clearTypedText() {
		mDefaultKeySsb.clear();
		mDefaultKeySsb.clearSpans();
		Selection.setSelection(mDefaultKeySsb, 0);
	}

	/**
	 * Given the integer (ordinal) value of a State enum instance, convert it to
	 * a variable of type State
	 */
	private static State intToState(int stateOrdinal) {
		State state = State.WORKSPACE;
		final State[] stateValues = State.values();
		for (int i = 0; i < stateValues.length; i++) {
			if (stateValues[i].ordinal() == stateOrdinal) {
				state = stateValues[i];
				break;
			}
		}
		return state;
	}

	/**
	 * Restores the previous state, if it exists.
	 * 
	 * @param savedState
	 *            The previous state.
	 */
	private void restoreState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		State state = intToState(savedState.getInt(RUNTIME_STATE,
				State.WORKSPACE.ordinal()));
		if (state == State.APPS_CUSTOMIZE) {
			mOnResumeState = State.APPS_CUSTOMIZE;
		}

		int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
		if (currentScreen > -1) {
			mWorkspace.setCurrentPage(currentScreen);
		}

		if (mModel != null) {
			boolean mounted = savedState.getBoolean(SDCARD_MOUNTED_STATE);
			mModel.setmSdcardMounted(mounted);
			LauncherApplication.logVulcan.print("in restoreState setmSdcardMounted to" + mounted);
		}

		final long pendingAddContainer = savedState.getLong(
				RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
		final int pendingAddScreen = savedState.getInt(
				RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

		if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
			mPendingAddInfo.container = pendingAddContainer;
			mPendingAddInfo.screen = pendingAddScreen;
			mPendingAddInfo.cellX = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
			mPendingAddInfo.cellY = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
			mPendingAddInfo.spanX = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
			mPendingAddInfo.spanY = savedState
					.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
			mPendingAddWidgetInfo = savedState
					.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
			mWaitingForResult = true;
			mRestoring = true;
		}

		boolean renameFolder = savedState.getBoolean(
				RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
		if (renameFolder) {
			long id = savedState
					.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
			mFolderInfo = mModel.getFolderById(this, sFolders, id);
			mRestoring = true;
		}

		// Restore the AppsCustomize tab
		if (mAppsCustomizeTabHost != null) {
			String curTab = savedState.getString("apps_customize_currentTab");
			if (curTab != null) {
				mAppsCustomizeTabHost
						.setContentTypeImmediate(mAppsCustomizeTabHost
								.getContentTypeForTabTag(curTab));
				mAppsCustomizeContent.loadAssociatedPages(mAppsCustomizeContent
						.getCurrentPage());
			}

			int currentIndex = savedState.getInt("apps_customize_currentIndex");
			mAppsCustomizeContent.restorePageForIndex(currentIndex);
		}
	}
	
	/**
	 * @return previewBar tangjun 9.24
	 */
	public ThumbnailHorizontalScrollView getHorizontalScrollView( ) {
		
		return previewBar;
	}
	
	/**
	 * @return previewContent tangjun 9.24
	 */
	public ThumbnailImage getPreviewContent( ) {
		
		return previewContent;
	}
	
	/**
	 * setPreviewContentGravity tangjun 9.25
	 */
	public void setPreviewContentGravity( ) {
		
		int count = mWorkspace.getChildCount();
		//int count = 10;
		HorizontalScrollView.LayoutParams lp = new HorizontalScrollView.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);  
		
		if ( count <= ThumbnailImage.COUNTINONEPAGE ) {
			//此处相当于布局文件中的Android:layout_gravity属性   
			lp.gravity = Gravity.CENTER_HORIZONTAL;  
			
		} else {
			
			lp.gravity = Gravity.LEFT;  
		}
		previewContent.setLayoutParams(lp);
	}
	
	/**
	 * @param curPos setPreviewBarScroll tangjun 9.26
	 */
	public void setPreviewBarScroll ( int curPos ) {

		curPos += 1;//iht 2014-09-25
		
		int count = mWorkspace.getChildCount();		
		// 设置初始页
		if ( count > ThumbnailImage.COUNTINONEPAGE ) {
			
			//说明处在第一页；则起始位置为：0；
			if(curPos <= ThumbnailImage.COUNTINONEPAGE){
				previewBar.setLeftpage(0);
			}else{
				if(curPos / ThumbnailImage.COUNTINONEPAGE  < count / ThumbnailImage.COUNTINONEPAGE){
					previewBar.setLeftpage(( curPos / ThumbnailImage.COUNTINONEPAGE ) * ThumbnailImage.COUNTINONEPAGE);
				}else{
					//相等-->到结尾
					previewBar.setLeftpage((count/ThumbnailImage.COUNTINONEPAGE)*ThumbnailImage.COUNTINONEPAGE);
				}
			}
			
			/*if ( curPos / ThumbnailImage.COUNTINONEPAGE  < count / ThumbnailImage.COUNTINONEPAGE ) {
				previewBar.setLeftpage(( curPos / ThumbnailImage.COUNTINONEPAGE ) * ThumbnailImage.COUNTINONEPAGE);
			} else {
				previewBar.setLeftpage(count - ThumbnailImage.COUNTINONEPAGE);
			}
			//previewBar.setScrollX(previewBar.getLeftpage() * ThumbnailImage.ITEMWIDTH); //144
			//previewBar.getScroller().startScroll(0, 0, (previewBar.getLeftpage()) * ThumbnailImage.ITEMWIDTH, 0, 1);
			*/
			
			previewContent.setTranslationX( -previewBar.getLeftpage() * ThumbnailImage.ITEMWIDTH);
		}
	}
	
	/**
	 * @param PageNum  Scroll to which page  xiexiujie 9.16
	 * use for AppsCustomizeAsyncTask
	 */
	public void  scrollToPage(int PageNum) {
		mWorkspace.snapToPage(PageNum);		
	}
	
	/**
	 * @param leftorright smoothPreviewBarScroll tangjun 9.26
	 */
	public void smoothPreviewBarScroll(int leftorright) {
		int count = mWorkspace.getChildCount();
		/*
		if (leftorright == 0) {
			if (previewBar.getLeftpage() - ThumbnailImage.COUNTINONEPAGE >= 0) {
				previewBar.setLeftpage(previewBar.getLeftpage()- ThumbnailImage.COUNTINONEPAGE);
			} else {
				previewBar.setLeftpage(0);
			}
			//mWorkspace.snapToPage(previewBar.getLeftpage()); //让页面一起滚动
		}
		else {
			if (previewBar.getLeftpage() + 2 * ThumbnailImage.COUNTINONEPAGE <= count) {
				previewBar.setLeftpage(previewBar.getLeftpage()+ ThumbnailImage.COUNTINONEPAGE); //显示的时候 +1；
			} else {
				//previewBar.setLeftpage(count - ThumbnailImage.COUNTINONEPAGE);
				previewBar.setLeftpage((count/ThumbnailImage.COUNTINONEPAGE)*ThumbnailImage.COUNTINONEPAGE);
			}
			//mWorkspace.snapToPage(previewBar.getLeftpage()+ ThumbnailImage.COUNTINONEPAGE - 1);
		}
		//previewBar.smoothScrollTo(previewBar.getLeftpage() * ThumbnailImage.ITEMWIDTH, 0); //x：移动的终点
		//previewBar.getScroller().startScroll(0, 0, previewBar.getLeftpage() * ThumbnailImage.ITEMWIDTH, 0, 1);
		*/
		
		if(leftorright == 0){
			if(previewBar.getLeftpage() - ThumbnailImage.COUNTINONEPAGE  >=  0){
				previewBar.setLeftpage(previewBar.getLeftpage()- ThumbnailImage.COUNTINONEPAGE);
			}else{
				previewBar.setLeftpage(0);
			}
		}else{
			if(previewBar.getLeftpage() + ThumbnailImage.COUNTINONEPAGE <= count){
				previewBar.setLeftpage(previewBar.getLeftpage()+ ThumbnailImage.COUNTINONEPAGE);
			}else{
				previewBar.setLeftpage( (count/ThumbnailImage.COUNTINONEPAGE)*ThumbnailImage.COUNTINONEPAGE );
			}
		}
		
		ObjectAnimator trans = ObjectAnimator.ofFloat(previewContent, "translationX", 
				previewContent.getTranslationX(), -previewBar.getLeftpage() * ThumbnailImage.ITEMWIDTH);
		trans.setDuration(200);
		trans.setInterpolator(new DecelerateInterpolator());
		trans.start();
		
		//判断翻页的“< >”显示
		setThumbnailImageArrowHead();
	}
	
	/**
	 * @param leftorright smoothPreviewBarScrollForSnapTo tangjun 10.18
	 */
	public void smoothPreviewBarScrollForSnapTo ( int leftorright ) {
		int count = mWorkspace.getChildCount();
		if( leftorright == 0 ) {
			if ( previewBar.getLeftpage() - ThumbnailImage.COUNTINONEPAGE >= 0 ) { 
				previewBar.setLeftpage(previewBar.getLeftpage() - ThumbnailImage.COUNTINONEPAGE);
			} else {
				previewBar.setLeftpage(0);
			}
		} else {
			
			if ( previewBar.getLeftpage() + 2 * ThumbnailImage.COUNTINONEPAGE <= count ) { 
				previewBar.setLeftpage(previewBar.getLeftpage() + ThumbnailImage.COUNTINONEPAGE);
			} else {
				previewBar.setLeftpage(count - ThumbnailImage.COUNTINONEPAGE);
			}
		}
		previewBar.smoothScrollTo(previewBar.getLeftpage() * ThumbnailImage.ITEMWIDTH, 0);
		
		if (getDragController().isDragging()) {
//			setThumbnailImageArrowHead( );
		}
	}
	
	/**
	 * @param curPos  setThumbnailImageArrowHead  tangjun 9.25
	 */
	public void setThumbnailImageArrowHead() {
		int count = mWorkspace.getChildCount();
		if (count <= ThumbnailImage.COUNTINONEPAGE) {
			leftarrowhead.setVisibility(View.GONE);
			rightarrowhead.setVisibility(View.GONE);
		} else {
			if (previewBar.getLeftpage() == 0) {
				leftarrowhead.setVisibility(View.GONE);
			} else {
				leftarrowhead.setVisibility(View.VISIBLE);
			}
			if (previewBar.getLeftpage() + ThumbnailImage.COUNTINONEPAGE >= count) {
				rightarrowhead.setVisibility(View.GONE);
			} else {
				rightarrowhead.setVisibility(View.VISIBLE);
			}
		}
	}
	
	/**
	 * @param alpha   tangjun 10.12
	 */
	public void setPageIndicatotorTextViewAlpha( float alpha ) {
		if ( alpha == 1f ) {
			if ( bgAnim != null && bgAnim.isStarted() == true ) {
				bgAnim.cancel();
			}
			mPageIndicatotorTextView.setAlpha(alpha);
		} else {
			if ( mPageIndicatotorTextView.getAlpha() > 0 ) {
				bgAnim = ValueAnimator.ofFloat(mPageIndicatotorTextView.getAlpha(), 0f).setDuration(500);
				bgAnim.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// TODO Auto-generated method stub
						mPageIndicatotorTextView.setAlpha(((Float) bgAnim.getAnimatedValue()).floatValue());
					}
				});
	            bgAnim.start();
			}
		}
	}
	
	/**
	 * @param visible is visible or not tangjun 9.25
	 */
	@SuppressLint("NewApi")
	public void thumbnailImageSetVisibility( int visible ) {
		leftarrowhead.setVisibility( visible );
		rightarrowhead.setVisibility( visible );
	}
	
	/**
	 * @param screen     tangjun 10.08
	 * @param delete
	 */
	private void thumbnailImageOnPageCountChange( int screen, boolean delete ) {
	
		if(delete){
			LauncherApplication.logVulcan.print("delete screen on thumbnailImageOnPageCountChange,screen = " + screen);
			if(screen==-1){
				int count = mWorkspace.getChildCount();
				removeThumbnailImageItem(count - 1);
			}else{
				removeThumbnailImageItem(screen);
			}
		}else{
			addThumbnailImageItem();
		}
		setPreviewContentGravity( );
	}
	
	/**
	 * @param index  tangjun 10.08
	 */
	private void removeThumbnailImageItem( int index ) {
		ThumbnailImageItem target = (ThumbnailImageItem)previewContent.getChildAt(index);
		previewContent.removeViewAt(index);
		mDragController.removeDropTarget(target);
	}
	
	/**
	 *   tangjun 9.26
	 */
	private void addThumbnailImageItem ( ) {
		ThumbnailImageItem item = new ThumbnailImageItem(this);
		item.setLauncher(this);
		item.setBackgroundResource(R.drawable.thumbnailsmall);
		item.setGravity(Gravity.CENTER);
		item.setAlpha(0f);
		previewContent.addView(item);
		mDragController.addDropTarget(item);
	}
	
	/**
	 * show ThumbnailImage  tangjun
	 */
	private void thumbnailImageInit() {
		int count = mWorkspace.getChildCount();
		int itemWidth = Utilities.getIntegerValueFromResourcesDimens(getResources(),R.dimen.thumbnail_item_width);
		previewBar = (ThumbnailHorizontalScrollView) findViewById(R.id.previewbar);
		previewBar.setTranslationY(-25.0f);
		leftarrowhead = (ThumbnailImageArrowhead) findViewById(R.id.leftarrowhead);
		rightarrowhead = (ThumbnailImageArrowhead) findViewById(R.id.rightarrowhead);
		thumbnailImageSetVisibility( View.GONE );
		
		previewContent.setThumbnailImagetextView(mPageIndicatotorTextView);
		previewContent.removeAllViews();
		for (int i = 0; i < count; i++) {
			addThumbnailImageItem();
		}
		
		leftarrowhead.setLauncher(this);
		rightarrowhead.setLauncher(this);
		previewContent.setLauncher(this);
		previewContent.setItemWidth(itemWidth);
		mDragController.addDropTarget(leftarrowhead);
		mDragController.addDropTarget(rightarrowhead);
		
		setPreviewContentGravity( );
	}

	/**
	 * Finds all the views we need and configure them properly.
	 */
	private void setupViews() {
		final DragController dragController = mDragController;
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = 	(int)getResources().getDimension(R.dimen.display_height);
		mLauncherView = findViewById(R.id.launcher);
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
		mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		//mDragLayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		
		//Aurora-start:xiejun
        if(mSharedPrefs.getBoolean(WALLPAPER_FIRST_TIME_SET, true)){
			getWorkspace().setWallpaperLoad(true);
        }
		//Aurora-end:xiejun
		// Setup the drag layer
		mDragLayer.setup(this, dragController);
		mDragController.registPintch(this);
		// Setup the hotseat
		mHotseat = (Hotseat) findViewById(R.id.hotseat);
		if (mHotseat != null) {
			mHotseat.setup(this);
		}
		mWidgetViewLayout = (LinearLayout) findViewById(R.id.widget_preview_baselayout);
		mWidgetViewContent = (AppsCustomizePagedView) findViewById(R.id.customize_widget_content);
		mWidgetViewContent.setup(this, dragController);
		mWidgetframeleft = (ImageView) findViewById(R.id.widgetframeleft);
		mWidgetframeright = (ImageView) findViewById(R.id.widgetframeright);
		mPageIndicatotorTextView = (TextView) findViewById(R.id.page_navigation);
		mPageIndicatotorTextView.setTextColor(0x99000000);
		mPageIndicator = (PageIndicator)findViewById(R.id.paged_view_indicator);
		mPageIndicator.setPageIndicatortextView(mPageIndicatotorTextView);
		mDragController.setDragControllertextView(mPageIndicatotorTextView);
		mAppSearchBgImageView = (ImageView) findViewById(R.id.apps_search_bg);
		mAppSearchImageView = (AppSearchImageView) findViewById(R.id.appsearchimageview);
		mLayerMask = (GlsurfaceMask)findViewById(R.id.layer_mask);
		mLayerMask.setAlpha(0f);
	
		mAppSearchImageView.setTranslationX(screenWidth);
		mAppSearchImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mAppSearchAnimatorGoing && !mAppRetrievalAppListAnimator) {
					enterFilterSearchIndexMode();
					enterAppIndexSearchAnimator(true, true, 350, null,
							APP_RETRIEVAL_NONE);
					mQuickIndexSearchEditText.setVisibility(View.VISIBLE);
					mQuickIndexSearchEditText.setAlpha(0f);
					showSearchEditTextInputSoftKey();
					mSearchQuickIndexListView.bringToFront();
				}else{
					Log.e("linp", "----------------------------still running in app search mode. so any click will ignored");
				}
			}
		});
		mDragController.setAppSearchViewContext(mAppSearchImageView);
		

        // Aurora <haojj> <2013-12-12> add for 快速检索功能 begin
		mQuickIndexListView = (StickyListHeadersListView)findViewById(R.id.quick_index_list_view);
		mQuickIndexListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideSearchEditTextInputSoftKey();
				return false;
			}
		});
		// 添加此段的目的是为了解决S4滑动有黑线的问题，后文字没有阴影后便不会有黑线了，因此注释掉
		/*mQuickIndexListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if(mAppSearchImageView != null) mAppSearchImageView.invalidate();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});*/
		
		mSearchQuickIndexListView = (StickyListHeadersListView)findViewById(R.id.quick_index_search_list_view);
		mSearchQuickIndexListView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideSearchEditTextInputSoftKey();
				return false;
			}
		});
		
		mQuickIndexOutline = (QuickIndexFrameLayout) findViewById(R.id.outline_framelayout);
		mQuickIndexLayout = (QuickIndexLayout)findViewById(R.id.quick_index_layout);		
		mQuickIndexTitle = (TextView) findViewById(R.id.quick_index_title);
		try {
			Typeface tf = Typeface.createFromFile("system/fonts/title.ttf");
			mQuickIndexTitle.setTypeface(tf);
			mTypeface = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
			mPageIndicatotorTextView.setTypeface(mTypeface);
		} catch (Exception e) {
			ALog.i(TAG,"Typeface:"+e);
		}
		
		mQuickIndexTitle.setText(R.string.quick_index_title);		
		letterSideBar = (AlphbetSideBar)findViewById(R.id.letterSideBar);
		
		mQuickIndexSearchEditText = (EditText) findViewById(R.id.quick_index_search_edit_text);
		mQuickIndexSearchEditText.setOnEditorActionListener(this);
		mQuickIndexSearchEditText.addTextChangedListener(mTextWatcher);
		mQuickIndexBgBlurView = (ImageView)findViewById(R.id.appindex_blur_backgroud);
		mDragController.setAppContentViewContext(mQuickIndexLayout);
		mQuickIndexLayout.setTranslationY(screenHeight);
		setTransMode(TransModeEnum.NONE);
		mResultEmpty = (View)findViewById(R.id.quick_search_empty);
		mShadowImageView = (DefMarginImageView) findViewById(R.id.shadowimage);
		mUpperShadowImageView = (ImageView) findViewById(R.id.shadowimage_upper); 
		
		//For voice test
		mVoiceOutLine = (FrameLayout) findViewById(R.id.voiceoutline);
		mVoiceLayout = (VoiceLayout) findViewById(R.id.voicelayout);
		mVoiceLayout.setTranslationY(screenHeight);
		mVoiceButton = (VoiceButtonImageView) findViewById(R.id.voicebuttonimage);
		mVoiceButton.setBackgroundResource(R.drawable.voicebutton_mic);
		mVoiceButton.setTranslationX(-screenWidth);
		//For voice test
	
		previewContent = (ThumbnailImage)findViewById(R.id.preview_content);
		// Aurora <haojj> <2013-09-09> add for initing folder blur background begin
		mFolderBgBlurView = (ImageView)findViewById(R.id.folder_blur_backgroud);
		// Aurora <haojj> <2013-09-09> add for initing folder blur background end
		
		// Setup the workspace
		mWorkspace.setHapticFeedbackEnabled(false);
		mWorkspace.setOnLongClickListener(this);
		mWorkspace.setup(dragController);
		mWorkspace.registPageCountListener(mPageIndicator);
		mWorkspace.setOnClickListener(this);
		mWorkspace.registPageCountListener(new PageCountChangeListener() {
			@Override
			public void onPageCountChange(int screen, boolean delete) {
			LauncherApplication.logVulcan.print(String.format("onPageCountChange: screen = %d, delete = %b", screen, delete));
				thumbnailImageOnPageCountChange(screen, delete);
			}
		});
		// add by xiexiujie for calender plugin icon start 10.13
		mWorkspace
				.setOnCalenderPluginListening(new Workspace.OnCalenderPluginListening() {

					@Override
					public void CalenderPluginListening() {
						Log.i("xiexiujie","____________CalenderPluginListening____________");
						
						CellLayout curLayout = (CellLayout) mWorkspace
								.getChildAt(mWorkspace.getCurrentPage());
						if (curLayout == null)
							return;

						ArrayList<ShortcutInfo> list = LauncherModel
								.getItemsByPackage(Launcher.this);
						if (list == null || list.size() == 0) {
							return;
						}
						if (list.get(0).container == -100) {

							if ((list.get(0).screen == mWorkspace
									.getCurrentPage() && play && !isInClassifyModel)) {
								Log.i("xiexiujie","____________CalenderPluginListening_______2_____");
								addCalenderAnimationView();
								Log.i("xiexiujie","____________CalenderPluginListening_______list size_____"+ list.size());

								LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
								lp.setMargins(
										getResources().getInteger(R.integer.calendar_icon_dynamic_wight),0,getResources().getInteger(R.integer.calendar_icon_dynamic_wight),
										getResources().getInteger(R.integer.calendar_icon_dynamic_workspace));
								flipView.setLayoutParams(lp);
								mWorkspace.addCalenderIconInScreen(layout,
										list.get(0).container,
										list.get(0).screen, list.get(0).cellX,
										list.get(0).cellY, list.get(0).spanX,
										list.get(0).spanY, false);

								CalendarIcon.refreshDay2(Launcher.this, currentTransModeEnum());
								play = false;
							}
						}

					}
				});
		// add by xiexiujie for calender plugin icon end 10.13
		dragController.addDragListener(mWorkspace);
		// Get the search/delete bar
		mSearchDropTargetBar = (SearchDropTargetBar) mDragLayer
				.findViewById(R.id.qsb_bar);
		// Setup AppsCustomize
		mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
		mAppsCustomizeContent = (AppsCustomizePagedView) mAppsCustomizeTabHost
				.findViewById(R.id.apps_customize_pane_content);
		mAppsCustomizeContent.setup(this, dragController);
		// Setup the drag controller (drop targets have to be added in reverse
		// order in priority)
		dragController.setDragScoller(mWorkspace);
		dragController.setScrollView(mDragLayer);
		dragController.setMoveTarget(mWorkspace);
		dragController.addDropTarget(mWorkspace);
		if (mSearchDropTargetBar != null) {
			mSearchDropTargetBar.setup(this, dragController);
		}	
		//AURAORA-START:
		mFolderImportHint=(TextView)findViewById(R.id.folder_import_hint);
		mFolderImportButton=(Button)findViewById(R.id.folder_import_button);
		setImportFolderText(true);
		mFolderImportButtonContainer=findViewById(R.id.folder_import_button_container);
		mFolderImportHintContainer=findViewById(R.id.folder_import_hint_container);
		mFolderImportButton.setOnClickListener(this);
		//AURORA-END:
		thumbnailImageInit();

		// add by xiexiujie for calender plugin icon start 10.13
		IntentFilter datefilter = new IntentFilter();		
		datefilter.addAction(Intent.ACTION_TIME_CHANGED);		
		datefilter.addAction(Intent.ACTION_DATE_CHANGED);
		datefilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		datefilter.addAction(Intent.ACTION_TIME_TICK);
		this.registerReceiver(mIntentDateReceiver, datefilter);	
		// add by xiexiujie for calender plugin icon end 10.13
		

		mClassficateIonsLayout = (LinearLayout)findViewById(R.id.icon_classfifcation_layout);
		btnClassifyOk = (Button)findViewById(R.id.btn_ok);
		btnCalssifyCancel= (Button)findViewById(R.id.btn_cancel);
		ClassifyLoadingdot1 = (ImageView)findViewById(R.id.loadingdot1);
		ClassifyLoadingdot2 = (ImageView)findViewById(R.id.loadingdot2);
		ClassifyLoadingdot3= (ImageView)findViewById(R.id.loadingdot3);
		mClassifyAnimationLayout = (RelativeLayout)findViewById(R.id.classifyLoadingLayout);
		btnClassifyOk.setOnClickListener(this);
		btnCalssifyCancel.setOnClickListener(this);
		btnClassifyOk.setClickable(false);
		btnCalssifyCancel.setClickable(false);
		clsAnimation.setAnimatorSet(clsAnimation.setupAnimation(ClassifyLoadingdot1, ClassifyLoadingdot2, ClassifyLoadingdot3));

	}

	// add by xiexiujie for calender plugin icon start 10.13
	private BroadcastReceiver mIntentDateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			 Log.i("xiexiujie", "_______launcehr________onReceive__1___");
			 String action = intent.getAction();  	           	              
	            if ((action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)
	            		|| action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_TIME_TICK))&& CalendarIcon.isPlay()) {
	        Log.i("xiexiujie", "_______launcehr________onReceive__2___");	
				ArrayList<ShortcutInfo> list = LauncherModel.getItemsByPackage(Launcher.this);
				if(list.size()==0){
					return;	
				}
			
				CellLayout curLayout = (CellLayout) mWorkspace
						.getChildAt(mWorkspace.getCurrentPage());

				if (curLayout == null)
					return;
				if (list.get(0).container == -101) {
					 addCalenderAnimationView();	
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(
							getResources().getInteger(
									R.integer.calendar_icon_dynamic_wight),
							0,
							getResources().getInteger(
									R.integer.calendar_icon_dynamic_wight),
							getResources().getInteger(
									R.integer.calendar_icon_dynamic_dock));
					flipView.setLayoutParams(lp);
					mWorkspace.addCalenderIconInScreen(layout,
							list.get(0).container, list.get(0).screen,
							list.get(0).cellX, list.get(0).cellY,
							list.get(0).spanX, list.get(0).spanY, false);
					CalendarIcon.refreshDay2(Launcher.this, currentTransModeEnum());
				}
				else if(list.get(0).container == -100 &&list.get(0).screen==mWorkspace.getCurrentPage()){
					 addCalenderAnimationView();	
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(
							getResources()
									.getInteger(
											R.integer.calendar_icon_dynamic_wight),
							0,
							getResources()
									.getInteger(
											R.integer.calendar_icon_dynamic_wight),
							getResources()
									.getInteger(
											R.integer.calendar_icon_dynamic_workspace));
					flipView.setLayoutParams(lp);
					mWorkspace.addCalenderIconInScreen(
							layout, list.get(0).container,
							list.get(0).screen,
							list.get(0).cellX,
							list.get(0).cellY,
							list.get(0).spanX,
							list.get(0).spanY, false);
					CalendarIcon.refreshDay2(Launcher.this, currentTransModeEnum());	
				}else{
					play = true;
					
				}
			
			}
		}
	};

	// add by xiexiujie for calender plugin icon end 10.13
	/**
	 * Creates a view representing a shortcut.
	 * 
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from R.layout.application.
	 */
	View createShortcut(ShortcutInfo info) {
		return createShortcut(R.layout.application,
				(ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()),
				info);

	}

	/**
	 * Creates a view representing a shortcut inflated from the specified
	 * resource.
	 * 
	 * @param layoutResId
	 *            The id of the XML layout used to create the shortcut.
	 * @param parent
	 *            The group the shortcut belongs to.
	 * @param info
	 *            The data structure describing the shortcut.
	 * 
	 * @return A View inflated from layoutResId.
	 */
	View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
		BubbleTextView favorite = (BubbleTextView) mInflater.inflate(
				layoutResId, parent, false);
        // Aurora <jialf> <2013-09-19> add for install apps begin
		if (info.newFlag) {
			favorite.setNewApp(true);
		}
        // Aurora <jialf> <2013-09-19> add for install apps end
		favorite.applyFromShortcutInfo(info, mIconCache);
		favorite.setOnClickListener(this);

		//vulcan changed it in 2014-6-9
		favorite.mDynIconPlg = DynIconPlg.produceDynIconPlg(info,favorite);
		//vulcan added in 2014-6-3 for dynamic icon
		//if(info.title.toString().contains("时钟")) {
		//	favorite.mDynIconPlg = new DynIconPlg((TextView)favorite);
		//}
		return favorite;
	}

	/**
	 * Add an application shortcut to the workspace.
	 * 
	 * @param data
	 *            The intent describing the application.
	 * @param cellInfo
	 *            The position on screen where to create the shortcut.
	 */
	void completeAddApplication(Intent data, long container, int screen,
			int cellX, int cellY) {
		final int[] cellXY = mTmpAddItemCellCoordinates;
		final CellLayout layout = getCellLayout(container, screen);

		// First we check if we already know the exact location where we want to
		// add this item.
		if (cellX >= 0 && cellY >= 0) {
			cellXY[0] = cellX;
			cellXY[1] = cellY;
		} else if (!layout.findCellForSpan(cellXY, 1, 1)) {
			showOutOfSpaceMessage(isHotseatLayout(layout));
			return;
		}
		final ShortcutInfo info = mModel.getShortcutInfo(getPackageManager(),
				data, this);
		if (info != null) {
			info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			info.container = ItemInfo.NO_ID;
			mWorkspace.addApplicationShortcut(info, layout, container, screen,
					cellXY[0], cellXY[1], isWorkspaceLocked(), cellX, cellY);
		} else {
			Log.e(TAG, "Couldn't find ActivityInfo for selected application: "
					+ data);
		}
	}

	/**
	 * Add a shortcut to the workspace.
	 * 
	 * @param data
	 *            The intent describing the shortcut.
	 * @param cellInfo
	 *            The position on screen where to create the shortcut.
	 */
	private void completeAddShortcut(Intent data, long container, int screen,
			int cellX, int cellY) {
		int[] cellXY = mTmpAddItemCellCoordinates;
		int[] touchXY = mPendingAddInfo.dropPos;
		CellLayout layout = getCellLayout(container, screen);

		if (layout == null) {
			return;
		}
				
		boolean foundCellSpan = false;
		ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
		if (info == null) {
			return;
		}
		final View view = createShortcut(info);

		// First we check if we already know the exact location where we want to
		// add this item.
		if (cellX >= 0 && cellY >= 0) {
			cellXY[0] = cellX;
			cellXY[1] = cellY;
			foundCellSpan = true;
			// If appropriate, either create a folder or add to an existing
			// folder
			if (mWorkspace.createUserFolderIfNecessary(view, container, layout,
					cellXY, 0, true, null, null)) {
				return;
			}
			DragObject dragObject = new DragObject();
			dragObject.dragInfo = info;
			if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY,
					0, dragObject, true)) {
				return;
			}
		} else if (touchXY != null) {
			// when dragging and dropping, just find the closest free spot
			int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1],
					1, 1, cellXY);
			foundCellSpan = (result != null);
		} else {
			foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
		}
		if (!foundCellSpan) {
			showOutOfSpaceMessage(isHotseatLayout(layout));
			return;
		}

		LauncherModel.addItemToDatabase(this, info, container, screen,
				cellXY[0], cellXY[1], false);
		if (!mRestoring) {
			mWorkspace.addInScreen(view, container, screen, cellXY[0],
					cellXY[1], 1, 1, isWorkspaceLocked());
		}
	}

	static int[] getSpanForWidget(Context context, ComponentName component,
			int minWidth, int minHeight) {
		Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context,
				component, null);
		// We want to account for the extra amount of padding that we are adding
		// to the widget
		// to ensure that it gets the full amount of space that it has requested
		int requiredWidth = minWidth + padding.left + padding.right;
		int requiredHeight = minHeight + padding.top + padding.bottom;
		return CellLayout.rectToCell(context.getResources(), requiredWidth,
				requiredHeight, null);
	}

	static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
		return getSpanForWidget(context, info.provider, info.minWidth,
				info.minHeight);
	}

	static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
		return getSpanForWidget(context, info.provider, info.minResizeWidth,
				info.minResizeHeight);
	}

	static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
		return getSpanForWidget(context, info.componentName, info.minWidth,
				info.minHeight);
	}

	static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
		return getSpanForWidget(context, info.componentName,
				info.minResizeWidth, info.minResizeHeight);
	}

	/**
	 * Add a widget to the workspace.
	 * 
	 * @param appWidgetId
	 *            The app widget id
	 * @param cellInfo
	 *            The position on screen where to create the widget.
	 */
	private void completeAddAppWidget(final int appWidgetId, long container,
			int screen, AppWidgetHostView hostView,
			AppWidgetProviderInfo appWidgetInfo) {
		if (appWidgetInfo == null) {
			appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		}

		// Calculate the grid spans needed to fit this widget
		CellLayout layout = getCellLayout(container, screen);

		if (layout == null) {
			return;
		}

		int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
		int[] spanXY = getSpanForWidget(this, appWidgetInfo);

		// Try finding open space on Launcher screen
		// We have saved the position to which the widget was dragged-- this
		// really only matters
		// if we are placing widgets on a "spring-loaded" screen
		int[] cellXY = mTmpAddItemCellCoordinates;
		int[] touchXY = mPendingAddInfo.dropPos;
		int[] finalSpan = new int[2];
		boolean foundCellSpan = false;
		if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
			cellXY[0] = mPendingAddInfo.cellX;
			cellXY[1] = mPendingAddInfo.cellY;
			spanXY[0] = mPendingAddInfo.spanX;
			spanXY[1] = mPendingAddInfo.spanY;
			foundCellSpan = true;
		} else if (touchXY != null) {
			// when dragging and dropping, just find the closest free spot
			int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1],
					minSpanXY[0], minSpanXY[1], spanXY[0], spanXY[1], cellXY,
					finalSpan);
			spanXY[0] = finalSpan[0];
			spanXY[1] = finalSpan[1];
			foundCellSpan = (result != null);
		} else {
			foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0],
					minSpanXY[1]);
		}

		if (!foundCellSpan) {
			if (appWidgetId != -1) {
				// Deleting an app widget ID is a void call but writes to disk
				// before returning
				// to the caller...
				new Thread("deleteAppWidgetId") {
					public void run() {
						mAppWidgetHost.deleteAppWidgetId(appWidgetId);
					}
				}.start();
			}
			showOutOfSpaceMessage(isHotseatLayout(layout));
			return;
		}

		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appWidgetId, appWidgetInfo.provider);
		launcherInfo.spanX = spanXY[0];
		launcherInfo.spanY = spanXY[1];
		launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
		launcherInfo.minSpanY = mPendingAddInfo.minSpanY;
		launcherInfo.title = String.format("widget(%d,%d,%d)", screen, cellXY[0],cellXY[1]);

		LauncherApplication.logVulcan.print(String.format("completeAddAppWidget: adding widget in DB: %s(%d,%d,%d)",
				launcherInfo.title,
				screen,
				cellXY[0],
				cellXY[1]));
		LauncherModel.addItemToDatabase(this, launcherInfo, container, screen,
				cellXY[0], cellXY[1], false);

		if (!mRestoring) {
			if (hostView == null) {
				// Perform actual inflation because we're live
				launcherInfo.hostView = mAppWidgetHost.createView(this,
						appWidgetId, appWidgetInfo);
				launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
			} else {
				// The AppWidgetHostView has already been inflated and
				// instantiated
				launcherInfo.hostView = hostView;
			}

			launcherInfo.hostView.setTag(launcherInfo);
			launcherInfo.hostView.setVisibility(View.VISIBLE);
			launcherInfo.notifyWidgetSizeChanged(this);

			mWorkspace.addInScreen(launcherInfo.hostView, container, screen,
					cellXY[0], cellXY[1], launcherInfo.spanX,
					launcherInfo.spanY, isWorkspaceLocked());

			addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
		}
		resetAddInfo();
	}
	
	private boolean lockState = false;
	private boolean isOurLockMessage = false;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				mUserPresent = false;
				mDragLayer.clearAllResizeFrames();
				updateRunning();
				dismissCustomMenu();
				// Reset AllApps to its initial state only if we are not in the
				// middle of
				// processing a multi-step drop
				if(mEditMode!=EditMode.NONE){
					//exitEditMode(false);
				}
				if (mAppsCustomizeTabHost != null
						&& mPendingAddInfo.container == ItemInfo.NO_ID) {
					mAppsCustomizeTabHost.reset();
					showWorkspace(false);
				}
				ALog.i("screen1","1:ACTION_SCREEN_OFF");
				//KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE); 	
				//if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
				/*
				if(isNeedUseBootAnimator()){
					handupViews(30);
					lockState = true;
					isOurLockMessage = false;
				}
				*/ 
				//}
				
				//vulcan added it in 2014-6-20
				//notify DynIconDriver that screen is off.
				DynIconDriver.setScreenSwitch(false);
			}else if(Intent.ACTION_SCREEN_ON.equals(action)){
				
				//M:shigq Fix bug #14337, #14536, #15346 on Android5.0 platform begin
				/*
				if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
					Log.d("SHIGQ_LAUNCHER", "Intent.ACTION_SCREEN_ON---------lockState = "+lockState+" isOurLockMessage = "+isOurLockMessage);
					
					KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
					Log.d("SHIGQ_LAUNCHER", "Intent.ACTION_SCREEN_ON---------mKeyguardManager.inKeyguardRestrictedInputMode() = "+
																							mKeyguardManager.inKeyguardRestrictedInputMode());
					if (lockState && !mKeyguardManager.inKeyguardRestrictedInputMode()) {
						Log.d("SHIGQ_LAUNCHER", "Intent.ACTION_SCREEN_ON-------------need to run startAnimationRunnable to show icon!!!!!!");
						lockState = false;
						mHandler.removeCallbacks(startAnimationRunnable);
						mHandler.post(startAnimationRunnable);
					}
				}
				*/
				//M:shigq Fix bug #14337, #14536, #15346 on Android5.0 platform end
						
				ALog.i("screen1","2:ACTION_SCREEN_ON");
				/*
				if(lockState==false){
					KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE); 	
					if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
						lockState = true;
						isOurLockMessage = false;
						handupViews(30);	
					}
				}
				*/
				//vulcan added it in 2014-6-20
				//notify DynIconDriver that screen is on.
				DynIconDriver.setScreenSwitch(true);
			}else if (Intent.ACTION_USER_PRESENT.equals(action)) {
				mUserPresent = true;
				updateRunning();
				ALog.i("screen1","3:ACTION_USER_PRESENT : isOurLockMessage="+isOurLockMessage);
				Log.d("SHIGQ_LAUNCHER", "Intent.ACTION_USER_PRESENT----------------isOurLockMessage = "+isOurLockMessage);
				/*
				if(!isOurLockMessage){
					ALog.i("start","1");				
					mHandler.removeCallbacks(startAnimationRunnable);
					mHandler.post(startAnimationRunnable);
					lockState = false;
					isOurLockMessage = false;
				}
				*/
			} else if (inputIsShowing.equals(action)) {
				inputState = true;
			} else if (inputIsHiden.equals(action)) {
				inputState = false;
			}
		}
	};
	
	private boolean isNeedUseBootAnimator(){
		return (mEditMode==EditMode.NONE||mEditMode==EditMode.DRAG) && isLauncherOnTop();
	}

	//AURORA_START:当其他应用处在顶端时，则不播放Launcher的加载动画
	//ht:2014-10-23
	private boolean isLauncherOnTop(){
		ActivityManager acMng = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tsklist = acMng.getRunningTasks(1);
		if(tsklist != null && !tsklist.isEmpty()){
			for(RunningTaskInfo t : tsklist){
				if("com.aurora.launcher".equals(t.topActivity.getPackageName())){
					return true;
				}
			}
		}
		return false;
	}
	//AURORA_START:当其他应用处在顶端时，则不播放Launcher的加载动画
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Listen for broadcasts related to user-presence
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(inputIsShowing);
		filter.addAction(inputIsHiden);
		filter.setPriority(Integer.MAX_VALUE);
		registerReceiver(mReceiver, filter);

		mAttached = true;
		mVisible = true;
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mVisible = false;

		if (mAttached) {
			unregisterReceiver(mReceiver);
			mAttached = false;
		}
		updateRunning();
	}

	public void onWindowVisibilityChanged(int visibility) {
		mVisible = visibility == View.VISIBLE;
		updateRunning();
		// The following code used to be in onResume, but it turns out onResume
		// is called when
		// you're in All Apps and click home to go to the workspace.
		// onWindowVisibilityChanged
		// is a more appropriate event to handle
		if (mVisible) {
			mAppsCustomizeTabHost.onWindowVisible();
			if (!mWorkspaceLoading) {
				final ViewTreeObserver observer = mWorkspace
						.getViewTreeObserver();
				// We want to let Launcher draw itself at least once before we
				// force it to build
				// layers on all the workspace pages, so that transitioning to
				// Launcher from other
				// apps is nice and speedy. Usually the first call to preDraw
				// doesn't correspond to
				// a true draw so we wait until the second preDraw call to be
				// safe
				observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
					public boolean onPreDraw() {
						// We delay the layer building a bit in order to give
						// other message processing a time to run. In particular
						// this avoids a delay in hiding the IME if it was
						// currently shown, because doing that may involve
						// some communication back with the app.
						mWorkspace.postDelayed(mBuildLayersRunnable, 500);

						observer.removeOnPreDrawListener(this);
						return true;
					}
				});
			}
			// When Launcher comes back to foreground, a different Activity
			// might be responsible for
			// the app market intent, so refresh the icon
			updateAppMarketIcon();
			clearTypedText();
		}
	}

	private void sendAdvanceMessage(long delay) {
		mHandler.removeMessages(ADVANCE_MSG);
		Message msg = mHandler.obtainMessage(ADVANCE_MSG);
		mHandler.sendMessageDelayed(msg, delay);
		mAutoAdvanceSentTime = System.currentTimeMillis();
	}

	private void updateRunning() {
		boolean autoAdvanceRunning = mVisible && mUserPresent
				&& !mWidgetsToAdvance.isEmpty();
		if (autoAdvanceRunning != mAutoAdvanceRunning) {
			mAutoAdvanceRunning = autoAdvanceRunning;
			if (autoAdvanceRunning) {
				long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval
						: mAutoAdvanceTimeLeft;
				sendAdvanceMessage(delay);
			} else {
				if (!mWidgetsToAdvance.isEmpty()) {
					mAutoAdvanceTimeLeft = Math
							.max(0,
									mAdvanceInterval
											- (System.currentTimeMillis() - mAutoAdvanceSentTime));
				}
				mHandler.removeMessages(ADVANCE_MSG);
				mHandler.removeMessages(0); // Remove messages sent using
											// postDelayed()
			}
		}
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == ADVANCE_MSG) {
				int i = 0;
				for (View key : mWidgetsToAdvance.keySet()) {
					final View v = key
							.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
					final int delay = mAdvanceStagger * i;
					if (v instanceof Advanceable) {
						postDelayed(new Runnable() {
							public void run() {
								((Advanceable) v).advance();
							}
						}, delay);
					}
					i++;
				}
				sendAdvanceMessage(mAdvanceInterval);
			} else if (msg.what == 0x50) {
				handleQuickIndexLocation(msg.arg1, (View) msg.obj);
			} else if (msg.what == 0x51) {
				View v = (View) msg.obj;
				ItemInfo info = (ItemInfo) v.getTag();
				FolderIcon mFolderIcon = getWorkspace().getFolderByContainer(
						info.container);
				if (mFolderIcon != null) {
					handleFolderClick(mFolderIcon);
					Folder mFolder = mFolderIcon.getFolder();
					if (mFolder != null) {
						FolderWorkspace mFolderWorkspce = mFolder
								.getFolderWorkspace();
						if (mFolderWorkspce != null) {
							mFolderWorkspce.findItemInFolderPage(v);
						}
					}
				}
			}else if(msg.what == 0x52){
				Bitmap[] bitmapArray = (Bitmap[])msg.obj;
				LoadingQuickBlurBitmap(true,bitmapArray);
				//getDragController().enterTransModeTo(TransModeEnum.APPS_INDEX, msg.arg1);
			}else if(msg.what == ICON_RESHOW){
				//ht 2014-09-30
				if(mWorkspace != null){
					mWorkspace.snapToPage(mWorkspace.getChildCount() - 1);
				}
			}
		}
	};

	void addWidgetToAutoAdvanceIfNeeded(View hostView,
			AppWidgetProviderInfo appWidgetInfo) {
		if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1)
			return;
		View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
		if (v instanceof Advanceable) {
			mWidgetsToAdvance.put(hostView, appWidgetInfo);
			((Advanceable) v).fyiWillBeAdvancedByHostKThx();
			updateRunning();
		}
	}

	void removeWidgetToAutoAdvance(View hostView) {
		if (mWidgetsToAdvance.containsKey(hostView)) {
			mWidgetsToAdvance.remove(hostView);
			updateRunning();
		}
	}

	public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
		removeWidgetToAutoAdvance(launcherInfo.hostView);
		launcherInfo.hostView = null;
	}

	void showOutOfSpaceMessage(boolean isHotseatLayout) {
		int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.no_space);
		Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
	}

	public LauncherAppWidgetHost getAppWidgetHost() {
		return mAppWidgetHost;
	}

	public LauncherModel getModel() {
		return mModel;
	}

	void closeSystemDialogs() {
		getWindow().closeAllPanels();
		
		if (mDismissFolderDialog != null) {
			mDismissFolderDialog.dismiss();
			mDismissFolderDialog = null;
		}

		// Whatever we were doing is hereby canceled.
		mWaitingForResult = false;
	}
	
	private Dialog mDismissFolderDialog;
	public void setDismissFolderDialog(Dialog dialog) {
		mDismissFolderDialog = dialog;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		final int intentFlags=intent.getFlags();
		//For voice test
		if (getTransModeEnum() == TransModeEnum.VOICE) {
			sendBroadCastToSlave(ToDismissMenuByHomeKey, "homekeypressed", true);
		}
		//if pending intent get boolean values return true means it need classify icons 
		if(intent.getBooleanExtra("EXTRA_CLASSIFY_ICONS", false)){
			startSmartClassify();
		}
		
		super.onNewIntent(intent);
			/**check intent extra value*/
			startCheckVoicePrintStatus(intent);
			//For voice test
			// Close the menu
			if (Intent.ACTION_MAIN.equals(intent.getAction())) {
				if(mEditMode==EditMode.CLASSIFICATE_ICONS && !bClassifyCompleted && !getWorkspace().getFolderOpenState()){
					showClassifyResultAlertDialog();
					return;
				}
				
				// also will cancel mWaitingForResult.
				isHomeKeyPress = true;
				closeSystemDialogs();
				dismissCustomMenu();
				final boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				Runnable processIntent = new Runnable() {
					public void run() {
						if (mWorkspace == null) {
							// Can be cases where mWorkspace is null, this prevents
							// a NPE
							return;
						}
						Folder openFolder = mWorkspace.getOpenFolder();
						// In all these cases, only animate if we're already on home
						if((mEditMode==EditMode.APPWIDGET_ADD || mEditMode == EditMode.FOLDER_IMPORT) &&mExitAnimatorFinish){
							LauncherApplication.logVulcan.print("onNewIntent: to call exitEditMode(true)!");
							showFolderIcon();
							exitEditMode(true);
							if(closeFolderAnimator!=null&&(closeFolderAnimator.isRunning()||closeFolderAnimator.isStarted())){
								closeFolderAnimator.end();
							}
						}

						if(mEditMode == EditMode.NONE && getTransModeEnum() != TransMode.NONE  && !getAnimationFlag()){
							//TODO modify the code that fits onbackpress 
							// 270532608 The current interface is launcher flag  
							//274726912  The current interface is not launcher flag add by xiexiujie 9.16
							if(intentFlags==270532608){
								toggleTransModeQuitAction();
								
							}
							
				        }else if(mEditMode == EditMode.QUICK_INDEX){
							if (appIndexSearchAnimator != null) {
								appIndexSearchAnimator.cancel();
								appIndexSearchAnimator = null;
				            }
							Log.e("linp", "EditMode.QUICK_INDEX");
							enterAppIndexSearchAnimator(true, false, 60, null,APP_RETRIEVAL_HOME);
							
				        }
						if(letterSideBar.getAlpha() == 0f || letterSideBar.getVisibility() == View.GONE){
							letterSideBar.setAlpha(1.0f);
							letterSideBar.setVisibility(View.VISIBLE);
						}
						mWorkspace.exitWidgetResizeMode();
						if (!isAlignIconDialogOpen && alreadyOnHome && mState == State.WORKSPACE
								&& !mWorkspace.isTouchActive()
								&& openFolder == null&&mState!=State.SCALE_WORKSPACE&&mExitAnimatorFinish
								&&mEditMode==EditMode.NONE && getTransModeEnum() == TransMode.NONE) {
							mWorkspace.moveToDefaultScreen(true);		
						}
						isAlignIconDialogOpen = false;
						// 270532608 The current interface is launcher flag  
						//274726912  The current interface is not launcher flag add by xiexiujie 9.16
						if(intentFlags==270532608){
							closeFolder();
							
						}																
						//exitSpringLoadedDragMode();
						// If we are already on home, then just animate back to the
						// workspace,
						// otherwise, just wait until onResume to set the state back
						// to Workspace
						if (alreadyOnHome&&mExitAnimatorFinish) {
							showWorkspace(true);
						} else {
							mOnResumeState = State.WORKSPACE;
						}
						final View v = getWindow().peekDecorView();
						if (v != null && v.getWindowToken() != null) {
							InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
						}	
					}
				};
				/**Hazel start to add to fix bug #6117*/
				if(leftarrowhead.getVisibility() == View.VISIBLE || rightarrowhead.getVisibility() == View.VISIBLE){
					leftarrowhead.setVisibility(View.GONE);
					rightarrowhead.setVisibility(View.GONE);
				}
				

				if (alreadyOnHome && !mWorkspace.hasWindowFocus()) {
					// Delay processing of the intent to allow the status bar
					// animation to finish
					// first in order to avoid janky animations.
					mWorkspace.postDelayed(processIntent, 350);
				} else {
					// Process the intent immediately.
					processIntent.run();
				}
			}
		

	}

	@Override
	public void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		for (int page : mSynchronouslyBoundPages) {
			mWorkspace.restoreInstanceStateForChild(page);
		}
	}
	
	private static final String SDCARD_MOUNTED_STATE = "mounted";
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getNextPage());
		super.onSaveInstanceState(outState);

		if (mModel != null) {
			outState.putBoolean(SDCARD_MOUNTED_STATE, mModel.ismSdcardMounted());
		}

		outState.putInt(RUNTIME_STATE, mState.ordinal());
		// We close any open folder since it will not be re-opened, and we need
		// to make sure
		// this state is reflected.
		if (mPendingAddInfo.container != ItemInfo.NO_ID
				&& mPendingAddInfo.screen > -1 && mWaitingForResult) {
			outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER,
					mPendingAddInfo.container);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN,
					mPendingAddInfo.screen);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X,
					mPendingAddInfo.cellX);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y,
					mPendingAddInfo.cellY);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X,
					mPendingAddInfo.spanX);
			outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y,
					mPendingAddInfo.spanY);
			outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO,
					mPendingAddWidgetInfo);
		}

		if (mFolderInfo != null && mWaitingForResult) {
			outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
			outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID,
					mFolderInfo.id);
		}

		// Save the current AppsCustomize tab
		if (mAppsCustomizeTabHost != null) {
			String currentTabTag = mAppsCustomizeTabHost.getCurrentTabTag();
			if (currentTabTag != null) {
				outState.putString("apps_customize_currentTab", currentTabTag);
			}
			int currentIndex = mAppsCustomizeContent
					.getSaveInstanceStateIndex();
			outState.putInt("apps_customize_currentIndex", currentIndex);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Remove all pending runnables
		mHandler.removeMessages(ADVANCE_MSG);
		mHandler.removeMessages(0);
		mWorkspace.removeCallbacks(mBuildLayersRunnable);

		// Stop callbacks from LauncherModel
		LauncherApplication app = ((LauncherApplication) getApplication());
		mModel.stopLoader();
		app.setLauncher(null);

		try {
			mAppWidgetHost.stopListening();
		} catch (NullPointerException ex) {
			Log.w(TAG,
					"problem while stopping AppWidgetHost during Launcher destruction",
					ex);
		}
		mAppWidgetHost = null;

		mWidgetsToAdvance.clear();

		TextKeyListener.getInstance().release();

		// Disconnect any of the callbacks and drawables associated with
		// ItemInfos on the workspace
		// to prevent leaking Launcher activities on orientation change.
		if (mModel != null) {
			LauncherApplication.logVulcan.print("in onDestroy setmSdcardMounted to " + false);
			mModel.setmSdcardMounted(false);
			mModel.unbindItemInfosAndClearQueuedBindRunnables();
		}

		getContentResolver().unregisterContentObserver(mWidgetObserver);
		unregisterReceiver(mCloseSystemDialogsReceiver);
		
		//For voice test
		unregisterReceiver(mSlaveToMasterReceiver);
		//For voice test
		unregisterReceiver(mIntentDateReceiver);
		mDragLayer.clearAllResizeFrames();
		((ViewGroup) mWorkspace.getParent()).removeAllViews();
		mWorkspace.removeAllViews();
		mWorkspace = null;
		mDragController = null;
		unregisterReceiver(startAnim);
		LauncherAnimUtils.onDestroyActivity();
		// 获取电话服务
		//TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		// 反注册对PhoneStateListener
		//manager.listen(mMyPhoneStateListener,PhoneStateListener.LISTEN_NONE);
		
		//added by vulcan in 2014-5-17
		//LauncherApplication.logVulcan.close();
		
		LauncherApplication.logVulcan.print("onDestroy is called!");
		Log.d("vulcan-finish","onDestroy is called!");
	}

	public DragController getDragController() {
		return mDragController;
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		if (requestCode >= 0)
			mWaitingForResult = true;
		super.startActivityForResult(intent, requestCode);
	}

	/**
	 * Indicates that we want global search for this activity by setting the
	 * globalSearch argument for {@link #startSearch} to true.
	 */
	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		showWorkspace(true);
		if (initialQuery == null) {
			// Use any text typed in the launcher as the initial query
			initialQuery = getTypedText();
		}
		if (appSearchData == null) {
			appSearchData = new Bundle();
		}
		Rect sourceBounds = new Rect();
		if (mSearchDropTargetBar != null) {
			sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
		}
		startGlobalSearch(initialQuery, selectInitialQuery, appSearchData,
				sourceBounds);
	}

	/**
	 * Starts the global search activity. This code is a copied from
	 * SearchManager
	 */
	public void startGlobalSearch(String initialQuery,
			boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		ComponentName globalSearchActivity = searchManager
				.getGlobalSearchActivity();
		if (globalSearchActivity == null) {
			Log.w(TAG, "No global search activity found.");
			return;
		}
		Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(globalSearchActivity);
		// Make sure that we have a Bundle to put source in
		if (appSearchData == null) {
			appSearchData = new Bundle();
		} else {
			appSearchData = new Bundle(appSearchData);
		}
		// Set source to package name of app that starts global search, if not
		// set already.
		if (!appSearchData.containsKey("source")) {
			appSearchData.putString("source", getPackageName());
		}
		intent.putExtra(SearchManager.APP_DATA, appSearchData);
		if (!TextUtils.isEmpty(initialQuery)) {
			intent.putExtra(SearchManager.QUERY, initialQuery);
		}
		if (selectInitialQuery) {
			intent.putExtra(SearchManager.EXTRA_SELECT_QUERY,
					selectInitialQuery);
		}
		intent.setSourceBounds(sourceBounds);
		LauncherApplication.logVulcan.print("startGlobalSearch: setSourceBounds intent = " + intent.toUri(0));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			Log.e(TAG, "Global search activity not found: "
					+ globalSearchActivity);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ALog.i("menu","onCreateOptionsMenu");
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	} 

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		/*super.onPrepareOptionsMenu(menu);
		if (mAppsCustomizeTabHost.isTransitioning() 
				|| getWorkspace().getFolderOpenState()
				|| getEditMode() == EditMode.FOLDER_IMPORT) {
			return false;
		}
		boolean allAppsVisible = (mAppsCustomizeTabHost.getVisibility() == View.VISIBLE);
		menu.setGroupVisible(MENU_GROUP_WALLPAPER, !allAppsVisible);*/
		ALog.i("menu","onPrepareOptionsMenu");
		return true;
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		ALog.i("menu","onMenuOpened : canOpenMenu = "+canOpenMenu());
		if(canOpenMenu()){
			if (mCustomMenuProxy.isShowing()) {
				dismissCustomMenu();
			} else {
				mCustomMenuProxy.showAtLocation(findViewById(R.id.drag_layer),
						Gravity.BOTTOM, 0, 0);
				//set Navigation bar
				setNavigationbarBackgroundColor();
			}
		}
		return false;// 返回为true 则显示系统menu
	}
	
	/**invoke by DragController */
	public boolean isCustomMenuProxyShowing(){
		return mCustomMenuProxy.isShowing();
	}
	
	private boolean canOpenMenu(){
		View cling = findViewById(R.id.workspace_cling);
		boolean canOpenCuatomMenu = mEditMode == EditMode.NONE
				&& getTransModeEnum() == TransModeEnum.NONE
				&& getWorkspace().getOpenFolder() == null
				&& !isWorkspaceLocked()
				&& (cling == null? true:(cling.getVisibility()==View.VISIBLE? false:true));
		ALog.i("menu","mEditMode = "+mEditMode+"  ,getTransModeEnum() = "+getTransModeEnum()+"  getOpenFolder()="+getWorkspace().getOpenFolder()+"  !isWorkspaceLocked"+!isWorkspaceLocked());
		return canOpenCuatomMenu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ALog.i("menu","onOptionsItemSelected");
		switch (item.getItemId()) {
		case MENU_WALLPAPER_SETTINGS:
			startWallpaper();
			return true;
		case MENU_WIDGET:
			enterEditMode(EditMode.APPWIDGET_ADD,true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, true);
		return true;
	}

	public boolean isWorkspaceLocked() {
		return mWorkspaceLoading || mWaitingForResult;
	}

	private void resetAddInfo() {
		mPendingAddInfo.container = ItemInfo.NO_ID;
		mPendingAddInfo.screen = -1;
		mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
		mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
		mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
		mPendingAddInfo.dropPos = null;
	}

	void addAppWidgetImpl(final int appWidgetId, ItemInfo info,
			AppWidgetHostView boundWidget, AppWidgetProviderInfo appWidgetInfo) {
		if (appWidgetInfo.configure != null) {
			mPendingAddWidgetInfo = appWidgetInfo;

			// Launch over to configure widget, if needed
			Intent intent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidgetInfo.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
		} else {
			// Otherwise just add it
			completeAddAppWidget(appWidgetId, info.container, info.screen,
					boundWidget, appWidgetInfo);
			// Exit spring loaded mode if necessary after adding the widget
			exitSpringLoadedDragModeDelayed(true, false, null);
		}
	}

	/**
	 * Process a shortcut drop.
	 * 
	 * @param componentName
	 *            The name of the component
	 * @param screen
	 *            The screen where it should be added
	 * @param cell
	 *            The cell it should be added to, optional
	 * @param position
	 *            The location on the screen where it was dropped, optional
	 */
	void processShortcutFromDrop(ComponentName componentName, long container,
			int screen, int[] cell, int[] loc) {
		resetAddInfo();
		mPendingAddInfo.container = container;
		mPendingAddInfo.screen = screen;
		mPendingAddInfo.dropPos = loc;

		if (cell != null) {
			mPendingAddInfo.cellX = cell[0];
			mPendingAddInfo.cellY = cell[1];
		}
		mFromAuroraWidget = true;
		Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		createShortcutIntent.setComponent(componentName);
		processShortcut(createShortcutIntent);
	}

	/**
	 * Process a widget drop.
	 * 
	 * @param info
	 *            The PendingAppWidgetInfo of the widget being added.
	 * @param screen
	 *            The screen where it should be added
	 * @param cell
	 *            The cell it should be added to, optional
	 * @param position
	 *            The location on the screen where it was dropped, optional
	 */
	void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container,
			int screen, int[] cell, int[] span, int[] loc) {
		resetAddInfo();
		mPendingAddInfo.container = info.container = container;
		mPendingAddInfo.screen = info.screen = screen;
		mPendingAddInfo.dropPos = loc;
		mPendingAddInfo.minSpanX = info.minSpanX;
		mPendingAddInfo.minSpanY = info.minSpanY;

		if (cell != null) {
			mPendingAddInfo.cellX = cell[0];
			mPendingAddInfo.cellY = cell[1];
		}
		if (span != null) {
			mPendingAddInfo.spanX = span[0];
			mPendingAddInfo.spanY = span[1];
		}

		AppWidgetHostView hostView = info.boundWidget;
		int appWidgetId;
		if (hostView != null) {
			appWidgetId = hostView.getAppWidgetId();
			addAppWidgetImpl(appWidgetId, info, hostView, info.info);
		} else {
			// In this case, we either need to start an activity to get
			// permission to bind
			// the widget, or we need to start an activity to configure the
			// widget, or both.
			appWidgetId = getAppWidgetHost().allocateAppWidgetId();
			Bundle options = info.bindOptions;

			boolean success = false;
			if (options != null) {
				success = mAppWidgetManager.bindAppWidgetIdIfAllowed(
						appWidgetId, info.componentName, options);
			} else {
				success = mAppWidgetManager.bindAppWidgetIdIfAllowed(
						appWidgetId, info.componentName);
			}
			if (success) {
				addAppWidgetImpl(appWidgetId, info, null, info.info);
			} else {
				mPendingAddWidgetInfo = info.info;
				Intent intent = new Intent(
						AppWidgetManager.ACTION_APPWIDGET_BIND);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
						info.componentName);
				// TODO: we need to make sure that this accounts for the options
				// bundle.
				// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS,
				// options);
				startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
			}
		}
	}

	void processShortcut(Intent intent) {
		// Handle case where user selected "Applications"
		String applicationName = getResources().getString(
				R.string.group_applications);
		String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

		if (applicationName != null && applicationName.equals(shortcutName)) {
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
			pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
			pickIntent.putExtra(Intent.EXTRA_TITLE,
					getText(R.string.title_select_application));
			startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
		} else {
			startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
		}
	}

	void processWallpaper(Intent intent) {
		startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
	}

	FolderIcon addFolder(CellLayout layout, long container, final int screen,
			int cellX, int cellY,String folderName) {
		final FolderInfo folderInfo = new FolderInfo();
		folderInfo.title = folderName;

		// Update the model
		LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container,
				screen, cellX, cellY, false);
		sFolders.put(folderInfo.id, folderInfo);

		// Create the view
		FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
				layout, folderInfo, mIconCache);
		mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1,
				1, isWorkspaceLocked());
		return newFolder;
	}

	void removeFolder(FolderInfo folder) {
		sFolders.remove(folder.id);
	}

	private void startWallpaper() {
		showWorkspace(true);
		try {
			final Intent auroraWallpaperIntent = new Intent("aurora.intent.action.wallpaper_set");
			//auroraWallpaperIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			//startActivityForResult(auroraWallpaperIntent, REQUEST_PICK_WALLPAPER);
			startActivity(auroraWallpaperIntent);
		} catch (Exception e) {
			final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
			Intent chooser = Intent.createChooser(pickWallpaper,
					getText(R.string.chooser_wallpaper));
			startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
		}
	}

	/**
	 * Registers various content observers. The current implementation registers
	 * only a favorites observer to keep track of the favorites applications.
	 */
	private void registerContentObservers() {
		ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(
				LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true,
				mWidgetObserver);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	
		ALog.i("menu","event.getKeyCode() = "+event.getKeyCode());
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			//For voice test
			if (getTransModeEnum() == TransModeEnum.VOICE) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
					mShadowImageView.setVisibility(View.GONE);
					if (mShowShadowImageDelayRunnable != null) {
						mHandler.removeCallbacks(mShowShadowImageDelayRunnable);
					}
					if(getInputMethodState()){
						mLayerMask.setBackgroundColor(getResources().getColor(R.color.workspace_layer_mask));
						return true;
					}else{  
						setNavigationbarBackgroundColor();
						mLayerMask.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_layer));
					}
		     		return mVoiceContentView.dispatchKeyEvent(event);
				}
//				if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN | event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
//					if (mEnterVoiceFullScreenDelayRunnable != null) {
//						mHandler.removeCallbacks(mEnterVoiceFullScreenDelayRunnable);
//					}
//					enterFullScreenInVoiceMode(100);
//				}
			}else if(getTransModeEnum() == TransMode.APPS_INDEX){
				getDragController().scheduleTimerTask();
			}
			//For voice test
			
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_HOME:
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (isPropertyEnabled(DUMP_STATE_PROPERTY)) {
					dumpState();
					return true;
				}
				break;
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {
		ALog.i("menu","onBackPressed");
		if (isAllAppsVisible()) {
			showWorkspace(true);
		} else if (mWorkspace.getOpenFolder() != null) {
			Folder openFolder = mWorkspace.getOpenFolder();
			if (openFolder.isEditingName()) {
				openFolder.dismissEditingName();
			} else {
				closeFolder();
			}
		}else if(mEditMode ==  EditMode.FOLDER_IMPORT){
			LauncherApplication.logVulcan.print("onBackPressed: to call exitEditMode(true)!,mEditMode = FOLDER_IMPORT");
        	showFolderIcon();
			mOpenFolderIcon = true;
			exitEditMode(true);
		}else if(mEditMode==EditMode.APPWIDGET_ADD){
			LauncherApplication.logVulcan.print("onBackPressed: to call exitEditMode(true)!,mEditMode = APPWIDGET_ADD");
        	exitEditMode(true);
        } else if(mEditMode == EditMode.QUICK_INDEX){
        	if(isEnterFilterMode){
        		//exitFilterSearchIndexMode();
        		
        		// 添加该段代码的目的是，mQuickIndexSearchEditText被gone以后，listview进行了上移
        		emptySearchEditText();
        		if (!mAppSearchAnimatorGoing) {
        			mQuickIndexOutline.updateMargin(false);
        			enterAppIndexSearchAnimator(true, false, 350, null,APP_RETRIEVAL_BACK);
       		}
        	}
        } 
        else if(mEditMode == EditMode.NONE && getTransModeEnum() != TransMode.NONE){
			if (isEnterFilterMode) {
				exitFilterSearchIndexMode();
			} else {
				if (!mAnimationIsGoing) {
					Log.e("linp", "back press and will invoke exitTransMode function");
					getDragController().setHomeOrBackPress(true);
					mDragController.exitTransMode();
					if(letterSideBar.getAlpha() == 0f || letterSideBar.getVisibility() == View.GONE)
						letterSideBar.setAlpha(1.0f);
				}
			}
        }else if(mEditMode == EditMode.CLASSIFICATE_ICONS && !bClassifyCompleted && !getWorkspace().getFolderOpenState() ){
        	showClassifyResultAlertDialog();
        }
		else {
			mWorkspace.exitWidgetResizeMode();
			// Back button is a no-op here, but give at least some feedback for
			// the button press
			mWorkspace.showOutlinesTemporarily();
		}
		
		/**Hazel start to add to fix bug #6117*/
		if(leftarrowhead.getVisibility() == View.VISIBLE || rightarrowhead.getVisibility() == View.VISIBLE){
			leftarrowhead.setVisibility(View.GONE);
			rightarrowhead.setVisibility(View.GONE);
		}
		
		
	
	}

	/**
	 * Re-listen when widgets are reset.
	 */
	private void onAppWidgetReset() {
		if (mAppWidgetHost != null) {
			mAppWidgetHost.startListening();
		}
	}

	private void handleShortcutClick(BubbleTextView v, ShortcutInfo info){
		if(!v.isChecked()){
			mCheckedShortcutInfos.add(info);
		} else {
			mCheckedShortcutInfos.remove(info);
		}
	}
	void setImportFolderText(boolean zero){
		int v = 0;
		if( !zero ){
			v = mCheckedShortcutInfos.size();
			Iterator<FolderInfo> iterator = mCheckedFolderInfos.iterator();
			while (iterator.hasNext()) {
				FolderInfo info = iterator.next();
				v += info.checkInfos.size();
			}
			mRemainImportItemsCount = mMaxImportItemsCount - v;
		} else {
			mRemainImportItemsCount = 0;
		}
		mFolderImportButton.setText(getString(R.string.add_into_folder, v));	
		if(v > 0){
			mFolderImportButton.setTextColor(getResources().getColor(R.color.folder_import_txt_normal_color));
			mFolderImportButton.getBackground().setAlpha(255);
			mFolderImportButton.setEnabled(true);
		} else {
			mFolderImportButton.getBackground().setAlpha(127);
			mFolderImportButton.setTextColor(getResources().getColor(R.color.folder_import_txt_invalid_color));
			mFolderImportButton.setEnabled(false);
		}
	}	
	/**
	 * Launches the intent referred by the clicked shortcut.
	 * 
	 * @param v
	 *            The view representing the clicked shortcut.
	 */
	private int mOnClickCount = 0;
	public void onClick(View v) {
		// Make sure that rogue clicks don't get through while allapps is
		// launching, or after the
		// view has detached (it's possible for this to happen if the view is
		// removed mid touch).
		//Launcher debug mode will be Opened or closed before clicking the empty place 20 times 
		mOnClickCount++;
		if (mOnClickCount == 20) {
			clearOnClickCount();
			ALog.debug();
		}
		if (getAnimationFlag()) {
			ALog.i(TAG,"onClick  return 1");
			return;
		}
		if (getEditMode() == EditMode.APPWIDGET_ADD ) {
			ALog.i(TAG,"onClick  return 2");
			return;
		}
		if (v.getWindowToken() == null) {
			ALog.i(TAG,"onClick  return 3");
			return;
		}
		Object tag = v.getTag();		
		if (!mWorkspace.isFinishedSwitchingState()) {
			ALog.i(TAG,"onClick  return 4");
			return;
		}
		if (tag instanceof ShortcutInfo) {
			clearOnClickCount();
			if(getEditMode() == EditMode.CLASSIFICATE_ICONS)return;
			
			if(mEditMode == EditMode.FOLDER_IMPORT){
				if(v instanceof BubbleTextView){
					BubbleTextView bView = (BubbleTextView) v;
					if(bView.isChecked()){
						mCheckedShortcutInfos.remove((ShortcutInfo)tag);
						mCheckedBubbleTextViews.remove(bView);
					} else {
						if(getRemainImportItemsCount() <= 0) {
							Toast.makeText(getApplicationContext(), R.string.folder_import_full_toast,
									Toast.LENGTH_SHORT).show();
							return;
						}
						mCheckedShortcutInfos.add((ShortcutInfo)tag);
						mCheckedBubbleTextViews.add(bView);
					}
					bView.setChecked();
					setImportFolderText(false);
				}
			}else{
				final Intent intent = ((ShortcutInfo) tag).intent;
				int[] pos = new int[2];
				v.getLocationOnScreen(pos);
				((ShortcutInfo) tag).newFlag = false;
				intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
						+ v.getWidth(), pos[1] + v.getHeight()));
				LauncherApplication.logVulcan.print("onClick: setSourceBounds intent = " + intent.toUri(0));
				Log.d("vulcan-setup","onClick: intent: " + intent);
	
				boolean success = startActivitySafely(v, intent, tag);
				if (success && v instanceof BubbleTextView) {
					mWaitingForResume = (BubbleTextView) v;
					mWaitingForResume.setNewApp(false);
					mWaitingForResume.setStayPressed(true);
				}
			}
		} else if (tag instanceof FolderInfo) {
			if(isRequestAniRunning()) return;
			clearOnClickCount();
			if(mEditMode == EditMode.FOLDER_IMPORT){
				if (tag == mEditFolderInfo) return;
				if (!mCheckedFolderInfos.contains(tag) && getRemainImportItemsCount() <= 0) {
					Toast.makeText(getApplicationContext(), R.string.folder_import_full_toast,
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
			if (v instanceof FolderIcon) {
				FolderIcon fi = (FolderIcon) v;
				handleFolderClick(fi);
			}
		} else if (v == mAllAppsButton) {
			clearOnClickCount();
			if (isAllAppsVisible()) {
				showWorkspace(true);
			} else {
				onClickAllAppsButton(v);
			}
		}
        else if(v == mFolderImportButton){
        	clearOnClickCount();
        	closeFolder();
        	int i=0;
        	for(ShortcutInfo info :mCheckedShortcutInfos){
        		View view=mWorkspace.getChildAt(info.screen);
        		if(view instanceof CellLayout){
        			mCheckedBubbleTextViews.get(i).setChecked(false);
        			((CellLayout) view).removeView(mCheckedBubbleTextViews.get(i));
        		}
        		i++;
        	}
        	
        	Iterator<FolderInfo> iterator = mCheckedFolderInfos.iterator();
        	while(iterator.hasNext()){
        		FolderInfo info = iterator.next();
        		info.multiRemove();
        		mEditFolderInfo.multiAdd(info.checkInfos);
        	}
        	mEditFolderInfo.multiAdd(mCheckedShortcutInfos);
			mOpenFolderIcon = true;
			LauncherApplication.logVulcan.print("onClick: to call exitEditMode(false)!");
			exitEditMode(false);
			showFolderIcon();
		}
		/**add for classify result button action save or restore*/
		switch (v.getId()) {
		case R.id.btn_ok:
			restoreBackupAfterClassifyResult();
		break;
  
		case R.id.btn_cancel:
			acceptClassifyResult();
		break;
		}
	}
	
	Runnable restoreBackupAfterClassifyReusltRunnable =new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (getWorkspace().getCurrentPage() > 0) {
				getWorkspace().snapToPage(0);
			}
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					startRequestAni();
					getWorkspace().setCurrentPagesAlpha(0.2f);
					mFolderImportHint.setText(R.string.classify_app_restoring);
					bPullbackAction = true;
					btnCalssifyCancel.setClickable(false);
					btnClassifyOk.setTextColor(getResources().getColor(
							R.color.folder_import_txt_invalid_color));
					btnCalssifyCancel.setTextColor(getResources().getColor(
							R.color.folder_import_txt_invalid_color));
					mModel.fallbackToOriginal();
				}
			}, getWorkspace().mScroller.getDuration());
		
		}
		
	};
	
	private void clearOnClickCount(){
		mOnClickCount = 0;
	}
	
	void clearShortcutToFolder(){
		boolean hasFolderNoClosed = false;
		if(mCheckedBubbleTextViews != null) {
			for(int i=0;i<mCheckedBubbleTextViews.size();i++){
        		mCheckedBubbleTextViews.get(i).setChecked(false);
        	}
			mCheckedBubbleTextViews.clear();
		}
		if(mCheckedShortcutInfos != null) mCheckedShortcutInfos.clear();
		if(mCheckedFolderInfos != null){
			Iterator<FolderInfo> iter = mCheckedFolderInfos.iterator();
			while(iter.hasNext()){
				FolderInfo info = (FolderInfo)iter.next();
				info.clearChecked();
				if(info.opened) hasFolderNoClosed = true;
			}
			mCheckedFolderInfos.clear();
		}
		setImportFolderText(true);
		mEditFolderInfo = null;
		if(hasFolderNoClosed) closeFolder();
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		// this is an intercepted event being forwarded from mWorkspace;
		// clicking anywhere on the workspace causes the customization drawer to
		// slide down
		showWorkspace(true);
		return false;
	}

	/**
	 * Event handler for the search button
	 * 
	 * @param v
	 *            The view that was clicked.
	 */
	public void onClickSearchButton(View v) {
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		onSearchRequested();
	}

	/**
	 * Event handler for the voice button
	 * 
	 * @param v
	 *            The view that was clicked.
	 */
	public void onClickVoiceButton(View v) {
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		try {
			final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			ComponentName activityName = searchManager
					.getGlobalSearchActivity();
			Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (activityName != null) {
				intent.setPackage(activityName.getPackageName());
			}
			startActivity(null, intent, "onClickVoiceButton");
		} catch (ActivityNotFoundException e) {
			Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivitySafely(null, intent, "onClickVoiceButton");
		}
	}

	/**
	 * Event handler for the "grid" button that appears on the home screen,
	 * which enters all apps mode.
	 * 
	 * @param v
	 *            The view that was clicked.
	 */
	public void onClickAllAppsButton(View v) {
		showAllApps(true);
	}

	public void onTouchDownAllAppsButton(View v) {
		// Provide the same haptic feedback that the system offers for virtual
		// keys.
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	public void onClickAppMarketButton(View v) {
		if (mAppMarketIntent != null) {
			startActivitySafely(v, mAppMarketIntent, "app market");
		} else {
			Log.e(TAG, "Invalid app market intent.");
		}
	}

	void startApplicationDetailsActivity(ComponentName componentName) {
		String packageName = componentName.getPackageName();
		Intent intent = new Intent(
				Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
						"package", packageName, null));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivitySafely(null, intent, "startApplicationDetailsActivity");
	}

	void startApplicationUninstallActivity(ApplicationInfo appInfo) {
		if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
			// System applications cannot be installed. For now, show a toast
			// explaining that.
			// We may give them the option of disabling apps this way.
			int messageId = R.string.uninstall_system_app_text;
			Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
		} else {
			String packageName = appInfo.componentName.getPackageName();
			String className = appInfo.componentName.getClassName();
			Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts(
					"package", packageName, className));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            mIsUninstallShow = true;
			startActivity(intent);
		}
	}

	boolean startActivity(View v, Intent intent, Object tag) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			// Only launch using the new animation if the shortcut has not opted
			// out (this is a
			// private contract between launcher and may be ignored in the
			// future).
			boolean useLaunchAnimation = (v != null)
					&& !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
			if (useLaunchAnimation) {
				ActivityOptions opts = true?ActivityOptions.makeCustomAnimation(this, R.anim.task_open_enter, R.anim.no_anim) :
                    ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

				startActivity(intent, opts.toBundle());
			} else {
				startActivity(intent);
			}
			return true;
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG,"Launcher does not have the permission to launch "+ intent
							+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
							+ "or use the exported attribute for this activity. "
							+ "tag=" + tag + " intent=" + intent, e);
		}
		return false;
	}

	boolean startActivitySafely(View v, Intent intent, Object tag) {
		boolean success = false;
		try {
			success = startActivity(v, intent, tag);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
		}
		return success;
	}

	void startActivityForResultSafely(Intent intent, int requestCode) {
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG,"Launcher does not have the permission to launch "+ intent
							+ ". Make sure to create a MAIN intent-filter for the corresponding activity "
							+ "or use the exported attribute for this activity.",e);
		}
	}

	private void handleFolderClick(FolderIcon folderIcon) {
		final FolderInfo info = folderIcon.getFolderInfo();
		Folder openFolder = mWorkspace.getFolderForTag(info);

		// If the folder info reports that the associated folder is open, then
		// verify that
		// it is actually opened. There have been a few instances where this
		// gets out of sync.
		if (info.opened && openFolder == null) {
			Log.d(TAG,"Folder info marked as open, but associated folder is not open. Screen: "
							+ info.screen + " (" + info.cellX + ", "
							+ info.cellY + ")");
			info.opened = false;
		}

		if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
			// Close any open folder
			closeFolder();
			// Open the requested folder
			openFolder(folderIcon, true);
		} else {
			// Find the open folder...
			int folderScreen;
			if (openFolder != null) {
				folderScreen = mWorkspace.getPageForView(openFolder);
				// .. and close it
				closeFolder(openFolder, true, true);
				if (folderScreen != mWorkspace.getCurrentPage()) {
					// Close any folder open on the current screen
					closeFolder();
					// Pull the folder onto this screen
					openFolder(folderIcon, true);
				}
			}
		}
	}
	
	/**
	 * This method draws the FolderIcon to an ImageView and then adds and
	 * positions that ImageView in the DragLayer in the exact absolute location
	 * of the original FolderIcon.
	 */
	private void copyFolderIconToImage(FolderIcon fi) {
		
		Folder f = fi.getFolder();
		/*int width = f.mContent.getDesiredWidth();
		int height = f.mContent.getDesiredHeight();
		//Log.e("HJJ", "copyFolderIconToImage width:" + width + ",height:" + height);
		
		if (mFolderIconImageView == null) {
			mFolderIconImageView = new ImageView(this);
			mFolderIconImageView.setBackgroundResource(R.drawable.folder_content_bg);
		}

		DragLayer.LayoutParams lp;
		if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
			lp = (DragLayer.LayoutParams) mFolderIconImageView
					.getLayoutParams();
		} else {
			lp = new DragLayer.LayoutParams(width, height);
		}
		
		lp.x = this.getResources().getDimensionPixelSize(R.dimen.bg_left_point);
		lp.y = this.getResources().getDimensionPixelSize(R.dimen.bg_top_point);
		lp.width = width;
		lp.height = height;
		lp.customPosition = true;

		if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
			mDragLayer.removeView(mFolderIconImageView);
		}
		mDragLayer.addView(mFolderIconImageView, lp);
		mFolderIconImageView.setAlpha(1f);*/
		fi.setAlpha(0f);
		
		if (fi.getFolder() != null) {
			fi.getFolder().bringToFront();
		}
	}

	private ObjectAnimator growAndFadeOutFolderIcon(FolderIcon fi, long time, Interpolator in) {
		if (fi == null) return null;
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.3f, 0);
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 4f); // old 1.5f
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 4f); // old 1.5f

		FolderInfo info = (FolderInfo) fi.getTag();
		if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			CellLayout cl = (CellLayout) fi.getParent().getParent();
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi
					.getLayoutParams();
			cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
		}
		// Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
		copyFolderIconToImage(fi);
		fi.setVisibility(View.INVISIBLE);
		if(mFolderIconImageView==null){
			return null;
		}

		ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
				mFolderIconImageView, alpha, scaleX, scaleY);
		// oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
		
		oa.setDuration(time);
		oa.setInterpolator(in);
		return oa;
	}

	private Animator shrinkAndFadeInFolderIcon(final FolderIcon fi, long time, Interpolator ln) {
		if (fi == null)
			return null;
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha",0.1f, 0f); // 1.0f
		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 4.0f, 1f); // 1.0f
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 4.0f, 1f); // 1.0f

		final CellLayout cl = (CellLayout) fi.getParent().getParent();

		// We remove and re-draw the FolderIcon in-case it has changed
		mDragLayer.removeView(mFolderIconImageView);
		copyFolderIconToImage(fi);
		ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
				mFolderIconImageView, alpha, scaleX, scaleY);
		// oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
		oa.setDuration(time);
		oa.setInterpolator(ln);
		oa.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (cl != null) {
					cl.clearFolderLeaveBehind();
					// Remove the ImageView copy of the FolderIcon and make the
					// original visible.
					mDragLayer.removeView(mFolderIconImageView);
					//fi.setVisibility(View.VISIBLE);
				}
			}
		});
		return oa;
	}

	private void enterFullScreen(){
		onEnterFullScreen();
	}
	
	private AnimatorSet closeFolderAnimator = null;
	/**
	 * Opens the user folder described by the specified tag. The opening of the
	 * folder is animated relative to the specified View. If the View is null,
	 * no animation is played.
	 * 
	 * @param folderInfo
	 *            The FolderInfo describing the folder to open.
	 */
	public void openFolder(FolderIcon folderIcon, boolean animate) {
		// 出现无法文件夹背板消失的情况，一定要确保关闭动画是结束的
        
		if(closeFolderAnimator !=null && closeFolderAnimator.isRunning()){
			closeFolderAnimator.end();
		} 
		
		final Folder folder = folderIcon.getFolder();
		FolderInfo info = folder.mInfo;
		
	    final FolderIcon folIcon=folderIcon;
		info.opened = true;
		// Aurora <haojj> <2013-10-10> add for 文件打开状态 begin
		getWorkspace().setFolderOpenState(true);
	    getDragController().setFolderDragScoller(folder);
        //enterFullScreen();
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				enterFullScreen();
			}
		}, 300);
        
		// Just verify that the folder hasn't already been added to the
		// DragLayer.
		// There was a one-off crash where the folder had a parent already.
		if (folder.getParent() == null) {
			mDragLayer.addView(folder);
			if(animate){
				mDragController.addDropTarget((DropTarget) folder);
				mDragController.addDropTarget((DropTarget) folder.getEditTextRegion());
				mDragController.addDropTarget(folder.getFolderEmptyDropTarget());
			}
			// Aurora <haojj> <2013-10-2> end
		} else {
			Log.w(TAG, "Opening folder (" + folder+ ") which already has a parent (" + folder.getParent()+ ").");
		}
		
		long duration = 350;
		long delayEditName = 200;
		long delayAddView = 300;
		
		Interpolator ln = new DecelerateInterpolator(1.5f);
		ArrayList<Animator> items  = new ArrayList<Animator>();
		
		copyFolderIconToImage(folderIcon);
		
		Animator animFolder = folder.animateOpen(duration, ln, mFolderIconImageView);
		if(animFolder != null) items.add(animFolder);
		
		/*Animator animEditName = folder.animateShowEditName(duration, ln);
		if(animEditName != null) {
			animEditName.setStartDelay(delayEditName);
			items.add(animEditName);
		}*/

		if(folder.isOpenAfterImport()){
			folder.setAddItemsVisible(View.INVISIBLE);
		} else {
			if(mEditMode == EditMode.CLASSIFICATE_ICONS){
			//	folder.mContent.onlyDetachFolderAddView();
				folder.mContent.detachFolderAddView(false);
				folder.mFolderName.setEnabled(false);
			}
			else{
				if (animate ) {
					/*Animator animFolderAddIcon = folder.animateShowFolderAddIcon(duration, ln);
					if (animFolderAddIcon != null) {
						animFolderAddIcon.setStartDelay(delayAddView);
						folder.mContent.setFolderAddIconScaleToSmall(0f);
						items.add(animFolderAddIcon);
					}*/
				} else {
					// 长按某个icon打开文件夹时，不应该显示
					folder.mContent.suspendFolderAddIconAnimator();
				}
			}

		}
		
		Animator animBlurViewBg = animateShowBlurViewBg(duration, ln);
		if(animBlurViewBg != null) items.add(animBlurViewBg);
		
		AnimatorSet set = new AnimatorSet();
		set.playTogether(items);
		//if(!animate) set.setDuration(10);
		set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            	folder.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            	folder.buildLayer();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                folder.setLayerType(View.LAYER_TYPE_NONE, null);
                // 添加时，如果这时候打开一个文件夹，则hotseat会显示出来，这是不允许的
				if (getEditMode() != EditMode.FOLDER_IMPORT
						&& mHotseat.getVisibility() != View.VISIBLE) {
					mHotseat.setVisibility(View.VISIBLE);
				}
				startCalenderIconPlugin(folIcon,folder);	
            }
	    });
		set.start();
		
		if(!animate) {
			/*mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
					mDragController.addDropTarget((DropTarget) folder);
					mDragController.addDropTarget((DropTarget) folder.getEditTextRegion());
					mDragController.addDropTarget(folder.getFolderEmptyDropTarget());
				}
			}, duration - 100);*/
			addDropTargetRunnable.setFolder(folder);
			mHandler.postDelayed(addDropTargetRunnable, (long)(0.6 * duration));
		}
		processingFolderNoClick(duration);	
	}

	// add by xiexiujie for calender plugin icon start 10.13
	public void startCalenderIconPlugin(FolderIcon folderIcon, Folder folder) {

		Folder mFolder = folderIcon.getFolder();

		final FolderWorkspace mFolderWorkspce = mFolder.getFolderWorkspace();

		mFolderWorkspce
				.setOnCalenderPluginListening2(new FolderWorkspace.OnCalenderPluginListening2() {

					@Override
					public void CalenderPluginListening2() {
						// TODO Auto-generated method stub
						int i, j, size, childIndex;
						CellLayout curLayout = (CellLayout) mFolderWorkspce
								.getChildAt(mFolderWorkspce.getCurrentPage());

						if (curLayout == null)
							return;

						for (i = 0; i < curLayout.getCountY(); i++) {
							for (j = 0; j < curLayout.getCountX(); j++) {
								View v = curLayout.getChildAt(j, i);
								if (null == v)
									continue;
								ItemInfo info = (ItemInfo) v.getTag();
								if (info instanceof ShortcutInfo) {
									ShortcutInfo item = (ShortcutInfo) info;

									if ((item.getPackageName(item.intent)
											.equals("com.android.calendar") && play)) {	
										 addCalenderAnimationView();	
										LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
												LinearLayout.LayoutParams.WRAP_CONTENT,
												LinearLayout.LayoutParams.WRAP_CONTENT);
										lp.setMargins(
												getResources()
														.getInteger(
																R.integer.calendar_icon_dynamic_wight),
												0,
												getResources()
														.getInteger(
																R.integer.calendar_icon_dynamic_wight),
												getResources()
														.getInteger(
																R.integer.calendar_icon_dynamic_folder));
										flipView.setLayoutParams(lp);
										mFolderWorkspce
												.createAndAddCalenderPluginIcon(item.screen);
										CalendarIcon.refreshDay2(Launcher.this, currentTransModeEnum());
										play = false;
									}
								}

							}
						}

					}
				});
		FolderInfo fi = folder.mInfo;
		ArrayList<View> list = mFolderWorkspce.getAllItemsInReadingOrder();
		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			if (v.getTag() instanceof ShortcutInfo) {
				ShortcutInfo si = (ShortcutInfo) v.getTag();
				if ((si.getPackageName(si.intent)
						.equals("com.android.calendar") && play && si.screen == mFolderWorkspce
							.getCurrentPage())) {
					 addCalenderAnimationView();	
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(
							getResources().getInteger(
									R.integer.calendar_icon_dynamic_wight),
							0,
							getResources().getInteger(
									R.integer.calendar_icon_dynamic_wight),
							getResources().getInteger(
									R.integer.calendar_icon_dynamic_folder));
					flipView.setLayoutParams(lp);
					mFolderWorkspce.createAndAddCalenderPluginIcon(si.screen);
					CalendarIcon.refreshDay2(Launcher.this, currentTransModeEnum());
					play = false;
				}

			}
		}

	}

	// add by xiexiujie for calender plugin icon end 10.13
	private AddDropTargetRunnable addDropTargetRunnable = new AddDropTargetRunnable();
	private class AddDropTargetRunnable implements Runnable {
		private Folder folder;
		public AddDropTargetRunnable(){	
		}
		void setFolder(Folder f){
			folder = f;
		}
		@Override
		public void run() {
			if(folder != null){
				mDragController.addDropTarget((DropTarget) folder);
				mDragController.addDropTarget((DropTarget) folder.getEditTextRegion());
				mDragController.addDropTarget(folder.getFolderEmptyDropTarget());
			}
		}
	}
	
	private void processingFolderNoClick(long time){
		long duration = time - 100;
		if(duration < 200) {
			duration = 200;
		}
	    getDragLayer().setOpenFoldering(true);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				getDragLayer().setOpenFoldering(false);
				
				//vulcan created and tested it in 2014-6-19
				//notify launcher that folder is open or close!
				if(Launcher.this != null) {
					Launcher.this.onFolderProcess();
					setHomeKeyVal(false);
				}
				
			}
		}, duration);
	}
	

	
	//vulcan created and tested it in 2014-6-19
	//after folder is open or close, this method will be called.
	private boolean onFolderProcess() {
		getWorkspace().getFolderOpenState();
		Workspace w = getWorkspace();
		if(w != null) {
			if(w.getFolderOpenState()) {
				return onFolderOpenComplete();
			}
			else {
				return onFolderCloseComplete();
			}
		}
		return true;
	}
	private boolean onFolderOpenComplete() {
		Log.d("vulcan-iconop","processingFolderNoClick: openFlag = " + Launcher.this.getWorkspace().getFolderOpenState());
		DynIconDriver.setFolderWorksapceOpenSwitch(true);
		return true;
	}
	private boolean onFolderCloseComplete() {
		Log.d("vulcan-iconop","processingFolderNoClick: openFlag = " + Launcher.this.getWorkspace().getFolderOpenState());
		DynIconDriver.setFolderWorksapceOpenSwitch(false);
		return true;
	}

	private Animator animateShowBlurViewBg(long time, Interpolator ln){
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1.0f);
		ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
			mFolderBgBlurView, alpha);
		oa.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mFolderBgBlurView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				showFolderBgBlurView();
				mFolderBgBlurView.setAlpha(0f);
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				mFolderBgBlurView.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		});	
		oa.setDuration(time);
		oa.setInterpolator(ln);
		return oa;
	}
	
	private Animator animateHideBlurViewBg(long time, Interpolator ln){
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
		ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
			mFolderBgBlurView, alpha);
		oa.setDuration(time);
		oa.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				dismissBlurView();
			}
		});
		oa.setInterpolator(ln);
		return oa;
	}

	// 关闭动画
	public void closeFolder() {
		if(addDropTargetRunnable != null) {
			mHandler.removeCallbacks(addDropTargetRunnable);
		}
		closeFolder(true);
	}
	
	private void closeFolder(boolean animate) {
		Folder folder = mWorkspace.getOpenFolder();
		if (folder != null) {
			if (folder.isEditingName()) {
				folder.dismissEditingName();
			}			
			closeFolder(folder, true, animate);
			// Dismiss the folder cling
			//dismissFolderCling(null);

		}
		
	}
	
	void animateCloseFolder(final Folder folder, boolean showFolderIcon, boolean animate){
		long duration = 400;
		long delaytime = 200;
		Interpolator ln = new DecelerateInterpolator(1.5f);
		ArrayList<Animator> delayItems = new ArrayList<Animator>();
		ArrayList<Animator> items = new ArrayList<Animator>();
		
		FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);

		Animator animClosedFolder = folder.animateClosed(duration, ln);
		if(animClosedFolder != null){
			delayItems.add(animClosedFolder);
		}
		Animator animHideBlurViewBg = animateHideBlurViewBg(duration, ln); //new DecelerateInterpolator(1.5f)
		if(animHideBlurViewBg != null){
			delayItems.add(animHideBlurViewBg);
		}
		
		if(getEditMode() != EditMode.FOLDER_IMPORT  &&  getEditMode() != EditMode.CLASSIFICATE_ICONS){
			/*Animator animHideFolderAddIcon = folder.animateHideFolderAddIcon(200, ln);
			if(animHideFolderAddIcon != null) items.add(animHideFolderAddIcon);*/
		}
		
		/*Animator animHideEditName = folder.animateHideEditName(300,ln);
		if(animHideEditName != null) items.add(animHideEditName);*/
		
		AnimatorSet childSet = new AnimatorSet();
		childSet.playTogether(delayItems);
		childSet.setStartDelay(delaytime);
		items.add(childSet);
		
		if(showFolderIcon){
			Animator animClosedFolderIcon = animateFolderIcon(fi, 500, ln);
			animClosedFolderIcon.setStartDelay(400);
			items.add(animClosedFolderIcon);
		} else if(getEditMode() == EditMode.FOLDER_IMPORT){
			Animator animFolderIcon = folder.animateFolderIcon(500, ln);
			animFolderIcon.setStartDelay(400);
			items.add(animFolderIcon);
			
			// Aurora <haojj> <2013-12-17> add for 导入模式时hint和button的动画 begin
			Animator animImportHint = animateEnterFolderImportHint();
			if(animImportHint != null) {
				animImportHint.setStartDelay(350);
				items.add(animImportHint);
			}
			
			Animator animImportButton = animateEnterFolderImportButton();
			if(animImportButton != null) {
				animImportButton.setStartDelay(350);
				items.add(animImportButton);
			}
			// Aurora <haojj> <2013-12-17> end
		}
		closeFolderAnimator = new AnimatorSet();
		closeFolderAnimator.playTogether(items);
		if(!animate) {
			duration = 10;
			closeFolderAnimator.setDuration(duration);
		}
		closeFolderAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				folder.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				folder.buildLayer();
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				folder.setLayerType(View.LAYER_TYPE_NONE, null);
				//Log.e("linp", "############################onAnimationEnd");
				if(getEditMode() ==EditMode.CLASSIFICATE_ICONS){
				folder.getFolderWorkspace().attachFolderAddView(false, 0, 0);
			}
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
			closeFolderAnimator.start();	
			processingFolderNoClick(duration );
	}
	
	void closeFolder(Folder folder, boolean showFolderIcon, boolean animate) {
		if(addDropTargetRunnable != null) {
			mHandler.removeCallbacks(addDropTargetRunnable);
		}
		folder.getInfo().opened = false;
		getWorkspace().setFolderOpenState(false);
		animateCloseFolder(folder, showFolderIcon, animate);
		
		if(folder.getDragInProgress() || getDragController().dragging()) return;
		if(getEditMode() == EditMode.FOLDER_IMPORT) {
			folder.setShowFolderAddViewOnAttach();
			return;
		}
		//Hazel add for CLASSIFICATE_ICONS
		if(getEditMode() ==EditMode.CLASSIFICATE_ICONS){
			//folder.setShowFolderAddViewOnAttach();
			//folder.getFolderWorkspace().hideFolderAddView();
			//folder.getFolderWorkspace().attachFolderAddView(false, 0, 0);
			folder.mFolderName.setEnabled(true);
			return;
		}
		
		//LauncherApplication.logVulcan.print("closeFolder: to call exitEditMode!");
		//LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				exitEditMode(true);
			}
		}, 100); // 130
	}

	private Animator animateFolderIcon(final FolderIcon fi, long time, Interpolator ln){
		ValueAnimator va = LauncherAnimUtils.ofFloat(0f, 1f);
    	va.removeAllUpdateListeners();
    	va.addUpdateListener(new AnimatorUpdateListener(){
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if(fi != null) {
                	fi.setAlpha(progress);
                } 
            }
        });    
    	va.setDuration(time);
        va.setInterpolator(ln);
        return va;
	}
	
	void closeFolderNoAnimate(Folder folder) {
		if (folder.isEditingName()) {
			folder.dismissEditingName();
		}
		
		folder.getInfo().opened = false;
		getWorkspace().setFolderOpenState(false);
		
		animateCloseFolder(folder, true, false);
		
		LauncherApplication.logVulcan.print("closeFolderNoAnimate: to call exitEditMode!");
		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
		exitEditMode(true);
	}
	 //add by xiexiujie for calender plugin icon start 10.13
	public void addCalenderAnimationView() {
		flipView=new FlipViewController(this);
		if(layout!=null&&layout.getParent()!=null){
			 CellLayout	ly=	(CellLayout) layout.getParent().getParent();
	  		ly.removeView(layout);  	
		}
		layout=new CalenderPlugin(this);
	    layout.setOrientation(LinearLayout.VERTICAL);
				
	    DynIconPlg.createCalendar(flipView,layout);
		flipView.setFocusableInTouchMode(false);
		flipView.setFocusable(false);
		if(flipView.getParent()!=null){
			layout.removeView(flipView)	;
		}

		layout.addView(flipView);		
	}
	 //add by xiexiujie for calender plugin icon end 10.13
	public boolean onLongClick(View v) {
		
		//Aurora_START: iht 2014-12-10
		if(mWorkspace != null){
			mWorkspace.before_edit_pagesCount = mWorkspace.getChildCount();
		}
		//Aurora_END
		
		if (!isDraggingEnabled()) return false;
		if (isWorkspaceLocked()) return false;
		
		if(mEditMode==EditMode.FOLDER_IMPORT || mEditMode==EditMode.CLASSIFICATE_ICONS)return false;
		if (!(v instanceof CellLayout)) {
			v = (View) v.getParent().getParent();
		}

		resetAddInfo();
		
		CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
		//add by xiexiujie for calender plugin icon start 10.13
		if (longClickCellInfo.cell instanceof CalenderPlugin) {
			return false;
		}
		//add by xiexiujie for calender plugin icon start 10.13
		// This happens when long clicking an item with the dpad/trackball
		if (longClickCellInfo == null) {
			return true;
		}

		// The hotseat touch handling does not go through Workspace, and we
		// always allow long press
		// on hotseat items.
		final View itemUnderLongClick = longClickCellInfo.cell;
		boolean allowLongPress = isHotseatLayout(v)|| mWorkspace.allowLongPress();
		if(mEditMode==EditMode.APPWIDGET_ADD){
			if (!(itemUnderLongClick instanceof LauncherAppWidgetHostView)) return false;
		}
		
		if (allowLongPress && !mDragController.isDragging()) {
			if (itemUnderLongClick == null) {
				// User long pressed on empty space
				mWorkspace.performHapticFeedback(
						HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
				enterEditMode(EditMode.APPWIDGET_ADD,true);
				createNewPage();
			} else {
				if (!(itemUnderLongClick instanceof Folder)) {
					mAuroraDragView = itemUnderLongClick;
					mWorkspace.startDrag(longClickCellInfo);
					View view = mWorkspace.getChildAt(mWorkspace.getChildCount()-1);
					if(view!=null && view instanceof CellLayout){
						ShortcutAndWidgetContainer sw = ((CellLayout)view).getShortcutsAndWidgets();
						//ALog.i("aaa","sw.getChildCount()="+sw.getChildCount());
						if(sw.getChildCount()>0){
							createNewPage();
							toPlayThumnailLongClickAnimator(true);
						}
					}
					previewContent.movetoItem(mWorkspace.getCurrentPage());
					setPreviewBarScroll(mWorkspace.getCurrentPage());
//					toPlayThumnailLongClickAnimator(true);
				}
			}
		}
		return true;
	}

	boolean isHotseatLayout(View layout) {
		return mHotseat != null && layout != null
				&& (layout instanceof CellLayout)
				&& (layout == mHotseat.getLayout());
	}

	Hotseat getHotseat() {
		return mHotseat;
	}

	SearchDropTargetBar getSearchBar() {
		return mSearchDropTargetBar;
	}

	/**
	 * Returns the CellLayout of the specified container at the specified
	 * screen.
	 */
	CellLayout getCellLayout(long container, int screen) {
		if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
			if (mHotseat != null) {
				return mHotseat.getLayout();
			} else {
				return null;
			}
		} else {
			return (CellLayout) mWorkspace.getChildAt(screen);
		}
	}

	Workspace getWorkspace() {
		return mWorkspace;
	}

	// Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
	@Override
	public boolean isAllAppsVisible() {
		return (mState == State.APPS_CUSTOMIZE)
				|| (mOnResumeState == State.APPS_CUSTOMIZE);
	}

	@Override
	public boolean isAllAppsButtonRank(int rank) {
		return mHotseat.isAllAppsButtonRank(rank);
	}

	/**
	 * Helper method for the cameraZoomIn/cameraZoomOut animations
	 * 
	 * @param view
	 *            The view being animated
	 * @param scaleFactor
	 *            The scale factor used for the zoom
	 */
	private void setPivotsForZoom(View view, float scaleFactor) {
		view.setPivotX(view.getWidth() / 2.0f);
		view.setPivotY(view.getHeight() / 2.0f);
	}

	void disableWallpaperIfInAllApps() {
		// Only disable it if we are in all apps
		if (isAllAppsVisible()) {
			if (mAppsCustomizeTabHost != null
					&& !mAppsCustomizeTabHost.isTransitioning()) {
				updateWallpaperVisibility(false);
			}
		}
	}

	private void setWorkspaceBackground(boolean workspace) {
	}

	void updateWallpaperVisibility(boolean visible) {
		int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
				: 0;
		int curflags = getWindow().getAttributes().flags
				& WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
		if (wpflags != curflags) {
			getWindow().setFlags(wpflags,
					WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
	}

	private void dispatchOnLauncherTransitionPrepare(View v, boolean animated,
			boolean toWorkspace) {
		if (v instanceof LauncherTransitionable) {
			((LauncherTransitionable) v).onLauncherTransitionPrepare(this,
					animated, toWorkspace);
		}
	}

	private void dispatchOnLauncherTransitionStart(View v, boolean animated,
			boolean toWorkspace) {
		if (v instanceof LauncherTransitionable) {
			((LauncherTransitionable) v).onLauncherTransitionStart(this,
					animated, toWorkspace);
		}

		// Update the workspace transition step as well
		dispatchOnLauncherTransitionStep(v, 0f);
	}

	private void dispatchOnLauncherTransitionStep(View v, float t) {
		if (v instanceof LauncherTransitionable) {
			((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
		}
	}

	private void dispatchOnLauncherTransitionEnd(View v, boolean animated,
			boolean toWorkspace) {
		if (v instanceof LauncherTransitionable) {
			((LauncherTransitionable) v).onLauncherTransitionEnd(this,
					animated, toWorkspace);
		}

		// Update the workspace transition step as well
		dispatchOnLauncherTransitionStep(v, 1f);
	}

	/**
	 * Things to test when changing the following seven functions. - Home from
	 * workspace - from center screen - from other screens - Home from all apps
	 * - from center screen - from other screens - Back from all apps - from
	 * center screen - from other screens - Launch app from workspace and quit -
	 * with back - with home - Launch app from all apps and quit - with back -
	 * with home - Go to a screen that's not the default, then all apps, and
	 * launch and app, and go back - with back -with home - On workspace, long
	 * press power and go back - with back - with home - On all apps, long press
	 * power and go back - with back - with home - On workspace, power off - On
	 * all apps, power off - Launch an app and turn off the screen while in that
	 * app - Go back with home key - Go back with back key TODO: make this not
	 * go to workspace - From all apps - From workspace - Enter and exit car
	 * mode (becuase it causes an extra configuration changed) - From all apps -
	 * From the center workspace - From another workspace
	 */

	/**
	 * Zoom the camera out from the workspace to reveal 'toView'. Assumes that
	 * the view to show is anchored at either the very top or very bottom of the
	 * screen.
	 */
	private void showAppsCustomizeHelper(final boolean animated,
			final boolean springLoaded) {
		if (mStateAnimation != null) {
			mStateAnimation.cancel();
			mStateAnimation = null;
		}
		final Resources res = getResources();

		final int duration = res
				.getInteger(R.integer.config_appsCustomizeZoomInTime);
		final int fadeDuration = res
				.getInteger(R.integer.config_appsCustomizeFadeInTime);
		final float scale = (float) res
				.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
		final View fromView = mWorkspace;
		final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
		final int startDelay = res
				.getInteger(R.integer.config_workspaceAppsCustomizeAnimationStagger);

		setPivotsForZoom(toView, scale);
		Animator workspaceAnim = mWorkspace.getChangeStateAnimation(
				Workspace.State.SMALL, animated);

		if (animated) {
			toView.setScaleX(scale);
			toView.setScaleY(scale);
			final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(
					toView);
			scaleAnim.scaleX(1f).scaleY(1f).setDuration(duration)
					.setInterpolator(new Workspace.ZoomOutInterpolator());

			toView.setVisibility(View.VISIBLE);
			toView.setAlpha(0f);
			final ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(toView,
					"alpha", 0f, 1f).setDuration(fadeDuration);
			alphaAnim.setInterpolator(new DecelerateInterpolator(1.5f));
			alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					if (animation == null) {
						throw new RuntimeException("animation is null");
					}
					float t = (Float) animation.getAnimatedValue();
					dispatchOnLauncherTransitionStep(fromView, t);
					dispatchOnLauncherTransitionStep(toView, t);
				}
			});

			// toView should appear right at the end of the workspace shrink
			// animation
			mStateAnimation = LauncherAnimUtils.createAnimatorSet();
			mStateAnimation.play(scaleAnim).after(startDelay);
			mStateAnimation.play(alphaAnim).after(startDelay);

			mStateAnimation.addListener(new AnimatorListenerAdapter() {
				boolean animationCancelled = false;

				@Override
				public void onAnimationStart(Animator animation) {
					updateWallpaperVisibility(true);
					// Prepare the position
					toView.setTranslationX(0.0f);
					toView.setTranslationY(0.0f);
					toView.setVisibility(View.VISIBLE);
					toView.bringToFront();
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					dispatchOnLauncherTransitionEnd(fromView, animated, false);
					dispatchOnLauncherTransitionEnd(toView, animated, false);

					if (mWorkspace != null && !springLoaded
							&& !LauncherApplication.isScreenLarge()) {
						// Hide the workspace scrollbar
						mWorkspace.hideScrollingIndicator(true);
						hideDockDivider();
					}
					if (!animationCancelled) {
						updateWallpaperVisibility(false);
					}

					// Hide the search bar
					if (mSearchDropTargetBar != null) {
						mSearchDropTargetBar.hideSearchBar(false);
					}
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					animationCancelled = true;
				}
			});

			if (workspaceAnim != null) {
				mStateAnimation.play(workspaceAnim);
			}

			boolean delayAnim = false;
			final ViewTreeObserver observer;

			dispatchOnLauncherTransitionPrepare(fromView, animated, false);
			dispatchOnLauncherTransitionPrepare(toView, animated, false);

			// If any of the objects being animated haven't been measured/laid
			// out
			// yet, delay the animation until we get a layout pass
			if ((((LauncherTransitionable) toView).getContent()
					.getMeasuredWidth() == 0)
					|| (mWorkspace.getMeasuredWidth() == 0)
					|| (toView.getMeasuredWidth() == 0)) {
				observer = mWorkspace.getViewTreeObserver();
				delayAnim = true;
			} else {
				observer = null;
			}

			final AnimatorSet stateAnimation = mStateAnimation;
			final Runnable startAnimRunnable = new Runnable() {
				public void run() {
					// Check that mStateAnimation hasn't changed while
					// we waited for a layout/draw pass
					if (mStateAnimation != stateAnimation)
						return;
					setPivotsForZoom(toView, scale);
					dispatchOnLauncherTransitionStart(fromView, animated, false);
					dispatchOnLauncherTransitionStart(toView, animated, false);
					toView.post(new Runnable() {
						public void run() {
							// Check that mStateAnimation hasn't changed while
							// we waited for a layout/draw pass
							if (mStateAnimation != stateAnimation)
								return;
							mStateAnimation.start();
						}
					});
				}
			};
			if (delayAnim) {
				final OnGlobalLayoutListener delayedStart = new OnGlobalLayoutListener() {
					public void onGlobalLayout() {
						toView.post(startAnimRunnable);
						observer.removeOnGlobalLayoutListener(this);
					}
				};
				observer.addOnGlobalLayoutListener(delayedStart);
			} else {
				startAnimRunnable.run();
			}
		} else {
			toView.setTranslationX(0.0f);
			toView.setTranslationY(0.0f);
			toView.setScaleX(1.0f);
			toView.setScaleY(1.0f);
			toView.setVisibility(View.VISIBLE);
			toView.bringToFront();

			if (!springLoaded && !LauncherApplication.isScreenLarge()) {
				// Hide the workspace scrollbar
				mWorkspace.hideScrollingIndicator(true);
				hideDockDivider();

				// Hide the search bar
				if (mSearchDropTargetBar != null) {
					mSearchDropTargetBar.hideSearchBar(false);
				}
			}
			dispatchOnLauncherTransitionPrepare(fromView, animated, false);
			dispatchOnLauncherTransitionStart(fromView, animated, false);
			dispatchOnLauncherTransitionEnd(fromView, animated, false);
			dispatchOnLauncherTransitionPrepare(toView, animated, false);
			dispatchOnLauncherTransitionStart(toView, animated, false);
			dispatchOnLauncherTransitionEnd(toView, animated, false);
			updateWallpaperVisibility(false);
		}
	}

	/**
	 * Zoom the camera back into the workspace, hiding 'fromView'. This is the
	 * opposite of showAppsCustomizeHelper.
	 * 
	 * @param animated
	 *            If true, the transition will be animated.
	 */
	private void hideAppsCustomizeHelper(State toState, final boolean animated,
			final boolean springLoaded, final Runnable onCompleteRunnable) {

		if (mStateAnimation != null) {
			mStateAnimation.cancel();
			mStateAnimation = null;
		
		}
		Resources res = getResources();

		final int duration = res
				.getInteger(R.integer.config_appsCustomizeZoomOutTime);
		final int fadeOutDuration = res
				.getInteger(R.integer.config_appsCustomizeFadeOutTime);
		final float scaleFactor = (float) res
				.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
		final View fromView = mAppsCustomizeTabHost;
		final View toView = mWorkspace;
		Animator workspaceAnim = null;

		if (toState == State.WORKSPACE) {
			int stagger = res
					.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger);
			workspaceAnim = mWorkspace.getChangeStateAnimation(
					Workspace.State.NORMAL, animated, stagger);
		} else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
			workspaceAnim = mWorkspace.getChangeStateAnimation(
					Workspace.State.SPRING_LOADED, animated);
		}

		setPivotsForZoom(fromView, scaleFactor);
		updateWallpaperVisibility(true);
		showHotseat(animated);
		if (animated) {
			final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(
					fromView);
			scaleAnim.scaleX(scaleFactor).scaleY(scaleFactor)
					.setDuration(duration)
					.setInterpolator(new Workspace.ZoomInInterpolator());

			final ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(fromView,
					"alpha", 1f, 0f).setDuration(fadeOutDuration);
			alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
			alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float t = 1f - (Float) animation.getAnimatedValue();
					dispatchOnLauncherTransitionStep(fromView, t);
					dispatchOnLauncherTransitionStep(toView, t);
				}
			});

			mStateAnimation = LauncherAnimUtils.createAnimatorSet();

			dispatchOnLauncherTransitionPrepare(fromView, animated, true);
			dispatchOnLauncherTransitionPrepare(toView, animated, true);
			mAppsCustomizeContent.pauseScrolling();

			mStateAnimation.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					updateWallpaperVisibility(true);
					fromView.setVisibility(View.GONE);
					dispatchOnLauncherTransitionEnd(fromView, animated, true);
					dispatchOnLauncherTransitionEnd(toView, animated, true);
					if (mWorkspace != null) {
						mWorkspace.hideScrollingIndicator(false);
					}
					if (onCompleteRunnable != null) {
						onCompleteRunnable.run();
					}
					mAppsCustomizeContent.updateCurrentPageScroll();
					mAppsCustomizeContent.resumeScrolling();
				}
			});

			mStateAnimation.playTogether(scaleAnim, alphaAnim);
			if (workspaceAnim != null) {
				mStateAnimation.play(workspaceAnim);
			}
			dispatchOnLauncherTransitionStart(fromView, animated, true);
			dispatchOnLauncherTransitionStart(toView, animated, true);
			final Animator stateAnimation = mStateAnimation;
			mWorkspace.post(new Runnable() {
				public void run() {
					if (stateAnimation != mStateAnimation)
						return;
					mStateAnimation.start();
				}
			});
		} else {
			fromView.setVisibility(View.GONE);
			dispatchOnLauncherTransitionPrepare(fromView, animated, true);
			dispatchOnLauncherTransitionStart(fromView, animated, true);
			dispatchOnLauncherTransitionEnd(fromView, animated, true);
			dispatchOnLauncherTransitionPrepare(toView, animated, true);
			dispatchOnLauncherTransitionStart(toView, animated, true);
			dispatchOnLauncherTransitionEnd(toView, animated, true);
			mWorkspace.hideScrollingIndicator(false);
		}
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
			mAppsCustomizeTabHost.onTrimMemory();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			// When another window occludes launcher (like the notification
			// shade, or recents),
			// ensure that we enable the wallpaper flag so that transitions are
			// done correctly.
			updateWallpaperVisibility(true);
            if(isStatusBarSetUnTransparent()){
            	setStatusBarTransparent(false);
            }
           //mLayerMask.setBackgroundColor(Color.CYAN);
		} else {
			setStatusBarTransparent(true);
			/*if (lockState && !isOurLockMessage) {
				ALog.i("start", "2");
				lockState = false;
				mHandler.removeCallbacks(startAnimationRunnable);
				mHandler.post(startAnimationRunnable);
			}*/
			if(getTransModeEnum() == TransModeEnum.VOICE){
				Log.e("linp","onWindowFocusChanged TransModeEnum.VOICE hasFocus="+hasFocus);
				if(mVoiceContentView!=null){
					mVoiceContentView.clearFocus();
					/**Hide workspace ScrollingIndicator()*/
					View scrollingIndicator = getWorkspace().getScrollingIndicator();
					scrollingIndicator.setVisibility(View.INVISIBLE);
					mLayerMask.setBackgroundColor(getResources().getColor(R.color.workspace_layer_mask));
					getDragController().scheduleTimerTask();
				}
			}
		}
		super.onWindowFocusChanged(hasFocus);
	}
	
	private boolean isStatusBarSetUnTransparent() {
		boolean transparent = true;
		KeyguardManager manager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		transparent = transparent && !manager.isKeyguardLocked();
		if (mCustomMenuProxy != null)
			transparent = transparent && !mCustomMenuProxy.isShowing();
		if (mAlignIconDialog != null)
			transparent = transparent && !mAlignIconDialog.isShowing();
		if (mDismissFolderDialog != null)
			transparent = transparent && !mDismissFolderDialog.isShowing();
        if (mIsUninstallShow) {
            mIsUninstallShow = false;
            transparent = false;
        }
		return transparent;
	}

	void showWorkspace(boolean animated) {
		showWorkspace(animated, null);
	}

	void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
		Log.i("xiejun9","mSate==State.WORKSPACE:::::"+mState);
		if (mState != State.WORKSPACE&&mState!=State.SCALE_WORKSPACE) {
			//boolean wasInSpringLoadedMode = (mState == State.APPS_CUSTOMIZE_SPRING_LOADED);
			mWorkspace.setVisibility(View.VISIBLE);
		}

		//mWorkspace.flashScrollingIndicator(animated);

		// Change the state *after* we've called all the transition code
		mState = State.WORKSPACE;

		// Resume the auto-advance of widgets
		mUserPresent = true;
		updateRunning();

		// Send an accessibility event to announce the context change
		getWindow().getDecorView().sendAccessibilityEvent(
				AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void showAllApps(boolean animated) {
		if (mState != State.WORKSPACE)
			return;

		showAppsCustomizeHelper(animated, false);
		mAppsCustomizeTabHost.requestFocus();

		// Change the state *after* we've called all the transition code
		mState = State.APPS_CUSTOMIZE;

		// Pause the auto-advance of widgets until we are out of AllApps
		mUserPresent = false;
		updateRunning();
		closeFolder();

		// Send an accessibility event to announce the context change
		getWindow().getDecorView().sendAccessibilityEvent(
				AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void enterSpringLoadedDragMode() {
		if (isAllAppsVisible()) {
			hideAppsCustomizeHelper(State.APPS_CUSTOMIZE_SPRING_LOADED, true,
					true, null);
			hideDockDivider();
			mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
		}
	}

	void exitSpringLoadedDragModeDelayed(final boolean successfulDrop,
			boolean extendedDelay, final Runnable onCompleteRunnable) {
		if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED)
			return;

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (successfulDrop) {
					// Before we show workspace, hide all apps again because
					// exitSpringLoadedDragMode made it visible. This is a bit
					// hacky; we should
					// clean up our state transition functions
					mAppsCustomizeTabHost.setVisibility(View.GONE);
					showWorkspace(true, onCompleteRunnable);
				} else {
					exitSpringLoadedDragMode();
				}
			}
		}, (extendedDelay ? EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT
				: EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT));
	}

	void exitSpringLoadedDragMode() {
		if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
			final boolean animated = true;
			final boolean springLoaded = true;
			showAppsCustomizeHelper(animated, springLoaded);
			mState = State.APPS_CUSTOMIZE;
		}
		// Otherwise, we are not in spring loaded mode, so don't do anything.
	}

	void hideDockDivider() {
		// TODO
	}

	void showDockDivider(boolean animated) {
		// TODO
	}

	void lockAllApps() {
		// TODO
	}

	void unlockAllApps() {
		// TODO
	}

	/**
	 * Shows the hotseat area.
	 */
	void showHotseat(boolean animated) {
		if (!LauncherApplication.isScreenLarge()) {
			if (animated) {
				if (mHotseat.getAlpha() != 1f) {
					int duration = 0;
					if (mSearchDropTargetBar != null) {
						duration = mSearchDropTargetBar
								.getTransitionInDuration();
					}
					mHotseat.animate().alpha(1f).setDuration(duration);
				}
			} else {
				mHotseat.setAlpha(1f);
			}
		}
	}

	/**
	 * Hides the hotseat area.
	 */
	void hideHotseat(boolean animated) {
		if (!LauncherApplication.isScreenLarge()) {
			if (animated) {
				if (mHotseat.getAlpha() != 0f) {
					int duration = 0;
					if (mSearchDropTargetBar != null) {
						duration = mSearchDropTargetBar
								.getTransitionOutDuration();
					}
					mHotseat.animate().alpha(0f).setDuration(duration);
				}
			} else {
				mHotseat.setAlpha(0f);
			}
		}
	}

	/**
	 * Add an item from all apps or customize onto the given workspace screen.
	 * If layout is null, add to the current screen.
	 */
	void addExternalItemToScreen(ItemInfo itemInfo, final CellLayout layout) {
		if (!mWorkspace.addExternalItemToScreen(itemInfo, layout)) {
			showOutOfSpaceMessage(isHotseatLayout(layout));
		}
	}

	/**
	 * Maps the current orientation to an index for referencing orientation
	 * correct global icons
	 */
	private int getCurrentOrientationIndexForGlobalIcons() {
		// default - 0, landscape - 1
		switch (getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			return 1;
		default:
			return 0;
		}
	}

	private Drawable getExternalPackageToolbarIcon(ComponentName activityName,
			String resourceName) {
		try {
			PackageManager packageManager = getPackageManager();
			// Look for the toolbar icon specified in the activity meta-data
			Bundle metaData = packageManager.getActivityInfo(activityName,
					PackageManager.GET_META_DATA).metaData;
			if (metaData != null) {
				int iconResId = metaData.getInt(resourceName);
				if (iconResId != 0) {
					Resources res = packageManager
							.getResourcesForActivity(activityName);
					return res.getDrawable(iconResId);
				}
			}
		} catch (NameNotFoundException e) {
			// This can happen if the activity defines an invalid drawable
			Log.w(TAG,"Failed to load toolbar icon; "+ activityName.flattenToShortString()+ " not found", e);
		} catch (Resources.NotFoundException nfe) {
			// This can happen if the activity defines an invalid drawable
			Log.w(TAG,"Failed to load toolbar icon from "+ activityName.flattenToShortString(), nfe);
		}
		return null;
	}

	// if successful in getting icon, return it; otherwise, set button to use
	// default drawable
	private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
			int buttonId, ComponentName activityName, int fallbackDrawableId,
			String toolbarResourceName) {
		Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName,
				toolbarResourceName);
		Resources r = getResources();
		int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
		int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);

		TextView button = (TextView) findViewById(buttonId);
		// If we were unable to find the icon via the meta-data, use a generic
		// one
		if (toolbarIcon == null) {
			toolbarIcon = r.getDrawable(fallbackDrawableId);
			toolbarIcon.setBounds(0, 0, w, h);
			if (button != null) {
				button.setCompoundDrawables(toolbarIcon, null, null, null);
			}
			return null;
		} else {
			toolbarIcon.setBounds(0, 0, w, h);
			if (button != null) {
				button.setCompoundDrawables(toolbarIcon, null, null, null);
			}
			return toolbarIcon.getConstantState();
		}
	}

	// if successful in getting icon, return it; otherwise, set button to use
	// default drawable
	private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
			int buttonId, ComponentName activityName, int fallbackDrawableId,
			String toolbarResourceName) {
		ImageView button = (ImageView) findViewById(buttonId);
		Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName,
				toolbarResourceName);

		if (button != null) {
			// If we were unable to find the icon via the meta-data, use a
			// generic one
			if (toolbarIcon == null) {
				button.setImageResource(fallbackDrawableId);
			} else {
				button.setImageDrawable(toolbarIcon);
			}
		}

		return toolbarIcon != null ? toolbarIcon.getConstantState() : null;

	}

	private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
		TextView button = (TextView) findViewById(buttonId);
		button.setCompoundDrawables(d, null, null, null);
	}

	private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
		ImageView button = (ImageView) findViewById(buttonId);
		button.setImageDrawable(d.newDrawable(getResources()));
	}

	private void invalidatePressedFocusedStates(View container, View button) {
		if (container instanceof HolographicLinearLayout) {
			HolographicLinearLayout layout = (HolographicLinearLayout) container;
			layout.invalidatePressedFocusedStates();
		} else if (button instanceof HolographicImageView) {
			HolographicImageView view = (HolographicImageView) button;
			view.invalidatePressedFocusedStates();
		}
	}

	private boolean updateGlobalSearchIcon() {
		// TODO
		return false;
	}

	private void updateGlobalSearchIcon(Drawable.ConstantState d) {
		// TODO
	}

	private boolean updateVoiceSearchIcon(boolean searchVisible) {
		// TODO
		return false;
	}

	private void updateVoiceSearchIcon(Drawable.ConstantState d) {
		final View voiceButtonContainer = findViewById(R.id.voice_button_container);
		final View voiceButton = findViewById(R.id.voice_button);
		updateButtonWithDrawable(R.id.voice_button, d);
		invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
	}

	/**
	 * Sets the app market icon
	 */
	private void updateAppMarketIcon() {
	}

	private void updateAppMarketIcon(Drawable.ConstantState d) {
		// Ensure that the new drawable we are creating has the approprate
		// toolbar icon bounds
		Resources r = getResources();
		Drawable marketIconDrawable = d.newDrawable(r);
		int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
		int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);
		marketIconDrawable.setBounds(0, 0, w, h);

		updateTextButtonWithDrawable(R.id.market_button, marketIconDrawable);
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		final boolean result = super.dispatchPopulateAccessibilityEvent(event);
		final List<CharSequence> text = event.getText();
		text.clear();
		// Populate event with a fake title based on the current state.
		if (mState == State.APPS_CUSTOMIZE) {
			text.add(getString(R.string.all_apps_button_label));
		} else {
			text.add(getString(R.string.all_apps_home_button_label));
		}
		return result;
	}

	/**
	 * Receives notifications when system dialogs are to be closed.
	 */
	private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
		final String SYSTEM_DIALOG_REASON_KEY = "reason";
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		@Override
		public void onReceive(Context context, Intent intent) {
			closeSystemDialogs();
			if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
				if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY))) {
					if(mAlignIconDialog != null && mAlignIconDialog.isShowing()) {
						isAlignIconDialogOpen = true;
						mAlignIconDialog.dismiss();
					}
				}
			}else if(EXPAND_STATUSBAR_DISABLE.equals(intent.getAction())){
				enableExpandStatusBar = false;
			}else if(EXPAND_STATUSBAR_ENABLE.equals(intent.getAction())){
				enableExpandStatusBar = true;
			}
		}
	}

	/**
	 * Receives notifications whenever the appwidgets are reset.
	 */
	private class AppWidgetResetObserver extends ContentObserver {
		public AppWidgetResetObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			onAppWidgetReset();
		}
	}

	/**
	 * If the activity is currently paused, signal that we need to re-run the
	 * loader in onResume.
	 * 
	 * This needs to be called from incoming places where resources might have
	 * been loaded while we are paused. That is becaues the Configuration might
	 * be wrong when we're not running, and if it comes back to what it was when
	 * we were paused, we are not restarted.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 * 
	 * @return true if we are currently paused. The caller might be able to skip
	 *         some work in that case since we will come back again.
	 */
	public boolean setLoadOnResume() {
		if (mPaused) {
			Log.i(TAG, "setLoadOnResume");
			mOnResumeNeedsLoad = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public int getCurrentWorkspaceScreen() {
		if (mWorkspace != null) {
			return mWorkspace.getCurrentPage();
		} else {
			return SCREEN_COUNT / 2;
		}
	}
	
	/**
	 * save all folder's icon information on the desktop
	 */
	private void saveAllFolderIcons() {
		CellLayout page = null;
		ArrayList<FolderIcon> folderList = null;
		final Workspace workspace = mWorkspace;
		final int count = workspace.getChildCount();		
		for (int ii = 0; ii < count; ii++) {
			page = (CellLayout) workspace.getChildAt(ii);
			folderList = page.getAllFodlerIcon();
			for(int jj = 0; jj < folderList.size();jj ++) {
				Long key = ((ItemInfo)folderList.get(jj).getTag()).id;
				mSavedFolderIconMap.put(key, folderList.get(jj));
			}
		}

		for(FolderIcon fi: mSavedFolderIconMap.values()) {
			Log.d("vulcan-crash",String.format("saveAllFolderIcons: id = %d,folder = %s",
					((ItemInfo)fi.getTag()).id,
					((ItemInfo)fi.getTag()).title));
		}
	}
	
	/**
	 * check if we have saved the folder icons before reloading workspace
	 * @return
	 */
	public boolean hasSaveFolderIcon() {
		if(mSavedFolderIconMap == null) {
			return false;
		}
		return mSavedFolderIconMap.size() > 0;
	}
	
	/**
	 * query the FolderIcon from saved list
	 * @param itemId
	 * @return
	 */
	public FolderIcon getFolderIconFromSaved(long itemId) {
		if(mSavedFolderIconMap == null) {
			return null;
		}
		FolderIcon fi = mSavedFolderIconMap.get(itemId);
		return fi;
	}

	/**
	 * Refreshes the shortcuts shown on the workspace.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	
	public void startBinding(HashMap<Long, ItemInfo> itemMap,HashMap<Long, FolderInfo> folderMap) {
		
		Log.d("vulcan-widget","startBinding");

		setIsWorkspaceBinding(true);
		
		Folder folder = mWorkspace.getOpenFolder();
		if(folder != null  || isFolderImportMode()) {
			mFolderIconShouldSave = true;
		}

		if (mFolderIconShouldSave) {
			saveAllFolderIcons();
			recreateEditFolder(itemMap);
			recreateCheckList(itemMap,folderMap);
		}
		
		final Workspace workspace = mWorkspace;
		mNewShortcutAnimatePage = -1;
		mNewShortcutAnimateViews.clear();
		mWorkspace.clearDropTargets();
		int count = workspace.getChildCount();
		LauncherApplication.logVulcan.print("140728-p4-startBinding: will delete workspace item right now, now is " + System.currentTimeMillis()%60000);
		for (int i = 0; i < count; i++) {
			// Use removeAllViewsInLayout() to avoid an extra requestLayout()
			// and invalidate().
			final CellLayout layoutParent = (CellLayout) workspace
					.getChildAt(i);
			layoutParent.removeAllViewsInLayout();
		}
		mWidgetsToAdvance.clear();
		if (mHotseat != null) {
			mHotseat.resetAuroraLayout();
			mHotseat.calculateAuroraWidth();
		}

	}

	/**
	 * Bind the items start-end from the list.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	//public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end) {
	public void bindItems(final ArrayList shortcutList, final int start, final int end) {
		
		final ArrayList<ItemInfo>  shortcuts = shortcutList;
		
		setLoadOnResume();
		Set<String> newApps = new HashSet<String>();
		newApps = mSharedPrefs.getStringSet(
				InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);
		Workspace workspace = mWorkspace;
		for (int i = start; i < end; i++) {
			final ItemInfo item = shortcuts.get(i);
			LauncherApplication.logVulcan.print(String.format(
					"bindItems: title = %s,screen = %d, x=%d, y=%d",
					item.title, item.screen, item.cellX, item.cellY));

			// Short circuit if we are loading dock items for a configuration
			// which has no dock
			if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT
					&& mHotseat == null) {
				continue;
			}
			int count = workspace.getChildCount();
			Log.d("doubleicon", String.format(
					"bindItems: workspace page count = %d", count));
			if (item.screen > count - 1
					&& item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
				for (int j = count; j <= item.screen; j++) {
					createNewPage();
				}
			}
			switch (item.itemType) {
			case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
			case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
				ShortcutInfo info = (ShortcutInfo) item;
				String uri = info.intent.toUri(0).toString();
				View shortcut = createShortcut(info);

				if (getEditMode() == EditMode.FOLDER_IMPORT
						&& shortcut instanceof BubbleTextView) {
					updateCheckedBubbleTextView(info, (BubbleTextView) shortcut);
				}

				workspace.addInScreen(shortcut, item.container, item.screen,
						item.cellX, item.cellY, 1, 1, false);
				boolean animateIconUp = false;
				synchronized (newApps) {
					if (newApps.contains(uri)) {
						animateIconUp = newApps.remove(uri);
					}
				}
				if (animateIconUp) {
					// Prepare the view to be animated up
					shortcut.setAlpha(0f);
					shortcut.setScaleX(0f);
					shortcut.setScaleY(0f);
					mNewShortcutAnimatePage = item.screen;
					if (!mNewShortcutAnimateViews.contains(shortcut)) {
						mNewShortcutAnimateViews.add(shortcut);
					}
				}
				break;
			case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
				// FolderIcon newFolder =
				// FolderIcon.fromXml(R.layout.folder_icon,
				// this, (ViewGroup) workspace.getChildAt(workspace
				// .getCurrentPage()), (FolderInfo) item,
				// mIconCache);
				FolderIcon newFolder = FolderIcon.createFolderIcon(
						R.layout.folder_icon, Launcher.this,
						(ViewGroup) workspace.getChildAt(workspace
								.getCurrentPage()), (FolderInfo) item,
						mIconCache);
				workspace.addInScreen(newFolder, item.container, item.screen,
						item.cellX, item.cellY, 1, 1, false);
				break;
			}
		}
		
		//Aurora_START: iht 2014-12-10
		workspace.before_edit_pagesCount = workspace.getChildCount();
		//Aurora_END
		workspace.requestLayout();
	}

	/**
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindFolders(HashMap<Long, FolderInfo> folders) {
		setLoadOnResume();
		sFolders.clear();
		sFolders.putAll(folders);
	}

	/**
	 * Add the views for a widget to the workspace.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAppWidget(LauncherAppWidgetInfo item) {
		setLoadOnResume();

		final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
		if (DEBUG_WIDGETS) {
			Log.d(TAG, "bindAppWidget: " + item);
		}
		final Workspace workspace = mWorkspace;

		final int appWidgetId = item.appWidgetId;
		final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);
		if (DEBUG_WIDGETS) {
			Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
		}

		item.hostView = mAppWidgetHost.createView(this, appWidgetId,
				appWidgetInfo);

		item.hostView.setTag(item);
		item.onBindAppWidget(this);
		//Aurora-start:xiejun:BUG #120
		int count =getWorkspace().getChildCount();
		if(item.screen>count-1){
			for(int i=count; i<=item.screen;i++){
				createNewPage();
			}
		}
		//Aurora-end:xiejun:BUG #120
		
		
		LauncherApplication.logVulcan.print(String.format("bindAppWidget: binding widget: %s(%d,%d,%d)",
				item.title,item.screen,item.cellX,item.cellY));
		workspace.addInScreen(item.hostView, item.container, item.screen,
				item.cellX, item.cellY, item.spanX, item.spanY, false);
		addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

		//Aurora_START: iht 2014-12-10
		workspace.before_edit_pagesCount = workspace.getChildCount();
		//Aurora_END
		
		workspace.requestLayout();

		if (DEBUG_WIDGETS) {
			Log.d(TAG, "bound widget id=" + item.appWidgetId + " in "+ (SystemClock.uptimeMillis() - start) + "ms");
		}
	}

	public void onPageBoundSynchronously(int page) {
		mSynchronouslyBoundPages.add(page);
	}

	/**
	 * Callback saying that there aren't any more items to bind.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void finishBindingItems() {
		
		LauncherApplication.logVulcan.print("140728-p4-finishBindingItems: have already finish binding workspace, now is " + System.currentTimeMillis()%60000);
		setIsWorkspaceBinding(false);
		
		setLoadOnResume();
		if (mSavedState != null) {
			if (!mWorkspace.hasFocus()) {
				mWorkspace.getChildAt(mWorkspace.getCurrentPage())
						.requestFocus();
			}
			mSavedState = null;
		}
		mWorkspace.dispatchRestoreInstanceState(null);
		mWorkspace.restoreInstanceStateForRemainingPages();

		// If we received the result of any pending adds while the loader was
		// running (e.g. the
		// widget configuration forced an orientation change), process them now.
		for (int i = 0; i < sPendingAddList.size(); i++) {
			completeAdd(sPendingAddList.get(i));
		}
		sPendingAddList.clear();

		// Update the market app icon as necessary (the other icons will be
		// managed in response to
		// package changes in bindSearchablesChanged()
		// Animate up any icons as necessary
		LauncherApplication.logVulcan.print("finishBindingItems: mVisible = " + mVisible + ",mWorkspaceLoading = " + mWorkspaceLoading);
		if (mVisible || mWorkspaceLoading) {
			Runnable newAppsRunnable = new Runnable() {
				@Override
				public void run() {
					runNewAppsAnimation(false);
				}
			};

			boolean willSnapPage = mNewShortcutAnimatePage > -1
					&& mNewShortcutAnimatePage != mWorkspace.getCurrentPage();
			if (canRunNewAppsAnimation()) {
				// If the user has not interacted recently, then either snap to
				// the new page to show
				// the new-apps animation or just run them if they are to appear
				// on the current page
				if (willSnapPage) {
					mWorkspace.snapToPage(mNewShortcutAnimatePage,
							newAppsRunnable);
				} else {
					runNewAppsAnimation(false);
				}
			} else {
				// If the user has interacted recently, then just add the items
				// in place if they
				// are on another page (or just normally if they are added to
				// the current page)
				runNewAppsAnimation(willSnapPage);
			}
		}

	
		LauncherApplication.logVulcan.print("finishBindingItems: to call autoManangePages");
		
		//vulcan added it in 2014-6-20
		//after finishBindingItems, we notify one time to ensure dynamic icon refreshing
		mWorkspace.notifyPageSwitchListener();
		autoManangePages();
		// Aurora <haojj> <2013-12-12> add for 加载快速检索数据 begin
		constructAppsIndex(true);
		// Aurora <haojj> <2013-12-12> end
		mWorkspaceLoading = false;
		/**check Launcher is restart or already started. from voice print application invoke startActivity(..) */
		if(bAppRestart){
			getDragController().enterVoiceModeByVoicePrint(LAUNCHER_TASK_FLAG_NOT_EXIST);
			Log.e("linp", "###In finishBindingItems function bAppRestart is true and will enter Voice Mode");
			bAppRestart = false;//clear flag
		}
	}

	public  boolean isWorkspaceLoading (){
		return mWorkspaceLoading;
	}
	
	private TextWatcher mTextWatcher = new TextWatcher() {
    	@Override
        public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                int arg3) {
    		mQuickIndexOutline.updateMargin(true);
        }
       
        @Override
        public void onTextChanged(CharSequence s, int arg1, int arg2,
                int arg3) {
        }
       
        @Override
        public void afterTextChanged(Editable s) {
        	String ss = s.toString().trim();
        	boolean b = ss.contains("'");
        	if(b){
        		ss=ss.replace("'", "");
        		mQuickIndexSearchEditText.setText(ss);
        		mQuickIndexSearchEditText.setSelection(ss.trim().length());
        	}
        	if(!TextUtils.isEmpty(ss.trim())) {
        		searchInfosSet.clear();
        		ArrayList<ShortcutInfo> filterList = LauncherModel.getItemsFilterByLetter(Launcher.this, ss);
        		searchInfosSet.addAll(filterList);
        		if(filterList.isEmpty()) {
            		mResultEmpty.setVisibility(View.VISIBLE);
        		} else {
            		mResultEmpty.setVisibility(View.GONE);
        		}
        		initSearchQuickIndexAppsData(searchInfosSet);
        	} else {
        		mResultEmpty.setVisibility(View.GONE);
        		clearAppsIndex();
        	}
        }
        
    };
    
    public void hideSearchEditTextInputSoftKey(){
		if (mQuickIndexSearchEditText != null) {
			InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
			im.hideSoftInputFromWindow(
					mQuickIndexSearchEditText.getWindowToken(), 0);
		}
    }
    
    public void showSearchEditTextInputSoftKey(){
		if (mQuickIndexSearchEditText != null) {
			mQuickIndexSearchEditText.requestFocus();
			InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
			im.showSoftInput(mQuickIndexSearchEditText, 0);
		}
    }
    
	private void enterFilterSearchIndexMode(){
		setEditMode(EditMode.QUICK_INDEX);
		isEnterFilterMode = true;
		letterSideBar.setVisibility(View.GONE);
		mQuickIndexTitle.setVisibility(View.GONE);
		showSearchEditTextInputSoftKey();
		//mQuickIndexOutline.updateMargin(true);
		//mAppSearchBgImageView.setVisibility(View.INVISIBLE);
		mShadowImageView.setVisibility(View.INVISIBLE);
		//mUpperShadowImageView.setVisibility(View.INVISIBLE);
		clearAppsIndex();
		letterSideBar.setAlpha(0f);
		
	}
	
	private void emptySearchEditText(){
		if(mQuickIndexSearchEditText != null){
			if(!TextUtils.isEmpty(mQuickIndexSearchEditText.getText().toString())){
				mQuickIndexSearchEditText.setText(null);
			} else{
				//Log.e("HJJ", "==>SearchEditText is null");
			}
		}
	}
	
	private boolean isSearchEditTextEmpty(){
		boolean empty = true;
		if(mQuickIndexSearchEditText != null){
			if(!TextUtils.isEmpty(mQuickIndexSearchEditText.getText().toString())){
				empty = false;
			} 
		}
		return empty;
	}
	
	private void exitFilterSearchIndexMode(){
		setEditMode(EditMode.NONE);
		emptySearchEditText();
		isEnterFilterMode = false;
		hideSearchEditTextInputSoftKey();
		mQuickIndexSearchEditText.setVisibility(View.GONE);
		mQuickIndexTitle.setVisibility(View.VISIBLE);
		//mQuickIndexOutline.updateMargin(false);
		mAppSearchImageView.setTranslationY(0);
		mAppSearchImageView.setVisibility(View.VISIBLE);
		mAppSearchBgImageView.setVisibility(View.VISIBLE);
		mShadowImageView.setVisibility(View.VISIBLE);
		mUpperShadowImageView.setVisibility(View.VISIBLE);
		//setQuickIndexListViewVisible(true);
		
		if(mSearchQuickIndexListView != null && mSearchQuickIndexListView.getVisibility() == View.VISIBLE){
			Log.e("linp","###mSearchQuickIndexListView setVisibility GONE\n");
			mSearchQuickIndexListView.setVisibility(View.GONE);
		}
		Log.e("linp","exitFilterSearchIndexMode");
		letterSideBar.setVisibility(View.VISIBLE);
	}
	
	
	// Aurora <haojj> <2013-12-12> add for 更新gridview begin
	private void constructAppsIndex(boolean updatespell){
		currentInfosSet.clear();
		// long startTime = System.currentTimeMillis();
		HashSet<ShortcutInfo> apps = new HashSet<ShortcutInfo>();
		for (ItemInfo info : mModel.sBgWorkspaceItems) {
             if (info instanceof ShortcutInfo && info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            	 ShortcutInfo sInfo = (ShortcutInfo)info;
            	 /*if(updatespell) {
            		 sInfo.constructSpell(getApplicationContext());
            	 }*/
            	 apps.add(sInfo);
             } 
        }
		
		for (FolderInfo folderInfo : mModel.sBgFolders.values()) {
			ArrayList<ShortcutInfo> contents = folderInfo.contents;
			for (ItemInfo info : contents){
				if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
					ShortcutInfo sInfo = (ShortcutInfo)info;
					/*if(updatespell) {
						sInfo.constructSpell(getApplicationContext());
					}*/
	            	apps.add(sInfo);
		        } 
			} 
        }
		
		if(mQuickIndexListView != null){
			currentInfosSet.addAll(apps);
			Log.w("HJJ", "try to test initing quickIndex ==> constructAppsIndex");
			initQuickIndexAppsData(currentInfosSet, false);
			if(mQuickIndexListAdapter != null && !mQuickIndexListAdapter.getUseSoftReference()) {
				preLoadQuickIndexView();
			} else {
				// 刚开机时等待图标加载完成后才能进入appIndex
				getDragController().setReadyToAppIndex(true);
			}
		}
		// Log.e("HJJ", "index total time:" + (System.currentTimeMillis() - startTime));
	}
	
	// Aurora <haojj> <2014-2-27> add for 用于暗地滑动快速检索listview begin
	Runnable mScrollIndexListRunnable ;
	private class ScrollIndexListRunnable implements Runnable {
		public void run() {
			int len = mQuickIndexListAdapter.getSectionLength();
			HashSet<Integer> mSet = mQuickIndexListAdapter.getBuildViewIndexSet();
			
			int index=0;
			for(index=0;index<len;index++){
				if(!mSet.contains(index)) break;
			}
			if(index == len) {
				mQuickIndexListView.setSelectionFromTop(0, 0);
				mQuickIndexListView.invalidate();
				getDragController().setReadyToAppIndex(true);
				return;
			}
			
			// Log.e("HJJ", "setSelectionFromTop[" + index + "].....");
			mQuickIndexListView.setSelectionFromTop(index, 0);
			mQuickIndexListView.invalidate();
			
			mHandler.postDelayed(mScrollIndexListRunnable, 50);
		}
	}
	
	/*
	 * 将App检索全部图标进行加载，可能会导致内存不够的现象，后续会添加对App的限制
	 * 如果不使用预加载，请直接调用getDragController().setReadyToAppIndex(true);
	 */
	private void preLoadQuickIndexView(){
		int len = mQuickIndexListAdapter.getSectionLength();
		HashSet<Integer> mSet = mQuickIndexListAdapter.getBuildViewIndexSet();
		int index=0;
		for(index=0;index<len;index++){
			if(!mSet.contains(index)) break;
		}
		
		if(index == len) {
			mQuickIndexListView.setSelectionFromTop(0, 0);
			mQuickIndexListView.invalidate();
			getDragController().setReadyToAppIndex(true);
			return;
		}
		// Log.e("HJJ", "aaa setSelectionFromTop[" + index + "].....");
		mQuickIndexListView.setSelectionFromTop(index, index);
		mQuickIndexListView.invalidate();
		
		if(mScrollIndexListRunnable == null){
			mScrollIndexListRunnable = new ScrollIndexListRunnable();
		}
		mHandler.postDelayed(mScrollIndexListRunnable, 50);
	}
	// Aurora <haojj> <2014-2-27> end
	
	protected void setQuickIndexListViewVisible(boolean visible){		
		if(mQuickIndexListView != null && mQuickIndexListAdapter != null) {
			if(visible  && mQuickIndexListView.getAlpha()==0f) {
				mQuickIndexListView.setAlpha(1.0f);
			} else if(!visible && mQuickIndexListView.getAlpha()==1f) {
				mQuickIndexListView.setAlpha(0.0f);
			}
		}
	}
	
	
	public void restoreLetterSideBarAlphaAndVisibility(){
		if(letterSideBar.getAlpha() == 0f || letterSideBar.getVisibility() == View.GONE){
			letterSideBar.setAlpha(1.0f);
			letterSideBar.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 日志显示刚开机时会调用clearAppsIndex，但调用并不是一定的，可能调用也可能不调用
	 * 调用有时序问题，constructAppsIndex和clearAppsIndex不是同一时序，因此有可能导致mQuickIndexListView被置为INVISIBLE
	 * 因此经过打日志分析后添加mQuickIndexListAdapter != null
	 */
	private void clearAppsIndex(){
		Log.w("linp", "clearAppsIndex() from HJJ try to test initing quickIndex ==> clearAppsIndex, mQuickIndexListAdapter:" + mQuickIndexListAdapter);
		setQuickIndexListViewVisible(false);
		
		if(mSearchQuickIndexListView != null && mSearchQuickIndexListView.getVisibility() != View.VISIBLE){
			Log.e("linp", "###mSearchQuickIndexListView setVisibility visible!\n");
			mSearchQuickIndexListView.setVisibility(View.VISIBLE);
		}
		searchInfosSet.clear();
		initSearchQuickIndexAppsData(searchInfosSet);
	}
	
    private void initSearchQuickIndexAppsData(HashSet<ShortcutInfo> apps){
    	if(mSearchQuickIndexListAdapter == null){			
    		appSearchCategoryDataList = new ArrayList<AppCategoryData>();
    		
    		initSearchAppCategoryDataList(apps);
    		mSearchQuickIndexListAdapter = new QuickIndexListAdapter(this, appSearchCategoryDataList, apps.size());
    		mSearchQuickIndexListView.setAdapter(mSearchQuickIndexListAdapter);
			mSearchQuickIndexListAdapter.setAdapterHeaderVisiblity(true);
			mSearchQuickIndexListView.setAdapterHeaderVisiblity(true);
		}else{
			appSearchCategoryDataList.clear();
			initSearchAppCategoryDataList(apps);
			mSearchQuickIndexListView.setAdapterHeaderVisiblity(true);
			mSearchQuickIndexListAdapter.setAdapterHeaderVisiblity(true);
			mSearchQuickIndexListAdapter.mNotify();
		}
    }
    
	private void initSearchAppCategoryDataList(HashSet<ShortcutInfo> apps){
		String startLetter = "#";
		AppCategoryData categoryData = new AppCategoryData(startLetter);
		categoryData.addShortcutInfo(apps, false);
		appSearchCategoryDataList.add(0, categoryData);
		
		AppCategoryData firstAppCategoryData = appSearchCategoryDataList.get(0);
		if(firstAppCategoryData == null || firstAppCategoryData.getCategoryDataSize() == 0) return;
		Collections.sort(firstAppCategoryData.getCategoryDataList(), new Comparator<ShortcutInfo>() {
			public int compare(ShortcutInfo s1, ShortcutInfo s2) {
				return Utils.compare(s1.fullPinyin, s2.fullPinyin);
				//return Utils.compare(s1.title.toString(), s2.title.toString());
			}
		});
	}
	
	/**
	 * @param apps 
	 * @param hideHeader 原本隐藏标题，现在已经不需要了
	 */
    private void initQuickIndexAppsData(HashSet<ShortcutInfo> apps, boolean hideHeader){
    	if(mQuickIndexListAdapter == null){			
    		appCategoryDataList = new ArrayList<AppCategoryData>();
    		
    		initAppCategoryDataList(apps);
			mQuickIndexListAdapter = new QuickIndexListAdapter(this, appCategoryDataList, apps.size());
			mQuickIndexListView.setAdapter(mQuickIndexListAdapter);
			// Log.w("HJJ", "try to test initing quickIndex ==> initQuickIndexAppsData(param1, param2)");
			letterSideBar.setList(mQuickIndexListView);
			mQuickIndexListAdapter.setAdapterHeaderVisiblity(hideHeader);
			mQuickIndexListView.setAdapterHeaderVisiblity(hideHeader);
		}else{
			appCategoryDataList.clear();
			initAppCategoryDataList(apps);
			mQuickIndexListView.setAdapterHeaderVisiblity(hideHeader);
			mQuickIndexListAdapter.setAdapterHeaderVisiblity(hideHeader);
			mQuickIndexListAdapter.mNotify();
		}
    }
    
	private void initAppCategoryDataList(HashSet<ShortcutInfo> apps){
		HashMap<String, AppCategoryData> map = new HashMap<String, AppCategoryData>();	
		ShortcutInfo info;
		CharSequence title;
		Iterator<ShortcutInfo> iter = apps.iterator();
		while (iter.hasNext()) {
			info = iter.next();
			title = info.title;
			String startLetter;
			if(title == null){
				startLetter = "#";
			} else {
			    startLetter = Utils.getFirstPinYin(title.toString().replace(' ', ' ').trim());
			}
			if(map.containsKey(startLetter)){
				map.get(startLetter).addShortcutInfo(info);
			} else {
				AppCategoryData categoryData = new AppCategoryData(startLetter);
				categoryData.addShortcutInfo(info);
				map.put(startLetter, categoryData);
			}
		}
		
		if(map.containsKey("#")) {
			appCategoryDataList.add(map.get("#"));
		}
		
		for(char c ='A'; c <= 'Z'; c++){
			String key = String.valueOf(c);
			if(map.containsKey(key)) {
    			appCategoryDataList.add(map.get(key));
    		}
		}
		map.clear();
		
		for(int i=0; i< appCategoryDataList.size(); i++){
			AppCategoryData d = appCategoryDataList.get(i);
			Collections.sort(d.getCategoryDataList(), new Comparator<ShortcutInfo>() {
				public int compare(ShortcutInfo s1, ShortcutInfo s2) {
					return Utils.compare(s1.fullPinyin, s2.fullPinyin);
				}
			});
		}
	}
	
	protected void updateQuickIndexAppsData(ShortcutInfo info, String startLetter) {
		for (int i = 0; i < appCategoryDataList.size(); i++) {
			AppCategoryData d = appCategoryDataList.get(i);
			if (startLetter.equals(d.getCategoryName())) {
				d.removeShortcutInfo(info);
				if (d.getCategoryDataSize() <= 0) {
					appCategoryDataList.remove(i);
				}
				break;
			}
		}
	}
	
	// Aurora <haojj> <2014-1-11> add for 当添加、删除、更新items时要同步更新快速检索 begin
	protected void updateQuickIndexItems(ShortcutInfo item){
		// Log.e("HJJ", ">>>updateQuickIndexItems:" + item);
		String origStartLetter = item.startLetter;
		item.constructSpell(getApplicationContext());
		String nowStartLetter = item.startLetter;
		// Log.e("HJJ", ">>>updateQuickIndexItems==>origStartLetter:" + origStartLetter + ",nowLetter:" + nowStartLetter);
		updateQuickIndexAppsData(item, origStartLetter);
		ArrayList<String> changeLetters = new ArrayList<String>();
		changeLetters.add(origStartLetter);
		if(!nowStartLetter.equals(origStartLetter)) {
			changeLetters.add(nowStartLetter);
		} 
		
		addQuickIndexAppsData(item, false);
		mQuickIndexListAdapter.mNotify(changeLetters);
		letterSideBar.updateSections();
	}
	
	protected void removeQuickIndexItems(ArrayList<ShortcutInfo> list){
		// Log.e("HJJ", ">>>removeQuickIndexItems, list:" + list.size());
		if(list != null && list.size() == 0) return;
		// Log.e("HJJ", ">>>removeQuickIndexItems, execute...");
		if(isEnterFilterMode){
			if(!isSearchEditTextEmpty()){
				searchInfosSet.removeAll(list);
				// filterAppCategoryDataList(currentInfosSet);
				initSearchQuickIndexAppsData(searchInfosSet);
			}
		} 
		
		currentInfosSet.removeAll(list);
		
		if(mQuickIndexListView != null && mQuickIndexListAdapter != null){
			if(currentInfosSet.size() < QuickIndexListAdapter.MAX_NUM_FOR_NO_SOFTREFERENCE && mQuickIndexListAdapter.getUseSoftReference()) {
				mQuickIndexListAdapter.setUseSoftReference(false);
				removeQuickIndexAppsData(list, true);
			} else {
				removeQuickIndexAppsData(list, false);
			}
		}
	}
	
	protected void addQuickIndexItem(ShortcutInfo info){
		// Log.e("HJJ", ">>>addQuickIndexItem:" + info);
		if(isEnterFilterMode){
			if(!isSearchEditTextEmpty() && info.title != null && isItemMatchSearchStr(info)){
				searchInfosSet.add(info);
				// filterAppCategoryDataList(searchInfosSet);
				initSearchQuickIndexAppsData(searchInfosSet);
			}
		} 
		currentInfosSet.add(info);

		if (mQuickIndexListView != null && mQuickIndexListAdapter != null) {
			if(currentInfosSet.size() > QuickIndexListAdapter.MAX_NUM_FOR_NO_SOFTREFERENCE) {
				mQuickIndexListAdapter.setUseSoftReference(true);
				addQuickIndexAppsData(info, false);
				mQuickIndexListAdapter.mNotify();
				letterSideBar.updateSections();
			} else {
				addQuickIndexAppsData(info, true);
			}
		}
	}
	
	private boolean isItemMatchSearchStr(ShortcutInfo info){
		String searchStr = mQuickIndexSearchEditText.getText().toString();
		int len = searchStr.length();
        if(len < 1) return false;
        
		String title = info.title.toString();
		String simplePinyin = info.simplePinyin;
		String fullPinyin = info.fullPinyin;
		String upperSearchStr = searchStr.toUpperCase();
		
		if(title != null && (title.indexOf(upperSearchStr) != -1 || title.indexOf(searchStr) != -1)){
			return true;
		} else if((simplePinyin != null && simplePinyin.indexOf(upperSearchStr.charAt(0)) != -1) 
				&& ((simplePinyin.indexOf(upperSearchStr) != -1) || (fullPinyin != null && fullPinyin.indexOf(upperSearchStr) != -1))){
			// 首先匹配简拼，然后再判断全拼和简拼中是否包含该搜索字段
			return true;
		}
		return false;
	}
	
	protected void removeQuickIndexAppsData(ArrayList<ShortcutInfo> apps, boolean notifyAll) {
		ShortcutInfo info;
		CharSequence title;
		String startLetter;
		ArrayList<String> changeLetters = new ArrayList<String>();
		
		Iterator<ShortcutInfo> iter = apps.iterator();
		while (iter.hasNext()) {
			info = iter.next();
			title = info.title;
			
			if(title == null){
				startLetter = "#";
			} else {
			    startLetter = Utils.getFirstPinYin(title.toString().replace(' ', ' ').trim());
			}
			
			for(int i=0; i< appCategoryDataList.size(); i++) {
				AppCategoryData d = appCategoryDataList.get(i);
				if(startLetter.equals(d.getCategoryName())){
					d.removeShortcutInfo(info);
					changeLetters.add(startLetter);
					if(d.getCategoryDataSize() <= 0){
						appCategoryDataList.remove(i);
					}
					break;
				}
			}
		}
		if(notifyAll) {
			mQuickIndexListAdapter.mNotify();
		} else {
			mQuickIndexListAdapter.mNotify(changeLetters);
		}
		letterSideBar.updateSections();
	}
	
	protected void addQuickIndexAppsData(ShortcutInfo info, boolean notifyChange) {
		String startLetter;
		CharSequence title = info.title;
		if(title == null){
			startLetter = "#";
		} else {
		    startLetter = Utils.getFirstPinYin(title.toString().replace(' ', ' ').trim());
		}
		
		int index = -1;
		for(int i=0; i< appCategoryDataList.size(); i++) {
			AppCategoryData d = appCategoryDataList.get(i);
			if(startLetter.equals(d.getCategoryName())){
				index = i;
				break;
			}
		}
		
		// 添加新元素
		AppCategoryData addAppCategoryData = null;
		if(index == -1){
			addAppCategoryData = new AppCategoryData(startLetter);
			// 要将该插入到指定位置
			if(startLetter.equals("#")) {
				appCategoryDataList.add(0, addAppCategoryData);
			} else {
			    char startChar = startLetter.charAt(0);
			    int i=1;
				for(i=1; i< appCategoryDataList.size(); i++) {
					AppCategoryData d = appCategoryDataList.get(i);
					if(startChar < d.getCategoryName().charAt(0)){
						break;
					}
				}
				appCategoryDataList.add(i, addAppCategoryData);
			}
		} else {
			addAppCategoryData = appCategoryDataList.get(index);
		}
		addAppCategoryData.addShortcutInfo(info);
		
		// 排序
		if(addAppCategoryData.getCategoryDataSize() > 1) {
			Collections.sort(addAppCategoryData.getCategoryDataList(), new Comparator<ShortcutInfo>() {
				public int compare(ShortcutInfo s1, ShortcutInfo s2) {
					return Utils.compare(s1.fullPinyin, s2.fullPinyin);
				}
			});
		}
		
		if(notifyChange) {
			ArrayList<String> changeLetters = new ArrayList<String>();
			if (index != -1) changeLetters.add(startLetter);
			mQuickIndexListAdapter.mNotify(changeLetters);
			letterSideBar.updateSections();
		}
	}
	
	// Aurora <haojj> <2014-1-11> end
	
	private boolean canRunNewAppsAnimation() {
		long diff = System.currentTimeMillis()
				- mDragController.getLastGestureUpTime();
		return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
	}

	/**
	 * Runs a new animation that scales up icons that were added while Launcher
	 * was in the background.
	 * 
	 * @param immediate
	 *            whether to run the animation or show the results immediately
	 */
	private void runNewAppsAnimation(boolean immediate) {
		AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
		Collection<Animator> bounceAnims = new ArrayList<Animator>();

		// Order these new views spatially so that they animate in order
		Collections.sort(mNewShortcutAnimateViews, new Comparator<View>() {
			@Override
			public int compare(View a, View b) {
				CellLayout.LayoutParams alp = (CellLayout.LayoutParams) a
						.getLayoutParams();
				CellLayout.LayoutParams blp = (CellLayout.LayoutParams) b
						.getLayoutParams();
				int cellCountX = LauncherModel.getCellCountX();
				return (alp.cellY * cellCountX + alp.cellX)
						- (blp.cellY * cellCountX + blp.cellX);
			}
		});

		// Animate each of the views in place (or show them immediately if
		// requested)
		if (immediate) {
			for (View v : mNewShortcutAnimateViews) {
				v.setAlpha(1f);
				v.setScaleX(1f);
				v.setScaleY(1f);
			}
		} else {
			for (int i = 0; i < mNewShortcutAnimateViews.size(); ++i) {
				View v = mNewShortcutAnimateViews.get(i);
				ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
								PropertyValuesHolder.ofFloat("alpha", 1f),
								PropertyValuesHolder.ofFloat("scaleX", 1f),
								PropertyValuesHolder.ofFloat("scaleY", 1f));
				bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
				bounceAnim.setStartDelay(i* InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
				bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
				bounceAnims.add(bounceAnim);
			}
			anim.playTogether(bounceAnims);
			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (mWorkspace != null) {
						mWorkspace.postDelayed(mBuildLayersRunnable, 500);
					}
				}
			});
			anim.start();
		}

		// Clean up
		mNewShortcutAnimatePage = -1;
		mNewShortcutAnimateViews.clear();
		new Thread("clearNewAppsThread") {
			public void run() {
				mSharedPrefs.edit()
						.putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1)
						.putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, null)
						.commit();
			}
		}.start();
	}

	@Override
	public void bindSearchablesChanged() {
		// TODO
	}

	/**
	 * Add the icons for all apps.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAllApplications(final ArrayList<ApplicationInfo> apps) {
		Runnable setAllAppsRunnable = new Runnable() {
			public void run() {
				if (mAppsCustomizeContent != null) {
					mAppsCustomizeContent.setApps(apps);
				}
			}
		};

		// Remove the progress bar entirely; we could also make it GONE
		// but better to remove it since we know it's not going to be used
		View progressBar = mAppsCustomizeTabHost
				.findViewById(R.id.apps_customize_progress_bar);
		if (progressBar != null) {
			((ViewGroup) progressBar.getParent()).removeView(progressBar);

			// We just post the call to setApps so the user sees the progress
			// bar
			// disappear-- otherwise, it just looks like the progress bar froze
			// which doesn't look great
			mAppsCustomizeTabHost.post(setAllAppsRunnable);
		} else {
			// If we did not initialize the spinner in onCreate, then we can
			// directly set the
			// list of applications without waiting for any progress bars views
			// to be hidden.
			setAllAppsRunnable.run();
		}
	}

	/**
	 * A package was installed.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
		setLoadOnResume();

		if (mAppsCustomizeContent != null) {
			mAppsCustomizeContent.addApps(apps);
		}
	}

	/**
	 * A package was updated.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
		setLoadOnResume();
		if (mWorkspace != null) {
			mWorkspace.updateShortcuts(apps);
		}

		if (mAppsCustomizeContent != null) {
			mAppsCustomizeContent.updateApps(apps);
		}
	}

	/**
	 * A package was uninstalled.
	 * 
	 * Implementation of the method from LauncherModel.Callbacks.
	 */
	public void bindAppsRemoved(HashSet<Intent> appsToRmv, HashSet<String> widgetsToRmv, boolean permanent) {
		if (permanent) {
			mWorkspace.removeItems(appsToRmv,widgetsToRmv);
		}
		mDragController.onAppsRemoved(appsToRmv, this);
		if (mWidgetViewContent != null) {
			mWidgetViewContent.removeApps(appsToRmv);
		}
		
		//ht 2014-09-13
		//resetAuroraUninstallData(); //卸载\安装时，则清空临时记录? 为什么要这样操作？
	}

	/**
	 * A number of packages were updated.
	 */
	public void bindPackagesUpdated() {
		/*if (mAppsCustomizeContent != null) {
			mAppsCustomizeContent.onPackagesUpdated();
		}*/
		if (mWidgetViewContent != null) {
			mWidgetViewContent.onPackagesUpdated();
		}
	}

	private int mapConfigurationOriActivityInfoOri(int configOri) {
		final Display d = getWindowManager().getDefaultDisplay();
		int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
		switch (d.getRotation()) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			// We are currently in the same basic orientation as the natural
			// orientation
			naturalOri = configOri;
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			// We are currently in the other basic orientation to the natural
			// orientation
			naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ? Configuration.ORIENTATION_PORTRAIT
					: Configuration.ORIENTATION_LANDSCAPE;
			break;
		}
		int[] oriMap = { ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
				ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
				ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE };
		// Since the map starts at portrait, we need to offset if this device's
		// natural orientation
		// is landscape.
		int indexOffset = 0;
		if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
			indexOffset = 1;
		}
		return oriMap[(d.getRotation() + indexOffset) % 4];
	}

	public boolean isRotationEnabled() {
		boolean enableRotation = sForceEnableRotation||getResources().getBoolean(R.bool.allow_rotation);
		return enableRotation;
	}
	
	public void lockScreenOrientation() {
		if (isRotationEnabled()) {
			setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources().getConfiguration().orientation));
		}
	}

	public void unlockScreenOrientation(boolean immediate) {
		if (isRotationEnabled()) {
			if (immediate) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			} else {
				mHandler.postDelayed(new Runnable() {
					public void run() {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					}
				}, mRestoreScreenOrientationDelay);
			}
		}
	}

	/* Cling related */
	private boolean isClingsEnabled() {
		if (ActivityManager.isRunningInTestHarness())
			return false;

		return false;
	}
	
	private boolean isWorkspaceClingsEnabled() {
		if (ActivityManager.isRunningInTestHarness())
			return false;

		return true;
	}

	private Cling initCling(int clingId, int[] positionData, boolean animate,
			int delay) {
		final Cling cling = (Cling) findViewById(clingId);
		if (cling != null) {
			cling.init(this, positionData);
			cling.setVisibility(View.VISIBLE);
			cling.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			cling.setAlpha(1f);
			final Button btn_dismiss = (Button)cling.findViewById(R.id.btn_cling_dismiss);
			ImageView img_hand = (ImageView)cling.findViewById(R.id.img_cling_hand);
			btn_dismiss.setAlpha(0f);
			if (animate) {
				/*cling.buildLayer();
				cling.setAlpha(0f);
				cling.animate().alpha(1f)
						.setInterpolator(new AccelerateInterpolator())
						.setDuration(SHOW_CLING_DURATION).setStartDelay(delay)
						.start();*/
				/*Animation animationImg = new TranslateAnimation
					   (Animation.RELATIVE_TO_SELF,
                        0.0f,
                        Animation.RELATIVE_TO_SELF,
                        -6f,
                        Animation.RELATIVE_TO_SELF,
                        0.0f,
                        Animation.RELATIVE_TO_SELF,
                        0.0f);
				animationImg.setRepeatCount(Animation.INFINITE);
				animationImg.setRepeatMode(Animation.RESTART);
				animationImg.setDuration(800);
				animationImg.setInterpolator(new DecelerateInterpolator());
				img_hand.startAnimation(animationImg);*/
				
				/*add by xiangzx to show workspace cling in 2015/09/15 */
				img_hand.setBackgroundResource(R.anim.drag_bottom_left_anim);
				AnimationDrawable aniDraw = (AnimationDrawable)img_hand.getBackground();
				aniDraw.start();
				cling.postDelayed(new Runnable() {
					public void run() {
						btn_dismiss.setAlpha(1f);
					}
				},2000);
				
			} else {
				btn_dismiss.setAlpha(1f);
			}
			cling.setFocusableInTouchMode(true);
			cling.post(new Runnable() {
				public void run() {
					cling.setFocusable(true);
					cling.requestFocus();
				}
			});
//			mHideFromAccessibilityHelper.setImportantForAccessibilityToNo(
//					mDragLayer, clingId == R.id.all_apps_cling);
		}
		return cling;
	}

	private void dismissCling(final Cling cling, final String flag, int duration) {
		// To catch cases where siblings of top-level views are made invisible,
		// just check whether
		// the cling is directly set to GONE before dismissing it.
		if (cling != null && cling.getVisibility() != View.GONE) {
			ObjectAnimator anim = LauncherAnimUtils.ofFloat(cling, "alpha", 0f);
			anim.setDuration(duration);
			anim.addListener(new AnimatorListenerAdapter() {
				public void onAnimationEnd(Animator animation) {
					cling.setVisibility(View.GONE);
					cling.cleanup();
					// We should update the shared preferences on a background
					// thread
					new Thread("dismissClingThread") {
						public void run() {
							SharedPreferences.Editor editor = mSharedPrefs
									.edit();
							editor.putBoolean(flag, true);
							editor.commit();
						}
					}.start();
				};
			});
			anim.start();
			mHideFromAccessibilityHelper
					.restoreImportantForAccessibility(mDragLayer);
		}
	}

	private void removeCling(int id) {
		final View cling = findViewById(id);
		if (cling != null) {
			final ViewGroup parent = (ViewGroup) cling.getParent();
			parent.post(new Runnable() {
				@Override
				public void run() {
					parent.removeView(cling);
				}
			});
			mHideFromAccessibilityHelper
					.restoreImportantForAccessibility(mDragLayer);
		}
	}

	private boolean skipCustomClingIfNoAccounts() {
		Cling cling = (Cling) findViewById(R.id.workspace_cling);
		boolean customCling = cling.getDrawIdentifier().equals(
				"workspace_custom");
		if (customCling) {
			AccountManager am = AccountManager.get(this);
			Account[] accounts = am.getAccountsByType("com.google");
			return accounts.length == 0;
		}
		return false;
	}

	public void showFirstRunWorkspaceCling() {
		// Enable the clings only if they have not been dismissed before
		if (isWorkspaceClingsEnabled()
				&& !mSharedPrefs.getBoolean(
						Cling.WORKSPACE_CLING_DISMISSED_KEY, false)
				&& !skipCustomClingIfNoAccounts()) {
			// If we're not using the default workspace layout, replace
			// workspace cling
			// with a custom workspace cling (usually specified in an overlay)
			// For now, only do this on tablets
			if (mSharedPrefs.getInt(
					LauncherProvider.DEFAULT_WORKSPACE_RESOURCE_ID, 0) != 0
					&& getResources().getBoolean(R.bool.config_useCustomClings)) {
				// Use a custom cling
				View cling = findViewById(R.id.workspace_cling);
				ViewGroup clingParent = (ViewGroup) cling.getParent();
				int clingIndex = clingParent.indexOfChild(cling);
				clingParent.removeViewAt(clingIndex);
				View customCling = mInflater.inflate(
						R.layout.custom_workspace_cling, clingParent, false);
				clingParent.addView(customCling, clingIndex);
				customCling.setId(R.id.workspace_cling);
			}
			initCling(R.id.workspace_cling, null, true, 0);
		} else {
			removeCling(R.id.workspace_cling);
		}
	}

	public void showFirstRunAllAppsCling(int[] position) {
		// Enable the clings only if they have not been dismissed before
		if (isClingsEnabled()
				&& !mSharedPrefs.getBoolean(Cling.ALLAPPS_CLING_DISMISSED_KEY,
						false)) {
			initCling(R.id.all_apps_cling, position, true, 0);
		} else {
			removeCling(R.id.all_apps_cling);
		}
	}

	public Cling showFirstRunFoldersCling() {
		// Enable the clings only if they have not been dismissed before
		if (isClingsEnabled()
				&& !mSharedPrefs.getBoolean(Cling.FOLDER_CLING_DISMISSED_KEY,
						false)) {
			return initCling(R.id.folder_cling, null, true, 0);
		} else {
			removeCling(R.id.folder_cling);
			return null;
		}
	}

	public boolean isFolderClingVisible() {
		Cling cling = (Cling) findViewById(R.id.folder_cling);
		if (cling != null) {
			return cling.getVisibility() == View.VISIBLE;
		}
		return false;
	}
	
	public void dismissWorkspaceCling(View v) {
		Cling cling = (Cling) findViewById(R.id.workspace_cling);
		dismissCling(cling, Cling.WORKSPACE_CLING_DISMISSED_KEY,
				DISMISS_CLING_DURATION);
	}

	public void dismissAllAppsCling(View v) {
		Cling cling = (Cling) findViewById(R.id.all_apps_cling);
		dismissCling(cling, Cling.ALLAPPS_CLING_DISMISSED_KEY,
				DISMISS_CLING_DURATION);
	}

	public void dismissFolderCling(View v) {
		Cling cling = (Cling) findViewById(R.id.folder_cling);
		dismissCling(cling, Cling.FOLDER_CLING_DISMISSED_KEY,
				DISMISS_CLING_DURATION);
	}

	/**
	 * Prints out out state for debugging.
	 */
	public void dumpState() {
		Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
		Log.d(TAG, "mSavedState=" + mSavedState);
		Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
		Log.d(TAG, "mRestoring=" + mRestoring);
		Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
		Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
		Log.d(TAG, "sFolders.size=" + sFolders.size());
		mModel.dumpState();

		if (mAppsCustomizeContent != null) {
			mAppsCustomizeContent.dumpState();
		}
		Log.d(TAG, "END launcher2 dump state");
	}

	@Override
	public void dump(String prefix, FileDescriptor fd, PrintWriter writer,
			String[] args) {
		super.dump(prefix, fd, writer, args);
		writer.println(" ");
		writer.println("Debug logs: ");
		for (int i = 0; i < sDumpLogs.size(); i++) {
			writer.println("  " + sDumpLogs.get(i));
		}
	}

	public static void dumpDebugLogsToConsole() {
		Log.d(TAG, "");
		Log.d(TAG, "*********************");
		Log.d(TAG, "Launcher debug logs: ");
		for (int i = 0; i < sDumpLogs.size(); i++) {
			Log.d(TAG, "  " + sDumpLogs.get(i));
		}
		Log.d(TAG, "*********************");
		Log.d(TAG, "");
	}

	// AUROR-START:for workspace:xiejun
	public void setState(State state) {
		mState = state;
	}

	// TODO:xiejun
	/**
	 * @param toState
	 *            : the state of Launcher
	 * @param animated
	 *            :sign if use antimation
	 * @param onCompleteRunnable
	 *            : when the animation end process
	 */
	private void scaleModeofWorkspace(State toState, final boolean animated,
			final Runnable onCompleteRunnable,final Runnable  onCompleteStartRunnable,final EditMode mode) {

		if (mStateAnimation != null) {
			mStateAnimation.cancel();
			mStateAnimation = null;
		
		}
		Resources res = getResources();
		final View toView = mWorkspace;
		Animator workspaceAnim = null;
		Animator hotSeatAnimator=null;
		Animator widgetAnimator=null;
		Animator thumnailAnimator = null;
		Animator scrollviewAnimator = null;
		Animator classificateIconsAnimator = null;
		if (toState == State.WORKSPACE) {
			int stagger = res
					.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger);
		if(mWorkspace != null){
			workspaceAnim = mWorkspace.getChangeStateAnimation(
					Workspace.State.NORMAL, animated, stagger);
		 }
		} else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
			workspaceAnim = mWorkspace.getChangeStateAnimation(
					Workspace.State.SPRING_LOADED, animated);
		} else if (toState == State.SCALE_WORKSPACE) {
			workspaceAnim = mWorkspace.getChangeStateAnimation(
					Workspace.State.WORKSPACE_EDIT, animated);
		}
		
		if((mode==EditMode.APPWIDGET_ADD)){
			hotSeatAnimator=mHotseat.getAnimator(0.97F, true, true, animated, 350, null);
			widgetAnimator=getWidgetAnimator(animated, true, 400);
			if(hotSeatAnimator!=null){
				hotSeatAnimator.setInterpolator(new DecelerateInterpolator(2.5F));
			}
			if(widgetAnimator!=null){
				widgetAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			}
			if (getScrollIndicatorVisible() != -1 && getScrollIndicatorVisible() != View.VISIBLE) {
				thumbnailImageSetVisibility(View.GONE);
				setThumanailImageVisible(false);
				setScrollIndicatorVisible(true);
			}
		}else if(mode==EditMode.CLASSIFICATE_ICONS){
			hotSeatAnimator=mHotseat.getAnimator(0.97F, true, true, animated, 350, null);
			classificateIconsAnimator = getClassficateIconsAnim(animated, true, 400);
			if(hotSeatAnimator!=null){
				hotSeatAnimator.setInterpolator(new DecelerateInterpolator(2.5F));
			}
			if(classificateIconsAnimator!=null){
				classificateIconsAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			}
			if (getScrollIndicatorVisible() != -1 && getScrollIndicatorVisible() != View.VISIBLE) {
				thumbnailImageSetVisibility(View.GONE);
				setThumanailImageVisible(false);
				setScrollIndicatorVisible(true);
			}
			
		}else{
			if(mode==EditMode.NONE){
				hotSeatAnimator=mHotseat.getAnimator(1.0F, false, animated, false, 350, null);
				widgetAnimator=getWidgetAnimator(animated, false, 400);
				if(hotSeatAnimator!=null){
					hotSeatAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
				}
				if(widgetAnimator!=null){
					widgetAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
				}
				
				classificateIconsAnimator = getClassficateIconsAnim(animated, false, 400);
				if(hotSeatAnimator!=null){
					hotSeatAnimator.setInterpolator(new DecelerateInterpolator(2.5F));
				}
				
				if (delPageindicator() && mPageIndicator.getChildCount() > 1) {
					mPageIndicator.removePage(this.getWorkspace()
							.getChildCount() - 1);
				}
				if (mEditMode == EditMode.DRAG) {
					if(mStateAnimation1!=null){
						if(mStateAnimation1.isRunning()){
							mStateAnimation1.cancel();
						}
					}
					thumbnailImageSetVisibility(View.GONE);
					
					scrollviewAnimator = getThumnailScrollViewAnimator(
							animated, false, 300, null);
					if (scrollviewAnimator != null) {
						scrollviewAnimator
								.setInterpolator(new DecelerateInterpolator(
										1.5f));
						scrollviewAnimator
								.addListener(new AnimatorListenerAdapter() {
									@Override
									public void onAnimationEnd(
											Animator animation) {
										setThumanailImageVisible(false);
										setScrollIndicatorVisible(true);
									}
								});
					}
				}
			}else{
				
				hotSeatAnimator=mHotseat.getAnimator(0.97F, true, animated, false, 350, null);
				if(hotSeatAnimator!=null){
					hotSeatAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
				}
				
			}
		}
		setPivotsForZoom(toView, 2.0F);
		updateWallpaperVisibility(true);
		if (animated) {
			mStateAnimation = LauncherAnimUtils.createAnimatorSet();
			dispatchOnLauncherTransitionPrepare(toView, animated, true);
			mStateAnimation.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					Log.i("linp","mStateAnimation onAnimationEnd");
					updateWallpaperVisibility(true);
					dispatchOnLauncherTransitionEnd(toView, animated, true);
					if (onCompleteRunnable != null) {
						onCompleteRunnable.run();
					}
				}
				@Override
				public void onAnimationStart(Animator animation) {
					Log.i("xiejun7","onAnimationStart");
					super.onAnimationStart(animation);
					onCompleteStartRunnable.run();
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					// TODO Auto-generated method stub
					//AURORA-START::Fix bug #823::Shi guiqiang::20131118
					Log.i("xiejun7", "onAnimationCancel");
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							//AURORA-START::Fix bug #1892::Shi guiqiang::20131118
							if (mEditMode == EditMode.DRAG) {
								setScrollIndicatorVisible(false);
							} else {
								thumbnailImageSetVisibility(View.GONE);
								setThumanailImageVisible(false);
								setScrollIndicatorVisible(true);
							}
						}
					}, 20);
					
					super.onAnimationCancel(animation);
				}
			});
			if (workspaceAnim != null) {
				workspaceAnim.setInterpolator(new DecelerateInterpolator(1.0f));
				mStateAnimation.play(workspaceAnim);
			}
			if(hotSeatAnimator!=null){
				Log.i("xiejun8","hotSeatAnimator");
				mStateAnimation.play(hotSeatAnimator);
			}
			if(widgetAnimator!=null){
				Log.i("xiejun8","widgetAnimator");
				mStateAnimation.play(widgetAnimator);
			}
			if (thumnailAnimator != null) {
				mStateAnimation.play(thumnailAnimator);
			}
			if (scrollviewAnimator != null) {
				mStateAnimation.play(scrollviewAnimator);
			}
			if(classificateIconsAnimator != null){
				mStateAnimation.play(classificateIconsAnimator);
			}
			dispatchOnLauncherTransitionStart(toView, animated, true);
			final Animator stateAnimation = mStateAnimation;
		 if(mWorkspace != null){
			mWorkspace.post(new Runnable() {
				public void run() {
					if (stateAnimation != mStateAnimation)
						return;
					mStateAnimation.start();
				}
			});
		 }
		} else {
			onCompleteStartRunnable.run();
			dispatchOnLauncherTransitionPrepare(toView, animated, true);
			dispatchOnLauncherTransitionStart(toView, animated, true);
			dispatchOnLauncherTransitionEnd(toView, animated, true);
			onCompleteRunnable.run();
		}
	}
	
	public CellLayout createNewPage() {
		CellLayout newPage = (CellLayout) mInflater.inflate(R.layout.workspace_screen, mWorkspace, false);
		mWorkspace.addView(newPage);
		newPage.setOnLongClickListener(this);
		newPage.setOnClickListener(this);
		mWorkspace.notifyPageCountChange(-1,false);
		if(EditMode.NONE!=mEditMode && EditMode.QUICK_INDEX!=mEditMode){
			newPage.setScaleX(0.97f);
			newPage.setScaleY(0.97f);
			newPage.setBackgroundAlpha(1.0f);
		}
		Log.d("doubleicon","createNewPage: newPage = " + newPage);
		return newPage;
	}

	@Override
	public void onExitFullScreen() {
		ALog.i("oooo","onExitFullScreen");
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(params);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		//mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		
	}

	@Override
	public void onEnterFullScreen() {
		ALog.i("oooo","onEnterFullScreen");
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(params);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		//mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
	}

	public void setEditMode(EditMode mode) {
		Log.d("doubleicon","setEditMode: new mode = " + mode + ", old mode = " + mEditMode);
		mEditMode = mode;
	}


	// Aurora <haojj> <2013-09-12> add for initing begin
	public EditMode getEditMode() {
		return mEditMode;
	}
    
	public void reverseNormal(){
		LauncherApplication.logVulcan.print("reverseNormal: to call exitEditMode(true)!");
		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				exitEditMode(true);
			}
		}, 500); 
	}
	// Aurora <haojj> <2013-09-12> add for initing end
	private boolean mEnterAnimatorFinish=true;
	private boolean mExitAnimatorFinish=true;
	private Runnable mEnterStartRunnable=new Runnable() {
		@Override
		public void run() {
			Log.i("xiejun7","----------------mEnterAnimatorStart-----------------------");
			mEnterAnimatorFinish=false;
		}
	};
	private Runnable mEnterFinishRunnable=new Runnable() {
		@Override
		public void run() {
			Log.i("xiejun7","----------------mEnterAnimatorFinish-----------------------");
			mEnterAnimatorFinish=true;
			mHandler.removeCallbacks(mEnterDelayRunnable);
		}
	};
	private Runnable mExitStartRunnable=new Runnable() {
		@Override
		public void run() {
			LauncherApplication.logVulcan.print("closeFolder: to call mExitStartRunnable!");
			LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
			Log.i("xiejun7","----------------mExitAnimatorStart-----------------------");
			mExitAnimatorFinish=false;
			mHandler.removeCallbacks(mExitDelayRunnable);
		}
	};
	
	private void animateShowFolderIcon(){
		if (mEditFolderIcon != null && mEditFolderIcon.getAlpha() < 1.0f) {
			PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", 1f);
			ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(
					mEditFolderIcon, alphaHolder);
			oa.setDuration(100);
			oa.setInterpolator(new DecelerateInterpolator());
			oa.start();
		}
	}

	private Runnable mExitFinishRunnable=new Runnable() {
		@Override
		public void run() {
			Log.i("xiejun7","----------------mExitAnimatorFinish--------:isExitFullScreen="+isExitFullScreen);
			if (mEditMode == EditMode.FOLDER_IMPORT) {
				// Aurora <haojj> <2013-10-15> add for 在退出Import模式之前清除 begin
				// clearShortcutToFolder();
				// showFolderIcon();
				// Aurora <haojj> <2013-10-15> end
				// enterFolderImportEditMode(false);
				animateShowFolderIcon();
			}
			setEditMode(EditMode.NONE);
			setState(State.WORKSPACE);
			if(mNeedAutoManagePagesWhenExited){
				// autoManangePages();
				LauncherApplication.logVulcan.print("mExitFinishRunnable: to call autoManagePagesImediatly");
				autoManagePagesImediatly();
			}
			//openFolderAfterExitEditMode(mOpenFolderIcon);
			if(!mFromFolderDrop&&!isExitFullScreen&&getTransModeEnum()==TransModeEnum.NONE){
		    	onExitFullScreen();
			}
			isExitFullScreen=false;
			mFromFolderDrop=false;
			mExitAnimatorFinish=true;	
		}
	};
	
	private  boolean mFromFolderDrop = false;
	public void setFromFolderDrop(boolean b){
		mFromFolderDrop = b;
	}
	
	Runnable mEnterDelayRunnable=new Runnable() {
		@Override
		public void run() {
			Log.i("xiejun7","*********************mEnterDelayRunnable********************");
			mEnterAnimatorFinish = true;
		}
	};
	
	public void enterEditMode(final EditMode mode,boolean animated) {
		Log.i("xiejun7","1:enterEditMode mode="+mode);
		mWidgetViewLayout.setVisibility(View.VISIBLE);
		mClassficateIonsLayout.setVisibility(View.VISIBLE);
		if(!mEnterAnimatorFinish)return;
		mEnterAnimatorFinish=false;
		//mWorkspace.setCelllayoutSEditbackgroud(true,animated);
		scaleModeofWorkspace(State.SCALE_WORKSPACE, animated, mEnterFinishRunnable,mEnterStartRunnable,mode);
		setState(State.SCALE_WORKSPACE);
		setEditMode(mode);
		if (mode == EditMode.FOLDER_IMPORT) {
			enterFolderImportEditMode(true);
		}
			mHandler.postDelayed(mEnterDelayRunnable, 600);
	}
	
	Runnable mExitDelayRunnable=new Runnable() {
		@Override
		public void run() {
			Log.i("xiejun7","*********************mExitDelayRunnable********************");
			mExitAnimatorFinish = true;
		}
	};
	
	//iht 2015-02-03 此方法提供给动画日历使用，若true则日历动画不播放；
	public boolean currentTransModeEnum(){
		if(TransMode == TransModeEnum.VOICE || TransMode == TransModeEnum.APPS_INDEX){
			return true;
		}
		return false;
	}
	
	public void exitEditMode(boolean animated) {
		Log.i("xiejun7","mExitAnimatorFinish="+mExitAnimatorFinish);
		mFromAuroraWidget = false;
		if(!mExitAnimatorFinish)return;	
		mExitAnimatorFinish=false;
		Log.i("xiejun7","2:exitEditMode mode="+mEditMode);
		exitEditModeAfterOpenFolder(animated);
	}
	
	private boolean mFromAuroraWidget;
	public boolean ismFromAuroraWidget() {
		return mFromAuroraWidget;
	}
	
	//桌面退出编辑模式之前，特殊Icon(从Hotseat进入Desktop，其父layout为null)应加以处理,防止图标丢失；
	//iht 2014-10-29 
	private void beforeExitEditMode(){
		if(mAuroraDragView != null){
			ItemInfo info = (ItemInfo)mAuroraDragView.getTag();
			mWorkspace.resetAuroraChild(mAuroraDragView, false); 
			if (mAuroraDragView instanceof FolderIcon) {
				((FolderIcon) mAuroraDragView).setTextVisible(true);
			} else if(mAuroraDragView instanceof BubbleTextView){
				((BubbleTextView) mAuroraDragView).setTextColor(
						getResources().getColor(R.color.workspace_icon_text_color));
			}
			CellLayout parent = mWorkspace.getParentCellLayoutForView(mAuroraDragView);
			if (parent != null) {
				parent.markCellsAsOccupiedForView(mAuroraDragView);
			}
		}
	}


	public void exitEditMode(boolean animated, boolean workspace, boolean unNormal) {
		//AURORA_START: ht-2014-10-29 fix BUG#9156
		if(workspace && unNormal){
			beforeExitEditMode();
			//AURORA_START iht-2015-02-02 fix BUG#11288/若应用已经被删除，则无须还原；
			/*if(checkAppUninstall(mAuroraUninstallPkgName)){//该应用是否已经被删除
				beforeExitEditMode();
			}*/
			//AURORA_END
		}
		//AURORA_END: ht-2014-10-29 fix BUG#9156
		
		//AURORA_START: ht-2014-10-29 fix BUG#9158
		if(mPageIndicatotorTextView != null && mPageIndicatotorTextView.getAlpha() > 0){
			setPageIndicatotorTextViewAlpha(0.0f);
		}
		//AURORA_END: ht-2014-10-29 fix BUG#9158
		
		if(!mExitAnimatorFinish)return;
		exitEditModeAfterOpenFolder(animated);
	}
	private void executeExitEditMode(boolean animated){
		if (mEditMode == EditMode.FOLDER_IMPORT) {
			clearShortcutToFolder();
			enterFolderImportEditMode(false);
		}
		scaleModeofWorkspace(State.WORKSPACE, animated,	mExitFinishRunnable, mExitStartRunnable,EditMode.NONE);
		mHandler.postDelayed(mExitDelayRunnable, 600);
	}
	
	public void exitEditModeAfterOpenFolder(final boolean animated){
		if(mOpenFolderIcon && mEditFolderIcon != null ){
			int destScreen = mEditFolderIcon.getFolderInfo().screen;
			int srcScreen = mWorkspace.getCurrentPage();
			if(destScreen != srcScreen && mEditFolderIcon.getParent() != null 
					&& !isHotseatLayout((View)mEditFolderIcon.getParent().getParent())){
				int duration = 200; 
				mWorkspace.snapToPage(destScreen, duration, new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mFromFolderDrop = true;
						executeExitEditMode(false);
						handleFolderClick(mEditFolderIcon);
						mEditFolderIcon = null;
					}
				});
			} else {
				mFromFolderDrop = true;
				executeExitEditMode(false);
				handleFolderClick(mEditFolderIcon);
				mEditFolderIcon = null;
			}
			mOpenFolderIcon=false;
		} else {
			executeExitEditMode(animated);
		}
	}
	@Override
	public void OnResponsePintchEvent() {
		//To be defined	
	}
	
	@Override
	public void AddWidgetModeEnter() {
		this.enterEditMode(EditMode.APPWIDGET_ADD,true);
		int count=getWorkspace().getChildCount();
		
		//AURORA_START: iht 2014-12-10
		if(mWorkspace != null){
			mWorkspace.before_edit_pagesCount = count;
		}
		//AURORA_END
		
		View itemcontainer = getWorkspace().getChildAt(count - 1);
		if(itemcontainer instanceof CellLayout){
			if((((CellLayout) itemcontainer).getShortcutsAndWidgets().getChildCount()) > 0){
				createNewPage();
			}
		}
	}
	@Override
	public void AddWidgetModeExit() {
		LauncherApplication.logVulcan.print("AddWidgetModeExit: to call exitEditMode(true)!");
		LauncherApplication.logVulcan.print(LogWriter.StackToString(new Throwable()));
		this.exitEditMode(true);
	}
	
	public void scaleHotSeat(float fact,boolean scale){
		mHotseat.scale(fact, scale);
	}
	
	public void enterFolderImportEditMode(boolean enable) {
		// 导入时添加和退出动画逻辑
		// (1) mFolderImportHintContainer及mFolderImportButtonContainer的可见性在animateCloseFolder中实现
		// (2) mHotseat可见在打开文件夹后实现
		// (3) 退出编辑模式的动画在些实现
		if (enable) {
			// 此处将hotseat设为不可见
			// mFolderImportHintContainer.setVisibility(View.VISIBLE);
			// mFolderImportButtonContainer.setVisibility(View.VISIBLE);
			mHotseat.setVisibility(View.GONE);
			onEnterFullScreen();
		} else {
			// 此处添加了导入模式时的退出动画
			//mFolderImportHintContainer.setVisibility(View.GONE)
			//mFolderImportButtonContainer.setVisibility(View.GONE);
			animateExitFolderImportHint(500);
			animateExitFolderImportButton(100);
			// 此处暂缓打开mHotseat，因为在openfolder的时候才调用，这样效果更好些
		}
	}

	private void animateExitFolderImportHint(long time){
		if(mFolderImportHintContainer != null){
			int height = mFolderImportHintContainer.getHeight();
			PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("translationY", 0, -height);
			ObjectAnimator oa =
	                LauncherAnimUtils.ofPropertyValuesHolder(mFolderImportHintContainer, holder);
	    	oa.setInterpolator(new DecelerateInterpolator());
	    	oa.addListener(new AnimatorListenerAdapter() {
	    		@Override
	    		public void onAnimationEnd(Animator animation) {
	    			// TODO Auto-generated method stub
	    			super.onAnimationEnd(animation);
	    			mFolderImportHintContainer.setVisibility(View.GONE);
	    			mFolderImportHintContainer.setTranslationY(0f);
	    		}
	    	});
	    	oa.setDuration(time);
	    	oa.start();
		}
	}
	
	private void animateExitFolderImportButton(long time){
		final boolean openFolderIcon = mOpenFolderIcon;
		if(mFolderImportButtonContainer != null){
			int height = mFolderImportButtonContainer.getHeight();
			PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("translationY", 0, height);
			ObjectAnimator oa =
	                LauncherAnimUtils.ofPropertyValuesHolder(mFolderImportButtonContainer, holder);
	    	oa.setInterpolator(new DecelerateInterpolator());
	    	oa.addListener(new AnimatorListenerAdapter() {
	    		@Override
	    		public void onAnimationEnd(Animator animation) {
	    			super.onAnimationEnd(animation);
	    			mFolderImportButtonContainer.setVisibility(View.GONE);
	    			mFolderImportButtonContainer.setTranslationY(0f);	
	    			if(!openFolderIcon) {
		    			Animator animator = animateResetHoseat();
		    			if (animator != null)
		    				animator.start();
	    			}
	    		}
	    	});
	    	oa.setDuration(time);
	    	oa.start();
	    
		}
	}	
	private Animator animateResetHoseat() {
		ObjectAnimator oa = null;
		if (mHotseat != null) {
			final int height = mHotseat.getHeight();
			PropertyValuesHolder holder = PropertyValuesHolder.ofFloat(
					"translationY", height, 0);
			oa = LauncherAnimUtils.ofPropertyValuesHolder(mHotseat, holder);
			oa.setInterpolator(new DecelerateInterpolator(1.5F));
			oa.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					mHotseat.setVisibility(View.VISIBLE);
				}
			});
			oa.setDuration(250);
		}
		return oa;
	}
	
	private Animator animateEnterFolderImportHint(){
		ObjectAnimator oa = null;
		if(mFolderImportHintContainer != null){
			final int height = mFolderImportHintContainer.getHeight();
			PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("translationY", -height, 0);
			oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderImportHintContainer, holder);
	    	oa.setInterpolator(new DecelerateInterpolator());
	    	oa.addListener(new AnimatorListenerAdapter() {
	    		@Override
	    		public void onAnimationStart(Animator animation) {
	    			super.onAnimationStart(animation);
	    			mFolderImportHintContainer.setVisibility(View.VISIBLE);
	    			mFolderImportHintContainer.setTranslationY(-height);
	    			
	    		}
	    	});
	    	oa.setDuration(250);
		}
		return oa;
	}
	
	private Animator animateEnterFolderImportButton(){
		ObjectAnimator oa = null;
		if(mFolderImportButtonContainer != null){
			final int height = mFolderImportButtonContainer.getHeight();
			PropertyValuesHolder holder = PropertyValuesHolder.ofFloat("translationY", height, 0);
			oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderImportButtonContainer, holder);
	    	oa.setInterpolator(new DecelerateInterpolator());
	    	oa.addListener(new AnimatorListenerAdapter() {
	    		@Override
	    		public void onAnimationStart(Animator animation) {
	    			// TODO Auto-generated method stub
	    			super.onAnimationStart(animation);
	    			mFolderImportButtonContainer.setVisibility(View.VISIBLE);
	    			mFolderImportButtonContainer.setTranslationY(height);
	    		}
	    	});
	    	oa.setDuration(250);
		}
		return oa;
	}
	
	// Aurora <haojj> <2013-09-09> add for show or dismiss folder background
    // 获取指定Activity的截屏，保存到png文件
	private Bitmap getScreenBitmap() {
		// View是你需要截图的View
		View view = getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		// 去掉标题栏
		Bitmap b = Bitmap.createBitmap(b1, 0, mStatusBarHeight, mScreenWidth, mScreenHeight - mStatusBarHeight);
		view.destroyDrawingCache();
		
		if(!b1.isRecycled()) b1.recycle(); 
		return b;
	}
	
    public void showFolderBgBlurView2(){
    	if(mFolderBgBlurView != null) {
			Bitmap mScreenBitmap = GnSurface.screenshot(mScreenWidth, mScreenHeight);
			if(mScreenBitmap == null) {
				mFolderBgBlurView.setBackgroundResource(R.drawable.folder_backgroud);
				mFolderBgBlurView.setVisibility(View.VISIBLE);
				return ;
			}
			Bitmap newImg = Blur.fastblur(this, small(mScreenBitmap), 20);
			if (!mScreenBitmap.isRecycled()){
				mScreenBitmap.recycle();
			}
			if(newImg == null){
				mFolderBgBlurView.setBackgroundResource(R.drawable.folder_backgroud);
			} else {
				mFolderBgBlurView.setBackground(new BitmapDrawable(newImg));
			}
			mFolderBgBlurView.setVisibility(View.VISIBLE);
		}
    }
	
    public void showFolderBgBlurView(){
    	if(mFolderBgBlurView != null) {
			Bitmap 	mScreenBitmap = GnSurface.screenshot(getWorkspace().getWidth(), getWorkspace().getHeight());
			if(mScreenBitmap == null) {
				mFolderBgBlurView.setBackgroundResource(R.drawable.folder_backgroud);
				mFolderBgBlurView.setVisibility(View.VISIBLE);
				return ;
			}
			mScreenBitmap = magnifyBitmap(mScreenBitmap, 0.1f, 0.1f);
			Bitmap newImg = Blur.fastblur(this, mScreenBitmap, 23);
			if (!mScreenBitmap.isRecycled()){
				mScreenBitmap.recycle();
			}
			if(newImg == null){
				mFolderBgBlurView.setBackgroundResource(R.drawable.folder_backgroud);
			} else {
				mFolderBgBlurView.setBackground(new BitmapDrawable(newImg));
			}
			mFolderBgBlurView.setVisibility(View.VISIBLE);
		}
    }
    
    public void showQuickIndex(){
    	animateShowBlurViewBg(300, new DecelerateInterpolator()).start();
    	mQuickIndexLayout.setBackground(null);
    	mQuickIndexTitle.setVisibility(View.VISIBLE);
		mQuickIndexLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    
    public void dismissQuickIndex(){
    	onExitFullScreen();
    	dismissBlurView();
    	dismissBothBlurView();
    }
    
    public void showQuickIndexBgBlurView(){
    	if(mQuickIndexLayout != null) {
			Bitmap mScreenBitmap = GnSurface.screenshot(108, 192);
			if(mScreenBitmap == null) {
				mQuickIndexLayout.setBackgroundResource(R.drawable.folder_backgroud);
				mQuickIndexLayout.setVisibility(View.VISIBLE);
				return ;
			}
			Bitmap newImg = Blur.fastblur(this, mScreenBitmap, 23);
			if (!mScreenBitmap.isRecycled()){
				mScreenBitmap.recycle();
			}
			mQuickIndexLayout.setBackground(null);
			mQuickIndexLayout.setVisibility(View.VISIBLE);
			mQuickIndexLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
    }
    public void showQuickIndexBothBgBlurView(boolean showQuickIndex){
    	if(mQuickIndexBgBlurView != null) {
    		int width = getWorkspace().getWidth()/10;
    		int height = getWorkspace().getHeight()/10;
    		Bitmap mScreenBitmap = GnSurface.screenshot(width, height);	
			if(mScreenBitmap == null) {
				mQuickIndexBgBlurView.setBackgroundResource(R.drawable.folder_backgroud);
				mQuickIndexBgBlurView.setVisibility(View.VISIBLE);
				mAppSearchBgImageView.setBackgroundResource(R.drawable.folder_backgroud);
				mAppSearchBgImageView.setVisibility(View.VISIBLE);
				return ;
			}
			Bitmap newImg = Blur.fastblur(this, mScreenBitmap, 23);
			if (!mScreenBitmap.isRecycled()){
				mScreenBitmap.recycle();
			}	
			int lowerheight = (int)(height*0.139f);
			Bitmap bg_upper = Bitmap.createBitmap(newImg, 0, 0, width, height - lowerheight);
			Bitmap bg_lower = Bitmap.createBitmap(newImg, 0, height - lowerheight, width, lowerheight);
			int lowerheight2 = (int)(height*0.145f);	//91dp-->27
			Bitmap bg_shadow = Bitmap.createBitmap(newImg, 0, height - lowerheight2 - lowerheight, width, lowerheight2);
			// @贵强原来定义的渐变方案，我进行修正如下，分成两段，一段为字母表占据的宽约33dp哪块，这段在字母表的下面
			// bg_shadow = setLinearAlpha(bg_shadow, 0);
			Bitmap[] bgArray = new Bitmap[2];
			setLinearAlpha(bg_shadow, 0, bgArray, showQuickIndex);
			bg_shadow = bgArray[0];
			// 结束渐变操作
			if (!newImg.isRecycled()) {
				newImg.recycle();
			}
			
			if(bg_upper == null){
				mQuickIndexBgBlurView.setBackgroundResource(R.drawable.folder_backgroud);
			} else {
				mQuickIndexBgBlurView.setBackground(new BitmapDrawable(getResources(),bg_upper));
			}
			if(bg_lower == null){
				mAppSearchBgImageView.setBackgroundResource(R.drawable.folder_backgroud);
			} else {
				mAppSearchBgImageView.setBackground(new BitmapDrawable(getResources(), bg_lower));
			}
			if(bg_shadow == null){
				mShadowImageView.resetMarginValue(true);
				// 下面这段有问题
				mShadowImageView.setBackgroundResource(R.drawable.folder_backgroud);
				mUpperShadowImageView.setVisibility(View.INVISIBLE);
			} else {
				if(showQuickIndex && bgArray[1] != null){
					mShadowImageView.resetMarginValue(false);
					mShadowImageView.setBackground(new BitmapDrawable(getResources(), bg_shadow));
					mUpperShadowImageView.setBackground(new BitmapDrawable(getResources(), bgArray[1]));
					mUpperShadowImageView.setVisibility(View.VISIBLE);
				} else {
					mShadowImageView.resetMarginValue(true);
					mShadowImageView.setBackground(new BitmapDrawable(getResources(), bg_shadow));
					mUpperShadowImageView.setVisibility(View.INVISIBLE);
				}
			}
			//mQuickIndexBgBlurView.setVisibility(View.VISIBLE);
			//mAppSearchBgImageView.setVisibility(View.VISIBLE);
			//mShadowImageView.setVisibility(View.VISIBLE);
		}
    }
    
    public void startTask(){
    	Bitmap[] bitmapArray= processBgBlurView(true);
		LoadingQuickBlurBitmap(true,bitmapArray);
//	    new blurThread().start();
    	
    }
     

    
    /***
     * m[0] bg_upper
     * m[1] bg_lower
     * m[2] bg_shadow
     * */
    
	public Bitmap[]  processBgBlurView(boolean showQuickIndex){
		long starttime  = System.currentTimeMillis();
    	int width = getWorkspace().getWidth()/10;
		int height = getWorkspace().getHeight()/10;
		
		Bitmap mScreenBitmap = null;
		try {
			mScreenBitmap = GnSurface.screenshot(width, height);
			//mScreenBitmap = magnifyBitmap(mScreenBitmap, 0.1f, 0.1f);
		} catch (NoClassDefFoundError e) {
			// TODO: handle exception
			Log.e("linp", "GnSurface Class may not found");
			/**wallpaper_01 just for test.you can change the one you like it*/
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 10;
			mScreenBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.wallpaper_01, opts);
		}
		
		//M:shigq fix bug #15143 begin
		catch (NullPointerException e) {
			// TODO: handle exception
			Log.e("linp", "GnSurface NullPointerException");
			/**wallpaper_01 just for test.you can change the one you like it*/
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 10;
			mScreenBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.wallpaper_01, opts);
		}
		//M:shigq fix bug #15143 end
		
		/**scale the screenshot image then restore the img width and height*/
/*		int scaleWidth = (int)(mScreenBitmap.getWidth()/1.5f);
		int scaleHeight =  (int)(mScreenBitmap.getHeight()/1.5f);
		Log.e("linp", "@@@scaleWidth="+scaleWidth+","+"scaleHeight="+scaleHeight);
		Bitmap a = proccessBitmap(mScreenBitmap,scaleWidth,scaleHeight);
		Bitmap newImg = Blur.fastblur(this, a, 16);
		newImg = proccessBitmap(newImg,mScreenBitmap.getWidth(),mScreenBitmap.getHeight());*/
			
		Bitmap newImg = Blur.fastblur(this, mScreenBitmap, 23);

		//Log.e("linp","################processBgBlurView time is="+String.valueOf(System.currentTimeMillis() - starttime));
		
		if (!mScreenBitmap.isRecycled()){
			mScreenBitmap.recycle();
		}	
		
		float upperOffset = getFloatValueFromResourcesDimens(R.dimen.quick_index_background_background_blur_shawdow_upper_offset);
		//Log.e("linp","getValueFromResourcesDimens result upperOffset="+upperOffset);
		int lowerheight = (int)(height*upperOffset); // U2: 192*89/640= 26.7 / 192 =0.139f 
		//Log.e("linp", "-----------------lowerheight="+lowerheight);
		Bitmap bg_upper = Bitmap.createBitmap(newImg, 0, 0, width, height - lowerheight);
		Bitmap bg_lower = Bitmap.createBitmap(newImg, 0, height - lowerheight, width,lowerheight);
	
		float shadowOffset = getFloatValueFromResourcesDimens(R.dimen.quick_index_background_background_blur_shawdow_offset);
		//Log.e("linp","getValueFromResourcesDimens result shadowOffset="+shadowOffset)  ;
		
		int lowerheight2 = (int)(height*shadowOffset);	//91dp-->27 27 = 192*91/640 (For:80dp: 256*80/853 =24 )
		//Log.e("linp", "-----------------lowerheight2="+lowerheight2);
	
		Bitmap bg_shadow = Bitmap.createBitmap(newImg, 0, height -lowerheight2- lowerheight, width,lowerheight2);
		//Bitmap bg_shadow = Bitmap.createBitmap(newImg, 0, height - lowerheight2- lowerheight, width,lowerheight2);
		// @贵强原来定义的渐变方案，我进行修正如下，分成两段，一段为字母表占据的宽约33dp哪块，这段在字母表的下面
		// bg_shadow = setLinearAlpha(bg_shadow, 0);
	    bgArray = new Bitmap[2];
		setLinearAlpha(bg_shadow, 0, bgArray, showQuickIndex);
		bg_shadow = bgArray[0];
		// 结束渐变操作
		if (!newImg.isRecycled()) {
			newImg.recycle();
		}
		blurImage[0] = bg_upper;
		blurImage[1] = bg_lower;
		blurImage[2] =bg_shadow;

		return blurImage;
    }
	
	
	private Bitmap proccessBitmap (Bitmap org,int w,int h){
		// 获得图片的宽高
				int width1 = org.getWidth();
				int height1 = org.getHeight();
				// 设置想要的大小
				int newWidth = w;
				int newHeight = h;
				// 计算缩放比例
				float scaleWidth = ((float) newWidth) / width1;
				float scaleHeight = ((float) newHeight) / height1;
				// 取得想要缩放的matrix参数
				Matrix matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);
				// 得到新的图片
				Bitmap newbm = Bitmap.createBitmap(org, 0, 0, width1, height1, matrix,
						true);
				return newbm;
	}
    
	public void LoadingQuickBlurBitmap(boolean showQuickIndex, Bitmap[] m1) {
		mQuickIndexBgBlurView.setBackground(new BitmapDrawable(
					getResources(), m1[0]));

			mAppSearchBgImageView.setBackground(new BitmapDrawable(
					getResources(), m1[1]));
/*			mQuickIndexBgBlurView.setBackgroundColor(Color.CYAN);
			mAppSearchBgImageView.setBackgroundColor(Color.RED);*/
			if (showQuickIndex && bgArray[1] != null) {
				mShadowImageView.resetMarginValue(false);
			mShadowImageView.setBackground(new BitmapDrawable(
						getResources(), m1[2]));
				mUpperShadowImageView.setBackground(new BitmapDrawable(
						getResources(), bgArray[1]));
		}
	}

    public void show(TransModeEnum mode){
    	if (mode == TransModeEnum.VOICE) {
			mShadowImageView.setBackground(null);
			mShadowImageView.setVisibility(View.GONE);
			mShadowImageView.setAlpha(0.0f);
		} else {
			mShadowImageView.setVisibility(View.VISIBLE);
			mShadowImageView.setAlpha(1.0f);
			//mShadowImageView.setBackgroundColor(Color.BLUE);
		}
    	mQuickIndexBgBlurView.setVisibility(View.VISIBLE);
		mAppSearchBgImageView.setVisibility(View.VISIBLE);
		mQuickIndexBgBlurView.setAlpha(1.0f);
		mAppSearchBgImageView.setAlpha(1.0f);
		
    }
    
    public void dismissBothBlurView(){
		if(mQuickIndexBgBlurView != null){
			mQuickIndexBgBlurView.setVisibility(View.GONE);
			mQuickIndexBgBlurView.setBackground(null);
		}
		if(mAppSearchBgImageView != null){
			mAppSearchBgImageView.setVisibility(View.GONE);
			mAppSearchBgImageView.setBackground(null);
		}
		if(mShadowImageView != null){
			mShadowImageView.setVisibility(View.GONE);
			mShadowImageView.setBackground(null);
		}
		if(mUpperShadowImageView != null){
			mUpperShadowImageView.setVisibility(View.GONE);
			mUpperShadowImageView.setBackground(null);
		}
	}
	
	private static Bitmap big(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(1.5f, 1.5f); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}

	private static Bitmap small(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(0.09f, 0.09f); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}

	public void dismissBlurView(){
		if(mFolderBgBlurView != null){
			mFolderBgBlurView.setVisibility(View.INVISIBLE);
        	mFolderBgBlurView.setBackground(null);
		}
	}
	void startAuroraApplicationUninstallActivity(ComponentName cn, int flags) {
		Thread.dumpStack();
		if ((flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
			// System applications cannot be installed. For now, show a toast
			// explaining that.
			// We may give them the option of disabling apps this way.
			int messageId = R.string.uninstall_system_app_text;
			Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
			LauncherApplication.logVulcan.print("startAuroraApplicationUninstallActivity: deleting system app,but failed");
			resetAuroraUninstallData();
		} else {
			mAuroraUninstallPkgName = cn.getPackageName();
			Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts(
					"package", cn.getPackageName(), cn.getClassName()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            mIsUninstallShow = true;
			startActivity(intent);
		}
	}
	
	@Override
	public void bindAuroraAppsAdded(ShortcutInfo info) {
		Log.d("vulcan-contact","bindAuroraAppsAdded: info = " + info.title);
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		items.add(info);
		bindItems(items, 0, 1);
		//bindAppsAndWriteBack(items, 0, 1);
        addQuickIndexItem(info);
		LauncherModel.addItemToDatabase(this, info,LauncherSettings.Favorites.CONTAINER_DESKTOP, info.screen,
				info.cellX, info.cellY, false);
		mOnResumeNeedsLoad = false;
	}

	@Override
	public void bindAuroraAppsUpdated(ShortcutInfo info) {
		ShortcutAndWidgetContainer child = null;
		View view;
		boolean inFolder = false;
		if(info.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			if(mWorkspace.getChildAt(info.screen) == null) {
				return;
			}
			child = ((CellLayout) mWorkspace.getChildAt(info.screen)).getShortcutsAndWidgets();
			
		} else if(info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT){
			child = (ShortcutAndWidgetContainer) getHotseat().getLayout().getShortcutsAndWidgets();
		} else {
			inFolder = true;
			for(FolderInfo fInfo : mModel.sBgFolders.values()) {
				if(fInfo.id == info.container) {
					Log.i(TAG, fInfo.toString());
					fInfo.updateShortcutItem(info);
					break;
				}
			}
		}
		if (!inFolder) {
			view = child.getChildAt(info.cellX, info.cellY);
			final Intent intent = info.intent;
	        final ComponentName name = intent.getComponent();
	        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
	                Intent.ACTION_MAIN.equals(intent.getAction()) && name != null && view != null) {
	        	BubbleTextView shortcut = (BubbleTextView) view;
	            info.updateIcon(mIconCache);
	            info.title = info.title.toString();
	            shortcut.applyFromShortcutInfo(info, mIconCache);
	        }
		}
        mOnResumeNeedsLoad = false;
        // Aurora <haojj> <2014-1-11> add for 快速检索需要同步更新 begin
        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
        	updateQuickIndexItems(info);
        }
		// Aurora <haojj> <2014-1-11> end
	}	
	// Aurora <jialf> <2013-10-02> add for dismiss folder begin
	public void dissolveAuroraFolder(FolderInfo fInfo) {
		int[] loc = mModel.findLastPostionForDismissFolder();
		int screen = -1;
		int cellX,cellY;
		// view == null means that the last screen has no empty cell,
		// so we need create a new screen
		if (loc != null && loc[0] != -1 && loc[1] != -1) {
			int count = mWorkspace.getChildCount();
			CellLayout cLayout = (CellLayout) mWorkspace.getChildAt(count - 1);
			if (cLayout.getShortcutsAndWidgets().getChildCount() == 0
					&& count > 1) {
				screen = count - 2;
			} else {
				screen = count - 1;
			}
			// screen = mWorkspace.getChildCount() - 2;
			cellX = loc[0];
			cellY = loc[1];
		} else {
			screen = mWorkspace.indexOfChild(createNewPage());
			cellX = 0;
			cellY = 0;
		}
		Log.i(TAG, "cellX = " + cellX + ", cellY = " + cellY + ",screen = "
				+ screen + ",id = " + fInfo.id);
		int countX = LauncherModel.getCellCountX();
		int countY = LauncherModel.getCellCountY();
		
		final int emptyCount = countX * (countY - cellY - 1)
				+ (countX - cellX);
		Cursor cursor = mModel.queryItemInfoInFolder(fInfo.id);
		if(cursor == null) return;
		// Aurora <jialf> <2013-12-19> modify for fix bug #1341 begin
		long container = mModel.queryAuroraFolderContainer(fInfo.id);
		if(container != LauncherSettings.Favorites.CONTAINER_HOTSEAT){
			CellLayout cellLayut = (CellLayout) mWorkspace.getChildAt(fInfo.screen);
			
			//M:shigq fix bug15697 start
			if (cellLayut == null) return;
			//M:shigq fix bug15697 end
			
			cellLayut.removeView(cellLayut.getChildAt(fInfo.cellX, fInfo.cellY));
		}
		// Aurora <jialf> <2013-12-19> modify for fix bug #1341 end
		int idIndex = cursor.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
		ShortcutInfo sInfo;
		mWorkspace.cleanFolderItems();
		while (cursor.moveToNext()) {
			long itemId = cursor.getLong(idIndex);
			sInfo = (ShortcutInfo) mModel.sBgItemsIdMap.get(itemId);
			sInfo.container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
			if (cellX == 0 && cellY == 0) {
				sInfo.cellX = cellX;
				sInfo.cellY = cellY;
			} else {
				if (cellX > LauncherModel.getCellCountX() - 1) {
					cellX = 0;
					cellY++;
					if (cellY > LauncherModel.getCellCountY() - 1) {
						screen = mWorkspace.indexOfChild(createNewPage());
						cellX = 0;
						cellY = 0;
					}
				}
				sInfo.cellX = cellX;
				sInfo.cellY = cellY;
			}
			sInfo.screen = screen;
			View shortcut = createShortcut(sInfo);
			mWorkspace.addAuroraFolderItems(shortcut);
			mWorkspace.addInScreen(shortcut, sInfo.container, screen, cellX, cellY, 1, 1);
			shortcut.setVisibility(View.GONE);
			LauncherModel.modifyItemInDatabase(this, sInfo, sInfo.container,screen, cellX, cellY, 1, 1);
			cellX++;
		} 
		cursor.close();
		mWorkspace.filterValues(emptyCount);
		LauncherModel.deleteFolderContentsFromDatabase(this, fInfo);
		
		//iht 2014-11-29
		mWorkspace.snapToPage(mWorkspace.getChildCount() -1);
		mWorkspace.requestLayout();
	}
	
	private void alignIcons(){
		LauncherModel.enableInstallQueue();
		int i,j,size, childIndex;
		CellLayout curLayout = (CellLayout) mWorkspace.getChildAt(mWorkspace.getCurrentPage());
		ArrayList<View> allViews = new ArrayList<View>();
		ArrayList<View> alignViews = new ArrayList<View>();
		HashSet<Integer> ySet = new HashSet<Integer>();
		if(curLayout == null) return;		
		// 获取所有的要变化的view
		for(i=0; i < curLayout.getCountY(); i++){
			for(j=0; j < curLayout.getCountX(); j++){
				View v = curLayout.getChildAt(j, i);
				if(null == v) continue;
				ItemInfo info = (ItemInfo)v.getTag();
				if(info instanceof LauncherAppWidgetInfo){
					int widgetCellY = info.cellY;
					int widgetSpanY = info.spanY;
					for(int k = widgetCellY; k < widgetCellY + widgetSpanY ; k++){
						ySet.add(k);
					}
				} else {
					allViews.add(v);
				}
			}
		}
		// widget所在行的view不变化
		size = allViews.size();
		for(i=0;i < size; i++){
			ItemInfo info = (ItemInfo) allViews.get(i).getTag();
			ALog.i(TAG,"info.toString="+info.toString());
			if(!ySet.contains(info.cellY)){
				alignViews.add(allViews.get(i));
			}    
		} 
		
		allViews.clear();
		// 位置重排并保存到数据库
		childIndex = 0;
		size = alignViews.size();
		ALog.i(TAG, "alignViews.size()="+size);
		
		for(i=0;i < curLayout.getCountY();i++){
			if(ySet.contains(i)) continue;
			for(j=0;j < curLayout.getCountX();j++){
				if(childIndex >= size) {
					break;
				}
				View child = alignViews.get(childIndex++);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				ItemInfo item = (ItemInfo) child.getTag();
				if(item.cellX != j || item.cellY != i){
					curLayout.removeView(child);
					lp.cellX = item.cellX = j;
					lp.cellY = item.cellY = i;
					lp.cellHSpan = item.spanX;
					lp.cellVSpan = item.spanY;
					lp.useTmpCoords = false;
					mWorkspace.addInScreen(child, item.container, item.screen,
							item.cellX, item.cellY, 1, 1, false);
					LauncherModel.moveItemInDatabase(this, item, item.container, item.screen,
							item.cellX, item.cellY);
				} 
			}
		}
		
		ySet.clear();
		alignViews.clear();
		
		LauncherModel.disableAndFlushInstallQueue();
	}
	// Aurora <haojj> <2013-12-4> end
	
	/*private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.add_widget:
				enterEditMode(EditMode.APPWIDGET_ADD,true);
				createNewPage();
				break;
			case R.id.change_wallpaper:
				startWallpaper();
				break;
			case R.id.align_icon:
				if(mAlignIconDialog == null) {
					AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(Launcher.this, AlertDialog.THEME_TRADITIONAL);
					builder.setTitle(R.string.dialog_align_icon_title);
					builder.setMessage(R.string.dialog_align_icon_content);
					builder.setNegativeButton(android.R.string.cancel, null);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							alignIcons();
						}
					});
					mAlignIconDialog = builder.create();
					mAlignIconDialog.setCanceledOnTouchOutside(true);
				}
				mAlignIconDialog.show();
				break;
			case R.id.system_settings:
				try {
					Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
					settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					startActivity(settings);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast messageToast = Toast.makeText(Launcher.this, R.string.settings_not_found, Toast.LENGTH_LONG);
					messageToast.show();
				}
				break;
			default:
				break;
			}
		}
	};*/
	
	public void setWidgetFrameLeftVisible(boolean b) {
		if (b) {
			mWidgetframeleft.setImageAlpha(255);
		} else {
			mWidgetframeleft.setImageAlpha(85);
		}
	}
	public void setWidgetFrameRightVisible(boolean b) {
		if (b) {
			mWidgetframeright.setImageAlpha(255);
		} else {
			mWidgetframeright.setImageAlpha(85);
		}
	}
	@Override
	public void autoManangePages(){
		mWorkspace =getWorkspace();
		if(mWorkspace!=null){
			mWorkspace.autoManagePages();
		}
	}
	
	public void autoManagePagesImediatly(){
		mWorkspace =getWorkspace();
		if(mWorkspace!=null){
			mWorkspace.autoManagePagesImediatly();
		}
	}
	
	public int getmScreenWidth() {
		return mScreenWidth;
	}
	
	private static final String WALLPAPER_FIRST_TIME_SET =
            "WALLPAPER_FIRST_TIME_SET";
	public SharedPreferences getSharedPreferences(){
		return mSharedPrefs;
	}
	
	Animator getWidgetAnimator(boolean animated, boolean isAddwieget,
			int duration) {
		return getWidgetAnimator(animated, isAddwieget, duration, null);
	}
	
	Animator getWidgetAnimator(boolean animated, boolean isAddwieget,
			int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet()
				: null;
		int trancationY = isAddwieget ? 0 : (mWidgetViewLayout.getHeight()+170);
		float alpha = isAddwieget ? 1.0f : 0.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(
					mWidgetViewLayout);
			a.setDuration(duration);
			a.translationY(trancationY);
			a.alpha(alpha);
			anim.play(a);
			// mWidgetViewLayout.setVisibility(isAddwieget?View.VISIBLE:View.GONE);
		} else {
			mWidgetViewLayout.setAlpha(alpha);
			mWidgetViewLayout.setTranslationY(trancationY);
		}
		return anim;
	}
	
	Animator getClassficateIconsAnim(boolean animated,boolean isEnterClassificateIcons,
			int duration){
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet()
				: null;
		int trancationY = isEnterClassificateIcons ? 0 : (mClassficateIonsLayout.getHeight()+170);
		float alpha = isEnterClassificateIcons?1.0f:0.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(
					mClassficateIonsLayout);
			a.setDuration(duration);
			a.translationY(trancationY);
			a.alpha(alpha);
			anim.play(a);
			// mWidgetViewLayout.setVisibility(isAddwieget?View.VISIBLE:View.GONE);
		} else {
			mClassficateIonsLayout.setAlpha(alpha);
			mClassficateIonsLayout.setTranslationY(trancationY);
		}
		return anim;
		
	}
	
	private void setStatusBarTransparent(boolean enable) {
		Log.i("aaa", "1:setStatusBarTransparent:"+enable);
		
		//M:shigq status bar transparent set status bar transparent on Android5.0 platform begin
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        	Log.d("SHIGQ_LAUNCHER", "setStatusBarTransparent---------------enable = "+enable);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            /*window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);*/
            if (enable) {
            	window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
			}
            
        } else if (Build.VERSION.SDK_INT > 18 && VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
//		if (Build.VERSION.SDK_INT > 18) {
        //M:shigq status bar transparent set status bar transparent on Android5.0 platform end
        	
			Log.i("aaa", "2:setStatusBarTransparent:"+enable);
			Intent StatusBarBGIntent = new Intent();
			StatusBarBGIntent.setAction("aurora.action.CHANGE_STATUSBAR_BG");
			StatusBarBGIntent.putExtra("transparent", enable);
			sendBroadcast(StatusBarBGIntent);
		} else {
			NotificationManager notificationManager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification.Builder builder = new Builder(this);
			builder.setSmallIcon(R.mipmap.ic_launcher_application);
			String tag = "auroraSBNT653";
			if (enable) {
				tag = "auroraSBT8345";
			} else {
				tag = "auroraSBNT653";
			}
			notificationManager.notify(tag, 0, builder.build());
		}
    }
	
	public boolean isEditModeAnimationFinish(){
		Log.i("xiejun6","mExitAnimatorFinish="+mExitAnimatorFinish+"  ,mEnterAnimatorFinish="+mEnterAnimatorFinish);
		return !mExitAnimatorFinish||!mEnterAnimatorFinish;
	}
 
	private boolean mNeedAutoManagePagesWhenExited=true;
	public void setNeedAutoManagePagesToExitEditMode(boolean b){
		mNeedAutoManagePagesWhenExited=b;
	}
	Animator getThumnailAnimator(boolean animated, boolean isDrag, int duration) {		
		int deltaAndrepeat[] = {0, 0};
		int childcount = previewContent.getChildCount();
		int currentindex = getWorkspace().getCurrentPage();
		
		deltaAndrepeat = getRepeatCount(currentindex, childcount);
		ArrayList<Animator> animatorArrayList = new ArrayList<Animator>();
		
		int delay = 0;
		if (isDrag) {
			delay = 50;
		} else {
			delay = 0;
		}
		
		final ThumbnailImageItem tImagecurrentItem = (ThumbnailImageItem) previewContent.getChildAt(currentindex);
		if (tImagecurrentItem != null) {
			Animator a = getThumnailAnimator(tImagecurrentItem, animated, isDrag, duration, null);
			if (a != null) {
				animatorArrayList.add(a);
			}
		}
		for (int i = 1; i <= deltaAndrepeat[1]; i++) {
			ThumbnailImageItem tImagerightItem = (ThumbnailImageItem) previewContent.getChildAt(currentindex + i); // +i
			ThumbnailImageItem tImageleftItem = (ThumbnailImageItem) previewContent.getChildAt(currentindex - i); // -i

			if (deltaAndrepeat[0] != -1) {	//Delete the left or right ThumbnailImageItems that beyond the COUNTINONEPAGE(only contain 7 items)
				if (deltaAndrepeat[0] * 2 < previewContent.COUNTINONEPAGE - 1) {
					if (i > deltaAndrepeat[0] && tImageleftItem != null) {
						tImageleftItem = null;
					}
				} else {
					if (i >= (previewContent.COUNTINONEPAGE - deltaAndrepeat[0]) && tImagerightItem != null) {
						tImagerightItem = null;
					}
				}
			}
			if (tImageleftItem != null) {
				Animator a1 = getThumnailAnimator(tImageleftItem, animated, isDrag, duration, null);
				if (a1 != null) {
					a1.setStartDelay(delay * i);
					animatorArrayList.add(a1);
				}
			}
			if (tImagerightItem != null) {
				Animator a2 = getThumnailAnimator(tImagerightItem, animated, isDrag, duration, null);
				if (a2 != null) {
					a2.setStartDelay(delay * i);
					animatorArrayList.add(a2);
				}
			}
		}
		
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(animatorArrayList);
		return animatorSet;
	}

	Animator getThumnailAnimator(ThumbnailImageItem item, boolean animated, boolean isDrag,
			int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		float ScaleX = isDrag ? 1.0f : 0.4f;
		float ScaleY = isDrag ? 1.0f : 0.4f;
		
		float alpha = isDrag ? 1.0f : 0.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(item);
			a.setDuration(duration);
			a.scaleX(ScaleX);
			a.scaleY(ScaleY);
			a.alpha(alpha);
			anim.play(a);
		} else {
			item.setAlpha(alpha);
			item.setScaleX(ScaleX);
			item.setScaleY(ScaleY);
		}
		return anim;
	}
	
	Animator getThumnailScrollViewAnimator(boolean animated, boolean isDrag, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		float ScaleX = isDrag ? 1.0f : 0.2f;
		float ScaleY = isDrag ? 1.0f : 0.2f;
		
		float translationY = isDrag ? 0.0f : -25.0f;
		
		float alpha = isDrag ? 1.0f : 0.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(previewBar);
			a.setDuration(duration);
			a.translationY(translationY);
			a.scaleX(ScaleX);
			a.scaleY(ScaleY);
			a.alpha(alpha);
			anim.play(a);
		} else {
			previewBar.setPivotX(0.5f);
			previewBar.setPivotY(0.5f);
			previewBar.setAlpha(alpha);
			previewBar.setTranslationY(translationY);
			previewBar.setScaleX(ScaleX);
			previewBar.setScaleY(ScaleY);
		}
		return anim;
	}
	
	public int[] getRepeatCount(int index, int childcount) {
		int deltaAndrepeat[] = {0, 0};		// delta = deltaAndrepeat[0]; repeat = deltaAndrepeat[1];
		int repeat = 0;
		int delta = 0;
		if (childcount <= previewContent.COUNTINONEPAGE) {
			if (index * 2 > childcount) {
				repeat = index;
			} else {
				repeat = childcount - index;
			}
			delta = -1;
		} else {
			if (index / previewContent.COUNTINONEPAGE < childcount / previewContent.COUNTINONEPAGE) {
				delta = (index / previewContent.COUNTINONEPAGE) * previewContent.COUNTINONEPAGE;
			} else {
				delta = childcount - previewContent.COUNTINONEPAGE;
			}
			delta = index - delta;
			if (delta * 2 < previewContent.COUNTINONEPAGE - 1) {
				repeat = previewContent.COUNTINONEPAGE - delta;
			} else {
				repeat = delta;
			}
		}
		deltaAndrepeat[0] = delta;
		deltaAndrepeat[1] = repeat;
		return deltaAndrepeat;
	}
	
	public void setScrollIndicatorVisible(boolean b) {
		View mScrollIndicator = (View)findViewById(R.id.paged_view_indicator);
		if (mScrollIndicator != null) {
			if (b) {
				mScrollIndicator.setVisibility(View.VISIBLE);
			} else {
				mScrollIndicator.setVisibility(View.GONE);
			}
		}
	}
	
	public int getScrollIndicatorVisible() {
		View mScrollIndicator = (View)findViewById(R.id.paged_view_indicator);
		if (mScrollIndicator != null) {
			return mScrollIndicator.getVisibility();
		}
		return -1;
	}
	
	public void setThumanailImageVisible(boolean b) {
		final int childcount = previewContent.getChildCount();
		for (int i = 0; i < childcount; i++) {
			ThumbnailImageItem tItem = (ThumbnailImageItem) previewContent.getChildAt(i);
			if (b) {
				tItem.setAlpha(1f);
				tItem.setScaleX(1f);
				tItem.setScaleY(1f);
			} else {
				tItem.setAlpha(0f);
				tItem.setScaleX(0.3f);
				tItem.setScaleY(0.3f);
			}
		}
	}
	
	public boolean delPageindicator() {
		int count=this.getWorkspace().getChildCount();
		View itemcontainer = this.getWorkspace().getChildAt(count - 1);
		if(itemcontainer instanceof CellLayout){
			if((((CellLayout) itemcontainer).getShortcutsAndWidgets().getChildCount()) <= 0){
				return true;
			}
		}
		return false;
	}
	
	public void setTransMode(TransModeEnum transModeEnum) {
		TransMode = transModeEnum;
	}
	
	public int getScreenMode() {
		return mLauncherView.getSystemUiVisibility();
		//return mDragLayer.getSystemUiVisibility();
	}
	
	public void setScreenMode(int value) {
		mLauncherView.setSystemUiVisibility(value);
		//mDragLayer.setSystemUiVisibility(value);
	}
	
	public TransModeEnum getTransModeEnum() {
		return TransMode;
	}
	
	public PageIndicator getPageIndicator() {
		return mPageIndicator;
	}
	
	public ImageView getQuickIndexBgBlurView() {
		return mQuickIndexBgBlurView;
	}
	
	public ImageView getAppSearchBgImageView() {
		return mAppSearchBgImageView;
	}
	
	
	/*public void setLinearShadowVisable(boolean b) {
		if (b) {
			mShadowImageView.setVisibility(View.VISIBLE);
		} else {
			mShadowImageView.setVisibility(View.GONE);
		}
	}*/

	
	public void setAppSearchImageViewAlpha(float alpha) {
		mAppSearchImageView.setAlpha(alpha);
	}
	
	public void setQuickIndexBgBlurViewAlpha(float alpha) {
		if (mQuickIndexBgBlurView != null) {
			mQuickIndexBgBlurView.setAlpha(alpha);
		}
	}
	
	public void setAppSearchBgAlpha(float alpha) {
		if (mAppSearchBgImageView != null) {
			mAppSearchBgImageView.setAlpha(alpha);
		}
	}
	
	public void setGaussBlurShadowAlpha(float alpha) {
		if (mShadowImageView != null) {
			mShadowImageView.setAlpha(alpha);
		}
		if (mUpperShadowImageView != null) {
			mUpperShadowImageView.setAlpha(alpha);
		}
	}
	
	public void setPageIndicatorAlpha(float alpha) {
		if (mPageIndicator != null) {
			mPageIndicator.setAlpha(alpha);
		}
	}
	
	public void setCelllayoutAlpha(float alpha) {
		int currentpage = getWorkspace().getCurrentPage();
		CellLayout cellLayout = (CellLayout) getWorkspace().getChildAt(currentpage);
		if (cellLayout != null) {
			cellLayout.setAlpha(alpha);
		}
	}
	
	public void setAllObjectToOrignalAlpha() {
		setQuickIndexBgBlurViewAlpha(0f);
		setAppSearchBgAlpha(0f);
	}
	
	public void delAppSearchBgBackground() {
		if (mAppSearchBgImageView != null) {
			mAppSearchBgImageView.setBackground(null);
		}
	}
	
	/**set voice quit action to fits requirements*/
	public void toggleTransModeQuitAction() {
		int duration = 0;
		if (getTransModeEnum() == TransModeEnum.VOICE) {
			if (mVoiceContentViewHasFocus()) {
				mVoiceContentView.clearFocus();
				duration = 200;
			}
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mDragController.exitTransMode();
			}
		}, duration);
	}
	
	Animator getPageIndicatorAlphaAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		
		float alpha = direction ? 1.0f : 0.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mPageIndicator);
			a.setDuration(duration);
			a.alpha(alpha);    
			anim.play(a);
		} else {
			mPageIndicator.setAlpha(alpha);
		}
		return anim;
	}
	
	Animator getCelllayoutAlphaAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		
		int currentpage = getWorkspace().getCurrentPage();
		CellLayout cellLayout = (CellLayout) getWorkspace().getChildAt(currentpage);
		
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		
		float alpha = direction ? 1.0f : 0.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(cellLayout);
			a.setDuration(duration);
			a.alpha(alpha);    
			anim.play(a);
		} else {
			cellLayout.setAlpha(alpha);
		}
		return anim;
	}
	
	Animator getGaussBlurLowerAlphaAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		
		float alpha = direction ? 0.0f : 1.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mAppSearchBgImageView);
			a.setDuration(duration);
			a.alpha(alpha);    
			anim.play(a);
		} else {
			mAppSearchBgImageView.setAlpha(alpha);
		}
		return anim;
	}
	
	Animator getGaussBlurUpperAlphaAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		
		float alpha = direction ? 0.0f : 1.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mQuickIndexBgBlurView);
			a.setDuration(duration);
			a.alpha(alpha);    
			anim.play(a);
		} else {
			mQuickIndexBgBlurView.setAlpha(alpha);
		}
		return anim;
	}
	
	Animator getGaussBlurShadowAlphaAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		
		float alpha = direction ? 0.0f : 1.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mShadowImageView);
			a.setDuration(duration);
			a.alpha(alpha);  
			LauncherViewPropertyAnimator a2 = new LauncherViewPropertyAnimator(mUpperShadowImageView);
			a2.setDuration(duration);
			a2.alpha(alpha);  
			ArrayList<Animator> items = new ArrayList<Animator>();
			items.add(a);
			items.add(a2);
			//anim.play(a);
			anim.playTogether(items);
		} else {
			mShadowImageView.setAlpha(alpha);
			mUpperShadowImageView.setAlpha(alpha);
		}
		return anim;
	}
	
	Animator getGaussBlurAlphaAnimator(boolean animated, boolean direction, int duration) {
		ArrayList<Animator> animatorArrayList = new ArrayList<Animator>();
		Animator gaussBlurUpperAnimator = getGaussBlurUpperAlphaAnimator(animated, direction, duration, null);
		Animator gaussBlurLowerAnimator = getGaussBlurLowerAlphaAnimator(animated, direction, duration, null);
		Animator gaussBlurShadowAnimator;
		if (!direction) {
			gaussBlurShadowAnimator = getGaussBlurShadowAlphaAnimator(animated, direction, duration/4, null);
		} else {
			gaussBlurShadowAnimator = getGaussBlurShadowAlphaAnimator(animated, direction,duration/5, null);

		}	
		AnimatorSet animatorSet = new AnimatorSet();
		if (direction) {
			if (gaussBlurShadowAnimator != null) {
				animatorArrayList.add(gaussBlurShadowAnimator);
			}
			if (gaussBlurUpperAnimator != null) {
				gaussBlurUpperAnimator.setStartDelay(150);
				animatorArrayList.add(gaussBlurUpperAnimator);
			}
			if (gaussBlurLowerAnimator != null) {
				gaussBlurLowerAnimator.setStartDelay(150);
				animatorArrayList.add(gaussBlurLowerAnimator);
			}
		} else {
			if (gaussBlurUpperAnimator != null) {
				animatorArrayList.add(gaussBlurUpperAnimator);
			}
			if (gaussBlurLowerAnimator != null) {
				animatorArrayList.add(gaussBlurLowerAnimator);
			}
			if (gaussBlurShadowAnimator != null) {
				gaussBlurShadowAnimator.setStartDelay(200);
				animatorArrayList.add(gaussBlurShadowAnimator);
			}
		}
		animatorSet.playTogether(animatorArrayList);
		return animatorSet;
	}
	
	public void setGaussBlurAlphaAnimator(final boolean animated, final boolean direction, final int duration) {
		/**SET IT VERY SOON*/
		setTransMode(TransModeEnum.NONE);
		mGaussBlurAlphaAnimator = getGaussBlurAlphaAnimator(animated, direction, duration);
		if (mGaussBlurAlphaAnimator != null) {
			mGaussBlurAlphaAnimator.addListener(new AnimatorListenerAdapter() {			
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					Log.e("linp", "Animator is start");
					mAnimationIsGoing = true;
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					Log.e("linp", "Animator is end");
					getDragController().setHomeOrBackPress(true);
					if (direction) {
//						dismissQuickIndex();
					dismissBothBlurView();
					
						mQuickIndexLayout.setLayerType(View.LAYER_TYPE_NONE, null);
						mQuickIndexLayout.buildLayer();
						
						getHotseat().setLayerType(View.LAYER_TYPE_NONE, null);
						getHotseat().buildLayer();
						
						mVoiceLayout.setLayerType(View.LAYER_TYPE_NONE, null);
						mVoiceLayout.buildLayer();
						
						mVoiceButton.setLayerType(View.LAYER_TYPE_NONE, null);
						mVoiceButton.buildLayer();
						
//						if (getTransModeEnum() == TransModeEnum.APPS_INDEX) {
							exitFullScreenInAppindexeMode();
//						} 
//						else if (getTransModeEnum() == TransModeEnum.VOICE) {
//							exitFullScreenInVoiceMode();
//						}
						mVoiceOutLine.setAlpha(1f);
//					    setTransMode(TransModeEnum.NONE);
						mQuickIndexLayout.setAlpha(0f);
						mVoiceButton.setBackground(null);
						mAnimationIsGoing = false;
					}
					
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					Log.d("DEBUG", "Animator will be canceled");
					if (!direction) {
						if (getTransModeEnum() == TransMode.APPS_INDEX) {
							Log.d("DEBUG", "cancel from APPS_INDEX");
							mDragController.exitAppIndexModeWithTime(200, true);
						}
						if (getTransModeEnum() == TransMode.VOICE) {
							Log.d("DEBUG", "cancle from VOICE");
							mDragController.exitVoiceModeWithTime(200, true);
						}
					}
					super.onAnimationCancel(animation);
				}
			});
			mGaussBlurAlphaAnimator.start();
		}
	}
	
	/** restore miscellaneous object */
	public void restoreMiscellaneousObject() {
		setTransMode(TransModeEnum.NONE);
		mQuickIndexLayout.setLayerType(View.LAYER_TYPE_NONE, null);
		mQuickIndexLayout.buildLayer();

		getHotseat().setLayerType(View.LAYER_TYPE_NONE, null);
		getHotseat().buildLayer();

		mVoiceLayout.setLayerType(View.LAYER_TYPE_NONE, null);
		mVoiceLayout.buildLayer();

		mVoiceButton.setLayerType(View.LAYER_TYPE_NONE, null);
		mVoiceButton.buildLayer();

		mVoiceOutLine.setAlpha(1f);
		mQuickIndexLayout.setAlpha(0f);
		mVoiceButton.setBackground(null);
		mAnimationIsGoing = false;
	}
	
	public void cancelGaussBlurAlphaAnimator() {
      if (mGaussBlurAlphaAnimator != null) {
    	  mGaussBlurAlphaAnimator.cancel();
    	  mGaussBlurAlphaAnimator = null;
      }
	}
	
	public boolean getAnimationFlag() {
		return mAnimationIsGoing;
	}
	
	Animator getAppSearchViewClickAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		float transY = this.getFloatValueFromResourcesDimens(R.dimen.appsearch_iv_translation_y);
		
		float translationY = direction ? transY : 0.0f;		//-1688f:0.0f --> 13dp

		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mAppSearchImageView);
			a.setDuration(duration);
			a.translationY(translationY);
			anim.play(a);
		} else {
			mAppSearchImageView.setTranslationY(translationY);
		}
		return anim;
	}
	
//	Animator getAppSearchEditTextAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
//		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
//		float ScaleX = direction ? 1.0f : 0.0f;
//
//		if (animated) {
//			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mQuickIndexSearchEditText);
//			a.setDuration(duration);
//			a.scaleX(ScaleX);
//			anim.play(a);
//		} else {
//			mQuickIndexSearchEditText.setPivotX(0.5f);
//			mQuickIndexSearchEditText.setScaleX(ScaleX);	
//		}
//		return anim;
//	}
	
	/*Animator getAppSearchFrameLayoutAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		float ScaleX = direction ? 1.0f : 0.1f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(mQuickIndexSearchLayout);
			a.setDuration(duration);
			a.scaleX(ScaleX);
			anim.play(a);
		} else {
			mQuickIndexSearchLayout.setPivotX(0.5f);
			mQuickIndexSearchLayout.setScaleX(ScaleX);
		}
		return anim;
	}*/
	
	ValueAnimator getAppSearchEditTextAnimator(boolean animated, boolean direction, int duration, Runnable callback) {
		ValueAnimator animation;
		float searchImgWidth = getFloatValueFromResourcesDimens(R.dimen.quick_index_search_img_width);
		if (direction){
			animation= ValueAnimator.ofFloat(150f,searchImgWidth).setDuration(duration);
			
		} else {
			animation= ValueAnimator.ofFloat(searchImgWidth, 150f).setDuration(duration);
		}
		animation.addUpdateListener(new AnimatorUpdateListener() {
		    @Override
		    public void onAnimationUpdate(ValueAnimator animation) {
		    	float step = ((Float)animation.getAnimatedValue()).floatValue();
		    	mQuickIndexSearchEditText.setWidth((int)step);
		    }
		});
		return animation;
	}
	
	Animator getAppIndexSearchAnimator(boolean animated, final boolean direction, int duration, Runnable callback) {
		ArrayList<Animator> animatorArrayList = new ArrayList<Animator>();
		mAppSearchViewAnimator = getAppSearchViewClickAnimator(animated, direction, duration, callback);
		if (mAppSearchViewAnimator != null) {
			animatorArrayList.add(mAppSearchViewAnimator);
			if (direction == false) {
				mAppSearchViewAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			}
			mAppSearchViewAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (direction) {
						mQuickIndexSearchEditText.setAlpha(1.0f);
						mAppSearchImageView.setVisibility(View.INVISIBLE);
						mQuickIndexSearchEditText.setVisibility(View.VISIBLE);
					}
				}
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
				}
			});
		}
		duration = duration*2/3;
		
		mAppSearchEditTextAnimator = getAppSearchEditTextAnimator(animated, direction, duration, callback);
		if (mAppSearchEditTextAnimator != null) {
			animatorArrayList.add(mAppSearchEditTextAnimator);
			if (direction == true) {
				mAppSearchEditTextAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			}
			mAppSearchEditTextAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (!direction) {
						mQuickIndexSearchEditText.setVisibility(View.GONE);
						mAppSearchImageView.setVisibility(View.VISIBLE);
					}
				}	
				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
				}
			});
		}

		/*mAppSearchEditTextAnimator = getAppSearchFrameLayoutAnimator(animated, direction, duration, callback);
		if (mAppSearchEditTextAnimator != null) {
			animatorArrayList.add(mAppSearchEditTextAnimator);
			if (direction == true) {
				mAppSearchEditTextAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			}
			mAppSearchEditTextAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (mQuickIndexSearchLayout.getScaleX() != 1f) {
						Log.d("DEBUG", "the Edittext animator is end");
						mAppSearchImageView.setVisibility(View.VISIBLE);
						mQuickIndexSearchLayout.setVisibility(View.GONE);
					}
				}	
				@Override
				public void onAnimationCancel(Animator animation) {
					Log.d("DEBUG", "the edittext animator cancel is called----");
					
					super.onAnimationCancel(animation);
				}
			});
		}*/
		
		AnimatorSet animatorSet = new AnimatorSet();
		if (direction == true) {
			animatorSet.play(mAppSearchEditTextAnimator).after(mAppSearchViewAnimator);
		} else {
			animatorSet.play(mAppSearchViewAnimator).after(mAppSearchEditTextAnimator);
		}
		return animatorSet;
	}
	
	/**expand it
	 * @param mode indicated that which button will been pressed (backpress or HomePress)
	 * */
	public void enterAppIndexSearchAnimator(boolean animated, final boolean direction, int duration, Runnable callback,final int mode) {
		appIndexSearchAnimator = getAppIndexSearchAnimator(animated, direction, duration, callback);
		if (appIndexSearchAnimator != null) {
			appIndexSearchAnimator.addListener(new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					mAppSearchAnimatorGoing = true;
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					mAppSearchAnimatorGoing = false;
					if (!direction) {
						//we add getAnimationFlag() because it will be wrong when press Home Key more than once
						startAppRetrievalListAnimator();
						exitFilterSearchIndexMode();
						if (mode != APP_RETRIEVAL_BACK && !getAnimationFlag()) {
							mDragController.exitTransMode();
						}
					}
				}
				@Override
				public void onAnimationCancel(Animator animation) {					
					if (mAppSearchEditTextAnimator != null) {
						mAppSearchEditTextAnimator.cancel();
						mAppSearchEditTextAnimator = null;
					}
					if (mAppSearchViewAnimator != null) {
						mAppSearchViewAnimator.cancel();
						mAppSearchViewAnimator = null;
					}
					mAppSearchBgImageView.setVisibility(View.VISIBLE);
					
					//exitFilterSearchIndexMode();
					super.onAnimationCancel(animation);
				}
			});
			appIndexSearchAnimator.start();
		}
	}
	
	public void setLinearAlpha(Bitmap sourceImg, int offset, Bitmap[] bABitmaps, boolean showUpper) {  
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,sourceImg.getWidth(), sourceImg.getHeight());	//get the ARGB from photo  
        int linearAlphaOffset = Utilities.getIntegerValueFromResourcesDimens(getResources(),R.dimen.quick_index_set_linear_alpha_offset);
        //Log.e("linp", "---------linearAlphaOffset="+linearAlphaOffset);
		int number = 0;
		for (int i = 0; i < argb.length; i++) {
			int j = i/sourceImg.getWidth() + offset;
			number = (int)(j*255/linearAlphaOffset);//5: 20/91*25
			if (number <= 255) {
				argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);	//change the highest 2 bits
			} else {
				break;
			}
		} 
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);
		if(showUpper) {
			// showUpper为true，说明是充满状态，因此要截断
			sourceImg = magnifyBitmap(sourceImg, 10f, 10f);
			int bitmapWidth = mScreenWidth - mQuickIndexShadowImageRightMargin;
			int bitmapHeight = mQuickIndexShadowImageHeight < sourceImg.getHeight() ? mQuickIndexShadowImageHeight : sourceImg
					.getHeight();
			bABitmaps[0] = Bitmap.createBitmap(sourceImg, 0, 0, bitmapWidth, bitmapHeight);
			bABitmaps[1] = Bitmap.createBitmap(sourceImg, bitmapWidth, 0, mQuickIndexShadowImageRightMargin, bitmapHeight);
		} else {
			bABitmaps[0] = sourceImg;
			bABitmaps[1] = null;
		}
	}
	
	private static Bitmap magnifyBitmap(Bitmap bitmap, float ws, float hs) {
		Matrix matrix = new Matrix();
		matrix.postScale(ws, hs); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
	
	public void enterFullScreenInAppindexMode(int duration) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Runnable runnable = getExitRunnable();
				if(runnable!=null)getWorkspace().removeCallbacks(runnable);
				onEnterFullScreen();
			}
		}, duration);
	}
	
	public void exitFullScreenInAppindexeMode() {
		onExitFullScreen();
	}
	
	//For voice test
	public FrameLayout getVoiceOutLine() {
		return mVoiceOutLine;
	}
	
	public void updateMarginForVoiceOutLine(boolean b) {
		int mMarginBottom = getResources().getDimensionPixelOffset(R.dimen.voice_outline_margin_bottom);
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mVoiceOutLine.getLayoutParams();
		if (b) {
			params.bottomMargin = 0;
		} else {
			params.bottomMargin = mMarginBottom;
		}
	}
	
	//get voicelayout from voice apk
	public boolean getVoicePackageSuccess() {
		String classString = "com.aurora.voiceassistant.view.MainActivity";
		mLocalActivityManager = getLocalActivityManager();
		final Intent intent = new Intent();
		String packagename = "com.aurora.voiceassistant";
		intent.setClassName(packagename, classString);
		List<ResolveInfo> resolveInfos=this.getPackageManager().queryIntentActivities(intent, 0);
		if(resolveInfos != null && resolveInfos.size() != 0) {
			return true;
		}
		return false;
	}
	
	public void getVoiceContextFromActivity() {
//			new loadVoiceAssistantDecorViewThread().start();
			loadVoiceAssistantDecorView();
			mVoiceLayout.removeAllViews();	
			mVoiceLayout.addView(mVoiceContentView);

	}
	
	
	/**start to load voice assistant*/
	private void loadVoiceAssistantDecorView(){
		try {
			String classString = "com.aurora.voiceassistant.view.MainActivity";
			LocalActivityManager mLocalActivityManager = getLocalActivityManager();
			final Intent intent = new Intent();
			String packagename = "com.aurora.voiceassistant";
			intent.setClassName(packagename, classString);
			mVoiceContentView = mLocalActivityManager.startActivity("", intent).getDecorView();
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
		}
	}
	
	public View getVoiceContentView() {
		return mVoiceContentView;
	}
	
	public VoiceLayout getVoiceLayout() {
		return mVoiceLayout;
	}
	
	public GlsurfaceMask getImageLayerMask(){
		return mLayerMask;
	}
	
	public VoiceButtonImageView getVoiceButtonImageView() {
		return mVoiceButton;
	}
	
	public void setVoiceButtonVisible(boolean b) {
		if (b) {
			mVoiceButton.setVisibility(View.VISIBLE);
		} else {
			mVoiceButton.setVisibility(View.GONE);
		}
	}
	
	private String ToDismissMenuByHomeKey = "dismissmenubyhomekey";
	private String ToStopPlayVoice = "stopplayvoice";
	private String ToCheckGreeting = "checkgreeting";
	public void sendBroadCastToSlave(String action, String valuename, boolean value) {
		mMasterToSlaveIntent.setAction(action);
		mMasterToSlaveIntent.putExtra(valuename, value);
		this.sendBroadcast(mMasterToSlaveIntent);
	}
	
	private String voiceInputChanged_Receive = "voiceinputchanged";
	BroadcastReceiver mSlaveToMasterReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String receivedString = intent.getAction();
			if (receivedString.equals(voiceInputChanged_Receive)) {
				setVoiceButtonClickState(intent.getExtras().getBoolean("state"));
			}else if(receivedString.equals(ADD_NAVIGATION_TRANSLUCENT_FLAG)){
				 if (VERSION.SDK_INT > 18) {
					 Log.e("linp","#############################ADD_NAVIGATION_TRANSLUCENT_FLAG");
					 restoreNavigationbarBackgroundColor();
				 }
			}else if(receivedString.equals(ENTER_VOICEMODE_BY_VOICEPRINT)){
				Log.e("linp", "----------------------------------------ENTER_VOICEMODE_BY_VOICEPRINT");
			    getDragController().enterTransModeTo(TransModeEnum.VOICE, 1500);
				
			}else if(receivedString.equals(ACTION_NAVIGATION_HINT_BACK)){
				if(getTransModeEnum()!=TransModeEnum.NONE){
					if(VERSION.SDK_INT>18){
						 Log.e("linp", "Launcher ACTION_NAVIGATION_HINT_BACK and sdk version 18!");
						 getDragController().scheduleTimerTask();
					}
				}
			}else if (receivedString.endsWith(ACTION_VOICE_CLEAR_SCREEN)){
				mLayerMask.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_layer));
			}
		}
	};
	private boolean getButtonClickState = false;
	public void setVoiceButtonClickState(boolean value) {
		getButtonClickState = value;
		if (value == false) {
//			Log.d("DEBUG", "set voice button background as mic");
			mVoiceButton.setBackgroundResource(R.drawable.voicebutton_mic);
		} else {
//			Log.d("DEBUG", "set voice button background as input");
			mVoiceButton.setBackgroundResource(R.drawable.voicebutton_input);
		}
	}
	
	public boolean getVoiceButtonClickState(){
		return getButtonClickState;
	}
	
	public void enterFullScreenInVoiceMode(int duration) {
		if (mExitVoiceFullScreenDelayRunnable != null) {
			mHandler.removeCallbacks(mExitVoiceFullScreenDelayRunnable);
		}
		mHandler.postDelayed(mEnterVoiceFullScreenDelayRunnable, duration);
	}
	
	public void exitFullScreenInVoiceMode() {
		if (mEnterVoiceFullScreenDelayRunnable != null) {
			mHandler.removeCallbacks(mEnterVoiceFullScreenDelayRunnable);
		}
		mHandler.postDelayed(mExitVoiceFullScreenDelayRunnable, 100);
	}
	
	public boolean mVoiceContentViewHasFocus() {
		if (mVoiceContentView != null) {
			if (mVoiceContentView.hasFocus()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getInputMethodState() {
		return inputState;
	}
		
	Runnable mShowShadowImageDelayRunnable = new Runnable() {
		@Override
		public void run() {
//			Log.d("DEBUG","*********************mShowShadowImageDelayRunnable********************");
			mShadowImageView.resetMarginValue(true);
			mShadowImageView.setVisibility(View.VISIBLE);
		}
	};
	
	Runnable mEnterVoiceFullScreenDelayRunnable = new Runnable() {
		@Override
		public void run() {
			if (mVoiceContentView != null) {
				mVoiceContentView
						.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
			}
		}
	};

	Runnable mExitVoiceFullScreenDelayRunnable = new Runnable() {
		@Override
		public void run() {
			if (mVoiceContentView != null) {
				mVoiceContentView
						.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			}
		}
	};

	public void sendBroadCastToStopPlayVoice() {
		sendBroadCastToSlave(ToStopPlayVoice, "stopplay", true);
	}
	
	public void sendBroadCastToCheckGreeting() {
		sendBroadCastToSlave(ToCheckGreeting, "checkgreeting", true);
	}
	//For voice test

	public boolean isEditMode() {
		return mEditMode == EditMode.DRAG
				|| mEditMode == EditMode.APPWIDGET_ADD;
	}
	
	public boolean isDragMode() {
		return mEditMode == EditMode.DRAG;
	}
	public View mAuroraDragView;
	private String mAuroraUninstallPkgName;
	
	private boolean checkAppUninstall(String pkgName) {
		if (pkgName == null || "".equals(pkgName))
			return false;
		List<ResolveInfo> apps = AllAppsList.findActivitiesForPackage(this, pkgName);
		return apps.size() != 0;
	}
	private void showFolderIcon(){
		if(mEditFolderIcon != null && mEditFolderIcon.getAlpha() < 1.0f) {
			mEditFolderIcon.setAlpha(1.0f);
		}
	}
	
	public void resetAuroraUninstallData() {
		mAuroraDragView = null;
		mAuroraUninstallPkgName = null;
	}
	
	public boolean isUninstalling() {
		return mAuroraDragView != null || mAuroraUninstallPkgName != null;
	}
	
	private void addAuroraShortcut(ShortcutInfo item, int screen, int[] loc) {
		if (mWorkspace != null) {
			int count = mWorkspace.getChildCount();
			if (item.screen > count - 1) {
				createNewPage();
			}
			View view = createShortcut(item);
			mWorkspace.addInScreen(view,
					LauncherSettings.Favorites.CONTAINER_DESKTOP, screen, loc[0],
					loc[1], 1, 1);
		}
	}
	
	private String unlockToLauncherRecieverAction="com.aurora.lancher.start.anim";
	private UnlockToLauncherReciever startAnim = new UnlockToLauncherReciever();
	class UnlockToLauncherReciever extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			/*
			if(arg1!=null && arg1.getAction().equals(unlockToLauncherRecieverAction)){
				if(lockState){
					ALog.i("start","3");
					Log.d("SHIGQ_LAUNCHER", "com.aurora.lancher.start.anim----------------");
					mHandler.removeCallbacks(startAnimationRunnable);
					mHandler.post(startAnimationRunnable);
					lockState = false;
					isOurLockMessage = true;
				}
			}
			*/
		}
	}

	Runnable startAnimationRunnable = new Runnable() {
		@Override
		public void run() {
			if(true)return;
			ALog.i("start","startAnimationRunnable");
			final Workspace workspace = getWorkspace();
			int currentpage = -1;
			CellLayout cellLayout = null;
			CellLayout hotCellLayout = null;
			if(getHotseat()!=null){
				hotCellLayout = getHotseat().getLayout();
				if(hotCellLayout!=null&&hotCellLayout.isHandupItems()){
					hotCellLayout.beginOrAdjustHintAnimations();
				}
			}
			if(workspace!=null){
				currentpage = workspace.getHidePageIndex();
				if(currentpage!=-1){
					cellLayout=(CellLayout)(workspace.getChildAt(currentpage));
					if(cellLayout!=null&&cellLayout.isHandupItems()){
						cellLayout.beginOrAdjustHintAnimations();
					}
				}
				workspace.clearHidePage();
				final View view = workspace.getScrollingIndicator();
				if (view != null) {
					ValueAnimator vb = LauncherAnimUtils.ofFloat(0f, 1f);
					vb.setDuration(1000);
					vb.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							float r = ((Float) animation.getAnimatedValue())
									.floatValue();
							view.setAlpha(r);
						}
					});
					vb.setStartDelay(100);
					vb.start();
				}
			}
		}
	};
	
	
	private void handupViews(int px){
		if(true)return;
		ALog.i("start","handupViews");
		Log.d("SHIGQ_LAUNCHER", "handupViews----------------");
		Workspace workspace = getWorkspace();
		int currentpage = -1;
		CellLayout cellLayout = null;
		CellLayout hotCellLayout = null;
		if(getHotseat()!=null){
			hotCellLayout = getHotseat().getLayout();
			if(hotCellLayout!=null){
				hotCellLayout.handupChilds(30);
			}
		}
		if(workspace!=null){
			currentpage = workspace.getCurrentPage();
			if(currentpage!=-1){
				workspace.setHidePageIndex(currentpage);
				cellLayout=(CellLayout)(workspace.getChildAt(currentpage));
				if(cellLayout!=null){
					cellLayout.handupChilds(30);
				}
			}
			View view = workspace.getScrollingIndicator();
			if(view != null){
				if(view.getAlpha()==1.0f){
					view.setAlpha(0.0f);
				}
			}
		}
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		return true;
	}
	
	private boolean isExitFullScreen = false;
	private Runnable mExitRunnable = new Runnable() {
		@Override
		public void run() {
			isExitFullScreen=true;
			onExitFullScreen();
		}
	};
	
	public Runnable getExitRunnable(){
		return mExitRunnable;
	}

	private IAddAuroraShortcut mAddshortcut = new IAddAuroraShortcut() {
		public void addAuroraShortcut(ShortcutInfo item, int screen, int[] loc) {
			Launcher.this.addAuroraShortcut(item, screen, loc);
		}
		
		public boolean isFromAuroraShortcut(){
			return mFromAuroraWidget;
		}
	};
	public static final int MENU_ADD_WIDGET_INDEX = 0;
	public static final int MENU_CHANGE_WALLPAPER_INDEX = 1;
	public static final int MENU_ALIGN_ICONS_INDEX = 3;
	public static final int MENU_SYSTEM_SETTINGS_INDEX = 4;
	public static final int MENU_CLASSIFICATE_ICONS_INDEX = 2;
	OnItemClickListener mCustomMenuItemClickListener=new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			case MENU_ADD_WIDGET_INDEX:
				enterEditMode(EditMode.APPWIDGET_ADD,true);
				createNewPage();
				break;
			case MENU_CHANGE_WALLPAPER_INDEX:
				startWallpaper();
				break;
			case MENU_ALIGN_ICONS_INDEX:
				if(mAlignIconDialog == null) {
					AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(Launcher.this, AlertDialog.THEME_TRADITIONAL);
					builder.setTitle(R.string.dialog_align_icon_title);
					builder.setMessage(R.string.dialog_align_icon_content);
					builder.setNegativeButton(android.R.string.cancel, null);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							alignIcons();
						}
					});
					
					mAlignIconDialog = builder.create();
					mAlignIconDialog.setCanceledOnTouchOutside(true);
				}
				mAlignIconDialog.show();
				break;
			case MENU_SYSTEM_SETTINGS_INDEX:
				try {
					Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
					settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					startActivity(settings);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast messageToast = Toast.makeText(Launcher.this, R.string.settings_not_found, Toast.LENGTH_LONG);
					messageToast.show();
				}
				break;
			case MENU_CLASSIFICATE_ICONS_INDEX:
				startSmartClassify();
				break;
			default:
				break;
			}
			dismissCustomMenu();
		}
	};

	//AURORA-START::Fix bug #2752::Shi guiqiang::20140304
	AnimatorSet mStateAnimation1 = null;
	public void toPlayThumnailLongClickAnimator(boolean animated) {
		if (mStateAnimation1 != null) {
			mStateAnimation1.cancel();
			mStateAnimation1 = null;
		}

		Animator thumnailAnimator = null;
		Animator scrollviewAnimator = null;

		setScrollIndicatorVisible(false);
		setThumanailImageVisible(false);
		thumnailAnimator = getThumnailAnimator(animated, true, 350);
		if (thumnailAnimator != null) {
			thumnailAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			thumnailAnimator.setInterpolator(new OvershootInterpolator(1.5f));
			thumnailAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					thumbnailImageSetVisibility(View.VISIBLE);
					setThumbnailImageArrowHead();
					setThumanailImageVisible(true);
				}
			});
		}
		scrollviewAnimator = getThumnailScrollViewAnimator(animated, true, 350, null);
		if (scrollviewAnimator != null) {
			scrollviewAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			scrollviewAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//Do nothing
				}
			});
		}
		if(mStateAnimation1 == null)mStateAnimation1 = LauncherAnimUtils.createAnimatorSet();
		if (thumnailAnimator != null) {
			mStateAnimation1.play(thumnailAnimator);
		}
		if (scrollviewAnimator != null) {
			mStateAnimation1.play(scrollviewAnimator);
		}
		final AnimatorSet stateAnimation1 =mStateAnimation1;
		mWorkspace.post(new Runnable() {
			public void run() {
				if (stateAnimation1 != mStateAnimation1)
					return;
				mStateAnimation1.start();
			}
		});
	}
	//AURORA-END::Fix bug #2752::Shi guiqiang::20140304
	/**
	 * Desc: Call handleFolderClick function when user  press the QuickIndexListView's item  (long click) 
	 * Date: 2014/03/21
	 * */
	public void openFolderActionFromQucikSearch(View v) {
		ItemInfo info = (ItemInfo) v.getTag();
		FolderIcon mFolderIcon = getWorkspace().getFolderByContainer(info.container);
		if(mFolderIcon != null){
		int FolderScreen = mFolderIcon.getFolderInfo().screen;
		if(getWorkspace().getCurrentPage()!=FolderScreen && mFolderIcon.getFolderInfo().container!= LauncherSettings.Favorites.CONTAINER_HOTSEAT){
			getWorkspace().snapToPage(FolderScreen);
		}
		Message m = new Message();
		m.obj =  v;
		m.what = 0x51;
		
		if(!getWorkspace().mScroller.isFinished()){
		    mHandler.sendMessageDelayed(m, getWorkspace().mScroller.getDuration());
		}else{
			mHandler.sendMessage(m);	
		}
		}	
	}

	/**
	 * Desc:start up thread to handle open folder but it needs getAnimationFlag to be false
	 *  
	 * */
	public void sendOpenFolderMessage(final View v,final int categories) {
		Thread handleFolderOpenThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (!getAnimationFlag()) {
						Message m = new Message();
						m.what = 0x50;
						m.arg1 = categories;
						m.obj = v;
						mHandler.sendMessage(m);
						break;
					}
				}
			}
		});
		handleFolderOpenThread.start();
	}
	
	public QuickIndexFrameLayout getQuickIndexFrameLayout(){
		return mQuickIndexOutline;
	}
	
	public boolean isQuickSearchFilterMode(){
		return isEnterFilterMode;
	}
	
	public Animation getQuickLocateItemAnim(){
		return AnimationUtils.loadAnimation(this, R.anim.quick_locate_item_anim);
 	}
	
	public void ExitQuickSearchFilterMode(){
		exitFilterSearchIndexMode();
	}
	
	/**handle quick Index location*/
	private void handleQuickIndexLocation(int categories,View v){
		switch (categories) {
		case QuickDropLocate.CATEGORIES_FOLDER:
			openFolderActionFromQucikSearch(v);
			break;
		case QuickDropLocate.CATEGORIES_WORKSPACE:
			ItemInfo item = (ItemInfo)v.getTag();
			getWorkspace().snapToPage(item.screen);
			startQuickSearchItemAnim(v);
			break;
		}
	}
	
	public void startQuickSearchItemAnim(View v) {
		View ItemView = getWorkspace().getViewForTag(v.getTag());
		Animation anim = getQuickLocateItemAnim();
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
			}
		});
		if (anim != null) {
			ItemView.startAnimation(anim);

		}
	}
	
	/**Get value from dimens list.other class can use it!*/
	public float getFloatValueFromResourcesDimens(int id){
		TypedValue typedValue = new TypedValue();
		getResources().getValue(id, typedValue, true);
		return  typedValue.getFloat();
	}
	
	 
	class blurThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//setPriority(Thread.MIN_PRIORITY);
			Bitmap[] bitmapArray = processBgBlurView(true);
			Message m = new Message();
			m.obj = bitmapArray;
			m.what = 0x52;
			mHandler.sendMessage(m);
			super.run();
		}

	}
	
	/**start alpha animate when search */
	Animator getAppRetrievalAlphaAnimator(boolean animated,int duration) {
		if (animated) {
			ArrayList<Animator> animatorArrayList = new ArrayList<Animator>();
			Animator AppRetrievalListAnimator =getAppRetrievalListAndSideBarAlphaAnimator(animated,duration,mQuickIndexListView);
			Animator AppRetrievalSideBarAnimator = getAppRetrievalListAndSideBarAlphaAnimator(animated,duration,letterSideBar);
			AnimatorSet animatorSet = new AnimatorSet();

			if (AppRetrievalListAnimator != null) {
				animatorArrayList.add(AppRetrievalListAnimator);
			}
			if (AppRetrievalSideBarAnimator != null) {
				animatorArrayList.add(AppRetrievalSideBarAnimator);
			}

			animatorSet.playTogether(animatorArrayList);
			return animatorSet;
		}
		return null;
	}
	
	Animator getAppRetrievalListAndSideBarAlphaAnimator(boolean animated,int duration,View v) {
		AnimatorSet anim = animated ? LauncherAnimUtils.createAnimatorSet() : null;
		float alpha = 1.0f;
		if (animated) {
			LauncherViewPropertyAnimator a = new LauncherViewPropertyAnimator(v);
			a.setDuration(duration);
			a.alpha(alpha);    
			anim.play(a);
		} 
		return anim;
	}
	
	/**start quick index app retrievallist animate*/
	private void startAppRetrievalListAnimator(){
		Animator mAppRetrievalListAlphaAnimator = getAppRetrievalAlphaAnimator(true, 250);
		if (mAppRetrievalListAlphaAnimator != null) {
			mAppRetrievalListAlphaAnimator.addListener(new AnimatorListenerAdapter() {			
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					mAppRetrievalAppListAnimator = true;
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mAppRetrievalAppListAnimator = false;
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {

	            }
			});
			mAppRetrievalListAlphaAnimator.start();
         }
	}
	
	/**read intent extra from intent*/
	private int readIntentExtraFromVoicePrint(Intent it) {
		if (it != null) {
			Bundle b = it.getExtras();
			if (b != null) {
               int val = b.getInt(VOICEPRINT_INTENT_EXTRA_KEY_NAME);
               return val;
			} else {
				Log.e("linp", "intent getExtra bundle return null!\n");
			}
		} else {
			Log.e("linp", "readIntentExtraFromVoicePrint intent null!\n");
		}
		return -1;
	}
	
	/**read voice print status and handle the result */
	private void startCheckVoicePrintStatus(Intent it) {
		int val = readIntentExtraFromVoicePrint(it);
		Log.e("linp", "###startCheckVoicePrintStatus val=" + val);
		switch (val) {
		case LAUNCHER_TASK_FLAG_INVALID:
			Log.e("linp","###startCheckVoicePrintStatus readIntentExtraFromVoicePrint() return -1\n");
			break;
		case LAUNCHER_TASK_FLAG_EXIST:
			Log.e("linp","###startCheckVoicePrintStatus readIntentExtraFromVoicePrint() LAUNCHER_TASK_FLAG_EXIST");
			getDragController().enterVoiceModeByVoicePrint(LAUNCHER_TASK_FLAG_EXIST);
			break;

		case LAUNCHER_TASK_FLAG_NOT_EXIST:
			Log.e("linp","###startCheckVoicePrintStatus readIntentExtraFromVoicePrint() LAUNCHER_TASK_FLAG_NOT_EXIST");
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP,"voicewakeup_lock");
			wakeLock.acquire();
			if(wakeLock.isHeld()){
				wakeLock.release();
			}
			bAppRestart = true;
			break;

		default:
			break;
		}
	}

	private boolean enableExpandStatusBar = true;

	public boolean isEnableExpandStatusBar() {
		return enableExpandStatusBar;
	}

	class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				enableExpandStatusBar = true;
				break;
			default:
				enableExpandStatusBar = false;
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
	
	/***/
	public boolean isHomeKeyPress(){
		return isHomeKeyPress;
	}
	
	public void setHomeKeyVal(boolean b){
		isHomeKeyPress = b;
	}
	
	/**
	 * vulcan added it in 2014-7-24
	 * update item info of icon on the desktop
	 * @param newView newView is a new BubbleTextView after reload workspace
	 */
	private void updateDesktopCheckedBubbleTextView(ShortcutInfo newSi, BubbleTextView newView) {
		int ii;
		if(mCheckedBubbleTextViews != null) {
			for(ii = 0;ii < mCheckedBubbleTextViews.size();ii ++) {
				if( mCheckedBubbleTextViews.get(ii).getTag() instanceof ShortcutInfo) {
					ShortcutInfo thisSi = (ShortcutInfo)mCheckedBubbleTextViews.get(ii).getTag();
					if(thisSi.id == newSi.id) {
						mCheckedBubbleTextViews.set(ii, newView);
						newView.setChecked(true);
						Log.d("vulcan-crash",String.format("updateDesktopCheckedBubbleTextView: mCheckedBubbleTextViews %d,%s",ii,newSi.title));
					}
				}
				
			}
		}
		
		if(mCheckedShortcutInfos != null) {
			for(ii = 0;ii < mCheckedShortcutInfos.size();ii ++) {
				if( mCheckedShortcutInfos.get(ii) instanceof ShortcutInfo) {
					ShortcutInfo thisSi = (ShortcutInfo)mCheckedBubbleTextViews.get(ii).getTag();
					if(thisSi.id == newSi.id) {
						//mCheckedShortcutInfos.set(ii, newSi);
						newView.setChecked(true);
						Log.d("vulcan-crash",String.format("updateDesktopCheckedItemInfo: mCheckedShortcutInfos %d,%s",ii,newSi.title));
					}
				}
				
			}
		}
		return;
	}
	
	/**
	 * vulcan added it in 2014-7-24
	 * update item info of icon in the folder
	 * @param newView newView is a new BubbleTextView after reload workspace
	 */
	private void updateFolderCheckedBubbleTextView(ShortcutInfo newSi, BubbleTextView newView) {
		int ii = 0;
		if(mCheckedFolderInfos != null){
			Iterator<FolderInfo> iter = mCheckedFolderInfos.iterator();
			while(iter.hasNext()){
				FolderInfo fi = (FolderInfo)iter.next();
				for(ii = 0;ii < fi.checkInfos.size();ii ++) {
					ShortcutInfo thisSi = fi.checkInfos.get(ii);
					if(thisSi.id == newSi.id) {
						//fi.checkInfos.set(ii, newSi);
						newView.setChecked(true);
						Log.d("vulcan-crash",String.format("updateFolderCheckedItemInfo: %d,%s",ii,newSi.title));
						return;
					}
				}
			}
		}
		return;
	}
	
	/**
	 * vulcan added it in 2014-7-24
	 * this method restore all the checked icon to be imported to folder
	 * we call it after reload workspace.
	 * @param si
	 */
	public void updateCheckedBubbleTextView(ShortcutInfo newSi, BubbleTextView newView) {
		updateFolderCheckedBubbleTextView(newSi,newView);
		updateDesktopCheckedBubbleTextView(newSi,newView);
		return;
	}
	
	/**
	 * member mCheckedShortcutInfos will be expired after reloading workspace,
	 * thus we recreate it's item info
	 */
	private void recreateItemInfoInCheckedShortcutList(HashMap<Long, ItemInfo> itemMap) {
		if(mCheckedShortcutInfos == null) {
			return;
		}
		
		final ArrayList<ShortcutInfo> newShortcutInfoCheckList = new ArrayList<ShortcutInfo>();
		ShortcutInfo newShortcutInfo = null;
		for(ShortcutInfo oldShortcutInfo: mCheckedShortcutInfos) {
			newShortcutInfo = (ShortcutInfo)itemMap.get(oldShortcutInfo.id);
			if(newShortcutInfo == null) {
				continue;
			}
			newShortcutInfoCheckList.add(newShortcutInfo);
		}
		
		mCheckedShortcutInfos.clear();
		
		if(newShortcutInfoCheckList.size() > 0) {
			mCheckedShortcutInfos.addAll(newShortcutInfoCheckList);
		}
		
		for(ShortcutInfo thisShortcutInfo: mCheckedShortcutInfos) {
			if(thisShortcutInfo == null) {
				continue;
			}
			Log.d("vulcan-crash","recreateItemInfoInCheckedShortcutList: thisShortcutInfo: " + thisShortcutInfo.title);
		}
		return;
	}
	
	/**
	 * member mCheckedFolderInfos will be expired after reloading workspace,
	 * thus we recreate it's item info
	 */
	private void recreateItemInfoInCheckedFolderList(HashMap<Long, ItemInfo> itemMap,HashMap<Long, FolderInfo> folderMap){
		if(mCheckedFolderInfos == null) {
			return;
		}
		
		final HashSet<FolderInfo> newFolderInfoCheckList = new HashSet<FolderInfo>();
		
		FolderInfo newFolderInfo = null;
		ShortcutInfo newShortcutInfo = null;
		for(FolderInfo oldFolder: mCheckedFolderInfos) {
			newFolderInfo = folderMap.get(oldFolder.id);
			if(newFolderInfo == null) {
				continue;
			}
			for(ShortcutInfo oldShortcut: oldFolder.checkInfos) {
				newShortcutInfo = (ShortcutInfo)itemMap.get(oldShortcut.id);
				if(newShortcutInfo == null) {
					continue;
				}
				newFolderInfo.checkInfos.add(newShortcutInfo);
			}
			if(newFolderInfo.checkInfos.size() > 0) {
				newFolderInfoCheckList.add(newFolderInfo);
			}
		}
		mCheckedFolderInfos.clear();
		if(newFolderInfoCheckList.size() > 0) {
			mCheckedFolderInfos.addAll(newFolderInfoCheckList);
		}
		
		for(FolderInfo thisFolder: mCheckedFolderInfos) {
			if(thisFolder == null) {
				continue;
			}
			Log.d("vulcan-crash","recreateItemInfoInCheckedFolderList: folder: " + thisFolder.title);
			for(ShortcutInfo thisShortcut: thisFolder.checkInfos) {
				Log.d("vulcan-crash","recreateItemInfoInCheckedFolderList: shortcut: " + thisShortcut.title);
			}
		}
		return;
	}
	
	/**
	 * mEditFolderInfo become expired after reloading workspace,
	 * thus we need to recrate it.
	 * @param itemMap the newest item map reloaded
	 */
	public void recreateEditFolder(HashMap<Long, ItemInfo> itemMap) {
		/**
		 * vulcan added it in 2014-7-24
		 * after reloading workspace, all the items are no longer the original items.
		 * Thus, we have to refresh all the saved variable of type ItemInfo.
		 * After reloading workspace, all the icons are no longer the original icons,
		 * Thus, we have to refresh all the icons to be shown.
		 */
		if (getEditMode() == EditMode.FOLDER_IMPORT) {
			
			if (mEditFolderInfo != null) {
				ItemInfo newItem = itemMap.get(mEditFolderInfo.id);
				if(newItem instanceof FolderInfo) {
					mEditFolderInfo = (FolderInfo)newItem;
				}
			}
		}
	}
	
	/**
	 * vulcan create it in 2014-7-25
	 * @param itemMap
	 * @param folderMap
	 */
	public void recreateCheckList(HashMap<Long, ItemInfo> itemMap,HashMap<Long, FolderInfo> folderMap) {
		recreateItemInfoInCheckedFolderList(itemMap,folderMap);
		recreateItemInfoInCheckedShortcutList(itemMap);
		return;
	}
	
	/**
	 * vulcan create this method in 2014-7-25
	 * @return if it is folder import mode, return true
	 */
	public boolean isFolderImportMode() {
		return getEditMode() == EditMode.FOLDER_IMPORT;
	}

	/**
	 * @return the isWorkspaceBinding
	 */
	public boolean isWorkspaceBinding() {
		return mIsWorkspaceBinding;
	}

	/**
	 * @param mIsWorkspaceBinding the mIsWorkspaceBinding to set
	 */
	public void setIsWorkspaceBinding(boolean mIsWorkspaceBinding) {
		this.mIsWorkspaceBinding = mIsWorkspaceBinding;
	}

	/**set scoller final X value when install new app*/
	@Override
	public void dampedOverScroll() {
		// TODO Auto-generated method stub
		Log.e("linp", "###########dampedOverScroll");
		if(getWorkspace()!=null&&getWorkspace().mScroller!=null){
			getWorkspace().mScroller.setFinalX(getWorkspace().mScroller.getFinalX());
			getWorkspace().requestLayout();
			getWorkspace().invalidate();
		}
	}
	
	/**set Navigation bar background to dark*/
	private void setNavigationbarBackgroundColor(){
		if(VERSION.SDK_INT > 18){
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);
		}
	}
	
	/**restore Navigation bar background */
	public void restoreNavigationbarBackgroundColor(){
		if(VERSION.SDK_INT > 18){
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);
		}
	}
	
	/**set Navigation bar style (dot style)*/
	public void setNavigationbarStyle(){
		if(VERSION.SDK_INT > 20){
		}else if(VERSION.SDK_INT > 18){
			mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}
	
	/**restore Navigation bar style (normal mode)*/
	public void restoreNavigationbarStyle() {
		if(VERSION.SDK_INT > 20){
			
		}else if (VERSION.SDK_INT > 18) {
			if (mLauncherView.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_LOW_PROFILE) {
				mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
	}
	
	/**
	 * vulcan created it in 2014-7-31
	 * @param shortcuts
	 * @param start
	 * @param end
	 */
	public void bindAppsAndWriteBack(final ArrayList<ItemInfo> shortcuts, final int start, final int end) {
				Set<String> newApps = new HashSet<String>();
				newApps = mSharedPrefs.getStringSet(
						InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);
				Workspace workspace = mWorkspace;
				for (int i = start; i < end; i++) {
					final ItemInfo item = shortcuts.get(i);
					
					//three invalid values indicate that we need look for a vacancy now.
					if(item.screen == -1 && item.cellX == -1 && item.cellY == -1) {
						if(Launcher.this == null) {
							continue;
						}
						if(Launcher.this.getWorkspace() == null) {
							continue;
						}
						ItemPos itemPos = Launcher.this.getWorkspace().findFirstVacancyOfWorkspace();
						if(itemPos == null) {
							continue;
						}
						item.screen = itemPos.screen;
						item.cellX = itemPos.x;
						item.cellY = itemPos.y;
					}
					LauncherApplication.logVulcan.print(String.format("bindAppsAndWriteBack: title = %s,screen = %d, x=%d, y=%d",
							item.title,item.screen,item.cellX,item.cellY));
					
					// Short circuit if we are loading dock items for a configuration
					// which has no dock
					if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT
							&& mHotseat == null) {
						continue;
					}
					int count =workspace.getChildCount();
					Log.d("doubleicon",String.format("bindItems: workspace page count = %d",count));
					if (item.screen > count-1&&item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
						for(int j =count;j<=item.screen;j++){
							createNewPage();
						}
					}
					switch (item.itemType) {
					case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
					case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
						ShortcutInfo info = (ShortcutInfo) item;
						String uri = info.intent.toUri(0).toString();
						View shortcut = createShortcut(info);
						
						if (getEditMode() == EditMode.FOLDER_IMPORT
								&& shortcut instanceof BubbleTextView) {
							updateCheckedBubbleTextView(info,(BubbleTextView)shortcut);
						}
						
				        Launcher.this.addQuickIndexItem(info);
						LauncherModel.addItemToDatabase(
								Launcher.this, 
								info,info.container,
								info.screen,info.cellX, info.cellY, false);
						workspace.addInScreen(shortcut, item.container, item.screen,
								item.cellX, item.cellY, 1, 1, false);
						boolean animateIconUp = false;
						synchronized (newApps) {
							if (newApps.contains(uri)) {
								animateIconUp = newApps.remove(uri);
							}
						}
						if (animateIconUp) {
							// Prepare the view to be animated up
							shortcut.setAlpha(0f);
							shortcut.setScaleX(0f);
							shortcut.setScaleY(0f);
							mNewShortcutAnimatePage = item.screen;
							if (!mNewShortcutAnimateViews.contains(shortcut)) {
								mNewShortcutAnimateViews.add(shortcut);
							}
						}
						break;
					}
					}
				workspace.requestLayout();	
	mOnResumeNeedsLoad = false;
}
	
	/**add folder to databases and set it content order by classify result*/
	public void addFolder(String folderName, ItemInfo item,
			List<ShortcutInfo> listFolderContent) {
		//FolderIcon newFolder = setupFolder(folderName, item);
		FolderInfo folderInfo = 	getClassifyFolderInfo(folderName, item);
		
		for (ShortcutInfo info : listFolderContent) {
			if (folderInfo.contents.size()<80)
				folderInfo.add(info);
			else {
				String strNewFolderName = getResources().getString(
						R.string.classify_names_other);
				folderInfo = 	getClassifyFolderInfo(folderName, item);
				folderInfo.add(info);
			}
			mModel.sBgWorkspaceItems.remove(info);
		}
	}
	
	public FolderInfo getClassifyFolderInfo(String folderName,ItemInfo item){
		   CellLayout layout = (CellLayout) getWorkspace().getChildAt(item.screen);
			final FolderInfo folderInfo = new FolderInfo();
			folderInfo.title = folderName;
			LauncherModel.addItemToDatabase(Launcher.this, folderInfo, LauncherSettings.Favorites.CONTAINER_DESKTOP,
					 item.screen, item.cellX,item.cellY, false);
			sFolders.put(folderInfo.id, folderInfo);
			return folderInfo;
	}
   
	/***writePagesInDBCompleted function means we finish item binding and all the page is already under control*/
   @Override
   public void writePagesInDBCompleted() {
   	// TODO Auto-generated method stub
	   Log.e("linp", "++++++++++++++++writePagesInDBCompleted+++++++++++++++");
		btnClassifyOk.setClickable(false);
		btnCalssifyCancel.setClickable(false);
	   if(getEditMode() == EditMode.CLASSIFICATE_ICONS){
		   mFolderImportHint.setText(R.string.classify_app_complete);
           Log.e("linp", "writePagesInDBCompleted Enter CLASSIFICATE_ICONS mode");
			btnClassifyOk.setClickable(true);
			btnClassifyOk.setTextColor(getResources().getColor(R.color.folder_import_txt_normal_color));
			btnCalssifyCancel.setTextColor(getResources().getColor(R.color.folder_import_txt_normal_color));
			btnCalssifyCancel.setClickable(true);
			bClassifyCompleted = false;
			endRequestAni();
			getWorkspace().setCurrentPagesAlpha(1.0f);
	   }
	   if(bPullbackAction){
		   endRequestAni();
		   exitEditMode(true);
		   mFolderImportHint.setText(R.string.folder_import_hint);
		   mFolderImportHintContainer.setVisibility(View.INVISIBLE);
		   bPullbackAction = false;
		   bClassifyCompleted = false;
		   getWorkspace().setCurrentPagesAlpha(1.0f);
	   }
	   
	   //AURORA_START iht 2014-12-10
	   if(mWorkspace != null){
		   mWorkspace.before_edit_pagesCount = mWorkspace.getChildCount();
	   }
	   //AURORA_END
   }
   
   private void showClassifyResultAlertDialog(){
	   if(ClassifyAlertDialg == null){
		   ClassifyAlertDialg = new AuroraAlertDialog.Builder(this, AlertDialog.THEME_TRADITIONAL)
			.setTitle(R.string.classify_app_title)
			.setMessage(R.string.classify_app_msg)
			.setNegativeButton(R.string.classify_app_btn_restore,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							restoreBackupAfterClassifyResult();
						}
					})
					
			.setPositiveButton(R.string.classify_app_btn_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							acceptClassifyResult();
						}
					})
			.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
			
			}
	   }).create();
	   }
	   ClassifyAlertDialg.show();
   }
   
   /**accept classify result and will invoked by alert dialog or button click*/
   private void acceptClassifyResult(){
	   //add by xiexiujie for calender icon bug 14.11.24 start
	   isInClassifyModel=false;
	   //add by xiexiujie for calender icon bug 14.11.24 end
		if(!bClassifyCompleted){
			executeExitEditMode(true);
			//bClassifyCompleted = false;
			btnCalssifyCancel.setClickable(false);
			mFolderImportHintContainer.setVisibility(View.GONE);
			btnClassifyOk.setTextColor(getResources().getColor(R.color.folder_import_txt_invalid_color));
			btnCalssifyCancel.setTextColor(getResources().getColor(R.color.folder_import_txt_invalid_color));
			mFolderImportHint.setText(R.string.folder_import_hint); // restore it 
		}
   }
   
   /**restore backup data after classify result and will invoked by alert dialog or button click*/
   private void restoreBackupAfterClassifyResult(){
	   //add by xiexiujie for calender icon bug 14.11.24 start
	   isInClassifyModel=false;
	 //add by xiexiujie for calender icon bug 14.11.24 end
		if(!bClassifyCompleted){
			mHandler.removeCallbacks(restoreBackupAfterClassifyReusltRunnable);
			mHandler.post(restoreBackupAfterClassifyReusltRunnable);
		}
   }
   
   /**start classify loading animation*/
   private void startRequestAni(){
        mClassifyAnimationLayout.setVisibility(View.VISIBLE);
	    clsAnimation.startRequestAni();
   }
   
   /**stop classify loading animation*/
   private void endRequestAni(){
	   mClassifyAnimationLayout.setVisibility(View.GONE);
	   clsAnimation.endRequestAni();
   }
   
   /**checking the classify loading animation is running or not*/
   public boolean isRequestAniRunning(){
	   return clsAnimation.isRequestAniRunning();
   }
   
   /**
    * @desc find the total icon without the folder and send notification 
    * @author hazel
    * @date 2014/11/19
    * */
   public void findSpareIconsAndSendNotification(){
	   int count = 0;
	   for(ItemInfo info : mModel.sBgWorkspaceItems){
		   if( info instanceof ShortcutInfo){
			   int flag  = ((ShortcutInfo)info).flags;		   
				if ((flag& android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 && (info.screen!=0)) {
					if(count>10){
						break;
					}
					count++;
				}
		   }
	   }
	     
	   if(getWorkspace().getChildCount()>MAX_PAGE_FOR_SMART_CLASSIFY && count >10 ){
		   //send notification 
		   String title  = getString(R.string.classify_app_title);
		   String content = getString(R.string.classify_app_send_notification_content); 
		   
	       NotificationManager notificationManager = (NotificationManager)    
	           this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);   
		   
				   
	       Notification notification =new Notification(R.mipmap.ic_launcher_home,
	    		   title, System.currentTimeMillis()); 
	       notification.flags |= Notification.FLAG_AUTO_CANCEL;  
	      
	       Intent notificationIntent =new Intent(this, Launcher.class); 
	       notificationIntent.putExtra("EXTRA_CLASSIFY_ICONS", true);
	       PendingIntent contentItent = PendingIntent.getActivity(this, 0, notificationIntent, 0);   
	       notification.setLatestEventInfo(this, title, content, contentItent);   
	        
	       notificationManager.notify(0, notification);   
	   }

   }
  
   /**start smart classify application */
   public void startSmartClassify(){
		if(getWorkspace().getCurrentPage()>0){
			
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//add by xiexiujie for calender icon bug 14.11.24 start;
					isInClassifyModel=true;
					//add by xiexiujie for calender icon bug 14.11.24 end;
					Workspace workspace = getWorkspace();
					int scrolToPage = 0;
					int currentPage = workspace.getChildCount()-1;
					int duration=Workspace.PAGE_SNAP_ANIMATION_DURATION+(currentPage - scrolToPage )*120;
					workspace.snapToPage(scrolToPage,duration>1000?1000:duration);
				}
			}, 300);

	
		}
		mHandler.postDelayed(new Runnable() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				getWorkspace().setCurrentPagesAlpha(0.2f);
				enterEditMode(EditMode.CLASSIFICATE_ICONS, true);
				mFolderImportHintContainer.setVisibility(View.VISIBLE);
				mFolderImportHint
						.setText(R.string.classify_app_sorting);
				onEnterFullScreen(); 
				startRequestAni();
				bClassifyCompleted = true;
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mModel.classifyApp();
					}
				}, 1000);
			}
		}, getWorkspace().mScroller.getDuration() + 700);
   }
   
}
interface LauncherTransitionable {
	View getContent();

	void onLauncherTransitionPrepare(Launcher l, boolean animated,
			boolean toWorkspace);

	void onLauncherTransitionStart(Launcher l, boolean animated,
			boolean toWorkspace);

	void onLauncherTransitionStep(Launcher l, float t);

	void onLauncherTransitionEnd(Launcher l, boolean animated,
			boolean toWorkspace);
}
