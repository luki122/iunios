/**
 * Vulcan created this file in 2014年10月13日 上午11:09:40 .
 */
package com.privacymanage.activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Vulcan created ChangePassword2 in 2014年10月13日 .
 * 
 */
public class ChangePassword2 extends PagePasswordAgain {

	/**
	 * 
	 */
	public ChangePassword2() {
		mStringNextStepResId = ResIdMan.STRING_CONFIRM;
		//mShoudShowActionBar = true;
		//mActionBarIsEmpty = false;
	}
	
	public boolean isPrivateActivity() {
		return true;
	}
	

	/**
	 * 
	 * Vulcan created this method in 2014年10月17日 下午6:02:28 .
	 */
	protected void startPrivacySpace() {
		Intent intentPrivacyManageActivity = new Intent(this, SetActivity.class);
		intentPrivacyManageActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intentPrivacyManageActivity);
		return;
	}

	@Override
	public void onClick(View v) {
		final ImageView ivPasswordFrame = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PASSWORD_FRAME);
		EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		String password = aetPassword.getText().toString();
		
		if(!password.equals(AccountCreateInfo.mPassword)) {
			setErrorInfoText(ResIdMan.STRING_PASSWORD_DIFFERENT);
			ivPasswordFrame.setSelected(true);
			return;
		}
		
		
		
		String finalPassword = new String(AccountCreateInfo.mFirstBit + password);
		UnderlyingWrapper.thisAccountChangePassword(finalPassword);
		
		ivPasswordFrame.setSelected(false);
		setErrorInfoText(ResIdMan.STRING_NULL);
		AccountCreateInfo.mPassword2 = password;
		
		
		Log.d("vpass", "onClick: password = " + password);
		
		UnderlyingWrapper.thisAccountChangePassword(finalPassword);
		
		startPrivacySpace();
		finish();
		
		Toast toast = Toast.makeText(this, ResIdMan.STRING_INFO_CHANGE_PASSWORD_SUCCESS, Toast.LENGTH_LONG);
		toast.show();
	}
	
}
