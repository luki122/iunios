package com.aurora.reject;

import gionee.provider.GnTelephony.Mms;
import gionee.provider.GnTelephony.MmsSms;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.Threads;
import gionee.telephony.AuroraTelephoneManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.CircularRedirectException;

import com.aurora.reject.R;
import com.aurora.reject.adapter.PhoneAdapter;
import com.aurora.reject.adapter.SmsAdapter;
import com.aurora.reject.bean.CallLogDB;
import com.aurora.reject.bean.CallLogEntity;
import com.aurora.reject.bean.MmsDB;
import com.aurora.reject.bean.MmsItem;
import com.aurora.reject.bean.SmsDB;
import com.aurora.reject.bean.SmsEntity;
import com.aurora.reject.custom.CustomViewPaper;
import com.aurora.reject.util.AuroraBatch;
import com.aurora.reject.util.RejectApplication;
import com.aurora.reject.util.SelectionManager;
import com.aurora.reject.util.TotalCount;
import com.aurora.reject.util.YuloreUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneNumberUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraViewPager;

public class AuroraRejectActivity extends AuroraActivity implements OnItemClickListener,OnItemLongClickListener{
	private boolean ischange=false;
	private boolean isQuery=false;
	private int operationType=0;
	private boolean callBath=false;
	private boolean smsBath=false;
	private boolean flag=true;
	private boolean smsFlag=false;
	private boolean callFlag=false;
	private boolean touch=false;
	private boolean s=true;
	private boolean is;
	private boolean goToSms;
	private boolean all=false;
	private boolean goToCall=false;
	private List<MmsItem> mmsItems;
	private long thread_id;
	private String targetNumber;
	private String blackName;
	private int targetSimId;
	private String targetId;
	private int type;
	private static final int ACTION_BTN_SET = 1;
	private AuroraActionBar mActionBar;
	private boolean unload=true;
//	private CustomViewPaper mPager;//页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursorImage;// 动画图片
	private LinearLayout calls,smss;
	private RelativeLayout title;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private ImageView circle;
	private View phone_tab,sms_tab;
	private AuroraListView phone_list;
	private AuroraListView sms_list;
	private TextView phone_empty;
	private TextView sms_empty;
	public static View scrollIconView;
	private AuroraTabWidget auroraTabWidget;
	public static AuroraViewPager pager = null;
	private ContentResolver contentResolver;
	private ContentResolver mContentResolver;
	private AsyncQueryHandler mQueryHandler;
	private ContentResolver mContentResolverSms;
	private AsyncQueryHandler mQueryHandlerSms;
	private ContentResolver mContentResolverMms;
	private AsyncQueryHandler mQueryHandlerMms;
	private static Uri uri = Calls.CONTENT_URI;
	private static Uri uriName = ContactsContract.Contacts.CONTENT_URI;
	private static Uri uriNumber = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	private static Uri uriSms =Uri.parse("content://sms");
	private static Uri uriMms = Uri.parse("content://mms");
	private boolean sms_mms=false;
	private static final int SHOW_BUSY = 0;
	private static final int SHOW_LIST = 1;
	private static final int SHOW_EMPTY = 2;
	private static final int SHOW_SMS_BUSY = 3;
	private static final int SHOW_SMS_LIST = 4;
	private static final int SHOW_SMS_EMPTY = 5;
	private int mState;
	private static final int DIALOG_REFRESH = 1;
	private AuroraProgressDialog dialog;
	private PhoneAdapter mListAdapter = null;
	private SmsAdapter mSmsListAdapter = null;
	private boolean isbatch = false;
	private AuroraBatch<Integer> auroraBatch;
	private int mSelectCount = 0;
	private Map<Integer, Integer> mThreadsMap = new HashMap<Integer, Integer>();
	private boolean isShowing=false;
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.arg1==0){
				isShowing=true;
				showDialog();
			}else{
				auroraBatch.leaveSelectionMode();
//				removeDialog(DIALOG_REFRESH);
//				isShowing=false;
				touch=false;
				if(msg.arg1==2){
					callBath=false;
					startQuery();
				}else{
					smsBath=false;
					startQueryMms();
				}
			
			}
		};
	};
	private List<CallLogEntity> callList;
	private MatrixCursor callCursor=null;

	private Handler callHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			String[] tableCursor = new String[] {"_id","number", "date","area","count","reject","name","lable","simId"};
			callCursor = new MatrixCursor(tableCursor);
			for(int i=0;i<callList.size();i++){
				callCursor.addRow(new Object[] { i,callList.get(i).getDBPhomeNumber(), callList.get(i).getLastCallDate(),callList.get(i).getArea(), callList.get(i).getCount(),callList.get(i).getReject(),callList.get(i).getName(),callList.get(i).getLable(),callList.get(i).getSimId()});
			}
			if(mListAdapter == null){
				callCursor.moveToFirst();
				mListAdapter = new PhoneAdapter(AuroraRejectActivity.this, callCursor);
				phone_list.setAdapter(mListAdapter);
				updateState(SHOW_LIST);
				isQuery=false;
			}else{
				mListAdapter.changeCursor(callCursor);
				mListAdapter.notifyDataSetChanged();
				updateState(SHOW_LIST);
				isQuery=false;
			}
//			removeDialog(DIALOG_REFRESH);
			if(dialog!=null){
				dialog.dismiss();
			}
			isShowing=false;
			
//			removeDialog(DIALOG_REFRESH);
		};
	};
	
	
	private MatrixCursor cursors=null;
	private List<SmsEntity> smsList;
	private Handler smsHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			String[] tableCursor = new String[] {"_id","address", "date","body","count","thread_id","ismms","name","read"};
			cursors = new MatrixCursor(tableCursor);
			for(int i=0;i<smsList.size();i++){
				cursors.addRow(new Object[] { i,smsList.get(i).getDBPhomeNumber(), smsList.get(i).getLastDate(),smsList.get(i).getBody(), smsList.get(i).getCount(), smsList.get(i).getThread_id(), smsList.get(i).getIsMms(),smsList.get(i).getName(),smsList.get(i).getRead() });
			}
			if(mSmsListAdapter==null){
				cursors.moveToFirst();
				mSmsListAdapter = new SmsAdapter(AuroraRejectActivity.this, cursors);
				sms_list.setAdapter(mSmsListAdapter);
				updateState(SHOW_SMS_LIST);
				isQuery=false;
			}else{
				mSmsListAdapter.changeCursor(cursors);
				mSmsListAdapter.notifyDataSetChanged();
				updateState(SHOW_SMS_LIST);
				isQuery=false;
			}
//			removeDialog(DIALOG_REFRESH);
			if(dialog!=null){
				dialog.dismiss();
			}
			isShowing=false;
			
		};
	};
	
	private List<String> smsIds;
	private List<String> smsNumbers;
	private List<String> smsNumbersE164;
	private ContentObserver changeObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfUpdate) {
			if(flag&&!callBath){

					startQuery();

			}else{
				callFlag=true;
			}
		}
	};
//	private ContentObserver changeObserverSms = new ContentObserver(new Handler()) {
//
//		@Override
//		public void onChange(boolean selfUpdate) {
//			
//		}
//	};
	
	private ContentObserver changeObserverMms = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfUpdate) {
			   System.out.println("unload="+unload);
			   ischange=true;
			    	
			if (!unload) {
				if (flag&&!smsBath) {
						startQueryMms();
						System.out.println("slkfjas;lfajs;lfjasl;fj");
				}else{
					smsFlag=true;
				}

			}
			  
			
		}
	};
	private Activity mActivity; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		  StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//	        .detectAll()
