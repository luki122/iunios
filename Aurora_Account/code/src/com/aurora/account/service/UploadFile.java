package com.aurora.account.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UploadFile {
	String multipart_form_data = "multipart/form-data";  
    String twoHyphens = "--";  
    String boundary = "****************fD4fH3gL0hK7aI6";
    String lineEnd = System.getProperty("line.separator"); 
  
    private void addImageContent(File file, DataOutputStream output) {
            StringBuilder split = new StringBuilder();  
            split.append(twoHyphens + boundary + lineEnd);  
            split.append("Content-Disposition: form-data; name=\"uploadForm\"; filename=\"gift.JPG\"" + lineEnd);  
            split.append("Content-Type: multipart/form-data; boundary=" + boundary + lineEnd);  
            split.append(lineEnd); 
            
            InputStream in = null;
            try {
                output.writeBytes(split.toString());
                byte[] tempbytes = new byte[200];
                in = new FileInputStream(file);
                while (in.read(tempbytes) != -1) {
                    output.write(tempbytes, 0, tempbytes.length);
                }  
                output.writeBytes(lineEnd);  
            } catch (IOException e) {  
                throw new RuntimeException(e);  
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
    
    private void addFormField(Map<String,Object> params, DataOutputStream output) { 
    	
        StringBuilder sb = new StringBuilder();  
        for(Map.Entry<String, Object> param : params.entrySet()) {  
            sb.append(twoHyphens + boundary + lineEnd);  
            sb.append("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + lineEnd);  
            sb.append(lineEnd);  
            sb.append(param.getValue() + lineEnd);  
        }  
        try {  
            output.writeBytes(sb.toString()); 
        } catch (IOException e) {  
            throw new RuntimeException(e);  
        }  
    }  
      
    public String post(String actionUrl, Map<String, Object> params, File file) {  
        HttpURLConnection conn = null;  
        DataOutputStream output = null;  
        BufferedReader input = null;
        
        try {  
            URL url = new URL(actionUrl);  
            conn = (HttpURLConnection) url.openConnection();  
            conn.setConnectTimeout(120000);  
            conn.setDoInput(true); 
            conn.setDoOutput(true); 
            conn.setUseCaches(false);  
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Connection", "keep-alive");  
            conn.setRequestProperty("Content-Type", multipart_form_data + "; boundary=" + boundary);  
              
            conn.connect();  
            output = new DataOutputStream(conn.getOutputStream()); 
            addFormField(params, output);
            addImageContent(file, output);  
              
            
              
            output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);  
            output.flush();  
              
            int code = conn.getResponseCode();  
            if(code != 200) {  
                throw new RuntimeException("请求‘" + actionUrl +"’失败！");  
            }  
              
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
            StringBuilder response = new StringBuilder();  
            String oneLine;  
            while((oneLine = input.readLine()) != null) {  
                response.append(oneLine + lineEnd);  
            }  
              
            return response.toString();  
        } catch (IOException e) {  
            throw new RuntimeException(e);  
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

	public static void main(String[] args) {
		try {  
            String response = "";  
            
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userId", 12345678);
            params.put("userKey", "107b9cfff6c91dea5cee7f7fc6912c7e");
            params.put("part", "sms");
            
            File file = new File("D:\\work home\\Docs\\gift.JPG");
              
            response = new UploadFile().post("http://dev.ucloud.iunios.com/sync?module=attachment&action=upload", params, file);  
            System.out.println("返回结果：" + response);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
	}

}
