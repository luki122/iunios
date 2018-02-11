package com.android.settings.feedback;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import aurora.widget.AuroraButton;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import com.android.settings.R;
public class UserFBActivity extends AuroraActivity implements OnClickListener{
	
	private static final String TAG="FeedBackProblemActivity";
	private AuroraButton btn_provide_advice;
    private AuroraButton btn_feedback_the_problem;
    private AuroraActionBar actionBar;
    public UserFBActivity(){};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.user_feedback ,AuroraActionBar.Type.Normal);
		getAuroraActionBar().setTitle(R.string.feed_back);
		btn_provide_advice=(AuroraButton)findViewById(R.id.btn_provide_advice);
		btn_feedback_the_problem=(AuroraButton)findViewById(R.id.btn_feedback_the_problem);
		btn_provide_advice.setOnClickListener(this);
		btn_feedback_the_problem.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_provide_advice:
		    Intent intent = new Intent(this, AuroraAdvicesActivity.class);
		    startActivity(intent);
			break;
		case R.id.btn_feedback_the_problem:
			Intent intent1= new Intent(this, AuroraPFBActivity.class);
			startActivity(intent1);
			break;
		default:
			break;
		}
	}
	private static final int REQUEST_CODE=2;
}
