package com.android.auroramusic.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import aurora.app.AuroraActivity;

import com.android.auroramusic.util.LogUtil;
import com.android.music.R;

@Deprecated //add by JXH
public class AuroraPlayListDetail extends AuroraActivity {

	private static final String TAG = "AuroraPlayListDetail";
	private View titleLayout,editLayout;
	private TextView leftBtn,rightBtn;
	private boolean isEditMode=false; //
	private boolean isAnimationRun=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aurora_playlist_detail);
		try {
			initView();
		} catch (Exception e) {
			
		}
	}
	
	private void initView(){
		TextView title = (TextView)findViewById(R.id.aurora_title_text);
		titleLayout = findViewById(R.id.title_layout);
		editLayout = findViewById(R.id.edite_layout);
		leftBtn = (TextView)findViewById(R.id.id_left_btn);
		rightBtn = (TextView) findViewById(R.id.id_right_btn);
		leftBtn.setText("取消");
		rightBtn.setText("全选");
		LogUtil.d(TAG, "roatex:"+titleLayout.getX()+" height:"+titleLayout.getHeight());
		Typeface mFace = Typeface.createFromFile("system/fonts/title.ttf");
		title.setTypeface(mFace);
		title.setText(R.string.recently_added_songs);
	}
	
	public void onTestClick(View view){
		if(!isEditMode){
			setTitleLayoutAnimation(0);
		}else{
			setEditLayoutAnimation(0);
		}
	}
	
	private void setEditLayoutAnimation(int type){
		TranslateAnimation trans;
		int height = editLayout.getHeight();
		if(type==0){
			trans = new TranslateAnimation(0, 0, 0, -1*height);
		}else{
			trans = new TranslateAnimation(0, 0, -1*height, 0);
		}
		trans.setDuration(300);
		trans.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				LogUtil.d(TAG, "onAnimationStart");
				
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {

				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				if(!isEditMode){
					//进入编辑模式
					isEditMode=true;
				}else{
					titleLayout.setVisibility(View.VISIBLE);
					editLayout.setVisibility(View.GONE);
					setTitleLayoutAnimation(1);
				}
			}
		});
		editLayout.startAnimation(trans);
	}
	
	private void setTitleLayoutAnimation(int type){
		TranslateAnimation trans;
		int height = titleLayout.getHeight();
		if(type==0){
			trans = new TranslateAnimation(0, 0, 0, -1*height);
		}else{
			trans = new TranslateAnimation(0, 0, -1*height, 0);
		}		
		trans.setDuration(300);
		trans.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				LogUtil.d(TAG, "onAnimationStart");
				
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {

				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				LogUtil.d(TAG, "onAnimationEnd");
				if(!isEditMode){
					titleLayout.setVisibility(View.GONE);
					editLayout.setVisibility(View.VISIBLE);
					setEditLayoutAnimation(1);	
				}else{
					//退出编辑模式
					isEditMode=false;
				}							
			}
		});
		titleLayout.startAnimation(trans);
	}
}
