package com.aurora.community.share.weibo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.aurora.community.R;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.share.weibo.openapi.StatusesAPI;
import com.aurora.community.utils.FileLog;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

public class WeiboShareUtil {
	
	private static final String TAG = "WeiboShareUtil"; 
	
	private Activity mContext;
	private static WeiboShareUtil mInstance;
	
	private AuthInfo authInfo;
	private IWeiboShareAPI  mWeiboShareAPI = null;
	private SsoHandler mSsoHandler;
	
	public static final int AURORA_WEIBO_FAILED = 0;
	public static final int AURORA_WEIBO_SUCCESS = 1;
	public static final int AURORA_WEIBO_CANCEL = 2;
	public static final int AURORA_WEIBO_UNKOWNERRO = 3;
	
	private AuroraWeiBoCallBack mCallBack = null;
	
	private int mShareType = SHARE_CLIENT;
	public static final int SHARE_CLIENT = 1;
    public static final int SHARE_ALL_IN_ONE = 2;
	
	public interface AuroraWeiBoCallBack {
		public void onSinaWeiBoCallBack(int ret);
	}
	
	private WeiboShareUtil(Activity context) {
		mContext = context;
		authInfo = new AuthInfo(mContext, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
		inidWeiBoSdk(context);
	}
	
	private void inidWeiBoSdk(Activity context) {
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, WeiboConstants.APP_KEY);        
        mWeiboShareAPI.registerApp();
	}

	public static synchronized WeiboShareUtil getInstance(Activity context) {
		if (mInstance == null)
			mInstance = new WeiboShareUtil(context);

		return mInstance;
	}

	public boolean isAuth() {
		Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(mContext);
        if (token != null && token.isSessionValid()) {
        	return true;
        }
		return false;
	}
	
	public void authWeibo(Activity activity, AuroraWeiBoCallBack callBack) {
		mCallBack = callBack;
		mSsoHandler = new SsoHandler(activity, authInfo);
        mSsoHandler.authorize(new AuthListener());
	}
	
	public void sendImageMessage(String text, Bitmap image) {
		if (mWeiboShareAPI.isWeiboAppInstalled() && mWeiboShareAPI.isWeiboAppSupportAPI()) {
			sendImageMessageClient(text, image);
		} else {
			
			Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
			if (accessToken != null && accessToken.isSessionValid()) {
				StatusesAPI mStatusesAPI = new StatusesAPI(mContext, WeiboConstants.APP_KEY, accessToken);
				
				Toast.makeText(mContext, mContext.getString(R.string.share_weibo_sending), Toast.LENGTH_SHORT).show();
				
				if (TextUtils.isEmpty(text)) {
					text = mContext.getString(R.string.share_weibo_sharepic);
				}
				mStatusesAPI.upload(text, image, null, null, new RequestListener() {
					
					@Override
					public void onWeiboException(WeiboException arg0) {
						FileLog.i(TAG, "weibo send exception, message: " + arg0.getMessage());
						Toast.makeText(mContext, mContext.getString(R.string.share_weibo_send_fail), Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onComplete(String arg0) {
						Toast.makeText(mContext, mContext.getString(R.string.share_weibo_send_success), Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				FileLog.i(TAG, "weibo send fail accesstoken not valid!");
				Toast.makeText(mContext, mContext.getString(R.string.share_weibo_auth_error), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void sendImageMessageClient(String text, Bitmap image) {
		// 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (!TextUtils.isEmpty(text)) {
        	TextObject textObject = new TextObject();
            textObject.text = text;
            weiboMessage.textObject = textObject;
        }
        
		if (image != null) {
			ImageObject imageObject = new ImageObject();
			imageObject.setImageObject(image);
			weiboMessage.imageObject = imageObject;
		}
		
		// 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        
        if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
        	mShareType = SHARE_CLIENT;
        } else {
        	mShareType = SHARE_ALL_IN_ONE;
        }
        
        // 3. 发送请求消息到微博，唤起微博分享界面
        if (mShareType == SHARE_CLIENT) {
            mWeiboShareAPI.sendRequest(mContext, request);
        }
        else if (mShareType == SHARE_ALL_IN_ONE) {
            Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
            String token = "";
            if (accessToken != null) {
                token = accessToken.getToken();
            }
            mWeiboShareAPI.sendRequest(mContext, request, authInfo, token, new WeiboAuthListener() {
                
                @Override
                public void onWeiboException( WeiboException arg0 ) {
                }
                
                @Override
                public void onComplete( Bundle bundle ) {
                    // TODO Auto-generated method stub
                    Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                    AccessTokenKeeper.writeAccessToken(mContext, newToken);
                    Toast.makeText(mContext, "onAuthorizeComplete token = " + newToken.getToken(), 0).show();
                }
                
                @Override
                public void onCancel() {
                }
            });
        }
	}
	
	/**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
        	Oauth2AccessToken mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(mContext, mAccessToken);
                
                if (mCallBack != null) {
                	mCallBack.onSinaWeiBoCallBack(AURORA_WEIBO_SUCCESS);
                	inidWeiBoSdk(mContext);
				}
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
//                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                String message = "授权失败";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                if (mCallBack != null) {
                	mCallBack.onSinaWeiBoCallBack(AURORA_WEIBO_UNKOWNERRO);
				}
            }
        }

        @Override
        public void onCancel() {
        	Toast.makeText(mContext, "取消", Toast.LENGTH_LONG).show();
			if (mCallBack != null) {
				mCallBack.onSinaWeiBoCallBack(AURORA_WEIBO_CANCEL);
			}
        }

        @Override
        public void onWeiboException(WeiboException e) {
        	Toast.makeText(mContext, "失败", Toast.LENGTH_LONG).show();
			if (mCallBack != null) {
				mCallBack.onSinaWeiBoCallBack(AURORA_WEIBO_FAILED);
			}
        }
    }
    
    public void authorizeCallBack(int requestCode, int resultCode, Intent data) {
		// SSO 授权回调, 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
	}

}
