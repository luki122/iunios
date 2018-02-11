package com.android.gallery3d.xcloudalbum.tools;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	public static void showTast(Context context, int rId) {
		Toast.makeText(context, context.getString(rId), Toast.LENGTH_SHORT)
				.show();
	}

	public static void showTast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