//	        .penaltyLog()
//	        .penaltyDeath()
//	        .build());
		super.onCreate(savedInstanceState);
		System.out.println("onCreate");
		setAuroraContentView(R.layout.activity_main, AuroraActionBar.Type.Normal);
		SharedPreferences preferences=getSharedPreferences("user", Context.MODE_PRIVATE);
		currIndex=preferences.getInt("currIndex", 0);
		System.out.println("currIndex="+currIndex);
		mActivity= AuroraRejectActivity.this;
		SharedPreferences rejectSwitch=getSharedPreferences("com.aurora.reject_preferences", Context.MODE_PRIVATE);
		boolean smsSwitch=rejectSwitch.getBoolean("sms", true);
		boolean interceptSwitch=rejectSwitch.getBoolean("intercept", true);
		if(smsSwitch){
			Log.i("qiaohu", "antisms");
			new TotalCount(this, "180", "antisms", 1).CountData();
			
		}
		if(interceptSwitch){
			Log.i("qiaohu", "notification");
			new TotalCount(this, "180", "notification", 1).CountData();
		}
		
		mActionBar = getAuroraActionBar();
		mActionBar.setElevation(0f);
		setAuroraActionbarSplitLineVisibility(View.GONE);

		contentResolver=getContentResolver();
		mActionBar.addItem(AuroraActionBarItem.Type.Set,ACTION_BTN_SET);
		mActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
		mActionBar.setTitle(getResources().getString(R.string.reject));
//		mActionBar.getTitleView().setTextColor(getResources().getColor(R.color.actionbar_title));
//		mActionBar.setBackground(getResources().getDrawable(R.drawable.main_title_bg));
		circle=(ImageView)findViewById(R.id.circle2);
		Intent intent=getIntent();
	    all=intent.getBooleanExtra("all", false);
	    goToSms= intent.getBooleanExtra("goToSms", false);
	    goToCall= intent.getBooleanExtra("goToCall", false);
	    if((RejectApplication.getInstance().getCountCall()>0||RejectApplication.getInstance().getCountSms()>0)&&(goToSms||goToCall)){
	    	Log.i("qiaohu", "fromNotiCount");
	    	new TotalCount(this, "180", "fromnotfc", 1).CountData();
	    }
	    if(currIndex==1){
			goToSms=true;
		}
	    if(RejectApplication.getInstance().getCountCall()>0){
	    	if(RejectApplication.getInstance().getCountSms()>0){
	    		System.out.println("all=true;1111111111111111111");
	    		all=true;
	    	}else{
	    		all=false;
	    		System.out.println("all=false;11111111111111111111");
	    	}
	    	goToSms=false;
	    	Intent notificationCancel = new Intent("android.intent.action.MY_BROADCAST");  
	    	intent.putExtra("notificationCancel", true);
	    	sendBroadcast(notificationCancel);
	    	RejectApplication.getInstance().setCountCall(0);
	    	RejectApplication.getInstance().setCountSms(0);
	    }else if(RejectApplication.getInstance().getCountSms()>0){
	    	goToSms=true;
	    	Intent notificationCancel = new Intent("android.intent.action.MY_BROADCAST");  
	    	intent.putExtra("notificationCancel", true);
	    	sendBroadcast(notificationCancel);
	    	RejectApplication.getInstance().setCountCall(0);
	    	RejectApplication.getInstance().setCountSms(0);
	    	System.out.println("goToSms=true;11111111111111111111111111111");
	    }
	    
	    System.out.println("all="+all);
	    if(all){
	    	circle.setVisibility(View.VISIBLE);
	    	System.out.println("all="+all);
	    }
	    if(goToSms){
	    	is=true;
	    	currIndex=1;
	    }else{
	    	is=false;
	    	currIndex=0;
	    }
	    initAuroraTabWidget(savedInstanceState);
	    
//		InitImageView();
		InitTextView();
		InitViewPager();
		phone_list.setOnItemClickListener(this);
		phone_list.setOnItemLongClickListener(this);

		phone_list.setRecyclerListener(mListAdapter);
		
		
		
		sms_list.setOnItemClickListener(this);
		sms_list.setOnItemLongClickListener(this);

//											new Thread(){
//				     							public void run() {
//				     								Cursor pcursor = (Cursor) sms_list
//				 		     								.getItemAtPosition(position);
//				 		     						if (pcursor == null) {
//				 		     							return;
//				 		     						}
//				 		     						String p=pcursor.getString(pcursor.getColumnIndex("address"));
//				 		     						String p2=pcursor.getString(pcursor.getColumnIndex("thread_id"));
//				 		     						String numberE164 = PhoneNumberUtils.formatNumberToE164(
//						     								p, getCurrentCountryIso(AuroraRejectActivity.this));
//						     						if(numberE164!=null){
//						     							contentResolver.delete(uriSms, "address=? and type=1 and reject=1",new String[] {numberE164});
//						     						}
////				 		     						sms_list.auroraDeleteSelectedItemAnim();// 首先执行控件的删除动画
//				     								contentResolver.delete(uriMms, "reject=1 and msg_box=1 and thread_id=?", new String[] { p2 });
//					     							contentResolver.delete(uriSms, "address=? and type=1 and reject=1", new String[] { p });
//				     							};
//				     						}.start();

		sms_list.setRecyclerListener(mSmsListAdapter);

		
		mContentResolver = getContentResolver();
		mQueryHandler = new QueryHandler(mContentResolver, this);
		// Aurora xuyong 2016-01-06 deleted for CTS test start
		//startQuery();
		// Aurora xuyong 2016-01-06 deleted for CTS test end
		
		mContentResolverSms = getContentResolver();
		mQueryHandlerSms = new QueryHandlerSms(mContentResolverSms, this);
		
		mContentResolverMms = getContentResolver();
		mQueryHandlerMms = new QueryHandlerMms(mContentResolverMms, this);
        // Aurora xuyong 2016-01-06 deleted for CTS test start
		//startQueryMms();
		// Aurora xuyong 2016-01-06 deleted for CTS test end
		// Aurora xuyong 2016-01-06 added for CTS test start
		getContentQuery();
		// Aurora xuyong 2016-01-06 added for CTS test end
		
		mContentResolver.registerContentObserver(uri, true, changeObserver);
		mContentResolver.registerContentObserver(uriName, true, changeObserver);
		mContentResolver.registerContentObserver(uriNumber, true, changeObserver);
		
