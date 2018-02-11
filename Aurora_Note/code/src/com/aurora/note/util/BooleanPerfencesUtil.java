/**
 * Copyright © 2009-2013 Feidee.All Rights Reserved
 */
package com.aurora.note.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.aurora.note.NoteApp;

/**
 * 布尔型的配置存储，采用长整型64位与的方法
 * @author JimXia
 * 2014-7-29 下午4:21:00
 */
public final class BooleanPerfencesUtil {

	private final static SharedPreferences sp = NoteApp.ysApp.getSharedPreferences("boolean_prefences", Context.MODE_PRIVATE);
    private final static SharedPreferences.Editor editor = sp.edit();

    private final static String KEY_FIRST_GROUP = "first_group";

    private final static long MASK_BASE = 0x4000000000000000L; // 最多只能往右移62位
    private final static long MASK_FIRST_ENTER_QUICK_RECORD = MASK_BASE >> 0; // 第一次使用快速录音功能
    private final static long MASK_AUTO_INDENT = MASK_BASE >> 1; // 是否打开首行缩进

    public static boolean isFirstTimeUseQuickRecord(){
    	return (getFirstGroup() & MASK_FIRST_ENTER_QUICK_RECORD) == 0;
    }

    public static void markFirstTimeUseQuickRecord(){
    	long value = getFirstGroup() | MASK_FIRST_ENTER_QUICK_RECORD;
    	putFirstGroup(value);
    }
    
    public static boolean isAutoIndent(){
        return (getFirstGroup() & MASK_AUTO_INDENT) == MASK_AUTO_INDENT;
    }

    public static void setAutoIndent(boolean autoIndent) {
        long value;
        if (autoIndent) {
            value = getFirstGroup() | MASK_AUTO_INDENT;
        } else {
            value = getFirstGroup() & (~MASK_AUTO_INDENT);
        }
        putFirstGroup(value);
    }

    private static long getFirstGroup(){
    	return sp.getLong(KEY_FIRST_GROUP, 0L);
    }

    private static void putFirstGroup(long value){
    	editor.putLong(KEY_FIRST_GROUP, value);
    	editor.commit();
    }

}