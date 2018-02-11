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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Utils2Icon{
	public static final String TAG = "PUBLIC_ICON";
	private static Utils2Icon instance = null;
	private Context mPrePackageContext = null;
	private static final String PUBLIC_ICON_RES = "com.aurora.publicicon.res";
	private static final String PACKAGE_CLASS = "lable_map";
	private static int ICON_REAL_SIZE = 178;
	private static int APP_ICON_RADIUS = 12;
	private static final boolean USE_RIGHT_ANGLE = true;
	private static final boolean USE_ICON_SHADOW = true;
	/* Shadow type */
	public static final int OUTER_SHADOW = 0;
	public static final int INTER_SHADOW = 1;
	public static final int OUTER_NONE_SHADOW_WITH_BOTTOM = 2;
	public static final int INTER_NONE_SHADOW_WITH_BOTTOM = 3;
	public static final int NONE_SHADOW = 4;
	private static int mIconDpi;
	private ExecutorService mThreadPool;
	private static String icon_dir_prefix = "/data/aurora/icons";//"/system/iuni/aurora/launcher";
	private static String inter_icon_dir_suffix = "/inter_icons/";
	private static String outer_icon_dir_suffix = "/outer_icons/";
	private static String icon_sddir_prefix = "/sdcard/icons";
	private static String inter_icon_dir;
	private static String outer_icon_dir;
	private static String inter_icon_sddir;
	private static String outer_icon_sddir;
	private  static final String ICON_VERSION_KEY = "icon_version";
	private static final String ICON_VERSION_FILENAME = "/version.config";
	
	private Utils2Icon(Context context){
		Log.i(TAG,"initing : Utils2Icon");
		mThreadPool = Executors.newCachedThreadPool();
		configClassArray(context, PACKAGE_CLASS);
		initVersion();
		Log.i(TAG,"inited : Utils2Icon");
	}
	
	public synchronized static Utils2Icon getInstance(Context context){
		if(instance==null)instance = new Utils2Icon(context);
		return instance;
	}
	
	private void initVersion(){
		Context context = mPrePackageContext;
		File file  = new File(icon_dir_prefix);
		if(!file.exists()||!file.isDirectory()){
			file.mkdirs();
		}
		String versionFilePath = icon_dir_prefix+ICON_VERSION_FILENAME;
		String launcherResOldVersion=getIconOldVersion(versionFilePath, ICON_VERSION_KEY, "1.000");;
		if(isLauncherResVersionChanged(context,versionFilePath,ICON_VERSION_KEY,launcherResOldVersion)){
			deleteDir(new File(inter_icon_dir));
			deleteDir(new File(outer_icon_dir));
		}
	}

	/*
	 * 鐢ㄤ簬鍚庢湡搴旂敤鍥炬爣鏂规
	 */
	public  Bitmap drawable2bitmap(Drawable dw) {
		// 鍒涘缓鏂扮殑浣嶅浘
		Bitmap bg = Bitmap.createBitmap(dw.getIntrinsicWidth(),
				dw.getIntrinsicHeight(), Config.ARGB_8888);
		// 鍒涘缓浣嶅浘鐢绘澘
		Canvas canvas = new Canvas(bg);
		// 缁樺埗鍥惧舰
		dw.setBounds(0, 0, dw.getIntrinsicWidth(), dw.getIntrinsicHeight());
		dw.draw(canvas);
		// 閲婃斁璧勬簮
		canvas.setBitmap(null);
		return bg;
	}

	public  Bitmap getRoundedBitmap(Bitmap bm) {
		return getRoundedBitmap(bm,-1,false);
	}
	
	public Bitmap  getRoundedBitmap(Bitmap bm,int customerSize){
		return getRoundedBitmap(bm,customerSize,false);
	}
	
	public  Bitmap getRoundedBitmap(Bitmap bm,int customerSize,boolean needScale) {
		// 鍒涘缓鏂扮殑浣嶅浘
		int width = customerSize;
		//if(width < 0 || customerSize>bm.getWidth()){
			width = ICON_REAL_SIZE;
		//}
		Bitmap bg = Bitmap.createBitmap(width, width,
				Config.ARGB_8888);
		// 鍒涘缓浣嶅浘鐢绘澘
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		int extraW = (bm.getWidth() - width) / 2;
		if(2*extraW<bm.getWidth()-width){
			extraW++;
		}	
		//here need to be redesign
		int extraH = (bm.getHeight() - width) / 2;
		if(2*extraH<bm.getHeight()-width){
			extraH++;
		}
		extraW = extraW>50?10:extraW;
		extraH = extraH>50?10:extraH;
		Rect rect = new Rect(extraW, extraH, bm.getWidth() - extraW,
				bm.getHeight() - extraH);
		Rect rect2 = new Rect(0, 0, bg.getWidth(), bg.getHeight());
		RectF rectF = new RectF(extraW, extraH, bm.getWidth() - extraW,
				bm.getHeight() - extraH);
		float roundR = APP_ICON_RADIUS;
		paint.setAntiAlias(true);
		canvas.save();
		canvas.translate(-(float) extraW, -(float) extraH);
		canvas.drawRoundRect(rectF, roundR, roundR, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.restore();
		canvas.save();
		canvas.drawBitmap(bm, rect, rect2, paint);
		canvas.restore();
		canvas.setBitmap(null);
		return bg;
	}
	//1.draw shawdow 2.draw bitmap
	public  Bitmap getShadowBitmap1(Bitmap bm, Drawable shadow) {
		Bitmap shadowBitmap = drawable2bitmap(shadow);
		Bitmap bg = Bitmap.createBitmap(shadowBitmap.getWidth(),
				shadowBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		
		canvas.save();
		canvas.translate((shadowBitmap.getWidth()-bm.getWidth())/2, (shadowBitmap.getHeight()-bm.getHeight())/2);
		canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
		canvas.restore();
		shadow.setBounds(0, 0, bg.getWidth(), bg.getHeight());
		shadow.draw(canvas);
		canvas.setBitmap(null);
		return bg;
	}
	
	//1.draw bitmap 2.draw shawdow
	public  Bitmap getShadowBitmap3(Bitmap bm, Drawable shadow) {
		int bw=shadow.getIntrinsicWidth();
		int bh=shadow.getIntrinsicHeight();
		float extraW = (bw -bm.getWidth())/2;
		float extraH = (bh-bm.getHeight())/2;
				
	Bitmap bg = Bitmap.createBitmap(shadow.getIntrinsicWidth(),
				shadow.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		canvas.save();
		canvas.translate(extraW, extraH);
		canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
		canvas.restore();
		shadow.setBounds(0, 0, bg.getWidth(), bg.getHeight());
		shadow.draw(canvas);
		canvas.setBitmap(null);
		return bg;
	} 

	public static Bitmap getShadowBitmap2(Bitmap bm) {
		BlurMaskFilter blurMaskFilter = new BlurMaskFilter(30,
				BlurMaskFilter.Blur.SOLID);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurMaskFilter);
		int[] offsetXY = new int[2];
		Bitmap shadow = bm.extractAlpha(shadowPaint, offsetXY);
		shadow = shadow.copy(Config.ARGB_8888, true);
		Bitmap bg = Bitmap.createBitmap(shadow.getWidth(), shadow.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		Paint paint = new Paint();
		canvas.drawBitmap(shadow, 0, 0, paint);
		canvas.drawBitmap(bm, (shadow.getWidth() - bm.getWidth()) / 2.0f,
				(shadow.getHeight() - bm.getHeight()) / 2.0f, paint);
		canvas.setBitmap(null);
		return bg;
	}
	
	private Drawable getSystemIconDrawable(String iconName, final int  isUseOuterShadow ) {
		Context prePackageContext = mPrePackageContext;
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
		drawable = getSystemIconDrawable(drawable,isUseOuterShadow);
		return drawable;
	}
	
	public  Drawable getSystemIconDrawable(Drawable d,final int iconMode){
		Context prePackageContext = mPrePackageContext;
		Resources resources = prePackageContext.getResources();
		Drawable drawable = d;
		if (USE_RIGHT_ANGLE && drawable != null) {
			int shadowId = 0;
			switch (iconMode) {
			case OUTER_SHADOW:
				shadowId = resources.getIdentifier("shadow", "drawable",
						PUBLIC_ICON_RES);
				break;
			case INTER_SHADOW:
				shadowId = resources.getIdentifier("shadow_app", "drawable",
						PUBLIC_ICON_RES);
				break;
			case OUTER_NONE_SHADOW_WITH_BOTTOM:
				shadowId = resources.getIdentifier("shadow_not_self", "drawable",
						PUBLIC_ICON_RES);
				break;
			case INTER_NONE_SHADOW_WITH_BOTTOM:
				shadowId = resources.getIdentifier("shadow_not_self_inter", "drawable",
						PUBLIC_ICON_RES);
				break;
			case NONE_SHADOW:
				//Maybe one day will use this tag;
				break;
			default:
				break;
			}	
			Bitmap bitmap1 = drawable2bitmap(drawable);
			Bitmap bitmap2 = null;
			
			if(iconMode == OUTER_NONE_SHADOW_WITH_BOTTOM || iconMode==INTER_NONE_SHADOW_WITH_BOTTOM){
				bitmap1 = zoomDrawable(bitmap1,ICON_REAL_SIZE,ICON_REAL_SIZE,resources);
				bitmap2 = getRoundedBitmap(bitmap1,ICON_REAL_SIZE);
			}else{
				bitmap2 = getRoundedBitmap(bitmap1);
			}
			Log.i("test","ICON_REAL_SIZE = "+ICON_REAL_SIZE+"   ,    USE_ICON_SHADOW = "+USE_ICON_SHADOW+"   ,   iconMode = "+iconMode);
			if(USE_ICON_SHADOW){
				Bitmap bitmap3 = null;
				if (shadowId != 0) {
					Drawable shadow = resources.getDrawable(shadowId);
					if(shadow!=null){
						bitmap3 = getShadowBitmap1(bitmap2, shadow);
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
	
	/*
	 * 鍒ゆ柇鍥惧浘鏍囧舰鐘�	 */
	private Drawable isRectangle(Drawable d,int iconMode) {
		Context context = mPrePackageContext;
		Bitmap bmp = ((BitmapDrawable) d).getBitmap();
		if (null == bmp)
			return null;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int DEST_SIZE = 230; // TODO magic number is good enough for now, should go with display info
        if (width > DEST_SIZE || height > DEST_SIZE) {
            // Resize bitmap
            float scale = 250f / Math.max(width, height);
            int destWidth = (int)(width * scale);
            int destHeight = (int)(height * scale);
            bmp = Bitmap.createScaledBitmap(bmp, destWidth, destHeight, false);
            width = bmp.getWidth();
            height = bmp.getHeight();
        }
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
				//finalDrawable = clipDrawable(d, context.getResources(),r, l, b, t);
				finalDrawable = clipDrawable(new BitmapDrawable(context.getResources(),bmp), context.getResources(),r, l, b, t);
				finalDrawable = getSystemIconDrawable(finalDrawable ,iconMode);
				return finalDrawable;
			}
		}
		return null;
	}
	
	private Drawable clipDrawable(Drawable drawable,Resources res,int r,int l,int b,int t){
		Bitmap bg = Bitmap.createBitmap(Math.abs(r -l)+1,
				Math.abs(t - b)+1, Config.ARGB_8888);
		Canvas canvas = new Canvas(bg);
		canvas.save();
		Bitmap oldBitmap = drawable2bitmap(drawable);
		canvas.drawBitmap(oldBitmap, -l, -t, new Paint());
		canvas.restore();
		return new BitmapDrawable(res,bg);
	}
	
	
	private Bitmap zoomDrawable(Bitmap oldbmp, int finalWidth, int finalHeight,Resources res) {
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
	
	
	/*
	 * 鍒ゆ柇App Icon鏄惁闇�鍔犲簳鎵�	 */

	private HashMap<String, String> mlables_icons = null;
	public Bitmap getIcon(ResolveInfo info,int iconMode){
		Bitmap icon = null;
		if(info.activityInfo!=null){
			icon = getIcon(info.activityInfo,iconMode);
		}else{
			icon = getIcon(info.serviceInfo,iconMode);
		}
		return icon;
	}
	
	public  Bitmap getIcon(ActivityInfo info,int iconMode){
		Bitmap icon = null;
		icon = getIconFromCacheDir(info,iconMode);
		if (icon == null) {
			String iconName = getIconName(info);
			if (iconName != null) {
				icon = getBitmapByName(iconName, iconMode);
			}
			if (icon == null) {
				icon = getCustomerIcon(info, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(icon, info.packageName, info.name, iconMode));
			}
			if(icon == null){
				icon = getDefaultIcon(iconMode);
			}
		}
		return icon;
	}
	
	public Bitmap getIcon(ServiceInfo info,int iconMode){
		Bitmap icon = null;
		icon = getIconFromCacheDir(info,iconMode);
		if (icon == null) {
			String iconName = getIconName(info);
			if (iconName != null) {
				icon = getBitmapByName(iconName, iconMode);
			}
			if (icon == null) {
				icon = getCustomerIcon(info, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(icon, info.packageName, info.name, iconMode));
			}
			if(icon == null){
				icon = getDefaultIcon(iconMode);
			}
		}
		return icon;
	}

	public Bitmap getIcon(String pkg,String cls ,int iconMode){
		Context context = mPrePackageContext;
		Bitmap icon = null;
		icon = getIconFromCacheDir(pkg,cls,iconMode);
		if (icon == null) {
			CharSequence key = pkg + "$" + cls;
			String iconName = getIconName( key.toString());
			if (iconName != null) {
				icon = getBitmapByName(iconName, iconMode);
			}
			if(icon==null){
				PackageManager pm = context.getPackageManager();
				ApplicationInfo info;
				try {
					info = pm.getApplicationInfo(pkg, 0);
				} catch (NameNotFoundException e1) {
					Log.i(TAG, "getApkFileIcon  :  NameNotFoundException  :  "
							+ pkg);
					info = null;
				}
				if(info!=null){
					key = info.loadLabel(pm);
					if(key!=null){
						iconName = getIconName( key.toString());
					}
					if (iconName != null) {
						icon = getBitmapByName(iconName, iconMode);
					}	
				}
			}
			if (icon == null) {
				icon = getCustomerIcon(pkg, cls, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(icon, pkg , cls , iconMode));
			}
			
			if(icon == null){
				icon = getDefaultIcon(iconMode);
			}
		}
		return icon;
	}
	
	public Bitmap getIcon(String pkg, int iconMode) {
		Context context = mPrePackageContext;
		Bitmap icon = null;
		icon = getIconFromCacheDir(pkg, null, iconMode);
		if (icon == null) {
			String iconName = getIconName(pkg);
			if (iconName == null) {
				PackageManager pm = context.getPackageManager();
				ApplicationInfo info = null;
				try {
					info = pm.getApplicationInfo(pkg, 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				CharSequence key = null;
				if(info != null){
					key = info.loadLabel(pm);
				}
				if (key != null){
					iconName = getIconName(pkg);
				}
			}
			if(iconName!=null){
				icon = getBitmapByName(iconName, iconMode);
			}
			if (icon == null) {
				icon = getCustomIcon(context, pkg, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(icon, pkg , null , iconMode));
			}
			
			if(icon == null){
				icon = getDefaultIcon(iconMode);
			}
		}
		return icon;
	}

	public Drawable getIconDrawable(ResolveInfo info,int iconMode){
		Drawable icon = null;
		if(info.activityInfo!=null){
			icon = getIconDrawable(info.activityInfo,iconMode);
		}else{
			icon = getIconDrawable(info.serviceInfo,iconMode);
		}
		return icon;
	}
	
	public Drawable getIconDrawable(ActivityInfo info, int iconMode) {
		Drawable icon = null;
		icon = getIconFromCacheDirDrawable(info, iconMode);
		if (icon == null) {
			String iconName = getIconName(info);
			Log.i(TAG,"iconName = "+iconName);
			if (iconName != null) {
				icon = getSystemIconDrawable(iconName, iconMode);
			}
			if (icon == null) {
				icon = getCustomerIconDrawable(info, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(drawable2bitmap(icon), info.packageName, info.name, iconMode));
			}
			if(icon == null){
				icon = getDefaultIconDrawable(iconMode);
			}
		}
		return icon;
	}
	
	public Drawable getIconDrawable(ServiceInfo info,int iconMode){
		Drawable icon = null;
		icon = getIconFromCacheDirDrawable(info, iconMode);
		if (icon == null) {
			String iconName = getIconName(info);
			if (iconName != null) {
				icon = getSystemIconDrawable(iconName, iconMode);
			}
			if (icon == null) {
				icon = getCustomerIconDrawable(info, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(drawable2bitmap(icon), info.packageName, info.name, iconMode));
			}
			if(icon == null){
				icon = getDefaultIconDrawable(iconMode);
			}
		}
		return icon;
	}

	public Drawable getIconDrawable(String pkg, String cls, int iconMode) {
		Drawable icon = null;
		icon = getIconFromLocalDirDrawable(pkg, cls, iconMode);
		if (icon == null) {
			CharSequence key = pkg + "$" + cls;
			String iconName = getIconName(key.toString());
			if (iconName != null) {
				icon = getSystemIconDrawable(iconName, iconMode);
			}
			if (icon == null) {
				icon = getCustomerIconDrawable(pkg, cls, iconMode);
			}
			//TODO:SAVE
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(drawable2bitmap(icon), pkg, cls, iconMode));
			}
			if(icon == null){
				icon = getDefaultIconDrawable(iconMode);
			}
		}
		return icon;
	}
	
	public Drawable getIconDrawable( String pkg, int iconMode) {
		Context context = mPrePackageContext;
		Drawable icon = null;
		icon = getIconFromLocalDirDrawable(pkg, null, iconMode);
		CharSequence key = null;
		if (icon == null) {
			String iconName = getIconName(pkg);
			if (iconName == null) {
				PackageManager pm = context.getPackageManager();
				ApplicationInfo info = null;
				try {
					info = pm.getApplicationInfo(pkg, 0);
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					Log.i(TAG,"NameNotFoundException : "+pkg);
					e.printStackTrace();
				}
				if(info != null){
					key = info.loadLabel(pm);
				}
				if (key != null){
					//icon = getIcon(rInfo, iconMode);
					iconName = getIconName(key.toString());
				}
			}
			Log.i(TAG,"iconName = "+iconName+"   key = "+key);
			if(iconName!=null){
				icon = getSystemIconDrawable(iconName, iconMode);
			}
			if(icon==null){
				icon = getCustomIconDrawable(context, pkg, iconMode);
				
			}
			if(icon!=null){
				mThreadPool.execute(new SaveIconRunnable(drawable2bitmap(icon), pkg,null, iconMode));
			}
			if(icon == null){
				icon = getDefaultIconDrawable(iconMode);
			}
		}
		return icon;
	}
	
	private Bitmap getBitmapByName(String drawableName,int iconMode) {
		Bitmap bitmap = null;
		Drawable drawable = getSystemIconDrawable(drawableName,iconMode);
		if (drawable != null) {
			bitmap = drawable2bitmap(drawable);
		}
		return bitmap;
	}
	
	private Bitmap getCustomerIcon(String pkg,String cls,int iconMode){
		Drawable d = getCustomerIconDrawable( pkg,cls, iconMode);
		return d!=null?((BitmapDrawable)d).getBitmap():null;
	}
	
	private  Drawable getCustomerIconDrawable(String pkg,String cls,int iconMode){
		ResolveInfo info = getResolveInfo(pkg,cls);
		return getCustomerIconDrawable(info,iconMode);
	}
	
	private  Bitmap getCustomerIcon(ServiceInfo info, int iconMode){
		Drawable d = getCustomerIconDrawable(info, iconMode);
		return d!=null?((BitmapDrawable)d).getBitmap():null;
	}
	
	private  Bitmap getCustomerIcon(ActivityInfo info, int iconMode){
		Drawable d = getCustomerIconDrawable(info, iconMode);
		return d!=null?((BitmapDrawable)d).getBitmap():null;
	}
	
	private Bitmap getCustomIcon(Context context,String pkg,int iconMode){
		Drawable d = getCustomIconDrawable(context , pkg ,iconMode);
		return d!=null?((BitmapDrawable)d).getBitmap():null;
	}
	
	private Drawable getCustomIconDrawable(Context context,String pkg,int iconMode){
		PackageManager pm = context.getPackageManager();
		ApplicationInfo info;
		try {
			info = pm.getApplicationInfo(pkg, 0);
		} catch (NameNotFoundException e1) {
			Log.i(TAG, "getApkFileIcon  :  NameNotFoundException  :  "
					+ pkg);
			info = null;
		}
		
		// try to get the icon from application
		Drawable d = null;
		if(info!=null){
			try {
				d = info.loadIcon(pm);
			} catch (Resources.NotFoundException e) {
				d = null;
			}
		}
		return getCustomerIcon(d, iconMode);
	}
	
	private  Drawable getCustomerIconDrawable(ResolveInfo info, int iconMode){
		Drawable d = null;
		if (info != null) {
			if (info != null) {
				if (info.activityInfo != null) {
					d = getCustomerIconDrawable(info.activityInfo, iconMode);
				} else if (info.serviceInfo != null) {
					d = getCustomerIconDrawable(info.serviceInfo, iconMode);
				}
			}
		}else{
			d = getCustomerIcon(d, iconMode);
		}
		return d;
	}
	
	private Drawable getCustomerIconDrawable(ServiceInfo info, int iconMode) {
		Drawable d = null;
		if (info != null) {
			Context context = mPrePackageContext;
			PackageManager pm = context.getPackageManager();
			Resources res;
			try {
				res = pm.getResourcesForApplication(info.applicationInfo);
			} catch (NameNotFoundException e) {
				res = null;
				Log.i(TAG, "exception has catched!", e);
			}

			// try to get the icon from application
			if (res != null) {
				int iconId = info.getIconResource();
				if (iconId != 0) {
					try {
						d = res.getDrawableForDensity(iconId, mIconDpi);
					} catch (Resources.NotFoundException e) {
						d = null;
					}
				}else{
					d = pm.getApplicationIcon(info.applicationInfo);
				}
			}
		}
		return getCustomerIcon(d, iconMode);
	}
	
	private  Drawable getCustomerIconDrawable(ActivityInfo info,int iconMode){
		Drawable d = null;
		if (info != null) {
			Context context = mPrePackageContext;
			PackageManager pm = context.getPackageManager();
			Resources res;
			try {
				res = pm.getResourcesForApplication(info.applicationInfo);
			} catch (NameNotFoundException e) {
				res = null;
				Log.i(TAG, "exception has catched!", e);
			}
			// try to get the icon from application

			if (res != null) {
				int iconId = info.getIconResource();
				Log.i(TAG,"iconId = "+iconId);
				if (iconId != 0) {
					try {
						d = res.getDrawableForDensity(iconId, mIconDpi);
					} catch (Resources.NotFoundException e) {
						d = null;
					}
				}else{
					d = pm.getApplicationIcon(info.applicationInfo);
				}
			}
		}
		return getCustomerIcon(d, iconMode);
	}
	
	public Drawable getCustomerIcon( Drawable d, int iconMode) {
		if(d==null)return null;
		Context prePackageContext = mPrePackageContext;
		// we try to distinguish the icon from application is rectangle or
		// not
		int iconChangeMode = 0;
		if (iconMode == INTER_SHADOW) {
			iconChangeMode = INTER_NONE_SHADOW_WITH_BOTTOM;
		} else if (iconMode == OUTER_SHADOW) {
			iconChangeMode = OUTER_NONE_SHADOW_WITH_BOTTOM;
		} else {
			iconChangeMode = iconMode;
		}
		Drawable finalDrawable = isRectangle(d, iconChangeMode);
		Log.i(TAG,"finalDrawable = "+finalDrawable+" iconChangeMode = "+iconChangeMode);
		if (finalDrawable != null) {
			// Here maybe get something wrong
			return finalDrawable;
		}

		// try to get the shadow && baseIcon from com.aurora.publicicon.res
		Resources resources = null;
		if (prePackageContext != null) {
			resources = prePackageContext.getResources();
		}
		int baseIconId = 0;
		int shadowId = 0;
		switch (iconMode) {
		case OUTER_SHADOW:
			shadowId = resources.getIdentifier("shadow", "drawable",
					PUBLIC_ICON_RES);
			break;
		case INTER_SHADOW:
			shadowId = resources.getIdentifier("shadow_app", "drawable",
					PUBLIC_ICON_RES);
			break;
		case OUTER_NONE_SHADOW_WITH_BOTTOM:
			shadowId = resources.getIdentifier("shadow_not_self", "drawable",
					PUBLIC_ICON_RES);
			break;
		case INTER_NONE_SHADOW_WITH_BOTTOM:
			shadowId = resources.getIdentifier("shadow_not_self_inter",
					"drawable", PUBLIC_ICON_RES);
			break;
		case NONE_SHADOW:
			// Maybe one day will use this tag;
			break;
		default:
			break;
		}
		baseIconId = resources.getIdentifier("drawable_base", "drawable",
				PUBLIC_ICON_RES);
		Drawable shadow = null;
		Drawable baseIcon = null;
		if (shadowId != 0) {
			shadow = resources.getDrawable(shadowId);
		}
		if (baseIconId != 0) {
			baseIcon = resources.getDrawable(baseIconId);
		}

		int dWidth = d.getIntrinsicWidth();
		int dHeight = d.getIntrinsicHeight();
		int basewidth = baseIcon.getIntrinsicWidth();
		int baseheight = baseIcon.getIntrinsicHeight();

		// Make the application icon with Round angle , base and shadow
		Bitmap sourceIcon = ((BitmapDrawable) d).getBitmap();
		Log.i(TAG,"d = "+dWidth+"  "+dHeight+  "  ICON_REAL_SIZE" +ICON_REAL_SIZE);
		if (dWidth > ICON_REAL_SIZE) {
			final float ratiowidth = (float) sourceIcon.getWidth()
					/ ICON_REAL_SIZE;
			final float ratioheight = (float) sourceIcon.getHeight()
					/ ICON_REAL_SIZE;
			if (basewidth > 0 && baseheight > 0) {
				Matrix matrix = new Matrix();
				matrix.postScale(1 / ratiowidth, 1 / ratioheight);
				sourceIcon = Bitmap.createBitmap(sourceIcon, 0, 0,
						sourceIcon.getWidth(), sourceIcon.getHeight(), matrix,
						true); // get the new scaled Bitmap
				dWidth = sourceIcon.getWidth();
				dHeight = sourceIcon.getHeight();
			}
			sourceIcon = getRoundedBitmap(sourceIcon);
		}

		int left = Math.abs(dWidth - basewidth) / 2;
		int top = Math.abs(dHeight - baseheight) / 2;

		Bitmap sourcebitmap = Bitmap.createBitmap(basewidth, baseheight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(sourcebitmap);
		Paint mPaint = new Paint();
		canvas.drawBitmap(((BitmapDrawable) baseIcon).getBitmap(), 0, 0, mPaint);
		canvas.drawBitmap(sourceIcon, left, top, mPaint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		sourcebitmap = getShadowBitmap1(sourcebitmap, shadow);
		BitmapDrawable iconDrawable = new BitmapDrawable(resources, sourcebitmap);
		return iconDrawable;
	}
	
	public Bitmap getDefaultIcon(int iconMode){
		Drawable d = getDefaultIconDrawable(iconMode);
		return (((BitmapDrawable)d).getBitmap());
	}
	
	public Drawable getDefaultIconDrawable(int iconMode){
		Context context = mPrePackageContext;
		Resources resources = null;
		if(context!=null){
			resources= context.getResources();
		}
		int shadowId = 0;
		switch (iconMode) {
		case OUTER_SHADOW:
			shadowId = resources.getIdentifier("shadow", "drawable",
					PUBLIC_ICON_RES);
			break;
		case INTER_SHADOW:
			shadowId = resources.getIdentifier("shadow_app", "drawable",
					PUBLIC_ICON_RES);
			break;
		case OUTER_NONE_SHADOW_WITH_BOTTOM:
			shadowId = resources.getIdentifier("shadow_not_self", "drawable",
					PUBLIC_ICON_RES);
			break;
		case INTER_NONE_SHADOW_WITH_BOTTOM:
			shadowId = resources.getIdentifier("shadow_not_self_inter", "drawable",
					PUBLIC_ICON_RES);
			break;
		case NONE_SHADOW:
			// Maybe one day will use this tag;
			break;
		default:
			break;
		}
		Drawable shadow = null;
		if(shadowId!=0){
			 shadow = resources.getDrawable(shadowId);
		}
		int defaultIconId = resources.getIdentifier("default_app_icon", "mipmap",
				PUBLIC_ICON_RES);
		Drawable drawable = resources.getDrawable(defaultIconId);
		Bitmap finalIcon = drawable2bitmap(drawable);
		finalIcon = getRoundedBitmap(finalIcon);
		finalIcon = getShadowBitmap1(finalIcon, shadow);
		return new BitmapDrawable(resources , finalIcon);
	}
	
	private String getIconName(ServiceInfo info){
		Context context = mPrePackageContext;
		CharSequence key = info.packageName + "$" + info.name;
		String iconName = getIconName(key.toString());
		if(iconName == null){
			key = info.loadLabel(context
					.getPackageManager());
			if(key!=null){
				iconName = getIconName(key.toString());
			}
		}
		return iconName;
	}
	
	private String getIconName(ActivityInfo info){
		Context context = mPrePackageContext;
		CharSequence key = info.packageName + "$" + info.name;
		String iconName = getIconName(key.toString());
		if(iconName == null){
			key = info.loadLabel(context
					.getPackageManager());
			if(key!=null){
				iconName = getIconName(key.toString());
			}
		}
		return iconName;
	}
	
	private String getIconName(String key){
		String bitmapName = mlables_icons.get(key.trim());
		String iconName = null;
		if (bitmapName != null) {
			String[] s = bitmapName.split("\\.");
			if (s.length == 2) {
				iconName = s[0];
			}
		}
		return iconName;
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
	
	public boolean isUseSystemIcon(Context context, ActivityInfo info) {
		if (info == null)
			return false;
		return isUseSystemIcon(context , info.packageName , info.name);
	}

	public boolean isUseSystemIcon(Context context, String packageName,
			String className) {
		if (packageName == null || className == null)
			return false;
		CharSequence key = packageName + "$" + className;
		return getIconName(key.toString()) != null;
	}

	private void configClassArray(Context context, String arrayName) {
		if(mlables_icons!=null){
			mlables_icons.clear();
		}
		try {
			mPrePackageContext = context.createPackageContext(PUBLIC_ICON_RES,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Create Res Apk Failed:NameNotFoundException");
		} catch (Exception e) {
			Log.e(TAG, "Create Res Apk Failed");
		}
		Resources res = mPrePackageContext.getResources();
		final int icon_real_size_dimenId = res.getIdentifier("app_icon_real_size", "dimen", PUBLIC_ICON_RES);
		final int app_icon_radius_dimenId = res.getIdentifier("app_icon_radius", "dimen", PUBLIC_ICON_RES);
		ICON_REAL_SIZE = res.getDimensionPixelSize(icon_real_size_dimenId);
		APP_ICON_RADIUS = res.getDimensionPixelSize(app_icon_radius_dimenId);
		HashMap<String, String> packageClassMap = new HashMap<String, String>();
		final int resId = res.getIdentifier(arrayName, "array", PUBLIC_ICON_RES);
		if (resId == 0){
			return;
		}
		final String[] packageClasseIcons = mPrePackageContext.getResources()
				.getStringArray(resId);
		for (String packageClasseIcon : packageClasseIcons) {
			String[] packageClasses_Icon = packageClasseIcon.split("#");
			Log.i(TAG,packageClasseIcon+":  packageClasses_Icon.length = "+packageClasses_Icon.length);
			if (packageClasses_Icon.length == 2) {
				String[] packageClasses = packageClasses_Icon[0].split("\\|");
				for (String s : packageClasses) {
					//Log.i(TAG," s ="+s +" packageClasses_Icon[1] = "+packageClasses_Icon[1]);
					packageClassMap.put(s.trim(), packageClasses_Icon[1]);
					String[] packageClass = s.split("\\$");
					if (packageClass.length == 2) {
						packageClassMap.put(packageClass[0],
								packageClasses_Icon[1]);
					}
				}
			}
		}
		mlables_icons = packageClassMap;
		ActivityManager activityManager = (ActivityManager) mPrePackageContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mIconDpi = activityManager.getLauncherLargeIconDensity();
		//icon_dir_prefix = mPrePackageContext.getFilesDir().getAbsolutePath();
		inter_icon_dir = icon_dir_prefix + inter_icon_dir_suffix;
		outer_icon_dir = icon_dir_prefix + outer_icon_dir_suffix;
		inter_icon_sddir = icon_sddir_prefix +inter_icon_dir_suffix;
		outer_icon_sddir = icon_sddir_prefix +outer_icon_dir_suffix;
	}
	
	private ResolveInfo getResolveInfo(String pkg,String cls){
		Context context = mPrePackageContext;
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent();
		intent.setClassName(pkg, cls);
		List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
		ResolveInfo info = null;
		if(!infos.isEmpty()){
			info = infos.get(0);
		}
		return info;
	}
	
	private Bitmap getIconFromCacheDir( ActivityInfo info,
			int iconMode) {
		Bitmap bitmap = getIconFromLocalSdcardDir(info.packageName, info.name, iconMode);
		if(bitmap==null){
			bitmap = getIconFromLocalDir(info.packageName, info.name, iconMode);
		}
		return bitmap;
	}

	private Bitmap getIconFromCacheDir( ServiceInfo info,
			int iconMode) {
		Bitmap bitmap = getIconFromLocalSdcardDir(info.packageName, info.name, iconMode);
		if(bitmap==null){
			bitmap = getIconFromLocalDir(info.packageName, info.name, iconMode);
		}
		return bitmap;
		//return getIconFromLocalDir(info.packageName, info.name, iconMode);
	}

	private Bitmap getIconFromCacheDir( String pkg, String cls,
			int iconMode) {
		Bitmap bitmap = getIconFromLocalSdcardDir(pkg, cls, iconMode);
		if(bitmap==null){
			bitmap = getIconFromLocalDir(pkg, cls, iconMode);
		}
		return bitmap;
		//return getIconFromLocalDir(pkg, cls, iconMode);
	}
	
	public Bitmap getIconFromLocalDir(String pkg,String cls,int iconMode){
		Bitmap bitmap =null;
		try {
			String iconDir = null;
			if(iconMode == INTER_SHADOW || iconMode == INTER_NONE_SHADOW_WITH_BOTTOM){
				iconDir = inter_icon_dir;
			}else{
				iconDir = outer_icon_dir;
			}
			String fileName=null;
			if(pkg!=null){
				if(cls!=null){
					fileName = iconDir+pkg+"_"+cls+".png";
				}else{
					fileName = iconDir+pkg+".png";
				}
			}
			File f = new File(fileName);
			if(!f.exists()){
				return bitmap;
			}
			BitmapFactory.Options  opt =new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,opt);
			final int REQUIRED_SIZE=280;
	        int scale=1;
	        while(opt.outWidth/scale/2>=REQUIRED_SIZE && opt.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;
	        BitmapFactory.Options opt2 = new BitmapFactory.Options();
	        opt2.inSampleSize=scale;
	        bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, opt2);
		} catch (FileNotFoundException e) {
			Log.i(TAG,"FileNotFoundException :  fileName is not exists");
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}
	
	public Bitmap getIconFromLocalSdcardDir(String pkg,String cls,int iconMode){
		Bitmap bitmap =null;
		try {
			String iconDir = null;
			if(iconMode == INTER_SHADOW || iconMode == INTER_NONE_SHADOW_WITH_BOTTOM){
				iconDir = inter_icon_sddir;
			}else{
				iconDir = outer_icon_sddir;
			}
			String fileName=null;
			if(pkg!=null){
				if(cls!=null){
					fileName = iconDir+pkg+"_"+cls+".png";
				}else{
					fileName = iconDir+pkg+".png";
				}
			}
			File f = new File(fileName);
			if(!f.exists()){
				return bitmap;
			}
			BitmapFactory.Options  opt =new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,opt);
			final int REQUIRED_SIZE=280;
	        int scale=1;
	        while(opt.outWidth/scale/2>=REQUIRED_SIZE && opt.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;
	        BitmapFactory.Options opt2 = new BitmapFactory.Options();
	        opt2.inSampleSize=scale;
	        bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, opt2);
		} catch (FileNotFoundException e) {
			Log.i(TAG,"FileNotFoundException :  fileName is not exists");
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}
	
	private Drawable getIconFromCacheDirDrawable(ActivityInfo info,
			int iconMode) {
		return getIconFromLocalDirDrawable(info.packageName, info.name, iconMode);
	}
	
	private Drawable getIconFromCacheDirDrawable(ServiceInfo info,
			int iconMode) {
		return getIconFromLocalDirDrawable(info.packageName, info.name, iconMode);
		
	}
	
	public Drawable getIconFromLocalDirDrawable(String pkg,String cls,int iconMode){
		Context context = mPrePackageContext;
		Bitmap icon = getIconFromLocalDir(pkg, cls, iconMode);
		if(icon!=null)return new BitmapDrawable(context.getResources(),icon);
		return null;
	}
	
	class SaveIconRunnable implements Runnable{
		Bitmap mSavingBitmap = null;
		String  pkg =null;
		String cls = null;
		int iconMode = -1;
		public SaveIconRunnable(Bitmap bitmap,String pkg,String cls,int iconMode){
			mSavingBitmap = bitmap;
			this.pkg = pkg;
			this.cls = cls;
			this.iconMode = iconMode;
		}

		@Override
		public void run() {
			try {
				String iconDir = null;
				String bitmapName = null;
				if(iconMode == INTER_SHADOW || iconMode == INTER_NONE_SHADOW_WITH_BOTTOM){
					iconDir = inter_icon_dir;
				}else{
					iconDir = outer_icon_dir;
				}
				if(pkg!=null){
					if(cls!=null){
						bitmapName = pkg+"_"+cls+".png";
					}else{
						bitmapName = pkg+".png";
					}
					
				}
				saveMyBitmap(iconDir,bitmapName,mSavingBitmap);
			} catch (IOException e) {
				Log.i(TAG,"run xxx",e);
				e.printStackTrace();
			}
			
		}
	}
	
	private void saveMyBitmap(String iconDir,String bitName, Bitmap bitmap) throws IOException {
		File f1 = new File(iconDir);
		if (!f1.exists() && !f1.isDirectory()) {
			f1.mkdirs();
		}
		File f = new File(iconDir + bitName);
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			Log.i(TAG,"1:e = "+e);
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			Log.i(TAG,"2:e = "+e);
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			Log.i(TAG,"3:e = "+e);
			e.printStackTrace();
		}
	}
	
	public static boolean deleteDir(File dir) {
		if(dir==null||(dir!=null&&!dir.exists()))return false;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //閫掑綊鍒犻櫎鐩綍涓殑瀛愮洰褰曚笅
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        boolean returnB = dir.delete();
        return returnB;
    }
	
	public boolean isLauncherResVersionChanged(Context context ,String path ,String key,String oldVersion){
		String currentVersion = getVersion(context , mLauncherResPackageName);
		Log.i(TAG,"currentVersion = "+currentVersion);
		if(!oldVersion.equals(currentVersion)){
			saveLauncherResCurVersion(context,path , key,currentVersion);
			return true;
		}
		return false;
	}
	
	/**
	 * 鑾峰彇鐗堟湰鍙�	 * @return 褰撳墠搴旂敤鐨勭増鏈彿
	 */
	private String mLauncherResPackageName="com.aurora.publicicon.res";
	private String getVersion(Context context,String packageName) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(packageName, 0);
	        String version = info.versionName;
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private void saveLauncherResCurVersion(Context context,String path,String key ,String version){
		setIconOldVersion(path,key,version);
	}
	
	private String getIconOldVersion(String path,String key,String defaultVal){
		String returnVal = "";
	    String res ="";
		File file = new File(path);
		if(file.exists()){
			BufferedReader bread = null;
			try {
				bread = new BufferedReader(new FileReader(file));
				while( (res = bread.readLine()) != null){
					if(res.contains(key)){
						returnVal = res.split(":")[1].toString();
						break;
					}else{
						returnVal = defaultVal;
					}
				}
				bread.close(); 
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					if(bread != null){
						bread.close();
					}
				} catch (Exception e2) {
				}
			}
		}
		return returnVal;
	}
	
	private void setIconOldVersion(String path,String key,String value){
		BufferedWriter bwrite = null;
		try {	
			File file = new File(path);
			if(!file.exists()){
				file.createNewFile();
			}
			bwrite = new BufferedWriter(new FileWriter(file));
			if(key != null&&!"".equals(key.trim())){
				bwrite.write(key + ":"+ value+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(bwrite != null){
					bwrite.flush();
					bwrite.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
