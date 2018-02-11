/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.android.internal.annotations.Immutable;
import com.android.settings.R;

import android.content.Context;
import android.content.Intent;
import android.security.Credentials;
import android.security.KeyStore;
import aurora.app.AuroraActivity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.CheckBox;
import aurora.widget.AuroraEditText;
import android.widget.TextView;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import android.R.anim;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import com.android.settings.SettingsPreferenceFragment;
import aurora.preference.AuroraPreferenceActivity;
import android.app.Fragment;
import android.widget.ImageButton;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.CustomAuroraActionBarItem;
import aurora.widget.AuroraEditText;
import android.provider.MediaStore;
import android.provider.Settings;
import android.net.Uri;
import android.nfc.Tag;
import android.content.SharedPreferences;
import android.view.View.OnFocusChangeListener;
import android.app.Dialog;
import android.content.DialogInterface;
import aurora.app.AuroraAlertDialog;
import android.database.Cursor;
import android.content.res.Resources;
import android.view.inputmethod.InputMethodManager;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 
 * Main bootanimation interface
 * 
 * @author hanpingjiang 2014.04.15
 * 
 */

public class BootanimationSettings extends AuroraActivity implements View.OnClickListener, TextWatcher, OnFocusChangeListener {
	public static final String TAG = "BootanimationSettings";
	
	private Dialog mServiceDialog;
	private ImageView saveEdit;
	private View mView;
	private Button changeButton;
//	private Button shareButton;
	private Button noChangeButton;
	private Button resetButton;
	private ImageButton mScanBtn;
    private AuroraActionBar mActionBar;
	private AuroraEditText iuniEdit;
	private CharSequence inputLimit;
	private String backgroundImagePath = "/sdcard/iuni";
	private String animationPart0Path = "/sdcard/iuni/bootanimation/part0";
	private String animationPart1Path = "/sdcard/iuni/bootanimation/part1";
	private String animationPath = "/sdcard/iuni/bootanimation";
	private String bootanimationZip = "/data/aurora/bootanimation.zip";
	private Bitmap handleBitmap = null;
	private boolean isCrop = false;
	Uri originalUri;
	FrameLayout layout = null;
	Paint textPaint = null;
	
	//whether hidden inputmethod
    InputMethodManager imm = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraPicContentView(R.layout.bootanimation_settings);

