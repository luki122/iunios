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
 * limitations under the License
 */

package com.android.contacts.detail;

import java.util.ArrayList;

import com.android.contacts.ContactLoader;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactDetailActivity.FragmentKeyListener;
import com.android.contacts.activities.ContactsLog;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.PhoneCapabilityTester;
import com.mediatek.contacts.util.Objects;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.Loader;
import android.content.Entity.NamedContentValues;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import android.os.ServiceManager;
//import com.android.internal.telephony.ITelephony;
import android.os.SystemProperties;

import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

//add by mtk80684,2011.12.24
import android.bluetooth.BluetoothAdapter;
//mtk80684,2011.12.24 end


// Gionee:wangth 20120529 modify for CR00560595 begin
import com.android.contacts.ContactsUtils;
// Gionee:wangth 20120529 modify for CR00560595 end

/**
 * This is an invisible worker {@link Fragment} that loads the contact details for the contact card.
 * The data is then passed to the listener, who can then pass the data to other {@link View}s.
 */
public class ContactLoaderFragment extends Fragment implements FragmentKeyListener {

    private static final String TAG = ContactLoaderFragment.class.getSimpleName();

    /** The launch code when picking a ringtone */
    private static final int REQUEST_CODE_PICK_RINGTONE = 1;


    private boolean mOptionsMenuOptions;
    private boolean mOptionsMenuEditable;
    private boolean mOptionsMenuShareable;
    // add by mediatek for print
    private boolean mOptionsMenuPrintable;
    private boolean mSendToVoicemailState;
    private String mCustomRingtone;

    private boolean mBlockVideoCallState;
    private boolean mRejectAllVoiceCall = false;
    private boolean mRejectAllVideoCall = false;
    
    private boolean mIsAuroraPrivacyContact = false;
    

    /**
     * This is a listener to the {@link ContactLoaderFragment} and will be notified when the
     * contact details have finished loading or if the user selects any menu options.
     */
    public static interface ContactLoaderFragmentListener {
        /**
         * Contact was not found, so somehow close this fragment. This is raised after a contact
         * is removed via Menu/Delete
         */
        public void onContactNotFound();

        /**
         * Contact details have finished loading.
         */
        public void onDetailsLoaded(ContactLoader.Result result);

        /**
         * User decided to go to Edit-Mode
         */
        public void onEditRequested(Uri lookupUri);

        /**
         * User decided to delete the contact
         */
        public void onDeleteRequested(Uri lookupUri);

    }

    private static final int LOADER_DETAILS = 1;

    private static final String KEY_CONTACT_URI = "contactUri";
    private static final String LOADER_ARG_CONTACT_URI = "contactUri";

    private Context mContext;
    private Uri mLookupUri;
    private ContactLoaderFragmentListener mListener;

    private ContactLoader.Result mContactData;
	
	//add by mtk80684, 2011.12.24
    // Gionee lihuafang 20120326 modify for CR00555703 begin
    /*
	private static boolean mShowPrintMenu = true;
	*/
    private static boolean mShowPrintMenu = false;
	// Gionee lihuafang 20120326 modify for CR00555703 end
	static {
		if (null == BluetoothAdapter.getDefaultAdapter() || !FeatureOption.MTK_BT_PROFILE_BPP)
			mShowPrintMenu = false;
	}
	//mtk80684, 2011.12.24 end

