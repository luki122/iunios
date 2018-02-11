package gn.com.android.mmitest.item;

import java.util.jar.Attributes.Name;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.media.AudioSystem;
import android.os.SystemProperties;



import java.io.BufferedReader;
import java.io.FileReader;
import android.content.SharedPreferences;
import android.app.Dialog;
import android.provider.Settings;

public class RecevierAndAlsTest extends Activity implements OnClickListener {
	Button mToneBt;

	private ToneGenerator mToneGenerator;

	private int TONE_LENGTH_MS = 3000;

	private Button mRightBtn, mWrongBtn, mRestartBtn;

	private AudioManager mAM;

	private static final String TAG = "RecevierAndAlsTest";

	private boolean mIsToneOn;
	private final int duration = 15; // seconds

	private final int sampleRate = 8000;

	private final int numSamples = duration * sampleRate;

	private final double sample[] = new double[numSamples];

	private final double freqOfTone = 500; // hz
	private final double freqOfTone2 = 1000; // hz

	private final byte generatedSnd[] = new byte[2 * numSamples];

	private static final int CAL_FAIL = 0;
	private static final int CAL_SUCCESS = 1;
	
	private static final int PLAY_FREQOFTONE = 0;
	private Handler mHandler = new Handler();

	private Handler mMainHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PLAY_FREQOFTONE:
				mMainHandler.removeMessages(RecevierAndAlsTest.PLAY_FREQOFTONE);
				mEarpieceTips.setText(R.string.receiver_note_2);
				
				if (null != mAM) {
					int maxVol = mAM
							.getStreamMaxVolume(AudioManager.MODE_NORMAL);
					mAM.setStreamVolume(AudioManager.MODE_NORMAL, maxVol
							- TestUtils.VOL_MINUS_INCALL, 0);
				}
				// Gionee xiaolin 20121017 modify for CR00715318 end
				final Thread thread = new Thread(new Runnable() {
					public void run() {
						genTone();
						mHandler.post(new Runnable() {
							public void run() {
								playSound();
							}
						});
					}
				});
				thread.start();
				break;
			}
		}
	};

    private SensorManager mSensorMgr;
    private Sensor mPSensor;
    private boolean  mIsProximityRight;
    private boolean mIsClose;
    
    SensorEventListener mProximityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            int i = (int) event.values[0];
            Log.d(TAG, "i = " + i);
            mIsClose = (i == 0 ? true : false);
            if (true == mIsClose) {
                mParent.setBackgroundColor(Color.GREEN);
                mRightBtn.setEnabled(true);
                
            } else {
                mParent.setBackgroundColor(Color.BLACK);
            }
            
            if (0 != i)
            	i = 1;
            mProximityNum.setText(i + "");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }

    };
    
    private RelativeLayout mParent;
	private TextView mEarpieceTips,mProximityNum;
	
	private AudioTrack mAudioTrack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		// lp.dispatchAllKey = 1;
		getWindow().setAttributes(lp);
		setContentView(R.layout.proximity_earpiece);
		mParent = (RelativeLayout) findViewById(R.id.proximity_earpiece_rl);
		mEarpieceTips = (TextView) findViewById(R.id.earpiece_tips);
		mProximityNum = (TextView) findViewById(R.id.proximity_num);
		mProximityNum.setText("1");
		
		mRightBtn = (Button) findViewById(R.id.right_btn);
		mRightBtn.setOnClickListener(this);
		mWrongBtn = (Button) findViewById(R.id.wrong_btn);
		mWrongBtn.setOnClickListener(this);
		mRestartBtn = (Button) findViewById(R.id.restart_btn);
		mRestartBtn.setOnClickListener(this);
		
		
		mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAM.setMode(AudioManager.MODE_IN_CALL);
		mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, numSamples,
				AudioTrack.MODE_STATIC);
		
		 mMainHandler.sendEmptyMessage(PLAY_FREQOFTONE);
		  
	     try{
	        	mPSensor = mSensorMgr.getSensorList(Sensor.TYPE_PROXIMITY).get(0);
	     }catch(Exception e){
	        	e.printStackTrace();
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.proximity);
	        	builder.setMessage(R.string.get_proximity_fail);
	        	builder.create().show();
	     }
		 
	     mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor, SensorManager.SENSOR_DELAY_FASTEST);
	     if (false == mIsProximityRight) {
	            try {
	                Thread.sleep(300);
	                mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor,
	                        SensorManager.SENSOR_DELAY_FASTEST);
	            } catch (InterruptedException e) {

	            }
	            if (false == mIsProximityRight) {
	                mProximityNum.setText(R.string.init_proximity_sensor_fail);
	            }
	        }
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (null != mAudioTrack) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mAM.setMode(AudioManager.MODE_NORMAL);
		
		if (true == mIsProximityRight) {
            mSensorMgr.unregisterListener(mProximityListener);
        }
	}


	void genTone() {
		// fill out the array
		for (int i = 0; i < numSamples; ++i) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalised.
		int idx = 0;
		for (final double dVal : sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
	}

	void playSound() {
		mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
		mAudioTrack.play();
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
