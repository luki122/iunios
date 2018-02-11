package cn.com.xy.sms.sdk.ui.bubbleview;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.log.PrintTestLogUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BubblePopupView;
import cn.com.xy.sms.sdk.ui.popu.popupview.PartViewParam;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import cn.com.xy.sms.util.SdkCallBack;

/**
 * xiaoyuan richBubbleView Manager
 * 
 * @author Administrator
 * 
 */
@SuppressLint("NewApi")
public class DuoquBubbleViewManager {

	final static String TAG = "XIAOYUAN";
	final static int DUOQU_CACHE_ITEM_VIEW_MAX_SIZE = 200;
	public static final int DUOQU_BUBBLE_VIEW_ID = 999999999;
	// Cache type of the return data 1:Returns the SDK cache id
	public static final byte DUOQU_RETURN_CACHE_SDK_MSG_ID = 1;
	// Cache type of the return data 2: Returns the recognition results, Save it
	// by the developer or identification results.
	public static final byte DUOQU_RETURN_CACHE_SDK_MSG_VALUE = 2;
	private static final String DUOQU_VIEW_ID = "View_fdes";

	// cache msg data
	static LruCache<String, BusinessSmsMessage> mFormatSmsDataCache = new LruCache<String, BusinessSmsMessage>(
			DUOQU_CACHE_ITEM_VIEW_MAX_SIZE);
	// cache bubble view
	static LruCache<String, LinkedList<BubblePopupView>> mFormatItemViewCacheMapList = new LruCache<String, LinkedList<BubblePopupView>>(
			DUOQU_CACHE_ITEM_VIEW_MAX_SIZE);
	// cache PartViewParam
	static HashMap<String, Map<String, PartViewParam>> viewParamCache = new HashMap<String, Map<String, PartViewParam>>();

	public static void putMsgToCache(String cacheKey, BusinessSmsMessage msg) {
		if (cacheKey == null || msg == null) {
			LogManager
					.w(TAG,
							"DuoquBubbleViewManager.pubMsgToCache cacheKey or msg is null. ");
			return;
		}
		synchronized (mFormatSmsDataCache) {
			mFormatSmsDataCache.put(cacheKey, msg);
		}
	}

	public static BusinessSmsMessage getFomratSmsData(String cacheKey) {
		if (cacheKey == null) {
			LogManager
					.w(TAG,
							"DuoquBubbleViewManager.getMsgFromCache cacheKey is null. ");
			return null;
		}
		return mFormatSmsDataCache.get(cacheKey);
	}

	public static void putBubbleItemTypeViewToCache(String cacheKey,
			LinkedList<BubblePopupView> bubbleViews) {
		if (cacheKey == null || bubbleViews == null) {
			LogManager
					.w(TAG,
							"DuoquBubbleViewManager.putBubbleItemTypeViewToCache cacheKey or msg is null. ");
			return;
		}
		synchronized (mFormatItemViewCacheMapList) {
			mFormatItemViewCacheMapList.put(cacheKey, bubbleViews);
		}
	}

	public static LinkedList<BubblePopupView> getFomratItemViewList(
			String cacheKey) {
		if (cacheKey == null) {
			LogManager
					.w(TAG,
							"DuoquBubbleViewManager.getBubbleItemTypeViewFromCache cacheKey is null. ");
			return null;
		}
		return mFormatItemViewCacheMapList.get(cacheKey);
	}

	public static void clearCacheData() {
		if (mFormatSmsDataCache != null) {
			synchronized (mFormatSmsDataCache) {
				mFormatSmsDataCache.evictAll();
			}
		}
		if (mFormatItemViewCacheMapList != null) {
			synchronized (mFormatItemViewCacheMapList) {
				mFormatItemViewCacheMapList.evictAll();
			}
		}
	}

