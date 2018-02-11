package com.aurora.account.contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;

import com.aurora.account.bean.accessoryInfo;
import com.aurora.account.bean.accessoryObj;
import com.aurora.account.bean.syncDataItemObject;
import com.aurora.account.bean.syncDataObject;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Log;
import com.aurora.datauiapi.data.bean.InitMapInfo;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountsAdapter {
	
	private static final String TAG = "AccountsAdapter";
	private HashMap<String, String> syncUri = new HashMap<String, String>();
	private ContentResolver resolver = null;

	private String m_packageName,m_uri;
	
	public AccountsAdapter(Context context,String packageName,String uri) {
		resolver = context.getContentResolver();
		syncUri.put(packageName, uri);
		this.m_packageName = packageName;
		this.m_uri = uri;
	}
	
	/**
	 * 清除指定包的本地数据
	 * @return
	 */
	public boolean clearLocalData() {
	    Uri baseUri = Uri.parse(syncUri.get(m_packageName));
        Uri clearLocalDataUri = Uri.withAppendedPath(baseUri, "clean_data"); // 清除数据
        try {
            int deletedRows = resolver.delete(clearLocalDataUri, null, null);
            Log.d(TAG, "Jim, delete " + deletedRows + " records for " + m_packageName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
	    return false;
	}
	
	/**
	 * 清除数据的同步Id
	 * @return
	 */
	public boolean clearLocalDataSyncId() {
	    Uri baseUri = Uri.parse(syncUri.get(m_packageName));
        Uri clearSyncIdUri = Uri.withAppendedPath(baseUri, "clean_account"); // 清除同步Id
        try {
            int updatedRows = resolver.delete(clearSyncIdUri, null, null);
            Log.d(TAG, "Jim, update " + updatedRows + " records for " + m_packageName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
	}
	
	public long getSyncCount(){
		Uri baseUri = Uri.parse(syncUri.get(m_packageName));
		long count = 0;
		Uri uri = Uri.withAppendedPath(baseUri, "sync_up_size");
		Cursor cursor = resolver.query(uri, null, null, null, null);
		if(cursor != null){
			if(cursor.moveToFirst()){
				count = cursor.getLong(0);
			}
			cursor.close();
		}
		return count;
	}
	public String getisFirstSync(){
		Uri baseUri = Uri.parse(syncUri.get(m_packageName));
		String isFirst = "";
		Uri uri = Uri.withAppendedPath(baseUri, "is_first_sync");
		Cursor cursor = resolver.query(uri, null, null, null, null);
		if(cursor != null){
			if(cursor.moveToFirst()){
				isFirst = cursor.getString(0);
			}
			cursor.close();
		}
		return isFirst;
	}
	private void updateSyncReset() {
		Uri baseUri = Uri.parse(syncUri.get(m_packageName));
		Uri syncUpUri = Uri.withAppendedPath(baseUri, "sync"); // 获取全部
		ContentValues values = new ContentValues();
		values.put("dirty", 1);
		resolver.update(syncUpUri, values, null, null);
	}
	
	public syncDataObject syncUp(String serverTime,
			String timeStamp,int currentCount,int perCount) {
		syncDataObject obj = new syncDataObject();
		Uri baseUri = Uri.parse(syncUri.get(m_packageName));
		// 获取数据接口

		Uri syncUpUri = Uri.withAppendedPath(baseUri, "sync_up/"+currentCount+"/"+perCount); // 获取全部

		Builder builder = syncUpUri.buildUpon();
		// 具体参数请确定
		builder.appendQueryParameter("serverTime", serverTime);
		builder.appendQueryParameter("timeStamp", timeStamp);
		Cursor cursor = resolver.query(builder.build(), null, null, null, null);
		//Log.d(TAG, "cursor = " + cursor);
		if (cursor != null) {
			// 获取数据数量接口
			//long count = getSyncCount();//cursor.getExtras().getLong("size", -1);
			//Log.d(TAG, "count = " + count);

			while (cursor.moveToNext()) {
				syncDataItemObject synccitemdata = new syncDataItemObject();

				String body = cursor.getString(0);
				synccitemdata.setBody(body);
				//Log.d(TAG, "body = " + body);
				String accessory = cursor.getString(1);
				Log.d(TAG, "zhangwei the accessory = " + accessory);
				FileLog.d(TAG, "zhangwei the syncUp accessory = " + accessory);
				FileLog.d(TAG, "zhangwei the syncUp body = " + body);
				try {
					String add_str = cursor.getString(2);
					ObjectMapper mapper1 = new ObjectMapper();
					JsonNode rootNode = mapper1.readTree(add_str);
					String op = rootNode.path("op").getValueAsText();
					String id = rootNode.path("id").getValueAsText();
					String date = rootNode.path("date").getValueAsText();
					synccitemdata.setOp(op);
					synccitemdata.setId(id);
					synccitemdata.setDate(date);
					//Log.d(TAG, "id = " + id);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(null != accessory)
				{
					accessoryObj access = new accessoryObj();
	
					try {
						ObjectMapper mapper = new ObjectMapper();
						/*
						 * access = mapper.readValue(accessory, new
						 * TypeReference<List<accessoryInfo>>() { });
						 */
		
						
						access = mapper.readValue(accessory, accessoryObj.class);
	
						synccitemdata.setAccOjb(access);
					
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				obj.getSycndata().add(synccitemdata);
			}

			cursor.close();
		}

		//

		// getStreamByAccessory(packageName,obj.getSycndata().get(0).getAccOjb().getAccessory().get(0));
		//upResultUpdata(packageName);
		return obj;
	}
	
	public String getNewPath(String packageName, String syncid, String path) {
		Uri baseUri = Uri.parse(syncUri.get(packageName));
		Uri accessoryUri = Uri.withAppendedPath(baseUri, "accessory");
		return getNewPath(accessoryUri, syncid, path);
	}

	private String getNewPath(Uri accessoryUri, String syncid, String path) {
		Builder accessoryUriBuilder1 = accessoryUri.buildUpon();
		accessoryUriBuilder1.appendQueryParameter("syncid", syncid);
		accessoryUriBuilder1.appendQueryParameter("path", path);
		Cursor cursor = resolver.query(accessoryUriBuilder1.build(), null,
				null, null, null); // 获取文件名
		String newPath = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				newPath = cursor.getString(0);
			}
			cursor.close();
		}
		return newPath;
	}

	public InputStream getInputStreamByAccessory(String packageName,
			accessoryInfo accInfo) {
		InputStream in = null;
		Uri baseUri = Uri.parse(syncUri.get(packageName));
		Uri accessoryUri = Uri.withAppendedPath(baseUri, "accessory");

		String accessoryid = accInfo.getAccessoryid();
		String path = accInfo.getPath();
		String type = accInfo.getType();
		if (type != null && accessoryid != null && path != null) {
			if (type.equals("providerFile")) {
				Builder accessoryUriBuilder = accessoryUri.buildUpon();

				accessoryUriBuilder.appendQueryParameter("adccessoryid",
						accessoryid);
				accessoryUriBuilder.appendQueryParameter("path", path);
				try {
					in = resolver.openInputStream(accessoryUriBuilder.build());
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			} else if (type.equals("commenFile")) {

			}

		} else {
			return null;
		}

		return in;
	}

	public OutputStream getOutputStreamByAccessory(String packageName,
			accessoryInfo accInfo) {
		OutputStream out = null;
		Uri baseUri = Uri.parse(syncUri.get(packageName));
		Uri accessoryUri = Uri.withAppendedPath(baseUri, "accessory");
		String syncid = accInfo.getSyncid();
		String path = accInfo.getNew_path();
		String type = accInfo.getType();
		if (syncid != null && type != null && path != null) {
			if (type.equals("providerFile")) {
				Builder accessoryUriBuilder = accessoryUri.buildUpon();

				accessoryUriBuilder.appendQueryParameter("syncid", syncid);

				accessoryUriBuilder.appendQueryParameter("path", path);
				try {
					out = resolver
							.openOutputStream(accessoryUriBuilder.build());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			} else if (type.equals("commenFile")) {
				
			}

		} else {
			return null;
		}

		return out;
	}

	public boolean upResultDowndata(String packageName, syncDataObject sync_obj) {
		// 如果想多条一起发回来
		Uri baseUri = Uri.parse(syncUri.get(packageName));
		Uri syncUpResultUri = Uri.withAppendedPath(baseUri, "sync_down_multi");
		/*
		 * JSONArray array = new JSONArray(); for (int i = 0; i < 7; i++) {
		 * JSONObject object = new JSONObject(); try { object.put("body",
		 * "**body**"); } catch (JSONException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } try { object.put("accessory",
		 * "**accessory**"); } catch (JSONException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } array.put(object); }
		 */
		/*for (syncDataItemObject item : sync_obj.getSycndata()) {
			for (accessoryInfo info : item.getAccOjb().getAccessory()) {
				Log.i(TAG, "write path: " + info.getNew_path());
			}
		}*/
		
		String access = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			/*
			 * access = mapper.readValue(accessory, new
			 * TypeReference<List<accessoryInfo>>() { });
			 */
//			access = mapper.writeValueAsString(tmpobj.getSycndata());
			access = mapper.writeValueAsString(sync_obj.getSycndata());
			FileLog.i(TAG, "zhangwei upResultDowndata the size ="+sync_obj.getSycndata().size());
			FileLog.i(TAG, "zhangwei upResultDowndata the access ="+access);
		} catch (IOException e) {
			Log.i(TAG, "zhangwei upResultDowndata the error ="+e.getMessage());
			e.printStackTrace();
		}
		ContentValues values = new ContentValues();
		values.put("multi", access);
		Uri uri = resolver.insert(syncUpResultUri, values);
		String result =  uri.getLastPathSegment();
		Log.i(TAG, "zhangwei upResultDowndata the result ="+result);
		FileLog.i(TAG, "zhangwei upResultDowndata the result ="+result);
		if(result.equals("true"))
			
			return true;
		else
			return false;
	}

	public boolean upResultUpdata(String packageName,syncDataObject sync_obj) {
		// 如果想多条一起发回来
		Uri baseUri = Uri.parse(syncUri.get(packageName));
		Uri syncUpResultUri = Uri.withAppendedPath(baseUri,
				"sync_up_result_multi");
		/*
		 * JSONArray array = new JSONArray(); for (int i = 0; i < 7; i++) {
		 * JSONObject object = new JSONObject(); try { object.put("body",
		 * "**body**"); } catch (JSONException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } try { object.put("accessory",
		 * "**accessory**"); } catch (JSONException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } array.put(object); }
		 */
		String access = null;
		try {
			ObjectMapper mapper = new ObjectMapper();

			
			access = mapper.writeValueAsString(sync_obj);
			
			Log.i(TAG, "zhangwei the upResultUpdata ="+access);
			FileLog.i(TAG, "zhangwei the upResultUpdata ="+access);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ContentValues values = new ContentValues();
		values.put("multi", access);
		Uri uri = resolver.insert(syncUpResultUri, values);
		String result =  uri.getLastPathSegment();
		Log.i(TAG, "zhangwei upResultUpdata the result ="+result);
		FileLog.i(TAG, "zhangwei upResultUpdata the result ="+result);
		if(result.equals("true"))
			return true;
		else
			return false;
	
	}
	
	public List<String> getList(String packageName, List<InitMapInfo> init_map)
	{
		Uri baseUri = Uri.parse(syncUri.get(packageName));
		Uri syncUpResultUri = Uri.withAppendedPath(baseUri, "init_account_multi");

		String access = null;
		try {
			ObjectMapper mapper = new ObjectMapper();

			access = mapper.writeValueAsString(init_map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ContentValues values = new ContentValues();
		values.put("multi", access);
		Uri uri = resolver.insert(syncUpResultUri, values);
		List<String> m_ids =  uri.getQueryParameters("results");
		
		return m_ids;
	}
	
	public List<String> initmapdata(String packageName, ArrayList<InitMapInfo> init_map) {
		// 如果想多条一起发回来
	/*	ArrayList<InitMapInfo> tmp_map = new ArrayList<InitMapInfo>();
		for(int i = 0; i < 10; i++)
		{
			tmp_map.add(init_map.get(i));
		}*/
		List<String> list = new ArrayList<String>();
		Log.i("zhangwei", "zhangwei the size="+init_map.size());
		int size = init_map.size();
		if(size <= 1000)
		{
			list = getList(packageName,init_map);
		
		}
		else
		{
			
			int index = 0;
			List<InitMapInfo> tmp_map = new ArrayList<InitMapInfo>();
			
			
			while(index < size)
			{
			  if(index+1000 < size)	
			  {
				  tmp_map.clear();
				  for(int i = index; i < index+1000; i++)
				  {
					  tmp_map.add(init_map.get(i));
				  }
				  //tmp_map = init_map.subList(index, index+1000);
				  list.addAll(getList(packageName,tmp_map));
				  index += 1000;
			  }
			  else
			  {
				  tmp_map = init_map.subList(index,size);
				  list.addAll(getList(packageName,tmp_map));
				  break;
			  }
			  
			}
		}
		
		
		Log.i(TAG, "zhangwei test packageName="+packageName);
		Log.i(TAG, "zhangwei test="+list.toString());
		//FileLog.i(TAG, "zhangwei test="+list.toString());
		return list;
	}
}