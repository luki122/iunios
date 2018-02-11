package com.aurora.voiceassistant.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.CollationKey;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import org.json.JSONObject;

import com.aurora.internal.R.color;
import com.aurora.voiceassistant.*;
import com.aurora.voiceassistant.view.BarCodeMainActivity;
import com.aurora.voiceassistant.account.TotalCount;
import com.aurora.voiceassistant.model.AppsItem;
import com.aurora.voiceassistant.model.CFG;
import com.aurora.voiceassistant.model.ContactsItem;
import com.aurora.voiceassistant.model.Event;
import com.aurora.voiceassistant.model.LoadData;
import com.aurora.voiceassistant.model.QuizText;
import com.aurora.voiceassistant.model.Response;
import com.aurora.voiceassistant.model.RkLog;
import com.aurora.voiceassistant.model.Tts;
import com.aurora.voiceassistant.model.TtsRes;
import com.aurora.voiceassistant.view.ContentViewConstructor.onClickListener;
import com.aurora.voiceassistant.view.CustomerScrollView.CustomerScrollViewListener;
import com.gionee.featureoption.FeatureOption;
import com.google.gson.internal.ConstructorConstructor;
import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.sogou.tts.offline.TTSPlayer;
import com.wowenwen.yy.api.RequestListener;
import com.wowenwen.yy.api.SogouYYSDK;

import android.R.integer;
import android.R.layout;
import android.R.string;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.StaticLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.content.ContentResolver;
import android.database.Cursor;
//shigq
import aurora.widget.AuroraListView;
import aurora.widget.AuroraEditText;

import gionee.telephony.GnTelephonyManager;
//import gionee.telephony.GnTelephony.SIMInfo;
import gionee.provider.GnCallLog.Calls;
import android.location.CountryDetector;
import gionee.provider.GnTelephony.SIMInfo;
import android.content.pm.ResolveInfo;
import com.aurora.utils.Utils2Icon;

public class MainActivity extends Activity implements OnClickListener, CustomerScrollViewListener {
	private boolean mExtState  = true;
	private Button toMicBtn;
	private Button sendBtn;
	private AuroraEditText inputEdit;
	private Button toInputBtn;
	private Button micBtn;
	private Context context;
	private VoiceAssistant va;
	//private ProgressDialog dialog = null;
	private Tts tts;

	private boolean isVp = false;
	private static final String vp_key = "vopen";
	
	private static boolean platform_U2;

	public static final int MSG_TYPE_TTS = 0;
	public static final int MSG_TYPE_SPEECH = 1;
	public static final int MSG_TYPE_UPDATE_FILLVIEW = 2;
	public static final int MSG_TYPE_TIMER = 3;
	public static final int MSG_GREETING = 7;
	public static final int MSG_BOBAO = 8;
	public static final int MSG_TYPE_SHOW_REQUEST_TEXT = 9;
	
	public static final int MSG_TYPE_OFFLINE_VOICE_INPUT = 15;
	public static final int MSG_TYPE_OFFLINE_MESSAGE_STATE = 16;

	private final String TAG = "Voice_Assistant";

	private CustomMenuProxy mCustomMenuProxy;
	private static final int CLEAR_SCREEN = 0;
	private static final int MENU_SETTINGS = 1;

	public Intent mSlaveToMasterIntent;
	public IntentFilter mMasterToSlaveFilter;

	String hintString;
	boolean actionDown = false;
	boolean preventTouch = false;
	private static String searchContent = null;

	private static final String vswitch = "vswitch"; // voiceSettingInfo
	private SharedPreferences mVoiceSettingSharePreference;

	// miche(播报授权交互)
	private boolean isSetting;
	
    //For scrollView
	private RelativeLayout mVoiceMainLayout;
	private LinearLayout mVoiceContentLayout;
	private View mView;
	private int listoutlineheight = 0;
	private CustomerScrollView mScrollView;
	private LayoutManager mLayoutManager;
	private ContentViewConstructor mViewConstructor;
	private LinearLayout mBaseItemView;
	private LayoutInflater mLayoutInflater;
	private LoadData mLoadData;
	private boolean isNeedScroll = false;

	private String inputIsShowing = "android.intent.action.ACTION_INPUT_METHOD_SHOW";
	private String inputIsHiden = "android.intent.action.ACTION_INPUT_METHOD_HIDE";
	/**For voice print broadcast */
	private String VOICE_COLLECT_ACTION = "com.iuni.voiceassistant.ACTION_VOICE_COLLECT";
	private String ACTION_VOICE_CLEAR_SCREEN = "com.iuni.voiceassistant.ACTION_VOICE_CLEAR_SCREEN";
	
	private boolean inputState = false;
	
	//弹动
	private LinearLayout mVoiceOutLine;
	private int flag = 0;
	private float starty = 0f;
	private float endy = 0f;
	private float trany = 0f;
	private float tag = 0f;
	private int scrolly = 0;
	private int offset = 0;
	
	//输入框（一整横条）
	private int fillviewheight;
	private int check;
	private RelativeLayout input_state;
	private RelativeLayout normal_state;
	private boolean input_line_ishow;
	
	//greeting
	private AnimatorSet atorSet;
	private int diswidth;
	private int disheight;
	private LinearLayout greeting;
	private TextView hello_text;
	private LinearLayout greetingqp;
	private TtsRes myttsRes;
	private GreetingTimeRecord greetingRecord;
	
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
		
	//ispause
	private boolean isPause;
	private boolean isAudioInit;
	
	//加载动画
	private RequestAnimation requestAni;
	
	private AnimationDrawable mVoiceInputEnter;
	private AnimationDrawable mVoiceInputExit;
	private boolean inputSwitchAnimFlag = false;
	
	private enum VAState{
		IN, OUT
	}
	private VAState mVAState = VAState.OUT;
	private boolean clearScreen = false;
	
	enum HideState{
		NewResult, ExitVoice
	}
	private HideState mHideState = HideState.NewResult;
		
	private int SCROLL_Y;
	
	private float mainLayoutHeight;
	ViewGroup.LayoutParams lParams;
	
	private Button barCodeButton;
	private Button barCodeButtonSmall;
	private ImageView barCodeDivideLine;
	private static final int BARCODE = 2;
	
	//Offline start
	private ContentResolver mContentResolver;
	private TelephonyManager mTelephonyManager;
	private String MESSAGE_SENT_ACTION = "com.aurora.mms.transaction.MESSAGE_STATUS_CHANGED";
	private String MESSAGE_SENT_ACTION2 = "com.android.mms.transaction.MESSAGE_SENT";	
	public enum OFFLINE_TYPE {
		NONE, CALLING, MESSAGE, CONTACTS, APPLICATION, TIPS
	}
	private OFFLINE_TYPE mOffLineType = OFFLINE_TYPE.NONE;
	private StringBuilder mOfflineAnswer = new StringBuilder();
	
	public static final String SUBSCRIPTION_KEY  = "subscription";
		
	private Uri contentUri;
	final String AUTHORITY = "com.aurora.launcher.settings";
	final String TABLE_FAVORITES = "favorites";
    final String TABLE_FAVORITES_BACKUP= "favorites_backup";
    final String TABLE_APP_CATEGORY = "app_info_category";
    final String PARAMETER_NOTIFY = "notify";
    
    final String TITLE = "title";
    final String SIMPLE_PINYIN = "simplePinyin";
    final String FULL_PINYIN = "fullPinyin";
    
    final String ITEM_TYPE = "itemType";
    final int ITEM_TYPE_APPLICATION = 0;
    final int ITEM_TYPE_SHORTCUT = 1;
    private static final String VS_INTENT = "#Intent;action=android.intent.action.MAIN;"+
											"category=android.intent.category.LAUNCHER;"+
											"launchFlags=0x10200000;component=com.aurora.voiceassistant/.view.MainActivity;end";
    
    boolean isOnLine = false;
    private boolean isVoiceInput = false;
    private ArrayList<ContactsItem> mContactsItemList = null;
    private ArrayList<AppsItem> mAppsItemList = null;

    private Timer mCallTimer = null;
	private TimerTask mCallTimerTask = null;
	private ProgressBar mCallProgressBar = null;
    
    private boolean mVoiceInputForOfflineMessage = false;
    private AuroraEditText targetEditText = null;
    private LinearLayout mSendingMessageView;
    private String SHOW_VOICE_INPUT_TIPS = "showtips";
    
    private static float sDensity;
	//Offline end
    
