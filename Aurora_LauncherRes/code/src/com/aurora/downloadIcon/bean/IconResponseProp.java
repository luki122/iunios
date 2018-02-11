package com.aurora.downloadIcon.bean;

import android.R.integer;

public class IconResponseProp {
	private String packageName;
	private String className;
	private String label;
	private String resolution;
	private String version;
	private String path;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "{\"packageName\":\"" + packageName
				+ "\", \"className\":\"" + className + "\", \"label\":\"" + label
				+ "\", \"resolution\":\"" + resolution + "\", \"version\":\"" + version
				+ "\", \"path\":\"" + path + "\"}";
	}

	public boolean equals(IconResponseProp another) {
		boolean returnBool=true;
		if ((this.packageName != null && this.packageName
				.equals(another.packageName))
				&& (this.className != null && this.className
						.equals(another.className))
				&& (this.version != null && this.version
						.equals(another.version))
				&& (this.resolution != null && this.resolution
						.equals(another.resolution))
				&& (this.label != null && this.label.equals(another.label))
				&& (this.path != null && this.path.equals(another.path))) {

		} else {
			returnBool = false;
		}
		return returnBool;
	}

}