		// preview button
		getAuroraActionBar().addItem(R.layout.aurora_actionbar_scan_image, 0);
		CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) getAuroraActionBar()
				.getItem(0);
		View view = item.getItemView();
		mScanBtn = (ImageButton) view.findViewById(R.id.btn_scan);
		mScanBtn.setOnClickListener(this);
		getAuroraActionBar().setBackgroundColor(Color.parseColor("#a0ffffff"));
		getAuroraActionBar().setTitle(R.string.bootanimation_settings);

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		textPaint = new Paint();
		
		addMenu(0, R.string.scan_bootanimation, scanBootanimation);
		addMenu(1, R.string.share_bootanimation, shareBootanimation);

		File bootanimationZipPath = new File(bootanimationZip);
		// if(!bootanimationZipPath.exists()){
		// bootanimationZipPath.mkdirs();
		// }
		File auroraFile = new File("/data/aurora/");
		if (!auroraFile.exists()) {
			auroraFile.mkdirs();
		}
		File file = new File(backgroundImagePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
	
public static void chmod(String permission, String path) {
                try {
                        String command = "chmod " + permission + " " + path;
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exec(command);
                } catch (IOException e) {
                        e.printStackTrace();
                }
}

	OnMenuItemClickLisener scanBootanimation = new OnMenuItemClickLisener() {

		@Override
		public void onItemClick(View arg0) {
			// TODO Auto-generated method stub
			if (isExistBootanimationImage()) {
				scanBootanimationImage();
			} else {
				Toast.makeText(BootanimationSettings.this, R.string.set_bootanimation_first,
						Toast.LENGTH_LONG).show();
			}
		}
	};

	OnMenuItemClickLisener shareBootanimation = new OnMenuItemClickLisener() {
		
		@Override
		public void onItemClick(View arg0) {
			// TODO Auto-generated method stub
		    if (isExistBootanimationImage()) {
		    	Intent intent=new Intent(Intent.ACTION_SEND);   
			    intent.setType("image/*"); 
				File imageFile = new File(backgroundImagePath + "/iuni_bootanimation_end.png");
	    		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));  
			    startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_bootanimation)));
			} else {
				Toast.makeText(BootanimationSettings.this, R.string.set_bootanimation_first,
						Toast.LENGTH_LONG).show();
			}
		}
	};

	/**
	 * 
	 * Monitor EditText input
	 * 
	 */
	public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		inputLimit = s;
    }
	
    public void afterTextChanged(Editable editable) {
		//Limit that sign isn't more than 32 char.
		if(textPaint.measureText(iuniEdit.getText().toString()) > 127.0) {
//			Toast.makeText(this,R.string.input_exceed_limit,Toast.LENGTH_SHORT).show();
			editable.delete(inputLimit.length()-1, inputLimit.length());
		}
		
//		//save input from user.
//		SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
//		SharedPreferences.Editor edit = iuniSP.edit();  
//		edit.putString("iuniText", iuniEdit.getText().toString());  
//		edit.commit();
    }
	
	/**
	 * 
	 * Monitor EditText focus
	 * 
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		AuroraEditText hintText=(AuroraEditText)v;
		if (hasFocus) {
			String hint=hintText.getHint().toString();
            hintText.setTag(hint);
            hintText.setHint("");
			showSaveImage();
			changeButton.setSelected(false);
			changeButton.setEnabled(false);
//			shareButton.setSelected(false);
//			shareButton.setEnabled(false);
			resetButton.setSelected(false);
			resetButton.setEnabled(false);
		} else {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			hintText.setHint(hintText.getTag().toString());
			noShowSaveImage();
			changeButton.setSelected(true);
			changeButton.setEnabled(true);
//			shareButton.setSelected(true);
//			shareButton.setEnabled(true);
			resetButton.setSelected(true);
			resetButton.setEnabled(true);
		}
	}

	/**
	 * 
	 * display save img and edit img or not.
	 * 
	 */
	public void noShowSaveImage() {
		findViewById(R.id.save_edit).setVisibility(View.GONE);
		findViewById(R.id.iuni_bg_edit).setVisibility(View.VISIBLE);
	}

	public void showSaveImage() {
		findViewById(R.id.save_edit).setVisibility(View.VISIBLE);
		saveEdit = (ImageView) findViewById(R.id.save_edit);
		findViewById(R.id.iuni_bg_edit).setVisibility(View.INVISIBLE);
		if(saveEdit != null) {
			saveEdit.setOnClickListener(this);
		}
	}
	
	/**
	 * sp changes into px.
	 * 
	 * @param spValue
	 * @param fontScale
	 * @return
	 */
	public int sp2px(float spValue) {
		float fontScale = getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public int dip2px(float dpValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 
	 * Save the last bootanimation img.
	 * 
	 */
	public void saveBootanimationImage() {
		Bitmap saveEndBitmap = null;
		Bitmap dirBitmap = null;
		try{
//			dirBitmap = readBitmapFromPath(backgroundImagePath + "/iuni_bootanimation.jpeg");
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			opt.inJustDecodeBounds = false;  
			File file = new File(backgroundImagePath + "/iuni_bootanimation.png");
			FileInputStream fStream = null;
			try {
				fStream = new FileInputStream(file);
//				dirBitmap = BitmapFactory.decodeFile(backgroundImagePath + "/iuni_bootanimation.png");
				dirBitmap = BitmapFactory.decodeStream(fStream, null, opt);
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
			}
			//Set screen width and height.
			int screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
			int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
//			int textLength = iuniEdit.getText().toString().getBytes().length;
			
			//Create canvas and draw endBackgroundImage.
			saveEndBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888);
			Canvas canvas = new Canvas(saveEndBitmap);
			
			//Paint
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(sp2px(13.0f));
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
			android.util.Log.e("hanping", "iuniEdit.textWidth()-1111>" + textPaint.measureText(iuniEdit.getText().toString()));
			
			canvas.drawBitmap(dirBitmap, 0, 0, null);
			//####### Optimization
			if(1080 == screenWidth) {
				canvas.drawText(iuniEdit.getText().toString() , screenWidth/2, 900, paint);
			} else {
				canvas.drawText(iuniEdit.getText().toString() , screenWidth/2, screenHeight/2/*1128*/, paint);
			}
			//#######
			canvas.save( Canvas.ALL_SAVE_FLAG ); 
			canvas.restore();
			
			File endImageFile = new File(backgroundImagePath + "/iuni_bootanimation_end.png");
//			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(endImageFile));
//			saveEndBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			saveEndBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

			FileOutputStream os = new FileOutputStream(endImageFile);
			os.write(stream.toByteArray());
			os.close();
			
			getandSaveCurrentImage(endImageFile);
			
//			if(null != bos) {
//				bos.flush();
//				bos.close();
//			}
			if(null != dirBitmap && !dirBitmap.isRecycled()){  
				dirBitmap.recycle();  
            	dirBitmap = null;  
        	}
			if(null != saveEndBitmap && !saveEndBitmap.isRecycled()){  
				saveEndBitmap.recycle();  
            	saveEndBitmap = null;  
        	}
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
	}
	
	/**
     * 使用文件内存映射实现
     * @throws IOException
     */
    private void coypByMbb(File frmFile, File toFile) throws IOException {
        long startTime = System.currentTimeMillis();
        FileInputStream fin = new FileInputStream(frmFile);
        RandomAccessFile fout = new RandomAccessFile(toFile, "rw");
        FileChannel fcin = fin.getChannel();
        FileChannel fcout = fout.getChannel();
        MappedByteBuffer mbbi = fcin.map(FileChannel.MapMode.READ_ONLY, 0,
                fcin.size());
        MappedByteBuffer mbbo = fcout.map(FileChannel.MapMode.READ_WRITE, 0,
                fcin.size());
        mbbo.put(mbbi);
        mbbi.clear();
        mbbo.clear();
        long endTime = System.currentTimeMillis();
    }

	/**
	 * 
	 * Save bootanimaton img, zip file and write configuration file.
	 * 
	 */
	private void getandSaveCurrentImage(File srcFile) {
		FileOutputStream out = null;
		FileOutputStream out1 = null;	
		try {
			// img path
			File animationPart0 = new File(animationPart0Path);
		    File animationPart1 = new File(animationPart1Path);
		    File file1 = new File(animationPart1Path + "/iuni.png");
			File outFile = new File(animationPath + "/desc.txt");
		    if(!animationPart0.exists()){  
		    	animationPart0.mkdirs(); 
		    }  
		    if(!animationPart1.exists()){  
			    animationPart1.mkdirs();
			} 
		    if (!file1.exists()) {  
			    file1.createNewFile();
			}
			if (!outFile.exists()) {  
			    outFile.createNewFile();
			}
			out1 = new FileOutputStream(file1);
			out = new FileOutputStream(outFile);
			
			//write configuration file
			//####### Optimization
			int screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
			int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
			//boolean hasNav = this.getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
			boolean hasNav = (Settings.System.getInt(getContentResolver(), "has_navigation_bar", 0) == 1);
			int navHeightPx = this.getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
			if(hasNav) {
				screenHeight = screenHeight + navHeightPx;
				out.write((screenWidth + " " + screenHeight + " 15\np 1 0 part0\np 0 0 part1\n").getBytes());
				android.util.Log.e("hanping", "screenWidth-00000>" + screenWidth + "--screenHeight-00000>" + screenHeight);
			} else {
				out.write((screenWidth + " " + screenHeight + " 15\np 1 0 part0\np 0 0 part1\n").getBytes());
				android.util.Log.e("hanping", "screenWidth-11111>" + screenWidth + "--screenHeight-11111>" + screenHeight);
			}
			//#######
//		    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out1);
//		    
			coypByMbb(srcFile,file1);
		    
		    if (null != out1) {  
		    	out1.flush();  
		    	out1.close();
		    }
			if (null != out) {  
				out.flush();  
				out.close();
		    }
//			if(null != bitmap && !bitmap.isRecycled()){  
//				bitmap.recycle();  
//				bitmap = null;  
//        	}
			
			String iuniBootEnd = bootanimationZip;
			File iuniBootEndFile = null;
			try {
				iuniBootEndFile = new File(iuniBootEnd);
				if (!iuniBootEndFile.exists()) {
					iuniBootEndFile = new File(iuniBootEnd);
				}
				if (null != iuniBootEndFile || iuniBootEndFile.exists()) {
					iuniBootEndFile.delete();
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
			}
			
			zipBootanimation();
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
	}

	// Zip bootanimation.zip
	private void zipBootanimation() {
		Project pj = new Project();
		Zip zip = new Zip();
		zip.setProject(pj);
		zip.setCompress(false);
		zip.setDestFile(new File(bootanimationZip));
		FileSet fileSet = new FileSet();
		fileSet.setProject(pj);
		fileSet.setDir(new File(animationPath));
		zip.addFileset(fileSet);
		zip.execute();
		chmod("666",bootanimationZip); //wolfu change /data/aurora/bootanimation.zip permission to 666
//		Toast.makeText(this, R.string.set_bootanimation_success, Toast.LENGTH_LONG).show();
	}

	/**
	 *
	 * Judging whether exsiting unsign bootanimation img.
	 * 
	 */
	public boolean isBootanimationImageSave() {
		File file=new File(backgroundImagePath + "/iuni_bootanimation.png"); 
		return (!file.exists()) ? false : true;
	}	

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
//		case R.id.share:
//			Intent intent=new Intent(Intent.ACTION_SEND);   
//		    intent.setType("image/*");   
//			File imageFile = new File(backgroundImagePath + "/iuni_bootanimation_end.png");
//    		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));  
//		    startActivity(Intent.createChooser(intent, this.getTitle()));
//			break;
		case R.id.change:
			showChoiceDialog();
			break;
		case R.id.reset:
//			resetHandler.post(resetTask);
			resetDefaultBG();
			break;
		case R.id.none_change:
			showChoiceDialog();
			break;
		case R.id.btn_scan:
//			if(isExistBootanimationImage()) {
//				scanBootanimationImage();			
//			}else {
//				Toast.makeText(this, R.string.set_bootanimation_first,Toast.LENGTH_LONG).show();			
//			}
			showCustomMenu();
			break;
		case R.id.save_edit:
//			saveBootanimationImage();
			new Thread(new Runnable(){

				@Override
				public void run() {

					// TODO Auto-generated method stub
					saveBootanimationImage();
				}

			}).start();
			//save input from user.
			SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
			SharedPreferences.Editor edit = iuniSP.edit();  
			edit.putString("iuniText", iuniEdit.getText().toString());  
			edit.commit();
			findViewById(R.id.save_edit).setVisibility(View.GONE);
			layout.requestFocus();
			Toast.makeText(this, R.string.set_bootanimation_success, Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}
	
	private final Handler resetHandler = new Handler(); 
	private final Runnable resetTask = new Runnable() {
        @Override  
        public void run() {  
            // TODO Auto-generated method stub  
        	resetDefaultBG();
        	resetButton.setSelected(true);
    		resetButton.setEnabled(true);
        }  
    }; 
	
	public void resetDefaultBG() {
		resetButton.setSelected(false);
		resetButton.setEnabled(false);
		resetButton.setBackgroundResource(R.drawable.bootanimation_button_unpressed);
		resetButton.setTextColor(Color.parseColor("#7f7f7f"));
		String iuniBg = backgroundImagePath + "/iuni_bootanimation.png";
		String iuniBgEnd = backgroundImagePath + "/iuni_bootanimation_end.png";
		File iuniBgFile = null;
		File iuniBgEndFile = null;
		try {
			iuniBgFile = new File(iuniBg);
			iuniBgEndFile = new File(iuniBgEnd);
			if (!iuniBgFile.exists()) {
				iuniBgFile = new File(iuniBg);
			}
			if (null != iuniBgFile || iuniBgFile.exists()) {
				iuniBgFile.delete();
			}
			if (!iuniBgEndFile.exists()) {
				iuniBgEndFile = new File(iuniBgEnd);
			}
			if (null != iuniBgEndFile || iuniBgEndFile.exists()) {
				iuniBgEndFile.delete();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
		
		SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
		SharedPreferences.Editor edit = iuniSP.edit();  
		edit.putString("iuniText", "I AM UNIQUE");  
		edit.commit();
		iuniEdit.setText("I AM UNIQUE");
		
		int screenWidth = this.getWindowManager().getDefaultDisplay()
				.getWidth();
		int screenHeight = this.getWindowManager().getDefaultDisplay()
				.getHeight();
		Bitmap bgBitmap = readBitmapFromResource(R.drawable.bootanimation_bg);
		Bitmap cvBitmap = null;

		// 获取图片的宽高
		int width = bgBitmap.getWidth();
		int height = bgBitmap.getHeight();
		// 计算缩放比例
		float scale = ((float) screenWidth) / width;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片
		bgBitmap = Bitmap.createBitmap(bgBitmap, 0, 0, width, height, matrix,
				true);

		cvBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Config.ARGB_8888);
		Canvas cvMap = new Canvas(cvBitmap);
//		boolean hasNav = getResources().getBoolean(
//				com.android.internal.R.bool.config_showNavigationBar);
		boolean hasNav = (Settings.System.getInt(getContentResolver(), "has_navigation_bar", 0) == 1);

		// 40dp + 100dp - 75dp = 65dp
		int navHeightPx = getResources().getDimensionPixelSize(
				com.android.internal.R.dimen.navigation_bar_height);

		// IUNI
		// 虚拟键高度+状态栏高度-75
		if (hasNav) {
			cvMap.drawBitmap(bgBitmap, 0, -65, null);
		} else {
			cvMap.drawBitmap(bgBitmap, 0, 0, null);
		}
		// IUNI
		cvMap.save(Canvas.ALL_SAVE_FLAG);
		cvMap.restore();

		File imageFile = new File(backgroundImagePath
				+ "/iuni_bootanimation.png");

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			cvBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

			FileOutputStream os = new FileOutputStream(imageFile);
			os.write(stream.toByteArray());
			os.close();

			if(cvBitmap != null && !cvBitmap.isRecycled()){  
				cvBitmap.recycle();  
	            cvBitmap = null;  
	        }
			if(bgBitmap != null && !bgBitmap.isRecycled()){  
				bgBitmap.recycle();  
				bgBitmap = null;  
	        }
		} catch (Exception e) {
			// TODO: handle exception
		}

		BitmapDrawable defaultBg = new BitmapDrawable(
				readBitmapFromResource(R.drawable.bootanimation_cut_bg));
		layout.setBackground(defaultBg);
		if (null != defaultBg) {
			defaultBg.setCallback(null);
		}
		
		//BUG #5690  begin
		String iuniBootanimationZip = bootanimationZip;
		File zipFile = null;
		try {
			zipFile = new File(iuniBootanimationZip);
			if(null != zipFile || zipFile.exists()) {
				zipFile.delete();
			}
		} catch(Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
		//BUG #5690 end
		
		if(!isExistBootanimationImage()) {
			mScanBtn.setOnClickListener(null);
			mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button_unpressed);
		} else {
			mScanBtn.setOnClickListener(this);
			mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button);
		}
		
		Toast.makeText(BootanimationSettings.this, R.string.bootanimation_reset,
				Toast.LENGTH_LONG).show();
	}

	/**
	 *
	 * Show choice Dialog
	 * 
	 */
	public void showChoiceDialog() {
		final CharSequence[] choice = {getString(R.string.photograph_bootanimation), getString(R.string.album_bootanimation)};
		mServiceDialog = new AuroraAlertDialog.Builder(this)
                    .setTitle(R.string.set_bootanimation_settings)
					.setItems(choice, new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int item) {
							if(0 == item) {
								String fileName = backgroundImagePath + "/temporary.png";
								Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName)));
								startActivityForResult(intent, 0);
							} else if(1 == item) {
//								isCrop = true;
								
								Intent intent = new Intent();
								intent.setAction("com.aurora.filemanager.SINGLE_GET_CONTENT");
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.setType("image/*");			
								startActivityForResult(intent, 1);
							}       				
						}})
                    .show();
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == -1) {
            Intent intent = new Intent();
			intent.setClass(BootanimationSettings.this, BootanimationCrop.class);
			startActivity(intent);
        } else if (requestCode == 1 && resultCode == -1) {
            try { 
                originalUri = data.getData(); 
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor actualImageCursor = this.managedQuery(originalUri,proj,null,null,null);
				int actualImageColumnIndex = actualImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				actualImageCursor.moveToFirst();
				String imgePath = actualImageCursor.getString(actualImageColumnIndex);
				
				SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
				SharedPreferences.Editor edit = iuniSP.edit();  
				edit.putString("iuniPhotoCrop", imgePath);  
				edit.commit();
				
				Intent intent = new Intent();
				// Add begin by aurora.jiangmx 
				intent.putExtra("isAlbum", true);
				// Add end
				intent.setClass(BootanimationSettings.this, BootanimationCrop.class);
				startActivity(intent);
            } catch (Exception e) { 
            	Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
            } 
        }
        super.onActivityResult(requestCode, resultCode, data);
}

	@Override
    public void onResume() {
        super.onResume();
		//判断isCrop，以防backgroundBitmap被回收后，加载报Bitmap recycle()异常Crash
//		if(!isCrop) {
			SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);
			String iuniText = iuniSP.getString("iuniText", getResources().getString(R.string.sign_bootanimation));
			String iuniPhoto = iuniSP.getString("iuniPhoto", "failed");
			if(isBootanimationImageSave()) {
				findViewById(R.id.settings).setVisibility(View.VISIBLE);
				layout = (FrameLayout) findViewById(R.id.settings);
				changeButton = (Button) findViewById(R.id.change);
				changeButton.setOnClickListener(this);
//				shareButton = (Button) findViewById(R.id.share);
//				shareButton.setOnClickListener(this);
				resetButton = (Button) findViewById(R.id.reset);
				resetButton.setOnClickListener(this);
				iuniEdit = (AuroraEditText) findViewById(R.id.iuni_edit);
				iuniEdit.addTextChangedListener(this);
				iuniEdit.setOnFocusChangeListener(this);
				iuniEdit.setText(iuniText);
				setBootanimationBg();
				if(iuniPhoto.equals("success")) {
					saveBootanimationImage();	
				}
				isCrop = false;
				if(null != noChangeButton) {
					findViewById(R.id.none_settings).setVisibility(View.INVISIBLE);
				}
				if(!isExistBootanimationImage()) {
					mScanBtn.setOnClickListener(null);
					mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button_unpressed);
				} else {
					mScanBtn.setOnClickListener(this);
					mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button);
				}
			}else {
				findViewById(R.id.none_settings).setVisibility(View.VISIBLE);
				layout = (FrameLayout) findViewById(R.id.none_settings);	
				noChangeButton = (Button) findViewById(R.id.none_change);
				noChangeButton.setOnClickListener(this);
				mScanBtn.setOnClickListener(null);
				mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button_unpressed);
				BitmapDrawable defaultBg= new BitmapDrawable(readBitmapFromResource(R.drawable.bootanimation_default_bg));
				layout.setBackground(defaultBg);
				isCrop = true;
				if(null != defaultBg) {
					defaultBg.setCallback(null);
	    		}
			}
