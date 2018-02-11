/*aurora linchunhui disable begin
package com.android.settings.notification;

import com.android.settings.R;

import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class LetterToast {
	 private FrameLayout toastFatherLayout;
	 private LinearLayout toastLayout;
	 private TextView overlay;
	 private Handler handler = new Handler() ;
	 private OverlayThread overlayThread = new OverlayThread();
	 private int initMarginRight;
	 private LayoutParams layoutParams;
		
     public LetterToast(FrameLayout toastFatherLayout){
    	 this.toastFatherLayout = toastFatherLayout;
    	 toastLayout = (LinearLayout)toastFatherLayout.findViewById(R.id.toastLayout);
    	 overlay = (TextView)toastFatherLayout.findViewById(R.id.tvLetter);
 		 initMarginRight = toastFatherLayout.getWidth()-toastLayout.getRight();
 		 layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
 		 layoutParams.gravity = Gravity.RIGHT;
     }
     
 	private class OverlayThread implements Runnable {
		public void run() {
			if(toastLayout != null){
				toastLayout.setVisibility(View.GONE);
			}			
		}
	}
aurora linchunhui disable end*/
 	/**
 	 * 更换显示的内容
 	 * @param s 选中的字符
 	 * @param positionOfY 显示选中字符Y轴位置
 	 * @param listview
 	 * @param adapter
 	 * @param position 置顶item的index
 	 * @param headerHeight listView悬停栏的高度
 	 */
/*aurora linchunhui disable begin
	public void LetterChanged(String s,float positionOfY,
			ListView listview,BaseAdapter adapter,int position,
			int headerHeight) {
		if(overlay == null || 
				toastLayout == null ||
				handler == null || 
				overlayThread == null ||
				listview == null ||
				adapter == null){
			return ;
		}
		updateOverlayPosition(positionOfY);
		overlay.setText(s);
		toastLayout.setVisibility(View.VISIBLE);	
		handler.removeCallbacks(overlayThread);
		handler.postDelayed(overlayThread, 1000);
        if(LetterSideBar.CHARTS[0].equals(s) && 
        		adapter.getCount()>0){
        	listview.setSelection(0);
        }else if (position >= 0) {
			listview.setSelectionFromTop(position, -headerHeight);
		}
	}
aurora linchunhui disable end*/
	/**
	 * 更新显示选中字母的位置
	 * @param downPositionOfY 点击点在Y轴的位置
	 */
/*aurora linchunhui disable begin
	private void updateOverlayPosition(float downPositionOfY){
		if(toastLayout == null ||
				toastFatherLayout == null ||
				layoutParams == null){
			return ;
		}
		
		int top = (int)(downPositionOfY-toastLayout.getHeight()/2);
		if(top <0){
			top = 0;
		}else if(top+toastLayout.getHeight()>toastFatherLayout.getHeight() ){
			top = toastFatherLayout.getHeight() - toastLayout.getHeight();
		}
				
		layoutParams.setMargins(0, top, initMarginRight, 0);
		toastFatherLayout.updateViewLayout(toastLayout, layoutParams);
	}
aurora linchunhui disable end*/
//	private int alphaIndexer(BaseAdapter adapter,String s) {		
//		int position = -1;
//		if(StringUtils.isEmpty(s) || adapter == null){
//			return position;
//		}
//		AppInfo appInfo;
//		for (int i = 0; i < adapter.getCount(); i++) {
//			appInfo = (AppInfo)adapter.getItem(i);
//			if(appInfo != null && 
//					appInfo.getAppNamePinYin().startsWith(s)){
//				position = i;
//				break;
//			}
//		}
//		return position;
//	}
	
	/**
     * 释放不需要用的对象所占用的堆内存
     */
/*aurora linchunhui disable begin
	public void releaseObject(){
//		overlay = null;
//		toastLayout = null;
//		handler = null;
//		overlayThread = null;	
//		layoutParams = null;		
//		toastFatherLayout = null;
	}
}
aurora linchunhui disable end*/
