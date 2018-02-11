package com.android.deskclock;


import android.R.bool;
import android.app.Activity;
import android.content.Context;
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

public class RepeatPopupWindow {
	private PopupWindow popWindow;
	private Context mContext;
	private View mView;
	private LinearLayout linear;
	private int selectDays;
	/**  监听器*/
	private OnButtonClickListener mOnButtonClickListener;
	
	public interface OnRepeatPopupWindowDismissListener{
	    public boolean dismiss();
	}
	
    public interface OnButtonClickListener
    {
        void onSureClick( );
        void onCancelClick( );
    }
	
	private OnRepeatPopupWindowDismissListener dismissListener;
	public void setOnRepeatPopupWindowDismissListener(OnRepeatPopupWindowDismissListener dismissListener){
	    this.dismissListener = dismissListener;
	}
	
    /**
    * 
    * @param listener
    */
	public void setOnButtonClickListener(OnButtonClickListener listener)
	{
		this.mOnButtonClickListener = listener;
	}
	
	public RepeatPopupWindow( Context context, int resId ) {
		
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
		CheckBox check = (CheckBox)linear.findViewById(R.id.dayone);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(0, isChecked);
			}
		});
		check = (CheckBox)linear.findViewById(R.id.daytwo);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(1, isChecked);
			}
		});
		check = (CheckBox)linear.findViewById(R.id.daythree);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(2, isChecked);
			}
		});
		check = (CheckBox)linear.findViewById(R.id.dayfour);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(3, isChecked);
			}
		});
		check = (CheckBox)linear.findViewById(R.id.dayfive);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(4, isChecked);
			}
		});
		check = (CheckBox)linear.findViewById(R.id.daysix);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(5, isChecked);
			}
		});
		check = (CheckBox)linear.findViewById(R.id.dayseven);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setSelectDays(6, isChecked);
			}
		});
		Button button = (Button)linear.findViewById(R.id.sure);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonClickListener != null)
				{
					mOnButtonClickListener.onSureClick();
				}
				popWindow.dismiss();
			}
		});
		button = (Button)linear.findViewById(R.id.quxiao);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mOnButtonClickListener != null)
				{
					mOnButtonClickListener.onCancelClick();
				}
				popWindow.dismiss();
			}
		});
	}
	
    private void setSelectDays(int day, boolean set) {
        if (set) {
        	selectDays |= (1 << day);
        } else {
        	selectDays &= ~(1 << day);
        }
    }
    
    public int getSelectDays( ) {
    	return selectDays;
    }
	
	public void showRepeatPopupWindow( ) {
		if ( popWindow != null && !popWindow.isShowing() ) {
			popWindow.showAtLocation(mView, Gravity.BOTTOM, 0, 0);
		}
	}
	
	public void dismissRepeatPopupWindow( ) {
		if ( popWindow != null && popWindow.isShowing() ) {
			popWindow.dismiss();
		}
	}
}
