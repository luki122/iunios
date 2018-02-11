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

public class FrontCameraTest extends Activity implements OnClickListener {
    String TAG =  "FrontCameraTest";
    Button mRightBtn, mWrongBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent localIntent = new Intent("gn.com.android.mmitest.item.FrontCameraTest");
        try {
            startActivityForResult(localIntent, 1); 
        } catch (ActivityNotFoundException ex ) {

        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FrontCameraTest.this.setContentView(R.layout.common_textview_norestart);
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

import java.io.IOException;
import java.util.List;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
// Gionee liuyanbo 20111212 modified for CR00471988 start
import android.os.SystemClock;
// Gionee liuyanbo 20111212 modified for CR00471988 end

public class FrontCameraTest extends Activity implements OnClickListener, Camera.PictureCallback,
        SurfaceHolder.Callback {
    Camera mBackCra;

    Button mBackCraBtn;

    SurfaceView mSV;

    private SurfaceHolder mHolder = null;

    boolean mIsTaken;

    boolean mFlag;

//    private Handler mHandler;
//
//    private Runnable mRable;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "FrontCameraTest";

    private static int DELAY_TIME = 1000;

    private PreviewFrameLayout mFrameLayout;

    private Camera.Parameters mCameraParam;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        setContentView(R.layout.camera_test);
        mFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame_layout);
        mBackCraBtn = (Button) findViewById(R.id.camera_btn);
        mBackCraBtn.setOnClickListener(this);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mSV = (SurfaceView) findViewById(R.id.camera_preview);
//        mHandler = new Handler();
//        mRable = new Runnable() {
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                if (null != mBackCra) {
//                    mBackCra.startPreview();
//                    mBackCraBtn.setEnabled(true);
//                }
//            }
//
//        };
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
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mBackCra != null) {
//                mHandler.removeCallbacks(mRable);
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
                // Gionee liuyanbo 20111212 modified for CR00471988 start
//                if (false == mIsTaken) {
//                    mBackCra.takePicture(null, null, this);
//                    mBackCraBtn.setEnabled(false);
//                    mRightBtn.setEnabled(true);
//                } else {
//                    if (null != mBackCra) {
//                        mBackCra.startPreview();
//                        mBackCra.setParameters(mCameraParam);
//                        mBackCraBtn.setEnabled(true);
//                        mBackCraBtn.setText(R.string.take_picture);
//                        mIsTaken = false;
//                    }
//                }
//                
                if (mBackCraBtn.isClickable()) {
                    mBackCraBtn.setClickable(false);
                    mBackCraBtn.setEnabled(false);
                    if (false == mIsTaken) {
                        mBackCra.takePicture(null, null, this);
                        mRightBtn.setEnabled(true);
                    } else {
                        if (null != mBackCra) {
                            mBackCra.startPreview();
                            mBackCra.setParameters(mCameraParam);
                            SystemClock.sleep(200);
                            mBackCraBtn.setEnabled(true);
                            mBackCraBtn.setClickable(true);
                            mBackCraBtn.setText(R.string.take_picture);
                            mIsTaken = false;
                        }
                    }
                }
                // Gionee liuyanbo 20111212 modified for CR00471988 start
                break;
            }
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (mBackCra != null) {
//                    mHandler.removeCallbacks(mRable);
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
//                    mHandler.removeCallbacks(mRable);
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
            mBackCra = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
            mCameraParam = mBackCra.getParameters();
        } catch (RuntimeException e) {
            try {
                if (null != mBackCra) {
                    mBackCra.release();
                    mBackCra = null;
                }
                Thread.sleep(500);
                Log.e(TAG, "second open");
                mBackCra = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception sube) {
                Log.e(TAG, "fail to open camera");
                sube.printStackTrace();
                mBackCra = null;
            }
        }
        
        if (mBackCra != null) {
            mCameraParam.setAntibanding(Parameters.ANTIBANDING_50HZ);
            mCameraParam.setPictureFormat(PixelFormat.JPEG);
            List<Size> list = mCameraParam.getSupportedPictureSizes();
            if (list.size() > 0) {
                Size maxSize = list.get(list.size() - 1);
                mCameraParam.setPictureSize(maxSize.width, maxSize.height);
                mFrameLayout.setAspectRatio((double) maxSize.width / maxSize.height);
                List<Size> sizes = mCameraParam.getSupportedPreviewSizes();
                Size optimalSize = null;
                if (maxSize != null && maxSize.height != 0)
                    optimalSize = getOptimalPreviewSize(sizes, (double) maxSize.width
                            / maxSize.height);
                if (optimalSize != null) {
                    mCameraParam.setPreviewSize(optimalSize.width, optimalSize.height);
                }
                mCameraParam.set("fps-mode", 0); // Frame rate is normal
                mCameraParam.set("cam-mode", 0); // Cam mode is preview
            }

            try {
                mBackCra.setPreviewDisplay(mHolder);
                mBackCra.setParameters(mCameraParam);
                Log.e(TAG, "start preview");
                mBackCra.startPreview();
                mFlag = true;
            } catch (IOException e) {
                mBackCra.release();
                // mCamera = null;
            }
        } else {
            // Gionee liuyanbo 20111212 modified for CR00471988 start
            mBackCraBtn.setClickable(false);
            // Gionee liuyanbo 20111212 modified for CR00471988 end
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
        // TODO Auto-generated method stub
//        mHandler.removeCallbacks(mRable);
//        mHandler.postDelayed(mRable, DELAY_TIME);
        mIsTaken = true;
        // Gionee liuyanbo 20111212 modified for CR00471988 start
        SystemClock.sleep(200);
        mBackCraBtn.setEnabled(true);
        mBackCraBtn.setClickable(true);
        // Gionee liuyanbo 20111212 modified for CR00471988 end
        mBackCraBtn.setText(R.string.preview);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.05;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        Display display = getWindowManager().getDefaultDisplay();
        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            // We don't know the size of SurefaceView, use screen height
            WindowManager windowManager = (WindowManager) getSystemService(this.WINDOW_SERVICE);
            targetHeight = windowManager.getDefaultDisplay().getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            Log.v(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        } else {
            Log.v(TAG, String.format("Optimal preview size is %sx%s", optimalSize.width,
                    optimalSize.height));
        }
        return optimalSize;
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

}
*/
