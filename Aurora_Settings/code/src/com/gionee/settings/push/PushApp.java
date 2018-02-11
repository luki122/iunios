package com.gionee.settings.push;

import android.net.Uri;

////////////////////////////////////////////////////////////////////////////////////////
//  DO NOT MODIFY ANYTHING IN THIS FILE,IT IS A COPY FOR PUSH APP TABLE IN            //
//  SYSTEM SETTING PACKAGE ,ONLY YOU MODIFY THE FILE IN FRAMEWORK,THEN YOU CAN MODIFY //
//  THIS FOR SYNC                                                                     //
////////////////////////////////////////////////////////////////////////////////////////

public final class PushApp{ 

	public static final String TABLE = "pushapp";
	private static final String AUTHOURITY = "com.gionee.settings.NotifyPushProvider";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" 
			+ AUTHOURITY + "/"+ TABLE);
	
	public static class Column{
		public static final String _ID = "_id";
		public static final String PACKAGE = "package";
		public static final String SWITCH = "switch";
		public static final String REGISTER = "register";
		public static final String DLG_SHOW_TIMES = "dlg_show_time";
		public static final String SEEN = "seen";
	}
}
