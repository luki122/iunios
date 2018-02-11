package com.aurora.account.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.activity.VerifyCodeLoader.VC_EVENT;
import com.aurora.account.totalCount.TotalCount;
import com.aurora.account.util.EmailUtil;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.util.ValidateUtil;
import com.aurora.account.widget.ClearableEditText;
import com.aurora.account.widget.VerifyCodeView;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.UserVC;
import com.aurora.datauiapi.data.implement.DataResponse;

public class FindPwdByEmailActivity extends BaseActivity implements OnClickListener {
	
	private ClearableEditText cet_email;
	private TextView tv_re_registration;
	private Button btn_reset;
	private VerifyCodeView mVCIv;
	private ClearableEditText mVCCodeEt;
	
	private AccountManager mAccountManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_findpwd_by_email,
				AuroraActionBar.Type.Normal, true);

		initViews();
		initData();
		setListeners();
		
		mAccountManager = new AccountManager(this);
	}
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.find_pwd);
	}
	
	private void initViews() {
		cet_email = (ClearableEditText) findViewById(R.id.cet_email);
		tv_re_registration = (TextView) findViewById(R.id.tv_re_registration);
		btn_reset = (Button) findViewById(R.id.btn_reset);
		
		tv_re_registration.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); 
        tv_re_registration.getPaint().setAntiAlias(true);
        
        mVCIv = (VerifyCodeView) findViewById(R.id.vc_code_iv);
	    mVCIv.setVCEvent(VC_EVENT.VC_EVENT_FINDPWD);
	    mVCCodeEt = (ClearableEditText) findViewById(R.id.vc_code_cet);
	    
//	    cet_email.setFocusable(false);
//	    cet_email.setFocusableInTouchMode(false);
		
		initErrorViews();
	}
	
	private void initData() {
		// 默认编码
//		cet_email.setText("xxxxxxx@qq.com");
	}
	
	private void setListeners() {
		btn_reset.setOnClickListener(this);
		tv_re_registration.setOnClickListener(this);
		setListenerForErrorView(cet_email);
		setListenerForErrorView(mVCCodeEt);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_reset:
			if (checkEmail()) {
				findPassword();
			}
			break;
		case R.id.tv_re_registration:
			setResult(FindPwdActivity.RESULT_OPEN_REGISTER);
			finish();
			break;
		}
	}
	
	private boolean checkEmail() {
		String email = cet_email.getText().toString().trim();
		String validCode = mVCCodeEt.getText().toString().trim();
		
		if (TextUtils.isEmpty(email)) {
			showErrorInfo(findViewById(R.id.cet_email_ly), cet_email, getResources().getString(R.string.register_please_input_email));
			return false;
		}
		
		if (!ValidateUtil.isEmailValid(email)) {
			showErrorInfo(findViewById(R.id.cet_email_ly), cet_email, getResources().getString(R.string.register_email_format_invalid));
            return false;
        }
		
		if (TextUtils.isEmpty(validCode)) {
			showErrorInfo(findViewById(R.id.vc_code_ly), mVCCodeEt, getResources().getString(R.string.change_pwd_error_vccode_cannot_be_empty));
			return false;
		}
		
		return true;
	}
	
	private void findPassword() {
		final String email = cet_email.getText().toString().trim();
		String validCode = mVCCodeEt.getText().toString().trim();
		
		showProgressDialog(getString(R.string.findpwd_sending_reset_email));
		
		mAccountManager.resetLoginPwd(new DataResponse<BaseResponseObject>() {
			@Override
			public void run() {
				Log.i(TAG, "the value=" + value.getCode());
				
				dismissProgressDialog();
				
				// 是否需要刷新图片验证码
				boolean needRefresh = true;
				
				// add by zw 02-13 找回密码次数
				if (value.getCode() == BaseResponseObject.CODE_SUCCESS) 
				{
				new TotalCount(FindPwdByEmailActivity.this, "280",
						"009", 1).CountData();
				}
				else
				{
					new TotalCount(FindPwdByEmailActivity.this, "280",
							"010", 1).CountData();
				}
				// end by zw
				
				
				if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
					AlertDialog.Builder builder = new AlertDialog.Builder(FindPwdByEmailActivity.this);
					builder.setMessage(getString(R.string.findpwd_reset_by_email_tips_dialog));
					builder.setPositiveButton(getString(R.string.dialog_confirm), new Dialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Intent i = new Intent(FindPwdByEmailActivity.this, LoginActivity.class);
							i.putExtra(LoginActivity.EXTRA_KEY_ACCOUNT, email);
							i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
							
							EmailUtil.openEmailLoginPageByBrowser(FindPwdByEmailActivity.this, email, 
									getString(R.string.change_email_error_failed_to_open_browser));
						}
					});
					builder.show();
					
					needRefresh = false;
				} else if (value.getCode() == UserVC.CODE_ERROR_CHECK_INPUT_ERROR) {
					ToastUtil.shortToast(getString(R.string.findpwd_error_checkcode_input_error));
				} else if (value.getCode() == UserVC.CODE_ERROR_EMAIL_NOT_REGISTERED) {
					ToastUtil.shortToast(getString(R.string.findpwd_error_email_not_registered));
				} else if (value.getCode() == UserVC.CODE_ERROR_EMAIL_SEND_ERROR) {
					ToastUtil.shortToast(getString(R.string.findpwd_error_email_send_error));
				} else if (value.getCode() == UserVC.CODE_ERROR_CHECKCODE_NOT_INPUT) {
					ToastUtil.shortToast(getString(R.string.findpwd_error_checkcode_not_input));
				} else {
					ToastUtil.shortToast(value.getDesc());
				}
				
				if (needRefresh) {
					// 刷新界面验证码
					if (mVCIv != null) {
						mVCIv.refresh();
					}
				}
			}
			
		}, FindPwdByEmailActivity.this, email, null, null, null, null, null, validCode, SystemUtils.getIMEI());
		
	}

}
