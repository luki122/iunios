/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts.list;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.util.ThemeUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

// The following lines are provided and maintained by Mediatek Inc.
// Description: for SIM name display
import com.android.contacts.util.AccountFilterUtil;
// The previous lines are provided and maintained by Mediatek Inc.

//Gionee:wangth 20120413 add for CR00572641 begin
import com.android.contacts.ContactsUtils;
import com.android.contacts.model.AccountType;
//Gionee:wangth 20120413 add for CR00572641 end
/**
 * Contact list filter parameters.
 */
public class ContactListFilterView extends LinearLayout {

    private ImageView mIcon;
    private TextView mLabel;
    private View mIndent;
    private ContactListFilter mFilter;
    private boolean mSingleAccount;
    private int mActivatedBackground;

    public ContactListFilterView(Context context) {
        super(context);
    }

    public ContactListFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setContactListFilter(ContactListFilter filter) {
        mFilter = filter;
    }

    public ContactListFilter getContactListFilter() {
        return mFilter;
    }

    public void setSingleAccount(boolean flag) {
        this.mSingleAccount = flag;
    }

    public void bindView(boolean dropdown) {
        if (dropdown) {
            if (mActivatedBackground == 0) {
                mActivatedBackground = ThemeUtils.getActivatedBackground(getContext().getTheme());
            }
            setBackgroundResource(mActivatedBackground);
        }

        if (mLabel == null) {
            mIcon = (ImageView) findViewById(R.id.icon);
            mLabel = (TextView) findViewById(R.id.label);
            mIndent = findViewById(R.id.indent);
        }

        if (mFilter == null) {
            mLabel.setText(R.string.contactsList);
            return;
        }

        switch (mFilter.filterType) {
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
                // gionee xuhz 201020523 modify for CR00601249 start
                if (ContactsApplication.sIsGnDarkStyle) {
                    bindView(R.drawable.gn_ic_menu_contacts_holo_dark, R.string.list_filter_all_accounts,
                            dropdown);
                } else {
                    bindView(R.drawable.ic_menu_contacts_holo_light, R.string.list_filter_all_accounts,
                            dropdown);
                }
                // gionee xuhz 201020523 modify for CR00601249 end
                break;
            }
            case ContactListFilter.FILTER_TYPE_STARRED: {
                bindView(R.drawable.ic_menu_star_holo_light, R.string.list_filter_all_starred,
                        dropdown);
                break;
            }
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
            	bindView(ResConstant.getIconRes(ResConstant.IconTpye.Setting),
                        dropdown ? R.string.list_filter_customize : R.string.list_filter_custom,
                        dropdown);
                break;
            }
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY: {
                bindView(0, R.string.list_filter_phones, dropdown);
                break;
            }
            case ContactListFilter.FILTER_TYPE_SINGLE_CONTACT: {
                bindView(0, R.string.list_filter_single, dropdown);
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
                mIcon.setVisibility(View.VISIBLE);
                if (mFilter.icon != null) {
                    mIcon.setImageDrawable(mFilter.icon);
                } else {
                    mIcon.setImageResource(R.drawable.unknown_source);
                }
                // The following lines are provided and maintained by Mediatek Inc.
                // Description: for SIM name display
                // Keep previous code here.
                // mLabel.setText(mFilter.accountName);
                String displayName = null;
                displayName = AccountFilterUtil.getAccountDisplayNameByAccount(mFilter.accountType, mFilter.accountName);
                if (null == displayName) {
                    //Gionee:wangth 20120413 modify for CR00572641 begin
                    //mLabel.setText(mFilter.accountName);
                    if (ContactsUtils.mIsGnContactsSupport && AccountType.ACCOUNT_TYPE_LOCAL_PHONE.equals(mFilter.accountType)) {
                        mLabel.setText(R.string.gn_phone_text);
                    } else {
                        mLabel.setText(mFilter.accountName);
                    }
                    //Gionee:wangth 20120413 modify for CR00572641 end
                } else {
                    mLabel.setText(displayName);
                }
                // The previous lines are provided and maintained by Mediatek Inc.                
                if (dropdown) {
                    mIndent.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    private void bindView(int iconResource, int textResource, boolean dropdown) {
        if (iconResource != 0) {
            mIcon.setVisibility(View.VISIBLE);
            mIcon.setImageResource(iconResource);
        } else {
            mIcon.setVisibility(dropdown ? View.INVISIBLE : View.GONE);
        }

        mLabel.setText(textResource);

        if (mIndent != null) {
            mIndent.setVisibility(View.GONE);
        }
    }
    
    //Gionee:huangzy 20120710 add add CR00614794 start    
    public void bindView(Drawable icon, String text) {
        mIcon.setVisibility(View.VISIBLE);
        mIcon.setImageDrawable(icon);
        mLabel.setText(text);
    }
    //Gionee:huangzy 20120710 add add CR00614794 end
}
