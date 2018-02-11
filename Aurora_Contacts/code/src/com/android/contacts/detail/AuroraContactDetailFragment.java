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
 * limitations under the License
 */

package com.android.contacts.detail;

import android.view.ViewGroup.LayoutParams;

import com.android.contacts.Collapser;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.ResConstant.IconTpye;
import com.android.contacts.ContactLoader;
import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.GroupMetaData;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.TypePrecedence;
import com.android.contacts.activities.ContactDetailActivity.FragmentKeyListener;
import com.android.contacts.editor.AuroraContactEditorFragment;
import com.android.contacts.editor.PhotoActionPopup;
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountType.EditField;
import com.android.contacts.model.AccountType.EditType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.EntityDeltaList;
import com.android.contacts.model.EntityModifier;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.StatisticsUtil;
import com.android.contacts.util.StructuredPostalUtils;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.widget.TransitionAnimationView;
import com.android.internal.telephony.ITelephony;
import com.google.common.annotations.VisibleForTesting;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.ContactsIntent;

import android.R.color;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.Event;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Im;
import gionee.provider.GnContactsContract.CommonDataKinds.Nickname;
import gionee.provider.GnContactsContract.CommonDataKinds.Note;
import gionee.provider.GnContactsContract.CommonDataKinds.Organization;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Relation;
import gionee.provider.GnContactsContract.CommonDataKinds.SipAddress;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredPostal;
import gionee.provider.GnContactsContract.CommonDataKinds.Website;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.DisplayNameSources;
import gionee.provider.GnContactsContract.Intents.UI;
import gionee.provider.GnContactsContract.PhoneLookup;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.StatusUpdates;
import gionee.provider.GnContactsContract.DisplayPhoto;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.contacts.quickcontact.QuickDataAction;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.database.Cursor;

import com.android.contacts.util.NotifyingAsyncQueryHandler;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactsLog;
//import com.android.contacts.activities.OnMenuItemClickLisener;
import com.android.contacts.activities.PeopleActivity;

import gionee.provider.GnTelephony.SIMInfo; 
import gionee.provider.GnTelephony;

import com.android.contacts.detail.AssociationSimActivity.ContactDetailInfo;

import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.RawContactsEntity;

import com.mediatek.contacts.util.OperatorUtils;
import com.privacymanage.service.AuroraPrivacyUtils;
import com.gionee.internal.telephony.GnITelephony;
import com.android.contacts.util.GnCallForSelectSim;

import gionee.provider.GnTelephony.SIMInfo;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import gionee.telephony.AuroraTelephoneManager;
import aurora.app.AuroraActivity;
import com.android.contacts.AuroraCardTypeUtils;

