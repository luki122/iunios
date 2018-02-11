package com.aurora.community.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.aurora.community.R;
import com.aurora.community.activity.account.VerifyCodeLoader.VC_EVENT;
import com.aurora.community.utils.EncryptUtil;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ToastUtil;
import com.aurora.community.utils.ValidateUtil;
import com.aurora.community.widget.ClearableEditText;
import com.aurora.community.widget.VerifyCodeView;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.UserRegisterObject;
import com.aurora.datauiapi.data.implement.DataResponse;

public class RegisterByEmailActivity extends BaseAccountActivity implements OnClickListener {

	private ClearableEditText cet_email;
	private ClearableEditText cet_password;
//	private Button btn_next;
	private VerifyCodeView mVCIv;
	private EditText mVCCodeEt;
	
	private AccountManager mAccountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register_by_email);
		
		setupViews();

		mAccountManager = new AccountManager(this);
		
		// 弹出软键盘
		//CommonUtil.showSoftInputDelay(this, cet_email, 100);
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		initData();
//		configActionBar(getString(R.string.register_next));
		setListeners();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.register_by_phonenum_title);
		
		addActionBarItem(getString(R.string.register_next), ACTION_BAR_RIGHT_ITEM_ID);
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
//		return getString(R.string.register_by_phonenum_title);
//	}

	private void initViews() {
		cet_email = (ClearableEditText) findViewById(R.id.cet_email);
		cet_password = (ClearableEditText) findViewById(R.id.cet_password);
//		btn_next = (Button) findViewById(R.id.btn_next);
		mVCIv = (VerifyCodeView) findViewById(R.id.vc_code_iv);
	    mVCIv.setVCEvent(VC_EVENT.VC_EVENT_REGISTER);
	    mVCCodeEt = (EditText) findViewById(R.id.vc_code_cet);
		initErrorViews();
	}
	
	private void initData() {
		
	}
	
	private void setListeners() {
//		btn_next.setOnClickListener(this);
		
		setListenerForErrorView(cet_email);
        setListenerForErrorView(cet_password);
        
        if (mActionBarRightTv != null) {
		    mActionBarRightTv.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.btn_next:
//			doNext();
//			break;
//		case R.id.right_tv:
//			doNext();
//			break;
		}
	}
	
	private void doNext() {
		String email = cet_email.getText().toString().trim();
		String pwd = cet_password.getText().toString().trim();
		String validCode = mVCCodeEt.getText().toString().trim();
		
		if (TextUtils.isEmpty(email)) {
			showErrorInfo(findViewById(R.id.cet_email_ly), cet_email, getResources().getString(R.string.register_please_input_email));
			return;
		}
		
		if (!ValidateUtil.isEmailValid(email)) {
			showErrorInfo(findViewById(R.id.cet_email_ly), cet_email, getResources().getString(R.string.register_email_format_invalid));
            return;
        }
		
		if (TextUtils.isEmpty(pwd) || pwd.length() < getResInteger(R.integer.password_min_length)) {
		    showErrorInfo(findViewById(R.id.cet_password_ly), cet_password, getResources().getString(R.string.register_error_pwd_cannot_less_than_8));
			return;
		}
		
		if (!ValidateUtil.isCorrectFormatPwd(pwd)) {
			showErrorInfo(findViewById(R.id.cet_password_ly), cet_password, getResources().getString(R.string.register_error_pwd_format_invalid));
            return;
        }
		
		if (TextUtils.isEmpty(validCode)) {
			showErrorInfo(findViewById(R.id.vc_code_ly), mVCCodeEt, getResources().getString(R.string.register_error_vccode_cannot_be_empty));
			return;
		}
		
		getNetData();
	}
	
	private void getNetData() {
		final String email = cet_email.getText().toString().trim();
		final String password = cet_password.getText().toString().trim();
		final String validCode = mVCCodeEt.getText().toString().trim();
		
		showProgressDialog(getString(R.string.register_sending_email_verify));
		
		mAccountManager.registerAccount(new DataResponse<UserRegisterObject>() {
			@Override
			public void run() {
				Log.i(TAG, "the value=" + value.getCode());
				
				dismissProgressDialog();
				
				// 是否需要刷新图片验证码
				boolean needRefresh = true;
				
				if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
					Intent intent = new Intent(RegisterByEmailActivity.this, EmailVerificationActivity.class);
					intent.putExtra(EmailVerificationActivity.EMAIL, email);
					intent.putExtra(EmailVerificationActivity.PWD, password);
					startActivity(intent);
					
					needRefresh = false;
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_VERCODE_ERROR) {
					ToastUtil.shortToast(getString(R.string.register_error_ercode_error));
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_PHONENUM_ALREADY_REGISTER) {
					ToastUtil.shortToast(getString(R.string.register_error_phonenum_already_register));
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_EMAIL_ALREADY_REGISTER) {
					ToastUtil.shortToast(getString(R.string.register_error_email_already_register));
				} else {
					Log.i(TAG, "call registerAccount: " + value.getDesc());
					ToastUtil.shortToast(getString(R.string.register_fail));
				}
				
				if (needRefresh) {
					// 刷新界面验证码
					if (mVCIv != null) {
						mVCIv.refresh();
					}
				}
			}
		}, RegisterByEmailActivity.this, null, email, password,
			EncryptUtil.getMD5Str(password), SystemUtils.getIMEI(), null, null, validCode);
	}
	
}
