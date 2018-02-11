package com.aurora.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.aurora.utils.Utils2Icon;

public class IconCache {
	@SuppressWarnings("unused")
	private static final String TAG = "Launcher.IconCache";

	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

	private static class CacheEntry {
		public Bitmap icon;
		public String title;
	}

	private final Bitmap mDefaultIcon;
	private final LauncherApplication mContext;
	private final PackageManager mPackageManager;
	private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<ComponentName, CacheEntry>(
			INITIAL_ICON_CACHE_CAPACITY);
	private int mIconDpi;
	private String mIconDir = null;
	private ExecutorService mThreadPool;
	public static final String LAUNCER_RES_VERSION_FILE = "launcher_res_version";
	public static final String VERSION = "res_version";
	private final static boolean USE_DEFAULT_ICON_IN_PACKAGE = false;
	private Utils2Icon mUtils2Icon = null;

	public IconCache(LauncherApplication context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		mContext = context;
		
		mThreadPool = Executors.newCachedThreadPool();
		mIconDir = mContext.getFilesDir().getAbsolutePath()+"/Icons/";
		mPackageManager = context.getPackageManager();
		mIconDpi = activityManager.getLauncherLargeIconDensity();

		// need to set mIconDpi before getting default icon
		mUtils2Icon = Utils2Icon.getInstance(context);
		mDefaultIcon = makeDefaultIcon();
		//TODO:Icon
		/*SharedPreferences sp = mContext.getSharedPreferences(LAUNCER_RES_VERSION_FILE,Context.MODE_PRIVATE);
		String launcherResOldVersion=sp.getString(VERSION, "1.000");
		if(isLauncherResVersionChanged(launcherResOldVersion)){
			Log.i("xiejun","Launcher Res Version = "+launcherResOldVersion);
			deleteDir(new File(mIconDir));	
		}*/
	}

	public Drawable getFullResDefaultActivityIcon() {
		/*
		 * return getFullResIcon(Resources.getSystem(),
		 * android.R.mipmap.sym_def_app_icon);
		 */
		//TODO:Icon
		/*Drawable dw = getDefaultAppIcon();
		if (dw != null)
			return dw;
		return getFullResIcon(Resources.getSystem(),
				android.R.mipmap.sym_def_app_icon);*/
		return mUtils2Icon.getDefaultIconDrawable(Utils2Icon.OUTER_SHADOW);
	}

	public Drawable getFullResIcon(Resources resources, int iconId) {
		Drawable d;
		try {
			d = resources.getDrawableForDensity(iconId, mIconDpi);
		} catch (Resources.NotFoundException e) {
			d = null;
		}
		return (d != null) ? d : getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(Resources resources, int iconId,
			boolean isSystemIcon) {
		Drawable d;
		try {
			d = resources.getDrawableForDensity(iconId, mIconDpi);
		} catch (Resources.NotFoundException e) {
			d = null;
		}
		if (d != null && !isSystemIcon)
			d = mUtils2Icon.getCustomerIcon(d, Utils2Icon.OUTER_SHADOW);
		
		return (d != null) ? d : getFullResDefaultActivityIcon();
	}

	// just for widget's pre image or shorcut's icon
	public Drawable getFullResIcon(String packageName, int iconId) {
		/*
		 * Resources resources; try { resources =
		 * mPackageManager.getResourcesForApplication(packageName); } catch
		 * (PackageManager.NameNotFoundException e) { resources = null; } if
		 * (resources != null) { if (iconId != 0) { return
		 * getFullResIcon(resources, iconId ); } }
		 */
		return getFullResDefaultActivityIcon();
	}

	public Drawable getFullResIcon(ResolveInfo info) {
		return getFullResIcon(info.activityInfo);
	}

	public Drawable getFullResIcon(ActivityInfo info) {
		Drawable drawable =mUtils2Icon.getIconDrawable(info,Utils2Icon.OUTER_SHADOW);
		return drawable;
		//TODO:Icon
		/*if (drawable != null)return drawable;
		Resources resources;
		try {
			resources = mPackageManager
					.getResourcesForApplication(info.applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			int iconId = info.getIconResource();
			if (iconId != 0) {
				return getFullResIcon(resources, iconId, false);
			}
		}
		return getFullResDefaultActivityIcon();*/
	}

	private Bitmap makeDefaultIcon() {
		Drawable d = getFullResDefaultActivityIcon();
		Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
				Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		d.setBounds(0, 0, b.getWidth(), b.getHeight());
		d.draw(c);
		c.setBitmap(null);
		return b;
	}

	/**
	 * Remove any records for the supplied ComponentName.
	 */
	public void remove(ComponentName componentName) {
		synchronized (mCache) {
			mCache.remove(componentName);
		}
	}

	/**
	 * Empty out the cache.
	 */
	public void flush() {
		synchronized (mCache) {
			mCache.clear();
		}
	}

	/**
	 * Fill in "application" with the icon and label for "info."
	 */
	public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info,
			HashMap<Object, CharSequence> labelCache) {
		synchronized (mCache) {
			CacheEntry entry = cacheLocked(application.componentName, info,
					labelCache);

			application.title = entry.title;
			application.iconBitmap = entry.icon;
		}
	}

