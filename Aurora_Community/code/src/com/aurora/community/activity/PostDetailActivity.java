package com.aurora.community.activity;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.twitter.TwitterNoteActivity;
import com.aurora.community.adapter.CommentOfPostDetailAdapter;
import com.aurora.community.share.ShareCommonUtil;
import com.aurora.community.share.ShareCommonUtil.AuroraDilogCallBack;
import com.aurora.community.share.weibo.WeiboShareUtil;
import com.aurora.community.share.weibo.WeiboShareUtil.AuroraWeiBoCallBack;
import com.aurora.community.share.wx.WxShareUtil;
import com.aurora.community.totalCount.TotalCount;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.AccountUtil;
import com.aurora.community.utils.BitmapUtil;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.DialogUtil;
import com.aurora.community.utils.DialogUtil.IAlertDialogClick;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.TimeUtils;
import com.aurora.community.widget.AuroraEditText;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.AddCommentHolder;
import com.aurora.datauiapi.data.bean.AddFavourHolder;
import com.aurora.datauiapi.data.bean.CancelFavourHolder;
import com.aurora.datauiapi.data.bean.CommentHolder;
import com.aurora.datauiapi.data.bean.CommentInfo;
import com.aurora.datauiapi.data.bean.ImageInfo;
import com.aurora.datauiapi.data.bean.PostDeleteHolder;
import com.aurora.datauiapi.data.bean.PostDetailHolder;
import com.aurora.datauiapi.data.bean.PostDetailInfo;
import com.aurora.datauiapi.data.bean.TagInfo;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.sdk.platformtools.Log;
import com.umeng.analytics.MobclickAgent;
public class PostDetailActivity extends BaseActivity{

	private PullToRefreshListView lv_post_detail;
	
	private List<CommentInfo> datas = new ArrayList<CommentInfo>();
	private CommentOfPostDetailAdapter adapter;
	
	private View postDetailHeadView;
	private LayoutInflater mInflater;
	private HeadViewHolder mHeadViewHolder;
	private CommunityManager netManager;
	
	private TextView comment_input_text_count;
	private AuroraEditText input_reply,input_reply_common;
	private LinearLayout ll_input_reply,ll_input_common;
	private InputMethodManager inputMethodManager;
	
	private String pageId,userId,comeFrom;
	public static final String PAGE_ID_KEY = "page_id",
			                      USER_ID_KEY = "user_id",
			                      COME_FROM_KEY = "come_from";
	
	private int page = 1;
	private static final int REQUEST_COUNT_EACHTIME = 20; 
	
	private int[] collectionRes = {R.drawable.collection,R.drawable.uncollection};
	private boolean isFavour = false;
	
	private WxShareUtil wxShareUtil;
	private WeiboShareUtil weiboShareUtil;
	private ShareCommonUtil shareCommonUtil;
	
	private RelativeLayout network_layout,rl_content,loading_layout;
	
	private int bitmapWidth;
	
	public static final int RESULT_BACK = 0x1212;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_detail);
		pageId = getIntent().getStringExtra(PAGE_ID_KEY);
