package com.aurora.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import aurora.widget.AuroraActionBar;

import com.aurora.account.R;

public class RegisterActivity extends BaseActivity implements OnClickListener {
	private RelativeLayout rl_registerByPhoneNum;
	private RelativeLayout rl_registerByEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_register,
				AuroraActionBar.Type.Normal, true);
		
		initViews();
		setListeners();
	}
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.register_title);
	}
	
	private void initViews() {
		rl_registerByPhoneNum = (RelativeLayout) findViewById(R.id.rl_registerByPhoneNum);
		rl_registerByEmail = (RelativeLayout) findViewById(R.id.rl_registerByEmail);
	}
	
	private void setListeners() {
		rl_registerByPhoneNum.setOnClickListener(this);
		rl_registerByEmail.setOnClickListener(this);
	}
	
	@Override
    protected boolean shouldHandleFocusChanged() {
        return false;
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
