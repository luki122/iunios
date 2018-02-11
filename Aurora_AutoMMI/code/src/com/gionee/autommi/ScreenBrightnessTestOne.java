
package com.gionee.autommi;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


public class ScreenBrightnessTestOne extends BaseActivity {
    View mColorView;
    private static final String TAG = "ScreenBrightnessTestOne";
    


    WindowManager.LayoutParams mlp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mColorView = new View(this);
        mlp = getWindow().getAttributes();
        mlp.screenBrightness = 0.75f;
        getWindow().setAttributes(mlp);
        mColorView.setBackgroundColor(Color.WHITE);
        setContentView(mColorView);
    }

    @Override
    public void onStop() {
        super.onStop();
	ScreenBrightnessTestOne.this.finish();
	Log.i("aaaa","onStop");
    }
}