    ArrayList<Event> mEventList = new ArrayList<Event>();
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (MSG_GREETING == msg.what) {
				Log.d("DEBUG", "the handle message =====MSG_GREETING=========");
				myttsRes = (TtsRes) msg.obj;
				noVoiceGreeting();
				registerReceiver(mMasterToSlaveReceiver, mMasterToSlaveFilter);
				
				//account start
				startToCount(context, CFG.ACCOUNT_ACTION_ENTER, 1);
				//account end

			} else if (MSG_BOBAO == msg.what) {
				Log.d("DEBUG", "the handle message =====MSG_BOBAO=========");
				isVp = readVSwitch();
				if (myttsRes != null && isAudioInit() && mVAState == VAState.IN) {
					if (isVp && myttsRes.flag && null != tts && isPause()) {
						tts.play(myttsRes.text);
					}
				}
				
			} else if (MSG_TYPE_TTS == msg.what) {
				Log.d("DEBUG", "the handle message =====MSG_TYPE_TTS=========");
				
				if(requestAni != null){
					if(requestAni.isRuning()){
						requestAni.endRequestAni();
						requestAni.setVisibility(View.GONE);
					}
				}

				if (va != null && mLayoutManager != null) {
					Response response = va.getLoadData().getResponse();
					resetFillViewHeight();
					
					// 播报
					isVp = readVSwitch();
					TtsRes ttsRes = (TtsRes) msg.obj;
					
					//Offline start					
					if (mOffLineType != OFFLINE_TYPE.NONE && mOffLineType != OFFLINE_TYPE.TIPS) {
						if (mOfflineAnswer != null) {
							ttsRes.text = mOfflineAnswer.toString();
						}
					}
					response.setAnswerString(ttsRes.text);
					//Offline end
					
					//mLayoutManager.updateLayoutManager(response,mViewConstructor);
					mLayoutManager.layoutToShowResult(response, mViewConstructor);
					
					if(isAudioInit() && mVAState == VAState.IN){
						if (isVp && ttsRes.flag && null != tts && isPause()) {
							tts.play(ttsRes.text);
						}
					}
					mScrollViewStartToScroll();
        		 }
        		      			
         	 } else if(MSG_TYPE_SPEECH == msg.what) {
        		 LinearLayout speechTip = (LinearLayout)findViewById(R.id.speech_tip);
 				 speechTip.setVisibility(View.GONE);
 				 
        	 } else if (MSG_TYPE_UPDATE_FILLVIEW == msg.what) {
//        		 Log.d("DEBUG", "the handle message =====MSG_TYPE_UPDATE_FILLVIEW");
        		 //calculateFillViewHeight();
        		 
        		 if (msg.getData().getInt("updatebyclearscreen") == 0) {
        			 updateFillViewHeight();
        		 } else {
        			 int height = msg.getData().getInt("updatebyclearscreen");
        			 updateFillViewHeight(height);
        		 }
        		 
        	 } else if (msg.what == MSG_TYPE_TIMER){
        		 Log.d("DEBUG", "the handle message =====MSG_TYPE_TIMER=========time out and need to clear screen");
        		 if (mVAState == VAState.OUT) {
        			 mLayoutManager.clearAllItems();
            		 greetingRecord.setAllTimeState(false);
            		 if (va != null) {
						va.setAllTimeState(false);
            		 }
            		 mTimer = null;
            		 mTimerTask = null;
        		 }
        		 //Offline start
        		 if (mViewConstructor != null) {
					mViewConstructor.unRegistAllLayout();
					mViewConstructor.clearLayoutIdMap();
        		 }
        		 //Offline end
        		 
  			} else if(msg.what == MSG_TYPE_SHOW_REQUEST_TEXT){
  				Log.d("DEBUG", "the handle message =====MSG_TYPE_SHOW_REQUEST_TEXT=========");
  				final String string = msg.obj.toString();
  				mScrollView.post(new Runnable() {
					@Override
					public void run() {
						resetFillViewHeight();
						mLayoutManager.layoutToShowRequest(string);
						mScrollViewStartToScroll();
					}
				});
  				
			} 
			//Offline start
  			else if (msg.what == MSG_TYPE_OFFLINE_VOICE_INPUT) {
  				String contentString = (String) msg.obj;
  				if (targetEditText != null) {
  					int index = targetEditText.getSelectionStart();
  					if (index == -1) {
  						targetEditText.append(contentString);
					} else {
						Editable editable = targetEditText.getText();
						editable.insert(index, contentString);
					}
				}
			}
			//Offline end
		}
	};        	
         
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{	
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		setContentView(R.layout.vs_activity_main);
		
		registerVoiceCollectBroadCastReceivcer();

		platform_U2 = getResources().getBoolean(R.bool.platform_type_U2);
		
//		isBarCodeScannerInstalled();
		showBarCodeIcon();

		mVoiceMainLayout = (RelativeLayout) findViewById(R.id.vs_main);
		mainLayoutHeight = getResources().getDimension(R.dimen.vs_main_height);
		lParams = mVoiceMainLayout.getLayoutParams();
		Log.d("DEBUG", "mainLayoutHeight = "+mainLayoutHeight);
		
		input_state = (RelativeLayout) findViewById(R.id.input_state); 
		normal_state = (RelativeLayout) findViewById(R.id.normal_state);
		toMicBtn = (Button) findViewById(R.id.btn_to_mic);
		micBtn = (Button) findViewById(R.id.btn_voice);
		sendBtn = (Button) findViewById(R.id.btn_send);
		toInputBtn = (Button) findViewById(R.id.to_input_btn);
		inputEdit = (AuroraEditText) findViewById(R.id.edit_input);

		hintString = getResources().getString(R.string.vs_input_text);
		inputEdit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_SEND) {     
					setEnterKeyAsSend();
				}
				return false;
			}
		});
		inputEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
				
				if (hasFocus) {
					inputEdit.setHint("");
            		im.showSoftInput(inputEdit, 0);
				} else {
//					inputEdit.setHint(hintString);
            		im.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
				}
			}
		});
		
		toMicBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		toInputBtn.setOnClickListener(this);
		micBtn.setOnClickListener(this);
		
		context = this;
		va = new VoiceAssistant(context,handler);
		tts = new Tts(context);
		va.getMainActivity(this);
		
		if(mVoiceSettingSharePreference == null){
			mVoiceSettingSharePreference = getApplicationContext().getSharedPreferences(vswitch, Context.MODE_PRIVATE);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Looper.prepare();
				setAudioInit(tts.init()); 
				setInitBool();
				Looper.loop();
			}
		}).start();

		mCustomMenuProxy = (CustomMenuProxy) findViewById(R.id.vs_custom_menu_proxy_id);
		mCustomMenuProxy.setAnimationStyle(R.style.PopupAnimation);
		mCustomMenuProxy.setCustomMenuItemListener(mCustomMenuItemClickListener);
		mCustomMenuProxy.getMainActivity(this);

		mSlaveToMasterIntent = new Intent();
		mMasterToSlaveFilter = new IntentFilter();
		mMasterToSlaveFilter.addAction(DismissMenuByHomeKey);
		mMasterToSlaveFilter.addAction(ToStopPlayVoice);
		mMasterToSlaveFilter.addAction(ToCheckGreeting);
		// mMasterToSlaveFilter.addAction(AUTHORIZE_ACTION);
		mMasterToSlaveFilter.addAction(inputIsShowing);
		mMasterToSlaveFilter.addAction(inputIsHiden);
		mMasterToSlaveFilter.addAction(Intent.ACTION_SCREEN_OFF);
		
		//Offline start
		mMasterToSlaveFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mMasterToSlaveFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mMasterToSlaveFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mMasterToSlaveFilter.addAction(MESSAGE_SENT_ACTION);
		//Offline end
		
		// registerReceiver(mMasterToSlaveReceiver, mMasterToSlaveFilter);

		// For scrollView
		mVoiceContentLayout = (LinearLayout) findViewById(R.id.listcontent);
		mLayoutManager = new LayoutManager(this, handler, mVoiceContentLayout);
		va.getLayoutManager(mLayoutManager);
		mLayoutManager.getMainActivity(this);

		mViewConstructor = new ContentViewConstructor(this, handler);
		mViewConstructor.getMainActivity(this);
		// va.getContentViewConstructor(mViewConstructor);

		mScrollView = (CustomerScrollView) findViewById(R.id.scrollview);
		mScrollView.setSmoothScrollingEnabled(true);
		mScrollView.setFillViewport(true);
		mScrollView.setListener(this);
		
		mVoiceOutLine = (LinearLayout)findViewById(R.id.listoutline);
		lineoutlineAnimation();
		
		greetingRecord = new GreetingTimeRecord();
		setPause(true);
		// 第一次
		va.yySDKInit(true);
		// 欢迎动画
		initAnimation();

		mView = (View) findViewById(R.id.fillview);
		mLayoutInflater = LayoutInflater.from(this);

		isVp = readVSwitch();
		
		mVAState = VAState.IN;
		
		//请求动画隐藏
		initRequtestAni();
		
		//Offline start
		mTelephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, 
									PhoneStateListener.LISTEN_SERVICE_STATE |
									PhoneStateListener.LISTEN_SIGNAL_STRENGTH |
									PhoneStateListener.LISTEN_CALL_STATE |
									PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
									PhoneStateListener.LISTEN_DATA_ACTIVITY);
		mContentResolver = getContentResolver();
		
		sDensity = getResources().getDisplayMetrics().density;
		//Offline end
	}
	
	private void initRequtestAni(){
		requestAni = (RequestAnimation) findViewById(R.id.request_ani);
		requestAni.setVisibility(View.GONE);
	}
	
	private void initAnimation() {
		diswidth = getWindowManager().getDefaultDisplay().getWidth();
		disheight = getWindowManager().getDefaultDisplay().getHeight();

		greeting = (LinearLayout) findViewById(R.id.greeting);
		hello_text = (TextView) findViewById(R.id.hello_text);
		hello_text.setText(getGreetingWord());

		greeting.setTranslationY(disheight - 200);
		greeting.setTranslationX((diswidth - 430)/2);
		
		//greeting_qipao;
		greetingqp = (LinearLayout) findViewById(R.id.greeting_qp);
		greetingqp.setTranslationY(80f);

		greetingAnimatorSet();
	}

	private void greetingAnimatorSet() {
		// 启动位置
		float starty = greeting.getTranslationY();
		float startx = greeting.getTranslationX();

		atorSet = new AnimatorSet();

		// 第一阶段上浮
		ObjectAnimator tran = ObjectAnimator.ofFloat(greeting, "translationY",starty, starty / 2);
		tran.setDuration(850);
		tran.setInterpolator(new OvershootInterpolator(1.5f));

		// 第二阶段对角
		AnimatorSet atorset2 = new AnimatorSet();
		// 偏移
		AnimatorSet tranSet = new AnimatorSet();
		ObjectAnimator trany2 = ObjectAnimator.ofFloat(greeting,"translationY", starty / 2, 30);
		ObjectAnimator tranx2 = ObjectAnimator.ofFloat(greeting,"translationX", startx, 0);
		tranSet.playTogether(tranx2, trany2);

		// 透明1-0
		ObjectAnimator alpha2 = ObjectAnimator.ofFloat(greeting, "alpha", 1,0.7f);

		// 缩放
		AnimatorSet atorScale = new AnimatorSet();
		ObjectAnimator scalex = ObjectAnimator.ofFloat(greeting, "scaleX",0.43f);
		ObjectAnimator scaley = ObjectAnimator.ofFloat(greeting, "scaleY",0.43f);
		atorScale.playTogether(scalex, scaley);

		// 获得气泡布局
		float qpstartx = greetingqp.getTranslationY();
		AnimatorSet qSet = new AnimatorSet();
		ObjectAnimator qtran = ObjectAnimator.ofFloat(greetingqp,"translationY", qpstartx, 0);
		ObjectAnimator qAlpha = ObjectAnimator.ofFloat(greetingqp, "alpha",0.0f, 1f);
		qSet.playTogether(qtran, qAlpha);

		atorset2.setDuration(500);
		atorset2.setInterpolator(new DecelerateInterpolator());
		atorset2.playTogether(tranSet, alpha2, atorScale, qSet);

		// 第三阶段隐藏凸显
		AnimatorSet disSet = new AnimatorSet();
		// 透明1-0
		ObjectAnimator alphad = ObjectAnimator.ofFloat(greetingqp, "alpha", 1,0.3f);
		// 透明0-1
		ObjectAnimator alpha = ObjectAnimator.ofFloat(mVoiceOutLine, "alpha",0, 1);
		alpha.setInterpolator(new AccelerateInterpolator(0.5f));

		alpha.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				greeting.setVisibility(View.GONE);
				handler.sendEmptyMessage(MSG_BOBAO);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				greetingqp.setVisibility(View.GONE);
				mVoiceOutLine.setAlpha(1.0f);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				if(greeting.getVisibility() != View.GONE){
					greeting.setVisibility(View.GONE);
				}
				if(greetingqp.getVisibility() != View.GONE){
					greetingqp.setVisibility(View.GONE);
				}
				mVoiceOutLine.setAlpha(1.0f);
			}
		});
		disSet.setDuration(200);
		disSet.playTogether(alphad, alpha);

		// 动画集合第二、三阶段
		AnimatorSet displaySet = new AnimatorSet();
		displaySet.play(disSet).after(atorset2);

		if(atorSet != null){
			//动画（二、三）阶段，结合动画一
			atorSet.play(displaySet).after(tran);
			atorSet.start();
		}
	}
	
	private void lineoutlineAnimation(){
		mScrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//to hide the input method on touch in VOICE mode
                if (inputState) {
                	InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
            		im.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
        		}
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					break;
					
				case MotionEvent.ACTION_MOVE:
					
					if(flag == 0){
						starty = event.getY();
						flag = 1;
					}
					endy = event.getY();
					
					offset = mVoiceOutLine.getMeasuredHeight() - mScrollView.getHeight(); //定值
					scrolly = v.getScrollY();
//					tag = (endy - starty)/6;
					tag = (endy - starty)/3;
					
					if(scrolly == 0){
						if(tag > 0){
							mVoiceOutLine.setTranslationY(tag);
						}
					}
					trany = mVoiceOutLine.getTranslationY();
					
					
					if(trany > 0 && scrolly > 0 ){
						if(tag > 0){
							mVoiceOutLine.setTranslationY(tag);
						}
					}

					if (scrolly == offset) {
						mVoiceOutLine.setTranslationY(tag);
					}
					
					break;
				case MotionEvent.ACTION_UP:
					flag = 0;
					scrolly = 0;
					tag = 0;
					trany = 0;
					addAnimation(mVoiceOutLine.getTranslationY());
					break;
				}
				return false;
			}
		});
	}
	
	private void addAnimation(float y){
		TranslateAnimation tran = new TranslateAnimation(0, 0, y, 0);
		tran.setDuration(300);
		mVoiceOutLine.startAnimation(tran);
		mVoiceOutLine.setTranslationY(0);
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mCustomMenuProxy.isShowing()) {
			dismissCustomMenu();
		}
		InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
		if (im.isActive(inputEdit)) {
			im.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
		}
		mLayoutManager.dismissClearScreenDialog();
		mLayoutManager.dismissDeleteDialog();
		mLayoutManager.dismissPopupWindowMenu();
	}
	
	private String VoiceInputChanged_Send = "voiceinputchanged";
	public void sendBroadCastToMaster(String action, String valuename, boolean value) {
		mSlaveToMasterIntent.setAction(action);
		mSlaveToMasterIntent.putExtra(valuename, value);
		this.sendBroadcast(mSlaveToMasterIntent);
	}

	private String DismissMenuByHomeKey = "dismissmenubyhomekey";
	private String ToStopPlayVoice = "stopplayvoice";
	private String ToCheckGreeting = "checkgreeting";
	BroadcastReceiver mMasterToSlaveReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String receiveString = intent.getAction();
			if (receiveString.equals(DismissMenuByHomeKey)) {
				Log.d("DEBUG", "homekeytodismissmenu received = "+intent.getExtras().getBoolean("homekeypressed"));
				if (mCustomMenuProxy.isShowing()) {
					dismissCustomMenu();
				}
				//取消相关dialog
				mLayoutManager.dismissClearScreenDialog();
				mLayoutManager.dismissDeleteDialog();
				mLayoutManager.dismissPopupWindowMenu();

			}
			
			//电源键
			if(receiveString.equals(Intent.ACTION_SCREEN_OFF)){
				if (null != tts) {
					tts.stop(); 
				}
			}

			if (receiveString.equals(ToStopPlayVoice)) {
				Log.d("DEBUG", "ToStopPlayVoice received = "+intent.getExtras().getBoolean("ToStopPlayVoice"));
				mLayoutManager.destroyAllWebView();
				mLayoutManager.hidePreviousItems(HideState.ExitVoice);
				
				//Offline start
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						dismissCallingView();
					}
				});

				/*cancelCallingTimer();
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mLayoutManager.hideCallingViewOrContactForOffline();
					}
				});*/
				//Offline end
				
				setStatusBarBG(false);		//to make sure the state of satus can be clear after setted
				
				mVAState = VAState.OUT;
				if(va != null){
					va.cancelRequestText(); //同时取消请求
				}
				
				if(requestAni != null){
					if(requestAni.isRuning()){
						/*if(va != null){
//							va.cancelRequestText(true); //同时取消请求
							va.cancelRequestText(); //同时取消请求
						}*/
						requestAni.endRequestAni();
						requestAni.setVisibility(View.GONE);
					}
				}
				
				if(va != null){
					va.stopLocationService();
				}
				
				if (null != tts) {
					tts.stop(); 
				}
				if (inputEdit.getText().length() > 0) {
					inputEdit.setText("");
				}
				
				//To clear all of items after 1 hour
				startScreenClearTimer();
				
				if (getWindow().getDecorView() != null) {
					getWindow().getDecorView().clearFocus();
				}
								
			}
			
			if (receiveString.equals(ToCheckGreeting)) {
//				Log.d("DEBUG", "tocheckgreeting received = "+intent.getExtras().getBoolean("checkgreeting"));
				mLayoutManager.showHidenWebViewInLastItem();
				
				mVAState = VAState.IN;
				
				//To cancel the timer if user enter voice mode within 1 hour
				cancelScreenClearTimer();
				
				Date curDate = new Date(System.currentTimeMillis());
				int hour = curDate.getHours();
				if (hour < 6) {
					checkGreetingRecord(1, true);
				} else if (hour >= 6 && hour < 11) {
					checkGreetingRecord(2, true);
				} else if (hour >= 11 && hour < 13) {
					checkGreetingRecord(3, true);
				} else if (hour >= 13 && hour < 18) {
					checkGreetingRecord(4, true);
				} else if (hour >= 18) {
					checkGreetingRecord(5, true);
				} else {
					checkGreetingRecord(1, true);
				}
				
				//如果已经开启定位
				va.startLocationService();
				
				//account start
				startToCount(context, CFG.ACCOUNT_ACTION_ENTER, 1);
				//account end
			}
			//监听键盘事件
			if (receiveString.equals(inputIsShowing)) {
				inputState = true;
				
				setMainLayoutParams();
				
				input_state.setBackgroundResource(R.drawable.vs_bottom_interlayer);
			}
			
			if (receiveString.equals(inputIsHiden)) {
				inputState = false;
				
				clearMainLayoutParams();
				
				showBackGroundForInputArea();
			}
			
			//Offline start
			if (receiveString.equals(WifiManager.RSSI_CHANGED_ACTION)
		            || receiveString.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
		            || receiveString.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

		        updateWifiState(intent);
		    }
			
			if (receiveString.equals(MESSAGE_SENT_ACTION)) {
				String id = intent.getExtras().getString("third_response");
				Log.d("DEBUG", "MESSAGE_SENT_ACTION------------------------------id = "+id);
				String uri = intent.getExtras().getString("uri");
				Log.d("DEBUG", "MESSAGE_SENT_ACTION------------------------------uri = "+uri);
				int errorCode = intent.getIntExtra("errorCode", 0);
				
				if (id == null && uri == null) return;
				
				mViewConstructor.updateMessageSentState(id, uri, errorCode);
			}
			//Offline end
		}
	};

	private void pointTimeGreeting() {
		mVoiceOutLine.setAlpha(0.0f);
		va.checkTimeToPushGreet();

		greeting.setVisibility(View.VISIBLE);
		greeting.setScaleX(1.0f);
		greeting.setScaleY(1.0f);
		greetingqp.setVisibility(View.VISIBLE);
		greetingqp.setAlpha(0.0f);

		initAnimation();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if (getWindow().getDecorView() != null) {
					getWindow().getDecorView().clearFocus();
				}
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				if (getWindow().getDecorView() != null) {
					if (getWindow().getDecorView().hasFocus()) {
						Log.d("DEBUG", "the input length = "+inputEdit.getText().length());
						if (inputEdit.getText().length() > 13) {
							inputEdit.setText("");
						}
						getWindow().getDecorView().clearFocus();
					}
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		InputMethodManager im = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
		
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_HOME:
			//The HOME Key event has been intercepted at "onSaveInstanceState"
			break;
		case KeyEvent.KEYCODE_MENU:
			if (/*!im.isActive(inputEdit) && */event.getRepeatCount() == 0) {
				if (mCustomMenuProxy.isShowing()) {
					dismissCustomMenu();
				} else {
					setStatusBarBG(true);
					mCustomMenuProxy.showAtLocation(findViewById(R.id.vs_main), Gravity.BOTTOM, 0, 0);

				}
			}
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return true;
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(null != va) {
        	va.destory();
        	va = null;
        }
        
        if(null != tts) {
        	tts.deinit();
        	tts = null;
        }

        unregisterReceiver(mMasterToSlaveReceiver);
        unregisterReceiver(mVoicePrintReceiver);
    }
	 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		//RelativeLayout layoutInput = (RelativeLayout)findViewById(R.id.normal_state);
		if(0 == requestCode && 1 == resultCode) {
			normal_state.setVisibility(View.VISIBLE);
			Bundle data = intent.getExtras();
			int retCode = data.getInt("retCode");
			
			isOnLine = data.getBoolean("isOnLine");
			Log.e("DEBUG", "----------------onActivityResult----------------isOnLine------------= "+isOnLine);
			//录音
			if(0 == retCode) {
				if (isOnLine) {
					String content = data.getString("content");
					//dialog = getDialogToShow(context);
					//va.requestText(content,dialog);
//					if (mOffLineType == OFFLINE_TYPE.MESSAGE) {
					if (mVoiceInputForOfflineMessage) {
						Log.d("DEBUG", "voice input for message!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						mVoiceInputForOfflineMessage = false;
						
						Message msg = new Message();
						msg.what = MSG_TYPE_OFFLINE_VOICE_INPUT;
						msg.obj = content.toString();
						//handler.sendMessageDelayed(msg, 90);
						handler.sendMessage(msg);
						return;
					}
					
					startRequestByContent(content);
					
				} else {
					String content = data.getString("content");
					if (content.isEmpty()) return;
					
					startRequestByContent(content);
				}
				/*String content = data.getString("content");
				//dialog = getDialogToShow(context);
				//va.requestText(content,dialog);
				
				setSearchContent(content);
				
				Message msg = new Message();
				msg.what = MSG_TYPE_SHOW_REQUEST_TEXT;
				msg.obj = content.toString();
				//handler.sendMessageDelayed(msg, 90);
				handler.sendMessage(msg);
				
				va.requestText(content, null);
				
				if(requestAni != null){
					if(requestAni.getVisibility() != View.VISIBLE){
						requestAni.setVisibility(View.VISIBLE);
					}
					requestAni.startRequestAni();
				}*/
				
			} else if(-1 == retCode) {
				String content = data.getString("content");
				
				LinearLayout speechTip = (LinearLayout)findViewById(R.id.speech_tip);
				speechTip.setVisibility(View.VISIBLE);
				
				TextView speechTipText = (TextView)findViewById(R.id.speech_tip_text);
				speechTipText.setText(content);
				
				new Timer().schedule(new TimerTask() {
					@Override      
				    public void run() {  
				        Message message = Message.obtain();
				        message.what = MSG_TYPE_SPEECH;
				        handler.sendMessage(message); 
				    }
				},1000);
			}
			
		} else if(100 == requestCode) {
			if (intent != null && intent.getExtras() != null ) {
				//boolean bval = intent.getExtras().getBoolean("vswitch");
				//writeVSwitch(bval);
			}
		} else if (BARCODE == requestCode) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = intent.getExtras();
				startRequestByContent(bundle.getString("result"));
			}
			
		} else {
			normal_state.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("DEBUG", "MainActivity -----------onStart!!!!!!!!");
		/*setPause(true);
		if(isSetting()){
			readFile("/data/data/"+this.getPackageName()+"/switch.txt");
			setSetting(false);
		}*/
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("DEBUG", "MainActivity -----------onResume!!!!!!!!");
		setPause(true);
		if(isSetting()){
			readFile("/data/data/"+this.getPackageName()+"/switch.txt");
			setSetting(false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("ZXingDemo", "MainActivity -----------onPause!!!!!!!!");
		Log.d("DEBUG", "MainActivity -----------onPause!!!!!!!!");
		
		//Offline start
		dismissCallingView();
		//Offline end
		
		/*
		if(dialog != null){
			if(dialog.isShowing()){
				dialog.cancel();
				if(va != null){
					va.cancelRequestText(true); //同时取消正在执行的请求...
				}
			}
		}
		*/
		
		/*if(requestAni != null){
			if(requestAni.isRuning()){
				if(va != null){
					Log.d("DEBUG", "MainActivity-----------onPause()-------------va.cancelRequestText(true)");
					va.cancelRequestText(true); //同时取消请求
				}
				requestAni.endRequestAni();
				requestAni.setVisibility(View.GONE);
			}
		}*/
		
		//取消相关dialog
		/*mLayoutManager.dismissClearScreenDialog();
		mLayoutManager.dismissDeleteDialog();
		mLayoutManager.dismissPopupWindowMenu();

		if (null != tts) {
			tts.stop();
		}*/

		if (atorSet != null && atorSet.isRunning()) {
			setPause(false);
		}
	}

	@Override
	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager) getApplicationContext() .getSystemService(Context.INPUT_METHOD_SERVICE); 
		//RelativeLayout layoutInput;
		setPause(true);
		switch(v.getId()) {
			case R.id.btn_to_mic:
				mExtState = false;
				inputSwitchAnimation(false, 500);
				imm.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
				input_state = (RelativeLayout)findViewById(R.id.input_state);
				if(View.INVISIBLE != input_state.getVisibility()) {
					sendBroadCastToMaster(VoiceInputChanged_Send, "state", mExtState);
				}
				break;
				
			case R.id.btn_voice:
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_VOICE_INPUT, 1);
				//account end
				
				startCollectVoiceInput();
				//Offline start
				isVoiceInput = true;
				//Offline end
				break;
				
			case R.id.btn_send:
				/*if (inputEdit.getText().length() == 0) {
					break;
				}
				imm.hideSoftInputFromWindow(sendBtn.getWindowToken(), 0);
				String content = inputEdit.getText().toString();*/
				
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_SEND, 1);
				//account end
				
				imm.hideSoftInputFromWindow(sendBtn.getWindowToken(), 0);
				
				String content = inputEdit.getText().toString().trim();
				if (content.isEmpty()) break;
				
				startRequestByContent(content);
				
				inputEdit.setText("");
				preventTouch = true;
				break;
				
			case R.id.to_input_btn:
				mExtState = true;
				setInput_line_ishow(false);
				inputSwitchAnimation(true, 500);
				sendBroadCastToMaster(VoiceInputChanged_Send, "state", mExtState);
				inputEdit.requestFocus();
				imm.showSoftInput(inputEdit, 0);
				
				//Offline start
				isVoiceInput = false;
				//Offline end
				break;

			case R.id.barcode:
			case R.id.barcode_small:
				Log.d("DEBUG", "the touch on barcode");
				try {
					/*Intent intent = new Intent();
					intent.setClassName(PACKAGENAME, CLASSNAME);
//					startActivity(intent);*/
					
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, BarCodeMainActivity.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivityForResult(intent, BARCODE);
					
				} catch (Exception e) {  
					Toast.makeText(getApplicationContext(), "没有找到相关的应用程序！", 0).show();
				}
				
				break;
		}
		
	}
	
	public void setSearchContent(String str) {
		searchContent = str;
	}
	
	public String getSearchContent() {
		return searchContent;
	}
	
	private AnimationDrawable getVoiceButtonEnterAnimation() {
		micBtn.setBackgroundResource(R.anim.vs_voice_input_enter_scale_anim);
		mVoiceInputEnter = (AnimationDrawable) micBtn.getBackground();
		return mVoiceInputEnter;
	}
	
	private AnimationDrawable getVoiceButtonExitAnimation() {
		micBtn.setBackgroundResource(R.anim.vs_voice_input_exit_scale_anim);
		mVoiceInputExit = (AnimationDrawable) micBtn.getBackground();
		return mVoiceInputExit;
	}
	
	public void inputSwitchAnimation(final boolean flag, int duration) {
		float transX = getResources().getDimension(R.dimen.vs_input_switch_translation_X);
		float editWidth = getResources().getDimension(R.dimen.vs_input_edittext_width);
		final float editWidthExit = getResources().getDimension(R.dimen.vs_input_edittext_width_pre);
		
		editWidth = flag? editWidth:editWidthExit;
		
		float startWith = flag? 0f:editWidth;
		float endWith = flag? editWidth:0f;
		
		float startScale = flag? 1f:0.6f;
		float endScale = flag? 0.6f:1f;
		
		float startTransX = flag? 0f:(-1)*transX;
		float endTransX = flag? (-1)*transX:0f;
		
		float micStartAlpha = flag? 1f:0.3f;
		float micEndAlpha = flag? 0.3f:1f;
		
		float startAlpha = flag? 1f:0f;
		float endAlpha = flag? 0f:1f;
		
//		int duration = 500;
		
		//final RelativeLayout layoutInput = (RelativeLayout)findViewById(R.id.input_state);
		//final RelativeLayout layoutNormal = (RelativeLayout)findViewById(R.id.normal_state);
		
		//The animator for the width of inputEdit
		ValueAnimator inputEditAnimator = ValueAnimator.ofFloat(startWith, endWith);
		inputEditAnimator.setDuration(duration);
		inputEditAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				inputEdit.setWidth((int)value);
			}
		});
		
		//The animator for the alpha of toIputBtn
		ValueAnimator toInputButtonAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
		toInputButtonAnimator.setDuration(duration);
		toInputButtonAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				toInputBtn.setAlpha(value);
				sendBtn.setAlpha(1 - value);
			}
		});
		
		//The animator for the translationX of mic button
		ValueAnimator micButtonTransAnimator = ValueAnimator.ofFloat(startTransX, endTransX);
		if (flag) {
			micButtonTransAnimator.setDuration(duration+20);//*4/5);
		} else {
			micButtonTransAnimator.setDuration(duration*4/5);
		}
		micButtonTransAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				micBtn.setTranslationX(value);
			}
		});
		micButtonTransAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
		
		//The animator for the alpha of barcode button
		ValueAnimator barCodeAlphaAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
		barCodeAlphaAnimator.setDuration(duration);
		barCodeAlphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float value = (Float)arg0.getAnimatedValue();
				barCodeButton.setAlpha(value);
			}
		});
		
		AnimatorSet mAnimatorSet = new AnimatorSet();
		if (flag) {
			mAnimatorSet.playTogether(inputEditAnimator, toInputButtonAnimator, micButtonTransAnimator);
		} else {
			mAnimatorSet.playTogether(inputEditAnimator, toInputButtonAnimator, micButtonTransAnimator, barCodeAlphaAnimator);
		}
