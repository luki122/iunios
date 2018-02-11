/**
 * Copyright © 2009-2013 Feidee.All Rights Reserved
 */
package com.aurora.iunivoice.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 布尔型的配置存储，采用长整型64位与的方法
 * 
 * @author JimXia 2014-7-29 下午4:21:00
 */
@SuppressLint("CommitPrefEdits")
public final class BooleanPreferencesUtil {
	private final SharedPreferences sp;
	private final SharedPreferences.Editor editor;

	private static BooleanPreferencesUtil sInstance;

	private final static String KEY_FIRST_GROUP = "first_group";

	private final static long MASK_BASE = 0x4000000000000000L; // 最多只能往右移62位
	private final static long MASK_HAS_LOGIN = MASK_BASE >> 0; // 用户有没有登录

	public static BooleanPreferencesUtil getInstance(Context context) {
		if (sInstance == null) {
			synchronized (BooleanPreferencesUtil.class) {
				if (sInstance == null) {
					sInstance = new BooleanPreferencesUtil(context);
				}
			}
		}

		return sInstance;
	}

	private BooleanPreferencesUtil(Context context) {
		sp = context.getSharedPreferences("boolean_prefences",
				Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	public boolean hasLogin() {
		return (getFirstGroup() & MASK_HAS_LOGIN) == MASK_HAS_LOGIN;
	}

	public void setLogin(boolean hasLogin) {
		long value = getFirstGroup();
		if (hasLogin) {
			value |= MASK_HAS_LOGIN;
		} else {
			value &= (~MASK_HAS_LOGIN);
		}
		putFirstGroup(value);
	}

	private long getFirstGroup() {
		return sp.getLong(KEY_FIRST_GROUP, 0L);
	}

	private synchronized void putFirstGroup(long value) {
		editor.putLong(KEY_FIRST_GROUP, value);
		editor.commit();
	}
	
}