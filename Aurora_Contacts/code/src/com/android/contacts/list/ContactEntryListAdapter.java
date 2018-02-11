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


import com.android.internal.telephony.ITelephony;


import com.android.contacts.ContactPhotoManager;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;

import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.widget.IndexerListAdapter;
import com.android.contacts.widget.TextWithHighlightingFactory;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.util.OperatorUtils;

import android.R.integer;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.ContactCounts;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
//The following lines are provided and maintained by Mediatek inc.

import android.os.ServiceManager;
//The following lines are provided and maintained by Mediatek inc.

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.gionee.internal.telephony.GnITelephony;
//Gionee <xuhz> <2013-08-05> add for CR00845736 begin
import android.os.SystemProperties;
//Gionee <xuhz> <2013-08-05> add for CR00845736 end
import com.android.contacts.AuroraCardTypeUtils;
/**
 * Common base class for various contact-related lists, e.g. contact list, phone number list
 * etc.
 */
public abstract class ContactEntryListAdapter extends IndexerListAdapter {

    private static final String TAG = "ContactEntryListAdapter";

    /**
     * Indicates whether the {@link Directory#LOCAL_INVISIBLE} directory should
     * be included in the search.
     */
    private static final boolean LOCAL_INVISIBLE_DIRECTORY_ENABLED = false;

    /**
     * The animation is used here to allocate animated name text views.
     */
    private TextWithHighlightingFactory mTextWithHighlightingFactory;
    private int mDisplayOrder;
    private int mSortOrder;
    private boolean mNameHighlightingEnabled;

    private boolean mDisplayPhotos;
    private boolean mQuickContactEnabled;

    /**
     * indicates if contact queries include profile
     */
    private boolean mIncludeProfile;

    /**
     * indicates if query results includes a profile
     */
    private boolean mProfileExists;

    private ContactPhotoManager mPhotoLoader;

    public String mQueryString;
    private char[] mUpperCaseQueryString;
    private boolean mSearchMode;
    private int mDirectorySearchMode;
    private int mDirectoryResultLimit = Integer.MAX_VALUE;

    private boolean mLoading = true;
    private boolean mEmptyListEnabled = true;

    private boolean mSelectionVisible;

    private ContactListFilter mFilter;
    private String mFilterExInfo;
    private String mContactsCount = "";
    private boolean mDarkTheme = false;
    
    private boolean mIsNeedStarredShow = false;
    public boolean mIsCheckBoxAppear = false;
    
    private int mStarredCount = 0;
    
    public void setStarredCount(int count) {
        mStarredCount = count;
    }
    
    // add by wangth
    public int getStarredCount() {
        return mStarredCount;
    }
    // add by wangth
 
    public ContactEntryListAdapter(Context context) {
        super(context);
        addPartitions();
    }

    @Override
    protected View createPinnedSectionHeaderView(Context context, ViewGroup parent) {
        return new ContactListPinnedHeaderView(context, null);
    }

    @Override
    protected void setPinnedSectionTitle(View pinnedHeaderView, String title) {
        ((ContactListPinnedHeaderView)pinnedHeaderView).setSectionHeader(title);
    }

    @Override
    protected void setPinnedHeaderContactsCount(View header) {
        // Update the header with the contacts count only if a profile header exists
        // otherwise, the contacts count are shown in the empty profile header view
        if (mProfileExists) {
            ((ContactListPinnedHeaderView)header).setCountView(mContactsCount);
        } else {
            clearPinnedHeaderContactsCount(header);
        }
    }

    @Override
    protected void clearPinnedHeaderContactsCount(View header) {
        ((ContactListPinnedHeaderView)header).setCountView(null);
    }

    protected void addPartitions() {
        addPartition(createDefaultDirectoryPartition());
    }

    protected DirectoryPartition createDefaultDirectoryPartition() {
        DirectoryPartition partition = new DirectoryPartition(true, true);
        partition.setDirectoryId(Directory.DEFAULT);
        partition.setDirectoryType(getContext().getString(R.string.contactsList));
        partition.setPriorityDirectory(true);
        partition.setPhotoSupported(true);
        return partition;
    }

