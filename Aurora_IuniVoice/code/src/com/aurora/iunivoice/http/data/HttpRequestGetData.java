package com.aurora.iunivoice.http.data;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.http.HttpRequstData;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ObjectToJsonUtil;
import com.aurora.iunivoice.utils.SystemUtils;


public class HttpRequestGetData {

    private static final String TAG = "HttpRequestGetData";
    
    public static String getHomepageListData(int page, int tpp) throws Exception {
    	StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			map.put("page", String.valueOf(page));
			map.put("tpp", String.valueOf(tpp));
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = Globals.HTTP_HOMEPAGE_LIST;
		return HttpRequstData.doPost(map, url);
    }

	public static String getForumData() throws Exception {
		StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = Globals.HTTP_FORUM_LIST;
		return HttpRequstData.doPost(map, url);
	}

	public static String getPostData(String fid, int page, int tpp) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			map.put("fid", fid);
			map.put("page", String.valueOf(page));
			map.put("tpp", String.valueOf(tpp));
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = Globals.HTTP_FORUM_DISPLAY;
		return HttpRequstData.doPost(map, url);
	}
	
	public static String signDaily(String formhash) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			map.put("formhash", formhash);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = Globals.HTTP_SIGN_DAILY;
		return HttpRequstData.doPost(map, url);
	}
	
	public static String publish(String formhash, String subject, String message,
    		String fid, int[] attachnew) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			map.put("formhash", formhash);
			map.put("subject", subject);
			map.put("message", message);
			map.put("fid", fid);
			map.put("topicsubmit", "yes");
			
			if (attachnew != null) {
				for (int i = 0; i < attachnew.length; i++) {
					map.put("attachnew[" + attachnew[i] + "]", String.valueOf(attachnew[i]));
				}
			}
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		String url = Globals.HTTP_PUBLISH;
		return HttpRequstData.doPost(map, url);
	}
	
	public static String uploadImageFile(String fileName, String path) throws Exception {
		AccountHelper mAccountHelper = AccountHelper.getInstance(IuniVoiceApp.getInstance());
		StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			map.put("uid", mAccountHelper.user_id);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		String url = Globals.HTTP_UPLOAD_IMAGE_FILE;
		return HttpRequstData.uploadImageFile(url, map, fileName, path);
	}
	

//    public static String getCategoryInfo() throws Exception {
//
//	/*	StringWriter str = new StringWriter();
//        Map<String, Object> map = new HashMap<String, Object>();
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			map.put("type", type);
//			map.put("count", count);
//			map.put("page", page);
//			mapper.writeValue(str, map);
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//			return null;
//		} catch (JsonMappingException e) {
//			e.printStackTrace();
//			return null;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}*/
//
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_NAME_APPLIST,
//                Globals.HTTP_APPLIST_METHOD);
//        //url += Globals.HTTP_ACTION_PARAM + str.toString();
//        //String url = "http://paopao.iuni.com/api/tag/discover";
//        //String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
//        String returnData = new String();
//        if (Globals.isTestData) {
//            // 先使用模拟数据
//            returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(), "news_category.json");
//
//        } else {
//            returnData = HttpRequstData.doRequest(url);
//        }
//        return returnData;
//    }

//    public static String uploadPhoto(String fileName, String path, String color) throws Exception {
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("resize_avatar_now", "1");
//        map.put("base_color", color);
//
//
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_UPLOAD_PHOTO,
//                Globals.HTTP_UPLOAD_PHOTO_METHOD);
//
//
//        String returnData = new String();
//        if (Globals.isTestData) {
//            // 先使用模拟数据
//            returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(),
//                    "photos.json");
//
//        } else {
//            returnData = HttpRequstData.uploadFile(url, map, fileName, path);
//        }
//
//        return returnData;
//    }

    public static String getNewsInfo() throws Exception {
        String returnData = new String();
        if (Globals.isTestData) {
            // 先使用模拟数据
            returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(), "news.json");
        }
        return returnData;
    }

