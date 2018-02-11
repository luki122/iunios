package com.aurora.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;

import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.model.CountryCode;
import com.aurora.account.util.EncryptUtil;
import com.aurora.account.util.Log;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.util.ValidateUtil;
import com.aurora.account.widget.ClearableEditText;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.implement.DataResponse;

/**
 * 验证当前绑定手机号或者邮箱界面，如果有的话
 * @author JimXia
 *
 * @date 2014-9-30 下午3:28:57
 */
public class ValidatePwdActivity extends BaseActivity implements OnClickListener {
    public static final String EXTRA_KEY_MODE = "mode";
    public static final int MODE_PHONE = 1;
    public static final int MODE_EMAIL = 2;
    
    private ClearableEditText mAccountEt;
	private ClearableEditText mPwdEt;
//	private Button btn_next;
	
	private AccountManager mAccountManager;
	private int mMode = MODE_PHONE;
	
	private String lastPwd = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.validate_pwd_activity,
				AuroraActionBar.Type.Normal, true);
		
		mMode = getIntent().getIntExtra(EXTRA_KEY_MODE, MODE_PHONE);
		
		initViews();
		configActionBar(getString(R.string.register_next));
		setListeners();
		
		mAccountManager = new AccountManager(this);
	}

	@Override
	protected String getActionBarTitle() {
	    if (mMode == MODE_PHONE) {
	        return getString(R.string.validate_pwd_title_phone);
	    }
	    
	    return getString(R.string.validate_pwd_title_email);
	}

	private void initViews() {
	    initErrorViews();
	    mAccountEt = (ClearableEditText) findViewById(R.id.account_cet);
	    mPwdEt = (ClearableEditText) findViewById(R.id.password_cet);
//		btn_next = (Button) findViewById(R.id.btn_next);
		if (mMode == MODE_PHONE) {
		    mAccountEt.setHint(R.string.validate_pwd_account_phone_hint);
		    mAccountEt.setRawInputType(EditorInfo.TYPE_CLASS_PHONE);
		} else {
		    mAccountEt.setHint(R.string.validate_pwd_account_email_hint);
		    mAccountEt.setRawInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}
	}
	
	private void setListeners() {
//		btn_next.setOnClickListener(this);
	    mActionBarRightTv.setOnClickListener(this);
		setListenerForErrorView(mAccountEt);
		setListenerForErrorView(mPwdEt);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.btn_next:
//			doNext();
//			break;
		case R.id.right_tv:
		    doNext();
		    break;
		}
	}
	
	private String getAccountEmptyErrorMsg() {
	    if (mMode == MODE_PHONE) {
	        return getString(R.string.validate_pwd_error_phone_is_empty);
	    }
	    
	    return getString(R.string.validate_pwd_error_email_is_empty);
	}
	
	private void doNext() {
	    String account = mAccountEt.getText().toString().trim();
	    if (TextUtils.isEmpty(account)) {
            showErrorInfo(mAccountEt, mAccountEt, getAccountEmptyErrorMsg());
            return;
        }
	    
	    if (mMode == MODE_PHONE) {
	        if (!ValidateUtil.isMobilePhoneNumVaild(account)) {
	            showErrorInfo(mAccountEt, mAccountEt, getString(R.string.validate_pwd_error_phone_format_invalid));
	            return;
	        }
	    } else {
	        if (!ValidateUtil.isEmailValid(account)) {
                showErrorInfo(mAccountEt, mAccountEt, getString(R.string.validate_pwd_error_email_format_invalid));
                return;
            }
	    }
	    
		String pwd = mPwdEt.getText().toString().trim();
		
		if (TextUtils.isEmpty(pwd)) {
		    showErrorInfo(mPwdEt, mPwdEt, getString(R.string.validate_pwd_error_pwd_is_empty));
			return;
		}
		
		if (mMode == MODE_PHONE) {
            validatePhoneByServer(account, pwd);
        } else {
            validateEmailByServer(account, pwd);
        }
	}
	
	private void validatePhoneByServer(final String phoneNum, final String pwd) {
	    if (mPref.isVercodeExpired(phoneNum,
	            PhoneNumVerificationActivity.MODE_FOR_VALIDATE_OLD_PHONE)
	            || !pwd.equals(lastPwd)) {
	        showProgressDialog(getString(R.string.validate_pwd_validating));
	        mAccountManager.checkCurPhone(new DataResponse<BaseResponseObject>() {
	            public void run() {
	                dismissProgressDialog();
	                
	                if (value != null) {
	                    final int code = value.getCode();
	                     Log.i(TAG, "Jim, the value=" + code);
	                     
	                     switch (code) {
	                         case BaseResponseObject.CODE_SUCCESS:
	                        	 lastPwd = pwd;
	                             mPref.recordLastSendTime(phoneNum,
	                                     PhoneNumVerificationActivity.MODE_FOR_VALIDATE_OLD_PHONE);
	                             openPhoneNumVerificationActivity(phoneNum, pwd);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_CUR_PHONE_INVALID:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_phone_error_cur_phone_invalid);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_PWD_ERROR:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_phone_error_pwd_error);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_NO_PHONE_BIND:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_phone_error_no_phone_bind);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_SEND_SMS_TOO_MANY:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_phone_error_send_sms_too_many);
	                             break;
	                         default:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.validate_pwd_error_unknow_error);
	                             break;
	                     }
	                }
	            }
	        }, this, mPref.getUserID(), mPref.getUserKey(), phoneNum, EncryptUtil.getMD5Str(pwd));
	    } else {
	        openPhoneNumVerificationActivity(phoneNum, pwd);
	    }
    }
	
	private void openPhoneNumVerificationActivity(String phoneNum, String pwd) {
	    Intent intent = new Intent(ValidatePwdActivity.this, PhoneNumVerificationActivity.class);
        intent.putExtra(PhoneNumVerificationActivity.EXTRA_KEY_TITLE,
                getString(R.string.validate_old_phone_title));
        intent.putExtra(PhoneNumVerificationActivity.EXTRA_KEY_MODE,
                PhoneNumVerificationActivity.MODE_FOR_VALIDATE_OLD_PHONE);
        intent.putExtra(PhoneNumVerificationActivity.PHONE_NUM, phoneNum);
        intent.putExtra(PhoneNumVerificationActivity.PWD, pwd);
        intent.putExtra(PhoneNumVerificationActivity.COUNTRY_CODE,
                CountryCode.getDefault().getCode());
        startActivity(intent);
	}
	
	private void validateEmailByServer(final String email, String pwd) {
	    if (mPref.isVercodeExpired(email,
	            EmailVerificationActivity.MODE_FOR_CHANGE_EMAIL)) {
	        showProgressDialog(getString(R.string.validate_pwd_validating));
	        mAccountManager.checkCurEmail(new DataResponse<BaseResponseObject>() {
	            public void run() {
	                dismissProgressDialog();
	                
	                if (value != null) {
	                    final int code = value.getCode();
	                     Log.i(TAG, "Jim, the value=" + code);
	                     
	                     switch (code) {
	                         case BaseResponseObject.CODE_SUCCESS:
	                             mPref.recordLastSendTime(email,
	                                     EmailVerificationActivity.MODE_FOR_CHANGE_EMAIL);
	                             openEmailVerificationActivity(email);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_NO_EMAIL_BIND:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_email_error_no_email_bind);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_CUR_EMAIL_INVALID:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_email_error_cur_email_invalid);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_SEND_MAIL_TOO_MANY:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.change_email_error_send_email_too_many);
	                             break;
	                         default:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(R.string.validate_pwd_error_unknow_error);
	                             break;
	                     }
	                }
	            }
	        }, this, mPref.getUserID(), mPref.getUserKey(), email);
	    } else {
	        openEmailVerificationActivity(email);
	    }
    }
	
	private void openEmailVerificationActivity(String email) {
	    Intent intent = new Intent(ValidatePwdActivity.this, EmailVerificationActivity.class);
        intent.putExtra(EmailVerificationActivity.EXTRA_KEY_TITLE,
                getString(R.string.change_email_verification_title));
        intent.putExtra(EmailVerificationActivity.EXTRA_KEY_MODE, EmailVerificationActivity.MODE_FOR_CHANGE_EMAIL);
        intent.putExtra(EmailVerificationActivity.EMAIL, email);
        startActivity(intent);
	}
}