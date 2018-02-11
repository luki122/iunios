package com.aurora.account.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.util.EncryptUtil;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.widget.ClearableEditText;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.UserRegisterObject;
import com.aurora.datauiapi.data.bean.UserVC;
import com.aurora.datauiapi.data.bean.ValidateVCObject;
import com.aurora.datauiapi.data.implement.DataResponse;

public class PhoneNumVerificationActivity extends BaseActivity implements OnClickListener {

    public static final String EXTRA_KEY_TITLE = "title"; // 标题
    public static final String EXTRA_KEY_MODE = "mode"; // 模式
    public static final int MODE_FOR_REGISTER = 1; // 注册用
    public static final int MODE_FOR_CHANGE_PHONE = 2; // 修改绑定手机号用
    public static final int MODE_FOR_FIND_PWD = 3; // 找回密码用
    public static final int MODE_FOR_VALIDATE_OLD_PHONE = 4; // 修改绑定手机号时验证旧手机号
    
    public static final long CODE_REGET_TIME = 120;
            
	private final String TAG = "PhoneNumVerificationActivity";
	private final int HANDLE_UPDATE_TIME = 100;
	private final int FIRST_TIME_DATA = -10;
	
	public static final String PHONE_NUM = "phone_num";
	public static final String PWD = "pwd";
	public static final String COUNTRY_CODE = "country_code";
	public static final String VALIDCODE = "validCode";

	private TextView tv_tips;
	private ClearableEditText cet_verification_code;
	private RelativeLayout rl_get_verifycode;
	private TextView tv_time;
//	private TextView tv_agreement_tips;
	private LinearLayout ll_agreement_tips;
	private TextView tv_user_agreement;
//	private Button btn_next;
	
	private AccountManager mAccountManager;
	
	private String phoneNum = "";
	private String password = "";
	private String countryCode = "";
	private String vcId = "";
	private long time = FIRST_TIME_DATA;
	private String mValidCode = "";
	
