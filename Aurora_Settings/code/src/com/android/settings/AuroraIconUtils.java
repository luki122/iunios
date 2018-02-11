package com.android.settings;

import java.util.HashMap;
import java.util.List;

import android.R.drawable;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class AuroraIconUtils {
	private static final String TAG = "PUBLIC_ICON";
	private static final String PUBLIC_ICON_RES = "com.aurora.publicicon.res";
	private static final String PACKAGE_CLASS = "package_class";
	private static final String LABLES_ICONS = "lable_map";
	private static final int ICON_REAL_SIZE = 178;
	private static final boolean USE_LABLE_MAP = true;
	private static final boolean USE_RIGHT_ANGLE = true;
	private static final boolean USE_ICON_SHADOW = true;
	private static final boolean IS_LAUNCHER = false;

	/*
	 * 用于后期应用图标方案
	 */
	public static Bitmap drawable2bitmap(Drawable dw) {
		// 创建新的位图
		Bitmap bg = Bitmap.createBitmap(dw.getIntrinsicWidth(),
				dw.getIntrinsicHeight(), Config.ARGB_8888);
		// 创建位图画板
		Canvas canvas = new Canvas(bg);
		// 绘制图形
		dw.setBounds(0, 0, dw.getIntrinsicWidth(), dw.getIntrinsicHeight());
		dw.draw(canvas);
		// 释放资源
		canvas.setBitmap(null);
		return bg;
	}

	public static Bitmap getRoundedBitmap(Bitmap bm) {
		return getRoundedBitmap(bm,-1);
	}
	public static Bitmap getRoundedBitmap(Bitmap bm,int customerSize) {
		// 创建新的位图
		int width = customerSize;
		if(width == -1||customerSize>bm.getWidth())width = ICON_REAL_SIZE;
		Bitmap bg = Bitmap.createBitmap(width, width,
				Config.ARGB_8888);
		// 创建位图画板
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		int extra = (bm.getWidth() - width) / 2;
		Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
		RectF rectF = new RectF(extra, extra, bm.getWidth() - extra,
				bm.getHeight() - extra);
		// 设置圆角半径
		float roundR = 12;
		paint.setAntiAlias(true);
		canvas.save();
		canvas.translate(-(float) extra, -(float) extra);
		// 绘制圆形矩形
		canvas.drawRoundRect(rectF, roundR, roundR, paint);
		// 设置图形叠加模式
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		// 绘制图形
		canvas.drawBitmap(bm, rect, rect, paint);
		canvas.restore();
		// 释放资源
		canvas.setBitmap(null);
		return bg;
	}
	//1.draw shawdow 2.draw bitmap
	public static Bitmap getShadowBitmap1(Bitmap bm, Drawable shadow) {
		final int SHADOW_LEN = 2;
		// 创建新位图
		Bitmap shadowBitmap = drawable2bitmap(shadow);
		Bitmap bg = Bitmap.createBitmap(shadowBitmap.getWidth(),
				shadowBitmap.getHeight(), Config.ARGB_8888);
		// 创建画板
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		
		// 绘制图形
		canvas.save();
		canvas.translate((shadowBitmap.getWidth()-bm.getWidth())/2, (shadowBitmap.getHeight()-bm.getHeight())/2);
		canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
		// 释放资源
		canvas.restore();
		// 绘制背景
		shadow.setBounds(0, 0, bg.getWidth(), bg.getHeight());
		shadow.draw(canvas);
		canvas.setBitmap(null);
		return bg;
	}
	
	//1.draw bitmap 2.draw shawdow
	public static Bitmap getShadowBitmap3(Bitmap bm, Drawable shadow) {
		final int SHADOW_LEN = 2;
		int bw=shadow.getIntrinsicWidth();
		int bh=shadow.getIntrinsicHeight();
		float extraW = (bw -bm.getWidth())/2;
		float extraH = (bh-bm.getHeight())/2;
				
		// 创建新位图
		Bitmap bg = Bitmap.createBitmap(shadow.getIntrinsicWidth(),
				shadow.getIntrinsicHeight(), Config.ARGB_8888);
		// 创建画板
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		// 绘制图形
		canvas.save();
		canvas.translate(extraW, extraH);
		canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
		canvas.restore();
		// 绘制背景
		shadow.setBounds(0, 0, bg.getWidth(), bg.getHeight());
		shadow.draw(canvas);
		// 释放资源
		canvas.setBitmap(null);
		return bg;
	} 

	public static Bitmap getShadowBitmap2(Bitmap bm) {
		// 创建阴影滤镜
		BlurMaskFilter blurMaskFilter = new BlurMaskFilter(30,
				BlurMaskFilter.Blur.SOLID);
		// 创建阴影画笔
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurMaskFilter);
		// 获取阴影
		int[] offsetXY = new int[2];
		Bitmap shadow = bm.extractAlpha(shadowPaint, offsetXY);
		shadow = shadow.copy(Config.ARGB_8888, true);
		// 创建新位图
		Bitmap bg = Bitmap.createBitmap(shadow.getWidth(), shadow.getHeight(),
				Config.ARGB_8888);
		// 创建画板
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		// 绘制背景
		canvas.drawBitmap(shadow, 0, 0, paint);
		// 绘制图形
		canvas.drawBitmap(bm, (shadow.getWidth() - bm.getWidth()) / 2.0f,
				(shadow.getHeight() - bm.getHeight()) / 2.0f, paint);
		// 释放资源
		canvas.setBitmap(null);
		return bg;
	}
	/*
	 * 判断图图标形状
	 */
	public static Drawable isRectangle(Drawable d,Context context) {
		Bitmap bmp = ((BitmapDrawable) d).getBitmap();
		if (null == bmp)
			return null;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Drawable finalDrawable =null;
		int s = 0;
		int l = -1;
		int r = -1;
		int t = -1;
		int b = -1;
		int i, j;
		for (i = 0; i < height; ++i) {
			for (j = 0; j < width; ++j) {
				if ((bmp.getPixel(j, i) >>> 24) > 10) {
					if (l < 0 || l > j)
						l = j;
					if (r < 0 || r < j)
						r = j;
					if (t < 0 || t > i)
						t = i;
					if (b < 0 || b < i)
						b = i;
					++s;
				}
			}
		}
		float w = r - l;
		float h = b - t;
		if (s > 0.91f * w * h) {
			if (w > 0.97f * h && w < 1.02f * h) {
				finalDrawable = clipDrawable(d, context.getResources(),r, l, b, t);
				finalDrawable = getSelfNotDrawDrawable(context, finalDrawable);
				return finalDrawable;
			}
		}
		return null;
	}
	
	private static Drawable clipDrawable(Drawable drawable,Resources res,int r,int l,int b,int t){
		Bitmap bg = Bitmap.createBitmap(Math.abs(r -l)+1,
				Math.abs(t - b)+1, Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		canvas.save();
		Bitmap oldBitmap = drawable2bitmap(drawable);
		canvas.drawBitmap(oldBitmap, -l, -t, new Paint());
		canvas.restore();
		return new BitmapDrawable(res,bg);
	}
	
	/*
	 * 判断App Icon是否需要加底托
	 */

	public static HashMap<String, String> mApp_icons = null;
	public static HashMap<String, String> mlables_icons = null;
	
	public static Bitmap getSystemIconBitmapByPackage(Context context,String packageName){
		ResolveInfo rInfo=getPackageFirstResolveInfo(context,packageName);
		if(rInfo!=null)return getSystemIconBitmap(context,rInfo);
		return null;
	}

	public static Bitmap getSystemIconBitmap(Context context, ResolveInfo info) {
		Bitmap bitmap = null;
		if (USE_LABLE_MAP) {
			CharSequence lableCharSequence = info.loadLabel(context
					.getPackageManager());
			if (lableCharSequence != null) {
				bitmap = getSystemIconByLable(context,
						lableCharSequence.toString());
			}
		} else {
			bitmap = getSystemIconBitmap(context, info.activityInfo);
		}
		return bitmap;
	}

	public static Bitmap getSystemIconBitmap(Context context, ActivityInfo info) {
		Bitmap bitmap = null;
		if (USE_LABLE_MAP) {
			CharSequence lableCharSequence = info.loadLabel(context
					.getPackageManager());
			if (lableCharSequence != null) {
				bitmap = getSystemIconByLable(context,
						lableCharSequence.toString());
			}
		} else {
			bitmap = getSystemIconBitmap(context, info.packageName, info.name);
		}
		return bitmap;
	}

	public static Bitmap getSystemIconBitmap(Context context,
			String packageName, String className) {
		String iconName = getSystemIconName(context, packageName, className);
		Bitmap bitmap = null;
		if (iconName != null)
			bitmap = getBitmapByName(context, iconName);
		return bitmap;
	}

	public static Bitmap getSystemIconByLable(Context context, String lable) {
		String iconName = getSystemIconNameByLable(context, lable);
		Bitmap bitmap = null;
		if (iconName != null)
			bitmap = getBitmapByName(context, iconName);
		return bitmap;
	}
	
	public static Drawable getSystemIconDrawableByPackage(Context context,String packageName){
		ResolveInfo rInfo=getPackageFirstResolveInfo(context,packageName);
		if(rInfo!=null)return getSystemIconDrawable(context,rInfo);
		return null;
	}

	public static Drawable getSystemIconDrawable(Context context,
			ResolveInfo info) {
		// if(info!=null)return
		// getSystemIconDrawable(context,info.activityInfo);
		Drawable drawable = null;
		if (USE_LABLE_MAP) {
			CharSequence lableCharSequence = info.loadLabel(context
					.getPackageManager());
			if (lableCharSequence != null) {
				drawable = getSystemIconDrawableByLable(context,
						lableCharSequence.toString());
			}
		} else {
			drawable = getSystemIconDrawable(context, info.activityInfo);
		}

		return drawable;
	}

	public static Drawable getSystemIconDrawable(Context context,
			ActivityInfo info) {
		Drawable drawable = null;
		if (USE_LABLE_MAP) {
			CharSequence lableCharSequence = info.loadLabel(context
					.getPackageManager());
			if (lableCharSequence != null) {
				drawable = getSystemIconDrawableByLable(context,
						lableCharSequence.toString());
			}
		} else {
			drawable = getSystemIconDrawable(context, info.packageName,
					info.name);
		}

		return drawable;
	}

	public static Drawable getSystemIconDrawable(Context context,
			String packageName, String className) {
		Drawable drawable = null;
		String iconName = getSystemIconName(context, packageName, className);
		if (iconName != null)
			drawable = getSystemIconDrawable(context, iconName);
		return drawable;
	}

	public static Drawable getSystemIconDrawableByLable(Context context,
			String lable) {
		String iconName = getSystemIconNameByLable(context, lable);
		Drawable drawable = null;
		if (iconName != null)
			drawable = getSystemIconDrawable(context, iconName);
		return drawable;
	}

	public static boolean isUseSystemIcon(Context context, ActivityInfo info) {
		if (info == null)
			return false;
		return getSystemIconName(context, info.packageName, info.name) != null;
	}

	public static boolean isUseSystemIcon(Context context, String packageName,
			String className) {
		if (packageName == null || className == null)
			return false;
		return getSystemIconName(context, packageName, className) != null;
	}

	private static String getSystemIconName(Context context,
			String packageName, String className) {
		String iconName = null;
		if (mApp_icons == null)
			mApp_icons = configClassArray(context, PACKAGE_CLASS);
		String bitmapName = mApp_icons.get(packageName + "$" + className);
		if (bitmapName != null) {
			String[] s = bitmapName.split("\\.");
			if (s.length == 2) {
				iconName = s[0];
			}
		}
		return iconName;
	}

	private static String getSystemIconNameByLable(Context context, String lable) {
		if (mlables_icons == null) {
			mlables_icons = configClassArray(context, LABLES_ICONS);
		}
		String bitmapName = mlables_icons.get(lable.trim());
		String iconName = null;
		if (bitmapName != null) {
			String[] s = bitmapName.split("\\.");
			if (s.length == 2) {
				iconName = s[0];
			}
		}
		return iconName;
	}

	private static Drawable getSystemIconDrawable(Context context,
			String iconName) {
		Context prePackageContext = null;
		try {
			prePackageContext = context.createPackageContext(PUBLIC_ICON_RES,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Create Res Apk Failed:NameNotFoundException");
		} catch (Exception e) {
			Log.e(TAG, "Create Res Apk Failed:Exception");
		}
		Drawable drawable = null;
		Resources resources = prePackageContext.getResources();
		if (prePackageContext != null) {
			int resId = resources.getIdentifier(iconName, "drawable",
					PUBLIC_ICON_RES);
			if (resId == 0) {
				return null;
			}
			drawable = resources.getDrawable(resId);
		}
		if (USE_RIGHT_ANGLE && drawable != null) {
			int shadowId = 0;
			if(IS_LAUNCHER){
				shadowId = resources.getIdentifier("shadow", "drawable",
						PUBLIC_ICON_RES);
			}else{
				shadowId = resources.getIdentifier("shadow_app", "drawable",
						PUBLIC_ICON_RES);
			}
			
			Bitmap bitmap1 = drawable2bitmap(drawable);
			Bitmap bitmap2 = getRoundedBitmap(bitmap1);
			if(USE_ICON_SHADOW){
				//drawable = resources.getDrawable(shadowId);
				Bitmap bitmap3 = null;
				if (shadowId != 0) {
					Drawable shadow = resources.getDrawable(shadowId);
					if(shadow!=null){
						if(IS_LAUNCHER){
							bitmap3 = getShadowBitmap1(bitmap2, shadow);
						}else{
							bitmap3 = getShadowBitmap3(bitmap2, shadow);
						}
						drawable = new BitmapDrawable(resources, bitmap3);
					}else{
						drawable = new BitmapDrawable(resources, bitmap2);
					}
				}
			}else{
				drawable = new BitmapDrawable(resources, bitmap2);
			}

		}
		return drawable;
	}
	
	public  static  Drawable getSelfNotDrawDrawable(Context context,Drawable d){
		Context prePackageContext = null;
		try {
			prePackageContext = context.createPackageContext(PUBLIC_ICON_RES,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Create Res Apk Failed:NameNotFoundException");
		} catch (Exception e) {
			Log.e(TAG, "Create Res Apk Failed:Exception");
		}
		Resources resources = prePackageContext.getResources();
		Drawable drawable = null;
		if (USE_RIGHT_ANGLE && d != null) {
			int shadowId = resources.getIdentifier("shadow_not_self", "drawable",
					PUBLIC_ICON_RES);
			Bitmap bitmap1 = drawable2bitmap(d);
			bitmap1 = zoomDrawable(bitmap1,ICON_REAL_SIZE,ICON_REAL_SIZE,resources);
			Bitmap bitmap2 = getRoundedBitmap(bitmap1);
			if(USE_ICON_SHADOW){
				Bitmap bitmap3 = null;
				if (shadowId != 0) {
					Drawable shadow = resources.getDrawable(shadowId);
					if (shadow != null && IS_LAUNCHER) {
						bitmap3 = getShadowBitmap1(bitmap2, shadow);
						drawable = new BitmapDrawable(resources, bitmap3);
					} else {
						drawable = new BitmapDrawable(resources, bitmap2);
					}
				}else{
					drawable = new BitmapDrawable(resources, bitmap2);
				}
			}else{
				drawable = new BitmapDrawable(resources, bitmap2);
			}

		}
		return drawable;
	}
	
	private static Bitmap zoomDrawable(Bitmap oldbmp, int finalWidth, int finalHeight,Resources res) {
		Matrix matrix = new Matrix();
		int width = oldbmp.getWidth();
		int height = oldbmp.getHeight();
		float scaleWidth = ((float) finalWidth / width);
		float scaleHeight = ((float) finalHeight / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return newbmp;
	}
	private static Bitmap getBitmapByName(Context context, String drawableName) {
		Bitmap bitmap = null;
		Drawable drawable = getSystemIconDrawable(context, drawableName);
		if (drawable != null) {
			bitmap = drawable2bitmap(drawable);
		}
		return bitmap;
	}

	public static HashMap<String, String> configClassArray(Context context,
			String arrayName) {
		Context prePackageContext = null;
		try {
			prePackageContext = context.createPackageContext(PUBLIC_ICON_RES,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Create Res Apk Failed:NameNotFoundException");
		} catch (Exception e) {
			Log.e(TAG, "Create Res Apk Failed");
		}
		HashMap<String, String> packageClassMap = new HashMap<String, String>();
		if (prePackageContext != null) {
			final int resId = prePackageContext.getResources().getIdentifier(
					arrayName, "array", PUBLIC_ICON_RES);
			if (resId == 0) {
				return null;
			}
			final String[] packageClasseIcons = prePackageContext
					.getResources().getStringArray(resId);
			for (String packageClasseIcon : packageClasseIcons) {
				String[] packageClasses_Icon = packageClasseIcon.split("#");
				if (packageClasses_Icon.length == 2) {
					String[] packageClasses = packageClasses_Icon[0]
							.split("\\|");
					for (String s : packageClasses) {
						packageClassMap.put(s.trim(), packageClasses_Icon[1]);
					}
				}
			}
		}
		return packageClassMap;
	}
	
	private static ResolveInfo getPackageFirstResolveInfo(Context context,String packageName){
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		it.setPackage(packageName);
		List<ResolveInfo> list=context.getPackageManager().queryIntentActivities(it, 0);
		ResolveInfo resolveInfo=null;
		if(!list.isEmpty()){
			resolveInfo = list.get(0);
		}
		return resolveInfo;
	}
}