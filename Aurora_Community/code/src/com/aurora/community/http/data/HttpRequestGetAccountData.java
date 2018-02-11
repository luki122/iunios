package com.aurora.community.http.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.aurora.community.CommunityApp;
import com.aurora.community.http.HttpRequstData;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.Log;
import com.aurora.community.utils.SystemUtils;

public class HttpRequestGetAccountData {

	private static final String TAG = "HttpRequestGetAccountData";

	public static String getLoginObject(String acctName, String pwdMD5,
			String imei, String validCode,int type) throws Exception {

		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("acctName", acctName);
			map.put("pwdMD5", pwdMD5);
			map.put("imei", imei);
			if (!TextUtils.isEmpty(validCode)) {
			    map.put("validCode", validCode);
			}
			//先默认为1
			map.put("includeToken", 1);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HTTPS_LOGIN_METHOD;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, action);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(),
					"login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String getRegisterObject(String phoneNo, String email,
			String pwd, String pwdMD5, String imei, String vc, String vcId, String validCode)
			throws Exception {

		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("phoneNo", phoneNo);
			map.put("email", email);
			if (!TextUtils.isEmpty(validCode)) {
			    map.put("validCode", validCode);
			}
			map.put("pwd", pwd);
			map.put("pwdMD5", pwdMD5);
			map.put("imei", imei);
			map.put("vc", vc);
			map.put("phoneStateCode", "+86");
			map.put("vcId", vcId);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HTTPS_REGISTER_METHOD;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, action);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(),
					"login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String getVerifyCodeObject(String userId, String userKey,
			String phoneNo, String event, String validCode, String imei) throws Exception {

		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (!TextUtils.isEmpty(userId)) {
				map.put("userId", userId);
			}
			if (!TextUtils.isEmpty(userKey)) {
				map.put("userKey", userKey);
			}
			if (!TextUtils.isEmpty(validCode)) {
			    map.put("validCode", validCode);
			}
			map.put("phoneNo", phoneNo);
			map.put("event", event);
			map.put("imei", imei);

			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HTTPS_VERIFYCODE_METHOD;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, action);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(),
					"login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	
	public static String getUserCookie(String userId, String userKey) throws Exception {
		
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (!TextUtils.isEmpty(userId)) {
				map.put("userId", userId);
			}
			if (!TextUtils.isEmpty(userKey)) {
				map.put("userKey", userKey);
			}

			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HTTPS_COOKIE_METHOD;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, action);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(),
					"login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}
	
	public static String getUserInfoObject(String userId, String userKey)
			throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);

			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HTTPS_USERINFO_METHOD;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_PROFILE, action);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			returnData = SystemUtils.getFromAssets(CommunityApp.getInstance(),
					"login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String resetLoginPwd(String email, String phoneNo,
			String newPwd, String newPwdMd5, String vc, String vdId, String validCode, String imei) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (!TextUtils.isEmpty(email)) {
				map.put("email", email);
				if (!TextUtils.isEmpty(validCode)) {
				    map.put("validCode", validCode);
				}
				map.put("imei", imei);
			} else {
				map.put("phoneNo", phoneNo);
				map.put("newPwd", newPwd);
				map.put("newPwdMD5", newPwdMd5);
				map.put("vc", vc);
				map.put("vcId", vdId);
				map.put("imei", imei);
			}
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, Globals.HTTPS_FINDPWD_METHOD);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String logout(String userId, String userKey) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, Globals.HTTPS_LOGOUT_METHOD);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String updateAccountInfo(String userId, String userKey,
			String nickName) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);
			map.put("nick", nickName); // 昵称可能含有特殊字符，比如&
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_PROFILE, Globals.HTTPS_EDIT_METHOD);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String changePhoto(String userId, String userKey,
			String fileName, InputStream is) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("userId", userId);
		map.put("userKey", userKey);

		String url = HttpRequstData.getURLStr(Globals.HTTP_ACCOUNT_REQUEST_URL,
				Globals.MODULE_PROFILE, Globals.HTTPS_CHANGEPHOTO_METHOD);

		// String url = HttpRequstData.getURLStr("http://18.8.0.244/account",
		// Globals.MODULE_PROFILE, Globals.HTTPS_CHANGEPHOTO_METHOD);

		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.uploadFile(url, map, fileName, is);
		}

		return returnData;
	}

	public static boolean downloadPhoto(String photoUrl, String userId,
			String userKey, OutputStream os) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return HttpRequstData.downloadFile(photoUrl, str.toString(), os);
	}
	
	public static Bitmap downloadVerifyCode(String url, String imei, String event) throws Exception {
	    StringWriter str = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map.put("imei", imei);
            map.put("event", event);
            mapper.writeValue(str, map);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
	        boolean result = HttpRequstData.downloadFile(url, str.toString(), baos);
	        if (result) {
	            byte[] bitmapData = baos.toByteArray();
	            Bitmap vcCode = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	            return vcCode;
	        }
	    } finally {
	        baos.close();
	    }
        
        return null;
    }

	public static String changeLoginPwd(String userId, String userKey,
			String oldPwdMd5, String newPwdMd5, String oldPwd, String newPwd) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);
			map.put("oldPwdMD5", oldPwdMd5);
			map.put("newPwdMD5", newPwdMd5);
			map.put("oldPwd", oldPwd);
            map.put("newPwd", newPwd);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_PROFILE, Globals.HTTPS_CHANGEPWD_METHOD);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String changePhoneNo(String userId, String userKey,
			String newPhoneNo, String vc, String phoneStateCode, String vcId)
			throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);
			map.put("newPhoneNo", newPhoneNo);
			map.put("vc", vc);
			map.put("phoneStateCode", phoneStateCode);
			map.put("vcId", vcId);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_PROFILE, Globals.HTTPS_CHANGEPHONE_METHOD);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String changeEmail(String userId, String userKey,
			String newEmail) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("userId", userId);
			map.put("userKey", userKey);
			map.put("newEmail", newEmail);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_PROFILE, Globals.HTTPS_CHANGEEMAIL_METHOD);
		// url += Globals.HTTPS_ACTION_PARAM + str.toString();

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
			// returnData = SystemUtils.getFromAssets(AccountApp.getInstance(),
			// "login.json");

		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}
		return returnData;
	}

	public static String resendVerifyEmail(String userId, String userKey, String email, String event) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("email", email);
			map.put("event", event);		//注册时：register；绑定邮箱时：bindemail
			map.put("imei", SystemUtils.getIMEI());
			if (event.equals("bindemail")) {
				map.put("userId", userId);
				map.put("userKey", userKey);
			}
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HPTTS_RESEND_VERIFY_EMAIL;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, action);

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}

		Log.i(TAG, "resendVerifyEmail: " + returnData);

		return returnData;
	}
	
	public static String checkCurPhone(String userId, String userKey, String phoneNo, String pwdMD5) throws Exception {
	    StringWriter str = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map.put("userId", userId);
            map.put("userKey", userKey);
            map.put("phoneNo", phoneNo);
            map.put("pwdMD5", pwdMD5);
            mapper.writeValue(str, map);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
                Globals.MODULE_PROFILE, Globals.HTTPS_CHECKCURPHONE_METHOD);

        String returnData = new String();
        if (Globals.isTestData) {
            // 先使用模拟数据
        } else {
            returnData = HttpRequstData.doHttpsRequest(url, str.toString());
        }

        return returnData;
	}
	
	public static String validateChgPhoneVc(String userId, String userKey, String vc) throws Exception {
        StringWriter str = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map.put("userId", userId);
            map.put("userKey", userKey);
            map.put("vc", vc);
            mapper.writeValue(str, map);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
                Globals.MODULE_PROFILE, Globals.HTTPS_VALIDATECHGPHONEVC_METHOD);

        String returnData = new String();
        if (Globals.isTestData) {
            // 先使用模拟数据
        } else {
            returnData = HttpRequstData.doHttpsRequest(url, str.toString());
        }

        return returnData;
    }
	
	public static String checkCurEmail(String userId, String userKey, String email) throws Exception {
        StringWriter str = new StringWriter();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map.put("userId", userId);
            map.put("userKey", userKey);
            map.put("email", email);
            mapper.writeValue(str, map);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
                Globals.MODULE_PROFILE, Globals.HTTPS_CHECKCUREMAIL_METHOD);

        String returnData = new String();
        if (Globals.isTestData) {
            // 先使用模拟数据
        } else {
            returnData = HttpRequstData.doHttpsRequest(url, str.toString());
        }

        return returnData;
    }
    
    public static String validateFindpwdVc(String imei, String vc) throws Exception {
		StringWriter str = new StringWriter();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			map.put("imei", imei);
			map.put("vc", vc);
			mapper.writeValue(str, map);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		String action = "";

		action = Globals.HTTPS_VALIDATE_FINDPWDVC;

		String url = HttpRequstData.getURLStr(Globals.HTTPS_ACCOUNT_REQUEST_URL,
				Globals.MODULE_AUTH, action);

		String returnData = new String();
		if (Globals.isTestData) {
			// 先使用模拟数据
		} else {
			returnData = HttpRequstData.doHttpsRequest(url, str.toString());
		}

		Log.i(TAG, "validateFindpwdVc: " + returnData);

		return returnData;
	}
    
}