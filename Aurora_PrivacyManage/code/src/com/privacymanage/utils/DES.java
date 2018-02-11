package com.privacymanage.utils;

import javax.crypto.Cipher; 
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class DES { 
    private static byte[] iv = {6,5,56,7,8,3,27,34};  
    private static final String keystr = "87ea0cee";
    /**
     * 加密
     * @param encryptString
     * @return
     */
    public static String encryptDES(String encryptString){   
    	try{
            IvParameterSpec zeroIv = new IvParameterSpec(iv);  
            SecretKeySpec key = new SecretKeySpec(keystr.getBytes(), "DES");  
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");  
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);  
            byte[] encryptedData = cipher.doFinal(encryptString.getBytes("UTF8"));  
            return new String(Base64.encodeToByte(encryptedData),"UTF8");
    	}catch(Exception e){
    		e.printStackTrace();
    		return encryptString;
    	}
    }  
    
    /**
     * 解密
     * @param decryptString
     * @return
     */
    public static String decryptDES(String decryptString){  
        try{
            byte[] byteMi = Base64.decodeFromByte(decryptString.getBytes("UTF8"));
            IvParameterSpec zeroIv = new IvParameterSpec(iv);   
            SecretKeySpec key = new SecretKeySpec(keystr.getBytes("UTF8"), "DES");  
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");  
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);  
            byte decryptedData[] = cipher.doFinal(byteMi);        
            return new String(decryptedData); 
    	}catch(Exception e){
    		e.printStackTrace();
    		return decryptString;
    	}
    }  
} 