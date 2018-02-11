package com.aurora.iunivoice.activity.account;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.EmailUtil;
import com.aurora.iunivoice.utils.EncryptUtil;
import com.aurora.iunivoice.utils.FileLog;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.ToastUtil;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;

public class EmailVerificationActivity extends BaseAccountActivity implements OnClickListener {
	
    public static final String EXTRA_KEY_TITLE = "title"; // 标题
    public static final String EXTRA_KEY_MODE = "mode"; // 模式
    public static final int MODE_FOR_REGISTER = 1; // 注册用
    public static final int MODE_FOR_CHANGE_EMAIL = 2; // 修改绑定邮箱用
    public static final int MODE_FOR_VALIDATE_OLD_EMAIL = 3; // 修改绑定邮箱时验证旧邮箱
    
    public static final long CODE_REGET_TIME = 120;
    
	private final String TAG = "EmailVerificationActivity";
	private final int HANDLE_UPDATE_TIME = 100;
	
	public static final String EMAIL = "email";
	public static final String PWD = "pwd";

	private AccountManager mAccountManager;
	
	private TextView tv_email;
	private TextView tv_re_registration;
	private Button btn_re_send;
	private Button btn_check_finish;
	
	private String email = "";
	private String password = "";
	
	private long time = CODE_REGET_TIME;
	
	private String mTitle;
    private int mMode = MODE_FOR_REGISTER;
    
    private long mStopTime = -1; // 这个界面熄屏时的系统时间
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		getIntentData();
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_email_verification);
		
		setupViews();
			
		mAccountManager = new AccountManager(this);
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		enableBackItem(false);
		initViews();
		setListeners();	
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		
		if (!TextUtils.isEmpty(mTitle)) {
			setTitleText(mTitle);
		} else {
			setTitleRes(R.string.register_email_account_title);
		}

		if (mMode == MODE_FOR_CHANGE_EMAIL || mMode == MODE_FOR_VALIDATE_OLD_EMAIL) {
			addActionBarItem(getString(R.string.finish), ACTION_BAR_RIGHT_ITEM_ID);
		}
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		case ACTION_BAR_RIGHT_ITEM_ID:
			switch (mMode) {
			case MODE_FOR_REGISTER:
				Intent loginIntent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
				loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				loginIntent.putExtra(LoginActivity.EXTRA_KEY_ACCOUNT, email);
				loginIntent.putExtra(LoginActivity.EXTRA_KEY_PWD, password);
				loginIntent.putExtra(LoginActivity.EXTRA_KEY_NEED_AUTO_LOGIN, true);
				startActivity(loginIntent);
				break;
			case MODE_FOR_CHANGE_EMAIL:
			case MODE_FOR_VALIDATE_OLD_EMAIL:
				back();
                break;
			}
			break;
		}
	}

