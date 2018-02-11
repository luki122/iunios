package com.aurora.internet;

import android.content.Context;
import android.text.TextUtils;

import com.aurora.internet.toolbox.ByteArrayPool;
import com.aurora.internet.toolbox.PoolingByteArrayOutputStream;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.utils.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HttpUtils {

	/** Reads the contents of HttpEntity into a byte[]. */
	public static byte[] responseToBytes(HttpResponse response) throws IOException, ServerError {
		HttpEntity entity = response.getEntity();
		PoolingByteArrayOutputStream bytes =
				new PoolingByteArrayOutputStream(ByteArrayPool.get(), (int) entity.getContentLength());
		byte[] buffer = null;
		try {
			InputStream in = entity.getContent();
			if (isGzipContent(response) && !(in instanceof GZIPInputStream)) {
				in = new GZIPInputStream(in);
			}

			if (in == null) {
				throw new ServerError();
			}

			buffer = ByteArrayPool.get().getBuf(1024);
			int count;
			while ((count = in.read(buffer)) != -1) {
				bytes.write(buffer, 0, count);
			}
			return bytes.toByteArray();
		} finally {
			try {
				// Close the InputStream and release the resources by "consuming the content".
				entity.consumeContent();
			} catch (IOException e) {
				// This can happen if there was an exception above that left the entity in
				// an invalid state.
				InternetLog.v("Error occured when calling consumingContent");
			}
			ByteArrayPool.get().returnBuf(buffer);
			bytes.close();
		}
	}

	/** Returns the charset specified in the Content-Type of this header. */
	public static String getCharset(HttpResponse response) {
		Header header = response.getFirstHeader(HTTP.CONTENT_TYPE);
		if (header != null) {
			String contentType = header.getValue();
			if (!TextUtils.isEmpty(contentType)) {
				String[] params = contentType.split(";");
				for (int i = 1; i < params.length; i++) {
					String[] pair = params[i].trim().split("=");
					if (pair.length == 2) {
						if (pair[0].equals("charset")) {
							return pair[1];
						}
					}
				}
			}
		}
		return null;
	}

	public static String getHeader(HttpResponse response, String key) {
		Header header = response.getFirstHeader(key);
		return header == null ? null : header.getValue();
	}

	public static boolean isSupportRange(HttpResponse response) {
		if (TextUtils.equals(getHeader(response, "Accept-Ranges"), "bytes")) {
			return true;
		}
		String value = getHeader(response, "Content-Range");
		return value != null && value.startsWith("bytes");
	}

	public static boolean isGzipContent(HttpResponse response) {
		return TextUtils.equals(getHeader(response, "Content-Encoding"), "gzip");
	}
	
	/**
	 * 构建post请求需要的参数列表
	 * @return
	 */
	public static JSONObject createPostParams(Context context,int page){
		Map<String, Object> map = new HashMap<String, Object>();  
		map.put("pageNum", page);  
		map.put("phoneModel", /*SystemUtils.getModel()*/"IUNI N1");  
		map.put("imei",SystemUtils.getImei(context));  
		map.put("width", SystemUtils.getDisplayWidth(context));  
		map.put("height", SystemUtils.getDisplayHeight(context));  
		map.put("osVersion", SystemUtils.OS_VERSION+"");  
		JSONObject jsonObject = new JSONObject(map);
		return jsonObject;
	}
	
	public static JSONObject createPostParamsFromMap(Map<String, Object>  map){
		JSONObject json = new  JSONObject(map);
		Log.d("json", ""+json.toString());
		return json;
	}
	
	/**
	 * 构造一个预加载了设备信息的map，该map用于
	 * 转换为JSON对象作为Post请求的参数
	 * @param key
	 * @param value
	 * @param context
	 * @return
	 */
	public static Map<String,Object> createPostMap(String key,Object value,Context context){
		Map<String, Object> map = new HashMap<String, Object>();  
		map.put(key, value);
		map.put("phoneModel", /*SystemUtils.getModel()*/"IUNI N1");  
		map.put("imei",SystemUtils.getImei(context));  
		map.put("width", SystemUtils.getDisplayWidth(context));  
		map.put("height", SystemUtils.getDisplayHeight(context));  
		map.put("osVersion", SystemUtils.OS_VERSION+"");  
		return map;
	}
	
	

}
