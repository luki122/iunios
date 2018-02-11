package com.aurora.apihook.wallpaper;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;


public class WallpaperManagerHook implements Hook {
	public final static String TAG = "WallpaperManagerHook";

	public WallpaperManagerHook() {

	}

	public void before_setBitmap(MethodHookParam param) {
		Log.e(TAG,"before_setBitmap has invoked.");
		Object sGlobals = ClassHelper.getStaticObjectField(android.app.WallpaperManager.class,
				"sGlobals");
		Object mService = ClassHelper.getObjectField(sGlobals, "mService");
		if (mService == null) {
			Log.i(TAG, "method setBitmap mService is null.");
			return;
		}
		Bitmap bitmap =null;
		if(param.args!=null&&param.args.length>0){
			bitmap = (Bitmap)param.args[0];
		}
		if (bitmap == null) {
			return;
		}
		int w = bitmap.getWidth(), h = bitmap.getHeight();
		int[] pixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

		byte[] rgb = auroraAddBMP_RGB_888(pixels, w, h);
		byte[] header = auroraAddBMPImageHeader(rgb.length);
		byte[] infos = auroraAddBMPImageInfosHeader(w, h);

		byte[] buffer = new byte[54 + rgb.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(infos, 0, buffer, 14, infos.length);
		System.arraycopy(rgb, 0, buffer, 54, rgb.length);

		try {
			Context context = (Context) (ClassHelper.getObjectField(
					param.thisObject, "mContext"));
			int permission = context
					.checkCallingOrSelfPermission(android.Manifest.permission.SET_WALLPAPER_HINTS);
			if (PackageManager.PERMISSION_GRANTED == permission) {
				DisplayMetrics dm = context.getResources().getDisplayMetrics();
				if (dm.widthPixels > dm.heightPixels) {
					int swap = dm.widthPixels;
					dm.widthPixels = dm.heightPixels;
					dm.heightPixels = swap;
				}
				if (bitmap.getWidth() < bitmap.getHeight()) {
					ClassHelper.callMethod(param.thisObject, "suggestDesiredDimensions", dm.widthPixels,dm.heightPixels);
				} else {
					ClassHelper.callMethod(param.thisObject, "suggestDesiredDimensions", 2*dm.widthPixels,dm.heightPixels);
				}
			}
			ParcelFileDescriptor fd =(ParcelFileDescriptor)(ClassHelper.callMethod(mService, "setWallpaper"));
			if (fd == null) {
				return;
			}
			FileOutputStream fos = null;
			try {
				fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
				fos.write(buffer);
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		} catch (Exception e) {
			Log.i(TAG,"method setBitmap mService handle Exception.",e);
		}
		param.setResult(null);

	}

	private byte[] auroraAddBMP_RGB_888(int[] b, int w, int h) {
		int len = b.length;
		byte[] buffer = new byte[w * h * 3];
		int offset = 0;
		for (int i = len - 1; i >= w; i -= w) {
			// DIB文件格式最后一行为第一行，每行按从左到右顺序
			int end = i, start = i - w + 1;
			for (int j = start; j <= end; j++) {
				buffer[offset] = (byte) (b[j] >> 0);
				buffer[offset + 1] = (byte) (b[j] >> 8);
				buffer[offset + 2] = (byte) (b[j] >> 16);
				offset += 3;
			}
		}
		return buffer;
	}

	// BMP文件头
	private byte[] auroraAddBMPImageHeader(int size) {
		byte[] buffer = new byte[14];
		buffer[0] = 0x42;
		buffer[1] = 0x4D;
		buffer[2] = (byte) (size >> 0);
		buffer[3] = (byte) (size >> 8);
		buffer[4] = (byte) (size >> 16);
		buffer[5] = (byte) (size >> 24);
		buffer[6] = 0x00;
		buffer[7] = 0x00;
		buffer[8] = 0x00;
		buffer[9] = 0x00;
		buffer[10] = 0x36;
		buffer[11] = 0x00;
		buffer[12] = 0x00;
		buffer[13] = 0x00;
		return buffer;
	}

	// BMP文件信息头
	private byte[] auroraAddBMPImageInfosHeader(int w, int h) {
		byte[] buffer = new byte[40];
		buffer[0] = 0x28;
		buffer[1] = 0x00;
		buffer[2] = 0x00;
		buffer[3] = 0x00;
		buffer[4] = (byte) (w >> 0);
		buffer[5] = (byte) (w >> 8);
		buffer[6] = (byte) (w >> 16);
		buffer[7] = (byte) (w >> 24);
		buffer[8] = (byte) (h >> 0);
		buffer[9] = (byte) (h >> 8);
		buffer[10] = (byte) (h >> 16);
		buffer[11] = (byte) (h >> 24);
		buffer[12] = 0x01;
		buffer[13] = 0x00;
		buffer[14] = 0x18;
		buffer[15] = 0x00;
		buffer[16] = 0x00;
		buffer[17] = 0x00;
		buffer[18] = 0x00;
		buffer[19] = 0x00;
		buffer[20] = 0x00;
		buffer[21] = 0x00;
		buffer[22] = 0x00;
		buffer[23] = 0x00;
		buffer[24] = (byte) 0xE0;
		buffer[25] = 0x01;
		buffer[26] = 0x00;
		buffer[27] = 0x00;
		buffer[28] = 0x02;
		buffer[29] = 0x03;
		buffer[30] = 0x00;
		buffer[31] = 0x00;
		buffer[32] = 0x00;
		buffer[33] = 0x00;
		buffer[34] = 0x00;
		buffer[35] = 0x00;
		buffer[36] = 0x00;
		buffer[37] = 0x00;
		buffer[38] = 0x00;
		buffer[39] = 0x00;
		return buffer;
	}
}
