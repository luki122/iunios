
package com.aurora.commemoration.sina.weibo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.calendar.CalendarApp;
import com.android.calendar.R;
import com.android.calendar.R.layout;
import com.aurora.calendar.util.BitmapUtil;
import com.aurora.calendar.util.SystemUtils;
import com.aurora.calendar.util.ToastUtil;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;

import java.io.File;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;

/**
 * 分享新浪微波界面
 * @author JimXia
 * 2014-6-16 下午2:10:49
 */
public class ShareSinaWeiboActivity extends AuroraActivity implements OnClickListener {
    private static final String TAG = "ShareSinaWeiboActivity";

    private static final int MAX_TEXT_LENGTH = 140;
    private static final long REMINDER_DELAY = 10000;
    
    private static final int AURORA_SEND = 1;
    
    private static final int AURORA_CUSTOM_MENU_UNBIND_AUTHORIZE = 2;
    
    public static final String KEY_SHARE_IMAGE_URL = "shareImageURL";
    
    private EditText mContentEt;
    private ImageView mPicIndicator;
//    private TextView mRemainTv;
    private TextView mRightSendTv;
    
    private String mImageUrl;
    private Bitmap mBitmap;
    private ForegroundColorSpan mSpan;
    
    public static boolean sIsWeiboSending = false; // 标识是否正有微博在发送
    private static Handler sHandler = null;
    private static Reminder sReminder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(layout.share_sina_weibo_activity, AuroraActionBar.Type.Normal);
        initActionBar();
        initViews();
        initAuroraMenu();
        setListener();
        if (!initData()) {
            finish();
            return;
        }
    }
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
    private boolean initData() {
        final Intent intent = getIntent();
        mImageUrl = intent.getStringExtra(KEY_SHARE_IMAGE_URL);
        if (TextUtils.isEmpty(mImageUrl)) {
            Log.w(TAG, "Jim, image url is empty or null");
        } else {
            try {
                mBitmap = BitmapFactory.decodeFile(mImageUrl);
                mPicIndicator.setImageBitmap(getThumbnail());
            } catch (OutOfMemoryError e) {
                ToastUtil.shortToast(R.string.memory_is_not_enough);
                Log.e(TAG, "Jim, decode bitmap error: " + e.getMessage());
                return false;
            } catch (Throwable t) {
                ToastUtil.shortToast(R.string.decode_bitmap_error);
                Log.e(TAG, "Jim, decode bitmap error: " + t.getMessage());
                return false;
            }
        }
        
        return true;
    }
    
    private Bitmap getThumbnail() {
        final Resources res = getResources();
        final int expectedWidth = res.getDimensionPixelSize(R.dimen.share_pic_width);
        final int maxHeight = res.getDimensionPixelSize(R.dimen.share_pic_max_height);
        Bitmap bitmap = BitmapUtil.getBitmap(mImageUrl, expectedWidth, Integer.MAX_VALUE);
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d(TAG, "Jim, getThumbnail, width: " + width + ", height: " + height);
        if (width != expectedWidth) {
            bitmap = BitmapUtil.scaleBitmap(bitmap, expectedWidth, 0);
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            Log.d(TAG, "Jim, getThumbnail, after scale, width: " + width + ", height: " + height);
            if (height > maxHeight) {
                bitmap = BitmapUtil.cropBitmap(bitmap, expectedWidth, maxHeight, true);
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                Log.d(TAG, "Jim, getThumbnail, after crop, width: " + width + ", height: " + height);
                return bitmap;
            }
        }
        return bitmap;
    }

    private void initAuroraMenu() {
        final SinaAccessToken token = AccessTokenKeeper.readAccessToken(this);
        if (token != null && token.getToken().isSessionValid()) {
            String format = getString(R.string.share_sina_weibo_menu_unbind);
            String unbindMenuItemTitle = String.format(format, token.getUserName());
            addMenu(AURORA_CUSTOM_MENU_UNBIND_AUTHORIZE, unbindMenuItemTitle, new OnMenuItemClickLisener() {
                @Override
                public void onItemClick(View paramView) {
                    // 检查一下网络是否可用
                    if (!SystemUtils.isNetworkConnected()) {
                        ToastUtil.shortToast(R.string.share_sina_weibo_no_network);
                        return;
                    }
                    new LogoutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, token.getToken());
                }
            });
        }
    }
    
    private void closeSoftInputWindow() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mContentEt.getWindowToken(), 0);
        }
    }
    
    private class LogoutTask extends AsyncTask<Oauth2AccessToken, Integer, String> {
        private ProgressDialog mPd;
        private boolean mHasError = false;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPd = ProgressDialog.show(ShareSinaWeiboActivity.this, null,
                    getResources().getString(R.string.share_sina_weibo_unbind_ongoing),
                    true, false);
            mPd.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mPd.isShowing() && !ShareSinaWeiboActivity.this.isFinishing()) {
                mPd.dismiss();
            }
            Log.d(TAG, "Jim, logout response: " + result);
            if (mHasError || TextUtils.isEmpty(result)) {
                ToastUtil.shortToast(R.string.share_sina_weibo_unbind_error);
            } else {
                /*
                 * 
                 */
                AccessTokenKeeper.clear(CalendarApp.ysApp);
                ToastUtil.shortToast(R.string.share_sina_weibo_unbind_success);
                ShareSinaWeiboActivity.this.finish();
            }
        }

        @Override
        protected String doInBackground(Oauth2AccessToken... params) {
            Oauth2AccessToken token = params[0];
            try {
                return new LogoutAPI(token).logout();
            } catch (Exception e) {
                Log.e(TAG, "Jim, logout exception.", e);
                mHasError = true;
            }
            
            return null;
        }
    }

    private void setListener() {
        mContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                final int realLength = s.length();
                ForegroundColorSpan[] spans = s.getSpans(0, realLength, ForegroundColorSpan.class);
                if (spans != null) {
                    Log.d(TAG, "Jim, spans: " + spans + ", length: " + spans.length);
                    for (ForegroundColorSpan span: spans) {
                        s.removeSpan(span);
                    }
                }
                
                double len = 0;
                int length = 0;
                int spanStart = -1;
                for (int i = 0; i < realLength; i++) {
                    int temp = (int) s.charAt(i);
                    if (temp > 0 && temp < 127) {
                        len += 0.5;
                    } else {
                        len++;
                    }
                    length = (int)(len + 0.5d);
                    if (spanStart == -1 && length > MAX_TEXT_LENGTH) {
                        spanStart = i;
                    }
                }
                
                if (length > MAX_TEXT_LENGTH) {
                    mRightSendTv.setEnabled(false);
                    ForegroundColorSpan span = mSpan;
                    if (span == null) {
                        span = new ForegroundColorSpan(0xffff4444);
                        mSpan = span;
                    }
                    s.setSpan(span, spanStart, realLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    mRightSendTv.setEnabled(true);
                }
            }
        });
    }

    private void initViews() {
        mContentEt = (EditText) findViewById(R.id.content_et);
        mPicIndicator = (ImageView) findViewById(R.id.pic_indicator);
    }

    private void initActionBar() {
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.share_sina_weibo_title);
        actionBar.addItem(R.layout.share_sina_weibo_activity_actionbar_custom, AURORA_SEND);