//		}
			if(null != resetButton) {
				if(isExistBootanimationImage()) {
		        	resetButton.setSelected(true);
		    		resetButton.setEnabled(true);
		    		resetButton.setBackgroundResource(R.drawable.bootanimation_button);
		    		resetButton.setTextColor(Color.parseColor("#000000"));
		        } else {
		        	resetButton.setSelected(false);
		    		resetButton.setEnabled(false);
		    		resetButton.setBackgroundResource(R.drawable.bootanimation_button_unpressed);
		    		resetButton.setTextColor(Color.parseColor("#7f7f7f"));
		        }
			}
    }
	
	//read img from resources
	public Bitmap readBitmapFromResource(int resId){ 
        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inJustDecodeBounds = false;
//        opt.inSampleSize = 1; 
        // get img from resources
        InputStream is = getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }
	
	//read img from bitmap
	public Bitmap readBitmapFromBitmap(Bitmap bitmap) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = 1; 
        // get img from bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);  
        InputStream is = new ByteArrayInputStream(baos.toByteArray());  
		return BitmapFactory.decodeStream(is);
	}
	
	//read img from path
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
//	}
	
    @Override
    public void onPause() {
        super.onPause();
		SharedPreferences iuniSP = this.getSharedPreferences("iuni", Context.MODE_PRIVATE);  
		SharedPreferences.Editor edit = iuniSP.edit();  
		edit.putString("iuniPhoto", "failed");
		edit.commit();
    }

	@Override
    public void onStop() {
        super.onStop();
        if(!isExistBootanimationImage()) {
			mScanBtn.setOnClickListener(null);
			mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button_unpressed);
		} else {
			mScanBtn.setOnClickListener(this);
			mScanBtn.setBackgroundResource(R.drawable.bootanimation_scan_button);
		}
