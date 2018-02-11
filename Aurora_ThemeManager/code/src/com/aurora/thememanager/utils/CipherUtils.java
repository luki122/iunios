package com.aurora.thememanager.utils;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.Charset;
import java.security.Key;
import java.util.Arrays;

import android.text.TextUtils;
import android.util.Base64;

/**
 * @author jason zhang
 */
public class CipherUtils {

    public static final String CIPHER_ALG = "AES";
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int KEY_LENGTH = 128/8;

    public static String decrypt(String cipherText, String key) {
    	
        if(cipherText == null) {
            throw new IllegalArgumentException("must provide valid plain text");
        }
    	
        if(TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("must provide a key for decryption");
        }
        if(key.length() < KEY_LENGTH) {
            throw new IllegalArgumentException("key must at least " + KEY_LENGTH + " characters");
        }
        try {
            byte[] keyBytes = Arrays.copyOfRange(key.getBytes(CHARSET), 0, KEY_LENGTH);

            Key keySpec = new SecretKeySpec(keyBytes, CIPHER_ALG);

            Cipher cipher = Cipher.getInstance(CIPHER_ALG);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] plainBytes = cipher.doFinal(Base64.decode(cipherText,Base64.DEFAULT));
            String plainText = new String(plainBytes, CHARSET);

            return plainText;
        }catch(Exception e) {
            throw new RuntimeException("decrypt cipher text error", e);
        }
    }

    public static String encrypt(String plainText, String key) {
        if(plainText == null) {
            throw new IllegalArgumentException("must provide valid plain text");
        }
        if(TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("must provide a key for encryption");
        }
        if(key.length() < KEY_LENGTH) {
            throw new IllegalArgumentException("key must at least " + KEY_LENGTH + " characters");
        }
        try {
            byte[] keyBytes = Arrays.copyOfRange(key.getBytes(CHARSET), 0, KEY_LENGTH);

            Key keySpec = new SecretKeySpec(keyBytes, CIPHER_ALG);

            Cipher cipher = Cipher.getInstance(CIPHER_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(CHARSET));
            String cipherText = Base64.encodeToString(cipherBytes,Base64.DEFAULT);

            return cipherText;
        }catch(Exception e) {
            throw new RuntimeException("encrypt plain text error", e);
        }
    }
}