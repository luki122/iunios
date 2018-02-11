package com.gionee.zoom.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;
import android.app.ActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.gionee.zoom.DynamicZoomControl;
import com.gionee.zoom.ImageZoomView;
import com.gionee.zoom.LongPressZoomListener;
import com.gionee.zoom.PinchZoomListener;
import com.android.contacts.R;
import com.gionee.zoom.utils.ZoomUtils;

//Gionee huangwenting 2012-06-05 add for CR00614312 begin
import android.content.ContentResolver;
import android.provider.MediaStore;
//Gionee huangwenting 2012-06-05 add for CR00614312 end
//Gionee <fangbin> <2013-04-22> added for CR00793758 start
import android.view.WindowManager;
//Gionee <fangbin> <2013-04-22> added for CR00793758 end
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import android.os.SystemProperties;
/**
 * Activity for zoom tutorial 1
 */
public class ZoomClipViewActivity extends AuroraActivity {

    private static final String TAG = "ZoomImageActivity";

    /** Image zoom view */
    private ImageZoomView mZoomView;

    /** Zoom control */
    private DynamicZoomControl mZoomControl;

    /** Decoded bitmap image */
    private Bitmap mBitmap;

    /** On touch listener for zoom view */
    private LongPressZoomListener mZoomListener;
    
    private PinchZoomListener mPinchZoomListener;
    private ImageView mLeftRotateView = null;
    private ImageView mRightRotateView = null;
    //aurora add liguangyu 20131102 for clip photo start
    private ImageView mClipView = null;
    //aurora add liguangyu 20131102 for clip photo end
    
    private static final int DEFALUT_ROTATE_DEGREE = 90;
    
    public static int WINDOW_WIDTH = 0;
    public static int WINDOW_HEIGHT = 0;
    public static int DEFALUT_CLIP_SIZE = 256;
    public static final String KEY_OUTPUT_FORMAT = "outputFormat";
    public static final String KEY_CROPPED_RECT = "cropped-rect";
    public static final String KEY_DATA = "data";
    public static final String GN_KEY_DATA = "gn_data";
    // Gionee fangbin 20120809 modified for CR00671959 start
    public static final String DOWNLOAD_STRING = "/download";
    private File DOWNLOAD_BUCKET = null;
    // Gionee fangbin 20120809 modified for CR00671959 end
    private static final String TIME_STAMP_NAME = "'IMG'_yyyyMMdd_HHmmss";
    private AuroraProgressDialog mDialog = null;
    private static final int CROP_SUCCESS = 0;
    private static final int CROP_ERROR = 1;
    
    public static int mRadius = 501;
    public static int mRadiusWidth = 9;
    public static int mCcy = 229 * 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	  //aurora change liguangyu 20131102 for clip photo start 
        // Gionee <fangbin> <2013-07-03> add for CR00832695 begin
        // Gionee <fangbin> <2013-07-05> modify for CR00833305 begin
        // Gionee <fangbin> <2013-07-30> modify for CR00844329 begin 
//        setTheme(R.style.GnLightThemeCommon);
        // Gionee <fangbin> <2013-07-30> modify for CR00844329 end 
        // Gionee <fangbin> <2013-07-05> modify for CR00833305 end
        // Gionee <fangbin> <2013-07-03> add for CR00832695 end
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
//        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        // Gionee <fangbin> <2013-04-22> added for CR00793758 start
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Gionee <fangbin> <2013-04-22> added for CR00793758 end
        super.onCreate(savedInstanceState);

        setAuroraContentView(R.layout.gn_zoom_clip_view_activity);
        initDisplayMetrics();
        
//        ActionBar actionBar = getAuroraActionBar();
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
//        actionBar.setTitle(R.string.crop_picture);
//        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_call_menu_pop_bg));
//        // Gionee fangbin 20120627 added for CR00627113 start
//        actionBar.setIcon(null);
//        actionBar.setDisplayUseLogoEnabled(false);
//        // Gionee fangbin 20120627 added for CR00627113 end
        getAuroraActionBar().setTitle(R.string.crop_picture);
  	  //aurora change liguangyu 20131102 for clip photo end
        
        // aurora wangth 20140522 add for 2K begin
        Context context = ZoomClipViewActivity.this;
        mRadius = context.getResources().getDimensionPixelOffset(R.dimen.aurora_zoom_clip_radius);
        mCcy = context.getResources().getDimensionPixelOffset(R.dimen.aurora_zoom_clip_ccy);
        Log.d(TAG, "mRadius = " + mRadius + "   mCcy = " + mCcy);
        // aurora wangth 20140522 add for 2K begin
        
