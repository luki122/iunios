/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.util.Arrays;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactTileLoaderFactory;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.list.ContactTileAdapter.DisplayType;
import com.android.contacts.model.AccountType;
import com.mediatek.contacts.list.ContactsIntentResolverEx;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.mediatek.contacts.util.ContactsIntent;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraListView;
import android.widget.TextView; 
import android.widget.Toast;
// The following lines are provided and maintained by Mediatek Inc.
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

// The previous lines are provided and maintained by Mediatek Inc.
/**
 * Fragment containing a list of starred contacts followed by a list of frequently contacted.
 *
 * TODO: Make this an abstract class so that the favorites, frequent, and group list functionality
 * can be separated out. This will make it easier to customize any of those lists if necessary
 * (i.e. adding header views to the ListViews in the fragment). This work was started
 * by creating {@link ContactTileFrequentFragment}.
 */
public class ContactTileListFragment extends Fragment {
    private static final String TAG = ContactTileListFragment.class.getSimpleName();

    public interface Listener {
        public void onContactSelected(Uri contactUri, Rect targetRect);
    }

    private static int LOADER_CONTACTS = 1;

    private Listener mListener;
    private ContactTileAdapter mAdapter;
    private DisplayType mDisplayType;
    private TextView mEmptyView;
    private AuroraListView mListView;
    
    // gionee xuhz 20120515 add start
    private View mGnEmptyView;
    private TextView mGnEmptyTextView;
    // gionee xuhz 20120515 add end
    
    public static final int REQUEST_PICK_MULTIPLE_CONTACTS_SET_STAR = 11;
    public static final int REQUEST_PICK_MULTIPLE_CONTACTS_REMOVE_STARRED = 12;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Resources res = getResources();
        int columnCount = res.getInteger(R.integer.contact_tile_column_count);

