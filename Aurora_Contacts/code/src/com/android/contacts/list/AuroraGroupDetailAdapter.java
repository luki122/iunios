package com.android.contacts.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.SearchSnippetColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.DefaultContactListAdapter;
import com.android.contacts.list.ProfileAndContactsLoader;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.model.AccountType;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

public class AuroraGroupDetailAdapter extends ContactListAdapter {

    private CursorLoader mLoader = null;
    private static final String TAG = "liyang-AuroraGroupDetailAdapter";
    public static final char SNIPPET_START_MATCH = '\u0001';
    public static final char SNIPPET_END_MATCH = '\u0001';
    public static final String SNIPPET_ELLIPSIS = "\u2026";
    public static final int SNIPPET_MAX_TOKENS = 5;
    public static final String SNIPPET_ARGS = SNIPPET_START_MATCH + ","
            + SNIPPET_END_MATCH + "," + SNIPPET_ELLIPSIS + ","
            + SNIPPET_MAX_TOKENS;
    
    boolean mIsPrivacyMode = false;

    public AuroraGroupDetailAdapter(Context context) {
        super(context);
    }
    
    public void setPrivacyMode(boolean flag) {
    	mIsPrivacyMode = flag;
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor,
            int position) {
    	Log.d(TAG,"bindview:"+cursor+" count:"+cursor.getCount());
        final RelativeLayout mainUi = (RelativeLayout)itemView.findViewById(com.aurora.R.id.aurora_listview_front);
        final AuroraCheckBox checkBox = (AuroraCheckBox) itemView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        final ContactListItemView view = (ContactListItemView)(mainUi.getChildAt(0));
        
//        int count = cursor.getCount();
//        if (count < 15) {
            mainUi.removeViewAt(0);
            mainUi.addView(view, 0);
//        }
            
        LinearLayout contentUi = (LinearLayout) itemView.findViewById(com.aurora.R.id.content);
//        AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);
        
        boolean isGroupDetaiFragment = isGroupDetailFragment();
//        if (isGroupDetaiFragment) {
//        	Log.d(TAG,"isGroupDetaiFragment");
//            checkBox = (AuroraCheckBox) itemView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
//        }
        
        view.setHighlightedPrefix(isSearchMode() ? getUpperCaseQueryString()
                : null);

        if (isSelectionVisible()) {
//            view.setActivated(isSelectedContact(partition, cursor));
        }

        auroraBindSectionHeaderAndDivider(itemView, position, cursor);
        view.auroraSetCheckable(false);
        
        if (getCheckBoxEnable()) {
        	Log.d(TAG, "bindview11");
            if (!isGroupDetaiFragment) {
//                view.setCheckable(false);
            	
            }


//            view.setNameTextViewStyle(true, false);

            int contactId = getContactID(position);
            boolean checked = false;
            if (getCheckedItem() != null
                    && getCheckedItem().containsKey(Long.valueOf(contactId))) {
                checked = true;
            }
            
            if (getNeedAnim()) {
                AuroraListView.auroraStartCheckBoxAppearingAnim(mainUi, checkBox);
            } else {
            	Log.d(TAG, "mainUi:"+mainUi+" checkBox:"+checkBox);
                AuroraListView.auroraSetCheckBoxVisible(mainUi, checkBox, true);
            }            
            checkBox.setChecked(checked);

            /*if (!isGroupDetaiFragment) {
//            	Log.d(TAG, "bindview22");
//                view.getCheckBox().setChecked(checked);
            	Log.d(TAG, "mainUi:"+mainUi+" checkBox:"+checkBox);
                AuroraListView.auroraSetCheckBoxVisible(mainUi, checkBox, true);
            } else {
                if (getNeedAnim()) {
                    AuroraListView.auroraStartCheckBoxAppearingAnim(mainUi, checkBox);
                } else {
                	Log.d(TAG, "mainUi:"+mainUi+" checkBox:"+checkBox);
                    AuroraListView.auroraSetCheckBoxVisible(mainUi, checkBox, true);
                }
                checkBox.setChecked(checked);
            }*/
        } else {
            if (checkBox != null) {
                if (getNeedAnim()) {
                	Log.d(TAG, "bindview44");
                    AuroraListView.auroraStartCheckBoxDisappearingAnim(mainUi, checkBox);
                } else {
                	Log.d(TAG, "bindview33");
                    AuroraListView.auroraSetCheckBoxVisible(mainUi, checkBox, false);
                }
            }
            view.setCheckable(false);
//            view.setNameTextViewStyle(false, false);
        }

//        view.removePhotoView();

        Log.d(TAG, "bindview1");
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
        
        Log.d(TAG, "bindview2");
        bindName(view, cursor);
        if (getIsQueryForDialer()) {
            view.setSnippet(null);
            if (getCheckBoxEnable()) {
                view.setNameTextViewStyle(true, false);
            } else {
                view.setNameTextViewStyle(false, false);
            }
            
            return;
        }
        bindPresenceAndStatusMessage(view, cursor);

        Log.d(TAG, "bindview3");
        if (isSearchMode()) {
            bindSearchSnippet(view, cursor);
        } else {
            view.setSnippet(null);
        }
        
        if (getCheckBoxEnable()) {
            view.setNameTextViewStyle(true, false);
        } else {
            view.setNameTextViewStyle(false, false);
        }
        
        if (getNeedAnim() && isGroupDetaiFragment) {
            mHandler.sendMessage(mHandler.obtainMessage());
        }
    }

