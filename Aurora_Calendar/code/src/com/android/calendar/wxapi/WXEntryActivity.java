package com.android.calendar.wxapi;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.android.calendar.CalendarApp;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.aurora.calendar.util.BitmapUtil;
import com.aurora.calendar.util.Globals;
import com.aurora.calendar.util.SDcardManager;
import com.aurora.calendar.util.SystemUtils;
import com.aurora.calendar.util.TimeUtils;
import com.aurora.calendar.util.ToastUtil;
import com.aurora.commemoration.activity.AddOrEditReminderActivity;
import com.aurora.commemoration.alarm.CalendarAlarmReceiver;
import com.aurora.commemoration.db.RememberDayDao;
import com.aurora.commemoration.model.RememberDayInfo;
import com.aurora.commemoration.share.PictureViewActivity;
import com.aurora.commemoration.share.RememberDayFragment;
import com.aurora.commemoration.sina.weibo.AccessTokenKeeper;
import com.aurora.commemoration.sina.weibo.Constants;
import com.aurora.commemoration.sina.weibo.ShareSinaWeiboActivity;
import com.aurora.commemoration.sina.weibo.SinaAccessToken;
import com.aurora.commemoration.ui.RMViewPager;
import com.aurora.commemoration.widget.InfoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraSwitch;

public class WXEntryActivity extends FragmentActivity implements OnClickListener, IWXAPIEventHandler, IUiListener, IRequestListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "WXEntryActivity";

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 128;
    private Context mContext;
    private RememberDayInfo newInfo;
    private IWXAPI api;

    private String mImageUrl = null;
    private List<String> data = new ArrayList<String>();

    private MainAdapter picPagerAdapter;

    private RelativeLayout rootLayout;
    private RelativeLayout topToolDetail;
    private RelativeLayout topToolEdit;
    private RelativeLayout topToolShare;
    private ImageButton back;
    private ImageButton back1;
    private RMViewPager vp;
    private InfoView content;
    private View bottomToolDetail;
    private View bottomToolShare;
    private View bottonToolEdit;
    private View mShare;
    private View mEditComfirm;
    private View mEditCancle;
    private View mSaveToGallery;
    private View mShareByWeibo;
    private View mSendByWx;
    private View mShareByWx;
    private View mShareByQQ;
    private View mShareByQZone;
    private View mShareMore;
    private AuroraSwitch mScheduleSwith;
    private View mAddReminder;
    private TextView mAddReminderText;

    private View m_edit;
    private View m_delete;

    private final int DIS_DETAIL_FLAG = 0;
    private final int DIS_SHARE_FLAG = 1;
    private final int DIS_EDIT_FLAG = 2;
    // 0 -- detail 1- share
    private int dis_type = DIS_DETAIL_FLAG;
    public final static int NONE_DOWN_FLAG = 0;
    private final int SHARE_DOWN_FLAG = 1;
    private final int EDIT_DOWN_FLAG = 2;
    private final int DETAIL_DOWN_FLAG = 3;
    public static boolean NEW_FLAG = false;
    private AnimatorSet mAnimatorSet;

    // 临时编辑数据
    private int tmpScheduleFlag = 0;
    private String tmpPicPath;
    private Long tmpReminderData = 0L;

    // 图片加载工具
    public ImageLoader imageLoader = ImageLoader.getInstance();
    public DisplayImageOptions options;

    // 控件隐藏状态
    private boolean isVisible = false;

    private SsoHandler mSsoHandler;

    // 腾讯QQ分享相关
    private Tencent mTencent;

    private int[] mImgIds;
    private LinearLayout mGallery;
    private LayoutInflater mInflater;
    private int galleryIndex = 0;
    private int tmpIndex = 0;
    private int curGalleryIndex = 0;
    private ImageView bg;
    private int[] mImgBgs;
    private int mIndex = 0;
    private int mIsnotifi = 0;
    private List<ImageView> selectIVS = new ArrayList<ImageView>();
    private List<RememberDayInfo> allDayInfos = new ArrayList<RememberDayInfo>();
    private static final String IMAGE_TYPE = "image/*";

    private static final int REQUEST_CODE_ADD_REMINDER = 0;
    private static final int REQUEST_CODE_DESKTOP_WALLPAPER = 1;
    private static final int REQUEST_CODE_SET_WALLPAPER = 2;

    private Bitmap tmpBitmap = null;
    private Uri tmpUri = null;
    private int DEFAULT_PIC_NUM;
    private int warn_type = AddOrEditReminderActivity.MODE_NO_REMINDER;
    private long ANIMATION_TIME = 300L;
    private final int GALLERY_NUM = 7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.picture_preview_activity);
        getData();
        // 初始化控件
        initData();
        initViews();
        // 初始化图片加载类
        initImageLoader();
        // 获取传递的数据
        // 注册监听器
        setListener();
        setAdapter();
        initDisplayMode();
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);

        getData();
        if (mIsnotifi == 1) {
            // 初始化控件
            //initData();
            //setAdapter();
            hideImm(mEditCancle);
            if (dis_type == DIS_EDIT_FLAG) {
                editCancle();
            }
            //initDisplayMode();
            mIsnotifi = 0;
        } else {
            api.handleIntent(intent, this);
        }
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
        api = WXAPIFactory.createWXAPI(this, Globals.APP_ID);
        mContext = this;
        mTencent = Tencent.createInstance(Globals.QQ_APP_ID, getApplicationContext());
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.a_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.a_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.a_bg));
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.b_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.b_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.b_bg));
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.c_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.c_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.c_bg));
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.d_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.d_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.d_bg));
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.e_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.e_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.e_bg));
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.f_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.f_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.f_bg));
        data.add(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.g_bg)
                + '/' + getResources().getResourceTypeName(R.drawable.g_bg)
                + '/' + getResources().getResourceEntryName(R.drawable.g_bg));

        RememberDayDao dao = new RememberDayDao(mContext);
        allDayInfos = dao.getRememberDayList();
        DEFAULT_PIC_NUM = allDayInfos.size();

        if (NEW_FLAG) {
            newInfo = new RememberDayInfo();
            newInfo.setMillTime(new Date().getTime());
            newInfo.setReminderData(0L);
            newInfo.setTitle("");
            newInfo.setPicPath(data.get(0));
            tmpPicPath = data.get(0);
            newInfo.setDay(TimeUtils.getStringDateShort());
            tmpReminderData = 0L;
        }
        mAnimatorSet = new AnimatorSet();
        mImgIds = new int[]{R.drawable.a, R.drawable.b,
                R.drawable.c, R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g};
        mImgBgs = new int[]{R.drawable.a_bg, R.drawable.b_bg,
                R.drawable.c_bg, R.drawable.d_bg, R.drawable.e_bg, R.drawable.f_bg,
                R.drawable.g_bg};
        mInflater = LayoutInflater.from(this);
        dao.closeDatabase();
    }

    private void initImageLoader() {
        options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .cacheInMemory(true)
                .bitmapConfig(Config.RGB_565).build();
    }

    private void setListener() {
        back.setOnClickListener(this);
        back1.setOnClickListener(this);
        findViewById(R.id.back_ly).setOnClickListener(this);
        findViewById(R.id.back_ly1).setOnClickListener(this);
        mSaveToGallery.setOnClickListener(this);
        m_edit.setOnClickListener(this);
        m_delete.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mEditCancle.setOnClickListener(this);
        mEditComfirm.setOnClickListener(this);
        mShareByWeibo.setOnClickListener(this);
        mSendByWx.setOnClickListener(this);
        mShareByWx.setOnClickListener(this);
        mShareByQQ.setOnClickListener(this);
        mShareByQZone.setOnClickListener(this);
        mShareMore.setOnClickListener(this);
        mAddReminder.setOnClickListener(this);
        mScheduleSwith.setOnCheckedChangeListener(this);
    }

    private void setAdapter() {
//        picPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), allDayInfos);
        picPagerAdapter = new MainAdapter();
        vp.setAdapter(picPagerAdapter);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //三种状态，0为空闲，1为滑动，2为加载完毕
            @Override
            public void onPageScrollStateChanged(int arg0) {
                //arg0 ==1的时候表示正在滑动，arg0==2的时候表示滑动完毕了，arg0==0的时候表示什么都没做，就是停在那。
            }

            //界面1滑动到界面2，在界面1滑动前调用
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //  表示在前一个页面滑动到后一个页面的时候，在前一个页面滑动前调用的方法。
            }

            //从界面1到界面2，2页加载后调用...
            @Override
            public void onPageSelected(int position) {
                //arg0是表示你当前选中的页面，这事件是在你页面跳转完毕的时候调用的。
                content.initData(false, allDayInfos.get(position));
                mIndex = position;
            }
        });
        //vp.setPageMargin(30);