//		mContentResolverSms.registerContentObserver(uriSms, true, changeObserverSms);
//		mContentResolverSms.registerContentObserver(uriName, true, changeObserverSms);
//		mContentResolverSms.registerContentObserver(uriNumber, true, changeObserverSms);
		
		mContentResolverMms.registerContentObserver(uriSms, true, changeObserverMms);
		mContentResolverMms.registerContentObserver(uriMms, true, changeObserverMms);
		mContentResolverMms.registerContentObserver(android.provider.Telephony.MmsSms.CONTENT_URI, true, changeObserverMms);
		mContentResolverMms.registerContentObserver(uriName, true, changeObserverMms);
		mContentResolverMms.registerContentObserver(uriNumber, true, changeObserverMms);
        // Aurora xuyong 2016-01-06 modified for CTS test start
		if (mNeedShowConfirmDialog = needShowConfirmDialog()) {
			showConfirmDialog(this);
			return;
		}
        // Aurora xuyong 2016-01-06 modified for CTS test end
	}
    // Aurora xuyong 2016-01-06 added for CTS test start
	private boolean mNeedShowConfirmDialog = true;
	private void getContentQuery() {
		startQuery();
		startQueryMms();
	}
    // Aurora xuyong 2016-01-06 added for CTS test end
	private void killProcess() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private void initAuroraTabWidget(Bundle savedInstanceState) {
		auroraTabWidget = (AuroraTabWidget)findViewById(R.id.auroratabwidget);
		auroraTabWidget.setOnPageChangeListener(new MainOnPageChangeListener());
		pager = auroraTabWidget.getViewPager();
		scrollIconView = auroraTabWidget.getmScrollIconLinearLayout();
	}

	private final String MMS_AUTHORITY_CONFIRM = "mms_authority_check";
	private final String MMS_AUTHORITY_CONFIRM_TAG = "mms_authority_confirmed";

	private void putConfirmStatus(AuroraCheckBox checkBox) {
		if (checkBox.isChecked()) {
			getSharedPreferences(MMS_AUTHORITY_CONFIRM, AuroraActivity.MODE_PRIVATE).edit().putBoolean(MMS_AUTHORITY_CONFIRM_TAG, true).commit();
		}
	}

	private boolean needShowConfirmDialog() {
		return !getSharedPreferences(MMS_AUTHORITY_CONFIRM, AuroraActivity.MODE_PRIVATE).getBoolean(MMS_AUTHORITY_CONFIRM_TAG, false);
	}

	private void showConfirmDialog(Context context) {
		View showContent = LayoutInflater.from(context).inflate(R.layout.aurora_alert_authority_confirm_dialog, null);
		final AuroraCheckBox checkBox = (AuroraCheckBox)showContent.findViewById(R.id.checkbox);
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature start
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature end
				.setTitle(R.string.sys_auth_dlg_title)
				.setView(showContent)
				.setPositiveButton(R.string.aurora_continue, new DialogInterface.OnClickListener() {

					@Override
					public final void onClick(DialogInterface dialog, int which) {
						putConfirmStatus(checkBox);
                        // Aurora xuyong 2016-01-06 added for CTS test start
						mNeedShowConfirmDialog = false;
						getContentQuery();
                        // Aurora xuyong 2016-01-06 added for CTS test end
					}

				})
				.setNegativeButton(R.string.aurora_exit,  new DialogInterface.OnClickListener() {

					@Override
					public final void onClick(DialogInterface dialog, int which) {
						killProcess();
					}

				})
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature start
				.create();
		dialog.setCanceledOnTouchOutside(false);
        // Aurora xuyong 2016-04-11 added for bug #22165 start
        dialog.setCancelable(false);
        // Aurora xuyong 2016-04-11 added for bug #22165 end
		dialog.show();
        // Aurora xuyong 2016-04-07 modified for aurora 2.0 new feature end
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		System.out.println("onStart");
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		System.out.println("onRestart");
		if(callFlag){
			startQuery();
		}
		if(ischange){
			if(smsFlag||sms_mms){
				startQueryMms();
				sms_mms=false;
			}
		}
		ischange=true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("onResume");
		flag=true;
		if(!ischange){
			if(callFlag){
				startQuery();
			}
			if(smsFlag||sms_mms){
				startQueryMms();
				sms_mms=false;
			}	
		}
		if(currIndex==0){
			if(phone_list!=null){
				phone_list.auroraOnResume();
			}
		}else{
			if(sms_list!=null){
				sms_list.auroraOnResume();
			}
		}
		ischange=false;
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		ischange=false;
		flag=false;
		System.out.println("onPause");
		if(currIndex==0){
			if(phone_list!=null){
				phone_list.auroraOnPause();
			}
		}else{
			if(sms_list!=null){
				sms_list.auroraOnPause();
			}
		}
		SharedPreferences preferences=getSharedPreferences("user",Context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putInt("currIndex", currIndex);
		editor.commit();
	}
	
	/**
	 * 初始化头标
	 */
	private void InitTextView() {
		calls = (LinearLayout) findViewById(R.id.calls);
		smss = (LinearLayout) findViewById(R.id.smss);
		title = (RelativeLayout) findViewById(R.id.title);
//		calls.setOnClickListener(new MyOnClickListener(0));
//		smss.setOnClickListener(new MyOnClickListener(1));
	}

	/**
	 * 初始化ViewPager
	 */
	private void InitViewPager() {
//		mPager = (CustomViewPaper) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		phone_tab=mInflater.inflate(R.layout.phone_tab, null);
		phone_list=(AuroraListView) phone_tab.findViewById(R.id.phone_list);
		phone_empty=(TextView) phone_tab.findViewById(R.id.phone_empty);
		sms_tab=mInflater.inflate(R.layout.sms_tab, null);
		sms_list=(AuroraListView) sms_tab.findViewById(R.id.sms_list);
		sms_empty=(TextView) sms_tab.findViewById(R.id.sms_empty);
		
		listViews.add(phone_tab);
		listViews.add(sms_tab);
		pager.setAdapter(new MyPagerAdapter(listViews));
		auroraTabWidget.setOnPageChangeListener(new MainOnPageChangeListener());
//		mPager.setAdapter(new MyPagerAdapter(listViews));
		System.out.println("goToSms    "+goToSms);
		 if(goToSms){
			 pager.setCurrentItem(1);
//				smss.setSelected(true);
		    }else{
		    	pager.setCurrentItem(0);
//				calls.setSelected(true);
		    }
//		
//		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

//	/**
//	 * 初始化动画
//	 */
//	private void InitImageView() {
//		cursorImage = (ImageView) findViewById(R.id.cursor);
//		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.tab_scroll)
//				.getWidth();// 获取图片宽度
//		DisplayMetrics dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		int screenW = dm.widthPixels;// 获取分辨率宽度
//		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
//		Matrix matrix = new Matrix();
//		if(goToSms){
//			System.out.println("offset+screenW / 2");
//			matrix.postTranslate(offset+screenW / 2, 0);
//		}else{
//			System.out.println("offset");
//			matrix.postTranslate(offset, 0);
//		}
//		
//		cursorImage.setImageMatrix(matrix);// 设置动画初始位置
//	}

	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends aurora.view.PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((AuroraViewPager) container).removeView(mListViews.get(position));
		}
		
		@Override
		public Object instantiateItem(View container, int position) {
			((AuroraViewPager) container).addView(mListViews.get(position));
			return mListViews.get(position);
		}

		@Override
		public void startUpdate(View arg0) {
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}
		
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

//	/**
//	 * 头标点击监听
//	 */
//	public class MyOnClickListener implements View.OnClickListener {
//		private int index = 0;
//
//		public MyOnClickListener(int i) {
//			index = i;
//		}
//
//		@Override
//		public void onClick(View v) {
//			mPager.setCurrentItem(index);
//		}
//	};
	
	/**
	 * 页卡切换监听
	 */
	public class MainOnPageChangeListener implements AuroraViewPager.OnPageChangeListener {

//		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		@Override
		public void onPageSelected(int arg0) {
//			Animation animation = null;
			System.out.println("page:  "+arg0);
			System.out.println("goToSms:  "+goToSms);
			System.out.println("goToCall:  "+goToCall);
			switch (arg0) {
			case 0:
				
				System.out.println("currIndex"+currIndex);
				
				
				if (currIndex == 1) {
					
//					if (!is) {
//						animation = new TranslateAnimation(one, 0, 0, 0);
//					} else {
//						System.out
//								.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//						animation = new TranslateAnimation(0, -one, 0, 0);
//					}
//					
					calls.setSelected(true);
					smss.setSelected(false);
					
				} 
				break;
			case 1:
				System.out.println("currIndex"+currIndex);
				if(all){
					all=false;	
					circle.setVisibility(View.GONE);
				}
				
				if (currIndex == 0) {
//					if(!is){
//						animation = new TranslateAnimation(0, one, 0, 0);
//					}else{
//						animation = new TranslateAnimation(-one, 0, 0, 0);
//					}
//					
					calls.setSelected(false);
					smss.setSelected(true);
				}
				break;
			}
//			if(animation!=null){
				currIndex = arg0;
				if(!goToSms){
					if(unload){
						startQuerySms();
						unload=false;
					}
				}
//				animation.setFillAfter(true);// True:图片停在动画结束位置
//				animation.setDuration(180);
//				cursorImage.startAnimation(animation);
//			}
			
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			System.out.println("goToSms1  "+goToSms);
			switch (arg0) {
			case 0:
				
				System.out.println("currIndex"+currIndex);
				
				
				if (currIndex == 1) {
					
					if (!is) {
						animation = new TranslateAnimation(one, 0, 0, 0);
					} else {
						System.out
								.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
						animation = new TranslateAnimation(0, -one, 0, 0);
					}
					
					calls.setSelected(true);
					smss.setSelected(false);
					
				} 
				break;
			case 1:
				if(all){
					all=false;	
					circle.setVisibility(View.GONE);
				}
				
				if (currIndex == 0) {
					if(!is){
						animation = new TranslateAnimation(0, one, 0, 0);
					}else{
						animation = new TranslateAnimation(-one, 0, 0, 0);
					}
					
					calls.setSelected(false);
					smss.setSelected(true);
				}
				break;
			}
			if(animation!=null){
				currIndex = arg0;
				if(!goToSms){
					if(unload){
						startQuerySms();
						unload=false;
					}
				}
//				animation.setFillAfter(true);// True:图片停在动画结束位置
//				animation.setDuration(180);
//				cursorImage.startAnimation(animation);
			}
			
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case ACTION_BTN_SET:
				Intent intent = new Intent(AuroraRejectActivity.this, AuroraSettingActivity.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("onDestroy");
		mContentResolver.unregisterContentObserver(changeObserver);
//		mContentResolverSms.unregisterContentObserver(changeObserverSms);
		mContentResolverMms.unregisterContentObserver(changeObserverMms);
		if(mListAdapter != null) {
			mListAdapter.changeCursor(null);
		}
	}
	
	private void startQuery() {
//		showDialog(DIALOG_REFRESH);
		isQuery=true;
		mQueryHandler.startQuery(0, null, uri, null,"type in (1,3) and reject=?", new String[] { "1" }, "_id desc");
		callFlag=false;
		
	}
	private void startQuerySms() {
//		showDialog(DIALOG_REFRESH);
		isQuery=true;
		mQueryHandlerSms.startQuery(0, null, uriSms, null, "reject=? and type=?", new String[] { "1","1" }, "_id desc");
		
	}

	private void startQueryMms() {
//		showDialog(DIALOG_REFRESH);
		mQueryHandlerMms.startQuery(0, null, uriMms, null, "reject=? and msg_box=?", new String[] { "1","1" }, "_id desc");
		smsFlag=false;
		isQuery=true;
//		mQueryHandlerMms.startQuery(0, null, uriMms, null, "msg_box=?", new String[] { "1" }, "_id desc");
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		if(isQuery){
			return true;
		}
		if (auroraBatch == null) {
			calls.setEnabled(false);
			smss.setEnabled(false);
			scrollIconView.setVisibility(View.GONE);
			pager.setCanScroll(false);
//			cursorImage.setImageResource(R.drawable.tab_scroll);
//			mActionBar.setBackgroundColor(getResources().getColor(R.color.actionbar_disable));
//			mActionBar.setBackground(getResources().getDrawable(R.drawable.main_title_bg_disable));
//			title.setBackgroundColor(getResources().getColor(R.color.actionbar_disable));
//			((TextView)mActionBar.getMiddleTextView()).setTextColor(getResources().getColor(R.color.main_batch_title));
//			((TextView)mActionBar.getSelectLeftButton()).setTextColor(getResources().getColor(R.color.main_batch_title));
//			((TextView)mActionBar.getSelectRightButton()).setTextColor(getResources().getColor(R.color.main_batch_title));
			((TextView)mActionBar.getSelectLeftButton()).setBackground(getResources().getDrawable(R.drawable.aurora_select_selector));
			((TextView)mActionBar.getSelectRightButton()).setBackground(getResources().getDrawable(R.drawable.aurora_select_right_selector));
//			((TextView)mActionBar.getMiddleTextView()).setText(getResources().getString(R.string.actionbar_count_tip, 1));
			if(currIndex==0){
				RejectApplication.getInstance().isSelectMode=2;
				RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
//		        TextView title=(TextView)(front.findViewById(R.id.title));
				calls.setSelected(false);
				setAuroraBottomBarMenuCallBack(auroraMenuCallBack); // 必须写在布局前
				mActionBar.initActionBottomBarMenu(R.menu.aurora_list_menu_delete,
						1);
				mActionBar.showActionBarDashBoard();
				isbatch = true;
				initThreadsMap();
				initAuroraBatch();
				auroraBatch.enterSelectionMode(false, position);
				
				return true;
			}else{
				smss.setSelected(false);
				setAuroraBottomBarMenuCallBack(auroraMenuCallBack); // 必须写在布局前
				mActionBar.initActionBottomBarMenu(R.menu.aurora_list_menu_sms,
						2);
				mActionBar.showActionBarDashBoard();
				isbatch = true;
				initThreadsMap();
				initAuroraBatch();
				auroraBatch.enterSelectionMode(false, position);
				return true;
			}
			
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		System.out.println("onItemClick"+currIndex);
		// TODO Auto-generated method stub
		if(isQuery){
			return ;
		}
		if(auroraBatch == null){
			if(currIndex==0){
				Cursor pcursor = (Cursor) phone_list
						.getItemAtPosition(position);
				if (pcursor == null) {
					return;
				}
				targetNumber = pcursor.getString(pcursor.getColumnIndex("number"));
				targetSimId=pcursor.getInt(pcursor.getColumnIndex("simId"));
				targetId=pcursor.getString(pcursor.getColumnIndex("_id"));
				type=pcursor.getInt(pcursor.getColumnIndex("reject"));
				blackName = pcursor.getString(pcursor.getColumnIndex("name"));
				setAuroraSystemMenuCallBack(auroraMenuCallBack);
				
				
				Cursor cursor=getContentResolver().query(Uri
						.parse("content://com.android.contacts/black"), null, "number='"+targetNumber+"' and isblack=1 and reject>0", null, null);
				boolean isblack = true;
				if(cursor!=null){
					if(cursor.getCount()>0){
						isblack = true;
						setAuroraMenuItems(R.menu.phone);
						
					}else{
						isblack = false;
						setAuroraMenuItems(R.menu.phone_record);
					}
				}
				showDialogMenu(currIndex, isblack);
//				showAuroraMenu();
				if(cursor!=null){
					cursor.close();
				}
				
			}else{
				sms_mms=true;
				Cursor pcursor = (Cursor) sms_list
						.getItemAtPosition(position);
				if (pcursor == null) {
					return;
				}
				
				thread_id = pcursor.getLong(pcursor.getColumnIndex("thread_id"));
				final Intent SmsIntent = new Intent();
				SmsIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
				SmsIntent.putExtra("thread_id", thread_id);
				SmsIntent.putExtra("isFromReject", true);
				try {
					startActivity(SmsIntent);
				} catch (ActivityNotFoundException a) {
				    a.printStackTrace();
				}
			}
			
		}else {
			if (currIndex==0) {
				AuroraCheckBox mCheckBox = (AuroraCheckBox) view
						.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				if (mCheckBox == null) {
					return;
				}
				boolean isChecked = mCheckBox.isChecked();
				mCheckBox.auroraSetChecked(!isChecked, true);
			} else {
				AuroraCheckBox mCheckBox = (AuroraCheckBox) view
						.findViewById(R.id.sms_checkbox);
				if (mCheckBox == null) {
					return;
				}
				boolean isChecked = mCheckBox.isChecked();
				mCheckBox.auroraSetChecked(!isChecked, true);
			}
			
			auroraBatch.getSelectionManger().toggle(position);
		}
		
		
	}

	private void showDialogMenu(int index, boolean isBack) {
		if (index == 0 && isBack) {
			AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
					AuroraRejectActivity.this).setItems(R.array.calllog_menu,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int position) {
							final int CALL_BACK = 0;
							final int SMS_BACK = 1;
							final int NO_REJECT = 2;
							final int VIEW = 3;
							final int DELETE = 4;
							switch (position) {
							case CALL_BACK:
								Intent intents;
								if (RejectApplication.getInstance().MTK_GEMINI_SUPPORT) {
									if (SimUtils
											.isShowDoubleButton(AuroraRejectActivity.this)) {
										intents = AuroraTelephoneManager
												.getCallNumberIntent(
														targetNumber,
														targetSimId);
									} else {
										intents = new Intent(
												Intent.ACTION_CALL, Uri
														.parse("tel:"
																+ targetNumber));
									}
								} else {
									intents = new Intent(Intent.ACTION_CALL,
											Uri.parse("tel:" + targetNumber));
								}
								startActivity(intents);
								break;
							case SMS_BACK:
								Uri uri = Uri.parse("smsto:" + targetNumber);
								Intent it = new Intent(Intent.ACTION_SENDTO,
										uri);
								startActivity(it);
								break;
							case NO_REJECT:
								View view = LayoutInflater.from(
										AuroraRejectActivity.this).inflate(
										R.layout.black_phone_remove, null);
								final AuroraCheckBox phone = (AuroraCheckBox) view
										.findViewById(R.id.phone);
								phone.setChecked(true);
								AuroraAlertDialog dia = new AuroraAlertDialog.Builder(
										AuroraRejectActivity.this)
										.setTitle(
												AuroraRejectActivity.this
														.getResources()
														.getString(
																R.string.confirm_no_reject))
										.setView(view)
										.setPositiveButton(
												android.R.string.ok,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														ContentResolver cr = getContentResolver();
														ContentValues cv = new ContentValues();
														if (phone.isChecked()) {
															cv.put("isblack", 0);

														} else {
															cv.put("isblack",
																	-1);
														}
														cv.put("number",
																targetNumber);
														cv.put("reject", type);
														int uri2 = cr.update(
																Uri.parse("content://com.android.contacts/black"),
																cv,
																"number=?",
																new String[] { targetNumber });
														System.out
																.println("updated"
																		+ ":"
																		+ uri2);
														dialog.dismiss();

													}
												})
										.setNegativeButton(
												android.R.string.cancel,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.dismiss();
													}
												}).show();
								dia.setCanceledOnTouchOutside(false);
								break;
							case VIEW: {
								// TODO Auto-generated method stub
								final Intent intent = new Intent();
								// String name = blackName;
								Log.e("System.out", "name = " + blackName
										+ "  number = " + targetNumber);
								intent.setClassName("com.android.contacts",
										"com.android.contacts.FullCallDetailActivity");
								intent.putExtra("number", targetNumber);
								intent.putExtra("black_name", blackName);
								intent.putExtra("reject_detail", true);
								// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

								String userMark = YuloreUtil.getUserMark(
										mActivity, targetNumber);
								String markContent = YuloreUtil.getMarkContent(
										targetNumber, mActivity);
								int markCount = YuloreUtil.getMarkNumber(
										mActivity, targetNumber);
								intent.putExtra("user-mark", userMark);
								intent.putExtra("mark-content", markContent);
								intent.putExtra("mark-count", markCount);

								try {
									mActivity.startActivity(intent);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
								}
								break;
							}
							case DELETE: {
								AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(
										AuroraRejectActivity.this)
										.setTitle(AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_one_dial_bn))
										/*.setMessage(
												AuroraRejectActivity.this
														.getResources()
														.getString(
																R.string.confirm_del_phone_record))*/
										.setPositiveButton(
												R.string.aurora_remove_dial_confirm,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.dismiss();
														deleteOne();
													}
												})
										.setNegativeButton(
												android.R.string.cancel,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.dismiss();
													}
												}).show();
								break;
							}
							default:
								break;
							}

						}

					}).show();
		} else if (index == 0 && !isBack) {
			AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
					AuroraRejectActivity.this).setItems(R.array.calllog_menu_rubbish,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int position) {
							final int CALL_BACK = 0;
							final int SMS_BACK = 1;
//							final int NO_REJECT = 2;
							final int VIEW = 2;
							final int DELETE = 3;
							switch (position) {
							case CALL_BACK:
								Intent intents;
								if (RejectApplication.getInstance().MTK_GEMINI_SUPPORT) {
									if (SimUtils
											.isShowDoubleButton(AuroraRejectActivity.this)) {
										intents = AuroraTelephoneManager
												.getCallNumberIntent(
														targetNumber,
														targetSimId);
									} else {
										intents = new Intent(
												Intent.ACTION_CALL, Uri
														.parse("tel:"
																+ targetNumber));
									}
								} else {
									intents = new Intent(Intent.ACTION_CALL,
											Uri.parse("tel:" + targetNumber));
								}
								startActivity(intents);
								break;
							case SMS_BACK:
								Uri uri = Uri.parse("smsto:" + targetNumber);
								Intent it = new Intent(Intent.ACTION_SENDTO,
										uri);
								startActivity(it);
								break;
							case VIEW: {
								// TODO Auto-generated method stub
								final Intent intent = new Intent();
								// String name = blackName;
								Log.e("System.out", "name = " + blackName
										+ "  number = " + targetNumber);
								intent.setClassName("com.android.contacts",
										"com.android.contacts.FullCallDetailActivity");
								intent.putExtra("number", targetNumber);
								intent.putExtra("black_name", blackName);
								intent.putExtra("reject_detail", true);
								// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

								String userMark = YuloreUtil.getUserMark(
										mActivity, targetNumber);
								String markContent = YuloreUtil.getMarkContent(
										targetNumber, mActivity);
								int markCount = YuloreUtil.getMarkNumber(
										mActivity, targetNumber);
								intent.putExtra("user-mark", userMark);
								intent.putExtra("mark-content", markContent);
								intent.putExtra("mark-count", markCount);

								try {
									mActivity.startActivity(intent);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
								}
								break;
							}
							case DELETE: {
								AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(
										AuroraRejectActivity.this)
										.setTitle(AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_one_dial_bn))
										/*.setMessage(
												AuroraRejectActivity.this
														.getResources()
														.getString(
																R.string.confirm_del_phone_record))*/
										.setPositiveButton(
												R.string.aurora_remove_dial_confirm,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.dismiss();
														deleteOne();
													}
												})
										.setNegativeButton(
												android.R.string.cancel,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int whichButton) {
														dialog.dismiss();
													}
												}).show();
								break;
							}
							default:
								break;
							}

						}

					}).show();
		}
	}
	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int arg0) {
			// TODO Auto-generated method stub
			switch (arg0) {
			case R.id.call_back_record:
				Intent intents_record;
				if(RejectApplication.getInstance().MTK_GEMINI_SUPPORT){
					if(SimUtils.isShowDoubleButton(AuroraRejectActivity.this)){
						intents_record = AuroraTelephoneManager.getCallNumberIntent(targetNumber, targetSimId);
					}else{
						intents_record = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + targetNumber));
					}
				}else{
					intents_record = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + targetNumber));
				}
                startActivity(intents_record);
				break;
			case R.id.sms_back_record:
				 Uri uri_record = Uri.parse("smsto:"+targetNumber);            
				 Intent it_record = new Intent(Intent.ACTION_SENDTO, uri_record);                  
				 startActivity(it_record);  
				break;
			
			case R.id.call_back:
				Intent intents;
				if(RejectApplication.getInstance().MTK_GEMINI_SUPPORT){
					if(SimUtils.isShowDoubleButton(AuroraRejectActivity.this)){
						intents = AuroraTelephoneManager.getCallNumberIntent(targetNumber, targetSimId);
					}else{
						intents = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + targetNumber));
					}
				}else{
					intents = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + targetNumber));
				}
                startActivity(intents);
				break;
			case R.id.sms_back:
				 Uri uri = Uri.parse("smsto:"+targetNumber);            
				 Intent it = new Intent(Intent.ACTION_SENDTO, uri);                  
				 startActivity(it);  
				break;
			case R.id.no_reject:
				View view=LayoutInflater.from(AuroraRejectActivity.this).inflate(R.layout.black_phone_remove, null);
				final AuroraCheckBox phone=(AuroraCheckBox)view.findViewById(R.id.phone);
				phone.setChecked(true);
				  AuroraAlertDialog dia = new AuroraAlertDialog.Builder(AuroraRejectActivity.this)
	                .setTitle(AuroraRejectActivity.this.getResources().getString(R.string.confirm_no_reject))
	                .setView(view)
	                .setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	ContentResolver cr = getContentResolver();
                				ContentValues cv = new ContentValues();
	                        	if(phone.isChecked()){
	                				cv.put("isblack", 0);
	                				
	                        	}else{
	                        		cv.put("isblack", -1);
	                        	}
	                        	cv.put("number", targetNumber);
	                        	cv.put("reject", type);
	                        	int uri2 = cr.update(Uri
                						.parse("content://com.android.contacts/black"), cv, "number=?", new String[] { targetNumber });
                				System.out.println("updated" + ":" + uri2);
	                        	dialog.dismiss();
	                        	
	                        }
	                    }
	                )
	                .setNegativeButton(android.R.string.cancel,   
	                		new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	dialog.dismiss();
	                    }
	                }).show();
				  dia.setCanceledOnTouchOutside(false);
				break;
			case R.id.menu_view:{
				// TODO Auto-generated method stub
				final Intent intent = new Intent();
//				String name = blackName;
				Log.e("System.out", "name = " + blackName + "  number = " + targetNumber);
				intent.setClassName("com.android.contacts",
						"com.android.contacts.FullCallDetailActivity");
				intent.putExtra("number", targetNumber);
				intent.putExtra("black_name", blackName);
				intent.putExtra("reject_detail", true);
//				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

				String userMark = YuloreUtil.getUserMark(mActivity, targetNumber);
				String markContent = YuloreUtil.getMarkContent(targetNumber, mActivity);
				int markCount = YuloreUtil.getMarkNumber(mActivity, targetNumber);
				intent.putExtra("user-mark", userMark);
				intent.putExtra("mark-content", markContent);
				intent.putExtra("mark-count", markCount);

				try {
					mActivity.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
				break;
			}
			case R.id.menu_delete:{
				AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(AuroraRejectActivity.this)
                .setTitle(AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_one_dial_bn))
                /*.setMessage(AuroraRejectActivity.this.getResources().getString(R.string.confirm_del_phone_record))*/
                .setPositiveButton(R.string.aurora_remove_dial_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
								deleteOne();
							}
						}
				)
                .setNegativeButton(android.R.string.cancel,   
                		new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	dialog.dismiss();
                    }
                }).show();
				break;
			}
			case R.id.del:
				 if(touch){
  					return;
  				}
  				touch=true;
				String title3 = null;
				int selectCount3 = auroraBatch.getSelected().size();
				if (selectCount3 <= 1) {
					title3 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_one_dial_bn);
				} else {
					int totalCount3 = mListAdapter.getCount();
					if (selectCount3 < totalCount3) {
						title3 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_multi_dial_bn, selectCount3);
					} else {
						title3 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_all_dial_bn);
					}
				}
				AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(AuroraRejectActivity.this)
                .setTitle(title3)
                //.setMessage(AuroraRejectActivity.this.getResources().getString(R.string.confirm_del_phone_record))
                .setPositiveButton(R.string.aurora_remove_dial_confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	 dialog.dismiss();
                        	 delAllSelected();
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel,   
                		new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	dialog.dismiss();
                    	touch=false;
                    }
                }).show();
				dialogs.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if(keyCode == KeyEvent.KEYCODE_BACK){
							touch=false;
						}
						return false;
					}
				});
				 dialogs.setCanceledOnTouchOutside(false);
				
				break;