//		if(backgroundBitmap != null && !backgroundBitmap.isRecycled()){  
//			backgroundBitmap.recycle();  
//	        backgroundBitmap = null;  
//    	}
        
        if(null != resetButton) {
			if(isExistBootanimationImage()) {
	        	resetButton.setSelected(true);
	    		resetButton.setEnabled(true);
	    		resetButton.setBackgroundResource(R.drawable.bootanimation_button);
	    		resetButton.setTextColor(Color.parseColor("#000000"));
	        } else {
	        	resetButton.setSelected(false);
	    		resetButton.setEnabled(false);
	    		resetButton.setBackgroundResource(R.drawable.bootanimation_button_unpressed);
	    		resetButton.setTextColor(Color.parseColor("#7f7f7f"));
	        }
		}
        // Delete begin by aurora.jiangmx for: exception of trying to use a recycled bitmap 
        /*if ( handleBitmap != null && !handleBitmap.isRecycled()) {
			handleBitmap.recycle();
			handleBitmap = null;
		}
        
        if(!isCrop) {
        	BitmapDrawable backgroundImage = (BitmapDrawable)layout.getBackground();
    		if(null != backgroundImage) {
    			backgroundImage.setCallback(null);
    			backgroundImage.getBitmap().recycle();
    		}
        }*/
       // Delete end
    }

	@Override
    public void onDestroy() {
        super.onDestroy();
        
        // Add begin by aurora.jiangmx for: exception of trying to use a recycled bitmap 
        if ( handleBitmap != null && !handleBitmap.isRecycled()) {
		   handleBitmap.recycle();
		   handleBitmap = null;
	    }
    
        if(!isCrop) {
    	   BitmapDrawable backgroundImage = (BitmapDrawable)layout.getBackground();
		   if(null != backgroundImage) {
			backgroundImage.setCallback(null);
			backgroundImage.getBitmap().recycle();
		   }
        }
        // Add end
    }
	
	/**
	 *
	 * Judging whether exsiting bootanimation img.
	 * 
	 */
	public boolean isExistBootanimationImage() {
		try {
			File backgroundPath = new File(backgroundImagePath); 
            File backgroundScanImagePath = new File(backgroundImagePath + "/iuni_bootanimation_end.png");
			if(!backgroundPath.exists() || !backgroundScanImagePath.exists()){  
		    	return false;
		    } else {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		} 
		return false;
	}

	/**
	 *
	 *Preview bootanimation img.
     *
	 */
	public void scanBootanimationImage() {
		try {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			File backgroundScanImagePath = new File(backgroundImagePath
					+ "/iuni_bootanimation_end.png");
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(backgroundScanImagePath));
			intent.setClass(this, ScanBootanimation.class);
			startActivity(intent);
			overridePendingTransition(R.anim.aurora_zoom_in, 0);
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
	}
	
	/**
	 * keyback event
	 * 
	 */
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(null != iuniEdit && iuniEdit.hasFocus()) {
				layout.requestFocus();
			} else{
				this.onBackPressed();
			}
            return true;
          }
		return false;
    }
	
	/**
	 * Set backgroundBg.
	 * 
	 */
	public Bitmap imageBootanimation() {
		Bitmap backgroundBitmap = null;
		int screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
		
		//set backgroundBg img.
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inJustDecodeBounds = false;  
		File file = new File(backgroundImagePath + "/iuni_bootanimation.png");
		FileInputStream fStream = null;
		try {
			fStream = new FileInputStream(file);
			backgroundBitmap = BitmapFactory.decodeStream(fStream, null, opt);
//			backgroundBitmap = BitmapFactory.decodeFile(backgroundImagePath + "/iuni_bootanimation.png");
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
		}
		handleBitmap = Bitmap.createBitmap(screenWidth, screenHeight-getStatusBarHeight(), Config.ARGB_8888 );
		Canvas canvas = new Canvas(handleBitmap);
		//#######需要对位置进行优化，增强适配性，这里应该时状态栏高度25dp * 3(4)
		canvas.drawBitmap(backgroundBitmap, 0, -getStatusBarHeight(), null);
		android.util.Log.e("hanping", "getStatusBarHeight()-0>" + getStatusBarHeight());
		//#######
		canvas.save( Canvas.ALL_SAVE_FLAG );//保存  
		canvas.restore();//存储
		
		if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
			backgroundBitmap.recycle();
			backgroundBitmap = null;
		}
		
		return handleBitmap;
	}
	
	// get statusBarHeight
	public int getStatusBarHeight() {
		boolean hasNav = (Settings.System.getInt(getContentResolver(), "has_navigation_bar", 0) == 1);
		if(!hasNav){
			return 0;
		}
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
		if (statusBarHeight < 75) {
			statusBarHeight = 75;
		}
		return statusBarHeight;
	}

	/**
	 * Set bootanimation background img.
	 * 
	 */
	public void setBootanimationBg() {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inJustDecodeBounds = false;
		BitmapDrawable backgroundImage = null;
		backgroundImage = new BitmapDrawable(imageBootanimation());
		layout.setBackground(backgroundImage);
		if(null != backgroundImage) {
			backgroundImage.setCallback(null);
		}
	}

}

