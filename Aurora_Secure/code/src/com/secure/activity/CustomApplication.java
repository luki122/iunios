package com.secure.activity;

import android.app.Application;

public class CustomApplication extends Application{
    private static Application application;
	
    @Override
    public void onCreate(){
    	application = this;
        super.onCreate();
    }
    
    public static Application getApplication(){
    	return application;
    }    
}
