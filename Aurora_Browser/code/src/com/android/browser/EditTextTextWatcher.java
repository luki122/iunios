package com.android.browser;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraEditText;

public class EditTextTextWatcher implements TextWatcher {
	private AuroraAlertDialog dialog;
	private Button btnSaveBkmk;
	private AuroraEditText etTitle;
	private AuroraEditText etUrl;
	public EditTextTextWatcher(AuroraAlertDialog dialog,AuroraEditText etTitle, AuroraEditText etUrl) {
		this.dialog = dialog;
		this.etTitle = etTitle;
		this.etUrl = etUrl;
	}
	
	public EditTextTextWatcher(Button btnSaveBkmk,AuroraEditText etTitle, AuroraEditText etUrl) {
		this.btnSaveBkmk = btnSaveBkmk;
		this.etTitle = etTitle;
		this.etUrl = etUrl;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		if(btnSaveBkmk == null && dialog != null) {
			btnSaveBkmk = ((AuroraAlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
		}
    	if(this.etTitle.getText().toString().equals("") || this.etUrl.getText().toString().equals("")) {
    		btnSaveBkmk.setEnabled(false);
    	}else {
    		btnSaveBkmk.setEnabled(true);
    	}
	}
	
}
