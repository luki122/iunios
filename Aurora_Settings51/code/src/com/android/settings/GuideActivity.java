package com.android.settings;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.android.settings.R;

public class GuideActivity extends Activity {

	private ImageView mGuideImage;
	private AnimationDrawable animationDrawable;
	private Button mButton;
	private static final int SHOW_BUTTON = 1;

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SHOW_BUTTON) {
				mButton.setVisibility(View.VISIBLE);
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.aurora_guide_main);
		mGuideImage = (ImageView) findViewById(R.id.image);
		mButton = (Button) findViewById(R.id.i_know);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mGuideImage.setBackgroundResource(R.anim.aurora_guide);
		animationDrawable = (AnimationDrawable) mGuideImage.getBackground();

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		animationDrawable.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHandler.sendEmptyMessageDelayed(SHOW_BUTTON, 2000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("666", "onPause");
		animationDrawable.stop();
		mHandler.removeMessages(SHOW_BUTTON);
	}


	@Override
	public void finish() {
		super.finish();
		//this.overridePendingTransition(0, R.anim.aurora_guide_close_exit);
	}

}
