package com.aurora.note.wxapi;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sina.sso.RemoteSSO;
import com.sina.sso.RemoteSSO.Stub;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboDialogException;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.SecurityHelper;
import java.util.List;

public class SsoHandler
{
  private static final String TAG = "Weibo_SSO_login";
  private static final String DEFAULT_SINA_WEIBO_PACKAGE_NAME = "com.sina.weibo";
  private static final String DEFAULT_WEIBO_REMOTE_SSO_SERVICE_NAME = "com.sina.weibo.remotessoservice";
  private static final int REQUEST_CODE_SSO_AUTH = 32973;
  private WeiboAuth mWeiboAuth;
  private WeiboAuthListener mAuthListener;
  private Activity mAuthActivity;
  private int mSSOAuthRequestCode;
  private ServiceConnection mConnection = new ServiceConnection()
  {
    public void onServiceDisconnected(ComponentName name) {
      SsoHandler.this.mWeiboAuth.anthorize(SsoHandler.this.mAuthListener);
    }

    public void onServiceConnected(ComponentName name, IBinder service)
    {
      RemoteSSO remoteSSOservice = RemoteSSO.Stub.asInterface(service);
      try {
        String ssoPackageName = remoteSSOservice.getPackageName();
        String ssoActivityName = remoteSSOservice.getActivityName();

        SsoHandler.this.mAuthActivity.getApplicationContext().unbindService(SsoHandler.this.mConnection);

        if (!SsoHandler.this.startSingleSignOn(ssoPackageName, ssoActivityName))
          SsoHandler.this.mWeiboAuth.anthorize(SsoHandler.this.mAuthListener);
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  };

  public SsoHandler(Activity activity, WeiboAuth weiboAuth)
  {
    this.mAuthActivity = activity;
    this.mWeiboAuth = weiboAuth;
  }

  public void authorize(WeiboAuthListener listener)
  {
    authorize(32973, listener, null);
  }

  public void authorize(WeiboAuthListener listener, String packageName)
  {
    authorize(32973, listener, packageName);
  }

  public void authorize(int requestCode, WeiboAuthListener listener, String packageName)
  {
    this.mSSOAuthRequestCode = requestCode;
    this.mAuthListener = listener;

    boolean bindSucced = bindRemoteSSOService(this.mAuthActivity.getApplicationContext(), packageName);

    if ((!bindSucced) && 
      (this.mWeiboAuth != null))
      this.mWeiboAuth.anthorize(this.mAuthListener);
  }

  public void authorizeCallBack(int requestCode, int resultCode, Intent data)
  {
    LogUtil.d("Weibo_SSO_login", "requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + data);
    if (requestCode == this.mSSOAuthRequestCode)
    {
      if (resultCode == -1) {
        if (!SecurityHelper.checkResponseAppLegal(this.mAuthActivity, data)) {
          return;
        }

        String error = data.getStringExtra("error");
        if (error == null) {
          error = data.getStringExtra("error_type");
        }

        if (error != null) {
          if ((error.equals("access_denied")) || (error.equals("OAuthAccessDeniedException"))) {
            LogUtil.d("Weibo_SSO_login", "Login canceled by user.");

            this.mAuthListener.onCancel();
          } else {
            String description = data.getStringExtra("error_description");
            if (description != null) {
              error = error + ":" + description;
            }

            LogUtil.d("Weibo_SSO_login", "Login failed: " + error);
            this.mAuthListener.onWeiboException(
              new WeiboDialogException(error, resultCode, description));
          }
        }
        else {
          Bundle bundle = data.getExtras();
          Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);

          if ((accessToken != null) && (accessToken.isSessionValid())) {
            LogUtil.d("Weibo_SSO_login", "Login Success! " + accessToken.toString());
            this.mAuthListener.onComplete(bundle);
          } else {
            LogUtil.d("Weibo_SSO_login", "Failed to receive access token by SSO");

            this.mWeiboAuth.anthorize(this.mAuthListener);
          }
        }

      }
      else if (resultCode == 0)
      {
        if (data != null) {
          LogUtil.d("Weibo_SSO_login", "Login failed: " + data.getStringExtra("error"));
          this.mAuthListener.onWeiboException(
            new WeiboDialogException(
            data.getStringExtra("error"), 
            data.getIntExtra("error_code", -1), 
            data.getStringExtra("failing_url")));
        }
        else {
          LogUtil.d("Weibo_SSO_login", "Login canceled by user.");
          this.mAuthListener.onCancel();
        }
      }
    }
  }

  public static ComponentName isServiceExisted(Context context, String packageName)
  {
    ActivityManager activityManager = 
      (ActivityManager)context.getSystemService("activity");
    List<RunningServiceInfo> serviceList = 
      activityManager.getRunningServices(2147483647);

    for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceList) {
      ComponentName serviceName = runningServiceInfo.service;

      if ((serviceName.getPackageName().equals(packageName)) && 
        (serviceName.getClassName().equals(packageName + ".business.RemoteSSOService"))) {
        return serviceName;
      }

    }

    return null;
  }

  private boolean bindRemoteSSOService(Context context, String packageName)
  {
    String tempPkgName = (TextUtils.isEmpty(packageName)) || (packageName.trim().equals("")) ? 
      "com.sina.weibo" : packageName;
    Intent intent = new Intent("com.sina.weibo.remotessoservice");
    intent.setPackage(tempPkgName);

    /*if (!context.bindService(intent, this.mConnection, 1)) {
      intent = new Intent("com.sina.weibo.remotessoservice");
      return context.bindService(intent, this.mConnection, 1);
    }

    return true;*/

    return context.bindService(intent, this.mConnection, 1);
  }

  private boolean startSingleSignOn(String ssoPackageName, String ssoActivityName)
  {
    boolean bSucceed = true;
    Intent intent = new Intent();

    intent.setClassName(ssoPackageName, ssoActivityName);

    intent.putExtras(this.mWeiboAuth.getAuthInfo().getAuthBundle());

    intent.putExtra("_weibo_command_type", 3);
    intent.putExtra("_weibo_transaction", String.valueOf(System.currentTimeMillis()));

    if (!SecurityHelper.validateAppSignatureForIntent(this.mAuthActivity, intent)) {
      return false;
    }

    try
    {
      this.mAuthActivity.startActivityForResult(intent, this.mSSOAuthRequestCode);
    } catch (ActivityNotFoundException e) {
      bSucceed = false;
    }

    return bSucceed;
  }
}
