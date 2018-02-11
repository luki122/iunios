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

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GnFilterHeaderClickListener;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.editor.AuroraContactEditorFragment;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.list.ContactListAdapter.ContactQuery;
import com.android.contacts.list.DefaultContactListAdapter.GnContactInfo;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.widget.AbsListIndexer;
import com.android.contacts.widget.AlphbetIndexView;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;

import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import gionee.provider.GnContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import aurora.widget.AuroraButton;
import android.widget.FrameLayout;
import aurora.widget.AuroraListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView; 
// The following lines are provided and maintained by Mediatek Inc.
import java.util.List;
import com.mediatek.contacts.list.service.MultiChoiceRequest;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.animation.AnimationUtils;
import android.content.Loader;
// The previous lines are provided and maintained by Mediatek Inc.
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import com.android.contacts.activities.PeopleActivity;

/**
 * Fragment containing a contact list used for browsing (as compared to
 * picking a contact with one of the PICK intents).
 */
public class DefaultContactBrowseListFragment extends ContactBrowseListFragment {
    private static final String TAG = DefaultContactBrowseListFragment.class.getSimpleName();

    private static final int REQUEST_CODE_ACCOUNT_FILTER = 1;

    private TextView mCounterHeaderView;
    private View mSearchHeaderView;
    private View mAccountFilterHeader;
    private FrameLayout mProfileHeaderContainer;
    private View mProfileHeader;
    private View mProfileMessage;
    private FrameLayout mMessageContainer;
    private TextView mProfileTitle;
	
    private View mPaddingView;

