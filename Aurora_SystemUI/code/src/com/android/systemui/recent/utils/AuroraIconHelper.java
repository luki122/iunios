package com.android.systemui.recent.utils;

import android.content.Context;
import com.android.systemui.R;

//import android.util.Log;

public class AuroraIconHelper {
	//private static String TAG = "AuroraIconHelper";
//	private static final int INV_RESID = 0;
	private static final String PRE_PACKAGE_NAME = "com.aurora.launcher.res";
	private static AuroraIconHelper mAuroraIconHelper = null;
	private static String sTskAffinity = "android.task.contacts.phone";
	private AuroraIconHelper(){

	}
	
	public static AuroraIconHelper getInstance(){
		if(null == mAuroraIconHelper){
			mAuroraIconHelper = new AuroraIconHelper();
		}
		return mAuroraIconHelper;
	}

	private  final DrawableData mNameMap[] = new DrawableData[]{
	};
	public int getDrawableId(String pkgName,String taskAffinity){
		if("com.android.contacts".equals(pkgName) && sTskAffinity.equals(taskAffinity)){
			return R.drawable.phone;
		}
		
		int size = mNameMap.length;
		for(int i = 0;i < size;++i){
				String []pkgs = mNameMap[i].mPkgs.split("#");
				for(int j = 0;j < pkgs.length;++j){
					if(pkgs[j].equals(pkgName)){
						return mNameMap[i].mIconId;
					}
				}
		}
		
		return 0;
		
	}
	
	private final class DrawableData{
		DrawableData(String pkgs,int iconId){
			mPkgs = pkgs;
			mIconId = iconId;
		}
		String mPkgs;
		int mIconId;
	}
}
