/*
 * @author zw
 */
package com.aurora.thememanager.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aurora.thememanager.ThemeManagerApplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SystemUtils {
	private static String TAG = "SystemUtils";

	private SystemUtils() {
	}

	public final static int SDK_15 = 3;
	public final static int SDK_16 = 4;
	public final static int SDK_20 = 5;
	public final static int SDK_201 = 6;
	public final static int SDK_21 = 7;
	public final static int SDK_22 = 8;
	public final static int SDK_23 = 9;
	
	/**
	 * 当前os版本
	 */
	public static final float OS_VERSION = 2.0f;
	

	// screen resolution of u3
	public final static int U3_SCREEN_WIDTH = 1440;
	public final static int U3_SCREEN_HEIGHT = 2400;
	
	
	public final static String EMULATOR_DEVICE_ID = "000000000000000";

	public static String getSysLang() {
		return String.format("%s-%s", Locale.getDefault().getLanguage(), Locale
				.getDefault().getCountry());
	}

	public static String getFromAssets(Context context, String fileName) {
		StringBuffer sb = new StringBuffer();
		try {

			InputStreamReader inputReader = new InputStreamReader(context
					.getResources().getAssets().open(fileName), "utf-8");
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";

			while ((line = bufReader.readLine()) != null)
				sb.append(line);
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			FileLog.e(TAG, e.toString());
		}
		return sb.toString();
	}

	public static String getSysCountry() {
		return Locale.getDefault().getCountry();
	}

	// 取得mainfest中的versionCode
	public static int getVersionCode(Context context, String packageName) {
		int versionCode = -1;
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					packageName, 0).versionCode;
		} catch (NameNotFoundException e) {

			FileLog.e(TAG, e.toString());
		}
		return versionCode;
	}

	// 取得mainfest中的VersionName
	public static String getVersionName(Context context, String packageName) {
		String versionName = "";
		try {
			versionName = context.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			FileLog.e(TAG, e.toString());
		}
		return versionName;
	}
	
	public static String getNetworkType(Context context){
		int type = getConnectingType(context);
		switch (type) {
		case Globals.NETWORK_2G:
			
			return "2G";
		case Globals.NETWORK_3G:
			return "3G";
		case Globals.NETWORK_4G:
			
			return "4G";
		case Globals.NETWORK_WIFI:
			
			return "wifi";
		default:
			return "wifi";
		}
	}
	
	/**
	 * Get IMEI of this phone
	 * @param context
	 * @return
	 */
	public static String getImei(Context context){
		TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * 获取屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getDisplayWidth(Context context){
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	/**
	 * 获取屏幕高度
	 * @param context
	 * @return
	 */
	public static int getDisplayHeight(Context context){
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	

	/**
	 * Get phone model
	 * @return
	 */
	public static String getModel(){
		Log.d("model", ""+android.os.Build.MODEL );
		return android.os.Build.MODEL;
	}
	
	public static boolean isSimReady(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		int nSimState = manager.getSimState();

		return (nSimState == TelephonyManager.SIM_STATE_READY);
	}

	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public static boolean hasActiveNetwork(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");
		if (connectivityManager.getActiveNetworkInfo() != null) {
			return connectivityManager.getActiveNetworkInfo().isAvailable();
		} else {
			return false;
		}
	}

	public static boolean isNetworkReady(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		return (manager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isAvailable()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		} else {
			NetworkInfo[] info = manager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isWifiEnabled(Context context) {
		WifiManager manager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		return (manager == null ? false : manager.isWifiEnabled());
	}

	public static String getMacAddress(Context context)
	{
		WifiManager manager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = manager.getConnectionInfo();
		String macAddress = (wifiInfo == null ? null : wifiInfo.getMacAddress());
		return macAddress;
	}
	
	
	public static boolean isWifiAvailable(Context context) {
		
		String macAddress = getMacAddress(context);

		return (macAddress != null);
	}

	public static boolean isMobileNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");

		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if (info == null) {
			return false;
		}

		int netType = info.getType();

		// Check if Mobile Network is connected
		if (netType == ConnectivityManager.TYPE_MOBILE) {
			return info.isConnected();
		} else {
			return false;
		}
	}

	public static boolean isHighSpeedConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService("connectivity");

		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		if (info == null) {
			return false;
		}

		int netType = info.getType();
		int netSubtype = info.getSubtype();

		// Check if WiFi or 3G is connected
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return info.isConnected();
		} else if (netType == ConnectivityManager.TYPE_MOBILE
				&& netSubtype >= TelephonyManager.NETWORK_TYPE_UMTS) {
			return info.isConnected();
		} else {
			return false;
		}
	}
	/**
	 * 获取是否有网络
	 * 
	 * @return
	 */
	public static boolean hasNetwork() {
		Context context = ThemeManagerApplication.getInstance();
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();
			State mobileState = cm.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();
			if (wifiState == State.CONNECTED || mobileState == State.CONNECTED) {
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	public static int getConnectingType(Context context) {
		ConnectivityManager mConnectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mTelephony = (TelephonyManager) context
				.getSystemService(context.TELEPHONY_SERVICE);

		NetworkInfo info = mConnectivity.getActiveNetworkInfo();

		if (info == null || !mConnectivity.getBackgroundDataSetting()) {
			return -1;
		}

		int netType = info.getType();
		int netSubtype = info.getSubtype();

		if (netType == ConnectivityManager.TYPE_WIFI) {
			return Globals.NETWORK_WIFI;
		} else {
			if ((netSubtype == TelephonyManager.NETWORK_TYPE_GPRS)
					|| (netSubtype == TelephonyManager.NETWORK_TYPE_EDGE)
					|| (netSubtype == TelephonyManager.NETWORK_TYPE_CDMA)) {
				return Globals.NETWORK_2G;
			} else if(netSubtype == TelephonyManager.NETWORK_TYPE_LTE){
				return Globals.NETWORK_4G;
			}else {
				return Globals.NETWORK_3G;
			}
		}

	}

	public static boolean isCPUFreqOK() {
		ProcessBuilder cmd;
		String result = null;

		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();

			InputStreamReader sr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(sr);

			result = br.readLine();
			if (result != null) {
				Log.i("CPU_Freq", result);
			}

			in.close();

		} catch (IOException e) {
			FileLog.e(TAG, e.toString());
		}
		if (result != null) {
			return (Integer.valueOf(result) >= 990000);
		} else {
			return false;
		}

	}

	public static boolean isSnsAccount(String username) {
		if (username.startsWith("#") == true) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * base64的解码
	 */
	public static String decodeBase64(String s) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			decodeBase64(s, bos);
		} catch (IOException e) {
			FileLog.e(TAG, e.toString());
			throw new RuntimeException();
		}
		String result = bos.toString();
		try {
			bos.close();
			bos = null;
		} catch (IOException e) {
			FileLog.e(TAG, e.toString());
		}
		return result;
	}

	private static void decodeBase64(String s, OutputStream os)
			throws IOException {
		int i = 0;

		int len = s.length();

		while (true) {
			while (i < len && s.charAt(i) <= ' ')
				i++;

			if (i == len)
				break;

			int tri = (decodeBase64(s.charAt(i)) << 18)
					+ (decodeBase64(s.charAt(i + 1)) << 12)
					+ (decodeBase64(s.charAt(i + 2)) << 6)
					+ (decodeBase64(s.charAt(i + 3)));

			os.write((tri >> 16) & 255);
			if (s.charAt(i + 2) == '=')
				break;
			os.write((tri >> 8) & 255);
			if (s.charAt(i + 3) == '=')
				break;
			os.write(tri & 255);

			i += 4;
		}
	}

	private static int decodeBase64(char c) {
		if (c >= 'A' && c <= 'Z')
			return ((int) c) - 65;
		else if (c >= 'a' && c <= 'z')
			return ((int) c) - 97 + 26;
		else if (c >= '0' && c <= '9')
			return ((int) c) - 48 + 26 + 26;
		else
			switch (c) {
			case '+':
				return 62;
			case '/':
				return 63;
			case '=':
				return 0;
			default:
				throw new RuntimeException("unexpected code: " + c);
			}
	}

	public static int Dip2Px(Context context, int dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);

	}

	public static float Dip2Px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (float) dpValue * scale;

	}

	public static boolean isWifiNetwork(Context context) {
		if (!SystemUtils.isNetworkConnected(context)) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			Log.d(TAG, "isWifiNetwork None network");
			return false;
		}
		// 连接后判断当前WIFI
		if (SystemUtils.getConnectingType(context) == Globals.NETWORK_WIFI) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int isNetStatus(Context context) {
		if (!SystemUtils.isNetworkConnected(context)) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			Log.d(TAG, "isWifiNetwork None network");
			return 0;
		}
		// 连接后判断当前WIFI
		if (SystemUtils.getConnectingType(context) == Globals.NETWORK_WIFI) {
			return 1;
		} else {
			return 2;
		}
	}
	
	public static boolean isLoadingImage(Context cxt) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
		boolean nFlag = sp.getBoolean("none_download_pic_key", false);
		SharedPreferences sp1 = cxt.getSharedPreferences(Globals.SHARED_WIFI_UPDATE,
				cxt.MODE_APPEND);
		
		int iswifi = sp1.getInt(
				Globals.SHARED_NETSTATUS_KEY_ISEXITS, 0);
		if(nFlag && (iswifi == 2))
		{
			return false;
		}
		else
		{
			return true;
		}
		
	}
	public float getDimenPixenSize(Context context) {
		float size = 0;

		return size;
	}

	public static boolean isNull(String str) {
		boolean bl = false;
		if ((null != str) && (!str.equals("")) && (!str.equals("null"))) {
			bl = false;
		} else {
			bl = true;
		}

		return bl;

	}

	@SuppressLint("NewApi")
	public static void setBuildSDKBackground(View v, Drawable drawable) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			v.setBackgroundDrawable(drawable);
		} else {
			v.setBackground(drawable);
		}
	}

	// 正则表达式 替换字符串
	// String[][] object={new String[]{"\\[image::::","\\'"},new
	// String[]{"\\]","\\'"}};
	// Log.i(TAG,replace(str,object));
	// String content =
	// replace(viewHolder.tvw_card_content.getText().toString(),object);
	// String[][] object = { new String[] { "\\[image::::(.*?)\\::::]", "" } };
	public static String replace(final String sourceString, Object[] object) {
		String temp = sourceString;
		for (int i = 0; i < object.length; i++) {
			String[] result = (String[]) object[i];
			Pattern pattern = Pattern.compile(result[0]);
			Matcher matcher = pattern.matcher(temp);
			temp = matcher.replaceAll(result[1]);
		}
		return temp;
	}

	// "\\[image::::(.*?)\\]"
	//
	public static ArrayList<String> find(final String sourceString,
			String object) {
		ArrayList<String> rep_str = new ArrayList<String>();

		Pattern p = Pattern.compile(object);// 正则表达式，取=和|之间的字符串，不包括=和|
		Matcher m = p.matcher(sourceString);

		while (m.find()) {
			rep_str.add(m.group(1));
		}
		return rep_str;
	}

	public static int getCount(final String sourceString, String object) {
		int sum = 0;

		Pattern p = Pattern.compile(object);// 正则表达式，取=和|之间的字符串，不包括=和|
		Matcher m = p.matcher(sourceString);

		while (m.find()) {
			sum++;
		}
		return sum;
	}

	public static void lengthFilter(final Context context,
			final EditText editText, final int max_length, final String err_msg) {

		InputFilter[] filters = new InputFilter[1];

		filters[0] = new InputFilter.LengthFilter(max_length) {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,

			Spanned dest, int dstart, int dend) {

				int destLen = getCharacterNum(dest.toString()); // 获取字符个数(一个中文算2个字符)

				int sourceLen = getCharacterNum(source.toString());

				if (destLen + sourceLen > max_length) {

					Toast.makeText(context, err_msg, Toast.LENGTH_SHORT).show();

					return "";

				}

				return source;

			}

		};

		editText.setFilters(filters);

	}

	/**
	 * 
	 * @description 获取一段字符串的字符个数（包含中英文，一个中文算2个字符）
	 * 
	 * @param content
	 * 
	 * @return
	 */

	public static int getCharacterNum(final String content) {

		if (null == content || "".equals(content)) {

			return 0;

		} else {

			// return (content.length() + getChineseNum(content));
			return content.length();

		}

	}

	/**
	 * 
	 * @description 返回字符串里中文字或者全角字符的个数
	 * 
	 * @param s
	 * 
	 * @return
	 */

	public static int getChineseNum(String s) {

		int num = 0;

		char[] myChar = s.toCharArray();

		for (int i = 0; i < myChar.length; i++) {

			if ((char) (byte) myChar[i] != myChar[i]) {

				num++;

			}

		}

		return num;

	}
	
	/**
	* @Title: bytes2kb
	* @Description: byte转为KB或者MB字符串
	* @param @param bytes
	* @param @return
	* @return String
	* @throws
	 */
	public static String bytes2kb(long bytes) {
		BigDecimal filesize = new BigDecimal(bytes);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		if (returnValue > 1)
			return (returnValue + "M");
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		return (returnValue + "K");
	}
	
	/**
	* @Title: getTimeString
	* @Description: 获取时间戳，格式为20140702102040
	* @param @param time
	* @param @return
	* @return String
	* @throws
	 */
	public static String getTimeString(long time) {
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		return dateFormat.format(date);
	}
	
	/** 
	* @Title: isDownload
	* @Description: wifi下才能下载
	* @param @param cxt
	* @param @return
	* @return boolean
	* @throws 
	*/ 
	public static boolean isDownload(Context cxt) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(cxt);
		boolean nFlag = sp.getBoolean("wifi_download_key", false);

		boolean isMobile = SystemUtils.isMobileNetworkConnected(cxt);

		if (nFlag && isMobile) {
			return false;
		} else {
			return true;
		}
	}
	
	/** 
	* @Title: isHold
	* @Description: 判断下载的应用安装文件是否保留
	* @param @param cxt
	* @param @return
	* @return boolean
	* @throws 
	*/ 
	public static boolean isHold(Context cxt) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(cxt);
		boolean nFlag = sp.getBoolean("hold_app_key", false);

		return nFlag;
	}

	private static BufferedReader br;
	// 获取build.prop中的指定属性
	public static String getBuildProproperties(String PropertiesName) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(
					new File("/system/build.prop")));
			br = new BufferedReader(new InputStreamReader(is));
			String strTemp = "";
			while ((strTemp = br.readLine()) != null) {// 如果文件没有读完则继续
				if (strTemp.indexOf(PropertiesName) != -1)
					return strTemp.substring(strTemp.indexOf("=") + 1);
			}
			br.close();
			is.close();
			return null;
		} catch (Exception e) {
			if (e.getMessage() != null)
				System.out.println(e.getMessage());
			else
				e.printStackTrace();
			return null;
		}
	}
}
