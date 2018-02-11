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
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.widget.AuroraActionBar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.R.anim;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.settings.SettingsPreferenceFragment;

import android.app.Fragment;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.content.ContentResolver;
import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.FileInputStream;

import android.database.Cursor;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.SystemProperties;
/**
 * 
 * Crop bootanimation interface
 * 
 * @author hanpingjiang 2014.04.15
 * 
 */

public class BootanimationCrop extends AuroraActivity implements OnClickListener{
	public static final String TAG = "BootanimationCrop";

	private View mView;
	private ImageView mMaskBg;
	private CropImageView mCropImage;
	private Button cropImageButton;
	private Bitmap myBitmap = null;
//	private Bitmap pictureBitmap = null;
//	private Bitmap backgroundBitmap = null;
	private String backgroundImagePath = "/sdcard/iuni";
	private AuroraActionBar mActionBar;
	private ProgressDialog pd;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraPicContentView(R.layout.bootanimation_crop);
		
		getAuroraActionBar().setBackgroundColor(Color.parseColor("#a0ffffff"));
		getAuroraActionBar().setTitle(R.string.set_bootanimation_settings);
		
		SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);
		String iuniAlbum = iuniSP.getString("iuniPhotoCrop", null);
		String iuniPhotograph = backgroundImagePath + "/temporary.png";
		android.util.Log.e("hanping", "iuniAlbum----->" + iuniAlbum);
		
		// Add begin by aurora.jiangmx
		boolean lIsAlbum = getIntent().getBooleanExtra("isAlbum", false);
		// Add end
		
		File photoFile = null;
		FileInputStream fis = null;
