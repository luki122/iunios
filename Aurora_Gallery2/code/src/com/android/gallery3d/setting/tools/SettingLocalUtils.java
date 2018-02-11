package com.android.gallery3d.setting.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraActionBar;

import com.android.gallery3d.app.Log;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

public class SettingLocalUtils {
	private static final String TAG = "SettingLocalUtils";

	private static String filepath = "autoUploadPaths.xml";

//	/**
//	 * 隐藏键盘
//	 * @param context
//	 * @param v
//	 * @return
//	 */
//	public static Boolean hideInputMethod(Context context, View v) {
//		if (v == null) {
//			return false;
//		}
//		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (imm != null) {
//			return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//		}
//		return false;
//	}
//
//	/**
//	 * 强制打开键盘
//	 * @param context
//	 * @return
//	 */
//	public static boolean showInputMethod(Context context) {
//		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (imm != null) {
//
//			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
//		}
//		return false;
//	}
//
//	/**
//	 * 设置auroraActionBar title
//	 * @param title
//	 */
//	public static void setAuroraActionBarTitle(AuroraActionBar auroraActionBar, String title) {
//		if (auroraActionBar != null) {
//			auroraActionBar.setTitle(title);
//			auroraActionBar.getHomeButton().setVisibility(View.VISIBLE);
//			setAuroraActionBarBtn(auroraActionBar, true);
//		}
//	}
//
//	/**
//	 * 设置auroraActionBar 是否可用返回
//	 * @param enable
//	 *            true 可以返回 false 不能
//	 */
//	public static void setAuroraActionBarBtn(AuroraActionBar auroraActionBar, boolean enable) {
//		if (auroraActionBar != null) {
//			auroraActionBar.setDisplayHomeAsUpEnabled(enable);
//		}
//	}
//
//	public static boolean isWrongText(String name) {
//		String pstr = "[\u2600-\u27bf]+";  // 杂项符号 印刷符号
//		Pattern p = Pattern.compile(pstr);
//		Matcher m = p.matcher(name);
//		if (m.find()) {
//			return true;
//		}
//		if (name.contains("\\") || name.contains("/") || name.contains("?.:") || name.contains("*") || name.contains(".")) {
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 设置BottomBarMenu状态
//	 * @param auroraActionBar
//	 * @param position
//	 * @param use
//	 */
//	public static void setItemBottomBar(AuroraActionBar auroraActionBar, int position, boolean use) {
//		if (auroraActionBar != null) {
//			LogUtil.d(TAG, "---position:" + position + " use:" + use);
//			auroraActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(position, use);
//		}
//	}
//
//	/**
//	 * 设置BottomBarMenu是否显示
//	 * @param auroraActionBar
//	 * @param position
//	 * @param use
//	 */
//	public static void setItemBottomBarVisibility(AuroraActionBar auroraActionBar, int position, boolean visibility) {
//		if (auroraActionBar != null) {
//			LogUtil.d(TAG, "---position:" + position + " visibility:" + visibility);
//			auroraActionBar.getAuroraActionBottomBarMenu().getLayoutByPosition(position).setVisibility(visibility ? View.VISIBLE : View.GONE);
//		}
//	}
//
//	/**
//	 * 显示或者隐藏MENU
//	 * @param auroraActionBar
//	 * @param show
//	 */
//	public static void showOrHideMenu(AuroraActionBar auroraActionBar, boolean show) {
//		if (auroraActionBar != null) {
//			auroraActionBar.setShowBottomBarMenu(show);
//			auroraActionBar.showActionBarDashBoard();
//		}
//	}
//
//	public static void viewImg(String path, Context mContext, int pos, boolean isImage) {
//		Intent intent = new Intent();
//		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//		// intent.setAction("aurora.cloud.action.VIEW");
//		// intent.putExtra("position", pos);
//		intent.setAction(android.content.Intent.ACTION_VIEW);
//		intent.setDataAndType(Uri.fromFile(new File(path)), isImage ? "image/*" : "video/*");
//		try {
//			mContext.startActivity(intent);
//		} catch (Exception e) {
//
//		}
//	}

