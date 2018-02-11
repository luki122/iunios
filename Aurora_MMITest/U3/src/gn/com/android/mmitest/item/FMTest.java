
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;

import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.caf.fmradio.IFMRadioService;
import android.media.AudioManager;
//import com.android.internal.telephony.Phone;
//import com.android.internal.telephony.PhoneFactory;
//import android.os.AsyncResult;

public class FMTest extends Activity implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener {
    private SeekBar mSeekBar;

    private Button mMinusBtn, mPlusBtn, mDefaultBtn, mRightBtn, mWrongBtn;

    private TextView mCurrentHz, mEarphoneTv;

    private EarphonePluginReceiver mEarphonePluginReceiver;

    private String TAG = "FMTest";

    private IFMRadioService mService;

    public static final int HIGHEST_STATION = 1080;

    public static final int LOWEST_STATION = 875, EVENT_RESPONSE_SN_WRITE = 0;

    private int miCurrentStation;

    private RemoteServiceConnection mConnection;

    private boolean mIsPlud;

    private boolean mIsBind;

    private AudioManager mAM;

//    private Handler mUiHandler;

    private boolean mIsPass;
    private class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                Log.d(TAG, " ---onServiceConnected---");
                mService = IFMRadioService.Stub.asInterface(service);
                if (null == mService) {
                    Log.e(TAG, "Error: null interface");
                } else {
                         Log.d(TAG, " ---onServiceConnected--1111-");
                	mService.fmOn();
                	mService.tune((int)(107.5f * 1000));
                        enableWidget(true);
                	Log.d(TAG, " ---fmOn()---");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "IFMRadioService RemoteException");
            } catch (Exception e) {
                e.printStackTrace();
                  Log.e(TAG, "IFMRadioService Exception");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        setContentView(R.layout.fm_test);

        mDefaultBtn = (Button) findViewById(R.id.default_btn);
        mCurrentHz = (TextView) findViewById(R.id.current_hz);
        mEarphoneTv = (TextView) findViewById(R.id.earphone_note);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setIndeterminate(false);
        mSeekBar.setMax(210);
        mSeekBar.setProgress(205);
        mMinusBtn = (Button) findViewById(R.id.minus_btn);
        mPlusBtn = (Button) findViewById(R.id.plus_btn);

        mSeekBar.setEnabled(false);
        mMinusBtn.setEnabled(false);
        mPlusBtn.setEnabled(false);
        mDefaultBtn.setEnabled(false);

        mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(107.5)));
        mMinusBtn.setOnClickListener(this);
        mPlusBtn.setOnClickListener(this);
        mDefaultBtn.setOnClickListener(this);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mEarphonePluginReceiver = new EarphonePluginReceiver();

    }

    @Override
    public void onStart() {
    
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
     
        super.onStart();
        mConnection = new RemoteServiceConnection();
        /*
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_FM);
            mAM.setStreamVolume(AudioManager.STREAM_FM, maxVol - TestUtils.VOL_MINUS, 0);
        }
        */
        if (mEarphonePluginReceiver != null) {
            registerReceiver(mEarphonePluginReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        }


    }

    @Override
    public void onStop() {
        super.onStop();
       
        //this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
      
        if (null != mEarphonePluginReceiver) {
            unregisterReceiver(mEarphonePluginReceiver);
        }

        try {
            if (null != mService && true == mIsBind) {
            	mService.fmOff();
                unbindService(mConnection);
                mIsBind = false;
                mService = null;
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub
        if (null != mCurrentHz) {
            mCurrentHz.setText(getResources().getString(R.string.current_hz,
                    String.valueOf(87 + progress * 0.1)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        float frequency = (float) (87 + seekBar.getProgress() * 0.1);
        mCurrentHz
                .setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
        try {
            mService.tune((int)(frequency * 1000));
        } catch (RemoteException e) {

        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.minus_btn: {
                if (mSeekBar.getProgress() > 0) {
                    mSeekBar.incrementProgressBy(-1);
                    float frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                    mCurrentHz.setText(getResources().getString(R.string.current_hz,
                            String.valueOf(frequency)));
                    if (false == mIsBind) {
                        return;
                    }
                    try {
                        mService.tune((int)(frequency * 1000));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.plus_btn: {
                float frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                if (mSeekBar.getProgress() < mSeekBar.getMax()) {
                    mSeekBar.incrementProgressBy(1);
                    frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                    mCurrentHz.setText(getResources().getString(R.string.current_hz,
                            String.valueOf(frequency)));
                    if (false == mIsBind) {
                        return;
                    }
                    try {
                        mService.tune((int)(frequency * 1000));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.default_btn: {
                mSeekBar.setProgress(205);
                mCurrentHz.setText(getResources().getString(R.string.current_hz,
                        String.valueOf(107.5)));
                if (false == mIsBind) {
                    return;
                }
                try {
                    mService.tune((int)(107.5f * 1000));
                } catch (RemoteException e) {

                }
                break;
            }

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("52", "P");
                    editor.commit();
                }
                TestUtils.rightPress(TAG, FMTest.this);
//                }
                break;
            }

            case R.id.wrong_btn: {

                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("52", "F");
                    editor.commit();
                }
                TestUtils.wrongPress(TAG, FMTest.this);
//                }
                break;
            }
        }
    }

    private class EarphonePluginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            	Log.d(TAG, "ACTION_HEADSET_PLUG");
                int state = intent.getIntExtra("state", 0);
                Log.d(TAG, "HEADSET_PLUG_STAT : " + state );
                if (0 == state) {
                	if(true == mIsBind) {
                		unbindService(mConnection);
                		mIsBind = false;
                	}
                    mEarphoneTv.setText(R.string.insert_earphone);
                    mIsPlud = false;
                    enableWidget(false);

                } else if (1 == state) {
                    mEarphoneTv.setText(R.string.inserted_earphone);

                    if (false == mIsBind) {
                        mIsBind = bindService(new Intent("com.caf.fmradio.IFMRadioService"),
                                mConnection, Context.BIND_AUTO_CREATE);
                        if (true == mIsBind) {
                          //enableWidget(true);
                            Log.e(TAG, "bindService sucessful");
                        } else {
                            Log.e(TAG, "bindService fail");
                        }
                    }
           
                }
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

	private void enableWidget(boolean state ) {
		mRightBtn.setEnabled(state);
		mSeekBar.setEnabled(state);
		mMinusBtn.setEnabled(state);
		mPlusBtn.setEnabled(state);
		mDefaultBtn.setEnabled(state);
	}
}



