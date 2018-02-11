package com.aurora.note;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.ImageView;
/*import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;*/

import com.aurora.note.activity.BaseActivity;
import com.aurora.note.util.Log;

public class NoteStartActivity extends BaseActivity {

	/*private ImageView mBgView;
	private TranslateAnimation animation;
	private TranslateAnimation animation2;

	private AnimationListener animationListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			mBgView.startAnimation(animation2);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			
		}

	};*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.note_start_activity);

		DisplayMetrics dm = getResources().getDisplayMetrics();
		Log.i("likai", "heightPixels = " + dm.heightPixels + " widthPixels = " + dm.widthPixels + " density = " + dm.density
				+ " densityDpi = " + dm.densityDpi + " xdpi = " + dm.xdpi + " ydpi = " + dm.ydpi);
		float widthDps = dm.widthPixels / dm.density;
		if (widthDps > 400) {
			ImageView bgView = (ImageView) findViewById(R.id.note_bg);
			bgView.setImageResource(R.drawable.ic_note_bg_3);
		} else if (widthDps > 360) {
			ImageView bgView = (ImageView) findViewById(R.id.note_bg);
			bgView.setImageResource(R.drawable.ic_note_bg_2);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mHandler.sendEmptyMessageDelayed(0, 1200);
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);

		gotoMainActivity();
	}

	private void gotoMainActivity() {
		Intent intent = new Intent(this, NoteMainActivity.class);
		startActivity(intent);
		finish();
	}

	/*private void initView() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float halfInvisibleHeight = 100 * displayMetrics.density;

		animation = new TranslateAnimation(0, 0, 0, halfInvisibleHeight);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setAnimationListener(animationListener);

		animation2 = new TranslateAnimation(0, 0, halfInvisibleHeight, -halfInvisibleHeight);
		animation2.setDuration(1000);
		animation2.setRepeatCount(-1);
		animation2.setRepeatMode(Animation.REVERSE);
		animation2.setFillAfter(true);

		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(animation);

		mBgView = (ImageView) findViewById(R.id.note_bg);
		mBgView.startAnimation(animation);
	}*/

}