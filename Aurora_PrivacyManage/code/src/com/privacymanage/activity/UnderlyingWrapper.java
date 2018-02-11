/**
 * Vulcan created this file in 2014年10月13日 上午10:11:27 .
 */
package com.privacymanage.activity;



import com.privacymanage.data.AccountData;
import com.privacymanage.model.AccountModel;
import com.privacymanage.model.AccountModel.EmailSendingCallback;
import com.privacymanage.provider.AccountProvider;
import com.privacymanage.utils.LogUtils;
import com.privacymanage.utils.NetworkUtils;

/**
 * Vulcan created UnderlyingWrapper in 2014年10月13日 .
 * 
 */
public class UnderlyingWrapper{
	
	/**
	 * 
	 */
	public UnderlyingWrapper() {
	}
	

	/**
	 * 
	 * Vulcan created this method in 2014年10月11日 上午10:36:19 .
	 * @param email
	 * @return
	 */
	protected static boolean emailIsRegistered(String email) {
		return AccountProvider.isHadEmail(CustomApplication.getApplication(), email);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 上午11:03:39 .
	 * @param password
	 * @return
	 */
	protected static boolean passwordExists(String password) {
		return AccountProvider.isHadPassword(CustomApplication.getApplication(), password);
	}
	
	/**
	 * create privacy account by specifying password and email address
	 * Vulcan created this method in 2014年10月14日 下午2:17:34 .
	 * @param password
	 * @param eamail
	 * @return
	 */
	protected static AccountData createPrivacyAccount(String password, String email) {
		
		if(password.length() < 5) {
			LogUtils.printWithLogCat("vprivacy", String.format("createPrivacyAccount: password = %s, email = %s",password,email));
			LogUtils.printWithLogCat("vprivacy", "createPrivacyAccount: stack = " + LogUtils.StackToString(new Throwable()));
		}
		AccountData ad = AccountModel.getInstance().createAccount(password, email);
		return ad;
	}
	
	/**
	 * change email address of current account.
	 * Vulcan created this method in 2014年10月14日 下午2:59:36 .
	 * @param email
	 */
	protected static void thisAccountChangeEmail(String email) {
		AccountProvider.updateEmail(CustomApplication.getApplication(), AccountModel.getInstance().getCurAccount(), email);
		return;
	}
	
	/**
	 * change password of curent account.
	 * Vulcan created this method in 2014年10月14日 下午3:03:31 .
	 * @param password
	 */
	protected static void thisAccountChangePassword(String password) {
		AccountProvider.updatePassword(CustomApplication.getApplication(), AccountModel.getInstance().getCurAccount(), password);
	}
	

	/**
	 * send email with gotten-back password list(perhaps several passwords)
	 * Vulcan created this method in 2014年10月14日 下午3:14:35 .
	 * @param email
	 * @return 0, success, -1, failure
	 */
	protected static int sendGetbackPasswordEmail(String email,EmailSendingCallback callback) {
		LogUtils.printWithLogCat("vemail",
				String.format("sendGetbackPasswordEmail: email:%s,stack=%s",
						email,
						LogUtils.StackToString(new Throwable())));
		AccountModel.getInstance().sendEmailForBackPassword(email,callback);
		return 0;
	}
	

	/**
	 * send email to notify password conflict
	 * Vulcan created this method in 2014年10月14日 下午3:15:10 .
	 * @param password
	 * @return 0, success, -1, failure(timeout or other error)
	 */
	protected static int sendConflictEmail(String password,EmailSendingCallback callback) {
		//this method should be synchronized.
		//when it returns, the result is either success or timeout.
		AccountModel.getInstance().sendEmailForPasswordRepeat(password,new EmailSendingCallback() {
			@Override
			public void onNotifyEmailSendingResult(int error) {
			}
		});
		return 0;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月14日 下午3:27:49 .
	 * @param ad
	 */
	protected static void openPrivacyAccount(AccountData ad) {
		AccountModel.getInstance().enterPrivacyAccount(ad);
	}
	
	/**
	 * check network
	 * Vulcan created this method in 2014年10月14日 下午3:46:31 .
	 * @return true if network is available
	 */
	protected static boolean networkIsAvailable() {
		return NetworkUtils.isConn(CustomApplication.getApplication());
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月16日 上午9:55:52 .
	 * @return
	 */
	protected static boolean isInPrivacySpace() {
		boolean b = AccountModel.getInstance().isInPrivacySpaceNow();
		return b;
	}
	
	/**
	 * get the first bit of the password of the current account.
	 * Vulcan created this method in 2014年10月24日 上午11:36:17 .
	 * @return
	 */
	protected static int getFirstBitOfCurPassword() {
		String s = AccountModel.getInstance().getCurAccountFirstCharOfPassword();
		return Integer.parseInt(s);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月28日 下午4:55:37 .
	 * @return
	 */
	protected static String getEmailOfCurAccount() {
		return AccountModel.getInstance().getCurAccountEmail();
	}

}
