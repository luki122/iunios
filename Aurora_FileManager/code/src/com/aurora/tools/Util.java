package com.aurora.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.SystemProperties;
import android.R.string;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.filemanager.FileExplorerTabActivity;

import android.os.storage.IMountService;
import android.os.ServiceManager;
import aurora.widget.AuroraUtil;

import com.aurora.tools.AuroraPinYinUtils.Token;
import com.aurora.tools.MediaFile.MediaFileType;
import com.privacymanage.data.AidlAccountData;

public class Util {

	private static final String TAG = "Util";

	private static final String ANDROID_SECURE = ".android_secure";
	private static final String XML = "accountData";
	private static final String XMLId = "id";
	private static final String XMLHome = "home";
	private static final String XMLHomePath = "homePath";
	
	
	
	
	public static boolean isNoMeunKey(){
		String prop = SystemProperties.get("ro.product.model");
		if(prop.equalsIgnoreCase("IUNI U0003")){
			return true;
		}
		return false;
	}
	
	
	
	public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {  
        // Retrieve all services that can match the given intent  
        PackageManager pm = context.getPackageManager();  
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);  
   
        // Make sure only one match was found  
        if (resolveInfo == null || resolveInfo.size() != 1) {  
            return null;  
        }  
   
        // Get component info and create ComponentName  
        ResolveInfo serviceInfo = resolveInfo.get(0);  
        String packageName = serviceInfo.serviceInfo.packageName;  
        String className = serviceInfo.serviceInfo.name;  
        ComponentName component = new ComponentName(packageName, className);  
   
        // Create a new intent. Use the old one for extras and such reuse  
        Intent explicitIntent = new Intent(implicitIntent);  
   
        // Set the component to be explicit  
        explicitIntent.setComponent(component);  
   
