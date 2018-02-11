package com.aurora.change.utils;


public class FileManager {

	public static String getSaveFilePath() {
		if (CommonUtil.hasSDCard()) {
			return CommonUtil.getRootFilePath() + "aurora/files/";
		} else {
			return CommonUtil.getRootFilePath() + "aurora/files/";
		}
	}
}
