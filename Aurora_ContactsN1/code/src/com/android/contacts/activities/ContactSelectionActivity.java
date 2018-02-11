/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.contacts.activities;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactPickerFragment;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.DirectoryListLoader;
import com.android.contacts.list.EmailAddressPickerFragment;
import com.android.contacts.list.OnContactPickerActionListener;
import com.android.contacts.list.OnEmailAddressPickerActionListener;
import com.android.contacts.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.list.OnPostalAddressPickerActionListener;
import com.android.contacts.list.PhoneNumberPickerFragment;
import com.android.contacts.list.PostalAddressPickerFragment;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.widget.ContextMenuAdapter;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import aurora.widget.AuroraSearchView;
//import aurora.widget.AuroraSearchView.OnCloseListener;
//import aurora.widget.AuroraSearchView.OnQueryTextListener;
import aurora.widget.AuroraActionBar;
import java.util.Set;

/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting one.
 */
public class ContactSelectionActivity extends ContactsActivity
        implements View.OnCreateContextMenuListener, /*OnQueryTextListener,*/
                /*OnCloseListener,*/ OnFocusChangeListener {
    private static final String TAG = "ContactSelectionActivity";

    private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;

    private static final String KEY_ACTION_CODE = "actionCode";
    private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

    // Delay to allow the UI to settle before making search view visible
    private static final int FOCUS_DELAY = 200;

    private ContactsIntentResolver mIntentResolver;
    private ContactEntryListFragment<?> mListFragment;

    private int mActionCode = -1;
    // add by mediatek
    private String fromWhereActivity = "";

    private ContactsRequest mRequest;
    private AuroraSearchView mSearchView;
    // gionee xuhz 20120719 modify for CR00651556 start
    private ImageView mGnAciontUpView;
    // gionee xuhz 20120719 modify for CR00651556 end
    


    public ContactSelectionActivity() {
        mIntentResolver = new ContactsIntentResolver(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactEntryListFragment<?>) {
            mListFragment = (ContactEntryListFragment<?>) fragment;
            setupActionListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        
        super.onCreate(savedState);

        if (savedState != null) {
            mActionCode = savedState.getInt(KEY_ACTION_CODE);
        }

        // Extract relevant information from the intent
        mRequest = mIntentResolver.resolveIntent(getIntent());
        if (!mRequest.isValid()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Intent redirect = mRequest.getRedirectIntent();
        if (redirect != null) {
            // Need to start a different activity
            startActivity(redirect);
            finish();
            return;
        }

        configureActivityTitle();

        //aurora <wangth> <2013-9-23> modify for auroro ui begin
        /*
        setContentView(R.layout.contact_picker);
        */
        setAuroraContentView(R.layout.aurora_contact_picker,
                AuroraActionBar.Type.Normal);
        addSearchviewInwindowLayout();
        
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.contactPickerActivityTitle);
        //aurora <wangth> <2013-9-23> modify for auroro ui end

        // add by meidatek
        Intent intent = getIntent();
        fromWhereActivity = (String)intent.getExtra("fromWhere");
        Log.i(TAG, "-------------" +fromWhereActivity+ "-------------");
        
        // Gionee:wangth 20120627 modify for CR00627787 begin
        /*
        if (mActionCode != mRequest.getActionCode()) {
            mActionCode = mRequest.getActionCode();
            configureListFragment();
        }
        */
        mActionCode = mRequest.getActionCode();
        configureListFragment();
        // Gionee:wangth 20120627 modify for CR00627787 end

        prepareSearchViewAndActionBar();
    }

    private boolean shouldShowCreateNewContactButton() {
        return (mActionCode == ContactsRequest.ACTION_INSERT_OR_EDIT_CONTACT
                || (mActionCode == ContactsRequest.ACTION_PICK_OR_CREATE_CONTACT
                        && !mRequest.isSearchMode()));
    }

    private void prepareSearchViewAndActionBar() {
        // Postal address pickers (and legacy pickers) don't support search, so just show
        // "HomeAsUp" button and title.
        if (mRequest.getActionCode() == ContactsRequest.ACTION_PICK_POSTAL ||
                mRequest.isLegacyCompatibilityMode()) {
//            findViewById(R.id.search_view).setVisibility(View.GONE);
            return;
        }


        // Clear focus and suppress keyboard show-up.
        //  aurora <wangth> <2013-12-9> remove for aurora begin
//        mSearchView.clearFocus();
        //  aurora <wangth> <2013-12-9> remove for aurora end
    }
    
  
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // If we want "Create New Contact" button but there's no such a button in the layout,
        // try showing a menu for it.
        if (shouldShowCreateNewContactButton()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.contact_picker_options, menu);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Go back to previous screen, intending "cancel"
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.create_new_contact: {
                startCreateNewContactActivity();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ACTION_CODE, mActionCode);
    }

    private void configureActivityTitle() {
        if (mRequest.getActivityTitle() != null) {
            setTitle(mRequest.getActivityTitle());
            return;
        }

        int actionCode = mRequest.getActionCode();
        switch (actionCode) {
            case ContactsRequest.ACTION_INSERT_OR_EDIT_CONTACT: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_CONTACT: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_OR_CREATE_CONTACT: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CONTACT: {
                setTitle(R.string.shortcutActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_PHONE: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_EMAIL: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CALL: {
                setTitle(R.string.callShortcutActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_SMS: {
                setTitle(R.string.messageShortcutActivityTitle);
                break;
            }

            case ContactsRequest.ACTION_PICK_POSTAL: {
                setTitle(R.string.contactPickerActivityTitle);
                break;
            }
        }
    }

    /**
     * Creates the fragment based on the current request.
     */
    public void configureListFragment() {
        Log.i(TAG, "======= mActionCode ---- " + mActionCode + " ======");
        switch (mActionCode) {
            case ContactsRequest.ACTION_INSERT_OR_EDIT_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setEditMode(true);
                fragment.setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_NONE);
                // add by mediatek
//                if (fromWhereActivity != null && fromWhereActivity.equals("CALL_LOG")) {
//                    fragment.setmFromCallLog(true);
//                }
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setIncludeProfile(mRequest.shouldIncludeProfile());
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_OR_CREATE_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CONTACT: {
                ContactPickerFragment fragment = new ContactPickerFragment();
                fragment.setShortcutRequested(true);
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_PHONE: {
                PhoneNumberPickerFragment fragment = new PhoneNumberPickerFragment();
                mListFragment = fragment;
                break;
            }

            case ContactsRequest.ACTION_PICK_EMAIL: {
                mListFragment = new EmailAddressPickerFragment();
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_CALL: {
                PhoneNumberPickerFragment fragment = new PhoneNumberPickerFragment();
                fragment.setShortcutAction(Intent.ACTION_CALL);

                mListFragment = fragment;
                // aurora <wangth> <2013-11-7> modify for aurora begin
                mListFragment.setOnlyPhone(true);
                // aurora <wangth> <2013-11-7> modify for aurora end
                break;
            }

            case ContactsRequest.ACTION_CREATE_SHORTCUT_SMS: {
                PhoneNumberPickerFragment fragment = new PhoneNumberPickerFragment();
                fragment.setShortcutAction(Intent.ACTION_SENDTO);

                mListFragment = fragment;
                // aurora <wangth> <2013-11-7> modify for aurora begin
                mListFragment.setOnlyPhone(true);
                // aurora <wangth> <2013-11-7> modify for aurora end
                break;
            }

            case ContactsRequest.ACTION_PICK_POSTAL: {
                PostalAddressPickerFragment fragment = new PostalAddressPickerFragment();
                mListFragment = fragment;
                break;
            }

            default:
                throw new IllegalStateException("Invalid action code: " + mActionCode);
        }

        mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);
        // aurora <wangth> <2013-11-5> add for aurora begin
        mListFragment.setSlideDelete(false);
        // aurora <wangth> <2013-11-5> add for aurora end

        getFragmentManager().beginTransaction()
                .replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    public void setupActionListener() {
        if (mListFragment instanceof ContactPickerFragment) {
            ((ContactPickerFragment) mListFragment).setOnContactPickerActionListener(
                    new ContactPickerActionListener());
        } else if (mListFragment instanceof PhoneNumberPickerFragment) {
            ((PhoneNumberPickerFragment) mListFragment).setOnPhoneNumberPickerActionListener(
                    new PhoneNumberPickerActionListener());
        } else if (mListFragment instanceof PostalAddressPickerFragment) {
            ((PostalAddressPickerFragment) mListFragment).setOnPostalAddressPickerActionListener(
                    new PostalAddressPickerActionListener());
        } else if (mListFragment instanceof EmailAddressPickerFragment) {
            ((EmailAddressPickerFragment) mListFragment).setOnEmailAddressPickerActionListener(
                    new EmailAddressPickerActionListener());
        } else {
            throw new IllegalStateException("Unsupported list fragment type: " + mListFragment);
        }
    }

    private final class ContactPickerActionListener implements OnContactPickerActionListener {
        @Override
        public void onCreateNewContactAction() {
            startCreateNewContactActivity();
        }

        @Override
        public void onEditContactAction(Uri contactLookupUri) {
        	//Gionee <huangzy> <2013-05-22> modify CR00813879 begin
        	/*Bundle extras = getIntent().getExtras();
            if (launchAddToContactDialog(extras)) {
                // Show a confirmation dialog to add the value(s) to the existing contact.
                Intent intent = new Intent(ContactSelectionActivity.this,
                        ConfirmAddDetailActivity.class);
                intent.setData(contactLookupUri);
                if (extras != null) {
                    intent.putExtras(extras);
                }
                // Wait for the activity result because we want to keep the picker open (in case the
                // user cancels adding the info to a contact and wants to pick someone else).
                startActivityForResult(intent, SUBACTIVITY_ADD_TO_EXISTING_CONTACT);
            } else {
                // Otherwise launch the full contact editor.
                startActivityAndForwardResult(new Intent(Intent.ACTION_EDIT, contactLookupUri));
            }*/
        	
        	startActivityAndForwardResult(new Intent(Intent.ACTION_EDIT, contactLookupUri));
            //Gionee <huangzy> <2013-05-22> modify CR00813879 end
        }

        @Override
        public void onPickContactAction(Uri contactUri) {
            returnPickerResult(contactUri);
        }

        @Override
        public void onShortcutIntentCreated(Intent intent) {
            returnPickerResult(intent);
        }

        /**
         * Returns true if is a single email or single phone number provided in the {@link Intent}
         * extras bundle so that a pop-up confirmation dialog can be used to add the data to
         * a contact. Otherwise return false if there are other intent extras that require launching
         * the full contact editor.
         */
        private boolean launchAddToContactDialog(Bundle extras) {
            if (extras == null) {
                return false;
            }
            Set<String> intentExtraKeys = extras.keySet();
            int numIntentExtraKeys = intentExtraKeys.size();
            if (numIntentExtraKeys == 2) {
                boolean hasPhone = intentExtraKeys.contains(Insert.PHONE) &&
                        intentExtraKeys.contains(Insert.PHONE_TYPE);
                boolean hasEmail = intentExtraKeys.contains(Insert.EMAIL) &&
                        intentExtraKeys.contains(Insert.EMAIL_TYPE);
                return hasPhone || hasEmail;
            } else if (numIntentExtraKeys == 1) {
                return intentExtraKeys.contains(Insert.PHONE) ||
                        intentExtraKeys.contains(Insert.EMAIL);
            }
            // Having 0 or more than 2 intent extra keys means that we should launch
            // the full contact editor to properly handle the intent extras.
            return false;
        }
    }

    private final class PhoneNumberPickerActionListener implements
            OnPhoneNumberPickerActionListener {
        @Override
        public void onPickPhoneNumberAction(Uri dataUri) {
            returnPickerResult(dataUri);
        }

        @Override
        public void onShortcutIntentCreated(Intent intent) {
            returnPickerResult(intent);
        }

        public void onHomeInActionBarSelected() {
            ContactSelectionActivity.this.onBackPressed();
        }
    }

    private final class PostalAddressPickerActionListener implements
            OnPostalAddressPickerActionListener {
        @Override
        public void onPickPostalAddressAction(Uri dataUri) {
            returnPickerResult(dataUri);
        }
    }

    private final class EmailAddressPickerActionListener implements
            OnEmailAddressPickerActionListener {
        @Override
        public void onPickEmailAddressAction(Uri dataUri) {
            returnPickerResult(dataUri);
        }
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

    //  aurora <wangth> <2013-12-9> remove for aurora begin
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        mListFragment.setQueryString(newText, true);
//        return false;
//    }
//
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//        return false;
//    }
//
//    @Override
//    public boolean onClose() {
//        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
//            mSearchView.setQuery(null, true);
//        }
//        return true;
//    }
    //  aurora <wangth> <2013-12-9> remove for aurora end

    //Gionee <huangzy> <2013-04-25> remove for CR00801750 begin
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
    //Gionee <huangzy> <2013-04-25> remove for CR00801750 end

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


    private void startCreateNewContactActivity() {
        //Gionee:huangzy 20130401 modify for CR00792013 start
        /*Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);*/
    	Intent intent = IntentFactory.newInsertContactIntent(true, null, null, null);
        //Gionee:huangzy 20130401 modify for CR00792013 end
        startActivityAndForwardResult(intent);
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
    }
}
