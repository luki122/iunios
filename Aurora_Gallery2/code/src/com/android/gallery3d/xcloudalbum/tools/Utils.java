package com.android.gallery3d.xcloudalbum.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.android.gallery3d.exif.ExifInterface.Saturation;

import android.R.string;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.text.TextUtils;
import android.widget.TextView;

public class Utils {
	private static final String TAG="Utils";

	private static long lastClickTime2;

	public static boolean isFastDouble() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime2;
		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime2 = time;
		return false;
	}

	
	private static long lastTotalRxBytes = 0;
	private static long lastTimeStamp = 0;
	public static void showNetSpeed(Context context) {
		 
	    long nowTotalRxBytes = getTotalRxBytes(context);
	    long nowTimeStamp = System.currentTimeMillis();
	    long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
	    LogUtil.d(TAG, "speed "+speed+" kb/s");
	    lastTimeStamp = nowTimeStamp;
	    lastTotalRxBytes = nowTotalRxBytes;
	}

	private static long getTotalRxBytes(Context context) {
	    return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid)==TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);//转为KB
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

	public static String getPathNameFromPath(String filepath) {
		if (filepath == null) {
			return "";
		}
		int dotPosition = filepath.lastIndexOf('/');
		if (dotPosition != -1) {
			return filepath.substring(dotPosition + 1, filepath.length());
		}
		return "";
	}

	/**
	 * 判断APP是否在后台运行
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isRunningForeground(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName)
				&& currentPackageName.equals(context.getPackageName())) {
			return true;
		}

		return false;
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

	private static final String XML = "getFileListDiffFromBaidu";
	private static final String Diff = "FileListDiff";

	public synchronized static void setListTag(Context context, String diff) {
		if (context == null) {
			return;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(Diff, diff);
		editor.commit();
	}

	public synchronized static String getListTag(Context context) {
		if (context == null) {
			return null;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(XML,
				Context.MODE_PRIVATE);
		return sharedPreferences.getString(Diff, null);
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
	
	/**
	 * 检测网络是否连接
	 * 
	 * @param context
	 * @return true : 网络连接成功
	 * @return false : 网络连接失败
	 * */
	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager != null) {
			// 通过ConnectivityManager得到NetworkInfo网络信息
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info != null) {
				// 判断当前网络是否已经连接
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	
}
