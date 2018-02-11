package com.aurora.setupwizard.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.aurora.setupwizard.domain.ApkData;
import com.aurora.setupwizard.domain.ApkInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 
 * @author lmjssjj
 *
 */
public class ApkUtil {

	public static List<ApkInfo> readJson() {
		try {
			FileInputStream fis = new FileInputStream(new File(
					Constants.APK_INFO));
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				bao.write(buffer, 0, len);
			}
			String result = bao.toString();

			Log.v("lmjssjj", result);

			if (TextUtils.isEmpty(result)) {
				return null;
			}

			ObjectMapper mapper = new ObjectMapper();

			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);

			ApkData apkData = mapper.readValue(result, ApkData.class);

			return apkData.getDatas();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void getApkInfo() {
		List<ApkInfo> apkDatas = new ArrayList<ApkInfo>();
		try {
			File dataDirs = Environment.getDataDirectory();
			String apkInfoFilePath = dataDirs.getAbsolutePath()
					+ Constants.APK_FILEINFO_NAME;
			File apkInfoFile = new File(apkInfoFilePath);
			if (apkInfoFile != null && apkInfoFile.isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(apkInfoFile)));
				StringBuilder sb = new StringBuilder();
				String str = "";
				while ((str = br.readLine()) != null) {
					sb.append(str);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getApkPath() {
		return "";
	}

	public static boolean isRecommend(){
		File file = new File(Constants.APK_INFO);
		return file.exists();
	}
	
	public static boolean isExistApk() {
		boolean b = false;
		File files = new File(getApkPath());
		File[] listFiles = files.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith("apk")) {
					return true;
				}
				return false;
			}
		});
		if (listFiles != null && listFiles.length > 0) {
			b = true;
		}
		return b;
	}

	public static List<File> getApk(String path) {
		List<File> apks = new ArrayList<File>();
		Log.v("lmjssjj", "===" + path + "===");
		File files = new File(path);
		System.out.println(files.isDirectory() + "--" + files.isFile());
		if (files != null) {
			File[] listFiles = files.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					if (filename.endsWith("apk")) {
						return true;
					}
					return false;
				}
			});
			apks.addAll(Arrays.asList(listFiles));
		}
		return apks;
	}

