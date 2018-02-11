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

import java.util.Random;

import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.ContactListEmptyView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.model.AccountType;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.SharedPreferencesUtil;
import com.android.contacts.widget.AbsListIndexer;
import com.android.contacts.widget.ContextMenuAdapter;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.dialpad.DialerSearchUtils;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.IContentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import aurora.preference.AuroraPreferenceManager;

/**
 * Common base class for various contact-related list fragments.
 */
public abstract class ContactEntryListFragment<T extends ContactEntryListAdapter>
extends Fragment
implements OnItemClickListener, OnScrollListener, OnFocusChangeListener, OnTouchListener,
LoaderCallbacks<Cursor> {
	private static final String TAG = "liyang-ContactEntryListFragment";

	// TODO: Make this protected. This should not be used from the PeopleActivity but
	// instead use the new startActivityWithResultFromFragment API
	public static final int ACTIVITY_REQUEST_CODE_PICKER = 1;

	private static final String KEY_LIST_STATE = "liststate";
	private static final String KEY_SECTION_HEADER_DISPLAY_ENABLED = "sectionHeaderDisplayEnabled";
	private static final String KEY_PHOTO_LOADER_ENABLED = "photoLoaderEnabled";
	private static final String KEY_QUICK_CONTACT_ENABLED = "quickContactEnabled";
	private static final String KEY_INCLUDE_PROFILE = "includeProfile";
	private static final String KEY_SEARCH_MODE = "searchMode";
	private static final String KEY_VISIBLE_SCROLLBAR_ENABLED = "visibleScrollbarEnabled";
	private static final String KEY_SCROLLBAR_POSITION = "scrollbarPosition";
	private static final String KEY_QUERY_STRING = "queryString";
	private static final String KEY_DIRECTORY_SEARCH_MODE = "directorySearchMode";
	private static final String KEY_SELECTION_VISIBLE = "selectionVisible";
	private static final String KEY_REQUEST = "request";
	private static final String KEY_DARK_THEME = "darkTheme";
	private static final String KEY_LEGACY_COMPATIBILITY = "legacyCompatibility";
	private static final String KEY_DIRECTORY_RESULT_LIMIT = "directoryResultLimit";

	private static final String DIRECTORY_ID_ARG_KEY = "directoryId";

	private static final int DIRECTORY_LOADER_ID = -1;

	private static final int DIRECTORY_SEARCH_DELAY_MILLIS = 300;
	private static final int DIRECTORY_SEARCH_MESSAGE = 1;

	private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

	private boolean mSectionHeaderDisplayEnabled;
	private boolean mPhotoLoaderEnabled;
	private boolean mQuickContactEnabled = true;
	private boolean mIncludeProfile;
	private boolean mSearchMode;
	private boolean mVisibleScrollbarEnabled;
	private int mVerticalScrollbarPosition = View.SCROLLBAR_POSITION_RIGHT;
	public String mQueryString;
	private int mDirectorySearchMode = DirectoryListLoader.SEARCH_MODE_NONE;
	private boolean mSelectionVisible;
	private boolean mLegacyCompatibility;

	private boolean mEnabled = true;

	private T mAdapter;
	private View mView;
	private AuroraListView mListView;

	/**
	 * Used for keeping track of the scroll state of the list.
	 */
	private Parcelable mListState;

	private int mDisplayOrder;
	private int mSortOrder;
	private int mDirectoryResultLimit = DEFAULT_DIRECTORY_RESULT_LIMIT;

	private ContextMenuAdapter mContextMenuAdapter;
	private ContactPhotoManager mPhotoManager;
	private ContactListEmptyView mEmptyView;
	private ProviderStatusLoader mProviderStatusLoader;
	private ContactsPreferences mContactsPrefs;

	private boolean mForceLoad;

	private boolean mDarkTheme;

	protected boolean mUserProfileExists;

	private static final int STATUS_NOT_LOADED = 0;
	private static final int STATUS_LOADING = 1;
	private static final int STATUS_LOADED = 2;

	private int mDirectoryListStatus = STATUS_NOT_LOADED;

	/**
	 * Indicates whether we are doing the initial complete load of data (false) or
	 * a refresh caused by a change notification (true)
	 */
	private boolean mLoadPriorityDirectoriesOnly;

	private Context mContext;

	private LoaderManager mLoaderManager;

	// aurora <wangth> <2013-10-10> add for aurora ui begin
	private Cursor mCursor = null;
	public static final String QUERY_ABC_PREFIX = "auroracontactqueryfordialerprefix";
	private boolean mIsNoNeedQueryAbc = false;
	private boolean mIsNeedSlideDelete = true;
	private String mSelection = null;
	public int mStarredCount = 0;

	public boolean mIsShowingEmailList = false;

	private View searchView = null;

	public void setSearchView (View v) {
		searchView = v;
	}
	
	private String searchTextString;
	public void setSearchTextString (String searchTextString) {
		this.searchTextString=searchTextString;
	}

	public void setSearchViewSelection(String str) {
		mSelection = str;
	}

	public String getSearchViewSelection() {
		return mSelection;
	}

	public void setSlideDelete(boolean flag) {
		mIsNeedSlideDelete = flag;
	}
	// aurora <wangth> <2013-10-10> add for aurora ui end

	private Handler mDelayedDirectorySearchHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == DIRECTORY_SEARCH_MESSAGE) {
				loadDirectoryPartition(msg.arg1, (DirectoryPartition) msg.obj);
			}else if(msg.what==100){
				Log.d(TAG,"what==100");
				startLoading();
			}else if(msg.what==101){
				Log.d(TAG,"what==101");
				mListView.setFastScrollEnabled(!isSearchMode() && !isAlphbetIndexEnabled());
			}
		}
	};

	protected abstract View inflateView(LayoutInflater inflater, ViewGroup container);
	protected abstract T createListAdapter();

	/**
	 * @param position Please note that the position is already adjusted for
	 *            header views, so "0" means the first list item below header
	 *            views.
	 */
	protected abstract void onItemClick(int position, long id);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setContext(activity);
		setLoaderManager(super.getLoaderManager());
	}

	/**
	 * Sets a context for the fragment in the unit test environment.
	 */
	public void setContext(Context context) {
		mContext = context;
		configurePhotoLoader();
	}

	public Context getContext() {
		return mContext;
	}

	public void setEnabled(boolean enabled) {
		if (mEnabled != enabled) {
			mEnabled = enabled;
			if (mAdapter != null) {
				if (mEnabled) {
					reloadData();
				} else {
					mAdapter.clearPartitions();
				}
			}
		}
	}

	/**
	 * Overrides a loader manager for use in unit tests.
	 */
	public void setLoaderManager(LoaderManager loaderManager) {
		mLoaderManager = loaderManager;
	}

	@Override
	public LoaderManager getLoaderManager() {
		return mLoaderManager;
	}

	public T getAdapter() {
		return mAdapter;
	}

	@Override
	public View getView() {
		return mView;
	}

	public AuroraListView getListView() {
		return mListView;
	}

	public ContactListEmptyView getEmptyView() {
		return mEmptyView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_SECTION_HEADER_DISPLAY_ENABLED, mSectionHeaderDisplayEnabled);
		outState.putBoolean(KEY_PHOTO_LOADER_ENABLED, mPhotoLoaderEnabled);
		outState.putBoolean(KEY_QUICK_CONTACT_ENABLED, mQuickContactEnabled);
		outState.putBoolean(KEY_INCLUDE_PROFILE, mIncludeProfile);
		outState.putBoolean(KEY_SEARCH_MODE, mSearchMode);
		outState.putBoolean(KEY_VISIBLE_SCROLLBAR_ENABLED, mVisibleScrollbarEnabled);
		outState.putInt(KEY_SCROLLBAR_POSITION, mVerticalScrollbarPosition);
		outState.putInt(KEY_DIRECTORY_SEARCH_MODE, mDirectorySearchMode);
		outState.putBoolean(KEY_SELECTION_VISIBLE, mSelectionVisible);
		outState.putBoolean(KEY_LEGACY_COMPATIBILITY, mLegacyCompatibility);
		outState.putString(KEY_QUERY_STRING, mQueryString);
		outState.putInt(KEY_DIRECTORY_RESULT_LIMIT, mDirectoryResultLimit);
		outState.putBoolean(KEY_DARK_THEME, mDarkTheme);

		if (mListView != null) {
			outState.putParcelable(KEY_LIST_STATE, mListView.onSaveInstanceState());
		}
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		mContactsPrefs = new ContactsPreferences(mContext);
		restoreSavedState(savedState);


		IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
		try {
			mContext.registerReceiver(mBroadcastReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Received Intent:" + intent);
			try {
				getActivity().finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub


		try {
			super.onDestroy();
			mContext.unregisterReceiver(mBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void restoreSavedState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		mSectionHeaderDisplayEnabled = savedState.getBoolean(KEY_SECTION_HEADER_DISPLAY_ENABLED);
		mPhotoLoaderEnabled = savedState.getBoolean(KEY_PHOTO_LOADER_ENABLED);
		mQuickContactEnabled = savedState.getBoolean(KEY_QUICK_CONTACT_ENABLED);
		mIncludeProfile = savedState.getBoolean(KEY_INCLUDE_PROFILE);
		mSearchMode = savedState.getBoolean(KEY_SEARCH_MODE);
		mVisibleScrollbarEnabled = savedState.getBoolean(KEY_VISIBLE_SCROLLBAR_ENABLED);
		mVerticalScrollbarPosition = savedState.getInt(KEY_SCROLLBAR_POSITION);
		mDirectorySearchMode = savedState.getInt(KEY_DIRECTORY_SEARCH_MODE);
		mSelectionVisible = savedState.getBoolean(KEY_SELECTION_VISIBLE);
		mLegacyCompatibility = savedState.getBoolean(KEY_LEGACY_COMPATIBILITY);
		mQueryString = savedState.getString(KEY_QUERY_STRING);
		mDirectoryResultLimit = savedState.getInt(KEY_DIRECTORY_RESULT_LIMIT);
		mDarkTheme = savedState.getBoolean(KEY_DARK_THEME);

		// Retrieve list state. This will be applied in onLoadFinished
		mListState = savedState.getParcelable(KEY_LIST_STATE);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG,"onStart12");
		mContactsPrefs.registerChangeListener(mPreferencesChangeListener);

		if (mProviderStatusLoader == null) {
			mProviderStatusLoader = new ProviderStatusLoader(mContext);
		}

		mForceLoad = loadPreferences();

		mDirectoryListStatus = STATUS_NOT_LOADED;
		mLoadPriorityDirectoriesOnly = true;

		// aurora <wangth> <2013-10-18> modify for aurora ui begin
		//startLoading();
		if (mAdapter != null && mAdapter.getIsQueryForDialer()) {
			auroraInitQueryDialerABC();
			auroraQueryDialerABC();
		} else {
			startLoading();
		}
		//  aurora <wangth> <2013-10-18> modify for aurora ui end
	}

	//  aurora <wangth> <2013-11-6> add for aurora begin
	private boolean mNeesInitDialerABC = false;
	@Override
	public void onResume() {

		// TODO Auto-generated method stub
		super.onResume();

		if (mNeesInitDialerABC) {
			auroraInitQueryDialerABC();
			mNeesInitDialerABC = false;
		}
	}
	//  aurora <wangth> <2013-11-6> add for aurora end

	protected void startLoading() {
		Log.d(TAG,"startLoading");
		if (mAdapter == null) {
			// The method was called before the fragment was started
			Log.d(TAG, "[ContactEntryListFragment -> startLoading()]: mAdapter == null");
			return;
		}
		Log.d(TAG, "[ContactEntryListFragment -> startLoading()]: mLoadPriorityDirectoriesOnly = " + mLoadPriorityDirectoriesOnly);
		configureAdapter();
		int partitionCount = mAdapter.getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			Partition partition = mAdapter.getPartition(i);
			Log.d(TAG, "[ContactEntryListFragment -> startLoading()], i = " + i + "; partition = " + partition);
			if (partition instanceof DirectoryPartition) {
				DirectoryPartition directoryPartition = (DirectoryPartition)partition;
				Log.d(TAG, "[ContactEntryListFragment -> startLoading()], directoryPartition.getStatus() = " + directoryPartition.getStatus());
				if (directoryPartition.getStatus() == DirectoryPartition.STATUS_NOT_LOADED) {
					Log.d(TAG, "[ContactEntryListFragment -> startLoading()], directoryPartition.isPriorityDirectory() = " + directoryPartition.isPriorityDirectory());
					if (directoryPartition.isPriorityDirectory() || !mLoadPriorityDirectoriesOnly) {
						Log.d(TAG, "[ContactEntryListFragment -> startLoading()], startLoadingDirectoryPartition(" + i + ")");
						startLoadingDirectoryPartition(i);
					}
				}
			} else {
				Log.d(TAG, "[ContactEntryListFragment -> startLoading()], i = " + i);
				getLoaderManager().initLoader(i, null, this);                
			}
		}

		// Next time this method is called, we should start loading non-priority directories
		mLoadPriorityDirectoriesOnly = false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader,id:"+id);
		if (id == DIRECTORY_LOADER_ID) {
			DirectoryListLoader loader = new DirectoryListLoader(mContext);
			mAdapter.configureDirectoryLoader(loader);
			return loader;
		} else {
			CursorLoader loader = createCursorLoader();
			if(loader==null){
				Log.d("loaderlog","loader=null,reload");
				startLoading();
				return null;
			}
			long directoryId = args != null && args.containsKey(DIRECTORY_ID_ARG_KEY)
					? args.getLong(DIRECTORY_ID_ARG_KEY)
							: Directory.DEFAULT;
					mAdapter.configureLoader(loader, directoryId);
					return loader;
		}
	}

	public CursorLoader createCursorLoader() {
		return new CursorLoader(mContext, null, null, null, null, null);
	}

	private void startLoadingDirectoryPartition(int partitionIndex) {
		DirectoryPartition partition = (DirectoryPartition)mAdapter.getPartition(partitionIndex);
		Log.d(TAG, "[ContactEntryListFragment -> startLoadingDirectoryPartition()]: partitionIndex = " + partitionIndex + "; partition = " + partition);
		partition.setStatus(DirectoryPartition.STATUS_LOADING);
		long directoryId = partition.getDirectoryId();
		Log.d(TAG, "[ContactEntryListFragment -> startLoadingDirectoryPartition()]: directoryId = " + directoryId + "; mForceLoad = " + mForceLoad);
		if (mForceLoad) {
			if (directoryId == Directory.DEFAULT) {
				loadDirectoryPartition(partitionIndex, partition);
			} else {
				loadDirectoryPartitionDelayed(partitionIndex, partition);
			}
		} else {
			Bundle args = new Bundle();
			args.putLong(DIRECTORY_ID_ARG_KEY, directoryId);
			getLoaderManager().initLoader(partitionIndex, args, this);
		}
	}

	/**
	 * Queues up a delayed request to search the specified directory. Since
	 * directory search will likely introduce a lot of network traffic, we want
	 * to wait for a pause in the user's typing before sending a directory request.
	 */
	private void loadDirectoryPartitionDelayed(int partitionIndex, DirectoryPartition partition) {
		mDelayedDirectorySearchHandler.removeMessages(DIRECTORY_SEARCH_MESSAGE, partition);
		Message msg = mDelayedDirectorySearchHandler.obtainMessage(
				DIRECTORY_SEARCH_MESSAGE, partitionIndex, 0, partition);
		mDelayedDirectorySearchHandler.sendMessageDelayed(msg, DIRECTORY_SEARCH_DELAY_MILLIS);
	}

	/**
	 * Loads the directory partition.
	 */
	protected void loadDirectoryPartition(int partitionIndex, DirectoryPartition partition) {
		Bundle args = new Bundle();
		args.putLong(DIRECTORY_ID_ARG_KEY, partition.getDirectoryId());
		getLoaderManager().restartLoader(partitionIndex, args, this);
	}

	/**
	 * Cancels all queued directory loading requests.
	 */
	private void removePendingDirectorySearchRequests() {
		mDelayedDirectorySearchHandler.removeMessages(DIRECTORY_SEARCH_MESSAGE);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mListView.setVisibility(View.VISIBLE);


		if (!mEnabled) {
			Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], mEnabled == false");
			return;
		}

		// Gionee:wangth 20120618 add for CR00624733 begin

		// Gionee:wangth 20120618 add for CR00624733 end

		if (mAdapter.getIsQueryForDialer()) {
			return;
		}

		// aurora <wangth> <2013-12-25> add for aurora begin
		if (loader instanceof StarredAndContactsLoader) {
			mStarredCount = ((StarredAndContactsLoader) loader).getCursorCount();
			mAdapter.setStarredCount(mStarredCount);

			if (data == null) {
				if(AuroraDefaultContactBrowseListFragment.contactsCountHeader!=null){
					AuroraDefaultContactBrowseListFragment.contactsCount=0;
					AuroraDefaultContactBrowseListFragment.contactsCountHeader.
					setText(mContext.getResources().getString(R.string.contacts_count,0));
				}
				return;
			}

			if(AuroraDefaultContactBrowseListFragment.contactsCountHeader!=null){
				AuroraDefaultContactBrowseListFragment.contactsCount=data.getCount()-mStarredCount;
				AuroraDefaultContactBrowseListFragment.contactsCountHeader.
				setText(mContext.getResources().getString(R.string.contacts_count,AuroraDefaultContactBrowseListFragment.contactsCount));
			}
		}
		// aurora <wangth> <2013-12-25> add for aurora end
		Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], count = "+data.getCount());
		int loaderId = loader.getId();
		if (loaderId == DIRECTORY_LOADER_ID) {
			mDirectoryListStatus = STATUS_LOADED;
			mAdapter.changeDirectories(data);
			Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], 1");
			startLoading();
		} else {
			Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], 2");
			onPartitionLoaded(loaderId, data);
			if (isSearchMode()) {
				int directorySearchMode = getDirectorySearchMode();
				Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], directorySearchMode = " + directorySearchMode);
				if (directorySearchMode != DirectoryListLoader.SEARCH_MODE_NONE) {
					Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], mDirectoryListStatus = " + mDirectoryListStatus);
					if (mDirectoryListStatus == STATUS_NOT_LOADED) {
						mDirectoryListStatus = STATUS_LOADING;
						getLoaderManager().initLoader(DIRECTORY_LOADER_ID, null, this);
					} else {
						startLoading();
					}
				}
			} else {
				Log.d(TAG, "[ContactEntryListFragment -> onLoadFinished()], 4");
				mDirectoryListStatus = STATUS_NOT_LOADED;
				getLoaderManager().destroyLoader(DIRECTORY_LOADER_ID);
			}
		}

		//		auroraNoMatchView(true, null);


	}



	public void onLoaderReset(Loader<Cursor> loader) {
	}

	protected void onPartitionLoaded(int partitionIndex, Cursor data) {
		Log.d(TAG,"changeCursor0");
		if (partitionIndex >= mAdapter.getPartitionCount()) {
			// When we get unsolicited data, ignore it.  This could happen
			// when we are switching from search mode to the default mode.
			return;
		}

		Log.d(TAG,"changeCursor");
		mAdapter.changeCursor(partitionIndex, data);
		setProfileHeader();
		showCount(partitionIndex, data);

		if (!isLoading()) {
			completeRestoreInstanceState();
		}
	}

	public boolean isLoading() {
		if (mAdapter != null && mAdapter.isLoading()) {
			return true;
		}

		if (isLoadingDirectoryList()) {
			return true;
		}

		return false;
	}

	public boolean isLoadingDirectoryList() {
		return isSearchMode() && getDirectorySearchMode() != DirectoryListLoader.SEARCH_MODE_NONE
				&& (mDirectoryListStatus == STATUS_NOT_LOADED
				|| mDirectoryListStatus == STATUS_LOADING);
	}

	@Override
	public void onStop() {
		super.onStop();
		mContactsPrefs.unregisterChangeListener();
		mAdapter.clearPartitions();
		// aurora <wangth> <2013-11-6> add for aurora begin
		mNeesInitDialerABC = true;
		//  aurora <wangth> <2013-11-6> add for aurora end
	}

	protected void reloadData() {
		Log.d(TAG, "onLoadData");
		if (mIsNoNeedQueryAbc && null != mAdapter && mAdapter.getIsQueryForDialer()) {
			auroraQueryDialerABC();
			return;
		}

		if (mCursor != null) {
			mCursor.close();
			getAdapter().changeCursor(null);
		}

		removePendingDirectorySearchRequests();
		mAdapter.onDataReload();
		mLoadPriorityDirectoriesOnly = true;
		mForceLoad = true;
		startLoading();
	}

	/**
	 * Shows the count of entries included in the list. The default
	 * implementation does nothing.
	 */
	protected void showCount(int partitionIndex, Cursor data) {
	}

	/**
	 * Shows a view at the top of the list with a pseudo local profile prompting the user to add
	 * a local profile. Default implementation does nothing.
	 */
	protected void setProfileHeader() {
		mUserProfileExists = false;
	}

	/**
	 * Provides logic that dismisses this fragment. The default implementation
	 * does nothing.
	 */
	protected void finish() {
	}

	public void setSectionHeaderDisplayEnabled(boolean flag) {
		if (mSectionHeaderDisplayEnabled != flag) {
			mSectionHeaderDisplayEnabled = flag;
			if (mAdapter != null) {
				mAdapter.setSectionHeaderDisplayEnabled(flag);
			}
			configureVerticalScrollbar();
		}
	}

	public boolean isSectionHeaderDisplayEnabled() {
		return mSectionHeaderDisplayEnabled;
	}

	public void setVisibleScrollbarEnabled(boolean flag) {
		if (mVisibleScrollbarEnabled != flag) {
			mVisibleScrollbarEnabled = flag;
			configureVerticalScrollbar();
		}
	}

	//Gionee <huangzy> <2013-04-25> add for CR00801750 begin
	protected boolean isAlphbetIndexEnabled() {
		return false;
	}
	//Gionee <huangzy> <2013-04-25> add for CR00801750 end

	public boolean isVisibleScrollbarEnabled() {
		return mVisibleScrollbarEnabled;
	}

	public void setVerticalScrollbarPosition(int position) {
		if (mVerticalScrollbarPosition != position) {
			mVerticalScrollbarPosition = position;
			configureVerticalScrollbar();
		}
	}

	private void configureVerticalScrollbar() {
		// aurora <wangth> <2014-1-21> modify for aurora begin
		//boolean hasScrollbar = isVisibleScrollbarEnabled() && isSectionHeaderDisplayEnabled();
		boolean hasScrollbar = false;
		// aurora <wangth> <2014-1-21> modify for aurora end

		if (mListView != null) {
			mListView.setFastScrollEnabled(hasScrollbar && !isAlphbetIndexEnabled());
			mListView.setFastScrollAlwaysVisible(hasScrollbar);

			mListView.setVerticalScrollbarPosition(mVerticalScrollbarPosition);
			mListView.setScrollBarStyle(AuroraListView.SCROLLBARS_OUTSIDE_OVERLAY);
			int leftPadding = 0;
			int rightPadding = 0;
			if (mVerticalScrollbarPosition == View.SCROLLBAR_POSITION_LEFT) {
				leftPadding = mContext.getResources().getDimensionPixelOffset(
						R.dimen.list_visible_scrollbar_padding);
			} else {
				// gionee xuhz 20120605 add start
				if (ContactsApplication.sIsGnContactsSupport) {
					rightPadding = mContext.getResources().getDimensionPixelOffset(
							R.dimen.gn_contact_list_item_right_margin);
				} else {
					rightPadding = mContext.getResources().getDimensionPixelOffset(
							R.dimen.list_visible_scrollbar_padding);
				}
				// gionee xuhz 20120605 add end
			}
			mListView.setPadding(leftPadding, mListView.getPaddingTop(),
					rightPadding, mListView.getPaddingBottom());
		}
	}

	public void setPhotoLoaderEnabled(boolean flag) {
		mPhotoLoaderEnabled = flag;
		configurePhotoLoader();
	}

	public boolean isPhotoLoaderEnabled() {
		return mPhotoLoaderEnabled;
	}

	/**
	 * Returns true if the list is supposed to visually highlight the selected item.
	 */
	public boolean isSelectionVisible() {
		return mSelectionVisible;
	}

	public void setSelectionVisible(boolean flag) {
		this.mSelectionVisible = flag;
	}

	public void setQuickContactEnabled(boolean flag) {
		this.mQuickContactEnabled = flag;
	}

	public void setIncludeProfile(boolean flag) {
		mIncludeProfile = flag;
		if(mAdapter != null) {
			mAdapter.setIncludeProfile(flag);
		}
	}

	/**
	 * Enter/exit search mode.  By design, a fragment enters search mode only when it has a
	 * non-empty query text, so the mode must be tightly related to the current query.
	 * For this reason this method must only be called by {@link #setQueryString}.
	 *
	 * Also note this method doesn't call {@link #reloadData()}; {@link #setQueryString} does it.
	 */
	protected void setSearchMode(boolean flag) {
		if (mSearchMode != flag) {
			mSearchMode = flag;
			Log.d(TAG, "mSearchMode2:"+mSearchMode);
			setSectionHeaderDisplayEnabled(!mSearchMode);

			if (!flag) {
				mDirectoryListStatus = STATUS_NOT_LOADED;
				getLoaderManager().destroyLoader(DIRECTORY_LOADER_ID);
			}

			if (mAdapter != null) {
				mAdapter.setPinnedPartitionHeadersEnabled(flag);
				mAdapter.setSearchMode(flag);

				mAdapter.clearPartitions();
				if (!flag) {
					// If we are switching from search to regular display,
					// remove all directory partitions (except the default one).
					int count = mAdapter.getPartitionCount();
					for (int i = count; --i >= 1;) {
						mAdapter.removePartition(i);
					}
				}
				mAdapter.configureDefaultPartition(false, flag);
			}

			if (mListView != null) {
				mListView.setFastScrollEnabled(!flag && !isAlphbetIndexEnabled());
			}
		}
	}

	public final boolean isSearchMode() {
		return mSearchMode;
	}

	public final String getQueryString() {
		return mQueryString;
	}

	public void setQueryString(String queryString, boolean delaySelection) {
		// Normalize the empty query.
		if (TextUtils.isEmpty(queryString.trim())) queryString = null;

		if (!TextUtils.equals(mQueryString, queryString)) {
			Log.d(TAG, "mQueryString:"+mQueryString+" queryString:"+queryString);
			mQueryString = queryString;
			//			setSearchMode(!TextUtils.isEmpty(mQueryString));

			if (mAdapter != null) {
				mAdapter.setIsQueryForDialer(false);
				mAdapter.setQueryString(queryString);
				//reloadData();

				if (TextUtils.isEmpty(queryString) && isSearchMode()) {
					Log.d(TAG, "queryString is Empty");
					if (mCursor != null) {
						mCursor.close();
						mCursor = null;
					}
					mAdapter.changeCursor(null);
					return;
				}

				if (/*TextUtils.isEmpty(queryString) || */!checkIsNeedQueryFromDialer(queryString)) {
					Log.d(TAG, "[setQueryString] reloadData()");
					reloadData();
					return;
				}

				// aurora <wangth> <2013-10-18> modify for aurora ui begin

				mAdapter.setIsQueryForDialer(true);
				auroraQueryDialerABC();
				// aurora <wangth> <2013-10-18> modify for aurora ui end
			}
		}
	}

	public int getDirectorySearchMode() {
		return mDirectorySearchMode;
	}

	public void setDirectorySearchMode(int mode) {
		mDirectorySearchMode = mode;
	}

	public boolean isLegacyCompatibilityMode() {
		return mLegacyCompatibility;
	}

	public void setLegacyCompatibilityMode(boolean flag) {
		mLegacyCompatibility = flag;
	}

	protected int getContactNameDisplayOrder() {
		return mDisplayOrder;
	}

	protected void setContactNameDisplayOrder(int displayOrder) {
		mDisplayOrder = displayOrder;
		if (mAdapter != null) {
			mAdapter.setContactNameDisplayOrder(displayOrder);
		}
	}

	public int getSortOrder() {
		return mSortOrder;
	}

	public void setSortOrder(int sortOrder) {
		mSortOrder = sortOrder;
		if (mAdapter != null) {
			mAdapter.setSortOrder(sortOrder);
		}
	}

	public void setDirectoryResultLimit(int limit) {
		mDirectoryResultLimit = limit;
	}

	public void setContextMenuAdapter(ContextMenuAdapter adapter) {
		mContextMenuAdapter = adapter;
		if (mListView != null) {
			mListView.setOnCreateContextMenuListener(adapter);
		}
	}

	public ContextMenuAdapter getContextMenuAdapter() {
		return mContextMenuAdapter;
	}

	protected boolean loadPreferences() {
		boolean changed = false;
		if (getContactNameDisplayOrder() != mContactsPrefs.getDisplayOrder()) {
			setContactNameDisplayOrder(mContactsPrefs.getDisplayOrder());
			changed = true;
		}

		if (getSortOrder() != mContactsPrefs.getSortOrder()) {
			setSortOrder(mContactsPrefs.getSortOrder());
			changed = true;
		}

		return changed;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		onCreateView(inflater, container);

		mAdapter = createListAdapter();

		boolean searchMode = isSearchMode();
		mAdapter.setSearchMode(searchMode);
		mAdapter.configureDefaultPartition(false, searchMode);
		// aurora <wangth> <2013-11-1> remove for aurora begin
		//        mAdapter.setPhotoLoader(mPhotoManager);
		// aurora <wangth> <2013-11-1> remove for aurora end
		mListView.setAdapter(mAdapter);
		try {
			//        	mListView.auroraEnableOverScroll(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// aurora <wangth> <2013-9-29> modify for aurora ui begin
		mListView.auroraSetSelectorToContentBg(true);
		if (mIsNeedSlideDelete) {
			mListView.auroraSetNeedSlideDelete(true);
			getListView().auroraEnableSelector(true);
			getListView().auroraSetWhetherUseSepcialLayout(true);
			mListView.setOnScrollListener(null);
		}
		// aurora <wangth> <2013-9-29> modify for aurora ui end

		if (!isSearchMode()) {
			mListView.setFocusableInTouchMode(true);
			mListView.requestFocus();
		}

		//		Log.d(TAG,"oncreateview,sharedpreference:"+SharedPreferencesUtil.getInstance(mContext).getInt("has_update_randomphoto"));
		//		if(SharedPreferencesUtil.getInstance(mContext).getInt("has_update_randomphoto")!=0){
		//			
		//		}

		//		importRandomContactPhoto(3000);


		return mView;
	}

	protected void importRandomContactPhoto(final long sleep){

		new Thread(){
			public void run(){
				if(sleep>0){
					try{
						Thread.sleep(sleep);
					}catch(Exception e){

					}
				}

				try{
					Cursor cursor=mContext.getContentResolver().query(Contacts.CONTENT_URI,
							new String[]{Contacts.NAME_RAW_CONTACT_ID,"photo_id"},
							"photo_id is null",null,null);
					Log.d(TAG,"cursor9:"+cursor.getCount());
					if(cursor.getCount()>0){
						Random random=new Random();
						ContentValues values = new ContentValues();
						while(cursor.moveToNext()){
							if(cursor.isNull(cursor.getColumnIndex("photo_id"))){
								values.put("photo_id", -random.nextInt(14)-100);		                    	
								values.put("flag", 1);
								mContext.getContentResolver().update(Contacts.CONTENT_URI, values, 
										Contacts.NAME_RAW_CONTACT_ID + "=" + cursor.getInt(cursor.getColumnIndex(Contacts.NAME_RAW_CONTACT_ID)), null);
							}
						}	

						setPartionStatus();

						//					if(sleep>0){
						//						SharedPreferencesUtil.getInstance(mContext).putInt("has_update_randomphoto",0);	
						//					}
					}	

				}catch(Exception e){
					e.printStackTrace();
				}

			}
		}.start();
	}

	protected void setPartionStatus(){
		for (int i = 0; i < mAdapter.getPartitionCount(); i++) {
			Partition partition = mAdapter.getPartition(i);
			if (partition instanceof DirectoryPartition) {
				DirectoryPartition directoryPartition = (DirectoryPartition)partition;
				directoryPartition.setStatus(STATUS_NOT_LOADED);
			}
		}

		mDelayedDirectorySearchHandler.sendEmptyMessage(100);
	}

	protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
		mView = inflateView(inflater, container);

		mListView = (AuroraListView)mView.findViewById(android.R.id.list);
		if (mListView == null) {
			throw new RuntimeException(
					"Your content must have a ListView whose id attribute is " +
					"'android.R.id.list'");
		}

		View emptyView = mView.findViewById(com.android.internal.R.id.empty);
		if (emptyView != null) {
			mListView.setEmptyView(emptyView);
			if (emptyView instanceof ContactListEmptyView) {
				mEmptyView = (ContactListEmptyView)emptyView;
			}
		}

		mListView.setOnItemClickListener(this);
		mListView.setOnFocusChangeListener(this);
		mListView.setOnTouchListener(this);
		
//		Message msg = mDelayedDirectorySearchHandler.obtainMessage(
//				101);
//		mDelayedDirectorySearchHandler.sendMessageDelayed(msg, 500);
//		mListView.setFastScrollEnabled(!isSearchMode() && !isAlphbetIndexEnabled());
		

		// Tell list view to not show dividers. We'll do it ourself so that we can *not* show
		// them when an A-Z headers is visible.
		mListView.setDividerHeight(0);

		// We manually save/restore the listview state
		mListView.setSaveEnabled(false);

		if (mContextMenuAdapter != null) {
			mListView.setOnCreateContextMenuListener(mContextMenuAdapter);
		}

		// aurora <wangth> <2013-11-1> remove for aurora begin
		//        configureVerticalScrollbar();
		//        configurePhotoLoader();
		// aurora <wangth> <2013-11-1> remove for aurora end
	}

	protected void configurePhotoLoader() {
		// aurora <wangth> <2013-11-1> modify for aurora begin
		mDelayedDirectorySearchHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				if (isPhotoLoaderEnabled() && mContext != null) {
					if (mPhotoManager == null) {
						mPhotoManager = ContactPhotoManager.getInstance(mContext);
					}

					if (mAdapter != null) {
						mAdapter.setPhotoLoader(mPhotoManager);
					}
				}
			}

		}, 100);

		if (mListView != null) {
			mListView.setOnScrollListener(this);
		}
		// aurora <wangth> <2013-11-1> remove for aurora end
	}

	protected void configureAdapter() {
		if (mAdapter == null) {
			return;
		}

		mAdapter.setQuickContactEnabled(mQuickContactEnabled);
		mAdapter.setIncludeProfile(mIncludeProfile);
		mAdapter.setQueryString(mQueryString);
		mAdapter.setDirectorySearchMode(mDirectorySearchMode);
		mAdapter.setPinnedPartitionHeadersEnabled(mSearchMode);
		mAdapter.setContactNameDisplayOrder(mDisplayOrder);
		mAdapter.setSortOrder(mSortOrder);
		mAdapter.setSectionHeaderDisplayEnabled(mSectionHeaderDisplayEnabled);
		mAdapter.setSelectionVisible(mSelectionVisible);
		mAdapter.setDirectoryResultLimit(mDirectoryResultLimit);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (!mSearchMode && mListView != null) {
			if (firstVisibleItem < mListView.getHeaderViewsCount() 
					|| (mAdapter != null && mAdapter.getCount() == 0)) {
				mListView.auroraSetHeaderViewYOffset(-1000);
				ContactsUtils.mListHeaderTop = 0;
				ContactsUtils.mListHeaderY = 0;
			} else {
				int offset = ContactsUtils.mContactHeaderHight;
				if (ContactsUtils.mListHeaderY != 0) {
					offset += ContactsUtils.mListHeaderTop;
				}

				mListView.auroraSetHeaderViewYOffset(offset);
				mListView.auroraShowHeaderView(); // refresh right now
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mPhotoManager != null && scrollState == OnScrollListener.SCROLL_STATE_FLING) {
			mPhotoManager.pause();
		} else if (mPhotoManager != null && isPhotoLoaderEnabled()) {
			mPhotoManager.resume();
			if (null != mAdapter) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		hideSoftKeyboard();

		int adjPosition = position - mListView.getHeaderViewsCount();
		if (adjPosition >= 0) {
			onItemClick(adjPosition, id);
		}
	}

	private void hideSoftKeyboard() {
		// Hide soft keyboard, if visible
		InputMethodManager inputMethodManager = (InputMethodManager)
				mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mListView.getWindowToken(), 0);

		// aurora <wangth> <2013-10-22> add for aurora ui begin
		if (null != searchView) {
			searchView.clearFocus();
		}
		// aurora <wangth> <2013-10-22> add for aurora ui end
	}

	/**
	 * Dismisses the soft keyboard when the list takes focus.
	 */
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		//        if (view == mListView && hasFocus) {
		//            hideSoftKeyboard(); // remove for BUG #7882 by wangth 20140903
		//        }
	}

	/**
	 * Dismisses the soft keyboard when the list is touched.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view == mListView) {
			hideSoftKeyboard();
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		removePendingDirectorySearchRequests();
	}

	/**
	 * Dismisses the search UI along with the keyboard if the filter text is empty.
	 */
	public void onClose() {
		hideSoftKeyboard();
		finish();
	}

	/**
	 * Restore the list state after the adapter is populated.
	 */
	protected void completeRestoreInstanceState() {
		if (mListState != null) {
			mListView.onRestoreInstanceState(mListState);
			mListState = null;
		}
	}

	protected void setEmptyText(int resourceId) {
		TextView empty = (TextView) getEmptyView().findViewById(R.id.emptyText);
		empty.setText(mContext.getText(resourceId));
		empty.setVisibility(View.VISIBLE);
	}

	protected boolean hasIccCard() {
		TelephonyManager telephonyManager =
				(TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.hasIccCard();
	}

	public void setDarkTheme(boolean value) {
		mDarkTheme = value;
		if (mAdapter != null) mAdapter.setDarkTheme(value);
	}

	/**
	 * Processes a result returned by the contact picker.
	 */
	public void onPickerResult(Intent data) {
		throw new UnsupportedOperationException("Picker result handler is not implemented.");
	}

	private ContactsPreferences.ChangeListener mPreferencesChangeListener =
			new ContactsPreferences.ChangeListener() {
		@Override
		public void onChange() {
			Log.d(TAG, "onchange1");
			loadPreferences();
			reloadData();
		}
	};

	// aurora <wangth> <2013-10-19> modify for aurora ui beginonChange
	public boolean checkIsNeedQueryFromDialer(String str) {
		if (mIsShowingEmailList) {
			return false;
		}

		if (str == null) {
			return false;
		}
		boolean result = true;
		for (char c : str.toCharArray()) {
			if (!('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z')) {
				result = false;
				str = str.toLowerCase();
				break;
			}
		}

		return result;
	}

	public void auroraInitQueryDialerABC() {
		Log.d(TAG,"auroraInitQueryDialerABC");
		try {
			mContext.getContentResolver().query(Uri.parse("content://com.android.contacts/gn_dialer_search_init"),
					null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void auroraQueryDialerABC() {
		if (null != getAdapter() && getAdapter().getIsQueryForDialer()) {
			Log.d(TAG,"auroraQueryDialerABC");
			if (mQueryString == null) {
				return;
			}

			String key = mQueryString;
			if (!mMutliPhoneSearch) {
				key = QUERY_ABC_PREFIX + key;
			}

			if (mCursor != null) {
				mCursor.close();
			}

			initSearchViewSelection();

			if (mOnlySearchPhone) {
				if (mSelection == null) {
					mSelection = " vds_mimetype_id = 5 ";
				} else {
					mSelection = "(" + mSelection + ") and vds_mimetype_id = 5 ";
				}
			}

			if (!mMutliPhoneSearch) {
				if (mSelection == null) {
					mSelection = " vds_mimetype_id = 7 ";
				} else {
					mSelection = "(" + mSelection + ") and vds_mimetype_id = 7 ";
				}
			}
			// Aurora xuyong 2015-10-28 added for bug #16902 start
			if (mNeedShieldSimContact) {
				if (mSelection == null) {
					mSelection = " vds_indicate_phone_sim = -1 ";
				} else {
					mSelection = "(" + mSelection + ") and vds_indicate_phone_sim = -1 ";
				}
			}
			// Aurora xuyong 2015-10-28 added for bug #16902 end
			try {
				mCursor = mContext.getContentResolver().query(Uri.parse("content://com.android.contacts/gn_dialer_search/" +
						key), null, mSelection, null, null);
			} catch (SQLiteException sqle) {
				sqle.printStackTrace();
				auroraInitQueryDialerABC();
				mCursor = mContext.getContentResolver().query(Uri.parse("content://com.android.contacts/gn_dialer_search/" +
						key), null, mSelection, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.d(TAG,"mCursor:"+mCursor);
		if (mCursor != null) {
			Log.d(TAG,"cursor count:"+mCursor.getCount());

			mIsNoNeedQueryAbc = true;
		} else {
			mIsNoNeedQueryAbc = false;
			reloadData();
		}
		getAdapter().changeCursor(mCursor);
	}

	private boolean isCustomMode() {
		SharedPreferences mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(getActivity());
		int filterInt = mPrefs.getInt("filter.type", -1);
		if (filterInt == -3) {
			return true;
		}

		return false;
	}

	private void initSearchViewSelection() {
		String extra = mAdapter.getFilterExInfo();
		ContactListFilter filter = mAdapter.getFilter();

		boolean privacyDialerSearch = false;

		StringBuilder selection = new StringBuilder();
		if (!(extra == null)) {
			boolean noCustomOrLocal = false;
			if (extra.startsWith("withGroupId/")) {
				extra = extra.replace("withGroupId/", "");
				selection.append("( vds_raw_contact_id IN ( "
						+ "SELECT raw_contact_id FROM view_data" + " WHERE ("
						+ Data.MIMETYPE + " = '"
						+ GroupMembership.CONTENT_ITEM_TYPE
						+ "' AND data1=" + extra + ")))");
			} else if (extra.startsWith("noGroupId/")) {
				extra = extra.replace("noGroupId/", "");
				if (extra.isEmpty()) {
					selection.append("( vds_raw_contact_id  NOT IN ( "
							+ "SELECT raw_contact_id FROM view_data" + " WHERE ("
							+ Data.MIMETYPE + " = '"
							+ GroupMembership.CONTENT_ITEM_TYPE
							+ "' AND data1 is not null"
							+ " or " + RawContacts.INDICATE_PHONE_SIM + " >= 0 "
							+ ")))");
				} else {
					selection.append("( vds_raw_contact_id  NOT IN ( "
							+ "SELECT raw_contact_id FROM view_data" + " WHERE ("
							+ Data.MIMETYPE + " = '"
							+ GroupMembership.CONTENT_ITEM_TYPE
							+ "' AND data1=" + extra
							+ " or " + RawContacts.INDICATE_PHONE_SIM + " >= 0 "
							+ ")))");

					if (filter != null) {
						selection.append(" AND ( vds_account_name='" + filter.accountName + 
								"' AND vds_account_type='" + filter.accountType + "')");
					}
				}
			} else if (extra.startsWith("simContacts/")) {
				selection.append("(vds_index_in_sim >= 0)");
				noCustomOrLocal = true;
			} else if (extra.startsWith("auto_record/")) {
				selection.append("(vds_auto_record = 1 AND vds_indicate_phone_sim = -1)");
				noCustomOrLocal = true;
				privacyDialerSearch = true;

				long privacyId = AuroraPrivacyUtils.mCurrentAccountId;
				if (privacyId > 0) {
					selection.append(" and(vds_is_privacy > -1)");
				} else {
					selection.append(" and(vds_is_privacy = 0)");
				}
			} else if (extra.startsWith("add_to_auto_record/")) {
				selection.append("(vds_auto_record = 0 AND vds_indicate_phone_sim = -1)");
				noCustomOrLocal = true;
				privacyDialerSearch = true;

				long privacyId = AuroraPrivacyUtils.mCurrentAccountId;
				if (privacyId > 0) {
					selection.append(" and(vds_is_privacy > -1)");
				} else {
					selection.append(" and(vds_is_privacy = 0)");
				}
			} else if (extra.startsWith("mms_select/")) {
				long privacyId = AuroraPrivacyUtils.mCurrentAccountId;
				if (privacyId > 0) {
					selection.append("(vds_is_privacy > -1)");
				} else {
					selection.append("(vds_is_privacy = 0)");
				}

				noCustomOrLocal = true;
				privacyDialerSearch = true;
			}

			if (null != selection) {
				if (!noCustomOrLocal) {
					if (isCustomMode()) {
						selection.append(" AND vds_in_visible_group=1 ");
					} else {
						selection.append(" AND ( vds_account_name='" + AccountType.ACCOUNT_NAME_LOCAL_PHONE + 
								"' AND vds_account_type='" + AccountType.ACCOUNT_TYPE_LOCAL_PHONE + "')");
					}
				}

				if (!privacyDialerSearch) {
					selection = initPrivacySelect(selection);
				}

				setSearchViewSelection(selection.toString());
			}

			return;
		}

		if (isCustomMode()) {
			selection.append("vds_in_visible_group=1");
		}  else {
			selection.append("( vds_account_name='" + AccountType.ACCOUNT_NAME_LOCAL_PHONE + 
					"' AND vds_account_type='" + AccountType.ACCOUNT_TYPE_LOCAL_PHONE + "')");
		}

		if (!privacyDialerSearch) {
			selection = initPrivacySelect(selection);
		}

		setSearchViewSelection(selection.toString());
	}

	private StringBuilder initPrivacySelect(StringBuilder selection) {
		if (ContactsApplication.sIsAuroraPrivacySupport) {
			if (null != selection && !selection.toString().contains("vds_is_privacy")) {
				selection.append(" AND vds_is_privacy=0");
			} else {
				selection.append(" vds_is_privacy=0");
			}
		}

		return selection;
	}

	private boolean mOnlySearchPhone = false;
	public void setOnlyPhone(boolean flag) {
		mOnlySearchPhone = flag;
	}

	private boolean mMutliPhoneSearch = false;
	public void setMultiPhoneSearch(boolean flag) {
		mMutliPhoneSearch = flag;
	}

	protected AbsListIndexer mAlphbetIndexView;
	public void auroraNoMatchView(boolean flag, String str) {
		Log.d(TAG,"auroraNoMatchView,flag:"+flag);
		//		mListView.setVisibility(View.VISIBLE);
		LinearLayout ll = null;
		FrameLayout mainContent = (FrameLayout) getView().findViewById(R.id.main_content);
		if (ll == null && mContext != null && mainContent != null) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			FrameLayout rl = (FrameLayout) inflater.inflate(R.layout.aurora_search_empty, mainContent);
			if (rl != null) {
				ll = (LinearLayout)rl.findViewById(R.id.no_match);
			}
		}

		if (!flag && ll != null) {
			ll.setVisibility(View.GONE);
			if (null != mAlphbetIndexView) {
				mAlphbetIndexView.setVisibility(View.GONE);
			}
			mListView.setVisibility(View.GONE);
			return;
		}


		TextView no = (TextView)ll.findViewById(R.id.no_match_view);
		if (no != null && str != null) {
			no.setText(str);
		}

		boolean showNoMatchView = false;
		Log.d(TAG, "mListView.getCount():"+mListView.getCount()+" mListView.getHeaderViewsCount():"
				+mListView.getHeaderViewsCount()+" mListView.getFooterViewsCount():"+mListView.getFooterViewsCount());
		if ((mListView != null && mListView.getCount() - mListView.getHeaderViewsCount() -mListView.getFooterViewsCount()> 0)||TextUtils.isEmpty(searchTextString)) {
			showNoMatchView = false;
		} else {
			showNoMatchView = true;
		}

		Log.d(TAG,"showNoMatchView:"+showNoMatchView);
		if (showNoMatchView) {
			if (ll != null) {
				ll.setVisibility(View.VISIBLE);
				if (null != mAlphbetIndexView) {
					mAlphbetIndexView.setVisibility(View.GONE);
				}
			}
		} else {
			if (ll != null) {

				Log.d(TAG,"showNoMatchView1:"+mListView.getVisibility());
				ll.setVisibility(View.GONE);

				if (null != mAlphbetIndexView) {
					mAlphbetIndexView.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public void auroraNoMatchView(boolean flag, int count) {
		LinearLayout ll = null;
		FrameLayout mainContent = (FrameLayout) getView().findViewById(R.id.main_content);
		if (ll == null && mContext != null && mainContent != null) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			FrameLayout rl = (FrameLayout) inflater.inflate(R.layout.aurora_search_empty, mainContent);
			if (rl != null) {
				ll = (LinearLayout)rl.findViewById(R.id.no_match);
			}
		}

		if (!flag) {
			if (ll != null) {
				ll.setVisibility(View.GONE);
			}
			return;
		}

		if (count == 0) {
			if (ll != null) {
				ll.setVisibility(View.VISIBLE);
			}
			return;
		}

		boolean showNoMatchView = false;
		if (mListView != null && mListView.getCount() - mListView.getHeaderViewsCount() > 0) {
			showNoMatchView = false;
		} else {
			showNoMatchView = true;
		}

		if (showNoMatchView) {
			if (ll != null) {
				ll.setVisibility(View.VISIBLE);
			}
		} else {
			if (ll != null) {
				ll.setVisibility(View.GONE);
			}
		}
	}
	// aurora <wangth> <2013-10-19> modify for aurora ui end
	// Aurora xuyong 2015-10-28 added for bug #16902 start
	private boolean mNeedShieldSimContact;
	public void setNeedShieldSimContact(boolean need) {
		mNeedShieldSimContact = true;
	}
	// Aurora xuyong 2015-10-28 added for bug #16902 end
}
