/*
 * @author zw
 */

package com.aurora.note.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;

import com.aurora.note.NoteApp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public final static String EMULATOR_DEVICE_ID = "000000000000000";

    public static boolean isIndiaVersion() {
        return SystemProperties.get("ro.iuni.country.option", "unknown").equals("INDIA");
    }

    public static boolean isChineseEnvironment() {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.CHINA.getLanguage())
                || lang.equals(Locale.CHINESE.getLanguage())
                || lang.equals(Locale.TAIWAN.getLanguage())
                || lang.equals(Locale.SIMPLIFIED_CHINESE.getLanguage())
                || lang.equals(Locale.TRADITIONAL_CHINESE.getLanguage())) {
            return true;
        }

        return false;
    }

    public static String getSysLang() {
        return String.format("%s-%s", Locale.getDefault().getLanguage(), Locale
                .getDefault().getCountry());
    }

    public static String getFromAssets(Context mcontent, String fileName) {
        StringBuffer sb = new StringBuffer();
        try {
            InputStreamReader inputReader = new InputStreamReader(mcontent
                    .getResources().getAssets().open(fileName), "GB2312");
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

    public static String getModelNumber() {
        return URLEncoder.encode(Build.MODEL);
    }

    public static String getBuildNumber() {
        return URLEncoder.encode(Build.DISPLAY);
    }

    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static boolean isEmulator(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        return EMULATOR_DEVICE_ID.equalsIgnoreCase(manager.getDeviceId());
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

    public static boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) NoteApp.ysApp
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

    public static boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) NoteApp.ysApp
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

    public static boolean isWifiAvailable(Context context) {
        WifiManager manager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        String macAddress = (wifiInfo == null ? null : wifiInfo.getMacAddress());

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

    public static boolean hasNetwork() {
		ConnectivityManager cm = (ConnectivityManager) NoteApp.ysApp
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if (wifiState == State.CONNECTED || mobileState == State.CONNECTED) {
			return true;
		} else {
			return false;
		}
	}

    public static int getConnectingType() {
        ConnectivityManager mConnectivity = (ConnectivityManager) NoteApp.ysApp
                .getSystemService(Context.CONNECTIVITY_SERVICE);

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
            } else {
                return Globals.NETWORK_3G;
            }
        }

    }

    public static boolean isCPUFreqOK() {
        ProcessBuilder cmd;
        String result = null;

        try {
            String[] args = {
                    "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
            };
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

    public static boolean isWifiNetwork() {
        if (!SystemUtils.isNetworkConnected()) {
            // 当前网络获取判断，如无网络连接，直接后台日志
            Log.d(TAG, "isWifiNetwork None network");
            return false;
        }
        // 连接后判断当前WIFI
        if (SystemUtils.getConnectingType() == Globals.NETWORK_WIFI) {
            return true;
        } else {
            return false;
        }
    }

    public static int getNetStatus() {
		if (!SystemUtils.isNetworkConnected()) {
			// 当前网络获取判断，如无网络连接，直接后台日志
			Log.d(TAG, "isWifiNetwork None network");
			return 0;
		}
		// 连接后判断当前WIFI
		if (SystemUtils.getConnectingType() == Globals.NETWORK_WIFI) {
			return 1;
		} else {
			return 2;
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
    public static ArrayList<String> find(final String sourceString, String object) {
        ArrayList<String> rep_str = new ArrayList<String>();

        Pattern p = Pattern.compile(object);// 正则表达式，取=和|之间的字符串，不包括=和|
        Matcher m = p.matcher(sourceString);

        while (m.find()) {
            rep_str.add(m.group(1));
        }
        return rep_str;
    }

    /**
     * 获取所有的图片的路径
     * @param sourceString
     * @return
     */
    public static ArrayList<String> getAllImageList(final String sourceString) {
        ArrayList<String> rep_str = new ArrayList<String>();

//        String image_obj = Globals.ATTACHMENT_IMAGE_PATTERN;
//        Pattern p = Pattern.compile(image_obj);
//        Matcher m = p.matcher(sourceString);
//        while (m.find()) {
//            rep_str.add(m.group(1));
//        }
//        
//        image_obj = Globals.ATTACHMENT_IMAGE_GROUP_PATTERN;
//        p = Pattern.compile(image_obj);
//        m = p.matcher(sourceString);
//        while (m.find()) {
//            String path = m.group(1);
//            String[] allPath = path.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
//            for (String imagePath: allPath) {
//                rep_str.add(imagePath);
//            }
//        }

        String image_obj = Globals.ATTACHMENT_IMAGE_PATTERN;
        Pattern p = Pattern.compile(image_obj);
        Matcher m = p.matcher(sourceString);
        while (m.find()) {
            String path = m.group(1);
            String[] allPath = path.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
            for (String imagePath: allPath) {
                rep_str.add(imagePath);
            }
        }

        return rep_str;
    }

    public static ArrayList<String> getImagesExceptPreset(final String sourceString) {
        ArrayList<String> rep_str = new ArrayList<String>();

        String image_obj = Globals.ATTACHMENT_IMAGE_EXCEPT_PRESET_PATTERN;
        Pattern p = Pattern.compile(image_obj);
        Matcher m = p.matcher(sourceString);
        while (m.find()) {
            String path = m.group(1);
            String[] allPath = path.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
            for (String imagePath: allPath) {
                rep_str.add(imagePath);
            }
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

    public static int getImageCount(final String sourceString) {
        int sum = 0;

        Pattern p = Pattern.compile(Globals.ATTACHMENT_IMAGE_PATTERN);
        Matcher m = p.matcher(sourceString);

        while (m.find()) {
            String imagePaths = m.group(1);
            String[] paths = imagePaths.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
            sum += paths.length;
        }

        return sum;
    }

    public static void lengthFilter(final EditText editText, final int max_length, final String err_msg) {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(max_length) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                    Spanned dest, int dstart, int dend) {

                int destLen = getCharacterNum(dest.toString()); // 获取字符个数(一个中文算2个字符)
                int sourceLen = getCharacterNum(source.toString());
                if (destLen + sourceLen > max_length) {
                    ToastUtil.shortToast(err_msg);
                    return "";
                }

                return source;
            }
        };

        editText.setFilters(filters);
    }

    /**
     * @description 获取一段字符串的字符个数（包含中英文，一个中文算2个字符）
     * @param content
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
     * @description 返回字符串里中文字或者全角字符的个数
     * @param s
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

    public static Typeface getTypeface(String typefaceName) {
        return Typeface.createFromFile(typefaceName);
    }

    public static void vibrate(long duration) {
        Context context = NoteApp.ysApp;
        Vibrator vibrator = (Vibrator) NoteApp.ysApp.getSystemService(Context.VIBRATOR_SERVICE);

        final boolean hapticEnabled = Settings.System.getInt(
                context.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0;
        if (hapticEnabled) {
            vibrator.vibrate(duration);
        }
    }
}
