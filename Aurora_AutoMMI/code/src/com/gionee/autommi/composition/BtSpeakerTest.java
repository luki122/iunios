package com.gionee.autommi.composition;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.BluetoothTest;

public class BtSpeakerTest extends BaseActivity {
	
	private static final String EXTRA_MAC = "mac";
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver sentinel, resultProc;
	private String expDev;
	private boolean found;

	private int duration = 2; // seconds
	private final int sampleRate = 8000;
	private int numSamples;
	private double[] sample;
	private final double freqOfTone = 1000; // hz
	private byte[] generatedSnd;
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
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 2;
		compose();
		

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		expDev = it.getStringExtra(EXTRA_MAC);
		sentinel = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub

			}		
		};
		
		resultProc = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
				if ( rssi >= 128) {
					rssi -= 256;
				}
				Log.d(BluetoothTest.TAG, dev.getAddress());
				Log.d(BluetoothTest.TAG, " "+ rssi); 
				if(expDev.equalsIgnoreCase(dev.getAddress())) {
					found = true;
					((AutoMMI)getApplication()).recordResult(BluetoothTest.TAG, "" + rssi,"1");
				}
			}		
		};
		
		((AutoMMI)getApplication()).recordResult(BluetoothTest.TAG, "", "0");
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
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, numSamples,
				AudioTrack.MODE_STATIC);
		track.write(generatedSnd, 0, generatedSnd.length);
		track.play();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		track.stop();
		track.release();
		this.unregisterReceiver(sentinel);
		this.unregisterReceiver(resultProc);
		btAdapter.cancelDiscovery();
		this.finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		tester.start();
		
		this.registerReceiver(sentinel, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		this.registerReceiver(resultProc, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		btAdapter.startDiscovery();
	}

}
