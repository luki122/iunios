package com.aurora.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import aurora.widget.AuroraActionBar;

import com.aurora.account.R;

public class FindPwdActivity extends BaseActivity implements OnClickListener {
	
	public static final int REQUEST_FIND_BY_EMAIL = 1000;
	public static final int RESULT_OPEN_REGISTER = 2000;
	
	private RelativeLayout rl_findpwdByPhoneNum;
	private RelativeLayout rl_findpwdByEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_findpwd,
				AuroraActionBar.Type.Normal, true);
		
		initViews();
		setListeners();
	}
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.find_pwd);
	}
	
	private void initViews() {
		rl_findpwdByPhoneNum = (RelativeLayout) findViewById(R.id.rl_findpwdByPhoneNum);
		rl_findpwdByEmail = (RelativeLayout) findViewById(R.id.rl_findpwdByEmail);
	}
	
	private void setListeners() {
		rl_findpwdByPhoneNum.setOnClickListener(this);
		rl_findpwdByEmail.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_findpwdByPhoneNum:
			Intent phoneNumIntent = new Intent(FindPwdActivity.this, FindPwdByPhoneNumActivity.class);
			startActivity(phoneNumIntent);
			break;
		case R.id.rl_findpwdByEmail:
			Intent emailIntent = new Intent(FindPwdActivity.this, FindPwdByEmailActivity.class);
			startActivityForResult(emailIntent, REQUEST_FIND_BY_EMAIL);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_FIND_BY_EMAIL && resultCode == RESULT_OPEN_REGISTER) {
			finish();
			Intent i = new Intent(this, RegisterActivity.class);
			startActivity(i);
		}
	}

}
