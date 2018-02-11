package com.android.gallery3d.local.tools;

import java.io.File;
import java.io.FilenameFilter;

public class AuroraAlbumsFilenameFilter implements FilenameFilter {

	private static AuroraAlbumsFilenameFilter inStance;

	public static AuroraAlbumsFilenameFilter getInstance() {
		if (inStance == null) {
			inStance = new AuroraAlbumsFilenameFilter();
		}
		return inStance;
	}

	private boolean isHideFileOrDir(File file) {
		if (file.isHidden() || file.isDirectory()) {
			return true;
		}
		return false;
	}

	private boolean isImageFile(File file) {// jpg jpeg gif png bmp wbmp webp
		String end = file.getAbsolutePath().toLowerCase();
		if (end.endsWith("jpg") || end.endsWith("jpeg") || end.endsWith("gif") || end.endsWith("png") || end.endsWith("bmp") || end.endsWith("wbmp") || end.endsWith("webp")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean accept(File dir, String filename) {
		dir = new File(dir, filename);
		return !isHideFileOrDir(dir)&&isImageFile(dir);
	}

}
