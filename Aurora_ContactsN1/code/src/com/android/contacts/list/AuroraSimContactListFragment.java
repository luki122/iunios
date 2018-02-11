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
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnCloseButtonClickListener;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.activities.AuroraSimContactListActivity;
import com.android.contacts.list.AuroraSimContactListAdapter.PhoneQuery;
import com.android.contacts.widget.AbsListIndexer;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.view.View.OnFocusChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import android.content.SharedPreferences;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraCheckBox;

/**
 * AURORA::add::wangth::20130902
 */
public class AuroraSimContactListFragment extends
        ContactEntryListFragment<ContactEntryListAdapter> {
    private static final String TAG = "AuroraSimContactListFragment";
    private Context mContext;
    
    public AuroraSimContactListAdapter mAdapter;
    
    private boolean mIsAttachment = false;
    private boolean mIsBlackName = false;
    private boolean mIsCallRecord = false;
    private ArrayList<String> mDataIdList = new ArrayList<String>();
    private String mBlackNumbers = null;
    private boolean mIsMmsSelectContact = false;
    private ArrayList<String> mNumberList = new ArrayList<String>();
    
    private boolean mIsEmailSelect = false;
    private ArrayList<String> mEmailList = new ArrayList<String>();
    
    public boolean mSearchViewHasFocus = false;
    
    private View mLoadingContainer;
    private TextView mLoadingContact;
    private ProgressBar mProgress;
    
    private boolean isFinished = false;
    private static final int WAIT_CURSOR_START = 1230;
    
    private ContactListFilter mFilter;
    private String mFilterExInfo;

    private RelativeLayout mGotoSearchLayout;
    private AbsListIndexer mAlphbetIndexView;
    private FrameLayout mMainLay;
    private AuroraSearchView mSearchView;
    private TextView mNoContactsEmptyView;
    
    private boolean mIsAuroraSearchMode = false;
    private boolean mIsNeedContextMenu = true;
    private static boolean mNeedCreateDialerTempTable = true;
    public int mItemCount = 0;
    
    private SharedPreferences mPrefs;
    private boolean mIsCustomFilter = false;
    
    public void AuroraSimContactListFragment() {
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
        mIsCallRecord = false; 
        mIsMmsSelectContact = false;
        mIsEmailSelect = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(getActivity());
        int filterInt = mPrefs.getInt("filter.type", -1);
        if (filterInt == -3) {
            mIsCustomFilter = true;
        }

        String action = getActivity().getIntent().getAction();
        if (null != action) {
        	if (AuroraSimContactListActivity.ACTION_FOR_MMS_SELECT.equals(action)) {
        		mIsMmsSelectContact = true;
        	} else if (AuroraSimContactListActivity.ACTION_FOR_EMAIL_SELECT.equals(action)) {
        		mIsEmailSelect = true;
        		mIsShowingEmailList = true;
        	}
        }
        
        Bundle extras = getActivity().getIntent().getExtras();
        if (null != extras) {
            mFilter = extras
                    .getParcelable(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER);
            mFilterExInfo = extras
                    .getString(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO);
            
            mIsCallRecord = extras.getBoolean("aurora_call_record_select");
            
            mIsBlackName = extras.getBoolean("blackname_select");
            mBlackNumbers = extras.getString("blacknumbers");
            
            mNumberList = extras.getStringArrayList("ContactNumbers");
            mIsAttachment = extras.getBoolean("isAttachment");
            
            mDataIdList = extras.getStringArrayList("auto_record_data_ids");
            
            mEmailList = extras.getStringArrayList("emails");
        }
        
        if (null == mFilterExInfo) {
            mFilterExInfo = "mms_select/";
        }
        
        setSlideDelete(false);
    }
    
    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.aurora_group_detail_list_content, null);
    }
    
    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);

        isFinished = false;
        mAlphbetIndexView = (AbsListIndexer) getView().findViewById(
                R.id.gn_alphbet_indexer);
        mMainLay = (FrameLayout)getView().findViewById(
                R.id.main_content);
        		
        mLoadingContainer = getView().findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact = (TextView) getView().findViewById(
                R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) getView().findViewById(
                R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);
        mHandler.sendEmptyMessage(WAIT_CURSOR_START);
        
        if (!mIsBlackName) {
            mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
        	mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    ((AuroraActivity) getActivity()).showSearchviewLayout();
                    mSearchView = ((AuroraActivity) getActivity()).getSearchView();
                    mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                    mSearchView.setMaxLength(30);
                    initButtomBar(false);
                    
                    int count = 0;
                    if (null != mAdapter.getCheckedItem()) {
                        count += mAdapter.getCheckedItem().size();
                    }
                    if (null != mAdapter.getMmsSelectList()) {
                        count += mAdapter.getMmsSelectList().size();
                    }
                    
                    ((AuroraSimContactListActivity)getActivity()).setRightBtnTv(count);
                    ((AuroraActivity) getActivity()).setOnQueryTextListener(new svQueryTextListener());
                    
                    mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean hasFocus) {
                            mSearchViewHasFocus = hasFocus;
                            if (hasFocus) {
                                if (mNeedCreateDialerTempTable) {
                                    auroraInitQueryDialerABC();
                                    mNeedCreateDialerTempTable = false;
                                }
                            }
                        }
                    });
                    setSearchView(mSearchView);
                }
                
            });
            getListView().addHeaderView(mGotoSearchLayout);
        }

        getListView().setFastScrollEnabled(false);
        getListView().setFastScrollAlwaysVisible(false);
        getListView().setOnCreateContextMenuListener(this);
        
        mNoContactsEmptyView = (TextView)getView().findViewById(
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
                    mMainLay.setVisibility(View.GONE);
                } else {
                    mLoadingContainer.setVisibility(View.GONE);
                    mLoadingContact.setVisibility(View.GONE);
                    mProgress.setVisibility(View.GONE);
                    mMainLay.setVisibility(View.VISIBLE);
                }
                break;

            default:
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        bindToAlphdetIndexer();
        getListView().auroraEnableSelector(false);
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
//        }
        
        mAdapter.setSectionHeaderDisplayEnabled(true);
        mAdapter.setCheckBoxEnable(mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect);
        mAdapter.setMmsSelectMode(mIsMmsSelectContact, mNumberList);
        mAdapter.setIsMmsAttachment(mIsAttachment);
        mAdapter.setAutoRecordSelectMode(mIsCallRecord, mDataIdList);
        mAdapter.setBlackNameSelectMode(mIsBlackName, mBlackNumbers);
        mAdapter.setEmailSelectMode(mIsEmailSelect, mEmailList);
        return mAdapter;
    }
    
    @Override
    public CursorLoader createCursorLoader() {
        Log.i(TAG, "createCursorLoader");
        
        if (mIsAuroraSearchMode || mIsBlackName || mIsEmailSelect) {
            return new CursorLoader(mContext, null, null, null, null, null);
        }

        Log.d(TAG, "filter = " + getAdapter().getFilter().filterType);
        if (mIsCallRecord) {
            return new StarredAndContactsLoader(getActivity(), true, true);
        }
        return new StarredAndContactsLoader(getActivity(), true);
    }
    
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
    }

    private void updateListCheckBoxeState(boolean checked) {
        if (mIsAuroraSearchMode) {
            return;
        }
        
        final int headerCount = getListView().getHeaderViewsCount();
        final int count = mAdapter.getCount() + headerCount;
        int firstVPos = getListView().getFirstVisiblePosition();
        int realPosition = 0;
        int adapterPos = 0;
        int privacyId = 0;
        String name = null;
        String number = null;
        long dataId = -1;
        View view = null;
        AuroraCheckBox checkBox = null;
        
        for (int position = 0; position < count; ++position) {
            realPosition = position - firstVPos;
            adapterPos = position - headerCount;
            
            if (realPosition >= 0) {
                view = getListView().getChildAt(realPosition);
                if (view != null) {
                    checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                    if (null != checkBox) {
                        checkBox.auroraSetChecked(checked, true);
                    }
                }
            }
            
            if (adapterPos >= 0) {
                name = mAdapter.getContactDisplayName(adapterPos);
                number = mAdapter.getNumber(adapterPos);
                dataId = mAdapter.getDataId(adapterPos);
                
                if (checked) {
                	if (mIsEmailSelect) {
                		mAdapter.setCheckedItem(dataId, name + AuroraSimContactListActivity.POLYPHONIC_SEPARATOR + number);
                	} else if (ContactsApplication.sIsAuroraPrivacySupport && !mIsAttachment) {
                		privacyId = mAdapter.getPrivacyId(realPosition);
                		mAdapter.setCheckedItem(dataId, name + '\1' + number + '\1' + privacyId);
                	} else {
                		mAdapter.setCheckedItem(dataId, name + ":" + number);
                	}
                } else {
                    mAdapter.getCheckedItem().clear();
                    if (mIsMmsSelectContact && number != null 
                            && mAdapter.getMmsSelectList() != null 
                            && mAdapter.getMmsSelectList().contains(number)) {
                        mAdapter.getMmsSelectList().remove(number);
                    } else if (mIsCallRecord && mAdapter.getCallRecordSelectList() != null) {
                        mAdapter.getCallRecordSelectList().remove(String.valueOf(dataId));
                    } else if (mIsEmailSelect && number != null 
                            && mAdapter.getEmailSelectList() != null 
                            && mAdapter.getEmailSelectList().contains(number)) {
                    	mAdapter.getEmailSelectList().remove(number);
                    }
                }
            }
        }
        
        ((AuroraSimContactListActivity)getActivity()).setBottomMenuEnable(checked);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
        
        isFinished = true;
        mHandler.sendEmptyMessage(WAIT_CURSOR_START);
        bindToAlphdetIndexer();
        
        if (mIsAuroraSearchMode) {
            auroraNoMatchView(true, null);
        }
        
        if (data != null && data.getCount() <= 0) {
            showEmptyView(true);
        } else {
            showEmptyView(false);
        }
        
        if (!(mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect)) {
            return;
        }
        
        mItemCount = mAdapter.getCount();
        for (int position = 0; position < mItemCount; position++) {
            try {
                String name = mAdapter.getContactDisplayName(position);
                String number = mAdapter.getNumber(position);
                long dataId = mAdapter.getDataId(position);
                
                if (null != number) {
                    number = number.replaceAll(" ", "");
                }
                
                if (ContactsApplication.sIsAuroraPrivacySupport && mIsMmsSelectContact && !mIsAttachment) {
                	number = number + '\1' + AuroraPrivacyUtils.mCurrentAccountId;
                }
                
                if (number != null && mEmailList != null && mEmailList.contains(number)) {
                	mAdapter.getCheckedItem().put(dataId, name + AuroraSimContactListActivity.POLYPHONIC_SEPARATOR + number);
                } else if (number != null && mNumberList != null && mNumberList.contains(number)) {
                	if (ContactsApplication.sIsAuroraRejectSupport) {
            			number = number + '\1' + mAdapter.getPrivacyId(position);
            		}
                    mAdapter.getCheckedItem().put(dataId, name + '\1' + number);
                } else if (number != null && mDataIdList != null && mDataIdList.contains(String.valueOf(dataId))) {
                    mAdapter.getCheckedItem().put(dataId, name + ":" + number);
                    mDataIdList.remove(String.valueOf(dataId));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (mEmailList != null) {
        	mEmailList.clear();
        } else if (mNumberList != null) {
        	mNumberList.clear();
        } else if (mDataIdList != null) {
        	mDataIdList.clear();
        }
        
        ((AuroraSimContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
    }
    
    private void setQueryTextToFragment(String query) {
        setQueryString(query, true);
        setVisibleScrollbarEnabled(!mIsAuroraSearchMode);
    }
    
    @Override
    protected void onItemClick(int position, long id) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        
        if (!(mIsCallRecord || mIsMmsSelectContact || mIsBlackName || mIsEmailSelect)) {
            return;
        }
        
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
                if (mIsEmailSelect) {
            		mAdapter.setCheckedItem(dataId, name + AuroraSimContactListActivity.POLYPHONIC_SEPARATOR + number);
            	} else if (ContactsApplication.sIsAuroraPrivacySupport && !mIsAttachment) {
                	int privacyId = mAdapter.getPrivacyId(realPosition);
                	mAdapter.setCheckedItem(dataId, name + '\1' + number + '\1' + privacyId);
                } else {
                	mAdapter.setCheckedItem(dataId, name + ":" + number);
                }
                Log.d(TAG, "number = " + number + "  name = " + name + "  dataId = " + dataId);
            } else {
                mAdapter.getCheckedItem().remove(dataId);
                if (mIsMmsSelectContact && mAdapter.getMmsSelectList() != null) {
                    mAdapter.getMmsSelectList().remove(number);
                } else if ((mIsBlackName || mIsCallRecord) && mAdapter.getCallRecordSelectList() != null) {
                    mAdapter.getCallRecordSelectList().remove(String.valueOf(dataId));
                } else if (mIsEmailSelect && number != null 
                        && mAdapter.getEmailSelectList() != null 
                        && mAdapter.getEmailSelectList().contains(number)) {
                	mAdapter.getEmailSelectList().remove(number);
                }
            }
            
            mAdapter.notifyDataSetChanged();
            ((AuroraSimContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
        }
    }

    private final class svQueryTextListener implements aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener {
        @Override
        public boolean onQueryTextChange(String newText) {
            setMultiPhoneSearch(true);
            
            if (newText.length() > 0) {
                mIsAuroraSearchMode = true;
                setQueryTextToFragment(newText);
                mAlphbetIndexView.setVisibility(View.GONE);
                
                if (checkIsNeedQueryFromDialer(newText)) {
                    auroraNoMatchView(mIsAuroraSearchMode, null);
                }
                
//                if (mGotoSearchLayout != null) {
//                    mGotoSearchLayout.setVisibility(View.GONE);
//                }
                getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
            } else {
                mIsAuroraSearchMode = false;
                setQueryTextToFragment("");
                mAlphbetIndexView.setVisibility(View.VISIBLE);
                auroraNoMatchView(mIsAuroraSearchMode, null);
                
//                if (mGotoSearchLayout != null) {
//                    mGotoSearchLayout.setVisibility(View.VISIBLE);
//                }
                getListView().auroraSetHeaderViewYOffset(0);
            }
            
//            ((AuroraSimContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
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
    	if (mIsBlackName) {
    	    return position;
    	}
        return position - 1;
    }
}
