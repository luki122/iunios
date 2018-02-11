package com.android.contacts;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.contacts.list.AccountFilterActivity;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.list.ContactListFilterView;
import com.android.contacts.list.CustomContactListFilterActivity;
import com.android.contacts.list.AccountFilterActivity.FilterListAdapter;

//Gionee:huangzy 20120710 add CR00614794 start
public class GnFilterHeaderClickListener implements OnClickListener {
    private Activity mActivity;
    private View mAccountFilterHeader;
    
    public GnFilterHeaderClickListener(Activity activity, View accountFilterHeader) {
        mActivity = activity;
        mAccountFilterHeader = accountFilterHeader;
    }
    
    private PopupWindow mPopupWindow;
    private List<ContactListFilter> mFilters;
    
    @Override
    public void onClick(View view) {
        showFilterSelector();
    }
    
    private void showFilterSelector() {
        // Gionee:wangth 20121120 modify for CR00715827 begin
        mFilters = AccountFilterActivity.loadAccountFilters(mActivity);
        // Gionee:wangth 20121120 modify for CR00715827 end

        FilterListAdapter adapter = new AccountFilterActivity.FilterListAdapter(mActivity, mFilters) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ContactListFilterView clfv = (ContactListFilterView) super.getView(position, convertView, parent);
                
                if (ContactsApplication.sIsGnTransparentTheme) {
                    ((TextView)(clfv.findViewById(R.id.label))).setTextColor(Color.BLACK);
                    
                    switch(clfv.getContactListFilter().filterType) {                
                        case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
                            ((ImageView)(clfv.findViewById(R.id.icon))).setImageResource(R.drawable.ic_menu_contacts_holo_light);
                            break;
                        }
                        case ContactListFilter.FILTER_TYPE_CUSTOM: {
                            ((ImageView)(clfv.findViewById(R.id.icon))).setImageResource(R.drawable.ic_menu_settings_holo_light);
                            break;
                        }
                    }
                }
                
                return clfv;
            }
        };
        
        AuroraListView ll = new AuroraListView(mActivity);
        ll.setAdapter(adapter);
        if (ContactsApplication.sIsGnTransparentTheme) {
            ll.setBackgroundColor(Color.WHITE);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            TypedArray a = mActivity.obtainStyledAttributes(null,
                    com.android.internal.R.styleable.Spinner, 
                    com.android.internal.R.attr.spinnerStyle, 0);
            ll.setBackgroundDrawable(a.getDrawable(com.android.internal.R.styleable.Spinner_popupBackground));            
        } else if (ContactsApplication.sIsGnLightTheme) {
            ll.setBackgroundColor(Color.WHITE);
        }
        mPopupWindow = new PopupWindow(ll, LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setWidth(mAccountFilterHeader.getWidth());
        mPopupWindow.update();
        mPopupWindow.showAsDropDown(mAccountFilterHeader);
        
        ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (null != mPopupWindow && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                    mPopupWindow = null;
                }
                
                ContactListFilter filter = mFilters.get(position);
                
                updataAccountFilterHeader(mAccountFilterHeader, (ContactListFilterView) view, filter);
                 
                if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                    final Intent intent = new Intent(mActivity, CustomContactListFilterActivity.class);
                    mActivity.startActivity(intent);
                }
                 
                ContactListFilterController.getInstance(mActivity).setContactListFilter(filter, true);
            }
        });
    }
    
    private static void updataAccountFilterHeader(View mAccountFilterHeader, ContactListFilterView selectedView,
            ContactListFilter filter) {
        TextView label = (TextView)(mAccountFilterHeader.findViewById(R.id.account_filter_header));
        if (null != label) {
            label.setText(((TextView)((selectedView).findViewById(R.id.label))).getText());
        }
        
        ImageView icon = (ImageView)(mAccountFilterHeader.findViewById(R.id.account_type_icon));
        if (null != icon) {
            if (ContactsApplication.sIsGnTransparentTheme) {
                if (filter.filterType == ContactListFilter.FILTER_TYPE_CUSTOM) {
                    icon.setImageResource(R.drawable.gn_ic_menu_settings_holo_dark);
                    return;   
                } else if (filter.filterType == ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS) {
                    icon.setImageResource(R.drawable.ic_see_contacts_holo_dark);
                    return;
                }                
            }
            
            icon.setImageDrawable(((ImageView)(selectedView.findViewById(R.id.icon))).getDrawable());
        }
    }
    
    public static void initAccountFilterLabalAndIcon(Context context, ContactListFilter curFilter,
            View accountFilterHeader) {
        List<ContactListFilter> filters = AccountFilterActivity.loadAccountFilters(context);
        FilterListAdapter adapter = new AccountFilterActivity.FilterListAdapter(context, filters);
        
        ContactListFilter tmpFilter;
        for (int i = 0, size = filters.size(); i < size; i++) {
            tmpFilter = filters.get(i);
            if (tmpFilter.equals(curFilter)) {
                View view = adapter.getView(i, null, null);
                updataAccountFilterHeader(accountFilterHeader, (ContactListFilterView) view, tmpFilter);
                break;
            }
        }
    }
}
//Gionee:huangzy 20120710 add CR00614794 end