//		mAnimatorSet.playTogether(inputEditAnimator, toInputButtonAnimator, micButtonTransAnimator );
		mAnimatorSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				inputSwitchAnimFlag = true;
				if (flag) {
					toInputBtn.setBackgroundResource(R.drawable.vs_input_button);
					toInputBtn.setVisibility(View.VISIBLE);
					
					sendBtn.setVisibility(View.VISIBLE);
					toMicBtn.setVisibility(View.INVISIBLE);
					
					input_state.setVisibility(View.VISIBLE);
					
					mVoiceInputEnter = getVoiceButtonEnterAnimation();
					if (mVoiceInputEnter != null) {
						mVoiceInputEnter.start();
					}
					
				} else {
					toMicBtn.setVisibility(View.INVISIBLE);
					micBtn.setVisibility(View.VISIBLE);
					toInputBtn.setBackgroundResource(R.drawable.vs_input_button);
					sendBtn.setVisibility(View.VISIBLE);
					
					normal_state.setVisibility(View.VISIBLE);
					
					mVoiceInputExit = getVoiceButtonExitAnimation();
					if (mVoiceInputExit != null) {
						mVoiceInputExit.start();
					}
					
					//to prevent the jumping when the barcode icon disapeared
					inputEdit.setWidth((int)editWidthExit);
				}
				
				barCodeIconAnimation(flag);
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				inputSwitchAnimFlag = false;
				if (flag) {
					micBtn.setVisibility(View.INVISIBLE);
					
					toMicBtn.setVisibility(View.VISIBLE);
					
					normal_state.setVisibility(View.INVISIBLE);
				} else {
					toInputBtn.setVisibility(View.VISIBLE);
					toInputBtn.setBackgroundResource(R.drawable.vs_input_unpressed);

					input_state.setVisibility(View.INVISIBLE);
					
					showBackGroundForInputArea();
				}
				
				if (mVoiceInputEnter != null) {
					mVoiceInputEnter = null;
				}
				if (mVoiceInputExit != null) {
					mVoiceInputExit = null;
				}
				
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mAnimatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
		
		if (flag) {
			mAnimatorSet.setStartDelay(80);
		} else {
			if (inputState) {
				mAnimatorSet.setStartDelay(150);
			}
		}
		mAnimatorSet.start();
	}
	
	private void inputSwitchAnim(boolean micMode) {
		if(micMode) {
			mExtState = true;
			//RelativeLayout layoutInput = (RelativeLayout)findViewById(R.id.input_state);
			input_state.setVisibility(View.VISIBLE);
			//RelativeLayout layoutNormal = (RelativeLayout)findViewById(R.id.normal_state);
			normal_state.setVisibility(View.INVISIBLE);
		} else {
			mExtState = false;
			//RelativeLayout layoutInput = (RelativeLayout)findViewById(R.id.input_state);
			input_state.setVisibility(View.INVISIBLE);
			//RelativeLayout layoutNormal = (RelativeLayout)findViewById(R.id.normal_state);
			normal_state.setVisibility(View.VISIBLE);
		}
	}

	public void dismissCustomMenu() {
		mCustomMenuProxy.dismissCustomMenu();
	}
	
	public void mCustomMenuProxyCallBack() {
		mCustomMenuProxy.onCallBack();
	}
	
	OnItemClickListener mCustomMenuItemClickListener=new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			case CLEAR_SCREEN:
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_CLEAR_SCREEN, 1);
				//account end
				
				clearScreen = true;
				mLayoutManager.clearScreen();
				sendBroadcast(new Intent(ACTION_VOICE_CLEAR_SCREEN));
				//此时停止播报
				if(tts != null){
					tts.stop();
				}
				break;
			case MENU_SETTINGS:
				//account start
				MainActivity.startToCount(context, CFG.ACCOUNT_ACTION_SETTING, 1);
				//account end
				
				Intent intent  = new Intent(MainActivity.this,SettingActivity.class);
				startActivity(intent);
				setSetting(true);
				break;
			
			default:
				break;
			}
			dismissCustomMenu();
		}
	};
	
	public void setClearScreenFlag(boolean b) {
		clearScreen = b;
	}
	
	public boolean getClearScreenFlag() {
		return clearScreen;
	}
	
	public void setEnterKeyAsSend() {
		InputMethodManager imm = (InputMethodManager) getApplicationContext() .getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(sendBtn.getWindowToken(), 0);

//		String content = inputEdit.getText().toString();
		
		String content = inputEdit.getText().toString().trim();
		if (content.isEmpty()) return;
		
		startRequestByContent(content);
		
		inputEdit.setText("");
		preventTouch = true;
	}

	public void setStatusBarBG(boolean isTransparent) {
		if (Build.VERSION.SDK_INT <= 18) {
			Intent StatusBarBGIntent = new Intent();
			StatusBarBGIntent.setAction("aurora.action.SET_STATUSBAR_TRANSPARENT");
			StatusBarBGIntent.putExtra("transparent", isTransparent);
			sendBroadcast(StatusBarBGIntent);
			Log.e("linp", "------------------setStatusBarBG----------------1!");
		}else{
			Log.e("linp", "------------------setStatusBarBG----------------2!");
		}
	}

	public Handler getHandler() {
		return handler;
	}

	/*
	public ProgressDialog getDialogToShow(Context context) {
		dialog = new ProgressDialog(context);
		dialog.setTitle(R.string.vs_pocessing);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (va != null) {
					va.cancelRequestText();
				}
			}
		});
		return dialog;
	}
	*/

	/** start to write value to share perference */
	public void writeVSwitch(boolean bval) {
		if (mVoiceSettingSharePreference == null) {
			mVoiceSettingSharePreference = getApplicationContext().getSharedPreferences(vswitch, Context.MODE_PRIVATE);
		}
		mVoiceSettingSharePreference.edit().putBoolean(vp_key, bval).commit();
	}
	
	/**start to read value from share perference*/
	private boolean readVSwitch(){
		if(mVoiceSettingSharePreference == null){
			mVoiceSettingSharePreference = getSharedPreferences(vswitch, Context.MODE_PRIVATE);
		}
		
		/* Fix bug #10096
		 * 
		 * if update the ROM with cache cleared, the file of "vswitch" will be deleted, so the value of mVoiceSettingSharePreference can not be found,
		 * and then return false, and the isVp = false, there will be no voice.
		 * 
		 * return mVoiceSettingSharePreference.getBoolean(vp_key, false);// default to false
		 */		
		return mVoiceSettingSharePreference.getBoolean(vp_key, true);// default to false
	}

	private void setInitBool() {
		if (mVoiceSettingSharePreference == null) {
			mVoiceSettingSharePreference = getSharedPreferences(vswitch,Context.MODE_PRIVATE);
		}
		mVoiceSettingSharePreference.edit().putBoolean("initvoice", isAudioInit()).commit();
	}
	
	//For scrollView
	@Override
	public void onSizeChanged(CustomerScrollView v, int w, int h, int oldw, int oldh) {}


	private boolean nomal_state_hasbg;
	private boolean input_state_hasbg;
	@Override
	public void onScrollChanged(CustomerScrollView v, int l, int t, int oldl, int oldt) {
		
		offset = mVoiceOutLine.getMeasuredHeight() - mScrollView.getHeight(); //定值		
		fillviewheight = mView.getMeasuredHeight();
//		check = offset - fillviewheight;// + 60;
		
		if (mScrollView.getScrollY() < offset - fillviewheight) {
			//凸显
//			Log.d("DEBUG", "onScrollChanged----------show the background");
			if(input_state.getVisibility() == View.VISIBLE){
				//input_state
				if (!input_state_hasbg) {
					input_state.setBackgroundResource(R.drawable.vs_bottom_interlayer);
				}
				input_state_hasbg = true;
			}
			
			if(normal_state.getVisibility() == View.VISIBLE){
				//nomal_state
				if (!nomal_state_hasbg) {
					normal_state.setBackgroundResource(R.drawable.vs_bottom_interlayer);
				}
				nomal_state_hasbg = true;
			}
			
		} else {
			//隐藏
//			Log.d("DEBUG", "onScrollChanged----------hide the background");
			input_state.setBackground(null);
			input_state_hasbg = false;
			
			normal_state.setBackground(null);
			nomal_state_hasbg = false;
		}
	}

	public LinearLayout getLinearLayout() {
		return mVoiceContentLayout;
	}

	public void setFillViewHeight(int height) {
		if (mView != null) {
			LayoutParams params = mView.getLayoutParams();
			params.height = height;
			mView.setLayoutParams(params);
			mView.requestLayout();
		}
	}
	
	public void resetFillViewHeight() {
		int height = mScrollView.getMeasuredHeight();
		setFillViewHeight(height);
	}

	public void calculateFillViewHeight() {
		int totalHeight = mScrollView.getMeasuredHeight();
		isNeedScroll = false;
		int fillViewHeight = totalHeight;
		int lastItemHeight = mLayoutManager.getLastItemHeight() < 0? 0:mLayoutManager.getLastItemHeight();
		fillViewHeight = totalHeight - lastItemHeight;
		if (fillViewHeight < 0) {
			fillViewHeight = 0;
			isNeedScroll = true;
		}
		setFillViewHeight(fillViewHeight);
	}

	public void mScrollViewStartToScroll() {
		if (mScrollView != null) {
			mScrollView.post(new Runnable() {
				@Override
				public void run() {
					calculateFillViewHeight();
					if (isNeedScroll) {
						int offsetY = mLayoutManager.getOffsetToScroll();
						mScrollView.scrollTo(0, offsetY);
					} else {
						mScrollView.fullScroll(View.FOCUS_DOWN);
					}
				}
			});
		}
	}

	public void updateFillViewHeight() {
		int scrollViewHeight = mScrollView.getMeasuredHeight();
		int lastItemHeight = mLayoutManager.getLastInnerItemHeight();
		int fillViewHeight = 0;
		if (lastItemHeight < scrollViewHeight) {
			fillViewHeight = scrollViewHeight -lastItemHeight;
		}
		setFillViewHeight(fillViewHeight);
	}
	
	public void updateFillViewHeight(int height) {
		int scrollViewHeight = mScrollView.getMeasuredHeight();
//		int lastItemHeight = mLayoutManager.getLastInnerItemHeight();
		int fillViewHeight = 0;
		if (height < scrollViewHeight) {
			fillViewHeight = scrollViewHeight -height;
		}
		setFillViewHeight(fillViewHeight);
	}
	
	private void readFile(String path){
		File file = new File(path);
		if(file.exists()){
			String bool = "";
			String res ="";
			BufferedReader bread = null;
			try {
				bread = new BufferedReader(new FileReader(file));
				while( (res = bread.readLine()) != null){
					if(res.contains(vp_key)){
						bool = res.split(":")[1].toString();
						break;
					}
				}
				bread.close(); 
				
				//写入配置信息
				writeVSwitch(Boolean.valueOf(bool));
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally{
				try {
					if(bread != null){
						bread.close();
					}
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			Log.e("iuni-ht", "MM:"+bool);
		}else{
			Log.e("iuni-ht", "文件不存在...");
		}
	}

	public boolean isSetting() {
		return isSetting;
	}

	public void setSetting(boolean isSetting) {
		this.isSetting = isSetting;
	}
	
	public void startRequestByContent(String str) {
		mLayoutManager.hidePreviousItems(HideState.NewResult);
		if (null != str && str.length() > 0) {
			
			setSearchContent(str);
			
			//刷新页面
			Message msg = new Message();
			msg.what = MSG_TYPE_SHOW_REQUEST_TEXT;
			msg.obj = str;
			//handler.sendMessageDelayed(msg, 90);
			handler.sendMessage(msg);
			
			//Offline start
			//请求网络
//			va.requestText(str, null);
			
			startKeyWordsRecognizing(str);
			//Offline end
			
			//启动动画
			if(requestAni != null){
				if(requestAni.getVisibility() != View.VISIBLE){
					requestAni.setVisibility(View.VISIBLE);
				}
				requestAni.startRequestAni();
			}
		}
	}

	private void noVoiceGreeting() {
		if (va != null && mLayoutManager != null) {
			resetFillViewHeight();
			mLayoutManager.updateLayoutManager(va.getLoadData().getResponse(), mViewConstructor);
			mScrollViewStartToScroll();
		}
	}

	private void checkGreetingRecord(int key, boolean bool) {
		switch (key) {
		case 1:
			if (!greetingRecord.isBeforeDawn) {
				if (bool) {
					pointTimeGreeting();
				}
				greetingRecord.setBeforeDawn(true);
			}
			greetingRecord.setMorning(false);
			greetingRecord.setMiddle(false);
			greetingRecord.setAfternoon(false);
			greetingRecord.setNight(false);
			break;
		case 2:
			if (!greetingRecord.isMorning) {
				if (bool) {
					pointTimeGreeting();
				}
				greetingRecord.setMorning(true);
			}
			greetingRecord.setBeforeDawn(false);
			greetingRecord.setMiddle(false);
			greetingRecord.setAfternoon(false);
			greetingRecord.setNight(false);
			break;
		case 3:
			if (!greetingRecord.isMiddle) {
				if (bool) {
					pointTimeGreeting();
				}
				greetingRecord.setMiddle(true);
			}
			greetingRecord.setBeforeDawn(false);
			greetingRecord.setMorning(false);
			greetingRecord.setAfternoon(false);
			greetingRecord.setNight(false);
			break;
		case 4:
			if (!greetingRecord.isAfternoon) {
				if (bool) {
					pointTimeGreeting();
				}
				greetingRecord.setAfternoon(true);
			}
			greetingRecord.setBeforeDawn(false);
			greetingRecord.setMorning(false);
			greetingRecord.setMiddle(false);
			greetingRecord.setNight(false);
			break;
		case 5:
			if (!greetingRecord.isNight) {
				if (bool) {
					pointTimeGreeting();
				}
				greetingRecord.setNight(true);
			}
			greetingRecord.setBeforeDawn(false);
			greetingRecord.setMorning(false);
			greetingRecord.setMiddle(false);
			greetingRecord.setAfternoon(false);
			break;
		}
	}

	private String getGreetingWord() {
		Date curDate = new Date(System.currentTimeMillis());
		int hour = curDate.getHours();
		if (hour < 6) {
			checkGreetingRecord(1, false);
			return getResources().getString(R.string.vs_greeting_early);
		}

		else if (hour >= 6 && hour < 11) {
			checkGreetingRecord(2, false);
			return getResources().getString(R.string.vs_greeting_morning);
		}

		else if (hour >= 11 && hour < 13) {
			checkGreetingRecord(3, false);
			return getResources().getString(R.string.vs_greeting_noon);
		}

		else if (hour >= 13 && hour < 18) {
			checkGreetingRecord(4, false);
			return getResources().getString(R.string.vs_greeting_aoon);
		}

		else if (hour >= 18) {
			checkGreetingRecord(5, false);
			return getResources().getString(R.string.vs_greeting_night);
		}

		else {
			checkGreetingRecord(1, false);
			return getResources().getString(R.string.vs_greeting_early);
		}
	}

	class GreetingTimeRecord {
		private boolean isBeforeDawn;
		private boolean isMorning;
		private boolean isMiddle;
		private boolean isAfternoon;
		private boolean isNight;

		public boolean isBeforeDawn() {
			return isBeforeDawn;
		}

		public void setBeforeDawn(boolean isBeforeDawn) {
			this.isBeforeDawn = isBeforeDawn;
		}

		public boolean isMorning() {
			return isMorning;
		}

		public void setMorning(boolean isMorning) {
			this.isMorning = isMorning;
		}

		public boolean isMiddle() {
			return isMiddle;
		}

		public void setMiddle(boolean isMiddle) {
			this.isMiddle = isMiddle;
		}

		public boolean isAfternoon() {
			return isAfternoon;
		}

		public void setAfternoon(boolean isAfternoon) {
			this.isAfternoon = isAfternoon;
		}

		public boolean isNight() {
			return isNight;
		}

		public void setNight(boolean isNight) {
			this.isNight = isNight;
		}
		
		public void setAllTimeState(boolean b) {
			this.isBeforeDawn = b;
			this.isMorning = b;
			this.isMiddle = b;
			this.isAfternoon = b;
			this.isNight = b;
		}

	}

	public boolean isPause() {
		return isPause;
	}

	public void setPause(boolean isPause) {
		this.isPause = isPause;
	}

	public boolean isAudioInit() {
		return isAudioInit;
	}

	public void setAudioInit(boolean isAudioInit) {
		this.isAudioInit = isAudioInit;
	}
	
	public void showBackGroundForInputArea() {
		if (input_state.getVisibility() == View.VISIBLE) {
			if(nomal_state_hasbg || input_state_hasbg){
				input_state.setBackgroundResource(R.drawable.vs_bottom_interlayer);
			} else {
				input_state.setBackground(null);
			}
			normal_state.setBackground(null);
		} else {
			if(nomal_state_hasbg || input_state_hasbg){
				normal_state.setBackgroundResource(R.drawable.vs_bottom_interlayer);
			} else {
				normal_state.setBackground(null);
			}
			input_state.setBackground(null);
		}
	}

	public boolean isInput_line_ishow() {
		return input_line_ishow;
	}

	public void setInput_line_ishow(boolean input_line_ishow) {
		this.input_line_ishow = input_line_ishow;
	}

	/**assemble voice input  code */
	public void startCollectVoiceInput(){
		Log.e("linp", "###########startCollectVoiceInput");
		if (inputSwitchAnimFlag) {
			return;
		}
		setStatusBarBG(true);
		if(null != tts) {
			tts.stop();
		}
		
		if (mExtState = true) {
			mExtState = false;
			inputSwitchAnimation(false, 0);
			InputMethodManager imm = (InputMethodManager) getApplicationContext() .getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
			sendBroadCastToMaster(VoiceInputChanged_Send, "state", mExtState);
		}
		
		normal_state = (RelativeLayout)findViewById(R.id.normal_state);
		normal_state.setVisibility(View.GONE);
		
		Intent intent = new Intent(this,SpeechActivity.class);
		startActivityForResult(intent, 0);
		overridePendingTransition(R.anim.vs_specch_acti_in, R.anim.vs_speech_acti_exit);
	}
	
	/**new BroadCast Receiver for voice print*/
	private BroadcastReceiver mVoicePrintReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String act = arg1.getAction();
			if(act.equals(VOICE_COLLECT_ACTION)){
				startCollectVoiceInput();
				Log.e("linp", "-----------------------------------------------VOICE_COLLECT_ACTION");
			}
		}
	};

	private void registerVoiceCollectBroadCastReceivcer(){
	   	IntentFilter mVoiceCollectFilter = new IntentFilter();
	   	mVoiceCollectFilter.addAction(VOICE_COLLECT_ACTION);
		registerReceiver(mVoicePrintReceiver, mVoiceCollectFilter);
	}
	
	public void startScreenClearTimer() {
		cancelScreenClearTimer();
		
		mTimer = new Timer();
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				Message message = Message.obtain();
				message.what = MSG_TYPE_TIMER;
				handler.sendMessage(message);
				Log.d("DEBUG", "the timer is time out+++++++++++++++++++++++++++==");
			}
		};
		Long time = (long) 1000*60*60;		//1 hour
