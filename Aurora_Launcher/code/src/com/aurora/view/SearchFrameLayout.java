package com.aurora.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class SearchFrameLayout extends FrameLayout{
	/**
	 * 如果在xml中应用到这个layout则必须添加这个初始化函数
	 */
    public SearchFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	//Log.e("HJJ", "dispatchTouchEvent==>" + " RawXY(" + ev.getRawX() + "," + ev.getRawY() + ")");
    	// TODO Auto-generated method stub
    	// return super.dispatchTouchEvent(ev);
    	// 这样写的话EditText可以听到点击事件，如果直接return true，就不再往下传递了，因此听不到点击事件，无法拉起编辑框。
        // 添加这段处理的目的是因为整个layout有点小缝隙，这个小缝可以响应桌面的点击事件
    	super.dispatchTouchEvent(ev);
    	return true;
    }
}
