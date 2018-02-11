package com.aurora.note.activity.picbrowser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.aurora.note.R;
import com.aurora.note.activity.fragment.PicViewFragment;
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
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * 生成图片预览界面
 * @author JimXia
 * 2014-5-19 上午10:50:21
 */
public class PicturePreviewActivity extends FragmentActivity implements OnClickListener {

    private static final String TAG = "PicturePreviewActivity";

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 150;

    private IWXAPI api;

    private String mImageUrl;
    private ArrayList<String> data = new ArrayList<String>();

    private ImagePagerAdapter picPagerAdapter;

    private RelativeLayout topTools;
    private ImageButton back;

    private PicViewPager vp;

    private LinearLayout bottomTools;
    private ImageView mSaveToGalleryIv;
    private ImageView mShareByWeiboTv;
    private ImageView mSendByWxIV;
    private ImageView mShareByWxIv;

    // 图片加载工具
    public ImageLoader imageLoader = ImageLoader.getInstance();
    public DisplayImageOptions options;

    // 控件隐藏状态
    private boolean isVisible = true;

    private SsoHandler mSsoHandler;

    private void showTools() {
        topTools.startAnimation(AnimationUtils.loadAnimation(
                PicturePreviewActivity.this, R.anim.pic_view_push_top_in));
        bottomTools.startAnimation(AnimationUtils.loadAnimation(
                PicturePreviewActivity.this, R.anim.pic_view_push_bottom_in));
        topTools.setVisibility(View.VISIBLE);
        bottomTools.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, Globals.APP_ID);

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
        topTools.setOnClickListener(this);
        mSaveToGalleryIv.setOnClickListener(this);
        mShareByWeiboTv.setOnClickListener(this);
        mSendByWxIV.setOnClickListener(this);
        mShareByWxIv.setOnClickListener(this);
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

        bottomTools = (LinearLayout) findViewById(R.id.pic_view_bottom_tools);
        mSaveToGalleryIv = (ImageView) findViewById(R.id.save_to_gallery);
        mShareByWeiboTv = (ImageView) findViewById(R.id.share_by_weibo);
        mSendByWxIV = (ImageView) findViewById(R.id.send_by_wx);
        mShareByWxIv = (ImageView) findViewById(R.id.share_by_wx);

