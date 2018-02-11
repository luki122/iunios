
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
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class PhoneLoopbackTest2 extends Activity implements OnClickListener {

    boolean mIsRecording, mIsStop, mIsGetState;
    TextView mContentTv;
    AudioRecord mRecord;

    AudioTrack mTrack;

    AudioManager mAM;

    int mRecBuffSize, mTrackBuffSize;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "PhoneLoopbackTest2";

    RecordThread mRecThread;
//    private EarphonePluginReceiver mEarphonePluginReceiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        TextView recordTitle = (TextView) findViewById(R.id.test_title);
        recordTitle.setText(R.string.mic2test);
        //Gionee xiaolin 20120608 add for CR00622935 start
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Gionee xiaolin 20120613 modify for CR00624109 start
        mRecBuffSize = TestUtils.audioRecBufferSize;
        mTrackBuffSize = TestUtils.audioTrackBufferSize;
        mRecord = TestUtils.aRecord;
        mTrack = TestUtils.aTrack;
        // Gionee xiaolin 20120613 modify for CR00624109 end
        //Gionee xiaolin 20120608 add for CR00622935 end
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                mIsStop = true;
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mIsStop = true;
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
    
    // Gionee xiaolin 20120608 modify for CR00622935 start
    @Override
    public void onStart() {
        super.onStart();
        mAM.setMode(AudioManager.MODE_NORMAL);
        mAM.setParameters("MMIMic2=1");
        if (!mIsRecording) {
            new RecordThread().start();
        }
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - TestUtils.VOL_MINUS_INCALL, 0);
        }
    }
    @Override
    public void onPause() {
        mIsStop = true;
        super.onPause();
        mAM.setParameters("MMIMic2=0");
        mAM.setMode(AudioManager.MODE_NORMAL);
        // Gionee xiaolin 20120615 add for CR00624700 start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Gionee xiaolin 20120615 add for CR00624700 end
    }

    class RecordThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[mRecBuffSize];
                mRecord.startRecording();
                mTrack.play();
                mIsRecording = true;
                while (false == mIsStop) {
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        Log.e("lich", "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult ];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
                // Gionee xiaolin 20120613 modify for CR00624109 start
                mTrack.stop();
                mRecord.stop();
                // Gionee xiaolin 20120613 modify for CR00624109 end
                mIsRecording = false;
            } catch (Throwable t) {
            }
        }
    }
    // Gionee xiaolin 20120608 modify for CR00622935 end
    
    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
       
}
