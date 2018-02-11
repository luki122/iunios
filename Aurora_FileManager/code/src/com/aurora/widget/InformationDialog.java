package com.aurora.widget;

import java.io.File;

import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class InformationDialog extends aurora.app.AuroraAlertDialog {
	private static final String TAG = "InformationDialog";
	protected static final int ID_USER = 100;
	private FileInfo mFileInfo;
	private Context mContext;
	private View mView;
	private FileExplorerTabActivity activity;
	// add by jxh 2014-8-26 begin 判断是不是搜索界面
	private boolean isSearch = false;

	public InformationDialog(Context context, FileInfo f) {
		super(context);
		mFileInfo = f;
		mContext = context;
		activity = (FileExplorerTabActivity) mContext;
	}

	public InformationDialog(Context context, FileInfo f, boolean isSearch) {
		super(context);
		mFileInfo = f;
		mContext = context;
		activity = (FileExplorerTabActivity) mContext;
		this.isSearch = isSearch;
	}

	protected void onCreate(Bundle savedInstanceState) {
		mView = getLayoutInflater().inflate(R.layout.information_dialog, null);
		if (mFileInfo.IsDir) {
			asyncGetSize();
		}

		setTitle(mContext.getString(R.string.file_info_title));
		setCanceledOnTouchOutside(false);// touchu消失
		String filePath = mFileInfo.filePath.substring(0,
				mFileInfo.filePath.length() - mFileInfo.fileName.length());
		filePath = activity.getStorageName(filePath);
		((TextView) mView.findViewById(R.id.information_filename))
				.setText(mFileInfo.fileName);
		if (activity.isPribacyView()) {
			((TextView) mView.findViewById(R.id.information_location))
					.setText(activity
							.getString(R.string.p_workspace));
		} else {
			((TextView) mView.findViewById(R.id.information_location))
					.setText(filePath);
		}
		((TextView) mView.findViewById(R.id.information_size))
				.setText(formatFileSizeString(mFileInfo.fileSize));
		((TextView) mView.findViewById(R.id.information_modified)).setText(Util
				.formatInfoDateString(mContext, mFileInfo.ModifiedDate));
		((TextView) mView.findViewById(R.id.information_ishidden))
				.setText(mFileInfo.isHidden ? R.string.yes : R.string.no);

		setView(mView);
		if (activity.isOpenDir()
				&& !activity.isPribacyView()) {
			if (!mFileInfo.IsDir) {
				setButton(BUTTON_NEGATIVE,
						mContext.getString(R.string.open_dir),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								if (isSearch) {
									// LogUtil.elog(TAG,
									// "isSearch=="+isSearch);
									mHandler.postDelayed(new Runnable() {

										@Override
										public void run() {
											activity
													.hideSearchviewLayout();
										}
									}, 100);
									isSearch = false;
								}
								activity.unDoAllOperation();
								activity
										.setGofragment(activity
												.getCurrentFragment());
								activity
										.setGoCategory(activity
												.getNowfileCategory());
								activity
										.setGoFilePath(mFileInfo.filePath);
								activity.showFileViewPathByDialoh(Util
										.getPathFromFilepath(mFileInfo.filePath));
							}
						});
			}
			setButton(BUTTON_POSITIVE,
					mContext.getString(R.string.i_confirm_know),
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if (isSearch) {
								activity.setSearchKey("");
								activity.setSearchPostion(0);
								isSearch = false;
							}
						}
					});
		} else {
			setButton(BUTTON_NEGATIVE,
					mContext.getString(R.string.confirm_know),
					(DialogInterface.OnClickListener) null);
		}
		super.onCreate(savedInstanceState);
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ID_USER:
				Bundle data = msg.getData();
				long size = data.getLong("SIZE");
				((TextView) mView.findViewById(R.id.information_size))
						.setText(formatFileSizeString(size));
			}
		};
	};

	private AsyncTask task;

	@SuppressWarnings("unchecked")
	private void asyncGetSize() {
		task = new AsyncTask() {
			private long size;

			@Override
			protected Object doInBackground(Object... params) {
				String path = (String) params[0];
				size = 0;
				getSize(path);
				task = null;
				return null;
			}

			private void getSize(String path) {
				if (isCancelled())
					return;
				File file = new File(path);
				if (file.isDirectory()) {
					File[] listFiles = file.listFiles();
					if (listFiles == null)
						return;

					for (File f : listFiles) {
						if (isCancelled())
							return;

						getSize(f.getPath());
					}
				} else {
					size += file.length();
					onSize(size);
				}
			}

		}.execute(mFileInfo.filePath);
	}

	private void onSize(final long size) {
		Message msg = new Message();
		msg.what = ID_USER;
		Bundle bd = new Bundle();
		bd.putLong("SIZE", size);
		msg.setData(bd);
		mHandler.sendMessage(msg); // 向Handler发送消息,更新UI
	}

	private String formatFileSizeString(long size) {
		String ret = "";
		if (size >= 1024) {
			ret = Util.convertStorage(size);
		} else {
			ret = mContext.getResources().getString(R.string.file_size, size);
		}

		return ret;
	}
}
