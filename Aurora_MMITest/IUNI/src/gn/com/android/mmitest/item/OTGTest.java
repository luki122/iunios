
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;

import gn.com.android.mmitest.TestUtils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class OTGTest extends Activity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "OTGTest";
    
    private OtgPluginReceiver mOtgPluginReceiver;

	TextView promt;
    private RelativeLayout mParent;
    private boolean mIsPressure;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
 

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        getWindow().setAttributes(lp);
        setContentView(R.layout.otg_test); 
        promt = (TextView) findViewById(R.id.promt); 
        
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        mOtgPluginReceiver = new OtgPluginReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
    
    }
    

    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	    if (mOtgPluginReceiver != null) {
	    	IntentFilter intentFilter = new IntentFilter();
	    	intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    	intentFilter.addDataScheme("file");
	    	registerReceiver(mOtgPluginReceiver, intentFilter);
        }
 
	}

	@Override
    public void onPause() {
        super.onPause();
        if (null != mOtgPluginReceiver) {
            unregisterReceiver(mOtgPluginReceiver);
        }
       
      
    }
    @Override
    public void onStop() {
        super.onStop();
       
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                //mNfcAdapter.disable();
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                //mNfcAdapter.disable();
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
  /*  public void onNewIntent(Intent intent) {
        Log.i("aaaa","intent.getAction()="+intent.getAction());
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(intent.getAction().equals("android.nfc.action.TECH_DISCOVERED")||intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED")||intent.getAction().equals("android.nfc.action.TAG_DISCOVERED"))
         {
          promt.setText(R.string.test_right_nfc);
          mRightBtn.setEnabled(true);
          
        }
    }*/
    private class OtgPluginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            	Log.d(TAG, "receiver --> ACTION_MEDIA_MOUNTED");
            	promt.setText(R.string.test_right_otg);
                mRightBtn.setEnabled(true);
            	
            }
        }

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
