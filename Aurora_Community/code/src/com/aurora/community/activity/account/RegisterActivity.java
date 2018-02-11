package com.aurora.community.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.aurora.community.R;

public class RegisterActivity extends BaseAccountActivity implements OnClickListener {
	
	private RelativeLayout rl_registerByPhoneNum;
	private RelativeLayout rl_registerByEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);
		
		setupViews();
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		setListeners();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.register_title);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		}
	}
	
//	@Override
//	protected String getActionBarTitle() {
//		return getString(R.string.register_title);
//	}
	
	private void initViews() {
		rl_registerByPhoneNum = (RelativeLayout) findViewById(R.id.rl_registerByPhoneNum);
		rl_registerByEmail = (RelativeLayout) findViewById(R.id.rl_registerByEmail);
	}
	
	private void setListeners() {
		rl_registerByPhoneNum.setOnClickListener(this);
		rl_registerByEmail.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_registerByPhoneNum:
			Intent phoneNumIntent = new Intent(RegisterActivity.this, RegisterByPhoneNumActivity.class);
			startActivity(phoneNumIntent);
			break;
		case R.id.rl_registerByEmail:
			Intent emailIntent = new Intent(RegisterActivity.this, RegisterByEmailActivity.class);
			startActivity(emailIntent);
			break;
		}
	}
	
}
