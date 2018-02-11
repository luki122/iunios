package com.aurora.community.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aurora.community.R;
import com.aurora.community.activity.account.VerifyCodeLoader.VC_EVENT;
import com.aurora.community.utils.BooleanPreferencesUtil;
import com.aurora.community.utils.EncryptUtil;
import com.aurora.community.utils.FileLog;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ToastUtil;
import com.aurora.community.utils.ValidateUtil;
import com.aurora.community.widget.OnSizeChangeLinearLayout;
import com.aurora.community.widget.OnSizeChangeLinearLayout.OnSizeChangedListener;
import com.aurora.community.widget.VerifyCodeView;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;

/**
 * 登录界面
 * 
 * @author JimXia
 *
 * @date 2014-9-30 上午11:12:37
 */
public class LoginActivity extends BaseAccountActivity implements OnClickListener {

	public static final String EXTRA_KEY_ACCOUNT = "account";
	public static final String EXTRA_KEY_PWD = "password";
	public static final String EXTRA_KEY_NEED_AUTO_LOGIN = "needAutoLogin";

	private EditText mAccountEt;
	private EditText mPwdEt;
	private Button mLoginBtn;
	private LinearLayout mVcCodeLy;
	// private LinearLayout mDescLy;
	private VerifyCodeView mVCIv;
	private EditText mVCCodeEt;

	private AccountManager mAccountManager;
	
	private boolean mNeedExitAnim = false; // 是否需要设置activity的退出动画

	private OnSizeChangeLinearLayout mRootLy;
	
	//给其它模块调用类型  0 是正常  1 电话邦
	private int type  = 0;
	
	// private ImageView mLogoIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login_activity);
		// SignatureUtil.getSignCode(this, getPackageName());
		
//		mNeedExitAnim = getIntent().getBooleanExtra(
//                SyncAccountActivity.EXTRA_KEY_NEED_SET_EXIT_ANIMATION, false);
		type = getIntent().getIntExtra("type", 0);
		Log.i("zhangwei", "zhangwei the type ="+type);
		if (BooleanPreferencesUtil.getInstance(this).hasLogin()) {
			finish();
			return;
		}

		setupViews();

		mAccountManager = new AccountManager(this);
		handleLoginRequest(getIntent());
		