    private class FilterHeaderClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            AccountFilterUtil.startAccountFilterActivityForResult(
                        DefaultContactBrowseListFragment.this, REQUEST_CODE_ACCOUNT_FILTER);
        }
    }
    
    //Gionee:huangzy 20120710 modify CR00614794 start
    private OnClickListener mFilterHeaderClickListener;
    //Gionee:huangzy 20120710 modify CR00614794 end

    public DefaultContactBrowseListFragment() {
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
    }

    @Override
    public CursorLoader createCursorLoader() {
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */
        Log.i(TAG, "createCursorLoader");

        isFinished = false;
        mLoadingContainer.setVisibility(View.GONE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                WAIT_CURSOR_DELAY_TIME);
        /*
         * Bug Fix by Mediatek End.
         */

        return new ProfileAndContactsLoader(getActivity());
    }

    @Override
    protected void onItemClick(int position, long id) {
        viewContact(getAdapter().getContactUri(position));
    }

    @Override
    protected ContactListAdapter createListAdapter() {
        DefaultContactListAdapter adapter = new DefaultContactListAdapter(getContext());
        adapter.setSectionHeaderDisplayEnabled(isSectionHeaderDisplayEnabled());
        adapter.setDisplayPhotos(true);
        return adapter;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		return inflater.inflate(R.layout.gn_contact_list_content, null);
    	} else {
    		return inflater.inflate(R.layout.contact_list_content, null);
    	}
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);

        mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
        // Gionee:huangzy 20120604 add for CR00614041, CR00614029 start
        if (ContactsApplication.sIsGnContactsSupport) {
            mCounterHeaderView = (TextView) mAccountFilterHeader.findViewById(R.id.contacts_count);
        }
        // Gionee:huangzy 20120604 add for CR00614041, CR00614029 end
        
        //Gionee:huangzy 20120710 modify CR00614794 start
        if (ContactsApplication.sIsGnContactsSupport) {
            mFilterHeaderClickListener = new GnFilterHeaderClickListener(getActivity(), mAccountFilterHeader);            
        } else {
            mFilterHeaderClickListener = new FilterHeaderClickListener();
        }
        //Gionee:huangzy 20120710 modify CR00614794 end
        
        /*mAccountFilterHeader.setOnClickListener(mFilterHeaderClickListener);*/

        // Create an empty user profile header and hide it for now (it will be visible if the
        // contacts list will have no user profile).
        addEmptyUserProfileHeader(inflater);
        showEmptyUserProfile(false);
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mLoadingContainer = getView().findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact = (TextView) getView().findViewById(R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) getView().findViewById(R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);
        /*
         * Bug Fix by Mediatek End.
         */

        // Putting the header view inside a container will allow us to make
        // it invisible later. See checkHeaderViewVisibility()
        FrameLayout headerContainer = new FrameLayout(inflater.getContext());
        mSearchHeaderView = inflater.inflate(R.layout.search_header, null, false);
        headerContainer.addView(mSearchHeaderView);
        getListView().addHeaderView(headerContainer, null, false);
        // Gionee zhangxx 2012-05-23 modify for CR00605018 begin
        if (!ContactsApplication.sIsGnContactsSupport) {            
            checkHeaderViewVisibility();
        }
        // Gionee zhangxx 2012-05-23 modify for CR00605018 end
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	getListView().setFastScrollEnabled(!mIsShowAlphbetIndexView);
        	getListView().setFastScrollAlwaysVisible(!mIsShowAlphbetIndexView);

        	//Gionee:huangzy 20120607 add for CR00614801 start
        	mAlphbetIndexView = (AbsListIndexer) getView().findViewById(R.id.gn_alphbet_indexer);
        	mFirstCharPromptTextView = (TextView) getView().findViewById(R.id.gn_first_char_prompt_tv);
        	//Gionee:huangzy 20120607 add for CR00614801 end
        }
        
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    protected void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
        checkHeaderViewVisibility();
        
        //Gionee:huangzy 20120527 add for CR00607983 start
        if (ContactsApplication.sIsGnContactsSupport) {
        	int visibility = flag ? View.GONE : View.VISIBLE;
        	final AbsListIndexer aiv = (AbsListIndexer) getView().findViewById(R.id.gn_alphbet_indexer);
        	if (null != aiv) {
        		aiv.setVisibility(visibility);
        	}
        }
        //Gionee:huangzy 20120527 add for CR00607983 end
    }

    private void checkHeaderViewVisibility() {
        if (mCounterHeaderView != null) {
            mCounterHeaderView.setVisibility(isSearchMode() ? View.GONE : View.VISIBLE);
        }
        updateFilterHeaderView();

        // Hide the search header by default. See showCount().
        if (mSearchHeaderView != null) {
        	// Gionee:huangzy 20120528 modify for CR00608876 start
        	if (ContactsApplication.sIsGnContactsSupport) {
        		if (!isSearchMode()) {
            		mSearchHeaderView.setVisibility(View.GONE);
            	} else {
            		ContactListAdapter adapter = getAdapter();
                    if (adapter != null) {
                        if (adapter.isLoading()) {
                        	mSearchHeaderView.setVisibility(View.GONE);
                        }
                    }
            	}
        	} else {
        		mSearchHeaderView.setVisibility(View.GONE);
        	}
        	// Gionee:huangzy 20120528 modify for CR00608876 end
        }
    }

    @Override
    public void setFilter(ContactListFilter filter) {
        super.setFilter(filter);
        updateFilterHeaderView();
    }

    //Gionee:huangzy 20120710 modify CR00614794 start
    private void updateFilterHeaderView() {
        if (mAccountFilterHeader == null) {
            return; // Before onCreateView -- just ignore it.
        }
        final ContactListFilter filter = getFilter();
        if (filter != null && !isSearchMode()) {
            if (ContactsApplication.sIsGnContactsSupport &&
            		!ContactsApplication.sIsGnGGKJ_V2_0Support) {
                mAccountFilterHeader.setVisibility(View.VISIBLE);
            } else {
                final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPeople(
                        mAccountFilterHeader, filter, false, false);
                mAccountFilterHeader.setVisibility(shouldShowHeader ? View.VISIBLE : View.GONE);                
            }
        } else {
            mAccountFilterHeader.setVisibility(View.GONE);
        }
    }
    //Gionee:huangzy 20120710 modify CR00614794 end

    @Override
    protected void showCount(int partitionIndex, Cursor data) {
//        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
        if (!isSearchMode() && data != null) {
            int count = data.getCount();
            if (count != 0) {
                count -= (mUserProfileExists ? 1: 0);
                String format = getResources().getQuantityText(
                        R.plurals.listTotalAllContacts, count).toString();

                // Gionee:huangzy 20120604 add for CR00614041, CR00614029 start
                if (ContactsApplication.sIsGnContactsSupport) {
                    mCounterHeaderView.setText(String.format(format, count));
                } else {
                 // Do not count the user profile in the contacts count
                    if (mUserProfileExists) {
                        getAdapter().setContactsCount(String.format(format, count));
                    } else {
                        mCounterHeaderView.setText(String.format(format, count));
                    }
                }
                // Gionee:huangzy 20120604 add for CR00614041, CR00614029 end
            } else {
            	if (ContactsApplication.sIsGnContactsSupport) {
            		mCounterHeaderView.setText(getString(R.string.listTotalAllContactsZero));
            		return;
            	}
                ContactListFilter filter = getFilter();
                int filterType = filter != null ? filter.filterType
                        : ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS;
                switch (filterType) {
                    case ContactListFilter.FILTER_TYPE_ACCOUNT:
                        String accountName = "";
                        if (AccountType.ACCOUNT_NAME_SIM.equals(filter.accountName)
                                || AccountType.ACCOUNT_NAME_USIM.equals(filter.accountName)) {
                            accountName = SIMInfoWrapper.getDefault().getSimDisplayNameBySlotId(0);
                        } else if (AccountType.ACCOUNT_NAME_SIM2.equals(filter.accountName)
                                || AccountType.ACCOUNT_NAME_USIM2.equals(filter.accountName)) {
                            accountName = SIMInfoWrapper.getDefault().getSimDisplayNameBySlotId(1);
                        } else {
                            accountName = filter.accountName;
                        }
                        mCounterHeaderView.setText(getString(
                                R.string.listTotalAllContactsZeroGroup, accountName));
                        break;
                    case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY:
                        mCounterHeaderView.setText(R.string.listTotalPhoneContactsZero);
                        break;
                    case ContactListFilter.FILTER_TYPE_STARRED:
                        mCounterHeaderView.setText(R.string.listTotalAllContactsZeroStarred);
                        break;
                    case ContactListFilter.FILTER_TYPE_CUSTOM:
                        mCounterHeaderView.setText(R.string.listTotalAllContactsZeroCustom);
                        break;
                    default:
                        mCounterHeaderView.setText(R.string.listTotalAllContactsZero);
                        break;
                }
//                setSectionHeaderDisplayEnabled(false);
                setVisibleScrollbarEnabled(false);
            }
        } else {
            ContactListAdapter adapter = getAdapter();
            if (adapter == null) {
                return;
            }

            // In search mode we only display the header if there is nothing found
            if (TextUtils.isEmpty(getQueryString()) || !adapter.areAllPartitionsEmpty()) {
                mSearchHeaderView.setVisibility(View.GONE);
            } else {
                TextView textView = (TextView) mSearchHeaderView.findViewById(
                        R.id.totalContactsText);
                ProgressBar progress = (ProgressBar) mSearchHeaderView.findViewById(
                        R.id.progress);
                mSearchHeaderView.setVisibility(View.VISIBLE);
                
                // gionee xuhz 20121129 add for GIUI2.0 start
                if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                	RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                	textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                	textView.setLayoutParams(textParams);
                	textView.setPadding(0, 40, 0, 0);
                	textView.setTextSize(24);
                	
                    /*TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.EmptyListView);
                    int color = a.getColor(R.styleable.EmptyListView_empty_view_text_color,
                            Color.GREEN);
                    textView.setTextColor(color);
                    a.recycle();*/
                }
                // gionee xuhz 20121129 add for GIUI2.0 end
                if (adapter.isLoading()) {
                    textView.setText(R.string.search_results_searching);
                    progress.setVisibility(View.VISIBLE);
                } else {
                    textView.setText(R.string.listFoundAllContactsZero);
                    textView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
                    progress.setVisibility(View.GONE);
                }
            }
            showEmptyUserProfile(false);
        }
    }

    @Override
    protected void setProfileHeader() {
    	if (com.gionee.featureoption.FeatureOption.GN_FEATURE_4_9_0) {
    		return;
    	}
    	
        mUserProfileExists = getAdapter().hasProfile();
        showEmptyUserProfile(!mUserProfileExists && !isSearchMode());
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

    private void showEmptyUserProfile(boolean show) {
        // Changing visibility of just the mProfileHeader doesn't do anything unless
        // you change visibility of its children, hence the call to mCounterHeaderView
        // and mProfileTitle
        mProfileHeaderContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        mProfileHeader.setVisibility(show ? View.VISIBLE : View.GONE);
        // Gionee:huangzy 20120604 add for CR00614041, CR00614029 start
        if (!ContactsApplication.sIsGnContactsSupport) {
            mCounterHeaderView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        // Gionee:huangzy 20120604 add for CR00614041, CR00614029 end
        mProfileTitle.setVisibility(show ? View.VISIBLE : View.GONE);
        mMessageContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        mProfileMessage.setVisibility(show ? View.VISIBLE : View.GONE);

        mPaddingView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * This method creates a pseudo user profile contact. When the returned query doesn't have
     * a profile, this methods creates 2 views that are inserted as headers to the listview:
     * 1. A header view with the "ME" title and the contacts count.
     * 2. A button that prompts the user to create a local profile
     */
    private void addEmptyUserProfileHeader(LayoutInflater inflater) {

        AuroraListView list = (AuroraListView)getListView();
        // Put a header with the "ME" name and a view for the number of contacts
        // The view is embedded in a frame view since you cannot change the visibility of a
        // view in a ListView without having a parent view.
        mProfileHeaderContainer = new FrameLayout(inflater.getContext());
        if (ContactsApplication.sIsGnContactsSupport) {
            mProfileHeader = inflater.inflate(R.layout.gn_user_profile_header, null, false);
        } else {
            mProfileHeader = inflater.inflate(R.layout.user_profile_header, null, false);
            mCounterHeaderView = (TextView) mProfileHeader.findViewById(R.id.contacts_count);
        }

        mProfileTitle = (TextView) mProfileHeader.findViewById(R.id.profile_title);
        mProfileTitle.setAllCaps(true);
        // gionee xuhz 20120605 add start
        if(ContactsApplication.sIsGnContactsSupport) {
        	mProfileTitle.setTextColor(ResConstant.sHeaderTextColor);
        }
        // gionee xuhz 20120605 add end
        
        mProfileHeaderContainer.addView(mProfileHeader);
        list.addHeaderView(mProfileHeaderContainer, null, false);

        // Add a selectable view with a message inviting the user to create a local profile
        mMessageContainer = new FrameLayout(inflater.getContext());
        // gionee xuhz 20120605 add start
        if(ContactsApplication.sIsGnContactsSupport) {
            mProfileMessage = inflater.inflate(R.layout.gn_user_profile_button, null, false);
        } else {
            mProfileMessage = inflater.inflate(R.layout.user_profile_button, null, false);
        }
        // gionee xuhz 20120605 add end
        mMessageContainer.addView(mProfileMessage);
        list.addHeaderView(mMessageContainer, null, true);

        mProfileMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
		        //Gionee:huangzy 20130401 modify for CR00792013 start
                /*Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);*/
                Intent intent = IntentFactory.newInsertContactIntent(true, null, null, null);
		        //Gionee:huangzy 20130401 modify for CR00792013 end
                intent.putExtra(AuroraContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
                startActivity(intent);
            }
        });

        View paddingViewContainer =
                inflater.inflate(R.layout.contact_detail_list_padding, null, false);
        mPaddingView = paddingViewContainer.findViewById(R.id.contact_detail_list_padding);
        mPaddingView.setVisibility(View.GONE);
        // gionee xuhz 20121204 modify for GIUI2.0 start
        if (!ContactsApplication.sIsGnGGKJ_V2_0Support) {
            list.addHeaderView(paddingViewContainer);
        }
        // gionee xuhz 20121204 modify for GIUI2.0 end
    }

    /*
     * Bug Fix by Mediatek Begin. Original Android's code: CR ID: ALPS00115673
     * Descriptions: add wait cursor
     */

    private TextView mLoadingContact;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onLoadFinished   DefaultContactBrowseListFragment");

        isFinished = true;
        mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_out));
        mLoadingContainer.setVisibility(View.GONE);
        mLoadingContact.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
        super.onLoadFinished(loader, data);
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	gnShowEmptyView();
        	gnBindToAlphdetIndexer();
        }
    }

    private ProgressBar mProgress;

    private View mLoadingContainer;

    public static boolean isFinished = false;

    private static final int WAIT_CURSOR_START = 1230;

    private static final long WAIT_CURSOR_DELAY_TIME = 500;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage msg==== " + msg.what);

            switch (msg.what) {

                case WAIT_CURSOR_START:
                    Log.i(TAG, "start WAIT_CURSOR_START !isFinished : " + !isFinished);
                    if (!isFinished) {
                        mLoadingContainer.setVisibility(View.VISIBLE);
                        mLoadingContact.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };
    /*
     * Bug Fix by Mediatek End.
     */

    //*****************************follow lines are Gionee*******************************
    private View mNoContactsEmptyView;
    private AbsListIndexer mAlphbetIndexView;
    private boolean mIsShowAlphbetIndexView = true;
    //Gionee:huangzy 20120607 add for CR00614801 start
    private TextView mFirstCharPromptTextView;
    private boolean mIsNeed2ShowFirstCharPrompt = false;
    //Gionee:huangzy 20120607 add for CR00614801 end
    
    private void gnPrepareEmptyView() {
    	if (!ContactsApplication.sIsGnContactsSupport)
    		return;    	    	
    	int contactsCount = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Contacts._COUNT, 1);
    	
    	// gionee xuhz 20120904 modify for CR00687201 CR00686660 start
    	gnCheckShowEmptyView(contactsCount);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (ContactsApplication.sIsGnContactsSupport) {
    	    GnFilterHeaderClickListener.initAccountFilterLabalAndIcon(getActivity(), getFilter(), mAccountFilterHeader);
    	    
	    	gnPrepareEmptyView();    	
	    	
	    	gnBindToAlphdetIndexer();
	    	
	    	// Gionee zhangxx 2012-05-23 modify for CR00605018 begin
	    	checkHeaderViewVisibility();
	    	// Gionee zhangxx 2012-05-23 modify for CR00605018 end
	    	
	        //Gionee:huangzy 20120607 add for CR00614801 start
	    	mIsNeed2ShowFirstCharPrompt = false;
	        //Gionee:huangzy 20120607 add for CR00614801 end
    	}
    }
    
    private void gnBindToAlphdetIndexer() {
    	if (mIsShowAlphbetIndexView) {
        	mAlphbetIndexView = (AbsListIndexer) getView().findViewById(R.id.gn_alphbet_indexer);
        	if (null != mAlphbetIndexView) {
        	    //Gionee:huangzy 20120607 modify for CR00614801 start
        		mAlphbetIndexView.setList(getListView(), this);
        	    //Gionee:huangzy 20120607 modify for CR00614801 end
        	}
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	Log.i("James", "newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE  : " + 
    			(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE));
    	
    	//Gionee:huangzy 20130309 remove for CR00772323 start
    	/*if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		mIsShowAlphbetIndexView = false;
    	}*/
    	//Gionee:huangzy 20130309 remove for CR00772323 end
    }
    
    // gionee xuhz 20120519 add for CR00596377 start
    private void gnShowEmptyView() {
    	if (!ContactsApplication.sIsGnContactsSupport)
    		return;    	    	
    	
    	// gionee xuhz 20120809 modify for CR00663853 start
        new SimpleAsynTask() {
			@Override
			protected Integer doInBackground(Integer... params) {
				return ContactsUtils.getContactsCount(ContactsApplication.getInstance().getContentResolver());
			}
			
			protected void onPostExecute(Integer result) {
		    	// gionee xuhz 20120904 modify for CR00687201 CR00686660 start
		    	gnCheckShowEmptyView(result.intValue());
		    	// gionee xuhz 20120904 modify for CR00687201 CR00686660 end
			};
        }.execute();
    }
    // gionee xuhz 20120519 add for CR00596377 end
    
    //Gionee:huangzy 20120607 add for CR00614801 start
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            gnShowFirstCharPrompt(false);
            mIsNeed2ShowFirstCharPrompt = false;
        } else {
            mIsNeed2ShowFirstCharPrompt = true;
        }
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        
        gnShowFirstCharPrompt(true);
        
        if (null != mAlphbetIndexView) {
            mAlphbetIndexView.invalidateShowingLetterIndex();
        }
    }
    
    private void gnShowFirstCharPrompt(boolean show) {
        if (null == mFirstCharPromptTextView) {
            return;
        }

        if (show && mIsNeed2ShowFirstCharPrompt) {
            String firstChar = getAdapter().gnGetLastBindNameFristChar();
            if (!(null != mAlphbetIndexView && mAlphbetIndexView.isBusying()) && !TextUtils.isEmpty(firstChar)) {
                if (!firstChar.equals(mFirstCharPromptTextView.getText().toString())) {
                    mFirstCharPromptTextView.setText(firstChar);
                }
                if (mFirstCharPromptTextView.getVisibility() != View.VISIBLE) {
                    mFirstCharPromptTextView.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
        
        if (mFirstCharPromptTextView.getVisibility() == View.VISIBLE) {
            mFirstCharPromptTextView.setVisibility(View.GONE);
        }
    }
    //Gionee:huangzy 20120607 add for CR00614801 end
    
	// gionee xuhz 20120904 add for CR00687201 CR00686660 start
    private void gnCheckShowEmptyView(int contactsCount) {
    	if (0 == contactsCount) {
    		if (null == mNoContactsEmptyView) {
    			ViewStub vs = (ViewStub) getView().findViewById(R.id.no_contacts_stub);    		
    			mNoContactsEmptyView = vs.inflate();
    		} 
    		
    		if (null != mNoContactsEmptyView) {
    		    View eBackupView = mNoContactsEmptyView.findViewById(R.id.empty_view_gn_exchange);
    		    if (null != eBackupView) {
    		    	boolean isGnExchangeExist = ContactsUtils.isGnExchangeExist();
    		        if (!isGnExchangeExist) {
    		            eBackupView.setVisibility(View.INVISIBLE);
    		        }
    		    }    		    
    			mNoContactsEmptyView.setVisibility(View.VISIBLE);
    		}
    	} else {
    		if (null != mNoContactsEmptyView) {
    			mNoContactsEmptyView.setVisibility(View.GONE);
    		}
    	}
    }
	// gionee xuhz 20120904 add for CR00687201 CR00686660 end
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	if (null != menuInfo && menuInfo instanceof AdapterContextMenuInfo) {
    		View targetView = ((AdapterContextMenuInfo)menuInfo).targetView;
    		if (null != targetView && targetView instanceof ContactListItemView) {
    			ContactListItemView civ = (ContactListItemView)targetView;
    			GnContactInfo info = (GnContactInfo)civ.getTag();
    			if (null != info) {
    				menu.setHeaderTitle(info.mName);
        			menu.add(0, R.id.gn_menu_share_contact, 0, R.string.gn_menu_share_contact)
        				.setIntent(IntentFactory.newShareContactIntent(getActivity(), 
        						info.mLookupKey, info.mIsUserProfile));
        			return;
    			}
    		}
    	}
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    }
}
