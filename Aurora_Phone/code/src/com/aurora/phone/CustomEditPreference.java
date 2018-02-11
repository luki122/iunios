/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.DialerKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import aurora.widget.*;
import aurora.app.*;
import aurora.preference.*;

public class CustomEditPreference extends AuroraEditTextPreference {

	public CustomEditPreference(Context context) {
		this(context, null);
	}

	/*
	 * Constructors
	 */
	public CustomEditPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	// called when we're binding the dialog to the preference's view.
	@Override
	protected void onBindDialogView(View view) {

		super.onBindDialogView(view);

		// get the edittext component within the number field
		final EditText editText = getEditText();
		// get the contact pick button within the number field

		// setup number entry
		if (editText != null) {
			editText.setOnFocusChangeListener(mDialogFocusChangeListener);
			editText.addTextChangedListener(new TextWatcher() {
				public void afterTextChanged(Editable s) {
					int length = editText.getText().toString().length();
					AuroraAlertDialog dialog = (AuroraAlertDialog) (CustomEditPreference.this
							.getDialog());
					if (dialog != null) {
						Button pButton = dialog
								.getButton(DialogInterface.BUTTON_POSITIVE);
						if (pButton != null)
							pButton.setEnabled(length > 1 ? true : false);
					}
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}
			});
		}

	}

	// Listeners
	/** Called when focus is changed between fields */
	private View.OnFocusChangeListener mDialogFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				TextView textView = (TextView) v;
				String number = textView.getText().toString();
				int length = 0;
				if (!TextUtils.isEmpty(number)) {
					length = number.length();
				}
				AuroraAlertDialog dialog = (AuroraAlertDialog) (CustomEditPreference.this
						.getDialog());
				if (dialog != null) {
					Log.i("onBindDialogView", "dialog not null");
					Button pButton = dialog
							.getButton(DialogInterface.BUTTON_POSITIVE);
					if (pButton != null)
						pButton.setEnabled(length > 1 ? true : false);
				}
			}
		}
	};

}
