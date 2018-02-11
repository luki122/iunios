package com.aurora.iunivoice.activity.account;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.ToastUtil;
import com.aurora.iunivoice.utils.ValidateUtil;

/**
 * 修改昵称界面
 * 
 * @author JimXia
 *
 * @date 2014-9-30 上午11:12:02
 */
public class SetNickActivity extends BaseAccountActivity implements OnClickListener {
	
	private EditText mNickEt;

	private AccountManager mAccountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_set_nick);

		setupViews();

		mAccountManager = new AccountManager(this);
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		setListeners();
		initData();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.set_nick_title);
		
		addActionBarItem(getString(R.string.save), ACTION_BAR_RIGHT_ITEM_ID);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		case ACTION_BAR_RIGHT_ITEM_ID:
			saveNick();
			break;
		}
	}

	private void initViews() {
		mNickEt = (EditText) findViewById(R.id.nick_name_et);
		initErrorViews();
	}

	private void initData() {
		mNickEt.setText(mPref.getUserNick());
		// mNickEt.setSelection(0, mNickEt.getText().toString().length());
		mNickEt.setSelection(mNickEt.getText().toString().length());
	}

	private void setListeners() {
		setListenerForErrorView(mNickEt);
	}

	@Override
	public void onClick(View v) {
		
	}

	private void saveNick() {
		String nickName = mNickEt.getText().toString().trim();
		if (TextUtils.isEmpty(nickName)) {
			showErrorInfo(
					mNickEt,
					mNickEt,
					getResources().getString(
							R.string.set_nick_error_name_is_empty));
			return;
		}

		final int nickLength = ValidateUtil.getCharCount(nickName);
		if (nickLength < 6 || nickLength > 20) {
			showErrorInfo(
					mNickEt,
					mNickEt,
					getResources().getString(
							R.string.set_nick_error_name_length_invalid));
			return;
		}

		if (nickName.equals(mPref.getUserNick())) {
			// 昵称没有修改
			finish();
		} else {
			getNetData(nickName);
		}
	}

	private void getNetData(String nickName) {
		showProgressDialog(getString(R.string.set_nick_saving));
		mAccountManager.updateAccountInfo(new DataResponse<UserLoginObject>() {
			public void run() {
				dismissProgressDialog();
				if (value != null) {
					final int code = value.getCode();
					Log.i(TAG, "the value=" + code);
					if (code == BaseResponseObject.CODE_SUCCESS) {
						ToastUtil.shortToast(R.string.set_nick_success);
						// add by zw 02-13 修改昵称次数
						/*new TotalCount(SetNickActivity.this, "280", "007", 1)
								.CountData();*/
						// end by zw
						finish();
					} else if (code == BaseResponseObject.CODE_ERROR_NICK_INVALID) {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil
								.shortToast(R.string.set_nick_error_nick_invalid);
					} else if (code == BaseResponseObject.CODE_ERROR_NICK_ALREADY_EXIST) {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil
								.shortToast(R.string.set_nick_error_nick_already_exist);
					} else if (code == BaseResponseObject.CODE_ERROR_NICK_UPDATE_FREQUENTLY) {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil
								.shortToast(R.string.set_nick_error_nick_update_frequently);
					} else {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil.shortToast(R.string.set_nick_error);
					}
				}
			}
		}, this, mPref.getUserID(), mPref.getUserKey(), nickName);
	}
	
}
