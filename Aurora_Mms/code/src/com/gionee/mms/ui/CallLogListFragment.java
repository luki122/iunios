/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;

import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.ui.AddReceiptorTab.ContactsPicker;
import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;

import android.app.Fragment;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract.ProviderStatus;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.AbsListView.OnScrollListener;
import aurora.app.AuroraActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CallLogListFragment extends Fragment implements ContactsPicker{
    AuroraActivity mActivity;
    private static final String LIST_STATE_KEY = "liststate";
    boolean mJustCreated = false;
    private AuroraListView mList;
    private LinearLayout mEmptyView;

    private AuroraButton mSelAllButton;
    private ContactsListArrayAdapter mAdapter;

    private static final int MENU_SELECT_CANCEL = 0;
    private static final int MENU_SELECT_DONE   = 1;
    private static final int MENU_SELECT_ALL    = 2;

    private Parcelable mListState = null;
    private AdapterView.OnItemClickListener mContactListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            mAdapter.checkBoxClicked(position, view);

            int checkCount = mAdapter.getCheckedCount();
            updateSelectDoneText();

        }
    };

    public CallLogListFragment(AuroraActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
        mJustCreated = true;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View fragmentView = inflater.inflate(R.layout.gn_calllog_select_list_layout, container, false);
        mList = (AuroraListView) fragmentView.findViewById(R.id.list);
        mEmptyView = (LinearLayout) fragmentView.findViewById(R.id.gn_calllog_empty);
        return fragmentView;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mList.setOnScrollListener(mAdapter);

        mAdapter = new ContactsListArrayAdapter(getActivity());

        // We manually save/restore the listview state
        mList.setSaveEnabled(false);
        mList.setOnItemClickListener(mContactListClickListener);

        if (ContactsCacheSingleton.getInstance().getCalllogContactCount() != 0) {

            mList.setVisibility(View.VISIBLE);

            mEmptyView.setVisibility(View.GONE);

            if (mListState != null) {
                mList.onRestoreInstanceState(mListState);
                mListState = null;
            }

            for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getCallLogList()) {
                    mAdapter.add(cache);
            }
            mList.setAdapter(mAdapter);

//            updateSelectDoneText();
        } else {
            mList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.gn_add_receiptor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if(mEmptyView.getVisibility() == View.VISIBLE) {
            menu.getItem(MENU_SELECT_ALL).setTitle(R.string.select_all);
            menu.getItem(MENU_SELECT_ALL).setEnabled(false);
        } else if(isAllChecked()) {
            menu.getItem(MENU_SELECT_ALL).setTitle(R.string.unselect_all);
        } else {
            menu.getItem(MENU_SELECT_ALL).setTitle(R.string.select_all);
        }
        int checkedCount = ContactsCacheSingleton.getInstance().getCheckedCount();
        if(checkedCount == 0) {
            menu.getItem(MENU_SELECT_DONE).setTitle(R.string.gn_selected);
            menu.getItem(MENU_SELECT_DONE).setEnabled(false);
        } else {
            menu.getItem(MENU_SELECT_DONE).setTitle(getString(R.string.gn_selected) + "(" + checkedCount + ")");
        }
        super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_add_select_all:
            mAdapter.setAllItemCheckState(item.getTitle().toString().equals(getString(R.string.select_all)));
            updateSelectDoneText();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("MMS", "CallLogListFragment...........onResume");

        registerProviderStatusObserver();

        if (mJustCreated) {
            mJustCreated = false;
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(icicle);
        // Save list state in the bundle so we can restore it after the
        // QueryHandler has run
        if (mList != null) {
            icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterProviderStatusObserver();
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mAdapter.getPhotoLoader().stop();
    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void registerProviderStatusObserver() {
        getActivity().getContentResolver().registerContentObserver(ProviderStatus.CONTENT_URI, false,
                mProviderStatusObserver);
    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void unregisterProviderStatusObserver() {
        getActivity().getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }
    

    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            // checkProviderState(true);
        }
    };

    private final class ContactsListArrayAdapter extends
            ArrayAdapter<ContactsCacheSingleton.ContactListItemCache> implements OnScrollListener {
        private boolean mDisplayPhotos = true;
        private ContactPhotoLoader mPhotoLoader;
        public ContactPhotoLoader getPhotoLoader() {
            return mPhotoLoader;
        }

        public void bindView(View itemView, Context context, int position) {
            final ContactListItemView view = (ContactListItemView) itemView;
            final ContactsCacheSingleton.ContactListItemCache cache = getItem(position);
            view.getCheckBox().setChecked(cache.mIsChecked);
            // Aurora xuyong 2014-07-19 modified for sougou start
            String area = MessageUtils.getNumAreaFromAora(context, cache.mNumber);
            // Aurora xuyong 2014-07-19 modified for sougou end
            if (cache.mNames.isEmpty()){
                view.getNameTextView().setText(cache.mNumber);

                if (area == null || "".equals(area)) {
                    view.getDataView().setVisibility(View.GONE);
                } else {
                    view.getDataView().setText(area);
                }
            }else{
                view.getNameTextView().setText(cache.getNameString());
                if (area == null || "".equals(area)) {
                    view.getDataView().setText(cache.mNumber);
                } else {
                    view.getDataView().setText(cache.mNumber + "  " + area);
                }
            }

        }

        public void checkBoxClicked(int position, View convertView) {
            final ContactListItemView view = (ContactListItemView) convertView;

            view.getCheckBox().setChecked(!view.getCheckBox().isChecked());
            getItem(position).mIsChecked = view.getCheckBox().isChecked();
        }

        public int getCheckedCount() {
            int checkedCount = 0;
            for (int i = 0; i < getCount(); i++) {
                if (getItem(i).mIsChecked) {
                    checkedCount++;
                }
            }
            return checkedCount;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v;
            if (convertView == null) {
                final ContactListItemView view = new ContactListItemView(getContext(), null);
                v = (View) view;
            } else {
                v = convertView;
            }
            bindView(v, getContext(), position);

            return v;
        }

        public ContactsListArrayAdapter(Context context) {
            super(context, 0);
            mPhotoLoader = new ContactPhotoLoader(context, -1);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (view instanceof PinnedHeaderListView) {
                ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
            }

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mPhotoLoader.pause();
            } else if (this.mDisplayPhotos) {
                mPhotoLoader.resume();
            }
        }

        public void setAllItemCheckState(boolean isCheck) {

            // TODO: user may check several checkBox first, and then click the
            // mark all menu item
            // In this situation, may need to do some special work
            for (int index = 0; index < getCount(); index++) {
                ContactsCacheSingleton.ContactListItemCache contact = getItem(index);
                contact.mIsChecked = isCheck;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void updateSelectDoneText() {
        mAdapter.notifyDataSetChanged();
        
    }
    
    private boolean isAllChecked(){
        boolean isAllChecked = true;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            ContactsCacheSingleton.ContactListItemCache cache = mAdapter.getItem(i);
            if (cache.mIsChecked == false) {
                isAllChecked = false;
                break;
            }
        }
        return isAllChecked;
    }

    @Override
    public void markAll() {
        // TODO Auto-generated method stub
        mAdapter.setAllItemCheckState(true);
    }

    @Override
    public void updateButton() {
        updateSelectDoneText();
    }
}
