package com.mediatek.contacts.list;

import java.util.Iterator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import gionee.provider.GnContactsContract.Contacts;
import android.util.Log;
import android.widget.Toast;
import com.android.contacts.R;

public class MultiContactsShareFragment extends MultiContactsPickerBaseFragment {

    private static final String TAG = "MultiContactsShareFragment";

    @Override
    public void onOptionAction() {
        
        Activity activity = getActivity();

        int selectedCount = this.getListView().getCheckedItemCount();

        if (selectedCount == 0) {
            return;
//            activity.setResult(Activity.RESULT_CANCELED, null);
//            activity.finish();
        }

        final Intent retIntent = new Intent();
        if (null == retIntent) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        int curArray = 0;
        String[] idArrayUri = new String[selectedCount];
        if (null == idArrayUri) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
        int itemCount = getListView().getCount();
        
        if (selectedCount > 5000) {
            Toast.makeText(getContext(), R.string.share_contacts_limit, Toast.LENGTH_LONG).show();
            return;
        }
        
        for (int position = 0; position < itemCount; ++position) {
            if (getListView().isItemChecked(position)) {
                idArrayUri[curArray++] = adapter.getContactLookUpKey(position);
                if (curArray > selectedCount) {
                    break;
                }
            }
        }

        retIntent.putExtra(resultIntentExtraName, idArrayUri);
        doShareVisibleContacts("Multi_Contact",null,idArrayUri);
        activity.setResult(Activity.RESULT_OK, retIntent);
        activity.finish();
    }
    
    private void doShareVisibleContacts(String type, Uri uri ,String[] idArrayUriLookUp) {
        if (idArrayUriLookUp == null || idArrayUriLookUp.length == 0) {
            return;
        }

//        final String lookupKey = mContactData.getLookupKey();
        StringBuilder uriListBuilder = new StringBuilder();
        int index = 0;
        for (int i = 0; i < idArrayUriLookUp.length; i++) {
            if (index != 0) {
                uriListBuilder.append(":");    
            }
            // find lookup key
            uriListBuilder.append(idArrayUriLookUp[i]);
            index++;
        }
        
//        Log.i(TAG, "-----------------uriListBuilder is " + uriListBuilder.toString());
        Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI, Uri.encode(uriListBuilder.toString()));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setDataAndType(shareUri, Contacts.CONTENT_VCARD_TYPE);
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
//        intent.putExtra("contactId", String.valueOf(mContactData.getContactId()));
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        intent.putExtra("LOOKUPURIS", uriListBuilder.toString());

        // Launch chooser to share contact via
        final CharSequence chooseTitle = getText(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

        try {
            startActivity(chooseIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.share_error, Toast.LENGTH_SHORT).show();
        }
    }
    
}
