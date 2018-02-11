package com.android.phone;  
  
import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;   
import java.net.HttpURLConnection;  
import java.net.URL; 
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
  
public class ImageUtils {  
    private static final String TAG = "ImageUtils";
    /* 
     * 从数据流中获得数据 
     */  
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        while((len = inputStream.read(buffer)) != -1) {  
            bos.write(buffer, 0, len);  
        }  
        bos.close();  
        return bos.toByteArray();  
          
    }  
    
    
    public static Bitmap getImage(String path) {  
        try { 
	        URL url = new URL(path);  
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
	        conn.setRequestMethod("GET");   //设置请求方法为GET  
	        conn.setReadTimeout(3*1000);    //设置请求过时时间为5秒  
	        InputStream inputStream = conn.getInputStream();   //通过输入流获得图片数据     //获得图片的二进制数据  
            byte[] data = readInputStream(inputStream);
	        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	        inputStream.close();
			Log.i(TAG, " bitmap = " + bitmap);
	        return bitmap;
        } catch (Exception e) {  
            Log.i(TAG, e.toString());  
        }  
        return null;
    }  
    

}  