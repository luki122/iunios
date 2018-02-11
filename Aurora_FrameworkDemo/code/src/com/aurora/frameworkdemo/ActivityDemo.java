package com.aurora.frameworkdemo;

import android.os.Bundle;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.Type;

public class ActivityDemo extends AuroraActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_demo, Type.Normal);
	}
	
	
	
}