//		Long time = (long) 1000*10;
		Log.d("DEBUG", "start timer and the time +++++++++++++++++++++++++++== "+time);
		mTimer.schedule(mTimerTask, time);
	}
	
	public void cancelScreenClearTimer() {
		if (mTimer != null && mTimerTask != null) {
			Log.d("DEBUG", "enter voice mode and the timer will be canceled+++++++++++++++++++++");
			mTimer.cancel();
			mTimerTask.cancel();
			mTimer = null;
			mTimerTask = null;
		}
	}
	
	public ContentViewConstructor getContentViewConstructor() {
		return mViewConstructor;
	}
	
	public void setScrollViewYOffset () {
		if (mScrollView != null) {
			SCROLL_Y = mScrollView.getScrollY();
			Log.d("DEBUG", "setScrollY === "+SCROLL_Y);
		}
	}
	
	public void scrollToPreviousPosition () {
		Log.d("DEBUG", "getScrollY === "+SCROLL_Y);
		handler.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(0, SCROLL_Y);
			}
		});
	}
	
	public CustomMenuProxy getCustonMenu(){
		return mCustomMenuProxy;
	}
	
	public void setMainLayoutParams() {
		lParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
		mVoiceMainLayout.setLayoutParams(lParams);
	}
	
	public void clearMainLayoutParams() {
		lParams.height = (int)mainLayoutHeight;
		mVoiceMainLayout.setLayoutParams(lParams);
	}

	
	public boolean getPlatformTypeIsU2() {
		return platform_U2;
	}

	
	public void showBarCodeIcon() {
		barCodeButton = (Button)findViewById(R.id.barcode);
		barCodeButton.setOnClickListener(this);
		barCodeButton.setVisibility(View.VISIBLE);
		
		barCodeButtonSmall = (Button)findViewById(R.id.barcode_small);
		barCodeButtonSmall.setOnClickListener(this);
		barCodeButtonSmall.setVisibility(View.VISIBLE);
		
		barCodeDivideLine = (ImageView)findViewById(R.id.barcode_divide_line);
		barCodeDivideLine.setVisibility(View.VISIBLE);
		
	}
	
	public void barCodeIconAnimation(boolean flag) {
		if (flag) {
			barCodeButton.setVisibility(View.GONE);
			barCodeButtonSmall.setVisibility(View.VISIBLE);
			barCodeDivideLine.setVisibility(View.VISIBLE);
		} else {
			barCodeButtonSmall.setVisibility(View.GONE);
			barCodeDivideLine.setVisibility(View.GONE);
			
			barCodeButton.setVisibility(View.VISIBLE);
			barCodeButton.setAlpha(0);
		}
		
	}
	
	//Offline start
	public ContentResolver getContentResolverForContact() {
		return mContentResolver;
	}
	
	public boolean isAirPlaneModeOn(Context context) {
		boolean state = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1 ? true : false;
		Log.d("DEBUG","MainActivity--------------airplane mode = "+state);
		return state;
	}
	
	@SuppressLint("NewApi")
	public void setAirplaneMode(Context context, boolean enabling) {
		Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, enabling ? 1 : 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
		intent.putExtra("state", enabling);
		context.sendBroadcast(intent);
	}
	
	public int getSIMCardState() {
        int simState = mTelephonyManager.getSimState();
//        simState = 1;	//without sim card or sim card is disabled
//        simState = 5;	//sim is OK
        
        return simState;
	}
	
	public boolean isNetworkConnected(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        Log.d("DEBUG", "wifi.isAvailable() = "+wifi.isAvailable()+" mobile.getState() = "+mobile.getState());
        if (wifi.isAvailable() || mobile.getState() == NetworkInfo.State.CONNECTED) {
			Log.d("DEBUG", "the network is connected!!!!!!!!");
			return true;
		} else {
			return false;
		}

    }
	
	private void updateWifiState(Intent intent) {
	    final String action = intent.getAction();
	    boolean mWifiEnabled;
	    boolean mWifiConnected = false;
	    
	    if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
	        mWifiEnabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;

	    } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
	        final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	        boolean wasConnected = mWifiConnected;
	        mWifiConnected = networkInfo != null && networkInfo.isConnected();
	        
