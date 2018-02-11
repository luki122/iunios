/**
 * 
 */
package com.android.keyguard.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author vulcan
 *
 */
public class Trace {

	public static String StackToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
}
