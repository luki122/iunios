package com.aurora.community.activity.account;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.aurora.community.R;
import com.aurora.community.utils.Globals;

public class UserAgreementActivity extends BaseAccountActivity {
	
	private WebView wb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_agreement);
		
		setupViews();
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		initData();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.register_user_agreement);
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
	
//	@Override
//	protected String getActionBarTitle() {
//		return getString(R.string.register_user_agreement);
//	}
	
	private void initViews() {
		wb = (WebView) findViewById(R.id.wb);
	}
	
	private void initData() {
		WebSettings settings = wb.getSettings(); 
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true); 
		
		wb.loadUrl(Globals.USER_AGREEMENT_HTML_URL);
	}

}
