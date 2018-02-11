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

package com.android.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentService;
import android.os.RemoteException;
import gionee.provider.GnContactsContract;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Displays a message when there is nothing to display in a contact list.
 */
public class ContactListEmptyView extends ScrollView {

    private static final String TAG = "ContactListEmptyView";

    public ContactListEmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void hide() {
        TextView empty = (TextView) findViewById(R.id.emptyText);
        empty.setVisibility(GONE);
    }
}
