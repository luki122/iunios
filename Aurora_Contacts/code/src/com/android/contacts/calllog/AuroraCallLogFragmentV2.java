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
 * See the License for the specific language governing permissions andsetMarqueeText1:
 * limitations under the License.
 */

package com.android.contacts.calllog;

import com.android.contacts.activities.AuroraPrivateCallLogActivity;
import com.gionee.internal.telephony.GnITelephony;


import gionee.provider.GnTelephony;
import com.android.common.io.MoreCloseables;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.FragmentCallbacks;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.mediatek.contacts.activities.CallLogMultipleDeleteActivity;
import com.android.contacts.AuroraCallDetailActivity.ViewEntry;
import com.android.contacts.activities.AuroraCallLogActivity;
import com.android.contacts.activities.AuroraCallRecordActivity;
import com.android.contacts.activities.AuroraContactsSetting;
import com.android.contacts.activities.AuroraDialActivity;
import com.android.contacts.activities.AuroraDialActivityV3;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.activities.MainFragment;
import com.android.contacts.util.CommonUtils;
import com.android.contacts.util.EmptyLoader;
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.StatisticsUtil;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.voicemail.VoicemailStatusHelper;
import com.android.contacts.voicemail.VoicemailStatusHelper.StatusMessage;
import com.android.contacts.voicemail.VoicemailStatusHelperImpl;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.ITelephony;
import com.google.common.annotations.VisibleForTesting;
import com.android.contacts.util.DensityUtil;

import android.R.integer;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import aurora.preference.AuroraPreferenceManager; // import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import aurora.widget.AuroraButton;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import aurora.widget.AuroraSpinner;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.android.contacts.util.Constants;
import com.android.contacts.util.GnHotLinesUtil.T9SearchRetColumns;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.widget.SimPickerDialog;
import com.mediatek.contacts.calllog.CallLogListItemView;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.android.contacts.calllog.AuroraCallLogAdapterV2;
import com.android.contacts.calllog.PhoneNumberHelper;
// The following lines are provided and maintained by Mediatek Inc.
import com.mediatek.contacts.list.CallLogUnavailableFragment;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Inflater;

import android.widget.ProgressBar;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
// The previous lines are provided and maintained by Mediatek Inc.

// aurora <ukiliu> <2013-9-27> add for aurora ui begin
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.widget.NormalAuroraActionBarItem;
import aurora.widget.AuroraButton;
import aurora.app.AuroraAlertDialog; 



// aurora <ukiliu> <2013-9-27> add for aurora ui end
import com.android.contacts.GNContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants;

import aurora.app.AuroraProgressDialog;

import com.privacymanage.service.AuroraPrivacyUtils;
import com.yulore.framework.YuloreHelper;

import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import gionee.provider.GnCallLog;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnTelephony.SimInfo;
/**
 * Displays a list of call log entries.
 */
