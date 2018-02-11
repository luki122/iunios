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
package com.android.contacts.list;

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.FragmentCallbacks;
import com.android.contacts.GnFilterHeaderClickListener;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.list.DefaultContactListAdapter.GnContactInfo;
import com.android.contacts.list.ProviderStatusLoader;
import com.android.contacts.list.ProviderStatusLoader.ProviderStatusListener;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.CommonUtils;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.StatisticsUtil;
import com.android.contacts.widget.AbsListIndexer;
import com.android.contacts.widget.AlphbetIndexView;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.dialpad.DialerSearchUtils;
import com.mediatek.contacts.list.service.MultiChoiceRequest;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.RawContacts;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import aurora.widget.AuroraButton;
import android.widget.FrameLayout;
import aurora.widget.AuroraListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Text;

import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.content.Loader;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;

import com.android.contacts.activities.AuroraCallLogActivity;
import com.android.contacts.activities.AuroraContactImportExportActivity;
import com.android.contacts.activities.AuroraContactsSetting;
import com.android.contacts.activities.AuroraDialActivity;
import com.android.contacts.activities.AuroraPeopleActivity;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.MainFragment;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;

import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnCloseButtonClickListener;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraCheckBox;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.telephony.AuroraTelephoneManager;
import gionee.provider.GnTelephony.SIMInfo;

import com.android.contacts.GNContactsUtils;
import android.os.Build;
/**
 * AURORA::add::wangth::20130902
 */