//	@Override
//	protected String getActionBarTitle() {
//	    if (!TextUtils.isEmpty(mTitle)) {
//            return mTitle;
//        }
//	    
//		return getString(R.string.register_email_account_title);
//	}

	private void initViews() {
		tv_email = (TextView) findViewById(R.id.tv_email);
		tv_re_registration = (TextView) findViewById(R.id.tv_re_registration);
		btn_re_send  = (Button) findViewById(R.id.btn_re_send);
		btn_check_finish = (Button) findViewById(R.id.btn_check_finish);
//		tv_email.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); 
//		tv_email.getPaint().setAntiAlias(true);
		
		if (mMode == MODE_FOR_CHANGE_EMAIL || mMode == MODE_FOR_VALIDATE_OLD_EMAIL) {
		    tv_re_registration.setVisibility(View.GONE);
		    btn_check_finish.setVisibility(View.GONE);
		} else {
			btn_check_finish.setVisibility(View.VISIBLE);
		    tv_re_registration.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); 
	        tv_re_registration.getPaint().setAntiAlias(true);
		}

		StringBuilder sb = new StringBuilder();
		if (mMode == MODE_FOR_REGISTER) {
		    sb.append(getString(R.string.register_please_visit_mailbox));
	        sb.append(email);
	        sb.append(Globals.LINE);
	        sb.append(getString(R.string.register_click_the_link));
		} else {
		    sb.append(getString(R.string.bind_email_success_tips, email));
		}
		String str = sb.toString();
		SpannableString emailString = new SpannableString(str);
		int startIndex = str.indexOf(email);
		emailString.setSpan(new EmailURLSpan("mailto://" + email, this), startIndex, startIndex + email.length(),
		        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		emailString.setSpan(new ForegroundColorSpan(getResources().
		        getColor(R.color.register_link_color)), startIndex, startIndex + email.length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_email.setText(emailString);
		tv_email.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	private static class EmailURLSpan extends URLSpan {
	    private WeakReference<Activity> mTarget;
	    
        public EmailURLSpan(String url, Activity target) {
            super(url);
            
            mTarget = new WeakReference<Activity>(target);
        }

        @Override
	    public void onClick(View widget) {
	        /*try {
	            super.onClick(widget);
	        } catch (ActivityNotFoundException e) {
	            ToastUtil.longToast(R.string.change_email_error_no_email_client);
	        } catch (Throwable t) {
	        }*/
            Activity target = mTarget.get();
            if (target != null) {
                EmailUtil.openEmailLoginPageByBrowser(target, getURL(),
                        target.getString(R.string.change_email_error_failed_to_open_browser));
            }
	    }
	}
	
	private void getIntentData() {
	    final Intent intent = getIntent();
		if (intent != null) {
			email = intent.getStringExtra(EMAIL);
			password = intent.getStringExtra(PWD);
			
			mTitle = intent.getStringExtra(EXTRA_KEY_TITLE);
            mMode = intent.getIntExtra(EXTRA_KEY_MODE, MODE_FOR_REGISTER);
		}
		
		Log.i(TAG, "email: " + email);
		Log.i(TAG, "password: " + password);
	}

	private void setListeners() {
		btn_re_send.setOnClickListener(this);
		btn_check_finish.setOnClickListener(this);
		tv_re_registration.setOnClickListener(this);
		if (mActionBarRightTv != null) {
		    mActionBarRightTv.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_re_send:
			getVerifyCode();
			break;
//		case R.id.right_tv:
//			switch (mMode) {
//			case MODE_FOR_REGISTER:
//				Intent loginIntent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
//				loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				loginIntent.putExtra(LoginActivity.EXTRA_KEY_ACCOUNT, email);
//				loginIntent.putExtra(LoginActivity.EXTRA_KEY_PWD, password);
//				loginIntent.putExtra(LoginActivity.EXTRA_KEY_NEED_AUTO_LOGIN, true);
//				startActivity(loginIntent);
//				break;
//			case MODE_FOR_CHANGE_EMAIL:
//			case MODE_FOR_VALIDATE_OLD_EMAIL:
//				back();
//                break;
//			}
//            break;
		case R.id.tv_re_registration:
		    Intent registerIntent = new Intent(EmailVerificationActivity.this, RegisterActivity.class);
            registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(registerIntent);
			break;
		case R.id.btn_check_finish:
			switch (mMode) {
			case MODE_FOR_REGISTER:
				checkEmailFinish(email, password);
				break;
			case MODE_FOR_CHANGE_EMAIL:
				back();
                break;
			}
			
			break;
		}
	}
	
	@Override
    public void onBackPressed() {
	    // 屏蔽物理返回
    }

    private void back() {
        Intent intent = new Intent(this, AccountInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Globals.EXTRA_COMMAND, Globals.COMMAND_REFRESH_EMAIL);
        startActivity(intent);
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
	
	@Override
    protected void onStart() {
        super.onStart();
        
        mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
    }
	
    @Override
    protected void onStop() {
        super.onStop();
        mStopTime = System.currentTimeMillis();
        mHandler.removeMessages(HANDLE_UPDATE_TIME);
    }

    private void updateTime() {
        if (mStopTime != -1) {
            time -= (System.currentTimeMillis() - mStopTime) / 1000;
            mStopTime = -1;
        } else {
            time--;
        }
		Log.d(TAG, "Jim, time: " + time);
		
		String timeStr = "";
		if (time <= 0) {
			timeStr = "";
		} else if (time < 10) {
			timeStr = "(0" + time + ")";
		} else {
			timeStr = "(" + time + ")";
		}
		
		btn_re_send.setText(getString(R.string.register_re_send_email_verify) + timeStr);
		
		if (time <= 0) {
			btn_re_send.setEnabled(true);
		} else {
			btn_re_send.setEnabled(false);
			
			mHandler.sendEmptyMessageDelayed(HANDLE_UPDATE_TIME, 1000);
		}
	}
	
	private void getVerifyCode() {
		showProgressDialog(getString(R.string.register_resending_email_verify));
		
		if (mMode == MODE_FOR_REGISTER) {
			mAccountManager.resendVerifyEmail(new DataResponse<BaseResponseObject>() {
	            public void run() {
	                dismissProgressDialog();
	                if (value != null) {
	                    Log.i(TAG, "the value=" + value.getCode());
	                    if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
	                        ToastUtil.shortToast(getString(R.string.register_get_email_verify_success));
	                        
	                        time = CODE_REGET_TIME;
	                        mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
	                    } else {
	                        Log.d(TAG, "Jim, error: " + value.getDesc());
	                        ToastUtil.shortToast(getString(R.string.register_get_email_verify_fail));
	                    }
	                }
	            }
	        }, this,"", "", email, "register");
		} else if (mMode == MODE_FOR_CHANGE_EMAIL) {
			mAccountManager.resendVerifyEmail(new DataResponse<BaseResponseObject>() {
	            public void run() {
	                dismissProgressDialog();
	                if (value != null) {
	                    Log.i(TAG, "the value=" + value.getCode());
	                    if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
	                        ToastUtil.shortToast(getString(R.string.register_get_email_verify_success));
	                        
	                        time = CODE_REGET_TIME;
	                        mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
	                    } else {
	                        Log.d(TAG, "Jim, error: " + value.getDesc());
	                        ToastUtil.shortToast(getString(R.string.register_get_email_verify_fail));
	                    }
	                }
	            }
	        }, this, mPref.getUserID(), mPref.getUserKey(), email, "bindemail");
		} else if (mMode == MODE_FOR_VALIDATE_OLD_EMAIL) {
		    mAccountManager.checkCurEmail(new DataResponse<BaseResponseObject>() {
	            public void run() {
	                dismissProgressDialog();
	                if (value != null) {
	                    Log.i(TAG, "the value=" + value.getCode());
                        if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
                            ToastUtil.shortToast(getString(R.string.register_get_email_verify_success));
                            
                            time = CODE_REGET_TIME;
                            mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME);
                        } else {
                            Log.d(TAG, "Jim, error: " + value.getDesc());
                            ToastUtil.shortToast(getString(R.string.register_get_email_verify_fail));
                        }
	                }
	            }
	        }, this, mPref.getUserID(), mPref.getUserKey(), email);
		}
	}
	
	private void checkEmailFinish(final String email, String pwd) {
	    showProgressDialog(getString(R.string.register_verifying_email));
	    mAccountManager.loginAccount(new DataResponse<UserLoginObject>() {
            public void run() {
            	dismissProgressDialog();
                if (value != null) {
                    final int code = value.getCode();
                     Log.i(TAG, "the resp code: " + code);
                     switch (code) {
                         case UserLoginObject.CODE_SUCCESS:
                    		 Intent loginIntent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
             				 loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             				 loginIntent.putExtra(LoginActivity.EXTRA_KEY_ACCOUNT, email);
             				 loginIntent.putExtra(LoginActivity.EXTRA_KEY_PWD, password);
             				 loginIntent.putExtra(LoginActivity.EXTRA_KEY_NEED_AUTO_LOGIN, true);
             				 startActivity(loginIntent);
             				 mPref.clearVercodeInfo();
							 break;
                         case UserLoginObject.CODE_ERROR_ACC_OR_PWD_ERROR:
                        	 Intent loginIntent2 = new Intent(EmailVerificationActivity.this, LoginActivity.class);
             				 loginIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             				 startActivity(loginIntent2);
                        	 break;
                         default:
                             ToastUtil.shortToast(getString(R.string.register_please_login_email_to_finish));
                             break;
                     }
                } else {
                    FileLog.e(TAG, "login resp, value is null");
                    ToastUtil.shortToast(getString(R.string.register_verifying_email_error));
                }
            }
        }, EmailVerificationActivity.this, email, EncryptUtil.getMD5Str(pwd), SystemUtils.getIMEI(), "",0);
	}

}