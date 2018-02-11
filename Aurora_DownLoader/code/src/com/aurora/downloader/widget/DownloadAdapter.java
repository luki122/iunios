package com.aurora.downloader.widget;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Downloads;
import android.provider.MediaStore.Files.FileColumns;
import aurora.widget.AuroraCheckBox;
import aurora.widget.IAuroraWidget;

import com.aurora.downloader.DownloadActivity;
import com.aurora.downloader.FileInfo;
import com.aurora.downloader.R;
import com.aurora.downloader.util.AuroraLog;
import com.aurora.downloader.util.DownloadStateUtil;
import com.aurora.downloader.util.DownloadStateUtil.Status;
import com.aurora.downloader.util.FileIconHelper;
import com.aurora.downloader.util.Util;
import com.aurora.downloader.widget.DownloadButton.DownloadButtonListener;

/**
 * 下载适配器
 * 
 * @author jiangxh
 * @CreateTime 2014年6月9日 下午3:32:11
 * @Description com.aurora.downloader.widget DownloadAdapter.java
 */
public class DownloadAdapter extends CursorAdapter {

	private static final String TAG = "DownloadAdapter";

	private Integer sMaxLength = 700;

	private Integer rMaxLength = 350;

	private AuroraDownLoadListViewCache auroraDownLoadListViewCache;
	private DownloadActivity downloadActivity;
	private LayoutInflater layoutInflater;
	private DateFormat mDateFormat;
	private SimpleDateFormat mTimeFormat;

	private boolean isOperation;

	/**
	 * @param isOperation
	 *            the isOperation to set
	 */
	public void setOperation(boolean isOperation) {
		this.isOperation = isOperation;
	}

	/**
	 * @return the isOperation
	 */
	public boolean isOperation() {
		return isOperation;
	}

	private List<FileInfo> selectPath;

	/**
	 * @return the selectPath
	 */
	public List<FileInfo> getSelectPath() {
		return selectPath;
	}

	/**
	 * @param selectPath
	 *            the selectPath to set
	 */
	public void setSelectPath(List<FileInfo> selectPath) {
		this.selectPath = selectPath;
	}

	private Cursor mCursor;

	private final int mTitleColumnId;
	private final int mDescriptionColumnId;
	private final int mStatusColumnId;
	private final int mReasonColumnId;
	private final int mTotalBytesColumnId;
	private final int mMediaTypeColumnId;
	private final int mDateColumnId;
	private final int mIdColumnId;
	private final int mFileNameColumnId;
	// add to support progress bar
	private final int mCurrentBytesColumnId;
	private int mControl;

	private FileIconHelper fileIconHelper;

	public FileIconHelper getFileIconHelper() {
		return fileIconHelper;
	}