	private String mTitle;
	private int mMode = MODE_FOR_REGISTER;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.activity_phonenum_verification,
				AuroraActionBar.Type.Normal, true);
		
		getIntentData();
		initViews();
		configActionBar(getString(R.string.register_next));
		setListeners();
		initData();
		
		mAccountManager = new AccountManager(this);
	}

	@Override
	protected String getActionBarTitle() {
	    if (!TextUtils.isEmpty(mTitle)) {
	        return mTitle;
	    }
		return getString(R.string.register_verify_phonenum_title);
	}

    @Override
	protected void onResume() {
		super.onResume();
		
		long num_vercode_last_time = mPref.getVercodeLastSendTime(phoneNum);
		int num_vercode_last_model = mPref.getVercodeLastMode(phoneNum);
		
		if (num_vercode_last_model == mMode) {
			if (System.currentTimeMillis() - num_vercode_last_time > CODE_REGET_TIME * 1000) {
				if (time == FIRST_TIME_DATA) {
					time = CODE_REGET_TIME;
				} else {
					time = 0;
				}
			} else {
				time = CODE_REGET_TIME - ((System.currentTimeMillis() - num_vercode_last_time) / 1000);
			}
		} else {
			if (time == FIRST_TIME_DATA) {
				time = CODE_REGET_TIME;
			}
		}
		
		mHandler.removeMessages(HANDLE_UPDATE_TIME);
		mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
	}

	private void initViews() {
		tv_tips = (TextView) findViewById(R.id.tv_tips);
		cet_verification_code = (ClearableEditText) findViewById(R.id.cet_verification_code);
		rl_get_verifycode = (RelativeLayout) findViewById(R.id.rl_get_verifycode);
		tv_time = (TextView) findViewById(R.id.tv_time);
		ll_agreement_tips = (LinearLayout) findViewById(R.id.ll_agreement_tips);
		tv_user_agreement = (TextView) findViewById(R.id.tv_agreement_tips2);
//		btn_next = (Button) findViewById(R.id.btn_next);
		
		String phoneStr = phoneNum;
		if (!TextUtils.isEmpty(countryCode)) {
		    phoneStr = addPlusToCode(countryCode) + " " + phoneStr;
		}
		tv_tips.setText(getString(R.string.register_verify_phonenum_tips, phoneStr));
		
		if (mMode != MODE_FOR_REGISTER) {
			ll_agreement_tips.setVisibility(View.GONE);
		}
		
		tv_user_agreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); 
		tv_user_agreement.getPaint().setAntiAlias(true);
		
		initErrorViews();
	}
	
	private void getIntentData() {
	    final Intent intent = getIntent();
		if (intent != null) {
			phoneNum = intent.getStringExtra(PHONE_NUM);
			password = intent.getStringExtra(PWD);
			countryCode = intent.getStringExtra(COUNTRY_CODE);
			mTitle = intent.getStringExtra(EXTRA_KEY_TITLE);
	        mMode = intent.getIntExtra(EXTRA_KEY_MODE, MODE_FOR_REGISTER);
	        mValidCode = intent.getStringExtra(VALIDCODE);
		}
		
		Log.i(TAG, "phoneNum: " + phoneNum);
		Log.i(TAG, "password: " + password);
		Log.i(TAG, "countryCode: " + countryCode);
		Log.i(TAG, "vcId: " + vcId);
	}

	private void setListeners() {
		rl_get_verifycode.setOnClickListener(this);
//		btn_next.setOnClickListener(this);
		setListenerForErrorView(cet_verification_code);
		if (mActionBarRightTv != null) {
		    mActionBarRightTv.setOnClickListener(this);
		}
		tv_user_agreement.setOnClickListener(this);
	}
	
	private void initData() {
//		time = REGET_TIME;
//		mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_get_verifycode:
			getVerifyCode();
			break;
//		case R.id.btn_next:
//			if (checkVerifyCode()) {
//				switch (mMode) {
//				case MODE_FOR_REGISTER:
//					doRegister();
//					break;
//				case MODE_FOR_CHANGE_PHONE:
//					doChangePhone();
//					break;
//				case MODE_FOR_FIND_PWD:
//					doFindPwd();
//					break;
//				}
//			}
		case R.id.right_tv:
			if (checkVerifyCode()) {
				switch (mMode) {
				case MODE_FOR_REGISTER:
					doRegister();
					break;
				case MODE_FOR_CHANGE_PHONE:
					doChangePhone();
					break;
				case MODE_FOR_FIND_PWD:
					doFindPwd();
					break;
				case MODE_FOR_VALIDATE_OLD_PHONE:
				    doValidateOldPhone();
				    break;
				}
			}
			break;
		case R.id.tv_agreement_tips2:
			Intent i = new Intent(PhoneNumVerificationActivity.this, UserAgreementActivity.class);
			startActivity(i);
			break;
		}
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		
		switch (msg.what) {
		case HANDLE_UPDATE_TIME:
			updateTime();
			break;
		}
	}

	private String addPlusToCode(String code) {
		try {
			int codeInt = Integer.parseInt(code);
			if (codeInt > 0) {
				return "+" + codeInt;
			}
		} catch (NumberFormatException e) {
		}
		
		return code;
	}
	
	private void updateTime() {
		time--;
		
		if (time <= 0) {
			rl_get_verifycode.setEnabled(true);
			tv_time.setVisibility(View.GONE);
		} else {
			rl_get_verifycode.setEnabled(false);
			tv_time.setVisibility(View.VISIBLE);
			
			if (time < 10) {
				tv_time.setText("(0" + time + ")");
			} else {
				tv_time.setText("(" + time + ")");
			}
			
			mHandler.sendEmptyMessageDelayed(HANDLE_UPDATE_TIME, 1000);
		}
	}
	
	private void getVerifyCode() {
		showProgressDialog(getString(R.string.register_getting_verify_code));
		if (mMode == MODE_FOR_VALIDATE_OLD_PHONE) {
		    mAccountManager.checkCurPhone(new DataResponse<BaseResponseObject>() {
	            public void run() {
	                dismissProgressDialog();
	                if (value != null) {
	                    final int code = value.getCode();
	                     Log.i(TAG, "Jim, the value=" + code);
	                     
	                     switch (code) {
	                         case BaseResponseObject.CODE_SUCCESS:
	                             mPref.recordLastSendTime(phoneNum, mMode);
	                             ToastUtil.shortToast(getString(R.string.register_get_verify_code_success));
	                             
	                             time = CODE_REGET_TIME;
	                             mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
	                             break;
	                         case BaseResponseObject.CODE_ERROR_VC_INVALID_PLEASE_BACK:
	                             ToastUtil.shortToast(getString(R.string.validate_error_vc_invalid_please_back));
	                        	 break;
	                         default:
	                             Log.d(TAG, "Jim, error: " + value.getDesc());
	                             ToastUtil.shortToast(getString(R.string.register_get_verify_code_fail));
	                             break;
	                     }
	                }
	            }
	        }, this, mPref.getUserID(), mPref.getUserKey(), phoneNum, EncryptUtil.getMD5Str(password));
		} else {
		    String event = "register";
//	        String validCode = null;
	        switch (mMode) {
	            case MODE_FOR_CHANGE_PHONE:
	                event = "bindphone";
	                break;
	            case MODE_FOR_FIND_PWD:
	                event = "findpwd";
//	                validCode = mValidCode;
	                break;
	        }
	        mAccountManager.getVerifyCode(new DataResponse<UserVC>() {
	            public void run() {
	                if (value != null) {
	                    Log.i(TAG, "the value=" + value.getCode());
	                    
	                    dismissProgressDialog();
	                    
	                    if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
	                        mPref.recordLastSendTime(phoneNum, mMode);
//	                        if (mMode == MODE_FOR_REGISTER || mMode == MODE_FOR_FIND_PWD) {
//	                            mPref.recordLastSendTime(phoneNum);
//	                            mPref.recordLastSendTimeMode(phoneNum, mMode);
//	                        }
	                        ToastUtil.shortToast(getString(R.string.register_get_verify_code_success));
	                        time = CODE_REGET_TIME;
	                        mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
	                    } else if (value.getCode() == UserVC.CODE_ERROR_CHECK_INPUT_ERROR) {
							ToastUtil.shortToast(getString(R.string.findpwd_error_checkcode_input_error));
						} else if (value.getCode() == BaseResponseObject.CODE_ERROR_VC_INVALID_PLEASE_BACK) {
							ToastUtil.shortToast(getString(R.string.validate_error_vc_invalid_please_back));
						} else {
	                        Log.d(TAG, "Jim, error: " + value.getDesc());
	                        ToastUtil.shortToast(getString(R.string.register_get_verify_code_fail));
	                    }
	                }
	            }
	        }, PhoneNumVerificationActivity.this, mPref.getUserID(), mPref.getUserKey(),
	            phoneNum, event, mValidCode, SystemUtils.getIMEI());
		}
	}
	
	private boolean checkVerifyCode() {
		String verifyCode = cet_verification_code.getText().toString().trim();
		
		if (TextUtils.isEmpty(verifyCode)) {
			showErrorInfo(findViewById(R.id.cet_verification_code_ly), cet_verification_code,
			        getResources().getString(R.string.register_error_verifycode_cannot_be_empty));
			return false;
		}
		
		return true;
	}
	
	private void doRegister() {
		String verifyCode = cet_verification_code.getText().toString().trim();
		
		showProgressDialog(getString(R.string.register_is_being_registered));
		
		mAccountManager.registerAccount(new DataResponse<UserRegisterObject>() {
			@Override
			public void run() {
				Log.i(TAG, "the value=" + value.getCode());
				
				dismissProgressDialog();
				
				if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
					ToastUtil.shortToast(getString(R.string.register_success));
					
					Intent intent = new Intent(PhoneNumVerificationActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(LoginActivity.EXTRA_KEY_ACCOUNT, phoneNum);
					intent.putExtra(LoginActivity.EXTRA_KEY_PWD, password);
					intent.putExtra(LoginActivity.EXTRA_KEY_NEED_AUTO_LOGIN, true);
					startActivity(intent);
					mPref.clearVercodeInfo();
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_VERCODE_ERROR) {
					ToastUtil.shortToast(getString(R.string.register_error_ercode_error));
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_PHONENUM_ALREADY_REGISTER) {
					ToastUtil.shortToast(getString(R.string.register_error_phonenum_already_register));
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_EMAIL_ALREADY_REGISTER) {
					ToastUtil.shortToast(getString(R.string.register_error_email_already_register));
				} else if (value.getCode() == UserRegisterObject.CODE_ERROR_SEND_MSG_FREQUENT) { 
					ToastUtil.shortToast(getString(R.string.register_code_error_send_msg_frequent));
				} else {
					Log.i(TAG, "call registerAccount: " + value.getDesc());
					ToastUtil.shortToast(getString(R.string.register_fail));
				}
			}
		}, PhoneNumVerificationActivity.this, phoneNum, null, password,
			EncryptUtil.getMD5Str(password), SystemUtils.getIMEI(), verifyCode, vcId, mValidCode);
	}
	
	private void doChangePhone() {
	    String verifyCode = cet_verification_code.getText().toString().trim();
        showProgressDialog(getString(R.string.set_nick_saving));
        
        mAccountManager.changePhoneNo(new DataResponse<BaseResponseObject>() {
            @Override
            public void run() {
                Log.i(TAG, "the value=" + value.getCode());
                
                dismissProgressDialog();
                
                if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
                    ToastUtil.shortToast(getString(R.string.update_success));
                    
                    Intent intent = new Intent(PhoneNumVerificationActivity.this, BindSuccessActivity.class);
                    intent.putExtra(BindSuccessActivity.PHONE_NUM, phoneNum);
                    startActivity(intent);
                    mPref.clearVercodeInfo();
                } else {
					ToastUtil.shortToast(value.getDesc());
				}
            }
        }, PhoneNumVerificationActivity.this, mPref.getUserID(), mPref.getUserKey(), phoneNum, verifyCode, countryCode, vcId);
	}
	
	private void doValidateOldPhone() {        
        String verifyCode = cet_verification_code.getText().toString().trim();
        showProgressDialog(getString(R.string.change_phone_validating_vc));
        mAccountManager.validateChgPhoneVc(new DataResponse<BaseResponseObject>() {
            @Override
            public void run() {
                Log.i(TAG, "the value=" + value.getCode());
                
                dismissProgressDialog();
                
                if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
                    Intent intent = new Intent(PhoneNumVerificationActivity.this, ChangePhoneNumActivity.class);
                    intent.putExtra(ChangePhoneNumActivity.EXTRA_KEY_MODE, ChangePhoneNumActivity.MODE_REBIND);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Jim, error: " +  value.getDesc());
                    ToastUtil.shortToast(R.string.register_error_ercode_error);
                }
            }
        }, PhoneNumVerificationActivity.this, mPref.getUserID(), mPref.getUserKey(), verifyCode);
	}
	
	private void doFindPwd() {
		String verifyCode = cet_verification_code.getText().toString().trim();
        showProgressDialog(getString(R.string.findpwd_validating_code));
        
        mAccountManager.validateFindpwdVc(new DataResponse<ValidateVCObject>() {
            @Override
            public void run() {
                Log.i(TAG, "the value=" + value.getCode());
                
                dismissProgressDialog();
                
                if (value.getCode() == ValidateVCObject.CODE_SUCCESS) {
                	String verifyCode = cet_verification_code.getText().toString().trim();
            		Intent intent = new Intent(PhoneNumVerificationActivity.this, ResetPasswordActivity.class);
            		intent.putExtra(ResetPasswordActivity.PHONE_NUM, phoneNum);
            		intent.putExtra(ResetPasswordActivity.VERIFY_CODE, verifyCode);
            		intent.putExtra(ResetPasswordActivity.VCID, vcId);
            	    startActivity(intent);
                } else if (value.getCode() == ValidateVCObject.CODE_ERROR_IMEI_ISEMPTY) {
                	ToastUtil.shortToast(getString(R.string.findpwd_error_imei_isempty));
				} else if (value.getCode() == ValidateVCObject.CODE_ERROR_PHONE_VERIFYCODE_WRONG) {
					ToastUtil.shortToast(getString(R.string.findpwd_phone_verifycode_wrong));
				} else {
					ToastUtil.shortToast(getString(R.string.findpwd_error_validate_code_fail));
				}
            }
        }, PhoneNumVerificationActivity.this, SystemUtils.getIMEI(), verifyCode);
	}

}
