
package com.gionee.autommi;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


public class ScreenBrightnessTest extends BaseActivity {
    View mColorView;
    private static final String TAG = "ScreenBrightnessTest";
    


    WindowManager.LayoutParams mlp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        getWindow().setAttributes(lp);


        mColorView = new View(this);
        mlp = getWindow().getAttributes();
        mlp.screenBrightness = 1.0f;
        getWindow().setAttributes(mlp);
        mColorView.setBackgroundColor(Color.WHITE);
        setContentView(mColorView);
    }
    @Override
    public void onResume() {
        super.onResume();
        TestUtils.acquireWakeLock(this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        TestUtils.releaseWakeLock();
    }

    @Override
    public void onStop() {
        super.onStop();
	ScreenBrightnessTest.this.finish();
	Log.i("aaaa","onStop");
    }
}
