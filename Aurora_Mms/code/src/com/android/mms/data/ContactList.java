package com.android.mms.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.LogTag;
import com.android.mms.ui.MessageUtils;

//a0
import com.android.mms.MmsApp;
import java.util.Arrays;
import java.util.LinkedList;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Presence;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
//a1


public class ContactList extends ArrayList<Contact>  {
    private static final long serialVersionUID = 1L;

    public static ContactList getByNumbers(Iterable<String> numbers, boolean canBlock) {
        ContactList list = new ContactList();
        for (String number : numbers) {
          // Aurora xuyong 2014-10-23 modifie
            String[] info = number.split(":");
            if (info.length == 2) {
                if (!TextUtils.isEmpty(info[0])) {
                    list.add(Contact.get(info[0], canBlock, Long.parseLong(info[1])));
                }
            } else {
                if (!TextUtils.isEmpty(number)) {
                    list.add(Contact.get(number, canBlock));
                }
            }
          // Aurora xuyong 2014-10-23 modified for privacy feature end
        }
        return list;
    }

   //gionee gaoj 2012-3-22 added for CR00555790 start
    public static ContactList getByNumbers(String semiSepNumbers,
            boolean canBlock, boolean replaceNumber, boolean whetherkilledBack) {
        if (whetherkilledBack) {
            ContactList list = new ContactList();
            for (String number : semiSepNumbers.split(";")) {
                if (!TextUtils.isEmpty(number)) {
                    Contact contact = Contact.get(number, canBlock);
                    if (replaceNumber) {
                        contact.setNumber(number);
                    }
                    list.add(contact);
                }
            }
            return list;
        } else {
            return getByNumbers(semiSepNumbers, canBlock, replaceNumber);
        }
    }
   //gionee gaoj 2012-3-22 added for CR00555790 end
    public static ContactList getByNumbers(String semiSepNumbers,
                                           boolean canBlock,
                                           boolean replaceNumber) {
        ContactList list = new ContactList();
        if (semiSepNumbers != null) {
            for (String number : semiSepNumbers.split(";")) {
                if (!TextUtils.isEmpty(number)) {
                    Log.d(TAG, "ContactList.getByNumbers(): before Contact.get(), number=" + number);
                    Contact contact = Contact.get(number, canBlock);
                    Log.d(TAG, "ContactList.getByNumbers(): after Contact.get(), number=" + contact.getNumber());
                    list.add(contact);
                }
            }
        }
        return list;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static ContactList getByNumbers(String semiSepNumbers,
            boolean canBlock,
            boolean replaceNumber, long privacy) {
        ContactList list = new ContactList();
        if (semiSepNumbers != null) {
            for (String number : semiSepNumbers.split(";")) {
                if (!TextUtils.isEmpty(number)) {
                    Contact contact = Contact.get(number, canBlock, privacy);
                    list.add(contact);
                }
            }
        }
        return list;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    /**
     * Returns a ContactList for the corresponding recipient URIs passed in. This method will
     * always block to query provider. The given URIs could be the phone data URIs or tel URI
     * for the numbers don't belong to any contact.
     *
     * @param uris phone URI to create the ContactList
     */
    public static ContactList blockingGetByUris(Parcelable[] uris) {
        ContactList list = new ContactList();
        if (uris != null && uris.length > 0) {
            for (Parcelable p : uris) {
                Uri uri = (Uri) p;
                if ("tel".equals(uri.getScheme())) {
                    Contact contact = Contact.get(uri.getSchemeSpecificPart(), true);
                    list.add(contact);
                }
            }
            final List<Contact> contacts = Contact.getByPhoneUris(uris);
            if (contacts != null) {
                list.addAll(contacts);
            }
        }
        return list;
    }
    
    //a0
    public static ContactList blockingGetByIds(long[] ids) {
        ContactList list = new ContactList();
        if (ids != null && ids.length > 0) {
            final List<Contact> contacts = Contact.getByPhoneIds(ids);
            if (contacts != null) {
                list.addAll(contacts);
            }
        }
        return list;
    }
    //a1

    /**
     * Returns a ContactList for the corresponding recipient ids passed in. This method will
     * create the contact if it doesn't exist, and would inject the recipient id into the contact.
     */
    public static ContactList getByIds(String spaceSepIds, boolean canBlock) {
        ContactList list = new ContactList();
        for (RecipientIdCache.Entry entry : RecipientIdCache.getAddresses(spaceSepIds)) {
            if (entry != null && !TextUtils.isEmpty(entry.number)) {
                Contact contact = Contact.get(entry.number, canBlock);
                //gionee gaoj 2012-12-4 added for CR00738888 start
                if (null != contact) {
                    contact.setRecipientId(entry.id);
                    list.add(contact);
                }
                //gionee gaoj 2012-12-4 added for CR00738888 end
            }
        }
        return list;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static ContactList getByIds(Context context, String spaceSepIds, boolean canBlock) {
        ContactList list = new ContactList();
        for (RecipientIdCache.Entry entry : RecipientIdCache.getAddresses(context, spaceSepIds)) {
            if (entry != null && !TextUtils.isEmpty(entry.number)) {
                Contact contact = Contact.get(entry.number, canBlock, entry.mPrivacy);
                //gionee gaoj 2012-12-4 added for CR00738888 start
                if (null != contact) {
                    contact.setRecipientId(entry.id);
                    list.add(contact);
                }
                //gionee gaoj 2012-12-4 added for CR00738888 end
            }
        }
        return list;
    }
    
    public static ContactList getByIds(Context context, String spaceSepIds, boolean canBlock, long privacy) {
        ContactList list = new ContactList();
        for (RecipientIdCache.Entry entry : RecipientIdCache.getAddresses(context, spaceSepIds, privacy)) {
            if (entry != null && !TextUtils.isEmpty(entry.number)) {
                Contact contact = Contact.get(entry.number, canBlock, entry.mPrivacy);
                //gionee gaoj 2012-12-4 added for CR00738888 start
                if (null != contact) {
                    contact.setRecipientId(entry.id);
                    list.add(contact);
                }
                //gionee gaoj 2012-12-4 added for CR00738888 end
            }
        }
        return list;
    }
    // Aurora xuyong 2014-10-24 added for privacy feature end
    public int getPresenceResId() {
        // We only show presence for single contacts.
        if (size() != 1)
            return 0;

        return get(0).getPresenceResId();
    }

    public String formatNames(String separator) {
        String[] names = new String[size()];
        int i = 0;
        for (Contact c : this) {
            names[i++] = c.getName();
        }
        return TextUtils.join(separator, names);
    }

    // Aurora liugj 2014-01-10 modified for listItem optimize start
    public String fromFormatNames(String separator) {
        String[] names = new String[size()];
        int i = 0;
        for (Contact c : this) {
            names[i++] = c.getName();
            if (i > 4) {
                break;
            }
        }
        return TextUtils.join(separator, names);
    }
     // Aurora liugj 2014-01-10 modified for listItem optimize end

    public String formatNamesAndNumbers(String separator) {
        String[] nans = new String[size()];
        int i = 0;
        for (Contact c : this) {
            nans[i++] = c.getNameAndNumber();
        }
        return TextUtils.join(separator, nans);
    }

    public String serialize() {
        return TextUtils.join(";", getNumbers());
    }

    public boolean containsEmail() {
        for (Contact c : this) {
            if (c.isEmail()) {
                return true;
            }
        }
        return false;
    }

    public String[] getNumbers() {
        return getNumbers(false /* don't scrub for MMS address */);
    }

    public String[] getNumbers(boolean scrubForMmsAddress) {
        List<String> numbers = new ArrayList<String>();
        String number;
        for (Contact c : this) {
            number = c.getNumber();

            if (scrubForMmsAddress) {
                // parse/scrub the address for valid MMS address. The returned number
                // could be null if it's not a valid MMS address. We don't want to send
                // a message to an invalid number, as the network may do its own stripping,
                // and end up sending the message to a different number!
                number = MessageUtils.parseMmsAddress(number);
            }

            // Don't add duplicate numbers. This can happen if a contact name has a comma.
            // Since we use a comma as a delimiter between contacts, the code will consider
            // the same recipient has been added twice. The recipients UI still works correctly.
            // It's easiest to just make sure we only send to the same recipient once.
            if (!TextUtils.isEmpty(number) && !numbers.contains(number)) {
                numbers.add(number);
            }
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    @Override
    public boolean equals(Object obj) {
        try {
            ContactList other = (ContactList)obj;
            // If they're different sizes, the contact
            // set is obviously different.
            if (size() != other.size()) {
                return false;
            }

            // Make sure all the individual contacts are the same.
            for (Contact c : this) {
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                if (!MmsApp.sHasPrivacyFeature) {
                    if (!other.contains(c)) {
                        return false;
                    } 
                } else {
                    for (Contact co : other) {
                        if (co.getNumber() != c.getNumber() || co.getPrivacy() != c.getPrivacy()) {
                            return false;
                        }
                    }
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                }
            }

            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    private void log(String msg) {
        Log.d(LogTag.TAG, "[ContactList] " + msg);
    }

    //a0
    private static final String TAG = "Mms/ContactList";
    // query params for contact lookup by number
    private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

    private static final String[] CALLER_ID_PROJECTION = new String[] {
        Phone.NUMBER, // 0
        Phone.LABEL, // 1
        Phone.DISPLAY_NAME, // 2
        Phone.CONTACT_ID, // 3
        Phone.CONTACT_PRESENCE, // 4
        Phone.CONTACT_STATUS, // 5
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        Phone.NORMALIZED_NUMBER, //6
        "is_privacy"
        // Aurora xuyong 2014-10-23 modified for privacy feature end
    };

    private static final int PHONE_NUMBER_COLUMN = 0;
    private static final int PHONE_LABEL_COLUMN = 1;
    private static final int CONTACT_NAME_COLUMN = 2;
    private static final int CONTACT_ID_COLUMN = 3;
    private static final int CONTACT_PRESENCE_COLUMN = 4;
    private static final int CONTACT_STATUS_COLUMN = 5;
    private static final int PHONE_NORMALIZED_NUMBER = 6;
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private static final int IS_PRIVACY = 7;
    // Aurora xuyong 2014-10-24 added for privacy feature end
    //a1
    
    //a0
    public static ContactList getByNumbers(Context mContext, String semiSepNumbers, boolean canBlock,
            boolean replaceNumber) {
        ContactList list = new ContactList();
        if (semiSepNumbers.contains(";")) {
            String selection = Phone.NUMBER + " in ('" + semiSepNumbers.replaceAll(";", "','") + "')";
            selection = selection.replaceAll("-", "");
            LinkedList<String> numberList = new LinkedList<String>(Arrays.asList(semiSepNumbers.split(";")));
            Log.d(TAG, "ContactList.getByNumbers()--selection = \"" + selection + "\"");
            selection = DatabaseUtils.sqlEscapeString(selection);
            Cursor cursor = mContext.getContentResolver().query(PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION,
                selection, null, null);
            if (cursor == null) {
                Log.w(TAG, "ContactList.getByNumbers(" + semiSepNumbers + ") returned NULL cursor! contact uri used "
                    + PHONES_WITH_PRESENCE_URI);
                return list;
            }
            Collections.sort(numberList);
            try {
                while (cursor.moveToNext()) {
                    String number = cursor.getString(PHONE_NUMBER_COLUMN);
                    String label = cursor.getString(PHONE_LABEL_COLUMN);
                    String name = cursor.getString(CONTACT_NAME_COLUMN);
                    String mNumberE164 = cursor.getString(PHONE_NORMALIZED_NUMBER);
                    String nameAndNumber = Contact.formatNameAndNumber(name, number, mNumberE164);
                    long personId = cursor.getLong(CONTACT_ID_COLUMN);
                    int presence = cursor.getInt(CONTACT_PRESENCE_COLUMN);
                    String presenceText = cursor.getString(CONTACT_STATUS_COLUMN);
                    // Aurora xuyong 2014-10-23 modified for privacy feature start
                    Contact entry = null;
                    if (MmsApp.sHasPrivacyFeature) {
                        long privacy = cursor.getLong(IS_PRIVACY);
                        entry = new Contact(number, label, name, nameAndNumber, personId, presence, presenceText, privacy);
                    } else {
                        entry = new Contact(number, label, name, nameAndNumber, personId, presence, presenceText);
                    }
                    // Aurora xuyong 2014-10-23 modified for privacy feature end
                    byte[] data = Contact.loadAvatarData(entry, mContext);
                    synchronized (entry) {
                        entry.mAvatarData = data;
                    }
                    list.add(entry);
                    removeNumberFromList(numberList, number);
                }
            } finally {
                cursor.close();
            }
            Log.d(TAG, "getByNumbers(): numberList.size():" + numberList.size());
            if (numberList.size() > 0) {
                for (String number : numberList) {
                    Contact entry = Contact.get(number, false);
                    list.add(entry);
                }
            }
            return list;
        } else {
            // only one recipient, query with block
            canBlock = true;
            return getByNumbers(semiSepNumbers, canBlock, replaceNumber);
        }
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static ContactList getByNumbers(Context mContext, String semiSepNumbers, boolean canBlock,
            boolean replaceNumber, long privacy) {
        ContactList list = new ContactList();
        if (semiSepNumbers.contains(";")) {
            String selection = Phone.NUMBER + " in ('" + semiSepNumbers.replaceAll(";", "','") + "')" + " AND is_privacy = " + privacy;
            selection = selection.replaceAll("-", "");
            LinkedList<String> numberList = new LinkedList<String>(Arrays.asList(semiSepNumbers.split(";")));
            Log.d(TAG, "ContactList.getByNumbers()--selection = \"" + selection + "\"");
            selection = DatabaseUtils.sqlEscapeString(selection);
            Cursor cursor = mContext.getContentResolver().query(PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION,
                selection, null, null);
            if (cursor == null) {
                Log.w(TAG, "ContactList.getByNumbers(" + semiSepNumbers + ") returned NULL cursor! contact uri used "
                    + PHONES_WITH_PRESENCE_URI);
                return list;
            }
            Collections.sort(numberList);
            try {
                while (cursor.moveToNext()) {
                    String number = cursor.getString(PHONE_NUMBER_COLUMN);
                    String label = cursor.getString(PHONE_LABEL_COLUMN);
                    String name = cursor.getString(CONTACT_NAME_COLUMN);
                    String mNumberE164 = cursor.getString(PHONE_NORMALIZED_NUMBER);
                    String nameAndNumber = Contact.formatNameAndNumber(name, number, mNumberE164);
                    long personId = cursor.getLong(CONTACT_ID_COLUMN);
                    int presence = cursor.getInt(CONTACT_PRESENCE_COLUMN);
                    String presenceText = cursor.getString(CONTACT_STATUS_COLUMN);
                    Contact entry = new Contact(number, label, name, nameAndNumber, personId, presence, presenceText);

                    byte[] data = Contact.loadAvatarData(entry, mContext);
                    synchronized (entry) {
                        entry.mAvatarData = data;
                    }
                    list.add(entry);
                    removeNumberFromList(numberList, number);
                }
            } finally {
                cursor.close();
            }
            Log.d(TAG, "getByNumbers(): numberList.size():" + numberList.size());
            if (numberList.size() > 0) {
                for (String number : numberList) {
                    Contact entry = Contact.get(number, false);
                    list.add(entry);
                }
            }
            return list;
        } else {
            // only one recipient, query with block
            canBlock = true;
            ContactList conl = getByNumbers(semiSepNumbers, canBlock, replaceNumber, privacy);
            return getByNumbers(semiSepNumbers, canBlock, replaceNumber, privacy);
        }
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end  
    public String getFirstName(String separator) {
        return this.get(0).getName() + " & " + (size()-1);
    }

    public static void removeNumberFromList (LinkedList<String> list, String number) {
        boolean go = true;
        int listSize = list.size();
        int i = listSize / 2;
        int start = 0;
        int end = listSize;
        while (go) {
            int j = number.compareToIgnoreCase(list.get(i));
            Log.d(TAG, "removeNumberFromList(): i=" + i + ", j=" + j + ", start=" + start + ", end=" + end);
            if (j == 0) {
                Log.d(TAG, "removeNumberFromList(): remove number: " + list.get(i));
                list.remove(i);
                break;
            } else if (j > 0) {
                start = i + 1;
            } else if (j < 0) {
                end = i;
            }
            if (i == 0 || i == (listSize - 1) || start == end) {
                break;
            }
            i = (start + end) / 2;
        }
        Log.d(TAG, "removeNumberFromList(): after remove number: list.size()=" + list.size());
    }
    //a1
}
