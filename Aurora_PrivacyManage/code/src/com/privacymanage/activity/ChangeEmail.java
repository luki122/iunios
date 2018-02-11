/**
 * Vulcan created this file in 2014年10月11日 下午5:38:59 .
 */
package com.privacymanage.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import aurora.widget.AuroraEditText;

import com.privacymanage.utils.StringUtils;

/**
 * Vulcan created ChangeEmail in 2014年10月11日 .
 * 
 */
public class ChangeEmail extends PagePasswordProtection{

	/**
	 * 
	 */
	public ChangeEmail() {
		super();
	}
	
	public boolean isPrivateActivity() {
		return true;
	}
	

	@Override
	public void onClick(View v) {
		AuroraEditText aetEmail = (AuroraEditText)findViewById(ResIdMan.EDITTEXT_EMAIL_ADDR);
		String email = aetEmail.getText().toString();
		if(StringUtils.emailIsInvalid(email)) {
			setErrorInfoText(ResIdMan.STRING_EMAIL_ADDRESS_NOT_COMPLETE);
			aetEmail.setSelected(true);
			return;
		}

		aetEmail.setSelected(false);
		setErrorInfoText(ResIdMan.STRING_NULL);
		AccountCreateInfo.mEmailAddr = email;
		dbChangeEmail(email);
		finish();
		Toast toast = Toast.makeText(this, ResIdMan.STRING_INFO_CHANGE_EMAIL_SUCCESS, Toast.LENGTH_LONG);
		toast.show();
		return;
	}

	@Override
	public void onAuroraActionBarBackItemClicked(int arg0) {
		finish();
		return;
	}

	
	/**
	 * update email address for current account
	 * Vulcan created this method in 2014年10月13日 上午10:05:00 .
	 * @param email
	 */
	protected void dbChangeEmail(String email) {
		UnderlyingWrapper.thisAccountChangeEmail(email);
		return;
	}

	/* (non-Javadoc)
	 * @see com.privacymanage.activity.PagePasswordProtection#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AuroraEditText aetEmail = (AuroraEditText)findViewById(ResIdMan.EDITTEXT_EMAIL_ADDR);
		aetEmail.setText(UnderlyingWrapper.getEmailOfCurAccount());
		aetEmail.setSelection(aetEmail.getText().toString().length());
	}
}
