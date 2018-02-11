/**
 * Vulcan created this file in 2014年9月29日 下午5:16:39 .
 */
package com.privacymanage.activity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraTextView;

/**
 * @author vulcan
 *
 */
public class PagePasswordAgain extends FounderPage {
	public PagePasswordAgain() {
		super();
		mLayoutResId = ResIdMan.LAYOUT_FILE_INPUT_PASSWORD;
		mStringActionBarTitle = ResIdMan.STRING_PASSWORD_AGAIN;
		mStringNextStepResId = ResIdMan.STRING_NEXT_STEP;
		mPageId = PAGE_ID_PASSWORD_AGAIN;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int firstBit = AccountCreateInfo.mFirstBit;
		TextView tvMostLeftBit = (TextView)findViewById(ResIdMan.TEXTVIEW_MOST_LEFT_BIT);
		tvMostLeftBit.setText(Integer.toString(firstBit));
		AccountCreateInfo.mFirstBit = firstBit;
		
		final ImageView ivPasswordFrame = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PASSWORD_FRAME);
		final EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		aetPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				setErrorInfoText(ResIdMan.STRING_NULL);
				ivPasswordFrame.setSelected(false);
			}
			
		});
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月8日 下午4:29:36 .
	 * @param resid
	 */
	protected void setErrorInfoText(int resid) {
		AuroraTextView tvErrorMsg = (AuroraTextView)findViewById(ResIdMan.TEXTVIEW_ERROR_MSG);
		tvErrorMsg.setText(resid);
	}
	
	@Override
	public void onClick(View v) {
		EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		String password = aetPassword.getText().toString();
		
		if(!password.equals(AccountCreateInfo.mPassword)) {
			setErrorInfoText(ResIdMan.STRING_PASSWORD_DIFFERENT);
			final ImageView ivPasswordFrame = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PASSWORD_FRAME);
			ivPasswordFrame.setSelected(true);
			return;
		}
		
		setErrorInfoText(ResIdMan.STRING_NULL);
		AccountCreateInfo.mPassword2= password;
		super.onClick(v);
	}
}
