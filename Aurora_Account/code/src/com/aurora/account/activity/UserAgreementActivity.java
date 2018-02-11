package com.aurora.account.activity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.util.Globals;

public class UserAgreementActivity extends BaseActivity {
	
	private WebView wb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAuroraContentView(R.layout.activity_user_agreement,
				AuroraActionBar.Type.Normal, true);
		
		initViews();
		initData();
	}
	
	@Override
    protected boolean shouldHandleFocusChanged() {
        return false;
    }
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.register_user_agreement);
	}
	
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
