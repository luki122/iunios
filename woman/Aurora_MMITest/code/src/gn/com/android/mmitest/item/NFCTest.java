
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;

import gn.com.android.mmitest.TestUtils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.MifareClassic;
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


public class NFCTest extends Activity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "NFCTest";

	TextView promt;
    private RelativeLayout mParent;
    private boolean mIsPressure;
    private  NfcAdapter mNfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
 

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        setContentView(R.layout.nfc_test); 
        promt = (TextView) findViewById(R.id.promt); 
        
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
        this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter == null) {
			finish();
			return;
		}
	if(!mNfcAdapter.isEnabled()){
		Log.i("aaaa","mNfcAdapter != isEnable");
			mNfcAdapter.enable();
		}
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    try {
        ndef.addDataType("*/*");    
    }
    catch (MalformedMimeTypeException e) {
        throw new RuntimeException("fail", e);
    }
   intentFiltersArray = new IntentFilter[] {ndef, };
   techListsArray = new String[][] { new String[] { NfcA.class.getName() },
                                                        new String[] { NfcB.class.getName() },
                                                        new String[] { NfcF.class.getName() },
                                                        new String[] { NfcV.class.getName() },
                                                        new String[] { Ndef.class.getName() },
                                                        new String[] { NdefFormatable.class.getName() },
                                                        new String[] { MifareClassic.class.getName() },
                                                        new String[] { MifareUltralight.class.getName() },
                                                        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("aaaa","onResume");
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
 
    }


    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
       
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
    public void onNewIntent(Intent intent) {
        Log.i("aaaa","intent.getAction()="+intent.getAction());
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(intent.getAction().equals("android.nfc.action.TECH_DISCOVERED")||intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED")||intent.getAction().equals("android.nfc.action.TAG_DISCOVERED"))
         {
          promt.setText(R.string.test_right_nfc);
          mRightBtn.setEnabled(true);
          
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
