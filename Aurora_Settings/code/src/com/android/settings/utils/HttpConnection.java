package com.android.settings.utils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//import javax.imageio.ImageIO;
//import javax.imageio.ImageReader;
//import javax.imageio.stream.ImageInputStream;

import android.content.Context;
import android.util.Log;

public class HttpConnection {
	private static final String TAG="gd";
	public static final int FEEDBACK=1;
	public static final int SUGGESTS=0;
    static long stampTime;
     static int respondeCode;
    
	URL url;
	HttpURLConnection conn;
//	String boundary = "--------httppost123";
	String boundary = "910c075854bb6c06c330d7e852c67cbf";
	Map<String, String> textParams = new HashMap<String, String>();
	Map<String, File> fileparams = new HashMap<String, File>();
	Map<String, String> headers = new HashMap<String, String>();
	DataOutputStream ds;
	
   public static void initialConn()
   {
		Log.d(TAG, " initial upload  !!!!!!!!!!");
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("imei", "008600215140400");
        
		String initUrl="http://i.iunios.com/app/init";
        String initResp = null;
		try {
			initResp = postJsonData(initUrl, initParams);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "initial code="+initResp);
   }
   
	public static String postJsonData(String requestUrl, Map<String,String> params) throws IOException {       
    	HttpURLConnection conn = null;  
        DataOutputStream output = null;  
        BufferedReader input = null;
        String lineEnd = System.getProperty("line.separator");
        
        try {  
            URL url = new URL(requestUrl);  
            conn = (HttpURLConnection) url.openConnection();  
            conn.setConnectTimeout(120000);  
            conn.setDoInput(true); 
            conn.setDoOutput(true); 
            conn.setUseCaches(false);  
            conn.setRequestMethod("POST");  
//            conn.setRequestProperty("Connection", "keep-alive");  
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            
            conn.connect();  
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for(Map.Entry<String, String> paramEntry : params.entrySet()) {  
                sb.append("\"").append(paramEntry.getKey()).append("\":\"").append(paramEntry.getValue()).append("\",");  
            }
            sb.setLength(sb.length() - 1);
            sb.append("}");
            
            output = new DataOutputStream(conn.getOutputStream());
            
            output.write(sb.toString().getBytes("UTF-8"));  
            output.flush();  
              
            int code = conn.getResponseCode(); 
            Log.d(TAG, " initial  code="+code);
            input = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));  
            StringBuilder response = new StringBuilder();  
            String oneLine = null;  
            while((oneLine = input.readLine()) != null) {  
                response.append(oneLine + lineEnd);  
            } 
            stampTime=System.currentTimeMillis() - conn.getDate();
            Log.d(TAG, "Date="+conn.getDate()+"       System.currentTimeMillis()==="+System.currentTimeMillis()+"      stampTime====="+stampTime);
            return response.toString();
        } finally {  
            try {  
                if(output != null) {  
                    output.close();  
                }  
                if(input != null) {  
                    input.close();  
                }  
            } catch (IOException e) {  
                throw new RuntimeException(e);  
            }  
              
            if(conn != null) {  
                conn.disconnect();  
            }  
        }
    }
	
	

	public HttpConnection(String url) throws Exception {
		this.url = new URL(url);
	}

	public void setUrl(String url) throws Exception {
		this.url = new URL(url);
	}
	
	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addTextParameter(String name, String value) {
		textParams.put(name, value);
	}

	public void addFileParameter(String name, File value) {
		fileparams.put(name, value);
	}

	public void clearAllParameters() {
		textParams.clear();
		fileparams.clear();
	}

	public byte[] send(Map<String,File> files) throws Exception {
		initConnection();
		try {
			conn.connect();
		} catch (SocketTimeoutException e) {
			throw new RuntimeException();
		}
		ds = new DataOutputStream(conn.getOutputStream());
		writeFileParams(files);
		writeStringParams();
		paramsEnd();
		
		respondeCode=conn.getResponseCode();
		Log.d(TAG, "  respondeCode="+respondeCode);
		
		byte[] respBytes = null;
		if(respondeCode == 200){
			InputStream in = conn.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			conn.disconnect();
			respBytes = out.toByteArray();
		}
		return respBytes;
	}

	private void initConnection() throws Exception {
		conn = (HttpURLConnection) this.url.openConnection();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setConnectTimeout(10000); 
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary + "; charset=UTF-8");
		
		setHttpHeader();
	}
	
	private void setHttpHeader() throws Exception {
		for(Entry<String, String> headerEntry : headers.entrySet()) { 
    		conn.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
    	}
	}

	private void writeStringParams() throws Exception {
		Set<String> keySet = textParams.keySet();
		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
			String name = it.next();
			String value = textParams.get(name);
			ds.writeBytes("--" + boundary + "\r\n");
			ds.writeBytes("Content-Disposition: form-data; name=\"" + name
					+ "\"\r\n");
			ds.writeBytes("\r\n");
			ds.writeBytes(encode(value) + "\r\n");
			Log.d(TAG, "  name="+name+"  value="+value);
		}
	}

	private void writeFileParams(Map<String,File> files) throws Exception {
		
		String lineEnd="\r\n";
		String twoHyphens = "--";  
        for(Entry<String, File> fileEntry : files.entrySet()) {
            StringBuilder filePart = new StringBuilder();  
            filePart.append(twoHyphens).append(boundary).append(lineEnd); 
            filePart.append("Content-Disposition: form-data; name=\"").append(fileEntry.getKey()).append("\"; filename=\"").append(fileEntry.getValue().getName()).append("\"").append(lineEnd);  
            filePart.append("Content-Type: application/octet-stream").append(lineEnd);  
            filePart.append(lineEnd); 
            
            InputStream in = null;
            try {
                ds.write(filePart.toString().getBytes(("UTF-8")));
                byte[] tempbytes = new byte[1024];
                in = new FileInputStream(fileEntry.getValue());
                int readedBytes = 0;
                while ((readedBytes = in.read(tempbytes)) != -1) {
                    ds.write(tempbytes, 0, readedBytes);
                }   
                ds.writeBytes(lineEnd);  
            } finally{
            	if(in != null){
            		try {
						in.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
            	}
            }
        }
	}

	private byte[] getBytes(File f) throws Exception {
		FileInputStream in = new FileInputStream(f);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int n;
		while ((n = in.read(b)) != -1) {
			out.write(b, 0, n);
		}
		in.close();
		return out.toByteArray();
	}

	private void paramsEnd() throws Exception {
		ds.writeBytes("--" + boundary + "--" + "\r\n");
		ds.writeBytes("\r\n");
	}

	private String encode(String value) throws Exception {
		return URLEncoder.encode(value, "UTF-8");
	}

//	public static void main(String[] args) throws Exception {
	public static int upLoad(Context mContext,Map<String,File> files,String discrible,String contentID,int index) throws Exception
	{
		String appId = "iunios_feedback";
        String appKey = "5dsimrhgkpe5pdiictku0q8m6p";
        long timestamp=System.currentTimeMillis()-stampTime;
        String desc = appId + appKey + timestamp;
        String token = null;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(desc.getBytes());
            byte messageDigest[] = digest.digest();
           
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                String md5Hex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (md5Hex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(md5Hex);
            }
            token = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        Log.d(TAG, " add Header And Parameter");
        HttpConnection u=null;
        if(index==SUGGESTS)
        {
        	 u= new HttpConnection("http://i.iunios.com/feedback/suggests");
        }else if (index==FEEDBACK)
        {
        	 u = new HttpConnection("http://i.iunios.com/feedback/issues");
        }
        
		u.addHeader("User-Agent", "iunios_feedback");
		u.addHeader("Date", timestamp + "");
		u.addHeader("Authorization", "token " + token);        
        
        u.addTextParameter("imei", FeedBackUtil.getIMEIID(mContext));
        u.addTextParameter("deviceType",  FeedBackUtil.getDeviceModel());
        u.addTextParameter("resolution", "1440*2560");
        u.addTextParameter("platform", FeedBackUtil.getCpuInfo()[0]);
        u.addTextParameter("romVersion",  FeedBackUtil.getDeviceRelease());
        u.addTextParameter("desc", discrible);
        u.addTextParameter("contact", contentID);
		byte[] b = u.send(files);
		String result = new String(b);
		Log.d(TAG, " result="+result);
		return respondeCode;
	}
}
