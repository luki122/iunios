package com.aurora.ota.reporter;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.youju.statistics.util.Utils;

import gn.com.android.update.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.os.SystemProperties;

public class APKinfoUtil {
    private static final String TAG = "APKinfoUtil";
    private static final String CHANNEL_PATH = "/system/iuni/channel.xml";
    private Context mContext;
    public APKinfoUtil(Context context){
        this.mContext = context;
    }
    
    public static String getCurrentVersion(Context context){
        Log.d(TAG, "getCurrentVersion()");
        try { 
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int curVersionCode =info.versionCode ;
            String curVersionName = info.versionName;
            Log.d(TAG, "curVersionCode = " + curVersionCode + ", curVersionName = " + curVersionName);
            return curVersionName;
        } catch (NameNotFoundException e) {    
            e.printStackTrace(System.err);
        }
        return null; 
    }
    
    public static String getMobilModel(){
       // return android.os.Build.MODEL;
    	return Util.getModel();
    }
    
    public static String getChanel2(Context ctx){
        String CHANNELID="IUNI OS";  
        File file = new File(CHANNEL_PATH);
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
            CHANNELID = parseChannel(builder.toString());
            Log.e("channel", parseChannel(builder.toString()));
        } catch (Exception e) {
            CHANNELID="IUNI OS";
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
        return CHANNELID;
    }
    public static String getChanel(Context ctx){  
        String CHANNELID="IUNI OS";  
        return getChanel2(ctx);  
    }

 static class ChannelHandler extends DefaultHandler{
        
        private boolean isChannel;
        private String channel;
        
        public String getChannel(){
            return channel;
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
            if(localName.equals("Channel")){
                isChannel = true;
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // TODO Auto-generated method stub
            if(localName.equals("Channel")){
                isChannel = false;
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(isChannel){
//                Log.e("channel", new String(ch, start, length));
                channel = new String(ch, start, length);
            }
            // TODO Auto-generated method stub
        }
        
        
        
    }

    private static String parseChannel(String xml) {
       String defaultChannel = "IUNI OS";
        if(TextUtils.isEmpty(xml)){
            return defaultChannel;
        }
        StringReader reader = new StringReader(xml);
        InputSource source = new InputSource(reader);
        try{
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            ChannelHandler handler = new ChannelHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(source);
            defaultChannel = handler.getChannel();
            
        }catch (Exception e) {
            // TODO: handle exception
            defaultChannel = "IUNI OS";
        }
        
       
        return defaultChannel;
    }
    
    public static String getRecoveryVersion(){
        return SystemProperties.get("com.iuni.recovery_version");
    }

    public static String getPhoneNumber(Context ctx) throws GeneralSecurityException {

        TelephonyManager tm = null;
        String key64 = "vCwEonZXUS2F1RFK2Suwjw==";


        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        String encryptResult = null;

        if (tm.getLine1Number() != null) {

            encryptResult = getEncryptValueByBase64(key64, tm.getLine1Number());

            return encryptResult;
        } else {

            return null;
        }
    }
    
    public static int getPhoneWidth(Activity context){
        DisplayMetrics m = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(m);
        
        return m.widthPixels;
    }
    
    public static int getPhoneHeight(Activity context){
        
        DisplayMetrics m = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(m);
        
        return m.heightPixels;
    }
    
    
    
    

    public static String getUniqueID(Context ctx) throws GeneralSecurityException {

        TelephonyManager tm = null;
        String key64 = "vCwEonZXUS2F1RFK2Suwjw==";


        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        String encryptResult = null;

        if (tm.getDeviceId() != null) {
            encryptResult = getEncryptValueByBase64(key64, tm.getDeviceId());

            return encryptResult;
        } else {
            encryptResult = "";
            return encryptResult;
        }
    }

    public static final String AES = "AES";

    /**
     * Key Generation 直接生成密钥的原生byte[]
     * 
     * @param seed
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] genAesKey(String seed) throws GeneralSecurityException {
        byte[] rawKeyData = null;

        SecureRandom sr = null;
        if (seed != null && !seed.equals("")) {
            sr = new SecureRandom(seed.getBytes());
        } else {
            sr = new SecureRandom();
        }
        KeyGenerator keyGen = KeyGenerator.getInstance(AES);
        keyGen.init(128, sr);
        SecretKey sKey = keyGen.generateKey();
        rawKeyData = sKey.getEncoded();

        return rawKeyData;
    }
    
    /**
     * 
     * @param rawKeyData
     * @param decryptData
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] encrypt(byte[] rawKeyData, byte[] decryptData) throws GeneralSecurityException {
        SecureRandom sr = new SecureRandom();
        SecretKeySpec key = new SecretKeySpec(rawKeyData, AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, key, sr);
        byte[] encryptedData = cipher.doFinal(decryptData);
        
        return encryptedData;
    }
    
    /**
     * Encrypt Value Return String
     * 使用Base64转换加密结果
     * 使用Base64转换Key
     * @param rawKeyData
     * @param encryptValue
     * @return String
     * @throws GeneralSecurityException
     */
    public static String getEncryptValueByBase64(String rawKeyData, String encryptValue) 
            throws GeneralSecurityException {
        return Base64encode(encrypt(Base64decode(rawKeyData), encryptValue.getBytes()));
    }
    
    
    /**
     * Key Generation
     * 使用BASE64转换byte[]
     * @param seed
     * @return
     * @throws GeneralSecurityException
     */
    public static String genKeyByBase64(String seed) 
            throws GeneralSecurityException {
        return Base64encode(genAesKey(seed));
    }

    public static String Base64encode(byte[] decodeValue) {
        return new String(base64.encode(decodeValue));
    }
    
    public static byte[] Base64decode(String encodeValue) {
        return base64.decode(encodeValue.getBytes());
    }

    
    
    private static final Base64 base64 = new Base64();


    public static String loadFileAsString(String filePath)
            throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    
    
    
    
    public static String getMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * public static String digest(String input) { try { MessageDigest md =
     * MessageDigest.getInstance("MD5"); return new BigInteger(1,
     * md.digest(input.getBytes())).toString(16) .toUpperCase(); } catch
     * (Exception e) { return null; } }
     */

    /**
     * 加密
     * 
     * @param content
     *            需要加密的内容
     * @param password
     *            加密密码
     * @return
     */
    public static byte[] encrypt(String content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
