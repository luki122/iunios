
package com.aurora.community.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密工具类
 * @author JimXia
 * 2014-9-25 下午3:39:35
 */
public class EncryptUtil {
    private final static String TAG="EncryptUtil";
    
    /**
     * MD5 加密
     */
    public static byte[] encryptMD5(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        return md5.digest();
    }
    /**
     * MD5 加密 32位加密，不要改动，其他地方有根据长度判断的代码
     * @param str 待加密字符串
     * @return 返回空子表示加密失败
     */
    public static String getMD5Str(String str) {  
        MessageDigest messageDigest = null;  
        StringBuilder md5StrBuff = new StringBuilder("");
        try {
            messageDigest = MessageDigest.getInstance("MD5");  
            messageDigest.reset();  
            messageDigest.update(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            messageDigest = null;
        } catch (UnsupportedEncodingException e) {
            messageDigest = null;
        }    
        if(messageDigest!=null){
            byte[] byteArray = messageDigest.digest(); 
            for (int i = 0; i < byteArray.length; i++) {              
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
                else  
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
            }
        }  
        return md5StrBuff.toString();  
    }

    /**
     * SHA 加密
     * @throws NoSuchAlgorithmException 
     */
    public static String encryptSHA(String encryptStr) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA");
        sha.update(encryptStr.getBytes());
        return toHex(sha.digest());
    }
    /**
     * 获取HmacSHA256加密的密文
     * @param data 待加密数据
     * @param key 加密key
     * @return 密文，""为加密失败
     */
    public static String getHmacSHA256(String data,String key){
        String result="";
        try {
            SecretKeySpec secretKeySpec=new SecretKeySpec(key.getBytes(), "HmacSHA256");
            Mac mac=Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            result=toHex(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            FileLog.e(TAG, e.getMessage());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            FileLog.e(TAG, e.getMessage());
        }
        return result;
    }

    // /** *//** 取得HMAC密钥 */
    // public static String getMacKey() throws Exception {
    // KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5");
    // SecretKey secretKey = keyGenerator.generateKey();
    // return new Base64Encoder().encode(secretKey.getEncoded());
    // }
    //    
    // /** *//** 执行加密 */
    // public static byte[] encryptHMAC(byte[] data, String key) throws
    // Exception {
    // byte[] bkey = new BASE64Decoder().decodeBuffer(key);
    // SecretKey secretKey = new SecretKeySpec(bkey, "HmacMD5");
    // Mac mac = Mac.getInstance(secretKey.getAlgorithm());
    // mac.init(secretKey);
    // return mac.doFinal(data);
    // }

    private static String toHex(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 3);
        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 0x0f, 16));
        }
        return sb.toString();
    }
}
