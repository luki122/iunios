package com.android.gallery3d.setting.tools;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.os.AsyncTask.Status;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ProgressBar;
import aurora.app.AuroraProgressDialog;

import com.android.gallery3d.app.Log;
import com.android.gallery3d.app.AlbumPage.state;
import com.android.gallery3d.local.BasicActivity;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.StorageUtils;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.R;

public class SettingMediaFileOperationUtil {
	private Context mContext;
	private static final String TAG = "MediaFileOperationUtil";
	private static final String format = "format";
	public static final String external = "external";
	private static final int formatstyle = 12289;
	private static final Integer EOF = -1, DEFAULT_BUFFER_SIZE = 1024 * 4;

	public void setContext(Context mContext) {
		this.mContext = mContext;
	}

	private static SettingMediaFileOperationUtil operationUtil;

	public static SettingMediaFileOperationUtil getMediaFileOperationUtil(Context mContext) {
		if (operationUtil == null) {
			operationUtil = new SettingMediaFileOperationUtil();
		}
		operationUtil.setContext(mContext);
		return operationUtil;
	}

	/**
	 * 屏蔽
	 * @param albumsPath
	 * @return
	 */
	public boolean shieldOperation(List<String> albumsPath) {
		String albums = SettingLocalUtils.writeToString(albumsPath);
//		if (TextUtils.isEmpty(albums)) {
//			return false;
//		}
		return SettingLocalUtils.writeToXml(mContext, albums);

	}

	/**
	 * 取消屏蔽
	 * @param albumsPath
	 * @return
	 */
	public boolean unShieldOperation(List<String> albumsPath, List<String> allNoShowPaths) {
		if (allNoShowPaths.removeAll(albumsPath)) {
			String albums = SettingLocalUtils.writeToString(allNoShowPaths);
			return SettingLocalUtils.writeToXml(mContext, albums);
		}
		return false;
	}

