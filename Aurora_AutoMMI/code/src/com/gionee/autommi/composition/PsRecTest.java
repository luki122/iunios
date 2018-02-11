package com.gionee.autommi.composition;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.widget.TextView;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.R;

import com.gionee.autommi.PSensorTest;

public class PsRecTest extends BaseActivity implements SensorEventListener {
    private Sensor pSensor;
    private SensorManager sensorManager;
    private boolean pass;
    private boolean farFlag;
    private boolean nearFlag;
    private TextView tip;
    
    private int duration = 2; // seconds
    private final int sampleRate = 8000;
    private int numSamples ;
    private double[] sample ;
    private final double freqOfTone = 1000; // hz
    private byte[] generatedSnd ;
	private static final String DURA = "dura";
    Thread tester = new Thread() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			genTone();
			playSound();
		}	
    };
	private AudioTrack track;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip);
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		pSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		((AutoMMI)getApplication()).recordResult(PSensorTest.TAG, "", "0");
		
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 2;
		compose();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		sensorManager.registerListener(this, pSensor, SensorManager.SENSOR_DELAY_UI);
		tester.start();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sensorManager.unregisterListener(this);
		track.stop();
		track.release();
		this.finish();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		int value = (int) event.values[0];
		if(0 == value) {
			nearFlag = true;
		} else if (1 <= value) {
			farFlag = true;
			value = 1;
		} else {
			// Something is wrong
		}
		tip.setText("距离值 ： " + event.values[0]);
		if(nearFlag && farFlag && !pass) {
			pass = true;
			((AutoMMI)getApplication()).recordResult(PSensorTest.TAG, "", "1");
		}
	}
    
	private void compose() {
		// TODO Auto-generated method stub
    	numSamples = duration * sampleRate;
    	sample = new double[numSamples];
    	generatedSnd = new byte[2 * numSamples];		
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
    	track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        track.write(generatedSnd, 0, generatedSnd.length);
        track.play();
    }

}
