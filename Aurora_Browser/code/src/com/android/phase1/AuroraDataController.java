/**
 * Vulcan created this file in 2015年4月1日 下午2:32:19 .
 */
package com.android.phase1;

import android.content.Context;

import com.android.browser.DataController;
import com.android.browser.Tab;

/**
 * Vulcan created AuroraDataController in 2015年4月1日 .
 * 
 */
public class AuroraDataController extends DataController {

	/**
	 * @param c
	 */
	public AuroraDataController() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.android.browser.DataController#initInstance(android.content.Context)
	 */
	@Override
	protected void initInstance(Context c) {
		// TODO Auto-generated method stub
		super.initInstance(c);
	}

	/* (non-Javadoc)
	 * @see com.android.browser.DataController#getDefaultDataControllerHandler()
	 */
	@Override
	protected DataControllerHandler getDefaultDataControllerHandler() {
		// TODO Auto-generated method stub
		return super.getDefaultDataControllerHandler();
	}

	/* (non-Javadoc)
	 * @see com.android.browser.DataController#saveThumbnail(com.android.browser.Tab)
	 */
	@Override
	public void saveThumbnail(Tab tab) {
		mDataHandler.sendMessage(TAB_SAVE_THUMBNAIL_BY_FILE, tab);
	}
	


}