	/**
	 * 分享
	 **/
	public void shareOperation(List<MediaFileInfo> selectFiles) {
		if (selectFiles == null || selectFiles.isEmpty()) {
			return;
		}
		if (selectFiles.size() > Integer.valueOf(mContext.getString(R.string.share_max))) {
			ToastUtils.showTast(mContext, R.string.share_too_much);
			return;
		}
		Intent intent = buildSendFile(selectFiles);
		try {
			mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.send_file)));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(TAG, "no app share " + e.getMessage());
		}
	}

	/**
	 * 删除多个相册
	 * @param albumsPath
	 * @param mediaFilesMap
	 * @return
	 */
	public boolean delAlbumsOperation(List<String> albumsPath, ConcurrentHashMap<String, List<MediaFileInfo>> mediaFilesMap) {
		boolean del = true;
		for (String path : albumsPath) {
			List<MediaFileInfo> medias = mediaFilesMap.get(path);
			del = del & delAlbumOperation(path, medias);
		}
		return del;
	}

	/**
	 * 删除相册
	 * @param albumPath
	 * @param medias
	 * @return
	 */
	private boolean delAlbumOperation(String albumPath, List<MediaFileInfo> medias) {
		boolean canDel = !(albumPath.equals(GalleryLocalActivity.collectionPath));
		File filePath = new File(albumPath);
		if (delOperation(medias)) {
			File[] files = filePath.listFiles();
			if (files != null) {
				if (files.length == 0 && canDel) {
					return filePath.delete();
				}
			} else if (canDel) {
				return filePath.delete();
			}
		}
		return false;
	}

	/**
	 * 创建新相册
	 * @param file
	 * @return
	 */
	public boolean createNewAlbum(File file) {
		if (file.mkdirs()) {
			ContentValues cv = new ContentValues();
			cv.put(MediaStore.Files.FileColumns.DATA, file.getPath());
			cv.put(format, formatstyle);// 格式化为文件夹
			mContext.getContentResolver().insert(MediaStore.Files.getContentUri(external), cv);
			return true;
		}
		return false;
	}

	/**
	 * 删除
	 **/
	public boolean delOperation(List<MediaFileInfo> selectFiles) {
		String where = buildSelectFilesId(selectFiles);
		int index = mContext.getContentResolver().delete(MediaStore.Files.getContentUri("external"), MediaStore.Files.FileColumns._ID + " in (" + where + ")", null);
		LogUtil.d(TAG, "-----delOperation index:" + index + " selectFiles:" + selectFiles.size());
		return true;
	}
	
	private boolean delePath(List<MediaFileInfo> selectFiles){
		StringBuffer buffer = new StringBuffer();
		int size = selectFiles.size();
		for (int i = 0; i < size - 1; i++) {
			buffer.append(selectFiles.get(i).filePath + " ,");
		}
		buffer.append(selectFiles.get(size - 1).filePath);
		int index = mContext.getContentResolver().delete(MediaStore.Files.getContentUri("external"), MediaStore.Files.FileColumns.DATA + " in (" + buffer.toString() + ")", null);
		LogUtil.d(TAG, "-----delePath index:" + index + " selectFiles:" + selectFiles.size());
		return true;
	}

	/**
	 * 收藏
	 **/
	public boolean addFavoriteOperation(List<MediaFileInfo> selectFiles) {
		String where = buildSelectFilesId(selectFiles);
		ContentValues values = new ContentValues();
		values.put("favorite", 1);
		int index = mContext.getContentResolver().update(MediaStore.Files.getContentUri("external"), values, MediaStore.Files.FileColumns._ID + " in (" + where + ")", null);
		LogUtil.d(TAG, "-----addFavoriteOperation index:" + index + " selectFiles:" + selectFiles.size());
		return true;
	}

	/**
	 * 取消收藏
	 **/
	public boolean removeFavoriteOperation(List<MediaFileInfo> selectFiles) {
		String where = buildSelectFilesId(selectFiles);
		ContentValues values = new ContentValues();
		values.put("favorite", 0);
		int index = mContext.getContentResolver().update(MediaStore.Files.getContentUri("external"), values, MediaStore.Files.FileColumns._ID + " in (" + where + ")", null);
		LogUtil.d(TAG, "-----addFavoriteOperation index:" + index + " selectFiles:" + selectFiles.size());
		return true;
	}

	private String buildSelectFilesId(List<MediaFileInfo> selectFiles) {
		StringBuffer buffer = new StringBuffer();
		int size = selectFiles.size();
		for (int i = 0; i < size - 1; i++) {
			buffer.append(selectFiles.get(i).dbId + " ,");
		}
		buffer.append(selectFiles.get(size - 1).dbId);
		return buffer.toString();
	}

	private Intent buildSendFile(List<MediaFileInfo> files) {
		ArrayList<Uri> uris = new ArrayList<Uri>();
		MediaFileInfo fileInfo = files.get(0);
		boolean isImage = fileInfo.isImage;
		String mimeType = "*/*";
		if (isImage) {
			mimeType = "image/*";
		} else {
			mimeType = "video/*";
		}
		for (MediaFileInfo info : files) {
			uris.add(Uri.fromFile(new File(info.filePath)));
		}

		if (uris.size() == 0)
			return null;

		boolean multiple = uris.size() > 1;
		Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE : android.content.Intent.ACTION_SEND);
		intent.setType(mimeType);
		if (multiple) {
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} else {
			intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
		}
		LogUtil.d(TAG, "mimeType::" + mimeType);
		return intent;
	}

	/**
	 * action for directory scanning
	 */
	public final static String ACTION_DIR_SCAN = "android.intent.action.AURORA_DIRECTORY_SCAN";

	/**
	 * action for file scanning
	 */
	public final static String ACTION_FILE_SCAN = "android.intent.action.AURORA_FILE_SCAN";

	/**
	 * 发送扫描广播
	 */
	public void sendBroadcastScan(String scan, String path) {
		Intent intent = new Intent();
		intent.setAction(scan);
		intent.setData(Uri.fromFile(new File(path)));
		mContext.sendBroadcast(intent);
	}

	private MoveAsyncTask moveAsyncTask;

	/**
	 * 移动
	 * @param mediaFileInfos
	 * @param dstPath
	 */
	public void moveOperation(List<MediaFileInfo> mediaFileInfos, String dstPath) {
		if (mediaFileInfos == null || mediaFileInfos.isEmpty() || TextUtils.isEmpty(dstPath)) {
			return;
		}
		if (moveAsyncTask != null && (moveAsyncTask.getStatus() != Status.FINISHED && mIsTaskFinished)) {
			LogUtil.e(TAG, "----MoveAsyncTask is busy");
			return;
		}
		LogUtil.d(TAG, "-------------mContext:" + mContext+" dstPath:"+dstPath);
		AuroraProgressDialog progress = new AuroraProgressDialog(mContext);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setMax(100);
		moveAsyncTask = new MoveAsyncTask(mediaFileInfos, progress);
		moveAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dstPath);
	}

	private boolean mIsTaskFinished = false;

	private class MoveAsyncTask extends AsyncTask<String, Integer, List<MediaFileInfo>> {
		private List<MediaFileInfo> mediaFileInfos;
		private AuroraProgressDialog progress;
		private long count = 0, totalSize = 0, oldCount = 0, i = 0;
		private String dstPath;
		private boolean isDbIdMediaFileInfo=true;

		public MoveAsyncTask(List<MediaFileInfo> mediaFileInfos, AuroraProgressDialog progress) {
			super();
			this.mediaFileInfos = mediaFileInfos;
			this.progress = progress;
		}

		@Override
		protected List<MediaFileInfo> doInBackground(String... params) {
			String dstPath = params[0];
			this.dstPath = dstPath;
			MediaFileInfo info = mediaFileInfos.get(0);
			List<MediaFileInfo> medias = new ArrayList<MediaFileInfo>();
			File dstFile = new File(dstPath);
			long freeSpace = dstFile.getFreeSpace();
			if (isSomeRootPath(dstPath, info.filePath)) {
				totalSize = mediaFileInfos.size();
			} else {
				totalSize = SettingLocalUtils.getAllSize(mediaFileInfos);
				if (freeSpace <= totalSize + 10000) {
					LogUtil.e(TAG, "moveFileOrDir no free memory");
					return null;
				}
			}
			for (int k = 0; k < mediaFileInfos.size(); k++) {
				MediaFileInfo fileInfo = mediaFileInfos.get(k);
				isDbIdMediaFileInfo=isDbIdMediaFileInfo&&(fileInfo.dbId!=0?true:false);
				File srcFile = new File(fileInfo.filePath);
				if (!moveFile(srcFile, new File(dstFile, srcFile.getName()))) {
					medias.add(fileInfo);
				}

			}

			return medias;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsTaskFinished = false;
			progress.show();
			publishProgress(0);
		}

		@Override
		protected void onPostExecute(List<MediaFileInfo> result) {
			super.onPostExecute(result);
			mIsTaskFinished = true;
			if(!result.isEmpty()){
				boolean s = isDbIdMediaFileInfo?delOperation(result):delePath(result);
			}
			sendBroadcastScan(ACTION_DIR_SCAN, dstPath);
			progress.dismiss();

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progress.setProgress(values[0]);
		}
		
		

		/**
		 * 判断两个路径是否是同一个存储器 add by JXH 2014-8-15
		 * @param f
		 * @param d
		 * @return
		 */
		private boolean isSomeRootPath(String f, String d) {
			List<String> root = StorageUtils.getAllStorages(true, mContext);
			if (root != null && !TextUtils.isEmpty(f) && !TextUtils.isEmpty(d)) {
				for (int i = 0; i < root.size(); i++) {
					if (f.startsWith(root.get(i)) && d.startsWith(root.get(i))) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean moveFile(File srcFile, File destFile) {
			if (destFile.exists()) {
				destFile = new File(SettingLocalUtils.autoGenerateName(destFile));
			}
			LogUtil.d(TAG, "---------moveFile srcFile:" + srcFile.getAbsolutePath() + " destFile:" + destFile.getAbsolutePath());
			boolean rename = srcFile.renameTo(destFile);
			LogUtil.d(TAG, "---------moveFile rename:" + rename);
			if (!rename) {
				File parentFile = destFile.getParentFile();
				if (parentFile != null) {
					if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
						LogUtil.e(TAG, "Destination '" + parentFile.getAbsolutePath() + "' directory cannot be created");
						return false;
					}
				}

				FileInputStream fis = null;
				FileOutputStream fos = null;
				try {
					int n = 0;
					byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
					fis = new FileInputStream(srcFile);
					fos = new FileOutputStream(destFile);
					while (EOF != (n = fis.read(buffer))) {
						fos.write(buffer, 0, n);
						count += n;
						if (totalSize != 0) {
							int index = (int) ((Float.valueOf(count) / Float.valueOf(totalSize)) * 100);
							if (index > oldCount) {
								oldCount = index;
								if (oldCount == 100) {
									oldCount = 0;
								}
								publishProgress(index);
							}
						}
					}
					fos.getFD().sync();
				} catch (Exception exception) {
					exception.printStackTrace();
					return false;
				} finally {
					closeQuietly(fos);
					closeQuietly(fis);
				}

				if (srcFile.length() != destFile.length()) {
					LogUtil.e(TAG, "Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
				}
			} else {
				i++;
				int temp = (int) ((Float.valueOf(i + 1) / Float.valueOf(totalSize)) * 100);
				if (temp > oldCount) {
					oldCount = temp;
					LogUtil.d(TAG, "---------moveFile temp:" + temp);
					publishProgress(temp);
				}
			}
			return true;
		}

	}

	public static void closeQuietly(InputStream input) {
		closeQuietly((Closeable) input);
	}

	public static void closeQuietly(OutputStream output) {
		closeQuietly((Closeable) output);
	}

	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	public boolean renameTo(File oldFile, File newFile) {
		try {
			libcore.io.Libcore.os.rename(oldFile.getAbsolutePath(), newFile.getAbsolutePath());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
