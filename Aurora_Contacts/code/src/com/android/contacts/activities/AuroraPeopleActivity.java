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

package com.android.contacts.activities;


//import gionee.app.GnStatusBarManager;


import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.interactions.PhoneNumberInteraction;
//import com.android.contacts.list.AuroraDefaultContactBrowseListFragment.IAuroraContactsFragment;
import com.android.contacts.list.ContactBrowseListFragment;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.list.ContactTileAdapter.DisplayType;
import com.android.contacts.list.ContactTileFrequentFragment;
import com.android.contacts.list.ContactTileListFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.ContactsUnavailableFragment;
import com.android.contacts.list.AuroraDefaultContactBrowseListFragment;
import com.android.contacts.list.DirectoryListLoader;
import com.android.contacts.list.OnContactBrowserActionListener;
import com.android.contacts.list.OnContactsUnavailableActionListener;
import com.android.contacts.list.ProviderStatusLoader;
import com.android.contacts.list.ProviderStatusLoader.ProviderStatusListener;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.AccountPromptUtils;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.AccountsListAdapter;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DensityUtil;
import com.android.contacts.util.DialogManager;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.StatisticsUtil;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.AutoScrollListView;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;

import com.mediatek.contacts.list.service.MultiChoiceRequest;
import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.android.contacts.activities.AuroraContactImportExportActivity;
import com.gionee.android.contacts.SimContactsService;
import com.android.contacts.activities.AuroraSimContactListActivity;
import com.android.contacts.model.AccountType;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;
import aurora.preference.AuroraPreferenceActivity;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Intents;
import gionee.provider.GnContactsContract.ProviderStatus;
import gionee.provider.GnContactsContract.QuickContact;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.content.ContentProviderOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.app.StatusBarManager;


import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;

import com.google.android.collect.Lists;

import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;
import aurora.app.AuroraActivity;
/**
 * AURORA::add::wangth::20130902
 */
public class AuroraPeopleActivity extends Activity {

	private static final String TAG = "liyang-AuroraPeopleActivity";


	/**
	 * Showing a list of Contacts. Also used for showing search results in search mode.
	 */
	public AuroraDefaultContactBrowseListFragment mAllFragment;

//	private ImageView mGnAudioSearchView;

	/**
	 * True if this activity instance is a re-created one.  i.e. set true after orientation change.
	 * This is set in {@link #onCreate} for later use in {@link #onStart}.
	 */
//	private boolean mIsRecreatedInstance;

	/**
	 * If {@link #configureFragments(boolean)} is already called.  Used to avoid calling it twice
	 * in {@link #onStart}.
	 * (This initialization only needs to be done once in onStart() when the Activity was just
	 * created from scratch -- i.e. onCreate() was just called)
	 */
//	private boolean mFragmentInitialized;


//	public static Context mContext;
//	private AuroraActionBar mActionBar;


	
	
	
	
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		Log.d(TAG, "onCreate");
		


//		mContext=AuroraPeopleActivity.this;


//		setAuroraContentView(R.layout.aurora_default_contacts_browse_list_frg,
//				AuroraActionBar.Type.Empty);
//		getAuroraActionBar().setVisibility(View.GONE);
		setContentView(R.layout.aurora_default_contacts_browse_list_frg);


//		mActionBar = getAuroraActionBar();

//		getAuroraActionBar().hide();
		
//		mActionBar=AuroraDialActivity.actionBar;

		mAllFragment = (AuroraDefaultContactBrowseListFragment)getFragmentManager().
				findFragmentById(R.id.aurora_default_contact_browse_list_frg);

		if (!mAllFragment.processIntent(false)) {
			finish();
			return;
		}
//		mAllFragment.setIAuroraContactsFragment(myIAuroraContactsFragment);

		if (ContactsApplication.sIsAuroraPrivacySupport) {
			ContactsApplication.mPrivacyActivityList.add(this);
		}

//		mIsRecreatedInstance = (savedState != null);

		//        if (FeatureOption.MTK_GEMINI_SUPPORT) {
			//            mReceiver = new SimIndicatorBroadcastReceiver();
		//            IntentFilter intentFilter =
		//                new IntentFilter(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
		//            registerReceiver(mReceiver, intentFilter);   
		//        }
		
		
	}

