package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

import java.util.ArrayList;
import java.util.List;

import com.android.contacts.R;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.dialpad.IDialerSearchController.AuroraDialerSearchResultColumns;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.provider.ContactsContract.SearchSnippets;

/**
 * A cursor adapter for the {@link Phone#CONTENT_TYPE} content type.
 */
public class AuroraSimContactListAdapter extends ContactEntryListAdapter {
    private static final String TAG = AuroraSimContactListAdapter.class.getSimpleName();

    protected static class PhoneQuery {
        private static final String[] PROJECTION_PRIMARY = new String[] {
            Phone._ID,                          // 0
            Phone.TYPE,                         // 1
            Phone.LABEL,                        // 2
            Phone.NUMBER,                       // 3
            Phone.CONTACT_ID,                   // 4
            Phone.LOOKUP_KEY,                   // 5
            Phone.PHOTO_ID,                     // 6
            Phone.DISPLAY_NAME_PRIMARY,         // 7
            RawContacts.INDICATE_PHONE_SIM,     // 8
            
            "auto_record",
            "is_privacy"
        };

        private static final String[] PROJECTION_ALTERNATIVE = new String[] {
            Phone._ID,                          // 0
            Phone.TYPE,                         // 1
            Phone.LABEL,                        // 2
            Phone.NUMBER,                       // 3
            Phone.CONTACT_ID,                   // 4
            Phone.LOOKUP_KEY,                   // 5
            Phone.PHOTO_ID,                     // 6
            Phone.DISPLAY_NAME_ALTERNATIVE,     // 7
            RawContacts.INDICATE_PHONE_SIM,     // 8
            
            "auto_record",
            "is_privacy"
        };

        private static final int PHONE_ID           = 0;
        
        
        private static final int PHONE_NUMBER       = 3;
        
        
        private static final int PHONE_PHOTO_ID     = 6;
         
        public static final int PHONE_DISPLAY_NAME = 7;
        private static final int PHONE_INDICATE_PHONE_SIM_COLUMN_INDEX = 8;
        
        
        
    }
    
    protected static class EmailQuery {
        private static final String[] PROJECTION_PRIMARY = new String[] {
            Email._ID,                       // 0
            Email.TYPE,                      // 1
            Email.LABEL,                     // 2
            Email.DATA,                      // 3
            Email.PHOTO_ID,                  // 4
            Email.DISPLAY_NAME_PRIMARY,      // 5
            RawContacts.INDICATE_PHONE_SIM,  // 8
        };

        private static final String[] PROJECTION_ALTERNATIVE = new String[] {
            Email._ID,                       // 0
            Email.TYPE,                      // 1
            Email.LABEL,                     // 2
            Email.DATA,                      // 3
            Email.PHOTO_ID,                  // 4
            Email.DISPLAY_NAME_ALTERNATIVE,  // 5
            RawContacts.INDICATE_PHONE_SIM,  // 8
        };

        private static final int EMAIL_ID           = 0;
        
        
        private static final int EMAIL_ADDRESS      = 3;
        
        private static final int EMAIL_DISPLAY_NAME = 5;
        
    }

    private final CharSequence mUnknownNameText;

    private ContactListItemView.PhotoPosition mPhotoPosition;

    public AuroraSimContactListAdapter(Context context) {
        super(context);

        mUnknownNameText = context.getText(android.R.string.unknownName);
    }

    protected CharSequence getUnknownNameText() {
        return mUnknownNameText;
    }

