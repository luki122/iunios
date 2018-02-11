package com.aurora.frameworkdemo;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraSystemMenu;

public class AuroraMenuDemo extends AuroraActivity{
	
	private AuroraActionBar mAuroraActionBar;
	private int ii = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_menu_demo, Type.Normal);
		
		//得到AuroraActionbar对象
		mAuroraActionBar = getAuroraActionBar();
				
		//可以给自己的AuroraActionbar设置titile
		mAuroraActionBar.setTitle("AuroraMenuDemo");
		
		//设置监听
		setAuroraMenuCallBack(new OnAuroraMenuItemClickListener() {
			
			@Override
			public void auroraMenuItemClick(int itemId) {
				// TODO Auto-generated method stub
				switch (itemId) {
				case R.id.aurora_bottombar_id:
					Toast.makeText(AuroraMenuDemo.this, "AuroraMenu Click",
							Toast.LENGTH_SHORT).show();
				case 111:
					Toast.makeText(AuroraMenuDemo.this, "add AuroraMenu Click",
							Toast.LENGTH_SHORT).show();
				default:
					break;
				}
			}
		});
		
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(getAuroraMenu().isShowing()) {
					dismissAuroraMenu();
				} else {
					showAuroraMenu();
				}
			}
		});
		
		//静态设置menu
		setAuroraMenuItems(R.menu.bottombar_menu);
		
		//也可以动态添加menu,通过AuroraSystemMenu调用
		final AuroraSystemMenu auroraSystemMenu = getAuroraMenu();
		findViewById(R.id.button2).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ii % 2 == 0) {
					//添加menu，如果没有icon，则传入-1
					auroraSystemMenu.addMenu(111, "添加menu", R.drawable.ic_launcher);
				} else {
					//根据itemid删除menu
					auroraSystemMenu.removeMenuByItemId(111);
				}
				ii++;
			}
		});
	}
}
