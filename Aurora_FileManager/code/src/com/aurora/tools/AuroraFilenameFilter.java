package com.aurora.tools;

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
		if(file.isHidden()){
			return true;
		}
		return false;
	}
	
	private boolean isBusy(File file){
		if(file.canRead()){
			return false;
		}
		return true;
	}
	@Override
	public boolean accept(File dir, String filename) {
		dir = new File(dir, filename);
//		return true;
		return !isHideFileOrDir(dir)&&!isBusy(dir);
	}
	
	

}
