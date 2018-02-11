package com.aurora.note.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.aurora.note.NoteApp;
import com.aurora.note.R;
import com.aurora.note.activity.fragment.PicturePreviewFragment;
import com.aurora.note.report.ReportCommand;
import com.aurora.note.report.ReportUtil;
import com.aurora.note.sina.weibo.AccessTokenKeeper;
import com.aurora.note.sina.weibo.Constants;
import com.aurora.note.sina.weibo.ShareSinaWeiboActivity;
import com.aurora.note.sina.weibo.SinaAccessToken;
import com.aurora.note.ui.PicViewPager;
import com.aurora.note.util.BitmapUtil;
import com.aurora.note.util.FileUtils;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SDcardManager;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.ToastUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
//import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.open.utils.HttpUtils.HttpStatusException;
import com.tencent.open.utils.HttpUtils.NetworkUnavailableException;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class WXEntryActivity extends FragmentActivity implements OnClickListener, IWXAPIEventHandler, IUiListener, IRequestListener {

    private static final String TAG = "WXEntryActivity";

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 128;

    private IWXAPI api;

    private String mImageUrl;
    private ArrayList<String> data = new ArrayList<String>();

    private ImagePagerAdapter picPagerAdapter;

    private RelativeLayout topTools;
    private ImageButton back;

    private PicViewPager vp;

    private View bottomTools;
    private View mSaveToGallery;
    private View mShareByWeibo;
    private View mSendByWx;
    private View mShareByWx;
    private View mShareByQQ;
    private View mShareByQZone;
    private View mShareMore;

    // 图片加载工具
    public ImageLoader imageLoader = ImageLoader.getInstance();
    public DisplayImageOptions options;

    // 控件隐藏状态
    private boolean isVisible = true;

    private SsoHandler mSsoHandler;
    
    // 腾讯QQ分享相关
    private Tencent mTencent;

    private void showTools() {
        topTools.startAnimation(AnimationUtils.loadAnimation(
        		WXEntryActivity.this, R.anim.pic_view_push_top_in));
        bottomTools.startAnimation(AnimationUtils.loadAnimation(
        		WXEntryActivity.this, R.anim.pic_view_push_bottom_in));
        topTools.setVisibility(View.VISIBLE);
        bottomTools.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.aurora.utils.SystemUtils.setStatusBarBackgroundTransparent(this);

        api = WXAPIFactory.createWXAPI(this, Globals.APP_ID);
        mTencent = Tencent.createInstance(Globals.QQ_APP_ID, getApplicationContext());

        setContentView(R.layout.picture_preview_activity);
        // 初始化控件
        initViews();
        // 初始化图片加载类
        initImageLoader();
        // 获取传递的数据
        getData();
        // 注册监听器
        setListener();
        initData();

        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                com.aurora.utils.SystemUtils.STATUS_BAR_MODE_WHITE, this);
    }

    @Override
    public void onReq(BaseReq resp) {

    }

    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        switch (resp.errCode) {
        case BaseResp.ErrCode.ERR_OK:
            result = R.string.errcode_success;
            break;
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            result = R.string.errcode_cancel;
            break;
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            result = R.string.errcode_deny;
            break;
        default:
            result = R.string.errcode_unknown;
            break;
        }

        ToastUtil.shortToast(result);

        if (TextUtils.isEmpty(mImageUrl)) {
            finish();
        }
	}

    private void initData() {
        showTools();
        setAdapter();
    }

    private void initImageLoader() {
        options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) 
                .cacheInMemory(true)
                .bitmapConfig(Config.RGB_565).build();
    }

    private void setListener() {
        back.setOnClickListener(this);
        findViewById(R.id.back_ly).setOnClickListener(this);
        
        mSaveToGallery.setOnClickListener(this);
        mShareByWeibo.setOnClickListener(this);
        mSendByWx.setOnClickListener(this);
        mShareByWx.setOnClickListener(this);
        mShareByQQ.setOnClickListener(this);
        mShareByQZone.setOnClickListener(this);
        mShareMore.setOnClickListener(this);
    }

    private void setAdapter() {
        picPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), data);
        vp.setAdapter(picPagerAdapter);
        vp.setCurrentItem(0, false);
    }

    private void getData() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        mImageUrl = url;
        if (!TextUtils.isEmpty(url)) {
            if (!url.startsWith(Globals.FILE_PROTOCOL)) {
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                url = Globals.FILE_PROTOCOL + url;
            }
        }
        data.add(url);
    }

    private void initViews() {
    	topTools = (RelativeLayout)findViewById(R.id.pic_view_top_tools);
        back = (ImageButton) findViewById(R.id.pic_view_go_back);
        vp = (PicViewPager) findViewById(R.id.pic_view_viewpager);

        bottomTools = findViewById(R.id.pic_view_bottom_tools);
        mSaveToGallery = findViewById(R.id.save_to_gallery);
        mShareByWeibo = findViewById(R.id.share_by_weibo);
        mSendByWx = findViewById(R.id.send_by_wx);
        mShareByWx = findViewById(R.id.share_by_wx);
        mShareByQQ = findViewById(R.id.share_by_qq);
        mShareByQZone = findViewById(R.id.share_by_qzone);
        mShareMore = findViewById(R.id.share_more);

        int wxSdkVersion = api.getWXAppSupportAPI();
        if (wxSdkVersion < TIMELINE_SUPPORTED_VERSION) {
            if (wxSdkVersion == 0) {
            	mSendByWx.setVisibility(View.GONE);
            }
            mShareByWx.setVisibility(View.GONE);
        }
        
        if (!isQQInstalled()) {
            mShareByQQ.setVisibility(View.GONE);
        }
        mShareByQZone.setVisibility(View.GONE);
    }
    
    private static boolean isQQInstalled() {
        String versionName = com.tencent.open.utils.SystemUtils.getAppVersionName(NoteApp.getInstance(), "com.tencent.mobileqq");
        if (versionName == null) {
            return false;
        } else {
            Log.d(TAG, "Jim, QQ client version: " + versionName);
        }
        
        return true;
    }

    public void controlToolViews() {
        if (isVisible) {
            topTools.startAnimation(AnimationUtils.loadAnimation(
            		WXEntryActivity.this, R.anim.pic_view_push_top_out));
            bottomTools.startAnimation(AnimationUtils.loadAnimation(
            		WXEntryActivity.this, R.anim.pic_view_push_bottom_out));
            topTools.setVisibility(View.GONE);
            bottomTools.setVisibility(View.GONE);
            isVisible = false;
        } else {
            topTools.startAnimation(AnimationUtils.loadAnimation(
            		WXEntryActivity.this, R.anim.pic_view_push_top_in));
            bottomTools.startAnimation(AnimationUtils.loadAnimation(
            		WXEntryActivity.this, R.anim.pic_view_push_bottom_in));
            topTools.setVisibility(View.VISIBLE);
            bottomTools.setVisibility(View.VISIBLE);
            isVisible = true;
        }
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> data;

        public ImagePagerAdapter(FragmentManager fm , ArrayList<String> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url = null;
            try {
                url = data.get(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return PicturePreviewFragment.newInstance(url);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
        
    }    

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.pic_view_go_back:
        case R.id.back_ly:
            finish();
            break;
        case R.id.save_to_gallery:
            saveToGallery();
            break;
        case R.id.share_by_weibo:
            // 分享到新浪微博
            shareToSinaWeibo();
            break;
        case R.id.send_by_wx:
            shareByWx(true);
            break;
        case R.id.share_by_wx:
            shareByWx(false);
            break;
        case R.id.share_by_qq:
            share2QQ();
            break;
        case R.id.share_by_qzone:
            share2QZone();
            break;
        case R.id.share_more:
            shareMore(this, getString(R.string.share_activity_title), "", "", mImageUrl);
            break;
        }
    }

    private void saveToGallery() {
        ReportCommand command = new ReportCommand(this, ReportUtil.TAG_GALLERY);
        command.updateData();

        if (SDcardManager.checkSDCardAvailable()) {
            File dstFile = Environment.getExternalStorageDirectory();
            if (dstFile == null) {
                ToastUtil.shortToast(getResources().getString(R.string.sdcard_is_not_available));
                return;
            }
            File srcFile = new File(mImageUrl);
            String srcFileName = srcFile.getName();
            String extName = srcFileName.substring(srcFileName.indexOf('.'));
            if (TextUtils.isEmpty(extName)) {
                extName = ".jpg";
            }

            // 先判断一下路径是否存在，如果不存在则尝试创建
            dstFile = new File(dstFile.getAbsolutePath() + "/DCIM/Camera/");
            boolean result = true;
            if (!dstFile.exists()) {
                result = dstFile.mkdirs();
                Log.d(TAG, "Jim, Gallery directory does not exist, mkdir result: " + result);
            }
            if (!result) {
                ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_failed));
            } else {
                dstFile = new File(dstFile.getAbsolutePath(), System.currentTimeMillis() + extName);
                result = FileUtils.copyFile(srcFile, dstFile);
                Log.d(TAG, "Jim, dest image path: " + dstFile.getAbsolutePath() + ", result: " + result);
                if (result) {
                    Uri uri = Uri.fromFile(dstFile);
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(uri);
                    sendBroadcast(intent);
                    ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_success));
                } else {
                    ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_failed));
                }
            }
        } else {
            ToastUtil.shortToast(getResources().getString(R.string.sdcard_is_not_available));
        }
    }
    
    private static void shareMore(Activity activity, String activityTitle,
            String msgTitle, String msgText, String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (TextUtils.isEmpty(imgPath)) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/*");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(Intent.createChooser(intent, activityTitle));
    }
    
    private void share2QQ() {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, mImageUrl);
//        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, /*手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替。*/);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ(this, params, this);
    }
    
    private void share2QZone() {
        Bundle params = new Bundle();
        //分享类型
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "标题");//必填
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "内容");//选填
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, "http://www.iunios.com");//必填
        ArrayList<String> imagePaths = new ArrayList<String>(1);
        imagePaths.add(mImageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imagePaths);
        mTencent.shareToQzone(this, params, this);
    }

    private void shareByWx(boolean sendByWx) {
        if (sendByWx) {
            ReportCommand command = new ReportCommand(this, ReportUtil.TAG_CHATS);
            command.updateData();
        } else {
            ReportCommand command = new ReportCommand(this, ReportUtil.TAG_MOMENTS);
            command.updateData();
        }

        // 先检查一下是否有网络连接
        if (!SystemUtils.isNetworkConnected()) {
            ToastUtil.shortToast(R.string.share_sina_weibo_no_network);
            return;
        }

        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(mImageUrl);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        /*Bitmap bmp = BitmapFactory.decodeFile(mImageUrl);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();*/
        // Bitmap thumbBmp = BitmapUtil.getThumbBitmap(mImageUrl, THUMB_SIZE, THUMB_SIZE);
        Bitmap thumbBmp = null;
        try {
            thumbBmp = BitmapUtil.getThumbBitmap(mImageUrl, THUMB_SIZE, THUMB_SIZE);
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
            thumbBmp = null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError", e);
            thumbBmp = null;
        }
        if (thumbBmp == null) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
            bmp.recycle();
        }
        msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "img" + String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = sendByWx ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }

    private void shareToSinaWeibo() {
        ReportCommand command = new ReportCommand(this, ReportUtil.TAG_WEIBO);
        command.updateData();

        // 先检查一下是否有网络连接
        if (!SystemUtils.isNetworkConnected()) {
            ToastUtil.shortToast(R.string.share_sina_weibo_no_network);
            return;
        }

        // 是否有授权
        SinaAccessToken token = AccessTokenKeeper.readAccessToken(this);
        if (token == null || !token.getToken().isSessionValid()) {
            // 未授权或者授权已过期，需要重新授权
            if (mSsoHandler == null) {
                // 创建微博实例
                mSsoHandler = new SsoHandler(this,
                        new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
            }
            mSsoHandler.authorize(new AuthListener());
        } else {
            startShareSinaWeiboActivity();
        }
    }

    private void startShareSinaWeiboActivity() {
        if (ShareSinaWeiboActivity.sIsWeiboSending) {
            ToastUtil.shortToast(R.string.share_sina_weibo_is_sending);
            return;
        }
        Intent intent = new Intent(this, ShareSinaWeiboActivity.class);
        intent.putExtra(ShareSinaWeiboActivity.KEY_SHARE_IMAGE_URL, mImageUrl);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (mTencent != null) {
            mTencent.releaseResource();
            mTencent = null;
        }
        
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
        
        mTencent.onActivityResult(requestCode, resultCode, data);
    }

    private class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken.isSessionValid()) {
                String nickName = values.getString(Constants.NICK_NAME, "");
                String userName = values.getString(Constants.USER_NAME, "");
                if (TextUtils.isEmpty(nickName) && TextUtils.isEmpty(userName)) {
                    getUserInfo(accessToken);
                    return;
                }
                saveSinaToken(accessToken, nickName, userName);
                startShareSinaWeiboActivity();
            } else {
                ToastUtil.shortToast(R.string.share_sina_weibo_authorize_invalid_token);
                String code = values.getString("code");
                Log.e(TAG, "Jim, sina weibo authorize failed, code: " + code);
            }
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "Jim, sina weibo authorize canceled");
            ToastUtil.shortToast(R.string.share_sina_weibo_authorize_cancel);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ToastUtil.shortToast(R.string.share_sina_weibo_authorize_invalid_token);
            Log.e(TAG, "Jim, sina weibo authorize exception: " + e.getMessage());
        }
    }

    private void saveSinaToken(Oauth2AccessToken token, String nickName, String userName) {
        SinaAccessToken sinaToken = new SinaAccessToken();
        sinaToken.setToken(token);
        sinaToken.setNickName(nickName);
        sinaToken.setUserName(userName);
        AccessTokenKeeper.writeAccessToken(WXEntryActivity.this, sinaToken);
    }

    private void getUserInfo(Oauth2AccessToken token) {
        new GetUserInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, token);
    }

    private class GetUserInfoTask extends AsyncTask<Oauth2AccessToken, Integer, String> {
        private ProgressDialog mPd;
        private Oauth2AccessToken mToken;
        private boolean mHasError = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPd = ProgressDialog.show(WXEntryActivity.this, null,
                    getResources().getString(R.string.share_sina_weibo_get_user_info),
                    true, false);
            mPd.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mPd.isShowing() && !WXEntryActivity.this.isFinishing()) {
                mPd.dismiss();
            }
            Log.d(TAG, "Jim, get user info response: " + result);
            if (mHasError || TextUtils.isEmpty(result)) {
                ToastUtil.shortToast(R.string.share_sina_weibo_get_user_info_failed);
            } else {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    String nickName = jsonResult.optString("screen_name", "");
                    String userName = jsonResult.optString("name", "");
                    saveSinaToken(mToken, nickName, userName);
                    startShareSinaWeiboActivity();
                } catch (JSONException e) {
                    Log.e(TAG, "Jim, get user info, invalid response.", e);
                    ToastUtil.shortToast(R.string.share_sina_weibo_get_user_info_failed);
                }
            }
        }

        @Override
        protected String doInBackground(Oauth2AccessToken... params) {
            Oauth2AccessToken token = params[0];
            mToken = token;
            UsersAPI userAPI = new UsersAPI(token);

            long uid = Long.parseLong(token.getUid());
            try {
                return userAPI.show(uid);
            } catch (Exception e) {
                Log.e(TAG, "Jim, get user info exception.", e);
                mHasError = true;
            }

            return null;
        }
    }

    // IRequestListener begin
    @Override
    public void onComplete(JSONObject arg0) {
        Log.d(TAG, "Jim, onComplete, arg0: " + arg0);
    }

    @Override
    public void onConnectTimeoutException(ConnectTimeoutException arg0) {
        Log.d(TAG, "Jim, onConnectTimeoutException, arg0: " + arg0);
    }

    @Override
    public void onHttpStatusException(HttpStatusException arg0) {
        Log.d(TAG, "Jim, onHttpStatusException, arg0: " + arg0);
    }

    @Override
    public void onIOException(IOException arg0) {
        Log.d(TAG, "Jim, onIOException, arg0: " + arg0);
    }

    @Override
    public void onJSONException(JSONException arg0) {
        Log.d(TAG, "Jim, onJSONException, arg0: " + arg0);
    }

    @Override
    public void onMalformedURLException(MalformedURLException arg0) {
        Log.d(TAG, "Jim, onMalformedURLException, arg0: " + arg0);
    }

    @Override
    public void onNetworkUnavailableException(NetworkUnavailableException arg0) {
        Log.d(TAG, "Jim, onNetworkUnavailableException, arg0: " + arg0);
    }

    @Override
    public void onSocketTimeoutException(SocketTimeoutException arg0) {
        Log.d(TAG, "Jim, onSocketTimeoutException, arg0: " + arg0);
    }

    @Override
    public void onUnknowException(Exception arg0) {
        Log.d(TAG, "Jim, onUnknowException, arg0: " + arg0);
    }
    // IRequestListener end

    // IUiListener begin
    @Override
    public void onCancel() {
        Log.d(TAG, "Jim, onCancel enter");
    }

    @Override
    public void onComplete(Object arg0) {
        Log.d(TAG, "Jim, onComplete, arg0: " + arg0);
    }

    @Override
    public void onError(UiError arg0) {
        Log.d(TAG, "Jim, onError, arg0: " + arg0);
    }
    // IUiListener end
}
