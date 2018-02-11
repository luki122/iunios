/**
 * 
 */
package com.privacymanage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.privacymanage.service.WatchDogService;

import aurora.widget.AuroraButton;



/**
 * @author vulcan
 *
 */
public class PageWelcome extends FounderPage {
	


	/**
	 * 
	 */
	public PageWelcome() {
		super();
		mLayoutResId = ResIdMan.LAYOUT_FILE_WELCOME;
		mStringActionBarTitle = RESOURCE_ID_INVALID;
		mStringNextStepResId = RESOURCE_ID_INVALID;
		mPageId = PAGE_ID_WELCOME;
		//mShoudShowActionBar = false;
		mActionBarIsEmpty = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this,WatchDogService.class));
		
		//setup page manager
		//we have to put the page in the page manager by showing order
		//1.welcome page
		//2.create account page
		//3.password again page
		//4.password protection
		//5.done
		getPageMan().clear();
		getPageMan().put(this);
		getPageMan().put(new PageCreateAccount());
		getPageMan().put(new PagePasswordAgain());
		getPageMan().put(new PagePasswordProtection());
		getPageMan().put(new PageCreateDone());
		
		AuroraButton btnToCreateAccount = (AuroraButton)findViewById(ResIdMan.BUTTON_TO_CREATE_ACCOUNT);
		btnToCreateAccount.setOnClickListener(this);

		final TextView tvCantEnter = (TextView)findViewById(ResIdMan.TEXTVIEW_CANT_ENTER_PRIVACY);
//		tvCantEnter.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvCantEnter.setClickable(true);
		tvCantEnter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startPageGetbackPassword();
			}
			
		});
		
	}

	/**
	 * 
	 * Vulcan created this method in 2014年10月10日 下午6:21:34 .
	 */
	private void startPageGetbackPassword() {
		Intent intentGetbackPassword = new Intent(this, GetbackPassword.class);
		startActivity(intentGetbackPassword);
		return;
	}

}
