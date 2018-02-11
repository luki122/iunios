/**
 * Vulcan created this file in 2014年10月13日 上午10:50:00 .
 */
package com.privacymanage.activity;

import com.privacymanage.utils.LogUtils;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;



/**
 * Vulcan created ChangePassword in 2014年10月13日 .
 * 
 */
public class ChangePassword extends PageCreateAccount {

	/**
	 * 
	 */
	public ChangePassword() {
		super();
		mStringActionBarTitle = ResIdMan.STRING_CHANGE_PASSWORD;
		//mShoudShowActionBar = true;
		//mActionBarIsEmpty = false;
	}
	

	public boolean isPrivateActivity() {
		return true;
	}


	
	@Override
	public void onClick(View v) {
		
		final ImageView ivPasswordFrame = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PASSWORD_FRAME);
		EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		String password = aetPassword.getText().toString();
		if(!passwordLengthIsGood(password)) {
			setErrorInfoText(ResIdMan.STRING_PASSWORD_LENGTH_NOT_GOOD);
			ivPasswordFrame.setSelected(true);
			return;
		}
		
		String finalPassword = new String(AccountCreateInfo.mFirstBit + password);
		if(UnderlyingWrapper.passwordExists(finalPassword)) {
			setErrorInfoText(ResIdMan.STRING_PASSWORD_NO_CHANGE);
			ivPasswordFrame.setSelected(true);
			return;
		}

		ivPasswordFrame.setSelected(false);
		setErrorInfoText(ResIdMan.STRING_NULL);
		AccountCreateInfo.mPassword = password;
		startActivityChangePassword2();
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 上午11:10:22 .
	 */
	private void startActivityChangePassword2() {
		Intent intentChangePassword2 = new Intent(this, ChangePassword2.class);
		startActivity(intentChangePassword2);
		return;
	}


	@Override
	protected int loadFirstBitOfPassword() {
		
		boolean isInPrivacySpace = UnderlyingWrapper.isInPrivacySpace();
		LogUtils.printWithLogCat("vprivacy","loadFirstBitOfPassword: isInPrivacySpace = " + isInPrivacySpace);
		if(!isInPrivacySpace) {
			LogUtils.printWithLogCat("vprivacy","loadFirstBitOfPassword: stack = " + LogUtils.StackToString(new Throwable()));
		}
		
		int firstBit = UnderlyingWrapper.getFirstBitOfCurPassword();
		return firstBit;
	}
}
