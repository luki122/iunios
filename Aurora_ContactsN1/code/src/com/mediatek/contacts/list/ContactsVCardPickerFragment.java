
package com.mediatek.contacts.list;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class ContactsVCardPickerFragment extends MultiContactsPickerBaseFragment {

    private static final String TAG = ContactsVCardPickerFragment.class.getSimpleName();

    private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP + "=1";

    final String[] sLookupProjection = new String[] {
        Contacts.LOOKUP_KEY
    };

    @Override
    public void onOptionAction() {
        Activity activity = getActivity();

        int selectedCount = this.getListView().getCheckedItemCount();

        if (selectedCount == 0) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
        }

        final Intent retIntent = new Intent();
        if (null == retIntent) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        int curArray = 0;
        long[] idArray = new long[selectedCount];
        if (null == idArray) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
        int itemCount = getListView().getCount();
        for (int position = 0; position < itemCount; ++position) {
            if (getListView().isItemChecked(position)) {
                idArray[curArray++] = adapter.getContactID(position);
                if (curArray > selectedCount) {
                    break;
                }
            }
        }

        Uri uri = null;
        if (selectedCount == 1) {
            uri = getLookupUriForEmail("Single_Contact", idArray); // single Contact
        } else {
            uri = getLookupUriForEmail("Multi_Contact", idArray); // multiple Contacts
        }

        Log.d(TAG, "The result uri is " + uri);
        retIntent.putExtra(resultIntentExtraName, uri);

        activity.setResult(Activity.RESULT_OK, retIntent);
        activity.finish();
    }

    private Uri getLookupUriForEmail(String type, long[] contactsIds) {

        Cursor cursor = null;
        Uri uri = null;
        Log.i(TAG, "type is " + type);
        if (type == "Single_Contact") {
            Log.i(TAG, "In single contact");
            uri = Uri.withAppendedPath(Contacts.CONTENT_URI, Long.toString(contactsIds[0]));
            cursor = getActivity().getContentResolver().query(uri, sLookupProjection,
                    null, null, null);

            Log.i(TAG, "cursor is " + cursor);
            if (cursor != null && cursor.moveToNext()) {

                Log.i(TAG, "Single_Contact  cursor.getCount() is " + cursor.getCount());

                uri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, cursor.getString(0));
                Log.i(TAG, "Single_Contact  uri is " + uri + " \ncursor.getString(0) is "
                        + cursor.getString(0));
            }
        } else if (type == "Multi_Contact") {
            StringBuilder sb = new StringBuilder("");
            for (long contactId : contactsIds) {
                if (contactId == contactsIds[contactsIds.length - 1]) {
                    sb.append(contactId);
                } else {
                    sb.append(contactId + ",");
                }
            }
            String selection = Contacts._ID + " in (" + sb.toString() + ")";
            Log.d(TAG, "Multi_Contact, selection=" + selection);
            cursor = getActivity().getContentResolver().query(Contacts.CONTENT_URI,
                    sLookupProjection, selection, null, null);
            if (cursor != null) {
                Log.i(TAG, "Multi_Contact  cursor.getCount() is " + cursor.getCount());
            }
            if (!cursor.moveToFirst()) {
                // Toast.makeText(this, R.string.share_error,
                // Toast.LENGTH_SHORT).show();
                return null;
            }

            StringBuilder uriListBuilder = new StringBuilder();
            int index = 0;
            for (; !cursor.isAfterLast(); cursor.moveToNext()) {
                if (index != 0)
                    uriListBuilder.append(':');
                uriListBuilder.append(cursor.getString(0));
                index++;
            }
            uri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI, Uri.encode(uriListBuilder
                    .toString()));
            Log.i(TAG, "Multi_Contact  uri is " + uri);
        }
        if (cursor != null)
            cursor.close();

        return uri;

    }

}