    public ContactLoaderFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLookupUri = savedInstanceState.getParcelable(KEY_CONTACT_URI);
            mIsAuroraPrivacyContact = savedInstanceState.getBoolean("is_privacy_contact");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CONTACT_URI, mLookupUri);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        setHasOptionsMenu(true);
        // This is an invisible view.  This fragment is declared in a layout, so it can't be
        // "viewless".  (i.e. can't return null here.)
        // See also the comment in the layout file.
        return inflater.inflate(R.layout.contact_detail_loader_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mLookupUri != null) {
            Bundle args = new Bundle();
            args.putParcelable(LOADER_ARG_CONTACT_URI, mLookupUri);
            getLoaderManager().initLoader(LOADER_DETAILS, args, mDetailLoaderListener);
        }
    }

    public void loadUri(Uri lookupUri) {
        if (Objects.equal(lookupUri, mLookupUri)) {
            // Same URI, no need to load the data again
            return;
        }

        mLookupUri = lookupUri;
        if (mLookupUri == null) {
            getLoaderManager().destroyLoader(LOADER_DETAILS);
            mContactData = null;
            if (mListener != null) {
                mListener.onDetailsLoaded(mContactData);
            }
        } else if (getActivity() != null) {
            Bundle args = new Bundle();
            args.putParcelable(LOADER_ARG_CONTACT_URI, mLookupUri);
            getLoaderManager().restartLoader(LOADER_DETAILS, args, mDetailLoaderListener);
        }
    }

    public void setListener(ContactLoaderFragmentListener value) {
        mListener = value;
    }

    /**
     * The listener for the detail loader
     */
    private final LoaderManager.LoaderCallbacks<ContactLoader.Result> mDetailLoaderListener =
            new LoaderCallbacks<ContactLoader.Result>() {
        @Override
        public Loader<ContactLoader.Result> onCreateLoader(int id, Bundle args) {
            Uri lookupUri = args.getParcelable(LOADER_ARG_CONTACT_URI);
            ContactsLog.logt(TAG, "start load");
            return new ContactLoader(mContext, lookupUri, true /* loadGroupMetaData */,
                    true /* loadStreamItems */, true /* load invitable account types */, mIsAuroraPrivacyContact); // aurora wangth 20141006 modify
        }

        @Override
        public void onLoadFinished(Loader<ContactLoader.Result> loader, ContactLoader.Result data) {
            if (!mLookupUri.equals(data.getRequestedUri())) {
                return;
            }
            ContactsLog.logt(TAG, "onLoadFinished load");
            if (data.isError()) {
                // This shouldn't ever happen, so throw an exception. The {@link ContactLoader}
                // should log the actual exception.
                // Gionee:wangth 20120529 modify for CR00560595 begin
                /*
                throw new IllegalStateException("Failed to load contact", data.getException());
                */
           
                    Log.d(TAG, "load contact error.");
                    getActivity().finish();
                    return;
             
                // Gionee:wangth 20120529 modify for CR00560595 end
            } else if (data.isNotFound()) {
                Log.i(TAG, "No contact found: " + ((ContactLoader)loader).getLookupUri());
                mContactData = null;
            } else {
                mContactData = data;
            }

            if (mListener != null) {
                if (mContactData == null) {
                    mListener.onContactNotFound();
                } else {
                    mListener.onDetailsLoaded(mContactData);
                }
            }
            // Make sure the options menu is setup correctly with the loaded data.
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onLoaderReset(Loader<ContactLoader.Result> loader) {}
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
		//modified by mtk80684, 2011.12.24

    		inflater.inflate(R.menu.gn_view_contact, menu);


    }

    public boolean isOptionsMenuChanged() {
        return mOptionsMenuOptions != isContactOptionsChangeEnabled()
                || mOptionsMenuEditable != isContactEditable()
                || mOptionsMenuShareable != isContactShareable();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mOptionsMenuOptions = isContactOptionsChangeEnabled();
        mOptionsMenuEditable = isContactEditable();
        mOptionsMenuShareable = isContactShareable();
        // add by mediatek for print
        mOptionsMenuPrintable = isContactPrintable();
        if (mContactData != null) {
            mSendToVoicemailState = mContactData.isSendToVoicemail();
            mBlockVideoCallState = mContactData.isBlockVideoCall();
            mCustomRingtone = mContactData.getCustomRingtone();
        }

        // Hide telephony-related settings (ringtone, send to voicemail)
        // if we don't have a telephone
//        final MenuItem optionsSendToVoicemail = menu.findItem(R.id.menu_send_to_voicemail);
//        if (optionsSendToVoicemail != null) {
//            optionsSendToVoicemail.setChecked(mSendToVoicemailState);
//            optionsSendToVoicemail.setVisible(mOptionsMenuOptions);
//        }
        
        // Gionee:wangth 20120629 add for CR00631745 && CR00633206 begin
        if ( mContactData != null 
                && mContactData.getLookupKey() != null
                && mContactData.getLookupKey().equals("profile")) {
            mOptionsMenuShareable = false;
            mOptionsMenuOptions = false;
        }
        // Gionee:wangth 20120629 add for CR00631745 && CR00633206 end
        
        final MenuItem optionsRingtone = menu.findItem(R.id.menu_set_ringtone);
        if (optionsRingtone != null) {
//            optionsRingtone.setVisible(mOptionsMenuOptions);
            optionsRingtone.setVisible(mOptionsMenuOptions &&
            		false);
            
            // edit by mediatek
            if (this.mContactData != null && this.mContactData.getIndicate() >= 0) {
                optionsRingtone.setVisible(false);
            }
        }

        final MenuItem editMenu = menu.findItem(R.id.menu_edit);
        if (editMenu != null) {
            editMenu.setVisible(mOptionsMenuEditable);
        }

        final MenuItem deleteMenu = menu.findItem(R.id.menu_delete);
        if (deleteMenu != null) {
            deleteMenu.setVisible(mOptionsMenuEditable);
        }

        final MenuItem shareMenu = menu.findItem(R.id.menu_share);
        if (shareMenu != null) {
            shareMenu.setVisible(mOptionsMenuShareable);
        }
        
        /*
         * New Feature by Mediatek Begin.            
         * set block voice and block video menu state        
         */
        final MenuItem blockVideoCallmenu = menu.findItem(R.id.menu_block_video_incoming_call);
        if (blockVideoCallmenu != null) {
            blockVideoCallmenu.setVisible(false);
        }
        
        String mOptr = SystemProperties.get("ro.operator.optr");
        boolean cmccSupport = false;
        if (null != mOptr && mOptr.equals("OP01")) {
            cmccSupport = true;
        }
        final MenuItem blockVoiceCallmenu = menu.findItem(R.id.menu_block_voice_incoming_call);
        blockVoiceCallmenu.setVisible(false);
        
        final MenuItem add2BlacklistMenu = menu.findItem(R.id.gn_menu_add_blacklist);
        add2BlacklistMenu.setVisible(false);
    }

    public boolean isContactOptionsChangeEnabled() {
        return mContactData != null && !mContactData.isDirectoryEntry()
               && mContactData.getIndicate() < 0 && PhoneCapabilityTester.isPhone(mContext);
    }

    public boolean isContactEditable() {
        return mContactData != null && !mContactData.isDirectoryEntry();
    }

    public boolean isContactShareable() {
        return mContactData != null && !mContactData.isDirectoryEntry();
    }

    // add by mediatek for print
    public boolean isContactPrintable() {
        return mContactData != null && !mContactData.isDirectoryEntry();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                if (mListener != null) mListener.onEditRequested(mLookupUri);
                break;
            }
            case R.id.menu_delete: {
                if (mListener != null) mListener.onDeleteRequested(mLookupUri);
                return true;
            }
            case R.id.menu_set_ringtone: {
                if (mContactData == null) return false;
                doPickRingtone();
                return true;
            }  
            case R.id.menu_share: {
                if (mContactData == null) return false;

                final String lookupKey = mContactData.getLookupKey();
                Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                if (mContactData.isUserProfile()) {
                    // User is sharing the profile.  We don't want to force the receiver to have
                    // the highly-privileged READ_PROFILE permission, so we need to request a
                    // pre-authorized URI from the provider.
                    shareUri = getPreAuthorizedUri(shareUri);
                    intent.setType(Contacts.CONTENT_VCARD_TYPE);
                    intent.putExtra("userProfile", "true");
                }else{
                	intent.setType(Contacts.CONTENT_VCARD_TYPE);
                    //intent.setDataAndType(shareUri, Contacts.CONTENT_VCARD_TYPE);
                    intent.putExtra("contactId", String.valueOf(mContactData.getContactId()));
                }

                //intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                // Launch chooser to share contact via
                final CharSequence chooseTitle = mContext.getText(R.string.share_via);
                final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

                try {
                    mContext.startActivity(chooseIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(mContext, R.string.share_error, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.menu_block_voice_incoming_call: {
                mSendToVoicemailState = !mSendToVoicemailState;
                item.setChecked(mSendToVoicemailState);
                //Gionee:huangzy 20120704 modify for CR00633431 start
                Intent intent = null;
                    Uri contactUri = ContactsUtils.gnLookupUri2ContactUri(getActivity(), mLookupUri);
                    intent = ContactSaveService.createSetSendToVoicemail(mContext, contactUri , mSendToVoicemailState);    
         
                mContext.startService(intent);
                return true;
            }
            case R.id.menu_block_video_incoming_call: {             
                mBlockVideoCallState = !mBlockVideoCallState;
                item.setChecked(mBlockVideoCallState);
                //Gionee:huangzy 20120704 modify for CR00633431 start
                Intent intent = null;
                    Uri contactUri = ContactsUtils.gnLookupUri2ContactUri(getActivity(), mLookupUri);
                    intent = ContactSaveService.createSetBlockVideoCall(mContext, contactUri, mBlockVideoCallState);    
            
                
                mContext.startService(intent);
                return true;
            }
            //Gionee:huangzy 20120806 modify for CR00667827 start
            case R.id.gn_menu_add_blacklist: {
            	return true;
            }
            //Gionee:huangzy 20120806 modify for CR00667827 end
        }
        return false;
    }

    /**
     * Calls into the contacts provider to get a pre-authorized version of the given URI.
     */
    private Uri getPreAuthorizedUri(Uri uri) {
        Bundle uriBundle = new Bundle();
        uriBundle.putParcelable(ContactsContract.Authorization.KEY_URI_TO_AUTHORIZE, uri);
        Bundle authResponse = mContext.getContentResolver().call(
                ContactsContract.AUTHORITY_URI,
                ContactsContract.Authorization.AUTHORIZATION_METHOD,
                null,
                uriBundle);
        if (authResponse != null) {
            return (Uri) authResponse.getParcelable(
                    ContactsContract.Authorization.KEY_AUTHORIZED_URI);
        } else {
            return uri;
        }
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL: {
                if (mListener != null) mListener.onDeleteRequested(mLookupUri);
                return true;
            }
        }
        return false;
    }

    private void doPickRingtone() {

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        // Don't show 'Silent'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

        Uri ringtoneUri;
        if (mCustomRingtone != null) {
            ringtoneUri = Uri.parse(mCustomRingtone);
        } else {
            // Otherwise pick default ringtone Uri so that something is selected.
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

        // Launch!
        startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_PICK_RINGTONE: {
                Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                handleRingtonePicked(pickedUri);
                break;
            }
        }
    }

    private void handleRingtonePicked(Uri pickedUri) {
        if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
            mCustomRingtone = null;
        } else {
            mCustomRingtone = pickedUri.toString();
        }
        //Gionee:huangzy 20120704 modify for CR00633431 start
        Intent intent = null;
            Uri contactUri = ContactsUtils.gnLookupUri2ContactUri(getActivity(), mLookupUri);
            intent = ContactSaveService.createSetRingtone(
                    mContext, contactUri, mCustomRingtone);    
       
        //Gionee:huangzy 20120704 modify for CR00633431 end
        
        mContext.startService(intent);
    }
    
    public boolean ifSupport3G324M() {         
        if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
            String mOptr = SystemProperties.get("ro.operator.optr");
            if (null != mOptr && mOptr.equals("OP01")) {
                return true;
            } 
        }
        return false;
    }
    
    protected void setStar(boolean isStar) {
    	if (mLookupUri != null) {
            Intent intent = ContactSaveService.createSetStarredIntent(
                    getActivity(), mLookupUri, isStar);
            getActivity().startService(intent);
    	}
	}
    
    private boolean gnIsExistNumber() {
    	if (null != mContactData) {
    		StringBuilder numbers = new StringBuilder();
        	ArrayList<Entity> entities = mContactData.getEntities();
        	if (null != entities) {
        		for (Entity entitie : entities) {    			
        			for (NamedContentValues sub : entitie.getSubValues()) {
        				if (Phone.CONTENT_ITEM_TYPE.equals(sub.values.getAsString("mimetype"))) {
        					return true;
        				}
        			}
            	}	
        	}
    	}
    	
    	return false;    	
    }
    //Gionee:huangzy 20120806 modify for CR00667827 end
}
