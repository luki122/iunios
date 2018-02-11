package com.secure.request.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import com.secure.data.HttpData;
import com.secure.utils.CustomerHttpClient;
import com.secure.utils.LogUtils;
import com.secure.utils.StringUtils;
import android.content.Context;
import android.os.Handler;

/**
 * 注意，网络请求的异常情况有：
 * 1.没有网络（请求前，可以通过网络连接状态去判断）
 * 2.有网络，但网络很差，连接超时，这是请求会抛出异常
 * 3.连接WIFI，手机端与WIFI端网络通畅，但WIFI与外网断开，此时请求会被拒绝，请求会抛出异常
 * 4.网络通畅，连接上服务器后，服务器的返回码不是200，而是（400,504等）
 * 5.网络通畅，但服务器返回的数据无法通过预定的JSON数据格式解析，此时JSON数据解析部分抛出异常
 * @author bin.huang
 *
 */
public class HttpModel {
	public static final int HTTP_STATUS_OK = 200;
	public static final String SUCCESS_CODE = "0000"; // 正确码
	public static final String ERROR_CODE = "3001";
		
	String url = "";	
	HttpClient client = null;
	HttpPost httppost = null;
	HttpData httpData = null;
	Handler handler = null;//UI线程处理数据更新
	Context context;
	public String TAG = HttpModel.class.getName();
	
	/**
	 * @param context
	 * @param needCache 是否需要缓存请求数据到sdCard
	 * @param cacheName
	 * @param url
	 * @param tag
	 */
	public HttpModel(Context context,
			String url){
		this.context = context;
		this.url = url;
		initRequest();
	}
	
	private void initRequest() {	
		if(httpData == null){
			httpData = new HttpData();
		}
	}
	
	/**
	 * 获取相关请求过程相关数据
	 * @return
	 */
	public HttpData getHttpData(){
		return httpData;
	}
	
	/**
	 * 创建post请求的数据
	 * @return
	 */
	public String createPostReqData(){
		return null;
	}
	
	/**
	 * 重置数据
	 */
	public void resetData(){
		
	}
	
	public boolean postRequest() {		
		try {
			resetData();
			setHttpData(true,null);
			boolean result = true;
			StringUtils.str_replace(url, "//", "/");
			httppost = new HttpPost(url);
			LogUtils.printWithLogCat(TAG,url);
			
			String msgStr = createPostReqData();		
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
			NameValuePair namevale  = new BasicNameValuePair("message", msgStr);
			postParams.add(namevale);
			
			httppost.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
		
			if(client == null){
				client = CustomerHttpClient.getHttpClient();
			}
			HttpResponse response = client.execute(httppost);				
			StatusLine status = response.getStatusLine();
			
			LogUtils.printWithLogCat(TAG,""+status.getStatusCode());
			
			if (status.getStatusCode() == HTTP_STATUS_OK) {
				if (httppost.isAborted()) {
					result = false;
				}				
				if(this.DoThing(response.getEntity().getContent())){
					result = true;
				}else{
					result = false;
				}					
			}else{
				result = false;
			}		
			if(result){
				setHttpData(false,HttpData.STATUS.SUCESS);
			}else{
				setHttpData(false,HttpData.STATUS.ERROR_OF_504);
			}
			return result;
		} catch (Exception ex) {
			LogUtils.printWithLogCat(TAG,""+ex.toString());
			ex.printStackTrace();
			setHttpData(false,HttpData.STATUS.ERROR_OF_TIMEOUT);
			return false;
		}
	}
	
	/**
	 * 解析输入流
	 * @param in
	 * @return
	 */
	public boolean DoThing(InputStream in) {
		return false;
	}
	
	/**
	 * 记录请求的相关数据
	 * @param duringRequest true:请求开始   false：请求结束
	 * @param status
	 */
    void setHttpData(boolean duringRequest,HttpData.STATUS status){
		if(httpData != null){			
			if(status != null){
				httpData.setRequestStatus(status);
			}			
			if(duringRequest){
				httpData.start();
			}else{
				httpData.end();
			}
		}
	}
  
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	public void releaseObject(){	
//		client = null;
//		httppost = null;
//		httpData = null;
//		context = null;
	}
}
