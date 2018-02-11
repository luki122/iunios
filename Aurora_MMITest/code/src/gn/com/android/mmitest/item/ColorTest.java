
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.app.Notification;
import android.app.NotificationManager;

public class ColorTest extends Activity implements OnClickListener {
    private View mColorView;

    private int mCount;

    private Handler mColorHandler;

    private Runnable mColorRunnable;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    
    private static final int NOTIFICATION_ID = 0;
  	private static final int RED =   0xFFFF0000; //red		
	private static final int GREEN = 0xFF00FF00;//green
	private static final int BLUE =  0xFF0000FF; //blue
    private static final String TAG = "ColorTest";
    NotificationManager    mNotificationManager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        mColorHandler = new Handler();
        mColorView = new View(this);
        mColorView.setBackgroundColor(Color.RED);
        setContentView(mColorView);
        showNotification(RED,1,0);
    }

    private void showNotification(int  color,int on,int off) {
	    Notification notice = new Notification();  
        notice.ledARGB= color;  
        notice.ledOnMS = on;  
        notice.ledOffMS = off;  
        notice.flags |= Notification.FLAG_SHOW_LIGHTS;  
        Log.d(TAG, "zhangxiaowei mmicolor"+Integer.toHexString(notice.ledARGB)+"on--"+notice.ledOnMS+"off--"+notice.ledOffMS);
        mNotificationManager.notify(NOTIFICATION_ID, notice);  
}
    private void showNotification1() {
    	mNotificationManager.cancel(NOTIFICATION_ID) ;
}
    @Override
    public void onStart() {
        super.onStart();
//        if (true == TestUtils.mIsAutoMode) {
//            mCount = 0;
//            mColorHandler.postDelayed(mColorRunnable, 1000);
//        }
    }

    @Override
	public boolean onTouchEvent(MotionEvent event) {
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			Log.e("lich", "mCount == " + mCount);
			switch (mCount) {			
			    case 0:
				    mColorView.setBackgroundColor(Color.GREEN);
                    showNotification(GREEN,1,0);
				    mCount++;
				    break;
			    case 1:
				    mColorView.setBackgroundColor(Color.BLUE);
                    showNotification(BLUE,1,0);
				    mCount++;
				    break;
			    case 2:
				    mColorView.setBackgroundColor(Color.BLACK);
				    showNotification1();
				    mCount++;
				    break;
			    case 3:
				    mColorView.setBackgroundColor(Color.WHITE);
				    mCount++;
				    break;
			    case 4:
				    mCount++;
				    if (true == TestUtils.mIsAutoMode|| true == TestUtils.mIsAutoMode_2 ||true == TestUtils.mIsAutoMode_3) {
					    ColorTest.this.setContentView(R.layout.common_textview);
					    mRightBtn = (Button) ColorTest.this
							    .findViewById(R.id.right_btn);
					    mRightBtn.setOnClickListener(ColorTest.this);
					    mRightBtn.setEnabled(true);
					    mWrongBtn = (Button) ColorTest.this
							    .findViewById(R.id.wrong_btn);
					    mWrongBtn.setOnClickListener(ColorTest.this);
					    mRestartBtn = (Button) findViewById(R.id.restart_btn);
					    mRestartBtn.setOnClickListener(this);
				    } else {
					    finish();
				    }
				    break;
			}
		}
		return true;
	}

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
            
            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