    @Override
    public void configureLoader(CursorLoader loader, long directoryId) {
    	Uri uri;
    	if (mIsEmailSelectMode) {
    		final Builder builder;
    		if (isSearchMode()) {
                builder = Email.CONTENT_FILTER_URI.buildUpon();
                String query = getQueryString();
                builder.appendPath(TextUtils.isEmpty(query) ? "" : query);
            } else {
                builder = Email.CONTENT_URI.buildUpon();
            }
            builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                    String.valueOf(directoryId));
            builder.appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true");
            uri = builder.build();
            if (isSectionHeaderDisplayEnabled()) {
                uri = buildSectionIndexerUri(uri);
            }
            loader.setUri(uri);

            if (getContactNameDisplayOrder() == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                loader.setProjection(EmailQuery.PROJECTION_PRIMARY);
            } else {
                loader.setProjection(EmailQuery.PROJECTION_ALTERNATIVE);
            }

            if (getSortOrder() == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
                loader.setSortOrder(Email.SORT_KEY_PRIMARY);
            } else {
                loader.setSortOrder(Email.SORT_KEY_ALTERNATIVE);
            }
            
    	    return;	
    	}
    	
        if (directoryId != Directory.DEFAULT) {
            Log.w(TAG, "PhoneNumberListAdapter is not ready for non-default directory ID ("
                    + "directoryId: " + directoryId + ")");
        }
        
        if (loader instanceof StarredAndContactsLoader) {
            ((StarredAndContactsLoader) loader).setLoadStars(true);
            setStarredMode(true);
        }

