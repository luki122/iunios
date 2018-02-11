package com.aurora.setupwizard;

import android.os.Bundle;
import aurora.app.AuroraActivity;

public class BaseActivity extends AuroraActivity {
	private App manager = App.getActivityManager(this);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager.putActivity(this);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		manager.removeActivity(this);
	}

	public void exit() {
		manager.exit();
	}
}
