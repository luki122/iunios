package com.aurora.calender;

import java.util.ArrayList;

import com.aurora.plugin.CalendarIcon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aurora.launcher.R;

public class CalenderAdapter extends BaseAdapter {
	LinearLayout linearLayout;
	LayoutInflater inflater;
	  Context context;
	  ArrayList<Drawable> list;
      public CalenderAdapter(Context context, ArrayList<Drawable> list) {
          super();
      	inflater=LayoutInflater.from(context);
          this.context = context;
          this.list=list;
      }
	
		
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		 View view = arg1;
   	  
			
	        if (arg1 == null) {
	        //	BubbleTextView favorite = (BubbleTextView) mInflater.inflate(
	        	//		R.layout.application, parent, false);
	       linearLayout=(LinearLayout)	inflater.inflate(R.layout.clander, null, false);
	      
	          view =(TextView) linearLayout.findViewById(R.id.imageView1);
	   
	        } 

	        //  ((ImageView) view).setImageDrawable(CalendarIcon.drawDayList.get(position));
	         ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(null,null, null,  list.get(arg0));
	        

	        return view;
	      
	}

}
