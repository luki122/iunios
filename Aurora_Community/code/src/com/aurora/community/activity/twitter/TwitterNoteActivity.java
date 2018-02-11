package com.aurora.community.activity.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.BaseActivity;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.activity.PostDetailActivity;
import com.aurora.community.activity.picBrowser.ImagePickerActvity;
import com.aurora.community.adapter.TwitterNoteSelectedPhotoAdapter;
import com.aurora.community.bean.PhotoSerializable;
import com.aurora.community.common.MessageHandler;
import com.aurora.community.common.WeakHandler;
import com.aurora.community.totalCount.TotalCount;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.AccountUtil;
import com.aurora.community.utils.BitmapUtil;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.DensityUtil;
import com.aurora.community.utils.DialogUtil;
import com.aurora.community.utils.DialogUtil.IAlertDialogClick;
import com.aurora.community.utils.Globals;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.ToastUtil;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.Attachnfo;
import com.aurora.datauiapi.data.bean.ImageInfo;
import com.aurora.datauiapi.data.bean.PhotosObject;
import com.aurora.datauiapi.data.bean.PostDetailInfo;
import com.aurora.datauiapi.data.bean.TagInfo;
import com.aurora.datauiapi.data.bean.UpArticleObject;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

public class TwitterNoteActivity extends BaseActivity implements MessageHandler, INotifiableController{

	private static final String TAG = "TwitterNoteActivity";
	private Button mButton;

	private GridView mGridPicSelected;

	private ArrayList<String> list = new ArrayList<String>();

	//
	private Map<String, String> up_pic = new HashMap<String, String>();
	private TwitterNoteSelectedPhotoAdapter adapter;
	protected WeakHandler mHandler = new WeakHandler(this);
	private EditText content;
	public CommunityManager mComanager;

	private Dialog loadingDialog;
	
	private String pid = "";
	private String gid = "";
	private boolean isEditable = false;// 是否来自postdetail页面

