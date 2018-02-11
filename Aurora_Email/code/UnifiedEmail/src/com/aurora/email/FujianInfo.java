package com.aurora.email;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.DecimalFormat;

import com.android.mail.providers.Attachment;

import android.net.Uri;

public class FujianInfo {

	private String name;
	private long size;
	private String pathUri;
	private Uri uri;
	private String mimetype;
	
	public static final String FILE_SCHEMA = "file";
	public static final String CONTENT_SCHEMA = "content";

	public FujianInfo() {

	}

	public FujianInfo(Uri uri) {
		if (uri == null) {
			return;
		}
		this.uri = uri;
		if (uri.getScheme().equals(FILE_SCHEMA)) {
			pathUri = uri.getPath();
			File file = new File(pathUri);
			name = file.getName();
			size = file.length();
			try {
				mimetype=getMimeType(uri.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return pathUri;
	}

	public void setPath(String pathUri) {
		this.pathUri = pathUri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public Uri getUri() {
		return uri;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	@Override
	public String toString() {
		return "FujianInfo [name=" + name + ", size=" + size + ", pathUri="
				+ pathUri + ", uri=" + uri + ", mimetype=" + mimetype + "]";
	}

	public static String getMimeType(String fileUrl) throws java.io.IOException {

		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(fileUrl);
		return type;
	}
	
	public static String FormetFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.0");
		String fileSizeString = "0.0B";
		if (fileS == 0) {
			fileSizeString = "0.0B";
		} else if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}
}
