package com.aurora.stopwatch;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SimpleAdapter;

import com.android.deskclock.R;

public class MarkListAdapter extends SimpleAdapter {

	 private LayoutInflater mInflater;  
	 private Context mContext;
	 Handler h = new Handler();
	 private boolean isAnim = false;
	 private Handler mHandler;
	 
	 void setAnim(boolean value) {
		 isAnim = value;		 
	 }
	 
	public MarkListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
		  mInflater = (LayoutInflater) context  
				    .getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		  mContext = context;
		  mHandler = new Handler();
	}
	
	public View getView (int position, View convertView, ViewGroup parent) {
		   final View view = super.getView(position, convertView, parent);
		   if(isAnim) {
			   Animation itemAnimation;
			   if(position == 0) {
				  itemAnimation = AnimationUtils.loadAnimation(mContext, R.anim.first_mark_list_item_show);
				  itemAnimation.setAnimationListener(new Animation.AnimationListener() {				
						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							view.setVisibility(View.INVISIBLE);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub							
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							view.setVisibility(View.VISIBLE);										
						}
					});
			   } else{
				  itemAnimation = AnimationUtils.loadAnimation(mContext, R.anim.mark_list_item_show);
			   }
			   view.startAnimation(itemAnimation);
			   mHandler.post(new Runnable(){
				   public void run(){
					   isAnim = false;
				   }
			   });
		   }
		   return view;  		
	}
	
}