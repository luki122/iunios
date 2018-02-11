package com.aurora.setupwizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class testActivity extends Activity implements OnClickListener {

	private Button btn_next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		initData();
	}

	private void initViews() {
		btn_next = (Button) findViewById(R.id.btn_next);
	}

	private void initData() {
		btn_next.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			doNext();
			break;
		}
	}
	
	private void doNext() {
		if (SetupWizardUtils.hasTelephony(this)) {
			if (SetupWizardUtils.isSimInserted(this)) {
			} else {
				Intent i = new Intent(testActivity.this, SimCardMissingActivity.class);
				startActivity(i);
			}
        } else {
        }
	}

}