//	public static List<ApkInfo> getAllApp(Context context) {
//		List<ApkInfo> list = readJson();
//		if (list == null) {
//			return null;
//		}
//		for (ApkInfo apkInfo : list) {
//			Log.v("lmjssjj", apkInfo.getApkPath());
//			Log.v("lmjssjj", apkInfo.getApkPackageName());
//			addApkInfo(context, apkInfo);
//			Log.v("lmjssjj", apkInfo.getApkName());
//		}
//		return list;
//	}

	public static List<ApkInfo> selectAndIcon() {
		List<ApkInfo> list = readJson();
		if (list == null) {
			return null;
		}
		List<ApkInfo> result = new ArrayList<ApkInfo>();
		for (ApkInfo apkInfo : list) {
			apkInfo.setCheck(false);
			
			if(TextUtils.isEmpty(apkInfo.getApkPath())||!(new File(apkInfo.getApkPath()).exists())){
				continue;
			}
			
			if (apkInfo.getApkIconPath() != null) {
				Bitmap bitmap = BitmapFactory.decodeFile(apkInfo
						.getApkIconPath());
				if (bitmap != null) {
					apkInfo.setApkIcon(new BitmapDrawable(bitmap));
				}
			}
			
			result.add(apkInfo);
		}
		return result;
	}

	public static List<ApkInfo> getApkInfo(Context context) {

		List<ApkInfo> lists = selectAndIcon();

		if (lists == null) {
			return null;
		}
		for (ApkInfo apkInfo2 : lists) {
			
			Drawable icon = apkInfo2.getApkIcon();
			
			if (icon == null) {
				
				Drawable apkIcon = getApkIcon(context, apkInfo2.getApkPath());
//				PackageManager pm = context.getPackageManager();
//				PackageInfo info = pm.getPackageArchiveInfo(
//						apkInfo2.getApkPath(), PackageManager.GET_ACTIVITIES);
				
				if (apkIcon != null) {
//					ApplicationInfo appInfo = info.applicationInfo;
//					Drawable loadIcon = appInfo.loadIcon(pm);
					apkInfo2.setApkIcon(apkIcon);
				}
			}
			
		}
		return lists;
	}

	public static ApkInfo getApkInfo(Context context, String apkPath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		ApkInfo apkInfo = null;
		if (info != null) {
			apkInfo = new ApkInfo();
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			String label = appInfo.loadLabel(pm).toString();
			String packageName = appInfo.packageName;
			String version = info.versionName == null ? "0" : info.versionName;
			Drawable loadIcon = appInfo.loadIcon(pm);
			apkInfo.setApkName(label);
			apkInfo.setApkPackageName(packageName);
			apkInfo.setApkPath(apkPath);
			apkInfo.setApkIcon(loadIcon);
		}
		return apkInfo;
	}

	public static Drawable getApkIcon(Context context, String apkPath) {
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			// String label = appInfo.loadLabel(pm).toString();
			// String packageName = appInfo.packageName;
			// String version = info.versionName==null?"0":info.versionName;
			try {
				return appInfo.loadIcon(pm);
			} catch (OutOfMemoryError e) {

			}
		}
		return null;
	}

	/**
	 * @Title: intstallApp
	 * @Description: 安装应用
	 * @param @param context
	 * @param @param apkFile
	 * @param @param observer
	 * @return void
	 * @throws
	 */
	public static void intstallApp(Context context, String packageName,
			File apkFile, IPackageInstallObserver.Stub observer) {
		PackageManager pm = context.getPackageManager();
		if (TextUtils.isEmpty(packageName)) {
			PackageParser.Package parsed = getPackageInfo(apkFile);
			packageName = parsed.packageName;
		}
		int installFlags = 0;
		try {
			PackageInfo pi = pm.getPackageInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
			}
		} catch (NameNotFoundException e) {
		}
		Uri mPackageURI = Uri.fromFile(apkFile);
		String filepath = mPackageURI.getPath();
		pm.installPackage(mPackageURI, observer, installFlags, null);
	}

	private static PackageParser.Package getPackageInfo(File sourceFile) {

		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		Object pkg = null;
		final String archiveFilePath = sourceFile.getAbsolutePath();
		try {
			Class<?> clazz = Class.forName("android.content.pm.PackageParser");
			Object instance = getParserObject(archiveFilePath);
			if (Build.VERSION.SDK_INT >= 21) {
				Method method = clazz.getMethod("parsePackage", File.class,
						int.class);
				pkg = method.invoke(instance, sourceFile, 0);
			} else {
				Method method = clazz.getMethod("parsePackage", File.class,
						String.class, DisplayMetrics.class, int.class);
				pkg = method.invoke(instance, sourceFile, archiveFilePath,
						metrics, 0);
			}
			instance = null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (PackageParser.Package) pkg;
	}

	private static Object getParserObject(String archiveFilePath)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException {
		Class<?> clazz = Class.forName("android.content.pm.PackageParser");
		return Build.VERSION.SDK_INT >= 21 ? clazz.getConstructor()
				.newInstance() : clazz.getConstructor(String.class)
				.newInstance(archiveFilePath);
	}
	
	public static void deleteFile(File file) {
		if (file.exists()) { 
			if (file.isFile()) { 
				boolean delete = file.delete(); 
				Log.v("lmjssjj", "delete file:"+file.getName()+"===="+delete);
			} else if (file.isDirectory()) { 
				File files[] = file.listFiles(); 
				for (int i = 0; i < files.length; i++) {
					deleteFile(files[i]); 
				}
			}
			file.delete();
		} else {
			
		}
	}

}
