package com.aurora.launcher;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
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
import com.aurora.utils.*;
public class Utils{
	private static final String TAG = "PUBLIC_ICON";
	private static final String PUBLIC_ICON_RES = "com.aurora.publicicon.res";
	private static final String PACKAGE_CLASS = "lable_map";
	private static int ICON_REAL_SIZE = 178;
	private static int APP_ICON_RADIUS = 12;
	private static final boolean USE_RIGHT_ANGLE = true;
	private static final boolean USE_ICON_SHADOW = true;
	private static boolean isInitedConfig = false;
	/* Shadow type */
	public static final int OUTER_SHADOW = 0;
	public static final int INTER_SHADOW = 1;
	public static final int OUTER_NONE_SHADOW_WITH_BOTTOM = 2;
	public static final int INTER_NONE_SHADOW_WITH_BOTTOM = 3;
	public static final int NONE_SHADOW = 4;
	private static int mIconDpi;
	private static Object configLock = new Object();
	private ExecutorService mThreadPool;
	private String inter_icon_dir;
	private String outer_icon_dir;

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
	
	public static Bitmap getRoundedBitmap(Bitmap bm ,Context context){
		synchronized (configLock) {
			configClassArray(context, PACKAGE_CLASS);
		}
		Log.i("linp", "Moving icons will use systemProvide getRoundBitmap function getRoundedBitmap");
		return Utils2Icon.getInstance(context).getRoundedBitmap(bm);
	}

	public static Bitmap getRoundedBitmap(Bitmap bm) {
		return getRoundedBitmap(bm,-1,false);
	}
	
	public static Bitmap  getRoundedBitmap(Bitmap bm,int customerSize){
		return getRoundedBitmap(bm,customerSize,false);
	}
	
	public static Bitmap getRoundedBitmap(Bitmap bm,int customerSize,boolean needScale) {
		Log.e("linp", "########Utils getRoundedBitmap");
		// 创建新的位图
		int width = customerSize;
		//if(width < 0 || customerSize>bm.getWidth()){
			width = ICON_REAL_SIZE;
		//}
		Bitmap bg = Bitmap.createBitmap(width, width,
				Config.ARGB_8888);
		// 创建位图画板
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
		// 设置圆角半径
		float roundR = APP_ICON_RADIUS;
		paint.setAntiAlias(true);
		canvas.save();
		canvas.translate(-(float) extraW, -(float) extraH);
		// 绘制圆形矩形
		canvas.drawRoundRect(rectF, roundR, roundR, paint);
		// 设置图形叠加模式
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		// 绘制图形
		canvas.restore();
		canvas.save();
		canvas.drawBitmap(bm, rect, rect2, paint);
		canvas.restore();
		// 释放资源
		canvas.setBitmap(null);
		
//		int width = bm.getWidth();  
//        int height = bm.getHeight();  
//        float roundPx;  
//        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;  
//        if (width <= height) {  
//            roundPx = width / 2;  
//            left = 0;  
//            top = 0;  
//            right = width;  
//            bottom = width;  
//            height = width;  
//            dst_left = 0;  
//            dst_top = 0;  
//            dst_right = width;  
//            dst_bottom = width;  
//        } else {  
//            roundPx = height / 2;  
//            float clip = (width - height) / 2;  
//            left = clip;  
//            right = width - clip;  
//            top = 0;  
//            bottom = height;  
//            width = height;  
//            dst_left = 0;  
//            dst_top = 0;  
//            dst_right = height;  
//            dst_bottom = height;  
//        }  
//  
//        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);  
//        Canvas canvas = new Canvas(output);  
//  
//        final int color = 0xff424242;  
//        final Paint paint = new Paint();  
//        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);  
//        final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);  
//        final RectF rectF = new RectF(dst);  
//  
//        paint.setAntiAlias(true);// 设置画笔无锯齿  
//  
//        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas  
//        paint.setColor(color);  
//  
//        // 以下有两种方法画圆,drawRounRect和drawCircle  
//        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。  
//        canvas.drawCircle(roundPx, roundPx, roundPx, paint);  
//  
//        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452  
//        canvas.drawBitmap(bm, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle  
		
		
		
		return bg;
	}
	//1.draw shawdow 2.draw bitmap
	public static Bitmap getShadowBitmap1(Bitmap bm, Drawable shadow) {
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
		canvas.restore();
		// 绘制背景
		shadow.setBounds(0, 0, bg.getWidth(), bg.getHeight());
		shadow.draw(canvas);
		canvas.setBitmap(null);
		return bg;
	}
	