	//wenyongzhe 2016.3.8
	public static void inint(Context context){
		String printTxtPath = context.getApplicationContext().getFilesDir().getAbsolutePath();
		File file = new File(printTxtPath+"/"+filepath);
		if (!file.exists()) {
			List<String> list= new ArrayList<>();
			list.add("/storage/emulated/0/DCIM/Camera");
			String albums = SettingLocalUtils.writeToString(list);
			SettingLocalUtils.writeToXml(context, albums);
		}
	}
	
	boolean isFolderExists(String strFolder) {
		File file = new File(strFolder);
		if (!file.exists()) {
			if (file.mkdir()) {
				return true;
			} else
				return false;
		}
		return true;
	}
    
	/**
	 * 将数据序列化为xml流
	 * @param list
	 * @return
	 */
	public static String writeToString(List<String> list) {

		// 实现xml信息序列号的一个对象
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		if (list.size() == 0) {
			return null;
		}
		try {
			// xml数据经过序列化后保存到String中，然后将字串通过OutputStream保存为xml文件
			serializer.setOutput(writer);
			// 文档开始
			serializer.startDocument("utf-8", true);
			// 开始一个节点
			serializer.startTag("", "paths");
			for (String path : list) {
				serializer.startTag("", "name");
				serializer.text(path);
				serializer.endTag("", "name");
			}
			serializer.endTag("", "paths");
			// 关闭文档
			serializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * 将数据保存为私有xml文件
	 * @param context
	 * @param str
	 * @return
	 */
	public static boolean writeToXml(Context context, String str) {
		return writeToXml(context, str, filepath);
	}
	
	/**
	 * 将数据保存为私有xml文件
	 * @param context
	 * @param str
	 * @param filepath *.xml
	 * @return
	 */
	public static boolean writeToXml(Context context, String str,String filepath) {
		if (str == null) {
			str = "";
		}
		try {
			OutputStream out = context.openFileOutput(filepath, Context.MODE_PRIVATE);
			OutputStreamWriter outw = new OutputStreamWriter(out);
			try {
				outw.write(str);
				outw.flush();
				outw.close();
				out.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		} catch (FileNotFoundException e) {

			return false;
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param filepath *.xml
	 * @return
	 */
	public static List<String> doParseXml(Context context,String filepath){

		List<String> paths = new ArrayList<String>();
		String path = null;

		try {
			XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();
			InputStream in = context.openFileInput(filepath);
			parser.setInput(in, "utf-8");
			// 获取事件类型
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = parser.getName();
				switch (eventType) {
				// 文档开始
				case XmlPullParser.START_DOCUMENT:

					break;
				case XmlPullParser.START_TAG:
					if ("name".equals(name)) {
						parser.next();
						path = parser.getText();

					}
					break;
				case XmlPullParser.END_TAG:
					if ("name".equals(name)) {

						paths.add(path);
					}
					break;
				}
				eventType = parser.next();
			}
			in.close();
		} catch (XmlPullParserException e) {

			// e.printStackTrace();
		} catch (IOException e) {

			// e.printStackTrace();
		}
		return paths;
	
	}

	/**
	 * pull 解析xml文件
	 * @param context
	 * @return
	 */
	public static List<String> doParseXml(Context context) {
		return doParseXml(context, filepath);
	}

	/**
	 * This method generates a new suffix if a name conflict occurs, ex: paste a
	 * file named "stars.txt", the target file name would be "stars(1).txt"
	 * @param conflictFile
	 *            the conflict file
	 * @return a new name for the conflict file
	 */
	public static String autoGenerateName(File conflictFile) {
		int prevMax = 0;
		int newMax = 0;
		int leftBracketIndex = 0;
		int rightBracketIndex = 0;
		String tmp = null;
		String numeric = null;
		String fileName = null;
		File dir = null;
		File[] files = null;
		String parentDir = conflictFile.getParent();
		String conflictName = conflictFile.getName();

		if (parentDir != null) {
			dir = new File(parentDir);
			files = dir.listFiles();
		}

		if (conflictFile.isDirectory()) {
			if (conflictName.endsWith(")")) {
				leftBracketIndex = conflictName.lastIndexOf("(");
				if (leftBracketIndex != -1) {
					numeric = conflictName.substring(leftBracketIndex + 1, conflictName.length() - 1);
					if (numeric.matches("[0-9]+")) {
						newMax = findSuffixNumber(conflictName, prevMax);
						prevMax = newMax;
						conflictName = conflictName.substring(0, leftBracketIndex);
					}
				}
			}

			if (files != null) {
				for (File file : files) {
					fileName = file.getName();
					if (fileName.endsWith(")")) {
						leftBracketIndex = fileName.lastIndexOf("(");
						if (leftBracketIndex != -1) {
							tmp = fileName.substring(0, leftBracketIndex);
							if (tmp.equalsIgnoreCase(conflictName)) {
								numeric = fileName.substring(leftBracketIndex + 1, fileName.length() - 1);
								if (numeric.matches("[0-9]+")) {
									newMax = findSuffixNumber(fileName, prevMax);
									prevMax = newMax;
								}
							}
						}
					}
				}
			}
			return parentDir + "/" + conflictName + "(" + Integer.toString(newMax + 1) + ")";
		} else {
			String ext = "";
			int extIndex = conflictName.lastIndexOf(".");
			if (extIndex == -1) {
				extIndex = conflictName.length(); // this file has no extension
			} else {
				ext = conflictName.substring(extIndex);
			}

			String prefix = conflictName.substring(0, extIndex);
			if (prefix.endsWith(")")) {
				leftBracketIndex = prefix.lastIndexOf("(");
				if (leftBracketIndex != -1) {
					numeric = prefix.substring(leftBracketIndex + 1, prefix.length() - 1);
					if (numeric.matches("[0-9]+")) {
						newMax = findSuffixNumber(conflictName, prevMax);
						prevMax = newMax;
					}
				}
			}

			// reset conflictName to a new name without (x)
			int index = conflictName.lastIndexOf("(");
			if (index == -1) {
				conflictName = conflictName.substring(0, extIndex);
			} else {
				conflictName = conflictName.substring(0, index); // e.g.
																	// /sdcard/stars
			}

			if (files != null) {
				for (File file : files) {
					fileName = file.getName();
					if (fileName.endsWith(")" + ext)) {
						leftBracketIndex = fileName.lastIndexOf("(");
						rightBracketIndex = fileName.lastIndexOf(")");
						if (leftBracketIndex != -1) {
							tmp = fileName.substring(0, leftBracketIndex);
							if (tmp.equalsIgnoreCase(conflictName)) {
								numeric = fileName.substring(leftBracketIndex + 1, rightBracketIndex);
								if (numeric.matches("[0-9]+")) {
									newMax = findSuffixNumber(fileName, prevMax);
									prevMax = newMax;
								}
							}
						}
					}
				}
			}
			return parentDir + "/" + conflictName + "(" + Integer.toString(newMax + 1) + ")" + ext;
		}
	}

	/**
	 * This method finds the current max number of suffix for a conflict file
	 * ex: there are A(1).txt, A(2).txt, then the max number of suffix is 2
	 * @param fileName
	 *            the conflict file
	 * @param maxVal
	 *            the old max number of suffix
	 * @return the new max number of suffix
	 */
	private static int findSuffixNumber(String fileName, int maxVal) {
		int val = 0;
		int leftBracket = fileName.lastIndexOf("(");
		int rightBracket = fileName.lastIndexOf(")");

		String s = fileName.substring(leftBracket + 1, rightBracket);

		try {
			val = Integer.parseInt(s);
			if (val > maxVal) {
				return val;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return maxVal;
	}

	/**
	 * 获取文件总大小
	 * @param fileInfos
	 * @return
	 */
	public static long getAllSize(List<MediaFileInfo> fileInfos) {
		long size = 0;
		if (fileInfos == null || fileInfos.size() == 0) {
			return 0;
		}
		for (int i = 0; i < fileInfos.size(); i++) {
			String path = fileInfos.get(i).filePath;
			if (!TextUtils.isEmpty(path)) {
				File file = new File(path);
				size = getChildSizes(file, size);
			}
		}
		return size;
	}

	private static long getChildSizes(File file, long size) {
		if (file.isDirectory()) {
			File[] fs = file.listFiles();
			if (fs != null) {// modfiy by JXH 2014-8-11 BUG 7405
				for (int i = 0; i < fs.length; i++) {
					size = getChildSizes(fs[i], size);
				}
			}
		} else {
			size += file.length();
		}
		return size;
	}
}
