
package com.mediatek.contacts.list;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.mediatek.contacts.util.ContactsIntent;

import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Intents;
import gionee.provider.GnContactsContract.Intents.UI;

/**
 * A sub class to extend the ContactsIntentResolver parses
 * {@link com.mediatek.contacts.util.ContactsIntent} not defined in Android
 * source code.
 * <p>
 * ContactsIntentResolver parses a Contacts intent, extracting all relevant
 * parts and packaging them as a
 * {@link com.android.contacts.list.ContactsRequest} object.
 * </p>
 */

public class ContactsIntentResolverEx extends ContactsIntentResolver {

    private static final String TAG = "ContactsIntentResolverEx";

    public static final int REQ_TYPE_IMPORT_EXPORT_PICKER = 1;

    private static final int REQ_TYPE_VCARD_PICKER = 3;
    
    public static final String EXTRA_KEY_REQUEST_TYPE = "request_type"; 
    
    /** Mask for picking multiple contacts of packing vCard */
    public static final int MODE_MASK_VCARD_PICKER = 0x01000000;

    /** Mask for picking multiple contacts of import/export */
    public static final int MODE_MASK_IMPORT_EXPORT_PICKER = 0x02000000;
    
    //Gionee:huangzy 20120604 add for CR00616160 start
    public static final int REQ_TYPE_SET_STAR_PICKER = 4;
    public static final int MODE_MASK_SET_STAR_PICKER = 0x03000000;
    
    public static final int REQ_TYPE_REMOVE_STAR_PICKER = 5;
    public static final int MODE_MASK_REMOVE_STARED_PICKER = 0x04000000;
    
    public static final int GN_REQ_TYPE_ADD2GROUP_PICKER = 6;
    public static final int GN_MODE_MASK_ADD2GROUP_PICKER = 0x05000000;
    //Gionee:huangzy 20120604 add for CR00616160 end

    public ContactsIntentResolverEx(Activity context) {
        super(context);
    }

    @Override
    public ContactsRequest resolveIntent(Intent intent) {

        if (ContactsIntent.contain(intent)) {

            String action = intent.getAction();

            Log.i(TAG, "Called with action: " + action);
            ContactsRequest request = new ContactsRequest();
            if (ContactsIntent.LIST.ACTION_PICK_MULTICONTACTS.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS);
                int requestType = intent.getIntExtra(EXTRA_KEY_REQUEST_TYPE, 0);

                switch (requestType) {
                    case REQ_TYPE_VCARD_PICKER:
                        request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                                | MODE_MASK_VCARD_PICKER);
                        break;

                    case REQ_TYPE_IMPORT_EXPORT_PICKER:
                        request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                                | MODE_MASK_IMPORT_EXPORT_PICKER);
                        break;
                        
                    case REQ_TYPE_SET_STAR_PICKER:
                    	request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                                | MODE_MASK_SET_STAR_PICKER);
                    	break;
                    	
                    case REQ_TYPE_REMOVE_STAR_PICKER:
                    	request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                                | MODE_MASK_REMOVE_STARED_PICKER);
                    	break;
                    //Gionee:huangzy 20120604 add for CR00616160 start                    	
                    case GN_REQ_TYPE_ADD2GROUP_PICKER:
                        request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_CONTACTS
                                | GN_MODE_MASK_ADD2GROUP_PICKER);
                    	break;
                    //Gionee:huangzy 20120604 add for CR00616160 end

                    default:
                        break;
                }
            } else if (ContactsIntent.LIST.ACTION_PICK_MULTIEMAILS.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_EMAILS);
            } else if (ContactsIntent.LIST.ACTION_PICK_MULTIPHONES.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_PHONES);
            } else if (ContactsIntent.LIST.ACTION_DELETE_MULTICONTACTS.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_DELETE_MULTIPLE_CONTACTS);
            } else if (ContactsIntent.LIST.ACTION_PICK_GROUP_MULTICONTACTS.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_PICK_GROUP_MULTIPLE_CONTACTS);
            } else if (ContactsIntent.LIST.ACTION_PICK_MULTIPHONEANDEMAILS.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_PICK_MULTIPLE_PHONEANDEMAILS);
            } else if (ContactsIntent.LIST.ACTION_SHARE_MULTICONTACTS.equals(action)) {
                request.setActionCode(ContactsRequest.ACTION_SHARE_MULTIPLE_CONTACTS);
            }

            // Allow the title to be set to a custom String using an extra on
            // the intent
            String title = intent.getStringExtra(UI.TITLE_EXTRA_KEY);
            if (title != null) {
                request.setActivityTitle(title);
            }
            return request;
        }

        return super.resolveIntent(intent);
    }
}
