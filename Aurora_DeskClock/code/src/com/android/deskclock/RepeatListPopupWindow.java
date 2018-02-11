package com.android.deskclock;


import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

public class RepeatListPopupWindow {
	private PopupWindow popWindow;
	private Context mContext;
	private View mView;
	private LinearLayout linear;
	/**  监听器*/
	private OnButtonListClickListener mOnButtonListClickListener;
	
	public interface OnRepeatListPopupWindowDismissListener{
	    public boolean dismiss();
	}
	
    public interface OnButtonListClickListener
    {
        void onButtonListClick( int position );
    }
	
	private OnRepeatListPopupWindowDismissListener dismissListener;
	public void setOnRepeatListPopupWindowDismissListener(OnRepeatListPopupWindowDismissListener dismissListener){
	    this.dismissListener = dismissListener;
	}
	
    /**
    * 
    * @param listener
    */
	public void setOnButtonListClickListener(OnButtonListClickListener listener)
	{
		this.mOnButtonListClickListener = listener;
	}
	
	public RepeatListPopupWindow( Context context, int resId ) {
		
		mView = ((Activity) context).getWindow().getDecorView();
		mContext = context;
		linear = (LinearLayout) LayoutInflater.from(context).inflate(resId, null);
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
		
		Button button = (Button)linear.findViewById(R.id.onlyonce);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonListClickListener != null)
				{
					mOnButtonListClickListener.onButtonListClick(0);
				}
				popWindow.dismiss();
			}
		});
		button = (Button)linear.findViewById(R.id.everyday);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonListClickListener != null)
				{
					mOnButtonListClickListener.onButtonListClick(1);
				}
				popWindow.dismiss();
			}
		});
		button = (Button)linear.findViewById(R.id.weekday);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonListClickListener != null)
				{
					mOnButtonListClickListener.onButtonListClick(2);
				}
				popWindow.dismiss();
			}
		});
		button = (Button)linear.findViewById(R.id.officialweekday);
	Resources resources=	mContext.getResources();
	String isShowOfficialWeekday=resources.getString(R.string.isshowofficeday);
	if(isShowOfficialWeekday.equals("no")){
		button.setVisibility(View.GONE);				
	}
	
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonListClickListener != null)
				{
					mOnButtonListClickListener.onButtonListClick(3);
				}
				popWindow.dismiss();
			}
		});
		button = (Button)linear.findViewById(R.id.selfdefine);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonListClickListener != null)
				{
					mOnButtonListClickListener.onButtonListClick(4);
				}
				popWindow.dismiss();
			}
		});
	}
	
	public void showRepeatListPopupWindow( ) {
		if ( popWindow != null && !popWindow.isShowing() ) {
			popWindow.showAtLocation(mView, Gravity.BOTTOM, 0, 0);
		}
	}
	
	public void dismissRepeatListPopupWindow( ) {
		if ( popWindow != null && popWindow.isShowing() ) {
			popWindow.dismiss();
		}
	}
}
