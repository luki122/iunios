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

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Data;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnCloseButtonClickListener;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.activities.AuroraCallRecordContactListActivity;
import com.android.contacts.activities.AuroraGroupDetailActivity;
import com.android.contacts.activities.AuroraSimContactListActivity;
import com.android.contacts.list.AuroraSimContactListAdapter.PhoneQuery;
import com.android.contacts.widget.AbsListIndexer;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;

import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import android.content.SharedPreferences;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraListView;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraCheckBox;

public class AuroraCallRecordContactListFragment extends
        ContactEntryListFragment<ContactEntryListAdapter> {
    private static final String TAG = "AuroraCallRecordContactListFragment";
    private Context mContext;
    
    public AuroraSimContactListAdapter mAdapter;
    
    private boolean mIsImport = false;
    public boolean mSearchViewHasFocus = false;
    
    private View mLoadingContainer;
    private TextView mLoadingContact;
    private ProgressBar mProgress;
    
    public static boolean isFinished = false;
    private static final int WAIT_CURSOR_START = 1230;
    private static final long WAIT_CURSOR_DELAY_TIME = 500;
    
    private ContactListFilter mFilter;
    private String mFilterExInfo;

    private RelativeLayout mGotoSearchLayout;
    private AbsListIndexer mAlphbetIndexView;
    private AuroraSearchView mSearchView;
    private View mNoContactsEmptyView;
    
    private boolean mIsAuroraSearchMode = false;
    private boolean mIsNeedContextMenu = true;
    private boolean mIsRemoveMemberMode = false;
    private static boolean mNeedCreateDialerTempTable = true;
    public int mItemCount = 0;
    
    private SharedPreferences mPrefs;
    private boolean mIsCustomFilter = false;
    
    public void AuroraCallRecordContactListFragment() {
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(getActivity());
//        int filterInt = mPrefs.getInt("filter.type", -1);
//        if (filterInt == -3) {
//            mIsCustomFilter = true;
//        }
//
//        Bundle extras = getActivity().getIntent().getExtras();
//        if (null != extras) {
//            mFilter = extras
//                    .getParcelable(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER);
//            mFilterExInfo = extras
//                    .getString(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO);
//        }
        mFilterExInfo = "auto_record/";
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        
        if (getListView() != null) {
            try {
                getListView().auroraOnPause();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.aurora_group_detail_list_content, null);
    }
    
    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);

        mAlphbetIndexView = (AbsListIndexer) getView().findViewById(
                R.id.gn_alphbet_indexer);

        mLoadingContainer = getView().findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact = (TextView) getView().findViewById(
                R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) getView().findViewById(
                R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                WAIT_CURSOR_DELAY_TIME);
        
//        mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
//        mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                ((AuroraActivity) getActivity()).showSearchviewLayout();
//                mSearchView = ((AuroraActivity) getActivity()).getSearchView();
//                mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
//                initButtomBar(false);
//                
//                int count = 0;
//                if (null != mAdapter.getCheckedItem()) {
//                    count += mAdapter.getCheckedItem().size();
//                }
//                if (null != mAdapter.getMmsSelectList()) {
//                    count += mAdapter.getMmsSelectList().size();
//                }
//                
//                ((AuroraCallRecordContactListActivity)getActivity()).setRightBtnTv(count);
//                ((AuroraActivity) getActivity()).setOnQueryTextListener(new svQueryTextListener());
//                
//                mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View view, boolean hasFocus) {
//                        mSearchViewHasFocus = hasFocus;
//                        if (hasFocus) {
//                            if (mNeedCreateDialerTempTable) {
//                                auroraInitQueryDialerABC();
//                                mNeedCreateDialerTempTable = false;
//                            }
//                        }
//                    }
//                });
//                setSearchView(mSearchView);
//                getListView().auroraSetNeedSlideDelete(false);
//            }
//            
//        });
//        getListView().addHeaderView(mGotoSearchLayout);

        getListView().setFastScrollEnabled(false);
        getListView().setFastScrollAlwaysVisible(false);
        getListView().setOnCreateContextMenuListener(this);
        getListView().auroraSetAuroraBackOnClickListener(
                new AuroraListView.AuroraBackOnClickListener() {
                    
                    @Override
                    public void auroraOnClick(final int position) {
                        if (mAdapter == null || mContext == null) {
                            return;
                        }
                        
                        if (AuroraCallRecordContactListActivity.mIsAddAutoRecording) {
                            ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording);
                            getListView().auroraSetRubbishBackNoAnim();
                            return;
                        }
                        
                        if (AuroraCallRecordContactListActivity.mIsRemoving) {
                            ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording_remove);
                            getListView().auroraSetRubbishBackNoAnim();
                            return;
                        }
                        
                        final int pos = getRightPosition(position);
                        final String number = mAdapter.getNumber(pos);
                        final String message = getActivity().getString(R.string.aurora_remove_auto_record_message, number);
                        AuroraAlertDialog deleteConDialog = new AuroraAlertDialog.Builder(mContext,
                                AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                                .setTitle(R.string.gn_remove)
                                .setMessage(message)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                    
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                                long dataId = mAdapter.getDataId(pos);
                                                ContentValues value = new ContentValues();
                                                value.put("auto_record", 0);
                                                
                                                mContext.getContentResolver().update(Data.CONTENT_URI, value, 
                                                        Data._ID + "=" + dataId, null);
                                                Toast.makeText(mContext, 
                                                        mContext.getResources().getString(
                                                                R.string.aurora_remove_group_one_toast, number), 
                                                                Toast.LENGTH_SHORT).show();
                                                
//                                                getListView().auroraDeleteSelectedItemAnim(); //remove temp
                                                getListView().auroraSetRubbishBackNoAnim();
                                            }
                                }).create();
                        deleteConDialog.show();
                    }
                    
                    @Override
                    public void auroraPrepareDraged(int position) {
                        if (mContext != null && mAlphbetIndexView != null && mAlphbetIndexView.isShown()) {
                            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.aurora_contact_prompt_exit);
                            mAlphbetIndexView.startAnimation(animation);
                            mAlphbetIndexView.setVisibility(View.GONE);
                        }
                    }
                    
                    @Override
                    public void auroraDragedSuccess(int position) {
                        if (mAlphbetIndexView != null) {
                            mAlphbetIndexView.setVisibility(View.GONE);
                        }
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
                });
        
        mNoContactsEmptyView = getView().findViewById(
                R.id.no_contacts);
    }
    
    private void initButtomBar(boolean flag) {
        try {
            AuroraActionBar actionBar;
            actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
            actionBar.setShowBottomBarMenu(flag);
            actionBar.showActionBottomeBarMenu();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage msg = " + msg.what);

            switch (msg.what) {

            case WAIT_CURSOR_START:
                Log.i(TAG, "start WAIT_CURSOR_START !isFinished : "
                        + !isFinished);
                if (!isFinished) {
                    mLoadingContainer.setVisibility(View.VISIBLE);
                    mLoadingContact.setVisibility(View.VISIBLE);
                    mProgress.setVisibility(View.VISIBLE);
                } else {
                    mLoadingContainer.setVisibility(View.GONE);
                    mLoadingContact.setVisibility(View.GONE);
                    mProgress.setVisibility(View.GONE);
                }
                break;

            default:
                break;
            }
        }
    };

    
    @Override
    public void onResume() {
        getListView().auroraOnResume();
        super.onResume();

        bindToAlphdetIndexer();
        if(AuroraSimContactListActivity.dataIdForAutoRecord!=null&&AuroraSimContactListActivity.dataIdForAutoRecord.size()>0){
        	Toast.makeText(mContext, getResources().getString(R.string.aurora_add_group_member_toast, 
        			AuroraSimContactListActivity.dataIdForAutoRecord.size()), Toast.LENGTH_SHORT).show();
        	AuroraSimContactListActivity.dataIdForAutoRecord.clear();
        	AuroraSimContactListActivity.dataIdForAutoRecord=null;
        }
        
    }
    
    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        if (mAdapter != null) {
            mAdapter = null;
        }
    }

    @Override
    protected ContactEntryListAdapter createListAdapter() {
        mAdapter = new AuroraSimContactListAdapter(getContext());
//        if (mIsCustomFilter) {
//            mAdapter.setFilter(ContactListFilter
//                    .createFilterWithType(ContactListFilter.FILTER_TYPE_CUSTOM));
//        } else {
            mAdapter.setFilter(ContactListFilter
                    .createFilterWithType(ContactListFilter.FILTER_TYPE_ACCOUNT));
            mAdapter.setCallRecordMode(true);
//        }
        
        mAdapter.setSectionHeaderDisplayEnabled(true);
        mAdapter.setCheckBoxEnable(false);
        return mAdapter;
    }
    