//		BitmapFactory.Options opt = new BitmapFactory.Options();
//		opt.inPreferredConfig = Bitmap.Config.RGB_565;
//		opt.inPurgeable = true;
//		opt.inInputShareable = true;
//		opt.inJustDecodeBounds = false;  
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
//		Bitmap bitmap = BitmapFactory.decodeFile(oldPath, options);
//		opt.inSampleSize = 10;
		try {
			photoFile = new File(iuniPhotograph);
			if(!photoFile.exists()) {
				BitmapFactory.decodeFile(iuniAlbum, options);
				int height1 = options.outHeight;
				int width1 = options.outWidth;
				int reqHeight = 0;
				int bitmapMaxWidth = 1080;
				int reqWidth = bitmapMaxWidth;
				reqHeight = (reqWidth * height1) / width1;
//				 在内存中创建bitmap对象，这个对象按照缩放大小创建的
				options.inSampleSize = calculateInSampleSize(options, bitmapMaxWidth,
						reqHeight);
				options.inJustDecodeBounds = false;
				
				photoFile = new File(iuniAlbum);
				fis = new FileInputStream(photoFile);
				myBitmap = BitmapFactory.decodeStream(fis, null, options);
//				pictureBitmap = BitmapFactory.decodeFile(iuniAlbum);
				
				// 获取图片的宽高
				int width = myBitmap.getWidth();
				int height = myBitmap.getHeight();
				Matrix matrix = new Matrix();
				int postRotate = getPostRotate(iuniAlbum);
				if(postRotate != 0) {
					matrix.postRotate(postRotate);
					myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, width, height, matrix, true);
				}
			} else {
			    
			    // Modify begin by aurora.jiangmx
				// BitmapFactory.decodeFile(iuniPhotograph, options);
			    // ------------div-------------
			    if( lIsAlbum && iuniAlbum != null ){
                    BitmapFactory.decodeFile(iuniAlbum, options);
                    photoFile = new File(iuniAlbum);
                }else{
                    BitmapFactory.decodeFile(iuniPhotograph, options);
                }
			    // Modify end
			    
				int height2 = options.outHeight;
				int width2 = options.outWidth;
				int reqHeight = 0;
				int bitmapMaxWidth = 1080;
				int reqWidth = bitmapMaxWidth;
				reqHeight = (reqWidth * height2) / width2;
//				 在内存中创建bitmap对象，这个对象按照缩放大小创建的
				options.inSampleSize = calculateInSampleSize(options, bitmapMaxWidth,
						reqHeight);
				options.inJustDecodeBounds = false;
				
				fis = new FileInputStream(photoFile);
				myBitmap = BitmapFactory.decodeStream(fis, null, options);
				// 获取图片的宽高
				int width = myBitmap.getWidth();
				int height = myBitmap.getHeight();
				Matrix matrix = new Matrix();
				int postRotate = getPostRotate(iuniPhotograph);
				if(postRotate != 0) {
					matrix.postRotate(postRotate);
					myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, width, height, matrix, true);
				}
			}
			if(null != fis) {
				fis.close();// 关闭
			}
		} catch(Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
		BitmapDrawable cropImage=new BitmapDrawable(drawProperSrcImg(myBitmap));
		mCropImage = (CropImageView) findViewById(R.id.cropImg);
		
		mCropImage.setX(0);
		mCropImage.setY(-getStatusBarHeight());
		LayoutParams params = mCropImage.getLayoutParams();
		int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();	
		params.height=screenHeight;
		mCropImage.setLayoutParams(params); 
		mCropImage.setDrawable(cropImage,360,640);
		mMaskBg = (ImageView) findViewById(R.id.mask_bg);
		BitmapDrawable maskBg= new BitmapDrawable(readBitmapFromResource(R.drawable.bootanimation_cut_bg));
		mMaskBg.setBackground(maskBg);
		cropImageButton = (Button) findViewById(R.id.crop_image);
		cropImageButton.setOnClickListener(this);
		if(null != cropImage) {
			cropImage.setCallback(null);
		}
		if(null != maskBg) {
			maskBg.setCallback(null);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}

//	/** 从给定的路径加载图片，并指定是否自动旋转方向, adjustOritation 调整方向 */
//	public static Bitmap loadBitmap(String imgpath, boolean adjustOritation) {
//		if (!adjustOritation) {
//			return loadBitmap(imgpath);
//		} else {
//			Bitmap bm = loadBitmap(imgpath);
//			int digree = 0;
//			ExifInterface exif = null;
//			try {
//				exif = new ExifInterface(imgpath);
//			} catch (IOException e) {
//				e.printStackTrace();
//				exif = null;
//			}
//			if (exif != null) {
//				// 读取图片中相机方向信息
//				int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//						ExifInterface.ORIENTATION_UNDEFINED);
//				// 计算旋转角度
//				switch (ori) {
//				case ExifInterface.ORIENTATION_ROTATE_90:
//					digree = 90;
//					break;
//				case ExifInterface.ORIENTATION_ROTATE_180:
//					digree = 180;
//					break;
//				case ExifInterface.ORIENTATION_ROTATE_270:
//					digree = 270;
//					break;
//				default:
//					digree = 0;
//					break;
//				}
//			}
//			if (digree != 0) {
//				// 旋转图片
//				Matrix m = new Matrix();
//				m.postRotate(digree);
//				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
//						bm.getHeight(), m, true);
//			}
//			return bm;
//		}
//	}
	
	//获取相片调整方向
	/** 从给定的路径加载图片，并指定是否自动旋转方向 */
	public static int getPostRotate(String imgpath) {
		int digree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(imgpath);
		} catch (IOException e) {
			e.printStackTrace();
			exif = null;
		}
		if (exif != null) {
			// 读取图片中相机方向信息
			int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_UNDEFINED);
			// 计算旋转角度
			switch (ori) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				digree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				digree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				digree = 270;
				break;
			default:
				digree = 0;
				break;
			}
		}
		return digree;
	}
	
	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}
	
	public Bitmap drawProperSrcImg(Bitmap bitmap) {
		// 想要设置的宽高
		int screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();				
		// 获取图片的宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// 计算缩放比例
		float scale = ((float) screenWidth) / width;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}
	
	//get statusBarHeight
	public int getStatusBarHeight(){ 
        Class<?> c = null; 
        Object obj = null; 
        Field field = null; 
        int x = 0, statusBarHeight = 0; 
        try { 
            c = Class.forName("com.android.internal.R$dimen"); 
            obj = c.newInstance(); 
            field = c.getField("status_bar_height"); 
            x = Integer.parseInt(field.get(obj).toString()); 
            statusBarHeight = getResources().getDimensionPixelSize(x);  
            Log.v(TAG, "the status bar height is : " + statusBarHeight);
        } catch (Exception e1) { 
        	Log.e(TAG, "Exception: " + Log.getStackTraceString(e1));
        }  
        if(statusBarHeight < 75) {
        	statusBarHeight = 75;
        }
        return statusBarHeight; 
    }
	
	//read img from resources
	public Bitmap readBitmapFromResource(int resId){ 
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = 1; 
        // get img from resources
        InputStream is = getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }
	
	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public int px2dip(float pxValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public void drawCropImage(Bitmap bitmap) {
		int screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
		Bitmap bgBitmap = null;
		Bitmap cvBitmap = null;
//		File photoFile = null;
//		FileInputStream ffs = null;
		try {
//			photoFile = new File(FileUtil.SDCARD_PAHT+"/iuni/crop.png");
//			ffs = new FileInputStream(photoFile);
			bgBitmap = readBitmapFromResource(R.drawable.bootanimation_bg);
			
			// 获取图片的宽高
			int width = bgBitmap.getWidth();
			int height = bgBitmap.getHeight();
			// 计算缩放比例
			float scale = ((float) screenWidth) / width;
			// 取得想要缩放的matrix参数
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			// 得到新的图片
			bgBitmap = Bitmap.createBitmap(bgBitmap, 0, 0, width, height, matrix, true);
			
			cvBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888 );
			Canvas cvMap = new Canvas(cvBitmap);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(44);
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
			
			//boolean hasNav = getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
			boolean hasNav = (Settings.System.getInt(getContentResolver(), "has_navigation_bar", 0) == 1);
			android.util.Log.e("hanping", "hasNav-0>" + hasNav);
			
			//40dp + 100dp - 75dp = 65dp
			int navHeightPx = getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
			android.util.Log.e("hanping", "navHeightPx-0>" + px2dip(navHeightPx));
			
			//IUNI
			cvMap.drawBitmap(bitmap, 0, 0, null);
			
			// Add begin by aurora.jiangmx
			String lDeviceName = SystemProperties.get("ro.gn.iuniznvernumber");
			// Add end
			
			//虚拟键高度+状态栏高度-75
			// Add begin by aurora.jiangmx
			if(lDeviceName.contains("MI2W")){
				cvMap.drawBitmap(bgBitmap, 0, 0, null);
			}else
			// Add end
			if(hasNav) {
				cvMap.drawBitmap(bgBitmap, 0, -65, null);
			} else {
				cvMap.drawBitmap(bgBitmap, 0, 0, null);
			}
			//IUNI
			cvMap.save( Canvas.ALL_SAVE_FLAG );
			cvMap.restore();

			File imageFile = new File(backgroundImagePath + "/iuni_bootanimation.png");
//			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imageFile));
//			cvBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			cvBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

			FileOutputStream os = new FileOutputStream(imageFile);
			os.write(stream.toByteArray());
			os.close();
			
			SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
			SharedPreferences.Editor edit = iuniSP.edit();  
			edit.putString("iuniPhoto", "success");  
			edit.commit();
			
//			if (photoFile.exists()) {
//		    	if (photoFile.isFile()) {
//		        	photoFile.delete();
//		        }
//			}
			if(cvBitmap != null && !cvBitmap.isRecycled()){  
				cvBitmap.recycle();  
	            cvBitmap = null;  
	        }
			if(bgBitmap != null && !bgBitmap.isRecycled()){  
				bgBitmap.recycle();  
				bgBitmap = null;  
	        }
			if(bitmap != null && !bitmap.isRecycled()){  
				bitmap.recycle();  
				bitmap = null;  
	        }
//			if(null != ffs) {
//				ffs.close();
//			}
		} catch(Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
	}

	@Override
    public void onResume() {
        super.onResume();
    }

	@Override
    public void onPause() {
        super.onPause();
    }
	
	@Override
    public void onStop() {
        super.onStop();
        BitmapDrawable backgroundImage = (BitmapDrawable)mMaskBg.getBackground();
		if(null != backgroundImage) {
			backgroundImage.setCallback(null);
//			backgroundImage.getBitmap().recycle();
		}
		finish();
    }

	@Override
    public void onDestroy() {
        super.onDestroy();
        if(null != pd) {
        	pd.dismiss();
        }
//        String iuniPhotograph = backgroundImagePath + "/temporary.png";
//		File photoFile = null;
//		try {
//			photoFile = new File(iuniPhotograph);
//			if(!photoFile.exists()) {
//				photoFile = new File(iuniPhotograph);
//			}
//			if(null != photoFile || photoFile.exists()) {
//				photoFile.delete();
//			}
//		} catch(Exception e) {
//			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
//		}
        
		if(myBitmap != null && !myBitmap.isRecycled()){ 
			myBitmap.recycle();  
            myBitmap = null;  
        }
		System.gc();
    }

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view == cropImageButton) {
			
			pd = ProgressDialog.show(BootanimationCrop.this, getResources().getString(R.string.font_bold_dlg_title), getResources().getString(R.string.font_bold_dlg_msg), true,false);
			
			new Thread(new Runnable(){

				@Override
				public void run() {

					// TODO Auto-generated method stub
					int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
					int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
					Bitmap tmpBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888);
					Bitmap mCroBitmap = mCropImage.getCropImage(screenWidth, screenHeight, tmpBitmap);
					mCroBitmap=Bitmap.createScaledBitmap(mCroBitmap, screenWidth, screenHeight, true);
					try {
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						mCroBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
								stream);
					} catch (Exception e) {
						// TODO: handle exception
					}

					drawCropImage(mCroBitmap);
					// imageBootanimation();
					if (null != mCroBitmap) {
						mCroBitmap.recycle();
						mCroBitmap = null;
					}
					if (null != tmpBitmap) {
						tmpBitmap.recycle();
						tmpBitmap = null;
					}
					finish();
				}

			}).start();

		}
	}
	
