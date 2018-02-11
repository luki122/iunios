package com.privacymanage.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.aurora.privacymanage.R;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.privacymanage.activity.ActivityMan;
import com.privacymanage.activity.CustomApplication;
import com.privacymanage.activity.PrivacyManageActivity;
import com.privacymanage.activity.UserGuide;
import com.privacymanage.data.AccountConfigData;
import com.privacymanage.data.AccountData;
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.data.ConfigData;
import com.privacymanage.data.HttpData;
import com.privacymanage.interfaces.AccountSubject;
import com.privacymanage.provider.AccountProvider;
import com.privacymanage.provider.ConfigProvider;
import com.privacymanage.provider.ModuleInfoProvider;
import com.privacymanage.request.http.HttpModel;
import com.privacymanage.request.http.SendEmailModel;
import com.privacymanage.utils.FileUtils;
import com.privacymanage.utils.LogUtils;
import com.privacymanage.utils.mConfig;

public class AccountModel extends AccountSubject {
    private final static Object sGlobalLock = new Object();
    private final int MSG_deleteAccount = 1;
    private final int MSG_accountSwitch = 2;
    private final int MSG_enterPrivacySpace = 3;
    private final int MSG_quitPrivacySpace = 4;
    private final int MSG_showToastForEnterPrivacySpace = 5;
    private static AccountModel sInstance;
    private final String TAG = AccountModel.class.getName();
    private Context mApplicationContext;
    private final Object mLock = new Object();
    private AtomicBoolean isDuringSendEmail = new AtomicBoolean(false);
    private UIHandler mUIhandler;
    private final HandlerThread mQueueThread;
    private final QueueHandler mQueueHandler;
    private final AccountData curAccount;
    private final AccountConfigData curAccountConfigData;
    private AtomicBoolean isKeepPrivacySpace = new AtomicBoolean(false);

    public interface EmailSendingCallback {
        public static final int SUCCESS = 0;
        public static final int ERROR_GENERAL = -1;
        public static final int ERROR_TIMEOUT = -2;
        public static final int ERROR_NET_BAD = -3;

        public void onNotifyEmailSendingResult(int error);
    }

