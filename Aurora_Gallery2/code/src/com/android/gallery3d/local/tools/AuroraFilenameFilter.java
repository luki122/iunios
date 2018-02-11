package com.android.gallery3d.local.tools;

import java.io.File;
import java.io.FilenameFilter;

public class AuroraFilenameFilter implements FilenameFilter {
	
	private static AuroraFilenameFilter inStance;

	public static AuroraFilenameFilter getInstance() {
		if (inStance == null) {
			inStance = new AuroraFilenameFilter();
		}
		return inStance;
	}

	private boolean isHideFileOrDir(File file){
		if(file.isHidden()||file.isFile()){
			return true;
		}
		return false;
	}

	private boolean is100ANDRO(File file){
		if(file.getName().equals("100ANDRO")||file.getName().equals("Camera")||file.getName().equals("cloud")){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean accept(File dir, String filename) {
		dir = new File(dir, filename);
		return !isHideFileOrDir(dir)&&!is100ANDRO(dir);
	}
	
	

}
