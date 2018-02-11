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

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.ResConstant;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.model.AccountType;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import com.android.contacts.R;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import aurora.preference.AuroraPreferenceManager; // import android.preference.PreferenceManager;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.SearchSnippetColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

/**
 * A cursor adapter for the {@link GnContactsContract.Contacts#CONTENT_TYPE} content type.
 */
public class DefaultContactListAdapter extends ContactListAdapter {
    private static final String TAG = "DefaultContactListAdapter";

    public static final char SNIPPET_START_MATCH = '\u0001';
    public static final char SNIPPET_END_MATCH = '\u0001';
    public static final String SNIPPET_ELLIPSIS = "\u2026";
    public static final int SNIPPET_MAX_TOKENS = 5;
    /*
     * Bug Fix by Mediatek Begin.
     *   Original Android's code:
     *     
     *   CR ID: ALPS00112614
     *   Descriptions: only show phone contact if it's from sms
     */
    private boolean onlyShowPhoneContacts = false;
    
    private int mListViewSingleHight;
    private int mListViewDoubleHight;
    private int mListViewHeaderHight;
    
    public boolean isOnlyShowPhoneContacts() {
        return onlyShowPhoneContacts;
    }

    public void setOnlyShowPhoneContacts(boolean onlyShowPhoneContacts) {
        this.onlyShowPhoneContacts = onlyShowPhoneContacts;
    }
    
    /*
     * Bug Fix by Mediatek End.
     */

    public static final String SNIPPET_ARGS = SNIPPET_START_MATCH + "," + SNIPPET_END_MATCH + ","
            + SNIPPET_ELLIPSIS + "," + SNIPPET_MAX_TOKENS;