public class AuroraCallLogFragmentV2 extends ListFragment implements
CallLogQueryHandler.Listener,
AuroraCallLogAdapterV2.CallFetcher, View.OnClickListener,
OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "liyang-AuroraCallLogFragmentV2";

	private static final int AURORA_EDIT_CALLLOG = 1;
	/**
	 * ID of the empty loader to defer other fragments.
	 */
	private static final int EMPTY_LOADER_ID = 0;
	private static final int TAB_INDEX_CALL_LOG = 1;

	private AuroraCallLogAdapterV2 mAdapter;
	private CallLogQueryHandler mCallLogQueryHandler;
	private boolean mScrollToTop;

	/** Whether there is at least one voicemail source installed. */
	private boolean mVoicemailSourcesAvailable = false;
	/** Whether we are currently filtering over voicemail. */
	private boolean mShowingVoicemailOnly = false;

	private VoicemailStatusHelper mVoicemailStatusHelper;
	private View mStatusMessageView;
	private TextView mStatusMessageText;
	private TextView mStatusMessageAction;
	private KeyguardManager mKeyguardManager;

	private boolean mEmptyLoaderRunning;
	private boolean mCallLogFetched;
	private boolean mVoicemailStatusFetched;


	private boolean isLongClickEnable = true;
	private static String mSelectAllStr;
	private static String mUnSelectAllStr;

	private String[] CALL_TYPE_TITLE;
	private String[] CALL_TYPE_EMPTY_MSG;
	private View mCallLogLayut;
	private AuroraButton mAllCallLogType;
	private AuroraButton mMissingCallLogType;
	private static boolean editMode = false;
	private Button calllog_delete;

	//aurora add zhouxiaobing 20140512 start
	public static final String UNKNOWN_NUMBER = "-1";
	public static final String PRIVATE_NUMBER = "-2";
	public static final String PAYPHONE_NUMBER = "-3";
	//aurora add zhouxiaobing 20140512 end
	private static ExecutorService exec = Executors.newSingleThreadExecutor();

	//	public static RelativeLayout mToolBar;
	private TextView leftTextView;
	private TextView rightTextView;
	private GnContactInfo contactInfo;
	public static PopupWindow popupWindow;
	private LinearLayout pop_detail;
	private LinearLayout pop_sendsms;
	private LinearLayout pop_copy;
	private LinearLayout pop_delete;

	private Context context;
	private List<Long> simInfoId; 
	public Handler handler=new Handler(){

		public void handleMessage(Message msg){
			if(msg.what==AuroraDialActivity.SWITCH_TO_PAGE0){//SWITCH_TO_PAGE0
				setDefaultActionBar(AuroraDialActivity.auroraDialActivity, actionBar);	

			}else if(msg.what==AuroraDialActivity.DESTROY_ACTIVITY){//DESTROY_ACTIVITY
				AuroraCallLogFragmentV2.this.onDestroy();
			}else if(msg.what==AuroraDialActivity.ON_RESUME){
				//				resume();
				updateOnEntry();
			}else if(msg.what==AuroraDialActivity.SWITCH_TO_NORMAL_MODE){
				switch2NormalMode();
				initActionBar(false);
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
	}

	public static boolean dismissPopupWindow() {
		try{
			if (popupWindow != null && popupWindow.isShowing()) {
				popupWindow.dismiss();
				popupWindow = null;
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public class TimeChangeBroadCastReceiver extends BroadcastReceiver    
	{     

		@Override  
		public void onReceive(Context context, Intent intent)   
		{   
			String action = intent.getAction();    	

			Log.d(TAG, "onReceive: "+intent.getAction());
			if (action.equals(ACTION_TIME_CHANGED)) {
				if (getActivity() == null || getActivity().isFinishing() || editMode) {
					return;
				}
				refreshData();
			}
		}   

	} 




	// aurora <ukiliu> <2013-9-27> add for aurora ui begin
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE);
	}
	// aurora <ukiliu> <2013-9-27> add for aurora ui endcall_log/calls

	private TimeChangeBroadCastReceiver myBroadCastReceiver;
	private static final String ACTION_TIME_CHANGED = Intent.ACTION_TIME_CHANGED;
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		context = this.getActivity();
		editMode=false;		
		if(getActivity() instanceof AuroraPrivateCallLogActivity){ 
			//			mIsPrivate = ((AuroraPrivateCallLogActivity)getActivity()).isPrivate();
			mIsPrivate=true;
		}

		actionBar=mIsPrivate? ((AuroraActivity)getActivity()).getAuroraActionBar():AuroraDialActivityV3.actionBar;
		Log.d(TAG, "onCreate,mIsPrivate:"+mIsPrivate+" actionBar:"+actionBar);
		//		actionBar=AuroraDialActivityV3.actionBar;
		AuroraDialActivity.setCallLogHandler(handler);

		mScrollToTop = true;
		mCallLogQueryHandler = new CallLogQueryHandler(getActivity()
				.getContentResolver(), this);
		mKeyguardManager = (KeyguardManager) getActivity().getSystemService(
				Context.KEYGUARD_SERVICE);

		SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler,
				SIM_INFO_UPDATE_MESSAGE, null);

		CALL_TYPE_TITLE = getResources().getStringArray(
				R.array.gn_call_log_type);
		CALL_TYPE_EMPTY_MSG = getResources().getStringArray(
				R.array.gn_call_log_type_empty);

		mContactChangeObserver = new ContactChangeObserver();

		getActivity().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContactChangeObserver);
//		getActivity().getContentResolver().registerContentObserver(GnTelephony.SimInfo.CONTENT_URI, true, mContactChangeObserver);	
//		getActivity().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mContactChangeObserver);	



		simInfoId=new ArrayList<Long>();
		List<SIMInfo> simInfo=SIMInfoWrapper.getDefault().getInsertedSimInfoList();
		for(SIMInfo info:simInfo){
			Log.d(TAG,"info.simId:"+info.mSimId+" info.simslot:"+info.mSlot);
			simInfoId.add(info.mSimId);
		}

		if(myBroadCastReceiver==null){
			//生成广播处理   
			myBroadCastReceiver = new TimeChangeBroadCastReceiver();   
			IntentFilter intentFilter = new IntentFilter(ACTION_TIME_CHANGED); 

			//注册广播   
			context.registerReceiver(myBroadCastReceiver, intentFilter);  
		}



		//		Log.d(TAG,"setAuroraMenuCallBack");
		//		((AuroraActivity)getActivity()).setAuroraSystemMenuCallBack(auroraMenuCallBack);
		//		((AuroraActivity)getActivity()).setAuroraMenuItems(R.menu.aurora_action);




	}

	public void setDefaultActionBar(AuroraActivity auroraActivity, final AuroraActionBar actionBar) {
		//		if(actionBar.getVisibility() != View.VISIBLE){
		//			            actionBar.setVisibility(View.VISIBLE);
		//		}
		//		actionBar.setTitle(mIsPrivate ? R.string.private_call_log : R.string.aurora_recentCallsLabel);
		//		AuroraActionBarItem item = actionBar.getItem(0);
		//		if(item != null){
		//			int itemId = item.getItemId();
		//			if(itemId == AURORA_EDIT_CALLLOG){
		//				actionBar.changeItemType(AuroraActionBarItem.Type.Edit, AURORA_EDIT_CALLLOG);
		//			} else {
		//				actionBar.removeItem(item);
		//				actionBar.addItem(AuroraActionBarItem.Type.Edit, AURORA_EDIT_CALLLOG);
		//			}
		//		} else {
		//			actionBar.addItem(AuroraActionBarItem.Type.Edit, AURORA_EDIT_CALLLOG);
		//		}
		//		actionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		if (auroraActivity != null) {
			auroraActivity.setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		}
		if(mIsPrivate) {
			actionBar.initActionBottomBarMenu(R.menu.aurora_delete, 1); 
		} else {
			actionBar.initActionBottomBarMenu(R.menu.aurora_calllog_delete, 1);
		}

		//		//aurora add liguangyu 201311204 start
		//		if(!mIsPrivate) {
		//			auroraActivity.setAuroraMenuCallBack(auroraMenuCallBackCallSettings);
		//			auroraActivity.setAuroraMenuItems(R.menu.aurora_dialtacts_options);
		//			auroraActivity.getAuroraMenu().removeMenuItemById(R.id.menu_add_new_contact);
		//			auroraActivity.getAuroraMenu().removeMenuItemById(R.id.menu_add_exist_contact);
		//		}
		//		//aurora add liguangyu 201311204 end
	}

	AuroraActionBar actionBar;
	public void setBottomMenuEnable(boolean flag) {
		//		actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		AuroraMenu auroraMenu = actionBar.getAuroraActionBottomBarMenu();
		if(mIsPrivate) {
			auroraMenu.setBottomMenuItemEnable(1, flag);
		} else {
			boolean value = getAdapter().getCheckedCount() == 1;
			auroraMenu.setBottomMenuItemEnable(1, flag);
			//			auroraMenu.setBottomMenuItemEnable(2, value);
			//			auroraMenu.setBottomMenuItemEnable(3, value);
		}

	}

	public void updateMenuItemState(boolean all_checked) {
		//		AuroraActionBar actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		if (all_checked) {
			if (actionBar != null && rightTextView != null) {
				rightTextView
				.setText(mUnSelectAllStr);
			}
		} else {
			if (actionBar != null && rightTextView != null) {
				rightTextView
				.setText(mSelectAllStr);
			}
		}

		if (all_checked || getAdapter().getCheckedCount() > 0) {
			setBottomMenuEnable(true);
		} else {
			setBottomMenuEnable(false);
		}
	}

	//aurora add liguangyu 20131113 for start
	public void updateActionBar() {
		//		AuroraActionBar actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		AuroraActionBarItem item = actionBar.getItem(0);
		//		if(getAdapter().getCount() == 0) {	
		//			if(item != null) {
		//				actionBar.removeItem(item);
		//			}
		//		} else {
		//			if(item == null) {
		//				actionBar.addItem(AuroraActionBarItem.Type.Edit, AURORA_EDIT_CALLLOG);
		//			}
		//		}
	}
	//aurora add liguangyu 20131113 for end

	//aurora add qiaohu 20141203 for #10247 start 
	public void updateUi(){
		//		AuroraActionBar actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		((TextView)actionBar.getSelectRightButton()).setText(mSelectAllStr);
		setBottomMenuEnable(false);
	}
	//aurora add qiaohu 20141203 for #10247 end

	//aurora add liguangyu 201311204 start
	private OnAuroraMenuItemClickListener auroraMenuCallBackCallSettings = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.menu_call_settings: {

				getActivity().startActivity(DialtactsActivity.getCallSettingsIntent());
				break; 
			}

			case R.id.menu_call_record: {
				getActivity().startActivity(new Intent(ContactsApplication.getInstance().getApplicationContext(), AuroraCallRecordActivity.class));
				break;
			}
			default:
				break;
			}
		}
	};
	//aurora add liguangyu 201311204 end

	public static FragmentCallbacks mCallbacks;	

	//	private Handler mHandler = new Handler();
	private static final int SUBACTIVITY_ACCOUNT_FILTER = 4;
	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			Log.d(TAG,"itemId:"+itemId);
			//			AuroraActionBar actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
			switch (itemId) {
			case R.id.pop_addcontact: {
				startActivity(IntentFactory.newCreateContactIntent(contactInfo.number));
				break;
			}

			case R.id.menu_delete: {
				onMenuCleanCallLog(null);
				break;
			}

			case R.id.pop_detail://查看详情
			{
				if(ContactsApplication.sIsAuroraYuloreSupport){
					Log.v(TAG, "cliv == null2");
					if(YuloreUtils.getInstance(getActivity()).getName(contactInfo.number)!=null){
						YuloreUtils.getInstance(context).startActivity(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP, "yulore.view", null, "yulore://viewDetail?tel="+contactInfo.number+"&title=通话详情", "yulore/detail_call", null, null);
					}else{
						Log.v(TAG, "cliv == null3");
						// detail
						int filterType = getCurFilterType();
						int callsType = getCallsType(filterType);
						// aurora changes zhouxiaobing 20130925
						/*
						 * Intent intent =
						 * IntentFactory.newShowCallDetailIntent(context,
						 * contactInfo.number, callsType, contactInfo.callId,
						 * contactInfo.gnCallsCount, contactInfo.voicemailUri);
						 */

						if(contactInfo.lookupUri==null){
							Log.d(TAG,"isEmpty");
							Intent intent = IntentFactory.newShowCallDetailIntent(context,
									contactInfo.number, callsType, contactInfo.callId,
									contactInfo.gnCallsCount, contactInfo.voicemailUri,
									contactInfo.ids);
							intent.putExtra("contact_sim_id", contactInfo.contactSimId);
							// aurora changes zhouxiaobing 20130925

							context.startActivity(intent);
						}else{
							Log.d(TAG,"not isEmpty");
							Intent intent = IntentFactory.newViewContactIntent(contactInfo.lookupUri);
							context.startActivity(intent);
						}
						// aurora changes zhouxiaobing 20130925

					}
				}else{
					Log.v(TAG, "cliv == null4");
					// detail
					int filterType = getCurFilterType();
					int callsType = getCallsType(filterType);
					// aurora changes zhouxiaobing 20130925
					/*
					 * Intent intent =
					 * IntentFactory.newShowCallDetailIntent(context,
					 * contactInfo.number, callsType, contactInfo.callId,
					 * contactInfo.gnCallsCount, contactInfo.voicemailUri);
					 */
					//					dismissPopupWindow();
					if(contactInfo.lookupUri==null){
						Log.d(TAG,"isEmpty");
						Intent intent = IntentFactory.newShowCallDetailIntent(context,
								contactInfo.number, callsType, contactInfo.callId,
								contactInfo.gnCallsCount, contactInfo.voicemailUri,
								contactInfo.ids);
						intent.putExtra("contact_sim_id", contactInfo.contactSimId);
						// aurora changes zhouxiaobing 20130925

						context.startActivity(intent);
					}else{
						Log.d(TAG,"not isEmpty");
						Intent intent = IntentFactory.newViewContactIntent(contactInfo.lookupUri);
						context.startActivity(intent);
					}
					// aurora changes zhouxiaobing 20130925


				}
				break;
			}

			case R.id.pop_sendsms://发送短信
			{
				//				dismissPopupWindow();
				Intent intent = IntentFactory.newCreateSmsIntent(contactInfo.number);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Log.d(TAG, "ActivityNotFoundException for secondaryIntent");
				}

				break;
			}

			case R.id.pop_copy://复制
			{
				//				ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				//				clipboard.setPrimaryClip(ClipData.newPlainText(null, contactInfo.number));
				//				Toast.makeText(getActivity(), R.string.toast_text_copied, Toast.LENGTH_SHORT).show();
				//
				//
				//				dismissPopupWindow();

				if(mCallbacks!=null){
					Intent intent=new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", contactInfo.number, null));
					mCallbacks.onFragmentCallback(FragmentCallbacks.COPY_NUMBER_TO_DIALPAD, intent);
				}
				break;
			}

			case R.id.pop_delete://删除通话记录
			{
				onMenuCleanCallLog(contactInfo);
				//				dismissPopupWindow();
				break;
			}

			/*case R.id.menu_copy: {
				String number = getCallLogCheckedNumber();
				ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setPrimaryClip(ClipData.newPlainText(null, number));
				Toast.makeText(getActivity(), R.string.toast_text_copied, Toast.LENGTH_SHORT).show();
				break;
			}
			case R.id.menu_edit: {
				final String number = getCallLogCheckedNumber();
				mHandler.postDelayed(new Runnable() {
					public void run() {   
						getActivity().startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)));
					}
				}, 450); 
				switch2NormalMode();
				Log.v(TAG, "setShowBottomBarMenu2");
				actionBar.setShowBottomBarMenu(false);
				actionBar.showActionBarDashBoard();
				((TextView) (actionBar.getActionBarMenu().getActionMenuRightView())).setText(mSelectAllStr);											
				break;
			}*/

			case R.id.aurora_menu_contacts_setting: {
				Log.d(TAG, "click aurora_menu_contacts_setting");
				Intent intent=new Intent(context, AuroraContactsSetting.class);
				startActivityForResult(intent, SUBACTIVITY_ACCOUNT_FILTER);
				//				StatisticsUtil.getInstance(context.getApplicationContext()).report(StatisticsUtil.Contact_Setting);
				break;
			}

			default:
				break;
			}
		}
	};

	//aurora modify liguangyu 20140410 for #4037 start
	private AuroraAlertDialog mCleanCallLogDialog = null;
	public void onMenuCleanCallLog(final GnContactInfo contactInfo) {
		if(contactInfo==null&&getAdapter().getCheckedCount()<=0) return;

		mCleanCallLogDialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(R.string.gn_clearSelectedCallLogConfirmation_title)  // gionee xuhz 20120728 modify for CR00658189
		//		.setMessage(R.string.gn_clearSelectedCallLogConfirmation)
		.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (getAdapter().isAllSelect()) {
					deleteAllCalllogs();
				} else {
					removeCallLogRecord(contactInfo);
				}

				updateMenuItemState(false);
				setBottomMenuEnable(false);

			}
		})
		.setNegativeButton(android.R.string.no, null).create();
		mCleanCallLogDialog.show();        
	}
	//aurora modify liguangyu 20140410 for #4037 end



	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AURORA_EDIT_CALLLOG:
				if (getAdapter().getCount() != 0) {
					switch2EditMode();
					setBottomMenuEnable(false);
				}
				break;
			default:
				break;
			}
		}
	};

	protected String getCurCallTypeTitle() {
		return CALL_TYPE_TITLE[mCurCallTypePosition];
	}

	protected String getCurCallTypeEmptyMsg() {
		return CALL_TYPE_EMPTY_MSG[mCurCallTypePosition];
	}

	// aurora changes zhouxiaobing 20130925 start
	public int checkHotlinenumber(String number) {
		String[] hotnumbers = mAdapter.getHotlinesNumber();
		String[] hotnames = mAdapter.getHotlinesName();
		if(hotnumbers==null)
			return -1;
		for(int i=0;i<hotnumbers.length;i++) {
			if(number.equalsIgnoreCase(hotnumbers[i])) {
				return i;
			}
		}
		return -1;
	}
	public static GnContactInfo contactInfos=null;
	public void changecursor(Cursor cursor) {
		if (cursor == null) {
			return;
		}
		try{
			if (cursor.moveToFirst()) {
				int[] hotnumberIndexs=mAdapter.getHotlineIndex(cursor.getCount());
				int i=0;
				do{
					String number = cursor
							.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_NUMBER);
					hotnumberIndexs[i]=checkHotlinenumber(number);
					i++;
				}while(cursor.moveToNext());
			}
			if(cursor.moveToFirst()) {	

				contactInfos=mAdapter.getContactInfo(cursor);

			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			//			cursor.close();
		}

		//aurora add liguangyu 20131113 for start
		//		if (mUpdateActionBarlistener != null) {
		//			mUpdateActionBarlistener.updateActionBar();
		//	    }
		//		updateActionBar();
		//aurora add liguangyu 20131113 for end
	}
	// aurora changes zhouxiaobing 20130925 end

	/**
	 * Called by the CallLogQueryHandler when the list of calls has been fetched
	 * or updated.
	 */
	@Override
	public void onCallsFetched(Cursor cursor) {
		log("onCallsFetched(), cursor = " + cursor);
		if(cursor!=null) Log.d(TAG, "cursor count:"+cursor.getCount());
		//		if (getActivity() == null || getActivity().isFinishing()) {
		//			return;
		//		}
		//aurora modify liguangyu 20140923 for #8487 start
		new InitHotProviderTask().executeOnExecutor(exec, cursor);
		//aurora modify liguangyu 20140923 for #8487 end
	}

	/**
	 * Called by {@link CallLogQueryHandler} after a successful query to
	 * voicemail status provider.
	 */
	@Override
	public void onVoicemailStatusFetched(Cursor statusCursor) {
		if (getActivity() == null || getActivity().isFinishing()) {
			return;
		}
		updateVoicemailStatusMessage(statusCursor);

		int activeSources = mVoicemailStatusHelper
				.getNumberActivityVoicemailSources(statusCursor);
		setVoicemailSourcesAvailable(activeSources != 0);
		MoreCloseables.closeQuietly(statusCursor);
		mVoicemailStatusFetched = true;
		destroyEmptyLoaderIfAllDataFetched();
	}

	private void destroyEmptyLoaderIfAllDataFetched() {
		if (mCallLogFetched && mVoicemailStatusFetched && mEmptyLoaderRunning) {
			mEmptyLoaderRunning = false;
			getLoaderManager().destroyLoader(EMPTY_LOADER_ID);
		}
	}

	/** Sets whether there are any voicemail sources available in the platform. */
	private void setVoicemailSourcesAvailable(boolean voicemailSourcesAvailable) {
		if (mVoicemailSourcesAvailable == voicemailSourcesAvailable)
			return;
		mVoicemailSourcesAvailable = voicemailSourcesAvailable;

		Activity activity = getActivity();
		if (activity != null) {
			// This is so that the options menu content is updated.
			activity.invalidateOptionsMenu();
		}
	}

	// aurora <ukiliu> <2013-9-27> modify for aurora ui begin
	// aurora change zhouxiaobing 20130912 start
	public static boolean getEditMode() {
		return editMode;
	}

	private void setEditMode(boolean is_edit) {
		editMode = is_edit;
		mAdapter.setEditMode(is_edit);
		mAdapter.setIs_listitem_changing(true);
		mAdapter.notifyDataSetChanged();

		if (editMode) {
			if(mIAuroraCallLogFragment != null){
				mIAuroraCallLogFragment.setTabWidget(View.INVISIBLE);
			}
			//			getActivity().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContactChangeObserver);
		} else {
			if(mIAuroraCallLogFragment != null){
				mIAuroraCallLogFragment.setTabWidget(View.VISIBLE);
			}
			mAdapter.clearAllcheckes();
			//			getActivity().getContentResolver().unregisterContentObserver(mContactChangeObserver);
		}
		//		if(mFooterView != null && !mIsPrivate) {
		//			mFooterView.setVisibility(editMode ? View.GONE : View.GONE);
		//		}
	}
	// aurora change zhouxiaobing 20130912 end
	// aurora <ukiliu> <2013-9-27> modify for aurora ui end

	public interface IAuroraCallLogFragment{
		public void setTabWidget(int visible);
	}
	private IAuroraCallLogFragment mIAuroraCallLogFragment;
	public void setIAuroraCallLogFragment(IAuroraCallLogFragment iAuroraCallLogFragment){
		mIAuroraCallLogFragment = iAuroraCallLogFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedState) {
		Log.d(TAG, "onCreateView");
		mHasPermanentMenuKey = ViewConfiguration.get(getActivity())
				.hasPermanentMenuKey();
		View fragmentView = inflater.inflate(
				R.layout.aurora_call_log_fragment_v4, container, false);
		mListView = (AuroraListView)fragmentView.findViewById(android.R.id.list);

		if(mIsPrivate){
			setDefaultActionBar((AuroraActivity)getActivity(), actionBar);
		}else{
			setDefaultActionBar(AuroraDialActivity.auroraDialActivity, actionBar);
		}


		mSelectAllStr = getResources().getString(R.string.select_all);
		mUnSelectAllStr = getResources().getString(
				R.string.unselect_all);
		//		}

		mListView.setDividerHeight(0);

		//		if(ContactsApplication.isMultiSimEnabled) {
		//			mListView.setDivider(getResources().getDrawable(R.color.aurora_calllog_divider_color));
		//			mListView.setDividerHeight(0);
		//		}            

		//		Animation animation=AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_in);
		//		LayoutAnimationController lac= new LayoutAnimationController(animation);
		//		lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
		//		lac.setDelay(0.05f);        
		//		mListView.setLayoutAnimation(lac);

		//		mFooterView = fragmentView.findViewById(R.id.footer_view);
		//		if(mIsPrivate) {
		//			mFooterView.setVisibility(View.GONE);
		//		}

		//		calllog_delete = (Button) fragmentView
		//				.findViewById(R.id.aurora_calllog_delete);

		mEmptyTitle =fragmentView
				.findViewById(R.id.gn_calllog_empty_tip);
		mLoadingContainer = fragmentView.findViewById(R.id.loading_container);
		mLoadingContainer.setVisibility(View.GONE);
		mLoadingTextView = (TextView) fragmentView
				.findViewById(R.id.loading_contact);
		mLoadingTextView.setVisibility(View.GONE);
		mProgress = (ProgressBar) fragmentView
				.findViewById(R.id.progress_loading_contact);
		mProgress.setVisibility(View.GONE);

		SharedPreferences.Editor editor = AuroraPreferenceManager
				.getDefaultSharedPreferences(this.getActivity()).edit();
		editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_ALL);
		editor.commit();

		return fragmentView;
	}

	//aurora add zhouxiaobing 20130929 start	
	public ArrayList<Integer> getCallLogCheckedId(GnContactInfo info) {
		ArrayList<Integer> als = new ArrayList<Integer>();
		int length = mAdapter.getRealCount();
		Log.d("TAG","all length::"+(length));

		if(info!=null){
			int count=info.gnCallsCount;
			Log.d("TAG","count::"+(count));
			for(int j=0;j<info.gnCallsCount;j++) {
				if(info != null && info.ids != null) {
					als.add(Integer.valueOf(info.ids[j]));
				}
			}
		}else{
			for (int i = 0; i < length; i++) {
				if (mAdapter.getCheckedArrayValue(i)) {
					Cursor cursor = (Cursor) mAdapter.getItem(i);
					GnContactInfo contactInfo = mAdapter.getContactInfo(cursor);
					int count=contactInfo.gnCallsCount;
					Log.d("TAG","count::"+(count));
					for(int j=0;j<contactInfo.gnCallsCount;j++) {
						Log.d("TAG","als == null::"+(als == null));
						Log.d("TAG","contactInfo == null::"+(contactInfo == null));
						if(contactInfo != null && contactInfo.ids != null) {
							Log.d("TAG","contactInfo.ids[j]::"+(contactInfo.ids[j]));
							als.add(Integer.valueOf(contactInfo.ids[j]));
						}
					}
				}
			}
		}
		return als;
	}

	private String toDeleteSelectionId(ArrayList<Integer> selected) {
		if (null == selected || selected.size() <= 0) {
			return NO_CALL_LOG_SELECTED;
		}

		int callType = getCurCallType();
		StringBuilder selection = new StringBuilder();
		if (callType > 0) {
			selection.append("type=").append(callType).append(" And ");
		}

		if (ContactsApplication.sIsGnCombineCalllogMatchNumber) {
			selection.append("_id in (");
			for (Integer id : selected) {
				selection.append("'").append(id.intValue()).append("',");
			}

			selection.setLength(selection.length() - 1);
			selection.append(")");
		}

		return selection.toString();
	}
	//aurora add zhouxiaobing 20130929 end

	// aurora <ukiliu> <2013-9-27> add for aurora ui begin
	public ArrayList<String> getCallLogChecked() {
		ArrayList<String> als = new ArrayList<String>();
		int length = mAdapter.getRealCount();
		for (int i = 0; i < length; i++) {
			if (mAdapter.getCheckedArrayValue(i)) {
				Cursor cursor = (Cursor) mAdapter.getItem(i);
				GnContactInfo contactInfo = mAdapter.getContactInfo(cursor);
				als.add(contactInfo.number);
			}
		}
		return als;
	}

	public void removeCallLogRecord(final GnContactInfo contactInfo) {
		//ArrayList<String> als = getCallLogChecked();
		ArrayList<Integer> als = getCallLogCheckedId(contactInfo);
		if (als == null || als.size() == 0) {
			initActionBar(false);
			switch2NormalMode();
			return;
		}
		//		initActionBar(false);
		//final String selection = toDeleteSelection(als);
		final String selection = toDeleteSelectionId(als);
		new AsyncTask<Integer, Integer, Integer>() {
			private Context mContext = getActivity();
			private AuroraProgressDialog mProgressDialog;

			protected void onPreExecute() {
				if (getActivity() != null && !getActivity().isFinishing()) {
					mProgressDialog = AuroraProgressDialog.show(mContext, "",
							getString(R.string.deleting_call_log));
				}
			};

			@Override
			protected Integer doInBackground(Integer... params) {
				String privateString = "";
				if (ContactsApplication.sIsAuroraPrivacySupport) {
					privateString = " AND privacy_id > -1 "; 
				} 
				mContext.getContentResolver().delete(Calls.CONTENT_URI,
						selection + privateString , null);
				return null;
			}

			protected void onPostExecute(Integer result) {
				if (getActivity() != null && !getActivity().isFinishing()) {
					mProgressDialog.dismiss();
					if(contactInfo==null){

						switch2NormalMode();
						initActionBar(false);
					}
				}
			};
		}.executeOnExecutor(exec);

	}



	private TextView middleTextView;
	/*private void initActionBar(boolean flag) {
		AuroraActionBar actionBar;
		actionBar = ((AuroraActivity) getActivity()).getAuroraActionBar();
		Log.i("TAG","setShowBottomBarMenu" +flag);
		actionBar.setShowBottomBarMenu(flag);
		//aurora modify liguangyu 20140527 for BUG #5151 start
		if(!flag && !editMode) {
			Log.d(TAG,"hideToolbar");
			mToolBar.setVisibility(View.GONE);
			AuroraCallLogActivity.onSetViewPagerListener.setVisibity(true);
		} else {
			Log.d(TAG,"showToolbar");
			//add by liyang:显示此Activity的ActionBar,并隐藏viewpager的ActionBar和滑动条
			//			actionBar.setVisibility(View.VISIBLE);
			mToolBar.setVisibility(View.VISIBLE);
			AuroraCallLogActivity.onSetViewPagerListener.setVisibity(false);
			//add by liyang end.


		}
		actionBar.showActionBarDashBoard();
		//aurora modify liguangyu 20140527 for BUG #5151 end
	}
	 */	


	private void initActionBar(boolean flag) {
		//		AuroraActionBar actionBar;
		//		actionBar = AuroraDialActivity.actionBar;
		actionBar.setShowBottomBarMenu(flag);
		actionBar.showActionBarDashBoard();
		if(!flag && !editMode) {

		} else {

			actionBar.auroraIsEntryEditModeAnimRunning();
			actionBar.auroraIsExitEditModeAnimRunning();

			if(leftTextView==null){
				leftTextView=(TextView) actionBar.getSelectLeftButton();
			}
			if(rightTextView==null){
				rightTextView=(TextView) actionBar.getSelectRightButton();
			}
			if(middleTextView==null){
				middleTextView=actionBar.getMiddleTextView();
			}
			if(middleTextView!=null){
				middleTextView.setText(context.getString(R.string.selected_total_num, 1));
			}

			if(leftTextView!=null){
				leftTextView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (getEditMode()) {
							//							AuroraCallLogActivity.onSetViewPagerListener.setVisibity(true);
							//							actionBar.setVisibility(View.GONE);
							//							mToolBar.setVisibility(View.GONE);

							switch2NormalMode();
							Log.v(TAG, "leftTextView onclick");
							actionBar.setShowBottomBarMenu(false);
							actionBar.showActionBarDashBoard();
						}
					}
				});
			}

			if(rightTextView!=null){
				rightTextView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.v(TAG, "rightTextView onclick");
						// TODO Auto-generated method stub
						String selectStr = ((TextView)v).getText()
								.toString();
						if (selectStr.equals(mSelectAllStr)) {
							((TextView)v)
							.setText(mUnSelectAllStr);
							onSelectAll(true);
							setBottomMenuEnable(true);
						} else if (selectStr.equals(mUnSelectAllStr)) {
							((TextView)v)
							.setText(mSelectAllStr);
							onSelectAll(false);
							setBottomMenuEnable(false);
						}
					}
				});
			}



		}
	}



	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		mAdapter = new AuroraCallLogAdapterV2(getActivity(), this);
		mAdapter.setOnItemClickListener(this);
		mAdapter.setPrivate(mIsPrivate);	
		refreshData();

		final AuroraListView listView = (AuroraListView) getListView();
		if (null != listView) {
			mAdapter.setListView(listView);
			listView.setHeaderDividersEnabled(false);
			listView.setItemsCanFocus(true);
			setListAdapter(mAdapter);
			listView.auroraSetNeedSlideDelete(false);
			listView.setOnScrollListener(mAdapter);
			// listView.setOnCreateContextMenuListener(this);
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
			editMode=false;

			/*// aurora ukiliu 2013-10-15 add for aurora UI begin添加为联系人
			listView.auroraSetAuroraBackOnClickListener(
					new AuroraListView.AuroraBackOnClickListener() {
						@Override
						public void auroraOnClick(final int position) {
							mListView.auroraDeleteSelectedItemAnim();
						}

						@Override
						public void auroraPrepareDraged(int position) {
							if (getListView() != null && getListView().getChildAt(position) != null) {
								RelativeLayout iv = (RelativeLayout)getListView().getChildAt(position).findViewById(R.id.aurora_secondary_action_icon);
								if (iv != null) {
									iv.setVisibility(View.GONE);
								}
							}
						}

						@Override
						public void auroraDragedSuccess(int position) {
							//aurora modify liguangyu 20140419 for BUG #4415 start
							try {
								if (getListView() != null && getListView().getChildAt(position) != null) {
									RelativeLayout iv = (RelativeLayout)getListView().getChildAt(position).findViewById(R.id.aurora_secondary_action_icon);
									if (iv != null) {
										iv.setVisibility(View.GONE);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							//aurora modify liguangyu 20140419 for BUG #4415 end
						}

						@Override
						public void auroraDragedUnSuccess(int position) {
							// aurora ukiliu 2013-10-17 modify for aurora ui begin
							if (getListView() != null && getListView().getChildAt(position) != null) {
								RelativeLayout iv = (RelativeLayout)getListView().getChildAt(position).findViewById(R.id.aurora_secondary_action_icon);
								//aurora change liguangyu 20131108 for BUG #511 start
								if (iv != null && isLongClickEnable) {
									//aurora change liguangyu 20131108 for BUG #511 end
									iv.setVisibility(View.VISIBLE);
								}
							}
							// aurora ukiliu 2013-10-17 modify for aurora ui end
						}


					});
			// aurora ukiliu 2013-10-15 add for aurora UI end
			 */

			/*//aurora add liguangyu 20140409 for AuroraListView SlideDelete start
			listView.auroraSetDeleteItemListener(
					new AuroraListView.AuroraDeleteItemListener() { 						
						public void auroraDeleteItem(View v,int position){
							final int pos = position;
							Log.i("TAG","listview SlideDelete pos::"+pos);
							final StringBuilder callIds = new StringBuilder();
							GnContactInfo mContactInfo = getContactInfo(position);
							if (mContactInfo == null || mContactInfo.ids == null) {
								return;
							}

							for (int callUri : mContactInfo.ids) {
								if (callIds.length() != 0) {
									callIds.append(",");
								}
								callIds.append(callUri);
							}

							String privateString = "";
							if (ContactsApplication.sIsAuroraPrivacySupport) {
								privateString = " AND privacy_id > -1 "; 
							} 
							getActivity().getContentResolver().delete(
									Calls.CONTENT_URI,
									Calls._ID + " IN (" + callIds + ")" + privateString, null);
						}
					});
			//aurora add liguangyu 20140409 for AuroraListView SlideDelete end
			 */

			//			//add contextMenu:
			//			mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {     
			//				   
			//	              public void onCreateContextMenu(ContextMenu conMenu, View view , ContextMenuInfo info) {     
			//	                   conMenu.setHeaderTitle("ContextMenu");     
			//	                   conMenu.add(0, 0, 0, "Delete!");     
			//	                     
			//	                   /* Add as many context-menu-options as you want to. */     
			//	              }     
			//	         });     
			//			//add contextMenu end.
		}

		//		updateOnEntry();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		mScrollToTop = false;//mHadGoneCallLogDetail ? false : true;//aurora change zhouxiaobing 20140612 because we need not scroll to top

		// Start the empty loader now to defer other fragments. We destroy it
		// when both calllog
		// and the voicemail status are fetched.
		getLoaderManager().initLoader(EMPTY_LOADER_ID, null,
				new EmptyLoader.Callback(getActivity()));
		mEmptyLoaderRunning = true;
		super.onStart();
	}

	//	@Override
	//	public void onResume() {
	//		Log.d(TAG,"onResume");
	//		super.onResume();
	//
	//		PhoneNumberHelper.getVoiceMailNumber();
	//		//aurora modify liguangyu 20140808 for BUG #7384 start
	//		if(!editMode) {
	//			refreshData();
	//		}
	//		//aurora modify liguangyu 20140808 for BUG #7384 end
	//		updateOnEntry();
	//
	//		if (null != mAdapter) {
	//			mAdapter.refreshSimColor();
	//		}
	//		final AuroraListView listView = (AuroraListView) getListView();
	//		listView.auroraOnResume(); 
	//	}

	public void resume(){
		Log.d(TAG,"onResume");
		super.onResume();

		PhoneNumberHelper.getVoiceMailNumber();
		//aurora modify liguangyu 20140808 for BUG #7384 start
		if(!editMode) {
			refreshData();
		}
		//aurora modify liguangyu 20140808 for BUG #7384 end
		updateOnEntry();

		if (null != mAdapter) {
			mAdapter.refreshSimColor();
		}
		final AuroraListView listView = (AuroraListView) getListView();
		listView.auroraOnResume(); 
	}

	private void updateVoicemailStatusMessage(Cursor statusCursor) {
		List<StatusMessage> messages = mVoicemailStatusHelper
				.getStatusMessages(statusCursor);
		if (messages.size() == 0) {
			mStatusMessageView.setVisibility(View.GONE);
		} else {
			mStatusMessageView.setVisibility(View.VISIBLE);
			// TODO: Change the code to show all messages. For now just pick the
			// first message.
			final StatusMessage message = messages.get(0);
			if (message.showInCallLog()) {
				mStatusMessageText.setText(message.callLogMessageId);
			}
			if (message.actionMessageId != -1) {
				mStatusMessageAction.setText(message.actionMessageId);
			}
			if (message.actionUri != null) {
				mStatusMessageAction.setVisibility(View.VISIBLE);
				mStatusMessageAction
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						getActivity().startActivity(
								new Intent(Intent.ACTION_VIEW,
										message.actionUri));
					}
				});
			} else {
				mStatusMessageAction.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		final AuroraListView listView = (AuroraListView) getListView();
		listView.auroraOnPause(); //aurora add zhouxiaobing 20131218
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		//		updateOnExit();

		if (null != mClearCallLogDialog && mClearCallLogDialog.isShowing()) {
			mClearCallLogDialog.dismiss();
		}
		if(mCleanCallLogDialog != null){
			mCleanCallLogDialog.dismiss();
		}

	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		super.onDestroy();
		mAdapter.changeCursor(null);
		SIMInfoWrapper.getDefault().unregisterForSimInfoUpdate(mHandler);
		if (getActivity() != null) {
			getActivity().getContentResolver().unregisterContentObserver(mContactChangeObserver);
		}

		try{
			context.unregisterReceiver(myBroadCastReceiver);
			myBroadCastReceiver=null;
		}catch(Exception e){
		}
	}

	@Override
	public synchronized void fetchCalls() {
		Log.d(TAG, "fetchCalls");
		if (mShowingVoicemailOnly) {
			mCallLogQueryHandler.fetchVoicemailOnly();
		} else {
			/**
			 * Change Feature by Mediatek Begin. Original Android's Code:
			 * mCallLogQueryHandler.fetchAllCalls(); Descriptions:
			 */
			Activity activity = this.getActivity();
			if (activity == null) {
				Log.e(TAG,
						" fetchCalls(), but this.getActivity() is null, use default value");
				fetchCallsJionDataView(Constants.FILTER_SIM_DEFAULT,
						Constants.FILTER_TYPE_DEFAULT,mAdapter);
			} else {

				Log.d(TAG, "fetch calls1");

				mHandler.removeCallbacks(mfetchCallsRunnable);
				mHandler.postDelayed(mfetchCallsRunnable, 400);


			}
			/**
			 * Change Feature by Mediatek End.
			 */
		}
	}

	public void startCallsQuery() {
		log("startCallsQuery()");
		Log.v(TAG, "startCallsQuery time="+System.currentTimeMillis());
		mAdapter.setLoading(true);
		/**
		 * Change Feature by Mediatek Begin. Original Android's Code:
		 * mCallLogQueryHandler.fetchAllCalls(); Descriptions:
		 */
		SharedPreferences prefs = AuroraPreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		// gionee xuhz 20120919 modify for CR00696560 start
		final int simFilter = prefs.getInt(Constants.SIM_FILTER_PREF,
				Constants.FILTER_SIM_DEFAULT);
		final int typeFilter = prefs.getInt(Constants.TYPE_FILTER_PREF,
				Constants.FILTER_TYPE_DEFAULT);

		mCallLogQueryHandler.postDelayed(new Runnable() {
			public void run() {
				Log.d(TAG, "mCallLogQueryHandler run");
				fetchCallsJionDataView(simFilter, typeFilter, mAdapter);
			}
		}, 100);//old is 500, aurora zhouxiaobing changes 
		// gionee xuhz 20120919 modify for CR00696560 end

		/**
		 * Change Feature by Mediatek End.
		 */
		if (mShowingVoicemailOnly) {
			mShowingVoicemailOnly = false;
			getActivity().invalidateOptionsMenu();
		}
		/*
		 * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
		 * ALPS00115673 Descriptions: add wait cursor
		 */
		int i = this.getListView().getCount();

		Log.i(TAG, "***********************i : " + i);
		isFinished = false;
		if (i == 0) {
			Log.i(TAG, "call sendmessage");
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(WAIT_CURSOR_START),
					WAIT_CURSOR_DELAY_TIME);

		}
		/*
		 * Bug Fix by Mediatek End.
		 */
	}

	private void startVoicemailStatusQuery() {
		log("startVoicemailStatusQuery()");
		mCallLogQueryHandler.fetchVoicemailStatus();
	}


	private void changeCalllogType(int filterType, boolean voicemailOnly) {
		SharedPreferences.Editor editor = AuroraPreferenceManager
				.getDefaultSharedPreferences(ContactsApplication.getInstance())
				.edit();

		editor.putInt(Constants.TYPE_FILTER_PREF, filterType);
		editor.commit();
		refreshData();
		mShowingVoicemailOnly = voicemailOnly;
	}

	public void callSelectedEntry() {
		log("callSelectedEntry()");
		int position = getListView().getSelectedItemPosition();
		if (position < 0) {
			// In touch mode you may often not have something selected, so
			// just call the first entry to make sure that [send] [send] calls
			// the
			// most recent entry.
			position = 0;
		}
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		if (cursor != null) {
			String number = cursor.getString(CallLogQuery.NUMBER);
			if (TextUtils.isEmpty(number)
					|| number.equals(UNKNOWN_NUMBER)//aurora change zhouxiaobing 20140512 for4.4,old is/CallerInfo.UNKNOWN_NUMBER
					|| number.equals(PRIVATE_NUMBER)
					|| number.equals(PAYPHONE_NUMBER)) {
				// This number can't be called, do nothing
				return;
			}
			Intent intent;
			// If "number" is really a SIP address, construct a sip: URI.
			if (PhoneNumberUtils.isUriNumber(number)) {
				intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
						Uri.fromParts("sip", number, null));
				intent.setClassName(Constants.PHONE_PACKAGE,
						Constants.OUTGOING_CALL_BROADCASTER);
			} else {
				// We're calling a regular PSTN phone number.
				// Construct a tel: URI, but do some other possible cleanup
				// first.
				int callType = cursor.getInt(CallLogQuery.CALL_TYPE);
				if (!number.startsWith("+")
						&& (callType == Calls.INCOMING_TYPE || callType == Calls.MISSED_TYPE)) {
					// If the caller-id matches a contact with a better
					// qualified number, use it
					String countryIso = cursor
							.getString(CallLogQuery.COUNTRY_ISO);
					number = mAdapter.getBetterNumberFromContacts(number,
							countryIso);
				}

				intent = IntentFactory.newDialNumberIntent(number);
				intent.setClassName(Constants.PHONE_PACKAGE,
						Constants.OUTGOING_CALL_BROADCASTER);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			startActivity(intent);
		}
	}

	public AuroraCallLogAdapterV2 getAdapter() {
		return mAdapter;
	}

	/** Requests updates to the data to be shown. */
	private void refreshData() {
		// Mark all entries in the contact info cache as out of date, so they
		// will be looked up
		// again once being shown.
		Log.d(TAG,"refreshData()");
		startCallsQuery();
		// Deleted by Mediatek Inc to close Google default Voicemail function.
		//		updateOnEntry();
	}

	/** Updates call data and notification state while leaving the call log tab. */
	private void updateOnExit() {
		updateOnTransition(false);
	}

	/**
	 * Updates call data and notification state while entering the call log tab.
	 */
	private void updateOnEntry() {
		updateOnTransition(true);
	}

	private void updateOnTransition(boolean onEntry) {
		log("updateOnTransition onEntry = " + onEntry);
		// We don't want to update any call data when keyguard is on because the
		// user has likely not
		// seen the new calls yet.
		//		if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
		// On either of the transitions we reset the new flag and update the
		// notifications.
		// While exiting we additionally consume all missed calls (by
		// marking them as read).
		// This will ensure that they no more appear in the "new" section
		// when we return back.
		mCallLogQueryHandler.markNewCallsAsOld();
		//			if (!onEntry) {
		mCallLogQueryHandler.markMissedCallsAsRead();
		//			}

		Log.d(TAG,"getActivity()111:"+getActivity());
		CallLogNotificationsHelper.removeMissedCallNotifications(getActivity());
		// updateVoicemailNotifications();
		//		}
	}

	private void updateVoicemailNotifications() {
		Intent serviceIntent = new Intent(getActivity(),
				CallLogNotificationsService.class);
		serviceIntent
		.setAction(CallLogNotificationsService.ACTION_UPDATE_NOTIFICATIONS);
		getActivity().startService(serviceIntent);
	}

	// The following lines are provided and maintained by Mediatek Inc.
	private AuroraButton typeFilter_all;
	private AuroraButton typeFilter_outgoing;
	private AuroraButton typeFilter_incoming;
	private AuroraButton typeFilter_missed;

	private static final int SIM_INFO_UPDATE_MESSAGE = 100;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			log("handleMessage msg==== " + msg.what);

			switch (msg.what) {
			case SIM_INFO_UPDATE_MESSAGE:
				if (null != mAdapter) {
					mAdapter.notifyDataSetChanged();
				}
				break;

			case WAIT_CURSOR_START:
				Log.i(TAG, "start WAIT_CURSOR_START !isFinished : "
						+ !isFinished);
				if (!isFinished) {
					// Gionee:wangth 20130326 modify for CR00786566 begin
					if (mNeedShowProgress) {
						mLoadingContainer.setVisibility(View.VISIBLE);
						mProgress.setVisibility(View.VISIBLE);
						mLoadingTextView.setVisibility(View.VISIBLE);
						mNeedShowProgress = false;
					}
					// Gionee:wangth 20130326 modify for CR00786566 end
					mEmptyTitle.setVisibility(View.GONE);
				}
				break;

			default:
				break;
			}
		}
	};

	/**
	 * Called by the CallLogQueryHandler when the list of calls has been
	 * deleted.
	 */
	@Override
	public void onCallsDeleted() {
		log("onCallsDeleted(), do nothing");
	}

	private void changeButton(View view) {
		log("changeButton(), view = " + view);
		if (view != typeFilter_all) {
			typeFilter_all.setBackgroundResource(R.drawable.btn_calllog_all);
		} else {
			typeFilter_all
			.setBackgroundResource(R.drawable.btn_calllog_all_sel);
		}

		if (view != typeFilter_missed) {
			typeFilter_missed
			.setBackgroundResource(R.drawable.btn_calllog_missed);
		} else {
			typeFilter_missed
			.setBackgroundResource(R.drawable.btn_calllog_missed_sel);
		}
	}

	public void onClick(View view) {
		int id = view.getId();
		log("onClick(), view id = " + id);
		SharedPreferences.Editor editor = AuroraPreferenceManager
				.getDefaultSharedPreferences(this.getActivity()).edit();
		switch (id) {
		case R.id.btn_type_filter_all:
			editor.putInt(Constants.TYPE_FILTER_PREF, Constants.FILTER_TYPE_ALL);
			changeButton(view);
			break;
		case R.id.btn_type_filter_outgoing:
			editor.putInt(Constants.TYPE_FILTER_PREF,
					Constants.FILTER_TYPE_OUTGOING);
			changeButton(view);
			break;
		case R.id.btn_type_filter_incoming:
			editor.putInt(Constants.TYPE_FILTER_PREF,
					Constants.FILTER_TYPE_INCOMING);
			changeButton(view);
			break;
		case R.id.btn_type_filter_missed:
			editor.putInt(Constants.TYPE_FILTER_PREF,
					Constants.FILTER_TYPE_MISSED);
			changeButton(view);
			break;



		default:
			break;
		}
		editor.commit();
		refreshData();
	}

	private void log(final String log) {
		Log.i(TAG, log);
	}

	private ProgressBar mProgress;
	// Gionee:wangth 20130326 add for CR00786566 begin
	private View mLoadingContainer;
	private TextView mLoadingTextView;
	private boolean mNeedShowProgress = true;
	// Gionee:wangth 20130326 add for CR00786566 end

	private View mEmptyTitle;
	public static boolean isFinished = false;
	private static final int WAIT_CURSOR_START = 1230;
	private static final long WAIT_CURSOR_DELAY_TIME = 100;

	private boolean mHasPermanentMenuKey;
	final int MATCH_CONTACTS_NUMBER_LENGTH = ContactsApplication.GN_MATCH_CONTACTS_NUMBER_LENGTH;
	private final String NO_CALL_LOG_SELECTED = "";

	private String toDeleteSelection(String number) {
		if (TextUtils.isEmpty(number)) {
			return NO_CALL_LOG_SELECTED;
		}

		int callType = getCurCallType();
		StringBuilder selection = new StringBuilder();
		if (callType > 0) {
			selection.append("type=").append(callType).append(" And ");
		}

		if (!ContactsApplication.sIsGnCombineCalllogMatchNumber
				|| number.length() < MATCH_CONTACTS_NUMBER_LENGTH) {
			selection.append("number ='").append(number).append('\'');
		} else {
			selection.append("number LIKE '%").append(number).append('\'');
		}

		return selection.toString();
	}

	private String toDeleteSelection(ArrayList<String> selected) {
		if (null == selected || selected.size() <= 0) {
			return NO_CALL_LOG_SELECTED;
		}

		int callType = getCurCallType();
		StringBuilder selection = new StringBuilder();
		if (callType > 0) {
			selection.append("type=").append(callType).append(" And ");
		}

		if (!ContactsApplication.sIsGnCombineCalllogMatchNumber) {
			selection.append("number in (");
			for (String number : selected) {
				selection.append("'").append(number).append("',");
			}

			selection.setLength(selection.length() - 1);
			selection.append(")");
		} else {
			selection.append("(").append(Calls.NUMBER).append(" in (");
			int shorterCount = 0;
			for (String number : selected) {
				int numLen = number.length();
				if (numLen < MATCH_CONTACTS_NUMBER_LENGTH) {
					++shorterCount;
					selection.append("'").append(number).append("',");
				}
			}

			if (shorterCount > 0) {
				selection.setLength(selection.length() - 1);
			} else {
				selection.setLength(0);
				selection.append("(");
			}

			if (selected.size() - shorterCount > 0) {
				selection.append(") OR ");

				for (String info : selected) {
					int numLen = info.length();
					if (numLen >= MATCH_CONTACTS_NUMBER_LENGTH) {
						selection
						.append(Calls.NUMBER)
						.append(" LIKE '%")
						.append(info.substring(numLen
								- MATCH_CONTACTS_NUMBER_LENGTH, numLen))
								.append("' OR ");
					}
				}

				selection.setLength(selection.length() - 4);
			}

			selection.append(")");
		}

		return selection.toString();
	}

	private AuroraAlertDialog mClearCallLogDialog;

	protected int getCurFilterType() {
		SharedPreferences prefs = AuroraPreferenceManager
				.getDefaultSharedPreferences(this.getActivity());
		return prefs.getInt(Constants.TYPE_FILTER_PREF,
				Constants.FILTER_TYPE_DEFAULT);
	}

	protected int getCurCallType() {
		return getCallsType(getCurFilterType());
	}

	protected String getTitleByFilterType(int filterType) {
		switch (filterType) {
		case Constants.FILTER_TYPE_ALL:
			return getString(R.string.gn_allcall_tab_label);
		case Constants.FILTER_TYPE_OUTGOING:
			return getString(R.string.gn_outgoingcall_tab_label);
		case Constants.FILTER_TYPE_INCOMING:
			return getString(R.string.gn_incomingcall_tab_label);
		case Constants.FILTER_TYPE_MISSED:
			return getString(R.string.gn_misscall_tab_label);
		default:
			break;
		}
		return null;
	}

	// gionee xuhz 20120517 modify for CR00601200 CR00601202 start
	protected int getCallsType(int filterType) {
		switch (filterType) {
		case Constants.FILTER_TYPE_ALL:
			return 0;
		case Constants.FILTER_TYPE_OUTGOING:
			return Calls.OUTGOING_TYPE;
		case Constants.FILTER_TYPE_INCOMING:
			return Calls.INCOMING_TYPE;
		case Constants.FILTER_TYPE_MISSED:
			return Calls.MISSED_TYPE;
		default:
			return 0;
		}
	}

	// gionee xuhz 20120517 modify for CR00601200 CR00601202 end

	public int getCurTypeCallLogCount() {
		// gionee xuhz 20120919 modify for CR00696560 start
		if (mAdapter != null) {
			return mAdapter.getCount();
		}
		// gionee xuhz 20120919 modify for CR00696560 end
		return 0;
	}

	protected ArrayAdapter<CharSequence> createTitleSpinnerAdapter() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.gn_call_log_type,
				R.layout.gn_simple_spinner_item_wht);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	private int mCurCallTypePosition;

	protected int spinnerPosition2FilterType(int position) {
		mCurCallTypePosition = position;

		switch (position) {
		case 0:
			return Constants.FILTER_TYPE_ALL;
		case 1:
			return Constants.FILTER_TYPE_OUTGOING;
		case 2:
			return Constants.FILTER_TYPE_INCOMING;
		case 3:
			return Constants.FILTER_TYPE_MISSED;
		default:
			return Constants.FILTER_TYPE_ALL;
		}
	}

	private GnContactInfo getContactInfo(int position) {
		if (null != mAdapter) {
			Cursor cursor = mAdapter.getCursor();
			if (null != cursor) {
				int cp = cursor.getPosition();
				//aurora change liguangyu 20131108 for BUG #419 start
				if (cursor.moveToPosition(position)) {
					//aurora change liguangyu 20131108 for BUG #419 end
					GnContactInfo info = mAdapter.getContactInfo(cursor);
					cursor.moveToPosition(cp);
					return info;
				}
			}
		}
		return null;
	}

	// gionee xuhz 20120626 add for CR00626582 start
	protected void changSpinnerPosition2Default() {
		/*
		 * if (null != mCallTypeSpinner) { mCallTypeSpinner.setSelection(0);
		 * return; }
		 */

		changeCalllogType(spinnerPosition2FilterType(0), false);
	}

	// gionee xuhz 20120626 add for CR00626582 end



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		Log.d(TAG, "onItemClick view id:"+view.getId()+" position:"+position);

		RelativeLayout mainUi;
		AuroraCallLogListItemViewV2 cliv = null;

		mainUi = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		if (mainUi != null) {

			View frontView = mainUi.getChildAt(0);
			if (frontView != null && frontView instanceof AuroraCallLogListItemViewV2) {
				cliv = (AuroraCallLogListItemViewV2)frontView;

			}
		}

		//		if(view instanceof AuroraCallLogListItemViewV2){
		//			cliv=(AuroraCallLogListItemViewV2)view;
		//		}


		if (cliv == null) {


			Object obj = null;
			contactInfo = null;
			boolean isCallPrimary = ResConstant.isCallLogListItemCallPrimary();
			boolean isPrimaryClick = false;

			obj = view.getTag();
			isPrimaryClick = true;


			if (null != obj && obj instanceof GnContactInfo) {
				contactInfo = (GnContactInfo) obj;
			}

			if (null == contactInfo) {
				return;
			}

			if (editMode) {


				boolean checked=false;	
				View rootView=(View) view.getParent().getParent().getParent().getParent().getParent().getParent();
				CheckBox checkBox=(CheckBox) rootView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				Log.d(TAG,"cliv == null & editMode:"+checkBox);
				if(checkBox==null) {return;}

				checked = !checkBox
						.isChecked();
				checkBox.setChecked(checked);		


				mAdapter.setCheckedArrayValue(contactInfo.position, checked);
				//				middleTextView.setText("已选择"+getAdapter().getCheckedCount()+"项");
				if(middleTextView!=null){
					middleTextView.setText(context.getString(R.string.selected_total_num, getAdapter().getCheckedCount()));
				}

				if (checked) {
					if (mAdapter.isAllSelect()) {
						updateMenuItemState(true);
					} else {
						setBottomMenuEnable(true);
					}
				} else {
					updateMenuItemState(false);
				}
				return;
			}

			if(view.getId()==R.id.expand||view.getId()==R.id.call_date_ll){//popupWindow
				Log.d(TAG,"popupwindow");
				int[] location = new int[2];

				((View)view.getParent().getParent()).getLocationInWindow(location);	
				int height=location[1]
						+context.getResources().getDimensionPixelOffset(R.dimen.double_list_item_height)/2;
				//				if(view.getId()==R.id.expand){
				//					
				//					height=location[1];
				//				}else{
				//					height=location[1]
				//							+context.getResources().getDimensionPixelOffset(R.dimen.double_list_item_height)/2
				//							-context.getResources().getDimensionPixelOffset(R.dimen.listitem_double_margin_top);
				//				}

				(AuroraDialActivity.auroraDialActivity).setAuroraSystemMenuCallBack(auroraMenuCallBack);
				if(contactInfo.lookupUri==null){
					Log.d(TAG,"isEmpty");
					(AuroraDialActivity.auroraDialActivity).setAuroraMenuItems(R.menu.calllog_expand_with_addcontact);
				}else{
					(AuroraDialActivity.auroraDialActivity).setAuroraMenuItems(R.menu.calllog_expand);
				}				

				(AuroraDialActivity.auroraDialActivity).showAuroraMenu(view, Gravity.TOP | Gravity.RIGHT,0
						/*context.getResources().getDimensionPixelOffset(R.dimen.calllog_popupwindow_margin_right)*/, 
						height);


				//				View contentView = View.inflate(context,
				//						R.layout.popupwindow, null);
				//
				//				pop_detail = (LinearLayout) contentView
				//						.findViewById(R.id.pop_detail);
				//				pop_sendsms = (LinearLayout) contentView
				//						.findViewById(R.id.pop_sendsms);
				//				pop_copy = (LinearLayout) contentView
				//						.findViewById(R.id.pop_copy);
				//				pop_delete = (LinearLayout) contentView
				//						.findViewById(R.id.pop_delete);
				//
				//				pop_detail.setOnClickListener(AuroraCallLogFragmentV2.this);
				//				pop_sendsms.setOnClickListener(AuroraCallLogFragmentV2.this);
				//				pop_copy.setOnClickListener(AuroraCallLogFragmentV2.this);
				//				pop_delete.setOnClickListener(AuroraCallLogFragmentV2.this);
				//
				//				LinearLayout ll_popup_container = (LinearLayout) contentView
				//						.findViewById(R.id.ll_popup_container);
				//
				//				ScaleAnimation sa = new ScaleAnimation( 0.0f,
				//						1.0f,
				//						0.0f,
				//						1.0f,
				//						Animation.RELATIVE_TO_SELF, 
				//						1.0f, 
				//						Animation.RELATIVE_TO_SELF, 
				//						0.0f);
				//				sa.setDuration(100);				
				//
				//
				//				DisplayMetrics dm = new DisplayMetrics();
				//				getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				//				int screenW = dm.widthPixels;// 获取分辨率宽度
				//
				//				popupWindow = new PopupWindow(contentView, DensityUtil.dip2px(context, 130), 
				//						DensityUtil.dip2px(context, 208));
				//				popupWindow.setBackgroundDrawable(new ColorDrawable(
				//						Color.TRANSPARENT));
				//
				//				int[] location = new int[2];
				//				view.getLocationInWindow(location);
				//				Log.d(TAG, "location[0]"+location[0]
				//						+"\nlocation[1]:"+location[1]
				//								+"\nDensityUtil.dip2px(context, 146):"+DensityUtil.dip2px(context, 146)
				//								+"\nDensityUtil.dip2px(context, 42):"+DensityUtil.dip2px(context, 42)
				//								+"\nscreenW:"+screenW
				//						);
				//				popupWindow.showAtLocation(view, Gravity.TOP | Gravity.LEFT,
				//						screenW-DensityUtil.dip2px(context, 146), location[1]+DensityUtil.dip2px(context, 42));
				//
				//				ll_popup_container.startAnimation(sa);

				return;
			}

			if(ContactsApplication.sIsAuroraYuloreSupport){
				Log.v(TAG, "cliv == null2");
				if(YuloreUtils.getInstance(getActivity()).getName(contactInfo.number)!=null){
					YuloreUtils.getInstance(context).startActivity(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP, "yulore.view", null, "yulore://viewDetail?tel="+contactInfo.number+"&title=通话详情", "yulore/detail_call", null, null);
				}else{
					Log.v(TAG, "cliv == null3");
					// detail
					int filterType = getCurFilterType();
					int callsType = getCallsType(filterType);
					// aurora changes zhouxiaobing 20130925
					/*
					 * Intent intent =
					 * IntentFactory.newShowCallDetailIntent(context,
					 * contactInfo.number, callsType, contactInfo.callId,
					 * contactInfo.gnCallsCount, contactInfo.voicemailUri);
					 */
					if(contactInfo.lookupUri==null){
						Log.d(TAG,"isEmpty");
						Intent intent = IntentFactory.newShowCallDetailIntent(context,
								contactInfo.number, callsType, contactInfo.callId,
								contactInfo.gnCallsCount, contactInfo.voicemailUri,
								contactInfo.ids);
						intent.putExtra("contact_sim_id", contactInfo.contactSimId);
						// aurora changes zhouxiaobing 20130925

						context.startActivity(intent);
					}else{
						Log.d(TAG,"not isEmpty");
						Intent intent = IntentFactory.newViewContactIntent(contactInfo.lookupUri);
						context.startActivity(intent);
					}
				}
			}else{
				Log.v(TAG, "cliv == null4");
				// detail
				int filterType = getCurFilterType();
				int callsType = getCallsType(filterType);
				// aurora changes zhouxiaobing 20130925
				/*
				 * Intent intent =
				 * IntentFactory.newShowCallDetailIntent(context,
				 * contactInfo.number, callsType, contactInfo.callId,
				 * contactInfo.gnCallsCount, contactInfo.voicemailUri);
				 */
				if(contactInfo.lookupUri==null){
					Log.d(TAG,"isEmpty");
					Intent intent = IntentFactory.newShowCallDetailIntent(context,
							contactInfo.number, callsType, contactInfo.callId,
							contactInfo.gnCallsCount, contactInfo.voicemailUri,
							contactInfo.ids);
					intent.putExtra("contact_sim_id", contactInfo.contactSimId);
					// aurora changes zhouxiaobing 20130925

					context.startActivity(intent);
				}else{
					Log.d(TAG,"not isEmpty");
					Intent intent = IntentFactory.newViewContactIntent(contactInfo.lookupUri);
					context.startActivity(intent);
				}

			}
		} else if (cliv != null) {
			Log.v(TAG, "auroraOnItemClick 1position=" + position);
			// aurora changes zhouxiaobing 20130912
			// aurora <ukiliu> <2013-9-27> modify for aurora ui begin
			if (editMode) {
				if(middleTextView==null) return;
				boolean checked = cliv.mCheckBox
						.isChecked();
				//				cliv.mCheckBox
				//						.setChecked(!checked);
				cliv.mCheckBox.setChecked(!checked);
				mAdapter.setCheckedArrayValue(position, !checked);

				if(middleTextView!=null){
					middleTextView.setText(context.getString(R.string.selected_total_num, getAdapter().getCheckedCount()));
				}
				if (!checked) {
					if (mAdapter.isAllSelect()) {
						updateMenuItemState(true);
					} else {
						setBottomMenuEnable(true);
					}
				} else {
					updateMenuItemState(false);
				}
				return;
			}

			//aurora add liguangyu 20140613 for BUG #5519 start
			if (isFastClick()) {   
				Log.d(TAG, "isfastclick");
				return;
			}
			//aurora add liguangyu 20140613 for BUG #5519 end

			Log.v(TAG, "auroraOnItemClick 2position=" + position);
			Context context = this.getActivity();
			GnContactInfo contactInfo = null;
			boolean isCallPrimary = ResConstant.isCallLogListItemCallPrimary();
			boolean isPrimaryClick = false;
			{
				Object obj = null;

				obj = cliv.contactPhoto
						.getTag();
				isPrimaryClick = true;


				if (null != obj && obj instanceof GnContactInfo) {
					contactInfo = (GnContactInfo) obj;
				}

				if (null == contactInfo) {
					return;
				}
			}

			if (null != contactInfo) {
				Log.d(TAG, "onitemclick2,isPrimary:"+isPrimaryClick+" isCallprimary:"+isCallPrimary);
				// if ((isPrimaryClick && isCallPrimary) || ((!isPrimaryClick &&
				// !isCallPrimary))) {
				if (/*!((isPrimaryClick && isCallPrimary) || ((!isPrimaryClick && !isCallPrimary)))*/true) {// aurora
					// change
					// zhouxiaobing
					Log.d(TAG, "onitemclick3");
					//aurora change liguangyu 20140305 for bug #2805 start
					int number_id = 0;
					if ("-1".equals(contactInfo.number)) {
						number_id = R.string.unknown;
					} else if ("-2".equals(contactInfo.number)) {
						number_id = R.string.private_num;
					} else if ("-3".equals(contactInfo.number)) {
						number_id = R.string.payphone;
					}
					// Gionee <huangzy> <2013-06-09> add for CR00822712 begin
					//					if ("-1".equals(contactInfo.number)) {
					if(number_id != 0) {
						Toast.makeText(context,
								//								context.getString(R.string.gn_hidden_number),
								context.getString(number_id),								
								Toast.LENGTH_SHORT).show();
						return;
					}
					// Gionee <huangzy> <2013-06-09> add for CR00822712 end
					//aurora change liguangyu 20140305 for bug #2805 end

					// call
					if (ContactsApplication.sIsGnDualSimSelectSupport) {
						context.startActivity(IntentFactory
								.newGnDualSimSelectIntent(cliv, contactInfo.number));
					} else {
						//						if(simInfoId.contains(Long.parseLong(String.valueOf(contactInfo.simId)))){
						//aurora change liguangyu 20131206 start
						Intent intent = IntentFactory
								.newDialNumberIntent(contactInfo.number);
						intent.putExtra("contactUri", contactInfo.lookupUri);
						if(ContactsApplication.isMultiSimEnabled) {
							int slot = SIMInfoWrapper.getDefault().getInsertedSimSlotById(contactInfo.simId);
							if(!(slot==0||slot==1))
							{
								if(GnITelephony.isSimInsert(null,1))
								{
									slot=1;
								}
								else
								{
									slot=0;
								}
							}
							intent.putExtra(ContactsUtils.getSlotExtraKey(), slot);
						}
						context.startActivity(intent);
						//aurora change liguangyu 20131206 end
						//						}
						//						
						//						
						//						else{
						//							final View customView = LayoutInflater.from(context).inflate(R.layout.choosesimdialog, null); 
						//							AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(context);
						//							builder.setTitle(R.string.choose_sim_title);  
						//							builder.setView(customView);  				
						//
						//							builder.create().show();
						//
						//						}


					}
				} else {
					Log.d(TAG, "onitemclick4");
					// detail
					int filterType = getCurFilterType();
					int callsType = getCallsType(filterType);
					// aurora changes zhouxiaobing 20130925
					/*
					 * Intent intent =
					 * IntentFactory.newShowCallDetailIntent(context,
					 * contactInfo.number, callsType, contactInfo.callId,
					 * contactInfo.gnCallsCount, contactInfo.voicemailUri);
					 */
					if(contactInfo.lookupUri==null){
						Log.d(TAG,"isEmpty");
						Intent intent = IntentFactory.newShowCallDetailIntent(context,
								contactInfo.number, callsType, contactInfo.callId,
								contactInfo.gnCallsCount, contactInfo.voicemailUri,
								contactInfo.ids);
						intent.putExtra("contact_sim_id", contactInfo.contactSimId);
						// aurora changes zhouxiaobing 20130925

						context.startActivity(intent);
					}else{
						Log.d(TAG,"not isEmpty");
						Intent intent = IntentFactory.newViewContactIntent(contactInfo.lookupUri);
						context.startActivity(intent);
					}
				}
			}

		}

	}


	private long lastClickTime; 
	public boolean isFastClick() { 
		long time = System.currentTimeMillis(); 
		long timeD = time - lastClickTime; 
		if ( 0 < timeD && timeD < 800) {    
			return true;    
		}    
		lastClickTime = time;    
		return false;    
	} 

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		AuroraCallLogAdapterV2.startPosition=mListView.getLastVisiblePosition();

		/*// aurora <ukiliu> <2013-9-27> modify for aurora ui begin
		if (!isLongClickEnable) {
			return true;
		}
		//aurora add liguangyu 20131107 start
		//		RelativeLayout mainUi;
		AuroraCallLogListItemViewV2 cliv = null;
		//		mainUi = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		//		if (mainUi != null) {
		//			View frontView = mainUi.getChildAt(0);
		//			if (frontView != null && frontView instanceof AuroraCallLogListItemViewV2) {
		//				cliv = (AuroraCallLogListItemViewV2)frontView;
		//			}
		//		}

		if(view instanceof AuroraCallLogListItemViewV2){
			cliv=(AuroraCallLogListItemViewV2)view;
		}


		if(cliv != null) {
			Log.d(TAG,"onItemLongClick cliv != null");

			boolean checked = false;
			Log.d(TAG,"checkbox0:"+cliv.mCheckBox);
			//			cliv.mCheckBox.setChecked(!checked);
			cliv.mCheckBox.setChecked(!checked);
			mAdapter.setCheckedArrayValue(position, !checked);			
			if (mAdapter.isAllSelect()) {
				updateMenuItemState(true);
			}			
		}

		else{
			Log.d(TAG,"onItemLongClick cliv = null");
			View rl=null;
			if(view.getId()==R.id.contact_photo_rl){
				rl=(LinearLayout)view.getParent();
			}else if(view.getId()==R.id.contact_expand_layout){
				rl=(RelativeLayout)view.getParent().getParent();
			}
			if(rl==null) {return false;}
			CheckBox checkBox=(CheckBox)(rl.findViewById(R.id.aurora_list_item_check_box));
			Log.d(TAG,"checkbox1:"+checkBox);
			boolean checked = false;
			checkBox.setChecked(!checked);
			mAdapter.setCheckedArrayValue(position, !checked);			
			if (mAdapter.isAllSelect()) {
				updateMenuItemState(true);
			}	
		}


		//aurora add liguangyu 20131107 start
		switch2EditMode();
		setBottomMenuEnable(true);
		// aurora <ukiliu> <2013-9-27> modify for aurora ui end

		return true;*/

		Log.d(TAG,"onItemLongClick view:"+view+" getId():"+view.getId()+" position:"+position);
		if (!isLongClickEnable) {
			return false;
		}

		//aurora add liguangyu 20131107 start
		RelativeLayout mainUi;
		AuroraCallLogListItemViewV2 cliv = null;
		mainUi = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		if (mainUi != null) {
			View frontView = mainUi.getChildAt(0);
			if (frontView != null && frontView instanceof AuroraCallLogListItemViewV2) {
				cliv = (AuroraCallLogListItemViewV2)frontView;
			}
		}
		if(cliv != null) {
			boolean checked = false;
			Log.d(TAG,"test888");
			cliv.mCheckBox.setChecked(!checked);
			//			cliv.mCheckBox.auroraSetChecked(!checked, true);
			mAdapter.setCheckedArrayValue(position, !checked);			
			if (mAdapter.isAllSelect()) {
				updateMenuItemState(true);
			}			
		}else{
			View rootView=(View) view.getParent().getParent().getParent().getParent().getParent().getParent();
			CheckBox checkBox=(CheckBox) rootView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			if(checkBox==null) {return false;}

			boolean checked = false;
			Log.d(TAG,"test999");
			checkBox.setChecked(!checked);
			//			cliv.mCheckBox.auroraSetChecked(!checked, true);
			mAdapter.setCheckedArrayValue(position, !checked);			
			if (mAdapter.isAllSelect()) {
				updateMenuItemState(true);
			}	
		}
		//aurora add liguangyu 20131107 start
		switch2EditMode();
		setBottomMenuEnable(true);
		// aurora <ukiliu> <2013-9-27> modify for aurora ui end

		return false;
	}
	// aurora <ukiliu> <2013-9-27> add for aurora ui begin
	public void switch2NormalMode() {
		(AuroraDialActivity.auroraDialActivity).setMenuEnable(true);
		((AuroraListView)getListView()).auroraSetNeedSlideDelete(false);
		//		mListView.auroraEnableSelector(true);
		isLongClickEnable = true;
		Log.i(TAG,"mAdapter.getCount() == "+mAdapter.getCount());
		setEditMode(false);


		if(mCallbacks!=null){
			mCallbacks.onFragmentCallback(FragmentCallbacks.SWITCH_TO_NORMAL_MODE,null);
		}
	}

	public void switch2EditMode() {
		(AuroraDialActivity.auroraDialActivity).setMenuEnable(false);	
		//aurora change liguangyu 20131108 for BUG #511 start
		try {
			boolean deleteIsShow = mListView.auroraIsRubbishOut();
			if (deleteIsShow) {
				getAuroraListView().auroraSetRubbishBack();
				mHandler.postDelayed(new Runnable() {
					public void run() {   
						isLongClickEnable = false;
						initActionBar(true);
						setEditMode(true);
						mListView.auroraSetNeedSlideDelete(false);
						mListView.auroraEnableSelector(false);


					}
				}, deleteIsShow ? 450 : 0); 
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		isLongClickEnable = false;

		if(mCallbacks!=null){
			mCallbacks.onFragmentCallback(FragmentCallbacks.SWITCH_TO_EDIT_MODE,null);
		}


		initActionBar(true);
		setEditMode(true);
		mListView.auroraSetNeedSlideDelete(false);
		mListView.auroraEnableSelector(false);
		//aurora change liguangyu 20131108 for BUG #511 end

	}
	// aurora <ukiliu> <2013-9-27> add for aurora ui end

	// aurora <ukiliu> <2013-9-27> add for aurora ui begin
	public void onSelectAll(boolean check) {
		AuroraCallLogAdapterV2 mAdapter = getAdapter();
		if (mAdapter != null && mAdapter.getCount() > 0) {
			//			mAdapter.setAllSelect(check);
			mAdapter.createCheckedArray(mAdapter.getRealCount());

			mAdapter.setAllSelect(check);
			//aurora add liguangyu 20140331 for bug 3948 start
			mAdapter.setAllSelectFlag(true);
			//aurora add liguangyu 20140331 for bug 3948 end
			mAdapter.notifyDataSetChanged();
			if(middleTextView!=null){
				middleTextView.setText(context.getString(R.string.selected_total_num, getAdapter().getCheckedCount()));
			}
		}
	}
	// aurora <ukiliu> <2013-9-27> add for aurora ui end
	public void deleteAllCalllogs(){
		new AsyncTask<Integer, Integer, Integer>() {
			private Context mContext = getActivity();
			private AuroraProgressDialog mProgressDialog;

			protected void onPreExecute() {
				// Gionee <wangth><2013-04-16> modify for CR00796966 begin

				if (getActivity() != null && !getActivity().isFinishing()) {
					mProgressDialog = AuroraProgressDialog.show(mContext, "",
							getString(R.string.deleting_call_log));
				}
				// Gionee <wangth><2013-04-16> modify for CR00796966 end
			};

			@Override
			protected Integer doInBackground(Integer... params) {
				String privateString = null;
				if (ContactsApplication.sIsAuroraPrivacySupport) {
					if(mIsPrivate) {
						privateString = " privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId();
					} else {
						privateString = " privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId() + " or privacy_id = 0";
					}
				} 
				mContext.getContentResolver().delete(Calls.CONTENT_URI,
						privateString, null);
				return null;
			}

			protected void onPostExecute(Integer result) {
				// Gionee <wangth><2013-04-16> modify for CR00796966 begin
				if (getActivity() != null && !getActivity().isFinishing()) {
					mProgressDialog.dismiss();

					switch2NormalMode();
					initActionBar(false);
				}
				// Gionee <wangth><2013-04-16> modify for CR00796966 end
			};
		}.executeOnExecutor(exec);
	}

	//aurora add liguangyu 20131108 for BUG #508 start
	private AuroraListView mListView;
	public AuroraListView getAuroraListView() {
		return mListView;
	}
	//aurora add liguangyu 20131108 for BUG #508 end

	//aurora add liguangyu 20131113 for start
	//    public interface updateActionBarListener {
	//        void updateActionBar();
	//    }

	//    private updateActionBarListener mUpdateActionBarlistener = null;

	//    public void onAttach(Activity activity) {
	//        super.onAttach(activity);
	//        try {
	//        	mUpdateActionBarlistener = (updateActionBarListener) activity;
	//         } catch (ClassCastException e) {
	//            throw new ClassCastException(activity.toString() + " must implement mUpdateActionBarlistener");
	//        }
	//    }
	//aurora add liguangyu 20131113 for end

	public String getCallLogCheckedNumber() {
		ArrayList<Integer> als = new ArrayList<Integer>();
		int length = mAdapter.getRealCount();
		Log.d("TAG","all length::"+(length));
		for (int i = 0; i < length; i++) {
			if (mAdapter.getCheckedArrayValue(i)) {
				Cursor cursor = (Cursor) mAdapter.getItem(i);
				GnContactInfo contactInfo = mAdapter.getContactInfo(cursor);
				return contactInfo.number;
			}
		}
		return "";
	}

	private ContactChangeObserver mContactChangeObserver;

	private class ContactChangeObserver extends ContentObserver {
		public ContactChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.i(TAG, "Observer onChange");
			if (getActivity() == null || getActivity().isFinishing() || editMode) {
				return;
			}

			mAdapter.simInfoId.clear();
			List<SIMInfo> simInfo= SIMInfoWrapper.getDefault().getInsertedSimInfoList();
			for(SIMInfo info:simInfo){
				Log.d(TAG,"info.simId:"+info.mSimId+" info.simslot:"+info.mSlot);
				mAdapter.simInfoId.add(info.mSimId);
			}

			mHandler.removeCallbacks(mRunnable);
			mHandler.postDelayed(mRunnable, 500);
		}
	}

	private Runnable mRunnable=new Runnable(){    
		public void run() {    
			refreshData();
		} 
	};

	private Runnable mfetchCallsRunnable=new Runnable(){    
		public void run() {    
			Log.d(TAG, "mfetchCallsRunnable run");
			SharedPreferences prefs = AuroraPreferenceManager
					.getDefaultSharedPreferences(context);
			int simFilter = prefs.getInt(Constants.SIM_FILTER_PREF,
					Constants.FILTER_SIM_DEFAULT);
			int typeFilter = prefs.getInt(Constants.TYPE_FILTER_PREF,
					Constants.FILTER_TYPE_DEFAULT);
			fetchCallsJionDataView(simFilter, typeFilter, mAdapter);
		} 
	};


	//	public View mFooterView;

	public void animationAfterSetTab() {
		Animation in =AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_in);
		//		this.getContentView().startAnimation(in);
		(AuroraDialActivity.auroraDialActivity).getAuroraActionBar().startAnimation(in);
		Animation viewIn =AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_in);
		viewIn.setFillAfter(true);
		getView().startAnimation(viewIn);
		mListView.startLayoutAnimation();
	}

	public void animationBeforeSetTab() {
		Animation out =AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_out);
		Animation viewOut =AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_out);
		(AuroraDialActivity.auroraDialActivity).getAuroraActionBar().startAnimation(out);
		viewOut.setFillAfter(true);
		getView().startAnimation(viewOut);
	}

	//aurora add liguangyu 20140923 for #8487 start
	private class InitHotProviderTask extends AsyncTask<Cursor, Void, Boolean> {
		Cursor mLocalCursor = null;

		@Override
		protected Boolean doInBackground(Cursor... cursor) {
			if(cursor != null) {
				//				if (mIsPrivate) {
				//	                mLocalCursor = trimPrivateCursor(cursor[0]);
				//				} else {
				mLocalCursor = cursor[0];
				//				}

			}
			Activity activity = getActivity();
			if(mAdapter.getHotlinesName() == null && activity != null && !activity.isFinishing()) {
				Cursor hotlinesCursor = activity.getContentResolver().query(GnHotLinesUtil.HOT_LINES_URI, 
						new String[]{T9SearchRetColumns.NAME, T9SearchRetColumns.NUMBER},
						null, null, T9SearchRetColumns.ID + " LIMIT 1");
				String[] hotnumbers=null;
				String[] hotnames=null ;
				if (hotlinesCursor != null) {
					if (hotlinesCursor.moveToFirst()) {
						int hotcount = hotlinesCursor.getCount();
						mAdapter.creatHotlinesNumber(hotcount);
						hotnames = mAdapter.getHotlinesName();
						hotnumbers = mAdapter.getHotlinesNumber();
						int index = 0;
						hotnames[index] = hotlinesCursor.getString(0);
						hotnumbers[index++] = hotlinesCursor.getString(1);
						while (hotlinesCursor.moveToNext()) {
							hotnames[index] = hotlinesCursor.getString(0);
							hotnumbers[index++] = hotlinesCursor.getString(1);
						}
					}
					hotlinesCursor.close();
					hotlinesCursor = null;
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.v(TAG, "onPostExecute = " + result);
			mAdapter.setLoading(false);
			mAdapter.changeCursor(mLocalCursor);
			mAdapter.createCheckedArray(mAdapter.getRealCount());
			changecursor(mLocalCursor);    	

			if (mScrollToTop) {
				final AuroraListView listView = (AuroraListView) getListView();
				if (listView.getFirstVisiblePosition() > 5) {
					listView.setSelection(5);
				}
				listView.smoothScrollToPosition(0);
				mScrollToTop = false;
			}
			mCallLogFetched = true;

			Log.i(TAG, "onCallsFetched is call");
			isFinished = true;
			// Gionee:wangth 20130326 modify for CR00786566 begin
			mLoadingContainer.setVisibility(View.GONE);
			mProgress.setVisibility(View.GONE);
			mLoadingTextView.setVisibility(View.GONE);
			if (mAdapter.getCount() <= 0) {
				//				mEmptyTitle.setText(getCurCallTypeEmptyMsg());
				mEmptyTitle.setVisibility(View.VISIBLE);
			} else {
				mEmptyTitle.setVisibility(View.GONE);
			}
			// Gionee:wangth 20130326 modify for CR00786566 end

			destroyEmptyLoaderIfAllDataFetched();
			//aurora add qiaohu 20141203 for #10247 start 
			if(!isLongClickEnable){
				updateUi();
			}
			//aurora add qiaohu 20141203 for #10247 end
		}
	}
	//aurora add liguangyu 20140923 for #8487 end

	private Cursor trimPrivateCursor(Cursor src) {
		if((src == null) || (src.getCount() <= 0)) {
			return src;
		}

		MatrixCursor retCursor = new MatrixCursor(CallLogQuery.GN_PROJECTION_CALLS_JOIN_DATAVIEW);
		final int len = CallLogQuery.GN_PROJECTION_CALLS_JOIN_DATAVIEW.length;
		Object[] raw = new Object[len];
		final long currentPrivateId = AuroraPrivacyUtils.getCurrentAccountId();
		src.moveToFirst();
		do { 
			boolean isAdd = true;
			long privateId = src.getLong(0x19);	        

			if(mIsPrivate) {
				isAdd = privateId == currentPrivateId;
			} else {		        
				if(privateId > 0) {
					if(privateId != currentPrivateId) {
						isAdd = false;
					}
				}
			}
			if(isAdd) {
				raw[0x0] = Long.valueOf(src.getLong(0x0));
				raw[0x1] = src.getString(0x1);
				raw[0x2] = Long.valueOf(src.getLong(0x2));
				raw[0x3] = Long.valueOf(src.getLong(0x3));
				raw[0x4] = Integer.valueOf(src.getInt(0x4));
				raw[0x5] = src.getString(0x5);
				raw[0x6] = src.getString(0x6);
				raw[0x7] = src.getString(0x7);
				raw[0x8] = Integer.valueOf(src.getInt(0x8));
				raw[0x9] = Integer.valueOf(src.getInt(0x9));
				raw[0xa] = Integer.valueOf(src.getInt(0xa));
				raw[0xb] = Long.valueOf(src.getLong(0xb));
				raw[0xc] = Long.valueOf(src.getLong(0xc));
				raw[0xd] = src.getString(0xd);
				raw[0xe] = src.getString(0xe);
				raw[0xf] = Integer.valueOf(src.getInt(0xf));
				raw[0x10] = Long.valueOf(src.getLong(0x10));
				raw[0x11] = Integer.valueOf(src.getInt(0x11));
				raw[0x12] = Long.valueOf(src.getLong(0x12));
				raw[0x13] = src.getString(0x13);
				raw[0x14] = src.getString(0x14);
				raw[0x15] = src.getString(0x15);
				raw[0x16] = Integer.valueOf(src.getInt(0x16));
				raw[0x17] = src.getString(0x17);
				raw[0x18] = Integer.valueOf(src.getInt(0x18));
				raw[0x19] = Long.valueOf(src.getLong(0x19));
				raw[0x1a] = Integer.valueOf(src.getInt(0x1a));
				raw[0x1b] = src.getString(0x1b);
				retCursor.addRow(raw);
			}

		} while(src.moveToNext());           

		src.close();
		src = null;

		if(getActivity() != null) {
			retCursor.setNotificationUri(getActivity().getContentResolver(), GnCallLog.CONTENT_URI);
		}
		return retCursor;

	}

	private boolean mIsPrivate = false;

	//aurora add liguangyu 20150127 for #11359 start
	void fetchCallsJionDataView(int simFilter, int typeFilter,AuroraCallLogAdapterV2 adapter) {
		Log.d(TAG, "fetchCallsJionDataView,,");
		mCallLogQueryHandler.fetchCallsJionDataView(simFilter, typeFilter, adapter, mIsPrivate);
	}
	//aurora add liguangyu 20150127 for #11359 end

	public void setPrivate(boolean isPrivate) {
		mIsPrivate = isPrivate;
	}
}