        return explicitIntent;  
    }  

	/**
	 * 保存隐私空间主路径
	 * 
	 * @param context
	 * @param path
	 */
	public static void savePrivacyHomePath(Context context, String path) {
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(XMLHomePath, path);
		editor.commit();
	}

	/**
	 * 获取隐私空间主路径
	 * 
	 * @param context
	 */
	public static String getPrivacyHomePath(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		return preferences.getString(XMLHomePath, "");
	}

	/**
	 * 保存隐私帐号
	 * 
	 * @param context
	 * @param accountData
	 */
	public static void savePrivacyAccount(Context context,
			AidlAccountData accountData) {
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(XMLId, accountData.getAccountId());
		editor.putString(XMLHome, accountData.getHomePath());
		editor.commit();
	}

	/**
	 * 获取隐私帐号
	 * 
	 * @param context
	 * @return
	 */
	public static AidlAccountData getPrivacyAccount(Context context,boolean reset) {
		AidlAccountData accountData = new AidlAccountData();
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		accountData.setAccountId(preferences.getLong(XMLId, 0));
		accountData.setHomePath(preferences.getString(XMLHome, ""));
		if(reset){
			AidlAccountData accountData2 = new AidlAccountData();
			savePrivacyAccount(context, accountData2);
		}
		return accountData;
	}
	
	/**
	 * 隐藏键盘
	 * @param context
	 * @param v
	 * @return
	 */
	public static Boolean hideInputMethod(Context context, View v) {
		if (v == null) {
			return false;
		}
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		return false;
	}

	/**
	 * 强制打开键盘
	 * @param context
	 * @return
	 */
	public static boolean showInputMethod(Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {

			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
		}
		return false;
	}
	
	
	/**
	 * 刷新标志
	 * @param context
	 * @param category
	 * @param refresh
	 */
	public static void saveRefreshCategory(Context context,FileCategory category,boolean refresh){
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(category.toString(), refresh);
		editor.commit();
	}
	/**
	 * 获取刷新标志
	 * @param context
	 * @param category
	 * @param reset
	 * @return
	 */
	public static boolean getRefreshCategory(Context context,FileCategory category,boolean reset){
		SharedPreferences preferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		boolean refresh = preferences.getBoolean(category.toString(), false);
		if(reset){
			saveRefreshCategory(context, category,false);
		}
		return refresh;
	}
	


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
	 * 通过文件路径 获取父路径
	 * 
	 * @param filepath
	 *            /
	 * @return
	 */
	public static String getPathFromPath(String filepath) {
		if (TextUtils.isEmpty(filepath)) {
			return "";
		}
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			String path = filepath.substring(0, pos);
			int poss = path.lastIndexOf('/');
			if (poss != -1) {
				return path.substring(0, poss);
			}
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

	/**
	 * 通过路径获取文件信息
	 * 
	 * @param filePath
	 * @return
	 */
	public static FileInfo GetFileInfo(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			LogUtil.e(TAG, "filePath isEmpty ");
			return null;
		}
		lFile = new File(filePath);
		if (!lFile.exists()) {
			LogUtil.e(TAG, "file is no exist");
			return null;
		}

		FileInfo lFileInfo = new FileInfo();
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = Util.getNameFromFilepath(filePath);
		lFileInfo.ModifiedDate = lFile.lastModified();
		lFileInfo.IsDir = lFile.isDirectory();
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
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		lFileInfo.filePath = filePath;
		lFileInfo.fileName = f.getName();
		lFileInfo.IsDir = f.isDirectory();
		lFileInfo.fileSize = f.length();
		lFileInfo.Count = -1;
		if(lFileInfo.IsDir){
			String[] s = f.list(AuroraFilenameFilter.getInstance());
			if(s!=null){
				lFileInfo.Count=s.length;	
			}else {
				lFileInfo.Count=0;
			}
		}
		lFileInfo.ModifiedDate = f.lastModified();
		return lFileInfo;
	}

	public static FileInfo getAllFileInfo(FileInfo fileInfo, boolean showHidden) {
		lFile = new File(fileInfo.filePath);
		fileInfo.canRead = lFile.canRead();
		fileInfo.canWrite = lFile.canWrite();
		fileInfo.isHidden = lFile.isHidden();
		if (fileInfo.IsDir) {
			int lCount = 0;
			String[] s = null;
			if (showHidden) {
				s = lFile.list();
			} else {
				s = lFile.list(AuroraFilenameFilter.getInstance());
			}
			if (s != null) {
				lCount = s.length;
			}
			fileInfo.Count = lCount;

		} else {

			fileInfo.fileSize = lFile.length();

		}
		return fileInfo;
	}

	public static FileInfo getFileInfo(File f, boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		lFileInfo.canRead = f.canRead();
		lFileInfo.canWrite = f.canWrite();
		lFileInfo.isHidden = f.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.ModifiedDate = f.lastModified();
		lFileInfo.IsDir = f.isDirectory();
		lFileInfo.filePath = filePath;
		if (lFileInfo.IsDir) {
			int lCount = 0;
			String[] s = null;
			if (showHidden) {
				s = f.list();
			} else {
				s = f.list(AuroraFilenameFilter.getInstance());
			}
			if (s != null) {
				lCount = s.length;
			}

			lFileInfo.Count = lCount;

		} else {

			lFileInfo.fileSize = f.length();

		}

		// // 避免得到1970-1-1的默认值,改为2012-1-1
		// if (0 == lFileInfo.ModifiedDate)
		// lFileInfo.ModifiedDate = DEFAULT_MODIFIED_DATE;

		return lFileInfo;
	}

	/**
	 * 返回的拼音为大写字母
	 * 
	 * @param str
	 * @return
	 */
	public static String getSpell(String str) {
		if (Build.VERSION.SDK_INT == 17) {
			StringBuffer buffer = new StringBuffer();
			if (str != null && !str.equals("")) {
				char[] cc = str.toCharArray();
				for (int i = 0; i < cc.length; i++) {
					ArrayList<Token> mArrayList = AuroraPinYinUtils
							.getInstance().get(String.valueOf(cc[i]));
					if (mArrayList.size() > 0) {
						String n = mArrayList.get(0).target;
						buffer.append(n);
					}
				}
			}
			String spellStr = buffer.toString();
			return spellStr.toUpperCase();
		} else if (Build.VERSION.SDK_INT > 17) {
			return HanziToPinyin.hanziToPinyin(str);
		}
		return "";
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

	public static String convertStorageG(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
		if (size >= gb) {
			return String.format("%.2fG", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.1fM" : "%.2fM", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.1fK" : "%.2fK", f);
		} else
			return String.format("%dB", size);
	}

	/**
	 * 获取文件信息
	 * 
	 * @param cursor
	 * @return
	 */
	public static FileInfo getFileInfo(Cursor cursor, FileCategory fileCategory) {
		if ((cursor == null || cursor.getCount() == 0)) {
			return null;
		}
		FileInfo fi = null;
		try {
			fi = Util.GetFileInfo(cursor.getString(cursor
					.getColumnIndex(FileColumns.DATA)));
			if (fi != null) {
				fi.dbId = cursor
						.getLong(cursor.getColumnIndex(FileColumns._ID));
				if (fileCategory != null
						&& fileCategory.equals(FileCategory.Picture)) {
					fi.orientation = cursor
							.getInt(cursor
									.getColumnIndex(FileCategoryHelper.FILECOLUMNS_ORIENTATION));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @Description com.aurora.tools Util.java
	 */
	public static class SDCardInfo {
		public long total;

		public long free;

		public long inUse;

		@Override
		public String toString() {
			return "SDCardInfo [total=" + total + ", free=" + free + ", inUse="
					+ inUse + "]";
		}

	}

	/**
	 * 获取存储器信息
	 * 
	 * @param isMounted
	 * @param path
	 * @return
	 */
	@SuppressWarnings("deprecation")
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
				if(nAvailaBlock==0){//极端条件
					info.free=nFreeBlock*nBlocSize;
				}else {
					info.free = nAvailaBlock * nBlocSize;
				}

				BigDecimal total = new BigDecimal(info.total);
				BigDecimal free = new BigDecimal(info.free);
				info.inUse = total.subtract(free).longValue();

			} catch (Exception e) {
				LogUtil.e(TAG, "StatFs error " + e.getLocalizedMessage());
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
	 * 文件名称长度截取
	 * 
	 * @param fileName
	 * @param textView
	 * @param size
	 * @return
	 */
	public static CharSequence getEllipsizeEnd(String fileName,
			TextView textView, int size) {
		if (fileName == null || textView == null) {
			return null;
		}
		return TextUtils.ellipsize(
				fileName.substring(fileName.lastIndexOf("/") + 1,
						fileName.length()), textView.getPaint(), size,
				TextUtils.TruncateAt.END);
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
			// Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" +
			// info.uid);
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
			// CharSequence label = null;
			// if (info.labelRes != 0) {
			// label = res.getText(info.labelRes);
			// }
			// if (label == null) {
			// label = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel
			// : info.packageName;
			// }
			// Log.d("ANDROID_LAB", "label=" + label);
			// 这里就是读取一个apk程序的图标
			if (info.icon != 0) {
				Drawable icon = res.getDrawable(info.icon);
				return icon;
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
			// LogUtil.elog(TAG, info.toString());
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

	private static IMountService mountService = IMountService.Stub
			.asInterface(ServiceManager.getService("mount"));

	public static boolean sdIsMounted(String mount) {
		try {
			if (mountService.getVolumeState(mount).equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private static Matrix matrix = new Matrix();

	/**
	 * picture 转向
	 * 
	 * @param bm
	 * @param r
	 * @return
	 */
	public static Bitmap iamgeRotation(Bitmap bm, int r) {
		int h = bm.getHeight();
		int w = bm.getWidth();
		matrix.setRotate(r);
		Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
		return bitmap;
	}

	/**
	 * 获取文件总大小
	 * 
	 * @param fileInfos
	 * @return
	 */
	public static long getAllSize(List<FileInfo> fileInfos) {
		long size = 0;
		if (fileInfos == null || fileInfos.size() == 0) {
			return 0;
		}
		for (int i = 0; i < fileInfos.size(); i++) {
			String path = fileInfos.get(i).filePath;
			if (!TextUtils.isEmpty(path)) {
				File file = new File(path);
				size = getChildSizes(file, size);
			}
		}
		return size;
	}

	private static long getChildSizes(File file, long size) {
		if (file.isDirectory()) {
			File[] fs = file.listFiles();
			if (fs != null) {// modfiy by JXH 2014-8-11 BUG 7405
				for (int i = 0; i < fs.length; i++) {
					size = getChildSizes(fs[i], size);
				}
			}
		} else {
			size += file.length();
		}
		return size;
	}

	public static int getCounts(List<FileInfo> fileInfos) {
		int count = 0;
		if (fileInfos == null || fileInfos.size() == 0) {
			return 0;
		}
		for (int i = 0; i < fileInfos.size(); i++) {
			String path = fileInfos.get(i).filePath;
			if (!TextUtils.isEmpty(path)) {
				File file = new File(path);
				count = getChildCounts(file, count);
			}
		}
		return count;
	}

	private static int getChildCounts(File file, int count) {
		if (file.isDirectory()) {
			File[] fs = file.listFiles();
			if (fs != null) {// modfiy by JXH 2014-8-11 BUG 7405
				for (int i = 0; i < fs.length; i++) {
					count = getChildCounts(fs[i], count);
				}
			}
		}
		count++;

		return count;
	}

	// 2. 根据手机的分辨率从dp的单位转成px
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	// 3.根据手机的分辨率从px转成dp
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static Typeface auroraCreateTitleFont() {
		Typeface auroraTitleFace = null;
		try {

			auroraTitleFace = Typeface
					.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		} catch (Exception e) {
			// TODO: handle exception
			e.getCause();
			e.printStackTrace();
			auroraTitleFace = null;
		}

		return auroraTitleFace;
	}

	/**
	 * 获取指定目录下文件信息
	 * 
	 * @param path
	 * @return
	 */
	public static List<FileInfo> getPathFiles(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		getPathFiles(file, fileInfos);
		return fileInfos;

	}

	private static void getPathFiles(File file, List<FileInfo> fileInfos) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					getPathFiles(f, fileInfos);
				}
			}
		} else {
			fileInfos.add(getFileInfo(file, false));
		}
	}

	public static List<FileInfo> getPathFilesByBase64(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		final File[] files = file.listFiles();
		if (files != null) {
			try {
				for (File f : files) {
					if (FileExplorerTabActivity.thread != null) {
						if (FileExplorerTabActivity.thread.isInterrupted()) {
							LogUtil.e(
									TAG,
									"isInterrupted=="
											+ FileExplorerTabActivity.thread
													.isInterrupted());
							return null;
						}
					}
					if (!f.getName().contains(".")) {
						String dPath = f.getParent()
								+ File.separator
								+ new String(Base64.decode(f.getName()
										.getBytes(), Base64.URL_SAFE));
						File file2 = new File(Util.replaceBlank(dPath));
						boolean b = f.renameTo(file2);
						if (b) {
							FileUtils.changeFile(file2.getPath());
						}
						fileInfos.add(getFileInfo(file2, false));
					} else {
						fileInfos.add(getFileInfo(f, false));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, "error e " + e.getLocalizedMessage());
			}
		}
//		LogUtil.elog(TAG, "fileInfos.size== " + fileInfos.size());
		return fileInfos;

	}

	public static List<FileInfo> getPathFilesByBase64All(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

		return getPathFilesAll(file, fileInfos);

	}

	public static List<FileInfo> getPathFilesAll(File file,
			List<FileInfo> fileInfos) {
		final File[] files = file.listFiles();
		if (files != null) {
			try {
				for (File f : files) {
					if (f.isDirectory()) {
						fileInfos = getPathFilesAll(f, fileInfos);
					} else {
						if (!f.getName().contains(".")) {
							String dPath = f.getParent()
									+ File.separator
									+ new String(Base64.decode(f.getName()
											.getBytes(), Base64.URL_SAFE));
							File file2 = new File(Util.replaceBlank(dPath));
							f.renameTo(file2);
							FileUtils.changeFile(file2.getPath());
							fileInfos.add(getFileInfo(file2, false));
						} else {
							fileInfos.add(getFileInfo(f, false));
						}

					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return fileInfos;
	}

	/**
	 * 去除字符串中的空格、回车、换行符、制表符
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			dest = str.replace("\n", "");
		}
		return dest;
	}

}
