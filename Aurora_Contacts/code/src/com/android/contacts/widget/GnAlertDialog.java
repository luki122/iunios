package com.android.contacts.widget;

import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.contacts.R;

import aurora.widget.AuroraEditText;

public class GnAlertDialog {
	
	public interface GnStdInputLisenter {
		void onPositiveClicked(String inputed);
		void onNegativeClicked();
	}
	
	public static AuroraAlertDialog createStdInputDialog(
			Context context,
			String title,
			final GnStdInputLisenter stdInputLisenter,
			CharSequence hint,
			int inputType,
			final String defaultInput,
			final TextView viewBinded) {
    	
		View customView = LayoutInflater.from(context).inflate(
                R.layout.gn_std_input_dialog_custom_view, null);
        
        final AuroraEditText editText = (AuroraEditText)customView.findViewById(R.id.custom_dialog_content);        
        if (-1 != inputType) {
        	editText.setInputType(inputType);	
        }
        editText.setHint(hint);
        
        final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(context)
        		.setTitle(title)
        		.setView(customView)
        		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                String inputed = editText.getText().toString().trim();
		                stdInputLisenter.onPositiveClicked(inputed);
		            }
		        })
		        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		            	stdInputLisenter.onNegativeClicked();
		            }
		        })
		        .setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						stdInputLisenter.onNegativeClicked();
					}
				})
				.create();
        
    	editText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			public void afterTextChanged(Editable s) {
				if (null != s) {
					View positiveButton = dialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE);
                	positiveButton.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
                }
				
				if (null != viewBinded) {
					viewBinded.setText(s);
				}
			}
		});
    	
    	dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				if (null != viewBinded) {
					editText.setText(viewBinded.getText());
					return;
				}
				if (editText.length() == 0) {
					editText.setText(defaultInput);
				}
			}
		});

        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return dialog;
    }
}
