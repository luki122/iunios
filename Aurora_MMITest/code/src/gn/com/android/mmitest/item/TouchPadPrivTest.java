
package gn.com.android.mmitest.item;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
//Gionee zhangxiaowei 20130905 add for CR00888208 start
//import amigo.provider.AmigoSettings;

//Gionee zhangxiaowei 20130905 add for CR00888208 end
public class TouchPadPrivTest extends Activity implements Button.OnClickListener {
	private Vibrator mVibrator;
	private Button mRightBtn, mWrongBtn, mStopBtn, mRestartBtn;
	private static final String TAG = "TouchPadPrivTest";
	//private Handler mUiHandler;
	private static final int CAL_ING = 0;
	private static final int CAL_SUCCESS_OK = 1;
	private static final int CAL_FAIL_FAIL = 2;
	private static final int CAL_ING1 = 3;
	private static final int CAL_SUCCESS_OK1 = 4;
	private static final int CAL_FAIL_FAIL1 = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		// lp.dispatchAllKey = 1;
		getWindow().setAttributes(lp);
	 	View view = getWindow().getDecorView();
	     int visFlags = View.STATUS_BAR_DISABLE_BACK
               | View.STATUS_BAR_DISABLE_HOME
               | View.STATUS_BAR_DISABLE_RECENT
               | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION ;
               //|View.SYSTEM_UI_FLAG_IMMERSIVE;
       view.setSystemUiVisibility(visFlags);
       Log.e(TAG, "onCreate" );
		setContentView(R.layout.common_textview);
		
		mRightBtn = (Button) findViewById(R.id.right_btn);
		mRightBtn.setOnClickListener(this);
		mRightBtn.setEnabled(true);
		
		mWrongBtn = (Button) findViewById(R.id.wrong_btn);
		mWrongBtn.setOnClickListener(this);
		
		mRestartBtn = (Button) findViewById(R.id.restart_btn);
		mRestartBtn.setOnClickListener(this);
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Log.e(TAG, " onResume " );
		
		
		new Thread(new Runnable() {
			public void run() {
				try {			
					   
					 Log.e(TAG, "send CAL_ING " );		
						mUiHandler.sendEmptyMessage(CAL_ING);
						String result = getTpResult();
						Log.e(TAG, "result Number = " +  result);
						if (result  == null) {
							Log.e(TAG, "result == null " );
							mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL);
						} else if (result.equals("1")) {
							Log.e(TAG, "result == 1 " );
							mUiHandler.sendEmptyMessage(CAL_SUCCESS_OK);
						} else {
							Log.e(TAG, "result other" );
							mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL);
						}
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	//主线程显示画面
	private Handler mUiHandler = new Handler() {
		public void handleMessage(Message msg) {
			int what = msg.what;
			Log.e(TAG, "msg: " + what);
			switch (what) {
			case CAL_ING:
				Log.e(TAG, "result 00000000000 " );
				showDialog(CAL_ING1);
				break;
			case CAL_SUCCESS_OK:
				removeDialog(CAL_ING1);
				showDialog(CAL_SUCCESS_OK1);
				mWrongBtn.setEnabled(false);
				break;
			case CAL_FAIL_FAIL:
				//calibsucc = false;
				removeDialog(CAL_ING1);
				showDialog(CAL_FAIL_FAIL1);
				mRightBtn.setEnabled(false);
				break;
			}
		}
	};

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG,"onStop");
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case CAL_ING1:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(TouchPadPrivTest.this);
			builder2.setMessage(R.string.touch_priv_pad).setCancelable(false)
					.setPositiveButton(R.string.touch_priv_pad_ing, null);
			dialog = builder2.create();
			break;
		case CAL_SUCCESS_OK1:
			AlertDialog.Builder builder = new AlertDialog.Builder(TouchPadPrivTest.this);
			builder.setMessage(R.string.touch_priv_pad).setCancelable(false)
					.setPositiveButton(R.string.touch_priv_pad_ok, null);
			dialog = builder.create();
			break;
		case CAL_FAIL_FAIL1:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(TouchPadPrivTest.this);
			builder1.setMessage(R.string.touch_priv_pad).setCancelable(false)
					.setPositiveButton(R.string.touch_priv_pad_fail, null);
			dialog = builder1.create();
			break;
		}
		return dialog;
	}
	
	public String getTpResult() {
        String tpResult = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = "/sys/devices/platform/tp_wake_switch/factory_check";
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File currentFilePath = new File(mFileName);
                if (currentFilePath.exists()) {
                    fileInputStream = new FileInputStream(currentFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                    	tpResult = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tpResult;
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
