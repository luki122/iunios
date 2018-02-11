package com.android.gallery3d.local.tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.storage.StorageManager;

public class StorageUtils {

	public static List<String> getAllStorages(boolean isMount, Context mContext) {
		List<String> storages = new ArrayList<String>();
		List<String> mounts = new ArrayList<String>();
		StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
		try {
			Method method = mStorageManager.getClass().getMethod("getVolumeList");
			Object[] object = (Object[]) method.invoke(mStorageManager);
			for (int i = 0; i < object.length; i++) {
				Method getPathMethod = object[i].getClass().getMethod("getPath");
				String path = (String) getPathMethod.invoke(object[i]);
				if (isMount(path, mContext)) {
					mounts.add(path);
				}
				storages.add(path);
			}
			if (isMount) {
				storages.clear();
				return mounts;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		mounts.clear();
		return storages;
	}

	public static boolean isMount(String path, Context mContext) {
		StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
		try {
			Method method = mStorageManager.getClass().getMethod("getVolumeState", String.class);
			String mount = (String) method.invoke(mStorageManager, path);
			if (mount.equals(android.os.Environment.MEDIA_MOUNTED)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public static int getIntPref(Context context, String name, int def) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return prefs.getInt(name, def);
	}

	public static void setIntPref(Context context, String name, int value) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(name, value);
		SharedPreferencesCompat.apply(ed);
	}

}
