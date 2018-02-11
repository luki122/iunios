/**
 * Vulcan created this file in 2014年9月29日 下午5:30:03 .
 */
package com.privacymanage.activity;


import com.privacymanage.utils.StringUtils;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraTextView;

/**
 * @author vulcan
 *
 */
public class PagePasswordProtection extends FounderPage {

	/**
	 * 
	 */
	public PagePasswordProtection() {
		super();
		mLayoutResId = ResIdMan.LAYOUT_FILE_PASSWORD_PROTECTION;
		mStringActionBarTitle = ResIdMan.STRING_PASSWORD_PROTECTION;
		mStringNextStepResId = ResIdMan.STRING_CONFIRM;
		mPageId = PAGE_ID_PASSWORD_PROTECTION;
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
		AuroraEditText aetEmail = (AuroraEditText)findViewById(ResIdMan.EDITTEXT_EMAIL_ADDR);
		String email = aetEmail.getText().toString();
		if(StringUtils.emailIsInvalid(email)) {
			setErrorInfoText(ResIdMan.STRING_EMAIL_ADDRESS_NOT_GOOD);
			aetEmail.setSelected(true);
			return;
		}
		
		

		setErrorInfoText(ResIdMan.STRING_NULL);
		AccountCreateInfo.mEmailAddr = email;
		
		String finalPassword = new String(AccountCreateInfo.mFirstBit + AccountCreateInfo.mPassword);
		AccountCreateInfo.mAccountRef = UnderlyingWrapper.createPrivacyAccount(finalPassword, AccountCreateInfo.mEmailAddr);
		super.onClick(v);
	}


	/* (non-Javadoc)
	 * @see com.privacymanage.activity.FounderPage#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final AuroraEditText aetEmail = (AuroraEditText)findViewById(ResIdMan.EDITTEXT_EMAIL_ADDR);
		aetEmail.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				setErrorInfoText(ResIdMan.STRING_NULL);
				aetEmail.setSelected(false);
			}
			
		});
		
	}
	
	
}
