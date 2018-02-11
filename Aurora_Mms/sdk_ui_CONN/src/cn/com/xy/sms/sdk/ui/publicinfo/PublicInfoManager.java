package cn.com.xy.sms.sdk.ui.publicinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony.Threads;
import android.util.LruCache;
import android.widget.BaseAdapter;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class PublicInfoManager {

    public static final int IMAGE_WIDTH = 100;
    public static final int IMAGE_HIGHT = 100;
    private static HashMap<String, String> extend = null;

    // public static XyMemoryCache memoryCache = new XyMemoryCache();
    private static LruCache<String, BitmapDrawable> logoCache = new LruCache<String, BitmapDrawable>(100);
    // 是否需要预加载logo，根据具体要求设置
    public static boolean isBeforeLoadLogo = true;

    public static HashMap<String, JSONObject> publicInfoData = new HashMap<String, JSONObject>();

    public static HashMap<String, String> phonePublicIdData = new HashMap<String, String>();

    public static ExecutorService beforPublicInfoPool = Executors.newFixedThreadPool(1);

    public static ExecutorService beforLogoPublicInfoPool = Executors.newFixedThreadPool(2);

    public static ExecutorService publicInfoPool = Executors.newFixedThreadPool(2);

    private static BaseAdapter mBaseAdapter = null;
    private static boolean isLoaded = false;
    private static Handler mHandler = null;

    public static void registerBaseAdapter(Handler handler,
            BaseAdapter baseAdapter) {
        if (isLoaded || mBaseAdapter != null) {
            return;
        }
        synchronized (publicInfoPool) {
            mBaseAdapter = baseAdapter;
            mHandler = handler;
        }
    }

    private static void notifyDataChange() {
        if (mBaseAdapter != null) {

            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mBaseAdapter.notifyDataSetChanged();
                        synchronized (publicInfoPool) {
                            mBaseAdapter = null;
                            mHandler = null;
                        }
                    }
                });
            } else {
                mBaseAdapter = null;
            }
        }
    }

    private static void putLogoDrawable(String logo, BitmapDrawable bd) {

        if (logo == null || bd == null) {
            return;
        }
        try {
            synchronized (logoCache) {
                logoCache.put(logo, bd);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static BitmapDrawable getLogoDrawable(String logo) {

        if (logo == null) {
            return null;
        }
        try {
            synchronized (logoCache) {
                return logoCache.get(logo);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Set<String> loadTop50Num(Context context) {
        String selection = " _id in (SELECT " + Threads.RECIPIENT_IDS
                + " FROM threads ORDER BY date DESC LIMIT 50)";
        return loadPublicNumbers(context, selection);
    }

    /**
     * 预加载企业资料
     * 
     * @param context
     */
    public static void beforeLoadPublicInfo(final Context context) {
        beforPublicInfoPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Set<String> publicNumbers = loadTop50Num(context);
                    boolean isInit = ParseManager.isInitData();
                    long stTime = System.currentTimeMillis();
                    while (!isInit) {
                        isInit = ParseManager.isInitData();
                        if (isInit) {
                            break;
                        } else {
                            long edTime = System.currentTimeMillis();
                            if (edTime - stTime > 30000) {
                                break;
                            }
                            Thread.sleep(3);
                        }
                    }

                    befroeLoadPublicInfo(context, publicNumbers);
                    notifyDataChange();
                    isLoaded = true;
                    Set<String> allPublicNumbers = loadPublicNumbers(context,
                            null);

                    allPublicNumbers.removeAll(publicNumbers);
                    befroeLoadPublicInfo(context, allPublicNumbers);

                    publicNumbers.clear();
                    allPublicNumbers.clear();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void befroeLoadPublicInfo(final Context context,
            Set<String> publicNumbers) {
    	try {
			if (publicNumbers == null || publicNumbers.isEmpty()) {
            return;
        }
        final HashMap<String, String> numResult = ParseManager
                .loadAllPubNum(publicNumbers);

        if (numResult != null && !numResult.isEmpty()) {
            synchronized (phonePublicIdData) {
                phonePublicIdData.putAll(numResult);
            }
            HashSet<String> tempSet = new HashSet<String>();
            tempSet.addAll(numResult.values());
            HashMap<String, JSONObject> result = ParseManager
                    .loadAllPubInfo(tempSet);
            if (result != null && !result.isEmpty()) {
                synchronized (publicInfoData) {
                    publicInfoData.putAll(result);
                }
            }
        }
        beforeLoadPublicInfoAndLogo(context, publicNumbers, numResult);
        numResult.clear();
    
		} catch (Throwable e) {
			e.printStackTrace();
		}

        }

    private static void beforeLoadPublicInfoAndLogo(final Context context,
            Set<String> publicNumbers, HashMap<String, String> numResult) {
    	try {
			 if (publicNumbers != null && !publicNumbers.isEmpty()) {
            Iterator<String> it = publicNumbers.iterator();
            while (it.hasNext()) {
                final String phoneNum = it.next();
                final String pubId = numResult.get(phoneNum);
                if (pubId != null) {
                    beforLogoPublicInfoPool.execute(new Runnable() {
                        public void run() {
                        	try {
                        		JSONObject json = publicInfoData.get(pubId);
                        		if (json != null) {
									String logoName = json.optString("logoc");
									findLogoByLogoName(logoName, null);
								}
                        		
							} catch (Throwable e) {
								e.printStackTrace();
							}
                        }
                    });
                    continue;
                }
                publicInfoPool.execute(new Runnable() {
                    @Override
                    public void run() {
                    	try {
                    		loadPublicInfo(context, phoneNum);
						} catch (Throwable e) {
							e.printStackTrace();
						}
                    }
                });
            }
        }
    
		} catch (Throwable e) {
			e.printStackTrace();
		}
       }

    //
    //
    private static Set<String> loadPublicNumbers(Context context,
            String selection) {
        HashSet<String> hashSet = new HashSet<String>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://mms-sms/canonical-addresses"),
                    new String[] { "address" }, selection, null, null);
            if (cursor != null) {
                int addressColumn = cursor.getColumnIndex("address");
                while (cursor.moveToNext()) {

                    String address = cursor.getString(addressColumn);
                    if (!StringUtils.isNull(address)) {
                        String phoneNumber = address;
                        phoneNumber = phoneNumber.replace(" ", "");
                        if (!StringUtils.isPhoneNumber(phoneNumber)) {
                            hashSet.add(phoneNumber);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                cursor.close();
                cursor = null;
            } catch (Throwable e) {
                e.printStackTrace();
                // TODO: handle Throwable
            }
        }
        return hashSet;

    }

    private static void loadPublicInfo(final Context context,
            final String phoneNum) {
    	try {
			if (StringUtils.isNull(phoneNum) || !ParseManager.isInitData()) {
            return;
        }
        SdkCallBack callBack = new SdkCallBack() {
            @Override
            public void execute(Object... obj) {
                try {
                    if (obj != null && obj.length > 0) {
                        if (!StringUtils.isNull((String) obj[0])) {
                            final JSONObject json = new JSONObject(
                                    (String) obj[0]);
                            saveJsonToCache(json, phoneNum);
                            final String logoName = json.optString("logoc");
                            if (getLogoDrawable(logoName) != null) {
                                return;
                            }
                            if (isBeforeLoadLogo) {
                                beforLogoPublicInfoPool.execute(new Runnable() {
                                    public void run() {
                                    	try {
                                    		findLogoByLogoName(logoName, null);
										} catch (Throwable e) {
											e.printStackTrace();
										}
                                    }
                                });
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        ParseManager.queryPublicInfo(context, phoneNum, 1, "", null, callBack);

    
		} catch (Throwable e) {
			e.printStackTrace();
		}
        }

    /**
     * 有回调
     * @param context
     * @param phoneNum
     * @param sdkCallBack
     */
    public static void loadPublicInfo(final Context context,
            final String phoneNum, final SdkCallBack sdkCallBack) {
    	try {
			if (StringUtils.isNull(phoneNum) || !ParseManager.isInitData()) {
            return;
        }
        publicInfoPool.execute(new Runnable() {
            @Override
            public void run() {
            	try {
					 SdkCallBack callBack = new SdkCallBack() {

                    @Override
                    public void execute(Object... obj) {
                        try {
                            if (obj == null || obj.length <= 2) {
                                return;
                            }
                            String Oldid = (String) obj[2];
                            String result = (String) obj[1];
                            Integer status = (Integer) obj[0];
                            if (status == 0 && !StringUtils.isNull(result)
                                    && phoneNum.equals(Oldid)) {
                                JSONObject json = new JSONObject(result);
                                saveJsonToCache(json, phoneNum);
                                String logoName = json.optString("logoc");
                                String name = json.optString("name");
                                BitmapDrawable bd = findLogoByLogoName(
                                        logoName, null);
                                sdkCallBack.execute(phoneNum, name, logoName,
                                        bd);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };
                Map<String, String> extend = new HashMap<String, String>();
                extend.put("id", phoneNum);
                ParseManager.queryPublicInfoWithId(context, phoneNum, 1, "",
                        extend, callBack);
            
				} catch (Throwable e) {
					e.printStackTrace();
				}
            	
               }
        });
    
		} catch (Throwable e) {
			e.printStackTrace();
		}
        }

    public static JSONObject getPublicInfoByPhoneIncache(String phone) {
        return getPublicInfoByPhoneIncache(phone, false);
    }

    public static JSONObject getPublicInfoByPhoneIncache(String phone,
            boolean isloadAsynchronous) {
        if (StringUtils.isPhoneNumber(phone))
            return null;
        JSONObject json = null;
        String publicId = phonePublicIdData.get(phone);
        if (!StringUtils.isNull(publicId)) {
            json = publicInfoData.get(publicId);
        }
        if (json == null && isloadAsynchronous) {
            final String queryPhone = phone;
            publicInfoPool.execute(new Runnable() {
                @Override
                public void run() {
                	try {
                		loadPublicInfo(Constant.getContext(), queryPhone);
					} catch (Throwable e) {
						e.printStackTrace();
					}
                }
            });
        }

        return json;
    }

    public static void saveJsonToCache(JSONObject json, String phoneNum) {
        try {
            if (json == null)
                return;
            String publicId = json.optString("id");
            json.remove("classifyName");
            json.remove("classifyCode");
            json.remove("email");
            json.remove("weiboName");
            json.remove("weiboUrl");
            json.remove("weixin");
            json.remove("website");
            json.remove("moveWebSite");
            json.remove("pubnum");
            if (!StringUtils.isNull(publicId)) {
                publicInfoData.put(publicId, json);
                phonePublicIdData.put(phoneNum, publicId);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static synchronized HashMap<String, String> getExtend(boolean isAsync) {
        if (isAsync) {
            return null;
        }
        if (extend == null) {
            extend = new HashMap<String, String>();
            extend.put("syn", "true");
        }
        return extend;
    }

    public static BitmapDrawable findLogoByLogoName(String logoName,
            final SdkCallBack callBack) {
    	BitmapDrawable bd = null;
    	try {
			if (StringUtils.isNull(logoName) || !ParseManager.isInitData()) {
            return null;
        }
        bd = getLogoDrawable(logoName);
        if (bd != null) {
            return bd;
        }
        final String localLogoName = logoName;
        SdkCallBack logoCallback = new SdkCallBack() {
            public void execute(Object... obj) {
                if (obj != null && obj.length > 0) {
                    if (obj[0] != null) {
                        putLogoDrawable(localLogoName, (BitmapDrawable) obj[0]);
                        if (callBack != null) {
                            callBack.execute(localLogoName, obj[0]);
                        }
                    }
                }
            }
        };
        bd = ParseManager.findLogoByLogoName(Constant.getContext(), logoName,
                IMAGE_WIDTH, IMAGE_HIGHT, 1, getExtend(true), logoCallback);
        putLogoDrawable(localLogoName, bd);
		} catch (Throwable e) {
			e.printStackTrace();
		}
        
        return bd;
    }

    public static String getValueByKey(String phone, String key) {
        return getValueByKey(phone, key, true);
    }

    public static String getValueByKey(String phone, String key,
            boolean isloadAsynchronous) {
    	try {
			 if (StringUtils.isNull(phone) || StringUtils.isPhoneNumber(phone)
                || StringUtils.isNull(key))
            return null;
        phone = StringUtils.getPhoneNumberNo86(phone);
        JSONObject json = getPublicInfoByPhoneIncache(phone, isloadAsynchronous);
        if (json != null) {
            return json.optString(key);
        }
		} catch (Throwable e) {
			e.printStackTrace();
		}
       
        return null;
    }
}
