package com.aurora.downloader.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.aurora.downloader.DownloadActivity;
import com.aurora.downloader.FileInfo;
import com.aurora.downloader.util.FileIconHelper.FileCategory;
import com.aurora.downloader.util.MediaFile.MediaFileType;

import android.provider.Downloads;

public class Util {

	private static final String TAG = "Util";

	private static final String ANDROID_SECURE = ".android_secure";

	private static long lastClickTime;

	/**
	 * 防止双击
	 * 
	 * @return
	 */
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/**
	 * 通过文件名获取文件后缀
	 * 
	 * @param filename
	 * @return
	 */
	public static String getExtFromFilename(String filename) {
		if (filename == null) {
			return "";
		}
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(dotPosition + 1, filename.length());
		}
		return "";
	}

	/**
	 * 通过文件名获取不带后缀文件名
	 * 
	 * @param filename
	 * @return
	 */
	public static String getNameFromFilename(String filename) {
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(0, dotPosition);
		}
		return "";
	}

	/**
	 * 通过文件路径 获取父路径
	 * 
	 * @param filepath
	 * @return
	 */
	public static String getPathFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(0, pos);
		}
		return "";
	}

	/**
	 * 通过文件路径获取文件名
	 * 
	 * @param filepath
	 * @return
	 */
	public static String getNameFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return "";
	}

	private static File lFile;
	private static FileInfo lFileInfo;

	/**
	 * 通过路径获取文件信息
	 * 
	 * @param filePath
	 * @return
	 */
	public static FileInfo GetFileInfo(String filePath) {
		if (filePath == null) {
			return null;
		}
		lFile = new File(filePath);
		lFileInfo = new FileInfo();
		lFileInfo.isExists = lFile.exists();
		lFileInfo.fileName = Util.getNameFromFilepath(filePath);
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.filePath = filePath;
		lFileInfo.fileSize = lFile.length();
		return lFileInfo;
	}

	/**
	 * 获取简单FileInfo
	 * 
	 * @param f
	 * @return
	 */
	public static FileInfo getSimpleFileInfo(File f) {
		lFileInfo = new FileInfo();
		String filePath = f.getPath();
		lFileInfo.filePath = filePath;
		lFileInfo.fileName = f.getName();
		lFileInfo.fileSize = f.length();
		lFileInfo.ModifiedDate = f.lastModified();
		return lFileInfo;
	}

	/**
	 * 格式化时间
	 * 
	 * @param context
	 * @param time
	 * @return
	 */
	public static String formatInfoDateString(Context context, long time) {
		Date date = new Date(time);
		SimpleDateFormat sdfDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdfDateFormat.format(date);
	}

	/**
	 * 格式化大小
	 * 
	 * @param size
	 * @return
	 */
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
		if (size >= gb) {
			return String.format("%.2f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.1f MB" : "%.2f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.1f KB" : "%.2f KB", f);
		} else
			return String.format("%d B", size);
	}

	/**
	 * 获取文件信息
	 * 
	 * @param cursor
	 * @return
	 */
	public static FileInfo getFileInfo(Cursor cursor) {
		if ((cursor == null || cursor.getCount() == 0)) {
			return null;
		}
		FileInfo fi = null;
		try {
			fi = Util
					.GetFileInfo(cursor.getString(cursor
							.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME)));
			if (fi != null) {
				fi.isCanShare = cursor.getInt(cursor
						.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL ? true
						: false;
			} else {
				fi = new FileInfo();
			}
			fi.downloadId = cursor.getLong(cursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
			if (TextUtils.isEmpty(cursor.getString(cursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)))) {
				fi.isDbExists = true;
			} else {
				fi.isDbExists = false;
			}
			fi.fileSize = cursor.getLong(cursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
			int downloadStatus = cursor.getInt(cursor
					.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
			int control = cursor.getInt(cursor
					.getColumnIndexOrThrow(Downloads.Impl.COLUMN_CONTROL));
			fi.status = DownloadStateUtil.getDownloadStates(downloadStatus,
					control);
		} catch (Exception e) {
			e.printStackTrace();
			AuroraLog.elog(TAG, e.getMessage());
		}
		return fi;
	}

	/**
	 * This method generates a new suffix if a name conflict occurs, ex: paste a
	 * file named "stars.txt", the target file name would be "stars(1).txt"
	 * 
	 * @param conflictFile
	 *            the conflict file
	 * @return a new name for the conflict file
	 */
	public static String autoGenerateName(File conflictFile) {
		int prevMax = 0;
		int newMax = 0;
		int leftBracketIndex = 0;
		int rightBracketIndex = 0;
		String tmp = null;
		String numeric = null;
		String fileName = null;
		File dir = null;
		File[] files = null;
		String parentDir = conflictFile.getParent();
		String conflictName = conflictFile.getName();

		if (parentDir != null) {
			dir = new File(parentDir);
			files = dir.listFiles();
		}

		if (conflictFile.isDirectory()) {
			if (conflictName.endsWith(")")) {
				leftBracketIndex = conflictName.lastIndexOf("(");
				if (leftBracketIndex != -1) {
					numeric = conflictName.substring(leftBracketIndex + 1,
							conflictName.length() - 1);
					if (numeric.matches("[0-9]+")) {
						newMax = findSuffixNumber(conflictName, prevMax);
						prevMax = newMax;
						conflictName = conflictName.substring(0,
								leftBracketIndex);
					}
				}
			}

			if (files != null) {
				for (File file : files) {
					fileName = file.getName();
					if (fileName.endsWith(")")) {
						leftBracketIndex = fileName.lastIndexOf("(");
						if (leftBracketIndex != -1) {
							tmp = fileName.substring(0, leftBracketIndex);
							if (tmp.equalsIgnoreCase(conflictName)) {
								numeric = fileName.substring(
										leftBracketIndex + 1,
										fileName.length() - 1);
								if (numeric.matches("[0-9]+")) {
									newMax = findSuffixNumber(fileName, prevMax);
									prevMax = newMax;
								}
							}
						}
					}
				}
			}
			return parentDir + "/" + conflictName + "("
					+ Integer.toString(newMax + 1) + ")";
		} else {
			// check if source file already contains "(x)", e.g.
			// /sdcard/stars(3).jpg
			String ext = "";
			int extIndex = conflictName.lastIndexOf(".");
			if (extIndex == -1) {
				extIndex = conflictName.length(); // this file has no extension
			} else {
				ext = conflictName.substring(extIndex);
			}

			String prefix = conflictName.substring(0, extIndex);
			if (prefix.endsWith(")")) {
				leftBracketIndex = prefix.lastIndexOf("(");
				if (leftBracketIndex != -1) {
					numeric = prefix.substring(leftBracketIndex + 1,
							prefix.length() - 1);
					if (numeric.matches("[0-9]+")) {
						newMax = findSuffixNumber(conflictName, prevMax);
						prevMax = newMax;
					}
				}
			}

			// reset conflictName to a new name without (x)
			int index = conflictName.lastIndexOf("(");
			if (index == -1) {
				conflictName = conflictName.substring(0, extIndex);
			} else {
				conflictName = conflictName.substring(0, index); // e.g.
																	// /sdcard/stars
			}

			if (files != null) {
				for (File file : files) {
					fileName = file.getName();
					if (fileName.endsWith(")" + ext)) {
						leftBracketIndex = fileName.lastIndexOf("(");
						rightBracketIndex = fileName.lastIndexOf(")");
						if (leftBracketIndex != -1) {
							tmp = fileName.substring(0, leftBracketIndex);
							if (tmp.equalsIgnoreCase(conflictName)) {
								numeric = fileName
										.substring(leftBracketIndex + 1,
												rightBracketIndex);
								if (numeric.matches("[0-9]+")) {
									newMax = findSuffixNumber(fileName, prevMax);
									prevMax = newMax;
								}
							}
						}
					}
				}
			}
			return parentDir + "/" + conflictName + "("
					+ Integer.toString(newMax + 1) + ")" + ext;
		}
	}

	/**
	 * This method finds the current max number of suffix for a conflict file
	 * ex: there are A(1).txt, A(2).txt, then the max number of suffix is 2
	 * 
	 * @param fileName
	 *            the conflict file
	 * @param maxVal
	 *            the old max number of suffix
	 * @return the new max number of suffix
	 */
	private static int findSuffixNumber(String fileName, int maxVal) {
		int val = 0;
		int leftBracket = fileName.lastIndexOf("(");
		int rightBracket = fileName.lastIndexOf(")");

		String s = fileName.substring(leftBracket + 1, rightBracket);

		try {
			val = Integer.parseInt(s);
			if (val > maxVal) {
				return val;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return maxVal;
	}

	/**
	 * 存储器信息
	 * 
	 * @author jiangxh
	 * @CreateTime 2014年5月5日 下午8:10:32
	 * @Description com.aurora.util Util.java
	 */
	public static class SDCardInfo {
		public long total;

		public long free;

		public long inUse;
	}

	/**
	 * 获取存储器信息
	 * 
	 * @param isMounted
	 * @param path
	 * @return
	 */
	public static SDCardInfo getSDCardInfo(boolean isMounted, String path) {
		SDCardInfo info = null;
		if (isMounted && path != null) {
			File pathFile = new File(path);
			info = new SDCardInfo();
			try {
				android.os.StatFs statfs = new android.os.StatFs(
						pathFile.getPath());

				// 获取SDCard上BLOCK总数
				long nTotalBlocks = statfs.getBlockCount();

				// 获取SDCard上每个block的SIZE
				long nBlocSize = statfs.getBlockSize();

				// 获取可供程序使用的Block的数量
				long nAvailaBlock = statfs.getAvailableBlocks();

				// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
				long nFreeBlock = statfs.getFreeBlocks();

				// 计算SDCard 总容量大小MB
				info.total = nTotalBlocks * nBlocSize;

				// 计算 SDCard 剩余大小MB
				info.free = nAvailaBlock * nBlocSize;

				BigDecimal total = new BigDecimal(info.total);
				BigDecimal free = new BigDecimal(info.free);
				info.inUse = total.subtract(free).longValue();

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return info;
	}

	/**
	 * This method gets the extension of a file
	 * 
	 * @param fileName
	 *            the name of the file
	 * @return the extension of the file
	 */
	public static String getFileExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		String extension = null;

		if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
			extension = fileName.substring(lastDot + 1).toLowerCase();
		}
		return extension;
	}

	public static String makePath(String path1, String path2) {
		if (path1.endsWith(File.separator))
			return path1 + path2;

		return path1 + File.separator + path2;
	}

	private static String APK_EXT = "apk";

	/**
	 * 通过后缀名获取文件mimetype类型
	 * 
	 * @param path
	 * @return
	 */
	public static FileCategory getCategoryFromPath(String path) {
		if (path == null || path.equals("")) {
			return FileCategory.Other;
		}
		MediaFileType type = MediaFile.getFileType(path);
		if (type != null) {
			if (MediaFile.isAudioFileType(type.fileType))
				return FileCategory.Music;
			if (MediaFile.isVideoFileType(type.fileType))
				return FileCategory.Video;
			if (MediaFile.isImageFileType(type.fileType))
				return FileCategory.Picture;
			if (MimeTypeUtil.sDocMimeTypesSet.contains(type.mimeType))
				return FileCategory.Doc;
		}

		int dotPosition = path.lastIndexOf('.');
		if (dotPosition < 0) {
			return FileCategory.Other;
		}
		String ext = path.substring(dotPosition + 1);
		if (ext.equalsIgnoreCase(APK_EXT)) {
			return FileCategory.Apk;
		}

		return FileCategory.Other;
	}

	/**
	 * 文件名称长度截取
	 * 
	 * @param fileName
	 * @param textView
	 * @param size
	 * @return
	 */
	public static CharSequence getEllipsize(String fileName, TextView textView,
			int size) {
		if (fileName == null || textView == null) {
			return null;
		}
		return TextUtils.ellipsize(
				fileName.substring(fileName.lastIndexOf("/") + 1,
						fileName.length()), textView.getPaint(), size,
				TextUtils.TruncateAt.MIDDLE);
	}

	
	/**
	 * 4.4及以下版本
	 * @return
	 */
	public static boolean isLowVersion() {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version <= 20) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取APK中图标适用5.0及以上
	 * 
	 * @param activity
	 * @param apkPath
	 * @return
	 */
	public static Drawable getApkIconForLollipop(Context activity, String apkPath){
		String packageParserPath = "android.content.pm.PackageParser";
		String assetManagerPath="android.content.res.AssetManager";
		try {
			Class packageParserClass = Class.forName(packageParserPath);
			Constructor pkg = packageParserClass.getConstructor();
			Object pkgParser = pkg.newInstance();
			Class[] typeArgs = new Class[2];
			typeArgs[0] = File.class;
			typeArgs[1] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = packageParserClass.getDeclaredMethod(
					"parsePackage", typeArgs);
			Object[] valueArgs = new Object[2];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = 0;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);
			Class assetMagCls = Class.forName(assetManagerPath);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = activity.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				return icon;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	/**
	 * 获取APK中图标
	 * 
	 * @param activity
	 * @param apkPath
	 * @return
	 */
	public static Drawable getAPKIcon(Context activity, String apkPath) {
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// apk包的文件路径
			// 这是一个Package 解释器, 是隐藏的
			// 构造函数的参数只有一个, apk文件的路径
			// PackageParser packageParser = new PackageParser(apkPath);
			Class pkgParserCls = Class.forName(PATH_PackageParser);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());
			// 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			// PackageParser.Package mPkgInfo = packageParser.parsePackage(new
			// File(apkPath), apkPath,
			// metrics, 0);
			typeArgs = new Class[4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[3] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);
			valueArgs = new Object[4];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = apkPath;
			valueArgs[2] = metrics;
			valueArgs[3] = 0;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);
			// 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
			// ApplicationInfo info = mPkgInfo.applicationInfo;
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);
			// uid 输出为"-1"，原因是未安装，系统未分配其Uid。
			Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" + info.uid);
			// Resources pRes = getResources();
			// AssetManager assmgr = new AssetManager();
			// assmgr.addAssetPath(apkPath);
			// Resources res = new Resources(assmgr, pRes.getDisplayMetrics(),
			// pRes.getConfiguration());
			Class assetMagCls = Class.forName(PATH_AssetManager);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = activity.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);
			CharSequence label = null;
			if (info.labelRes != 0) {
				label = res.getText(info.labelRes);
			}
			// if (label == null) {
			// label = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel
			// : info.packageName;
			// }
			Log.d("ANDROID_LAB", "label=" + label);
			// 这里就是读取一个apk程序的图标
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				return icon;
				// ImageView image = (ImageView)
				// findViewById(R.id.apkIconBySodino);
				// image.setVisibility(View.VISIBLE);
				// image.setImageDrawable(icon);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
	 * apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 */
	public static Drawable getApkIcon(Context context, String apkPath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);//
		if (info != null) {

			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			AuroraLog.elog(TAG, info.toString());
			try {
				return appInfo.loadIcon(pm);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 文件浏览排除路径
	 * 
	 * @param fullName
	 * @return
	 */
	public static boolean isNormalFile(String fullName) {
		return !fullName.equals(makePath(getSdDirectory(), ANDROID_SECURE));
	}

	/**
	 * 获取SD卡根目录
	 * 
	 * @return
	 */
	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	/**
	 * 获取可用空间
	 * 
	 * @param path
	 * @return
	 */
	public static long getFreeSpace(String path) {
		StatFs stat = new StatFs(path);
		long freeSpace = (long) stat.getAvailableBlocks()
				* (long) stat.getBlockSize();
		return freeSpace;
	}

	/**
	 * 获取内部储存器
	 * 
	 * @return
	 */
	public static String getSDPath(Context context) {
		return gionee.os.storage.GnStorageManager.getInstance(context)
				.getInternalStoragePath();
	}
}
