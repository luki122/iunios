package com.aurora.thememanager.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.aurora.thememanager.utils.FileLog;

public class HttpRequstData {
	private static final String TAG = "HttpRequstData";

	public static String getURLStr(String http_url, String http_name,
			String http_action) {
	
		String url = http_url + http_name + http_action;

		return url;
	}

	public static String getDecodeStr(String http_param) {
		String jo_str = "";

		try {
			jo_str = URLEncoder.encode(http_param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return jo_str;
	}

	/**
	 * 执行一个HTTP POST请求，返回请求响应的HTML
	 * 
	 * @param url
	 *            请求的URL地址
	 * @param params
	 *            请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static String doPost(String http_url, String jr) throws IllegalStateException,
			IOException {
		String strResult = "";
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(http_url);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		/*params.add(new BasicNameValuePair("serviceName", http_name));
		params.add(new BasicNameValuePair("method", http_action));*/
		params.add(new BasicNameValuePair("parmJson", jr));
		/* 添加请求参数到请求对象 */
		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		
		Log.i(TAG, "the dopost url=" + http_url);
		Log.i(TAG, "doPost the params=" + params.toString());
		
		HttpResponse response = httpClient.execute(post);

		// 若状态码为200 ok
		if (response.getStatusLine().getStatusCode() == 200) {
			// 取出回应字串
			strResult = getStringFromHttp(response.getEntity());
		}
		return strResult;
	}

	// 获取所有的网页信息以String 返回
	private static String getStringFromHttp(HttpEntity entity) {

		StringBuffer buffer = new StringBuffer();

		try {
			// 获取输入流
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					entity.getContent()));

			// 将返回的数据读到buffer中
			String temp = null;

			while ((temp = reader.readLine()) != null) {
				buffer.append(temp);
			}
		} catch (IllegalStateException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public static String doRequest(String request_url)
			throws IOException {

		// do the decode base64
		try{
		URL url = new URL(request_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		BufferedReader in = null;
		String content = "";
		StringBuilder builder = new StringBuilder();
		Log.i(TAG, request_url);
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(10000);
		conn.connect();
		// int length = conn.getContentLength();
		int stutas = conn.getResponseCode();
		Log.i(TAG, "stutas " + stutas);
		if (stutas == 200) {
			Log.i(TAG, "http success");
			InputStream inStream = conn.getInputStream();
			in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
			while ((content = in.readLine()) != null) {
				builder.append(content);
			}
			in.close();
			int leng = builder.toString().getBytes("UTF-8").length;
			// Log.i(TAG, length+" "+leng);
			// if (length == leng){
			/*
			 * for(int i = 0; i< builder.length(); i ++) { Log.i(TAG,
			 * "the return str="+builder.charAt(i)); }
			 */
			return builder.toString();
			/*
			 * }else { FileLog.e(TAG, "the return data length is error"); return
			 * null; }
			 */
		} else {
			FileLog.e(TAG, "the error code is" + stutas);
			return null;
		}
		}
		catch(HttpHostConnectException e)
		{
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		}
		catch(ConnectException e)
		{
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		}
		catch (MalformedURLException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// MyLog.e("error2", e.getMessage());
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		}
	}

	public static String getFromHttp(String url)
			throws ClientProtocolException, IOException {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);

		HttpResponse response = client.execute(get);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "GBK"));

		for (String s = reader.readLine(); s != null; s = reader.readLine()) {
			builder.append(s);
		}

		return builder.toString();
	}
}