//        mRemainTv = (TextView) actionBar.findViewById(R.id.remain_text_tv);
        mRightSendTv = (TextView) actionBar.findViewById(R.id.right_menu_tv);
//        mRemainTv.setText(String.valueOf(MAX_TEXT_LENGTH));
        mRightSendTv.setOnClickListener(this);
    }
    
    private void send() {
        // 检查一下网络是否可用
        if (!SystemUtils.isNetworkConnected()) {
            ToastUtil.shortToast(R.string.share_sina_weibo_no_network);
            return;
        }
        
        // 检查要发送图片的大小
        File file = new File(mImageUrl);
        if (file.length() > Constants.MAX_IMAGE_SIZE_IN_MEMORY) {
            ToastUtil.shortToast(R.string.share_sina_weibo_image_too_large);
            return;
        }
        
        String content = mContentEt.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            content = getResources().getString(R.string.share_sina_weibo_default_content); // 新浪微博的接口不允许这个字段为空
        }
        SinaAccessToken token = AccessTokenKeeper.readAccessToken(this);
        if (token != null && token.getToken().isSessionValid()) {
            StatusesAPI statusApi = new StatusesAPI(token.getToken());
            statusApi.upload(content, mBitmap, null, null, new SinaWeiboRequestListener());
            
            final boolean debug = false;
            if (debug) {
                StringBuilder sb = new StringBuilder();
                sb.append("发送图片的宽：").append(mBitmap.getWidth()).append(", 高: ").append(mBitmap.getHeight());
                sb.append("\n占用内存大小： ").append(mBitmap.getByteCount() / (1024 * 1024)).append("M");
                debug(sb.toString());
            }
            
            sReminder = new Reminder();
            sHandler = new Handler();
            sHandler.postDelayed(sReminder, REMINDER_DELAY);
            sIsWeiboSending = true;
            finish();
            ToastUtil.shortToast(R.string.share_sina_weibo_sending);
        } else {
            ToastUtil.shortToast(R.string.share_sina_weibo_authorize_expire);
            finish();
        }
    }
    
    void debug(String info) {
        ToastUtil.longToast(info);
        ToastUtil.longToast(info);
        ToastUtil.longToast(info);
    }
    
    private static class Reminder implements Runnable {
        @Override
        public void run() {
            ToastUtil.shortToast(R.string.share_sina_weibo_send_ongoing);
            sHandler.postDelayed(this, REMINDER_DELAY);
        }
        
    }
    
    private static void cancelReminder() {
        if (sHandler != null && sReminder != null) {
            sHandler.removeCallbacks(sReminder);
            sReminder = null;
            sHandler = null;
        }
    }
    
    /**
     * 微博 OpenAPI 回调接口。
     */
    private static class SinaWeiboRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            sIsWeiboSending = false;
            cancelReminder();
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"created_at\"")) {
                    ToastUtil.shortToast(R.string.share_sina_weibo_send_success);
                    return;
                } else {
                    Log.d(TAG, "Jim, unexpected response format: " + response);
                }
            } else {
                Log.d(TAG, "Jim, response is empty");
            }
            ToastUtil.shortToast(R.string.share_sina_weibo_send_failed);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            sIsWeiboSending = false;
            cancelReminder();
            Log.e(TAG, "Jim, onWeiboException, " + e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Log.e(TAG, "Jim, error info: " + info.toString());
            ToastUtil.shortToast(R.string.share_sina_weibo_send_failed);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_menu_tv:
                send();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            closeSoftInputWindow();
            showCustomMenu();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }    
}