package com.aurora.iunivoice.activity;

import android.os.Bundle;

import com.aurora.iunivoice.R;

public class MinePublishActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setupViews();
	}
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		setTitleRes(R.string.mine_publish_text);
	}

}
