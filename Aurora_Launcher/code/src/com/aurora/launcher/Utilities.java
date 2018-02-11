/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.aurora.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.aurora.launcher.R;
import com.aurora.util.DeviceProperties;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {
    @SuppressWarnings("unused")
    private static final String TAG = "Launcher.Utilities";

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconTextureHeight = -1;

    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    private static String externalFavoriteFilePath  = "/system/iuni/aurora/launcher/config/";
    
    private static String device_prefix = "";
    
    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    /**
     * Returns a bitmap suitable for the all apps view. Used to convert pre-ICS
     * icon bitmaps that are stored in the database (which were 74x74 pixels at hdpi size)
     * to the proper size (48dp)
     */
    static Bitmap createIconBitmap(Bitmap icon, Context context) {
        int textureWidth = sIconTextureWidth;
        int textureHeight = sIconTextureHeight;
        int sourceWidth = icon.getWidth();
        int sourceHeight = icon.getHeight();
        Bitmap bitmap = null;
        if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
            // Icon is bigger than it should be; clip it (solves the GB->ICS migration case)
        	bitmap = Bitmap.createBitmap(icon,
                    (sourceWidth - textureWidth) / 2,
                    (sourceHeight - textureHeight) / 2,
                    textureWidth, textureHeight);
        } else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
            // Icon is the right size, no need to change it
        	bitmap = icon;
        } else {
            // Icon is too small, render to a larger bitmap
            final Resources resources = context.getResources();
            bitmap = createIconBitmap(new BitmapDrawable(resources, icon), context);
        }
        //TODO:NOTE3
        if(DeviceProperties.isNeedScale()){
        	bitmap = zoomBitmap(bitmap, context);
        }
        return bitmap;
    }
    
    public static Bitmap zoomBitmap(Bitmap oldbmp ,Context context){
    	Matrix matrix = new Matrix();
		int width = oldbmp.getWidth();
		int height = oldbmp.getHeight();
		float scaleWidth = 0.93f;
		float scaleHeight = 0.93f;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return newbmp;
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            /*final */Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);
            //TODO:NOTE3
            if(DeviceProperties.isNeedScale()){
            	bitmap = zoomBitmap(bitmap, context);
            }
            return bitmap;
        }
    }

    static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight,
            boolean pressed, Bitmap src) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                // We can't have gotten to here without src being initialized, which
                // comes from this file already.  So just assert.
                //initStatics(context);
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }

            dest.drawColor(0, PorterDuff.Mode.CLEAR);

            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);

            float px = (destWidth - src.getWidth()) / 2;
            float py = (destHeight - src.getHeight()) / 2;
            dest.drawBitmap(mask, px + xy[0], py + xy[1],
                    pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

            mask.recycle();
        }
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                final Resources resources = context.getResources();
                return createIconBitmap(new BitmapDrawable(resources, bitmap), context);
            }
        }
    }

    static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);
            
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

            canvas.setBitmap(null);

            return disabled;
        }
    }

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorFocusedPaint.setColor(0xffff8e00);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

    /** Only works for positive numbers. */
    static int roundToPow2(int n) {
        int orig = n;
        n >>= 1;
        int mask = 0x8000000;
        while (mask != 0 && (n & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n |= mask;
            mask >>= 1;
        }
        n += 1;
        if (n != orig) {
            n <<= 1;
        }
        return n;
    }

    static int generateRandomId() {
        return new Random(System.currentTimeMillis()).nextInt(1 << 24);
    }
    
    //AURORA-START::App index, get customized bitmap::Shi guiqiang::20140106
    /*
	 * creat bitmap by customized size(left, top, right, bottom) defined by user 
	 */
	public static Bitmap drawable2bitmap(Drawable dw, int left, int top, int right, int bottom) {
		// set up new bitmap
		Bitmap bg = Bitmap.createBitmap(dw.getIntrinsicWidth(),
				dw.getIntrinsicHeight(), Config.ARGB_8888);
		// set up new canvas
		Canvas canvas = new Canvas(bg);
		// draw bounds
		dw.setBounds(left, top, right, bottom);
		dw.draw(canvas);
		// release resource
		canvas.setBitmap(null);
		return bg;
	}
	//AURORA-END::App Index, get customized bitmap::Shi guiqiang::20140106
	
	
	/**set XmlPullParser input
	 * 
	 * @param context used it for get values from xml list
	 * @param xpp use it for setInputstream input 
	 * 
	 * */
	public static void setXmlPullParserInput(Context cx, XmlPullParser xpp)
			throws XmlPullParserException, IOException {

		
		device_prefix = DeviceProperties.getSysProductDevicePrefixName();  //iuni
		String fileName = null;
		if("".equals(device_prefix)){
			fileName ="default_workspace.xml";
		}else{
			fileName ="default_workspace_"+device_prefix+".xml";
		}
		String sfile  =  externalFavoriteFilePath+fileName;
		Log.e("linp", "setXmlPullParserInput fileName="+fileName+";"+"device_prefix="+device_prefix+";"+"sfile="+sfile);
		File mExteranlFile = new File(sfile);
		FileReader freader;
		if (mExteranlFile.exists()) {
			freader = new FileReader(sfile);
			xpp.setInput(freader);
			Log.e("linp", "extenal favorite file find and will decode exteranl file very soon.."+sfile);
		} else {
			AssetManager assetManager = cx.getAssets();
			InputStream stream = null;
			try {
				stream =assetManager.open(fileName);// load default_workspace_xml  from assert directory
			} catch (Exception e) {
				stream =assetManager.open("default_workspace.xml");
			}
			xpp.setInput(stream, null);
			Log.e("linp", "extenal favorite file lost and start to read assert file...");
		}
	}
	
	/**get current language from system set*/
	public static String getCurrentLanuage(Context cx) {
        Locale locale =cx.getResources().getConfiguration().locale;
        Log.e("linp","###################Lanuage = "+locale.getLanguage());
        return locale.getLanguage();
    }
	
	/**read data from assert file*/
	public static ArrayList<String> readfileFromAssert(Context ctx,String FileName){
		ArrayList<String> result = new ArrayList<String>();
		try {
			InputStream inputStream = ctx.getAssets().open(FileName);
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));  
		    String line = "";  
		    while ((line = in.readLine()) != null) {  
		    	result.add(line.toString().trim());
            }  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  result;
	}
	
	public static List<String> splitString(String source,String regularExpression){
        List<String> list = new ArrayList<String>();
		if(source.contains(regularExpression)){
			list = Arrays.asList(source.split(regularExpression));
		}else{
			list.add(source);
		}
		return list;
	}
	
	
	/**Get Integer value from dimens list.other class can use it!*/
	public static int getIntegerValueFromResourcesDimens(Resources res,int id){
		return res.getInteger(id);
	}
	
	/**Get Boolean value from dimens list.other class can use it!*/
	public static boolean getBooleanValueFromResourcesDimens(Resources res,int id){
		return res.getBoolean(id);
	}
	
}
