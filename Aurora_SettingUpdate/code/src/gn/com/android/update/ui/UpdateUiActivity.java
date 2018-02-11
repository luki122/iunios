
package gn.com.android.update.ui;

import java.security.GeneralSecurityException;
import java.text.NumberFormat;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;

import aurora.app.AuroraAlertDialog;
/*
 import aurora.app.AuroraActivity;
 import aurora.widget.AuroraActionBar;
 import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
 import aurora.widget.AuroraMenu;
 import aurora.widget.AuroraMenu.OnAuroraMenuItemClickListener;
 */
import aurora.app.AuroraProgressDialog;
import gn.com.android.update.business.Config;
import gn.com.android.update.business.ErrorStateException;
import gn.com.android.update.business.IOtaCheckLocalUpgradeFileCallback;
import gn.com.android.update.business.IOtaCheckVersionCallback;
import gn.com.android.update.business.IOtaDownloadCallback;
import gn.com.android.update.business.IOtaPauseDownloadCallback;
import gn.com.android.update.business.IOtaUpgradeCallback;
import gn.com.android.update.business.NetworkConfig;
import gn.com.android.update.business.NetworkConfig.ConnectionType;
import gn.com.android.update.business.job.CheckVersionJob;
import gn.com.android.update.business.job.OtaCheckLocalUpdateFileJob;
import gn.com.android.update.business.job.Job.JobEvent;
import gn.com.android.update.business.job.Job.JobEventListener;
import gn.com.android.update.business.job.Job.JobEventType;
import gn.com.android.update.business.NoSpaceException;
import gn.com.android.update.business.OtaUpgradeInfo;
import gn.com.android.update.business.OtaUpgradeManager;
import gn.com.android.update.business.OtaUpgradeState;
import gn.com.android.update.settings.ApplicationDataManager;
import gn.com.android.update.settings.ApplicationDataManager.DataOwner;
import gn.com.android.update.settings.ApplicationDataManager.OtaSettingKey;
import gn.com.android.update.settings.OtaSettings;
import gn.com.android.update.settings.OtaSettings.AutoCheckCycle;
import gn.com.android.update.ui.anim.OTAFrameAnimation.AnimationImageListener;
import gn.com.android.update.ui.view.FrameImageView;
import gn.com.android.update.ui.view.OTAMainPageFrameLayout;
import gn.com.android.update.ui.view.TweenFrameLayout;
import gn.com.android.update.utils.AnimUtils;
import gn.com.android.update.utils.BatteryUtil;
import gn.com.android.update.utils.DensityUtil;
import gn.com.android.update.utils.Error;
import gn.com.android.update.utils.FileUtil;
import gn.com.android.update.utils.HttpUtils;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.MSG;
import gn.com.android.update.utils.NotificationUtil;
import gn.com.android.update.utils.OtaInent;
import gn.com.android.update.utils.StorageUtil;
import gn.com.android.update.utils.Util;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import gn.com.android.update.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.ota.database.RepoterManager;
import com.aurora.ota.reporter.Constants;
import com.youju.statistics.YouJuAgent;
import com.youju.statistics.util.Utils;

import com.aurora.utils.SystemUtils;

public class UpdateUiActivity extends Activity implements AnimationImageListener {

    private static final String TAG = "UpdateUiActivity";
    private static final int DIALOG_ID_NO_SPACE = 1;
    private static final int DIALOG_ID_FILE_MD5_CHECK_ERR = 2;
    private static final int DIALOG_ID_NET_MOBILE = 3;
    private static final int DIALOG_ID_EXIT_UPGRADE = 4;
    private static final int DIALOG_ID_CHECK_LOCAL_UPDATE_FILE = 5;
    private static final int DIALOG_ID_CHECK_LOCAL_UPDATE_FILE_WRONG = 6;
    private static final int DIALOG_ID_RESART_UPGRADE = 7;
    private static final int DIALOG_ID_CHARGE = 8;
    private static final int DIALOG_ID_EEMC_NO_SPACE = 10;
    private static final int DIALOG_ID_CHECK_UPDATE_FILE_WRONG = 11;
   
    private static final int DIALOG_ID_MOBILE_NET = 12;
    private static final int DIALOG_ID_CANCEL_DOWNLOAD = 13;
    private static final int DIALOG_ID_RESTART_PROGRESS = 14;
    
    private static final int DIALOG_NOTICE_RECOVERY_UPDATED = 15;
    
    private static final int VIBRATE_LONG = 100;
    private static final int RESULT_CODE_FOR_FILEMANAGER = 100001;
    private static final int MESSAGE_NOTIFY_FRAME_ANIMOTION_END = 101;
    private static final int MESSAGE_NOTIFY_HAVA_NEW_VERSION = 102;
    private static final int MESSAGE_NOTIFY_HAVA_NO_UPDATE = 103;
    private static final int MESSAGE_NOTIFY_NETWORK_ERROR = 104;
    private static final int MESSAGE_NOTIFY_NO_NETWORK = 105;

    private static final int MESSAGE_NOTIFY_NO_STORAGE = 106;
    
    
    private static final int MESSAGE_NOTIFY_CONNECTION_TIME_OUT = 107;
    
    private static final int MESSAGE_NOTIFY_SERVER_NOT_FOUND = 108;
    
    private static final int MESSAGE_NOTIFY_BAD_REQUEST = 109;
    
    private static final int MESSAGE_NOTIFY_REQUEST_DOWNLOAD = 110;
    
    
    
    
    private static final int ROTATE_ANIMATION_DURATION = 2500;
    private static final int SEND_MASSEGE_DURATION = 2500;
   
    private static final int REQUEST_CODE_FOR_DETAIL = 7;
    
    
    private FrameImageView mInitIndexView;
    
    private ImageView mRoateAImageView = null;
    private Animation mRoateAnimation = null;
    private Animation mNailRotateAnimation = null;
    private AnimationDrawable mFrameAnimationDrawable = null;
    private SoundPool mSoundPool = null;
    private OtaUpgradeManager mUpgradeManager = null;
    private OtaUpgradeInfo mOtaUpgradeInfo = null;
    private OtaUpgradeState mOtaState = null;
    private boolean mEnableMenu = true;
    private String mAction = null;
    private LinearLayout mCheckButton = null;
    private GNLayout mGnLayout = null;
    private TextView mBottomTextView = null;
    private TextView mBottomStatusTextView = null;
    private GNProgressButton mGnProgressButton = null;
    private TextView mDownLoadPeople = null;
    private TextView mDownNewVersion = null;
    private int mOtaTotalFileSize = 0;
    private boolean mLocalUpdate = false;
    private int mPlaySondId = -1;
    private String mCurentVerImproveInfo = null;
    private boolean mResumeNotFromBackground = false;
    private File mLocalUpgradeFile = null;
    private int mProgress = 0;
    private Menu mMenu = null;
    private double mLastSampleTime = 0;
    private int mLastSampleFileSize = 0;
    private int mCountLowerSpeed = 0;
    private int mCountOverSpeed = 0;
    private boolean mIsWaiting = false;
    private boolean mClickPause = false;
    
    private boolean mPause = false; //是否真的暂停下载
    private int mPauseState = 0;  // 0 -- 初始状态  1-- 点击暂停后  2 --  已经暂停了  3 --点击继续后
    
    private boolean mShowingCheckLocalUpdateFileDialog = false;
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private boolean mHaveNoSpaceForDownload = false;
    private boolean mNewIntentIsPush = false;
    // Aurora <likai> add begin
    private View mLogoView;
    private TweenFrameLayout mFrameAnimView;
    private TextView mProgressView;
    private TextView mStatusView;

    private ImageView mShadowView;
    private Animation mShadowAnimation;

    private View mVersionView;
    private TextView mResultView;
    private TextView mVersionInfoView;

    private View mButtonView;
    private Button mStartButton;
    private Button mPauseDownloadButton;
    private Button mCancelDownloadButton;
    
    private ImageView mClickableArrow;

    private boolean mRecoveryUpdated = false;
    // Aurora <likai> add end

    // Aurora <Luofu> <2013-12-11> modify for Animation begin
    private Animation mVersionAnim;
    private Animation mBottomViewAnim;
    private Animation mPauseViewAnim;
    private Animation mButtonAnim;
    private Animation mNailAlphaAnim;
    private AnimationSet mNailAnimationSet;
    private AnimationSet mRotateAnimationSet;
    private AnimationSet mIndexInitAnimationSet;
    
    private Animation mIndexInitRoateAnimation;
    private Animation mIndexInitAlphaAnimation;
    
    private int mBaseSystemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    private AnimUtils mAnimUtil;

    private View mCheckResultView;
    private View mLogoLayout;
    private View mInfoLayout;

    private boolean hasNewVersion = false;

    private boolean mFirstTimeCheck = true;
    private boolean mWapIsActive = false;
    private boolean mPauseButtonClicked = false;
    private boolean isPauseOnResume = false;
    private boolean isStartButtonClicked = false;
    private boolean isResultOk = false;

    private boolean isStart = false;
    private boolean isFromUpdateButFileDeleted = false;
    private int maxCheckTime = 2;
    private int mCheckTime = 0;

    private float mLogoPaddingTopHasNewVDP = 37f;
    private float mLogoPaddingTopNoVDP = 65.3f;

    private float mLogoPaddinTopHasNewVPX;
    private float mLogoPaddingTopNoVPX;

    private boolean isRestartNow = false; //add by gary.gou
    
    private boolean mNoData = true;
    
    private UiPanel mPanel;
    
    private ImageView mNailImageView;
    
    private boolean mOneButton = true;
    
    private boolean mGotNewVersion = false;
    
    private boolean mShowAnimationWhenNoStorage = true;
    
    private boolean mFromBackground = false;
    
    private boolean isNotShowDialogForMoblieNet = true; //add by gary.gou for bug 5589
    private int mFrameIndex = 0; //add by gary.gou
    private boolean isInitIndexViewStartAnim = false;  //add by gary.gou
    
    private boolean isMobileChangeToMobile = false; //解决移动网络断开后又连上移动网络时的动画错误
    