    static public AccountModel getInstance() {
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new AccountModel();
            }
            return sInstance;
        }
    }

    private AccountModel() {
        mApplicationContext = CustomApplication.getApplication();
        curAccount = new AccountData();
        curAccountConfigData = new AccountConfigData();
        mUIhandler = new UIHandler(Looper.getMainLooper());
        mQueueThread = new HandlerThread(TAG + ":Background");
        mQueueThread.start();
        mQueueHandler = new QueueHandler(mQueueThread.getLooper());
    }

    /**
     * 查找当前账户的密保邮箱
     *
     * @return 返回值不可能为null
     */
    public String getCurAccountEmail() {
        return curAccount.getEMail();
    }

    /**
     * 获取当前帐号密码的第一位
     *
     * @return
     */
    public String getCurAccountFirstCharOfPassword() {
        String password = curAccount.getPassword();
        if (password == null || password.length() < 1) {
            return null;
        } else {
            return password.substring(0, 1);
        }
    }

    /**
     * 当前是否在锁屏时保持隐私状态
     *
     * @return true：保持  false：不保持
     */
    public boolean getIsKeepPrivacySpace() {
        return isKeepPrivacySpace.get();
    }

    /**
     * 当前是否在锁屏时保持隐私状态
     */
    public void changeIsKeepPrivacySpace() {
        isKeepPrivacySpace.set(isKeepPrivacySpace.get() ? false : true);
    }

    /**
     * 获取当前的账户信息
     *
     * @return 返回值不可能为null
     */
    public AccountData getCurAccount() {
        synchronized (mLock) {
            return this.curAccount;
        }
    }

    /**
     * 获取当前的账户的配置信息
     *
     * @return 返回值不可能为null
     */
    public AccountConfigData getAccountConfigData() {
        synchronized (mLock) {
            return this.curAccountConfigData;
        }
    }

    /**
     * 当前是不是在隐私空间中
     *
     * @return true:当前处于隐私空间   false：当前处于正常空间
     */
    public boolean isInPrivacySpaceNow() {
        if (curAccount.getAccountId() == AccountData.NOM_ACCOUNT) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 新建一个隐私账户
     *
     * @param password
     */
    public synchronized AccountData createAccount(String password, String eMail) {
        if (password == null) {
            return null;
        }
        ConfigData configData = ConfigModel.getInstance(mApplicationContext).getConfigData();
        long newAccountId = configData.getLastAccountId() + 1;
        String path = createFolderForNewAccount(newAccountId);
        if (path == null) {
            //2次创建
            path = createFolderForNewAccount(newAccountId);
        }
        AccountData tmpAccountData = new AccountData();
        tmpAccountData.setAccountId(newAccountId);
        tmpAccountData.setCreateTime(System.currentTimeMillis());
        tmpAccountData.setEMail(eMail);
        tmpAccountData.setHomePath(path);
        tmpAccountData.setPassword(password);
        tmpAccountData.setState(AccountData.NO_ENTERED);

        AccountConfigData tmpAccountConfigData = new AccountConfigData();
        tmpAccountConfigData.setMsgNotifySwitch(true);
        tmpAccountConfigData.setMsgNotifyHintStr(
                mApplicationContext.getString(R.string.have_new_system_msg));

        //保存必要的信息
        AccountProvider.insertOrUpdateDate(mApplicationContext, tmpAccountData);
        ConfigProvider.insertOrUpdateDate(mApplicationContext,
                tmpAccountConfigData, tmpAccountData.getAccountId());
        configData.setLastAccountId(newAccountId);
        ConfigModel.getInstance(mApplicationContext).saveConfigData();

        return tmpAccountData;
    }

    /**
     * 进入隐私空间
     *
     * @param password
     * @param needStartPrivacyManageActivity 是否要进入隐私管理界面
     */
    public void enterPrivacyAccount(String password, boolean needStartPrivacyManageActivity) {
        AccountData tmpAccountData = AccountProvider.
                getAccountInfoByPassword(mApplicationContext, password);
        synchronized (mLock) {
            Message msg = mQueueHandler.obtainMessage();
            msg.what = MSG_enterPrivacySpace;
            msg.obj = tmpAccountData;
            msg.arg1 = needStartPrivacyManageActivity ? 1 : 0;
            mQueueHandler.sendMessage(msg);
        }
    }

    /**
     * 进入隐私空间
     *
     * @param accountData
     * @return true:进入成功  false：进入失败
     */
    public void enterPrivacyAccount(AccountData accountData) {
        synchronized (mLock) {
            Message msg = mQueueHandler.obtainMessage();
            msg.what = MSG_enterPrivacySpace;
            msg.obj = accountData;
            mQueueHandler.sendMessage(msg);
        }
    }

    /**
     * 退出隐私空间
     */
    public void quitPrivacyAccount() {
        synchronized (mLock) {
            Message msg = mQueueHandler.obtainMessage();
            msg.what = MSG_quitPrivacySpace;
            mQueueHandler.sendMessage(msg);
        }
    }

    /**
     * 删除隐私账户
     *
     * @param delete true：删除隐私空间数据，false：还原隐私空间数据
     */
    public void deleteAccount(boolean delete) {
        synchronized (mLock) {
            Message msg = mQueueHandler.obtainMessage();
            msg.what = MSG_deleteAccount;
            msg.arg1 = delete ? 1 : 0;
            mQueueHandler.sendMessage(msg);
        }
    }

    final class QueueHandler extends Handler {
        public QueueHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_enterPrivacySpace:
                    enterPrivacyAccountFunc((AccountData) msg.obj, msg.arg1 == 1 ? true : false);
                    break;
                case MSG_quitPrivacySpace:
                    quitPrivacyAccountFunc();
                    break;
                case MSG_deleteAccount:
                    deleteAccountFunc(msg.arg1 == 1 ? true : false);
                    break;
            }
        }

        ;
    }

    private boolean enterPrivacyAccountFunc(AccountData accountData
            , boolean needStartPrivacyManageActivity) {
        boolean result = false;
        if (accountData != null) {
            if (accountData.getAccountId() == AccountData.NOM_ACCOUNT ||
                    accountData.getAccountId() == curAccount.getAccountId() ||
                    !checkForCanEnterPriavcyAccount()) {
                return result;
            }
            synchronized (mLock) {
                curAccount.copy(accountData);
            }
            sendBroadcastForStatusBar(true);
            ActivityMan.killAllActivitiesWithExcept(null);
            if (needStartPrivacyManageActivity) {
                Intent intent = new Intent(mApplicationContext, PrivacyManageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mApplicationContext.startActivity(intent);
            }
            notifyForSwitchAccount();
            result = true;
        }
        return result;
    }

    private void quitPrivacyAccountFunc() {
        if (curAccount.getAccountId() == AccountData.NOM_ACCOUNT) {
            return;
        }
        synchronized (mLock) {
            curAccount.reset();
        }
        sendBroadcastForStatusBar(false);
        notifyForSwitchAccount();
        ActivityMan.killAllPrivateActivitiesExcept(null);
    }

    private void deleteAccountFunc(boolean delete) {
        AccountData needDeleteAccountData = new AccountData();
        needDeleteAccountData.copy(curAccount);
        synchronized (mLock) {
            curAccount.reset();
        }

        ConfigProvider.deleteDate(mApplicationContext, needDeleteAccountData.getAccountId());
        AccountProvider.deleteDate(mApplicationContext, needDeleteAccountData.getAccountId());
        ModuleInfoProvider.deleteDate(mApplicationContext, needDeleteAccountData.getAccountId());

        AidlAccountData aidlAccountData = new AidlAccountData();
        aidlAccountData.setAccountId(needDeleteAccountData.getAccountId());
        aidlAccountData.setHomePath(needDeleteAccountData.getHomePath());
        Intent intent = new Intent(mConfig.ACTION_DELETE_ACCOUNT);
        intent.putExtra(mConfig.KEY_ACCOUNT, aidlAccountData);
        intent.putExtra(mConfig.KEY_DELETE, delete);
        mApplicationContext.sendBroadcast(intent);

        sendBroadcastForStatusBar(false);
        ActivityMan.killAllPrivateActivitiesExcept(null);

        try {
            Thread.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Message mUIhandlerMsg = mUIhandler.obtainMessage();
        mUIhandlerMsg.what = MSG_deleteAccount;
        mUIhandlerMsg.arg1 = delete ? 1 : 0;
        mUIhandlerMsg.obj = needDeleteAccountData;
        mUIhandler.sendMessage(mUIhandlerMsg);
    }

    /**
     * 向状态栏发送广播，通知当前是否进度隐私空间
     *
     * @param isEnterPrivacySpace
     */
    private void sendBroadcastForStatusBar(boolean isEnterPrivacySpace) {
        //功能暂时屏蔽
//		Intent intent = new Intent("aurora.action.SET_STATUSBAR_COLOR");
//		intent.putExtra("isEnterPrivacySpace", isEnterPrivacySpace);
//		mApplicationContext.sendBroadcast(intent);	
    }

    final class UIHandler extends Handler {
        public UIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_deleteAccount:
                    notifyObserversOfDeleteAccount((AccountData) msg.obj, msg.arg1 == 1 ? true : false);
                    Toast.makeText(mApplicationContext, R.string.deleted_privacy_space,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MSG_accountSwitch:
                    notifyObserversOfSwitchAccount(curAccount);
                    break;
                case MSG_showToastForEnterPrivacySpace:
                    if (curAccount.getAccountId() != AccountData.NOM_ACCOUNT) {
                        Toast.makeText(mApplicationContext, R.string.enter_privacy_space,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    /**
     * 账户切换后，发送通知事件
     */
    private void notifyForSwitchAccount() {
        AccountConfigData tmpAccountConfigData = ConfigProvider.
                getAccountConfigInfo(mApplicationContext, curAccount.getAccountId());
        synchronized (mLock) {
            if (tmpAccountConfigData == null) {
                curAccountConfigData.setMsgNotifySwitch(true);
                curAccountConfigData.setMsgNotifyHintStr(
                        mApplicationContext.getString(R.string.have_new_system_msg));
            } else {
                curAccountConfigData.copy(tmpAccountConfigData);
            }
        }

        LogUtils.printWithLogCat(TAG, "account switch ,id=" + curAccount.getAccountId());
        ChildModuleModel.getInstance().updatePrivayItemNumWhenAccountSwitch();

        AidlAccountData aidlAccountData = new AidlAccountData();
        aidlAccountData.setAccountId(curAccount.getAccountId());
        aidlAccountData.setHomePath(curAccount.getHomePath());
        Intent intent = new Intent(mConfig.ACTION_SWITCH_ACCOUNT);
        intent.putExtra(mConfig.KEY_ACCOUNT, aidlAccountData);
        mApplicationContext.sendBroadcast(intent);

        Message mUIhandlerMsg = mUIhandler.obtainMessage();
        mUIhandlerMsg.what = MSG_accountSwitch;
        mUIhandler.sendMessage(mUIhandlerMsg);

        if (curAccount.getAccountId() != AccountData.NOM_ACCOUNT) {
            try {
                Thread.sleep(800);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mUIhandlerMsg = mUIhandler.obtainMessage();
            mUIhandlerMsg.what = MSG_showToastForEnterPrivacySpace;
            mUIhandler.sendMessage(mUIhandlerMsg);
        }
    }

    /**
     * 判断是否能进入隐私空间（只有从正常空间，才能进入到隐私空间）
     *
     * @return true:可以进入隐私空间    false:不可进入隐私空间
     */
    private boolean checkForCanEnterPriavcyAccount() {
        if (curAccount.getAccountId() == AccountData.NOM_ACCOUNT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 当密码重复时发送邮件
     *
     * @param repeat
     */
    public void sendEmailForPasswordRepeat(final String password,
                                           final EmailSendingCallback callback) {
        final String email;
        AccountData accountData = AccountProvider.
                getAccountInfoByPassword(mApplicationContext, password);
        if (accountData != null) {
            email = accountData.getEMail();
        } else {
            email = null;
        }
        if (email == null) {
            return;
        }

        if (isDuringSendEmail.get()) {
            return;
        }
        isDuringSendEmail.set(true);
        new Thread() {
            @Override
            public void run() {
                SendEmailModel sendEmailModel = new SendEmailModel(
                        mApplicationContext, 1, email, password);
                sendEmailModel.postRequest();
                endOfSendEmail(sendEmailModel, callback);
                isDuringSendEmail.set(false);
            }
        }.start();
    }

    /**
     * 找回密码时发送邮件
     *
     * @param repeat
     */
    public void sendEmailForBackPassword(final String email,
                                         final EmailSendingCallback callback) {
        final List<String> passwordList = AccountProvider.
                queryPasswordByEmail(mApplicationContext, email);
        if (passwordList.size() == 0) {
            return;
        }

        if (isDuringSendEmail.get()) {
            return;
        }
        isDuringSendEmail.set(true);
        new Thread() {
            @Override
            public void run() {
                SendEmailModel sendEmailModel = new SendEmailModel(
                        mApplicationContext, 0, email, passwordList.toString());
                sendEmailModel.postRequest();
                endOfSendEmail(sendEmailModel, callback);
                isDuringSendEmail.set(false);
            }
        }.start();
    }

    private synchronized void endOfSendEmail(SendEmailModel sendEmailModel,
                                             EmailSendingCallback callback) {
        if (sendEmailModel == null || callback == null) {
            return;
        }
        if (sendEmailModel.getErrorCode() == null) {
            HttpData.STATUS status = sendEmailModel.getHttpData().getRequestStatus();
            if (status == HttpData.STATUS.SUCESS) {
                callback.onNotifyEmailSendingResult(EmailSendingCallback.ERROR_GENERAL);
            } else if (status == HttpData.STATUS.ERROR_OF_NET) {
                callback.onNotifyEmailSendingResult(EmailSendingCallback.ERROR_NET_BAD);
            } else if (status == HttpData.STATUS.ERROR_OF_TIMEOUT) {
                callback.onNotifyEmailSendingResult(EmailSendingCallback.ERROR_TIMEOUT);
            } else {
                callback.onNotifyEmailSendingResult(EmailSendingCallback.ERROR_GENERAL);
            }
        } else if (HttpModel.SUCCESS_CODE.equals(sendEmailModel.getErrorCode())) {
            callback.onNotifyEmailSendingResult(EmailSendingCallback.SUCCESS);
        } else {
            callback.onNotifyEmailSendingResult(EmailSendingCallback.ERROR_GENERAL);
        }
    }

    private String createFolderForNewAccount(long accountId) {
        String path = FileSyncModel.getInstance(mApplicationContext).getSoftPath() + accountId + File.separator;
        if (FileUtils.makeDir(path)) {
            return path;
        } else {
            LogUtils.printWithLogCat(TAG, "path make eror:" + path);
            return null;
        }
    }

    public static void releaseObject() {
        if (sInstance != null) {
            sInstance = null;
        }
    }
}
