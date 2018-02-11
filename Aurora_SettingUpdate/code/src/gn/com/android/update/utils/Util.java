
package gn.com.android.update.utils;

import gn.com.android.update.business.EnvironmentConfig;
import gn.com.android.update.business.NetworkConfig;
import gn.com.android.update.business.NoSpaceException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.io.Reader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.lang.reflect.Method;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.os.SystemProperties;

import com.aurora.ota.config.RegionXMLParser;
import com.aurora.utils.ProductConfiguration;
import com.aurora.utils.DecodeUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.os.RecoverySystem;
import gionee.os.GnRecoverySystem;
import android.util.Log;
import android.util.Slog;

/**
 * @author
 */
public class Util {

    private static final String TAG = "Util:";

    /**
     * get imei of phone
     * 
     * @param context
     * @return imei
     */
    public static String getImei(Context context) {
        String imei = null;
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            imei = tm.getDeviceId();
        }
        return imei;
    }

    /**
     * get ua info
     * 
     * @param imei
     * @return ua string
     */
    public static String getUaString(String imei) {
        return ProductConfiguration.getUAString(imei);
    }

    /**
     * Determine whether the characters in the string are ascii characters
     * 
     * @param s
     * @return
     */
    public static boolean isAsci(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 126 || chars[i] < 32) {
                return false;
            }
        }

        return true;
    }

    /**
     * get encrypted imei
     * 
     * @param imei
     * @return encrypted imei
     */
    public static String getEncryptionImei(String imei) {
        return DecodeUtils.get(imei);
    }

    /**
     * get ua info
     * 
     * @return ua string
     */
    public static String getProduct() {
    	return Util.getModel();
       // return android.os.Build.MODEL;
    }

    /**
     * get gionee internal number
     * 
     * @return internal number
     */
    public static String getInternalVersion() {
        // ro.gn.iuniznvernumber
        // ro.gn.gnznvernumber
        return SystemProperties.get("ro.gn.iuniznvernumber");
    }

    public static String getRecoveryVersion() {

        return SystemProperties.get("ro.gn.iunirecoverynumber");
    }

    static class ChannelHandler extends DefaultHandler {

        private boolean isRecovery;
        private String recovery;

        public String getRecovery() {
            return recovery;
        }

        @Override
        public void startDocument() throws SAXException {
            // TODO Auto-generated method stub
        }

        @Override
        public void endDocument() throws SAXException {
            // TODO Auto-generated method stub
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            // TODO Auto-generated method stub
            if (localName.equals("Recovery")) {
                isRecovery = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // TODO Auto-generated method stub
            if (localName.equals("Recovery")) {
                isRecovery = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isRecovery) {
                // Log.e("channel", new String(ch, start, length));
                recovery = new String(ch, start, length);
            }
            // TODO Auto-generated method stub
        }

    }

    private static String parseRecovery(String xml) {
        String defaultRecovery = "";
        if (TextUtils.isEmpty(xml)) {
            return defaultRecovery;
        }
        StringReader reader = new StringReader(xml);
        InputSource source = new InputSource(reader);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            ChannelHandler handler = new ChannelHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(source);
            defaultRecovery = handler.getRecovery();

        } catch (Exception e) {
            // TODO: handle exception
            defaultRecovery = "";
        }

        return defaultRecovery;
    }

    public static String getRecovery() {
        String Recovery = "";
        File file = new File("/system/iuni/rcconfig.xml");
        FileInputStream FIS = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        InputStreamReader inReader = null;
        try {
            FIS = new FileInputStream(file);
            inReader = new InputStreamReader(FIS);
            reader = new BufferedReader(inReader);
            String str = null;
            if (reader != null) {
                while ((str = reader.readLine()) != null) {
                    builder.append(str + "\n");
                }
            }
            Log.e("Recovery", "Recovery");
            Log.e("Recovery", parseRecovery(builder.toString()));
            Recovery = parseRecovery(builder.toString());
        } catch (Exception e) {
            Recovery = "";
        } finally {
            if (FIS != null) {
                try {
                    FIS.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block

                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block

                }
            }
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block

                }
            }
        }
        return Recovery;
    }

    /*heweijiang, modify check root method,20151219,begin*/
    /**
     * execute shell command
     * @param shellCmd
     */
    private static void execShellCommand(String shellCmd) {
        try {
            Process process = Runtime.getRuntime().exec(shellCmd);
            int exitValue = process.waitFor();
            if (0 != exitValue) {
                LogUtils.logd("check root", "exec shell command. error code is :" + exitValue);
            }
        } catch (Throwable e) {
            LogUtils.logd("check root","exec shell command. " + e);
        }
    }

    /**
     * check root by read /data/aurora/rootstatus
     * 0 not root
     * 1 already rooted
     * -1 Md5 has not generated
     * @return boolean isRoot
     */
    public static boolean isRoot(){
        execShellCommand("/system/bin/sh /system/etc/md5_check.sh");
        File file = new File("/data/aurora/rootstatus");
        Reader reader = null;
        int rootCode = 0;
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            rootCode = reader.read();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtils.logd("check root", "result code = " + rootCode);
        return (rootCode == 49); // 1 -- 49
    }
    /*heweijiang, modify check root method,20151219,end*/

    /**
     * get environment type
     * 
     * @see EnvironmentConfig
     * @return environment type
     */
    public static int getEnvironment() {
        String sdcardPath = StorageUtil.getExternalStoragePath();
        StringBuilder developerUrl = new StringBuilder();
        boolean developerTest = false;
        File dirOtaTest = new File(sdcardPath + EnvironmentConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME);
        File dirNormalTestVersion = new File(sdcardPath
                + EnvironmentConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME
                + EnvironmentConfig.GIONEE_OTA_TEST_PACKAGE_FLAGE_NORMAL);
        File dirTestTestVersion = new File(sdcardPath
                + EnvironmentConfig.GIONEE_OTA_TEST_FLAGE_FILE_NAME
                + EnvironmentConfig.GIONEE_OTA_TEST_PACKAGE_FLAGE_TEST);

        int environment = EnvironmentConfig.NORMAL_ENVIRONMENT_NORMAL_VERSION;
        if (dirOtaTest.exists()) {
            if (dirOtaTest.isDirectory()) {
                File[] children = dirOtaTest.listFiles();
                if (children != null && children.length > 0) {
                    for (int i = 0; i < children.length; i++) {
                        String childName = children[i].getName();
                        developerTest = isIp(childName);
                        if (developerTest) {
                            developerUrl.append(NetworkConfig.IUNIOS_DEVELOPER_HOST_PREFX);
                            developerUrl.append(childName);
                            developerUrl.append("/ota");
                            NetworkConfig.IUNIOS_DEVELOPER_HOST = developerUrl.toString();
                        }else {
                       	    NetworkConfig.IUNIOS_NORMAL_TEST = childName; //±£´æ°µÃÅ²ÎÊý
                       	    environment = EnvironmentConfig.NORMAL_ENVIRONMENT_TEST_VERSION;
                       	    return environment;
                       }
                    }
                }
            }
            if (developerTest) {
                environment = EnvironmentConfig.TEST_ENVIRONMENT_DEVELOPER;
            }
            else if (dirNormalTestVersion.exists()) {
                environment = EnvironmentConfig.NORMAL_ENVIRONMENT_TEST_VERSION;
            } else if (dirTestTestVersion.exists()) {
                environment = EnvironmentConfig.TEST_ENVIRONMENT_TEST_VERSION;
            } else {
                environment = EnvironmentConfig.TEST_ENVIRONMENT_NORMAL_VERSION;
            }
        }

        LogUtils.logd(TAG, "getEnvironment() environment = " + environment);
        return environment;
    }

    private static boolean isIp(String ip) {
        boolean isIp = false;
        if (TextUtils.isEmpty(ip)) {
            return false;
        }
        String ipInternal = ip.trim();
        if (ipInternal.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = ipInternal.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            isIp = true;
        }
        return isIp;
    }

    /**
     * is in test environment
     * 
     * @return
     */
    public static boolean isTestEnvironment() {
        int environment = getEnvironment();
        if (environment == EnvironmentConfig.TEST_ENVIRONMENT_NORMAL_VERSION
                || environment == EnvironmentConfig.TEST_ENVIRONMENT_TEST_VERSION
                || environment == EnvironmentConfig.TEST_ENVIRONMENT_DEVELOPER) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * when catch the NoSpaceException , you can use this method to get the
     * right error code
     * @param NoSpaceException
     * @return if /mnt/sdcard or /storage/sdcard0 has bo space , @see
     * Error.ERROR_CODE_STORAGE_NO_SPACE if /mnt/sdcard2 or /storage/sdcard1 has
     * no space, @see Error.ERROR_CODE_INTERNAL_STORAGE_NO_SPACE
     */
    public static int getErrorCodeWhenNoSpaceException(NoSpaceException exception) {
        if (exception.isFileExists()) {
            if (StorageUtil.getExternalStoragePath().equals(exception.getStoragePath())) {
                return Error.ERROR_CODE_STORAGE_NO_SPACE;
            } else {
                return Error.ERROR_CODE_INTERNAL_STORAGE_NO_SPACE;
            }
        } else {
            return Error.ERROR_CODE_STORAGE_NO_SPACE;
        }
    }

    /**
     * get current thread id
     * 
     * @return thread id
     */
    public static long getThreadId() {
        return Thread.currentThread().getId();
    }

    public static void sendUpgradBroadcast(final Context context, final File upgradeFile) {
        /*
         * Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
         * intent.putExtra("OTAUpdate", true); if (Util.isTestEnvironment()) {
         * intent.putExtra("mode", "test"); } intent.putExtra("FileName",
         * createFileNameInIntent(context, upgradeFile));
         * context.sendBroadcast(intent);
         */

        Slog.w(TAG, "!!! OTA UPGRADE !!!");
        // The reboot call is blocking, so we need to do it on another thread.
        Thread thr = new Thread("Reboot") {
            @Override
            public void run() {
                try {
                    // RecoverySystem.installPackage(context, upgradeFile);
                    GnRecoverySystem.gn_installPackage(context,
                            createFileNameInIntent(context, upgradeFile), null);
                    Log.wtf(TAG, "Still running after ota upgrade?!");
                } catch (IOException e) {
                    Slog.e(TAG, "Can't perform master clear/ota upgrade", e);
                }
            }
        };
        thr.start();
        return;
    }

    private static String createFileNameInIntent(Context context, File upgradeFile) {
        if (upgradeFile == null) {
            LogUtils.loge(TAG, "createFileNameInIntent() upgradeFile is null");
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getStorageNameForRecoverry(context, upgradeFile));
        stringBuffer.append(getFilePathWithoutStoragePath(upgradeFile));
        String fileName = stringBuffer.toString();
        LogUtils.logd(TAG, "getFileNameInIntent() fileName = " + fileName);
        return fileName;
    }

    private static String getFilePathWithoutStoragePath(File upgradeFile) {
        String storage = StorageUtil.getStroageOfFile(upgradeFile);
        String filePath = upgradeFile.getPath();
        filePath = filePath.substring(storage.length());
        LogUtils.logd(TAG, "getFilePathWithoutStoragePath() filePath = " + filePath);
        return filePath;
    }

    private static String getStorageNameForRecoverry(Context context, File file) {
        if (StorageUtil.isFileInInternalStoarge(context, file)) {
        	Log.d("988", "isExternalStorageEmulated :  " + Environment.isExternalStorageEmulated());
        	if(Environment.isExternalStorageEmulated()){
           // return "/sdcard";
	        	String primaryStoragePath = Environment.getExternalStorageDirectory().getPath();
	        	try {
	                Class policyClass = Class.forName("android.os.Environment");
	                try {
	                    Method getPath = policyClass.getMethod("getMediaStorageDirectory", null);
	                    File path = (File) getPath.invoke(null, (Object[]) null);
	                    Log.d("111", "getPath : " + path);
	                    primaryStoragePath = String.valueOf(path);
	                    return primaryStoragePath;
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
        	}
        	return "/sdcard";
        } else {
            return "/sdcard2";
        }

    }

    public static int getDownloadProgress(int downloadSize, int totalSize) {
        return downloadSize / (totalSize / 100);
    }

    public static String getModel() {
    	String modelString = SystemProperties.get("ro.gn.iuniznvernumber");
    	if(modelString == null) return "Unknow";
    	String array [] = modelString.split("-");
    	if(array.length > 1){
    		return array[1];
    	}else {
    		return "Unknow";
    	}
        //return SystemProperties.get("ro.product.model");
    }

    public static String getAndroidVersion() {
        return SystemProperties.get("ro.build.version.release");
    }

    public static String getGioneeVersion() {
        return SystemProperties.get("ro.gn.iuniznvernumber");
    }

    public static String getBuildTime() {
        return SystemProperties.get("ro.build.date.utc", "0");
    }

    public static String getfingerPrint() {
        return SystemProperties.get("ro.build.fingerprint", "");
    }

    public static boolean isGeminiSupport() {
        return ("true".equals(SystemProperties.get("ro.mediatek.gemini_support")))
                || ("dsda".equals(SystemProperties.get("persist.multisim.config")))
                || ("dsds".equals(SystemProperties.get("persist.multisim.config")));
    }

    public static boolean isMtkPlatform() {
        return SystemProperties.get("ro.mediatek.version.release") != null
                && !SystemProperties.get("ro.mediatek.version.release").equals("");
    }
    /*
     * 是否是海外版本
     */
    public static boolean isAbroadVersion() {
    	 RegionXMLParser parser;  
 	    List<com.aurora.ota.config.Config> configs;  
 	    int region = -1;
     	File configFile = new File(EnvironmentConfig.GIONEE_OTA_CONFIG_FILE_NAME);
     	if(configFile.exists()){
     			if(configFile.isFile()){
     				 try {  
     	                    InputStream is = new FileInputStream(configFile);  

     	                    parser = new RegionXMLParser();
     	                    configs = parser.parse(is);  //解析输入流  
     	                    for (com.aurora.ota.config.Config config : configs) {  
     	                    	region = config.getRegion();
     	                    }  
     	                    
     	                    if(region == 1){
     	                    	 return true;
     	                    }
     	                    
     	                } catch (Exception e) {  
     	                   LogUtils.loge(TAG,"parse the config_file exception");
     	                }  
     			}
     	}
     	return false;
    }
  
        
}
