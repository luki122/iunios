/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.bootanimation;

import com.android.settings.R;

import aurora.app.AuroraActivity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.settings.SettingsPreferenceFragment;
import android.app.Fragment;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.content.SharedPreferences;
import android.content.Context;
import java.io.InputStream;
import android.os.Build;

/**
 * 
 * 开机画面拍照界面
 * 
 * @author hanpingjiang 2014.03.04
 * 
 */

public class BootanimationPreview extends Fragment implements Callback, OnClickListener, AutoFocusCallback {
	SurfaceView mySurfaceView;// surfaceView声明
	SurfaceHolder holder;// surfaceHolder声明
	Camera myCamera;// 相机声明
	private String backgroundImagePath = "/sdcard/iuni";// 照片保存路径
	private boolean isClicked = false;// 是否点击标识
	private View mView;
	private ImageView imageView;
	private Bitmap pictureBitmap = null;
	private Bitmap newPictureImage = null;
	private Bitmap bgBitmap = null;
	private Bitmap newBgImage = null;
	//画无签名图
	private Bitmap cvBitmap = null;
	//画无签名图，并调整图与签名的上下位置
	private Bitmap newBGBitmap = null;

	//String filePath = "/sdcard/iuni/iuni_data.jpeg";// 照片保存路径

	// 创建jpeg图片回调数据对象
	PictureCallback jpeg = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			try {
				pictureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				//test picture
				/*File filetest = new File(filePath);
				File file1 = new File(backgroundImagePath);
				if(!file1.exists()){  
					file1.mkdirs(); 
				}
				BufferedOutputStream bostest = new BufferedOutputStream(
						new FileOutputStream(filetest));
				pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bostest);// 将图片压缩到流中
				
				//Bitmap pictureBitmap = BitmapFactory.decodeFile("/data/iuni/iuni_data.jpeg");				
							
				//Bitmap pictureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				*/

				// 想要设置的宽高
				int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
				int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();				
				// 获取图片的宽高
				int width = pictureBitmap.getWidth();
				int height = pictureBitmap.getHeight();
				android.util.Log.e("hanping", "width->" + width + "---height->" + height);
				// 计算缩放比例
				float scaleWidth = ((float) screenWidth) / height;
				float scaleHeight = ((float) screenHeight) / width;
				float scale;
				if(scaleWidth > scaleHeight) {
					scale = scaleHeight/1.5f;			
				}else {
					scale = scaleWidth/1.5f;				
				}
				android.util.Log.e("hanping", "scaleWidth->" + scaleWidth + "---scaleHeight->" + scaleHeight);
				// 取得想要缩放的matrix参数
				Matrix matrix = new Matrix();
				matrix.setScale(-1, 1);
				matrix.postRotate(90);
				matrix.postScale(scale, scale);
				// 得到新的图片
				newPictureImage = Bitmap.createBitmap(pictureBitmap, 0, 0, width, height, matrix, true);

				// 获得资源
				bgBitmap = readBitMap(R.drawable.bootanimation_bg);
				int bgWidth = bgBitmap.getWidth();
				int bgHeight = bgBitmap.getHeight();
				// 计算缩放比例
				float bgScaleWidth = ((float) screenWidth) / bgWidth;
				float bgScaleHeight = ((float) screenHeight) / bgHeight;
				android.util.Log.e("hanping", "bgScaleWidth->" + bgScaleWidth + "---bgScaleHeight->" + bgScaleHeight);
				// 取得想要缩放的matrix参数
				Matrix bgMatrix = new Matrix();
				bgMatrix.postScale(bgScaleWidth, bgScaleHeight);
				// 得到新的图片
				newBgImage = Bitmap.createBitmap(bgBitmap, 0, 0, bgWidth, bgHeight, bgMatrix, true);

				//画无签名图
				cvBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888 );
				Canvas cvMap = new Canvas(cvBitmap);
				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
				paint.setTextSize(48);
				paint.setColor(Color.BLACK);
				paint.setTextAlign(Paint.Align.CENTER);
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
				//IUNI
				//cvMap.drawBitmap(newPictureImage, 177, 137, null);
				//cvMap.drawBitmap(newBgImage, 0, 0, null);
				//IUNI
				String buildModel = Build.MODEL;
				if (buildModel.contains("I9500")) {
				    cvMap.drawBitmap(newPictureImage, 177, 290, null);
					cvMap.drawBitmap(newBgImage, 0, 0, null);
				} else {
					cvMap.drawBitmap(newPictureImage, 177, 137, null);
					cvMap.drawBitmap(newBgImage, 0, 0, null);
				}
				cvMap.save( Canvas.ALL_SAVE_FLAG );//保存  
			   	cvMap.restore();//存储

				//画无签名图，并调整图与签名的上下位置
/*				newBGBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888 );
				Canvas newBGMap = new Canvas(newBGBitmap);
				Paint paintBG = new Paint(Paint.ANTI_ALIAS_FLAG);
				paintBG.setTextSize(48);
				paintBG.setColor(Color.BLACK);
				paintBG.setTextAlign(Paint.Align.CENTER);
				paintBG.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
				newBGMap.drawBitmap(cvBitmap, 0, -225, null);
				newBGMap.save( Canvas.ALL_SAVE_FLAG );//保存  
			   	newBGMap.restore();//存储
*/
				android.util.Log.e("hanping", "Bitmap->");
				File imageFile = new File(backgroundImagePath + "/iuni_bootanimation.jpeg");
				android.util.Log.e("hanping", "File->");
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imageFile));
				android.util.Log.e("hanping", "BufferedOutputStream->");
				cvBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);// 将图片压缩到流中
				android.util.Log.e("hanping", "compress->");
