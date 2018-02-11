package com.aurora.note.activity.manager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraSwitch;

import com.aurora.note.R;
import com.aurora.note.activity.NewNoteActivity;
import com.aurora.note.alarm.NoteAlarmReceiver;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.db.NoteAdapter;
import com.aurora.note.report.ReportCommand;
import com.aurora.note.report.ReportUtil;
import com.aurora.note.util.BitmapUtil;
import com.aurora.note.util.BooleanPerfencesUtil;
import com.aurora.note.util.Globals;
import com.aurora.note.util.SDcardManager;
import com.aurora.note.util.ToastUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

public class NoteManageActivity extends AuroraActivity implements View.OnClickListener {

	private static final String BACKUP_FILE_SUFFIX = ".bak";
	private static final int REQUEST_CODE_DO_EXPRESS = 520;

	private Context mContext;
	private IWXAPI wxApi;

	private NoteAdapter mNoteAdapter;

	private int mWhichRestore;
	private boolean mIsChanged = false;
	
	private AuroraSwitch mAutoIndentSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		wxApi = WXAPIFactory.createWXAPI(this, Globals.APP_ID);

		setAuroraContentView(R.layout.note_manage_activity, AuroraActionBar.Type.Normal);

		initDB();
		initActionBar();
		initMenu();
		initView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_DO_EXPRESS) {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		closeDB();
	}

	@Override
	public void finish() {
		if (mIsChanged) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
		super.finish();
	}

	private void initDB() {
		mNoteAdapter = new NoteAdapter(this);
		mNoteAdapter.open();
	}

	private void closeDB() {
		if (mNoteAdapter != null) {
			mNoteAdapter.close();
			mNoteAdapter = null;
		}
	}

	private void initActionBar() {
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.setTitle(R.string.action_settings);
	}

	private void initMenu() {
		setAuroraSystemMenuCallBack(new OnAuroraMenuItemClickListener() {
			@Override
			public void auroraMenuItemClick(int itemId) {
				switch (itemId) {
				case R.id.action_share_weibo:
					gotoShareActivity();
					break;
				case R.id.action_share_wx_timeline:
					shareToWx(false);
					break;
				case R.id.action_share_wx_session:
					shareToWx(true);
					break;
				default:
					break;
				}
			}
		});

		setAuroraMenuItems(R.menu.share_note);

		int wxSdkVersion = wxApi.getWXAppSupportAPI();
        if (wxSdkVersion < NoteShareActivity.TIMELINE_SUPPORTED_VERSION) {
            if (wxSdkVersion == 0) {
            	removeAuroraMenuItemById(R.id.action_share_wx_session);
            }
            removeAuroraMenuItemById(R.id.action_share_wx_timeline);
        }
	}

	private void shareToWx(boolean isWxSession) {
        if (isWxSession) {
        	ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_CHATS_2);
			command.updateData();
        } else {
        	ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_MOMENTS_2);
			command.updateData();
        }

		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://www.iunios.com/memo.shtml";

		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = webpage;
		msg.title = getString(R.string.note_share_wx_title);
		msg.description = getString(R.string.note_share_wx_summary);

		Bitmap thumbBmp = BitmapFactory.decodeResource(getResources(), R.drawable.note_share_logo);
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

	private void initView() {
		TextView backupView = (TextView) findViewById(R.id.backup_view);
		backupView.setOnClickListener(this);
		TextView restoreView = (TextView) findViewById(R.id.restore_view);
		restoreView.setOnClickListener(this);
		TextView aboutView = (TextView) findViewById(R.id.about_view);
		aboutView.setOnClickListener(this);
		TextView shareView = (TextView) findViewById(R.id.share_view);
		shareView.setOnClickListener(this);
		TextView expressView = (TextView) findViewById(R.id.express_view);
		expressView.setOnClickListener(this);
		
		mAutoIndentSwitch = (AuroraSwitch) findViewById(R.id.auto_indent_switch);
		mAutoIndentSwitch.setChecked(BooleanPerfencesUtil.isAutoIndent());
		mAutoIndentSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BooleanPerfencesUtil.setAutoIndent(isChecked);
            }
        });
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backup_view:
			doBackup();
			break;
		case R.id.restore_view:
			doRestore();
			break;
		case R.id.about_view:
			gotoAboutActivity();
			break;
		case R.id.share_view:
			showAuroraMenu();
			break;
		case R.id.express_view:
			doExpress();
			break;
		}
	}

	private void doExpress() {
		ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_TOTA);
		command.updateData();

        long currentMillis = System.currentTimeMillis();
        String presetQingrenjieContent = mContext.getString(R.string.preset_qingrenjie_content);

        NoteResult presetNote = new NoteResult();

        presetNote.setIs_preset(2);
        presetNote.setUuid(UUID.randomUUID().toString());
        presetNote.setContent(Globals.PRESET_IMAGE_QINGRENJIE_TEXT + Globals.NEW_LINE + presetQingrenjieContent);
        presetNote.setCharacter(presetQingrenjieContent);
        presetNote.setImage_count(1);
        presetNote.setIs_warn(0);
        presetNote.setCreate_time(currentMillis);
        presetNote.setUpdate_time(currentMillis);

        long noteId = mNoteAdapter.insert(presetNote);
        mIsChanged = true;

        Bundle bundle = new Bundle();
		bundle.putInt(NewNoteActivity.TYPE_GET_DATA, 1);
		bundle.putInt(NewNoteActivity.NOTE_ID, (int) noteId); 

		Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtras(bundle);
		startActivityForResult(intent, REQUEST_CODE_DO_EXPRESS);
	}

	private void gotoShareActivity() {
		ReportCommand command = new ReportCommand(this, ReportUtil.TAG_WEIBO_2);
		command.updateData();

		Intent intent = new Intent(this, NoteShareActivity.class);
		startActivity(intent);
	}

	private void gotoAboutActivity() {
		Intent intent = new Intent(this, NoteAboutActivity.class);
		startActivity(intent);
	}

	private void doRestore() {
		if (!SDcardManager.checkSDCardMount()) {
			ToastUtil.longToast(R.string.sdcard_not_mounted);
			return;
		}

		if (!Globals.BACKUP_DIR.exists()) {
			ToastUtil.shortToast(R.string.note_restore_none);
		} else {
			String[] fileNames = Globals.BACKUP_DIR.list();
			if (fileNames == null || fileNames.length == 0) {
				ToastUtil.shortToast(R.string.note_restore_none);
				return;
			}

			ArrayList<String> fileNameList = new ArrayList<String>();
			for (String fileName : fileNames) {
				if (fileName.endsWith(BACKUP_FILE_SUFFIX)) {
					fileNameList.add(fileName.substring(0, fileName.length() - BACKUP_FILE_SUFFIX.length()));
				}
			}

			if (fileNameList.isEmpty()) {
				ToastUtil.shortToast(R.string.note_restore_none);
				return;
			}

			Collections.sort(fileNameList, new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					if (lhs != null && rhs != null) {
						return rhs.compareTo(lhs);
					}
					return 0;
				};
			});

			final String[] validFileNames = new String[fileNameList.size()];
			fileNameList.toArray(validFileNames);

			AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.note_restore_choose)
				.setTitleDividerVisible(true)
				.setSingleChoiceItems(validFileNames, mWhichRestore, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mWhichRestore = which;
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String fileName = validFileNames[mWhichRestore] + BACKUP_FILE_SUFFIX;
						new RestoreTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName);
					}
				})
				.show();
		}
	}

	private void doBackup() {
		if (!checkSDCard()) {
			return;
		}

		ArrayList<NoteResult> noteList = mNoteAdapter.queryDataForBackup();
		if (noteList == null || noteList.isEmpty()) {
			ToastUtil.shortToast(R.string.note_backup_none);
			return;
		}

		final String fileName = getBackupFileName();
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
			.setTitle(R.string.note_backup)
			.setTitleDividerVisible(true)
			.setMessage(getString(R.string.note_backup_to) + "\"" + Globals.BACKUP_DIR.getPath() + "/" + fileName + "\"")
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new BackupTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName);
				}
			})
			.show();
	}

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

	private String getBackupFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'Memo'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + BACKUP_FILE_SUFFIX;
	}

	private class BackupTask extends AsyncTask<String, Void, String> {

		private boolean hasError = false;
		private AuroraProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = AuroraProgressDialog.show(mContext, null, getResources().getString(R.string.note_backup_doing), true, false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String fileName = params[0];

			try {
				if (!Globals.BACKUP_DIR.exists()) {
					Globals.BACKUP_DIR.mkdirs();
				}

				File realFile = new File(Globals.BACKUP_DIR, fileName);
				if (realFile.exists()) {
					realFile.delete();
				}

				File tempfile = new File(Globals.BACKUP_DIR, fileName + ".tmp");
				if (!tempfile.exists()) {
					tempfile.createNewFile();
				} else {
					tempfile.delete();
					tempfile.createNewFile();
				}

				ArrayList<NoteResult> noteList = mNoteAdapter.queryDataForBackup();

				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempfile, true));
				oos.writeObject(noteList);
				oos.flush();
				oos.close();
			} catch (IOException e) {
				hasError = true;
			} finally {
				File file = new File(Globals.BACKUP_DIR, fileName + ".tmp");
				if (file.exists()) {
					if (hasError) {
						file.delete();
					} else {
						file.renameTo(new File(Globals.BACKUP_DIR, fileName));
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (progressDialog.isShowing() && !NoteManageActivity.this.isFinishing()) {
				progressDialog.dismiss();
			}
			if (hasError) {
				ToastUtil.shortToast(R.string.note_backup_failed);
			} else {
				ToastUtil.shortToast(R.string.note_backup_success);

				ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_BACKUP);
				command.updateData();
			}
		}
	}

	private class RestoreTask extends AsyncTask<String, Void, String> {

		private boolean hasError = false;
		private AuroraProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = AuroraProgressDialog.show(mContext, null, getResources().getString(R.string.note_restore_doing), true, false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String fileName = params[0];
			File file = new File(Globals.BACKUP_DIR, fileName);
			if (!file.exists()) {
				hasError = true;
				return null;
			}

			ObjectInputStream ois = null;
			ArrayList<NoteResult> readList = null;

			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				readList = (ArrayList<NoteResult>) ois.readObject();
			} catch (ClassNotFoundException e) {
				hasError = true;
				readList = null;
			} catch (IOException e) {
				hasError = true;
				readList = null;
			} finally {
				try {
					ois.close();
				} catch (IOException e) {
					hasError = true;
					readList = null;
				}
			}

			if (readList != null && !readList.isEmpty()) {
				ArrayList<String> uuidList = mNoteAdapter.queryNoteUUID();
				if (uuidList == null || uuidList.isEmpty()) {
					mNoteAdapter.insert(readList);
				} else {
					ArrayList<NoteResult> doInsertList = new ArrayList<NoteResult>();
					for (NoteResult note : readList) {
						if (!uuidList.contains(note.getUuid())) {
							doInsertList.add(note);
						}
					}
					mNoteAdapter.insert(doInsertList);
				}
				NoteAlarmReceiver.scheduleAlarms();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (progressDialog.isShowing() && !NoteManageActivity.this.isFinishing()) {
				progressDialog.dismiss();
			}
			if (hasError) {
				ToastUtil.shortToast(R.string.note_restore_failed);
			} else {
				ToastUtil.shortToast(R.string.note_restore_success);
				mIsChanged = true;

				ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_RESTORE);
				command.updateData();
			}
		}

	}

}