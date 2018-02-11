package com.aurora.iunivoice.feedback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.BaseActivity;
public class UserFBActivity extends BaseActivity implements OnClickListener{
	
	private static final String TAG="FeedBackProblemActivity";
	private Button btn_provide_advice;
    private Button btn_feedback_the_problem;
    public UserFBActivity(){};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.feedback_user_feedback);
	
		btn_provide_advice=(Button)findViewById(R.id.btn_provide_advice);
		btn_feedback_the_problem=(Button)findViewById(R.id.btn_feedback_the_problem);
		btn_provide_advice.setOnClickListener(this);
		btn_feedback_the_problem.setOnClickListener(this);
	}

	@Override
	public void setupAuroraActionBar() {
		//enableActionBar(true);
		setTitleText(getResources().getString(R.string.feed_back));
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
		    Intent intent = new Intent(this, ProviderIdea.class);
		    startActivity(intent);
			break;
		case R.id.btn_feedback_the_problem:
			Intent intent1= new Intent(this, PFBActivity.class);
			startActivity(intent1);
			break;
		default:
			break;
		}
	}
	private static final int REQUEST_CODE=2;
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		
	}
}
