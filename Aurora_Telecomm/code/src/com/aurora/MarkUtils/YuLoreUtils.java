package com.android.server.telecom;

import com.yulore.framework.*;
import com.yulore.superyellowpage.modelbean.*;

import java.util.List;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.graphics.Bitmap;

public class YuLoreUtils extends AuroraMarkUtils{
    private static final String TAG = "YuLoreUtils";
	
    private YuloreHelper helper;
    
    public static String logoUrl = "";
    	
    public static String slogan = "";
    
    public static boolean isV = false;
    
    private static final String auroraApikey = "jfvLhfephClV9jkRuscG9nmpDL9SiOc7";
    
    private static final String auroraSecret = "0hOdge6xrsi9fcpXRKplnmpub3xXpDdzaeML6HjUCMufLDiwyEtRJuLocasVkoiJ5thxzwDdvan7lsi1zajntau9i5rpoVlrg5jvVC1aksvi4TlLfWyxdlrQB7pnMcNzvD06GHCrrMrju0hzqvYlrvzoFDBh3SoxGjKcd";
    
    public void init(Context context) {
    	helper = new YuloreHelper(context);
    }
    
    private boolean mIsInit = false;
	
    public boolean bindService() {
    	mIsInit = helper.bindService(auroraApikey, auroraSecret);
    	Log.i(TAG, "bindService result = " + mIsInit);
    	if(mIsInit) {
//        	try {
//        		helper.setNetworkAccess(auroraApikey, auroraSecret, true);
//        	} catch (Exception e) {
//        		e.printStackTrace();
//        	}
    	}
    	return mIsInit;
    }
    
    public boolean isInit() {
    	return mIsInit;
    }
    
    public void unbindService() {
    	helper.unbindService();
    }

//    功能描述:该方法用于获取全部已缓存到本地的数据,其中包括识别到商户的数据,识别到归属地的数据以及识别到
//    标识信息的数据。方法为耗时操作,建议在创建异步线程中执行该方法。
    public List<RecognitionTelephone> getAllRecognitionTelephones(){
    	try {
    	    return helper.getAllRecognitionTelephones(auroraApikey, auroraSecret);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

//    功能描述:该方法用于获取全部已标记的数据。方法为耗时操作,建议在创建异步线程中执行该方法。
    public List<TagTelephone> getAllTagTelephones(String apiKey, String secret) {
    	try {
        	return helper.getAllTagTelephones(auroraApikey, auroraSecret);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
//    功能描述:该方法用于根据电话号码获取号码的标记信息。方法为耗时操作,建议在创建异步线程中执行该方法。
    public Tag getNumberTag(String number) {
    	try {
        	return helper.getNumberTag(auroraApikey, auroraSecret, number);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;

    }

    
//    功能描述:该方法用于取消号码在本地的标记。方法为耗时操作,建议在创建异步线程中执行该方法。
    public boolean deleteTagTelephoneNumber(String number) {
    	try {
        	return helper.deleteTagTelephoneNumber(auroraApikey, auroraSecret, number);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }

    
//    功能描述:该方法用于获取全部标记类型,包括“自定义标记”。用户在自定义标记中添加的类型不罗列在内。
    public List<Tag> getAllTags() {
    	try {
        	return helper.getAllTags(auroraApikey, auroraSecret);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

//    功能描述:该方法返回电话号码相关的各种信息,并将信息缓存到本地,方法中操作为耗时操作,建议
//    在创建异步线程中执行该方法。
//    应用对应的 APIKEY
//    参数:apiKey
//    secret 应用对应的 SECRET
//    telNum 查询电话实例
//    type 1 呼入, 2 呼出
//    smart 智能模式开关
//    immediately  立即返回数据
//    limit  1=不限制 2=wifi 下获取数据 3=不联网
    public RecognitionTelephone queryNumberInfo(String telNum, int type, boolean immediately, int limit) {
    	Log.i(TAG, "queryNumberInfo");
    	try {
    		RecognitionTelephone r = helper.queryNumberInfo(auroraApikey, auroraSecret, telNum, type, true, immediately, limit);
			Log.i(TAG, " RecognitionTelephone = " + r);
			return r; 
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

//    功能描述:该方法批量识别电话号码相关的各种信息,并缓存数据到本地。方法中操作为耗时操作,每次执行最多返
//    回 30 个电话信息,建议在创建异步线程中执行该方法。
//    参数:
//        callList 需识别的电话号码列表
//        telList 对应的来去电方式 1 呼入, 2 呼出
//        smart 是否开启智能模式,在此模式下,会查询缓存中的数据,同时在 wifi 条件下进行数据的在线更新。否则直接查询在线数据。
    public List<RecognitionTelephone> queryNumberInfoBatch(List<String> telList, List<String> callList) {
    	try {
        	return helper.queryNumberInfoBatch(auroraApikey, auroraSecret, telList, callList, true);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
        
//    功能描述:该方法将用户标记的电话号码信息发送给电话帮号码标识联盟,方法为耗时操作,建议在创建异步线程
//    中执行该方法。
    public boolean tagTelNumber(String telNumber, String tag) {
    	try {
        	return helper.tagTelNumber(auroraApikey, auroraSecret, telNumber, tag);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }

//    功能描述:该方法通过号码查询标准化服务等信息,方法中操作为耗时操作,建议在创建异步线程中执行该方法。
//    public NotificationNumber querySmsInfo(String telNumber, String tag){
//    	return helper.querySmsInfo(auroraApikey, auroraSecret, telNumber, tag);
//    }

    public static void getMarkInternal(String number) {
        if(AuroraGlobals.getInstance().mYuloreUtils.isInit()) {
        	RecognitionTelephone r = AuroraGlobals.getInstance().mYuloreUtils.queryNumberInfo(number, 1, false, 1);
      	    if(r == null) {
        		return;
        	}
        	slogan = r.getSlogan();
        	logoUrl = r.getLogo();
        	isV = !TextUtils.isEmpty(r.getSloganImg());
        	Log.i(TAG, "slogan = " + slogan + " logoUrl = " + logoUrl + " SloganImg = " + r.getSloganImg());
			TelephoneFlag f = r.getFlag();
			if(f != null) {
				mNumber = f.getNum();
				mMark = f.getType();				
	         	Log.i(TAG, "mNumber = " + mNumber + " mMark = " + mMark);
			}        	
    	}    	
    }
    
	public static void reset() {
		slogan = "";
    	logoUrl = "";
    	isV = false;
	}
	
	public static Bitmap getPhoto(){
		return ImageUtils.getImage(logoUrl);
	}
       
	public static boolean getIsV() {
		return isV;
	}
	
	public static String getSlogan() {
		return slogan;
	}

}