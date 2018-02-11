package com.aurora.puremanager.traffic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.android.internal.util.FastXmlSerializer;

import android.os.Environment;
import android.util.AtomicFile;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.Xml;
//import libcore.io.IoUtils;
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class NetworkDisableUids {

	private static final String LIST_TAG = "list";
	private static final String UID_TAG = "uid";
	private static final String MOBILE_DISABLE_FILENAME = "mobile_disable_files.xml";
	private static final String WIFI_DISABLE_FILENAME = "wifi_disable_files.xml";
	private SparseIntArray mMobileDisableUids = new SparseIntArray();
	private SparseIntArray mWifiDisableUids = new SparseIntArray();
	private int NETWORK_DISABLED = 1;
	private AtomicFile mMobileDisableFile;
	private AtomicFile mWifiDisableFile;
	private static NetworkDisableUids mInstance;

	public static NetworkDisableUids getInstance() {
		if (mInstance == null) {
			mInstance = new NetworkDisableUids();
		}
		return mInstance;
	}

	private NetworkDisableUids() {
		mMobileDisableFile = new AtomicFile(new File(getSystemDir(), MOBILE_DISABLE_FILENAME));
		mWifiDisableFile = new AtomicFile(new File(getSystemDir(), WIFI_DISABLE_FILENAME));
		readDisableUidsRules(mMobileDisableFile, mMobileDisableUids);
		readDisableUidsRules(mWifiDisableFile, mWifiDisableUids);
		clearOtherNetworkData();
	}
	
	public SparseIntArray getDisableUids(int networkType){
		SparseIntArray array = new SparseIntArray();
		switch (networkType) {
		case Constant.MOBILE:
			array = mMobileDisableUids;
			break;
		case Constant.WIFI:
			array = mWifiDisableUids;
			break;
		default:
			break;
		}
		return array;
	}

	public int getDisableUidsNums(int networkType){
		int num = 0;
		switch (networkType) {
		case Constant.MOBILE:
			num = mMobileDisableUids.size();
			break;

		case Constant.WIFI:
			num = mWifiDisableUids.size();
			break;

		default:
			break;
		}
		return num;
	}
	
	public void resetDisableUidsRules(int networkType, SparseIntArray array){
		switch (networkType) {
		case Constant.MOBILE:
			writeDisableUidsRules(mMobileDisableFile, array);
			break;

		case Constant.WIFI:
			writeDisableUidsRules(mWifiDisableFile, array);
			break;

		default:
			break;
		}
	}
	
	private void readDisableUidsRules(AtomicFile file, SparseIntArray array) {
		FileInputStream in = null;
		try {
			in = file.openRead();
			final XmlPullParser xml = Xml.newPullParser();
			xml.setInput(in, null);
			int type;
			while ((type = xml.next()) != END_DOCUMENT) {
				final String tag = xml.getName();
				if (type == START_TAG) {
					if (UID_TAG.equals(tag)) {
						String uid = xml.nextText();
						array.put(Integer.parseInt(uid), NETWORK_DISABLED);
					}
				}
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} catch (XmlPullParserException e) {

		} finally {
//			IoUtils.closeQuietly(in);
		}
	}

	private void writeDisableUidsRules(AtomicFile file, SparseIntArray array) {
		FileOutputStream ou = null;
		try {
			ou = file.startWrite();
			XmlSerializer out = new FastXmlSerializer();
			out.setOutput(ou, "utf-8");
			out.startDocument(null, true);
			out.startTag(null, LIST_TAG);
			for (int i = 0; i < array.size(); i++) {
				final int uid = array.keyAt(i);
				out.startTag(null, UID_TAG);
				out.text("" + uid);
				out.endTag(null, UID_TAG);
			}
			out.endTag(null, LIST_TAG);
			out.endDocument();
			file.finishWrite(ou);
		} catch (IOException e) {
			if (ou != null) {
				file.failWrite(ou);
			}
		}
	}
	
	private void clearOtherNetworkData(){
		AtomicFile disnet = new AtomicFile(new File(getSystemDir(), "disablenetwork.xml"));
		AtomicFile netpolicy = new AtomicFile(new File(getSystemDir(), "netpolicy.xml"));
		SparseIntArray disnetUids = new SparseIntArray();
		SparseIntArray netpolicyUids = new SparseIntArray();
		readDisableUidsRules(disnet, disnetUids);
		readDisableUidsRules(netpolicy, netpolicyUids);
		Log.d("action", disnetUids.size() + "-->");
		for(int uid = 0; uid < disnetUids.size(); uid ++){
			Log.d("action", uid + "-->");
			netpolicyUids.delete(uid);
		}
		disnetUids.clear();
		writeDisableUidsRules(netpolicy, netpolicyUids);
		writeDisableUidsRules(disnet, disnetUids);		
	}
	
	private static File getSystemDir() {
		return new File(Environment.getDataDirectory(), "system");
	}
}
