package com.aurora.iunivoice.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.ImageUpObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.picBrowser.ImagePickerActvity;
import com.aurora.iunivoice.activity.picBrowser.TwitterItemPreviewActivity;
import com.aurora.iunivoice.adapter.EmotionAdapter;
import com.aurora.iunivoice.bean.PhotoSerializable;
import com.aurora.iunivoice.utils.CommonUtil;
import com.aurora.iunivoice.utils.DensityUtil;
import com.aurora.iunivoice.utils.DialogUtil;
import com.aurora.iunivoice.utils.EmotionUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ImageLoaderHelper;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.ToastUtil;
import com.aurora.iunivoice.widget.GridViewGallery;
import com.aurora.iunivoice.widget.GridViewGallery.onGridViewItemClickListener;
import com.aurora.iunivoice.widget.OnSizeChangeLinearLayout;

public class PublishActivity extends BaseActivity implements OnClickListener {

	public static final String FORMHASH = "formhash";
	public static final int REQUEST_FORUM_CHOICE = 100;
	public static final int REQUEST_PICK_IMG = 101;
	public static final int REQUEST_CAMERA = 102;
	public static final int REQUEST_IMG_EDIT = 103;
	
	public static boolean CANCLE_FLAG = false;			// 发表取消标识
	public static long OPERATION_TAG = 0;				// 发表操作标识
	
	protected static final int ACTION_BAR_RIGHT_ITEM_ID = 1;
	
	private IuniVoiceManager mManager;
	
	private OnSizeChangeLinearLayout mRootLy;
	private LinearLayout ll_top;
	
	private RelativeLayout rl_forum;
	private TextView tv_forum;
	private EditText et_subject;
	private EditText et_content;
	
	private ImageView iv_camera;
	private ImageView iv_img;
	private ImageView iv_face;
	
	private GridViewGallery gvGallery_img;
	private GridView gv_face;
	
	private Dialog loadingDialog;
	
	private int checkIndex = -1;
	private String forumFid;
	private String forumTitle;
	
	private String formhash;
	private String subject;
	private String content;
	
	private ArrayList<String> imgList = new ArrayList<String>();
	
	private static final String IMAGE_CAPTURE_PREFIX = ImageLoaderHelper
			.getDirectory().toString();
	private String fileName;
	private final int maxPic = 9;
	private String addPicUri = "drawable://" + R.drawable.icon_add_pic;
	
	private static final int TYPE_NORMAL = 0;	// 不显示
	private static final int TYPE_IMG = 1;		// 显示图片
	private static final int TYPE_FACE = 2;		// 显示表情
	private int showType = TYPE_NORMAL;
	
