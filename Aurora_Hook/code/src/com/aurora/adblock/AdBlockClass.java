package com.aurora.adblock;

import com.adblock.data.AidlAdData;
import com.aurora.apihook.adblock.AuroraAdBlockInterface;

import android.content.Context;
import android.view.View;

public class AdBlockClass implements AuroraAdBlockInterface{
	private final String TAG = AdBlockClass.class.getName();
	
	public AdBlockClass(){
		LogUtils.printWithLogCat(TAG,"init");
	}

	@Override
	public boolean auroraHideAdView(Context arg0, View arg1) {
		AidlAdData curAidlAdData = AdBlockMain.getInstance(arg0).getAidlAdData();
		if(curAidlAdData == null){
			return false;
		}
		String className = arg1.getClass().getName();		
		int size = curAidlAdData.getAdClassLis()==null?0:curAidlAdData.getAdClassLis().size();
		for(int i=0;i<size;i++){
			if(className.contains(curAidlAdData.getAdClassLis().get(i))){
				arg1.setVisibility(View.GONE);
				break;
			}
		}	
		return false;
	}
}
