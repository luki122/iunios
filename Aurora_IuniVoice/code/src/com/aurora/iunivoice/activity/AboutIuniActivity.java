package com.aurora.iunivoice.activity;

import com.aurora.iunivoice.R;

import android.os.Bundle;
import android.widget.TextView;

public class AboutIuniActivity extends BaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_iuni);

		enableBackItem(true);
		setTitleText(getResources().getString(R.string.about_title));
		setupViews();
	}
	
	@Override
	public void setupViews() {
		
	}

}
