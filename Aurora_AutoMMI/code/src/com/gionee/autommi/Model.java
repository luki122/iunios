package com.gionee.autommi;

import android.os.SystemProperties;

public class Model {
	private String mModel;

	private static Model instance = null;

	public static Model getInstance() {
		if (instance == null) {
			instance = new Model();
		}
		return instance;
	}

	private Model() {
		String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
		String[] strName = deviceName.split("-");
		if (strName.length > 1) {
			mModel = strName[1];
		} else {
			mModel = "";
		}
	}

	public boolean isU3() {
		if (mModel.equals("U3")) {
			return true;
		}
		return false;
	}

	public boolean isI1() {
		if (mModel.equals("i1")) {
			return true;
		}
		return false;
	}

	public boolean isContainsU2() {
		if (mModel.contains("U2")) {
			return true;
		}
		return false;
	}
 
	public boolean isU2() {
		if (mModel.equals("U2")) {
			return true;
		}
		return false;
	}
}