    private String mVersionInfo;
    private String mVersionInfoWithSize;
    private enum UiPanel{
        PANEL_HAS_NEW_VERSION,PANEL_DOWNLOADDING,PANEL_UPDATE;
    }
    
    
    private LayoutInflater mInflater;
    private View mContentView;
    class CallBack {
        public IOtaCheckVersionCallback mOtaCheckVersionCallback = new IOtaCheckVersionCallback() {

            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case Error.ERROR_CODE_BAD_REQUEST:
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_BAD_REQUEST, SEND_MASSEGE_DURATION);
                      break;
                    case Error.ERROR_CODE_CONNECTION_TIME_OUT:
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_CONNECTION_TIME_OUT, SEND_MASSEGE_DURATION);                
                      break;
                    case Error.ERROR_CODE_SERVER_ERROR:
                    case Error.ERROR_CODE_SERVER_NOT_FOUND:
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_SERVER_NOT_FOUND, SEND_MASSEGE_DURATION);
                     break;
                    case Error.ERROR_CODE_INTERNET_NOT_USED:
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_NO_NETWORK, SEND_MASSEGE_DURATION);
                     break;
                    case Error.ERROR_CODE_NETWORK_ERROR:
                        /*
                         * showToast(R.string.network_error);
                         * dismissCheckingAnimation();
                         */

                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_NETWORK_ERROR, SEND_MASSEGE_DURATION);
                        break;
                    case Error.ERROR_CODE_STORAGE_NOT_MOUNTED:
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_NO_STORAGE, SEND_MASSEGE_DURATION);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onCheckResult(boolean result, OtaUpgradeInfo otaUpgradeInfo) {
                LogUtils.logd(TAG, "---result = " + result);
                
                if (result) {
                    mOtaUpgradeInfo = otaUpgradeInfo;
                    if(mOtaUpgradeInfo != null){
                        mRecoveryUpdated = mOtaUpgradeInfo.getRecoveryUpdate();
                    }
                    /*
                     * playSound(1, 0); loadReadyToDownView(true);
                     */
                    if (mFirstTimeCheck) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_HAVA_NEW_VERSION, SEND_MASSEGE_DURATION);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_HAVA_NO_UPDATE, SEND_MASSEGE_DURATION);
                    }
                    
                } else {
                    CheckVersionJob.mNeedTwiceCheck = true;
                    mFirstTimeCheck = false;
                    checkTwiceVersion();
                    mGotNewVersion = false;
                    CheckVersionJob.mNeedTwiceCheck = false;
                }

            }
        };

        public IOtaDownloadCallback mOtaDownloadCallback = new IOtaDownloadCallback() {

            @Override
            public void onError(int errorCode) {
                LogUtils.logd(TAG, " mOtaDownloadCallback  errorCode = " + errorCode);
                mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                handleDownloadError(errorCode);
            }

            @Override
            public void onDownloadComplete() {
                mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                // Aurora <likai> modify begin
                // mGnProgressButton.setEnabled(false);
                // mBottomTextView.setText(R.string.verfy_file);
                mPauseDownloadButton.setVisibility(View.VISIBLE);
                mCancelDownloadButton.setVisibility(View.VISIBLE);
                mPauseDownloadButton.setEnabled(false);
                mCancelDownloadButton.setEnabled(false);
                mProgressView.setText(null);
                mStatusView.setText(getString(R.string.verfy_download_file));
                // Aurora <likai> modify end
            }

            public void onVerifySucessful() {
                mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                loadDownCompleteView();
                // Aurora <likai> add begin
                writeStatistics("E1");
                if (BatteryUtil.getBatteryLevel() < Config.CHARGE) {
                    showDialog(DIALOG_ID_CHARGE);
                } else {
                    showDialog(DIALOG_ID_RESART_UPGRADE);
                }
                // Aurora <likai> add end
            }

            @Override
            public void onProgress(int currentProgress) {
                mProgress = Util.getDownloadProgress(currentProgress, mOtaTotalFileSize);
                LogUtils.logd(TAG, "mProgress = " + mProgress);
                // Aurora <likai> modify begin
                // mGnProgressButton.setProgress(mProgress);
                showProgress("" + mProgress + "%");
                // Aurora <likai> modify end
                mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                mHandler.sendEmptyMessageDelayed(MSG.NOTIFY_LOW_DOWNLOAD_SPEED,
                        NetworkConfig.GIONEE_CONNECT_TIMEOUT);
           // LogUtils.logd(TAG, "mIsWaiting =      " + mIsWaiting+"            mLastSampleFileSize =         "+ mLastSampleFileSize);
                if (mIsWaiting && (currentProgress > mLastSampleFileSize)) {//当有数据下载下来的时候置位，但是不用重复启动动画
                	setWaiting(false);
                	// mIsWaiting = false;
                }
                // Aurora <likai> modify begin
                if (isShowNetNotPower(currentProgress)) {
                    // mBottomStatusTextView.setText(getString(R.string.net_speed_slow));
                    mStatusView.setText(getString(R.string.net_speed_slow));
                } else {
                    /*
                     * if (mProgress < 50 || (mProgress == 100)) {
                     * mBottomStatusTextView.setText(null); } else {
                     * mBottomStatusTextView
                     * .setText(getString(R.string.download_over_half)); }
                     */
                    mStatusView.setText(null);
                }
                // Aurora <likai> modify end
            }
        };

        private void handleDownloadError(int errorCode) {
           LogUtils.log(TAG, "handleDownloadError  errorCode =   "  + errorCode);
            switch (errorCode) {
                case Error.ERROR_CODE_MOBILE_NETWORK:
                case Error.ERROR_CODE_NETWORK_DISCONNECT:
                case Error.ERROR_CODE_NETWORK_ERROR:
                case Error.ERROR_CODE_STORAGE_NOT_MOUNTED:
                	setWaiting(true);
                    break;

                case Error.ERROR_CODE_STORAGE_NO_SPACE:
                    setPause(true);
                    showDialog(DIALOG_ID_NO_SPACE);
                    break;

                case Error.ERROR_CODE_REMOTE_FILE_NOT_FOUND:
                    setPause(true);
                    loadUpdateInfoCheckView();
                    showToast(R.string.remote_file_not_found);
                    break;

                case Error.ERROR_CODE_INTERNAL_STORAGE_NO_SPACE:
                    setPause(true);
                    showDialog(DIALOG_ID_EEMC_NO_SPACE);
                    break;

                case Error.ERROR_CODE_FILE_VERIFY_FAILED:
                    setPause(true);
                    showDialog(DIALOG_ID_FILE_MD5_CHECK_ERR);
                    break;

                case Error.ERROR_CODE_DOWNLOADFILE_DELETED:
                    setPause(true);
                    break;

                default:
                    break;
            }
        }

        private void initSample(int currentProgress) {
            if (mLastSampleFileSize == 0) {
                mLastSampleFileSize = currentProgress;
                mLastSampleTime = (double) System.currentTimeMillis();
                return;
            }
        }

        private void staticsSpeedCount(int currentProgress) {
            if (currentProgress > mLastSampleFileSize) {
                double currentTime = (double) System.currentTimeMillis();
                double netSpeed = ((currentProgress - mLastSampleFileSize) / 1024)
                        / ((currentTime - mLastSampleTime) / 1000);
                if (netSpeed < 10) {
                    if (mCountLowerSpeed != 10) {
                        mCountLowerSpeed++;
                    }
                    mCountOverSpeed = 0;
                } else {
                    if (mCountOverSpeed != 4) {
                        mCountOverSpeed++;
                    }
                    mCountLowerSpeed = 0;
                }
                mLastSampleFileSize = currentProgress;
                mLastSampleTime = currentTime;
            } else {
                mCountOverSpeed = 0;
                mCountLowerSpeed = 0;
                mLastSampleFileSize = currentProgress;
            }
        }

        private boolean isShowNetNotPower(int currentProgress) {
            initSample(currentProgress);
            staticsSpeedCount(currentProgress);
            if (mCountLowerSpeed == 4) {
                mCountLowerSpeed = 0;
                return true;
            }
            if (mCountOverSpeed == 4) {
                mCountOverSpeed = 0;
            }
            return false;
        }

        public IOtaUpgradeCallback mOtaUpgradeCallback = new IOtaUpgradeCallback() {
            @Override
            public void onError(int errorCode) {
                LogUtils.logd(TAG, "errorCode = " + errorCode);
                switch (errorCode) {
                    case Error.ERROR_CODE_FILE_NOT_FOUND:
                        loadReadyToDownView(false);
                        showToast(R.string.file_delete);
                        break;
                    case Error.ERROR_CODE_FILE_VERIFY_FAILED:
                        showDialog(DIALOG_ID_CHECK_UPDATE_FILE_WRONG);
                        break;
                    case Error.ERROR_CODE_STORAGE_NOT_MOUNTED:
                        showToast(R.string.storage_unuseful);
                        break;
                    default:
                        break;
                }
            }
        };

        private IOtaPauseDownloadCallback mOtaPauseDownloadCallback = new IOtaPauseDownloadCallback() {
            @Override
            public void onPauseComplete() {
                LogUtils.logd(TAG, "onPauseComplete");
                // Aurora <likai> modify begin
                // mGnProgressButton.setEnabled(true);
                mPauseDownloadButton.setEnabled(true);
                showButton(false, false, false);
              //  mPauseDownloadButton.setText(R.string.continue_download);
                // Aurora <likai> modify end
                mClickPause = false;
                mPause = true; //已经暂停下载了
            }
        };

        private IOtaCheckLocalUpgradeFileCallback mOtaCheckLocalUpgradeFileCallback = new IOtaCheckLocalUpgradeFileCallback() {

            @Override
            public void onError(int errorCode) {
                dismissDialog(DIALOG_ID_CHECK_LOCAL_UPDATE_FILE);
                mShowingCheckLocalUpdateFileDialog = false;
                switch (errorCode) {
                    case Error.ERROR_CODE_WRONG_UPDATE_FILE:
                        showDialog(DIALOG_ID_CHECK_LOCAL_UPDATE_FILE_WRONG);
                        break;
                    case Error.ERROR_CODE_FILE_NOT_FOUND:
                        showToast(R.string.file_delete);
                        break;
                    case Error.ERROR_CODE_STORAGE_NOT_MOUNTED:
                        showToast(R.string.storage_unuseful);
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onCheckComplete() {
                dismissDialog(DIALOG_ID_CHECK_LOCAL_UPDATE_FILE);
                mShowingCheckLocalUpdateFileDialog = false;
                mLocalUpdate = true;
                showDialog(DIALOG_ID_RESART_UPGRADE);
            }

        };
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG.NOTIFY_LOW_DOWNLOAD_SPEED:
                    // Aurora <likai> modify begin
                    // mBottomStatusTextView.setText(getString(R.string.net_speed_slow));
                    mStatusView.setText(getString(R.string.net_speed_slow));
                    // Aurora <likai> modify end
                    break;
                case MESSAGE_NOTIFY_FRAME_ANIMOTION_END:
                    // mFrameAnimView.setBackgroundResource(R.drawable.aurora_rotate_bg);
                    // doActionAfterAnimotion();
//                    showCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_HAVA_NEW_VERSION:
                    loadView();
                    break;
                case MESSAGE_NOTIFY_HAVA_NO_UPDATE:
                    showCurrentVersion();
                    dismissCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_NETWORK_ERROR:
//                    showToast(R.string.have_network_error);
                    showNetworkError(R.string.have_network_error,R.string.have_network_error_info);
                    dismissCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_NO_NETWORK:
                    sendNoNetworkBroadcast();
                    dismissCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_NO_STORAGE:
                    showCurrentVersion(R.string.has_no_storage);
                    dismissCheckingAnimation();
//                    mNoData = false;
                    break;
                case MESSAGE_NOTIFY_BAD_REQUEST:
                    showNetworkError(R.string.request_error,R.string.have_network_error_info);
                    dismissCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_CONNECTION_TIME_OUT:
                    showNetworkError(R.string.connection_time_out,R.string.have_network_error_info);
                    dismissCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_SERVER_NOT_FOUND:
                    showNetworkError(R.string.server_error,R.string.have_network_error_info);
                    dismissCheckingAnimation();
                    break;
                case MESSAGE_NOTIFY_REQUEST_DOWNLOAD:
                {
                	try{
                		LogUtils.logd(TAG,  "  9  mPauseState =   "+ mPauseState);
                        mUpgradeManager.downloadOtaFile(new CallBack().mOtaDownloadCallback);//下载文件
                	} catch (ErrorStateException e) {
                                      LogUtils.logd(
                                              TAG,
                                              "loadDownProgressView----mOtaState = "
                                                      + mUpgradeManager.getOtaUpgradeState());
                                  } catch (NoSpaceException e) {
                                      showToast(R.string.sdcard_no_space);
                                  }
                }
                	break;
                default:
                    break;
            }
            mNoData = false;
        }
    };
    
    private void showNetworkError(int titleRes,int msgRes){
        if(mPanel == UiPanel.PANEL_DOWNLOADDING){
            showToast(R.string.have_network_error);
        }else{
            String msgTitle = getString(titleRes);
            String msgInfo = getString(msgRes);
            showNotification(msgTitle, msgInfo,false);
        }
        
        
    }

    public void showToast(int messageId) {
        String message = getString(messageId);
        Toast.makeText(UpdateUiActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void checkTwiceVersion() {
        if (mCheckTime < maxCheckTime) {
            try {
                if(mUpgradeManager == null){
                    mUpgradeManager = OtaUpgradeManager.getInstance(this);
                }
                mUpgradeManager.checkOtaVersion(new CallBack().mOtaCheckVersionCallback);
            } catch (ErrorStateException e) {
            }
            mCheckTime = maxCheckTime;

        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_HAVA_NO_UPDATE, SEND_MASSEGE_DURATION);
        }

    }

    private void showCheckingAnimation() {
    	LogUtils.log(TAG, "showCheckingAnimation !!!!!!!");
        // Aurora <likai> modify begin
        /*
         * mEnableMenu = false; setSettingMenuEnabled(mEnableMenu);
         * mCheckButton.setBackgroundResource(R.drawable.anim8);
         * mBottomTextView.setText(getString(R.string.check_wait)); if
         * (!mRoateAnimation.hasStarted() || mRoateAnimation.hasEnded()) {
         * mRoateAImageView.startAnimation(mRoateAnimation);
         * mFrameAnimationDrawable.stop(); }
         * mRoateAImageView.setVisibility(View.VISIBLE);
         * mCheckButton.setClickable(false);
         */
        
//        mNailImageView.setBackgroundResource(R.drawable.aurora_rotate_anim);
//        mNailImageView.startAnimation(mNailAlphaAnim);
//        mNailImageView.startAnimation(mRoateAnimation);
//        AnimationSet set = new AnimationSet(true);
//        set.addAnimation(mNailAlphaAnim);
//        set.addAnimation(mRoateAnimation);
//        set.setRepeatCount(RotateAnimation.INFINITE);
//        set.setDuration(ROTATE_ANIMATION_DURATION);
//        set.setInterpolator(new LinearInterpolator());
//        mNailImageView.startAnimation(set);
        
//        mRoateAImageView.setBackgroundResource(R.drawable.aurora_rotate_anim);
//        mRoateAnimation.setAnimationListener(new AnimationListener() {
//            
//            @Override
//            public void onAnimationStart(Animation animation) {
//                // TODO Auto-generated method stub
//                
//            }
//            
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//                // TODO Auto-generated method stub
//                mRoateAImageView.postInvalidate();
//            }
//            
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                // TODO Auto-generated method stub
//                
//            }
//        });aurora_rotate_anim.png
        initialCheckAnimation();
        mRoateAImageView.clearAnimation();
//        mRoateAImageView.setBackgroundResource(R.drawable.aurora_rotate_anim);
        if (!mRotateAnimationSet.hasStarted() || mRotateAnimationSet.hasEnded()) {
            mRoateAImageView.startAnimation(mRotateAnimationSet);
        }
        if (!mShadowAnimation.hasStarted() || mShadowAnimation.hasEnded()) {
            mShadowView.startAnimation(mShadowAnimation);
        }
        
//        if (!mNailRotateAnimation.hasStarted() || mNailRotateAnimation.hasEnded()) {
//            mNailImageView.startAnimation(mNailRotateAnimation);
//        }
        if (!mNailAnimationSet.hasStarted() || mNailAnimationSet.hasEnded()) {
            mNailImageView.startAnimation(mNailAnimationSet);
        }
        
//        if (!mNailAlphaAnim.hasStarted() || mNailAlphaAnim.hasEnded()) {
//            mNailImageView.startAnimation(mNailAlphaAnim);
//        }
        
        
        mStartButton.setEnabled(false);
//        mStartButton.setVisibility(View.INVISIBLE);
        // Aurora <likai> modify end
    }

    private void dismissCheckingAnimation() {
    	LogUtils.log(TAG, "dismissCheckingAnimation !!!");

        mStartButton.setEnabled(true);
       
        if (mRotateAnimationSet != null) {
            if (!mRotateAnimationSet.hasEnded()) {
                mRotateAnimationSet.cancel();
                mRotateAnimationSet.reset();
            }
        }
        if (mRoateAImageView != null) {
            mRoateAImageView.clearAnimation();
            
             mRoateAImageView.setVisibility(View.VISIBLE);
            mRoateAImageView.setBackgroundResource(R.drawable.aurora_index);
            
        }

        if (mShadowAnimation != null) {
            if (!mShadowAnimation.hasEnded()) {
                mShadowAnimation.cancel();
                mShadowAnimation.reset();
            }
        }
        if (mShadowView != null) {
            mShadowView.clearAnimation();
        }
        
        if(mNailAnimationSet != null){
            if (!mNailAnimationSet.hasEnded()) {
                mNailAnimationSet.cancel();
                mNailAnimationSet.reset();
            }
        }
        if(mNailImageView != null){
            mNailImageView.clearAnimation();
            mNailImageView.setVisibility(View.INVISIBLE);
        }
    }

    private int getCheckDuration() {
        AutoCheckCycle autoCheckCycle = OtaSettings.getAutoCheckCycle(UpdateUiActivity.this);
        switch (autoCheckCycle) {
            case SEVEN_DAYS:
                return 7;
            case FOURTEEN_DAYS:
                return 14;
            case THIRTY_DAYS:
                return 30;
            case NINETY_DAYS:
                return 90;
            default:
                return 30;
        }

    }

    private void writeInitialSettingToStatistics() {
        writeNetInfoSatics();
        int duration = getCheckDuration();
        boolean autoCheckEnable = OtaSettings.getAutoCheckEnabled(UpdateUiActivity.this, false);
        boolean autoDownLoadEnable = OtaSettings
                .getAutoDownloadEnabled(UpdateUiActivity.this, true);
        if (autoDownLoadEnable) {
            if (autoCheckEnable) {
                writeStatistics("S1w1a" + duration);
            } else {
                writeStatistics("S1w1a0");
            }
        } else {
            if (autoCheckEnable) {
                writeStatistics("S1w0a" + duration);
            } else {
                writeStatistics("S1w0a0");
            }
        }
        if (mAction != null) {
            if (mAction.equals("android.intent.action.MAIN")) {
                if (OtaSettings.isHaveNewVersionApplicationIcon(UpdateUiActivity.this, false)) {
                    writeStatistics("A2");
                } else {
                    writeStatistics("A1");
                }
            } else if (mAction.equals("android.settings.GN_OTA_SYSTEM_UPDATE_SETTINGS")) {
                writeStatistics("A4");
            } else if (mAction.equals("gn.com.android.update.action.start")) {
                writeStatistics("A3");
            }
        }
    };

    private void writeNetInfoSatics() {
        ConnectionType connectType = HttpUtils.getConnectionType(UpdateUiActivity.this);
        if (connectType.equals(ConnectionType.CONNECTION_TYPE_WIFI)) {
            writeStatistics("N1");
        } else if (connectType.equals(ConnectionType.CONNECTION_TYPE_2G)) {
            writeStatistics("N2");
        } else if (connectType.equals(ConnectionType.CONNECTION_TYPE_3G)) {
            writeStatistics("N3");
        } else {
            writeStatistics("N4");
        }
    }

    public void writeStatistics(String info) {
        YouJuAgent.onEvent(UpdateUiActivity.this, info);
        LogUtils.logd(TAG, "writeStatistics=" + info);
    }
    
    /**
     * show notification to user when get some error ,such as net work error,
     * @param msgTitle
     * @param msgInfo
     * @param clickable
     */
  private void showNotification(String msgTitle,String msgInfo,boolean clickable){
        
        mInfoLayout.setVisibility(View.VISIBLE);
        if (isStart) {
            mVersionView.startAnimation(mBottomViewAnim);
            showLogoAnimation(false);
            isStart = !isStart;
        } else {

            // mVersionView.set
        }
        mVersionView.setVisibility(View.VISIBLE);

        mBottomViewAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(300);
                    mCheckResultView.startAnimation(mVersionAnim);
                    mCheckResultView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        });

        mClickableArrow.setVisibility((clickable?View.VISIBLE:View.GONE));
        // Aurora <Luofu> <2013-12-11> modify for Animation end
        if(clickable){
            mCheckResultView.setOnClickListener(new OnClickListener() {
                boolean isCurrentVersion = true;
                boolean isLocalVerson = false;

                @Override
                public void onClick(View v) {
                	
                    if (!HttpUtils.isNetworkAvailable(UpdateUiActivity.this)) {
                        sendNoNetworkBroadcast();
                        return;
                    }
                    String otaReleaseNote = OtaSettings
                            .getCurrVerReleasenote(UpdateUiActivity.this, "");
                    String otagetReleaseNoteUrl = OtaSettings.getCurrVerReleasenoteUrl(
                            UpdateUiActivity.this, "");

                    if (!mFirstTimeCheck) {
                        if (mOtaUpgradeInfo != null) {
                            otaReleaseNote = mOtaUpgradeInfo.getReleaseNote();
                            otagetReleaseNoteUrl = mOtaUpgradeInfo.getReleaseNoteUrl();
                            isCurrentVersion = false;
                            isLocalVerson = true;
                        }

                    }
                    boolean haveNotOtaReleaseNote = otaReleaseNote.equals("null")
                            || otaReleaseNote.equals("");
                    boolean haveNotOtaReleaseNoteUrl = "null".equals(otagetReleaseNoteUrl)
                            || "".equals(otagetReleaseNoteUrl);
                    if (haveNotOtaReleaseNote && haveNotOtaReleaseNoteUrl) {
//                        showToast(R.string.kown_more_no_info);
//                        return;
                        intentDetailsInfoActivity(true);
                    } else {
                        intentDetailsInfoActivity(otagetReleaseNoteUrl, isCurrentVersion, isLocalVerson);
                    }
               }
            });
        }
       
        mResultView.setText(msgTitle);
        mVersionInfoView.setText(msgInfo);
        
    }
  
     /**
      * show version info
      * @param titleResId  info oanel title res
      */
    private void showCurrentVersion(int titleResId){
        DetailsInfoActivity.HAS_NEW_VERSION = false;
        String msgTitle = getString(titleResId);
        String msgInfo = getString(R.string.click_to_check_current_version);
        showNotification(msgTitle, msgInfo,true);
    }
    // Aurora <likai> add begin
    /**
     * show version info
     */
    private void showCurrentVersion() {
//        String romVersionString = Util.getInternalVersion();

//        long timeMillis = Long.parseLong(Util.getBuildTime());
//        if (romVersionString.length() > 12) {
//            String timeString = romVersionString.substring(romVersionString.length() - 12);
//            String year = timeString.substring(0, 4);
//            String month = timeString.substring(4, 6);
//            String monthDay = timeString.substring(6, 8);
//            Time time = new Time();
//            time.set(Integer.parseInt(monthDay), Integer.parseInt(month) - 1,
//                    Integer.parseInt(year));
//            timeMillis = time.toMillis(false);
//        }

//        String updateTime = DateUtils.formatDateTime(UpdateUiActivity.this, timeMillis,
//                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
        // Aurora <Luofu> <2013-12-11> modify for Animation begin
        DetailsInfoActivity.HAS_NEW_VERSION = false;
        String msgTitle = getString(R.string.have_no_update);
        String msgInfo = getString(R.string.click_to_check_current_version);
        showNotification(msgTitle, msgInfo,true);
//        mInfoLayout.setVisibility(View.VISIBLE);
//        if (isStart) {
//            mVersionView.startAnimation(mBottomViewAnim);
//            showLogoAnimation(false);
//            isStart = !isStart;
//        } else {
//
//            // mVersionView.set
//        }
//        mVersionView.setVisibility(View.VISIBLE);
//
//        mBottomViewAnim.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                // TODO Auto-generated method stub
//                try {
//                    Thread.sleep(300);
//                    mCheckResultView.startAnimation(mVersionAnim);
//                    mCheckResultView.setVisibility(View.VISIBLE);
//                } catch (Exception e) {
//                    // TODO: handle exception
//                }
//
//            }
//        });
//
//        // Aurora <Luofu> <2013-12-11> modify for Animation end
//        mCheckResultView.setOnClickListener(new OnClickListener() {
//            boolean isCurrentVersion = true;
//            boolean isLocalVerson = false;
//
//            @Override
//            public void onClick(View v) {
//                if (!HttpUtils.isNetworkAvailable(UpdateUiActivity.this)) {
//                    sendNoNetworkBroadcast();
//                    return;
//                }
//                String otaReleaseNote = OtaSettings
//                        .getCurrVerReleasenote(UpdateUiActivity.this, "");
//                String otagetReleaseNoteUrl = OtaSettings.getCurrVerReleasenoteUrl(
//                        UpdateUiActivity.this, "");
//
//                if (!mFirstTimeCheck) {
//                    if (mOtaUpgradeInfo != null) {
//                        otaReleaseNote = mOtaUpgradeInfo.getReleaseNote();
//                        otagetReleaseNoteUrl = mOtaUpgradeInfo.getReleaseNoteUrl();
//                        isCurrentVersion = false;
//                        isLocalVerson = true;
//                    }
//
//                }
//                boolean haveNotOtaReleaseNote = otaReleaseNote.equals("null")
//                        || otaReleaseNote.equals("");
//                boolean haveNotOtaReleaseNoteUrl = "null".equals(otagetReleaseNoteUrl)
//                        || "".equals(otagetReleaseNoteUrl);
//                if (haveNotOtaReleaseNote && haveNotOtaReleaseNoteUrl) {
//                    showToast(R.string.kown_more_no_info);
//                    return;
//                } else {
//                    intentDetailsInfoActivity(otagetReleaseNoteUrl, isCurrentVersion, isLocalVerson);
//                }
//            }
//        });
//        mResultView.setText(R.string.have_no_update);
//        mVersionInfoView.setText(getString(R.string.click_to_check_current_version,updateTime));
    }

    /**
     * used for show which button
     * @param canStartCheck   if has newversion it will be true
     * @param canStartDownload then we can download new package it will be true
     * @param canStartUpdate   when we downloaded package it will be true
     */
    private void showButton(boolean canStartCheck, boolean canStartDownload, boolean canStartUpdate) {
       
        if (canStartCheck || canStartDownload || canStartUpdate) {
            if (canStartCheck) {
                mStartButton.setText(R.string.start_check);
            } else if (canStartDownload) {
                mStartButton.setText(R.string.start_update);
            } else {
                mStartButton.setText(R.string.start_update);
            }
            mStartButton.setVisibility(View.VISIBLE);
            mPauseDownloadButton.setVisibility(View.GONE);
            mCancelDownloadButton.setVisibility(View.GONE);
        } else {
            mStartButton.setVisibility(View.INVISIBLE);
            mPauseDownloadButton.setVisibility(View.VISIBLE);
            mCancelDownloadButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * show download progress when download ota package
     * @param progress
     */
    private void showProgress(String progress) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(progress);
        ssb.setSpan(new AbsoluteSizeSpan(72),
                progress.indexOf("%"), progress.indexOf("%") + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mProgressView.setText(ssb);
    }

    /**
     * when user start ota application ,this method will invoked,it just play 
     * animation for start app
     */
    private void showAnimation() {
            mFrameAnimView.startAnim();
    }

    /**
     * this method is used for checking new version
     */
    private void doActionAfterAnimotion() {
    	
    	

        if(mUpgradeManager == null){
            mUpgradeManager = OtaUpgradeManager.getInstance(this);
        }
        mUpgradeManager.setOtaInitial();
        mOtaState = mUpgradeManager.getOtaUpgradeState();
        
        LogUtils.log(TAG, "doActionAfterAnimotion   mOtaState  = "  + mOtaState);
        switch (mOtaState) {
            case INITIAL:
                if (needCheckVersionWhenOnResume()) {
                    try {
                    
                        mUpgradeManager.checkOtaVersion(new CallBack().mOtaCheckVersionCallback);
                    } catch (ErrorStateException e) {
                        LogUtils.logd(TAG, "onresume() checkOtaVersion----mOtaState = " + mOtaState);
                    }
                }
                loadUpdateInfoCheckView();
                break;
            case CHECKING:
                loadUpdateInfoCheckView();
                mUpgradeManager.resumeCallback(new CallBack().mOtaCheckVersionCallback);
                break;
            case READY_TO_DOWNLOAD:
                long lastCheckTime = OtaSettings.getLastCheckTime(UpdateUiActivity.this, 0);
                if ((System.currentTimeMillis() - lastCheckTime) >= ONE_DAY) {
                    loadUpdateInfoCheckView();
                    mUpgradeManager.resumeInitialStateAfterOneDay();
                    return;
                }
                loadReadyToDownView(false);
                break;
            case DOWNLOADING:
                if (mClickPause) {
                    LogUtils.logd(TAG, "mClickPause = " + mClickPause);
                    mUpgradeManager.resumeCallback(new CallBack().mOtaPauseDownloadCallback);
                    return;
                }
                loadDownProgressView(false, false);
                mUpgradeManager.resumeCallback(new CallBack().mOtaDownloadCallback);
                mHandler.sendEmptyMessageDelayed(MSG.NOTIFY_LOW_DOWNLOAD_SPEED,
                        NetworkConfig.GIONEE_CONNECT_TIMEOUT);
                break;
            case DOWNLOAD_INTERRUPT:
                loadDownProgressView(false, true);
                mUpgradeManager.resumeCallback(new CallBack().mOtaDownloadCallback);
                break;
            case DOWNLOAD_PAUSE:
                loadDownProgressView(true, false);
                break;
            case DOWNLOAD_COMPLETE:
                loadDownCompleteView();
                break;
            default:
                break;
        }

        processLastError(mUpgradeManager.popLastErrorCode());
    }
    
    /**
     * load download view
     */
    private void loadView() {
        String fileNameWithoutStoragePath = FileUtil
                .getOtaFileNameWithoutStoragePath(mOtaUpgradeInfo.getMd5());
        File file = FileUtil.getAlreadyDownloadedFile(this, fileNameWithoutStoragePath);
        if (file == null || FileUtil.getFileLengthByUrl(getApplicationContext(),mOtaUpgradeInfo.getDownloadurl()) == mOtaUpgradeInfo.getFileSize()) {
        	 LogUtils.log(TAG, " loadView  file is null OR  file already exist ");
            loadReadyToDownView(true);
        } else {
        	LogUtils.log(TAG, " loadView  file is exist but not complete");
            if(mUpgradeManager == null){
                mUpgradeManager = OtaUpgradeManager.getInstance(this);
            }
            mUpgradeManager.setOtaDownloadPause();
            loadDownProgressView(true, false);
        }
    }

    /**
     * invoke this method to hide versin info panel when show download panel
     */
    private void hideNewVersion() {
        mOtaTotalFileSize = mOtaUpgradeInfo.getFileSize();

        mVersionView.setVisibility(View.VISIBLE);
        mInfoLayout.setVisibility(View.INVISIBLE);
        mCheckResultView.setVisibility(View.INVISIBLE);
        showLogoAnimation(true);
        mButtonView.setVisibility(View.VISIBLE);
        //LayoutParams params = (LayoutParams) mButtonView.getLayoutParams();
        //params.height = 597;
        //mButtonView.setLayoutParams(params);
    }

    /**
     * invoke this method to cancel download
     */
    private void cancelDownload() {
        mUpgradeManager.cancelOtaDownload();
        isStart = true;
        loadReadyToDownView(true);
        String fileNameWithoutStoragePath = FileUtil
                .getOtaFileNameWithoutStoragePath(mOtaUpgradeInfo.getMd5());
        File file = FileUtil.getAlreadyDownloadedFile(UpdateUiActivity.this,
                fileNameWithoutStoragePath);
        FileUtil.deleteFileIfExists(file);
    }

    // Aurora <likai> add end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStatusBarBG(true);
        SystemUtils.setStatusBarBackgroundTransparent(this);
        Intent networkIntent = new Intent(OtaInent.APP_START_INTENT_PUBLIC_NETWORK_MODULE);
        networkIntent.putExtra(OtaInent.INTENT_EXTRA_NAME, getPackageName());
        sendBroadcast(networkIntent);
        registerMobileNetConfirmReceiver();
        registerConnectivityReceiver();//add by gary.gou for bug 5589
        mUpgradeManager = OtaUpgradeManager.getInstance(this);
        if (mOtaState == null) {
            mOtaState = mUpgradeManager.getOtaUpgradeState();
        }
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = mInflater.inflate(R.layout.aurora_main_layout, null);
//        setNavVisibility(true);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN/*|View.SYSTEM_UI_FLAG_LAYOUT_STABLE*/);
        setContentView(mContentView);
        initLogoMarginTop();
        init();
        // Aurora <Luofu> <2013-12-11> modify for Animation begin
        initAnimation();
//        initialCheckAnimation();
        // Aurora <Luofu> <2013-12-11> modify for Animation end
//        mResumeNotFromBackground = true;
//        mUpgradeManager.onActivityCreate();
//        // startService(new Intent(this,ReporterService2.class));
//        loadScanAnimation();
//        isStart = true;
        start();
        
    }
    
    private void start(){
        mResumeNotFromBackground = true;
        mUpgradeManager.onActivityCreate();
        // startService(new Intent(this,ReporterService2.class));
        loadScanAnimation();
        isStart = true;
    }

    private void loadScanAnimation() {
        // mFrameAnimView.setBackgroundResource(R.anim.aurora_frame_anim);
        // mFrameAnimationDrawable = (AnimationDrawable)
        // mFrameAnimView.getBackground();
        mFrameAnimView.setAnimationListener(mAnimListener);

    }
    void setNavVisibility(boolean visible) {
        int newVis = mBaseSystemUiVisibility;
        if (!visible) {
            newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        final boolean changed = newVis == mContentView.getSystemUiVisibility();

        // Unschedule any pending event to hide navigation if we are
        // changing the visibility, or making the UI visible.

        // Set the new desired visibility.
        Log.e("window", "changed");
        mContentView.setSystemUiVisibility(newVis);
    }
  

    private void initLogoMarginTop() {
        mLogoPaddinTopHasNewVPX = DensityUtil.dip2px(this, mLogoPaddingTopHasNewVDP);
        mLogoPaddingTopNoVPX = DensityUtil.dip2px(this, mLogoPaddingTopNoVDP);
    }

    protected void onNewIntent(Intent intent) {
        LogUtils.logd(TAG, "onNewIntent");
        if (isPushIntent(intent)) {
            mNewIntentIsPush = true;
        }
    }

    private boolean isPushIntent(Intent intent) {
        if (intent != null && OtaInent.START_MAIN_ACTIVITY_ACTION.equals(intent.getAction())
                && "push".equals(intent.getStringExtra("who"))) {
            LogUtils.logd(TAG, "isPushIntent is push intent");
            return true;
        } else {
            return false;
        }
    }

    /**
     * init views from layout
     */
    public void initialLayout() {
        // Aurora <likai> modify begin
        /*
         * mGnLayout = (GNLayout)
         * findViewById(R.id.check_download_info_gnlayout); mBottomTextView =
         * (TextView) findViewById(R.id.bottom_text); mBottomStatusTextView =
         * (TextView) findViewById(R.id.bottom_status_text); mGnProgressButton =
         * (GNProgressButton) findViewById(R.id.download_button); mCheckButton =
         * (LinearLayout) findViewById(R.id.check_button); TextView
         * mCurrentVersion = (TextView)
         * findViewById(R.id.check_current_version_text); mCurrentVersion
         * .setText(getString(R.string.now_version) +
         * SystemProperties.get("ro.build.display.id")); mDownLoadPeople =
         * (TextView) findViewById(R.id.current_download_text); mDownNewVersion
         * = (TextView) findViewById(R.id.download_new_version_text);
         */
        mLogoView = (View) findViewById(R.id.logo_view);
        mFrameAnimView = (TweenFrameLayout) findViewById(R.id.frame_anim_view);
        mProgressView = (TextView) findViewById(R.id.progress_view);
        mStatusView = (TextView) findViewById(R.id.status_view);

        mVersionView = (View) findViewById(R.id.version_view);
        mResultView = (TextView) findViewById(R.id.result_view);
        mVersionInfoView = (TextView) findViewById(R.id.version_info_view);

        mButtonView = (View) findViewById(R.id.button_view);
        mStartButton = (Button) findViewById(R.id.start_button);
        mPauseDownloadButton = (Button) findViewById(R.id.pause_download_button);
        mCancelDownloadButton = (Button) findViewById(R.id.cancel_download_button);
        // Aurora <likai> modify end
        mClickableArrow = (ImageView)findViewById(R.id.aurora_clickable_arrow);

        mCheckResultView = findViewById(R.id.info_view);
        mLogoLayout = findViewById(R.id.logo_layout);
        mInfoLayout = findViewById(R.id.info_view_layout);
        //mFrameAnimView.setFrameAnimationList(R.anim.aurora_frame_anim);
        
        mNailImageView = (ImageView)findViewById(R.id.rotate_nail);
        mNailImageView.setVisibility(View.INVISIBLE);
        
        mRoateAImageView = (ImageView) findViewById(R.id.rotate_anim_view);
        mShadowView = (ImageView) findViewById(R.id.shadow_view);
        mShadowView.setBackgroundResource(R.drawable.aurora_shadow);
        mRoateAImageView.setBackgroundResource(R.drawable.aurora_index);
        mRoateAImageView.setVisibility(View.INVISIBLE);
        mShadowView.setVisibility(View.INVISIBLE);
        
       
        mInitIndexView = (FrameImageView)findViewById(R.id.init_index);
        mInitIndexView.setVisibility(View.INVISIBLE);
        mInitIndexView.setFrameAnimationList(R.anim.aurora_init_index_frame_anim);
        mInitIndexView.setAnimationListener(new AnimationImageListener() {
            
            @Override
            public void onRepeat(int repeatIndex) {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void onFrameChange(int repeatIndex, int frameIndex, int currentTime) {
                // TODO Auto-generated method stub
            	mFrameIndex = frameIndex;
                if(frameIndex == 8){
                    doActionAfterAnimotion();
                    mInitIndexView.stopAnim();
                }
            }
            
            @Override
            public void onAnimationStart() {
                mInitIndexView.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationEnd() {
            }
        });
    }

    private void init() {
        initialLayout();
        // initialSoundpool();
        // Aurora <likai> modify begin
        // mDownNewVersion.setOnClickListener(new OnClickListener() {
        
        
        mCheckResultView.setOnClickListener(new OnClickListener() {
            // Aurora <likai> modify end
            @Override
            public void onClick(View v) {
                writeStatistics("D3");
                writeNetInfoSatics();
                if (!HttpUtils.isNetworkAvailable(UpdateUiActivity.this)) {
                    sendNoNetworkBroadcast();
                    return;
                }
                if(mOtaUpgradeInfo != null){
                    String otaReleaseNote = mOtaUpgradeInfo.getReleaseNote();
                    String otagetReleaseNoteUrl = mOtaUpgradeInfo.getReleaseNoteUrl();
                    boolean haveNotOtaReleaseNote = otaReleaseNote.equals("null")
                            || otaReleaseNote.equals("");
                    boolean haveNotOtaReleaseNoteUrl = "null".equals(otagetReleaseNoteUrl)
                            || "".equals(otagetReleaseNoteUrl);
                    if (haveNotOtaReleaseNote && haveNotOtaReleaseNoteUrl) {
//                        showToast(R.string.kown_more_no_info);
//                        return;
                        
                        intentDetailsInfoActivity(true);
                    } else {
                        intentDetailsInfoActivity(otagetReleaseNoteUrl, false, false);
                    }
                }
            }
        });
        
        }

    // Aurora <Luofu> <2013-12-11> modify for Animation begin
    /**
     * init animation for bottom panel
     */
    private void initAnimation() {
        mNailAlphaAnim = AnimationUtils.loadAnimation(this, R.anim.aurora_nail_alpha_anim);
//        mIndexInitAnimation = AnimationUtils.loadAnimation(this, R.anim.index_init_anim);
        
        mAnimUtil = AnimUtils.getInstance(this);
        mVersionAnim = mAnimUtil.getCheckedNotifyAnim();
        mBottomViewAnim = mAnimUtil.getBottomViewAnim();
        mButtonAnim = mAnimUtil.getOptionAnimation();
        mButtonAnim.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                mStartButton.setVisibility(View.VISIBLE);
                mStartButton.setClickable(false);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                mStartButton.setClickable(true);
                if(mOneButton){
                    mStartButton.setVisibility(View.VISIBLE);
                }else{
                    mStartButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        mPauseViewAnim = mAnimUtil.getBottomViewAnim();

    }

    private void showBottomView() {

    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) {
     * getMenuInflater().inflate(R.menu.menu, menu); mCurentVerImproveInfo =
     * OtaSettings.getCurrVerReleasenote(UpdateUiActivity.this, ""); String
     * curentVerDescUrl =
     * OtaSettings.getCurrVerReleasenoteUrl(UpdateUiActivity.this, ""); if
     * (curentVerDescUrl.equals("") && "".equals(mCurentVerImproveInfo)) {
     * menu.removeItem(R.id.version_improving_information); } mMenu = menu; if
     * (mUpgradeManager.getOtaUpgradeState() == OtaUpgradeState.CHECKING) {
     * setSettingMenuEnabled(false); } return super.onCreateOptionsMenu(menu); }
     * private void setSettingMenuEnabled(boolean enabled) { LogUtils.logd(TAG,
     * "setSettingMenuEnabled() " + enabled); if (mMenu == null) { return; }
     * MenuItem settingMenuItem = mMenu.findItem(R.id.menu_setting);
     * settingMenuItem.setEnabled(enabled); if (enabled) {
     * settingMenuItem.setIcon(R.drawable.menu_item_setting); } else {
     * settingMenuItem.setIcon(R.drawable.menu_item_setting_disabled); }
     * updateOptionsMenu(mMenu); } public boolean onPrepareOptionsMenu(Menu
     * menu) { if (OtaUpgradeState.INITIAL !=
     * mUpgradeManager.getOtaUpgradeState()) {
     * setUpgradeFromLocalMenuItemVisable(mMenu, false); } else {
     * setUpgradeFromLocalMenuItemVisable(mMenu, true); } return
     * super.onPrepareOptionsMenu(mMenu); }
     */

    /**
     * start DetailInfoActivity to show detail info of new version
     * @param url  detail info url
     * @param isCurrentVersionInfo is new version or not
     * @param isLocalVersion  is local version or not
     */
    public void intentDetailsInfoActivity(String url, boolean isCurrentVersionInfo,
            boolean isLocalVersion) {
        Intent intent = new Intent(UpdateUiActivity.this, DetailsInfoActivity.class);
        intent.putExtra(DetailsInfoActivity.MROE_INFO_URL, url);
        intent.putExtra(DetailsInfoActivity.IS_CURRENT_VERSION_INFO, isCurrentVersionInfo);
        // Aurora <likai> modify begin
        // startActivity(intent);
        intent.putExtra(DetailsInfoActivity.IS_LOCAL_VERSION_INFO, isLocalVersion);
        startActivityForResult(intent, REQUEST_CODE_FOR_DETAIL);
        // Aurora <likai> modify end
    }
    
    private void intentDetailsInfoActivity(boolean hasNoVersionInfo){
        Intent intent = new Intent(UpdateUiActivity.this, DetailsInfoActivity.class);
        intent.putExtra(DetailsInfoActivity.HAS_NO_VERSION_INFO, hasNoVersionInfo);
        startActivityForResult(intent, REQUEST_CODE_FOR_DETAIL);
    }

    /*
     * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
     * (item.getItemId()) { case R.id.upgrade_from_local: writeStatistics("B1");
     * if (BatteryUtil.getBatteryLevel() < Config.CHARGE) {
     * showDialog(DIALOG_ID_CHARGE); return true; } Intent intent = new
     * Intent(); intent.setAction("com.mediatek.filemanager.ADD_FILE");
     * intent.putExtra("type", "zip"); startActivityForResult(intent,
     * RESULT_CODE_FOR_FILEMANAGER); break; case
     * R.id.version_improving_information: writeStatistics("B2"); if
     * (!HttpUtils.isNetworkAvailable(UpdateUiActivity.this)) {
     * sendNoNetworkBroadcast(); break; }
     * intentDetailsInfoActivity(OtaSettings.getCurrVerReleasenoteUrl(this, ""),
     * true); break; case R.id.menu_setting: writeStatistics("B3"); Intent
     * intent3 = new Intent(UpdateUiActivity.this,
     * UpdateSettingsActivity.class); startActivity(intent3); break; } return
     * true; }
     */

    private void showCheckButton(boolean isShow) {
        if (isShow) {
            mCheckButton.setVisibility(View.VISIBLE);
            mGnProgressButton.setVisibility(View.INVISIBLE);
        } else {
            mCheckButton.setVisibility(View.INVISIBLE);
            mGnProgressButton.setVisibility(View.VISIBLE);
        }
    }

    protected void onStart() {
        super.onStart();
        LogUtils.logv(TAG, "onStart");
    }

    protected void onStop() {
        super.onStop();
        LogUtils.logv(TAG, "onStop () ");
    }

    protected void onPause() {
        super.onPause();
        // mLogoLayout.
        // FrameLayout.LayoutParams p =
        // (android.widget.FrameLayout.LayoutParams)
        // mLogoLayout.getLayoutParams();
        // p.topMargin = 40;
        // mLogoLayout.setLayoutParams(p);
        if(mFrameIndex < 8 && isInitIndexViewStartAnim){
        	Log.v("gary","mFrameIndex---");
        	mInitIndexView.stopAnim();
        	mInitIndexView.setVisibility(View.GONE);
        	mFrameIndex = 0;
        	isInitIndexViewStartAnim = false; 
        }
        mFrameAnimView.stopAnim();
        
        mFromBackground = true;
        mUpgradeManager.pauseCallback();
        if (mShowingCheckLocalUpdateFileDialog) {
            dismissDialog(DIALOG_ID_CHECK_LOCAL_UPDATE_FILE);
            mShowingCheckLocalUpdateFileDialog = false;
        }
        if(mNoData){
            mResumeNotFromBackground = true;
        }else{
            mResumeNotFromBackground = false;
        }
        mShowAnimationWhenNoStorage = false;
        mNewIntentIsPush = false;
        mHandler.removeCallbacksAndMessages(null);
        YouJuAgent.onPause(UpdateUiActivity.this);
        dismissCheckingAnimation();
        LogUtils.logv(TAG, "onPause");
        Log.v("gary", "onPause");
        Log.e("luofu", " OTA onPause()  "+mResumeNotFromBackground);
//        if(mNoData){
//            finish();
//        }
        
    }

    @Override
    protected void onDestroy() {
        LogUtils.logv(TAG, "onDestroy");
        mUpgradeManager.onActivityDestroy();
        
        // releaseSountPool();
        try{
        mUpgradeManager
        .pauseOtaDownload(new IOtaPauseDownloadCallback(){

			@Override
			public void onPauseComplete() {
				 NotificationUtil.clearNotification(UpdateUiActivity.this, Config.DOWNLOAD_NOTIFICATION_ID);
			}
        	
        });
        }catch(Exception e){
        	e.printStackTrace();
        	LogUtils.logv(TAG, "onDestroy pauseOtaDownload has Exception");
        }
        NotificationUtil.clearNotification(this, Config.DOWNLOAD_NOTIFICATION_ID);
        mUpgradeManager = null;
        unregisterMobileNetConfirmReceiver();
        unregisterConnectivityReceiver();//add by gary.gou for bug 5589
        super.onDestroy();
    }

    /**
     * need to check new version or not when invoke onResume();
     * 1:if start ota application from launcher ,will return true
     * 2:if current net work is not useable,return false
     * 3:if get msg from push will return true
     * @return
     */
    private boolean needCheckVersionWhenOnResume() {
        if (!HttpUtils.isNetworkAvailable(this)) {
            Log.e("ota", "!HttpUtils.isNetworkAvailable");
            return false;
        }

        boolean resumeByPush = mNewIntentIsPush || isPushIntent(getIntent());

        if (resumeByPush) {
            return true;
        }

        if (/*HttpUtils.isWIFIConnection(this) && */mResumeNotFromBackground) {
            return true;
        }

        return false;

    }

    protected void onResume() {
        super.onResume();
        SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_WHITE, this);
         checkWhenStart();
    }
    
    private void checkWhenStart(){
        YouJuAgent.onResume(UpdateUiActivity.this);
        if (mResumeNotFromBackground) {
            writeInitialSettingToStatistics();
            writeNetInfoSatics();
        }
        mOtaState = mUpgradeManager.getOtaUpgradeState();
        LogUtils.logv(TAG, "onResume mOtaState=" + mOtaState+" mResumeNotFromBackground "+mResumeNotFromBackground);
        LogUtils.logv(TAG, "onResume ---+ mOtaState=" + mOtaState);
        if ((OtaUpgradeState.INITIAL != mOtaState) && (OtaUpgradeState.CHECKING != mOtaState)) {
            mOtaUpgradeInfo = mUpgradeManager.getOtaUpgradeInfo();
        }
        if (mResumeNotFromBackground) {
            isPauseOnResume = true;
            showAnimation();
            return;
        }

        switch (mOtaState) {
            case INITIAL:
                Log.e("ota", "needCheckVersionWhenOnResume=" + needCheckVersionWhenOnResume());
                if (needCheckVersionWhenOnResume()) {
                    try {
                        mUpgradeManager.checkOtaVersion(new CallBack().mOtaCheckVersionCallback);
                    } catch (ErrorStateException e) {
                        LogUtils.logd(TAG, "onresume() checkOtaVersion----mOtaState = " + mOtaState);
                    }
                }
                loadUpdateInfoCheckView();
                break;
            case CHECKING:
                loadUpdateInfoCheckView();
                mUpgradeManager.resumeCallback(new CallBack().mOtaCheckVersionCallback);
                break;
            case READY_TO_DOWNLOAD:
                long lastCheckTime = OtaSettings.getLastCheckTime(UpdateUiActivity.this, 0);
                if ((System.currentTimeMillis() - lastCheckTime) >= ONE_DAY) {
                    loadUpdateInfoCheckView();
                    mUpgradeManager.resumeInitialStateAfterOneDay();
                    return;
                }
                loadReadyToDownView(false);
                break;
            case DOWNLOADING:
               /* if (mClickPause) {
                    LogUtils.logd(TAG, "mClickPause = " + mClickPause);
                    mUpgradeManager.resumeCallback(new CallBack().mOtaPauseDownloadCallback);
                    return;
                }*/
                loadDownProgressView(false, false);
                mUpgradeManager.resumeCallback(new CallBack().mOtaDownloadCallback);
                mHandler.sendEmptyMessageDelayed(MSG.NOTIFY_LOW_DOWNLOAD_SPEED,
                        NetworkConfig.GIONEE_CONNECT_TIMEOUT);
                break;
            case DOWNLOAD_INTERRUPT:
                loadDownProgressView(false, true);
                mUpgradeManager.resumeCallback(new CallBack().mOtaDownloadCallback);
                break;
            case DOWNLOAD_PAUSE:
                loadDownProgressView(true, false);
                break;
            case DOWNLOAD_COMPLETE:
                loadDownCompleteView();
                break;
            default:
                break;
        }

        processLastError(mUpgradeManager.popLastErrorCode());
    }

    /**
     * process ota checking error
     * @param lastErrorCode
     */
    private void processLastError(int lastErrorCode) {
        LogUtils.logd(TAG, "processLastError() lastErrorCode = " + lastErrorCode);
        switch (lastErrorCode) {
            case Error.ERROR_CODE_STORAGE_NO_SPACE:
                showDialog(DIALOG_ID_NO_SPACE);
                break;

            case Error.ERROR_CODE_REMOTE_FILE_NOT_FOUND:
                showToast(R.string.remote_file_not_found);
                break;

            case Error.ERROR_CODE_INTERNAL_STORAGE_NO_SPACE:
                showDialog(DIALOG_ID_EEMC_NO_SPACE);
                break;

            case Error.ERROR_CODE_FILE_VERIFY_FAILED:
                showDialog(DIALOG_ID_FILE_MD5_CHECK_ERR);
                break;

            default:
                break;
        }

    }
    
    /**
     * use this method to filter latter and digit
     * @param src
     * @return
     */
    private boolean containLetter(String src){
        if(TextUtils.isEmpty(src)){
            return false;
        }
        String regex=".*[a-zA-Z]+.*";
        Matcher m=Pattern.compile(regex).matcher(src);
        Pattern pattern = Pattern.compile("[0-9]*");
        return m.matches(); 
    }

    /**
     * set new version info to info layout
     */
    public void setNewVerText() {
        if (!mFirstTimeCheck) {
            showCurrentVersion();
            return;
        }
        DetailsInfoActivity.HAS_NEW_VERSION = true;
        String ver = "";
//        if(mOtaUpgradeInfo.getRecoveryUpdate()){
            ver = mOtaUpgradeInfo.getInternalVer();
        
//        }
        
        String[] verSub = ver.split("-");
        
        /*
         * split new version info from its end char,we just show this msg
         * like (IUNI-I9500-AlphaV1.0-20140506(112M))
         */
        String endStr = verSub[verSub.length-1];//ver.substring(ver.length()-4, ver.length());
        if(!containLetter(endStr)){
            ver = ver.substring(5, ver.length() - 4);
        }else{
            if(!TextUtils.isEmpty(endStr)){
                for(int i = 0;i< endStr.length();i++){
                    String subStr = endStr.charAt(i)+"";
                    if(containLetter(subStr)){
                        int end = endStr.length() - i;
                        ver = ver.substring(5, ver.length() - (4 + end));
                        Log.e("ver", ver);
                        break;
                    }
                } 
            }
            
        }
        mOtaTotalFileSize = mOtaUpgradeInfo.getFileSize();
        String mOtaTotalFileSizeFormat = "";
        if (mOtaTotalFileSize >= 0) {
            mOtaTotalFileSizeFormat = String.format("%.2f",
                    ((double) mOtaTotalFileSize / (1024 * 1024)));
        }
        // Aurora <likai> modify begin
        /*
         * String newVersionFormat =
         * String.format(getString(R.string.text_new_version), ver,
         * mOtaTotalFileSizeFormat); SpannableStringBuilder ssb = new
         * SpannableStringBuilder(newVersionFormat); ssb.setSpan(new
         * AbsoluteSizeSpan
         * (getResources().getInteger(R.integer.new_version_filesize_textsize)),
         * newVersionFormat.indexOf("("), newVersionFormat.indexOf(")") + 1,
         * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); mDownNewVersion.setText(ssb);
         * String currentDownloadedPepleFormat =
         * String.format(getString(R.string.text_current_download),
         * mOtaUpgradeInfo.getDownloadedPeopleNum());
         * mDownLoadPeople.setText(currentDownloadedPepleFormat);
         */
        String newVersionFormat = String.format(getString(R.string.new_version_info), ver,
                mOtaTotalFileSizeFormat);
        // Aurora <Luofu> <2013-12-11> modify for Animation begin
        mInfoLayout.setVisibility(View.VISIBLE);
        if (isStart || isFromUpdateButFileDeleted) {
            if(isStart){
                showLogoAnimation(true);
                isStart = !isStart;
            }
            mVersionView.startAnimation(mBottomViewAnim);
            if(isFromUpdateButFileDeleted){
                isFromUpdateButFileDeleted = false;
            }
        }
        mVersionView.setVisibility(View.VISIBLE);
        /*
         * show the button panel with mBottomViewAnim
         */
        mBottomViewAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                mStartButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(300);
                    mCheckResultView.startAnimation(mVersionAnim);
//                    mButtonAnim.setAnimationListener()
                    mStartButton.startAnimation(mButtonAnim);
                    mOneButton = true;
//                    mStartButton.setVisibility(View.VISIBLE);
                    mCheckResultView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        });
        // mVersionView.setVisibility(View.VISIBLE);
        // Aurora <Luofu> <2013-12-11> modify for Animation end
        if(mOtaUpgradeInfo.getRecoveryUpdate()){
            mResultView.setText(R.string.have_new_recovery);
        }else if (mOtaUpgradeInfo.getExtPkg()) {
            mResultView.setText(R.string.have_new_version_extpkg);
        } else {
            mResultView.setText(R.string.have_new_version);
        }
        mVersionInfoView.setText(newVersionFormat);
        mResultView.setVisibility(View.VISIBLE);
        mVersionInfoView.setVisibility(View.VISIBLE);
        mButtonView.setVisibility(View.VISIBLE);
       // LayoutParams params = (LayoutParams) mButtonView.getLayoutParams();
       // params.height = 384;
        //mButtonView.setLayoutParams(params);
        // Aurora <likai> modify end
       
    }

    /**
     * init animation for checking 
     */
    public void initialCheckAnimation() {
        // Aurora <likai> modify begin
        /*
         * mRoateAImageView = (ImageView) findViewById(R.id.roate_animation);
         * mRoateAnimation = new RotateAnimation(0f, +360f,
         * Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
         * mRoateAnimation.setRepeatCount(Animation.INFINITE);
         * mRoateAnimation.setDuration(3000); LinearInterpolator lin = new
         * LinearInterpolator(); mRoateAnimation.setInterpolator(lin);
         * mCheckButton.setBackgroundResource(R.anim.frameanim);
         * mFrameAnimationDrawable = (AnimationDrawable)
         * mCheckButton.getBackground(); mFrameAnimationDrawable.start();
         * mRoateAImageView.setVisibility(View.GONE);
         */
        mNailAnimationSet = new AnimationSet(true);
        mRotateAnimationSet = new AnimationSet(true);
        
        mIndexInitAnimationSet = new AnimationSet(true);
//        mIndexInitAnimationSet.setDuration(500);
        mIndexInitAnimationSet.setInterpolator(new LinearInterpolator());
        
        mIndexInitAlphaAnimation = new AlphaAnimation(0.0f, 0.5f);
        mIndexInitAlphaAnimation.setDuration(400);
        mIndexInitAlphaAnimation.setInterpolator(new LinearInterpolator());
        
        mIndexInitRoateAnimation = new RotateAnimation(0f, 0f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mIndexInitRoateAnimation.setDuration(400);
        mIndexInitRoateAnimation.setInterpolator(new LinearInterpolator());

        mIndexInitAnimationSet.addAnimation(mIndexInitAlphaAnimation);
        mIndexInitAnimationSet.addAnimation(mIndexInitRoateAnimation);
        
        
        mIndexInitAnimationSet.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
//                showCheckingAnimation();
//                mInitIndexView.setVisibility(View.INVISIBLE);
                doActionAfterAnimotion();
//                mHandler.sendEmptyMessage(MESSAGE_NOTIFY_FRAME_ANIMOTION_END);
            }
        });
        
        mRoateAnimation = new RotateAnimation(0f, +360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mRoateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        mRoateAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mRoateAnimation.setInterpolator(new LinearInterpolator());

        
        mNailImageView.setBackgroundResource(R.drawable.aurora_nail);

        
        
        mNailRotateAnimation = new RotateAnimation(0f, +360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mNailRotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        mNailRotateAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mNailRotateAnimation.setInterpolator(new LinearInterpolator());
        
        mShadowAnimation = new RotateAnimation(0f, +360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mShadowAnimation.setRepeatCount(RotateAnimation.INFINITE);
        mShadowAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mShadowAnimation.setInterpolator(new LinearInterpolator());

//        mRoateAImageView = (ImageView) findViewById(R.id.rotate_anim_view);
//        mShadowView = (ImageView) findViewById(R.id.shadow_view);
//        mShadowView.setBackgroundResource(R.drawable.aurora_shadow);
//        mRoateAImageView.setBackgroundResource(R.drawable.aurora_index);
        mRoateAImageView.setVisibility(View.INVISIBLE);
        mShadowView.setVisibility(View.INVISIBLE);
        // Aurora <likai> modify end
//        mInitIndexView.setVisibility(View.INVISIBLE);
        mNailAnimationSet.addAnimation(mNailRotateAnimation);
        mNailAnimationSet.addAnimation(mNailAlphaAnim);
        mNailAnimationSet.setRepeatCount(RotateAnimation.INFINITE);
        mNailAnimationSet.setInterpolator(new LinearInterpolator());
        mNailAnimationSet.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
//                mNailImageView.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                
            }
        });
        
        mRotateAnimationSet.addAnimation(mRoateAnimation);
//        mRotateAnimationSet.addAnimation(mNailAlphaAnim);
        mRotateAnimationSet.setRepeatCount(RotateAnimation.INFINITE);
        mRotateAnimationSet.setInterpolator(new LinearInterpolator());
        mRotateAnimationSet.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
//                mRoateAImageView.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    /*
     * private void setUpgradeFromLocalMenuItemVisable(Menu menu, boolean
     * visable) { MenuItem item = menu.findItem(R.id.upgrade_from_local);
     * item.setVisible(visable); }
     */

    private void loadUpdateInfoCheckView() {
        LogUtils.logd(TAG, "LoadUpdateInfoCheckView");
        // Aurora <likai> modify begin
        /*
         * showCheckButton(true); mGnLayout.showCheckLayout();
         * initialCheckAnimation(); mBottomTextView.setVisibility(View.VISIBLE);
         * mOtaState = mUpgradeManager.getOtaUpgradeState();
         * mBottomStatusTextView.setText(null);
         */
       // showButton(true, false, false);
//        initialCheckAnimation();
        mOtaState = mUpgradeManager.getOtaUpgradeState();
        mStatusView.setText(null);
        // Aurora <likai> modify end
        boolean hasStorage = StorageUtil.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if ((OtaUpgradeState.CHECKING == mOtaState)) {
            showCheckingAnimation();
        } else {
            if(!hasStorage && mShowAnimationWhenNoStorage){
                showCheckingAnimation();
                mShowAnimationWhenNoStorage = false;
            }else{
                dismissCheckingAnimation();
            }
        }
        
        // Aurora <likai> modify begin
        // mCheckButton.setOnClickListener(new OnClickListener() {
        mStartButton.setOnClickListener(new OnClickListener() {
            // Aurora <likai> modify end
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(UpdateUiActivity.this.VIBRATOR_SERVICE);
                vibrator.vibrate(VIBRATE_LONG);
                writeStatistics("C1");
                writeNetInfoSatics();
                if (HttpUtils.isNetworkAvailable(UpdateUiActivity.this)) {
                    try {
                        mUpgradeManager.checkOtaVersion(new CallBack().mOtaCheckVersionCallback);
                        OtaSettings.setLastCheckTime(UpdateUiActivity.this,
                                System.currentTimeMillis());
                        showCheckingAnimation();
                    } catch (ErrorStateException e) {
                        LogUtils.logd(TAG, "checkOtaVersion----mOtaState = " + mOtaState);
                    }
                } else {
                    sendNoNetworkBroadcast();
                }
            }
        });

        if (!HttpUtils.isNetworkAvailable(this)) {
            showCheckingAnimation();
            mHandler.sendEmptyMessageDelayed(MESSAGE_NOTIFY_NO_NETWORK, SEND_MASSEGE_DURATION);
        }
    }

    private void loadReadyToDownView(boolean isCheckNew) {
        LogUtils.logd(TAG, "LoadReadyToDownView");
        // Aurora <likai> modify begin
        /*
         * showCheckButton(false); dismissCheckingAnimation(); mOtaState =
         * mUpgradeManager.getOtaUpgradeState(); if (isCheckNew) { if
         * (!mGnLayout.isInitFinish()) { mGnLayout.initView();
         * LogUtils.logd("wangyan", "mGnLayout.initView();"); }
         * mGnLayout.showDownLoadLayout(); } else {
         * mGnLayout.showDownLoadLayoutWithoutAnim(); }
         * mBottomTextView.setVisibility(View.INVISIBLE);
         * mBottomStatusTextView.setText(null);
         * mGnProgressButton.setPause(true); mGnProgressButton.setEnabled(true);
         * mGnProgressButton.setProgress(0);
         * mGnProgressButton.setTextString(getString(R.string.start_down));
         */
        showButton(false, true, false);
        dismissCheckingAnimation();//停止指针动画
        if(mUpgradeManager == null){
            mUpgradeManager = OtaUpgradeManager.getInstance(this);
        }
        mOtaState = mUpgradeManager.getOtaUpgradeState();

        mStatusView.setText(null);
        mProgressView.setText(null);
        // Aurora <likai> modify end

        setNewVerText();
        mPanel = UiPanel.PANEL_HAS_NEW_VERSION;
        mNoData = false;
        // Aurora <likai> modify begin
        // mGnProgressButton.setOnClickListener(new OnClickListener() {
        mStartButton.setOnClickListener(new OnClickListener() {
            // Aurora <likai> modify end
            public void onClick(View v) {
            	//BUG #5018 点击升级后升级按钮不消失
            	if(mButtonAnim != null && mButtonAnim.hasStarted() && !mButtonAnim.hasEnded()){
            	    return;	
            	}
            	
                writeStatistics("D1");
                writeNetInfoSatics();
                isStartButtonClicked = true;
                requestDownloadOtaFile();
                mOneButton = false;
               // mStartButton.setVisibility(View.GONE);

            }
        });
        if(mRecoveryUpdated){
            showDialog(DIALOG_NOTICE_RECOVERY_UPDATED);
        }
    }

    private void setWaiting(boolean isWaiting) {
        boolean isPause = mUpgradeManager.getOtaUpgradeState() == OtaUpgradeState.DOWNLOAD_PAUSE;
        LogUtils.log(TAG, "setWaiting    isPause   = "  + isPause);
        if (isWaiting && !isPause) {
            if (HttpUtils.isMobileNetwork(UpdateUiActivity.this)) {
                if (StorageUtil.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // mBottomStatusTextView.setText(R.string.download_wait_WLAN);
                    // showDialog(DIALOG_ID_MOBILE_NET);
                    mStatusView.setText(getString(R.string.download_wait_WLAN));
                } else {
                    // mBottomStatusTextView.setText(getString(R.string.download_wait_storage));
                    mStatusView.setText(getString(R.string.download_wait_storage));
                }
            } else if (StorageUtil.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // mBottomStatusTextView.setText(R.string.download_suspend);
                // showToast(R.string.pause_download_no_network);
                mStatusView.setText(getString(R.string.download_suspend));
            } else {
                // mBottomStatusTextView.setText(getString(R.string.download_wait_storage));
                mStatusView.setText(getString(R.string.download_wait_storage));
            }
        }

        mIsWaiting = isWaiting;
        // Aurora <likai> modify begin
        // mGnProgressButton.setPause(isWaiting);
        if (isWaiting) {
            dismissCheckingAnimation();
            // if (isPause) return;
            //
            // try {
            // mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
            // mClickPause = true;
            // mUpgradeManager.pauseOtaDownload(new
            // CallBack().mOtaPauseDownloadCallback);
            // } catch (ErrorStateException e) {
            // LogUtils.logd(TAG, "pauseOtaDownload catch----mOtaState = " +
            // mOtaState);
            // }
        } else {
            showCheckingAnimation();
        }
        // Aurora <likai> modify end
    }

    private void setPause(boolean ispause) {
        LogUtils.logd(TAG, "ispause = " + ispause);
        // Aurora <likai> modify begin
        /*
         * if (ispause) {
         * mBottomTextView.setText(getString(R.string.continue_dowm));
         * mBottomStatusTextView.setText(null); } else {
         * mBottomTextView.setText(getString(R.string.pause_down)); }
         * mGnProgressButton.setPause(ispause);
         */
        if (ispause) {
            mStatusView.setText(null);
            dismissCheckingAnimation();
        } else {
        	 LogUtils.logd(TAG, "setPause     isNotShowDialogForMoblieNet = " + isNotShowDialogForMoblieNet);
        	if(isNotShowDialogForMoblieNet){ //切换到移动网络的时候，不再重复启动动画
        	 showCheckingAnimation();
        	}
        }
        // Aurora <likai> modify end
    }

    private int getLastProgress() {
        String fileNameWithoutStoragePath = FileUtil
                .getOtaFileNameWithoutStoragePath(mOtaUpgradeInfo
                        .getMd5());
        File file = FileUtil.getAlreadyDownloadedFile(UpdateUiActivity.this,
                fileNameWithoutStoragePath);
        if (file != null) {
            mProgress = Util.getDownloadProgress((int) FileUtil.getFileLengthByUrl(UpdateUiActivity.this, mOtaUpgradeInfo.getDownloadurl()), mOtaTotalFileSize);
        }
        return mProgress;
    }
    
    private void loadDownProgressView(boolean ispause, boolean isWaiting) {
        LogUtils.logd(TAG, "loadDownProgressView");
        // Aurora <likai> modify begin
        /*
         * mGnLayout.showDownLoadLayoutWithoutAnim();
         * mBottomTextView.setVisibility(View.VISIBLE);
         * mBottomTextView.setText(getString(R.string.pause_down));
         * showCheckButton(false); setNewVerText();
         */
        if (ispause) {
            showButton(false, false, false);
            mPauseDownloadButton.setText(R.string.continue_download);
        } else {
            showButton(false, false, false);
            mPauseDownloadButton.setText(R.string.pause_download);
        }
//        if (!mPauseButtonClicked) {
//          mButtonView.startAnimation(mPauseViewAnim);
//      }
//      if (isStartButtonClicked) {
//          mButtonView.startAnimation(mPauseViewAnim);
//          isStartButtonClicked = !isStartButtonClicked;
//      }
      if(isResultOk ||/* !mPauseButtonClicked ||*/isStartButtonClicked){
          mButtonView.startAnimation(mPauseViewAnim);
          isResultOk = false;
          isStartButtonClicked = false;
      }
//        initialCheckAnimation();
        hideNewVersion();
        // Aurora <likai> modify end
        
        if(mPauseDownloadButton != null){
        	mPauseDownloadButton.setEnabled(true);
        }
        if(mCancelDownloadButton != null){
        	mCancelDownloadButton.setEnabled(true);
        }

        setPause(ispause);
        if (isWaiting) {
            setWaiting(isWaiting);
        }
        mPanel = UiPanel.PANEL_DOWNLOADDING;
        // Aurora <likai> modify begin
        // mGnProgressButton.setEnabled(true);
        // mGnProgressButton.setProgress(getLastProgress());
        // mGnProgressButton.setOnClickListener(new OnClickListener() {
        showProgress("" + getLastProgress() + "%");
        mPauseDownloadButton.setOnClickListener(new OnClickListener() {
            // Aurora <likai> modify end
            public void onClick(View v) {
                mOtaState = mUpgradeManager.getOtaUpgradeState();
                LogUtils.logd(TAG, "loadDownProgressView()---mOtaState = " + mOtaState);
                // Aurora <likai> modify begin
                // mBottomStatusTextView.setText(null);
                mStatusView.setText(null);
                // Aurora <likai> modify end
                switch (mOtaState) {
                    case DOWNLOADING:
                    	if(mPauseState == 1){ //暂停前 点击继续
                    		 LogUtils.logd(TAG,  "1   mPauseState =   "+ mPauseState);
                    		writeNetInfoSatics();
                         mPauseButtonClicked = true;
                    		if(beforeDownloadOtaFile()){
                    		mPauseState = 3;
                    		LogUtils.logd(TAG,  "  2  mPauseState =   "+ mPauseState);
                    		}
                    		return;
                    	}
                    	if(mPauseState == 3){ //暂停前 点击继续 又点击暂停
                    		LogUtils.logd(TAG,  "  3  mPauseState =   "+ mPauseState);
                            setPause(true);
                            mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                           mPauseDownloadButton.setEnabled(true);
                           mPauseDownloadButton.setText(R.string.continue_download);
                           mClickPause = true;
                           mPauseState = 1;
                           LogUtils.logd(TAG,  "  4  mPauseState =   "+ mPauseState);
                    		return;
                    	}
                        // Aurora <likai> modify begin
                        // mGnProgressButton.setEnabled(false);
                       // mPauseDownloadButton.setEnabled(false);
                        // Aurora <likai> modify end
                    case DOWNLOAD_INTERRUPT:
                       
                        try {
                            setPause(true);
                            mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                           mPauseDownloadButton.setEnabled(false);
                           showButton(false, false, false);
                           mPauseDownloadButton.setText(R.string.continue_download);
                
               
                            mClickPause = true;
                           
                            mUpgradeManager
                                    .pauseOtaDownload(new CallBack().mOtaPauseDownloadCallback);
                            //由于暂停下载需要较长的一段时间，所以做出一个假象，使继续下载按钮可以点击但是不做什么操作
                            //begin
                            mPause = false;
                            mPauseState = 1;
                            mPauseDownloadButton.setEnabled(true);
                            showButton(false, false, false);
                          
                            LogUtils.logd(TAG,  "  5  mPauseState =   "+ mPauseState);
                            //轮询状态
		                            new Thread(new Runnable(){  
		                            	  
		                                @Override  
		                                public void run() {  
		                                    while(true){
		                                    	if(mPause){
		                                    		if(mPauseState == 3){
		                                    			mHandler.sendEmptyMessage(MESSAGE_NOTIFY_REQUEST_DOWNLOAD);
		                                    			/*try{
		                                      mUpgradeManager.downloadOtaFile(new CallBack().mOtaDownloadCallback);//下载文件
		                                    			} catch (ErrorStateException e) {
		                                                    LogUtils.logd(
		                                                            TAG,
		                                                            "loadDownProgressView----mOtaState = "
		                                                                    + mUpgradeManager.getOtaUpgradeState());
		                                                } */
		                                      mPauseState = 0;
		                                      LogUtils.logd(TAG,  "  6 mPauseState =   "+ mPauseState);
		                                      			return;
		                                    		}
		                                    mPauseState = 2;
		                                    LogUtils.logd(TAG,  "  7  mPauseState =   "+ mPauseState);
		                                    		return;
		                                    	}
		                                    	
		                                    }
		                                   
		                                  }
		                                }).start();  
                            //end
                            
                        } catch (ErrorStateException e) {
                            LogUtils.logd(TAG, "pauseOtaDownload catch----mOtaState = " + mOtaState);
                        }
                        break;
                    case DOWNLOAD_PAUSE:
                    	mPauseState = 0;
                    	LogUtils.logd(TAG,  "  8  mPauseState =   "+ mPauseState);
                        writeNetInfoSatics();
                        mPauseButtonClicked = true;
                        requestDownloadOtaFile();
                        break;
                    default:
                        break;
                }
            }
        });

        // Aurora <likai> add begin
        mCancelDownloadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ID_CANCEL_DOWNLOAD);
            }
        });
        // Aurora <likai> add end
    }
    
    public boolean beforeDownloadOtaFile() {
        if (StorageUtil.isExSdcardInserted(this)) {
        	LogUtils.log(TAG, " beforeDownloadOtaFile   more than 1 storage is mounted");
            if ((!StorageUtil.checkExternalStorageMounted())
                    || (!StorageUtil.checkInternalStorageMounted(UpdateUiActivity.this))) {
                showToast(R.string.storage_unuseful);
                return false;
            }
        } else {
            if (!StorageUtil.checkExternalStorageMounted()) {
                showToast(R.string.storage_unuseful);
                return false;
            }
        }
        if (HttpUtils.isWIFIConnection(UpdateUiActivity.this)) { // have
                                                                 // Sdcard，judge
                                                                 // type of
                                                                 // Network is
                                                                 // WIFI
        	LogUtils.log(TAG, " beforeDownloadOtaFile   The network environment is Wifi");
           
                storageSpaceRemind();
                if (mHaveNoSpaceForDownload) {
                    mHaveNoSpaceForDownload = false;
                    return false;
                }
//                if (!mPauseButtonClicked) {
//                    mButtonView.startAnimation(mPauseViewAnim);
//                }
//                if (isStartButtonClicked) {
//                    mButtonView.startAnimation(mPauseViewAnim);
//                    isStartButtonClicked = !isStartButtonClicked;
//                }

            
                if(mPauseDownloadButton != null){
                    mPauseDownloadButton.setEnabled(true);
                }
                if(mCancelDownloadButton != null){
                    mCancelDownloadButton.setEnabled(true);
                }
                mOneButton = false;//是否是一个按钮

                mPauseDownloadButton.setText(R.string.pause_download);

                showCheckingAnimation();
                showProgress("" + getLastProgress() + "%");
                return true;
           
        } else if (HttpUtils.isMobileNetwork(UpdateUiActivity.this)) {
            if (HttpUtils.isWapConnection(UpdateUiActivity.this)) {
            	LogUtils.log(TAG, " beforeDownloadOtaFile   The network environment is MobileNet");
                showToast(R.string.wap_moblie_net);
                return false;
            } else {
            	 showToast(R.string.poor_moblie_net);
                return false;
            }
        } else { // have no network
            sendNoNetworkBroadcast();
            return false;
        }
    }

    private void loadDownCompleteView() {
        LogUtils.logd(TAG, "loadDownCompleteView");
        // Aurora <likai> modify begin
        /*
         * showCheckButton(false); mGnLayout.showDownLoadLayoutWithoutAnim();
         * mGnProgressButton.setPause(true); mGnProgressButton.setEnabled(true);
         * setNewVerText();
         * mGnProgressButton.setProgress(getResources().getInteger
         * (R.integer.download_button_max));
         * mBottomTextView.setVisibility(View.VISIBLE);
         * mBottomTextView.setText(getString(R.string.restart_upgrade));
         * mBottomStatusTextView.setText(null);
         * mGnProgressButton.setOnClickListener(new OnClickListener() {
         */
        showButton(false, false, true);
        // mInfoLayout.setVisibility(View.GONE);
        dismissCheckingAnimation();
        hideNewVersion();

        // showProgress("" +
        // getResources().getInteger(R.integer.download_button_max) + "%");
        mProgressView.setText(null);
        mStatusView.setText(null);
        mStartButton.setOnClickListener(new OnClickListener() {
            // Aurora <likai> modify end
            public void onClick(View v) {
                writeStatistics("E1");
                if (BatteryUtil.getBatteryLevel() < Config.CHARGE) {
                    showDialog(DIALOG_ID_CHARGE);
                } else {
                    showDialog(DIALOG_ID_RESART_UPGRADE);
                }
            }
        });
    }

    private void initialSoundpool() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        mPlaySondId = mSoundPool.load(getApplicationContext(), R.raw.dingdong, 1);
    }

    private void playSound(int soundId, int number) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        float audioCurrentVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        mSoundPool.play(mPlaySondId, volumnRatio, volumnRatio, 1, number, 1);
    }

    private void releaseSountPool() {
        mSoundPool.unload(mPlaySondId);
        mSoundPool.release();
    }

    private void storageSpaceRemind() {
        String fileNameWithoutStoragePath = FileUtil
                .getOtaFileNameWithoutStoragePath(mOtaUpgradeInfo
                        .getMd5());
        try {
            String filePath = FileUtil.getDownloadFilePath(UpdateUiActivity.this,
                    fileNameWithoutStoragePath,
                    mOtaTotalFileSize);
            File file = new File(filePath);
            String storage = StorageUtil.getStroageOfFile(file);
            if (!StorageUtil.isExSdcardInserted(this)) {
                return;
            }
            if (StorageUtil.getExternalStorageAvailableSpace(UpdateUiActivity.this) >= mOtaTotalFileSize) {
                return;
            }
            if (!StorageUtil.isInternalStorage(this, storage)) {
                return;
            }
            if (mUpgradeManager.getOtaUpgradeState() != OtaUpgradeState.READY_TO_DOWNLOAD) {
                return;
            }
            if (getLastProgress() == 100) {
                return;
            }
            showToast(R.string.use_emmc);
        } catch (NoSpaceException e) {
            mHaveNoSpaceForDownload = true;
            if (e.isFileExists() && StorageUtil.isInternalStorage(this, e.getStoragePath())) {
                showToast(R.string.interanl_card_no_space);
            } else {
                showToast(R.string.sdcard_no_space);
            }
			
           //BUG #5992, //modify by gary.gou
            /*
            if(mStartButton != null){
                int visiblity = mStartButton.getVisibility();
                if((visiblity == View.GONE) || (visiblity == View.INVISIBLE)){
                    mStartButton.setVisibility(View.VISIBLE);
                }
            }
            */
        }
    }

    public void requestDownloadOtaFile() {
        if (StorageUtil.isExSdcardInserted(this)) {
        	LogUtils.log(TAG, " requestDownloadOtaFile   more than 1 storage is mounted");
            if ((!StorageUtil.checkExternalStorageMounted())
                    || (!StorageUtil.checkInternalStorageMounted(UpdateUiActivity.this))) {
                showToast(R.string.storage_unuseful);
                return;
            }
        } else {
            if (!StorageUtil.checkExternalStorageMounted()) {
                showToast(R.string.storage_unuseful);
                return;
            }
        }
        if (HttpUtils.isWIFIConnection(UpdateUiActivity.this)) { // have
                                                                 // Sdcard，judge
                                                                 // type of
                                                                 // Network is
                                                                 // WIFI
        	LogUtils.log(TAG, " requestDownloadOtaFile   The network environment is Wifi");
            try {
                storageSpaceRemind();
                if (mHaveNoSpaceForDownload) {
                    mHaveNoSpaceForDownload = false;
                    return;
                }
//                if (!mPauseButtonClicked) {
//                    mButtonView.startAnimation(mPauseViewAnim);
//                }
//                if (isStartButtonClicked) {
//                    mButtonView.startAnimation(mPauseViewAnim);
//                    isStartButtonClicked = !isStartButtonClicked;
//                }

                mUpgradeManager.downloadOtaFile(new CallBack().mOtaDownloadCallback);//下载文件
                if(mPauseDownloadButton != null){
                    mPauseDownloadButton.setEnabled(true);
                }
                if(mCancelDownloadButton != null){
                    mCancelDownloadButton.setEnabled(true);
                }
                mOneButton = false;//是否是一个按钮
                
                loadDownProgressView(false, false);
            } catch (ErrorStateException e) {
                LogUtils.logd(
                        TAG,
                        "loadDownProgressView----mOtaState = "
                                + mUpgradeManager.getOtaUpgradeState());
            } catch (NoSpaceException e) {
                showToast(R.string.sdcard_no_space);
            }
        } else if (HttpUtils.isMobileNetwork(UpdateUiActivity.this)) {
            if (HttpUtils.isWapConnection(UpdateUiActivity.this)) {
            	LogUtils.log(TAG, " requestDownloadOtaFile   The network environment is MobileNet");
                showToast(R.string.wap_moblie_net);
                return;
            } else {
                mWapIsActive = true;
                showDialog(DIALOG_ID_NET_MOBILE);
            }
        } else { // have no network
            sendNoNetworkBroadcast();
        }
    }

    public Dialog onCreateDialog(int id, Bundle bundle) {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this,
                AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        switch (id) {
            case DIALOG_ID_NO_SPACE:
                builder.setTitle(getString(R.string.sd_card_abnormal));
                builder.setMessage(getString(R.string.sdcard_no_space));
                builder.setPositiveButton(getString(R.string.sure), null);
                return builder.create();
            case DIALOG_ID_EEMC_NO_SPACE:
                builder.setTitle(getString(R.string.sd_card_abnormal));
                builder.setMessage(getString(R.string.interanl_card_no_space));
                builder.setPositiveButton(getString(R.string.sure), null);
                return builder.create();
            case DIALOG_ID_FILE_MD5_CHECK_ERR:
                builder.setTitle(getString(R.string.title_error));
                builder.setMessage(getString(R.string.local_file_md5_error));
                builder.setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadReadyToDownView(false);

                            }
                        });
                builder.setPositiveButton(getString(R.string.download_again),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestDownloadOtaFile();
                            }
                        });
                return builder.create();
            case DIALOG_ID_CHECK_UPDATE_FILE_WRONG:
                builder.setTitle(getString(R.string.title_error));
                builder.setMessage(getString(R.string.file_MD5_Err));
                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.setPositiveButton(getString(R.string.download_again),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestDownloadOtaFile();
                            }
                        });
                return builder.create();
            case DIALOG_ID_NET_MOBILE:
                builder.setTitle(getString(R.string.notice_mobile_net));
                builder.setMessage(getString(R.string.start_download_mobile_net));
                /*
                 * builder.setPositiveButton(getString(R.string.wlanset), new
                 * DialogInterface.OnClickListener() {
                 * @Override public void onClick(DialogInterface dialog, int
                 * which) { startActivity(new
                 * Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)); }
                 * });
                 */
                builder.setPositiveButton(getString(R.string.cancel), null);
                builder.setNeutralButton(getString(R.string.continue_dowm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               // 
                                requestDownLoadMobileNet();
                            }
                        });
                return builder.create();
            case DIALOG_ID_MOBILE_NET:
                builder.setTitle(getString(R.string.notice_mobile_net));
                if(!isNotShowDialogForMoblieNet){  //当移动网络断开后，重新连上移动网络，提示以下这段话
                	builder.setMessage(getString(R.string.start_download_mobile_net));
                }else{
                   builder.setMessage(getString(R.string.continue_download_mobile_net));
                }
                builder.setPositiveButton(getString(R.string.cancel), null);
                builder.setNeutralButton(getString(R.string.sure),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestDownLoadMobileNet();
                                
                            }
                        });
                Dialog d = builder.create();
                d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                return d;
            case DIALOG_ID_EXIT_UPGRADE:
                builder.setTitle(getString(R.string.giveup_down));
                builder.setMessage(getString(R.string.exit_upgrade_dialog_message));
                builder.setPositiveButton(getString(R.string.sure),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                exitUpgrade();
                            }
                        });
                builder.setNegativeButton(getString(R.string.cancel), null);
                return builder.create();
            case DIALOG_ID_CHECK_LOCAL_UPDATE_FILE:
                AuroraProgressDialog checkFileDialog = new AuroraProgressDialog(this,
                        AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
                checkFileDialog.setMessage(getString(R.string.check_local_file));
                checkFileDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
                checkFileDialog.setCancelable(true);
                return checkFileDialog;
            case DIALOG_ID_CHECK_LOCAL_UPDATE_FILE_WRONG:
                builder.setMessage(getString(R.string.check_local_file_failed));
                builder.setPositiveButton(getString(R.string.sure), null);
                return builder.create();
            case DIALOG_ID_RESART_UPGRADE:
                builder.setTitle(getString(R.string.system_upgrade));
                builder.setMessage(getString(R.string.system_upgrade_message));
                builder.setPositiveButton(getString(R.string.restart_now),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            	Log.v(TAG, "----set isRestartNow true----");
                                isRestartNow = true; //add by gary.gou
                                showRebootDialog(UpdateUiActivity.this);
                                requestUpgrde();
                            }
                        });
                builder.setNegativeButton(getString(R.string.upgrade_latter), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						 Log.v(TAG, "----set isRestartNow false----");
						 isRestartNow = false; //add by gary.gou
					}
				});
                return builder.create();
            case DIALOG_ID_CHARGE:
                builder.setTitle(getString(R.string.battery_title))
                        .setMessage(getString(R.string.battery_summery2))
                        .setNegativeButton(getString(R.string.sure), null).create();
                return builder.create();
            case DIALOG_ID_CANCEL_DOWNLOAD:
                builder.setTitle(getString(R.string.cancel_download_dialog_title));
                builder.setMessage(getString(R.string.cancel_download_dialog_message));
                builder.setPositiveButton(getString(R.string.sure),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mHandler.removeMessages(MSG.NOTIFY_LOW_DOWNLOAD_SPEED);
                                cancelDownload();
                            }
                        });
                builder.setNegativeButton(getString(R.string.cancel), null);
                return builder.create();
            case DIALOG_ID_RESTART_PROGRESS:
                AuroraProgressDialog dialog = new AuroraProgressDialog(this);
                dialog.setTitle(R.string.start_reboot);
                dialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
                dialog.setMax(0);
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                return dialog;
            case DIALOG_NOTICE_RECOVERY_UPDATED:
                builder.setTitle(getString(R.string.have_new_recovery));
                builder.setMessage(getString(R.string.new_recovery_msg));
                builder.setPositiveButton(getString(R.string.sure),null);
                return builder.create();
            default:
                return null;
        }
    }

    private void requestDownLoadMobileNet() {
       
        if (StorageUtil.isExSdcardInserted(this)) {
            if ((!StorageUtil.checkExternalStorageMounted())
                    || (!StorageUtil.checkInternalStorageMounted(UpdateUiActivity.this))) {
                showToast(R.string.storage_unuseful);
                return;
            }
        } else {
            if (!StorageUtil.checkExternalStorageMounted()) {
                showToast(R.string.storage_unuseful);
                return;
            }
        }
//        if (HttpUtils.isWapConnection(UpdateUiActivity.this)) {
//            showToast(R.string.wap_moblie_net);
//            return;
//        }
        
        if (HttpUtils.isNetworkAvailable(UpdateUiActivity.this)) {
            // Aurora <likai> delete begin
            // mBottomTextView.setText(getString(R.string.pause_down));
            // Aurora <likai> delete end
            try {
                storageSpaceRemind();
                if (mHaveNoSpaceForDownload) {
                    mHaveNoSpaceForDownload = false;
                    return;
                }
//                mButtonView.startAnimation(mPauseViewAnim);
                mUpgradeManager.downloadOtaFile(new CallBack().mOtaDownloadCallback);
                loadDownProgressView(false, false);
                isNotShowDialogForMoblieNet = true;  
            } catch (ErrorStateException e) {
                LogUtils.logd(TAG, "mobile----mOtaState = " + mUpgradeManager.getOtaUpgradeState());
            } catch (NoSpaceException e) {
                showToast(R.string.sdcard_no_space);
            }
        } else {
            sendNoNetworkBroadcast();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        Log.e("window", "focus:"+hasFocus);
//       if(hasFocus){
//           setStatusBarBG(true);
//       }
        super.onWindowFocusChanged(hasFocus);
        
    }
   public void setStatusBarBG(boolean isTransparent) {
       /*Intent StatusBarBGIntent = new Intent();
       StatusBarBGIntent.setAction("aurora.action.SET_STATUSBAR_TRANSPARENT");
       StatusBarBGIntent.putExtra("transparent", isTransparent);
       sendBroadcast(StatusBarBGIntent);*/
       NotificationManager notificationManager = (NotificationManager) this
               .getSystemService(Context.NOTIFICATION_SERVICE);
       Notification.Builder builder = new Builder(this);
       builder.setSmallIcon(R.drawable.aurora_index);
       String tag = "auroraSBFNT653";
       if (isTransparent) {
           tag = "auroraSBFT8345";
       } else {
           tag = "auroraSBFNT653";
       }
       notificationManager.notify(tag, 0, builder.build());

   }
    
    
    private void exitUpgrade() {
        writeStatistics("D2");
        writeNetInfoSatics();
        mUpgradeManager.cancelOtaUpgrade();
        mHandler.removeCallbacksAndMessages(null);
        finish();
    }

   
    private void showRebootDialog(Context context){
       showDialog(DIALOG_ID_RESTART_PROGRESS);
    }
    private void requestUpgrde() {
        if (mLocalUpdate) {
            
            Util.sendUpgradBroadcast(UpdateUiActivity.this, mLocalUpgradeFile);
            mLocalUpdate = false;
            return;
        }
        try {
            mUpgradeManager.upgradeOta(new CallBack().mOtaUpgradeCallback);
        } catch (FileNotFoundException e) {
            dismissDialog(DIALOG_ID_RESTART_PROGRESS);
            isFromUpdateButFileDeleted = true;
            loadReadyToDownView(false);
            showToast(R.string.file_delete);
            isRestartNow = false;
        } catch (ErrorStateException e) {
            LogUtils.logd(TAG, "restart_uograde" + mUpgradeManager.getOtaUpgradeState());
            isRestartNow = false;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.logd(TAG, "requestCode=" + requestCode + "resultCode=" + resultCode);
        // Aurora <likai> add begin
        if(requestCode == REQUEST_CODE_FOR_DETAIL && resultCode == ConfirmDownloadWithMobileNetDialog.POSITIVE_RESULT){
            requestDownLoadMobileNet();
            return;
        }
        
        if (requestCode == REQUEST_CODE_FOR_DETAIL && resultCode == RESULT_OK) {
            mOtaState = mUpgradeManager.getOtaUpgradeState();
            writeStatistics("D1");
            writeNetInfoSatics();
            isResultOk = true;
            
            requestDownloadOtaFile();
            return;
        }
        // Aurora <likai> add end
        mOtaState = mUpgradeManager.getOtaUpgradeState();
        if (requestCode != RESULT_CODE_FOR_FILEMANAGER) {
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        if (OtaUpgradeState.INITIAL != mOtaState) {
            return;
        }
        String path = data.getStringExtra("name");
        if (path == null || path.length() == 0) {
            LogUtils.loge(TAG, "onActivityResult() path is null");
        } else {
            mLocalUpgradeFile = new File(path);
            if (mLocalUpgradeFile.exists()) {
                if (!Util.isAsci(path)) {
                    showToast(R.string.not_ascii);
                    return;
                }
                if (path.endsWith(".zip")) {
                    showDialog(DIALOG_ID_CHECK_LOCAL_UPDATE_FILE);
                    mShowingCheckLocalUpdateFileDialog = true;
                    mUpgradeManager.checkLocalUpgradeFile(mLocalUpgradeFile,
                            new CallBack().mOtaCheckLocalUpgradeFileCallback);
                } else {
                    LogUtils.loge(TAG, "onActivityResult() wrong type");
                }
            } else {
                LogUtils.loge(TAG, "onActivityResult() file not exists");
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            writeStatistics("C2");
            writeNetInfoSatics();

            // Aurora <likai> <2013-11-07> modify begin
            // showDialog(DIALOG_ID_EXIT_UPGRADE);
            if (mUpgradeManager.getOtaUpgradeState() == OtaUpgradeState.DOWNLOADING
                    || mUpgradeManager.getOtaUpgradeState() == OtaUpgradeState.DOWNLOAD_INTERRUPT) {
                showDialog(DIALOG_ID_EXIT_UPGRADE);
            } else {
                exitUpgrade();
            }
            // Aurora <likai> <2013-11-07> modify end

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void sendNoNetworkBroadcast() {
        // Aurora <likai> <2013-10-22> modify begin
        /*
         * Intent intent = new
         * Intent(OtaInent.NO_NETWORK_INTENT_PUBLIC_NETWORK_MODULE);
         * intent.putExtra(OtaInent.INTENT_EXTRA_NAME, getPackageName());
         * sendBroadcast(intent);
         */
        /*
         * Intent intent = new Intent("android.settings.WIFI_SETTINGS");
         * startActivity(intent);
         */
//        showToast(R.string.have_no_network);
        if(mPanel == UiPanel.PANEL_DOWNLOADDING){
            showToast(R.string.have_no_network);
        }else{
            String msgTitle = getString(R.string.have_no_network);
            String msgInfo = getString(R.string.msg_for_check_network);
            showNotification(msgTitle, msgInfo,false);  
        }
        
        // Aurora <likai> <2013-10-22> modify end
    }

    public static final String ACTION_CHANGE_TO_MOBILE="com.aurora.ota.ChangeToMobileNetReceiver";
    ChangeToMobileNetReceiver confirmMobileReceiver;
    class ChangeToMobileNetReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals(ACTION_CHANGE_TO_MOBILE)){
                LogUtils.log(TAG, "download  ChangeToMobileNetReceiver");
                isNotShowDialogForMoblieNet = false; //移动网络时，提示用户
                showConfirmMobileDialog();
            }
            
        }
        
    }
    private void registerMobileNetConfirmReceiver(){
        confirmMobileReceiver = new ChangeToMobileNetReceiver();
        IntentFilter filter = new IntentFilter(ACTION_CHANGE_TO_MOBILE);
        registerReceiver(confirmMobileReceiver, filter);
    }
    private void unregisterMobileNetConfirmReceiver()
    {
        unregisterReceiver(confirmMobileReceiver);
    }
    
    
    private void showConfirmMobileDialog(){
        setWaiting(true);
        try{
            mUpgradeManager.pauseOtaDownload(new CallBack().mOtaPauseDownloadCallback);
        }catch (Exception e) {
            // TODO: handle exception
        }
//        showDialog(DIALOG_ID_MOBILE_NET);
  
        LogUtils.log(TAG, "showConfirmMobileDialog     isNotShowDialogForMoblieNet   ==  "  + isNotShowDialogForMoblieNet);
        if(isNotShowDialogForMoblieNet){
            requestDownLoadMobileNet();
        }else{
            showDialog(DIALOG_ID_MOBILE_NET);
           
        }
//        startActivityForResult(new Intent(this,ConfirmDownloadWithMobileNetDialog.class), REQUEST_CODE_FOR_DETAIL);
    }
    
    
   //Begin add by gary.gou for bug 5589
    WifiStateChangeReceiver mWifiStateChangeReceiver;
    class WifiStateChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            	LogUtils.log(TAG, "WifiStateChangeReceiver---onReceive");
            //	isNotShowDialogForMoblieNet = false;
            }
        }
        
    }
    
    private void registerConnectivityReceiver(){
    	 mWifiStateChangeReceiver = new WifiStateChangeReceiver();
    	 IntentFilter intentFilter = new IntentFilter();
    	 intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
         registerReceiver(mWifiStateChangeReceiver, intentFilter);
    }
    
    
    private void unregisterConnectivityReceiver()
    {
        unregisterReceiver(mWifiStateChangeReceiver);
    }
    //End add by gary.gou for bug 5589
    
    private void showLogoAnimation(boolean newVersion) {
        
        if (newVersion) {
            endPaddingTop = (int) mLogoPaddinTopHasNewVPX;
        } else {
            endPaddingTop = (int) mLogoPaddingTopNoVPX;
        }
        currentPaddinTop = mLogoLayout.getPaddingTop();
        v = getV();
        startAnimation();
    }


    private int duration = 300;
    private int endPaddingTop;

    private float v;
    private int currentPaddinTop;

    
    private static final int MSG_UPDATE_MARGIN = 100;
    private static final int MSG_STOP_ANIM = 101;
    private Handler animHandler = new Handler() {
        public void handleMessage(Message msg) {
            
            mLogoLayout.setPadding(mLogoLayout.getPaddingLeft(), currentPaddinTop,
                    mLogoLayout.getPaddingRight(), mLogoLayout.getPaddingBottom());
            mLogoLayout.invalidate();
        };

    };

    private float getV() {
        int movingDistance = currentPaddinTop - endPaddingTop;
        return (float) movingDistance / duration;
    }

    public void startAnimation() {
//        animThread.start();
        new LogoAnimThread().start();
    }