    public DefaultContactListAdapter(Context context) {
        super(context);
        
        mListViewSingleHight = context.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_singleline_height);
        mListViewDoubleHight = context.getResources().getDimensionPixelOffset(com.aurora.R.dimen.aurora_list_doubleline_height);
        mListViewHeaderHight = context.getResources().getDimensionPixelOffset(R.dimen.aurora_edit_group_margin_top);
    }
    
    private boolean mIsContactSelectMode = false;
    public DefaultContactListAdapter(Context context, boolean flag) {
        super(context);
        mIsContactSelectMode = flag;
    }

    @Override
    public void configureLoader(CursorLoader loader, long directoryId) {
        //aurora <wangth> <2013-9-4> modify for auroro ui begin
        /*
        if (loader instanceof ProfileAndContactsLoader) {
            ((ProfileAndContactsLoader) loader).setLoadProfile(shouldIncludeProfile());
        }
        */
        if (loader instanceof StarredAndContactsLoader) {
            ((StarredAndContactsLoader) loader).setLoadStars(true);
            setStarredMode(true);
        }
        //aurora <wangth> <2013-9-4> modify for auroro ui end

        ContactListFilter filter = getFilter();
        if (isSearchMode()) {
            String query = getQueryString();
            if (query == null) {
                query = "";
            }
            query = query.trim();
            if (TextUtils.isEmpty(query)) {
                // Regardless of the directory, we don't want anything returned,
                // so let's just send a "nothing" query to the local directory.
                loader.setUri(Contacts.CONTENT_URI);
                loader.setProjection(getProjection(false));
                loader.setSelection("0");
            } else {
                Builder builder = Contacts.CONTENT_FILTER_URI.buildUpon();
                builder.appendPath(query);      // Builder will encode the query
                builder.appendQueryParameter(GnContactsContract.DIRECTORY_PARAM_KEY,
                        String.valueOf(directoryId));
                if (directoryId != Directory.DEFAULT && directoryId != Directory.LOCAL_INVISIBLE) {
                    builder.appendQueryParameter(GnContactsContract.LIMIT_PARAM_KEY,
                            String.valueOf(getDirectoryResultLimit()));
                }
                builder.appendQueryParameter(SearchSnippetColumns.SNIPPET_ARGS_PARAM_KEY,
                        SNIPPET_ARGS);
                builder.appendQueryParameter(SearchSnippetColumns.DEFERRED_SNIPPETING_KEY,"1");
                loader.setUri(builder.build());
                loader.setProjection(getProjection(true));
                // aurora <wangth> <2013-9-29> modify for aurora ui begin 
                configureSelection(loader, directoryId, filter);
                // aurora <wangth> <2013-9-29> modify for aurora ui end
            }
        } else {
            configureUri(loader, directoryId, filter);
            loader.setProjection(getProjection(false));
            configureSelection(loader, directoryId, filter);
        }

        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     
         *   CR ID: ALPS00112614
         *   Descriptions: only show phone contact if it's from sms
         */
        if (isOnlyShowPhoneContacts()) {
            configureOnlyShowPhoneContactsSelection(loader, directoryId, filter);
        }
        /*
         * Bug Fix by Mediatek End.
         */
        
        String sortOrder;
        if (getSortOrder() == GnContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            sortOrder = Contacts.SORT_KEY_PRIMARY;
        } else {
            sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
        }

        loader.setSortOrder(sortOrder);
    }

    /*
     * Bug Fix by Mediatek Begin.
     *   Original Android's code:
     *     
     *   CR ID: ALPS00112614
     *   Descriptions: only show phone contact if it's from sms
     */
    private void configureOnlyShowPhoneContactsSelection(CursorLoader loader, long directoryId,
            ContactListFilter filter) {
        if (filter == null) {
            return;
        }

        if (directoryId != Directory.DEFAULT) {
            return;
        }

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();
        
        selection.append(Contacts.INDICATE_PHONE_SIM + "= ?");
        selectionArgs.add("-1");
        
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
    }
    /*
     * Bug Fix by Mediatek End.
     */

    protected void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = Contacts.CONTENT_URI;
        if (filter != null && filter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
            String lookupKey = getSelectedContactLookupKey();
            if (lookupKey != null) {
                uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
            } else {
                uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, getSelectedContactId());
            }
        }

        if (directoryId == Directory.DEFAULT && isSectionHeaderDisplayEnabled()) {
            uri = buildSectionIndexerUri(uri);
        }

        // The "All accounts" filter is the same as the entire contents of Directory.DEFAULT
        if (filter != null
                && filter.filterType != ContactListFilter.FILTER_TYPE_CUSTOM
                && filter.filterType != ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
            uri = uri.buildUpon().appendQueryParameter(
                    GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
                    .build();
        }

        loader.setUri(uri);
    }

    private void configureSelection(
            CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (filter == null) {
            return;
        }

        if (directoryId != Directory.DEFAULT) {
            return;
        }

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        Log.e(TAG, "filter.filterType = " + filter.filterType + "   filter.accountType = " + filter.accountType);
        switch (filter.filterType) {
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
                // We have already added directory=0 to the URI, which takes care of this
                // filter
                break;
            }
            case ContactListFilter.FILTER_TYPE_SINGLE_CONTACT: {
                // We have already added the lookup key to the URI, which takes care of this
                // filter
                break;
            }
            case ContactListFilter.FILTER_TYPE_STARRED: {
                selection.append(Contacts.STARRED + "!=0");
                break;
            }
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY: {
                selection.append(Contacts.HAS_PHONE_NUMBER + "=1");
                break;
            }
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
                selection.append(Contacts.IN_VISIBLE_GROUP + "=1");
                if (isCustomFilterForPhoneNumbersOnly()) {
                    selection.append(" AND " + Contacts.HAS_PHONE_NUMBER + "=1");
                }
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
                // TODO: avoid the use of private API
                if (AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(filter.accountType)) {
                    selection.append("EXISTS ("
                                    + "SELECT DISTINCT " + RawContacts.CONTACT_ID
                                    + " FROM raw_contacts"
                                    + " WHERE ( "
                                    + RawContacts.CONTACT_ID + " = " + "view_contacts."+ Contacts._ID
                                    + " AND (" + RawContacts.ACCOUNT_TYPE + " IS NULL "
                                    + " AND " + RawContacts.ACCOUNT_NAME + " IS NULL "
                                    + " AND " +  RawContacts.DATA_SET + " IS NULL "
                                    + " OR " + RawContacts.ACCOUNT_TYPE + "=? "
                                    + " AND " + RawContacts.ACCOUNT_NAME + "=? ");
                } else {
                    selection.append("EXISTS ("
                                    + "SELECT DISTINCT " + RawContacts.CONTACT_ID
                                    + " FROM raw_contacts"
                                    + " WHERE ( "
                                    + RawContacts.CONTACT_ID + " = " + "view_contacts."+ Contacts._ID
                                    + " AND (" + RawContacts.ACCOUNT_TYPE + "=?"
                                    + " AND " + RawContacts.ACCOUNT_NAME + "=?");
                }
                selectionArgs.add(filter.accountType);
                selectionArgs.add(filter.accountName);
                if (filter.dataSet != null) {
                    selection.append(" AND " + RawContacts.DATA_SET + "=? )");
                    selectionArgs.add(filter.dataSet);
                } else {
                    selection.append(" AND " +  RawContacts.DATA_SET + " IS NULL )");
                }
                selection.append("))");
                break;
            }
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        final RelativeLayout mainUi = (RelativeLayout)itemView.findViewById(com.aurora.R.id.aurora_listview_front);
        final AuroraCheckBox checkBox = (AuroraCheckBox) itemView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