    private int getPartitionByDirectoryId(long id) {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                if (((DirectoryPartition)partition).getDirectoryId() == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public abstract String getContactDisplayName(int position);
    public abstract void configureLoader(CursorLoader loader, long directoryId);

    /**
     * Marks all partitions as "loading"
     */
    public void onDataReload() {
        boolean notify = false;
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition)partition;
                if (!directoryPartition.isLoading()) {
                    notify = true;
                }
                directoryPartition.setStatus(DirectoryPartition.STATUS_NOT_LOADED);
            }
        }
        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void clearPartitions() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition)partition;
                directoryPartition.setStatus(DirectoryPartition.STATUS_NOT_LOADED);
            }
        }
        super.clearPartitions();
    }

    public boolean isSearchMode() {
        return mSearchMode;
    }

    public void setSearchMode(boolean flag) {
        mSearchMode = flag;
    }
    
    //aurora <wangth> <2013-9-3> modify for auroro ui begin
    public void setStarredMode(boolean flag) {
        mIsNeedStarredShow = flag;
    }
    //aurora <wangth> <2013-9-3> modify for auroro ui end

    public String getQueryString() {
        return mQueryString;
    }

    public void setQueryString(String queryString) {
        mQueryString = queryString;
        if (TextUtils.isEmpty(queryString)) {
            mUpperCaseQueryString = null;
        } else {
            mUpperCaseQueryString = queryString.toUpperCase().toCharArray();
        }
    }

    public char[] getUpperCaseQueryString() {
        return mUpperCaseQueryString;
    }

    public int getDirectorySearchMode() {
        return mDirectorySearchMode;
    }

    public void setDirectorySearchMode(int mode) {
        mDirectorySearchMode = mode;
    }

    public int getDirectoryResultLimit() {
        return mDirectoryResultLimit;
    }

    public void setDirectoryResultLimit(int limit) {
        this.mDirectoryResultLimit = limit;
    }

    public int getContactNameDisplayOrder() {
        return mDisplayOrder;
    }

    public void setContactNameDisplayOrder(int displayOrder) {
        mDisplayOrder = displayOrder;
    }

    public int getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        mSortOrder = sortOrder;
    }

    public void setPhotoLoader(ContactPhotoManager photoLoader) {
        mPhotoLoader = photoLoader;
    }

    protected ContactPhotoManager getPhotoLoader() {
        return mPhotoLoader;
    }

    public boolean getDisplayPhotos() {
        return mDisplayPhotos;
    }

    public void setDisplayPhotos(boolean displayPhotos) {
        mDisplayPhotos = displayPhotos;
    }

    public boolean isEmptyListEnabled() {
        return mEmptyListEnabled;
    }

    public void setEmptyListEnabled(boolean flag) {
        mEmptyListEnabled = flag;
    }

    public boolean isSelectionVisible() {
        return mSelectionVisible;
    }

    public void setSelectionVisible(boolean flag) {
        this.mSelectionVisible = flag;
    }

    public boolean isQuickContactEnabled() {
        mQuickContactEnabled = false; // wangth add for aurora 20131106
        return mQuickContactEnabled;
    }

    public void setQuickContactEnabled(boolean quickContactEnabled) {
        mQuickContactEnabled = quickContactEnabled;
    }

    public boolean shouldIncludeProfile() {
        return mIncludeProfile;
    }

    public void setIncludeProfile(boolean includeProfile) {
        mIncludeProfile = includeProfile;
    }

    public void setProfileExists(boolean exists) {
        mProfileExists = exists;
        // Stick the "ME" header for the profile
        if (exists) {
            SectionIndexer indexer = getIndexer();
            if (indexer != null) {
                ((ContactsSectionIndexer) indexer).setProfileHeader(
                        getContext().getString(R.string.user_profile_contacts_list_header));
            }
        }
    }

    public boolean hasProfile() {
        return mProfileExists;
    }

    public void setDarkTheme(boolean value) {
        mDarkTheme = value;
    }

    public void configureDirectoryLoader(DirectoryListLoader loader) {
        loader.setDirectorySearchMode(mDirectorySearchMode);
        loader.setLocalInvisibleDirectoryEnabled(LOCAL_INVISIBLE_DIRECTORY_ENABLED);
    }

    /**
     * Updates partitions according to the directory meta-data contained in the supplied
     * cursor.
     */
    public void changeDirectories(Cursor cursor) {
        if (cursor.getCount() == 0) {
            // Directory table must have at least local directory, without which this adapter will
            // enter very weird state.
            Log.e(TAG, "Directory search loader returned an empty cursor, which implies we have " +
                    "no directory entries.", new RuntimeException());
            return;
        }
        HashSet<Long> directoryIds = new HashSet<Long>();

        int idColumnIndex = cursor.getColumnIndex(Directory._ID);
        int directoryTypeColumnIndex = cursor.getColumnIndex(DirectoryListLoader.DIRECTORY_TYPE);
        int displayNameColumnIndex = cursor.getColumnIndex(Directory.DISPLAY_NAME);
        int photoSupportColumnIndex = cursor.getColumnIndex(Directory.PHOTO_SUPPORT);

        // TODO preserve the order of partition to match those of the cursor
        // Phase I: add new directories
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumnIndex);
            directoryIds.add(id);
            if (getPartitionByDirectoryId(id) == -1) {
                DirectoryPartition partition = new DirectoryPartition(false, true);
                partition.setDirectoryId(id);
                partition.setDirectoryType(cursor.getString(directoryTypeColumnIndex));
                partition.setDisplayName(cursor.getString(displayNameColumnIndex));
                int photoSupport = cursor.getInt(photoSupportColumnIndex);
                partition.setPhotoSupported(photoSupport == Directory.PHOTO_SUPPORT_THUMBNAIL_ONLY
                        || photoSupport == Directory.PHOTO_SUPPORT_FULL);
                addPartition(partition);
            }
        }

        // Phase II: remove deleted directories
        int count = getPartitionCount();
        for (int i = count; --i >= 0; ) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                long id = ((DirectoryPartition)partition).getDirectoryId();
                if (!directoryIds.contains(id)) {
                    removePartition(i);
                }
            }
        }

        invalidate();
        notifyDataSetChanged();
    }

    @Override
    public void changeCursor(int partitionIndex, Cursor cursor) {
        if (partitionIndex >= getPartitionCount()) {
            // There is no partition for this data
            return;
        }

        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            ((DirectoryPartition)partition).setStatus(DirectoryPartition.STATUS_LOADED);
        }

        if (mDisplayPhotos && mPhotoLoader != null && isPhotoSupported(partitionIndex)) {
            mPhotoLoader.refreshCache();
        }

        super.changeCursor(partitionIndex, cursor);

        if (isSectionHeaderDisplayEnabled() && partitionIndex == getIndexedPartition()) {
            updateIndexer(cursor);
        }
    }

    public void changeCursor(Cursor cursor) {
        changeCursor(0, cursor);
    }

    /**
     * Updates the indexer, which is used to produce section headers.
     */
    private void updateIndexer(Cursor cursor) {
        if (cursor == null) {
            setIndexer(null);
            return;
        }

        Bundle bundle = cursor.getExtras();
        if (bundle != null && bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
            String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
            int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
            
            // aurora <wangth> <2013-9-2> modify for auroro ui begin 
//            int starredCount = ContactsUtils.getStarredCount(mContext);
            if (mIsNeedStarredShow) {
                if (mStarredCount > 0) {
                    String newSections[] = new String[sections.length + 1];
                    int newCounts[] = new int[counts.length + 1];
                    newSections[0] = mContext.getString(R.string.aurora_starred_contacts_header);
                    newCounts[0] = mStarredCount;
                    
                    for (int i = 0; i < sections.length; i++) {
                        newSections[i + 1] = sections[i];
                    }
                    
                    for (int i = 0; i < counts.length; i++) {
                        newCounts[i + 1] = counts[i];
                    }
                    
                    setIndexer(new ContactsSectionIndexer(newSections, newCounts));
                    return;
                }
            }
            // aurora <wangth> <2013-9-2> modify for auroro ui end
            
            setIndexer(new ContactsSectionIndexer(sections, counts));
        } else {
            setIndexer(null);
        }
    }

    @Override
    public int getViewTypeCount() {
        // We need a separate view type for each item type, plus another one for
        // each type with header, plus one for "other".
        return getItemViewTypeCount() * 2 + 1;
    }

    @Override
    public int getItemViewType(int partitionIndex, int position) {
        int type = super.getItemViewType(partitionIndex, position);
        if (!isUserProfile(position)
                && isSectionHeaderDisplayEnabled()
                && partitionIndex == getIndexedPartition()) {
            Placement placement = getItemPlacementInSection(position);
            return placement.firstInSection ? type : getItemViewTypeCount() + type;
        } else {
            return type;
        }
    }

    @Override
    public boolean isEmpty() {
        // TODO
//        if (contactsListActivity.mProviderStatus != ProviderStatus.STATUS_NORMAL) {
//            return true;
//        }

        if (!mEmptyListEnabled) {
            return false;
        } else if (isSearchMode()) {
            return TextUtils.isEmpty(getQueryString());
        } else if (mLoading) {
            // We don't want the empty state to show when loading.
            return false;
        } else {
            return super.isEmpty();
        }
    }

    public boolean isLoading() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition
                    && ((DirectoryPartition) partition).isLoading()) {
                return true;
            }
        }
        return false;
    }

    public boolean areAllPartitionsEmpty() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            if (!isPartitionEmpty(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Changes visibility parameters for the default directory partition.
     */
    public void configureDefaultPartition(boolean showIfEmpty, boolean hasHeader) {
        // aurora <wangth> <2013-9-25> add for aurora ui begin 
        if (isSearchMode()) {
            hasHeader = false;
        }
        // aurora <wangth> <2013-9-25> add for aurora ui end
        int defaultPartitionIndex = -1;
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition &&
                    ((DirectoryPartition)partition).getDirectoryId() == Directory.DEFAULT) {
                defaultPartitionIndex = i;
                break;
            }
        }
        if (defaultPartitionIndex != -1) {
            setShowIfEmpty(defaultPartitionIndex, showIfEmpty);
            setHasHeader(defaultPartitionIndex, hasHeader);
        }
    }

    @Override
    protected View newHeaderView(Context context, int partition, Cursor cursor,
            ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.directory_header, parent, false);
    }

    @Override
    protected void bindHeaderView(View view, int partitionIndex, Cursor cursor) {
        Partition partition = getPartition(partitionIndex);
        if (!(partition instanceof DirectoryPartition)) {
            return;
        }

        DirectoryPartition directoryPartition = (DirectoryPartition)partition;
        long directoryId = directoryPartition.getDirectoryId();
        TextView labelTextView = (TextView)view.findViewById(R.id.label);
        TextView displayNameTextView = (TextView)view.findViewById(R.id.display_name);
        if (directoryId == Directory.DEFAULT || directoryId == Directory.LOCAL_INVISIBLE) {
            labelTextView.setText(R.string.local_search_label);
            displayNameTextView.setText(null);
        } else {
            labelTextView.setText(R.string.directory_search_label);
            String directoryName = directoryPartition.getDisplayName();
            String displayName = !TextUtils.isEmpty(directoryName)
                    ? directoryName
                    : directoryPartition.getDirectoryType();
            displayNameTextView.setText(displayName);
        }

        TextView countText = (TextView)view.findViewById(R.id.count);
        if (directoryPartition.isLoading()) {
            countText.setText(R.string.search_results_searching);
        } else {
            int count = cursor == null ? 0 : cursor.getCount();
            if (directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE
                    && count >= getDirectoryResultLimit()) {
                countText.setText(mContext.getString(
                        R.string.foundTooManyContacts, getDirectoryResultLimit()));
            } else {
            	if (ContactsApplication.sIsGnContactsSupport) {
            		countText.setText(getQuantityText(
                            count, R.string.listFoundAllContactsZero, R.plurals.gn_searchFoundContacts));
            	} else {
            		countText.setText(getQuantityText(
                            count, R.string.listFoundAllContactsZero, R.plurals.searchFoundContacts));
            	}
            }
        }

        // gionee xuhz 20121129 add for GIUI2.0 start
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {        	
        	countText.setPadding(ResConstant.sHeaderTextLeftPadding, 0, 0, 0);
        	countText.setTextSize(16);
            labelTextView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_LEFT | RelativeLayout.ALIGN_PARENT_TOP);
            countText.setLayoutParams(params);
        }
        // gionee xuhz 20121129 add for GIUI2.0 end
    }

    /**
     * Checks whether the contact entry at the given position represents the user's profile.
     */
    protected boolean isUserProfile(int position) {
        // The profile only ever appears in the first position if it is present.  So if the position
        // is anything beyond 0, it can't be the profile.
        boolean isUserProfile = false;
        // aurora <wangth> <2013-11-6> remove for aurora begin
//        if (position == 0) {
//            int partition = getPartitionForPosition(position);
//            if (partition >= 0) {
//                // Save the old cursor position - the call to getItem() may modify the cursor
//                // position.
//                int offset = getCursor(partition).getPosition();
//                Cursor cursor = (Cursor) getItem(position);
//                if (cursor != null) {
//                    int profileColumnIndex = cursor.getColumnIndex(Contacts.IS_USER_PROFILE);
//                    if (profileColumnIndex != -1) {
//                        isUserProfile = cursor.getInt(profileColumnIndex) == 1;
//                    }
//                    // Restore the old cursor position.
//                    cursor.moveToPosition(offset);
//                }
//            }
//        }
        //  aurora <wangth> <2013-11-6> remove for aurora end
        return isUserProfile;
    }

    // TODO: fix PluralRules to handle zero correctly and use Resources.getQuantityText directly
    public String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
        if (count == 0) {
            return getContext().getString(zeroResourceId);
        } else {
            String format = getContext().getResources()
                    .getQuantityText(pluralResourceId, count).toString();
            return String.format(format, count);
        }
    }

    public boolean isPhotoSupported(int partitionIndex) {
        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            return ((DirectoryPartition) partition).isPhotoSupported();
        }
        return true;
    }

    /**
     * Returns the currently selected filter.
     */
    public ContactListFilter getFilter() {
        return mFilter;
    }

    public void setFilter(ContactListFilter filter) {
        mFilter = filter;
    }

    // TODO: move sharable logic (bindXX() methods) to here with extra arguments

    protected void bindQuickContact(final ContactListItemView view, int partitionIndex,
            Cursor cursor, int photoIdColumn, int contactIdColumn, int lookUpKeyColumn) {
        long photoId = 0;
        if (!cursor.isNull(photoIdColumn)) {
            photoId = cursor.getLong(photoIdColumn);
        }
        //The following lines are provided and maintained by Mediatek inc.
        int indicatePhoneSim = cursor.getInt(cursor
                .getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
        if(indicatePhoneSim > 0){
            photoId = getSimType(indicatePhoneSim);
        }
        //The following lines are provided and maintained by Mediatek inc.
        QuickContactBadge quickContact = view.getQuickContact();
        quickContact.assignContactUri(
                getContactUri(partitionIndex, cursor, contactIdColumn, lookUpKeyColumn));
        getPhotoLoader().loadPhoto(quickContact, photoId, false, mDarkTheme);
    }

    protected Uri getContactUri(int partitionIndex, Cursor cursor,
            int contactIdColumn, int lookUpKeyColumn) {
        long contactId = cursor.getLong(contactIdColumn);
        String lookupKey = cursor.getString(lookUpKeyColumn);
        Uri uri = Contacts.getLookupUri(contactId, lookupKey);
        long directoryId = ((DirectoryPartition)getPartition(partitionIndex)).getDirectoryId();
        if (directoryId != Directory.DEFAULT) {
            uri = uri.buildUpon().appendQueryParameter(
                    GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
        }
        return uri;
    }

    public void setContactsCount(String count) {
        mContactsCount = count;
    }

    public String getContactsCount() {
        return mContactsCount;
    }

    // The following lines are provided and maintained by Mediatek inc.
    private int mSlot = -1;
    private SIMInfoWrapper mSimInfoWrapper;
    private ITelephony mITelephony;
    
    public long getSimType(int indicate) {
        long photoId = 0;
        if (mSimInfoWrapper == null) {
            mSimInfoWrapper = SIMInfoWrapper.getDefault();
        }
        mSlot = mSimInfoWrapper.getSimSlotById(indicate);
//        if (null == mITelephony) {
        mITelephony = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
//      }
        //Gionee <xuhz> <2013-08-05> modify for CR00845736 begin
        String optr = SystemProperties.get("ro.operator.optr");
        if (optr != null && optr.equals("OP02")) {
        //Gionee <xuhz> <2013-08-05> modify for CR00845736 end
            Log.i(TAG,"[getSimType] OP02 mSlot : "+mSlot);
            if (mSlot == 0) {
                return -3;
            } else {
                return -4;
            }
        } 
        
        // qc begin
        if (ContactsApplication.isMultiSimEnabled && GNContactsUtils.isOnlyQcContactsSupport()) {
            mSlot = indicate -1;
            
            if (mSlot == 0) {
                return -3;
            } else {
                return -4;
            }
        }
        // qc end
        // Gionee lihuafang 20120422 add for CR00573564 begin
        else if (ContactsUtils.mIsGnContactsSupport && (ContactsUtils.mIsGnShowSlotSupport || ContactsUtils.mIsGnShowDigitalSlotSupport)) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (mSlot == 0) {
                    return -3;
                } else if (mSlot == 1) {
                    return -4;
                } else {
                    return 0;
                }
            } else {
                if (mSlot == 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        // Gionee lihuafang 20120422 add for CR00573564 begin
        else {
            try {
                boolean bUSIM = false;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    if (mITelephony != null
                            //qc--mtk
                            //&& mITelephony.getIccCardTypeGemini(mSlot).equals("USIM")) {
                            && AuroraCardTypeUtils.getIccCardTypeGemini(mITelephony, mSlot).equals("USIM")) {
                        bUSIM = true;
                    }
                } else {
                    //qc--mtk
                    //if (mITelephony != null && mITelephony.getIccCardType().equals("USIM")) {
                    if (mITelephony != null && AuroraCardTypeUtils.getIccCardType(mITelephony).equals("USIM")) {
                        bUSIM = true;
                    }
                }
                Log
                        .i(TAG, "[getSimType] bUSIM : " + bUSIM + " | indicate : " + indicate
                                + " | mSlot : " + mSlot + " | mITelephony !=null :"
                                + (mITelephony != null));
                Log.i(TAG,
                        "[getSimType] FeatureOption.MTK_GEMINI_SUPPORT : "
                                + FeatureOption.MTK_GEMINI_SUPPORT);
                if (bUSIM) {
                    photoId = -2;
                } else {
                    photoId = -1;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return photoId;
        }
    }
    // The following lines are provided and maintained by Mediatek inc.

    public void setFilterExInfo(String exinfo) {
		mFilterExInfo = exinfo;
	}
    
    public String getFilterExInfo() {
		return mFilterExInfo;
	}
    
    
    // aurora <wangth> <2013-9-26> modify for aurora ui begin 
    public HashMap<Long, String> mCheckedItem = new HashMap<Long, String>();
    public boolean mCheckBoxEnable = false;
    public boolean mNeedAnim = false;
    public boolean mIsQueryForDialer = false;
    
    public void setIsQueryForDialer(boolean flag) {
        mIsQueryForDialer = flag;
    }
    
    
    
    public boolean getIsQueryForDialer() {
        return mIsQueryForDialer;
    }
    
    public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }
    
    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }
    
    public void setNeedAnim(boolean flag) {
        mNeedAnim = flag;
    }
    
    public boolean getNeedAnim() {
        return mNeedAnim;
    }
    
    public boolean mAuroraListDelet = false;
    
    public void setAuroraListDelet(boolean flag) {
        mAuroraListDelet = flag;
    }
    
    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            setNeedAnim(false);
            super.handleMessage(msg);
        }

    };
    
    public void setCheckedItem(long contactId, String str) {
        if (mCheckedItem == null) {
            mCheckedItem = new HashMap<Long, String>();
        }
        
        mCheckedItem.put(contactId, str);
    }
    
    
    public int getSelectedCount(){
    	if(mCheckedItem==null) return 0;
    	return mCheckedItem.size();
    }
    
    public HashMap<Long, String> getCheckedItem() {
        return mCheckedItem;
    }
    
    public int getContactID(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        // aurora <wangth> <2013-10-19> modify for aurora ui begin 
        if (getIsQueryForDialer()) {
            return cursor.getInt(cursor.getColumnIndexOrThrow("contact_id"));
        }
        // aurora <wangth> <2013-10-19> modify for aurora ui end
        return cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
    }
    
    public int getRawcontactId(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        // aurora <wangth> <2013-10-19> modify for aurora ui begin 
        if (getIsQueryForDialer()) {
            int contactId = cursor.getInt(cursor.getColumnIndexOrThrow("contact_id"));
            return ContactsUtils.queryForRawContactId(mContext.getContentResolver(), contactId);
        }
        // aurora <wangth> <2013-10-19> modify for aurora ui end
        return cursor.getInt(cursor.getColumnIndexOrThrow(ContactQuery.RAW_CONTACT_ID));
    }
    
    public int getIndicatePhoneSim(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        
        if (getIsQueryForDialer()) {
            return cursor.getInt(cursor.getColumnIndexOrThrow("indicate_phone_sim"));
        }
        
        return cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
    }
    
    public int getSimIndex(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        
        if (getIsQueryForDialer()) {
            return cursor.getInt(cursor.getColumnIndexOrThrow("index_in_sim"));
        }
        
        return cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDEX_IN_SIM));
    }
    // aurora <wangth> <2013-9-26> modify for aurora ui end
}
