
package gn.com.android.mmitest.item;

import java.util.Set;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.os.AsyncResult;
import java.util.ArrayList;

public class BluetoothTest extends Activity implements View.OnClickListener {
    public static final String FACTORY_BT = "bluetooth";

	private BluetoothAdapter mBluetoothAdapter;

    private TextView mTv;

    private BroadcastReceiver mReceiver;

    private ArrayAdapter mArrayAdapter;

    private Handler mUiHandler;

    private ListView mBtLv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private boolean mIsDiscovery, mIsPass;

    private static final int INIT_BT_FAIL = 0;

    private static final int BEGIN_TO_SCAN = 1, EVENT_RESPONSE_SN_WRITE = 2;

    private static String TAG = "BluetoothTest";

	private ArrayList<String> macAds ;
	
	// Gionee xiaolin 20120522 add for CR00607817 start
	private BroadcastReceiver mDF;
	private int mDisNum;
	// Gionee xiaolin 20120522 add for CR00607817 end
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

		macAds = new ArrayList<String>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
        setContentView(R.layout.bluetooth_test);
        mTv = (TextView) findViewById(R.id.bt_title);
        mBtLv = (ListView) findViewById(R.id.bt_content);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        mBtLv.setAdapter(mArrayAdapter);
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case INIT_BT_FAIL:
                        mTv.setText(R.string.init_bluetooth_fail);
                        break;
                    case BEGIN_TO_SCAN:
                        mTv.setText(R.string.scanning_bluetooth_device);
                        mBluetoothAdapter.startDiscovery();
                        mIsDiscovery = true;
                        break;
//                    case EVENT_RESPONSE_SN_WRITE: {
//                        AsyncResult ar;
//                        ar = (AsyncResult) msg.obj;
//                        if (ar.exception == null) {
//                            if (true == mIsPass) {
//                                TestUtils.wrongPress(TAG, BluetoothTest.this);
//                            } else {
//                                TestUtils.rightPress(TAG, BluetoothTest.this);
//                            }
//                        } else {
//                            if (true == mIsPass) {
//                                TestUtils.wrongPress(TAG, BluetoothTest.this);
//                            } else {
//                                TestUtils.rightPress(TAG, BluetoothTest.this);
//                            }
//                        }
//                        break;
//                    }
                    
                }
            }
        };
        if (mBluetoothAdapter == null) {
            finish();
        }
    }
    
    // Gionee xiaolin 20120522 modify for CR00607817 start
    @Override
    protected void onStop() {
        super.onStop();
        if (null != mBluetoothAdapter) {
            if (true == mIsDiscovery) {
                mBluetoothAdapter.cancelDiscovery();
            }
            this.unregisterReceiver(mReceiver);
        }
        
        unregisterReceiver(mDF);
    }
    
   
    @Override
    protected void onStart() {
        super.onStart();
        mIsDiscovery = false;
        // xiaolin
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, intent.toString());
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.d(TAG, "ACTION_FOUND");
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Gionee xiaolin 20120408 add for CR00568197 start
                    String name = device.getName();
                    String address = device.getAddress();
                    if (macAds.contains(address)) 
                        return;
                    else
                        macAds.add(address);
                    if (null != name)
                        mArrayAdapter.add(name + "\n" + address);
                    else
                        mArrayAdapter.add(address + "\n" + address);
                    // Gionee xiaolin 20120408 add for CR00568197 end
                    mTv.setText(BluetoothTest.this.getResources().getString(
                            R.string.find_bluetooth_device_num,
                            String.valueOf(mArrayAdapter.getCount())));
                    mRightBtn.setEnabled(true);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        
        mDF = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                    mDisNum ++;
                    if (1== mDisNum){
                      if(mArrayAdapter.isEmpty()) 
                          Toast.makeText(BluetoothTest.this, "Discovery the second time", Toast.LENGTH_LONG).show();
                          mBluetoothAdapter.startDiscovery();
                    }
                }            
            }            
        };
        
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mDF, filter);
        
        if (false == mBluetoothAdapter.isEnabled()) {
            mTv.setText(R.string.opening_bluetooth);
            mBluetoothAdapter.enable();
            new Thread(new Runnable() {
                public void run() {
                    int i = 0;
                    try {
                        while (!mBluetoothAdapter.isEnabled()) {
                            Thread.sleep(1000);
                            i++;
                            if (i > 20) {
                                mUiHandler.sendEmptyMessage(INIT_BT_FAIL);
                                return;
                            }
                        }
                        mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            mTv.setText(R.string.scanning_bluetooth_device);
            mBluetoothAdapter.startDiscovery();
            mIsDiscovery = true;
        }
                                 
    }
    // Gionee xiaolin 20120522 modify for CR0060781 end
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString(TestUtils.factoryFlag.get(FACTORY_BT), "P");
                    editor.commit();
                }
                    TestUtils.rightPress(TAG, BluetoothTest.this);
                break;
            }

            case R.id.wrong_btn: {
 
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString(TestUtils.factoryFlag.get(FACTORY_BT), "F");
                    editor.commit();
                }
                    TestUtils.wrongPress(TAG, BluetoothTest.this);
//                }
                break;
            }
            
            case R.id.restart_btn: {
                 // Gionee xiaolin 20120522 modify for CR00607817 start
			     mRightBtn.setEnabled(false);		     
			     macAds.clear();
		         mArrayAdapter.clear();
		         mBluetoothAdapter.cancelDiscovery();
				 try {
                     Thread.sleep(100);
			     } catch (InterruptedException e) {
				 	e.printStackTrace();
				 }
			     mBluetoothAdapter.startDiscovery();
				 mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
				 break;
				 // Gionee xiaolin 20120522 modify for CR00607817 end
            }
            
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
}
