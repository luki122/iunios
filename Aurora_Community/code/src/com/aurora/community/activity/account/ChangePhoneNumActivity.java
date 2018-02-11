package com.aurora.community.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.ToastUtil;
import com.aurora.community.utils.ValidateUtil;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.UserVC;
import com.aurora.datauiapi.data.implement.DataResponse;

/**
 * 修改绑定手机号界面
 * @author JimXia
 *
 * @date 2014-9-30 下午3:29:07
 */
public class ChangePhoneNumActivity extends BaseAccountActivity implements OnClickListener {
    private static final int REQUEST_COUNTRY_CODE = 10;
    
    public static final String EXTRA_KEY_MODE = "mode";
    public static final int MODE_SET = 1;
    public static final int MODE_REBIND = 2;
    
	private EditText mNewPhoneEt;
//	private Button mNextBtn;
	private LinearLayout ll_countryCode;
    private TextView tv_countryCode;
	
	private AccountManager mAccountManager;
	private int mMode = MODE_REBIND;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mMode = getIntent().getIntExtra(EXTRA_KEY_MODE, MODE_REBIND);
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.change_phone_num_activity);
		
		setupViews();
		initData();
		
		mAccountManager = new AccountManager(this);
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
		
		if (mMode == MODE_REBIND) {
			setTitleRes(R.string.change_phone_title);
		} else {
			setTitleRes(R.string.change_phone_title_set);
		}
		
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
			changePhone();
			break;
		}
	}
	
	private void initData() {
        // 默认编码
    }
	
	private void initViews() {
	    mNewPhoneEt = (EditText) findViewById(R.id.new_phone_et);
//	    mNextBtn = (Button) findViewById(R.id.btn_next);
	    ll_countryCode = (LinearLayout) findViewById(R.id.ll_countryCode);
        tv_countryCode = (TextView) findViewById(R.id.tv_countryCode);
	    initErrorViews();
	    if (mMode == MODE_SET) {
	        mNewPhoneEt.setHint(R.string.change_phone_new_phone_hint_set);
	    }
	}
	
	private void setListeners() {
	    ll_countryCode.setOnClickListener(this);
//	    mNextBtn.setOnClickListener(this);
//	    mActionBarRightTv.setOnClickListener(this);
	    setListenerForErrorView(mNewPhoneEt);
	}
	
	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
//	        case R.id.btn_next:
//	            changePhone();
//	            break;
//	        case R.id.right_tv:
//	            changePhone();
//	            break;
//	        case R.id.ll_countryCode:
//	            Intent codeIntent = new Intent(this, CountryCodeActivity.class);
//	            startActivityForResult(codeIntent, REQUEST_COUNTRY_CODE);
//	            break;
	    }
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_COUNTRY_CODE && resultCode == RESULT_OK) {
//            if (data != null) {
//                CountryCode countryCode = data.getParcelableExtra(CountryCodeActivity.COUNTRY_CODE);
//                if (countryCode != null) {
//                    this.countryCode = countryCode;
//                    showCountryCode();
//                }
//            }
//        }
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
	
	private void changePhone() {
	    String newPhone = mNewPhoneEt.getText().toString().trim();
	    if (TextUtils.isEmpty(newPhone)) {
	        showErrorInfo(mNewPhoneEt, mNewPhoneEt,
	                mMode == MODE_REBIND ? getString(R.string.change_phone_error_new_phone_is_empty):
	                    getString(R.string.change_phone_error_bind_phone_is_empty));
            return;
        }
	    
	    if (!ValidateUtil.isMobilePhoneNumVaild(newPhone)) {
	        showErrorInfo(mNewPhoneEt, mNewPhoneEt, getString(R.string.register_error_phonenum_format_invalid));
            return;
	    }
	    
	    getNetData(newPhone);
	}
	
	private void getNetData(final String phoneNum) {
	    if (mPref.isVercodeExpired(phoneNum,
	            PhoneNumVerificationActivity.MODE_FOR_CHANGE_PHONE)) {
	        showProgressDialog(getString(R.string.register_getting_verify_code));
	        mAccountManager.getVerifyCode(new DataResponse<UserVC>() {
	            public void run() {
	                dismissProgressDialog();
	                if (value != null) {
	                    final int code = value.getCode();
	                    Log.i(TAG, "Jim, the value=" + code);
	                    if (code == UserVC.CODE_SUCCESS) {
	                        mPref.recordLastSendTime(phoneNum,
	                                PhoneNumVerificationActivity.MODE_FOR_CHANGE_PHONE);
	                        openPhoneNumVerificationActivity(phoneNum);
	                    } else if (code == UserVC.CODE_ERROR_PHONE_NUM_ALREADY_REGISTERED) {
	                        Log.d(TAG, "Jim, error: " + value.getDesc());
	                        ToastUtil.shortToast(R.string.change_phone_error_phone_num_already_registered);
	                    } else if (code == UserVC.CODE_ERROR_FAILED_TO_SEND) {
	                        Log.d(TAG, "Jim, error: " + value.getDesc());
	                        ToastUtil.shortToast(R.string.change_phone_error_failed_to_send);
	                    } else {
	                        Log.d(TAG, "Jim, error: " + value.getDesc());
	                        ToastUtil.shortToast(R.string.register_get_verify_code_fail);
	                    }
	                }
	            }
	        }, ChangePhoneNumActivity.this, mPref.getUserID(),
	                mPref.getUserKey(), phoneNum, "bindphone", null,
	                SystemUtils.getIMEI());
	    } else {
	        openPhoneNumVerificationActivity(phoneNum);
	    }
    }
	
	private void openPhoneNumVerificationActivity(String phoneNum) {
	    Intent intent = new Intent(ChangePhoneNumActivity.this, PhoneNumVerificationActivity.class);
        intent.putExtra(PhoneNumVerificationActivity.EXTRA_KEY_TITLE,
                mMode == MODE_REBIND ? getString(R.string.change_phone_verification_title):
                    getString(R.string.change_phone_verification_title_set));
        intent.putExtra(PhoneNumVerificationActivity.EXTRA_KEY_MODE, PhoneNumVerificationActivity.MODE_FOR_CHANGE_PHONE);
        intent.putExtra(PhoneNumVerificationActivity.PHONE_NUM, phoneNum);
//        intent.putExtra(PhoneNumVerificationActivity.COUNTRY_CODE, countryCode.getCode());
        startActivity(intent);
	}
	
}