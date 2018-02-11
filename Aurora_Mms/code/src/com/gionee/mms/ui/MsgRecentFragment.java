/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.ui;

import java.util.List;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.data.RecentContact;
import com.gionee.mms.ui.AddReceiptorTab.ContactsPicker;
import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;

import aurora.app.AuroraAlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MsgRecentFragment extends Fragment implements ContactsPicker{
    private Activity mActivity;
    private final String TAG = "MsgRecentFragment";
    private AuroraListView mList;
    private ContactsListArrayAdapter mAdapter;

    // Change the following according to our situations
    private LinearLayout mEmptyView;

    boolean mJustCreated = false;
    
    private static final int MENU_SELECT_CANCEL = 0;
    private static final int MENU_SELECT_DONE   = 1;
    private static final int MENU_SELECT_ALL    = 2;

    public void updateSelectDoneText() {
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end
        // gionee zhouyj 2013-01-23 modify for CR00765961 start 
        if (mActivity != null) {
            mActivity.invalidateOptionsMenu();
        }
        // gionee zhouyj 2013-01-23 modify for CR00765961 end 
    }
    
    private boolean isAllChecked() {
        boolean isAllChecked = true;
        for (int i = 0; mAdapter != null && i < mAdapter.getCount(); i++) {
            ContactsCacheSingleton.ContactListItemCache cache = mAdapter.getItem(i);
            if (cache.mIsChecked == false) {
                isAllChecked = false;
                break;
            }
        }
        return isAllChecked;
    }

    private AdapterView.OnItemClickListener mRecentListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            mAdapter.checkBoxClicked(position, view);
            updateSelectDoneText();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mActivity = AddReceiptorTab.mCurrent;
        super.onCreate(savedInstanceState);
    }

    private MenuItem mMenuSelectAllItem;
    private MenuItem mMenuOkItem;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // select all
        mMenuSelectAllItem = menu.findItem(R.id.gn_add_select_all);
        mMenuSelectAllItem.setEnabled(true);
        if(mEmptyView != null && mEmptyView.getVisibility() == View.VISIBLE) {
            mMenuSelectAllItem.setTitle(R.string.select_all);
            mMenuSelectAllItem.setEnabled(false);
        } else if(isAllChecked()) {
            mMenuSelectAllItem.setTitle(R.string.unselect_all);
        } else {
            mMenuSelectAllItem.setTitle(R.string.select_all);
        }
        
        // ok
        mMenuOkItem = menu.findItem(R.id.gn_add_select_done);
        int checkedCount = ContactsCacheSingleton.getInstance().getCheckedCount();
        if(checkedCount == 0) {
            mMenuOkItem.setTitle(R.string.gn_confirm);
            mMenuOkItem.setEnabled(false);
        } else {
            mMenuOkItem.setTitle(getString(R.string.gn_confirm) + "(" + checkedCount + ")");
            mMenuOkItem.setEnabled(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.gn_add_select_all:
            mAdapter.setAllItemCheckState(item.getTitle().toString().equals(getString(R.string.select_all)));
            updateSelectDoneText();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View fragmentView = inflater.inflate(R.layout.gn_recent_contact_list_layout, container, false);
        mList = (AuroraListView) fragmentView.findViewById(R.id.list);
        mEmptyView = (LinearLayout) fragmentView.findViewById(R.id.gn_recent_contacts_empty);
        TextView emptytTextView = (TextView) fragmentView.findViewById(R.id.gn_recent_contact_text);
        if (MmsApp.mDarkStyle) {
            emptytTextView.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }
        mJustCreated = true;
        return fragmentView;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //gionee gaoj 2012-10-18 added for CR00711168 start
        if (MmsApp.mIsSafeModeSupport) {
            mList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }
        //gionee gaoj 2012-10-18 added for CR00711168 end
        if (mAdapter.getCount() > 0) {
            mEmptyView.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        } else {
            mList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        if (mJustCreated == true) {
            mJustCreated = false;
        } else {
            mAdapter.notifyDataSetChanged();
        }
        updateSelectDoneText();
    }

    private final class ContactsListArrayAdapter extends
            ArrayAdapter<ContactsCacheSingleton.ContactListItemCache> {

        private ContactPhotoLoader mPhotoLoader = null;
        public void bindView(View itemView, Context context, int position) {
            final ContactListItemView view = (ContactListItemView) itemView;
            final ContactsCacheSingleton.ContactListItemCache cache = getItem(position);
            view.getCheckBox().setChecked(cache.mIsChecked);
            // Aurora xuyong 2014-07-19 modified for sougou start
            String area = MessageUtils.getNumAreaFromAora(context, cache.mNumber);
            // Aurora xuyong 2014-07-19 modified for sougou end
            /*When no photo and the name is empty, we think it as an unknown people*/
            if (cache.mNames.isEmpty()){
                view.getNameTextView().setText(cache.mNumber);

                if (area == null || "".equals(area)) {
                    view.getDataView().setVisibility(View.GONE);
                } else {
                    view.getDataView().setVisibility(View.VISIBLE);
                    view.getDataView().setText(area);
                }
            }else{
                view.getNameTextView().setText(cache.getNameString());
                view.getDataView().setVisibility(View.VISIBLE);
                if (area == null || "".equals(area)) {
                    view.getDataView().setText(cache.mNumber);
                } else {
                    view.getDataView().setText(cache.mNumber + "  " + area);
                }
            }

        }

        public void checkBoxClicked(int position, View convertView) {
            final ContactListItemView view = (ContactListItemView) convertView;

            view.getCheckBox().setChecked(!view.getCheckBox().isChecked());
            getItem(position).mIsChecked = view.getCheckBox().isChecked();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                final ContactListItemView view = new ContactListItemView(getContext(), null);
                v = (View) view;
            } else {
                v = convertView;
            }
            bindView(v, getContext(), position);
            return v;
        }

        public ContactsListArrayAdapter(Context context) {
            super(context, 0);
            mPhotoLoader = new ContactPhotoLoader(context, -1);
        }

        public ContactsListArrayAdapter(Context context,
                List<ContactsCacheSingleton.ContactListItemCache> objects) {
            super(context, 0, objects);
            mPhotoLoader = new ContactPhotoLoader(context, -1);
        }

        public void setAllItemCheckState(boolean isCheck) {
            // mark all menu item
            for (int index = 0; index < getCount(); index++) {
                ContactsCacheSingleton.ContactListItemCache contact = getItem(index);
                contact.mIsChecked = isCheck;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        // Initialize the list adapter with a null cursor.
        mAdapter = new ContactsListArrayAdapter(getActivity());
        for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getRecentCallLogList()) {
              // if ((cache.mTag & ContactListItemCache.TAG_RECENT_CONTACT) != 0) {
                mAdapter.add(cache);
              // }
        }
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mRecentListClickListener);
        mList.setOnItemLongClickListener(mItemLongClickListner);
    }
    
    public void updateAdapter() {
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        if (mAdapter != null && mAdapter.isEmpty()) {
            for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getRecentCallLogList()) {
                mAdapter.add(cache);
            }
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getCount() > 0) {
                mEmptyView.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
            }
        }
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 end
    }

    private OnItemLongClickListener mItemLongClickListner = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
            final ContactsCacheSingleton.ContactListItemCache contactItem = mAdapter
                    .getItem(position);
            final String number = contactItem.mNumber;
            final boolean isinCallLog = ContactsCacheSingleton.getInstance().getCallLogList().contains(contactItem);
            Contact contact = Contact.get(number, false);
            String title = contact.getName();
            if (title == null || title.length() == 0) {
                title = contact.getNumber();
            }
            Context context = null;
            if (MmsApp.mLightTheme) {
                context = new ContextThemeWrapper(getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog);
            } else {
                context = new ContextThemeWrapper(getActivity(),
                        android.R.style.Theme_Holo_Dialog);
            }
            final Context dlgContext = context;

            final LayoutInflater dlgInflater = (LayoutInflater) dlgContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(
                    getActivity(), android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = dlgInflater.inflate(android.R.layout.simple_list_item_1,
                                parent, false);
                    }
                    final int resID = this.getItem(position);
                    ((TextView) convertView).setText(resID);
                    ((TextView) convertView).setTextSize(20);
                    //Gionee <zhouyj> <2013-05-23> add for CR00799376 begin for super theme
                    if (MmsApp.mDarkTheme) {
                        ((TextView) convertView).setTextColor(dlgContext.getResources().getColor(R.color.gn_title_name_color));
                    }
                    //Gionee <zhouyj> <2013-05-23> add for CR00799376 end
                    return convertView;
                }
            };

            adapter.add(R.string.delete_recent_contact);

            final OnClickListener clickListener = new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();

                    final int resID = adapter.getItem(whichButton);
                    switch (resID) {
                        case R.string.delete_recent_contact:

                            //gionee gaoj 2012-12-3 added for CR00738791 start
                            new AuroraAlertDialog.Builder(getActivity()/* , AuroraAlertDialog.THEME_AMIGO_FULLSCREEN*/)
                            .setTitle(R.string.gn_confirm_dialog_title)
                            .setMessage(isinCallLog ? getResources().getString(R.string.gn_confirm_delete_calllog_mms) : getResources().getString(R.string.gn_confirm_delete_recent_mms))
                            .setNegativeButton(R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    // TODO Auto-generated method stub
                                    RecentContact.deleteRecordByNumber(number);
                                    if (isinCallLog) {
                                        //gionee gaoj 2012-12-5 added for CR00739410 start
                                        StringBuilder selection = new StringBuilder();
                                        selection.append("number = '").append(number).append("'");
                                        //gionee gaoj 2012-12-5 added for CR00739410 end
                                        getActivity().getContentResolver().delete(Calls.CONTENT_URI, selection.toString(), null);
                                    }
                                    mAdapter.remove(contactItem);
                                    if (mAdapter.getCount() == 0) {
                                        mList.setVisibility(View.GONE);
                                        // start
                                        mEmptyView.setVisibility(View.VISIBLE);
                                    }
                                    //gionee gaoj 2012-12-5 added for CR00738917 start
                                    ContactsCacheSingleton.getInstance().setCheckFalse(number);
                                    getActivity().invalidateOptionsMenu();
                                    //gionee gaoj 2012-12-5 added for CR00738917 end
                                }
                                
                            }).show();
                            //gionee gaoj 2012-12-3 added for CR00738791 end 
                            break;
                        default:
                            Log.e(TAG, "Unexpected resource");
                    }
                }
            };

            new AuroraAlertDialog.Builder(getActivity()).setTitle(title)
            .setCancelIcon(true)
            .setSingleChoiceItems(adapter, -1, clickListener).show();

            return true;
        }
    };
    
    public void markAll() {
        mAdapter.setAllItemCheckState(true);
        updateSelectDoneText();
    }

    @Override
    public void updateButton() {
        updateSelectDoneText();
    }
}
