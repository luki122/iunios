package com.aurora.community.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.aurora.community.R;
import com.aurora.community.utils.EncryptUtil;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ToastUtil;
import com.aurora.community.utils.ValidateUtil;
import com.aurora.community.widget.ClearableEditText;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.implement.DataResponse;

public class ResetPasswordActivity extends BaseAccountActivity implements OnClickListener {
	
	public static final String PHONE_NUM = "phone_num";
	public static final String VERIFY_CODE = "verify_code";
	public static final String VCID = "vcid";
	
	private ClearableEditText cet_password;
//	private Button btn_finish;
	
	private String phoneNum = "";
	private String verifyCode = "";
	private String vcId = "";
	
	private AccountManager mAccountManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		getIntentData();
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_reset_password);

		setupViews();
		
		mAccountManager = new AccountManager(this);
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		initData();
//		configActionBar(getString(R.string.finish));
		setListeners();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleText(getString(R.string.find_pwd) + "3/3");
		
		addActionBarItem(getString(R.string.finish), ACTION_BAR_RIGHT_ITEM_ID);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		case ACTION_BAR_RIGHT_ITEM_ID:
			doNext();
			break;
		}
	}

//	@Override
//	protected String getActionBarTitle() {
//		return getString(R.string.find_pwd) + "3/3";
//	}
	
	private void getIntentData() {
	    final Intent intent = getIntent();
		if (intent != null) {
			phoneNum = intent.getStringExtra(PHONE_NUM);
			verifyCode = intent.getStringExtra(VERIFY_CODE);
			vcId = intent.getStringExtra(VCID);
		}
		
		Log.i(TAG, "phoneNum: " + phoneNum);
		Log.i(TAG, "verifyCode: " + verifyCode);
		Log.i(TAG, "vcId: " + vcId);
	}

	private void initViews() {
		cet_password = (ClearableEditText) findViewById(R.id.cet_password);
//		btn_finish = (Button) findViewById(R.id.btn_finish);
		initErrorViews();
	}
	
	private void initData() {
		
	}
	
	private void setListeners() {
//		btn_finish.setOnClickListener(this);
		setListenerForErrorView(cet_password);
		
		if (mActionBarRightTv != null) {
		    mActionBarRightTv.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.btn_finish:
//			doNext();
//			break;
//		case R.id.right_tv:
//			doNext();
//			break;
		}
	}
	
	private void doNext() {
		String pwd = cet_password.getText().toString().trim();
		
		if (TextUtils.isEmpty(pwd)) {
		    showErrorInfo(findViewById(R.id.cet_password_ly), cet_password, getResources().getString(R.string.register_error_pwd_cannot_be_empty));
			return;
		}
		
		if (pwd.length() < 8) {
			showErrorInfo(findViewById(R.id.cet_password_ly), cet_password, getResources().getString(R.string.register_error_pwd_cannot_less_than_8));
			return;
		}
		
		if (!ValidateUtil.isCorrectFormatPwd(pwd)) {
		    showErrorInfo(findViewById(R.id.cet_password_ly), cet_password, getResources().getString(R.string.register_error_pwd_format_invalid));
            return;
        }
		
		getNetData();
	}
	
	private void getNetData() {
		final String pwd = cet_password.getText().toString().trim();
		
		showProgressDialog(getString(R.string.findpwd_finding_pwd));
		
		mAccountManager.resetLoginPwd(new DataResponse<BaseResponseObject>() {
			@Override
			public void run() {
				Log.i(TAG, "the value=" + value.getCode());
				
				dismissProgressDialog();
				
				if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
					ToastUtil.shortToast(R.string.findpwd_find_success);
					
					Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(LoginActivity.EXTRA_KEY_ACCOUNT, phoneNum);
					intent.putExtra(LoginActivity.EXTRA_KEY_PWD, pwd);
					intent.putExtra(LoginActivity.EXTRA_KEY_NEED_AUTO_LOGIN, true);
					startActivity(intent);
					
				} else if (value.getCode() == BaseResponseObject.CODE_ERROR_PHONE_VERIFYCODE_WRONG) {
					ToastUtil.shortToast(getString(R.string.findpwd_phone_verifycode_wrong));
				} else {
					ToastUtil.shortToast(getString(R.string.findpwd_find_fail));
				}
			}
			
		}, ResetPasswordActivity.this, null, phoneNum, pwd, EncryptUtil.getMD5Str(pwd), verifyCode, vcId, null, SystemUtils.getIMEI());
		
	}

}
