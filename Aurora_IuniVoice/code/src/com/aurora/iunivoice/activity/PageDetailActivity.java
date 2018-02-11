package com.aurora.iunivoice.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.conn.HttpHostConnectException;

import u.aly.ba;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.AddCommentHolder;
import com.aurora.datauiapi.data.bean.AddFavourHolder;
import com.aurora.datauiapi.data.bean.AddScorerHolder;
import com.aurora.datauiapi.data.bean.CommentInfoHolder;
import com.aurora.datauiapi.data.bean.UserInfoDetailInfo;
import com.aurora.datauiapi.data.bean.UserInfoHolder;
import com.aurora.datauiapi.data.bean.UserInfoSpace;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.http.HttpRequstData;
import com.aurora.iunivoice.share.ShareCommonUtil;
import com.aurora.iunivoice.share.weibo.WeiboConstants;
import com.aurora.iunivoice.share.weibo.WeiboShareUtil;
import com.aurora.iunivoice.share.wx.WxShareUtil;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.DefaultUtil;
import com.aurora.iunivoice.utils.FileLog;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.LoadingPageUtil;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.ToastUtil;
import com.aurora.iunivoice.widget.AuroraEditText;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;

/**
 * Created by joy on 11/2/15.
 */
public class PageDetailActivity extends BaseActivity implements
		View.OnClickListener, IWeiboHandler.Response {

	private static final long ANIMATION_TIME = 300L;
	private int THREE_OF_SCREEN_SIZE;// 屏幕高度的3分之一，以此来判断是否弹出键盘
	private LinearLayout ll_input_normal;
	private LinearLayout ll_input_press;
	private LinearLayout rl_share;
	private LinearLayout wb_share;
	private LinearLayout wx_share;
	private LinearLayout wx_circle_share;
	private AuroraEditText input_reply_press;
	private AuroraEditText input_reply_normal;
	private RelativeLayout rl_input_reply;
	private ImageView comment_iv;
	private ImageView one_iv;
	private ImageView two_iv;
	private ImageView favor_iv;
	private ImageView hide_bg;
	private Button send_reply;
	private WebView mWebView;
	private String url;
	private String mShareUrl;

	private WxShareUtil wxShareUtil;
	private WeiboShareUtil weiboShareUtil;
	private ShareCommonUtil shareCommonUtil;

	private static final int ACTIONBAR_ID_SHARE = 0x4521;// 顶部分享按钮
	private final int MAX_INPUT = 100;
	private final int MAX_LINE = 90;
	private IuniVoiceManager netManager;
	private InputMethodManager inputMethodManager;

	ProgressDialog progressDialog = null;
	private boolean isFavour = false;
	private boolean isRated = false;
	private boolean isRateShow = false;

	private boolean isCanShow = true;

	private String tidId;
	private String fildId;
	private String hashId;

	public static final String PAGE_ID_KEY = "page_id";
	public static final String PAGE_TITLE_KEY = "title_id";
	public static final String PAGE_DESCRIBE_KEY = "describe_id";
	public static final String FILD_ID_KEY = "fild_id";
	public static final String FORM_HASH_KEY = "formhash";

	public static final int  WEBVIEW_SHOW = 100;
	
	// 从banner进来
	public static final String POST_URL = "url";
	// 固定1
	public static final String POST_BANNER = "from_banner";
	public String post_tid;
	public String post_url_from_banner;
	public int from_banner = -1;

	private boolean isSharing = false;
	private String title;
	private String describe;
	private boolean isSending = false;
	private LoadingPageUtil loadingPageUtil;
	private boolean isLoading = false;
	private UserInfoDetailInfo detailInfo;
	private String userScore;
	private TextView scoreTv;
	private TextView lengthTv;
	private IWeiboShareAPI mWeiboShareAPI;

	private boolean hasF = false;
	private boolean hasG = false;
	private int grade = 0;
	private int reply = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_detail_ly);
		
		// 创建微博分享接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,
				WeiboConstants.APP_KEY);
		// 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
		// 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
		// NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
		mWeiboShareAPI.registerApp();
		// 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
		// 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。 
		// 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
		// 失败返回 false，不调用上述回调
		if (savedInstanceState != null) {
			mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
		}

		initViews();
		initData();
		setupViews();
		getNetData();
		initShareUtil();
		initWebView();
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (loadingPageUtil != null) {
			loadingPageUtil.clearRegister();
		}
	}

	private void initData() {
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		netManager = new IuniVoiceManager(this);
		tidId = getIntent().getStringExtra(PAGE_ID_KEY);// tid
		fildId = getIntent().getStringExtra(FILD_ID_KEY);
		hashId = getIntent().getStringExtra(FORM_HASH_KEY);
		title = getIntent().getStringExtra(PAGE_TITLE_KEY);

		post_url_from_banner = getIntent().getStringExtra(POST_URL);
		from_banner = getIntent().getIntExtra(POST_BANNER, -1);

		describe = getIntent().getStringExtra(PAGE_DESCRIBE_KEY);
		if (describe == null) {
			describe = "来自 IUNI 论坛";
		}

		initLoadingPage();
	}

	private void initViews() {
		THREE_OF_SCREEN_SIZE = getResources().getDisplayMetrics().heightPixels / 3;
		ll_input_normal = (LinearLayout) findViewById(R.id.ll_input_normal);
		ll_input_press = (LinearLayout) findViewById(R.id.ll_input_press);
		rl_share = (LinearLayout) findViewById(R.id.share_ly);
		wx_share = (LinearLayout) findViewById(R.id.friends);
		wx_circle_share = (LinearLayout) findViewById(R.id.circle);
		wb_share = (LinearLayout) findViewById(R.id.wb_ly);
		input_reply_press = (AuroraEditText) findViewById(R.id.input_reply_press);
		input_reply_normal = (AuroraEditText) findViewById(R.id.input_reply_normal);
		rl_input_reply = (RelativeLayout) findViewById(R.id.rl_input_reply);
		comment_iv = (ImageView) findViewById(R.id.comment_iv);
		favor_iv = (ImageView) findViewById(R.id.favour_iv);
		one_iv = (ImageView) findViewById(R.id.add_one_iv);
		two_iv = (ImageView) findViewById(R.id.add_two_iv);
		hide_bg = (ImageView) findViewById(R.id.hide_bg);
		send_reply = (Button) findViewById(R.id.btn_send_reply);
		lengthTv = (TextView) findViewById(R.id.length_hint);
		scoreTv = (TextView) findViewById(R.id.score_hint);
		input_reply_press.addTextChangedListener(watcher);
		mWebView = (WebView) findViewById(R.id.detail_web_cotent);
	}

	public void replyInputView() {
		if (!isCanShow) {
			rl_input_reply.setVisibility(View.GONE);
		} else {
			rl_input_reply.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setupViews() {
		rl_input_reply.addOnLayoutChangeListener(layoutChangeListener);
		favor_iv.setOnClickListener(this);
		comment_iv.setOnClickListener(this);
		send_reply.setOnClickListener(this);
		one_iv.setOnClickListener(this);
		two_iv.setOnClickListener(this);
		wb_share.setOnClickListener(this);
		wx_share.setOnClickListener(this);
		wx_circle_share.setOnClickListener(this);
		hide_bg.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		setActionBarItem();
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(this, getWindow().getDecorView());
		loadingPageUtil.setOnShowListener(new LoadingPageUtil.OnShowListener() {
			@Override
			public void onShow() {
			}
		});
		loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
			@Override
			public void onHide() {
			}
		});
		loadingPageUtil
				.setOnRetryListener(new LoadingPageUtil.OnRetryListener() {
					@Override
					public void retry() {
						mWebView.reload();
					}
				});
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			if (s.length() > MAX_LINE) {
				lengthTv.setVisibility(View.VISIBLE);
				lengthTv.setText(MAX_INPUT - s.length() + "");
			} else {
				lengthTv.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() > 0) {
				scoreTv.setVisibility(View.INVISIBLE);
			} else {
				scoreTv.setVisibility(View.VISIBLE);
			}
		}
	};

	private void initShareUtil() {
		wxShareUtil = new WxShareUtil(this);
		wxShareUtil.registerApp(this);
		// weiboShareUtil = WeiboShareUtil.getInstance(this);
		shareCommonUtil = new ShareCommonUtil();

		// mShareUrl = HttpRequstData.getShareURLStr(
		// Globals.HTTP_WEB_PAGE_REQUEST_URL, tidId, 1);
	}

	private void setActionBarItem() {
		removeActionBarItem(ACTIONBAR_ID_SHARE);
		addActionBarItem(R.drawable.drawable_share, ACTIONBAR_ID_SHARE);
	}

	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.postdetail_title);
	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case ACTIONBAR_ID_SHARE:
			if (!AccountHelper.mLoginStatus) {
				startLogin();
			} else {
				/*
				 * new TotalCount(PageDetailActivity.this, "300", "013", 1)
				 * .CountData();
				 */
				// MobclickAgent.onEvent(PageDetailActivity.this,
				// Globals.PREF_TIMES_SHARE);

				if (!isCanShow) {
					ToastUtil.shortToast("当前页面无法分享！");
					return;
				}

				if (!isSharing) {
					((ImageView) findViewById(ACTIONBAR_ID_SHARE))
							.setImageResource(R.drawable.aurora_share_select);
					shareContent();
				} else {
					((ImageView) findViewById(ACTIONBAR_ID_SHARE))
							.setImageResource(R.drawable.aurora_share_unselect);
					closeShare();
				}
			}
			break;
		case BACK_ITEM_ID:
			back();
			// onBackPressed();
			break;
		default:
			break;
		}
	}

	private void shareContent() {
		isSharing = true;
		rl_share.setVisibility(View.VISIBLE);
		ObjectAnimator tranAnim = ObjectAnimator.ofFloat(rl_share,
				"TranslationY", -rl_share.getHeight(), 0.0F);
		tranAnim.setDuration(ANIMATION_TIME);
		tranAnim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
			}

			@Override
			public void onAnimationEnd(Animator animator) {

			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		tranAnim.start();

		// showSendConfirmDialog(R.string.share_title);
	}

	private void closeShare() {
		ObjectAnimator tranAnim = ObjectAnimator.ofFloat(rl_share,
				"TranslationY", 0, -rl_share.getHeight());
		tranAnim.setDuration(ANIMATION_TIME);
		tranAnim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				((ImageView) findViewById(ACTIONBAR_ID_SHARE))
						.setImageResource(R.drawable.aurora_share_unselect);
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		tranAnim.start();
		isSharing = false;
	}

	private void showSendConfirmDialog(final int messageId) {

		shareCommonUtil.setCallback(new ShareCommonUtil.AuroraDilogCallBack() {

			@Override
			public void onFinishDialog(int ret) {
				if (ret == 0) {// 微信会话
					shareOnWeiXin(ret);
				} else if (ret == 1) {// 朋友圈
					shareOnWeiXin(ret);
				} else if (ret == 2) {// 微薄
					shareOnXLWb();
				} else {// 其他
					// createShareIntent();
				}
			}
		});

		shareCommonUtil.showShareDialog(this, messageId);
	}

	private void shareOnWeiXin(final int type) {
		if (wxShareUtil.isSupport(this)) {
			/*
			 * new CreateBitmapTask().execute(new CreateCallBack() {
			 * 
			 * @Override public void onCreatePic(Bitmap bitmap) {
			 * wxShareUtil.sendImage(PageDetailActivity.this, null,
			 * ShareCommonUtil.TEMP_PIC_FILE_PATH, null, type == 1 ? true :
			 * false); } });
			 */

			WxShareUtil.ShareContentWebpage scw = new WxShareUtil.ShareContentWebpage(
					title, describe, mShareUrl, R.drawable.share_icon);
			wxShareUtil.shareWebPage(scw, type == 1 ? true : false);
		} else {
			Toast.makeText(this, getString(R.string.share_wx_not_support),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void shareOnXLWb() {
		if (weiboShareUtil.isAuth()) {
			Log.e("JOY", "isAuth");
			weiboShareUtil.sendImageMessage("hello", BitmapFactory
					.decodeResource(getResources(), R.drawable.add_nice));
		} else {
			Log.e("JOY", "NOT isAuth");
			weiboShareUtil.authWeibo(this,
					new WeiboShareUtil.AuroraWeiBoCallBack() {
						@Override
						public void onSinaWeiBoCallBack(int ret) {
							Log.e("JOY", ret + " rret");
							if (ret == WeiboShareUtil.AURORA_WEIBO_SUCCESS) {
								weiboShareUtil.sendImageMessage("",
										BitmapFactory.decodeResource(
												getResources(),
												R.drawable.add_nice));
							}
						}
					});
		}
	}

	/**
	 * @see {@link Activity#onNewIntent}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			break;
		}
	}

	private void sendSingleMessage() {

		// 1. 初始化微博的分享消息
		// 用户可以分享文本、图片、网页、音乐、视频中的一种
		WeiboMessage weiboMessage = new WeiboMessage();
		weiboMessage.mediaObject = getWebpageObj();
		// 2. 初始化从第三方到微博的消息请求
		SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
		// 用transaction唯一标识一个请求
		request.transaction = String.valueOf(System.currentTimeMillis());
		request.message = weiboMessage;

		// 3. 发送请求消息到微博，唤起微博分享界面
		boolean sendRequest = mWeiboShareAPI.sendRequest(
				PageDetailActivity.this, request);

		if (!sendRequest) {
			ToastUtil.shortToast("请检查是否已安装客户端");
		}

	}

	/**
	 * 创建多媒体（网页）消息对象。
	 *
	 * @return 多媒体（网页）消息对象。
	 */
	private WebpageObject getWebpageObj() {
		WebpageObject mediaObject = new WebpageObject();
		mediaObject.identify = Utility.generateGUID();
		mediaObject.title = title;
		mediaObject.description = describe;

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.share_icon);
		// 设置 Bitmap 类型的图片到视频对象里 设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
		mediaObject.setThumbImage(bitmap);
		mediaObject.actionUrl = mShareUrl;
		mediaObject.defaultText = "Webpage 默认文案";
		return mediaObject;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.add_one_iv:
		case R.id.add_two_iv:

			addScore(view);
			// comment_iv.setImageResource(R.drawable.comment_pic_press);
			// comment_iv.setClickable(false);
			break;
		case R.id.comment_iv:
			if (isRated) {
				ToastUtil.shortToast(R.string.rated);
				return;
			} else {
				if (one_iv.getVisibility() == View.GONE) {
					isRateShow = true;
					one_iv.setVisibility(View.VISIBLE);
					two_iv.setVisibility(View.VISIBLE);
				} else {
					isRateShow = false;
					one_iv.setVisibility(View.GONE);
					two_iv.setVisibility(View.GONE);
				}
			}
			break;
		case R.id.favour_iv:
			if (!AccountHelper.mLoginStatus) {
				startLogin();
			} else {
				/*
				 * new TotalCount(PostDetailActivity.this, "300", "012", 1)
				 * .CountData();
				 */
				// MobclickAgent.onEvent(PageDetailActivity.this,
				// Globals.PREF_TIMES_COLLECT);
				if (!isFavour) {
					addFavour();
				} else {
					ToastUtil.shortToast(R.string.favoured);
				}
			}
			break;
		case R.id.btn_send_reply:
			send();
			break;
		case R.id.friends:
			closeShare();
			shareOnWeiXin(0);
			break;
		case R.id.circle:
			closeShare();
			shareOnWeiXin(1);
			break;
		case R.id.wb_ly:
			closeShare();
			// shareOnXLWb();
			sendSingleMessage();
			break;
		default:
			break;
		}
	}

	private void addFavour() {
		if (!DefaultUtil.checkNetWork(this)) {
			return;
		}
		if (!AccountHelper.mLoginStatus) {
			startLogin();
			return;
		}
		netManager.addFavour(new DataResponse<AddFavourHolder>() {
			@Override
			public void run() {
				super.run();
				if (value != null) {
					if (value.getReturnCode() == Globals.CODE_SUCCESS) {
						if (value.getMsg() == null) {
							ToastUtil.shortToast(R.string.network_exception);
							return;
						}
						ToastUtil.shortToast(value.getMsg());
						favor_iv.setClickable(false);
						hasF = true;
						favor_iv.setImageResource(R.drawable.favor_pic_press);
						mWebView.reload();
					} else {
						ToastUtil.shortToast(value.getMsg());
						favor_iv.setClickable(true);
						favor_iv.setImageResource(R.drawable.add_nice);
					}
				}
			}
		}, tidId, hashId);

	}

	private void addScore(View view) {
		if (!DefaultUtil.checkNetWork(this)) {
			return;
		}
		if (!AccountHelper.mLoginStatus) {
			startLogin();
			return;
		}

		final String score;
		if (view.getId() == R.id.add_one_iv) {
			score = "1";
		} else {
			score = "2";
		}
		netManager.addScore(new DataResponse<AddScorerHolder>() {
			@Override
			public void run() {
				super.run();
				if (value != null) {
					if (value.getReturnCode() == Globals.CODE_SUCCESS) {
						if (value.getMsg() == null) {
							ToastUtil.shortToast(R.string.network_exception);
							return;
						}
						ToastUtil.shortToast(R.string.comment_success);
						comment_iv
								.setImageResource(R.drawable.comment_pic_press);
						isRated = true;
						mWebView.reload();
						hasG = true;
						grade = Integer.parseInt(score);
						// comment_iv.setClickable(false);
					} else {
						ToastUtil.shortToast(value.getMsg());
						comment_iv.setImageResource(R.drawable.add_score);
						comment_iv.setClickable(true);
					}
				}
			}
		}, fildId, tidId, hashId, "", score);
		startAlphaAnimation(one_iv, 1, 0, 500, false, false);
		startAlphaAnimation(two_iv, 1, 0, 500, false, false);
	}

	private void getNetData() {

		if (TextUtils.isEmpty(tidId)) {
			return;
		}

		if (!DefaultUtil.checkNetWork(this)) {
			return;
		}
		if (isLoading) {
			return;
		}
		isLoading = true;
		if (AccountHelper.mLoginStatus) {
			// startLogin();
			netManager.getUserInfo(new DataResponse<UserInfoHolder>() {
				@Override
				public void run() {
					super.run();
					if (value != null) {
						detailInfo = value.getData();
						if (detailInfo != null && detailInfo.getSpace() != null) {

							UserInfoSpace userInfoSpace = detailInfo.getSpace();

							userScore = userInfoSpace.getCredits();
							if (userScore == null) {
								userScore = "";
							}
							scoreTv.setText(userScore);
						}
					}
				}
			});

			netManager.queryDetailInfo(new DataResponse<CommentInfoHolder>() {
				@Override
				public void run() {
					super.run();
					if (value != null) {
						if (value.getReturnCode() == Globals.CODE_SUCCESS) {
							isLoading = false;
							loadingPageUtil.hideLoadPage();
							if (value.getData() != null) {
								String rate = value.getData().getRate();
								String favor = value.getData().getRecommend();
								if (rate != null && rate.equals("1")) {
									isRated = true;
									comment_iv
											.setImageResource(R.drawable.comment_pic_press);
									// comment_iv.setClickable(false);
								} else {
									isRated = false;
									comment_iv
											.setImageResource(R.drawable.add_score);
								}
								if (favor != null && favor.equals("1")) {
									isFavour = true;
									favor_iv.setImageResource(R.drawable.favor_pic_press);
									// favor_iv.setClickable(false);
								} else {
									isFavour = false;
									favor_iv.setImageResource(R.drawable.add_nice);
								}
							}
						}
					}
				}
			}, tidId, hashId);

		} else {
			register();
		}
		isLoading = false;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case Globals.NETWORK_ERROR:
			isLoading = false;
			if (loadingPageUtil.isShowing()) {
				loadingPageUtil.showNetworkError();
			} else {
				ToastUtil.longToast(R.string.network_exception);
			}
			break;
		case Globals.NO_NETWORK:
			isLoading = false;
			if (loadingPageUtil.isShowing()) {
				loadingPageUtil.showNoNetWork();
			} else {
				ToastUtil.longToast(R.string.network_not_available);
			}
			break;
		case WEBVIEW_SHOW:
			loadingPageUtil.hideLoadPage();
			break;
		default:
			break;
		}
	}

	private class CreateBitmapTask extends
			AsyncTask<CreateCallBack, Integer, Bitmap> {

		private CreateCallBack mCallBack = null;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(PageDetailActivity.this, "",
					getString(R.string.share_making_pic));
		}

		@Override
		protected Bitmap doInBackground(CreateCallBack... params) {
			this.mCallBack = params[0];
			// Bitmap bitmap =
			// shareCommonUtil.getBitmap(PageDetailActivity.this,
			// lv_post_detail,
			// postDetailHeadView, adapter,
			// -getResources().getDimensionPixelOffset(R.dimen.xlistview_footerview_height));
			// shareCommonUtil.savePic(bitmap,
			// ShareCommonUtil.TEMP_PIC_FILE_PATH);
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			if (result == null) {
				Toast.makeText(PageDetailActivity.this,
						getString(R.string.share_make_pic_fail),
						Toast.LENGTH_SHORT).show();
			} else {
				if (mCallBack != null) {
					mCallBack.onCreatePic(result);
				}
			}
		}

		@Override
		protected void onCancelled() {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}

	}

	private interface CreateCallBack {
		public void onCreatePic(Bitmap bitmap);
	}

	/**
	 * 弹出键盘和隐藏键盘的逻辑处理
	 *
	 * @param isInputModel
	 *            true为弹出键盘 false隐藏键盘
	 */
	private void changeInputModel(boolean isInputModel) {
		if (isInputModel) {
			input_reply_press.requestFocus();
			ll_input_press.setVisibility(View.VISIBLE);
			ll_input_normal.setVisibility(View.GONE);
			startAlphaAnimation(hide_bg, 0, 1, 500, true, true);
		} else {
			ll_input_press.setVisibility(View.GONE);
			ll_input_normal.setVisibility(View.VISIBLE);
			input_reply_normal.setText(input_reply_press.getText());
			startAlphaAnimation(hide_bg, 1, 0, 500, false, true);
		}
	}

	private void startAlphaAnimation(final ImageView iv, float from, float to,
			long duration, final boolean visible, final boolean needReload) {
		AlphaAnimation alphaAnim = new AlphaAnimation(from, to);
		alphaAnim.setDuration(duration);
		alphaAnim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (visible) {
					iv.setVisibility(View.VISIBLE);
				//	mWebView.stopLoading();
				//	mWebView.onPause();
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				
				if (!visible) {
					iv.setVisibility(View.GONE);
					if (needReload && isSending) {
						mWebView.reload();
						isSending = false;
					}
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		iv.startAnimation(alphaAnim);
	}

	private View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {

		@Override
		public void onLayoutChange(View v, int left, int top, int right,
				int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
			// TODO Auto-generated method stub
			if (oldBottom != 0) {
				int bottomDel = bottom - oldBottom;
				if (Math.abs(bottomDel) > THREE_OF_SCREEN_SIZE) {
					if (bottomDel > 0) {
						changeInputModel(false);
					} else {
						changeInputModel(true);
					}
				}
			}
		}
	};
	private WebSettings mSettings;

	private void send() {
		isSending = true;
		String content = null;
		content = input_reply_press.getText().toString();
		if (TextUtils.isEmpty(content.trim())) {
			Toast.makeText(this, R.string.comment_content_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (content.length() < 10) {
			Toast.makeText(this, R.string.comment_content_least,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!AccountHelper.mLoginStatus) {
			startLogin();
			return;
		}
		if (!DefaultUtil.checkNetWork(this)) {
			return;
		}
		/*
		 * if (!AccountHelper.mLoginStatus) { startLogin(); return; }
		 */

		netManager.addComment(new DataResponse<AddCommentHolder>() {
			@Override
			public void run() {
				super.run();
				if (value != null) {
					if (value.getReturnCode() == Globals.CODE_SUCCESS) {
						inputMethodManager.hideSoftInputFromWindow(
								input_reply_press.getWindowToken(), 0);
						resetInputView();
						if (value.getMsg() == null) {
							ToastUtil.shortToast(R.string.network_exception);
							return;
						}
						ToastUtil.longToast(value.getMsg());
						reply++;
					} else {
						ToastUtil.longToast(value.getMsg());

					}
				}
			}
		}, tidId, content, fildId, hashId);
	}

	private void resetInputView() {
		input_reply_normal.setText("");
		input_reply_press.setText("");
	}

	private void startLogin() {
		AccountUtil.getInstance().startLogin(this);
	}

	private void startRegister() {
		AccountUtil.getInstance().register(this);
	}

	@SuppressLint("JavascriptInterface")
	// @SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {

		mWebView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isSharing) {
					closeShare();
					return true;
				}
				if (isRateShow) {
					isRateShow = false;
					one_iv.setVisibility(View.GONE);
					two_iv.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});
		
		mSettings = mWebView.getSettings();
		// fix CN error code
		mSettings.setDefaultTextEncodingName("GBK");
		// enable js
		mSettings.setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new IuniJavaScriptInterface(), "iuni");

		// 设置 缓存模式
		mSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		// mSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		// settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		// 自适应屏幕
		mSettings.setUseWideViewPort(true);
		mSettings.setLoadWithOverviewMode(true);
		// 图片过大时自动适应屏幕
		// mSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		// mSettings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		// 开启 DOM storage API 功能
		mSettings.setAllowFileAccess(true);

		mSettings.setDomStorageEnabled(true);
		// 开启 database storage API 功能
		mSettings.setDatabaseEnabled(true);
		String cacheDirPath = getFilesDir().getAbsolutePath()
				+ Globals.APP_CACAHE_DIRNAME;
		// 设置 Application Caches 缓存目录
		mSettings.setAppCachePath(cacheDirPath);
		// 开启 Application Caches 功能
		// 开启 database storage API 功能
		mSettings.setAppCacheEnabled(true);
		mSettings.setDatabaseEnabled(true);

		mSettings.setSupportZoom(true);
		mSettings.setBuiltInZoomControls(true);
		mSettings.setDisplayZoomControls(false);

		mWebView.setBackgroundColor(Color.parseColor("#00000000"));
		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// url jump inside webView
		mWebView.setWebViewClient(new StoreWebViewClient());
		// enable dialog.alert etc.
		mWebView.setWebChromeClient(new StoreWebChromeClient());
		// interf for js of alipay
		// mWebView.addJavascriptInterface(new AliPay(this, UIHandler),
		// Constants.ALI_PAY);

		SharedPreferences sp = getSharedPreferences(Globals.SETTING_SP, 0);
		String spVersion = sp.getString(Globals.VERSION, "0");
		String currentVersion = SystemUtils.getVersionName(this);
		if (!spVersion.equals(currentVersion)) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Globals.VERSION, currentVersion);
			editor.commit();
			clearWebViewCache();
		}

		mShareUrl = HttpRequstData.getShareURLStr(
				Globals.HTTP_WEB_PAGE_REQUEST_URL, tidId, 1);

		SharedPreferences sp1 = getSharedPreferences(
				MineSettingActivity.IUNIVOICE_SETTING_NAME,
				Context.MODE_PRIVATE);

		if (from_banner == 1) {
			if (post_url_from_banner.contains("?")) {
				url = post_url_from_banner
						+ Globals.HTTP_WEB_PAGE_DETAIL_MOBILE;
			} else {
				url = post_url_from_banner + "?mobile=4";
			}
		} else {
			url = Globals.HTTP_WEB_PAGE_DETAIL_URL_NEW + "&tid=" + tidId
					+ Globals.HTTP_WEB_PAGE_DETAIL_MOBILE;
		}

		if (!SystemUtils.isWifiNetwork()) {
			boolean isOpenLoadingPic = sp1.getBoolean(
					MineSettingActivity.IS_AUTO_SHOW_PIC, true);

			if (!isOpenLoadingPic) {
				url = url + "&nopicture=yes";
			}
		}
		synCookies(this, url);
		Log.v("lmjssjj", "webview.url:" + url);
		mWebView.loadUrl(url);
		// mWebView.loadDataWithBaseURL(null, content, "text/html", "utf-8",
		// null);
	}

	/**
	 * 清除WebView缓存
	 */
	public void clearWebViewCache() {

		/**
		 * 清理Webview缓存数据库 try { deleteDatabase("webview.db");
		 * deleteDatabase("webviewCache.db"); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */

		/**
		 * WebView 缓存目录 File webviewCacheDir = new
		 * File(getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME);
		 * //删除webview 缓存目录 if(webviewCacheDir.exists()){
		 * FileUtils.deleteFile(webviewCacheDir); }
		 */
		mWebView.clearCache(true);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isRateShow && isSharing) {
			closeShare();
			one_iv.setVisibility(View.GONE);
			two_iv.setVisibility(View.GONE);
			isRateShow = false;
			return true;
		}
		if (isSharing) {
			closeShare();
			return true;
		}
		if (isRateShow) {
			isRateShow = false;
			one_iv.setVisibility(View.GONE);
			two_iv.setVisibility(View.GONE);
			return true;
		}
		if (mWebView.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			mWebView.goBack();
			return true;
		}
		if (!mWebView.canGoBack()
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class StoreWebViewClient extends WebViewClient {

		@SuppressLint("DefaultLocale")
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.startsWith("http://bbs.iuni.com")
					&& !url.contains("mobile=4")) {

				if (url.contains("?")) {
					url = url + Globals.HTTP_WEB_PAGE_DETAIL_MOBILE;
				} else {
					url = url + "?mobile=4";
				}
			}
			synCookies(PageDetailActivity.this, url);
			if (url.toLowerCase().startsWith(
					"http://bbs.iuni.com/member.php?mod=logging&action=login")) {

				startLogin();
				System.out.println("1111");
				return true;

			} else if (url.toLowerCase().startsWith(
					"http://bbs.iuni.com/member.php?mod=register")) {
				startRegister();
				return true;
			} else if (url.toLowerCase().startsWith(
					"http://bbs.iuni.com/forum.php?mod=attachment")) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
				return true;
			} else if (url
					.startsWith("http://bbs.iuni.com/plugin.php?id=dsu_paulsign:sign")) {
				return true;
			} else if (url
					.startsWith("http://bbs.iuni.com/forum.php?mod=viewthread")
					|| url.startsWith("http://bbs.iuni.com/thread")) {
				view.loadUrl(url);
				return true;
			} else if (url.toLowerCase().startsWith("http:")
					|| url.toLowerCase().startsWith("https:")
					|| url.toLowerCase().startsWith("file:")) {

				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
				return true;

				// view.loadUrl(url);
				// return true;
			} else if (url.startsWith(Globals.SCHEME_WTAI_MC)) {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(WebView.SCHEME_TEL
									+ url.substring(Globals.SCHEME_WTAI_MC
											.length())));
					startActivity(intent);
					return true;
				} catch (Exception e) {
				}
			}

			return false;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
			if (SystemUtils.hasNetwork()) {
				loadingPageUtil.showLoading();
				new GetHttp(tidId).execute();
			} else {
				loadingPageUtil.showNoNetWork();
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			if (SystemUtils.hasNetwork()) {
				getNetData();
				//mHandler.sendEmptyMessageDelayed(WEBVIEW_SHOW, 100);
//				for(int i=100;i<10;i++){
//					mWebView.scrollBy(0, i);
//					mWebView.scrollBy(0, -i);
//					mWebView.scrollBy(0, 2*i);
//				})
//				mWebView.scrollTo(0, 0);
				loadingPageUtil.hideLoadPage();
			} else {
				loadingPageUtil.showNoNetWork();
			}

		}

		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
		//	Log.v("lmjssjj","WebView资源路径Url="+url);
			
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {

			Log.v("lmjssjj", "errorCode" + errorCode);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

	}

	private class StoreWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			return super.onJsAlert(view, url, message, result);
		}
	}

	/**
	 * 同步一下cookie
	 */
	public void synCookies(Context context, String url) {
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeAllCookie();
		cookieManager.removeSessionCookie();// 移除
		cookieManager.setCookie(url, "tgt=" + AccountHelper.cookie);// cookies是在HttpClient中获得的cookie
		cookieManager.setCookie(url, "bbsapp=" +1);// cookies是在HttpClient中获得的cookie
		CookieSyncManager.getInstance().sync();
	}

	final class IuniJavaScriptInterface {
		IuniJavaScriptInterface() {
		}

		public void picBrowserBack() {
			runOnUiThread(new Runnable() {
				public void run() {
					mWebView.goBack();
				}
			});
		}

		@JavascriptInterface
		public void browserPic(final String[] baseUrls, final int currentPager) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					 Log.v("lmjssjj",
//					 baseUrls[0]+"-=-=-=-=-=-="+baseUrls.length);
					if (baseUrls == null || baseUrls.length <= 0) {
						return;
					}
					if(baseUrls[0]==null){
						return;
					}
					Intent picIntent = new Intent(PageDetailActivity.this,
							PageDetailPicBrowserActivity.class);
					picIntent.putExtra("urls", baseUrls);
					picIntent.putExtra("position", currentPager);
					PageDetailActivity.this.startActivity(picIntent);
				}
			});

		}

		@JavascriptInterface
		public void login() {
			startLogin();
		}

		@JavascriptInterface
		public void register() {
			startRegister();
		}

		@JavascriptInterface
		public void getPostTidFromBanner(String tid) {
			tidId = tid;
			if (from_banner == 1) {
				getNetData();
			}
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == Globals.REQUEST_LOGIN_CODE) {
			if (arg1 == RESULT_OK) {
				getNetData();
				initWebView();
			}
		}

	}

	public void back() {
		Intent data = new Intent();
		data.putExtra(ForumDatailActivity.FORUM_FAVOUR, hasF);
		data.putExtra(ForumDatailActivity.FORUM_COMMEND, hasG);
		data.putExtra(ForumDatailActivity.FORUM_GRADE, grade);
		data.putExtra(ForumDatailActivity.FORUM_REPLY, reply);
		data.putExtra("tid", tidId);
		setResult(RESULT_OK, data);
		finish();
	}

	// Intent b = new Intent(Globals.LOCAL_LOGIN_ACTION);
	// b.putExtra(Globals.LOCAL_LOGIN_RESULT, Globals.LOCAL_LOGIN_SUCCESS);
	// sendBroadcast(b);
	private LoginReceiver mReceiver;
	private IntentFilter mFilter;

	public void register() {
		if (mReceiver == null) {
			mReceiver = new LoginReceiver();
			mFilter = new IntentFilter();
			mFilter.addAction(Globals.LOCAL_LOGIN_ACTION);
		}
		registerReceiver(mReceiver, mFilter);
	}

	public void unRegister() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
			mFilter = null;
		}
	}

	class LoginReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			getNetData();
			initWebView();
			// mWebView.reload();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegister();
	}

	class GetHttp extends AsyncTask<Void, Void, String> {

		private HttpURLConnection mConn;
		private URL mUrl;
		private String urlStr;

		public GetHttp(String tid) {
			if (from_banner == 1) {
				if (post_url_from_banner.contains("?")) {
					urlStr = post_url_from_banner
							+ Globals.HTTP_WEB_PAGE_DETAIL_MOBILE;
				} else {
					urlStr = post_url_from_banner + "?mobile=4";
				}
			} else {
				urlStr = Globals.HTTP_WEB_PAGE_DETAIL_URL_NEW + "&tid=" + tidId
						+ Globals.HTTP_WEB_PAGE_DETAIL_MOBILE;
			}
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(Void... v) {
			// String urlStr = params[0];
			try {
				mUrl = new URL(urlStr);
				mConn = (HttpURLConnection) mUrl.openConnection();

				BufferedReader in = null;
				String content = "";
				StringBuilder builder = new StringBuilder();
				mConn.setRequestMethod("GET");
				mConn.setConnectTimeout(10000);
				mConn.setReadTimeout(10000);

				mConn.connect();
				// int length = conn.getContentLength();
				int stutas = mConn.getResponseCode();

				if (stutas == 200) {

					InputStream inStream = mConn.getInputStream();
//					in = new BufferedReader(new InputStreamReader(inStream,
//							"UTF-8"));
					byte[] buff = new byte[1024*4];
					int len = inStream.read(buff);
					
//					while ((content = in.readLine()) != null) {
//						builder.append(content);
//					}
					inStream.close();
//					int leng = builder.toString().getBytes("UTF-8").length;
//					String result = builder.toString();
					String result = new String(buff);

					return result;

				} else {

					return null;
				}
			} catch (HttpHostConnectException e) {
				e.printStackTrace();
			} catch (ConnectException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mConn.disconnect();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			//System.out.println(result);
			if (result != null) {
				if (result.contains("您访问的页面无手机页面")) {
					isCanShow = false;
					replyInputView();
				} else {
					isCanShow = true;
					replyInputView();
				}
			}
		}

	}

}