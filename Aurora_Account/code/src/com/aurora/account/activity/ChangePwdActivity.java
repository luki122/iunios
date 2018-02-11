package com.aurora.account.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.totalCount.TotalCount;
import com.aurora.account.util.EncryptUtil;
import com.aurora.account.util.Log;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.util.ValidateUtil;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.implement.DataResponse;

/**
 * 修改密码界面
 * 
 * @author JimXia
 *
 * @date 2014-9-30 上午11:11:47
 */
public class ChangePwdActivity extends BaseActivity implements OnClickListener {
	private EditText mOldPwdEt;
	private EditText mNewPwdEt;
	// private Button mSaveBtn;

	private AccountManager mAccountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.change_pwd_activity,
				AuroraActionBar.Type.Normal, true);

		initViews();
		configActionBar(getString(R.string.save));
		setListeners();

		mAccountManager = new AccountManager(this);
	}

	@Override
	protected String getActionBarTitle() {
		return getResources().getString(R.string.change_pwd_title);
	}

	private void initViews() {
		initErrorViews();
		mOldPwdEt = (EditText) findViewById(R.id.old_pwd_et);
		mNewPwdEt = (EditText) findViewById(R.id.new_pwd_et);
		// mSaveBtn = (Button) findViewById(R.id.save_btn);
	}

	private void setListeners() {
		// mSaveBtn.setOnClickListener(this);
		mActionBarRightTv.setOnClickListener(this);
		setListenerForErrorView(mOldPwdEt);
		setListenerForErrorView(mNewPwdEt);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.save_btn:
		// changePwd();
		// break;
		case R.id.right_tv:
			changePwd();
			break;
		}
	}

	private void changePwd() {
		String oldPwd = mOldPwdEt.getText().toString().trim();
		if (TextUtils.isEmpty(oldPwd)) {
			showErrorInfo(
					mOldPwdEt,
					mOldPwdEt,
					getResources().getString(
							R.string.change_pwd_error_old_pwd_is_empty));
			return;
		}
		String newPwd = mNewPwdEt.getText().toString().trim();
		if (TextUtils.isEmpty(newPwd)
				|| newPwd.length() < getResInteger(R.integer.password_min_length)) {
			showErrorInfo(
					mNewPwdEt,
					mNewPwdEt,
					getResources().getString(
							R.string.register_error_pwd_cannot_less_than_8));
			return;
		}
		if (!ValidateUtil.isCorrectFormatPwd(newPwd)) {
			showErrorInfo(
					mNewPwdEt,
					mNewPwdEt,
					getResources().getString(
							R.string.register_error_pwd_format_invalid));
			return;
		}

		getNetData(oldPwd, newPwd);
	}

	private void getNetData(String oldPwd, String newPwd) {
		showProgressDialog(getString(R.string.set_nick_saving));
		mAccountManager.changeLoginPwd(
				new DataResponse<BaseResponseObject>() {
					public void run() {
						dismissProgressDialog();
						if (value != null) {
							int code = value.getCode();
							Log.i(TAG, "Jim, the value=" + code);
							if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
								// add by zw 02-13 修改密码次数
								new TotalCount(ChangePwdActivity.this, "280",
										"008", 1).CountData();
								// end by zw
								ToastUtil
										.shortToast(R.string.change_pwd_success);
								finish();
							} else if (code == BaseResponseObject.CODE_ERROR_OLD_PWD_INCORRECT) {
								Log.d(TAG, "Jim, error: " + value.getDesc());
								ToastUtil
										.shortToast(R.string.change_pwd_error_old_pwd_incorrect);
							} else {
								Log.d(TAG, "Jim, error: " + value.getDesc());
								ToastUtil
										.shortToast(R.string.change_pwd_error_failed_to_save);
							}
						}
					}
				}, this, mPref.getUserID(), mPref.getUserKey(), EncryptUtil
						.getMD5Str(oldPwd), EncryptUtil.getMD5Str(newPwd),
				oldPwd,
				newPwd);
	}
}