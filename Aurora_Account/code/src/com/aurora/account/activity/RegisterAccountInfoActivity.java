package com.aurora.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.util.ToastUtil;

public class RegisterAccountInfoActivity extends BaseActivity implements OnClickListener {

	private Button btn_save;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_register_accountinfo,
				AuroraActionBar.Type.Normal, true);

		initViews();
		setListeners();
	}

	@Override
	protected String getActionBarTitle() {
		return getString(R.string.register_account_info_title);
	}

	private void initViews() {
		btn_save = (Button) findViewById(R.id.btn_save);
	}

	private void setListeners() {
		btn_save.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save:
			ToastUtil.longToast("保存成功");
			Intent loginIntent = new Intent(RegisterAccountInfoActivity.this, LoginActivity.class);
			startActivity(loginIntent);
			break;
		}
	}

}
