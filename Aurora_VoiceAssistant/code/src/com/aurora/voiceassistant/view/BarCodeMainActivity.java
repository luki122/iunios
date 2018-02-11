package com.aurora.voiceassistant.view;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import com.aurora.voiceassistant.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.result.ResultHandler;
import com.google.zxing.result.ResultHandlerFactory;
import com.google.zxing.decoding.BarCodeMainActivityHandler;
import com.google.zxing.decoding.FinishListener;
import com.google.zxing.decoding.InactivityTimer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraButton;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import com.aurora.utils.SystemUtils;


public class BarCodeMainActivity extends AuroraActivity implements Callback {
	private final static String TAG = "BarCode";
	
	private static final int FLASH_BUTTON = 0;
	private AuroraActionBar mActionBar;
	
	private FrameLayout mScanningLayout;
	private FrameLayout mResultLayout;
	private BarCodeMainActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private TextView mResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	
	private Context mContext;
	private AuroraButton mPhotos;
	private AuroraButton mSearch;
	private AuroraButton mShare;
	private AuroraButton mAddContacts;
	private boolean mFlashLightState = false;
	
	private static final int GET_PHOTOS = 1;
	private static final int TRIM_PHOTO = 2;
	private static final int SHARE = 3;
	private String photo_path = null;
	private Bitmap scanBitmap;
	
	private LinearLayout mVcardLayout;
	
	ResultHandler resultHandler;
	
	private boolean notFoundFlag = false;
	private AlertDialog notFoundDialog = null;
		
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == 0) {
				if (handler == null) {
					handler = new BarCodeMainActivityHandler(BarCodeMainActivity.this, decodeFormats, characterSet);
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ZXingDemo", "onCreate----------------");
		setAuroraContentView(R.layout.vs_barcode_main, AuroraActionBar.Type.Normal);
		
		//M:shigq set status bar background as transparent for its flashing on Android5.0 platform begin
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//		getWindow().setStatusBarColor(Color.TRANSPARENT);
		//M:shigq set status bar background as transparent for its flashing on Android5.0 platform end
		
		mContext = this;
				
		mActionBar = getAuroraActionBar();
		changeActionBarAs(true);
		/*mActionBar.setTitle(R.string.title_scanner);
		mActionBar.setBackgroundColor(Color.BLACK);*/
		
		addFlashLightButtonOnState(mFlashLightState);
		
		//CameraManager
		CameraManager.init(getApplication());
		
		mScanningLayout = (FrameLayout) findViewById(R.id.scanning_layout);
		mResultLayout = (FrameLayout) findViewById(R.id.result_layout);
		mSearch = (AuroraButton) findViewById(R.id.search);
		mSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = getIntent();
				intent.putExtra("result", mResult.getText());
				
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		mShare = (AuroraButton) findViewById(R.id.share);
		mShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, "share");
				intent.putExtra(Intent.EXTRA_TEXT, mResult.getText());
//				startActivity(Intent.createChooser(intent, getResources().getString(R.string.vs_barcode_share_to)));
				startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.vs_barcode_share_to)), SHARE);
				