//    Thread animThread = new Thread(new Runnable() {
//
//       
//    });
    
    class LogoAnimThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (duration > 0) {
                try {
                    duration--;
                    Thread.sleep(1);

                    if (currentPaddinTop > endPaddingTop) {
                        currentPaddinTop = (int) ((float) currentPaddinTop - v);

                        // Log.e("luofu", "V:"+v);
                        animHandler.sendEmptyMessage(MSG_UPDATE_MARGIN);
                    }else{
                        currentPaddinTop = endPaddingTop;
                        break;
                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onAnimationStart() {
        // TODO Auto-generated method stub
//        mInitIndexView.startAnim();
        if(mRoateAImageView != null){
            mRoateAImageView.setVisibility(View.GONE);
        }
        
    }

    @Override
    public void onAnimationEnd() {
        // TODO Auto-generated method stub
        mFrameAnimView.setBackgroundResource(R.drawable.aurora_frame_anim_27);
//        doActionAfterAnimotion();
////        showCheckingAnimation();
//        mRoateAImageView.clearAnimation();
//        mHandler.sendEmptyMessage(MESSAGE_NOTIFY_FRAME_ANIMOTION_END);
//        mRoateAImageView.clearAnimation();
    }

    @Override
    public void onRepeat(int repeatIndex) {
        // TODO Auto-generated method stub
       
    }

    @Override
    public void onFrameChange(int repeatIndex, int frameIndex, int currentTime) {
        // TODO Auto-generated method stub
        if(frameIndex == 15){
//            mInitIndexView.startAnimation(mIndexInitAnimationSet);
            mInitIndexView.startAnim();
            }
    }
    
    
    
       //Begin add by gary.gou
    private AnimationListener mAnimListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			mRoateAImageView.setVisibility(View.GONE);
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			 mInitIndexView.startAnim();
			 isInitIndexViewStartAnim = true;
		}
	};
    
   	//bug 6109,otaDownloaded,disable back key
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	  // TODO Auto-generated method stub
	 
	  if(isRestartNow && event.getKeyCode() == event.KEYCODE_BACK){
		   Log.v(TAG, "----isRestartNow is true----");
		   return true;
	  }
	  
	  return super.dispatchKeyEvent(event);
	}
    
   //End add by gary.gou
    

}
