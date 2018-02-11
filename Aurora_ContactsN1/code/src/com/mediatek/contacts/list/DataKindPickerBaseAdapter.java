
package com.mediatek.contacts.list;

import java.util.ArrayList;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraListView;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.R;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.widget.IndexerListAdapter.Placement;

public abstract class DataKindPickerBaseAdapter extends ContactEntryListAdapter {

    private static final String TAG = DataKindPickerBaseAdapter.class.getSimpleName();
    private AuroraListView mListView = null;
    private ContactListItemView.PhotoPosition mPhotoPosition = null;
    private Context mContext = null;

    public DataKindPickerBaseAdapter(Context context, AuroraListView lv) {
        super(context);
        mListView = lv;
        mContext = context;
    }

    protected AuroraListView getListView() {
        return mListView;
    }

    @Override
    public final void configureLoader(CursorLoader loader, long directoryId) {

        loader.setUri(configLoaderUri(directoryId));
        loader.setProjection(configProjection());
        configureSelection(loader, directoryId, getFilter());

        // Set the Contacts sort key as sort order
        String sortOrder;
        if (getSortOrder() == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            sortOrder = Contacts.SORT_KEY_PRIMARY;
        } else {
            sortOrder = Contacts.SORT_KEY_ALTERNATIVE;
        }

        loader.setSortOrder(sortOrder);
    }

    protected abstract String[] configProjection();

    protected abstract Uri configLoaderUri(long directoryId);

    protected abstract void configureSelection(CursorLoader loader, long directoryId,
            ContactListFilter filter);

    protected static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
    }

    @Override
    protected View newView(Context context, int partition, Cursor cursor, int position,
            ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(context.getText(android.R.string.unknownName));
        view.setQuickContactEnabled(isQuickContactEnabled());

        // Enable check box
        view.setCheckable(true);
        if (mPhotoPosition != null) {
            view.setPhotoPosition(mPhotoPosition);
        }
        view.setActivatedStateSupported(true);
        return view;
    }

    public void displayPhotoOnLeft() {
        mPhotoPosition = ContactListItemView. PhotoPosition.START;
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        final ContactListItemView view = (ContactListItemView) itemView;

        // Look at elements before and after this position, checking if contact
        // IDs are same.
        // If they have one same contact ID, it means they can be grouped.
        //
        // In one group, only the first entry will show its photo and names
        // (display name and
        // phonetic name), and the other entries in the group show just their
        // data (e.g. phone
        // number, email address).
        cursor.moveToPosition(position);
        boolean isFirstEntry = true;
        boolean showBottomDivider = true;
        final long currentContactId = cursor.getLong(getContactIDColumnIndex());
        if (cursor.moveToPrevious() && !cursor.isBeforeFirst()) {
            final long previousContactId = cursor.getLong(getContactIDColumnIndex());
            if (currentContactId == previousContactId) {
                isFirstEntry = false;
            }
        }
        cursor.moveToPosition(position);
        if (cursor.moveToNext() && !cursor.isAfterLast()) {
            final long nextContactId = cursor.getLong(getContactIDColumnIndex());
            if (currentContactId == nextContactId) {
                // The following entry should be in the same group, which means
                // we don't want a
                // divider between them.
                // TODO: we want a different divider than the divider between
                // groups. Just hiding
                // this divider won't be enough.
                showBottomDivider = false;
            }
        }
        cursor.moveToPosition(position);

        bindSectionHeaderAndDivider(view, position, cursor);

        if (isFirstEntry) {
            bindName(view, cursor);
            if (isQuickContactEnabled()) {
                bindQuickContact(view, partition, cursor);
            } else {
                bindPhoto(view, cursor);
            }
        } else {
            unbindName(view);

            view.removePhotoView(true, false);
        }

        bindData(view, cursor);

        if (isSearchMode()) {
            // bindSearchSnippet(view, cursor);
        } else {
            view.setSnippet(null);
        }
        view.setDividerVisible(showBottomDivider);

        view.getCheckBox().setChecked(mListView.isItemChecked(position));
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position, Cursor cursor) {
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);

            view.setSectionHeader(placement.sectionHeader);
            view.setDividerVisible(!placement.lastInSection);
        } else {
            view.setSectionHeader(null);
            view.setDividerVisible(true);
        }
    }

    protected void bindPhoto(final ContactListItemView view, Cursor cursor) {
        long photoId = 0;
        if (!cursor.isNull(getPhotoIDColumnIndex())) {
            photoId = cursor.getLong(getPhotoIDColumnIndex());
        }

        int indicatePhoneSim = cursor.getInt(getIndicatePhoneSIMColumnIndex());
        if (indicatePhoneSim > 0) {
            photoId = getSimType(indicatePhoneSim);
        }

        getPhotoLoader().loadPhoto(view.getPhotoView(), photoId, false, false);
    }

    protected void bindData(ContactListItemView view, Cursor cursor) {
        CharSequence label = null;
        if (!cursor.isNull(getDataTypeColumnIndex())) {
            final int type = cursor.getInt(getDataTypeColumnIndex());
            final String customLabel = cursor.getString(getDataLabelColumnIndex());

            // TODO cache
            label = Phone.getTypeLabel(mContext.getResources(), type, customLabel);
        }
        view.setLabel(label);
        view.showData(cursor, getDataColumnIndex());
    }

    protected void unbindName(final ContactListItemView view) {
        view.hideDisplayName();
        view.hidePhoneticName();
    }

    public abstract Uri getDataUri(int position);

    public abstract long getDataId(int position);

    public abstract void bindName(final ContactListItemView view, Cursor cursor);

    public abstract void bindQuickContact(final ContactListItemView view, int partitionIndex,
            Cursor cursor);

    public abstract int getPhotoIDColumnIndex();

    public abstract int getDataTypeColumnIndex();

    public abstract int getDataLabelColumnIndex();

    public abstract int getDataColumnIndex();

    public abstract int getContactIDColumnIndex();

    public abstract int getPhoneticNameColumnIndex();

    public abstract int getIndicatePhoneSIMColumnIndex();

    public boolean hasStableIds() {
        return false;
    }

    public long getItemId(int position) {
        return getDataId(position);
    }

    public int getMyCheckedItemCount(long[] checkedItems, Cursor cursor) {
        if (checkedItems == null || cursor == null || cursor.getCount() == 0) {
            return 0;
        }

        int checkItems = 0;
        int contactId = -1;
        ArrayList<Long> items = new ArrayList<Long>();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            contactId = -1;
            contactId = cursor.getInt(0);

            items.add(Long.valueOf(contactId));
        }
        cursor.moveToPosition(-1);
        for (long item : checkedItems) {
            if (items.contains(item)) {
                ++checkItems;
            }
        }
        return checkItems;
    }

}