	//1.draw bitmap 2.draw shawdow
	public static Bitmap getShadowBitmap3(Bitmap bm, Drawable shadow) {
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
	
	private static Drawable getSystemIconDrawable(Context context,
			String iconName, final int  isUseOuterShadow ) {
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
		drawable = getSystemIconDrawable(prePackageContext,drawable,isUseOuterShadow);
		return drawable;
	}
	
	//private  static  Drawable getSystemIconDrawable(Context context,Drawable d,final int iconMode){
	//iht2014-11-11 fix BUG#9712;change the private method to public;
	public  static  Drawable getSystemIconDrawable(Context context,Drawable d,final int iconMode){
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
	 * 判断图图标形状
	 */
	public static Drawable isRectangle(Drawable d,Context context,int iconMode) {
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
				finalDrawable = getSystemIconDrawable(context, finalDrawable ,iconMode);
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
	
	
	/*
	 * 判断App Icon是否需要加底托
	 */

	public static HashMap<String, String> mlables_icons = null;
	public static Bitmap getIcon(Context context,ResolveInfo info,int iconMode){
		Bitmap icon = null;
		if(info.activityInfo!=null){
			icon = getIcon(context,info.activityInfo,iconMode);
		}else{
			icon = getIcon(context,info.serviceInfo,iconMode);
		}
		return icon;
	}
	
	public static Bitmap getIcon(Context context,ActivityInfo info,int iconMode){
		String iconName = getIconName(context,info);
		Bitmap icon = null;
		if(iconName!=null){
			icon= getBitmapByName(context,iconName,iconMode);
		}
		
		if(icon == null ){
			icon = getCustomerIcon(context,info,iconMode);
		}
		return icon;
	}
	
	public static Bitmap getIcon(Context context,ServiceInfo info,int iconMode){
		String iconName = getIconName(context,info);
		Bitmap icon = null;
		if(iconName!=null){
			icon= getBitmapByName(context,iconName,iconMode);
		}
		if(icon == null ){
			icon = getCustomerIcon(context,info,iconMode);
		}
		return icon;
	}

	public static Bitmap getIcon(Context context,String pkg,String cls ,int iconMode){
		CharSequence key = pkg+ "$" + cls;
		String iconName = getIconName(context,key.toString());
		Bitmap icon = null;
		if(iconName!=null){
			icon= getBitmapByName(context,iconName,iconMode);
		}
		if(icon == null ){
			icon = getCustomerIcon(context,pkg,cls,iconMode);
		}
		return icon;
	}
	
	public static Bitmap getIcon(Context context,String pkg,int iconMode){
		String iconName = getIconName(context,pkg);
		Bitmap icon = null;
		if(iconName == null ){
			ResolveInfo rInfo=getPackageFirstResolveInfo(context,pkg);
			if(rInfo!=null)icon = getIcon(context,rInfo,iconMode);
		}else {
			icon = getBitmapByName(context,iconName,iconMode);
		}
		if(icon == null){
			icon = getCustomIcon(context,pkg,iconMode);
		}
		return icon;
	}

	public static Drawable getIconDrawble(Context context,ResolveInfo info,int iconMode){
		Drawable icon = null;
		if(info.activityInfo!=null){
			icon = getIconDrawble(context,info.activityInfo,iconMode);
		}else{
			icon = getIconDrawble(context,info.serviceInfo,iconMode);
		}
		
		return icon;
	}
	
	public static Drawable getIconDrawble(Context context,ActivityInfo info,int iconMode){
		String iconName = getIconName(context,info);
		Drawable icon = null;
		if(iconName!=null){
			icon= getSystemIconDrawable(context,iconName,iconMode);
		}
		if(icon == null ){
			icon = getCustomerIconDrawable(context,info,iconMode);
		}
		return icon;
	}
	
	public static Drawable getIconDrawble(Context context,ServiceInfo info,int iconMode){
		String iconName = getIconName(context,info);
		Drawable icon = null;
		if(iconName!=null){
			icon= getSystemIconDrawable(context,iconName,iconMode);
		}
		if(icon == null ){
			icon = getCustomerIconDrawable(context,info,iconMode);
		}
		return icon;
	}

	public static Drawable getIconDrawble(Context context,String pkg,String cls ,int iconMode){
			CharSequence key = pkg + "$" + cls;
			String iconName = getIconName(context,key.toString());
			Drawable icon = null;
			if(iconName!=null){
				icon= getSystemIconDrawable(context,iconName,iconMode);
			}
			if(icon == null ){
				icon = getCustomerIconDrawable(context, pkg,cls,iconMode);
			}
			return icon;
		}
	
	public static Drawable getIconDrawble(Context context,String pkg,int iconMode){
		String iconName = getIconName(context,pkg);
		Drawable icon = null;
		if(iconName == null ){
			//here is not a perfect logic
			ResolveInfo rInfo=getPackageFirstResolveInfo(context,pkg);
			if(rInfo!=null)icon = getIconDrawble(context,rInfo,iconMode);
		}else {
			icon = getSystemIconDrawable(context,iconName,iconMode);
		}
		return icon;
	}
	
	private static Bitmap getBitmapByName(Context context, String drawableName,int iconMode) {
		Bitmap bitmap = null;
		Drawable drawable = getSystemIconDrawable(context, drawableName,iconMode);
		if (drawable != null) {
			bitmap = drawable2bitmap(drawable);
		}
		return bitmap;
	}
	
	private static Bitmap getCustomerIcon(Context context,String pkg,String cls,int iconMode){
		Drawable d = getCustomerIconDrawable(context, pkg,cls, iconMode);
		return ((BitmapDrawable)d).getBitmap();
	}
	
	private static  Drawable getCustomerIconDrawable(Context context,String pkg,String cls,int iconMode){
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent();
		intent.setClassName(pkg, cls);
		List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
		ResolveInfo info = null;
		if(!infos.isEmpty()){
			info = infos.get(0);
		}
		
		return getCustomerIconDrawable(context,info,iconMode);
	}
	
	private static  Bitmap getCustomerIcon(Context context, ServiceInfo info, int iconMode){
		Drawable d = getCustomerIconDrawable(context, info, iconMode);
		return ((BitmapDrawable)d).getBitmap();
	}
	
	private static  Bitmap getCustomerIcon(Context context, ActivityInfo info, int iconMode){
		Drawable d = getCustomerIconDrawable(context, info, iconMode);
		return ((BitmapDrawable)d).getBitmap();
	}
	
	private static Bitmap getCustomIcon(Context context,String pkg,int iconMode){
		Drawable d = getCustomIconDrawable(context , pkg ,iconMode);
		return ((BitmapDrawable)d).getBitmap();
	}
	
	private static Drawable getCustomIconDrawable(Context context,String pkg,int iconMode){
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
		return getCustomerIcon(context, d, iconMode);
	}
	
	private static  Drawable getCustomerIconDrawable(Context context, ResolveInfo info, int iconMode){
		Drawable d = null;
		if(info.activityInfo!=null ){
			d = getCustomerIconDrawable(context, info.activityInfo, iconMode);
		}else if(info.serviceInfo!=null){
			d = getCustomerIconDrawable(context, info.serviceInfo, iconMode);
		}
		return d;
	}
	
	private static  Drawable getCustomerIconDrawable(Context context, ServiceInfo info,int iconMode){
		PackageManager pm = context.getPackageManager();
		Resources res;
		try {
			res = pm.getResourcesForApplication(info.applicationInfo);
		} catch (NameNotFoundException e) {
			res = null;
			Log.i(TAG,"exception has catched!",e);
		}
		
		//try to get the icon from application
		Drawable d = null;
		if(res!=null){
			int iconId = info.getIconResource();
			if(iconId != 0){
				try {
					d = res.getDrawableForDensity(iconId, mIconDpi);
				} catch (Resources.NotFoundException e) {
					d = null;
				}
			}
		}
		return getCustomerIcon(context, d, iconMode);
	}
	
	private static  Drawable getCustomerIconDrawable(Context context, ActivityInfo info,int iconMode){
		PackageManager pm = context.getPackageManager();
		Resources res;
		try {
			res = pm.getResourcesForApplication(info.applicationInfo);
		} catch (NameNotFoundException e) {
			res = null;
			Log.i(TAG,"exception has catched!",e);
		}
		//try to get the icon from application
		Drawable d = null;
		if(res!=null){
			int iconId = info.getIconResource();
			if(iconId != 0){
				try {
					d = res.getDrawableForDensity(iconId, mIconDpi);
				} catch (Resources.NotFoundException e) {
					d = null;
				}
			}
		}
		return getCustomerIcon(context, d, iconMode);
	}
	
	private static Drawable getCustomerIcon(Context context, Drawable d, int iconMode) {
		Context prePackageContext = null;
		try {
			prePackageContext = context.createPackageContext(PUBLIC_ICON_RES,
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Create Res Apk Failed:NameNotFoundException");
		} catch (Exception e) {
			Log.e(TAG, "Create Res Apk Failed");
		}
		if (d == null) {
			// Get default icon because the application icon is null
			return getDefaultIconDrawable(prePackageContext, iconMode);

		} else {
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
			Drawable finalDrawable = isRectangle(d, context,
					iconChangeMode);
			if (finalDrawable != null) {
				// Here maybe get something wrong
				return finalDrawable;
			}
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
		if (dWidth > ICON_REAL_SIZE) {
			sourceIcon = getRoundedBitmap(sourceIcon, context);
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
	
	private static Drawable getDefaultIconDrawable(Context context,int iconMode){
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
		int defaultIconId = resources.getIdentifier("default_app_icon", "drawable",
				PUBLIC_ICON_RES);
		Drawable drawable = resources.getDrawable(defaultIconId);
		Bitmap finalIcon = drawable2bitmap(drawable);
		finalIcon = getRoundedBitmap(finalIcon,context);
		finalIcon = getShadowBitmap1(finalIcon, shadow);
		return new BitmapDrawable(resources , finalIcon);
	}
	
	private static String getIconName(Context context,ServiceInfo info){
		CharSequence key = info.packageName + "$" + info.name;
		String iconName = getIconName(context,key.toString());
		if(iconName == null){
			key = info.loadLabel(context
					.getPackageManager());
			if(key!=null){
				iconName = getIconName(context,key.toString());
			}
		}
		return iconName;
	}
	
	private static String getIconName(Context context,ActivityInfo info){
		CharSequence key = info.packageName + "$" + info.name;
		String iconName = getIconName(context,key.toString());
		if(iconName == null){
			key = info.loadLabel(context
					.getPackageManager());
			if(key!=null){
				iconName = getIconName(context,key.toString());
			}
		}
		return iconName;
	}
	
	private static String getIconName(Context context,String key){
		synchronized (configLock) {
				configClassArray(context, PACKAGE_CLASS);
		}
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
	
	public static boolean isUseSystemIcon(Context context, ActivityInfo info) {
		if (info == null)
			return false;
		return isUseSystemIcon(context , info.packageName , info.name);
	}

	public static boolean isUseSystemIcon(Context context, String packageName,
			String className) {
		if (packageName == null || className == null)
			return false;
		CharSequence key = packageName + "$" + className;
		return getIconName(context , key.toString()) != null;
	}
	private static HashMap<String, String> configClassArray(Context context,
			String arrayName) {
		if (!isInitedConfig) {
			Context prePackageContext = null;
			try {
				prePackageContext = context.createPackageContext(
						PUBLIC_ICON_RES, Context.CONTEXT_IGNORE_SECURITY);
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Create Res Apk Failed:NameNotFoundException");
			} catch (Exception e) {
				Log.e(TAG, "Create Res Apk Failed");
			}
			Resources res = prePackageContext.getResources();
			final int icon_real_size_dimenId = res.getIdentifier("app_icon_real_size","dimen",PUBLIC_ICON_RES);
			final int app_icon_radius_dimenId = res.getIdentifier("app_icon_radius","dimen",PUBLIC_ICON_RES);
			ICON_REAL_SIZE = res.getDimensionPixelSize(icon_real_size_dimenId);
			APP_ICON_RADIUS = res.getDimensionPixelSize(app_icon_radius_dimenId);
			
			HashMap<String, String> packageClassMap = new HashMap<String, String>();
			if (prePackageContext != null) {
				final int resId = res.getIdentifier(arrayName, "array", PUBLIC_ICON_RES);
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
							packageClassMap.put(s.trim(),
									packageClasses_Icon[1]);
							String[] packageClass = s.split("\\$");
							if(packageClass.length == 2){
								packageClassMap.put(packageClass[0], packageClasses_Icon[1]);
							}
						}
					}
				}
			}
			isInitedConfig = true;
			mlables_icons = packageClassMap;
			if (prePackageContext != null) {
				ActivityManager activityManager = (ActivityManager) prePackageContext
						.getSystemService(Context.ACTIVITY_SERVICE);
				mIconDpi = activityManager.getLauncherLargeIconDensity();
			}
			
		}
		return mlables_icons;
	}
	
	//TODO:xiejun
	private static Bitmap getIconFromCacheDir(Context context,ResolveInfo info , int iconMode){
		return null;
	}
	
	private static Bitmap getIconFromCacheDir(Context context,ActivityInfo info , int iconMode){
		return null;
	}
	
	private static Bitmap getIconFromCacheDir(Context context,ServiceInfo info , int iconMode){
		return null;
	}
}