//    public static String getPublishOfUserCenter(int page, int count, String userId) throws Exception {
//
//        String param = "?";
//        param += "&pn=" + page;
//        param += "&ps=" + count;
//
//        //url += Globals.HTTP_ACTION_PARAM + str.toString();
//        //String url = "http://paopao.iuni.com/api/tag/discover";
//        //String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_PUBLISH,
//                Globals.HTTP_PUBLISH_METHOD);
//        url += "/" + userId;
//        url += param;
////		Log.e("jadon", "url = "+url);
//        String returnData = new String();
//        if (Globals.isTestData) {
//            // 先使用模拟数据
//            returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(), "publish_of_usercenter.json");
//        } else {
//            returnData = HttpRequstData.doRequest(url);
//        }
////		Log.e("jadon", "returnData = "+returnData);
//        return returnData;
//
//    }

//    public static String getArticleListInfo(String tid, int page, int count) throws Exception {
//
//        String param = "?";
//        param += "ps=" + count;
//
//        //url += Globals.HTTP_ACTION_PARAM + str.toString();
//        //String url = "http://paopao.iuni.com/api/tag/discover";
//        //String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_NAME_APPLIST,
//                Globals.HTTP_POSTS_METHOD);
//
//
//        url += "/" + tid + "/" + page;
//        url += param;
//
//        String returnData = new String();
//        if (Globals.isTestData) {
//            // 先使用模拟数据
//            returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(), "publish_of_usercenter.json");
//
//        } else {
//            returnData = HttpRequstData.doRequest(url);
//        }
//        return returnData;
//
//    }
//
//
//    public static String getCollectionOfUserCenter(int page, int count, String userId) throws Exception {
//        String param = "?";
//        param += "&pn=" + page;
//        param += "&ps=" + count;
//
//        //url += Globals.HTTP_ACTION_PARAM + str.toString();
//        //String url = "http://paopao.iuni.com/api/tag/discover";
//        //String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_PUBLISH,
//                Globals.HTTP_COLLECTION_METHOD);
//        url += "/" + userId;
//        url += param;
//        String result = new String();
//        if (Globals.isTestData) {
//            /**getting local data from assert*/
//        } else {
//            //TODO start getting data from server.
//            result = HttpRequstData.doRequest(url);
//        }
//        return result;
//    }
//
//    public static String getPostDetail(String pid) throws Exception {
//        String result = new String();
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_POST,
//                Globals.HTTP_INDEX_METHOD);
//        url += "/" + pid;
//        if (Globals.isTestData) {
//            /**getting local data from assert*/
////			result =SystemUtils.getFromAssets(context, url);
//        } else {
//            //TODO start getting data from server.
//            result = HttpRequstData.doRequest(url);
//        }
//        return result;
//    }
//
//    public static String getCommentList(String pid, int page, int count) throws Exception {
//        String result = new String();
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_COMMENT,
//                Globals.HTTP_GETLIST_METHOD);
//        String param = "?";
//        param += "&pid=" + pid;
//        param += "&page=" + page;
//        param += "&size=" + count;
//        url += param;
//        result = HttpRequstData.doRequest(url);
//        return result;
//    }

    public static String addComment(String pid, String commentContent, String fid,
                                    String hashId) throws Exception {
        StringWriter str = new StringWriter();
        Map<String, String> map = new HashMap<String, String>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map.put(Globals.COMMENT_IUNI_BBS_IDENTIFY, Globals.COMMENT_YES);
            map.put(Globals.COMMENT_REPLY_SUBMIT, Globals.COMMENT_YES);
            map.put(Globals.COMMENT_FID, fid);
            map.put(Globals.COMMENT_TID, pid);
            map.put(Globals.COMMENT_FORM, hashId);
            map.put(Globals.COMMENT_MESSAGE_CONTENT, commentContent);
            mapper.writeValue(str, map);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String action = "";
        String url = HttpRequstData.getURLStr(Globals.HTTP_PAGE_REQUEST_URL,
                Globals.MODULE_SEND_REPLY, action);

        String returnData;
        if (Globals.isTestData) {
            // 先使用模拟数据
            returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(),
                    "login.json");
        } else {
            //returnData = HttpRequstData.doHttpsRequest(url, str.toString());
            returnData = HttpRequstData.doPost(map, url);
        }
        return returnData;
        //return HttpRequstData.doPost(map, url);
    }

    public static String addFavour(String pid, String hash) throws Exception {
        String url = Globals.HTTP_REQUEST_FAVOR_URL;
        Map<String, String> map = new HashMap<String, String>();
        map.put(Globals.COMMENT_IUNI_BBS_IDENTIFY, "yes");
        map.put(Globals.COMMENT_TID, pid);
        map.put(Globals.COMMENT_HASH, hash);

        return HttpRequstData.doPost(map, url);
    }

    public static String getDetailInfo(String pid, String hash) throws Exception {
        String url = Globals.HTTP_DETAIL_QUERY_URL;
        Map<String, String> map = new HashMap<String, String>();
        map.put(Globals.COMMENT_IUNI_BBS_IDENTIFY, "yes");
        map.put(Globals.COMMENT_TID, pid);
        map.put(Globals.COMMENT_FORM, hash);

        return HttpRequstData.doPost(map, url);
    }

    public static String addScore(String fid, String tid, String hash, String reason, String score) throws Exception {
        String url = Globals.HTTP_REQUEST_SCORE_URL;
        Map<String, String> map = new HashMap<String, String>();
        map.put(Globals.COMMENT_IUNI_BBS_IDENTIFY, Globals.COMMENT_YES);
        map.put(Globals.COMMENT_FORM, hash);
        map.put(Globals.COMMENT_RATE, Globals.COMMENT_YES);
        map.put(Globals.COMMENT_PLAT, "1");
        map.put(Globals.COMMENT_SCORE, score);
        map.put(Globals.COMMENT_REASON, reason);
        //map.put(Globals.COMMENT_FID, fid);
        map.put(Globals.COMMENT_TID, tid);//tid

        return HttpRequstData.doPost(map, url);
    }