/*
				File imageBGFile = new File(backgroundImagePath + "/iuni_bg_bootanimation.jpeg");
				android.util.Log.e("hanping", "File->");
				BufferedOutputStream bosBG = new BufferedOutputStream(new FileOutputStream(imageBGFile));
				android.util.Log.e("hanping", "BufferedOutputStream->");
				newBGBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bosBG);// 将图片压缩到流中
				android.util.Log.e("hanping", "compress->");
*/
				//获取到sharepreference 对象， 参数一为xml文件名，参数为文件的可操作模式  
				SharedPreferences iuniSP = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);  
				//获取到编辑对象  
				SharedPreferences.Editor edit = iuniSP.edit();  
				//添加新的值，可见是键值对的形式添加  
				edit.putString("iuniPhoto", "success");  
				//提交.  
				edit.commit();

				getActivity().onBackPressed();
				android.util.Log.e("hanping", "here->");
				if(null != bos) {
					bos.flush();// 输出
					bos.close();// 关闭
				}
//				if(null != bosBG) {
//					bosBG.flush();// 输出
//					bosBG.close();// 关闭
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	//解决java.lang.OutOfMemoryError
	public Bitmap readBitMap(int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		InputStream is = getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.bootanimation_preview, container, false);
		//mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
		// 获得控件
		mySurfaceView = (SurfaceView) mView.findViewById(R.id.bootanimation_surfaceview);
		imageView = (ImageView) mView.findViewById(R.id.animation_setting);
		// 获得句柄
		holder = mySurfaceView.getHolder();
		// 添加回调
		holder.addCallback(this);
		// 设置类型
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 设置监听
		if (null != mySurfaceView) {
			mySurfaceView.setOnClickListener(this);
		}
		imageView.setOnClickListener(this);
		return mView;
	}

	@Override
    public void onPause() {
        super.onPause();
		if(pictureBitmap != null && !pictureBitmap.isRecycled()){  
			pictureBitmap.recycle();  
            pictureBitmap = null;  
        }
		if(newPictureImage != null && !newPictureImage.isRecycled()){  
			newPictureImage.recycle();  
            newPictureImage = null;  
        }
		if(bgBitmap != null && !bgBitmap.isRecycled()){  
			bgBitmap.recycle();  
            bgBitmap = null;  
        }
		if(newBgImage != null && !newBgImage.isRecycled()){  
			newBgImage.recycle();  
            newBgImage = null;  
        }
		if(cvBitmap != null && !cvBitmap.isRecycled()){  
			cvBitmap.recycle();  
            cvBitmap = null;  
        }
		if(newBGBitmap != null && !newBGBitmap.isRecycled()){  
			newBGBitmap.recycle();  
            newBGBitmap = null;  
        }
		System.gc();
    }

	@Override
    public void onDestroy() {
        super.onDestroy();
    }

	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		android.util.Log.e("hanping", "surfaceCreated->");
		int CameraIndex = FindFrontCamera();
		int CameraIndex1 = FindBackCamera();
		// 开启相机
		if (myCamera == null) {
			myCamera = Camera.open(CameraIndex);// 开启相机,不能放在构造函数中，不然不会显示画面.
		try {
			myCamera.setPreviewDisplay(holder);
			myCamera.setDisplayOrientation(getPreviewDegree(getActivity()));
			myCamera.startPreview();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		android.util.Log.e("hanping", "surfaceChanged->");
		// TODO Auto-generated method stub
		// 想要设置的宽高
		int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();				
		android.util.Log.e("hanping", "screenWidth->" + screenWidth + "---screenHeight->" + screenHeight);
		// 设置参数并开始预览
		Camera.Parameters params = myCamera.getParameters();
		params.setPictureFormat(PixelFormat.JPEG);
		List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, width, height);
		android.util.Log.e("hanping","----width->" + optimalSize.width + "----height->" + optimalSize.height);
		params.setPreviewSize(optimalSize.width, optimalSize.height);
		//^^^设置预览大小，根据相机大小参数设置
		params.setPictureSize(optimalSize.width, optimalSize.height);
		params.setJpegQuality(100);
		myCamera.setParameters(params);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// 关闭预览并释放资源
		android.util.Log.e("hanping", "surfaceDestroyed->");
		if(myCamera!=null) {
			//myCamera.stopPreview();
			myCamera.release();
			myCamera = null;
		}
	}
	
	//前置摄像头
	private int FindFrontCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return -1;
	}

	//后置摄像头
	private int FindBackCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				return camIdx;
			}
		}
		return -1;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

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
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	// 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	public static int getPreviewDegree(Activity activity) {
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if (view == imageView) {
			if(myCamera != null) {
				myCamera.takePicture(mShutterCallback, null, jpeg);
			}
		//myCamera.autoFocus(this);  //自动对焦
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub
		if (success && myCamera!=null) {
		/**
			// 设置参数,并拍照
			Camera.Parameters params = myCamera.getParameters();
			params.setPictureFormat(PixelFormat.JPEG);
			// params.setPreviewSize(480,640);
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			Camera.Size selected = sizes.get(12);
			params.setPreviewSize(selected.width, selected.height);
			myCamera.setParameters(params);
		*/
			myCamera.takePicture(mShutterCallback, null, jpeg);
		}

	}
	
	/**  
	 * 在相机快门关闭时候的回调接口，通过这个接口来通知用户快门关闭的事件，  
	 * 普通相机在快门关闭的时候都会发出响声，根据需要可以在该回调接口中定义各种动作， 例如：使设备震动  
	 */    
	 private final ShutterCallback mShutterCallback = new ShutterCallback() {    
		 public void onShutter() {    
		     Log.d("ShutterCallback", "...onShutter...");    
		 }    
	 }; 
}