//	        Log.d("DEBUG", "WifiManager.NETWORK_STATE_CHANGED_ACTION===========mWifiConnected = "+mWifiConnected);
	        final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        final android.net.NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        boolean mobileState =  mobile.getState() == NetworkInfo.State.CONNECTED;
	        
	        mViewConstructor.updateVoiceButtonAlpha(mWifiConnected || mobileState);	        

	    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
//	    	Log.d("DEBUG", "WifiManager.RSSI_CHANGED_ACTION==========mWifiConnected = "+mWifiConnected);
	    	//do nothing
	    }
	}
	
	PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//        	Log.d("DEBUG", "onSignalStrengthsChanged signalStrength=" + signalStrength);
//        	Log.d("DEBUG", "onSignalStrengthsChanged signalStrength.getGsmSignalStrength()=" +signalStrength.getGsmSignalStrength());
            //do nothing
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
//            Log.d("DEBUG", "onServiceStateChanged state=" + state.getState());
            //do nothing
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //do nothing
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
//            Log.d("DEBUG", "onDataConnectionStateChanged: state=" + state + " type=" + networkType);

            final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            
            mViewConstructor.updateVoiceButtonAlpha(wifi.isAvailable() || state == 2);
        }

        @Override
        public void onDataActivity(int direction) {
//        	Log.d("DEBUG", "onDataActivity: direction=" + direction);
            //do nothing
        }
    };
	
	public static final String getCurrentCountryIso(Context context) {
        CountryDetector detector = (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
        return detector.detectCountry().getCountryIso();
    }
	
	public String getFormattedNumber(String number) {
		return PhoneNumberUtils.formatNumber(number, number, getCurrentCountryIso(context));
	}
	
	public boolean isDualSimCard() {
		Log.d("DEBUG", "dual sim = "+GnTelephonyManager.isMultiSimEnabled());
		return GnTelephonyManager.isMultiSimEnabled();
	}
	
	public int getInsertSimCount() {
		Log.d("DEBUG", "the sim card count = "+gionee.provider.GnTelephony.SIMInfo.getInsertedSIMCount(this));
		return gionee.provider.GnTelephony.SIMInfo.getInsertedSIMCount(this);
	}
	
	public int getDefaultSim() {
		int defaultSim = 0;
		//for android5.0, the MULTI_SIM_PRIORITY_SUBSCRIPTION can't be found
		/*try {
			defaultSim = Settings.Global.getInt(mContentResolver, Settings.Global.MULTI_SIM_PRIORITY_SUBSCRIPTION);
			Log.d("DEBUG", "default simNumber ======================================= "+defaultSim);
		} catch (Exception e) {
			// TODO: handle exception
			defaultSim = 0;
		}*/
		return defaultSim;
	}
	
	public static int getSimIcon(Context mContext, int simId) {  // simId : 0 ,1
    	int result = -1;
    	int slot = 0;
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(mContext, simId);
        if (simInfo != null) {          
//        	result = color2resId(simInfo.mColor);
        	slot = simInfo.mSlot;
        }
       	if(result == -1) {    
        	if(slot == 1) {
        		result = R.drawable.vs_offline_simcard_icon_sim2;
	    	} else {
	    		result = R.drawable.vs_offline_simcard_icon_sim1;
	    	}	
    	}
        
    	return result;
    }
	
	public int getSimIdBySlot(Context mContext, int simId) {
		SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(mContext, simId);
        if (simInfo != null) {
        	return simId;
        } else {
        	if (simId == 0) {
				return 1;
			}
        }
        return 0;
	}

	public static int color2resId(int color) {
    	int result = -1 ;
    	switch (color) {
            case 0: {
                result = R.drawable.vs_offline_simcard_icon_sim1;
                break;
            }
            case 1: {
                result = R.drawable.vs_offline_simcard_icon_sim2;
                break;
            }         
            case 2: {
                result = R.drawable.vs_offline_simcard_icon_net;
                break;
            }
            case 3: {
                result = R.drawable.vs_offline_simcard_icon_home;
                break;
            }
            case 4: {
                result = R.drawable.vs_offline_simcard_icon_office;
                break;
            }         
            case 5: {
                result = R.drawable.vs_offline_simcard_icon_phone;
                break;
            }
        }
    	
    	return result;
    }
	
	public int setSendIconImage(int iconId) {
		int sendIcon = -1;
		switch (iconId) {
	        case R.drawable.vs_offline_simcard_icon_sim1:
	            sendIcon = R.drawable.vs_offline_message_sendby_sim1;
	            break;

	        case R.drawable.vs_offline_simcard_icon_sim2:
	        	sendIcon = R.drawable.vs_offline_message_sendby_sim2;
	            break;

	        case R.drawable.vs_offline_simcard_icon_net:
	        	sendIcon = R.drawable.vs_offline_message_sendby_net;
	            break;
	            
	        case R.drawable.vs_offline_simcard_icon_home:
	            sendIcon = R.drawable.vs_offline_message_sendby_home;
	            break;
	            
	        case R.drawable.vs_offline_simcard_icon_office:
	            sendIcon = R.drawable.vs_offline_message_sendby_office;
	            break;
	            
	        case R.drawable.vs_offline_simcard_icon_phone:
	            sendIcon = R.drawable.vs_offline_message_sendby_phone;
	            break;
		}
		if(sendIcon == -1) {    
        	if(iconId == 1) {
        		sendIcon = R.drawable.vs_offline_message_sendby_sim2;
	    	} else {
	    		sendIcon = R.drawable.vs_offline_message_sendby_sim1;
	    	}	
    	}
		return sendIcon;
	}
		
	public void setTimerForCalling(final LinearLayout callingView, final int length, final String number) {		
    	final int time = 10;		//10ms
    	mCallProgressBar = (ProgressBar) callingView.findViewById(R.id.vs_offline_contact_calling_progressbar);
    	
    	mCallTimer = new Timer();
		mCallTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (mCallProgressBar == null) return;
				
				if (mCallProgressBar.getProgress() != length) {
					mCallProgressBar.incrementProgressBy(time);
										
				} else {
					if (mVAState == VAState.OUT) {
						Log.d("DEBUG", "mVAState == VAState.OUT===========================dismissCallingView");
						dismissCallingView();
						return;
					} else {
						LinearLayout callingView = (LinearLayout) mCallProgressBar.getParent();
						LinearLayout callingLayout = (LinearLayout) callingView.getParent();
						if (callingLayout == null) return;
						
						if (callingLayout.getVisibility() == View.GONE) {
							Log.d("DEBUG", "mVAState == VAState.IN===========================callingLayout.getVisibility() == View.GONE");
							cancelCallingTimer();
							return;
						} else {
							dismissCallingView();
							callContactBySimCard(number, getLastCallSlotId(number));
						}
					}
				}
			}
		};
		mCallTimer.schedule(mCallTimerTask, time, time);
    }
	
	public void cancelCallingTimer() {
		Log.d("DEBUG", "calling timer will be canceled+++++++++++++++++++++");
		if (mCallTimer != null && mCallTimerTask != null) {
			mCallTimer.cancel();
			mCallTimerTask.cancel();
		}
		if (mCallProgressBar != null) {
			mCallProgressBar.setProgress(0);
		}
		mCallTimer = null;
		mCallTimerTask = null;
	}
	
	public void dismissCallingView() {
		cancelCallingTimer();
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mCallProgressBar != null) {
					mViewConstructor.hideCallingView((LinearLayout) mCallProgressBar.getParent());
				}
			}
		});
	}
	
	public void setVoiceContentForOfflineMessage(String content, AuroraEditText editText) {
		mVoiceInputForOfflineMessage = true;
		startCollectVoiceInput();
		targetEditText = editText;
	}
	
	public void sendBroadcastToShowVoiceInputTips() {
		final Intent intent = new Intent();
		intent.setAction(SHOW_VOICE_INPUT_TIPS);
		intent.putExtra("showTipsState", true);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sendBroadcast(intent);
				Log.d("DEBUG", "sendBroadcastToShowVoiceInputTips-----------Sent");
			}
		}, 300);	//make sure SpeechActivity receive broadcast, it will be too early if time is set as 200, so set the time = 300
		
	}
	
	public void callContactBySimCard(String numberString, int simNumber) {
		if (isAirPlaneModeOn(context) && getInsertSimCount() <= 0 && getSIMCardState() == 1) {
			return;
		}
		if (simNumber == -1) {
			simNumber = getDefaultSim();
		}
		
		Intent intent;
		if (numberString == null) {
			intent = new Intent(Intent.ACTION_DIAL);
		} else {
			intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + numberString));
		}
		intent.putExtra(SUBSCRIPTION_KEY, simNumber);
		startActivity(intent);
	}
	
	// get last call SlotId
    public int getLastCallSlotId(String number) {
    	int insertSimCount = getInsertSimCount();
    	if (insertSimCount < 2 || number == null) return getDefaultSim();

        int lastSimId = -1;
        int lastSlot = -1;
        String[] projection = { Calls.SIM_ID };
        Cursor cursor = mContentResolver.query(Calls.CONTENT_URI, projection, Calls.NUMBER + 
        									   " = '" + number + "'", null, "_id desc");
//        Log.d("DEBUG", "the cursor = "+cursor);
        if (cursor == null) return -1;

        if (cursor.moveToFirst()) {
            lastSimId = Integer.valueOf(cursor.getInt(0));
        }
        cursor.close();
        
        //lastSimId will be updated with the number of simcard has been insert ever
        SIMInfo simInfo = SIMInfo.getSIMInfoById(context, lastSimId);
        if (simInfo != null) {
			lastSlot = simInfo.mSlot;
		}
        Log.d("DEBUG", "getLastCallSlotId-----------the lastsimId = "+lastSimId+" and the lastSlot = "+lastSlot);
        
        if (lastSlot == 0) {
			return 0;
		} else if (lastSlot == 1) {
			return 1;
		} else {
			return getDefaultSim();
		}
    }
	
	public void sendMessageBySystem(String number, int simNumber, StringBuilder message) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
		intent.putExtra(SUBSCRIPTION_KEY, simNumber);
		if (message != null) {
			intent.putExtra("sms_body", message.toString());
		}
		startActivity(intent);
	}
	
	public void editContactsByContactId(String contactId) {
		Long id = Long.parseLong(contactId);
		Uri contactUri = null;
		Uri lookUpUri = null;
		
		try {
			contactUri=ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
	        lookUpUri=Contacts.getLookupUri(mContentResolver, contactUri);
	        Log.d("DEBUG", "the lookUpUri = "+lookUpUri);
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("DEBUG", "get the lookUpUri of "+id+ "= "+"is null");
		}
		
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setData(lookUpUri);
		startActivity(intent);
	}
	
	public void sendMessageByAurora(LinearLayout sendingMessageView, String number, String message, int simNumber, final ImageView simCardIcon){
		RelativeLayout sendResultLayout= (RelativeLayout) sendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result_layout);
		
		final ImageView resultIcon = (ImageView) sendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result_icon);
		final TextView resultText = (TextView) sendingMessageView.findViewById(R.id.vs_offline_sending_message_send_result);
		final ProgressBar sendProgressBar = (ProgressBar) sendingMessageView.findViewById(R.id.vs_offline_sending_message_progressbar);
		
		final String sending = getResources().getString(R.string.vs_offline_sending_message_sending);
		final String sendingFailed = getResources().getString(R.string.vs_offline_sending_message_failed);
		
		sendProgressBar.setAlpha(1f);
		resultText.setText(getResources().getString(R.string.vs_offline_sending_message_sending));
		
		simCardIcon.setBackgroundResource(getSimIcon(context, simNumber));
		if (isDualSimCard() && getInsertSimCount() == 2) {
			simCardIcon.setVisibility(View.VISIBLE);
		}
		
		mSendingMessageView = sendingMessageView;
        
        try {
        	ComponentName mAuroraComponent = new ComponentName("com.android.mms", "com.android.mms.ui.NoConfirmationSendServiceProxy");
    		final Uri uri = Uri.fromParts(Intent.ACTION_SENDTO, number, null);
            Intent intent = new Intent("android.intent.action.RESPOND_VIA_MESSAGE", uri);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.putExtra("simId",simNumber);
            String string = String.valueOf(sendingMessageView.getId());
            intent.putExtra("third_response",string);
            Log.d("DEBUG", "sendMessageByAurora-----------------sendingMessageView.getId() = "+string);
            intent.setComponent(mAuroraComponent);
            
            context.startService(intent);
            
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("DEBUG", "sendMessageByAurora Exception ================== "+e);
		}
	}

	public String getSystemTime(){
        //24小时制
//        SimpleDateFormat dateFormat24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormat24 = new SimpleDateFormat("MM-dd HH:mm");
        //12小时制  
//      SimpleDateFormat dateFormat12 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
        String timeString = dateFormat24.format(curDate);

        return timeString;  
    }
	
	public boolean isNumberOrCharContained(String str){
		if (str.isEmpty()) return false;
		
		Pattern pattern = Pattern.compile("[a-zA-Z0-9]");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return true;
		}
		return false;
	}
	
	public boolean isCallCommand(String contentString) {
		String tempString;
		
		if (contentString.contains(CFG.OFFLINE_KEYWORDS_PHONE) || 
			contentString.contains(CFG.OFFLINE_KEYWORDS_NUMBER) || 
			contentString.contains(CFG.OFFLINE_KEYWORDS_GEI)) {
			
			int index = contentString.indexOf(CFG.OFFLINE_KEYWORDS_PHONE);
			if (index == -1) {
				index = contentString.indexOf(CFG.OFFLINE_KEYWORDS_NUMBER);
			}
			if (index != -1) {
				tempString = contentString.substring(0, index);
				if (tempString.contains(CFG.OFFLINE_KEYWORDS_DA) || 
					tempString.contains(CFG.OFFLINE_KEYWORDS_BO) || 
					tempString.contains(CFG.OFFLINE_KEYWORDS_QU)) return true;
			}
			
			index = contentString.indexOf(CFG.OFFLINE_KEYWORDS_GEI);
			if (index != -1) {
				tempString = contentString.substring(0, index);
				if (tempString.contains(CFG.OFFLINE_KEYWORDS_DA) || 
					tempString.contains(CFG.OFFLINE_KEYWORDS_BO)) return true;
			}
		}
				
		return false;
	}
	
	public boolean isMessageCommand(String contentString) {		
		if (contentString.contains(CFG.OFFLINE_KEYWORDS_MESSAGE)) return true;
		
		return false;
	}
	
	public boolean isSearchContactsCommand(String contentString) {		
		if (contentString.contains(CFG.OFFLINE_KEYWORDS_PHONE_2) || 
			contentString.contains(CFG.OFFLINE_KEYWORDS_NUMBER_2)) return true;
		
		return false;
	}
	
	public boolean isApplicationCommand(String contentString) {
		if (contentString.contains(CFG.OFFLINE_KEYWORDS_DAKAI)) return true;
		
		return false;
	}
	
	public void setOffLineType(OFFLINE_TYPE t) {
		mOffLineType = t;
	}
	
	public OFFLINE_TYPE getOffLineType() {
		return mOffLineType;
	}
	
	public void startKeyWordsRecognizing(String string) {
		new KeyWordsRecognizingTask().execute(string);
	}
	
	private class KeyWordsRecognizingTask extends AsyncTask<Object, Object, OFFLINE_TYPE> {
		private String contentString;
		
		@Override
		protected OFFLINE_TYPE doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("DEBUG", "KeyWordsRecognizeTask---------------doInBackground");
			contentString = (String) params[0];
			
			if (!isNetworkConnected(context)) return OFFLINE_TYPE.NONE;
			
			keyWordsRecognization(contentString);
			
			return mOffLineType;
		}
		
		protected void onPostExecute(OFFLINE_TYPE result) {
     		Log.d("DEBUG", "KeyWordsRecognizeTask---------------onPostExecute--------result = "+result);
     		
     		if (result == OFFLINE_TYPE.NONE) {
				va.requestText(contentString, null);
				
			} else {
				TtsRes  ttsRes = new TtsRes();
     			ttsRes.flag = true;
     			
				Message message = Message.obtain();
				message.what = MSG_TYPE_TTS;
				message.obj = ttsRes;
				handler.sendMessage(message);
			}
     	}
	}
	
	public void keyWordsRecognization(String contentString) {
		String finalString = null;
		int insertSimCount = getInsertSimCount();
		
		boolean isNumberOrChar = isNumberOrCharContained(contentString);
		Log.d("DEBUG", "keyWordsRecognization-----------isNumberOrChar = "+isNumberOrChar);
		Log.d("DEBUG", "keyWordsRecognization-----------isVoiceInput = "+isVoiceInput);
		setOffLineType(OFFLINE_TYPE.NONE);
		
		if (isMessageCommand(contentString)) {
			setOffLineType(OFFLINE_TYPE.MESSAGE);
			
			if (contentString.contains(CFG.OFFLINE_KEYWORDS_SHUO)) {
				int index = contentString.indexOf(CFG.OFFLINE_KEYWORDS_SHUO);
				contentString = contentString.substring(0, index);
			}
			finalString = contentString.replaceAll(CFG.OFFLINE_KEYWORDS_FA, "")
									   .replaceAll(CFG.OFFLINE_KEYWORDS_MESSAGE, "")
									   .replaceAll(CFG.OFFLINE_KEYWORDS_SONG, "")
									   .replaceAll(CFG.OFFLINE_KEYWORDS_GEI, "")
									   .replaceAll(CFG.OFFLINE_KEYWORDS_GE, "");
			
			Log.d("DEBUG", "keyWordsRecognization-----------发短信=final string = "+finalString);
			
			if (!finalString.isEmpty()) {
				queryContacts(finalString, isNumberOrChar);

			} else {
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				String callDefault = getResources().getString(R.string.vs_offline_answer_message_default);
				mOfflineAnswer.append(callDefault);
				
				sendMessageBySystem("", 0, null);
			}

		} else if (isCallCommand(contentString)) {
			setOffLineType(OFFLINE_TYPE.CALLING);
			
			finalString = contentString.replaceAll(CFG.OFFLINE_KEYWORDS_DA, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_PHONE, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_NUMBER, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_DE, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_GEI, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_BO, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_GE, "")
										.replaceAll(CFG.OFFLINE_KEYWORDS_QU, "");
			
			Log.d("DEBUG", "keyWordsRecognization-----------打电话=final string = "+finalString);
			
			if (!finalString.isEmpty()) {
				queryContacts(finalString, isNumberOrChar);
				
			} else {
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				String callDefault = getResources().getString(R.string.vs_offline_answer_call_default);
				mOfflineAnswer.append(callDefault);
				
				callContactBySimCard(null, getLastCallSlotId(null));
			}
		
		} else if (isSearchContactsCommand(contentString)) {
			setOffLineType(OFFLINE_TYPE.CONTACTS);
			
			finalString = contentString.replaceAll(CFG.OFFLINE_KEYWORDS_PHONE_2, "").replaceAll(CFG.OFFLINE_KEYWORDS_NUMBER_2, "");
			
			Log.d("DEBUG", "keyWordsRecognization-----------搜联系人1=final string =  "+finalString);

			queryContacts(finalString, isNumberOrChar);
			
		} else if (queryContacts(contentString, isNumberOrChar)) {
			
			Log.d("DEBUG", "keyWordsRecognization-----------搜联系人2");
			setOffLineType(OFFLINE_TYPE.CONTACTS);
			
		} else if (isApplicationCommand(contentString)) {
			setOffLineType(OFFLINE_TYPE.APPLICATION);
			
			finalString = contentString.replaceAll(CFG.OFFLINE_KEYWORDS_DAKAI, "");
			
			Log.d("DEBUG", "keyWordsRecognization-----------打开应用=final string = "+finalString);
			
			queryInstalledApplications(finalString, true);
			
		} else if (queryInstalledApplications(contentString, false)) {
			setOffLineType(OFFLINE_TYPE.APPLICATION);
			
			Log.d("DEBUG", "keyWordsRecognization-----------打开应用2");
			
		} else {
			setOffLineType(OFFLINE_TYPE.NONE);
			
			Log.d("DEBUG", "keyWordsRecognization-----------其他");
		}
		
		if (mOffLineType != OFFLINE_TYPE.NONE) {
			Log.d("DEBUG", "keyWordsRecognization-----------set answer-----finalString = "+finalString);
			if (finalString == null) {
				finalString = contentString;
			}
			if (!finalString.isEmpty()) {
				constructOfflineAnswer(finalString, mOffLineType, insertSimCount);
			} else if (mOffLineType == OFFLINE_TYPE.APPLICATION) {
				constructOfflineAnswer(finalString, mOffLineType, insertSimCount);
			}
		}
	}
	
	public void constructOfflineAnswer(String name, OFFLINE_TYPE type, int count) {
		String callHead = getResources().getString(R.string.vs_offline_answer_call_head);
		String callTail = getResources().getString(R.string.vs_offline_answer_call_tail);
		String callChoose = getResources().getString(R.string.vs_offline_answer_call_choose);
		String airplaneMode = getResources().getString(R.string.vs_offline_answer_call_airplane_mode);
		
		String messageEdit = getResources().getString(R.string.vs_offline_answer_message_edit);
		String messageChoose = getResources().getString(R.string.vs_offline_answer_message_choose);
		String messageNoSim = getResources().getString(R.string.vs_offline_answer_message_no_sim);
		
		String contactsHead = getResources().getString(R.string.vs_offline_contacts_search_head);
		String contactsTail = getResources().getString(R.string.vs_offline_contacts_search_tail);
		
		String contactsSeveral = getResources().getString(R.string.vs_offline_contacts_several);
		String contactsNotFoundHead = getResources().getString(R.string.vs_offline_contacts_not_found_head);
		
		String applications = getResources().getString(R.string.vs_offline_applications);
		String applicationModel = getResources().getString(R.string.vs_offline_application_model);
		String applicationNotFound = getResources().getString(R.string.vs_offline_application_not_found);
		String applicationWillRun = getResources().getString(R.string.vs_offline_application_will_run);
		String applicationIsRunning = getResources().getString(R.string.vs_offline_application_is_running);
		
		if (isAirPlaneModeOn(context)) {
			mOfflineAnswer.delete(0, mOfflineAnswer.length());
			mOfflineAnswer.append(airplaneMode);
			return;
		}
		
		if (type != OFFLINE_TYPE.APPLICATION) {
			if (mContactsItemList == null || mContactsItemList.isEmpty()) {
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				mOfflineAnswer.append(contactsNotFoundHead);
				mOfflineAnswer.append(name);
				return;
			}
		}
		
		int size = mContactsItemList.size();

		switch (type) {
			case CALLING:
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				
				if (getInsertSimCount() <= 0 || getSIMCardState() == 1) {
					mOfflineAnswer.append(messageNoSim);
					
				} else if (size == 1) {
					mOfflineAnswer.append(callHead);
					mOfflineAnswer.append(name);
					mOfflineAnswer.append(callTail);
					
				} else if (size == 1 && count > 1) {
					mOfflineAnswer.append(callChoose);
					
				} else if (size > 1) {
					mOfflineAnswer.append(contactsSeveral);
				}
				
				break;
			case MESSAGE:
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				if (getInsertSimCount() <= 0 || getSIMCardState() == 1) {
					mOfflineAnswer.append(messageNoSim);
					
				} else if (size == 1) {
					mOfflineAnswer.append(messageEdit);
					
				} else if (size == 1 && count > 1) {
					mOfflineAnswer.append(messageChoose);
					
				} else if (size > 1) {
					mOfflineAnswer.append(contactsSeveral);
				}
				
				break;
			case CONTACTS:
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				if (size == 1) {
					mOfflineAnswer.append(contactsHead);
					mOfflineAnswer.append(name);
					mOfflineAnswer.append(contactsTail);
					
				} else if (size > 1) {
					mOfflineAnswer.append(contactsSeveral);
				}
				
				break;
			case APPLICATION:
				mOfflineAnswer.delete(0, mOfflineAnswer.length());
				if (mAppsItemList != null) {
					if (mAppsItemList.size() <= 0) {
						mOfflineAnswer.append(applicationNotFound);
						
//					} else if (mAppsItemList.size() == 1 && mAppsItemList.get(0).getAppName().equals("IUNI语音助手")) {
					} else if (mAppsItemList.size() == 1) {
						if (mAppsItemList.get(0).getAppName().equals(getResources().getString(R.string.vs_app_name))) {
							mOfflineAnswer.append(applicationIsRunning);
						} else {
							mOfflineAnswer.append(applicationWillRun);
						}
						
					} else {
						mOfflineAnswer.append(applications);
					}
					
				} else {
					mOfflineAnswer.append(applicationModel);
				}
				
				break;
	
			default:
				break;
		}
	}
	
	public ArrayList<ContactsItem> getContactsItemList() {
		return mContactsItemList;
	}
	
	public boolean queryContacts(String contentString, boolean isNumberOrChar) {
		mContactsItemList = null;
		
		if (contentString.isEmpty()) return false;
		
		String selectionName = null;
		String selectionContactId = null;
		Cursor mNameCursor = null;
		
		mContactsItemList = new ArrayList<ContactsItem>();
		
		//根据姓名查询，查询条件，主要用在mNameCursor游标上
		selectionName = ContactsContract.Data.MIMETYPE
						+ "='"
						+ ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
						+ "'"
						+ " AND "
						+ ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
						+ " LIKE " + "'%"+ contentString.trim() + "%'";
		Log.d("DEBUG", "queryContacts-------------selection = "+selectionName);
		
		try{
			// 根据姓名查询出完整姓名和通讯录ID			
			//if input is number or char, query contacts word by word
			//if input is chinese, query contacts by fuzzy search(get the result from searching by aurora contacts model)
			if (isNumberOrChar) {
				mNameCursor = mContentResolver.query(Data.CONTENT_URI, 
						 new String[] {
							ContactsContract.Data.CONTACT_ID,
							ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
							ContactsContract.Data.LOOKUP_KEY},
						 selectionName, null, null);
			} else {
				mNameCursor = mContentResolver.query(Uri.parse("content://com.android.contacts/aurora_multi_search/" + contentString.trim()), 
													 null, null, null, null);
			}

			if (mNameCursor == null || mNameCursor.getCount() <= 0) return false;
			Log.d("DEBUG", "queryContacts-------------mNameCursor.getCount() = "+mNameCursor.getCount());
			
			int temp = -1;
			while(mNameCursor.moveToNext()) {
				int contactIdValue = mNameCursor.getInt(0);
				String contactId = String.valueOf(contactIdValue);
				String contactName = mNameCursor.getString(1);
				
				Log.d("DEBUG", "queryContacts-------------contact ID = "+mNameCursor.getInt(0));
				Log.d("DEBUG", "queryContacts-------------displayName = "+mNameCursor.getString(1));
				Log.d("DEBUG", "queryContacts-------------lookup key or PinYin = "+mNameCursor.getString(2));
				
				if (!isVoiceInput && !isNumberOrChar) {
					if (!contactName.contains(contentString) && !contactName.equals(contentString)) {
						continue;
					}
				}
				
				if (temp != contactIdValue) {
					temp = contactIdValue;
				
					// 根据通讯录ID，查找对应的电话号码的查询条件，主要用于mNumberCursor游标
					selectionContactId = ContactsContract.CommonDataKinds.Phone.CONTACT_ID 
										 + "="  
										 + contactId;
	
					Cursor mNumberCursor = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
																  null,
																  selectionContactId, 
																  null, null);
					
					if (mNumberCursor == null || mNumberCursor.getCount() <= 0) continue;
					Log.d("DEBUG", "queryContacts-------------mNumberCursor.getCount() = "+mNumberCursor.getCount());
					
					//set contactId, displayName, lookup key
					ContactsItem mContactsItem = new ContactsItem();
					mContactsItem.setContactId(contactId);
					mContactsItem.setContactName(mNameCursor.getString(1));
//					mContactsItem.setContactLookUpKey(mNameCursor.getString(2));
					
					while(mNumberCursor.moveToNext()) {
						int typeIndex = mNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
						int numberType = mNumberCursor.getInt(typeIndex);
						int index = mNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
						String numberString = mNumberCursor.getString(index);
						
						mContactsItem.setContactNumber(numberType, numberString);
						Log.d("DEBUG", "queryContacts-------------phone_type = "+numberType+" phoneNumber = "+numberString);
						
					}
					mNumberCursor.close();
					
					mContactsItemList.add(mContactsItem);
				}
			}
			//关闭游标
			mNameCursor.close();
			
			if (mContactsItemList.isEmpty()) {
				Log.d("DEBUG", "queryContacts-------------mContactsItemList.isEmpty() = "+mContactsItemList.isEmpty());
				return false;
			}
			
			if (isNumberOrChar) {
				sortContactsListByNumber();
			}
			
		} catch(Exception e) {
			// TODO: handle exception
			Log.d("DEBUG", "queryContacts-------------Exception = "+e);
		}
		
		return true;
	}
	
	public void sortContactsListByNumber() {
		Collections.sort(mContactsItemList, new NumberStringComparator());
	}
	
	public class NumberStringComparator implements Comparator {
		@Override
		public int compare(Object obj1, Object obj2) {
			// TODO Auto-generated method stub
			ContactsItem item1 = (ContactsItem) obj1;
			ContactsItem item2 = (ContactsItem) obj2;
			
			String str1 = item1.getContactName();
			String str2 = item2.getContactName();
			
			/*if (str1 == str2) {
				return 0;
			}
			if (str1 == null) {
				return 1;
			}
			if (str2 == null) {
				return -1;
			}*/
			
			int index = 0;
			/*for (index = str1.length() - 1; index >= 0
                     && (str1.charAt(index) >= '0' && str1.charAt(index) <= '9'); index--);
			int num1 = Integer.parseInt(str1.substring(index + 1));
			Log.d("DEBUG", "num1 = ======================= "+num1);*/
			
			int num1 = -1;
			int num2 = -1;
			int i = 0;
			Log.d("DEBUG", "str1 = "+str1);
			for (i = str1.length() - 1; i >= 0; i--) {
				if (str1.charAt(i) < '0' || str1.charAt(i) > '9') {
					return 0;
				}
			}
			num1 = Integer.parseInt(str1.substring(i + 1));
			Log.d("DEBUG", "num1 = ======================= "+num1);
			/*for (index = str2.length() - 1; index >= 0
                    && (str2.charAt(index) >= '0' && str2.charAt(index) <= '9'); index--);
			int num2 = Integer.parseInt(str2.substring(index + 1));*/
			int j = 0;
			for (j = str2.length() - 1; j >= 0; j--) {
				if (str2.charAt(j) < '0' || str2.charAt(j) > '9') {
					return 0;
				}
			}
			num2 = Integer.parseInt(str2.substring(j + 1));
			
			Log.d("DEBUG", "num2 = ======================= "+num2);
			
			return num1 - num2;
		}
	}

	public boolean queryInstalledApplications(String contentString, boolean flag) {
		mAppsItemList = null;
		
		contentString = contentString.replaceAll("'", "");
		if (flag) {
			if (contentString.isEmpty()) return true;
		} else {
			if (contentString.isEmpty()) return false;
			
			if (contentString.contains(CFG.OFFLINE_KEYWORDS_DAKAI)) {
				contentString = contentString.replaceAll(CFG.OFFLINE_KEYWORDS_DAKAI, "");
				if (contentString.isEmpty()) return true;
			}
		}
		
		String nameString = contentString.toUpperCase();
		mAppsItemList = new ArrayList<AppsItem>();
		
		contentUri = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FAVORITES);
		
		String selection = ITEM_TYPE + "=" + ITEM_TYPE_APPLICATION 
        							 + " and title like '%" + nameString + "%'"
        							 + " or title like '%" + contentString + "%'"
        							 + " or simplePinyin like '%" + nameString.charAt(0) + "%'";

        Cursor mCursor = mContentResolver.query(contentUri, new String[] {"intent",TITLE}, selection, null, "simplePinyin");
        
        if (mCursor == null || mCursor.getCount() <= 0) return false;
        Log.e("DEBUG", "queryInstalledApplications-----------------------mCursor.getCount() = " + mCursor.getCount());
                
        while (mCursor.moveToNext()) {
        	Intent readIntent;
        	String intent = mCursor.getString(mCursor.getColumnIndex("intent"));
        	String title = mCursor.getString(mCursor.getColumnIndex("title"));
        	
        	if (VS_INTENT.equals(intent) && mCursor.getCount() > 1) {
				continue;
			}
        	
        	if (isNeedToAddToList(contentString, title)) {
        		Log.d("DEBUG", "queryInstalledApplications-----------------------intent:" +intent);
            	Log.d("DEBUG", "queryInstalledApplications-----------------------title:" +title);
        		try {
    				if(intent != null && !intent.equals("")){
    					readIntent = Intent.parseUri(intent, 0);
    					ComponentName componentString = readIntent.getComponent();
    					String packageName = componentString.getPackageName();
    					
    					ResolveInfo resolve = getPackageFirstResolveInfo(packageName);
    					Utils2Icon mUtils2Icon = Utils2Icon.getInstance(context);
    					Drawable drawable =mUtils2Icon.getIconDrawable(resolve.activityInfo,Utils2Icon.OUTER_SHADOW);
    					
    					AppsItem mAppsItem = new AppsItem(title, readIntent, drawable);
    					mAppsItemList.add(mAppsItem);
    				}
    				
    			} catch (Exception e) {
    				// TODO: handle exception
    				Log.e("DEBUG", "queryInstalledApplications----------------Exception:" +e);
    			}
			}
		}
        mCursor.close();
        
        if (mAppsItemList != null && mAppsItemList.size() > 0) {
			return true;
		}
        
        return false;
	}
	
	private ResolveInfo getPackageFirstResolveInfo(String packageName){
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		it.setPackage(packageName);
		PackageManager mPackageManager = getPackageManager();
		List<ResolveInfo> list=mPackageManager.queryIntentActivities(it, 0);
		ResolveInfo resolveInfo=null;
		if(!list.isEmpty()){
			resolveInfo = list.get(0);
		}
		return resolveInfo;
	}
	
	public ArrayList<AppsItem> getAppsItemsList() {
		return mAppsItemList;
	}
	
	public boolean isNeedToAddToList(String contentString, String title) {
		/*if (isVoiceInput) {
			return true;
			
		} else {*/
			if (title.toUpperCase().contains(contentString.toUpperCase()) || title.toUpperCase().equals(contentString.toUpperCase())) {
				return true;
			}
//		}
		return false;
	}
	
	public void queryMessageDataForResending(LinearLayout sendingMessageView, String number, String message, String simCard, 
											ImageView simCardIcon, String resendUri, ProgressBar sendProgressBar, ImageView resultIcon) {
		new queryMessageDataTask().execute(sendingMessageView, number, message, simCard, simCardIcon, resendUri, sendProgressBar, resultIcon);
	}
	
	private class queryMessageDataTask extends AsyncTask<Object, Object, Integer> {
		private LinearLayout mSendingMessageView;
		private String mNumber;
		private String messageString;
		private String simCard;
		private ImageView mSimCardIcon;
		private String mResendUri;
		private ProgressBar mProgressBar;
		private ImageView mResultIcon;
		
		@Override
		protected Integer doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("DEBUG", "queryMessageDataTask---------------doInBackground");
			mSendingMessageView = (LinearLayout) params[0];
			mNumber = (String) params[1];
			messageString = (String) params[2];
			simCard = (String) params[3];
			mSimCardIcon = (ImageView) params[4];
			mResendUri = (String) params[5];
			mProgressBar = (ProgressBar) params[6];
			mResultIcon = (ImageView) params[7];
			
			int result = 0;
			try {
				Cursor messageCursor = mContentResolver.query(Uri.parse(mResendUri), null, null, null, null);
				if (messageCursor == null || messageCursor.getCount() <= 0) return 0;
				
				if (messageCursor.getCount() == 1) result = 1;
				
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("DEBUG", "queryMessageDataTask---------------doInBackground-------mContentResolver.query----Exception = "+e);
			}
			
			return result;
			
		}
		
		protected void onPostExecute(Integer result) {
     		Log.d("DEBUG", "queryMessageDataTask---------------onPostExecute");
     		Log.d("DEBUG", "queryMessageDataTask---------------onPostExecute--------result = "+result);
     		if (result == 0) {
     			mProgressBar.setVisibility(View.GONE);
     			mResultIcon.setBackgroundResource(R.drawable.vs_offline_message_send_failed);
         		mResultIcon.setVisibility(View.VISIBLE);
         		
         		Toast.makeText(context, getResources().getString(R.string.vs_offline_sending_message_processed), Toast.LENGTH_LONG).show();
         		
			} else if (result == 1) {
				try {
					mContentResolver.delete(Uri.parse(mResendUri), null, null);
					
				} catch (Exception e) {
					// TODO: handle exception
					Log.d("DEBUG", "queryMessageDataTask---------------onPostExecute-------mContentResolver.delete----Exception = "+e);
				}
				sendMessageByAurora(mSendingMessageView, mNumber, messageString, 0, mSimCardIcon);
			}
     	}
		
	}
	
	public static float getScreenDensity() {
        return sDensity;
    }
	//Offline end
	
	public ArrayList<Event> getEventArrayList() {
		return mEventList;
	}
	
	//account start
	public static void startToCount(Context context, String action, int value) {
		TotalCount mTotalCount = new TotalCount(context, CFG.ACCOUNT_MODULE_ID, action, value);
		mTotalCount.CountData();
	}
	//account end

}
