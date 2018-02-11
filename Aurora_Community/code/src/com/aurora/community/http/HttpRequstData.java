package com.aurora.community.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.aurora.community.CommunityApp;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.BitmapUtil;
import com.aurora.community.utils.FileLog;
import com.aurora.community.utils.Log;
import com.aurora.datauiapi.data.exception.ServerException;


public class HttpRequstData {
	private static final String TAG = "HttpRequstData";
	
	private static myX509TrustManager xtm = new myX509TrustManager();

	private static myHostnameVerifier hnv = new myHostnameVerifier();

	private static final String session_str = "\"returnCode\":-1";
	
	/**
	 * 重写三个方法
	 * 
	 * @author Administrator
	 * 
	 */
	static class myX509TrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) {
			/*
			 * System.out.println("cert: " + chain[0].toString() +
			 * ", authType: " + authType);
			 */
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	/** */
	/**
	 * 重写一个方法
	 * 
	 * @author Administrator
	 * 
	 */
	static class myHostnameVerifier implements HostnameVerifier {

		public boolean verify(String hostname, SSLSession session) {
			/*
			 * System.out.println("Warning: URL Host: " + hostname + " vs. " +
			 * session.getPeerHost());
			 */
			return true;
		}
	}

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
	public static String doPost(Map<String, String> map, String http_url)
			throws IllegalStateException, IOException {
		String strResult = "";
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpPost post = new HttpPost(http_url);
		// 请求超时
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
		// 读取超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				15000);
		if (!TextUtils.isEmpty(AccountHelper.cookie))
			post.addHeader("Cookie", "tgt=" + AccountHelper.cookie);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		/*
		 * params.add(new BasicNameValuePair("serviceName", http_name));
		 * params.add(new BasicNameValuePair("method", http_action));
		 * params.add(new BasicNameValuePair("parmJson", jr));
		 */

		// params.add(new BasicNameValuePair("Cookie",
		// "tgt="+AccountHelper.cookie));
		for (Map.Entry<String, String> entry : map.entrySet()) {
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		/* 添加请求参数到请求对象 */
		post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		HttpResponse response = httpClient.execute(post);

		// 若状态码为200 ok
		if (response.getStatusLine().getStatusCode() == 200) {
			// 取出回应字串
			strResult = getStringFromHttp(response.getEntity());
		} else {
			FileLog.e(TAG, "the error code is"
					+ response.getStatusLine().getStatusCode());
			return null;
		}
		// 判断cookie失效
		if (strResult.contains(session_str)) {
			FileLog.e(TAG, "the cookie is timeout");
			if (getCookie()) {
				strResult = doPost(map, http_url);
			}

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
			throws MalformedURLException, IOException {

		// do the decode base64
		URL url = new URL(request_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {

			BufferedReader in = null;
			String content = "";
			StringBuilder builder = new StringBuilder();
			Log.i(TAG, request_url);
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			if (!TextUtils.isEmpty(AccountHelper.cookie))
				conn.setRequestProperty("Cookie", "tgt=" + AccountHelper.cookie);
			conn.connect();
			// int length = conn.getContentLength();
			int stutas = conn.getResponseCode();
			Log.i(TAG, "stutas " + stutas);
			if (stutas == 200) {
				Log.i(TAG, "http success");
				InputStream inStream = conn.getInputStream();
				in = new BufferedReader(
						new InputStreamReader(inStream, "UTF-8"));
				while ((content = in.readLine()) != null) {
					builder.append(content);
				}
				in.close();
				int leng = builder.toString().getBytes("UTF-8").length;
				String result = builder.toString();
				// 判断cookie失效
				if (result.contains(session_str)) {
					FileLog.e(TAG, "the cookie is timeout");
					if (getCookie()) {
						result = doRequest(request_url);
					}

				}
				Log.i(TAG, "the result= " + result);
				return result;
				/*
				 * }else { FileLog.e(TAG, "the return data length is error");
				 * return null; }
				 */
			} else {
				FileLog.e(TAG, "the error code is" + stutas + " the url = "
						+ request_url);
				return null;
			}
		} catch (HttpHostConnectException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (ConnectException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (MalformedURLException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// MyLog.e("error2", e.getMessage());
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} finally {
			conn.disconnect();
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

	public static boolean getCookie() {
		String returnData = new String();
		Uri uri = Uri
				.parse("content://com.aurora.account.accountprovider/account_cookie");
		Cursor cursor = CommunityApp.getInstance().getContentResolver()
				.query(uri, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			returnData = cursor.getString(cursor.getColumnIndex("cookie"));
			FileLog.e(TAG, "the cookie request success the returnData="
					+ returnData);
		} else {
			FileLog.e(TAG, "the cookie request failed");
			return false;
		}
		AccountHelper.cookie = returnData;
		return true;
	}

	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 * 
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public static String uploadFile(String actionUrl,
			Map<String, String> params, String fileName, String path)
			throws IOException {
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";

		// InputStream isStream = new FileInputStream(path);
		InputStream isStream;
		int pic_type = 0;
		String tmpname = fileName.toLowerCase();
		if (tmpname.endsWith("jpg") || tmpname.endsWith("jpeg"))
			pic_type = 0;
		else if (tmpname.endsWith("png"))
			pic_type = 1;
		else if (tmpname.endsWith("webp"))
			pic_type = 2;
		else
			pic_type = -1;

		try {
			if (pic_type != -1) {
				Bitmap bitmap = BitmapUtil.compressImageFromFile(path, 720);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (pic_type == 0)
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				else if (pic_type == 1)
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				else if (pic_type == 2) {
					fileName = fileName.replaceFirst("webp", "jpg");
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				}
				isStream = new ByteArrayInputStream(baos.toByteArray());
			} else {
				isStream = new FileInputStream(path);
			}
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "zhangwei, convertViewToBitmap out of memory", e);
			return "1";
		} catch (Throwable t) {
			Log.e(TAG, "zhangwei, convertViewToBitmap error", t);
			return null;
		}

		URL uri = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		conn.setConnectTimeout(15 * 1000);
		conn.setReadTimeout(15 * 1000); // 缓存的最长时间
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false); // 不允许使用缓存
		conn.setRequestMethod("POST");
		// conn.setRequestProperty("Accept", "*/*");
		// conn.setRequestProperty("Accept-Language", "zh-cn");
		if (!TextUtils.isEmpty(AccountHelper.cookie))
			conn.setRequestProperty("Cookie", "tgt=" + AccountHelper.cookie);
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
				+ ";boundary=" + BOUNDARY);
		// 首先组拼文本类型的参数
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINEND);
			sb.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"" + LINEND);
			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
			sb.append(LINEND);
			sb.append(entry.getValue());
			sb.append(LINEND);
		}

		DataOutputStream httpOutStream = null;
		BufferedReader httpIn = null;
		try {
			httpOutStream = new DataOutputStream(conn.getOutputStream());
			httpOutStream.write(sb.toString().getBytes());
			Log.i(TAG, "sb=" + sb.toString());
			// 发送文件数据
			if (null != isStream) {
				StringBuilder sb1 = new StringBuilder();
				sb1.append(PREFIX);
				sb1.append(BOUNDARY);
				sb1.append(LINEND);

				sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
						+ fileName + "\"" + LINEND);
				sb1.append("Content-Type: application/octet-stream; charset="
						+ CHARSET + LINEND);
				sb1.append(LINEND);
				httpOutStream.write(sb1.toString().getBytes());
				Log.i(TAG, "sb1=" + sb1.toString());
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = isStream.read(buffer)) != -1) {
					httpOutStream.write(buffer, 0, len);
				}
				httpOutStream.write(LINEND.getBytes());
			}
			// 请求结束标志
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
			httpOutStream.write(end_data);
			httpOutStream.flush();
			// 得到响应码
			StringBuilder sb2 = new StringBuilder();
			int res = conn.getResponseCode();
			if (res == 200) {
				httpIn = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "UTF-8"));
				String content = "";
				while ((content = httpIn.readLine()) != null) {
					sb2.append(content);
				}
			} else {
				FileLog.e(TAG, "the error code is" + res);
				return null;
			}
			String result = sb2.toString();
			// 判断cookie失效
			if (result.contains(session_str)) {
				FileLog.e(TAG, "the cookie is timeout");
				if (getCookie()) {
					isStream.skip(0);
					result = uploadFile(actionUrl, params, fileName, path);
				}

			}
			Log.i(TAG, sb2.toString());
			return result;
		} finally {
			if (isStream != null) {
				isStream.close();
			}

			if (httpIn != null) {
				httpIn.close();
			}

			if (httpOutStream != null) {
				httpOutStream.close();
			}

			conn.disconnect();
		}
	}
	
	public static String doHttpsRequest(String request_url,String param) throws Exception {

		// do the decode base64
		HttpsURLConnection conn = null;
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");

			X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };

			sslContext.init(null, xtmArray, new java.security.SecureRandom());

			if (sslContext != null) {
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
						.getSocketFactory());
			}
			HttpsURLConnection.setDefaultHostnameVerifier(hnv);

			URL url = new URL(request_url);
			conn = (HttpsURLConnection) url.openConnection();
			BufferedReader in = null;
			String content = "";
			StringBuilder builder = new StringBuilder();
			Log.i(TAG, request_url);
			FileLog.i(TAG,"the request_url="+request_url);
			FileLog.i(TAG, "the param="+param);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(10000);
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
			
			StringBuilder entityBuilder = new StringBuilder("");
			
			entityBuilder.append("parmJson=").append(getDecodeStr(param));
			/*conn.getOutputStream().write(request_url.getBytes());
			conn.getOutputStream().flush();
			conn.getOutputStream().close();*/
			OutputStream outStream = conn.getOutputStream();
			outStream.write(entityBuilder.toString().getBytes(("UTF-8")));
			outStream.flush();
			outStream.close();
			
			
			conn.connect();
		

			
			// int length = conn.getContentLength();
			int stutas = conn.getResponseCode();
			Log.i(TAG, "stutas " + stutas);
			if (stutas == 200) {
				Log.i(TAG, "http success");
				InputStream inStream = conn.getInputStream();
				in = new BufferedReader(
						new InputStreamReader(inStream, "UTF-8"));
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
				Log.i(TAG, "the return data is="+ builder.toString());
				FileLog.i(TAG, "the return data is="+builder.toString());
				return builder.toString();
				/*
				 * }else { FileLog.e(TAG, "the return data length is error");
				 * return null; }
				 */
			} else {
				FileLog.e(TAG, "the error code is" + stutas);
				throw new ServerException(stutas);
			}
		} catch (HttpHostConnectException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (ConnectException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (NoSuchAlgorithmException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			/* throw e; */
			return request_url;
		} catch (KeyManagementException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			/* throw e; */
			return request_url;
		} catch (MalformedURLException e) {
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// MyLog.e("error2", e.getMessage());
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
			throw e;
		}
		finally {
			conn.disconnect();
		}

	}
	
	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 * 
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public static String uploadFile(String actionUrl,
			Map<String, String> params, String fileName, InputStream isStream) throws IOException {
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";

		URL uri = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		conn.setReadTimeout(15 * 1000); // 缓存的最长时间
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false); // 不允许使用缓存
		conn.setRequestMethod("POST");
		// conn.setRequestProperty("Accept", "*/*");
		// conn.setRequestProperty("Accept-Language", "zh-cn");
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
				+ ";boundary=" + BOUNDARY);
		// 首先组拼文本类型的参数
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINEND);
			sb.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"" + LINEND);
			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
			sb.append(LINEND);
			sb.append(entry.getValue());
			sb.append(LINEND);
		}
		
		DataOutputStream httpOutStream = null;
		BufferedReader httpIn = null;
		try {
		    httpOutStream = new DataOutputStream(conn.getOutputStream());
	        httpOutStream.write(sb.toString().getBytes());
	        Log.i(TAG, "sb=" + sb.toString());
	        // 发送文件数据
	        if (null != isStream) {
	            StringBuilder sb1 = new StringBuilder();
	            sb1.append(PREFIX);
	            sb1.append(BOUNDARY);
	            sb1.append(LINEND);

	            sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
	                    + fileName + BOUNDARY + "\"" + LINEND);
	            sb1.append("Content-Type: application/octet-stream; charset="
	                    + CHARSET + LINEND);
	            sb1.append(LINEND);
	            httpOutStream.write(sb1.toString().getBytes());
	            Log.i(TAG, "sb1=" + sb1.toString());
	            byte[] buffer = new byte[1024];
	            int len = 0;
	            while ((len = isStream.read(buffer)) != -1) {
	                httpOutStream.write(buffer, 0, len);
	            }
	            httpOutStream.write(LINEND.getBytes());
	        }
	        // 请求结束标志
	        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
	        httpOutStream.write(end_data);
	        httpOutStream.flush();
	        // 得到响应码
	        StringBuilder sb2 = new StringBuilder();
	        int res = conn.getResponseCode();
	        if (res == 200) {
	            httpIn = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	            String content = "";
	            while ((content = httpIn.readLine()) != null) {
	                sb2.append(content);
	            }
	        }
	        
	        Log.i(TAG, sb2.toString());
	        return sb2.toString();
		} finally {
		    if (isStream != null) {
		        isStream.close();
		    }
		    
		    if (httpIn != null) {
		        httpIn.close();
		    }
		    
		    if (httpOutStream != null) {
		        httpOutStream.close();
		    }
		    
		    conn.disconnect();
		}
	}
	
	public static boolean downloadFile(String downloadUrl, String param, OutputStream os) throws IOException {
	    URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(10000);
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
        
        StringBuilder entityBuilder = null;
        if (!TextUtils.isEmpty(param)) {
            entityBuilder = new StringBuilder("");
            entityBuilder.append("parmJson=").append(param);
        }
        OutputStream outStream = null;
        InputStream inStream = null;
        BufferedReader inReader = null;
        try {
            outStream = conn.getOutputStream();
            if (entityBuilder != null) {
                outStream.write(entityBuilder.toString().getBytes());
                outStream.flush();
            }
            
            conn.connect();
            int code = conn.getResponseCode();
            if (code == HttpStatus.SC_OK) {
                inStream = conn.getInputStream();
                String type = conn.getContentType();
                if ("application/octet-stream".equalsIgnoreCase(type) ||
                        "image/jpeg;charset=UTF-8".equalsIgnoreCase(type)) {
                    // 附件的内容
                    byte[] buffer = new byte[4096];
                    int offset = 0;
                    while ((offset = inStream.read(buffer)) != -1) {
                        os.write(buffer, 0, offset);
                    }
                    return true;
                } else {
                    // 下载出错提示
                    inReader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                    String content = null;
                    StringBuilder result = new StringBuilder();
                    while ((content = inReader.readLine()) != null) {
                        result.append(content);
                    }
                    FileLog.d(TAG, result.toString());
                }
            }
        } finally {
            if (os != null) {
                os.close();
            }
            
            if (inStream != null) {
                inStream.close();
            }
            
            if (inReader != null) {
                inReader.close();
            }
            
            if (outStream != null) {
                outStream.close();
            }
            
            conn.disconnect();
        }
        
        return false;
	}

}
