/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * package-level logging flag
 */

package com.gionee.calendar.view;

public class Log {

	private static String LOGTAG = "GN_Calendar";

	public static boolean LOGV = true;
	private static boolean LOGD = true;
	private static boolean LOGI = true;
	private static boolean LOGE = true;
	private static boolean LOGWTF = true;

	/**
	 * Log Verbose
	 * @param msg
	 */
	public static void v(String msg) {
		if(LOGV){ android.util.Log.v(LOGTAG, msg); }
	}

	/**
	 * Log debug
	 * @param msg
	 */
	public static void d(String msg){
		if(LOGD){ android.util.Log.d(LOGTAG, msg); }
	}

	/**
	 * Log info
	 * @param msg
	 */
	public static void i(String msg) {
		if(LOGI){ android.util.Log.i(LOGTAG, msg); }
	}

	/**
	 * Log error
	 * @param msg
	 */
	public static void e(String msg) {
		if(LOGE){ android.util.Log.e(LOGTAG, msg); }
	}

	/**
	 * Log error
	 * @param msg
	 * @param ex
	 */
	public static void e(String msg, Exception ex) {
		if(LOGE){ android.util.Log.e(LOGTAG, msg, ex); }
	}

	/**
	 * Log Terrible Failure
	 * @param msg
	 */
	public static void wtf(String msg) {
		if(LOGWTF){ android.util.Log.wtf(LOGTAG, msg); }
	}

	// getters and setters belows
	/**
	 * LOGTAG
	 */
	public static String getLOGTAG() {
		return LOGTAG;
	}

	public static void setLOGTAG(String lOGTAG) {
		LOGTAG = lOGTAG;
	}

	/**
	 * LOGV
	 * @return
	 */
	public static boolean isLOGV() {
		return LOGV;
	}

	public static void setLOGV(boolean lOGV) {
		LOGV = lOGV;
	}

	/**
	 * LOGD
	 * @return
	 */
	public static boolean isLOGD() {
		return LOGD;
	}

	public static void setLOGD(boolean lOGD) {
		LOGD = lOGD;
	}

	/**
	 * LOGI
	 * @return
	 */
	public static boolean isLOGI() {
		return LOGI;
	}

	public static void setLOGI(boolean lOGI) {
		LOGI = lOGI;
	}

	/**
	 * LOGE
	 * @return
	 */
	public static boolean isLOGE() {
		return LOGE;
	}

	public static void setLOGE(boolean lOGE) {
		LOGE = lOGE;
	}

	/**
	 * LOGWTF
	 * @return
	 */
	public static boolean isLOGWTF() {
		return LOGWTF;
	}

	public static void setLOGWTF(boolean lOGWTF) {
		LOGWTF = lOGWTF;
	}

}
