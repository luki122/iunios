package com.aurora.tools;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisUtils {

	

	public static void dumpViewTree(ViewGroup root) {
		dumpViewTree(root, "");
	}

	private static void dumpViewTree(ViewGroup g, String prefix) {
		Log.i("JXH",
				prefix + "VIEWGROUP:" + g + "childCount=" + g.getChildCount());
		final String childPrefix = prefix + "  ";
		for (int i = 0; i < g.getChildCount(); i++) {
			final View child = g.getChildAt(i);
			if (child instanceof ViewGroup) {
				dumpViewTree((ViewGroup) child, childPrefix);
			} else {
				Log.i("JXH", childPrefix + " CHILD #" + i + " :" + child);
			}
		}
	}

	/**
	 * throw Exception check StackTrace just for test
	 * 
	 * @param msg
	 */
	public static void throwException(String msg) {
		try {
			throw new Exception(msg+" just for test");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
