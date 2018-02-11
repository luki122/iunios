package com.privacymanage.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	/**
	 * 字符串是否为空
	 */
	public static boolean isEmpty(String s) {
		if (s == null || s.length() == 0 || s.equals("null")) {
			return true;
		}
		return false;
	}

	/**
	 * 字符串不为空
	 * */
	public static boolean notEmpty(final String str) {
		return !isEmpty(str);
	}
		
	/**
	 * 字符串转换成十六进制字符串
	 */
	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString();
	}

	/**
	 * 把16进制字符串转换成字节数组
	 * 
	 * @param hexString
	 * @return byte[]
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	// ASCII字符串转换为十六进制字节数组
	public static byte[] convert2HexArray(String apdu) {
		int len = apdu.length() / 2;
		char[] chars = apdu.toCharArray();
		String[] hexes = new String[len];
		byte[] bytes = new byte[len];
		for (int i = 0, j = 0; j < len; i = i + 2, j++) {
			hexes[j] = "" + chars[i] + chars[i + 1];
			bytes[j] = (byte) Integer.parseInt(hexes[j], 16);
		}
		return bytes;
	}

	public static String str_replace(String strSource, String strFrom,
			String strTo) {
		if (strSource == null) {
			return null;
		}
		int i = 0;
		if ((i = strSource.indexOf(strFrom, i)) >= 0) {
			char[] cSrc = strSource.toCharArray();
			char[] cTo = strTo.toCharArray();
			int len = strFrom.length();
			StringBuffer buf = new StringBuffer(cSrc.length);
			buf.append(cSrc, 0, i).append(cTo);
			i += len;
			int j = i;
			while ((i = strSource.indexOf(strFrom, i)) > 0) {
				buf.append(cSrc, j, i - j).append(cTo);
				i += len;
				j = i;
			}
			buf.append(cSrc, j, cSrc.length - j);
			return buf.toString();
		}
		return strSource;
	}

	public static String[] StringToArray(String splitStr, String sourceStr,
			String end) {
		String[] targetStr = new String[sourceStr.length()];
		int i = 0;
		int j = 0;
		while ((i = sourceStr.indexOf(splitStr)) >= 0) {
			targetStr[j] = sourceStr.substring(0, i) + end;
			sourceStr = sourceStr.substring(i + splitStr.length(), sourceStr
					.length());
			j++;
		}
		if (sourceStr.length() > 0) {
			targetStr[j] = sourceStr + end;
		}
		return targetStr;
	}
	
	/**
	 * 以@为分割符，将字符串拆分成字符串list 例如：aaaaa@bbbbb@cccc
	 * @param str
	 * @return
	 */
	public static List<String> splitStrByat(String str){
		List<String> strList = new ArrayList<String>();
		if(str == null){
			return null;
		}
		
		while(true){
			int index = str.indexOf("@");
			if(index == -1){
				strList.add(str);
				break;
			}else{
				strList.add(str.substring(0,index));
				str = str.substring(index+1);
			}
		}		
		return strList;		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月8日 下午5:08:47 .
	 * @param email
	 * @return
	 */
	public static boolean emailIsInvalid(String email) {
		/*
		String regEx =
		    	"^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
		    	    +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
		    	    +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
		    	    +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
		    	    +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
		    	    +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
		*/
		String regEx = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		
		String trimedEmail = email.trim();
		Matcher matcherObj = Pattern.compile(regEx).matcher(trimedEmail);
		return !matcherObj.matches();
	}
}