        mAdapter = new ContactTileAdapter(activity, mAdapterListener,
                columnCount, mDisplayType);
        mAdapter.setPhotoLoader(ContactPhotoManager.getInstance(activity));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // gionee xuhz 20120515 modify start
        if (ContactsApplication.sIsGnContactsSupport) {
            return inflateAndSetupView(inflater, container, savedInstanceState,
                    R.layout.gn_contact_tile_list_for_starred);
        } else {
            return inflateAndSetupView(inflater, container, savedInstanceState,
                    R.layout.contact_tile_list);
        }
        // gionee xuhz 20120515 modify end
    }

    protected View inflateAndSetupView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState, int layoutResourceId) {
        View listLayout = inflater.inflate(layoutResourceId, container, false);

        // gionee xuhz 20120515 modify start
        if (ContactsApplication.sIsGnContactsSupport) {
            mGnEmptyView = (View) listLayout.findViewById(R.id.empty);
            mGnEmptyTextView = (TextView) listLayout.findViewById(R.id.contact_tile_list_empty);
        } else {
            mEmptyView = (TextView) listLayout.findViewById(R.id.contact_tile_list_empty);
        }
        // gionee xuhz 20120515 modify end

        mListView = (AuroraListView) listLayout.findViewById(R.id.contact_tile_list);

        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mAdapter);
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
         * ALPS00115673 Descriptions: add wait cursor
         */

        mLoadingContainer = listLayout.findViewById(R.id.loading_container);
        mLoadingContact = (TextView) listLayout.findViewById(R.id.loading_contact);
        mLoadingContact.setVisibility(View.GONE);
        mProgress = (ProgressBar) listLayout.findViewById(R.id.progress_loading_contact);
        mProgress.setVisibility(View.GONE);

        /*
         * Bug Fix by Mediatek End.
         */

        return listLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: Use initLoader?
        getLoaderManager().restartLoader(LOADER_CONTACTS, null, mContactTileLoaderListener);
    }

    public void setColumnCount(int columnCount) {
        mAdapter.setColumnCount(columnCount);
    }

    public void setDisplayType(DisplayType displayType) {
        mDisplayType = displayType;
        mAdapter.setDisplayType(mDisplayType);
    }

    public void enableQuickContact(boolean enableQuickContact) {
        mAdapter.enableQuickContact(enableQuickContact);
    }

    private final LoaderManager.LoaderCallbacks<Cursor> mContactTileLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */

            Log.i(TAG, "onCreateLoader ContactTileListFragment");
            isFinished = false;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(WAIT_CURSOR_START),
                    WAIT_CURSOR_DELAY_TIME);
            /*
             * Bug Fix by Mediatek End.
             */
            
    		// gionee xuhz 20121108 add for CR00721861 start
    		boolean isGnGuestMode = Settings.Secure.getInt(
    				getActivity().getContentResolver(), ContactsApplication.GN_GUEST_MODE, 0) == 1;
    		// gionee xuhz 20121108 add for CR00721861 end
            
            switch (mDisplayType) {
              case STARRED_ONLY:
                  return ContactTileLoaderFactory.createStarredLoader(getActivity());
              case STREQUENT:
          		// gionee xuhz 20121108 modify for CR00721861 start
          		if (isGnGuestMode) {
                    return ContactTileLoaderFactory.createStarredLoader(getActivity());
          		} else {
                    return ContactTileLoaderFactory.createStrequentLoader(getActivity());
          		}
          		// gionee xuhz 20121108 modify for CR00721861 end
              case STREQUENT_PHONE_ONLY:
                  return ContactTileLoaderFactory.createStrequentPhoneOnlyLoader(getActivity());
              case FREQUENT_ONLY:
                  return ContactTileLoaderFactory.createFrequentLoader(getActivity());
              default:
                  throw new IllegalStateException(
                      "Unrecognized DisplayType " + mDisplayType);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */

            Log.i(TAG, "onloadfinished11111111111111111111111");

            isFinished = true;
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));
            mLoadingContainer.setVisibility(View.GONE);
            mLoadingContact.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
            /*
             * Bug Fix by Mediatek End.
             */
            mAdapter.setContactCursor(data);
            // gionee xuhz 20120515 modify start
            if (ContactsApplication.sIsGnContactsSupport) {
                mGnEmptyTextView.setText(getEmptyStateText());
                mListView.setEmptyView(mGnEmptyView);
            } else {
                mEmptyView.setText(getEmptyStateText());
                mListView.setEmptyView(mEmptyView);
            }
            // gionee xuhz 20120515 modify end
            
            //Gionee:huangzy 20100717 add for CR00650878 start
            getActivity().invalidateOptionsMenu();
            //Gionee:huangzy 20100717 add for CR00650878 end

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    private String getEmptyStateText() {
        String emptyText;
        switch (mDisplayType) {
            case STREQUENT:
            case STREQUENT_PHONE_ONLY:
            case STARRED_ONLY:
                emptyText = getString(R.string.listTotalAllContactsZeroStarred);
                break;
            case FREQUENT_ONLY:
            case GROUP_MEMBERS:
                emptyText = getString(R.string.noContacts);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized DisplayType " + mDisplayType);
        }
        return emptyText;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private ContactTileAdapter.Listener mAdapterListener =
            new ContactTileAdapter.Listener() {
        @Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
            if (mListener != null) {
                mListener.onContactSelected(contactUri, targetRect);
            }
        }
        
        // gionee xuhz 20130117 add for CR00765218 start
        public void onCreateContextMenu(Uri lookupUri, String displayName, int simIndex){
        	return;
        }
        // gionee xuhz 20130117 add for CR00765218 end
    };

    /*
     * Bug Fix by Mediatek Begin. Original Android's code: CR ID: ALPS00115673
     * Descriptions: add wait cursor
     */

    private View mLoadingContainer;

    private TextView mLoadingContact;

    private ProgressBar mProgress;

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
    
    protected void addFavoriteMembersRequest() {
    	Intent intent = null;
    	
    	intent = new Intent(ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS);
        intent.setType(Phone.CONTENT_TYPE);        
        
        ContactListFilter filter = ContactListFilter.createAccountFilter(AccountType.ACCOUNT_TYPE_LOCAL_PHONE, 
        		AccountType.ACCOUNT_NAME_LOCAL_PHONE, null, null);
        intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER, filter);
        intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO, "withoutStarred/");
        intent.putExtra(AccountFilterActivity.EXTRA_ONLY_STARRBLE_ACCOUNT, true);
        intent.putExtra(ContactsIntentResolverEx.EXTRA_KEY_REQUEST_TYPE, ContactsIntentResolverEx.REQ_TYPE_SET_STAR_PICKER);
        
    	startActivityForResult(intent, REQUEST_PICK_MULTIPLE_CONTACTS_SET_STAR);
	}
    
    protected void removeFavoriteMembersRequest() {
    	Intent intent = null;
    	
    	intent = new Intent(ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS);
        intent.setType(Phone.CONTENT_TYPE);        
        
        ContactListFilter filter = ContactListFilter.createAccountFilter(AccountType.ACCOUNT_TYPE_LOCAL_PHONE, 
        		AccountType.ACCOUNT_NAME_LOCAL_PHONE, null, null);
        intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER, filter);
        intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO, "onlyStarred/");
        intent.putExtra(AccountFilterActivity.EXTRA_ONLY_STARRBLE_ACCOUNT, true);
        intent.putExtra(ContactsIntentResolverEx.EXTRA_KEY_REQUEST_TYPE, ContactsIntentResolverEx.REQ_TYPE_REMOVE_STAR_PICKER);
        
    	startActivityForResult(intent, REQUEST_PICK_MULTIPLE_CONTACTS_REMOVE_STARRED);
	}
    
    protected void setMembersStarred(long[] rawContactIds, boolean star) {
        //Gionee:huangzy 20120621 add for CR00624998 start
        if (null == rawContactIds) {
            return;
        }
        int len = rawContactIds.length;
        if (len > MAX_BATCH_HANDLE_NUM) {
            rawContactIds = subIdsIfNeed(rawContactIds);
            int textRes = star ? R.string.gn_max_add_num : R.string.gn_max_remove_num;
            showSubMsg(String.format(getString(textRes), MAX_BATCH_HANDLE_NUM));
        }
        //Gionee:huangzy 20120621 add for CR00624998 end

    	Intent setStarIntent = ContactSaveService.createSetStarredIntent(getActivity(), rawContactIds, star);
    	getActivity().startService(setStarIntent);
    }
    
    //Gionee:huangzy 20120621 add for CR00624998 start
    private final int MAX_BATCH_HANDLE_NUM = ContactsApplication.MAX_BATCH_HANDLE_NUM;
    protected long[] subIdsIfNeed(long[] ids) {
        if (null == ids || ids.length < MAX_BATCH_HANDLE_NUM) {
            return ids;
        }
        
        return Arrays.copyOfRange(ids, 0, MAX_BATCH_HANDLE_NUM);
    }
    
    protected void showSubMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
    //Gionee:huangzy 20120621 add for CR00624998 end
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
		case R.id.gn_menu_add_to_favorite:
			addFavoriteMembersRequest();
			return true;
		
		case R.id.gn_menu_remove_from_favorite:
			removeFavoriteMembersRequest();
			return true;
			
	    //Gionee:huangzy 20100717 add for CR00650878 start
		case R.id.gn_menu_clear_frequent_contacted:
		    gnClearFrequentContacted();
		    return true;
		//Gionee:huangzy 20100717 add for CR00650878 end

		default:
			break;
		}
    	
    	return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (Activity.RESULT_OK == resultCode) {
    		Bundle extra = data.getExtras();        			
			long[] contactsIds = extra.getLongArray(MultiContactsPickerBaseFragment.EXTRA_ID_ARRAY);
			
    		switch (requestCode) {
        		case REQUEST_PICK_MULTIPLE_CONTACTS_SET_STAR:
        			setMembersStarred(contactsIds, true);
        			break;
        		case REQUEST_PICK_MULTIPLE_CONTACTS_REMOVE_STARRED:
        			setMembersStarred(contactsIds, false);        			
        			break;
    		}
    	}
    }
    
    //Gionee:huangzy 20100717 add for CR00650878 start
    protected void gnClearFrequentContacted() {
        new AuroraAlertDialog.Builder(getActivity())
        .setTitle(R.string.gn_clear_frequent_contacted_title)
        .setMessage(R.string.gn_clearFrequentContactedConfirmation)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContactsUtils.clearFrequentContacted(getActivity().getContentResolver());
                getLoaderManager().restartLoader(LOADER_CONTACTS, null, mContactTileLoaderListener);
            }           
        })
        .setNegativeButton(android.R.string.no, null)
        .show();
    }
    //Gionee:huangzy 20100717 add for CR00650878 end
    
    public boolean showingFrequent() {
        boolean hasFrequent = false;
        if (null != mAdapter) {
            hasFrequent = mAdapter.getFrequentHeaderPosition() != mAdapter.getCount();            
        }
        
        return hasFrequent;
    }

	//gionee xuhz 20120917 modify for CR00693553 start
    public boolean getHasStaredPeople() {
        boolean hasStaredPeople = false;
        if (null != mAdapter) {
        	hasStaredPeople = mAdapter.getFrequentHeaderPosition() > 0;            
        }
        
        return hasStaredPeople;
    }
	//gionee xuhz 20120905 modify for CR00693553 end
}
