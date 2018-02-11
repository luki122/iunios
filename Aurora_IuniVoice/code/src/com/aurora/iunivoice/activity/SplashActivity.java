package com.aurora.iunivoice.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.update.UpdateApp;
import com.aurora.iunivoice.utils.SystemUtils;

/**
 * @author lmjssjj
 */
public class SplashActivity extends BaseActivity {
	private ImageView iv_mainview;
	private SharedPreferences sp ;

	private Handler Handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(msg.what==0){
				Intent main = new Intent(SplashActivity.this,GuideActivity.class);
				startActivity(main);
				finish();
			}else if(msg.what==1){
				Intent main = new Intent(SplashActivity.this,MainActivity.class);
				startActivity(main);
				finish();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);   
//		}
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		enableActionBar(false);
		setupViews();
	
	}

	private void initEvent() {
		sp = getSharedPreferences("config", MODE_PRIVATE);
		int versioncode = sp.getInt("versioncode", 0);
		int versionCode2 = SystemUtils.getVersionCode(this, getPackageName());
		boolean isFirst = sp.getBoolean("isFirst", true);
		if(isFirst||(versioncode!=versionCode2)){
			Handler.sendEmptyMessageDelayed(0, 1500);
		}else{
			Handler.sendEmptyMessageDelayed(1, 1500);
		}
	}

	private void initView() {
		setContentView(R.layout.activity_splash);
		iv_mainview = (ImageView) findViewById(R.id.iv_splash_mainview);

	}

	@Override
	public void setupViews() {
		initView();
		initEvent();
		//new UpdateApp().checkUpdateApp(this, false, new IuniVoiceManager(this));		
	}
}
