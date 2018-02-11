package com.aurora.account.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;



/**
 * @author yuanzhe(yuanzhe@baidu.com)
 * @date 2013-10-29 上午11:25:02
 */
public class SignatureUtil {
	
	public static String getSignCode(Context context, String pkgName){
		long start = System.currentTimeMillis();
		String signCode = "";
		try{
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
			Signature sign = packageInfo.signatures[0];
			signCode = parseSignature(sign.toByteArray());
			signCode = signCode.toLowerCase();
		}catch (Exception e){
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();

	//	ILogger log = LoggerFactory.getLogger("SignatureUtil");
		//log.d("getSignCode spend mills:" + (end - start));
		
		//log.i(pkgName + " signCode:" + signCode);
		Log.i("qianming", "zhangwei the signcode="+signCode);
		return signCode;
	}
	
	private static String parseSignature(byte[] signature) {  
        String sign = ""; 
        FileInputStream is;
        try {  
        	/*is = new FileInputStream("/sdcard/platform.x509.pem");*/
            CertificateFactory certFactory = CertificateFactory  
                    .getInstance("X.509");  
            X509Certificate cert = (X509Certificate) certFactory  
                    .generateCertificate(new ByteArrayInputStream(signature));  
           /* X509Certificate cert = (X509Certificate)certFactory.generateCertificate(is);*/
            String pubKey = cert.getPublicKey().toString();  
            String ss = subString(pubKey);  
            ss = ss.replace(",", "");  
            ss = ss.toLowerCase();  
            int aa = ss.indexOf("modulus");  
            int bb = ss.indexOf("publicexponent");  
            sign = ss.substring(aa + 8, bb);  
        } catch (Exception e) {  
            e.printStackTrace();
        }  
        return sign;  
    }
	
	private static String subString(String sub) {  
        Pattern pp = Pattern.compile("\\s*|\t|\r|\n");  
        Matcher mm = pp.matcher(sub);  
        return mm.replaceAll("");  
    }
}
