package com.android.browser;



import com.android.browser.TabControl.NumberChangeListener;
import com.android.browser.UI.ComboViews;
import com.android.phase1.activity.SettingPage;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import aurora.widget.AuroraTextView;

public class ToolBarLinearLayout extends LinearLayout implements OnClickListener{
	
	   private ImageView forward;
	   private ImageView backward;
	   private ImageView bookmark;
	   private ImageView switcher;
	   private ImageView setting;
	   private AuroraTextView numberTextView;
	   protected BaseUi mBaseUi;
	   protected UiController mUiController;
	
	   public ToolBarLinearLayout(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		   public ToolBarLinearLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}
		public ToolBarLinearLayout(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			// TODO Auto-generated constructor stub
		}
		
		
		 void setToolBarLinearLayout(UiController controller, BaseUi ui){
			 mBaseUi=ui;
			 mUiController=controller;
			 controller.getTabControl().setNumberChangeListener(new NumberChangeListener() {
				
				@Override
				public void onNumberChange(int number) {
					
					
					try {
						Typeface tf = Typeface.createFromFile("system/fonts/Roboto-Bold.ttf");
						numberTextView.setTypeface(tf);
					} catch (Exception e) {
						Log.i("xie","Typeface:"+e);
					}
					numberTextView.setText(""+number);
					if(number >= 10) {
						numberTextView.setPadding(
								BaseUi.dip2px(mContext, 7), 
								numberTextView.getPaddingTop(), 
								numberTextView.getPaddingRight(), 
								numberTextView.getPaddingBottom());
					}else {
						numberTextView.setPadding(
								BaseUi.dip2px(mContext, 10), 
								numberTextView.getPaddingTop(), 
								numberTextView.getPaddingRight(), 
								numberTextView.getPaddingBottom());
					}
					
				}
			});
			
		}
	@Override
	public void onClick(View v ) {
		// TODO Auto-generated method stub
	    switch (v.getId()) {  
        case R.id.toolbar_forward: 
        	mUiController.getCurrentTab().goForward();
//        Boolean can=	mUiController.getCurrentTab().canGoForward();
//        Log.i("xie1", "_______onClick_____toolbar_forward____________"+can);
//        if(can){
//       // 	mUiController.getCurrentTab().goForward();
//        }
        	 
           
            break;
        case R.id.toolbar_backward: 
        	mUiController.getCurrentTab().goBack();
//        	 Boolean can2=mUiController.getCurrentTab().canGoForward();
//        	  Log.i("xie1", "_______onClick_____toolbar_backward____________"+can2);
//        	 if(can2){
//            // 	mUiController.getCurrentTab().goBack();
//             }
         
            break;  
        case R.id.toolbar_bookmark: 
        	mUiController.setDefaultBookmarkSaveFolder();
        	mUiController.bookmarksOrHistoryPicker(ComboViews.Bookmarks);
          
            break;  
        case R.id.toolbar_switcher:  
        	((PhoneUi) mBaseUi).toggleNavScreen();
          
            break;
        case R.id.toolbar_setting:  
        	
        	// SettingPage.start(getContext(), mUiController.getCurrentTopWebView().getUrl());
            if (mUiController.getCurrentTopWebView() != null) {
                SettingPage.start(getContext(), mUiController.getCurrentTopWebView().getUrl());
            }
            break;
        default:  
            break;  
        }  
		
	}
	@Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        forward = (ImageView) findViewById(R.id.toolbar_forward);
        backward = (ImageView) findViewById(R.id.toolbar_backward);
        bookmark = (ImageView) findViewById(R.id.toolbar_bookmark);
        switcher = (ImageView) findViewById(R.id.toolbar_switcher);
        setting = (ImageView) findViewById(R.id.toolbar_setting);
        numberTextView=(AuroraTextView)findViewById(R.id.toolbar_text);
        forward.setOnClickListener(this);
        backward.setOnClickListener(this);
        bookmark.setOnClickListener(this);
        switcher.setOnClickListener(this);
        setting.setOnClickListener(this);
        backward.setEnabled(false);
		forward.setEnabled(false);
      
       
    }
	public void changeStatueOfForwardBackword() {
		if (mUiController.getCurrentTab() != null) {
			if (mUiController.getCurrentTab().canGoBack()) {
				// 设置可使用状态
				backward.setEnabled(true);
			} else {
				// 设置禁止状态
				backward.setEnabled(false);
			}
			if (mUiController.getCurrentTab().canGoForward()) {
				// 设置可使用状态
				forward.setEnabled(true);
			} else {
				// 设置禁止状态
				forward.setEnabled(false);
			}
		}
	}
}