//				finish();
			}
		});
		mAddContacts = (AuroraButton) findViewById(R.id.addcontacts);
		mAddContacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (resultHandler != null) {
					resultHandler.addContacts();
				}
				
			}
		});
		mPhotos = (AuroraButton) findViewById(R.id.getphotos);
		mPhotos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						choosePhotosForScanning();
					}
				});
			}
		});
		
		mVcardLayout = (LinearLayout) findViewById(R.id.vcard_layout);

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

		mResult = (TextView) findViewById(R.id.result_text);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		showResultLayout(false);
	}
	
	@Override
	protected void onResume() {
		Log.d("ZXingDemo", "onResume----------------");
		super.onResume();
		
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
		if (getResultLayoutVisibility() == View.GONE) {
			setAddContactVisibility(View.GONE);
		}
		
	}

	@Override
	protected void onPause() {
		Log.d("ZXingDemo", "onPause----------------");
		super.onPause();
		
		dismissNotFoundDialog();
		
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		Log.d("ZXingDemo", "onDestroy----------------");
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	/*protected void  finish() {
		BarCodeMainActivity.this.
	}*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("ZXingDemo", "BarCodeMainActivity-------------------------------onActivityResult");
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case GET_PHOTOS:
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String[] proj = { MediaStore.Images.Media.DATA };
						// get the path of photo which is selected
						Cursor cursor = getContentResolver().query(data.getData(), proj, null, null, null);

						if (cursor.moveToFirst()) {
							int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
							photo_path = cursor.getString(column_index);
							if (photo_path == null) {
//								photo_path = Utils.getPath(getApplicationContext(), data.getData());
								photo_path = getSelectedPath(getApplicationContext(), data.getData());
								Log.d("ZXingDemo", "get the photo_path = "+photo_path);
							}
							Log.d("ZXingDemo", "get the final------------------photo_path = "+photo_path);
						}

						cursor.close();
						
						getBitmapForDecoding(photo_path);
					}
				}).start();

				break;
			case SHARE:
				//do nothing
				break;
				
			case TRIM_PHOTO:
				if (data != null) {
					releaseChoosedBitmap();
					scanBitmap = data.getParcelableExtra("data");
				}
				Log.d("ZXingDemo", "get crop image ++++++++++++++++++++++++++++++++++++++++++++ scanBitmap = "+scanBitmap);
				
				break;

			default:
				break;
			}
		} else {
			if (requestCode == SHARE) {
				//do nothing
			} else {
				Log.d("ZXingDemo", "the onActivity return -----------");			
//				mScanningLayout.setVisibility(View.VISIBLE);
				showResultLayout(false);
			}
		}
	}

	private void initCamera(final SurfaceHolder surfaceHolder) {
		//to prevent the UI thread blocked and do this operation in new thread
		Thread iniThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					CameraManager.get().openDriver(surfaceHolder);
				} catch (IOException ioe) {
					return;
				} catch (RuntimeException e) {
					return;
				}
				
				//back to UI thread to set up new BarCodeMainActivityHandler 
				Message msg = Message.obtain();
				msg.what = 0;
				mHandler.sendMessage(msg);

			}
		});
		iniThread.start();

	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG,"surfaceCreated hasSurface = "+hasSurface);
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG,"surfaceDestroyed hasSurface = "+hasSurface);
		hasSurface = false;
		CameraManager.get().closeDriver();

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		Log.d(TAG, "BarCodeMainActivity----------handleDecode");
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();
//		txtResult.setText(obj.getBarcodeFormat().toString() + ":" + obj.getText());
		 Log.d("ZXingDemo", "BarCodeMainActivity-------------------handleDecode-----obj = "+obj);
//		mResult.setText(obj.getText());
		
		resultHandler = ResultHandlerFactory.makeResultHandler(this, obj);
		handleDecodeInternally(obj, resultHandler, barcode);
		
		processResultText(obj);

		Message msg = Message.obtain();
		msg.what = R.id.return_scan_result;
//		msg.what = R.id.return_scan_result;
		handler.sendMessage(msg);
		
	}
		
    public static final boolean isChineseCharacter(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
        	//是否是Unicode编码,除了"�"这个字符.这个字符要另外处理
            if ((charArray[i] >= '\u0000' && charArray[i] < '\uFFFD')||((charArray[i] > '\uFFFD' && charArray[i] < '\uFFFF'))) {
                continue;
            } else{
            	return false;
            }
        }
        return true;
    }
	
	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
//		CharSequence displayContents = resultHandler.getDisplayContents();
		
		Log.d("ZXingDemo", "handleDecodeInternally---------resultHandler.getType = "+resultHandler.getType());
		switch (resultHandler.getType()) {
			case ADDRESSBOOK:
				addressBookInfoProcess(resultHandler);
				setAddContactVisibility(View.VISIBLE);
				break;
	
			default:
				String UTF_Str = "";
				String GB_Str = "";
				try {
					UTF_Str = new String(rawResult.toString().getBytes("ISO-8859-1"),"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					Log.d("ZXingDemo", "handleDecodeInternally-------------------ISO-8859-1  UTF-8-----Exception = "+e1);
				}
				Log.d("ZXingDemo", "handleDecodeInternally-------------------after UTF-8 = "+UTF_Str);
				
				boolean is_CN = isChineseCharacter(UTF_Str.toString());
				Log.d("ZXingDemo", "handleDecodeInternally---------UTF_Str.toString()  is_CN = "+is_CN);
				
				if (!is_CN) {
					try {
						GB_Str = new String(rawResult.toString().getBytes("ISO-8859-1"),"GB2312");
						Log.d("ZXingDemo", "handleDecodeInternally-------------------after GB2312 = "+GB_Str);
					} catch (Exception e) {
						// TODO: handle exception
						Log.d("ZXingDemo", "handleDecodeInternally-------------------ISO-8859-1  GB2312-8----Exception = "+e);
					}
				} else {
					GB_Str = rawResult.toString();
				}
				
				mResult.setText(GB_Str);
				break;
		}
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	private OnAuroraActionBarItemClickListener onbarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			Log.d(TAG, "onbarItemClickListener------------");
			mFlashLightState = !mFlashLightState;
			addFlashLightButtonOnState(mFlashLightState);
			CameraManager.get().openFlashLight(mFlashLightState);
		}
	};
	
	public void addFlashLightButtonOnState(boolean b) {
		AuroraActionBarItem item = mActionBar.getItem(FLASH_BUTTON);
		if (item != null) mActionBar.removeItem(FLASH_BUTTON);
		
		if (b) {
			mActionBar.addItem(R.drawable.vs_barcode_flash_enable, FLASH_BUTTON, null);
		} else {
			mActionBar.addItem(R.drawable.vs_barcode_flash_disable, FLASH_BUTTON, null);
		}
		mActionBar.setOnAuroraActionBarListener(onbarItemClickListener);
	}
	
	public void changeActionBarAs(boolean b) {
		if (b) {
			mActionBar.setTitle(R.string.vs_barcode_title_scanner);
			mActionBar.setBackgroundColor(Color.BLACK);
		} else {
			mActionBar.setTitle(R.string.vs_barcode_title_result);
//			mActionBar.setBackgroundColor(Color.WHITE);
			mActionBar.setBackgroundResource(com.aurora.R.drawable.aurora_action_bar_top_bg);
			
			TextView titleTextView = mActionBar.getTitleView();
			titleTextView.setTextColor(Color.BLACK);
			
			ImageButton titleButton = (ImageButton)mActionBar.getHomeButton();
//			titleButton.setBackgroundResource(com.aurora.R.drawable.aurora_action_bar_back);
			titleButton.setImageResource(com.aurora.R.drawable.aurora_action_bar_back);
			
			mActionBar.removeItem(FLASH_BUTTON);
		}
		
	}
	
	public void showResultLayout(boolean b) {
		if (b) {
			mScanningLayout.setVisibility(View.GONE);
			mResultLayout.setVisibility(View.VISIBLE);
			
			mResultLayout.setBackgroundColor(Color.WHITE);
		} else {
			mScanningLayout.setVisibility(View.VISIBLE);
			mResultLayout.setVisibility(View.GONE);
		}
	}
	
	public void setAddContactVisibility(int v) {
		mAddContacts.setVisibility(v);
		if (v == View.VISIBLE) {
			mSearch.setVisibility(View.GONE);
			mShare.setVisibility(View.GONE);
		} else if (v == View.GONE) {
			mSearch.setVisibility(View.VISIBLE);
			mShare.setVisibility(View.VISIBLE);
		}
	}
	
	public void processResultText(Result obj) {
		if (obj == null) return;
		
		//web link and then open it
		String resultString = obj.getText().toString();
		
		if (resultString.startsWith("http://") || resultString.startsWith("https://") || 
												  resultString.startsWith("www.")	  || 
												  resultString.startsWith("ftp://")) {
			if (resultString.startsWith("www.")) { 
				resultString = "http://" + resultString;
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultString));
            mContext.startActivity(intent);
            
            finish();
		} else {
			changeActionBarAs(false);
			showResultLayout(true);
			 
			//M:shigq status bar black set status bar black on Android5.0 platform begin
			//SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, mContext);
			//SystemUtils.setStatusBarBackgroundTransparent(this);
			//M:shigq status bar black set status bar black on Android5.0 platform end
		}
		
	}
	
	public int getResultLayoutVisibility() {
		return mResultLayout.getVisibility();
	}
	
	public void addressBookInfoProcess(ResultHandler resultHandler) {
		AddressBookParsedResult result = (AddressBookParsedResult) resultHandler.getResult();
		if (result == null) return;
		
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
		View vcardLayout = mLayoutInflater.inflate(R.layout.vs_barcode_vcard_layout, null);
		
		if (result.getNames() != null) {
	    	String name = result.getNames()[0];
	    	
	    	RelativeLayout nameLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_name_layout);
			nameLayout.setVisibility(View.VISIBLE);
			
			TextView nameTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_name_text);
			nameTextView.setText(name.trim());
	    	Log.d("ZXingDemo", "AddressBookResultHandle--------getDisplayContents-------result.getNames = "+name);
	    	
		}
		
	    if (result.getTitle() != null) {
	    	String title = result.getTitle().toString();
	    	
	    	RelativeLayout titleLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_title_layout);
			titleLayout.setVisibility(View.VISIBLE);
			
			TextView titleTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_title_text);
			titleTextView.setText(title.trim());
			Log.d("ZXingDemo", "AddressBookResultHandle---------getDisplayContents-------result.getTitle = "+title);
		}
	    
	    if (result.getOrg() != null) {
	    	String org = result.getOrg().toString();
			
			RelativeLayout orgLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_org_layout);
			orgLayout.setVisibility(View.VISIBLE);
			
			TextView orgTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_org_text);
			orgTextView.setText(org.trim());
			Log.d("ZXingDemo", "AddressBookResultHandle---------getDisplayContents-------result.getOrg = "+org);
		}
	    
	    if (result.getAddresses() != null) {
	    	String addr = result.getAddresses()[0];
			
			RelativeLayout addrLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_addr_layout);
			addrLayout.setVisibility(View.VISIBLE);
			
			TextView addrTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_addr_text);
			addrTextView.setText(addr.trim());
			Log.d("ZXingDemo", "AddressBookResultHandle---------getDisplayContents-------result.getAddresses = "+addr);
		}
	    
	    if (result.getPhoneNumbers() != null) {
	    	String tel = result.getPhoneNumbers()[0];
			
			RelativeLayout telLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_tel_layout);
			telLayout.setVisibility(View.VISIBLE);
			
			TextView telTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_tel_text);
			telTextView.setText(tel.trim());
	    	Log.d("ZXingDemo", "AddressBookResultHandle--------getDisplayContents-------result.getPhoneNumbers = "+tel);
		}
	    
	    if (result.getEmails() != null) {
	    	String mail = result.getEmails()[0];
			
			RelativeLayout mailLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_mail_layout);
			mailLayout.setVisibility(View.VISIBLE);
			
			TextView nameTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_mail_text);
			nameTextView.setText(mail.trim());
			Log.d("ZXingDemo", "AddressBookResultHandle---------getDisplayContents-------result.getEmails = "+mail);
		}
	    
	    if (result.getURLs() != null) {
	    	String url = result.getURLs()[0];
			
			RelativeLayout urlLayout = (RelativeLayout) vcardLayout.findViewById(R.id.vs_barcode_vcard_website_layout);
			urlLayout.setVisibility(View.VISIBLE);
			
			TextView urlTextView = (TextView) vcardLayout.findViewById(R.id.vs_barcode_vcard_website_text);
			urlTextView.setText(url.trim());
			Log.d("ZXingDemo", "AddressBookResultHandle---------getDisplayContents-------result.getURLs = "+url);
		}
	    /*if (result.getBirthday() != null) {
			Log.d("ZXingDemo", "AddressBookResultHandle---------getDisplayContents-------result.getBirthday = "+result.getBirthday().toString());
		}
	    if (result.getNote() != null) {
			Log.d("ZXingDemo", "AddressBookResultHandle--------getDisplayContents-------result.getNote = "+result.getNote().toString());
		}*/
	    
	    mVcardLayout.addView(vcardLayout);
	    
	}
	
	public void choosePhotosForScanning() {
		Log.d("ZXingDemo", "choosePhotosForScanning-------------------");
//		Intent innerIntent = new Intent(); // "android.intent.action.GET_CONTENT"
		
		Intent intent = new Intent("android.intent.action.PICK");
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
		
	    /*if (Build.VERSION.SDK_INT < 19) {
	        innerIntent.setAction(Intent.ACTION_GET_CONTENT);
	        Log.d("ZXingDemo", "sdk_int < 19");
	    } else {
	    	Log.d("ZXingDemo", "sdk_int > 19");
//	    	innerIntent.setAction(Intent.ACTION_GET_CONTENT);
	    	innerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//	        innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
	    }*/
	    	    
//	    innerIntent.setType("image/*");
//	    Intent wrapperIntent = Intent.createChooser(innerIntent, getResources().getString(R.string.vs_barcode_choosephotos));
//	    BarCodeMainActivity.this.startActivityForResult(wrapperIntent, GET_PHOTOS);
	    startActivityForResult(intent, GET_PHOTOS);
	    
	}
	
	public void trimPhotoForScanning() {

		if (photo_path == null) return;

		Uri uri = Uri.parse(photo_path);
		File filepath = new File(photo_path);
		
  		Intent intent = new Intent("com.android.camera.action.CROP")
  		.setDataAndType(Uri.fromFile(filepath), "image/*")
        .putExtra("crop", "true")
        .putExtra("aspectX", 1)
        .putExtra("aspectY", 1)
        .putExtra("outputX", 200)
        .putExtra("outputY", 200)
        .putExtra("scale", true)//是否保留比例，用于去除裁剪后黑边
        .putExtra("scaleUpIfNeeded", true)//拉伸，用于去除裁剪后黑边
        .putExtra("return-data", true)
        .putExtra("noFaceDetection", true)
        .putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

		startActivityForResult(intent, TRIM_PHOTO);
	}
	
	public void getBitmapForDecoding(String path) {
		releaseChoosedBitmap();
		if (TextUtils.isEmpty(path)) {
			return;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // get the original size first
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // get the new size

		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		
		Log.d("ZXingDemo", "the sanBitmap 2222222222222222 = "+scanBitmap);
	}
	
	public Bitmap getTransferedBitmap() {
		return scanBitmap;
	}
	
	public void releaseChoosedBitmap() {
		if (scanBitmap != null) {
			scanBitmap.recycle();
			scanBitmap = null;
		}
	}
	
	public void showNotFoundDialog() {
		notFoundDialog = new AlertDialog.Builder(BarCodeMainActivity.this)
		.setTitle(getResources().getString(R.string.vs_barcode_photo_tip_title))
		.setMessage(getResources().getString(R.string.vs_barcode_photo_tip_message))
		.setPositiveButton(getResources().getString(R.string.vs_barcode_photo_button_right), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
			}
		})
		.setNegativeButton(getResources().getString(R.string.vs_barcode_photo_button_left), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				trimPhotoForScanning();
			}
		}).show();
		
		notFoundFlag = true;
		notFoundDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				notFoundFlag = false;
				
				Message message = Message.obtain(mHandler, R.id.decode_failed);
		    	message.sendToTarget();
			}
		});
	}
	
	public boolean getNotFoundFlag() {
		return notFoundFlag;
	}
	
	public void dismissNotFoundDialog() {
		if (notFoundDialog != null) {
			if (notFoundDialog.isShowing()) {
				notFoundDialog.dismiss();
				notFoundDialog = null;
			}
		}
	}
	
	
	@SuppressLint("NewApi")
	public String getSelectedPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}
	
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

}
