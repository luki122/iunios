package com.aurora.community.utils;

import java.text.DecimalFormat;


public class FormatUtil {
	private static final long _1K = 1024; // 1k
	private static final long _1M = _1K * 1024; // 1M
	private static final long _1G = _1M * 1024; // 1G
	
	public static String formatStorageSize(long byteSize) {
		final DecimalFormat df = new DecimalFormat("#.#");
		if (byteSize >= _1G) {
			// 用G做单位
			return df.format(byteSize * 1.0d / _1G) + Globals.GB;
		} else if (byteSize >= _1M) {
			// 用M做单位
			return df.format(byteSize * 1.0d / _1M) + Globals.MB;
		} else if (byteSize >= _1K) {
			// 用K做单位
			return df.format(byteSize * 1.0d / _1K) + Globals.KB;
		} else {
			// 用B做单位
			return byteSize + Globals.B;
		}
	}
}
