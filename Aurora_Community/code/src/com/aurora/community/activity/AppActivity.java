package com.aurora.community.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;

import com.aurora.community.R;
import com.aurora.community.interfaces.IAppActionBar;
import com.aurora.community.view.ActionBarLayout;
import com.aurora.community.view.ActionBarLayout.OnActionBarItemClickListener;
import com.aurora.community.view.MenuPopupWindow;
import com.aurora.community.view.MenuPopupWindow.OnBottomMenuClickListener;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class AppActivity extends Activity implements IAppActionBar,OnBottomMenuClickListener {

	private ActionBar mActionBar;
	private ActionBarLayout actionBarLayout;
	private MenuPopupWindow menuPop;
	
	private Animation hideViewShowAnimation;
	
	private FrameLayout fr_action_bar_container,fr_content;
	
	private boolean isEnableActionBar = true;
	
	private ImageView action_bar_divider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		super.setContentView(R.layout.activity_app);
		initViews();
		setupMenu();
	}
	
	protected void enableActionBar(boolean isEnable){
		this.isEnableActionBar = isEnable;
		fr_action_bar_container.setVisibility(isEnableActionBar ? View.VISIBLE : View.GONE);
	}
	
	private void initViews(){
		fr_action_bar_container = (FrameLayout) findViewById(R.id.fr_action_bar_container);
		fr_content = (FrameLayout) findViewById(R.id.fr_content);
		action_bar_divider = (ImageView) findViewById(R.id.action_bar_divider);
		setupActionBar();
	}
	
	protected void enableActionBarDivider(boolean isEnable){
		action_bar_divider.setVisibility(isEnable ? View.VISIBLE : View.GONE);
	}
    @Override
    protected void onResume() {
        super.onResume();
  
        MobclickAgent.onResume(this);
    }

    @Override
	protected void onPause() {
		super.onPause();
		
		MobclickAgent.onPause(this);
	}
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		fr_content.addView(LayoutInflater.from(this).inflate(layoutResID, null), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
	}
	
	public void setCustomerActionBar(View view){
		fr_action_bar_container.removeAllViews();
		fr_action_bar_container.addView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//		mActionBar.setCustomView(view,  new android.app.ActionBar.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT, android.app.ActionBar.LayoutParams.MATCH_PARENT));
	}
	
	public void setCustomerActionBar(int res){
		setCustomerActionBar(LayoutInflater.from(this).inflate(res, null));
	}
	
	private void setupActionBar(){
//		mActionBar = getActionBar();
//		mActionBar.setDisplayUseLogoEnabled(false);
//		mActionBar.setIcon(R.drawable.action_bar_transparent);
//		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//		mActionBar.setDisplayShowCustomEnabled(true);
//		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.aurora_titlebar_bg));
		actionBarLayout = new ActionBarLayout(this);
		fr_action_bar_container.addView(actionBarLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		actionBarLayout.setOnActionBarItemClickListener(actionBarItemClickListener);
	}
	
	private OnActionBarItemClickListener actionBarItemClickListener = new OnActionBarItemClickListener() {
		@Override
		public void onClick(View view, int id) {
			// TODO Auto-generated method stub
			onActionBarItemClick(view, id);
		}
	};
	
	private void setupMenu(){
		menuPop = new MenuPopupWindow(this);
		menuPop.setOnBottomMenuClickListener(this);
		menuPop.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				if(hideView != null)
				{
					((ViewGroup)getWindow().getDecorView().getRootView()).removeView(hideView);
				}
			}
		});
	}
	
	@Override
	public void setTitleColor(int color) {
		if(actionBarLayout != null)
		actionBarLayout.setTitleColor(color);
	}
	
	protected void addMenu(String menu){
		menuPop.addMenu(menu);
	}
	
	private ImageButton hideView;//灰色朦板
	
	@SuppressLint("ResourceAsColor")
	protected void showMenu(){
		menuPop.showAtLocation(getWindow().getDecorView(), Gravity.LEFT, 0, 0);
//		menuPop.showAsDropDown(actionBarLayout);
		if(hideView == null)
		{
			hideView = new ImageButton(this);
			hideView.setBackgroundColor(R.color.activity_post_detail_comment_iv_hide_bg);
			hideViewShowAnimation = new AlphaAnimation(0f, 1f);
			hideViewShowAnimation.setDuration(200);
			hideView.setFocusableInTouchMode(true);
		}
		((ViewGroup)getWindow().getDecorView().getRootView()).addView(hideView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		hideView.startAnimation(hideViewShowAnimation);
	}
	
	protected void hideMenu(){
		menuPop.dismiss();
	}
	
	
	@Override
	public void onMenuClick(int position,String menuText){
	}
	
	protected void onActionBarItemClick(View view,int itemId){
		
	}

	@Override
	public void enableBackItem(boolean isEnable) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.enableBackItem(isEnable);
	}

	@Override
	public void setBackItemRes(int imageRes) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.setBackItemRes(imageRes);
	}

	@Override
	public void setTitleText(String titleText) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.setTitleText(titleText);
	}

	@Override
	public void setTitleRes(int titleRes) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.setTitleRes(titleRes);
	}
	
	@Override
	public void addActionBarItem(int imageRes, int itemId) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.addActionBarItem(imageRes, itemId);
	}

	@Override
	public void changeActionBarItemImageRes(int imageRes, int itemId) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.changeActionBarItemImageRes(imageRes, itemId);
	}

	@Override
	public void addActionBarItem(String itemText, int Id) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.addActionBarItem(itemText, Id);
	}

	@Override
	public void removeActionBarItem(int Id) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.removeActionBarItem(Id);
	}

	@Override
	public void setTitleSize(float size) {
		// TODO Auto-generated method stub
		if(actionBarLayout != null)
		actionBarLayout.setTitleSize(size);
	}

	@Override
	public View getActionBarItem(int itemId) {
		// TODO Auto-generated method stub
		return actionBarLayout.getActionBarItem(itemId);
	}

	@Override
	public void setActionBarBg(int res) {
		// TODO Auto-generated method stub
		
		findViewById(R.id.activity_layout).setBackgroundResource(res);
//		if(actionBarLayout != null)
//			actionBarLayout.setActionBarBg(res);
	}
	
}


