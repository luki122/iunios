/**
 * Vulcan created this file in 2014年10月16日 上午9:58:54 .
 */
package com.privacymanage.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Vulcan created PrivacyEntrance in 2014年10月16日 .
 * 
 */
public class PrivacyEntrance extends FounderPage {

	/**
	 * 
	 */
	public PrivacyEntrance() {
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(UnderlyingWrapper.isInPrivacySpace()) {
			Intent intentUserGuide = new Intent(this, PrivacyManageActivity.class);
			startActivity(intentUserGuide);
		}
		else {
			Intent intentUserGuide = new Intent(this, PageWelcome.class);
			startActivity(intentUserGuide);
		}
		finish();
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月16日 上午11:06:51 .
	 * @param context
	 */
	public static void start(Context context) {
		Intent intentPrivacyEntrance = new Intent(context, PrivacyEntrance.class);
		intentPrivacyEntrance.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intentPrivacyEntrance);
	}

}
