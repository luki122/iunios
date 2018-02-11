package com.android.calculator2;

import com.android.calculator2.R;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AuroraMenuView2 extends LinearLayout {
	AuroraMenuView menu;

	public AuroraMenuView2(Context context,AuroraMenuView menu) {
		super(context);
		this.menu=menu;
		// TODO Auto-generated constructor stub
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.
        		LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		setLayoutParams(param);

	}
	
	
	
	public void add(){
		LinearLayout.LayoutParams paramed = new LinearLayout.LayoutParams(ViewGroup.
        		LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		menu.setLayoutParams(paramed);
		addView(menu);
		ImageView imageView=new ImageView(getContext());
		

		imageView.setBackgroundResource(R.drawable.dasfas22);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.
        		LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
    
        /*添加m_ListView到m_LinearLayout布局*/
      addView(imageView,param);

		
	//	addView(imageView);
		
		
		
		
		
		
	}

}
