package com.gionee.autommi;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Color;

public class whiteTest extends BaseActivity {

	private static final String TAG = "whiteTest";
    private View mColorView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "ritColor : ");
        mColorView = new View(this);
		mColorView.setBackgroundColor(Color.WHITE);
        setContentView(mColorView);
	}
	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onStop()");
		this.finish();
	}
}
