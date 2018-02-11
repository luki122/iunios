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

package com.android.settings.bootanimation;

import com.android.settings.R;


import android.os.Bundle;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView; 
import java.io.File;
import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;

/**
 * 
 * Preview bootanimation interface
 * 
 * @author hanpingjiang 2014.03.16
 * 
 */

public class ScanBootanimation extends Activity implements View.OnClickListener {
	public static final String TAG = "ScanBootanimation";
	
	private ImageView scanImageView; 
	private Bitmap scanBitmap = null;
	String backgroundImagePath = "/sdcard/iuni";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_bootanimation);
		scanImageView = (ImageView) findViewById(R.id.scan_image);
		scanImageView.setOnClickListener(this);
		File photoFile = null;
		FileInputStream fis = null;
		try {
			photoFile = new File(backgroundImagePath + "/iuni_bootanimation_end.png");
			fis = new FileInputStream(photoFile);
			
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			scanBitmap = BitmapFactory.decodeStream(fis, null, opt);
			
		    scanImageView.setImageBitmap(scanBitmap);
		    if(null != fis) {
				fis.close();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			overridePendingTransition(0, R.anim.aurora_zoom_out);
            return true;
          }
		return false;
        }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if (view == scanImageView) {
			finish();
			overridePendingTransition(0, R.anim.aurora_zoom_out);
		}
	}
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        if(null != scanBitmap && !scanBitmap.isRecycled()){  
			scanBitmap.recycle();  
            scanBitmap = null;  
        }
        if(null != scanImageView){     
		    scanImageView.setImageDrawable(null);    
		} 
    }
}


