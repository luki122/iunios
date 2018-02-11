package com.aurora.community.http.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.community.CommunityApp;
import com.aurora.community.http.HttpRequstData;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.SystemUtils;
import com.aurora.datauiapi.data.bean.Attachnfo;


public class HttpRequestGetData {

	private static final String TAG = "HttpRequestGetData";
	
	
	public static String getCategoryInfo() throws Exception{
		
	/*	StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("type", type);
			map.put("count", count);
			map.put("page", page);
			mapper.writeValue(str, map);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}*/

		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NAME_APPLIST,
				Globals.HTTP_APPLIST_METHOD);
		//url += Globals.HTTP_ACTION_PARAM + str.toString();
		//String url = "http://paopao.iuni.com/api/tag/discover";
		//String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(), "news_category.json");

		} else {
			returnData = HttpRequstData.doRequest(url);
		}
		return returnData;
	}
	public static String uploadPhoto(String fileName, String path,String color) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("resize_avatar_now", "1");
		map.put("base_color", color);
		

		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_UPLOAD_PHOTO,
				Globals.HTTP_UPLOAD_PHOTO_METHOD);



		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			 returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(),
			 "photos.json");

		} else {
			returnData = HttpRequstData.uploadFile(url, map, fileName, path);
		}

		return returnData;
	}
	public static String getNewsInfo() throws Exception{
		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(), "news.json");
		} 
		return returnData;
	}
	
	public static String getPublishOfUserCenter(int page,int count,String userId) throws Exception{
	
		String param = "?";
		param +="&pn="+page;
		param +="&ps="+count;
		
		//url += Globals.HTTP_ACTION_PARAM + str.toString();
		//String url = "http://paopao.iuni.com/api/tag/discover";
		//String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_PUBLISH,
				Globals.HTTP_PUBLISH_METHOD);
		url+="/"+userId;
		url += param;
//		Log.e("jadon", "url = "+url);
		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(), "publish_of_usercenter.json");
		} else {
			returnData = HttpRequstData.doRequest(url);
		}
//		Log.e("jadon", "returnData = "+returnData);
		return returnData;
	
	}
	
	public static String getArticleListInfo(String tid,int page,int count) throws Exception{
		
		String param = "?";
		param +="ps="+count;
		
		//url += Globals.HTTP_ACTION_PARAM + str.toString();
		//String url = "http://paopao.iuni.com/api/tag/discover";
		//String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NAME_APPLIST,
				Globals.HTTP_POSTS_METHOD);
		
	
		url+="/"+tid+"/"+page;
		url += param;
		
		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(), "publish_of_usercenter.json");

		} else {
			returnData = HttpRequstData.doRequest(url);
		}
		return returnData;
	
	}
	
	
	public static String getCollectionOfUserCenter(int page,int count,String userId)throws Exception{
		String param = "?";
		param +="&pn="+page;
		param +="&ps="+count;
		
		//url += Globals.HTTP_ACTION_PARAM + str.toString();
		//String url = "http://paopao.iuni.com/api/tag/discover";
		//String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_PUBLISH,
				Globals.HTTP_COLLECTION_METHOD);
		url+="/"+userId;
		url += param;
		String result = new String();
		if(Globals.isTestData){
			/**getting local data from assert*/
		}else{
			//TODO start getting data from server. 
			result = HttpRequstData.doRequest(url);	
		}
		return result;
	}
	
	public static String getPostDetail(String pid)throws Exception{
		String result = new String();
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_POST,
				Globals.HTTP_INDEX_METHOD);
		url+="/"+pid;
		if(Globals.isTestData){
			/**getting local data from assert*/
//			result =SystemUtils.getFromAssets(context, url);
		}else{
			//TODO start getting data from server. 
			result = HttpRequstData.doRequest(url);	
		}
		return result;
	}
	
	public static String getCommentList(String pid,int page,int count) throws Exception{
		String result = new String();
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_COMMENT,
				Globals.HTTP_GETLIST_METHOD);
		String param = "?";
		param +="&pid="+pid;
		param +="&page="+page;
		param +="&size="+count;
		url+=param;
		result = HttpRequstData.doRequest(url);	
		return result;		
	}
	
	
	public static String addComment(String pid,String commentContent,String replyCid)  throws Exception{
		String result = new String();
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_COMMENT,
				Globals.HTTP_ADD_COMMENT_METHOD);
		String param = "?";
		param +="&pid="+pid;
		param +="&replycid="+(replyCid==null?0:replyCid);
		param +="&content="+HttpRequstData.getDecodeStr(commentContent);
		url+=param;
		
		result = HttpRequstData.doRequest(url);	
		return result;		
	}
	
	public static String addFavour(String pid) throws Exception{
		String result = new String();
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_PUBLISH,
				Globals.HTTP_ADD_FAVOUR);
		String param = "?";
		param +="&id="+pid;
		url+=param;
		result = HttpRequstData.doRequest(url);	
		return result;		
	}
	
	public static String cancelFavour(String pid) throws Exception{
		String result = new String();
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_PUBLISH,
				Globals.HTTP_CANCEL_FAVOUR);
		String param = "?";
		param +="&id="+pid;
		url+=param;
		result = HttpRequstData.doRequest(url);	
		return result;		
	}
	
	
	public static String uploadArticle(String pid,String gid,ArrayList<Attachnfo> attachid,String content,String type,String tags)throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		map.put("type", type);
		String str ="";
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			str = mapper.writeValueAsString(attachid);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if(!TextUtils.isEmpty(pid))
			map.put("pid", pid);