        mLeftRotateView = (ImageView) findViewById(R.id.rotationLeft);
        mLeftRotateView.setOnClickListener(mClickListener);
        mRightRotateView = (ImageView) findViewById(R.id.rotationRight);
        mRightRotateView.setOnClickListener(mClickListener);
        // Gionee <fangbin> <2013-08-08> add for CR00850751 begin
        mLeftRotateView.setVisibility(View.GONE);
        mRightRotateView.setVisibility(View.GONE);
        // Gionee <fangbin> <2013-08-08> add for CR00850751 end
        //aurora add liguangyu 20131102 for clip photo start        
        mClipView = (ImageView) findViewById(R.id.clip_check_button);
        mClipView.setOnClickListener(mClickListener);
        //aurora add liguangyu 20131102 for clip photo end

        mZoomControl = new DynamicZoomControl();

        mZoomListener = new LongPressZoomListener(getApplicationContext());
        mZoomListener.setZoomControl(mZoomControl);
        mPinchZoomListener = new PinchZoomListener(getApplicationContext());
        mPinchZoomListener.setZoomControl(mZoomControl);
        
        
        mZoomView = (ImageZoomView)findViewById(R.id.zoomview);
        mZoomView.setZoomState(mZoomControl.getZoomState());
        mZoomView.setDrawingCacheEnabled(true);

        mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
//        resetZoomState();
        mZoomView.setOnTouchListener(mPinchZoomListener);
        initZoomData();
        // Gionee <fangbin> <2013-07-30> modify for CR00844329 begin 
        // Gionee <fangbin> <2013-08-08> delete for CR00850751 begin
//        setOptionsMenuHideMode(true);
        // Gionee <fangbin> <2013-08-08> delete for CR00850751 end
        // Gionee <fangbin> <2013-07-30> modify for CR00844329 end 
    }
    
    private OnClickListener mClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
            case R.id.rotationLeft:
                rotate(-DEFALUT_ROTATE_DEGREE);
                break;

            case R.id.rotationRight:
                rotate(DEFALUT_ROTATE_DEGREE);
                break;
            //aurora add liguangyu 20131102 for clip photo start        
            case R.id.clip_check_button:
                showDialog();
                saveBitmap();
                v.setEnabled(false);//aurora add zhouxiaobing 20140419 for click more caused crash
                break;
            //aurora add liguangyu 20131102 for clip photo end
                
            default:
                break;
            }
        }
    };
    
    @Override
    public void onPause(){
        super.onPause();
        
        // Kill when pressing home key.
        //this.finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mBitmap) {
            mBitmap.recycle();
        }
        mZoomView.setOnTouchListener(null);
        mZoomControl.getZoomState().deleteObservers();
        closeDialog();
    }

    // Gionee <fangbin> <2013-08-08> modify for CR00850751 begin
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        getMenuInflater().inflate(R.menu.crop, menu);
        return super.onCreateOptionsMenu(menu);
    }
    // Gionee <fangbin> <2013-08-08> modify for CR00850751 end
    
    // Gionee <fangbin> <2013-08-08> add for CR00850751 begin
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        menu.findItem(R.id.cancel).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.findItem(R.id.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
        return super.onPrepareOptionsMenu(menu);
    }
    // Gionee <fangbin> <2013-08-08> add for CR00850751 end

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
//            case R.id.reset:
//                resetZoomState();
//                break;
            case R.id.save:
                showDialog();
                saveBitmap();
                break;
        }
        return true;
    }

    /**
     * Reset zoom state and notify observers
     */
    private void resetZoomState() {
	    int w = WINDOW_WIDTH;
	    int h = WINDOW_HEIGHT - 240;
	    int bw = mBitmap.getWidth();
	    int bh = mBitmap.getHeight();
	    float x=0.5f,y=0.591f;
	    float scale = 0f;
	    float r = (float)bw/bh;
	    float min = Math.min(bw, bh);
      	if(r<980f/h) {
      		scale = 980f/(h*r);
      	} else if(r> 1080/980f){
      		scale = 980*r/1080f;
      		y = 0.65f;
      	} else {
      		scale = 1f;
      	}	
	    Log.e("liumxxx", "bw = " + bw + " bh=" + bh + " scale =" + scale);
        mZoomControl.getZoomState().setPanX(x);
        mZoomControl.getZoomState().setPanY(y);   
        mZoomControl.getZoomState().setZoom(scale);
        mZoomControl.getZoomState().notifyObservers();
    }

    private void initDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        WINDOW_HEIGHT = metrics.heightPixels;
        WINDOW_WIDTH = metrics.widthPixels;
