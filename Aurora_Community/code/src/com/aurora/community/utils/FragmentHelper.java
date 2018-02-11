package com.aurora.community.utils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class FragmentHelper {

	private static final String TAG = "FragmentHelper";

    public interface IFragmentController{
    	public void initFragment();
    }

    public  Fragment mCurrentPage;
  
    public static FragmentHelper mFragmentHelper;
    
    public static FragmentHelper getInstantce(){
    	if(mFragmentHelper==null){
    		mFragmentHelper = new FragmentHelper();
    	}
    		return mFragmentHelper;
    }
  
    public void setCurrentContent(Fragment current){
    	mCurrentPage  = current;
    	Log.e("linp", "setCurrentContent id="+mCurrentPage.getId()+";"+"tag="+mCurrentPage.getTag());
    }
    
    public Fragment getCurrentContent(){
    	return this.mCurrentPage;
    }
    
	public  void switchContent(Fragment to,FragmentManager fm) {
		Log.e("linp", "~~~~~~~~~~~~~~~switchContent");
		if(mCurrentPage!=null)
			Log.e("linp", "switchContent id="+mCurrentPage.getId()+";"+"tag="+mCurrentPage.getTag());
		if (mCurrentPage == to) {
			return;
		}
		
	   FragmentTransaction t = fm.beginTransaction();
		t.hide(mCurrentPage);
		setCurrentContent(to);
		t.show(to);
     	t.commit();
	}
}
