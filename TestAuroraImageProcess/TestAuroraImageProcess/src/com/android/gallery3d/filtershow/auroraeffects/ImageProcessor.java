package com.android.gallery3d.filtershow.auroraeffects;

import android.graphics.Bitmap;

public class ImageProcessor {
	
	public static final int MEID_NORMAL = 0xFFFFFFFF;//SQF ADDED. not in doc.
	public static final int MEID_FLEETINGTIME = 0x0000;
	public static final int MEID_CRAYON	= 0x0001;
	public static final int MEID_SNOWFLAKES = 0x0002;
	public static final int MEID_LIGHTBEAM = 0x0003;
	public static final int MEID_REFLECTION = 0x0004;
	public static final int MEID_SUNSET = 0x0005;
	public static final int MEID_REVERSAL = 0x0006;
	public static final int MEID_WARMLOMO = 0x0007;
	public static final int MEID_COLDLOMO = 0x0008;
	public static final int MEID_SOFTPINK = 0x0009;
	public static final int MEID_JAPANBACKLIGHT = 0x000a;
	public static final int MEID_COSMETOLOGY_BACKLIGHT = 0x000b;
	
	static {
        //System.loadLibrary("/system/lib/libAuroraImageProcess.so");
		System.loadLibrary("AuroraImageProcess");
	}

	public static native Bitmap generateFilteredBitmap(Bitmap src);
	
	public static native Bitmap generateFilteredBitmap(Bitmap src, int type);
	
	public static Bitmap generate(Bitmap src) {
		return generateFilteredBitmap(src);
	}
	
	public static Bitmap generate(Bitmap src, int type) {
		return generateFilteredBitmap(src, type);
	}

}