public class AuroraDefaultContactBrowseListFragment extends
ContactBrowseListFragment implements 
ContactListFilterController.ContactListFilterListener,
ProviderStatusListener,
OnSearchViewQuitListener
{
	private static final String TAG = "liyang-AuroraDefaultContactBrowseListFragment";

	private AuroraSearchView mSearchView;
	private RelativeLayout mGotoSearchLayout;
	private RelativeLayout mGotoGroup;
	private AuroraTextView mGotoGroupTv;
	private View mGotoGroupLayout;
	public static TextView contactsCountHeader;
	private Context mContext;

	private RelativeLayout mGotoPrivacyContacts;
	private AuroraTextView mGotoPrivacyContactsTv;
	private LinearLayout mGotoPrivacyContactsLayout;

	public static boolean mIsAuroraSearchMode = false;
	private boolean mIsRemoveMemberMode = false;
	public static boolean mIsEditMode = false;


	private boolean mIsNeedContextMenu = true;

	private AuroraAlertDialog mDeleteConDialog;
	private AuroraAlertDialog mDeleteSelectConDialog;

	private static int mItemCount = 0;
	//    public static int mStarredCount = 0;
	public static boolean mNeedCreateDialerTempTable = true;
	public boolean mSearchViewHasFocus = false;

	public boolean mHasInvisibleContacts = false;

	private boolean mIsInPrivacyMode = false;


	private static String mSelectAllStr;
	private static String mUnSelectAllStr;
	private static boolean mContactsIsDeleting = false;
	private static final int AURORA_NEW_CONTACT = 1;
	// These values needs to start at 2. See {@link ContactEntryListFragment}.
	private static final int SUBACTIVITY_NEW_CONTACT = 2;
	private static final int SUBACTIVITY_EDIT_CONTACT = 3;
	private static final int SUBACTIVITY_ACCOUNT_FILTER = 4;
	private ContactListFilterController mContactListFilterController;

	private ProviderStatusLoader mProviderStatusLoader;

	private ContactsIntentResolver mIntentResolver;
	private ContactsRequest mRequest;
	private BroadcastReceiver mAccountChangeReceiver = new AccountChangeReceiver();

	/**
	 * Whether or not the current contact filter is valid or not. We need to do a check on
	 * start of the app to verify that the user is not in single contact mode. If so, we should
	 * dynamically change the filter, unless the incoming intent specifically requested a contact
	 * that should be displayed in that mode.
	 */
	private boolean mCurrentFilterIsValid;
	/** Sequential ID assigned to each instance; used for logging */
	private int mInstanceId;

	public static FragmentCallbacks mCallbacks;	

	private static final AtomicInteger sNextInstanceId = new AtomicInteger();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contactsCountHeader=null;
		setPhotoLoaderEnabled(true);
		setSectionHeaderDisplayEnabled(true);
		mIntentResolver = new ContactsIntentResolver(getActivity());
		mInstanceId = sNextInstanceId.getAndIncrement();
		mProviderStatusLoader = new ProviderStatusLoader(getActivity());
		mContext = getActivity();
		//		if(getActivity() instanceof AuroraActivity){
		//			final AuroraActivity auroraActivity = (AuroraActivity)getActivity();
		final AuroraActivity auroraActivity=AuroraDialActivity.auroraDialActivity;
		//			mActionBar = auroraActivity.getAuroraActionBar();
		mActionBar = AuroraDialActivity.actionBar;
		//			mHandler.postDelayed(new Runnable() {
		//
		//				@Override
		//				public void run() {
		//					// TODO Auto-generated method stub
		//					setDefaultActionBar(auroraActivity);
		//				}
		//			}, 50);
		setOnContactListActionListener(new ContactBrowserActionListener());
		mSelectAllStr = mContext.getResources().getString(R.string.select_all);
		mUnSelectAllStr = mContext.getResources().getString(R.string.unselect_all);

		//            auroraActivity.setOnSearchViewQuitListener(this);

		mContactListFilterController = ContactListFilterController.getInstance(getActivity());
		mContactListFilterController.checkFilterValidity(false);
		mContactListFilterController.addListener(this);

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.aurora.change.contacts.account");
		filter.addAction("import_vcard_finished");
		mContext.registerReceiver(mAccountChangeReceiver, filter);


		//		}

		/*if(myBroadCastReceiver==null){
			//生成广播处理   
			myBroadCastReceiver = new MyBroadCastReceiver();   
			IntentFilter intentFilter = new IntentFilter(AuroraDialActivity.SEARCH_ACTION_BROADCAST); 
			intentFilter.addAction(AuroraDialActivity.QUERY_ACTION_BROADCAST);
			intentFilter.addAction(AuroraDialActivity.QUIT_SEARCH_ACTION_BROADCAST);
			intentFilter.addAction(AuroraDialActivity.SWITCH_TO_PAGE1);
			intentFilter.addAction(AuroraDialActivity.DESTROY_ACTIVITY);
			//注册广播   
			mContext.registerReceiver(myBroadCastReceiver, intentFilter);  
		}*/

		AuroraDialActivity.setContactsHandler(handler);
		//		Log.d(TAG,"auroraMenuCallBack");
		//		((AuroraActivity)getActivity()).setAuroraSystemMenuCallBack(auroraMenuCallBack);
		//		((AuroraActivity)getActivity()).setAuroraMenuItems(R.menu.aurora_action);


	}

	private TextView leftTextView;
	private TextView rightTextView;
	private TextView middleTextView;
	public static int contactsCount=0;
	AuroraActivity auroraActivity;
	//	public RelativeLayout mToolBar;
	public void setDefaultActionBar(AuroraActivity auroraActivity){
		//		mActionBar.setTitle(R.string.people);
		//		if(mActionBar.getVisibility() != View.VISIBLE){
		//			mActionBar.setVisibility(View.VISIBLE);
		//		}
		//		AuroraActionBarItem item = mActionBar.getItem(0);
		//		if(item != null){
		//			int itemId = item.getItemId();
		//			if(itemId == AURORA_NEW_CONTACT){
		//				mActionBar.changeItemType(AuroraActionBarItem.Type.Add, AURORA_NEW_CONTACT);
		//			} else {
		//				mActionBar.removeItem(item);
		//				mActionBar.addItem(AuroraActionBarItem.Type.Add, AURORA_NEW_CONTACT);
		//			}
		//		} else {
		//			mActionBar.addItem(AuroraActionBarItem.Type.Add, AURORA_NEW_CONTACT);
		//		}
		Log.d(TAG, "setDefaultActionBar");

		auroraActivity.setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		mActionBar.initActionBottomBarMenu(R.menu.aurora_delete, 1);

		//		auroraActivity.setAuroraMenuItems(R.menu.aurora_action);

		//		auroraActivity.addSearchviewInwindowLayout();
		//		mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		//		mActionBar.initActionBottomBarMenu(R.menu.aurora_delete, 1);

		//		showLeftRight();





	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int id = view.getId();
			switch (id) {
			case R.id.empty_view_import_from_sdcard: {
				if (mHasInvisibleContacts) {
					Intent intent=new Intent(mContext, AuroraContactsSetting.class);
					intent.putExtra("goto_account_setting", true);
					startActivityForResult(intent, SUBACTIVITY_ACCOUNT_FILTER);
					break;
				}

				Intent intent=new Intent(mContext, AuroraContactImportExportActivity.class);
				intent.putExtra("import_only", true);
				startActivity(intent);
				break;
			}

			case R.id.empty_view_import_from_acount: {
				Uri uri = Uri.parse("openaccount://com.aurora.account.login");
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setData(uri);
				startActivity(intent);
				break;
			}

			case R.id.empty_view_import_from_sim: {
				if (!SubContactsUtils.simStateReady()||GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
					break;
				}
				showSimContacts(true);
				break;
			}

			default:
				break;
			}
		}
	};

	private class AccountChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.aurora.change.contacts.account")) {
				Log.d(TAG, "account change");
				mContactListFilterController.selectCustomFilter();

				if (!isAdded()) {
					return;
				}

				setFilter(mContactListFilterController.getFilter());
			}else if(action.equals("import_vcard_finished")){
				Log.d(TAG, "import_vcard_finished");
				setPartionStatus();
			}
		}
	}

	protected void setPartionStatus() {
		super.setPartionStatus();		
	}

	// exit from search mode
	public boolean quit() {
		//		Log.d(TAG,"quit");
		//
		//		if (getRemoveMemberMode()) {
		//			mActionBar.setShowBottomBarMenu(true);
		//			mActionBar.showActionBottomeBarMenu();
		//		}     
		//
		//
		//		getListView().auroraSetNeedSlideDelete(false);
		//
		//		if (mGotoPrivacyContactsLayout != null && !mGotoPrivacyContactsLayout.isShown() 
		//				&& ContactsApplication.sIsAuroraPrivacySupport
		//				&& mIsInPrivacyMode) {
		//			getListView().addHeaderView(mGotoPrivacyContactsLayout, null, false);
		//		}
		//
		//		if (mGotoGroupLayout != null && !mGotoGroupLayout.isShown()) {
		//			getListView().addHeaderView(mGotoGroupLayout, null, false);
		//		}
		//
		//		getListView().setAdapter(getAdapter());
		//
		//		getListView().auroraSetHeaderViewYOffset(0);
		//		mIsAuroraSearchMode = false;
		//		getAdapter().setAuroraListDelet(true);
		//		setQueryTextToFragment("");


		return true;
	}

	//    @Override
	//    public boolean onSearchRequested() { // Search key pressed.
	//        return true;
	//    }

	@Override
	public void onContactListFilterChanged() {
		if (!isAdded()) {
			return;
		}

		setFilter(mContactListFilterController.getFilter());
	}

	private final class ContactBrowserActionListener implements OnContactBrowserActionListener {

		@Override
		public void onSelectionChange() {

		}

		@Override
		public void onViewContactAction(Uri contactLookupUri) {
			Log.d(TAG,"onViewContactAction");
			Intent intent = IntentFactory.newViewContactIntent(contactLookupUri);
			startActivity(intent);
		}

		@Override
		public void onCreateNewContactAction() {
			Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
			Bundle extras = getActivity().getIntent().getExtras();
			if (extras != null) {
				intent.putExtras(extras);
			}
			startActivity(intent);
		}

		@Override
		public void onEditContactAction(Uri contactLookupUri) {
			Intent intent = new Intent(Intent.ACTION_EDIT, contactLookupUri);
			Bundle extras = getActivity().getIntent().getExtras();
			if (extras != null) {
				intent.putExtras(extras);
			}
			intent.putExtra(
					ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
			startActivityForResult(intent, SUBACTIVITY_EDIT_CONTACT);
		}

		@Override
		public void onAddToFavoritesAction(Uri contactUri) {
			ContentValues values = new ContentValues(1);
			values.put(Contacts.STARRED, 1);
			mContext.getContentResolver().update(contactUri, values, null, null);
		}

		@Override
		public void onRemoveFromFavoritesAction(Uri contactUri) {
			ContentValues values = new ContentValues(1);
			values.put(Contacts.STARRED, 0);
			mContext.getContentResolver().update(contactUri, values, null, null);
		}

		@Override
		public void onCallContactAction(Uri contactUri) {
			//            PhoneNumberInteraction.startInteractionForPhoneCall(AuroraPeopleActivity.this, contactUri);
		}

		@Override
		public void onSmsContactAction(Uri contactUri) {
			//            PhoneNumberInteraction.startInteractionForTextMessage(AuroraPeopleActivity.this, contactUri);
		}

		@Override
		public void onDeleteContactAction(Uri contactUri) {
			ContactDeletionInteraction.start(getActivity(), contactUri, false);
		}

		@Override
		public void onFinishAction() {
			getActivity().onBackPressed();
		}

		@Override
		public void onInvalidSelection() {
			ContactListFilter filter;
			ContactListFilter currentFilter = getFilter();
			if (currentFilter != null
					&& currentFilter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
				filter = ContactListFilter.createFilterWithType(
						ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
				setFilter(filter);
			} else {
				filter = ContactListFilter.createFilterWithType(
						ContactListFilter.FILTER_TYPE_SINGLE_CONTACT);
				setFilter(filter, false);
			}
		}
	}

	/**
	 * Resolve the intent and initialize {@link #mRequest}, and launch another activity if redirect
	 * is needed.
	 *
	 * @param forNewIntent set true if it's called from {@link #onNewIntent(Intent)}.
	 * @return {@code true} if {@link PeopleActivity} should continue running.  {@code false}
	 *         if it shouldn't, in which case the caller should finish() itself and shouldn't do
	 *         farther initialization.
	 */
	public boolean processIntent(boolean forNewIntent) {
		// Extract relevant information from the intent
		mRequest = mIntentResolver.resolveIntent(getActivity().getIntent());
		if (Log.isLoggable("AuroraPeopleActivity", Log.DEBUG)) {
			Log.d(TAG, this + " processIntent: forNewIntent=" + forNewIntent
					+ " intent=" + getActivity().getIntent() + " request=" + mRequest);
		}
		if (!mRequest.isValid()) {
			getActivity().setResult(Activity.RESULT_CANCELED);
			return false;
		}

		Intent redirect = mRequest.getRedirectIntent();
		if (redirect != null) {
			// Need to start a different activity
			startActivity(redirect);
			return false;
		}

		if (mRequest.getActionCode() == ContactsRequest.ACTION_VIEW_CONTACT
				&& !PhoneCapabilityTester.isUsingTwoPanes(mContext)) {
			redirect = new Intent(mContext, ContactDetailActivity.class);
			redirect.setAction(Intent.ACTION_VIEW);
			redirect.setData(mRequest.getContactUri());
			startActivity(redirect);
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		// Shown on logcat
		return String.format("%s@%d", getClass().getSimpleName(), mInstanceId);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SUBACTIVITY_ACCOUNT_FILTER: {
			AccountFilterUtil.handleAccountFilterResult(
					mContactListFilterController, resultCode, data);
			break;
		}

		case SUBACTIVITY_NEW_CONTACT:
		case SUBACTIVITY_EDIT_CONTACT: {
			if (resultCode == Activity.RESULT_OK && PhoneCapabilityTester.isUsingTwoPanes(mContext)) {
				mRequest.setActionCode(ContactsRequest.ACTION_VIEW_CONTACT);
				setSelectionRequired(true);
				reloadDataAndSetSelectedUri(data.getData());
				// No need to change the contact filter
				mCurrentFilterIsValid = true;
			}
			break;
		}

		// TODO: Using the new startActivityWithResultFromFragment API this should not be needed
		// anymore
		case ContactEntryListFragment.ACTIVITY_REQUEST_CODE_PICKER:
			if (resultCode == Activity.RESULT_OK) {
				onPickerResult(data);
			}

			//aurora add zhouxiaobing 20131216 start  
		case AURORA_SIM_CONTACTS:
			if (resultCode == Activity.RESULT_OK) {
				showSimContacts(false);
			}
			break;
			//aurora add zhouxiaobing 20131216 end  	
		}
	}

	private void showLeftRight() {
		if (mActionBar.getSelectLeftButton() != null) {
			mActionBar.getSelectLeftButton().setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							mActionBar.setShowBottomBarMenu(false);
							mActionBar.showActionBarDashBoard();

							if (getRemoveMemberMode()) {
								changeToNormalMode(true);
							}
						}
					});
		}

		if (mActionBar.getSelectRightButton() != null) {
			mActionBar.getSelectRightButton().setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							String selectStr = ((TextView) (mActionBar
									.getSelectRightButton())).getText()
									.toString();
							if (selectStr.equals(mSelectAllStr)) {
								((TextView) (mActionBar.getSelectRightButton()))
								.setText(mUnSelectAllStr);
								onSelectAll(true);
							} else if (selectStr.equals(mUnSelectAllStr)) {
								((TextView) (mActionBar.getSelectRightButton()))
								.setText(mSelectAllStr);
								onSelectAll(false);
							}
						}
					});
		}
	}

	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AURORA_NEW_CONTACT:
				Intent intent = IntentFactory.newInsertContactIntent(true,
						null, null, null);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.menu_delete: {
				Log.d(TAG, "menu click menu_delete");
				if (mContactsIsDeleting) {
					Toast.makeText(mContext, R.string.aurora_deleting_contacts_toast, Toast.LENGTH_SHORT).show();
					break;
				}
				removeContacts();
				break;
			}

			case R.id.aurora_menu_contacts_setting: {
				Log.d(TAG, "click aurora_menu_contacts_setting");
				Intent intent=new Intent(mContext, AuroraContactsSetting.class);
				startActivityForResult(intent, SUBACTIVITY_ACCOUNT_FILTER);
				//				StatisticsUtil.getInstance(mContext.getApplicationContext()).report(StatisticsUtil.Contact_Setting);
				break;
			}

			default:
				break;
			}
		}
	};

	private void removeContacts() {
		int selectCount = getAdapter().getCheckedItem().size();
		if (selectCount <= 0) {
			return;
		}

		if (null == mDeleteSelectConDialog) {
			Log.d(TAG, "getAdapter().getSelectedCount():"+getAdapter().getSelectedCount());
			mDeleteSelectConDialog = new AuroraAlertDialog.Builder(mContext,
					AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
			.setTitle(mContext.getResources().getString(R.string.multichoice_delete_confirm_message,getAdapter().getSelectedCount()))
			.setMessage(R.string.single_contact_delete_summary)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(R.string.delete,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int whichButton) {
					doDeleteContact();
				}
			}).create();
		}
		mDeleteSelectConDialog.
		setTitle(mContext.getResources().getString(R.string.multichoice_delete_confirm_message,getAdapter().getSelectedCount()));

		mDeleteSelectConDialog.show();
	}
	private void doDeleteContact() {
		if (mDeleteSelectConDialog != null) {
			mDeleteSelectConDialog.dismiss();
			mDeleteSelectConDialog = null;
		}
		DeleteThread deleteThread = null;
		if (null != deleteThread && deleteThread.isAlive()) {
			Toast.makeText(mContext, R.string.contact_delete_all_tips,
					Toast.LENGTH_SHORT).show();
			return;
		}

		mContactsIsDeleting = true;

		int selectedCount = getAdapter().getCheckedItem().size();
		long[] checkedIds = new long[selectedCount];
		int[] positions = new int[selectedCount];
		int index = 0;
		Set<Long> contactsIds = getAdapter().getCheckedItem().keySet();
		for (Long id : contactsIds) {
			checkedIds[index++] = id;
		}

		ArrayList<Long> contactIdList = new ArrayList<Long>();
		ArrayList<Integer> rawContactIdList = new ArrayList<Integer>();
		ArrayList<Integer> indicateIdList = new ArrayList<Integer>();
		int[] simIndexIds = new int[selectedCount];

		List<MultiChoiceRequest> requests = new ArrayList<MultiChoiceRequest>();
		for (int position = 0, curArray = 0; position < selectedCount && curArray < selectedCount; ++ position) {
			int contactId = (int)checkedIds[position];
			int listPos = Integer.parseInt(getAdapter().getCheckedItem().get(checkedIds[position]));
			simIndexIds[curArray] = -1;
			contactIdList.add(Long.valueOf(contactId));
			rawContactIdList.add(getAdapter().getRawcontactId(listPos));
			indicateIdList.add(getAdapter().getIndicatePhoneSim(listPos));
			curArray++;
		}

		sendMessage(mHandler, START_DELETE, 0, contactIdList.size());
		if (null == deleteThread) {
			deleteThread = new DeleteThread(contactIdList, rawContactIdList, indicateIdList);
		}
		deleteThread.start();

		mNeedCreateDialerTempTable = true;
	}

	private int ActualBatchDelete(ArrayList<Long> contactIdList) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (Long id : contactIdList) {
			sb.append(String.valueOf(id));
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		Log.d(TAG, "ActualBatchDelete ContactsIds " + sb.toString() + " ");

		int deleteCount = 0;
		try {
			deleteCount = mContext.getContentResolver().delete(
					RawContacts.CONTENT_URI, 
					RawContacts.CONTACT_ID+ " IN " + sb.toString() + " AND " + RawContacts.DELETED + "=0", 
					null);
		} catch (IllegalStateException ie) {
			ie.printStackTrace();
			Log.d(TAG, "db error, delete failed");
		}

		Log.d(TAG, "ActualBatchDelete ContactsIds: " + deleteCount);
		return deleteCount;
	}

	private class DeleteThread extends Thread {
		private ArrayList<Long> contactIdList;
		private ArrayList<Integer> rawContactIdList;
		private ArrayList<Integer> indicateList;
		private boolean mLarge = false;
		public DeleteThread(ArrayList<Long> contactIdList, ArrayList<Integer> raw, ArrayList<Integer> indi) {
			this.contactIdList = contactIdList;
			this.rawContactIdList = raw;
			this.indicateList = indi;
			if (contactIdList != null && contactIdList.size() > 500) {
				mLarge = true;
			}
		}

		@Override
		public void run() {
			Log.d(TAG, "ActualBatchDelete");
			if (contactIdList == null || contactIdList.size() == 0) {
				return;
			}

			if (rawContactIdList == null || rawContactIdList.size() == 0) {
				return;
			}

			if (indicateList == null || indicateList.size() == 0) {
				return;
			}

			int successfulItems = 0;
			int currentCount = 0;
			long raw_contact_id = -1;
			long indicate = -1;
			final ArrayList<Long> idsList = new ArrayList<Long>();
			for (long id : contactIdList) {
				//aurora add zhouxiaobing 20131227 start
				int index = contactIdList.indexOf(id);
				raw_contact_id = rawContactIdList.get(index);
				indicate = indicateList.get(index);
				Log.d(TAG, "contactId = " + id + "  rawContactId = " + raw_contact_id + "  indicate = " + indicate);

				if (indicate >= 0) {
					// stop
					if ((FeatureOption.MTK_GEMINI_SUPPORT && !SimCardUtils.isSimStateReady(SIMInfoWrapper.getDefault().getSlotIdBySimId((int)indicate)))
							|| (!FeatureOption.MTK_GEMINI_SUPPORT && !SimCardUtils
									.isSimStateReady(0))||GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
						continue;
					}

					String number="";
					String name="";
					final String[] projection = new String[] {
							Contacts._ID, 
							Contacts.Data.MIMETYPE, 
							Contacts.Data.DATA1,
					};
					Cursor dataCursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
							projection, Data.RAW_CONTACT_ID + " =? ", 
							new String[] { String.valueOf(raw_contact_id) }, null);
					if (dataCursor != null && dataCursor.moveToFirst()) {
						do {
							if(dataCursor.getString(1).equals(Phone.CONTENT_ITEM_TYPE)) {
								number=dataCursor.getString(2);
							} else if (dataCursor.getString(1).equals(StructuredName.CONTENT_ITEM_TYPE)) {
								name=dataCursor.getString(2);
							}
						} while (dataCursor.moveToNext());

						dataCursor.close();
						dataCursor=null;
						Log.v(TAG, "tag = " + name + "  number = " + number + "  uri = " + SubContactsUtils.getUri(0));
						int result = 0;
						try {
							if (Build.VERSION.SDK_INT < 21) {
								Log.d(TAG, "uri0:"+SimCardUtils.getSimContactsUri(SIMInfo.getSlotById(mContext,indicate), false));
								String s="鳚";
								name=name.replace(' ', s.toCharArray()[0]);//aurora add zhouxiaobing 20140305 for delete space
								result = mContext.getContentResolver().delete(/*SubContactsUtils.getUri(0)*/
										SimCardUtils.getSimContactsUri(SIMInfo.getSlotById(mContext,indicate), false), ("tag=" + name+" AND "+"number="+number), null);
							} else {
								Log.d(TAG, "uri:"+SimCardUtils.getSimContactsUri((int)indicate,
										SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, indicate))));
								// Aurora xuyong 2015-07-13 modified for bug #14161 start
								result = mContext.getContentResolver().delete(SimCardUtils.getSimContactsUri((int)indicate,
										SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, indicate))), ("tag=" + name+" AND "+"number="+number), null);
								// Aurora xuyong 2015-07-13 modified for bug #14161 end
							} 
							Log.d(TAG, "result:"+result);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						if(dataCursor != null) {
							dataCursor.close();
						}
					}
				}
				//aurora add zhouxiaobing 20131227 end
				idsList.add(id);
				currentCount++;

				if (idsList.size() >= 30) {
					successfulItems += ActualBatchDelete(idsList);
					sendMessage(mHandler, REFRESH_DELETE, successfulItems, contactIdList.size());
					idsList.clear();
					// Aurora xuyong 2015-08-12 added for bug #15590 start
					try {
						if (mLarge) {
							Thread.sleep(300);
						} else {
							Thread.sleep(100);
						}
					} catch (InterruptedException e) {
					}
					// Aurora xuyong 2015-08-12 added for bug #15590 end
				}

				//wait for database delete sync. if not sleep, the UI will not friend.
				// Aurora xuyong 2015-08-12 deleted for bug #15590 start
				/*try {
                	if (mLarge) {
                		Thread.sleep(300);
                	} else {
                		Thread.sleep(100);
                	}
                } catch (InterruptedException e) {
                }*/
				// Aurora xuyong 2015-08-12 deleted for bug #15590 end
			}

			if (idsList.size() > 0) {
				successfulItems += ActualBatchDelete(idsList);
				idsList.clear();
			}


			sendMessage(mHandler, END_DELETE, successfulItems, contactIdList.size());
		}
	}

	@Override
	public CursorLoader createCursorLoader() {
		Log.i(TAG, "createCursorLoader");

		if (mIsAuroraSearchMode) {
			return new CursorLoader(mContext, null, null, null, null, null);
		}

		isFinished = false;
		mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
				WAIT_CURSOR_DELAY_TIME);
		return new StarredAndContactsLoader(getActivity());
	}

	private long lastClickTime; 
	public boolean isFastClick() { 
		long time = System.currentTimeMillis(); 
		long timeD = time - lastClickTime; 
		Log.d(TAG, "timeD:"+timeD);
		if ( 0 < timeD && timeD < 800) {    
			return true;    
		}    
		lastClickTime = time;    
		return false;    
	} 

	@Override
	protected void onItemClick(int position, long id) {
		if (getRemoveMemberMode()) {
			return;
		}
		//		StatisticsUtil.getInstance(mContext.getApplicationContext()).report(StatisticsUtil.Contact_Detail);
		if (isFastClick()) {   
			return;
		}
		viewContact(getAdapter().getContactUri(position));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.onItemClick(parent, view, position, id);
		Log.d(TAG, "onItemClick: position = " + position + "  id = " + id);
		if (!getRemoveMemberMode()) {
			return;
		}

		int realPosition = getRightPosition(position);
		//		if (!mIsAuroraSearchMode && realPosition < mStarredCount) {
		//			return;
		//		}

		final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		Log.d(TAG,"checkbox:"+checkBox);
		if (null != checkBox) {
			boolean checked = checkBox.isChecked();
			checkBox.auroraSetChecked(!checked, true);

			if (!checked) {
				Log.d(TAG,"Long.valueOf(getAdapter().getContactID(realPosition)):"+Long.valueOf(getAdapter().getContactID(realPosition)));
				getAdapter().setCheckedItem(Long.valueOf(getAdapter().getContactID(realPosition)), String.valueOf(realPosition));
			} else {
				getAdapter().getCheckedItem().remove(Long.valueOf(getAdapter().getContactID(realPosition)));
			}

			if (null != getActivity()) {
				updateSelectedItemsView(mItemCount);
			}

			getAdapter().notifyDataSetChanged();
		}
	}

	public void onSelectAll(boolean check) {
		updateListCheckBoxeState(check);
	}

	private void updateListCheckBoxeState(boolean checked) {
		final int headerCount = getListView().getHeaderViewsCount();
		final int count = getAdapter().getCount() + headerCount;
		int contactId = -1;

		if (!mIsAuroraSearchMode) {
			for (int position = mStarredCount + headerCount;
					position < count; ++position) {
				int adapterPos = position - headerCount;
				contactId = getAdapter().getContactID(adapterPos);
				Log.d(TAG, "adapterPos = " + adapterPos + "  contactId = " + contactId);

				if (checked) {
					getAdapter().setCheckedItem(Long.valueOf(contactId), String.valueOf(adapterPos));
				} else {
					getAdapter().getCheckedItem().clear();
				}

				int realPos = position - getListView().getFirstVisiblePosition();
				if (realPos >= 0) {
					View view = getListView().getChildAt(realPos);
					if (view != null) {
						final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
						if (null != checkBox) {
							checkBox.auroraSetChecked(checked, true);
						}
					}
				}
			}
		} else {
			for (int position = 0; position < count; ++position) {
				contactId = getAdapter().getContactID(position);
				Log.d(TAG, "position = " + position + "  contactId = " + contactId);

				if (checked) {
					getAdapter().setCheckedItem(Long.valueOf(contactId), String.valueOf(position));
				} else {
					getAdapter().getCheckedItem().clear();
				}

				if (position >= 0) {
					View view = getListView().getChildAt(position);
					if (view != null) {
						final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
						if (null != checkBox) {
							checkBox.auroraSetChecked(checked, true);
						}
					}
				}
			}
		}

		if (null != getActivity()) {
			setBottomMenuEnable(checked);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (mIsAuroraSearchMode) {
			return;
		}

		if (!mIsNeedContextMenu) {
			return;
		}

		super.onCreateContextMenu(menu, v, menuInfo);

		View targetView = ((AdapterContextMenuInfo)menuInfo).targetView;
		RelativeLayout mainUi = (RelativeLayout)targetView.findViewById(com.aurora.R.id.aurora_listview_front);
		if (null != mainUi && mainUi.getChildAt(0) instanceof ContactListItemView) {
			AdapterView.AdapterContextMenuInfo info;
			try {
				info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			} catch (ClassCastException e) {
				Log.e(TAG, "bad menuInfo", e);
				return;
			}

			Log.d(TAG, "info.id = " + info.id + "   info.po = " + info.position);

			int pos = getRightPosition(info.position);
			//			if (pos < mStarredCount) {
			//				return;
			//			}


			getAdapter().setCheckedItem(Long.valueOf(getAdapter().getContactID(pos)), String.valueOf(pos));
		}

		//		mGotoGroupLayout.setClickable(false);
		//		mGotoGroupLayout.setEnabled(false);
		//		mGotoGroupTv.setTextColor(R.color.calllog_list_header_text_color);
		//
		//		mGotoPrivacyContactsLayout.setClickable(false);
		//		mGotoPrivacyContactsLayout.setEnabled(false);
		//		mGotoPrivacyContactsTv.setTextColor(R.color.calllog_list_header_text_color);

		if (mGotoPrivacyContactsLayout != null/* && mGotoPrivacyContactsLayout.isShown()*/
				&& ContactsApplication.sIsAuroraPrivacySupport
				&& mIsInPrivacyMode) {
			getListView().removeHeaderView(mGotoPrivacyContactsLayout);
		}
		if (mGotoGroupLayout != null/* && mGotoGroupLayout.isShown()*/) {
			getListView().removeHeaderView(mGotoGroupLayout);
		}

		//		setTabVisib(false);

		mIsNeedContextMenu = false;
		getAdapter().setCheckBoxEnable(true);
		getAdapter().setNeedAnim(true);
		setRemoveMemberMode(true);
		getAdapter().notifyDataSetChanged();

		getListView().auroraSetNeedSlideDelete(false);
		getListView().auroraEnableSelector(false);

		initActionBar(true);


		if(mCallbacks!=null){
			mCallbacks.onFragmentCallback(FragmentCallbacks.SWITCH_TO_EDIT_MODE,null);
		}


		(AuroraDialActivity.auroraDialActivity).setMenuEnable(false);
		updateSelectedItemsView(mItemCount);

		mIsEditMode=true;
	}

	//	public interface IAuroraContactsFragment{
	//		public void setTabWidget(boolean flag);
	//	}
	//	private IAuroraContactsFragment mIAuroraContactsFragment;
	//	public void setIAuroraContactsFragment(IAuroraContactsFragment iAuroraContactsFragment){
	//		mIAuroraContactsFragment = iAuroraContactsFragment;
	//	}
	//
	//	private boolean setTabVisib(boolean flag) {
	//		if(mIAuroraContactsFragment != null){
	//			mIAuroraContactsFragment.setTabWidget(flag);
	//		}
	//		return false;
	//	}

	public void setFooterViewVisibility(int visible){
		//		if(mFooterView != null){
		//			mFooterView.setVisibility(visible);
		//		}
	}

	public AuroraActionBar mActionBar;
	private void initActionBar(boolean flag) {
		//		AuroraActionBar mActionBar;
		//		mActionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();

		//		mActionBar.setVisibility(View.GONE);
		mActionBar.setShowBottomBarMenu(flag);
		//        mActionBar.showActionBarMenu();
		mActionBar.showActionBarDashBoard();
		mActionBar.auroraIsEntryEditModeAnimRunning();
		mActionBar.auroraIsExitEditModeAnimRunning();


		if(leftTextView==null){
			leftTextView=(TextView) mActionBar.getSelectLeftButton();
		}
		if(rightTextView==null){
			rightTextView=(TextView) mActionBar.getSelectRightButton();
		}
		if(middleTextView==null){
			middleTextView=mActionBar.getMiddleTextView();
		}
		if(middleTextView!=null){
			middleTextView.setText(mContext.getString(R.string.selected_total_num, 1));
		}

		if(leftTextView!=null){
			leftTextView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					changeToNormalMode(true);
					mActionBar.setShowBottomBarMenu(false);
					mActionBar.showActionBarDashBoard();
				}
			});
		}


		Log.d(TAG,"rightTextView:"+rightTextView);
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
						middleTextView.setText(mContext.getString(R.string.selected_total_num, mItemCount));

					} else if (selectStr.equals(mUnSelectAllStr)) {
						((TextView)v)
						.setText(mSelectAllStr);
						onSelectAll(false);
						setBottomMenuEnable(false);
						middleTextView.setText(mContext.getString(R.string.selected_total_num, 0));
					}

					getAdapter().notifyDataSetChanged();
				}
			});
		}

	}

	private void initButtomBar(boolean flag) {
		try {
			//			AuroraActionBar mActionBar;
			//			mActionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
			mActionBar.setShowBottomBarMenu(flag);
			mActionBar.showActionBottomeBarMenu();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setRemoveMemberMode(boolean flag) {
		mIsRemoveMemberMode = flag;
	}

	public boolean getRemoveMemberMode() {
		return mIsRemoveMemberMode;
	}

	public void changeToNormalMode(boolean flag) {

		if(mCallbacks!=null){
			mCallbacks.onFragmentCallback(FragmentCallbacks.SWITCH_TO_NORMAL_MODE,null);
		}

		Log.d(TAG, "changeToNormalMode  " + flag);
		if (getActivity() == null) {
			return;
		}
		(AuroraDialActivity.auroraDialActivity).setMenuEnable(true);
		if (!flag) {
			initActionBar(false);
		}

		getListView().auroraSetNeedSlideDelete(false);
		getListView().auroraEnableSelector(true);
		//		mToolBar.setVisibility(View.GONE);
		//隐藏顶部
		//		AuroraCallLogActivity.onSetViewPagerListener.setVisibity(true);
		//		setTabVisib(true);

		//		mGotoGroupLayout.setClickable(true);
		//		mGotoGroupLayout.setEnabled(true);
		//		mGotoGroupTv.setTextAppearance(mContext, R.style.list_goto_group_text_style);
		//
		//		mGotoPrivacyContactsLayout.setClickable(true);
		//		mGotoPrivacyContactsLayout.setEnabled(true);
		//		mGotoPrivacyContactsTv.setTextAppearance(mContext, R.style.list_goto_group_text_style);

		if (mGotoPrivacyContactsLayout != null/* && !mGotoPrivacyContactsLayout.isShown()*/ 
				&& ContactsApplication.sIsAuroraPrivacySupport
				&& mIsInPrivacyMode&&mIsEditMode) {
			Log.d(TAG,"addHeaderView mGotoPrivacyContactsLayout");
			getListView().addHeaderView(mGotoPrivacyContactsLayout, null, false);
		}
		if (mGotoGroupLayout != null/* && !mGotoGroupLayout.isShown()*/&&mIsEditMode) {
			Log.d(TAG,"addHeaderView mGotoGroupLayout");
			getListView().addHeaderView(mGotoGroupLayout, null, false);
		}


		// Aurora xuyong 2015-08-15 modified for bug #15699 start
		/*if (!flag) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        getAdapter().getCheckedItem().clear();
                        getAdapter().setNeedAnim(true);
                        getAdapter().setCheckBoxEnable(false);
                        setRemoveMemberMode(false);
                        mIsNeedContextMenu = true;
                        getAdapter().notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 330);
        } else {*/
		try {
			// Aurora xuyong 2015-08-15 modified for bug #15699 end
			getAdapter().getCheckedItem().clear();
			getAdapter().setNeedAnim(true);
			getAdapter().setCheckBoxEnable(false);
			setRemoveMemberMode(false);
			mIsNeedContextMenu = true;
			getAdapter().notifyDataSetChanged();
			// Aurora xuyong 2015-08-15 modified for bug #15699 start
		} catch (Exception e) {
			e.printStackTrace();
		}
		//}
		// Aurora xuyong 2015-08-15 modified for bug #15699 end

		mIsEditMode=false;
	}

	@Override
	protected ContactListAdapter createListAdapter() {
		DefaultContactListAdapter adapter = new DefaultContactListAdapter(getContext());
		return adapter;
	}

	//    @Override
	//    protected void configureAdapter() {
	//        super.configureAdapter();
	//        ContactEntryListAdapter adapter = getAdapter();
	//        adapter.setFilter(ContactListFilter.createFilterWithType(ContactListFilter.FILTER_TYPE_CUSTOM));
	//    }

	@Override
	protected View inflateView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.aurora_contact_list_content, null);
	}

	public void auroraInitQueryDialerABC() {
		Log.d(TAG,"auroraInitQueryDialerABC0");
		try {
			mContext.getContentResolver().query(Uri.parse("content://com.android.contacts/gn_dialer_search_init"),
					null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void reloadData() {
		super.reloadData();
	}


	private boolean isHeaderShown=true;
	public Handler handler=new Handler(){
		public void handleMessage(Message msg){
			if(msg.what==AuroraDialActivity.SEARCH_ACTION_BROADCAST){//SEARCH_ACTION_BROADCAST
				//				queryString=msg.getData().getString("queryString");
				Log.d(TAG,"receive search action");
				mSearchView=AuroraDialActivity.mSearchView;

				if (mGotoPrivacyContactsLayout != null/* && mGotoPrivacyContactsLayout.isShown()*/
						&& ContactsApplication.sIsAuroraPrivacySupport
						&& mIsInPrivacyMode) {
					getListView().removeHeaderView(mGotoPrivacyContactsLayout);
				}
				if (mGotoGroupLayout != null/* && mGotoGroupLayout.isShown()*/) {
					getListView().removeHeaderView(mGotoGroupLayout);
				}
				isHeaderShown=false;

				showSearchView();

				auroraInitQueryDialerABC();
			}else if(msg.what==AuroraDialActivity.QUERY_ACTION_BROADCAST){//QUERY_ACTION_BROADCAST
				queryString=(String) msg.obj;
				beginSearch(queryString);
			}else if(msg.what==AuroraDialActivity.SWITCH_TO_PAGE1){//SWITCH_TO_PAGE1
				setDefaultActionBar(AuroraDialActivity.auroraDialActivity);	
			}else if(msg.what==AuroraDialActivity.DESTROY_ACTIVITY){//DESTROY_ACTIVITY
				AuroraDefaultContactBrowseListFragment.this.onDestroy();
			}else if(msg.what==AuroraDialActivity.QUERY_QUIT){
				Log.d(TAG,"receive QUERY_QUIT");
				mIsAuroraSearchMode=false;
				setQueryTextToFragment("");
				getAdapter().setSearchMode(mIsAuroraSearchMode);
				reloadData();				
				getListView().auroraSetHeaderViewYOffset(0);
				getListView().setVisibility(View.VISIBLE);

				if(footer!=null) footer.setPadding(0, 0, 0, 0);  

				if (null != mAlphbetIndexView) {
					mAlphbetIndexView.setVisibility(View.VISIBLE);
				}
				if (mGotoPrivacyContactsLayout != null/* && !mGotoPrivacyContactsLayout.isShown() */
						&& ContactsApplication.sIsAuroraPrivacySupport
						&& mIsInPrivacyMode&&!isHeaderShown) {
					Log.d(TAG,"addHeaderView mGotoPrivacyContactsLayout");
					getListView().addHeaderView(mGotoPrivacyContactsLayout, null, false);
				}
				if (mGotoGroupLayout != null/* && !mGotoGroupLayout.isShown()&&!isHeaderShown*/) {
					Log.d(TAG,"addHeaderView mGotoGroupLayout");
					getListView().addHeaderView(mGotoGroupLayout, null, false);
				}
				isHeaderShown=true;
			}else if(msg.what==AuroraDialActivity.SWITCH_TO_NORMAL_MODE){
				changeToNormalMode(true);
				mActionBar.setShowBottomBarMenu(false);
				mActionBar.showActionBarDashBoard();
			}
		}
	};



	//	private MyBroadCastReceiver myBroadCastReceiver;




	/*public class MyBroadCastReceiver extends BroadcastReceiver    
	{     

		@Override  
		public void onReceive(Context context, Intent intent)   
		{   
			String action = intent.getAction();    	

			Log.d(TAG, "onReceive: "+intent.getAction());
			if (action.equals(AuroraDialActivity.SEARCH_ACTION_BROADCAST)) {
				Log.d(TAG,"receive search action");
				mSearchView=AuroraDialActivity.mSearchView;

				showSearchView();

			}else if(action.equals(AuroraDialActivity.QUERY_ACTION_BROADCAST)){
				String queryString=intent.getStringExtra("queryString");
				Log.d(TAG,"queryString:"+queryString);
				beginSearch(queryString);

			}else if(action.equals(AuroraDialActivity.QUIT_SEARCH_ACTION_BROADCAST)){
				quit();

			}else if (action.equals(AuroraDialActivity.SWITCH_TO_PAGE1)) {
				setDefaultActionBar(AuroraDialActivity.auroraDialActivityV3);				

			}else if (action.equals(AuroraDialActivity.DESTROY_ACTIVITY)) {
				AuroraDefaultContactBrowseListFragment.this.onDestroy();				

			}
		}   

	}  */



	private void beginSearch(String queryString){
		ContactsApplication.sendSimContactBroad();
		setSearchTextString(queryString);
		Log.d(TAG,"queryString:"+queryString);
		if (queryString!=null&&queryString.length() > 0) {	
			getListView().setVisibility(View.VISIBLE);		
			if (null != mAlphbetIndexView) {
				mAlphbetIndexView.setVisibility(View.VISIBLE);
			}

			//			getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
			mIsAuroraSearchMode = true;


			getAdapter().setAuroraListDelet(false);
			setQueryTextToFragment(queryString);

			if (checkIsNeedQueryFromDialer(queryString)) {
				auroraNoMatchView(mIsAuroraSearchMode, null);
			}

		} else {
			this.queryString=null;		

			getListView().setAdapter(getAdapter());

			//            if (mGotoSearchLayout != null) {
			//                mGotoSearchLayout.setVisibility(View.VISIBLE);
			//            }

			getListView().auroraSetHeaderViewYOffset(0);
			//			mIsAuroraSearchMode = false;
			getAdapter().setAuroraListDelet(true);
			setQueryTextToFragment("");
			mListView.setVisibility(View.GONE);
			auroraNoMatchView(false, null);


		}


		getAdapter().setSearchMode(mIsAuroraSearchMode);
	}



	public void showSearchView(){
		Log.d(TAG, "showsearchView0");

		/*((AuroraActivity) getActivity()).showSearchviewLayout();
        mSearchView = ((AuroraActivity) getActivity()).getSearchView();
        if (null == mSearchView) {
            return;
        }
        mSearchView.setMaxLength(30);

        mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);*/

		//        initButtomBar(false);
		//        setRightBtnTv(getAdapter().getCheckedItem().size());
		//        ((AuroraActivity) getActivity()).setOnQueryTextListener(new svQueryTextListener());
		mSearchView=AuroraDialActivity.mSearchView;
		//        mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {
		//                    @Override
		//                    public void onFocusChange(View view,
		//                            boolean hasFocus) {
		//                        mSearchViewHasFocus = hasFocus;
		//                        if (hasFocus) {
		//                            if (mNeedCreateDialerTempTable) {
		//                                auroraInitQueryDialerABC();
		//                                mNeedCreateDialerTempTable = false;
		//                            }
		//                        }
		//                    }
		//                });
		setSearchView(mSearchView);
		mIsAuroraSearchMode=true;
		//		getListView().auroraSetNeedSlideDelete(false);

		if(mNoContactsEmptyView!=null&&mNoContactsEmptyView.getVisibility()==View.VISIBLE){
			mNoContactsEmptyView.setVisibility(View.GONE);
		}else{
			getListView().setVisibility(View.INVISIBLE);		
			if (null != mAlphbetIndexView) {
				mAlphbetIndexView.setVisibility(View.GONE);
			}
		}

		if(footer!=null) footer.setPadding(0, -(mListView.getHeight()>0?mListView.getHeight():100), 0, 0);  
	}



	//	public static View contacts_count_rl;
	//	public static TextView contactsCountTextView;
	private View footer;
	@Override
	protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
		super.onCreateView(inflater, container);

		mContext = getActivity();        

		mLayout = (LinearLayout) getView().findViewById(R.id.pinned_header_list_layout);
		mMainContent = (FrameLayout) getView().findViewById(R.id.main_content);
		mNoContactsFra = (FrameLayout) getView().findViewById(R.id.no_contacts_fra);
		//		mFooterView = getView().findViewById(R.id.footer_view);

		mListView = (AuroraListView)getView().findViewById(android.R.id.list);  


		Animation animation=AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_in);
		LayoutAnimationController lac= new LayoutAnimationController(animation);
		lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
		lac.setDelay(0.05f);        
		mListView.setLayoutAnimation(lac);

		//		mToolBar=(RelativeLayout)mLayout.findViewById(R.id.toolBar);
		//		leftTextView=(TextView)mLayout.findViewById(R.id.left_title);
		//		middleTextView=(TextView)mLayout.findViewById(R.id.middle_title);
		//		rightTextView=(TextView)mLayout.findViewById(R.id.right_title);


		/*mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
        if (mGotoSearchLayout != null) {
            mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                	showSearchView();
                }
            });
        }
        getListView().addHeaderView(mGotoSearchLayout);*/

		mGotoPrivacyContactsLayout = (LinearLayout) inflater.inflate(R.layout.aurora_goto_privacy_contact, null);
		mGotoPrivacyContactsTv = (AuroraTextView) mGotoPrivacyContactsLayout.findViewById(
				R.id.goto_privacy_title);
		if (null != mGotoPrivacyContactsLayout) {
			mGotoPrivacyContactsLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mIsRemoveMemberMode) {
						return;
					}

					Intent intent = new Intent("com.aurora.privacymanage.GOTO_CONTACT_PRIVACY_MODULE");
					startActivity(intent);
				}
			});

			getListView().addHeaderView(mGotoPrivacyContactsLayout, null, false);
		}

		mGotoGroupLayout = (View) LayoutInflater.from(mContext).inflate(
				com.aurora.R.layout.aurora_slid_listview, null);
		RelativeLayout mainUi = (RelativeLayout) mGotoGroupLayout
				.findViewById(com.aurora.R.id.aurora_listview_front);
		mainUi.addView(inflater.inflate(R.layout.aurora_contact_list_goto_group, null), 0, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		View paddingView=mGotoGroupLayout.findViewById(com.aurora.R.id.control_padding);
		paddingView.setPadding(0, 0, 0, 0);

		//		contacts_count_rl=mGotoGroupLayout.findViewById(R.id.contacts_count_rl);
		getListView().addHeaderView(mGotoGroupLayout, null, false);
		mGotoGroup = (RelativeLayout) mGotoGroupLayout.findViewById(
				R.id.aurora_group_entrance);
		//		contactsCountTextView=(TextView) mGotoGroupLayout.findViewById(R.id.contacts_count);
		mGotoGroupLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (mIsRemoveMemberMode) {
					return;
				}
				Log.d(TAG, "startActivity:com.aurora.action.group");
				Intent intent = new Intent("com.aurora.action.group");
				startActivity(intent);
				return;
				//				getActivity().overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter, com.aurora.R.anim.aurora_activity_open_exit);
			}
		});
		mGotoGroupTv = (AuroraTextView) mGotoGroupLayout.findViewById(
				R.id.group_title);

		mProgress = (ProgressBar) getView().findViewById(
				R.id.progress_loading_contact);
		mProgress.setVisibility(View.GONE);
		getListView().setVisibility(View.GONE);
		if (null != mAlphbetIndexView) {
			mAlphbetIndexView.setVisibility(View.GONE);
		}

		if(footer==null) footer = inflater.inflate(R.layout.contactlist_header_view, null);
		mListView.addFooterView(footer, null, false);
		mListView.setFooterDividersEnabled(false);
		contactsCountHeader=(TextView)mListView.findViewById(R.id.contacts_count);

		getListView().setFastScrollEnabled(false);
		getListView().setFastScrollAlwaysVisible(false);
		getListView().setOnCreateContextMenuListener(this);


		/*getListView().auroraSetDeleteItemListener(new AuroraListView.AuroraDeleteItemListener() {

			@Override
			public void auroraDeleteItem(View v,int position) {
				// Aurora xuyong 2015-08-15 added for bug #15615 start
				if (mListView != null) {
					mListView.auroraSetNeedSlideDelete(false);
				}
				// Aurora xuyong 2015-08-15 added for bug #15615 end
				StatisticsUtil.getInstance(mContext.getApplicationContext()).report(StatisticsUtil.Contact_Delete);
				final int pos = getRightPosition(position);
				int rawContactId = getAdapter().getRawcontactId(pos);
				int indicateId = getAdapter().getIndicatePhoneSim(pos);
				int simIndex = getAdapter().getSimIndex(pos);
				Log.d(TAG, "position = " + position + "  rawContactId = " + rawContactId + "  indicateId = " + indicateId);
				if (pos < mStarredCount && !mIsAuroraSearchMode) {
					final Uri contactUri = getAdapter().getContactUri(pos);
					if (contactUri != null) {
						Intent intent = ContactSaveService.createSetStarredIntent(
								getContext(), contactUri, false);
						getContext().startService(intent);
					}

					CharSequence toastChar = ContactsApplication.getInstance()
							.getResources().getString(R.string.aurora_remove_starred_toast);
					Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
							toastChar,Toast.LENGTH_SHORT).show();

				} else {
					if (indicateId >= 0) {

						if (GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
							Toast.makeText(
									mContext,
									R.string.aurora_sim_not_ready,
									Toast.LENGTH_SHORT).show();
							getAdapter().notifyDataSetChanged();
							return;
						}
						//aurora add zhouxiaobing 20131228 start
						String number = null;
						String name = null;
						final String[] projection = new String[] {
								Contacts._ID, 
								Contacts.Data.MIMETYPE, 
								Contacts.Data.DATA1,
						};

						Cursor dataCursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
								projection, Data.RAW_CONTACT_ID + " =? ", 
								new String[] { String.valueOf(rawContactId) }, null);
						if (dataCursor != null && dataCursor.moveToFirst()) {
							do {
								if (dataCursor.getString(1).equals(Phone.CONTENT_ITEM_TYPE)) {
									number = dataCursor.getString(2);
								} else if (dataCursor.getString(1).equals(StructuredName.CONTENT_ITEM_TYPE)) {
									name = dataCursor.getString(2);
								}

							} while (dataCursor.moveToNext());

							dataCursor.close();
							dataCursor = null;
							Log.v(TAG, "tag = " + name + "  number = " + number + "   uri = " + SubContactsUtils.getUri(0));
							int result = 0;
							try {
								if (Build.VERSION.SDK_INT < 21) {
									String s="鳚";
									name=name.replace(' ', s.toCharArray()[0]);//aurora add zhouxiaobing 20140305 for delete space
									result = mContext.getContentResolver().delete(SubContactsUtils.getUri(0)SimCardUtils.getSimContactsUri(SIMInfo.getSlotById(mContext,indicateId), false),//aurora change zhouxiaobing 20140421 
											"tag=" + name + " AND " + "number=" + number, null);
								} else {

									// Aurora xuyong 2015-07-13 modified for bug #14161 start
									result = mContext.getContentResolver().delete(SimCardUtils.getSimContactsUri((int)indicateId, SimCardUtils.isSimUsimType(SIMInfo.getSlotById(mContext, indicateId))),//aurora change zhouxiaobing 20140421 
											"index=" + simIndex, null);
									// Aurora xuyong 2015-07-13 modified for bug #14161 end
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							//                            if (result <= 0) {
							//                                Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
							//                                        R.string.delete_error, Toast.LENGTH_SHORT).show();
							//                                getAdapter().notifyDataSetChanged();
							//                                return;
							//                            }
						} else {
							if(dataCursor != null) {
								dataCursor.close();
							}
						}
						//aurora add zhouxiaobing 20131228 end  
					}

					int dbdelete = mContext.getContentResolver().delete(
							RawContacts.CONTENT_URI, 
							RawContacts._ID + " = " + rawContactId + " and deleted = 0", 
							null);

					if (dbdelete > 0) {
						CharSequence toastChar = ContactsApplication.getInstance().
								getResources().getString(R.string.aurora_delete_one_contact_toast, 
										getAdapter().getContactDisplayName(pos));
						Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
								toastChar, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
								R.string.delete_error, Toast.LENGTH_SHORT).show();
						getAdapter().notifyDataSetChanged();

					}
				}
			}
		});*/
		/*getListView().auroraSetAuroraBackOnClickListener(
				new AuroraListView.AuroraBackOnClickListener() {

					@Override
					public void auroraOnClick(final int position) {
						final int pos = getRightPosition(position);
						int title = 0;
						String message = "";
						if (pos < mStarredCount && !mIsAuroraSearchMode) {
							title = R.string.aurora_remove_star_title;
							message = ContactsApplication.getInstance().
									getResources().getString(R.string.aurora_remove_star_message, 
											getAdapter().getContactDisplayName(pos));
						} else {
							title = R.string.delete;
							String name = getAdapter().getContactDisplayName(pos);
							message = ContactsApplication.getInstance().
									getResources().getString(R.string.aurora_delete_one_contact_message, name);
						}

						mDeleteConDialog = new AuroraAlertDialog.Builder(mContext,
								AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
						.setTitle(title)
						.setMessage(message)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								AuroraListView listView=getListView();
								if(listView!=null){
									listView.auroraDeleteSelectedItemAnim();
									getAdapter().setAuroraListDelet(true);
									ContactsApplication.getInstance().sendSyncBroad();
								}
							}
						}).create();

						mDeleteConDialog.show();
						Log.d(TAG, "position = " + position + "  message = " + message);
					}

					@Override
					public void auroraPrepareDraged(int position) {
						if (mAlphbetIndexView != null && mAlphbetIndexView.isShown() && mContext != null) {
							Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.aurora_contact_prompt_exit);
							mAlphbetIndexView.startAnimation(animation);
							mAlphbetIndexView.setVisibility(View.GONE);
						}
					}

					@Override
					public void auroraDragedSuccess(int position) {
						if (mAlphbetIndexView != null && mAlphbetIndexView.isShown() && mContext != null) {
							Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.aurora_contact_prompt_exit);
							mAlphbetIndexView.startAnimation(animation);
							mAlphbetIndexView.setVisibility(View.GONE);
						}
						//                        if (mAlphbetIndexView != null) {
						//                            mAlphbetIndexView.setVisibility(View.GONE);
						//                        }
					}

					@Override
					public void auroraDragedUnSuccess(int position) {
						if (mAlphbetIndexView != null && mContext != null) {
							Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.aurora_contact_prompt_enter);
							mAlphbetIndexView.startAnimation(animation);
							mAlphbetIndexView.setVisibility(View.VISIBLE);
							mAlphbetIndexView.invalidate();
						}
					}
				});*/

		//        getListView().setOnItemLongClickListener(this);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onDestroy");
		if (null != mAccountChangeReceiver) {
			try{
				mContext.unregisterReceiver(mAccountChangeReceiver);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		if (mContactListFilterController != null) {
			mContactListFilterController.removeListener(this);
		}
		mNeedCreateDialerTempTable = true;

		//		try{
		//			mContext.unregisterReceiver(myBroadCastReceiver);
		//			myBroadCastReceiver=null;
		//		}catch(Exception e){
		//		}

		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		mProviderStatusLoader.setProviderStatusListener(null);

		if (getListView() != null) {
			try {
				getListView().auroraSetHeaderViewYOffset(-10000);
				getListView().auroraOnPause();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	@Override
	public void onProviderStatusChange() {
		Log.d(TAG, "[PeopleActivity -> onProviderStatusChange()]");
	}

	public void setCurrentFilterIsValid(boolean currentFilterIsValid){
		mCurrentFilterIsValid = currentFilterIsValid;
	}

	public void onStop(){
		mCurrentFilterIsValid = false;
		super.onStop();
	}

	@Override
	protected void setSearchMode(boolean flag) {
		super.setSearchMode(flag);

		int visibility = flag ? View.GONE : View.VISIBLE;
		final AbsListIndexer aiv = (AbsListIndexer) getView().findViewById(
				R.id.gn_alphbet_indexer);
		if (null != aiv) {
			aiv.setVisibility(visibility);
		}
	}


	@Override
	protected void showCount(int partitionIndex, Cursor data) {

	}

	@Override
	protected void setProfileHeader() {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onLoadFinished   AuroraDefaultContactBrowseListFragment");

		super.onLoadFinished(loader, data);
		isFinished = true;
		mProgress.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);


		//((AuroraPeopleActivity) getActivity()).gnCheckIsNoContacts();
		if (mIsAuroraSearchMode&&!TextUtils.isEmpty(queryString)) {
			auroraNoMatchView(true, null);
			return;
		} else {
			setHeadersVisOrInVis(true);
			// Aurora xuyong 2015-08-15 added for bug #15615 start
			if (mListView != null) {
				mListView.auroraSetNeedSlideDelete(false);
			}
			// Aurora xuyong 2015-08-15 added for bug #15615 end
		}

		mIsInPrivacyMode = ContactsApplication.sIsAuroraPrivacySupport
				&& AuroraPrivacyUtils.mCurrentAccountId > 0 ? true : false;
				Log.i(TAG, "mIsInPrivacyMode = " + mIsInPrivacyMode + "   AuroraPrivacyUtils.mCurrentAccountId = " + AuroraPrivacyUtils.mCurrentAccountId);
				if (!mIsInPrivacyMode && null != mGotoPrivacyContactsLayout) {
					getListView().removeHeaderView(mGotoPrivacyContactsLayout);
				}

				if (data != null) {
					gnCheckShowEmptyView(data.getCount());

					if (!getRemoveMemberMode()) {
						getListView().auroraEnableSelector(true);
					} else {
						if (data.getCount() == 0) {
							dismissActionBar();
							changeToNormalMode(true);
						}
					}
				} else {
					gnCheckShowEmptyView(0);
				}
				//				boolean isShown = AuroraDialActivity.mSearchView == null ? false : AuroraDialActivity.mSearchView.isShown();
				//				Log.d(TAG,"AuroraDialActivity.mSearchView.isShown():"+ isShown +" queryString:"+queryString);
				if(mIsAuroraSearchMode&&TextUtils.isEmpty(queryString)){
					Log.d(TAG,"hideListView2");
					getListView().setVisibility(View.INVISIBLE);		
					if (null != mAlphbetIndexView) {
						mAlphbetIndexView.setVisibility(View.GONE);
					}
				}else{
					getListView().setVisibility(View.VISIBLE);
					if (null != mAlphbetIndexView) {
						mAlphbetIndexView.setVisibility(View.VISIBLE);
					}
				}
	}

	public void dismissActionBar() {
		if (mActionBar != null) {
			mActionBar.setShowBottomBarMenu(false);
			mActionBar.showActionBarDashBoard();
		}
	}

	private ProgressBar mProgress;
	public static boolean isFinished = false;
	private static final int WAIT_CURSOR_START = 1230;
	private static final int SHOW_EMPTY_VIEW = 1231;
	private static final long WAIT_CURSOR_DELAY_TIME = 500;
	private ContactsUtils.AuroraContactsProgressDialog mDeleteProgressDialog;
	private static final int START_DELETE = 0;
	private static final int REFRESH_DELETE = 1;
	private static final int END_DELETE = 2;
	private static final int PREPARE_IMPORT = 3;
	private static final int START_IMPORT = 4;
	private static final int REFRESH_IMPORT = 5;
	private static final int END_IMPORT = 6;
	private static final int SIM_IS_EMPTY = 7;

	private static final int MAX_OP_COUNT_IN_ONE_BATCH = 100;
	public static boolean mIsImportSimContact = false;

	public static final int AURORA_SIM_CONTACTS=10;//aurora add zhouxiaobing 20131216

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_EMPTY_VIEW:{
				gnCheckShowEmptyViewForHandler((msg.arg1==1)?true:false);
				break;
			}
			case WAIT_CURSOR_START:
				Log.i(TAG, "start WAIT_CURSOR_START !isFinished : "
						+ !isFinished);
				if (!isFinished) {
					mProgress.setVisibility(View.VISIBLE);
					getListView().setVisibility(View.GONE);
					if (null != mAlphbetIndexView) {
						mAlphbetIndexView.setVisibility(View.GONE);
					}
				}
				break;
			case START_DELETE: {
				mContactsIsDeleting = true;
				if (mDeleteProgressDialog == null) {
					mDeleteProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
				}

				mDeleteProgressDialog.setTitle(R.string.multichoice_confirmation_title_delete);
				mDeleteProgressDialog.setIndeterminate(false);
				mDeleteProgressDialog.setMax(msg.arg2);
				mDeleteProgressDialog.setProgress(0);
				mDeleteProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_HORIZONTAL);
				try {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mDeleteProgressDialog.show();
							Log.e("wangth", "START_DELETE, DeleteProgressDialog  show");
						}
					});
				} catch (Exception e) {
					Log.e("wangth", "START_DELETE, DeleteProgressDialog  show error");
					e.printStackTrace();
				}

				break;
			}

			case REFRESH_DELETE: {
				mContactsIsDeleting = true;
				try {
					mDeleteProgressDialog.setTitle(R.string.multichoice_confirmation_title_delete);
					mDeleteProgressDialog.setMax(msg.arg2);
					mDeleteProgressDialog.setProgress(msg.arg1);
					mDeleteProgressDialog.setIndeterminate(false);
					if(!mDeleteProgressDialog.isShowing()){
						Log.e("wangth", "REFRESH_DELETE, DeleteProgressDialog  refresh error, mDeleteProgressDialog is not showing");
						mDeleteProgressDialog.show();
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("wangth", "REFRESH_DELETE, DeleteProgressDialog  refresh error");

					mDeleteProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
					mDeleteProgressDialog.setTitle(R.string.multichoice_confirmation_title_delete);
					mDeleteProgressDialog.setIndeterminate(false);
					mDeleteProgressDialog.setMax(msg.arg2);
					mDeleteProgressDialog.setProgress(msg.arg1);
					mDeleteProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_HORIZONTAL);
					try {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mDeleteProgressDialog.show();
								Log.e("wangth", "REFRESH_DELETE, DeleteProgressDialog  show again");
							}
						});
					} catch (Exception ex) {
						Log.e("wangth", "REFRESH_DELETE, DeleteProgressDialog show error  again");
						ex.printStackTrace();
					}
				}

				break;
			}

			case END_DELETE: {
				mContactsIsDeleting = false;
				try {
					mDeleteProgressDialog.dismiss();
					mDeleteProgressDialog = null;
				} catch (Exception e) {
					Log.e("wangth", "END_DELETE, DeleteProgressDialog  dismiss error");
				}

				Toast.makeText(mContext, 
						mContext.getString(R.string.notifier_finish_delete_content, msg.arg1), 
						Toast.LENGTH_SHORT)
						.show();

				changeToNormalMode(false);
				ContactsApplication.getInstance().sendSyncBroad();

				contactsCount-=msg.arg1;

				contactsCountHeader.setText(mContext.getResources().getString(R.string.contacts_count,contactsCount));




				break;
			}

			case PREPARE_IMPORT: {
				findSimContactsDialog();
				break;
			}

			case SIM_IS_EMPTY: {
				AuroraAlertDialog ad=new AuroraAlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.aurora_menu_import_contacts))
						.setMessage(mContext.getString(R.string.aurora_sim_no_contacts))   
						.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						}).create();
				ad.show();
				break;
			}

			case START_IMPORT: {
				Toast.makeText(mContext, R.string.aurora_start_daoru, Toast.LENGTH_SHORT).show();
				AuroraContactImportExportActivity.is_importexporting_sim=true;//aurora change zhouxiaobing 20131211
				break;
			}

			case REFRESH_IMPORT: {
				break;
			}

			case END_IMPORT: {
				Toast.makeText(mContext, R.string.aurora_end_daoru, Toast.LENGTH_SHORT).show();//aurora change zhouxiaobing 20131211
				AuroraContactImportExportActivity.is_importexporting_sim=false;
				break;
			}

			default:
				break;
			}
		}
	};

	private void findSimContactsDialog() {
		final Cursor simCursor = mContext.getContentResolver().query(
				RawContacts.CONTENT_URI, null,
				RawContacts.INDICATE_PHONE_SIM + "> 0", null, null);
		if (simCursor != null) {
			simCursor.moveToFirst();

			if (simCursor.getCount() > 0) {
				AuroraAlertDialog ad = new AuroraAlertDialog.Builder(mContext)  // aurora change zhouxiaobing 20131223
				.setTitle(R.string.aurora_import_sim_title)
				.setMessage(this.getResources().getString(R.string.aurora_find_sim_contacts_message, simCursor.getCount()))
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub
						simCursor.close();
					}
				})
				.setPositiveButton(R.string.aurora_import_title,
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub

						sendMessage(mHandler, START_IMPORT, 0, 0);
						long ids[] = new long[simCursor.getCount()];
						for (int i = 0; i < ids.length; i++) {
							ids[i] = simCursor.getInt(simCursor.getColumnIndexOrThrow("contact_id"));
							simCursor.moveToNext();
						}
						simCursor.close();

						ImportThread it = new ImportThread(ids);
						it.start();
					}
				}).create();
				ad.setCanceledOnTouchOutside(false);
				ad.show();
			} else {
				AuroraAlertDialog ad = new AuroraAlertDialog.Builder(
						mContext)
				.setTitle(mContext.getString(R.string.aurora_menu_import_contacts))
				.setMessage(mContext.getString(R.string.aurora_sim_no_contacts))
				.setNegativeButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub
					}
				}).create();
				ad.show();
				simCursor.close();
			}
		} else {
			AuroraAlertDialog ad=new AuroraAlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.aurora_menu_import_contacts))
					.setMessage(mContext.getString(R.string.aurora_sim_no_contacts))   
					.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					}).create();  
			ad.show();        	
		}
	}

	//aurora add zhouxiaobing 20131211 start
	public void startnotifySimImport(int jobid,int currentCount,int totalCount) {
		String description=mContext.getString(R.string.aurora_start_daoru);
		final Notification notification = AuroraContactImportExportActivity.constructProgressNotification(
				mContext.getApplicationContext(), 0, description,
				totalCount, currentCount);
		((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(AuroraContactImportExportActivity.DEFAULT_NOTIFICATION_TAG, jobid, notification);   	
	}

	public void endnotifySimImport(int jobid) {
		final Notification notification = AuroraContactImportExportActivity.constructfinishNotification(
				mContext.getApplicationContext(), 0);
		((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(AuroraContactImportExportActivity.DEFAULT_NOTIFICATION_TAG, jobid, notification);     	
	}
	//aurora add zhouxiaobing 20131211 end

	private final static String[] DATA_ALLCOLUMNS = new String[] {
		Data._ID,
		Data.MIMETYPE,
		Data.IS_PRIMARY,
		Data.IS_SUPER_PRIMARY,
		Data.DATA1,
		Data.DATA2,
		Data.DATA3,
		Data.DATA4,
		Data.DATA5,
		Data.DATA6,
		Data.DATA7,
		Data.DATA8,
		Data.DATA9,
		Data.DATA10,
		Data.DATA11,
		Data.DATA12,
		Data.DATA13,
		Data.DATA14,
		Data.DATA15,
		Data.SYNC1,
		Data.SYNC2,
		Data.SYNC3,
		Data.SYNC4,
		Data.IS_ADDITIONAL_NUMBER
	};

	private class ImportThread extends Thread {
		private long[] adArray;
		public ImportThread(long[] adArray) {
			this.adArray = adArray;
		}

		@Override
		public void run() {
			int successfulItems = 0;
			int totalItems = 0;
			final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

			for (long id : adArray) {
				Cursor dataCursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
						DATA_ALLCOLUMNS, Data.RAW_CONTACT_ID + " =? ", 
						new String[] { String.valueOf(id) }, null);
				if (dataCursor == null || dataCursor.getCount() <= 0) {
					if(dataCursor != null) {
						dataCursor.close();
					}
					continue;
				}

				int backRef = operationList.size();
				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newInsert(RawContacts.CONTENT_URI);
				builder.withValue(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
				builder.withValue(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
				operationList.add(builder.build());

				dataCursor.moveToPosition(-1);
				String[] columnNames = dataCursor.getColumnNames();
				while (dataCursor.moveToNext()) {
					String mimeType = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
					if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
						continue;
					}

					builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);

					for (int i = 1; i < columnNames.length; i++) {
						cursorColumnToBuilder(dataCursor, columnNames, i, builder);
					}

					builder.withValueBackReference(Data.RAW_CONTACT_ID, backRef);
					operationList.add(builder.build());
				}

				dataCursor.close();
				successfulItems++;

				sendMessage(mHandler, REFRESH_IMPORT, successfulItems, adArray.length);

				if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
					try {
						mContext.getContentResolver().applyBatch(GnContactsContract.AUTHORITY, operationList);
					} catch (android.os.RemoteException e) {
						e.printStackTrace();
					} catch (android.content.OperationApplicationException e) {
						e.printStackTrace();
					}
					operationList.clear();
				}
				startnotifySimImport(0,successfulItems,adArray.length);//aurora add zhouxiaobing 20131211
			}

			if (operationList.size() > 0) {
				try {
					mContext.getContentResolver().applyBatch(GnContactsContract.AUTHORITY, operationList);
				} catch (android.os.RemoteException e) {
				} catch (android.content.OperationApplicationException e) {
				}
				operationList.clear();
			}
			endnotifySimImport(0);//aurora add zhouxiaobing 20131211
			sendMessage(mHandler, END_IMPORT, adArray.length, adArray.length);
		}
	}

	private void cursorColumnToBuilder(Cursor cursor, String[] columnNames,
			int index, ContentProviderOperation.Builder builder) {
		switch (cursor.getType(index)) {
		case Cursor.FIELD_TYPE_NULL:
			// don't put anything in the content values
			break;
		case Cursor.FIELD_TYPE_INTEGER:
			builder.withValue(columnNames[index], cursor.getLong(index));
			break;
		case Cursor.FIELD_TYPE_STRING:
			builder.withValue(columnNames[index], cursor.getString(index));
			break;
		case Cursor.FIELD_TYPE_BLOB:
			builder.withValue(columnNames[index], cursor.getBlob(index));
			break;
		default:
			throw new IllegalStateException("Invalid or unhandled data type");
		}
	}

	private void sendMessage(Handler handler, int what, int arg1, int arg2) {
		if (null != handler) {
			Message msg = Message.obtain();
			msg.what = what;
			msg.arg1 = arg1;
			msg.arg2 = arg2;
			handler.sendMessage(msg);
		}
	}

	private LinearLayout mLayout;
	private FrameLayout mNoContactsFra;
	private FrameLayout mNoContactsEmptyView;
	private FrameLayout mMainContent;

	private boolean mIsShowAlphbetIndexView = true;

	@Override
	public void onResume() {
		super.onResume();
		mProviderStatusLoader.setProviderStatusListener(this);
		getListView().auroraOnResume();

		if (mIsImportSimContact) {
			mIsImportSimContact = false;
			showSimContacts(true);
		}
	}

	private void showSimContacts(final boolean isImport) {
		mHandler.sendEmptyMessage(PREPARE_IMPORT);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		super.onScrollStateChanged(view, scrollState);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

		if (null != mAlphbetIndexView) {
			mAlphbetIndexView.invalidateShowingLetterIndex();
		}

		//        if (mIsAuroraSearchMode || getListView() == null) {
		//        	return;
		//        }

		//        int lP = getListView().getLastVisiblePosition() - getListView().getFirstVisiblePosition();
		//        View v = getListView().getChildAt(lP);
		//        if (v != null) {
		//        	if (v.getBottom() <= getListView().getBottom()) {
		//        		getListView().auroraSetHeaderViewYOffset(0);
		//        		return;
		//        	}
		//        }

		//        int vP = getListView().getFirstVisiblePosition();
		//        if (vP < 2) {
		//        	getListView().auroraSetHeaderViewYOffset(-1000);
		//        	ContactsUtils.mListHeaderTop = 0;
		//        	ContactsUtils.mListHeaderY = 0;
		//        } else {
		//        	int headerHight = mContext.getResources().getDimensionPixelSize(
		//                    R.dimen.aurora_edit_group_margin_top);
		//    		
		//        	if (ContactsUtils.mListHeaderY == 0) {
		//        		getListView().auroraSetHeaderViewYOffset(headerHight);
		//        	} else {
		//        		getListView().auroraSetHeaderViewYOffset(headerHight + ContactsUtils.mListHeaderTop);
		//        	}
		//        }
	}

	private void gnCheckShowEmptyViewForHandler(boolean hasInvisibleContacts){
		Log.d(TAG, "hasInvisibleContacts:"+hasInvisibleContacts);

		View importFromAcount=null;
		if (null == mNoContactsEmptyView) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			mNoContactsEmptyView = (FrameLayout) inflater.inflate(R.layout.aurora_contacts_list_empty_view, mNoContactsFra);

		}

		if (null != mNoContactsEmptyView) {
			importFromAcount = mNoContactsEmptyView.findViewById(R.id.empty_view_import_from_acount);

			importFromAcount.setFadingEdgeLength(0);
			if(ContactsApplication.sIsChinaProduct){
				importFromAcount.setVisibility(View.VISIBLE);
				importFromAcount.setOnClickListener(mClickListener);
			} else {
				importFromAcount.setVisibility(View.INVISIBLE);
				importFromAcount.setOnClickListener(null);
			}
			mNoContactsEmptyView.setVisibility(View.VISIBLE);

		}

		if (null != mSearchView) {
			mSearchView.setVisibility(View.GONE);
		}

		setHeadersVisOrInVis(false);

		if (null != mAlphbetIndexView) {
			mAlphbetIndexView.setVisibility(View.GONE);
		}



		//			TextView tv = (TextView)mNoContactsEmptyView.findViewById(R.id.empty_text);
		TextView importFromSD = (TextView) mNoContactsEmptyView.findViewById(R.id.empty_view_import_from_sdcard);
		TextView tips= (TextView)mNoContactsEmptyView.findViewById(R.id.gn_calllog_empty_tip_text);
		importFromSD.setOnClickListener(mClickListener);
		if (hasInvisibleContacts) {
			//				tv.setText(R.string.aurora_no_contacts_show);
			importFromSD.setText(R.string.aurora_show_contacts_accounts);
			importFromAcount.setVisibility(View.INVISIBLE);
			tips.setText(R.string.empty_contacts_tips);
			mHasInvisibleContacts = true;
		} else {
			mHasInvisibleContacts = false;
			importFromAcount.setVisibility(View.VISIBLE);
			tips.setText(R.string.aurora_empty_contact_text);
			importFromSD.setText(R.string.aurora_menu_import);
		}

		if (mListView != null) {
			mListView.auroraSetHeaderViewYOffset(-10000);
			mListView.auroraShowHeaderView();

		}

		//            ((AuroraActivity)getActivity()).setMenuEnable(false);

	}

	boolean hasInvisibleContacts = false;
	private void gnCheckShowEmptyView(int contactsCount) {
		Log.i(TAG, "contactsCount = " + contactsCount);
		if (0 == contactsCount) {
			new Thread(){
				public void run(){					
					Cursor c = null;
					try {
						c = mContext.getContentResolver().query(RawContacts.CONTENT_URI, null, "deleted=0", null, null);
						if (c != null) {
							if (c.getCount() > 0) {
								hasInvisibleContacts = true;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (c != null) {
							c.close();
						}
					}

					Message message=mHandler.obtainMessage(SHOW_EMPTY_VIEW);
					message.arg1=hasInvisibleContacts?1:0;
					mHandler.sendMessage(message);
				}
			}.start();

		} else {
			if (null != mNoContactsEmptyView) {
				mNoContactsEmptyView.setVisibility(View.GONE);
			}

			if (mAlphbetIndexView == null && getActivity() != null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				FrameLayout rl = (FrameLayout) inflater.inflate(R.layout.gn_alphbet_indexer, mMainContent);
				if (rl != null) {
					mAlphbetIndexView = (AbsListIndexer)rl.findViewById(R.id.gn_alphbet_indexer);
				}
			}

			if (null != mSearchView) {
				mSearchView.setVisibility(View.VISIBLE);
			}

			setHeadersVisOrInVis(true);

			if (null != mAlphbetIndexView) {
				mAlphbetIndexView.setVisibility(View.VISIBLE);
				mAlphbetIndexView.setList(getListView(), this);
			}

			mItemCount = getAdapter().getCount();
			//            mStarredCount = ContactsUtils.getStarredCount(mContext);
			//            Log.e(TAG, "mStarredCount = " + mStarredCount);

			if (!mIsAuroraSearchMode) {
				mItemCount = mItemCount - mStarredCount;
			}

			if (mItemCount > 0 && mIsRemoveMemberMode) {
				if (null != getActivity()) {
					updateSelectedItemsView(mItemCount);
				}
			}

			(AuroraDialActivity.auroraDialActivity).setMenuEnable(true);
		}
	}

	public void updateSelectedItemsView(int allCount) {
		int count = allCount;
		int curArray = getAdapter().getCheckedItem().size();
		Log.d(TAG, "curArray = " + curArray + "  count = " + count);

		try {
			if (curArray >= count) {
				((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
			} else {
				((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (curArray > 0) {
			setBottomMenuEnable(true);
		} else {
			setBottomMenuEnable(false);
		}

		setRightBtnTv(curArray);

		//		showLeftRight();

		middleTextView.setText(mContext.getString(R.string.selected_total_num, getAdapter().getSelectedCount()));


	}

	public void setBottomMenuEnable(boolean flag) {
		AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
		auroraMenu.setBottomMenuItemEnable(1, flag);
	}

	public void setRightBtnTv (int checkCount) {
		AuroraActivity auroraActivity = AuroraDialActivity.auroraDialActivity;
		Button conBut = auroraActivity.getSearchViewRightButton();
		if (conBut != null) {
			String str = mContext.getResources().getString(R.string.cancel);
			boolean boo = getAdapter().getCheckBoxEnable();
			if (boo) {
				str = mContext.getResources().getString(R.string.aurora_search_continue);
			}

			conBut.setText(str);
		}
	}

	private void setQueryTextToFragment(String query) {
		setSearchMode(mIsAuroraSearchMode);
		setQueryString(query, true);
		setVisibleScrollbarEnabled(!mIsAuroraSearchMode);
	}

	private String queryString=null;
	/*private final class svQueryTextListener implements aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener {
		@Override
		public boolean onQueryTextChange(String queryString) {
			if (queryString.length() > 0) {
//				if (mGotoPrivacyContactsLayout != null && mGotoPrivacyContactsLayout.isShown()
//						&& ContactsApplication.sIsAuroraPrivacySupport
//						&& mIsInPrivacyMode) {
//					getListView().removeHeaderView(mGotoPrivacyContactsLayout);
//				}
//
//				if (mGotoGroupLayout != null && mGotoGroupLayout.isShown()) {
//					getListView().removeHeaderView(mGotoGroupLayout);
//				}

//				getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
				mIsAuroraSearchMode = true;
				getAdapter().setAuroraListDelet(false);
				setQueryTextToFragment(queryString);

				if (checkIsNeedQueryFromDialer(queryString)) {
					auroraNoMatchView(mIsAuroraSearchMode, null);
				}
			} else {
//				if (mGotoPrivacyContactsLayout != null && !mGotoPrivacyContactsLayout.isShown() 
//						&& ContactsApplication.sIsAuroraPrivacySupport
//						&& mIsInPrivacyMode) {
//					getListView().addHeaderView(mGotoPrivacyContactsLayout, null, false);
//				}
//
//				if (mGotoGroupLayout != null && !mGotoGroupLayout.isShown()) {
//					getListView().addHeaderView(mGotoGroupLayout, null, false);
//				}

				getListView().setAdapter(getAdapter());

				if (mGotoSearchLayout != null) {
					mGotoSearchLayout.setVisibility(View.VISIBLE);
				}

				getListView().auroraSetHeaderViewYOffset(0);
				mIsAuroraSearchMode = false;
				getAdapter().setAuroraListDelet(true);
				setQueryTextToFragment("");

				auroraNoMatchView(false, null);
			}

			getAdapter().setSearchMode(mIsAuroraSearchMode);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return true;
		}
	}*/

	private int getRightPosition(int position) {
		if (mIsAuroraSearchMode) {
			return position;
		}

		if (mIsInPrivacyMode) {
			return position - 3;
		}

		if(mIsEditMode){
			return position;
		}

		return position - 1;
	}

	private void setHeadersVisOrInVis(boolean flag) {
		int vis = flag ? View.VISIBLE : View.GONE;

		if (mGotoGroup != null) {
			mGotoGroup.setVisibility(vis);
		}

		if (mGotoGroupLayout != null) {
			mGotoGroupLayout.setVisibility(vis);
		}

		if (mGotoPrivacyContacts != null) {
			mGotoPrivacyContacts.setVisibility(vis);
		}

		if (mGotoPrivacyContactsLayout != null) {
			mGotoPrivacyContactsLayout.setVisibility(vis);
		}
	}

	//	public View mFooterView;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		//		if(getActivity() != null && getActivity().getParent() == null) {
		//			mFooterView.setVisibility(View.GONE);
		//		}
	}

	public AuroraListView mListView;
	public void animationAfterSetTab() {
		Animation in =AnimationUtils.loadAnimation(mContext, R.anim.aurora_alpha_in);
		//			this.getContentView().startAnimation(in);
		mActionBar.startAnimation(in);
		Animation viewIn =AnimationUtils.loadAnimation(mContext, R.anim.aurora_alpha_in);
		viewIn.setFillAfter(true);
		getView().startAnimation(viewIn);
		mListView.startLayoutAnimation();
	}

	public void animationBeforeSetTab(){
		Animation out =AnimationUtils.loadAnimation(mContext, R.anim.aurora_alpha_out);
		Animation viewOut =AnimationUtils.loadAnimation(mContext, R.anim.aurora_alpha_out);
		mActionBar.startAnimation(out);
		viewOut.setFillAfter(true);
		getView().startAnimation(viewOut);
	}

	//	boolean isLongClickEnable=true;
	//	@Override
	//	public boolean onItemLongClick(AdapterView<?> arg0, View view, int position,
	//			long arg3) {
	// TODO Auto-generated method stub

	//		Log.d(TAG,"onItemLongClick view:"+view+" getId():"+view.getId()+" position:"+position);



	/*if (!isLongClickEnable) {
			return false;
		}

		int realPosition = getRightPosition(position);
        if (!mIsAuroraSearchMode && realPosition < mStarredCount) {
            return false;
        }

		RelativeLayout mainUi;
		ContactListItemView cliv = null;
		mainUi = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		if (mainUi != null) {
			View frontView = mainUi.getChildAt(0);
			if (frontView != null && frontView instanceof ContactListItemView) {
				cliv = (ContactListItemView)frontView;
			}
		}
		CheckBox checkBox=(CheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		if(cliv != null) {
			boolean checked = false;
			Log.d("liyang","test888");
			checkBox.setChecked(!checked);
			getAdapter().setCheckedItem(Long.valueOf(getAdapter().getContactID(realPosition)), String.valueOf(realPosition));


		}

//		else{
//			View rootView=(View) view.getParent().getParent().getParent().getParent().getParent().getParent();
//			CheckBox checkBox=(CheckBox) rootView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
//			if(checkBox==null) {return false;}
//
//			boolean checked = false;
//			Log.d("liyang","test999");
//			checkBox.setChecked(!checked);
//			//			cliv.mCheckBox.auroraSetChecked(!checked, true);
//			mAdapter.setCheckedArrayValue(position, !checked);			
//			if (mAdapter.isAllSelect()) {
//				updateMenuItemState(true);
//			}	
//		}


		switch2EditMode();
		setBottomMenuEnable(true);*/


	//		return false;
	//	}


	/*public void switch2EditMode() {
		((AuroraActivity) getActivity()).setMenuEnable(false);	
		//aurora change liguangyu 20131108 for BUG #511 start

		isLongClickEnable = false;
		initActionBar(true);
		setEditMode(true);
		mListView.auroraSetNeedSlideDelete(false);
		mListView.auroraEnableSelector(false);
		//aurora change liguangyu 20131108 for BUG #511 end
	}


	public void setEditMode(boolean is_edit) {
		editMode = is_edit;
		getAdapter().setEditMode(is_edit);
		mAdapter.setIs_listitem_changing(true);
		mAdapter.notifyDataSetChanged();

		if (editMode) {
			if(mIAuroraCallLogFragment != null){
				mIAuroraCallLogFragment.setTabWidget(View.INVISIBLE);
			}
			getActivity().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, mContactChangeObserver);
		} else {
			if(mIAuroraCallLogFragment != null){
				mIAuroraCallLogFragment.setTabWidget(View.VISIBLE);
			}
			mAdapter.clearAllcheckes();
			getActivity().getContentResolver().unregisterContentObserver(mContactChangeObserver);
		}
		if(mFooterView != null && !mIsPrivate) {
			mFooterView.setVisibility(editMode ? View.GONE : View.GONE);
		}
	}*/






}
