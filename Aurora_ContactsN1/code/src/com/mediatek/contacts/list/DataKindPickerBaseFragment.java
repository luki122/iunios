
package com.mediatek.contacts.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.util.AccountFilterUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

import aurora.app.AuroraProgressDialog;

public abstract class DataKindPickerBaseFragment extends
        ContactEntryListFragment<ContactEntryListAdapter> implements ContactListMultiChoiceListener {

    private final String TAG = DataKindPickerBaseFragment.class.getSimpleName();

    private static final String resultIntentExtraName = "com.mediatek.contacts.list.pickdataresult";
    private static final String KEY_CHECKEDIDS = "checkedids";
    private static final String KEY_CHECKEDSTATES = "checkedstates";

    private String mSlectedItemsFormater = null;

    //private ArrayList<Long> mCheckedItemsList = new ArrayList<Long>();
    private HashMap<Long, Boolean> mCheckedItemsMap = new HashMap<Long, Boolean>();
    private String mSearchString;

    // Show account filter settings
    private View mAccountFilterHeader;

    private TextView mEmptyView = null;
    private LinearLayout mContactList = null;

    // gionee xuhz 20120515 add start
    private View mGnEmptyView = null;
    private TextView mGnEmptyTextView = null;
    // gionee xuhz 20120515 add end
    
    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {

        return inflater.inflate(R.layout.gn_multichoice_contact_list, null);
   
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
  
	    gnOnCreateView(inflater, container);
 
   
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSlectedItemsFormater = getActivity().getString(R.string.menu_actionbar_selected_items);
        updateSelectedItemsView();

        this.getListView().setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void configureAdapter() {
        ContactEntryListAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }

        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setEmptyListEnabled(true);
        //adapter.setSearchMode(false);
        adapter.setIncludeProfile(false);
        // Show A-Z section index.
        adapter.setSectionHeaderDisplayEnabled(true);
        // Disable pinned header. It doesn't work with this fragment.
        adapter.setPinnedPartitionHeadersEnabled(false);
        super.setPhotoLoaderEnabled(true);
        adapter.setQueryString(mSearchString);
        if (mAccountFilterHeader != null) {
            final TextView headerTextView = (TextView) mAccountFilterHeader.findViewById(
                    R.id.account_filter_header);
            if (headerTextView != null) {
                headerTextView.setText(R.string.contact_list_loading);
                mAccountFilterHeader.setVisibility(View.VISIBLE);
            }
        }

   
        mGnEmptyView.setVisibility(View.GONE);
 

        mContactList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick with adapterView");
        if (getListView().getCheckedItemCount() > 5000) {
            Toast.makeText(getActivity(), R.string.multichoice_contacts_limit, Toast.LENGTH_SHORT).show();
            getListView().setItemChecked(position, false);
            return;
        }

        super.onItemClick(parent, view, position, id);

        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();
        mCheckedItemsMap.put(Long.valueOf(id), getListView().isItemChecked(position));

        updateSelectedItemsView(getListView().getCheckedItemCount());
    }

    @Override
    public void onClearSelect() {
        updateListCheckBoxeState(false);
    }

    @Override
    public void onOptionAction() {

        Activity activity = getActivity();

        int selectedCount = this.getListView().getCheckedItemCount();

        if (selectedCount == 0) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
        }

        final Intent retIntent = new Intent();
        if (null == retIntent) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        int curArray = 0;
        long[] idArray = new long[selectedCount];
        if (null == idArray) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) this.getAdapter();
        int itemCount = getListView().getCount();
        for (int position = 0; position < itemCount; ++position) {
            if (getListView().isItemChecked(position)) {
                idArray[curArray++] = adapter.getDataId(position);
                if (curArray > selectedCount) {
                    break;
                }
            }
        }

        for (long item:idArray) {
            Log.d(TAG, "result array: item " + item);
        }
        retIntent.putExtra(resultIntentExtraName, idArray);
        activity.setResult(Activity.RESULT_OK, retIntent);
        activity.finish();
    }

    @Override
    public void onSelectAll() {
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     updateListCheckBoxeState(true);
         *   CR ID: ALPS00247750
         *   Descriptions: add progessdialog when it's busy 
         */
        final AuroraProgressDialog progressDialog = AuroraProgressDialog.show(getActivity(),
                getString(R.string.please_wait),
                getString(R.string.upgrade_in_progress), true, false);
        progressDialog.show();
        Log.i(TAG,"onSelectAll+");
        updateListCheckBoxeState(true);
        Log.i(TAG,"onSelectAll-");
        progressDialog.dismiss();
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private void updateListCheckBoxeState(boolean checked) {
        final DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();
        final int count = getListView().getAdapter().getCount();
        long dataId = -1;
        for (int position = 0; position < count; ++position) {
            if (checked) {
                if (getListView().getCheckedItemCount() >= 5000) {
                    Toast.makeText(getActivity(), R.string.multichoice_contacts_limit, Toast.LENGTH_SHORT)
                            .show();
                    break;
                }
                getListView().setItemChecked(position, checked);
                dataId = adapter.getDataId(position);
                mCheckedItemsMap.put(Long.valueOf(dataId), checked);
            } else {
                if (getListView().isItemChecked(position)) {
                    getListView().setItemChecked(position, checked);
                    dataId = adapter.getDataId(position);
                    mCheckedItemsMap.put(Long.valueOf(dataId), checked);
                }
            }
        }

        updateSelectedItemsView(getListView().getCheckedItemCount());
    }

    @Override
    protected void onItemClick(int position, long id) {
        return;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        
    	gnOnLoadFinished(loader, data);

    }

    @Override
    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);

        if (savedState == null) {
            return;
        }

        long ids[] = savedState.getLongArray(KEY_CHECKEDIDS);
        boolean[] states = savedState.getBooleanArray(KEY_CHECKEDSTATES);
        if (mCheckedItemsMap == null) {
            mCheckedItemsMap = new HashMap<Long, Boolean>();
        }
        if (ids.length != states.length) {
            return;
        }
        mCheckedItemsMap.clear();
        int checkedStatesSize = ids.length;
        for (int index = 0; index < checkedStatesSize; ++index) {
            mCheckedItemsMap.put(Long.valueOf(ids[index]), states[index]);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int CheckedItemsCount = mCheckedItemsMap.size();
        long[] checkedIds = new long[CheckedItemsCount];
        int index = 0;
        Set<Long> ids = mCheckedItemsMap.keySet();
        for (Long id : ids) {
            checkedIds[index++] = id;
        }
        outState.putLongArray(KEY_CHECKEDIDS, checkedIds);

        boolean[] checkedStates = new boolean[CheckedItemsCount];
        Collection<Boolean> states = mCheckedItemsMap.values();
        index = 0;
        for (Boolean state : states) {
            checkedStates[index++] = state;
        }
        outState.putBooleanArray(KEY_CHECKEDSTATES, checkedStates);
    }

    public void startSearch(String searchString) {
        // It could not meet the layout Req. So, we should not use the default search function

        // Normalize the empty query.
        if (TextUtils.isEmpty(searchString)) {
            searchString = null;
        }

        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();
        if (searchString == null) {
            if (adapter != null) {
                mSearchString = null;
                adapter.setQueryString(searchString);
                adapter.setSearchMode(false);
                reloadData();
            }
        } else if (!TextUtils.equals(mSearchString, searchString)) {
            mSearchString = searchString;
            if (adapter != null) {
                adapter.setQueryString(searchString);
                adapter.setSearchMode(true);
                reloadData();
            }
        }
    }

    private void updateSelectedItemsView(int checkedItemsCount) {
        if (getAdapter().isSearchMode()) {
            return;
        }

        TextView selectedItemsView = (TextView) getActivity().getActionBar().getCustomView()
                .findViewById(R.id.select_items);
        if (selectedItemsView == null) {
            Log.e(TAG, "Load view resource error!");
            return;
        }
        if (mSlectedItemsFormater == null) {
            Log.e(TAG, "Load string resource error!");
            return;
        }

        selectedItemsView.setText(String.format(mSlectedItemsFormater, String
                .valueOf(checkedItemsCount)));
    }

    public void updateSelectedItemsView() {

        final DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();

        int checkedItemsCount = 0;
        long dataId = -1;
        int count = getListView().getAdapter().getCount();
        for (int position = 0; position < count; ++position) {
            dataId = -1;
            dataId = adapter.getDataId(position);
            if (mCheckedItemsMap.containsKey(Long.valueOf(dataId))
                    && mCheckedItemsMap.get(Long.valueOf(dataId))) {
                ++checkedItemsCount;
            }
        }

        updateSelectedItemsView(checkedItemsCount);
    }
    
    // gionee xuhz 20120515 add start
    protected void gnOnCreateView(LayoutInflater inflater, ViewGroup container) {
        mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
        mAccountFilterHeader.setClickable(false);
        mAccountFilterHeader.setVisibility(View.GONE);

        mGnEmptyView = (View) getView().findViewById(R.id.empty);
        mGnEmptyTextView = (TextView) getView().findViewById(R.id.contact_list_empty);
        if (mGnEmptyTextView != null) {
        	mGnEmptyTextView.setText(R.string.noContacts);
        }
        mContactList = (LinearLayout) getView().findViewById(R.id.contact_list);
    }
    
    public void gnOnLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mGnEmptyTextView != null) {
            if (getAdapter().isSearchMode()) {
            	mGnEmptyTextView.setText(R.string.listFoundAllContactsZero);
            } else {
            	mGnEmptyTextView.setText(R.string.noContacts);
            }
        }
        if (data == null || (data != null && data.getCount() == 0)) {
        	mGnEmptyView.setVisibility(View.VISIBLE);
            mContactList.setVisibility(View.GONE);
        } else {
        	mGnEmptyView.setVisibility(View.GONE);
            mContactList.setVisibility(View.VISIBLE);
        }

        super.onLoadFinished(loader, data);
        DataKindPickerBaseAdapter adapter = (DataKindPickerBaseAdapter) getAdapter();

        // clear list view choices
        getListView().clearChoices();

        int checkedItemsCount = 0;
        long dataId = -1;
        int count = getListView().getAdapter().getCount();
        for (int position = 0; position < count; ++position) {
            dataId = -1;
            dataId = adapter.getDataId(position);

            if (mCheckedItemsMap.containsKey(Long.valueOf(dataId))) {
                boolean checked = mCheckedItemsMap.get(Long.valueOf(dataId));
                getListView().setItemChecked(position, checked);
                if (checked) {
                    ++checkedItemsCount;
                }
            } else {
                getListView().setItemChecked(position, false);
                mCheckedItemsMap.put(Long.valueOf(dataId), false);
            }
        }
        updateSelectedItemsView(checkedItemsCount);
        final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPeople(
                mAccountFilterHeader, ContactListFilter
                        .createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS), false,
                true);
    }
    // gionee xuhz 20120515 add end
}
