package com.android.deskclock;


import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;

import com.android.deskclock.R;

public class SettingPopupWindow {
	private PopupWindow popWindow;
	private Context mContext;
	private View mView;
	private LinearLayout linear;
	/**  监听器*/
	private OnButtonSettingClickListener mOnButtonSettingClickListener;
	
	public interface OnSettingPopupWindowDismissListener{
	    public boolean dismiss();
	}
	
    public interface OnButtonSettingClickListener
    {
        void onButtonSettingClick( int position );
    }
	
	private OnSettingPopupWindowDismissListener dismissListener;
	public void setOnSettingPopupWindowDismissListener(OnSettingPopupWindowDismissListener dismissListener){
	    this.dismissListener = dismissListener;
	}
	
    /**
    * 
    * @param listener
    */
	public void setOnButtonSettingClickListener(OnButtonSettingClickListener listener)
	{
		this.mOnButtonSettingClickListener = listener;
	}
	
	public SettingPopupWindow( Context context, int resId ) {
		
		mView = ((Activity) context).getWindow().getDecorView();
		mContext = context;
		linear = (LinearLayout) LayoutInflater.from(context).inflate(resId, null);
		linear.setFocusableInTouchMode(true);//能够获得焦点  
		linear.setOnKeyListener(new OnKeyListener() {  
            @Override  
            public boolean onKey(View v, int keyCode, KeyEvent event) {  
                if (event.getAction() == KeyEvent.ACTION_UP) {  
                    switch(keyCode) {  
                    case KeyEvent.KEYCODE_BACK: 
                    	dismissSettingListPopupWindow( );
                        break;  
                    case KeyEvent.KEYCODE_MENU:
                    	dismissSettingListPopupWindow( );
                        break;  
                    }  
                }  
                return true;  
            } 
        });  
		popWindow = new PopupWindow(linear, WindowManager.LayoutParams.FILL_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		popWindow.setOutsideTouchable(true);
		popWindow.setBackgroundDrawable(new BitmapDrawable());
		popWindow.setFocusable(true);
		popWindow.setAnimationStyle(R.style.RepeatPopupWindowMenu);
		popWindow.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				if (dismissListener != null) {
					dismissListener.dismiss();
				}
			}
		});
		initChildView( );
	}
	
	private void initChildView ( ) {
		
		Button button = (Button)linear.findViewById(R.id.setting);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonSettingClickListener != null)
				{
					mOnButtonSettingClickListener.onButtonSettingClick(0);
				}
				popWindow.dismiss();
			}
		});
	}
	
	public void showSettingListPopupWindow( ) {
		if ( popWindow != null && !popWindow.isShowing() ) {
			popWindow.showAtLocation(mView, Gravity.BOTTOM, 0, 0);
		}
	}
	
	public void dismissSettingListPopupWindow( ) {
		if ( popWindow != null && popWindow.isShowing() ) {
			popWindow.dismiss();
		}
	}
	
	public boolean isSettingListPopupWindowShow( ) {
		return popWindow.isShowing();
	}
}
