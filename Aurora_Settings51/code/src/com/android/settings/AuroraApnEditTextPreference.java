package com.android.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import aurora.preference.AuroraEditTextPreference;
import aurora.widget.AuroraEditText;

public class AuroraApnEditTextPreference extends AuroraEditTextPreference {
	private AuroraEditText mEditText;
	public static boolean mIsModify;
	private Context mContext;

	public AuroraApnEditTextPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		
       
	}

	public AuroraApnEditTextPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextPreferenceStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public AuroraApnEditTextPreference(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		mEditText = new AuroraEditText(mContext);
        
        // Give it an ID so it can be saved/restored
        mEditText.setId(android.R.id.edit);
		AuroraEditText editText = mEditText;
        editText.setText(getText());
        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
        
	}
	
	protected void onAddEditTextToDialogView(View dialogView, AuroraEditText editText) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(com.aurora.internal.R.id.aurora_edittext_container);
        if (container != null) {
        	container.removeAllViews();
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            String value = mEditText.getText().toString();
            
            if(!value.equals(getText())){
            
            	mIsModify = true;
            }
            
            if (callChangeListener(value)) {
                setText(value);
            }
            
            
        }
    }

	
	
	

}
