package com.aurora.frameworkdemo;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

public class AuroraActionbarDemo extends AuroraActivity{

	private AuroraActionBar mAuroraActionBar;
	private int ii = 0;
	private int jj = 0;
	private int kk = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_actionbar_demo, Type.Normal);
		
		//得到AuroraActionbar对象
		mAuroraActionBar = getAuroraActionBar();
		
		//可以给自己的AuroraActionbar设置titile
		mAuroraActionBar.setTitle("Normal");
		
		//添加右侧Item，第二个参数是监听时需要用到的
		mAuroraActionBar.addItem(AuroraActionBarItem.Type.Add, 1);
		
		//添加右侧Item，如果自己定义layout，第二个参数没用了，自己通过findViewById去设置监听
		mAuroraActionBar.addItem(R.layout.text, 2);
		
		//设置item回调接口
		mAuroraActionBar.setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
			
			@Override
			public void onAuroraActionBarItemClicked(int itemId) {
				// TODO Auto-generated method stub
				switch (itemId) {
				//对应addItem的第二个参数
				case 1:
					Toast.makeText(AuroraActionbarDemo.this, "点击Add", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		});
		TextView text = (TextView)findViewById(R.id.text1);
		text.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(AuroraActionbarDemo.this, "点击Text", Toast.LENGTH_SHORT).show();
			}
		});
		
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ii % 2 == 0) {
					//删除item，支持根据id或者添加顺序删除，即public void removeItem(int position) ，下面是根据id删除的
					mAuroraActionBar.removeItemByItemId(1);
				} else {
					mAuroraActionBar.addItem(AuroraActionBarItem.Type.Add, 1);
				}
				ii++;
			}
		});
		
		findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(jj % 2 == 0) {
					//可以调用函数改变actionbar的显示类型
					mAuroraActionBar.changeAuroraActionbarType(Type.Dashboard);
					mAuroraActionBar.getCancelButton().setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Toast.makeText(AuroraActionbarDemo.this, "点击取消", Toast.LENGTH_SHORT).show();
						}
					});
					mAuroraActionBar.getOkButton().setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Toast.makeText(AuroraActionbarDemo.this, "点击确定", Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					//可以调用函数改变actionbar的显示类型
					mAuroraActionBar.changeAuroraActionbarType(Type.Normal);
				}
				jj++;
			}
		});
		
		//设置底部bottombar回调
		setAuroraMenuCallBack(new OnAuroraMenuItemClickListener() {
			
			@Override
			public void auroraMenuItemClick(int itemId) {
				// TODO Auto-generated method stub
				switch (itemId) {
				case R.id.aurora_bottombar_id:
					Toast.makeText(AuroraActionbarDemo.this, "BottomBar Click",Toast.LENGTH_SHORT).show();
				default:
					break;
				}
			}
		});
		//先设置底部bottombar回调在初始化
		mAuroraActionBar.initActionBottomBarMenu(R.menu.bottombar_menu, 1);
		
		findViewById(R.id.button3).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//控制是否显示底部bottombar
				if(kk % 2 == 0) {
					mAuroraActionBar.setShowBottomBarMenu(true);
				} else {
					mAuroraActionBar.setShowBottomBarMenu(false);
				}
				kk++;
				//显示或者取消显示批量操作和底部bottombar
				mAuroraActionBar.showActionBarDashBoard();
			}
		});
		
		//批量操作的取消按键监听
		if ( mAuroraActionBar.getSelectLeftButton() != null ) {
			mAuroraActionBar.getSelectLeftButton().setOnClickListener(new OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			// TODO Auto-generated method stub
        			mAuroraActionBar.setShowBottomBarMenu(false);
        			mAuroraActionBar.showActionBarDashBoard();
        		}
        	});
		}
		
		//批量操作的确定按键监听
		if ( mAuroraActionBar.getSelectRightButton() != null ) {
			mAuroraActionBar.getSelectRightButton().setOnClickListener(new OnClickListener() {

        		@Override
        		public void onClick(View v) {
        			// TODO Auto-generated method stub
        			mAuroraActionBar.setShowBottomBarMenu(false);
        			mAuroraActionBar.showActionBarDashBoard();
        		}
        	});
		}
	}
}