//	private IAuroraContactsFragment myIAuroraContactsFragment = new IAuroraContactsFragment() {
//
//		@Override
//		public void setTabWidget(boolean flag) {
//			try {
//				AuroraDialActivityV1 activityparent;
//				activityparent = (AuroraDialActivityV1) getParent();
//
//				if (activityparent != null) {
//					if (flag) {
//						activityparent.setTabWidgetVisible(View.VISIBLE);
//					} else {
//						activityparent.setTabWidgetVisible(View.INVISIBLE);
//					}
//					mAllFragment.setFooterViewVisibility(flag ? View.VISIBLE : View.GONE);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	};

	/*@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		if (!mAllFragment.processIntent(true)) {
			finish();
			return;
		}

		mAllFragment.setCurrentFilterIsValid(true);
	}

	@Override
	protected void onPause() {

		//        if (FeatureOption.MTK_GEMINI_SUPPORT) {
			//            Log.i(TAG, "onPause, setSimIndicatorVisibility ");
		//            setSimIndicatorVisibility(false);
		//            mShowSimIndicator = false;
		//        }

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");

		//        if (FeatureOption.MTK_GEMINI_SUPPORT) {
		//            Log.i(TAG, "onResume, setSimIndicatorVisibility ");
		//            setSimIndicatorVisibility(true);
		//            mShowSimIndicator = true;
		//        }

	}

	@Override
	protected void onStop() {
		Log.d(TAG,"onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy");
		// Some of variables will be null if this Activity redirects Intent.
		// See also onCreate() or other methods called during the Activity's initialization.
		//        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
		//            if (mReceiver != null) {
		//                unregisterReceiver(mReceiver);
		//                mReceiver = null;
		//            }
		//        }
		super.onDestroy();
		if (ContactsApplication.sIsAuroraPrivacySupport) {
			ContactsApplication.mPrivacyActivityList.remove(this);
		}
	}

	public void startActivityAndForwardResult(final Intent intent) {
		intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

		// Forward extras to the new activity
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			intent.putExtras(extras);
		}
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO move to the fragment
		Log.d(TAG,"onkeydown000");
		switch (keyCode) {
		
		case KeyEvent.KEYCODE_MENU: {
//			if (mAllFragment.mSearchViewHasFocus) {
//				return false;

//			if (mAllFragment.getRemoveMemberMode()) {
//				return false;
//			}

		}

		case KeyEvent.KEYCODE_BACK: {
			try {
				boolean deleteIsShow = mAllFragment.getListView().auroraIsRubbishOut();
				if (deleteIsShow) {
					mAllFragment.getListView().auroraSetRubbishBack();
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (mActionBar != null && 
					(mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
				return true;
			}

			if (mAllFragment.getRemoveMemberMode()) {
				try {
					Thread.sleep(300);

					if (isSearchviewLayoutShow()) {
						hideSearchviewLayout();

						mActionBar.setShowBottomBarMenu(true);
						//                        mActionBar.showActionBarDashBoard();
						mActionBar.showActionBottomeBarMenu();
					} else {
						mActionBar.setShowBottomBarMenu(false);
						mActionBar.showActionBarDashBoard();
						mAllFragment.changeToNormalMode(true);
					}

					mAllFragment.getListView().auroraSetNeedSlideDelete(false);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			break;
		}

		case KeyEvent.KEYCODE_DEL: {
			if (deleteSelection()) {
				return true;
			}
			break;
		}

		default: {
			// Bring up the search UI if the user starts typing
			final int unicodeChar = event.getUnicodeChar();
			if (unicodeChar != 0 && !Character.isWhitespace(unicodeChar)) {
				String query = new String(new int[] { unicodeChar }, 0, 1);
			}
		}
		}

		//        if(keyCode==KeyEvent.KEYCODE_BACK){
		//        	 moveTaskToBack(true);
		//        }

		Log.d(TAG,"onkeydown0");
		return super.onKeyDown(keyCode, event);
	}

	private boolean deleteSelection() {
		// TODO move to the fragment
		return false;
	}

	// Visible for testing
	public ContactBrowseListFragment getListFragment() {
		return mAllFragment;
	}*/

	// New feature for SimIndicator begin
	//    private StatusBarManager mStatusBarMgr = null;
	//    private boolean mShowSimIndicator = false;
	//    private SimIndicatorBroadcastReceiver mReceiver = null;
	//
	//    private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
	//        public void onReceive(Context context, Intent intent) {
	//            String action = intent.getAction();
	//            if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
	//                Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive ");
	//                if (action.equals(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED)) {
	//                    Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive, mShowSimIndicator= "
	//                            + mShowSimIndicator);
	//                    if (true == mShowSimIndicator) {
	//                        setSimIndicatorVisibility(true);
	//                    }
	//                }
	//            }
	//        }
	//    }
	//
	//    void setSimIndicatorVisibility(boolean visible) {
	//        if(mStatusBarMgr == null)
	//            mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
	//        if (visible)
	//            GnStatusBarManager.showSIMIndicator(mStatusBarMgr, getComponentName(), ContactsFeatureConstants.VOICE_CALL_SIM_SETTING);
	//        else
	//            GnStatusBarManager.hideSIMIndicator(mStatusBarMgr, getComponentName());
	//    }
	// New feature for SimIndicator end

	public static class AccountCategoryInfo implements Parcelable {

		public String mAccountCategory;
		public int mSlotId;
		public int mSimId;
		public String mSimName;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(mAccountCategory);
			out.writeInt(mSlotId);
			out.writeInt(mSimId);
			out.writeString(mSimName);
		}

		public static final Parcelable.Creator<AccountCategoryInfo> CREATOR
		= new Parcelable.Creator<AccountCategoryInfo>() {
			public AccountCategoryInfo createFromParcel(Parcel in) {
				return new AccountCategoryInfo(in);
			}

			public AccountCategoryInfo[] newArray(int size) {
				return new AccountCategoryInfo[size];
			}
		};

		private AccountCategoryInfo(Parcel in) {
			mAccountCategory = in.readString();
			mSlotId = in.readInt();
			mSimId = in.readInt();
			mSimName = in.readString();
		}

		public AccountCategoryInfo(String accountCategory, int slot, int simId, String simName) {
			mAccountCategory = accountCategory;
			mSlotId = slot;
			mSimId = simId;
			mSimName = simName;
		}
	}

	// aurora ukiliu 2013-10-11 add for aurora ui begin
	//    private Boolean isInternalStorageFull() {
	//
	//        File sdcardDir = Environment.getExternalStorageDirectory(); 
	//        StatFs sf = new StatFs(sdcardDir.getPath());
	//        long availCount = sf.getAvailableBlocks(); 
	//        if (availCount > 0) {
	//            return false;
	//        } else {
	//            return true;
	//        }
	//	}
	// aurora ukiliu 2013-10-11 add for aurora ui end

	//    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
	//        return uri.buildUpon().appendQueryParameter(GnContactsContract.CALLER_IS_SYNCADAPTER,
	//                String.valueOf(true)).build();
	//    }

	//    private String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
	//        if (count == 0) {
	//            return mContext.getString(zeroResourceId);
	//        } else {
	//            String format = mContext.getResources()
	//                    .getQuantityText(pluralResourceId, count).toString();
	//            return String.format(format, count);
	//        }
	//    }



	public void animationBeforeSetTab() {
		mAllFragment.animationBeforeSetTab();
	}

	public void animationAfterSetTab() {
		mAllFragment.animationAfterSetTab();
	}

