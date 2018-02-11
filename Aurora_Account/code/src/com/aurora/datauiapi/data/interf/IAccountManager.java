package com.aurora.datauiapi.data.interf;

import java.io.InputStream;

import android.content.Context;

import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.ChangePhotoRespObject;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.bean.UserRegisterObject;
import com.aurora.datauiapi.data.bean.UserVC;
import com.aurora.datauiapi.data.bean.ValidateVCObject;
import com.aurora.datauiapi.data.implement.DataResponse;



public interface IAccountManager {

	
	/** 
	* @Title: loginAccount
	* @Description: 登录
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void loginAccount(final DataResponse<UserLoginObject> response,final Context context,final String acctName,
	        final String pwdMD5,final String imei, final String validCode,final int type);
	
	/** 
	* @Title: registerAccount
	* @Description: 注册
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void registerAccount(final DataResponse<UserRegisterObject> response,final Context context,final String phoneNo,final String email,final String pwd,final String pwdMD5,final String imei,final String vc,final String vcId, final String validCode);
	
	/** 
	* @Title: getVerifyCode
	* @Description: 获取验证码
	* @param @param response
	* @param @param context
	* @return void
	* @throws 
	*/ 
	public void getVerifyCode(final DataResponse<UserVC> response,final Context context,final String phoneNo,final String event, final String validCode, final String imei);
	
	/**
	 * 修改绑定手机号获取验证码
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param phoneNo
	 * @param event
	 */
	public void getVerifyCode(final DataResponse<UserVC> response,final Context context,
	        final String userId, final String userKey, final String phoneNo,final String event, final String validCode, final String imei);
		
	/**
	 * 获取账户详情
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 */
	public void getUserInfo(final DataResponse<UserLoginObject> response,final Context context,final String userId,final String userKey);
	
	/**
	 * 手机号重置密码接口
	 * @param response
	 * @param context
	 * @param phoneNo
	 * @param newPwdMd5
	 * @param vc
	 */
	public void resetLoginPwd(final DataResponse<BaseResponseObject> response, final Context context,
	        final String email, final String phoneNo,final String newPwd, final String newPwdMd5, final String vc, final String vcId, final String validCode, final String imei);
	
	/**
	 * 退出登录接口
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 */
	public void logout(final DataResponse<BaseResponseObject> response, final Context context,
	        final String userId, final String userKey);
	
	/**
	 * 修改帐号信息
	 * @param response
	 * @param context
	 * @param userInfo
	 */
	public void updateAccountInfo(final DataResponse<UserLoginObject> response, final Context context,
	        final String userId, final String userKey, final String nickName);
	
	/**
	 * 修改图像接口
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param is
	 */
	public void changePhoto(final DataResponse<ChangePhotoRespObject> response, final Context context,
            final String userId, final String userKey, String fileName, final InputStream is);
	
	/**
	 * 修改登录密码接口
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param oldPwdMd5
	 * @param newPwdMd5
	 */
	public void changeLoginPwd(final DataResponse<BaseResponseObject> response, final Context context,
	        final String userId, final String userKey, final String oldPwdMd5, final String newPwdMd5,
	        final String oldPwd, final String newPwd);
	
	/**
	 * 修改绑定手机
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param newPhoneNo
	 * @param vc
	 */
	public void changePhoneNo(final DataResponse<BaseResponseObject> response, final Context context,
	        final String userId, final String userKey, final String newPhoneNo, final String vc,
	        final String phoneStateCode, final String vcId);
	
	/**
	 * 修改绑定手机号时，验证当前绑定的手机号
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param phoneNo
	 * @param pwdMD5
	 */
	public void checkCurPhone(final DataResponse<BaseResponseObject> response, final Context context,
	        final String userId, final String userKey,final String phoneNo, final String pwdMD5);
	
	/**
	 * 修改绑定手机号时，验证发送给当前绑定手机号的验证码
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param vc
	 */
	public void validateChgPhoneVc(final DataResponse<BaseResponseObject> response, final Context context,
            final String userId, final String userKey,final String vc);
	
	
	/**
	 * 修改绑定邮箱时，验证当前绑定的邮箱
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param email
	 */
	public void checkCurEmail(final DataResponse<BaseResponseObject> response, final Context context,
            final String userId, final String userKey, final String email);
	
	/**
	 * 修改绑定邮箱
	 * @param response
	 * @param context
	 * @param userId
	 * @param userKey
	 * @param newEmail
	 */
	public void changeEmail(final DataResponse<BaseResponseObject> response, final Context context,
	        final String userId, final String userKey, final String newEmail);
	
	/**
	* 重发邮箱验证
	* @param @param response
	* @param @param context
	* @param @param email
	* @param @param event 注册时：register；绑定邮箱时：bindemail
	* @return void
	* @throws
	 */
	public void resendVerifyEmail(final DataResponse<BaseResponseObject> response, Context context,
			final String userId, final String userKey, final String email, final String event);
	
	/**
	 * 检验找回密码手机验证码
	* @Title: validateFindpwdVc
	* @Description: TODO(这里用一句话描述这个方法的作用)
	* @param @param response
	* @param @param context
	* @param @param imei
	* @param @param vc
	* @return void
	* @throws
	 */
	public void validateFindpwdVc(final DataResponse<ValidateVCObject> response, Context context, final String imei, String vc);
	
	/**
	 * Put in here everything that has to be cleaned up after leaving an activity.
	 */
	public void postActivity();
	

}
