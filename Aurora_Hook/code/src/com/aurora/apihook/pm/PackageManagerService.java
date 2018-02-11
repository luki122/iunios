package com.aurora.apihook.pm;

import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class PackageManagerService implements Hook{

	public void before_compareSignatures(MethodHookParam param){
		param.setResult(0);
	}
	
}
