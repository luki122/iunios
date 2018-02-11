package com.android.server.telecom;

import java.util.ArrayList;
import java.util.List;

import com.android.server.telecom.ManagePrivate;
import com.android.server.telecom.ManageReject;
import com.android.server.telecom.YuLoreUtils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import com.android.server.telecom.YuLoreUtils;

public class AuroraGlobals extends ContextWrapper{
	

	protected static AuroraGlobals sMe;
	
    public AuroraGlobals(Context context) {
        super(context);
        sMe = this;
        initAuroraObjects();
    }
    
    public static AuroraGlobals getInstance() {
        return sMe;
    }
    
    private void initAuroraObjects() {
	    if(AuroraPrivacyUtils.isSupportPrivate()) {
	    	mManagePrivate =  ManagePrivate.init(this);
	    }
   	    if(RejectUtils.isSupportBlack()) {
//	    	if(!SogouUtils.isInit()) {
//	    		SogouUtils.init(TelecomGlobals.getInstance());
//	    	}
	        mYuloreUtils = new YuLoreUtils();
	        mYuloreUtils.init(this);
	        mYuloreUtils.bindService();
   	    }   
	    mManageReject = new ManageReject(this);
    }
    
    ManagePrivate mManagePrivate;
    ManageReject mManageReject; 
    YuLoreUtils mYuloreUtils;   
    public static List<Activity> mPrivacyActivityList = new ArrayList<Activity>();
}