package com.aurora.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.util.Log;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.util.ValidateUtil;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.utils.DensityUtil;

/**
 * 修改绑定手机号界面
 * @author JimXia
 *
 * @date 2014-9-30 下午3:29:07
 */
public class ChangeEmailActivity extends BaseActivity implements OnClickListener {
    
    public static final String EXTRA_KEY_MODE = "mode";
    public static final int MODE_SET = 1;
    public static final int MODE_REBIND = 2;
    
	private EditText mNewEmailEt;
//	private Button mNextBtn;
	
	private AccountManager mAccountManager;
	private int mMode = MODE_REBIND;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.change_email_activity,
				AuroraActionBar.Type.Normal, true);
		mMode = getIntent().getIntExtra(EXTRA_KEY_MODE, MODE_REBIND);
		
		initViews();
		configActionBar(getString(R.string.register_next));
		setListeners();
		
		mAccountManager = new AccountManager(this);
	}
	
	@Override
    protected String getActionBarTitle() {
	    if (mMode == MODE_REBIND) {
	        return getResources().getString(R.string.validate_pwd_title_email);
	    }
	    
        return getResources().getString(R.string.change_email_title_set);
    }
	
	private void initViews() {
	    mNewEmailEt = (EditText) findViewById(R.id.new_email_et);
	    mNewEmailEt.setPadding(mNewEmailEt.getPaddingLeft(), mNewEmailEt.getPaddingTop(),DensityUtil.dip2px(this, 16), mNewEmailEt.getPaddingBottom());
//	    mNextBtn = (Button) findViewById(R.id.btn_next);
	    initErrorViews();
	    if (mMode == MODE_SET) {
	        mNewEmailEt.setHint(R.string.change_email_new_email_hint_set);
	    }
	}
	
	private void setListeners() {
//	    mNextBtn.setOnClickListener(this);
	    mActionBarRightTv.setOnClickListener(this);
	    setListenerForErrorView(mNewEmailEt);
	}

	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
//	        case R.id.btn_next:
//	            changeEmail();
//	            break;
	        case R.id.right_tv:
	            changeEmail();
	            break;
	    }
	}
	
	private void changeEmail() {
	    String newEmail = mNewEmailEt.getText().toString().trim();
	    if (TextUtils.isEmpty(newEmail)) {
//	        showErrorInfo(mNewEmailEt, mNewEmailEt,
//	                mMode == MODE_REBIND ? getString(R.string.change_email_error_new_email_is_empty):
//	                    getString(R.string.change_email_new_email_hint_set));
	        showErrorInfo(mNewEmailEt, mNewEmailEt,
                    mMode == MODE_REBIND ? getString(R.string.validate_pwd_error_email_is_empty):
                        getString(R.string.change_email_new_email_hint_set));
            return;
        }
	    
	    if (!ValidateUtil.isEmailValid(newEmail)) {
	        showErrorInfo(mNewEmailEt, mNewEmailEt, getString(mMode == MODE_REBIND ?
	                R.string.validate_pwd_error_email_format_invalid: R.string.register_email_format_invalid));
            return;
	    }
	    
	    if (mMode == MODE_REBIND) {
	        validateEmailByServer(newEmail);
	    } else {
	        getNetData(newEmail);
	    }
	}
	
	private void validateEmailByServer(final String email) {
        showProgressDialog(getString(R.string.validate_pwd_validating));
        mAccountManager.checkCurEmail(new DataResponse<BaseResponseObject>() {
            public void run() {
                dismissProgressDialog();
                
                if (value != null) {
                    final int code = value.getCode();
                     Log.i(TAG, "Jim, the value=" + code);
                     
                     switch (code) {
                         case BaseResponseObject.CODE_SUCCESS:
                             Intent intent = new Intent(ChangeEmailActivity.this, EmailVerificationActivity.class);
                             intent.putExtra(EmailVerificationActivity.EXTRA_KEY_TITLE,
                                     getString(R.string.change_email_verification_title));
                             intent.putExtra(EmailVerificationActivity.EXTRA_KEY_MODE,
                                     EmailVerificationActivity.MODE_FOR_VALIDATE_OLD_EMAIL);
                             intent.putExtra(EmailVerificationActivity.EMAIL, email);
                             startActivity(intent);
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
    }
	
	private void getNetData(final String newEmail) {
        showProgressDialog(getString(R.string.change_email_binding));
        mAccountManager.changeEmail(new DataResponse<BaseResponseObject>() {
            public void run() {
                dismissProgressDialog();
                if (value != null) {
                    final int code = value.getCode();
                    Log.i(TAG, "the value=" + code);
                    if (code == BaseResponseObject.CODE_SUCCESS) {
                        Intent intent = new Intent(ChangeEmailActivity.this, EmailVerificationActivity.class);
                        intent.putExtra(EmailVerificationActivity.EXTRA_KEY_TITLE,
                                mMode == MODE_REBIND ? getString(R.string.change_email_verification_title):
                                    getString(R.string.change_email_verification_title_set));
                        intent.putExtra(EmailVerificationActivity.EXTRA_KEY_MODE, EmailVerificationActivity.MODE_FOR_CHANGE_EMAIL);
                        intent.putExtra(EmailVerificationActivity.EMAIL, newEmail);
                        startActivity(intent);
                    } else if (code == BaseResponseObject.CODE_ERROR_EMAIL_ALREADY_BIND) {
                        Log.d(TAG, "Jim, error: " + value.getDesc());
                        ToastUtil.shortToast(R.string.change_email_error_already_bind);
                    } else if (code == BaseResponseObject.CODE_ERROR_INVALID_EMAIL) {
                        Log.d(TAG, "Jim, error: " + value.getDesc());
                        ToastUtil.shortToast(R.string.register_email_format_invalid);
                    } else if (code == BaseResponseObject.CODE_ERROR_SEND_MAIL_TOO_MANY) {
                        Log.d(TAG, "Jim, error: " + value.getDesc());
                        ToastUtil.shortToast(R.string.change_email_error_send_email_too_many);
                    } else {
                        Log.d(TAG, "Jim, error: " + value.getDesc());
                        ToastUtil.shortToast(R.string.change_email_error_unknow_error);
                    }
                }
            }
        }, this, mPref.getUserID(), mPref.getUserKey(), newEmail);
    }
}