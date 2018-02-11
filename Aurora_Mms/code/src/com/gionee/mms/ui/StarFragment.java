package com.gionee.mms.ui;
import java.util.ArrayList;
import java.util.List;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.gionee.mms.ui.AddReceiptorTab.ContactsPicker;
import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;

public class StarFragment extends Fragment implements ContactsPicker{

    private Activity mActivity;

    private static final String TAG = "StarFragment";
    boolean mJustCreated = false;
    private LinearLayout mEmptyView;
    private AuroraListView mList;
    private ContactsListArrayAdapter mAdapter;

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
        for (int i = 0; i < mAdapter.getCount(); i++) {
            ContactsCacheSingleton.ContactListItemCache cache = mAdapter.getItem(i);
            if (cache.mIsChecked == false) {
                isAllChecked = false;
                break;
            }
        }
        return isAllChecked;
    }

    private AdapterView.OnItemClickListener mStarListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            mAdapter.checkBoxClicked(position, view);
            updateSelectDoneText();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mActivity = AddReceiptorTab.mCurrent;
        super.onCreate(savedInstanceState);
    };

    private MenuItem mMenuSelectAllItem;
    private MenuItem mMenuOkItem;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        // select all
        mMenuSelectAllItem = menu.findItem(R.id.gn_add_select_all);
        mMenuSelectAllItem.setEnabled(true);
        if(mEmptyView.getVisibility() == View.VISIBLE) {
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
        View fragmentView = inflater.inflate(R.layout.gn_star_fragment_list_layout, container, false);
        mList = (AuroraListView) fragmentView.findViewById(R.id.list);
        mEmptyView = (LinearLayout) fragmentView.findViewById(R.id.gn_star_contacts_empty);
        TextView emptytTextView = (TextView) fragmentView.findViewById(R.id.gn_star_contacts_text);
        if (MmsApp.mDarkStyle) {
            emptytTextView.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }
        mJustCreated = true;
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        // Initialize the list adapter with a null cursor.
        mAdapter = new ContactsListArrayAdapter(getActivity());
        for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getStarList()) {
            mAdapter.add(cache);
        }
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mStarListClickListener);
    }

    public void updateAdapter() {
        //Gionee <zhouyj> <2013-04-24> modify for CR00801550 start
        if (mAdapter != null && mAdapter.isEmpty()) {
            for (ContactListItemCache cache : ContactsCacheSingleton.getInstance().getStarList()) {
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

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
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
        }

        public ContactsListArrayAdapter(Context context,
                List<ContactsCacheSingleton.ContactListItemCache> objects) {
            super(context, 0, objects);
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

    public void markAll() {
        // TODO Auto-generated method stub
        mAdapter.setAllItemCheckState(true);
        updateSelectDoneText();
    }

    public void updateButton() {
        // TODO Auto-generated method stub
        updateSelectDoneText();
    }

}
