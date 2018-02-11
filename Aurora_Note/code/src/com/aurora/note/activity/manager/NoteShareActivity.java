package com.aurora.note.activity.manager;

import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.note.R;
import com.aurora.note.sina.weibo.AccessTokenKeeper;
import com.aurora.note.sina.weibo.Constants;
import com.aurora.note.sina.weibo.SinaAccessToken;
import com.aurora.note.util.BitmapUtil;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.ToastUtil;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class NoteShareActivity extends AuroraActivity implements View.OnClickListener {

	private static final String TAG = "NoteShareActivity";

    public static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private static final int WEIBO_CHINESE_LIMITED_LENGTH = 140;

	private static final int TYPE_SINA_WEIBO = 1;
	private static final int TYPE_WX_SESSION = 2;
	private static final int TYPE_WX_TIMELINE = 3;

	private int shareType = TYPE_SINA_WEIBO;

	private Activity mActivity;
	private IWXAPI wxApi;

	private SsoHandler mSsoHandler;

	private EditText contentEditText;
	private ImageView sinaWeiboView;
	private ImageView wxSessionView;
	private ImageView wxTimelineView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = this;
		wxApi = WXAPIFactory.createWXAPI(this, Globals.APP_ID);

		setAuroraContentView(R.layout.note_share_activity, AuroraActionBar.Type.Normal);

		initActionBar();
		initView();
	}

	private void initActionBar() {
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.setTitle(R.string.note_share);
		actionBar.addItem(R.layout.note_share_activity_actionbar_custom, 1);

		actionBar.getTitleView().setTextColor(getResources().getColor(R.color.note_main_text_color));
		actionBar.setBackgroundResource(R.drawable.aurora_action_bar_top_bg);

		ImageButton homeIcon = (ImageButton) actionBar.getHomeButton();
		homeIcon.setImageResource(R.drawable.aurora_action_bar_back);

		TextView sendView = (TextView) actionBar.findViewById(R.id.send_view);
		sendView.setOnClickListener(this);
	}

	private void initView() {
		contentEditText = (EditText) findViewById(R.id.share_content);
		contentEditText.requestFocus();

		sinaWeiboView = (ImageView) findViewById(R.id.share_to_sina_weibo);
		sinaWeiboView.setOnClickListener(this);
		wxSessionView = (ImageView) findViewById(R.id.share_to_wx_session);
		wxSessionView.setOnClickListener(this);
		wxTimelineView = (ImageView) findViewById(R.id.share_to_wx_timeline);
		wxTimelineView.setOnClickListener(this);

		int wxSdkVersion = wxApi.getWXAppSupportAPI();
        if (wxSdkVersion < TIMELINE_SUPPORTED_VERSION) {
            if (wxSdkVersion == 0) {
            	wxSessionView.setVisibility(View.GONE);
            }
            wxTimelineView.setVisibility(View.GONE);
        }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send_view:
			sendShareRequest();
			break;
		case R.id.share_to_sina_weibo:
			selectShareType(TYPE_SINA_WEIBO);
			break;
		case R.id.share_to_wx_session:
			selectShareType(TYPE_WX_SESSION);
			break;
		case R.id.share_to_wx_timeline:
			selectShareType(TYPE_WX_TIMELINE);
			break;
		default:
			break;
		}
	}

	private void selectShareType(int type) {
		shareType = type;
		if (shareType == TYPE_SINA_WEIBO) {
			sinaWeiboView.setImageResource(R.drawable.picture_share_by_weibo_pressed);
			wxSessionView.setImageResource(R.drawable.picture_send_by_wx_normal);
			wxTimelineView.setImageResource(R.drawable.picture_send_by_wx_normal);
		} else if (shareType == TYPE_WX_SESSION) {
			sinaWeiboView.setImageResource(R.drawable.picture_share_by_weibo_normal);
			wxSessionView.setImageResource(R.drawable.picture_send_by_wx_pressed);
			wxTimelineView.setImageResource(R.drawable.picture_share_by_wx_normal);
		} else {
			sinaWeiboView.setImageResource(R.drawable.picture_share_by_weibo_normal);
			wxSessionView.setImageResource(R.drawable.picture_send_by_wx_normal);
			wxTimelineView.setImageResource(R.drawable.picture_share_by_wx_pressed);
		}
	}

	private void sendShareRequest() {
		if (TextUtils.isEmpty(contentEditText.getText().toString())) {
			return;
		}

		if (!SystemUtils.isNetworkConnected()) {
			ToastUtil.shortToast(R.string.toast_no_network);
			return;
		}

		if (shareType == TYPE_SINA_WEIBO) {
			shareToSinaWeibo();
		} else {
			shareToWx(shareType == TYPE_WX_SESSION);
		}
	}

	private void shareToWx(boolean isWxSession) {
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://www.iunios.com";

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = webpage;
		msg.title = contentEditText.getText().toString();
		msg.description = contentEditText.getText().toString();

		Bitmap thumbBmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = isWxSession ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

	private void shareToSinaWeibo() {
        if (getChineseLength() > WEIBO_CHINESE_LIMITED_LENGTH) {
            ToastUtil.shortToast(R.string.toast_weibo_too_long);
            return;
        }

        // 是否有授权
        SinaAccessToken token = AccessTokenKeeper.readAccessToken(this);
        if (token == null || !token.getToken().isSessionValid()) {
            // 未授权或者授权已过期，需要重新授权
            if (mSsoHandler == null) {
                // 创建微博实例
                mSsoHandler = new SsoHandler(this, new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
            }
            mSsoHandler.authorize(new AuthListener());
        } else {
            StatusesAPI statusApi = new StatusesAPI(token.getToken());
            statusApi.update(contentEditText.getText().toString(), null, null, new SinaWeiboRequestListener());
            ToastUtil.shortToast(R.string.share_sina_weibo_sending);
        }
	}

	private int getChineseLength() {
		Editable editable = contentEditText.getText();
		final int realLength = editable.length();

		double len = 0;
		for (int i = 0; i < realLength; i++) {
			int temp = (int) editable.charAt(i);
            if (temp > 0 && temp < 127) {
                len += 0.5;
            } else {
                len++;
            }
		}

		return (int) (len + 0.5);
	}

    private void sendToSinaWeibo() {
        SinaAccessToken token = AccessTokenKeeper.readAccessToken(this);
        if (token != null && token.getToken().isSessionValid()) {
            StatusesAPI statusApi = new StatusesAPI(token.getToken());
            statusApi.update(contentEditText.getText().toString(), null, null, new SinaWeiboRequestListener());

            ToastUtil.shortToast(R.string.share_sina_weibo_sending);
        } else {
            ToastUtil.shortToast(R.string.share_sina_weibo_authorize_expire);
        }
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
                sendToSinaWeibo();
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
            Log.e(TAG, "Jim, sina weibo authorize exception: " + e.getMessage());
            ToastUtil.shortToast(R.string.share_sina_weibo_authorize_invalid_token);
        }
    }

    private void saveSinaToken(Oauth2AccessToken token, String nickName, String userName) {
        SinaAccessToken sinaToken = new SinaAccessToken();
        sinaToken.setToken(token);
        sinaToken.setNickName(nickName);
        sinaToken.setUserName(userName);
        AccessTokenKeeper.writeAccessToken(NoteShareActivity.this, sinaToken);
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
            mPd = ProgressDialog.show(NoteShareActivity.this, null,
                    getResources().getString(R.string.share_sina_weibo_get_user_info),
                    true, false);
            mPd.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mPd.isShowing() && !NoteShareActivity.this.isFinishing()) {
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
                    sendToSinaWeibo();
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

    /**
     * 微博 OpenAPI 回调接口。
     */
    private class SinaWeiboRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                if (response.startsWith("{\"created_at\"")) {
                    ToastUtil.shortToast(R.string.share_sina_weibo_send_success);

                    mActivity.finish();
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
            Log.e(TAG, "Jim, onWeiboException, " + e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Log.e(TAG, "Jim, error info: " + info.toString());
            ToastUtil.shortToast(R.string.share_sina_weibo_send_failed);
        }
    }

}