    @Override
    public void configureLoader(CursorLoader loader, long directoryId) {
        mLoader = loader;

        ContactListFilter filter = getFilter();
        if (isSearchMode()) {
            String query = getQueryString();
            if (query == null) {
                query = "";
            }
            query = query.trim();
            if (TextUtils.isEmpty(query)) {
                loader.setUri(Contacts.CONTENT_URI);
                loader.setProjection(getProjection(false));
                loader.setSelection("0");
            } else {
                Builder builder = Contacts.CONTENT_FILTER_URI.buildUpon();
                builder.appendPath(query); // Builder will encode the query
                builder.appendQueryParameter(
                        GnContactsContract.DIRECTORY_PARAM_KEY,
                        String.valueOf(directoryId));
                if (directoryId != Directory.DEFAULT
                        && directoryId != Directory.LOCAL_INVISIBLE) {
                    builder.appendQueryParameter(
                            GnContactsContract.LIMIT_PARAM_KEY,
                            String.valueOf(getDirectoryResultLimit()));
                }
                builder.appendQueryParameter(
                        SearchSnippetColumns.SNIPPET_ARGS_PARAM_KEY,
                        SNIPPET_ARGS);
                builder.appendQueryParameter(
                        SearchSnippetColumns.DEFERRED_SNIPPETING_KEY, "1");
                loader.setUri(builder.build());
                loader.setProjection(getProjection(true));
                configureSelection(loader, directoryId, filter);
            }
        } else {
            configureUri(loader, directoryId, filter);
            loader.setProjection(getProjection(false));
            configureSelection(loader, directoryId, filter);
        }

        String sortOrder;
        if (getSortOrder() == GnContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            sortOrder = Contacts.SORT_KEY_PRIMARY;
        } else {
            sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
        }

        loader.setSortOrder(sortOrder);
    }

    protected void configureUri(CursorLoader loader, long directoryId,
            ContactListFilter filter) {
        Uri uri = Contacts.CONTENT_URI;

        if (directoryId == Directory.DEFAULT && isSectionHeaderDisplayEnabled()) {
            uri = buildSectionIndexerUri(uri);
        }

        // The "All accounts" filter is the same as the entire contents of
        // Directory.DEFAULT
        if (filter != null
                && filter.filterType != ContactListFilter.FILTER_TYPE_CUSTOM
                && filter.filterType != ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
            uri = uri
                    .buildUpon()
                    .appendQueryParameter(GnContactsContract.DIRECTORY_PARAM_KEY,
                            String.valueOf(Directory.DEFAULT)).build();
        }

        loader.setUri(uri);
    }

