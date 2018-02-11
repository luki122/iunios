package gn.com.android.mmitest.item;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.content.ActivityNotFoundException;

public class BackCameraTest extends Activity implements OnClickListener {
    String TAG =  "BackCameraTest";
    Button mRightBtn, mWrongBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent localIntent = new Intent("gn.com.android.mmitest.item.BackCameraTest");
        try {
            startActivityForResult(localIntent, 1); 
        } catch (ActivityNotFoundException ex ) {

        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BackCameraTest.this.setContentView(R.layout.common_textview_norestart);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.wrong_btn: {
                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.right_btn: {
                TestUtils.rightPress(TAG, this);
                break;
            }
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}









/*
package gn.com.android.mmitest.item;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BackCameraTest extends Activity implements OnClickListener, Camera.PictureCallback,
        SurfaceHolder.Callback {
    private Camera mBackCra;

    private Camera.Parameters mCameraParam;
    
    private Button mBackCraBtn;

    private SurfaceView mSV;

    private SurfaceHolder mHolder = null;

    private boolean mIsTaken;

    private boolean mFlag, mIsFlashOn;

    private Handler mHandler;

    private Runnable mRable, mWaitRable;
    private FocusRectangle mFocusRectangle;
    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private Rect mPreviewRect = new Rect();
    private static final String TAG = "BackCameraTest";

//    private static int DELAY_TIME = 200;
    private static int WAIT_TIME = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
        lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        setContentView(R.layout.camera_test);
        mBackCraBtn = (Button) findViewById(R.id.camera_btn);
        mBackCraBtn.setOnClickListener(this);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mFocusRectangle = (FocusRectangle) findViewById(R.id.focus_rectangle);
        mSV = (SurfaceView) findViewById(R.id.camera_preview);
        mHandler = new Handler();
//        mRable = new Runnable() {
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                if (null != mBackCra) {
//                    mBackCra.startPreview();
//                    mBackCra.setParameters(mCameraParam);
//                    mBackCraBtn.setEnabled(true);
//                }
//            }
//
//        };
        mWaitRable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                clearFocusIndicator();
                mBackCra.takePicture(null, null, BackCameraTest.this);
            }

        };
    }

    @Override
    public void onStart() {
        super.onResume();
        mHolder = mSV.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mBackCraBtn.setEnabled(true);
        mBackCraBtn.setText(R.string.take_picture);
    }

    int getFrontCameraId() {
        CameraInfo ci = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    
    private void clearFocusIndicator() {
        if(mFocusRectangle != null) {
            mFocusRectangle.clear();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mBackCra != null) {
                mHolder.removeCallback(this);
                mBackCra.stopPreview();
                mBackCra.release();
                mBackCra = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.camera_btn: {
                if (false == mIsTaken) {
                    mBackCra.autoFocus(mAutoFocusCallback);
                    mFocusRectangle.showStart();
                    mBackCraBtn.setEnabled(false);
                } else {
                    if (null != mBackCra) {
                        if (true == mIsFlashOn) {
                            mCameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                            mBackCra.setParameters(mCameraParam);
                            mIsFlashOn = false;
                        } else {
                            mCameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            mBackCra.setParameters(mCameraParam);
                            mIsFlashOn = true;
                        }
                        mBackCra.startPreview();
                        mBackCra.setParameters(mCameraParam);
                        mBackCraBtn.setEnabled(true);
                        mBackCraBtn.setText(R.string.take_picture);
                        mIsTaken = false;
                    }
                }
                break;
            }
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (mBackCra != null) {
                    mHolder.removeCallback(this);
                    mBackCra.stopPreview();
                    mBackCra.release();
                    mBackCra = null;
                }

                TestUtils.rightPress(TAG, this);
                break;
            }
            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (mBackCra != null) {
                    mHolder.removeCallback(this);
                    mBackCra.stopPreview();
                    mBackCra.release();
                    mBackCra = null;
                }

                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.delayRestart(this, TAG, 1500);
                break;
            }
        }
    }

    public void startCamera() {

        if (mFlag) {
            Log.e(TAG, "stop & close");
            if (mBackCra != null) {
                mBackCra.stopPreview();
                mBackCra.release();
                mFlag = false;
            }

        }
        try {
            Log.e(TAG, "open");
            mBackCra = Camera.open(CameraInfo.CAMERA_FACING_BACK);
        } catch (RuntimeException e) {
            try {
                if (null != mBackCra) {
                    mBackCra.release();
                    mBackCra = null;
                }
                Thread.sleep(500);
                Log.e(TAG, "second open");
                mBackCra = Camera.open(CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception sube) {
                Log.e(TAG, "fail to open camera");
                sube.printStackTrace();
                mBackCra = null;
            }
        }
        if (mBackCra != null) {
            mCameraParam = mBackCra.getParameters();
            mCameraParam.setAntibanding(Parameters.ANTIBANDING_50HZ);
            mCameraParam.setPictureFormat(PixelFormat.JPEG);
            mCameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            List<Size> list = mCameraParam.getSupportedPictureSizes();
            if (list.size() > 0) {
                Size maxSize = list.get(list.size() - 1);
                mCameraParam.setPictureSize(maxSize.width, maxSize.height);
            }
            try {
                mBackCra.setPreviewDisplay(mHolder);
                Log.e(TAG, "start preview");
                mBackCra.startPreview();
                mBackCra.setParameters(mCameraParam);
                mFlag = true;
            } catch (IOException e) {
                mBackCra.release();
                // mCamera = null;
            }
        } else {
            mBackCraBtn.setEnabled(false);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if (-1 < getFrontCameraId()) {
            startCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

// will to be adding 

///////////////////////////////////////////////////////
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getExternalStorageState())) {
//			try {
//				FileOutputStream outputStream = null;
//				File root = Environment.getExternalStorageDirectory();
//				File path = new File(root, "/data/mmi/");
//				if (!path.exists()) {
//					path.mkdirs();
//				}
//				File file = new File(root, "/data/mmi/"
//						+ System.currentTimeMillis() + ".jpg");
//				outputStream = new FileOutputStream(file.toString());
//				outputStream.write(data);
//				outputStream.close();
//				Intent it = new Intent(BackCameraTest.this, ImageViewer.class);
//				it.putExtra("image_file", "file://" + file.toString());
//				startActivity(it);
//				Log.e("lich", "well i reach here");
//			} catch (IOException e) {
//
//			}
//		}
///////////////////////////////////////////////////////    	
    	
    	
        // TODO Auto-generated method stub
        mRightBtn.setEnabled(true);
        mFocusRectangle.clear();
//        mHandler.removeCallbacks(mRable);
//        mHandler.postDelayed(mRable, DELAY_TIME);
        mIsTaken = true;
        mBackCraBtn.setEnabled(true);
        mBackCraBtn.setText(R.string.preview);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
    private final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, android.hardware.Camera camera) {
            if (focused) {
                mFocusRectangle.showSuccess();
                mHandler.postDelayed(mWaitRable, WAIT_TIME);
            } else {
                mFocusRectangle.showFail();
            }
        }
    }
    
}
*/
