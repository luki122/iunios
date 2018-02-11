package com.aurora.thememanager.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.Worker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public abstract class JsonParser implements DataParser {

	private final String TAG = "ThemeParser";
	
	private static final int MSG_START_PARSER = 1;
	
	private Worker mWorker;

	protected int mCode = -1;
	
	protected String mRequestDesc;
	
	protected int mTotalPage;
	
	protected CallBack mCallBack;
	
	
	/**
	 * 解析服务器请求的回调接口
	 * @author alexluo
	 *
	 */
	public interface CallBack{
		
		/**
		 * 判断解析的数据是否成功
		 * @param success   
		 * 					请求数据成功
		 * @param statusCode  
		 * 					请求数据的状态码
		 * @param desc 
		 * 					 请求数据的状态描述
		 * @param totalPage  
		 * 					请求数据的总页数
		 */
		void onParserSuccess(boolean success,int statusCode,String desc,int totalPage);
		
	}
	
	public void setCallBack(CallBack callback){
		this.mCallBack = callback;
	}
	
	
	
	@Override
	public List<Object> parser(Object source) throws ParserException {
		// TODO Auto-generated method stub

		try {
			/*
			 * we just parse json from string and inputstream
			 */
			if (source instanceof InputStream) {
				return readJsonStream((InputStream) source);
			} else if (source instanceof String) {
				return readJson((String) source);
			}else{
				throw new ParserException("source json file must be a string or inputstream object");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("Parser", "JsonParser  IOException"+e);
			e.printStackTrace();
		}
		return null;
	}

	private List<Object> readJson(String in) {
		if(TextUtils.isEmpty(in)){
			return null;
		}
		return getJsonObject(in);
	}

	private List<Object> readJsonStream(InputStream in) throws IOException {
		return readJson(getJsonStringFromInputStream(in));
	}

	/**
	 * parser our json here
	 * @param json  data need to parse
	 * @return	          target obj list
	 */
	private List<Object> getJsonObject(String json){
		try {
			List<Object> result = readObject(new JSONObject(json));
			if(mCallBack != null){
				mCallBack.onParserSuccess(mCode==0, mCode, mRequestDesc, mTotalPage);
			}
			return  result;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	/*
	 * convert inputstream to json string
	 * @param in
	 * @return
	 */
	public static String getJsonStringFromInputStream(InputStream in) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String result = null;
		byte[] data = new byte[1024];
		int len = 0;
		try {
			while ((len = in.read(data)) != -1) {
				os.write(data, 0, len);
			}
			result = new String(os.toByteArray());
		} catch (Exception ex) {
		}finally{
			try{
			in.close();
			os.close();
			}catch(Exception e){
				
			}
		}
		return result;
	}

	
	public abstract List<Object> readObject(JSONObject json);
	
	
	
}
