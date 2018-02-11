package com.aurora.iunivoice.activity.account;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

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
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.BaseResponseObject;
import com.aurora.datauiapi.data.bean.ChangePhotoRespObject;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.events.EventsType;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.BitmapUtil;
import com.aurora.iunivoice.utils.BooleanPreferencesUtil;
import com.aurora.iunivoice.utils.CommonUtil;
import com.aurora.iunivoice.utils.DialogUtil;
import com.aurora.iunivoice.utils.DialogUtil.IAlertDialogClick;
import com.aurora.iunivoice.utils.DialogUtil.IAlertDialogItemClick;
import com.aurora.iunivoice.utils.FileLog;
import com.aurora.iunivoice.utils.FormatUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.SDcardManager;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.ToastUtil;

/**
 * 账户详情界面
 * 
 * @author JimXia
 *
 * @date 2014-9-30 上午11:12:19
 */
public class AccountInfoActivity extends BaseObserverActivity implements
		OnClickListener {

	/** 拍照 */
	private static final int SET_ICON_MENU_PHOTO = 0;
	/** 相册 */
	private static final int SET_ICON_MENU_GALLERY = 1;

	private ImageView mAccountIconIv;
	private TextView mAccountIdTv;
	private TextView mAccountNicknameTv;
	private TextView mAccountPwdTv;
	private TextView mAccountPhoneTv;
	private TextView mAccountEmailTv;

	private LinearLayout mAccountNicknameLy;
	private LinearLayout mAccountPwdLy;
	private LinearLayout mAccountPhoneLy;
	private LinearLayout mAccountEmailLy;

	private TextView mStorageTotalTv;
	private TextView mStorageAvailableTv;

	private View mLogoutView;

	private AccountManager mAccountManager;
	private File mCurrentPhotoFile;
	private File mCurrentCropFile;
	private File mPickPhotoKitkatFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_account_info);

		setupViews();
		initData();

		mAccountManager = new AccountManager(this);
		loadUserInfo();
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		initViews();
		setListeners();
	}
	
	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.account_info_title);
	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null) {
			int command = intent.getIntExtra(Globals.EXTRA_COMMAND,
					Globals.COMMAND_UNKNOW);
			if (command == Globals.COMMAND_REFRESH_EMAIL) {
				// 邮箱修改了，刷新邮箱
				loadUserInfo();
			}
		}
	}

	private void initViews() {
		mAccountIconIv = (ImageView) findViewById(R.id.account_icon_iv);
		mAccountIdTv = (TextView) findViewById(R.id.account_id_tv);
		mAccountNicknameTv = (TextView) findViewById(R.id.account_nickname_tv);
		mAccountPwdTv = (TextView) findViewById(R.id.account_pwd_tv);
		mAccountPhoneTv = (TextView) findViewById(R.id.account_phone_tv);
		mAccountEmailTv = (TextView) findViewById(R.id.account_email_tv);
		mStorageTotalTv = (TextView) findViewById(R.id.storage_total_tv);
		mStorageAvailableTv = (TextView) findViewById(R.id.storage_available_tv);

		mAccountNicknameLy = (LinearLayout) findViewById(R.id.account_nickname_ly);
		mAccountPwdLy = (LinearLayout) findViewById(R.id.account_pwd_ly);
		mAccountPhoneLy = (LinearLayout) findViewById(R.id.account_phone_ly);
		mAccountEmailLy = (LinearLayout) findViewById(R.id.account_email_ly);

		mLogoutView = findViewById(R.id.logout_btn);
	}

	private void setListeners() {
		mLogoutView.setOnClickListener(this);

		mAccountIconIv.setOnClickListener(this);
		mAccountNicknameLy.setOnClickListener(this);
		mAccountPwdLy.setOnClickListener(this);
		mAccountPhoneLy.setOnClickListener(this);
		mAccountEmailLy.setOnClickListener(this);
	}

	private void initData() {
		mAccountIdTv.setText(mPref.getUserID());
		setUserNick(mPref.getUserNick());
		mAccountPwdTv.setText("********");
		setUserPhone(mPref.getUserPhone());
		setUserEmail(mPref.getUserEmail());
		mStorageTotalTv.setText(mPref.getStorageTotal());
		mStorageAvailableTv.setText(FormatUtil.formatStorageSize(mPref
				.getStorageAvailable()));

		Bitmap icon = IconLoader.loadLocalCacheIcon();
		if (icon != null) {
			mAccountIconIv.setImageBitmap(icon);
		}
	}

	private void setUserNick(String nick) {
		if (TextUtils.isEmpty(nick)) {
			nick = getString(R.string.uncreate);
		}
		mAccountNicknameTv.setText(nick);
	}

	private void setUserPhone(String phone) {
		if (TextUtils.isEmpty(phone)) {
			phone = getString(R.string.unbind);
		} else {
			phone = CommonUtil.hidePhoneNum(phone);
		}
		mAccountPhoneTv.setText(phone);
	}

	private void setUserEmail(String email) {
		if (TextUtils.isEmpty(email)) {
			email = getString(R.string.unbind);
		} else {
			email = CommonUtil.hideEmail(email);
		}
		mAccountEmailTv.setText(email);
	}

	/**
	 * 从服务器查询最新的账户信息
	 */
	private void loadUserInfo() {
		// showProgressDialog(getString(R.string.account_info_get_user_info));
		mAccountManager.getUserInfo(new DataResponse<UserLoginObject>() {
			public void run() {
				// dismissProgressDialog();
				if (value != null) {
					Log.i(TAG, "the value=" + value.getCode());
					if (value.getCode() == UserLoginObject.CODE_SUCCESS) {
						// 下边这些信息有可能在服务器被更新了，需要刷新界面
						setUserNick(mPref.getUserNick());
						setUserPhone(mPref.getUserPhone());
						setUserEmail(mPref.getUserEmail());
						mStorageAvailableTv.setText(FormatUtil
								.formatStorageSize(mPref.getStorageAvailable()));

						new IconLoader(AccountInfoActivity.this)
								.loadLatestPhoto();
					} else {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil
								.shortToast(R.string.account_sync_error_failed_to_get_info);
					}
				}
			}
		}, this, mPref.getUserID(), mPref.getUserKey());
	}

	private static class IconLoader extends PhotoLoader<AccountInfoActivity> {
		public IconLoader(AccountInfoActivity target) {
			super(target);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			AccountInfoActivity target = mTarget.get();
			if (target != null) {
				if (mErrorMsgResId != -1) {
					ToastUtil.shortToast(mErrorMsgResId);
					return;
				}

				if (result != null) {
					target.mAccountIconIv.setImageBitmap(result);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logout_btn:
			doLogout();
			break;
		case R.id.account_nickname_ly:
			setNick();
			break;
		case R.id.account_pwd_ly:
			changePwd();
			break;
		case R.id.account_phone_ly:
			setOrChangePhone();
			break;
		case R.id.account_icon_iv:
			changeAccountIcon();
			break;
		case R.id.account_email_ly:
			setOrChangeEmail();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BitmapUtil.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (resultCode == Activity.RESULT_OK) {
				cropIcon(mCurrentPhotoFile);
			}
			break;
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
					FileLog.e(TAG, e.getMessage());
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
					//converting the input stream into file to crop the 
					//selected image from sd-card.
					input = getApplicationContext().getContentResolver().openInputStream(selectedImage);
					try {
						mPickPhotoKitkatFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					} catch(Exception e) {
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
		default:
			break;
		}
	}

	private void deleteTempPhotoFile() {
		if (mCurrentPhotoFile != null && mCurrentPhotoFile.exists()) {
			mCurrentPhotoFile.delete();
		}
		mCurrentPhotoFile = null;
	}

	private void deleteTempCropFile() {
		if (mCurrentCropFile != null && mCurrentCropFile.exists()) {
			mCurrentCropFile.delete();
		}
		mCurrentCropFile = null;
	}
	
	private void deletePickPhotoKitkatFile() {
		if (mPickPhotoKitkatFile != null && mPickPhotoKitkatFile.exists()) {
			mPickPhotoKitkatFile.delete();
		}
		mPickPhotoKitkatFile = null;
	}

	private static class ChangePhotoTask extends
			AsyncTask<Uri, Integer, Bitmap> {
		private WeakReference<AccountInfoActivity> mTarget;
		private Uri mUri;

		public ChangePhotoTask(AccountInfoActivity target) {
			mTarget = new WeakReference<AccountInfoActivity>(target);
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			mUri = params[0];
			String path = mUri.getPath();
			AccountInfoActivity target = mTarget.get();
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
			AccountInfoActivity target = mTarget.get();
			if (target != null) {
				if (result != null) {
					target.mAccountIconIv.setImageBitmap(result);
					target.changePhoto();
				} else {
					ToastUtil
							.shortToast(R.string.account_info_error_photo_file_not_exist);
				}
			}
		}
	}

	private void changePhoto() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(mCurrentCropFile);
		} catch (FileNotFoundException e) {
			FileLog.e(TAG, e.getMessage());
			ToastUtil
					.shortToast(R.string.account_info_error_photo_file_not_exist);
			return;
		}

		showProgressDialog(getString(R.string.set_nick_saving));
		mAccountManager.changePhoto(new DataResponse<ChangePhotoRespObject>() {
			@Override
			public void run() {
				Log.i(TAG, "the value=" + value.getCode());
				dismissProgressDialog();
				// add by zw 02-13 上传头像时
				/*new TotalCount(AccountInfoActivity.this, "280", "005", 1)
						.CountData();*/
				// end by zw
				if (value.getCode() == ChangePhotoRespObject.CODE_SUCCESS) {
					handleChangePhotoSuccess();
					ToastUtil.shortToast(R.string.update_success);
				} else {
					Log.d(TAG, "Jim, error: " + value.getDesc());
					// add by zw 02-13 上传头像未成功时
					/*new TotalCount(AccountInfoActivity.this, "280", "006", 1)
							.CountData();*/
					// end by zw
					handleChangePhotoError(AccountInfoActivity.this
							.getString(R.string.account_info_error_upload_photo_failed));
				}
			}
		}, this, mPref.getUserID(), mPref.getUserKey(), IconLoader
				.getFinalIconFile().getName(), fis);
	}

	private void handleChangePhotoSuccess() {
		if (mCurrentCropFile != null && mCurrentCropFile.exists()) {
			File dstFile = IconLoader.getFinalIconFile();
			if (dstFile.exists()) {
				dstFile.delete();
			}
			mCurrentCropFile.renameTo(dstFile);
		}
		mCurrentCropFile = null;
	}

	private void handleChangePhotoError(String msg) {
		Dialog dialog = DialogUtil.getAlertDialog(this, 
				msg, 
				R.string.dialog_cancel, 
				R.string.account_info_retry, new IAlertDialogClick() {
			
			@Override
			public void sureClick() {
				// TODO Auto-generated method stub
				changePhoto();
			}
			
			@Override
			public void cancelClick() {
				// TODO Auto-generated method stub
				// 删除上传失败的图片文件
				deleteTempCropFile();

				Bitmap icon = IconLoader.loadLocalCacheIcon();
				if (icon != null) {
					mAccountIconIv.setImageBitmap(icon);
				} else {
					mAccountIconIv
							.setImageResource(R.drawable.default_account_icon);
				}
			}
		});
		dialog.show();
//		new AuroraAlertDialog.Builder(AccountInfoActivity.this)
//				.setTitle(R.string.dialog_prompt)
//				.setMessage(msg)
//				.setPositiveButton(R.string.account_info_retry,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								changePhoto();
//							}
//						})
//				.setNegativeButton(R.string.dialog_cancel,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								// 删除上传失败的图片文件
//								deleteTempCropFile();
//
//								Bitmap icon = IconLoader.loadLocalCacheIcon();
//								if (icon != null) {
//									mAccountIconIv.setImageBitmap(icon);
//								} else {
//									mAccountIconIv
//											.setImageResource(R.drawable.default_account_icon);
//								}
//							}
//						}).create().show();
	}

	private void changeAccountIcon() {
		DialogUtil.getItemAlertDialog(this,
				R.string.account_info_set_icon_title,
				R.array.setAccountIconMenus, new IAlertDialogItemClick() {

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
		
//		new AlertDialog.Builder(this)
//				.setTitle(R.string.account_info_set_icon_title)
//				.setItems(R.array.setAccountIconMenus,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								switch (which) {
//								case SET_ICON_MENU_PHOTO:
//									openCamera();
//									break;
//								case SET_ICON_MENU_GALLERY:
//									openGallery();
//									break;
//								}
//							}
//						}).create().show();
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
	 * 相册选取
	 * 
	 * @param type
	 */
	private void openGallery() {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		// 限制为只能用我们系统自带的Gallery来选择
//		intent.setAction("com.aurora.filemanager.SINGLE_GET_CONTENT");
		
		//intent.setPackage("com.android.gallery3d");
		
		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
			startActivityForResult(intent,
					BitmapUtil.REQUEST_CODE_GETIMAGE_BYSDCARD);
		} else {
			mPickPhotoKitkatFile = IconLoader.getPickPhotoKitkatFile();
			
			startActivityForResult(intent,
					BitmapUtil.REQUEST_CODE_GETIMAGE_BYSDCARD_KITKAT);
		}

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

	private void setOrChangePhone() {
		if (TextUtils.isEmpty(mPref.getUserPhone())) {
			Intent intent = new Intent(this, ChangePhoneNumActivity.class);
			intent.putExtra(ChangePhoneNumActivity.EXTRA_KEY_MODE,
					ChangePhoneNumActivity.MODE_SET);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, ValidatePwdActivity.class);
			intent.putExtra(ValidatePwdActivity.EXTRA_KEY_MODE,
					ValidatePwdActivity.MODE_PHONE);
			startActivity(intent);
		}
	}

	private void setOrChangeEmail() {
		if (TextUtils.isEmpty(mPref.getUserEmail())) {
			Intent intent = new Intent(this, ChangeEmailActivity.class);
			intent.putExtra(ChangeEmailActivity.EXTRA_KEY_MODE,
					ChangeEmailActivity.MODE_SET);
			startActivity(intent);
		} else {
			// Intent intent = new Intent(this, ValidatePwdActivity.class);
			// intent.putExtra(ValidatePwdActivity.EXTRA_KEY_MODE,
			// ValidatePwdActivity.MODE_EMAIL);
			Intent intent = new Intent(this, ChangeEmailActivity.class);
			intent.putExtra(ChangeEmailActivity.EXTRA_KEY_MODE,
					ChangeEmailActivity.MODE_REBIND);
			startActivity(intent);
		}
	}

	private void changePwd() {
		startActivity(ChangePwdActivity.class);
	}

	private void setNick() {
		startActivity(SetNickActivity.class);
	}

	@Override
	protected void onProgressDialogDismiss() {
		mLogoutView.setEnabled(true);
	}

	private void performLogout() {
		// add by zw 02-13 退出登录次数
		/*new TotalCount(AccountInfoActivity.this, "280", "011", 1).CountData();*/
		// end by zw

		if (SystemUtils.isNetworkConnected()) {
			showProgressDialog(getString(R.string.account_info_wait_logout));

			mAccountManager.logout(new DataResponse<BaseResponseObject>() {
				@Override
				public void run() {
					Log.i(TAG, "the value=" + value.getCode());
					dismissProgressDialog();
					if (value.getCode() == BaseResponseObject.CODE_SUCCESS) {
						postLogout();
					} else {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil
								.shortToast(R.string.account_info_error_falied_to_logout);
					}
				}
			}, this, mPref.getUserID(), mPref.getUserKey());
		} else {
			// 没网络，先退到登录界面，有网络的时候再通知服务器
			mPref.setPendingUserId(mPref.getUserID());
			mPref.setPendingUserKey(mPref.getUserKey());

			postLogout();
			mLogoutView.setEnabled(true);
		}
	}

	private void doLogout() {
		mLogoutView.setEnabled(false);
		performLogout();
	}

	private void postLogout() {
		mPref.clear();
		Intent b = new Intent(Globals.LOCAL_LOGIN_ACTION);
		AccountHelper.logout();
		b.putExtra(Globals.LOCAL_LOGIN_RESULT, Globals.LOCAL_LOGOUT_SUCCESS);
		sendBroadcast(b);
		BooleanPreferencesUtil.getInstance(getApplicationContext()).setLogin(
				false);

		Intent data = new Intent();
		data.putExtra(Globals.EXTRA_COMMAND, Globals.COMMAND_LOGOUT);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	protected void onChange(String eventType) {
		if (EventsType.EVENT_UPDATE_NICKNAME.equals(eventType)) {
			setUserNick(mPref.getUserNick());
		} else if (EventsType.EVENT_UPDATE_PHONE.equals(eventType)) {
			setUserPhone(mPref.getUserPhone());
		}
	}

	@Override
	protected String[] getObserverEventType() {
		return new String[] { EventsType.EVENT_UPDATE_NICKNAME,
				EventsType.EVENT_UPDATE_PHONE };
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

}