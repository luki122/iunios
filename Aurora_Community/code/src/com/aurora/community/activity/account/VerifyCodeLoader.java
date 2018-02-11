/**
 * 
 */
package com.aurora.community.activity.account;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.aurora.community.http.data.HttpRequestGetAccountData;
import com.aurora.community.utils.SystemUtils;

/**
 * 用来加载验证码的类
 * @author JimXia
 *
 * @date 2014年12月2日 上午10:46:54
 */
public class VerifyCodeLoader extends AsyncTask<Void, Integer, Bitmap> {
    private static final String TAG = "VerifyCodeLoader";
    
    private WeakReference<OnVerifyCodeLoadDoneListener> mListener;
    private String mUrl;
    private VC_EVENT mEvent;
    private LoadVerifyCodeErrorInfo mLoadError;
    
    public VerifyCodeLoader(String url, VC_EVENT event, OnVerifyCodeLoadDoneListener listener) {
        mListener = new WeakReference<OnVerifyCodeLoadDoneListener>(listener);
        mUrl = url;
        mEvent = event;
    }
    
    @Override
    protected void onPreExecute() {
    	System.out.println("onPreExecute");
        OnVerifyCodeLoadDoneListener listener = mListener.get();
        if (listener != null) {
            listener.onVerifyCodeLoadBegin();
        }
    }

    @Override
    protected void onCancelled(Bitmap result) {
    	System.out.println("onCancelled");
        OnVerifyCodeLoadDoneListener listener = mListener.get();
        if (listener != null) {
            listener.onVerifyCodeLoadDone(result, mLoadError);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Log.d(TAG, "Jim, vc url: " + mUrl);
        try {
            return HttpRequestGetAccountData.downloadVerifyCode(mUrl,
                    SystemUtils.getIMEI(), getEvent(mEvent));
        } catch (UnknownHostException e) {
            if (!SystemUtils.isNetworkConnected()) {
                mLoadError = new LoadVerifyCodeErrorInfo(LoadVerifyCodeErrorInfo.ERROR_CODE_NO_NETWORK);
            } else {
                mLoadError = new LoadVerifyCodeErrorInfo(LoadVerifyCodeErrorInfo.ERROR_CODE_NETWORK_EXCEPTION);
            }
        } catch (Exception e) {
            mLoadError = new LoadVerifyCodeErrorInfo(LoadVerifyCodeErrorInfo.ERROR_CODE_OTHER);
            Log.e(TAG, "Jim, exception", e);
        }
        
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
    	System.out.println("onPostExecute");
        OnVerifyCodeLoadDoneListener listener = mListener.get();
        if (listener != null) {
            listener.onVerifyCodeLoadDone(result, mLoadError);
        }
    }
    
    /**
     * 验证码加载完成后通知观察者
     * @author JimXia
     *
     * @date 2014年12月2日 上午10:49:27
     */
    public static interface OnVerifyCodeLoadDoneListener {
        void onVerifyCodeLoadBegin();
        void onVerifyCodeLoadDone(Bitmap verifyCode, LoadVerifyCodeErrorInfo error);
    }
    
    public static class LoadVerifyCodeErrorInfo {
        public static final int ERROR_CODE_NO_NETWORK = 1;
        public static final int ERROR_CODE_NETWORK_EXCEPTION = 2;
        public static final int ERROR_CODE_OTHER = 3;
        
        private int errorCode;
        
        LoadVerifyCodeErrorInfo(int errorCode) {
            this.errorCode = errorCode;
        }
        
        public int getErrorCode() {
            return errorCode;
        }
    }
    
    private static String getEvent(VC_EVENT event) {
        String strEvent = "";
        
        switch (event) {
            case VC_EVENT_REGISTER:
                strEvent = "register";
                break;
            case VC_EVENT_FINDPWD:
                strEvent = "findpwd";
                break;
            case VC_EVENT_LOGIN:
                strEvent = "login";
                break;
        }
        
        return strEvent;
    }
    
    public static enum VC_EVENT {
        VC_EVENT_REGISTER, // 注册
        VC_EVENT_FINDPWD, // 找回密码
        VC_EVENT_LOGIN // 登录
    }
}