//    public static String uploadArticle(String pid, String gid, ArrayList<Attachnfo> attachid, String content, String type, String tags) throws Exception {
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("type", type);
//        String str = "";
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//
//            str = mapper.writeValueAsString(attachid);
//        } catch (JsonParseException e) {
//            e.printStackTrace();
//            return null;
//        } catch (JsonMappingException e) {
//            e.printStackTrace();
//            return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//        if (!TextUtils.isEmpty(pid))
//            map.put("pid", pid);
////		if(!TextUtils.isEmpty(gid))
////			map.put("gid", gid);
//        map.put("atts", str);
//        map.put("content", content);
//        map.put("tags", tags);
//        //url += Globals.HTTP_ACTION_PARAM + str.toString();
//        //String url = "http://paopao.iuni.com/api/tag/discover";
//        //String url = "http://appmarket.iunios.com/service?module=app&action=fresh&parmJson={\"count\":30,\"page\":1}";
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_POST,
//                Globals.HTTP_UPLOAD_ARTICLE_METHOD);
//        String result = new String();
//        if (Globals.isTestData) {
//            /**getting local data from assert*/
//        } else {
//            //TODO start getting data from server.
//            result = HttpRequstData.doPost(map, url);
//        }
//        return result;
//    }

//    public static String deletePost(String pid) throws Exception {
//        String result = new String();
//        String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_POST,
//                Globals.HTTP_DELETE_POST_METHOD);
//        url += "/" + pid;
//        if (Globals.isTestData) {
//            /**getting local data from assert*/
////			result =SystemUtils.getFromAssets(context, url);
//        } else {
//            //TODO start getting data from server.
//            result = HttpRequstData.doRequest(url);
//        }
//        return result;
//    }
//
//    public static String messageBox(final int pageCount, final String type, final String startId, final String startPage) throws Exception {
//        String result = new String();
//        StringBuilder reqeustUrl = new StringBuilder();
//        reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_NOTICE,
//                Globals.HTTP_LIST_METHOD));
//        reqeustUrl.append("?");
////		param +="&id="+pid;
////		url+=param;
////		result = HttpRequstData.doRequest(url);	
//
//        reqeustUrl.append("&ps=" + pageCount);
//        if (type != null) {
//            reqeustUrl.append("&type=" + type);
//        }
//
//        if (startId != null) {
//            reqeustUrl.append("&id=" + startId);
//        }
//
//        if (startPage != null) {
//            reqeustUrl.append("&pn=" + startPage);
//        }
//        result = HttpRequstData.doRequest(reqeustUrl.toString());
//        return result;
//    }