//        try {
//            Field mField = ViewPager.class.getDeclaredField("mScroller");
//            mField.setAccessible(true);
//            FixedSpeedScroller mScroller =
//                    new FixedSpeedScroller(vp.getContext(), new AccelerateInterpolator());
//            mField.set(vp, mScroller);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void getData() {
        Intent intent = getIntent();
        NEW_FLAG = intent.getBooleanExtra("new", false);
        if (!NEW_FLAG) {
            mIndex = intent.getIntExtra("index", 0);
            if (mIndex == -1) {
                long millTime = intent.getLongExtra("id", System.currentTimeMillis());
                mIndex = getNewIndex(millTime);
            }
            mIsnotifi = intent.getIntExtra("isNotifi", 0);
        }
    }

    private void startFileManagerActivity() {
        try {
            Intent intent = new Intent();
            intent.setAction("com.aurora.filemanager.SINGLE_GET_CONTENT");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setType(IMAGE_TYPE);
            startActivityForResult(intent, REQUEST_CODE_DESKTOP_WALLPAPER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        content = (InfoView) findViewById(R.id.content);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        topToolDetail = (RelativeLayout) findViewById(R.id.pic_view_top_detail);
        topToolShare = (RelativeLayout) findViewById(R.id.pic_view_top_share);
        topToolEdit = (RelativeLayout) findViewById(R.id.pic_view_top_edit);
        back = (ImageButton) findViewById(R.id.pic_view_detail_back);
        back1 = (ImageButton) findViewById(R.id.pic_view_share_back);
        vp = (RMViewPager) findViewById(R.id.pic_view_viewpager);
        bg = (ImageView) findViewById(R.id.remember_bg);
        bottomToolDetail = findViewById(R.id.pic_view_bottom_detail);
        bottomToolShare = findViewById(R.id.pic_view_bottom_share);
        bottonToolEdit = findViewById(R.id.pic_view_bottom_edit);
        mSaveToGallery = findViewById(R.id.save_to_gallery);
        m_delete = findViewById(R.id.delete);
        m_edit = findViewById(R.id.edit);
        mShare = findViewById(R.id.share);
        mEditComfirm = findViewById(R.id.edit_true);
        mEditCancle = findViewById(R.id.edit_false);
        mShareByWeibo = findViewById(R.id.share_by_weibo);
        mSendByWx = findViewById(R.id.send_by_wx);
        mShareByWx = findViewById(R.id.share_by_wx);
        mShareByQQ = findViewById(R.id.share_by_qq);
        mShareByQZone = findViewById(R.id.share_by_qzone);
        mShareMore = findViewById(R.id.share_more);
        mScheduleSwith = (AuroraSwitch) findViewById(R.id.edit_schedule_choose);
        mAddReminder = findViewById(R.id.edit_add_reminder);
        mAddReminderText = (TextView) findViewById(R.id.add_reminder_prompt);
        mGallery = (LinearLayout) findViewById(R.id.id_gallery);

        ImageView customAddd = new ImageView(mContext);
        customAddd.setImageDrawable(getResources().getDrawable(R.drawable.aurora_pic_custom_add));
        customAddd.setPadding(Utils.dpToPx(getResources(), 5), 0, 0, 0);
        mGallery.addView(customAddd, 0);
        customAddd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startFileManagerActivity();
            }
        });

        for (int i = 0; i < mImgIds.length; i++) {
            final int index = i;
            View view;
            if (index == GALLERY_NUM - 1) {
                view = mInflater.inflate(R.layout.gallery_item_last,
                        mGallery, false);
            } else {
                view = mInflater.inflate(R.layout.gallery_item,
                        mGallery, false);
            }
            view.setTag(new Integer(index + 1));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer index = (Integer) view.getTag();
                    curGalleryIndex = index - 1;
                    if (galleryIndex != curGalleryIndex) {
                        if (galleryIndex != -1 && galleryIndex < GALLERY_NUM) {
                            selectIVS.get(galleryIndex).setVisibility(View.INVISIBLE);
                        }
                        selectIVS.get(curGalleryIndex).setVisibility(View.VISIBLE);
                        bg.setImageResource(mImgBgs[curGalleryIndex]);
                        galleryIndex = curGalleryIndex;
                        tmpPicPath = data.get(curGalleryIndex);
                    }
                }
            });
            ImageView img = (ImageView) view
                    .findViewById(R.id.img_item);
            img.setImageResource(mImgIds[i]);

            selectIVS.add((ImageView) view.findViewById(R.id.gallery_index));
            mGallery.addView(view, i + 1);
        }

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
        mShareByWeibo.setVisibility(View.GONE);
    }

    private static boolean isQQInstalled() {
        String versionName = com.tencent.open.utils.SystemUtils.getAppVersionName(CalendarApp.getInstance(), "com.tencent.mobileqq");
        if (versionName == null) {
            return false;
        } else {
            Log.d(TAG, "Jim, QQ client version: " + versionName);
        }

        return true;
    }

    public void controlTopViews(int downFlag) {
        mAnimatorSet = new AnimatorSet();
        if (dis_type == DIS_DETAIL_FLAG) {
            if (isVisible) {
                ObjectAnimator translateOut = ObjectAnimator.ofFloat(topToolDetail, "TranslationY", 0.0F, -topToolDetail.getHeight());
                translateOut.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaOut = ObjectAnimator.ofFloat(topToolDetail, "Alpha", 1.0F, 0.0F);
                alphaOut.setDuration(ANIMATION_TIME);
                ObjectAnimator translateOut1 = ObjectAnimator.ofFloat(bottomToolDetail, "TranslationY", 0.0F, bottomToolDetail.getHeight());
                translateOut1.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaOut1 = ObjectAnimator.ofFloat(bottomToolDetail, "Alpha", 1.0F, 0.0F);
                alphaOut1.setDuration(ANIMATION_TIME);

                switch (downFlag) {
                    case NONE_DOWN_FLAG:
                        mAnimatorSet.play(translateOut).with(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        mAnimatorSet.start();
                        isVisible = false;
                        break;
                    case SHARE_DOWN_FLAG:
                        ObjectAnimator shareTranslateIn = ObjectAnimator.ofFloat(topToolShare, "TranslationY", -topToolShare.getHeight(), 0.0F);
                        shareTranslateIn.setDuration(ANIMATION_TIME);
                        ObjectAnimator shareAlphaIn = ObjectAnimator.ofFloat(topToolShare, "Alpha", 0.0F, 1.0F);
                        shareAlphaIn.setDuration(ANIMATION_TIME);
                        ObjectAnimator shareTranslateIn1 = ObjectAnimator.ofFloat(bottomToolShare, "TranslationY", bottomToolShare.getHeight(), 0.0F);
                        shareTranslateIn1.setDuration(ANIMATION_TIME);
                        ObjectAnimator shareAlphaIn1 = ObjectAnimator.ofFloat(bottomToolShare, "Alpha", 0.0F, 1.0F);
                        shareAlphaIn1.setDuration(ANIMATION_TIME);
                        mAnimatorSet.play(translateOut).with(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        translateOut.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                getCurBitmap();
                                topToolShare.setVisibility(View.VISIBLE);
                                bottomToolShare.setVisibility(View.VISIBLE);
                                dis_type = DIS_SHARE_FLAG;
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        mAnimatorSet.play(shareTranslateIn).with(shareAlphaIn)
                                .with(shareTranslateIn1).with(shareAlphaIn1)
                                .after(translateOut);
                        mAnimatorSet.start();
                        break;
                    case EDIT_DOWN_FLAG:
                        ObjectAnimator editAlphaIn = ObjectAnimator.ofFloat(topToolEdit, "Alpha", 0.0F, 1.0F);
                        editAlphaIn.setDuration(ANIMATION_TIME);
                        topToolEdit.setAlpha(0);
                        topToolEdit.setVisibility(View.VISIBLE);
                        ObjectAnimator editTranslateIn1 = ObjectAnimator.ofFloat(bottonToolEdit, "TranslationY", bottonToolEdit.getHeight(), 0.0F);
                        editTranslateIn1.setDuration(ANIMATION_TIME);
                        ObjectAnimator editAlphaIn1 = ObjectAnimator.ofFloat(bottonToolEdit, "Alpha", 0.0F, 1.0F);
                        editAlphaIn1.setDuration(ANIMATION_TIME);
                        mAnimatorSet.play(translateOut).with(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        translateOut.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                topToolEdit.setVisibility(View.VISIBLE);
                                bottonToolEdit.setVisibility(View.VISIBLE);
                                //topToolDetail.setVisibility(View.GONE);
                                //bottomToolDetail.setVisibility(View.GONE);
                                dis_type = DIS_EDIT_FLAG;
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        mAnimatorSet.play(editAlphaIn)
                                .with(editTranslateIn1).with(editAlphaIn1)
                                .after(translateOut);
                        mAnimatorSet.start();
                        break;
                }
            } else {
                ObjectAnimator translateIn = ObjectAnimator.ofFloat(topToolDetail, "TranslationY", -topToolDetail.getHeight(), 0.0F);
                translateIn.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaIn = ObjectAnimator.ofFloat(topToolDetail, "Alpha", 0.0F, 1.0F);
                alphaIn.setDuration(ANIMATION_TIME);
                ObjectAnimator translateIn1 = ObjectAnimator.ofFloat(bottomToolDetail, "TranslationY", bottomToolDetail.getHeight(), 0.0F);
                translateIn1.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaIn1 = ObjectAnimator.ofFloat(bottomToolDetail, "Alpha", 0.0F, 1.0F);
                alphaIn1.setDuration(ANIMATION_TIME);
                topToolDetail.setVisibility(View.VISIBLE);
                bottomToolDetail.setVisibility(View.VISIBLE);
                mAnimatorSet.play(translateIn).with(alphaIn)
                        .with(translateIn1).with(alphaIn1);
                mAnimatorSet.start();
                isVisible = true;
            }
        } else if (dis_type == DIS_SHARE_FLAG) {
            if (isVisible) {
                ObjectAnimator translateOut = ObjectAnimator.ofFloat(topToolShare, "TranslationY", 0.0F, -topToolShare.getHeight());
                translateOut.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaOut = ObjectAnimator.ofFloat(topToolShare, "Alpha", 1.0F, 0.0F);
                alphaOut.setDuration(ANIMATION_TIME);
                ObjectAnimator translateOut1 = ObjectAnimator.ofFloat(bottomToolShare, "TranslationY", 0.0F, bottomToolShare.getHeight());
                translateOut1.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaOut1 = ObjectAnimator.ofFloat(bottomToolShare, "Alpha", 1.0F, 0.0F);
                alphaOut1.setDuration(ANIMATION_TIME);

                switch (downFlag) {
                    case NONE_DOWN_FLAG:
                        mAnimatorSet.play(translateOut).with(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        mAnimatorSet.start();
                        isVisible = false;
                        break;
                    case DETAIL_DOWN_FLAG:
                        ObjectAnimator detailTranslateIn = ObjectAnimator.ofFloat(topToolDetail, "TranslationY", -topToolDetail.getHeight(), 0.0F);
                        detailTranslateIn.setDuration(ANIMATION_TIME);
                        ObjectAnimator detailAlphaIn = ObjectAnimator.ofFloat(topToolDetail, "Alpha", 0.0F, 1.0F);
                        detailAlphaIn.setDuration(ANIMATION_TIME);
                        ObjectAnimator detailTranslateIn1 = ObjectAnimator.ofFloat(bottomToolDetail, "TranslationY", bottomToolDetail.getHeight(), 0.0F);
                        detailTranslateIn1.setDuration(ANIMATION_TIME);
                        ObjectAnimator detailAlphaIn1 = ObjectAnimator.ofFloat(bottomToolDetail, "Alpha", 0.0F, 1.0F);
                        detailAlphaIn1.setDuration(ANIMATION_TIME);
                        translateOut.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                topToolDetail.setVisibility(View.VISIBLE);
                                bottomToolDetail.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        mAnimatorSet.play(translateOut).with(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        mAnimatorSet.play(detailTranslateIn).with(detailAlphaIn)
                                .with(detailTranslateIn1).with(detailAlphaIn1)
                                .after(translateOut);
                        mAnimatorSet.start();
                        dis_type = DIS_DETAIL_FLAG;
                        break;
                }
            } else {
                ObjectAnimator translateIn = ObjectAnimator.ofFloat(topToolShare, "TranslationY", -topToolShare.getHeight(), 0.0F);
                translateIn.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaIn = ObjectAnimator.ofFloat(topToolShare, "Alpha", 0.0F, 1.0F);
                alphaIn.setDuration(ANIMATION_TIME);
                ObjectAnimator translateIn1 = ObjectAnimator.ofFloat(bottomToolShare, "TranslationY", bottomToolShare.getHeight(), 0.0F);
                translateIn1.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaIn1 = ObjectAnimator.ofFloat(bottomToolShare, "Alpha", 0.0F, 1.0F);
                alphaIn1.setDuration(ANIMATION_TIME);
                mAnimatorSet.play(translateIn).with(alphaIn)
                        .with(translateIn1).with(alphaIn1);
                mAnimatorSet.start();
                isVisible = true;
            }
        } else if (dis_type == DIS_EDIT_FLAG) {
            if (isVisible) {
                ObjectAnimator alphaOut = ObjectAnimator.ofFloat(topToolEdit, "Alpha", 1.0F, 0.0F);
                alphaOut.setDuration(ANIMATION_TIME);
                alphaOut.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        topToolEdit.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                ObjectAnimator translateOut1 = ObjectAnimator.ofFloat(bottonToolEdit, "TranslationY", 0.0F, bottonToolEdit.getHeight());
                translateOut1.setDuration(ANIMATION_TIME);
                ObjectAnimator alphaOut1 = ObjectAnimator.ofFloat(bottonToolEdit, "Alpha", 1.0F, 0.0F);
                alphaOut1.setDuration(ANIMATION_TIME);

                switch (downFlag) {
                    case NONE_DOWN_FLAG:
                        mAnimatorSet.play(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        mAnimatorSet.start();
                        isVisible = false;
                        break;
                    case DETAIL_DOWN_FLAG:
                        ObjectAnimator detailTranslateIn = ObjectAnimator.ofFloat(topToolDetail, "TranslationY", -topToolDetail.getHeight(), 0.0F);
                        detailTranslateIn.setDuration(ANIMATION_TIME);
                        ObjectAnimator detailAlphaIn = ObjectAnimator.ofFloat(topToolDetail, "Alpha", 0.0F, 1.0F);
                        detailAlphaIn.setDuration(ANIMATION_TIME);
                        ObjectAnimator detailTranslateIn1 = ObjectAnimator.ofFloat(bottomToolDetail, "TranslationY", bottomToolDetail.getHeight(), 0.0F);
                        detailTranslateIn1.setDuration(ANIMATION_TIME);
                        ObjectAnimator detailAlphaIn1 = ObjectAnimator.ofFloat(bottomToolDetail, "Alpha", 0.0F, 1.0F);
                        detailAlphaIn1.setDuration(ANIMATION_TIME);
                        alphaOut.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                topToolDetail.setVisibility(View.VISIBLE);
                                bottomToolDetail.setVisibility(View.VISIBLE);
                                dis_type = DIS_DETAIL_FLAG;
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });
                        mAnimatorSet.play(alphaOut)
                                .with(translateOut1).with(alphaOut1);
                        mAnimatorSet.play(detailTranslateIn).with(detailAlphaIn)
                                .with(detailAlphaIn1).with(detailTranslateIn1)
                                .after(alphaOut);
                        mAnimatorSet.start();
                        break;
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            tmpScheduleFlag = 1;
        } else {
            tmpScheduleFlag = 0;
        }
    }

    private class MainAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView iv = new ImageView(WXEntryActivity.this);
            String ivPath = allDayInfos.get(position).getPicPath();
            if (ivPath.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                iv.setImageURI(Uri.parse(ivPath));
            } else {
                String url = ivPath.substring(7);
                BitmapFactory ft = new BitmapFactory();
                Bitmap bt = ft.decodeFile(url);
                iv.setImageBitmap(bt);
            }
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    controlTopViews(WXEntryActivity.NONE_DOWN_FLAG);
                }
            });
            container.addView(iv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            vp.setObjectForPosition(iv, position);
            return iv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object obj) {
            //super.destroyItem(container, position, obj);
            container.removeView(vp.findViewFromObject(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return DEFAULT_PIC_NUM;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        public void upgradeItem(RememberDayInfo info) {
            RememberDayDao dao = new RememberDayDao(WXEntryActivity.this);
            dao.updateRememberDay(info);
            allDayInfos = dao.getRememberDayList();
            DEFAULT_PIC_NUM = allDayInfos.size();
            dao.closeDatabase();

            notifyDataSetChanged();
            vp.setCurrentItem(getNewIndex(info.getMillTime()), false);
        }

        public void addItem(RememberDayInfo info) {
            RememberDayDao dao = new RememberDayDao(WXEntryActivity.this);
            Long id = dao.insert(info);
            info.setId(id.intValue());
            NEW_FLAG = false;

            allDayInfos = dao.getRememberDayList();
            DEFAULT_PIC_NUM = allDayInfos.size();
            dao.closeDatabase();

            notifyDataSetChanged();
            vp.setCurrentItem(getNewIndex(info.getMillTime()), false);
        }

        public void removeItem() {
            RememberDayDao dao = new RememberDayDao(mContext);
            allDayInfos = dao.getRememberDayList();
            DEFAULT_PIC_NUM = allDayInfos.size();
            dao.closeDatabase();

            notifyDataSetChanged();
            if (DEFAULT_PIC_NUM == 0) {
                finish();
            } else {
                vp.setCurrentItem(mIndex, false);
            }
        }
    }

    private int getNewIndex(long millTime) {
        int index = 0;
        for (int i = 0; i < DEFAULT_PIC_NUM; i++) {
            if (allDayInfos.get(i).getMillTime() == millTime) {
                index = i;
                break;
            }
        }
        return index;
    }

    public class FixedSpeedScroller extends Scroller {
        private int mDuration = 150;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public void setmDuration(int time) {
            mDuration = time;
        }

        public int getmDuration() {
            return mDuration;
        }

    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        private List<RememberDayInfo> data;
        private ArrayList<RememberDayFragment> fragements = new ArrayList<RememberDayFragment>();

        public ImagePagerAdapter(FragmentManager fm, List<RememberDayInfo> data) {
            super(fm);
            this.data = data;
            for (int i = 0; i < data.size(); i++) {
                String url = null;
                try {
                    url = data.get(i).getPicPath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RememberDayFragment fragment = new RememberDayFragment(url, allDayInfos.get(i));
                fragements.add(fragment);
                //vp.setObjectForPosition(fragment, i);
            }
        }

        public void addFragment(String url) {
            if (fragements.size() == DEFAULT_PIC_NUM) {
                //fragements.add(RememberDayFragment.newInstance(url, allDayInfos.get(i)));
            } else {
                //fragements.add(DEFAULT_PIC_NUM, RememberDayFragment.newInstance(url));
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragements.get(position);
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
            case R.id.back_ly:
            case R.id.pic_view_share_back:
                controlTopViews(DETAIL_DOWN_FLAG);
                endShare();
                break;
            case R.id.back_ly1:
            case R.id.pic_view_detail_back:
                finish();
                break;
            case R.id.share:
                controlTopViews(SHARE_DOWN_FLAG);
                break;
            case R.id.edit:
                enterEdit();
                break;
            case R.id.edit_false:
                giveUpEditAlert();
                break;
            case R.id.edit_true:
                if (content.needComfirm()) {
                    hideImm(v);
                    editComfim();
                } else {
                    Toast.makeText(mContext,
                            R.string.aurora_remember_edit_save_no_title,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.delete:
                showDeleteDialog();
                break;
            case R.id.save_to_gallery:
                saveToGallery();
                break;
            case R.id.edit_add_reminder:
                addReminder();
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
                //fix me
                beforeShare();
                shareMore(this, getString(R.string.share_activity_title), "", "", mImageUrl);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (dis_type) {
                case DIS_EDIT_FLAG:
                    giveUpEditAlert();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void giveUpEditAlert() {
        if (!isChanged()) {
            if (NEW_FLAG) {
                finish();
                return;
            }
            hideImm(mEditCancle);
            editCancle();
        } else {
            new AuroraAlertDialog.Builder(this).setTitle(R.string.reminder)
                    .setMessage(R.string.give_up_confirm_message)
                    .setPositiveButton(R.string.aurora_comfirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (NEW_FLAG) {
                                finish();
                                return;
                            }
                            hideImm(mEditCancle);
                            editCancle();
                        }
                    })
                    .setNegativeButton(R.string.aurora_cancle, null)
                    .create()
                    .show();
        }
    }

    private boolean isChanged() {
        RememberDayInfo info = allDayInfos.get(mIndex);
        if (!content.isChanged(info)
                && tmpPicPath.equals(info)
                && tmpScheduleFlag == info.getScheduleFlag()
                && tmpReminderData == info.getReminderData()) {
            return false;
        } else {
            return true;
        }
    }

    private void hideImm(View v) {
        InputMethodManager imm =
                (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void addReminder() {
        Intent intent = new Intent(WXEntryActivity.this, AddOrEditReminderActivity.class);

        if (tmpReminderData == 0L) {
            intent.putExtra(AddOrEditReminderActivity.KEY_MODE,
                    AddOrEditReminderActivity.MODE_ADD_REMINDER);
        } else {
            intent.putExtra(AddOrEditReminderActivity.KEY_MODE,
                    AddOrEditReminderActivity.MODE_EDIT_REMINDER);
            intent.putExtra(AddOrEditReminderActivity.KEY_REMINDER_DATE_TIMESTAMP,
                    tmpReminderData);
        }
        startActivityForResult(intent, REQUEST_CODE_ADD_REMINDER);
    }

    private void deleteCur() {
        RememberDayInfo info = allDayInfos.get(mIndex);
        RememberDayDao dao = new RememberDayDao(WXEntryActivity.this);
        dao.deleteLastDay(info.getTitle(), String.valueOf(info.getMillTime()));
        dao.closeDatabase();
        CalendarAlarmReceiver.scheduleAlarmById(info.getId(),
                AddOrEditReminderActivity.MODE_DEL_REMINDER, info.getReminderData());
        if (curGalleryIndex != -1 && curGalleryIndex < DEFAULT_PIC_NUM) {
            selectIVS.get(curGalleryIndex).setVisibility(View.INVISIBLE);
        }

        picPagerAdapter.removeItem();

        vp.setVisibility(View.VISIBLE);
        bg.setVisibility(View.GONE);
        controlTopViews(NONE_DOWN_FLAG);
        content.setEditMode(false, true, NEW_FLAG);
        dis_type = DIS_DETAIL_FLAG;
    }

    private void enterEdit() {
        if (NEW_FLAG) {
            mScheduleSwith.setChecked(false);
        } else {
            RememberDayInfo info = allDayInfos.get(mIndex);
            initTmpInfo(info);
            initEditData(info);
        }
        vp.setVisibility(View.GONE);
        bg.setVisibility(View.VISIBLE);
        controlTopViews(EDIT_DOWN_FLAG);
    }

    private void initEditData(RememberDayInfo info) {
        if (info.getScheduleFlag() == 1) {
            mScheduleSwith.setChecked(true);
        } else {
            mScheduleSwith.setChecked(false);
        }

        if (info.getReminderData() != null
                && info.getReminderData() != 0L) {
            long reminderDate = info.getReminderData();
            Drawable drawable = getResources().getDrawable(R.drawable.ring);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                    drawable.getMinimumHeight());

            mAddReminderText.setCompoundDrawables(drawable, null, null, null);
            mAddReminderText.setText(TimeUtils.formatDateTime(reminderDate,
                    false));
        } else {
            mAddReminderText.setText(R.string.aurora_reminder_add);
            mAddReminderText.setCompoundDrawables(null, null, null, null);
        }

        if (info.getPicPath().startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).equals(info.getPicPath())) {
                    bg.setImageResource(mImgBgs[i]);
                    selectIVS.get(i).setVisibility(View.VISIBLE);
                    galleryIndex = curGalleryIndex = i;
                    break;
                }
            }
        } else {
            String url = info.getPicPath();
            url = url.substring(7);
            BitmapFactory ft = new BitmapFactory();
            Bitmap bt = ft.decodeFile(url);
            bg.setImageBitmap(bt);
            galleryIndex = curGalleryIndex = -1;
        }

        content.setEditMode(true, true, NEW_FLAG);
    }

    private void initTmpInfo(RememberDayInfo info) {
        tmpPicPath = info.getPicPath();
        tmpScheduleFlag = info.getScheduleFlag();
        tmpReminderData = info.getReminderData();
    }

    private void initDisplayMode() {
        if (NEW_FLAG) {
            content.setEditMode(true, false, NEW_FLAG);
            content.initData(NEW_FLAG, null);
            topToolEdit.setVisibility(View.VISIBLE);
            bottonToolEdit.setVisibility(View.VISIBLE);
            bg.setImageResource(mImgBgs[galleryIndex]);
            selectIVS.get(galleryIndex).setVisibility(View.VISIBLE);
            bg.setVisibility(View.VISIBLE);
            vp.setVisibility(View.GONE);
            dis_type = DIS_EDIT_FLAG;
            isVisible = true;
        } else {
            content.setEditMode(false, false, NEW_FLAG);
            content.initData(NEW_FLAG, allDayInfos.get(mIndex));
        }
        initDisplay();
    }

    private void initDisplay() {
        if (NEW_FLAG) {

        } else {
            vp.setCurrentItem(mIndex, false);
        }
    }

    private void editComfim() {
        RememberDayInfo info;
        if (NEW_FLAG) {
            info = newInfo;
        } else {
            info = allDayInfos.get(mIndex);
        }
        info.setScheduleFlag(tmpScheduleFlag);
        info.setReminderData(tmpReminderData);
        content.getEditData(info);

        if (curGalleryIndex != -1 && curGalleryIndex < DEFAULT_PIC_NUM) {
            selectIVS.get(curGalleryIndex).setVisibility(View.INVISIBLE);
        }
        if (!tmpPicPath.equals(info.getPicPath()) && !NEW_FLAG) {
            ImageView iv = (ImageView) vp.findViewFromObject(mIndex);
            info.setPicPath(tmpPicPath);
            if (tmpPicPath.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                iv.setImageURI(Uri.parse(tmpPicPath));
            } else {
                String url = tmpPicPath.substring(7);
                BitmapFactory ft = new BitmapFactory();
                Bitmap bt = ft.decodeFile(url);
                iv.setImageBitmap(bt);
            }
        }

        vp.setVisibility(View.VISIBLE);
        bg.setVisibility(View.GONE);
        controlTopViews(NONE_DOWN_FLAG);
        content.setEditMode(false, true, NEW_FLAG);
        dis_type = DIS_DETAIL_FLAG;

        if (NEW_FLAG) {
            info.setPicPath(tmpPicPath);
            picPagerAdapter.addItem(info);
        } else {
            picPagerAdapter.upgradeItem(info);
        }

        if (warn_type != AddOrEditReminderActivity.MODE_NO_REMINDER)
            CalendarAlarmReceiver.scheduleAlarmById(info.getId(), warn_type, info.getReminderData());
    }

    private void editCancle() {
        content.setEditMode(false, true, NEW_FLAG);
        if (curGalleryIndex != -1 && curGalleryIndex < GALLERY_NUM) {
            selectIVS.get(curGalleryIndex).setVisibility(View.INVISIBLE);
        }
        bg.setVisibility(View.GONE);
        vp.setVisibility(View.VISIBLE);
        controlTopViews(NONE_DOWN_FLAG);
        dis_type = DIS_DETAIL_FLAG;
    }

    private void saveToGallery() {
        beforeShare();
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(tmpUri);
        sendBroadcast(intent);
        ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_success));
    }

    //fix me 错误处理
    private void getTmpPic() {
        if (tmpBitmap == null) {
            ToastUtil.shortToast(getResources().getString(R.string.save_to_gallery_failed));
            return;
        }

        if (SDcardManager.checkSDCardAvailable()) {
            File dstFile = Environment.getExternalStorageDirectory();
            if (dstFile == null) {
                ToastUtil.shortToast(getResources().getString(R.string.sdcard_is_not_available));
                return;
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
                dstFile = new File(dstFile.getAbsolutePath(), System.currentTimeMillis() + ".jpg");
                try {
                    dstFile.createNewFile();
                    FileOutputStream fOut = null;
                    fOut = new FileOutputStream(dstFile);
                    result = tmpBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tmpUri = Uri.fromFile(dstFile);
                mImageUrl = tmpUri.getPath();
            }
        } else {
            ToastUtil.shortToast(getResources().getString(R.string.sdcard_is_not_available));
        }
    }

    private void getCurBitmap() {
        //topToolShare.setVisibility(View.INVISIBLE);
        //bottomToolShare.setVisibility(View.INVISIBLE);

        rootLayout.setDrawingCacheEnabled(true);
        rootLayout.buildDrawingCache(true);
        tmpBitmap = Bitmap.createBitmap(rootLayout.getDrawingCache());
        rootLayout.setDrawingCacheEnabled(false);

        //topToolShare.setVisibility(View.VISIBLE);
        //bottomToolShare.setVisibility(View.VISIBLE);
    }

    private void shareMore(Activity activity, String activityTitle,
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

    private void beforeShare() {
        if (tmpUri == null) {
            getTmpPic();
        }
    }

    private void endShare() {
        tmpBitmap.recycle();
        tmpBitmap = null;
        tmpUri = null;
        mImageUrl = null;
    }

    private void share2QQ() {
        beforeShare();
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, mImageUrl);
//        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, /*手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替。*/);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ(this, params, this);
    }

    private void share2QZone() {
        beforeShare();
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
        beforeShare();
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
        Bitmap thumbBmp = BitmapUtil.getThumbBitmap(mImageUrl, THUMB_SIZE, THUMB_SIZE);
        msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "img" + String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = sendByWx ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }

    private void shareToSinaWeibo() {
        beforeShare();
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
        if (tmpBitmap != null) {
            tmpBitmap.recycle();
            tmpBitmap = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_DESKTOP_WALLPAPER:
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        Uri uri = data.getData();
                        Intent request = new Intent(WXEntryActivity.this, PictureViewActivity.class);

                        request.setDataAndType(uri, IMAGE_TYPE);

                        startActivityForResult(request, REQUEST_CODE_SET_WALLPAPER);
                    }
                }
                break;
            case REQUEST_CODE_SET_WALLPAPER:
                if (resultCode == RESULT_OK) {
                    if (curGalleryIndex < DEFAULT_PIC_NUM) {
                        selectIVS.get(curGalleryIndex).setVisibility(View.INVISIBLE);
                    }
                    String url = data.getStringExtra("url");
                    BitmapFactory ft = new BitmapFactory();
                    Bitmap bt = ft.decodeFile(url);
                    bg.setImageBitmap(bt);
                    tmpPicPath = "file://" + url;
                    galleryIndex = DEFAULT_PIC_NUM;
                    curGalleryIndex = DEFAULT_PIC_NUM;


                }
                break;
            case REQUEST_CODE_ADD_REMINDER:
                if (resultCode == AddOrEditReminderActivity.RESULT_OK) {
                    warn_type = data.getIntExtra(AddOrEditReminderActivity.KEY_MODE,
                            AddOrEditReminderActivity.MODE_ADD_REMINDER);
                    if (warn_type
                            == AddOrEditReminderActivity.MODE_DEL_REMINDER) {
                        tmpReminderData = 0L;
                        mAddReminderText.setText(R.string.aurora_reminder_add);
                        mAddReminderText.setCompoundDrawables(null, null, null, null);
                    } else {
                        Long reminderData = data.getLongExtra(
                                AddOrEditReminderActivity.KEY_REMINDER_DATE_TIMESTAMP, 0L);
                        tmpReminderData = reminderData;
                        long reminderDate = data.getLongExtra(
                                AddOrEditReminderActivity.KEY_REMINDER_DATE_TIMESTAMP, -1L);
                        Drawable drawable = getResources().getDrawable(R.drawable.ring);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                                drawable.getMinimumHeight());

                        mAddReminderText.setCompoundDrawables(drawable, null, null, null);
                        mAddReminderText.setText(TimeUtils.formatDateTime(reminderDate, false));
                    }
                }
                break;
//            default:
//                //fix me
//                if (mSsoHandler != null) {
//                    mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
//                }
//                mTencent.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showDeleteDialog() {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete));
        builder.setMessage(R.string.aurora_remember_delete_cur);
        builder.setPositiveButton(getString(R.string.aurora_comfirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteCur();
                    }
                });
        builder.setNegativeButton(getString(R.string.aurora_cancle), null);
        builder.create().show();
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
