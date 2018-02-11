package gn.com.android.mmitest.item;

import java.util.ArrayList;
import java.util.Arrays;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

//Gionee liss 20111215 add for CR00478802 start
import android.view.inputmethod.InputMethodManager;

/** xiaolin
import com.mediatek.featureoption.FeatureOption;
*/

//Gionee liss 20111215 add for CR00478802 end

public class KeysTest extends Activity implements View.OnClickListener{
    public TextView mPowerTv;
    private int mPowerInt;
    public TextView mVolumeUpTv;
    private int mVolumeUpInt;
    public TextView mVolumeDownTv;
    private int mVolumeDownInt;
    public TextView mMenuTv;
    private int mMenuInt;
    public TextView mHomeTv;
    private int mHomeInt;
    public TextView mBackTv;
    private int mBackInt;
    public TextView mCameraTv;
    private int mCameraInt;
    private Button mQuitBtn;
    private Resources mRs;
    private int TEST_COLOR;
    private TextView mSearchTv;
    private int mSearchInt;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private static final int TONE_LENGTH_MS = 85;
    private boolean mIsNewCount;
    
    private ArrayList<View> mViewHolder = new ArrayList<View> (); 
    private ArrayList<String> mItems;
    private KeysAdapter mKeysAdapter;
    private ArrayList<Integer> mKeyState;
    private GridView mGrid;
    private ArrayList<String> mItemKeys;
    private int mKeyCount;
    private int mKeyPressCount;
    
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "KeysTest";

