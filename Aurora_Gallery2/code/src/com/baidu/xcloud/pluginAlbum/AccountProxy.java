package com.baidu.xcloud.pluginAlbum;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.account.AuthInfo;
import com.baidu.xcloud.account.AuthResponse;
import com.baidu.xcloud.account.IAuth;
import com.baidu.xcloud.account.IAuthExpireListener;
import com.baidu.xcloud.account.IAuthLoginListener;

public class AccountProxy {

    private static final String TAG = "AccountProxy";

    public interface IAccountInfoListener {
        void onComplete(AccountInfo account);

        void onException(String errorMsg);

        void onCancel();
    }

    public static final String NAME_PREF_XCLOUD_ACCOUNT = "name_xcloud_account";
    private static final String KEY_PREF_ACCOUNT_UID = "key_account_uid";
    private static final String KEY_PREF_ACCOUNT_NAME = "key_account_name";
    private static final String KEY_PREF_ACCOUNT_TOKEN = "key_account_token";
    public static final String KEY_PREF_THIRD_TOKEN = "key_third_token";

    /** 单例句柄 **/
    private static AccountProxy mInstance;

    // 登录后的账户信息
    private AccountInfo mAccount;

    // context
    private Context mContext;

    // 开发者的应用ID
    private String mAppID;

    // 开发者密钥
    private String mApiKey;

    private String mThirdAccessToken;

    // 开发者需要的权限
    private List<String> mPermissions;

    // 登录aidl接口
    private IAuth mAuth;

    // 与service的连接
    private ServiceConnection mConnection;

	// 是否正在连接service
	private boolean mConnecting;
	// 登陆页面正在加载对话框
	// private ProgressDialog mProgressDialog;
	// 结束dialog
	private static final int STATE_FINISH = 3;
	// 登陆时被取消
	private static final int STATE_CANCEL = 6;
	// 登录时异常
	private static final int STATE_EXCEPTION = 7;

    public static synchronized AccountProxy getInstance() {
        if (mInstance == null) {
            mInstance = new AccountProxy();
        }
        return mInstance;
    }

    private AccountProxy() {
    }

	/**
	 * 初始化
	 * 
	 * @param context
	 *            有效的Context对象
	 * @param appid
	 *            开发者的应用id
	 * @param apiKey
	 *            开发者应用密钥
	 * @param permissions
	 *            开发者需要使用的百度云权限
	 */
	public void init(Context context, String appid, String apiKey,
			String thirdAccessToken, String[] permissions) {
		Log.d(TAG, "init: ---start");
		mContext = context.getApplicationContext();
		pref=context.getSharedPreferences(
				NAME_PREF_XCLOUD_ACCOUNT, Context.MODE_PRIVATE);
		mAppID = appid;
		mApiKey = apiKey;
		mThirdAccessToken = thirdAccessToken;
		mPermissions = new ArrayList<String>();
		for (String permission : permissions) {
			mPermissions.add(permission);
		}
		// mProgressDialog = new ProgressDialog(context);
		// mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //
		// 设置Dialog无标题
		// mProgressDialog.setMessage("正在加载..."); // 设置Dialog显示文字内容
		// // 设置Dialog为SYSTEM_ALERT，便于前端显示.注意manifest中要申请相应权限
		// mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		bindAccountService();
		Log.d(TAG, "init: ---end");
	}

    /**
     * 关闭连接
     */
    public void close() {
        if (mAuth != null && mConnection != null && mContext != null) {
            mContext.unbindService(mConnection);
        }
        mAccount = null;
    }

	public void setThirdToken(String token) {
		this.mThirdAccessToken = token;
	}
	
	private SharedPreferences pref = null;

    public void getAccountInfo(final IAccountInfoListener listener, boolean forceLogin, int loginType) {
        Log.d(TAG, "getAccountInfo: ---start");

		if (listener == null) {
			throw new RuntimeException("IAccountInfoListener is null.");
		}
		
		// 读缓存
		if (mAccount == null) {
			if (pref.contains(KEY_PREF_ACCOUNT_UID)
					&& pref.contains(KEY_PREF_ACCOUNT_TOKEN)) {
				String uid = pref.getString(KEY_PREF_ACCOUNT_UID, null);
				String token = pref.getString(KEY_PREF_ACCOUNT_TOKEN, null);
				String uname = pref.getString(KEY_PREF_ACCOUNT_NAME, null);
				//LogUtil.d(TAG, "cache  uid::" + uid + " token::" + token
				//		+ " uname::" + uname);
				if (!TextUtils.isEmpty(token)&&!TextUtils.isEmpty(uid)) {
					uid = TextUtils.isEmpty(uname) ? uname : uid;
					mAccount = new AccountInfo(token, uid, mAppID);
					mAccount.add("userName", uname);
					//LogUtil.d(TAG, "cache mAccount ::" + mAccount.toString());
				}
			}
		}
		// 如果缓存也是空的
		if (mAccount == null || forceLogin) {
			// if (!mProgressDialog.isShowing()) {
			// mProgressDialog.show();
			// }
            login(new IAuthLoginListener.Stub() {
                public void onException(String errorMsg) throws RemoteException {
                    listener.onException(errorMsg);
                }

                public void onComplete(AuthResponse response) throws RemoteException {
                    String uid = response.getUserId();
                    String uname = response.getUserName();
                    String token = response.getAccessToken();
                    uid = TextUtils.isEmpty(uname) ? uname : uid;
                    mAccount = new AccountInfo(token, uid, mAppID);
                    mAccount.add("userName", uname);

					// 写缓存
					// SharedPreferences pref = mContext.getSharedPreferences(
					// NAME_PREF_XCLOUD_ACCOUNT, Context.MODE_PRIVATE);
					Editor e = pref.edit();
					e.putString(KEY_PREF_ACCOUNT_UID, uid);
					e.putString(KEY_PREF_ACCOUNT_TOKEN, token);
					e.putString(KEY_PREF_ACCOUNT_NAME, uname);
					e.commit();
					LogUtil.d(TAG,
							"onComplete mAccount ::" + mAccount.toString());
					listener.onComplete(mAccount);
				}

                public void onCancel() throws RemoteException {
                    listener.onCancel();
                }

				@Override
				public void onStateChanged(int state) throws RemoteException {
					switch (state) {
					case STATE_CANCEL:
					case STATE_EXCEPTION:
					case STATE_FINISH:
						// mProgressDialog.dismiss();
						break;
					default:
						break;
					}
				}
			}, loginType);
		} else {
			listener.onComplete(mAccount);
		}
	}
    
