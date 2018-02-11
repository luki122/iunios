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
import com.android.contacts.R;
import com.android.contacts.activities.AuroraSimContactListActivity;
import com.android.contacts.activities.ContactSelectionActivity;
import com.android.contacts.list.ShortcutIntentBuilder.OnShortcutIntentCreatedListener;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.widget.AbsListIndexer;
import com.android.contacts.widget.AlphbetIndexView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import aurora.widget.AuroraListView;
import aurora.app.AuroraActivity;
/**
 * Fragment containing a phone number list for picking.
 */
public class PhoneNumberPickerFragment extends ContactEntryListFragment<ContactEntryListAdapter>
        implements OnShortcutIntentCreatedListener {
    private static final String TAG = "liyang-"+PhoneNumberPickerFragment.class.getSimpleName();
    public boolean mIsAuroraSearchMode = false;
    private static final int REQUEST_CODE_ACCOUNT_FILTER = 1;

    private OnPhoneNumberPickerActionListener mListener;
    private String mShortcutAction;

    private ContactListFilter mFilter;

    private View mAccountFilterHeader;
    /**
     * Lives as ListView's header and is shown when {@link #mAccountFilterHeader} is set
     * to View.GONE.
     */
    private View mPaddingView;
    
    public static RelativeLayout mGotoSearchLayout;

    private static final String KEY_FILTER = "filter";

    /** true if the loader has started at least once. */
    private boolean mLoaderStarted;

    private ContactListItemView.PhotoPosition mPhotoPosition =
            ContactListItemView.DEFAULT_PHOTO_POSITION;

    private class FilterHeaderClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            AccountFilterUtil.startAccountFilterActivityForResult(
                    PhoneNumberPickerFragment.this, REQUEST_CODE_ACCOUNT_FILTER);
        }
    }
    private OnClickListener mFilterHeaderClickListener = new FilterHeaderClickListener();

    public void quitSeach(){			
		mIsAuroraSearchMode = false;
		super.setPartionStatus();
		ContactSelectionActivity.mSearchView.clearText();
		svQueryTextListener.onQueryTextChange("");
		((AuroraActivity)getActivity()).hideSearchviewLayout();	

		if(mGotoSearchLayout!=null && !mGotoSearchLayout.isShown()){
			getListView().addHeaderView(mGotoSearchLayout);

		}
		getListView().auroraSetHeaderViewYOffset(0);
		getListView().setVisibility(View.VISIBLE);
		
//		mAdapter.setSearchMode(mIsAuroraSearchMode);
		mAlphbetIndexView.setVisibility(View.VISIBLE);

	}


	public ImageButton searchViewBackButton;
	private SvQueryTextListener svQueryTextListener;
	
    public PhoneNumberPickerFragment() {
        // gionee xuhz 20121208 modify for GIUI2.0 start
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            setQuickContactEnabled(true);
        } else {
            setQuickContactEnabled(false);
        }
        // gionee xuhz 20121208 modify for GIUI2.0 end
        
        Log.d(TAG,"PhoneNumberPickerFragment setPhotoLoaderEnabled");
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_DATA_SHORTCUT);

        // Show nothing instead of letting caller Activity show something.
        setHasOptionsMenu(true);
    }

    public void setOnPhoneNumberPickerActionListener(OnPhoneNumberPickerActionListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        setPhotoLoaderEnabled(true);
        View paddingView = inflater.inflate(R.layout.contact_detail_list_padding, null, false);
        mPaddingView = paddingView.findViewById(R.id.contact_detail_list_padding);
        getListView().addHeaderView(paddingView);

        mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
        mAccountFilterHeader.setOnClickListener(mFilterHeaderClickListener);
        updateFilterHeaderView();
        
        //Gionee:huangzy 20120607 modify for CR00614801 start
        if (ContactsApplication.sIsGnContactsSupport) {
            mAlphbetIndexView = (AbsListIndexer) getView().findViewById(R.id.gn_alphbet_indexer);
        }
        //Gionee:huangzy 20120607 modify for CR00614801 end

        setVisibleScrollbarEnabled(!isLegacyCompatibilityMode());
        
        svQueryTextListener=new SvQueryTextListener();
		searchViewBackButton=(((AuroraActivity)getActivity()).getAuroraActionBar()).getAuroraActionbarSearchViewBackButton();				
		Log.d(TAG,"searchViewBackButton:"+searchViewBackButton);

		searchViewBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				quitSeach();
			}
		});
		
		mGotoSearchLayout = (RelativeLayout) inflater.inflate(R.layout.aurora_goto_search_mode, null);
		mGotoSearchLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//					mGotoSearchLayout.setVisibility(View.GONE);
				if(mGotoSearchLayout!=null && mGotoSearchLayout.isShown()){
					getListView().removeHeaderView(mGotoSearchLayout);
				}

				/*// TODO Auto-generated method stub
				((AuroraActivity) getActivity()).showSearchviewLayout();

				if (null == mSearchView) {
					mSearchView=AuroraSimContactListActivity.mActionBar.getAuroraActionbarSearchView();
					Log.d(TAG,"mSearchView:"+mSearchView);
				}

				if(null==mSearchView) return; 
				mSearchView.setOnQueryTextListener(new svQueryTextListener());	


				//                    mSearchView = ((AuroraActivity) getActivity()).getSearchView();
				mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
				mSearchView.setMaxLength(30);*/

				Log.d("liyang","onClick search");

				((AuroraActivity) getActivity()).showSearchviewLayout();	
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);  
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);					

				if(null==ContactSelectionActivity.mSearchView) return;	
				ContactSelectionActivity.mSearchView.setQueryHint(mContext.getResources().getString(R.string.search_contacts));
				ContactSelectionActivity.mSearchView.setOnQueryTextListener(svQueryTextListener);	


				setSearchView(ContactSelectionActivity.mSearchView);
				mIsAuroraSearchMode=true;
				getListView().setVisibility(View.GONE);
			}

		});
		getListView().addHeaderView(mGotoSearchLayout);
    }
    
	private void setQueryTextToFragment(String query) {
		setSearchMode(mIsAuroraSearchMode);
		setQueryString(query, true);
		setVisibleScrollbarEnabled(!mIsAuroraSearchMode);
	}
	
    private final class SvQueryTextListener implements OnQueryTextListener {
		@Override
		public boolean onQueryTextChange(String newText) {
			setMultiPhoneSearch(true);

			if (newText.length() > 0) {
				setQueryTextToFragment(newText);
				mAlphbetIndexView.setVisibility(View.GONE);

				if (checkIsNeedQueryFromDialer(newText)) {
					auroraNoMatchView(mIsAuroraSearchMode, null);
				}

				//                if (mGotoSearchLayout != null) {
				//                    mGotoSearchLayout.setVisibility(View.GONE);
				//                }
				getListView().setVisibility(View.VISIBLE);
				getListView().auroraSetHeaderViewYOffset(mContext.getResources().getDimensionPixelSize(R.dimen.aurora_goto_search_hight));
			} else {
				setQueryTextToFragment("");
				mAlphbetIndexView.setVisibility(View.VISIBLE);
				auroraNoMatchView(mIsAuroraSearchMode, null);

				//                if (mGotoSearchLayout != null) {
				//                    mGotoSearchLayout.setVisibility(View.VISIBLE);
				//                }
				getListView().auroraSetHeaderViewYOffset(0);
				getListView().setVisibility(View.GONE);
			}

			//            ((AuroraSimContactListActivity)getActivity()).updateSelectedItemsView(mItemCount);
//			mAdapter.setSearchMode(mIsAuroraSearchMode);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	private int getRightPosition(int position) {

		Log.d(TAG, "getRightPosition:"+position+" mIsAuroraSearchMode:"+mIsAuroraSearchMode);
		if(mIsAuroraSearchMode){
			Log.d(TAG,"mIsAuroraSearchMode");
			return position;
		}
		return position-1;
	}

    @Override
    protected void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
        updateFilterHeaderView();
        
        //Gionee:huangzy 20120527 add for CR00607983 start
        if (ContactsApplication.sIsGnContactsSupport) {
        	int visibility = flag ? View.GONE : View.VISIBLE;
        	final View aiv = getView().findViewById(R.id.gn_alphbet_indexer);
        	if (null != aiv) {
        		aiv.setVisibility(visibility);
        	}
        }
        //Gionee:huangzy 20120527 add for CR00607983 end
    }

    private void updateFilterHeaderView() {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnUpdateFilterHeaderView();
    		return;
    	}
    	
        final ContactListFilter filter = getFilter();
        if (mAccountFilterHeader == null || filter == null) {
            return;
        }
        final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPhone(
                mAccountFilterHeader, filter, false, false);
        if (shouldShowHeader) {
            mPaddingView.setVisibility(View.GONE);
            mAccountFilterHeader.setVisibility(View.VISIBLE);
        } else {
            mPaddingView.setVisibility(View.VISIBLE);
            mAccountFilterHeader.setVisibility(View.GONE);
        }
    }

    @Override
    public void restoreSavedState(Bundle savedState) {
        super.restoreSavedState(savedState);

        if (savedState == null) {
            return;
        }

        mFilter = savedState.getParcelable(KEY_FILTER);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_FILTER, mFilter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            if (mListener != null) {
                mListener.onHomeInActionBarSelected();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @param shortcutAction either {@link Intent#ACTION_CALL} or
     *            {@link Intent#ACTION_SENDTO} or null.
     */
    public void setShortcutAction(String shortcutAction) {
        this.mShortcutAction = shortcutAction;
    }

    @Override
    protected void onItemClick(int position, long id) {
        final Uri phoneUri;
        int realPosition = getRightPosition(position);
//        if (!isLegacyCompatibilityMode()) { // aurora wangth 20141114 modify for #9815
            PhoneNumberListAdapter adapter = (PhoneNumberListAdapter) getAdapter();
            phoneUri = adapter.getDataUri(position);

//        } else {
//            LegacyPhoneNumberListAdapter adapter = (LegacyPhoneNumberListAdapter) getAdapter();
//            phoneUri = adapter.getPhoneUri(position);
//        }

        if (phoneUri != null) {
            pickPhoneNumber(phoneUri);
        } else {
            Log.w(TAG, "Item at " + position + " was clicked before adapter is ready. Ignoring");
        }
    }

    @Override
    protected void startLoading() {
        mLoaderStarted = true;
        super.startLoading();
    }

    @Override
    protected ContactEntryListAdapter createListAdapter() {
//        if (!isLegacyCompatibilityMode()) { // aurora wangth 20141114 modify for #9815
            PhoneNumberListAdapter adapter = new PhoneNumberListAdapter(getActivity());
            adapter.setDisplayPhotos(true);
            return adapter;
//        } else {
//            LegacyPhoneNumberListAdapter adapter = new LegacyPhoneNumberListAdapter(getActivity());
//            adapter.setDisplayPhotos(true);
//            return adapter;
//        }
    }

    @Override
    protected void configureAdapter() {
        super.configureAdapter();

        final ContactEntryListAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }

        if (!isSearchMode() && mFilter != null) {
            adapter.setFilter(mFilter);
        }

        if (!isLegacyCompatibilityMode()) {
            ((PhoneNumberListAdapter) adapter).setPhotoPosition(mPhotoPosition);
        }
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		return inflater.inflate(R.layout.gn_contact_list_content, null);
    	} else {
    		return inflater.inflate(R.layout.contact_list_content, null);
    	}
    }

    public void pickPhoneNumber(Uri uri) {
        if (mShortcutAction == null) {
            mListener.onPickPhoneNumberAction(uri);
        } else {
            if (isLegacyCompatibilityMode()) {
                throw new UnsupportedOperationException();
            }
            ShortcutIntentBuilder builder = new ShortcutIntentBuilder(getActivity(), this);
            builder.createPhoneNumberShortcutIntent(uri, mShortcutAction);
        }
    }

    public void onShortcutIntentCreated(Uri uri, Intent shortcutIntent) {
        mListener.onShortcutIntentCreated(shortcutIntent);
    }

    @Override
    public void onPickerResult(Intent data) {
        mListener.onPickPhoneNumberAction(data.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACCOUNT_FILTER) {
            if (getActivity() != null) {
                AccountFilterUtil.handleAccountFilterResult(
                        ContactListFilterController.getInstance(getActivity()), resultCode, data);
            } else {
                Log.e(TAG, "getActivity() returns null during Fragment#onActivityResult()");
            }
        }
    }
    
    private Context mContext;
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

    public ContactListFilter getFilter() {
        return mFilter;
    }

    public void setFilter(ContactListFilter filter) {
        if ((mFilter == null && filter == null) ||
                (mFilter != null && mFilter.equals(filter))) {
            return;
        }

        mFilter = filter;
        if (mLoaderStarted) {
            reloadData();
        }
        updateFilterHeaderView();
    }

    public void setPhotoPosition(ContactListItemView.PhotoPosition photoPosition) {
        mPhotoPosition = photoPosition;
        if (!isLegacyCompatibilityMode()) {
            final PhoneNumberListAdapter adapter = (PhoneNumberListAdapter) getAdapter();
            if (adapter != null) {
                adapter.setPhotoPosition(photoPosition);
            }
        } else {
            Log.w(TAG, "setPhotoPosition() is ignored in legacy compatibility mode.");
        }
    }
    
    // ***************************follow lines are Gionee ************************
    //Gionee:huangzy 20120607 add for CR00614801 start
    AbsListIndexer mAlphbetIndexView;
    //Gionee:huangzy 20120607 modify for CR00614801 end
    
    private void gnUpdateFilterHeaderView() {
    	final ContactListFilter filter = getFilter();
        if (mAccountFilterHeader == null) {
            return;
        }
        if (filter == null) {
        	mPaddingView.setVisibility(View.GONE);
        	mAccountFilterHeader.setVisibility(View.GONE);
        	return;
        }
        
        final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPhone(
                mAccountFilterHeader, filter, false, false);
        if (shouldShowHeader) {
            mPaddingView.setVisibility(View.GONE);
            mAccountFilterHeader.setVisibility(View.VISIBLE);
        } else {
            mPaddingView.setVisibility(View.VISIBLE);
            mAccountFilterHeader.setVisibility(View.GONE);
        }
        
        /*final ContactListFilter filter = getFilter();
        if (mAccountFilterHeader == null || filter == null) {
            return;
        }
        // Gionee zhangxx 2012-05-25 modify for CR00608764 begin
        // AccountFilterUtil.updateAccountFilterTitleForPhone(mAccountFilterHeader, filter, false, true);
        mPaddingView.setVisibility(View.GONE);
        if (!isSearchMode()) {
            AccountFilterUtil.updateAccountFilterTitleForPhone(mAccountFilterHeader, filter, false, true);
            mAccountFilterHeader.setVisibility(View.VISIBLE);
        } else {
            mAccountFilterHeader.setVisibility(View.GONE);
        }
        // Gionee zhangxx 2012-05-25 modify for CR00608764 end*/
    }

    //Gionee:huangzy 20120607 modify for CR00614801 start
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	super.onLoadFinished(loader, data);
    	
    	if (ContactsApplication.sIsGnContactsSupport) {
        	bindToAlphdetIndexer();
        }
    	
    	// aurora <wangth> <2013-12-17> add for aurora begin
        if (getQueryString() != null && getQueryString().length() > 0) {
            if (data != null)
            auroraNoMatchView(true, data.getCount());
            if (null != mAlphbetIndexView) {
                mAlphbetIndexView.setVisibility(View.GONE);
            }
        }
        // aurora <wangth> <2013-12-17> add for aurora end
    }
    
    private void bindToAlphdetIndexer() {
    	AuroraListView listView = getListView(); 
    	
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
    
    protected boolean isAlphbetIndexEnabled() {
    	return null != mAlphbetIndexView;
    };
    //Gionee:huangzy 20120607 modify for CR00614801 end
}