        if (isSearchMode()) {
            String query = getQueryString();
            Builder builder = Phone.CONTENT_FILTER_URI.buildUpon();
            if (TextUtils.isEmpty(query)) {
                builder.appendPath("");
            } else {
                builder.appendPath(query);      // Builder will encode the query
            }

            builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                    String.valueOf(directoryId));
            uri = builder.build();
            configureSelection(loader, directoryId, getFilter());
        } else {
            uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                    .build();
            if (isSectionHeaderDisplayEnabled()) {
                uri = buildSectionIndexerUri(uri);
            }
            configureSelection(loader, directoryId, getFilter());
        }

        // Remove duplicates when it is possible.
        uri = uri.buildUpon()
                .appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
                .build();
        loader.setUri(uri);

        // TODO a projection that includes the search snippet
        if (getContactNameDisplayOrder() == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            loader.setProjection(PhoneQuery.PROJECTION_PRIMARY);
        } else {
            loader.setProjection(PhoneQuery.PROJECTION_ALTERNATIVE);
        }

        if (getSortOrder() == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            loader.setSortOrder(Phone.SORT_KEY_PRIMARY);
        } else {
            loader.setSortOrder(Phone.SORT_KEY_ALTERNATIVE);
        }
    }

    private void configureSelection(
            CursorLoader loader, long directoryId, ContactListFilter filter) {
    	if (mIsEmailSelectMode) {
    		return;
    	}
    	
        Log.d(TAG, "filter.filterType = " + filter.filterType);
        if (filter == null || directoryId != Directory.DEFAULT) {
            return;
        }

        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<String>();

        switch (filter.filterType) {
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
                selection.append(Contacts.IN_VISIBLE_GROUP + "=1");
                selection.append(" AND " + Contacts.HAS_PHONE_NUMBER + "=1");
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
            	long currentPrivacyId = AuroraPrivacyUtils.mCurrentAccountId;
                if (!mIsMmsSelectMode) {
                    if (mIsCallRecordMode) {
                    	selection.append("(");
                    	selection.append(RawContacts.INDICATE_PHONE_SIM + " < 0");
                        selection.append(" AND auto_record = 1");
                        if (ContactsApplication.sIsAuroraPrivacySupport && currentPrivacyId > 0) {
                        	selection.append(" AND is_privacy IN (0, " + currentPrivacyId + ")");
                        }
                        selection.append(")");
                    } else if (mIsAutoRecordSelectMode) {
                    	selection.append("(");
                    	selection.append(RawContacts.INDICATE_PHONE_SIM + " < 0");
                        selection.append(" AND auto_record = 0");
                        if (ContactsApplication.sIsAuroraPrivacySupport && currentPrivacyId > 0) {
                        	selection.append(" AND is_privacy IN (0, " + currentPrivacyId + ")");
                        }
                        selection.append(")");
                    } else if (mIsBlackNameMode) {
                		if (mBlackNumbers == null) {
//                			selection.append(" AND " + Phone.NUMBER);
                		} else {
                			selection.append(Phone.NUMBER + " not in(" + mBlackNumbers + ") ");
                		}
                    }
                } else {
//                    selection.append("(");
//                    selection.append(RawContacts.ACCOUNT_TYPE + "='" + AccountType.ACCOUNT_TYPE_LOCAL_PHONE + "'");
//                    selection.append(")");
                	if (ContactsApplication.sIsAuroraPrivacySupport && currentPrivacyId > 0) {
                    	selection.append("(is_privacy IN (0, " + currentPrivacyId + "))");
                    }
                }
                
                break;
            }
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS:
            case ContactListFilter.FILTER_TYPE_DEFAULT:
                break; // No selection needed.
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY:
                break; // This adapter is always "phone only", so no selection needed either.
            default:
                Log.w(TAG, "Unsupported filter type came " +
                        "(type: " + filter.filterType + ", toString: " + filter + ")" +
                        " showing all contacts.");
                // No selection.
                break;
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
    }

    protected static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
    }

    @Override
    public String getContactDisplayName(int position) {
    	if (mIsEmailSelectMode) {
    		return ((Cursor) getItem(position)).getString(EmailQuery.EMAIL_DISPLAY_NAME);
    	}
    	
        if (getIsQueryForDialer()) {
            return ((Cursor) getItem(position)).getString(6);
        }
        
        return ((Cursor) getItem(position)).getString(PhoneQuery.PHONE_DISPLAY_NAME);
    }

    /**
     * Builds a {@link Data#CONTENT_URI} for the given cursor position.
     *
     * @return Uri for the data. may be null if the cursor is not ready.
     */
    public Uri getDataUri(int position) {
        Cursor cursor = ((Cursor)getItem(position));
        if (cursor != null) {
            long id = cursor.getLong(PhoneQuery.PHONE_ID);
            return ContentUris.withAppendedId(Data.CONTENT_URI, id);
        } else {
            Log.w(TAG, "Cursor was null in getDataUri() call. Returning null instead.");
            return null;
        }
    }

    @Override
    protected View newView(Context context, int partition, Cursor cursor, int position,
            ViewGroup parent) {
        final ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(mUnknownNameText);
        view.setQuickContactEnabled(false);
        view.setPhotoPosition(mPhotoPosition);
        view.setDoubleRow(true);
        
        View v = (View) LayoutInflater.from(context).inflate(
                com.aurora.R.layout.aurora_slid_listview, null);
        RelativeLayout mainUi = (RelativeLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_front);
        mainUi.addView(view, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        ImageView iv = (ImageView) v.findViewById(com.aurora.R.id.aurora_listview_divider);
        iv.setVisibility(View.VISIBLE);
        
        return v;
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        final RelativeLayout mainUi = (RelativeLayout)itemView.findViewById(com.aurora.R.id.aurora_listview_front);
        final AuroraCheckBox checkBox = (AuroraCheckBox) itemView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        final ContactListItemView view = (ContactListItemView)(mainUi.getChildAt(0));
        //ContactListItemView view = (ContactListItemView)itemView;
        
        auroraBindSectionHeaderAndDivider(itemView, position, cursor);
        LinearLayout contentUi = (LinearLayout) itemView.findViewById(com.aurora.R.id.content);
        AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);
        
//        view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString()
//                : null);
        
        mainUi.removeViewAt(0);
        mainUi.addView(view, 0);
        
        view.auroraSetCheckable(false);
        
        View moveUi = mainUi;
        if (!mIsCallRecordMode) {
        	moveUi = view.getNameTextView();
        }
        
        int privacyId = 0;
        if (!mIsEmailSelectMode) {
        	privacyId = getPrivacyId(position);
        }
        
        if (getCheckBoxEnable()) {
            view.setNameTextViewStyle(true, true);
            
            String name = getContactDisplayName(position);
            String number = getNumber(position);
            if (null != number) {
                number = number.replaceAll(" ", "");
            }
            
            long dataId = getDataId(position);
            boolean checked = false;
            if (getCheckedItem() != null
                    && getCheckedItem().get(dataId) != null) {
                checked = true;
            }
            
            if (mIsMmsSelectMode) {
            	if (!mIsMmsAttachment) {
            		if (ContactsApplication.sIsAuroraRejectSupport) {
            			number = number + '\1' + privacyId;
            		}
            		
            		if (mNumberList != null && mNumberList.contains(number)) {
            			checked = true;
                        getCheckedItem().put(dataId, name + '\1' + number);
                        mNumberList.remove(number);
            		}
            	} else if (number != null
                        && mNumberList != null && mNumberList.contains(number)) {
            		checked = true;
                    getCheckedItem().put(dataId, name + ":" + number);
                    mNumberList.remove(number);
            	}
            } else if (mIsCallRecordMode && number != null
                    && mAutoRecordDataIdList != null && mAutoRecordDataIdList.contains(String.valueOf(dataId))) {
                checked = true;
                getCheckedItem().put(dataId, name + ":" + number);
                mAutoRecordDataIdList.remove(dataId);
            }
            
            if (getNeedAnim()) {
                AuroraListView.auroraStartCheckBoxAppearingAnim(moveUi, checkBox);
            } else {
                AuroraListView.auroraSetCheckBoxVisible(moveUi, checkBox, true);
            }

            checkBox.setChecked(checked);
        } else {
            view.setNameTextViewStyle(false, true);
            view.setCheckable(false);
            if (getNeedAnim()) {
                AuroraListView.auroraStartCheckBoxDisappearingAnim(moveUi, checkBox);
            } else {
                AuroraListView.auroraSetCheckBoxVisible(moveUi, checkBox, false);
            }
        }
         
        view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString() : null);

        bindName(view, cursor);
        bindPhoneNumber(view, cursor);

        
        if (!mIsEmailSelectMode) {
        	int sim_id = getIndicatePhoneSim(position);
            if (FeatureOption.MTK_GEMINI_SUPPORT && !mIsCallRecordMode && !mIsBlackNameMode) {
                if (sim_id > 0) {
                    view.setSimIconable(true, sim_id);
                } else {
                    view.setSimIconable(false, sim_id);
                }
            }
            
            if (ContactsApplication.sIsAuroraPrivacySupport && sim_id < 0) {
            	if (privacyId > 0) {
            		view.setPrivacyIconable(true, privacyId);
            	} else {
            		view.setPrivacyIconable(false, privacyId);
            	}
            }
        }
        
        if (!mIsCallRecordMode) {
        	if (getCheckBoxEnable()) {
            	if (view.mDataView != null && view.isVisible(view.getDataView())) {
                	AuroraListView.auroraSetCheckBoxVisible(view.getDataView(), checkBox, true);
                }
            } else {
            	if (view.mDataView != null && view.isVisible(view.getDataView())) {
                	AuroraListView.auroraSetCheckBoxVisible(view.getDataView(), checkBox, false);
                }
            }
        }
        
        
        if (getNeedAnim()) {
            mHandler.sendMessage(mHandler.obtainMessage());
        }
    }
    
    // aurora <wangth> <2013-11-2> add for aurora begin
    protected void auroraBindSectionHeaderAndDivider(View view, int position,
            Cursor cursor) {
        LinearLayout headerUi = (LinearLayout) view
                .findViewById(com.aurora.R.id.aurora_list_header);
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);
            ViewGroup.LayoutParams params = headerUi.getLayoutParams();
            if (placement.sectionHeader != null) {
                params.height = mContext.getResources().getDimensionPixelSize(
                        R.dimen.aurora_edit_group_margin_top);
                TextView tv = new TextView(mContext);
                tv.setText(placement.sectionHeader);
                int paddingLeft = mContext.getResources()
                        .getDimensionPixelSize(
                                R.dimen.aurora_group_entrance_left_margin);
//                if (placement.sectionHeader.equals("#")) {
//                    tv.setTextSize(15);
//                    tv.setPadding(paddingLeft, 0, 0, ContactsUtils.CONTACT_LIST_HEADER_PADDING_BOTTOM);
//                } else {
                    tv.setTextAppearance(mContext, R.style.aurora_list_header_style);
                    tv.setPadding(paddingLeft, 0, 0, ContactsUtils.CONTACT_LIST_HEADER_PADDING_BOTTOM);
                    if (placement.sectionHeader.equals("Q")) {
                        tv.setPadding(paddingLeft, 0, 0, 0);
                    }
//                }
                
                tv.setHeight(params.height);
                tv.setGravity(Gravity.BOTTOM);
                tv.setTextColor(mContext.getResources().getColor(
                        R.color.aurora_contact_list_header_color));
                if (headerUi != null) {
                    headerUi.removeAllViews();
                }
                headerUi.setEnabled(false);
                headerUi.setClickable(false);
                headerUi.addView(tv);
                headerUi.setLayoutParams(params);
            }
        }
        
        if (headerUi != null) {
            if (!isSearchMode()) {
                headerUi.setVisibility(View.VISIBLE);
            } else {
                headerUi.setVisibility(View.GONE);
            }
        }
        
        LinearLayout deleteUi = (LinearLayout) view
                .findViewById(com.aurora.R.id.aurora_listview_back);
        ViewGroup.LayoutParams param = deleteUi.getLayoutParams();
        param.width = mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_list_item_double_delete_back_width);
        deleteUi.setLayoutParams(param);
    }
    // aurora <wangth> <2013-11-2> add for aurora end

    protected void bindPhoneNumber(ContactListItemView view, Cursor cursor) {

    	
        if (isSearchMode()) {
        	if (getIsQueryForDialer()) {
                view.showData(cursor, AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
            } else {
            	view.auroraShowData(cursor, cursor.getString(mIsEmailSelectMode ? EmailQuery.EMAIL_ADDRESS : PhoneQuery.PHONE_NUMBER));
            }
        } else {
        	if (getIsQueryForDialer()) {
                view.showData(cursor, AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
            } else {
                view.showData(cursor, 
                		mIsEmailSelectMode ? EmailQuery.EMAIL_ADDRESS : PhoneQuery.PHONE_NUMBER);
            }
        }
    }

    protected void bindSectionHeaderAndDivider(final ContactListItemView view, int position) {
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);
            view.setSectionHeader(placement.firstInSection ? placement.sectionHeader : null);
            view.setDividerVisible(!placement.lastInSection);
        } else {
            view.setSectionHeader(null);
            view.setDividerVisible(true);
        }
    }

    protected void bindName(final ContactListItemView view, Cursor cursor) {
        //view.showDisplayName(cursor, PhoneQuery.PHONE_DISPLAY_NAME, getContactNameDisplayOrder());
        if (getIsQueryForDialer()) {
            view.auroraShowDisplayName(cursor, AuroraDialerSearchResultColumns.NAME_INDEX, mQueryString);
        } else {
            view.showDisplayName(cursor, 
            		mIsEmailSelectMode ? EmailQuery.EMAIL_DISPLAY_NAME : PhoneQuery.PHONE_DISPLAY_NAME, 
            				getContactNameDisplayOrder());
        }
        // Note: we don't show phonetic names any more (see issue 5265330)
    }

    protected void unbindName(final ContactListItemView view) {
        view.hideDisplayName();
    }

    protected void bindPhoto(final ContactListItemView view, Cursor cursor) {
        long photoId = 0;
        if (!cursor.isNull(PhoneQuery.PHONE_PHOTO_ID)) {
            photoId = cursor.getLong(PhoneQuery.PHONE_PHOTO_ID);
        }
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     
         *   CR ID: ALPS00112776
         *   Descriptions: sim card icon display not right
         */
        int indicatePhoneSim = cursor.getInt(PhoneQuery.PHONE_INDICATE_PHONE_SIM_COLUMN_INDEX);
        if (indicatePhoneSim > 0) {
            photoId = getSimType(indicatePhoneSim);
        }
        /*
         * Bug Fix by Mediatek End.
         */

        getPhotoLoader().loadPhoto(view.getPhotoView(), photoId, false, false);
    }

    public void setPhotoPosition(ContactListItemView.PhotoPosition photoPosition) {
        mPhotoPosition = photoPosition;
    }

    public ContactListItemView.PhotoPosition getPhotoPosition() {
        return mPhotoPosition;
    }

    public String getNumber(int position) {
        if (getIsQueryForDialer()) {
            return ((Cursor) getItem(position)).getString(AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
        }
        return ((Cursor) getItem(position)).getString(
        		mIsEmailSelectMode ? EmailQuery.EMAIL_ADDRESS : PhoneQuery.PHONE_NUMBER);
    }
    
    @Override
    public int getContactID(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        
        if (getIsQueryForDialer()) {
            return cursor.getInt(1);
        }

        return cursor.getInt(cursor.getColumnIndexOrThrow("contact_id"));
    }
    
    public long getDataId(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        
        if (getIsQueryForDialer()) {
            int contactId = cursor.getInt(1);
            String number = getNumber(position);
            int rawContactId = ContactsUtils.queryForRawContactId(mContext.getContentResolver(), contactId);
            
            Cursor c = mContext.getContentResolver().query(
                    Data.CONTENT_URI, 
                    new String[] {"_id"}, 
                    "raw_contact_id=" + rawContactId + " and mimetype_id=5 and data1='" + number + "'", 
                    null, 
                    null);
            
            long dataId = 0;
            if (c != null) {
                if (c.moveToFirst()) {
                    dataId = c.getLong(0);
                }
                
                c.close();
            }
            
            return dataId;
        }
        
        return cursor.getLong(mIsEmailSelectMode ? EmailQuery.EMAIL_ID : PhoneQuery.PHONE_ID);
    }
    
    public int getPrivacyId(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return 0;
        }
        
        return cursor.getInt(cursor.getColumnIndexOrThrow("is_privacy"));
    }
    
    private boolean mIsBlackNameMode = false;
    private boolean mIsCallRecordMode = false;
    private boolean mIsMmsSelectMode = false;
    private boolean mIsMmsAttachment = false;
    private boolean mIsAutoRecordSelectMode = false;
    private boolean mIsEmailSelectMode = false;
    private ArrayList<String> mAutoRecordDataIdList = new ArrayList<String>();
    private ArrayList<String> mNumberList = new ArrayList<String>();
    private ArrayList<String> mEmailList = new ArrayList<String>();
    private String mBlackNumbers = null;
    public void setMmsSelectMode(boolean flag, ArrayList<String> number) {
        mIsMmsSelectMode = flag;
        mNumberList = number;
    }
    
    public ArrayList<String> getMmsSelectList() {
        return mNumberList;
    }
    
    public void setIsMmsAttachment(boolean falg) {
    	mIsMmsAttachment = falg;
    }
    
    public void setCallRecordMode(boolean flag) {
        mIsCallRecordMode = flag;
    }
    
    public void setAutoRecordSelectMode(boolean flag, ArrayList<String> id) {
        mIsAutoRecordSelectMode = flag;
        mAutoRecordDataIdList = id;
    }
    
    public ArrayList<String> getCallRecordSelectList() {
        return mAutoRecordDataIdList;
    }
    
    public void setBlackNameSelectMode (boolean flag, String numbers) {
    	mIsBlackNameMode = flag;
    	mBlackNumbers = numbers;
    }
    
    public void setEmailSelectMode(boolean flag, ArrayList<String> number) {
    	mIsEmailSelectMode = flag;
        mEmailList = number;
        
        if (mIsEmailSelectMode) {
        	
        }
    }
    
    public ArrayList<String> getEmailSelectList() {
        return mEmailList;
    }
} 
