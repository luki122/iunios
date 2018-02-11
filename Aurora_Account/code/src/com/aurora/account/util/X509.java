package com.aurora.account.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/** 
* @ClassName: X509
* @Description: 得到签名证书的sha1
* @author jason
* @date 2014年11月3日 下午4:47:56
* 
*/ 
public class X509 {

    public  void getsha1() {
    	FileInputStream is;
    	try {
    		is = new FileInputStream("/sdcard/platform.x509.pem");
    		CertificateFactory x509CertFact = CertificateFactory.getInstance("X.509");
    		X509Certificate cert = (X509Certificate)x509CertFact.generateCertificate(is);
    		String thumbprint = getThumbPrint(cert);
    		System.out.println(thumbprint);

    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (CertificateException e) {
    		e.printStackTrace();
    	} catch (NoSuchAlgorithmException e) {
    		e.printStackTrace();
    	}

    }

    public  String getThumbPrint(X509Certificate cert) 
    	throws NoSuchAlgorithmException, CertificateEncodingException {
    	MessageDigest md = MessageDigest.getInstance("SHA-1");
    	byte[] der = cert.getEncoded();
    	md.update(der);
    	byte[] digest = md.digest();
    	return hexify(digest);

    }

    public  String hexify (byte bytes[]) {

    	char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
    			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    	StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
        	buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

}