//		if(!TextUtils.isEmpty(gid))
//			map.put("gid", gid);
		map.put("atts", str);
		map.put("content", content);
		map.put("tags", tags);
		//url += Globals.HTTP_ACTION_PARAM + str.toString();
		//String url = "http://paopao.iuni.com/api/tag/discover";
		//String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_POST,
				Globals.HTTP_UPLOAD_ARTICLE_METHOD);
		String result = new String();
		if(Globals.isTestData){
			/**getting local data from assert*/
		}else{
			//TODO start getting data from server. 
			result = HttpRequstData.doPost(map,url);	
		}
		return result;
	}
	public static String deletePost(String pid) throws Exception {
		String result = new String();
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_POST,
				Globals.HTTP_DELETE_POST_METHOD);
		url+="/"+pid;
		if(Globals.isTestData){
			/**getting local data from assert*/
//			result =SystemUtils.getFromAssets(context, url);
		}else{
			//TODO start getting data from server. 
			result = HttpRequstData.doRequest(url);	
		}
		return result;
	}
	
	public static String messageBox(final int pageCount,final String type,final String startId,final String startPage) throws Exception{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NOTICE,
				Globals.HTTP_LIST_METHOD));
		reqeustUrl.append("?");
//		param +="&id="+pid;
//		url+=param;
//		result = HttpRequstData.doRequest(url);	
		
		reqeustUrl.append("&ps="+pageCount);
		if(type != null)
		{
			reqeustUrl.append("&type="+type);
		}
		
		if(startId != null)
		{
			reqeustUrl.append("&id="+startId);
		}
		
		if(startPage != null)
		{
			reqeustUrl.append("&pn="+startPage);
		}
		result = HttpRequstData.doRequest(reqeustUrl.toString());	
		return result;
	}
	
	public static String messageReadAll() throws Exception{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NOTICE,
				Globals.HTTP_MESSAGE_READALL));
		result = HttpRequstData.doRequest(reqeustUrl.toString());	
		return result;
	}
	
	public static String deleteAllMessage() throws Exception{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NOTICE,
				Globals.HTTP_MESSAGE_CLEAR));
		result = HttpRequstData.doRequest(reqeustUrl.toString());
		return result;
	}
	
	public static String deleteMessage(String nid) throws Exception{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
				Globals.HTTP_SERVICE_NOTICE,
				Globals.HTTP_MESSAGE_DELETE));
		reqeustUrl.append("?&nid="+nid);
		result = HttpRequstData.doRequest(reqeustUrl.toString());
		return result;
	}
	
}
