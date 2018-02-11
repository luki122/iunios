package com.secure.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import android.os.Environment;
import android.util.Log;

public class LogUtils {
	private static Boolean isLogCatPrint = true;
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss:SSS");

	public static void printWithLogCat(Boolean flag, String filter,
			String content) {
		if (flag) {
			Log.i(filter, sdf.format(System.currentTimeMillis()) + " ---- "
					+ content);
		}
	}

	public static void printWithLogCat(String filter, String content) {
		printWithLogCat(isLogCatPrint, filter, content);
	}

	public static void setLogOnOff(boolean flag) {
		isLogCatPrint = flag;
	}

	/**
	 * 将日志信息直接写入到sd卡中
	 * 
	 * @param message
	 */
	public static void writeFile(String message) {
		String path = Environment.getExternalStorageDirectory().getPath();
		String fileName = path + File.separator + "huangbinlog";
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				FileOutputStream out = new FileOutputStream(fileName);
				String strText = "log begin:";
				byte[] bytes = strText.getBytes();
				out.write(bytes);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(message);
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Vulcan created this method in 2015年1月7日 下午4:09:02 .
	 * @param t
	 * @return
	 */
	public static String StackToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
}