	private int itemHeight;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// AuroraLog.elog(TAG, "isNeedAnim = false+++++");
			isNeedAnim = false;
			setAotuChanged(true);
		}
	};

	private boolean isNeedAnim = false;

	public void isShowAnim() {
		isNeedAnim = true;
		Message message = handler.obtainMessage();
		handler.sendMessage(message);
	}

	public DownloadAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		downloadActivity = (DownloadActivity) context;
		layoutInflater = LayoutInflater.from(context);
		mDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		mTimeFormat = new SimpleDateFormat("HH:mm");
		this.mCursor = cursor;
		fileIconHelper = FileIconHelper.getInstance(downloadActivity);

		mIdColumnId = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
		mTitleColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
		mDescriptionColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_DESCRIPTION);
		mStatusColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
		mReasonColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
		mTotalBytesColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
		mMediaTypeColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
		mDateColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
		mFileNameColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME);
		mCurrentBytesColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
		mControl = cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_CONTROL);
		// itemHeight = context.getResources().getInteger(R.integer.list_item);
		itemHeight = (int) context.getResources().getDimension(
				R.dimen.list_item_dp);
		sMaxLength = (int) context.getResources().getDimension(
				R.dimen.list_title_max_length);
		rMaxLength = (int) context.getResources().getDimension(
				R.dimen.list_title_min_length);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = (View) layoutInflater.inflate(
				com.aurora.R.layout.aurora_slid_listview, null);
		RelativeLayout main = (RelativeLayout) convertView
				.findViewById(com.aurora.R.id.aurora_listview_front);
		layoutInflater.inflate(R.layout.aurora_download_item, main);
		auroraDownLoadListViewCache = new AuroraDownLoadListViewCache(
				convertView);
		convertView.setTag(auroraDownLoadListViewCache);
		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		AuroraLog.log(TAG, "bindView");
		if (view.getHeight() >= itemHeight) {
			itemHeight = view.getHeight();
		}
		auroraDownLoadListViewCache = (AuroraDownLoadListViewCache) view
				.getTag();

		AuroraCheckBox checkBox = auroraDownLoadListViewCache.getCheckBox();

		if (selectPath != null && selectPath.size() > 0) {
			// AuroraLog.elog(TAG,
			// "selectPath.contains(Util.getFileInfo(cursor))=="
			// + selectPath.contains(Util.getFileInfo(cursor)));
			if (selectPath.contains(Util.getFileInfo(cursor))) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}
		} else {
			checkBox.setChecked(false);
		}

		String _data = cursor.getString(mFileNameColumnId);

		String title = "";
		if (TextUtils.isEmpty(_data)) {
			title = context.getResources().getString(R.string.missing_title);
		} else {
			title = Util.getNameFromFilepath(_data);
		}
		setTextForView(R.id.download_title, title);
		setTextForView(R.id.modified_time, getDateString());

		long downloadId = mCursor.getLong(mIdColumnId);
		int downloadStatus = mCursor.getInt(mStatusColumnId);
		int control = mCursor.getInt(mControl);
		int reason = mCursor.getInt(mReasonColumnId);

		Status status = DownloadStateUtil.getDownloadStates(downloadStatus,
				control);
		AuroraLog.elog(TAG, "downloadId==" + downloadId + " reason==" + reason
				+ " status==" + status.name());
		DownloadButton downloadButton = auroraDownLoadListViewCache
				.getDownload_control();
		downloadButton.setDownloadButtonListener(downloadButtonListener,
				downloadId);
		downloadButton.setDownloadStatus(status);
		AuroraCheckBox checkbox = auroraDownLoadListViewCache.getCheckBox();
		RelativeLayout mainUi = auroraDownLoadListViewCache.getLayoutMainUi();

		if (isOperation) {
			if (isNeedAnim) {
				aurora.widget.AuroraListView.auroraStartCheckBoxAppearingAnim(
						mainUi, checkbox);
			} else {
				aurora.widget.AuroraListView.auroraSetCheckBoxVisible(mainUi,
						checkbox, true);
			}
		} else {
			if (isNeedAnim) {
				aurora.widget.AuroraListView
						.auroraStartCheckBoxDisappearingAnim(mainUi, checkbox);
			} else {
				aurora.widget.AuroraListView.auroraSetCheckBoxVisible(mainUi,
						checkbox, false);
			}
		}
		ViewGroup.LayoutParams vl = view.getLayoutParams();
		if (null != vl) {
			AuroraLog.log(TAG, "itemHeight==" + itemHeight);
			vl.height = itemHeight;
			view.setLayoutParams(vl);
			view.findViewById(com.aurora.R.id.content).setAlpha(255);
		}

		long totalBytes = mCursor.getLong(mTotalBytesColumnId);
		long currentBytes = mCursor.getLong(mCurrentBytesColumnId);
		ProgressBar progressBar = auroraDownLoadListViewCache
				.getDownload_progress();

		setViewVisibility(R.id.status_text, true);
		setViewVisibility(R.id.size_text, true);
		setViewVisibility(R.id.modified_time, false);
		setViewVisibility(R.id.totalSize, false);
		switch (status) {
		case RUNNING:
			progressBar.setVisibility(View.VISIBLE);
			StringBuilder sbCurrentBytes = new StringBuilder();
			if (totalBytes > 0) {
				int progressAmount = (int) (currentBytes * 100 / totalBytes);
				if (progressAmount > 100) {
					progressAmount = 100;
				}

				progressBar.setIndeterminate(false);
				progressBar.setProgress(progressAmount);
			} else {
				progressBar.setIndeterminate(true);
			}
			sbCurrentBytes.append(getCurrentSizeText(currentBytes));
			sbCurrentBytes.append("/");
			sbCurrentBytes.append(getSizeText());
			// AuroraLog.elog(TAG, "RUNNING.....");
			setTextForView(R.id.status_text, "");
			setViewVisibility(R.id.status_text, false);
			setTextForViewByTitle(title, true);
			setTextForView(R.id.size_text, sbCurrentBytes.toString());
			setViewVisibility(R.id.download_title, true);
			setIconImageView(false, cursor);
			// 更改DownloadButton 状态
			setResForButton(R.id.download_control, true, 0);

			break;
		case WAITING:
		case PAUSED:
			// AuroraLog.elog(TAG, "WAITING or PAUSED.....");
			setResForButton(R.id.download_control, true, 1);
			progressBar.setVisibility(View.VISIBLE);
			int progressAmount = 0;
			if (totalBytes > 0) {
				progressAmount = (int) (currentBytes * 100 / totalBytes);
				if (progressAmount > 100) {
					progressAmount = 100;
				}
			}
			progressBar.setIndeterminate(false);
			progressBar.setProgress(progressAmount);
			setTextForView(R.id.status_text, downloadActivity.getResources()
					.getString(getStatusStringId(status)));
			setTextForView(R.id.size_text, "");
			setViewVisibility(R.id.size_text, false);
			setTextForViewByTitle(title, true);
			setViewVisibility(R.id.download_title, true);
			setIconImageView(false, cursor);

			break;
		case FAILED:
			// AuroraLog.elog(TAG, "FAILED .....");
			progressBar.setVisibility(View.GONE);
			setTextForView(R.id.status_text, downloadActivity.getResources()
					.getString(getStatusStringId(status)));
			setTextForView(R.id.size_text, "");
			setViewVisibility(R.id.size_text, false);
			setTextForViewByTitle(title, true);
			setViewVisibility(R.id.download_title, true);
			setResForButton(R.id.download_control, false, 0);
			setIconImageView(false, cursor);

			break;
		case SUCESS:
			// AuroraLog.elog(TAG, "SUCESS .....");
			progressBar.setVisibility(View.GONE);
			setTextForView(R.id.status_text, downloadActivity.getResources()
					.getString(getStatusStringId(status)));
			setTextForView(R.id.size_text, getSizeText());
			setViewVisibility(R.id.status_text, false);
			setViewVisibility(R.id.size_text, false);
			setViewVisibility(R.id.modified_time, true);
			setTextForViewByTitle(title, false);
			setViewVisibility(R.id.download_title, true);
			setTextForView(R.id.totalSize, getSizeText());
			setViewVisibility(R.id.totalSize, true);
			setIconImageView(true, cursor);
			setResForButton(R.id.download_control, false, 0);

			break;

		default:
			throw new IllegalStateException("Unknown status: " + status);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		if (!downloadActivity.actionBarIsAnimRunning() && !isNeedAnim) {
			super.notifyDataSetChanged();
		}
	}

	private String getReason(int reason) {

		switch (reason) {
		case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
			return "PAUSED_QUEUED_FOR_WIFI";

		case DownloadManager.PAUSED_UNKNOWN:
			return "PAUSED_UNKNOWN";
		case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
			return "PAUSED_WAITING_FOR_NETWORK";

		case DownloadManager.PAUSED_WAITING_TO_RETRY:
			return "PAUSED_WAITING_TO_RETRY";

		case Downloads.Impl.STATUS_TOO_MANY_REDIRECTS:
			return "ERROR_TOO_MANY_REDIRECTS";

		case Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR:
			return "ERROR_INSUFFICIENT_SPACE";

		case Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR:
			return "ERROR_DEVICE_NOT_FOUND";

		case Downloads.Impl.STATUS_CANNOT_RESUME:
			return "ERROR_CANNOT_RESUME";

		case Downloads.Impl.STATUS_FILE_ALREADY_EXISTS_ERROR:
			return "ERROR_FILE_ALREADY_EXISTS";

		default:
			return "ERROR_UNKNOWN";
		}
	}

	private boolean isAotuChanged = true;

	/**
	 * @return the isAotuChanged
	 */
	public boolean isAotuChanged() {
		return isAotuChanged;
	}

	/**
	 * @param isAotuChanged
	 *            the isAotuChanged to set
	 */
	public void setAotuChanged(boolean isAotuChanged) {
		AuroraLog.log(TAG, "setAotuChanged==" + isAotuChanged);
		this.isAotuChanged = isAotuChanged;
	}

	@Override
	public void onContentChanged() {
		AuroraLog.log(TAG, "onContentChanged isAotuChanged==" + isAotuChanged);
		super.onContentChanged();
		downloadActivity.showEmptyOrEditView();
	}

	@Override
	public void onSelfContentChanged() {
		AuroraLog.elog(TAG, "onSelfContentChanged==" + isAotuChanged);
		if (isAotuChanged) {
			super.onSelfContentChanged();
		}
	}

	private DownloadButtonListener downloadButtonListener = new DownloadButtonListener() {

		@Override
		public void onClick(DownloadButton view, Status status, long resultCode) {
			downloadActivity.auroraSetRubbishBack();
			switch (status) {
			case RUNNING:
				if (com.aurora.downloader.util.NetWorkConectUtil
						.hasNetWorkConection(downloadActivity)) {
					AuroraLog.log(TAG,
							"RUNNING restartDownloadedFiles resultCode=="
									+ resultCode);
					downloadActivity.restartDownloadedFiles(resultCode);

				} else {
					Toast.makeText(downloadActivity, R.string.no_network,
							Toast.LENGTH_SHORT).show();
					downloadActivity.pauseDownloadedFiles(resultCode);
				}
				break;
			case PAUSED:
				AuroraLog.log(TAG, "PAUSED pauseDownloadedFiles resultCode=="
						+ resultCode);
				downloadActivity.pauseDownloadedFiles(resultCode);
				break;

			default:
				break;
			}
			onContentChanged();

		}
	};

	private void setTextForViewByTitle(CharSequence text, boolean running) {
		TextView view = auroraDownLoadListViewCache.getDownload_title();
		if (running) {
			text = Util.getEllipsize(text.toString(), view, rMaxLength);
		} else {
			text = Util.getEllipsize(text.toString(), view, sMaxLength);
		}
		view.setText(text);

	}

	private void setTextForView(int textViewId, CharSequence text) {
		TextView view = null;
		switch (textViewId) {
		case R.id.download_title:
			view = auroraDownLoadListViewCache.getDownload_title();
			break;
		// case R.id.download_title_done:
		// view = auroraDownLoadListViewCache.getDownload_title_done();
		// break;
		case R.id.modified_time:
			view = auroraDownLoadListViewCache.getLast_modified_time();
			break;
		case R.id.totalSize:
			view = auroraDownLoadListViewCache.getTotalSize();
			break;
		case R.id.status_text:
			view = auroraDownLoadListViewCache.getStatus_text();
			break;
		case R.id.size_text:
			view = auroraDownLoadListViewCache.getSize_text();
			break;

		default:
			break;
		}
		if (view != null)
			view.setText(text);
	}

	private void setViewVisibility(int resId, boolean isShow) {
		TextView view = null;
		switch (resId) {
		case R.id.download_title:
			view = auroraDownLoadListViewCache.getDownload_title();
			break;
		// case R.id.download_title_done:
		// view = auroraDownLoadListViewCache.getDownload_title_done();
		// break;
		case R.id.modified_time:
			view = auroraDownLoadListViewCache.getLast_modified_time();
			break;
		case R.id.totalSize:
			view = auroraDownLoadListViewCache.getTotalSize();
			break;
		case R.id.status_text:
			view = auroraDownLoadListViewCache.getStatus_text();
			break;
		case R.id.size_text:
			view = auroraDownLoadListViewCache.getSize_text();
			break;

		default:
			break;
		}
		if (view != null) {
			view.setVisibility(isShow == true ? View.VISIBLE : View.GONE);
		}
		if (R.id.download_title == resId) {
			// if (isOperation) {
			// view.setTextAppearance(downloadActivity,
			// R.style.aurora_file_name_select);
			// } else {
			view.setTextAppearance(downloadActivity,
					R.style.aurora_file_name_normal);
			// }
		} else {
			// if (isOperation) {
			// view.setTextAppearance(downloadActivity,
			// R.style.aurora_file_other_select);
			// } else {
			view.setTextAppearance(downloadActivity,
					R.style.aurora_file_other_normal);
			// }
		}

	}

	private String getDateString() {
		Date date = new Date(mCursor.getLong(mDateColumnId));
		if (date.before(getStartOfToday())) {
			return mDateFormat.format(date);
		} else {
			return mTimeFormat.format(date);
		}
	}

	private Date getStartOfToday() {
		Calendar today = new GregorianCalendar();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		return today.getTime();
	}

	private String getCurrentSizeText(long currentBytes) {
		if (currentBytes >= 0) {
			return Formatter.formatFileSize(downloadActivity, currentBytes);
		} else {
			return Formatter.formatFileSize(downloadActivity, 0);
		}
	}

	private String getSizeText() {
		long totalBytes = mCursor.getLong(mTotalBytesColumnId);
		String sizeText = "";
		if (totalBytes >= 0) {
			sizeText = Formatter.formatFileSize(downloadActivity, totalBytes);
		} else {
			sizeText = downloadActivity.getResources().getString(
					R.string.unknow);
		}
		return sizeText;
	}

	private int getStatusStringId(Status status) {
		switch (status) {
		case RUNNING:
			return R.string.download_running;
		case WAITING:
			return R.string.download_waiting;
		case PAUSED:
			return R.string.download_paused;
		case SUCESS:
			return R.string.download_success;
		case FAILED:
			return R.string.download_error;
		default:
			throw new IllegalStateException("Unknown status: " + status);
		}
	}

	private boolean isLock;

	public void setIconImage(boolean isLock) {
		this.isLock = isLock;
		if (isLock) {
			fileIconHelper.pause();
			;
		} else {
			fileIconHelper.resume();
		}
	}

	/**
	 * 设置ICON
	 * 
	 * @param sucess
	 * @param cursor
	 */
	private void setIconImageView(boolean sucess, Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			AuroraLog.elog(TAG, " cursor is null or closed");
		}
		FileInfo fileInfo = Util.getFileInfo(cursor);
		ImageView fileImage = auroraDownLoadListViewCache.getDownload_icon();
		fileImage.setVisibility(View.VISIBLE);
		if (fileInfo == null || !fileInfo.isExists) {
			fileImage.setImageResource(R.drawable.file_icon_deleted);
			AuroraLog.elog(TAG, " fileInfo is null or path no exists");
			return;
		}
		if (TextUtils.isEmpty(fileInfo.filePath)) {
			AuroraLog.elog(TAG, " icon path is null");
			return;
		}
		if (TextUtils.isEmpty(fileInfo.fileName)) {
			AuroraLog.elog(TAG, " icon name is null");
			return;
		}
		if (sucess) {
			if (fileIconHelper.ismPaused() && !isLock) {
				fileIconHelper.resume();
			}
			fileIconHelper.setIcon(fileInfo, fileImage);
		} else {
			int id = FileIconHelper.getFileIcon(Util
					.getExtFromFilename(fileInfo.fileName));
			if (id == 0) {
				fileImage.setImageResource(R.drawable.file_icon_default_light);
			} else {
				fileImage.setImageResource(id);
			}
		}
	}

	private void setResForButton(int textViewId, boolean isShow, int type) {
		if (textViewId == R.id.download_control) {
			DownloadButton view = auroraDownLoadListViewCache
					.getDownload_control();
			if (isShow && !isOperation) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.GONE);
			}

			switch (type) {
			case 0:
				view.setBackgroundResource(R.drawable.aurora_download_stop);
				break;
			case 1:
				view.setBackgroundResource(R.drawable.aurora_download_start);
				break;

			default:
				break;
			}
		}
	}

}
