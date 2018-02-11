package com.aurora.voiceassistant.model;

import android.util.Log;

public class RkLog 
{
	static public void i(String tag,String msg)
	{
		if(!CFG.DEBUG || null == tag || null == msg) return;
		
		Log.i(tag, msg);
	}
	
	static public void e(String tag,String msg)
	{
		if(!CFG.DEBUG || null == tag || null == msg) return;
		
		Log.e(tag, msg);
	}
}
