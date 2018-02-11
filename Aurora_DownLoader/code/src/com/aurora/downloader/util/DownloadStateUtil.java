package com.aurora.downloader.util;

import android.app.DownloadManager;
import android.provider.Downloads;

public class DownloadStateUtil {

	public enum Status {
		RUNNING, WAITING, PAUSED, SUCESS, FAILED
	}

	public static final Status getDownloadStates(int status, int control) {
		// modify by JXH 2014-7-28 pause test ||
		if (control == Downloads.Impl.CONTROL_PAUSED
				|| status == DownloadManager.STATUS_PAUSED) {
			return Status.PAUSED;
		}
		switch (status) {
		case DownloadManager.STATUS_PENDING:
			return Status.WAITING;
		case DownloadManager.STATUS_RUNNING:
			return Status.RUNNING;
		case DownloadManager.STATUS_PAUSED:
			return Status.PAUSED;
		case DownloadManager.STATUS_FAILED:
			return Status.FAILED;
		case DownloadManager.STATUS_SUCCESSFUL:
			return Status.SUCESS;
		}
		return Status.RUNNING;
	}
}
