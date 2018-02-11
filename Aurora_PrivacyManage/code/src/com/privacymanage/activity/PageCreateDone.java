/**
 * Vulcan created this file in 2014年9月29日 下午5:24:29 .
 */
package com.privacymanage.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import aurora.widget.AuroraButton;

/**
 * @author vulcan
 *
 */
public class PageCreateDone extends FounderPage {

	/**
	 * 
	 */
	public PageCreateDone() {
		super();
		mLayoutResId = ResIdMan.LAYOUT_FILE_CREATE_DONE;
		mStringActionBarTitle = RESOURCE_ID_INVALID;
		mStringNextStepResId = RESOURCE_ID_INVALID;
		mPageId = PAGE_ID_CREATE_DONE;
		//mShoudShowActionBar = false;
		mActionBarIsEmpty = true;
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        //使用xml文件实现帧动画效果    
        ImageView ivDone = (ImageView)findViewById(ResIdMan.IMAGEVIEW_CREATE_DONE);
        //ivDone.setBackgroundResource(R.anim.frameanimation);
        AnimationDrawable animOK = (AnimationDrawable) ivDone.getBackground();
        //this.setBackgroundDrawable(mAnimationDrawable);
        
        animOK.setOneShot(true);
        animOK.start();
		
		AuroraButton abToUserGuide = (AuroraButton)findViewById(ResIdMan.BUTTON_TO_USER_GUIDE);
		abToUserGuide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startUserGuide();
			}
		});
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月9日 下午2:58:23 .
	 */
	private void startUserGuide() {
		Intent intentUserGuide = new Intent(this, UserGuide.class);
		Bundle bundle = new Bundle();   
		bundle.putInt(UserGuide.LAUNCH_MODE_KEY,UserGuide.LAUNCH_MODE_CREATE_DONE);
		intentUserGuide.putExtras(bundle); 	
		startActivity(intentUserGuide);
		return;
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		return;
	}

}
