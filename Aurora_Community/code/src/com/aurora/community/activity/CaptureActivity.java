package com.aurora.community.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aurora.community.activity.twitter.TwitterNoteActivity;
import com.aurora.community.bean.PhotoInfo;
import com.aurora.community.bean.PhotoSerializable;
import com.aurora.community.utils.ImageLoaderHelper;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class CaptureActivity extends Activity {
	private static final String TAG = "CaptureActivity";

	private static final String IMAGE_CAPTURE_PREFIX = ImageLoaderHelper.getDirectory().toString();
	
	private String fileName;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
		 fileName = IMAGE_CAPTURE_PREFIX +"/"+ System.currentTimeMillis()+".jpg";
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(fileName)));  
	
		startActivityForResult(intent,10);
	}

    @Override
    protected void onResume() {
        super.onResume();
  
        MobclickAgent.onResume(this);
    }

    @Override
	protected void onPause() {
		super.onPause();
		
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
	    Log.e("linp", "~~~~~~~~~requestCode="+requestCode+";"+"resultCode="+resultCode);
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);   //, MediaStore.Images.Media.EXTERNAL_CONTENT_URI  
        Uri uri = Uri.fromFile(new File(fileName));     
        intent.setData(uri);     
        this.sendBroadcast(intent);   
        ArrayList<String> urlList = new ArrayList<String>();
		urlList.add(fileName);
		Bundle d = new Bundle();
		Intent intent1 = new Intent(this,TwitterNoteActivity.class);
		PhotoSerializable photoSerializable = new PhotoSerializable();
		photoSerializable.setUrlList(urlList);
		d.putSerializable("list", photoSerializable);
		intent1.putExtras(d);
		Log.e("linp", "~~~~~~~~~~~~~~~~~~~~~~~onActivityResult");
        startActivity(intent1);
       this.finish();
		
     //   super.onActivityResult(requestCode, resultCode, data);

	}

	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
}