//		userId = getIntent().getStringExtra(USER_ID_KEY);
		comeFrom = getIntent().getStringExtra(COME_FROM_KEY);
		replyNameColor = getResources().getColor(R.color.postdetail_replyname_color);
		commentContentColor = getResources().getColor(R.color.gloab_title_color);
		setupViews();
		initShareUtil();
		setTitleRes(R.string.postdetail_title);
		if(!DefaultUtil.checkNetWork(this))
		{
			showNetworkErrorLayout();
		}else{
			rl_content.setVisibility(View.GONE);
			loading_layout.setVisibility(View.VISIBLE);
			reqPageDetail();
			reqCommentList();
			hideNetworkErrorLayout();
		}
	}
	
	public String getComeFrom(){
		return comeFrom;
	}
	
	private void setActionBarItem(){
		removeActionBarItem(ACTION_ID_MORE);
		removeActionBarItem(ACTIONBAR_ID_SHARE);
		removeActionBarItem(ACTIONBAR_ID_COLLECTION);
		if(AccountHelper.user_id.equals(userId))
		{
			addActionBarItem(R.drawable.aurora_more, ACTION_ID_MORE);
		}
		addActionBarItem(R.drawable.drawable_share, ACTIONBAR_ID_SHARE);
		addActionBarItem(R.drawable.collection, ACTIONBAR_ID_COLLECTION);
	}
	
	private boolean isSetUpActionBarItem(){
		if(AccountHelper.user_id.equals(userId))
		{
			return getActionBarItem(ACTION_ID_MORE) != null;
		}
	   return getActionBarItem(ACTIONBAR_ID_COLLECTION) != null;
	}
	
	private void startLogin() {
		AccountUtil.getInstance().startLogin(this);

	}
	
	private String replyName = "";
	private int replyNameColor ,commentContentColor;
	private void createColorString(){
		if(!TextUtils.isEmpty(replyName))
		{
			StringBuffer buff = new StringBuffer();
			buff.append(replyName);
			SpannableStringBuilder builder = new SpannableStringBuilder(buff);
			builder.setSpan(new ForegroundColorSpan(replyNameColor), 0, replyName.length()-1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			input_reply.setText(builder);
		}
	}
	
	private void showNetworkErrorLayout(){
		network_layout.setVisibility(View.VISIBLE);
		rl_content.setVisibility(View.GONE);
		loading_layout.setVisibility(View.GONE);
	}
	
	private void hideNetworkErrorLayout(){
		network_layout.setVisibility(View.GONE);
//		rl_content.setVisibility(View.VISIBLE);
	}
	
	private int THREE_OF_SCREEN_SIZE;//屏幕高度的3分之一，以此来判断是否弹出键盘
	private ImageView hide_bg;
	private RelativeLayout rl_input_reply;
	
	private boolean hasModify = false;
	
	private static final int ACTIONBAR_ID_SHARE = 0x4521,//顶部分享按钮
			                    ACTIONBAR_ID_COLLECTION = 0X4584,
			                    ACTION_ID_MORE = 0X4474;
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			if(hasModify)
			{
				setResult(RESULT_OK);
			}else{
				setResult(RESULT_CANCELED);
			}
			finish();
			break;
		case ACTIONBAR_ID_SHARE:
			if(!AccountHelper.mLoginStatus)
			{
				startLogin();
			}else{
			/*	new TotalCount(PostDetailActivity.this, "300", "013", 1)
				.CountData();*/
				MobclickAgent.onEvent(PostDetailActivity.this, Globals.PREF_TIMES_SHARE);
			    shareContent();
			}
			break;
		case ACTION_ID_MORE:
			showMenu();
			break;
		case ACTIONBAR_ID_COLLECTION:
			if(!AccountHelper.mLoginStatus)
			{
				startLogin();
			}else{
				/*new TotalCount(PostDetailActivity.this, "300", "012", 1)
				.CountData();*/
				MobclickAgent.onEvent(PostDetailActivity.this, Globals.PREF_TIMES_COLLECT);
				if (isFavour) {
					cancelFavour();
				} else {
					addFavour();
				}
			}
			break;
			
		default:
			break;
		}
		
		
	}
	
	
	
	private static final int MENU_DELETE_ID = 0X41254,
			                     MENU_EDIT_ID = 0X45211;
	
	private void setupMenu(){
		addMenu(getString(R.string.postdetail_more_edit));
		addMenu(getString(R.string.postdetail_more_delete));
	}
	
	@Override
	public void onMenuClick(int position, String menuText) {
		
		if(position == 0)
		{
			toEditPage();
		}else{
			showDeleteTipDialog();
		}
		
	};
	
	
    private Dialog alertDialog;
    
    private void showDeleteTipDialog(){
		if(alertDialog == null)
		{
			alertDialog = DialogUtil.getAlertDialog(this, getString(R.string.delete_tip_msg), R.string.delete_tip_cancel, R.string.delete_tip_delete,alertDialogClick);
		}
		alertDialog.show();
	   }
	
    private IAlertDialogClick alertDialogClick = new IAlertDialogClick() {
		
		@Override
		public void sureClick() {
			// TODO Auto-generated method stub
			deletePost();
		}
		
		@Override
		public void cancelClick() {
			// TODO Auto-generated method stub
			
		}
	};
    
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		for (Bitmap bitmap : bitmaps) {
			if(!bitmap.isRecycled())
			{
				bitmap.recycle();
				bitmap = null;
				System.gc();
			}
		}
		bitmaps.clear();
	}
	
	
	public static final String POST_DETAIL_KEY = "postdetail";
	
	private static final int EDIT_REQUEST_CODE = 0X1444;
	
	private void toEditPage(){
		Intent intent = new Intent(this, TwitterNoteActivity.class);
		intent.putExtra(POST_DETAIL_KEY, postDetail);
		startActivity(intent);
		hasModify = true;
		toEditablePage = true;
	}
	
	private boolean toEditablePage = false;
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(toEditablePage)
		{
			reqPageDetail();
			toEditablePage = false;
		}
	}
	
	private void deletePost(){
		if(!DefaultUtil.checkNetWork(this))
		{
			return;
		}
		netManager.deletePost(new DataResponse<PostDeleteHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						sendRefreshBroad();
						setResult(RESULT_OK);
						finish();
					}
					Toast.makeText(PostDetailActivity.this, value.getMsg(), 200).show();
				}
				
			}
		}, pageId);
	}
	
	private void sendRefreshBroad(){
		Intent intent = new Intent(MainActivity.REFRESH_MAIN_CATEGORY_ACTION);
		sendBroadcast(intent);
	}
	
	
	private void shareContent()
	{
		showSendConfirmDialog(R.string.share_title);
	}
	
	@Override
	public void setupViews() {
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		netManager = new CommunityManager(this);
		mInflater = LayoutInflater.from(this);
		network_layout = (RelativeLayout) findViewById(R.id.network_layout);
		rl_content = (RelativeLayout) findViewById(R.id.rl_content);
		loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
		findViewById(R.id.bt_retry_network).setOnClickListener(onClickListener);
		setupActivityView();
		setupHeadView();
		setupListViews();
		setupMenu();
	}
	
	
	private void setupActivityView(){
//		imageLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)getResources().getDimension(R.dimen.publish_image_of_postetail_height));
		imageLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		imageLP.topMargin = (int)getResources().getDimension(R.dimen.publish_image_of_postetail_topmargin);
		comment_input_text_count = (TextView)findViewById(R.id.comment_input_text_count);
		comment_input_text_count.setText(MAX_INPUT+"");
		input_reply = (AuroraEditText)findViewById(R.id.input_reply);
		input_reply.addTextChangedListener(watcher);
		input_reply_common = (AuroraEditText)findViewById(R.id.input_reply_common);
		inputHeight = (int)getResources().getDimension(R.dimen.postdetail_input_height_input);
		commonHeight = (int)getResources().getDimension(R.dimen.postdetail_input_height_common);
	    findViewById(R.id.btn_send_reply).setOnClickListener(onClickListener);
	    findViewById(R.id.btn_send_reply_common).setOnClickListener(onClickListener);
		ll_input_reply = (LinearLayout)findViewById(R.id.ll_input_reply);
		ll_input_common = (LinearLayout)findViewById(R.id.ll_input_common);
		hide_bg = (ImageView)findViewById(R.id.hide_bg);
		hide_bg.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		THREE_OF_SCREEN_SIZE = getResources().getDisplayMetrics().heightPixels/3;
		rl_input_reply = (RelativeLayout)findViewById(R.id.rl_input_reply);
		rl_input_reply.addOnLayoutChangeListener(layoutChangeListener);
	}
	
	private void setupListViews(){
		lv_post_detail = (PullToRefreshListView)findViewById(R.id.lv_post_detail);
		lv_post_detail.addHeaderView(postDetailHeadView);
		adapter = new CommentOfPostDetailAdapter(datas, this);
		lv_post_detail.setOnItemClickListener(onItemClickListener);
		lv_post_detail.setAdapter(adapter);
		lv_post_detail.setPullRefreshEnable(false);
		lv_post_detail.setPullLoadEnable(false);
		lv_post_detail.setXListViewListener(listViewListener);
	}
	
	private IXListViewListener listViewListener = new IXListViewListener() {
		
		@Override
		public void onRefresh() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLoadMore() {
			// TODO Auto-generated method stub
			page++;
			reqCommentList();
		}
	};
	
	
	private OnLayoutChangeListener layoutChangeListener = new OnLayoutChangeListener() {
		
		@Override
		public void onLayoutChange(View v, int left, int top, int right,
				int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
			// TODO Auto-generated method stub
			if(oldBottom != 0)
			{
				int bottomDel = bottom - oldBottom;
				if(Math.abs(bottomDel)>THREE_OF_SCREEN_SIZE)
				{
					if(bottomDel > 0)
					{
						changeInputModel(false);
					}else{
						changeInputModel(true);
					}
				}
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Globals.REQUEST_LOGIN_CODE)
		{
			if(resultCode == RESULT_OK)
			{
				reqPageDetail();
			}
		}
		
	};
	
	private int inputHeight,commonHeight;
	private LinearLayout.LayoutParams inputLP;
	
	/**
	 * 弹出键盘和隐藏键盘的逻辑处理
	 * @param isInputModel true为弹出键盘 false隐藏键盘
	 */
	private void changeInputModel(boolean isInputModel){
		if(isInputModel)
		{
			ll_input_reply.setVisibility(View.VISIBLE);
			ll_input_common.setVisibility(View.GONE);
			hide_bg.setVisibility(View.VISIBLE);
			input_reply.requestFocus();
		}else{
			ll_input_reply.setVisibility(View.GONE);
			ll_input_common.setVisibility(View.VISIBLE);
			hide_bg.setVisibility(View.GONE);
			input_reply_common.setText(input_reply.getText());
		}
	}
	
	private static final int MAX_INPUT = 100;
	
	private void resetInputView(){
		replyCid = null;
		replyName = null;
		replyNameLength = 0;
		input_reply_common.setText("");
		input_reply.setText("");
		comment_input_text_count.setText(MAX_INPUT+"");
		input_reply.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_INPUT)});
	}
	
	private int replyNameLength = 0;
	
	private TextWatcher watcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
				comment_input_text_count.setVisibility(View.VISIBLE);
				comment_input_text_count.setText((MAX_INPUT-s.length()+replyNameLength)+"");
				if(replyNameLength != 0 && s.length() <replyNameLength)
				{
					resetInputView();
				}
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	
	
	private void setupHeadView(){
		mHeadViewHolder = new HeadViewHolder();
		postDetailHeadView = mInflater.inflate(R.layout.headview_of_postdetail, null);
		bitmapWidth = getResources().getDisplayMetrics().widthPixels-postDetailHeadView.getPaddingLeft()-postDetailHeadView.getPaddingRight();
		mHeadViewHolder.user_icon = (ImageView)postDetailHeadView.findViewById(R.id.user_icon);
		mHeadViewHolder.user_icon.setOnClickListener(onClickListener);
		mHeadViewHolder.publish_images_container = (LinearLayout)postDetailHeadView.findViewById(R.id.publish_images_container);
		mHeadViewHolder.publish_content = (AuroraEditText)postDetailHeadView.findViewById(R.id.publish_content);
		mHeadViewHolder.publish_content.setFocusable(true);
		mHeadViewHolder.screen_name = (TextView)postDetailHeadView.findViewById(R.id.screen_name);
		mHeadViewHolder.screen_name.setOnClickListener(onClickListener);
		mHeadViewHolder.publish_time = (TextView)postDetailHeadView.findViewById(R.id.publish_time);
		mHeadViewHolder.collection_count = (TextView)postDetailHeadView.findViewById(R.id.collection_count);
		mHeadViewHolder.comment_count = (TextView)postDetailHeadView.findViewById(R.id.comment_count);
		mHeadViewHolder.label_content = (TextView) postDetailHeadView.findViewById(R.id.label_content);
	}
	
	private LinearLayout.LayoutParams imageLP = null;
	private PostDetailInfo postDetail;
	private void setHeadViewData(PostDetailInfo detail){
		userId = detail.getUid();
		if(!isSetUpActionBarItem())
		{
		   setActionBarItem();
		}
		isFavour = Integer.parseInt(detail.getIsFavor())==1;
		setFavourIcon();
		fillPublishImages(detail.getImages());
		mHeadViewHolder.label_content.setText(getTags(detail.getTags()));
		ImageLoaderHelper.disPlay(detail.getUid_avatar(), mHeadViewHolder.user_icon, DefaultUtil.getDefaultUserDrawable(this));
		mHeadViewHolder.collection_count.setText(detail.getFavorites());
		mHeadViewHolder.comment_count.setText(detail.getReplies());
		if(TextUtils.isEmpty(detail.getBody()))
		{
			mHeadViewHolder.publish_content.setVisibility(View.GONE);
		}else{
			mHeadViewHolder.publish_content.setVisibility(View.VISIBLE);
			mHeadViewHolder.publish_content.setText(detail.getBody());
		}
		mHeadViewHolder.screen_name.setText(detail.getUid_nick());
		mHeadViewHolder.publish_time.setText(TimeUtils.getDataTimeFromLongOth(Long.parseLong(detail.getCreated())));
	}
	
	private static final String EMPTY = "  ";
	private String getTags(ArrayList<TagInfo> tags){
		StringBuffer tagStr = new StringBuffer();
		for(int i = 0;i<tags.size();i++)
		{
			tagStr.append(tags.get(i).getTsname());
			
			if(i != tags.size() -1)
			{
				tagStr.append(EMPTY);
			}
			
		}
		return tagStr.toString();
	}
	
	private static final int MAX_SUPPORT_HEIGHT = 4096;
	private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
	private void fillPublishImages(List<ImageInfo> imageUrls){
		if(imageUrls == null)
		{
			return;
		}
		mHeadViewHolder.publish_images_container.removeAllViews();
		this.imageUrls.clear();
		for(int i=0;i<imageUrls.size();i++)
		{
			this.imageUrls.add(imageUrls.get(i).getFpath());
			final ImageView image = new ImageView(this);
			image.setLayoutParams(imageLP);
			image.setTag(i);
			image.setScaleType(ScaleType.CENTER_CROP);
			image.setOnClickListener(imageClickListener);
			mHeadViewHolder.publish_images_container.addView(image);
			ImageLoaderHelper.loadImage(imageUrls.get(i).getFpath(), new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
					float scale = (float)bitmapWidth/loadedImage.getWidth();
					loadedImage = BitmapUtil.getBitmap(loadedImage, bitmapWidth, (int) (loadedImage.getHeight()*scale) > MAX_SUPPORT_HEIGHT ? MAX_SUPPORT_HEIGHT:(int) (loadedImage.getHeight()*scale));
					bitmaps.add(loadedImage);
					image.setImageBitmap(loadedImage);
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	private ArrayList<String> imageUrls = new ArrayList<String>();
	
	private OnClickListener imageClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if(imageUrls.size() == 0)
				return;
			// TODO Auto-generated method stub
			int position = (Integer) arg0.getTag();
			Intent intent = new Intent(PostDetailActivity.this, PostDetailImagePreviewActivity.class);
			intent.putExtra(PostDetailImagePreviewActivity.CURRENT_POSITION_KEY, position);
			intent.putStringArrayListExtra(PostDetailImagePreviewActivity.IMAGE_URLS_KEY,imageUrls);
			startActivity(intent);
		}
	};
	
	class HeadViewHolder{
		ImageView user_icon;
		TextView screen_name,
		         publish_time,
		         collection_count,
		         comment_count,
		         label_content;
		AuroraEditText publish_content;
		LinearLayout publish_images_container;
	}
	
	private String replyCid;
	private void send(){
		String content = null;
		if(!TextUtils.isEmpty(replyName))
		{
			String inputContent = input_reply.getText().toString();
			content = inputContent.substring(replyName.length()-1, inputContent.length());
		}else{
			content = input_reply.getText().toString();
		}
		if(TextUtils.isEmpty(content.trim()))
		{
			Toast.makeText(this, R.string.comment_content_empty, 200).show();
			return;
		}
		if(!DefaultUtil.checkNetWork(this))
		{
			return;
		}
		
		if(!AccountHelper.mLoginStatus)
		{
			startLogin();
			return;
		}
		netManager.addComment(new DataResponse<AddCommentHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						hasModify = true;
						if(postDetail != null)
						{
							postDetail.replyAdd();
							setHeadViewData(postDetail);
						}
						inputMethodManager.hideSoftInputFromWindow(input_reply.getWindowToken(), 0);
						datas.add(0,value.getData());
						adapter.notifyDataSetChanged();
						lv_post_detail.setSelection(2);
						resetInputView();
					}
				}
			}
		}, pageId, content, replyCid);
	}
	
	private OnClickListener onClickListener =new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_send_reply_common:
				send();
				break;
			case R.id.btn_send_reply:
				send();
				break;
			case R.id.bt_retry_network:
				if(!DefaultUtil.checkNetWork(PostDetailActivity.this))
				{
					return;
				}
				reqPageDetail();
				reqCommentList();
				break;
			case R.id.user_icon:
				toUserCenter();
				break;
			case R.id.screen_name:
				toUserCenter();
				break;
			default:
				break;
			}
			
		}
	};
	
	private void toUserCenter(){
		
		if(postDetail == null)
		{
			return;
		}
		
//		if(getComeFrom() != null && getComeFrom().equals(MessageBoxActivity.class.getName()))
//		{
//			setResult(PostDetailActivity.RESULT_BACK);
//			finish();
//		}else{
//			finish();
//			CommunityApp.getInstance().getinstance().setDisView(PostDetailActivity.class.getName());
//		}
		
		
		if(userId.equals(AccountHelper.user_id))
		{
			if(getComeFrom() != null &&getComeFrom().equals(MessageBoxActivity.class.getName()))
			{
				setResult(PostDetailActivity.RESULT_BACK);
				finish();
			}else{
				finish();
				CommunityApp.getInstance().getinstance().setDisView(PostDetailActivity.class.getName());
			}
		}else{
			Intent intent =new Intent(this,OtherUserCenterActivity.class);
			intent.putExtra(OtherUserCenterActivity.USER_ID_KEY, userId);
			intent.putExtra(OtherUserCenterActivity.USER_NICK_KEY, postDetail.getUid_nick());
			startActivity(intent);
		}
		
		
//		if(userId.equals(AccountHelper.user_id))
//		{
//			/*Intent intent =new Intent(mContext,MainActivity.class);
//			intent.putExtra(MainActivity.COME_FROM_KEY, PostDetailActivity.class.getName());
//			mContext.startActivity(intent);*/
//			finish();
//			CommunityApp.getInstance().getinstance().setDisView(PostDetailActivity.class.getName());
//			
//		}else{
//			Intent intent =new Intent(PostDetailActivity.this,OtherUserCenterActivity.class);
//			intent.putExtra(OtherUserCenterActivity.USER_ID_KEY, userId);
//			intent.putExtra(OtherUserCenterActivity.USER_NICK_KEY, postDetail.getUid_nick());
//			startActivity(intent);
//		}
		
	}
	
	
	private void addFavour(){
		if(!DefaultUtil.checkNetWork(this))
		{
			return;
		}
		if(!AccountHelper.mLoginStatus)
		{
			startLogin();
		}
		netManager.addFavour(new DataResponse<AddFavourHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						hasModify = true;
						if(postDetail != null)
						{
							postDetail.favourAdd();
							postDetail.revertIsFavor();
							setHeadViewData(postDetail);
						}
					}
					Toast.makeText(PostDetailActivity.this, value.getMsg(), 200).show();
				}
				
			}
		}, pageId);
	}
	
	private void cancelFavour(){
		if(!DefaultUtil.checkNetWork(this))
		{
			return;
		}
		if(!AccountHelper.mLoginStatus)
		{
			startLogin();
		}
		netManager.cancelFavour(new DataResponse<CancelFavourHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					hasModify = true;
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						if(postDetail != null)
						{
							postDetail.favourReduce();
							postDetail.revertIsFavor();
							setHeadViewData(postDetail);
						}
					}
					Toast.makeText(PostDetailActivity.this, value.getMsg(), 200).show();
				}
				
			}
		}, pageId);
	}
	
	private void setFavourIcon(){
		if(isFavour)
		{
			changeActionBarItemImageRes(collectionRes[0], ACTIONBAR_ID_COLLECTION);
		}else{
			changeActionBarItemImageRes(collectionRes[1], ACTIONBAR_ID_COLLECTION);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(hasModify)
			{
				setResult(RESULT_OK);
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(view.getId() == R.id.postdetail_headview)
			{
				
			}else{
				replyCid = datas.get((int)id).getCid();
				replyName = getString(R.string.reply_user_nick, datas.get((int)id).getUid_nick());
				replyNameLength = replyName.length();
				createColorString();
				input_reply.setSelection(replyNameLength);
				input_reply.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_INPUT+replyNameLength)});
				inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
			}
		}
		
	};
	
	private void reqCommentList(){
		if(!DefaultUtil.checkNetWork(this))
		{
			showNetworkErrorLayout();
			return;
		}else{
			hideNetworkErrorLayout();
		}
		netManager.getCommentList(new DataResponse<CommentHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						datas.addAll(value.getData().getDataContext());
						adapter.notifyDataSetChanged();
						if(Integer.parseInt(value.getData().getCountRow()) <=datas.size())
						{
							lv_post_detail.setPullLoadEnable(false);
						}else{
							lv_post_detail.setPullLoadEnable(true);
						}
					}
					lv_post_detail.stopLoadMore();
				}
			}
		}, pageId, page, REQUEST_COUNT_EACHTIME);
	}
	
	private void reqPageDetail(){
		if(!DefaultUtil.checkNetWork(this))
		{
			showNetworkErrorLayout();
			return;
		}else{
			hideNetworkErrorLayout();
		}
		netManager.getPostDetail(new DataResponse<PostDetailHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					if(value.getReturnCode() == Globals.CODE_SUCCESS)
					{
						postDetail = value.getData().getPost();
						setHeadViewData(value.getData().getPost());
						rl_content.setVisibility(View.VISIBLE);
						loading_layout.setVisibility(View.GONE);
					}else{
						Log.e("jadon1", value.getReturnCode() +"        "+value.getMsg());
					}
				}
			}
		}, pageId);
		
		
	}
	
	private Handler errorHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			lv_post_detail.stopLoadMore();
			showNetworkErrorLayout();
		};
	};
	
	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		super.onError(code, message, manager, e);
		Log.e("jadon1", "code = "+code+"      message = "+message);
		errorHandler.sendEmptyMessage(0);
	}
	
	//＝＝＝＝＝＝＝＝＝＝＝ 分享相关代码 ＝＝＝＝＝＝＝＝＝＝＝
	
		private void initShareUtil() {
			wxShareUtil = new WxShareUtil(this);
			wxShareUtil.registerApp(this);
//			weiboShareUtil = WeiboShareUtil.getInstance(this);
			shareCommonUtil = new ShareCommonUtil();
		}
		
		private void showSendConfirmDialog(final int messageId) {
			
			shareCommonUtil.setCallback(new AuroraDilogCallBack() {
				
				@Override
				public void onFinishDialog(int ret) {
					if (ret == 0) {// 微信会话
						shareOnWeiXin(ret);
					} else if (ret == 1) {// 朋友圈
						shareOnWeiXin(ret);
					} else if (ret == 2) {// 微薄
						shareOnXLWb();
					} else {// 其他
//						createShareIntent();
					}
				}
			});
			
			shareCommonUtil.showShareDialog(this, messageId);
		}
		
		ProgressDialog progressDialog = null;
		
		private void shareOnWeiXin(final int type) {
			if (wxShareUtil.isSupport(this)) {
				new CreateBitmapTask().execute(new CreateCallBack() {
					@Override
					public void onCreatePic(Bitmap bitmap) {
						wxShareUtil.sendImage(PostDetailActivity.this, null, 
								ShareCommonUtil.TEMP_PIC_FILE_PATH, null, type == 1 ? true : false);
					}
				});
			} else {
				Toast.makeText(this, getString(R.string.share_wx_not_support), Toast.LENGTH_SHORT).show();
			}
		}
		
		private void shareOnXLWb() {
			if (weiboShareUtil.isAuth()) {
				new CreateBitmapTask().execute(new CreateCallBack() {
					@Override
					public void onCreatePic(Bitmap bitmap) {
						weiboShareUtil.sendImageMessage("", BitmapFactory.decodeFile(ShareCommonUtil.TEMP_PIC_FILE_PATH));
					}
				});
			} else {
				weiboShareUtil.authWeibo(this, new AuroraWeiBoCallBack() {
					@Override
					public void onSinaWeiBoCallBack(int ret) {
						if (ret == WeiboShareUtil.AURORA_WEIBO_SUCCESS) {
							new CreateBitmapTask().execute(new CreateCallBack() {
								@Override
								public void onCreatePic(Bitmap bitmap) {
									weiboShareUtil.sendImageMessage("", BitmapFactory.decodeFile(ShareCommonUtil.TEMP_PIC_FILE_PATH));
								}
							});
						}
					}
				});
			}
			
		}
		
		private class CreateBitmapTask extends AsyncTask<CreateCallBack, Integer, Bitmap> {
			
			private CreateCallBack mCallBack = null;

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(PostDetailActivity.this, "", getString(R.string.share_making_pic));
			}

			@Override
			protected Bitmap doInBackground(CreateCallBack... params) {
				this.mCallBack = params[0];
				Bitmap bitmap = shareCommonUtil.getBitmap(PostDetailActivity.this, lv_post_detail, 
						postDetailHeadView, adapter, -getResources().getDimensionPixelOffset(R.dimen.xlistview_footerview_height));
				shareCommonUtil.savePic(bitmap, ShareCommonUtil.TEMP_PIC_FILE_PATH);
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				
				if (result == null) {
					Toast.makeText(PostDetailActivity.this, getString(R.string.share_make_pic_fail), 
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
		
		//＝＝＝＝＝＝＝＝＝＝＝ 分享相关代码 ＝＝＝＝＝＝＝＝＝＝＝

}
