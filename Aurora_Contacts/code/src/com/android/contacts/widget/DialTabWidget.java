package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabWidget;

public class DialTabWidget extends TabWidget{

	public DialTabWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public DialTabWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public DialTabWidget(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCurrentTab(int index) {
		// TODO Auto-generated method stub
		super.setCurrentTab(index);
	}

	@Override
	public void focusCurrentTab(int index) {
		// TODO Auto-generated method stub
/*		if(this.getChildCount()>1)
		{			
		if(index==0)
			index=1;
		else if(index==1)
			index=0;
		}*/
		super.focusCurrentTab(index);
	}	
	
	
}