//			case R.id.no_reject_multi:
//				if(touch){
//  					return;
//  				}
//				View view=LayoutInflater.from(AuroraRejectActivity.this).inflate(R.layout.black_phone_remove, null);
//				final AuroraCheckBox phone=(AuroraCheckBox)view.findViewById(R.id.phone);
//				phone.setChecked(true);
//				  AuroraAlertDialog dia = new AuroraAlertDialog.Builder(AuroraRejectActivity.this)
//	                .setTitle(AuroraRejectActivity.this.getResources().getString(R.string.confirm_no_reject))
//	                .setView(view)
//	                .setPositiveButton(android.R.string.ok,
//	                    new DialogInterface.OnClickListener() {
//	                        @Override
//	                        public void onClick(DialogInterface dialog, int whichButton) {
//	                        	ContentResolver cr = getContentResolver();
//                				ContentValues cv = new ContentValues();
//	                        	if(phone.isChecked()){
//	                				cv.put("isblack", 0);
//	                				
//	                        	}else{
//	                        		cv.put("isblack", -1);
//	                        	}
//	                        	cv.put("number", targetNumber);
//	                        	cv.put("reject", type);
//	                        	int uri2 = cr.update(Uri
//                						.parse("content://com.android.contacts/black"), cv, "number=?", new String[] { targetNumber });
//                				System.out.println("updated" + ":" + uri2);
//	                        	dialog.dismiss();
//	                        	
//	                        }
//	                    }
//	                )
//	                .setNegativeButton(android.R.string.cancel,   
//	                		new DialogInterface.OnClickListener() {
//	                    @Override
//	                    public void onClick(DialogInterface dialog, int whichButton) {
//	                    	dialog.dismiss();
//	                    }
//	                }).show();
//				  dia.setCanceledOnTouchOutside(false);
//				break;
			case R.id.delSms:
				if(touch){
					return;
				}
				touch=true;
				String title1 = null;
				int selectCount1= auroraBatch.getSelected().size();
				if (selectCount1 <= 1) {
					title1 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_one_conv_bn);
				} else {
					int totalCount1 = mSmsListAdapter.getCount();
					if (selectCount1 < totalCount1) {
						title1 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_multi_conv_bn, selectCount1);
					} else {
						title1 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_remove_all_conv_bn);
					}
				}
				AuroraAlertDialog dias = new AuroraAlertDialog.Builder(AuroraRejectActivity.this)
                .setTitle(title1)
                //.setMessage(AuroraRejectActivity.this.getResources().getString(R.string.confirm_del_sms_record))
                .setPositiveButton(R.string.aurora_remove_conv_confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	 dialog.dismiss();
                        	delAllSelectedSms();
                        	
                        	
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel,   
                		new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	dialog.dismiss();
                    	touch=false;
                    }
                }).show();
				dias.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if(keyCode == KeyEvent.KEYCODE_BACK){
							touch=false;
						}
						return false;
					}
				});
				 dias.setCanceledOnTouchOutside(false);
				break;
			case R.id.showSms:
				if(touch){
					return;
				}
				touch=true;
				String title2 = null;
				int selectCount2 = auroraBatch.getSelected().size();
				if (selectCount2 <= 1) {
					title2 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_resume_one_conv_bn);
				} else {
					int totalCount2 = mSmsListAdapter.getCount();
					if (selectCount2 < totalCount2) {
						title2 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_resume_multi_conv_bn, selectCount2);
					} else {
						title2 = AuroraRejectActivity.this.getResources().getString(R.string.aurora_resume_all_conv_bn);
					}
				}
				AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(AuroraRejectActivity.this)
