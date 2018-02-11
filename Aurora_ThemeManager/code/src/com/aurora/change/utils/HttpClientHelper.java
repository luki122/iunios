package com.aurora.change.utils;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class HttpClientHelper {
	
	public static String httpClientPost(String url, JSONObject obj) {
		String result = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost mHttpPost = new HttpPost(url);
//            HttpPost mHttpPost = new HttpPost("http://adstest.virtual.iunios.com/app/init");
//            HttpPost request = new HttpPost("http://i.iunios.com/app/init");
            
            //添加http头信息
//            mHttpPost.addHeader("Authorization", "your token"); //认证token
            mHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
//            mHttpPost.addHeader("User-Agent", "imgfornote");
            
//            obj.put("imei", "008600215140400");
            mHttpPost.setEntity(new StringEntity(obj.toString()));
            
            HttpResponse response = client.execute(mHttpPost);
            if (response == null) return result;
            
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ) {
            	Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientPost-----failed!!!");
            	return result;
            }
                        
            result = EntityUtils.toString(response.getEntity());
//            Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientPost-----result = "+result);
            
//            JSONObject jsonObject = new JSONObject(result);
//            Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientPost-----result = "+result);
            
            /*in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientPost-----in = "+in);
            
            StringBuffer string = new StringBuffer("");
            String lineStr = "";
            while ((lineStr = in.readLine()) != null) {
                string.append(lineStr + "\n");
            }
            in.close();*/

            
        } catch(Exception e) {
            // Do something about exceptions
        	Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientPost-----Exception = "+e);
        }/* finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
        
        return result;
	}
	
	public static String httpClientGet(String uri, String token, String agent, String date) {
		String result = null;
		try {
            //得到HttpClient对象
            HttpClient getClient = new DefaultHttpClient();
            
            //得到HttpGet对象
            HttpGet mHttpGet = new HttpGet(uri);
            //添加http头信息
            mHttpGet.addHeader("User-Agent", agent);
            mHttpGet.addHeader("Date", date);
            mHttpGet.addHeader("Authorization", "token " + token); //认证token
            
            //客户端使用GET方式执行请教，获得服务器端的回应response
            HttpResponse response = getClient.execute(mHttpGet);
            //判断请求是否成功
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d("Wallpaper_DEBUG", "请求服务器端失败");
                return result;
            }/* else {
            	 //获得输入流
                InputStream  inStrem = response.getEntity().getContent();
                int read = inStrem.read();
                while (read != -1){
                    read = inStrem.read();
                }
                
                Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientGet-----result = "+result);
                //关闭输入流
                inStrem.close();
            }*/
            result = EntityUtils.toString(response.getEntity());
//            Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientGet-----result = "+result);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
        	Log.d("Wallpaper_DEBUG", "HttpClientHelper-----------httpClientGet-----Exception = "+e);
        }
		return result;
	}
	
}