public class AuroraContactDetailFragment extends Fragment implements
        FragmentKeyListener, ViewOverlay, SelectAccountDialogFragment.Listener,
        OnItemClickListener, NotifyingAsyncQueryHandler.AsyncQueryListener,
        NotifyingAsyncQueryHandler.AsyncUpdateListener, View.OnClickListener {

    private static final String TAG = "ContactDetailFragment";

    private interface ContextMenuIds {
        static final int COPY_TEXT = 0;
        static final int CLEAR_DEFAULT = 1;
        static final int SET_DEFAULT = 2;
        static final int NEW_ASSOCIATION_SIM = 3;
        static final int DEL_ASSOCIATION_SIM = 4;
        static final int EDIT_NUMBER_BEFORE_CALL = 5;
    }

    private static final String KEY_CONTACT_URI = "contactUri";
    private static final String KEY_LIST_STATE = "liststate";

    // TODO: Make maxLines a field in {@link DataKind}
    private static final int WEBSITE_MAX_LINES = 1;
    private static final int SIP_ADDRESS_MAX_LINES= 1;
    private static final int POSTAL_ADDRESS_MAX_LINES = 10;
    private static final int GROUP_MAX_LINES = 10;
    private static final int NOTE_MAX_LINES = 100;

    private static Context mContext;//aurora add zhouxiaobing 20140504,the old is not static
    private View mView;
    private OnScrollListener mVerticalScrollListener;
    private Uri mLookupUri;
    private static Uri mCallUri;
    private Listener mListener;

    private ContactLoader.Result mContactData;
    private ImageView mStaticPhotoView;
    private View mHeaderView;
    private AuroraListView mListView;
    private ViewAdapter mAdapter;
    private Uri mPrimaryPhoneUri = null;
    private ViewEntryDimensions mViewEntryDimensions;
    
    private boolean mIsPrivacyContact = false;

    private int mNumPhoneNumbers = 0;
    private String mDefaultCountryIso;
    private boolean mContactHasSocialUpdates;
    private boolean mShowStaticPhoto = true;
    private static boolean hasClicked = false;
    
    private boolean hasQueryRejected = false;
    
    /**
     * Device capability: Set during buildEntries and used in the long-press context menu
     */
    private boolean mHasPhone;

    /**
     * Device capability: Set during buildEntries and used in the long-press context menu
     */
    private boolean mHasSms;

    /**
     * Device capability: Set during buildEntries and used in the long-press context menu
     */
    private boolean mHasSip;

    /**
     * The view shown if the detail list is empty.
     * We set this to the list view when first bind the adapter, so that it won't be shown while
     * we're loading data.
     */
    private View mEmptyView;

    /**
     * Initial alpha value to set on the alpha layer.
     */
    private float mInitialAlphaValue;

    /**
     * This optional view adds an alpha layer over the entire fragment.
     */
    private View mAlphaLayer;

    /**
     * This optional view adds a layer over the entire fragment so that when visible, it intercepts
     * all touch events on the fragment.
     */
    private View mTouchInterceptLayer;

    /**
     * Saved state of the {@link ListView}. This must be saved and applied to the {@ListView} only
     * when the adapter has been populated again.
     */
    private Parcelable mListState;

    /**
     * A list of distinct contact IDs included in the current contact.
     */
    private ArrayList<Long> mRawContactIds = new ArrayList<Long>();
	// aurora <ukiliu> <2013-9-25> add for aurora ui begin
    private ArrayList<OrganizationViewEntry> mOrganizationEntries = new ArrayList<OrganizationViewEntry>();
	// aurora <ukiliu> <2013-9-25> add for aurora ui end
    private ArrayList<DetailViewEntry> mPhoneEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mSmsEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mEmailEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mPostalEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mImEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mNicknameEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mGroupEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mRelationEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mNoteEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mWebsiteEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mSipEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mEventEntries = new ArrayList<DetailViewEntry>();
    private final Map<AccountType, List<DetailViewEntry>> mOtherEntriesMap =
            new HashMap<AccountType, List<DetailViewEntry>>();
    private ArrayList<ViewEntry> mAllEntries = new ArrayList<ViewEntry>();
    private LayoutInflater mInflater;

    private boolean mTransitionAnimationRequested;

    private boolean mIsUniqueNumber;
    private boolean mIsUniqueEmail;

    private NotifyingAsyncQueryHandler mHandler = null;
    private DetailViewEntry tempDetailViewEntry = null;
    
    private static boolean mOptr = false;
    
    private ArrayList<DetailViewEntry> mShowingPhoneEntries = null;
    
	// aurora yudingmin 2015-01-12 added for bug #10724 start
    private int spring_4_height;
    private int spring_5_height;
 // aurora yudingmin 2015-01-12 added for bug #10724 end
    
    public AuroraContactDetailFragment() {
        // Explicit constructor for inflation
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getActivity().getIntent().getExtras();
        if (ContactsApplication.sIsAuroraPrivacySupport && null != extras) {
        	mIsPrivacyContact = extras.getBoolean("is_privacy_contact");
        }
        
        if (savedInstanceState != null) {
            mLookupUri = savedInstanceState.getParcelable(KEY_CONTACT_URI);
            mCallUri = savedInstanceState.getParcelable(KEY_CONTACT_URI);
            mListState = savedInstanceState.getParcelable(KEY_LIST_STATE);
        }
        mOptr = OperatorUtils.getOptrProperties().equals("OP01");
        Log.d(TAG, "onCreate(), mOptr "+ mOptr );
        mHandler = new NotifyingAsyncQueryHandler(this.mContext, this);
        mHandler.setUpdateListener(this);

    	// aurora yudingmin 2015-01-12 added for bug #10724 start
        spring_4_height = getResources().getDimensionPixelSize(R.dimen.detail_spring_4_height);
        spring_5_height = getResources().getDimensionPixelSize(R.dimen.detail_spring_5_height);
    	// aurora yudingmin 2015-01-12 added for bug #10724 end
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CONTACT_URI, mLookupUri);
        if (mListView != null) {
            outState.putParcelable(KEY_LIST_STATE, mListView.onSaveInstanceState());
        }
    }

    @Override
    public void onPause() {
    	Log.d(TAG, "onPause");
    	
        super.onPause();
    }

    @Override
    public void onResume() {
        // aurora <wangth> <2014-1-4> add for aurora begin
    	hasQueryRejected = false;
    	
        loadPhotoPickSize();
        photoListener = new PhotoEditorListener();
        // aurora <wangth> <2014-1-4> add for aurora end
    	hasClicked = false;
        super.onResume();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(mContext);
        mViewEntryDimensions = new ViewEntryDimensions(mContext.getResources());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
    	ContactsLog.logt(TAG,"onCreateView start");   	
        mView = inflater.inflate(R.layout.gn_contact_detail_fragment, container, false);
        mInflater = inflater;
        
        mStaticPhotoView = (ImageView) mView.findViewById(R.id.photo);
        mHeaderView = mView.findViewById(R.id.aurora_detail_header);
        
        mListView = (AuroraListView) mView.findViewById(android.R.id.list);
        mListView.setScrollBarStyle(AuroraListView.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOnItemClickListener(this);
        mListView.setItemsCanFocus(true);
        mListView.setOnScrollListener(mVerticalScrollListener);
        mListView.setPadding(0, 0, 0, 0);

        // Don't set it to mListView yet.  We do so later when we bind the adapter.
        mEmptyView = mView.findViewById(android.R.id.empty);

        mTouchInterceptLayer = mView.findViewById(R.id.touch_intercept_overlay);
        mAlphaLayer = mView.findViewById(R.id.alpha_overlay);
//        ContactDetailDisplayUtils.setAlphaOnViewBackground(mAlphaLayer, mInitialAlphaValue);

//        mView.setVisibility(View.GONE);
        mView.setVisibility(View.VISIBLE);
        if (mContactData != null) {
            //bindData(); 
        }
        ContactsLog.logt(TAG,"onCreateView end");
        return mView;
    }

    protected View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        return mInflater.inflate(resource, root, attachToRoot);
    }

    public void setListener(Listener value) {
        mListener = value;
    }

    @Override
    public void setAlphaLayerValue(float alpha) {
        // If the alpha layer is not ready yet, store it for later when the view is initialized
        if (mAlphaLayer == null) {
            mInitialAlphaValue = alpha;
        } else {
            // Otherwise set the value immediately
//            ContactDetailDisplayUtils.setAlphaOnViewBackground(mAlphaLayer, alpha);
        }
    }

    @Override
    public void enableTouchInterceptor(OnClickListener clickListener) {
        if (mTouchInterceptLayer != null) {
            mTouchInterceptLayer.setVisibility(View.VISIBLE);
            mTouchInterceptLayer.setOnClickListener(clickListener);
        }
    }

    @Override
    public void disableTouchInterceptor() {
        if (mTouchInterceptLayer != null) {
            mTouchInterceptLayer.setVisibility(View.GONE);
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected Listener getListener() {
        return mListener;
    }

    protected ContactLoader.Result getContactData() {
        return mContactData;
    }

    public void setVerticalScrollListener(OnScrollListener listener) {
        mVerticalScrollListener = listener;
    }

    public Uri getUri() {
        return mLookupUri;
    }

    /**
     * Sets whether the static contact photo (that is not in a scrolling region), should be shown
     * or not.
     */
    public void setShowStaticPhoto(boolean showPhoto) {
        mShowStaticPhoto = showPhoto;
    }

    public void showEmptyState() {
        setData(null, null);
    }

    public void setData(Uri lookupUri, ContactLoader.Result result) {
        mLookupUri = lookupUri;
        mCallUri = lookupUri;
        mContactData = result;
        ContactsLog.logt(TAG, "start  bindData");
        bindData();
        ContactsLog.logt(TAG, "end  bindData");
    }
    
    public boolean isMe() {
        if (mContactData != null) {
            String lookupKey = mContactData.getLookupKey();
            if (lookupKey != null) {
                return lookupKey.equals("profile");
            }
        }
        return false;
    }

    /**
     * Reset the list adapter in this {@link Fragment} to get rid of any saved scroll position
     * from a previous contact.
     */
    public void resetAdapter() {
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    /**
     * Returns the top coordinate of the first item in the {@link ListView}. If the first item
     * in the {@link ListView} is not visible or there are no children in the list, then return
     * Integer.MIN_VALUE. Note that the returned value will be <= 0 because the first item in the
     * list cannot have a positive offset.
     */
    public int getFirstListItemOffset() {
        return ContactDetailDisplayUtils.getFirstListItemOffset(mListView);
    }

    /**
     * Tries to scroll the first item to the given offset (this can be a no-op if the list is
     * already in the correct position).
     * @param offset which should be <= 0
     */
    public void requestToMoveToOffset(int offset) {
        ContactDetailDisplayUtils.requestToMoveToOffset(mListView, offset);
    }
    
    protected void bindData() {
        if (mView == null) {
            return;
        }

        if (mTransitionAnimationRequested) {
            TransitionAnimationView.startAnimation(mView, mContactData == null);
            mTransitionAnimationRequested = false;
        }

        Log.d(TAG, "mContactData = " + mContactData);
        if (mContactData == null) {
            mView.setVisibility(View.INVISIBLE);
            mAllEntries.clear();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            return;
        }
        
		// aurora <ukiliu> <2013-9-25> add for aurora ui begin
        if (mHeaderView != null) {
        	int high = mContactData.getContentValues().size();
        	getHeaderEntryView(mHeaderView, (ViewGroup)mView, high);
        }
		// aurora <ukiliu> <2013-9-25> add for aurora ui end
        // Figure out if the contact has social updates or not
        mContactHasSocialUpdates = !mContactData.getStreamItems().isEmpty();

        // Setup the photo if applicable
        if (mStaticPhotoView != null) {
            // The presence of a static photo view is not sufficient to determine whether or not
            // we should show the photo. Check the mShowStaticPhoto flag which can be set by an
            // outside class depending on screen size, layout, and whether the contact has social
            // updates or not.
            if (mShowStaticPhoto) {
                mStaticPhotoView.setVisibility(View.VISIBLE);
                // aurora <wangth> <2014-1-4> add for aurora begin
                int indicatePhoneSim = mContactData.getIndicate();
                if (indicatePhoneSim < 0 && canChangePhotoAccount()) {
                    mStaticPhotoView.setClickable(true);
                    mStaticPhotoView.setOnClickListener(this);
                }
                // aurora <wangth> <2014-1-4> add for aurora end
                
                if (FeatureOption.MTK_GEMINI_SUPPORT && indicatePhoneSim > 0) {
                    int iconId = ContactsUtils.getSimBigIcon(mContext, indicatePhoneSim);
                    mStaticPhotoView.setBackgroundResource(iconId);
                } else {
                    ContactDetailDisplayUtils.setPhotoForDetail(mContext, mContactData, mStaticPhotoView, mIsPrivacyContact);
                }
            } else {
                mStaticPhotoView.setVisibility(View.GONE);
            }
        }

        // Build up the contact entries
        buildEntries();

        // Collapse similar data items for select {@link DataKind}s.
//        Collapser.collapseList(mPhoneEntries);//aurora change zhouxiaobing 20140304
        getShowingPhoneEntries();        
//        Collapser.collapseList(mSmsEntries);
//        Collapser.collapseList(mEmailEntries);
//        Collapser.collapseList(mPostalEntries);
//        Collapser.collapseList(mImEntries);

        mIsUniqueNumber = mPhoneEntries.size() == 1;
        mIsUniqueEmail = mEmailEntries.size() == 1;

        // Make one aggregated list of all entries for display to the user.
        setupFlattenedList();

        if (mAdapter == null) {
            mAdapter = new ViewAdapter();
            mListView.setAdapter(mAdapter);
        }

        // Restore {@link ListView} state if applicable because the adapter is now populated.
        if (mListState != null) {
            mListView.onRestoreInstanceState(mListState);
            mListState = null;
        }

        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
        	mAdapter.filterEntriesShow(mAllEntries);	
        }
        
        mAdapter.notifyDataSetChanged();

        mListView.setEmptyView(mEmptyView);
        mView.setVisibility(View.VISIBLE);
    }

    /** @return default group id or -1 if no group or several groups are marked as default */
    private long getDefaultGroupId(List<GroupMetaData> groups) {
        long defaultGroupId = -1;
        for (GroupMetaData group : groups) {
            if (group.isDefaultGroup()) {
                // two default groups? return neither
                if (defaultGroupId != -1) return -1;
                defaultGroupId = group.getGroupId();
            }
        }
        return defaultGroupId;
    }

    /**
     * Build up the entries to display on the screen.
     */
    private final void buildEntries() {
        mHasPhone = PhoneCapabilityTester.isPhone(mContext);
        mHasSms = PhoneCapabilityTester.isSmsIntentRegistered(mContext);
        mHasSip = PhoneCapabilityTester.isSipPhone(mContext);

        // Clear out the old entries
        mAllEntries.clear();

        mRawContactIds.clear();

        mPrimaryPhoneUri = null;
        mNumPhoneNumbers = 0;

        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);

        // Build up method entries
        if (mContactData == null) {
            return;
        }

        ArrayList<String> groups = new ArrayList<String>();
        for (Entity entity: mContactData.getEntities()) {
            final ContentValues entValues = entity.getEntityValues();
            final String accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
            final String dataSet = entValues.getAsString(RawContacts.DATA_SET);
            final long rawContactId = entValues.getAsLong(RawContacts._ID);

            if (!mRawContactIds.contains(rawContactId)) {
                mRawContactIds.add(rawContactId);
            }

            AccountType type = accountTypes.getAccountType(accountType, dataSet);

            for (NamedContentValues subValue : entity.getSubValues()) {
                final ContentValues entryValues = subValue.values;
                entryValues.put(Data.RAW_CONTACT_ID, rawContactId);

                final long dataId = entryValues.getAsLong(Data._ID);
                final String mimeType = entryValues.getAsString(Data.MIMETYPE);
                Log.i(TAG,"********** mimeType : "+mimeType);
                if (mimeType == null) continue;

                if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType) && !mIsPrivacyContact) {
                    Long groupId = entryValues.getAsLong(GroupMembership.GROUP_ROW_ID);
                    if (groupId != null) {
                        handleGroupMembership(groups, mContactData.getGroupMetaData(), groupId);
                    }
                    continue;
                }

                final DataKind kind = accountTypes.getKindOrFallback(
                        accountType, dataSet, mimeType);
                if (kind == null) continue;

                // aurora <ukiliu> <2013-9-25> add for aurora ui begin
                if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                	final OrganizationViewEntry entry = OrganizationViewEntry.fromValues(mContext, mimeType, kind, dataId, entryValues);
                	mOrganizationEntries.add(entry);
                    continue;
                } 
                // aurora <ukiliu> <2013-9-25> add for aurora ui end
                final DetailViewEntry entry = DetailViewEntry.fromValues(mContext, mimeType, kind,
                        dataId, entryValues, mContactData.isDirectoryEntry(),
                        mContactData.getDirectoryId());

                /*
                 * New Feature by Mediatek Begin.            
                 * set this telephone number's sim id bound to sim card        
                 */
                if (!mContactData.isDirectoryEntry()) {
                    entry.simId = entryValues.getAsInteger(Data.SIM_ASSOCIATION_ID);
                }
                /*
                 * New Feature  by Mediatek End.
                */
                
                final boolean hasData = !TextUtils.isEmpty(entry.data);
                Integer superPrimary = entryValues.getAsInteger(Data.IS_SUPER_PRIMARY);
                final boolean isSuperPrimary = superPrimary != null && superPrimary != 0;

                if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    // Always ignore the name. It is shown in the header if set
                } else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build phone entries
                    mNumPhoneNumbers++;
                    String phoneNumberE164 =
                            entryValues.getAsString(PhoneLookup.NORMALIZED_NUMBER);
                    
                    // aurora wangth 20140721 add for black begin
                    if (ContactsApplication.sIsAuroraRejectSupport) {
                    	if (!hasQueryRejected) {
                    		ContactDetailActivity.mRejectedList.clear();
                    		ContactDetailActivity.mThisRejectedList.clear();
                        	ContactDetailActivity.mRejectedCount = 0;
                        	loadRejectedList();
                        	hasQueryRejected = true;
                        }
                    	
                    	if (entry.data != null) {
                    		String number = entry.data.replaceAll(" ", "");
                        	if (number != null) {
                        		number = number.replaceAll("-", "");
                        		if (number != null) {
                        			if (ContactDetailActivity.mRejectedList.contains(number)) {
                        				if (!ContactDetailActivity.mThisRejectedList.contains(number)) {
                        					ContactDetailActivity.mThisRejectedList.add(number);
                                        	ContactDetailActivity.mRejectedCount++;
                        				}
                                	} else {
                                		String numberE164 = PhoneNumberUtils.formatNumberToE164(
                				                number, ContactsUtils.getCurrentCountryIso(mContext));
                                		if (numberE164 != null && !number.equals(numberE164)
                                				&& ContactDetailActivity.mRejectedList.contains(numberE164)) {
                                			if (!ContactDetailActivity.mThisRejectedList.contains(number)) {
                                				ContactDetailActivity.mThisRejectedList.add(number);
                                            	ContactDetailActivity.mRejectedCount++;
                                			}
                                		}
                                	}
                                	
                                	if (ContactDetailActivity.mRejectedCount == 0) {
                                		if (mIsPrivacyContact) {
                                			((AuroraActivity) mContext).setAuroraMenuItems(R.menu.aurora_privacy_contact_detail);
                                		} else {
                                			((AuroraActivity) mContext).setAuroraMenuItems(R.menu.aurora_contact_detail);
                                		}
                        			} else if (ContactDetailActivity.mRejectedCount == 1) {
                        				if (mIsPrivacyContact) {
                        					((AuroraActivity) mContext).setAuroraMenuItems(R.menu.aurora_privacy_contact_detail_black_one);
                        				} else {
                        					((AuroraActivity) mContext).setAuroraMenuItems(R.menu.aurora_contact_detail_black_one);
                        				}
                        			} else if (ContactDetailActivity.mRejectedCount > 1) {
                        				if (mIsPrivacyContact) {
                        					((AuroraActivity) mContext).setAuroraMenuItems(R.menu.aurora_privacy_contact_detail_black_mutil);
                        				} else {
                        					((AuroraActivity) mContext).setAuroraMenuItems(R.menu.aurora_contact_detail_black_mutil);
                        				}
                        			}
                        		}
                        	}
                    	}
                    }
                    // aurora wangth 20140721 add for black end
                    
                    entry.data = PhoneNumberUtils.formatNumber(
                            entry.data, phoneNumberE164, mDefaultCountryIso);
                    final Intent phoneIntent;
                    if (mHasPhone) {
                    	// gionee xuhz 20121215 modify for Dual Sim Select start
                    	if (ContactsApplication.sIsGnDualSimSelectSupport) {
                    		phoneIntent = new Intent("com.android.contacts.action.GNSELECTSIM");
                            phoneIntent.putExtra("number", entry.data);
                    	} else {
                    		phoneIntent = IntentFactory.newDialNumberIntent(entry.data);
                          	//aurora add liguangyu 20131206 start
                    		phoneIntent.putExtra("contactUri", getUri());
        	            	//aurora add liguangyu 20131206 end
                    	}
                        // gionee xuhz 20121215 modify for Dual Sim Select end
                    } else {
                    	phoneIntent = null;
                    }
                    
                    //Gionee:huangzy 20130401 modify for CR00792013 start
                    /*final Intent smsIntent = mHasSms ? new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts(Constants.SCHEME_SMSTO, entry.data, null)) : null;*/
                    Intent smsIntent = mHasSms ? IntentFactory.newCreateSmsIntent(entry.data) : null;
                    if (mIsPrivacyContact) {
                    	smsIntent.putExtra("is_privacy", AuroraPrivacyUtils.mCurrentAccountId);
                    }
                    //Gionee:huangzy 20130401 modify for CR00792013 end

                    // Configure Icons and Intents.
                    if (mHasPhone && mHasSms) {
                        entry.intent = phoneIntent;
                        entry.secondaryIntent = smsIntent;
                        // gionee xuhz 20120506 modify for CR00588990 start
                        if (ContactsApplication.sIsGnDarkStyle) {
                            entry.secondaryActionIcon = R.drawable.ic_text_holo_dark;
                        // aurora <ukiliu> <2013-9-25> add for aurora ui begin
                        } else if (ContactsApplication.auroraContactsSupport) {
                        	entry.secondaryActionIcon = R.drawable.aurora_detail_send_msg;
                        // aurora <ukiliu> <2013-9-25> add for aurora ui end
                        } else {
                            entry.secondaryActionIcon = kind.iconAltRes;
                        }
                        // gionee xuhz 20120506 modify for CR00588990 end
                        entry.secondaryActionDescription = kind.iconAltDescriptionRes;
                    } else if (mHasPhone) {
                        entry.intent = phoneIntent;
                    } else if (mHasSms) {
                        entry.intent = smsIntent;
                    } else {
                        entry.intent = null;
                    }

                    // Remember super-primary phone
                    if (isSuperPrimary) mPrimaryPhoneUri = entry.uri;

                    entry.isPrimary = isSuperPrimary;
                    mPhoneEntries.add(entry);
                } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build email entries
                    entry.intent = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts(Constants.SCHEME_MAILTO, entry.data, null));
                    entry.isPrimary = isSuperPrimary;
                    mEmailEntries.add(entry);

                    // When Email rows have status, create additional Im row
                    final DataStatus status = mContactData.getStatuses().get(entry.id);
                    if (status != null) {
                        final String imMime = Im.CONTENT_ITEM_TYPE;
                        final DataKind imKind = accountTypes.getKindOrFallback(accountType, dataSet,
                                imMime);
                        final DetailViewEntry imEntry = DetailViewEntry.fromValues(mContext, imMime,
                                imKind, dataId, entryValues, mContactData.isDirectoryEntry(),
                                mContactData.getDirectoryId());
                        buildImActions(mContext, imEntry, entryValues);
                        imEntry.applyStatus(status, false);
                        mImEntries.add(imEntry);
                    }
                } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build postal entries
                    entry.maxLines = POSTAL_ADDRESS_MAX_LINES;
                    entry.intent = StructuredPostalUtils.getViewPostalAddressIntent(entry.data);
                    mPostalEntries.add(entry);
                } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build IM entries
                    buildImActions(mContext, entry, entryValues);

                    // Apply presence and status details when available
                    final DataStatus status = mContactData.getStatuses().get(entry.id);
                    if (status != null) {
                        entry.applyStatus(status, false);
                    }
                    mImEntries.add(entry);
                } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Organizations are not shown. The first one is shown in the header
                    // and subsequent ones are not supported anymore
                    entry.uri = null;
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    Log.i(TAG,"************************** Nickname hasData");
                    // Build nickname entries
                    final boolean isNameRawContact =
                        (mContactData.getNameRawContactId() == rawContactId);

                    final boolean duplicatesTitle =
                        isNameRawContact
                        && mContactData.getDisplayNameSource() == DisplayNameSources.NICKNAME;

                    if (!duplicatesTitle) {
                        entry.uri = null;
                        mNicknameEntries.add(entry);
                    }
                } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build note entries
                    entry.uri = null;
                    entry.maxLines = NOTE_MAX_LINES;
                    mNoteEntries.add(entry);
                } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build Website entries
                    entry.uri = null;
                    entry.maxLines = WEBSITE_MAX_LINES;
                    try {
                        WebAddress webAddress = new WebAddress(entry.data);
                        entry.intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(webAddress.toString()));
                    } catch (ParseException e) {
                        Log.e(TAG, "Couldn't parse website: " + entry.data);
                    }
                    mWebsiteEntries.add(entry);
                } else if (SipAddress.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    // Build SipAddress entries
                    entry.uri = null;
                    entry.maxLines = SIP_ADDRESS_MAX_LINES;
                    if (mHasSip) {
                        entry.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_SIP, entry.data, null));
                    } else {
                        entry.intent = null;
                    }
                    mSipEntries.add(entry);
                    // TODO: Now that SipAddress is in its own list of entries
                    // (instead of grouped in mOtherEntries), consider
                    // repositioning it right under the phone number.
                    // (Then, we'd also update FallbackAccountType.java to set
                    // secondary=false for this field, and tweak the weight
                    // of its DataKind.)
                } else if (Event.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    entry.data = DateUtils.formatDate(mContext, entry.data);
                    entry.uri = null;
                    mEventEntries.add(entry);
                } else if (Relation.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                    entry.intent = new Intent(Intent.ACTION_SEARCH);
                    entry.intent.putExtra(SearchManager.QUERY, entry.data);
                    entry.intent.setType(Contacts.CONTENT_TYPE);
                    mRelationEntries.add(entry);
                } else {
                    // Handle showing custom rows
                	
                     entry.intent = new Intent(Intent.ACTION_VIEW);
                     entry.intent.setDataAndType(entry.uri, entry.mimetype);
                     entry.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//aurora add zhouxiaobing 20140419 for bug weixin
                    if (kind.actionBody != null) {
                         CharSequence body = kind.actionBody.inflateUsing(mContext, entryValues);
                         entry.data = (body == null) ? null : body.toString();
                    }

                    if (!TextUtils.isEmpty(entry.data)) {
                        // If the account type exists in the hash map, add it as another entry for
                        // that account type
                        if (mOtherEntriesMap.containsKey(type)) {
                            List<DetailViewEntry> listEntries = mOtherEntriesMap.get(type);
                            listEntries.add(entry);
                        } else {
                            // Otherwise create a new list with the entry and add it to the hash map
                            List<DetailViewEntry> listEntries = new ArrayList<DetailViewEntry>();
                            listEntries.add(entry);
                            mOtherEntriesMap.put(type, listEntries);
                        }
                    }
                }
            }
        }

        if (!groups.isEmpty()) {
            DetailViewEntry entry = new DetailViewEntry();
            try {
                Collections.sort(groups);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            StringBuilder sb = new StringBuilder();
            int size = groups.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(groups.get(i));
            }
            entry.mimetype = GroupMembership.MIMETYPE;
            entry.kind = mContext.getString(R.string.gn_others_label);
        	entry.typeString = mContext.getString(R.string.aurora_group_title);
        	
            if (mOptr) {
                entry.data = "! " + sb.toString();
            } else {
                entry.data = sb.toString();

            }
            entry.maxLines = GROUP_MAX_LINES;
            mGroupEntries.add(entry);
        }
        
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            // Gionee jialf 20130408 modified for CR00793565 start
            if (mContactData.isUserProfile()) {
            } else {
            	String ringtoneTitle = mContactData.getCustomRingtoneTitle();
            	if (null == ringtoneTitle) {
            		ringtoneTitle = getString(R.string.gnDefaultLabel);
            	}
            	
            	if (null != ringtoneTitle&&!ringtoneTitle.equals(getString(R.string.gnDefaultLabel))) {  
                	DetailViewEntry entry = new DetailViewEntry();
                    entry.mimetype = GroupMembership.MIMETYPE;                
                    if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                    	entry.kind = mContext.getString(R.string.gn_others_label);
                    	entry.typeString = mContext.getString(R.string.gn_ringtone_label);
                    } else {
                    	entry.kind = mContext.getString(R.string.gn_ringtone_label);	
                    }
                    /*entry.data = mContactData.getCustomRingtoneTitle();*/
                    entry.data = ringtoneTitle;
                    entry.maxLines = 1;
                    mGroupEntries.add(entry);
                }
            }
            // Gionee jialf 20130408 modified for CR00793565 end
        }
    }

    /**
     * Collapse all contact detail entries into one aggregated list with a {@link HeaderViewEntry}
     * at the top.
     */
    private void setupFlattenedList() {
        // All contacts should have a header view (even if there is no data for the contact).
        // gionee xuhz 20120830 add for CR00680623 start
        // aurora <ukiliu> <2013-9-25> modify for aurora ui begin
        if (ContactsApplication.sIsGnContactsSupport) {
            if (mStaticPhotoView == null) {
//                mAllEntries.add(new HeaderViewEntry());
            }
        } else {
//            mAllEntries.add(new HeaderViewEntry());
        }
        // gionee xuhz 20120830 add for CR00680623 end

        addPhoneticName();

        addOrganizationInfo();

        flattenList(mPhoneEntries);
        flattenList(mSmsEntries);
        flattenList(mEmailEntries);
        flattenList(mImEntries);
        flattenList(mNicknameEntries);
        flattenList(mWebsiteEntries);

        addNetworks();

        flattenList(mSipEntries);
        flattenList(mPostalEntries);
        flattenList(mEventEntries);
        flattenList(mGroupEntries);
        flattenList(mRelationEntries);
//        flattenList(mNoteEntries);
		// aurora <ukiliu> <2013-9-25> modify for aurora ui end
    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui begin
    private void addOrganizationInfo() {
    	String company = ContactDetailDisplayUtils.getCompany(mContext, mContactData);
    	String position = ContactDetailDisplayUtils.getPosition(mContext, mContactData);
    	ArrayList<String> organization = new ArrayList<String>();
    	if (!TextUtils.isEmpty(company)) {
    		organization.add(company);
        }
    	
        if (!TextUtils.isEmpty(position)) {
        	organization.add(position);
        }
        
        if (organization.size() == 0) {
        	return;
        }

        final OrganizationViewEntry entry = new OrganizationViewEntry();
        entry.mimetype = Organization.CONTENT_ITEM_TYPE;
//        entry.kind = mContext.getString(R.string.name_phonetic);
        entry.organizationInfo = organization;
        entry.typeHeight = organization.size();
        entry.data = organization.toString();

        Log.i(TAG, "addOrganizationInfo organization.data : " + entry.data);

        mAllEntries.add(entry);
    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui end

    /**
     * Add phonetic name (if applicable) to the aggregated list of contact details. This has to be
     * done manually because phonetic name doesn't have a mimetype or action intent.
     */
    private void addPhoneticName() {
        String phoneticName = ContactDetailDisplayUtils.getPhoneticName(mContext, mContactData);
        if (TextUtils.isEmpty(phoneticName)) {
            return;
        }

        // Add a title
        String phoneticNameKindTitle = mContext.getString(R.string.name_phonetic);
        mAllEntries.add(new KindTitleViewEntry(phoneticNameKindTitle.toUpperCase()));

        // Add the phonetic name
        final DetailViewEntry entry = new DetailViewEntry();
        entry.kind = phoneticNameKindTitle;
        entry.data = phoneticName;
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: xxx CR ID:
         * ALPS00246021 Descriptions:
         */
        if (mOptr) {
            entry.data = "! " + phoneticName;
            Log.i(TAG, "addPhoneticName phoneticName : " + entry.data);

        }
        /*
         * Bug Fix by Mediatek End.
         */
        mAllEntries.add(entry);
    }

    /**
     * Add attribution and other third-party entries (if applicable) under the "networks" section
     * of the aggregated list of contact details. This has to be done manually because the
     * attribution does not have a mimetype and the third-party entries don't have actually belong
     * to the same {@link DataKind}.
     */
    private void addNetworks() {
        String attribution = ContactDetailDisplayUtils.getAttribution(mContext, mContactData);
        boolean hasAttribution = !TextUtils.isEmpty(attribution);
        int networksCount = mOtherEntriesMap.keySet().size();

        // Note: invitableCount will always be 0 for me profile.  (ContactLoader won't set
        // invitable types for me profile.)
        int invitableCount = mContactData.getInvitableAccountTypes().size();
        if (!hasAttribution && networksCount == 0 && invitableCount == 0) {
            return;
        }

        // Add a title
        String networkKindTitle = mContext.getString(R.string.connections);
        // aurora <wangth> <2014-3-8> remove for aurora begin
        /*
        mAllEntries.add(new KindTitleViewEntry(networkKindTitle.toUpperCase()));
        */
        // aurora <wangth> <2014-3-8> remove for aurora end

        // Add the attribution if applicable
        if (hasAttribution) {
            final DetailViewEntry entry = new DetailViewEntry();
            entry.kind = networkKindTitle;
            entry.data = attribution;
            mAllEntries.add(entry);

            // Add a divider below the attribution if there are network details that will follow
            if (networksCount > 0) {
                mAllEntries.add(new SeparatorViewEntry());
            }
        }

        // Add the other entries from third parties
        for (AccountType accountType : mOtherEntriesMap.keySet()) {

            // aurora <wangth> <2014-3-8> remove for aurora begin
            /*
            // Add a title for each third party app
            mAllEntries.add(NetworkTitleViewEntry.fromAccountType(mContext, accountType));
            */
            // aurora <wangth> <2014-3-8> remove for aurora end

            for (DetailViewEntry detailEntry : mOtherEntriesMap.get(accountType)) {
                // Add indented separator
                SeparatorViewEntry separatorEntry = new SeparatorViewEntry();
                separatorEntry.setIsInSubSection(true);
                mAllEntries.add(separatorEntry);

                // Add indented detail
                detailEntry.setIsInSubSection(true);
                mAllEntries.add(detailEntry);
            }
        }

        mOtherEntriesMap.clear();

        // Add the "More networks" button, which opens the invitable account type list popup.
        if (invitableCount > 0) {
            addMoreNetworks();
        }
    }

    /**
     * Add the "More networks" entry.  When clicked, show a popup containing a list of invitable
     * account types.
     */
    private void addMoreNetworks() {
        // First, prepare for the popup.

        // Adapter for the list popup.
        final InvitableAccountTypesAdapter popupAdapter = new InvitableAccountTypesAdapter(mContext,
                mContactData);

        // Listener called when a popup item is clicked.
        final AdapterView.OnItemClickListener popupItemListener
                = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                if (mListener != null && mContactData != null) {
                    mListener.onItemClicked(ContactsUtils.getInvitableIntent(
                            popupAdapter.getItem(position) /* account type */,
                            mContactData.getLookupUri()));
                }
            }
        };

        // Then create the click listener for the "More network" entry.  Open the popup.
        View.OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showListPopup(v, popupAdapter, popupItemListener);
            }
        };

        // Finally create the entry.
        mAllEntries.add(NetworkTitleViewEntry.forMoreNetworks(mContext, onClickListener));
    }

    /**
     * Iterate through {@link DetailViewEntry} in the given list and add it to a list of all
     * entries. Add a {@link KindTitleViewEntry} at the start if the length of the list is not 0.
     * Add {@link SeparatorViewEntry}s as dividers as appropriate. Clear the original list.
     */
    private void flattenList(ArrayList<DetailViewEntry> entries) {
        int count = entries.size();

        // Add a title for this kind by extracting the kind from the first entry
        if (count > 0) {
            String kind = entries.get(0).kind;
//            mAllEntries.add(new KindTitleViewEntry(kind.toUpperCase()));
        }

        // Add all the data entries for this kind
        for (int i = 0; i < count; i++) {
            // For all entries except the first one, add a divider above the entry
            if (i != 0) {
//                mAllEntries.add(new SeparatorViewEntry());
            }
            mAllEntries.add(entries.get(i));
        }

        // Clear old list because it's not needed anymore.
        entries.clear();
    }

    /**
     * Maps group ID to the corresponding group name, collapses all synonymous groups.
     * Ignores default groups (e.g. My Contacts) and favorites groups.
     */
    private void handleGroupMembership(
            ArrayList<String> groups, List<GroupMetaData> groupMetaData, long groupId) {
        if (groupMetaData == null) {
            return;
        }

        for (GroupMetaData group : groupMetaData) {
            if (group.getGroupId() == groupId) {
                if (!group.isDefaultGroup() && !group.isFavorites()) {
                    String title = group.getTitle();
                    if (!groups.contains(title)) {
                        groups.add(title);
                    }
                }
                break;
            }
        }
    }

    private static String buildDataString(DataKind kind, ContentValues values,
            Context context) {
        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(context, values);
        return actionBody == null ? null : actionBody.toString();
    }

    // aurora <ukiliu> <2013-9-25> add for aurora ui begin
    private static ArrayList<String> buildOrganizationInfo(DataKind kind, ContentValues values,
            Context context) {
    	ArrayList<String> valueStrings = new ArrayList<String>();

    		if (kind.actionBody == null && kind.actionHeader == null) {
                return null;
            } else {
            	CharSequence actionBody = "";
            	CharSequence actionHeader = "";
            	if (kind.actionHeader != null) {
            		actionHeader = kind.actionHeader.inflateUsing(context, values);
            	}
            	if (kind.actionBody != null) {
            		actionBody = kind.actionBody.inflateUsing(context, values);
            	}
            	
            	if (!TextUtils.isEmpty(actionHeader)) {
            		valueStrings.add(actionHeader.toString());
            	}
            	if (!TextUtils.isEmpty(actionBody)) {
            		valueStrings.add(actionBody.toString());
            	}
            	return valueStrings;
            }
    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui end
    /**
     * Writes the Instant Messaging action into the given entry value.
     */
    @VisibleForTesting
    public static void buildImActions(Context context, DetailViewEntry entry,
            ContentValues values) {
        final boolean isEmail = Email.CONTENT_ITEM_TYPE.equals(values.getAsString(Data.MIMETYPE));

        if (!isEmail && !isProtocolValid(values)) {
            return;
        }

        final String data = values.getAsString(isEmail ? Email.DATA : Im.DATA);
        if (TextUtils.isEmpty(data)) {
            return;
        }

        final int protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK : values.getAsInteger(Im.PROTOCOL);

        if (protocol == Im.PROTOCOL_GOOGLE_TALK) {
            final Integer chatCapabilityObj = values.getAsInteger(Im.CHAT_CAPABILITY);
            final int chatCapability = chatCapabilityObj == null ? 0 : chatCapabilityObj;
            entry.chatCapability = chatCapability;
            entry.typeString = Im.getProtocolLabel(context.getResources(), Im.PROTOCOL_GOOGLE_TALK,
                    null).toString();
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: xxx CR ID:
             * ALPS00246021 Descriptions:
             */

            if (mOptr) {
                entry.typeString = "! "
                        + Im
                                .getProtocolLabel(context.getResources(), Im.PROTOCOL_GOOGLE_TALK,
                                        null).toString();
                Log.i(TAG, "************ entry.typeString : " + entry.typeString);
            }

            /*
             * Bug Fix by Mediatek End.
             */

            if ((chatCapability & Im.CAPABILITY_HAS_CAMERA) != 0) {
                entry.intent =
                        new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?message"));
                entry.secondaryIntent =
                        new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?call"));
            } else if ((chatCapability & Im.CAPABILITY_HAS_VOICE) != 0) {
                // Allow Talking and Texting
                entry.intent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?message"));
                entry.secondaryIntent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?call"));
            } else {
                entry.intent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?message"));
            }
        } else {
            // Build an IM Intent
            String host = values.getAsString(Im.CUSTOM_PROTOCOL);

            if (protocol != Im.PROTOCOL_CUSTOM) {
                // Try bringing in a well-known host for specific protocols
                host = ContactsUtils.lookupProviderNameFromId(protocol);
            }

            if (!TextUtils.isEmpty(host)) {
                final String authority = host.toLowerCase();
                final Uri imUri = new Uri.Builder().scheme(Constants.SCHEME_IMTO).authority(
                        authority).appendPath(data).build();
                entry.intent = new Intent(Intent.ACTION_SENDTO, imUri);
            }
        }
    }

    private static boolean isProtocolValid(ContentValues values) {
        String protocolString = values.getAsString(Im.PROTOCOL);
        if (protocolString == null) {
            return false;
        }
        try {
            Integer.valueOf(protocolString);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Show a list popup.  Used for "popup-able" entry, such as "More networks".
     */
    private void showListPopup(View anchorView, ListAdapter adapter,
            final AdapterView.OnItemClickListener onItemClickListener) {
        final ListPopupWindow popup = new ListPopupWindow(mContext, null);
        popup.setAnchorView(anchorView);
        popup.setWidth(anchorView.getWidth());
        popup.setAdapter(adapter);
        popup.setModal(true);

        // We need to wrap the passed onItemClickListener here, so that we can dismiss() the
        // popup afterwards.  Otherwise we could directly use the passed listener.
        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                onItemClickListener.onItemClick(parent, view, position, id);
                popup.dismiss();
            }
        });
        popup.show();
    }

    /**
     * Base class for an item in the {@link ViewAdapter} list of data, which is
     * supplied to the {@link ListView}.
     */
    static class ViewEntry {
        private final int viewTypeForAdapter;
        protected long id = -1;
        public int typeHeight = 0;
        /** Whether or not the entry can be focused on or not. */
        protected boolean isEnabled = false;

        ViewEntry(int viewType) {
            viewTypeForAdapter = viewType;
        }

        int getViewType() {
            return viewTypeForAdapter;
        }

        long getId() {
            return id;
        }

        boolean isEnabled(){
            return isEnabled;
        }

        /**
         * Called when the entry is clicked.  Only {@link #isEnabled} entries can get clicked.
         *
         * @param clickedView  {@link View} that was clicked  (Used, for example, as the anchor view
         *        for a popup.)
         * @param fragmentListener  {@link Listener} set to {@link AuroraContactDetailFragment}
         */
        public void click(View clickedView, Listener fragmentListener) {
        }
    }

    /**
     * Header item in the {@link ViewAdapter} list of data.
     */
    private static class HeaderViewEntry extends ViewEntry {

        HeaderViewEntry() {
            super(ViewAdapter.VIEW_TYPE_HEADER_ENTRY);
        }

    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui begin
    private static class OrganizationViewEntry extends ViewEntry {

    	public int type = -1;
        public String kind;
        public String typeString;
        public String data;
        public Uri uri;
        public int maxLines = 1;
        public String mimetype;

        public Context context = null;
        public String resPackageName = null;
        public int collapseCount = 0;
        public DataKind mKind;
        public ArrayList<String> organizationInfo;
        
    	OrganizationViewEntry() {
            super(ViewAdapter.VIEW_TYPE_ORGANIZATION_ENTRY);
        }
    	
    	public static OrganizationViewEntry fromValues(Context context, String mimeType, DataKind kind,
                 long dataId, ContentValues values) {
    		final OrganizationViewEntry entry = new OrganizationViewEntry();
            entry.id = dataId;
            entry.context = context;
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            entry.mimetype = Organization.CONTENT_ITEM_TYPE;
            entry.kind = (kind.titleRes == -1 || kind.titleRes == 0) ? ""
                    : context.getString(kind.titleRes);

            Log.i(TAG, "OrganizationViewEntry::"+entry.mimetype);
        	entry.mKind = kind;
            entry.organizationInfo = buildOrganizationInfo(kind, values, context);
            entry.typeHeight = entry.organizationInfo.size();
            entry.data = entry.organizationInfo.toString();
            Log.d(TAG, "fromValues(), kind.mimeType= " + kind.mimeType);
            Log.i(TAG,"fromValues(),typeHeight = "+entry.typeHeight);
            	
            entry.resPackageName = kind.resPackageName;
            entry.typeString = "";

            Log.i(TAG,"return entry = "+entry.typeString);
            Log.i(TAG,"return entry.data = "+entry.data);
            return entry;
    	}
    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui end
    /**
     * Separator between items of the same {@link DataKind} in the
     * {@link ViewAdapter} list of data.
     */
    private static class SeparatorViewEntry extends ViewEntry {

        /**
         * Whether or not the entry is in a subsection (if true then the contents will be indented
         * to the right)
         */
        private boolean mIsInSubSection = false;

        SeparatorViewEntry() {
            super(ViewAdapter.VIEW_TYPE_SEPARATOR_ENTRY);
        }

        public void setIsInSubSection(boolean isInSubSection) {
            mIsInSubSection = isInSubSection;
        }

        public boolean isInSubSection() {
            return mIsInSubSection;
        }
    }

    /**
     * Title entry for items of the same {@link DataKind} in the
     * {@link ViewAdapter} list of data.
     */
    private static class KindTitleViewEntry extends ViewEntry {

        private final String mTitle;

        KindTitleViewEntry(String titleText) {
            super(ViewAdapter.VIEW_TYPE_KIND_TITLE_ENTRY);
            mTitle = titleText;
        }

        public String getTitle() {
            return mTitle;
        }
    }

    /**
     * A title for a section of contact details from a single 3rd party network.  It's also
     * used for the "More networks" entry, which has the same layout.
     */
    private static class NetworkTitleViewEntry extends ViewEntry {
        private final Drawable mIcon;
        private final CharSequence mLabel;
        private final View.OnClickListener mOnClickListener;

        private NetworkTitleViewEntry(Drawable icon, CharSequence label, View.OnClickListener
                onClickListener) {
            super(ViewAdapter.VIEW_TYPE_NETWORK_TITLE_ENTRY);
            this.mIcon = icon;
            this.mLabel = label;
            this.mOnClickListener = onClickListener;
            this.isEnabled = false;
        }

        public static NetworkTitleViewEntry fromAccountType(Context context, AccountType type) {
            return new NetworkTitleViewEntry(
                    type.getDisplayIcon(context), type.getDisplayLabel(context), null);
        }

        public static NetworkTitleViewEntry forMoreNetworks(Context context, View.OnClickListener
                onClickListener) {
            // TODO Icon is temporary.  Need proper one.
            return new NetworkTitleViewEntry(
                    context.getResources().getDrawable(R.drawable.ic_menu_add_field_holo_light),
                    context.getString(R.string.add_connection_button),
                    onClickListener);
        }

        @Override
        public void click(View clickedView, Listener fragmentListener) {
            if (mOnClickListener == null) return;
            mOnClickListener.onClick(clickedView);
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public CharSequence getLabel() {
            return mLabel;
        }
    }

    /**
     * An item with a single detail for a contact in the {@link ViewAdapter}
     * list of data.
     */
    static class DetailViewEntry extends ViewEntry implements Collapsible<DetailViewEntry> {
        // TODO: Make getters/setters for these fields
        public int type = -1;
        public String kind;
        public String typeString;
        public String data;
        public Uri uri;
        public int maxLines = 1;
        public String mimetype;

        public Context context = null;
        public String resPackageName = null;
        public boolean isPrimary = false;
        public int secondaryActionIcon = -1;
        public int secondaryActionDescription = -1;
        public Intent intent;
        public Intent secondaryIntent = null;
        public ArrayList<Long> ids = new ArrayList<Long>();
        public int collapseCount = 0;
        public DataKind mKind;
        public ArrayList<String> organizationInfo;

        public int presence = -1;
        public int chatCapability = 0;

        public CharSequence footerLine = null;

        private boolean mIsInSubSection = false;

        /*
         * New Feature by Mediatek Begin.            
         * save the association's sim id 
         */
        public int simId = -1;
        /*
         * New Feature  by Mediatek End.
         */ 
        
        DetailViewEntry() {
            super(ViewAdapter.VIEW_TYPE_DETAIL_ENTRY);
            isEnabled = true;
        }

        /**
         * Build new {@link DetailViewEntry} and populate from the given values.
         */
        public static DetailViewEntry fromValues(Context context, String mimeType, DataKind kind,
                long dataId, ContentValues values, boolean isDirectoryEntry, long directoryId) {
            final DetailViewEntry entry = new DetailViewEntry();
            entry.id = dataId;
            entry.context = context;
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            if (isDirectoryEntry) {
                entry.uri = entry.uri.buildUpon().appendQueryParameter(
                        GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
            }
            entry.mimetype = mimeType;
            entry.kind = (kind.titleRes == -1 || kind.titleRes == 0) ? ""
                    : context.getString(kind.titleRes);
            //entry.data = buildDataString(kind, values, context);
            String tempData = buildDataString(kind, values, context);
            Log.d(TAG, "fromValues(), kind.mimeType= " + kind.mimeType);
            Log.i(TAG,"fromValues(),tempData = "+tempData);
            // aurora <ukiliu> <2013-9-25> add for aurora ui begin
            if (kind.mimeType == Phone.CONTENT_ITEM_TYPE) {
            	entry.typeHeight = 2;
            } else {
            	entry.typeHeight = 1;
            }
            // aurora <ukiliu> <2013-9-25> add for aurora ui end
            if (mOptr && kind.mimeType == SipAddress.CONTENT_ITEM_TYPE && tempData != null){
                Log.i(TAG,"SipAddress.CONTENT_ITEM_TYPE tempData = "+tempData);
                entry.data = "! " + tempData;

            } else if (mOptr && kind.mimeType == Nickname.CONTENT_ITEM_TYPE && tempData != null) {
                Log.i(TAG, "Nickname.CONTENT_ITEM_TYPE tempData = " + tempData);
                entry.data = "! " + tempData;
            // aurora <ukiliu> <2013-9-25> add for aurora ui begin
            } else if (kind.mimeType == Organization.CONTENT_ITEM_TYPE) {
            	Log.i(TAG, "Organization.CONTENT_ITEM_TYPE::"+entry.mimetype);
            	tempData = "";
            	entry.mKind = kind;
                entry.organizationInfo = buildOrganizationInfo(kind, values, context);
                for (int i = 0; i<entry.organizationInfo.size();i++) {
                	tempData = tempData + entry.organizationInfo.get(i);
                }
                entry.typeHeight = entry.organizationInfo.size();
                entry.data = tempData;
            // aurora <ukiliu> <2013-9-25> add for aurora ui end
            } else {
                entry.data = tempData;
            }
            entry.resPackageName = kind.resPackageName;
            
            if (kind.typeColumn != null && values.containsKey(kind.typeColumn)) {
                entry.type = values.getAsInteger(kind.typeColumn);

                // get type string
                entry.typeString = "";
                for (EditType type : kind.typeList) {
                    if (type.rawValue == entry.type) {
                        if (mOptr) {
                            if (type.customColumn == null) {
                                // Non-custom type. Get its description from the
                                // resource
                                if (isUnSync(kind, values)) {
                                    Log.i(TAG,"isUnSync is true kind.mimeType : "+kind.mimeType+" | context.getString(type.labelRes) : "+context.getString(type.labelRes));
                                    entry.typeString = "! " + context.getString(type.labelRes);

                                } else {
                                    Log.i(TAG,"isUnSync is false kind.mimeType : "+kind.mimeType+" | context.getString(type.labelRes) : "+context.getString(type.labelRes));
                                    entry.typeString = context.getString(type.labelRes);
                                }
                            } else {
                                // Custom type. Read it from the database
                                entry.typeString = "! " + values.getAsString(type.customColumn);
                            }
                        } else {
                            if (type.customColumn == null) {
                                // Non-custom type. Get its description from the
                                // resource
                                entry.typeString = context.getString(type.labelRes);
                            } else {
                                // Custom type. Read it from the database
                                entry.typeString = values.getAsString(type.customColumn);
                            }
                        }
                        
                        break;
                    }
                    
                    // aurora <wangth> <2014-3-3> add for aurora begin
                    if (kind.mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                        if (values.containsKey("data2")) {
                            if (values.getAsString("data2") != null && values.getAsString("data2").equals("0")) {
                                if (values.containsKey("data3") && values.getAsString("data3") != null) {
                                    entry.typeString = values.getAsString("data3");
                                }
                            }
                        }
                    }
                    // aurora <wangth> <2014-3-3> add for aurora end
                }
            } else {
                entry.typeString = "";
            }
            // aurora ukiliu 2013-11-21 add for aurora ui begin
            if (kind.mimeType == Event.CONTENT_ITEM_TYPE){
                entry.typeString = context.getString(R.string.eventLabelsGroup);
            }
            //aurora ukiliu 2013-11-21 add for aurora ui end
            
            // aurora <wangth> <2014-3-3> add for aurora begin
            if (kind.mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                if (entry.typeString.isEmpty()) {
                    entry.typeString = context.getString(R.string.gn_phone_label);
                }
            }
            // aurora <wangth> <2014-3-3> add for aurora end
            
            Log.i(TAG,"return entry = "+entry.typeString);
            Log.i(TAG,"return entry.data = "+entry.data);
            return entry;
        }

        /**
         * Apply given {@link DataStatus} values over this {@link DetailViewEntry}
         *
         * @param fillData When true, the given status replaces {@link #data}
         *            and {@link #footerLine}. Otherwise only {@link #presence}
         *            is updated.
         */
        public DetailViewEntry applyStatus(DataStatus status, boolean fillData) {
            presence = status.getPresence();
            if (fillData && status.isValid()) {
                this.data = status.getStatus().toString();
                this.footerLine = status.getTimestampLabel(context);
            }

            return this;
        }

        public void setIsInSubSection(boolean isInSubSection) {
            mIsInSubSection = isInSubSection;
        }

        public boolean isInSubSection() {
            return mIsInSubSection;
        }

        @Override
        public boolean collapseWith(DetailViewEntry entry) {
            // assert equal collapse keys
            if (!shouldCollapseWith(entry)) {
                return false;
            }

            // Choose the label associated with the highest type precedence.
            if (TypePrecedence.getTypePrecedence(mimetype, type)
                    > TypePrecedence.getTypePrecedence(entry.mimetype, entry.type)) {
                type = entry.type;
                kind = entry.kind;
                typeString = entry.typeString;
            }

            // Choose the max of the maxLines and maxLabelLines values.
            maxLines = Math.max(maxLines, entry.maxLines);

            // Choose the presence with the highest precedence.
            if (StatusUpdates.getPresencePrecedence(presence)
                    < StatusUpdates.getPresencePrecedence(entry.presence)) {
                presence = entry.presence;
            }

            // If any of the collapsed entries are primary make the whole thing primary.
            isPrimary = entry.isPrimary ? true : isPrimary;

            // uri, and contactdId, shouldn't make a difference. Just keep the original.

            // Keep track of all the ids that have been collapsed with this one.
            ids.add(entry.getId());
            collapseCount++;
            return true;
        }

        @Override
        public boolean shouldCollapseWith(DetailViewEntry entry) {
            if (entry == null) {
                return false;
            }

            if (!ContactsUtils.shouldCollapse(mimetype, data, entry.mimetype, entry.data)) {
                return false;
            }

            if (!TextUtils.equals(mimetype, entry.mimetype)
                    || !ContactsUtils.areIntentActionEqual(intent, entry.intent)
                    || !ContactsUtils.areIntentActionEqual(
                            secondaryIntent, entry.secondaryIntent)) {
                return false;
            }

            return true;
        }

        @Override
        public void click(View clickedView, Listener fragmentListener) {
            if (fragmentListener == null || intent == null) return;
            
			if (FeatureOption.MTK_GEMINI_SUPPORT) { // aurora add zhouxiaobing 20140504 for dual sim
				final ViewEntry entry = (ViewEntry) clickedView.getTag();
				if (entry == null)
					return;
				if (entry instanceof DetailViewEntry
						&& ((DetailViewEntry) entry).mimetype != null
						&& ((DetailViewEntry) entry).mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
					final Listener lis = fragmentListener;
					if (ContactsUtils.isShowDoubleButton() 
							&& SubContactsUtils.simStateReady(0) && SubContactsUtils.simStateReady(1)) {
						final String number = ((DetailViewEntry) entry).data.replaceAll(" ", "");
						int lastCallSlotId = ContactsUtils.getLastCallSlotId(mContext, number);
						String recentCall = context.getString(R.string.aurora_recent_call);
						String menuSlot0 = context.getString(R.string.aurora_slot_0) + context.getString(R.string.gn_dial_desciption);
						String menuSlot1 = context.getString(R.string.aurora_slot_1) + context.getString(R.string.gn_dial_desciption);

						if (lastCallSlotId == 0) {
							menuSlot0 = menuSlot0 + recentCall;
						} else if (lastCallSlotId == 1) {
							menuSlot1 = menuSlot1 + recentCall;
						}

						try {
							try {
								((AuroraActivity) mContext).removeMenuById(0);
								((AuroraActivity) mContext).removeMenuById(1);
								((AuroraActivity) mContext).removeMenuById(2);
							} catch (Exception e) {
								e.printStackTrace();
							}
							((AuroraActivity) mContext).addMenu(0, menuSlot1,
									new OnMenuItemClickLisener() {
										public void onItemClick(View menu) {
											lis.onItemClicked(getCallIntent(number, 1));
										}
									});
							((AuroraActivity) mContext).addMenu(1, menuSlot0,
									new OnMenuItemClickLisener() {
										public void onItemClick(View menu) {
											lis.onItemClicked(getCallIntent(number, 0));
										}
									});
							((AuroraActivity) mContext).showCustomMenu();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (SimCardUtils.isSimInserted(0) && !hasClicked && SubContactsUtils.simStateReady(0)) {
						hasClicked = true;
						lis.onItemClicked(getCallIntent(data, 0));
					} else if (!hasClicked) {
						hasClicked = true;
						lis.onItemClicked(getCallIntent(data, 1));
					}

					return;
				}
			}
    		
            fragmentListener.onItemClicked(intent);
        }
        
        private Intent getCallIntent(String number, int slot) {
        	Intent intent = AuroraTelephoneManager.getCallNumberIntent(number, slot);
        	intent.putExtra("contactUri", mCallUri);
        	return intent;
        }
    }

    /**
     * Cache of the children views for a view that displays a header view entry.
     */
    private static class HeaderViewCache {
        // aurora <ukiliu> <2013-9-25> modify for aurora ui begin
        public final TextView displayNameView;
        public final TextView SpringView;
//        public final TextView companyView;
        public final ImageView photoView;
        public final CheckBox starredView;
        //public final int layoutResourceId;
		public final TextView noteView;
		public final View photocontainer;

        public HeaderViewCache(View view) {
            displayNameView = (TextView) view.findViewById(R.id.name);
            SpringView = (TextView) view.findViewById(R.id.spring);
//            companyView = (TextView) view.findViewById(R.id.company);
            photoView = (ImageView) view.findViewById(R.id.photo);
            photocontainer =  view.findViewById(R.id.contact_photo_container);
            photocontainer.setVisibility(View.INVISIBLE);
            starredView = (CheckBox) view.findViewById(R.id.star);
            //layoutResourceId = layoutResourceInflated;
			noteView = (TextView) view.findViewById(R.id.note);
        }
        // aurora <ukiliu> <2013-9-25> modify for aurora ui end
    }

    /**
     * Cache of the children views for a view that displays a {@link NetworkTitleViewEntry}
     */
    private static class NetworkTitleViewCache {
        public final TextView name;
        public final ImageView icon;

        public NetworkTitleViewCache(View view) {
            name = (TextView) view.findViewById(R.id.network_title);
            icon = (ImageView) view.findViewById(R.id.network_icon);
        }
    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui begin
    private static class OrganizationViewCache {
        public final TextView primaryTextView;
        public final TextView secondaryTextView;
        public final TextView primaryTextOnlyView;
        public final View simpleLineContainer;
        public final View doubleLineContainer;
        public final int layoutResourceId;

        public OrganizationViewCache(View view, int layoutResourceInflated) {
        	primaryTextView = (TextView) view.findViewById(R.id.primary_text_view);
        	secondaryTextView = (TextView) view.findViewById(R.id.secondary_text_view);
        	primaryTextOnlyView = (TextView) view.findViewById(R.id.primary_text_only_view);
        	simpleLineContainer = view.findViewById(R.id.simpleline);
        	doubleLineContainer = view.findViewById(R.id.doubleline);
        	layoutResourceId = layoutResourceInflated;
        }
    }
    // aurora <ukiliu> <2013-9-25> add for aurora ui end

    /**
     * Cache of the children views of a contact detail entry represented by a
     * {@link DetailViewEntry}
     */
    private static class DetailViewCache {
        public final TextView type;
        public final TextView data;
        public final TextView footer;
        public final ImageView presenceIcon;
        public final ImageView secondaryActionButton;
        public final View actionsViewContainer;
        public final View primaryActionView;
        public final View secondaryActionViewContainer;
        public final View primaryIndicator;

        /*
         * New Feature by Mediatek Begin.            
         * save the newly view's object 
         */
        public final ImageView imgAssociationSimIcon;
        public final TextView  txtAssociationSimName;       
        public final View vtcallActionViewContainer;
        public final ImageView btnVtCallAction;
        /*
         * New Feature  by Mediatek End.
        */
        
        //gionee xuhz 20120530 add for gn theme start
        public TextView  gnNumberArea = null;
        //gionee xuhz 20120530 add for gn theme end

        // Gionee <jialf> <2013-04-16> added for CR00798010 begin
        public final View gnActionsViewContainer;
        public final View dialActionViewContainer;
        public final ImageView btnDailAction;
        // Gionee <jialf> <2013-04-16> added for CR00798010 end

        /*
         * New Feature by Mediatek Begin. 
         * Original Android?s code:
        public DetailViewCache(View view,
                OnClickListener primaryActionClickListener,
                OnClickListener secondaryActionClickListener) {
         * add the vtcall button's onClickListener           
         */
        public DetailViewCache(View view,
                OnClickListener primaryActionClickListener,
                final OnClickListener secondaryActionClickListener, OnClickListener vtCallActionClickListener) {
        /*
         * New Feature  by Mediatek End.
         */            
            type = (TextView) view.findViewById(R.id.type);
            data = (TextView) view.findViewById(R.id.data);
            footer = (TextView) view.findViewById(R.id.footer);
            primaryIndicator = view.findViewById(R.id.primary_indicator);
            presenceIcon = (ImageView) view.findViewById(R.id.presence_icon);

            actionsViewContainer = view.findViewById(R.id.actions_view_container);
//            actionsViewContainer.setOnClickListener(primaryActionClickListener); // aurora wangth 20140816 set listener external
            primaryActionView = view.findViewById(R.id.primary_action_view);

            gnActionsViewContainer = null;
            dialActionViewContainer = null;
            btnDailAction = null;
            
            secondaryActionViewContainer = view.findViewById(
                    R.id.secondary_action_view_container);
//            secondaryActionViewContainer.setOnClickListener(
//                    secondaryActionClickListener);
            secondaryActionViewContainer.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					secondaryActionClickListener.onClick(view);
			        StatisticsUtil.getInstance(mContext.getApplicationContext()).report(StatisticsUtil.Detail_Send_Mms);
				}
			});
            secondaryActionButton = (ImageView) view.findViewById(
                    R.id.secondary_action_button);

            
            /*
             * New Feature by Mediatek Begin.            
             * get the newly view's object  
             */
            vtcallActionViewContainer = view.findViewById(R.id.vtcall_action_view_container);
            if (null != vtcallActionViewContainer)
            	vtcallActionViewContainer.setOnClickListener(vtCallActionClickListener);            
            imgAssociationSimIcon = (ImageView) view.findViewById(R.id.association_sim_icon);
            txtAssociationSimName = (TextView) view.findViewById(R.id.association_sim_text);   
            btnVtCallAction = (ImageView) view.findViewById(R.id.vtcall_action_button);  
            /*
             * New Feature  by Mediatek End.
            */

            gnNumberArea = (TextView) view.findViewById(R.id.gn_number_area);
        }
    }

    private final class ViewAdapter extends BaseAdapter {

    	public static final int VIEW_TYPE_DETAIL_ENTRY = 0;
        public static final int VIEW_TYPE_HEADER_ENTRY = 1;
		// aurora <ukiliu> <2013-9-25> add for aurora ui begin
        public static final int VIEW_TYPE_ORGANIZATION_ENTRY = 1;
		// aurora <ukiliu> <2013-9-25> add for aurora ui end
        public static final int VIEW_TYPE_KIND_TITLE_ENTRY = 2;
        public static final int VIEW_TYPE_NETWORK_TITLE_ENTRY = 3;
        public static final int VIEW_TYPE_SEPARATOR_ENTRY = 4;
        // Gionee <jialf> <2013-04-16> modified for CR00798010 begin
        private static final int VIEW_TYPE_COUNT = 6;
        private int viewSize;
        // Gionee <jialf> <2013-04-16> modified for CR00798010 end
                
        private int[] mGgkjBgIndex;
        private void filterEntriesShow(ArrayList<ViewEntry> allEntries) {
        	ArrayList<ViewEntry> entriesShow = new ArrayList<AuroraContactDetailFragment.ViewEntry>();
    		
    		for (ViewEntry ve : allEntries) {
    			if (ve.getViewType() != VIEW_TYPE_SEPARATOR_ENTRY) {
    				entriesShow.add(ve);
    			}
    		}
    		
    		mAllEntries.clear();
    		mAllEntries.addAll(entriesShow);
    		
    		final int size = entriesShow.size();
    		viewSize = size;
    		mGgkjBgIndex = new int[size];
    		String[] mimeType = new String[size];
    		for (int i = 0; i < size; ++i) {
    			ViewEntry ve = entriesShow.get(i);
    			if (ve.getViewType() == VIEW_TYPE_DETAIL_ENTRY) {
    				mimeType[i] = ((DetailViewEntry)ve).mimetype;
    				if (null == mimeType[i]) {
    					mimeType[i] = "unkown"; 
    				}
    			}
    		}
    		for (int i = 0; i < size; ++i) {
    			if (null != mimeType[i]) {
    				String pre = i > 0 ? mimeType[i - 1] : null;
    				String next = i + 1 < size ? mimeType[i + 1] : null;
    				boolean equalsPre = mimeType[i].equals(pre);
    				boolean equalsNext = mimeType[i].equals(next);
    				
    				if (!equalsPre && !equalsNext) {
    					if (ContactsApplication.sIsGnDarkStyle) {
        					mGgkjBgIndex[i] = R.drawable.gn_dial_buttons_bar_bg_dark;
    					} else {
        					mGgkjBgIndex[i] = color.white;
    					}
    				} else if (equalsPre && equalsNext) {
    					if (ContactsApplication.sIsGnDarkStyle) {
         					mGgkjBgIndex[i] = R.drawable.gn_button_bg_middle_dark;
    					} else {
         					mGgkjBgIndex[i] = color.white;
    					}
     				} else if (equalsNext) {
    					if (ContactsApplication.sIsGnDarkStyle) {
        					mGgkjBgIndex[i] = R.drawable.gn_button_bg_top_dark;
    					} else {
        					mGgkjBgIndex[i] = color.white;
    					}
    				} else {
    					if (ContactsApplication.sIsGnDarkStyle) {
    						mGgkjBgIndex[i] = R.drawable.gn_button_bg_bottom_dark;
    					} else {
        					mGgkjBgIndex[i] = color.white;
    					}
    				}
    			}
    		}
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	Log.i(TAG,"position::"+position);
            switch (getItemViewType(position)) {
                // aurora <ukiliu> <2013-9-25> modify for aurora ui begin
                case VIEW_TYPE_ORGANIZATION_ENTRY:
                	Log.i("liumxxx","VIEW_TYPE_ORGANIZATION_ENTRY");
                    return getOrganizationEntryView(position, convertView, parent);
                // aurora <ukiliu> <2013-9-25> modify for aurora ui end
                case VIEW_TYPE_SEPARATOR_ENTRY:
                	Log.i("liumxxx","VIEW_TYPE_SEPARATOR_ENTRY");
                    return getSeparatorEntryView(position, convertView, parent);
                case VIEW_TYPE_KIND_TITLE_ENTRY:
                	Log.i("liumxxx","VIEW_TYPE_KIND_TITLE_ENTRY");
                    return getKindTitleEntryView(position, convertView, parent);
                case VIEW_TYPE_DETAIL_ENTRY:
                	Log.i("liumxxx","VIEW_TYPE_DETAIL_ENTRY");
                    return getDetailEntryView(position, convertView, parent);
                case VIEW_TYPE_NETWORK_TITLE_ENTRY:
                	Log.i("liumxxx","VIEW_TYPE_NETWORK_TITLE_ENTRY");
                    return getNetworkTitleEntryView(position, convertView, parent);
                default:
                    throw new IllegalStateException("Invalid view type ID " +
                            getItemViewType(position));
            }
        }

        // aurora <ukiliu> <2013-9-25> add for aurora ui begin
        private View getOrganizationEntryView(int position, View convertView, ViewGroup parent) {
    	    final OrganizationViewEntry entry = (OrganizationViewEntry) getItem(position);
	        View result = null;
	        OrganizationViewCache viewCache = null;
	        int desiredLayoutResourceId;
	        desiredLayoutResourceId = R.layout.aurora_detail_organization_item;
	        
	        OnLongClickListener listener = new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (entry != null) {
			    		AuroraAlertDialog dialog = (AuroraAlertDialog) createLongClickDialog(entry);
			    		dialog.show();
					}
		    		return false;
				}
	        	
	        };
	
	        // Only use convertView if it has the same layout resource ID as the one desired
	        // (the two can be different on wide 2-pane screens where the detail fragment is reused
	        // for many different contacts that do and do not have social updates).
	        if (convertView != null) {
	            viewCache = (OrganizationViewCache) convertView.getTag();
	            if (viewCache.layoutResourceId == desiredLayoutResourceId) {
	                result = convertView;
	            }
	        }
	
	        // Otherwise inflate a new header view and create a new view cache.
	        if (result == null) {
	            result = mInflater.inflate(desiredLayoutResourceId, parent, false);
	            viewCache = new OrganizationViewCache(result, desiredLayoutResourceId);
	            result.setTag(viewCache);
	        }

	        if (entry.typeHeight == 1) {
	        	 viewCache.primaryTextOnlyView.setText(entry.organizationInfo.get(0));
	        	 viewCache.doubleLineContainer.setVisibility(View.GONE);
	        	 viewCache.simpleLineContainer.setVisibility(View.VISIBLE);
	        	 result.findViewById(R.id.simpleline).setOnLongClickListener(listener);
	        } else if (entry.typeHeight == 2) {
	        	viewCache.primaryTextView.setText(entry.organizationInfo.get(0));
		        viewCache.secondaryTextView.setText(entry.organizationInfo.get(1));
		        viewCache.simpleLineContainer.setVisibility(View.GONE);
		        viewCache.doubleLineContainer.setVisibility(View.VISIBLE);
		        result.findViewById(R.id.doubleline).setOnLongClickListener(listener);
	        } else {
	        	viewCache.simpleLineContainer.setVisibility(View.GONE);
	        	viewCache.doubleLineContainer.setVisibility(View.GONE);
	        }
	
	        return result;
	    }
        // aurora <ukiliu> <2013-9-25> add for aurora ui end
        
        private View getSeparatorEntryView(int position, View convertView, ViewGroup parent) {
            final SeparatorViewEntry entry = (SeparatorViewEntry) getItem(position);
            final View result = (convertView != null) ? convertView :
                    mInflater.inflate(R.layout.contact_detail_separator_entry_view, parent, false);

            result.setPadding(0,0,0,0);
            result.setVisibility(View.GONE);
            return result;
        }

        private View getKindTitleEntryView(int position, View convertView, ViewGroup parent) {
            final KindTitleViewEntry entry = (KindTitleViewEntry) getItem(position);

            // gionee xuhz 20120607 modify start
            View result;
            if (ContactsApplication.sIsGnContactsSupport) {
                result = (convertView != null) ? convertView :
                    mInflater.inflate(R.layout.gn_list_separator, parent, false);
            } else {
                result = (convertView != null) ? convertView :
                    mInflater.inflate(R.layout.list_separator, parent, false);
            }
            // gionee xuhz 20120607 modify end
            final TextView titleTextView = (TextView) result.findViewById(R.id.title);
            titleTextView.setText(entry.getTitle());
            
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            	titleTextView.setBackgroundDrawable(null);
            }
            // aurora <ukiliu> <2013-9-25> add for aurora ui begin
            result.setVisibility(View.GONE);
            // aurora <ukiliu> <2013-9-25> add for aurora ui end
            return result;
        }

        private View getNetworkTitleEntryView(int position, View convertView, ViewGroup parent) {
            final NetworkTitleViewEntry entry = (NetworkTitleViewEntry) getItem(position);
            final View result;
            final NetworkTitleViewCache viewCache;

            if (convertView != null) {
                result = convertView;
                viewCache = (NetworkTitleViewCache) result.getTag();
            } else {
                result = mInflater.inflate(R.layout.contact_detail_network_title_entry_view,
                        parent, false);
                viewCache = new NetworkTitleViewCache(result);
                result.setTag(viewCache);
                result.findViewById(R.id.primary_action_view).setOnClickListener(
                        entry.mOnClickListener);
            }

            viewCache.name.setText(entry.getLabel());
            viewCache.icon.setImageDrawable(entry.getIcon());

            return result;
        }

        private View getDetailEntryView(int position, View convertView, ViewGroup parent) {
            final DetailViewEntry entry = (DetailViewEntry) getItem(position);
            final View v;
            final DetailViewCache viewCache;

            // Check to see if we can reuse convertView
            if (convertView != null) {
                v = convertView;
                viewCache = (DetailViewCache) v.getTag();
            } else {
                // Create a new view if needed
                v = mInflater.inflate(R.layout.aurora_contact_detail_list_item, parent, false);

                // Cache the children
                viewCache = new DetailViewCache(v, mPrimaryActionClickListener, mSecondaryActionClickListener, mVtCallActionClickListener);
                // aurora wangth 20140816 set listener
                if (!"mimetype".equals(entry.mimetype)) {
                    viewCache.actionsViewContainer.setOnClickListener(mPrimaryActionClickListener);
                }
                v.setTag(viewCache);
            }
            
            if (!"mimetype".equals(entry.mimetype)) {
                v.findViewById(R.id.actions_view_container).setOnLongClickListener(new OnLongClickListener() {
					
					public boolean onLongClick(View v) {
						if (entry != null) {
				    		AuroraAlertDialog dialog = (AuroraAlertDialog) createLongClickDialog(entry);
				    		try {
				    			if (null != getActivity() && !getActivity().isFinishing()) {
				    				dialog.show();
				    			}
				    		} catch (Exception e) {
				    			e.printStackTrace();
				    		}
						}
			    		return false;
					}
				});
            } else {
                v.findViewById(R.id.primary_action_view).setClickable(false);
            }
            
            bindDetailView(position, v, entry);
            return v;
        }

        private void bindDetailView(int position, View view, DetailViewEntry entry) {
            final Resources resources = mContext.getResources();
            final DetailViewCache views = (DetailViewCache) view.getTag();

            if (!TextUtils.isEmpty(entry.typeString)) {
                views.type.setText(entry.typeString);
                views.type.setVisibility(View.VISIBLE);
            } else {
                views.type.setVisibility(View.GONE);
            }
            
            if (StructuredPostal.CONTENT_ITEM_TYPE.equals(entry.mimetype)) {
            	Drawable left = getResources().getDrawable(
            			ResConstant.getIconRes(IconTpye.Location));
            	left.setBounds(0, 0, 36, 36);
            	views.data.setCompoundDrawables(left, null, null, null);
            } else if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(entry.mimetype)) {
            	String number = entry.data.replaceAll(" ", "");
            	number = number.replaceAll("-", "");
            	Log.d(TAG, "number = " + number + "  entry.data =  " + entry.data);
                if (ContactsApplication.sIsAuroraRejectSupport ) {
                	Drawable right = mContext.getResources().getDrawable(R.drawable.aurora_black_flag);
                	right.setBounds(0, 0, right.getMinimumWidth(), right.getMinimumHeight());
                	
                	if (ContactDetailActivity.mRejectedList.contains(number)) {
                		views.data.setCompoundDrawables(null, null, right, null);
                	} else {
                		String numberE164 = PhoneNumberUtils.formatNumberToE164(
				                number, ContactsUtils.getCurrentCountryIso(mContext));
                		if (numberE164 != null && !number.equals(numberE164)
                				&& ContactDetailActivity.mRejectedList.contains(numberE164)) {
                			views.data.setCompoundDrawables(null, null, right, null);
                		} else {
                			views.data.setCompoundDrawables(null, null, null, null);
                		}
                	}
                } else {
                	views.data.setCompoundDrawables(null, null, null, null);
                }
            } else {
            	views.data.setCompoundDrawables(null, null, null, null);
            }

            views.data.setText(entry.data);
            views.data.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
            setMaxLines(views.data, entry.maxLines);

            // Set the footer
            if (!TextUtils.isEmpty(entry.footerLine)) {
                views.footer.setText(entry.footerLine);
                views.footer.setVisibility(View.VISIBLE);
            } else {
                views.footer.setVisibility(View.GONE);
            }

            if (null != views.primaryIndicator) {
            	// Set the default contact method            
                views.primaryIndicator.setVisibility(entry.isPrimary ? View.VISIBLE : View.GONE);	
            }

            if (null != views.presenceIcon) {
            	// Set the presence icon
                final Drawable presenceIcon = ContactPresenceIconUtil.getPresenceIcon(
                        mContext, entry.presence);
                final ImageView presenceIconView = views.presenceIcon;
                if (presenceIcon != null) {
                    presenceIconView.setImageDrawable(presenceIcon);
                    presenceIconView.setVisibility(View.VISIBLE);
                } else {
                    presenceIconView.setVisibility(View.GONE);
                }	
            }

            final ActionsViewContainer actionsButtonContainer =
                    (ActionsViewContainer) views.actionsViewContainer;
            actionsButtonContainer.setTag(entry);
            actionsButtonContainer.setPosition(position);
//            registerForContextMenu(actionsButtonContainer);

            // Set the secondary action button
            final ImageView secondaryActionView = views.secondaryActionButton;
            Drawable secondaryActionIcon = null;
            String secondaryActionDescription = null;
            if (entry.secondaryActionIcon != -1) {            	
                secondaryActionIcon = resources.getDrawable(entry.secondaryActionIcon);
                secondaryActionDescription = resources.getString(entry.secondaryActionDescription);
            } else if ((entry.chatCapability & Im.CAPABILITY_HAS_CAMERA) != 0) {
                secondaryActionIcon =
                        resources.getDrawable(R.drawable.sym_action_videochat_holo_light);
                secondaryActionDescription = resources.getString(R.string.video_chat);
            } else if ((entry.chatCapability & Im.CAPABILITY_HAS_VOICE) != 0) {
                secondaryActionIcon =
                        resources.getDrawable(R.drawable.sym_action_audiochat_holo_light);
                secondaryActionDescription = resources.getString(R.string.audio_chat);
            }

            final View secondaryActionViewContainer = views.secondaryActionViewContainer;
            if (entry.secondaryIntent != null && secondaryActionIcon != null) {
                secondaryActionView.setImageDrawable(secondaryActionIcon);
                secondaryActionView.setContentDescription(secondaryActionDescription);
                secondaryActionViewContainer.setTag(entry);
                secondaryActionViewContainer.setVisibility(View.VISIBLE);
            } else {
                secondaryActionViewContainer.setVisibility(View.GONE);
            }

            int visibility = (entry.secondaryIntent != null && secondaryActionIcon != null) ? View.VISIBLE : View.GONE;
            showNewAddWidget(views, entry, visibility);

            if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(entry.mimetype)) {
                String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAora(mContext, entry.data, true);
                if(area != null){
                	setArea(views, secondaryActionViewContainer, actionsButtonContainer, area);
                } else {
                    views.gnNumberArea.setVisibility(View.GONE);
                	final String number = entry.data;
                	new Thread(new Runnable() {
						public void run() {
			                final String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAoraLock(mContext, number, true);
			                Activity activity = getActivity();
			                if(activity != null){
			                	activity.runOnUiThread(new Runnable() {
									public void run() {
					                	setArea(views, secondaryActionViewContainer, actionsButtonContainer, area);
									}
								});
			                }
						}
					}).start();
                }
            } else {
            	views.gnNumberArea.setVisibility(View.GONE);
            }
        }
        
        private void setArea(DetailViewCache views, View secondaryActionViewContainer, ActionsViewContainer actionsButtonContainer, String area){
            if (TextUtils.isEmpty(area)) {
                views.gnNumberArea.setVisibility(View.GONE);
             //aurora add zhouxiaobing 20140227   start
                secondaryActionViewContainer.setPadding(
	                    secondaryActionViewContainer.getPaddingLeft(),
	                    0,
	                    secondaryActionViewContainer.getPaddingRight(),
	                    0);
                actionsButtonContainer.setPadding(0, 0, 0, 0);
            //aurora add zhouxiaobing 20140227   end     

            } else {
                /*if (null != views.type && views.type.getVisibility() == View.VISIBLE) {
                    area = "-" + area;
                }*/
                views.gnNumberArea.setText(area);
                views.gnNumberArea.setVisibility(View.VISIBLE);
              //aurora add zhouxiaobing 20140227   start     
                secondaryActionViewContainer.setPadding(
	                    secondaryActionViewContainer.getPaddingLeft(),
	                    0,//mViewEntryDimensions.getPaddingTop(),
	                    secondaryActionViewContainer.getPaddingRight(),
	                    0/*mViewEntryDimensions.getPaddingBottom()*/);
                int top=mContext.getResources().getDimensionPixelSize(R.dimen.aurora_detail_item_padding_top);
                int bottom=mContext.getResources().getDimensionPixelSize(R.dimen.aurora_detail_item_padding_bottom);
                actionsButtonContainer.setPadding(0, top, 0, bottom);
              //aurora add zhouxiaobing 20140227   end       
            }
        
        }

        private void setMaxLines(TextView textView, int maxLines) {
            /*if (maxLines == 1) {
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            } else {
                textView.setSingleLine(false);
                textView.setMaxLines(maxLines);
                textView.setEllipsize(null);
            }*/
        }

        private final OnClickListener mPrimaryActionClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener == null) return;
                final ViewEntry entry = (ViewEntry) view.getTag();
                if (entry == null) return;
                // aurora <wangth> <2014-3-8> modify for aurora begin
                if (!FeatureOption.MTK_GEMINI_SUPPORT
                        && entry instanceof DetailViewEntry && ((DetailViewEntry)entry).mimetype != null
                        && ((DetailViewEntry)entry).mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
                    if (!hasClicked) {
                        hasClicked = true;
                        entry.click(view, mListener);
                    }

                } else {
                    entry.click(view, mListener);
                }
                // aurora <wangth> <2014-3-8> modify for aurora end
            }
        };

        private final OnClickListener mSecondaryActionClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener == null) return;
                if (view == null) return;
                final ViewEntry entry = (ViewEntry) view.getTag();
                if (entry == null || !(entry instanceof DetailViewEntry)) return;
                final DetailViewEntry detailViewEntry = (DetailViewEntry) entry;
                final Intent intent = detailViewEntry.secondaryIntent;
                if (intent == null) return;
                mListener.onItemClicked(intent);
            }
        };

        
        /*
         * New Feature by Mediatek Begin.              
         * handle vtcall action           
         */
        private final OnClickListener mVtCallActionClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener == null) return;

                final DetailViewEntry entry = (DetailViewEntry) view.getTag();
                if (entry != null) {
                    final Intent intent;
                    // Gionee:wangth 20130301 modify for CR00771431 begin
                    /*
                    if (ContactsApplication.sIsGnContactsSupport) {
                    	intent = IntentFactory.newDialNumberIntent(entry.data);
                    } else {
                    	intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel", entry.data, null));
                    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    
                    intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
                    intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) entry.simId);
                    */
                    if (GNContactsUtils.isOnlyQcContactsSupport()) {
                        intent = GNContactsUtils.startQcVideoCallIntent(entry.data);
                    } else {
                        if (ContactsApplication.sIsGnContactsSupport) {
                            intent = IntentFactory.newDialNumberIntent(entry.data);
                        } else {
                            intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel", entry.data, null));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        
                        intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
                        intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) entry.simId);
                    }
                    // Gionee:wangth 20130301 modify for CR00771431 end

                    mListener.onItemClicked(intent);
                }
            }
        };
        /*
         * New Feature  by Mediatek End.
        */

        @Override
        public int getCount() {
            return mAllEntries.size();
        }

        @Override
        public ViewEntry getItem(int position) {
            return mAllEntries.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return mAllEntries.get(position).getViewType();
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public long getItemId(int position) {
            final ViewEntry entry = mAllEntries.get(position);
            if (entry != null) {
                return entry.getId();
            }
            return -1;
        }

        @Override
        public boolean areAllItemsEnabled() {
            // Header will always be an item that is not enabled.
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }
    }

    @Override
    public void onAccountSelectorCancelled() {
    }

    @Override
    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        createCopy(account);
    }

    private void createCopy(AccountWithDataSet account) {
        if (mListener != null) {
            mListener.onCreateRawContactRequested(mContactData.getContentValues(), account);
        }
    }

    /**
     * Default (fallback) list item click listener.  Note the click event for DetailViewEntry is
     * caught by individual views in the list item view to distinguish the primary action and the
     * secondary action, so this method won't be invoked for that.  (The listener is set in the
     * bindview in the adapter)
     * This listener is used for other kind of entries.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener == null) return;
        final ViewEntry entry = mAdapter.getItem(position);
        if (entry == null) return;
        entry.click(view, mListener);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, view, menuInfo);
//
//        AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
//        DetailViewEntry selectedEntry = (DetailViewEntry) mAllEntries.get(info.position);
//        String selectedMimeType = selectedEntry.mimetype;
//
//        menu.setHeaderTitle(selectedEntry.data);
//        
//        menu.add(ContextMenu.NONE, ContextMenuIds.COPY_TEXT,
//                ContextMenu.NONE, getString(R.string.copy_text));
//
//        // Defaults to true will only enable the detail to be copied to the clipboard.
//        boolean isUniqueMimeType = true;
//
//        // Only allow primary support for Phone and Email content types
//        if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
//            isUniqueMimeType = mIsUniqueNumber;
//        } else if (Email.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
//            isUniqueMimeType = mIsUniqueEmail;
//        }
//        
//        if (ContactsApplication.sIsGnContactsSupport &&
//        		Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
//        	menu.add(ContextMenu.NONE, ContextMenuIds.EDIT_NUMBER_BEFORE_CALL,
//                    ContextMenu.NONE, getString(R.string.gn_edit_number_before_call));
//        }
//
//        // Checking for previously set default
//        // Gionee:wangth 20120505 remove for CR00561840 begin
//        /*
//        if (selectedEntry.isPrimary) {
//            menu.add(ContextMenu.NONE, ContextMenuIds.CLEAR_DEFAULT,
//                    ContextMenu.NONE, getString(R.string.clear_default));
//        } else if (!isUniqueMimeType) {
//            menu.add(ContextMenu.NONE, ContextMenuIds.SET_DEFAULT,
//                    ContextMenu.NONE, getString(R.string.set_default));
//        }
//        */
//        // Gionee:wangth 20120505 remove for CR00561840 end
//        
//        /*
//         * New Feature by Mediatek Begin.            
//         * create new association menu and del association menu        
//         */
//        /*
//         * Bug Fix by Mediatek Begin.
//         *   Original Android's code:
//         *     if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {  
//         *   CR ID: ALPS00116397
//         */
//        if (FeatureOption.MTK_GEMINI_SUPPORT && Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType) && !isMe()) { 
//        /*
//         * Bug Fix by Mediatek End.
//         */ 
//        	if (!this.mContactData.isDirectoryEntry()) {
//        	   // Gionee lihuafang 220120327 remove for CR00555703 begin
//                /*
//                if (selectedEntry.simId > -1) {
//                    menu.add(ContextMenu.NONE, ContextMenuIds.DEL_ASSOCIATION_SIM,
//                             ContextMenu.NONE, getString(R.string.menu_remove_association));
//                } else {                
//                    MenuItem item = menu.add(ContextMenu.NONE, ContextMenuIds.NEW_ASSOCIATION_SIM,
//                                             ContextMenu.NONE, getString(R.string.menu_association));
//                    item.setEnabled(ContactDetailActivity.getInsertedSimCardInfoList(this.mContext, false).size() > 0);
//                }
//                */
//        	   // Gionee lihuafang 220120327 remove for CR00555703 end
//        	}
//        }
//        /*
//         * New Feature  by Mediatek End.
//        */
//    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        try {
            menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case ContextMenuIds.COPY_TEXT:
                copyToClipboard(menuInfo.position);
                return true;
            case ContextMenuIds.SET_DEFAULT:
                setDefaultContactMethod(mListView.getItemIdAtPosition(menuInfo.position));
                return true;
            case ContextMenuIds.CLEAR_DEFAULT:
                clearDefaultContactMethod(mListView.getItemIdAtPosition(menuInfo.position));
                return true;
            
            /*
             * New Feature by Mediatek Begin.            
             * handle new and del association menu click event        
             */
            case ContextMenuIds.NEW_ASSOCIATION_SIM:                
                handleNewAssociationSimMenu((DetailViewEntry) mAllEntries.get(menuInfo.position));
                return true;
                
            case ContextMenuIds.DEL_ASSOCIATION_SIM:
                handleDelAssociationSimMenu((DetailViewEntry) mAllEntries.get(menuInfo.position));                
                return true;  
            /*
             * New Feature  by Mediatek End.
             */
            case ContextMenuIds.EDIT_NUMBER_BEFORE_CALL:
            	startActivity(ContactsUtils.getEditNumberBeforeCallIntent(((DetailViewEntry) mAllEntries.get(menuInfo.position)).data));
            	return true;
                
            default:
                throw new IllegalArgumentException("Unknown menu option " + item.getItemId());
        }
    }
    
    public void handleAssociationSimOptionMenu() {
        DetailViewEntry detail = getFirstDetailViewEntry();
        if (detail != null) {
            handleNewAssociationSimMenu(detail);
        }  
    }
    
    public DetailViewEntry getFirstDetailViewEntry() {       
        for (ViewEntry viewEntry: mAllEntries) {
            if (viewEntry instanceof DetailViewEntry) {
                DetailViewEntry detail = (DetailViewEntry) viewEntry;            
                /*
                 * Bug Fix by Mediatek Begin.
                 *   Original code:
                 *     return detail; 
                 *   CR ID: ALPS000113770                Â­
                 */
                if (detail.mimetype != null && detail.mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
                return detail;  
            }
                /*
                 * Bug Fix by Mediatek End.
                 */
            }
        }
        return null;
    }
    
    private void setDefaultContactMethod(long id) {
        Intent setIntent = ContactSaveService.createSetSuperPrimaryIntent(mContext, id);
        mContext.startService(setIntent);
    }

    private void clearDefaultContactMethod(long id) {
        Intent clearIntent = ContactSaveService.createClearPrimaryIntent(mContext, id);
        mContext.startService(clearIntent);
    }

    private void copyToClipboard(int viewEntryPosition) {
        // Getting the text to copied
        DetailViewEntry detailViewEntry = (DetailViewEntry) mAllEntries.get(viewEntryPosition);
        CharSequence textToCopy = detailViewEntry.data;

        // Checking for empty string
        if (TextUtils.isEmpty(textToCopy)) return;

        // Adding item to clipboard
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);
        String[] mimeTypes = new String[]{detailViewEntry.mimetype};
        ClipData.Item clipDataItem = new ClipData.Item(textToCopy);
        ClipData cd = new ClipData(detailViewEntry.typeString, mimeTypes, clipDataItem);
        clipboardManager.setPrimaryClip(cd);

        // Display Confirmation Toast
        String toastText = getString(R.string.toast_text_copied);
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                try {
                    ITelephony phone = ITelephony.Stub.asInterface(
                            ServiceManager.checkService("phone"));
                    if (phone != null && !phone.isIdle()) {
                        // Skip out and let the key be handled at a higher level
                        break;
                    }
                } catch (RemoteException re) {
                    // Fall through and try to call the contact
                }

                int index = mListView.getSelectedItemPosition();
                if (index != -1) {
                	//Gionee:huangzy 20130306 modify for CR00778663 start
                	ViewEntry obj = mAdapter.getItem(index);
                	if (null != obj)
	                	if (obj instanceof DetailViewEntry) {
	                		final DetailViewEntry entry = (DetailViewEntry) obj;
	                        if (entry != null && entry.intent != null &&
	                            entry.intent.getAction() == Intent.ACTION_CALL_PRIVILEGED) {
	                        	mContext.startActivity(entry.intent);
	                            return true;
	                        }
                	}
                	//Gionee:huangzy 20130306 modify for CR00778663 end                	
                } else if (mPrimaryPhoneUri != null) {
                    // There isn't anything selected, call the default number
                    final Intent intent;
                    if (ContactsApplication.sIsGnContactsSupport) {
                    	intent = IntentFactory.newDialNumberIntent(mPrimaryPhoneUri);
                    	//aurora add liguangyu 20131206 start
                    	intent.putExtra("contactUri", getUri());
    	            	//aurora add liguangyu 20131206 end
                    } else {
                    	intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, mPrimaryPhoneUri);
                    }
                    
                    mContext.startActivity(intent);
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * This class loads the correct padding values for a contact detail item so they can be applied
     * dynamically. For example, this supports the case where some detail items can be indented and
     * need extra padding.
     */
    private static class ViewEntryDimensions {

        private final int mWidePaddingLeft;
        private final int mPaddingLeft;
        private final int mPaddingRight;
        private final int mPaddingTop;
        private final int mPaddingBottom;

        public ViewEntryDimensions(Resources resources) {
            mPaddingLeft = resources.getDimensionPixelSize(
                    R.dimen.detail_item_side_margin);
            mPaddingTop = resources.getDimensionPixelSize(
                    R.dimen.detail_item_vertical_margin);
            mWidePaddingLeft = mPaddingLeft +
                    resources.getDimensionPixelSize(R.dimen.detail_item_icon_margin) +
                    resources.getDimensionPixelSize(R.dimen.detail_network_icon_size);
            mPaddingRight = mPaddingLeft;
            mPaddingBottom = mPaddingTop;
        }

        public int getWidePaddingLeft() {
            return mWidePaddingLeft;
        }

        public int getPaddingLeft() {
            return mPaddingLeft;
        }

        public int getPaddingRight() {
            return mPaddingRight;
        }

        public int getPaddingTop() {
            return mPaddingTop;
        }

        public int getPaddingBottom() {
            return mPaddingBottom;
        }
    }

    public static interface Listener {
        /**
         * User clicked a single item (e.g. mail). The intent passed in could be null.
         */
        public void onItemClicked(Intent intent);

        /**
         * User requested creation of a new contact with the specified values.
         *
         * @param values ContentValues containing data rows for the new contact.
         * @param account Account where the new contact should be created.
         */
        public void onCreateRawContactRequested(ArrayList<ContentValues> values,
                AccountWithDataSet account);
    }

    /**
     * Adapter for the invitable account types; used for the invitable account type list popup.
     */
    private final static class InvitableAccountTypesAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ContactLoader.Result mContactData;
        private final ArrayList<AccountType> mAccountTypes;

        public InvitableAccountTypesAdapter(Context context, ContactLoader.Result contactData) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mContactData = contactData;
            final List<AccountType> types = contactData.getInvitableAccountTypes();
            mAccountTypes = new ArrayList<AccountType>(types.size());

            AccountTypeManager manager = AccountTypeManager.getInstance(context);
            for (int i = 0; i < types.size(); i++) {
                mAccountTypes.add(types.get(i));
            }

            Collections.sort(mAccountTypes, new AccountType.DisplayLabelComparator(mContext));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View resultView =
                    (convertView != null) ? convertView
                    : mInflater.inflate(R.layout.account_selector_list_item, parent, false);

            final TextView text1 = (TextView)resultView.findViewById(android.R.id.text1);
            final TextView text2 = (TextView)resultView.findViewById(android.R.id.text2);
            final ImageView icon = (ImageView)resultView.findViewById(android.R.id.icon);

            final AccountType accountType = mAccountTypes.get(position);

            CharSequence action = accountType.getInviteContactActionLabel(mContext);
            CharSequence label = accountType.getDisplayLabel(mContext);
            if (TextUtils.isEmpty(action)) {
                text1.setText(label);
                text2.setVisibility(View.GONE);
            } else {
                text1.setText(action);
                text2.setVisibility(View.VISIBLE);
                text2.setText(label);
            }
            icon.setImageDrawable(accountType.getDisplayIcon(mContext));

            return resultView;
        }

        @Override
        public int getCount() {
            return mAccountTypes.size();
        }

        @Override
        public AccountType getItem(int position) {
            return mAccountTypes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
    
    /*
     * New Feature by Mediatek Begin.              
     * show newly view for association and vtcall                          
     */
    public void showNewAddWidget(DetailViewCache views, DetailViewEntry entry, int visibility) {
                    
        final int simId = entry.simId;
        int associVisible = View.GONE;
        if (visibility == View.VISIBLE) {                     
            if (simId > -1) {
                associVisible = View.VISIBLE;
                if (null != views.imgAssociationSimIcon) {
                	views.imgAssociationSimIcon.setImageDrawable(mContext.getResources().
                			getDrawable(R.drawable.ic_association));	
                }
                if (null != views.txtAssociationSimName) {
	                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	                    views.txtAssociationSimName.setMaxWidth(150);
	                } else {
	                    views.txtAssociationSimName.setMaxWidth(300);
	                }
	                
	                SIMInfo simInfo = SIMInfo.getSIMInfoById(mContext, simId);
	                if (simInfo != null) {
	                    Log.i(TAG, "[showNewAddWidget]: simInfo.mDisplayName is " + simInfo.mDisplayName);
	                    Log.i(TAG, "[showNewAddWidget]: simInfo.mColor is " + simInfo.mColor);
	                   	views.txtAssociationSimName.setText(simInfo.mDisplayName);
	                    int slotId = SIMInfo.getSlotById(mContext, simId);
	                    Log.d(TAG, "slotId = " + slotId);
	                    if (slotId >= 0) {
	                        Log.d(TAG, "[showNewAddWidget]: slotId >= 0 ");
	                        views.txtAssociationSimName.setBackgroundResource(GnTelephony.SIMBackgroundRes[simInfo.mColor]);
	                    } else {
	                        Log.d(TAG, "[showNewAddWidget]: slotId < 0 ");
	                        views.txtAssociationSimName.setBackgroundResource(R.drawable.sim_background_locked);
	                    }
	                    int padding = this.getResources().getDimensionPixelOffset(R.dimen.association_sim_leftright_padding);
	                    views.txtAssociationSimName.setPadding(padding, 0, padding, 0);
	                } else {
	                    Log.i(TAG, "[showNewAddWidget]: not find siminfo");
	                }
                }
            }
            // gionee xuhz 20120506 modify for CR00588990 start
            if (ContactsApplication.sIsGnDarkStyle) {
                views.btnVtCallAction.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_video_call_dark));
            } else {
                views.btnVtCallAction.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_video_call));
            }
            // gionee xuhz 20120506 modify for CR00588990 end
            views.vtcallActionViewContainer.setTag(entry);
            //views.btnVtCallAction.setTag(entry);
        }
        if (null != views.imgAssociationSimIcon) {
        	views.imgAssociationSimIcon.setVisibility(associVisible);
        }
        if (null != views.txtAssociationSimName) {
        	views.txtAssociationSimName.setVisibility(associVisible);	
        }
         
        if (FeatureOption.MTK_VT3G324M_SUPPORT) {
            views.btnVtCallAction.setVisibility(visibility);         
            views.vtcallActionViewContainer.setVisibility(visibility);
        } else {
            views.btnVtCallAction.setVisibility(View.GONE);
            views.vtcallActionViewContainer.setVisibility(View.GONE);
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * get if has phone number item        
     */
    public boolean hasPhoneEntry(ContactLoader.Result contactData) {        
        if (contactData == null) {
            Log.d(TAG, "[hasPhoneEntry]: contactData = null");
            return false;
        }       
     
        NamedContentValues subValue = getFirstNamedContentValues(contactData, Phone.CONTENT_ITEM_TYPE);
        if (subValue != null) {
            final String phoneNumber = subValue.values.getAsString(Data.DATA1);
            return !TextUtils.isEmpty(phoneNumber);
        }

        return false;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * get first NamedContentValues's object from contactData using the mimeType       
     */
    public NamedContentValues getFirstNamedContentValues(ContactLoader.Result contactData, final String mimeType) {        
        if (contactData == null) {
            Log.d(TAG, "[getFirstNamedContentValues]: contactData = null");
            return null;
        }
       
        for (Entity entity: contactData.getEntities()) {          
            for (NamedContentValues subValue : entity.getSubValues()) {                
                if (mimeType.equals(subValue.values.getAsString(Data.MIMETYPE))) {
                    return subValue;                    
                }
            }
        }
        
        return null;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * handle new association menu click event        
     */
    public void handleNewAssociationSimMenu(DetailViewEntry detailViewEntry) {
        if(detailViewEntry == null) {
            Log.d(TAG, "[handleNewAssociationSimMenu]: detailViewEntry = null");
            return;
        }
        tempDetailViewEntry = detailViewEntry;
        
        if (this.mContactData.getIndicate() > -1) {//RawContacts.INDICATE_SIM)       
            new AuroraAlertDialog.Builder(this.mContext)
                    .setTitle(this.mContactData.getDisplayName())
//                    .setIcon(android.R.drawable.ic_menu_more)
                    .setMessage(R.string.warning_detail)
                    .setPositiveButton(android.R.string.ok, mOnNewAssociationSimListener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .create()
                    .show();
        } else {
            startAssociationActivity(this.mContactData.getDisplayName(),  
                                     ContactDetailDisplayUtils.getCompany(this.mContext, this.mContactData),
                                     this.mContactData.getNameRawContactId(),
                                     tempDetailViewEntry.id,
                                     this.mContactData.getLookupUri(),
                                     getNumberContentValuesFromDataTable(mShowingPhoneEntries));                                                      
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * handle del association menu click event        
     */
    public void handleDelAssociationSimMenu(DetailViewEntry detailViewEntry) {
        if (detailViewEntry == null) {
            Log.d(TAG, "[handleDelAssociationSimMenu]: detailViewEntry = null");
            return;
        }
        tempDetailViewEntry = detailViewEntry;
        
        new AuroraAlertDialog.Builder(this.mContext)
                .setTitle(R.string.remove_number_title)
                .setIcon(android.R.drawable.ic_menu_more)
                .setMessage(R.string.remove_association_message)
                .setPositiveButton(R.string.remove_number_title, mOnDelSimAssociationListener)                           
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create()
                .show();        
    }
    /*
     * New Feature  by Mediatek End.
    */
    
 
    /*
     * New Feature by Mediatek Begin.            
     * handle dialog's ok button event        
     */
    private DialogInterface.OnClickListener mOnDelSimAssociationListener = new DialogInterface.OnClickListener() {
     
        public void onClick(DialogInterface dialog, int which) {
            if (tempDetailViewEntry == null) {
                Log.d(TAG, "[mDelSimAssociationListener.onClick]: tempDetailViewEntry = null");
                return;               
            }                        
            
            ContentValues values = new ContentValues();
            values.put(Data.SIM_ASSOCIATION_ID, -1);
            mHandler.startUpdate(0, null, Data.CONTENT_URI, values, Data._ID + "=? ", new String[] { String.valueOf(tempDetailViewEntry.id) });
        }        
    };
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * handle dialog's ok button event        
     */
    private DialogInterface.OnClickListener mOnNewAssociationSimListener = new DialogInterface.OnClickListener() {
        
        public void onClick(DialogInterface dialog, int which) {
            if (tempDetailViewEntry == null) {
                Log.d(TAG, "[mNewAssociationSimListener.onClick]: tempDetailViewEntry = null");
                return;               
            }              
            
            if (isUSimType(mContactData.getSlot())) {
                Log.d(TAG, "[mNewAssociationSimListener.onClick]: is USIM card");
                // importOneUSimContact(name, type, number, email, additional_number, grpIdSet);                
                
                String email = null;                
                List<NamedContentValues> values =  mContactData.getAllNamedContentValues(Email.CONTENT_ITEM_TYPE);
                if (values.size() > 1) {
                    email = values.get(0).values.getAsString(Data.DATA1);
                }            
            
                String additional_number = null;
                values =  mContactData.getAllNamedContentValues(Phone.CONTENT_ITEM_TYPE);
                if (values.size() > 1) {
                    for (NamedContentValues phoneInfo: values) {
                        if (tempDetailViewEntry.getId() != phoneInfo.values.getAsLong(Data._ID)) {
                            additional_number = phoneInfo.values.getAsString(Data.DATA1);
                            break;
                        }
                    }
                }
                
                HashSet<Long> grpIdSet = null;
                values =  mContactData.getAllNamedContentValues(GroupMembership.CONTENT_ITEM_TYPE);
                if (values.size() > 1) {
                    grpIdSet = new HashSet<Long>();
                    for (NamedContentValues groupInfo: values) {                        
                        grpIdSet.add(Long.valueOf(groupInfo.values.getAsString(Data.DATA1)));                         
                    }
                }
                
                importOneUSimContact(mContactData.getDisplayName(), tempDetailViewEntry.type, tempDetailViewEntry.data, email, additional_number, grpIdSet);
            } else {
                Log.d(TAG, "[mNewAssociationSimListener.onClick]: is USIM card");
                importOneSimContact(mContactData.getDisplayName(), tempDetailViewEntry.type, tempDetailViewEntry.data);
            }            
            
        }
    };
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * new add one phone contact from one USIM contacts        
     */
    private  void importOneUSimContact(String name, int phoneType, String phoneNumber,String email, String additional_number, Set<Long> grpAddIds) {

        ContentValues sEmptyContentValues = new ContentValues();
        
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        // add by mediatek
        ContentValues contactvalues = new ContentValues();
        contactvalues.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
        contactvalues.put(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, GnContactsContract.RawContacts.INDICATE_PHONE);
//        builder.withValues(sEmptyContentValues);
        builder.withValues(contactvalues);
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builder.withValue(Phone.TYPE, phoneType);
        if (!TextUtils.isEmpty(phoneNumber)) {
            builder.withValue(Phone.NUMBER, phoneNumber);
            builder.withValue(Data.IS_PRIMARY, 1);
        } else {
            builder.withValue(Phone.NUMBER, null);
            builder.withValue(Data.IS_PRIMARY, 1);
        }
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(StructuredName.DISPLAY_NAME, name);
        operationList.add(builder.build());
        
        if (!TextUtils.isEmpty(email)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
            builder.withValue(Email.DATA, email);
            operationList.add(builder.build());
        }               
        
        if (!TextUtils.isEmpty(additional_number)) {
            Log.i(TAG, "additionalNumber is " + additional_number);
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            builder.withValue(Phone.TYPE, phoneType);
            builder.withValue(Phone.NUMBER, additional_number);
           // builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
            operationList.add(builder.build());
        }  

        //USIM Group begin
        if (grpAddIds != null && grpAddIds.size()>0) {
            Long [] grpIdArray = grpAddIds.toArray(new Long[0]);
            for (Long grpId: grpIdArray) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
                operationList.add(builder.build());
            }
        }
        //USIM Group end
        
        try {
            startAssociationActivity(mContext.getContentResolver().applyBatch(GnContactsContract.AUTHORITY, operationList));  
        } catch (Exception e) {
            Log.e(TAG, "[importOneUSimContact]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * new add one phone contact from one SIM contacts        
     */
    private  void importOneSimContact(String name, int phoneType, String phoneNumber) {
//      NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
//              cursor.getString(NAME_COLUMN));
        ContentValues sEmptyContentValues = new ContentValues();
        
//      String emailAddresses = "";//cursor.getString(EMAILS_COLUMN);

        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        // add by mediatek
        ContentValues contactvalues = new ContentValues();
        contactvalues.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
        contactvalues.put(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, GnContactsContract.RawContacts.INDICATE_PHONE);
//        builder.withValues(sEmptyContentValues);
        builder.withValues(contactvalues);
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builder.withValue(Phone.TYPE, phoneType);
        if (!TextUtils.isEmpty(phoneNumber)) {
            builder.withValue(Phone.NUMBER, phoneNumber);
            builder.withValue(Data.IS_PRIMARY, 1);
        } else {
            builder.withValue(Phone.NUMBER, null);
            builder.withValue(Data.IS_PRIMARY, 1);
        }
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(StructuredName.DISPLAY_NAME, name);
        operationList.add(builder.build());

        try {
            startAssociationActivity(mContext.getContentResolver().applyBatch(GnContactsContract.AUTHORITY, operationList));  
        } catch (Exception e) {
            Log.e(TAG, "[importOneSimContact]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        } 
    }
    /*
     * New Feature  by Mediatek End.
    */
    
            
    /*
     * New Feature by Mediatek Begin.            
     * start association sim activity         
     */
    public void startAssociationActivity(ContentProviderResult[] r) {
            ContentProviderResult r1 = r[0];
            String raw_contact_id = r1.uri.getPath();
            raw_contact_id = raw_contact_id.substring(raw_contact_id.lastIndexOf("/")+1);
                                         
            ContentProviderResult r2 = r[1];           
            String data_id = r2.uri.getPath();
            data_id = data_id.substring(data_id.lastIndexOf("/")+1);
            
            startAssociationActivity(this.mContactData.getDisplayName(),  
                                 "",
                                     this.mContactData.getNameRawContactId(),
                                     Long.parseLong(data_id),
                                     this.mContactData.getLookupUri(),
                                     getNumberContentValuesFromDataTable(raw_contact_id));                 
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * get if usim type    
     */
    public boolean isUSimType(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel == null) {
            Log.d(TAG, "[isUSimType]: iTel = null");
            return false;
        }
        
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                // qc--mtk
                //return iTel.getIccCardTypeGemini(slot).equals("USIM");
                return AuroraCardTypeUtils.getIccCardTypeGemini(iTel, slot).equals("USIM");
            } else {
                // qc--mtk
                //return iTel.getIccCardType().equals("USIM");
                return AuroraCardTypeUtils.getIccCardType(iTel).equals("USIM");
            }
        } catch (Exception e) {
            Log.e(TAG, "[isUSimType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    
    
    
    /*
     * New Feature by Mediatek Begin.            
     * handle query return result        
     */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * start association sim activity        
     */
    public void startAssociationActivity(String displayTitle, String displaySubtitle, long rawContactId, long selDataId, 
                                         Uri lookupUri, List<NamedContentValues> numberInfoList) {
        
        AssociationSimActivity.sContactDetailInfo = new ContactDetailInfo(displayTitle, displaySubtitle, lookupUri, numberInfoList);
        AssociationSimActivity.sContactDetailInfo.mContactData = this.mContactData;
        
        Intent intent = new Intent(this.mContext, AssociationSimActivity.class);       
        intent.putExtra(AssociationSimActivity.INTENT_DATA_KEY_SEL_DATA_ID, selDataId);
        intent.putExtra(AssociationSimActivity.INTENT_DATA_KEY_SEL_SIM_ID, tempDetailViewEntry.simId);
        
        startActivity(intent);
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * get the rawContactId Contact's data from data table        
     */
    public List<NamedContentValues> getNumberContentValuesFromDataTable(String rawContactId) {
        //List<NamedContentValues> numberInfoList = new ArrayList<NamedContentValues>();
        
        Cursor cursor = this.mContext.getContentResolver().query(
                Data.CONTENT_URI,
                DATA_PROJECTION, 
                Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "= ?",  
                new String[] { rawContactId, Phone.CONTENT_ITEM_TYPE}, null);
    
        ArrayList<DetailViewEntry> phoneEntries = new ArrayList<DetailViewEntry>();
        if (cursor != null) {
        	if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					final DetailViewEntry entry = new DetailViewEntry();
					entry.id = cursor.getLong(DataQuery._ID);
					entry.data = cursor.getString(DataQuery.DATA1);
					entry.type = Integer.parseInt(cursor.getString(DataQuery.DATA2));
					entry.simId = (int) cursor.getLong(DataQuery.SIM_ASSOCIATION_ID);
					entry.mimetype = Phone.CONTENT_ITEM_TYPE;
					phoneEntries.add(entry);
				}
				//Collapser.collapseList(phoneEntries);//aurora change zhouxiaobing 20140304
        	}
            cursor.close();
        }
        
        return getNumberContentValuesFromDataTable(phoneEntries);
    }
    
    public List<NamedContentValues> getNumberContentValuesFromDataTable(ArrayList<DetailViewEntry> phoneEntries) {
        List<NamedContentValues> numberInfoList = new ArrayList<NamedContentValues>();                
    
        if (phoneEntries != null) {    
            for (DetailViewEntry detailEntry: phoneEntries) {
                ContentValues cv = new ContentValues();                
                cv.put(Data._ID, detailEntry.getId());
                cv.put(Data.DATA1, detailEntry.data);
                cv.put(Data.DATA2, String.valueOf(detailEntry.type));
                cv.put(Data.SIM_ASSOCIATION_ID, detailEntry.simId);
                NamedContentValues nv = new NamedContentValues(null, cv);
                numberInfoList.add(nv);
            }           
        }
        
        return numberInfoList;
    }
    
    final String[] DATA_PROJECTION = new String[] {
            Data._ID,
            Data.DATA1,
            Data.DATA2,
            Data.SIM_ASSOCIATION_ID,
    };
    private static class DataQuery {
        public final static int _ID = 0;        
        public final static int DATA1 = 1;
        public final static int DATA2 = 2;        
        public final static int SIM_ASSOCIATION_ID = 3;
    }
    
    /*
     * New Feature  by Mediatek End.
    */
    
    public static boolean isUnSync(DataKind kind, ContentValues values ){
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: xxx CR ID:
         * ALPS00246021 Descriptions:
         */
        if ((kind.mimeType == Im.CONTENT_ITEM_TYPE)
                || (kind.mimeType == Phone.CONTENT_ITEM_TYPE
                        && (GetType(values, Phone.TYPE) == Phone.TYPE_PAGER)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_CALLBACK)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_CAR)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_COMPANY_MAIN)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_MAIN)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_RADIO)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_TELEX)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_TTY_TDD)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_WORK_MOBILE)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_WORK_PAGER)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_MMS)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_ISDN) || (GetType(values,
                        Phone.TYPE) == Phone.TYPE_ASSISTANT))
                || (kind.mimeType == Email.CONTENT_ITEM_TYPE && (GetType(values, Email.TYPE) == Email.TYPE_MOBILE))||
                (kind.mimeType == Event.CONTENT_ITEM_TYPE)) {
            Log.d(TAG, "isUnSync(), return true ");
            return true;
        } else {
            return false;
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }
    private static int GetType(ContentValues values, String typeColumn){
        if (values.containsKey(typeColumn)) {
            return values.getAsInteger(typeColumn);
        }
        return -1;

    }
    
    private void getShowingPhoneEntries() {
    	mShowingPhoneEntries = new ArrayList<DetailViewEntry>();
    	for (DetailViewEntry entry: mPhoneEntries) {
    		mShowingPhoneEntries.add(entry);
    	}
    }

    public void onUpdateComplete(int token, Object cookie, int result) {
        if (token == 0) {
            Activity activity = this.getActivity();
            if (activity != null) {
                activity.sendBroadcast(new Intent("com.android.contacts.associate_changed"));
            }
        }
    }
    
    //Gionee:huangzy 20120717 add start
    class GnOnItemClick implements OnClickListener{
        private int mPosition;
        public GnOnItemClick(int position) {
            mPosition = position;
        }
        
        @Override
        public void onClick(View v) {
            onItemClick(mListView, v, mPosition, v.getId());
        }
    }
    //Gionee:huangzy 20120717 add end
    
    // Gionee:xuhz 20130328 add for CR00790874 start
    public String getDefaultPhoneNumber(ContactLoader.Result contactData) {        
        if (contactData == null) {
            Log.d(TAG, "[getDefaultPhoneNumber]: contactData = null");
            return null;
        }       
     
        NamedContentValues subValue = getFirstNamedContentValues(contactData, Phone.CONTENT_ITEM_TYPE);
        if (subValue != null) {
            final String phoneNumber = subValue.values.getAsString(Data.DATA1);
            if (!TextUtils.isEmpty(phoneNumber)) {
            	return phoneNumber;
            }
        }

        return null;
    }
    // Gionee:xuhz 20130328 add for CR00790874 end
	
	private AuroraAlertDialog createLongClickDialog(final DetailViewEntry entry) {
		ArrayList<String> itemList = new ArrayList<String>();
		
        String selectedMimeType = entry.mimetype;
        String titleText = entry.data;
        
        itemList.add(getResources().getString(R.string.copy_text));
		// Defaults to true will only enable the detail to be copied to the clipboard.
        boolean isUniqueMimeType = true;

        // Only allow primary support for Phone and Email content types
        if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
            isUniqueMimeType = mIsUniqueNumber;
        } else if (Email.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
            isUniqueMimeType = mIsUniqueEmail;
        }
        
        if (ContactsApplication.sIsGnContactsSupport &&
        		Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
        	itemList.add(getResources().getString(R.string.gn_edit_number_before_call));
        }
        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);
        
		return new AuroraAlertDialog.Builder(this.mContext)
        .setTitle(titleText)
        .setItems(items,new DialogInterface.OnClickListener() {//aurora change zhouxiaobing 20140227
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which) {
		            case 0:
		            	gnCopyToClipboard(entry);
		                break;
		            case 1:
		            	startActivity(ContactsUtils.getEditNumberBeforeCallIntent(entry.data));
		                break;
		            default:
		                break;
		        }
			}
		})
		.setTitleDividerVisible(true)
        .setCancelIcon(true)
        .create();
	}
	
	// aurora ukiliu 2013-10-14 add for aurora ui begin
	private AuroraAlertDialog createLongClickDialog(final OrganizationViewEntry entry) {
		ArrayList<String> itemList = new ArrayList<String>();
		
        String selectedMimeType = entry.mimetype;
        String titleText = entry.data;
        itemList.add(getResources().getString(R.string.copy_text));
		// Defaults to true will only enable the detail to be copied to the clipboard.
        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);
        
		return new AuroraAlertDialog.Builder(this.mContext)
        .setTitle(titleText)
        .setItems(items, new DialogInterface.OnClickListener() {//aurora change zhouxiaobing 20140227
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which) {
		            case 0:
		            	gnCopyToClipboard(entry);
		                break;
		            default:
		                break;
		        }
			}
		})
		.setTitleDividerVisible(true)
        .setCancelIcon(true)
        .create();
	}
	
	private void gnCopyToClipboard(OrganizationViewEntry entry) {
        // Getting the text to copied
		OrganizationViewEntry detailViewEntry = entry;
        CharSequence textToCopy = detailViewEntry.data;

        // Checking for empty string
        if (TextUtils.isEmpty(textToCopy)) return;

        // Adding item to clipboard
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);
        String[] mimeTypes = new String[]{detailViewEntry.mimetype};
        ClipData.Item clipDataItem = new ClipData.Item(textToCopy);
        ClipData cd = new ClipData(detailViewEntry.typeString, mimeTypes, clipDataItem);
        clipboardManager.setPrimaryClip(cd);

        // Display Confirmation Toast
        String toastText = getString(R.string.toast_text_copied);
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }
	// aurora ukiliu 2013-10-14 add for aurora ui end
	
    private void gnCopyToClipboard(DetailViewEntry entry) {
        // Getting the text to copied
        DetailViewEntry detailViewEntry = entry;
        CharSequence textToCopy = detailViewEntry.data;

        // Checking for empty string
        if (TextUtils.isEmpty(textToCopy)) return;

        // Adding item to clipboard
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);
        String[] mimeTypes = new String[]{detailViewEntry.mimetype};
        ClipData.Item clipDataItem = new ClipData.Item(textToCopy);
        ClipData cd = new ClipData(detailViewEntry.typeString, mimeTypes, clipDataItem);
        clipboardManager.setPrimaryClip(cd);

        // Display Confirmation Toast
        String toastText = getString(R.string.toast_text_copied);
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }
    
    // aurora <ukiliu> <2013-9-25> add for auroro ui begin
    private View getHeaderEntryView(View convertView, ViewGroup parent, int high) {
        View result = null;
        HeaderViewCache viewCache = null;

        // Otherwise inflate a new header view and create a new view cache.
        if (result == null) {
            viewCache = new HeaderViewCache(convertView);
        	// aurora yudingmin 2015-01-12 modified for bug #10724 start
            viewCache.SpringView.setMaxHeight(spring_4_height);
        	// aurora yudingmin 2015-01-12 modified for bug #10724 start
            convertView.setTag(viewCache);
            result = convertView;
        }
        Log.v(TAG, "name="+mContactData.getDisplayName()); 
        ContactDetailDisplayUtils.setDisplayName(mContext, mContactData,
                viewCache.displayNameView);
		ContactDetailDisplayUtils.setNoteName(mContext, mContactData, viewCache.noteView);

        // Set the photo if it should be displayed
        if (viewCache.photoView != null) {
        	int indicatePhoneSim = mContactData.getIndicate();
        	if (FeatureOption.MTK_GEMINI_SUPPORT && indicatePhoneSim > 0) {
        	    int iconId = ContactsUtils.getSimBigIcon(mContext, indicatePhoneSim);
        	    mStaticPhotoView.setBackgroundResource(iconId);
        	} else {
        	    ContactDetailDisplayUtils.setPhotoForDetail(mContext, mContactData, mStaticPhotoView, mIsPrivacyContact);
        	}
        }
        viewCache.photocontainer.setVisibility(View.VISIBLE); 
        LayoutParams linearParams = (LayoutParams) viewCache.SpringView.getLayoutParams();

    	// aurora yudingmin 2015-01-12 modified for bug #10724 start
        if (high <= 4) {
        	linearParams.height = spring_4_height;
        } else if (high == 5) {
        	linearParams.height = spring_5_height;
        } else {
        	linearParams.height = 0;
        }
    	// aurora yudingmin 2015-01-12 modified for bug #10724 start
        viewCache.SpringView.setLayoutParams(linearParams);
        viewCache.SpringView.invalidate();

        // Set the starred state if it should be displayed
        // The favorite star shown or not for tablet should be decided here for ALPS00242811
        if (mContactData.getIndicate() < 0 && !mIsPrivacyContact) {
        final CheckBox favoritesStar = viewCache.starredView;
        if (favoritesStar != null) {
            ContactDetailDisplayUtils.setStarred(mContactData, favoritesStar);
            final Uri lookupUri = mContactData.getLookupUri();
            favoritesStar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toggle "starred" state
                    // Make sure there is a contact
                    if (lookupUri != null) {
                        Intent intent = ContactSaveService.createSetStarredIntent(
                                getContext(), lookupUri, favoritesStar.isChecked());
                        getContext().startService(intent);
                    }
                    String favoritesStarText;
                    if (favoritesStar.isChecked()) {
                    	favoritesStarText = mContext.getResources().getString(R.string.aurora_add_star);
                    } else {
                    	favoritesStarText = mContext.getResources().getString(R.string.aurora_remove_star);
                    }
                    Toast.makeText(mContext, favoritesStarText, Toast.LENGTH_SHORT).show();
                }
            });
        }
        }
        
        if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
        	convertView.setBackgroundResource(R.color.aurora_privacy_contact_detail_header_color);
        } else {
        	convertView.setBackgroundResource(R.color.detail_header_color);
        }

        return result;
    }
    // aurora <ukiliu> <2013-9-25> add for auroro ui end

    // aurora <wangth> <2014-1-4> add for aurora begin
    private AuroraAlertDialog mPhotoActionDialog;
    private PhotoEditorListener photoListener;
    private File mCurrentPhotoFile;
    private int mPhotoPickSize;
    private Bitmap mPhoto = null;
    
    private static final int REQUEST_CODE_CAMERA_WITH_DATA = 1;
    private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 2;
    private static final int REQUEST_CODE_PHOTO_CROP = 3;
    
    private void loadPhotoPickSize() {
        final Context con = mContext;
        new Thread(){
            @Override
            public void run() {
                Cursor c = con.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
                        new String[]{DisplayPhoto.DISPLAY_MAX_DIM}, null, null, null);
                if (null != c && c.moveToFirst()) {
                    try {
                        mPhotoPickSize = c.getInt(0);
                        Log.e(TAG, "mPhotoPickSize = " + mPhotoPickSize);
                    } catch (Exception e) {
                        e.printStackTrace();
//                    } finally {
//                        c.close();
                    }
                }
                if(c != null) {
                	c.close();
                }
            }
        }.start();
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.photo: {
            long photoId = mContactData.getPhotoId();
            if (null == getActivity() || (null != getActivity() && getActivity().isFinishing())) {
                return;
            }
            
            if (0 == photoId) { // add new photo
                mPhotoActionDialog = PhotoActionPopup.createDialogMenu(mContext, photoListener, PhotoActionPopup.MODE_NO_PHOTO);
                if (null != getActivity() && !getActivity().isFinishing()) {
                	try {
                		mPhotoActionDialog.show();
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                }
            } else { // update photo
                mPhotoActionDialog = PhotoActionPopup.createDialogMenu(mContext, photoListener, PhotoActionPopup.MODE_PHOTO_DISALLOW_PRIMARY);
                if (null != getActivity() && !getActivity().isFinishing()) {
                	try {
                		mPhotoActionDialog.show();
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                }
            }
            
            break;
        }
        }
    }
    
    private final class PhotoEditorListener implements PhotoActionPopup.Listener {

        @Override
        public void onUseAsPrimaryChosen() {
            
        }

        @Override
        public void onRemovePictureChosen() {
            if (mContactData.getPhotoId() == 0) {
                return;
            }
            
            String accountType = null;
            for (Entity entity: mContactData.getEntities()) {
                final ContentValues entValues = entity.getEntityValues();
                accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
                if (accountType != null) {
                    break;
                }
                
                Log.e(TAG, "accountType = " + accountType);
            }
            
            long rawContactsId = ContactsUtils.queryForRawContactId(mContext.getContentResolver(), mContactData.getContactId());
            if(accountType != null && accountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)){
                mContext.getContentResolver().delete(Data.CONTENT_URI,  Data.MIMETYPE + " = '" + CommonDataKinds.Photo.CONTENT_ITEM_TYPE 
                        + "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactsId + " AND is_privacy > -1", null);
            } else {
                ContentValues cv = new ContentValues();
                cv.put(CommonDataKinds.Photo.PHOTO, (byte[])null);
                
                mContext.getContentResolver().update(Data.CONTENT_URI, cv, 
                        Data.MIMETYPE + " = '" + CommonDataKinds.Photo.CONTENT_ITEM_TYPE 
                        + "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactsId + " AND is_privacy > -1",
                        null);
            }
            
            
              ContactDetailDisplayUtils.setPhotoForDetail(mContext, mContactData, mStaticPhotoView, mIsPrivacyContact);
              ContactsApplication.getInstance().sendSyncBroad();
        }

        @Override
        public void onTakePhotoChosen() {
            try {
                // Launch camera to take photo for selected contact
                String availableDir = null;
                File photoDir = (null != availableDir ? new File(availableDir + "/DCIM/Camera") : AuroraContactEditorFragment.PHOTO_DIR);
                photoDir.mkdirs();
                mCurrentPhotoFile = new File(photoDir, AuroraContactEditorFragment.getPhotoFileName());
                
                final Intent intent = AuroraContactEditorFragment.getTakePickIntent(mCurrentPhotoFile);
                if(isAdded()) {
                	startActivityForResult(intent, REQUEST_CODE_CAMERA_WITH_DATA);
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPickFromGalleryChosen() {
            try {
                // Launch picker to choose photo for selected contact
                final Intent intent = getPhotoPickIntent();
                if(isAdded()) {
                	startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CODE_CAMERA_WITH_DATA: {
            if (resultCode != Activity.RESULT_OK)
                return;
            
            doCropPhoto(mCurrentPhotoFile);
            break;
        }
        
        case REQUEST_CODE_PHOTO_PICKED_WITH_DATA: {
            if (resultCode != Activity.RESULT_OK)
                return;
            
            if (data != null) {
            	Uri uri = data.getData();
                Log.d(TAG, "uri = " + uri);
                if (uri != null) {
                	Intent intent = new Intent().setDataAndType(uri, "image/*");
                    intent.setAction("com.android.camera.action.GNCROP");
                    startActivityForResult(intent, REQUEST_CODE_PHOTO_CROP);
                }
            }
            
            break;
        }
        
        case REQUEST_CODE_PHOTO_CROP: {
        	if (resultCode != Activity.RESULT_OK || data == null)
                return;
        	
        	if (ContactsApplication.sIsGnZoomClipSupport) {
                String photoPath = data.getStringExtra("gn_data");
                if (null != photoPath) {
                    File photoFile = new File(photoPath);
                    try {
                        FileInputStream ffs = new FileInputStream(photoFile);
                        mPhoto = BitmapFactory.decodeStream(ffs);
                        ffs.close();
                        final int size = mPhoto.getWidth() * mPhoto.getHeight() * 4;
                        final ByteArrayOutputStream out = new ByteArrayOutputStream(size);
                        mPhoto.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        byte[] bs = out.toByteArray();
                        ContentValues cv = new ContentValues();
                        cv.put(CommonDataKinds.Photo.PHOTO, bs);
                        
                        String accountType = null;
                        for (Entity entity: mContactData.getEntities()) {
                            final ContentValues entValues = entity.getEntityValues();
                            accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
                            if (accountType != null) {
                                Log.e(TAG, "accountType = " + accountType);
                                break;
                            }
                        }
                        
                        long rawContactsId = ContactsUtils.queryForRawContactId(mContext.getContentResolver(), mContactData.getContactId());
                        if (mContactData.getPhotoId() != 0) { // update
                            mContext.getContentResolver().update(Data.CONTENT_URI, cv, 
                                    Data.MIMETYPE + " = '" + CommonDataKinds.Photo.CONTENT_ITEM_TYPE 
                                    + "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactsId,
                                    null);
                        } else if(accountType != null && accountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE)){  // only for Local Phone Type
                            Cursor c = mContext.getContentResolver().query(Data.CONTENT_URI, 
                                    new String[]{Data._ID}, 
                                    Data.MIMETYPE + " = '" + CommonDataKinds.Photo.CONTENT_ITEM_TYPE 
                                    + "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactsId + " AND is_privacy > -1", 
                                    null, null);
                            int dataId = -1;
                            if (c != null && c.moveToFirst()) {
                                dataId = c.getInt(0);
                            }
                            if(c != null) {
                            	c.close();
                            }
                            Log.e(TAG, "dataId = " + dataId);
                            
                            if (dataId == -1) {
                                cv.put(CommonDataKinds.Photo.IS_SUPER_PRIMARY, 1);
                                cv.put(CommonDataKinds.Photo.IS_PRIMARY, 1);
                                cv.put(Data.MIMETYPE, CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                                cv.put(Data.RAW_CONTACT_ID, rawContactsId);
                                
                                mContext.getContentResolver().insert(Data.CONTENT_URI, cv);
                            } else {
                                mContext.getContentResolver().update(Data.CONTENT_URI, cv, 
                                        Data._ID + "=" + dataId,
                                        null);
                            }
                        } else { // update for other account
                            mContext.getContentResolver().update(Data.CONTENT_URI, cv, 
                                    Data.MIMETYPE + " = '" + CommonDataKinds.Photo.CONTENT_ITEM_TYPE 
                                    + "' AND " + Data.RAW_CONTACT_ID + " = " + rawContactsId,
                                    null);
                        }
                        
                        photoFile.delete();
                        ContactsApplication.getInstance().sendSyncBroad();
                        break;
                    } catch (Exception e) {
                    	Log.e(TAG, "REQUEST_CODE_PHOTO_CROP e = "+e.toString());
                    }
                    return;
                }
            }
        	
        	break;
        }
        
        }
    }
    
    /**
     * Sends a newly acquired photo to Gallery for cropping
     */
    protected void doCropPhoto(File f) {
        try {
            // Add the image to the media store
            MediaScannerConnection.scanFile(
                    mContext,
                    new String[] { f.getAbsolutePath() },
                    new String[] { null },
                    null);

            // Launch gallery to crop the photo
            final Intent intent = getCropImageIntent(Uri.fromFile(f));
            startActivityForResult(intent, REQUEST_CODE_PHOTO_CROP);
        } catch (Exception e) {
            Log.e(TAG, "Cannot crop image", e);
            Toast.makeText(mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Constructs an intent for image cropping.
     */
    private Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mPhotoPickSize);
        intent.putExtra("outputY", mPhotoPickSize);
        // The following lines are provided and maintained by Mediatek inc.
        intent.putExtra(AuroraContactEditorFragment.KEY_SCALE_UP_IF_NEEDED, true);
        // The following lines are provided and maintained by Mediatek inc.
        intent.putExtra("return-data", true);
        
        if (ContactsApplication.sIsGnZoomClipSupport) {
            intent.setAction("com.android.camera.action.GNCROP");
            intent.putExtra("gn_crop", true);
        }
        
        return intent;
    }
    
    /**
     * Constructs an intent for picking a photo from Gallery, cropping it and returning the bitmap.
     */
    public Intent getPhotoPickIntent() {
    	Intent intent = new Intent("com.aurora.filemanager.SINGLE_GET_CONTENT");
        intent.setType("image/*");
        
        return intent;
    }
    
    private boolean canChangePhotoAccount() {
        boolean result = false;
        String accountType = null;
        
        if (mContactData == null) {
            return result;
        }
        
        for (Entity entity: mContactData.getEntities()) {
            final ContentValues entValues = entity.getEntityValues();
            accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
            if (accountType != null) {
                Log.e(TAG, "accountType = " + accountType);
                break;
            }
        }
        
        if (accountType != null &&  (accountType.equals("com.google") || 
                accountType.equals(AccountType.ACCOUNT_TYPE_LOCAL_PHONE))) {
            result = true;
        } else if (accountType == null) {
            result = true;
        }
        
        return result;
    }
    // aurora <wangth> <2014-1-4> add for aurora end
    
    private void loadRejectedList() {
    	Cursor c = mContext.getContentResolver().query(Uri.parse("content://com.android.contacts/black"),
                new String[]{"number"}, "isblack>0", null, null);
        
        if (null != c && c.moveToFirst()) {
            try {
            	do {
            		String number = c.getString(0);
            		if (number == null) {
            			continue;
            		}
            		
            		String numberE164 = PhoneNumberUtils.formatNumberToE164(
			                number, ContactsUtils.getCurrentCountryIso(mContext));
            		
            		ContactDetailActivity.mRejectedList.add(number);
            		if (numberE164 != null && !number.equals(numberE164) && !ContactDetailActivity.mRejectedList.contains(numberE164)) {
            			ContactDetailActivity.mRejectedList.add(numberE164);
					}
            		
            		//add by ligy 
            		String trimNumber = number.replace(" ", "");
            		trimNumber = trimNumber.replace("-", "");
            		if (trimNumber != null && !number.equals(trimNumber) && !ContactDetailActivity.mRejectedList.contains(trimNumber)) {
            			ContactDetailActivity.mRejectedList.add(trimNumber);
					}
				} while (c.moveToNext());

            	Log.d(TAG, "ContactDetailActivity.mRejectedList = " + ContactDetailActivity.mRejectedList 
            			+ "   ContactDetailActivity.mThisRejectedList = " + ContactDetailActivity.mThisRejectedList);
            } catch (Exception e) {
                e.printStackTrace();
            }   
        }
        
        if (c != null) {
        	c.close();
        }
    }
    
}