    private void configureSelection(CursorLoader loader, long directoryId,
            ContactListFilter filter) {
        if (directoryId != Directory.DEFAULT) {
            return;
        }

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        String appendSelection = praseExInfo();
        if (null != appendSelection) {
            selection.append(appendSelection);
        }

        loader.setSelection(selection.toString());
    }

    private String praseExInfo() {
    	if (mIsPrivacyMode) {
    		String sel = "( name_raw_contact_id IN ( "
            + "SELECT raw_contact_id FROM view_data" + " WHERE ("
            + "is_privacy=0 AND "
            + RawContacts.ACCOUNT_NAME + " = '"
            + AccountType.ACCOUNT_NAME_LOCAL_PHONE
            + "' AND " + RawContacts.ACCOUNT_TYPE + " = '"
            + AccountType.ACCOUNT_TYPE_LOCAL_PHONE +  "')))";
    		return sel;
    	}
    	
        String exInfo = getFilterExInfo();

        if (null == exInfo) {
            return null;
        }

        StringBuilder selection = new StringBuilder();

        if (exInfo.startsWith("withGroupId/")) {
            exInfo = exInfo.replace("withGroupId/", "");
            selection.append("( name_raw_contact_id IN ( "
                    + "SELECT raw_contact_id FROM view_data" + " WHERE ("
                    + Data.MIMETYPE + " = '"
                    + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                    + "' AND data1=" + exInfo + ")))");
        } else if (exInfo.startsWith("noGroupId/")) {
            exInfo = exInfo.replace("noGroupId/", "");
            if (exInfo.isEmpty()) {
                selection.append("( name_raw_contact_id  NOT IN ( "
                        + "SELECT raw_contact_id FROM view_data" + " WHERE ("
                        + Data.MIMETYPE + " = '"
                        + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                        + "' AND data1 is not null"
                        + " or " + RawContacts.INDICATE_PHONE_SIM + " >= 0 "
                        + ")))");
            } else {
                ContactListFilter filter = getFilter();
                Log.d("AuroraGroupDetailAdapter", "filter.name = " + filter.accountName + "  filter.accountType = " + filter.accountType);
                if (filter.accountName != null && filter.accountType != null) {
                    selection.append("( name_raw_contact_id IN ( "
                            + "SELECT raw_contact_id FROM view_data" + " WHERE ("
                            + RawContacts.ACCOUNT_NAME + " = '"
                            + filter.accountName
                            + "' AND " + RawContacts.ACCOUNT_TYPE + " = '"
                            + filter.accountType + "')) AND name_raw_contact_id NOT IN ( "
                            + "SELECT raw_contact_id FROM view_data" + " WHERE ("
                            + Data.MIMETYPE + " = '"
                            + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                            + "' AND data1=" + exInfo
                            + ")))");
                    
                } else {
                    selection.append("( name_raw_contact_id  NOT IN ( "
                            + "SELECT raw_contact_id FROM view_data" + " WHERE ("
                            + Data.MIMETYPE + " = '"
                            + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                            + "' AND data1=" + exInfo
                            + " or " + RawContacts.INDICATE_PHONE_SIM + " >= 0 "
                            + ")))");
                }
            }
            
        } else if (exInfo.startsWith("exportSim/")) {
            selection.append("( name_raw_contact_id IN ( "
                    + "SELECT name_raw_contact_id FROM view_contacts" + " WHERE ("
                    + " has_phone_number = 1 )))");
        }

        return selection.toString();
    }
    
    private boolean isGroupDetailFragment() {
        String exInfo = getFilterExInfo();
        
        if (null != exInfo && exInfo.startsWith("withGroupId/")) {
            return true;
        } else if (null != exInfo && exInfo.startsWith("noGroupId/")) {
            exInfo = exInfo.replace("noGroupId/", "");
            if (exInfo.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
        
        return false;
    }
}
