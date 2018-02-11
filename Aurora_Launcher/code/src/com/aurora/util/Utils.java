package com.aurora.util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;

import com.aurora.util.HanziToPinyin.Token;

public class Utils {
	/**
	 * @param displayName
	 * @return
	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ*
	 */
	public static String[] getFullPinYin(String displayName) {
		if(displayName == null) return null;
        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(displayName);
        if (tokens != null && tokens.size() > 0) {
            StringBuilder sb = new StringBuilder();
            StringBuilder sb_simple = new StringBuilder();
            for (Token token : tokens) {
                // Put Chinese character's pinyin, then proceed with the
                // character itself.
                if (Token.PINYIN == token.type) {
                    //if (sb.length() > 0) {
                    //    sb.append(' ');
                    //}
                    sb.append(token.target);
                    sb_simple.append(token.target.charAt(0));
                } else {
                    //if (sb.length() > 0) {
                    //    sb.append(' ');
                    //}
                    sb.append(token.source);
                    sb_simple.append(token.source.charAt(0));
                }
            }
            String[] sArray = new String[2];
            sArray[0] = sb.toString();
            sArray[1] = sb_simple.toString();
            return sArray;
        }
        return null;
    }
	
	/**
	 * 返回的拼音为大写字母
	 * @param str
	 * @return
	 */
	public static String getFirstPinYin(String str){
		char letter = '#';
		StringBuffer buffer=new StringBuffer();
		
		if(str!=null && !str.equals("")){
			char[] cc=str.toCharArray();
			for(int i=0;i<cc.length;i++){
				ArrayList<Token> mArrayList = HanziToPinyin.getInstance().get(String.valueOf(cc[i]));
				if(mArrayList.size() > 0 ){
					String n = mArrayList.get(0).target;
					buffer.append(n);
				}
			}
		}
		String spellStr = buffer.toString().toUpperCase();
//		String spellStr = buffer.toString();
		if(spellStr.length() > 0){
			letter = spellStr.charAt(0);
			if(!(letter>='A' && letter<='Z')){
				letter = '#';
			}
		} 
		return String.valueOf(letter);
	}
	
	
	public static int compare(String s1, String s2) {
    	Collator collator = ((java.text.RuleBasedCollator)java.text.Collator.
    	    	getInstance(java.util.Locale.ENGLISH));
    	return collator.compare(s1, s2);
    }
}