	private LinearLayout tags_container;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("linp", "~~~~~~~~~~~~onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_layout);
		setTitleRes(R.string.cancel);
		mComanager = new CommunityManager(this);
		setupViews();
		Bundle d = getIntent().getExtras();
		if (d.containsKey("list")) {
			PhotoSerializable photoSerializable = (PhotoSerializable) d
					.getSerializable("list");
			List<String> result = photoSerializable.getUrlList();
			list.addAll(0, result);
		} else {
			getPostDetail();

		}
		fillTagsContainer();
		// TODO add add's icon
		String addUrl = "drawable://" + R.drawable.add_pic;
		list.add(addUrl);
		adapter = new TwitterNoteSelectedPhotoAdapter(this, list);
		mGridPicSelected.setAdapter(adapter);
		mGridPicSelected.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

				String url = list.get(arg2);
				if (url.contains("drawable://")) {
					Intent intent = new Intent(TwitterNoteActivity.this,
							ImagePickerActvity.class);
					intent.putExtra("left",
							mGridPicSelected.getChildCount() - 1);
					startActivity(intent);
				} else {
					Bundle d = new Bundle();
					Intent intent = new Intent(TwitterNoteActivity.this,
							TwitterItemPreviewActivity.class);
					PhotoSerializable photoSerializable = new PhotoSerializable();
					photoSerializable.setUrlList(list);
					d.putSerializable("list", photoSerializable);
					d.putInt("item", arg2);
					intent.putExtras(d);
					startActivity(intent);
				}
			}
		});

		mGridPicSelected.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return MotionEvent.ACTION_MOVE == event.getAction() ? true
						: false;
			}
		});
		// getPostDetail();
	}

	private final static int ACTION_BAR_ITEM_PUBLISH = 0x4444; 
	
	@Override
	public void setupAuroraActionBar() {
		// TODO Auto-generated method stub
		super.setupAuroraActionBar();
		addActionBarItem(getString(R.string.publish), ACTION_BAR_ITEM_PUBLISH);
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		// TODO Auto-generated method stub
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			showGiveUpTipDialog();
			break;
		case ACTION_BAR_ITEM_PUBLISH:
			publish();
			break;
		default:
			break;
		}
	}
	
	
	private ArrayList<TagInfo> existTags = new ArrayList<TagInfo>();
	
	private void getPostDetail() {
		PostDetailInfo detail = (PostDetailInfo) getIntent()
				.getSerializableExtra(PostDetailActivity.POST_DETAIL_KEY);
		if (detail != null) {
			isEditable = true;
			content.setText(detail.getBody());
			pid = detail.getPid();
			gid = detail.getUid();
			existTags.addAll(detail.getTags());
			for(int i = 0;i<tags.size();i++)
			{
				if(isExistTags(tags.get(i)))
				{
					selectTags.add(tags.get(i));
				}
			}
			
			// List<String> urls = new ArrayList<String>();
			List<ImageInfo> images = detail.getImages();
			for (int i = 0; i < images.size(); i++) {
				up_pic.put(images.get(i).getFpath(), images.get(i).getAid());

				list.add(images.get(i).getFpath());
				// urls.add(images.get(i).getFpath());
			}
			// return urls;
		}
		// return null;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("linp", "~~~~~~~~~~~~onNewIntent");
		Bundle d = intent.getExtras();
		PhotoSerializable photoSerializable = (PhotoSerializable) d
				.getSerializable("list");
		ArrayList<String> result = photoSerializable.getUrlList();
		boolean isPreview = intent.getBooleanExtra("preview", false);
		if (isPreview)
			list.clear();

		list.addAll(0, result);

		if (adapter != null)
			adapter.notifyDataSetChanged();
		super.onNewIntent(intent);
	}

	private ArrayList<TagInfo> tags;
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		Log.e("linp", "~~~~~~~~~~~~setupViews");
		mGridPicSelected = (GridView) findViewById(R.id.gv_pic_selected);
		content = (EditText) findViewById(R.id.twitter_content);
		tags_container = (LinearLayout) findViewById(R.id.tags_container);
		CommunityApp cpp = (CommunityApp) getApplicationContext();
		tags = cpp.getTagInfos();
	}

	private int getViewWidth(View view){
		int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		view.measure(width,height);
		return view.getMeasuredWidth();
	}
	
	private TextWatcher textWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	private void fillTagsContainer(){
		int maxWidth = getResources().getDisplayMetrics().widthPixels - tags_container.getPaddingLeft() - tags_container.getPaddingRight();
		LinearLayout tagsChildContainer = new LinearLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    LayoutInflater inflater = LayoutInflater.from(this);
	    LinearLayout.LayoutParams tagLPMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
	    tagLPMargin.leftMargin = DensityUtil.dip2px(this, 10);
	    LinearLayout.LayoutParams tagLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
	    LinearLayout.LayoutParams tagChildLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
	    tagChildLP.topMargin = DensityUtil.dip2px(this, 10);
	    if(tags.size() > 0)
	    {
	    	tags_container.addView(tagsChildContainer, lp);
	    }
	    boolean isFirst = true;
		for(int i = 0; i<tags.size(); i++)
		{
			TextView tv_tag_text = (TextView) inflater.inflate(R.layout.tags_item_text, null);
			tv_tag_text.setText(tags.get(i).getTsname());
			tv_tag_text.setTag(i);
			tv_tag_text.setOnClickListener(tagOnClickListener);
			tv_tag_text.setSelected(isExistTags(tags.get(i)));
		    if(isFirst)
		    {
		    	tagsChildContainer.addView(tv_tag_text,tagLP);
		    	isFirst = false;
		    }else{
		    	tagsChildContainer.addView(tv_tag_text,tagLPMargin);
		    }
			
		    if(getViewWidth(tagsChildContainer) > maxWidth)
		    {
		    	tagsChildContainer.removeView(tv_tag_text);
		    	isFirst = true;
		    	tagsChildContainer = new LinearLayout(this);
		    	tags_container.addView(tagsChildContainer, tagChildLP);
		    	i--;
		    }
		    
		}
		
	}
	
	private boolean isExistTags(TagInfo tag){
		
		for(int i = 0;i<existTags.size();i++)
		{
			if(tag.getTid().equals(existTags.get(i).getTid()))
			{
				return true;
			}
		}
		return false;
		
	}
	
	private ArrayList<TagInfo> selectTags = new ArrayList<TagInfo>();
	private View.OnClickListener tagOnClickListener  = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			v.setSelected(!v.isSelected());
			TagInfo tag = tags.get((Integer)v.getTag());
			if(v.isSelected())
			{
				if(!selectTags.contains(tag))
				{
					selectTags.add(tag);
				}
			}else{
				if(selectTags.contains(tag))
				{
					selectTags.remove(tag);
				}
				
			}
		}
	};
	
	private void printSelect(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<selectTags.size();i++)
		{
			sb.append(selectTags.get(i).getTname()+"         ");
		}
	}
	
	public void onClick(View v) {
		startActivity(new Intent(this, ImagePickerActvity.class));
	}

	private Dialog tipDialog;
	private void publish() {
       
		if(selectTags.size() == 0)
		{
			if(tipDialog == null)
			{
				tipDialog = DialogUtil.getPromptDialog(this, R.string.unselect_tags_tip);
			}
			tipDialog.show();
			return;
		}
		
		if(TextUtils.isEmpty(content.getText().toString().trim()))
		{
			ToastUtil.shortToast(R.string.publish_content_empyt);
			return;
		}
		
		if(!DefaultUtil.checkNetWork(this))
		{
			return;
		}
		
		if(!AccountHelper.mLoginStatus)
		{
			AccountUtil.getInstance().startLogin(this);
			return;
		}
		
		if(loadingDialog == null)
		{
			loadingDialog = DialogUtil.getLoadingDialog(this, getString(R.string.twittering));
		}
		loadingDialog.show();

		mGridPicSelected.setEnabled(false);
		content.setEnabled(false);
		/*new TotalCount(TwitterNoteActivity.this, "300", "006", 1).CountData();*/
		MobclickAgent.onEvent(TwitterNoteActivity.this, Globals.PREF_TIMES_PUBLISH2);
		if (list.size() > 0)
			uploadaPhotos();
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {

		switch (code) {
		case INotifiableController.CODE_UNKNONW_HOST:
		case INotifiableController.CODE_WRONG_DATA_FORMAT:
		case INotifiableController.CODE_REQUEST_TIME_OUT:
		case INotifiableController.CODE_CONNECT_ERROR:
		case INotifiableController.CODE_GENNERAL_IO_ERROR:
		case INotifiableController.CODE_NOT_FOUND_ERROR:
		case INotifiableController.CODE_JSON_PARSER_ERROR:
		case INotifiableController.CODE_JSON_MAPPING_ERROR:
		case INotifiableController.CODE_UNCAUGHT_ERROR:
			mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
			break;
		case INotifiableController.CODE_NOT_NETWORK:
			mHandler.sendEmptyMessage(Globals.NO_NETWORK);
			break;

		default:
			break;
		}
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		Log.e("linp", "##########TwitterNote onMessage");
		if (loadingDialog != null && loadingDialog.isShowing()
				&& !isFinishing()) {
			loadingDialog.dismiss();
			mGridPicSelected.setEnabled(true);
			content.setEnabled(true);
			ToastUtil.longToast("上传图片失败");
		}
		mGridPicSelected.setEnabled(true);
		content.setEnabled(true);
		switch (msg.what) {
		case Globals.NETWORK_ERROR:
			ToastUtil.longToast(R.string.network_exception);
			break;
		case Globals.NO_NETWORK:
			ToastUtil.longToast(R.string.network_not_available);
			break;

		default:
			break;
		}
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	private void uploadaPhotos() {

		mComanager.upPhotos(new DataResponse<ArrayList<PhotosObject>>() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (null != value) {
					Log.e("linp", "zhangwei the color end1");
					ArrayList<PhotosObject> obj = (ArrayList<PhotosObject>) value;
					
					if((obj.size()>0)&&(obj.get(obj.size()-1).getReturnCode() != Globals.CODE_SUCCESS))
					{
						upErrorMsg(obj.get(obj.size()-1).getMsg());
					}
					else
					{
						StringBuilder sb = new StringBuilder();
						for(int i = 0; i<selectTags.size(); i++)
						{
							sb.append(selectTags.get(i).getTid());
							if(i != selectTags.size()-1)
							{
								sb.append(",");
							}
						}
						uploadTwitter(obj,sb.toString());
					}
						
				} else {
					upErrorMsg(getResources().getString(R.string.upload_pic_err_msg));
				}

			}
		}, up_pic, list);
	}

	private void upErrorMsg(String err_msg)
	{
		if (loadingDialog != null && loadingDialog.isShowing()
				&& !isFinishing()) {
			loadingDialog.dismiss();
			mGridPicSelected.setEnabled(true);
			content.setEnabled(true);
			ToastUtil.longToast(err_msg);
		}
	}
	private void uploadTwitter(ArrayList<PhotosObject> obj,String tag) {
		Log.e("linp", "zhangwei the color end2");
		ArrayList<Attachnfo> attach = new ArrayList<Attachnfo>();
        String type  = new String();
        
		for (int i = 0; i < obj.size(); i++) {
			Attachnfo att = new Attachnfo();
			// att.setBase_color("");
			att.setDesc("");
			att.setAid(obj.get(i).getData().getAid());
			attach.add(att);
		}
		//setting type
		if(obj.size()>0){
			type = "2";
		}else{
			type = "1";
		}
		
		mComanager.upArticle(new DataResponse<UpArticleObject>() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (null != value) {
					if (value.getReturnCode() == Globals.CODE_SUCCESS) {
						//Log.i(TAG, "上传成功");
						Log.e("linp", "zhangwei the color end5");
						// ToastUtil.longToast("upload article success the pid="+value.getData().getPid());
						if (loadingDialog != null
								&& loadingDialog.isShowing()
								&& !isFinishing()) {
							loadingDialog.dismiss();
							mGridPicSelected.setEnabled(true);
							content.setEnabled(true);
							sendRefreshBroadCast();
							back();
						}
					}
					ToastUtil.shortToast(value.getMsg());
				}

			}
		}, pid, gid, attach, content.getText().toString(),type,tag);
	}

	private Dialog alertDialog;
	
	private void sendRefreshBroadCast(){
		Intent broadcastIntent = new Intent(MainActivity.REFRESH_MAIN_CATEGORY_ACTION);
		sendBroadcast(broadcastIntent);
	}
	
	private void showGiveUpTipDialog(){
		if(alertDialog == null)
		{
			alertDialog = DialogUtil.getAlertDialog(this, getString(R.string.giveup_edit_dialog_msg), R.string.giveup_edit_dialog_cancel, 
					R.string.giveup_edit_dialog_exit,alertDialogClick);
		}
		alertDialog.show();
		
	}
	
	private IAlertDialogClick  alertDialogClick = new IAlertDialogClick() {
		
		@Override
		public void sureClick() {
			// TODO Auto-generated method stub
			setResult(RESULT_CANCELED);
			finish();
		}
		
		@Override
		public void cancelClick() {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK&&isEditable)
		{
			showGiveUpTipDialog();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	private void back() {
		if (isEditable) {
			setResult(RESULT_OK);
			finish();
		} else {
			/*Intent intent = new Intent(TwitterNoteActivity.this,
					MainActivity.class);
			intent.putExtra(MainActivity.COME_FROM_KEY,
					TwitterNoteActivity.class.getName());
			startActivity(intent);*/
			if(CommunityApp.getInstance() != null)
			{
				if(CommunityApp.getInstance().getinstance()!=null)
				{
		    		CommunityApp.getInstance().getinstance().setDisView(TwitterNoteActivity.class.getName());
					finish();
				}else{
					Intent intent = new Intent(this,MainActivity.class);
					intent.putExtra(MainActivity.SHOW_PAGE_KEY, MainActivity.SHOW_USER_CENTER);
					startActivity(intent);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
}
