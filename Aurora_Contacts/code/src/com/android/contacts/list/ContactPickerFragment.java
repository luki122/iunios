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

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsSearchManager;
import com.android.contacts.R;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.widget.AbsListIndexer;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import aurora.widget.AuroraListView;

/**
 * Fragment for the contact list used for browsing contacts (as compared to
 * picking a contact with one of the PICK or SHORTCUT intents).
 */
public class ContactPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter>
        implements OnShortcutIntentCreatedListener {

    private static final String KEY_EDIT_MODE = "editMode";
    private static final String KEY_CREATE_CONTACT_ENABLED = "createContactEnabled";
    private static final String KEY_SHORTCUT_REQUESTED = "shortcutRequested";
	private static final String TAG = "liyang-ContactPickerFragment";

    //Gionee <huangzy> <2013-04-25> add for CR00801750 begin
    AbsListIndexer mAlphbetIndexView;
    //Gionee <huangzy> <2013-04-25> add for CR00801750 end
    
    private RelativeLayout mGotoSearchLayout;
    
    private OnContactPickerActionListener mListener;
    private boolean mCreateContactEnabled;
    private boolean mEditMode;
    private boolean mShortcutRequested;
    // add by mediatek
    private boolean mFromCallLog = false;

    public boolean ismFromCallLog() {
        return mFromCallLog;
    }
    private Context mContext;
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

    public void setmFromCallLog(boolean mFromCallLog) {
        this.mFromCallLog = mFromCallLog;
    }

    public ContactPickerFragment() {
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(false);
        setVisibleScrollbarEnabled(true);
        // gionee xuhz 20121208 modify for GIUI2.0 start
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            setQuickContactEnabled(true);
        } else {
            setQuickContactEnabled(false);
        }
        // gionee xuhz 20121208 modify for GIUI2.0 end
        setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_CONTACT_SHORTCUT);
    }

    public void setOnContactPickerActionListener(OnContactPickerActionListener listener) {
        mListener = listener;
    }

    public boolean isCreateContactEnabled() {
        return mCreateContactEnabled;
    }

    public void setCreateContactEnabled(boolean flag) {
        this.mCreateContactEnabled = flag;
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    public void setEditMode(boolean flag) {
        mEditMode = flag;
    }

    public boolean isShortcutRequested() {
        return mShortcutRequested;
    }

    public void setShortcutRequested(boolean flag) {
        mShortcutRequested = flag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_EDIT_MODE, mEditMode);
        outState.putBoolean(KEY_CREATE_CONTACT_ENABLED, mCreateContactEnabled);
        outState.putBoolean(KEY_SHORTCUT_REQUESTED, mShortcutRequested);
    }

    @Override
    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);

        if (savedState == null) {
            return;
        }

        mEditMode = savedState.getBoolean(KEY_EDIT_MODE);
        mCreateContactEnabled = savedState.getBoolean(KEY_CREATE_CONTACT_ENABLED);
        mShortcutRequested = savedState.getBoolean(KEY_SHORTCUT_REQUESTED);
    }

    public void quitSeach(){			
		ContactSelectionActivity.mSearchView.clearText();
		svQueryTextListener.onQueryTextChange("");
		((AuroraActivity)getActivity()).hideSearchviewLayout();	

		if(mGotoSearchLayout!=null && !mGotoSearchLayout.isShown()){
			getListView().addHeaderView(mGotoSearchLayout);

		}
		getListView().auroraSetHeaderViewYOffset(0);
		mIsAuroraSearchMode = false;
		getAdapter().setSearchMode(mIsAuroraSearchMode);
		mAlphbetIndexView.setVisibility(View.VISIBLE);
		reloadData();
	}
    
    protected void reloadData() {
		super.reloadData();
	}
    
    View main_content;
    public ImageButton searchViewBackButton;
    public boolean mIsAuroraSearchMode = false;
    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        Log.d(TAG,"onCreateView");
        
        svQueryTextListener=new SvQueryTextListener();
        
        searchViewBackButton=(((AuroraActivity)getActivity()).getAuroraActionBar()).getAuroraActionbarSearchViewBackButton();				
		Log.d(TAG,"searchViewBackButton:"+searchViewBackButton);

		searchViewBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ContactSelectionActivity.mSearchView.clearText();
				svQueryTextListener.onQueryTextChange("");
				((AuroraActivity)getActivity()).hideSearchviewLayout();	

				if(mGotoSearchLayout!=null && !mGotoSearchLayout.isShown()){
					getListView().addHeaderView(mGotoSearchLayout);
//					getListView().setPadding(0,0,0,0);

				}
				getListView().auroraSetHeaderViewYOffset(0);
				mIsAuroraSearchMode = false;
				getAdapter().setSearchMode(mIsAuroraSearchMode);
				reloadData();
				

			}
		});
		
        if (mCreateContactEnabled) {
            getListView().addHeaderView(inflater.inflate(R.layout.create_new_contact, null, false));
        }
        
        mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
        mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((AuroraActivity) getActivity()).showSearchviewLayout();
