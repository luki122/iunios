/**
 * Vulcan created this file in 2015年1月24日 下午2:54:06 .
 */
package com.android.phase1.view;

import com.android.browser.BrowserSettings;
import com.android.phase1.activity.SimpleActivity;
import com.android.phase1.model.AuroraBrowserSettings;

import android.content.Context;
import android.util.AttributeSet;
import aurora.preference.AuroraDialogPreference;

/**
 * Vulcan created RestoreFactoryDialogPref in 2015年1月24日 .
 * 
 */
public class RestoreFactoryDialogPref extends AuroraDialogPreference {

	/**
	 * @param context
	 * @param attrs
	 */
	public RestoreFactoryDialogPref(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public RestoreFactoryDialogPref(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);

	}

	/* (non-Javadoc)
	 * @see aurora.preference.AuroraDialogPreference#onDialogClosed(boolean)
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			setEnabled(false);
			//BrowserSettings settings = BrowserSettings.getInstance();
			//settings.resetDefaultPreferences();
			SimpleActivity.restorePreferences();
			AuroraBrowserSettings.getInstance(getContext().getApplicationContext()).restorePreferences();
			BrowserSettings.executeSetup();
			setEnabled(true);
		}
		super.onDialogClosed(positiveResult);
	}

}