	private static final int KEY_ON = 1;
	private static final int KEY_OFF = 2;
	private int keyType = KEY_OFF;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish);
		
		setupViews();
		initData();
		getIntentData();
	}
	
	@Override
	public void setupViews() {
		mRootLy = (OnSizeChangeLinearLayout) findViewById(R.id.root_ly);
		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		
		rl_forum = (RelativeLayout) findViewById(R.id.rl_forum);
		tv_forum = (TextView) findViewById(R.id.tv_forum);
		et_subject = (EditText) findViewById(R.id.et_subject);
		et_content = (EditText) findViewById(R.id.et_content);
		
		iv_camera = (ImageView) findViewById(R.id.iv_camera);
		iv_img = (ImageView) findViewById(R.id.iv_img);
		iv_face = (ImageView) findViewById(R.id.iv_face);
		
		gvGallery_img = (GridViewGallery) findViewById(R.id.gvGallery_img);
		gv_face = (GridView) findViewById(R.id.gv_face);
		
		rl_forum.setOnClickListener(this);
		iv_camera.setOnClickListener(this);
		iv_img.setOnClickListener(this);
		iv_face.setOnClickListener(this);
		
		gvGallery_img.setGridViewItemClickListener(new onGridViewItemClickListener() {
			@Override
			public void ongvItemClickListener(View v, int postion) {
				if (postion < imgList.size()) {
					if (imgList.get(postion).equals(addPicUri)) {
						Intent ipIntent = new Intent(PublishActivity.this, ImagePickerActvity.class);
						if (imgList.size() > 0) {
							ipIntent.putExtra("left", imgList.size() - 1);
						}
						startActivityForResult(ipIntent, REQUEST_PICK_IMG);
					} else {
						ArrayList<String> list = new ArrayList<String>();
						list.addAll(imgList);
						list.remove(addPicUri);
						
						Bundle d = new Bundle();
						Intent intent = new Intent(PublishActivity.this,
								TwitterItemPreviewActivity.class);
						PhotoSerializable photoSerializable = new PhotoSerializable();
						photoSerializable.setUrlList(list);
						d.putSerializable("list", photoSerializable);
						d.putInt("item", postion);
						intent.putExtras(d);
						startActivityForResult(intent, REQUEST_IMG_EDIT);
					}
				}
			}
		});
		
		mRootLy.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					
			float def = 0;

			@Override
			public void onGlobalLayout() {
				int heightDiff = mRootLy.getRootView().getHeight() - mRootLy.getHeight();
				
				if (def == 0) {
					def = DensityUtil.dip2px(PublishActivity.this, 100);
				}

				if (heightDiff > def) { // 说明键盘是弹出状态
					if (keyType == KEY_ON) {
						return;
					}

					keyType = KEY_ON;
					Log.v(TAG, "键盘弹出状态");
					
					if (et_content.hasFocus()) {
						ll_top.setVisibility(View.GONE);
						gv_face.setVisibility(View.GONE);
					}
					
					if (et_subject.hasFocus() && showType == TYPE_FACE) {
						showType = TYPE_NORMAL;
						gv_face.setVisibility(View.GONE);
					}
					
					if (imgList.size() > 0) {
						showType = TYPE_IMG;
						gvGallery_img.setVisibility(View.VISIBLE);
					} else {
						showType = TYPE_NORMAL;
						gvGallery_img.setVisibility(View.GONE);
						return;
					}
					
					LayoutParams params = gvGallery_img
							.getLayoutParams();
					int height = params.height;
					if (height == getResources().getDimensionPixelOffset(R.dimen.publich_imgbox_height_half)) {
						return;
					}
					params.height = getResources()
							.getDimensionPixelOffset(
									R.dimen.publich_imgbox_height_half);
					gvGallery_img.setLayoutParams(params);

					gvGallery_img.setPageItemCount(3);
					gvGallery_img.setList(imgList);

				} else {
					if (keyType == KEY_OFF) {
						return;
					}
					keyType = KEY_OFF;

					Log.v(TAG, "键盘收起状态");
					
					ll_top.setVisibility(View.VISIBLE);

					LayoutParams params = gvGallery_img
							.getLayoutParams();
					int height = params.height;
					if (height == getResources().getDimensionPixelOffset(R.dimen.publich_imgbox_height)) {
						return;
					}
					params.height = getResources()
							.getDimensionPixelOffset(
									R.dimen.publich_imgbox_height);
					gvGallery_img.setLayoutParams(params);

					gvGallery_img.setPageItemCount(6);
					gvGallery_img.setList(imgList);
				}
			}
		});
		
		gv_face.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Bitmap bitmap = BitmapFactory.decodeResource(PublishActivity.this.getResources(),
						EmotionUtil.getInstence().getEmotionList().get(position).getResId());
				bitmap = Bitmap.createScaledBitmap(bitmap, DensityUtil.dip2px(PublishActivity.this, 18), 
						DensityUtil.dip2px(PublishActivity.this, 18), true);
				ImageSpan imageSpan = new ImageSpan(PublishActivity.this, bitmap);
				String spannableString = EmotionUtil.getInstence().getEmotionList().get(position).getCode();
				SpannableString spannable = new SpannableString(spannableString);
				spannable.setSpan(imageSpan, 0, spannableString.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				if (et_content != null & et_content.hasFocus()) {
					et_content.append(spannable);
				}
			}
		});
		
	}

	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.publish_title);
		
		addActionBarItem(getString(R.string.publish), ACTION_BAR_RIGHT_ITEM_ID);
		
		View tv =  getActionBarItem(ACTION_BAR_RIGHT_ITEM_ID);
	    if (tv != null) {
	    	((TextView) tv).setTextColor(getResources().getColor(R.color.action_bar_account_text_color));
	    }
	}
	
	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		case ACTION_BAR_RIGHT_ITEM_ID:
			if (checkPublish()) {
				publish();
			}
			break;
		}
	}
	
	private void initData() {
		mManager = new IuniVoiceManager(this);
		
		EmotionAdapter emotionAdapter = new EmotionAdapter(this, EmotionUtil.getInstence().getEmotionList());
		gv_face.setAdapter(emotionAdapter);
	}
	
	private void getIntentData() {
		formhash = getIntent().getStringExtra(FORMHASH);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.rl_forum:
			Intent intent = new Intent(PublishActivity.this, ForumChoiceActivity.class);
			if (checkIndex != -1) {
				intent.putExtra(ForumChoiceActivity.FORUM_INDEX, checkIndex);
			}
			startActivityForResult(intent, REQUEST_FORUM_CHOICE);
			break;
		case R.id.iv_camera:
			openCamera();
			break;
		case R.id.iv_img:
			openImgBox();
			break;
		case R.id.iv_face:
			openFaceBox();
			break;
		}
		
	}
	
	private void openCamera() {
		if (showType == TYPE_FACE) {
			gv_face.setVisibility(View.GONE);
		}
		
		if (imgList.size() > 0) {
			showType = TYPE_IMG;
			gvGallery_img.setVisibility(View.VISIBLE);
		} else {
			showType = TYPE_NORMAL;
			gvGallery_img.setVisibility(View.GONE);
		}
		
		if (imgList.size() >= maxPic) { 
			if (!imgList.contains(addPicUri)) {
				Toast.makeText(PublishActivity.this, "最多选择" + maxPic + "张图片！",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Intent cIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileName = IMAGE_CAPTURE_PREFIX + "/"
				+ System.currentTimeMillis() + ".jpg";
		cIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(fileName)));
		startActivityForResult(cIntent, REQUEST_CAMERA);
	}
	
	private void openImgBox() {
		if (showType == TYPE_FACE) {
			gv_face.setVisibility(View.GONE);
		}
		
		if (imgList.size() > 0) {
			showType = TYPE_IMG;
			
			gvGallery_img.setVisibility(View.VISIBLE);
			return;
		}
		
		if (imgList.size() >= maxPic) {
			Toast.makeText(PublishActivity.this, "最多选择" + maxPic + "张图片！",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent ipIntent = new Intent(this, ImagePickerActvity.class);
		if (imgList.size() > 0) {
			ipIntent.putExtra("left", imgList.size() - 1);
		}
		startActivityForResult(ipIntent, REQUEST_PICK_IMG);
	}
	
	private void openFaceBox() {
		if (showType == TYPE_FACE) {
			gv_face.setVisibility(View.GONE);
			
			if (imgList.size() > 0) {
				showType = TYPE_IMG;
				
				gvGallery_img.setVisibility(View.VISIBLE);
			} else {
				showType = TYPE_NORMAL;
			}
			return;
		}
		
		if (et_subject.hasFocus()) {
			return;
		}
		
		showType = TYPE_FACE;
		
		gvGallery_img.setVisibility(View.GONE);
		gv_face.setVisibility(View.VISIBLE);
		
		if (CommonUtil.isSoftInputOpen(PublishActivity.this)) {
			CommonUtil.hideSoftInput(PublishActivity.this, et_content);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_FORUM_CHOICE && resultCode == RESULT_OK) {
			if (data != null) {
				checkIndex = data.getIntExtra(ForumChoiceActivity.FORUM_INDEX, -1);
				forumFid = data.getStringExtra(ForumChoiceActivity.FORUM_ID);
				forumTitle = data.getStringExtra(ForumChoiceActivity.FORUM_TITLE);
				tv_forum.setText(forumTitle);
			}
		} else if (requestCode == REQUEST_PICK_IMG && resultCode == RESULT_OK) {
			if (data != null) {
				Bundle d = data.getExtras();
				if (d.containsKey("list")) {
					PhotoSerializable photoSerializable = (PhotoSerializable) d
							.getSerializable("list");
					List<String> result = photoSerializable.getUrlList();
					if (imgList.size() > 0) { 
						imgList.remove(addPicUri);
					}
					imgList.addAll(result);
					if (imgList.size() < maxPic && imgList.size() > 0) {
						imgList.add(addPicUri);
					}
				
					gvGallery_img.setList(imgList);
				}
			}
			
			if (imgList.size() > 0) {
				showType = TYPE_IMG;
				changeTypeShow();
			}
		} else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
			Intent flush = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); // ,
			// MediaStore.Images.Media.EXTERNAL_CONTENT_URI
			Uri uri = Uri.fromFile(new File(fileName));
			flush.setData(uri);
			this.sendBroadcast(flush);
			
			String p = "file:///" + fileName;
			if (imgList.size() > 0) { 
				imgList.remove(addPicUri);
			}
			imgList.add(p);
			if (imgList.size() < maxPic && imgList.size() > 0) {
				imgList.add(addPicUri);
			}
			
			gvGallery_img.setList(imgList);
			
			if (imgList.size() > 0) {
				showType = TYPE_IMG;
				changeTypeShow();
			}
		} else if (requestCode == REQUEST_IMG_EDIT && resultCode == RESULT_OK) {
			if (data != null) {
				Bundle d = data.getExtras();
				if (d.containsKey("list")) {
					PhotoSerializable photoSerializable = (PhotoSerializable) d
							.getSerializable("list");
					List<String> result = photoSerializable.getUrlList();
					imgList.clear();
					imgList.addAll(result);
					
					if (imgList.size() < maxPic && imgList.size() > 0) {
						imgList.add(addPicUri);
					}
				
					gvGallery_img.setList(imgList);
				}
			}
			
			if (imgList.size() > 0) {
				showType = TYPE_IMG;
				changeTypeShow();
			} else {
				showType = TYPE_NORMAL;
				changeTypeShow();
			}
		}
	}
	
	public boolean checkPublish() {
		if (TextUtils.isEmpty(forumFid)) {
			ToastUtil.shortToast(R.string.publish_please_choice_forum);
			return false;
		}
		subject = et_subject.getText().toString();
		if (TextUtils.isEmpty(subject)) {
			ToastUtil.shortToast(R.string.publish_please_input_subject);
			return false;
		}
		content = et_content.getText().toString();
		if (TextUtils.isEmpty(content)) {
			ToastUtil.shortToast(R.string.publish_please_input_content);
			return false;
		}
		
		return true;
	}
	
	public void publish() {
		if (loadingDialog == null) {
			loadingDialog = DialogUtil.getLoadingDialog(this, getString(R.string.publish_publishing));
			loadingDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					CANCLE_FLAG = true;
					OPERATION_TAG = 0;
				}
			});
		}
		loadingDialog.show();
		OPERATION_TAG = System.currentTimeMillis();
		CANCLE_FLAG = false;
		final long operation = OPERATION_TAG;
		
		ArrayList<String> img = new ArrayList<String>();
		img.addAll(imgList);
		img.remove(addPicUri);
		
		if (img.size() > 0) {
			mManager.uploadImageFiles(new DataResponse<ArrayList<ImageUpObject>>() {
				@Override
				public void run() {
					if (null != value) {
						
						ArrayList<ImageUpObject> obj = (ArrayList<ImageUpObject>) value;

						if ((obj.size() > 0) && (obj.get(obj.size() - 1).getReturnCode() != Globals.CODE_SUCCESS)) {
							upErrorMsg(obj.get(obj.size() - 1).getMsg());
						} else {
							int[] ids = new int[obj.size()];
							for (int i = 0; i < ids.length; i++) {
								ids[i] = obj.get(i).getAttachnewId();
							}
							
							if (!CANCLE_FLAG && OPERATION_TAG == operation) {
								publish(ids);
							}
						}
					} else {
						upErrorMsg(getResources().getString(R.string.upload_pic_err_msg));
						if (loadingDialog != null && loadingDialog.isShowing()
								&& !isFinishing()) {
							loadingDialog.dismiss();
						}
					}
				}
			}, null, img, operation);
		} else {
			publish(null);
		}
	}
	
	public void publish(int[] attachnew) {
		mManager.publish(new DataResponse<BaseResponseObject>() {
			@Override
			public void run() {
				if (value != null) {
					if (loadingDialog != null && loadingDialog.isShowing()
							&& !isFinishing()) {
						loadingDialog.dismiss();
					}
					
					if (value.getReturnCode() == 0) { 	// 成功
						ToastUtil.shortToast(R.string.publish_publish_success);
						if (PublishActivity.this != null && !isFinishing()) {
							finish();
						}
					} else {
						ToastUtil.shortToast(value.getMsg());
					}
				}
			}
		}, formhash, subject, content, forumFid, attachnew);
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (loadingDialog != null && loadingDialog.isShowing()
				&& !isFinishing()) {
			loadingDialog.dismiss();
			ToastUtil.longToast("发表失败");
		}
		
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
	
	private void changeTypeShow() {
		if (showType == TYPE_NORMAL) {
			gvGallery_img.setVisibility(View.GONE);
		} else if (showType == TYPE_IMG) {
			gvGallery_img.setVisibility(View.VISIBLE);
		} else if (showType == TYPE_FACE) {
			gvGallery_img.setVisibility(View.GONE);
		}
	}
	
	private void upErrorMsg(String err_msg) {
		if (loadingDialog != null && loadingDialog.isShowing()
				&& !isFinishing()) {
			loadingDialog.dismiss();
			ToastUtil.longToast(err_msg);
			
			OPERATION_TAG = 0;
			CANCLE_FLAG = false;
		}
	}
	
}