//        Toast.makeText(this, "WINDOW_HEIGHT = " + WINDOW_HEIGHT + " WINDOW_WIDTH=" + WINDOW_WIDTH, Toast.LENGTH_LONG);
        Log.e("liumxxx", "WINDOW_HEIGHT = " + WINDOW_HEIGHT + " WINDOW_WIDTH=" + WINDOW_WIDTH);
    }
    
    private void saveBitmap() {
    	//aurora add liguangyu 20131226 for bug #1497 start
//        mBitmap = mZoomView.getDrawingCache(true);
    	
        //1.构建Bitmap    
        int w = WINDOW_WIDTH;    
        int h = WINDOW_HEIGHT;                
        Bitmap Bmp = Bitmap.createBitmap( w, h, Config.ARGB_8888 );        
            
        //2.获取屏幕    
        View decorview = getWindow().getDecorView();
        try {
            decorview.setDrawingCacheEnabled(true);
            Bmp = decorview.getDrawingCache();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed! Save has some error!", Toast.LENGTH_SHORT);
            return;
        }
        
        int r = mRadius - mRadiusWidth * 2 - 2;//(h >= w ? w : h) * 963 / 2160 ;
//        mBitmap = Bitmap.createBitmap(Bmp, w/2 - r, h*927/1920 - r, 2 * r, 2 * r);
        int width = 2 * r;    
        if(isHonor6()) {
        	h += 3* 35;
        }
        if( width > 500) {
	        float scaleWidth = 500f/width; 
	        Matrix matrix = new Matrix();        
	        matrix.postScale(scaleWidth, scaleWidth);
//	        mBitmap = Bitmap.createBitmap(Bmp, w/2 - r, h*927/1920 - r, 2 * r, 2 * r, matrix, true);
	        mBitmap = Bitmap.createBitmap(Bmp, w/2 - r, getAuroraActionBar().getHeight() + mCcy - r, 2 * r, 2 * r, matrix, true);
        } else {
//        	mBitmap = Bitmap.createBitmap(Bmp, w/2 - r, h*927/1920 - r, 2 * r, 2 * r);
        	mBitmap = Bitmap.createBitmap(Bmp, w/2 - r, getAuroraActionBar().getHeight() + mCcy - r, 2 * r, 2 * r);
        }
        Bmp.recycle();
        Bmp = null;
        //aurora add liguangyu 20131226 for bug #1497 end
                
        if (null == mBitmap) {
            closeDialog();
            Toast.makeText(this, "Save Failed! Save has some error!", Toast.LENGTH_SHORT);
            return;
        }            
        
        try {
            // Gionee fangbin 20120716 added for CR00636040 start
            // Gionee fangbin 20120809 modified for CR00671959 start
            final boolean flag = ZoomUtils.isAvaiableSpace(mBitmap, convertExtensionToCompressFormat(getFileExtension()), this);
            // Gionee fangbin 20120809 modified for CR00671959 end
            if (!flag) {
                closeDialog();
                Log.e("liumxxx","flag!!!!");
                Toast.makeText(this, getString(R.string.crop_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            // Gionee fangbin 20120716 added for CR00636040 end
            
            // Gionee fangbin 20120809 added for CR00671959 start
            if (null == ZoomUtils.getPrefixPath(this, ZoomUtils.DEFAULT_REDUNDANCY_VALUE)) {
                closeDialog();
                Log.e("liumxxx","null!!!!");
                Toast.makeText(this, getString(R.string.crop_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            DOWNLOAD_BUCKET = new File(ZoomUtils.getPrefixPath(this, ZoomUtils.DEFAULT_REDUNDANCY_VALUE)+DOWNLOAD_STRING);
            // Gionee fangbin 20120809 added for CR00671959 end
            if (!DOWNLOAD_BUCKET.isDirectory() && !DOWNLOAD_BUCKET.mkdirs()) {
                throw new RuntimeException("cannot create download folder");
            }
            
            new Thread() {
                public void run() {
                    // Gionee fangbin 20121029 modified for CR00712006 start
                    File candidate = null;
                    int quality = 100;
                    try {
                        while (true) {
                            long now = System.currentTimeMillis();
                            String fileExtension = getFileExtension();
                            String filename = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(now));
                            candidate = new File(DOWNLOAD_BUCKET, filename + "."
                                    + fileExtension);
                            
                            candidate.createNewFile();
                            if (!candidate.exists() || !candidate.isFile()) {
                                throw new RuntimeException("cannot create file: " + filename);
                            }
                            candidate.setReadable(true, false);
                            candidate.setWritable(true, false);
                            
                            FileOutputStream fos = null;
                            fos = new FileOutputStream(candidate);
                            if(null != fos){
                            	mBitmap.compress(convertExtensionToCompressFormat(fileExtension), quality, fos);
                                fos.flush();
                                fos.close();
                            }
                            quality = quality - 5;
                            Log.v(TAG, "file length: " + candidate.length() + " --quality : " + quality + " --: " + 
                                    convertExtensionToCompressFormat(fileExtension));
                            // Gionee <fangbin> <2013-06-27> modify for CR00830633 begin
                            if (quality <= 70) {
                                break;
                            }
                            if (null != candidate && candidate.length() <= 307200) {
                                break;
                            } else {
                                candidate.delete();
                                candidate = null;
                            }
                            // Gionee <fangbin> <2013-06-27> modify for CR00830633 end
                        }
                        if (null != candidate) {
                            Message msg = Message.obtain();
                            msg.obj= candidate.getAbsolutePath();
                            msg.what = CROP_SUCCESS;
                            mHandler.sendMessage(msg);
                        } else {
                            mHandler.sendEmptyMessage(CROP_ERROR);
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        if (null != candidate) {
                            candidate.delete();
                        }
                        mHandler.sendEmptyMessage(CROP_ERROR);
                    }
                    // Gionee fangbin 20121029 modified for CR00712006 end
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(CROP_ERROR);
        }
    }
    
    private CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png")
                ? CompressFormat.PNG
                : CompressFormat.JPEG;
    }
    
    private String getFileExtension() {
        String requestFormat = getIntent().getStringExtra(KEY_OUTPUT_FORMAT);
        // Gionee fangbin 20121029 modified for CR00712006 start
        String outputFormat = (requestFormat == null)
                ? "jpg"
                : requestFormat;
        // Gionee fangbin 20121029 modified for CR00712006 end

        outputFormat = outputFormat.toLowerCase();
        return (outputFormat.equals("png") || outputFormat.equals("gif"))
                ? "png" // We don't support gif compression.
                : "jpg";
    }
    
    private void initZoomData() {
        Uri uri = getIntent().getData();
        if (null == uri) {
            return;
        }
        // Gionee fangbin 20121030 removed for CR00721643 start
        /*
        String path = ZoomUtils.getImagePathByUri(this, uri);
        int rotation = ZoomUtils.getRotationByUri(this, uri);
        if (null == path) {
            return;
        }
        Bitmap bitmap = ZoomUtils.getThumbnailBitmap(path, WINDOW_WIDTH, WINDOW_HEIGHT);
        */
        // Gionee fangbin 20121030 removed for CR00721643 end
        // Gionee fangbin 20121030 added for CR00721643 start
        Bitmap bitmap = ZoomUtils.getBitmap(uri, WINDOW_WIDTH, WINDOW_HEIGHT, this);
        // Gionee fangbin 20121030 added for CR00721643 end
        Log.v(TAG, "initZoomData=====uri: " + uri + " --is null: " + (null == bitmap));
        
        // Gionee huangwenting 2012-06-05 add for CR00614312 begin
        if(bitmap == null){
        	ContentResolver resolver = getContentResolver();
            try{
            	bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);  
            }catch(Exception ex){
            	
            }
        }
        //Gionee huangwenting 2012-06-05 add for CR00614312 end

        if (null == bitmap) {
            // Gionee huangwenting 2012-07-12 add for CR00646741 begin
            Toast.makeText(this, getString(R.string.load_failed),
                Toast.LENGTH_LONG).show(); 
            finish();
            // Gionee huangwenting 2012-07-12 add for CR00646741 end
            return;
        }               
        
        mBitmap = bitmap;
        // Gionee fangbin 20121030 removed for CR00721643 start
//        rotate(rotation);
        // Gionee fangbin 20121030 removed for CR00721643 end
        // Gionee fangbin 20121030 added for CR00721643 start
        if (mBitmap != null) {
        	  resetZoomState();
            mZoomView.setImage(mBitmap);
        } else {
            closeDialog();
            Toast.makeText(this, R.string.outofmemory_error, Toast.LENGTH_LONG).show();
            finish();
        }
        // Gionee fangbin 20121030 added for CR00721643 end
    }
    
    private void rotate(int degree) {
        mBitmap = ZoomUtils.rotate(mBitmap, degree);
        // Gionee fangbin 20120912 modified for CR00687307 start
        if (mBitmap != null) {
            mZoomView.setImage(mBitmap);
        } else {
            closeDialog();
            Toast.makeText(this, R.string.outofmemory_error, Toast.LENGTH_LONG).show();
            finish();
        }
        // Gionee fangbin 20120912 modified for CR00687307 end
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case CROP_SUCCESS:
                String path = msg.obj.toString();
                Intent result = new Intent();
                result.putExtra(GN_KEY_DATA, path);
                setResult(RESULT_OK, result);
                closeDialog();
                finish();
                break;
            case CROP_ERROR:
                closeDialog();
                break;

            default:
                break;
            }
            
        }
    };
    
    private void showDialog() {
        if (null == mDialog) {
            mDialog = new AuroraProgressDialog(this);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage(getResources().getString(R.string.croping_dialog));
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }
    
    private void closeDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    
	  public static boolean isHonor6() {
	        String prop = SystemProperties.get("ro.gn.gnprojectid");
	        return prop.contains("H60L01");
	    }
}
