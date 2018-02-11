package com.tencent.mm.clone;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
	private PackageManager pm;
	private static final String WECHAT_CATEGORY = "android.intent.category.CLONED";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pm = getPackageManager();
		startWechat();
		finish();
	}

	private void startWechat(){
		Intent userOwner = pm.getLaunchIntentForPackage("com.tencent.mm");
		if(userOwner!=null){
			userOwner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			userOwner.addCategory(WECHAT_CATEGORY);
			startActivity(userOwner);
		}else{
			Toast.makeText(this, R.string.not_install_wechat, Toast.LENGTH_SHORT).show();
		}
	}
}