//	public Bitmap readBitmapFromPath(String path) {
//		BitmapFactory.Options opt = new BitmapFactory.Options();
//		opt.inPreferredConfig = Bitmap.Config.RGB_565;
//		opt.inPurgeable = true;
//		opt.inInputShareable = true;
//		opt.inJustDecodeBounds = false;  
//		File file = new File(path);
//		FileInputStream fStream = null;
//		try {
//			fStream = new FileInputStream(file);
//			return BitmapFactory.decodeStream(fStream, null, opt);
//		} catch (Exception e) {
//			// TODO: handle exception
//			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
//		}
////		Bitmap bmp = null;
////		if(fStream != null) {
////			try {
////				bmp = BitmapFactory.decodeFileDescriptor(fStream.getFD(), null, opt);
////			} catch (Exception e) {
////				// TODO: handle exception
////				Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
////			} finally {
////				if(fStream != null) {
////					try {
////						fStream.close();
////					} catch (Exception e2) {
////						// TODO: handle exception
////						Log.e(TAG, "Exception: " + Log.getStackTraceString(e2));
////					}
////				}
////			}
////		}
//		return BitmapFactory.decodeStream(fStream, null, opt);
//	}
	
//	/**
//	 * Set backgroundBg.
//	 * 
//	 */
//	public void imageBootanimation() {
//		int screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
//		int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
//		Bitmap handleBitmap = null;
//		
//		//set backgroundBg img.
//		BitmapFactory.Options opt = new BitmapFactory.Options();
//		opt.inPreferredConfig = Bitmap.Config.RGB_565;
//		opt.inPurgeable = true;
//		opt.inInputShareable = true;
//		opt.inJustDecodeBounds = false;  
//		File file = new File(backgroundImagePath + "/iuni_bootanimation.png");
//		FileInputStream fStream = null;
//		try {
//			fStream = new FileInputStream(file);
//			backgroundBitmap = BitmapFactory.decodeStream(fStream, null, opt);
////			backgroundBitmap = BitmapFactory.decodeFile(backgroundImagePath + "/iuni_bootanimation.png");
//		} catch (Exception e) {
//			// TODO: handle exception
//			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
//		}
//		handleBitmap = Bitmap.createBitmap(screenWidth, screenHeight-getStatusBarHeight(), Config.ARGB_8888 );
//		Canvas canvas = new Canvas(handleBitmap);
//		//#######需要对位置进行优化，增强适配性，这里应该时状态栏高度55dp * 3
//		canvas.drawBitmap(backgroundBitmap, 0, -getStatusBarHeight(), null);
//		android.util.Log.e("hanping", "getStatusBarHeight()-0>" + getStatusBarHeight());
//		//#######
//		canvas.save( Canvas.ALL_SAVE_FLAG );//保存  
//		canvas.restore();//存储
//		
//		try {
//			File showImage = new File(backgroundImagePath + "/show_bootanimation_bg.png");
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			android.util.Log.e("hanping", "compress-0>");
//			handleBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//			android.util.Log.e("hanping", "compress-1>");
//			FileOutputStream os = new FileOutputStream(showImage);
//			os.write(stream.toByteArray());
//			os.close();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
////		FileUtil.writeImage(handleBitmap, FileUtil.SDCARD_PAHT+"/iuni/show_bootanimation_bg.png", 100);
//		
//		if(null != backgroundBitmap && !backgroundBitmap.isRecycled()){  
//			backgroundBitmap.recycle();  
//            backgroundBitmap = null;  
//        }
//		if(null != handleBitmap && !handleBitmap.isRecycled()){  
//			handleBitmap.recycle();  
//            handleBitmap = null;  
//        }
//		android.util.Log.e("hanping", "getStatusBarHeight()-1>" + getStatusBarHeight());
//	}
}
