/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.mediatek.contacts.widget;

import com.android.contacts.R;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.QuickContactBadge;
import com.android.contacts.calllog.PhoneNumberHelper;

/**
 * Widget used to show an image with the standard QuickContact badge
 * and on-click behavior.
 */
public class QuickContactBadgeWithPhoneNumber extends QuickContactBadge implements OnClickListener {
    private String mPhoneNumber;
    private boolean mIsSipNumber;
    
    public QuickContactBadgeWithPhoneNumber(Context context) {
        this(context, null);
    }

    public QuickContactBadgeWithPhoneNumber(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickContactBadgeWithPhoneNumber(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOnClickListener(this);
    }
    
	public void assignPhoneNumber(String number, boolean isSipCallNumber) {
		mPhoneNumber = number;
		if (PhoneNumberHelper.canPlaceCallsTo(number)) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
		mIsSipNumber = isSipCallNumber;
	}
    
    @Override
    public void onClick(View v) {
    	if (mPhoneNumber != null) {
    		ShowDialog(mPhoneNumber);
    	} else {
    		super.onClick(v);
    	}
    }
    
	private void ShowDialog(final String number) {
		if (!TextUtils.isEmpty(number)) {
			String message = mContext.getString(
					R.string.add_contact_dlg_message_fmt, number);
			String title = mContext.getString(R.string.add_contact_dlg_title);
			AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(mContext).setTitle(
					title).setMessage(message).setNegativeButton(
					android.R.string.cancel, null).setPositiveButton(
					android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(
									Intent.ACTION_INSERT_OR_EDIT);
							intent.setType(Contacts.CONTENT_ITEM_TYPE);
							if (mIsSipNumber) {
								intent
										.putExtra(
												ContactsContract.Intents.Insert.SIP_ADDRESS,
												number);
							} else {
								intent.putExtra(Insert.PHONE, number);
							}
							mContext.startActivity(intent);
						}
					}).create();
			dialog.show();
		}
	}
}