//        checkBox.setVisibility(View.VISIBLE);
        final ContactListItemView view = (ContactListItemView)(mainUi.getChildAt(0));
        auroraBindSectionHeaderAndDivider(itemView, position, cursor);
        
        LinearLayout contentUi = (LinearLayout) itemView.findViewById(com.aurora.R.id.content);
        AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);
        
        // aurora <wangth> <2013-9-25> add for aurora ui begin
        mainUi.removeViewAt(0);
        mainUi.addView(view, 0);
        
        view.auroraSetCheckable(false);
        boolean isSearch = isSearchMode();
        view.setHighlightedPrefix(isSearch ? getUpperCaseQueryString() : null);
        
        if (position >= getStarredCount() || isSearch) {
            if (getCheckBoxEnable()) {
//                view.setCheckable(true);
//                view.setNameTextViewStyle(true, false);

                int contactId = getContactID(position);
                boolean checked = false;
                if (getCheckedItem() != null
                        && getCheckedItem().containsKey(Long.valueOf(contactId))) {
                    checked = true;
                }
                
                if (getNeedAnim()) {
                    AuroraListView.auroraStartCheckBoxAppearingAnim(view.getNameTextView(), checkBox);
                } else {
                    AuroraListView.auroraSetCheckBoxVisible(view.getNameTextView(), checkBox, true);
                }
                checkBox.setChecked(checked);
            } else {
                if (checkBox != null) {
                    if (getNeedAnim()) {
                        AuroraListView.auroraStartCheckBoxDisappearingAnim(view.getNameTextView(), checkBox);
                    } else {
                        AuroraListView.auroraSetCheckBoxVisible(view.getNameTextView(), checkBox, false);
                    }
                }
                view.setCheckable(false);
//                view.setNameTextViewStyle(false, false);
            }
        } else {
            if (checkBox != null) {
                AuroraListView.auroraSetCheckBoxVisible(view.getNameTextView(), checkBox, false);
            }
            view.setCheckable(false);
//            if (getCheckBoxEnable()) {
//                view.setNameTextViewStyle(true, false);
//            } else {
//                view.setNameTextViewStyle(false, false);
//            }
        }
        // aurora <wangth> <2013-9-25> add for aurora ui end
        
        if (isSelectionVisible()) {
            view.setActivated(isSelectedContact(partition, cursor));
        }

        //AURORA-START::remove for aurora ui::remove::wangth::20130902
        /*
        bindSectionHeaderAndDivider(view, position, cursor);
        //Gionee:huangzy 20130131 add for CR00770449 start
        {
	        ImageView photoView = isQuickContactEnabled() ? view.getQuickContact() : view.getPhotoView();
	        ContactPhotoManager.setContactPhotoViewTag(photoView, 
	        		cursor.getString(ContactQuery.CONTACT_DISPLAY_NAME), position, false);
        }
        //Gionee:huangzy 20130131 add for CR00770449 end
        if (isQuickContactEnabled()) {
            bindQuickContact(view, partition, cursor, ContactQuery.CONTACT_PHOTO_ID,
                    ContactQuery.CONTACT_ID, ContactQuery.CONTACT_LOOKUP_KEY);
        } else {
            bindPhoto(view, partition, cursor);
        }
        */
        //AURORA-END::remove for aurora ui::remove::wangth::20130902

        bindName(view, cursor);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int sim_id = getIndicatePhoneSim(position);
            if (sim_id > 0) {
                view.setSimIconable(true, sim_id);
            } else {
                view.setSimIconable(false, sim_id);
            }
        }
        
        if (getIsQueryForDialer()) {
            view.setSnippet(null);
            
			if (position >= getStarredCount() || isSearch) {
				if (getCheckBoxEnable()) {
					view.setNameTextViewStyle(true, false);
				} else {
					view.setNameTextViewStyle(false, false);
				}
			} else {
				if (getCheckBoxEnable()) {
	                view.setNameTextViewStyle(true, false);
	            } else {
	                view.setNameTextViewStyle(false, false);
	            }
			}
            
            if (mIsContactSelectMode) {
                return;
            }

            int high = mListViewSingleHight;
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            if (null != lp) {
                lp.height = high + 1;
                itemView.setLayoutParams(lp);
                contentUi.setAlpha(255);
            }
            
            return;
        }
        // aurora <wangth> <2014-1-25> remove for aurora begin
        //bindPresenceAndStatusMessage(view, cursor);
        // aurora <wangth> <2014-1-25> remove for aurora end
        
