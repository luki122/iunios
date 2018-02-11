/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import java.io.File;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;

public class ScanEggImageActivity extends Activity implements View.OnClickListener{
	private static final String LOG_TAG = "ScanEggImage";
	public static final int SHARE_TIME = 250;
	private Button shareEgg;
	private boolean isEnable = false;
	FrameLayout layout = null;
	Bitmap imageBitmap = null;
	
	String backgroundImagePath = "/system/iuni/IUNIOS.png";// 照片保存路径
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, savedInstanceState + "' =============AuroraPreference-------------");
		setContentView(R.layout.scan_egg);
		layout = (FrameLayout) findViewById(R.id.layout);
		shareEgg = (Button) findViewById(R.id.share_egg);
		setBackground();
		try {
			new Handler().postDelayed(new Runnable() {
			   	@Override
			   	public void run() {
			   	shareEgg.setVisibility(View.VISIBLE);
				// 图片渐变模糊度始终
				AlphaAnimation shareAlpha = new AlphaAnimation(0, 1.0f);
				// 渐变时间
				shareAlpha.setDuration(1000);
				// 展示图片渐变动画
				shareEgg.startAnimation(shareAlpha);
				isEnable = true;
				}}, SHARE_TIME);
			if(null != shareEgg) {
				shareEgg.setOnClickListener(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void setBackground() {
		imageBitmap = BitmapFactory.decodeFile(backgroundImagePath);
		BitmapDrawable eggBitmapDrawable = new BitmapDrawable(imageBitmap);
		layout.setBackground(eggBitmapDrawable);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.share_egg:
			shareEggImage();
			break;
		default:
			break;
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && isEnable) {
			isEnable = false; 
			shareEgg.setVisibility(View.INVISIBLE);  
			// 图片渐变模糊度始终
			AlphaAnimation eggAlpha = new AlphaAnimation(1.0f, 0.1f);
			// 渐变时间
			eggAlpha.setDuration(SHARE_TIME);
			// 展示图片渐变动画
			shareEgg.startAnimation(eggAlpha);

			new Handler().postDelayed(new Runnable() {
			   	@Override
			   	public void run() {
				finish();
				overridePendingTransition(0, R.anim.aurora_zoom_out);
				}}, SHARE_TIME);			
            return true;
          }
		return false;
        }

	public void shareEggImage() {
		Intent intent=new Intent(Intent.ACTION_SEND);   
		intent.setType("image/*"); 
		File imageFile = new File(backgroundImagePath);
		intent.putExtra(Intent.EXTRA_TEXT,"IUNIOS 生来纯净");
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));  
		startActivity(Intent.createChooser(intent, "分享"));	
	}

	@Override
    public void onStop() {
        super.onStop();
    }

	@Override
    public void onDestroy() {
        super.onDestroy();
		if(imageBitmap != null && !imageBitmap.isRecycled()){  
			imageBitmap.recycle();  
            imageBitmap = null;  
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

}