        int wxSdkVersion = api.getWXAppSupportAPI();
        if (wxSdkVersion < TIMELINE_SUPPORTED_VERSION) {
            if (wxSdkVersion == 0) {
            	mSendByWxIV.setVisibility(View.GONE);
            }
            mShareByWxIv.setVisibility(View.GONE);
        }
    }

    public void controlToolViews() {
        if (isVisible) {
            topTools.startAnimation(AnimationUtils.loadAnimation(
                    PicturePreviewActivity.this, R.anim.pic_view_push_top_out));
            bottomTools.startAnimation(AnimationUtils.loadAnimation(
                    PicturePreviewActivity.this, R.anim.pic_view_push_bottom_out));
            topTools.setVisibility(View.GONE);
            bottomTools.setVisibility(View.GONE);
            isVisible = false;
        } else {
            topTools.startAnimation(AnimationUtils.loadAnimation(
                    PicturePreviewActivity.this, R.anim.pic_view_push_top_in));
            bottomTools.startAnimation(AnimationUtils.loadAnimation(
                    PicturePreviewActivity.this, R.anim.pic_view_push_bottom_in));
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
            return PicViewFragment.newInstance(url);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);;
        }
        
    }    

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.pic_view_go_back:
        case R.id.pic_view_top_tools:
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
        }
    }

    /*private void saveToGallery() {
        try {
            String uri = MediaStore.Images.Media.insertImage(getContentResolver(), mImageUrl, generatePhotoName(), "");
            Log.d(TAG, "Jim, new uri: " + uri);
            
            Uri strUri = Uri.parse(uri);
            String path = getFilePathByContentResolver(this, strUri);
            if (path != null) {
                Log.d(TAG, "Jim, image path: " + path);
                strUri = Uri.fromFile(new File(path));
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(strUri);
                sendBroadcast(intent);
            }            
            ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_success));
            return;
        } catch (FileNotFoundException e) {
            FileLog.e(TAG, e.getMessage());
        }
        ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_failed));
    }*/

    private void saveToGallery() {
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

    private void shareByWx(boolean sendByWx) {
        // 先检查一下是否有网络连接
        if (!SystemUtils.isNetworkConnected()) {
            ToastUtil.shortToast(R.string.share_sina_weibo_no_network);
            return;
        }

        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(mImageUrl);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap bmp = BitmapFactory.decodeFile(mImageUrl);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "img" + String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = sendByWx ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }

    /*private static String getFilePathByContentResolver(Context context, Uri uri) {
        if (null == uri) {
            return null;
        }
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);
        String filePath  = null;
        if (null == c) {
            return null;
        }
        try {  
            if ((c.getCount() != 1) || !c.moveToFirst()) {
            } else {
                filePath = c.getString(c.getColumnIndex(MediaColumns.DATA));
            }
        } finally {
            c.close();
        }

        return filePath;
    }

    private String generatePhotoName() {
        return "Note_" + System.currentTimeMillis();
    }

    private void share() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mImageUrl)));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_title)));
    }*/

    private void shareToSinaWeibo() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
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
        AccessTokenKeeper.writeAccessToken(PicturePreviewActivity.this, sinaToken);
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
            mPd = ProgressDialog.show(PicturePreviewActivity.this, null,
                    getResources().getString(R.string.share_sina_weibo_get_user_info),
                    true, false);
            mPd.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mPd.isShowing() && !PicturePreviewActivity.this.isFinishing()) {
                mPd.dismiss();
            }
            Log.d(TAG, "Jim, get user info response: " + result);
            if (mHasError || TextUtils.isEmpty(result)) {
                ToastUtil.shortToast(R.string.share_sina_weibo_get_user_info_failed);
            } else {
                /*
                 *{"id":1789227017,"idstr":"1789227017","class":1,"screen_name":"Jim_Xia-2012",
                 *"name":"Jim_Xia-2012","province":"44","city":"3","location":"广东 深圳","description":"",
                 *"url":"","profile_image_url":"http://tp2.sinaimg.cn/1789227017/50/0/1","profile_url":"u/1789227017",
                 *"domain":"","weihao":"","gender":"m","followers_count":12,"friends_count":123,"statuses_count":17,
                 *"favourites_count":0,"created_at":"Thu Dec 20 17:33:51 +0800 2012","following":false,
                 *"allow_all_act_msg":false,"geo_enabled":true,"verified":false,"verified_type":-1,"remark":"",
                 *"status":{"created_at":"Wed Jun 11 11:14:08 +0800 2014","id":3720234345441537,"mid":"3720234345441537",
                 *"idstr":"3720234345441537","text":"关于@手机中国 的投票【下面的系统中，你最喜欢的是哪个？】，挺赞的！先转给你们，大家帮我投一票吧：http://t.cn/RvXj6QB  ",
                 *"source":"<a href=\"http://app.weibo.com/t/feed/3PAXPy\" rel=\"nofollow\">投票</a>","favorited":false,
                 *"truncated":false,"in_reply_to_status_id":"","in_reply_to_user_id":"","in_reply_to_screen_name":"","pic_urls":[],
                 *"geo":null,"annotations":[{"id":"2660654","title":"下面的系统中，你最喜欢...","name":"下面的系统中，你最喜欢的是哪个？",
                 *"skey":"","appid":"53","url":"http://vote.weibo.com/vid=2660654"}],"reposts_count":0,
                 *"comments_count":0,"attitudes_count":0,"mlevel":0,"visible":{"type":0,"list_id":0}},"ptype":0,
                 *"allow_all_comment":true,"avatar_large":"http://tp2.sinaimg.cn/1789227017/180/0/1",
                 *"avatar_hd":"http://tp2.sinaimg.cn/1789227017/180/0/1","verified_reason":"","verified_trade":"",
                 *"verified_reason_url":"","verified_source":"","verified_source_url":"","follow_me":false,"online_status":0,
                 *"bi_followers_count":3,"lang":"zh-cn","star":0,"mbtype":2,"mbrank":1,"block_word":0,"block_app":0,"worldcup_guess":0}
                 * 
                 */
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

}