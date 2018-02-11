
package gn.com.android.mmitest.item;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EarphoneLoopbackTest extends Activity implements OnClickListener {

    private boolean mIsRecording, mIsStop, mIsGetState;
    private TextView mContentTv, mTitleTv;
    private AudioRecord mRecord;

    private AudioTrack mTrack;

    private AudioManager mAM;

    private int mRecBuffSize, mTrackBuffSize;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "EarphoneLoopbackTest";

    private RecordThread mRecThread;
    private Handler mLpHander;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        setContentView(R.layout.common_textview);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.headsethook_note);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Gionee xiaolin 20120613 modify for CR00624109 start
		//Gionee <xuna><2013-06-03> delete for CR00873055 begin
        mRecBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
        mTrackBuffSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            mRecBuffSize);
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            mTrackBuffSize, AudioTrack.MODE_STREAM);
		//Gionee <xuna><2013-06-03> delete for CR00873055 end
        // Gionee xiaolin 20120613 modify for CR00624109 end
		try {
			mRecord.startRecording();
			mTrack.play();
		} catch (IllegalStateException ex) {

		}
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                cleanState();
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                cleanState();
                TestUtils.wrongPress(TAG, this);
                break;
            }
            
            case R.id.restart_btn: {
                cleanState();
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }
    
    private void cleanState() {
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);
        mIsStop = true;
		try {
                mTrack.stop();
                mRecord.stop();
                mTrack.release();
                mRecord.release();
		} catch (IllegalStateException ex) {

		}
        mAM.setMode(AudioManager.MODE_NORMAL);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - TestUtils.VOL_MINUS_INCALL, 0);
            mAM.setMode(AudioManager.MODE_IN_CALL);
        }
        Log.d(TAG, "---onStart---");
    }

    class RecordThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[mRecBuffSize];

                while (false == mIsStop) {
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    byte[] tmpBuf = new byte[bufferReadResult];
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                    mTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                
                mIsRecording = false;
                mAM.setMode(AudioManager.MODE_NORMAL);
            } catch (Throwable t) {
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event.getAction() == event.ACTION_DOWN) {
        	Log.d(TAG, "KEYCOD = "+ event.getKeyCode());
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_CALL: 
                case KeyEvent.KEYCODE_ENDCALL: 
                case KeyEvent.KEYCODE_HEADSETHOOK: 
                    mTitleTv.setText(R.string.headsethook_press);
                    mRightBtn.setEnabled(true);
                    if (!mIsRecording) {
                        new RecordThread().start();
                        mIsRecording = true;
                    }
                    break; 
            }
        }
        return true;
    }
    
}
