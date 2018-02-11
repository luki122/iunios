package com.android.calculator2;

import java.util.List;
import android.text.Layout;
import android.content.Context;
import android.os.Build.VERSION;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;

// Aurora liugj 2014-01-21 created for aurora's new feature
public class AuroraTextToolMenu extends AuroraToolMenu{
	int x;
	int y;
	AuroraMenuView menu;
	CalculatorEditText calculatorEditText;
	Context context;
	AuroraMenuView2 auroraMenuView2;

	public AuroraTextToolMenu(View viewToAttach) {
		super(viewToAttach);
		context=viewToAttach.getContext();
	}

	@Override
	protected void showMenu(PopupWindow window) {
		// 在text右上方显示菜单
      
        calculatorEditText=(CalculatorEditText) mViewToAttach;
        TextPaint mTextPaint=calculatorEditText.getPaint();

        CharSequence text=calculatorEditText.getText().toString();
           
        TextPaint paint = calculatorEditText.getPaint();

        int width =(int) Layout.getDesiredWidth(text, 0, text.length(), paint);
    
         Log.e("xxj", "calculatorEditText______________________"+(width/2-dip2px( context,51))+"__________"+ width+"___________"+px2dip( context,width));
        int length =text.length();

        if(px2dip( context,width)>=336){
        	
        	if(length >=12){
              window.showAtLocation(mViewToAttach, Gravity.TOP | Gravity.CENTER, 0,calculatorEditText.getHeight()-dip2px( context,76));        	
              }
          if(length ==11){
              window.showAtLocation(mViewToAttach, Gravity.TOP | Gravity.CENTER, 0, calculatorEditText.getHeight()-dip2px( context,79));        	
              }
          if(length ==10){
              window.showAtLocation(mViewToAttach, Gravity.TOP | Gravity.CENTER, 0,calculatorEditText.getHeight()-dip2px( context,84));        	
          }  
          if(length < 10){
              window.showAtLocation(mViewToAttach, Gravity.TOP | Gravity.CENTER, 0,calculatorEditText.getHeight()-dip2px( context,85));        	
          }   	
        	
        }else{
        	
        //	 window.showAtLocation(mViewToAttach, Gravity.TOP | Gravity.CENTER, width/2-dip2px( context,51),calculatorEditText.getHeight()-dip2px( context,85));
        	 window.showAtLocation(mViewToAttach, Gravity.TOP | Gravity.CENTER,(calculatorEditText.getWidth()- width)/2-dip2px( context,12),calculatorEditText.getHeight()-dip2px( context,85));
        	
        }
             
	}

	@Override
	protected void ensureMenuLoaded(Context context,View menuView, List<AuroraMenuItem> items) {
	
        menu.layoutMenu(items);
   
	}

	@Override
	protected View getMenuView(Context context) {
		// TODO Auto-generated method stub
		 menu = new AuroraMenuView(context);
		
		
		 
		auroraMenuView2=new AuroraMenuView2(context,menu);
		auroraMenuView2.setOrientation(LinearLayout.VERTICAL);
		auroraMenuView2.setGravity(Gravity.CENTER);
		
		
		auroraMenuView2.add();
		return auroraMenuView2;
	}
	public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

}
