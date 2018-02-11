package com.aurora.fileObserver;

import android.content.Context;
import android.os.FileObserver;

import com.aurora.tools.LogUtil;

/**
 * manage files and directories events
 * 
 * @author
 * 
 */
public class AuroraFileObserver extends FileObserver {

	private final static String TAG = "AuroraFileObserver";

	public static final int ISDIR = 0x40000000;

	private AuroraFileObserverListener fileObserverListener;

	/**
	 * @param fileObserverListener
	 *            the fileObserverListener to set
	 */
	public void setFileObserverListener(
			AuroraFileObserverListener fileObserverListener) {
		this.fileObserverListener = fileObserverListener;
	}

	public AuroraFileObserver(String parentPath, Context mContext) {
		super(parentPath, FileObserver.CLOSE_NOWRITE | FileObserver.CLOSE_WRITE
				| FileObserver.DELETE | FileObserver.DELETE_SELF
				| FileObserver.MOVED_FROM | FileObserver.MOVED_TO);

	}
	

	@Override
	public void onEvent(int event, String path) {
//		LogUtil.log(TAG, " enter FileWatcher: onEvent() path: " + path);

		switch (event) {

		case CLOSE_NOWRITE:
//			LogUtil.log(TAG, " close nowrite " + path);

			break;
		case CLOSE_WRITE:
//			LogUtil.log(TAG, " close write " + path);
			if (fileObserverListener != null) {
				fileObserverListener.onFileModified(path);
			}
			break;
		case CREATE:
//			LogUtil.log(TAG, " create " + path);
			if (fileObserverListener != null) {
				fileObserverListener.onFileCreated(path);
			}

			break;
		case DELETE:
//			LogUtil.log(TAG, " delete " + path);
			if (fileObserverListener != null) {
				fileObserverListener.onFileDeleted(path);
			}

			break;
		case DELETE_SELF:
//			LogUtil.log(TAG, " delete self " + path);
			if (fileObserverListener != null) {
				fileObserverListener.onFileDeleted(path);
			}
			break;
		case MOVE_SELF:
//			LogUtil.log(TAG, " move self " + path);

			break;
		case MOVED_FROM:
//			LogUtil.log(TAG, " moved from " + path);

			break;
		case MOVED_TO:
//			LogUtil.log(TAG, " moved to " + path);
			if (fileObserverListener != null) {
				fileObserverListener.onFileRenamed(path);
			}

			break;
		default:

			switch (event - ISDIR) {
			case CLOSE_WRITE:
//				LogUtil.log(TAG, " default close write " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileModified(path);
				}
				break;

			case CLOSE_NOWRITE:
//				LogUtil.log(TAG, " default close nowrite " + path);

				break;

			case CREATE:
//				LogUtil.log(TAG, " default create " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileCreated(path);
				}
				break;
			case DELETE:
//				LogUtil.log(TAG, " default delete " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileDeleted(path);
				}
				break;
			case DELETE_SELF:
//				LogUtil.log(TAG, " default delete self " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileDeleted(path);
				}
				break;
			case MODIFY:
//				LogUtil.log(TAG, " default modify " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileModified(path);
				}
				break;
			case MOVE_SELF:
//				LogUtil.log(TAG, " default move self " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileDeleted(path);
				}
				break;
			case MOVED_FROM:
//				LogUtil.log(TAG, " default moved from " + path);
				break;
			case MOVED_TO:
//				LogUtil.log(TAG, " default move to " + path);
				if (fileObserverListener != null) {
					fileObserverListener.onFileRenamed(path);
				}
				break;
			}

			break;

		}

	}

}