//    @Override
//    public CursorLoader createCursorLoader() {
//        Log.i(TAG, "createCursorLoader");
//
//        isFinished = false;
//        Log.d(TAG, "filter = " + getAdapter().getFilter().filterType);
//        return new StarredAndContactsLoader(getActivity(), true);
//    }
    
    @Override
    protected void configureAdapter() {
        super.configureAdapter();
        ContactEntryListAdapter adapter = getAdapter();
        adapter.setSectionHeaderDisplayEnabled(true);
//        if (mIsCustomFilter) {
//            adapter.setFilter(ContactListFilter
//                    .createFilterWithType(ContactListFilter.FILTER_TYPE_CUSTOM));
//        } else {
            adapter.setFilter(ContactListFilter
                    .createFilterWithType(ContactListFilter.FILTER_TYPE_ACCOUNT));
//        }
        adapter.setFilterExInfo(mFilterExInfo);
    }
    
    private void showEmptyView(boolean flag) {
        if (flag) {
            if (mAlphbetIndexView != null) {
                mAlphbetIndexView.setVisibility(View.GONE);
            }

            if (mNoContactsEmptyView != null && !mIsAuroraSearchMode) {
                mNoContactsEmptyView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mAlphbetIndexView != null && !mIsAuroraSearchMode) {
                mAlphbetIndexView.setVisibility(View.VISIBLE);
            }

            if (mNoContactsEmptyView != null) {
                mNoContactsEmptyView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
    	super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    	
        if (null != mAlphbetIndexView) {
            mAlphbetIndexView.invalidateShowingLetterIndex();
        }
    }
    
    private void bindToAlphdetIndexer() {
        if (null != mAlphbetIndexView) {
            mAlphbetIndexView.setList(getListView(), this);
        }
    }
    
    public void onSelectAll(boolean check) {
        updateListCheckBoxeState(check);
        
        if(middleTextView!=null){
			middleTextView.setText(mContext.getString(R.string.selected_total_num, getAdapter().getSelectedCount()));
		}
    }

    private void updateListCheckBoxeState(boolean checked) {
        if (mIsAuroraSearchMode) {
            return;
        }
        
        final int headerCount = getListView().getHeaderViewsCount();
        final int count = mAdapter.getCount() + headerCount;
        
        for (int position = 0; position < count; ++position) {
            int realPosition = position - getListView().getFirstVisiblePosition();
            int adapterPos = position - headerCount;
            
            if (realPosition >= 0) {
                View view = getListView().getChildAt(realPosition);
                if (view != null) {
                    final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                    if (null != checkBox) {
                        checkBox.auroraSetChecked(checked, true);
                    }
                }
            }
            
            if (adapterPos >= 0) {
                String name = mAdapter.getContactDisplayName(adapterPos);
                String number = mAdapter.getNumber(adapterPos);
                long dataId = mAdapter.getDataId(adapterPos);
                
                if (checked) {
                    mAdapter.setCheckedItem(dataId, name + ":" + number);
                } else {
                    mAdapter.getCheckedItem().clear();
                }
            }
        }
        
        ((AuroraCallRecordContactListActivity)getActivity()).setBottomMenuEnable(checked);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
        
        isFinished = true;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                WAIT_CURSOR_DELAY_TIME);
        bindToAlphdetIndexer();
        
        if (mIsAuroraSearchMode) {
            auroraNoMatchView(true, null);
        }
        
        if (data == null || (data != null && data.getCount() <= 0)) {
            showEmptyView(true);
        } else {
            showEmptyView(false);
        }
        
        mItemCount = mAdapter.getCount();
        for (int position = 0; position < mItemCount; position++) {
            try {
                String name = "";
                String number = mAdapter.getNumber(position);
                
                if (mAdapter.getIsQueryForDialer()) {
                    name =  data.getString(6);
                } else {
                    name = data.getString(PhoneQuery.PHONE_DISPLAY_NAME);
                }
                if (null != number) {
                    number = number.replaceAll(" ", "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        
        if (!getRemoveMemberMode()) {
            getListView().auroraEnableSelector(true);
        }
        
        ((AuroraCallRecordContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
    }
    
    private void setQueryTextToFragment(String query) {
        setQueryString(query, true);
        setVisibleScrollbarEnabled(!mIsAuroraSearchMode);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (((AuroraCallRecordContactListActivity)(getActivity())).isSearchviewLayoutShow()) {
            return;
        }
        
        if (!mIsNeedContextMenu) {
            return;
        }
        
        if (AuroraCallRecordContactListActivity.mIsAddAutoRecording) {
            ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording);
            return;
        }
        
        if (AuroraCallRecordContactListActivity.mIsRemoving) {
            ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording_remove);
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
            final int pos = getRightPosition(info.position);
            getAdapter().setCheckedItem(Long.valueOf(mAdapter.getDataId(pos)), "");
        }
        
        mIsNeedContextMenu = false;
        getAdapter().setCheckBoxEnable(true);
        getAdapter().setNeedAnim(true);
        setRemoveMemberMode(true);
        
        getListView().auroraSetNeedSlideDelete(false);
        getListView().auroraEnableSelector(false);
        
        getAdapter().notifyDataSetChanged();
        
        initActionBar(true);
        ((AuroraCallRecordContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
    }
    
    public void setRemoveMemberMode(boolean flag) {
        mIsRemoveMemberMode = flag;
    }
    
    public boolean getRemoveMemberMode() {
        return mIsRemoveMemberMode;
    }
    
    public void changeToNormalMode(boolean flag) {
        if (getActivity() == null) {
            return;
        }
        ((AuroraActivity)getActivity()).setMenuEnable(true);
        if (!flag) {
            initActionBar(false);
        }
        
        getListView().auroraSetNeedSlideDelete(true);
        getListView().auroraEnableSelector(true);
        
        if (!flag) {
            mHandler.postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    
                    try {
                        getAdapter().getCheckedItem().clear();
                        getAdapter().setCheckBoxEnable(false);
                        getAdapter().setNeedAnim(true);
                        setRemoveMemberMode(false);
                        mIsNeedContextMenu = true;
                        getAdapter().notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 330);
        } else {
            getAdapter().getCheckedItem().clear();
            getAdapter().setCheckBoxEnable(false);
            getAdapter().setNeedAnim(true);
            setRemoveMemberMode(false);
            mIsNeedContextMenu = true;
            getAdapter().notifyDataSetChanged();
        }
    }
    
    private TextView middleTextView;
    private void initActionBar(boolean flag) {
        AuroraActionBar actionBar;
        actionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
        actionBar.setShowBottomBarMenu(flag);
        actionBar.showActionBarDashBoard();
        
        if(middleTextView==null){
			middleTextView=actionBar.getMiddleTextView();
		}

//		getListView().removeHeaderView(add_group_member);
		if(middleTextView!=null){
			middleTextView.setText(mContext.getString(R.string.selected_total_num, 1));
		}
    }
    
    @Override
    protected void onItemClick(int position, long id) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        
        Log.d(TAG, "onItemClick with adapterView: position = " + position + "  id = " + id);
        
        final AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        if (null != checkBox) {
            boolean checked = checkBox.isChecked();
            checkBox.auroraSetChecked(!checked, true);
            
            int realPosition = getRightPosition(position);
            String number = mAdapter.getNumber(realPosition);
            long dataId = mAdapter.getDataId(realPosition);
            
            if (!checked) {
                String name = mAdapter.getContactDisplayName(realPosition);
                mAdapter.setCheckedItem(dataId, name + ":" + number);
                Log.d(TAG, "number = " + number + "  name = " + name + "  dataId = " + dataId);
            } else {
                mAdapter.getCheckedItem().remove(dataId);
            }
            
            if(middleTextView!=null){
				middleTextView.setText(mContext.getString(R.string.selected_total_num, getAdapter().getSelectedCount()));
			}
            
            mAdapter.notifyDataSetChanged();
            ((AuroraCallRecordContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
        }
    }

    private final class svQueryTextListener implements aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener {
        @Override
        public boolean onQueryTextChange(String newText) {
            setMultiPhoneSearch(true);
            
            if (newText.length() > 0) {
                setQueryTextToFragment(newText);
                mIsAuroraSearchMode = true;
                mAlphbetIndexView.setVisibility(View.GONE);
                
                if (checkIsNeedQueryFromDialer(newText)) {
                    auroraNoMatchView(mIsAuroraSearchMode, null);
                }
                
//                if (mGotoSearchLayout != null) {
//                    mGotoSearchLayout.setVisibility(View.GONE);
//                }
                getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
            } else {
                setQueryTextToFragment("");
                mIsAuroraSearchMode = false;
                mAlphbetIndexView.setVisibility(View.VISIBLE);
                auroraNoMatchView(mIsAuroraSearchMode, null);
                
//                if (mGotoSearchLayout != null) {
//                    mGotoSearchLayout.setVisibility(View.VISIBLE);
//                }
                getListView().auroraSetHeaderViewYOffset(0);
            }
            
//            ((AuroraCallRecordContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
            mAdapter.setSearchMode(mIsAuroraSearchMode);
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            // TODO Auto-generated method stub
            return false;
        }
    }
    
    private int getRightPosition(int position) {
        return position;
    }
}
