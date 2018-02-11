package com.aurora.frameworkdemo;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.Type;

public class AuroraActivitySearchDemo extends AuroraActivity{
	
	private AuroraActionBar mAuroraActionBar;
	private int ii = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//第三个参数为true表示需要搜索框
		setAuroraContentView(R.layout.aurora_activity_search_demo, Type.Normal, true);
		
		//得到AuroraActionbar对象
		mAuroraActionBar = getAuroraActionBar();
				
		//可以给自己的AuroraActionbar设置titile
		mAuroraActionBar.setTitle("AuroraActivitySearchDemo");
		
		//我们可以给搜索框设置监听事件，调用如下接口
		setOnQueryTextListener(new OnSearchViewQueryTextChangeListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		//
		
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//显示搜索框
				showSearchviewLayout();
			}
		});
		
		//设置对搜索界面完全退出时的监听事件
		setOnSearchViewQuitListener(new OnSearchViewQuitListener() {
			
			@Override
			public boolean quit() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		//设置对点击搜索界面蒙版时的监听事件
		setOnSearchBackgroundClickListener(new OnSearchBackgroundClickListener() {
			
			@Override
			public boolean searchBackgroundClick() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		//得到蒙版的View
		getSearchViewGreyBackground().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//参数clearText表明隐藏搜索界面时是否需要清空搜索框的内容，默认会清空
				hideSearchViewLayout(true);
				//hideSearchViewLayout();
			}
		});

		getSearchViewRightButton().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hideSearchviewLayout();
			}
		});
	}

}
