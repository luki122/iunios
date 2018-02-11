package com.aurora.iunivoice.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.BaseUserInfo;
import com.aurora.datauiapi.data.bean.ImageUpObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.account.PhotoLoader;
import com.aurora.iunivoice.activity.fragment.PersonalFragment;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.BitmapUtil;
import com.aurora.iunivoice.utils.DialogUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.ImageLoadUtil;
import com.aurora.iunivoice.utils.SDcardManager;
import com.aurora.iunivoice.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AccountManageActivity extends BaseActivity implements
		OnClickListener {

	private ImageView user_icon;

	private BaseUserInfo baseUserInfo;

	private IuniVoiceManager iuniVoiceManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_manage);
		baseUserInfo = getIntent().getParcelableExtra(
				PersonalFragment.USER_BASE_INFO_KEY);
		iuniVoiceManager = new IuniVoiceManager(this);
		setupViews();
	}

	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		setTitleRes(R.string.mine_account_namage);
		user_icon = (ImageView) findViewById(R.id.user_icon);
		user_icon.setOnClickListener(this);
		findViewById(R.id.change_icon_layout).setOnClickListener(this);
		findViewById(R.id.introduce_layout).setOnClickListener(this);
		ImageLoader.getInstance().displayImage(baseUserInfo.getUserIcon(),
				user_icon, ImageLoadUtil.circleUserIconOptions);
	}

	public static final String INTRODEUC_KEY = "introduce";
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.user_icon:
			changeIcon();
			break;
		case R.id.change_icon_layout:
			changeIcon();
			break;
		case R.id.introduce_layout:
			Intent intent = new Intent(this, UserIntroduceActivity.class);
			intent.putExtra(INTRODEUC_KEY, baseUserInfo.getUserRemarks());
			startActivityForResult(intent,CHANGE_INTRODUCE_REQUEST);
			break;
		default:
			break;
		}

	}
	

	/** 拍照 */
	private static final int SET_ICON_MENU_PHOTO = 0;
	/** 相册 */
	private static final int SET_ICON_MENU_GALLERY = 1;

	private File mCurrentPhotoFile;
	private File mCurrentCropFile;
	private File mPickPhotoKitkatFile;

	private void changeIcon() {
		DialogUtil
				.getItemAlertDialog(
						this,
						R.string.account_info_set_icon_title,
						R.array.setAccountIconMenus,
						new com.aurora.iunivoice.utils.DialogUtil.IAlertDialogItemClick() {

							@Override
							public void onClick(int which) {
								// TODO Auto-generated method stub
								switch (which) {
								case SET_ICON_MENU_PHOTO:
									openCamera();
									break;
								case SET_ICON_MENU_GALLERY:
									openGallery();
									break;
								}
							}
						}).show();
	}

	/**
	 * 相机拍照
	 */
	private void openCamera() {
		if (!checkSDCard()) {
			return;
		}
		if (!Globals.PHOTO_DIR.exists()) {
			Globals.PHOTO_DIR.mkdirs();
		}

		mCurrentPhotoFile = IconLoader.getPhotoFile();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(mCurrentPhotoFile));
		startActivityForResult(intent,
				BitmapUtil.REQUEST_CODE_GETIMAGE_BYCAMERA);
	}

	/**
	 * 这个方法只能在有Looper的线程里调用，因为会弹toast，否则会崩溃
	 * 
	 * @return
	 */
	private boolean checkSDCard() {
		if (!SDcardManager.checkSDCardMount()) {
			ToastUtil.longToast(R.string.sdcard_not_mounted);
			return false;
		}
		if (!SDcardManager.checkSDCardAvailableSize()) {
			ToastUtil.longToast(R.string.sd_space_not_enough);
			return false;
		}
		return true;
	}

	private static class IconLoader extends PhotoLoader<AccountManageActivity> {
		public IconLoader(AccountManageActivity target) {
			super(target);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			AccountManageActivity target = mTarget.get();
			if (target != null) {
				if (mErrorMsgResId != -1) {
					ToastUtil.shortToast(mErrorMsgResId);
					return;
				}

				if (result != null) {
					target.user_icon.setImageBitmap(result);
				}
			}
		}
	}

	/**
	 * 相册选取
	 * 
	 * @param type
	 */
	private void openGallery() {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		// 限制为只能用我们系统自带的Gallery来选择
		// intent.setAction("com.aurora.filemanager.SINGLE_GET_CONTENT");

		// intent.setPackage("com.android.gallery3d");

		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
			startActivityForResult(intent,
					BitmapUtil.REQUEST_CODE_GETIMAGE_BYSDCARD);
		} else {
			mPickPhotoKitkatFile = IconLoader.getPickPhotoKitkatFile();

			startActivityForResult(intent,
					BitmapUtil.REQUEST_CODE_GETIMAGE_BYSDCARD_KITKAT);
		}

	}

	public static final int CHANGE_INTRODUCE_REQUEST = 124;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case BitmapUtil.REQUEST_CODE_GETIMAGE_BYSDCARD:
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();
				Cursor cursor = null;
				try {
					cursor = getContentResolver().query(uri, null, null, null,
							null);
					if (cursor != null && cursor.moveToFirst()) {
						cropIcon(new File(cursor.getString(1)));
					}
				} catch (Exception e) {
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
			break;
		case BitmapUtil.REQUEST_CODE_GETIMAGE_BYSDCARD_KITKAT:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				InputStream input = null;
				OutputStream output = null;

				try {
					// converting the input stream into file to crop the
					// selected image from sd-card.
					input = getApplicationContext().getContentResolver()
							.openInputStream(selectedImage);
					try {
						mPickPhotoKitkatFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					output = new FileOutputStream(mPickPhotoKitkatFile);

					int read = 0;
					byte[] bytes = new byte[1024];

					while ((read = input.read(bytes)) != -1) {
						try {
							output.write(bytes, 0, read);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

				cropIcon(mPickPhotoKitkatFile);
			}
			break;
		case BitmapUtil.REQUEST_CODE_GETIMAGE_BYCROP:
			deleteTempPhotoFile();
			deletePickPhotoKitkatFile();

			if (resultCode == RESULT_OK) {
				Uri photoUri = data.getData();
				Log.d(TAG, "Jim, photoUri: " + photoUri);
				if (photoUri == null
						&& (mCurrentCropFile != null && mCurrentCropFile
								.exists())) {
					photoUri = Uri.fromFile(mCurrentCropFile);
					Log.d(TAG, "Jim, photoUri is set to: " + photoUri);
				}
				if (photoUri == null) {
					ToastUtil
							.shortToast(R.string.account_info_error_photo_file_not_exist);
				} else {
					new ChangePhotoTask(this).execute(photoUri);
				}
			} else {
				ToastUtil
						.shortToast(R.string.account_info_error_falied_to_crop);
			}
			break;
		case BitmapUtil.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (resultCode == Activity.RESULT_OK) {
				cropIcon(mCurrentPhotoFile);
			}
			break;
		case CHANGE_INTRODUCE_REQUEST:
			if (data != null) {
				String newIntroduce = data.getStringExtra(INTRODEUC_KEY);
				if (newIntroduce != null) {
					baseUserInfo.setUserRemarks(newIntroduce);
				}
			}
			break;
		}

	}

	private static class ChangePhotoTask extends
			AsyncTask<Uri, Integer, Bitmap> {
		private WeakReference<AccountManageActivity> mTarget;
		private Uri mUri;

		public ChangePhotoTask(AccountManageActivity target) {
			mTarget = new WeakReference<AccountManageActivity>(target);
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			mUri = params[0];
			String path = mUri.getPath();
			AccountManageActivity target = mTarget.get();
			if (target != null) {
				File file = new File(path);
				if (!file.equals(target.mCurrentCropFile)) {
					target.deleteTempCropFile();
					target.mCurrentCropFile = file;
				}
			}
			Bitmap src = BitmapFactory.decodeFile(path);
			if (src != null) {
				return BitmapUtil.clipCircleBitmap(src, src.getWidth());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			AccountManageActivity target = mTarget.get();
			if (target != null) {
				if (result != null) {
					target.user_icon.setImageBitmap(result);
					target.changePhoto();
				} else {
					ToastUtil
							.shortToast(R.string.account_info_error_photo_file_not_exist);
				}
			}
		}
	}

	private Dialog loadingDialog;
	private void changePhoto() {
		if (loadingDialog == null) {
			loadingDialog = DialogUtil.getLoadingDialog(this, getString(R.string.modifyinage_icon));
		}
		loadingDialog.show();
		ArrayList<String> images = new ArrayList<String>();
		images.add(mCurrentCropFile.getAbsolutePath());
		mComanager.changeUserIcon(new DataResponse<BaseResponseObject>(){
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				ToastUtil.longToast(value.getMsg());
				if(value !=null && value.getReturnCode() == Globals.CODE_SUCCESS)
				{
					AccountHelper.getInstance(AccountManageActivity.this).userInfoChange();
					finish();
				}
			}
			
		}, mCurrentCropFile.getAbsolutePath());
	}
	
	public void chageUserIcon(String id){
		mComanager.changeUserIcon(new DataResponse<BaseResponseObject>(){
			@Override
			public void run() {
				super.run();
				if(value.getCode() == Globals.CODE_SUCCESS)
				{
					loadingDialog.dismiss();
					ToastUtil.shortToast(R.string.modify_success);
				}
			}
		}, id);
	}
	
	
	private void deleteTempCropFile() {
		if (mCurrentCropFile != null && mCurrentCropFile.exists()) {
			mCurrentCropFile.delete();
		}
		mCurrentCropFile = null;
	}

	private void deleteTempPhotoFile() {
		if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
			mCurrentPhotoFile.delete();
		}
		mCurrentPhotoFile = null;
	}

	private void deletePickPhotoKitkatFile() {
		if (mPickPhotoKitkatFile != null && mPickPhotoKitkatFile.exists()) {
			mPickPhotoKitkatFile.delete();
		}
		mPickPhotoKitkatFile = null;
	}

	private void cropIcon(File iconFile) {
		final Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(iconFile), "image/*");
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		// final int expectedWidth = DensityUtil.dip2px(this, 76); //
		// 请保持跟布局文件里的控件的宽高一致
		final int expectedWidth = getResources().getDimensionPixelSize(
				R.dimen.account_info_account_icon_width);
		intent.putExtra("outputX", expectedWidth);
		intent.putExtra("outputY", expectedWidth);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", false);
		// intent.putExtra("data", mBitmap);

		mCurrentCropFile = IconLoader.getCropFile();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentCropFile));
		startActivityForResult(intent, BitmapUtil.REQUEST_CODE_GETIMAGE_BYCROP);
	}

}