//    public static String messageReadAll() throws Exception {
//        String result = new String();
//        StringBuilder reqeustUrl = new StringBuilder();
//        reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_NOTICE,
//                Globals.HTTP_MESSAGE_READALL));
//        result = HttpRequstData.doRequest(reqeustUrl.toString());
//        return result;
//    }
//
//    public static String deleteAllMessage() throws Exception {
//        String result = new String();
//        StringBuilder reqeustUrl = new StringBuilder();
//        reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_NOTICE,
//                Globals.HTTP_MESSAGE_CLEAR));
//        result = HttpRequstData.doRequest(reqeustUrl.toString());
//        return result;
//    }
//
//    public static String deleteMessage(String nid) throws Exception {
//        String result = new String();
//        StringBuilder reqeustUrl = new StringBuilder();
//        reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
//                Globals.HTTP_SERVICE_NOTICE,
//                Globals.HTTP_MESSAGE_DELETE));
//        reqeustUrl.append("?&nid=" + nid);
//        result = HttpRequstData.doRequest(reqeustUrl.toString());
//        return result;
//    }
    
        public static String getUserInfo()throws Exception{
    	String result = new String();
        StringBuilder reqeustUrl = new StringBuilder();
        reqeustUrl.append(HttpRequstData.getURLStr(Globals.HTTP_IUNIVOICE_REQUEST_URL,"",""));
        reqeustUrl.append("?module=" + "profile&iunibbsapp=yes");
        result = HttpRequstData.doRequest(reqeustUrl.toString());
        Log.e("jadon3", result);
        return result;
    }
    	public static String checkVersion(Context context) throws Exception {
    		
    		String url = HttpRequstData.getURLStr(Globals.HTTP_APPUPDATE_REQUEST_URL,
    				Globals.HTTP_APPUPDATE_SERVICE_NAME_APPLIST,
    				Globals.HTTP_APPUPDATE_UPGRADEAPP_METHOD);

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(IuniVoiceApp.getInstance(), "update.json");
		} else {
			try {
				returnData = HttpRequstData.doPost(url, ObjectToJsonUtil.getUpJason(context));
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return returnData;
	}

	public static String getPushMessage(int page) throws Exception {
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(
				Globals.HTTP_IUNIVOICE_REQUEST_URL, "", ""));
		reqeustUrl.append("?module=" + "mypm&iunibbsapp=yes&filter=announcepm&page="+page+"&tpp=10");
		result = HttpRequstData.doRequest(reqeustUrl.toString());
		return result;
	}
	
	public static String systemMsgDetail(String nid) throws Exception{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(
				Globals.HTTP_IUNIVOICE_REQUEST_URL, "", ""));
		reqeustUrl.append("?module=" + "mypm&&subop=viewg&iunibbsapp=yes&pmid="+nid);
		result = HttpRequstData.doRequest(reqeustUrl.toString());
		return result;
	}
	
	public static String changeUserIcon(String fileName,String filePath)  throws Exception{
		StringWriter str = new StringWriter();
		Map<String, String> map = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("iunibbsapp", "yes");
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		String url = "http://bbs.iuni.com/api/mobile/index.php?module=uploadavatar";
		return HttpRequstData.uploadImageFile(url, map, fileName, filePath);
	}

	public static String changeUserIntroduce(String content) throws Exception{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(
				Globals.HTTP_IUNIVOICE_REQUEST_URL, "", ""));
		reqeustUrl.append("?module=" + "spacecp&ac=profile&op=info&iunibbsapp=yes");
		Map<String, String> pa = new HashMap<String, String>();
		pa.put("profilesubmit", 1+"");
		pa.put("bio", content);
		result = HttpRequstData.doPost(pa, reqeustUrl.toString());
		return result;
	}
	
	public static String getSystemPushMsg() throws Exception
	{
		String result = new String();
		StringBuilder reqeustUrl = new StringBuilder();
		reqeustUrl.append(HttpRequstData.getURLStr(
				Globals.HTTP_IUNIVOICE_REQUEST_URL, "", ""));
		reqeustUrl.append("?module=" + "pushinfo&iunibbsapp=yes");
		result = HttpRequstData.doRequest(reqeustUrl.toString());
		return result;
	}
	
}