//        bindTag(view, cursor);
        
        if (isSearchMode()) {
            bindSearchSnippet(view, cursor);
        } else {
            view.setSnippet(null);
        }
        
        if (position >= getStarredCount() || isSearch) {
			if (getCheckBoxEnable()) {
				view.setNameTextViewStyle(true, false);
			} else {
				view.setNameTextViewStyle(false, false);
			}
		} else {
			if (getCheckBoxEnable()) {
                view.setNameTextViewStyle(true, false);
            } else {
                view.setNameTextViewStyle(false, false);
            }
		}
        
        if (mIsContactSelectMode) {
            return;
        }
        
        if (getNeedAnim()) {
            mHandler.sendMessage(mHandler.obtainMessage());
        }
        
        if (mAuroraListDelet) {
            ViewGroup.LayoutParams lp = itemView.getLayoutParams(); 
            
            int high = mListViewSingleHight;
            Placement placement = getItemPlacementInSection(position);
            if (isSectionHeaderDisplayEnabled() && placement.sectionHeader != null) {
                high = mListViewSingleHight + mListViewHeaderHight;
                high++;
            }
            
            if(null != lp) {
                lp.height = high + 1;
                itemView.setLayoutParams(lp);
                contentUi.setAlpha(255);
            }
        } else {
            if (isSearchMode()) {
                LinearLayout headerUi = (LinearLayout) view
                        .findViewById(com.aurora.R.id.aurora_list_header);
                if (headerUi != null) {
                    headerUi.setVisibility(View.GONE);
                }
                
                int high = mListViewSingleHight;
                if (view.isDataViewVisible() || view.isSnippetViewVisible()) {
                	high = mListViewDoubleHight;
                }
                
                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                if(null != lp) {
                    lp.height = high + 1;
                    
                    itemView.setLayoutParams(lp);
                    contentUi.setAlpha(255);
                }
            }
        }
        
        if (getCheckBoxEnable()) {
        	if (view.mSnippetView != null && view.isVisible(view.getSnippetView())) {
            	AuroraListView.auroraSetCheckBoxVisible(view.getSnippetView(), checkBox, true);
            }
        } else {
        	if (view.mSnippetView != null && view.isVisible(view.getSnippetView())) {
            	AuroraListView.auroraSetCheckBoxVisible(view.getSnippetView(), checkBox, false);
            }
        }
    }
    
    public static class GnContactInfo {
    	public String mName;
    	public String mLookupKey;
    	public boolean mIsUserProfile;
    	public int mIndicatePhoneSim;
    	public int mIndexInSim;
    	
    	private GnContactInfo() {
    	}
    	
    	public static GnContactInfo form(Cursor cursor) {
    		GnContactInfo info = new GnContactInfo();
    		
    		info.mName = cursor.getString(ContactQuery.CONTACT_DISPLAY_NAME);        
    		info.mLookupKey = cursor.getString(ContactQuery.CONTACT_LOOKUP_KEY);
    		info.mIsUserProfile = cursor.getInt(ContactQuery.CONTACT_IS_USER_PROFILE) == 1;
    		info.mIndicatePhoneSim = cursor.getInt(ContactQuery.CONTACT_INDICATE_PHONE_SIM);
    		info.mIndexInSim = cursor.getInt(ContactQuery.CONTACT_INDEX_IN_SIM);
    		
    		return info;
    	}
    }

    private void bindTag(ContactListItemView view, Cursor cursor) {
    	GnContactInfo info = GnContactInfo.form(cursor);
    	view.setTag(info);
	}

	private boolean isCustomFilterForPhoneNumbersOnly() {
        // TODO: this flag should not be stored in shared prefs.  It needs to be in the db.
        SharedPreferences prefs = AuroraPreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getBoolean(ContactsPreferences.PREF_DISPLAY_ONLY_PHONES,
                ContactsPreferences.PREF_DISPLAY_ONLY_PHONES_DEFAULT);
    }
}