//                .setTitle(AuroraRejectActivity.this.getResources().getString(R.string.recover_sms))
                .setTitle(title2)
                .setPositiveButton(R.string.aurora_resume_conv_confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	dialog.dismiss();
                        	showSms();
                        	new TotalCount(AuroraRejectActivity.this, "180", "restoresms", 1).CountData();
                        }
                    }
                )
                .setNegativeButton(android.R.string.cancel,   
                		new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	dialog.dismiss();
                    	touch=false;
                    }
                }).show();
				dialog.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if(keyCode == KeyEvent.KEYCODE_BACK){
							touch=false;
						}
						return false;
					}
				});
				dialog.setCanceledOnTouchOutside(false);
				 
				break;
			}

		}
	};
	
	private void deleteOne() {
		   new Thread(){
			  public void run() {
				String p=targetNumber;
				String numberE164 = PhoneNumberUtils.formatNumberToE164(
						p, getCurrentCountryIso(AuroraRejectActivity.this));
				if(numberE164!=null){
					contentResolver.delete(Calls.CONTENT_URI, "number=? and type in (1,3) and reject=1",new String[] {numberE164});
				}
//				mQueryHandler.startDelete(1, null, uri, "number=? and type in (1,3) and reject=1",new String[] { p });
				contentResolver.delete(Calls.CONTENT_URI, "number=? and type in (1,3) and reject=1",new String[] { p });
				
			  }; 
		   }.start();
	}
	
	private void delAllSelected(){
		new Thread(){
			@Override
			public void run() {
				 callBath=true;
				 Message message=handler.obtainMessage();
				 message.arg1=0;
				 message.sendToTarget();
				// TODO Auto-generated method stub
				List<Integer> list=auroraBatch.getSelected();
				List<String> calls=new ArrayList<String>();
				smsNumbersE164=new ArrayList<String>();
				Cursor pcursor;
				String num;
				String numberE164;
				for(int i=0;i<list.size();i++){
					
					pcursor = (Cursor) phone_list
							.getItemAtPosition(list.get(i));
					num=pcursor.getString(pcursor.getColumnIndex("number"));
					calls.add(num);
					numberE164 = PhoneNumberUtils.formatNumberToE164(
							num, getCurrentCountryIso(AuroraRejectActivity.this));
					if(numberE164!=null){
						smsNumbersE164.add(numberE164);
					}
				}
				for(int i=0;i<list.size();i++){
					contentResolver.delete(uri, "number='"+calls.get(i)+"' and type in (1,3) and reject=1",null);		
				}
				for(int i=0;i<smsNumbersE164.size();i++){
					contentResolver.delete(uri, "number='"+smsNumbersE164.get(i)+"' and type in (1,3) and reject=1",null);		
				}
				 message=handler.obtainMessage();
			     message.arg1=2;
			     message.sendToTarget();
			}
		}.start();
		
		
		
	
	}
	
	private void delAllSelectedSms(){
		// 点击了垃圾桶的响应事件
		
		new Thread(){
			@Override
			public void run() {
				smsBath=true;
				// TODO Auto-generated method stub
				Message message=handler.obtainMessage();
				 message.arg1=0;
				 message.sendToTarget();
				List<Integer> list=auroraBatch.getSelected();
				Cursor pcursor;
				smsIds=new ArrayList<String>();
				smsNumbers=new ArrayList<String>();
				smsNumbersE164=new ArrayList<String>();
				String num;
				String numberE164;
				for(int i=0;i<list.size();i++){
					pcursor = (Cursor) sms_list
							.getItemAtPosition(list.get(i));
					num=pcursor.getString(pcursor.getColumnIndex("address"));
					smsNumbers.add(num);
					numberE164 = PhoneNumberUtils.formatNumberToE164(
							num, getCurrentCountryIso(AuroraRejectActivity.this));
					if(numberE164!=null){
						smsNumbersE164.add(numberE164);
					}
					smsIds.add(pcursor.getString(pcursor.getColumnIndex("thread_id")));
				}
				System.out.println("smsNumbers.get(i)="+smsNumbers.get(0));
				for(int i=0;i<list.size();i++){
					contentResolver.delete(uriMms, "reject=1 and msg_box=1 and thread_id=?", new String[] { smsIds.get(i) });
					contentResolver.delete(uriSms, "address=? and type=1 and reject=1", new String[] { smsNumbers.get(i) });
					
				}
				for(int i=0;i<smsNumbersE164.size();i++){
					contentResolver.delete(uriSms, "address=? and type=1 and reject=1", new String[] { smsNumbersE164.get(i) });
				}
				 message=handler.obtainMessage();
			     message.arg1=1;
			     message.sendToTarget();
			}
		}.start();
		
		
		
		
		
		
		
	
	}
	public static final String getCurrentCountryIso(Context context) {
        CountryDetector detector =
                (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
        return detector.detectCountry().getCountryIso();
    }
	
	private void showSms(){
		// 点击了垃圾桶的响应事件
		operationType=1;
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				smsBath=true;
				Message message = handler.obtainMessage();
				message.arg1 = 0;
				message.sendToTarget();
				List<Integer> list = auroraBatch.getSelected();
				Cursor pcursor;
				smsIds=new ArrayList<String>();
				smsNumbers=new ArrayList<String>();
				smsNumbersE164=new ArrayList<String>();
				String num;
				String numberE164;
				for (int i = 0; i < list.size(); i++) {
					pcursor = (Cursor) sms_list.getItemAtPosition(list.get(i));
//					num=pcursor.getString(pcursor.getColumnIndex("address"));
//					smsNumbers.add(num) ;
//					numberE164 = PhoneNumberUtils.formatNumberToE164(
//							num, getCurrentCountryIso(AuroraRejectActivity.this));
//					if(numberE164!=null){
//						smsNumbersE164.add(numberE164);
//					}
					smsIds.add(pcursor.getString(pcursor.getColumnIndex("thread_id")));
				}
//				ContentResolver cr = getContentResolver();
//				ContentValues cv = new ContentValues();
//				cv.put("reject", 0);
//				cv.put("update_threads", 0);
				
				ContentValues threadValuesInsert = new ContentValues();
				threadValuesInsert.put("reject", 0);
				for (int i = 0; i < list.size(); i++) {
				 Uri threadUriInsert = Uri.parse(
			                "content://mms-sms/conversations_resume_all/" + smsIds.get(i));
		         getContentResolver().update(threadUriInsert, threadValuesInsert, null, null);
					
//					contentResolver.update(uriSms, cv,
//							"type=1 and reject=1 and address='" + smsNumbers.get(i) + "'",
//							null);
//					
//					contentResolver.update(uriMms, cv,
//							"reject=1 and msg_box=1 and thread_id=" + smsIds.get(i), null);

				}
//				for (int i = 0; i < smsNumbersE164.size(); i++) {
//					contentResolver.update(uriSms, cv,
//							"type=1 and reject=1 and address='" + smsNumbersE164.get(i) + "'",
//							null);
//				}
				message = handler.obtainMessage();
				message.arg1 = 1;
				message.sendToTarget();
			}
		}.start();
	}
	
	private class QueryHandler extends AsyncQueryHandler {
		private final Context context;

		public QueryHandler(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, final Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
//			removeDialog(DIALOG_REFRESH);
            // Aurora xuyong 2016-01-06 added for CTS test start
			if (mNeedShowConfirmDialog) {
				updateState(SHOW_EMPTY);
				return;
			}
            // Aurora xuyong 2016-01-06 added for CTS test end
			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					updateState(SHOW_EMPTY);
					isQuery=false;
//					removeDialog(DIALOG_REFRESH);
					if(dialog!=null){
						dialog.dismiss();
					}
					isShowing=false;
					cursor.close();
				} else{
//					showDialog(DIALOG_REFRESH);
					new Thread(){
						public void run() {
							callList=CallLogDB.queryCallLogs(cursor,context);
							callHandler.obtainMessage().sendToTarget();
						};
					}.start();
				
					
				}
			} else {
				if (mListAdapter != null) {
					mListAdapter.changeCursor(null);
					mListAdapter.notifyDataSetChanged();
				}
				updateState(SHOW_EMPTY);
				isQuery=false;
//				removeDialog(DIALOG_REFRESH);
				if(dialog!=null){
					dialog.dismiss();
				}
				isShowing=false;
			}
			
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}
	
	
	
	private class QueryHandlerSms extends AsyncQueryHandler {
		private final Context context;

		public QueryHandlerSms(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, final Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
//			removeDialog(DIALOG_REFRESH);
            // Aurora xuyong 2016-01-06 added for CTS test start
			if (mNeedShowConfirmDialog) {
				updateState(SHOW_SMS_EMPTY);
				return;
			}
            // Aurora xuyong 2016-01-06 added for CTS test end
			if (cursor != null) {
				if (!cursor.moveToFirst()&&mmsItems==null) {
					updateState(SHOW_SMS_EMPTY);
					isQuery=false;
//					removeDialog(DIALOG_REFRESH);
					if(dialog!=null){
						dialog.dismiss();
					}
					isShowing=false;
					cursor.close();
				} else{
					
					new Thread(){
						public void run() {
							smsList=SmsDB.querySms(cursor,mmsItems,AuroraRejectActivity.this);
							smsHandler.obtainMessage().sendToTarget();
						};
					}.start();
				} 
			} else {
				if (mSmsListAdapter != null) {
					mSmsListAdapter.changeCursor(null);
					mSmsListAdapter.notifyDataSetChanged();
				}
				updateState(SHOW_SMS_EMPTY);
				isQuery=false;
//				removeDialog(DIALOG_REFRESH);
				if(dialog!=null){
					dialog.dismiss();
				}
				isShowing=false;
			}
			
			
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}
	
	private class QueryHandlerMms extends AsyncQueryHandler {
		private final Context context;

		public QueryHandlerMms(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
//			removeDialog(DIALOG_REFRESH);
            // Aurora xuyong 2016-01-06 added for CTS test start
			if (mNeedShowConfirmDialog) {
				updateState(SHOW_SMS_EMPTY);
				if(goToSms){
					startQuerySms();
					goToSms=false;
					unload=false;
				}
				return;
			}
            // Aurora xuyong 2016-01-06 added for CTS test end
			if (cursor != null) {
				if(!cursor.moveToFirst()){
					mmsItems=null;
				}else{
					mmsItems=MmsDB.queryMms(cursor,AuroraRejectActivity.this);
				}
				if(goToSms){
					startQuerySms();
					goToSms=false;
					unload=false;
				}else{
					if (!unload) {
						startQuerySms();
					}
				}
			} 
			if(cursor!=null && !cursor.isClosed()){
				cursor.close();
			}
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}
	
	
	
	
	
	private void initThreadsMap() {
		Cursor cursor;
		if(currIndex==0){
			if (mListAdapter == null || mListAdapter.getCount() == 0) {
				return;
			}
			cursor = mListAdapter.getCursor();
		}else{
			if (mSmsListAdapter == null || mSmsListAdapter.getCount() == 0) {
				return;
			}
			cursor = mSmsListAdapter.getCursor();
		}

		cursor.moveToPosition(-1);
		int i = 0;
		while (cursor.moveToNext()) {
			if (mThreadsMap.get(i) == null) {
				mThreadsMap.put(i++, i - 1);
			}
		}
	}

	private void updateState(int state) {
		if (mState == state) {
			return;
		}

		mState = state;
		switch (state) {
		case SHOW_LIST:
			phone_empty.setVisibility(View.GONE);
			phone_list.setVisibility(View.VISIBLE);
			break;
		case SHOW_EMPTY:
			phone_list.setVisibility(View.GONE);
			phone_empty.setVisibility(View.VISIBLE);
			break;
		case SHOW_BUSY:
			phone_list.setVisibility(View.GONE);
			phone_empty.setVisibility(View.GONE);
//			showDialog(DIALOG_REFRESH);
			break;
			
			
			

		case SHOW_SMS_LIST:
			sms_empty.setVisibility(View.GONE);
			sms_list.setVisibility(View.VISIBLE);
			break;
		case SHOW_SMS_EMPTY:
			sms_list.setVisibility(View.GONE);
			sms_empty.setVisibility(View.VISIBLE);
			break;
		case SHOW_SMS_BUSY:
			sms_list.setVisibility(View.GONE);
			sms_empty.setVisibility(View.GONE);
//			showDialog(DIALOG_REFRESH);
			break;
		
		}
	}

	// showDialog（）回调此方法
//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//		case DIALOG_REFRESH: {
//			if (dialog != null && dialog.getContext() != this) {
//				removeDialog(DIALOG_REFRESH);
//			}
//			dialog = new AuroraProgressDialog(this);
//			dialog.setIndeterminate(true);
//			dialog.setCancelable(false);
//			if(operationType==0){
//				dialog.setMessage(getResources().getString(R.string.dels));
//			}else{
//				dialog.setMessage(getResources().getString(R.string.recovery));
//				operationType=0;
//			}
//			return dialog;
//		}
//		}
//		return null;
//	}
	private void showDialog(){
		if (dialog == null) {
			dialog = new AuroraProgressDialog(this);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
		}
		if(operationType==0){
			dialog.setMessage(getResources().getString(R.string.dels));
		}else{
			dialog.setMessage(getResources().getString(R.string.recovery));
			operationType=0;
		}
		dialog.show();
	}

	// launchMode
	// 为singleTask的时候，通过Intent启到一个Activity,如果系统已经存在一个实例，系统就会将请求发送到这个实例上，但这个时候，系统就不会再调用通常情况下我们处理请求数据的onCreate方法，而是调用onNewIntent方法
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}
	
	private void initAuroraBatch() {
		auroraBatch = new AuroraBatch<Integer>(mActionBar, this) {
			@Override
			public void enterSelectionMode(boolean autoLeave,
					Integer itemPressing) {
				phone_list.auroraSetNeedSlideDelete(false);
				sms_list.auroraSetNeedSlideDelete(false);
				if(currIndex==0){
					
					phone_list.auroraEnableSelector(false);// Item点击效果的设置
					mListAdapter.showCheckBox(true);
					mListAdapter.setCheckBoxAnim(true);
					super.enterSelectionMode(autoLeave, itemPressing);
					mListAdapter.notifyDataSetChanged();
				}else{
					
					sms_list.auroraEnableSelector(false);// Item点击效果的设置
					mSmsListAdapter.showCheckBox(true);
					mSmsListAdapter.setCheckBoxAnim(true);
					super.enterSelectionMode(autoLeave, itemPressing);
					mSmsListAdapter.notifyDataSetChanged();
				}
				
			}

			@Override
			public Set getDataSet() {
				Set<Integer> dataSet = new HashSet<Integer>(mThreadsMap.size());
				for (int i = 0; i < mThreadsMap.size(); i++)
					dataSet.add(mThreadsMap.get(i));
				return dataSet;
			}

			@Override
			public void leaveSelectionMode() {
				RejectApplication.getInstance().isSelectMode=1;
				calls.setEnabled(true);
				smss.setEnabled(true);
				scrollIconView.setVisibility(View.VISIBLE);
				pager.setCanScroll(true);
//				cursorImage.setImageResource(R.drawable.tab_scroll);
//				mActionBar.setBackground(getResources().getDrawable(R.drawable.main_title_bg));
//				title.setBackgroundColor(getResources().getColor(R.color.title_bg));
				mActionBar.setShowBottomBarMenu(false);
				mActionBar.showActionBarDashBoard();
				isbatch = false;
				if(currIndex==0){
					calls.setSelected(true);
					if (phone_list != null) {
						phone_list.auroraEnableSelector(true);
					}
					if (mListAdapter != null) {
						mListAdapter.showCheckBox(false);
						mListAdapter.setCheckBoxAnim(false);
						mListAdapter.updateAllCheckBox(0);
					}
					
					if (auroraBatch != null) {
						auroraBatch.destroyAction();
					}
					if (mListAdapter != null) {
						mListAdapter.notifyDataSetChanged();
					}
				}else{
					smss.setSelected(true);
					if (sms_list != null) {
						sms_list.auroraEnableSelector(true);
					}
					if (mSmsListAdapter != null) {
						mSmsListAdapter.showCheckBox(false);
						mSmsListAdapter.setCheckBoxAnim(false);
						mSmsListAdapter.updateAllCheckBox(0);
					}
					
					if (auroraBatch != null) {
						auroraBatch.destroyAction();
					}
					if (mSmsListAdapter != null) {
						mSmsListAdapter.notifyDataSetChanged();
					}
				}
				
			
				if (auroraBatch != null) {
					auroraBatch = null;
				}
				if (mThreadsMap != null) {
					mThreadsMap.clear();
				}
				

			}

			@Override
			public void updateUi() {
				mSelectCount = null != getSelected() ? getSelected().size() : 0;
				if(currIndex==0){
					mActionBar.getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(0,
							mSelectCount == 0 ? false : true);
				}else{
					mActionBar.getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(0,
							mSelectCount == 0 ? false : true);
					mActionBar.getAuroraActionBottomBarMenu()
					.setBottomMenuItemEnable(1,
							mSelectCount == 0 ? false : true);
				}
			}

			@Override
			public void updateListView(int allShow) {
				if(currIndex==0){
					mListAdapter.updateAllCheckBox(allShow);
					mListAdapter.notifyDataSetChanged();
				}else{
					mSmsListAdapter.updateAllCheckBox(allShow);
					mSmsListAdapter.notifyDataSetChanged();
				}
				
			}

			@Override
			public void bindToAdapter(SelectionManager<Integer> selectionManager) {
				if(currIndex==0){
					if (mListAdapter != null) mListAdapter.setSelectionManager(selectionManager);
				}else{
					if (mSmsListAdapter != null) mSmsListAdapter.setSelectionManager(selectionManager);
				}
				
			}
		};

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			touch=false;
			if(isShowing){
				return true;
			}
			if (isbatch) {
				auroraBatch.leaveSelectionMode();
				return true;
			}
			if(currIndex==0){
				if (phone_list.auroraIsRubbishOut()) {
					phone_list.auroraSetRubbishBack();
					return true;
				}
			}else{
				if (sms_list.auroraIsRubbishOut()) {
					sms_list.auroraSetRubbishBack();
					return true;
				}
			}
			break;
		case KeyEvent.KEYCODE_MENU: {
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}


	
}
