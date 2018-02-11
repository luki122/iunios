package com.aurora.ota.reporter;

import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONObject;
import com.aurora.ota.database.DataBaseCreator;
import com.aurora.ota.database.DataBaseCreator.RepoterColumns;
import com.aurora.ota.reporter.ReporterThread.Error;


/**
 * 
 * 类名称：SendDBThread
 * 类描述：发送统计的数据库文件到服务器
 * 创建时间：2015-2-2 下午5:34:06
 * @author jiyouguang
 *
 */
public class SendDBThread extends Thread {
	
	private static  final String tag = "SendDBThread"; 
    private String urlStr = "";
    private Context mContext;
    private CallBack callback;
    private static final String REPORTER_ADDR ="http://data.iunios.com/dp/storageDBfFile";
    
    private NetFocade mNetFacade;
    
    
    public SendDBThread(Context context,NetFocade netFacade){
    	this.mContext = context;
    	this.mNetFacade = netFacade;
    }

    public interface CallBack {
        void error(Error error);

        void success();
        void interupt(Error error);

    }
	@Override
	public void run() {
	    if (!mNetFacade.isWIFIConnection()) { //只在wifi环境下才上传
	    	if(null != callback){
            callback.error( Error.NET_WORK_ERROR);
	    	}
            return;
        }
		boolean result = uploadFile();
		if(result){
//			SQLiteDatabase DB = mContext.openOrCreateDatabase(DataBaseCreator.DB_NAME,Context.MODE_PRIVATE, null);
//			try{
//				DB.execSQL("update "+DataBaseCreator.Tables.DB_MODULE_TABLE+" set "+DataBaseCreator.RepoterColumns.KEY_VALUE+"=0");
//				/*DB.execSQL("DELETE FROM "+DataBaseCreator.Tables.DB_MODULE_TABLE);
//				DB.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = '"+DataBaseCreator.Tables.DB_MODULE_TABLE+"'");//自增列归零
//*/				
//				LogUtils.log(tag, "clean module DB ! ! !");
//			}catch(Exception e){
//				LogUtils.log(tag, "init module DB have exception ! ! !");
//			}
            if (callback != null) {
                callback.success();
            }
		} else {
			if (callback != null) {
                callback.error(Error.NET_WORK_ERROR);
            }
		}
	
	}
	
	public void registerCallBack(CallBack callback) {
        this.callback = callback;
    }
	
    private boolean uploadFile() {
    	
    	Upload u;
		try {
			u = new Upload(REPORTER_ADDR);
			 LogUtils.logd(tag, "request url : " +REPORTER_ADDR);
		} catch (Exception e) {
			return false;
		}
		SQLiteDatabase db = mContext.openOrCreateDatabase(DataBaseCreator.DB_NAME, android.content.Context.MODE_PRIVATE, null);
		u.addFileParameter("dataFile", new File(db.getPath()));
		u.addTextParameter("imei", Util.getImei(mContext));
		u.addTextParameter("phone",Util.getModel());
		u.addTextParameter("version",Util.getInternalVersion());
		byte[] b;
		try {
			b = u.send();
		} catch (Exception e) {
			return false;
		}
		LogUtils.logd(tag, "SendDB end ... " );
		String result = new String(b);
		try {
			return parseItem(result);
		} catch (Exception e) {
			return false;
		}
    	/*
        boolean result = false;
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        HttpURLConnection con = null;
        FileInputStream fStream = null;
        DataOutputStream ds = null;
        try {
            urlStr = REPORTER_ADDR;
            urlStr = urlStr.replace(" ", "");
            LogUtils.logd(tag, urlStr);
            if (TextUtils.isEmpty(urlStr)) {
                return result;
            }
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();

		     允许Input、Output，不使用Cache 
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

	         设定传送的method=POST 
            con.setRequestMethod("POST");
	      
	         setRequestProperty 
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            //con.setRequestProperty("Content-Range", "bytes " + beginBytePosition + "-3000/" + totalBytes);
            con.setConnectTimeout(10000);
	            
	         设定DataOutputStream 
            ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " + "name=\"dataFile\";filename=\"" + DataBaseCreator.DB_NAME + "\"" + end);
            ds.writeBytes("Content-Type: " + "application/octet-stream" + "\r\n");
            ds.writeBytes(end);

	         取得文件的FileInputStream 
            SQLiteDatabase db = mContext.openOrCreateDatabase(DataBaseCreator.DB_NAME, android.content.Context.MODE_PRIVATE, null);
            fStream = new FileInputStream(db.getPath());            
	        
	         设定每次写入1024bytes 
            byte[] buffer = new byte[BUFFER_SIZE];

	         从文件读取数据到缓冲区 
            int length = -1;
            while ((length = fStream.read(buffer)) != -1) {
		         将数据写入DataOutputStream中 
                ds.write(buffer, 0, length);
          
            } 
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            ds.writeBytes(end);
            ds.flush();
            LogUtils.logd(tag,"response code : "  + con.getResponseCode());
            result = this.DoThing(con.getInputStream());
        }catch (Exception e) {
            e.printStackTrace();
            LogUtils.logd(tag, e.toString());
        } finally {
            try {
				ds.close();
				fStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return result;
    */
    	}

	
    private boolean DoThing(InputStream in) {
        try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                builder.append(s);
            }

            String str = builder.toString();
            LogUtils.logd(tag, str);
            return parseItem(str);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseItem(String str) throws Exception {
    	
        JSONObject json =new JSONObject(str);
        if (json != null && !str.isEmpty()) {

               return json.getInt("state") == 1 ? true : false;
            }
     
        return false;
    }

}
