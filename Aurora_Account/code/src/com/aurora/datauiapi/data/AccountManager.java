package com.aurora.datauiapi.data;


import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.aurora.account.AccountApp;
import com.aurora.account.events.EventsType;
import com.aurora.account.events.NotificationCenter;
import com.aurora.account.http.data.HttpRequestGetAccountData;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.SystemUtils;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.ChangePhotoRespObject;
import com.aurora.datauiapi.data.bean.UserInfo;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.bean.UserRegisterObject;
import com.aurora.datauiapi.data.bean.UserVC;
import com.aurora.datauiapi.data.bean.ValidateVCObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.IAccountManager;
import com.aurora.datauiapi.data.interf.INotifiableController;


public class AccountManager extends BaseManager implements IAccountManager {

//    private static final String TAG = "AccountManager";
	private AccountPreferencesUtil mPref;
	
	public AccountManager(INotifiableController controller) {
	    super(controller);
	    mPref = AccountPreferencesUtil.getInstance(AccountApp.getInstance());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aurora.datauiapi.data.interf.IAccountManager#loginAccount(com.aurora
	 * .datauiapi.data.implement.DataResponse, android.content.Context,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void loginAccount(final DataResponse<UserLoginObject> response,
			final Context context, final String acctName,
			final String pwdMD5, final String imei, final String validCode,final int type) {
		mHandler.post(new Command<UserLoginObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetAccountData
						.getLoginObject(acctName, pwdMD5, imei, validCode,type);
				setResponse(response, result, UserLoginObject.class);
				if (response.value != null && response.value.getCode() == UserLoginObject.CODE_SUCCESS) {
				    saveUserInfoForLogin(response.value);
				}
			}
		});
	}
	
	/**
	 * 登录成功后保存服务器返回的用户信息
	 * @param userInfo
	 */
	private void saveUserInfoForLogin(UserLoginObject userInfo) {
	    mPref.setUserKey(userInfo.getUserKey());
	    mPref.setUserCookie(userInfo.getTgt());
	    final UserInfo info = userInfo.getUser();
	    if (info != null) {
	        final String oldUserId = mPref.getUserID();
	        if (!TextUtils.isEmpty(oldUserId) && !oldUserId.equals(info.getUserId())) {
	            // 切换帐号登录
	            BooleanPreferencesUtil.getInstance(AccountApp.getInstance()).setFirstTimeSync(true);
	            
	            Editor syncTimeEditor = AccountApp.getInstance().getSharedPreferences(
	                    Globals.SHARED_WIFI_SYNC, Activity.MODE_PRIVATE).edit();
	            syncTimeEditor.remove(Globals.SHARED_TIMESTMAP_SYNC_KEY);
	            syncTimeEditor.remove(Globals.SHARED_SERVERTIME_SYNC_KEY);
	            syncTimeEditor.commit();
	            
	            // 清除各模块的同步时间记录
	            SystemUtils.clearAppSyncTime();
	        }
	        mPref.setUserPhone(info.getPhoneNo());
	        mPref.setUserEmail(info.getEmail());
	        mPref.setUserID(info.getUserId());
	        mPref.setUserToken(info.getToken());
	        mPref.setUserNick(info.getNick());
	        mPref.setUserPhoneURL(info.getPhoto());
	       
	    }
	}
	
	/* (non-Javadoc)
	 * @see com.aurora.datauiapi.data.interf.IAccountManager#registerAccount(com.aurora.datauiapi.data.implement.DataResponse, android.content.Context, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void registerAccount(final DataResponse<UserRegisterObject> response,
			final Context context, final String phoneNo, final String email,final String pwd, final String pwdMD5,
			final String imei, final String vc,final String vcId, final String validCode) {
		mHandler.post(new Command<UserRegisterObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetAccountData
						.getRegisterObject(phoneNo,email,pwd,pwdMD5,imei, vc,vcId, validCode);
	
				setResponse(response, result, UserRegisterObject.class);
			}
		});
	}
	
	@Override
	public void getVerifyCode(final DataResponse<UserVC> response,
			final Context context, final String phoneNo,final String event, final String validCode, final String imei) {
		mHandler.post(new Command<UserVC>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetAccountData
						.getVerifyCodeObject(null, null, phoneNo, event, validCode, imei);
				System.out.println("getVerifyCode: " + result);
	
				setResponse(response, result, UserVC.class);
			}
		});
	}
	
	@Override
	public void getVerifyCode(final DataResponse<UserVC> response,final Context context,
            final String userId, final String userKey, final String phoneNo,final String event, final String validCode, final String imei) {
	    mHandler.post(new Command<UserVC>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData
                        .getVerifyCodeObject(userId, userKey, phoneNo,event, validCode, imei);
                System.out.println("getVerifyCode: " + result);
    
                setResponse(response, result, UserVC.class);
            }
        });
	}
	
	/**
     * 获取详情后保存服务器返回的用户信息
     * !!!注意服务器返回的信息没有userKey，所以不能保存userKey，不然会把之前的userKey冲掉
     * @param userInfo
     */
    private void saveUserInfoForDetail(UserInfo info) {
        if (info != null) {
            mPref.setUserPhone(info.getPhoneNo());
            mPref.setUserEmail(info.getEmail());
            mPref.setUserID(info.getUserId());
            mPref.setUserNick(info.getNick());
            mPref.setUserPhoneURL(info.getPhoto());
        }
    }
	
	@Override
	public void getUserInfo(final DataResponse<UserLoginObject> response,
			final Context context, final String userId, final String userKey) {
		mHandler.post(new Command<UserLoginObject>(response, this) {
			@Override
			public void doRun() throws Exception {
				String result = HttpRequestGetAccountData
						.getUserInfoObject(userId,userKey);
				setResponse(response, result, UserLoginObject.class);
				if (response.value != null && response.value.getCode() == UserLoginObject.CODE_SUCCESS) {
				    saveUserInfoForDetail(response.value.getUser());
                }
			}
		});
	}
	
    @Override
    public void resetLoginPwd(final DataResponse<BaseResponseObject> response, final Context context,
	        final String email, final String phoneNo,final String newPwd, final String newPwdMd5, final String vc, final String vcId,
	        final String validCode, final String imei) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
            	String result = HttpRequestGetAccountData
                        .resetLoginPwd(email, phoneNo, newPwd, newPwdMd5, vc, vcId, validCode, imei);
            	System.out.println("RESULT: " + result);
    
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }

    @Override
    public void logout(final DataResponse<BaseResponseObject> response, final Context context, final String userId,
            final String userKey) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.logout(userId, userKey);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }

    @Override
    public void updateAccountInfo(final DataResponse<UserLoginObject> response, Context context,
            final String userId, final String userKey, final String nickName) {
        mHandler.post(new Command<UserLoginObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.updateAccountInfo(userId, userKey, nickName);
                setResponse(response, result, UserLoginObject.class);
                if (response.value != null && response.value.getCode() == UserLoginObject.CODE_SUCCESS) {
                    saveUserInfoForDetail(response.value.getUser());
                    NotificationCenter.getInstance().notify(EventsType.EVENT_UPDATE_NICKNAME);
                }
            }
        });
    }
    
    @Override
    public void changePhoto(final DataResponse<ChangePhotoRespObject> response, final Context context, final String userId,
            final String userKey, final String fileName, final InputStream is) {
        mHandler.post(new Command<ChangePhotoRespObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.changePhoto(userId, userKey, fileName, is);
                setResponse(response, result, ChangePhotoRespObject.class);
                if (response.value != null && response.value.getCode() == ChangePhotoRespObject.CODE_SUCCESS) {
                    mPref.setUserPhoneURL(response.value.getPhoto());
                    NotificationCenter.getInstance().notify(EventsType.EVENT_UPDATE_ACCOUNT_ICON);
                }
            }
        });
    }

    @Override
    public void changeLoginPwd(final DataResponse<BaseResponseObject> response, Context context,
            final String userId, final String userKey, final String oldPwdMd5, final String newPwdMd5,
            final String oldPwd, final String newPwd) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.changeLoginPwd(userId, userKey, oldPwdMd5,
                        newPwdMd5, oldPwd, newPwd);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }

    @Override
    public void changePhoneNo(final DataResponse<BaseResponseObject> response, Context context,
            final String userId, final String userKey, final String newPhoneNo, final String vc,
            final String phoneStateCode, final String vcId) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.changePhoneNo(userId, userKey, newPhoneNo,
                        vc, phoneStateCode, vcId);
                setResponse(response, result, BaseResponseObject.class);
                if (response.value != null && response.value.getCode() == BaseResponseObject.CODE_SUCCESS) {
                    mPref.setUserPhone(newPhoneNo);
                    NotificationCenter.getInstance().notify(EventsType.EVENT_UPDATE_PHONE);
                }
            }
        });
    }

    @Override
    public void changeEmail(final DataResponse<BaseResponseObject> response, Context context,
            final String userId, final String userKey, final String newEmail) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.changeEmail(userId, userKey, newEmail);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }
    
    @Override
    public void resendVerifyEmail(final DataResponse<BaseResponseObject> response, Context context,
    		final String userId, final String userKey, final String email, final String event) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.resendVerifyEmail(userId, userKey, email, event);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }
    
    @Override
	public void validateFindpwdVc(final DataResponse<ValidateVCObject> response,
			Context context, final String imei, final String vc) {
    	mHandler.post(new Command<ValidateVCObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.validateFindpwdVc(imei, vc);
                setResponse(response, result, ValidateVCObject.class);
            }
        });
	}
	
	@Override
	public void postActivity() {
		/*
		 * if(failedRequests!=null){ failedRequests.clear(); }
		 */
		if (failedIORequests != null) {
			failedIORequests.clear();
		}
	}

    @Override
    public void checkCurPhone(final DataResponse<BaseResponseObject> response, Context context,
            final String userId, final String userKey, final String phoneNo, final String pwdMD5) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.checkCurPhone(userId, userKey, phoneNo, pwdMD5);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }

    @Override
    public void validateChgPhoneVc(final DataResponse<BaseResponseObject> response, Context context,
            final String userId, final String userKey, final String vc) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.validateChgPhoneVc(userId, userKey, vc);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }

    @Override
    public void checkCurEmail(final DataResponse<BaseResponseObject> response, Context context,
            final String userId, final String userKey, final String email) {
        mHandler.post(new Command<BaseResponseObject>(response, this) {
            @Override
            public void doRun() throws Exception {
                String result = HttpRequestGetAccountData.checkCurEmail(userId, userKey, email);
                setResponse(response, result, BaseResponseObject.class);
            }
        });
    }
}