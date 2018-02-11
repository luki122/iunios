
package com.mediatek.contacts.list;

import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import aurora.widget.AuroraSearchView;
import android.widget.TextView;
import aurora.widget.AuroraSearchView.OnCloseListener;
import aurora.widget.AuroraSearchView.OnQueryTextListener;
import android.widget.TextView.OnEditorActionListener;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ResConstant.IconTpye;
import com.android.contacts.activities.ConfirmAddDetailActivity;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.model.AccountType;
import com.android.contacts.widget.ContextMenuAdapter;
import com.android.contacts.widget.GnSearchEditText;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment.OnSelectedItemsChangeListener;

// Gionee:wangth 20120607 modify for CR00622147 begin
import com.android.contacts.ContactsUtils;
// Gionee:wangth 20120607 modify for CR00622147 end

/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting multiple contacts.
 */

public class ContactListMultiChoiceActivity extends ContactsActivity implements
        View.OnCreateContextMenuListener, OnQueryTextListener, OnClickListener,
        OnCloseListener, OnFocusChangeListener, TextWatcher, OnEditorActionListener {
    private static final String TAG = "ContactsMultiChoiceActivity";

    private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;

    private static final String KEY_ACTION_CODE = "actionCode";
    private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

    // Delay to allow the UI to settle before making search view visible
    private static final int FOCUS_DELAY = 200;

    private ContactsIntentResolverEx mIntentResolverEx;
    protected ContactEntryListFragment<?> mListFragment;

    private int mActionCode = -1;

    private ContactsRequest mRequest;
    private AuroraSearchView mSearchView;

	private GnSearchEditText mGnSearchEdit;
	private View mGnDeleteEditText;

    public ContactListMultiChoiceActivity() {
        mIntentResolverEx = new ContactsIntentResolverEx(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactEntryListFragment<?>) {
            mListFragment = (ContactEntryListFragment<?>) fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {   	
    	
        super.onCreate(savedState);

        if (savedState != null) {
            mActionCode = savedState.getInt(KEY_ACTION_CODE);
        }

        // Extract relevant information from the intent
        mRequest = mIntentResolverEx.resolveIntent(getIntent());
        if (!mRequest.isValid()) {
            Log.d(TAG, "Request is invalid!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        
        checkAllSelectable();

        Intent redirect = mRequest.getRedirectIntent();
        if (redirect != null) {
            // Need to start a different activity
            startActivity(redirect);
            finish();
            return;
        }

        // gionee xuhz 20121214 modify for GIUI2.0 start
  
 setContentView(R.layout.gn_contact_picker);
            
            Drawable searchIcon = getResources().getDrawable(R.drawable.gn_search_icon);

            mGnSearchEdit = (GnSearchEditText) findViewById(R.id.search_text);

            mGnSearchEdit.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
            if (mGnSearchEdit != null) {
            	mGnSearchEdit.addTextChangedListener(this);
            	mGnSearchEdit.setOnEditorActionListener(this);
            	mGnSearchEdit.setOnClickListener(this);
            }
            
            mGnDeleteEditText = findViewById(R.id.delete_text);
            if (null != mGnDeleteEditText) {
            	mGnDeleteEditText.setOnClickListener(this);
            }
 
        // gionee xuhz 20121214 modify for GIUI2.0 end

        configureListFragment();

        // Disable Search View in listview
        AuroraSearchView searchViewInListview = (AuroraSearchView) findViewById(R.id.search_view);
        searchViewInListview.setVisibility(View.GONE);



        showActionBar(false);
        

        	
        		updateTitle(0);
        

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"[onDestroy]");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ACTION_CODE, mActionCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       
    
            gnOnCreateOptionsMenu(menu);
            
            return super.onCreateOptionsMenu(menu);
  
        
        
    }
    
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {       

        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            ((MultiContactsPickerBaseFragment)mListFragment)
            .setOnSelectedItemsChangeListener(new OnSelectedItemsChangeListener() {
                @Override
                public boolean updateSelectedItemsView(
                        int checkedItemsCount, int itemsCount,
                        boolean isSearchMode, int checkedItemsCountInSearch) {
                    gnUpdateSelectedItemsView(checkedItemsCount, itemsCount,
                            isSearchMode, checkedItemsCountInSearch);
                    ContactListMultiChoiceActivity.this.onPrepareOptionsMenu(menu);
                    return true;
                }
            });
        }
    
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        
        if (v.getId() == R.id.action_mode_check_box) {
            if (mListFragment instanceof MultiContactsPickerBaseFragment) {
                MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
                if (mGnIsShowSelectAllState) {
                    fragment.onSelectAll();
                } else {
                    fragment.onClearSelect();
                }
            } else if (mListFragment instanceof DataKindPickerBaseFragment) {
                DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
                if (mGnIsShowSelectAllState) {
                    fragment.onSelectAll();
                } else {
                    fragment.onClearSelect();
                }
            }
        }
        
        switch (v.getId()) {
		case R.id.delete_text:
			if (null != mGnSearchEdit) {
				mGnSearchEdit.setText("");
				mGnDeleteEditText.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
        return;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();        

        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Creates the fragment based on the current request.
     */
    public void configureListFragment() {
        if (mActionCode == mRequest.getActionCode()) {
            return;
        }

        mActionCode = mRequest.getActionCode();
        Log.d(TAG, "configureListFragment action code is " + mActionCode);

        switch (mActionCode) {

            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS: {
                mListFragment = new MultiContactsPickerBaseFragment();
                break;
            }
            
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
		            | ContactsIntentResolverEx.MODE_MASK_SET_STAR_PICKER: {
		        mListFragment = new MultiContactsPickerBaseFragment();
		        break;
		    }
		            
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
		            | ContactsIntentResolverEx.MODE_MASK_REMOVE_STARED_PICKER: {
		        mListFragment = new MultiContactsPickerBaseFragment();
		        break;
		    }

		    //Gionee:huangzy 20120604 add for CR00616160 start
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.GN_MODE_MASK_ADD2GROUP_PICKER: {
                mListFragment = new MultiContactsPickerBaseFragment();
                break;
            }
            //Gionee:huangzy 20120604 add for CR00616160 end

            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_VCARD_PICKER: {
                mListFragment = new ContactsVCardPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER: {
                mListFragment = new MultiContactsDuplicationFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(MultiContactsPickerBaseFragment.FRAGMENT_ARGS, getIntent());
                mListFragment.setArguments(bundle);
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_EMAILS: {
                mListFragment = new MultiEmailsPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_PHONES: {
                mListFragment = new MultiPhoneNumbersPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS: {
                mListFragment = new ContactsMultiDeletionFragment();
                break;
            }

            case ContactsRequest.ACTION_PICK_GROUP_MULTIPLE_CONTACTS: {
                mListFragment = new ContactsGroupMultiPickerFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(MultiContactsPickerBaseFragment.FRAGMENT_ARGS, getIntent());
                mListFragment.setArguments(bundle);
                break;
            }

            case ContactsRequest.ACTION_PICK_MULTIPLE_PHONEANDEMAILS: {
                mListFragment = new MultiPhoneAndEmailsPickerFragment();
                break;
            }
            
            case ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS: {
                mListFragment = new MultiContactsShareFragment();
                break;
            }

            default:
                throw new IllegalStateException("Invalid action code: " + mActionCode);
        }

        mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        mListFragment.setQueryString(mRequest.getQueryString(), false);
        mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);
        mListFragment.setVisibleScrollbarEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    public void startActivityAndForwardResult(final Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        // Forward extras to the new activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuAdapter menuAdapter = mListFragment.getContextMenuAdapter();
        if (menuAdapter != null) {
            return menuAdapter.onContextItemSelected(item);
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            fragment.startSearch(newText);
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            fragment.startSearch(newText);
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Gionee:wangth 20120607 modify for CR00622147 begin
        /*
        return false;
        */
        if (mSearchView != null) {
            InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            
            mSearchView.clearFocus();
            return true;
        } else {
            return false;
        }
        // Gionee:wangth 20120607 modify for CR00622147 end
    }

    @Override
    public boolean onClose() {
        if (mSearchView == null) {
            return false;
        }
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        showActionBar(false);
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            fragment.updateSelectedItemsView();
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            fragment.updateSelectedItemsView();
        }
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()) {
            case R.id.search_view: {
                if (hasFocus) {
                    showInputMethod(mSearchView.findFocus());
                }
            }
        }
    }

    private void showInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(view, 0)) {
                Log.w(TAG, "Failed to show soft input method.");
            }
        }
    }

    public void returnPickerResult(Uri data) {
        Intent intent = new Intent();
        intent.setData(data);
        returnPickerResult(intent);
    }

    public void returnPickerResult(Intent intent) {
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBACTIVITY_ADD_TO_EXISTING_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    startActivity(data);
                }
                finish();
            }
        }

        if (resultCode == ContactImportExportActivity.RESULT_CODE) {
            finish();
        }
    }

    public void onBackPressed() {
        if (mSearchView != null && !mSearchView.isFocused()) {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            showActionBar(false);
            if (mListFragment instanceof MultiContactsPickerBaseFragment) {
                MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
                fragment.updateSelectedItemsView();
            } else if (mListFragment instanceof DataKindPickerBaseFragment) {
                DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
                fragment.updateSelectedItemsView();
            }
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        
        Log.i(TAG,"[onConfigurationChanged]" +newConfig);
        super.onConfigurationChanged(newConfig);
        //do nothing
    }

    private void showActionBar(boolean searchMode) {
        ActionBar actionBar = getActionBar();
        if (searchMode) {
            // gionee xuhz 20120529 modify for remove home icon from ActionBar start
            final View searchViewContainer;
                  
                searchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.gn_custom_action_bar, null);
           
            
            mSearchView = (AuroraSearchView) searchViewContainer.findViewById(R.id.search_view);

            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setQueryHint(getString(R.string.hint_findContacts));
            mSearchView.setIconified(false);

            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setOnQueryTextFocusChangeListener(this);
            // gionee xuhz 20121205 add for GIUI2.0 start
     
                mSearchView.onActionViewExpanded();
            
            // gionee xuhz 20121205 add for GIUI2.0 end
            actionBar.setCustomView(searchViewContainer, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
                
            ImageView mGnAciontUpView = (ImageView)searchViewContainer.findViewById(R.id.gn_action_up);
            if (mGnAciontUpView != null) {
                mGnAciontUpView.setVisibility(View.VISIBLE);
                mGnAciontUpView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
            
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
     
        } else {
     
        		gnShowNormalActionBar();

        
  
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:               
            	onBackPressed();
                return true;
                
            default:
                break;
        }
        
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            switch (item.getItemId()) {
  

                case R.id.menu_option:
                    fragment.onOptionAction();
                    if (fragment instanceof MultiContactsDuplicationFragment) {
                        Log.d(TAG, "Send result for copy action");
                        setResult(ContactImportExportActivity.RESULT_CODE);
                    }
                    break;
                    
                // Gionee:xuhz 20130105 add for CR00757469 start
                case R.id.menu_option_share:
                	fragment.onOptionShareAction();
                	break;
                // Gionee:xuhz 20130105 add for CR00757469 end

                default:
                    break;
            }
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            switch (item.getItemId()) {


                case R.id.menu_option:
                    fragment.onOptionAction();
                    break;

                default:
                    break;
            }
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // ----------------bellow lines are add by GIONEE-------------

    //Gionee:huangzy 20120604 add for CR00616160 start
    private MenuItem mGnOptionMenuItem;
    private String mGnActionTitle;
    private boolean mGnIsShowSelectAllState = true;
    // gionee xuhz 20120703 add for CR00628836 start
    private boolean mGnIsSelectAllEnable = true;

	private TextView mActionModeTitle;

	private CheckBox mSelectAllCheckBox;
    // gionee xuhz 20120703 add for CR00628836 end
	
    // Gionee:xuhz 20130105 add for CR00757469 start
	private MenuItem mGnOptionShareMenuItem;
    // Gionee:xuhz 20130105 add for CR00757469 end
    
    public boolean gnOnCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gn_list_multichoice, menu);
        
        mGnOptionMenuItem = menu.findItem(R.id.menu_option);
        
        // Gionee:xuhz 20130105 add for CR00757469 start
        mGnOptionShareMenuItem = menu.findItem(R.id.menu_option_share);
        mGnOptionShareMenuItem.setVisible(false);
        // Gionee:xuhz 20130105 add for CR00757469 end
        
        int actionCode = mRequest.getActionCode();
        switch (actionCode) {
        //Gionee <jianghuan> <2013-08-13> add for CR00855051 begin
        case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_VCARD_PICKER:
        //Gionee <jianghuan> <2013-08-13> add for CR00855051 end
        case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_SET_STAR_PICKER:
            mGnOptionMenuItem.setTitle(R.string.gn_add);
            mGnOptionMenuItem.setIcon(ResConstant.getIconRes(IconTpye.ADD));
            break;
        case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_REMOVE_STARED_PICKER:
            mGnOptionMenuItem.setTitle(R.string.gn_remove);
            break;
        case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:
            mGnOptionMenuItem.setTitle(android.R.string.copy);
            mGnOptionMenuItem.setIcon(ResConstant.getIconRes(IconTpye.COPY));
            break;
        case ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS:
            mGnOptionMenuItem.setTitle(R.string.menu_deleteContact);
            mGnOptionMenuItem.setIcon(ResConstant.getIconRes(IconTpye.TRASH));
            // Gionee:xuhz 20130105 add for CR00757469 start
            mGnOptionShareMenuItem.setVisible(true);
            mGnOptionShareMenuItem.setTitle(R.string.menu_share);
            mGnOptionShareMenuItem.setIcon(ResConstant.getIconRes(IconTpye.SHARE));
            // Gionee:xuhz 20130105 add for CR00757469 end
            break;
        case ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS:
            mGnOptionMenuItem.setTitle(R.string.menu_share);
            mGnOptionMenuItem.setIcon(ResConstant.getIconRes(IconTpye.SHARE));
            break;
        case ContactsRequest.ACTION_PICK_GROUP_MULTIPLE_CONTACTS:
            mGnOptionMenuItem.setTitle(R.string.move);
            mGnOptionMenuItem.setIcon(ResConstant.getIconRes(IconTpye.MOVE));
            break;            
        case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
				| ContactsIntentResolverEx.GN_MODE_MASK_ADD2GROUP_PICKER:
	        mGnOptionMenuItem.setTitle(R.string.gn_add);
            mGnOptionMenuItem.setIcon(ResConstant.getIconRes(IconTpye.ADD));
            break;
        }
        
        return true;
    }
    
    private void gnUpdateSelectedItemsView(int checkedItemsCount, int itemsCount, 
            boolean isSearchMode,int checkedItemsCountInSearch) {
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            // gionee xuhz 20120703 add for CR00628836 start
            if (itemsCount == 0) {
                mGnIsSelectAllEnable = false;
            } else {
                mGnIsSelectAllEnable = true;
            }
            // gionee xuhz 20120703 add for CR00628836 end
        	
            if (!isSearchMode) {
                if (itemsCount == checkedItemsCount) {
                    gnShowSelectAllMenu(false);
                } else if (false == mGnIsShowSelectAllState){
                    gnShowSelectAllMenu(true);
                }
            } else {
                if (itemsCount == checkedItemsCountInSearch) {
                    gnShowSelectAllMenu(false);
                } else if (false == mGnIsShowSelectAllState){
                    gnShowSelectAllMenu(true);
                }
            }
            
            if (null != mGnOptionMenuItem) {
                // gionee xuhz 20120626 modify for CR00627879 start
                if (!isSearchMode) {
                    // gionee xuhz 20120829 add for CR00681613 start
                    if (checkedItemsCount > 0) {
                        mGnOptionMenuItem.setEnabled(true);
                        // Gionee:xuhz 20130105 add for CR00757469 start
                        if (mGnOptionShareMenuItem != null && mGnOptionShareMenuItem.isVisible()) {
                        	mGnOptionShareMenuItem.setEnabled(true);
                        }
                        // Gionee:xuhz 20130105 add for CR00757469 end
                    } else {
                        mGnOptionMenuItem.setEnabled(false);
                        // Gionee:xuhz 20130105 add for CR00757469 start
                        if (mGnOptionShareMenuItem != null && mGnOptionShareMenuItem.isVisible()) {
                        	mGnOptionShareMenuItem.setEnabled(false);
                        }
                        // Gionee:xuhz 20130105 add for CR00757469 end
                    }
                    // gionee xuhz 20120829 add for CR00681613 end
                
                		updateTitle(checkedItemsCount);
                	
                } else {
                    // gionee xuhz 20120829 add for CR00681613 start
                    if (checkedItemsCountInSearch > 0) {
                        mGnOptionMenuItem.setEnabled(true);
                        // Gionee:xuhz 20130105 add for CR00757469 start
                        if (mGnOptionShareMenuItem != null && mGnOptionShareMenuItem.isVisible()) {
                        	mGnOptionShareMenuItem.setEnabled(true);
                        }
                        // Gionee:xuhz 20130105 add for CR00757469 end
                    } else {
                        mGnOptionMenuItem.setEnabled(false);
                        // Gionee:xuhz 20130105 add for CR00757469 start
                        if (mGnOptionShareMenuItem != null && mGnOptionShareMenuItem.isVisible()) {
                        	mGnOptionShareMenuItem.setEnabled(false);
                        }
                        // Gionee:xuhz 20130105 add for CR00757469 end
                    }
                    // gionee xuhz 20120829 add for CR00681613 end
               
                		updateTitle(checkedItemsCountInSearch);
                	
                }
                // gionee xuhz 20120626 modify for CR00627879 end
            }
            
//            gnSetActionBarTitle();
        }               
    }
    
    private void gnSetActionBarTitle() {
    	// gionee xuhz 20120706 remove for CR00640138 start
    	/*
        TextView selectedItemsView = (TextView) getActionBar().getCustomView().findViewById(R.id.select_items);
        if (selectedItemsView == null) {
            Log.e(TAG, "Load view resource error!");
            return;
        }*/
    	// gionee xuhz 20120706 remove for CR00640138 end
        
        if (null == mGnActionTitle) {
            int actionCode = mRequest.getActionCode();
            switch (actionCode) {
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_SET_STAR_PICKER:
                mGnActionTitle = getString(R.string.gn_set_star);
                break;
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_REMOVE_STARED_PICKER:
                mGnActionTitle = getString(R.string.gn_remove_star);
                break;
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.GN_MODE_MASK_ADD2GROUP_PICKER:
                mGnActionTitle = getString(R.string.gn_add_to_group);
                break;
            case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                    | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:
                mGnActionTitle = getString(R.string.imexport_title);
                break;
            case ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS:
                // Gionee:wangth 20121128 modify for CR00736648 begin
                mGnActionTitle = getString(R.string.gn_menu_batch_options);
                // Gionee:wangth 20121128 modify for CR00736648 end
                break;
            case ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS:
                mGnActionTitle = getString(R.string.share_visible_contacts);
                break;
            case ContactsRequest.ACTION_PICK_GROUP_MULTIPLE_CONTACTS:
                mGnActionTitle = getString(R.string.gn_add_to_group);
                break;            
            }
        }
        
    	// gionee xuhz 20120706 modify for CR00640138 start
        getActionBar().setTitle(mGnActionTitle);
        /*
        if (!selectedItemsView.getText().equals(mGnActionTitle)) {
            selectedItemsView.setText(mGnActionTitle);
        }        
        */
    	// gionee xuhz 20120706 modify for CR00640138 end
    }
    
    private void gnShowSelectAllMenu(boolean show) {
        mGnIsShowSelectAllState = show;
    	//Gionee:huangzy 20130328 remove for CR00786934 start
        /*invalidateOptionsMenu();*/
    	//Gionee:huangzy 20130328 remove for CR00786934 end
        
        mSelectAllCheckBox.setChecked(!show);
        if (!mGnIsSelectAllEnable) {
        	mSelectAllCheckBox.setChecked(false);
            mSelectAllCheckBox.setEnabled(false);
            
        } else {
            mSelectAllCheckBox.setEnabled(true);
        }
    }
    
    //Gionee:huangzy 20120604 add for CR00616160 end
    
    // gionee xuhz 20120706 modify for CR00640138 start
    private void gnShowNormalActionBar() {
    	
        ActionBar actionBar = getActionBar();
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.gn_multichoice_custom_action_bar,
                null);

        mSelectAllCheckBox = (CheckBox) customActionBarView
				.findViewById(R.id.action_mode_check_box);
        mSelectAllCheckBox.setChecked(false);
        mSelectAllCheckBox.setOnClickListener(this);
        mSelectAllCheckBox.setVisibility(mAllSelectable ? View.VISIBLE : View.GONE);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(customActionBarView, 
				new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.MATCH_PARENT, Gravity.RIGHT));
        mSearchView = null;
    }
    // gionee xuhz 20120706 modify for CR00640138 end

    private void hideSoftInput() {
    	if (mGnSearchEdit != null) {
            InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mGnSearchEdit.getWindowToken(), 0);
            }
    	}
    }
    
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
        	hideSoftInput();
            return true;
        }
        return false;
	}

	@Override
	public void afterTextChanged(Editable s) {
        if (mListFragment instanceof MultiContactsPickerBaseFragment) {
            MultiContactsPickerBaseFragment fragment = (MultiContactsPickerBaseFragment) mListFragment;
            fragment.startSearch(s.toString());
        } else if (mListFragment instanceof DataKindPickerBaseFragment) {
            DataKindPickerBaseFragment fragment = (DataKindPickerBaseFragment) mListFragment;
            fragment.startSearch(s.toString());
        }
        
        if (null != mGnDeleteEditText) {
        	mGnDeleteEditText.setVisibility(0 == s.length() ? View.GONE : View.VISIBLE);
        }
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	
    protected void updateTitle(int count) {    	
    		String title = null;
    		
        	title = String.format(getString(R.string.gn_selected_count), count);
    		    
        	ActionBar actionBar = getActionBar();
        	if (null != actionBar) {
        		actionBar.setTitle(title);
        	} else {
        		actionBar.setTitle(title);
        	}
    }
    
    private boolean mAllSelectable = true;
    private void checkAllSelectable() {
    	int actionCode = mRequest.getActionCode();
        switch (actionCode) {
        case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
        		| ContactsIntentResolverEx.MODE_MASK_SET_STAR_PICKER:
            mAllSelectable = false;
		    break;
		case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
		        | ContactsIntentResolverEx.MODE_MASK_REMOVE_STARED_PICKER:
		    mAllSelectable = false;
		    break;
		case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
		        | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:		    
		    break;
		case ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS:
		    break;
		case ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS:
		    break;
		case ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
        	| ContactsIntentResolverEx.GN_MODE_MASK_ADD2GROUP_PICKER:
			mAllSelectable = false;
		    break;
        }
    }
}
