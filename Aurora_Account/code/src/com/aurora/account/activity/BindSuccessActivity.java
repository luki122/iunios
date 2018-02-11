package com.aurora.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.util.Log;

/**
 * 绑定成功界面
 * @author JimXia
 *
 * @date 2014-9-30 下午3:49:38
 */
public class BindSuccessActivity extends BaseActivity implements OnClickListener {
    public static final String PHONE_NUM = "phone_num";
    
	private TextView mDetailTv;
	
	private String mPhoneNum = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.bind_success_activity,
				AuroraActionBar.Type.Empty, true);

		getIntentData();
		initViews();
		initData();
		configActionBar(getString(R.string.finish));
		setListeners();
	}
	
	private void getIntentData() {
        final Intent intent = getIntent();
        if (intent != null) {
            mPhoneNum = intent.getStringExtra(PHONE_NUM);
        }
        
        Log.i(TAG, "phoneNum: " + mPhoneNum);
    }
	
	private void initData() {
	    String detailInfo = getString(R.string.bind_success_details, mPhoneNum);
	    int startIndex = detailInfo.indexOf(mPhoneNum);
	    SpannableString ss = new SpannableString(detailInfo);
	    ss.setSpan(new ForegroundColorSpan(0xff019c73), startIndex,
	            startIndex + mPhoneNum.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	    mDetailTv.setText(ss);
    }

	@Override
	protected String getActionBarTitle() {
		return getString(R.string.bind_success_title);
	}

	private void initViews() {
	    mDetailTv = (TextView) findViewById(R.id.bind_detail_tv);
	}
	
	private void setListeners() {
	    mActionBarRightTv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.right_tv:
		    back();
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
	    startActivity(intent);
	}
	
	@Override
    protected boolean shouldHandleFocusChanged() {
        return false;
    }
}