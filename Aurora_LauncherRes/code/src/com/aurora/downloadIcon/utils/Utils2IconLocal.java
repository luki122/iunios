package com.aurora.downloadIcon.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Utils2IconLocal {
	private static String icon_dir_prefix = "/data/aurora/icons";
	private static String icon_sddir_prefix = "/sdcard/icons";
	private static String inter_icon_dir_suffix = "/inter_icons/";
	private static String outer_icon_dir_suffix = "/outer_icons/";
	private static String iconServerDataFileName = "icon_in_server_data.json";
	private static String icon_suffix = ".png";

	public static void dealAndSavedIcon(String iconName,
			BitmapDrawable bitmapDrawable, Context context) throws IOException {
		Utils2Icon utils2Icon = Utils2Icon.getInstance(context);
		BitmapDrawable bd = bitmapDrawable;
		Drawable innerShawdowDrawble = utils2Icon.getSystemIconDrawable(
				bitmapDrawable, Utils2Icon.INTER_SHADOW);
		Drawable outerShawdowDrawble = utils2Icon.getSystemIconDrawable(bd,
				Utils2Icon.OUTER_SHADOW);
		Log.i("test", "  inner_hawdowDrawble = "
				+ (innerShawdowDrawble instanceof BitmapDrawable) + "   "
				+ ((BitmapDrawable) innerShawdowDrawble).getBitmap()
				+ "innerShawdowDrawble = " + innerShawdowDrawble
				+ " outerShawdowDrawble = " + outerShawdowDrawble);
		if (innerShawdowDrawble instanceof BitmapDrawable
				&& ((BitmapDrawable) innerShawdowDrawble).getBitmap() != null) {
			saveMyBitmap(icon_sddir_prefix + inter_icon_dir_suffix, iconName+icon_suffix,
					((BitmapDrawable) innerShawdowDrawble).getBitmap());
		}

		if (outerShawdowDrawble instanceof BitmapDrawable
				&& ((BitmapDrawable) outerShawdowDrawble).getBitmap() != null) {
			saveMyBitmap(icon_sddir_prefix + outer_icon_dir_suffix, iconName+icon_suffix,
					((BitmapDrawable) outerShawdowDrawble).getBitmap());
		}

	}

	private static synchronized void saveMyBitmap(String iconDir, String bitName,
			Bitmap bitmap) throws IOException {
		File dir = new File(iconDir);
		if (!dir.exists() && !dir.isDirectory()) {
			Log.i("test", "1");
			dir.mkdirs();
		}
		Log.i("test", "2  iconDir = "+iconDir);
		File f = new File(iconDir + bitName);
		f.createNewFile();
		FileOutputStream fOut = null;
		Log.i("test", "3");
		try {
			fOut = new FileOutputStream(f);
			Log.i("test", "4");
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		} catch (FileNotFoundException e) {
			Log.i("test", "FileNotFoundException", e);
			e.printStackTrace();
		}finally{
			try {
				Log.i("test", "5");
				fOut.flush();
				Log.i("test", "6");
				fOut.close();
			} catch (IOException e) {
				Log.i("test", "IOException111111111111", e);
				e.printStackTrace();
			}
		}
		Log.i("test", "7");
	}

	public static Bitmap getIconFromLocalDir() {
		Bitmap bitmap = null;
		try {
			String fileName = icon_dir_prefix + "/1.png";
			File f = new File(fileName);
			if (!f.exists()) {
				Log.i("test", "fileName = " + fileName);
				return bitmap;
			}
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, opt);
			final int REQUIRED_SIZE = 280;
			int scale = 1;
			while (opt.outWidth / scale / 2 >= REQUIRED_SIZE
					&& opt.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;
			BitmapFactory.Options opt2 = new BitmapFactory.Options();
			opt2.inSampleSize = scale;
			bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null,
					opt2);
		} catch (FileNotFoundException e) {
			Log.i("test", "getIconFromLocalDir() :", e);
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}

	public static void writeIconServerData(String s) {
		BufferedWriter bwrite = null;
		try {
			String dirName = icon_sddir_prefix;
			File f = new File(dirName);
			if (!f.exists() || !f.isDirectory()) {
				f.mkdirs();
			}
			String fileAllName = icon_sddir_prefix + "/" + iconServerDataFileName;
			File f1 = new File(fileAllName);
			if (!f1.exists()) {
				f1.createNewFile();
			}
			bwrite = new BufferedWriter(new FileWriter(f1));
			s.replaceAll("\\r|\\n","");
			bwrite.write(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bwrite != null) {
					bwrite.flush();
					bwrite.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static String readIconServerDara() {
		String fileAllName = icon_sddir_prefix + "/" + iconServerDataFileName;
		File file = new File(fileAllName);
		String result = null;
		if (file.exists()) {
			BufferedReader bread = null;
			String line = null;
			try {
				bread = new BufferedReader(new FileReader(file));
				while ((line = bread.readLine()) != null) {
					if(result ==null){
						result = line;
					}else{
						result+=line;
					}
				}
				bread.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (bread != null) {
						bread.close();
					}
				} catch (Exception e2) {
				}
			}
		}
		return result;
	}
	
	public static Bitmap zoomDrawable(Bitmap oldbmp, float scaleFactor,Resources res) {
		
		Matrix matrix = new Matrix();
		int width = oldbmp.getWidth();
		int height = oldbmp.getHeight();
		Log.i("test","scale before : scaleFactor = "+scaleFactor+" width = "+width+"   height = "+height);
		matrix.postScale(scaleFactor, scaleFactor);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		Log.i("test","scale after :  scaleFactor = "+scaleFactor+" width = "+newbmp.getWidth()+"   height = "+newbmp.getHeight());
		return newbmp;
	}
	
	public static void clearCatch(){
		File aurora_dir = new File(icon_dir_prefix);
		if(aurora_dir!=null){
			deleteDir(aurora_dir);
		}
		aurora_dir = new File(icon_sddir_prefix);
		if(aurora_dir!=null){
			deleteDir(aurora_dir);
		}
	}
	
	private static boolean deleteDir(File dir){
		if(dir.isDirectory()){
			String[] children = dir.list();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		return dir.delete();
	}

}
