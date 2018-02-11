package com.android.auroramusic.db;

public class AuroraMusicInfo {

	protected String lrcPath;
	protected String picPath;
	
	public AuroraMusicInfo(String lrcPath, String picPath) {
		this.lrcPath = lrcPath;
		this.picPath = picPath;
	}

	public String getLrcPath() {
		return lrcPath;
	}

	public String getPicPath() {
		return picPath;
	}

	@Override
	public String toString() {
		return "AuroraMusicInfo [lrcPath=" + lrcPath + ", picPath=" + picPath
				+ "]";
	}

	
}
