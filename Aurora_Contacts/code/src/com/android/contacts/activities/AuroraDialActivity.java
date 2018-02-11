//<!-- aurora add zhouxiaobing 20130910 --> for dial launcher
package com.android.contacts.activities;
import android.os.Bundle;

public class AuroraDialActivity extends AuroraDialActivityV3 {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		SystemUtils.setStatusBarBackgroundTransparent(this);
	}
	
	@Override
		  public void onBackPressed() {		
		   		moveTaskToBack(true);		   	 
		  }
}