//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		// TODO Auto-generated method stub
//		super.onSaveInstanceState(outState);
//	}

	//	@Override
	//	  public void onBackPressed() {
	//		 AuroraDialActivityV1 activityparent = (AuroraDialActivityV1) getParent();
	//	   	 if(activityparent != null) {
	//	   		 activityparent.moveTaskToBack(true);
	//	   	 } else {
	//	   		moveTaskToBack(true);
	//	   	 }
	////	      	super.onBackPressed();
	//	  }
	/*private static final int FLING_MIN_VERTICAL_DISTANCE = 150;//350;
	private boolean countLableHasShown=false;
	private GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
		
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.d("liyang","onFling2");
//			int x1 = (int)e1.getX();
			int y1 = (int)e1.getY();
//			int x2 = (int)e2.getX();
			int y2 = (int)e2.getY();
//			int distanceX = x2 - x1;
			int distanceY = y2 - y1;
			Log.d(TAG,"distanceY:"+distanceY);
			if (Math.abs(distanceY) > FLING_MIN_VERTICAL_DISTANCE){
//				AuroraDefaultContactBrowseListFragment.contacts_count_rl.setVisibility(View.VISIBLE);
				mAllFragment.mListView.setPadding(0, DensityUtil.dip2px(mContext,100), 0, 0);
				mAllFragment.mListView.setSelection(0);
				countLableHasShown=true;
				return true;
			}

			
			return false;
		}
	});*/

	/*@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(countLableHasShown) return super.dispatchTouchEvent(ev);
		boolean isGestureDetected = mGestureDetector.onTouchEvent(ev); 
//		if(isGestureDetected) {
//			ev.setAction(MotionEvent.ACTION_CANCEL);
//		}
		
		return super.dispatchTouchEvent(ev);
	}*/

}
