package com.aurora.note.activity.picbrowser;

import java.io.File;
import java.util.Comparator;

public class PictureInfo {

	private String imagePath;
	private boolean isSelected;

	public PictureInfo(String imagePath,boolean isSelected) {
		this.imagePath = imagePath;
		this.isSelected = isSelected;
	}

	public String getimagePath() {
		return imagePath;
	}

	public void setimagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	static class PictureInfoComparator implements Comparator<Object> {
		public int compare(Object o1,Object o2)  {
			PictureInfo pic1=(PictureInfo)o1;
			PictureInfo pic2=(PictureInfo)o2;
			long pic1Time = new File(pic1.imagePath).lastModified();
			long pic2Time = new File(pic2.imagePath).lastModified();
			int result = pic1Time < pic2Time ? 1 : -1;
			return result;
		}
	}

}