  //wenyongzhe 2016.2.18 start
    public void getAccountInfoNotLogin(final IAccountInfoListener listener, boolean forceLogin, int loginType) {
        Log.d(TAG, "getAccountInfo: ---start");

		if (listener == null) {
			throw new RuntimeException("IAccountInfoListener is null.");
		}
		
		// 读缓存
		if (mAccount == null) {
			if (pref.contains(KEY_PREF_ACCOUNT_UID)
					&& pref.contains(KEY_PREF_ACCOUNT_TOKEN)) {
				String uid = pref.getString(KEY_PREF_ACCOUNT_UID, null);
				String token = pref.getString(KEY_PREF_ACCOUNT_TOKEN, null);
				String uname = pref.getString(KEY_PREF_ACCOUNT_NAME, null);
				if (!TextUtils.isEmpty(token)&&!TextUtils.isEmpty(uid)) {
					uid = TextUtils.isEmpty(uname) ? uname : uid;
					mAccount = new AccountInfo(token, uid, mAppID);
					mAccount.add("userName", uname);
					//LogUtil.d(TAG, "cache mAccount ::" + mAccount.toString());
				}
			}
		}
		// 如果缓存也是空的
		if (mAccount == null || forceLogin) {
		} else {
			listener.onComplete(mAccount);
		}
	}
  //wenyongzhe 2016.2.18 end

	private LoginThread loginThread;

	private void login(IAuthLoginListener listener, int loginType) {
		AuthInfo authInfo = new AuthInfo(mApiKey, mAppID,
				AlbumConfig.SECRETKEY, mThirdAccessToken, mPermissions,
				loginType);
	//Log.i(TAG, "appkey:" + mApiKey + "appid:" + mAppID + "token:"
	//			+ mThirdAccessToken);

		// try {
		// mAuth.startAuth(authInfo, listener);
		// } catch (RemoteException e) {
		// e.printStackTrace();
		// }
		loginThread = new LoginThread(listener, authInfo);
		loginThread.start();
	}

	class LoginThread extends Thread {
		private IAuthLoginListener listener;
		private AuthInfo authInfo;

		public LoginThread(IAuthLoginListener listener, AuthInfo authInfo) {
			super();
			this.listener = listener;
			this.authInfo = authInfo;
		}

		protected Object mLock = new Object();

		@Override
		public void run() {
			if (mAuth == null) {
				synchronized (mLock) {
					try {
						mLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			Looper.prepare();
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					try {
						mAuth.startAuth(authInfo, listener);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
			Looper.loop();

		}

		public void notifyTask() {
			try {
				synchronized (mLock) {
					mLock.notify();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public void logout(IAuthExpireListener listener) {
        bindAccountService();
		if (mAuth == null){
			Log.w(TAG, "logout :: mAuth == null");
			return;
		}

		String tokent = pref.getString(KEY_PREF_ACCOUNT_TOKEN, null);
        try {
            mAuth.expireToken(tokent, listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

	public boolean hasLogout(){
		return (null == pref.getString(KEY_PREF_ACCOUNT_TOKEN, null));
	}

	/**
	 * ADD BY JXH Clear Login Cache
	 */
	public void logOutClearCache() {
		LogUtil.d(TAG, "logOutClearCache");
		if (mContext == null) {
			return;
		}
		mAccount = null;
		Editor e = pref.edit();
		e.putString(KEY_PREF_ACCOUNT_UID, null);
		e.putString(KEY_PREF_ACCOUNT_TOKEN, null);
		e.putString(KEY_PREF_ACCOUNT_NAME, null);
		e.commit();
	}
	
    private void bindAccountService() {
        
        if (mAuth == null && !mConnecting) {
            mConnecting = true;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(AlbumConfig.SERVICE_PKG, "com.baidu.xcloud.account.AuthService"));
            mConnection = new ServiceConnection() {
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "onServiceDisconnected");
                    mAuth = null;
                    mConnection = null;
                    mConnecting = false;
                }

                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(TAG, "onServiceConnected");
                    mAuth = IAuth.Stub.asInterface(service);
                    mConnecting = false;
					if (loginThread != null) {
						loginThread.notifyTask();
					}
					Log.d(TAG, "bindAccountService done");
				}
            };
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			Log.d(TAG, "bindAccountService start");
        }
        
    }

}