	public static Map<String, Object> parseMsgToBubbleCardResult(Context ctx,
			String msgId, String phoneNum, String smsCenterNum,
			String smsContent, long smsReceiveTime, byte returnCacheType,
			HashMap<String, String> extend) {
		try {
			return ParseManager.parseMsgToBubbleCardResult(ctx, msgId,
					phoneNum, smsCenterNum, smsContent, smsReceiveTime,
					returnCacheType, extend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject getRichBubbleData(final Activity ctx,
			final String msgIds, final String phoneNum,
			final String smsCenterNum, final String smsContent,
			long smsReceiveTime, byte returnCacheType, final View itemView,
			ViewGroup parentView, final ViewGroup richItemGroup,
			final AdapterView adViews, final HashMap<String, Object> extend,
			final SdkCallBack xyCallBack, boolean scrollFing) {
		if (StringUtils.isNull(msgIds)) {
			XyUtil.doXycallBackResult(xyCallBack, -1, null, msgIds);
			return null;
		}
		try {
			ParseRichBubbleManager.queryDataByMsgItem(msgIds, phoneNum,
					smsContent, smsReceiveTime, smsCenterNum, 2, xyCallBack,
					scrollFing, extend);
		} catch (Exception e) {
			XyUtil.doXycallBackResult(xyCallBack, -1, null, msgIds);
			LogManager.e("getRichBubbleData", "error: " + e.getMessage(),e);
			e.printStackTrace();
		}

		return null;

	}

	private static View getBubblePopupView(Activity ctx,
			BusinessSmsMessage msg, String viewId, View itemView,
			ViewGroup apView) throws Exception {
		// long time =System.currentTimeMillis();
		BubblePopupView view = null;
		LinkedList<BubblePopupView> linkedList = null;
		int size = 0;
		if (!StringUtils.isNull(viewId)) {
			linkedList = getFomratItemViewList(viewId);
			if (linkedList != null) {
				size = linkedList.size();
				int index = -1;
				int cnt = 0;
				do {
					view = getCacheItemView(linkedList);
					index = ViewManger.indexOfChild(view, apView);
					cnt++;
				} while (index != -1 && cnt < size);
				if (index != -1) {// view used
					view = null;
				}
				// long edtime =System.currentTimeMillis();
				// LogManager.d("duoqu_xiaoyuan","aaaa getBubblePopupView take time: "+(edtime-time));
				if (view != null) {
					try {
						// view.reSetActivity(ctx);
						view.reBindData(ctx, msg);
						// edtime =System.currentTimeMillis();
						// LogManager.d("duoqu_xiaoyuan","bbbb getBubblePopupView take time: "+(edtime-time));
						// LogManager.d("duoqu_xiaoyuan",
						// "msgid: "+msg.smsId+" view reBindData viewId: "+viewId);
					} catch (Exception e) {
						e.printStackTrace();
						view = null;
						// LogManager.w("duoqu_xiaoyuan",
						// "msgid: "+msg.smsId+" view reBindData error:  "+e.getMessage());
					}
				}
			}
		}
		if (view == null) {
			view = new BubblePopupView(ctx);
			Map<String, PartViewParam> map = viewParamCache.get(viewId);
			if (map == null) {
				map = ViewManger.parseViewPartParam(viewId);
				viewParamCache.put(viewId, map);
			}
			msg.putValue("viewPartParam", map);
			view.init(ctx, msg, null);
			view.setId(DUOQU_BUBBLE_VIEW_ID);
			// long edtime =System.currentTimeMillis();
			// LogManager.d("duoqu_xiaoyuan","cccc getBubblePopupView take time: "+(edtime-time));
			if (linkedList == null) {
				linkedList = new LinkedList<BubblePopupView>();
				putBubbleItemTypeViewToCache(viewId, linkedList);
			}
			addCacheItemView(view, linkedList);
			// edtime =System.currentTimeMillis();
			// LogManager.d("duoqu_xiaoyuan","dddd getBubblePopupView take time: "+(edtime-time));
		}

		return view;
	}

	@SuppressLint("NewApi")
	private static void addCacheItemView(BubblePopupView itewView,
			LinkedList<BubblePopupView> listView) {
		listView.offerLast(itewView);// add elements
	}

	@SuppressLint("NewApi")
	private static BubblePopupView getCacheItemView(
			LinkedList<BubblePopupView> listView) {
		BubblePopupView itemView = null;
		if (listView != null) {
			itemView = listView.pollFirst();
			if (itemView != null) {
				addCacheItemView(itemView, listView);
			}
		} else {
			itemView = null;
		}
		return itemView;
	}

	public static View getRichBubbleView(Activity ctx, JSONObject jsobj,
			String smsId, String smsContent, String phoneNum,long smsReceiveTime,
			final View itemView, final AdapterView adViews,
			final HashMap<String, Object> extend) {
		View richview = null;
		try {
			String key = smsId + smsReceiveTime;
			BusinessSmsMessage msg = getFomratSmsData(key);
			PrintTestLogUtil.printTestLog("getRichBubbleView", "key="+key+" msg="+msg);
			if (msg != null) {
				// long stTime = System.currentTimeMillis();
				String viewId = (String) msg.getValue(DUOQU_VIEW_ID);
				try {
					richview = getBubblePopupView(ctx, msg, viewId, itemView,
							adViews);
				} catch (Exception e) {
					LogManager.e(TAG, "View_fdes : " + viewId
							+ " error: " + e.getMessage(),e);
					e.printStackTrace();
				}
			} else if (jsobj != null && jsobj.has(DUOQU_VIEW_ID)) {
				// long stTime = System.currentTimeMillis();
				final String viewId = jsobj.getString(DUOQU_VIEW_ID);
				msg = BusinessSmsMessage.createMsgObj();
				msg.smsId = Long.parseLong(smsId);
				msg.viewType = 1;// bubble view
				msg.bubbleJsonObj = jsobj;
				msg.messageBody = smsContent;
				msg.originatingAddress = phoneNum;
				msg.titleNo = jsobj.optString("title_num");
				msg.extendParamMap = extend;
				if (extend != null && !extend.isEmpty()) {
					msg.simIndex = XyUtil.getSimIndex(extend);
					msg.simName = (String) extend.get("simName");
					if (extend.containsKey("msgTime")) {
						try {
							msg.msgTime = Long.parseLong(extend.get("msgTime")
									.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				// long etTime = System.currentTimeMillis();
				// LogManager.e("duoqu_xiaoyuan",
				// " 1111 getRichBubbleView  take time:"+(etTime-stTime)+" viewId:"+viewId);
				richview = getBubblePopupView(ctx, msg, viewId, itemView,
						adViews);
				// etTime = System.currentTimeMillis();
				// LogManager.e("duoqu_xiaoyuan",
				// " 2222 getRichBubbleView  take time:"+(etTime-stTime));
				putMsgToCache(key, msg);
				// etTime = System.currentTimeMillis();
				// LogManager.e("duoqu_xiaoyuan",
				// " 3333 getRichBubbleView  take time:"+(etTime-stTime));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return richview;
	}

	/**
	 * before init bubbleview for scroll speed each bubbleView init maxsize 4
	 * 
	 * @param ctx
	 * @param phone
	 */
	public static void beforeInitBubbleView(Activity ctx, String phone) {
		// if(true)return;
		JSONObject obj = PhoneSmsParseManager.findObjectByPhone(phone);
		if (obj != null && obj.has("useBubbleViews")) {
			JSONArray arr = null;
			try {
				arr = new JSONArray(obj.getString("useBubbleViews"));
				if (arr != null) {
					int len = arr.length();
					BubblePopupView view = null;
					String viewId = null;
					int viewCacheSize = 0;
					LinkedList<BubblePopupView> linkedList = null;
					int maxCacheSize = 4;
					BusinessSmsMessage msg = BusinessSmsMessage.createMsgObj();
					msg.viewType = 1;
					for (int i = 0; i < len; i++) {
						viewId = arr.getString(i);
						if (StringUtils.isNull(viewId)) {
							continue;
						}
						linkedList = getFomratItemViewList(viewId);
						if (linkedList == null) {
							linkedList = new LinkedList<BubblePopupView>();
							putBubbleItemTypeViewToCache(viewId, linkedList);
						}
						viewCacheSize = linkedList.size();
						if (viewCacheSize >= maxCacheSize) {
							continue;
						}

						Map<String, PartViewParam> map = viewParamCache
								.get(viewId);
						if (map == null) {
							map = ViewManger.parseViewPartParam(viewId);
							viewParamCache.put(viewId, map);
						}
						msg.putValue("viewPartParam", map);
						do {
							view = new BubblePopupView(ctx);
							view.init(ctx, msg, null);
							view.setId(DUOQU_BUBBLE_VIEW_ID);
							addCacheItemView(view, linkedList);
							viewCacheSize++;
						} while (viewCacheSize < maxCacheSize);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
