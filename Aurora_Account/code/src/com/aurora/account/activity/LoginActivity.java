package com.aurora.account.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.activity.VerifyCodeLoader.VC_EVENT;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.contentprovider.AccountsAdapter;
import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.CustomAnimCallBack;
import com.aurora.account.util.CustomAnimation;
import com.aurora.account.util.EncryptUtil;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.util.ValidateUtil;
import com.aurora.account.widget.OnSizeChangeLinearLayout;
import com.aurora.account.widget.OnSizeChangeLinearLayout.OnSizeChangedListener;
import com.aurora.account.widget.VerifyCodeView;
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
public class LoginActivity extends BaseActivity implements OnClickListener {

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
	private int mLoginStatus = SyncAccountActivity.LOGIN_STATUS_NORMAL;
	// 是否显示提醒联系人同步
	private boolean isDisWarn = false;
	
	private boolean mNeedExitAnim = false; // 是否需要设置activity的退出动画

	private OnSizeChangeLinearLayout mRootLy;
	private RelativeLayout mTopRl;
	
	//给其它模块调用类型  0 是正常  1 电话邦
	private int type  = 0;

	// private ImageView mLogoIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.login_activity,
				AuroraActionBar.Type.Empty, false);
		// SignatureUtil.getSignCode(this, getPackageName());
		File fl = new File(Environment.getExternalStorageDirectory()
				+ "/accounttest1234567890");
		if (fl.isDirectory()) {
			if (fl.listFiles().length == 1) {
				Globals.HTTP_REQUEST_URL = "http://" + fl.list()[0].toString()
						+ "/account";

				Globals.HTTPS_REQUEST_URL = "https://"
						+ fl.list()[0].toString() + "/account";
			} else {
				Globals.HTTP_REQUEST_URL = Globals.HTTP_REQUEST_TEST_URL;
				Globals.HTTPS_REQUEST_URL = Globals.HTTPS_REQUEST_TEST_URL;
			}
		} else {
			Globals.HTTP_REQUEST_URL = Globals.HTTP_REQUEST_DEFAULT_URL;
			Globals.HTTPS_REQUEST_URL = Globals.HTTPS_REQUEST_DEFAULT_URL;
		}
		isDisWarn = getIntent().getBooleanExtra(
				SyncAccountActivity.EXTRA_KEY_DIS_WARN, false);
		mNeedExitAnim = getIntent().getBooleanExtra(
                SyncAccountActivity.EXTRA_KEY_NEED_SET_EXIT_ANIMATION, false);
		type = getIntent().getIntExtra("type", 0);
		Log.i("zhangwei", "zhangwei the type ="+type);
		if (BooleanPreferencesUtil.getInstance(this).hasLogin()) {
			if(type == 0)
				openMainActivity(mLoginStatus);
			else
				finish();
			
			return;
		}

		initViews();
		setListeners();

		mAccountManager = new AccountManager(this);
		handleLoginRequest(getIntent());
		
	/*	TotalCount tl = new TotalCount(this, "1", "1", 1);
		tl.CountData();*/

	}

	@Override
	protected void onResume() {
		super.onResume();
		clearContentFocus();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (BooleanPreferencesUtil.getInstance(this).hasLogin()) {
			Log.i(TAG, "LoginActivity onNewIntent hasLogin!");
			if (type == 0)
				openMainActivity(mLoginStatus);
			else
				finish();
			return;
		}
		
		type = getIntent().getIntExtra("type", 0);
		handleLoginRequest(intent);
		Log.i("zhangwei", "zhangwei onNewIntent");
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
		mTopRl = (RelativeLayout) findViewById(R.id.top_rl);
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
			private static final long START_UP = 150;
			private static final long START_ALPHA_TIME = 30;
			private static final long END_DOWN = 200;
			private static final long END_ALPHA_TIME = 50;
			private static final int VIRTUAL_BAR_HEIGHT = 160; // U3虚拟条的高度，估计的
			
			private int height = 0;
			private boolean isFirst = true;

			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				Log.d(TAG, "Jim, w: " + w + ", h: " + h + ", oldw: " + oldw
						+ ", oldh: " + oldh);
				if (isFirst) {
					isFirst = false;
					return;
				}
				if (height == 0) {
					height = mTopRl.getHeight();
				}
				if (h < oldh && h < oldh - VIRTUAL_BAR_HEIGHT) {
					if (mTopRl.getVisibility() == View.VISIBLE) {
						AnimationSet animationSet = new AnimationSet(true);
						
						CustomAnimation animation = new CustomAnimation(new CustomAnimCallBack() {
							@Override
							public void callBack(float interpolatedTime, Transformation t) {
								
								mTopRl.setPadding(0, -(int)(interpolatedTime * height), 0, 0);
							}
						});
						animation.setDuration(START_UP);
						
						CustomAnimation alphaAnimation = new CustomAnimation(new CustomAnimCallBack() {
							@Override
							public void callBack(float interpolatedTime, Transformation t) {
								
								mTopRl.setAlpha((1 - interpolatedTime));
							}
						});
						alphaAnimation.setDuration(START_ALPHA_TIME);
						
						animationSet.addAnimation(animation);
						animationSet.addAnimation(alphaAnimation);
						animationSet.setInterpolator(new DecelerateInterpolator());
						
						mTopRl.startAnimation(animationSet);
						
//						mRootLy.postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								mTopRl.setVisibility(View.GONE);
//							}
//						}, DELAY);
					}
				} else if (h > oldh + VIRTUAL_BAR_HEIGHT) {
					if (mVcCodeLy.getVisibility() != View.VISIBLE) {
						AnimationSet animationSet = new AnimationSet(true);
						
						CustomAnimation animation = new CustomAnimation(new CustomAnimCallBack() {
							@Override
							public void callBack(float interpolatedTime, Transformation t) {
								
								mTopRl.setPadding(0, -(int)((1 - interpolatedTime) * height), 0, 0);
							}
						});
						animation.setDuration(END_DOWN);
						
						CustomAnimation alphaAnimation = new CustomAnimation(new CustomAnimCallBack() {
							@Override
							public void callBack(float interpolatedTime, Transformation t) {
								
								mTopRl.setAlpha(interpolatedTime);
							}
						});
						alphaAnimation.setDuration(END_ALPHA_TIME);
						
						animationSet.addAnimation(animation);
						animationSet.addAnimation(alphaAnimation);
//						animationSet.setInterpolator(new AccelerateInterpolator());
						
						mTopRl.startAnimation(animationSet);
						
						
//						mRootLy.postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								mTopRl.setVisibility(View.VISIBLE);
//							}
//						}, DELAY);
					}
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
		final String oldUserId = mPref.getUserID();
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
						
						if(type == 1)
						{
							setResult(RESULT_OK);
							finish();
							return;
						}
						String newUserId = mPref.getUserID();
						if (!TextUtils.isEmpty(oldUserId)
								/*&& !oldUserId.equals(newUserId)*/) {
						    if (!oldUserId.equals(newUserId)) {
						        // 切换帐号登录
	                            mLoginStatus = SyncAccountActivity.LOGIN_STATUS_CHANGE_ACCOUNT_FIRST_LOGIN;
	                            confirmClear();
	                            
	                            // 切换账号如果此账户未绑定百度云，关掉相册备份开关
	                            if (!CommonUtil.checkPhotoBackupHasBind(LoginActivity.this)) {
	                            	List<AppConfigInfo> list = SystemUtils.getAppConfigInfo(LoginActivity.this);
	                            	for (AppConfigInfo info : list) {
	                            		if (info.getApp_packagename().equals(Globals.GALLERY_PACKAGE_NAME)) {
	                            			if (info.isSync()) {
	                            				SystemUtils.updateAppConfigInfo(LoginActivity.this,
	                            						info.getApp_packagename(), false);
	                            				CommonUtil.checkAndSetAppBackupAlarm();
	                            			}
	                            			break;
	                            		}
	                            	}
	                            }
						    } else {
						    	ExtraFileUpService.resetData();
						        mLoginStatus = SyncAccountActivity.LOGIN_STATUS_RELOGIN;
						        openMainActivity(mLoginStatus);
						    }
						} else {
							if (bPref.isFirstTimeLogin()) {
								mLoginStatus = SyncAccountActivity.LOGIN_STATUS_FIRST_LOGIN;
								bPref.setFirstTimeLogin(false);
							}
							if (!bPref.hasShownSyncSettingGuide()) {
								bPref.setShownSyncSettingGuide(true);
								Intent intent = new Intent(LoginActivity.this,
										SyncSettingGuide2Activity.class);
								intent.putExtra(
										SyncSettingGuide2Activity.EXTRA_KEY_LOGIN_STATUS,
										mLoginStatus);
								startActivity(intent);
								finish();
							} else {
								openMainActivity(mLoginStatus);
							}
						}
						return;
					case UserLoginObject.CODE_ERROR_ACC_OR_PWD_ERROR:
						ToastUtil
								.shortToast(R.string.login_error_acc_or_pwd_incorrect);
						break;
					case UserLoginObject.CODE_ERROR_EMAIL_ACC_NOT_ACTIVE:

						ToastUtil
								.shortToast(R.string.login_error_email_acc_not_active);
						Intent intent = new Intent(LoginActivity.this,
								EmailVerificationActivity.class);
						intent.putExtra(EmailVerificationActivity.EMAIL,
								account);
						intent.putExtra(EmailVerificationActivity.PWD, pwd);
						startActivity(intent);
						break;
					case UserLoginObject.CODE_ERROR_PWD_ERROR_3_TIMES:
						// 密码输错3次，需要输入验证码
						if (mVcCodeLy.getVisibility() != View.VISIBLE) {
							mVcCodeLy.setVisibility(View.VISIBLE);
							mTopRl.setVisibility(View.GONE);
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
							mTopRl.setVisibility(View.GONE);
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

	private void confirmClear() {
		new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.dialog_prompt)
				.setMessage(R.string.login_confirm_reserve_local_data)
				.setPositiveButton(R.string.login_confirm_reserve,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								performClear(ClearTask.CLEAR_SYNCID);
							}
						})
				.setNegativeButton(R.string.login_confirm_not_reserve,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								performClear(ClearTask.CLEAR_DATA);
							}
						}).create().show();
	}

	private void performClear(int clearWhat) {
		new ClearTask(this, clearWhat).execute();
	}

	private static class ClearTask extends AsyncTask<Void, Integer, Boolean> {
		public static final int CLEAR_DATA = 1;
		public static final int CLEAR_SYNCID = 2;

		private static final String TAG = "ClearTask";

		private WeakReference<LoginActivity> mTarget;
		private AuroraProgressDialog mPd;
		private int mClearWhat;

		public ClearTask(LoginActivity target, int clearWhat) {
			mTarget = new WeakReference<LoginActivity>(target);
			mClearWhat = clearWhat;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			LoginActivity target = mTarget.get();
			if (target != null) {
				mPd = AuroraProgressDialog.show(target, null, target
						.getString(R.string.login_clear_local_data_on_going),
						true, false);
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			LoginActivity target = mTarget.get();
			ExtraFileUpService.resetData();
			if (target != null) {
				switch (mClearWhat) {
				case CLEAR_DATA:
					return target.clearLocalData();
				case CLEAR_SYNCID:
					return target.clearSyncId();
				default:
					Log.d(TAG, "Jim, unsupported mode: " + mClearWhat);
					break;
				}
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			LoginActivity target = mTarget.get();
			if (target != null) {
				if (mPd != null && !target.isFinishing()) {
					mPd.dismiss();
				}
				mPd = null;
				target.openMainActivity(target.mLoginStatus);
			}
		}
	}

	/**
	 * 清空所有模块的本地数据
	 * 
	 * @return
	 */
	private boolean clearLocalData() {
		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(this);
		boolean result = true;
		if (apps != null && !apps.isEmpty()) {
			for (AppConfigInfo app : apps) {
				// 自己同步的APP不需要清理数据
				if (!app.isApp_syncself()) {		
					boolean rc = new AccountsAdapter(this,
							app.getApp_packagename(), app.getApp_uri())
					.clearLocalData();
					result &= rc;
					Log.d(TAG,
							"Jim, clear local data for " + app.getApp_packagename()
							+ " finished, result: " + rc);
				}
			}
		}

		return result;
	}

	/**
	 * 清除本地数据的同步Id
	 * 
	 * @return
	 */
	private boolean clearSyncId() {
		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(this);
		boolean result = true;
		if (apps != null && !apps.isEmpty()) {
			for (AppConfigInfo app : apps) {
				// 自己同步的APP不需要清理数据
				if (!app.isApp_syncself()) {	
					boolean rc = new AccountsAdapter(this,
							app.getApp_packagename(), app.getApp_uri())
					.clearLocalDataSyncId();
					result &= rc;
					Log.d(TAG, "Jim, clear syncId for " + app.getApp_packagename()
							+ " finished, result: " + rc);
				}
			}
		}

		return result;
	}

	private void openMainActivity(int loginStatus) {
		Log.d(TAG, "Jim, loginStatus: " + loginStatus);
		Intent intent = new Intent(this, SyncAccountActivity.class);
		intent.putExtra(SyncAccountActivity.EXTRA_KEY_LOGIN_STATUS, loginStatus);
		intent.putExtra(SyncAccountActivity.EXTRA_KEY_DIS_WARN, isDisWarn);
		intent.putExtra(SyncAccountActivity.EXTRA_KEY_NEED_SET_EXIT_ANIMATION, mNeedExitAnim);

		startActivity(intent);
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		
		if (mNeedExitAnim) {
			// 20150820 设计走查反馈动画不对，暂时屏蔽代码
//		    overridePendingTransition(
//	                com.aurora.R.anim.aurora_activity_close_enter,
//	                com.aurora.R.anim.aurora_activity_close_exit);
		}
	}
}