	/*	TotalCount tl = new TotalCount(this, "1", "1", 1);
		tl.CountData();*/

	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		setListeners();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.login);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		clearContentFocus();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleLoginRequest(intent);
	}

	/**
	 * 避免打开登录界面就弹出键盘
	 */
	private void clearContentFocus() {
		View rootView = mRootLy;
		rootView.setFocusable(true);
		rootView.setFocusableInTouchMode(true);
		rootView.requestFocus();
	}

	private boolean handleLoginRequest(Intent intent) {
		String account = intent.getStringExtra(EXTRA_KEY_ACCOUNT);
		String pwd = intent.getStringExtra(EXTRA_KEY_PWD);
		boolean needAutoLogin = intent.getBooleanExtra(
				EXTRA_KEY_NEED_AUTO_LOGIN, false);

		if (!TextUtils.isEmpty(account)) {
			mAccountEt.setText(account);
		}

		if (!TextUtils.isEmpty(pwd)) {
			mPwdEt.setText(pwd);
		}

		if (needAutoLogin) {
			mLoginBtn.performClick();
		}

		return needAutoLogin;
	}

	private void initViews() {
		mAccountEt = (EditText) findViewById(R.id.account_cet);
		// mAccountEt.setIsNeedDeleteAll(true);
		mPwdEt = (EditText) findViewById(R.id.password_cet);
		// mPwdEt.setIsNeedDeleteAll(true);
		mLoginBtn = (Button) findViewById(R.id.login_btn);
		mVcCodeLy = (LinearLayout) findViewById(R.id.vc_code_ly);
		// mDescLy = (LinearLayout) findViewById(R.id.desc_ly);
		mVCIv = (VerifyCodeView) findViewById(R.id.vc_code_iv);
		mVCIv.setVCEvent(VC_EVENT.VC_EVENT_LOGIN);
		mVCCodeEt = (EditText) findViewById(R.id.vc_code_cet);
		initErrorViews();

		mRootLy = (OnSizeChangeLinearLayout) findViewById(R.id.root_ly);
		// mLogoIv = (ImageView) findViewById(R.id.logo_iv);

		String account = mPref.getUserPhone();
		if (TextUtils.isEmpty(account)) {
			account = mPref.getUserEmail();
		}
		if (!TextUtils.isEmpty(account)) {
			mAccountEt.setText(account);
		}
		mAccountEt.setSelection(mAccountEt.getText().toString().length());
	}

	private void setListeners() {
		mLoginBtn.setOnClickListener(this);

		View v = findViewById(R.id.register_tv);
		v.setOnClickListener(this);

		v = findViewById(R.id.find_password_tv);
		v.setOnClickListener(this);

		setListenerForErrorView(mAccountEt);
		setListenerForErrorView(mPwdEt);

		mRootLy.setOnSizeChangedListener(new OnSizeChangedListener() {
			private static final long DELAY = 200;
			private static final int VIRTUAL_BAR_HEIGHT = 160; // U3虚拟条的高度，估计的

			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				Log.d(TAG, "Jim, w: " + w + ", h: " + h + ", oldw: " + oldw
						+ ", oldh: " + oldh);
				if (h < oldh && h < oldh - VIRTUAL_BAR_HEIGHT) {
					
				} else if (h > oldh + VIRTUAL_BAR_HEIGHT) {
					
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn:
			doLogin();
			break;
		case R.id.register_tv:
			doRegister();
			break;
		case R.id.find_password_tv:
			doFindPwd();
			break;
		default:
			break;
		}
	}

	private void doRegister() {
		Intent registerIntent = new Intent(this, RegisterActivity.class);
		startActivity(registerIntent);
	}

	private void doFindPwd() {
		Intent findpwdIntent = new Intent(this, FindPwdActivity.class);
		startActivity(findpwdIntent);
	}

	private void doLogin() {
		String account = mAccountEt.getText().toString()/* .trim() */;
		if (TextUtils.isEmpty(account)) {
			// showErrorTips(mAccountEt,
			// getResources().getString(R.string.login_error_account_cannot_be_empty));
			showErrorInfo(
					mAccountEt,
					mAccountEt,
					getResources().getString(
							R.string.login_error_account_cannot_be_empty));
			return;
		}

		if (!ValidateUtil.isAccountValid(account)) {
			showErrorInfo(
					mAccountEt,
					mAccountEt,
					getResources().getString(
							R.string.login_error_account_format_invalid));
			// showErrorTips(mAccountEt,
			// getResources().getString(R.string.login_error_account_format_invalid));
			return;
		}

		String pwd = mPwdEt.getText().toString()/* .trim() */;
		if (TextUtils.isEmpty(pwd)) {
			showErrorInfo(
					mPwdEt,
					mPwdEt,
					getResources().getString(
							R.string.login_error_pwd_cannot_be_empty));
			// showErrorTips(mPwdEt,
			// getResources().getString(R.string.login_error_pwd_cannot_be_empty));
			return;
		}

		String validCode = "";
		if (mVcCodeLy.getVisibility() == View.VISIBLE) {
			validCode = mVCCodeEt.getText().toString().trim();
			if (TextUtils.isEmpty(validCode)) {
				showErrorInfo(
						mVCCodeEt,
						mVCCodeEt,
						getResources().getString(
								R.string.login_error_vccode_cannot_be_empty));
				return;
			}
		}

		validateByServer(account, pwd, validCode);
	}

	private void validateByServer(final String account, final String pwd,
			String validCode) {
		showProgressDialog(getString(R.string.login_on_logining));
		final BooleanPreferencesUtil bPref = BooleanPreferencesUtil
				.getInstance(this);
		mAccountManager.loginAccount(new DataResponse<UserLoginObject>() {
			public void run() {
				dismissProgressDialog();
				if (value != null) {
					final int code = value.getCode();
					Log.i(TAG, "the resp code: " + code);
					switch (code) {
					case UserLoginObject.CODE_SUCCESS:
						bPref.setLogin(true);
						
						Intent b = new Intent(Globals.LOCAL_LOGIN_ACTION);
						b.putExtra(Globals.LOCAL_LOGIN_RESULT, Globals.LOCAL_LOGIN_SUCCESS);
						sendBroadcast(b);
						
						finish();
							
						return;
					case UserLoginObject.CODE_ERROR_ACC_OR_PWD_ERROR:
						ToastUtil
								.shortToast(R.string.login_error_acc_or_pwd_incorrect);
						break;
					case UserLoginObject.CODE_ERROR_EMAIL_ACC_NOT_ACTIVE:

						ToastUtil.shortToast(R.string.login_error_email_acc_not_active);
						Intent intent = new Intent(LoginActivity.this,
								EmailVerificationActivity.class);
						intent.putExtra(EmailVerificationActivity.EMAIL, account);
						intent.putExtra(EmailVerificationActivity.PWD, pwd);
						startActivity(intent);
						break;
					case UserLoginObject.CODE_ERROR_PWD_ERROR_3_TIMES:
						// 密码输错3次，需要输入验证码
						if (mVcCodeLy.getVisibility() != View.VISIBLE) {
							mVcCodeLy.setVisibility(View.VISIBLE);
//							mTopRl.setVisibility(View.GONE);
							// mDescLy.setVisibility(View.GONE);
							// mLogoIv.setVisibility(View.GONE);
						}
						mVCIv.refresh();
						mVCCodeEt.setText("");
						ToastUtil
								.shortToast(R.string.login_error_acc_or_pwd_incorrect);
						break;
					case UserLoginObject.CODE_ERROR_VC_CODE_ERROR:
						if (mVcCodeLy.getVisibility() != View.VISIBLE) {
							mVcCodeLy.setVisibility(View.VISIBLE);
//							mTopRl.setVisibility(View.GONE);
							// mDescLy.setVisibility(View.GONE);
							// mLogoIv.setVisibility(View.GONE);
							
							mVCIv.refresh();
							mVCCodeEt.requestFocus();
							ToastUtil
									.shortToast(R.string.login_error_vccode_cannot_be_empty);
						} else {
							mVCIv.refresh();
							mVCCodeEt.setText("");
							mVCCodeEt.requestFocus();
							ToastUtil
									.shortToast(R.string.login_error_vc_code_incorrect);
						}
						break;
					case UserLoginObject.CODE_ERROR_MISSING_IMEI:
						Log.e(TAG, "Jim, missing imei!");
					default:
						ToastUtil.shortToast(R.string.login_error_unknow);
						break;
					}
				} else {
					FileLog.e(TAG, "login resp, value is null");
					ToastUtil.shortToast(R.string.login_error_unknow);
				}
			}
		}, LoginActivity.this, account, EncryptUtil.getMD5Str(pwd), SystemUtils
				.getIMEI(), validCode,type);
	}

	private void openMainActivity(int loginStatus) {
		Log.d(TAG, "Jim, loginStatus: " + loginStatus);
//		Intent intent = new Intent(this, SyncAccountActivity.class);
//		intent.putExtra(SyncAccountActivity.EXTRA_KEY_LOGIN_STATUS, loginStatus);
//		intent.putExtra(SyncAccountActivity.EXTRA_KEY_DIS_WARN, isDisWarn);
//		intent.putExtra(SyncAccountActivity.EXTRA_KEY_NEED_SET_EXIT_ANIMATION, mNeedExitAnim);
//
//		startActivity(intent);
//		finish();
	}

	@Override
	public void finish() {
		super.finish();
		
//		if (mNeedExitAnim) {
//		    overridePendingTransition(
//	                com.aurora.R.anim.aurora_activity_close_enter,
//	                com.aurora.R.anim.aurora_activity_close_exit);
//		}
	}
	
}