	public Bitmap getIcon(Intent intent) {
		synchronized (mCache) {
			
			ResolveInfo resolveInfo = null;
			try {
				resolveInfo = mPackageManager.resolveActivity(
						intent, 0);
			}
			catch (Exception e) {
				Log.d("vulcan-db","getIcon: after resolveActivity = " + resolveInfo);
			}

			ComponentName component = intent.getComponent();

			if (resolveInfo == null || component == null) {
				return mDefaultIcon;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo, null);
			return entry.icon;
		}
	}

	//ht 2015-01-20 将天气的Icon做临时缓存
	public void updateIcon(Intent intent, Bitmap icon){
		synchronized (mCache) {
			ResolveInfo resolveInfo = null;
			try {
				resolveInfo = mPackageManager.resolveActivity(intent, 0);
			}catch (Exception e) {
				Log.d("iht-w","updateIcon: after resolveActivity = " + resolveInfo);
			}

			ComponentName component = intent.getComponent();
			if (resolveInfo == null || component == null) {
				return;
			}
			CacheEntry entry = cacheLocked(component, resolveInfo, null);
			if(icon != null){
				entry.icon = icon;
			}
		}
	}
	
	public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo,
			HashMap<Object, CharSequence> labelCache) {
		synchronized (mCache) {
			if (resolveInfo == null || component == null) {
				return null;
			}

			CacheEntry entry = cacheLocked(component, resolveInfo, labelCache);
			return entry.icon;
		}
	}

	public boolean isDefaultIcon(Bitmap icon) {
		return mDefaultIcon == icon;
	}

	private CacheEntry cacheLocked(ComponentName componentName,
			ResolveInfo info, HashMap<Object, CharSequence> labelCache) {
		Context context = mContext;
		CacheEntry entry = mCache.get(componentName);
		if (entry == null) {
			entry = new CacheEntry();

			mCache.put(componentName, entry);

			ComponentName key = getComponentNameFromResolveInfo(info);
			if (labelCache != null && labelCache.containsKey(key)) {
				entry.title = labelCache.get(key).toString();
			} else {
				entry.title = info.loadLabel(mPackageManager).toString();
				if (labelCache != null) {
					labelCache.put(key, entry.title);
				}
			}
			if (entry.title == null) {
				entry.title = info.activityInfo.name;
			}

			/*
			 * entry.icon = Utilities.createIconBitmap( getFullResIcon(info),
			 * mContext);
			 */
			//entry.icon = getIconFromLocalDir(componentName);
			if(entry.icon == null){
				/*final Bitmap bitmap = Utils.getIcon(context, info,Utils.OUTER_SHADOW);;
				if (bitmap != null) {
					entry.icon = Utilities.createIconBitmap(bitmap, context);
				} else {
					// 加底托
					entry.icon = Utilities.createIconBitmap(
							getFullResIcon(info), mContext);
				}*/
				entry.icon = Utilities.createIconBitmap(
						getFullResIcon(info), context);
				//mThreadPool.execute(new SaveIconRunnable(entry.icon,componentName));
			}
		}
		return entry;
	}

	public HashMap<ComponentName, Bitmap> getAllIcons() {
		synchronized (mCache) {
			HashMap<ComponentName, Bitmap> set = new HashMap<ComponentName, Bitmap>();
			for (ComponentName cn : mCache.keySet()) {
				final CacheEntry e = mCache.get(cn);
				set.put(cn, e.icon);
			}
			return set;
		}
	}

	static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
		if (info.activityInfo != null) {
			return new ComponentName(info.activityInfo.packageName,
					info.activityInfo.name);
		} else {
			return new ComponentName(info.serviceInfo.packageName,
					info.serviceInfo.name);
		}
	}

	/*public Drawable getCustomizedIcon(Drawable sourceicon) {
		Context context = mContext;
		Resources res = context.getResources();
		Drawable drawablebase = res.getDrawable(R.drawable.drawable_base);
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		Bitmap baseBitmap = ((BitmapDrawable) drawablebase).getBitmap();
		Bitmap iconBitmap = null;
		int basewidth = drawablebase.getIntrinsicWidth();
		int baseheight = drawablebase.getIntrinsicHeight();
		int width = sourceicon.getIntrinsicWidth();
		int height = sourceicon.getIntrinsicHeight();
		if (sourceicon != null) {
			Drawable finalDrawable = Utils.isRectangle(sourceicon, context,Utils.OUTER_NONE_SHADOW_WITH_BOTTOM);
			if (finalDrawable!=null) {
				return finalDrawable;
			}
			iconBitmap = ((BitmapDrawable) sourceicon).getBitmap();
		} else {
			sourceicon = getFullResDefaultActivityIcon();
			iconBitmap = ((BitmapDrawable) sourceicon).getBitmap();
		}
		Bitmap newBitmap = null;
		int icon_radius = res.getDimensionPixelSize(R.dimen.app_icon_radius);
		int icon_realSize = res.getDimensionPixelSize(R.dimen.app_icon_real_size);
		if (width > icon_realSize) {
			iconBitmap = Utils.getRoundedBitmap(iconBitmap,context);
			final float ratiowidth = (float) iconBitmap.getWidth() / icon_realSize;
			final float ratioheight = (float) iconBitmap.getHeight() / icon_realSize;
			if (basewidth > 0 && baseheight > 0) {
				Matrix matrix = new Matrix();
				matrix.postScale(1 / ratiowidth, 1 / ratioheight); 
				newBitmap = Bitmap.createBitmap(iconBitmap, 0, 0,
						iconBitmap.getWidth(), iconBitmap.getHeight(), matrix,
						true); // get the new scaled Bitmap
				width = newBitmap.getWidth();
				height = newBitmap.getHeight();
			}
		} else {
			newBitmap = iconBitmap;
			width = newBitmap.getWidth();
			height = newBitmap.getHeight();
		}
		int left = Math.abs(width - basewidth) / 2;
		int top = Math.abs(height - baseheight) / 2;

		Bitmap sourcebitmap = Bitmap.createBitmap(basewidth, baseheight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(sourcebitmap);
		Paint mPaint = new Paint();
		canvas.drawBitmap(baseBitmap, 0, 0, mPaint);
		canvas.drawBitmap(newBitmap, left, top, mPaint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		sourcebitmap = Utils.getShadowBitmap1(sourcebitmap, shadowDrawable);
		BitmapDrawable iconDrawable = new BitmapDrawable(res, sourcebitmap);
		return iconDrawable;
	}*/

	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		// setup new bitmap
		Bitmap bgBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		// set this bitmap as canvas
		Canvas mCanvas = new Canvas(bgBitmap);

		Paint mPaint = new Paint();
		int offsetX = bitmap.getWidth() / 20;
		int offsetY = bitmap.getHeight() / 20;
		Rect mRect = new Rect(offsetX, offsetY, bitmap.getWidth() - offsetX,
				bitmap.getHeight() - offsetY);
		RectF mRectF = new RectF(mRect);
		// set the radius as 10
		float roundPx = 10;
		mPaint.setAntiAlias(true);
		// draw the rect with round corner firstly
		mCanvas.drawRoundRect(mRectF, roundPx, roundPx, mPaint);

		// set the mode for superpostion
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		// draw the final bitmap
		mCanvas.drawBitmap(bitmap, mRect, mRect, mPaint);

		return bgBitmap;
	}
	
	private void saveMyBitmap(String bitName, Bitmap bitmap) throws IOException {
		File f1 = new File(mIconDir);
		if (!f1.exists() && !f1.isDirectory()) {
			f1.mkdir();
		}
		File f = new File(mIconDir + bitName + ".png");
		
		f.createNewFile();
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//bitmap = null;
	}
	
	public Bitmap getIconFromLocalDir(ComponentName componentName){
		Bitmap bitmap =null;
		//FileInputStream fis = null;
		try {
			Log.i("xiejun","getIconFromLocalDir:componentName.getClassName() = "+componentName.getClassName());
			String fileName = mIconDir+componentName.getPackageName()+"_"+componentName.getClassName()+".png";
			File f = new File(fileName);
			if(!f.exists())f = new File(mIconDir+componentName.getPackageName()+".png");
			//fis = new FileInputStream(f);
			//bitmap  = BitmapFactory.decodeStream(fis);
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
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}
	
	class SaveIconRunnable implements Runnable{
		Bitmap mSavingBitmap = null;
		ComponentName mComponentName =null;
		public SaveIconRunnable(Bitmap bitmap,ComponentName componentName){
			mSavingBitmap = bitmap;
			mComponentName = componentName;
		}

		@Override
		public void run() {
			String clName = mComponentName.getClassName();
			String pkName = mComponentName.getPackageName();
			try {
				saveMyBitmap(clName==null?pkName:(pkName+"_"+clName),mSavingBitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}	
	}
		
	public static boolean deleteDir(File dir) {
		if(dir==null||(dir!=null&&!dir.exists()))return false;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        boolean returnB = dir.delete();
        return returnB;
    }
	
	public boolean isLauncherResVersionChanged(String oldVersion){
		String currentVersion = getVersion(mLauncherResPackageName);
		if(!oldVersion.equals(currentVersion)){
			saveLauncherResCurVersion(currentVersion);
			return true;
		}
		return false;
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	private String mLauncherResPackageName="com.aurora.publicicon.res";
	public String getVersion(String packageName) {
	    try {
	        PackageManager manager = mContext.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(packageName, 0);
	        String version = info.versionName;
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private void saveLauncherResCurVersion(String version){
		SharedPreferences sp = mContext.getSharedPreferences(LAUNCER_RES_VERSION_FILE, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(VERSION,version).commit();
	}
	
}