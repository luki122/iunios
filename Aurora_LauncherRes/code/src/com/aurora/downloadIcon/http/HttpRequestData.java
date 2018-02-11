package com.aurora.downloadIcon.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.aurora.downloadIcon.utils.FileLog;
import com.aurora.downloadIcon.utils.Globals;
import com.aurora.downloadIcon.utils.Log;
import com.aurora.downloadIcon.utils.ResolutionUtils;
import com.aurora.downloadIcon.utils.Utils2IconLocal;

public class HttpRequestData {
	public static final String TAG = "HttpRequestData1";

	public static String doRequest(String request_url)
			throws MalformedURLException, IOException {
		// do the decode base64
		URL url = new URL(request_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		BufferedReader in = null;
		String content = "";
		StringBuilder builder = new StringBuilder();
		Log.i(TAG, request_url);
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10000);
		conn.connect();
		int length = conn.getContentLength();
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
			Log.i(TAG, length + " " + leng);
			if (length == leng) {
				return builder.toString();
			} else {
				FileLog.e(TAG, "the return data length is error");
				return null;
			}
		} else {
			FileLog.e(TAG, "the error code is" + stutas);
			return null;
		}
	}

	public static String doPost(String uri, String jsonData)
			throws ClientProtocolException, IOException {
		HttpClient hClient = new DefaultHttpClient();
		HttpPost hPost = new HttpPost(uri);
		String result = null;

		//just test begin
		//jsonData="{\"packageName\":\"a\",\"className\":\"a\",\"label\":\"aa\",\"resolution\":\"xxxhdpi\"}";
		//just test end
		StringEntity stringEntity = new StringEntity(jsonData,"UTF-8");
		//Log.i("test", "uri = "+uri+"    ,    jsonData = "+ jsonData);
		stringEntity.setContentType("application/json");
		hPost.setEntity(stringEntity);
		//Log.i("test", "HttpResponse  excute begein");
		HttpResponse response = hClient.execute(hPost);
		//Log.i("test", "HttpResponse  excute end");
		Log.i("test", "response.getStatusLine().getStatusCode() = "
				+ response.getStatusLine().getStatusCode());
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			result = getStringFromHttp(response.getEntity());
		}
		//Log.i("test", "result  =  "+result);
		return result;

	}

	public static Bitmap doGetIconBitmap(String uri,String resolution, Context context)
			throws Exception { 
		URL url = new URL(uri);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5 * 1000);
		InputStream inStream = conn.getInputStream();
		byte[] buff = readInputStream(inStream);
		Bitmap bitmap = BitmapFactory.decodeByteArray(buff, 0, buff.length);
		float scaleFactor = ResolutionUtils.iconScale(ResolutionUtils.getDEVICE_RESOLUTION() , resolution);
		//Log.i("test1","scaleFactor = "+scaleFactor);
		//if(scaleFactor>1.0f){
			bitmap = Utils2IconLocal.zoomDrawable(bitmap,scaleFactor,context.getResources());
		//}
		return bitmap;
	}

	public static byte[] readInputStream(InputStream instream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1204];
		int len = 0;
		while ((len = instream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		instream.close();
		return outStream.toByteArray();
	}

	private static String getStringFromHttp(HttpEntity entity) {
		StringBuffer buffer = new StringBuffer();
		try {
			// 获取输入流
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					entity.getContent(), EntityUtils.getContentCharSet(entity)));

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
}
