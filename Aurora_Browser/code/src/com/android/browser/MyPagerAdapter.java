package com.android.browser;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyPagerAdapter extends PagerAdapter {
	
	
	  Context context;
	  ArrayList<Tab> list;
      public MyPagerAdapter(Context context, ArrayList<Tab> list) {
         
          
          this.context = context;
          this.list=list;
         
      }
      
      @Override
  	public int getCount() {
  		// TODO Auto-generated method stub
  		return list.size();
  	}
      public void destroyItem(ViewGroup container, int position, Object object) {
    	   View view = list.get(position).getViewContainer();
//    	      WebView mainView  = list.get(position).getWebView();
//    	    
//    	      FrameLayout wrapper =
//    	              (FrameLayout) view.findViewById(R.id.webview_wrapper);
//    	      ViewGroup parent = (ViewGroup) mainView.getParent();
//    	      if (parent != wrapper) {
//    	          if (parent != null) {
//    	              parent.removeView(mainView);
//    	          }
//    	          wrapper.addView(mainView);
//    	      }
//    	      parent = (ViewGroup) view.getParent();
//    	      if (parent != mContentView) {
//    	          if (parent != null) {
//    	              parent.removeView(view);
//    	          }
//    	     
//    	      }
  		container.removeView(view);
  	}
  	@Override
  	public Object instantiateItem(ViewGroup container, int position) {
 	   View view = list.get(position).getViewContainer();
	      WebView mainView  = list.get(position).getWebView();
	    
	      FrameLayout wrapper =
	              (FrameLayout) view.findViewById(R.id.webview_wrapper);
	    
	      ViewGroup parent = (ViewGroup) mainView.getParent();
	      if (parent != wrapper) {
	          if (parent != null) {
	              parent.removeView(mainView);
	          }
	          wrapper.addView(mainView);
	      }
//	      parent = (ViewGroup) view.getParent();
//	      if (parent != mContentView) {
//	          if (parent != null) {
//	              parent.removeView(view);
//	          }
//	     
//	      }
  		container.addView(view);
  		return view;   
  	}

  	@Override
  	public boolean isViewFromObject(View arg0, Object arg1) {
  		return arg0==arg1; 
  	}

	

}