//                AuroraSearchView mSearchView = ((AuroraActivity) getActivity()).getSearchView();
//                mSearchView.setMaxLength(30);
//                mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
//                
//                ((AuroraActivity) getActivity()).setOnQueryTextListener(new svQueryTextListener());
            	
            	if(mGotoSearchLayout!=null && mGotoSearchLayout.isShown()){
					getListView().removeHeaderView(mGotoSearchLayout);
					

				}				

				Log.d(TAG,"onClick search");

				((AuroraActivity) getActivity()).showSearchviewLayout();	
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);  
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);					

				if(null==ContactSelectionActivity.mSearchView) return;	
				ContactSelectionActivity.mSearchView.setOnQueryTextListener(svQueryTextListener);	


				setSearchView(ContactSelectionActivity.mSearchView);
				mIsAuroraSearchMode=true;
				getAdapter().setSearchMode(mIsAuroraSearchMode);
				auroraInitQueryDialerABC();
				
//				getListView().setPadding(0,mContext.getResources()
//						.getDimensionPixelSize(
//								R.dimen.aurora_group_entrance_left_margin),0,0);
//				getListView().smoothScrollBy(-300, 0);
            }
        });
        getListView().addHeaderView(mGotoSearchLayout);
        
        //Gionee <huangzy> <2013-04-25> add for CR00801750 begin
        if (ContactsApplication.sIsGnContactsSupport) {
            mAlphbetIndexView = (AbsListIndexer) getView().findViewById(R.id.gn_alphbet_indexer);
        }
        //Gionee <huangzy> <2013-04-25> add for CR00801750 end
    }
    
    
    
    private SvQueryTextListener svQueryTextListener;
    // aurora <wangth> <2013-12-9> add for aurora begin
    private final class SvQueryTextListener implements OnQueryTextListener {
        @Override
        public boolean onQueryTextChange(String newText) {
            setQueryString(newText, true);
            if (newText.length() > 0) {
                if (checkIsNeedQueryFromDialer(newText)) {
                    auroraNoMatchView(true, null);
                }
                
//                if (mGotoSearchLayout != null) {
//                    mGotoSearchLayout.setVisibility(View.GONE);
//                }
                getListView().auroraSetHeaderViewYOffset(ContactsApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
            } else {
                auroraNoMatchView(false, null);
                
//                if (mGotoSearchLayout != null) {
//                    mGotoSearchLayout.setVisibility(View.VISIBLE);
//                }
                getListView().auroraSetHeaderViewYOffset(0);
            }
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    }
    // aurora <wangth> <2013-12-9> add for aurora end

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0 && mCreateContactEnabled) {
            mListener.onCreateNewContactAction();
        } else {
            super.onItemClick(parent, view, position, id);
        }
    }

    @Override
    protected void onItemClick(int position, long id) {
        Uri uri;
        if (isLegacyCompatibilityMode()) {
            uri = ((LegacyContactListAdapter)getAdapter()).getPersonUri(position);
        } else {
            uri = ((ContactListAdapter)getAdapter()).getContactUri(position);
        }
        if (mEditMode) {
            editContact(uri);
        } else  if (mShortcutRequested) {
            ShortcutIntentBuilder builder = new ShortcutIntentBuilder(getActivity(), this);
            builder.createContactShortcutIntent(uri);
        } else {
            pickContact(uri);
        }
    }

    public void createNewContact() {
        mListener.onCreateNewContactAction();
    }

    public void editContact(Uri contactUri) {
        mListener.onEditContactAction(contactUri);
    }

    public void pickContact(Uri uri) {
        mListener.onPickContactAction(uri);
    }

    @Override
    protected ContactEntryListAdapter createListAdapter() {
        if (!isLegacyCompatibilityMode()) {
            DefaultContactListAdapter adapter = new DefaultContactListAdapter(getActivity(), true);
            adapter.setFilter(ContactListFilter.createFilterWithType(
                    ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     
             *   CR ID: ALPS00112614
             *   Descriptions: only show phone contact if it's from sms  同时彻底删除
             */
            if (!ismFromCallLog()) {
            adapter.setOnlyShowPhoneContacts(true);
            }
            /*
             * Bug Fix by Mediatek End.
             */
            adapter.setSectionHeaderDisplayEnabled(false);
            adapter.setDisplayPhotos(true);
            adapter.setQuickContactEnabled(false);
            return adapter;
        } else {
            LegacyContactListAdapter adapter = new LegacyContactListAdapter(getActivity());
            adapter.setSectionHeaderDisplayEnabled(false);
            adapter.setDisplayPhotos(false);
            return adapter;
        }
    }

    @Override
    protected void configureAdapter() {
        super.configureAdapter();

        ContactEntryListAdapter adapter = getAdapter();

        // If "Create new contact" is shown, don't display the empty list UI
        adapter.setEmptyListEnabled(!isCreateContactEnabled());
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        //Gionee <huangzy> <2013-04-25> modify for CR00801750 begin
        if (ContactsApplication.sIsGnContactsSupport) {
    		return inflater.inflate(R.layout.gn_contact_picker_content, null);
    	} else {
    		return inflater.inflate(R.layout.contact_picker_content, null);
    	}
        //Gionee <huangzy> <2013-04-25> modify for CR00801750 begin
    }

    public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
        mListener.onShortcutIntentCreated(shortcutIntent);
    }

    @Override
    public void onPickerResult(Intent data) {
        mListener.onPickContactAction(data.getData());
    }
    
    //Gionee <huangzy> <2013-04-25> add for CR00801750 begin
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	super.onLoadFinished(loader, data);
    	
    	if (ContactsApplication.sIsGnContactsSupport) {
        	bindToAlphdetIndexer();
        	// aurora <wangth> <2013-12-17> add for aurora begin
        	if (getQueryString() == null || 
        	        (getQueryString() != null && getQueryString().length() == 0)) {
        	    
                if (data == null || (data != null && data.getCount() == 0)) {
                    if (getActivity() == null) {
                        return;
                    }
                    
                    if (getActivity().getResources() != null) {
                        String str = getActivity().getResources().getString(R.string.aurora_empty_contact_text);
                        auroraNoMatchView(true, str);
                    }
                    
                    if (null != mAlphbetIndexView) {
                        mAlphbetIndexView.setVisibility(View.GONE);
                    }
                    
                    if (null != mGotoSearchLayout) {
                        mGotoSearchLayout.setVisibility(View.GONE);
                    }
                    
                    return;
                }
        	}
        	
        	if (null != mAlphbetIndexView) {
        	    mAlphbetIndexView.setVisibility(View.VISIBLE);
            }
        	
            if (getQueryString() != null && getQueryString().length() > 0) {
                auroraNoMatchView(true, null);
                if (null != mAlphbetIndexView) {
                    mAlphbetIndexView.setVisibility(View.GONE);
                }
            }
            // aurora <wangth> <2013-12-17> add for aurora end
        }
    }
    
    private void bindToAlphdetIndexer() {
    	AuroraListView listView = (AuroraListView)getListView(); 
    	
    	if (null != mAlphbetIndexView) {
    		mAlphbetIndexView.setList(listView, this);
    	}
    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        if (null != mAlphbetIndexView) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                mAlphbetIndexView.invalidateShowingLetterIndex();
            }   
        }    
    }
    
    //Gionee <xuhz> <2013-08-07> add for CR00846702 begin
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    	super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        if (null != mAlphbetIndexView) {
            mAlphbetIndexView.invalidateShowingLetterIndex();
        }  
    }
    //Gionee <xuhz> <2013-08-07> add for CR00846702 end
    
    protected boolean isAlphbetIndexEnabled() {
    	return null != mAlphbetIndexView;
    }
    //Gionee <huangzy> <2013-04-25> add for CR00801750 end
}