    // Gionee liss 20111215 add for CR00478802 start
    public InputMethodManager mInputMethondManager;
    // Gionee liss 20111215 add for CR00478802 end
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keys_test);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
       // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        
        mRs = this.getResources();
        TEST_COLOR = mRs.getColor(R.color.test_blue);

        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mGrid = (GridView) findViewById(R.id.key_gridview);
        /** xiaolin
        // Gionee liss 20111215 add for CR00478802 start
        if (FeatureOption.GN_IME_HARDKEYBOARD_SUPPORT) {
            mGrid.setNumColumns(6);
        }
        // Gionee liss 20111215 add for CR00478802 end
        */
    }
    
    @Override
    public void onStart() {
        super.onStart();
        newToneGenerator();
        TestUtils.configKeyTestArrays(this);
        mItems = TestUtils.getKeyItems(this);
        mItemKeys = TestUtils.getKeyItemKeys(this);
        mKeyCount = mItems.size();
        mKeyState = new ArrayList<Integer>();
        
        // Gionee liss 20111215 add for CR00478802 start
        mInputMethondManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        //add by zhangxiaowei start
        //mInputMethondManager.setmKeysTestenable(true);
       //add by zhangxiaowei end
        
        /** xiaolin
        if (!FeatureOption.GN_IME_HARDKEYBOARD_SUPPORT
                && FeatureOption.GN_OVERSEA_PRODUCT) {
            mKeyCount = 7;
        }
        */
        // Gionee liss 20111215 add for CR00478802 end
        
        for (int i=0; i < mKeyCount; i++) {
            mKeyState.add(i, 0);
        }
        Log.e("lich", "mKeyCount = " + mKeyCount);
        Log.e("lich", "mKeyState.size()" + mKeyState.size());
        
        // Gionee liss 20111215 modify for CR00478802 start
        /*
        for (int i=0; i<mItems.size(); i++) {
            TextView v = (TextView) KeysTest.this.getLayoutInflater().inflate(R.layout.gridview_item, mGrid, false);
         */
        for (int i = 0; i < mKeyCount; i++) {
            TextView v = null;
            /** xiaolin
            if (FeatureOption.GN_IME_HARDKEYBOARD_SUPPORT) {
                v = (TextView) KeysTest.this.getLayoutInflater().inflate(
                        R.layout.key_item, mGrid, false);
            } else { 
            */
                v = (TextView) KeysTest.this.getLayoutInflater().inflate(
            
                	R.layout.gridview_item, mGrid, false);
            /** xiaolin
            }
            */
            // Gionee liss 20111215 modify for CR00478802 end
            
            v.setText(mItems.get(i));
            mViewHolder.add(v);
        }
        mKeysAdapter = new KeysAdapter();
        mGrid.setAdapter(mKeysAdapter);
    }
    
    
    @Override
    public void onStop() {
   
       //add by zhangxiaowei start
      //  mInputMethondManager.setmKeysTestenable(false);
      //add by zhangxiaowei end
   

        super.onStop();
        if (null != mViewHolder) {
            mViewHolder.clear();
        }
        releaseToneGenerator();
    }    
    
    private void newToneGenerator() {
        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    // we want the user to be able to control the volume of the dial tones
                    // outside of a call, so we use the stream type that is also mapped to the
                    // volume control keys for this activity
                    mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 240);
                    setVolumeControlStream(AudioManager.STREAM_MUSIC);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }
    }
    
    private void releaseToneGenerator() {
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
    }
    
    void playTone(int tone) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
            || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: "+tone);
                return;
            }
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
        }
    }
    



    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.restart_btn: {
//                mRightBtn.setEnabled(false);
//                mWrongBtn.setEnabled(false);
//                mRestartBtn.setEnabled(false);
                //TestUtils.restart(this, TAG);
                restartKeyTest();
                break;
            }
            
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
        }
    }
    
    void restartKeyTest() {
//        mItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
//                R.array.single_test_keys)));
//        
        mRightBtn.setEnabled(false);
        mKeyPressCount = 0;
        TestUtils.configKeyTestArrays(this);
        mItems = TestUtils.getKeyItems(this);
        mItemKeys = TestUtils.getKeyItemKeys(this);
        mKeyCount = mItems.size();
        mKeyState.clear();
        mViewHolder.clear();
        
        /** xiaolin
        // Gionee liss 20111215 add for CR00478802 start
        if (!FeatureOption.GN_IME_HARDKEYBOARD_SUPPORT
                && FeatureOption.GN_OVERSEA_PRODUCT) {
            mKeyCount = 7;
        }
        // Gionee liss 20111215 add for CR00478802 end
        
         */

        for (int i=0; i < mKeyCount; i++) {
            mKeyState.add(i, 0);
        }
        Log.e("lich", "mKeyCount = " + mKeyCount);
        Log.e("lich", "mKeyState.size()" + mKeyState.size());
        
        // Gionee liss 20111215 modify for CR00478802 start
        /*
        for (int i=0; i<mItems.size(); i++) {
            TextView v = (TextView) KeysTest.this.getLayoutInflater().inflate(R.layout.gridview_item, mGrid, false);
         */
        for (int i = 0; i < mKeyCount; i++) {
            TextView v = null;
            /** xiaolin
            if (FeatureOption.GN_IME_HARDKEYBOARD_SUPPORT) {
                v = (TextView) KeysTest.this.getLayoutInflater().inflate(
                        R.layout.key_item, mGrid, false);
            } else { */
                v = (TextView) KeysTest.this.getLayoutInflater().inflate(
                        R.layout.gridview_item, mGrid, false);
            /** xiaolin    
            }
            */
            
            // Gionee liss 20111215 modify for CR00478802 end
            v.setText(mItems.get(i));
            mViewHolder.add(v);
        }
        mGrid.setAdapter(mKeysAdapter);
        
    }
    
    
    
    
    
    public void rightShouldEnable() {
        mKeyPressCount++;
        Log.e("lich", "mKeyState.size == " + mKeyState.size());
        if (mKeyCount == mKeyPressCount) {
            mRightBtn.setEnabled(true);
        }
    }
    
    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event.getAction() == event.ACTION_DOWN) {

            playTone(ToneGenerator.TONE_DTMF_1);
            int code = event.getKeyCode();
           
            // Gionee liss 20111215 modify for CR00478802 start
           // for (int i=0; i<mItemKeys.size(); i++) {
            int length = mItemKeys.size();
            
            /** xiaolin
            if (!FeatureOption.GN_IME_HARDKEYBOARD_SUPPORT
                    && FeatureOption.GN_OVERSEA_PRODUCT) {
                length = 7;
            }
            */
            
            for (int i = 0; i < length; i++) {
                // Gionee liss 20111215 modify for CR00478802 end
                
                if (mItemKeys.get(i).equals(String.valueOf(code))) {
                    if (mKeyState.get(i).equals(0)) {
                        mViewHolder.get(i).setBackgroundResource(R.drawable.grid_view_item_press);
                        // Gionee xiaolin 20120604 modify for CR00616306 start 
                        mKeyState.set(i, 1);
                        // Gionee xiaolin 20120604 modify for CR00616306 end
                    } else if (mKeyState.get(i).equals(1)) {
                        mViewHolder.remove(i);
                        mItemKeys.remove(i);
                        mKeyState.remove(i);
                        mGrid.setAdapter(mKeysAdapter);
                        rightShouldEnable();
                    }
                    return true;
                }
            }
            
//            switch (event.getKeyCode()) {
//                case KeyEvent.KEYCODE_POWER: {
//                    if (0 == mPowerInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_POWER))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mPowerInt++;
//                    } else if (1 == mPowerInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_POWER));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mPowerInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_VOLUME_UP: {
//                    if (0 == mVolumeUpInt) {
//                        Log.e("lich", "mItemKeys.sze  " + mItemKeys.size());
//                        Log.e("lich", "mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_VOLUME_UP))" + mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_VOLUME_UP)));
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_VOLUME_UP))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mVolumeUpInt++;
//                    } else if (1 == mVolumeUpInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_VOLUME_UP));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mVolumeUpInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                } 
//                case KeyEvent.KEYCODE_VOLUME_DOWN: {
//                    if (0 == mVolumeDownInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_VOLUME_DOWN))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mVolumeDownInt++;
//                    } else if (1 == mVolumeDownInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_VOLUME_DOWN));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mVolumeDownInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_MENU: {
//                    if (0 == mMenuInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_MENU))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mMenuInt++;
//                    } else if (1 == mMenuInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_MENU));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mMenuInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_HOME: {
//                    if (0 == mHomeInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_HOME))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mHomeInt++;
//                        rightShouldEnable();
//                    } else if (1 == mHomeInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_HOME));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mHomeInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_BACK : {
//                    if (0 == mBackInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_BACK))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mBackInt++;
//                    } else if (1 == mBackInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_BACK));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mBackInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_CAMERA: {
//                    if (0 == mCameraInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_CAMERA))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mCameraInt++;
//                    } else if (1 == mCameraInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_CAMERA));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mCameraInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//                case KeyEvent.KEYCODE_SEARCH: {
//                    if (0 == mSearchInt) {
//                        mViewHolder.get(mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_SEARCH))).setBackgroundResource(R.drawable.grid_view_item_press);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mSearchInt++;
//                    } else if (1 == mSearchInt) {
//                        int index = mItemKeys.indexOf(String.valueOf(KeyEvent.KEYCODE_SEARCH));
//                        mViewHolder.remove(index);
//                        mItemKeys.remove(index);
//                        mGrid.setAdapter(mKeysAdapter);
//                        mSearchInt++;
//                        rightShouldEnable();
//                    }
//                    break;
//                }
//            }
        }
        return true;
    }
    
    
    class KeysAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return mViewHolder.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mViewHolder.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
                return mViewHolder.get(position);
        }
        
    }
}
